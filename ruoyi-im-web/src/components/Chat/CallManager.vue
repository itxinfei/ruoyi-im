<!--
  通话管理器组件
  统一管理语音通话和视频通话，自动处理来电和拨号
-->
<template>
  <div class="call-manager">
    <!-- 语音通话浮动窗口 -->
    <VoiceCallFloat
      :visible="voiceVisible"
      :call-id="voiceCallId"
      :remote-user="voiceRemoteUser"
      :call-state="voiceCallState"
      :call-duration="voiceCallDuration"
      @accept="handleVoiceAccept"
      @reject="handleVoiceReject"
      @hangup="handleVoiceHangup"
      @toggle-mute="handleVoiceMute"
      @toggle-speaker="handleVoiceSpeaker"
    />

    <!-- 视频通话全屏窗口（复用现有组件） -->
    <VideoCall
      v-if="videoVisible"
      :visible="videoVisible"
      :call-id="videoCallId"
      :call-type="videoCallType"
      :remote-user="videoRemoteUser"
      :call-state="videoCallState"
      @accept="handleVideoAccept"
      @reject="handleVideoReject"
      @hangup="handleVideoHangup"
      @state-change="handleVideoStateChange"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import VoiceCallFloat from './VoiceCallFloat.vue'
import VideoCall from './VideoCall.vue'
import { useVoiceCall, CALL_STATE, CALL_TYPE } from '@/composables/useVoiceCall.js'
import imWebSocket from '@/utils/websocket/imWebSocket'

// Props
const props = defineProps({
  currentUserId: {
    type: [Number, String],
    default: null,
  },
  currentConversationId: {
    type: [Number, String],
    default: null,
  },
})

// Emits
const emit = defineEmits(['call-started', 'call-ended', 'incoming-call'])

// 语音通话相关
const voiceCall = useVoiceCall()
const voiceVisible = computed(() => voiceCall.callState.value !== CALL_STATE.IDLE)
const voiceCallId = computed(() => voiceCall.callId.value)
const voiceRemoteUser = computed(() => voiceCall.remoteUser.value)
const voiceCallState = computed(() => voiceCall.callState.value)
const voiceCallDuration = computed(() => voiceCall.callDuration.value)

// 视频通话相关状态
const videoVisible = ref(false)
const videoCallId = ref(null)
const videoCallType = ref('video')
const videoRemoteUser = ref({})
const videoCallState = ref('idle')

/**
 * 发起语音通话
 */
const startVoiceCall = async (userId, conversationId = null) => {
  if (voiceCall.isInCall.value) {
    ElMessage.warning('正在通话中，请先结束当前通话')
    return false
  }

  if (videoVisible.value) {
    ElMessage.warning('正在进行视频通话')
    return false
  }

  const success = await voiceCall.initiateCall(userId, conversationId)
  if (success) {
    emit('call-started', { type: 'voice', userId, conversationId })
  }
  return success
}

/**
 * 发起视频通话（供外部调用）
 */
const startVideoCall = async (userId, conversationId = null) => {
  if (voiceCall.isInCall.value) {
    ElMessage.warning('正在进行语音通话')
    return false
  }

  if (videoVisible.value) {
    ElMessage.warning('正在视频通话中')
    return false
  }

  // 这里应该调用视频通话的逻辑
  // 暂时使用现有的视频通话组件
  videoCallType.value = 'video'
  videoRemoteUser.value = { id: userId }
  videoVisible.value = true
  emit('call-started', { type: 'video', userId, conversationId })
  return true
}

/**
 * 语音通话 - 接听
 */
const handleVoiceAccept = async () => {
  const success = await voiceCall.acceptCall()
  if (!success) {
    ElMessage.error('接听失败')
  }
}

/**
 * 语音通话 - 拒绝
 */
const handleVoiceReject = async () => {
  await voiceCall.rejectCall()
  emit('call-ended', { type: 'voice', reason: 'rejected' })
}

/**
 * 语音通话 - 挂断
 */
const handleVoiceHangup = async () => {
  const result = await voiceCall.endCall()
  emit('call-ended', { type: 'voice', result })
}

