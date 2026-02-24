<template>
  <div class="assistant-panel">
    <!-- 左侧会话列表 -->
    <aside class="assistant-sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">
          <span class="material-icons-outlined">auto_awesome</span>
          AI 助理
        </h2>
        <el-button
          circle
          size="small"
          @click="createNewChat"
        >
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>

      <div class="sidebar-search">
        <el-input
          v-model="searchQuery"
          placeholder="搜索对话历史..."
          :prefix-icon="Search"
          clearable
          size="small"
        />
      </div>

      <el-scrollbar class="chat-list-scroll">
        <div class="chat-list">
          <div
            v-for="chat in filteredChats"
            :key="chat.id"
            class="chat-item"
            :class="{ active: currentChat?.id === chat.id }"
            @click="selectChat(chat)"
          >
            <div class="chat-icon">
              <span class="material-icons-outlined">smart_toy</span>
            </div>
            <div class="chat-info">
              <div class="chat-title">
                {{ chat.title }}
              </div>
              <div class="chat-preview">
                {{ chat.preview }}
              </div>
            </div>
            <div class="chat-meta">
              <span class="chat-time">{{ chat.time }}</span>
              <el-dropdown
                trigger="click"
                @command="handleChatAction($event, chat)"
              >
                <span
                  class="more-btn"
                  @click.stop
                >
                  <el-icon><MoreFilled /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="rename">
                      <el-icon><Edit /></el-icon>重命名
                    </el-dropdown-item>
                    <el-dropdown-item
                      command="delete"
                      divided
                    >
                      <el-icon><Delete /></el-icon>删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </el-scrollbar>

      <!-- 快捷功能 -->
      <div class="quick-actions">
        <div class="quick-title">
          快捷功能
        </div>
        <div class="action-grid">
          <div
            v-for="action in quickActions"
            :key="action.key"
            class="action-item"
            @click="triggerQuickAction(action)"
          >
            <span
              class="material-icons-outlined"
              :style="{ color: action.color }"
            >{{ action.icon }}</span>
            <span class="action-label">{{ action.label }}</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- 右侧聊天区域 -->
    <main class="assistant-main">
      <template v-if="currentChat">
        <!-- 聊天头部 -->
        <header class="chat-header">
          <div class="header-info">
            <h3 class="chat-title">
              {{ currentChat.title }}
            </h3>
            <span class="model-badge">{{ currentModel.name }}</span>
          </div>
          <div class="header-actions">
            <el-tooltip content="切换模型">
              <el-button
                text
                size="small"
                @click="showModelSelector = true"
              >
                <span class="material-icons-outlined">swap_horiz</span>
              </el-button>
            </el-tooltip>
            <el-tooltip content="清空对话">
              <el-button
                text
                size="small"
                @click="clearChat"
              >
                <span class="material-icons-outlined">delete_sweep</span>
              </el-button>
            </el-tooltip>
          </div>
        </header>

        <!-- 消息列表 -->
        <el-scrollbar
          ref="messageScrollRef"
          class="message-list"
        >
          <div class="messages-container">
            <!-- 欢迎消息 -->
            <div
              v-if="messages.length === 0"
              class="welcome-section"
            >
              <div class="welcome-icon">
                <span class="material-icons-outlined">auto_awesome</span>
              </div>
              <h3 class="welcome-title">
                你好，我是你的 AI 助理
              </h3>
              <p class="welcome-desc">
                我可以帮你解答问题、撰写文案、分析数据、编写代码等
              </p>
              <div class="suggestion-chips">
                <div
                  v-for="suggestion in suggestions"
                  :key="suggestion"
                  class="suggestion-chip"
                  @click="sendMessage(suggestion)"
                >
                  {{ suggestion }}
                </div>
              </div>
            </div>

            <!-- 消息气泡 -->
            <div
              v-for="(msg, index) in messages"
              :key="index"
              class="message-item"
              :class="{ 'is-user': msg.role === 'user' }"
            >
              <div class="message-avatar">
                <span
                  v-if="msg.role === 'assistant'"
                  class="material-icons-outlined ai-avatar"
                >smart_toy</span>
                <DingtalkAvatar
                  v-else
                  :src="currentUser.avatar"
                  :name="currentUser.nickname || currentUser.username"
                  :size="32"
                />
              </div>
              <div class="message-content">
                <div
                  v-if="msg.role === 'assistant' && msg.isStreaming"
                  class="typing-indicator"
                >
                  <span />
                  <span />
                  <span />
                </div>
                <div
                  v-else
                  class="message-text"
                  v-html="formatMessage(msg.content)"
                />
                <div class="message-actions">
                  <el-tooltip content="复制">
                    <button @click="copyMessage(msg.content)">
                      <span class="material-icons-outlined">content_copy</span>
                    </button>
                  </el-tooltip>
                  <el-tooltip content="重新生成">
                    <button
                      v-if="msg.role === 'assistant' && index === messages.length - 1"
                      @click="regenerate"
                    >
                      <span class="material-icons-outlined">refresh</span>
                    </button>
                  </el-tooltip>
                </div>
              </div>
            </div>
          </div>
        </el-scrollbar>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-toolbar">
            <el-tooltip content="上传文件">
              <button @click="triggerUpload">
                <span class="material-icons-outlined">attach_file</span>
              </button>
            </el-tooltip>
            <el-tooltip content="联网搜索">
              <button
                :class="{ active: enableSearch }"
                @click="enableSearch = !enableSearch"
              >
                <span class="material-icons-outlined">travel_explore</span>
              </button>
            </el-tooltip>
            <el-tooltip content="深度思考">
              <button
                :class="{ active: enableReasoning }"
                @click="enableReasoning = !enableReasoning"
              >
                <span class="material-icons-outlined">psychology</span>
              </button>
            </el-tooltip>
          </div>
          <div class="input-box">
            <textarea
              v-model="inputMessage"
              rows="1"
              placeholder="输入消息，按 Enter 发送，Shift + Enter 换行..."
              @keydown="handleKeydown"
              @input="autoResize"
            />
            <button
              class="send-btn"
              :disabled="!inputMessage.trim() || isSending"
              @click="sendMessage()"
            >
              <span
                v-if="isSending"
                class="material-icons-outlined spinning"
              >refresh</span>
              <span
                v-else
                class="material-icons-outlined"
              >send</span>
            </button>
          </div>
          <div class="input-footer">
            <span class="model-info">{{ currentModel.name }} · 由 AI 生成，请谨慎采纳</span>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <div
        v-else
        class="empty-state"
      >
        <EmptyState
          type="ai"
          title="开始与 AI 对话"
          description="点击左侧新建对话或选择历史记录"
        >
          <template #action>
            <el-button
              type="primary"
              size="large"
              @click="createNewChat"
            >
              <el-icon><Plus /></el-icon>
              新建对话
            </el-button>
          </template>
        </EmptyState>
      </div>
    </main>

    <!-- 模型选择器弹窗 -->
    <el-dialog
      v-model="showModelSelector"
      title="选择 AI 模型"
      width="400px"
      align-center
    >
      <div class="model-list">
        <div
          v-for="model in aiModels"
          :key="model.id"
          class="model-option"
          :class="{ active: currentModel.id === model.id }"
          @click="selectModel(model)"
        >
          <div
            class="model-icon"
            :style="{ background: model.color }"
          >
            <span class="material-icons-outlined">smart_toy</span>
          </div>
          <div class="model-info">
            <div class="model-name">
              {{ model.name }}
            </div>
            <div class="model-desc">
              {{ model.description }}
            </div>
          </div>
          <el-icon
            v-if="currentModel.id === model.id"
            class="check-icon"
          >
            <Check />
          </el-icon>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, MoreFilled, Edit, Delete, Check } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import EmptyState from '@/components/Common/EmptyState.vue'

