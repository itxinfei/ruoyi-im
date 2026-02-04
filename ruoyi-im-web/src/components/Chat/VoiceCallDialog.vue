<template>
  <teleport to="body">
    <transition name="call-fade">
      <div v-if="visible" class="voice-call-overlay" @click.self="handleMinimize">
        <div class="voice-call-dialog">
          <!-- 背景模糊头像 -->
          <div class="call-background" :class="{ pulse: isCalling }">
            <DingtalkAvatar
              :src="remoteUser?.avatar"
              :name="remoteUser?.name || remoteUser?.friendName"
              :size="280"
              shape="circle"
            />
          </div>

          <!-- 通话内容 -->
          <div class="call-content">
            <!-- 用户信息 -->
            <div class="user-info">
              <DingtalkAvatar
                :src="remoteUser?.avatar"
                :name="remoteUser?.name || remoteUser?.friendName"
                :size="80"
                shape="circle"
                class="user-avatar"
                :class="{ pulse: isCalling }"
              />
              <h2 class="user-name">{{ remoteUser?.name || remoteUser?.friendName }}</h2>
              <p class="call-status">{{ callStatusText }}</p>
              <p v-if="isConnected" class="call-duration">{{ formattedDuration }}</p>
            </div>

            <!-- 通话控制 -->
            <div class="call-controls">
              <button
                class="control-btn"
                :class="{ active: isMuted }"
                @click="toggleMute"
              >
                <span class="material-icons-outlined">{{ isMuted ? 'mic_off' : 'mic' }}</span>
                <span class="btn-label">{{ isMuted ? '取消静音' : '静音' }}</span>
              </button>

              <button
                class="control-btn"
                :class="{ active: isSpeakerOn }"
                @click="toggleSpeaker"
              >
                <span class="material-icons-outlined">{{ isSpeakerOn ? 'volume_up' : 'volume_off' }}</span>
                <span class="btn-label">{{ isSpeakerOn ? '扬声器' : '听筒' }}</span>
              </button>

              <button
                class="control-btn minimize-btn"
                @click="handleMinimize"
              >
                <span class="material-icons-outlined">expand_less</span>
                <span class="btn-label">最小化</span>
              </button>

              <button class="control-btn hangup-btn" @click="handleHangup">
                <span class="material-icons-outlined">call_end</span>
                <span class="btn-label">挂断</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 最小化的悬浮窗 -->
    <transition name="float-window">
      <div v-if="isMinimized" class="call-float-window" :class="{ incoming: isIncoming }">
        <div class="float-content">
          <div class="float-avatar">
            <DingtalkAvatar
              :src="remoteUser?.avatar"
              :name="remoteUser?.name || remoteUser?.friendName"
              :size="40"
              shape="circle"
            />
            <div v-if="isCalling || isIncoming" class="call-indicator"></div>
          </div>
          <div class="float-info">
            <span class="float-name">{{ remoteUser?.name || remoteUser?.friendName }}</span>
            <span class="float-status">{{ minimizedStatusText }}</span>
          </div>
          <button class="float-expand" @click="handleExpand">
            <span class="material-icons-outlined">expand_more</span>
          </button>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { addTokenToUrl } from '@/utils/file'
import { formatDurationMMSS } from '@/utils/format'

const props = defineProps({
  visible: { type: Boolean, default: false },
  isIncoming: { type: Boolean, default: false },
  remoteUser: { type: Object, default: null },
  callType: { type: String, default: 'voice' } // voice, video
})

const emit = defineEmits(['update:visible', 'close', 'accept', 'reject', 'minimize', 'expand'])

// 通话状态
const callStatus = ref('calling') // calling, connected, ended, rejected
const isMinimized = ref(false)
const isMuted = ref(false)
const isSpeakerOn = ref(false)
const callDuration = ref(0)
let durationTimer = null
let audioStream = null

// 计算属性
const isCalling = computed(() => callStatus.value === 'calling')
const isConnected = computed(() => callStatus.value === 'connected')

const callStatusText = computed(() => {
  if (isIncoming.value) return '来电中...'
  if (callStatus.value === 'calling') return '正在呼叫...'
  if (callStatus.value === 'connected') return '通话中'
  if (callStatus.value === 'ended') return '通话结束'
  if (callStatus.value === 'rejected') return '对方拒绝'
  return ''
})

const minimizedStatusText = computed(() => {
  if (isIncoming.value) return '来电中'
  if (callStatus.value === 'calling') return '呼叫中'
  if (callStatus.value === 'connected') return formattedDuration.value
  return '语音通话'
})

// 使用共享工具函数格式化通话时长
const formattedDuration = computed(() => formatDurationMMSS(callDuration.value))

// 获取头像URL
if (props.remoteUser) {
  props.remoteUser.avatar = addTokenToUrl(props.remoteUser.avatar)
}

// 方法
const startDurationTimer = () => {
  stopDurationTimer()
  durationTimer = setInterval(() => {
    callDuration.value++
  }, 1000)
}

const stopDurationTimer = () => {
  if (durationTimer) {
    clearInterval(durationTimer)
    durationTimer = null
  }
}

const toggleMute = async () => {
  isMuted.value = !isMuted.value

  // 实际控制音频轨道
  if (audioStream) {
    audioStream.getAudioTracks().forEach(track => {
      track.enabled = !isMuted.value
    })
  }
}

const toggleSpeaker = () => {
  isSpeakerOn.value = !isSpeakerOn.value
  ElMessage.info(isSpeakerOn.value ? '已切换到扬声器' : '已切换到听筒')
}

