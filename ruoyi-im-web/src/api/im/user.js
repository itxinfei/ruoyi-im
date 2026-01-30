import request from '../request'

/**
 * 获取用户信息
 * @param {string|number} userId - 可选,用户ID。不传则获取当前用户信息
 * @returns {Promise}
 */
export function getUserInfo(userId) {
  if (userId) {
    return request({
      url: `/api/im/user/${userId}`,
      method: 'get'
    })
  }
  return request({
    url: '/api/im/user/info',
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {string|number} userId - 用户ID
 * @param {Object} data - 用户数据
 * @returns {Promise}
 */
export function updateUser(userId, data) {
  return request({
    url: `/api/im/user/${userId}`,
    method: 'put',
    data
  })
}

/**
 * 搜索用户
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchUsers(keyword) {
  return request({
    url: '/api/im/user/search',
    method: 'get',
    params: {
      keyword
    }
  })
}

/**
 * 搜索用户（API 别名）
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchUsersApi(keyword) {
  return searchUsers(keyword)
}

/**
 * 获取用户详情
 * @param {string|number} userId - 用户ID
 * @returns {Promise}
 */
export function getUserDetail(userId) {
  return request({
    url: `/api/im/user/${userId}`,
    method: 'get'
  })
}

/**
 * 上传用户头像
 * @param {FormData} data - 包含 avatarfile 的 FormData
 * @returns {Promise}
 */
export function uploadAvatar(data) {
  return request({
    url: '/api/im/user/avatar',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
