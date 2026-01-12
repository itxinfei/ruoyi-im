<!--
  WebRTC 视频通话组件
  功能：实现点对点音视频通话
  支持：视频通话、语音通话、屏幕共享、通话控制
-->
<template>
  <div v-if="visible" class="video-call-container">
    <!-- 视频通话界面 -->
    <div class="video-call-interface" :class="{ fullscreen: isFullscreen }">
      <!-- 远程视频（对方） -->
      <div class="remote-video-wrapper">
        <video
          ref="remoteVideoRef"
          class="remote-video"
          autoplay
          playsinline
          :muted="false"
        ></video>
        <div v-if="!remoteStreamReady" class="video-placeholder">
          <div class="placeholder-content">
            <i v-if="callType === 'voice'" class="el-icon-phone"></i>
            <i v-else class="el-icon-user"></i>
            <span>{{ callType === 'voice' ? '语音通话中' : '等待对方接听...' }}</span>
          </div>
        </div>
        <!-- 屏幕共享视频 -->
        <video
          v-if="screenStream"
          ref="screenVideoRef"
          class="screen-video"
          autoplay
          playsinline
        ></video>
      </div>

      <!-- 本地视频（自己） -->
      <div
        class="local-video-wrapper"
        :class="{ minimized: isLocalVideoMinimized }"
        @click="toggleLocalVideoSize"
      >
        <video
          ref="localVideoRef"
          class="local-video"
          autoplay
          playsinline
          muted
        ></video>
        <div v-if="!localStreamReady" class="local-placeholder">
          <i class="el-icon-loading"></i>
        </div>
      </div>

      <!-- 通话信息栏 -->
      <div class="call-info-bar">
        <div class="caller-info">
          <img :src="remoteUser.avatar || defaultAvatar" class="caller-avatar" />
          <div class="caller-details">
            <div class="caller-name">{{ remoteUser.name || remoteUser.nickname || '对方' }}</div>
            <div class="call-status">
              <span v-if="callState === 'calling'">
                <i class="el-icon-loading"></i> 呼叫中...
              </span>
              <span v-else-if="callState === 'connected'">
                <i class="el-icon-video-camera"></i>
                通话中 {{ formatDuration(callDuration) }}
              </span>
              <span v-else-if="callState === 'connecting'">
                <i class="el-icon-loading"></i> 连接中...
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 控制按钮栏 -->
      <div class="control-bar">
        <transition name="control-expand">
          <div v-if="showControls" class="control-buttons">
            <!-- 麦克风开关 -->
            <el-tooltip :content="isMicOn ? '关闭麦克风' : '开启麦克风'" placement="top">
              <button
                class="control-btn"
                :class="{ active: !isMicOn }"
                @click="toggleMic"
              >
                <i :class="isMicOn ? 'el-icon-microphone' : 'el-icon-microphone-off'"></i>
              </button>
            </el-tooltip>

            <!-- 摄像头开关 -->
            <el-tooltip v-if="callType === 'video'" :content="isCameraOn ? '关闭摄像头' : '开启摄像头'" placement="top">
              <button
                class="control-btn"
                :class="{ active: !isCameraOn }"
                @click="toggleCamera"
              >
                <i :class="isCameraOn ? 'el-icon-video-camera' : 'el-icon-video-camera-filled'"></i>
              </button>
            </el-tooltip>

            <!-- 屏幕共享 -->
            <el-tooltip v-if="callType === 'video'" content="屏幕共享" placement="top">
              <button
                class="control-btn"
                :class="{ active: isScreenSharing }"
                @click="toggleScreenShare"
              >
                <i class="el-icon-monitor"></i>
              </button>
            </el-tooltip>

            <!-- 扬声器开关 -->
            <el-tooltip :content="isSpeakerOn ? '关闭扬声器' : '开启扬声器'" placement="top">
              <button
                class="control-btn"
                :class="{ active: !isSpeakerOn }"
                @click="toggleSpeaker"
              >
                <i :class="isSpeakerOn ? 'el-icon-bell' : 'el-icon-close-notification'"></i>
              </button>
            </el-tooltip>

            <!-- 摄像头切换 -->
            <el-tooltip v-if="callType === 'video' && hasMultipleCameras" content="切换摄像头" placement="top">
              <button class="control-btn" @click="switchCamera">
                <i class="el-icon-refresh"></i>
              </button>
            </el-tooltip>

            <!-- 全屏切换 -->
            <el-tooltip :content="isFullscreen ? '退出全屏' : '全屏'" placement="top">
              <button class="control-btn" @click="toggleFullscreen">
                <i :class="isFullscreen ? 'el-icon-crop' : 'el-icon-full-screen'"></i>
              </button>
            </el-tooltip>

            <!-- 挂断 -->
            <el-tooltip content="挂断" placement="top">
              <button class="control-btn hangup" @click="hangup">
                <i class="el-icon-phone-outline"></i>
              </button>
            </el-tooltip>
          </div>
        </transition>
      </div>
    </div>

    <!-- 来电提醒 -->
    <div v-if="callState === 'incoming'" class="incoming-call-modal">
      <div class="incoming-modal-content">
        <div class="caller-info-large">
          <img :src="remoteUser.avatar || defaultAvatar" class="caller-avatar-large" />
          <div class="caller-name-large">{{ remoteUser.name || remoteUser.nickname || '对方' }}</div>
          <div class="call-type-text">{{ callType === 'video' ? '视频通话' : '语音通话' }}</div>
        </div>
        <div class="incoming-actions">
          <button class="action-btn reject" @click="rejectCall">
            <i class="el-icon-close"></i>
            <span>拒绝</span>
          </button>
          <button class="action-btn accept" @click="acceptCall">
            <i class="el-icon-check"></i>
            <span>接听</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  callType: {
    type: String,
    default: 'video', // 'video' | 'voice'
  },
  remoteUser: {
    type: Object,
    default: () => ({}),
  },
  callState: {
    type: String,
    default: 'calling', // 'calling' | 'incoming' | 'connecting' | 'connected' | 'ended'
  },
  signalingUrl: {
    type: String,
    default: '/ws/signaling',
  },
})

