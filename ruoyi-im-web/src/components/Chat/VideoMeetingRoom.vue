<template>
  <teleport to="body">
    <div v-if="visible" class="video-meeting-room">
      <!-- 顶部工具栏 -->
      <div class="room-header">
        <div class="header-left">
          <span class="meeting-title">{{ meeting?.title || '视频会议' }}</span>
          <span class="meeting-time">{{ meetingDuration }}</span>
        </div>
        <div class="header-right">
          <span class="participant-count">
            <span class="material-icons-outlined">group</span>
            {{ participants.length }}人
          </span>
          <el-button :icon="MoreFilled" circle size="small" />
        </div>
      </div>

      <!-- 主视频区域 -->
      <div class="room-main">
        <!-- 视频网格 -->
        <div :class="['video-grid', `grid-${gridSize}`]">
          <!-- 本地视频 -->
          <div
            :class="['video-cell', 'is-local', { 'is-speaking': isLocalSpeaking }]"
            @click="handleSetActive(null)"
          >
            <div class="video-placeholder local-video">
              <video
                ref="localVideoRef"
                autoplay
                muted
                playsinline
                class="video-element"
              ></video>
              <div v-if="!localStream" class="no-video">
                <span class="material-icons-outlined">videocam_off</span>
                <span>摄像头已关闭</span>
              </div>
            </div>
            <!-- 本地用户信息 -->
            <div class="participant-info">
              <span class="participant-name">我</span>
              <el-tag v-if="isHost" size="small" type="warning">主持人</el-tag>
            </div>
            <!-- 本地状态指示 -->
            <div class="status-indicators">
              <span v-if="!isVideoEnabled" class="status-icon video-off">
                <span class="material-icons-outlined">videocam_off</span>
              </span>
              <span v-if="!isAudioEnabled" class="status-icon audio-off">
                <span class="material-icons-outlined">mic_off</span>
              </span>
              <span v-if="isLocalSpeaking" class="speaking-indicator"></span>
            </div>
          </div>

          <!-- 远程参会者视频 -->
          <div
            v-for="participant in remoteParticipants"
            :key="participant.id"
            :class="[
              'video-cell',
              { 'is-active': activeParticipantId === participant.id },
              { 'is-speaking': participant.isSpeaking }
            ]"
            @click="handleSetActive(participant.id)"
          >
            <div class="video-placeholder">
              <video
                v-if="participant.stream"
                :ref="el => setVideoRef(participant.id, el)"
                autoplay
                playsinline
                class="video-element"
              ></video>
              <div v-else class="no-video">
                <DingtalkAvatar
                  :src="participant.avatar"
                  :name="participant.name"
                  :user-id="participant.id"
                  :size="60"
                  shape="square"
                />
              </div>
            </div>
            <!-- 参会者信息 -->
            <div class="participant-info">
              <span class="participant-name">{{ participant.name }}</span>
              <el-tag v-if="participant.role === 'HOST'" size="small" type="warning">主持人</el-tag>
            </div>
            <!-- 状态指示 -->
            <div class="status-indicators">
              <span v-if="!participant.hasVideo" class="status-icon video-off">
                <span class="material-icons-outlined">videocam_off</span>
              </span>
              <span v-if="!participant.hasAudio" class="status-icon audio-off">
                <span class="material-icons-outlined">mic_off</span>
              </span>
              <span v-if="participant.isSpeaking" class="speaking-indicator"></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部控制栏 -->
      <div class="room-controls">
        <div class="controls-left">
          <!-- 麦克风开关 -->
          <el-tooltip :content="isAudioEnabled ? '静音' : '取消静音'" placement="top">
            <el-button
              :type="isAudioEnabled ? 'default' : 'danger'"
              :icon="isAudioEnabled ? Mic : MicOff"
              circle
              size="large"
              @click="toggleAudio"
            />
          </el-tooltip>

          <!-- 摄像头开关 -->
          <el-tooltip :content="isVideoEnabled ? '关闭摄像头' : '开启摄像头'" placement="top">
            <el-button
              :type="isVideoEnabled ? 'default' : 'danger'"
              :icon="isVideoEnabled ? Videocam : VideocamOff"
              circle
              size="large"
              @click="toggleVideo"
            />
          </el-tooltip>

          <!-- 屏幕共享 -->
          <el-tooltip :content="isScreenSharing ? '停止共享' : '共享屏幕'" placement="top">
            <el-button
              :type="isScreenSharing ? 'primary' : 'default'"
              :icon="ScreenShare"
              circle
              size="large"
              @click="toggleScreenShare"
            />
          </el-tooltip>

          <!-- 更多选项 -->
          <el-dropdown trigger="click" @command="handleMoreCommand">
            <el-button :icon="MoreHoriz" circle size="large" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="participants">
                  <span class="material-icons-outlined">group</span>
                  管理参会者
                </el-dropdown-item>
                <el-dropdown-item command="chat">
                  <span class="material-icons-outlined">chat</span>
                  会议聊天
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <span class="material-icons-outlined">settings</span>
                  会议设置
                </el-dropdown-item>
                <el-dropdown-item command="record">
                  <span class="material-icons-outlined">fiber_manual_record</span>
                  录制会议
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <div class="controls-center">
          <!-- 离开/结束会议 -->
          <el-button
            :type="isHost ? 'danger' : 'default'"
            :icon="PhoneDisabled"
            @click="handleLeave"
          >
            {{ isHost ? '结束会议' : '离开会议' }}
          </el-button>
        </div>

        <div class="controls-right">
          <!-- 切换布局 -->
          <el-button-group>
            <el-button
              :type="gridLayout === 'grid' ? 'primary' : ''"
              @click="gridLayout = 'grid'"
            >
              <span class="material-icons-outlined">grid_view</span>
            </el-button>
            <el-button
              :type="gridLayout === 'strip' ? 'primary' : ''"
              @click="gridLayout = 'strip'"
            >
              <span class="material-icons-outlined">view_stream</span>
            </el-button>
          </el-button-group>
        </div>
      </div>

      <!-- 参会者侧边栏 -->
      <div v-if="showParticipants" class="participants-sidebar">
        <div class="sidebar-header">
          <span>参会者 ({{ participants.length }})</span>
          <el-button :icon="Close" circle size="small" @click="showParticipants = false" />
        </div>
        <div class="sidebar-content">
          <div
            v-for="participant in participants"
            :key="participant.id"
            class="participant-row"
          >
            <DingtalkAvatar
              :src="participant.avatar"
              :name="participant.name"
              :user-id="participant.id"
              :size="36"
              shape="square"
            />
            <span class="participant-row-name">{{ participant.name }}</span>
            <div class="participant-actions">
              <el-tooltip v-if="!participant.isLocal" content="静音" placement="top">
                <el-button
                  :icon="participant.hasAudio ? Mic : MicOff"
                  size="small"
                  circle
                  @click="toggleParticipantAudio(participant)"
                />
              </el-tooltip>
              <el-tooltip v-if="isHost && !participant.isHost" content="移除" placement="top">
                <el-button
                  :icon="PersonRemove"
                  size="small"
                  type="danger"
                  circle
                  @click="removeParticipant(participant)"
                />
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Mic,
  MicOff,
  Videocam,
  VideocamOff,
  ScreenShare,
  MoreHoriz,
  PhoneDisabled,
  MoreFilled,
  Close,
  PersonRemove
} from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  meeting: {
    type: Object,
    default: null
  },
  currentUser: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'ended', 'participant-update'])

