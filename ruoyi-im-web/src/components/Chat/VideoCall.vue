<!--
  WebRTC 视频通话组件 - 全面升级版
  功能：点对点音视频通话，支持钉钉5.6风格UI
  特性：
  - 可拖拽画中画本地视频
  - 网络质量实时指示
  - 智能控制栏自动隐藏
  - 屏幕共享
  - 完整的通话控制

  @author RuoYi-IM
  @version 2.0.0
-->
<template>
  <teleport to="body">
    <div v-if="visible" class="video-call-container">
      <!-- 主视频通话界面 -->
      <div
        ref="interfaceRef"
        class="video-call-interface"
        :class="{
          fullscreen: isFullscreen,
          'controls-hidden': !showControls && callState === 'connected',
        }"
        @mousemove="handleMouseMove"
        @click="handleClick"
      >
        <!-- 远程视频区域（对方） -->
        <div class="remote-video-wrapper">
          <video
            ref="remoteVideoRef"
            class="remote-video"
            autoplay
            playsinline
            :muted="false"
          ></video>

          <!-- 屏幕共享视频 -->
          <video
            v-if="screenStream"
            ref="screenVideoRef"
            class="screen-video"
            autoplay
            playsinline
          ></video>

          <!-- 视频占位符 -->
          <transition name="placeholder-fade">
            <div v-if="!remoteStreamReady" class="video-placeholder">
              <div class="placeholder-content">
                <img :src="remoteUserAvatar" class="placeholder-avatar" />
                <p class="placeholder-text">
                  <template v-if="callState === 'calling'">
                    <i class="el-icon-loading"></i> 正在呼叫...
                  </template>
                  <template v-else-if="callState === 'connecting'">
                    <i class="el-icon-loading"></i> 连接中...
                  </template>
                  <template v-else>
                    {{ callType === 'voice' ? '语音通话中' : '等待对方接听...' }}
                  </template>
                </p>
              </div>
            </div>
          </transition>

          <!-- 连接状态覆盖层 -->
          <div v-if="connectionIssue" class="connection-issue-overlay">
            <i class="el-icon-warning-outline"></i>
            <span>{{ connectionIssueText }}</span>
          </div>
        </div>

        <!-- 本地视频画中画（可拖拽） -->
        <transition name="pip-fade">
          <local-video-pip
            v-if="localStreamReady && showLocalVideo"
            ref="localPipRef"
            :stream="localStream"
            :user-avatar="currentUserAvatar"
            :mirrored="!isScreenSharing"
            :is-muted="!isMicOn"
            :show-controls="true"
            :initial-size="pipSize"
          />
        </transition>

        <!-- 顶部信息栏 -->
        <transition name="info-bar-slide">
          <div v-if="showInfoBar" class="call-info-bar">
            <div class="caller-info">
              <img :src="remoteUserAvatar" class="caller-avatar" />
              <div class="caller-details">
                <div class="caller-name">{{ remoteUserName }}</div>
                <div class="call-status">
                  <span v-if="callState === 'calling'" class="status-calling">
                    <i class="el-icon-loading"></i> 呼叫中
                  </span>
                  <span v-else-if="callState === 'connecting'" class="status-connecting">
                    <i class="el-icon-loading"></i> 连接中
                  </span>
                  <span v-else-if="callState === 'connected'" class="status-connected">
                    <i class="el-icon-video-camera"></i>
                    {{ formatDuration(callDuration) }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 网络质量指示器 -->
            <network-indicator
              v-if="showNetworkIndicator"
              :quality="networkQuality"
              :delay="networkStats.delay"
              :packet-loss="networkStats.packetLoss"
              :show-delay="true"
              :always-show-detail="false"
            />

            <!-- 加密标识 -->
            <div v-if="showEncryptionBadge" class="encryption-badge" title="端到端加密">
              <i class="el-icon-lock"></i>
            </div>
          </div>
        </transition>

        <!-- 底部控制栏 -->
        <transition name="control-bar-slide">
          <div v-if="showControls" class="control-bar">
            <div class="control-buttons">
              <!-- 麦克风开关 -->
              <control-button
                :active="!isMicOn"
                :icon="isMicOn ? 'el-icon-microphone' : 'el-icon-microphone-off'"
                :tooltip="isMicOn ? '关闭麦克风' : '开启麦克风'"
                @click="toggleMic"
              />

              <!-- 摄像头开关 -->
              <control-button
                v-if="callType === 'video'"
                :active="!isCameraOn"
                :icon="isCameraOn ? 'el-icon-video-camera' : 'el-icon-video-camera-filled'"
                :tooltip="isCameraOn ? '关闭摄像头' : '开启摄像头'"
                @click="toggleCamera"
              />

              <!-- 屏幕共享 -->
              <control-button
                v-if="callType === 'video'"
                :active="isScreenSharing"
                icon="el-icon-monitor"
                tooltip="屏幕共享"
                @click="toggleScreenShare"
              />

              <!-- 扬声器开关 -->
              <control-button
                :active="!isSpeakerOn"
                :icon="isSpeakerOn ? 'el-icon-bell' : 'el-icon-close-notification'"
                :tooltip="isSpeakerOn ? '关闭扬声器' : '开启扬声器'"
                @click="toggleSpeaker"
              />

              <!-- 摄像头切换 -->
              <control-button
                v-if="hasMultipleCameras"
                icon="el-icon-refresh"
                tooltip="切换摄像头"
                @click="switchCamera"
              />

              <!-- 全屏切换 -->
              <control-button
                :icon="isFullscreen ? 'el-icon-crop' : 'el-icon-full-screen'"
                :tooltip="isFullscreen ? '退出全屏' : '全屏'"
                @click="toggleFullscreen"
              />

              <!-- 挂断按钮 -->
              <control-button
                class="hangup-btn"
                icon="el-icon-phone-outline"
                tooltip="挂断"
                @click="hangup"
              />
            </div>
          </div>
        </transition>
      </div>

      <!-- 来电提醒弹窗 -->
      <incoming-call-modal
        :visible="callState === 'incoming'"
        :caller-avatar="remoteUserAvatar"
        :caller-name="remoteUserName"
        :call-type="callType"
        :timeout-duration="incomingTimeout"
        @accept="handleIncomingAccept"
        @reject="handleIncomingReject"
        @timeout="handleIncomingTimeout"
      />
    </div>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import LocalVideoPip from '../VideoCall/LocalVideoPip.vue'
import NetworkIndicator from '../VideoCall/NetworkIndicator.vue'
import IncomingCallModal from '../VideoCall/IncomingCallModal.vue'
import { sendSignal } from '@/api/im/video-call'
import { useNetworkStats } from '@/composables/useNetworkStats.js'

// ============ 子组件 ============
const ControlButton = {
  name: 'ControlButton',
  props: {
    active: Boolean,
    icon: String,
    tooltip: String,
  },
  template: `
    <el-tooltip :content="tooltip" placement="top">
      <button class="control-btn" :class="{ active }">
        <i :class="icon"></i>
      </button>
    </el-tooltip>
  `,
}

// ============ Props ============
const props = defineProps({
  /** 是否显示通话界面 */
  visible: {
    type: Boolean,
    default: false,
  },
  /** 通话ID */
  callId: {
    type: [Number, String],
    default: null,
  },
  /** 通话类型: video | voice */
  callType: {
    type: String,
    default: 'video',
  },
  /** 远程用户信息 */
  remoteUser: {
    type: Object,
    default: () => ({}),
  },
  /** 通话状态 */
  callState: {
    type: String,
    default: 'calling', // calling | incoming | connecting | connected | ended
  },
  /** 信令服务器URL */
  signalingUrl: {
    type: String,
    default: '/ws/signaling',
  },
  /** 来电超时时间（秒） */
  incomingTimeout: {
    type: Number,
    default: 30,
  },
})

// ============ Emits ============
const emit = defineEmits([
  'update:visible',
  'accept',
  'reject',
  'hangup',
  'state-change',
])

// ============ Refs ============
const interfaceRef = ref(null)
const remoteVideoRef = ref(null)
const screenVideoRef = ref(null)
const localPipRef = ref(null)

// ============ 本地状态 ============
const localStream = ref(null)
const remoteStream = ref(null)
const screenStream = ref(null)
const peerConnection = ref(null)

// 设备状态
const isMicOn = ref(true)
const isCameraOn = ref(true)
const isSpeakerOn = ref(true)
const isScreenSharing = ref(false)
const hasMultipleCameras = ref(false)

// UI 状态
const isFullscreen = ref(false)
const showControls = ref(true)
const showLocalVideo = ref(true)
const pipSize = ref('large') // large | small

// 通话状态
const localStreamReady = ref(false)
const remoteStreamReady = ref(false)
const callDuration = ref(0)
const callTimer = ref(null)

// 控制栏自动隐藏
const controlsTimer = ref(null)
const lastMouseMoveTime = ref(0)

// 网络质量监测
const networkQuality = ref('disconnected')
const networkStats = ref({ delay: 0, packetLoss: 0 })

// 连接问题
const connectionIssue = ref(false)
const connectionIssueText = ref('')

// ============ 常量 ============
const ICE_SERVERS = [
  { urls: 'stun:stun.l.google.com:19302' },
  { urls: 'stun:stun1.l.google.com:19302' },
]

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// ============ 计算属性 ============
const remoteUserAvatar = computed(() => props.remoteUser?.avatar || defaultAvatar)
const remoteUserName = computed(() => props.remoteUser?.name || props.remoteUser?.nickname || '对方')
const currentUserAvatar = computed(() => props.remoteUser?.myAvatar || defaultAvatar)
const showInfoBar = computed(() => showControls.value || props.callState === 'calling' || props.callState === 'connecting')
const showNetworkIndicator = computed(() => props.callState === 'connected')
const showEncryptionBadge = computed(() => props.callState === 'connected')

// ============ 工具函数 ============
const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// ============ 媒体流管理 ============
/**
 * 初始化本地媒体流
 */
const initLocalStream = async () => {
  try {
    const constraints = {
      audio: true,
      video: props.callType === 'video' ? {
        width: { ideal: 1280 },
        height: { ideal: 720 },
        facingMode: 'user',
      } : false,
    }

    localStream.value = await navigator.mediaDevices.getUserMedia(constraints)
    localStreamReady.value = true

    // 检查是否有多个摄像头
    const devices = await navigator.mediaDevices.enumerateDevices()
    const videoDevices = devices.filter(d => d.kind === 'videoinput')
    hasMultipleCameras.value = videoDevices.length > 1

    return localStream.value
  } catch (error) {
    console.error('获取媒体设备失败:', error)
    ElMessage.error('无法访问摄像头或麦克风，请检查权限设置')
    throw error
  }
}

/**
 * 创建 WebRTC 连接
 */
const createPeerConnection = () => {
  const config = {
    iceServers: ICE_SERVERS,
  }

  peerConnection.value = new RTCPeerConnection(config)

  // 添加本地流到连接
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => {
      peerConnection.value.addTrack(track, localStream.value)
    })
  }

  // 监听远程流
  peerConnection.value.ontrack = (event) => {
    if (event.streams && event.streams[0]) {
      remoteStream.value = event.streams[0]
      remoteStreamReady.value = true

      if (remoteVideoRef.value) {
        remoteVideoRef.value.srcObject = remoteStream.value
      }

      // 开始网络质量监测
      startNetworkMonitoring()
    }
  }

  // 监听 ICE 候选
  peerConnection.value.onicecandidate = (event) => {
    if (event.candidate) {
      sendSignalingMessage({
        type: 'ice-candidate',
        candidate: event.candidate,
      })
    }
  }

  // 监听连接状态
  peerConnection.value.onconnectionstatechange = () => {
    const state = peerConnection.value.connectionState
    console.log('连接状态:', state)

    if (state === 'connected') {
      connectionIssue.value = false
      startCallTimer()
      emit('state-change', 'connected')
    } else if (state === 'disconnected') {
      connectionIssue.value = true
      connectionIssueText.value = '连接中断，正在重连...'
    } else if (state === 'failed') {
      connectionIssue.value = true
      connectionIssueText.value = '连接失败'
      stopCallTimer()
      emit('state-change', 'ended')
    }
  }

  // 监听 ICE 连接状态
  peerConnection.value.oniceconnectionstatechange = () => {
    const state = peerConnection.value.iceConnectionState
    console.log('ICE连接状态:', state)

    if (state === 'connected' || state === 'completed') {
      connectionIssue.value = false
    } else if (state === 'disconnected') {
      connectionIssue.value = true
      connectionIssueText.value = '网络连接中断'
    } else if (state === 'failed') {
      connectionIssue.value = true
      connectionIssueText.value = '网络连接失败'
    }
  }

  return peerConnection.value
}

