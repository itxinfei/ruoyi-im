/**
 * 视频通话 API
 * @author ruoyi
 */

import request from '../request'

// ==================== 单人通话 ====================

/**
 * 发起通话
 * @param {Object} data - 通话参数
 * @param {Number} data.calleeId - 接收者ID
 * @param {Number} [data.conversationId] - 会话ID
 * @param {String} [data.callType] - 通话类型: VOICE/VIDEO
 * @returns {Promise}
 */
export function initiateCall(data) {
  return request({
    url: '/api/im/video-call/initiate',
    method: 'post',
    params: data
  })
}

/**
 * 接听通话
 * @param {Number} callId - 通话ID
 * @returns {Promise}
 */
export function acceptCall(callId) {
  return request({
    url: `/api/im/video-call/${callId}/accept`,
    method: 'post'
  })
}

/**
 * 拒绝通话
 * @param {Number} callId - 通话ID
 * @param {String} [reason] - 拒绝原因
 * @returns {Promise}
 */
export function rejectCall(callId, reason) {
  return request({
    url: `/api/im/video-call/${callId}/reject`,
    method: 'post',
    params: { reason }
  })
}

/**
 * 结束通话
 * @param {Number} callId - 通话ID
 * @returns {Promise}
 */
export function endCall(callId) {
  return request({
    url: `/api/im/video-call/${callId}/end`,
    method: 'post'
  })
}

/**
 * 获取通话信息
 * @param {Number} callId - 通话ID
 * @returns {Promise}
 */
export function getCallInfo(callId) {
  return request({
    url: `/api/im/video-call/${callId}`,
    method: 'get'
  })
}

/**
 * 获取用户当前通话状态
 * @returns {Promise}
 */
export function getUserActiveCall() {
  return request({
    url: '/api/im/video-call/active',
    method: 'get'
  })
}

/**
 * 发送WebRTC信令
 * @param {Number} callId - 通话ID
 * @param {String} signalType - 信令类型: offer/answer/ice-candidate
 * @param {String} signalData - 信令数据
 * @returns {Promise}
 */
export function sendSignal(callId, signalType, signalData) {
  return request({
    url: '/api/im/video-call/signal',
    method: 'post',
    params: { callId, signalType },
    data: signalData,
    headers: { 'Content-Type': 'application/json' }
  })
}

/**
 * 获取通话历史
 * @param {Number} [limit] - 限制数量
 * @returns {Promise}
 */
export function getCallHistory(limit = 20) {
  return request({
    url: '/api/im/video-call/history',
    method: 'get',
    params: { limit }
  })
}

// ==================== 群组通话 ====================

/**
 * 发起群组通话
 * @param {Object} data - 通话参数
 * @param {Number} data.conversationId - 会话ID
 * @param {String} [data.callType] - 通话类型
 * @param {Number} [data.maxParticipants] - 最大参与者数
 * @param {Array<Number>} data.invitedUserIds - 被邀请用户ID列表
 * @returns {Promise}
 */
export function initiateGroupCall(data) {
  return request({
    url: '/api/im/video-call/group/initiate',
    method: 'post',
    data
  })
}

/**
 * 加入群组通话
 * @param {Number} callId - 通话ID
 * @returns {Promise}
 */
export function joinGroupCall(callId) {
  return request({
    url: `/api/im/video-call/group/${callId}/join`,
    method: 'post'
  })
}

/**
 * 离开群组通话
 * @param {Number} callId - 通话ID
 * @returns {Promise}
 */
export function leaveGroupCall(callId) {
  return request({
    url: `/api/im/video-call/group/${callId}/leave`,
    method: 'post'
  })
}

/**
 * 获取通话参与者列表
 * @param {Number} callId - 通话ID
 * @returns {Promise}
 */
export function getParticipants(callId) {
  return request({
    url: `/api/im/video-call/group/${callId}/participants`,
    method: 'get'
  })
}

/**
 * 切换麦克风状态
 * @param {Number} callId - 通话ID
 * @param {Boolean} muted - 是否静音
 * @returns {Promise}
 */
export function toggleMute(callId, muted) {
  return request({
    url: `/api/im/video-call/group/${callId}/mute`,
    method: 'post',
    params: { muted }
  })
}

/**
 * 切换摄像头状态
 * @param {Number} callId - 通话ID
 * @param {Boolean} cameraOff - 是否关闭摄像头
 * @returns {Promise}
 */
export function toggleCamera(callId, cameraOff) {
  return request({
    url: `/api/im/video-call/group/${callId}/camera`,
    method: 'post',
    params: { cameraOff }
  })
}
