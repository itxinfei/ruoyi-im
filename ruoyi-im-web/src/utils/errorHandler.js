/**
 * 统一异常处理工具
 * 提供标准化的错误处理、提示和日志记录
 */
import { ElMessage, ElNotification } from 'element-plus'

// 错误类型枚举
export const ERROR_TYPE = {
  NETWORK: 'network', // 网络错误
  API: 'api', // API错误
  WEBSOCKET: 'websocket', // WebSocket错误
  WEBRTC: 'webrtc', // WebRTC错误
  STORAGE: 'storage', // 本地存储错误
  VALIDATION: 'validation', // 验证错误
  PERMISSION: 'permission', // 权限错误
  UNKNOWN: 'unknown', // 未知错误
}

// 错误级别枚举
export const ERROR_LEVEL = {
  INFO: 'info',
  WARNING: 'warning',
  ERROR: 'error',
  FATAL: 'fatal',
}

// 错误码映射
const ERROR_CODE_MAP = {
  // 网络错误
  ERR_NETWORK_001: '网络连接失败，请检查网络设置',
  ERR_NETWORK_002: '请求超时，请稍后重试',
  ERR_NETWORK_003: '服务器无响应',

  // API错误
  ERR_API_001: 'API调用失败',
  ERR_API_002: '数据解析失败',
  ERR_API_401: '未授权，请重新登录',
  ERR_API_403: '无权限访问',
  ERR_API_404: '请求的资源不存在',
  ERR_API_500: '服务器内部错误',

  // WebSocket错误
  ERR_WS_001: 'WebSocket连接失败',
  ERR_WS_002: 'WebSocket断开连接',
  ERR_WS_003: '消息发送失败',

  // WebRTC错误
  ERR_WEBRTC_001: '无法访问摄像头或麦克风',
  ERR_WEBRTC_002: '视频通话建立失败',
  ERR_WEBRTC_003: '屏幕共享失败',

  // 存储错误
  ERR_STORAGE_001: '本地存储已满',
  ERR_STORAGE_002: '本地存储访问被拒绝',

  // 权限错误
  ERR_PERM_001: '缺少必要的权限',
  ERR_PERM_002: '权限请求被拒绝',
}

/**
 * 错误处理器类
 */
class ErrorHandler {
  constructor() {
    this.errorLog = []
    this.maxLogSize = 100
    this.enableConsole = import.meta.env.DEV
  }

  /**
   * 处理错误
   * @param {Error|Object} error - 错误对象
   * @param {Object} options - 配置选项
   * @returns {string} 错误ID
   */
  handle(error, options = {}) {
    const {
      type = ERROR_TYPE.UNKNOWN,
      level = ERROR_LEVEL.ERROR,
      showMessage = true,
      logToServer = true,
      context = {},
    } = options

    // 标准化错误对象
    const normalizedError = this._normalizeError(error, type, context)

    // 记录错误
    this._logError(normalizedError)

    // 显示用户提示
    if (showMessage) {
      this._showErrorMessage(normalizedError)
    }

    // 上报到服务器（生产环境）
    if (logToServer && !import.meta.env.DEV) {
      this._reportToServer(normalizedError)
    }

    return normalizedError.id
  }

  /**
   * 处理网络错误
   */
  handleNetwork(error, context = {}) {
    return this.handle(error, {
      type: ERROR_TYPE.NETWORK,
      level: ERROR_LEVEL.ERROR,
      context,
    })
  }

  /**
   * 处理API错误
   */
  handleApi(error, context = {}) {
    return this.handle(error, {
      type: ERROR_TYPE.API,
      level: ERROR_LEVEL.WARNING,
      context,
    })
  }

  /**
   * 处理WebSocket错误
   */
  handleWebSocket(error, context = {}) {
    return this.handle(error, {
      type: ERROR_TYPE.WEBSOCKET,
      level: ERROR_LEVEL.WARNING,
      showMessage: false, // WebSocket错误由连接管理器处理
      context,
    })
  }

  /**
   * 处理WebRTC错误
   */
  handleWebRTC(error, context = {}) {
    return this.handle(error, {
      type: ERROR_TYPE.WEBRTC,
      level: ERROR_LEVEL.ERROR,
      context,
    })
  }

  /**
   * 处理存储错误
   */
  handleStorage(error, context = {}) {
    return this.handle(error, {
      type: ERROR_TYPE.STORAGE,
      level: ERROR_LEVEL.WARNING,
      showMessage: false,
      context,
    })
  }

