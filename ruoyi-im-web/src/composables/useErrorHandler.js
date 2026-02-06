/**
 * 错误处理 Composable
 * 提供统一的错误处理能力给 Vue 组件
 *
 * @module composables/useErrorHandler
 *
 * @example
 * const { handleError, withErrorHandling, retry } = useErrorHandler('MessageList')
 */

import { ref } from 'vue'
import {
  handleApiError,
  handleSilentError,
  handleBusinessError,
  withErrorHandling as withErrorHandlingUtil,
  withRetry,
  ErrorType
} from '@/utils/errorHandler'

/**
 * 错误处理 Hook
 * @param {String} context - 错误上下文（组件名或功能模块名）
 * @returns {Object} 错误处理相关的状态和方法
 */
export function useErrorHandler (context = 'Unknown') {
  const currentError = ref(null)
  const errorCount = ref(0)

  /**
   * 处理错误
   * @param {Error} error - 错误对象
   * @param {Object} options - 选项
   */
  const handleError = (error, options = {}) => {
    currentError.value = error
    errorCount.value++

    const defaultOptions = {
      context,
      ...options
    }

    if (options.silent) {
      handleSilentError(error, context)
    } else {
      handleApiError(error, context, defaultOptions)
    }
  }

  /**
   * 包装异步函数，自动处理错误
   * @param {Function} asyncFn - 异步函数
   * @param {Object} options - 选项
   * @returns {Promise}
   */
  const withErrorHandling = async (asyncFn, options = {}) => {
    return withErrorHandlingUtil(asyncFn, { context, ...options })
  }

  /**
   * 创建带重试的异步函数
   * @param {Function} asyncFn - 异步函数
   * @param {Object} options - 重试选项
   * @returns {Function}
   */
  const createRetryable = (asyncFn, options = {}) => {
    const defaultOptions = {
      context,
      maxRetries: 3,
      retryDelay: 1000,
      ...options
    }
    return withRetry(asyncFn, defaultOptions)
  }

  /**
   * 清除错误状态
   */
  const clearError = () => {
    currentError.value = null
  }

  /**
   * 重置错误计数
   */
  const resetErrorCount = () => {
    errorCount.value = 0
  }

  /**
   * 检查是否有错误
   * @returns {Boolean}
   */
  const hasError = () => {
    return currentError.value !== null
  }

  return {
    // 状态
    currentError,
    errorCount,
    // 方法
    handleError,
    handleSilentError: (error) => handleSilentError(error, context),
    handleBusinessError: (message, options) => handleBusinessError(message, { ...options, context }),
    withErrorHandling,
    createRetryable,
    clearError,
    resetErrorCount,
    hasError
  }
}

/**
 * 消息相关错误处理器
 * 专门处理消息发送、接收等场景的错误
 */
export function useMessageErrorHandler () {
  const base = useErrorHandler('Message')

  /**
   * 处理消息发送错误
   */
  const handleSendError = (error, message) => {
    base.handleError(error, {
      context: '发送消息',
      onError: (err, type) => {
        // 特殊处理：网络错误时可以加入重试队列
        if (type === ErrorType.NETWORK) {
          // TODO: 添加到重试队列
          console.log('消息加入重试队列:', message)
        }
      }
    })
  }

  /**
   * 处理媒体加载错误
   */
  const handleMediaError = (error, mediaType) => {
    base.handleSilentError(error, `${mediaType}加载`)
  }

  /**
   * 处理文件上传错误
   */
  const handleUploadError = (error, file) => {
    const message = error.response?.data?.msg || '文件上传失败'
    base.handleBusinessError(message, {
      code: 'ERR_UPLOAD',
      onError: () => {
        // 可以在这里处理上传失败的文件
        console.log('上传失败:', file.name)
      }
    })
  }

  return {
    ...base,
    handleSendError,
    handleMediaError,
    handleUploadError
  }
}

/**
 * 网络请求错误处理器
 * 专门处理 API 请求场景的错误
 */
export function useApiErrorHandler () {
  const base = useErrorHandler('API')

  /**
   * 包装 API 请求
   */
  const withApiErrorHandling = async (apiCall, options = {}) => {
    try {
      return await apiCall()
    } catch (error) {
      base.handleError(error, options)
      throw error
    }
  }

  /**
   * 带重试的 API 请求
   */
  const retryableApi = (apiCall, options = {}) => {
    return base.createRetryable(apiCall, {
      retryCondition: (error) => {
        // 仅对网络错误和 5xx 错误重试
        const status = error.response?.status
        return !status || status >= 500
      },
      ...options
    })
  }

  return {
    ...base,
    withApiErrorHandling,
    retryableApi
  }
}

export default useErrorHandler