// Emits
const emit = defineEmits([
  'update:visible',
  'accept',
  'reject',
  'hangup',
  'state-change',
])

// Refs
const localVideoRef = ref(null)
const remoteVideoRef = ref(null)
const screenVideoRef = ref(null)

// State
const localStream = ref(null)
const remoteStream = ref(null)
const screenStream = ref(null)
const peerConnection = ref(null)
const signalingSocket = ref(null)
const localStreamReady = ref(false)
const remoteStreamReady = ref(false)

// UI State
const isMicOn = ref(true)
const isCameraOn = ref(true)
const isSpeakerOn = ref(true)
const isScreenSharing = ref(false)
const isFullscreen = ref(false)
const isLocalVideoMinimized = ref(false)
const showControls = ref(true)
const hasMultipleCameras = ref(false)
const callDuration = ref(0)
const callTimer = ref(null)

// Constants
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const ICE_SERVERS = [
  { urls: 'stun:stun.l.google.com:19302' },
  { urls: 'stun:stun1.l.google.com:19302' },
]

// Methods
const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 初始化本地媒体流
const initLocalStream = async () => {
  try {
    const constraints = {
      audio: true,
      video: props.callType === 'video',
    }

    localStream.value = await navigator.mediaDevices.getUserMedia(constraints)
    localStreamReady.value = true

    if (localVideoRef.value) {
      localVideoRef.value.srcObject = localStream.value
    }

    // 检查是否有多个摄像头
    const devices = await navigator.mediaDevices.enumerateDevices()
    const videoDevices = devices.filter(d => d.kind === 'videoinput')
    hasMultipleCameras.value = videoDevices.length > 1

    return localStream.value
  } catch (error) {
    console.error('获取媒体设备失败:', error)
    ElMessage.error('无法访问摄像头或麦克风，请检查权限设置')
    throw error
  }
}

// 创建 WebRTC 连接
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
      remoteStreamReady.value = true

      if (remoteVideoRef.value) {
        remoteVideoRef.value.srcObject = remoteStream.value
      }
    }
  }

  // 监听 ICE 候选
  peerConnection.value.onicecandidate = (event) => {
    if (event.candidate) {
      sendSignalingMessage({
        type: 'ice-candidate',
        candidate: event.candidate,
      })
    }
  }

  // 监听连接状态
  peerConnection.value.onconnectionstatechange = () => {
    console.log('连接状态:', peerConnection.value.connectionState)
    if (peerConnection.value.connectionState === 'connected') {
      startCallTimer()
      emit('state-change', 'connected')
    } else if (peerConnection.value.connectionState === 'disconnected') {
      stopCallTimer()
      emit('state-change', 'ended')
    }
  }

  return peerConnection.value
}