// 本地媒体
const localVideoRef = ref(null)
const localStream = ref(null)
const isVideoEnabled = ref(true)
const isAudioEnabled = ref(true)
const isScreenSharing = ref(false)
const isLocalSpeaking = ref(false)
const isUnmounted = ref(false) // 标记组件是否已卸载

// 参会者
const participants = ref([])
const activeParticipantId = ref(null)
const showParticipants = ref(false)

// 会议功能
const showChat = ref(false)
const showSettings = ref(false)
const isRecording = ref(false)
let mediaRecorder = null
let recordedChunks = []

// 开始录制
const startRecording = () => {
  try {
    const stream = new MediaStream([...localStream.value.getTracks()])
    mediaRecorder = new MediaRecorder(stream, { mimeType: 'video/webm' })
    recordedChunks = []

    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        recordedChunks.push(event.data)
      }
    }

    mediaRecorder.onstop = () => {
      const blob = new Blob(recordedChunks, { type: 'video/webm' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `meeting-recording-${Date.now()}.webm`
      a.click()
      URL.revokeObjectURL(url)
    }

    mediaRecorder.start()
    isRecording.value = true
    ElMessage.success('开始录制会议')
  } catch (error) {
    ElMessage.error('录制失败，请稍后重试')
  }
}

// 停止录制
const stopRecording = () => {
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
    isRecording.value = false
    ElMessage.success('录制已保存')
  }
}

