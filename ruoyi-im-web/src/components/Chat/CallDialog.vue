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
          <video ref="remoteVideoRef" autoplay playsinline class="video-element" />
          <div v-if="status !== 'talking' || remoteVideoOff" class="video-mask">
            <DingtalkAvatar :src="peerAvatar" :name="peerName" :size="120" shape="circle" />
            <p class="mask-status">{{ statusText }}</p>
          </div>
        </div>
        <div v-show="!localVideoOff" class="local-track">
          <video ref="localVideoRef" autoplay playsinline muted class="video-element mirror" />
        </div>
      </div>

      <!-- 2. 呼叫信息区 -->
      <div v-if="type === 'voice' || status !== 'talking'" class="calling-info">
        <div class="avatar-ripple-container">
          <div class="ripple" v-if="status === 'calling' || status === 'incoming'"></div>
          <div class="ripple delay-1" v-if="status === 'calling' || status === 'incoming'"></div>
          <DingtalkAvatar :src="peerAvatar" :name="peerName" :size="100" shape="circle" custom-class="main-avatar" />
        </div>
        <h2 class="peer-name">{{ peerName }}</h2>
        <p class="call-status-tag">{{ statusText }}</p>
        <div v-if="status === 'talking'" class="call-timer">{{ formattedDuration }}</div>
      </div>

      <!-- 3. 控制控制台 -->
      <div class="call-console">
        <div v-if="status === 'incoming'" class="console-group">
          <button class="console-btn accept" @click="handleAccept">
            <div class="icon-circle"><el-icon><PhoneFilled /></el-icon></div>
            <span>接听</span>
          </button>
          <button class="console-btn hangup" @click="handleReject">
            <div class="icon-circle"><el-icon><CloseBold /></el-icon></div>
            <span>拒绝</span>
          </button>
        </div>

        <div v-else-if="status === 'calling'" class="console-group">
          <button class="console-btn hangup" @click="handleCancel">
            <div class="icon-circle"><el-icon><CloseBold /></el-icon></div>
            <span>取消</span>
          </button>
        </div>

        <div v-else-if="status === 'talking'" class="console-group talking">
          <button class="console-btn" :class="{ active: isMuted }" @click="toggleMute">
            <div class="icon-circle"><el-icon><Microphone v-if="!isMuted" /><Mute v-else /></el-icon></div>
            <span>{{ isMuted ? '静音' : '已静音' }}</span>
          </button>
          <button v-if="type === 'video'" class="console-btn" :class="{ active: localVideoOff }" @click="toggleVideo">
            <div class="icon-circle"><el-icon><VideoCamera v-if="!localVideoOff" /><VideoCameraFilled v-else /></el-icon></div>
            <span>摄像头</span>
          </button>
          <button class="console-btn hangup" @click="handleHangup">
            <div class="icon-circle"><el-icon><CloseBold /></el-icon></div>
            <span>挂断</span>
          </button>
        </div>

        <div v-else class="console-group">
          <button class="console-btn" @click="close">
            <div class="icon-circle gray"><el-icon><Close /></el-icon></div>
            <span>关闭</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 音效资产 -->
    <audio ref="ringtoneAudio" loop src="/assets/audio/ringtone.mp3"></audio>
    <audio ref="dialtoneAudio" loop src="/assets/audio/dialtone.mp3"></audio>
    <audio ref="remoteAudioRef" autoplay></audio>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { PhoneFilled, CloseBold, Microphone, VideoCamera, VideoCameraFilled, Close, Mute, RefreshRight } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { useWebRTC } from '@/composables/useWebRTC'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { acceptCall, rejectCall, endCall } from '@/api/im/videoCall'

const props = defineProps({ session: Object })
const emit = defineEmits(['closed'])

const { sendMessage } = useImWebSocket()
const visible = ref(false)
const status = ref('calling') // calling, incoming, talking, hanging_up, timeout, reconnecting
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
const ringtoneAudio = ref(null)
const dialtoneAudio = ref(null)

let localStream = null
let timer = null
let timeoutTimer = null

const dialogWidth = computed(() => type.value === 'video' ? '800px' : '360px')

const { createOffer, createAnswer, handleAnswer, handleCandidate, closePeerConnection } = useWebRTC({
  sendSignal: (action, data) => sendMessage({ type: 'call', data: { action, ...data } }),
  onConnectionStateChange: (state) => {
    if (state === 'reconnecting') status.value = 'reconnecting'
    if (state === 'connected') status.value = 'talking'
  },
  remoteVideo: remoteVideoRef, remoteAudio: remoteAudioRef
})

const statusText = computed(() => {
  const map = {
    calling: '正在呼叫对方...', incoming: '邀请你进行通话', talking: '正在通话',
    hanging_up: '通话已结束', timeout: '对方无应答', reconnecting: '网络不稳定，正在重连...'
  }
  return map[status.value] || ''
})

const formattedDuration = computed(() => {
  const m = Math.floor(duration.value / 60); const s = duration.value % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
})

const playAudio = (t) => { if (t === 'ring') ringtoneAudio.value?.play().catch(() => {}); else dialtoneAudio.value?.play().catch(() => {}) }
const stopAudio = () => { [ringtoneAudio, dialtoneAudio].forEach(a => { if (a.value) { a.value.pause(); a.value.currentTime = 0 } }) }

