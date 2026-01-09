/**
 * IM系统统一的用户信息获取工具
 * 解决多用户登录状态下用户ID获取不一致的问题
 */

const USER_INFO_KEY = 'userInfo'
const TOKEN_KEYS = ['Admin-Token', 'token', 'access_token']

/**
 * 获取当前登录用户的完整信息
 * @returns {Object|null} 用户信息对象
 */
export function getCurrentUserInfo() {
  try {
    const userInfoStr = localStorage.getItem(USER_INFO_KEY)
    if (userInfoStr) {
      const userInfo = JSON.parse(userInfoStr)
      return {
        id: userInfo.id || userInfo.userId,
        userId: userInfo.userId || userInfo.id,
        userName: userInfo.userName || userInfo.username,
        nickName: userInfo.nickName || userInfo.nickname,
        avatar: userInfo.avatar,
        email: userInfo.email,
        phonenumber: userInfo.phonenumber || userInfo.phone,
        ...userInfo,
      }
    }
  } catch (e) {
    console.error('获取用户信息失败:', e)
  }
  return null
}

/**
 * 获取当前用户ID
 * 优先级：userId > id > null
 * @returns {number|null} 用户ID
 */
export function getCurrentUserId() {
  const userInfo = getCurrentUserInfo()
  return userInfo ? Number(userInfo.userId) || Number(userInfo.id) : null
}

/**
 * 获取当前用户名称
 * @returns {string} 用户名称
 */
export function getCurrentUserName() {
  const userInfo = getCurrentUserInfo()
  return userInfo ? userInfo.nickName || userInfo.userName || '用户' : '用户'
}

/**
 * 获取当前用户头像
 * @returns {string} 用户头像URL
 */
export function getCurrentUserAvatar() {
  const userInfo = getCurrentUserInfo()
  return userInfo ? userInfo.avatar : ''
}

/**
 * 获取认证Token
 * @returns {string|null} Token字符串
 */
export function getToken() {
  for (const key of TOKEN_KEYS) {
    const token = localStorage.getItem(key)
    if (token) {
      return token
    }
  }
  return null
}

/**
 * 设置当前用户信息
 * @param {Object} userInfo 用户信息
 */
export function setUserInfo(userInfo) {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

/**
 * 设置Token
 * @param {string} token Token字符串
 */
export function setToken(token) {
  localStorage.setItem('Admin-Token', token)
  // 兼容其他可能的token key
  localStorage.setItem('token', token)
}

/**
 * 移除用户信息和Token（登出）
 */
export function clearUserInfo() {
  localStorage.removeItem(USER_INFO_KEY)
  TOKEN_KEYS.forEach(key => {
    localStorage.removeItem(key)
  })
}

/**
 * 检查用户是否已登录
 * @returns {boolean} 是否登录
 */
export function isLoggedIn() {
  return !!getCurrentUserId()
}

/**
 * 获取WebSocket连接URL
 * @param {string} baseUrl 基础URL
 * @returns {string} WebSocket URL
 */
export function getWebSocketUrl(baseUrl = null) {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = baseUrl || window.location.host
  return `${protocol}//${host}/ws/im`
}

export default {
  getCurrentUserInfo,
  getCurrentUserId,
  getCurrentUserName,
  getCurrentUserAvatar,
  getToken,
  setUserInfo,
  setToken,
  clearUserInfo,
  isLoggedIn,
  getWebSocketUrl,
}
