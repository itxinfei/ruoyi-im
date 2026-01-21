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

    <div v-loading="loading" class="message-area" ref="messageAreaRef">
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

      <div v-if="!loading && messages.length === 0" class="empty-state">
        <el-empty description="暂无消息，开始聊天吧" />
      </div>
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
            :loading="sending"
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
import { ref, computed, nextTick, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Phone,
  VideoCamera,
  MoreFilled,
  ChatDotRound,
  Folder,
  Picture,
  Promotion
} from '@element-plus/icons-vue'
import { getMessages, sendMessage as sendMessageApi, markAsRead } from '@/api/im/message'
import { useImWebSocket } from '@/composables/useImWebSocket'

const props = defineProps({
  session: {
    type: Object,
    default: null
  }
})

const currentUser = ref({
  id: null,
  name: '用户',
  avatar: ''
})

const inputMessage = ref('')
const messageAreaRef = ref(null)
const messages = ref([])
const loading = ref(false)
const sending = ref(false)

// WebSocket
const { sendMessage: wsSendMessage, onMessage } = useImWebSocket()

// 加载用户信息
const loadUserInfo = () => {
  const userInfoStr = localStorage.getItem('user_info')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      currentUser.value = {
        id: userInfo.userId || userInfo.id,
        name: userInfo.userName || userInfo.name || '用户',
        avatar: userInfo.avatar || ''
      }
    } catch (error) {
      console.error('解析用户信息失败:', error)
    }
  }
}

// 加载消息列表
const loadMessages = async () => {
  if (!props.session?.id) return
  
  loading.value = true
  try {
    const response = await getMessages({
      conversationId: props.session.id,
      pageSize: 50
    })
    
    if (response && response.data) {
      messages.value = response.data.map(item => ({
        id: item.messageId || item.id,
        content: item.content,
        senderId: item.senderId,
        senderName: item.senderName || '未知用户',
        senderAvatar: item.senderAvatar || '',
        timestamp: item.createTime || item.timestamp,
        isOwn: item.senderId === currentUser.value.id,
        isRead: item.isRead || false,
        messageType: item.messageType || 'TEXT'
      }))
      
      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
      
      // 标记已读
      if (messages.value.length > 0) {
        const lastMessage = messages.value[messages.value.length - 1]
        markAsRead({
          conversationId: props.session.id,
          messageId: lastMessage.id
        }).catch(err => console.error('标记已读失败:', err))
      }
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

// 消息带时间分割线
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

const sendMessage = async () => {
  if (!canSend.value || !props.session?.id) return

  const content = inputMessage.value.trim()
  sending.value = true

  try {
    // 调用 API 发送消息
    const response = await sendMessageApi({
      conversationId: props.session.id,
      messageType: 'TEXT',
      content: content
    })

    if (response && response.data) {
      // 添加到消息列表
      const newMessage = {
        id: response.data.messageId || response.data.id || Date.now(),
        content: content,
        senderId: currentUser.value.id,
        senderName: currentUser.value.name,
        senderAvatar: currentUser.value.avatar,
        timestamp: response.data.createTime || Date.now(),
        isOwn: true,
        isRead: false,
        messageType: 'TEXT'
      }

      messages.value.push(newMessage)
      inputMessage.value = ''

      // 通过 WebSocket 发送消息通知
      wsSendMessage({
        type: 'message',
        data: {
          conversationId: props.session.id,
          messageId: newMessage.id,
          content: content,
          messageType: 'TEXT'
        }
      })

      nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  if (messageAreaRef.value) {
    messageAreaRef.value.scrollTop = messageAreaRef.value.scrollHeight
  }
}

// 监听 WebSocket 消息
onMessage((message) => {
  // 只处理当前会话的消息
  if (message.conversationId === props.session?.id) {
    const newMessage = {
      id: message.messageId || message.id || Date.now(),
      content: message.content,
      senderId: message.senderId,
      senderName: message.senderName || '未知用户',
      senderAvatar: message.senderAvatar || '',
      timestamp: message.timestamp || Date.now(),
      isOwn: message.senderId === currentUser.value.id,
      isRead: false,
      messageType: message.messageType || 'TEXT'
    }

    messages.value.push(newMessage)

    nextTick(() => {
      scrollToBottom()
    })
  }
})

// 监听会话变化
watch(() => props.session, (newSession) => {
  if (newSession) {
    messages.value = []
    loadMessages()
  }
}, { immediate: true })

// 组件挂载时加载用户信息
onMounted(() => {
  loadUserInfo()
})
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

  .empty-state {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
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