  /**
   * 处理权限错误
   */
  handlePermission(error, context = {}) {
    return this.handle(error, {
      type: ERROR_TYPE.PERMISSION,
      level: ERROR_LEVEL.WARNING,
      context,
    })
  }

  /**
   * 标准化错误对象
   */
  _normalizeError(error, type, context) {
    const id = this._generateErrorId()
    const timestamp = new Date().toISOString()

    let message = '未知错误'
    let code = null
    let stack = null

    if (error instanceof Error) {
      message = error.message || message
      stack = error.stack
      // 尝试从错误消息中提取错误码
      code = this._extractErrorCode(message)
    } else if (typeof error === 'string') {
      message = error
    } else if (error?.message) {
      message = error.message
      code = error.code
    }

    // 使用错误码映射获取用户友好的消息
    const userMessage = code ? ERROR_CODE_MAP[code] : message

    return {
      id,
      type,
      message,
      userMessage,
      code,
      stack,
      timestamp,
      context,
    }
  }

  /**
   * 生成错误ID
   */
  _generateErrorId() {
    return `ERR_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  /**
   * 提取错误码
   */
  _extractErrorCode(message) {
    // 尝试从消息中匹配错误码
    const match = message.match(/ERR_\w+_\d+/)
    return match ? match[0] : null
  }

  /**
   * 记录错误
   */
  _logError(error) {
    // 控制台输出
    if (this.enableConsole) {
      const consoleMethod = error.type === ERROR_TYPE.NETWORK ? 'error' : 'warn'
      console[consoleMethod](`[${error.type.toUpperCase()}]`, error.message, error)
    }

    // 添加到内存日志
    this.errorLog.unshift(error)
    if (this.errorLog.length > this.maxLogSize) {
      this.errorLog = this.errorLog.slice(0, this.maxLogSize)
    }

    // 保存到本地存储（最近10条）
    try {
      const recentErrors = this.errorLog.slice(0, 10).map(e => ({
        id: e.id,
        type: e.type,
        message: e.message,
        timestamp: e.timestamp,
      }))
      localStorage.setItem('im_recent_errors', JSON.stringify(recentErrors))
    } catch (e) {
      // 忽略存储错误
    }
  }

  /**
   * 显示错误消息
   */
  _showErrorMessage(error) {
    const { type, userMessage, level } = error

    // 根据错误类型和级别选择显示方式
    if (
      level === ERROR_LEVEL.FATAL ||
      type === ERROR_TYPE.NETWORK ||
      type === ERROR_TYPE.PERMISSION
    ) {
      ElNotification({
        title: '操作失败',
        message: userMessage,
        type: 'error',
        duration: 5000,
      })
    } else {
      ElMessage({
        message: userMessage,
        type: 'warning',
        duration: 3000,
      })
    }
  }

  /**
   * 上报错误到服务器
   */
  _reportToServer(error) {
    // 防止上报过程中的错误造成无限循环
    try {
      // 使用 sendBeacon 确保数据能发送出去
      const data = JSON.stringify({
        id: error.id,
        type: error.type,
        message: error.message,
        code: error.code,
        timestamp: error.timestamp,
        context: error.context,
        userAgent: navigator.userAgent,
        url: window.location.href,
      })

      if (navigator.sendBeacon) {
        navigator.sendBeacon('/api/im/error/report', data)
      } else {
        fetch('/api/im/error/report', {
          method: 'POST',
          body: data,
          keepalive: true,
        }).catch(() => {})
      }
    } catch (e) {
      // 忽略上报错误
    }
  }

  /**
   * 获取最近的错误日志
   */
  getRecentErrors(limit = 10) {
    return this.errorLog.slice(0, limit)
  }

  /**
   * 清空错误日志
   */
  clearErrorLog() {
    this.errorLog = []
    localStorage.removeItem('im_recent_errors')
  }
}

// 创建单例
const errorHandler = new ErrorHandler()

// 导出便捷方法
export const handleError = (error, options) => errorHandler.handle(error, options)
export const handleNetworkError = (error, context) => errorHandler.handleNetwork(error, context)
export const handleApiError = (error, context) => errorHandler.handleApi(error, context)
export const handleWebSocketError = (error, context) => errorHandler.handleWebSocket(error, context)
export const handleWebRTCError = (error, context) => errorHandler.handleWebRTC(error, context)
export const handleStorageError = (error, context) => errorHandler.handleStorage(error, context)
export const handlePermissionError = (error, context) =>
  errorHandler.handlePermission(error, context)

export default errorHandler
