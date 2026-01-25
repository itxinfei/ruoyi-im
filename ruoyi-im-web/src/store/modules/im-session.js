/**
 * 会话管理模块
 * 管理会话列表、当前会话、会话操作
 */
import {
  getConversations,
  pinConversation,
  muteConversation,
  deleteConversation as apiDeleteConversation,
  markConversationAsRead
} from '@/api/im'
import { formatMessagePreviewFromObject } from '@/utils/message'

export default {
  namespaced: true,

  state: () => ({
    // 会话列表
    sessions: [],

    // 当前选中的会话
    currentSession: null,

    // 未读消息总数
    totalUnreadCount: 0,

    // 加载状态
    loading: false
  }),

  getters: {
    // 当前会话未读数
    currentSessionUnread(state) {
      if (!state.currentSession) return 0
      const session = state.sessions.find(s => s.id === state.currentSession.id)
      return session?.unreadCount || 0
    },

    // 排序后的会话列表（置顶优先）
    sortedSessions(state) {
      return [...state.sessions].sort((a, b) => {
        if (a.isPinned && !b.isPinned) return -1
        if (!a.isPinned && b.isPinned) return 1
        return new Date(b.lastMessageTime || 0) - new Date(a.lastMessageTime || 0)
      })
    },

    // 根据 ID 获取会话
    sessionById: (state) => (id) => {
      return state.sessions.find(s => s.id === id)
    }
  },

  mutations: {
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

    // 设置加载状态
    SET_LOADING(state, value) {
      state.loading = value
    },

    // 清空会话状态
    CLEAR_STATE(state) {
      state.sessions = []
      state.currentSession = null
      state.totalUnreadCount = 0
    }
  },

  actions: {
    // 加载会话列表
    async loadSessions({ commit, rootState }) {
      commit('SET_LOADING', true)
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
            commit('contact/SET_ALL_USER_STATUS', userStatusMap, { root: true })
          }
        }
      } finally {
        commit('SET_LOADING', false)
      }
    },

    // 置顶/取消置顶会话
    async pinSession({ commit }, { sessionId, pinned }) {
      await pinConversation(sessionId, pinned)
      commit('UPDATE_SESSION', { id: sessionId, isPinned: pinned })
    },

    // 免打扰/取消免打扰
    async muteSession({ commit }, { sessionId, muted }) {
      await muteConversation(sessionId, muted)
      commit('UPDATE_SESSION', { id: sessionId, isMuted: muted })
    },

    // 删除会话
    async deleteSession({ commit }, sessionId) {
      await apiDeleteConversation(sessionId)
      commit('DELETE_SESSION', sessionId)
    },

    // 标记会话为已读
    async markSessionAsRead({ commit }, sessionId) {
      await markConversationAsRead(sessionId)
      commit('UPDATE_SESSION', { id: sessionId, unreadCount: 0 })
    },

    // 选择会话
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
