const mockUsers = [
  {
    id: 1,
    name: '当前用户',
    avatar: '',
    username: 'currentuser',
  },
]

const mockSessions = [
  {
    id: '1',
    name: '张三',
    avatar: '',
    type: 'private',
    lastMessage: {
      content: '你好！',
      timestamp: Date.now() - 3600000,
    },
    unreadCount: 0,
    members: [1, 2],
  },
  {
    id: '2',
    name: '技术交流群',
    avatar: '',
    type: 'group',
    lastMessage: {
      content: '[图片]',
      timestamp: Date.now() - 7200000,
    },
    unreadCount: 2,
    members: [1, 2, 3],
  },
]

const mockMessages = {
  1: [
    {
      id: 1,
      sessionId: '1',
      senderId: 2,
      type: 'text',
      content: '你好！',
      timestamp: Date.now() - 3600000,
      status: 'sent',
    },
  ],
  2: [
    {
      id: 2,
      sessionId: '2',
      senderId: 3,
      type: 'image',
      content: 'image_url',
      timestamp: Date.now() - 7200000,
      status: 'sent',
    },
  ],
}

const mockUserStatus = {
  2: 'online',
  3: 'offline',
}

const state = {
  currentUser: mockUsers[0],
  sessions: mockSessions,
  currentSessionId: null,
  messages: mockMessages,
  userStatus: mockUserStatus,
  loading: false,
  error: null,
}

const getters = {
  currentSession: state => {
    return state.sessions.find(session => session.id === state.currentSessionId)
  },
  currentMessages: state => {
    if (!state.currentSessionId) return []
    return state.messages[state.currentSessionId] || []
  },
  unreadTotal: state => {
    return state.sessions.reduce((total, session) => total + (session.unreadCount || 0), 0)
  },
  onlineUsers: state => {
    return Object.entries(state.userStatus)
      .filter(([_, status]) => status === 'online')
      .map(([userId]) => parseInt(userId))
  },
}

