<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="isVideoCall ? '900px' : '400px'"
    :close-on-click-modal="false"
    :close-on-press-escape="canClose"
    :show-close="canClose"
    :class="['call-dialog', type, status]"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <!-- 视频通话界面 -->
    <div v-if="isVideoCall && (status === 'talking' || status === 'connecting')" class="video-call-container">
      <!-- 主视频区域 -->
      <div class="video-area">
        <!-- 远程视频 -->
        <div class="remote-video">
          <video
            ref="remoteVideoRef"
            autoplay
            playsinline
            :muted="false"
            @loadedmetadata="handleRemoteLoaded"
          ></video>
          <div v-if="!remoteStreamLoaded" class="video-placeholder">
            <span class="material-icons-outlined">videocam_off</span>
            <span>等待对方视频...</span>
          </div>
        </div>

        <!-- 本地视频（画中画） -->
        <div v-if="localStream" class="local-video" :class="{ minimized: localVideoMinimized }">
          <video
            ref="localVideoRef"
            autoplay
            playsinline
            muted
            @loadedmetadata="handleLocalLoaded"
          ></video>
          <div class="video-controls-overlay" @click="toggleLocalVideoMinimize">
            <span class="material-icons-outlined">open_in_full</span>
          </div>
        </div>
      </div>

      <!-- 通话控制栏 -->
      <div class="call-controls">
        <div class="duration">{{ formattedDuration }}</div>
        <div class="control-buttons">
          <!-- 麦克风 -->
          <button
            class="control-btn"
            :class="{ active: !isMuted }"
            @click="toggleMute"
            :title="isMuted ? '取消静音' : '静音'"
          >
            <span class="material-icons-outlined">{{ isMuted ? 'mic_off' : 'mic' }}</span>
          </button>

          <!-- 摄像头 -->
          <button
            class="control-btn"
            :class="{ active: !isCameraOff }"
            @click="toggleCamera"
            :title="isCameraOff ? '开启摄像头' : '关闭摄像头'"
          >
            <span class="material-icons-outlined">{{ isCameraOff ? 'videocam_off' : 'videocam' }}</span>
          </button>

          <!-- 扬声器 -->
          <button
            class="control-btn"
            :class="{ active: !isSpeakerOff }"
            @click="toggleSpeaker"
            :title="isSpeakerOff ? '开启扬声器' : '静音'"
          >
            <span class="material-icons-outlined">{{ isSpeakerOff ? 'volume_off' : 'volume_up' }}</span>
          </button>

          <!-- 屏幕共享 -->
          <button
            class="control-btn"
            @click="toggleScreenShare"
            :title="isScreenSharing ? '停止共享' : '屏幕共享'"
            :class="{ sharing: isScreenSharing }"
          >
            <span class="material-icons-outlined">{{ isScreenSharing ? 'stop_screen_share' : 'screen_share' }}</span>
          </button>

          <!-- 挂断 -->
          <button class="control-btn hangup" @click="handleHangup" title="挂断">
            <span class="material-icons-outlined">call_end</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 传统呼叫界面 -->
    <div v-else class="call-content">
      <div class="user-info">
        <!-- 群组使用图标，单聊使用钉钉风格头像 -->
        <div v-if="session?.type === 'GROUP'" class="call-avatar group-avatar">
          <span class="material-icons-outlined">groups</span>
        </div>
        <DingtalkAvatar
          v-else
          :src="session?.avatar"
          :name="session?.name"
          :user-id="session?.targetId || session?.peerUserId"
          :size="80"
          shape="circle"
          custom-class="call-avatar"
        />
        <h3 class="call-name">{{ session?.name }}</h3>
        <p class="call-status">{{ statusText }}</p>

        <!-- 通话类型标识 -->
        <div class="call-type-badge">
          <span class="material-icons-outlined">{{ isVideoCall ? 'videocam' : 'phone' }}</span>
          <span>{{ isVideoCall ? '视频通话' : '语音通话' }}</span>
        </div>
      </div>

      <div class="call-actions">
        <!-- 呼叫中 / 被叫中 -->
        <template v-if="status === 'calling' || status === 'incoming'">
          <div v-if="status === 'incoming'" class="incoming-actions">
            <button class="action-btn accept" @click="handleAccept">
              <span class="material-icons-outlined">{{ isVideoCall ? 'videocam' : 'phone' }}</span>
              <span>接听</span>
            </button>
            <button class="action-btn reject" @click="handleReject">
              <span class="material-icons-outlined">call_end</span>
              <span>挂断</span>
            </button>
          </div>
          <div v-else class="calling-actions">
            <div class="calling-animation">
              <div class="wave"></div>
              <div class="wave"></div>
              <div class="wave"></div>
            </div>
            <button class="action-btn reject" @click="handleCancel">
              <span class="material-icons-outlined">call_end</span>
              <span>取消</span>
            </button>
          </div>
        </template>

        <!-- 连接中 -->
        <template v-else-if="status === 'connecting'">
          <div class="connecting-animation">
            <div class="spinner"></div>
            <p>正在连接...</p>
          </div>
        </template>

        <!-- 通话中（语音） -->
        <template v-else-if="status === 'talking' && !isVideoCall">
          <div class="talking-actions">
            <div class="duration">{{ formattedDuration }}</div>
            <div class="voice-controls">
              <button
                class="control-btn small"
                :class="{ active: !isMuted }"
                @click="toggleMute"
                title="静音/取消静音"
              >
                <span class="material-icons-outlined">{{ isMuted ? 'mic_off' : 'mic' }}</span>
              </button>
              <button
                class="control-btn small"
                :class="{ active: !isSpeakerOff }"
                @click="toggleSpeaker"
                title="扬声器"
              >
                <span class="material-icons-outlined">{{ isSpeakerOff ? 'volume_off' : 'volume_up' }}</span>
              </button>
            </div>
            <button class="action-btn reject large" @click="handleHangup">
              <span class="material-icons-outlined">call_end</span>
              <span>挂断</span>
            </button>
          </div>
        </template>
      </div>
    </div>

    <!-- 群组多人视频通话 -->
    <div v-if="isGroupCall && status === 'talking' && isVideoCall" class="group-video-grid">
      <div
        v-for="participant in participants"
        :key="participant.userId"
        class="video-tile"
        :class="{
          speaking: participant.isSpeaking,
          main: participant.isMain
        }"
      >
        <video
          :ref="`participantVideo_${participant.userId}`"
          autoplay
          playsinline
          muted
        ></video>
        <div class="participant-info">
          <span class="name">{{ participant.name }}</span>
          <span v-if="participant.isMuted" class="muted-icon">
            <span class="material-icons-outlined">mic_off</span>
          </span>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import * as videoCallApi from '@/api/im/videoCall'

