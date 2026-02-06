<template>
  <div class="ai-assistant-panel">
    <!-- 聊天消息区域 -->
    <div
      ref="messagesContainer"
      class="chat-messages"
    >
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message', msg.role]"
      >
        <div class="message-avatar">
          <el-avatar
            :size="32"
            :src="msg.role === 'user' ? userAvatar : aiAvatar"
          />
        </div>
        <div class="message-content">
          <div class="message-text">
            {{ msg.content }}
          </div>
          <div class="message-time">
            {{ msg.time }}
          </div>
        </div>
      </div>
      <div
        v-if="isTyping"
        class="message assistant typing"
      >
        <div class="message-avatar">
          <el-avatar
            :size="32"
            :src="aiAvatar"
          />
        </div>
        <div class="message-content">
          <div class="typing-indicator">
            <span />
            <span />
            <span />
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="2"
        placeholder="输入消息与AI助手对话..."
        :disabled="isTyping"
        @keydown.enter.exact="sendMessage"
        @keydown.enter.shift.prevent
      />
      <div class="input-actions">
        <div class="input-tips">
          <span class="tip-text">按 Enter 发送，Shift + Enter 换行</span>
        </div>
        <el-button
          type="primary"
          size="small"
          :disabled="!inputText.trim() || isTyping"
          @click="sendMessage"
        >
          发送
        </el-button>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <el-button
        text
        size="small"
        @click="clearConversation"
      >
        <el-icon><Delete /></el-icon>
        清空对话
      </el-button>
      <el-dropdown
        trigger="click"
        @command="changeModel"
      >
        <el-button
          text
          size="small"
        >
          当前模型: {{ currentModel }}
          <el-icon><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="model in models"
              :key="model"
              :command="model"
            >
              {{ model }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, ArrowDown } from '@element-plus/icons-vue'
import { chat, clearConversation as apiClearConversation, getSupportedModels } from '@/api/im'
import { useUserStore } from '@/store/modules/user'
import { formatTime } from '@/utils/format'

const userStore = useUserStore()

const isUnmounted = ref(false) // 标记组件是否已卸载

const messages = ref([])
const inputText = ref('')
const isTyping = ref(false)
const conversationId = ref('')
const currentModel = ref('qwen')
const models = ref(['qwen', 'gpt-3.5-turbo', 'gpt-4', 'wenxin', 'hunyuan', 'spark'])
const messagesContainer = ref(null)

const userAvatar = ref(userStore.avatar || '')
const aiAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')

// 发送消息
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || isTyping.value) {return}

  // 添加用户消息
  const userMessage = {
    role: 'user',
    content: text,
    time: formatTime(new Date())
  }
  messages.value.push(userMessage)
  inputText.value = ''

  // 滚动到底部
  await nextTick()
  if (isUnmounted.value) {return}
  scrollToBottom()

  // 显示打字状态
  isTyping.value = true

  try {
    const res = await chat({
      content: text,
      conversationId: conversationId.value,
      userId: userStore.userId,
      model: currentModel.value
    })

    if (res.code === 200 && res.data) {
      // 更新会话ID
      conversationId.value = res.data.conversationId

      // 添加AI回复
      const aiMessage = {
        role: 'assistant',
        content: res.data.content,
        time: formatTime(new Date())
      }
      messages.value.push(aiMessage)
    } else {
      ElMessage.error(res.msg || 'AI回复失败')
    }
  } catch (error) {
    console.error('AI对话失败:', error)
    ElMessage.error('AI对话失败，请稍后重试')
    // 移除用户消息
    messages.value.pop()
  } finally {
    isTyping.value = false
  }
  await nextTick()
  if (isUnmounted.value) {return}
  scrollToBottom()
}

// 清空对话
const clearConversation = async () => {
  if (messages.value.length === 0) {
    ElMessage.info('暂无对话记录')
    return
  }

  if (conversationId.value) {
    try {
      await apiClearConversation(conversationId.value, userStore.userId)
    } catch (error) {
      console.error('清空对话失败:', error)
    }
  }

  messages.value = []
  conversationId.value = ''
  ElMessage.success('对话已清空')
}

// 切换模型
const changeModel = model => {
  currentModel.value = model
  ElMessage.info(`已切换至 ${model} 模型`)
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 加载支持的模型
const loadModels = async () => {
  try {
    const res = await getSupportedModels()
    if (res.code === 200 && res.data) {
      models.value = res.data
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
  }
}

onMounted(() => {
  loadModels()
})

onUnmounted(() => {
  isUnmounted.value = true // 标记组件已卸载
})
</script>

<script>
export default {
  name: 'AIAssistantPanel'
}
</script>

<style scoped>
.ai-assistant-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #fff;
  border-radius: var(--dt-radius-md);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #f5f5f5;
}

.message {
  display: flex;
  margin-bottom: 16px;
  gap: 8px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
}

.message.user .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-text {
  padding: 10px 14px;
  border-radius: var(--dt-radius-md);
  line-height: 1.5;
  word-wrap: break-word;
}

.message.assistant .message-text {
  background-color: #fff;
  border: 1px solid #e0e0e0;
}

.message.user .message-text {
  background-color: #007aff;
  color: #fff;
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.message.user .message-time {
  text-align: right;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: var(--dt-radius-md);
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background-color: #999;
  border-radius: var(--dt-radius-full);
  animation: typingPulse 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

.chat-input {
  padding: 12px 16px;
  border-top: 1px solid #e0e0e0;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.tip-text {
  font-size: 12px;
  color: #999;
}

.quick-actions {
  display: flex;
  gap: 8px;
  padding: 8px 16px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
}
</style>
