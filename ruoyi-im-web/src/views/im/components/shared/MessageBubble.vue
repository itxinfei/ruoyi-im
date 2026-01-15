<template>
  <div
    :data-message-id="message.id"
    class="message-item"
    :class="{ isOwn: isOwnMessage }"
  >
    <!-- 对方消息的左侧头像 -->
    <SmartAvatar
      v-if="!isOwnMessage"
      :name="senderName"
      :avatar="senderAvatar"
      :size="36"
      class="message-avatar"
    />

    <div class="message-content" @click.right.prevent="$emit('context-menu', $event, message)">
      <!-- 自己消息的右侧头像 -->
      <SmartAvatar
        v-if="isOwnMessage"
        :name="currentUser?.name || currentUser?.nickName || '我'"
        :avatar="currentUser?.avatar || currentUser?.avatarUrl"
        :size="36"
        class="message-avatar"
      />

      <!-- 发送者名称（对方消息显示） -->
      <div v-if="!isOwnMessage" class="sender-name">
        {{ senderName }}
      </div>

      <!-- 引用回复内容 -->
      <div v-if="message.replyTo" class="quote-message" @click="$emit('click-quote', message.replyTo.id)">
        <span class="quote-label">回复 {{ message.replyTo.senderName }}</span>
        <span class="quote-content">{{ message.replyTo.content }}</span>
      </div>

      <!-- 文本消息 -->
      <div
        v-if="message.type === 'text' || !message.type"
        class="message-bubble"
        :class="{
          sending: message.status === 'sending',
          failed: message.status === 'failed'
        }"
      >
        <span v-html="formattedContent"></span>
      </div>

      <!-- 图片消息 -->
      <div v-else-if="message.type === 'image'" class="message-image">
        <el-image
          :src="imageUrl"
          :preview-src-list="[imageUrl]"
          fit="cover"
          class="image-content"
          :class="{ sending: message.status === 'sending' }"
          :preview-teleported="true"
        >
          <template #error>
            <div class="image-error">
              <el-icon><Picture /></el-icon>
              <span>加载失败</span>
            </div>
          </template>
        </el-image>
      </div>

      <!-- 文件消息 -->
      <div
        v-else-if="message.type === 'file'"
        class="message-file"
        :class="{ sending: message.status === 'sending' }"
      >
        <div class="file-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="file-info">
          <div class="file-name">{{ fileName }}</div>
          <div class="file-size">{{ fileSize }}</div>
        </div>
        <el-button type="primary" size="small" @click.stop="$emit('download', message)">
          下载
        </el-button>
      </div>

      <!-- 语音消息 -->
      <div
        v-else-if="message.type === 'voice'"
        class="message-voice"
        :class="{ sending: message.status === 'sending' }"
      >
        <div class="voice-icon" :class="{ playing: isPlaying }">
          <el-icon><Microphone /></el-icon>
        </div>
        <div class="voice-info">
          <span class="voice-duration">{{ formatDuration(message.duration || 0) }}</span>
          <div class="voice-wave">
            <div
              v-for="(bar, index) in 10"
              :key="index"
              class="wave-bar"
              :style="{ height: getWaveBarHeight(index) + '%' }"
            ></div>
          </div>
        </div>
      </div>

      <!-- 链接卡片消息 -->
      <div v-else-if="message.type === 'link'" class="message-link">
        <a v-if="message.url" :href="message.url" target="_blank" class="link-content">
          <div v-if="message.image" class="link-image">
            <el-image :src="message.image" fit="cover">
              <template #error>
                <div class="link-image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div class="link-info">
            <div class="link-title">{{ message.title || '链接' }}</div>
            <div class="link-description">{{ message.description || message.url }}</div>
          </div>
        </a>
      </div>

      <!-- 系统消息 -->
      <div v-else-if="message.type === 'system'" class="message-system">
        {{ message.content }}
      </div>

      <!-- 其他类型 -->
      <div
        v-else
        class="message-bubble"
        :class="{ sending: message.status === 'sending' }"
      >
        <span class="message-type-label">[{{ message.type }}消息]</span>
      </div>

      <!-- 消息时间、状态和操作 -->
      <div class="message-meta">
        <div class="message-time">
          {{ formattedTime }}
          <el-icon v-if="message.status === 'sending'" class="status-icon sending">
            <Loading />
          </el-icon>
          <el-icon
            v-else-if="message.status === 'failed'"
            class="status-icon failed"
            @click="$emit('resend', message)"
          >
            <WarningFilled />
          </el-icon>
          <el-icon
            v-else-if="isOwnMessage"
            class="status-icon sent"
          >
            <SuccessFilled />
          </el-icon>

          <!-- 消息已读状态 -->
          <span
            v-if="
              isOwnMessage &&
              message.status !== 'sending' &&
              message.status !== 'failed'
            "
            class="read-receipt"
          >
            <template v-if="isPrivateChat">
              {{ message.isRead ? '已读' : '未读' }}
            </template>
            <template v-else>
              {{ message.readCount || 0 }}{{ message.memberCount ? `/${message.memberCount}` : '' }}已读
            </template>
          </span>
        </div>

        <!-- 消息反应 -->
        <div
          v-if="message.reactions && Object.keys(message.reactions).length > 0"
          class="message-reactions"
          @click.stop="$emit('toggle-reaction', message)"
        >
          <div
            v-for="(count, emoji) in message.reactions"
            :key="emoji"
            class="reaction-item"
            :class="{ active: isReactedByUser(emoji) }"
          >
            <span class="reaction-emoji">{{ emoji }}</span>
            <span class="reaction-count">{{ count }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import {
  Picture,
  Document,
  Microphone,
  Loading,
  WarningFilled,
  SuccessFilled
} from '@element-plus/icons-vue'
import SmartAvatar from '@/components/SmartAvatar/index.vue'

const props = defineProps({
  message: {
    type: Object,
    required: true
  },
  currentUser: {
    type: Object,
    default: () => ({})
  },
  sessionType: {
    type: String,
    default: 'PRIVATE'
  }
})

const emit = defineEmits([
  'context-menu',
  'click-quote',
  'download',
  'resend',
  'toggle-reaction'
])

const store = useStore()
const isPlaying = ref(false)

// 计算属性
const isOwnMessage = computed(() => {
  return props.message.isOwn || props.message.senderId === props.currentUser?.userId
})

const isPrivateChat = computed(() => {
  return props.sessionType === 'PRIVATE'
})

const senderName = computed(() => {
  return props.message.senderName || props.message.sender?.name || '未知用户'
})

const senderAvatar = computed(() => {
  return props.message.senderAvatar || props.message.avatar
})

const formattedContent = computed(() => {
  return formatMessageContent(props.message.content || '')
})

const formattedTime = computed(() => {
  const timestamp = props.message.timestamp || props.message.time || Date.now()
  return formatTime(timestamp)
})

const imageUrl = computed(() => {
  return getImageUrl(props.message)
})

const fileName = computed(() => {
  return getFileInfo(props.message).name || '未知文件'
})

const fileSize = computed(() => {
  const size = getFileInfo(props.message).size
  return size ? formatFileSize(size) : '未知大小'
})

// 方法
const formatMessageContent = (content) => {
  if (!content) return ''

  // URL链接转换
  content = content.replace(
    /(https?:\/\/[^\s]+)/g,
    '<a href="$1" target="_blank" class="message-link">$1</a>'
  )

  // @提及高亮
  content = content.replace(
    /@(\S+)/g,
    '<span class="mention">@$1</span>'
  )

  // 表情符号简单处理（实际项目中可以使用emoji库）
  content = content.replace(
    /:\)|😊|😄/g,
    '😊'
  )

  return content
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  // 今天的消息只显示时间
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  // 昨天的消息
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }

  // 其他日期
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatDuration = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