const props = defineProps({
  session: Object
})

const emit = defineEmits(['accept', 'reject', 'end', 'closed'])

// 状态管理
const visible = ref(false)
const status = ref('idle') // idle, calling, incoming, connecting, talking, hanging_up
const callType = ref('video') // voice, video
const duration = ref(0)
const callId = ref(null)
const isGroupCall = ref(false)
const participants = ref([])

// 媒体状态
const isMuted = ref(false)
const isCameraOff = ref(false)
const isSpeakerOff = ref(false)
const isScreenSharing = ref(false)
const localVideoMinimized = ref(false)
const remoteStreamLoaded = ref(false)
const localStream = ref(null)
const remoteStream = ref(null)

// 定时器
let durationTimer = null
let callTimeoutTimer = null

// DOM 引用
const localVideoRef = ref(null)
const remoteVideoRef = ref(null)

// 计算属性
const isVideoCall = computed(() => callType.value === 'video')
const canClose = computed(() => status.value === 'hanging_up' || status.value === 'idle')
const title = computed(() => {
  switch (status.value) {
    case 'calling': return isVideoCall.value ? '视频通话中...' : '语音通话中...'
    case 'incoming': return ` incoming call`
    case 'connecting': return '连接中...'
    case 'talking': return isVideoCall.value ? '视频通话' : '语音通话'
    default: return ''
  }
})

