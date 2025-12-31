import axios from 'axios'
import { ElMessage } from 'element-plus'
import envConfig from '../../env.config.js'

const pendingRequests = new Map()

function generateRequestKey(config) {
  const { method, url, params, data } = config
  return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&')
}

function addPendingRequest(config) {
  const requestKey = generateRequestKey(config)
  config.cancelToken =
    config.cancelToken ||
    new axios.CancelToken(cancel => {
      if (!pendingRequests.has(requestKey)) {
        pendingRequests.set(requestKey, cancel)
      }
    })
}

function removePendingRequest(config) {
  const requestKey = generateRequestKey(config)
  if (pendingRequests.has(requestKey)) {
    const cancel = pendingRequests.get(requestKey)
    cancel(requestKey)
    pendingRequests.delete(requestKey)
  }
}

const service = axios.create({
  baseURL: envConfig.baseAPI,
  timeout: 10000,
  withCredentials: true, // 携带cookie，支持Session认证
})

service.interceptors.request.use(
  config => {
    removePendingRequest(config)
    addPendingRequest(config)

    // 如果有token，添加到请求头（兼容JWT方式）
    const token = localStorage.getItem('token')
    if (token && config.headers.isToken !== false) {
      config.headers['Authorization'] = 'Bearer ' + token
    }

    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    removePendingRequest(response.config)
    const res = response.data

    // 兼容不同的成功状态码：200（新API）或 0（RuoYi原生）
    if (res.code === 200 || res.code === 0) {
      return res
    }

    // 处理错误
    ElMessage({
      message: res.msg || res.message || 'Error',
      type: 'error',
      duration: 5 * 1000,
    })

    // 未登录或登录过期
    if (res.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
    }

    return Promise.reject(new Error(res.msg || res.message || 'Error'))
  },
  error => {
    removePendingRequest(error.config || {})

    if (axios.isCancel(error)) {
      return Promise.reject(error)
    }

    console.error('Response error:', error)

    // 处理网络错误
    let message = error.message || 'Request failed'
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未登录或登录已过期'
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
          break
        case 403:
          message = '没有权限访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器错误'
          break
        default:
          message = error.response.data?.msg || error.message
      }
    }

    ElMessage({
      message: message,
      type: 'error',
      duration: 5 * 1000,
    })
    return Promise.reject(error)
  }
)

export default service
