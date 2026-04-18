/**
 * 消息管理模块
 * 管理消息列表、消息操作（发送、编辑、撤回等）
 */

import {
  getMessages,
  sendMessage as apiSendMessage,
  markAsRead,
  deleteMessage,
  batchDeleteMessages,
  recallMessage,
  editMessage as apiEditMessage,
  forwardMessage as apiForwardMessage,
  toggleMessageReaction,
  getMessageReactions,
  addMessageFavorite,
  removeMessageFavorite
} from '@/api/im/message'
import { formatMessagePreview, formatMessagePreviewFromObject } from '@/utils/message'

// ==================== 消息状态定义 ====================
/**
 * 消息发送状态枚举
 * @readonly
 * @enum {string}
 */
export const MESSAGE_SEND_STATUS = {
  PENDING: 'pending',   // 待发送 - 消息已创建，等待发送
  SENDING: 'sending',   // 发送中 - 消息正在发送到服务器
  SENT: 'sent',         // 已送达 - 消息已成功发送到服务器并推送到对方设备
  DELIVERED: 'delivered', // 已接收 - 消息已推送到对方设备（仅用于确认）
  FAILED: 'failed'      // 发送失败 - 消息发送失败，可能需要重试
}

/**
 * 消息阅读状态枚举
 * @readonly
 * @enum {string}
 */
export const MESSAGE_READ_STATUS = {
  UNREAD: 'unread',     // 未读 - 对方未查看
  READ: 'read'          // 已读 - 对方已查看
}

/**
 * 消息特殊状态枚举
 * @readonly
 * @enum {string}
 */
export const MESSAGE_SPECIAL_STATUS = {
  RECALLED: 'recalled'  // 已撤回 - 消息已被发送者撤回
}

// ==================== 状态转换规则 ====================
/**
 * 有效的状态转换映射
 * @type {Object.<string, string[]>}
 */
const VALID_STATUS_TRANSITIONS = {
  [MESSAGE_SEND_STATUS.PENDING]: [MESSAGE_SEND_STATUS.SENDING, MESSAGE_SEND_STATUS.FAILED],
  [MESSAGE_SEND_STATUS.SENDING]: [MESSAGE_SEND_STATUS.SENT, MESSAGE_SEND_STATUS.FAILED],
  [MESSAGE_SEND_STATUS.SENT]: [MESSAGE_SEND_STATUS.DELIVERED, MESSAGE_READ_STATUS.READ, MESSAGE_SPECIAL_STATUS.RECALLED],
  [MESSAGE_SEND_STATUS.DELIVERED]: [MESSAGE_READ_STATUS.READ, MESSAGE_SPECIAL_STATUS.RECALLED],
  [MESSAGE_READ_STATUS.UNREAD]: [MESSAGE_READ_STATUS.READ],
  [MESSAGE_READ_STATUS.READ]: [MESSAGE_SPECIAL_STATUS.RECALLED]
}

/**
 * 验证状态转换是否有效
 * @param {string} currentStatus - 当前状态
 * @param {string} newStatus - 新状态
 * @returns {boolean} 状态转换是否有效
 */
function isValidStatusTransition(currentStatus, newStatus) {
  if (!currentStatus || !newStatus) return true // 允许初始化
  const allowedTransitions = VALID_STATUS_TRANSITIONS[currentStatus]
  return allowedTransitions ? allowedTransitions.includes(newStatus) : false
}

/**
 * 消息 ID 字段名常量（统一使用 messageId）
 * @readonly
 * @enum {string}
 */
export const MESSAGE_ID_FIELD = 'messageId'

// 消息 ID 标准化：如果消息只有 id 而无 messageId，则复制为 messageId
function normalizeMessage(msg) {
  if (!msg) return msg
  const normalized = { ...msg }
  if (normalized.id !== undefined && normalized.messageId === undefined) {
    normalized.messageId = normalized.id
  }
  return normalized
}

