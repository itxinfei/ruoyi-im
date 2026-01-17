<template>
  <div class="ding-chat-container">
    <!-- 聊天头部 -->
    <div class="ding-chat-header" v-if="currentSession">
      <div class="header-left">
        <DtAvatar
          :name="currentSession.name || '对话'"
          :avatar="currentSession.avatar"
          :size="36"
          class="session-avatar"
        />
        <div class="session-info">
          <div class="session-name">{{ currentSession.name || '对话' }}</div>
          <div class="session-status">
            <span v-if="isGroupChat" class="group-info">
              <el-icon><User /></el-icon>
              {{ memberCount }}人
            </span>
            <span v-else class="online-status" :class="{ online: isUserOnline }">
              <span class="status-dot"></span>
              {{ isUserOnline ? '在线' : '离线' }}
            </span>
          </div>
        </div>
      </div>
      
      <div class="header-right">
        <el-tooltip content="语音通话" placement="bottom">
          <el-button :icon="Phone" text class="header-action-btn" @click="startVoiceCall" />
        </el-tooltip>
        <el-tooltip content="视频通话" placement="bottom">
          <el-button :icon="VideoCamera" text class="header-action-btn" @click="startVideoCall" />
        </el-tooltip>
        <el-tooltip content="群组设置" placement="bottom" v-if="isGroupChat">
          <el-button :icon="Setting" text class="header-action-btn" @click="showGroupSettings" />
        </el-tooltip>
        <el-dropdown trigger="click" placement="bottom-end" @command="handleMoreCommand">
          <el-button :icon="MoreFilled" text class="header-action-btn" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="search">搜索聊天记录</el-dropdown-item>
              <el-dropdown-item command="clear">清空聊天记录</el-dropdown-item>
              <el-dropdown-item command="mute">消息免打扰</el-dropdown-item>
              <el-dropdown-item divided command="report">举报</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 消息列表 -->
    <div 
      ref="messageListRef" 
      class="ding-message-list"
      @scroll="handleScroll"
      @contextmenu.prevent
    >
      <!-- 加载更多提示 -->
      <div v-if="loadingMore" class="loading-more">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载更多消息...</span>
      </div>
      
      <!-- 消息日期分隔符 -->
      <div v-for="(date, index) in messageDates" :key="`date-${index}`" class="message-date-divider">
        <div class="date-line"></div>
        <div class="date-text">{{ date }}</div>
        <div class="date-line"></div>
      </div>

      <!-- 消息列表 -->
      <div 
        v-for="msg in displayedMessages" 
        :key="msg.id || msg.tempId"
        class="ding-message-item"
        :class="{ 
          'is-own': msg.senderId === currentUserId,
          'is-system': msg.type === 'system'
        }"
        :data-message-id="msg.id || msg.tempId"
      >
        <!-- 系统消息 -->
        <div v-if="msg.type === 'system'" class="system-message">
          {{ msg.content }}
        </div>
        
        <!-- 普通消息 -->
        <div v-else class="message-wrapper">
          <!-- 对方头像 -->
          <DtAvatar
            v-if="msg.senderId !== currentUserId"
            :name="msg.senderName || msg.sender?.name || '用户'"
            :avatar="msg.senderAvatar || msg.avatar"
            :size="40"
            class="message-avatar"
          />
          
          <div class="message-content-wrapper">
            <!-- 发送者名称（群聊显示） -->
            <div 
              v-if="isGroupChat && msg.senderId !== currentUserId" 
              class="sender-name"
            >
              {{ msg.senderName || msg.sender?.name }}
            </div>
            
            <!-- 消息气泡 -->
            <div 
              class="message-bubble"
              :class="{ 
                'is-own': msg.senderId === currentUserId,
                'sending': msg.sendStatus === 'sending',
                'failed': msg.sendStatus === 'failed'
              }"
              @contextmenu.prevent="showMessageMenu($event, msg)"
            >
              <!-- 引用回复 -->
              <div v-if="msg.replyTo" class="message-reply">
                <div class="reply-quote"></div>
                <div class="reply-content">
                  <div class="reply-sender">{{ msg.replyTo.senderName }}</div>
                  <div class="reply-text">{{ getReplyText(msg.replyTo) }}</div>
                </div>
              </div>
              
              <!-- 文本消息 -->
              <div v-if="msg.type === 'text' || !msg.type" class="text-content">
                {{ msg.content }}
              </div>
              
              <!-- 图片消息 -->
              <div v-else-if="msg.type === 'image'" class="image-message">
                <el-image
                  :src="getImageUrl(msg)"
                  :preview-src-list="[getImageUrl(msg)]"
                  fit="cover"
                  class="image-content"
                  :initial-index="0"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                      <span>图片加载失败</span>
                    </div>
                  </template>
                </el-image>
              </div>
              
              <!-- 文件消息 -->
              <div v-else-if="msg.type === 'file'" class="file-message">
                <div class="file-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="file-info">
                  <div class="file-name">{{ getFileInfo(msg).name }}</div>
                  <div class="file-size">{{ formatFileSize(getFileInfo(msg).size) }}</div>
                </div>
                <el-button type="primary" size="small" @click="downloadFile(msg)">
                  下载
                </el-button>
              </div>
              
              <!-- 语音消息 -->
              <div v-else-if="msg.type === 'voice'" class="voice-message">
                <el-button :icon="VideoPlay" circle @click="playVoice(msg)" />
                <span class="voice-duration">{{ msg.duration || 0 }}''</span>
              </div>
              
              <!-- 消息状态图标 -->
              <div class="message-status">
                <el-icon v-if="msg.sendStatus === 'sending'" class="is-loading">
                  <Loading />
                </el-icon>
                <el-icon v-else-if="msg.sendStatus === 'failed'" color="#ff4d4f">
                  <Warning />
                </el-icon>
                <el-icon v-else-if="msg.senderId === currentUserId" color="#52c41a">
                  <Check />
                </el-icon>
              </div>
            </div>
            
            <!-- 消息时间 -->
            <div class="message-time">
              {{ formatMessageTime(msg.timestamp || msg.time) }}
            </div>
          </div>
          
          <!-- 自己头像 -->
          <DtAvatar
            v-if="msg.senderId === currentUserId"
            :name="currentUserInfo.nickName || currentUserInfo.userName || '我'"
            :avatar="currentUserInfo.avatar"
            :size="40"
            class="message-avatar"
          />
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="ding-input-area">
      <!-- 工具栏 -->
      <div class="input-toolbar">
        <el-tooltip content="表情">
          <el-button :icon="ChatDotRound" text @click="showEmojiPicker" />
        </el-tooltip>
        <el-tooltip content="发送图片">
          <el-button :icon="Picture" text @click="selectImage" />
        </el-tooltip>
        <el-tooltip content="发送文件">
          <el-button :icon="Document" text @click="selectFile" />
        </el-tooltip>
        <el-tooltip content="语音输入">
          <el-button :icon="Microphone" text @click="toggleVoiceInput" />
        </el-tooltip>
        <el-tooltip content="截图">
          <el-button :icon="Crop" text @click="takeScreenshot" />
        </el-tooltip>
        <el-tooltip content="@提及" v-if="isGroupChat">
          <el-button :icon="User" text @click="showMentionSelector" />
        </el-tooltip>
      </div>
      
      <!-- 输入框 -->
      <div class="input-wrapper">
        <textarea
          ref="messageInputRef"
          v-model="inputMessage"
          placeholder="输入消息... (Enter发送，Shift+Enter换行)"
          class="message-input"
          rows="1"
          @keydown="handleKeyDown"
          @input="handleInput"
          @paste="handlePaste"
        ></textarea>
        <el-button 
          type="primary" 
          :disabled="!canSend"
          :loading="sending"
          class="send-button"
          @click="sendMessage"
        >
          发送
        </el-button>
      </div>
    </div>

    <!-- 表情选择器 -->
    <EmojiPicker 
      v-if="emojiPickerVisible"
      :visible="emojiPickerVisible"
      @select="insertEmoji"
      @close="emojiPickerVisible = false"
    />

    <!-- 引用回复组件 -->
    <QuoteMessage
      v-if="replyingTo"
      :message="replyingTo"
      @cancel="cancelReply"
    />

    <!-- 消息右键菜单 -->
    <MessageMenu
      v-if="messageMenuVisible"
      :message="selectedMessage"
      :position="menuPosition"
      @close="messageMenuVisible = false"
      @action="handleMessageAction"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStore } from 'vuex'
