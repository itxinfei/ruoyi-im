import { sendMessage as apiSendMessage, listMessage } from '@/api/im/message'
import { listSession, updateSession, deleteSession as apiDeleteSession } from '@/api/im/session'
import { ElMessage } from 'element-plus'

const state = {
  currentSession: null,
  sessions: [],
  messageList: {},
  unreadCount: 0,
  onlineStatus: {},
  // 发送中的消息
  pendingMessages: {},
  // WebSocket连接状态
  wsConnected: false,
}

const getters = {
  // 根据会话ID获取消息列表
  messagesBySession: (state) => (sessionId) => {
    return state.messageList[sessionId] || []
  },
  // 当前用户ID
  currentUserId: () => {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.userId || null
  },
  // 获取会话列表
  sessionList: (state) => state.sessions,
  // 当前会话
  currentSession: (state) => state.currentSession,
  // 未读消息总数
  totalUnreadCount: (state) => {
    return state.sessions.reduce((total, session) => total + (session.unreadCount || 0), 0)
  },
}

const mutations = {
  SET_CURRENT_SESSION: (state, session) => {
    state.currentSession = session
  },
  SET_SESSIONS: (state, list) => {
    state.sessions = list
  },
  ADD_SESSION: (state, session) => {
    const exists = state.sessions.find(s => s.id === session.id)
    if (!exists) {
      state.sessions.unshift(session)
    }
  },
  UPDATE_SESSION: (state, { sessionId, updates }) => {
    const index = state.sessions.findIndex(s => s.id === sessionId)
    if (index !== -1) {
      state.sessions[index] = { ...state.sessions[index], ...updates }
    }
  },
  REMOVE_SESSION: (state, sessionId) => {
    state.sessions = state.sessions.filter(s => s.id !== sessionId)
  },
  SET_MESSAGE_LIST: (state, { sessionId, messages }) => {
    state.messageList[sessionId] = messages
  },
  ADD_MESSAGE: (state, { sessionId, message }) => {
    if (!state.messageList[sessionId]) {
      state.messageList[sessionId] = []
    }
    const exists = state.messageList[sessionId].find(m => m.id === message.id)
    if (!exists) {
      state.messageList[sessionId].push(message)
    }
  },
  UPDATE_MESSAGE: (state, { sessionId, messageId, updates }) => {
    if (state.messageList[sessionId]) {
      const index = state.messageList[sessionId].findIndex(m => m.id === messageId)
      if (index !== -1) {
        state.messageList[sessionId][index] = {
          ...state.messageList[sessionId][index],
          ...updates
        }
      }
    }
  },
  PREPEND_MESSAGES: (state, { sessionId, messages }) => {
    if (!state.messageList[sessionId]) {
      state.messageList[sessionId] = []
    }
    state.messageList[sessionId] = [...messages, ...state.messageList[sessionId]]
  },
  SET_UNREAD_COUNT: (state, count) => {
    state.unreadCount = count
  },
  SET_ONLINE_STATUS: (state, { userId, status }) => {
    state.onlineStatus[userId] = status
  },
  SET_WS_CONNECTED: (state, status) => {
    state.wsConnected = status
  },
  ADD_PENDING_MESSAGE: (state, { sessionId, tempId, message }) => {
    if (!state.pendingMessages[sessionId]) {
      state.pendingMessages[sessionId] = {}
    }
    state.pendingMessages[sessionId][tempId] = message
  },
  REMOVE_PENDING_MESSAGE: (state, { sessionId, tempId }) => {
    if (state.pendingMessages[sessionId]) {
      delete state.pendingMessages[sessionId][tempId]
    }
  },
  CLEAR_SESSION_UNREAD: (state, sessionId) => {
    const session = state.sessions.find(s => s.id === sessionId)
    if (session) {
      session.unreadCount = 0
    }
  },
}

