/**
 * 本地存储统一管理
 * 封装 localStorage 操作，统一 Key 命名和处理逻辑
 */

// ========== Storage Key 常量 ==========
export const StorageKeys = {
  // 认证相关
  TOKEN: 'im_token',
  ACCESS_TOKEN: 'access_token',
  USER_INFO: 'im_user_info',
  USER_ROLE: 'im_user_role',

  // 设置相关
  THEME: 'im_theme',
  SYSTEM_SETTINGS: 'im-system-settings',
  LANGUAGE: 'im_language',

  // 免打扰设置
  DND_START: 'im_dnd_start',
  DND_END: 'im_dnd_end',

  // 搜索历史
  SEARCH_HISTORY: 'im_search_history',
  CONTACTS_SEARCH_HISTORY: 'contacts_search_history',

  // 草稿相关
  DRAFTS: 'im_drafts',

  // 会话分组
  CONVERSATION_GROUPS: 'im_conversation_groups',
  CONVERSATION_GROUP_MAP: 'im_conversation_group_map',

  // 其他
  AI_REPLY_STYLE: 'ai_reply_style_config',
  REMEMBERED_USERNAME: 'remembered_username',
  EMOJI_RECENT: 'im_emoji_recent'
}

// ========== 基础操作 ==========

/**
 * 获取存储项
 * @param {string} key - 存储 Key
 * @param {any} defaultValue - 默认值
 * @returns {any}
 */
export function getItem(key, defaultValue = null) {
  try {
    const value = localStorage.getItem(key)
    if (value === null) {return defaultValue}
    return value
  } catch (e) {
    console.warn(`获取 localStorage 失败: ${key}`, e)
    return defaultValue
  }
}

/**
 * 设置存储项
 * @param {string} key - 存储 Key
 * @param {any} value - 存储值
 */
export function setItem(key, value) {
  try {
    localStorage.setItem(key, value)
  } catch (e) {
    console.warn(`设置 localStorage 失败: ${key}`, e)
  }
}

/**
 * 移除存储项
 * @param {string} key - 存储 Key
 */
export function removeItem(key) {
  try {
    localStorage.removeItem(key)
  } catch (e) {
    console.warn(`移除 localStorage 失败: ${key}`, e)
  }
}

/**
 * 清空所有存储
 */
export function clear() {
  try {
    localStorage.clear()
  } catch (e) {
    console.warn('清空 localStorage 失败', e)
  }
}

// ========== JSON 数据操作 ==========

/**
 * 获取 JSON 对象
 * @param {string} key - 存储 Key
 * @param {any} defaultValue - 默认值
 * @returns {Object}
 */
export function getJSON(key, defaultValue = {}) {
  try {
    const value = localStorage.getItem(key)
    if (value === null) {return defaultValue}
    return JSON.parse(value)
  } catch (e) {
    console.warn(`解析 JSON 失败: ${key}`, e)
    return defaultValue
  }
}

/**
 * 设置 JSON 对象
 * @param {string} key - 存储 Key
 * @param {Object} value - 对象值
 */
export function setJSON(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch (e) {
    console.warn(`存储 JSON 失败: ${key}`, e)
  }
}

// ========== 认证相关便捷方法 ==========

/**
 * 获取 Token
 * @returns {string}
 */
export function getToken() {
  return getItem(StorageKeys.TOKEN) || getItem(StorageKeys.ACCESS_TOKEN) || ''
}

/**
 * 设置 Token
 * @param {string} token
 */
export function setToken(token) {
  setItem(StorageKeys.TOKEN, token)
  setItem(StorageKeys.ACCESS_TOKEN, token)
}

/**
 * 清除 Token
 */
export function clearToken() {
  removeItem(StorageKeys.TOKEN)
  removeItem(StorageKeys.ACCESS_TOKEN)
}

/**
 * 获取用户信息
 * @returns {Object}
 */
export function getUserInfo() {
  return getJSON(StorageKeys.USER_INFO, {})
}

/**
 * 设置用户信息
 * @param {Object} userInfo
 */
export function setUserInfo(userInfo) {
  setJSON(StorageKeys.USER_INFO, userInfo)
}

/**
 * 获取存储的用户信息（别名方法，与 getUserInfo 相同）
 * @returns {Object}
 */
export function getStoredUserInfo() {
  return getUserInfo()
}

/**
 * 获取用户角色
 * @returns {string}
 */
export function getUserRole() {
  return getItem(StorageKeys.USER_ROLE, 'USER')
}

/**
 * 设置用户角色
 * @param {string} role
 */
export function setUserRole(role) {
  setItem(StorageKeys.USER_ROLE, role)
}

/**
 * 清除所有认证信息
 */
export function clearAuth() {
  clearToken()
  removeItem(StorageKeys.USER_INFO)
  removeItem(StorageKeys.USER_ROLE)
}

// ========== 设置相关便捷方法 ==========

/**
 * 获取主题设置
 * @returns {string}
 */
export function getTheme() {
  return getItem(StorageKeys.THEME, 'auto')
}

/**
 * 设置主题
 * @param {string} theme
 */