/**
 * 语音通话 - 静音切换
 */
const handleVoiceMute = (muted) => {
  voiceCall.setMuted(muted)
}

/**
 * 语音通话 - 扬声器切换
 */
const handleVoiceSpeaker = (on) => {
  voiceCall.setSpeaker(on)
}

/**
 * 视频通话 - 接听
 */
const handleVideoAccept = () => {
  // 视频通话接听逻辑
  videoCallState.value = 'connected'
}

/**
 * 视频通话 - 拒绝
 */
const handleVideoReject = () => {
  videoVisible.value = false
  videoCallState.value = 'rejected'
  emit('call-ended', { type: 'video', reason: 'rejected' })
}

/**
 * 视频通话 - 挂断
 */
const handleVideoHangup = () => {
  videoVisible.value = false
  videoCallState.value = 'ended'
  emit('call-ended', { type: 'video' })
}

/**
 * 视频通话 - 状态变化
 */
const handleVideoStateChange = (state) => {
  videoCallState.value = state
  if (state === 'ended') {
    emit('call-ended', { type: 'video' })
  }
}

/**
 * 处理来电通知（WebSocket）
 */
const handleIncomingCall = (message) => {
  const { callType, callerId, callerName, callerAvatar, callId, conversationId } = message

  if (callType === 'VOICE' || callType === 'voice') {
    // 语音来电
    voiceCall.handleIncomingCall(message)
    emit('incoming-call', { type: 'voice', callerId, callerName, callId })
  } else if (callType === 'VIDEO' || callType === 'video') {
    // 视频来电
    videoCallId.value = callId
    videoCallType.value = 'video'
    videoRemoteUser.value = {
      id: callerId,
      name: callerName,
      avatar: callerAvatar,
    }
    videoCallState.value = 'incoming'
    videoVisible.value = true
    emit('incoming-call', { type: 'video', callerId, callerName, callId })
  }
}

/**
 * 处理通话状态变化（WebSocket）
 */
const handleCallStatus = (message) => {
  const { status, callType } = message

  if (callType === 'VOICE' || callType === 'voice') {
    voiceCall.handleCallStatus(message)
  } else {
    // 视频通话状态
    videoCallState.value = status.toLowerCase()
    if (status === 'ENDED' || status === 'REJECTED' || status === 'TIMEOUT') {
      videoVisible.value = false
      emit('call-ended', { type: 'video', status })
    }
  }
}

/**
 * 处理 WebRTC 信令（WebSocket）
 */
const handleWebRTCSignal = (message) => {
  const { callId } = message

  // 根据通话ID判断是语音还是视频
  if (callId === voiceCall.callId.value) {
    voiceCall.handleWebRTCSignal(message)
  }
  // 视频通话的信令由 VideoCall 组件处理
}

// 设置 WebSocket 监听
const setupListeners = () => {
  imWebSocket.on('incomingCall', handleIncomingCall)
  imWebSocket.on('callStatus', handleCallStatus)
  imWebSocket.on('webrtcSignal', handleWebRTCSignal)

  // 同时设置 voiceCall 的监听
  voiceCall.setupWebSocketListeners()
}

// 移除监听
const removeListeners = () => {
  imWebSocket.off('incomingCall', handleIncomingCall)
  imWebSocket.off('callStatus', handleCallStatus)
  imWebSocket.off('webrtcSignal', handleWebRTCSignal)

  voiceCall.removeWebSocketListeners()
}

// 生命周期
onMounted(() => {
  setupListeners()
})

onUnmounted(() => {
  removeListeners()
})

// 暴露方法给父组件
defineExpose({
  startVoiceCall,
  startVideoCall,
  endCall: () => {
    voiceCall.endCall()
    handleVideoHangup()
  },
  isInCall: computed(() => voiceCall.isInCall.value || videoVisible.value),
  callState: computed(() => ({
    voice: voiceCall.callState.value,
    video: videoCallState.value,
  })),
})
</script>

<style lang="scss" scoped>
.call-manager {
  // 容器不占用空间，仅用于管理子组件
  position: relative;
  width: 0;
  height: 0;
  overflow: visible;
}
</style>
