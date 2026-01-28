<template>
  <teleport to="body">
    <transition name="call-fade">
      <div v-if="visible && !isMinimized" class="video-call-overlay" @click.self="handleMinimize">
        <div class="video-call-dialog">
          <!-- 远程视频区域 -->
          <div class="remote-video-container">
            <video
              ref="remoteVideoRef"
              class="remote-video"
              autoplay
              playsinline
              :muted="false"
            ></video>

            <!-- 无视频时的占位 -->
            <div v-if="!remoteStream && !isConnected" class="video-placeholder">
              <DingtalkAvatar
                :src="remoteUser?.avatar"
                :name="remoteUser?.name || remoteUser?.friendName"
                :size="80"
                shape="circle"
                class="placeholder-avatar"
                :class="{ pulse: isCalling }"
              />
              <p class="placeholder-status">{{ callStatusText }}</p>
            </div>

            <!-- 远程用户信息浮层 -->
            <div class="remote-info-overlay" v-if="remoteStream || isConnected">
              <span class="remote-name">{{ remoteUser?.name || remoteUser?.friendName }}</span>
              <span v-if="isConnected" class="call-timer">{{ formattedDuration }}</span>
            </div>

            <!-- 画中画本地视频 -->
            <div
              v-if="localStream"
              class="local-video-pip"
              :class="{ dragging: isDragging }"
              @mousedown="startDrag"
            >
              <video
                ref="localVideoRef"
                class="local-video"
                autoplay
                playsinline
                muted
              ></video>
              <div class="pip-controls">
                <button
                  class="pip-btn"
                  :class="{ active: !isCameraOn }"
                  @click="toggleCamera"
                >
                  <span class="material-icons-outlined">{{ isCameraOn ? 'videocam_off' : 'videocam' }}</span>
                </button>
              </div>
            </div>
          </div>

          <!-- 底部控制栏 -->
          <div class="video-controls">
            <!-- 左侧状态 -->
            <div class="controls-left">
              <div v-if="isNetworkQualityVisible" class="network-quality">
                <span class="material-icons-outlined" :class="networkQualityClass">signal_cellular_alt</span>
                <span class="quality-text">{{ networkQualityText }}</span>
              </div>
            </div>

            <!-- 中间主要控制 -->
            <div class="controls-center">
              <button
                class="control-btn"
                :class="{ active: isMuted }"
                @click="toggleMute"
              >
                <span class="material-icons-outlined">{{ isMuted ? 'mic_off' : 'mic' }}</span>
              </button>

              <button
                class="control-btn"
                :class="{ active: !isCameraOn }"
                @click="toggleCamera"
              >
                <span class="material-icons-outlined">{{ isCameraOn ? 'videocam' : 'videocam_off' }}</span>
              </button>

              <button
                class="control-btn"
                :class="{ active: isScreenSharing }"
                @click="toggleScreenShare"
                :disabled="!isConnected"
              >
                <span class="material-icons-outlined">screen_share</span>
              </button>

              <button class="control-btn hangup-btn" @click="handleHangup">
                <span class="material-icons-outlined">call_end</span>
              </button>

              <button
                class="control-btn"
                @click="toggleSpeaker"
                :class="{ active: isSpeakerOn }"
              >
                <span class="material-icons-outlined">{{ isSpeakerOn ? 'volume_up' : 'volume_off' }}</span>
              </button>
            </div>

            <!-- 右侧操作 -->
            <div class="controls-right">
              <button class="control-btn small" @click="handleMinimize">
                <span class="material-icons-outlined">expand_less</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 最小化悬浮窗 -->
    <transition name="float-window">
      <div v-if="visible && isMinimized" class="video-float-window">
        <div class="float-video-container" @click="handleExpand">
          <video
            v-if="localStream"
            ref="miniVideoRef"
            class="float-video"
            autoplay
            playsinline
            muted
          ></video>
          <div v-else class="float-placeholder">
            <DingtalkAvatar
              :src="remoteUser?.avatar"
              :name="remoteUser?.name || remoteUser?.friendName"
              :size="40"
              shape="circle"
            />
          </div>
          <div class="float-info">
            <span class="float-status">{{ minimizedStatusText }}</span>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { addTokenToUrl } from '@/utils/file'