const statusText = computed(() => {
  switch (status.value) {
    case 'idle': return ''
    case 'calling': return '正在等待对方接听...'
    case 'incoming': return '邀请你进行' + (isVideoCall.value ? '视频' : '语音') + '通话...'
    case 'connecting': return '正在连接...'
    case 'talking': return '通话中 ' + formattedDuration.value
    case 'hanging_up': return '通话已结束'
    default: return ''
  }
})

const formattedDuration = computed(() => {
  const m = Math.floor(duration.value / 60)
  const s = duration.value % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
})

// WebRTC 配置
const rtcConfig = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' }
  ]
}

let peerConnection = null
let mediaStream = null

// 打开通话弹窗
const open = async (options = {}) => {
  const {
    type = 'video',
    initialStatus = 'calling',
    callId: id = null,
    group = false,
    participantList = []
  } = options

  callType.value = type
  status.value = initialStatus
  callId.value = id
  isGroupCall.value = group
  participants.value = participantList.map(p => ({
    ...p,
    isMuted: false,
    isSpeaking: false,
    isMain: false
  }))
  duration.value = 0
  visible.value = true

  // 重置状态
  isMuted.value = false
  isCameraOff.value = false
  isSpeakerOff.value = false
  isScreenSharing.value = false
  remoteStreamLoaded.value = false

  // 如果是发起方，获取本地媒体流
  if (initialStatus === 'calling' || initialStatus === 'talking') {
    await startCall()
  }

  // 30秒无人接听自动挂断
  if (initialStatus === 'calling') {
    callTimeoutTimer = setTimeout(() => {
      if (status.value === 'calling') {
        handleCancel()
        ElMessage.warning('对方未接听')
      }
    }, 30000)
  }
}

// 开始通话
const startCall = async () => {
  try {
    status.value = 'connecting'

    // 获取本地媒体流
    const constraints = {
      audio: true,
      video: isVideoCall.value ? {
        width: { ideal: 1280 },
        height: { ideal: 720 }
      } : false
    }

    mediaStream = await navigator.mediaDevices.getUserMedia(constraints)
    localStream.value = mediaStream

    // 显示本地视频
    if (localVideoRef.value) {
      localVideoRef.value.srcObject = mediaStream
    }

    // 创建 WebRTC 连接
    if (!isGroupCall.value) {
      await createPeerConnection()
    }

    // 开始计时
    startTimer()

    status.value = 'talking'
  } catch (error) {
    console.error('获取媒体流失败:', error)
    ElMessage.error('无法访问摄像头/麦克风，请检查权限设置')
    handleCancel()
  }
}

// 创建 WebRTC 连接
const createPeerConnection = async () => {
  peerConnection = new RTCPeerConnection(rtcConfig)

  // 添加本地流
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => {
      peerConnection.addTrack(track, mediaStream)
    })
  }

  // 监听 ICE 候选
  peerConnection.onicecandidate = async (event) => {
    if (event.candidate && callId.value) {
      await videoCallApi.sendSignal(callId.value, 'ice-candidate', JSON.stringify(event.candidate))
    }
  }

  // 监听远程流
  peerConnection.ontrack = (event) => {
    if (event.streams && event.streams[0]) {
      remoteStream.value = event.streams[0]
      if (remoteVideoRef.value) {
        remoteVideoRef.value.srcObject = event.streams[0]
      }
      remoteStreamLoaded.value = true
    }
  }

  // 创建 offer
  const offer = await peerConnection.createOffer()
  await peerConnection.setLocalDescription(offer)

  // 发送 offer 到服务器
  if (callId.value) {
    await videoCallApi.sendSignal(callId.value, 'offer', JSON.stringify(offer))
  }
}

