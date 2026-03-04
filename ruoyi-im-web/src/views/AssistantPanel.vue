<template>
  <div class="assistant-panel">
    <!-- 头部 -->
    <header class="panel-header">
      <div class="header-left">
        <div class="ai-logo">
          <el-icon><Promotion /></el-icon>
        </div>
        <div class="header-info">
          <h2 class="panel-title">AI 助理</h2>
          <span class="model-tag" v-if="selectedModel">{{ selectedModel }}</span>
        </div>
      </div>
      <div class="header-actions">
        <el-dropdown trigger="click" @command="handleModelChange">
          <el-button class="model-btn">
            <el-icon><Setting /></el-icon>
            模型
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item 
                v-for="model in availableModels" 
                :key="model" 
                :command="model"
                :class="{ 'is-active': selectedModel === model }"
              >
                {{ model }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button class="new-chat-btn" @click="startNewChat">
          <el-icon><Plus /></el-icon>
          新对话
        </el-button>
      </div>
    </header>

    <!-- 主体内容 -->
    <div class="panel-body">
      <!-- 对话模式 -->
      <template v-if="messages.length > 0">
        <div class="messages-area" ref="messagesContainer">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message-row"
            :class="msg.role"
          >
            <div v-if="msg.role === 'assistant'" class="avatar ai">
              <el-icon><Promotion /></el-icon>
            </div>
            
            <div class="message-bubble" :class="msg.role">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
              <div v-if="msg.role === 'assistant'" class="message-meta">
                <span class="time">{{ formatTime(msg.timestamp) }}</span>
                <div class="actions">
                  <el-tooltip content="复制">
                    <button class="action-btn" @click="copyMessage(msg.content)">
                      <el-icon><DocumentCopy /></el-icon>
                    </button>
                  </el-tooltip>
                  <el-tooltip content="重新生成">
                    <button class="action-btn" @click="regenerateMessage">
                      <el-icon><Refresh /></el-icon>
                    </button>
                  </el-tooltip>
                </div>
              </div>
            </div>
            
            <div v-if="msg.role === 'user'" class="avatar user">
              {{ userInitial }}
            </div>
          </div>

          <!-- 输入中状态 -->
          <div v-if="isTyping" class="message-row assistant">
            <div class="avatar ai">
              <el-icon class="pulse"><Promotion /></el-icon>
            </div>
            <div class="message-bubble assistant typing">
              <div class="typing-dots">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="input-area">
          <div class="input-box">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="1"
              :autosize="{ minRows: 1, maxRows: 4 }"
              placeholder="输入消息，按 Enter 发送..."
              @keydown.enter.exact.prevent="sendMessage"
            />
            <el-button 
              type="primary" 
              :disabled="!inputMessage.trim() || isSending"
              :loading="isSending"
              @click="sendMessage"
            >
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
          </div>
        </div>
      </template>

      <!-- 首页模式 -->
      <template v-else>
        <div class="welcome-page">
          <!-- 欢迎区 -->
          <div class="welcome-hero">
            <div class="hero-icon">
              <el-icon><Promotion /></el-icon>
            </div>
            <h1 class="hero-title">你好，我是 AI 助理</h1>
            <p class="hero-desc">我可以帮你写文案、翻译、总结、写代码等，有什么可以帮你的？</p>
          </div>

          <!-- 快捷功能 -->
          <div class="quick-section">
            <h3 class="section-title">快捷功能</h3>
            <div class="quick-grid">
              <div
                v-for="action in quickActions"
                :key="action.id"
                class="quick-card"
                @click="handleQuickAction(action)"
              >
                <div class="card-icon" :style="{ background: action.color }">
                  <el-icon><component :is="action.icon" /></el-icon>
                </div>
                <span class="card-label">{{ action.label }}</span>
              </div>
            </div>
          </div>

          <!-- 历史对话 -->
          <div v-if="chatHistory.length > 0" class="history-section">
            <div class="section-header">
              <h3 class="section-title">最近对话</h3>
              <el-button link type="primary" @click="clearAllHistory">清空</el-button>
            </div>
            <div class="history-list">
              <div
                v-for="chat in chatHistory"
                :key="chat.id"
                class="history-item"
                @click="loadChat(chat)"
              >
                <el-icon class="history-icon"><ChatDotRound /></el-icon>
                <div class="history-content">
                  <div class="history-title">{{ chat.title }}</div>
                  <div class="history-time">{{ chat.time }}</div>
                </div>
                <el-button 
                  class="delete-btn" 
                  circle 
                  size="small"
                  @click.stop="deleteChat(chat)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import {
  Promotion, Plus, Setting, DocumentCopy, Refresh,
  ChatDotRound, Delete, Edit, Document, DataAnalysis,
  Sunny, Compass
} from '@element-plus/icons-vue'
import { chat, getSupportedModels } from '@/api/im/ai'

const store = useStore()
const messagesContainer = ref(null)

