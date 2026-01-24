<template>
  <div :class="['dingtalk-app', isDark ? 'dark' : '']">
    <div class="flex h-screen bg-background-light dark:bg-background-dark">
      <!-- 新侧边导航 -->
      <ImSideNavNew 
        :active-module="activeModule" 
        :collapsed="isSidebarCollapsed"
        @switch-module="handleSwitchModule" 
        @toggle-collapse="isSidebarCollapsed = !isSidebarCollapsed"
      />

      <!-- 主内容区 -->
      <main class="flex-1 overflow-hidden flex">
        <SessionPanel v-if="activeModule === 'chat'" @select-session="handleSelectSession" />
        <WorkbenchPanel v-if="activeModule === 'workbench'" />
        <ContactsPanel v-if="activeModule === 'contacts'" />
        <DocumentsPanel v-if="activeModule === 'drive'" />
        <CalendarPanel v-if="activeModule === 'calendar'" />
        <SettingsPanel v-if="activeModule === 'settings'" />
        <UserProfilePanel v-if="activeModule === 'profile'" />
        <ChatPanel v-if="currentSession" :session="currentSession" />
      </main>
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
import SettingsPanel from './SettingsPanel.vue'
import UserProfilePanel from './UserProfilePanel.vue'
import ChatPanel from './ChatPanel.vue'

const store = useStore()
const activeModule = ref('chat')
const isSidebarCollapsed = ref(false)
const currentSession = computed(() => store.state.im.currentSession)
const { isDark } = useTheme()

const { connect, onMessage, isConnected } = useImWebSocket()

const handleSwitchModule = (module) => {
  activeModule.value = module
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
