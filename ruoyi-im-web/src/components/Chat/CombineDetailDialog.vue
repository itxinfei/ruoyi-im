<template>
  <el-dialog
    v-model="visible"
    title="聊天记录详情"
    width="600px"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    class="combine-detail-dialog"
  >
    <div class="combine-detail-content">
      <!-- 会话信息 -->
      <div v-if="conversationInfo" class="conversation-info">
        <span class="material-icons-outlined">chat_bubble_outline</span>
        <span>{{ conversationInfo }}</span>
      </div>

      <!-- 消息列表 -->
      <div class="messages-list">
        <div
          v-for="(msg, index) in messages"
          :key="msg.id || index"
          class="message-item"
        >
          <!-- 发送者信息 -->
          <div class="message-header">
            <DingtalkAvatar
              :src="msg.senderAvatar"
              :name="msg.senderName"
              :size="36"
            />
            <div class="sender-info">
              <span class="sender-name">{{ msg.senderName }}</span>
              <span class="send-time">{{ formatTime(msg.timestamp || msg.sendTime) }}</span>
            </div>
          </div>

          <!-- 消息内容 -->
          <div class="message-content">
            <!-- 文本消息 -->
            <div v-if="msg.type === 'TEXT'" class="text-content">
              {{ msg.content }}
            </div>

            <!-- 图片消息 -->
            <div v-else-if="msg.type === 'IMAGE'" class="image-content">
              <img
                :src="getImageUrl(msg)"
                :alt="msg.senderName + '的图片'"
                @click="previewImage(getImageUrl(msg))"
              />
              [图片]
            </div>

            <!-- 视频消息 -->
            <div v-else-if="msg.type === 'VIDEO'" class="video-content">
              <span class="material-icons-outlined">videocam</span>
              [视频]
            </div>

            <!-- 文件消息 -->
            <div v-else-if="msg.type === 'FILE'" class="file-content">
              <span class="material-icons-outlined">insert_drive_file</span>
              <span v-if="getFileName(msg)">{{ getFileName(msg) }}</span>
              <span v-else>[文件]</span>
            </div>

            <!-- 语音消息 -->
            <div v-else-if="msg.type === 'VOICE' || msg.type === 'AUDIO'" class="voice-content">
              <span class="material-icons-outlined">mic</span>
              [语音] {{ getVoiceDuration(msg) }}
            </div>

            <!-- 撤回消息 -->
            <div v-else-if="msg.type === 'RECALLED'" class="recalled-content">
              <span class="material-icons-outlined">history</span>
              {{ msg.senderName }}撤回了一条消息
            </div>

            <!-- 其他类型 -->
            <div v-else class="other-content">
              [{{ msg.type }}]
            </div>
          </div>
        </div>
      </div>

      <!-- 消息统计 -->
      <div class="messages-summary">
        共 {{ messages.length }} 条消息
        <span v-if="timeRange">{{ timeRange }}</span>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" @click="handleForward">转发</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ElImageViewer } from 'element-plus'
import { parseMessageContent } from '@/utils/message'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  messages: {
    type: Array,
    default: () => []
  },
  conversationTitle: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'forward'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 图片预览状态
const showImageViewer = ref(false)
const imagePreviewUrl = ref('')
const imagePreviewIndex = ref(0)

// 会话信息
const conversationInfo = computed(() => {
  if (props.conversationTitle) return props.conversationTitle
  if (props.messages.length === 0) return ''
  // 从消息中提取会话信息（假设是群聊或单聊）
  const senders = [...new Set(props.messages.map(m => m.senderName))]
  if (senders.length === 1) {
    return `与 ${senders[0]} 的聊天记录`
  } else {
    return `群聊记录 (${senders.length}人)`
  }
})

