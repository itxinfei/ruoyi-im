import { ref, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * WebRTC 音视频通话核心组合式函数
 * @param {Object} options 配置项
 * @param {Function} options.sendSignal 发送信令的回调函数
 * @param {HTMLVideoElement} options.localVideo 本地视频元素
 * @param {HTMLVideoElement} options.remoteVideo 远端视频元素
 * @param {HTMLAudioElement} options.remoteAudio 远端音频元素
 */
export function useWebRTC({ sendSignal, remoteVideo, remoteAudio }) {
  const peerConnection = ref(null)
  const isConnected = ref(false)

  // ICE 服务器配置（生产环境建议使用 STUN/TURN 服务器）
  // TURN 服务器用于解决对称型 NAT 限制问题，保证内网用户也能建立通话
  const rtcConfig = {
    iceServers: [
      { urls: 'stun:stun.l.google.com:19302' },
      { urls: 'stun:stun1.l.google.com:19302' },
      // 可配置 TURN 服务器（需要替换为实际的 TURN 服务器地址和凭证）
      // { urls: 'turn:your-turn-server.com:3478', username: 'user', credential: 'pass' }
    ]
  }

  /**
   * 初始化 PeerConnection
   */
  const initPeerConnection = (callId, peerId) => {
    if (peerConnection.value) {
      closePeerConnection()
    }

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
    }

    // 监听连接状态
    pc.onconnectionstatechange = () => {
      console.log('WebRTC 连接状态:', pc.connectionState)
      if (pc.connectionState === 'connected') {
        isConnected.value = true
      } else if (['disconnected', 'failed', 'closed'].includes(pc.connectionState)) {
        isConnected.value = false
      }
    }

    peerConnection.value = pc
    return pc
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
    closePeerConnection
  }
}
