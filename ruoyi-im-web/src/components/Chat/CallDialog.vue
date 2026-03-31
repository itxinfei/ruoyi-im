<template>
  <el-dialog
    v-model="visible"
    :width="dialogWidth"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    class="call-dialog-immersive"
    :class="{ 'is-video': type === 'video', 'is-talking': status === 'talking' }"
  >
    <div class="call-stage">
      <!-- 1. 视频流容器 (沉浸式全屏) -->
      <div v-if="type === 'video'" class="video-canvas">
        <div class="remote-track">
          <video
            ref="remoteVideoRef"
            autoplay
            playsinline
            class="video-element"
          />
          <!-- 未接通/对端关闭摄像头 占位 -->
          <div v-if="status !== 'talking' || remoteVideoOff" class="video-mask">
            <DingtalkAvatar
              :src="peerAvatar"
              :name="peerName"
              :size="120"
              shape="circle"
            />
            <p class="mask-status">
              {{ statusText }}
            </p>
          </div>
        </div>

        <!-- 本地预览 (小窗) -->
        <div v-show="!localVideoOff" class="local-track">
          <video
            ref="localVideoRef"
            autoplay
            playsinline
            muted
            class="video-element mirror"
          />
        </div>
      </div>

      <!-- 2. 语音/呼叫信息区 -->
      <div v-if="type === 'voice' || status !== 'talking'" class="calling-info">
        <div class="avatar-ripple" :class="{ 'animating': status === 'calling' }">
          <DingtalkAvatar
            :src="peerAvatar"
            :name="peerName"
            :user-id="peerId"
            :size="100"
            shape="circle"
            custom-class="main-avatar"
          />
        </div>
        <h2 class="peer-name">
          {{ peerName }}
        </h2>
        <p class="call-status-tag">
          {{ statusText }}
        </p>
        <div v-if="status === 'talking'" class="call-timer">
          {{ formattedDuration }}
        </div>
      </div>

      <!-- 3. 控制控制台 (底栏) -->
      <div class="call-console">
        <!-- 呼入状态 -->
        <div v-if="status === 'incoming'" class="console-group">
          <button class="console-btn accept" @click="handleAccept">
            <div class="icon-circle">
              <el-icon><PhoneFilled /></el-icon>
            </div>
            <span>接听</span>
          </button>
          <button class="console-btn hangup" @click="handleReject">
            <div class="icon-circle">
              <el-icon><CloseBold /></el-icon>
            </div>
            <span>拒绝</span>
          </button>
        </div>

        <!-- 呼出状态 -->
        <div v-else-if="status === 'calling'" class="console-group">
          <button class="console-btn hangup" @click="handleCancel">
            <div class="icon-circle">
              <el-icon><CloseBold /></el-icon>
            </div>
            <span>取消</span>
          </button>
        </div>

        <!-- 通话中状态 -->
        <div v-else-if="status === 'talking'" class="console-group talking">
          <button class="console-btn" :class="{ active: isMuted }" @click="toggleMute">
            <div class="icon-circle">
              <el-icon><Microphone v-if="!isMuted" /><Mute v-else /></el-icon>
            </div>
            <span>{{ isMuted ? '取消静音' : '静音' }}</span>
          </button>

          <button
            v-if="type === 'video'"
            class="console-btn"
            :class="{ active: localVideoOff }"
            @click="toggleVideo"
          >
            <div class="icon-circle">
              <el-icon><VideoCamera v-if="!localVideoOff" /><VideoCameraFilled v-else /></el-icon>
            </div>
            <span>摄像头</span>
          </button>

          <button class="console-btn hangup" @click="handleHangup">
            <div class="icon-circle">
              <el-icon><CloseBold /></el-icon>
            </div>
            <span>挂断</span>
          </button>
        </div>

        <!-- 结束/超时 -->
        <div v-else class="console-group">
          <button class="console-btn" @click="close">
            <div class="icon-circle gray">
              <el-icon><Close /></el-icon>
            </div>
            <span>关闭</span>
          </button>
        </div>
      </div>
    </div>

    <audio ref="remoteAudioRef" autoplay />
  </el-dialog>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  PhoneFilled, CloseBold, Microphone, VideoCamera,
  VideoCameraFilled, Close, Mute
} from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { useWebRTC } from '@/composables/useWebRTC'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { acceptCall, rejectCall, endCall } from '@/api/im/videoCall'

const props = defineProps({ session: Object })
defineEmits(['accept', 'reject', 'cancel', 'hangup', 'closed'])

const { sendMessage } = useImWebSocket()
const visible = ref(false)
const status = ref('calling')
const type = ref('voice')
const duration = ref(0)
const callId = ref('')
const peerId = ref(null)
const peerName = ref('')
const peerAvatar = ref('')
const isMuted = ref(false)
const localVideoOff = ref(false)
const remoteVideoOff = ref(false)
const pendingOffer = ref(null)

