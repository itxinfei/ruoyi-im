/**
 * 统一错误处理工具
 *
 * 职责：
 * - 统一处理 API 错误、网络错误、业务错误
 * - 集成日志记录
 * - 提供用户友好的错误提示
 * - 支持静默错误处理
 *
 * 使用方式：
 * ```js
 * import { handleApiError, handleSilentError } from '@/utils/errorHandler'
 *
 * // API 错误处理（显示提示）
 * try {
 *   await someApi()
 * } catch (error) {
 *   handleApiError(error, '发送消息')
 * }
 *
 * // 静默错误处理（仅记录日志）
 * catch (error) {
 *   handleSilentError(error, '头像加载')
 * }
 * ```
 */

import { ElMessage } from 'element-plus'
import logger from './logger'

/**
 * 错误类型枚举
 */
export const ErrorType = {
  NETWORK: 'NETWORK',           // 网络错误
  API: 'API',                   // API 错误
  VALIDATION: 'VALIDATION',     // 参数校验错误
  PERMISSION: 'PERMISSION',     // 权限错误
  BUSINESS: 'BUSINESS',         // 业务错误
  UNKNOWN: 'UNKNOWN'            // 未知错误
}

/**
 * HTTP 状态码对应的错误消息
 */
const HttpStatusMessages = {
  400: '请求参数错误',
  401: '登录已过期，请重新登录',
  403: '没有权限执行此操作',
  404: '请求的资源不存在',
  409: '数据冲突，请刷新后重试',
  429: '请求过于频繁，请稍后再试',
  500: '服务器内部错误',
  502: '网关错误，请稍后重试',
  503: '服务暂时不可用',
  504: '请求超时'
}

/**
 * 默认错误消息
 */
const DefaultMessages = {
  [ErrorType.NETWORK]: '网络连接失败，请检查网络',
  [ErrorType.API]: '请求失败，请稍后重试',
  [ErrorType.VALIDATION]: '数据验证失败',
  [ErrorType.PERMISSION]: '权限不足',
  [ErrorType.BUSINESS]: '操作失败',
  [ErrorType.UNKNOWN]: '发生未知错误'
}

/**
 * 需要静默处理的 URL 模式
 */
const SilentPatterns = [
  '/avatar/',           // 头像 404
  '/thumb/',           // 缩略图 404
  '/health',           // 健康检查
  '/ping'              // 心跳检测
]

/**
 * 判断是否为静默错误
 * @param {Error} error - 错误对象
 * @returns {boolean}
 */
const isSilentError = (error) => {
  // 检查 URL 是否匹配静默模式
  const url = error.config?.url || error.response?.config?.url || ''
  return SilentPatterns.some(pattern => url.includes(pattern))
}

/**
 * 判断错误类型
 * @param {Error} error - 错误对象
 * @returns {string} 错误类型
 */
const getErrorType = (error) => {
  if (!error) return ErrorType.UNKNOWN

  // 网络错误（无响应）
  if (!error.response && error.request) {
    return ErrorType.NETWORK
  }

  // HTTP 错误
  if (error.response) {
    const status = error.response.status
    if (status === 401 || status === 403) {
      return ErrorType.PERMISSION
    }
    if (status === 400) {
      return ErrorType.VALIDATION
    }
    return ErrorType.API
  }

  // 业务错误（带错误码）
  if (error.code || error.errCode) {
    return ErrorType.BUSINESS
  }

  return ErrorType.UNKNOWN
}

/**
 * 获取错误消息
 * @param {Error} error - 错误对象
 * @param {string} context - 操作上下文
 * @param {string} errorType - 错误类型
 * @returns {string} 用户友好的错误消息
 */
const getErrorMessage = (error, context, errorType) => {
  // 从响应中获取服务端返回的消息
  const responseMsg = error.response?.data?.msg || error.response?.data?.message
  if (responseMsg) {
    return responseMsg
  }

  // 从错误对象中获取消息
  if (error.msg || error.message) {
    return error.msg || error.message
  }

  // HTTP 状态码消息
  const status = error.response?.status
  if (status && HttpStatusMessages[status]) {
    return HttpStatusMessages[status]
  }

  // 默认消息
  const defaultMsg = DefaultMessages[errorType] || DefaultMessages[ErrorType.UNKNOWN]
  return context ? `${context}：${defaultMsg}` : defaultMsg
}

/**
 * 处理 401 未授权错误
 * 清除 token 并跳转登录页
 */
const handleUnauthorized = () => {
  localStorage.removeItem('im_token')
  localStorage.removeItem('im_user_info')

  if (window.location.pathname !== '/login') {
    // 延迟跳转，避免打断用户操作
    setTimeout(() => {
      window.location.href = '/login'
    }, 1000)
  }
}

/**
 * 记录错误日志
 * @param {Error} error - 错误对象
 * @param {string} context - 操作上下文
 * @param {string} errorType - 错误类型
 */
