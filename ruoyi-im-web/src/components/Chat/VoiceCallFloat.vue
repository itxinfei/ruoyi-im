<!--
  语音通话浮动窗口组件
  功能：一对一语音通话，可拖动、可最小化
  支持：静音、扬声器切换、通话计时、挂断
-->
<template>
  <!-- 浮动通话窗口 -->
  <Transition name="float-fade">
    <div
      v-if="visible && !minimized"
      class="voice-call-float"
      :style="{ left: position.x + 'px', top: position.y + 'px' }"
    >
      <!-- 顶部拖动栏 -->
      <div class="float-header" @mousedown="startDrag">
        <div class="header-title">
          <el-icon><Phone /></el-icon>
          <span>语音通话</span>
        </div>
        <div class="header-actions">
          <button class="action-btn minimize-btn" @click.stop="minimize" title="最小化">
            <el-icon><Minus /></el-icon>
          </button>
        </div>
      </div>

      <!-- 主内容区 -->
      <div class="float-body">
        <!-- 头像 + 波纹动画 -->
        <div class="avatar-wrapper" :class="{ pulsing: callState === 'connected' }">
          <img
            :src="remoteUser?.avatar || defaultAvatar"
            :alt="remoteUser?.name || '对方'"
            class="user-avatar"
          />
          <div class="wave-animation"></div>
          <div class="wave-animation wave-delay-1"></div>
          <div class="wave-animation wave-delay-2"></div>
        </div>

        <!-- 用户信息 + 状态 -->
        <div class="call-info">
          <div class="user-name">{{ remoteUser?.name || remoteUser?.nickname || '对方' }}</div>
          <div class="call-status-text">{{ statusText }}</div>
          <div v-if="callState === 'connected'" class="call-duration">
            {{ formatDuration(callDuration) }}
          </div>
        </div>

        <!-- 控制按钮 -->
        <div class="control-buttons">
          <!-- 来电时显示接听/拒绝 -->
          <template v-if="callState === 'incoming'">
            <button class="btn-circle btn-reject" @click="handleReject" title="拒绝">
              <el-icon :size="20"><Close /></el-icon>
            </button>
            <button class="btn-circle btn-accept" @click="handleAccept" title="接听">
              <el-icon :size="20"><Check /></el-icon>
            </button>
          </template>

          <!-- 呼叫中/通话中显示控制按钮 -->
          <template v-else>
            <button
              class="btn-control"
              :class="{ 'btn-active': isMuted }"
              @click="handleToggleMute"
              title="静音/取消静音"
            >
              <el-icon :size="20">
                <component :is="isMuted ? 'MuteNotification' : 'Microphone'" />
              </el-icon>
              <span class="btn-label">{{ isMuted ? '取消静音' : '静音' }}</span>
            </button>
            <button
              class="btn-control"
              :class="{ 'btn-active': isSpeakerOn }"
              @click="handleToggleSpeaker"
              title="扬声器/听筒"
            >
              <el-icon :size="20">
                <component :is="isSpeakerOn ? 'Bell' : 'Mute'" />
              </el-icon>
              <span class="btn-label">{{ isSpeakerOn ? '扬声器' : '听筒' }}</span>
            </button>
            <button class="btn-control btn-hangup" @click="handleHangup" title="挂断">
              <el-icon :size="20"><PhoneFilled /></el-icon>
              <span class="btn-label">挂断</span>
            </button>
          </template>
        </div>
      </div>

      <!-- 音频元素（用于铃声和提示音） -->
      <audio ref="ringtoneAudio" :src="ringtoneSrc" loop preload="auto"></audio>
      <audio ref="connectAudio" :src="connectSrc" preload="auto"></audio>
    </div>
  </Transition>

  <!-- 最小化小图标 -->
  <Transition name="minimize-fade">
    <div
      v-if="minimized"
      class="voice-call-minimized"
      :style="{ right: '20px', bottom: '80px' }"
      @click="restore"
    >
      <div class="minimized-icon" :class="{ 'pulse-ring': callState === 'incoming' }">
        <el-icon :size="20"><Phone /></el-icon>
      </div>
      <div v-if="callState === 'connected'" class="minimized-duration">
        {{ formatDuration(callDuration) }}
      </div>
      <div v-else-if="callState === 'incoming'" class="minimized-badge"></div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import {
  Phone,
  PhoneFilled,
  Minus,
  Close,
  Check,
  Microphone,
  MuteNotification,
  Bell,
  Mute,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  callId: {
    type: [Number, String],
    default: null,
  },
  remoteUser: {
    type: Object,
    default: () => ({}),
  },
  callState: {
    type: String,
    default: 'idle', // idle, calling, incoming, connecting, connected, ended, rejected, timeout
  },
  callDuration: {
    type: Number,
    default: 0,
  },
})

// Emits
const emit = defineEmits([
  'accept',
  'reject',
  'hangup',
  'toggle-mute',
  'toggle-speaker',
  'update:minimized',
])

// Refs
const ringtoneAudio = ref(null)
const connectAudio = ref(null)

