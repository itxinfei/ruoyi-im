/**
 * 群组相关 API
 */
import request from '../request'

/**
 * 获取群组列表
 * @returns {Promise}
 */
export function getGroups() {
  return request({
    url: '/api/im/group/list',
    method: 'get'
  })
}

/**
 * 获取群组详情
 * @param {number} groupId - 群组ID
 * @returns {Promise}
 */
export function getGroup(groupId) {
  return request({
    url: `/api/im/group/${groupId}`,
    method: 'get'
  })
}

/**
 * 创建群组
 * @param {Object} data - 群组数据
 * @param {string} data.name - 群组名称
 * @param {string} data.avatar - 群组头像
 * @param {Array} data.memberIds - 成员ID列表
 * @returns {Promise}
 */
export function createGroup(data) {
  return request({
    url: '/api/im/group/create',
    method: 'post',
    data
  })
}

/**
 * 解散群组
 * @param {number} groupId - 群组ID
 * @returns {Promise}
 */
export function dismissGroup(groupId) {
  return request({
    url: `/api/im/group/${groupId}`,
    method: 'delete'
  })
}

/**
 * 退出群组
 * @param {number} groupId - 群组ID
 * @returns {Promise}
 */
export function leaveGroup(groupId) {
  return request({
    url: `/api/im/group/${groupId}/quit`,
    method: 'post'
  })
}

/**
 * 添加群成员
 * @param {Object} data - 成员数据
 * @param {number} data.groupId - 群组ID
 * @param {Array} data.userIds - 用户ID列表
 * @returns {Promise}
 */
export function addGroupMembers(groupId, userIds) {
  return request({
    url: `/api/im/group/${groupId}/members`,
    method: 'post',
    data: userIds
  })
}

/**
 * 移除群成员
 * @param {Object} data - 成员数据
 * @param {number} data.groupId - 群组ID
 * @param {number} data.userId - 用户ID
 * @returns {Promise}
 */
export function removeGroupMember(groupId, userIds) {
  return request({
    url: `/api/im/group/${groupId}/members`,
    method: 'delete',
    data: userIds
  })
}

/**
 * 设置群管理员
 * @param {Object} data - 管理员数据
 * @param {number} data.groupId - 群组ID
 * @param {number} data.userId - 用户ID
 * @param {boolean} data.isAdmin - 是否为管理员
 * @returns {Promise}
 */
export function setGroupAdmin(groupId, targetUserId, isAdmin) {
  return request({
    url: `/api/im/group/${groupId}/admin/${targetUserId}`,
    method: 'put',
    params: { isAdmin }
  })
}

/**
 * 转让群主
 * @param {Object} data - 转让数据
 * @param {number} data.groupId - 群组ID
 * @param {number} data.newOwnerId - 新群主ID
 * @returns {Promise}
 */
export function transferGroupOwner(groupId, newOwnerId) {
  return request({
    url: `/api/im/group/${groupId}/transfer/${newOwnerId}`,
    method: 'put'
  })
}

/**
 * 更新群组信息
 * @param {Object} data - 群组信息
 * @param {number} data.groupId - 群组ID
 * @param {string} data.name - 群组名称
 * @param {string} data.avatar - 群组头像
 * @param {string} data.notice - 群公告
 * @returns {Promise}
 */
export function updateGroup(groupId, data) {
  return request({
    url: `/api/im/group/${groupId}`,
    method: 'put',
    data
  })
}

/**
 * 获取群成员列表
 * @param {number} groupId - 群组ID
 * @returns {Promise}
 */
export function getGroupMembers(groupId) {
  return request({
    url: `/api/im/group/${groupId}/members`,
    method: 'get'
  })
}

/**
 * 设置群禁言
 * @param {Object} data - 禁言数据
 * @param {number} data.groupId - 群组ID
 * @param {boolean} data.isAllMuted - 是否全员禁言
 * @returns {Promise}
 */
export function setGroupMute(data) {
  return request({
    url: '/api/im/group/mute/set',
    method: 'put',
    data
  })
}

/**
 * 设置成员禁言
 * @param {Object} data - 禁言数据
 * @param {number} data.groupId - 群组ID
 * @param {number} data.userId - 用户ID
 * @param {number} data.duration - 禁言时长（秒）
 * @returns {Promise}
 */
export function muteGroupMember(groupId, targetUserId, duration) {
  return request({
    url: `/api/im/group/${groupId}/mute/${targetUserId}`,
    method: 'put',
    params: { duration }
  })
}

/**
 * 获取共同群组
 * 获取当前用户与指定用户共同加入的群组列表
 * @param {number} targetUserId - 目标用户ID
 * @returns {Promise}
 */
export function getCommonGroups(targetUserId) {
  return request({
    url: `/api/im/group/common/${targetUserId}`,
    method: 'get'
  })
}

/**
 * 加入群组
 * 通过群组ID或群组码申请加入群组
 * @param {string} groupCode - 群组ID或群组码
 * @returns {Promise}
 */
export function joinGroup(groupCode) {
  return request({
    url: '/api/im/group/join',
    method: 'post',
    data: { groupCode }
  })
}

/**
 * 获取群二维码
 * 获取群组的二维码图片供扫码加入
 * @param {number} groupId - 群组ID
 * @returns {Promise}
 */
export function getGroupQrcode(groupId) {
  return request({
    url: `/api/im/group/${groupId}/qrcode`,
    method: 'get'
  })
}

