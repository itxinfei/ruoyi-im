/**
 * 消息管理模块 - 增强版
 * 管理消息列表、消息操作（发送、编辑、撤回等）
 * 新增：消息去重、离线消息队列、失败重试机制
 */
import {
  getMessages,
  sendMessage as apiSendMessage,
  markAsRead,
  deleteMessage,
  recallMessage,
  editMessage as apiEditMessage,
  forwardMessage as apiForwardMessage
} from '@/api/im'
import { formatMessagePreview, formatMessagePreviewFromObject } from '@/utils/message'
import imWebSocket from '@/utils/websocket/imWebSocket'
import { info, warn, error } from '@/utils/logger'

/**
 * 消息发送状态枚举（与后端 SendStatus 枚举对应）
 * PENDING(0) - 等待发送
 * SENDING(1) - 发送中
 * DELIVERED(2) - 已送达
 * READ(3) - 已读
 * FAILED(4) - 发送失败
 */
export const SEND_STATUS = {
  PENDING: 0,
  SENDING: 1,
  DELIVERED: 2,
  READ: 3,
  FAILED: 4,
  RECALLED: 5
}

/**
 * 发送队列配置
 */
export const SENDING_QUEUE_CONFIG = {
  MAX_QUEUE_SIZE: 100,
  CLEANUP_INTERVAL: 60 * 1000,
  EXPIRED_TIME: 10 * 60 * 1000
}

/**
 * 离线消息队列配置
 */
export const OFFLINE_QUEUE_CONFIG = {
  MAX_QUEUE_SIZE: 500,
  FLUSH_INTERVAL: 5000,
  MAX_RETRY_TIMES: 3
}

/**
 * 将后端 sendStatus 数值映射为前端 UI 使用的字符串
 * @param {number} sendStatus - 后端 sendStatus 枚举值
 * @returns {string} 前端状态字符串
 */
function mapSendStatusToUi(sendStatus) {
  const statusMap = {
    [SEND_STATUS.PENDING]: 'pending',
    [SEND_STATUS.SENDING]: 'sending',
    [SEND_STATUS.DELIVERED]: 'delivered',
    [SEND_STATUS.READ]: 'read',
    [SEND_STATUS.FAILED]: 'failed',
    [SEND_STATUS.RECALLED]: 'recalled'
  }
  return statusMap[sendStatus] || 'pending'
}

/**
 * 判断消息是否为当前用户发送
 * 优先使用后端返回的 isSelf 字段，降级通过 senderId 比较
 */
function isOwnMessage(message, currentUserId) {
  if (message.isSelf === true) { return true }
  if (message.isSelf === false) { return false }
  const senderId = String(message.senderId || '')
  const userId = String(currentUserId || '')
  return senderId !== '' && senderId === userId
}

// 简单UUID生成
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

