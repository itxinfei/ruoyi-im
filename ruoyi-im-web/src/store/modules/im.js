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
  getGroups,
  deleteMessage,
  recallMessage,
  pinConversation,
  muteConversation,
  deleteConversation as apiDeleteConversation,
  markConversationAsRead
} from '@/api/im'
import { formatMessagePreview, formatMessagePreviewFromObject } from '@/utils/message'

// 简单UUID生成
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

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
    },

    // 当前正在回复的消息
    replyingMessage: null,

    // 用户在线状态 { userId: 'online' | 'offline' }
    userStatus: {},

    // 系统实用设置
    settings: {
      notifications: {
        enabled: true,
        sound: false
      },
      privacy: {
        showStatus: true,
        readReceipt: true
      },
      general: {
        language: 'zh-CN',
        theme: 'auto' // 'light' | 'dark' | 'auto'
      },
      shortcuts: {
        send: 'enter' // 'enter' | 'ctrl-enter'
      }
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

    // 预加消息（历史消息）
    PREPEND_MESSAGES(state, { sessionId, messages }) {
      if (!state.messages[sessionId]) {
        state.messages[sessionId] = []
      }
      state.messages[sessionId] = [...messages, ...state.messages[sessionId]]
    },

    // 添加消息
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

    // 删除会话
    DELETE_SESSION(state, sessionId) {
      const index = state.sessions.findIndex(s => s.id === sessionId)
      if (index !== -1) {
        state.sessions.splice(index, 1)
      }
      if (state.currentSession && state.currentSession.id === sessionId) {
        state.currentSession = null
      }
      state.totalUnreadCount = state.sessions.reduce((sum, s) => sum + (s.unreadCount || 0), 0)
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

    // 设置回复的消息
    SET_REPLYING_MESSAGE(state, message) {
      state.replyingMessage = message
    },

    // 更新用户状态
    SET_USER_STATUS(state, { userId, status }) {
      state.userStatus = { ...state.userStatus, [userId]: status }
    },

    // 批量设置用户状态
    SET_ALL_USER_STATUS(state, statusMap) {
      state.userStatus = { ...state.userStatus, ...statusMap }
    },

    // 更新系统设置
    UPDATE_SETTINGS(state, settings) {
      state.settings = { ...state.settings, ...settings }
      localStorage.setItem('im-system-settings', JSON.stringify(state.settings))
    },

    // 加载本地设置
    LOAD_SETTINGS(state) {
      try {
        const local = localStorage.getItem('im-system-settings')
        if (local) {
          state.settings = { ...state.settings, ...JSON.parse(local) }
        }
      } catch (e) {
        console.warn('加载设置失败', e)
      }
    },

    // 清空状态
    CLEAR_STATE(state) {
      state.sessions = []
      state.currentSession = null
      state.messages = {}
      state.contacts = []
      state.groups = []
      state.replyingMessage = null
      state.totalUnreadCount = 0
      state.userStatus = {}
    }
  },

  actions: {
    async loadSessions({ commit }) {
      commit('SET_LOADING', { key: 'sessions', value: true })
      try {
        const res = await getConversations()
        if (res.code === 200 && res.data) {
          const sessions = res.data.map(session => ({
            ...session,
            lastMessage: session.lastMessage ? formatMessagePreviewFromObject(session.lastMessage) : '[暂无消息]'
          }))
          commit('SET_SESSIONS', sessions)

          // 同步所有私聊用户的在线状态
          const userStatusMap = {}
          sessions.forEach(session => {
            if (session.type === 'PRIVATE' && session.targetId && session.peerOnline !== undefined) {
              userStatusMap[session.targetId] = session.peerOnline ? 'online' : 'offline'
            }
          })

          if (Object.keys(userStatusMap).length > 0) {
            commit('SET_ALL_USER_STATUS', userStatusMap)
          }
        }
      } finally {
        commit('SET_LOADING', { key: 'sessions', value: false })
      }
    },

    async loadMessages({ commit }, { sessionId, lastMessageId = null, pageSize = 20, isLoadMore = false }) {
      commit('SET_LOADING', { key: 'messages', value: true })
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
        commit('SET_LOADING', { key: 'messages', value: false })
      }
    },

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
        commit('UPDATE_SESSION', {
          id: sessionId,
          lastMessage: formatMessagePreviewFromObject(res.data),
          lastMessageTime: res.data.timestamp,
          lastMessageType: type
        })
        return res.data
      }
      throw new Error('发送消息失败')
    },

    async editMessage({ commit, state }, { messageId, content }) {
      const res = await apiEditMessage(messageId, { content })
      if (res.code === 200) {
        // 查找该消息在哪个会话中 (通常是当前会话)
        const sessionId = state.currentSession?.id
        if (sessionId && state.messages[sessionId]) {
          const editedMsg = { ...state.messages[sessionId].find(m => m.id === messageId), content, isEdited: true }
          commit('UPDATE_MESSAGE', { sessionId, message: editedMsg })

          // 如果是最后一条消息，更新会话列表
          const session = state.sessions.find(s => s.id === sessionId)
          if (session && session.lastMessageId === messageId) {
            commit('UPDATE_SESSION', {
              id: sessionId,
              lastMessage: formatMessagePreview('TEXT', content)
            })
          }
        }
        return res
      }
      throw new Error('编辑消息失败')
    },

    async forwardMessage({ commit }, { messageId, targetConversationId }) {
      const res = await apiForwardMessage({ messageId, targetConversationId })
      if (res.code === 200 && res.data) {
        commit('UPDATE_SESSION', {
          id: targetConversationId,
          lastMessage: formatMessagePreviewFromObject(res.data),
          lastMessageTime: res.data.timestamp,
          lastMessageType: res.data.type
        })
        return res.data
      }
      throw new Error('转发消息失败')
    },

    async markMessageAsRead({ commit }, { conversationId, messageId }) {
      await markAsRead({ conversationId, messageId })
      commit('UPDATE_SESSION', {
        id: conversationId,
        unreadCount: 0
      })
    },

    async deleteMessage({ commit }, messageId) {
      await deleteMessage(messageId)
      // 需要在组件中处理移除，或者这里commit移除mutation
    },

    async recallMessage({ commit }, messageId) {
      await recallMessage(messageId)
      // 同样需要在组件中处理
    },

    async pinSession({ commit }, { sessionId, pinned }) {
      await pinConversation(sessionId, pinned)
      commit('UPDATE_SESSION', { id: sessionId, isPinned: pinned })
    },

    async muteSession({ commit }, { sessionId, muted }) {
      await muteConversation(sessionId, muted)
      commit('UPDATE_SESSION', { id: sessionId, isMuted: muted })
    },

    async deleteSession({ commit }, sessionId) {
      await apiDeleteConversation(sessionId)
      commit('DELETE_SESSION', sessionId)
    },

    async markSessionAsRead({ commit }, sessionId) {
      await markConversationAsRead(sessionId)
      commit('UPDATE_SESSION', { id: sessionId, unreadCount: 0 })
    },



    // 同样需要在组件中处理

    // 接收消息（WebSocket 推送）
    receiveMessage({ commit, state }, message) {
      const sessionId = message.conversationId || message.sessionId
      if (!sessionId) return

      // 添加消息到列表
      commit('ADD_MESSAGE', { sessionId, message })

      // 如果不是当前正在查看的会话，则增加未读数
      const isCurrentSession = state.currentSession && state.currentSession.id === sessionId
      const session = state.sessions.find(s => s.id === sessionId)

      // 检查是否有 @ 我
      const hasMention = message.atUserIds && message.atUserIds.includes(state.currentUser.id)

      commit('UPDATE_SESSION', {
        id: sessionId,
        lastMessage: formatMessagePreview(message.type, message.content),
        lastMessageTime: message.timestamp,
        lastMessageType: message.type,
        lastSenderNickname: message.senderName,
        unreadCount: isCurrentSession ? 0 : ((session?.unreadCount || 0) + 1),
        hasMention: hasMention || (isCurrentSession ? false : session?.hasMention)
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

    async selectSession({ commit, dispatch }, session) {
      commit('SET_CURRENT_SESSION', session)
      commit('UPDATE_SESSION', {
        id: session.id,
        unreadCount: 0
      })
      try {
        await markConversationAsRead(session.id)
      } catch (e) {
        console.warn('标记已读失败', e)
      }
    }
  }
}