// 布局
const gridLayout = ref('grid')

// 远程视频引用
const videoRefs = new Map()
const setVideoRef = (id, el) => {
  if (el) videoRefs.set(id, el)
}

// 计算属性
const isHost = computed(() => {
  return props.meeting?.hostId === props.currentUser?.id
})

const remoteParticipants = computed(() => {
  return participants.value.filter(p => !p.isLocal)
})

const gridSize = computed(() => {
  const count = participants.value.length
  if (count <= 1) return 1
  if (count <= 4) return 4
  if (count <= 9) return 9
  return 16
})

// 会议时长
const meetingDuration = ref('00:00:00')
let durationTimer = null

// 初始化本地媒体
const initLocalMedia = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { width: 1280, height: 720 },
      audio: true
    })
    localStream.value = stream

    await nextTick()
    if (isUnmounted.value) return
    if (localVideoRef.value) {
      localVideoRef.value.srcObject = stream
    }

    // 监听音频变化实现说话检测
    const audioContext = new AudioContext()
    const analyser = audioContext.createAnalyser()
    const source = audioContext.createMediaStreamSource(stream)
    source.connect(analyser)

    const dataArray = new Uint8Array(analyser.frequencyBinCount)
    const checkSpeaking = () => {
      analyser.getByteFrequencyData(dataArray)
      const average = dataArray.reduce((a, b) => a + b) / dataArray.length
      isLocalSpeaking.value = average > 30
      requestAnimationFrame(checkSpeaking)
    }
    checkSpeaking()

    startDurationTimer()
  } catch (error) {
    console.error('获取本地媒体失败:', error)
    ElMessage.error('无法访问摄像头和麦克风')
  }
}

// 停止本地媒体
const stopLocalMedia = () => {
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => track.stop())
    localStream.value = null
  }
  if (durationTimer) {
    clearInterval(durationTimer)
    durationTimer = null
  }
}

// 开始时长计时
const startDurationTimer = () => {
  let seconds = 0
  durationTimer = setInterval(() => {
    seconds++
    const h = Math.floor(seconds / 3600).toString().padStart(2, '0')
    const m = Math.floor((seconds % 3600) / 60).toString().padStart(2, '0')
    const s = (seconds % 60).toString().padStart(2, '0')
    meetingDuration.value = `${h}:${m}:${s}`
  }, 1000)
}

// 切换麦克风
const toggleAudio = () => {
  if (localStream.value) {
    const audioTrack = localStream.value.getAudioTracks()[0]
    if (audioTrack) {
      audioTrack.enabled = !audioTrack.enabled
      isAudioEnabled.value = audioTrack.enabled
    }
  }
}

// 切换摄像头
const toggleVideo = () => {
  if (localStream.value) {
    const videoTrack = localStream.value.getVideoTracks()[0]
    if (videoTrack) {
      videoTrack.enabled = !videoTrack.enabled
      isVideoEnabled.value = videoTrack.enabled
    }
  }
}

// 屏幕共享
const toggleScreenShare = async () => {
  if (isScreenSharing.value) {
    // 停止共享
    stopLocalMedia()
    await initLocalMedia()
    isScreenSharing.value = false
  } else {
    try {
      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: { cursor: 'always' },
        audio: false
      })
      localStream.value = stream
      if (localVideoRef.value) {
        localVideoRef.value.srcObject = stream
      }
      isScreenSharing.value = true
      isVideoEnabled.value = true

      // 监听停止共享事件
      stream.getVideoTracks()[0].onended = () => {
        toggleScreenShare()
      }
    } catch (error) {
      ElMessage.error('屏幕共享失败')
    }
  }
}