// 时间范围
const timeRange = computed(() => {
  if (props.messages.length === 0) return ''

  const timestamps = props.messages
    .map(m => m.timestamp || m.sendTime || m.createTime)
    .filter(Boolean)
    .map(t => new Date(t).getTime())
    .sort((a, b) => a - b)

  if (timestamps.length === 0) return ''

  const start = new Date(timestamps[0])
  const end = new Date(timestamps[timestamps.length - 1])

  const formatDate = (date) => {
    const now = new Date()
    const isSameDay = date.toDateString() === now.toDateString()
    const isYesterday = new Date(now.setDate(now.getDate() - 1)).toDateString() === date.toDateString()

    if (isSameDay) {
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } else if (isYesterday) {
      return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } else {
      return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }) + ' ' +
             date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
  }

  const startStr = formatDate(start)
  const endStr = formatDate(end)

  return startStr === endStr ? startStr : `${startStr} - ${endStr}`
})

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const isSameDay = date.toDateString() === now.toDateString()
  const isYesterday = new Date(now.setDate(now.getDate() - 1)).toDateString() === date.toDateString()

  if (isSameDay) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (isYesterday) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else {
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }) + ' ' +
           date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
}

// 获取图片URL
const getImageUrl = (msg) => {
  const content = parseMessageContent(msg) || {}
  return content.url || content.imageUrl || ''
}

// 获取文件名
const getFileName = (msg) => {
  const content = parseMessageContent(msg) || {}
  return content.fileName || content.name || ''
}

// 获取语音时长
const getVoiceDuration = (msg) => {
  const content = parseMessageContent(msg) || {}
  const duration = content.duration || 0
  const mins = Math.floor(duration / 60)
  const secs = Math.floor(duration % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// 预览图片
const previewImage = (url) => {
  // 收集所有图片
  const images = props.messages
    .filter(m => m.type === 'IMAGE')
    .map(m => getImageUrl(m))
    .filter(url => url)

  imagePreviewUrl.value = url
  imagePreviewIndex.value = images.indexOf(url)
  showImageViewer.value = true
}

// 关闭图片预览
const closeImageViewer = () => {
  showImageViewer.value = false
}

// 转发
const handleForward = () => {
  emit('forward', props.messages)
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.combine-detail-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
    max-height: 60vh;
    overflow-y: auto;
  }
}

.combine-detail-content {
  .conversation-info {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: var(--dt-fill-color-light);
    color: var(--dt-text-secondary);
    font-size: 14px;
    border-bottom: 1px solid var(--dt-border-light);

    .material-icons-outlined {
      color: var(--dt-brand-color);
    }
  }

  .messages-list {
    max-height: 50vh;
    overflow-y: auto;

    .message-item {
      padding: 12px 16px;
      border-bottom: 1px solid var(--dt-border-lighter);
      transition: background 0.2s;

      &:hover {
        background: var(--dt-bg-hover);
      }

      &:last-child {
        border-bottom: none;
      }
    }

    .message-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;

      .sender-info {
        display: flex;
        flex-direction: column;
        gap: 2px;

        .sender-name {
          font-size: 14px;
          font-weight: 500;
          color: var(--dt-text-primary);
        }

        .send-time {
          font-size: 12px;
          color: var(--dt-text-tertiary);
        }
      }
    }

    .message-content {
      padding-left: 44px;
      font-size: 14px;
      color: var(--dt-text-primary);
      line-height: 1.5;

      .text-content {
        white-space: pre-wrap;
        word-break: break-word;
      }

      .image-content {
        display: flex;
        align-items: center;
        gap: 8px;
        color: var(--dt-text-secondary);

        img {
          max-width: 100px;
          max-height: 100px;
          border-radius: 4px;
          cursor: zoom-in;
          object-fit: cover;
        }
      }

      .video-content,
      .file-content,
      .voice-content {
        display: flex;
        align-items: center;
        gap: 8px;
        color: var(--dt-text-secondary);

        .material-icons-outlined {
          color: var(--dt-brand-color);
        }
      }

      .recalled-content {
        display: flex;
        align-items: center;
        gap: 8px;
        color: var(--dt-text-tertiary);
        font-style: italic;
      }

      .other-content {
        color: var(--dt-text-tertiary);
      }
    }
  }

  .messages-summary {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: var(--dt-fill-color-light);
    border-top: 1px solid var(--dt-border-light);
    font-size: 13px;
    color: var(--dt-text-secondary);
  }
}
</style>