const props = defineProps({
  visible: { type: Boolean, default: false },
  isIncoming: { type: Boolean, default: false },
  remoteUser: { type: Object, default: null }
})

const emit = defineEmits(['update:visible', 'close', 'accept', 'reject', 'minimize', 'expand'])

// Refs
const remoteVideoRef = ref(null)
const localVideoRef = ref(null)
const miniVideoRef = ref(null)

// 通话状态
const callStatus = ref('calling') // calling, connected, ended, rejected
const isMinimized = ref(false)
const isMuted = ref(false)
const isSpeakerOn = ref(false)
const isCameraOn = ref(true)
const isScreenSharing = ref(false)
const callDuration = ref(0)
const isNetworkQualityVisible = ref(true)

// 媒体流
const localStream = ref(null)
const remoteStream = ref(null)

// 拖拽相关
const isDragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })
const pipPosition = ref({ x: 20, y: 20 })

let durationTimer = null
let peerConnection = null

// 计算属性
const isCalling = computed(() => callStatus.value === 'calling')
const isConnected = computed(() => callStatus.value === 'connected')

const callStatusText = computed(() => {
  if (props.isIncoming) return '来电中...'
  if (callStatus.value === 'calling') return '正在呼叫...'
  if (callStatus.value === 'connected') return '通话中'
  if (callStatus.value === 'ended') return '通话结束'
  if (callStatus.value === 'rejected') return '对方拒绝'
  return ''
})

const minimizedStatusText = computed(() => {
  if (props.isIncoming) return '来电中'
  if (callStatus.value === 'calling') return '呼叫中'
  if (callStatus.value === 'connected') return formattedDuration.value
  return '视频通话'
})

const formattedDuration = computed(() => {
  const minutes = Math.floor(callDuration.value / 60)
  const seconds = callDuration.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

const networkQualityClass = computed(() => {
  // 模拟网络质量显示
  if (callDuration.value > 300) return 'excellent'
  if (callDuration.value > 60) return 'good'
  return 'normal'
})

const networkQualityText = computed(() => {
  const quality = networkQualityClass.value
  if (quality === 'excellent') return '极好'
  if (quality === 'good') return '良好'
  return '正常'
})

// 获取头像URL
if (props.remoteUser) {
  props.remoteUser.avatar = addTokenToUrl(props.remoteUser.avatar)
}

// 计时器
const startDurationTimer = () => {
  stopDurationTimer()
  durationTimer = setInterval(() => {
    callDuration.value++
    // 模拟网络质量变化
    if (Math.random() > 0.9) {
      isNetworkQualityVisible.value = true
      setTimeout(() => {
        isNetworkQualityVisible.value = false
      }, 3000)
    }
  }, 1000)
}

const stopDurationTimer = () => {
  if (durationTimer) {
    clearInterval(durationTimer)
    durationTimer = null
  }
}

// 媒体控制
const toggleMute = () => {
  isMuted.value = !isMuted.value
  if (localStream.value) {
    localStream.value.getAudioTracks().forEach(track => {
      track.enabled = !isMuted.value
    })
  }
}

const toggleCamera = () => {
  isCameraOn.value = !isCameraOn.value
  if (localStream.value) {
    localStream.value.getVideoTracks().forEach(track => {
      track.enabled = isCameraOn.value
    })
  }
}

const toggleSpeaker = () => {
  isSpeakerOn.value = !isSpeakerOn.value
  ElMessage.info(isSpeakerOn.value ? '已切换到扬声器' : '已切换到听筒')
}

const toggleScreenShare = async () => {
  if (isScreenSharing.value) {
    // 停止屏幕共享
    stopScreenShare()
  } else {
    // 开始屏幕共享
    try {
      const screenStream = await navigator.mediaDevices.getDisplayMedia({
        video: { cursor: 'always' },
        audio: false
      })

      // 替换视频轨道
      if (peerConnection && localStream.value) {
        const videoTrack = screenStream.getVideoTracks()[0]
        const sender = peerConnection.getSenders().find(s =>
          s.track && s.track.kind === 'video'
        )
        if (sender) {
          sender.replaceTrack(videoTrack)
        }
      }

      // 监听屏幕共享停止
      screenStream.getVideoTracks()[0].onended = () => {
        stopScreenShare()
      }

      isScreenSharing.value = true
      ElMessage.success('已开始屏幕共享')
    } catch (error) {
      console.error('屏幕共享失败:', error)
      ElMessage.warning('取消屏幕共享')
    }
  }
}

const stopScreenShare = () => {
  if (localStream.value && isCameraOn.value) {
    // 恢复摄像头
    const videoTrack = localStream.value.getVideoTracks()[0]
    if (videoTrack) {
      videoTrack.enabled = true
    }
  }
  isScreenSharing.value = false
}

// 初始化本地媒体
const initLocalMedia = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: {
        width: { ideal: 1280 },
        height: { ideal: 720 },
        facingMode: 'user'
      },
      audio: true
    })

    localStream.value = stream

    // 设置本地视频预览
    await nextTick()
    if (localVideoRef.value) {
      localVideoRef.value.srcObject = stream
    }

    return stream
  } catch (error) {
    console.error('获取媒体设备失败:', error)
    ElMessage.error('无法访问摄像头和麦克风，请检查权限设置')
    return null
  }
}

