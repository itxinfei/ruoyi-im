/**
 * 语音通话管理器
 * 提供语音通话的完整流程管理：发起、接听、拒绝、挂断、WebRTC信令处理
 * 基于 useVideoCall 扩展，专注于语音通话场景
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
  VOICE: 'VOICE',
  VIDEO: 'VIDEO',
}

/**
 * 使用语音通话
 * @returns {Object} 语音通话相关的状态和方法
 */
export function useVoiceCall() {
  // 状态
  const callState = ref(CALL_STATE.IDLE)
  const callId = ref(null)
  const remoteUser = ref({})
  const conversationId = ref(null)

  // WebRTC 相关
  const localStream = ref(null)
  const remoteStream = ref(null)
  const peerConnection = ref(null)

  // 媒体控制状态
  const isMuted = ref(false)
  const isSpeakerOn = ref(true)
  const callDuration = ref(0)
  const callTimer = ref(null)

  // 音频设备列表
  const audioOutputDevices = ref([])
  const selectedOutputDevice = ref(null)

  // ICE 服务器配置
  const ICE_SERVERS = [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' },
    { urls: 'stun:stun2.l.google.com:19302' },
  ]

  // 计算属性
  const isInCall = computed(() =>
    [CALL_STATE.CALLING, CALL_STATE.INCOMING, CALL_STATE.CONNECTING, CALL_STATE.CONNECTED].includes(
      callState.value
    )
  )

  const isActiveCall = computed(() => callState.value === CALL_STATE.CONNECTED)

  /**
   * 检查浏览器支持
   */
  const checkBrowserSupport = () => {
    const support = {
      webRTC: !!window.RTCPeerConnection,
      getUserMedia: !!navigator.mediaDevices?.getUserMedia,
      webSocket: !!window.WebSocket,
    }

    if (!support.webRTC || !support.getUserMedia) {
      ElMessage.error('您的浏览器不支持语音通话功能，请使用最新版 Chrome/Edge/Firefox')
      return false
    }
    return true
  }

  /**
   * 初始化本地音频流
   */
  const initLocalStream = async () => {
    try {
      // 语音通话只需要音频
      const constraints = {
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
          sampleRate: 44100,
        },
        video: false,
      }

      localStream.value = await navigator.mediaDevices.getUserMedia(constraints)
      return localStream.value
    } catch (error) {
      console.error('获取麦克风失败:', error)

      if (error.name === 'NotAllowedError') {
        ElMessage.error('请允许访问麦克风以进行语音通话')
      } else if (error.name === 'NotFoundError') {
        ElMessage.error('未检测到麦克风设备')
      } else {
        ElMessage.error('无法访问麦克风：' + error.message)
      }
      throw error
    }
  }

  /**
   * 获取音频输出设备列表
   */
  const getAudioOutputDevices = async () => {
    try {
      const devices = await navigator.mediaDevices.enumerateDevices()
      audioOutputDevices.value = devices.filter(d => d.kind === 'audiooutput')

      // 默认选择第一个设备
      if (audioOutputDevices.value.length > 0 && !selectedOutputDevice.value) {
        selectedOutputDevice.value = audioOutputDevices.value[0].deviceId
      }
    } catch (error) {
      console.warn('获取音频输出设备失败:', error)
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
      console.log('[语音通话] 连接状态:', peerConnection.value.connectionState)

      if (peerConnection.value.connectionState === 'connected') {
        callState.value = CALL_STATE.CONNECTED
        startCallTimer()
      } else if (peerConnection.value.connectionState === 'disconnected') {
        handleCallDisconnected()
      } else if (peerConnection.value.connectionState === 'failed') {
        ElMessage.error('通话连接失败')
        endCall()
      }
    }

    // 监听 ICE 连接状态
    peerConnection.value.oniceconnectionstatechange = () => {
      console.log('[语音通话] ICE 状态:', peerConnection.value.iceConnectionState)

      if (peerConnection.value.iceConnectionState === 'disconnected') {
        handleCallDisconnected()
      } else if (peerConnection.value.iceConnectionState === 'failed') {
        ElMessage.error('网络连接失败')
        endCall()
      }
    }

    return peerConnection.value
  }

  /**
   * 处理通话断开
   */
  const handleCallDisconnected = () => {
    // 可能是暂时断开，等待几秒确认
    setTimeout(() => {
      if (
        peerConnection.value &&
        peerConnection.value.iceConnectionState === 'disconnected'
      ) {
        ElMessage.warning('通话已断开')
        endCall()
      }
    }, 3000)
  }

  /**
   * 发起语音通话
   */
  const initiateCall = async (calleeId, convId = null) => {
    if (!checkBrowserSupport()) {
      return false
    }

    try {
      // 检查是否已有通话
      const activeCall = await videoCallApi.getUserActiveCall()
      if (activeCall?.data && activeCall.data.callId) {
        ElMessage.warning('您正在进行另一场通话')
        return false
      }

      conversationId.value = convId

      // 获取用户信息
      const userInfo = await fetchUserInfo(calleeId)
      remoteUser.value = {
        id: calleeId,
        name: userInfo?.nickname || userInfo?.username || '对方',
        avatar: userInfo?.avatar || '',
      }

      // 初始化本地音频流
      await initLocalStream()

      // 获取音频输出设备
      await getAudioOutputDevices()

      // 创建 PeerConnection
      createPeerConnection()

      // 创建 Offer
      const offer = await peerConnection.value.createOffer()
      await peerConnection.value.setLocalDescription(offer)

      // 调用后端 API 发起通话
      const response = await videoCallApi.initiateCall({
        calleeId,
        conversationId: convId,
        callType: CALL_TYPE.VOICE,
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
        cleanup()
        return false
      }
    } catch (error) {
      console.error('[语音通话] 发起失败:', error)
      ElMessage.error('发起通话失败')
      cleanup()
      return false
    }
  }

  /**
   * 获取用户信息
   */
  const fetchUserInfo = async (userId) => {
    try {
      const response = await fetch(`/api/im/user/${userId}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`,
        },
      })
      if (response.ok) {
        const result = await response.json()
        if (result.code === 200) {
          return result.data
        }
      }
    } catch (error) {
      console.warn('[语音通话] 获取用户信息失败:', error)
    }
    return null
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

      // 初始化本地音频流
      await initLocalStream()

      // 获取音频输出设备
      await getAudioOutputDevices()

      // 创建 PeerConnection
      createPeerConnection()

      callState.value = CALL_STATE.CONNECTING

      return true
    } catch (error) {
      console.error('[语音通话] 接听失败:', error)
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
      console.error('[语音通话] 拒绝失败:', error)
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
      stopCallTimer()
      cleanup()
      return true
    } catch (error) {
      console.error('[语音通话] 挂断失败:', error)
      stopCallTimer()
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
          // 收到 offer，创建 answer
          if (!peerConnection.value) {
            await initLocalStream()
            await getAudioOutputDevices()
            createPeerConnection()
          }

          await peerConnection.value.setRemoteDescription(
            new RTCSessionDescription(JSON.parse(signalData))
          )

          // 创建并发送 Answer
          const answer = await peerConnection.value.createAnswer()
          await peerConnection.value.setLocalDescription(answer)
          sendWebRTCSignal('answer', JSON.stringify(answer))

          callState.value = CALL_STATE.CONNECTING
          break

        case 'answer':
          // 收到 answer
          if (peerConnection.value) {
            await peerConnection.value.setRemoteDescription(
              new RTCSessionDescription(JSON.parse(signalData))
            )
            callState.value = CALL_STATE.CONNECTING
          }
          break

        case 'ice-candidate':
          // 收到 ICE 候选
          if (peerConnection.value) {
            await peerConnection.value.addIceCandidate(
              new RTCIceCandidate(JSON.parse(signalData))
            )
          }
          break
      }
    } catch (error) {
      console.error('[语音通话] 处理信令失败:', error)
    }
  }

  /**
   * 处理来电通知
   */
  const handleIncomingCall = (message) => {
    const { callId: id, callType: type, callerId, callerName, callerAvatar, conversationId: convId } =
      message

    // 如果是视频通话，忽略
    if (type === 'VIDEO') {
      return
    }

    // 如果正在通话中，自动拒绝
    if (isInCall.value) {
      videoCallApi.rejectCall(id, '对方正在通话中')
      return
    }

    callId.value = id
    conversationId.value = convId
    remoteUser.value = {
      id: callerId,
      name: callerName,
      avatar: callerAvatar,
    }
    callState.value = CALL_STATE.INCOMING

    // 显示来电通知
    ElNotification({
      title: '语音通话',
      message: `${callerName || '对方'} 来电中...`,
      type: 'info',
      duration: 0,
      showClose: false,
      onClick: () => {
        // 通知被点击，恢复窗口
      },
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
        startCallTimer()
        break
      case 'ENDED':
        callState.value = CALL_STATE.ENDED
        stopCallTimer()
        cleanup()
        break
      case 'REJECTED':
        callState.value = CALL_STATE.REJECTED
        ElMessage.info('对方已拒绝通话')
        stopCallTimer()
        cleanup()
        break
      case 'TIMEOUT':
        callState.value = CALL_STATE.TIMEOUT
        ElMessage.info('通话超时')
        stopCallTimer()
        cleanup()
        break
    }
  }

  /**
   * 切换麦克风
   */
  const toggleMute = () => {
    if (localStream.value) {
      const audioTrack = localStream.value.getAudioTracks()[0]
      if (audioTrack) {
        isMuted.value = !isMuted.value
        audioTrack.enabled = !isMuted.value
      }
    }
    return isMuted.value
  }

  /**
   * 设置麦克风状态（供外部调用）
   */
  const setMuted = (muted) => {
    if (localStream.value) {
      const audioTrack = localStream.value.getAudioTracks()[0]
      if (audioTrack) {
        isMuted.value = muted
        audioTrack.enabled = !muted
      }
    }
  }

  /**
   * 切换扬声器
   */
  const toggleSpeaker = () => {
    isSpeakerOn.value = !isSpeakerOn.value
    return isSpeakerOn.value
  }

  /**
   * 设置扬声器状态（供外部调用）
   */
  const setSpeaker = (on) => {
    isSpeakerOn.value = on
  }

  /**
   * 选择音频输出设备
   */
  const selectAudioOutput = async (deviceId) => {
    selectedOutputDevice.value = deviceId
    // 注意：setSinkId 需要 HTTPS 环境
    if (typeof remoteStream.value !== 'undefined' && remoteStream.value) {
      // 这里需要应用到实际的 audio 元素上
      // 在组件中处理
    }
  }

  /**
   * 开始通话计时
   */
  const startCallTimer = () => {
    stopCallTimer()
    callDuration.value = 0
    callTimer.value = setInterval(() => {
      callDuration.value++
    }, 1000)
  }

  /**
   * 停止通话计时
   */
  const stopCallTimer = () => {
    if (callTimer.value) {
      clearInterval(callTimer.value)
      callTimer.value = null
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
    isMuted.value = false
    isSpeakerOn.value = true

    // 保存通话信息用于显示结果
    const savedCallId = callId.value
    const savedRemoteUser = { ...remoteUser.value }
    const savedDuration = callDuration.value

    callId.value = null
    remoteUser.value = {}
    conversationId.value = null
    callDuration.value = 0

    // 延迟重置状态
    setTimeout(() => {
      if (
        [CALL_STATE.ENDED, CALL_STATE.REJECTED, CALL_STATE.TIMEOUT].includes(callState.value)
      ) {
        callState.value = CALL_STATE.IDLE
      }
    }, 1000)

    // 返回清理的信息（可用于显示通话记录）
    return {
      callId: savedCallId,
      remoteUser: savedRemoteUser,
      duration: savedDuration,
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

  // 组件卸载时清理
  onUnmounted(() => {
    removeWebSocketListeners()
    cleanup()
  })

  return {
    // 状态
    callState,
    callId,
    remoteUser,
    conversationId,
    localStream,
    remoteStream,
    isMuted,
    isSpeakerOn,
    callDuration,
    audioOutputDevices,
    selectedOutputDevice,
    isInCall,
    isActiveCall,

    // 方法
    initiateCall,
    acceptCall,
    rejectCall,
    endCall,
    toggleMute,
    setMuted,
    toggleSpeaker,
    setSpeaker,
    selectAudioOutput,
    cleanup,
    setupWebSocketListeners,
    removeWebSocketListeners,
    handleIncomingCall,
    handleWebRTCSignal,

    // 常量
    CALL_STATE,
    CALL_TYPE,
  }
}

export default useVoiceCall
