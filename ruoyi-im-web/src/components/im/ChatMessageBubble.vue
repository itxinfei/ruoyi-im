<template>
  <div
    class="message-row"
    :class="{
      'is-consecutive': isGrouped,
      'is-sent': isMe,
      'is-selected': isSelected
    }"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <!-- 头像区：连续消息时显示时间 -->
    <div class="avatar-zone" v-if="!isGrouped">
      <img
        v-if="avatarSrc"
        :src="avatarSrc"
        class="avatar"
        @click="$emit('show-user', message.senderId)"
      />
      <div v-else class="avatar-placeholder">
        {{ senderLabel?.charAt(0) || '?' }}
      </div>
    </div>
    <!-- 连续消息显示时间戳 -->
    <div v-else class="time-zone">
      <span class="time-text">{{ hoverTime }}</span>
    </div>

    <!-- 消息内容区 -->
    <div class="content-zone">
      <!-- 元数据：发送者 + 时间 (仅非连续消息) -->
      <div v-if="!isGrouped" class="meta-row">
        <span class="sender-name">{{ senderLabel }}</span>
        <span class="timestamp">{{ timelineLabel }}</span>
      </div>

      <!-- 消息体 -->
      <div class="bubble-wrapper">
        <!-- 文本消息 -->
        <div v-if="messageType === 'TEXT'" class="bubble bubble-text">
          {{ message.content }}
        </div>

        <!-- 图片消息 -->
        <div v-else-if="messageType === 'IMAGE'" class="bubble bubble-image">
          <el-image
            :src="mediaUrl"
            :preview-src-list="[mediaUrl]"
            fit="cover"
            class="message-image"
            :preview-teleported="true"
          />
        </div>

        <!-- 视频消息 -->
        <div v-else-if="messageType === 'VIDEO'" class="bubble bubble-video" @click="handleVideoClick">
          <video :src="mediaUrl" class="message-video" />
          <div class="video-overlay">
            <el-icon class="play-icon"><VideoPlay /></el-icon>
          </div>
        </div>

        <!-- 文件消息 -->
        <div v-else-if="messageType === 'FILE'" class="bubble bubble-file" @click="handleFileClick">
          <div class="file-icon-wrapper">
            <el-icon class="file-icon"><Document /></el-icon>
          </div>
          <div class="file-info">
            <div class="file-name">{{ resolvedFileName }}</div>
            <div class="file-size">{{ formatFileSize(resolvedFileSize) }}</div>
          </div>
          <el-icon class="download-icon"><Download /></el-icon>
        </div>

        <!-- 名片消息 -->
        <div v-else-if="messageType === 'CARD'" class="bubble bubble-card" @click="handleCardClick">
          <img :src="cardInfo.userAvatar || '/avatars/default-user.svg'" class="card-avatar" />
          <div class="card-info">
            <div class="card-name">{{ cardInfo.userName }}</div>
            <div class="card-dept">{{ cardInfo.department || '个人名片' }}</div>
          </div>
          <el-button size="small" type="primary" plain>发消息</el-button>
        </div>

        <!-- 位置消息 -->
        <div v-else-if="messageType === 'LOCATION'" class="bubble bubble-location" @click="handleLocationClick">
          <div class="location-map">
            <img v-if="locationInfo.staticMapUrl" :src="locationInfo.staticMapUrl" class="map-img" />
            <div v-else class="map-placeholder">
              <el-icon><Location /></el-icon>
            </div>
          </div>
          <div class="location-info">
            <div class="location-title">{{ locationInfo.title || '位置' }}</div>
            <div class="location-address">{{ locationInfo.address }}</div>
          </div>
        </div>

        <!-- 语音消息 -->
        <div v-else-if="messageType === 'VOICE'" class="bubble bubble-voice" @click="handleVoiceClick">
          <el-icon class="voice-icon" :class="{ 'is-playing': isPlaying }">
            <component :is="isPlaying ? VideoPause : Microphone" />
          </el-icon>
          <div class="voice-wave" :class="{ 'is-playing': isPlaying }">
            <span :style="{ animationDelay: '0s' }"></span>
            <span :style="{ animationDelay: '0.1s' }"></span>
            <span :style="{ animationDelay: '0.2s' }"></span>
            <span :style="{ animationDelay: '0.3s' }"></span>
            <span :style="{ animationDelay: '0.4s' }"></span>
          </div>
          <div class="voice-progress" v-if="isPlaying">
            <div class="voice-progress-bar" :style="{ width: voiceProgressPercent + '%' }"></div>
          </div>
          <span class="voice-duration">{{ formatVoiceTime(currentTime) }}/{{ formatVoiceTime(message.duration) }}"</span>
        </div>
      </div>

      <!-- 消息状态 (发送成功/失败/已读) -->
      <div v-if="isMe && !isGrouped" class="message-status">
        <el-icon v-if="message.status === 'failed'" class="status-icon failed"><CircleCloseFilled /></el-icon>
        <el-icon v-else-if="message.status === 'read'" class="status-icon read"><Check /></el-icon>
        <el-icon v-else-if="message.status === 'sent'" class="status-icon sent"><Check /></el-icon>
      </div>

      <!-- Reactions -->
      <div v-if="reactions.length > 0" class="reactions-row">
        <div
          v-for="r in reactions"
          :key="r.emoji"
          class="reaction-item"
          :class="{ 'is-me': r.hasMe }"
        >
          <span class="reaction-emoji">{{ r.emoji }}</span>
          <span class="reaction-count">{{ r.count }}</span>
        </div>
      </div>
    </div>

    <!-- 悬浮操作栏 -->
    <transition name="action-fade">
      <div v-if="isHovered && !isSelectionMode" class="action-bar">
        <button class="action-btn" title="表情" @click.stop="handleReaction">
          <el-icon><MagicStick /></el-icon>
        </button>
        <button class="action-btn" title="回复" @click.stop="handleReply">
          <el-icon><ChatDotSquare /></el-icon>
        </button>
        <button class="action-btn" title="转发" @click.stop="handleForward">
          <el-icon><Share /></el-icon>
        </button>
        <button class="action-btn" title="更多" @click.stop="handleMore">
          <el-icon><MoreFilled /></el-icon>
        </button>
      </div>
    </transition>
  </div>