/**
 * 发起呼叫
 */
const startCall = async () => {
  try {
    await initLocalStream()
    const pc = createPeerConnection()

    const offer = await pc.createOffer()
    await pc.setLocalDescription(offer)

    sendSignalingMessage({
      type: 'offer',
      sdp: offer,
      callType: props.callType,
    })

    emit('state-change', 'calling')
  } catch (error) {
    console.error('发起呼叫失败:', error)
    ElMessage.error('发起呼叫失败')
  }
}

/**
 * 接听呼叫
 */
const acceptCall = async () => {
  try {
    await initLocalStream()
    const pc = createPeerConnection()

    emit('accept')
    emit('state-change', 'connecting')
  } catch (error) {
    console.error('接听失败:', error)
    ElMessage.error('接听失败')
  }
}

/**
 * 拒绝呼叫
 */
const rejectCall = () => {
  emit('reject')
  emit('update:visible', false)
}

/**
 * 挂断通话
 */
const hangup = () => {
  stopCallTimer()
  stopNetworkMonitoring()

  // 停止所有轨道
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => track.stop())
  }
  if (remoteStream.value) {
    remoteStream.value.getTracks().forEach(track => track.stop())
  }
  if (screenStream.value) {
    screenStream.value.getTracks().forEach(track => track.stop())
  }

  // 关闭连接
  if (peerConnection.value) {
    peerConnection.value.close()
    peerConnection.value = null
  }

  // 发送挂断信令
  sendSignalingMessage({ type: 'hangup' })

  // 重置状态
  localStream.value = null
  remoteStream.value = null
  screenStream.value = null
  localStreamReady.value = false
  remoteStreamReady.value = false
  connectionIssue.value = false

  emit('hangup')
  emit('update:visible', false)
}

