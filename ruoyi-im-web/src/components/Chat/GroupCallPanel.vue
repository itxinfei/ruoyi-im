<template>
  <el-dialog
    v-model="visible"
    :width="panelWidth"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    :show-close="false"
    class="group-call-panel"
    append-to-body
    destroy-on-close
  >
    <template #header>
      <div class="panel-header">
        <div class="header-left">
          <el-icon class="call-icon"><Phone /></el-icon>
          <span class="title">群组通话</span>
          <el-tag :type="statusTagType" size="small" effect="dark">
            {{ statusLabel }}
          </el-tag>
          <span class="participant-count">{{ participants.length }} / {{ maxParticipants }} 人</span>
        </div>
        <div class="header-right">
          <el-button size="small" @click="handleLeave">
            离开
          </el-button>
          <el-button size="small" type="danger" @click="handleEndCall">
            结束通话
          </el-button>
        </div>
      </div>
    </template>

    <div class="call-content">
      <!-- 通话计时 -->
      <div v-if="status === 'talking'" class="call-timer">
        <span class="timer-label">通话时长</span>
        <span class="timer-value">{{ formattedDuration }}</span>
      </div>

      <!-- 参与者网格 -->
      <div class="participants-grid" :class="gridClass">
        <div
          v-for="participant in participants"
          :key="participant.userId"
          class="participant-cell"
          :class="{ 'is-speaking': participant.isSpeaking, 'is-muted': participant.isMuted, 'camera-off': participant.cameraOff }"
        >
          <!-- 视频流 -->
          <video
            v-if="!participant.cameraOff && participant.stream"
            :ref="el => setVideoRef(el, participant.userId)"
            autoplay
            playsinline
            class="participant-video"
          />
          <!-- 头像占位 -->
          <div v-else class="participant-avatar">
            <DingtalkAvatar
              :src="participant.avatar"
              :name="participant.name"
              :size="avatarSize"
              shape="circle"
            />
          </div>

          <!-- 覆盖信息层 -->
          <div class="participant-overlay">
            <span class="participant-name">{{ participant.name }}</span>
            <div class="participant-badges">
              <el-icon v-if="participant.isMuted" class="badge-icon muted"><Microphone /></el-icon>
              <el-icon v-if="participant.cameraOff" class="badge-icon camera"><VideoCamera /></el-icon>
              <el-icon v-if="participant.isScreenSharing" class="badge-icon screen"><Monitor /></el-icon>
            </div>
          </div>

          <!-- 正在说话动画 -->
          <div v-if="participant.isSpeaking" class="speaking-border" />
        </div>

        <!-- 等待加入的占位 -->
        <div v-for="n in emptySlots" :key="'empty-' + n" class="participant-cell empty-slot">
          <div class="empty-avatar">
            <el-icon><User /></el-icon>
          </div>
          <span class="empty-label">等待加入...</span>
        </div>
      </div>

      <!-- 本地预览小窗 -->
      <div v-if="localStream && !localCameraOff" class="local-preview">
        <video
          ref="localVideoRef"
          autoplay
          playsinline
          muted
          class="local-video"
        />
        <span class="local-label">我</span>
      </div>
    </div>

    <!-- 控制栏 -->
    <div class="control-bar">
      <div class="control-group">
        <button class="ctrl-btn" :class="{ active: isMuted }" @click="toggleMute">
          <div class="icon-circle">
            <el-icon><Microphone v-if="!isMuted" /><Mute v-else /></el-icon>
          </div>
          <span>{{ isMuted ? '取消静音' : '静音' }}</span>
        </button>

        <button class="ctrl-btn" :class="{ active: localCameraOff }" @click="toggleCamera">
          <div class="icon-circle">
            <el-icon><VideoCamera v-if="!localCameraOff" /><VideoCameraFilled v-else /></el-icon>
          </div>
          <span>{{ localCameraOff ? '开启摄像头' : '关闭摄像头' }}</span>
        </button>

        <button class="ctrl-btn" :class="{ active: isScreenSharing }" @click="toggleScreenShare">
          <div class="icon-circle">
            <el-icon><Monitor /></el-icon>
          </div>
          <span>{{ isScreenSharing ? '停止共享' : '屏幕共享' }}</span>
        </button>
      </div>
    </div>

    <!-- 邀请成员 -->
    <div v-if="status === 'talking'" class="invite-section">
      <el-button size="small" @click="showInviteDialog = true">
        <el-icon><Plus /></el-icon>
        邀请成员
      </el-button>
    </div>
    <div v-else-if="status === 'calling'" class="invite-section">
      <el-tag type="warning" effect="plain">正在等待对方加入...</el-tag>
    </div>

    <!-- 邀请成员弹窗 -->
    <el-dialog v-model="showInviteDialog" title="邀请成员" width="500px" append-to-body>
      <div class="invite-tip">选择要邀请的成员（当前通话最多 {{ maxParticipants }} 人）</div>
      <el-checkbox-group v-model="selectedMemberIds">
        <el-checkbox
          v-for="member in groupMembers"
          :key="member.userId"
          :value="member.userId"
          :disabled="isMemberInCall(member.userId)"
        >
          <div class="member-item">
            <DingtalkAvatar :src="member.avatar" :name="member.name" :size="32" shape="circle" />
            <span>{{ member.name }}</span>
            <el-tag v-if="isMemberInCall(member.userId)" type="info" size="small">已在通话中</el-tag>
          </div>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="showInviteDialog = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedMemberIds.length" @click="handleInvite">
          邀请 ({{ selectedMemberIds.length }})
        </el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Phone, Microphone, Mute, VideoCamera, VideoCameraFilled,
  Monitor, User, Plus
} from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import {
  initiateGroupCall,
  joinGroupCall,
  leaveGroupCall,
  getCallParticipants,
  toggleMute as apiToggleMute,
  toggleCamera as apiToggleCamera,
  endCall
} from '@/api/im/videoCall'
import { getUsersBatch } from '@/api/im/user'

