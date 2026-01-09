import { login, logout, getInfo } from '@/api/login'

const state = {
  token: localStorage.getItem('token') || '',
  name: '',
  avatar: '',
  roles: [],
  permissions: [],
}

const mutations = {
  SET_TOKEN: (state, token) => {
    state.token = token
    if (token) {
      localStorage.setItem('token', token)
    } else {
      localStorage.removeItem('token')
    }
  },
  SET_NAME: (state, name) => {
    state.name = name
  },
  SET_AVATAR: (state, avatar) => {
    state.avatar = avatar
  },
  SET_ROLES: (state, roles) => {
    state.roles = roles
  },
  SET_PERMISSIONS: (state, permissions) => {
    state.permissions = permissions
  },
  SET_USER_INFO: (state, userInfo) => {
    const { username, nickname, avatar, roles, permissions } = userInfo
    state.name = nickname || username
    state.avatar = avatar
    state.roles = roles
    state.permissions = permissions || []
    // 同时保存用户信息到localStorage
    if (userInfo) {
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } else {
      localStorage.removeItem('userInfo')
    }
  },
}

const actions = {
  // 登录
  login({ commit }, userInfo) {
    const { username, password } = userInfo
    return new Promise((resolve, reject) => {
      login(username, password)
        .then(response => {
          const { data } = response
          commit('SET_TOKEN', data.token)
          resolve()
        })
        .catch(error => {
          reject(error)
        })
    })
  },

  // 获取用户信息
  getInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      getInfo(state.token)
        .then(response => {
          if (response.code === 200) {
            const data = response.data

            if (!data) {
              reject('Verification failed, please Login again.')
            }

            // 提取用户信息（包含userId，这是关键！）
            const { userId, id, username, nickname, avatar, email, phone, roles, permissions } = data

            // 构建完整的用户信息对象
            const userInfo = {
              userId: userId || id,
              id: userId || id,
              username,
              nickname: nickname || username,
              nickName: nickname || username,
              avatar,
              email,
              phone,
              roles,
              permissions
            }

            commit('SET_NAME', nickname || username)
            commit('SET_AVATAR', avatar)
            // 关键：保存完整用户信息到localStorage
            commit('SET_USER_INFO', userInfo)
            resolve(data)
          } else {
            reject(new Error(response.msg || '获取用户信息失败'))
          }
        })
        .catch(error => {
          reject(error)
        })
    })
  },

  // 登出
  logout({ commit, state }) {
    return new Promise((resolve, reject) => {
      logout(state.token)
        .then(response => {
          if (response.code === 200 || response.code === undefined) {
            commit('SET_TOKEN', '')
            commit('SET_NAME', '')
            commit('SET_AVATAR', '')
            commit('SET_ROLES', [])
            commit('SET_PERMISSIONS', [])
            // 清除localStorage中的数据
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            resolve()
          } else {
            reject(new Error(response.msg || '登出失败'))
          }
        })
        .catch(error => {
          reject(error)
        })
    })
  },

  // 重置token
  resetToken({ commit }) {
    return new Promise(resolve => {
      commit('SET_TOKEN', '')
      commit('SET_NAME', '')
      commit('SET_AVATAR', '')
      commit('SET_ROLES', [])
      commit('SET_PERMISSIONS', [])
      // 清除localStorage中的数据
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      resolve()
    })
  },
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
}
