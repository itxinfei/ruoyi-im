/**
 * WebRTC 信令服务
 * 通过 WebSocket 在通话双方之间交换 SDP 和 ICE 候选
 */

class WebRTCSignalingService {
  constructor() {
    this.ws = null
    this.callbacks = {
      onOffer: null,
      onAnswer: null,
      onIceCandidate: null,
      onCallIncoming: null,
      onCallAccepted: null,
      onCallRejected: null,
      onCallEnded: null,
      onDataChannelMessage: null,
    }
  }

  /**
   * 初始化 WebSocket 连接
   */
  init(websocket) {
    this.ws = websocket

    // 监听 WebSocket 消息
    this.ws.addEventListener('message', (event) => {
      try {
        const data = JSON.parse(event.data)
        this.handleMessage(data)
      } catch (e) {
        console.error('解析信令消息失败:', e)
      }
    })
  }

  /**
   * 处理收到的信令消息
   */
  handleMessage(data) {
    switch (data.type) {
      case 'call':
        // 收到通话邀请
        if (this.callbacks.onCallIncoming) {
          this.callbacks.onCallIncoming(data.payload)
        }
        break

      case 'call-accepted':
        // 对方接听了通话
        if (this.callbacks.onCallAccepted) {
          this.callbacks.onCallAccepted(data.payload)
        }
        break

      case 'call-rejected':
        // 对方拒绝了通话
        if (this.callbacks.onCallRejected) {
          this.callbacks.onCallRejected(data.payload)
        }
        break

      case 'call-ended':
        // 对方结束了通话
        if (this.callbacks.onCallEnded) {
          this.callbacks.onCallEnded(data.payload)
        }
        break

      case 'offer':
        // 收到 SDP Offer
        if (this.callbacks.onOffer) {
          this.callbacks.onOffer(data.payload)
        }
        break

      case 'answer':
        // 收到 SDP Answer
        if (this.callbacks.onAnswer) {
          this.callbacks.onAnswer(data.payload)
        }
        break

      case 'ice-candidate':
        // 收到 ICE 候选
        if (this.callbacks.onIceCandidate) {
          this.callbacks.onIceCandidate(data.payload)
        }
        break

      case 'data-channel':
        // 收到数据通道消息
        if (this.callbacks.onDataChannelMessage) {
          this.callbacks.onDataChannelMessage(data.payload)
        }
        break

      default:
        console.log('未知的信令消息类型:', data.type)
    }
  }

  /**
   * 发起通话
   * @param {Object} callInfo - 通话信息
   * @param {string} callInfo.targetId - 目标用户ID
   * @param {string} callInfo.type - 通话类型 'video' | 'voice'
   * @param {Object} callInfo.caller - 发起者信息
   */
  sendCallInvite(callInfo) {
    this.send({
      type: 'call',
      payload: callInfo,
    })
  }

  /**
   * 接受通话
   * @param {string} callId - 通话ID
   * @param {string} targetId - 对方用户ID
   */
  sendCallAccepted(callId, targetId) {
    this.send({
      type: 'call-accepted',
      payload: { callId, targetId },
    })
  }

  /**
   * 拒绝通话
   * @param {string} callId - 通话ID
   * @param {string} targetId - 对方用户ID
   * @param {string} reason - 拒绝原因
   */
  sendCallRejected(callId, targetId, reason) {
    this.send({
      type: 'call-rejected',
      payload: { callId, targetId, reason },
    })
  }

  /**
   * 结束通话
   * @param {string} callId - 通话ID
   * @param {string} targetId - 对方用户ID
   * @param {string} reason - 结束原因
   */
  sendCallEnded(callId, targetId, reason) {
    this.send({
      type: 'call-ended',
      payload: { callId, targetId, reason },
    })
  }

  /**
   * 发送 SDP Offer
   * @param {RTCSessionDescription} offer - SDP Offer
   * @param {string} targetId - 目标用户ID
   */
  sendOffer(offer, targetId) {
    this.send({
      type: 'offer',
      payload: {
        sdp: offer.sdp,
        type: offer.type,
        targetId,
      },
    })
  }

  /**
   * 发送 SDP Answer
   * @param {RTCSessionDescription} answer - SDP Answer
   * @param {string} targetId - 目标用户ID
   */
  sendAnswer(answer, targetId) {
    this.send({
      type: 'answer',
      payload: {
        sdp: answer.sdp,
        type: answer.type,
        targetId,
      },
    })
  }

  /**
   * 发送 ICE 候选
   * @param {RTCIceCandidate} candidate - ICE 候选
   * @param {string} targetId - 目标用户ID
   */
  sendIceCandidate(candidate, targetId) {
    this.send({
      type: 'ice-candidate',
      payload: {
        candidate: candidate.candidate,
        sdpMid: candidate.sdpMid,
        sdpMLineIndex: candidate.sdpMLineIndex,
        targetId,
      },
    })
  }

  /**
   * 通过数据通道发送消息
   * @param {string} message - 消息内容
   * @param {string} targetId - 目标用户ID
   */
  sendDataChannelMessage(message, targetId) {
    this.send({
      type: 'data-channel',
      payload: {
        message,
        targetId,
      },
    })
  }

  /**
   * 发送消息
   */
  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
    } else {
      console.error('WebSocket未连接')
    }
  }

  /**
   * 注册回调
   */
  on(event, callback) {
    if (this.callbacks.hasOwnProperty('on' + event)) {
      this.callbacks['on' + event] = callback
    }
  }

  /**
   * 移除回调
   */
  off(event) {
    if (this.callbacks.hasOwnProperty('on' + event)) {
      this.callbacks['on' + event] = null
    }
  }

  /**
   * 清理
   */
  cleanup() {
    this.ws = null
    Object.keys(this.callbacks).forEach(key => {
      this.callbacks[key] = null
    })
  }
}

export default new WebRTCSignalingService()