export function setTheme(theme) {
  setItem(StorageKeys.THEME, theme)
}

/**
 * 获取系统设置
 * @returns {Object}
 */
export function getSystemSettings() {
  return getJSON(StorageKeys.SYSTEM_SETTINGS, {})
}

/**
 * 设置系统设置
 * @param {Object} settings
 */
export function setSystemSettings(settings) {
  setJSON(StorageKeys.SYSTEM_SETTINGS, settings)
}

/**
 * 获取免打扰时段
 * @returns {Object} { start: number, end: number }
 */
export function getDoNotDisturb() {
  return {
    start: parseInt(getItem(StorageKeys.DND_START, '22')),
    end: parseInt(getItem(StorageKeys.DND_END, '8'))
  }
}

/**
 * 设置免打扰时段
 * @param {number} start - 开始小时 (0-23)
 * @param {number} end - 结束小时 (0-23)
 */
export function setDoNotDisturb(start, end) {
  setItem(StorageKeys.DND_START, String(start))
  setItem(StorageKeys.DND_END, String(end))
}

// ========== 搜索历史相关 ==========

/**
 * 获取搜索历史
 * @returns {Array}
 */
export function getSearchHistory() {
  return getJSON(StorageKeys.SEARCH_HISTORY, [])
}

/**
 * 添加搜索历史
 * @param {string} keyword
 * @param {number} maxLength - 最大保留数量
 */
export function addSearchHistory(keyword, maxLength = 10) {
  if (!keyword || !keyword.trim()) {return}
  const history = getSearchHistory()
  const filtered = history.filter(item => item !== keyword)
  filtered.unshift(keyword)
  const trimmed = filtered.slice(0, maxLength)
  setJSON(StorageKeys.SEARCH_HISTORY, trimmed)
}

/**
 * 清空搜索历史
 */
export function clearSearchHistory() {
  removeItem(StorageKeys.SEARCH_HISTORY)
}

// ========== 草稿相关 ==========

/**
 * 获取草稿
 * @param {string} conversationId - 会话 ID
 * @returns {string}
 */
export function getDraft(conversationId) {
  const drafts = getJSON(StorageKeys.DRAFTS, {})
  return drafts[conversationId] || ''
}

/**
 * 设置草稿
 * @param {string} conversationId - 会话 ID
 * @param {string} content - 草稿内容
 */
export function setDraft(conversationId, content) {
  const drafts = getJSON(StorageKeys.DRAFTS, {})
  if (content) {
    drafts[conversationId] = content
  } else {
    delete drafts[conversationId]
  }
  setJSON(StorageKeys.DRAFTS, drafts)
}

/**
 * 清除指定会话草稿
 * @param {string} conversationId - 会话 ID
 */
export function clearDraft(conversationId) {
  setDraft(conversationId, '')
}

// ========== 会话分组相关 ==========

/**
 * 获取会话分组
 * @returns {Object}
 */
export function getConversationGroups() {
  return getJSON(StorageKeys.CONVERSATION_GROUPS, {})
}

/**
 * 设置会话分组
 * @param {Object} groups
 */
export function setConversationGroups(groups) {
  setJSON(StorageKeys.CONVERSATION_GROUPS, groups)
}

/**
 * 获取会话分组映射
 * @returns {Object}
 */
export function getConversationGroupMap() {
  return getJSON(StorageKeys.CONVERSATION_GROUP_MAP, {})
}

/**
 * 设置会话分组映射
 * @param {Object} map
 */
export function setConversationGroupMap(map) {
  setJSON(StorageKeys.CONVERSATION_GROUP_MAP, map)
}

// ========== 其他便捷方法 ==========

/**
 * 获取记住的用户名
 * @returns {string}
 */
export function getRememberedUsername() {
  return getItem(StorageKeys.REMEMBERED_USERNAME, '')
}

/**
 * 设置记住的用户名
 * @param {string} username
 */
export function setRememberedUsername(username) {
  setItem(StorageKeys.REMEMBERED_USERNAME, username)
}

/**
 * 获取最近使用的表情
 * @returns {Array}
 */
export function getRecentEmoji() {
  return getJSON(StorageKeys.EMOJI_RECENT, [])
}

/**
 * 添加最近使用的表情
 * @param {string} emoji
 * @param {number} maxLength - 最大保留数量
 */
export function addRecentEmoji(emoji, maxLength = 20) {
  const recent = getRecentEmoji()
  const filtered = recent.filter(e => e !== emoji)
  filtered.unshift(emoji)
  const trimmed = filtered.slice(0, maxLength)
  setJSON(StorageKeys.EMOJI_RECENT, trimmed)
}

/**
 * 获取 AI 回复风格配置
 * @returns {Object}
 */
export function getAiReplyStyle() {
  return getJSON(StorageKeys.AI_REPLY_STYLE, {})
}

/**
 * 设置 AI 回复风格配置
 * @param {Object} config
 */
export function setAiReplyStyle(config) {
  setJSON(StorageKeys.AI_REPLY_STYLE, config)
}

// 导出所有 Key 常量
export { StorageKeys as default }
