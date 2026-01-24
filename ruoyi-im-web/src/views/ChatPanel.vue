<template>
  <div class="chat-panel">
    <div v-if="!session" class="empty-placeholder">
      <el-empty description="选择一个会话开始聊天" />
    </div>
    <template v-else>
      <ChatHeader :session="session" />
      <MessageList 
        ref="msgListRef"
        :session-id="session?.id"
        :messages="messages" 
        :loading="loading" 
        :current-user="currentUser" 
        @delete="handleDelete"
        @recall="handleRecall"
        @reply="handleReply"
        @load-more="handleLoadMore"
        @edit="handleEdit"
      />
      <MessageInput
        :session="session"
        :sending="sending"
        :replying-message="replyingMessage"
        :editing-message="editingMessage"
        @send="handleSend"
        @cancel-reply="handleCancelReply"
        @cancel-edit="handleCancelEdit"
        @edit-confirm="handleEditConfirm"
        @start-call="handleStartCall"
        @start-video="handleStartVideo"
      />
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useStore } from 'vuex'
import ChatHeader from '@/components/Chat/ChatHeader.vue'
import MessageList from '@/components/Chat/MessageList.vue'
import MessageInput from '@/components/Chat/MessageInput.vue'
import { getMessages } from '@/api/im/message'
import { useImWebSocket } from '@/composables/useImWebSocket'

const props = defineProps({
  session: Object
})

const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])
const messages = ref([])
const loading = ref(false)
const sending = ref(false)
const noMore = ref(false)
const replyingMessage = computed(() => store.state.im.replyingMessage)
const editingMessage = ref(null)
const msgListRef = ref(null)

const { onMessage } = useImWebSocket()

const loadHistory = async () => {
  if (!props.session?.id) return
  loading.value = true
  noMore.value = false
  try {
    const res = await store.dispatch('im/loadMessages', {
      sessionId: props.session.id,
      pageSize: 50
    })
    console.log('===== ChatPanel loadHistory 开始 =====')
    console.log('ChatPanel - 当前登录用户:', currentUser.value)
    console.log('ChatPanel - 会话ID:', props.session.id)
    console.log('ChatPanel - 原始消息数量:', res?.length)

    messages.value = (res || []).map(m => {
      const transformed = transformMsg(m)
      console.log('消息详情:', {
        id: m.id,
        content: m.content,
        senderId: m.senderId,
        senderName: m.senderName,
        后端isSelf: m.isSelf,
        前端isOwn: transformed.isOwn,
        当前userId: currentUser.value?.id,
        匹配结果: m.senderId === currentUser.value?.id
      })
      return transformed
    })
    console.log('===== ChatPanel loadHistory 结束 =====')
  } finally {
    loading.value = false
    msgListRef.value?.scrollToBottom()
  }
}

const handleLoadMore = async () => {
  if (loading.value || noMore.value) return
  
  const firstMsg = messages.value[0]
  if (!firstMsg) return

  loading.value = true
  const oldHeight = msgListRef.value?.$refs.listRef.scrollHeight
  
  try {
    const newMsgs = await store.dispatch('im/loadMessages', {
      sessionId: props.session.id,
      lastMessageId: firstMsg.id,
      pageSize: 20,
      isLoadMore: true
    })
    
    if (newMsgs && newMsgs.length > 0) {
      messages.value = [...newMsgs, ...messages.value]
      msgListRef.value?.maintainScroll(oldHeight)
    } else {
      noMore.value = true
    }
  } finally {
    loading.value = false
  }
}

const transformMsg = (m) => {
  // 优先使用后端返回的 isSelf 字段，后端根据 userId header 判断
  // 如果后端返回了 isSelf（布尔值），直接使用；否则回退到前端判断
  const isOwn = m.isSelf === true || m.isSelf === false
    ? m.isSelf  // 后端已明确返回 isSelf 值
    : m.senderId === currentUser.value?.id  // 前端回退判断

  // 确保消息类型存在，默认为TEXT
  const messageType = m.type || m.messageType || 'TEXT'
  
  // 调试日志：查看消息原始数据
  if (!m.type) {
    console.warn('[ChatPanel] 消息缺少type字段:', m)
  }

  return {
    ...m,
    type: messageType,  // 确保type字段存在
    isOwn,
    timestamp: m.sendTime || m.createTime || m.timestamp
  }
}

