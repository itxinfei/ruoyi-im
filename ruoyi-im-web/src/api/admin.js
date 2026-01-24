/**
 * 管理员 API 接口封装
 */
import request from './request'

// ==================== 用户管理 ====================

/**
 * 获取用户列表（分页）
 * @param {Object} params - 查询参数 { keyword, role, pageNum, pageSize }
 * @returns {Promise}
 */
export function getUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

/**
 * 获取用户详情
 * @param {Number} id - 用户ID
 * @returns {Promise}
 */
export function getUserDetail(id) {
  return request({
    url: `/admin/users/${id}`,
    method: 'get'
  })
}

/**
 * 修改用户状态
 * @param {Number} id - 用户ID
 * @param {Number} status - 状态 0=禁用 1=启用
 * @returns {Promise}
 */
export function updateUserStatus(id, status) {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 修改用户角色
 * @param {Number} id - 用户ID
 * @param {String} role - 角色 USER/ADMIN/SUPER_ADMIN
 * @returns {Promise}
 */
export function updateUserRole(id, role) {
  return request({
    url: `/admin/users/${id}/role`,
    method: 'put',
    params: { role }
  })
}

/**
 * 删除用户
 * @param {Number} id - 用户ID
 * @returns {Promise}
 */
export function deleteUser(id) {
  return request({
    url: `/admin/users/${id}`,
    method: 'delete'
  })
}

/**
 * 获取用户统计
 * @returns {Promise}
 */
export function getUserStats() {
  return request({
    url: '/admin/users/stats',
    method: 'get'
  })
}

// ==================== 数据统计 ====================

/**
 * 获取系统概览数据
 * @returns {Promise}
 */
export function getOverview() {
  return request({
    url: '/admin/stats/overview',
    method: 'get'
  })
}

/**
 * 获取用户活跃度统计
 * @param {Number} days - 统计天数，默认7天
 * @returns {Promise}
 */
export function getUserActiveStats(days = 7) {
  return request({
    url: '/admin/stats/users/active',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取群组活跃度统计
 * @param {Number} days - 统计天数，默认7天
 * @returns {Promise}
 */
export function getGroupActiveStats(days = 7) {
  return request({
    url: '/admin/stats/groups/active',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取消息统计
 * @param {Object} params - { startDate, endDate }
 * @returns {Promise}
 */
export function getMessageStats(params) {
  return request({
    url: '/admin/stats/messages',
    method: 'get',
    params
  })
}
