<template>
  <div class="chat-panel-container">
    <!-- 未选择会话时的精致欢迎页 -->
    <div v-if="!session" class="welcome-screen">
      <div class="welcome-content">
        <div class="welcome-logo">IM</div>
        <h2>高效办公，即时协同</h2>
        <p>对标钉钉 8.2 的企业级通讯体验</p>
      </div>
    </div>

    <template v-else>
      <div class="chat-main-layout">
        <!-- 聊天主体区 -->
        <div class="chat-body-wrapper">
          <!-- 头部 (严格对齐) -->
          <ChatHeader 
            :session="session" 
            :show-detail="showDetail"
            @toggle-sidebar="handleToggleDetail"
            @voice-call="handleStartCall"
            @video-call="handleStartVideo"
          />

          <!-- 消息流 (极致流畅) -->
          <MessageList 
            ref="msgListRef"
            :session-id="session?.id"
            :messages="messages" 
            :loading="loading" 
            :current-user="currentUser" 
            :session-type="session?.type"
            @command="handleCommand"
            @load-more="handleLoadMore"
          />

          <!-- 输入框 -->
          <MessageInput
            ref="messageInputRef"
            :session="session"
            :sending="sending"
            @send="handleSend"
            @upload-file="handleUploadFile"
            @upload-image="handleUploadImage"
          />
        </div>

        <!-- 右侧详情页 (标签化) -->
        <Transition name="sidebar-anim">
          <ChatSidebar
            v-if="showDetail"
            :session="session"
            @close="showDetail = false"
          />
        </Transition>
      </div>
    </template>

    <CallDialog ref="callDialogRef" :session="session" />
    <ForwardDialog ref="forwardDialogRef" />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'
import ChatHeader from '@/components/Chat/ChatHeader.vue'
import MessageList from '@/components/Chat/MessageList.vue'
import MessageInput from '@/components/Chat/MessageInput.vue'
import ChatSidebar from '@/components/Chat/ChatSidebar.vue'
import ForwardDialog from '@/components/ForwardDialog/index.vue'
import CallDialog from '@/components/Chat/CallDialog.vue'

const props = defineProps({ session: Object })
const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])
const messages = ref([])
const loading = ref(false)
const sending = ref(false)
const showDetail = ref(false) // 默认隐藏侧边栏

const msgListRef = ref(null)

const loadHistory = async () => {
  if (!props.session?.id) return
  loading.value = true
  try {
    const res = await store.dispatch('im/message/loadMessages', { sessionId: props.session.id, pageSize: 50 })
    // 注入 isOwn 属性
    messages.value = (res || []).map(m => ({
      ...m,
      isOwn: m.senderId === currentUser.value?.id || m.isSelf
    }))
  } finally { loading.value = false; msgListRef.value?.scrollToBottom() }
}

const handleUploadFile = async (file) => {
  if (!file) return
  
  // 创建一个临时的“发送中”消息对象用于 UI 展示
  const tempId = 'temp-' + Date.now()
  const tempMsg = {
    id: tempId,
    type: 'FILE',
    content: JSON.stringify({ fileName: file.name, size: file.size }),
    senderId: currentUser.value.id,
    senderName: currentUser.value.nickname,
    senderAvatar: currentUser.value.avatar,
    timestamp: Date.now(),
    status: 'uploading',
    uploadProgress: 0,
    isOwn: true
  }
  messages.value.push(tempMsg)
  msgListRef.value?.scrollToBottom()

  try {
    // 模拟上传进度 (如果后端 API 支持真实进度，可在此处接入)
    let progress = 0
    const progressInterval = setInterval(() => {
      progress += Math.random() * 30
      if (progress >= 95) {
        clearInterval(progressInterval)
        progress = 95
      }
      const msg = messages.value.find(m => m.id === tempId)
      if (msg) msg.uploadProgress = Math.round(progress)
    }, 300)

    const res = await store.dispatch('im/message/sendMessage', {
      sessionId: props.session.id,
      type: 'FILE',
      content: file // 假设后端支持直接传 File 对象
    })

    clearInterval(progressInterval)
    // 替换临时消息
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index] = { ...res, isOwn: true, status: 'success' }
    }
  } catch (err) {
    const msg = messages.value.find(m => m.id === tempId)
    if (msg) msg.status = 'failed'
    ElMessage.error('文件上传失败')
  }
}

