<template>
  <div
    :class="bubbleClass"
    :data-message-id="message.id"
    @contextmenu.prevent="handleContextMenu"
    @click="$emit('click', $event)"
  >
    <!-- 发送者头像（群聊） -->
    <div v-if="isGroup && !isOwn" class="message-avatar">
      <el-avatar :size="40" :src="message.senderAvatar">
        {{ message.senderName?.charAt(0) }}
      </el-avatar>
    </div>

    <div class="message-content-wrapper">
      <!-- 发送者名称（群聊） -->
      <div v-if="isGroup && !isOwn" class="message-sender">
        {{ message.senderName }}
      </div>

      <!-- 消息气泡 -->
      <div class="message-bubble" :class="bubbleTypeClass">
        <!-- 消息内容 -->
        <div class="message-body">
          <!-- 文本消息 -->
          <template v-if="message.messageType === 'text'">
            <div class="message-text" v-html="renderContent"></div>
          </template>

          <!-- 图片消息 -->
          <template v-else-if="message.messageType === 'image'">
            <div class="message-image" @click="previewImage">
              <el-image
                :src="message.fileUrl"
                fit="cover"
                lazy
                :preview-src-list="previewList"
                :initial-index="0"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>加载失败</span>
                  </div>
                </template>
                <template #placeholder>
                  <div class="image-loading">
                    <el-icon class="is-loading"><Loading /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
          </template>

          <!-- 文件消息 -->
          <template v-else-if="message.messageType === 'file'">
            <div class="message-file">
              <div class="file-icon">
                <el-icon :size="32">
                  <Document />
                </el-icon>
              </div>
              <div class="file-info">
                <div class="file-name">{{ message.fileName }}</div>
                <div class="file-size">{{ formatFileSize(message.fileSize) }}</div>
              </div>
              <el-button type="primary" link @click.stop="downloadFile"> 下载 </el-button>
            </div>
          </template>

          <!-- 语音消息 -->
          <template v-else-if="message.messageType === 'audio'">
            <div class="message-audio" @click="playAudio">
              <el-icon :size="20">
                <VideoPlay v-if="!isPlaying" />
                <VideoPause v-else />
              </el-icon>
              <div class="audio-info">
                <div class="audio-wave" :class="{ playing: isPlaying }">
                  <span v-for="i in 20" :key="i" :style="{ animationDelay: `${i * 0.05}s` }"></span>
                </div>
                <div class="audio-progress" :style="{ width: `${audioProgress}%` }"></div>
              </div>
              <span class="audio-duration">{{ formatDuration(message.duration) }}</span>
            </div>
          </template>

          <!-- 视频消息 -->
          <template v-else-if="message.messageType === 'video'">
            <div class="message-video">
              <video :src="message.fileUrl" controls preload="metadata" @click.stop></video>
            </div>
          </template>

          <!-- @提及消息 -->
          <template v-else-if="message.messageType === 'mention'">
            <div class="message-mention">
              <el-icon><Notification /></el-icon>
              <span class="mention-text">{{ message.content }}</span>
            </div>
          </template>

          <!-- 系统消息 -->
          <template v-else-if="message.messageType === 'system'">
            <div class="message-system">
              {{ message.content }}
            </div>
          </template>

          <!-- OA审批卡片消息 -->
          <template v-else-if="message.messageType === 'oa'">
            <OaApprovalCard :card="oaCard" />
          </template>

          <!-- 撤回消息 -->
          <template v-else-if="message.isRevoked">
            <div class="message-recalled">
              <el-icon><RefreshLeft /></el-icon>
              <span>{{ isOwn ? '你撤回了一条消息' : `${message.senderName}撤回了一条消息` }}</span>
            </div>
          </template>
        </div>

        <!-- 引用回复 -->
        <div v-if="message.replyTo" class="message-reply">
          <div class="reply-quote"></div>
          <div class="reply-content">
            <span class="reply-sender">{{ message.replyTo.senderName }}:</span>
            <span class="reply-text">{{ getReplyText(message.replyTo) }}</span>
          </div>
        </div>

        <!-- 消息时间 -->
        <div v-if="showTime" class="message-time">
          {{ formatMessageTime(message.createTime) }}
        </div>

        <!-- 消息状态（仅自己的消息显示） -->
        <MessageStatus
          v-if="isOwn && !message.isRevoked"
          :status="message.status"
          :read-count="message.readCount"
          :show-read-count="isGroup"
          @retry="$emit('retry')"
        />

        <!-- 编辑标记 -->
        <div v-if="message.isEdited && !message.isRevoked" class="message-edited">
          <el-icon><Edit /></el-icon>
          <span>已编辑</span>
        </div>
      </div>

      <!-- 消息操作菜单 -->
      <transition name="fade">
        <div v-if="showActions" class="message-actions">
          <el-tooltip content="复制" placement="top">
            <el-button type="text" @click.stop="copyMessage">
              <el-icon><DocumentCopy /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip v-if="isOwn && canRecall" content="撤回" placement="top">
            <el-button type="text" @click.stop="recallMessage">
              <el-icon><RefreshLeft /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip v-if="isOwn && canEdit" content="编辑" placement="top">
            <el-button type="text" @click.stop="editMessage">
              <el-icon><Edit /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="引用" placement="top">
            <el-button type="text" @click.stop="replyMessage">
              <el-icon><ChatDotRound /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="转发" placement="top">
            <el-button type="text" @click.stop="forwardMessage">
              <el-icon><Share /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="收藏" placement="top">
            <el-button type="text" @click.stop="favoriteMessage">
              <el-icon><Star /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="多选" placement="top">
            <el-button type="text" @click.stop="selectMessage">
              <el-icon><CircleCheck /></el-icon>
            </el-button>
          </el-tooltip>
          <el-dropdown trigger="click" @command="handleMoreCommand">
            <el-button type="text">
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="delete">删除</el-dropdown-item>
                <el-dropdown-item command="link">复制链接</el-dropdown-item>
                <el-dropdown-item command="pin">置顶</el-dropdown-item>
                <el-dropdown-item command="mute">免打扰</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </transition>
    </div>

    <!-- 自己的头像 -->
    <div v-if="isOwn" class="message-avatar">
      <el-avatar :size="40" :src="currentUserAvatar">
        {{ currentUser?.nickname?.charAt(0) }}
      </el-avatar>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Picture,
  Loading,
  Document,
  VideoPlay,
  VideoPause,
  Notification,
  RefreshLeft,
  Edit,
  DocumentCopy,
  ChatDotRound,
  Share,
  Star,
  CircleCheck,
  MoreFilled,
} from '@element-plus/icons-vue'
import MessageStatus from './MessageStatus.vue'
import OaApprovalCard from './OaApprovalCard.vue'

