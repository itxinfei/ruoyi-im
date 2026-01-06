<template>
  <div class="video-call-container">
    <!-- 视频通话主界面 -->
    <div v-if="callState.status !== 'idle'" class="video-call-modal" :class="{ minimized: isMinimized }">
      <!-- 最小化时的悬浮球 -->
      <div v-if="isMinimized" class="minimized-ball" @click="toggleMinimize">
        <div class="ball-content">
          <el-icon><VideoCamera /></el-icon>
          <span class="call-timer">{{ formatTime(callDuration) }}</span>
        </div>
        <div class="pulse-ring"></div>
      </div>

      <!-- 完整视频界面 -->
      <div v-else class="full-screen">
        <!-- 远程视频区域 -->
        <div class="remote-video-area">
          <video
            ref="remoteVideoRef"
            class="remote-video"
            :srcObject="remoteStream"
            autoplay
            playsinline
          ></video>
          <video
            v-if="!remoteStream && callState.status === 'calling'"
            ref="remoteVideoRef"
            class="remote-video-placeholder"
            loop
            muted
          >
            <source :src="callState.type === 'video' ? videoPlaceholder : ''" type="video/mp4">
          </video>

          <!-- 等待提示 -->
          <div v-if="callState.status === 'calling'" class="waiting-hint">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>等待对方接听...</span>
          </div>

          <!-- 对方信息 -->
          <div class="remote-info">
            <div class="user-avatar">
              <el-avatar :size="60" :src="callState.remoteUser?.avatar">
                {{ callState.remoteUser?.name?.charAt(0) }}
              </el-avatar>
            </div>
            <div class="user-name">{{ callState.remoteUser?.name }}</div>
            <div class="call-status">{{ callStatusText }}</div>
          </div>
        </div>

        <!-- 本地视频区域 -->
        <div class="local-video-area" :class="{ expanded: isLocalExpanded }">
          <video
            ref="localVideoRef"
            class="local-video"
            :srcObject="localStream"
            autoplay
            muted
            playsinline
            @click="toggleLocalExpand"
          ></video>
          <div v-if="!localStream" class="local-placeholder">
            <el-icon><VideoCameraFilled /></el-icon>
          </div>
        </div>

        <!-- 控制栏 -->
        <div class="control-bar">
          <div class="control-buttons">
            <!-- 静音 -->
            <el-button
              :type="isMuted ? 'danger' : 'default'"
              circle
              size="large"
              @click="toggleMute"
            >
              <el-icon><component :is="isMuted ? Mute : Microphone" /></el-icon>
            </el-button>

            <!-- 视频/摄像头开关 -->
            <el-button
              v-if="callState.type === 'video'"
              :type="isVideoOff ? 'danger' : 'default'"
              circle
              size="large"
              @click="toggleVideo"
            >
              <el-icon><component :is="isVideoOff ? VideoCameraFilled : VideoCamera" /></el-icon>
            </el-button>

            <!-- 切换摄像头 -->
            <el-button
              v-if="callState.type === 'video' && isMobile"
              circle
              size="large"
              @click="switchCamera"
            >
              <el-icon><Refresh /></el-icon>
            </el-button>

            <!-- 挂断 -->
            <el-button type="danger" circle size="large" @click="endCall">
              <el-icon><PhoneFilled /></el-icon>
            </el-button>

            <!-- 接听按钮（来电时显示） -->
            <el-button
              v-if="callState.status === 'incoming'"
              type="success"
              circle
              size="large"
              @click="acceptCall"
            >
              <el-icon><Phone /></el-icon>
            </el-button>
          </div>

          <!-- 其他操作 -->
          <div class="extra-actions">
            <el-button circle @click="toggleMinimize">
              <el-icon><Minus /></el-icon>
            </el-button>
            <el-button circle @click="toggleFullscreen">
              <el-icon><FullScreen /></el-icon>
            </el-button>
            <el-button circle @click="toggleChatPanel">
              <el-icon><ChatDotRound /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- 聊天面板 -->
        <div v-if="showChatPanel" class="chat-panel">
          <div class="chat-header">
            <span>聊天</span>
            <el-button text @click="showChatPanel = false">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <div class="chat-messages" ref="chatMessagesRef">
            <div
              v-for="msg in callMessages"
              :key="msg.id"
              class="chat-message"
              :class="{ self: msg.isSelf }"
            >
              <div class="message-content">{{ msg.content }}</div>
              <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
            </div>
          </div>
          <div class="chat-input">
            <el-input
              v-model="chatInput"
              placeholder="输入消息..."
              @keyup.enter="sendChatMessage"
            >
              <template #append>
                <el-button @click="sendChatMessage">
                  <el-icon><Promotion /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </div>

    <!-- 拨号对话框 -->
    <el-dialog
      v-model="showDialDialog"
      title="发起通话"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="dial-dialog">
        <div class="user-selector">
          <el-select
            v-model="selectedUser"
            placeholder="选择联系人"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="user in contacts"
              :key="user.id"
              :label="user.name || user.username"
              :value="user.id"
            >
              <div class="user-option">
                <el-avatar :size="24" :src="user.avatar">
                  {{ (user.name || user.username)?.charAt(0) }}
                </el-avatar>
                <span>{{ user.name || user.username }}</span>
              </div>
            </el-option>
          </el-select>
        </div>

        <div class="call-type-selector">
          <el-radio-group v-model="callType">
            <el-radio-button label="voice">
              <el-icon><Phone /></el-icon>
              语音通话
            </el-radio-button>
            <el-radio-button label="video">
              <el-icon><VideoCamera /></el-icon>
              视频通话
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <template #footer>
        <el-button @click="showDialDialog = false">取消</el-button>
        <el-button type="primary" @click="startCall" :disabled="!selectedUser">
          呼叫
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useStore } from 'vuex'
import {
  Phone, VideoCamera, Microphone, Mute, PhoneFilled, VideoCameraFilled,
  Loading, Minus, Close, ChatDotRound, Promotion, FullScreen, Refresh,
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  targetUserId: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:modelValue', 'end', 'accept', 'reject'])

const store = useStore()

// 状态
const showDialDialog = ref(props.modelValue && !props.targetUserId)
const selectedUser = ref(props.targetUserId)
const callType = ref('video')
const isMinimized = ref(false)
const isLocalExpanded = ref(false)
const isMuted = ref(false)
const isVideoOff = ref(false)
const showChatPanel = ref(false)

// 媒体流
const localStream = ref(null)
const remoteStream = ref(null)

// DOM 引用
const localVideoRef = ref(null)
const remoteVideoRef = ref(null)
const chatMessagesRef = ref(null)

// 通话状态
const callState = ref({
  status: 'idle', // idle | calling | incoming | connected | ended
  type: 'video',
  remoteUser: null,
})

// 计时
const callDuration = ref(0)
let callTimer = null

// 聊天消息
const chatInput = ref('')
const callMessages = ref([])

// 联系人列表
const contacts = computed(() => store.state.im.contacts || [])

// 是否是移动设备
const isMobile = computed(() => {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
})

// 通话状态文字
const callStatusText = computed(() => {
  switch (callState.value.status) {
    case 'calling':
      return '正在呼叫...'
    case 'incoming':
      return '邀请您通话'
    case 'connected':
      return formatTime(callDuration.value)
    case 'ended':
      return '通话结束'
    default:
      return ''
  }
})

// 视频占位符
const videoPlaceholder = computed(() => {
  return '/video-placeholder.mp4' // 可以替换为实际的占位视频
})

// 初始化本地媒体
const initLocalMedia = async () => {
  try {
    const constraints = {
      audio: !isMuted.value,
      video: callType.value === 'video' ? !isVideoOff.value : false,
    }

    const stream = await navigator.mediaDevices.getUserMedia(constraints)
    localStream.value = stream

    await nextTick()
    if (localVideoRef.value) {
      localVideoRef.value.srcObject = stream
    }
  } catch (error) {
    console.error('获取本地媒体失败:', error)
    ElMessage.error('无法访问摄像头或麦克风')
  }
}

// 开始通话
const startCall = async () => {
  if (!selectedUser.value) {
    ElMessage.warning('请选择通话对象')
    return
  }

  const user = contacts.value.find(c => c.id === selectedUser.value)
  callState.value = {
    status: 'calling',
    type: callType.value,
    remoteUser: user,
  }

  showDialDialog.value = false

  // 初始化本地媒体
  await initLocalMedia()

  // 启动计时器
  startCallTimer()

  // TODO: 通过 WebSocket 发送通话邀请
  ElMessage.success('正在呼叫...')
}

// 接听来电
const acceptCall = async () => {
  callState.value.status = 'connected'

  await initLocalMedia()

  // TODO: 建立 WebRTC 连接
  ElMessage.success('通话已接通')
}

// 拒绝来电
const rejectCall = () => {
  callState.value = { status: 'idle', type: 'video', remoteUser: null }
  emit('reject')
}

// 结束通话
const endCall = () => {
  stopCallTimer()

  // 停止媒体流
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => track.stop())
    localStream.value = null
  }

  if (remoteStream.value) {
    remoteStream.value.getTracks().forEach(track => track.stop())
    remoteStream.value = null
  }

  callState.value = { status: 'idle', type: 'video', remoteUser: null }
  callDuration.value = 0

  emit('end')
  ElMessage.info('通话已结束')
}