const logError = (error, context, errorType) => {
  const logData = {
    context,
    type: errorType,
    message: error.message || error.msg,
    status: error.response?.status,
    url: error.config?.url || error.response?.config?.url,
    stack: error.stack
  }

  logger.error('ErrorHandler', logData)
}

/**
 * 统一 API 错误处理（显示用户提示）
 * @param {Error} error - 错误对象
 * @param {string} context - 操作上下文（如"发送消息"、"上传文件"）
 * @param {Object} options - 可选配置
 * @returns {void}
 */
export const handleApiError = (error, context = '', options = {}) => {
  const {
    silent = false,           // 是否静默处理
    showMessage = true,       // 是否显示消息提示
    onError = null            // 自定义错误回调
  } = options

  // 检查是否为静默错误
  if (silent || isSilentError(error)) {
    logError(error, context, getErrorType(error))
    return
  }

  const errorType = getErrorType(error)
  const message = getErrorMessage(error, context, errorType)

  // 记录日志
  logError(error, context, errorType)

  // 显示用户提示
  if (showMessage) {
    ElMessage.error(message)
  }

  // 处理 401 未授权
  if (error.response?.status === 401) {
    handleUnauthorized()
  }

  // 执行自定义回调
  if (onError) {
    onError(error, errorType, message)
  }
}

/**
 * 静默错误处理（仅记录日志，不显示提示）
 * @param {Error} error - 错误对象
 * @param {string} context - 操作上下文
 * @returns {void}
 */
export const handleSilentError = (error, context = '') => {
  handleApiError(error, context, { silent: true, showMessage: false })
}

/**
 * 业务错误处理（用于自定义业务逻辑错误）
 * @param {string} message - 错误消息
 * @param {Object} options - 可选配置
 * @returns {void}
 */
export const handleBusinessError = (message, options = {}) => {
  const {
    code = null,
    showMessage = true,
    onError = null
  } = options

  const error = new Error(message)
  error.code = code
  error.isBusiness = true

  logger.error('BusinessError', { message, code })

  if (showMessage) {
    ElMessage.error(message)
  }

  if (onError) {
    onError(error, ErrorType.BUSINESS, message)
  }
}

/**
 * 验证错误处理（用于表单验证等）
 * @param {Array<string>} errors - 错误消息数组
 * @returns {void}
 */
export const handleValidationErrors = (errors) => {
  if (!errors || errors.length === 0) return

  const message = errors.join('；')
  ElMessage.error(message)

  logger.error('ValidationError', { errors })
}

/**
 * 异步操作包装器（自动处理错误）
 * @param {Function} asyncFn - 异步函数
 * @param {Object} options - 可选配置
 * @returns {Promise}
 *
 * @example
 * const result = await withErrorHandling(
 *   () => api.sendMessage(data),
 *   { context: '发送消息' }
 * )
 */
export const withErrorHandling = async (asyncFn, options = {}) => {
  const {
    context = '',
    silent = false,
    onError = null
  } = options

  try {
    return await asyncFn()
  } catch (error) {
    handleApiError(error, context, { silent, onError })
    throw error
  }
}

/**
 * 创建带错误处理的 API 函数
 * @param {Function} apiFn - API 函数
 * @param {string} context - 操作上下文
 * @returns {Function} 包装后的 API 函数
 *
 * @example
 * const safeSendMessage = createSafeApi(sendMessageApi, '发送消息')
 * const result = await safeSendMessage(params)
 */
export const createSafeApi = (apiFn, context = '') => {
  return async (...args) => {
    try {
      return await apiFn(...args)
    } catch (error) {
      handleApiError(error, context)
      throw error
    }
  }
}

/**
 * 错误重试装饰器
 * @param {Function} asyncFn - 异步函数
 * @param {Object} options - 配置
 * @returns {Function}
 *
 * @example
 * const retryableFetch = withRetry(fetchData, { maxRetries: 3 })
 */
export const withRetry = (asyncFn, options = {}) => {
  const {
    maxRetries = 3,
    retryDelay = 1000,
    context = '',
    retryCondition = null  // 自定义重试条件 (error) => boolean
  } = options

  return async (...args) => {
    let lastError

    for (let i = 0; i < maxRetries; i++) {
      try {
        return await asyncFn(...args)
      } catch (error) {
        lastError = error

        // 检查是否应该重试
        const shouldRetry = retryCondition
          ? retryCondition(error)
          : error.response?.status >= 500 || error.message === 'Network Error'

        if (!shouldRetry || i === maxRetries - 1) {
          handleApiError(error, context)
          throw error
        }

        // 等待后重试
        await new Promise(resolve => setTimeout(resolve, retryDelay * (i + 1)))
      }
    }

    throw lastError
  }
}

// 导出错误类型和工具
export default {
  handleApiError,
  handleSilentError,
  handleBusinessError,
  handleValidationErrors,
  withErrorHandling,
  createSafeApi,
  withRetry,
  ErrorType
}