// 发起呼叫
const startCall = async () => {
  try {
    await initLocalStream()
    const pc = createPeerConnection()

    const offer = await pc.createOffer()
    await pc.setLocalDescription(offer)

    sendSignalingMessage({
      type: 'offer',
      sdp: offer,
      callType: props.callType,
    })

    emit('state-change', 'calling')
  } catch (error) {
    console.error('发起呼叫失败:', error)
    ElMessage.error('发起呼叫失败')
  }
}

// 接听呼叫
const acceptCall = async () => {
  try {
    await initLocalStream()
    const pc = createPeerConnection()

    const answer = await pc.createAnswer()
    await pc.setLocalDescription(answer)

    sendSignalingMessage({
      type: 'answer',
      sdp: answer,
    })

    emit('accept')
    emit('state-change', 'connecting')
  } catch (error) {
    console.error('接听失败:', error)
    ElMessage.error('接听失败')
  }
}

// 拒绝呼叫
const rejectCall = () => {
  emit('reject')
  emit('update:visible', false)
}

// 处理 offer
const handleOffer = async (offer) => {
  try {
    if (!peerConnection.value) {
      await initLocalStream()
      createPeerConnection()
    }

    await peerConnection.value.setRemoteDescription(new RTCSessionDescription(offer))
  } catch (error) {
    console.error('处理 offer 失败:', error)
  }
}

// 处理 answer
const handleAnswer = async (answer) => {
  try {
    if (peerConnection.value) {
      await peerConnection.value.setRemoteDescription(new RTCSessionDescription(answer))
      emit('state-change', 'connecting')
    }
  } catch (error) {
    console.error('处理 answer 失败:', error)
  }
}

// 处理 ICE 候选
const handleIceCandidate = async (candidate) => {
  try {
    if (peerConnection.value) {
      await peerConnection.value.addIceCandidate(new RTCIceCandidate(candidate))
    }
  } catch (error) {
    console.error('添加 ICE 候选失败:', error)
  }
}

// 发送信令消息
const sendSignalingMessage = (message) => {
  // 这里应该通过 WebSocket 发送信令消息
  console.log('发送信令:', message)
  // 实际实现需要连接到信令服务器
}

// 挂断
const hangup = () => {
  stopCallTimer()

  // 停止所有轨道
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => track.stop())
  }
  if (remoteStream.value) {
    remoteStream.value.getTracks().forEach(track => track.stop())
  }
  if (screenStream.value) {
    screenStream.value.getTracks().forEach(track => track.stop())
  }

  // 关闭连接
  if (peerConnection.value) {
    peerConnection.value.close()
    peerConnection.value = null
  }

  // 发送挂断信令
  sendSignalingMessage({ type: 'hangup' })

  // 重置状态
  localStream.value = null
  remoteStream.value = null
  screenStream.value = null
  localStreamReady.value = false
  remoteStreamReady.value = false

  emit('hangup')
  emit('update:visible', false)
}

// 切换麦克风
const toggleMic = () => {
  if (localStream.value) {
    const audioTrack = localStream.value.getAudioTracks()[0]
    if (audioTrack) {
      isMicOn.value = !isMicOn.value
      audioTrack.enabled = isMicOn.value
    }
  }
}

// 切换摄像头
const toggleCamera = () => {
  if (localStream.value) {
    const videoTrack = localStream.value.getVideoTracks()[0]
    if (videoTrack) {
      isCameraOn.value = !isCameraOn.value
      videoTrack.enabled = isCameraOn.value
    }
  }
}

// 切换扬声器
const toggleSpeaker = () => {
  isSpeakerOn.value = !isSpeakerOn.value
  if (remoteVideoRef.value) {
    remoteVideoRef.value.muted = !isSpeakerOn.value
  }
}

