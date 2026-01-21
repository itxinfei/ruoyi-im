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
      <!-- 文件上传组件（隐藏） -->
      <FileUpload
        ref="fileUploadRef"
        type="file"
        accept="*"
        :max-size="100"
        @success="handleFileUploadSuccess"
        @error="handleUploadError"
      />

      <!-- 图片上传组件（隐藏） -->
      <FileUpload
        ref="imageUploadRef"
        type="image"
        accept="image/*"
        :max-size="10"
        @success="handleImageUploadSuccess"
        @error="handleUploadError"
      />

      <div class="input-toolbar">
        <div class="emoji-picker-wrapper">
          <el-tooltip content="表情">
            <el-button :icon="ChatDotRound" text class="toolbar-btn" @click="toggleEmojiPicker" />
          </el-tooltip>
          <EmojiPicker
            v-if="showEmojiPicker"
            @select="selectEmoji"
            @click.stop
          />
        </div>
        <el-tooltip content="上传文件">
          <el-button :icon="Folder" text class="toolbar-btn" @click="handleUploadFile" />
        </el-tooltip>
        <el-tooltip content="上传图片">
          <el-button :icon="Picture" text class="toolbar-btn" @click="handleUploadImage" />
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

    <!-- 转发对话框 -->
    <ForwardDialog
      ref="forwardDialogRef"
      @forward="handleForwardMessage"
    />
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Phone,
  VideoCamera,
  MoreFilled,
  ChatDotRound,
  Folder,
  Picture,
  Promotion,
  TopRight,
  Close,
  DocumentCopy,
  RefreshLeft,
  Delete
} from '@element-plus/icons-vue'
import { getMessages, sendMessage as sendMessageApi, markAsRead, recallMessage, deleteMessage } from '@/api/im/message'
import { useImWebSocket } from '@/composables/useImWebSocket'
import FileUpload from '@/components/FileUpload/index.vue'
import ForwardDialog from '@/components/ForwardDialog/index.vue'
import EmojiPicker from '@/components/EmojiPicker/index.vue'

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
const replyingTo = ref(null)
const contextMessage = ref(null)
const contextMenuRef = ref(null)
const fileUploadRef = ref(null)
const imageUploadRef = ref(null)
const forwardDialogRef = ref(null)
const showEmojiPicker = ref(false)

// WebSocket
const { sendMessage: wsSendMessage, onMessage } = useImWebSocket()

// 处理右键菜单
const handleContextMenu = (event, message) => {
  contextMessage.value = message
  // 使用 Element Plus 的 dropdown 组件显示菜单
  const dropdown = contextMenuRef.value
  if (dropdown) {
    dropdown.handleOpen()
  }
}

// 处理菜单命令
const handleMenuCommand = async (command) => {
  const message = contextMessage.value
  if (!message) return

  switch (command) {
    case 'reply':
      handleReply(message)
      break
    case 'forward':
      handleForward(message)
      break
    case 'copy':
      handleCopy(message)
      break
    case 'recall':
      await handleRecall(message)
      break
    case 'delete':
      await handleDelete(message)
      break
  }
}

// 回复消息
const handleReply = (message) => {
  replyingTo.value = message
  ElMessage.info(`回复 ${message.senderName}`)
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
}

// 转发消息
const handleForward = (message) => {
  forwardDialogRef.value?.open(message)
}

// 切换表情选择器显示状态
const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

// 选择表情
const selectEmoji = (emoji) => {
  inputMessage.value += emoji
  showEmojiPicker.value = false
}

// 处理转发消息
const handleForwardMessage = async ({ message, targetSessionId }) => {
  try {
    sending.value = true

    // 调用 API 转发消息
    const response = await sendMessageApi({
      conversationId: targetSessionId,
      messageType: message.messageType,
      content: message.content,
      forwardFromMessageId: message.id
    })

    if (response && response.data) {
      ElMessage.success('转发成功')

      // 如果转发到当前会话，显示在消息列表中
      if (targetSessionId === props.session.id) {
        const newMessage = {
          id: response.data.messageId || response.data.id || Date.now(),
          content: message.content,
          messageType: message.messageType,
          fileData: message.fileData,
          imageData: message.imageData,
          forwardFrom: message,
          senderId: currentUser.value.id,
          senderName: currentUser.value.name,
          senderAvatar: currentUser.value.avatar,
          timestamp: response.data.createTime || Date.now(),
          isOwn: true,
          isRead: false
        }

        messages.value.push(newMessage)

        nextTick(() => {
          scrollToBottom()
        })
      }
    }
  } catch (error) {
    console.error('转发消息失败:', error)
    ElMessage.error('转发失败')
  } finally {
    sending.value = false
  }
}

