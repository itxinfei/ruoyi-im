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
 * 创建用户
 * @param {Object} data - 用户信息
 * @returns {Promise}
 */
export function createUser(data) {
  return request({
    url: '/api/admin/users',
    method: 'post',
    data
  })
}

/**
 * 更新用户信息
 * @param {Number} id - 用户ID
 * @param {Object} data - 用户信息
 * @returns {Promise}
 */
export function updateUser(id, data) {
  return request({
    url: `/api/admin/users/${id}`,
    method: 'put',
    data
  })
}

/**
 * 获取用户选项（用于下拉选择）
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getUserOptions(params) {
  return request({
    url: '/api/admin/users/options',
    method: 'get',
    params
  })
}

/**
 * 获取用户统计
 * @returns {Promise}
 */
export function getUserStats() {
  return request({
    url: '/api/admin/stats/users/roles',
    method: 'get'
  })
}

/**
 * 批量删除用户
 * @param {Array} ids - 用户ID列表
 * @returns {Promise}
 */
export function batchDeleteUsers(ids) {
  return request({
    url: '/api/admin/users/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 批量更新用户状态
 * @param {Array} ids - 用户ID列表
 * @param {Number} status - 状态 0=禁用 1=启用
 * @returns {Promise}
 */
export function batchUpdateUserStatus(ids, status) {
  return request({
    url: '/api/admin/users/batch/status',
    method: 'put',
    data: { ids, status }
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

/**
 * 添加群组成员
 * @param {Number} groupId - 群组ID
 * @param {Object} data - { userIds: [], role: '' }
 * @returns {Promise}
 */
export function addGroupMember(groupId, data) {
  return request({
    url: `/api/admin/groups/${groupId}/members`,
    method: 'post',
    data
  })
}

/**
 * 切换群组禁言状态
 * @param {Number} groupId - 群组ID
 * @param {Boolean} muted - 是否禁言
 * @returns {Promise}
 */
export function toggleGroupMute(groupId, muted) {
  return request({
    url: `/api/admin/groups/${groupId}/mute`,
    method: 'put',
    data: { muted }
  })
}

/**
 * 批量设置群成员禁言
 * @param {Number} groupId - 群组ID
 * @param {Array} userIds - 用户ID列表
 * @param {Number} duration - 禁言时长（秒），0表示永久禁言
 * @returns {Promise}
 */
export function batchMuteGroupMembers(groupId, userIds, duration) {
  return request({
    url: `/api/admin/groups/${groupId}/members/batch-mute`,
    method: 'put',
    data: { userIds, duration }
  })
}

/**
 * 批量解除群成员禁言
 * @param {Number} groupId - 群组ID
 * @param {Array} userIds - 用户ID列表
 * @returns {Promise}
 */
export function batchUnmuteGroupMembers(groupId, userIds) {
  return request({
    url: `/api/admin/groups/${groupId}/members/batch-unmute`,
    method: 'put',
    data: userIds
  })
}

/**
 * 转让群主
 * @param {Number} groupId - 群组ID
 * @param {Number} newOwnerId - 新群主用户ID
 * @returns {Promise}
 */
export function transferGroupOwner(groupId, newOwnerId) {
  return request({
    url: `/api/admin/groups/${groupId}/owner`,
    method: 'put',
    data: { newOwnerId }
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
 * 获取消息统计（管理后台）
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getMessageAdminStats(params) {
  return request({
    url: '/api/admin/stats/messages/types',
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

/**
 * 获取消息类型统计（管理后台）
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getMessageTypeStats(params) {
  return request({
    url: '/api/admin/stats/messages/types',
    method: 'get',
    params
  })
}

// ==================== 部门管理 ====================
// 后端接口路径: /api/im/organization

/**
 * 获取部门列表（树形）
 * @returns {Promise}
 */
export function getDepartmentTree() {
  return request({
    url: '/api/im/organization/department/tree',
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
    url: `/api/im/organization/department/${id}`,
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
    url: '/api/im/organization/department',
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
    url: '/api/im/organization/department',
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
    url: `/api/im/organization/department/${id}`,
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
    url: `/api/im/organization/department/${id}/members`,
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
    url: `/api/im/organization/department/${id}/leader/${leaderId}`,
    method: 'put'
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
    url: `/api/im/organization/department/${id}/move/${parentId}`,
    method: 'put'
  })
}

/**
 * 添加成员到部门
 * @param {Number} id - 部门ID
 * @param {Object} data - { userId, isPrimary }
 * @returns {Promise}
 */
export function addDepartmentMembers(id, data) {
  return request({
    url: '/api/im/organization/department/member',
    method: 'post',
    data: { departmentId: id, ...data }
  })
}

/**
 * 移除部门成员
 * @param {Number} id - 部门ID
 * @param {Number} userId - 用户ID
 * @returns {Promise}
 */
export function removeDepartmentMember(id, userId) {
  return request({
    url: `/api/im/organization/department/${id}/member/${userId}`,
    method: 'delete'
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
 * 后端接口路径: /api/admin/roles/permissions
 * @returns {Promise}
 */
export function getPermissionList() {
  return request({
    url: '/api/admin/roles/permissions',
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

/**
 * 添加成员到角色
 * @param {Number} id - 角色ID
 * @param {Array} userIds - 用户ID列表
 * @returns {Promise}
 */
export function addRoleMembers(id, userIds) {
  return request({
    url: `/api/admin/roles/${id}/members`,
    method: 'post',
    data: userIds
  })
}

/**
 * 移除角色成员
 * @param {Number} id - 角色ID
 * @param {Number} userId - 用户ID
 * @returns {Promise}
 */
export function removeRoleMember(id, userId) {
  return request({
    url: `/api/admin/roles/${id}/members/${userId}`,
    method: 'delete'
  })
}

// ==================== 系统配置 ====================
// 后端接口路径: /api/admin/config

/**
 * 获取系统配置
 * @returns {Promise}
 */
export function getSystemConfig() {
  return request({
    url: '/api/admin/config/all',
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
    url: '/api/admin/config/update',
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
    url: '/api/admin/config/sensitive-words',
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
    url: '/api/admin/config/sensitive-words',
    method: 'post',
    data
  })
}

// ==================== 审计日志 ====================
// 后端接口路径: /api/im/audit

/**
 * 获取审计日志列表
 * @param {Object} params - 查询参数 { pageNum, pageSize, userId, operationType, operationResult, startTime, endTime }
 * @returns {Promise}
 */
export function getAuditLogList(params) {
  return request({
    url: '/api/im/audit/list',
    method: 'get',
    params
  })
}

/**
 * 获取审计日志详情
 * @param {Number} id - 日志ID
 * @returns {Promise}
 */
export function getAuditLogDetail(id) {
  return request({
    url: `/api/im/audit/${id}`,
    method: 'get'
  })
}

/**
 * 获取审计统计信息
 * @param {Object} params - { startTime, endTime }
 * @returns {Promise}
 */
export function getAuditStatistics(params) {
  return request({
    url: '/api/im/audit/statistics',
    method: 'get',
    params
  })
}

/**
 * 获取用户操作日志
 * @param {Number} userId - 用户ID
 * @param {Object} params - { startTime, endTime }
 * @returns {Promise}
 */
export function getUserAuditLogs(userId, params) {
  return request({
    url: `/api/im/audit/user/${userId}`,
    method: 'get',
    params
  })
}

/**
 * 删除过期日志
 * @param {String} beforeDate - 删除此日期之前的日志 (ISO格式)
 * @returns {Promise}
 */
export function deleteExpiredLogs(beforeDate) {
  return request({
    url: '/api/im/audit/clean',
    method: 'delete',
    params: { beforeDate }
  })
}

// ==================== 数据备份 ====================
// 后端接口路径: /api/im/backup

/**
 * 获取备份列表
 * @returns {Promise}
 */
export function getBackupList() {
  return request({
    url: '/api/im/backup/list',
    method: 'get'
  })
}

/**
 * 获取备份详情
 * @param {Number} id - 备份ID
 * @returns {Promise}
 */
export function getBackupDetail(id) {
  return request({
    url: `/api/im/backup/${id}`,
    method: 'get'
  })
}

/**
 * 创建备份
 * @param {String} description - 备份描述
 * @returns {Promise}
 */
export function createBackup(description) {
  return request({
    url: '/api/im/backup/create',
    method: 'post',
    params: { description }
  })
}

/**
 * 恢复备份
 * @param {Number} id - 备份ID
 * @returns {Promise}
 */
export function restoreBackup(id) {
  return request({
    url: `/api/im/backup/restore/${id}`,
    method: 'post'
  })
}

/**
 * 删除备份
 * @param {Number} id - 备份ID
 * @returns {Promise}
 */
export function deleteBackup(id) {
  return request({
    url: `/api/im/backup/${id}`,
    method: 'delete'
  })
}

/**
 * 获取备份统计信息
 * @returns {Promise}
 */
export function getBackupStatistics() {
  return request({
    url: '/api/im/backup/statistics',
    method: 'get'
  })
}

/**
 * 导出用户数据
 * @param {Number} userId - 用户ID
 * @returns {Promise}
 */
export function exportUserData(userId) {
  return request({
    url: `/api/im/backup/export/user/${userId}`,
    method: 'get'
  })
}

// ==================== 系统监控 ====================
// 后端接口路径: 待实现 /api/admin/monitor

/**
 * 获取系统监控数据
 * @returns {Promise}
 */
export function getSystemMonitor() {
  return request({
    url: '/api/admin/monitor/system',
    method: 'get'
  })
}

/**
 * 获取在线用户列表
 * @returns {Promise}
 */
export function getOnlineUsers() {
  return request({
    url: '/api/admin/monitor/online-users',
    method: 'get'
  })
}

/**
 * 获取在线用户统计
 * @returns {Promise}
 */
export function getOnlineUserStats() {
  return request({
    url: '/api/admin/monitor/online-stats',
    method: 'get'
  })
}

/**
 * 踢出用户（强制下线）
 * @param {Number} userId - 用户ID
 * @returns {Promise}
 */
export function kickUser(userId) {
  return request({
    url: `/api/admin/monitor/kick/${userId}`,
    method: 'delete'
  })
}

/**
 * 根据会话ID踢出用户
 * @param {String} sessionId - 会话ID
 * @returns {Promise}
 */
export function kickBySessionId(sessionId) {
  return request({
    url: `/api/admin/monitor/kick/session/${sessionId}`,
    method: 'delete'
  })
}

/**
 * 获取系统性能指标
 * @returns {Promise}
 */
export function getSystemPerformance() {
  return request({
    url: '/api/admin/monitor/performance',
    method: 'get'
  })
}

// ==================== 文件管理 ====================
// 后端接口路径: 待实现 /api/admin/files

/**
 * 获取文件列表
 * @param {Object} params - 查询参数 { pageNum, pageSize, fileName, fileType, uploaderId, startTime, endTime }
 * @returns {Promise}
 */
export function getFileList(params) {
  return request({
    url: '/api/admin/files',
    method: 'get',
    params
  })
}

/**
 * 获取文件详情
 * @param {Number} id - 文件ID
 * @returns {Promise}
 */
export function getFileDetail(id) {
  return request({
    url: `/api/admin/files/${id}`,
    method: 'get'
  })
}

/**
 * 删除文件
 * @param {Number} id - 文件ID
 * @returns {Promise}
 */
export function deleteFile(id) {
  return request({
    url: `/api/admin/files/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除文件
 * @param {Array} ids - 文件ID列表
 * @returns {Promise}
 */
export function batchDeleteFiles(ids) {
  return request({
    url: '/api/admin/files/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 获取文件统计
 * @returns {Promise}
 */
export function getFileStatistics() {
  return request({
    url: '/api/admin/files/statistics',
    method: 'get'
  })
}
