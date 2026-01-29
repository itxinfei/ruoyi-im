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
    url: '/api/admin/users',
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
    url: `/api/admin/users/${id}`,
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
    url: `/api/admin/users/${id}/status`,
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
    url: `/api/admin/users/${id}/role`,
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
    url: `/api/admin/users/${id}`,
    method: 'delete'
  })
}

/**
 * 获取用户统计
 * @returns {Promise}
 */
export function getUserStats() {
  return request({
    url: '/api/admin/users/stats',
    method: 'get'
  })
}

// ==================== 群组管理 ====================

/**
 * 获取群组列表（分页）
 * @param {Object} params - 查询参数 { keyword, pageNum, pageSize }
 * @returns {Promise}
 */
export function getGroupList(params) {
  return request({
    url: '/api/admin/groups',
    method: 'get',
    params
  })
}

/**
 * 获取群组详情
 * @param {Number} id - 群组ID
 * @returns {Promise}
 */
export function getGroupDetail(id) {
  return request({
    url: `/api/admin/groups/${id}`,
    method: 'get'
  })
}

/**
 * 更新群组信息
 * @param {Number} id - 群组ID
 * @param {Object} data - 群组信息
 * @returns {Promise}
 */
export function updateGroup(id, data) {
  return request({
    url: `/api/admin/groups/${id}`,
    method: 'put',
    data
  })
}

/**
 * 解散群组
 * @param {Number} id - 群组ID
 * @returns {Promise}
 */
export function deleteGroup(id) {
  return request({
    url: `/api/admin/groups/${id}`,
    method: 'delete'
  })
}

/**
 * 批量解散群组
 * @param {Array} ids - 群组ID列表
 * @returns {Promise}
 */
