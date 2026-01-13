/**
 * 全局错误拦截插件
 * 捕获Vue应用中的未处理错误和Promise拒绝
 */
import { handleNetworkError, handleApiError, handleWebSocketError, handleWebRTCError } from './errorHandler'

/**
 * Vue全局错误处理插件
 */
export const setupGlobalErrorHandler = (app) => {
  // Vue错误处理
  app.config.errorHandler = (err, instance, info) => {
    console.error('Vue Error:', err, info)
    handleApiError(err, { component: instance?.$options?.name, info })
  }

  // 全局未捕获的错误
  window.addEventListener('error', (event) => {
    if (event.error) {
      const error = event.error
      const context = {
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno,
      }

      // 根据错误类型选择处理器
      if (error.message?.includes('network') || error.message?.includes('fetch')) {
        handleNetworkError(error, context)
      } else if (error.message?.includes('WebSocket')) {
        handleWebSocketError(error, context)
      } else if (error.message?.includes('RTCPeerConnection') || error.message?.includes('getUserMedia')) {
        handleWebRTCError(error, context)
      } else {
        handleApiError(error, context)
      }
    }
  })

  // 未处理的Promise拒绝
  window.addEventListener('unhandledrejection', (event) => {
    console.error('Unhandled Promise Rejection:', event.reason)
    handleApiError(event.reason || new Error('Promise rejected without reason'), {
      type: 'unhandledrejection',
    })
  })

  // 资源加载错误
  window.addEventListener('error', (event) => {
    if (event.target !== window) {
      // 资源加载失败
      const target = event.target
      const src = target.src || target.href
      console.warn('Resource Load Error:', src)
    }
  }, true)

  // 页面卸载前警告
  window.addEventListener('beforeunload', (e) => {
    // 检查是否有未保存的数据或正在进行的通话
    const hasUnsavedData = checkUnsavedData()
    const hasActiveCall = checkActiveCall()

    if (hasUnsavedData || hasActiveCall) {
      e.preventDefault()
      e.returnValue = ''
    }
  })
}

/**
 * 检查是否有未保存的数据
 */
function checkUnsavedData() {
  // 检查本地存储中的编辑状态
  try {
    const editingStates = localStorage.getItem('im_editing_states')
    if (editingStates) {
      const states = JSON.parse(editingStates)
      return Object.values(states).some(state => state === true)
    }
  } catch (e) {
    return false
  }
  return false
}

/**
 * 检查是否有正在进行的通话
 */
function checkActiveCall() {
  try {
    const activeCall = localStorage.getItem('im_active_call')
    return activeCall !== null
  } catch (e) {
    return false
  }
}

/**
 * 网络状态监听
 */
export const setupNetworkListener = () => {
  // 监听在线/离线状态
  window.addEventListener('online', () => {
    console.log('网络已恢复')
  })

  window.addEventListener('offline', () => {
    console.warn('网络已断开')
    // 这里可以触发网络错误处理
    handleNetworkError(new Error('网络连接已断开'), { type: 'offline' })
  })
}

/**
 * API请求拦截器配置
 * @param {AxiosInstance} axiosInstance - axios实例
 */
export const setupApiInterceptors = (axiosInstance) => {
  // 请求拦截器
  axiosInstance.interceptors.request.use(
    (config) => {
      // 添加时间戳，防止缓存
      if (config.method === 'get') {
        config.params = {
          ...config.params,
          _t: Date.now(),
        }
      }
      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )

  // 响应拦截器
  axiosInstance.interceptors.response.use(
    (response) => {
      const { data } = response

      // 处理业务错误码
      if (data && data.code !== undefined && data.code !== 200) {
        const error = new Error(data.msg || data.message || '请求失败')
        error.code = data.code
        error.response = response

        // 特殊处理认证错误
        if (data.code === 401 || data.code === 403) {
          // 清除token并跳转登录
          localStorage.removeItem('token')
          localStorage.removeItem('Admin-Token')
          window.location.href = '/login'
        }

        return Promise.reject(error)
      }

      return response
    },
    (error) => {
      // 处理网络错误
      if (!error.response) {
        // 请求超时或网络断开
        if (error.code === 'ECONNABORTED') {
          handleNetworkError(new Error('请求超时，请检查网络连接'), {
            url: error.config?.url,
          })
        } else {
          handleNetworkError(new Error('网络连接失败'), {
            url: error.config?.url,
          })
        }
        return Promise.reject(error)
      }

      // 处理HTTP错误状态码
      const { status, data } = error.response
      const errorObj = new Error(data?.msg || data?.message || `HTTP ${status} Error`)
      errorObj.code = `ERR_HTTP_${status}`
      errorObj.response = error.response

      handleApiError(errorObj, {
        status,
        url: error.config?.url,
      })

      return Promise.reject(error)
    }
  )
}

export default {
  install: (app) => {
    setupGlobalErrorHandler(app)
    setupNetworkListener()
  },
}