const props = defineProps({
  message: {
    type: Object,
    required: true,
  },
  isOwn: {
    type: Boolean,
    default: false,
  },
  isGroup: {
    type: Boolean,
    default: false,
  },
  currentUser: {
    type: Object,
    default: null,
  },
  showTime: {
    type: Boolean,
    default: false,
  },
  showActions: {
    type: Boolean,
    default: false,
  },
  // 撤回时间限制（分钟）
  recallLimit: {
    type: Number,
    default: 2,
  },
})

const emit = defineEmits([
  'click',
  'retry',
  'copy',
  'recall',
  'edit',
  'reply',
  'forward',
  'favorite',
  'select',
  'more',
])

const oaCard = computed(() => {
  const message = props.message || {}
  let payload = message.oaData || message.cardPayload
  if (!payload) {
    const content = message.content
    if (content && typeof content === 'object') {
      payload = content
    } else if (typeof content === 'string') {
      const trimmed = content.trim()
      if (trimmed.startsWith('{') || trimmed.startsWith('[')) {
        try {
          const parsed = JSON.parse(content)
          if (parsed && typeof parsed === 'object') {
            payload = parsed
          }
        } catch (e) {
          payload = { text: content }
        }
      } else if (content) {
        payload = { text: content }
      }
    }
  }
  const base = payload || {}
  return {
    ...base,
    approvalId: base.approvalId || message.approvalId || message.bizId,
    title: base.title || message.title || '',
    applicant: base.applicant || base.applicantName || message.applicant,
    applyTime: base.applyTime || base.createTime || message.createTime,
    status: base.status || message.status,
    statusText: base.statusText,
  }
})

const isPlaying = ref(false)
const audioElement = ref(null)
const audioProgress = ref(0)
const audioDuration = ref(0)

// 创建音频对象
const createAudio = () => {
  if (!props.message.fileUrl) return null

  const audio = new Audio()
  audio.src = props.message.fileUrl

  // 监听播放事件
  audio.addEventListener('play', () => {
    isPlaying.value = true
  })

  audio.addEventListener('pause', () => {
    isPlaying.value = false
  })

  audio.addEventListener('ended', () => {
    isPlaying.value = false
    audioProgress.value = 0
  })

  audio.addEventListener('timeupdate', () => {
    if (audio.duration) {
      audioProgress.value = (audio.currentTime / audio.duration) * 100
    }
  })

  audio.addEventListener('loadedmetadata', () => {
    audioDuration.value = audio.duration
  })

  audio.addEventListener('error', () => {
    ElMessage.error('音频加载失败')
    isPlaying.value = false
  })

  return audio
}

