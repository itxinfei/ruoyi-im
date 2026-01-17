const state = {
  // 基本设置
  settings: {
    basic: {
      theme: 'light',
      language: 'zh-CN',
      fontSize: 'medium',
      messageAlignment: 'left',
    },
    // 通知设置
    notification: {
      enableNotification: true,
      desktopNotification: true,
      soundNotification: true,
      messageSound: 'default',
      callSound: 'ringtone',
      notifyWhenMentioned: true,
      muteGroupNotification: false,
      enableQuietHours: false,
      quietHoursStart: null,
      quietHoursEnd: null,
    },
    // 隐私设置
    privacy: {
      onlineStatusVisible: 'all',
      lastSeenVisible: 'all',
      profilePhotoVisible: 'all',
      messagePermission: 'all',
      enableReadReceipts: true,
      enableTypingIndicator: true,
    },
  },
}

const mutations = {
  UPDATE_BASIC_SETTINGS: (state, settings) => {
    state.settings.basic = {
      ...state.settings.basic,
      ...settings,
    }
  },
  UPDATE_NOTIFICATION_SETTINGS: (state, settings) => {
    state.settings.notification = {
      ...state.settings.notification,
      ...settings,
    }
  },
  UPDATE_PRIVACY_SETTINGS: (state, settings) => {
    state.settings.privacy = {
      ...state.settings.privacy,
      ...settings,
    }
  },
}

const actions = {
  // 更新基本设置
  updateBasicSettings({ commit }, settings) {
    commit('UPDATE_BASIC_SETTINGS', settings)
  },

  // 更新通知设置
  updateNotificationSettings({ commit }, settings) {
    commit('UPDATE_NOTIFICATION_SETTINGS', settings)
  },

  // 更新隐私设置
  updatePrivacySettings({ commit }, settings) {
    commit('UPDATE_PRIVACY_SETTINGS', settings)
  },

  // 更新主题
  setTheme({ commit }, theme) {
    commit('UPDATE_BASIC_SETTINGS', { theme })
  },

  // 更新语言
  setLanguage({ commit }, language) {
    commit('UPDATE_BASIC_SETTINGS', { language })
  },
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
}
