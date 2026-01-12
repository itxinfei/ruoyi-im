/**
 * 视频通话API
 * @module api/im/video-call
 */
import request from '@/utils/request'

/**
 * 发起通话
 * @param {Object} data - 通话数据
 * @param {number} data.calleeId - 接收者ID
 * @param {number} [data.conversationId] - 会话ID
 * @param {string} [data.callType] - 通话类型 VIDEO/VOICE
 * @returns {Promise<Result<number>>} 返回通话ID
 */
export function initiateCall(data) {
  return request({
    url: '/api/im/video-call/initiate',
    method: 'post',
    params: {
      calleeId: data.calleeId,
      conversationId: data.conversationId,
      callType: data.callType || 'VIDEO',
    },
  })
}

/**
 * 接听通话
 * @param {number} callId - 通话ID
 * @returns {Promise<Result<void>>}
 */
export function acceptCall(callId) {
  return request({
    url: `/api/im/video-call/${callId}/accept`,
    method: 'post',
  })
}

/**
 * 拒绝通话
 * @param {number} callId - 通话ID
 * @param {string} [reason] - 拒绝原因
 * @returns {Promise<Result<void>>}
 */
export function rejectCall(callId, reason) {
  return request({
    url: `/api/im/video-call/${callId}/reject`,
    method: 'post',
    params: { reason },
  })
}

/**
 * 结束通话
 * @param {number} callId - 通话ID
 * @returns {Promise<Result<void>>}
 */
export function endCall(callId) {
  return request({
    url: `/api/im/video-call/${callId}/end`,
    method: 'post',
  })
}

/**
 * 获取通话信息
 * @param {number} callId - 通话ID
 * @returns {Promise<Result<CallInfo>>}
 */
export function getCallInfo(callId) {
  return request({
    url: `/api/im/video-call/${callId}`,
    method: 'get',
  })
}

/**
 * 获取用户当前通话状态
 * @returns {Promise<Result<CallInfo>>}
 */
export function getUserActiveCall() {
  return request({
    url: '/api/im/video-call/active',
    method: 'get',
  })
}

/**
 * 发送WebRTC信令
 * @param {Object} data - 信令数据
 * @param {number} data.callId - 通话ID
 * @param {string} data.signalType - 信令类型 offer/answer/ice-candidate
 * @param {string} data.signalData - 信令数据（JSON字符串）
 * @returns {Promise<Result<void>>}
 */
export function sendSignal(data) {
  return request({
    url: '/api/im/video-call/signal',
    method: 'post',
    params: {
      callId: data.callId,
      signalType: data.signalType,
    },
    data: data.signalData,
    headers: {
      'Content-Type': 'application/sdp',
    },
  })
}

/**
 * 获取通话历史
 * @param {number} [limit=20] - 限制数量
 * @returns {Promise<Result<CallInfo[]>>}
 */
export function getCallHistory(limit = 20) {
  return request({
    url: '/api/im/video-call/history',
    method: 'get',
    params: { limit },
  })
}

// 默认导出
export default {
  initiateCall,
  acceptCall,
  rejectCall,
  endCall,
  getCallInfo,
  getUserActiveCall,
  sendSignal,
  getCallHistory,
}