const getMediaStream = async (isVideo) => {
  try {
    if (localStream) localStream.getTracks().forEach(t => t.stop())
    localStream = await navigator.mediaDevices.getUserMedia({ audio: true, video: isVideo })
    if (isVideo && localVideoRef.value) localVideoRef.value.srcObject = localStream
    return true
  } catch (e) {
    ElMessage.error('无法获取摄像头或麦克风权限，请检查系统设置')
    return false
  }
}

const open = async (callType, options = {}) => {
  type.value = callType; status.value = options.status || 'calling'; callId.value = options.callId || `call-${Date.now()}`
  peerId.value = options.peerId; peerName.value = options.peerName; peerAvatar.value = options.peerAvatar
  pendingOffer.value = options.pendingOffer; visible.value = true; duration.value = 0
  
  if (status.value === 'incoming') playAudio('ring')
  if (status.value === 'calling') {
    playAudio('dial')
    if (await getMediaStream(callType === 'video')) {
      createOffer(callId.value, peerId.value, localStream)
      timeoutTimer = setTimeout(() => { if (status.value === 'calling') handleCancel() }, 45000)
    } else close()
  }
}

const handleAccept = async () => {
  stopAudio()
  if (await getMediaStream(type.value === 'video')) {
    await acceptCall(callId.value)
    if (pendingOffer.value) {
      await createAnswer(callId.value, peerId.value, pendingOffer.value, localStream)
      status.value = 'talking'
      startTimer()
    }
  } else handleReject()
}

const handleReject = () => { stopAudio(); rejectCall(callId.value); end('reject') }
const handleHangup = () => { endCall(callId.value); end('hangup') }
const handleCancel = () => { endCall(callId.value); end('cancel') }

const startTimer = () => { clearInterval(timer); timer = setInterval(() => duration.value++, 1000) }
const end = (reason) => {
  stopAudio(); clearInterval(timer); clearTimeout(timeoutTimer)
  if (localStream) localStream.getTracks().forEach(t => t.stop())
  closePeerConnection()
  sendMessage({ type: 'call', data: { action: reason || 'hangup', callId: callId.value, peerId: peerId.value } })
  status.value = 'hanging_up'
  setTimeout(() => visible.value = false, 1200)
}

const toggleMute = () => { if (localStream) { isMuted.value = !isMuted.value; localStream.getAudioTracks()[0].enabled = !isMuted.value } }
const toggleVideo = () => { if (localStream) { localVideoOff.value = !localVideoOff.value; localStream.getVideoTracks()[0].enabled = !localVideoOff.value } }
const close = () => { visible.value = false; emit('closed') }

onUnmounted(() => { stopAudio(); clearInterval(timer); if (localStream) localStream.getTracks().forEach(t => t.stop()) })
defineExpose({ open, end, handleWebRTCSignal: (a, d) => { if (a === 'answer') { stopAudio(); handleAnswer(d.sdp); status.value = 'talking'; startTimer() } if (a === 'candidate') handleCandidate(d.candidate) }, callId })
</script>

<style scoped lang="scss">
.call-dialog-immersive {
  background: var(--dt-bg-card) !important;
  :deep(.el-dialog) { background: var(--dt-bg-card); border-radius: var(--dt-radius-xl); overflow: hidden; }
  :deep(.el-dialog__header) { display: none; }
}

.call-stage {
  position: relative; width: 100%; height: 540px; display: flex; flex-direction: column; align-items: center; justify-content: space-between;
  padding: 60px 0; color: var(--dt-text-white);
}

.avatar-ripple-container {
  position: relative; width: 100px; height: 100px; margin: 0 auto 30px;
  .ripple {
    position: absolute; inset: 0; border: 2px solid var(--dt-brand-color); border-radius: 50%;
    opacity: 0.5;
  }
  .delay-1 { opacity: 0.3; }
}

.video-canvas {
  position: absolute; inset: 0; background: var(--dt-text-primary); z-index: 1;
  .remote-track, .video-element { width: 100%; height: 100%; object-fit: cover; }
  .local-track {
    position: absolute; top: 20px; right: 20px; width: 120px; height: 160px;
    border-radius: 12px; overflow: hidden; border: 2px solid var(--dt-border-light); z-index: 2;
  }
}

.calling-info { position: relative; z-index: 2; text-align: center; }
.peer-name { font-size: 24px; margin-bottom: 8px; }
.call-status-tag { color: rgba(255,255,255,0.6); font-size: 14px; }
.call-timer { font-size: 18px; color: var(--dt-brand-color); margin-top: 12px; font-family: monospace; }

.call-console {
  position: relative; z-index: 2; width: 100%;
  .console-group { display: flex; justify-content: center; gap: 32px; }
}

.console-btn {
  background: none; border: none; cursor: pointer; color: var(--dt-text-white); display: flex; flex-direction: column; align-items: center; gap: 10px;
  .icon-circle {
    width: 64px; height: 64px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
    font-size: 28px; background: rgba(255,255,255,0.15); transition: background var(--dt-transition-fast);
    &:hover { background: rgba(255,255,255,0.25); }
  }
  &.accept .icon-circle { background: var(--dt-success-color); }
  &.hangup .icon-circle { background: var(--dt-error-color); }
  &.active .icon-circle { background: var(--dt-text-white); color: var(--dt-text-primary); }
  span { font-size: 12px; opacity: 0.8; }
}
</style>