import { 
  Phone, VideoCamera, Setting, MoreFilled, 
  Loading, Picture, Document, VideoPlay, Warning, Check,
  ChatDotRound, User, Crop, Microphone
} from '@element-plus/icons-vue'
import DtAvatar from '@/components/DtAvatar.vue'
import EmojiPicker from '@/components/EmojiPicker.vue'
import QuoteMessage from '@/components/QuoteMessage.vue'
import MessageMenu from '@/components/MessageMenu.vue'
import { getCurrentUserId, getCurrentUserInfo } from '@/utils/im-user'

// 响应式数据
const store = useStore()
const inputMessage = ref('')
const messageListRef = ref(null)
const messageInputRef = ref(null)
const loadingMore = ref(false)
const sending = ref(false)
const emojiPickerVisible = ref(false)
const replyingTo = ref(null)
const messageMenuVisible = ref(false)
const selectedMessage = ref(null)
const menuPosition = ref({ x: 0, y: 0 })

// 计算属性
const currentSession = computed(() => store.state.im?.currentSession)
const currentUserId = computed(() => getCurrentUserId())
const currentUserInfo = computed(() => getCurrentUserInfo())
const displayedMessages = computed(() => {
  const sessionId = currentSession.value?.id
  if (!sessionId) return []
  return store.getters['im/messagesBySession'](sessionId) || []
})

