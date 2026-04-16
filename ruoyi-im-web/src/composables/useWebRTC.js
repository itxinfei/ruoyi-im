import { ref, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * WebRTC 音视频通话核心组合式函数
 * @param {Object} options 配置项
 * @param {Function} options.sendSignal 发送信令的回调函数
 * @param {Function} options.onConnectionStateChange 连接状态变化回调
 * @param {Function} options.onIceConnectionStateChange ICE连接状态变化回调
 * @param {HTMLVideoElement} options.localVideo 本地视频元素
 * @param {HTMLVideoElement} options.remoteVideo 远端视频元素
 * @param {HTMLAudioElement} options.remoteAudio 远端音频元素
 */
export function useWebRTC({ sendSignal, onConnectionStateChange, onIceConnectionStateChange, remoteVideo, remoteAudio }) {
  const peerConnection = ref(null)
  const isConnected = ref(false)
  let reconnectTimer = null
  let disconnectTimer = null
  let reconnectAttempts = 0
  const MAX_RECONNECT_ATTEMPTS = 3
  const SHORT_DISCONNECT_THRESHOLD = 3000 // 3秒内断线自动重连

  // ICE 服务器配置（生产环境建议使用 STUN/TURN 服务器）
  // TURN 服务器用于解决对称型 NAT 限制问题，保证内网用户也能建立通话
  const rtcConfig = {
    iceServers: [
      { urls: 'stun:stun.l.google.com:19302' },
      { urls: 'stun:stun1.l.google.com:19302' }
      // 可配置 TURN 服务器（需要替换为实际的 TURN 服务器地址和凭证）
      // { urls: 'turn:your-turn-server.com:3478', username: 'user', credential: 'pass' }
    ]
  }

  /**
   * 清理重连定时器
   */
  const clearReconnectTimers = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (disconnectTimer) {
      clearTimeout(disconnectTimer)
      disconnectTimer = null
    }
  }

  /**
   * 初始化 PeerConnection
   */
  const initPeerConnection = (callId, peerId) => {
    if (peerConnection.value) {
      closePeerConnection()
    }

    clearReconnectTimers()
    reconnectAttempts = 0

    const pc = new RTCPeerConnection(rtcConfig)

    // 监听 ICE 候选者
    pc.onicecandidate = (event) => {
      if (event.candidate) {
        sendSignal('candidate', {
          callId,
          toUserId: peerId,
          candidate: event.candidate
        })
      }
    }

    // 监听远端媒体流
    pc.ontrack = (event) => {
      const stream = event.streams[0]
      if (remoteVideo.value) {
        remoteVideo.value.srcObject = stream
      }
      if (remoteAudio.value) {
        remoteAudio.value.srcObject = stream
      }
      isConnected.value = true
      reconnectAttempts = 0
      clearReconnectTimers()
      onConnectionStateChange?.('connected')
    }

    // 监听连接状态
    pc.onconnectionstatechange = () => {
      console.log('WebRTC 连接状态:', pc.connectionState)
      onConnectionStateChange?.(pc.connectionState)

      if (pc.connectionState === 'connected') {
        isConnected.value = true
        reconnectAttempts = 0
        clearReconnectTimers()
      } else if (['disconnected', 'failed', 'closed'].includes(pc.connectionState)) {
        isConnected.value = false
      }
    }

    // 监听 ICE 连接状态（更精确的连接状态监测）
    pc.oniceconnectionstatechange = () => {
      console.log('ICE 连接状态:', pc.iceConnectionState)
      onIceConnectionStateChange?.(pc.iceConnectionState)

      if (pc.iceConnectionState === 'connected') {
        reconnectAttempts = 0
        clearReconnectTimers()
      } else if (pc.iceConnectionState === 'disconnected') {
        // 短时断线：启动计时器，3秒内恢复则不提示用户
        clearReconnectTimers()
        disconnectTimer = setTimeout(() => {
          // 3秒后仍未恢复，触发长时断线处理
          onConnectionStateChange?.('reconnecting')
        }, SHORT_DISCONNECT_THRESHOLD)
      } else if (pc.iceConnectionState === 'failed') {
        // ICE连接失败，尝试重连
        handleReconnect(callId, peerId)
      }
    }

    peerConnection.value = pc
    return pc
  }

  /**
   * 处理断线重连
   * 业务规则 C-009：短时断线(<3s)自动重连；长时断线提示重拨
   */
  const handleReconnect = (callId, peerId) => {
    clearReconnectTimers()

    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      console.warn('已达到最大重连次数，重连失败')
      onConnectionStateChange?.('reconnect_failed')
      return
    }

    reconnectAttempts++
    console.log(`正在进行第 ${reconnectAttempts} 次重连...`)

    // 尝试重新建立连接
    if (peerConnection.value) {
      // 尝试ICE重连
      peerConnection.value.createOffer()
        .then(offer => peerConnection.value.setLocalDescription(offer))
        .then(() => {
          sendSignal('offer', {
            callId,
            toUserId: peerId,
            sdp: peerConnection.value.localDescription,
            isReconnect: true
          })
        })
        .catch(e => {
          console.error('重连失败:', e)
          onConnectionStateChange?.('reconnect_failed')
        })
    }
  }

  /**
   * 主动发起重连（由用户触发）
   */
  const manualReconnect = (callId, peerId, localStream) => {
    reconnectAttempts = 0
    if (peerConnection.value) {
      closePeerConnection()
    }
    const pc = initPeerConnection(callId, peerId)
    localStream.getTracks().forEach(track => {
      pc.addTrack(track, localStream)
    })
    createOffer(callId, peerId, localStream)
  }

  /**
   * 发起呼叫 (Create Offer)
   */
  const createOffer = async (callId, peerId, localStream) => {
    const pc = initPeerConnection(callId, peerId)

    // 添加本地轨道
    localStream.getTracks().forEach(track => {
      pc.addTrack(track, localStream)
    })

    try {
      const offer = await pc.createOffer()
      await pc.setLocalDescription(offer)

      sendSignal('offer', {
        callId,
        toUserId: peerId,
        sdp: offer
      })
    } catch (e) {
      console.error('创建 Offer 失败:', e)
      ElMessage.error('建立通话连接失败')
    }
  }

  /**
   * 应答呼叫 (Create Answer)
   */
  const createAnswer = async (callId, peerId, offerSdp, localStream) => {
    const pc = initPeerConnection(callId, peerId)

    // 添加本地轨道
    localStream.getTracks().forEach(track => {
      pc.addTrack(track, localStream)
    })

    try {
      await pc.setRemoteDescription(new RTCSessionDescription(offerSdp))
      const answer = await pc.createAnswer()
      await pc.setLocalDescription(answer)

      sendSignal('answer', {
        callId,
        toUserId: peerId,
        sdp: answer
      })
    } catch (e) {
      console.error('创建 Answer 失败:', e)
    }
  }

  /**
   * 处理远端应答
   */
  const handleAnswer = async (answerSdp) => {
    if (peerConnection.value) {
      try {
        await peerConnection.value.setRemoteDescription(new RTCSessionDescription(answerSdp))
      } catch (e) {
        console.error('设置远端描述失败:', e)
      }
    }
  }

  /**
   * 处理 ICE 候选者
   */
  const handleCandidate = async (candidate) => {
    if (peerConnection.value) {
      try {
        await peerConnection.value.addIceCandidate(new RTCIceCandidate(candidate))
      } catch (e) {
        console.warn('添加 ICE 候选者失败:', e)
      }
    }
  }

  /**
   * 关闭连接
   */
  const closePeerConnection = () => {
    clearReconnectTimers()
    if (peerConnection.value) {
      peerConnection.value.close()
      peerConnection.value = null
    }
    isConnected.value = false
  }

  onUnmounted(() => {
    closePeerConnection()
  })

  return {
    isConnected,
    createOffer,
    createAnswer,
    handleAnswer,
    handleCandidate,
    closePeerConnection,
    manualReconnect
  }
}