// 切换静音
const toggleMute = async () => {
  isMuted.value = !isMuted.value

  if (localStream.value) {
    localStream.value.getAudioTracks().forEach(track => {
      track.enabled = !isMuted.value
    })
  }
}

// 切换视频
const toggleVideo = async () => {
  isVideoOff.value = !isVideoOff.value

  if (localStream.value) {
    localStream.value.getVideoTracks().forEach(track => {
      track.enabled = !isVideoOff.value
    })
  }

  if (isVideoOff.value) {
    // 关闭视频后重新初始化，只获取音频
    await initLocalMedia()
  }
}

// 切换摄像头（移动端）
const switchCamera = async () => {
  // TODO: 实现前后摄像头切换
  ElMessage.info('切换摄像头')
}

// 切换最小化
const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value
}

// 切换本地视频大小
const toggleLocalExpand = () => {
  isLocalExpanded.value = !isLocalExpanded.value
}

// 切换全屏
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 切换聊天面板
const toggleChatPanel = () => {
  showChatPanel.value = !showChatPanel.value
  if (showChatPanel.value) {
    nextTick(() => {
      if (chatMessagesRef.value) {
        chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
      }
    })
  }
}

// 发送聊天消息
const sendChatMessage = () => {
  const content = chatInput.value.trim()
  if (!content) return

  callMessages.value.push({
    id: Date.now(),
    content,
    isSelf: true,
    timestamp: Date.now(),
  })

  chatInput.value = ''

  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })

  // TODO: 通过 WebRTC 数据通道发送消息
}