const handleHangup = () => {
  callStatus.value = 'ended'
  stopDurationTimer()
  stopAudioStream()

  setTimeout(() => {
    emit('update:visible', false)
    emit('close')
  }, 500)
}

const handleMinimize = () => {
  isMinimized.value = true
  emit('minimize')
}

const handleExpand = () => {
  isMinimized.value = false
  emit('expand')
}

// 获取麦克风权限并初始化音频
const initAudio = async () => {
  try {
    audioStream = await navigator.mediaDevices.getUserMedia({ audio: true })
    // 模拟接通
    setTimeout(() => {
      if (callStatus.value !== 'ended' && callStatus.value !== 'rejected') {
        callStatus.value = 'connected'
        startDurationTimer()
      }
    }, 2000)
  } catch (error) {
    console.error('获取麦克风失败:', error)
    ElMessage.error('无法访问麦克风，请检查权限设置')
    callStatus.value = 'ended'
  }
}

const stopAudioStream = () => {
  if (audioStream) {
    audioStream.getTracks().forEach(track => track.stop())
    audioStream = null
  }
}

// 监听可见性变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    callStatus.value = props.isIncoming ? 'calling' : 'calling'
    callDuration.value = 0
    isMinimized.value = false
    isMuted.value = false
    isSpeakerOn.value = false

    // 如果是拨出，初始化音频
    if (!props.isIncoming) {
      initAudio()
    }
  } else {
    stopDurationTimer()
    stopAudioStream()
  }
})

// 接听来电
const acceptCall = () => {
  emit('accept')
  initAudio()
}

// 拒绝来电
const rejectCall = () => {
  callStatus.value = 'rejected'
  emit('reject')
  setTimeout(() => {
    emit('update:visible', false)
    emit('close')
  }, 500)
}

onUnmounted(() => {
  stopDurationTimer()
  stopAudioStream()
})

// 暴露方法供父组件调用
defineExpose({
  acceptCall,
  rejectCall,
  handleHangup
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 语音通话遮罩层
// ============================================================================
.voice-call-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(10px);
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.call-fade-enter-active,
.call-fade-leave-active {
  transition: opacity 0.3s ease;
}

.call-fade-enter-from,
.call-fade-leave-to {
  opacity: 0;
}

// ============================================================================
// 通话对话框
// ============================================================================
.voice-call-dialog {
  position: relative;
  width: 380px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  border-radius: var(--dt-radius-3xl);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 40px 30px 30px;
}

// 背景模糊头像
.call-background {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 300px;
  height: 300px;
  opacity: 0.08;
  filter: blur(40px);

  &.pulse {
    animation: callBgPulse 2s ease-in-out infinite;
  }
}

// 通话内容
.call-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 60px;
}

// 用户信息
.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  text-align: center;
  margin-bottom: 20px;
}

.user-avatar {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  border: 3px solid rgba(255, 255, 255, 0.1);

  &.pulse {
    animation: callAvatarPulse 1.5s ease-in-out infinite;
  }
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin: 0;
}

.call-status {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

.call-duration {
  font-size: 18px;
  font-weight: 500;
  color: #409eff;
  margin: 0;
  font-variant-numeric: tabular-nums;
}

// 通话控制
.call-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.control-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 60px;
  padding: 12px 0;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  backdrop-filter: blur(10px);

  .material-icons-outlined {
    font-size: 24px;
    color: #fff;
  }

  .btn-label {
    font-size: 11px;
    color: rgba(255, 255, 255, 0.8);
  }

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(0);
  }

  &.active {
    background: rgba(64, 158, 255, 0.3);

    .material-icons-outlined {
      color: #409eff;
    }
  }

  &.minimize-btn:hover {
    background: rgba(103, 194, 58, 0.3);

    .material-icons-outlined {
      color: #67c23a;
    }
  }

  &.hangup-btn {
    background: rgba(245, 108, 108, 0.2);

    .material-icons-outlined {
      color: #f56c6c;
    }

    &:hover {
      background: rgba(245, 108, 108, 0.4);
      transform: scale(1.05);
    }
  }
}

// ============================================================================
// 最小化悬浮窗
// ============================================================================
.call-float-window {
  position: fixed;
  bottom: 100px;
  right: 30px;
  width: 280px;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(20px);
  border-radius: var(--dt-radius-xl);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  z-index: 9999;
  overflow: hidden;
  cursor: pointer;

  &.incoming {
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.9) 0%, rgba(0, 137, 255, 0.9) 100%);
    animation: floatPulse 1.5s ease-in-out infinite;
  }
}

@keyframes floatPulse {
  0%, 100% {
    box-shadow: 0 8px 32px rgba(64, 158, 255, 0.4);
  }
  50% {
    box-shadow: 0 8px 48px rgba(64, 158, 255, 0.6);
  }
}

.float-window-enter-active,
.float-window-leave-active {
  transition: all 0.3s ease;
}

.float-window-enter-from,
.float-window-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.float-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}

.float-avatar {
  position: relative;
}

.call-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  background: #67c23a;
  border: 2px solid #fff;
  border-radius: 50%;
  animation: indicatorPulse 1s ease-in-out infinite;
}

@keyframes indicatorPulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}

.float-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.float-name {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.float-status {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.float-expand {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: var(--dt-radius-md);
  cursor: pointer;

  .material-icons-outlined {
    font-size: 20px;
    color: #fff;
  }

  &:hover {
    background: rgba(255, 255, 255, 0.2);
  }
}

// ============================================================================
// 暗色模式适配
// ============================================================================
@media (prefers-color-scheme: dark) {
  .voice-call-overlay {
    background: linear-gradient(135deg, #0a0a0f 0%, #0d1117 50%, #161b22 100%);
  }
}
</style>