// 批量标准化消息列表
function normalizeMessages(messages) {
  return messages.map(normalizeMessage)
}

// 简单UUID生成
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

export default {
  namespaced: true,

  state: () => ({
    // 消息列表（按 sessionId 分组）
    messages: {},

    // 当前正在回复的消息
    replyingMessage: null,

    // 加载状态
    loading: false
  }),

  getters: {
    // 获取当前会话的消息列表
    currentMessages: (state, getters, rootState) => {
      if (!rootState.session.currentSession) return []
      return state.messages[rootState.session.currentSession.id] || []
    },

    // 获取指定会话的消息列表
    messagesBySessionId: (state) => (sessionId) => {
      return state.messages[sessionId] || []
    },

    // 根据消息ID获取消息
    getMessageById: (state) => (sessionId, messageId) => {
      const messages = state.messages[sessionId] || []
      return messages.find(m => m.messageId === messageId)
    }
  },

  mutations: {
    // 设置消息列表
    SET_MESSAGES(state, { sessionId, messages }) {
      state.messages[sessionId] = normalizeMessages(messages)
    },

    // 预加消息（历史消息加载更多）
    PREPEND_MESSAGES(state, { sessionId, messages }) {
      if (!state.messages[sessionId]) {
        state.messages[sessionId] = []
      }
      state.messages[sessionId] = [...normalizeMessages(messages), ...state.messages[sessionId]]
    },

    // 添加单条消息
    ADD_MESSAGE(state, { sessionId, message }) {
      if (!state.messages[sessionId]) {
        state.messages[sessionId] = []
      }
      const normalized = normalizeMessage(message)
      const msgId = normalized.messageId
      const index = state.messages[sessionId].findIndex(m => m.messageId === msgId)
      if (index === -1) {
        state.messages[sessionId].push(normalized)
      } else {
        // 如果已存在，则更新 (比如编辑后)
        state.messages[sessionId][index] = { ...state.messages[sessionId][index], ...normalized }
      }
    },

    // 更新消息
    UPDATE_MESSAGE(state, { sessionId, message }) {
      if (!state.messages[sessionId]) {
        return
      }
      const msgId = message.messageId
      const index = state.messages[sessionId].findIndex(m => m.messageId === msgId)
      if (index !== -1) {
        state.messages[sessionId][index] = { ...state.messages[sessionId][index], ...normalizeMessage(message) }
      }
    },

    // 删除消息
    DELETE_MESSAGE(state, { sessionId, messageId }) {
      if (!state.messages[sessionId]) {
        return
      }
      const index = state.messages[sessionId].findIndex(m => m.messageId === messageId)
      if (index !== -1) {
        state.messages[sessionId].splice(index, 1)
      }
    },

    // 更新消息状态
    UPDATE_MESSAGE_STATUS(state, { sessionId, messageId, status }) {
      if (!state.messages[sessionId]) {
        return
      }
      const index = state.messages[sessionId].findIndex(m => m.messageId === messageId)
      if (index !== -1) {
        const currentStatus = state.messages[sessionId][index].status
        // 验证状态转换
        if (isValidStatusTransition(currentStatus, status)) {
          state.messages[sessionId][index] = {
            ...state.messages[sessionId][index],
            status,
            // 兼容旧字段 isRevoked
            isRevoked: status === MESSAGE_SPECIAL_STATUS.RECALLED ? 1 : state.messages[sessionId][index].isRevoked
          }
        } else {
          console.warn(`Invalid status transition: ${currentStatus} -> ${status}`)
        }
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

    // 清空指定会话的消息
    CLEAR_SESSION_MESSAGES(state, sessionId) {
      if (sessionId && state.messages[sessionId]) {
        state.messages[sessionId] = []
      }
    },

    // 清空消息状态
    CLEAR_STATE(state) {
      state.messages = {}
      state.replyingMessage = null
    },

    // 更新消息表情反应
    UPDATE_MESSAGE_REACTIONS(state, { sessionId, messageId, reactions }) {
      if (!state.messages[sessionId]) return
      const index = state.messages[sessionId].findIndex(m => m.messageId === messageId)
      if (index !== -1) {
        state.messages[sessionId][index] = {
          ...state.messages[sessionId][index],
          reactions: reactions || []
        }
      }
    },

    // 更新消息收藏状态
    UPDATE_MESSAGE_FAVORITE(state, { sessionId, messageId, isFavorited, favoriteId }) {
      if (!state.messages[sessionId]) return
      const index = state.messages[sessionId].findIndex(m => m.messageId === messageId)
      if (index !== -1) {
        state.messages[sessionId][index] = {
          ...state.messages[sessionId][index],
          isFavorited,
          favoriteId
        }
      }
    }
  },

  actions: {
    // 加载消息列表
    async loadMessages({ commit }, { sessionId, lastMessageId = null, pageSize = 20, isLoadMore = false }) {
      commit('SET_LOADING', true)
      try {
        const res = await getMessages(sessionId, { lastId: lastMessageId, limit: pageSize })
        if (res.code === 200 && res.data) {
          const transformed = res.data
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

    // 发送消息
    async sendMessage({ commit }, { sessionId, type = 'TEXT', content, replyToMessageId = null }) {
      const clientMsgId = generateUUID()
      // 创建临时消息，状态为 sending
      const tempMessage = {
        clientMsgId,
        conversationId: sessionId,
        type,
        content,
        replyToMessageId,
        status: MESSAGE_SEND_STATUS.SENDING,
        timestamp: Date.now(),
        senderId: null, // 将在服务器响应后填充
        senderName: null,
        messageId: clientMsgId // 使用 clientMsgId 作为临时 messageId
      }

      // 先添加到列表（发送中状态）
      commit('ADD_MESSAGE', { sessionId, message: tempMessage })

      try {
        const res = await apiSendMessage({
          conversationId: sessionId,
          type,
          content,
          replyToMessageId,
          clientMsgId
        })

        if (res.code === 200 && res.data) {
          // 更新为发送成功状态
          const finalMessage = {
            ...res.data,
            status: MESSAGE_SEND_STATUS.SENT
          }
          commit('UPDATE_MESSAGE', { sessionId, message: finalMessage })
          commit('im/session/UPDATE_SESSION', {
            id: sessionId,
            lastMessage: formatMessagePreviewFromObject(res.data),
            lastMessageTime: res.data.timestamp,
            lastMessageType: type
          }, { root: true })
          return res.data
        } else {
          // 发送失败，更新状态
          commit('UPDATE_MESSAGE_STATUS', {
            sessionId,
            messageId: clientMsgId,
            status: MESSAGE_SEND_STATUS.FAILED
          })
          throw new Error('发送消息失败')
        }
      } catch (error) {
        // 发送异常，更新状态
        commit('UPDATE_MESSAGE_STATUS', {
          sessionId,
          messageId: clientMsgId,
          status: MESSAGE_SEND_STATUS.FAILED
        })
        throw error
      }
    },

    // 编辑消息
    async editMessage({ commit, rootState }, { messageId, content }) {
      const res = await apiEditMessage(messageId, { content })
      if (res.code === 200) {
        // 查找该消息在哪个会话中 (通常是当前会话)
        const sessionId = rootState.session.currentSession?.id
        if (sessionId && rootState.message.messages[sessionId]) {
          const editedMsg = { ...rootState.message.messages[sessionId].find(m => m.messageId === messageId), content, isEdited: true }
          commit('UPDATE_MESSAGE', { sessionId, message: editedMsg })

          // 如果是最后一条消息，更新会话列表
          const session = rootState.session.sessions.find(s => s.id === sessionId)
          if (session && (session.lastMessageId === messageId || session.lastMessageId === editedMsg.messageId)) {
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
      const sessionId = rootState.session.currentSession?.id
      if (sessionId) {
        commit('DELETE_MESSAGE', { sessionId, messageId })
      }
    },

    // 批量删除消息
    async batchDeleteMessagesAction({ commit, rootState }, messageIds) {
      await batchDeleteMessages(messageIds)
      const sessionId = rootState.session.currentSession?.id
      if (sessionId) {
        messageIds.forEach(messageId => {
          commit('DELETE_MESSAGE', { sessionId, messageId })
        })
      }
    },

    // 清空指定会话的所有消息
    async clearMessages({ commit }, sessionId) {
      if (!sessionId) return
      commit('CLEAR_SESSION_MESSAGES', sessionId)
    },

    // 撤回消息
    async recallMessage({ commit, rootState }, messageId) {
      await recallMessage(messageId)
      // 需要找到该消息所属的会话ID并更新为撤回状态
      const sessionId = rootState.session.currentSession?.id
      if (sessionId && rootState.message.messages[sessionId]) {
        const msg = rootState.message.messages[sessionId].find(m => m.messageId === messageId)
        if (msg) {
          commit('UPDATE_MESSAGE_STATUS', {
            sessionId,
            messageId,
            status: MESSAGE_SPECIAL_STATUS.RECALLED
          })
        }
      }
    },

    // 接收消息（WebSocket 推送）
    receiveMessage({ commit, rootState }, message) {
      const sessionId = message.conversationId || message.sessionId
      if (!sessionId) return

      // 添加消息到列表
      commit('ADD_MESSAGE', { sessionId, message })

      // 如果不是当前正在查看的会话，则增加未读数
      const isCurrentSession = rootState.session.currentSession && rootState.session.currentSession.id === sessionId
      const session = rootState.session.sessions.find(s => s.id === sessionId)

      // 检查是否有 @ 我
      const currentUser = rootState.im?.currentUser
      const hasMention = message.atUserIds && currentUser?.id && message.atUserIds.includes(currentUser.id)

      commit('im/session/UPDATE_SESSION', {
        id: sessionId,
        lastMessage: formatMessagePreview(message.type, message.content),
        lastMessageTime: message.timestamp,
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

    // 切换消息表情反应
    async toggleReaction({ commit, rootState }, { messageId, emoji }) {
      try {
        const res = await toggleMessageReaction(messageId, emoji)
        if (res.code === 200) {
          // 重新获取最新的 reactions
          const reactionsRes = await getMessageReactions(messageId)
          const sessionId = rootState.session.currentSession?.id
          if (sessionId && reactionsRes.code === 200) {
            commit('UPDATE_MESSAGE_REACTIONS', {
              sessionId,
              messageId,
              reactions: reactionsRes.data || []
            })
          }
          return res.data
        }
        throw new Error('操作失败')
      } catch (error) {
        console.error('切换表情失败:', error)
        throw error
      }
    },

    // 添加收藏
    async addFavorite({ commit, rootState }, { messageId, conversationId }) {
      const res = await addMessageFavorite({ messageId, conversationId })
      if (res.code === 200) {
        const sessionId = rootState.session.currentSession?.id
        if (sessionId) {
          commit('UPDATE_MESSAGE_FAVORITE', {
            sessionId,
            messageId,
            isFavorited: true,
            favoriteId: res.data?.id
          })
        }
        return res.data
      }
      throw new Error('添加收藏失败')
    },

    // 移除收藏
    async removeFavorite({ commit, rootState }, { messageId, conversationId: _conversationId }) {
      const res = await removeMessageFavorite(messageId)
      if (res.code === 200) {
        const sessionId = rootState.session.currentSession?.id
        if (sessionId) {
          commit('UPDATE_MESSAGE_FAVORITE', {
            sessionId,
            messageId,
            isFavorited: false,
            favoriteId: null
          })
        }
        return res.data
      }
      throw new Error('移除收藏失败')
    }
  }
}
