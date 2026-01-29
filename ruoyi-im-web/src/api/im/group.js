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