// ============ WebRTC 信令处理 ============
/**
 * 处理 offer
 */
const handleOffer = async (offer) => {
  try {
    if (!peerConnection.value) {
      await initLocalStream()
      createPeerConnection()
    }

    await peerConnection.value.setRemoteDescription(new RTCSessionDescription(offer))

    // 创建并发送 answer
    const answer = await peerConnection.value.createAnswer()
    await peerConnection.value.setLocalDescription(answer)
    sendSignalingMessage({
      type: 'answer',
      sdp: answer,
    })
  } catch (error) {
    console.error('处理 offer 失败:', error)
  }
}

/**
 * 处理 answer
 */
const handleAnswer = async (answer) => {
  try {
    if (peerConnection.value) {
      await peerConnection.value.setRemoteDescription(new RTCSessionDescription(answer))
      emit('state-change', 'connecting')
    }
  } catch (error) {
    console.error('处理 answer 失败:', error)
  }
}

/**
 * 处理 ICE 候选
 */
const handleIceCandidate = async (candidate) => {
  try {
    if (peerConnection.value) {
      await peerConnection.value.addIceCandidate(new RTCIceCandidate(candidate))
    }
  } catch (error) {
    console.error('添加 ICE 候选失败:', error)
  }
}

/**
 * 发送信令消息
 */
