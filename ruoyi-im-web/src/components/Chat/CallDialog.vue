<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="type === 'video' && (status === 'talking' || status === 'calling') ? '600px' : '400px'"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    class="call-dialog"
    :class="{'video-mode': type === 'video' && status === 'talking'}"
  >
    <div class="call-content">
      <!-- 视频流展示区 -->
      <div v-if="type === 'video' && (status === 'talking' || status === 'calling')" class="video-container">
        <div class="remote-video-wrapper">
          <video ref="remoteVideoRef" autoplay playsinline class="remote-video"></video>
          <div v-if="status !== 'talking'" class="video-placeholder">
            <span class="material-icons-outlined">videocam_off</span>
            <p>等待对方接听...</p>
          </div>
        </div>
        <div class="local-video-wrapper">
          <video ref="localVideoRef" autoplay playsinline muted class="local-video"></video>
        </div>
      </div>

      <div v-else class="user-info">
        <div v-if="isGroupCall" class="call-avatar group-avatar">
          <span class="material-icons-outlined">groups</span>
        </div>
        <DingtalkAvatar
          v-else
          :src="peerAvatar"
          :name="peerName"
          :user-id="peerId"
          :size="80"
          shape="circle"
          custom-class="call-avatar"
        />
        <h3 class="call-name">{{ peerName }}</h3>
        <p class="call-status">{{ statusText }}</p>
      </div>

      <audio ref="remoteAudioRef" autoplay></audio>

      <div class="call-actions">
        <template v-if="status === 'calling' || status === 'incoming'">
          <div v-if="status === 'incoming'" class="incoming-actions">
            <button class="action-btn accept" @click="handleAccept">
              <span class="material-icons-outlined">phone</span>
              <span>接听</span>
            </button>
            <button class="action-btn reject" @click="handleReject">
              <span class="material-icons-outlined">call_end</span>
              <span>挂断</span>
            </button>
          </div>
          <div v-else class="calling-actions">
            <button class="action-btn reject" @click="handleCancel">
              <span class="material-icons-outlined">call_end</span>
              <span>取消</span>
            </button>
          </div>
        </template>

        <template v-else-if="status === 'talking'">
          <div class="talking-actions">
            <div class="duration">{{ formattedDuration }}</div>
            <button class="action-btn reject" @click="handleHangup">
              <span class="material-icons-outlined">call_end</span>
              <span>挂断</span>
            </button>
          </div>
        </template>

        <template v-else-if="status === 'timeout'">
          <div class="timeout-actions">
            <button class="action-btn reject" @click="close">
              <span class="material-icons-outlined">close</span>
              <span>关闭</span>
            </button>
          </div>
        </template>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { useWebRTC } from '@/composables/useWebRTC'
import { useImWebSocket } from '@/composables/useImWebSocket'

const props = defineProps({
  session: Object
})
const emit = defineEmits(['accept', 'reject', 'cancel', 'hangup', 'timeout', 'closed'])

const { sendMessage, onCall } = useImWebSocket()

const visible = ref(false)
const status = ref('calling')
const type = ref('voice')
const duration = ref(0)
const callId = ref('')
const peerId = ref(null)
const peerName = ref('')
const peerAvatar = ref('')
const pendingOffer = ref(null)

const localVideoRef = ref(null)
const remoteVideoRef = ref(null)
const remoteAudioRef = ref(null)
let localStream = null
let timer = null
let timeoutTimer = null
const CALL_TIMEOUT = 60000

const isGroupCall = computed(() => props.session?.type === 'GROUP')
const title = computed(() => type.value === 'voice' ? '语音通话' : '视频通话')

const { 
  createOffer, 
  createAnswer, 
  handleAnswer, 
  handleCandidate, 
  closePeerConnection 
} = useWebRTC({
  sendSignal: (action, data) => {
    sendMessage({ type: 'call', data: { action, ...data } })
  },
  localVideo: localVideoRef,
  remoteVideo: remoteVideoRef,
  remoteAudio: remoteAudioRef
})

const getMediaStream = async (isVideo) => {
  try {
    if (localStream) {
      localStream.getTracks().forEach(track => track.stop())
    }
    localStream = await navigator.mediaDevices.getUserMedia({
      audio: true,
      video: isVideo ? { width: 1280, height: 720 } : false
    })
    if (isVideo) {
      nextTick(() => {
        if (localVideoRef.value) localVideoRef.value.srcObject = localStream
      })
    }
    return true
  } catch (error) {
    ElMessage.error('无法访问麦克风或摄像头')
    return false
  }
}

const stopMediaStream = () => {
  if (localStream) {
    localStream.getTracks().forEach(track => track.stop())
    localStream = null
  }
  if (localVideoRef.value) localVideoRef.value.srcObject = null
  if (remoteVideoRef.value) remoteVideoRef.value.srcObject = null
  if (remoteAudioRef.value) remoteAudioRef.value.srcObject = null
}

const statusText = computed(() => {
  switch (status.value) {
    case 'calling': return '正在等待对方接受...'
    case 'incoming': return '邀请你进行通话...'
    case 'talking': return '正在通话中...'
    case 'hanging_up': return '通话已结束'
    case 'timeout': return '对方无应答'
    default: return ''
  }
})

const formattedDuration = computed(() => {
  const m = Math.floor(duration.value / 60)
  const s = duration.value % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
})

