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
        <SessionPanel v-if="activeModule === 'chat'" @select-session="handleSelectSession" />
        <WorkbenchPanel v-if="activeModule === 'workbench'" />
        <ContactsPanel v-if="activeModule === 'contacts'" />
        <DocumentsPanel v-if="activeModule === 'drive'" />
        <CalendarPanel v-if="activeModule === 'calendar'" />
        <TodoPanel v-if="activeModule === 'todo'" />
        <ApprovalPanel v-if="activeModule === 'approval'" />
        <MailPanel v-if="activeModule === 'mail'" />
        <AssistantPanel v-if="activeModule === 'assistant'" />
        
        <!-- 核心聊天面板 -->
        <ChatPanel v-if="activeModule === 'chat' && currentSession" :session="currentSession" />
      </main>

      <!-- 全局交互弹窗 (对齐钉钉模式) -->
      <PersonalProfileDialog v-model="showProfile" />
      <SystemSettingsDialog v-model="showSettings" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { useTheme } from '@/composables/useTheme'
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

// 新增弹窗组件
import PersonalProfileDialog from '@/components/Common/PersonalProfileDialog.vue'
import SystemSettingsDialog from '@/components/Common/SystemSettingsDialog.vue'

const store = useStore()
const activeModule = ref('chat')
const isSidebarCollapsed = ref(false)
const currentSession = computed(() => store.state.im.currentSession)
const { isDark } = useTheme()

// 弹窗状态控制
const showProfile = ref(false)
const showSettings = ref(false)

const { connect, onMessage, isConnected } = useImWebSocket()

const handleSwitchModule = (module) => {
  if (module === 'profile') {
    showProfile.value = true
  } else if (module === 'settings') {
    showSettings.value = true
  } else {
    activeModule.value = module
  }
}

const handleSelectSession = (session) => {
  store.dispatch('im/selectSession', session)
}

// Watch session change to auto-switch to chat
watch(currentSession, (sess) => {
  if (sess) {
    activeModule.value = 'chat'
  }
})

// Global WebSocket Message Handler
onMessage((msg) => {
  store.dispatch('im/receiveMessage', msg)
})

const { onOnline, onOffline } = useImWebSocket()

onOnline((data) => {
  if (data.userId) {
    store.commit('im/SET_USER_STATUS', { userId: data.userId, status: 'online' })
  }
})

onOffline((data) => {
  if (data.userId) {
    store.commit('im/SET_USER_STATUS', { userId: data.userId, status: 'offline' })
  }
})

onMounted(async () => {
  try {
    await store.dispatch('user/getUserInfo')
  } catch (error) {
    console.warn('获取用户信息失败', error)
  }

  try {
    await store.dispatch('im/loadSessions')
  } catch (error) {
    console.warn('加载会话列表失败', error)
  }

  if (!isConnected.value) {
    const token = localStorage.getItem('im_token')
    if (token) {
        connect(token)
    }
  }
})
</script>

<style lang="scss" scoped>
.dingtalk-app {
  width: 100%;
  height: 100%;
}
</style>