const isGroupChat = computed(() => 
  currentSession.value?.type === 'GROUP' || currentSession.value?.type === 'group'
)

const isUserOnline = computed(() => {
  if (isGroupChat.value) return false
  const targetUserId = currentSession.value?.targetId
  return store.state.im?.onlineStatus?.[targetUserId]
})

const memberCount = computed(() => currentSession.value?.memberCount || 0)

const canSend = computed(() => 
  inputMessage.value.trim().length > 0 && 
  currentSession.value && 
  !sending.value
)

// 消息日期分组
const messageDates = computed(() => {
  const dates = new Set()
  displayedMessages.value.forEach(msg => {
    const date = new Date(msg.timestamp || msg.time).toDateString()
    dates.add(date)
  })
  return Array.from(dates)
})

// 监听会话变化
watch(currentSession, async (newSession) => {
  if (newSession?.id) {
    await nextTick()
    scrollToBottom()
    // 标记消息为已读
    store.dispatch('im/markAsRead', { sessionId: newSession.id })
  }
}, { immediate: true })

// 监听消息变化，自动滚动到底部
watch(displayedMessages, () => {
  nextTick(() => {
    if (!loadingMore.value) {
      scrollToBottom()
    }
  })
}, { deep: true })

// 方法
function startVoiceCall() {
  ElMessage.info('语音通话功能开发中...')
}

function startVideoCall() {
  ElMessage.info('视频通话功能开发中...')
}

function showGroupSettings() {
  ElMessage.info('群组设置功能开发中...')
}

function handleMoreCommand(command) {
  switch (command) {
    case 'search':
      ElMessage.info('搜索功能开发中...')
      break
    case 'clear':
      clearChatHistory()
      break
    case 'mute':
      toggleMute()
      break
    case 'report':
      ElMessage.info('举报功能开发中...')
      break
  }
}

async function clearChatHistory() {
  try {
    await ElMessageBox.confirm('确定要清空聊天记录吗？此操作不可恢复。', '确认清空', {
      type: 'warning'
    })
    ElMessage.success('聊天记录已清空')
  } catch {
    // 用户取消
  }
}

function toggleMute() {
  const session = currentSession.value
  const isMuted = session?.isMuted || false
  store.dispatch('im/updateSession', {
    sessionId: session.id,
    updates: { isMuted: !isMuted }
  })
  ElMessage.success(isMuted ? '已开启消息提醒' : '已设置消息免打扰')
}

function handleKeyDown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function handleInput() {
  // 自动调整输入框高度
  const textarea = messageInputRef.value
  if (textarea) {
    textarea.style.height = 'auto'
    textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
  }
}

function handlePaste(e) {
  // 处理图片粘贴
  const items = e.clipboardData?.items
  if (items) {
    for (let i = 0; i < items.length; i++) {
      if (items[i].type.indexOf('image') !== -1) {
        e.preventDefault()
        const file = items[i].getAsFile()
        if (file) {
          sendImage(file)
        }
      }
    }
  }
}

async function sendMessage() {
  const text = inputMessage.value.trim()
  if (!text || !currentSession.value) return

  sending.value = true
  
  try {
    await store.dispatch('im/sendMessage', {
      sessionId: currentSession.value.id,
      type: 'text',
      content: text,
      replyToMessageId: replyingTo.value?.id,
    })
    
    inputMessage.value = ''
    cancelReply()
    
    // 重置输入框高度
    const textarea = messageInputRef.value
    if (textarea) {
      textarea.style.height = 'auto'
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('消息发送失败，请重试')
  } finally {
    sending.value = false
  }
}

async function sendImage(file) {
  if (!currentSession.value) return
  
  try {
    // 这里应该先上传图片，然后发送消息
    ElMessage.info('图片发送功能开发中...')
  } catch (error) {
    ElMessage.error('图片发送失败')
  }
}

function selectImage() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      sendImage(file)
    }
  }
  input.click()
}