export default {
  namespaced: true,

  state: () => ({
    // 消息列表（按 sessionId 分组）
    messages: {},

    // 发送中的消息 Map（用于内存泄漏防护）
    // key: clientMsgId, value: { timestamp, sessionId, message }
    sendingMessages: new Map(),

    // 当前正在回复的消息
    replyingMessage: null,

    // 加载状态
    loading: false,

    // 选中的消息列表（用于多选操作）
    selectedMessages: new Set(),

    // 最后点击的消息ID（用于连续选择）
    lastClickedMessageId: null,

    // ========== 新增：消息去重和离线处理 ==========

    // 消息 ID 去重缓存（使用 Map 实现 LRU 策略）
    // key: messageId, value: { timestamp } - 记录接收时间用于过期清理
    receivedMessageIds: new Map(),

    // 最大保留的消息 ID 数量（防止内存溢出）
    maxReceivedIdsSize: 10000,

    // 消息 ID 过期时间（30分钟）
    receivedIdsExpireTime: 30 * 60 * 1000,

    // 离线消息队列（网络断开时暂存消息）
    // { messageId, sessionId, message, retryCount, timestamp }
    offlineQueue: [],

    // 是否处于离线模式
    isOffline: false,

    // 失败消息重试队列
    // { clientMsgId, sessionId, message, retryCount, timestamp }
    failedRetryQueue: []
  }),

  getters: {
    // 获取当前会话的消息列表
    currentMessages: (state, getters, rootState) => {
      if (!rootState.im?.session?.currentSession) {return []}
      return state.messages[rootState.im.session.currentSession.id] || []
    },

    // 获取指定会话的消息列表
    messagesBySessionId: state => sessionId => {
      return state.messages[sessionId] || []
    },

    // 获取选中消息的数量
    selectedMessageCount: state => state.selectedMessages.size,

    // 获取选中消息的列表
    selectedMessageList: (state, getters, rootState) => {
      const sessionId = rootState.im?.session?.currentSession?.id
      if (!sessionId || !state.messages[sessionId]) {return []}

      return Array.from(state.selectedMessages).map(messageId => {
        return state.messages[sessionId].find(msg => msg.id === messageId)
      }).filter(msg => msg !== undefined)
    },

    // ========== 发送中消息管理 ==========
    // 获取发送中的消息列表
    sendingMessagesList: state => {
      return Array.from(state.sendingMessages.values())
    },

    // 获取发送中的消息列表（别名，用于向后兼容）
    getSendingMessages: state => {
      return Array.from(state.sendingMessages.values())
    },

    // 检查消息是否在发送中
    isMessageSending: state => clientMsgId => {
      return state.sendingMessages.has(clientMsgId)
    },

    // 获取发送队列大小
    sendingQueueSize: state => {
      return state.sendingMessages.size
    },

    // 检查发送队列是否已满
    isSendingQueueFull: state => {
      return state.sendingMessages.size >= SENDING_QUEUE_CONFIG.MAX_QUEUE_SIZE
    },

    // ========== 离线和重试管理 ==========
    // 获取离线队列大小
    offlineQueueSize: state => {
      return state.offlineQueue.length
    },

    // 获取失败重试队列大小
    failedRetryQueueSize: state => {
      return state.failedRetryQueue.length
    },

    // 是否处于离线状态
    isOfflineMode: state => {
      return state.isOffline
    },

    // 检查消息是否已接收（去重）
    hasReceivedMessage: state => messageId => {
      // 检查是否存在且未过期
      const item = state.receivedMessageIds.get(messageId)
      if (!item) {return false}
      // 检查是否过期
      if (Date.now() - item.timestamp > state.receivedIdsExpireTime) {
        state.receivedMessageIds.delete(messageId)
        return false
      }
      return true
    }
  },

  mutations: {
    // 设置消息列表
    SET_MESSAGES(state, { sessionId, messages }) {
      state.messages[sessionId] = messages
    },

    // 预加消息（历史消息加载更多）
    PREPEND_MESSAGES(state, { sessionId, messages }) {
      if (!state.messages[sessionId]) {
        state.messages[sessionId] = []
      }
      state.messages[sessionId] = [...messages, ...state.messages[sessionId]]
    },

    // 添加单条消息
    ADD_MESSAGE(state, { sessionId, message }) {
      if (!sessionId) {
        warn('MessageStore', 'ADD_MESSAGE 缺少 sessionId，消息被丢弃', message)
        return
      }
      
      if (!state.messages[sessionId]) {
        state.messages[sessionId] = []
      }
      
      // 严格校验消息是否属于该会话
      const msgSessionId = message.conversationId || message.sessionId
      if (msgSessionId && String(msgSessionId) !== String(sessionId)) {
        warn('MessageStore', `检测到跨会话消息注入！试图将 ${msgSessionId} 的消息推入 ${sessionId}`)
        return
      }

      const index = state.messages[sessionId].findIndex(m => m.id === message.id)
      if (index === -1) {
        state.messages[sessionId] = [...state.messages[sessionId], message]
      } else {
        state.messages[sessionId] = [
          ...state.messages[sessionId].slice(0, index),
          { ...state.messages[sessionId][index], ...message },
          ...state.messages[sessionId].slice(index + 1)
        ]
      }
    },

    // 更新消息
    UPDATE_MESSAGE(state, { sessionId, message }) {
      if (!state.messages[sessionId]) {
        return
      }
      // 先按 id 查找，找不到再按 clientMsgId 查找（兼容临时消息更新）
      let index = state.messages[sessionId].findIndex(m => m.id === message.id)
      if (index === -1 && message.clientMsgId) {
        index = state.messages[sessionId].findIndex(m => m.clientMsgId === message.clientMsgId)
      }
      if (index !== -1) {
        // 创建新数组触发响应式更新
        state.messages[sessionId] = [
          ...state.messages[sessionId].slice(0, index),
          { ...state.messages[sessionId][index], ...message },
          ...state.messages[sessionId].slice(index + 1)
        ]
      }
    },

    // 删除消息
    DELETE_MESSAGE(state, { sessionId, messageId }) {
      if (!state.messages[sessionId]) {
        return
      }
      const index = state.messages[sessionId].findIndex(m => m.id === messageId)
      if (index !== -1) {
        // 创建新数组触发响应式更新
        state.messages[sessionId] = [
          ...state.messages[sessionId].slice(0, index),
          ...state.messages[sessionId].slice(index + 1)
        ]
      }
    },

    // 设置正在回复的消息
    SET_REPLYING_MESSAGE(state, message) {
      state.replyingMessage = message
    },

    // 设置加载状态
    SET_LOADING(state, value) {
      state.loading = value
    },

    // 清空消息状态
    CLEAR_STATE(state) {
      state.messages = {}
      state.replyingMessage = null
    },

    // 切换消息选中状态
    TOGGLE_MESSAGE_SELECTION(state, messageId) {
      if (state.selectedMessages.has(messageId)) {
        state.selectedMessages.delete(messageId)
      } else {
        state.selectedMessages.add(messageId)
      }
      // 触发响应式更新
      state.selectedMessages = new Set(state.selectedMessages)
    },

    // 清空选中消息
    CLEAR_MESSAGE_SELECTION(state) {
      state.selectedMessages = new Set()
      state.lastClickedMessageId = null
    },

    // 范围选择消息（连续选择）
    SELECT_MESSAGE_RANGE(state, { sessionId, startMessageId, endMessageId }) {
      if (!state.messages[sessionId]) {return}

      const messages = state.messages[sessionId]
      const startIndex = messages.findIndex(m => m.id === startMessageId)
      const endIndex = messages.findIndex(m => m.id === endMessageId)

      if (startIndex === -1 || endIndex === -1) {return}

      // 确定范围
      const minIndex = Math.min(startIndex, endIndex)
      const maxIndex = Math.max(startIndex, endIndex)

      // 选中范围内的所有消息
      for (let i = minIndex; i <= maxIndex; i++) {
        state.selectedMessages.add(messages[i].id)
      }

      // 触发响应式更新
      state.selectedMessages = new Set(state.selectedMessages)
    },

    // 设置最后点击的消息ID（用于范围选择）
    SET_LAST_CLICKED_MESSAGE(state, messageId) {
      state.lastClickedMessageId = messageId
    },

    /**
     * 更新消息发送状态
     * 用于 WebSocket 接收到 message_status 事件时更新消息状态
     * @param {Object} state - Vuex state
     * @param {Object} payload - { sessionId, messageId, sendStatus }
     */
    UPDATE_MESSAGE_STATUS(state, { sessionId, messageId, sendStatus }) {
      if (!state.messages[sessionId]) {
        return
      }
      const index = state.messages[sessionId].findIndex(m => m.id === messageId)
      if (index !== -1) {
        state.messages[sessionId][index] = {
          ...state.messages[sessionId][index],
          status: mapSendStatusToUi(sendStatus)
        }
      }
    },

    /**
     * 替换临时消息为真实消息（乐观更新完成后）
     * 按 tempId 查找（匹配 id 或 clientMsgId），整体替换
     * @param {Object} state - Vuex state
     * @param {Object} payload - { sessionId, tempId, realMessage }
     */
    REPLACE_TEMP_MESSAGE(state, { sessionId, tempId, realMessage }) {
      if (!state.messages[sessionId]) { return }
      const messages = state.messages[sessionId]
      const index = messages.findIndex(m => m.id === tempId || m.clientMsgId === tempId)
      if (index !== -1) {
        // 关键：保留 tempMessage 中的 UI 状态，合并 realMessage 的数据
        const updatedMessage = {
          ...messages[index],
          ...realMessage,
          status: mapSendStatusToUi(realMessage.sendStatus)
        }
        
        state.messages[sessionId] = [
          ...messages.slice(0, index),
          updatedMessage,
          ...messages.slice(index + 1)
        ]
      }
    },

    /**
     * 标记消息状态（sending/failed/uploading 等）
     * @param {Object} state - Vuex state
     * @param {Object} payload - { sessionId, messageId, status }
     */
    MARK_MESSAGE_STATUS(state, { sessionId, messageId, status }) {
      if (!state.messages[sessionId]) { return }
      const messages = state.messages[sessionId]
      const index = messages.findIndex(m => m.id === messageId || m.clientMsgId === messageId)
      if (index !== -1) {
        state.messages[sessionId] = [
          ...messages.slice(0, index),
          { ...messages[index], status },
          ...messages.slice(index + 1)
        ]
      }
    },

    // ========== 发送中消息管理 Mutations ==========

    /**
     * 添加发送中的消息
     * @param {Object} state - Vuex state
     * @param {Object} payload - { clientMsgId, sessionId, message }
     */
    ADD_SENDING_MESSAGE(state, { clientMsgId, sessionId, message }) {
      state.sendingMessages.set(clientMsgId, {
        timestamp: message?.timestamp || Date.now(),
        sessionId,
        message
      })
    },

    /**
     * 移除发送中的消息
     * @param {Object} state - Vuex state
     * @param {string} clientMsgId - 客户端消息ID
     */
    REMOVE_SENDING_MESSAGE(state, clientMsgId) {
      state.sendingMessages.delete(clientMsgId)
    },

    /**
     * 清理过期的发送中消息（超过配置时间）
     * 避免内存泄漏
     * @param {Object} state - Vuex state
     */
    CLEANUP_EXPIRED_MESSAGES(state) {
      const now = Date.now()
      const expiredTime = SENDING_QUEUE_CONFIG.EXPIRED_TIME

      let cleanedCount = 0
      for (const [clientMsgId, data] of state.sendingMessages.entries()) {
        if (now - data.timestamp > expiredTime) {
          state.sendingMessages.delete(clientMsgId)
          cleanedCount++
        }
      }

      // 如果清理了消息，输出日志
      if (cleanedCount > 0) {
        info('MessageStore', `[发送队列清理] 清理了 ${cleanedCount} 条过期消息`)
      }
    },

    // ========== 消息去重和离线处理 Mutations ==========

    /**
     * 添加消息 ID 到去重集合（LRU 缓存策略）
     * @param {Object} state - Vuex state
     * @param {string} messageId - 消息 ID
     */
    ADD_RECEIVED_MESSAGE_ID(state, messageId) {
      const now = Date.now()

      // 如果已存在，更新时间戳（刷新 LRU）
      if (state.receivedMessageIds.has(messageId)) {
        state.receivedMessageIds.set(messageId, { timestamp: now })
        return
      }

      // 添加新记录
      state.receivedMessageIds.set(messageId, { timestamp: now })

      // 限制集合大小（LRU 淘汰）
      if (state.receivedMessageIds.size > state.maxReceivedIdsSize) {
        // 删除最旧的 10%（Map 保持插入顺序，最早的在前面）
        const deleteCount = Math.floor(state.maxReceivedIdsSize * 0.1)
        let deleted = 0
        for (const key of state.receivedMessageIds.keys()) {
          if (deleted >= deleteCount) {break}
          state.receivedMessageIds.delete(key)
          deleted++
        }
      }
    },

    /**
     * 批量添加消息 ID 到去重集合
     * @param {Object} state - Vuex state
     * @param {Array<string>} messageIds - 消息 ID 数组
     */
    ADD_RECEIVED_MESSAGE_IDS(state, messageIds) {
      const now = Date.now()
      messageIds.forEach(id => {
        state.receivedMessageIds.set(id, { timestamp: now })
      })

      // 限制集合大小
      if (state.receivedMessageIds.size > state.maxReceivedIdsSize) {
        const deleteCount = Math.floor(state.maxReceivedIdsSize * 0.1)
        let deleted = 0
        for (const key of state.receivedMessageIds.keys()) {
          if (deleted >= deleteCount) {break}
          state.receivedMessageIds.delete(key)
          deleted++
        }
      }
    },

    /**
     * 清理过期的去重 ID（基于时间戳）
     * @param {Object} state - Vuex state
     */
    CLEANUP_RECEIVED_IDS(state) {
      const now = Date.now()
      const expireTime = state.receivedIdsExpireTime
      let cleanedCount = 0

      // 遍历并删除过期记录
      for (const [id, data] of state.receivedMessageIds.entries()) {
        if (now - data.timestamp > expireTime) {
          state.receivedMessageIds.delete(id)
          cleanedCount++
        }
      }

      if (cleanedCount > 0) {
        info('MessageStore', `[消息去重] 清理了 ${cleanedCount} 条过期记录`)
      }
    },

    /**
     * 设置离线状态
     * @param {Object} state - Vuex state
     * @param {boolean} isOffline - 是否离线
     */
    SET_OFFLINE_STATUS(state, isOffline) {
      state.isOffline = isOffline
      info('MessageStore', `[离线状态] ${isOffline ? '已进入离线模式' : '已恢复在线'}`)
    },

    /**
     * 添加消息到离线队列
     * @param {Object} state - Vuex state
     * @param {Object} payload - { messageId, sessionId, message }
     */
    ADD_TO_OFFLINE_QUEUE(state, { messageId, sessionId, message }) {
      // 检查队列是否已满
      if (state.offlineQueue.length >= OFFLINE_QUEUE_CONFIG.MAX_QUEUE_SIZE) {
        // 移除最旧的消息
        state.offlineQueue.shift()
        warn('MessageStore', '[离线队列] 队列已满，移除最旧消息')
      }

      state.offlineQueue.push({
        messageId,
        sessionId,
        message,
        retryCount: 0,
        timestamp: Date.now()
      })
    },

    /**
     * 清空离线队列
     * @param {Object} state - Vuex state
     */
    CLEAR_OFFLINE_QUEUE(state) {
      state.offlineQueue = []
      info('MessageStore', '[离线队列] 队列已清空')
    },

    /**
     * 添加消息到失败重试队列
     * @param {Object} state - Vuex state
     * @param {Object} payload - { clientMsgId, sessionId, message }
     */
    ADD_TO_FAILED_RETRY_QUEUE(state, { clientMsgId, sessionId, message }) {
      // 检查是否已在队列中
      const existingIndex = state.failedRetryQueue.findIndex(
        item => item.clientMsgId === clientMsgId
      )

      if (existingIndex === -1) {
        state.failedRetryQueue.push({
          clientMsgId,
          sessionId,
          message,
          retryCount: 0,
          timestamp: Date.now()
        })
      }
    },

    /**
     * 从失败重试队列中移除
     * @param {Object} state - Vuex state
     * @param {string} clientMsgId - 客户端消息 ID
     */
    REMOVE_FROM_FAILED_RETRY_QUEUE(state, clientMsgId) {
      const index = state.failedRetryQueue.findIndex(
        item => item.clientMsgId === clientMsgId
      )
      if (index !== -1) {
        state.failedRetryQueue.splice(index, 1)
      }
    },

    /**
     * 更新失败重试队列中的消息重试次数
     * @param {Object} state - Vuex state
     * @param {string} clientMsgId - 客户端消息 ID
     */
    INCREMENT_RETRY_COUNT(state, clientMsgId) {
      const item = state.failedRetryQueue.find(
        item => item.clientMsgId === clientMsgId
      )
      if (item) {
        item.retryCount++
      }
    }
  },

  actions: {
    // 加载消息列表
    async loadMessages({ commit, rootState }, { sessionId, lastMessageId = null, pageSize = 20, isLoadMore = false }) {
      commit('SET_LOADING', true)
      try {
        const res = await getMessages(sessionId, { lastId: lastMessageId, pageSize })
        if (res.code === 200 && res.data) {
          const currentUserId = rootState.im?.currentUser?.id
          // 规范化消息字段
          const normalized = res.data.map(msg => ({
            ...msg,
            type: (msg.type || '').toUpperCase(),
            senderName: msg.senderName || msg.senderNickname || msg.nickname || msg.userName || '未知用户',
            senderAvatar: msg.senderAvatar || msg.avatar || '',
            isOwn: isOwnMessage(msg, currentUserId)
          }))
          // 后端已经按时间升序返回(oldest first, newest at bottom),无需反转
          const transformed = normalized
          if (isLoadMore) {
            commit('PREPEND_MESSAGES', { sessionId, messages: transformed })
          } else {
            commit('SET_MESSAGES', { sessionId, messages: transformed })
          }
          return transformed
        }
      } finally {
        commit('SET_LOADING', false)
      }
    },

    // 发送消息（含乐观 UI 和发送队列管理）
    async sendMessage({ commit, dispatch, state }, { sessionId, type = 'TEXT', content, replyToMessageId = null }) {
      // ========== 检查发送队列是否已满 ==========
      if (state.sendingMessages.size >= SENDING_QUEUE_CONFIG.MAX_QUEUE_SIZE) {
        const error = new Error(
          `发送队列已满（${SENDING_QUEUE_CONFIG.MAX_QUEUE_SIZE}条），请稍后重试`
        )
        error.code = 'QUEUE_FULL'
        error.queueSize = state.sendingMessages.size
        error.maxQueueSize = SENDING_QUEUE_CONFIG.MAX_QUEUE_SIZE
        throw error
      }

      const clientMsgId = generateUUID()

      // ========== 乐观 UI：先添加消息到发送队列 ==========
      const tempMessage = {
        clientMsgId,
        conversationId: sessionId,
        type,
        content,
        replyToMessageId,
        sendStatus: SEND_STATUS.PENDING, // 发送中状态
        timestamp: Date.now(),
        isOwn: true
      }

      // 添加到发送队列（用于内存泄漏防护）
      commit('ADD_SENDING_MESSAGE', {
        clientMsgId,
        sessionId,
        message: tempMessage
      })

      // 添加到消息列表（乐观 UI）
      commit('ADD_MESSAGE', { sessionId, message: tempMessage })

      try {
        // 发送消息到服务器
        const res = await apiSendMessage({
          conversationId: sessionId,
          type,
          content,
          replyToMessageId,
          clientMsgId
        })

        if (res.code === 200 && res.data) {
          // 发送成功：移除发送队列
          commit('REMOVE_SENDING_MESSAGE', clientMsgId)

          // 规范化响应中的消息类型
          const normalizedMessage = {
            ...res.data,
            type: (res.data.type || '').toUpperCase(),
            isOwn: true,
            // 移除临时状态，使用服务器返回的状态
            sendStatus: res.data.sendStatus
          }

          // 更新消息列表（替换临时消息）
          commit('UPDATE_MESSAGE', {
            sessionId,
            message: {
              clientMsgId,
              ...normalizedMessage
            }
          })

          // 更新会话列表
          commit('im/session/UPDATE_SESSION', {
            id: sessionId,
            lastMessage: formatMessagePreviewFromObject(normalizedMessage),
            lastMessageTime: normalizedMessage.timestamp,
            lastMessageType: type
          }, { root: true })

          return normalizedMessage
        } else {
          // 服务器返回错误：标记为失败
          commit('REMOVE_SENDING_MESSAGE', clientMsgId)
          commit('UPDATE_MESSAGE', {
            sessionId,
            message: {
              clientMsgId,
              sendStatus: SEND_STATUS.FAILED,
              sendErrorCode: 'SERVER_ERROR'
            }
          })
          throw new Error(res.msg || '发送消息失败')
        }
      } catch (error) {
        // 网络错误或其他异常：标记为失败
        commit('REMOVE_SENDING_MESSAGE', clientMsgId)
        commit('UPDATE_MESSAGE', {
          sessionId,
          message: {
            clientMsgId,
            sendStatus: SEND_STATUS.FAILED,
            sendErrorCode: 'NETWORK_ERROR'
          }
        })

        // 网络错误时加入离线队列，网络恢复后自动重试
        if (state.isOffline || !navigator.onLine) {
          commit('ADD_TO_OFFLINE_QUEUE', {
            messageId: clientMsgId,
            sessionId,
            message: { type, content, replyToMessageId }
          })
        }

        throw error
      }
    },

    // 编辑消息
    async editMessage({ commit, state, rootState }, { messageId, content }) {
      const res = await apiEditMessage(messageId, { content })
      if (res.code === 200) {
        // 查找该消息在哪个会话中 (通常是当前会话)
        const sessionId = rootState.im?.session?.currentSession?.id
        if (sessionId && state.messages[sessionId]) {
          const editedMsg = { ...state.messages[sessionId].find(m => m.id === messageId), content, isEdited: true }
          commit('UPDATE_MESSAGE', { sessionId, message: editedMsg })

          // 如果是最后一条消息，更新会话列表
          const session = rootState.im?.session?.sessions?.find(s => s.id === sessionId)
          if (session && session.lastMessageId === messageId) {
            commit('im/session/UPDATE_SESSION', {
              id: sessionId,
              lastMessage: formatMessagePreview('TEXT', content)
            }, { root: true })
          }
        }
        return res
      }
      throw new Error('编辑消息失败')
    },

    // 转发消息
    async forwardMessage({ commit }, { messageId, targetConversationId }) {
      const res = await apiForwardMessage({ messageId, targetConversationId })
      if (res.code === 200 && res.data) {
        commit('im/session/UPDATE_SESSION', {
          id: targetConversationId,
          lastMessage: formatMessagePreviewFromObject(res.data),
          lastMessageTime: res.data.timestamp,
          lastMessageType: res.data.type
        }, { root: true })
        return res.data
      }
      throw new Error('转发消息失败')
    },

    // 标记消息为已读
    async markMessageAsRead({ commit, rootState }, { conversationId, messageId }) {
      await markAsRead({ conversationId, messageId })
      
      // 发送消息级别的已读确认
      if (messageId) {
        imWebSocket.sendAck(messageId, 'read', rootState.im?.deviceId)
      }

      // 关键：触发多端同步，通知其他在线设备也清除未读
      imWebSocket.send({
        type: 'READ_SYNC',
        conversationId,
        lastReadMessageId: messageId,
        deviceId: rootState.im?.deviceId
      })

      commit('im/session/UPDATE_SESSION', {
        id: conversationId,
        unreadCount: 0
      }, { root: true })
    },

    // 删除消息
    async deleteMessage({ commit, rootState }, messageId) {
      await deleteMessage(messageId)
      // 需要找到该消息所属的会话ID
      const sessionId = rootState.im?.session?.currentSession?.id
      if (sessionId) {
        commit('DELETE_MESSAGE', { sessionId, messageId })
      }
    },

    // 撤回消息
    async recallMessage({ commit, dispatch, state, rootState }, messageId) {
      await recallMessage(messageId)
      const sessionId = rootState.im?.session?.currentSession?.id
      if (sessionId && state.messages[sessionId]) {
        const msg = state.messages[sessionId].find(m => m.id === messageId)
        if (msg) {
          await dispatch('applyRecallUpdate', { sessionId, messageId })
        }
      }
    },

    applyRecallUpdate({ commit, state }, { sessionId, messageId }) {
      const sessionIds = sessionId ? [sessionId] : Object.keys(state.messages)
      for (const sid of sessionIds) {
        const messages = state.messages[sid] || []
        const index = messages.findIndex(m => m.id === messageId || m.clientMsgId === messageId)
        if (index !== -1) {
          const message = messages[index]
          const originalContent = message.originalContent || message.content || ''
          commit('UPDATE_MESSAGE', {
            sessionId: sid,
            message: {
              ...message,
              isRevoked: true,
              sendStatus: SEND_STATUS.RECALLED,
              status: 'recalled',
              content: '',
              type: 'RECALLED',
              originalContent
            }
          })
          break
        }
      }
    },

    // 接收消息（WebSocket 推送）
    receiveMessage({ commit, dispatch, rootState, state }, message) {
      const sessionId = message.conversationId || message.sessionId
      if (!sessionId) { return }

      const currentSessionId = rootState.im?.session?.currentSession?.id

      // 1. 消息去重
      const messageId = message.id || message.messageId || message.clientMsgId
      if (messageId && state.receivedMessageIds.has(messageId)) {
        return
      }

      // 2. 规范化处理
      if (message.type) { message.type = message.type.toUpperCase() }
      message.senderName = message.senderName || message.senderNickname || message.nickname || '未知用户'
      message.senderAvatar = message.senderAvatar || ''
      
      const currentUser = rootState.im?.currentUser
      if (message.isOwn === undefined) {
        message.isOwn = isOwnMessage(message, currentUser?.id)
      }

      // 3. 分发逻辑：
      // 如果消息属于当前正在查看的会话，则推入列表
      if (String(sessionId) === String(currentSessionId)) {
        commit('ADD_MESSAGE', { sessionId, message })
        // 自动发送接收 ACK
        if (message.id && !message.isOwn) {
          imWebSocket.sendAck(message.id, 'receive', rootState.im?.deviceId)
        }
      }

      // 4. 无论是否为当前会话，都更新会话列表（未读数、最后消息预览）
      const isCurrentSession = String(sessionId) === String(currentSessionId)
      const session = rootState.im?.session?.sessions?.find(s => String(s.id) === String(sessionId))
      const hasMention = message.atUserIds && currentUser?.id && message.atUserIds.includes(currentUser.id)

      commit('im/session/UPDATE_SESSION', {
        id: sessionId,
        lastMessage: formatMessagePreviewFromObject(message) || '[新消息]',
        lastMessageTime: message.timestamp || message.sendTime,
        lastMessageType: message.type,
        lastSenderNickname: message.senderName,
        unreadCount: isCurrentSession ? 0 : ((session?.unreadCount || 0) + 1),
        hasMention: hasMention || (isCurrentSession ? false : session?.hasMention)
      }, { root: true })

      if (messageId) { commit('ADD_RECEIVED_MESSAGE_ID', messageId) }
    },

    // 设置回复消息
    setReplyingMessage({ commit }, message) {
      commit('SET_REPLYING_MESSAGE', message)
    },

    // 清除回复消息
    clearReplyingMessage({ commit }) {
      commit('SET_REPLYING_MESSAGE', null)
    },

    // 处理表情回复更新（WebSocket 推送）
    handleReactionUpdate({ commit, state }, { messageId, emoji, userId, userName, userAvatar, isAdd }) {
      // 遍历所有会话查找该消息
      for (const sessionId in state.messages) {
        const messages = state.messages[sessionId]
        const index = messages.findIndex(m => m.id === messageId)

        if (index !== -1) {
          const message = messages[index]
          let reactions = message.reactions || []

          if (isAdd) {
            // 添加表情回复
            const existingIndex = reactions.findIndex(
              r => r.emoji === emoji && r.userId === userId
            )

            if (existingIndex === -1) {
              reactions.push({
                emoji,
                userId,
                userName,
                userAvatar
              })
            }
          } else {
            // 移除表情回复
            reactions = reactions.filter(
              r => !(r.emoji === emoji && r.userId === userId)
            )
          }

          commit('UPDATE_MESSAGE', {
            sessionId,
            message: { ...message, reactions }
          })
          break
        }
      }
    },

    // 处理消息 ACK 确认（WebSocket 推送）
    handleMessageAck({ commit, state }, { messageId, ackType }) {
      for (const sessionId in state.messages) {
        const messages = state.messages[sessionId]
        const index = messages.findIndex(m => m.id === messageId)

        if (index !== -1) {
          const message = messages[index]
          let newStatus = message.sendStatus

          if (ackType === 'read') {
            newStatus = SEND_STATUS.READ
          } else if (ackType === 'receive') {
            if (message.sendStatus === SEND_STATUS.SENDING || message.sendStatus === SEND_STATUS.PENDING) {
              newStatus = SEND_STATUS.DELIVERED
            }
          }

          if (newStatus !== message.sendStatus) {
            commit('UPDATE_MESSAGE', {
              sessionId,
              message: { ...message, sendStatus: newStatus }
            })
          }
          break
        }
      }
    },

    // ========== 发送中消息管理 Actions ==========

    /**
     * 添加消息到发送队列（乐观 UI）
     * @param {Object} context - Vuex context
     * @param {Object} payload - { clientMsgId, sessionId, message }
     */
    addSendingMessage({ commit }, { clientMsgId, sessionId, message }) {
      commit('ADD_SENDING_MESSAGE', { clientMsgId, sessionId, message })
    },

    /**
     * 处理消息送达事件（WebSocket 推送）
     * 实现幂等性处理：使用 clientMsgId 去重
     * @param {Object} context - Vuex context
     * @param {Object} payload - { clientMsgId, messageId, sessionId }
     */
    handleMessageDelivered({ commit, state }, { clientMsgId, messageId, sessionId }) {
      // 幂等性处理：检查消息是否在发送队列中
      if (!state.sendingMessages.has(clientMsgId)) {
        return
      }

      // 移除发送队列记录
      commit('REMOVE_SENDING_MESSAGE', clientMsgId)

      // 更新消息状态为已送达
      commit('UPDATE_MESSAGE_STATUS', {
        sessionId,
        messageId,
        sendStatus: SEND_STATUS.DELIVERED
      })
    },

    /**
     * 处理消息失败事件（WebSocket 推送）
     * @param {Object} context - Vuex context
     * @param {Object} payload - { clientMsgId, sessionId, errorCode }
     */
    handleMessageFailed({ commit, state }, { clientMsgId, sessionId, errorCode }) {
      // 幂等性处理：检查消息是否在发送队列中
      if (!state.sendingMessages.has(clientMsgId)) {
        return
      }

      // 移除发送队列记录
      commit('REMOVE_SENDING_MESSAGE', clientMsgId)

      // 查找消息的 ID
      const message = state.messages[sessionId]?.find(m => m.clientMsgId === clientMsgId)
      if (message) {
        // 更新消息状态为失败
        commit('UPDATE_MESSAGE', {
          sessionId,
          message: {
            ...message,
            sendStatus: SEND_STATUS.FAILED,
            sendErrorCode: errorCode || 'UNKNOWN_ERROR'
          }
        })
      }
    },

    /**
     * 清理过期的发送中消息
     * 每 5 分钟清理一次超过 10 分钟的记录
     * @param {Object} context - Vuex context
     */
    cleanupExpiredMessages({ commit }) {
      commit('CLEANUP_EXPIRED_MESSAGES')
    },

    // ========== 离线消息和失败重试 Actions ==========

    /**
     * 设置离线状态
     * @param {Object} context - Vuex context
     * @param {boolean} isOffline - 是否离线
     */
    setOfflineStatus({ commit }, isOffline) {
      commit('SET_OFFLINE_STATUS', isOffline)
    },

    /**
     * 处理离线消息队列
     * 当连接恢复时，批量发送离线期间的消息
     * @param {Object} context - Vuex context
     */
    async processOfflineQueue({ commit, state, dispatch }) {
      if (state.offlineQueue.length === 0) {
        return
      }

      info('MessageStore', `[离线队列] 开始处理 ${state.offlineQueue.length} 条离线消息`)

      // 获取队列中的所有消息
      const messagesToProcess = [...state.offlineQueue]

      // 清空离线队列
      commit('CLEAR_OFFLINE_QUEUE')

      // 批量发送消息
      for (const item of messagesToProcess) {
        try {
          await dispatch('sendMessage', {
            sessionId: item.sessionId,
            type: item.message.type || 'TEXT',
            content: item.message.content,
            replyToMessageId: item.message.replyToMessageId
          })
        } catch (error) {
          error('MessageStore', '[离线队列] 发送消息失败:', error)
          // 如果发送失败，重新加入队列
          commit('ADD_TO_OFFLINE_QUEUE', {
            messageId: item.messageId,
            sessionId: item.sessionId,
            message: item.message
          })
        }
      }
    },

    /**
     * 重试失败的消息
     * @param {Object} context - Vuex context
     */
    async retryFailedMessages({ commit, state, dispatch }) {
      if (state.failedRetryQueue.length === 0) {
        return
      }

      info('MessageStore', `[失败重试] 开始重试 ${state.failedRetryQueue.length} 条失败消息`)

      // 过滤出需要重试的消息
      const messagesToRetry = state.failedRetryQueue.filter(
        item => item.retryCount < OFFLINE_QUEUE_CONFIG.MAX_RETRY_TIMES
      )

      for (const item of messagesToRetry) {
        try {
          commit('INCREMENT_RETRY_COUNT', item.clientMsgId)

          await dispatch('sendMessage', {
            sessionId: item.sessionId,
            type: item.message.type || 'TEXT',
            content: item.message.content,
            replyToMessageId: item.message.replyToMessageId
          })

          // 重试成功，从队列中移除
          commit('REMOVE_FROM_FAILED_RETRY_QUEUE', item.clientMsgId)
        } catch (error) {
          error('MessageStore', `[失败重试] 重试消息 ${item.clientMsgId} 失败:`, error)

          // 如果达到最大重试次数，从队列中移除
          if (item.retryCount >= OFFLINE_QUEUE_CONFIG.MAX_RETRY_TIMES) {
            commit('REMOVE_FROM_FAILED_RETRY_QUEUE', item.clientMsgId)
            warn('MessageStore', `[失败重试] 消息 ${item.clientMsgId} 达到最大重试次数，放弃重试`)
          }
        }
      }
    },

    /**
     * 清理过期的去重 ID
     * @param {Object} context - Vuex context
     */
    cleanupReceivedIds({ commit }) {
      commit('CLEANUP_RECEIVED_IDS')
    }
  }
}