const handleUploadImage = async (file) => {
  if (!file) return
  
  // 创建临时图片消息
  const tempId = 'temp-img-' + Date.now()
  const reader = new FileReader()
  reader.onload = async (e) => {
    const tempMsg = {
      id: tempId,
      type: 'IMAGE',
      content: JSON.stringify({ imageUrl: e.target.result }),
      senderId: currentUser.value.id,
      timestamp: Date.now(),
      status: 'uploading',
      uploadProgress: 0,
      isOwn: true
    }
    messages.value.push(tempMsg)
    msgListRef.value?.scrollToBottom()

    try {
      const res = await store.dispatch('im/message/sendMessage', {
        sessionId: props.session.id,
        type: 'IMAGE',
        content: file
      })
      
      const index = messages.value.findIndex(m => m.id === tempId)
      if (index !== -1) {
        messages.value[index] = { ...res, isOwn: true, status: 'success' }
      }
    } catch (err) {
      const msg = messages.value.find(m => m.id === tempId)
      if (msg) msg.status = 'failed'
    }
  }
  reader.readAsDataURL(file)
}

const handleSend = async (payload) => {
  const msg = await store.dispatch('im/message/sendMessage', { sessionId: props.session.id, ...payload })
  if (msg) {
    msg.isOwn = true 
    messages.value.push(msg)
    msgListRef.value?.scrollToBottom()
  }
}

const handleToggleDetail = () => { showDetail.value = !showDetail.value }

watch(() => props.session, () => {
  messages.value = []
  if (props.session) loadHistory()
}, { immediate: true })
</script>

<style scoped lang="scss">
.chat-panel-container { display: flex; flex-direction: column; height: 100%; flex: 1; min-width: 0; background: #fff; position: relative; }
.chat-main-layout { display: flex; width: 100%; height: 100%; overflow: hidden; }
.chat-body-wrapper { flex: 1; display: flex; flex-direction: column; height: 100%; min-width: 0; background: #f5f5f5; border-right: 1px solid #f2f3f5; }
.welcome-screen { display: flex; align-items: center; justify-content: center; height: 100%; background: #fff;
  .welcome-content { text-align: center; .welcome-logo { width: 64px; height: 64px; background: #1677ff; border-radius: 16px; color: #fff; font-size: 24px; font-weight: 800; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; }
    h2 { font-size: 24px; color: #1f2329; margin-bottom: 8px; }
    p { color: #8f959e; font-size: 14px; }
  }
}
.chat-sidebar-premium {
  width: 280px; height: 100%; background: #fff; border-left: 1px solid #f2f3f5; display: flex; flex-direction: column; z-index: 50;
  .sidebar-header { height: 60px; padding: 0 16px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #f2f3f5;
    .title { font-size: 15px; font-weight: 600; color: #1f2329; }
    .close-btn { border: none; background: transparent; cursor: pointer; color: #8f959e; &:hover { color: #1677ff; } }
  }
  .sidebar-tabs { flex: 1; display: flex; flex-direction: column;
    :deep(.el-tabs__header) { margin: 0; padding: 0 16px; }
    :deep(.el-tabs__content) { flex: 1; overflow: hidden; }
  }
}
.tab-content-scroller { height: 100%; overflow-y: auto; padding: 12px; }
.sidebar-search-box { padding-bottom: 12px; .inner-search { width: 100%; height: 30px; background: #f3f3f3; border: none; border-radius: 4px; padding: 0 12px; font-size: 12px; } }
.empty-placeholder-mini { display: flex; flex-direction: column; align-items: center; justify-content: center; color: #c9cdd4;
  .material-icons-outlined { font-size: 40px; margin-bottom: 8px; }
}
</style>
