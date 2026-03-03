<template>
  <div :class="['dingtalk-app', isDark ? 'dark' : '']">
    <div class="flex h-screen bg-background-light dark:bg-background-dark overflow-hidden">
      <!-- 新侧边导航 -->
      <ImSideNavNew
        :active-module="activeModule"
        :collapsed="isSidebarCollapsed"
        @switch-module="handleSwitchModule"
        @toggle-collapse="isSidebarCollapsed = !isSidebarCollapsed"
      />

      <!-- 主内容区 -->
      <main class="flex-1 min-w-0 overflow-hidden flex">
        <SessionPanel 
          v-if="activeModule === 'chat'" 
          :current-session="currentSession"
          @select-session="handleSelectSession" 
          @show-user="handleShowUser"
        />
        <WorkbenchPanel v-if="activeModule === 'workbench'" />
        <ContactsPanel v-if="activeModule === 'contacts'" />
        <DocumentsPanel v-if="activeModule === 'drive'" />
        <CalendarPanel v-if="activeModule === 'calendar'" />
        <TodoPanel v-if="activeModule === 'todo'" />
        <ApprovalPanel v-if="activeModule === 'approval'" />
        <MailPanel v-if="activeModule === 'mail'" />
        <AssistantPanel v-if="activeModule === 'assistant'" />
        
        <!-- 核心聊天面板 -->
        <ChatPanel 
          v-if="activeModule === 'chat'" 
          :session="currentSession" 
          @show-user="handleShowUser"
        />
      </main>

      <!-- 全局交互弹窗 (对齐钉钉模式) -->
      <PersonalProfileDialog v-model="showProfile" />
      <SystemSettingsDialog v-model="showSettings" />
      <HelpFeedbackDialog v-model="showHelp" />
      <UserDetailDrawer
        v-model="showUserDetail"
        :session="detailSession"
        @send-message="handleSendMessageFromDrawer"
        @voice-call="handleVoiceCallFromDrawer"
        @video-call="handleVideoCallFromDrawer"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { useTheme } from '@/composables/useTheme'
import { createConversation } from '@/api/im'
import { ElMessage } from 'element-plus'
import ImSideNavNew from '../components/ImSideNavNew/index.vue'
import SessionPanel from './SessionPanel.vue'
import WorkbenchPanel from './WorkbenchPanel.vue'
import ContactsPanel from './ContactsPanel.vue'
import DocumentsPanel from './DocumentsPanel.vue'
import CalendarPanel from './CalendarPanel.vue'
import TodoPanel from './TodoPanel.vue'
import ApprovalPanel from './ApprovalPanel.vue'
import MailPanel from './MailPanel.vue'
import AssistantPanel from './AssistantPanel.vue'
import ChatPanel from './ChatPanel.vue'
import UserDetailDrawer from '@/components/Chat/UserDetailDrawer.vue'

// 新增弹窗组件
import PersonalProfileDialog from '@/components/Common/PersonalProfileDialog.vue'
import SystemSettingsDialog from '@/components/Common/SystemSettingsDialog.vue'
import HelpFeedbackDialog from '@/components/Common/HelpFeedbackDialog.vue'

const store = useStore()
const router = useRouter()
const activeModule = ref('chat')
const isSidebarCollapsed = ref(false)
const currentSession = computed(() => store.state.im.session?.currentSession || null)
const { isDark } = useTheme()

// 弹窗状态控制
const showProfile = ref(false)
const showSettings = ref(false)
const showHelp = ref(false)
const showUserDetail = ref(false)
const detailSession = ref(null)

const { connect, onMessage, isConnected } = useImWebSocket()

const handleSwitchModule = (module) => {
  if (module === 'profile') {
    showProfile.value = true
  } else if (module === 'settings') {
    showSettings.value = true
  } else if (module === 'help') {
    showHelp.value = true
  } else {
    activeModule.value = module
  }
}

const handleSelectSession = (session) => {
  if (!session?.id) return
  store.dispatch('im/session/selectSession', session)
}

const handleShowUser = (userId) => {
  if (!userId) return
  // 构造简易 session 对象供 UserDetailDrawer 使用
  detailSession.value = {
    targetUserId: userId,
    type: 'PRIVATE'
  }
  showUserDetail.value = true
}

