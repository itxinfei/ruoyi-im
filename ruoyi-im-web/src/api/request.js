/**
 * Axios 基础配置
 * 用于统一管理 HTTP 请求
 * 包含 CSRF 防护机制，防止跨站请求伪造攻击
 * 包含自动刷新 token 机制
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import tokenManager from '@/utils/tokenManager'
import csrfManager from '@/utils/csrfManager'
import { refreshToken } from '@/api/im/auth'

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '', // 使用环境变量或空字符串
  timeout: 15000 // 请求超时时间
})

// 401 错误时的刷新锁，防止并发刷新
let isTokenRefreshing = false
let refreshSubscribers = []

/**
 * 订阅刷新成功事件
 * @param {Function} callback - 刷新成功后的回调
 */
function subscribeTokenRefresh(callback) {
  refreshSubscribers.push(callback)
}

/**
 * 刷新成功后执行所有订阅的回调
 * @param {string} newToken - 新的 token
 */
function onRefreshed(newToken) {
  refreshSubscribers.forEach(callback => callback(newToken))
  refreshSubscribers = []
}

/**
 * 刷新 token
 * @returns {Promise<string>} 返回新的 token
 */
async function handleTokenRefresh() {
  try {
    const res = await refreshToken()
    if (res.code === 200 && res.data) {
      const newToken = res.data.token || res.data
      tokenManager.setToken(newToken)
      onRefreshed(newToken)
      return newToken
    } else {
      throw new Error(res.msg || '刷新 token 失败')
    }
  } catch (error) {
    console.error('刷新 token 失败:', error)
    tokenManager.clearAll()
    csrfManager.clearToken()
    window.location.href = '/login'
    throw error
  }
}