const store = useStore()

// 当前用户
const currentUser = computed(() => store.getters['user/currentUser'])

// 搜索
const searchQuery = ref('')

// 当前对话
const currentChat = ref(null)
const messages = ref([])
const inputMessage = ref('')
const isSending = ref(false)
const enableSearch = ref(false)
const enableReasoning = ref(false)
const messageScrollRef = ref(null)

// AI 模型
const aiModels = [
  { id: 'gpt-4', name: 'GPT-4', description: '强大的通用模型，适合复杂任务', color: '#10a37f' },
  { id: 'gpt-3.5', name: 'GPT-3.5', description: '快速响应，适合日常对话', color: '#19c37d' },
  { id: 'claude', name: 'Claude', description: '擅长长文本分析和创意写作', color: '#d97757' },
  { id: 'deepseek', name: 'DeepSeek', description: '国产大模型，代码能力强', color: '#4f46e5' }
]
const currentModel = ref(aiModels[0])
const showModelSelector = ref(false)

// 快捷功能
const quickActions = [
  { key: 'summary', label: '总结文档', icon: 'summarize', color: '#2196F3' },
  { key: 'translate', label: '翻译', icon: 'translate', color: '#4CAF50' },
  { key: 'code', label: '写代码', icon: 'code', color: '#9C27B0' },
  { key: 'write', label: '写文章', icon: 'edit_note', color: '#FF9800' }
]