const localVideoRef = ref(null)
const remoteVideoRef = ref(null)
const remoteAudioRef = ref(null)
let localStream = null
let timer = null
let timeoutTimer = null

const dialogWidth = computed(() => type.value === 'video' ? '800px' : '360px')

const { createOffer, createAnswer, handleAnswer, handleCandidate, closePeerConnection } = useWebRTC({
  sendSignal: (action, data) => sendMessage({ type: 'call', data: { action, ...data } }),
  localVideo: localVideoRef, remoteVideo: remoteVideoRef, remoteAudio: remoteAudioRef
})

const statusText = computed(() => {
  switch (status.value) {
    case 'calling': return '正在呼叫对方...'
    case 'incoming': return '邀请你进行通话'
    case 'talking': return '正在通话'
    case 'hanging_up': return '通话已结束'
    case 'timeout': return '对方无应答'
    default: return ''
  }
})

const formattedDuration = computed(() => {
  const m = Math.floor(duration.value / 60); const s = duration.value % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
})

const getMediaStream = async (isVideo) => {
  try {
    if (localStream) localStream.getTracks().forEach(t => t.stop())
    localStream = await navigator.mediaDevices.getUserMedia({ audio: true, video: isVideo })
    if (isVideo && localVideoRef.value) localVideoRef.value.srcObject = localStream
    return true
  } catch { return false }
}

const toggleMute = () => { if (localStream) { isMuted.value = !isMuted.value; localStream.getAudioTracks()[0].enabled = !isMuted.value } }
const toggleVideo = () => { if (localStream && type.value === 'video') { localVideoOff.value = !localVideoOff.value; localStream.getVideoTracks()[0].enabled = !localVideoOff.value } }

const open = async (callType, options = {}) => {
  type.value = callType; status.value = options.status || 'calling'; callId.value = options.callId || `call-${Date.now()}`
  peerId.value = options.peerId ?? props.session?.targetId; peerName.value = options.peerName || props.session?.name; peerAvatar.value = options.peerAvatar || props.session?.avatar
  visible.value = true; duration.value = 0

  // 保存 pending offer（如果有）
  if (options.pendingOffer) {
    pendingOffer.value = options.pendingOffer
  }

  if (status.value === 'calling') {
    if (await getMediaStream(callType === 'video')) {
      await createOffer(callId.value, peerId.value, localStream)
      // 设置超时
      startTimeout()
    } else {
      close()
    }
  }

  if (status.value === 'talking') startTimer()
}

/**
 * 处理 WebRTC 信令
 */
const handleWebRTCSignal = async (action, data) => {
  if (!visible.value || data.callId !== callId.value) return

  switch (action) {
    case 'offer':
      // 收到对方 offer（已通过 handleCallEvent 处理）
      break

    case 'answer':
      // 收到对方应答
      if (data.sdp) {
        await handleAnswer(data.sdp)
        // 清除超时
        clearTimeout(timeoutTimer)
      }
      break

    case 'candidate':
      // 收到 ICE 候选者
      if (data.candidate) {
        await handleCandidate(data.candidate)
      }
      break
  }
}

/**
 * 启动超时计时器
 */
const startTimeout = () => {
  timeoutTimer = setTimeout(() => {
    status.value = 'timeout'
    ElMessage.warning('对方无应答')
    setTimeout(() => {
      end()
      sendMessage({ type: 'call', data: { action: 'hangup', callId: callId.value, peerId: peerId.value } })
    }, 2000)
  }, 60000) // 60秒超时
}

const handleAccept = async () => {
  try {
    // 先通过后端 API 接听通话
    const res = await acceptCall(callId.value)
    if (res.code !== 200) {
      throw new Error(res.message || '接听通话失败')
    }

    // 获取媒体流并建立 WebRTC 连接
    if (await getMediaStream(type.value === 'video')) {
      if (pendingOffer.value) {
        await createAnswer(callId.value, peerId.value, pendingOffer.value, localStream)
        status.value = 'talking'
        startTimer()
      }
    } else {
      handleReject()
    }
  } catch (error) {
    console.error('接听通话失败:', error)
    ElMessage.error(error.message || '接听通话失败')
    handleReject()
  }
}

const handleReject = async () => {
  try {
    // 通过后端 API 拒绝通话
    await rejectCall(callId.value, '用户拒绝')
  } catch (error) {
    console.error('拒绝通话失败:', error)
  } finally {
    end()
    sendMessage({ type: 'call', data: { action: 'reject', callId: callId.value, peerId: peerId.value } })
  }
}

const handleHangup = async () => {
  try {
    // 通过后端 API 结束通话
    await endCall(callId.value)
  } catch (error) {
    console.error('结束通话失败:', error)
  } finally {
    end()
    sendMessage({ type: 'call', data: { action: 'hangup', callId: callId.value, peerId: peerId.value } })
  }
}

