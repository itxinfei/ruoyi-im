/**
 * 视频通话相关 API
 * 对应后端 ImVideoCallController
 * @author ruoyi
 */
import request from '../request'

/**
 * 发起视频通话
 * @param {Object} data - 通话数据
 * @param {number} data.receiverId - 接收者ID
 * @param {string} data.callType - 通话类型 VIDEO/AUDIO
 * @param {number} data.conversationId - 会话ID
 * @returns {Promise}
 */
export function initiateVideoCall(data) {
  return request({
    url: '/api/im/video-calls/initiate',
    method: 'post',
    data
  })
}

/**
 * 接受视频通话
 * @param {number} callId - 通话ID
 * @returns {Promise}
 */
export function acceptVideoCall(callId) {
  return request({
    url: `/api/im/video-calls/${callId}/accept`,
    method: 'post'
  })
}

/**
 * 拒绝视频通话
 * @param {number} callId - 通话ID
 * @returns {Promise}
 */
export function rejectVideoCall(callId) {
  return request({
    url: `/api/im/video-calls/${callId}/reject`,
    method: 'post'
  })
}

/**
 * 结束视频通话
 * @param {number} callId - 通话ID
 * @returns {Promise}
 */
export function endVideoCall(callId) {
  return request({
    url: `/api/im/video-calls/${callId}/end`,
    method: 'post'
  })
}

/**
 * 获取通话记录
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {number} params.userId - 用户ID（可选）
 * @returns {Promise}
 */
export function getVideoCallHistory(params) {
  return request({
    url: '/api/im/video-calls/history',
    method: 'get',
    params
  })
}

/**
 * 获取当前通话信息
 * @param {number} callId - 通话ID
 * @returns {Promise}
 */
export function getCurrentCallInfo(callId) {
  return request({
    url: `/api/im/video-calls/${callId}`,
    method: 'get'
  })
}

/**
 * 邀请用户加入通话
 * @param {Object} data - 邀请数据
 * @param {number} data.callId - 通话ID
 * @param {Array<number>} data.invitedUserIds - 被邀请用户ID列表
 * @returns {Promise}
 */
export function inviteToVideoCall(data) {
  return request({
    url: '/api/im/video-calls/invite',
    method: 'post',
    data
  })
}

/**
 * 静音/取消静音
 * @param {Object} data - 静音数据
 * @param {number} data.callId - 通话ID
 * @param {boolean} data.muted - 是否静音
 * @returns {Promise}
 */
export function toggleMute(data) {
  return request({
    url: '/api/im/video-calls/toggle-mute',
    method: 'post',
    data
  })
}

/**
 * 切换摄像头
 * @param {Object} data - 切换数据
 * @param {number} data.callId - 通话ID
 * @param {boolean} data.videoOn - 是否开启视频
 * @returns {Promise}
 */
export function toggleVideo(data) {
  return request({
    url: '/api/im/video-calls/toggle-video',
    method: 'post',
    data
  })
}

/**
 * 获取通话质量报告
 * @param {number} callId - 通话ID
 * @returns {Promise}
 */
export function getCallQualityReport(callId) {
  return request({
    url: `/api/im/video-calls/${callId}/quality`,
    method: 'get'
  })
}

/**
 * 发送通话信令
 * @param {Object} data - 信令数据
 * @param {number} data.callId - 通话ID
 * @param {string} data.signalType - 信令类型
 * @param {Object} data.payload - 信令内容
 * @returns {Promise}
 */
export function sendCallSignal(data) {
  return request({
    url: '/api/im/video-calls/signal',
    method: 'post',
    data
  })
}

export default {
  initiateVideoCall,
  acceptVideoCall,
  rejectVideoCall,
  endVideoCall,
  getVideoCallHistory,
  getCurrentCallInfo,
  inviteToVideoCall,
  toggleMute,
  toggleVideo,
  getCallQualityReport,
  sendCallSignal
}