// 切换屏幕共享
const toggleScreenShare = async () => {
  if (isScreenSharing.value) {
    // 停止屏幕共享
    if (screenStream.value) {
      screenStream.value.getTracks().forEach(track => track.stop())
      screenStream.value = null
    }
    isScreenSharing.value = false
  } else {
    // 开始屏幕共享
    try {
      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: false,
      })

      screenStream.value = stream

      // 替换视频轨道
      if (peerConnection.value && localStream.value) {
        const videoTrack = stream.getVideoTracks()[0]
        const sender = peerConnection.value
          .getSenders()
          .find(s => s.track.kind === 'video')

        if (sender) {
          await sender.replaceTrack(videoTrack)
        }

        // 监听用户停止共享
        videoTrack.onended = () => {
          toggleScreenShare()
        }
      }

      isScreenSharing.value = true
    } catch (error) {
      console.error('屏幕共享失败:', error)
      ElMessage.warning('屏幕共享被取消')
    }
  }
}

// 切换摄像头（移动端）
const switchCamera = async () => {
  // 实现摄像头切换逻辑
  ElMessage.info('切换摄像头')
}

// 切换全屏
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  if (isFullscreen.value) {
    enterFullscreen()
  } else {
    exitFullscreen()
  }
}

const enterFullscreen = () => {
  const elem = document.querySelector('.video-call-interface')
  if (elem.requestFullscreen) {
    elem.requestFullscreen()
  }
}

const exitFullscreen = () => {
  if (document.exitFullscreen) {
    document.exitFullscreen()
  }
}

// 切换本地视频大小
const toggleLocalVideoSize = () => {
  isLocalVideoMinimized.value = !isLocalVideoMinimized.value
}

// 开始通话计时
const startCallTimer = () => {
  callDuration.value = 0
  callTimer.value = setInterval(() => {
    callDuration.value++
  }, 1000)
}

// 停止通话计时
const stopCallTimer = () => {
  if (callTimer.value) {
    clearInterval(callTimer.value)
    callTimer.value = null
  }
}

// 监听 props 变化
watch(() => props.visible, async (val) => {
  if (val) {
    // 显示通话界面
    if (props.callState === 'calling') {
      await startCall()
    } else if (props.callState === 'incoming') {
      // 来电，等待用户操作
    }
  } else {
    // 隐藏通话界面，清理资源
    hangup()
  }
})

watch(() => props.callState, (newState) => {
  if (newState === 'connected') {
    startCallTimer()
  } else if (newState === 'ended') {
    stopCallTimer()
  }
})

// 生命周期
onMounted(() => {
  // 自动隐藏控制条
  let controlsTimer
  const resetControlsTimer = () => {
    showControls.value = true
    clearTimeout(controlsTimer)
    controlsTimer = setTimeout(() => {
      if (props.callState === 'connected') {
        showControls.value = false
      }
    }, 3000)
  }

  document.addEventListener('mousemove', resetControlsTimer)
  document.addEventListener('click', resetControlsTimer)
  document.addEventListener('touchstart', resetControlsTimer)

  return () => {
    document.removeEventListener('mousemove', resetControlsTimer)
    document.removeEventListener('click', resetControlsTimer)
    document.removeEventListener('touchstart', resetControlsTimer)
  }
})

onUnmounted(() => {
  hangup()
})
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.video-call-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: $z-index-modal;
  background: #000;
}

.video-call-interface {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  &.fullscreen {
    &:fullscreen {
      width: 100vw;
      height: 100vh;
    }
  }
}

// 远程视频（对方）
.remote-video-wrapper {
  flex: 1;
  position: relative;
  background: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;

  .remote-video {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .screen-video {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: contain;
    z-index: 2;
  }

  .video-placeholder {
    position: absolute;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;

    .placeholder-content {
      text-align: center;
      color: #fff;

      i {
        display: block;
        font-size: 64px;
        margin-bottom: 16px;
        opacity: 0.6;
      }

      span {
        font-size: 16px;
        opacity: 0.8;
      }
    }
  }
}

// 本地视频（自己）
.local-video-wrapper {
  position: absolute;
  bottom: 100px;
  right: 20px;
  width: 180px;
  height: 240px;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 10;
  border: 2px solid rgba(255, 255, 255, 0.2);

  &.minimized {
    width: 120px;
    height: 160px;
    bottom: 20px;
  }

  .local-video {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transform: scaleX(-1); // 镜像翻转
  }

  .local-placeholder {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.5);

    i {
      font-size: 32px;
      color: #fff;
      animation: spin 1s linear infinite;
    }
  }
}

