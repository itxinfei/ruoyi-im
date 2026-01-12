<template>
  <div class="video-call-container">
    <!-- 视频通话主界面 -->
    <div
      v-if="callState.status !== 'idle'"
      class="video-call-modal"
      :class="{ minimized: isMinimized }"
    >
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
            <source :src="callState.type === 'video' ? videoPlaceholder : ''" type="video/mp4" />
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
          <div ref="chatMessagesRef" class="chat-messages">
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
            <el-input v-model="chatInput" placeholder="输入消息..." @keyup.enter="sendChatMessage">
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
          <el-select v-model="selectedUser" placeholder="选择联系人" filterable style="width: 100%">
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
        <el-button type="primary" :disabled="!selectedUser" @click="startCall"> 呼叫 </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useStore } from 'vuex'
import {
  Phone,
  VideoCamera,
  Microphone,
  Mute,
  PhoneFilled,
  VideoCameraFilled,
  Loading,
  Minus,
  Close,
  ChatDotRound,
  Promotion,
  FullScreen,
  Refresh,
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import webRTCService from '@/utils/webrtc'
import signalingService from '@/utils/webrtc-signaling'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  targetUserId: {
    type: String,
    default: '',
  },
  incomingCall: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['update:modelValue', 'end', 'accept', 'reject'])

const store = useStore()

// 状态
const showDialDialog = ref(props.modelValue && !props.targetUserId && !props.incomingCall)
const selectedUser = ref(props.targetUserId)
const callType = ref('video')
const isMinimized = ref(false)
const isLocalExpanded = ref(false)
const isMuted = ref(false)
const isVideoOff = ref(false)
const showChatPanel = ref(false)
const facingMode = ref('user') // 摄像头方向

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
  callId: null,
})

// 计时
const callDuration = ref(0)
let callTimer = null

// 聊天消息
const chatInput = ref('')
const callMessages = ref([])

// 联系人列表
const contacts = computed(() => store.state.im.contacts || [])

// 当前用户
const currentUser = computed(() => store.state.user || {})

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
  return '/video-placeholder.mp4'
})

// 初始化 WebRTC
const initWebRTC = () => {
  // 初始化 PeerConnection
  webRTCService.initPeerConnection()

  // 监听 ICE 候选
  webRTCService.onIceCandidate = (candidate) => {
    const targetId = callState.value.remoteUser?.id
    if (targetId) {
      signalingService.sendIceCandidate(candidate, targetId)
    }
  }

  // 监听远程流
  webRTCService.onTrack = (stream) => {
    remoteStream.value = stream
    nextTick(() => {
      if (remoteVideoRef.value) {
        remoteVideoRef.value.srcObject = stream
      }
    })
  }

  // 监听数据通道消息
  webRTCService.onDataChannelMessage = (data) => {
    try {
      const message = typeof data === 'string' ? JSON.parse(data) : data
      callMessages.value.push({
        id: Date.now(),
        content: message.content || message,
        isSelf: false,
        timestamp: message.timestamp || Date.now(),
      })
      nextTick(() => {
        if (chatMessagesRef.value) {
          chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
        }
      })
    } catch (e) {
      console.error('解析数据通道消息失败:', e)
    }
  }

  // 监听数据通道打开
  webRTCService.onDataChannelOpen = () => {
    console.log('通话聊天数据通道已打开')
  }

  // 初始化信令服务
  const ws = store.state.im?.websocket
  if (ws) {
    signalingService.init(ws)

    // 监听来电
    signalingService.on('CallIncoming', handleIncomingCall)

    // 监听对方接听
    signalingService.on('CallAccepted', handleCallAccepted)

    // 监听对方拒绝
    signalingService.on('CallRejected', handleCallRejected)

    // 监听通话结束
    signalingService.on('CallEnded', handleCallEnded)

    // 监听 Offer
    signalingService.on('Offer', handleOffer)

    // 监听 Answer
    signalingService.on('Answer', handleAnswer)

    // 监听 ICE 候选
    signalingService.on('IceCandidate', handleIceCandidate)

    // 监听数据通道消息
    signalingService.on('DataChannelMessage', (data) => {
      webRTCService.onDataChannelMessage?.(data)
    })
  }
}

// 处理来电
const handleIncomingCall = async (payload) => {
  console.log('收到来电:', payload)
  callState.value = {
    status: 'incoming',
    type: payload.callType || 'video',
    remoteUser: payload.caller,
    callId: payload.callId,
  }
}

// 处理对方接听
const handleCallAccepted = async (payload) => {
  console.log('对方已接听:', payload)
  callState.value.status = 'connected'
}

// 处理对方拒绝
const handleCallRejected = (payload) => {
  console.log('对方已拒绝:', payload)
  ElMessage.info('对方拒绝了通话请求')
  endCall()
}

// 处理通话结束
const handleCallEnded = (payload) => {
  console.log('通话已结束:', payload)
  ElMessage.info('对方已结束通话')
  endCall()
}

// 处理 Offer
const handleOffer = async (payload) => {
  console.log('收到 Offer:', payload)
  await webRTCService.setRemoteDescription(payload)

  // 创建 Answer
  const answer = await webRTCService.createAnswer({
    offerToReceiveAudio: true,
    offerToReceiveVideo: callType.value === 'video',
  })

  // 发送 Answer
  signalingService.sendAnswer(answer, callState.value.remoteUser?.id)
}