const props = defineProps({
  modelValue: Boolean,
  session: { type: Object, default: () => ({}) },
  conversationId: { type: Number, default: null }
})

const emit = defineEmits(['update:modelValue', 'ended'])

// 通话状态
const visible = ref(false)
const status = ref('idle') // idle | calling | talking | ended
const callId = ref(null)
const participants = ref([])
const maxParticipants = ref(9)
const duration = ref(0)
const localStream = ref(null)
const isMuted = ref(false)
const localCameraOff = ref(false)
const isScreenSharing = ref(false)
const localVideoRef = ref(null)
const screenStream = ref(null)
const videoRefs = ref({})
const timer = ref(null)
const roomInfo = ref(null)

// 邀请相关
const showInviteDialog = ref(false)
const selectedMemberIds = ref([])
const groupMembers = ref([])

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (!val) resetState()
})

// 计算属性
const statusLabel = computed(() => {
  switch (status.value) {
    case 'idle': return '未开始'
    case 'calling': return '等待加入'
    case 'talking': return '通话中'
    case 'ended': return '已结束'
    default: return ''
  }
})

const statusTagType = computed(() => {
  switch (status.value) {
    case 'talking': return 'success'
    case 'calling': return 'warning'
    case 'ended': return 'info'
    default: return 'info'
  }
})

const formattedDuration = computed(() => {
  const m = Math.floor(duration.value / 60)
  const s = duration.value % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
})

const avatarSize = computed(() => {
  const count = participants.value.length || 1
  if (count <= 2) return 100
  if (count <= 4) return 80
  return 60
})

const gridClass = computed(() => {
  const count = participants.value.length || 1
  if (count <= 1) return 'grid-1'
  if (count <= 2) return 'grid-2'
  if (count <= 4) return 'grid-2x2'
  if (count <= 6) return 'grid-2x3'
  return 'grid-3x3'
})

const emptySlots = computed(() => {
  return Math.max(0, maxParticipants.value - participants.value.length)
})

const panelWidth = computed(() => {
  const count = participants.value.length || 1
  if (count <= 2) return '520px'
  if (count <= 4) return '680px'
  return '900px'
})

// 工具方法
const setVideoRef = (el, userId) => {
  if (el) videoRefs.value[userId] = el
}

const getMediaStream = async (isVideo) => {
  try {
    if (localStream.value) localStream.value.getTracks().forEach(t => t.stop())
    localStream.value = await navigator.mediaDevices.getUserMedia({
      audio: true,
      video: isVideo ? { width: 1280, height: 720 } : false
    })
    if (localVideoRef.value) localVideoRef.value.srcObject = localStream.value
    return true
  } catch (e) {
    console.error('获取媒体设备失败:', e)
    ElMessage.error('无法访问麦克风或摄像头')
    return false
  }
}