// 建议问题
const suggestions = [
  '帮我写一份周报模板',
  '解释什么是机器学习',
  '用 JavaScript 写一个防抖函数',
  '帮我制定一个健身计划'
]

// 模拟对话历史
const chatHistory = ref([
  { id: 1, title: '前端开发技巧讨论', preview: '如何优化 React 性能？', time: '10:30' },
  { id: 2, title: '周报撰写', preview: '这是我帮你生成的周报...', time: '昨天' },
  { id: 3, title: '代码审查', preview: '这段代码可以优化...', time: '昨天' },
  { id: 4, title: '产品需求分析', preview: '根据你的需求，我建议...', time: '周一' }
])

const filteredChats = computed(() => {
  if (!searchQuery.value) {return chatHistory.value}
  return chatHistory.value.filter(chat =>
    chat.title.includes(searchQuery.value) ||
    chat.preview.includes(searchQuery.value)
  )
})

// 方法
function createNewChat() {
  const newChat = {
    id: Date.now(),
    title: '新对话',
    preview: '',
    time: '刚刚'
  }
  chatHistory.value.unshift(newChat)
  selectChat(newChat)
}

function selectChat(chat) {
  currentChat.value = chat
  messages.value = []
}

function handleChatAction(command, chat) {
  if (command === 'rename') {
    ElMessageBox.prompt('请输入新的对话名称', '重命名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: chat.title
    }).then(({ value }) => {
      chat.title = value
      ElMessage.success('重命名成功')
    })
  } else if (command === 'delete') {
    ElMessageBox.confirm('确定要删除这个对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      const index = chatHistory.value.findIndex(c => c.id === chat.id)
      if (index > -1) {
        chatHistory.value.splice(index, 1)
        if (currentChat.value?.id === chat.id) {
          currentChat.value = null
          messages.value = []
        }
      }
      ElMessage.success('删除成功')
    })
  }
}

function triggerQuickAction(action) {
  const prompts = {
    summary: '请帮我总结这份文档的要点：',
    translate: '请将以下内容翻译成中文：',
    code: '请帮我用 JavaScript 写一个函数，实现：',
    write: '请帮我写一篇关于以下主题的文章：'
  }
  inputMessage.value = prompts[action.key]
  createNewChat()
}

async function sendMessage(content) {
  const message = content || inputMessage.value.trim()
  if (!message || isSending.value) {return}

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: message,
    time: new Date()
  })

  inputMessage.value = ''
  isSending.value = true

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 模拟 AI 回复
  setTimeout(() => {
    const aiMessage = {
      role: 'assistant',
      content: generateAIResponse(message),
      isStreaming: true
    }
    messages.value.push(aiMessage)

    // 模拟流式输出
    let index = 0
    const fullContent = aiMessage.content
    aiMessage.content = ''

    const streamInterval = setInterval(() => {
      if (index < fullContent.length) {
        aiMessage.content += fullContent[index]
        index++
        scrollToBottom()
      } else {
        clearInterval(streamInterval)
        aiMessage.isStreaming = false
        isSending.value = false
      }
    }, 30)
  }, 500)
}

function generateAIResponse(userMessage) {
  // 模拟 AI 回复
  const responses = [
    '我理解你的需求。这是一个很好的问题，让我为你详细解答...',
    '根据你的描述，我建议你可以从以下几个方面入手：\n\n1. 首先分析问题\n2. 制定解决方案\n3. 执行并验证\n\n需要我详细说明某个步骤吗？',
    '这是一个很有意思的话题。我的看法是：\n\n- 从技术上讲，这是可行的\n- 从实践角度，需要注意一些细节\n- 建议先进行小规模测试\n\n你觉得这个方案如何？',
    '我已经为你准备好了相关内容。你可以根据实际情况进行调整。如果有任何疑问，随时问我！'
  ]
  return responses[Math.floor(Math.random() * responses.length)]
}

