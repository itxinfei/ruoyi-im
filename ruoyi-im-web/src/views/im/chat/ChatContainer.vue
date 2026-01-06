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
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { listMessage, sendMessage as apiSendMessage } from '@/api/im/message'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import { createImSocket } from '@/utils/socket/useImSocket'

const store = useStore()
const inputMessage = ref('')
const currentSession = ref(null)
const displayedMessages = ref([])
const messageListRef = ref(null)
const loadingMore = ref(false)
const hasMoreMessages = ref(true)
const defaultAvatar = '/static/avatar-default.png'

const currentSessionComputed = computed(() => store.state?.im?.currentSession || currentSession.value)

watch(currentSessionComputed, async () => {
  await nextTick()
  scrollToBottom()
}, { immediate: true })

function initializeMock() {
  displayedMessages.value = [
    { id: 1, content: '你好！有什么可以帮助你的吗？', type: 'text', time: Date.now() - 3600000, isOwn: false },
    { id: 2, content: '你好，我想了解一下项目进度', type: 'text', time: Date.now() - 1800000, isOwn: true },
    { id: 3, content: '项目进展顺利，预计下周可以完成第一阶段', type: 'text', time: Date.now() - 600000, isOwn: false }
  ]
}

function startCall() {
  ElMessage.info('通话功能开发中...')
}

async function fetchHistory() {
  if (!currentSessionComputed.value?.id) {
    initializeMock()
    return
  }
  try {
    const resp = await listMessage({ sessionId: currentSessionComputed.value.id, pageSize: 20 })
    const list = resp?.data ?? resp?.rows ?? []
    if (Array.isArray(list) && list.length) {
      displayedMessages.value = list.map(m => ({
        id: m.id ?? m.msgId ?? Date.now(),
        content: m.content,
        type: m.type ?? 'text',
        time: m.time ?? Date.now(),
        isOwn: !!m.isOwn
      }))
    } else {
      initializeMock()
    }
  } catch {
    initializeMock()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  })
}

onMounted(async () => {
  await fetchHistory()
  scrollToBottom()
  // 初始化 WebSocket 连接
  try {
    const wsUrl = import.meta.env.VITE_WS_API || 'ws://localhost:8080/ws'
    const socket = createImSocket(wsUrl, (msg) => {
      // 简单处理：若来自当前会话，投递到 UI
      if (!currentSessionComputed.value?.id) return
      if (msg?.sessionId && msg.sessionId === currentSessionComputed.value.id) {
        store.dispatch('im/receiveMessage', msg)
      }
    })
    // 断开时自动重连由 useImSocket 内部实现
    // 储存用于后续销毁（如需）
    ;(window)._ruoyiImSocket = socket
  } catch {
    // 不强制依赖 WebSocket，失败时降级为离线演示
  }
})

async function sendMessage() {
  if (!inputMessage.value.trim() || !currentSessionComputed.value) return
  const text = inputMessage.value.trim()
  const optimistic = { id: Date.now(), content: text, type: 'text', time: Date.now(), isOwn: true }
  displayedMessages.value.push(optimistic)
  inputMessage.value = ''
  scrollToBottom()
  try {
    await apiSendMessage({ sessionId: currentSessionComputed.value.id, type: 'text', content: text })
  } catch {
    ElMessage.error('发送失败，请稍后重试')
  }
}

function handleEnterPress(e) {
  if (e.shiftKey) return
  e.preventDefault()
  sendMessage()
}

function handleScrollTop() {
  if (!hasMoreMessages.value || loadingMore.value) return
  const el = messageListRef.value
  if (!el) return
  if (el.scrollTop <= 20) {
    loadMoreMessages()
  }
}

async function loadMoreMessages() {
  if (!currentSessionComputed.value?.id) return
  loadingMore.value = true
  try {
    const lastMsgId = displayedMessages.value[0]?.id
    const resp = await listMessage({ sessionId: currentSessionComputed.value.id, pageSize: 20, lastMessageId: lastMsgId })
    const more = resp?.data ?? resp?.rows ?? []
    const mapped = more.map(m => ({ id: m.id ?? m.msgId ?? Date.now(), content: m.content, type: m.type ?? 'text', time: m.time ?? Date.now(), isOwn: !!m.isOwn }))
    if (mapped.length) {
      displayedMessages.value = [...mapped, ...displayedMessages.value]
    }
    hasMoreMessages.value = mapped.length === 20
  } catch (e) {
    console.error('加载更多消息失败', e)
  } finally {
    loadingMore.value = false
  }
}
</script>

<style scoped>
.chat-content { display:flex; flex-direction:column; height:100%; }
.chat-header { display:flex; justify-content:space-between; align-items:center; height:56px; padding:0 12px; border-bottom:1px solid #eee; background:#fff; }
.header-left { display:flex; align-items:center; gap:8px; }
.header-avatar { width:36px; height:36px; border-radius:50%; }
.chat-title { font-size:14px; font-weight:600; }
.message-list { flex:1; overflow:auto; padding:12px; background:#f5f5f5; }
.message { margin:6px 0; }
.message-content { padding:8px 12px; border-radius:8px; background:#fff; display:inline-block; }
.image-preview { max-width:180px; border-radius:6px; }
.chat-input-area { padding:8px; border-top:1px solid #eee; display:flex; align-items:center; gap:8px; }
.message-input { width:100%; min-height:40px; padding:6px 8px; border:1px solid #ddd; border-radius:6px; resize:none; }
.send-btn { padding:6px 12px; background:#1677ff; color:#fff; border:none; border-radius:6px; }
.loading-more { text-align:center; padding:6px; color:#888; }
.icon-btn { background:#f0f2f5; border:none; padding:6px 10px; border-radius:4px; cursor:pointer; }
</style>