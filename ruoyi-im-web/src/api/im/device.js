/**
 * 设备相关 API
 * 对应后端 ImDeviceController
 * @author ruoyi
 */
import request from '../request'

/**
 * 获取设备列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @returns {Promise}
 */
export function getDevices(params) {
  return request({
    url: '/api/im/devices',
    method: 'get',
    params
  })
}

/**
 * 获取当前设备信息
 * @returns {Promise}
 */
export function getCurrentDevice() {
  return request({
    url: '/api/im/devices/current',
    method: 'get'
  })
}

/**
 * 注册设备
 * @param {Object} data - 设备数据
 * @param {string} data.deviceId - 设备ID
 * @param {string} data.deviceName - 设备名称
 * @param {string} data.deviceType - 设备类型 (WEB/MOBILE/DESKTOP)
 * @param {string} data.os - 操作系统
 * @param {string} data.browser - 浏览器信息
 * @returns {Promise}
 */
export function registerDevice(data) {
  return request({
    url: '/api/im/devices/register',
    method: 'post',
    data
  })
}

/**
 * 更新设备信息
 * @param {number} deviceId - 设备ID
 * @param {Object} data - 设备数据
 * @returns {Promise}
 */
export function updateDevice(deviceId, data) {
  return request({
    url: `/api/im/devices/${deviceId}`,
    method: 'put',
    data
  })
}

/**
 * 删除设备
 * @param {number} deviceId - 设备ID
 * @returns {Promise}
 */
export function deleteDevice(deviceId) {
  return request({
    url: `/api/im/devices/${deviceId}`,
    method: 'delete'
  })
}

/**
 * 设备登录/激活
 * @param {Object} data - 登录数据
 * @param {string} data.deviceId - 设备ID
 * @param {string} data.token - 验证token
 * @returns {Promise}
 */
export function activateDevice(data) {
  return request({
    url: '/api/im/devices/activate',
    method: 'post',
    data
  })
}

/**
 * 设备登出
 * @param {Object} data - 登出数据
 * @param {string} data.deviceId - 设备ID
 * @returns {Promise}
 */
export function logoutDevice(data) {
  return request({
    url: '/api/im/devices/logout',
    method: 'post',
    data
  })
}

/**
 * 获取设备在线状态
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function getDeviceStatus(userId) {
  return request({
    url: `/api/im/devices/status/${userId}`,
    method: 'get'
  })
}

/**
 * 同步设备数据
 * @param {Object} data - 同步数据
 * @param {string} data.deviceId - 设备ID
 * @param {string} data.syncType - 同步类型
 * @param {Object} data.syncData - 同步内容
 * @returns {Promise}
 */
export function syncDeviceData(data) {
  return request({
    url: '/api/im/devices/sync',
    method: 'post',
    data
  })
}

export default {
  getDevices,
  getCurrentDevice,
  registerDevice,
  updateDevice,
  deleteDevice,
  activateDevice,
  logoutDevice,
  getDeviceStatus,
  syncDeviceData
}