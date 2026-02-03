/**
 * IM 模块主 Store
 * 管理即时通讯的核心状态和子模块
 */
import { getUserSettingsMap, updateUserSetting, batchUpdateUserSettings } from '@/api/im/userSettings'
import { getJSON, setJSON } from '@/utils/storage'
import session from './im-session'
import message from './im-message'
import contact from './im-contact'

// 防抖计时器
let settingsUpdateTimer = null
let pendingSettings = {}

/**
 * 执行防抖后的设置更新
 */
const flushSettingsUpdate = async (dispatch) => {
  if (Object.keys(pendingSettings).length === 0) return

  const settingsToSend = { ...pendingSettings }
  pendingSettings = {}

  try {
    await batchUpdateUserSettings(settingsToSend)
  } catch (e) {
    // 409 冲突可以忽略（设置可能已存在），其他错误记录日志
    if (e.response?.status !== 409) {
      console.error('批量更新设置到服务器失败', e)
    }
  }
}

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
        language: 'zh-CN'
      },
      shortcuts: {
        send: 'enter' // 'enter' | 'ctrl-enter'
      },
      // 新增：聊天设置
      chat: {
        fontSize: 'medium', // 'small' | 'medium' | 'large' | 'xlarge'
        background: 'default', // 'default' | 'custom' | 'solid'
        bubbleStyle: 'default', // 'default' | 'compact' | 'loose'
        sendShortcut: 'enter', // 'enter' | 'ctrl-enter'
        recallTimeLimit: 2 // 消息撤回时限（分钟），默认2分钟
      },
      // 新增：文件管理
      file: {
        autoDownloadImage: true,
        autoDownloadFile: false,
        sizeWarning: true
      },
      // 新增：数据保留
      data: {
        keepOnLogout: true
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
    shortcutSettings: (state) => state.settings.shortcuts,

    // 聊天设置
    chatSettings: (state) => state.settings.chat,

    // 文件管理设置
    fileSettings: (state) => state.settings.file,

    // 数据保留设置
    dataSettings: (state) => state.settings.data
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
      setJSON('im-system-settings', state.settings)
    },

    // 加载本地设置
    LOAD_SETTINGS(state) {
      try {
        const local = getJSON('im-system-settings', null)
        if (local) {
          state.settings = { ...state.settings, ...local }
        }
      } catch (e) {
        console.warn('加载设置失败', e)
      }
    },

    // 从服务器加载设置并合并到本地
    MERGE_SERVER_SETTINGS(state, serverSettings) {
      // serverSettings 是键值对形式，如 { 'chat.fontSize': 'large' }
      const merged = { ...state.settings }

      for (const [key, value] of Object.entries(serverSettings)) {
        const parts = key.split('.')
        let current = merged

        for (let i = 0; i < parts.length - 1; i++) {
          if (!current[parts[i]]) {
            current[parts[i]] = {}
          }
          current = current[parts[i]]
        }

        current[parts[parts.length - 1]] = value
      }

      state.settings = merged
      localStorage.setItem('im-system-settings', JSON.stringify(merged))
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
    async initSettings({ commit, dispatch }) {
      commit('LOAD_SETTINGS')
      // 从服务器加载设置
      try {
        await dispatch('syncServerSettings')
      } catch (e) {
        console.warn('从服务器同步设置失败', e)
      }
    },

    // 从服务器同步设置
    async syncServerSettings({ commit }) {
      const { data } = await getUserSettingsMap()
      if (data && typeof data === 'object') {
        commit('MERGE_SERVER_SETTINGS', data)
      }
    },

    // 更新设置到服务器
    async updateServerSetting({ commit }, { key, value, type }) {
      try {
        await updateUserSetting({
          settingKey: key,
          settingValue: String(value),
          settingType: type
        })
      } catch (e) {
        console.error('更新设置到服务器失败', e)
        throw e
      }
    },

    // 批量更新设置到服务器（带防抖）
    batchUpdateServerSettings({ dispatch }, settings) {
      // 过滤掉 undefined 值，合并到待发送队列
      for (const [key, value] of Object.entries(settings)) {
        if (value !== undefined && value !== null) {
          pendingSettings[key] = String(value)
        }
      }

      // 清除之前的计时器
      if (settingsUpdateTimer) {
        clearTimeout(settingsUpdateTimer)
      }

      // 设置新的防抖计时器（500ms 后发送）
      settingsUpdateTimer = setTimeout(() => {
        flushSettingsUpdate(dispatch)
      }, 500)
    },

    // 新增：更新聊天设置
    updateChatSettings({ commit, dispatch }, settings) {
      commit('UPDATE_SETTINGS', { chat: { ...settings } })
      dispatch('batchUpdateServerSettings', {
        'chat.fontSize': settings.fontSize,
        'chat.background': settings.background,
        'chat.bubbleStyle': settings.bubbleStyle,
        'chat.sendShortcut': settings.sendShortcut
      })
    },

    // 新增：更新文件管理设置
    updateFileSettings({ commit, dispatch }, settings) {
      commit('UPDATE_SETTINGS', { file: { ...settings } })
      dispatch('batchUpdateServerSettings', {
        'file.autoDownloadImage': String(settings.autoDownloadImage),
        'file.autoDownloadFile': String(settings.autoDownloadFile),
        'file.sizeWarning': String(settings.sizeWarning)
      })
    },

    // 新增：更新数据保留设置
    updateDataSettings({ commit, dispatch }, settings) {
      commit('UPDATE_SETTINGS', { data: { ...settings } })
      dispatch('batchUpdateServerSettings', {
        'data.keepOnLogout': String(settings.keepOnLogout)
      })
    },

    // 更新通知设置
    updateNotificationSettings({ commit, dispatch }, settings) {
      commit('UPDATE_SETTINGS', { notifications: { ...settings } })
      dispatch('batchUpdateServerSettings', {
        'notifications.enabled': String(settings.enabled),
        'notifications.sound': String(settings.sound)
      })
    },

    // 更新隐私设置
    updatePrivacySettings({ commit, dispatch }, settings) {
      commit('UPDATE_SETTINGS', { privacy: { ...settings } })
      dispatch('batchUpdateServerSettings', {
        'privacy.showStatus': String(settings.showStatus),
        'privacy.readReceipt': String(settings.readReceipt)
      })
    },

    // 更新通用设置
    updateGeneralSettings({ commit, dispatch }, settings) {
      commit('UPDATE_SETTINGS', { general: { ...settings } })
      // 只发送有效的设置项到服务器
      const serverSettings = {}
      if (settings.language !== undefined) {
        serverSettings['general.language'] = settings.language
      }
      if (settings.theme !== undefined) {
        serverSettings['general.theme'] = settings.theme
      }
      if (Object.keys(serverSettings).length > 0) {
        dispatch('batchUpdateServerSettings', serverSettings).catch(() => {
          // 静默处理失败，本地状态已更新
        })
      }
    },

    // 更新快捷键设置
    updateShortcutSettings({ commit, dispatch }, settings) {
      commit('UPDATE_SETTINGS', { shortcuts: { ...settings } })
      dispatch('batchUpdateServerSettings', {
        'shortcuts.send': settings.send
      })
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
    logout({ commit, state }) {
      // 根据设置决定是否清除本地数据
      if (!state.settings.data?.keepOnLogout) {
        localStorage.removeItem('im-system-settings')
      }
      commit('CLEAR_ALL_STATE')
      commit('session/CLEAR_STATE', null, { root: true })
      commit('message/CLEAR_STATE', null, { root: true })
      commit('contact/CLEAR_STATE', null, { root: true })
    }
  }
}
