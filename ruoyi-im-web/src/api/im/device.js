/**
 * 设备管理相关 API
 */
import request from '../request'

/**
 * 注册设备
 * WebSocket连接成功后调用
 * @param {Object} data - 设备数据
 * @param {string} data.deviceId - 设备ID（客户端生成的UUID）
 * @param {string} data.deviceType - 设备类型（web/ios/android/pc/mac/mini）
 * @param {string} data.deviceName - 设备名称
 * @param {string} data.clientVersion - 客户端版本
 * @param {string} data.osVersion - 操作系统版本
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
 * 设备心跳
 * 定期调用以保持在线状态
 * @param {Object} data - 心跳数据
 * @param {string} data.deviceId - 设备ID
 * @returns {Promise}
 */
export function sendHeartbeat(data) {
  return request({
    url: '/api/im/devices/heartbeat',
    method: 'post',
    data
  })
}

/**
 * 获取用户的所有设备
 * @returns {Promise}
 */
export function getDeviceList() {
  return request({
    url: '/api/im/devices/list',
    method: 'get'
  })
}

/**
 * 获取用户的在线设备
 * @returns {Promise}
 */
export function getOnlineDevices() {
  return request({
    url: '/api/im/devices/online',
    method: 'get'
  })
}

/**
 * 移除设备（退出登录）
 * @param {string} deviceId - 设备ID
 * @returns {Promise}
 */
export function removeDevice(deviceId) {
  return request({
    url: `/api/im/devices/${deviceId}`,
    method: 'delete'
  })
}

/**
 * 获取设备统计信息
 * @returns {Promise}
 */
export function getDeviceStats() {
  return request({
    url: '/api/im/devices/stats',
    method: 'get'
  })
}

// ==================== 设备管理工具函数 ====================

/**
 * 生成设备ID
 * @returns {string} 设备ID
 */
export function generateDeviceId() {
  return `${getDeviceType()}_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`
}

/**
 * 获取当前设备类型
 * @returns {string} 设备类型
 */
export function getDeviceType() {
  const ua = navigator.userAgent

  if (/micromessenger/i.test(ua)) {
    return 'mini'
  } else if (/iphone|ipad|ipod/i.test(ua)) {
    return 'ios'
  } else if (/android/i.test(ua)) {
    return 'android'
  } else if (/win/i.test(navigator.platform)) {
    return 'pc'
  } else if (/mac/i.test(navigator.platform)) {
    return 'mac'
  }
  return 'web'
}

/**
 * 获取设备名称
 * @returns {string} 设备名称
 */
export function getDeviceName() {
  const ua = navigator.userAgent
  const browser = getBrowserInfo()
  const os = getOSInfo()

  if (os === 'iOS') {
    return 'iOS设备'
  } else if (os === 'Android') {
    return 'Android设备'
  } else if (os === 'Windows') {
    return 'Windows PC'
  } else if (os === 'Mac') {
    return 'Mac电脑'
  }

  return `${browser} 浏览器`
}

/**
 * 获取浏览器信息
 * @returns {string} 浏览器名称
 */
function getBrowserInfo() {
  const ua = navigator.userAgent

  if (/chrome/i.test(ua) && !/edge|opr|brave/i.test(ua)) {
    return 'Chrome'
  } else if (/safari/i.test(ua) && !/chrome/i.test(ua)) {
    return 'Safari'
  } else if (/firefox/i.test(ua)) {
    return 'Firefox'
  } else if (/edge/i.test(ua)) {
    return 'Edge'
  } else if (/opera|opr/i.test(ua)) {
    return 'Opera'
  }

  return 'Unknown'
}

/**
 * 获取操作系统信息
 * @returns {string} 操作系统名称
 */
function getOSInfo() {
  const ua = navigator.userAgent

  if (/iphone|ipad|ipod/i.test(ua)) {
    return 'iOS'
  } else if (/android/i.test(ua)) {
    return 'Android'
  } else if (/win/i.test(navigator.platform)) {
    return 'Windows'
  } else if (/mac/i.test(navigator.platform)) {
    return 'Mac'
  } else if (/linux/i.test(navigator.platform)) {
    return 'Linux'
  }

  return 'Unknown'
}

/**
 * 获取客户端版本
 * @returns {string} 版本号
 */
export function getClientVersion() {
  // 可以从package.json或环境变量读取
  return '1.0.0'
}

/**
 * 心跳管理器
 */
class HeartbeatManager {
  constructor(options = {}) {
    this.interval = options.interval || 30000 // 默认30秒
    this.deviceId = options.deviceId
    this.timer = null
    this.isRunning = false
  }

  /**
   * 启动心跳
   */
  start() {
    if (this.isRunning) {
      return
    }

    this.isRunning = true
    this.sendHeartbeat()
    this.timer = setInterval(() => {
      this.sendHeartbeat()
    }, this.interval)

    console.log('心跳已启动:', this.deviceId)
  }

  /**
   * 停止心跳
   */
  stop() {
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
    this.isRunning = false
    console.log('心跳已停止')
  }

  /**
   * 发送心跳
   */
  async sendHeartbeat() {
    if (!this.deviceId) {
      return
    }

    try {
      await sendHeartbeat({ deviceId: this.deviceId })
    } catch (error) {
      console.warn('心跳发送失败:', error)
    }
  }
}

/**
 * 创建心跳管理器
 * @param {Object} options - 配置选项
 * @returns {HeartbeatManager}
 */
export function createHeartbeatManager(options) {
  return new HeartbeatManager(options)
}

/**
 * 默认导出的心跳管理器实例
 */
export const heartbeatManager = new HeartbeatManager()
