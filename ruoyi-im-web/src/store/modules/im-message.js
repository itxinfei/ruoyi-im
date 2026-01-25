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

// 简单UUID生成
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8)
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
        state.messages[sessionId].push(message)
      } else {
        // 如果已存在，则更新 (比如编辑后)
        state.messages[sessionId][index] = { ...state.messages[sessionId][index], ...message }
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
        state.messages[sessionId].splice(index, 1)
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
    }
  },

  actions: {
    // 加载消息列表
    async loadMessages({ commit }, { sessionId, lastMessageId = null, pageSize = 20, isLoadMore = false }) {
      commit('SET_LOADING', true)
      try {
        const res = await getMessages(sessionId, { lastId: lastMessageId, pageSize })
        if (res.code === 200 && res.data) {
          // 后端返回是按时间倒序的 (newest first)，前端需要 newest at bottom
          const transformed = res.data.reverse()
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
      const res = await apiSendMessage({
        conversationId: sessionId,
        type,
        content,
        replyToMessageId,
        clientMsgId
      })

      if (res.code === 200 && res.data) {
        commit('ADD_MESSAGE', { sessionId, message: res.data })
        commit('session/UPDATE_SESSION', {
          id: sessionId,
          lastMessage: formatMessagePreviewFromObject(res.data),
          lastMessageTime: res.data.timestamp,
          lastMessageType: type
        }, { root: true })
        return res.data
      }
      throw new Error('发送消息失败')
    },

    // 编辑消息
    async editMessage({ commit, rootState }, { messageId, content }) {
      const res = await apiEditMessage(messageId, { content })
      if (res.code === 200) {
        // 查找该消息在哪个会话中 (通常是当前会话)
        const sessionId = rootState.session.currentSession?.id
        if (sessionId && rootState.message.messages[sessionId]) {
          const editedMsg = { ...rootState.message.messages[sessionId].find(m => m.id === messageId), content, isEdited: true }
          commit('UPDATE_MESSAGE', { sessionId, message: editedMsg })

          // 如果是最后一条消息，更新会话列表
          const session = rootState.session.sessions.find(s => s.id === sessionId)
          if (session && session.lastMessageId === messageId) {
            commit('session/UPDATE_SESSION', {
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
        commit('session/UPDATE_SESSION', {
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
      commit('session/UPDATE_SESSION', {
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

    // 撤回消息
    async recallMessage({ commit, rootState }, messageId) {
      await recallMessage(messageId)
      // 需要找到该消息所属的会话ID并更新为撤回状态
      const sessionId = rootState.session.currentSession?.id
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
      if (!sessionId) return

      // 添加消息到列表
      commit('ADD_MESSAGE', { sessionId, message })

      // 如果不是当前正在查看的会话，则增加未读数
      const isCurrentSession = rootState.session.currentSession && rootState.session.currentSession.id === sessionId
      const session = rootState.session.sessions.find(s => s.id === sessionId)

      // 检查是否有 @ 我
      const currentUser = rootState.im?.currentUser
      const hasMention = message.atUserIds && currentUser?.id && message.atUserIds.includes(currentUser.id)

      commit('session/UPDATE_SESSION', {
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
    }
  }
}
