/**
 * HTTP 请求工具
 * 基于 axios 封装，提供统一的请求拦截、错误处理、重复请求取消等功能
 * @author RuoYi-IM
 */
import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import envConfig from '../../env.config.js'
import { getCurrentUserId } from '@/utils/im-user'

// ==================== 配置常量 ====================
const CONFIG = {
  BASE_URL: envConfig.baseAPI,
  TIMEOUT: 30000, // 请求超时时间（毫秒）
  RETRY_COUNT: 2, // 失败重试次数
  RETRY_DELAY: 1000, // 重试延迟（毫秒）
  ERROR_MSG_DURATION: 3000, // 错误提示显示时间（毫秒）
}

// ==================== 重复请求管理 ====================
const pendingRequests = new Map()

/**
 * 生成请求唯一标识
 * @param {Object} config - 请求配置
 * @returns {String} 请求唯一标识
 */
function generateRequestKey(config) {
  const { method, url, params, data } = config
  // 排除不参与去重的请求
  if (config.skipDuplicateCheck) {
    return `${Date.now()}_${Math.random()}`
  }
  return [method, url, JSON.stringify(params || {}), JSON.stringify(data || {})].join('&')
}

/**
 * 添加待处理请求
 * @param {Object} config - 请求配置
 */
function addPendingRequest(config) {
  const requestKey = generateRequestKey(config)
  if (!pendingRequests.has(requestKey)) {
    config.cancelToken = config.cancelToken || new axios.CancelToken(cancel => {
      pendingRequests.set(requestKey, cancel)
    })
  }
}

/**
 * 移除待处理请求
 * @param {Object} config - 请求配置
 */
function removePendingRequest(config) {
  const requestKey = generateRequestKey(config)
  if (pendingRequests.has(requestKey)) {
    const cancel = pendingRequests.get(requestKey)
    cancel(requestKey)
    pendingRequests.delete(requestKey)
  }
}

/**
 * 清空所有待处理请求（用于登出时）
 */
export function clearPendingRequests() {
  pendingRequests.forEach((cancel, key) => {
    cancel('取消请求')
  })
  pendingRequests.clear()
}

// ==================== 错误处理 ====================

/**
 * 错误消息映射表
 */
const ERROR_MESSAGES = {
  400: '请求参数错误',
  401: '未登录或登录已过期',
  403: '没有权限访问',
  404: '请求的资源不存在',
  405: '请求方法不允许',
  408: '请求超时',
  409: '请求冲突',
  410: '资源已被删除',
  422: '验证失败',
  429: '请求过于频繁，请稍后再试',
  500: '服务器错误',
  502: '网关错误',
  503: '服务不可用',
  504: '网关超时',
  NETWORK_ERROR: '网络连接失败，请检查网络设置',
  TIMEOUT: '请求超时，请稍后重试',
  ABORTED: '请求已取消',
}

/**
 * 显示错误消息
 * @param {String} message - 错误消息
 * @param {String} type - 消息类型
 */
function showError(message, type = 'error') {
  // 避免重复显示相同错误
  const lastError = localStorage.getItem('last_error_msg')
  const lastErrorTime = parseInt(localStorage.getItem('last_error_time') || '0')
  const now = Date.now()

  if (message === lastError && now - lastErrorTime < 2000) {
    return // 2秒内不重复显示相同错误
  }

  localStorage.setItem('last_error_msg', message)
  localStorage.setItem('last_error_time', now.toString())

  ElMessage({
    message,
    type,
    duration: CONFIG.ERROR_MSG_DURATION,
    showClose: true,
  })
}

/**
 * 处理业务错误
 * @param {Number} code - 错误码
 * @param {String} msg - 错误消息
 */
function handleBusinessError(code, msg) {
  const message = msg || ERROR_MESSAGES[code] || '请求失败'

  // 401 未登录
  if (code === 401) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('Admin-Token')
    ElNotification({
      title: '登录已过期',
      message: '请重新登录',
      type: 'warning',
      duration: 3000,
    })
    setTimeout(() => {
      window.location.href = '/login'
    }, 1000)
    return
  }

  showError(message)
}

/**
 * 处理HTTP错误
 * @param {Object} error - 错误对象
 * @returns {String} 错误消息
 */
