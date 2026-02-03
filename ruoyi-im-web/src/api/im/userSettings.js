/**
 * 用户设置 API
 *
 * @author ruoyi
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
 * 获取指定类型的设置
 * @param {string} settingType - 设置类型：general/notification/security/storage
 * @returns {Promise}
 */
export function getSettingsByType(settingType) {
  return request({
    url: `/api/im/user/settings/${settingType}`,
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
 * 更新单个设置
 * @param {Object} data - { settingKey, settingValue }
 * @returns {Promise}
 */
export function updateSetting(data) {
  return request({
    url: '/api/im/user/settings',
    method: 'put',
    data
  })
}

/**
 * 更新用户单个设置（别名）
 * @param {Object} data - { settingKey, settingValue }
 * @returns {Promise}
 */
export function updateUserSetting(data) {
  return updateSetting(data)
}

/**
 * 批量更新设置
 * @param {Array} settings - [{ key, value }, ...]
 * @returns {Promise}
 */
export function batchUpdateSettings(settings) {
  return request({
    url: '/api/im/user/settings/batch',
    method: 'put',
    data: { settings }
  })
}

/**
 * 批量更新用户设置（别名）
 * @param {Object} settings - 键值对形式的设置对象
 * @returns {Promise}
 */
export function batchUpdateUserSettings(settings) {
  return batchUpdateSettings(settings)
}

/**
 * 删除设置
 * @param {string} settingKey - 设置键
 * @returns {Promise}
 */
export function deleteSetting(settingKey) {
  return request({
    url: `/api/im/user/settings/${settingKey}`,
    method: 'delete'
  })
}

/**
 * 删除用户单个设置（别名）
 * @param {string} settingKey - 设置键名
 * @returns {Promise}
 */
export function deleteUserSetting(settingKey) {
  return deleteSetting(settingKey)
}

/**
 * 初始化默认设置
 * @returns {Promise}
 */
export function initDefaultSettings() {
  return request({
    url: '/api/im/user/settings/init',
    method: 'post'
  })
}