// 启动通话计时器
const startCallTimer = () => {
  callDuration.value = 0
  callTimer = setInterval(() => {
    callDuration.value++
  }, 1000)
}

// 停止通话计时器
const stopCallTimer = () => {
  if (callTimer) {
    clearInterval(callTimer)
    callTimer = null
  }
}

// 格式化时间
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 清理
onUnmounted(() => {
  stopCallTimer()

  if (localStream.value) {
    localStream.value.getTracks().forEach(track => track.stop())
  }

  if (remoteStream.value) {
    remoteStream.value.getTracks().forEach(track => track.stop())
  }
})

// 如果有目标用户ID，直接发起通话
onMounted(async () => {
  if (props.targetUserId) {
    selectedUser.value = props.targetUserId
    await startCall()
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.video-call-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
}

.video-call-modal {
  width: 100%;
  height: 100%;
  background: #000;

  &.minimized {
    width: auto;
    height: auto;
    position: fixed;
    bottom: 100px;
    right: 20px;
  }
}

.minimized-ball {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);

  .ball-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: white;
    z-index: 1;

    .el-icon {
      font-size: 20px;
    }

    .call-timer {
      font-size: 10px;
      margin-top: 2px;
    }
  }

  .pulse-ring {
    position: absolute;
    top: -4px;
    left: -4px;
    right: -4px;
    bottom: -4px;
    border: 2px solid rgba(102, 126, 234, 0.5);
    border-radius: 50%;
    animation: pulse-ring 2s infinite;
  }
}

