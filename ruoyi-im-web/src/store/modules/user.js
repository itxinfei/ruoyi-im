import { login, logout } from '@/api/im/auth'
import { getUserInfo, updateUser } from '@/api/im/user'

export default {
    namespaced: true,

    state: () => ({
        token: localStorage.getItem('im_token') || '',
        userInfo: JSON.parse(localStorage.getItem('im_user_info') || '{}'),
        online: false
    }),

    mutations: {
        SET_TOKEN(state, token) {
            state.token = token
            localStorage.setItem('im_token', token)
        },
        SET_USER_INFO(state, userInfo) {
            state.userInfo = userInfo
            localStorage.setItem('im_user_info', JSON.stringify(userInfo))
        },
        CLEAR_USER(state) {
            state.token = ''
            state.userInfo = {}
            state.online = false
            localStorage.removeItem('im_token')
            localStorage.removeItem('im_user_info')
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
        async updateProfile({ commit }, profileData) {
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
        userAvatar: state => state.userInfo.avatar || '',
        userName: state => state.userInfo.nickname || state.userInfo.username || 'User'
    }
}