// 处理远程 offer
const handleRemoteOffer = async (offerData) => {
  try {
    status.value = 'connecting'

    await createPeerConnection()

    const offer = new RTCSessionDescription({
      type: 'offer',
      sdp: offerData
    })

    await peerConnection.setRemoteDescription(offer)

    // 创建 answer
    const answer = await peerConnection.createAnswer()
    await peerConnection.setLocalDescription(answer)

    // 发送 answer
    if (callId.value) {
      await videoCallApi.sendSignal(callId.value, 'answer', JSON.stringify(answer))
    }

    startTimer()
  } catch (error) {
    console.error('处理远程offer失败:', error)
  }
}

// 处理远程 answer
const handleRemoteAnswer = async (answerData) => {
  try {
    const answer = new RTCSessionDescription({
      type: 'answer',
      sdp: answerData
    })

    await peerConnection.setRemoteDescription(answer)
    status.value = 'talking'
  } catch (error) {
    console.error('处理远程answer失败:', error)
  }
}

// 处理 ICE 候选
const handleIceCandidate = async (candidateData) => {
  try {
    const candidate = new RTCIceCandidate(JSON.parse(candidateData))
    await peerConnection.addIceCandidate(candidate)
  } catch (error) {
    console.error('添加ICE候选失败:', error)
  }
}

// 接听通话
const handleAccept = async () => {
  try {
    // 调用后端 API
    if (callId.value) {
      await videoCallApi.acceptCall(callId.value)
    }

    emit('accept', { callId: callId.value })

    // 开始获取媒体流
    await startCall()
  } catch (error) {
    console.error('接听通话失败:', error)
    ElMessage.error('接听失败')
  }
}

// 拒绝通话
const handleReject = async () => {
  try {
    if (callId.value) {
      await videoCallApi.rejectCall(callId.value)
    }
    emit('reject', { callId: callId.value })
    close()
  } catch (error) {
    console.error('拒绝通话失败:', error)
  }
}

// 取消通话
const handleCancel = async () => {
  try {
    if (callId.value) {
      await videoCallApi.endCall(callId.value)
    }
    close()
  } catch (error) {
    console.error('取消通话失败:', error)
  }
}

// 挂断
const handleHangup = async () => {
  try {
    status.value = 'hanging_up'

    if (callId.value) {
      await videoCallApi.endCall(callId.value)
    }

    emit('end', { callId: callId.value, duration: duration.value })

    stopTimer()

    // 延迟关闭弹窗
    setTimeout(() => {
      close()
    }, 500)
  } catch (error) {
    console.error('挂断失败:', error)
  }
}

// 控制按钮功能
const toggleMute = () => {
  if (mediaStream) {
    const audioTrack = mediaStream.getAudioTracks()[0]
    if (audioTrack) {
      isMuted.value = !isMuted.value
      audioTrack.enabled = !isMuted.value
    }
  }
}

const toggleCamera = async () => {
  if (mediaStream) {
    const videoTrack = mediaStream.getVideoTracks()[0]
    if (videoTrack) {
      isCameraOff.value = !isCameraOff.value
      videoTrack.enabled = !isCameraOff.value
    }
  }
}

const toggleSpeaker = () => {
  isSpeakerOff.value = !isSpeakerOff.value
  if (remoteVideoRef.value) {
    remoteVideoRef.value.muted = isSpeakerOff.value
  }
}

const toggleScreenShare = async () => {
  if (isScreenSharing.value) {
    // 停止屏幕共享
    stopScreenShare()
  } else {
    // 开始屏幕共享
    try {
      const screenStream = await navigator.mediaDevices.getDisplayMedia({
        video: { cursor: true }
      })

      // 替换视频轨道
      if (peerConnection && mediaStream) {
        const videoTrack = screenStream.getVideoTracks()[0]
        const sender = peerConnection.getSenders().find(s => s.track.kind === 'video')
        if (sender) {
          await sender.replaceTrack(videoTrack)
        }
      }

      isScreenSharing.value = true

      // 监听共享停止
      screenStream.getVideoTracks()[0].onended = () => {
        stopScreenShare()
      }
    } catch (error) {
      console.error('屏幕共享失败:', error)
      ElMessage.error('屏幕共享失败')
    }
  }
}