const handleSend = async (content) => {
  sending.value = true
  try {
    const msg = await store.dispatch('im/sendMessage', {
      sessionId: props.session.id,
      type: 'TEXT',
      content,
      replyToMessageId: replyingMessage.value?.id
    })
    messages.value.push(transformMsg(msg))
    store.commit('im/SET_REPLYING_MESSAGE', null)
  } finally {
    sending.value = false
    msgListRef.value?.scrollToBottom()
  }
}

// Websocket handling
onMessage((msg) => {
  if (msg.conversationId === props.session?.id) {
    const transformedMsg = transformMsg(msg)
    messages.value.push(transformedMsg)
    msgListRef.value?.scrollToBottom()
    
    // 新消息提醒
    if (!transformedMsg.isOwn) {
      // 动态导入提醒工具,避免循环依赖
      import('@/utils/messageNotification').then(({ showMessageNotification, shouldNotify }) => {
        if (shouldNotify(msg, currentUser.value, props.session)) {
          showMessageNotification({
            title: msg.senderName || '新消息',
            body: msg.content || '[消息]',
            icon: msg.senderAvatar || '',
            sound: true,
            notification: true,
            titleFlash: true
          })
        }
      })
    }
  }
})

watch(() => props.session, () => {
  messages.value = []
  loadHistory()
})

const handleDelete = async (messageId) => {
  try {
    await store.dispatch('im/deleteMessage', messageId)
    // 移除本地消息
    const index = messages.value.findIndex(m => m.id === messageId)
    if (index !== -1) {
      messages.value.splice(index, 1)
    }
  } catch (error) {
    console.error('删除失败', error)
  }
}

const handleRecall = async (messageId) => {
  try {
    await store.dispatch('im/recallMessage', messageId)
    // 更新本地消息状态
    const index = messages.value.findIndex(m => m.id === messageId)
    if (index !== -1) {
      messages.value[index].type = 'RECALLED' // 或其他处理
      messages.value[index].content = '消息已撤回'
    }
  } catch (error) {
    console.error('撤回失败', error)
  }
}

const handleReply = (message) => {
  store.commit('im/SET_REPLYING_MESSAGE', message)
}

const handleEdit = (message) => {
  editingMessage.value = message
}

const handleCancelEdit = () => {
  editingMessage.value = null
}

const handleEditConfirm = async (content) => {
  if (!editingMessage.value) return
  
  try {
    await store.dispatch('im/editMessage', {
      messageId: editingMessage.value.id,
      content: content
    })
    
    // 更新本地消息列表
    const index = messages.value.findIndex(m => m.id === editingMessage.value.id)
    if (index !== -1) {
      messages.value[index].content = content
      messages.value[index].isEdited = true // 标记已编辑
    }
    
    editingMessage.value = null
    ElMessage.success('已编辑')
  } catch (error) {
    console.error('编辑失败', error)
  }
}

const handleCancelReply = () => {
  store.commit('im/SET_REPLYING_MESSAGE', null)
}

// 通话功能（待实现）
const handleStartCall = () => {
  console.log('语音通话功能开发中...')
  // TODO: 实现语音通话
}

const handleStartVideo = () => {
  console.log('视频通话功能开发中...')
  // TODO: 实现视频通话
}

onMounted(() => {
  if (props.session) loadHistory()
  
  // 请求浏览器通知权限
  import('@/utils/messageNotification').then(({ requestNotificationPermission }) => {
    requestNotificationPermission().then(permission => {
      if (permission === 'granted') {
        console.log('[消息提醒] 通知权限已授予')
      } else if (permission === 'denied') {
        console.warn('[消息提醒] 通知权限被拒绝')
      }
    })
  })
})
</script>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: #f5f7fa;

  .dark & {
    background: #0f172a;
  }
}

.empty-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  flex: 1;
}
</style>
