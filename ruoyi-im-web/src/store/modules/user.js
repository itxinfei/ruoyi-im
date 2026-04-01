import { login, logout, validateToken } from '@/api/im/auth'
import { getUserInfo, updateUser } from '@/api/im/user'
import tokenManager from '@/utils/tokenManager'

// 角色验证缓存时间（5分钟）
const ROLE_CACHE_DURATION = 5 * 60 * 1000

export default {
  namespaced: true,

  state: () => ({
    token: tokenManager.getToken() || '',
    userInfo: tokenManager.getUserInfo() || {},
    role: tokenManager.getRole() || 'USER',
    online: false,
    // 角色验证时间戳
    roleValidatedAt: 0
  }),

  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      tokenManager.setToken(token)
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      tokenManager.setUserInfo(userInfo)
      // 保存用户角色
      if (userInfo.role) {
        state.role = userInfo.role
        tokenManager.setRole(userInfo.role)
      }
    },
    SET_ROLE(state, role) {
      state.role = role
      tokenManager.setRole(role)
    },
    CLEAR_USER(state) {
      state.token = ''
      state.userInfo = {}
      state.role = 'USER'
      state.online = false
      state.roleValidatedAt = 0
      tokenManager.clearAll()
    },
    SET_ONLINE_STATUS(state, status) {
      state.online = status
    },
    SET_ROLE_VALIDATED_AT(state, timestamp) {
      state.roleValidatedAt = timestamp
    }
  },

  actions: {
    // 登录
    async login({ commit, dispatch }, loginForm) {
      try {
        const res = await login(loginForm)
        if (res.code === 200) {
          commit('SET_TOKEN', res.data.token)
          commit('SET_USER_INFO', res.data.userInfo)
          commit('SET_ONLINE_STATUS', true)
          commit('SET_ROLE_VALIDATED_AT', Date.now())
          // 同步当前用户到 im 模块，确保消息气泡 is-me 判断正确
          dispatch('im/setCurrentUser', res.data.userInfo, { root: true })
          return res
        }
        return Promise.reject(new Error(res.msg || '登录失败'))
      } catch (error) {
        return Promise.reject(error)
      }
    },

    // 获取用户信息（从后端获取，确保角色信息准确）
    async getUserInfo({ commit }, _forceRefresh = false) {
      try {
        const res = await getUserInfo()
        if (res.code === 200) {
          commit('SET_USER_INFO', res.data)
          commit('SET_ONLINE_STATUS', true)
          commit('SET_ROLE_VALIDATED_AT', Date.now())
          return res.data
        }
        throw new Error(res.msg || '获取用户信息失败')
      } catch (error) {
        console.error('Failed to get user info:', error)
        throw error
      }
    },

    // 验证用户角色（从后端验证，用于敏感路由）
    async validateUserRole({ state, dispatch }, requiredRoles) {
      const now = Date.now()
      const isCacheExpired = now - state.roleValidatedAt > ROLE_CACHE_DURATION

      // 如果缓存过期，强制从后端获取最新用户信息
      if (isCacheExpired || !state.userInfo.id) {
        try {
          await dispatch('getUserInfo', true)
        } catch (error) {
          console.error('Failed to refresh user info for role validation:', error)
          throw new Error('验证用户角色失败')
        }
      }

      // 检查角色是否匹配
      const userRole = state.role || 'USER'
      const hasPermission = requiredRoles.includes(userRole)

      if (!hasPermission) {
        throw new Error(`权限不足：需要角色 ${requiredRoles.join(' 或 ')}`)
      }

      return { hasPermission, role: userRole }
    },

    // 验证 token 有效性
    async validateToken() {
      try {
        const res = await validateToken()
        if (res.code === 200) {
          return true
        }
        return false
      } catch (error) {
        console.error('Token validation failed:', error)
        return false
      }
    },

    // 登出
    async logout({ commit }) {
      try {
        await logout()
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        commit('CLEAR_USER')
        // Disconnect WebSocket if exists (handled in IM store usually)
      }
    },

    // 更新用户信息
    async updateProfile(profileData) {
      try {
        const res = await updateUser(this.state.user.userInfo.id, profileData)
        if (res.code === 200) {
          // Refresh info
          await this.dispatch('user/getUserInfo')
        }
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    }
  },

  getters: {
    isLoggedIn: state => !!state.token,
    currentUser: state => state.userInfo,
    userRole: state => state.role || 'USER',
    isAdmin: state => state.role === 'ADMIN' || state.role === 'SUPER_ADMIN',
    userAvatar: state => state.userInfo.avatar || '',
    userName: state => state.userInfo.nickname || state.userInfo.username || 'User',
    // 检查角色是否需要验证（缓存过期）
    needsRoleValidation: state => {
      return Date.now() - state.roleValidatedAt > ROLE_CACHE_DURATION
    }
  }
}
