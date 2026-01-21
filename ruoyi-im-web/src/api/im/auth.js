import request from '../request'

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/api/im/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/api/im/auth/logout',
    method: 'post'
  })
}

/**
 * 刷新 Token
 * @returns {Promise}
 */
export function refreshToken() {
  return request({
    url: '/api/im/auth/refresh',
    method: 'post'
  })
}

/**
 * 验证 Token
 * @returns {Promise}
 */
export function validateToken() {
  return request({
    url: '/api/im/auth/validateToken',
    method: 'post'
  })
}