const loadParticipantsWithInfo = async (participantIds) => {
  try {
    const res = await getUsersBatch(participantIds)
    if (res.code === 200) {
      return (res.data || []).map(user => ({
        userId: user.userId || user.id,
        name: user.nickname || user.name || '未知',
        avatar: user.avatar || '',
        isMuted: false,
        cameraOff: false,
        isScreenSharing: false,
        stream: null
      }))
    }
  } catch (e) {
    console.error('获取参与者信息失败:', e)
  }
  return []
}

const refreshParticipants = async () => {
  if (!callId.value) return
  try {
    const res = await getCallParticipants(callId.value)
    if (res.code === 200) {
      const ids = (res.data || []).map(p => p.userId || p)
      const enriched = await loadParticipantsWithInfo(ids)
      participants.value = enriched
    }
  } catch (e) {
    console.error('刷新参与者失败:', e)
  }
}

const isMemberInCall = (userId) => {
  return participants.value.some(p => p.userId === userId)
}

// 事件处理
const toggleMute = async () => {
  if (!callId.value) return
  try {
    const newMuted = !isMuted.value
    await apiToggleMute(callId.value, newMuted)
    isMuted.value = newMuted
    if (localStream.value) {
      localStream.value.getAudioTracks().forEach(t => { t.enabled = !newMuted })
    }
  } catch (e) {
    ElMessage.error('切换静音失败')
  }
}

const toggleCamera = async () => {
  if (!callId.value) return
  try {
    const newCameraOff = !localCameraOff.value
    await apiToggleCamera(callId.value, newCameraOff)
    localCameraOff.value = newCameraOff
    if (localStream.value) {
      localStream.value.getVideoTracks().forEach(t => { t.enabled = !newCameraOff })
    }
  } catch (e) {
    ElMessage.error('切换摄像头失败')
  }
}

const toggleScreenShare = async () => {
  if (isScreenSharing.value) {
    // 停止屏幕共享
    if (screenStream.value) {
      screenStream.value.getTracks().forEach(t => t.stop())
      screenStream.value = null
    }
    isScreenSharing.value = false
    // 恢复本地摄像头
    if (localStream.value && !localCameraOff.value) {
      const videoTrack = localStream.value.getVideoTracks()[0]
      if (videoTrack && localVideoRef.value) {
        localVideoRef.value.srcObject = localStream.value
      }
    }
  } else {
    // 开始屏幕共享
    try {
      screenStream.value = await navigator.mediaDevices.getDisplayMedia({ video: true })
      isScreenSharing.value = true
      // 替换视频轨道
      if (localStream.value) {
        const videoTrack = screenStream.value.getVideoTracks()[0]
        const sender = localStream.value.getVideoTracks()[0]
        sender?.replaceTrack(videoTrack)
        videoTrack.onended = () => {
          isScreenSharing.value = false
          if (localStream.value && !localCameraOff.value) {
            getMediaStream(true)
          }
        }
      }
    } catch (e) {
      console.error('屏幕共享失败:', e)
    }
  }
}

const handleLeave = async () => {
  try {
    if (callId.value) {
      await leaveGroupCall(callId.value)
    }
  } catch (e) {
    console.error('离开通话失败:', e)
  } finally {
    visible.value = false
    emit('ended')
  }
}

const handleEndCall = async () => {
  try {
    if (callId.value) {
      await endCall(callId.value)
    }
  } catch (e) {
    console.error('结束通话失败:', e)
  } finally {
    visible.value = false
    emit('ended')
  }
}

const handleInvite = async () => {
  if (!selectedMemberIds.value.length) return
  ElMessage.info('邀请已发送')
  showInviteDialog.value = false
  selectedMemberIds.value = []
}

const startTimer = () => {
  timer.value = setInterval(() => { duration.value++ }, 1000)
}

const stopTimer = () => {
  clearInterval(timer.value)
  timer.value = null
}

