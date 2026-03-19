/**
 * Token 管理器
 * 统一管理 token、用户信息和角色，提供安全的存储和访问方式
 * 使用 sessionStorage 存储而非 localStorage，关闭浏览器自动清除
 */

// 内存缓存（用于快速访问）
const memoryCache = {
  token: '',
  userInfo: {},
  role: 'USER',
  expiresAt: 0
}

// Token 默认过期时间（2小时）
const DEFAULT_TOKEN_EXPIRES_IN = 2 * 60 * 60 * 1000

// Token 刷新阈值（剩余时间少于30分钟时自动刷新）
const TOKEN_REFRESH_THRESHOLD = 30 * 60 * 1000

// 刷新锁，防止并发刷新
let isRefreshing = false
let refreshPromise = null

// 存储键名
const STORAGE_KEYS = {
  TOKEN: 'im_token',
  USER_INFO: 'im_user_info',
  USER_ROLE: 'im_user_role',
  EXPIRES_AT: 'im_token_expires_at'
}

/**
 * 检查 token 是否过期
 * @returns {boolean}
 */
function isTokenExpired() {
  if (!memoryCache.expiresAt) {
    return false
  }
  return Date.now() > memoryCache.expiresAt
}

/**
 * 从 sessionStorage 加载数据到内存缓存
 */
function loadFromStorage() {
  try {
    const token = sessionStorage.getItem(STORAGE_KEYS.TOKEN)
    const userInfoStr = sessionStorage.getItem(STORAGE_KEYS.USER_INFO)
    const role = sessionStorage.getItem(STORAGE_KEYS.USER_ROLE)
    const expiresAt = sessionStorage.getItem(STORAGE_KEYS.EXPIRES_AT)

    if (token) {
      memoryCache.token = token
    }
    if (userInfoStr) {
      memoryCache.userInfo = JSON.parse(userInfoStr)
    }
    if (role) {
      memoryCache.role = role
    }
    if (expiresAt) {
      memoryCache.expiresAt = parseInt(expiresAt, 10)
    }
  } catch (error) {
    console.error('从 sessionStorage 加载数据失败:', error)
    // 清除缓存数据
    memoryCache.token = null
    memoryCache.userInfo = {}
    memoryCache.role = 'USER'
    memoryCache.expiresAt = null
  }
}

/**
 * Token 管理器对象
 */
