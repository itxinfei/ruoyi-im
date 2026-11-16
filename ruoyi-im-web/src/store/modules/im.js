/**
 * IM 模块主 Store
 * 管理即时通讯的核心状态和子模块
 */
import session from './im-session'
import message from './im-message'
import contact from './im-contact'

/**
 * 系统设置默认值
 * 与「设置」页（SystemSettingsPage）的字段一一对应
 */
const defaultSettings = () => ({
  notifications: {
    enabled: true,
    sound: true,
    preview: true,
    dnd: '22:00 - 08:00'
  },
  privacy: {
    showStatus: true,
    readReceipt: true,
    blockStranger: false
  },
  general: {
    language: 'zh-CN',
    theme: 'auto', // 'light' | 'dark' | 'auto'
    themeColor: 'blue', // blue | orange | green | red | purple
    density: 'standard', // compact | standard | loose
    enterToSend: true,
    autoUpdate: true
  },
  shortcuts: {
    send: 'enter', // 'enter' | 'ctrl-enter'
    globalSearch: 'Ctrl + K',
    newChat: 'Ctrl + N',
    screenshot: 'Alt + A',
    scrollTop: 'Home'
  }
})

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

    // 输入中状态 { conversationId: { userId: timestamp } }
    typingUsers: {},

    // 系统实用设置
    settings: defaultSettings()
  }),

  getters: {
    // 当前用户ID
    currentUserId: (state) => state.currentUser.id,

    // 是否已登录
    isLoggedIn: (state) => !!state.currentUser.id,

    // 获取指定会话的输入中用户列表
    getTypingUsers: (state) => (conversationId) => {
      const users = state.typingUsers[conversationId] || {}
      const now = Date.now()
      // 过滤掉超过 5 秒的输入状态
      return Object.entries(users)
        .filter(([_, timestamp]) => now - timestamp < 5000)
        .map(([userId]) => userId)
    },

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

    // 设置用户输入中状态
    SET_TYPING_USER(state, { conversationId, userId, isTyping }) {
      if (!state.typingUsers[conversationId]) {
        state.typingUsers[conversationId] = {}
      }
      if (isTyping) {
        state.typingUsers[conversationId][userId] = Date.now()
      } else {
        delete state.typingUsers[conversationId][userId]
      }
    },

    // 清除会话的输入中状态
    CLEAR_TYPING_USERS(state, conversationId) {
      if (state.typingUsers[conversationId]) {
        delete state.typingUsers[conversationId]
      }
    },

    // 更新系统设置
    UPDATE_SETTINGS(state, settings) {
      state.settings = { ...state.settings, ...settings }
      localStorage.setItem('im-system-settings', JSON.stringify(state.settings))
    },

    // 加载本地设置（与默认值深度合并，兼容旧版本缺少的新字段）
    LOAD_SETTINGS(state) {
      const defaults = defaultSettings()
      try {
        const local = localStorage.getItem('im-system-settings')
        const parsed = local ? JSON.parse(local) : {}
        state.settings = {
          notifications: { ...defaults.notifications, ...(parsed.notifications || {}) },
          privacy: { ...defaults.privacy, ...(parsed.privacy || {}) },
          general: { ...defaults.general, ...(parsed.general || {}) },
          shortcuts: { ...defaults.shortcuts, ...(parsed.shortcuts || {}) }
        }
      } catch (e) {
        console.warn('加载设置失败', e)
        state.settings = defaults
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
      state.typingUsers = {}
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

    // 设置用户输入中状态
    setTypingUser({ commit }, payload) {
      commit('SET_TYPING_USER', payload)
    },

    // 登出 - 清空所有状态
    logout({ commit }) {
      commit('CLEAR_ALL_STATE')
      commit('im/session/CLEAR_STATE', null, { root: true })
      commit('im/message/CLEAR_STATE', null, { root: true })
      commit('im/contact/CLEAR_STATE', null, { root: true })
    }
  }
}
