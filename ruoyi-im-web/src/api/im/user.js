import request from '../request'

/**
 * 获取当前用户信息
 * @returns {Promise}
 */
export function getUserInfo() {
  return request({
    url: '/im/user/info',
    method: 'get'
  })
}

/**
 * 获取指定用户信息
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function getUser(userId) {
  return request({
    url: `/im/user/${userId}`,
    method: 'get'
  })
}

/**
 * 搜索用户
 * @param {string} keyword - 关键词
 * @returns {Promise}
 */
export function searchUsers(keyword) {
  return request({
    url: '/im/user/search',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 更新用户信息
 * @param {number} userId - 用户ID
 * @param {Object} data - 更新数据
 * @param {string} data.nickname - 昵称
 * @param {string} data.avatar - 头像
 * @param {string} data.email - 邮箱
 * @param {string} data.mobile - 手机号
 * @param {number} data.gender - 性别
 * @param {string} data.signature - 签名
 * @param {string} data.department - 部门
 * @param {string} data.position - 职位
 * @returns {Promise}
 */
export function updateUser(userId, data) {
  return request({
    url: `/im/user/${userId}`,
    method: 'put',
    data
  })
}

/**
 * 修改密码
 * @param {number} userId - 用户ID
 * @param {string} oldPassword - 旧密码
 * @param {string} newPassword - 新密码
 * @returns {Promise}
 */
export function changePassword(userId, oldPassword, newPassword) {
  return request({
    url: `/im/user/${userId}/password`,
    method: 'put',
    params: {
      oldPassword,
      newPassword
    }
  })
}

/**
 * 上传头像
 * @param {FormData} data - 表单数据(avatarfile)
 * @returns {Promise}
 */
export function uploadAvatar(data) {
  return request({
    url: '/im/user/avatar',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
