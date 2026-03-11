<template>
  <div class="chat-panel-container">
    <!-- 1. 精致欢迎页 -->
    <ImEmpty 
      v-if="!session"
      title="高效办公，即时协同"
      description="对标钉钉 8.2 的企业级通讯体验"
      action-text="发起群聊"
      @action="handleOpenCreateGroup"
    >
      <template #icon><div class="welcome-logo-box">IM</div></template>
    </ImEmpty>

    <!-- 2. 聊天主体 -->
    <template v-else>
      <div class="chat-main-canvas">
        <div class="chat-main-canvas__body">
          <!-- 头部 (强制 Key 刷新，解决重构失效) -->
          <ChatHeader 
            :key="session.id"
            :session="session" 
            @toggle-sidebar="handleHeaderClick"
            @voice-call="handleVoiceCall"
            @video-call="handleVideoCall"
            @search="handleOpenSearch"
            @clear="handleClearMessages"
          />

          <!-- 消息流 -->
          <MessageList
            ref="msgListRef"
            :messages="messages"
            :loading="loading"
            :session-type="session?.type"
            :conversation-id="session?.id"
            @command="handleMessageCommand"
            @load-more="handleLoadMore"
            @show-user="handleShowUserProfile"
            @retry="handleRetryMessage"
          />

          <!-- 输入框 -->
          <footer class="chat-input-area">
            <MessageInput
              ref="messageInputRef"
              :session="session"
              :sending="sending"
              @send="handleSend"
              @upload-file="handleUploadFile"
              @upload-image="handleUploadImage"
            />
          </footer>
        </div>
      </div>
    </template>

    <!-- 3. 弹窗群 (严格身份校验) -->
    <UserProfileDialog 
      v-model="showUserDetail" 
      :user-id="activeUserId" 
      @start-call="handleStartCallFromProfile" 
    />
    
    <GroupProfileDialog 
      v-model="showGroupDetail" 
      :group-id="session?.type === 'GROUP' ? session?.targetId : null" 
      @show-user="handleShowUserProfile"
    />

    <CallDialog ref="callDialogRef" :session="session" />
    <GlobalSearch v-model:visible="showGlobalSearch" @select="handleSearchSelect" />
    <ForwardDialog ref="forwardDialogRef" />
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import ChatHeader from '@/components/Chat/ChatHeader.vue'
import MessageList from '@/components/Chat/MessageList.vue'
import MessageInput from '@/components/Chat/MessageInput.vue'
import UserProfileDialog from '@/components/Contacts/UserProfileDialog.vue'
import GroupProfileDialog from '@/components/Contacts/GroupProfileDialog.vue'
import ForwardDialog from '@/components/ForwardDialog/index.vue'
import CallDialog from '@/components/Chat/CallDialog.vue'
import ImEmpty from '@/components/Common/ImEmpty.vue'
import GlobalSearch from '@/components/Chat/GlobalSearch.vue'

const props = defineProps({ session: Object })
const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])
const messages = ref([])
const loading = ref(false)
const sending = ref(false)

const showUserDetail = ref(false)
const showGroupDetail = ref(false)
const activeUserId = ref(null)
const showGlobalSearch = ref(false)

const msgListRef = ref(null)
const callDialogRef = ref(null)
const forwardDialogRef = ref(null)

const loadHistory = async () => {
  if (!props.session?.id) return
  loading.value = true
  try {
    const res = await store.dispatch('im/message/loadMessages', { sessionId: props.session.id, pageSize: 50 })
    messages.value = (res || []).map(m => ({ ...m, isOwn: m.senderId === currentUser.value?.id || m.isSelf }))
  } finally { loading.value = false; nextTick(() => msgListRef.value?.scrollToBottom()) }
}

const handleHeaderClick = () => {
  if (!props.session) return

  if (props.session.type === 'GROUP') {
    showGroupDetail.value = true
  } else {
    // 优先使用 peerUserId，其次使用 targetId
    activeUserId.value = props.session.peerUserId || props.session.targetId
    showUserDetail.value = true
  }
}