// 处理 Answer
const handleAnswer = async (payload) => {
  console.log('收到 Answer:', payload)
  await webRTCService.setRemoteDescription(payload)
  callState.value.status = 'connected'
}

// 处理 ICE 候选
const handleIceCandidate = async (payload) => {
  console.log('收到 ICE 候选:', payload)
  await webRTCService.addIceCandidate(payload)
}

// 初始化本地媒体
const initLocalMedia = async () => {
  try {
    const constraints = {
      audio: true,
      video: callType.value === 'video' ? {
        facingMode: facingMode.value,
        width: { ideal: 1280 },
        height: { ideal: 720 },
      } : false,
    }

    const stream = await webRTCService.getLocalStream(constraints)
    localStream.value = stream

    // 添加到 PeerConnection
    webRTCService.addLocalStream(stream)

    await nextTick()
    if (localVideoRef.value) {
      localVideoRef.value.srcObject = stream
    }

    // 如果是发起方，创建数据通道
    if (callState.value.status === 'calling') {
      webRTCService.createDataChannel('chat')
    } else {
      // 如果是接收方，监听数据通道
      webRTCService.listenDataChannel()
    }
  } catch (error) {
    console.error('获取本地媒体失败:', error)
    ElMessage.error('无法访问摄像头或麦克风')
    throw error
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
    callId: `call_${Date.now()}_${currentUser.value.id}_${selectedUser.value}`,
  }

  showDialDialog.value = false

  // 初始化 WebRTC
  initWebRTC()

  // 初始化本地媒体
  await initLocalMedia()

  // 创建 Offer
  try {
    const offer = await webRTCService.createOffer({
      offerToReceiveAudio: true,
      offerToReceiveVideo: callType.value === 'video',
    })

    // 发送通话邀请和 Offer
    signalingService.sendCallInvite({
      callId: callState.value.callId,
      callType: callType.value,
      caller: {
        id: currentUser.value.id,
        name: currentUser.value.nickname || currentUser.value.username,
        avatar: currentUser.value.avatar,
      },
      targetId: selectedUser.value,
      sdp: offer.sdp,
    })

    // 启动计时器
    startCallTimer()
    ElMessage.success('正在呼叫...')
  } catch (error) {
    console.error('创建 Offer 失败:', error)
    ElMessage.error('发起通话失败')
  }
}

// 接听来电
const acceptCall = async () => {
  // 初始化 WebRTC
  initWebRTC()

  // 初始化本地媒体
  await initLocalMedia()

  // 发送接听确认
  signalingService.sendCallAccepted(
    callState.value.callId,
    callState.value.remoteUser?.id
  )

  ElMessage.success('通话已接通')
}

// 拒绝来电
const rejectCall = () => {
  signalingService.sendCallRejected(
    callState.value.callId,
    callState.value.remoteUser?.id,
    '用户拒绝'
  )

  callState.value = { status: 'idle', type: 'video', remoteUser: null, callId: null }
  emit('reject')
}

// 结束通话
const endCall = () => {
  // 发送结束通话信号
  if (callState.value.callId && callState.value.remoteUser) {
    signalingService.sendCallEnded(
      callState.value.callId,
      callState.value.remoteUser.id,
      '主动结束'
    )
  }

  stopCallTimer()

  // 关闭 WebRTC
  webRTCService.close()

  // 停止媒体流
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => track.stop())
    localStream.value = null
  }

  if (remoteStream.value) {
    // 远程流不需要手动停止，由 WebRTC 管理
    remoteStream.value = null
  }

  callState.value = { status: 'idle', type: 'video', remoteUser: null, callId: null }
  callDuration.value = 0

  emit('end')
  ElMessage.info('通话已结束')
}

// 切换静音
const toggleMute = async () => {
  isMuted.value = !isMuted.value
  webRTCService.toggleAudio(!isMuted.value)
}

// 切换视频
const toggleVideo = async () => {
  isVideoOff.value = !isVideoOff.value
  webRTCService.toggleVideo(!isVideoOff.value)
}

// 切换摄像头（移动端）
const switchCamera = async () => {
  try {
    facingMode.value = facingMode.value === 'user' ? 'environment' : 'user'

    const constraints = {
      video: {
        facingMode: facingMode.value,
        width: { ideal: 1280 },
        height: { ideal: 720 },
      },
    }

    const newStream = await navigator.mediaDevices.getUserMedia(constraints)
    await webRTCService.replaceVideoTrack(newStream)

    localStream.value = newStream
    if (localVideoRef.value) {
      localVideoRef.value.srcObject = newStream
    }

    ElMessage.success('摄像头已切换')
  } catch (error) {
    console.error('切换摄像头失败:', error)
    ElMessage.error('切换摄像头失败')
  }
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

  // 添加到本地列表
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

  // 通过数据通道发送
  webRTCService.sendDataChannelMessage({
    content,
    timestamp: Date.now(),
  })

  // 也通过信令通道发送（备用）
  signalingService.sendDataChannelMessage(content, callState.value.remoteUser?.id)
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
const formatTime = seconds => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 监听来电
watch(() => props.incomingCall, (newVal) => {
  if (newVal) {
    callState.value = {
      status: 'incoming',
      type: newVal.type || 'video',
      remoteUser: newVal.caller,
      callId: newVal.callId,
    }
  }
})

// 清理
onUnmounted(() => {
  stopCallTimer()
  webRTCService.close()
  signalingService.cleanup()

  if (localStream.value) {
    localStream.value.getTracks().forEach(track => track.stop())
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
