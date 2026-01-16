/**
 * IM用户模块API
 * @module api/im/user
 */
import request from '@/utils/request'

/**
 * 获取用户列表
 * @param {Object} [params] - 查询参数
 * @param {number} [params.pageSize=20] - 每页数量
 * @param {number} [params.pageNum=1] - 当前页码
 * @param {string} [params.keyword] - 搜索关键词
 * @param {number} [params.status] - 用户状态
 * @returns {Promise}
 */
export function listUser(params) {
  return request({
    url: '/api/im/user/list',
    method: 'get',
    params: {
      pageSize: params?.pageSize || 20,
      pageNum: params?.pageNum || 1,
      keyword: params?.keyword,
      status: params?.status,
    },
  })
}

/**
 * 获取用户详情
 * @param {string} userId - 用户ID
 * @returns {Promise}
 */
export function getUser(userId) {
  return request({
    url: `/api/im/user/${userId}`,
    method: 'get',
  })
}

/**
 * 创建用户
 * @param {Object} data - 用户数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.nickname - 昵称
 * @param {string} [data.mobile] - 手机号
 * @param {string} [data.email] - 邮箱
 * @returns {Promise}
 */
export function createUser(data) {
  return request({
    url: '/api/im/user',
    method: 'post',
    data,
  })
}

/**
 * 更新用户信息
 * @param {string} userId - 用户ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export function updateUser(userId, data) {
  return request({
    url: `/api/im/user/${userId}`,
    method: 'put',
    data,
  })
}

/**
 * 删除用户
 * @param {string} userId - 用户ID
 * @returns {Promise}
 */
export function deleteUser(userId) {
  return request({
    url: `/api/im/user/${userId}`,
    method: 'delete',
  })
}

/**
 * 修改用户状态
 * @param {string} userId - 用户ID
 * @param {number} status - 状态 0=禁用 1=启用
 * @returns {Promise}
 */
export function changeUserStatus(userId, status) {
  return request({
    url: '/api/im/user/changeStatus',
    method: 'put',
    data: { userId, status },
  })
}

/**
 * 获取用户好友列表
 * @param {string} userId - 用户ID
 * @returns {Promise}
 */
export function getUserFriends(userId) {
  return request({
    url: `/api/im/user/friends/${userId}`,
    method: 'get',
  })
}

/**
 * 获取当前登录用户信息
 * @returns {Promise}
 */
export function getCurrentUser() {
  return request({
    url: '/api/im/user/info',
    method: 'get',
  })
}

/**
 * 更新个人资料
 * @param {string} userId - 用户ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export function updateProfile(userId, data) {
  return updateUser(userId, data)
}

/**
 * 修改密码
 * @param {string} userId - 用户ID
 * @param {string} oldPassword - 旧密码
 * @param {string} newPassword - 新密码
 * @returns {Promise}
 */
export function changePassword(userId, oldPassword, newPassword) {
  return request({
    url: `/api/im/user/${userId}/password`,
    method: 'put',
    params: { oldPassword, newPassword },
  })
}

/**
 * 上传用户头像
 * @param {FormData} data - 文件数据
 * @returns {Promise}
 */
export function uploadAvatar(data) {
  return request({
    url: '/api/im/user/avatar',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
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
    params: { keyword },
  })
}

/**
 * 批量获取用户信息
 * @param {string[]} userIds - 用户ID数组
 * @returns {Promise}
 */
export function getUsersByIds(userIds) {
  return request({
    url: '/api/im/user/batch',
    method: 'get',
    params: { ids: userIds.join(',') },
  })
}

/**
 * 重置用户密码（管理员操作）
 * @param {string} userId - 用户ID
 * @param {string} password - 新密码
 * @returns {Promise}
 */
export function resetUserPassword(userId, password) {
  return request({
    url: `/api/im/user/${userId}/reset-password`,
    method: 'put',
    data: { password },
  })
}

// ========== 别名 - 向后兼容 ==========

export const addUser = createUser
export const delUser = deleteUser
export const listUserFriends = getUserFriends
export const getCurrentUserInfo = getCurrentUser

// 默认导出
export default {
  listUser,
  getUser,
  createUser,
  updateUser,
  deleteUser,
  changeUserStatus,
  getUserFriends,
  getCurrentUser,
  updateProfile,
  changePassword,
  uploadAvatar,
  searchUsers,
  getUsersByIds,
  // 别名
  addUser,
  delUser,
  listUserFriends,
  getCurrentUserInfo,
}