function selectFile() {
  const input = document.createElement('input')
  input.type = 'file'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      sendFile(file)
    }
  }
  input.click()
}

async function sendFile(file) {
  if (!currentSession.value) return
  ElMessage.info('文件发送功能开发中...')
}

function toggleVoiceInput() {
  ElMessage.info('语音输入功能开发中...')
}

function takeScreenshot() {
  ElMessage.info('截图功能开发中...')
}

function showMentionSelector() {
  ElMessage.info('@提及功能开发中...')
}

function showEmojiPicker() {
  emojiPickerVisible.value = true
}

function insertEmoji(emoji) {
  const textarea = messageInputRef.value
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const text = inputMessage.value
    inputMessage.value = text.substring(0, start) + emoji + text.substring(end)
    nextTick(() => {
      textarea.selectionStart = textarea.selectionEnd = start + emoji.length
      textarea.focus()
    })
  }
}

function cancelReply() {
  replyingTo.value = null
}

function showMessageMenu(event, message) {
  selectedMessage.value = message
  menuPosition.value = { x: event.clientX, y: event.clientY }
  messageMenuVisible.value = true
}

function handleMessageAction(action) {
  const message = selectedMessage.value
  switch (action) {
    case 'reply':
      replyingTo.value = message
      messageInputRef.value?.focus()
      break
    case 'copy':
      copyMessage(message)
      break
    case 'delete':
      deleteMessage(message)
      break
    case 'recall':
      recallMessage(message)
      break
    case 'forward':
      forwardMessage(message)
      break
  }
  messageMenuVisible.value = false
}

function copyMessage(message) {
  navigator.clipboard.writeText(message.content)
    .then(() => ElMessage.success('已复制到剪贴板'))
    .catch(() => ElMessage.error('复制失败'))
}

async function deleteMessage(message) {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '确认删除', {
      type: 'warning'
    })
    await store.dispatch('im/deleteMessage', {
      sessionId: currentSession.value.id,
      messageId: message.id
    })
    ElMessage.success('消息已删除')
  } catch {
    // 用户取消或删除失败
  }
}

async function recallMessage(message) {
  try {
    await store.dispatch('im/recallMessage', {
      sessionId: currentSession.value.id,
      messageId: message.id
    })
    ElMessage.success('消息已撤回')
  } catch (error) {
    ElMessage.error('撤回失败：' + error.message)
  }
}

function forwardMessage(message) {
  ElMessage.info('转发功能开发中...')
}

function playVoice(message) {
  ElMessage.info('语音播放功能开发中...')
}

function downloadFile(message) {
  ElMessage.info('文件下载功能开发中...')
}

function getImageUrl(message) {
  if (typeof message.content === 'string') {
    return message.content
  } else if (message.content?.url) {
    return message.content.url
  }
  return message.url || ''
}

function getFileInfo(message) {
  if (typeof message.content === 'object') {
    return message.content
  }
  return { name: '未知文件', size: 0 }
}

function getReplyText(replyTo) {
  if (typeof replyTo.content === 'string') {
    return replyTo.content.length > 50 
      ? replyTo.content.substring(0, 50) + '...' 
      : replyTo.content
  }
  return '[消息]'
}

function formatFileSize(size) {
  if (!size) return ''
  const units = ['B', 'KB', 'MB', 'GB']
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return size.toFixed(1) + units[unitIndex]
}

function formatMessageTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  
  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  } else {
    return date.toLocaleDateString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

function scrollToBottom() {
  if (messageListRef.value) {
    const scrollContainer = messageListRef.value
    scrollContainer.scrollTop = scrollContainer.scrollHeight
  }
}

async function handleScroll() {
  if (!messageListRef.value || loadingMore.value) return
  
  const { scrollTop, scrollHeight, clientHeight } = messageListRef.value
  
  // 滚动到顶部时加载更多
  if (scrollTop === 0 && hasMoreMessages.value) {
    await loadMoreMessages()
  }
}

const hasMoreMessages = computed(() => {
  const sessionId = currentSession.value?.id
  const messages = store.getters['im/messagesBySession'](sessionId) || []
  return messages.length >= 20 // 假设每页20条
})

async function loadMoreMessages() {
  if (!currentSession.value?.id || loadingMore.value) return
  
  loadingMore.value = true
  try {
    const messages = store.getters['im/messagesBySession'](currentSession.value.id) || []
    const oldestMessage = messages[0]
    
    await store.dispatch('im/loadMessages', {
      sessionId: currentSession.value.id,
      lastId: oldestMessage?.id,
      pageSize: 20,
    })
  } catch (error) {
    console.error('加载更多消息失败:', error)
  } finally {
    loadingMore.value = false
  }
}

// 生命周期
onMounted(async () => {
  // 加载会话列表
  await store.dispatch('im/loadSessions')
  
  // 组件挂载后聚焦输入框
  nextTick(() => {
    messageInputRef.value?.focus()
  })
})

onUnmounted(() => {
  // 清理定时器等
})
</script>

<style scoped>
.ding-chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f5f5;
  position: relative;
}

