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
 * 设置成员禁言
 * @param {number} groupId - 群组ID
 * @param {number} userId - 用户ID
 * @param {number} duration - 禁言时长（秒），不传或传null表示取消禁言
 * @returns {Promise}
 */
export function muteGroupMember(groupId, userId, duration) {
  return request({
    url: `/api/im/group/${groupId}/mute/${userId}`,
    method: 'put',
    params: duration !== undefined && duration !== null ? { duration } : {}
  })
}

/**
 * 取消成员禁言
 * @param {number} groupId - 群组ID
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function unmuteGroupMember(groupId, userId) {
  return request({
    url: `/api/im/group/${groupId}/mute/${userId}`,
    method: 'put'
  })
}

/**
 * 获取群二维码
 * @param {Number} groupId - 群组ID
 * @returns {Promise} - 返回二维码图片 Base64 字符串
 */
export function getGroupQrCode(groupId) {
  return request({
    url: `/api/im/group/${groupId}/qrcode`,
    method: 'get'
  })
}

// ==================== 群公告 API ====================

/**
 * 获取群公告列表
 * @param {number} groupId - 群组ID
 * @returns {Promise}
 */
export function getGroupAnnouncements(groupId) {
  return request({
    url: `/api/im/group/announcement/list/${groupId}`,
    method: 'get'
  })
}

/**
 * 获取群最新公告
 * @param {number} groupId - 群组ID
 * @returns {Promise}
 */
export function getLatestAnnouncement(groupId) {
  return request({
    url: `/api/im/group/announcement/latest/${groupId}`,
    method: 'get'
  })
}

/**
 * 创建群公告
 * @param {Object} data - 公告数据
 * @param {number} data.groupId - 群组ID
 * @param {string} data.content - 公告内容
 * @param {string} data.type - 公告类型
 * @param {boolean} data.isPinned - 是否置顶
 * @returns {Promise}
 */
export function createGroupAnnouncement(data) {
  return request({
    url: '/api/im/group/announcement',
    method: 'post',
    data
  })
}

/**
 * 更新群公告
 * @param {number} announcementId - 公告ID
 * @param {string} content - 新内容
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
 * 置顶/取消置顶群公告
 * @param {number} announcementId - 公告ID
 * @param {boolean} isPinned - 是否置顶
 * @returns {Promise}
 */
export function setAnnouncementPinned(announcementId, isPinned) {
  return request({
    url: `/api/im/group/announcement/pin/${announcementId}`,
    method: 'put',
    params: { isPinned: isPinned ? 1 : 0 }
  })
}
