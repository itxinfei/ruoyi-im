<template>
  <div class="assistant-v2">
    <!-- 1. 左侧历史侧边栏 -->
    <aside class="ai-history-sidebar">
      <div class="sidebar-header">
        <el-button class="new-chat-btn" @click="startNewChat">
          <el-icon><Plus /></el-icon>
          <span>开启新对话</span>
        </el-button>
      </div>
      <div class="history-list">
        <div 
          v-for="chat in chatHistory" 
          :key="chat.id" 
          class="history-item"
          :class="{ active: conversationId === chat.conversationId }"
          @click="loadChat(chat)"
        >
          <el-icon><ChatDotRound /></el-icon>
          <span class="chat-title">{{ chat.title }}</span>
          <el-icon class="del-icon" @click.stop="deleteChat(chat)"><Delete /></el-icon>
        </div>
      </div>
    </aside>

    <!-- 2. 中间对话主区 -->
    <main class="ai-chat-container">
      <header class="chat-header">
        <div class="title-info">
          <span class="ai-badge">AI</span>
          <h2 class="chat-title">{{ currentChatTitle }}</h2>
        </div>
        <div class="header-tools">
          <el-dropdown trigger="click" @command="handleModelChange">
            <span class="model-selector">{{ selectedModel }} <el-icon><ArrowDown /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="m in availableModels" :key="m" :command="m">{{ m }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <div ref="scrollRef" class="chat-body">
        <!-- 欢迎状态 -->
        <div v-if="messages.length === 0" class="welcome-view">
          <div class="welcome-card">
            <div class="ai-icon-large"><el-icon><Promotion /></el-icon></div>
            <h1>你好，我是你的 AI 助理</h1>
            <p>基于大语言模型，我可以帮你处理文档、总结记录或回答问题。</p>
            <div class="feature-chips">
              <span class="feature-chip">💼 润色周报</span>
              <span class="feature-chip">🌐 中英互译</span>
              <span class="feature-chip">📊 数据总结</span>
              <span class="feature-chip">📝 起草文档</span>
            </div>
          </div>
        </div>

        <!-- 对话流 -->
        <div v-else class="message-list">
          <div v-for="(msg, idx) in messages" :key="idx" class="msg-row" :class="msg.role">
            <div class="msg-avatar">
              <el-icon v-if="msg.role === 'assistant'"><Promotion /></el-icon>
              <span v-else>{{ userInitial }}</span>
            </div>
            <div class="msg-bubble" :class="msg.role">
              <div class="content markdown-body" v-html="formatMessage(msg.content)"></div>
              <!-- 打字机光标 -->
              <span v-if="msg.isStreaming" class="typing-cursor">|</span>
              
              <div v-if="msg.role === 'assistant' && !msg.isStreaming" class="msg-footer">
                <div class="msg-actions">
                  <button @click="copyMessage(msg.content)"><el-icon><DocumentCopy /></el-icon></button>
                  <button @click="regenerateMessage"><el-icon><Refresh /></el-icon></button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 3. 底部输入与快捷指令 -->
      <footer class="chat-footer">
        <div class="prompt-chips">
          <div v-for="chip in quickActions" :key="chip.id" class="p-chip" @click="handleQuickAction(chip)">
            {{ chip.label }}
          </div>
        </div>
        <div class="input-box-v2">
          <textarea
            v-model="inputMessage"
            placeholder="问我任何问题... (Shift + Enter 换行)"
            rows="1"
            @keydown.enter.exact.prevent="sendMessage"
          ></textarea>
          <div class="btn-group">
            <button v-if="isTyping" class="stop-btn" @click="stopGeneration">
              <el-icon><CircleClose /></el-icon> 停止生成
            </button>
            <button class="send-btn" :disabled="!inputMessage.trim() || isTyping" @click="sendMessage">
              <el-icon><Position /></el-icon>
            </button>
          </div>
        </div>
      </footer>
    </main>

    <!-- 4. 右侧总结侧边栏 (Conditional) -->
    <transition name="slide">
      <aside v-if="summaryVisible" class="ai-summary-panel">
        <div class="summary-header">
          <span>AI 总结</span>
          <el-icon @click="summaryVisible = false"><Close /></el-icon>
        </div>
        <div class="summary-body markdown-body" v-html="summaryContent"></div>
      </aside>
    </transition>
  </div>
</template>

<script setup lang="js">
import { ref, computed, nextTick, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import {
  Promotion, Plus, ChatDotRound, Delete, ArrowDown, 
  DocumentCopy, Refresh, Position, CircleClose, Close
} from '@element-plus/icons-vue'
import { chat, getSupportedModels } from '@/api/im/ai'
import { formatAIMessage } from '@/utils/htmlSanitizer'

const store = useStore()
const scrollRef = ref(null)

// 基础数据
const currentUser = computed(() => store.getters['user/currentUser'] || {})
const userInitial = computed(() => (currentUser.value.nickname || '我').charAt(0))
const availableModels = ref(['通义千问', 'GPT-4', 'DeepSeek-V3', '文心一言'])
const selectedModel = ref('通义千问')

// 状态管理
const inputMessage = ref('')
const isTyping = ref(false)
const conversationId = ref(null)
const messages = ref([])
const chatHistory = ref([])
const summaryVisible = ref(false)
const summaryContent = ref('')

const currentChatTitle = computed(() => {
  if (messages.value.length === 0) return '新建对话'
  return messages.value[0].content.substring(0, 15) + '...'
})

// 快捷指令
const quickActions = [
  { id: 1, label: '💼 润色周报', prompt: '请帮我润色以下周报内容，使其更专业、简洁有条理：\n\n' },
  { id: 2, label: '🌐 中英互译', prompt: '请将以下内容翻译成英文（保持原意，地道表达）：\n\n' },
  { id: 3, label: '📊 数据总结', prompt: '请帮我分析并总结以下数据的核心指标和关键洞察：\n\n' },
  { id: 4, label: '📝 起草通知', prompt: '请帮我起草一份公司内部通知，包括：标题、正文（背景/事项/要求）、落款\n\n' },
  { id: 5, label: '📧 写邮件', prompt: '请帮我起草一封专业邮件，包括：主题、正文（问候/目的/内容/期待行动/礼貌结语）\n\n收件人：\n主题：\n' },
  { id: 6, label: '📋 项目方案', prompt: '请帮我规划一个项目方案，包括：背景目标、核心模块、实施步骤、预期成果、风险评估\n\n项目主题：\n' }
]

// --- 逻辑实现 ---

const startNewChat = () => {
  if (messages.value.length > 0) archiveCurrentChat()
  messages.value = []
  conversationId.value = `conv_${Date.now()}`
}

const archiveCurrentChat = () => {
  const title = messages.value[0]?.content?.substring(0, 20) || '历史对话'
  chatHistory.value.unshift({
    id: Date.now(),
    title,
    conversationId: conversationId.value,
    messages: [...messages.value]
  })
  localStorage.setItem('ai_chat_history', JSON.stringify(chatHistory.value.slice(0, 20)))
}

const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || isTyping.value) return

  messages.value.push({ role: 'user', content, timestamp: Date.now() })
  inputMessage.value = ''
  isTyping.value = true
  
  // 预插一条 AI 消息用于流式显示
  const aiMsgIdx = messages.value.push({ 
    role: 'assistant', 
    content: '', 
    isStreaming: true,
    timestamp: Date.now() 
  }) - 1

  await nextTick(scrollToBottom)

  try {
    const res = await chat({
      content,
      conversationId: conversationId.value,
      model: selectedModel.value
    })

    if (res.code === 200) {
      simulateStreaming(aiMsgIdx, res.data.content)
    } else {
      messages.value[aiMsgIdx].content = '服务响应异常，请重试。'
      messages.value[aiMsgIdx].isStreaming = false
    }
  } catch (e) {
    messages.value[aiMsgIdx].content = '网络连接失败。'
    messages.value[aiMsgIdx].isStreaming = false
  } finally {
    isTyping.value = false
  }
}