const handleShowUserProfile = (userId) => {
  activeUserId.value = userId
  showUserDetail.value = true
}

const handleVoiceCall = (s) => {
  const peerId = s.peerUserId || s.targetId
  console.log('Trigger Voice Call for:', peerId)
  callDialogRef.value?.open('voice', { peerId, peerName: s.name, peerAvatar: s.avatar })
}

const handleVideoCall = (s) => {
  const peerId = s.peerUserId || s.targetId
  console.log('Trigger Video Call for:', peerId)
  callDialogRef.value?.open('video', { peerId, peerName: s.name, peerAvatar: s.avatar })
}

const handleStartCallFromProfile = (p) => {
  callDialogRef.value?.open(p.type, { peerId: p.user.userId, peerName: p.user.nickname, peerAvatar: p.user.avatar })
}

const handleOpenSearch = () => { showGlobalSearch.value = true }
const handleSearchSelect = (res) => { if (res.messageId) msgListRef.value?.scrollToMessage(res.messageId) }

const handleSend = async (payload) => {
  const msg = await store.dispatch('im/message/sendMessage', { sessionId: props.session.id, ...payload })
  if (msg) { msg.isOwn = true; messages.value.push(msg); nextTick(() => msgListRef.value?.scrollToBottom()) }
}

const handleMessageCommand = (c, m) => {
  if (c === 'forward') forwardDialogRef.value?.open(m)
  else if (c === 'recall') store.dispatch('im/message/recallMessage', { sessionId: props.session.id, messageId: m.id })
}

const handleClearMessages = () => {
  ElMessageBox.confirm('清空聊天记录？', '提示', { type: 'warning' }).then(async () => {
    await store.dispatch('im/message/clearMessages', props.session.id)
    messages.value = []; ElMessage.success('已清空')
  })
}

const handleOpenCreateGroup = () => ElMessage.info('请通过通讯录发起')
const handleLoadMore = () => {}
const handleUploadFile = (f) => {}
const handleUploadImage = (f) => {}

const handleRetryMessage = async (msg) => {
  if (!msg) return
  // 更新消息状态为发送中
  const index = messages.value.findIndex(m => m.id === msg.id)
  if (index !== -1) {
    messages.value[index].status = 'sending'
  }
  try {
    const result = await store.dispatch('im/message/sendMessage', { sessionId: props.session.id, content: msg.content, type: msg.type })
    if (result && index !== -1) {
      messages.value[index].status = 'sent'
      messages.value[index].id = result.id
    }
  } catch (error) {
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    ElMessage.error('消息发送失败，请重试')
  }
}

watch(() => props.session, (newSession) => {
  messages.value = []
  if (newSession) {
    loadHistory()
    // 切换会话时重置详情状态，防止影子状态残留
    showUserDetail.value = false
    showGroupDetail.value = false
  }
}, { immediate: true })
</script>

<style scoped lang="scss">
.chat-panel-container { height: 100%; flex: 1; min-width: 0; background: var(--dt-bg-card); display: flex; flex-direction: column; overflow: hidden; position: relative; }
.welcome-logo-box { width: 64px; height: 64px; background: var(--dt-brand-color); border-radius: var(--dt-radius-xl); color: #ffffff; font-size: 24px; font-weight: 800; display: flex; align-items: center; justify-content: center; margin: 0 auto var(--dt-spacing-xl); box-shadow: var(--dt-shadow-3); }
.chat-main-canvas { height: 100%; width: 100%; display: flex; flex-direction: column; overflow: hidden; &__body { flex: 1; display: flex; flex-direction: column; min-width: 0; position: relative; background: var(--dt-bg-chat); overflow: hidden; } }
.chat-input-area { flex-shrink: 0; background: var(--dt-bg-card); border-top: 1px solid var(--dt-border-light); min-height: 140px; z-index: 10; display: flex; flex-direction: column; }
</style>