onCall((msg) => {
  if (!visible.value || msg.data.callId !== callId.value) return
  const { action, sdp, candidate } = msg.data
  switch (action) {
    case 'offer': pendingOffer.value = sdp; break
    case 'answer': handleAnswer(sdp); break
    case 'candidate': handleCandidate(candidate); break
    case 'reject': case 'cancel': case 'hangup': end(); break
  }
})

const open = async (callType, options = {}) => {
  type.value = callType
  status.value = options.status || 'calling'
  callId.value = options.callId || `call-${Date.now()}`
  peerId.value = options.peerId ?? props.session?.targetId ?? props.session?.targetUserId ?? null
  peerName.value = options.peerName || props.session?.name || '未知'
  peerAvatar.value = options.peerAvatar || props.session?.avatar || ''
  visible.value = true
  duration.value = 0
  
  if (status.value === 'calling') {
    const success = await getMediaStream(callType === 'video')
    if (success) await createOffer(callId.value, peerId.value, localStream)
    else close()
    startTimeout()
  }
  if (status.value === 'talking') startTimer()
}

const handleAccept = async () => {
  stopTimeout()
  const success = await getMediaStream(type.value === 'video')
  if (!success) { handleReject(); return }
  if (pendingOffer.value) {
    await createAnswer(callId.value, peerId.value, pendingOffer.value, localStream)
  }
  status.value = 'talking'
  startTimer()
  emit('accept', { callId: callId.value, callType: type.value, peerId: peerId.value })
}

const handleReject = () => {
  stopTimeout(); stopMediaStream(); closePeerConnection()
  emit('reject', { callId: callId.value, peerId: peerId.value })
  close()
}

const handleCancel = () => {
  stopTimeout(); stopMediaStream(); closePeerConnection()
  emit('cancel', { callId: callId.value, peerId: peerId.value })
  close()
}

const handleHangup = () => {
  stopMediaStream(); closePeerConnection()
  emit('hangup', { callId: callId.value, peerId: peerId.value })
  close()
}

const startTimer = () => {
  stopTimer()
  timer = setInterval(() => { duration.value++ }, 1000)
}

const stopTimer = () => { if (timer) { clearInterval(timer); timer = null } }

const startTimeout = () => {
  stopTimeout()
  timeoutTimer = setTimeout(() => {
    status.value = 'timeout'
    setTimeout(() => { close() }, 2000)
  }, CALL_TIMEOUT)
}

const stopTimeout = () => { if (timeoutTimer) { clearTimeout(timeoutTimer); timeoutTimer = null } }

const close = () => {
  stopTimer(); stopTimeout(); stopMediaStream(); closePeerConnection()
  visible.value = false
  emit('closed', { callId: callId.value })
}

const end = () => {
  status.value = 'hanging_up'
  stopTimer(); stopTimeout(); stopMediaStream(); closePeerConnection()
  setTimeout(() => { visible.value = false }, 1000)
}

onUnmounted(() => { stopTimer(); stopTimeout(); stopMediaStream() })

defineExpose({ open, close, end })
</script>

<style scoped lang="scss">
.call-dialog {
  :deep(.el-dialog__header) { display: none; }
  :deep(.el-dialog__body) { padding: 40px 0; transition: padding 0.3s; }
  &.video-mode { :deep(.el-dialog__body) { padding: 0; } }
}

.video-container {
  width: 100%; height: 450px; position: relative; background: #1f2329; overflow: hidden;
  .remote-video-wrapper {
    width: 100%; height: 100%; position: relative; display: flex; align-items: center; justify-content: center;
    .remote-video { width: 100%; height: 100%; object-fit: cover; }
    .video-placeholder {
      position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center;
      color: #fff; background: #1f2329;
      .material-icons-outlined { font-size: 48px; margin-bottom: 12px; opacity: 0.8; }
    }
  }
  .local-video-wrapper {
    position: absolute; top: 16px; right: 16px; width: 120px; height: 160px; background: #000;
    border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.3); z-index: 10;
    border: 2px solid rgba(255,255,255,0.1);
    .local-video { width: 100%; height: 100%; object-fit: cover; transform: scaleX(-1); }
  }
}

.call-content { display: flex; flex-direction: column; align-items: center; gap: 32px; }
.user-info { text-align: center;
  .call-avatar { margin-bottom: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
  .group-avatar { width: 80px; height: 80px; border-radius: 50%; background-color: #1677ff; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 36px; margin: 0 auto 16px; }
  .call-name { font-size: 20px; font-weight: 600; margin: 8px 0; color: #1f2329; }
  .call-status { font-size: 14px; color: #8f959e; }
}
.call-actions { width: 100%; padding: 0 40px; }
.incoming-actions, .calling-actions, .talking-actions, .timeout-actions { display: flex; justify-content: space-around; align-items: center; }
.talking-actions { flex-direction: column; gap: 16px; .duration { font-size: 18px; font-weight: 500; color: #1f2329; } }
.action-btn { display: flex; flex-direction: column; align-items: center; gap: 8px; background: none; border: none; cursor: pointer; transition: transform 0.2s;
  &:hover { transform: scale(1.1); }
  .material-icons-outlined { width: 56px; height: 56px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 28px; }
  span:not(.material-icons-outlined) { font-size: 13px; color: #646a73; }
  &.accept .material-icons-outlined { background-color: #52c41a; }
  &.reject .material-icons-outlined { background-color: #f54a45; }
}
</style>