const mutations = {
  SET_CURRENT_SESSION_ID(state, sessionId) {
    state.currentSessionId = sessionId
  },
  SET_SESSIONS(state, sessions) {
    state.sessions = sessions
  },
  ADD_SESSION(state, session) {
    const existingIndex = state.sessions.findIndex(s => s.id === session.id)
    if (existingIndex >= 0) {
      state.sessions[existingIndex] = session
    } else {
      state.sessions.unshift(session)
    }
  },
  UPDATE_SESSION(state, { sessionId, updates }) {
    const session = state.sessions.find(s => s.id === sessionId)
    if (session) {
      Object.assign(session, updates)
    }
  },
  DELETE_SESSION(state, sessionId) {
    const index = state.sessions.findIndex(s => s.id === sessionId)
    if (index >= 0) {
      state.sessions.splice(index, 1)
    }
  },
  MARK_SESSION_READ(state, sessionId) {
    const session = state.sessions.find(s => s.id === sessionId)
    if (session) {
      session.unreadCount = 0
    }
  },
  SET_MESSAGES(state, { sessionId, messages }) {
    state.messages = {
      ...state.messages,
      [sessionId]: messages,
    }
  },
  ADD_MESSAGE(state, { sessionId, message }) {
    if (!state.messages[sessionId]) {
      state.messages[sessionId] = []
    }
    const existingIndex = state.messages[sessionId].findIndex(m => m.id === message.id)
    if (existingIndex >= 0) {
      state.messages[sessionId][existingIndex] = message
    } else {
      state.messages[sessionId].push(message)
    }
  },
  UPDATE_MESSAGE(state, { sessionId, messageId, updates }) {
    const message = state.messages[sessionId]?.find(msg => msg.id === messageId)
    if (message) {
      Object.assign(message, updates)
    }
  },
  DELETE_MESSAGE(state, { sessionId, messageId }) {
    const index = state.messages[sessionId]?.findIndex(msg => msg.id === messageId)
    if (index >= 0) {
      state.messages[sessionId].splice(index, 1)
    }
  },
  UPDATE_SESSION_LAST_MESSAGE(state, { sessionId, content, timestamp, type }) {
    const session = state.sessions.find(s => s.id === sessionId)
    if (session) {
      session.lastMessage = {
        content: type === 'text' ? content : `[${type}消息]`,
        timestamp,
      }
    }
  },
  UPDATE_USER_STATUS(state, { userId, status }) {
    state.userStatus = {
      ...state.userStatus,
      [userId]: status,
    }
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  SET_ERROR(state, error) {
    state.error = error
  },
}

const actions = {
  selectSession({ commit, state }, sessionId) {
    commit('SET_CURRENT_SESSION_ID', sessionId)
    commit('MARK_SESSION_READ', sessionId)
  },

  async sendMessage({ commit, state }, { content, type = 'text', extra = {} }) {
    if (!state.currentSessionId) {
      throw new Error('No active session')
    }

    const newMessage = {
      id: Date.now(),
      sessionId: state.currentSessionId,
      senderId: state.currentUser.id,
      type,
      content,
      timestamp: Date.now(),
      status: 'sending',
      ...extra,
    }

    commit('ADD_MESSAGE', { sessionId: state.currentSessionId, message: newMessage })

    try {
      await new Promise(resolve => setTimeout(resolve, 500))

      commit('UPDATE_MESSAGE', {
        sessionId: state.currentSessionId,
        messageId: newMessage.id,
        updates: { status: 'sent' },
      })

      commit('UPDATE_SESSION_LAST_MESSAGE', {
        sessionId: state.currentSessionId,
        content: newMessage.content,
        timestamp: newMessage.timestamp,
        type: newMessage.type,
      })

      return newMessage
    } catch (error) {
      commit('UPDATE_MESSAGE', {
        sessionId: state.currentSessionId,
        messageId: newMessage.id,
        updates: { status: 'failed' },
      })
      throw error
    }
  },

  async resendMessage({ commit, state }, messageId) {
    const message = state.messages[state.currentSessionId]?.find(msg => msg.id === messageId)
    if (!message || message.status !== 'failed') return

    commit('UPDATE_MESSAGE', {
      sessionId: state.currentSessionId,
      messageId: message.id,
      updates: { status: 'sending' },
    })

    try {
      await new Promise(resolve => setTimeout(resolve, 500))
      commit('UPDATE_MESSAGE', {
        sessionId: state.currentSessionId,
        messageId: message.id,
        updates: { status: 'sent' },
      })
    } catch (error) {
      commit('UPDATE_MESSAGE', {
        sessionId: state.currentSessionId,
        messageId: message.id,
        updates: { status: 'failed' },
      })
      throw error
    }
  },

  async loadMessages({ commit, state }, { sessionId, limit = 50, before = null }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)

    try {
      const existingMessages = state.messages[sessionId] || []
      const messages = before
        ? existingMessages.filter(m => m.timestamp < before)
        : existingMessages.slice(-limit)

      commit('SET_MESSAGES', { sessionId, messages })
      return messages
    } catch (error) {
      commit('SET_ERROR', error.message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  updateUserStatus({ commit }, { userId, status }) {
    commit('UPDATE_USER_STATUS', { userId, status })
  },

  toggleSessionPin({ commit, state }, sessionId) {
    const session = state.sessions.find(s => s.id === sessionId)
    if (session) {
      commit('UPDATE_SESSION', {
        sessionId,
        updates: { pinned: !session.pinned },
      })
    }
  },

  toggleSessionMute({ commit, state }, sessionId) {
    const session = state.sessions.find(s => s.id === sessionId)
    if (session) {
      commit('UPDATE_SESSION', {
        sessionId,
        updates: { muted: !session.muted },
      })
    }
  },

  deleteSession({ commit, state }, sessionId) {
    commit('DELETE_SESSION', sessionId)
    if (state.currentSessionId === sessionId) {
      commit('SET_CURRENT_SESSION_ID', null)
    }
  },

  clearError({ commit }) {
    commit('SET_ERROR', null)
  },
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions,
}