// 播放/暂停音频
const playAudio = () => {
  if (!props.message.fileUrl) {
    ElMessage.warning('音频文件不存在')
    return
  }

  // 如果还没有创建音频对象，创建一个
  if (!audioElement.value) {
    audioElement.value = createAudio()
  }

  const audio = audioElement.value
  if (!audio) return

  if (isPlaying.value) {
    audio.pause()
  } else {
    audio.play().catch(error => {
      console.error('音频播放失败:', error)
      ElMessage.error('音频播放失败')
    })
  }
}

// 清理音频资源
const cleanupAudio = () => {
  if (audioElement.value) {
    audioElement.value.pause()
    audioElement.value.src = ''
    audioElement.value = null
  }
  isPlaying.value = false
  audioProgress.value = 0
}

// 监听消息变化，重新创建音频对象
watch(
  () => props.message.fileUrl,
  () => {
    cleanupAudio()
  }
)

// 组件卸载时清理
onUnmounted(() => {
  cleanupAudio()
})

const bubbleClass = computed(() => {
  return ['message-item', { 'is-own': props.isOwn }, { 'is-group': props.isGroup }]
})

const bubbleTypeClass = computed(() => {
  return [{ 'is-own': props.isOwn }, `type-${props.message.messageType}`]
})

const currentUserAvatar = computed(() => {
  return props.currentUser?.avatar || ''
})

const previewList = computed(() => {
  return props.message.fileUrl ? [props.message.fileUrl] : []
})

const renderContent = computed(() => {
  if (!props.message.content) return ''

  // 处理@提及
  let content = props.message.content
  content = content.replace(/@(\S+)/g, '<span class="mention-tag">@$1</span>')

  // 处理换行
  content = content.replace(/\n/g, '<br>')

  // 处理链接
  content = content.replace(
    /(https?:\/\/[^\s]+)/g,
    '<a href="$1" target="_blank" class="link-tag">$1</a>'
  )

  return content
})

const canRecall = computed(() => {
  if (!props.message.createTime) return false
  const diff = Date.now() - new Date(props.message.createTime).getTime()
  return diff < props.recallLimit * 60 * 1000
})

const canEdit = computed(() => {
  if (!props.isOwn || !props.message.createTime) return false
  const diff = Date.now() - new Date(props.message.createTime).getTime()
  return diff < props.recallLimit * 60 * 1000 && props.message.messageType === 'text'
})

const formatFileSize = bytes => {
  if (!bytes) return ''
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(1)} ${units[unitIndex]}`
}

const formatDuration = seconds => {
  if (!seconds) return '0"'
  const min = Math.floor(seconds / 60)
  const sec = seconds % 60
  return min > 0 ? `${min}'${sec}"` : `${sec}"`
}

const formatMessageTime = time => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }

  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }

  // 今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }

  // 更早
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const getReplyText = replyTo => {
  if (!replyTo) return ''
  const text = replyTo.content || '[消息]'
  return text.length > 30 ? text.substring(0, 30) + '...' : text
}

const previewImage = () => {
  // 图片预览由el-image组件处理
}

const downloadFile = () => {
  if (props.message.fileUrl) {
    const link = document.createElement('a')
    link.href = props.message.fileUrl
    link.download = props.message.fileName || 'download'
    link.click()
  }
}

const playAudio = () => {
  isPlaying.value = !isPlaying.value
  // TODO: 实现音频播放逻辑
}

const handleContextMenu = e => {
  emit('click', e)
}

const copyMessage = () => {
  navigator.clipboard.writeText(props.message.content || '').then(() => {
    ElMessage.success('已复制到剪贴板')
  })
  emit('copy', props.message)
}

const recallMessage = () => {
  emit('recall', props.message)
}

const editMessage = () => {
  emit('edit', props.message)
}

const replyMessage = () => {
  emit('reply', props.message)
}

const forwardMessage = () => {
  emit('forward', props.message)
}

const favoriteMessage = () => {
  emit('favorite', props.message)
}

const selectMessage = () => {
  emit('select', props.message)
}

const handleMoreCommand = command => {
  emit('more', { command, message: props.message })
}
</script>

<style lang="scss" scoped>
.message-item {
  display: flex;
  padding: 6px 12px;
  margin-bottom: 8px;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: var(--el-fill-color-light);
  }

  &.is-own {
    flex-direction: row-reverse;
  }

  .message-avatar {
    flex-shrink: 0;
    margin: 0 6px;
  }
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 72%;
  min-width: 0;
  flex: 1 1 auto;
  overflow: hidden;
}