const sendSignalingMessage = async (message) => {
  if (!props.callId) {
    console.warn('未设置callId，无法发送信令')
    return
  }

  try {
    let signalType = 'offer'
    if (message.type === 'answer') {
      signalType = 'answer'
    } else if (message.type === 'ice-candidate' || message.candidate) {
      signalType = 'ice-candidate'
    } else if (message.type === 'hangup') {
      console.log('挂断通话:', props.callId)
      return
    }

    const signalData = message.candidate
      ? JSON.stringify(message.candidate)
      : JSON.stringify(message)

    await sendSignal({
      callId: Number(props.callId),
      signalType,
      signalData,
    })

    console.log('信令发送成功:', signalType)
  } catch (error) {
    console.error('发送信令失败:', error)
  }
}

// ============ 设备控制 ============
/**
 * 切换麦克风
 */
const toggleMic = () => {
  if (localStream.value) {
    const audioTrack = localStream.value.getAudioTracks()[0]
    if (audioTrack) {
      isMicOn.value = !isMicOn.value
      audioTrack.enabled = isMicOn.value
    }
  }
}

/**
 * 切换摄像头
 */
const toggleCamera = () => {
  if (localStream.value) {
    const videoTrack = localStream.value.getVideoTracks()[0]
    if (videoTrack) {
      isCameraOn.value = !isCameraOn.value
      videoTrack.enabled = isCameraOn.value
    }
  }
}