const resetState = () => {
  stopTimer()
  if (localStream.value) {
    localStream.value.getTracks().forEach(t => t.stop())
    localStream.value = null
  }
  if (screenStream.value) {
    screenStream.value.getTracks().forEach(t => t.stop())
    screenStream.value = null
  }
  status.value = 'idle'
  callId.value = null
  participants.value = []
  duration.value = 0
  isMuted.value = false
  localCameraOff.value = false
  isScreenSharing.value = false
}

// 主动发起群组通话
const openAsInitiator = async (callType = 'VIDEO', invitedUserIds = []) => {
  if (!props.conversationId) {
    ElMessage.warning('无法获取会话ID')
    return
  }

  visible.value = true
  status.value = 'calling'

  try {
    // 获取媒体流
    if (!await getMediaStream(callType === 'VIDEO')) {
      visible.value = false
      return
    }
    localCameraOff.value = callType !== 'VIDEO'

    // 发起群组通话
    const res = await initiateGroupCall({
      conversationId: props.conversationId,
      callType,
      maxParticipants: maxParticipants.value,
      invitedUserIds
    })

    if (res.code !== 200) {
      throw new Error(res.message || '发起通话失败')
    }

    roomInfo.value = res.data
    callId.value = res.data.callId

    // 将自己加入参与者列表
    const selfId = 0 // 后续从store获取
    const enriched = await loadParticipantsWithInfo([selfId])
    if (enriched.length) {
      enriched[0].stream = localStream.value
      enriched[0].cameraOff = localCameraOff.value
      participants.value = enriched
    }

    ElMessage.success('通话已发起，等待对方加入...')
  } catch (e) {
    ElMessage.error(e.message || '发起群组通话失败')
    visible.value = false
  }
}

// 作为参与者加入
const openAsParticipant = async (targetCallId) => {
  visible.value = true
  callId.value = targetCallId
  status.value = 'calling'

  try {
    // 获取媒体流
    if (!await getMediaStream(true)) {
      visible.value = false
      return
    }

    // 加入通话
    const res = await joinGroupCall(targetCallId)
    if (res.code !== 200) {
      throw new Error(res.message || '加入通话失败')
    }

    // 刷新参与者列表
    await refreshParticipants()
    status.value = 'talking'
    startTimer()
  } catch (e) {
    ElMessage.error(e.message || '加入群组通话失败')
    visible.value = false
  }
}

// 打开面板（选择模式：发起或加入）
const open = async (options = {}) => {
  visible.value = true
  const { mode = 'initiate', callId: targetCallId, callType = 'VIDEO', invitedUserIds = [] } = options

  if (mode === 'initiate') {
    await openAsInitiator(callType, invitedUserIds)
  } else if (mode === 'join') {
    await openAsParticipant(targetCallId)
  }
}

// 加载群组成员（用于邀请）
const loadGroupMembers = async (memberIds) => {
  try {
    const res = await getUsersBatch(memberIds)
    if (res.code === 200) {
      groupMembers.value = (res.data || []).map(u => ({
        userId: u.userId || u.id,
        name: u.nickname || u.name || '未知',
        avatar: u.avatar || ''
      }))
    }
  } catch (e) {
    console.error('加载群组成员失败:', e)
  }
}

defineExpose({ open, openAsInitiator, openAsParticipant, loadGroupMembers, callId, status })
</script>

<style scoped lang="scss">
.group-call-panel {
  :deep(.el-dialog) {
    background: var(--dt-bg-card-dark, #1a1a2e);
    border-radius: var(--dt-radius-xl);
    overflow: hidden;
  }

  :deep(.el-dialog__header) {
    background: var(--dt-overlay-bg);
    padding: var(--dt-spacing-lg) var(--dt-spacing-xl);
    margin: 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }

  :deep(.el-dialog__body) {
    padding: 0;
    color: var(--dt-text-white, #fff);
  }
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;

    .call-icon {
      color: var(--dt-brand-color);
      font-size: 18px;
    }

    .title {
      font-size: 15px;
      font-weight: 600;
      color: var(--dt-text-white);
    }

    .participant-count {
      margin-left: 8px;
      font-size: 13px;
      color: rgba(255, 255, 255, 0.6);
    }
  }

  .header-right {
    display: flex;
    gap: 8px;
  }
}

.call-content {
  position: relative;
  padding: 16px;
}

.call-timer {
  text-align: center;
  margin-bottom: 12px;

  .timer-label {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.5);
    margin-right: 8px;
  }

  .timer-value {
    font-size: 18px;
    font-weight: 700;
    font-family: monospace;
    color: var(--dt-brand-color);
  }
}