const ensurePrivateSession = async (userId) => {
  const sessions = store.state.im.session?.sessions || []
  const existed = sessions.find(
    s => s.type === 'PRIVATE' && `${s.targetId}` === `${userId}`
  )
  if (existed) return existed

  const res = await createConversation({
    type: 'PRIVATE',
    targetId: userId
  })
  if (res.code !== 200 || !res.data) {
    throw new Error(res.msg || '创建会话失败')
  }

  await store.dispatch('im/session/loadSessions')
  const latestSessions = store.state.im.session?.sessions || []
  return latestSessions.find(s => `${s.id}` === `${res.data.id}`) || res.data
}

const handleSendMessageFromDrawer = async (sessionLike) => {
  const userId = sessionLike?.targetUserId || sessionLike?.targetId || sessionLike?.userId
  if (!userId) return

  try {
    const targetSession = await ensurePrivateSession(userId)
    handleSelectSession(targetSession)
    activeModule.value = 'chat'
  } catch (error) {
    ElMessage.error('发起会话失败，请稍后重试')
  }
}

const triggerCallFromDrawer = async (sessionLike, callType) => {
  const userId = sessionLike?.targetUserId || sessionLike?.targetId || sessionLike?.userId
  if (!userId) return

  try {
    const targetSession = await ensurePrivateSession(userId)
    handleSelectSession(targetSession)
    activeModule.value = 'chat'
    window.dispatchEvent(new CustomEvent('im-start-call', {
      detail: { sessionId: targetSession.id, callType }
    }))
  } catch (error) {
    ElMessage.error('通话发起失败，请稍后重试')
  }
}

const handleVoiceCallFromDrawer = async (sessionLike) => {
  await triggerCallFromDrawer(sessionLike, 'voice')
}

const handleVideoCallFromDrawer = async (sessionLike) => {
  await triggerCallFromDrawer(sessionLike, 'video')
}

const handleSwitchToChatEvent = async (event) => {
  const session = event?.detail?.conversation
  if (!session?.id) return

  activeModule.value = 'chat'
  await store.dispatch('im/session/loadSessions')
  const sessions = store.state.im.session?.sessions || []
  const target = sessions.find(s => `${s.id}` === `${session.id}`) || session
  handleSelectSession(target)
}

// Watch session change to auto-switch to chat
watch(currentSession, (sess) => {
  if (sess) {
    activeModule.value = 'chat'
  }
})

// Global WebSocket Message Handler
onMessage((msg) => {
  store.dispatch('im/message/receiveMessage', msg)
})

const { onOnline, onOffline } = useImWebSocket()

onOnline((data) => {
  if (data.userId) {
    store.commit('im/contact/SET_USER_STATUS', { userId: data.userId, status: 'online' })
  }
})

onOffline((data) => {
  if (data.userId) {
    store.commit('im/contact/SET_USER_STATUS', { userId: data.userId, status: 'offline' })
  }
})

onMounted(async () => {
  window.addEventListener('switch-to-chat', handleSwitchToChatEvent)

  try {
    const userInfo = await store.dispatch('user/getUserInfo')
    if (!userInfo?.id) {
      throw new Error('登录状态无效')
    }
  } catch (error) {
    ElMessage.warning('登录已失效，请重新登录')
    localStorage.removeItem('im_token')
    localStorage.removeItem('im_user_info')
    localStorage.removeItem('im_user_role')
    router.replace('/login')
    return
  }

  try {
    await store.dispatch('im/session/loadSessions')
    const sessions = store.state.im.session?.sessions || []
    if (!store.state.im.session?.currentSession && sessions.length > 0) {
      handleSelectSession(sessions[0])
    }
  } catch (error) {
    ElMessage.error('加载会话失败，请刷新重试')
    console.warn('加载会话列表失败', error)
  }

  if (!isConnected.value) {
    const token = localStorage.getItem('im_token')
    if (token) {
        connect(token)
    }
  }
})

onUnmounted(() => {
  window.removeEventListener('switch-to-chat', handleSwitchToChatEvent)
})
</script>

<style lang="scss" scoped>
.dingtalk-app {
  width: 100%;
  height: 100%;
}
</style>