// State
const minimized = ref(false)
const isMuted = ref(false)
const isSpeakerOn = ref(true)
const position = ref({ x: window.innerWidth - 300, y: 100 })
const dragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })

// 音频文件路径（空字符串表示文件不存在，不会报错）
const ringtoneSrc = ref('')
const connectSrc = ref('')

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 计算属性：状态文本
const statusText = computed(() => {
  const statusMap = {
    calling: '呼叫中...',
    incoming: '来电',
    connecting: '连接中...',
    connected: '通话中',
    ended: '通话结束',
    rejected: '已拒绝',
    timeout: '未接听',
  }
  return statusMap[props.callState] || ''
})

// 格式化通话时长
const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 拖动功能
const startDrag = (e) => {
  dragging.value = true
  dragOffset.value = {
    x: e.clientX - position.value.x,
    y: e.clientY - position.value.y,
  }

  const onMove = (moveEvent) => {
    if (!dragging.value) return

    let newX = moveEvent.clientX - dragOffset.value.x
    let newY = moveEvent.clientY - dragOffset.value.y

    // 边界限制
    const maxX = window.innerWidth - 280
    const maxY = window.innerHeight - 400

    newX = Math.max(0, Math.min(newX, maxX))
    newY = Math.max(0, Math.min(newY, maxY))

    position.value = { x: newX, y: newY }
  }

  const onUp = () => {
    dragging.value = false
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }

  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

// 最小化
const minimize = () => {
  minimized.value = true
  emit('update:minimized', true)
  stopRingtone()
}

// 恢复窗口
const restore = () => {
  minimized.value = false
  emit('update:minimized', false)
}

// 播放铃声
const playRingtone = () => {
  if (!ringtoneSrc.value) {
    console.log('[语音通话] 未配置铃声文件，跳过播放')
    return
  }
  if (ringtoneAudio.value) {
    ringtoneAudio.value.currentTime = 0
    ringtoneAudio.value.play().catch(err => {
      console.warn('播放铃声失败:', err)
    })
  }
}

// 停止铃声
const stopRingtone = () => {
  if (ringtoneAudio.value) {
    ringtoneAudio.value.pause()
    ringtoneAudio.value.currentTime = 0
  }
}

// 播放连接音效
const playConnectSound = () => {
  if (!connectSrc.value) {
    console.log('[语音通话] 未配置连接音效文件，跳过播放')
    return
  }
  if (connectAudio.value) {
    connectAudio.value.currentTime = 0
    connectAudio.value.play().catch(err => {
      console.warn('播放连接音效失败:', err)
    })
  }
}

// 事件处理
const handleAccept = () => {
  stopRingtone()
  playConnectSound()
  emit('accept')
}

const handleReject = () => {
  stopRingtone()
  emit('reject')
}

const handleHangup = () => {
  stopRingtone()
  emit('hangup')
}

const handleToggleMute = () => {
  isMuted.value = !isMuted.value
  emit('toggle-mute', isMuted.value)
}

const handleToggleSpeaker = () => {
  isSpeakerOn.value = !isSpeakerOn.value
  emit('toggle-speaker', isSpeakerOn.value)
}

// 监听来电状态，播放铃声
watch(() => props.callState, (newState) => {
  if (newState === 'incoming' && props.visible) {
    playRingtone()
  } else if (newState === 'connected') {
    stopRingtone()
    playConnectSound()
  } else if (newState === 'ended' || newState === 'rejected' || newState === 'timeout') {
    stopRingtone()
  }
})

// 监听可见性
watch(() => props.visible, (visible) => {
  if (visible) {
    // 重置位置到默认
    position.value = { x: window.innerWidth - 300, y: 100 }
    minimized.value = false
    isMuted.value = false
    isSpeakerOn.value = true
  }
})

// 标题闪烁提醒（来电时）
let titleInterval = null
const startTitleFlash = () => {
  const originalTitle = document.title
  let count = 0
  titleInterval = setInterval(() => {
    document.title = count % 2 === 0 ? '📞 来电中...' : originalTitle
    count++
  }, 1000)
}

const stopTitleFlash = () => {
  if (titleInterval) {
    clearInterval(titleInterval)
    titleInterval = null
    document.title = '若言IM'
  }
}

watch(() => props.callState, (newState) => {
  if (newState === 'incoming') {
    startTitleFlash()
  } else {
    stopTitleFlash()
  }
})

// 页面可见性变化
const handleVisibilityChange = () => {
  if (document.hidden && props.callState === 'incoming') {
    startTitleFlash()
  }
}

// 生命周期
onMounted(() => {
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  stopRingtone()
  stopTitleFlash()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

// 暴露方法给父组件
defineExpose({
  minimize,
  restore,
  playRingtone,
  stopRingtone,
})
</script>

<style lang="scss" scoped>
.voice-call-float {
  position: fixed;
  width: 280px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  z-index: 9999;
  overflow: hidden;
  transition: box-shadow 0.3s ease;

  &:hover {
    box-shadow: 0 12px 48px rgba(0, 0, 0, 0.18);
  }
}

// 顶部拖动栏
.float-header {
  height: 44px;
  background: linear-gradient(135deg, #1890ff, #096dd9);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  cursor: move;
  user-select: none;

  .header-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 500;
  }

  .header-actions {
    display: flex;
    gap: 8px;

    .action-btn {
      width: 28px;
      height: 28px;
      border: none;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 6px;
      color: #fff;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;

      &:hover {
        background: rgba(255, 255, 255, 0.3);
      }
    }
  }
}

// 主内容区
.float-body {
  padding: 24px 20px 20px;
  text-align: center;
}

// 头像区域
.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 20px;

  .user-avatar {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    object-fit: cover;
    border: 3px solid #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    position: relative;
    z-index: 2;
  }

  .wave-animation {
    position: absolute;
    inset: -8px;
    border-radius: 50%;
    border: 2px solid #1890ff;
    opacity: 0;
    z-index: 1;
  }

  .wave-delay-1 {
    animation-delay: 0.5s;
  }

  .wave-delay-2 {
    animation-delay: 1s;
  }

  &.pulsing .wave-animation {
    animation: pulse-wave 2s ease-out infinite;
  }
}

@keyframes pulse-wave {
  0% {
    transform: scale(1);
    opacity: 0.6;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

// 通话信息
.call-info {
  margin-bottom: 24px;

  .user-name {
    font-size: 18px;
    font-weight: 500;
    color: #303133;
    margin-bottom: 4px;
  }

  .call-status-text {
    font-size: 14px;
    color: #909399;
    margin-bottom: 4px;
  }

  .call-duration {
    font-size: 20px;
    font-weight: 500;
    color: #1890ff;
    font-variant-numeric: tabular-nums;
  }
}

// 控制按钮
.control-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;

  .btn-circle {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    transition: all 0.2s ease;

    &:hover {
      transform: scale(1.08);
    }

    &:active {
      transform: scale(0.95);
    }

    &.btn-accept {
      background: linear-gradient(135deg, #52c41a, #389e0d);
      box-shadow: 0 4px 12px rgba(82, 196, 26, 0.4);
    }

    &.btn-reject {
      background: linear-gradient(135deg, #ff4d4f, #cf1322);
      box-shadow: 0 4px 12px rgba(255, 77, 79, 0.4);
    }
  }

  .btn-control {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 12px 16px;
    border: none;
    border-radius: 12px;
    background: #f5f5f5;
    color: #606266;
    cursor: pointer;
    transition: all 0.2s ease;
    min-width: 64px;

    &:hover {
      background: #e6f7ff;
      color: #1890ff;
    }

    &:active {
      transform: scale(0.95);
    }

    &.btn-active {
      background: #1890ff;
      color: #fff;
    }

    &.btn-hangup {
      background: #ff4d4f;
      color: #fff;

      &:hover {
        background: #ff7875;
      }
    }

    .btn-label {
      font-size: 12px;
      font-weight: 500;
    }
  }
}

// 最小化图标
.voice-call-minimized {
  position: fixed;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  z-index: 9998;

  .minimized-icon {
    width: 48px;
    height: 48px;
    background: linear-gradient(135deg, #1890ff, #096dd9);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
    transition: all 0.2s ease;
    position: relative;

    &:hover {
      transform: scale(1.1);
    }

    &.pulse-ring::after {
      content: '';
      position: absolute;
      inset: -4px;
      border-radius: 50%;
      border: 2px solid #1890ff;
      animation: pulse-ring 1.5s ease-out infinite;
    }
  }

  @keyframes pulse-ring {
    0% {
      transform: scale(1);
      opacity: 0.8;
    }
    100% {
      transform: scale(1.5);
      opacity: 0;
    }
  }

  .minimized-duration {
    padding: 4px 10px;
    background: rgba(0, 0, 0, 0.7);
    border-radius: 10px;
    font-size: 12px;
    color: #fff;
    font-variant-numeric: tabular-nums;
  }

  .minimized-badge {
    position: absolute;
    top: -4px;
    right: -4px;
    width: 16px;
    height: 16px;
    background: #ff4d4f;
    border-radius: 50%;
    border: 2px solid #fff;
    animation: pulse-ring 1.5s ease-out infinite;
  }
}

// 过渡动画
.float-fade-enter-active,
.float-fade-leave-active {
  transition: all 0.3s ease;
}

.float-fade-enter-from,
.float-fade-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

.minimize-fade-enter-active,
.minimize-fade-leave-active {
  transition: all 0.3s ease;
}

.minimize-fade-enter-from,
.minimize-fade-leave-to {
  opacity: 0;
  transform: scale(0) translateY(20px);
}

// 响应式
@media (max-width: 768px) {
  .voice-call-float {
    width: 260px;
  }

  .control-buttons {
    gap: 12px;

    .btn-circle {
      width: 48px;
      height: 48px;
    }

    .btn-control {
      padding: 10px 12px;
      min-width: 56px;

      .btn-label {
        font-size: 11px;
      }
    }
  }
}
</style>
