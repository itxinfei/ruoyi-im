import request from '../request'

/**
 * 获取用户信息
 * @param {string|number} userId - 可选,用户ID。不传则获取当前用户信息
 * @returns {Promise}
 */
export function getUserInfo(userId) {
  if (userId) {
    return request({
      url: `/api/im/user/${userId}`,
      method: 'get'
    })
  }
  return request({
    url: '/api/im/user/info',
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
    url: `/api/im/user/${userId}`,
    method: 'put',
    data
  })
}

/**
 * 搜索用户
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchUsers(keyword) {
  return request({
    url: '/api/im/user/search',
    method: 'get',
    params: {
      keyword
    }
  })
}

/**
 * 搜索用户（API 别名）
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchUsersApi(keyword) {
  return searchUsers(keyword)
}

/**
 * 获取用户详情
 * @param {string|number} userId - 用户ID
 * @returns {Promise}
 */
export function getUserDetail(userId) {
  return request({
    url: `/api/im/user/${userId}`,
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
    url: '/api/im/user/avatar',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取聊天背景设置
 * @param {string|number} userId - 用户ID，不传则获取当前用户
 * @returns {Promise}
 */
export function getChatBackground(userId) {
  return request({
    url: userId ? `/api/im/user/${userId}/background` : '/api/im/user/background',
    method: 'get'
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
    url: '/api/im/user/background',
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
    url: '/api/im/user/background',
    method: 'delete',
    params: conversationId ? { conversationId } : {}
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

/**
 * 扫描二维码
 * @param {string} qrData - 二维码内容
 * @returns {Promise}
 */
export function scanQRCode(qrData) {
  return request({
    url: '/api/im/user/scan-qrcode',
    method: 'post',
    data: { qrData }
  })
}
