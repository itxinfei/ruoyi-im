/**
 * 视频会议相关 API
 */
import request from '../request'

/**
 * 获取会议列表
 * @param {string} status - 会议状态 SCHEDULED预定中 IN_PROGRESS进行中 COMPLETED已结束 CANCELLED已取消
 * @returns {Promise}
 */
export function getMeetingList(status) {
  return request({
    url: '/api/meeting-rooms/list',
    method: 'get',
    params: { status }
  })
}

/**
 * 获取会议详情
 * @param {number} meetingId - 会议ID
 * @returns {Promise}
 */
export function getMeetingDetail(meetingId) {
  return request({
    url: `/api/meeting-rooms/${meetingId}`,
    method: 'get'
  })
}

/**
 * 创建会议
 * @param {Object} data - 会议数据
 * @returns {Promise}
 */
export function createMeeting(data) {
  return request({
    url: '/api/meeting-rooms',
    method: 'post',
    data
  })
}

/**
 * 更新会议
 * @param {number} meetingId - 会议ID
 * @param {Object} data - 会议数据
 * @returns {Promise}
 */
export function updateMeeting(meetingId, data) {
  return request({
    url: `/api/meeting-rooms/${meetingId}`,
    method: 'put',
    data
  })
}

/**
 * 取消会议
 * @param {number} meetingId - 会议ID
 * @returns {Promise}
 */
export function cancelMeeting(meetingId) {
  return request({
    url: `/api/meeting-rooms/${meetingId}/cancel`,
    method: 'post'
  })
}

/**
 * 删除会议
 * @param {number} meetingId - 会议ID
 * @returns {Promise}
 */
export function deleteMeeting(meetingId) {
  return request({
    url: `/api/meeting-rooms/${meetingId}`,
    method: 'delete'
  })
}

/**
 * 开始会议
 * @param {number} meetingId - 会议ID
 * @returns {Promise}
 */
export function startMeeting(meetingId) {
  return request({
    url: `/api/meeting-rooms/${meetingId}/start`,
    method: 'post'
  })
}

/**
 * 结束会议
 * @param {number} meetingId - 会议ID
 * @returns {Promise}
 */
export function endMeeting(meetingId) {
  return request({
    url: `/api/meeting-rooms/${meetingId}/end`,
    method: 'post'
  })
}

/**
 * 加入会议
 * @param {number} meetingId - 会议ID
 * @param {string} password - 会议密码
 * @returns {Promise}
 */
export function joinMeeting(meetingId, password) {
  return request({
    url: `/api/meeting-rooms/${meetingId}/join`,
    method: 'post',
    params: { password }
  })
}

/**
 * 离开会议
 * @param {number} meetingId - 会议ID
 * @returns {Promise}
 */
export function leaveMeeting(meetingId) {
  return request({
    url: `/api/meeting-rooms/${meetingId}/leave`,
    method: 'post'
  })
}

/**
 * 获取会议参与者列表
 * @param {number} meetingId - 会议ID
 * @returns {Promise}
 */
export function getParticipants(meetingId) {
  return request({
    url: `/api/meeting-rooms/${meetingId}/participants`,
    method: 'get'
  })
}

/**
 * 邀请用户加入会议
 * @param {number} meetingId - 会议ID
 * @param {Array} userIds - 用户ID列表
 * @returns {Promise}
 */
export function inviteUsers(meetingId, userIds) {
  return request({
    url: `/api/meeting-rooms/${meetingId}/invite`,
    method: 'post',
    data: userIds
  })
}

/**
 * 移除参与者
 * @param {number} meetingId - 会议ID
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function removeParticipant(meetingId, userId) {
  return request({
    url: `/api/meeting-rooms/${meetingId}/remove`,
    method: 'post',
    params: { userId }
  })
}

/**
 * 静音/取消静音
 * @param {number} meetingId - 会议ID
 * @param {number} userId - 用户ID
 * @param {boolean} muted - 是否静音
 * @returns {Promise}
 */
export function muteParticipant(meetingId, userId, muted) {
  return request({
    url: `/api/meeting-rooms/${meetingId}/mute`,
    method: 'post',
    params: { userId, muted }
  })
}