const actions = {
  // 加载会话列表
  async loadSessions({ commit }) {
    try {
      const response = await listSession()
      const sessions = response.rows || response.data || []
      commit('SET_SESSIONS', sessions)
      return sessions
    } catch (error) {
      console.error('加载会话列表失败:', error)
      throw error
    }
  },

  // 切换会话
  async switchSession({ commit, dispatch }, session) {
    commit('SET_CURRENT_SESSION', session)
    commit('CLEAR_SESSION_UNREAD', session.id)
    // 加载该会话的消息
    await dispatch('loadMessages', { sessionId: session.id, page: 1, pageSize: 20 })
  },

  // 加载消息
  async loadMessages({ commit, state }, { sessionId, page = 1, pageSize = 20 }) {
    try {
      const response = await listMessage({ sessionId, page, pageSize })
      const messages = response.rows || response.data || []

      if (page === 1) {
        commit('SET_MESSAGE_LIST', { sessionId, messages })
      } else {
        commit('PREPEND_MESSAGES', { sessionId, messages })
      }

      return {
        messages,
        hasMore: messages.length === pageSize
      }
    } catch (error) {
      console.error('加载消息失败:', error)
      throw error
    }
  },

  // 发送消息
  async sendMessage({ commit, state }, { sessionId, type, content, replyTo }) {
    const tempId = `temp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

    // 创建临时消息
    const tempMessage = {
      id: tempId,
      sessionId,
      type,
      content,
      replyTo,
      senderId: userInfo.userId,
      senderName: userInfo.nickName || userInfo.userName,
      senderAvatar: userInfo.avatar,
      timestamp: Date.now(),
      time: new Date().toISOString(),
      status: 'sending',
    }

    // 添加到消息列表
    commit('ADD_MESSAGE', { sessionId, message: tempMessage })
    commit('ADD_PENDING_MESSAGE', { sessionId, tempId, message: tempMessage })

    try {
      // 调用发送消息API
      const response = await apiSendMessage({
        sessionId,
        type,
        content: typeof content === 'object' ? JSON.stringify(content) : content,
        replyTo,
      })

      // 更新消息状态
      commit('UPDATE_MESSAGE', {
        sessionId,
        messageId: tempId,
        updates: {
          id: response.data?.id || tempId,
          status: 'sent',
        }
      })

      // 更新会话的最后消息
      commit('UPDATE_SESSION', {
        sessionId,
        updates: {
          lastMessage: {
            content: typeof content === 'string' ? content : '[消息]',
            timestamp: Date.now(),
          }
        }
      })

      commit('REMOVE_PENDING_MESSAGE', { sessionId, tempId })
      return response.data
    } catch (error) {
      // 更新消息状态为失败
      commit('UPDATE_MESSAGE', {
        sessionId,
        messageId: tempId,
        updates: { status: 'failed' }
      })
      commit('REMOVE_PENDING_MESSAGE', { sessionId, tempId })
      throw error
    }
  },

  // 重发消息
  async resendMessage({ commit, dispatch, state }, message) {
    // 更新消息状态为发送中
    commit('UPDATE_MESSAGE', {
      sessionId: message.sessionId,
      messageId: message.id,
      updates: { status: 'sending' }
    })

    try {
      await dispatch('sendMessage', {
        sessionId: message.sessionId,
        type: message.type,
        content: message.content,
        replyTo: message.replyTo,
      })
    } catch (error) {
      ElMessage.error('重发失败，请重试')
    }
  },

  // 接收新消息
  receiveMessage({ commit, state }, message) {
    commit('ADD_MESSAGE', { sessionId: message.sessionId, message })

    // 如果不是当前会话，增加未读数
    if (!state.currentSession || state.currentSession.id !== message.sessionId) {
      const session = state.sessions.find(s => s.id === message.sessionId)
      if (session) {
        commit('UPDATE_SESSION', {
          sessionId: message.sessionId,
          updates: {
            unreadCount: (session.unreadCount || 0) + 1,
            lastMessage: {
              content: message.content,
              timestamp: message.timestamp,
            }
          }
        })
      }
    }
  },

  // 置顶/取消置顶会话
  async toggleSessionPin({ commit, state }, sessionId) {
    const session = state.sessions.find(s => s.id === sessionId)
    if (!session) return

    const pinned = !session.pinned
    try {
      await updateSession(sessionId, { pinned })
      commit('UPDATE_SESSION', { sessionId, updates: { pinned } })
    } catch (error) {
      ElMessage.error('操作失败')
    }
  },

  // 静音/取消静音会话
  async toggleSessionMute({ commit, state }, sessionId) {
    const session = state.sessions.find(s => s.id === sessionId)
    if (!session) return

    const muted = !session.muted
    try {
      await updateSession(sessionId, { muted })
      commit('UPDATE_SESSION', { sessionId, updates: { muted } })
    } catch (error) {
      ElMessage.error('操作失败')
    }
  },

  // 删除会话
  async deleteSession({ commit, state }, sessionId) {
    try {
      await apiDeleteSession(sessionId)
      commit('REMOVE_SESSION', sessionId)
      if (state.currentSession?.id === sessionId) {
        commit('SET_CURRENT_SESSION', null)
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  },

  // 下载文件
  downloadFile({ commit }, message) {
    if (message.type === 'file' && message.content?.url) {
      const link = document.createElement('a')
      link.href = message.content.url
      link.download = message.content.name || 'file'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }
  },

  // 更新WebSocket连接状态
  setWsConnected({ commit }, status) {
    commit('SET_WS_CONNECTED', status)
  },

  // 更新在线状态
  updateOnlineStatus({ commit }, { userId, status }) {
    commit('SET_ONLINE_STATUS', { userId, status })
  },
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions,
}