// 设置活跃参会者
const handleSetActive = (id) => {
  activeParticipantId.value = id
}

// 切换参会者音频
const toggleParticipantAudio = (participant) => {
  emit('participant-update', { ...participant, hasAudio: !participant.hasAudio })
}

// 移除参会者
const removeParticipant = async (participant) => {
  try {
    await ElMessageBox.confirm(
      `确定要将${participant.name}移出会议吗？`,
      '移除参会者',
      {
        confirmButtonText: '移除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    emit('remove-participant', participant)
  } catch {
    // 取消
  }
}

// 处理更多命令
const handleMoreCommand = (command) => {
  switch (command) {
    case 'participants':
      showParticipants.value = !showParticipants.value
      break
    case 'chat':
      showChat.value = !showChat.value
      break
    case 'settings':
      showSettings.value = !showSettings.value
      break
    case 'record':
      if (isRecording.value) {
        stopRecording()
      } else {
        startRecording()
      }
      break
  }
}

// 离开/结束会议
const handleLeave = async () => {
  const action = isHost.value ? '结束' : '离开'
  try {
    await ElMessageBox.confirm(
      isHost.value ? '确定要结束会议吗？所有参会者将被移出。' : '确定要离开会议吗？',
      `${action}会议`,
      {
        confirmButtonText: isHost.value ? '结束会议' : '离开',
        cancelButtonText: '取消',
        type: isHost.value ? 'warning' : 'info'
      }
    )

    stopLocalMedia()
    emit('ended', { endedByHost: isHost.value })
  } catch {
    // 取消
  }
}

// 添加参会者
const addParticipant = (participant) => {
  const exists = participants.value.find(p => p.id === participant.id)
  if (!exists) {
    participants.value.push({
      ...participant,
      isLocal: participant.id === props.currentUser?.id,
      stream: null
    })
  }
}

// 移除参会者
const removeParticipantById = (id) => {
  const index = participants.value.findIndex(p => p.id === id)
  if (index > -1) {
    const participant = participants.value[index]
    if (participant.stream) {
      participant.stream.getTracks().forEach(track => track.stop())
    }
    participants.value.splice(index, 1)
  }
}

// 更新参会者流
const updateParticipantStream = (id, stream) => {
  const participant = participants.value.find(p => p.id === id)
  if (participant) {
    participant.stream = stream
    participant.hasVideo = stream.getVideoTracks().length > 0
    participant.hasAudio = stream.getAudioTracks().length > 0

    // 绑定到 video 元素
    nextTick(() => {
      if (isUnmounted.value) return
      const videoEl = videoRefs.get(id)
      if (videoEl && stream) {
        videoEl.srcObject = stream
      }
    })
  }
}

// 监听可见状态
watch(() => props.visible, (visible) => {
  if (visible) {
    // 添加本地用户
    participants.value = [{
      id: props.currentUser?.id,
      name: props.currentUser?.nickName || '我',
      avatar: props.currentUser?.avatar,
      isLocal: true,
      isHost: isHost.value,
      hasVideo: true,
      hasAudio: true,
      isSpeaking: false,
      stream: null
    }]
    initLocalMedia()
  } else {
    stopLocalMedia()
  }
})

// 暴露方法给父组件
defineExpose({
  addParticipant,
  removeParticipantById,
  updateParticipantStream,
  toggleAudio,
  toggleVideo
})

onUnmounted(() => {
  isUnmounted.value = true // 标记组件已卸载
  stopLocalMedia()
  // 清理定时器
  if (durationTimer) {
    clearInterval(durationTimer)
    durationTimer = null
  }
  // 清理媒体录制器
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
    mediaRecorder = null
  }
  // 清理 video 引用
  videoRefs.clear()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.video-meeting-room {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #000;
  z-index: 9999;
  display: flex;
  flex-direction: column;
}

.room-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(10px);

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .meeting-title {
      font-size: 16px;
      font-weight: 500;
      color: #fff;
    }

    .meeting-time {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.7);
      font-family: monospace;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;

    .participant-count {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 6px 12px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: var(--dt-radius-xl);
      font-size: 13px;
      color: #fff;
    }
  }
}

.room-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  overflow: hidden;
}