// 停止媒体流
const stopMediaStreams = () => {
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => track.stop())
    localStream.value = null
  }
  if (remoteStream.value) {
    remoteStream.value.getTracks().forEach(track => track.stop())
    remoteStream.value = null
  }
}

// 画中画拖拽
const startDrag = (e) => {
  isDragging.value = true
  dragOffset.value = {
    x: e.clientX - pipPosition.value.x,
    y: e.clientY - pipPosition.value.y
  }

  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

const onDrag = (e) => {
  if (!isDragging.value) return

  const container = document.querySelector('.remote-video-container')
  if (!container) return

  const rect = container.getBoundingClientRect()
  const pip = document.querySelector('.local-video-pip')
  if (!pip) return

  let newX = e.clientX - dragOffset.value.x
  let newY = e.clientY - dragOffset.value.y

  // 限制在容器内
  newX = Math.max(0, Math.min(newX, rect.width - 200))
  newY = Math.max(0, Math.min(newY, rect.height - 150))

  pipPosition.value = { x: newX, y: newY }
}

const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

// 通话控制
const handleHangup = () => {
  callStatus.value = 'ended'
  stopDurationTimer()
  stopMediaStreams()

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
  nextTick(() => {
    if (miniVideoRef.value && localStream.value) {
      miniVideoRef.value.srcObject = localStream.value
    }
  })
}

// 接听/拒绝
const acceptCall = async () => {
  emit('accept')
  const stream = await initLocalMedia()
  if (stream) {
    // 模拟连接
    setTimeout(() => {
      if (callStatus.value !== 'ended' && callStatus.value !== 'rejected') {
        callStatus.value = 'connected'
        startDurationTimer()
      }
    }, 1500)
  }
}

const rejectCall = () => {
  callStatus.value = 'rejected'
  emit('reject')
  setTimeout(() => {
    emit('update:visible', false)
    emit('close')
  }, 500)
}

// 监听可见性
watch(() => props.visible, (newVal) => {
  if (newVal) {
    callStatus.value = 'calling'
    callDuration.value = 0
    isMinimized.value = false
    isMuted.value = false
    isSpeakerOn.value = false
    isCameraOn.value = true
    isScreenSharing.value = false

    if (!props.isIncoming) {
      initLocalMedia().then(stream => {
        if (stream) {
          setTimeout(() => {
            if (callStatus.value !== 'ended') {
              callStatus.value = 'connected'
              startDurationTimer()
            }
          }, 2000)
        }
      })
    }
  } else {
    stopDurationTimer()
    stopMediaStreams()
  }
})

// 监听最小化状态，更新 mini 视频
watch(isMinimized, (newVal) => {
  if (!newVal && localStream.value) {
    nextTick(() => {
      if (localVideoRef.value) {
        localVideoRef.value.srcObject = localStream.value
      }
    })
  }
})

onUnmounted(() => {
  stopDurationTimer()
  stopMediaStreams()
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
})

// 暴露方法
defineExpose({
  acceptCall,
  rejectCall,
  handleHangup
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 视频通话遮罩层
// ============================================================================
.video-call-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
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
// 视频通话对话框
// ============================================================================
.video-call-dialog {
  position: relative;
  width: 700px;
  height: 480px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.6);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

// 远程视频容器
.remote-video-container {
  position: relative;
  flex: 1;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  overflow: hidden;
}

.remote-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

// 视频占位
.video-placeholder {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  text-align: center;
}

.placeholder-avatar {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);

  &.pulse {
    animation: avatarPulse 1.5s ease-in-out infinite;
  }
}

@keyframes avatarPulse {
  0%, 100% {
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  }
  50% {
    box-shadow: 0 8px 48px rgba(22, 119, 255, 0.6);
  }
}

.placeholder-status {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

// 远程用户信息浮层
.remote-info-overlay {
  position: absolute;
  top: 20px;
  left: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  border-radius: 24px;
}

.remote-name {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.call-timer {
  font-size: 14px;
  color: #409eff;
  font-variant-numeric: tabular-nums;
}

// 画中画本地视频
.local-video-pip {
  position: absolute;
  bottom: 80px;
  right: 16px;
  width: 140px;
  height: 105px;
  background: #000;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
  cursor: move;
  transition: box-shadow 0.2s ease;

  &.dragging {
    box-shadow: 0 8px 30px rgba(64, 158, 255, 0.5);
  }
}

.local-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scaleX(-1); // 镜像
}

.pip-controls {
  position: absolute;
  bottom: 8px;
  right: 8px;
  display: flex;
  gap: 4px;
}

.pip-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
  border: none;
  border-radius: 6px;
  cursor: pointer;

  .material-icons-outlined {
    font-size: 16px;
    color: #fff;
  }

  &.active {
    background: rgba(245, 108, 108, 0.8);
  }
}

// ============================================================================
// 视频控制栏
// ============================================================================
.video-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 30px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8) 0%, transparent 100%);
}