/* 聊天头部 */
.ding-chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.session-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.session-name {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.session-status {
  font-size: 12px;
  color: #8c8c8c;
}

.group-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.online-status {
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #d9d9d9;
}

.online-status.online .status-dot {
  background: #52c41a;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-action-btn {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  color: #595959;
}

.header-action-btn:hover {
  background: #f5f5f5;
  color: #262626;
}

/* 消息列表 */
.ding-message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 12px;
  scroll-behavior: smooth;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  color: #8c8c8c;
  font-size: 14px;
}

.message-date-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 16px 0;
}

.date-line {
  flex: 1;
  height: 1px;
  background: #e8e8e8;
}

.date-text {
  font-size: 12px;
  color: #8c8c8c;
  padding: 0 8px;
}

.ding-message-item {
  margin-bottom: 16px;
}

.ding-message-item.is-system {
  display: flex;
  justify-content: center;
}

.system-message {
  background: rgba(0, 0, 0, 0.05);
  color: #8c8c8c;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  max-width: 200px;
  text-align: center;
}

.message-wrapper {
  display: flex;
  gap: 8px;
}

.message-wrapper.is-own {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: calc(100% - 48px);
}

.message-wrapper.is-own .message-content-wrapper {
  align-items: flex-end;
}

.sender-name {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
  margin-left: 8px;
}

.message-bubble {
  position: relative;
  padding: 8px 12px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  word-wrap: break-word;
  max-width: 100%;
}

.message-wrapper.is-own .message-bubble {
  background: #1677ff;
  color: #fff;
}

.message-bubble.sending {
  opacity: 0.7;
}

.message-bubble.failed {
  background: #fff2f0;
  border: 1px solid #ffccc7;
}

.message-reply {
  display: flex;
  gap: 8px;
  margin-bottom: 4px;
  padding: 8px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 6px;
  border-left: 3px solid #1677ff;
}

.message-wrapper.is-own .message-reply {
  background: rgba(255, 255, 255, 0.2);
  border-left-color: #fff;
}

.reply-quote {
  width: 2px;
  background: #1677ff;
  border-radius: 1px;
}

.message-wrapper.is-own .reply-quote {
  background: #fff;
}

.reply-content {
  flex: 1;
}

.reply-sender {
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 2px;
}

.reply-text {
  font-size: 14px;
  opacity: 0.8;
}

.text-content {
  line-height: 1.4;
  white-space: pre-wrap;
}

.image-message {
  max-width: 200px;
}

.image-content {
  max-width: 100%;
  border-radius: 6px;
  cursor: pointer;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100px;
  background: #f5f5f5;
  border-radius: 6px;
  color: #8c8c8c;
}

.file-message {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #fafafa;
  border-radius: 6px;
  min-width: 200px;
}

.file-icon {
  font-size: 24px;
  color: #1677ff;
}

.file-info {
  flex: 1;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 2px;
}

.file-size {
  font-size: 12px;
  color: #8c8c8c;
}

.voice-message {
  display: flex;
  align-items: center;
  gap: 8px;
}

.voice-duration {
  font-size: 12px;
  color: #8c8c8c;
}

.message-status {
  display: flex;
  align-items: center;
  margin-left: 4px;
  font-size: 12px;
}

.message-time {
  font-size: 11px;
  color: #8c8c8c;
  margin-top: 2px;
  margin-left: 8px;
}

.message-wrapper.is-own .message-time {
  margin-right: 8px;
  margin-left: 0;
  text-align: right;
}

/* 输入区域 */
.ding-input-area {
  background: #fff;
  border-top: 1px solid #e8e8e8;
  padding: 12px 16px;
}

.input-toolbar {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 14px;
  line-height: 1.4;
  resize: none;
  outline: none;
  transition: border-color 0.2s;
  min-height: 36px;
  max-height: 120px;
}

.message-input:focus {
  border-color: #1677ff;
}

.send-button {
  flex-shrink: 0;
  height: 36px;
  padding: 0 20px;
}

/* 滚动条样式 */
.ding-message-list::-webkit-scrollbar {
  width: 6px;
}

.ding-message-list::-webkit-scrollbar-track {
  background: transparent;
}

.ding-message-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.ding-message-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>