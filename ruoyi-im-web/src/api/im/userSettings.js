/**
 * 用户设置 API
 *
 * @author ruoyi
 */
import request from '../request'

/**
 * 获取用户所有设置
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
 */
export function getSettingsByType(settingType) {
  return request({
    url: `/api/im/user/settings/${settingType}`,
    method: 'get'
  })
}

/**
 * 更新单个设置
 * @param {Object} data - { settingKey, settingValue }
 */
export function updateSetting(data) {
  return request({
    url: '/api/im/user/settings',
    method: 'put',
    data
  })
}

/**
 * 批量更新设置
 * @param {Array} settings - [{ key, value }, ...]
 */
export function batchUpdateSettings(settings) {
  return request({
    url: '/api/im/user/settings/batch',
    method: 'put',
    data: { settings }
  })
}

/**
 * 删除设置
 * @param {string} settingKey - 设置键
 */
export function deleteSetting(settingKey) {
  return request({
    url: `/api/im/user/settings/${settingKey}`,
    method: 'delete'
  })
}

/**
 * 初始化默认设置
 */
export function initDefaultSettings() {
  return request({
    url: '/api/im/user/settings/init',
    method: 'post'
  })
}