.message-sender {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 2px;
  margin-left: 2px;
}

.is-own .message-sender {
  display: none;
}

.message-bubble {
  position: relative;
  padding: 6px 10px;
  border-radius: 6px;
  background-color: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
  min-width: 0;
  overflow-wrap: break-word;
  word-wrap: break-word;
  word-break: break-word;

  &.is-own {
    background-color: #0089ff;
    color: #fff;

    .message-text :deep(a) {
      color: rgba(255, 255, 255, 0.9);
    }

    .message-text :deep(.mention-tag) {
      background-color: rgba(255, 255, 255, 0.2);
    }
  }

  &.type-image,
  &.type-video,
  &.type-file {
    padding: 4px;
  }

  &.type-system {
    background-color: transparent;
    box-shadow: none;
    padding: 0;
  }
}

.message-body {
  word-break: break-word;
  white-space: pre-wrap;
}

.message-text {
  font-size: 14px;
  line-height: 1.6;

  :deep(.mention-tag) {
    color: var(--el-color-primary);
    background-color: rgba(22, 119, 255, 0.1);
    padding: 2px 4px;
    border-radius: 4px;
    font-weight: 500;
  }

  :deep(a) {
    color: var(--el-color-primary);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.message-image {
  :deep(.el-image) {
    max-width: 280px;
    max-height: 280px;
    border-radius: 4px;
    overflow: hidden;
    cursor: pointer;
  }

  .image-error,
  .image-loading {
    width: 200px;
    height: 150px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: var(--el-text-color-secondary);
    background-color: var(--el-fill-color);
    border-radius: 4px;
  }
}

.message-file {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  min-width: 280px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;

  .file-icon {
    color: var(--el-color-primary);
  }

  .file-info {
    flex: 1;
    min-width: 0;

    .file-name {
      font-size: 14px;
      color: var(--el-text-color-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .file-size {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-top: 2px;
    }
  }
}

.message-audio {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  min-width: 120px;
  cursor: pointer;

  .audio-duration {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    white-space: nowrap;
  }

  .audio-info {
    position: relative;
    display: flex;
    align-items: center;
    flex: 1;
  }

  .audio-wave {
    display: flex;
    align-items: center;
    gap: 2px;
    height: 20px;

    span {
      width: 2px;
      background-color: var(--el-text-color-placeholder);
      border-radius: 1px;
      animation: wave 1s ease-in-out infinite;
      animation-play-state: paused;

      &:nth-child(odd) {
        height: 8px;
      }

      &:nth-child(even) {
        height: 16px;
      }

      &.playing span {
        animation-play-state: running;
      }
    }

    &.playing span {
      animation-play-state: running;
    }
  }

  .audio-progress {
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    height: 100%;
    background-color: rgba(64, 158, 255, 0.3);
    border-radius: 2px;
    pointer-events: none;
    transition: width 0.1s linear;
  }

  &.playing .audio-wave span {
    animation-play-state: running;
    background-color: var(--el-color-primary);
  }
}

@keyframes wave {
  0%,
  100% {
    transform: scaleY(0.5);
  }
  50% {
    transform: scaleY(1);
  }
}

.message-video {
  :deep(video) {
    max-width: 320px;
    max-height: 240px;
    border-radius: 4px;
  }
}

.message-mention {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--el-color-warning);
  font-size: 14px;

  .mention-text {
    flex: 1;
  }
}

.message-system {
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  padding: 4px 12px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}

.message-recalled {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  font-style: italic;
}

.message-reply {
  display: flex;
  gap: 8px;
  padding: 8px 0;
  margin-bottom: 4px;
  border-left: 2px solid var(--el-border-color);
  padding-left: 8px;

  .reply-content {
    flex: 1;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    overflow: hidden;

    .reply-sender {
      color: var(--el-text-color-primary);
      font-weight: 500;
    }

    .reply-text {
      color: var(--el-text-color-secondary);
    }
  }
}

.message-time {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  margin-top: 4px;
}

.message-edited {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  margin-left: 8px;
}

.message-actions {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px;
  background-color: var(--el-bg-color);
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;

  .is-own & {
    right: 100%;
    margin-right: 8px;
  }

  &:not(.is-own) & {
    left: 100%;
    margin-left: 8px;
  }

  .el-button {
    padding: 6px;
    color: var(--el-text-color-secondary);

    &:hover {
      color: var(--el-color-primary);
    }
  }
}

.message-bubble:hover + .message-actions,
.message-item:hover .message-actions {
  opacity: 1;
  visibility: visible;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-50%) scale(0.9);
}
</style>