const currentUser = computed(() => store.getters['user/currentUser'] || {})
const userInitial = computed(() => {
  const name = currentUser.value.nickname || currentUser.value.username || '我'
  return name.charAt(0).toUpperCase()
})
const currentUserId = computed(() => currentUser.value.id)

// 状态
const inputMessage = ref('')
const isSending = ref(false)
const isTyping = ref(false)
const conversationId = ref(null)
const availableModels = ref(['qwen', 'gpt-3.5-turbo', 'gpt-4', 'wenxin', 'spark'])
const selectedModel = ref('qwen')

// 消息列表
const messages = ref([])

// 对话历史
const chatHistory = ref([])

// 快捷功能
const quickActions = [
  { id: 1, label: '写文案', icon: 'Edit', color: '#1677ff', prompt: '帮我写一段关于' },
  { id: 2, label: '翻译', icon: 'Compass', color: '#52c41a', prompt: '请翻译以下内容：' },
  { id: 3, label: '总结', icon: 'Document', color: '#fa8c16', prompt: '请帮我总结以下内容：' },
  { id: 4, label: '代码', icon: 'DataAnalysis', color: '#722ed1', prompt: '请帮我写一段代码：' },
  { id: 5, label: '分析', icon: 'DataAnalysis', color: '#eb2f96', prompt: '请帮我分析：' },
  { id: 6, label: '创意', icon: 'Sunny', color: '#13c2c2', prompt: '请给我一些创意想法：' }
]

// 加载对话历史
const loadChatHistory = () => {
  try {
    const history = localStorage.getItem('ai_chat_history')
    if (history) {
      chatHistory.value = JSON.parse(history)
    }
  } catch (e) {
    console.error('加载对话历史失败', e)
  }
}

// 保存对话历史
const saveChatHistory = () => {
  try {
    localStorage.setItem('ai_chat_history', JSON.stringify(chatHistory.value))
  } catch (e) {
    console.error('保存对话历史失败', e)
  }
}

// 加载AI模型列表
const loadModels = async () => {
  try {
    const res = await getSupportedModels()
    if (res.code === 200 && res.data) {
      availableModels.value = res.data
    }
  } catch (e) {
    console.warn('加载AI模型失败，使用默认模型列表')
  }
}

// 切换模型
const handleModelChange = (model) => {
  selectedModel.value = model
  ElMessage.success(`已切换到 ${model}`)
}

// 开始新对话
const startNewChat = () => {
  if (messages.value.length > 0) {
    const title = messages.value[0]?.content?.substring(0, 20) || '新对话'
    const newChat = {
      id: Date.now(),
      title: title + '...',
      preview: messages.value[0]?.content || '',
      time: new Date().toLocaleDateString(),
      conversationId: conversationId.value
    }
    chatHistory.value.unshift(newChat)
    if (chatHistory.value.length > 10) {
      chatHistory.value = chatHistory.value.slice(0, 10)
    }
    saveChatHistory()
  }

  messages.value = []
  inputMessage.value = ''
  conversationId.value = null
}

// 快捷功能点击
const handleQuickAction = (action) => {
  inputMessage.value = action.prompt
}

// 加载历史对话
const loadChat = (chatItem) => {
  conversationId.value = chatItem.conversationId
  messages.value = [
    { role: 'user', content: chatItem.preview, timestamp: Date.now() },
    { role: 'assistant', content: '已加载历史对话，请继续...', timestamp: Date.now() }
  ]
}

// 发送消息
const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || isSending.value) return

  // 添加用户消息
  messages.value.push({ 
    role: 'user', 
    content, 
    timestamp: Date.now() 
  })
  inputMessage.value = ''
  isSending.value = true

  await nextTick()
  scrollToBottom()

  // 调用AI API
  isTyping.value = true
  try {
    const res = await chat({
      content: content,
      conversationId: conversationId.value || `conv_${Date.now()}`,
      userId: currentUserId.value,
      model: selectedModel.value
    })

    isTyping.value = false

    if (res.code === 200 && res.data) {
      conversationId.value = res.data.conversationId || conversationId.value
      messages.value.push({
        role: 'assistant',
        content: res.data.content || res.data.message || '抱歉，我无法生成回复',
        timestamp: Date.now()
      })
    } else {
      messages.value.push({
        role: 'assistant',
        content: res.msg || '抱歉，服务暂时不可用',
        timestamp: Date.now()
      })
    }
  } catch (e) {
    isTyping.value = false
    console.error('AI对话失败', e)
    messages.value.push({
      role: 'assistant',
      content: '抱歉，请求失败，请稍后重试',
      timestamp: Date.now()
    })
    ElMessage.error('AI对话失败')
  }

  await nextTick()
  scrollToBottom()
  isSending.value = false
}