function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function autoResize(e) {
  const textarea = e.target
  textarea.style.height = 'auto'
  textarea.style.height = Math.min(textarea.scrollHeight, 200) + 'px'
}

function scrollToBottom() {
  if (messageScrollRef.value) {
    const scrollWrap = messageScrollRef.value.$el.querySelector('.el-scrollbar__wrap')
    if (scrollWrap) {
      scrollWrap.scrollTop = scrollWrap.scrollHeight
    }
  }
}

function formatMessage(content) {
  // 简单的 Markdown 格式化
  return content
    .replace(/\n/g, '<br>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
}

function copyMessage(content) {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success('已复制到剪贴板')
  })
}

function regenerate() {
  // 移除最后一条 AI 消息并重新生成
  messages.value.pop()
  isSending.value = true

  setTimeout(() => {
    const aiMessage = {
      role: 'assistant',
      content: generateAIResponse('重新生成'),
      isStreaming: true
    }
    messages.value.push(aiMessage)

    let index = 0
    const fullContent = aiMessage.content
    aiMessage.content = ''

    const streamInterval = setInterval(() => {
      if (index < fullContent.length) {
        aiMessage.content += fullContent[index]
        index++
        scrollToBottom()
      } else {
        clearInterval(streamInterval)
        aiMessage.isStreaming = false
        isSending.value = false
      }
    }, 30)
  }, 500)
}

function clearChat() {
  ElMessageBox.confirm('确定要清空当前对话吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    messages.value = []
    ElMessage.success('对话已清空')
  })
}

function selectModel(model) {
  currentModel.value = model
  showModelSelector.value = false
  ElMessage.success(`已切换到 ${model.name}`)
}

function triggerUpload() {
  ElMessage.info('文件上传功能开发中...')
}
</script>

<style scoped lang="scss">
.assistant-panel {
  display: flex;
  width: 100%;
  height: 100%;
  background: var(--dt-bg-body);
}

// 侧边栏
.assistant-sidebar {
  width: 280px;
  min-width: 280px;
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-color);
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--dt-border-color);

  .sidebar-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;

    .material-icons-outlined {
      color: #7C4DFF;
    }
  }
}

.sidebar-search {
  padding: 12px 16px;
  border-bottom: 1px solid var(--dt-border-color);
}

.chat-list-scroll {
  flex: 1;
  overflow: hidden;
}

.chat-list {
  padding: 8px;
}

.chat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.active {
    background: var(--dt-brand-color-light);
  }
}

.chat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(135deg, #7C4DFF 0%, #651FFF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .material-icons-outlined {
    color: white;
    font-size: 20px;
  }
}

.chat-info {
  flex: 1;
  min-width: 0;

  .chat-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .chat-preview {
    font-size: 12px;
    color: var(--dt-text-secondary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-top: 2px;
  }
}

.chat-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;

  .chat-time {
    font-size: 11px;
    color: var(--dt-text-tertiary);
  }

  .more-btn {
    opacity: 0;
    transition: opacity 0.2s;
    color: var(--dt-text-tertiary);
    cursor: pointer;

    &:hover {
      color: var(--dt-text-primary);
    }
  }
}

.chat-item:hover .more-btn {
  opacity: 1;
}

// 快捷功能
.quick-actions {
  padding: 16px;
  border-top: 1px solid var(--dt-border-color);
}

.quick-title {
  font-size: 12px;
  font-weight: 500;
  color: var(--dt-text-secondary);
  margin-bottom: 12px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--dt-bg-body);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-hover);
  }

  .material-icons-outlined {
    font-size: 18px;
  }

  .action-label {
    font-size: 13px;
    color: var(--dt-text-primary);
  }
}

// 主区域
.assistant-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--dt-bg-body);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--dt-border-color);
  background: var(--dt-bg-card);

  .header-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .chat-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--dt-text-primary);
      margin: 0;
    }

    .model-badge {
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 4px;
      background: var(--dt-brand-color-light);
      color: var(--dt-brand-color);
    }
  }

  .header-actions {
    display: flex;
    gap: 4px;
  }
}

.message-list {
  flex: 1;
  overflow: hidden;
}

.messages-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

