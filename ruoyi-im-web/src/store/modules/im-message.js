/**
 * 消息管理模块
 * 管理消息列表、消息操作（发送、编辑、撤回等）
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
  FAILED: 4
}

/**
 * 发送队列配置
 */
export const SENDING_QUEUE_CONFIG = {
  MAX_QUEUE_SIZE: 100, // 最大队列长度（防止内存溢出和滥用）
  CLEANUP_INTERVAL: 60 * 1000, // 清理间隔：1分钟
  EXPIRED_TIME: 10 * 60 * 1000 // 过期时间：10分钟
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
    [SEND_STATUS.FAILED]: 'failed'
  }
  return statusMap[sendStatus] || 'pending'
}

// 简单UUID生成
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8)
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
    lastClickedMessageId: null
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
      if (!state.messages[sessionId]) {
        state.messages[sessionId] = []
      }
      // 避免重复添加 (通过消息ID判断)
      const index = state.messages[sessionId].findIndex(m => m.id === message.id)
      if (index === -1) {
        // 创建新数组触发响应式更新
        state.messages[sessionId] = [...state.messages[sessionId], message]
      } else {
        // 如果已存在，则更新 (比如编辑后) - 创建新数组触发响应式更新
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
      const index = state.messages[sessionId].findIndex(m => m.id === message.id)
      if (index !== -1) {
        state.messages[sessionId][index] = { ...state.messages[sessionId][index], ...message }
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
        console.log(`[发送队列清理] 清理了 ${cleanedCount} 条过期消息`)
      }
    }
  },

  actions: {
    // 加载消息列表
    async loadMessages({ commit }, { sessionId, lastMessageId = null, pageSize = 20, isLoadMore = false }) {
      commit('SET_LOADING', true)
      try {
        const res = await getMessages(sessionId, { lastId: lastMessageId, pageSize })
        if (res.code === 200 && res.data) {
          // 规范化消息字段
          const normalized = res.data.map(msg => ({
            ...msg,
            type: (msg.type || '').toUpperCase(),
            senderName: msg.senderName || msg.senderNickname || msg.nickname || msg.userName || '未知用户',
            senderAvatar: msg.senderAvatar || msg.avatar || ''
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
        timestamp: Date.now()
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
        throw error
      }
    },

    // 编辑消息
    async editMessage({ commit, rootState }, { messageId, content }) {
      const res = await apiEditMessage(messageId, { content })
      if (res.code === 200) {
        // 查找该消息在哪个会话中 (通常是当前会话)
        const sessionId = rootState.im?.session?.currentSession?.id
        if (sessionId && rootState.message.messages[sessionId]) {
          const editedMsg = { ...rootState.message.messages[sessionId].find(m => m.id === messageId), content, isEdited: true }
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
    async markMessageAsRead({ commit }, { conversationId, messageId }) {
      await markAsRead({ conversationId, messageId })
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
    async recallMessage({ commit, rootState }, messageId) {
      await recallMessage(messageId)
      // 需要找到该消息所属的会话ID并更新为撤回状态
      const sessionId = rootState.im?.session?.currentSession?.id
      if (sessionId && rootState.message.messages[sessionId]) {
        const msg = rootState.message.messages[sessionId].find(m => m.id === messageId)
        if (msg) {
          commit('UPDATE_MESSAGE', {
            sessionId,
            message: { ...msg, isRevoked: true, content: '' }
          })
        }
      }
    },

    // 接收消息（WebSocket 推送）
    receiveMessage({ commit, rootState }, message) {
      const sessionId = message.conversationId || message.sessionId
      if (!sessionId) {return}

      // 规范化消息类型为大写（兼容后端可能返回的小写类型）
      if (message.type) {
        message.type = message.type.toUpperCase()
      }

      // 规范化发送者信息
      message.senderName = message.senderName || message.senderNickname || message.nickname || message.userName || '未知用户'
      message.senderAvatar = message.senderAvatar || message.avatar || ''

      // 添加消息到列表
      commit('ADD_MESSAGE', { sessionId, message })

      // 如果不是当前正在查看的会话，则增加未读数
      const isCurrentSession = rootState.im?.session?.currentSession?.id === sessionId
      const session = rootState.im?.session?.sessions?.find(s => s.id === sessionId)

      // 检查是否有 @ 我
      const currentUser = rootState.im?.currentUser
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
    handleReactionUpdate({ commit, rootState }, { messageId, emoji, userId, userName, userAvatar, isAdd }) {
      // 遍历所有会话查找该消息
      for (const sessionId in rootState.message.messages) {
        const messages = rootState.message.messages[sessionId]
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
    }
  }
}

// ========== 定期清理机制 ==========
let cleanupTimer = null

/**
 * 启动定期清理任务
 * 每隔配置时间清理一次过期的发送中消息
 */
export function startCleanupTimer(store) {
  // 清除旧的定时器（如果存在）
  if (cleanupTimer) {
    clearInterval(cleanupTimer)
  }

  // 按配置的间隔时间清理
  cleanupTimer = setInterval(() => {
    store.dispatch('im/message/cleanupExpiredMessages')
    console.log(`[im-message] 清理过期的发送中消息（队列大小: ${store.getters['im/message/sendingQueueSize']}）`)
  }, SENDING_QUEUE_CONFIG.CLEANUP_INTERVAL)

  console.log(`[im-message] 启动定期清理任务（间隔: ${SENDING_QUEUE_CONFIG.CLEANUP_INTERVAL / 1000}秒）`)
}

/**
 * 停止定期清理任务
 */
export function stopCleanupTimer() {
  if (cleanupTimer) {
    clearInterval(cleanupTimer)
    cleanupTimer = null
    console.log('[im-message] 停止定期清理任务')
  }
}
