<template>
  <div class="assistant-panel">
    <div class="panel-header">
      <h2 class="panel-title">AI 助理</h2>
      <button class="new-chat-btn" @click="startNewChat">
        <span class="material-icons-outlined">add_comment</span>
        新对话
      </button>
    </div>

    <div class="panel-content">
      <!-- 对话区域 -->
      <div v-if="currentChat || messages.length > 0" class="chat-area">
        <div class="messages-container" ref="messagesContainer">
          <!-- 欢迎消息 -->
          <div v-if="messages.length === 0" class="welcome-message">
            <div class="ai-avatar">
              <span class="material-icons-outlined">smart_toy</span>
            </div>
            <div class="message-content">
              <div class="message-text">你好，我是你的 AI 助理。有什么可以帮你的吗？</div>
            </div>
          </div>

          <!-- 消息列表 -->
          <div
            v-for="(message, index) in messages"
            :key="index"
            class="message-item"
            :class="message.role"
          >
            <div v-if="message.role === 'assistant'" class="ai-avatar-small">
              <span class="material-icons-outlined">smart_toy</span>
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(message.content)"></div>
              <div v-if="message.role === 'assistant'" class="message-actions">
                <button class="action-btn" @click="copyMessage(message.content)">
                  <span class="material-icons-outlined">content_copy</span>
                </button>
                <button class="action-btn" @click="regenerateMessage">
                  <span class="material-icons-outlined">refresh</span>
                </button>
              </div>
            </div>
            <div v-if="message.role === 'user'" class="user-avatar">
              {{ currentUserNickname?.charAt(0) || '我' }}
            </div>
          </div>

          <!-- 输入中状态 -->
          <div v-if="isTyping" class="message-item assistant typing">
            <div class="ai-avatar-small">
              <span class="material-icons-outlined">smart_toy</span>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-wrapper">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="1"
              :autosize="{ minRows: 1, maxRows: 4 }"
              placeholder="输入消息..."
              @keydown.enter.exact="sendMessage"
            />
            <button
              class="send-btn"
              :disabled="!inputMessage.trim() || isSending"
              @click="sendMessage"
            >
              <span class="material-icons-outlined">send</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 首页：快捷功能 -->
      <div v-else class="home-area">
        <!-- 欢迎区 -->
        <div class="welcome-section">
          <div class="ai-avatar-large">
            <span class="material-icons-outlined">smart_toy</span>
          </div>
          <h3 class="welcome-title">你好，我是你的 AI 助理</h3>
          <p class="welcome-desc">有什么可以帮你的吗？</p>
        </div>

        <!-- 快捷功能 -->
        <div class="quick-actions">
          <div class="section-title">快捷功能</div>
          <div class="action-grid">
            <div
              v-for="action in quickActions"
              :key="action.id"
              class="action-card"
              @click="handleQuickAction(action)"
            >
              <div class="action-icon" :style="{ background: action.bgColor }">
                <span class="material-icons-outlined">{{ action.icon }}</span>
              </div>
              <span class="action-label">{{ action.label }}</span>
            </div>
          </div>
        </div>

        <!-- 对话历史 -->
        <div v-if="chatHistory.length > 0" class="chat-section">
          <div class="section-title">最近对话</div>
          <div class="chat-list">
            <div
              v-for="chat in chatHistory"
              :key="chat.id"
              class="chat-item"
              @click="loadChat(chat)"
            >
              <div class="chat-icon">
                <span class="material-icons-outlined">chat_bubble</span>
              </div>
              <div class="chat-content">
                <div class="chat-title">{{ chat.title }}</div>
                <div class="chat-preview">{{ chat.preview }}</div>
              </div>
              <span class="chat-time">{{ chat.time }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { chat as chatApi } from '@/api/im/ai'

const store = useStore()
const messagesContainer = ref(null)

const currentUser = computed(() => store.getters['user/currentUser'] || {})
const currentUserNickname = computed(() => currentUser.value.nickname || currentUser.value.username || '我')

// 状态
const currentChat = ref(null)
const conversationId = ref(null)
const inputMessage = ref('')
const isSending = ref(false)
const isTyping = ref(false)

// 消息列表
const messages = ref([])

// 对话历史 - 暂时保留，后续可对接真实API
const chatHistory = ref([])

// 快捷功能
const quickActions = [
  { id: 1, label: '写文案', icon: 'edit', bgColor: '#1677ff', prompt: '帮我写一段关于' },
  { id: 2, label: '翻译', icon: 'translate', bgColor: '#52c41a', prompt: '请翻译以下内容：' },
  { id: 3, label: '总结', icon: 'summarize', bgColor: '#fa8c16', prompt: '请帮我总结以下内容：' },
  { id: 4, label: '代码', icon: 'code', bgColor: '#722ed1', prompt: '请帮我写一段代码：' },
  { id: 5, label: '分析', icon: 'analytics', bgColor: '#eb2f96', prompt: '请帮我分析：' },
  { id: 6, label: '创意', icon: 'lightbulb', bgColor: '#8b5cf6', prompt: '请给我一些创意想法：' }
]

// 生成会话ID
const generateConversationId = () => {
  return `chat_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

// 开始新对话
const startNewChat = () => {
  currentChat.value = null
  conversationId.value = null
  messages.value = []
  inputMessage.value = ''
}

// 快捷功能点击
const handleQuickAction = (action) => {
  inputMessage.value = action.prompt
  sendMessage()
}

// 加载历史对话
const loadChat = (chat) => {
  currentChat.value = chat
  conversationId.value = chat.conversationId
  // 这里应该从后端加载历史消息，暂时使用模拟数据
  messages.value = chat.messages || [
    { role: 'user', content: chat.preview },
    { role: 'assistant', content: '这是历史对话的回复内容...' }
  ]
}

// 发送消息
const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || isSending.value) return

  // 如果没有会话ID，生成一个新的
  if (!conversationId.value) {
    conversationId.value = generateConversationId()
  }

  // 添加用户消息
  messages.value.push({ role: 'user', content })
  const userMessage = content
  inputMessage.value = ''

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 调用 AI API
  isSending.value = true
  isTyping.value = true

  try {
    const res = await chatApi({
      content: userMessage,
      conversationId: conversationId.value,
      userId: currentUser.value.id
    })

    isTyping.value = false

    if (res.code === 200 && res.data) {
      const aiResponse = res.data.reply || res.data.content || res.data.message || '抱歉，我没有收到回复。'
      messages.value.push({ role: 'assistant', content: aiResponse })

      // 如果是第一条消息，更新会话历史
      if (messages.value.length === 2) {
        chatHistory.value.unshift({
          id: Date.now(),
          title: userMessage.slice(0, 20) + (userMessage.length > 20 ? '...' : ''),
          preview: userMessage,
          time: '刚刚',
          conversationId: conversationId.value,
          messages: [...messages.value]
        })
      }
    } else {
      throw new Error(res.msg || 'AI 响应失败')
    }

    nextTick(() => scrollToBottom())
  } catch (error) {
    console.error('AI 聊天失败:', error)
    isTyping.value = false
    ElMessage.error(error.message || 'AI 服务暂时不可用，请稍后重试')
    // 失败时添加提示消息
    messages.value.push({
      role: 'assistant',
      content: '抱歉，我现在无法回复。请稍后再试，或检查网络连接。'
    })
    nextTick(() => scrollToBottom())
  } finally {
    isSending.value = false
  }
}

// 重新生成
const regenerateMessage = async () => {
  // 找到最后的用户消息，重新生成
  const lastUserMessage = [...messages.value].reverse().find(m => m.role === 'user')
  if (lastUserMessage && !isSending.value) {
    // 移除最后的 AI 回复
    const lastAssistantIndex = messages.value.map(m => m.role).lastIndexOf('assistant')
    if (lastAssistantIndex > -1) {
      messages.value.splice(lastAssistantIndex, 1)
    }
    // 重新发送
    inputMessage.value = lastUserMessage.content
    await sendMessage()
  }
}

// 复制消息
const copyMessage = (content) => {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 格式化消息（支持换行和基本格式）
const formatMessage = (content) => {
  if (!content) return ''
  return content
    .replace(/\n/g, '<br>')
    .replace(/• /g, '<br>• ')
    .replace(/(\d+)\./g, '<br>$1.')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onMounted(() => {
  // 组件加载后滚动到底部
  if (messages.value.length > 0) {
    nextTick(() => scrollToBottom())
  }
})
</script>

<style scoped lang="scss">
.assistant-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: var(--dt-bg-body);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-color);
  flex-shrink: 0;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.new-chat-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--dt-brand-color);
  color: #fff;
  border: none;
  border-radius: var(--dt-radius-lg);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--dt-transition-fast);
}

.new-chat-btn:hover {
  background: var(--dt-brand-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.25);
}

.panel-content {
  flex: 1;
  overflow: hidden;
}

/* 对话区域 */
.chat-area {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 消息样式 */
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

.message-item.user {
  flex-direction: row-reverse;
}

.welcome-message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ai-avatar .material-icons-outlined {
  font-size: 24px;
}

.ai-avatar-large {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
}

.ai-avatar-large .material-icons-outlined {
  font-size: 40px;
}

.message-content {
  max-width: 70%;
}

.message-item.user .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-text {
  padding: 12px 16px;
  border-radius: var(--dt-radius-xl);
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.message-item.assistant .message-text {
  background: var(--dt-bg-hover);
  color: var(--dt-text-primary);
  border-top-left-radius: var(--dt-radius-sm);
}

.message-item.user .message-text {
  background: var(--dt-brand-color);
  color: #fff;
  border-top-right-radius: var(--dt-radius-sm);
}

.message-actions {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}

.action-btn {
  padding: 4px;
  background: none;
  border: none;
  color: var(--dt-text-tertiary);
  cursor: pointer;
  border-radius: var(--dt-radius-sm);
  transition: all var(--dt-transition-fast);
}

.action-btn:hover {
  background: var(--dt-bg-hover);
  color: var(--dt-brand-color);
}

.action-btn .material-icons-outlined {
  font-size: 16px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--dt-brand-color);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  flex-shrink: 0;
}

/* 输入中动画 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-xl);
  width: fit-content;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: var(--dt-text-quaternary);
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-8px);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 输入区域 */
.input-area {
  padding: 16px 20px;
  background: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-color);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-xl);
  padding: 8px 12px 8px 16px;
  border: 1.5px solid var(--dt-border-color);
  transition: all var(--dt-transition-fast);
}

.input-wrapper:focus-within {
  border-color: var(--dt-brand-color);
  box-shadow: 0 0 0 3px var(--dt-brand-lighter);
}

.input-wrapper :deep(.el-textarea__inner) {
  background: transparent;
  border: none;
  padding: 0;
  font-size: 14px;
  resize: none;
  color: var(--dt-text-primary);
}

.input-wrapper :deep(.el-textarea__inner:focus) {
  box-shadow: none;
}

.input-wrapper :deep(.el-textarea__inner::placeholder) {
  color: var(--dt-text-quaternary);
}

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-lg);
  background: var(--dt-brand-color);
  color: #fff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  background: var(--dt-brand-hover);
  transform: scale(1.05);
}

.send-btn:disabled {
  background: var(--dt-border-color);
  cursor: not-allowed;
  opacity: 0.6;
}

.send-btn .material-icons-outlined {
  font-size: 18px;
}

/* 首页样式 */
.home-area {
  padding: 24px;
  overflow-y: auto;
}

.welcome-section {
  text-align: center;
  margin-bottom: 32px;
}

.welcome-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0 0 8px 0;
}

.welcome-desc {
  font-size: 14px;
  color: var(--dt-text-secondary);
  margin: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-secondary);
  margin: 0 0 16px 0;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.action-card {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  border: 1px solid var(--dt-border-light);
}

.action-card:hover {
  box-shadow: var(--dt-shadow-float);
  transform: translateY(-2px);
  border-color: var(--dt-brand-color);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--dt-radius-lg);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon .material-icons-outlined {
  font-size: 24px;
}

.action-label {
  font-size: 13px;
  color: var(--dt-text-secondary);
  font-weight: 500;
}

.action-card:hover .action-label {
  color: var(--dt-brand-color);
}

.chat-section {
  margin-top: 32px;
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chat-item {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  border: 1px solid var(--dt-border-light);
}

.chat-item:hover {
  background: var(--dt-bg-hover);
  box-shadow: var(--dt-shadow-1);
  border-color: var(--dt-brand-color);
}

.chat-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-lg);
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-icon .material-icons-outlined {
  font-size: 18px;
}

.chat-content {
  flex: 1;
  min-width: 0;
}

.chat-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: 2px;
}

.chat-preview {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-time {
  font-size: 11px;
  color: var(--dt-text-quaternary);
}

/* 暗色模式 */
.dark .assistant-panel {
  background: var(--dt-bg-body-dark);
}

.dark .panel-header {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .panel-title,
.dark .welcome-title {
  color: var(--dt-text-primary-dark);
}

.dark .message-text {
  background: var(--dt-bg-hover-dark);
  color: var(--dt-text-primary-dark);
}

.dark .input-area {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .input-wrapper {
  background: var(--dt-bg-input-dark);
  border-color: var(--dt-border-dark);
}

.dark .input-wrapper :deep(.el-textarea__inner) {
  color: var(--dt-text-primary-dark);
}

.dark .action-card,
.dark .chat-item {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .action-card:hover,
.dark .chat-item:hover {
  background: var(--dt-bg-hover-dark);
}

.dark .action-label {
  color: var(--dt-text-secondary-dark);
}

.dark .section-title {
  color: var(--dt-text-secondary-dark);
}

.dark .chat-title {
  color: var(--dt-text-primary-dark);
}

.dark .chat-preview {
  color: var(--dt-text-tertiary-dark);
}

.dark .chat-time {
  color: var(--dt-text-quaternary-dark);
}

.dark .chat-icon {
  background: var(--dt-brand-bg-dark);
}

.dark .typing-indicator {
  background: var(--dt-bg-hover-dark);
}

.dark .typing-indicator span {
  background: var(--dt-text-quaternary-dark);
}

.dark .action-btn {
  color: var(--dt-text-tertiary-dark);
}

.dark .action-btn:hover {
  background: var(--dt-bg-hover-dark);
  color: var(--dt-brand-color);
}

.dark .welcome-desc {
  color: var(--dt-text-secondary-dark);
}
</style>