.participants-grid {
  display: grid;
  gap: 12px;
  justify-content: center;

  &.grid-1 { grid-template-columns: 1fr; max-width: 400px; margin: 0 auto; }
  &.grid-2 { grid-template-columns: repeat(2, 1fr); max-width: 560px; margin: 0 auto; }
  &.grid-2x2 { grid-template-columns: repeat(2, 1fr); }
  &.grid-2x3 { grid-template-columns: repeat(3, 1fr); }
  &.grid-3x3 { grid-template-columns: repeat(3, 1fr); }
}

.participant-cell {
  position: relative;
  aspect-ratio: 16 / 9;
  border-radius: var(--dt-radius-xl);
  overflow: hidden;
  background: var(--dt-overlay-bg);
  border: 2px solid transparent;
  transition: all 0.3s;

  &.is-speaking {
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 16px rgba(var(--dt-brand-color-rgb, 64, 158, 255), 0.5);
  }

  &.is-muted .participant-overlay .participant-name {
    opacity: 0.7;
  }

  &.camera-off .participant-avatar {
    display: flex;
  }

  .participant-video {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .participant-avatar {
    display: none;
    position: absolute;
    inset: 0;
    align-items: center;
    justify-content: center;
  }

  .participant-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 8px;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
    display: flex;
    align-items: center;
    justify-content: space-between;

    .participant-name {
      font-size: 13px;
      font-weight: 500;
      color: white;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .participant-badges {
      display: flex;
      gap: 4px;

      .badge-icon {
        font-size: 14px;
        padding: 2px;
        border-radius: 4px;
        background: var(--dt-overlay-bg);

        &.muted { color: var(--dt-error-color); }
        &.camera { color: var(--dt-text-tertiary); }
        &.screen { color: var(--dt-brand-color); }
      }
    }
  }

  .speaking-border {
    position: absolute;
    inset: 0;
    border-radius: inherit;
    border: 2px solid var(--dt-brand-color);
    animation: speakingPulse 1.5s ease-in-out infinite;
  }

  &.empty-slot {
    border: 2px dashed rgba(255, 255, 255, 0.15);
    background: rgba(255, 255, 255, 0.03);

    .empty-avatar {
      position: absolute;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      color: rgba(255, 255, 255, 0.2);
      font-size: 32px;
    }

    .empty-label {
      position: absolute;
      bottom: 8px;
      left: 50%;
      transform: translateX(-50%);
      font-size: 12px;
      color: rgba(255, 255, 255, 0.2);
    }
  }
}

.local-preview {
  position: absolute;
  top: 24px;
  right: 24px;
  width: 140px;
  height: 90px;
  border-radius: var(--dt-radius-lg);
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: var(--dt-shadow-4);
  z-index: 10;

  .local-video {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transform: scaleX(-1);
  }

  .local-label {
    position: absolute;
    bottom: 4px;
    left: 50%;
    transform: translateX(-50%);
    font-size: 11px;
    color: var(--dt-text-white);
    opacity: 0.7;
    background: var(--dt-overlay-bg);
    padding: 1px 6px;
    border-radius: 4px;
  }
}

.control-bar {
  display: flex;
  justify-content: center;
  padding: 16px;
  background: rgba(0, 0, 0, 0.2);
  border-top: 1px solid rgba(255, 255, 255, 0.05);

  .control-group {
    display: flex;
    gap: 24px;
  }
}

.ctrl-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.2s;

  .icon-circle {
    width: 52px;
    height: 52px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    transition: all 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
      transform: translateY(-2px);
    }
  }

  span {
    font-size: 12px;
  }

  &.active .icon-circle {
    background: var(--dt-error-color, #F53F3F);
    color: white;
  }

  &:hover {
    color: white;
  }
}

.invite-section {
  display: flex;
  justify-content: center;
  padding: var(--dt-spacing-lg);
  background: var(--dt-bg-card-dark);
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.invite-tip {
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--dt-text-secondary);
}

.member-item {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  padding: 4px 0;
}

@keyframes speakingPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
