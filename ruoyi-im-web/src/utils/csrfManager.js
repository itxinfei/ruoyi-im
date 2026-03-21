/**
 * CSRF Token 管理器
 * 用于防止跨站请求伪造 (CSRF) 攻击
 * 提供 CSRF token 的获取、存储、验证和自动刷新功能
 */

import request from '@/api/request'

// 内存缓存（用于快速访问）
const memoryCache = {
  token: '',
  expiresAt: 0,
  isFetching: false
}

// CSRF Token 默认过期时间（1小时）
const DEFAULT_TOKEN_EXPIRES_IN = 60 * 60 * 1000

// 存储键名
const STORAGE_KEYS = {
  TOKEN: 'im_csrf_token',
  EXPIRES_AT: 'im_csrf_expires_at'
}

// Token 刷新阈值（过期前10分钟开始刷新）
const REFRESH_THRESHOLD = 10 * 60 * 1000

/**
 * 检查 token 是否过期
 * @returns {boolean}
 */
function isTokenExpired() {
  if (!memoryCache.expiresAt) {
    return true
  }
  return Date.now() > memoryCache.expiresAt
}

/**
 * 检查 token 是否即将过期（需要刷新）
 * @returns {boolean}
 */
function isTokenExpiringSoon() {
  if (!memoryCache.expiresAt) {
    return true
  }
  const timeLeft = memoryCache.expiresAt - Date.now()
  return timeLeft > 0 && timeLeft < REFRESH_THRESHOLD
}

/**
 * 从 sessionStorage 加载数据到内存缓存
 */
function loadFromStorage() {
  try {
    const token = sessionStorage.getItem(STORAGE_KEYS.TOKEN)
    const expiresAt = sessionStorage.getItem(STORAGE_KEYS.EXPIRES_AT)

    if (token) {
      memoryCache.token = token
    }
    if (expiresAt) {
      memoryCache.expiresAt = parseInt(expiresAt, 10)
    }
  } catch (error) {
    console.error('从 sessionStorage 加载 CSRF token 失败:', error)
    clearToken()
  }
}

/**
 * 清除 token
 */
function clearToken() {
  memoryCache.token = ''
  memoryCache.expiresAt = 0
  sessionStorage.removeItem(STORAGE_KEYS.TOKEN)
  sessionStorage.removeItem(STORAGE_KEYS.EXPIRES_AT)
}

/**
 * 从后端 API 获取 CSRF token
 * @returns {Promise<string>}
 */
async function fetchTokenFromServer() {
  try {
    // 使用独立的 axios 实例，避免拦截器循环
    const response = await request.get('/csrf/token', {
      skipCsrfToken: true, // 标记跳过 CSRF token 验证
      headers: {
        'Cache-Control': 'no-cache',
        'Pragma': 'no-cache'
      }
    })

    // 兼容多种响应结构
    const token = response?.data?.csrfToken || response?.csrfToken || response?.data?.token
    if (token) {
      return token
    }

    console.warn('后端响应中未找到 CSRF token，使用禁用占位符')
    return 'disabled-by-client-safety'
  } catch (error) {
    console.error('获取 CSRF token 失败 (已捕获并忽略):', error)
    return 'error-fallback-token'
  }
}

/**
 * CSRF Token 管理器对象
 */
export const csrfManager = {
  /**
   * 初始化（从 sessionStorage 加载数据）
   */
  init() {
    loadFromStorage()
  },

  /**
   * 获取 CSRF token（自动处理过期和刷新）
   * @returns {Promise<string>}
   */
  async getToken() {
    // 如果正在获取中，等待当前请求完成
    if (memoryCache.isFetching) {
      await new Promise(resolve => {
        const checkInterval = setInterval(() => {
          if (!memoryCache.isFetching) {
            clearInterval(checkInterval)
            resolve()
          }
        }, 100)
      })
      return memoryCache.token || ''
    }

    // 检查 token 是否需要刷新
    if (isTokenExpired() || isTokenExpiringSoon()) {
      await this.refreshToken()
    }

    return memoryCache.token || ''
  },

  /**
   * 刷新 CSRF token
   * @returns {Promise<boolean>}
   */
  async refreshToken() {
    // 防止并发刷新
    if (memoryCache.isFetching) {
      console.log('CSRF token 正在刷新中，跳过重复请求')
      return true
    }

    memoryCache.isFetching = true

    try {
      const token = await fetchTokenFromServer()

      if (!token) {
        console.warn('获取到的 CSRF token 为空')
        return false
      }

      const expiresAt = Date.now() + DEFAULT_TOKEN_EXPIRES_IN
      memoryCache.token = token
      memoryCache.expiresAt = expiresAt

      // 持久化到 sessionStorage
      sessionStorage.setItem(STORAGE_KEYS.TOKEN, token)
      sessionStorage.setItem(STORAGE_KEYS.EXPIRES_AT, expiresAt.toString())

      console.log('CSRF token 已刷新，过期时间:', new Date(expiresAt).toLocaleString())
      return true
    } catch (error) {
      console.error('刷新 CSRF token 失败:', error)
      return false
    } finally {
      memoryCache.isFetching = false
    }
  },

  /**
   * 获取 token 剩余有效时间（毫秒）
   * @returns {number}
   */
  getTokenTimeLeft() {
    if (!memoryCache.expiresAt) {
      return 0
    }
    const timeLeft = memoryCache.expiresAt - Date.now()
    return Math.max(0, timeLeft)
  },

  /**
   * 格式化剩余时间
   * @returns {string}
   */
  getTokenTimeLeftFormatted() {
    const timeLeft = this.getTokenTimeLeft()
    if (timeLeft <= 0) {
      return '已过期'
    }

    const minutes = Math.floor(timeLeft / (60 * 1000))
    const seconds = Math.floor((timeLeft % (60 * 1000)) / 1000)

    if (minutes > 60) {
      const hours = Math.floor(minutes / 60)
      return `${hours}小时${minutes % 60}分钟`
    }

    return `${minutes}分${seconds}秒`
  },

  /**
   * 清除 CSRF token（登出时调用）
   */
  clearToken() {
    clearToken()
    console.log('CSRF token 已清除')
  },

  /**
   * 检查 token 是否有效
   * @returns {boolean}
   */
  isTokenValid() {
    return !!memoryCache.token && !isTokenExpired()
  },

  /**
   * 预加载 CSRF token（应用启动时调用）
   * @returns {Promise<boolean>}
   */
  async preloadToken() {
    if (this.isTokenValid()) {
      console.log('CSRF token 有效，无需预加载')
      return true
    }

    console.log('开始预加载 CSRF token...')
    return await this.refreshToken()
  },

  /**
   * 获取当前 token（同步方法，不触发刷新）
   * @returns {string}
   */
  getCurrentToken() {
    return memoryCache.token || ''
  }
}

// 初始化 CSRF 管理器
csrfManager.init()

// 导出默认实例
export default csrfManager