/**
 * 切换扬声器
 */
const toggleSpeaker = () => {
  isSpeakerOn.value = !isSpeakerOn.value
  if (remoteVideoRef.value) {
    remoteVideoRef.value.muted = !isSpeakerOn.value
  }
}

/**
 * 切换屏幕共享
 */
const toggleScreenShare = async () => {
  if (isScreenSharing.value) {
    // 停止屏幕共享
    if (screenStream.value) {
      screenStream.value.getTracks().forEach(track => track.stop())
      screenStream.value = null
    }

    // 恢复摄像头
    if (localStream.value && peerConnection.value) {
      const videoTrack = localStream.value.getVideoTracks()[0]
      const sender = peerConnection.value.getSenders().find(s => s.track.kind === 'video')
      if (sender && videoTrack) {
        await sender.replaceTrack(videoTrack)
      }
    }

    isScreenSharing.value = false
  } else {
    // 开始屏幕共享
    try {
      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: {
          width: { ideal: 1920 },
          height: { ideal: 1080 },
        },
        audio: false,
      })

      screenStream.value = stream

      // 替换视频轨道
      if (peerConnection.value) {
        const screenTrack = stream.getVideoTracks()[0]
        const sender = peerConnection.value.getSenders().find(s => s.track.kind === 'video')
        if (sender) {
          await sender.replaceTrack(screenTrack)
        }

        // 监听用户停止共享
        screenTrack.onended = () => {
          toggleScreenShare()
        }
      }

      isScreenSharing.value = true
    } catch (error) {
      console.error('屏幕共享失败:', error)
      if (error.name === 'NotAllowedError') {
        ElMessage.info('取消了屏幕共享')
      } else {
        ElMessage.warning('屏幕共享失败')
      }
    }
  }
}

/**
 * 切换摄像头（移动端前后摄像头）
 */
const switchCamera = async () => {
  try {
    const currentTrack = localStream.value?.getVideoTracks()[0]
    const currentFacingMode = currentTrack?.getSettings()?.facingMode

    const newConstraints = {
      audio: true,
      video: {
        facingMode: currentFacingMode === 'user' ? 'environment' : 'user',
      },
    }

    const newStream = await navigator.mediaDevices.getUserMedia(newConstraints)
    const newVideoTrack = newStream.getVideoTracks()[0]

    // 替换轨道
    if (peerConnection.value) {
      const sender = peerConnection.value.getSenders().find(s => s.track.kind === 'video')
      if (sender) {
        await sender.replaceTrack(newVideoTrack)
      }
    }

    // 停止旧轨道
    if (currentTrack) {
      currentTrack.stop()
    }

    // 更新本地流
    localStream.value = newStream

    // 更新视频元素
    if (localPipRef.value) {
      localPipRef.value.setStream(newStream)
    }

    ElMessage.success('已切换摄像头')
  } catch (error) {
    console.error('切换摄像头失败:', error)
    ElMessage.error('切换摄像头失败')
  }
}

// ============ 全屏控制 ============
/**
 * 切换全屏
 */
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  if (isFullscreen.value) {
    enterFullscreen()
  } else {
    exitFullscreen()
  }
}

const enterFullscreen = () => {
  const elem = interfaceRef.value
  if (elem.requestFullscreen) {
    elem.requestFullscreen()
  } else if (elem.webkitRequestFullscreen) {
    elem.webkitRequestFullscreen()
  } else if (elem.mozRequestFullScreen) {
    elem.mozRequestFullScreen()
  } else if (elem.msRequestFullscreen) {
    elem.msRequestFullscreen()
  }
}

const exitFullscreen = () => {
  if (document.exitFullscreen) {
    document.exitFullscreen()
  } else if (document.webkitExitFullscreen) {
    document.webkitExitFullscreen()
  } else if (document.mozCancelFullScreen) {
    document.mozCancelFullScreen()
  } else if (document.msExitFullscreen) {
    document.msExitFullscreen()
  }
}

// ============ 控制栏自动隐藏 ============
/**
 * 处理鼠标移动
 */
const handleMouseMove = () => {
  const now = Date.now()
  lastMouseMoveTime.value = now

  if (props.callState === 'connected') {
    showControls.value = true
    resetControlsTimer()
  }
}

/**
 * 处理点击
 */
const handleClick = () => {
  if (props.callState === 'connected') {
    showControls.value = true
    resetControlsTimer()
  }
}