.controls-left,
.controls-right {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 150px;
}

.controls-center {
  display: flex;
  align-items: center;
  gap: 12px;
}

.control-btn {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;
  backdrop-filter: blur(10px);

  .material-icons-outlined {
    font-size: 24px;
    color: #fff;
  }

  &:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: scale(1.05);
  }

  &.active {
    background: rgba(245, 108, 108, 0.8);

    .material-icons-outlined {
      color: #fff;
    }
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  &.hangup-btn {
    background: rgba(245, 108, 108, 0.9);

    &:hover {
      background: rgba(245, 108, 108, 1);
      transform: scale(1.1);
    }
  }

  &.small {
    width: 40px;
    height: 40px;

    .material-icons-outlined {
      font-size: 20px;
    }
  }
}

// 网络质量
.network-quality {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 16px;

  .material-icons-outlined {
    font-size: 16px;
    color: #67c23a;

    &.excellent {
      color: #67c23a;
    }

    &.good {
      color: #e6a23c;
    }

    &.normal {
      color: #f56c6c;
    }
  }

  .quality-text {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.8);
  }
}

// ============================================================================
// 最小化悬浮窗
// ============================================================================
.video-float-window {
  position: fixed;
  bottom: 100px;
  right: 30px;
  width: 200px;
  height: 150px;
  background: #000;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  z-index: 9999;
  overflow: hidden;
  cursor: pointer;
}

.float-video-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.float-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scaleX(-1);
}

.float-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.float-info {
  position: absolute;
  bottom: 8px;
  left: 8px;
  right: 8px;
  padding: 6px 12px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 8px;
  text-align: center;
}

.float-status {
  font-size: 12px;
  color: #fff;
}

.float-window-enter-active,
.float-window-leave-active {
  transition: all 0.3s ease;
}

.float-window-enter-from,
.float-window-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}
</style>