const getImageUrl = (message) => {
  if (message.url) return message.url
  if (message.content) {
    try {
      const content = JSON.parse(message.content)
      return content.url || content.text
    } catch {
      return message.content
    }
  }
  return ''
}

const getFileInfo = (message) => {
  if (message.url) {
    const parts = message.url.split('/')
    return {
      name: message.fileName || parts[parts.length - 1],
      size: message.fileSize || 0
    }
  }
  if (message.content) {
    try {
      const content = JSON.parse(message.content)
      return {
        name: content.fileName || content.name || '未知文件',
        size: content.fileSize || content.size || 0
      }
    } catch {
      return { name: '未知文件', size: 0 }
    }
  }
  return { name: '未知文件', size: 0 }
}

const getWaveBarHeight = (index) => {
  // 生成随机高度的波形条
  const baseHeight = 20
  const randomHeight = Math.random() * 60
  return baseHeight + randomHeight
}

const isReactedByUser = (emoji) => {
  // TODO: 检查当前用户是否对消息进行了该表情反应
  return false
}
</script>

<style lang="scss" scoped>
.message-item {
  display: flex;
  margin-bottom: 20px;
  padding: 0 16px;

  &.isOwn {
    flex-direction: row-reverse;

    .message-content {
      align-items: flex-end;
    }

    .message-avatar {
      margin-left: 12px;
      margin-right: 0;
    }

    .message-bubble {
      background-color: #1677ff;
      color: #ffffff;
      border-radius: 8px 0 8px 8px;
    }

    .message-file,
    .message-voice,
    .message-link {
      background-color: #1677ff;
    }
  }
}