// 欢迎区域
.welcome-section {
  text-align: center;
  padding: 60px 20px;

  .welcome-icon {
    width: 80px;
    height: 80px;
    border-radius: 20px;
    background: linear-gradient(135deg, #7C4DFF 0%, #651FFF 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24px;

    .material-icons-outlined {
      font-size: 40px;
      color: white;
    }
  }

  .welcome-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 12px;
  }

  .welcome-desc {
    font-size: 14px;
    color: var(--dt-text-secondary);
    margin: 0 0 32px;
  }
}

.suggestion-chips {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.suggestion-chip {
  padding: 10px 16px;
  border-radius: 20px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  font-size: 13px;
  color: var(--dt-text-primary);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--dt-brand-color);
    color: var(--dt-brand-color);
  }
}

// 消息项
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  &.is-user {
    flex-direction: row-reverse;

    .message-content {
      align-items: flex-end;

      .message-text {
        background: var(--dt-brand-color);
        color: white;
        border-radius: 12px 12px 4px 12px;
      }
    }
  }
}

.message-avatar {
  flex-shrink: 0;

  .ai-avatar {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: linear-gradient(135deg, #7C4DFF 0%, #651FFF 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 18px;
  }
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 70%;

  .message-text {
    padding: 12px 16px;
    border-radius: 12px 12px 12px 4px;
    background: var(--dt-bg-card);
    color: var(--dt-text-primary);
    font-size: 14px;
    line-height: 1.6;
    border: 1px solid var(--dt-border-color);

    code {
      background: rgba(0, 0, 0, 0.05);
      padding: 2px 6px;
      border-radius: 4px;
      font-family: monospace;
      font-size: 13px;
    }

    strong {
      font-weight: 600;
    }
  }
}

.message-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;

  button {
    padding: 4px;
    border: none;
    background: transparent;
    color: var(--dt-text-tertiary);
    cursor: pointer;
    border-radius: 4px;

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-text-primary);
    }

    .material-icons-outlined {
      font-size: 16px;
    }
  }
}

.message-item:hover .message-actions {
  opacity: 1;
}

// 输入区域
.input-area {
  padding: 16px 20px 20px;
  background: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-color);
}

.input-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;

  button {
    padding: 6px;
    border: none;
    background: transparent;
    color: var(--dt-text-tertiary);
    cursor: pointer;
    border-radius: 6px;
    transition: all 0.2s;

    &:hover, &.active {
      background: var(--dt-brand-color-light);
      color: var(--dt-brand-color);
    }

    .material-icons-outlined {
      font-size: 18px;
    }
  }
}

.input-box {
  display: flex;
  gap: 8px;
  align-items: flex-end;
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-color);
  border-radius: 12px;
  padding: 8px 12px;

  textarea {
    flex: 1;
    border: none;
    background: transparent;
    resize: none;
    font-size: 14px;
    line-height: 1.5;
    color: var(--dt-text-primary);
    max-height: 200px;

    &::placeholder {
      color: var(--dt-text-tertiary);
    }

    &:focus {
      outline: none;
    }
  }

  .send-btn {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    border: none;
    background: var(--dt-brand-color);
    color: white;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;

    &:hover:not(:disabled) {
      background: var(--dt-brand-color-hover);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .material-icons-outlined {
      font-size: 16px;
    }

    .spinning {
      animation: spin 1s linear infinite;
    }
  }
}

.input-footer {
  margin-top: 8px;
  text-align: center;

  .model-info {
    font-size: 11px;
    color: var(--dt-text-tertiary);
  }
}

// 打字动画
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 16px;
  background: var(--dt-bg-card);
  border-radius: 12px 12px 12px 4px;
  border: 1px solid var(--dt-border-color);

  span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--dt-brand-color);
    animation: typing 1.4s infinite;

    &:nth-child(2) {
      animation-delay: 0.2s;
    }

    &:nth-child(3) {
      animation-delay: 0.4s;
    }
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 空状态
.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 模型选择器
.model-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.model-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.active {
    background: var(--dt-brand-color-light);
  }
}

.model-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;

  .material-icons-outlined {
    color: white;
    font-size: 20px;
  }
}

.model-info {
  flex: 1;

  .model-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);
  }

  .model-desc {
    font-size: 12px;
    color: var(--dt-text-secondary);
    margin-top: 2px;
  }
}

.check-icon {
  color: var(--dt-brand-color);
  font-size: 20px;
}
</style>