// 重新生成
const regenerateMessage = () => {
  const lastUserMessage = [...messages.value].reverse().find(m => m.role === 'user')
  if (lastUserMessage && !isSending.value) {
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

// 格式化消息
const formatMessage = (content) => {
  if (!content) return ''
  return content
    .replace(/\n/g, '<br>')
    .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const d = new Date(timestamp)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 删除历史对话
const deleteChat = (chatItem) => {
  chatHistory.value = chatHistory.value.filter(c => c.id !== chatItem.id)
  saveChatHistory()
  ElMessage.success('已删除')
}

// 清空所有历史
const clearAllHistory = () => {
  chatHistory.value = []
  saveChatHistory()
  ElMessage.success('已清空所有历史')
}

onMounted(() => {
  loadChatHistory()
  loadModels()
})
</script>

<style scoped lang="scss">
.assistant-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--dt-bg-body);
}

// 头部
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-logo {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.model-tag {
  font-size: 11px;
  padding: 2px 8px;
  background: var(--dt-brand-lighter);
  color: var(--dt-brand-color);
  border-radius: 10px;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.model-btn {
  border-color: var(--dt-border-color);
  color: var(--dt-text-secondary);
}

.new-chat-btn {
  background: var(--dt-brand-color);
  border-color: var(--dt-brand-color);
  color: #fff;
  
  &:hover {
    background: var(--dt-brand-hover);
    border-color: var(--dt-brand-hover);
  }
}

// 主体
.panel-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 消息区域
.messages-area {
  flex: 1;
  padding: 20px 24px;
  overflow-y: auto;
}

.message-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  &.user {
    flex-direction: row-reverse;
  }
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 14px;

  &.ai {
    background: linear-gradient(135deg, #667eea, #764ba2);
    color: #fff;
  }

  &.user {
    background: var(--dt-brand-color);
    color: #fff;
    font-weight: 600;
  }
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;

  &.assistant {
    background: var(--dt-bg-card);
    border: 1px solid var(--dt-border-light);
    border-top-left-radius: 4px;
  }

  &.user {
    background: var(--dt-brand-color);
    color: #fff;
    border-top-right-radius: 4px;
  }

  &.typing {
    padding: 16px 20px;
  }
}

.message-text {
  word-break: break-word;

  :deep(pre) {
    background: var(--dt-bg-body);
    padding: 12px;
    border-radius: 8px;
    overflow-x: auto;
    margin: 8px 0;
  }

  :deep(code) {
    background: var(--dt-bg-body);
    padding: 2px 6px;
    border-radius: 4px;
    font-family: monospace;
  }
}

.message-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--dt-border-lighter);

  .time {
    font-size: 11px;
    color: var(--dt-text-quaternary);
  }

  .actions {
    display: flex;
    gap: 4px;
  }
}

.action-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: var(--dt-text-tertiary);
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-session-hover);
    color: var(--dt-brand-color);
  }
}

// 输入中动画
.typing-dots {
  display: flex;
  gap: 4px;

  span {
    width: 8px;
    height: 8px;
    background: var(--dt-text-quaternary);
    border-radius: 50%;
    animation: bounce 1.4s infinite ease-in-out;

    &:nth-child(1) { animation-delay: 0s; }
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

.pulse {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

// 输入区
.input-area {
  padding: 16px 24px;
  background: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-light);
}

.input-box {
  display: flex;
  gap: 12px;
  align-items: flex-end;

  :deep(.el-textarea) {
    flex: 1;
  }

  :deep(.el-textarea__inner) {
    border-radius: 12px;
    border: 1.5px solid var(--dt-border-color);
    padding: 12px 16px;
    font-size: 14px;
    resize: none;

    &:focus {
      border-color: var(--dt-brand-color);
      box-shadow: 0 0 0 3px var(--dt-brand-lighter);
    }
  }
}

// 欢迎页
.welcome-page {
  flex: 1;
  padding: 40px 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.welcome-hero {
  text-align: center;
  margin-bottom: 48px;
}

.hero-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  border-radius: 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.hero-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--dt-text-primary);
  margin: 0 0 12px;
}

.hero-desc {
  font-size: 15px;
  color: var(--dt-text-secondary);
  margin: 0;
  max-width: 400px;
}

.quick-section {
  width: 100%;
  max-width: 600px;
  margin-bottom: 32px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-secondary);
  margin: 0 0 16px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.quick-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 12px;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.1);
    transform: translateY(-2px);
  }
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.card-label {
  font-size: 13px;
  color: var(--dt-text-primary);
  font-weight: 500;
}

.history-section {
  width: 100%;
  max-width: 600px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--dt-brand-color);
    background: var(--dt-bg-session-hover);
  }
}

.history-icon {
  color: var(--dt-brand-color);
  font-size: 20px;
}

.history-content {
  flex: 1;
  min-width: 0;
}

.history-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-time {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.2s;
  
  &:hover {
    color: var(--dt-error-color);
  }
}

.history-item:hover .delete-btn {
  opacity: 1;
}

// 暗色模式
.dark {
  .assistant-panel {
    background: var(--dt-bg-body-dark);
  }

  .panel-header {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .panel-title,
  .hero-title,
  .card-label,
  .history-title {
    color: var(--dt-text-primary-dark);
  }

  .message-bubble.assistant {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .input-area {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .quick-card,
  .history-item {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .hero-desc,
  .section-title {
    color: var(--dt-text-secondary-dark);
  }
}
</style>