const stopScreenShare = () => {
  // 恢复摄像头
  if (mediaStream && peerConnection) {
    const videoTrack = mediaStream.getVideoTracks()[0]
    const sender = peerConnection.getSenders().find(s => s.track.kind === 'video')
    if (sender && videoTrack) {
      sender.replaceTrack(videoTrack)
    }
  }
  isScreenSharing.value = false
}

const toggleLocalVideoMinimize = () => {
  localVideoMinimized.value = !localVideoMinimized.value
}

// 计时器
const startTimer = () => {
  stopTimer()
  durationTimer = setInterval(() => {
    duration.value++
  }, 1000)
}

const stopTimer = () => {
  if (durationTimer) {
    clearInterval(durationTimer)
    durationTimer = null
  }
  if (callTimeoutTimer) {
    clearTimeout(callTimeoutTimer)
    callTimeoutTimer = null
  }
}

// 清理资源
const cleanup = () => {
  stopTimer()

  if (localStream) {
    localStream.getTracks().forEach(track => track.stop())
    localStream = null
  }

  if (peerConnection) {
    peerConnection.close()
    peerConnection = null
  }

  localStream.value = null
  remoteStream.value = null
}

const close = () => {
  cleanup()
  visible.value = false
  status.value = 'idle'
}

const handleClosed = () => {
  emit('closed')
}

const handleLocalLoaded = () => {
  console.log('Local video loaded')
}

const handleRemoteLoaded = () => {
  console.log('Remote video loaded')
}

// 监听 WebSocket 信令消息
const setupWebSocketSignalListener = () => {
  // 在实际项目中，这里应该监听来自后端的 WebSocket 消息
  // 消息格式: { type: 'webrtc_signal', callId, signalType, signalData, fromUserId }
}

// 生命周期
onUnmounted(() => {
  cleanup()
})

// 暴露方法
defineExpose({
  open,
  close,
  handleRemoteOffer,
  handleRemoteAnswer,
  handleIceCandidate,
  setStatus: (s) => status.value = s,
  getStatus: () => status.value
})
</script>

<style scoped lang="scss">
.call-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog) {
    border-radius: var(--dt-radius-2xl);
    overflow: hidden;
  }
}

.call-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;
  padding: 40px 20px;
}

.user-info {
  text-align: center;

  .call-avatar {
    margin-bottom: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .group-avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36px;
    margin: 0 auto 16px;
  }

  .call-name {
    font-size: 20px;
    font-weight: 600;
    margin: 8px 0;
    color: var(--dt-text-primary);
  }

  .call-status {
    font-size: 14px;
    color: var(--dt-text-secondary);
  }

  .call-type-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 12px;
    background: var(--dt-bg-body);
    border-radius: 20px;
    font-size: 12px;
    color: var(--dt-text-secondary);
    margin-top: 8px;

    .material-icons-outlined {
      font-size: 16px;
    }
  }
}

.call-actions {
  width: 100%;
}

.incoming-actions,
.calling-actions {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.talking-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  width: 100%;

  .duration {
    font-size: 28px;
    font-weight: 600;
    font-family: 'SF Mono', 'Monaco', monospace;
    color: var(--dt-text-primary);
  }

  .voice-controls {
    display: flex;
    gap: 12px;
  }
}

.calling-animation {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;

  .wave {
    width: 8px;
    height: 40px;
    background: var(--dt-brand-color);
    border-radius: 4px;
    animation: wave 1.2s infinite-in-out;

    &:nth-child(2) { animation-delay: 0.1s; }
    &:nth-child(3) { animation-delay: 0.2s; }
  }
}

@keyframes wave {
  0%, 100% { transform: scaleY(0.4); }
  50% { transform: scaleY(1); }
}

