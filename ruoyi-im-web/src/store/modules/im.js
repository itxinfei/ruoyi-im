/**
 * IM 模块 Vuex Store
 * 管理即时通讯相关的状态
 */
import {
  getConversations,
  getMessages,
  sendMessage as apiSendMessage,
  markAsRead,
  getContacts,
  getGroups
} from '@/api/im'

export default {
  namespaced: true,

  state: () => ({
    // 当前用户
    currentUser: {
      id: null,
      name: '',
      avatar: '',
      email: ''
    },

    // 会话列表
    sessions: [],

    // 当前选中的会话
    currentSession: null,

    // 消息列表（按 sessionId 分组）
    messages: {},

    // 联系人列表
    contacts: [],

    // 群组列表
    groups: [],

    // 未读消息总数
    totalUnreadCount: 0,

    // WebSocket 连接状态
    wsConnected: false,

    // 加载状态
    loading: {
      sessions: false,
      messages: false,
      contacts: false,
      groups: false
    }
  }),

  getters: {
    currentMessages(state) {
      if (!state.currentSession) return []
      return state.messages[state.currentSession.id] || []
    },

    currentSessionUnread(state) {
      if (!state.currentSession) return 0
      const session = state.sessions.find(s => s.id === state.currentSession.id)
      return session?.unreadCount || 0
    },

    sortedSessions(state) {
      return [...state.sessions].sort((a, b) => {
        if (a.isPinned && !b.isPinned) return -1
        if (!a.isPinned && b.isPinned) return 1
        return new Date(b.lastMessageTime || 0) - new Date(a.lastMessageTime || 0)
      })
    },

    contactMap(state) {
      return new Map(state.contacts.map(contact => [contact.id, contact]))
    },

    groupMap(state) {
      return new Map(state.groups.map(group => [group.id, group]))
    }
  },

  mutations: {
    // 设置当前用户
    SET_CURRENT_USER(state, user) {
      state.currentUser = user
    },

    // 设置会话列表
    SET_SESSIONS(state, sessions) {
      state.sessions = sessions
      state.totalUnreadCount = sessions.reduce((sum, s) => sum + (s.unreadCount || 0), 0)
    },

    // 更新单个会话
    UPDATE_SESSION(state, session) {
      const index = state.sessions.findIndex(s => s.id === session.id)
      if (index !== -1) {
        state.sessions[index] = { ...state.sessions[index], ...session }
      } else {
        state.sessions.push(session)
      }
      // 更新未读总数
      state.totalUnreadCount = state.sessions.reduce((sum, s) => sum + (s.unreadCount || 0), 0)
    },

    // 设置当前会话
    SET_CURRENT_SESSION(state, session) {
      state.currentSession = session
    },

    // 设置消息列表
    SET_MESSAGES(state, { sessionId, messages }) {
      state.messages[sessionId] = messages
    },

    // 添加消息
    ADD_MESSAGE(state, { sessionId, message }) {
      if (!state.messages[sessionId]) {
        state.messages[sessionId] = []
      }
      state.messages[sessionId].push(message)
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

    // 设置联系人列表
    SET_CONTACTS(state, contacts) {
      state.contacts = contacts
    },

    // 设置群组列表
    SET_GROUPS(state, groups) {
      state.groups = groups
    },

    // 设置 WebSocket 连接状态
    SET_WS_CONNECTED(state, connected) {
      state.wsConnected = connected
    },

    // 设置加载状态
    SET_LOADING(state, { key, value }) {
      state.loading[key] = value
    },

    // 清空状态
    CLEAR_STATE(state) {
      state.sessions = []
      state.currentSession = null
      state.messages = {}
      state.contacts = []
      state.groups = []
      state.totalUnreadCount = 0
    }
  },

  actions: {
    async loadSessions({ commit }) {
      commit('SET_LOADING', { key: 'sessions', value: true })
      try {
        const res = await getConversations()
        if (res.code === 200 && res.data) {
          commit('SET_SESSIONS', res.data)
        }
      } finally {
        commit('SET_LOADING', { key: 'sessions', value: false })
      }
    },

    async loadMessages({ commit }, { sessionId, lastMessageId = null, pageSize = 20 }) {
      commit('SET_LOADING', { key: 'messages', value: true })
      try {
        const res = await getMessages({
          conversationId: sessionId,
          lastMessageId,
          pageSize
        })
        if (res.code === 200 && res.data) {
          commit('SET_MESSAGES', { sessionId, messages: res.data })
        }
      } finally {
        commit('SET_LOADING', { key: 'messages', value: false })
      }
    },

    async sendMessage({ commit }, { sessionId, messageType = 'TEXT', content, replyToMessageId = null }) {
      const res = await apiSendMessage({
        conversationId: sessionId,
        messageType,
        content,
        replyToMessageId
      })

      if (res.code === 200 && res.data) {
        commit('ADD_MESSAGE', { sessionId, message: res.data })
        commit('UPDATE_SESSION', {
          id: sessionId,
          lastMessage: content,
          lastMessageTime: res.data.timestamp
        })
        return res.data
      }
      throw new Error('发送消息失败')
    },

    async markMessageAsRead({ commit }, { conversationId, messageId }) {
      await markAsRead({ conversationId, messageId })
      commit('UPDATE_SESSION', {
        id: conversationId,
        unreadCount: 0
      })
    },

    // 接收消息（WebSocket 推送）
    receiveMessage({ commit, state }, message) {
      const sessionId = message.conversationId

      // 添加消息到列表
      commit('ADD_MESSAGE', { sessionId, message })

      // 更新会话信息
      commit('UPDATE_SESSION', {
        id: sessionId,
        lastMessage: message.content,
        lastMessageTime: message.timestamp,
        unreadCount: (state.sessions.find(s => s.id === sessionId)?.unreadCount || 0) + 1
      })
    },

    async loadContacts({ commit }) {
      commit('SET_LOADING', { key: 'contacts', value: true })
      try {
        const res = await getContacts()
        if (res.code === 200 && res.data) {
          commit('SET_CONTACTS', res.data)
        }
      } finally {
        commit('SET_LOADING', { key: 'contacts', value: false })
      }
    },

    async loadGroups({ commit }) {
      commit('SET_LOADING', { key: 'groups', value: true })
      try {
        const res = await getGroups()
        if (res.code === 200 && res.data) {
          commit('SET_GROUPS', res.data)
        }
      } finally {
        commit('SET_LOADING', { key: 'groups', value: false })
      }
    },

    selectSession({ commit }, session) {
      commit('SET_CURRENT_SESSION', session)
      commit('UPDATE_SESSION', {
        id: session.id,
        unreadCount: 0
      })
    }
  }
}
