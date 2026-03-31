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

/**
 * 修改密码
 * @param {string|number} userId - 用户ID
 * @param {string} oldPassword - 旧密码
 * @param {string} newPassword - 新密码
 * @returns {Promise}
 */
export function changePassword(userId, oldPassword, newPassword) {
  return request({
    url: `/api/im/user/${userId}/password`,
    method: 'put',
    params: {
      oldPassword,
      newPassword
    }
  })
}

/**
 * 获取用户好友列表
 * @param {string|number} userId - 用户ID
 * @returns {Promise}
 */
export function getUserFriends(userId) {
  return request({
    url: `/api/im/user/friends/${userId}`,
    method: 'get'
  })
}

/**
 * 批量获取用户信息
 * @param {Array} userIds - 用户ID数组
 * @returns {Promise}
 */
export function getUsersBatch(userIds) {
  return request({
    url: '/api/im/user/batch',
    method: 'get',
    params: {
      ids: userIds.join(',')
    }
  })
}

/**
 * 创建用户
 * @param {Object} data - 用户数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.nickname - 昵称
 * @param {string} data.email - 邮箱
 * @param {string} data.phone - 手机号
 * @returns {Promise}
 */
export function createUser(data) {
  return request({
    url: '/api/im/user',
    method: 'post',
    data
  })
}

/**
 * 删除用户
 * @param {string|number} userId - 用户ID
 * @returns {Promise}
 */
export function deleteUser(userId) {
  return request({
    url: `/api/im/user/${userId}`,
    method: 'delete'
  })
}

/**
 * 获取用户列表
 * @param {string} keyword - 搜索关键词（可选）
 * @returns {Promise}
 */
export function getUserList(keyword) {
  return request({
    url: '/api/im/user/list',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 修改用户状态
 * @param {Object} data - 状态数据
 * @param {number} data.id - 用户ID
 * @param {string} data.status - 状态 ENABLED/DISABLED
 * @returns {Promise}
 */
export function changeUserStatus(data) {
  return request({
    url: '/api/im/user/changeStatus',
    method: 'put',
    data
  })
}
