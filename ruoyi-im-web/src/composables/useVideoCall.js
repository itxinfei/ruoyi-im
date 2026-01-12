/**
 * 视频通话管理器
 * 提供视频通话的完整流程管理：发起、接听、拒绝、挂断、WebRTC信令处理
 */
import { ref, computed, onUnmounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import imWebSocket, { MSG_TYPE } from '@/utils/websocket/imWebSocket'
import * as videoCallApi from '@/api/im/video-call'

// 通话状态
export const CALL_STATE = {
  IDLE: 'idle',           // 空闲
  CALLING: 'calling',     // 呼叫中
  INCOMING: 'incoming',   // 来电中
  CONNECTING: 'connecting', // 连接中
  CONNECTED: 'connected', // 已连接
  ENDED: 'ended',         // 已结束
  REJECTED: 'rejected',   // 已拒绝
  TIMEOUT: 'timeout',     // 超时
}

// 通话类型
export const CALL_TYPE = {
  VIDEO: 'video',
  VOICE: 'voice',
}

/**
 * 使用视频通话
 * @returns {Object} 视频通话相关的状态和方法
 */
export function useVideoCall() {
  // 状态
  const callState = ref(CALL_STATE.IDLE)
  const callType = ref(CALL_TYPE.VIDEO)
  const callId = ref(null)
  const remoteUser = ref({})
  const conversationId = ref(null)

  // WebRTC 相关
  const localStream = ref(null)
  const remoteStream = ref(null)
  const peerConnection = ref(null)

  // 配置
  const ICE_SERVERS = [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' },
  ]

  // 计算属性
  const isInCall = computed(() =>
    [CALL_STATE.CALLING, CALL_STATE.INCOMING, CALL_STATE.CONNECTING, CALL_STATE.CONNECTED].includes(callState.value)
  )

  const isActiveCall = computed(() => callState.value === CALL_STATE.CONNECTED)

  /**
   * 初始化本地媒体流
   */
  const initLocalStream = async (video = true) => {
    try {
      const constraints = {
        audio: true,
        video: video,
      }

      localStream.value = await navigator.mediaDevices.getUserMedia(constraints)
      return localStream.value
    } catch (error) {
      console.error('获取媒体设备失败:', error)
      ElMessage.error('无法访问摄像头或麦克风，请检查权限设置')
      throw error
    }
  }

  /**
   * 创建 WebRTC 连接
   */
  const createPeerConnection = () => {
    const config = {
      iceServers: ICE_SERVERS,
    }

    peerConnection.value = new RTCPeerConnection(config)

    // 添加本地流到连接
    if (localStream.value) {
      localStream.value.getTracks().forEach(track => {
        peerConnection.value.addTrack(track, localStream.value)
      })
    }

    // 监听远程流
    peerConnection.value.ontrack = (event) => {
      if (event.streams && event.streams[0]) {
        remoteStream.value = event.streams[0]
      }
    }

    // 监听 ICE 候选
    peerConnection.value.onicecandidate = (event) => {
      if (event.candidate && callId.value) {
        sendWebRTCSignal('ice-candidate', JSON.stringify(event.candidate))
      }
    }

    // 监听连接状态
    peerConnection.value.onconnectionstatechange = () => {
      console.log('连接状态:', peerConnection.value.connectionState)
      if (peerConnection.value.connectionState === 'connected') {
        callState.value = CALL_STATE.CONNECTED
      } else if (peerConnection.value.connectionState === 'disconnected') {
        endCall()
      }
    }

    return peerConnection.value
  }

  /**
   * 发起通话
   */
  const initiateCall = async (calleeId, type = CALL_TYPE.VIDEO, convId = null) => {
    try {
      // 检查对方是否在线
      const activeCall = await videoCallApi.getUserActiveCall()
      if (activeCall?.data && activeCall.data.callerId === calleeId) {
        ElMessage.warning('对方正在通话中')
        return false
      }

      callType.value = type
      conversationId.value = convId

      // 获取对方用户信息
      // 这里可以从 store 或 API 获取
      remoteUser.value = { id: calleeId }

      // 初始化本地流
      await initLocalStream(type === CALL_TYPE.VIDEO)

      // 创建 PeerConnection
      createPeerConnection()

      // 创建 Offer
      const offer = await peerConnection.value.createOffer()
      await peerConnection.value.setLocalDescription(offer)

      // 调用后端 API 发起通话
      const response = await videoCallApi.initiateCall({
        calleeId,
        conversationId: convId,
        callType: type.toUpperCase(),
      })

      if (response.code === 200) {
        callId.value = response.data
        callState.value = CALL_STATE.CALLING

        // 发送 offer 信令
        sendWebRTCSignal('offer', JSON.stringify(offer))

        // 60秒超时处理
        setTimeout(() => {
          if (callState.value === CALL_STATE.CALLING) {
            ElMessage.warning('对方未接听')
            endCall()
          }
        }, 60000)

        return true
      } else {
        ElMessage.error(response.msg || '发起通话失败')
        return false
      }
    } catch (error) {
      console.error('发起通话失败:', error)
      ElMessage.error('发起通话失败')
      endCall()
      return false
    }
  }

  /**
   * 接听来电
   */
  const acceptCall = async () => {
    try {
      if (!callId.value) {
        ElMessage.error('通话信息不存在')
        return false
      }

      // 调用后端 API 接听通话
      const response = await videoCallApi.acceptCall(callId.value)

      if (response.code !== 200) {
        ElMessage.error(response.msg || '接听失败')
        return false
      }

      // 初始化本地流
      await initLocalStream(callType.value === CALL_TYPE.VIDEO)

      // 创建 PeerConnection
      createPeerConnection()

      callState.value = CALL_STATE.CONNECTING

      return true
    } catch (error) {
      console.error('接听失败:', error)
      ElMessage.error('接听失败')
      return false
    }
  }

  /**
   * 拒绝来电
   */
  const rejectCall = async (reason = '') => {
    try {
      if (callId.value) {
        await videoCallApi.rejectCall(callId.value, reason)
      }
      callState.value = CALL_STATE.REJECTED
      cleanup()
      return true
    } catch (error) {
      console.error('拒绝通话失败:', error)
      cleanup()
      return false
    }
  }

  /**
   * 挂断通话
   */
  const endCall = async () => {
    try {
      if (callId.value) {
        await videoCallApi.endCall(callId.value)
      }
      callState.value = CALL_STATE.ENDED
      cleanup()
      return true
    } catch (error) {
      console.error('挂断通话失败:', error)
      cleanup()
      return false
    }
  }

  /**
   * 发送 WebRTC 信令
   */
  const sendWebRTCSignal = (signalType, signalData) => {
    imWebSocket.sendWebRTCSignal({
      callId: callId.value,
      signalType,
      signalData,
    })
  }

  /**
   * 处理 WebRTC 信令
   */
  const handleWebRTCSignal = async (message) => {
    const { signalType, signalData } = message

    try {
      switch (signalType) {
        case 'offer':
          if (!peerConnection.value) {
            await createPeerConnection()
          }
          await peerConnection.value.setRemoteDescription(new RTCSessionDescription(JSON.parse(signalData)))

          // 创建并发送 Answer
          const answer = await peerConnection.value.createAnswer()
          await peerConnection.value.setLocalDescription(answer)
          sendWebRTCSignal('answer', JSON.stringify(answer))
          break

        case 'answer':
          if (peerConnection.value) {
            await peerConnection.value.setRemoteDescription(new RTCSessionDescription(JSON.parse(signalData)))
            callState.value = CALL_STATE.CONNECTING
          }
          break

        case 'ice-candidate':
          if (peerConnection.value) {
            await peerConnection.value.addIceCandidate(new RTCIceCandidate(JSON.parse(signalData)))
          }
          break
      }
    } catch (error) {
      console.error('处理信令失败:', error)
    }
  }

  /**
   * 处理来电通知
   */
  const handleIncomingCall = (message) => {
    const { callId: id, callType: type, callerId, conversationId: convId } = message

    // 如果正在通话中，自动拒绝
    if (isInCall.value) {
      videoCallApi.rejectCall(id, '对方正在通话中')
      return
    }

    callId.value = id
    callType.value = type.toLowerCase() || CALL_TYPE.VIDEO
    conversationId.value = convId
    remoteUser.value = { id: callerId }
    callState.value = CALL_STATE.INCOMING

    // 显示来电通知
    ElNotification({
      title: type === 'VIDEO' ? '视频通话' : '语音通话',
      message: '来电中...',
      type: 'info',
      duration: 0,
      showClose: false,
    })
  }

  /**
   * 处理通话状态变化
   */
  const handleCallStatus = (message) => {
    const { status } = message

    switch (status) {
      case 'CONNECTED':
        callState.value = CALL_STATE.CONNECTED
        break
      case 'ENDED':
        callState.value = CALL_STATE.ENDED
        cleanup()
        break
      case 'REJECTED':
        callState.value = CALL_STATE.REJECTED
        ElMessage.info('对方已拒绝通话')
        cleanup()
        break
      case 'TIMEOUT':
        callState.value = CALL_STATE.TIMEOUT
        ElMessage.info('通话超时')
        cleanup()
        break
    }
  }

  /**
   * 清理资源
   */
  const cleanup = () => {
    // 停止媒体流
    if (localStream.value) {
      localStream.value.getTracks().forEach(track => track.stop())
      localStream.value = null
    }

    // 关闭连接
    if (peerConnection.value) {
      peerConnection.value.close()
      peerConnection.value = null
    }

    remoteStream.value = null
    callId.value = null
    remoteUser.value = {}
    conversationId.value = null

    // 延迟重置状态
    setTimeout(() => {
      if ([CALL_STATE.ENDED, CALL_STATE.REJECTED, CALL_STATE.TIMEOUT].includes(callState.value)) {
        callState.value = CALL_STATE.IDLE
      }
    }, 1000)
  }

  /**
   * 切换麦克风
   */
  const toggleMic = () => {
    if (localStream.value) {
      const audioTrack = localStream.value.getAudioTracks()[0]
      if (audioTrack) {
        audioTrack.enabled = !audioTrack.enabled
        return audioTrack.enabled
      }
    }
    return false
  }

  /**
   * 切换摄像头
   */
  const toggleCamera = () => {
    if (localStream.value) {
      const videoTrack = localStream.value.getVideoTracks()[0]
      if (videoTrack) {
        videoTrack.enabled = !videoTrack.enabled
        return videoTrack.enabled
      }
    }
    return false
  }

  /**
   * 切换屏幕共享
   */
  const toggleScreenShare = async () => {
    try {
      if (!peerConnection.value || !localStream.value) {
        return false
      }

      const videoTrack = localStream.value.getVideoTracks()[0]
      if (!videoTrack) {
        return false
      }

      // 检查是否正在共享屏幕
      if (videoTrack.label.includes('screen')) {
        // 停止屏幕共享，恢复摄像头
        const newStream = await initLocalStream(true)
        const newVideoTrack = newStream.getVideoTracks()[0]
        const sender = peerConnection.value.getSenders().find(s => s.track.kind === 'video')
        if (sender) {
          await sender.replaceTrack(newVideoTrack)
        }
        return false
      } else {
        // 开始屏幕共享
        const screenStream = await navigator.mediaDevices.getDisplayMedia({ video: true })
        const screenTrack = screenStream.getVideoTracks()[0]
        const sender = peerConnection.value.getSenders().find(s => s.track.kind === 'video')
        if (sender) {
          await sender.replaceTrack(screenTrack)
        }

        // 监听用户停止共享
        screenTrack.onended = () => {
          toggleScreenShare()
        }

        return true
      }
    } catch (error) {
      console.error('切换屏幕共享失败:', error)
      ElMessage.warning('屏幕共享被取消')
      return false
    }
  }

  // 监听 WebSocket 消息
  const setupWebSocketListeners = () => {
    imWebSocket.on('incomingCall', handleIncomingCall)
    imWebSocket.on('callStatus', handleCallStatus)
    imWebSocket.on('webrtcSignal', handleWebRTCSignal)
  }

  const removeWebSocketListeners = () => {
    imWebSocket.off('incomingCall', handleIncomingCall)
    imWebSocket.off('callStatus', handleCallStatus)
    imWebSocket.off('webrtcSignal', handleWebRTCSignal)
  }

  // 自动设置监听
  setupWebSocketListeners()

  // 组件卸载时清理
  onUnmounted(() => {
    removeWebSocketListeners()
    cleanup()
  })

  return {
    // 状态
    callState,
    callType,
    callId,
    remoteUser,
    conversationId,
    localStream,
    remoteStream,
    isInCall,
    isActiveCall,

    // 方法
    initiateCall,
    acceptCall,
    rejectCall,
    endCall,
    toggleMic,
    toggleCamera,
    toggleScreenShare,
    cleanup,
    setupWebSocketListeners,
    removeWebSocketListeners,

    // 常量
    CALL_STATE,
    CALL_TYPE,
  }
}

export default useVideoCall