.video-grid {
  display: grid;
  gap: 12px;
  width: 100%;
  height: 100%;
  max-width: 1400px;

  &.grid-1 {
    grid-template-columns: 1fr;
  }

  &.grid-4 {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(2, 1fr);
  }

  &.grid-9 {
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: repeat(3, 1fr);
  }

  &.grid-16 {
    grid-template-columns: repeat(4, 1fr);
    grid-template-rows: repeat(4, 1fr);
  }
}

.video-cell {
  position: relative;
  background: #1a1a1a;
  border-radius: var(--dt-radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border: 2px solid rgba(0, 137, 255, 0.5);
  }

  &.is-active {
    border: 2px solid var(--dt-brand-color);
  }

  &.is-speaking::before {
    content: '';
    position: absolute;
    top: 12px;
    left: 12px;
    width: 10px;
    height: 10px;
    background: #10b981;
    border-radius: 50%;
    animation: pulse-speaking 1s infinite;
    z-index: 2;
  }

  @keyframes pulse-speaking {
    0%, 100% {
      transform: scale(1);
      opacity: 1;
    }
    50% {
      transform: scale(1.2);
      opacity: 0.8;
    }
  }

  &.is-local {
    transform: scaleX(-1);
  }

  .video-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #1a1a1a;

    .video-element {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .no-video {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      color: rgba(255, 255, 255, 0.3);

      .material-icons-outlined {
        font-size: 48px;
      }
    }
  }

  .participant-info {
    position: absolute;
    bottom: 12px;
    left: 12px;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 4px 10px;
    background: rgba(0, 0, 0, 0.6);
    border-radius: var(--dt-radius-xl);

    .participant-name {
      font-size: 13px;
      color: #fff;
    }
  }

  .status-indicators {
    position: absolute;
    top: 12px;
    right: 12px;
    display: flex;
    gap: 4px;

    .status-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 24px;
      height: 24px;
      background: rgba(0, 0, 0, 0.6);
      border-radius: 50%;
      color: #fff;

      .material-icons-outlined {
        font-size: 14px;
      }

      &.video-off {
        color: #ef4444;
      }

      &.audio-off {
        color: #f59e0b;
      }
    }

    .speaking-indicator {
      width: 10px;
      height: 10px;
      background: #10b981;
      border-radius: 50%;
      animation: pulse-speaking 1s infinite;
    }
  }
}

.room-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(10px);

  .controls-left,
  .controls-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .controls-center {
    display: flex;
    align-items: center;
  }

  .el-button {
    background: rgba(255, 255, 255, 0.1);
    border: none;
    color: #fff;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
    }

    &.el-button--danger {
      background: #ef4444;

      &:hover {
        background: #dc2626;
      }
    }

    &.el-button--primary {
      background: var(--dt-brand-color);

      &:hover {
        background: #1557b0;
      }
    }
  }
}

.participants-sidebar {
  position: absolute;
  top: 56px;
  right: 0;
  width: 280px;
  height: calc(100% - 56px - 80px);
  background: rgba(30, 30, 30, 0.95);
  backdrop-filter: blur(10px);
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  animation: slide-in 0.3s ease-out;

  @keyframes slide-in {
    from {
      transform: translateX(100%);
    }
    to {
      transform: translateX(0);
    }
  }

  .sidebar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    font-size: 14px;
    font-weight: 500;
    color: #fff;
  }

  .sidebar-content {
    flex: 1;
    overflow-y: auto;
    padding: 8px;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.2);
      border-radius: var(--dt-radius-sm);
    }
  }

  .participant-row {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px;
    border-radius: var(--dt-radius-md);
    transition: background 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.05);
    }

    .participant-row-name {
      flex: 1;
      font-size: 13px;
      color: #fff;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .participant-actions {
      display: flex;
      gap: 4px;
      opacity: 0;
      transition: opacity 0.2s;

      .el-button {
        background: rgba(255, 255, 255, 0.1);
        border: none;
        color: #fff;

        &:hover {
          background: rgba(255, 255, 255, 0.2);
        }
      }
    }

    &:hover .participant-actions {
      opacity: 1;
    }
  }
}
</style>
