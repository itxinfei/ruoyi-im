import request from '../request'

/**
 * 获取通知设置
 * @returns {Promise}
 */
export function getNotificationSettings() {
  return request({
    url: '/api/im/config/notification',
    method: 'get'
  })
}

/**
 * 更新通知设置
 * @param {Object} settings - 通知设置
 * @returns {Promise}
 */
export function updateNotificationSettings(settings) {
  return request({
    url: '/api/im/config/notification',
    method: 'put',
    data: settings
  })
}

/**
 * 获取隐私设置
 * @returns {Promise}
 */
export function getPrivacySettings() {
  return request({
    url: '/api/im/config/privacy',
    method: 'get'
  })
}

/**
 * 更新隐私设置
 * @param {Object} settings - 隐私设置
 * @returns {Promise}
 */
export function updatePrivacySettings(settings) {
  return request({
    url: '/api/im/config/privacy',
    method: 'put',
    data: settings
  })
}

/**
 * 获取通用设置
 * @returns {Promise}
 */
export function getGeneralSettings() {
  return request({
    url: '/api/im/config',
    method: 'get'
  })
}

/**
 * 更新通用设置
 * @param {Object} settings - 通用设置
 * @returns {Promise}
 */
export function updateGeneralSettings(settings) {
  return request({
    url: '/api/im/config',
    method: 'put',
    data: settings
  })
}

/**
 * 获取黑名单
 * @returns {Promise}
 */
export function getBlockedUsers() {
  return request({
    url: '/api/im/config/blocked',
    method: 'get'
  })
}

/**
 * 拉黑用户
 * @param {number} targetUserId - 目标用户ID
 * @returns {Promise}
 */
export function blockUser(targetUserId) {
  return request({
    url: `/api/im/config/blocked/${targetUserId}`,
    method: 'post'
  })
}

/**
 * 解除拉黑
 * @param {number} targetUserId - 目标用户ID
 * @returns {Promise}
 */
export function unblockUser(targetUserId) {
  return request({
    url: `/api/im/config/blocked/${targetUserId}`,
    method: 'delete'
  })
}
