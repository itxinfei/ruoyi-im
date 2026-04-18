import { ref, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * WebRTC 音视频通话核心组合式函数 - 2026 稳定性增强版
 * 增强点：ICE Restart 机制、自动化重连算法、媒体轨道深度清理
 */
export function useWebRTC({ sendSignal, onConnectionStateChange, onIceConnectionStateChange, remoteVideo, remoteAudio }) {
  const peerConnection = ref(null)
  const isConnected = ref(false)
  let reconnectTimer = null
  let disconnectTimer = null
  let reconnectAttempts = 0
  const MAX_RECONNECT_ATTEMPTS = 5 // 增加重连尝试次数
  const SHORT_DISCONNECT_THRESHOLD = 3000

  const rtcConfig = {
    iceServers: [
      { urls: 'stun:stun.l.google.com:19302' },
      { urls: 'stun:stun1.l.google.com:19302' },
      { urls: 'stun:stun2.l.google.com:19302' }
    ],
    iceTransportPolicy: 'all',
    bundlePolicy: 'max-bundle',
    rtcpMuxPolicy: 'require',
    iceCandidatePoolSize: 10
  }

  const clearReconnectTimers = () => {
    if (reconnectTimer) clearTimeout(reconnectTimer)
    if (disconnectTimer) clearTimeout(disconnectTimer)
    reconnectTimer = null
    disconnectTimer = null
  }

  const initPeerConnection = (callId, peerId) => {
    if (peerConnection.value) closePeerConnection()
    
    clearReconnectTimers()
    const pc = new RTCPeerConnection(rtcConfig)

    pc.onicecandidate = (event) => {
      if (event.candidate) {
        sendSignal('candidate', { callId, toUserId: peerId, candidate: event.candidate })
      }
    }

    pc.ontrack = (event) => {
      const stream = event.streams[0]
      if (remoteVideo.value) remoteVideo.value.srcObject = stream
      if (remoteAudio.value) remoteAudio.value.srcObject = stream
      isConnected.value = true
      reconnectAttempts = 0
      clearReconnectTimers()
      onConnectionStateChange?.('connected')
    }

    pc.onconnectionstatechange = () => {
      console.log('[WebRTC] Connection State:', pc.connectionState)
      onConnectionStateChange?.(pc.connectionState)
      if (pc.connectionState === 'connected') {
        isConnected.value = true
        reconnectAttempts = 0
        clearReconnectTimers()
      } else if (['disconnected', 'failed', 'closed'].includes(pc.connectionState)) {
        isConnected.value = false
      }
    }

    pc.oniceconnectionstatechange = () => {
      console.log('[WebRTC] ICE State:', pc.iceConnectionState)
      onIceConnectionStateChange?.(pc.iceConnectionState)

      if (['connected', 'completed'].includes(pc.iceConnectionState)) {
        reconnectAttempts = 0
        clearReconnectTimers()
      } else if (pc.iceConnectionState === 'disconnected') {
        clearReconnectTimers()
        disconnectTimer = setTimeout(() => {
          if (isConnected.value) {
            onConnectionStateChange?.('reconnecting')
            handleReconnect(callId, peerId, false)
          }
        }, SHORT_DISCONNECT_THRESHOLD)
      } else if (['failed', 'closed'].includes(pc.iceConnectionState)) {
        handleReconnect(callId, peerId, true) // 强制 ICE Restart
      }
    }

    peerConnection.value = pc
    return pc
  }

  /**
   * 处理断线重连 (核心算法)
   */
  const handleReconnect = async (callId, peerId, forceRestart = false) => {
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      onConnectionStateChange?.('reconnect_failed')
      return
    }

    reconnectAttempts++
    console.warn(`[WebRTC] Reconnecting... Attempt ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS} (Restart: ${forceRestart})`)

    if (peerConnection.value) {
      try {
        const offerOptions = forceRestart ? { iceRestart: true } : {}
        const offer = await peerConnection.value.createOffer(offerOptions)
        await peerConnection.value.setLocalDescription(offer)

        sendSignal('offer', {
          callId,
          toUserId: peerId,
          sdp: peerConnection.value.localDescription,
          isReconnect: true
        })
      } catch (e) {
        console.error('[WebRTC] Reconnect Offer Failed:', e)
        reconnectTimer = setTimeout(() => handleReconnect(callId, peerId, true), 2000)
      }
    }
  }

  const createOffer = async (callId, peerId, localStream) => {
    const pc = initPeerConnection(callId, peerId)
    localStream.getTracks().forEach(track => pc.addTrack(track, localStream))
    try {
      const offer = await pc.createOffer()
      await pc.setLocalDescription(offer)
      sendSignal('offer', { callId, toUserId: peerId, sdp: offer })
    } catch (e) {
      console.error('[WebRTC] Create Offer Error:', e)
      ElMessage.error('建立通话连接失败')
    }
  }

  const createAnswer = async (callId, peerId, offerSdp, localStream) => {
    const pc = initPeerConnection(callId, peerId)
    localStream.getTracks().forEach(track => pc.addTrack(track, localStream))
    try {
      await pc.setRemoteDescription(new RTCSessionDescription(offerSdp))
      const answer = await pc.createAnswer()
      await pc.setLocalDescription(answer)
      sendSignal('answer', { callId, toUserId: peerId, sdp: answer })
    } catch (e) {
      console.error('[WebRTC] Create Answer Error:', e)
    }
  }

  const handleAnswer = async (answerSdp) => {
    if (peerConnection.value) {
      try {
        await peerConnection.value.setRemoteDescription(new RTCSessionDescription(answerSdp))
      } catch (e) { console.error('[WebRTC] Set Remote Error:', e) }
    }
  }

  const handleCandidate = async (candidate) => {
    if (peerConnection.value) {
      try {
        await peerConnection.value.addIceCandidate(new RTCIceCandidate(candidate))
      } catch (e) { console.warn('[WebRTC] Add Candidate Error:', e) }
    }
  }

  const closePeerConnection = () => {
    clearReconnectTimers()
    if (peerConnection.value) {
      peerConnection.value.getSenders().forEach(sender => {
        if (sender.track) sender.track.stop()
      })
      peerConnection.value.close()
      peerConnection.value = null
    }
    isConnected.value = false
  }

  onUnmounted(closePeerConnection)

  return {
    isConnected,
    createOffer,
    createAnswer,
    handleAnswer,
    handleCandidate,
    closePeerConnection,
    manualReconnect: (callId, peerId, localStream) => {
      reconnectAttempts = 0
      createOffer(callId, peerId, localStream)
    }
  }
}