const handleCancel = async () => {
  try {
    // 通过后端 API 取消通话
    await endCall(callId.value)
  } catch (error) {
    console.error('取消通话失败:', error)
  } finally {
    end()
    sendMessage({ type: 'call', data: { action: 'cancel', callId: callId.value, peerId: peerId.value } })
  }
}

const startTimer = () => { timer = setInterval(() => { duration.value++ }, 1000) }
const end = () => { clearInterval(timer); if (localStream) localStream.getTracks().forEach(t => t.stop()); closePeerConnection(); status.value = 'hanging_up'; setTimeout(() => { visible.value = false }, 1500) }
const close = () => { visible.value = false }

onUnmounted(() => {
  clearInterval(timer)
  clearTimeout(timeoutTimer)
  if (localStream) localStream.getTracks().forEach(t => t.stop())
})
defineExpose({ open, end, handleWebRTCSignal, callId })
</script>

<style scoped lang="scss">
@mixin flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

@mixin button-reset {
  background: none;
  border: none;
  padding: 0;
  margin: 0;
  cursor: pointer;
  color: inherit;
  font: inherit;
}

.call-dialog-immersive {
  background: var(--dt-bg-card-dark) !important;
  :deep(.el-dialog) { background: var(--dt-bg-card-dark); border-radius: var(--dt-radius-xl); overflow: hidden; box-shadow: var(--dt-shadow-modal); }
  :deep(.el-dialog__header) { display: none; }
  :deep(.el-dialog__body) { padding: 0 !important; }
}

.call-stage {
  position: relative; width: 100%; height: 520px; display: flex; flex-direction: column; align-items: center; justify-content: space-between;
  padding: var(--dt-spacing-2xl) 0; color: var(--dt-text-white);
}

.video-canvas {
  position: absolute; inset: 0; background: var(--dt-bg-card-dark); z-index: 1;
  .remote-track {
    width: 100%; height: 100%; position: relative;
    .video-element { width: 100%; height: 100%; object-fit: cover; }
    .video-mask { position: absolute; inset: 0; @include flex-center; flex-direction: column; background: var(--dt-overlay-bg); backdrop-filter: blur(10px); }
  }
  .local-track {
    position: absolute; top: var(--dt-spacing-lg); right: var(--dt-spacing-lg); width: 140px; height: 180px;
    border-radius: var(--dt-radius-md); overflow: hidden; border: 2px solid rgba(255,255,255,0.2); box-shadow: var(--dt-shadow-modal);
    .video-element { width: 100%; height: 100%; object-fit: cover; &.mirror { transform: scaleX(-1); } }
  }
}

.calling-info {
  position: relative; z-index: 2; text-align: center; margin-top: var(--dt-spacing-xl);
  .avatar-ripple {
    position: relative; margin-bottom: var(--dt-spacing-xl);
    &.animating::after {
      content: ''; position: absolute; inset: -15px; border-radius: var(--dt-radius-full); border: 2px solid var(--dt-brand-color);
      animation: ripple 2s infinite; opacity: 0;
    }
  }
  .peer-name { font-size: 24px; font-weight: 600; margin: var(--dt-spacing-md) 0; }
  .call-status-tag { font-size: 14px; color: var(--dt-text-white); opacity: 0.7; }
  .call-timer { font-size: 20px; font-family: monospace; margin-top: var(--dt-spacing-md); color: var(--dt-brand-color); }
}

.call-console {
  position: relative; z-index: 2; width: 100%; padding: 0 var(--dt-spacing-2xl);
  .console-group { display: flex; justify-content: center; gap: var(--dt-spacing-2xl);
    &.talking { gap: 32px; }
  }
}

.console-btn {
  @include button-reset; display: flex; flex-direction: column; align-items: center; gap: 8px;
  .icon-circle {
    width: 64px; height: 64px; border-radius: var(--dt-radius-full); @include flex-center; font-size: 28px;
    background: rgba(255,255,255,0.1); backdrop-filter: blur(4px); transition: all 0.2s;
    &:hover { background: rgba(255,255,255,0.2); transform: translateY(-2px); }
  }
  span { font-size: 13px; color: var(--dt-text-white); opacity: 0.8; }

  &.accept .icon-circle { background: var(--dt-success-color); &:hover { background: var(--dt-brand-hover); } }
  &.hangup .icon-circle { background: var(--dt-error-color); &:hover { background: var(--dt-error-color); filter: brightness(1.2); } .el-icon { transform: rotate(135deg); } }
  &.active .icon-circle { background: var(--dt-text-white); color: var(--dt-text-primary); }
  .icon-circle.gray { background: var(--dt-text-tertiary-dark); }
}

@keyframes ripple {
  0% { transform: scale(1); opacity: 0.5; }
  100% { transform: scale(1.5); opacity: 0; }
}
</style>