// 通话信息栏
.call-info-bar {
  position: absolute;
  top: 20px;
  left: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  z-index: 10;

  .caller-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .caller-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      object-fit: cover;
    }

    .caller-details {
      .caller-name {
        font-size: 16px;
        font-weight: 500;
        color: #fff;
        margin-bottom: 2px;
      }

      .call-status {
        font-size: 13px;
        color: rgba(255, 255, 255, 0.7);

        i {
          margin-right: 4px;
        }
      }
    }
  }
}

// 控制栏
.control-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  display: flex;
  justify-content: center;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  z-index: 10;

  .control-buttons {
    display: flex;
    align-items: center;
    gap: 12px;

    .control-btn {
      width: 56px;
      height: 56px;
      border-radius: 50%;
      border: none;
      background: rgba(255, 255, 255, 0.15);
      backdrop-filter: blur(10px);
      color: #fff;
      font-size: 24px;
      cursor: pointer;
      transition: all 0.2s ease;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        background: rgba(255, 255, 255, 0.25);
        transform: scale(1.05);
      }

      &:active {
        transform: scale(0.95);
      }

      &.active {
        background: rgba(255, 77, 79, 0.9);

        i {
          color: #fff;
        }
      }

      &.hangup {
        background: rgba(255, 77, 79, 0.9);

        &:hover {
          background: rgba(255, 77, 79, 1);
        }
      }
    }
  }
}

// 来电提醒
.incoming-call-modal {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
  animation: fadeIn 0.3s ease;

  .incoming-modal-content {
    text-align: center;
    animation: slideUp 0.3s ease;

    .caller-info-large {
      margin-bottom: 40px;

      .caller-avatar-large {
        width: 120px;
        height: 120px;
        border-radius: 50%;
        object-fit: cover;
        margin-bottom: 20px;
        border: 4px solid rgba(255, 255, 255, 0.2);
      }

      .caller-name-large {
        font-size: 24px;
        font-weight: 500;
        color: #fff;
        margin-bottom: 8px;
      }

      .call-type-text {
        font-size: 16px;
        color: rgba(255, 255, 255, 0.7);
      }
    }

    .incoming-actions {
      display: flex;
      justify-content: center;
      gap: 40px;

      .action-btn {
        width: 64px;
        height: 64px;
        border-radius: 50%;
        border: none;
        cursor: pointer;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        transition: all 0.2s ease;

        &.reject {
          background: rgba(255, 77, 79, 0.9);

          &:hover {
            background: rgba(255, 77, 79, 1);
            transform: scale(1.05);
          }
        }

        &.accept {
          background: rgba(82, 196, 26, 0.9);

          &:hover {
            background: rgba(82, 196, 26, 1);
            transform: scale(1.05);
          }
        }

        i {
          font-size: 24px;
          color: #fff;
          margin-bottom: 4px;
        }

        span {
          font-size: 12px;
          color: #fff;
        }
      }
    }
  }
}

// 动画
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.control-expand-enter-active,
.control-expand-leave-active {
  transition: all 0.3s ease;
}

.control-expand-enter-from,
.control-expand-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

// 响应式
@media (max-width: 768px) {
  .local-video-wrapper {
    width: 120px;
    height: 160px;
    bottom: 80px;
    right: 12px;

    &.minimized {
      width: 80px;
      height: 107px;
      bottom: 12px;
    }
  }

  .control-bar {
    padding: 12px;

    .control-buttons {
      gap: 8px;

      .control-btn {
        width: 48px;
        height: 48px;
        font-size: 20px;
      }
    }
  }

  .call-info-bar {
    top: 12px;
    left: 12px;
    padding: 8px 12px;

    .caller-info {
      .caller-avatar {
        width: 32px;
        height: 32px;
      }

      .caller-details {
        .caller-name {
          font-size: 14px;
        }

        .call-status {
          font-size: 12px;
        }
      }
    }
  }
}
</style>