/**
 * 重置控制栏计时器
 */
const resetControlsTimer = () => {
  if (controlsTimer.value) {
    clearTimeout(controlsTimer.value)
  }

  controlsTimer.value = setTimeout(() => {
    if (props.callState === 'connected') {
      showControls.value = false
    }
  }, 3000)
}

// ============ 通话计时 ============
/**
 * 开始通话计时
 */
const startCallTimer = () => {
  callDuration.value = 0
  callTimer.value = setInterval(() => {
    callDuration.value++
  }, 1000)
}

/**
 * 停止通话计时
 */
const stopCallTimer = () => {
  if (callTimer.value) {
    clearInterval(callTimer.value)
    callTimer.value = null
  }
}

// ============ 网络质量监测 ============
let networkStatsCollector = null

/**
 * 开始网络监测
 */
const startNetworkMonitoring = () => {
  if (!peerConnection.value) return

  // 使用 useNetworkStats 进行监测
  const { quality, stats, start, stop } = useNetworkStats(peerConnection, {
    updateInterval: 2000,
    autoStart: true,
  })

  // 监听质量变化
  // 注意：这里需要通过 watch 来实现
  networkStatsCollector = { quality, stats, stop }
}

/**
 * 停止网络监测
 */
const stopNetworkMonitoring = () => {
  if (networkStatsCollector) {
    networkStatsCollector.stop?.()
    networkStatsCollector = null
  }
  networkQuality.value = 'disconnected'
  networkStats.value = { delay: 0, packetLoss: 0 }
}

// ============ 来电处理 ============
/**
 * 处理来电接听
 */
const handleIncomingAccept = () => {
  acceptCall()
}

/**
 * 处理来电拒绝
 */
const handleIncomingReject = () => {
  rejectCall()
}

/**
 * 处理来电超时
 */
const handleIncomingTimeout = () => {
  rejectCall()
  ElMessage.info('对方未接听，通话已超时')
}

// ============ 监听 props 变化 ============
watch(() => props.visible, async (val) => {
  if (val) {
    // 显示通话界面
    if (props.callState === 'calling') {
      await startCall()
    } else if (props.callState === 'incoming') {
      // 来电，等待用户操作
    }
  } else {
    // 隐藏通话界面，清理资源
    hangup()
  }
})

watch(() => props.callState, (newState) => {
  if (newState === 'connected') {
    startCallTimer()
    showControls.value = true
    resetControlsTimer()
  } else if (newState === 'ended') {
    stopCallTimer()
  }
})

// ============ 暴露方法 ============
defineExpose({
  acceptCall,
  rejectCall,
  hangup,
  toggleMic,
  toggleCamera,
  toggleSpeaker,
  toggleScreenShare,
})

// ============ 生命周期 ============
onMounted(() => {
  // 添加键盘快捷键监听
  document.addEventListener('keydown', handleKeydown)

  // 监听全屏变化
  document.addEventListener('fullscreenchange', handleFullscreenChange)
  document.addEventListener('webkitfullscreenchange', handleFullscreenChange)
})

onUnmounted(() => {
  // 清理
  hangup()
  stopNetworkMonitoring()

  if (controlsTimer.value) {
    clearTimeout(controlsTimer.value)
  }

  document.removeEventListener('keydown', handleKeydown)
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
  document.removeEventListener('webkitfullscreenchange', handleFullscreenChange)
})

/**
 * 处理键盘快捷键
 */
const handleKeydown = (e) => {
  if (!props.visible) return

  switch (e.code) {
    case 'Space':
      // 空格切换麦克风
      e.preventDefault()
      toggleMic()
      break
    case 'KeyV':
      // V键切换摄像头
      e.preventDefault()
      if (props.callType === 'video') {
        toggleCamera()
      }
      break
    case 'KeyS':
      // S键切换屏幕共享
      e.preventDefault()
      if (props.callType === 'video') {
        toggleScreenShare()
      }
      break
    case 'Escape':
      // ESC 挂断
      e.preventDefault()
      hangup()
      break
  }
}

/**
 * 处理全屏状态变化
 */
const handleFullscreenChange = () => {
  isFullscreen.value = !!(
    document.fullscreenElement ||
    document.webkitFullscreenElement ||
    document.mozFullScreenElement ||
    document.msFullscreenElement
  )
}
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