</template>

<script setup lang="js">
import { ref, computed } from 'vue'
import {
  Check, Document, Download, Location, Microphone,
  ChatDotSquare, Share, MoreFilled, VideoPlay,
  CircleCloseFilled, MagicStick, VideoPause
} from '@element-plus/icons-vue'

const props = defineProps({
  message: { type: Object, required: true },
  isMe: { type: Boolean, default: false },
  isGrouped: { type: Boolean, default: false },
  isSelectionMode: { type: Boolean, default: false },
  isSelected: { type: Boolean, default: false }
})

const emit = defineEmits([
  'reply', 'forward', 'delete', 'reaction', 'select-message',
  'toggle-selection', 'show-user', 'retry'
])

const isHovered = ref(false)
const isPlaying = ref(false)
const currentTime = ref(0)
const audioRef = ref(null)
let playInterval = null

const messageType = computed(() =>
  (props.message.messageType || props.message.type || 'TEXT').toUpperCase()
)

const mediaUrl = computed(() => props.message.content || '')

const timelineLabel = computed(() => {
  if (!props.message.sendTime) return ''
  const d = new Date(props.message.sendTime)
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
})

const hoverTime = computed(() => timelineLabel.value)

const avatarSrc = computed(() => props.message.senderAvatar || '/avatars/default-user.svg')
const senderLabel = computed(() => props.message.senderName || props.message.senderNickname || '成员')

const cardInfo = computed(() => props.message.payload || {})
const locationInfo = computed(() => {
  const p = props.message.payload || {}
  return { title: p.title, address: p.address || '', staticMapUrl: p.staticMapUrl || '' }
})

const resolvedFileName = computed(() => props.message.fileName || '未知文件')
const resolvedFileSize = computed(() => props.message.fileSize || 0)
const formatFileSize = (s) => {
  if (s > 1024 * 1024) return (s / 1024 / 1024).toFixed(1) + ' MB'
  if (s > 1024) return (s / 1024).toFixed(1) + ' KB'
  return s + ' B'
}

const reactions = computed(() => props.message.reactions || [])

const voiceProgressPercent = computed(() => {
  const duration = parseInt(props.message.duration) || 0
  if (duration === 0) return 0
  return Math.min((currentTime.value / duration) * 100, 100)
})

const formatVoiceTime = (seconds) => {
  const s = parseInt(seconds) || 0
  const mins = Math.floor(s / 60)
  const secs = s % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const cleanupAudio = () => {
  if (playInterval) {
    clearInterval(playInterval)
    playInterval = null
  }
  if (audioRef.value) {
    audioRef.value.pause()
    audioRef.value = null
  }
  isPlaying.value = false
  currentTime.value = 0
}

// 事件处理
const handleFileClick = () => {
  const url = mediaUrl.value
  if (url) window.open(url, '_blank')
}

const handleVideoClick = () => {
  const url = mediaUrl.value
  if (url) window.open(url, '_blank')
}

const handleCardClick = () => {
  const { userId } = cardInfo.value
  if (userId) emit('show-user', userId)
}

const handleLocationClick = () => {
  const { latitude, longitude } = props.message.payload || {}
  if (latitude && longitude) {
    window.open(`https://maps.google.com/?q=${latitude},${longitude}`, '_blank')
  }
}

const handleVoiceClick = () => {
  if (!mediaUrl.value) return
  
  if (isPlaying.value) {
    cleanupAudio()
    return
  }
  
  isPlaying.value = true
  currentTime.value = 0
  
  audioRef.value = new Audio(mediaUrl.value)
  audioRef.value.preload = 'auto'
  
  audioRef.value.onloadedmetadata = () => {
    playInterval = setInterval(() => {
      if (audioRef.value && audioRef.value.currentTime) {
        currentTime.value = Math.floor(audioRef.value.currentTime)
      }
    }, 100)
  }
  
  audioRef.value.onended = () => {
    cleanupAudio()
  }
  
  audioRef.value.onerror = () => {
    console.error('Failed to play voice message:', mediaUrl.value)
    cleanupAudio()
  }
  
  audioRef.value.play().catch(e => {
    console.error('Playback failed:', e)
    cleanupAudio()
  })
}

const handleReaction = () => emit('reaction', props.message)
const handleReply = () => emit('reply', props.message)
const handleForward = () => emit('forward', props.message)
const handleMore = () => emit('select-message', props.message)
</script>

<style lang="scss">
// 导入独立的消息气泡样式文件
@use '@/styles/components/message-bubble.scss';
</style>