// 模拟流式打字机效果
const simulateStreaming = (idx, fullText) => {
  let current = 0
  const timer = setInterval(() => {
    messages.value[idx].content = fullText.substring(0, current)
    current += 3
    scrollToBottom()
    if (current >= fullText.length) {
      messages.value[idx].content = fullText
      messages.value[idx].isStreaming = false
      clearInterval(timer)
    }
  }, 20)
}

const handleQuickAction = (chip) => {
  inputMessage.value = chip.prompt
}

const loadChat = (item) => {
  conversationId.value = item.conversationId
  messages.value = item.messages || []
}

const deleteChat = (item) => {
  chatHistory.value = chatHistory.value.filter(c => c.id !== item.id)
  localStorage.setItem('ai_chat_history', JSON.stringify(chatHistory.value))
}

const formatMessage = (c) => formatAIMessage(c)
const scrollToBottom = () => { if (scrollRef.value) scrollRef.value.scrollTop = scrollRef.value.scrollHeight }
const copyMessage = (c) => { navigator.clipboard.writeText(c); ElMessage.success('已复制') }

// 重新生成上一条回复
const regenerateMessage = () => {
  if (messages.value.length < 2) return
  // 移除最后一条AI回复
  messages.value.pop()
  // 重新发送最后一条用户消息
  const lastUserMsg = messages.value[messages.value.length - 1]
  if (lastUserMsg && lastUserMsg.role === 'user') {
    inputMessage.value = lastUserMsg.content
    sendMessage()
  }
}