// ========== 定期清理机制 ==========
let cleanupTimer = null

/**
 * 启动定期清理任务
 * 每隔配置时间清理一次过期的发送中消息和去重 ID
 */
export function startCleanupTimer(store) {
  // 清除旧的定时器（如果存在）
  if (cleanupTimer) {
    clearInterval(cleanupTimer)
  }

  // 按配置的间隔时间清理
  cleanupTimer = setInterval(() => {
    store.dispatch('im/message/cleanupExpiredMessages')
    store.dispatch('im/message/cleanupReceivedIds')
    info('MessageStore', `定期清理完成（发送队列: ${store.getters['im/message/sendingQueueSize']}）`)
  }, SENDING_QUEUE_CONFIG.CLEANUP_INTERVAL)

  info('MessageStore', `启动定期清理任务（间隔: ${SENDING_QUEUE_CONFIG.CLEANUP_INTERVAL / 1000}秒）`)
}

/**
 * 停止定期清理任务
 */
export function stopCleanupTimer() {
  if (cleanupTimer) {
    clearInterval(cleanupTimer)
    cleanupTimer = null
    info('MessageStore', '停止定期清理任务')
  }
}

/**
 * 初始化消息模块
 * 在应用启动时调用，初始化各种清理任务和监听器
 * @param {Object} store - Vuex store 实例
 */
export function initMessageModule(store) {
  // 启动定期清理任务
  startCleanupTimer(store)

  // 监听网络状态变化
  if (typeof window !== 'undefined') {
    const handleOnline = () => {
      info('MessageStore', '网络已恢复')
      store.dispatch('im/message/setOfflineStatus', false)

      // 处理离线队列中的消息
      store.dispatch('im/message/processOfflineQueue')

      // 重试失败的消息
      store.dispatch('im/message/retryFailedMessages')
    }

    const handleOffline = () => {
      info('MessageStore', '网络已断开')
      store.dispatch('im/message/setOfflineStatus', true)
    }

    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)

    // 初始化时检查网络状态
    if (!navigator.onLine) {
      handleOffline()
    }
  }

  info('MessageStore', '消息模块初始化完成')
}
