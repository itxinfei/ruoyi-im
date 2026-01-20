<template>
  <div class="chat-panel">
    <div class="chat-header">
      <el-avatar :size="40" :src="session?.avatar">
        {{ session?.name?.charAt(0) }}
      </el-avatar>
      <div class="header-info">
        <div class="header-name">{{ session?.name }}</div>
        <div class="header-status">在线</div>
      </div>
      <div class="header-actions">
        <el-tooltip content="语音通话">
          <el-button :icon="Phone" text circle size="small" />
        </el-tooltip>
        <el-tooltip content="视频通话">
          <el-button :icon="VideoCamera" text circle size="small" />
        </el-tooltip>
        <el-tooltip content="更多">
          <el-button :icon="MoreFilled" text circle size="small" />
        </el-tooltip>
      </div>
    </div>

    <div class="message-area" ref="messageAreaRef">
      <template v-for="(msg, index) in messagesWithTimeDivider" :key="msg.id || `divider-${index}`">
        <div v-if="msg.isTimeDivider" class="time-divider">
          <span>{{ msg.timeText }}</span>
        </div>
        <div
          v-else
          class="message-item"
          :class="{ isOwn: msg.isOwn }"
        >
          <el-avatar v-if="!msg.isOwn" :size="36" :src="msg.senderAvatar">
            {{ msg.senderName?.charAt(0) }}
          </el-avatar>
          <div class="message-content">
            <div v-if="!msg.isOwn" class="sender-name">
              {{ msg.senderName }}
            </div>
            <div class="message-bubble" :class="{ isOwn: msg.isOwn }">
              {{ msg.content }}
            </div>
            <div class="message-time">
              {{ formatMessageTime(msg.timestamp) }}
              <span v-if="msg.isOwn" class="read-status">
                {{ msg.isRead ? '已读' : '未读' }}
              </span>
            </div>
          </div>
          <el-avatar v-if="msg.isOwn" :size="36" :src="currentUser?.avatar">
            {{ currentUser?.name?.charAt(0) }}
          </el-avatar>
        </div>
      </template>
    </div>

    <div class="chat-input">
      <div class="input-toolbar">
        <el-tooltip content="表情">
          <el-button :icon="ChatDotRound" text class="toolbar-btn" />
        </el-tooltip>
        <el-tooltip content="上传文件">
          <el-button :icon="Folder" text class="toolbar-btn" />
        </el-tooltip>
        <el-tooltip content="上传图片">
          <el-button :icon="Picture" text class="toolbar-btn" />
        </el-tooltip>
        <el-tooltip content="@成员">
          <el-button :icon="Promotion" text class="toolbar-btn" />
        </el-tooltip>
      </div>

      <div class="input-area-wrapper">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :autosize="{ minRows: 1, maxRows: 5 }"
          placeholder="输入消息... Enter发送，Shift+Enter换行"
          class="chat-input"
          @keydown="handleKeydown"
        />

        <div class="send-button-wrapper">
          <el-button
            type="primary"
            :disabled="!canSend"
            @click="sendMessage"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import {
  Phone,
  VideoCamera,
  MoreFilled,
  ChatDotRound,
  Folder,
  Picture,
  Promotion
} from '@element-plus/icons-vue'

