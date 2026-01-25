/**
 * IM 模块主 Store
 * 管理即时通讯的核心状态和子模块
 */
import session from './im-session'
import message from './im-message'
import contact from './im-contact'

export default {
  namespaced: true,

  // 注册子模块
  modules: {
    session,
    message,
    contact
  },

  state: () => ({
    // 当前用户
    currentUser: {
      id: null,
      name: '',
      avatar: '',
      email: ''
    },

    // WebSocket 连接状态
    wsConnected: false,

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
    // 当前用户ID
    currentUserId: (state) => state.currentUser.id,

    // 是否已登录
    isLoggedIn: (state) => !!state.currentUser.id,

    // 通知设置
    notificationSettings: (state) => state.settings.notifications,

    // 隐私设置
    privacySettings: (state) => state.settings.privacy,

    // 通用设置
    generalSettings: (state) => state.settings.general,

    // 快捷键设置
    shortcutSettings: (state) => state.settings.shortcuts
  },

  mutations: {
    // 设置当前用户
    SET_CURRENT_USER(state, user) {
      state.currentUser = user
    },

    // 设置 WebSocket 连接状态
    SET_WS_CONNECTED(state, connected) {
      state.wsConnected = connected
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

    // 清空所有状态
    CLEAR_ALL_STATE(state) {
      state.currentUser = {
        id: null,
        name: '',
        avatar: '',
        email: ''
      }
      state.wsConnected = false
    }
  },

  actions: {
    // 初始化设置
    initSettings({ commit }) {
      commit('LOAD_SETTINGS')
    },

    // 更新通知设置
    updateNotificationSettings({ commit }, settings) {
      commit('UPDATE_SETTINGS', { notifications: { ...settings } })
    },

    // 更新隐私设置
    updatePrivacySettings({ commit }, settings) {
      commit('UPDATE_SETTINGS', { privacy: { ...settings } })
    },

    // 更新通用设置
    updateGeneralSettings({ commit }, settings) {
      commit('UPDATE_SETTINGS', { general: { ...settings } })
    },

    // 更新快捷键设置
    updateShortcutSettings({ commit }, settings) {
      commit('UPDATE_SETTINGS', { shortcuts: { ...settings } })
    },

    // 设置当前用户
    setCurrentUser({ commit }, user) {
      commit('SET_CURRENT_USER', user)
    },

    // 设置 WebSocket 连接状态
    setWsConnected({ commit }, connected) {
      commit('SET_WS_CONNECTED', connected)
    },

    // 登出 - 清空所有状态
    logout({ commit }) {
      commit('CLEAR_ALL_STATE')
      commit('session/CLEAR_STATE', null, { root: true })
      commit('message/CLEAR_STATE', null, { root: true })
      commit('contact/CLEAR_STATE', null, { root: true })
    }
  }
}
