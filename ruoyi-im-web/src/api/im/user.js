import request from '../request'

/**
 * 获取用户信息
 * @param {string|number} userId - 可选,用户ID。不传则获取当前用户信息
 * @returns {Promise}
 */
export function getUserInfo(userId) {
  if (userId) {
    return request({
      url: `/api/im/users/${userId}`,
      method: 'get'
    })
  }
  return request({
    url: '/api/im/users/me',
    method: 'get'
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise}
 */
export function getCurrentUser() {
  return request({
    url: '/api/im/users/me',
    method: 'get'
  })
}

/**
 * 获取用户详情
 * @param {string|number} userId - 用户ID
 * @returns {Promise}
 */
export function getUserDetail(userId) {
  return request({
    url: `/api/im/users/${userId}`,
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {string|number} userId - 用户ID
 * @param {Object} data - 用户数据
 * @returns {Promise}
 */
export function updateUser(userId, data) {
  return request({
    url: `/api/im/users/${userId}`,
    method: 'put',
    data
  })
}

/**
 * 修改用户状态
 * @param {string|number} userId - 用户ID
 * @param {Object} data - 状态数据
 * @param {string} data.status - 状态: ENABLED/DISABLED
 * @returns {Promise}
 */
export function changeUserStatus(userId, data) {
  return request({
    url: `/api/im/users/${userId}/status`,
    method: 'put',
    data
  })
}

/**
 * 搜索用户（已废弃）
 * @deprecated 请使用全局搜索接口 /api/im/search/contacts
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchUsers(keyword) {
  return request({
    url: '/api/im/users/search',
    method: 'get',
    params: {
      keyword
    }
  })
}

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @param {string} params.keyword - 搜索关键词
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @returns {Promise}
 */
export function getUserList(params) {
  return request({
    url: '/api/im/users',
    method: 'get',
    params
  })
}

/**
 * 获取所有用户列表
 * @param {string} keyword - 可选，搜索关键词
 * @returns {Promise}
 */
export function getAllUsers(keyword) {
  return request({
    url: '/api/im/users',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 批量获取用户信息
 * @param {Array<number>|string} ids - 用户ID列表或逗号分隔的字符串
 * @returns {Promise}
 */
export function getBatchUsers(ids) {
  const idsStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({
    url: '/api/im/users/batch',
    method: 'get',
    params: { ids: idsStr }
  })
}

/**
 * 获取用户的好友列表
 * @param {string|number} userId - 用户ID
 * @returns {Promise}
 */
export function getUserFriends(userId) {
  return request({
    url: `/api/im/users/${userId}/friends`,
    method: 'get'
  })
}

/**
 * 上传用户头像
 * @param {FormData} data - 包含 avatarfile 的 FormData
 * @returns {Promise}
 */
export function uploadAvatar(data) {
  return request({
    url: '/api/im/users/avatar',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取聊天背景设置
 * @param {string|number} conversationId - 可选，会话ID
 * @returns {Promise}
 */
export function getChatBackground(conversationId) {
  return request({
    url: '/api/im/users/me/background',
    method: 'get',
    params: conversationId ? { conversationId } : {}
  })
}

/**
 * 设置聊天背景
 * @param {Object} data - 背景设置
 * @param {string} data.type - 背景类型: color(纯色), image(图片), default(默认)
 * @param {string} data.value - 背景值（颜色值或图片URL）
 * @param {string|number} data.conversationId - 可选，指定会话ID
 * @returns {Promise}
 */
export function setChatBackground(data) {
  return request({
    url: '/api/im/users/me/background',
    method: 'put',
    data
  })
}

/**
 * 清除聊天背景
 * @param {string|number} conversationId - 可选，指定会话ID
 * @returns {Promise}
 */
export function clearChatBackground(conversationId) {
  return request({
    url: '/api/im/users/me/background',
    method: 'delete',
    params: conversationId ? { conversationId } : {}
  })
}

/**
 * 扫描二维码
 * @param {string} qrData - 二维码内容
 * @returns {Promise}
 */
export function scanQRCode(qrData) {
  return request({
    url: '/api/im/users/scan-qrcode',
    method: 'post',
    data: { qrData }
  })
}

/**
 * 短信验证码登录
 * @param {Object} data - 登录数据
 * @param {string} data.phone - 手机号
 * @param {string} data.code - 验证码
 * @returns {Promise}
 */
export function smsLogin(data) {
  return request({
    url: '/api/im/auth/sms-login',
    method: 'post',
    data
  })
}

/**
 * 发送短信验证码
 * @param {Object} data - 请求参数
 * @param {string} data.phone - 手机号
 * @param {string} data.type - 验证码类型: login, register, reset
 * @returns {Promise}
 */
export function sendSmsCode(data) {
  return request({
    url: '/api/im/auth/sms-code',
    method: 'post',
    data
  })
}