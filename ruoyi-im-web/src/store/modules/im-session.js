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
  getConversation
} from '@/api/im'
import { formatMessagePreviewFromObject } from '@/utils/message'

// 默认分组配置
const DEFAULT_GROUPS = [
  { id: 'pinned', name: '置顶聊天', order: 0, isSystem: true, isExpanded: true },
  { id: 'default', name: '全部消息', order: 1, isSystem: true, isExpanded: true }
]

const STORAGE_KEY_GROUPS = 'im-session-groups'
const STORAGE_KEY_CONVERSATION_GROUP = 'im-conversation-group-map'
const STORAGE_KEY_DRAFTS = 'im_message_drafts'

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

    // 分组列表
    groups: [],

    // 会话ID → 分组ID 映射
    conversationGroupMap: {},

    // 草稿状态 { conversationId: { content, timestamp } }
    drafts: {},

    // 输入状态 { conversationId: { isTyping, lastTypingTime } }
    typingSessions: {}
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

    // 排序后的分组列表
    sortedGroups(state) {
      return [...state.groups].sort((a, b) => a.order - b.order)
    },

    // 分组会话列表（含分组信息）
    groupedSessions: (state, getters) => {
      const sortedGroups = getters.sortedGroups
      const sessionsByGroup = {}

      // 初始化分组
      sortedGroups.forEach(group => {
        sessionsByGroup[group.id] = {
          group,
          sessions: []
        }
      })

      // 分配会话到分组
      state.sessions.forEach(session => {
        // 置顶的会话优先进入置顶分组
        if (session.isPinned) {
          if (sessionsByGroup['pinned']) {
            sessionsByGroup['pinned'].sessions.push(session)
          }
        } else {
          // 根据映射表查找分组
          const groupId = state.conversationGroupMap[session.id] || 'default'
          if (sessionsByGroup[groupId]) {
            sessionsByGroup[groupId].sessions.push(session)
          } else {
            // 如果分组不存在，放入默认分组
            if (sessionsByGroup['default']) {
              sessionsByGroup['default'].sessions.push(session)
            }
          }
        }
      })

      // 对每个分组内的会话进行排序
      Object.values(sessionsByGroup).forEach(item => {
        item.sessions.sort((a, b) => {
          return new Date(b.lastMessageTime || 0) - new Date(a.lastMessageTime || 0)
        })
      })

      // 返回只包含有会话或展开的分组
      return Object.values(sessionsByGroup).filter(item => {
        const group = item.group
        // 系统分组始终显示，自定义分组展开时或有会话时显示
        return group.isSystem || group.isExpanded || item.sessions.length > 0
      })
    },

    // 获取会话所属分组
    sessionGroup: (state) => (conversationId) => {
      const groupId = state.conversationGroupMap[conversationId]
      return state.groups.find(g => g.id === groupId)
    },

    // ========== 草稿相关 getters ==========

    // 判断会话是否有草稿
    hasDraft: (state) => (conversationId) => {
      const draft = state.drafts[conversationId]
      return draft && draft.content && draft.content.length > 0
    },

    // 获取草稿预览文本
    getDraftPreview: (state) => (conversationId) => {
      const draft = state.drafts[conversationId]
      if (!draft || !draft.content) return ''
      return draft.content.slice(0, 20)
    },

    // 获取草稿完整内容
    getDraftContent: (state) => (conversationId) => {
      const draft = state.drafts[conversationId]
      return draft?.content || ''
    },

    // ========== 输入状态相关 getters ==========

    // 判断会话是否正在输入
    isTyping: (state) => (conversationId) => {
      const typing = state.typingSessions[conversationId]
      if (!typing) return false
      
      // 5秒后自动清除输入状态
      if (Date.now() - typing.lastTypingTime > 5000) {
        return false
      }
      
      return typing.isTyping
    },

    // 获取所有正在输入的会话ID
    getTypingSessionIds: (state) => {
      return Object.keys(state.typingSessions).filter(conversationId => {
        const typing = state.typingSessions[conversationId]
        return typing && (Date.now() - typing.lastTypingTime <= 5000)
      })
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
      let updatedSession

      if (index !== -1) {
        // 更新现有会话
        updatedSession = { ...state.sessions[index], ...session }
        state.sessions[index] = updatedSession
      } else {
        // 添加新会话
        updatedSession = session
        state.sessions.push(updatedSession)
      }

      // 如果更新了 lastMessageTime，将会话移到列表顶部（保持置顶会话在顶部）
      if (session.lastMessageTime !== undefined) {
        // 移除该会话
        state.sessions = state.sessions.filter(s => s.id !== session.id)
        // 重新插入，置顶会话保持在最前
        const pinnedSessions = state.sessions.filter(s => s.isPinned).sort((a, b) => {
          return new Date(b.lastMessageTime || 0) - new Date(a.lastMessageTime || 0)
        })
        const unpinnedSessions = state.sessions.filter(s => !s.isPinned).sort((a, b) => {
          return new Date(b.lastMessageTime || 0) - new Date(a.lastMessageTime || 0)
        })

        if (updatedSession.isPinned) {
          pinnedSessions.unshift(updatedSession)
        } else {
          unpinnedSessions.unshift(updatedSession)
        }

        state.sessions = [...pinnedSessions, ...unpinnedSessions]
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
    },

    // ========== 分组相关 mutations ==========

    // 设置分组列表
    SET_GROUPS(state, groups) {
      state.groups = groups
      // 保存到本地存储
      try {
        localStorage.setItem(STORAGE_KEY_GROUPS, JSON.stringify(groups))
      } catch (e) {
        console.warn('保存分组失败', e)
      }
    },

    // 添加分组
    ADD_GROUP(state, group) {
      state.groups.push(group)
      try {
        localStorage.setItem(STORAGE_KEY_GROUPS, JSON.stringify(state.groups))
      } catch (e) {
        console.warn('保存分组失败', e)
      }
    },

    // 更新分组
    UPDATE_GROUP(state, { groupId, updates }) {
      const index = state.groups.findIndex(g => g.id === groupId)
      if (index !== -1) {
        state.groups[index] = { ...state.groups[index], ...updates }
        try {
          localStorage.setItem(STORAGE_KEY_GROUPS, JSON.stringify(state.groups))
        } catch (e) {
          console.warn('保存分组失败', e)
        }
      }
    },

    // 删除分组
    DELETE_GROUP(state, groupId) {
      const index = state.groups.findIndex(g => g.id === groupId)
      if (index !== -1) {
        state.groups.splice(index, 1)
        // 将该分组下的会话移到默认分组
        const newMap = { ...state.conversationGroupMap }
        Object.keys(newMap).forEach(convId => {
          if (newMap[convId] === groupId) {
            delete newMap[convId]
          }
        })
        state.conversationGroupMap = newMap
        try {
          localStorage.setItem(STORAGE_KEY_GROUPS, JSON.stringify(state.groups))
          localStorage.setItem(STORAGE_KEY_CONVERSATION_GROUP, JSON.stringify(newMap))
        } catch (e) {
          console.warn('保存分组失败', e)
        }
      }
    },

    // 切换分组展开/收起
    TOGGLE_GROUP_EXPAND(state, groupId) {
      const group = state.groups.find(g => g.id === groupId)
      if (group) {
        group.isExpanded = !group.isExpanded
        try {
          localStorage.setItem(STORAGE_KEY_GROUPS, JSON.stringify(state.groups))
        } catch (e) {
          console.warn('保存分组状态失败', e)
        }
      }
    },

    // 设置会话所属分组
    SET_CONVERSATION_GROUP(state, { conversationId, groupId }) {
      state.conversationGroupMap = {
        ...state.conversationGroupMap,
        [conversationId]: groupId
      }
      try {
        localStorage.setItem(STORAGE_KEY_CONVERSATION_GROUP, JSON.stringify(state.conversationGroupMap))
      } catch (e) {
        console.warn('保存会话分组失败', e)
      }
    },

    // 批量设置会话分组
    BATCH_SET_CONVERSATION_GROUPS(state, mapping) {
      state.conversationGroupMap = {
        ...state.conversationGroupMap,
        ...mapping
      }
      try {
        localStorage.setItem(STORAGE_KEY_CONVERSATION_GROUP, JSON.stringify(state.conversationGroupMap))
      } catch (e) {
        console.warn('保存会话分组失败', e)
      }
    },

    // 加载分组数据
    LOAD_GROUPS(state) {
      try {
        const groups = localStorage.getItem(STORAGE_KEY_GROUPS)
        const map = localStorage.getItem(STORAGE_KEY_CONVERSATION_GROUP)

        if (groups) {
          state.groups = JSON.parse(groups)
        } else {
          // 初始化默认分组
          state.groups = [...DEFAULT_GROUPS]
        }

        if (map) {
          state.conversationGroupMap = JSON.parse(map)
        }
      } catch (e) {
        console.warn('加载分组数据失败', e)
        state.groups = [...DEFAULT_GROUPS]
        state.conversationGroupMap = {}
      }
    },

    // ========== 草稿相关 mutations ==========

    // 设置草稿
    SET_DRAFT(state, { conversationId, content }) {
      if (content && content.trim().length > 0) {
        state.drafts[conversationId] = {
          content: content.trim(),
          timestamp: Date.now()
        }
      } else {
        delete state.drafts[conversationId]
      }
    },

    // 清除草稿
    CLEAR_DRAFT(state, conversationId) {
      delete state.drafts[conversationId]
    },

    // 清空所有草稿
    CLEAR_ALL_DRAFTS(state) {
      state.drafts = {}
    },

    // ========== 输入状态相关 mutations ==========

    // 设置输入状态
    SET_TYPING(state, { conversationId, isTyping }) {
      if (isTyping) {
        state.typingSessions[conversationId] = {
          isTyping: true,
          lastTypingTime: Date.now()
        }
      } else {
        delete state.typingSessions[conversationId]
      }
    },

    // 清除输入状态
    CLEAR_TYPING(state, conversationId) {
      delete state.typingSessions[conversationId]
    }
  },

  actions: {
    // 加载会话列表
    async loadSessions({ commit, dispatch, rootState }) {
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
            // 使用 dispatch action 而不是跨模块 commit mutation，避免命名空间问题
            dispatch('im/contact/batchUpdateUserStatus', userStatusMap, { root: true })
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
    },

    // 根据ID选择会话（用于从通讯录发起聊天）
    async selectSessionById({ commit, state, dispatch }, conversationId) {
      // 先从现有列表中查找
      let session = state.sessions.find(s => s.id === conversationId)

      // 如果找不到，从API获取
      if (!session) {
        try {
          const res = await getConversation(conversationId)
          if (res.code === 200 && res.data) {
            session = {
              ...res.data,
              lastMessage: res.data.lastMessage ? formatMessagePreviewFromObject(res.data.lastMessage) : '[暂无消息]'
            }
            // 添加到会话列表
            commit('UPDATE_SESSION', session)
          }
        } catch (e) {
          console.error('获取会话详情失败', e)
          throw e
        }
      }

      if (session) {
        await dispatch('selectSession', session)
      }
    },

    // ========== 分组相关 actions ==========

    // 初始化分组数据
    initGroups({ commit }) {
      commit('LOAD_GROUPS')
    },

    // 创建分组
    createGroup({ commit, state }, { name, order }) {
      const id = `group_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
      const group = {
        id,
        name,
        order: order ?? state.groups.length,
        isSystem: false,
        isExpanded: true
      }
      commit('ADD_GROUP', group)
      return group
    },

    // 重命名分组
    renameGroup({ commit }, { groupId, name }) {
      commit('UPDATE_GROUP', { groupId, updates: { name } })
    },

    // 删除分组
    deleteGroup({ commit }, groupId) {
      commit('DELETE_GROUP', groupId)
    },

    // 切换分组展开/收起
    toggleGroupExpand({ commit }, groupId) {
      commit('TOGGLE_GROUP_EXPAND', groupId)
    },

    // 移动会话到分组
    moveConversationToGroup({ commit }, { conversationId, groupId }) {
      commit('SET_CONVERSATION_GROUP', { conversationId, groupId })
    },

    // 批量移动会话到分组
    batchMoveConversationsToGroup({ commit }, { conversationIds, groupId }) {
      const mapping = {}
      conversationIds.forEach(id => {
        mapping[id] = groupId
      })
      commit('BATCH_SET_CONVERSATION_GROUPS', mapping)
    },

    // 调整分组顺序
    reorderGroups({ commit, state }, { groupIds }) {
      const updatedGroups = groupIds.map((id, index) => {
        const group = state.groups.find(g => g.id === id)
        return { ...group, order: index }
      })
      commit('SET_GROUPS', updatedGroups)
    },

    // ========== 草稿相关 actions ==========

    // 保存草稿
    async saveDraft({ commit, state }, { conversationId, content }) {
      commit('SET_DRAFT', { conversationId, content })
      
      // 同步到 localStorage（持久化）
      try {
        localStorage.setItem(STORAGE_KEY_DRAFTS, JSON.stringify(state.drafts))
      } catch (e) {
        console.warn('保存草稿到 localStorage 失败', e)
      }
    },

    // 加载草稿
    async loadDrafts({ commit }) {
      try {
        const data = localStorage.getItem(STORAGE_KEY_DRAFTS)
        if (data) {
          const drafts = JSON.parse(data)
          Object.entries(drafts).forEach(([conversationId, draft]) => {
            commit('SET_DRAFT', { conversationId, content: draft.content })
          })
        }
      } catch (e) {
        console.warn('从 localStorage 加载草稿失败', e)
      }
    },

    // 清除草稿
    async clearDraft({ commit, state }, conversationId) {
      commit('CLEAR_DRAFT', conversationId)
      
      // 同步到 localStorage
      try {
        localStorage.setItem(STORAGE_KEY_DRAFTS, JSON.stringify(state.drafts))
      } catch (e) {
        console.warn('清除草稿到 localStorage 失败', e)
      }
    },

    // 清空所有草稿
    async clearAllDrafts({ commit, state }) {
      commit('CLEAR_ALL_DRAFTS')
      
      // 同步到 localStorage
      try {
        localStorage.setItem(STORAGE_KEY_DRAFTS, JSON.stringify(state.drafts))
      } catch (e) {
        console.warn('清空草稿到 localStorage 失败', e)
      }
    },

    // ========== 输入状态相关 actions ==========

    // 设置输入状态
    setTyping({ commit }, { conversationId, isTyping }) {
      commit('SET_TYPING', { conversationId, isTyping })
    },

    // 清除输入状态
    clearTyping({ commit }, conversationId) {
      commit('CLEAR_TYPING', conversationId)
    },

    // 定期清理过期的输入状态
    startTypingCleanup({ state, commit }) {
      const intervalId = setInterval(() => {
        const now = Date.now()
        Object.entries(state.typingSessions).forEach(([conversationId, typing]) => {
          if (now - typing.lastTypingTime > 5000) {
            commit('CLEAR_TYPING', conversationId)
          }
        })
      }, 1000)
      
      // 保存 intervalId 以便清理
      return intervalId
    }
  }
}