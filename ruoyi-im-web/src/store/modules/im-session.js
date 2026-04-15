/**
 * 会话管理模块
 * 管理会话列表、当前会话、会话操作
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
    // 会话列表
    sessions: [],

    // 当前选中的会话
    currentSession: null,

    // 未读消息总数
    totalUnreadCount: 0,

    // 加载状态
    loading: false,

    // 当前筛选类型
    currentFilter: 'all',

    // 会话分组列表
    sessionGroups: [],

    // 默认分组ID（未分组）
    defaultGroupId: null
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
    },

    // 获取会话分组
    sessionGroups(state) {
      return state.sessionGroups
    },

    // 根据分组获取会话
    sessionsByGroup: (state) => (groupId) => {
      if (!groupId) {
        // 未分组：返回没有分组ID的会话
        return state.sessions.filter(s => !s.groupId)
      }
      return state.sessions.filter(s => s.groupId === groupId)
    },

    // 分组后的会话列表（带分组标题）
    groupedSessions(state) {
      const groups = state.sessionGroups.map(g => ({
        ...g,
        sessions: state.sessions.filter(s => s.groupId === g.id)
      }))
      // 未分组的会话
      const ungrouped = state.sessions.filter(s => !s.groupId)
      if (ungrouped.length > 0) {
        groups.push({
          id: null,
          name: '未分组',
          sessions: ungrouped
        })
      }
      return groups
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
        // 合并更新现有会话
        state.sessions[index] = { ...state.sessions[index], ...session }
      } else {
        // 新会话，插入到列表首部
        state.sessions.unshift(session)
      }

      // 更新未读总数
      state.totalUnreadCount = state.sessions.reduce((sum, s) => sum + (Number(s.unreadCount) || 0), 0)
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
      state.currentFilter = 'all'
      state.sessionGroups = []
    },

    // 设置筛选类型
    SET_FILTER(state, filter) {
      state.currentFilter = filter
    },

    // 设置会话分组列表
    SET_SESSION_GROUPS(state, groups) {
      state.sessionGroups = groups
    },

    // 添加会话分组
    ADD_SESSION_GROUP(state, group) {
      state.sessionGroups.push(group)
    },

    // 更新会话分组
    UPDATE_SESSION_GROUP(state, { id, ...updates }) {
      const index = state.sessionGroups.findIndex(g => g.id === id)
      if (index !== -1) {
        state.sessionGroups[index] = { ...state.sessionGroups[index], ...updates }
      }
    },

    // 删除会话分组
    REMOVE_SESSION_GROUP(state, groupId) {
      state.sessionGroups = state.sessionGroups.filter(g => g.id !== groupId)
      // 清除该分组下会话的分组ID
      state.sessions.forEach(s => {
        if (s.groupId === groupId) {
          s.groupId = null
        }
      })
    },

    // 设置会话分组
    SET_SESSION_GROUP(state, { sessionId, groupId }) {
      const session = state.sessions.find(s => s.id === sessionId)
      if (session) {
        session.groupId = groupId
      }
    }
  },

  actions: {
    // 加载会话列表
    async loadSessions({ commit }, filter = 'all') {
      commit('SET_LOADING', true)
      try {
        const res = await getConversations(filter)
        if (res.code === 200 && res.data) {
          const drafts = getDraftMap()
          const sessions = res.data.map(session => ({
            ...session,
            lastMessage: session.lastMessage ? formatMessagePreviewFromObject(session.lastMessage) : '[暂无消息]',
            draftContent: drafts[session.id]?.content || '',
            draftTime: drafts[session.id]?.time || ''
          })).sort((a, b) => {
            // 置顶优先
            if (a.isPinned !== b.isPinned) return a.isPinned ? -1 : 1
            // 草稿优先
            const aDraft = a.draftContent ? 1 : 0
            const bDraft = b.draftContent ? 1 : 0
            if (aDraft !== bDraft) return bDraft - aDraft
            // 未读优先
            const aUnread = a.unreadCount ? 1 : 0
            const bUnread = b.unreadCount ? 1 : 0
            if (aUnread !== bUnread) return bUnread - aUnread
            const timeA = new Date(a.lastMessageTime || a.draftTime || 0).getTime()
            const timeB = new Date(b.lastMessageTime || b.draftTime || 0).getTime()
            return timeB - timeA
          })
          commit('SET_SESSIONS', sessions)
          commit('SET_FILTER', filter)

          // 同步所有私聊用户的在线状态
          const userStatusMap = {}
          sessions.forEach(session => {
            if (session.type === 'PRIVATE' && session.targetId && session.peerOnline !== undefined) {
              userStatusMap[session.targetId] = session.peerOnline ? 'online' : 'offline'
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

    // 保存草稿
    async saveDraft({ commit }, { sessionId, content }) {
      if (!sessionId) return
      const drafts = getDraftMap()
      if (content && content.trim()) {
        drafts[sessionId] = { content, time: Date.now() }
        setDraftMap(drafts)
        commit('UPDATE_SESSION', { id: sessionId, draftContent: content, draftTime: drafts[sessionId].time })
        // 同步到后端
        try {
          await apiSaveDraft(sessionId, content)
        } catch (e) {
          console.error('保存草稿到服务器失败', e)
        }
      } else {
        delete drafts[sessionId]
        setDraftMap(drafts)
        commit('UPDATE_SESSION', { id: sessionId, draftContent: '', draftTime: '' })
        // 同步到后台
        try {
          await apiSaveDraft(sessionId, '')
        } catch (e) {
          console.error('保存草稿到服务器失败', e)
        }
      }
    },

    // 清除草稿
    clearDraft({ commit }, sessionId) {
      if (!sessionId) return
      const drafts = getDraftMap()
      delete drafts[sessionId]
      setDraftMap(drafts)
      commit('UPDATE_SESSION', { id: sessionId, draftContent: '', draftTime: '' })
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
    async selectSession({ commit }, session) {
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
    },

    // 加载会话分组
    async loadSessionGroups({ commit }) {
      try {
        const res = await getSessionGroups()
        if (res.code === 200) {
          commit('SET_SESSION_GROUPS', res.data || [])
        }
      } catch (e) {
        console.error('加载会话分组失败', e)
      }
    },

    // 创建会话分组
    async addSessionGroup({ commit }, { name, sortOrder }) {
      try {
        const res = await createSessionGroup({ name, sortOrder })
        if (res.code === 200) {
          commit('ADD_SESSION_GROUP', res.data)
          return res.data
        }
      } catch (e) {
        console.error('创建会话分组失败', e)
        throw e
      }
    },

    // 更新会话分组
    async editSessionGroup({ commit }, { id, name, sortOrder }) {
      try {
        const res = await updateSessionGroup(id, { name, sortOrder })
        if (res.code === 200) {
          commit('UPDATE_SESSION_GROUP', { id, name, sortOrder })
        }
      } catch (e) {
        console.error('更新会话分组失败', e)
        throw e
      }
    },

    // 删除会话分组
    async removeSessionGroup({ commit }, groupId) {
      try {
        const res = await deleteSessionGroup(groupId)
        if (res.code === 200) {
          commit('REMOVE_SESSION_GROUP', groupId)
        }
      } catch (e) {
        console.error('删除会话分组失败', e)
        throw e
      }
    },

    // 将会话添加到分组
    async addSessionToGroup({ commit }, { sessionId, groupId }) {
      try {
        const res = await addConversationToGroup({ conversationId: sessionId, groupId })
        if (res.code === 200) {
          commit('SET_SESSION_GROUP', { sessionId, groupId })
        }
      } catch (e) {
        console.error('添加会话到分组失败', e)
        throw e
      }
    },

    // 将会话从分组移除
    async removeSessionFromGroup({ commit }, { sessionId, groupId }) {
      try {
        const res = await removeConversationFromGroup({ conversationId: sessionId, groupId })
        if (res.code === 200) {
          commit('SET_SESSION_GROUP', { sessionId, groupId: null })
        }
      } catch (e) {
        console.error('从分组移除会话失败', e)
        throw e
      }
    },

    // 移动会话到另一个分组
    async moveSessionToGroup({ commit }, { sessionId, fromGroupId, toGroupId }) {
      try {
        const res = await moveConversationToGroup({ conversationId: sessionId, fromGroupId, toGroupId })
        if (res.code === 200) {
          commit('SET_SESSION_GROUP', { sessionId, groupId: toGroupId })
        }
      } catch (e) {
        console.error('移动会话到分组失败', e)
        throw e
      }
    }
  }
}

function getDraftMap() {
  try {
    return JSON.parse(localStorage.getItem('im_session_drafts') || '{}')
  } catch {
    return {}
  }
}

function setDraftMap(map) {
  localStorage.setItem('im_session_drafts', JSON.stringify(map || {}))
}
