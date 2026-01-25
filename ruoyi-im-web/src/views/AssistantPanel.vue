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

const store = useStore()
const messagesContainer = ref(null)

const currentUser = computed(() => store.getters['user/currentUser'] || {})
const currentUserNickname = computed(() => currentUser.value.nickname || currentUser.value.username || '我')

// 状态
const currentChat = ref(null)
const inputMessage = ref('')
const isSending = ref(false)
const isTyping = ref(false)

// 消息列表
const messages = ref([])

// 对话历史
const chatHistory = ref([
  {
    id: 1,
    title: '周报总结',
    preview: '帮我生成本周工作总结',
    time: '昨天'
  },
  {
    id: 2,
    title: '代码优化',
    preview: '这段代码如何优化',
    time: '3天前'
  }
])

// 快捷功能
const quickActions = [
  { id: 1, label: '写文案', icon: 'edit', bgColor: '#1677ff', prompt: '帮我写一段关于' },
  { id: 2, label: '翻译', icon: 'translate', bgColor: '#52c41a', prompt: '请翻译以下内容：' },
  { id: 3, label: '总结', icon: 'summarize', bgColor: '#fa8c16', prompt: '请帮我总结以下内容：' },
  { id: 4, label: '代码', icon: 'code', bgColor: '#722ed1', prompt: '请帮我写一段代码：' },
  { id: 5, label: '分析', icon: 'analytics', bgColor: '#eb2f96', prompt: '请帮我分析：' },
  { id: 6, label: '创意', icon: 'lightbulb', bgColor: '#8b5cf6', prompt: '请给我一些创意想法：' }
]

// 开始新对话
const startNewChat = () => {
  currentChat.value = null
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
  messages.value = [
    { role: 'user', content: chat.preview },
    { role: 'assistant', content: '这是历史对话的回复内容...' }
  ]
}

// 发送消息
const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content) return

  // 添加用户消息
  messages.value.push({ role: 'user', content })
  inputMessage.value = ''

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 模拟 AI 响应
  isTyping.value = true
  setTimeout(() => {
    isTyping.value = false
    const response = generateMockResponse(content)
    messages.value.push({ role: 'assistant', content: response })
    nextTick(() => scrollToBottom())
  }, 1500 + Math.random() * 1000)
}

// 生成模拟回复
const generateMockResponse = (input) => {
  const responses = [
    `我理解你说的"${input}"。这是一个很有趣的问题，让我来帮你分析一下。\n\n首先，我们可以从以下几个方面来考虑：\n1. 问题的核心是什么\n2. 有哪些可能的解决方案\n3. 每种方案的优缺点\n\n你希望我详细展开哪个方面呢？`,
    `关于"${input}"，我的看法是：\n\n这是一个很好的问题。根据我的分析，建议你可以尝试以下方法：\n• 方法一：循序渐进\n• 方法二：寻求帮助\n• 方法三：持续学习\n\n有什么需要我进一步解释的吗？`,
    `"${input}" - 这个问题很有意思！\n\n让我为你整理一下思路：\n\n1. 首先明确目标\n2. 然后制定计划\n3. 最后执行和验证\n\n你觉得这个思路如何？`
  ]
  return responses[Math.floor(Math.random() * responses.length)]
}

// 重新生成
const regenerateMessage = () => {
  // 找到最后的用户消息，重新生成
  const lastUserMessage = [...messages.value].reverse().find(m => m.role === 'user')
  if (lastUserMessage) {
    messages.value = messages.value.slice(0, messages.value.length - 1)
    inputMessage.value = lastUserMessage.content
    sendMessage()
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

<style scoped>
.assistant-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: #f4f7f9;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  flex-shrink: 0;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.new-chat-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.new-chat-btn:hover {
  background: #4096ff;
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
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.message-item.assistant .message-text {
  background: #f0f0f0;
  color: #262626;
  border-top-left-radius: 4px;
}

.message-item.user .message-text {
  background: #1677ff;
  color: #fff;
  border-top-right-radius: 4px;
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
  color: #8c8c8c;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #e6e6e6;
  color: #1677ff;
}

.action-btn .material-icons-outlined {
  font-size: 16px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #1677ff;
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
  background: #f0f0f0;
  border-radius: 12px;
  width: fit-content;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #bfbfbf;
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
  background: #fff;
  border-top: 1px solid #e6e6e6;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: #f5f5f5;
  border-radius: 12px;
  padding: 8px 12px 8px 16px;
}

.input-wrapper :deep(.el-textarea__inner) {
  background: transparent;
  border: none;
  padding: 0;
  font-size: 14px;
  resize: none;
}

.input-wrapper :deep(.el-textarea__inner:focus) {
  box-shadow: none;
}

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #1677ff;
  color: #fff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  background: #4096ff;
}

.send-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
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
  color: #262626;
  margin: 0 0 8px 0;
}

.welcome-desc {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #595959;
  margin: 0 0 16px 0;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.action-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
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
  color: #595959;
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
  background: #fff;
  border-radius: 12px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.chat-item:hover {
  background: #fafafa;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.chat-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #f0f0f0;
  color: #1677ff;
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
  color: #262626;
  margin-bottom: 2px;
}

.chat-preview {
  font-size: 12px;
  color: #8c8c8c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-time {
  font-size: 11px;
  color: #bfbfbf;
}

/* 暗色模式 */
:deep(.dark) .assistant-panel {
  background: #0f172a;
}

:deep(.dark) .panel-header {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .panel-title,
:deep(.dark) .welcome-title {
  color: #f1f5f9;
}

:deep(.dark) .message-text {
  background: #334155;
  color: #e2e8f0;
}

:deep(.dark) .input-area {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .input-wrapper {
  background: rgba(30, 41, 59, 0.5);
}

:deep(.dark) .action-card,
:deep(.dark) .chat-item {
  background: #1e293b;
}

:deep(.dark) .action-card:hover,
:deep(.dark) .chat-item:hover {
  background: rgba(51, 65, 85, 0.5);
}

:deep(.dark) .action-label {
  color: #cbd5e1;
}

:deep(.dark) .section-title {
  color: #94a3b8;
}

:deep(.dark) .chat-preview {
  color: #64748b;
}

:deep(.dark) .chat-icon {
  background: rgba(30, 41, 59, 0.5);
}
</style>
