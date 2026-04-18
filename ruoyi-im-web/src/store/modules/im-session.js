/**
 * 会话管理模块
 * 管理会话列表、当前会话、会话操作
 * 2026 深度优化：草稿用户隔离、智能权重排序
 */
import {
  getConversations,
  pinConversation,
  muteConversation,
  deleteConversation as apiDeleteConversation,
  markConversationAsRead,
  saveDraft as apiSaveDraft,
  getSessionGroups,
  createSessionGroup,
  updateSessionGroup,
  deleteSessionGroup,
  addConversationToGroup,
  removeConversationFromGroup,
  moveConversationToGroup
} from '@/api/im'
import { formatMessagePreviewFromObject } from '@/utils/message'

export default {
  namespaced: true,

  state: () => ({
    sessions: [],
    currentSession: null,
    totalUnreadCount: 0,
    loading: false,
    currentFilter: 'all',
    sessionGroups: [],
    defaultGroupId: null
  }),

  getters: {
    // 排序后的会话列表 (深度权重：置顶 > 草稿 > 未读 > 活跃时间)
    sortedSessions(state) {
      return [...state.sessions].sort((a, b) => {
        // 1. 置顶权重最高
        if (a.isPinned !== b.isPinned) return a.isPinned ? -1 : 1
        
        // 2. 草稿权重次之 (钉钉风格：提醒用户有未完成的输入)
        const aHasDraft = !!(a.draftContent && a.draftContent.trim())
        const bHasDraft = !!(b.draftContent && b.draftContent.trim())
        if (aHasDraft !== bHasDraft) return aHasDraft ? -1 : 1
        
        // 3. 未读数权重
        const aHasUnread = (a.unreadCount || 0) > 0
        const bHasUnread = (b.unreadCount || 0) > 0
        if (aHasUnread !== bHasUnread) return aHasUnread ? -1 : 1
        
        // 4. 最后活跃时间 (消息时间或草稿时间)
        const timeA = new Date(a.lastMessageTime || a.draftTime || 0).getTime()
        const timeB = new Date(b.lastMessageTime || b.draftTime || 0).getTime()
        return timeB - timeA
      })
    },

    currentSessionUnread(state) {
      if (!state.currentSession) return 0
      const session = state.sessions.find(s => s.id === state.currentSession.id)
      return session?.unreadCount || 0
    },

    sessionById: (state) => (id) => {
      return state.sessions.find(s => s.id === id)
    },

    sessionGroups(state) {
      return state.sessionGroups
    },

    sessionsByGroup: (state) => (groupId) => {
      return state.sessions.filter(s => s.groupId === groupId)
    }
  },

  mutations: {
    SET_SESSIONS(state, sessions) {
      state.sessions = sessions
      state.totalUnreadCount = sessions.reduce((sum, s) => sum + (s.unreadCount || 0), 0)
    },

    UPDATE_SESSION(state, sessionUpdate) {
      const index = state.sessions.findIndex(s => s.id === sessionUpdate.id)
      if (index !== -1) {
        // 增量更新现有会话，保留未提及的旧字段（如 draftContent）
        state.sessions[index] = { ...state.sessions[index], ...sessionUpdate }
      } else {
        // 新会话，插入到首部
        state.sessions.unshift(sessionUpdate)
      }
      state.totalUnreadCount = state.sessions.reduce((sum, s) => sum + (Number(s.unreadCount) || 0), 0)
    },

    SET_CURRENT_SESSION(state, session) {
      state.currentSession = session
    },

    DELETE_SESSION(state, sessionId) {
      const index = state.sessions.findIndex(s => s.id === sessionId)
      if (index !== -1) state.sessions.splice(index, 1)
      if (state.currentSession?.id === sessionId) state.currentSession = null
      state.totalUnreadCount = state.sessions.reduce((sum, s) => sum + (s.unreadCount || 0), 0)
    },

    SET_LOADING(state, value) { state.loading = value },

    SET_FILTER(state, filter) { state.currentFilter = filter },

    SET_SESSION_GROUPS(state, groups) { state.sessionGroups = groups },

    ADD_SESSION_GROUP(state, group) { state.sessionGroups.push(group) },

    UPDATE_SESSION_GROUP(state, { id, ...updates }) {
      const index = state.sessionGroups.findIndex(g => g.id === id)
      if (index !== -1) state.sessionGroups[index] = { ...state.sessionGroups[index], ...updates }
    },

    REMOVE_SESSION_GROUP(state, groupId) {
      state.sessionGroups = state.sessionGroups.filter(g => g.id !== groupId)
      state.sessions.forEach(s => { if (s.groupId === groupId) s.groupId = null })
    },

    SET_SESSION_GROUP(state, { sessionId, groupId }) {
      const session = state.sessions.find(s => s.id === sessionId)
      if (session) session.groupId = groupId
    }
  },

  actions: {
    // 加载会话列表
    async loadSessions({ commit, rootState }, filter = 'all') {
      commit('SET_LOADING', true)
      try {
        const res = await getConversations(filter)
        if (res.code === 200 && res.data) {
          const currentUserId = rootState.user?.userId
          const drafts = getDraftMap(currentUserId)
          const sessions = res.data.map(session => ({
            ...session,
            lastMessage: session.lastMessage ? formatMessagePreviewFromObject(session.lastMessage) : '[暂无消息]',
            draftContent: drafts[session.id]?.content || '',
            draftTime: drafts[session.id]?.time || ''
          }))
          commit('SET_SESSIONS', sessions)
          commit('SET_FILTER', filter)

          // 同步在线状态
          const userStatusMap = {}
          sessions.forEach(s => {
            if (s.type === 'PRIVATE' && s.targetId && s.peerOnline !== undefined) {
              userStatusMap[s.targetId] = s.peerOnline ? 'online' : 'offline'
            }
          })
          if (Object.keys(userStatusMap).length > 0) {
            commit('im/contact/SET_ALL_USER_STATUS', userStatusMap, { root: true })
          }
        }
      } finally {
        commit('SET_LOADING', false)
      }
    },

    // 保存草稿 (带用户隔离与持久化)
    async saveDraft({ commit, rootState }, { sessionId, content }) {
      if (!sessionId) return
      const currentUserId = rootState.user?.userId
      const drafts = getDraftMap(currentUserId)
      
      const trimmedContent = (content || '').trim()
      if (trimmedContent) {
        drafts[sessionId] = { content: trimmedContent, time: Date.now() }
        setDraftMap(drafts, currentUserId)
        commit('UPDATE_SESSION', { 
          id: sessionId, 
          draftContent: trimmedContent, 
          draftTime: drafts[sessionId].time 
        })
      } else {
        delete drafts[sessionId]
        setDraftMap(drafts, currentUserId)
        commit('UPDATE_SESSION', { id: sessionId, draftContent: '', draftTime: '' })
      }
      
      // 同步到后端
      try {
        await apiSaveDraft(sessionId, trimmedContent)
      } catch (e) {
        console.warn('同步草稿至服务器失败', e)
      }
    },

    // 清除草稿
    clearDraft({ commit, rootState }, sessionId) {
      if (!sessionId) return
      const currentUserId = rootState.user?.userId
      const drafts = getDraftMap(currentUserId)
      delete drafts[sessionId]
      setDraftMap(drafts, currentUserId)
      commit('UPDATE_SESSION', { id: sessionId, draftContent: '', draftTime: '' })
    },

    // 删除会话 (同步清除本地草稿)
    async deleteSession({ commit, rootState }, sessionId) {
      try {
        await apiDeleteConversation(sessionId)
        const currentUserId = rootState.user?.userId
        const drafts = getDraftMap(currentUserId)
        if (drafts[sessionId]) {
          delete drafts[sessionId]
          setDraftMap(drafts, currentUserId)
        }
        commit('DELETE_SESSION', sessionId)
      } catch (e) {
        console.error('删除会话失败', e)
        throw e
      }
    },

    async pinSession({ commit }, { sessionId, pinned }) {
      await pinConversation(sessionId, pinned)
      commit('UPDATE_SESSION', { id: sessionId, isPinned: pinned })
    },

    async muteSession({ commit }, { sessionId, muted }) {
      await muteConversation(sessionId, muted)
      commit('UPDATE_SESSION', { id: sessionId, isMuted: muted })
    },

    async markSessionAsRead({ commit }, sessionId) {
      await markConversationAsRead(sessionId)
      commit('UPDATE_SESSION', { id: sessionId, unreadCount: 0 })
    },

    async selectSession({ commit }, session) {
      commit('SET_CURRENT_SESSION', session)
      commit('UPDATE_SESSION', { id: session.id, unreadCount: 0 })
      try {
        await markConversationAsRead(session.id)
      } catch (e) {
        console.warn('标记已读失败', e)
      }
    },

    async loadSessionGroups({ commit }) {
      try {
        const res = await getSessionGroups()
        if (res.code === 200) commit('SET_SESSION_GROUPS', res.data || [])
      } catch (e) { console.error('加载会话分组失败', e) }
    },

    async addSessionGroup({ commit }, { name, sortOrder }) {
      const res = await createSessionGroup({ name, sortOrder })
      if (res.code === 200) commit('ADD_SESSION_GROUP', res.data)
      return res.data
    },

    async editSessionGroup({ commit }, { id, name, sortOrder }) {
      const res = await updateSessionGroup(id, { name, sortOrder })
      if (res.code === 200) commit('UPDATE_SESSION_GROUP', { id, name, sortOrder })
    },

    async removeSessionGroup({ commit }, groupId) {
      const res = await deleteSessionGroup(groupId)
      if (res.code === 200) commit('REMOVE_SESSION_GROUP', groupId)
    },

    async addSessionToGroup({ commit }, { sessionId, groupId }) {
      const res = await addConversationToGroup({ conversationId: sessionId, groupId })
      if (res.code === 200) commit('SET_SESSION_GROUP', { sessionId, groupId })
    },

    async removeSessionFromGroup({ commit }, { sessionId, groupId }) {
      const res = await removeConversationFromGroup({ conversationId: sessionId, groupId })
      if (res.code === 200) commit('SET_SESSION_GROUP', { sessionId, groupId: null })
    },

    async moveSessionToGroup({ commit }, { sessionId, fromGroupId, toGroupId }) {
      const res = await moveConversationToGroup({ conversationId: sessionId, fromGroupId, toGroupId })
      if (res.code === 200) commit('SET_SESSION_GROUP', { sessionId, groupId: toGroupId })
    }
  }
}

/**
 * 草稿持久化工具
 */
function getDraftMap(userId) {
  try {
    const key = userId ? `im_drafts_${userId}` : 'im_session_drafts'
    return JSON.parse(localStorage.getItem(key) || '{}')
  } catch { return {} }
}

function setDraftMap(map, userId) {
  const key = userId ? `im_drafts_${userId}` : 'im_session_drafts'
  localStorage.setItem(key, JSON.stringify(map || {}))
}