export const tokenManager = {
  /**
   * 初始化（从 sessionStorage 加载数据）
   */
  init() {
    loadFromStorage()
  },

  /**
   * 设置 token
   * @param {string} token - 认证令牌
   * @param {number} expiresIn - 过期时间（毫秒），默认2小时
   */
  setToken(token, expiresIn = DEFAULT_TOKEN_EXPIRES_IN) {
    if (!token) {
      console.warn('尝试设置空的 token')
      return
    }

    const expiresAt = Date.now() + expiresIn
    memoryCache.token = token
    memoryCache.expiresAt = expiresAt

    // 持久化到 sessionStorage
    sessionStorage.setItem(STORAGE_KEYS.TOKEN, token)
    sessionStorage.setItem(STORAGE_KEYS.EXPIRES_AT, expiresAt.toString())

    console.log('Token 已设置，过期时间:', new Date(expiresAt).toLocaleString())
  },

  /**
   * 获取 token
   * @returns {string|null}
   */
  getToken() {
    // 检查是否过期
    if (isTokenExpired()) {
      console.warn('Token 已过期')
      this.clearToken()
      return null
    }

    return memoryCache.token || null
  },

  /**
   * 清除 token
   */
  clearToken() {
    memoryCache.token = ''
    memoryCache.expiresAt = 0
    sessionStorage.removeItem(STORAGE_KEYS.TOKEN)
    sessionStorage.removeItem(STORAGE_KEYS.EXPIRES_AT)
  },

  /**
   * 设置用户信息
   * @param {object} userInfo - 用户信息
   */
  setUserInfo(userInfo) {
    if (!userInfo || typeof userInfo !== 'object') {
      console.warn('尝试设置无效的用户信息')
      return
    }

    memoryCache.userInfo = userInfo

    // 保存用户角色
    if (userInfo.role) {
      memoryCache.role = userInfo.role
      sessionStorage.setItem(STORAGE_KEYS.USER_ROLE, userInfo.role)
    }

    // 持久化到 sessionStorage
    sessionStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(userInfo))
  },

  /**
   * 获取用户信息
   * @returns {object}
   */
  getUserInfo() {
    return memoryCache.userInfo || {}
  },

  /**
   * 设置用户角色
   * @param {string} role - 用户角色
   */
  setRole(role) {
    if (!role) {
      return
    }
    memoryCache.role = role
    sessionStorage.setItem(STORAGE_KEYS.USER_ROLE, role)
  },

  /**
   * 获取用户角色
   * @returns {string}
   */
  getRole() {
    return memoryCache.role || 'USER'
  },

  /**
   * 获取用户 ID
   * @returns {string|number|null}
   */
  getUserId() {
    return memoryCache.userInfo?.id || null
  },

  /**
   * 清除所有数据（登出时调用）
   */
  clearAll() {
    this.clearToken()
    memoryCache.userInfo = {}
    memoryCache.role = 'USER'

    sessionStorage.removeItem(STORAGE_KEYS.USER_INFO)
    sessionStorage.removeItem(STORAGE_KEYS.USER_ROLE)

    console.log('Token 管理器已清空所有数据')
  },

  /**
   * 检查是否已登录
   * @returns {boolean}
   */
  isLoggedIn() {
    const token = this.getToken()
    return !!token && !isTokenExpired()
  },

  /**
   * 检查 token 是否即将过期（剩余时间少于30分钟）
   * @returns {boolean}
   */
  isTokenExpiringSoon() {
    if (!memoryCache.expiresAt) {
      return false
    }
    const timeLeft = memoryCache.expiresAt - Date.now()
    return timeLeft > 0 && timeLeft < TOKEN_REFRESH_THRESHOLD
  },

  /**
   * 刷新 token（需要调用 API）
   * @param {function} refreshFunction - 刷新 token 的异步函数
   * @returns {Promise<boolean>}
   */
  async refreshToken(refreshFunction) {
    try {
      if (typeof refreshFunction !== 'function') {
        console.error('刷新函数必须是有效的函数')
        return false
      }

      const result = await refreshFunction()
      if (result && result.token) {
        this.setToken(result.token, result.expiresIn || DEFAULT_TOKEN_EXPIRES_IN)
        console.log('Token 刷新成功')
        return true
      }
      return false
    } catch (error) {
      console.error('Token 刷新失败:', error)
      this.clearAll()
      return false
    }
  },

  /**
   * 自动刷新 token（带并发控制）
   * 如果 token 即将过期（剩余时间少于30分钟），自动刷新
   * @param {function} refreshFunction - 刷新 token 的异步函数
   * @returns {Promise<string|null>} 返回新的 token 或 null
   */
  async autoRefreshToken(refreshFunction) {
    // 如果正在刷新，等待刷新完成
    if (isRefreshing) {
      console.log('Token 正在刷新中，等待刷新完成...')
      return refreshPromise
    }

    // 检查是否需要刷新
    if (!this.isTokenExpiringSoon()) {
      return memoryCache.token
    }

    // 开始刷新
    isRefreshing = true
    refreshPromise = (async () => {
      try {
        if (typeof refreshFunction !== 'function') {
          console.error('刷新函数必须是有效的函数')
          return null
        }

        const result = await refreshFunction()
        if (result && result.token) {
          this.setToken(result.token, result.expiresIn || DEFAULT_TOKEN_EXPIRES_IN)
          console.log('Token 自动刷新成功')
          return result.token
        }
        return null
      } catch (error) {
        console.error('Token 自动刷新失败:', error)
        // 刷新失败，清除所有数据并跳转登录页
        this.clearAll()
        window.location.href = '/login'
        return null
      } finally {
        isRefreshing = false
        refreshPromise = null
      }
    })()

    return refreshPromise
  },

  /**
   * 检查是否需要刷新 token
   * @returns {boolean}
   */
  shouldRefreshToken() {
    return this.isTokenExpiringSoon()
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
  }
}

// 初始化 token 管理器
tokenManager.init()

// 导出默认实例
export default tokenManager
