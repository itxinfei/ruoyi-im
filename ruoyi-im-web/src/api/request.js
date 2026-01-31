/**
 * Axios 基础配置
 * 用于统一管理 HTTP 请求
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, getUserInfo, clearAuth } from '@/utils/storage'

// 错误消息队列，防止重复弹窗
const errorQueue = new Set()
let lastErrorTime = 0
const ERROR_THROTTLE = 2000 // 2秒内相同错误不重复提示

/**
 * 判断是否应该显示错误消息（节流）
 */
function shouldShowError(key) {
  const now = Date.now()
  if (errorQueue.has(key) && now - lastErrorTime < ERROR_THROTTLE) {
    return false
  }
  errorQueue.add(key)
  lastErrorTime = now
  // 清理旧的错误key
  setTimeout(() => errorQueue.delete(key), ERROR_THROTTLE)
  return true
}

/**
 * 判断错误类型
 */
function getErrorType(error) {
  if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
    return 'TIMEOUT'
  }
  if (!error.response && error.message?.includes('Network Error')) {
    return 'NETWORK_ERROR'
  }
  if (error.response?.status === 502 || error.response?.status === 503) {
    return 'SERVER_DOWN'
  }
  return 'OTHER'
}

/**
 * 获取用户友好的错误消息
 */
function getErrorMessage(error) {
  const errorType = getErrorType(error)

  switch (errorType) {
    case 'TIMEOUT':
      return '请求超时，请检查网络连接'
    case 'NETWORK_ERROR':
      return '网络连接失败，请确认后端服务已启动'
    case 'SERVER_DOWN':
      return '服务暂时不可用，请稍后重试'
    default:
      if (error.response?.data?.msg) {
        return error.response.data.msg
      }
      if (error.response?.status) {
        const statusMessages = {
          400: '请求参数错误',
          401: '未授权，请重新登录',
          403: '拒绝访问',
          404: '请求的资源不存在',
          500: '服务器内部错误',
          502: '网关错误，服务未启动',
          503: '服务暂时不可用'
        }
        return statusMessages[error.response.status] || `请求失败: ${error.response.status}`
      }
      return error.message || '请求失败'
  }
}

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '', // 使用环境变量或空字符串
  timeout: 15000 // 请求超时时间
})

// 请求计数器，用于判断是否是初始加载
let requestCount = 0
const isInitialLoading = () => requestCount < 5 // 前5个请求视为初始加载

// 请求拦截器
service.interceptors.request.use(
  config => {
    requestCount++

    // 支持静默模式
    if (config.silent === true) {
      config._silent = true
    }

    // 从 localStorage 获取 token
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    // 从 localStorage 获取用户信息并发送 userId
    const userInfo = getUserInfo()
    if (userInfo?.id) {
      config.headers['userId'] = String(userInfo.id)
    }

    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果返回的状态码不是 200，则判断为错误
    if (res.code !== undefined && res.code !== 200) {
      const errorMessage = res.msg || '请求失败'

      // 401: 未授权 - 总是显示并清除 token
      if (res.code === 401) {
        ElMessage.error(errorMessage)
        clearAuth()
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
        return Promise.reject(new Error(errorMessage))
      }

      // 静默模式不显示错误
      if (!response.config._silent && shouldShowError(errorMessage)) {
        ElMessage.error(errorMessage)
      }

      return Promise.reject(new Error(errorMessage))
    }

    return res
  },
  error => {
    // 头像404错误静默处理，不显示错误弹窗
    if (error.response?.status === 404 && error.config?.url?.includes('/avatar/')) {
      return Promise.reject(error)
    }

    // 401 未授权 - 清除 token 并跳转
    if (error.response?.status === 401) {
      ElMessage.error('未授权，请重新登录')
      clearAuth()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(error)
    }

    // 获取错误消息
    const errorMessage = getErrorMessage(error)
    const errorKey = `${error.config?.url || 'request'}_${errorMessage}`

    // 静默模式或初始加载期间不显示网络错误
    const isSilent = error.config?._silent || error.config?.silent
    const shouldSuppress = isInitialLoading() && getErrorType(error) !== 'OTHER'

    if (!isSilent && !shouldSuppress && shouldShowError(errorKey)) {
      ElMessage.error(errorMessage)
    }

    return Promise.reject(error)
  }
)

export default service

/**
 * 带重试的请求方法
 * @param {Function} requestFn - 返回 Promise 的请求函数
 * @param {Object} options - 重试选项
 * @returns {Promise}
 */
export function requestWithRetry(requestFn, options = {}) {
  const {
    maxRetries = 3,
    retryDelay = 1000,
    backoffMultiplier = 2,
    retryCondition = null
  } = options

  return new Promise((resolve, reject) => {
    let attempt = 0

    const attemptRequest = () => {
      attempt++

      requestFn()
        .then(resolve)
        .catch(error => {
          // 检查是否应该重试
          const shouldRetry = attempt < maxRetries &&
            (retryCondition ? retryCondition(error, attempt) : isRetryableError(error))

          if (shouldRetry) {
            const delay = retryDelay * Math.pow(backoffMultiplier, attempt - 1)
            setTimeout(attemptRequest, delay)
          } else {
            reject(error)
          }
        })
    }

    attemptRequest()
  })
}

/**
 * 判断错误是否可重试
 */
function isRetryableError(error) {
  // 网络错误可重试
  if (!error.response && error.message?.includes('Network Error')) {
    return true
  }
  // 超时可重试
  if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
    return true
  }
  // 5xx 服务器错误可重试
  if (error.response?.status >= 500 && error.response?.status < 600) {
    return true
  }
  return false
}