export function batchDeleteGroups(ids) {
  return request({
    url: '/api/admin/groups/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 获取群组统计
 * @returns {Promise}
 */
export function getGroupStats() {
  return request({
    url: '/api/admin/groups/stats',
    method: 'get'
  })
}

/**
 * 获取群组成员列表
 * @param {Number} id - 群组ID
 * @returns {Promise}
 */
export function getGroupMembers(id) {
  return request({
    url: `/api/admin/groups/${id}/members`,
    method: 'get'
  })
}

/**
 * 移除群组成员
 * @param {Number} groupId - 群组ID
 * @param {Number} userId - 用户ID
 * @returns {Promise}
 */
export function removeGroupMember(groupId, userId) {
  return request({
    url: `/api/admin/groups/${groupId}/members/${userId}`,
    method: 'delete'
  })
}

// ==================== 消息管理 ====================

/**
 * 搜索消息列表（分页）
 * @param {Object} params - 查询参数 { keyword, messageType, senderId, conversationId, startTime, endTime, pageNum, pageSize }
 * @returns {Promise}
 */
export function searchMessages(params) {
  return request({
    url: '/api/admin/messages',
    method: 'get',
    params
  })
}

/**
 * 获取消息详情
 * @param {Number} id - 消息ID
 * @returns {Promise}
 */
export function getMessageDetail(id) {
  return request({
    url: `/api/admin/messages/${id}`,
    method: 'get'
  })
}

/**
 * 删除消息
 * @param {Number} id - 消息ID
 * @returns {Promise}
 */
export function deleteMessage(id) {
  return request({
    url: `/api/admin/messages/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除消息
 * @param {Array} ids - 消息ID列表
 * @returns {Promise}
 */
export function batchDeleteMessages(ids) {
  return request({
    url: '/api/admin/messages/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 获取消息统计
 * @param {Object} params - { startTime, endTime }
 * @returns {Promise}
 */
export function getMessageAdminStats(params) {
  return request({
    url: '/api/admin/messages/stats',
    method: 'get',
    params
  })
}

// ==================== 数据统计 ====================

/**
 * 获取系统概览数据
 * @returns {Promise}
 */
export function getOverview() {
  return request({
    url: '/api/admin/stats/overview',
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
    url: '/api/admin/stats/users/active',
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
    url: '/api/admin/stats/groups/active',
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
    url: '/api/admin/stats/messages',
    method: 'get',
    params
  })
}

// ==================== 部门管理 ====================

/**
 * 获取部门列表（树形）
 * @returns {Promise}
 */
export function getDepartmentTree() {
  return request({
    url: '/api/admin/departments/tree',
    method: 'get'
  })
}

/**
 * 获取部门详情
 * @param {Number} id - 部门ID
 * @returns {Promise}
 */
export function getDepartmentDetail(id) {
  return request({
    url: `/api/admin/departments/${id}`,
    method: 'get'
  })
}

/**
 * 新增部门
 * @param {Object} data - 部门信息 { name, parentId, leaderId, description, sort }
 * @returns {Promise}
 */
export function createDepartment(data) {
  return request({
    url: '/api/admin/departments',
    method: 'post',
    data
  })
}

/**
 * 更新部门信息
 * @param {Number} id - 部门ID
 * @param {Object} data - 部门信息
 * @returns {Promise}
 */
export function updateDepartment(id, data) {
  return request({
    url: `/api/admin/departments/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除部门
 * @param {Number} id - 部门ID
 * @returns {Promise}
 */
export function deleteDepartment(id) {
  return request({
    url: `/api/admin/departments/${id}`,
    method: 'delete'
  })
}

/**
 * 获取部门成员列表
 * @param {Number} id - 部门ID
 * @returns {Promise}
 */
export function getDepartmentMembers(id) {
  return request({
    url: `/api/admin/departments/${id}/members`,
    method: 'get'
  })
}

/**
 * 设置部门负责人
 * @param {Number} id - 部门ID
 * @param {Number} leaderId - 负责人用户ID
 * @returns {Promise}
 */
export function setDepartmentLeader(id, leaderId) {
  return request({
    url: `/api/admin/departments/${id}/leader`,
    method: 'put',
    params: { leaderId }
  })
}

/**
 * 移动部门到新的父部门
 * @param {Number} id - 部门ID
 * @param {Number} parentId - 新的父部门ID
 * @returns {Promise}
 */
export function moveDepartment(id, parentId) {
  return request({
    url: `/api/admin/departments/${id}/move`,
    method: 'put',
    params: { parentId }
  })
}

// ==================== 角色权限管理 ====================

/**
 * 获取角色列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getRoleList(params) {
  return request({
    url: '/api/admin/roles',
    method: 'get',
    params
  })
}

/**
 * 获取角色详情
 * @param {Number} id - 角色ID
 * @returns {Promise}
 */
export function getRoleDetail(id) {
  return request({
    url: `/api/admin/roles/${id}`,
    method: 'get'
  })
}

/**
 * 新增角色
 * @param {Object} data - 角色信息 { name, code, description, permissions }
 * @returns {Promise}
 */
export function createRole(data) {
  return request({
    url: '/api/admin/roles',
    method: 'post',
    data
  })
}

/**
 * 更新角色信息
 * @param {Number} id - 角色ID
 * @param {Object} data - 角色信息
 * @returns {Promise}
 */
export function updateRole(id, data) {
  return request({
    url: `/api/admin/roles/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除角色
 * @param {Number} id - 角色ID
 * @returns {Promise}
 */
export function deleteRole(id) {
  return request({
    url: `/api/admin/roles/${id}`,
    method: 'delete'
  })
}

/**
 * 获取角色权限列表
 * @returns {Promise}
 */
export function getPermissionList() {
  return request({
    url: '/api/admin/permissions',
    method: 'get'
  })
}

/**
 * 分配角色权限
 * @param {Number} id - 角色ID
 * @param {Array} permissionIds - 权限ID列表
 * @returns {Promise}
 */
export function assignRolePermissions(id, permissionIds) {
  return request({
    url: `/api/admin/roles/${id}/permissions`,
    method: 'put',
    data: permissionIds
  })
}

/**
 * 获取角色成员列表
 * @param {Number} id - 角色ID
 * @returns {Promise}
 */
export function getRoleMembers(id) {
  return request({
    url: `/api/admin/roles/${id}/members`,
    method: 'get'
  })
}

// ==================== 系统配置 ====================

/**
 * 获取系统配置
 * @returns {Promise}
 */
export function getSystemConfig() {
  return request({
    url: '/api/admin/system/config',
    method: 'get'
  })
}

/**
 * 更新系统配置
 * @param {Object} data - 配置项键值对
 * @returns {Promise}
 */
export function updateSystemConfig(data) {
  return request({
    url: '/api/admin/system/config',
    method: 'put',
    data
  })
}

/**
 * 获取敏感词列表
 * @returns {Promise}
 */
export function getSensitiveWords() {
  return request({
    url: '/api/admin/system/sensitive-words',
    method: 'get'
  })
}

/**
 * 保存敏感词列表
 * @param {Object} data - { strategy: 'reject|replace', words: [] }
 * @returns {Promise}
 */
export function saveSensitiveWords(data) {
  return request({
    url: '/api/admin/system/sensitive-words',
    method: 'post',
    data
  })
}