// 复制消息
const handleCopy = async (message) => {
  try {
    await navigator.clipboard.writeText(message.content)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

// 撤回消息
const handleRecall = async (message) => {
  try {
    // 检查是否在2分钟内
    const timeDiff = Date.now() - message.timestamp
    if (timeDiff > 2 * 60 * 1000) {
      ElMessage.warning('只能撤回2分钟内的消息')
      return
    }

    await ElMessageBox.confirm(
      '确定要撤回这条消息吗？',
      '撤回消息',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await recallMessage(message.id)
    
    // 更新消息为已撤回状态
    const index = messages.value.findIndex(m => m.id === message.id)
    if (index !== -1) {
      messages.value[index].content = '你撤回了一条消息'
      messages.value[index].isRecalled = true
    }

    ElMessage.success('消息已撤回')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回消息失败:', error)
      ElMessage.error('撤回失败')
    }
  }
}

// 删除消息
const handleDelete = async (message) => {
  try {
    await ElMessageBox.confirm(
      '删除后无法恢复，确定要删除这条消息吗？',
      '删除消息',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteMessage(message.id)
    
    // 从列表中移除消息
    messages.value = messages.value.filter(m => m.id !== message.id)

    ElMessage.success('消息已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除消息失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 上传文件
const handleUploadFile = () => {
  fileUploadRef.value?.triggerUpload()
}

// 上传图片
const handleUploadImage = () => {
  imageUploadRef.value?.triggerUpload()
}

// 文件上传成功
const handleFileUploadSuccess = async ({ data }) => {
  try {
    sending.value = true

    // 调用 API 发送文件消息
    const response = await sendMessageApi({
      conversationId: props.session.id,
      messageType: 'FILE',
      content: JSON.stringify({
        fileName: data.fileName,
        fileSize: data.fileSize,
        fileUrl: data.fileUrl
      })
    })

    if (response && response.data) {
      // 添加到消息列表
      const newMessage = {
        id: response.data.messageId || response.data.id || Date.now(),
        content: `[文件] ${data.fileName}`,
        messageType: 'FILE',
        fileData: {
          fileName: data.fileName,
          fileSize: data.fileSize,
          fileUrl: data.fileUrl
        },
        senderId: currentUser.value.id,
        senderName: currentUser.value.name,
        senderAvatar: currentUser.value.avatar,
        timestamp: response.data.createTime || Date.now(),
        isOwn: true,
        isRead: false
      }

      messages.value.push(newMessage)

      // 通过 WebSocket 发送消息通知
      wsSendMessage({
        type: 'message',
        data: {
          conversationId: props.session.id,
          messageId: newMessage.id,
          messageType: 'FILE',
          content: JSON.stringify({
            fileName: data.fileName,
            fileSize: data.fileSize,
            fileUrl: data.fileUrl
          })
        }
      })

      nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    console.error('发送文件消息失败:', error)
    ElMessage.error('发送文件消息失败')
  } finally {
    sending.value = false
  }
}

// 图片上传成功
const handleImageUploadSuccess = async ({ data }) => {
  try {
    sending.value = true

    // 调用 API 发送图片消息
    const response = await sendMessageApi({
      conversationId: props.session.id,
      messageType: 'IMAGE',
      content: JSON.stringify({
        imageUrl: data.fileUrl,
        width: data.width,
        height: data.height
      })
    })

    if (response && response.data) {
      // 添加到消息列表
      const newMessage = {
        id: response.data.messageId || response.data.id || Date.now(),
        content: '[图片]',
        messageType: 'IMAGE',
        imageData: {
          imageUrl: data.fileUrl,
          width: data.width,
          height: data.height
        },
        senderId: currentUser.value.id,
        senderName: currentUser.value.name,
        senderAvatar: currentUser.value.avatar,
        timestamp: response.data.createTime || Date.now(),
        isOwn: true,
        isRead: false
      }

      messages.value.push(newMessage)

      // 通过 WebSocket 发送消息通知
      wsSendMessage({
        type: 'message',
        data: {
          conversationId: props.session.id,
          messageId: newMessage.id,
          messageType: 'IMAGE',
          content: JSON.stringify({
            imageUrl: data.fileUrl,
            width: data.width,
            height: data.height
          })
        }
      })

      nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    console.error('发送图片消息失败:', error)
    ElMessage.error('发送图片消息失败')
  } finally {
    sending.value = false
  }
}

// 上传错误处理
const handleUploadError = ({ error }) => {
  console.error('上传失败:', error)
  ElMessage.error(error || '上传失败')
}
  try {
    sending.value = true

    // 调用 API 发送图片消息
    const response = await sendMessageApi({
      conversationId: props.session.id,
      messageType: 'IMAGE',
      content: JSON.stringify({
        imageUrl: data.fileUrl,
        width: data.width,
        height: data.height
      })
    })

    if (response && response.data) {
      // 添加到消息列表
      const newMessage = {
        id: response.data.messageId || response.data.id || Date.now(),
        content: '[图片]',
        messageType: 'IMAGE',
        imageData: {
          imageUrl: data.fileUrl,
          width: data.width,
          height: data.height
        },
        senderId: currentUser.value.id,
        senderName: currentUser.value.name,
        senderAvatar: currentUser.value.avatar,
        timestamp: response.data.createTime || Date.now(),
        isOwn: true,
        isRead: false
      }

      messages.value.push(newMessage)

      // 通过 WebSocket 发送消息通知
      wsSendMessage({
        type: 'message',
        data: {
          conversationId: props.session.id,
          messageId: newMessage.id,
          messageType: 'IMAGE',
          content: JSON.stringify({
            imageUrl: data.fileUrl,
            width: data.width,
            height: data.height
          })
        }
      })

      nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    console.error('发送图片消息失败:', error)
    ElMessage.error('发送图片消息失败')
  } finally {
    sending.value = false
  }
}

// 滚动到指定消息
const scrollToMessage = (messageId) => {
  // TODO: 实现滚动到指定消息
  console.log('滚动到消息:', messageId)
}

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
      content: content,
      replyToMessageId: replyingTo.value?.id // 回复消息ID
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
        messageType: 'TEXT',
        replyTo: replyingTo.value ? {
          id: replyingTo.value.id,
          senderName: replyingTo.value.senderName,
          content: replyingTo.value.content
        } : null
      }

      messages.value.push(newMessage)
      inputMessage.value = ''
      replyingTo.value = null // 清除回复状态

      // 通过 WebSocket 发送消息通知
      wsSendMessage({
        type: 'message',
        data: {
          conversationId: props.session.id,
          messageId: newMessage.id,
          content: content,
          messageType: 'TEXT',
          replyToMessageId: newMessage.replyTo?.id
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