onMounted(() => {
  const saved = localStorage.getItem('ai_chat_history')
  if (saved) chatHistory.value = JSON.parse(saved)
  if (messages.value.length === 0) startNewChat()
})
</script>

<style scoped lang="scss">
.assistant-v2 {
  display: flex; height: 100%; background: var(--dt-bg-body); overflow: hidden;
}

// 1. 历史侧栏
.ai-history-sidebar {
  width: 240px; background: var(--dt-bg-body); border-right: 1px solid var(--dt-border-light);
  display: flex; flex-direction: column;
  .sidebar-header { padding: 16px; }
  .new-chat-btn {
    width: 100%; height: 40px; border-radius: var(--dt-radius-lg); border: 1.5px dashed var(--dt-brand-color);
    color: var(--dt-brand-color); background: var(--dt-bg-card);
    &:hover { background: var(--dt-brand-bg); }
  }
  .history-list {
    flex: 1; overflow-y: auto; padding: 0 8px;
    .history-item {
      height: 44px; display: flex; align-items: center; gap: 10px; padding: 0 12px;
      border-radius: var(--dt-radius-lg); cursor: pointer; color: var(--dt-text-secondary); margin-bottom: 2px;
      &:hover { background: var(--dt-bg-hover); .del-icon { opacity: 1; } }
      &.active { background: var(--dt-brand-bg); color: var(--dt-brand-color); font-weight: 600; }
      .chat-title { flex: 1; @include text-ellipsis; font-size: 13px; }
      .del-icon { opacity: 0; font-size: 14px; transition: var(--dt-transition-fast); &:hover { color: var(--dt-error-color); } }
    }
  }
}

// 2. 对话容器
.ai-chat-container {
  flex: 1; display: flex; flex-direction: column; background: var(--dt-bg-card); position: relative;
  .chat-header {
    height: 56px; padding: 0 24px; border-bottom: 1px solid var(--dt-border-light);
    display: flex; align-items: center; justify-content: space-between;
    .title-info { display: flex; align-items: center; gap: 8px; }
    .ai-badge { background: var(--dt-brand-color); color: var(--dt-text-white); font-size: 10px; padding: 1px 4px; border-radius: var(--dt-radius-sm); }
    .chat-title { font-size: 15px; font-weight: 600; }
    .model-selector { font-size: 13px; color: var(--dt-text-tertiary); cursor: pointer; }
  }
}

.chat-body {
  flex: 1; overflow-y: auto; padding: 24px 0;
  &::-webkit-scrollbar { width: 4px; }
}

.welcome-view {
  height: 80%; display: flex; align-items: center; justify-content: center;
  .welcome-card {
    text-align: center; max-width: 500px;
    .ai-icon-large { font-size: 48px; color: var(--dt-brand-color); margin-bottom: 20px; }
    h1 { font-size: 20px; color: var(--dt-text-primary); margin-bottom: 12px; }
    p { color: var(--dt-text-tertiary); font-size: 14px; line-height: 1.6; margin-bottom: 24px; }
    .feature-chips {
      display: flex; flex-wrap: wrap; justify-content: center; gap: 8px;
      .feature-chip {
        padding: 6px 14px; background: var(--dt-bg-input); border: 1px solid var(--dt-border-light);
        border-radius: 16px; font-size: 13px; color: var(--dt-text-secondary); cursor: pointer;
        transition: var(--dt-transition-fast);
        &:hover { border-color: var(--dt-brand-color); color: var(--dt-brand-color); background: var(--dt-brand-bg); }
      }
    }
  }
}

