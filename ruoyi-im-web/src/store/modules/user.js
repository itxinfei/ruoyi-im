import { login, logout } from '@/api/im/auth'
import { getUserInfo, updateUser } from '@/api/im/user'
import { getToken, setToken, getUserInfo as getStoredUserInfo, setUserInfo, setUserRole, clearAuth } from '@/utils/storage'

export default {
    namespaced: true,

    state: () => ({
        token: getToken(),
        userInfo: getStoredUserInfo(),
        role: getUserRole(),
        online: false
    }),

    mutations: {
        SET_TOKEN(state, token) {
            state.token = token
            setToken(token)
        },
        SET_USER_INFO(state, userInfo) {
            state.userInfo = userInfo
            setUserInfo(userInfo)
            // 保存用户角色
            if (userInfo.role) {
                state.role = userInfo.role
                setUserRole(userInfo.role)
            }
        },
        SET_ROLE(state, role) {
            state.role = role
            setUserRole(role)
        },
        CLEAR_USER(state) {
            state.token = ''
            state.userInfo = {}
            state.role = 'USER'
            state.online = false
            clearAuth()
        },
        SET_ONLINE_STATUS(state, status) {
            state.online = status
        }
    },

    actions: {
        // 登录
        async login({ commit }, loginForm) {
            try {
                const res = await login(loginForm)
                if (res.code === 200) {
                    commit('SET_TOKEN', res.data.token)
                    commit('SET_USER_INFO', res.data.userInfo)
                    commit('SET_ONLINE_STATUS', true)
                    return res
                }
                return Promise.reject(new Error(res.msg || '登录失败'))
            } catch (error) {
                return Promise.reject(error)
            }
        },

        // 获取用户信息
        async getUserInfo({ commit }) {
            try {
                const res = await getUserInfo()
                if (res.code === 200) {
                    commit('SET_USER_INFO', res.data)
                    commit('SET_ONLINE_STATUS', true)
                    return res.data
                }
            } catch (error) {
                console.error('Failed to get user info:', error)
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
        async updateProfile({ commit, dispatch }, profileData) {
            try {
                const res = await updateUser(this.state.user.userInfo.id, profileData)
                if (res.code === 200) {
                    // Refresh info
                    await dispatch('getUserInfo')
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
        userName: state => state.userInfo.nickname || state.userInfo.username || 'User'
    }
}
