<template>
  <div class="chat-content">
    <template v-if="currentSession">
      <div class="chat-header">
        <div class="header-left">
          <img :src="currentSession.avatar || defaultAvatar" class="header-avatar" alt="avatar" />
          <div class="header-info">
            <span class="chat-title">{{ currentSession.name || '对话' }}</span>
          </div>
        </div>
        <div class="header-right">
          <button @click="startCall" class="icon-btn">语音</button>
          <button @click="startCall" class="icon-btn">视频</button>
        </div>
      </div>

      <div ref="messageListRef" class="message-list" @scroll="handleScrollTop">
        <div v-for="msg in displayedMessages" :key="msg.id" class="message">
          <div class="message-content" v-if="msg.type === 'text'">{{ msg.content }}</div>
          <div class="message-content" v-else-if="msg.type === 'image'">
            <img :src="msg.content" class="image-preview" />
          </div>
        </div>
        <div v-if="loadingMore" class="loading-more">加载中...</div>
      </div>

      <div class="chat-input-area">
        <textarea v-model="inputMessage" placeholder="输入消息..." rows="2" @keydown.enter="handleEnterPress" class="message-input"></textarea>
        <button class="send-btn" @click="sendMessage">发送</button>
      </div>
    </template>
    <div v-else>请选择一个会话开始聊天</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'

const store = useStore()
const inputMessage = ref('')
const messageListRef = ref(null)
const loadingMore = ref(false)
const defaultAvatar = '/static/avatar-default.png'

// 从 store 获取会话相关数据
const currentSession = computed(() => store.state.im?.currentSession)
const displayedMessages = computed(() => {
  const sessionId = currentSession.value?.id
  if (!sessionId) return []

  const messageList = store.getters['im/messagesBySession'](sessionId) || []
  return messageList
})

const hasMoreMessages = computed(() => {
  const sessionId = currentSession.value?.id
  const messageList = store.getters['im/messagesBySession'](sessionId) || []
  return messageList.length >= 20 // 每页20条
})

const sessionName = computed(() => currentSession.value?.name || '对话')
const sessionAvatar = computed(() => currentSession.value?.avatar || defaultAvatar)
const isGroupChat = computed(() => currentSession.value?.type === 'group')

// 监听会话变化，自动滚动到底部
watch(currentSession, async (newSession) => {
  if (newSession?.id) {
    await nextTick()
    scrollToBottom()
  }
})

function startCall() {
  ElMessage.info('语音/视频通话功能开发中...')
}

function handleEnterPress(e) {
  if (e.shiftKey) return
  e.preventDefault()
  sendMessage()
}

async function sendMessage() {
  const text = inputMessage.value.trim()
  if (!text || !currentSession.value) return

  // 构建临时消息用于乐观更新
  const tempId = `temp_${Date.now()}`
  const optimistic = {
    id: tempId,
    content: text,
    type: 'text',
    timestamp: Date.now(),
    time: new Date().toISOString(),
    status: 'sending',
    isOwn: true
  }

  // 通过 store 发送消息
  inputMessage.value = ''
  scrollToBottom()

  await store.dispatch('im/sendMessage', {
    sessionId: currentSession.value.id,
    type: 'text',
    content: text,
    tempId // 传递临时ID用于去重
  })
}

async function fetchHistory() {
  if (!currentSession.value?.id) return

  loadingMore.value = true
  try {
    await store.dispatch('im/loadMessages', {
      sessionId: currentSession.value.id,
      page: 1,
      pageSize: 20
    })
  } finally {
    loadingMore.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

async function handleScrollTop() {
  if (!hasMoreMessages.value || loadingMore.value) return

  const el = messageListRef.value
  if (!el) return

  if (el.scrollTop <= 20) {
    await fetchHistory()
  }
}

// 组件挂载时初始化
onMounted(async () => {
  // 加载会话列表
  await store.dispatch('im/loadSessions')
  // 加载联系人列表（用于私聊）
  await store.dispatch('im/loadContacts')
  // 加载群组列表
  await store.dispatch('im/loadGroups')
  // 加载未读消息数
  await store.dispatch('im/getTotalUnreadCount')
})

// 组件卸载时清理
onUnmounted(() => {
  // 清理临时数据
})
</script>

<style scoped>
.chat-content { display: flex; flex-direction: column; height: 100%; }
.chat-header { display: flex; justify-content: space-between; align-items: center; height: 56px; padding: 0 12px; border-bottom: 1px solid #eee; background: #fff; }
.header-left { display: flex; align-items: center; gap: 8px; }
.header-avatar { width: 36px; height: 36px; border-radius: 50%; }
.chat-title { font-size: 14px; font-weight: 600; }
.message-list { flex: 1; overflow: auto; padding: 12px; background: #f5f5f5; flex: 1; }
.message { margin: 6px 0; }
.message-content { padding: 8px 12px; border-radius: 8px; background: #fff; display: inline-block; }
.image-preview { max-width: 180px; border-radius: 6px; }
.chat-input-area { padding: 8px; border-top: 1px solid #eee; display: flex; align-items: center; gap: 8px; }
.message-input { width: 100%; min-height: 40px; padding: 6px 8px; border: 1px solid #ddd; border-radius: 6px; resize: none; }
.send-btn { padding: 6px 12px; background: #1677ff; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.icon-btn { background: #f0f2f5; border: none; padding: 6px 10px; border-radius: 4px; cursor: pointer; }
</style>