.message-avatar {
  flex-shrink: 0;
  margin-right: 12px;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 60%;
  position: relative;
}

.sender-name {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.quote-message {
  padding: 8px 12px;
  background-color: #f5f5f5;
  border-left: 3px solid #1677ff;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #e8e8e8;
  }

  .quote-label {
    font-size: 12px;
    color: #1677ff;
    margin-bottom: 4px;
  }

  .quote-content {
    font-size: 13px;
    color: #595959;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.message-bubble {
  padding: 10px 14px;
  background-color: #ffffff;
  border-radius: 0 8px 8px 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  word-wrap: break-word;
  word-break: break-all;
  line-height: 1.6;
  font-size: 14px;
  color: #262626;
  transition: all 0.2s ease;

  &.sending {
    opacity: 0.6;
  }

  &.failed {
    background-color: #fff1f0;
    border: 1px solid #ffccc7;
  }

  :deep(.message-link) {
    color: #1677ff;
    text-decoration: underline;
    cursor: pointer;
  }

  :deep(.mention) {
    color: #1677ff;
    font-weight: 500;
  }
}

.message-image {
  max-width: 300px;
  border-radius: 8px;
  overflow: hidden;

  .image-content {
    width: 100%;
    display: block;
    cursor: pointer;

    &.sending {
      opacity: 0.6;
    }
  }

  .image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 200px;
    height: 150px;
    background-color: #f5f5f5;
    color: #8c8c8c;

    .el-icon {
      font-size: 32px;
      margin-bottom: 8px;
    }
  }
}

.message-file {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  min-width: 240px;

  &.sending {
    opacity: 0.6;
  }

  .file-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    background-color: #e6f7ff;
    border-radius: 8px;
    color: #1677ff;
    font-size: 20px;
    flex-shrink: 0;
  }

  .file-info {
    flex: 1;
    min-width: 0;
  }

  .file-name {
    font-size: 14px;
    color: #262626;
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .file-size {
    font-size: 12px;
    color: #8c8c8c;
  }
}

.message-voice {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  min-width: 160px;
  cursor: pointer;

  &.sending {
    opacity: 0.6;
  }

  .voice-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    background-color: #1677ff;
    border-radius: 50%;
    color: #ffffff;
    font-size: 16px;
    flex-shrink: 0;
    transition: all 0.3s ease;

    &.playing {
      animation: voice-pulse 1s infinite;
    }
  }

  @keyframes voice-pulse {
    0%, 100% {
      transform: scale(1);
    }
    50% {
      transform: scale(1.1);
    }
  }

  .voice-info {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .voice-duration {
    font-size: 14px;
    color: #262626;
    white-space: nowrap;
  }

  .voice-wave {
    display: flex;
    align-items: center;
    gap: 2px;
  }

  .wave-bar {
    width: 3px;
    background-color: #1677ff;
    border-radius: 1px;
    transition: height 0.2s ease;
  }
}

.message-link {
  background-color: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  max-width: 300px;
  text-decoration: none;

  .link-content {
    display: block;
    text-decoration: none;
  }

  .link-image {
    width: 100%;
    height: 150px;
    overflow: hidden;

    .el-image {
      width: 100%;
      height: 100%;
    }

    .link-image-error {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      background-color: #f5f5f5;
      color: #8c8c8c;
    }
  }

  .link-info {
    padding: 12px;
  }

  .link-title {
    font-size: 14px;
    font-weight: 500;
    color: #262626;
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .link-description {
    font-size: 12px;
    color: #8c8c8c;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.message-system {
  text-align: center;
  font-size: 12px;
  color: #8c8c8c;
  padding: 8px 16px;
  background-color: #f5f5f5;
  border-radius: 4px;
  align-self: center;
}

.message-type-label {
  color: #8c8c8c;
  font-size: 12px;
}

.message-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 4px;
}

.message-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #8c8c8c;

  .status-icon {
    font-size: 14px;

    &.sending {
      color: #8c8c8c;
      animation: rotate 1s linear infinite;
    }

    &.failed {
      color: #ff4d4f;
      cursor: pointer;
    }

    &.sent {
      color: #52c41a;
    }
  }

  @keyframes rotate {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }

  .read-receipt {
    font-size: 11px;
    color: #52c41a;
  }
}

.message-reactions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;

  .reaction-item {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 2px 8px;
    background-color: #f0f0f0;
    border-radius: 12px;
    font-size: 12px;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background-color: #e6f7ff;
    }

    &.active {
      background-color: #e6f7ff;
      border: 1px solid #1677ff;
    }

    .reaction-emoji {
      font-size: 14px;
    }

    .reaction-count {
      font-size: 12px;
      color: #595959;
    }
  }
}
</style>