const props = defineProps({
  session: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['send-message'])

const currentUser = ref({
  id: 1,
  name: '测试用户',
  avatar: 'https://via.placeholder.com/40'
})

const inputMessage = ref('')
const messageAreaRef = ref(null)

const messages = computed(() => {
  return props.session?.messages || []
})

const messagesWithTimeDivider = computed(() => {
  const msgs = messages.value
  if (msgs.length === 0) return []
  
  const result = []
  let lastDate = null
  
  msgs.forEach((msg) => {
    const msgDate = new Date(msg.timestamp).toDateString()
    
    if (msgDate !== lastDate) {
      lastDate = msgDate
      result.push({
        isTimeDivider: true,
        timeText: formatMessageTime(msg.timestamp)
      })
    }
    
    result.push(msg)
  })
  
  return result
})

const canSend = computed(() => {
  return inputMessage.value.trim().length > 0
})

const formatMessageTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }
  
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleKeydown = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const sendMessage = () => {
  if (!canSend.value) return

  const message = {
    id: Date.now(),
    content: inputMessage.value.trim(),
    senderId: currentUser.value.id,
    senderName: currentUser.value.name,
    senderAvatar: currentUser.value.avatar,
    timestamp: Date.now(),
    isOwn: true,
    isRead: false
  }

  emit('send-message', message)
  inputMessage.value = ''

  nextTick(() => {
    scrollToBottom()
  })
}

const scrollToBottom = () => {
  if (messageAreaRef.value) {
    messageAreaRef.value.scrollTop = messageAreaRef.value.scrollHeight
  }
}
</script>

<style scoped lang="scss">
.chat-panel {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: #fff;
}

.chat-header {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 16px;
  border-bottom: 1px solid #f0f0f0;
  gap: 12px;

  .header-info {
    flex: 1;
    
    .header-name {
      font-size: 16px;
      font-weight: 500;
      color: #262626;
      margin-bottom: 2px;
    }
    
    .header-status {
      font-size: 12px;
      color: #52c41a;
    }
  }

  .header-actions {
    display: flex;
    gap: 4px;
  }
}

.message-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f5f5;

  .message-item {
    display: flex;
    margin-bottom: 20px;
    padding: 0 16px;
    
    &.isOwn {
      flex-direction: row-reverse;
    }
  }

  .message-content {
    display: flex;
    flex-direction: column;
    max-width: 60%;
  }

  .sender-name {
    font-size: 12px;
    color: #8c8c8c;
    margin-bottom: 4px;
  }

  .message-bubble {
    padding: 12px 16px;
    border-radius: 8px;
    word-wrap: break-word;
    word-break: break-all;
    line-height: 1.6;
    font-size: 14px;
    
    &:not(.isOwn) {
      background-color: #ffffff;
      color: #262626;
      border-radius: 0 8px 8px 8px;
    }
    
    &.isOwn {
      background-color: #0089ff;
      color: #ffffff;
      border-radius: 8px 0 8px 8px;
    }
  }

  .message-time {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 11px;
    color: #8c8c8c;
    margin-top: 4px;

    .read-status {
      font-size: 11px;
      color: #52c41a;
    }
  }

  .time-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 16px 0;
    
    span {
      background: rgba(0, 0, 0, 0.04);
      padding: 4px 12px;
      border-radius: 4px;
      font-size: 12px;
      color: #8c8c8c;
    }
  }
}

.chat-input {
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

.input-toolbar {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-bottom: 1px solid #f0f0f0;
  gap: 4px;

  .toolbar-btn {
    width: 32px;
    height: 32px;
    border-radius: 4px;
    font-size: 18px;
    color: #666;
    transition: all 0.2s ease;

    &:hover {
      color: #1677ff;
      background: rgba(0, 0, 0, 0.04);
    }
  }
}

.input-area-wrapper {
  display: flex;
  align-items: flex-end;
  padding: 8px 16px 12px;
  gap: 12px;
}

.chat-input {
  flex: 1;

  :deep(.el-textarea__inner) {
    border: none !important;
    padding: 8px 0 !important;
    resize: none !important;
    font-size: 14px !important;
    line-height: 1.5 !important;
    background: transparent !important;
    box-shadow: none !important;
    color: #262626 !important;

    &::placeholder {
      color: #bfbfbf !important;
    }
  }
}

.send-button-wrapper {
  flex-shrink: 0;
  margin-bottom: 2px;

  .el-button {
    min-width: 80px;
    height: 32px;
    border-radius: 4px;
    font-size: 14px;
  }
}
</style>