function handleHttpError(error) {
  if (!error.response) {
    // 网络错误或请求取消
    if (error.code === 'ECONNABORTED') {
      return ERROR_MESSAGES.TIMEOUT
    }
    if (axios.isCancel(error)) {
      return ERROR_MESSAGES.ABORTED
    }
    return ERROR_MESSAGES.NETWORK_ERROR
  }

  const { status, data } = error.response
  const message = data?.msg || data?.message || ERROR_MESSAGES[status]

  // 401 未登录
  if (status === 401) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('Admin-Token')
    ElNotification({
      title: '登录已过期',
      message: '请重新登录',
      type: 'warning',
      duration: 3000,
    })
    setTimeout(() => {
      window.location.href = '/login'
    }, 1000)
    return message
  }

  return message
}

// ==================== 重试机制 ====================

/**
 * 延迟函数
 * @param {Number} ms - 延迟时间（毫秒）
 */
function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 判断是否应该重试请求
 * @param {Object} error - 错误对象
 * @param {Number} retryCount - 当前重试次数
 * @returns {Boolean} 是否应该重试
 */
function shouldRetry(error, retryCount) {
  if (retryCount >= CONFIG.RETRY_COUNT) {
    return false
  }

  // 网络错误或超时可以重试
  if (!error.response) {
    return true
  }

  // 5xx 服务器错误可以重试
  const status = error.response.status
  return status >= 500 && status < 600
}

// ==================== 创建 axios 实例 ====================
const service = axios.create({
  baseURL: CONFIG.BASE_URL,
  timeout: CONFIG.TIMEOUT,
  withCredentials: true, // 携带cookie，支持Session认证
})

// ==================== 请求拦截器 ====================
service.interceptors.request.use(
  config => {
    // 移除并添加新的待处理请求
    removePendingRequest(config)
    addPendingRequest(config)

    // 添加 token（兼容JWT方式）
    const token = localStorage.getItem('token') || localStorage.getItem('Admin-Token')
    if (token && config.headers?.isToken !== false) {
      config.headers = config.headers || {}
      config.headers['Authorization'] = 'Bearer ' + token
    }

    // 添加 userId 请求头（后端 IM API 需要）
    const userId = getCurrentUserId()
    if (userId) {
      config.headers = config.headers || {}
      config.headers['userId'] = userId.toString()
    }

    // 设置默认Content-Type
    if (!config.headers?.['Content-Type'] && config.method === 'post') {
      config.headers = config.headers || {}
      config.headers['Content-Type'] = 'application/json;charset=UTF-8'
    }

    // 记录请求开始时间（用于计算请求耗时）
    config.metadata = { startTime: Date.now() }

    // 开发环境打印请求信息
    if (import.meta.env.DEV && !config.skipLog) {
      console.log(`[HTTP Request] ${config.method?.toUpperCase()} ${config.url}`, config.params || config.data)
    }

    return config
  },
  error => {
    console.error('[HTTP Request Error]', error)
    return Promise.reject(error)
  }
)

// ==================== 响应拦截器 ====================
service.interceptors.response.use(
  response => {
    removePendingRequest(response.config)

    // 开发环境打印响应信息
    if (import.meta.env.DEV && !response.config.skipLog) {
      const duration = Date.now() - (response.config.metadata?.startTime || 0)
      console.log(`[HTTP Response] ${response.config.url} - ${duration}ms`)
    }

    const res = response.data

    // 兼容不同的成功状态码：200（新API）或 0（RuoYi原生）
    if (res.code === 200 || res.code === 0) {
      return res
    }

    // 处理业务错误
    handleBusinessError(res.code, res.msg || res.message)

    return Promise.reject(new Error(res.msg || res.message || '请求失败'))
  },
  async error => {
    removePendingRequest(error.config || {})

    // 请求被取消，不显示错误
    if (axios.isCancel(error)) {
      return Promise.reject(error)
    }

    const config = error.config || {}
    const retryCount = config.__retryCount || 0

    // 尝试重试
    if (shouldRetry(error, retryCount)) {
      config.__retryCount = retryCount + 1

      // 开发环境打印重试信息
      if (import.meta.env.DEV) {
        console.log(`[HTTP Retry] ${config.url} - 第 ${config.__retryCount} 次重试`)
      }

      // 延迟后重试
      await delay(CONFIG.RETRY_DELAY * config.__retryCount)

      return service(config)
    }

    // 处理HTTP错误
    const message = handleHttpError(error)

    // 显示错误消息（非取消的请求）
    if (!axios.isCancel(error)) {
      showError(message)
    }

    return Promise.reject(error)
  }
)

// ==================== 导出 ====================
export default service

// 导出常用方法
export const request = service
export const setBaseURL = (url) => { service.defaults.baseURL = url }
export const getBaseURL = () => service.defaults.baseURL
export const setTimeout = (ms) => { service.defaults.timeout = ms }
export const getTimeout = () => service.defaults.timeout
