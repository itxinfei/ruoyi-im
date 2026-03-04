/**
 * 视频通话相关 API
 */
import request from '../request'

/**
 * 发起通话
 * @param {Object} data - 通话数据
 * @param {number} data.calleeId - 接收者ID
 * @param {number} data.conversationId - 会话ID（可选）
 * @param {string} data.callType - 通话类型 VIDEO/VOICE
 * @returns {Promise}
 */
export function initiateCall(data) {
    return request({
        url: '/api/im/video-call/initiate',
        method: 'post',
        params: {
            calleeId: data.calleeId,
            conversationId: data.conversationId,
            callType: data.callType || 'VIDEO'
        }
    })
}

/**
 * 接听通话
 * @param {number} callId - 通话ID
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
 * @param {number} callId - 通话ID
 * @param {string} reason - 拒绝原因（可选）
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
 * @param {number} callId - 通话ID
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
 * @param {number} callId - 通话ID
 * @returns {Promise}
 */
export function getCallInfo(callId) {
    return request({
        url: `/api/im/video-call/${callId}`,
        method: 'get'
    })
}

/**
 * 获取当前通话状态
 * @returns {Promise}
 */
export function getActiveCall() {
    return request({
        url: '/api/im/video-call/active',
        method: 'get'
    })
}

/**
 * 发送WebRTC信令
 * @param {Object} data - 信令数据
 * @param {number} data.callId - 通话ID
 * @param {string} data.signalType - 信令类型 offer/answer/candidate
 * @param {Object} data.signalData - 信令数据
 * @returns {Promise}
 */
export function sendSignal(data) {
    return request({
        url: '/api/im/video-call/signal',
        method: 'post',
        params: {
            callId: data.callId,
            signalType: data.signalType
        },
        data: data.signalData
    })
}

/**
 * 获取通话历史
 * @param {number} limit - 数量限制
 * @returns {Promise}
 */
export function getCallHistory(limit = 20) {
    return request({
        url: '/api/im/video-call/history',
        method: 'get',
        params: { limit }
    })
}

// ==================== 群组多人通话接口 ====================

/**
 * 发起群组通话
 * @param {Object} data - 群组通话数据
 * @param {number} data.conversationId - 会话ID
 * @param {string} data.callType - 通话类型 VIDEO/VOICE
 * @param {number} data.maxParticipants - 最大参与者数（默认9）
 * @param {number[]} data.invitedUserIds - 被邀请用户ID列表
 * @returns {Promise}
 */
export function initiateGroupCall(data) {
    return request({
        url: '/api/im/video-call/group/initiate',
        method: 'post',
        params: {
            conversationId: data.conversationId,
            callType: data.callType || 'VIDEO',
            maxParticipants: data.maxParticipants || 9
        },
        data: data.invitedUserIds
    })
}

/**
 * 加入群组通话
 * @param {number} callId - 通话ID
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
 * @param {number} callId - 通话ID
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
 * @param {number} callId - 通话ID
 * @returns {Promise}
 */
export function getCallParticipants(callId) {
    return request({
        url: `/api/im/video-call/group/${callId}/participants`,
        method: 'get'
    })
}

/**
 * 切换麦克风状态
 * @param {number} callId - 通话ID
 * @param {boolean} muted - 是否静音
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
 * @param {number} callId - 通话ID
 * @param {boolean} cameraOff - 是否关闭摄像头
 * @returns {Promise}
 */
export function toggleCamera(callId, cameraOff) {
    return request({
        url: `/api/im/video-call/group/${callId}/camera`,
        method: 'post',
        params: { cameraOff }
    })
}