/**
 * 刷新群二维码
 * 重新生成群二维码
 * @param {number} groupId - 群组ID
 * @returns {Promise}
 */
export function refreshGroupQrcode(groupId) {
  return request({
    url: `/api/im/group/${groupId}/qrcode/refresh`,
    method: 'post'
  })
}

/**
 * 获取群公告列表
 * @param {number} groupId - 群组ID
 * @returns {Promise}
 */
export function getGroupAnnouncements(groupId) {
  return request({
    url: '/api/im/group/announcement/list',
    method: 'get',
    params: { groupId }
  })
}

/**
 * 创建群公告
 * @param {Object} data - 公告数据
 * @param {number} data.groupId - 群组ID
 * @param {string} data.title - 公告标题
 * @param {string} data.content - 公告内容
 * @param {boolean} data.isPinned - 是否置顶
 * @returns {Promise}
 */
export function createGroupAnnouncement(data) {
  return request({
    url: '/api/im/group/announcement/create',
    method: 'post',
    data
  })
}

/**
 * 更新群公告
 * @param {number} announcementId - 公告ID
 * @param {string} content - 公告内容
 * @returns {Promise}
 */
export function updateGroupAnnouncement(announcementId, content) {
  return request({
    url: `/api/im/group/announcement/${announcementId}`,
    method: 'put',
    params: { content }
  })
}

/**
 * 删除群公告
 * @param {number} announcementId - 公告ID
 * @returns {Promise}
 */
export function deleteGroupAnnouncement(announcementId) {
  return request({
    url: `/api/im/group/announcement/${announcementId}`,
    method: 'delete'
  })
}

/**
 * 撤回群公告
 * @param {number} announcementId - 公告ID
 * @returns {Promise}
 */
export function recallGroupAnnouncement(announcementId) {
  return request({
    url: `/api/im/group/announcement/recall/${announcementId}`,
    method: 'put'
  })
}

/**
 * 设置群公告置顶状态
 * @param {number} announcementId - 公告ID
 * @param {boolean} isPinned - 是否置顶
 * @returns {Promise}
 */
export function setAnnouncementPinned(announcementId, isPinned) {
  return request({
    url: `/api/im/group/announcement/pin/${announcementId}`,
    method: 'put',
    params: { isPinned }
  })
}

// ==================== 群组权限管理 ====================

/**
 * 获取群组权限配置
 * @param {number} groupId - 群组ID
 * @returns {Promise} 权限配置列表
 */
export function getGroupPermissions(groupId) {
  return request({
    url: `/api/im/group/${groupId}/permissions`,
    method: 'get'
  })
}

/**
 * 更新群组角色权限
 * 仅群主可以调用
 * @param {number} groupId - 群组ID
 * @param {string} role - 角色 (OWNER/ADMIN/MEMBER)
 * @param {Object} permissions - 权限配置
 * @param {number} permissions.canInvite - 邀请成员权限
 * @param {number} permissions.canRemove - 移除成员权限
 * @param {number} permissions.canMute - 禁言成员权限
 * @param {number} permissions.canAnnounce - 发布公告权限
 * @param {number} permissions.canUpload - 上传文件权限
 * @param {number} permissions.canEditGroup - 修改群信息权限
 * @param {number} permissions.canKick - 踢人权限
 * @param {number} permissions.canSetAdmin - 设置管理员权限
 * @param {number} permissions.canDisband - 解散群组权限
 * @returns {Promise}
 */
export function updateGroupPermissions(groupId, role, permissions) {
  return request({
    url: `/api/im/group/${groupId}/permissions/${role}`,
    method: 'put',
    data: permissions
  })
}

/**
 * 重置群组权限为默认配置
 * 仅群主可以调用
 * @param {number} groupId - 群组ID
 * @returns {Promise}
 */
export function resetGroupPermissions(groupId) {
  return request({
    url: `/api/im/group/${groupId}/permissions/reset`,
    method: 'post'
  })
}

/**
 * 检查当前用户是否有指定权限
 * @param {number} groupId - 群组ID
 * @param {string} permission - 权限名称 (canInvite/canRemove/canMute等)
 * @returns {Promise} { hasPermission: boolean, permission: string }
 */
export function checkGroupPermission(groupId, permission) {
  return request({
    url: `/api/im/group/${groupId}/permission/check`,
    method: 'get',
    params: { permission }
  })
}

/**
 * 获取当前用户在群组中的角色
 * @param {number} groupId - 群组ID
 * @returns {Promise} { isOwner: boolean, isAdminOrOwner: boolean }
 */
export function getUserGroupRole(groupId) {
  return request({
    url: `/api/im/group/${groupId}/role`,
    method: 'get'
  })
}

/**
 * 批量检查多个权限
 * @param {number} groupId - 群组ID
 * @param {Array<string>} permissions - 权限名称列表
 * @returns {Promise} Map<permission, boolean>
 */
export function checkMultiplePermissions(groupId, permissions) {
  return Promise.all(
    permissions.map(permission =>
      checkGroupPermission(groupId, permission).then(res => ({
        permission,
        hasPermission: res.data?.hasPermission || false
      }))
    )
  ).then(results => {
    const permissionMap = {}
    results.forEach(({ permission, hasPermission }) => {
      permissionMap[permission] = hasPermission
    })
    return permissionMap
  })
}