.full-screen {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
}

.remote-video-area {
  flex: 1;
  position: relative;
  background: #1a1a1a;
  overflow: hidden;

  .remote-video,
  .remote-video-placeholder {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .remote-video-placeholder {
    opacity: 0.3;
  }

  .waiting-hint {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    color: white;

    .el-icon {
      font-size: 32px;
    }
  }

  .remote-info {
    position: absolute;
    bottom: 100px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    color: white;
    text-align: center;

    .user-avatar {
      margin-bottom: 12px;
    }

    .user-name {
      font-size: 20px;
      font-weight: 500;
      margin-bottom: 4px;
    }

    .call-status {
      font-size: 14px;
      opacity: 0.8;
    }
  }
}

.local-video-area {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 120px;
  height: 160px;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.3s;
  z-index: 10;

  &.expanded {
    width: calc(100% - 32px);
    height: calc(100% - 32px);
    top: 16px;
    right: 16px;
  }

  .local-video {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .local-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: rgba(255, 255, 255, 0.3);

    .el-icon {
      font-size: 32px;
    }
  }
}

.control-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);

  .control-buttons {
    display: flex;
    gap: 12px;
    align-items: center;

    .el-button {
      width: 50px;
      height: 50px;
      background: rgba(255, 255, 255, 0.2);
      border-color: rgba(255, 255, 255, 0.3);
      color: white;

      &:hover {
        background: rgba(255, 255, 255, 0.3);
      }

      &.el-button--danger {
        background: #f56c6c;
        border-color: #f56c6c;

        &:hover {
          background: #f78989;
        }
      }

      &.el-button--success {
        background: #67c23a;
        border-color: #67c23a;

        &:hover {
          background: #85ce61;
        }
      }
    }
  }

  .extra-actions {
    display: flex;
    gap: 8px;
    margin-left: auto;

    .el-button {
      width: 40px;
      height: 40px;
      background: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.2);
      color: white;

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }
}

.chat-panel {
  position: absolute;
  top: 16px;
  left: 16px;
  width: 300px;
  height: 400px;
  background: white;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);

  .chat-header {
    padding: 12px 16px;
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 500;
  }

  .chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 12px;

    .chat-message {
      margin-bottom: 12px;

      &.self {
        text-align: right;

        .message-content {
          background: #409eff;
          color: white;
          margin-left: auto;
        }
      }

      .message-content {
        display: inline-block;
        max-width: 70%;
        padding: 8px 12px;
        background: #f5f7fa;
        border-radius: 8px;
        word-break: break-word;
      }

      .message-time {
        font-size: 11px;
        color: #999;
        margin-top: 4px;
      }
    }
  }

  .chat-input {
    padding: 12px;
    border-top: 1px solid #eee;
  }
}

.dial-dialog {
  .user-selector {
    margin-bottom: 20px;
  }

  .call-type-selector {
    display: flex;
    justify-content: center;
    padding: 20px 0;

    :deep(.el-radio-button) {
      margin: 0 10px;

      .el-radio-button__inner {
        padding: 12px 20px;
      }
    }
  }

  .user-option {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

@keyframes pulse-ring {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}
</style>
