/**
 * 联系人和群组管理模块
 * 管理联系人列表、群组列表、用户在线状态
 */
import {
  getContacts,
  getGroups
} from '@/api/im'

export default {
  namespaced: true,

  state: () => ({
    // 联系人列表
    contacts: [],

    // 群组列表
    groups: [],

    // 用户在线状态 { userId: 'online' | 'offline' }
    userStatus: {},

    // 加载状态
    loading: {
      contacts: false,
      groups: false
    }
  }),

  getters: {
    // 联系人 Map（便于快速查找）
    contactMap(state) {
      return new Map(state.contacts.map(contact => [contact.id, contact]))
    },

    // 群组 Map（便于快速查找）
    groupMap(state) {
      return new Map(state.groups.map(group => [group.id, group]))
    },

    // 在线用户列表
    onlineUsers(state) {
      return Object.entries(state.userStatus)
        .filter(([_, status]) => status === 'online')
        .map(([userId]) => userId)
    },

    // 根据用户ID获取联系人
    contactById: (state, getters) => (userId) => {
      return getters.contactMap.get(userId)
    },

    // 根据群组ID获取群组
    groupById: (state, getters) => (groupId) => {
      return getters.groupMap.get(groupId)
    },

    // 获取用户在线状态
    getUserStatus: (state) => (userId) => {
      return state.userStatus[userId] || 'offline'
    }
  },

  mutations: {
    // 设置联系人列表
    SET_CONTACTS(state, contacts) {
      state.contacts = contacts
    },

    // 添加或更新单个联系人
    UPDATE_CONTACT(state, contact) {
      const index = state.contacts.findIndex(c => c.id === contact.id)
      if (index !== -1) {
        // 创建新数组触发响应式更新
        state.contacts = [
          ...state.contacts.slice(0, index),
          { ...state.contacts[index], ...contact },
          ...state.contacts.slice(index + 1)
        ]
      } else {
        // 创建新数组触发响应式更新
        state.contacts = [...state.contacts, contact]
      }
    },

    // 删除联系人
    DELETE_CONTACT(state, contactId) {
      const index = state.contacts.findIndex(c => c.id === contactId)
      if (index !== -1) {
        // 创建新数组触发响应式更新
        state.contacts = [...state.contacts.slice(0, index), ...state.contacts.slice(index + 1)]
      }
    },

    // 设置群组列表
    SET_GROUPS(state, groups) {
      state.groups = groups
    },

    // 添加或更新单个群组
    UPDATE_GROUP(state, group) {
      const index = state.groups.findIndex(g => g.id === group.id)
      if (index !== -1) {
        // 创建新数组触发响应式更新
        state.groups = [
          ...state.groups.slice(0, index),
          { ...state.groups[index], ...group },
          ...state.groups.slice(index + 1)
        ]
      } else {
        // 创建新数组触发响应式更新
        state.groups = [...state.groups, group]
      }
    },

    // 删除群组
    DELETE_GROUP(state, groupId) {
      const index = state.groups.findIndex(g => g.id === groupId)
      if (index !== -1) {
        // 创建新数组触发响应式更新
        state.groups = [...state.groups.slice(0, index), ...state.groups.slice(index + 1)]
      }
    },

    // 更新用户状态
    SET_USER_STATUS(state, { userId, status }) {
      state.userStatus = { ...state.userStatus, [userId]: status }
    },

    // 批量设置用户状态
    SET_ALL_USER_STATUS(state, statusMap) {
      state.userStatus = { ...state.userStatus, ...statusMap }
    },

    // 移除用户状态
    REMOVE_USER_STATUS(state, userId) {
      const newStatus = { ...state.userStatus }
      delete newStatus[userId]
      state.userStatus = newStatus
    },

    // 设置加载状态
    SET_LOADING(state, { key, value }) {
      state.loading[key] = value
    },

    // 清空联系人状态
    CLEAR_STATE(state) {
      state.contacts = []
      state.groups = []
      state.userStatus = {}
    }
  },

  actions: {
    // 加载联系人列表
    async loadContacts({ commit, state }) {
      // 如果已经有数据，先不重新加载（避免重复请求）
      if (state.contacts.length > 0) return

      commit('SET_LOADING', { key: 'contacts', value: true })
      try {
        const res = await getContacts()
        if (res.code === 200 && res.data) {
          commit('SET_CONTACTS', res.data)
        } else {
          // API 返回非 200，确保有空数组
          commit('SET_CONTACTS', [])
        }
      } catch (error) {
        // 网络错误时设置空数组，避免 UI 报错
        console.warn('加载联系人失败，使用空列表:', error.message)
        commit('SET_CONTACTS', [])
      } finally {
        commit('SET_LOADING', { key: 'contacts', value: false })
      }
    },

    // 加载群组列表
    async loadGroups({ commit, state }) {
      // 如果已经有数据，先不重新加载
      if (state.groups.length > 0) return

      commit('SET_LOADING', { key: 'groups', value: true })
      try {
        const res = await getGroups()
        if (res.code === 200 && res.data) {
          commit('SET_GROUPS', res.data)
        } else {
          commit('SET_GROUPS', [])
        }
      } catch (error) {
        // 网络错误时设置空数组
        console.warn('加载群组失败，使用空列表:', error.message)
        commit('SET_GROUPS', [])
      } finally {
        commit('SET_LOADING', { key: 'groups', value: false })
      }
    },

    // 更新用户在线状态
    updateUserStatus({ commit }, { userId, status }) {
      commit('SET_USER_STATUS', { userId, status })
    },

    // 批量更新用户在线状态
    batchUpdateUserStatus({ commit }, statusMap) {
      commit('SET_ALL_USER_STATUS', statusMap)
    },

    // 用户下线
    userOffline({ commit }, userId) {
      commit('REMOVE_USER_STATUS', userId)
    }
  }
}