.message-list {
  max-width: 800px; margin: 0 auto; padding: 0 20px;
}

.msg-row {
  display: flex; gap: 16px; margin-bottom: 32px;
  &.user { flex-direction: row-reverse; }
}

.msg-avatar {
  width: 36px; height: 36px; border-radius: var(--dt-radius-lg); flex-shrink: 0;
  @include flex-center; font-size: 18px;
  .msg-row.assistant & { background: var(--dt-brand-color); color: var(--dt-text-white); }
  .msg-row.user & { background: var(--dt-bg-hover); color: var(--dt-text-secondary); font-size: 14px; font-weight: 600; }
}

.msg-bubble {
  max-width: calc(100% - 100px); padding: 12px 16px; border-radius: var(--dt-radius-lg); line-height: 1.6; font-size: 14px;
  &.assistant {
    background: var(--dt-bg-input); border: 1px solid var(--dt-border-light); color: var(--dt-text-primary);
    border-top-left-radius: 2px;
  }
  &.user {
    background: var(--dt-brand-color); color: var(--dt-text-white); border-top-right-radius: 2px;
  }
}

.typing-cursor {
  display: inline-block; width: 2px; height: 16px; background: var(--dt-brand-color);
  margin-left: 4px; opacity: 0.6; vertical-align: middle;
}

.msg-footer {
  margin-top: 12px; padding-top: 8px; border-top: 1px solid var(--dt-border-light);
  .msg-actions {
    display: flex; gap: 12px;
    button {
      background: none; border: none; cursor: pointer; color: var(--dt-text-quaternary);
      font-size: 16px; transition: var(--dt-transition-fast); &:hover { color: var(--dt-brand-color); }
    }
  }
}

// 3. 底部
.chat-footer {
  padding: 0 24px 24px;
  .prompt-chips {
    display: flex; gap: 8px; margin-bottom: 12px; overflow-x: auto; padding-bottom: 4px;
    &::-webkit-scrollbar { height: 0; }
    .p-chip {
      white-space: nowrap; padding: 6px 12px; background: var(--dt-bg-card); border: 1px solid var(--dt-border-light);
      border-radius: 16px; font-size: 12px; color: var(--dt-text-secondary); cursor: pointer;
      transition: var(--dt-transition-fast); &:hover { border-color: var(--dt-brand-color); color: var(--dt-brand-color); background: var(--dt-brand-bg); }
    }
  }
}

.input-box-v2 {
  background: var(--dt-bg-input); border: 1.5px solid var(--dt-border-light); border-radius: var(--dt-radius-lg);
  padding: 12px; display: flex; flex-direction: column; transition: var(--dt-transition-fast);
  &:focus-within { border-color: var(--dt-brand-color); background: var(--dt-bg-card); }
  
  textarea {
    border: none; background: transparent; outline: none; resize: none;
    font-size: 14px; line-height: 1.5; min-height: 24px; max-height: 160px;
    &::placeholder { color: var(--dt-text-quaternary); }
  }
  
  .btn-group {
    display: flex; justify-content: flex-end; align-items: center; gap: 12px; margin-top: 8px;
    .stop-btn {
      font-size: 12px; color: var(--dt-text-tertiary); display: flex; align-items: center; gap: 4px;
      background: none; border: none; cursor: pointer; &:hover { color: var(--dt-error-color); }
    }
    .send-btn {
      width: 32px; height: 32px; border-radius: var(--dt-radius-lg); background: var(--dt-brand-color); color: var(--dt-text-white);
      border: none; cursor: pointer; transition: var(--dt-transition-fast); @include flex-center;
      &:disabled { background: var(--dt-bg-hover); cursor: not-allowed; }
      &:hover:not(:disabled) { background: var(--dt-brand-hover); }
    }
  }
}

// 4. 总结侧板
.ai-summary-panel {
  width: 320px; background: var(--dt-bg-body); border-left: 1px solid var(--dt-border-light);
  display: flex; flex-direction: column;
  .summary-header { height: 56px; padding: 0 16px; display: flex; align-items: center; justify-content: space-between; font-weight: 600; border-bottom: 1px solid var(--dt-border-light); }
  .summary-body { flex: 1; overflow-y: auto; padding: 20px; font-size: 13px; line-height: 1.7; }
}

.slide-enter-active, .slide-leave-active { transition: opacity 0.3s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; }
</style>