// 请求拦截器
service.interceptors.request.use(
  async config => {
    // 从 tokenManager 获取 token
    let token = tokenManager.getToken()

    // 如果 token 即将过期，自动刷新
    if (token && tokenManager.shouldRefreshToken() && !config.skipTokenRefresh) {
      try {
        console.debug('Token 即将过期，自动刷新中...')
        token = await tokenManager.autoRefreshToken(refreshToken)
        if (!token) {
          console.warn('Token 刷新失败，取消请求')
          return Promise.reject(new Error('Token 刷新失败'))
        }
      } catch (error) {
        console.error('Token 自动刷新失败:', error)
        return Promise.reject(error)
      }
    }

    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    // 从 tokenManager 获取用户信息并发送 userId
    const userInfo = tokenManager.getUserInfo()
    if (userInfo && userInfo.id) {
      config.headers['userId'] = String(userInfo.id)
    }

    /**
     * CSRF 防护机制
     * 对所有非 GET 请求（POST、PUT、DELETE、PATCH）添加 CSRF token
     * 跳过标记为 skipCsrfToken 的请求（如获取 CSRF token 本身）
     */
    const method = (config.method || '').toUpperCase()
    const isModificationRequest = ['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)

    if (isModificationRequest && !config.skipCsrfToken) {
      try {
        const csrfToken = await csrfManager.getToken()
        if (csrfToken) {
          // 将 CSRF token 添加到请求头
          config.headers['X-CSRF-Token'] = csrfToken
        } else {
          console.warn('CSRF token 为空，请求可能被拒绝')
        }
      } catch (error) {
        console.error('获取 CSRF token 失败:', error)
        // 不阻断请求，让后端处理 token 缺失的情况
      }
    }

    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果返回的状态码不是 200，则判断为错误
    if (res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')

      // 401: 未授权
      if (res.code === 401) {
        // 清除 token 并跳转登录页
        tokenManager.clearAll()
        csrfManager.clearToken()
        window.location.href = '/login'
      }

      return Promise.reject(new Error(res.msg || '请求失败'))
    }

    return res
  },
  async error => {
    // 头像404错误静默处理，不显示错误弹窗
    if (error.response?.status === 404 && error.config?.url?.includes('/avatar/')) {
      return Promise.reject(error)
    }

    console.error('响应错误:', error)

    if (error.response) {
      const { status, data } = error.response

      // 打印更详细的错误信息
      console.debug('错误详情:', data)

      /**
       * 401: Token 过期或无效，尝试刷新 token 并重试
       */
      if (status === 401 && !error.config._retry) {
        // 如果是登出或刷新接口本身报 401，严禁重试，直接清理跳转
        const isAuthService = error.config.url.includes('/auth/logout') || error.config.url.includes('/auth/refresh');
        if (isAuthService) {
          tokenManager.clearAll();
          csrfManager.clearToken();
          window.location.href = '/login';
          return Promise.reject(error);
        }

        // 标记为已重试过，防止无限循环
        error.config._retry = true

        // 如果正在刷新 token，将请求加入队列
        if (isTokenRefreshing) {
          return new Promise((resolve) => {
            subscribeTokenRefresh((newToken) => {
              // 使用新 token 重试请求
              error.config.headers['Authorization'] = `Bearer ${newToken}`
              resolve(service.request(error.config))
            })
          })
        }

        // 开始刷新 token
        isTokenRefreshing = true
        try {
          const newToken = await handleTokenRefresh()

          // 使用新 token 重试请求
          error.config.headers['Authorization'] = `Bearer ${newToken}`
          return service.request(error.config)
        } catch (refreshError) {
          console.error('刷新 token 失败:', refreshError)
          ElMessage.error('登录已过期，请重新登录')
          tokenManager.clearAll()
          csrfManager.clearToken()
          window.location.href = '/login'
          return Promise.reject(refreshError)
        } finally {
          isTokenRefreshing = false
        }
      }

      /**
       * CSRF Token 验证失败处理
       * 如果后端返回 403 错误且提示 CSRF token 无效，尝试刷新 token 并重试请求
       */
      if (status === 403 && isCsrfError(data)) {
        console.warn('CSRF token 验证失败，尝试刷新 token')

        try {
          // 刷新 CSRF token
          await csrfManager.refreshToken()

          // 重试原始请求（仅限一次，避免无限循环）
          if (!error.config._retryCsrf) {
            error.config._retryCsrf = true

            // 更新请求头中的 CSRF token
            const newCsrfToken = csrfManager.getCurrentToken()
            if (newCsrfToken) {
              error.config.headers['X-CSRF-Token'] = newCsrfToken
            }

            console.debug('使用新 CSRF token 重试请求')
            return service.request(error.config)
          }
        } catch (refreshError) {
          console.error('刷新 CSRF token 失败:', refreshError)
          ElMessage.error('CSRF 验证失败，请刷新页面重试')
        }
      }

      // 处理其他错误状态码
      switch (status) {
        case 400:
          ElMessage.error(data?.msg || '请求参数错误')
          break
        case 401:
          // 如果已经重试过，显示错误并跳转登录
          if (error.config._retry) {
            ElMessage.error('登录已过期，请重新登录')
            tokenManager.clearAll()
            csrfManager.clearToken()
            window.location.href = '/login'
          }
          break
        case 403:
          // 如果不是 CSRF 错误，则显示通用拒绝访问消息
          if (!isCsrfError(data)) {
            ElMessage.error('拒绝访问')
          }
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(`请求失败: ${status}`)
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error(error.message || '请求失败')
    }

    return Promise.reject(error)
  }
)

/**
 * 判断是否为 CSRF 相关错误
 * @param {object} data - 错误响应数据
 * @returns {boolean}
 */
function isCsrfError(data) {
  if (!data) return false

  const errorMsg = (data.msg || data.message || data.error || '').toLowerCase()
  const csrfKeywords = ['csrf', 'token', '跨站请求伪造', '无效的令牌']

  return csrfKeywords.some(keyword => errorMsg.includes(keyword))
}

export default service
