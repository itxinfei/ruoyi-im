/**
 * 钉钉5.6风格头像颜色生成工具
 * 根据用户名/昵称生成固定的背景颜色
 */

// 钉钉5.6风格的预定义颜色列表（按实际钉钉配色）
const DINGTALK_COLORS = [
  { bg: '#F5222D', text: '#FFFFFF' }, // 红色
  { bg: '#FA8C16', text: '#FFFFFF' }, // 橙色
  { bg: '#FAAD14', text: '#FFFFFF' }, // 金色
  { bg: '#13C2C2', text: '#FFFFFF' }, // 青色
  { bg: '#52C41A', text: '#FFFFFF' }, // 绿色
  { bg: '#0089FF', text: '#FFFFFF' }, // 钉钉蓝
  { bg: '#2F54EB', text: '#FFFFFF' }, // 靛蓝
  { bg: '#722ED1', text: '#FFFFFF' }, // 紫色
  { bg: '#EB2F96', text: '#FFFFFF' }, // 粉色
  { bg: '#1890FF', text: '#FFFFFF' }, // 亮蓝（备用）
]

/**
 * 根据字符串生成唯一索引
 * @param {string} str - 输入字符串（用户名/昵称）
 * @returns {number} 颜色索引
 */
function getStringIndex(str) {
  if (!str) return 0

  let hash = 0
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash // 转换为32位整数
  }

  return Math.abs(hash) % DINGTALK_COLORS.length
}

/**
 * 获取用户头像颜色配置
 * @param {string} username - 用户名
 * @param {string} nickname - 昵称（优先使用）
 * @returns {Object} { bg: 背景色, text: 文字色 }
 */
export function getAvatarColor(username, nickname = '') {
  const key = nickname || username || ''
  const index = getStringIndex(key)
  return DINGTALK_COLORS[index]
}

/**
 * 获取用户头像显示的字符
 * @param {string} username - 用户名
 * @param {string} nickname - 昵称（优先使用）
 * @returns {string} 显示的字符
 */
export function getAvatarText(username, nickname = '') {
  const name = nickname || username || ''
  if (!name) return '?'

  // 中文名取最后一个字
  if (/[\u4e00-\u9fa5]/.test(name)) {
    return name.charAt(name.length - 1)
  }

  // 英文名取首字母（大写）
  return name.charAt(0).toUpperCase()
}

/**
 * 生成用户头像的完整配置
 * @param {Object} user - 用户对象 { username, nickname, avatar }
 * @returns {Object} { color, text, hasAvatar }
 */
export function getUserAvatarConfig(user) {
  const { username, nickname, avatar } = user || {}

  return {
    color: getAvatarColor(username, nickname),
    text: getAvatarText(username, nickname),
    hasAvatar: !!avatar,
  }
}

/**
 * 为用户列表批量添加头像颜色配置
 * @param {Array} users - 用户列表
 * @returns {Array} 添加了color属性的用户列表
 */
export function enrichUsersWithAvatarColor(users) {
  if (!Array.isArray(users)) return []

  return users.map(user => ({
    ...user,
    _avatarColor: getAvatarColor(user.username, user.nickname),
    _avatarText: getAvatarText(user.username, user.nickname),
  }))
}

export default {
  getAvatarColor,
  getAvatarText,
  getUserAvatarConfig,
  enrichUsersWithAvatarColor,
}