// ============ 容器 ============
.video-call-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  background: #000;
}

.video-call-interface {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  &.fullscreen {
    &:fullscreen {
      width: 100vw;
      height: 100vh;
    }
  }

  &.controls-hidden {
    cursor: none;

    .control-bar {
      opacity: 0;
    }
  }
}

// ============ 远程视频区域 ============
.remote-video-wrapper {
  flex: 1;
  position: relative;
  background: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  .remote-video {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .screen-video {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: contain;
    z-index: 2;
  }
}

// ============ 视频占位符 ============
.video-placeholder {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #2c3e50 0%, #1a1a1a 100%);

  .placeholder-content {
    text-align: center;
    color: #fff;

    .placeholder-avatar {
      width: 120px;
      height: 120px;
      border-radius: 50%;
      object-fit: cover;
      margin-bottom: 20px;
      border: 4px solid rgba(255, 255, 255, 0.2);
    }

    .placeholder-text {
      font-size: 16px;
      opacity: 0.8;
      margin: 0;

      i {
        margin-right: 8px;
      }
    }
  }
}

.placeholder-fade-enter-active,
.placeholder-fade-leave-active {
  transition: opacity 0.3s ease;
}

.placeholder-fade-enter-from,
.placeholder-fade-leave-to {
  opacity: 0;
}

// ============ 连接问题覆盖层 ============
.connection-issue-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: rgba(255, 77, 79, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  color: #fff;
  font-size: 14px;
  z-index: 5;

  i {
    font-size: 18px;
  }
}

// ============ 画中画过渡动画 ============
.pip-fade-enter-active,
.pip-fade-leave-active {
  transition: all 0.3s ease;
}

.pip-fade-enter-from,
.pip-fade-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

// ============ 顶部信息栏 ============
.call-info-bar {
  position: absolute;
  top: 16px;
  left: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  pointer-events: none;
  z-index: 10;

  .caller-info {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 16px;
    background: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(10px);
    border-radius: 24px;
    pointer-events: auto;

    .caller-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      object-fit: cover;
    }

    .caller-details {
      .caller-name {
        font-size: 16px;
        font-weight: 500;
        color: #fff;
        line-height: 1.2;
        margin-bottom: 2px;
      }

      .call-status {
        font-size: 13px;
        color: rgba(255, 255, 255, 0.7);

        i {
          margin-right: 4px;
        }
      }
    }
  }

  .encryption-badge {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(10px);
    border-radius: 50%;
    color: rgba(255, 255, 255, 0.6);
    font-size: 14px;
    pointer-events: auto;
  }
}

.info-bar-slide-enter-active,
.info-bar-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.info-bar-slide-enter-from,
.info-bar-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

// ============ 底部控制栏 ============
.control-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  display: flex;
  justify-content: center;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  z-index: 10;
  pointer-events: none;

  .control-buttons {
    display: flex;
    align-items: center;
    gap: 12px;
    pointer-events: auto;

    .control-btn {
      width: 56px;
      height: 56px;
      border-radius: 50%;
      border: none;
      background: rgba(255, 255, 255, 0.15);
      backdrop-filter: blur(10px);
      color: #fff;
      font-size: 24px;
      cursor: pointer;
      transition: all 0.2s ease;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        background: rgba(255, 255, 255, 0.25);
        transform: scale(1.05);
      }

      &:active {
        transform: scale(0.95);
      }

      &.active {
        background: rgba(255, 77, 79, 0.9);
      }

      &.hangup-btn {
        background: rgba(255, 77, 79, 0.9);

        &:hover {
          background: rgba(255, 77, 79, 1);
        }
      }
    }
  }
}

.control-bar-slide-enter-active,
.control-bar-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.control-bar-slide-enter-from,
.control-bar-slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

// ============ 响应式 ============
@media (max-width: 768px) {
  .call-info-bar {
    top: 12px;
    left: 12px;
    right: 12px;

    .caller-info {
      padding: 6px 12px;

      .caller-avatar {
        width: 32px;
        height: 32px;
      }

      .caller-details .caller-name {
        font-size: 14px;
      }

      .caller-details .call-status {
        font-size: 12px;
      }
    }
  }

  .control-bar {
    padding: 12px;

    .control-buttons {
      gap: 8px;

      .control-btn {
        width: 48px;
        height: 48px;
        font-size: 20px;
      }
    }
  }
}
</style>
