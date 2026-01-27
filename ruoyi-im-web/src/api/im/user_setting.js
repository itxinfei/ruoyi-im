/**
 * 用户设置相关 API
 */
import request from '../request'

/**
 * 获取用户所有设置
 * @returns {Promise}
 */
export function getUserSettings() {
  return request({
    url: '/api/im/user/settings',
    method: 'get'
  })
}

/**
 * 按类型获取用户设置
 * @param {string} settingType - 设置类型（CHAT, NOTIFICATION, PRIVACY, FILE, DATA, GENERAL）
 * @returns {Promise}
 */
export function getUserSettingsByType(settingType) {
  return request({
    url: `/api/im/user/settings/type/${settingType}`,
    method: 'get'
  })
}

/**
 * 获取用户设置键值对映射
 * @returns {Promise}
 */
export function getUserSettingsMap() {
  return request({
    url: '/api/im/user/settings/map',
    method: 'get'
  })
}

/**
 * 更新用户单个设置
 * @param {Object} data - { settingKey: string, settingValue: string, settingType?: string }
 * @returns {Promise}
 */
export function updateUserSetting(data) {
  return request({
    url: '/api/im/user/settings',
    method: 'put',
    data
  })
}

/**
 * 批量更新用户设置
 * @param {Object} settings - 键值对形式的设置对象
 * @returns {Promise}
 */
export function batchUpdateUserSettings(settings) {
  return request({
    url: '/api/im/user/settings/batch',
    method: 'put',
    data: { settings }
  })
}

/**
 * 删除用户单个设置
 * @param {string} settingKey - 设置键名
 * @returns {Promise}
 */
export function deleteUserSetting(settingKey) {
  return request({
    url: `/api/im/user/settings/${settingKey}`,
    method: 'delete'
  })
}