.connecting-animation {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;

  .spinner {
    width: 40px;
    height: 40px;
    border: 3px solid var(--dt-border-color);
    border-top-color: var(--dt-brand-color);
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  p {
    color: var(--dt-text-secondary);
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 50%;
  padding: 16px;
  min-width: 64px;

  &:hover {
    transform: scale(1.05);
  }

  .material-icons-outlined {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 24px;
  }

  span:not(.material-icons-outlined) {
    font-size: 13px;
    color: var(--dt-text-secondary);
  }

  &.accept .material-icons-outlined {
    background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
    box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
  }

  &.reject .material-icons-outlined {
    background: linear-gradient(135deg, #f54a45 0%, #ff7875 100%);
    box-shadow: 0 4px 12px rgba(245, 74, 69, 0.3);
  }

  &.large {
    padding: 20px;
    min-width: 72px;

    .material-icons-outlined {
      width: 56px;
      height: 56px;
      font-size: 28px;
    }
  }
}

// ==================== 视频通话样式 ====================

.video-call-container {
  display: flex;
  flex-direction: column;
  height: 600px;
  background: #000;
}

.video-area {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.remote-video {
  width: 100%;
  height: 100%;
  position: relative;
  background: #1a1a1a;

  video {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .video-placeholder {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    color: #666;

    .material-icons-outlined {
      font-size: 48px;
    }
  }
}

.local-video {
  position: absolute;
  bottom: 100px;
  right: 20px;
  width: 180px;
  height: 135px;
  background: #000;
  border-radius: 8px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
  transition: all 0.3s ease;

  &.minimized {
    width: 120px;
    height: 90px;
    bottom: 20px;
  }

  video {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .video-controls-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
    padding: 8px;
    display: flex;
    justify-content: flex-end;
    opacity: 0;
    transition: opacity 0.2s;

    &:hover {
      opacity: 1;
    }

    .material-icons-outlined {
      color: #fff;
      font-size: 20px;
    }
  }

  &:hover .video-controls-overlay {
    opacity: 1;
  }
}

.call-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);

  .duration {
    font-size: 16px;
    color: #fff;
    font-family: 'SF Mono', 'Monaco', monospace;
  }

  .control-buttons {
    display: flex;
    gap: 12px;
  }
}

.control-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  .material-icons-outlined {
    font-size: 24px;
  }

  &:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: scale(1.1);
  }

  &.active {
    background: rgba(255, 255, 255, 0.3);
  }

  &.hangup {
    background: #f54a45;

    &:hover {
      background: #ff7875;
    }
  }

  &.sharing {
    background: #1677ff;
  }

  &.small {
    width: 40px;
    height: 40px;

    .material-icons-outlined {
      font-size: 20px;
    }
  }
}

// ==================== 群组视频网格 ====================

.group-video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 8px;
  padding: 8px;
  height: calc(600px - 80px);
  background: #000;
  overflow-y: auto;
}

.video-tile {
  position: relative;
  background: #1a1a1a;
  border-radius: 8px;
  overflow: hidden;
  aspect-ratio: 16/9;

  &.main {
    grid-column: span 2;
    grid-row: span 2;
    border: 2px solid var(--dt-brand-color);
  }

  &.speaking::after {
    content: '';
    position: absolute;
    inset: 0;
    border: 2px solid #52c41a;
    border-radius: 8px;
    pointer-events: none;
  }

  video {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .participant-info {
    position: absolute;
    bottom: 8px;
    left: 8px;
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 4px;
    color: #fff;
    font-size: 12px;

    .muted-icon {
      .material-icons-outlined {
        font-size: 14px;
      }
    }
  }
}

// ==================== 响应式 ====================

@media (max-width: 768px) {
  .call-dialog {
    :deep(.el-dialog) {
      width: 100% !important;
      height: 100vh;
      margin: 0;
      border-radius: 0;
    }
  }

  .video-call-container {
    height: 100vh;
  }

  .local-video {
    width: 120px;
    height: 90px;
    bottom: 80px;
    right: 12px;

    &.minimized {
      width: 80px;
      height: 60px;
    }
  }

  .call-controls {
    padding: 12px 16px;

    .duration {
      font-size: 14px;
    }

    .control-buttons {
      gap: 8px;
    }
  }

  .control-btn {
    width: 44px;
    height: 44px;

    &.small {
      width: 36px;
      height: 36px;
    }
  }

  .group-video-grid {
    grid-template-columns: 1fr;
  }
}
</style>
