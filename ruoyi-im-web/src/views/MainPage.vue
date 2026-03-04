<template>
  <div :class="['dingtalk-app', isDark ? 'dark' : '']">
    <div class="layout-wrapper">
      <!-- 1. 左侧一级导航 (固定 68px) -->
      <ImSideNavNew
        :active-module="activeModule"
        @switch-module="handleSwitchModule"
      />

      <!-- 2. 中间二级列表与右侧主区 (Flex 1) -->
      <main class="content-container">
        <!-- 消息模块专用：双栏布局 -->
        <template v-if="activeModule === 'chat'">
          <SessionPanel
            :current-session="currentSession"
            @select-session="handleSelectSession"
            @show-user="handleShowUser"
          />
          <ChatPanel
            :session="currentSession"
            @show-user="handleShowUser"
          />
        </template>

        <!-- 其他模块：单面板布局 -->
        <template v-else>
          <div class="module-panel">
            <WorkbenchPanel v-if="activeModule === 'workbench'" />
            <ContactsPanel v-if="activeModule === 'contacts'" @show-user="handleShowUser" />
            <DocumentsPanel v-if="activeModule === 'drive'" />
            <CalendarPanel v-if="activeModule === 'calendar'" />
            <TodoPanel v-if="activeModule === 'todo'" />
            <ApprovalPanel v-if="activeModule === 'approval'" />
            <MailPanel v-if="activeModule === 'mail'" />
            <AssistantPanel v-if="activeModule === 'assistant'" />
            <AdminLayout v-if="activeModule === 'admin'" />
            <SearchPanel v-if="activeModule === 'search'" @show-user="handleShowUser" @go-to-session="handleGoToSession" @go-to-group="handleGoToGroup" />
            <SettingsPanel v-if="activeModule === 'settings'" />
            <ProfilePanel v-if="activeModule === 'profile'" />
          </div>
        </template>
      </main>

      <!-- 全局交互层 -->
      <UserProfileDialog
        v-model="showUserDetail"
        :user-id="detailUserId"
        @start-call="handleStartCall"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { useImWebSocket } from '@/composables/useImWebSocket'
import ImSideNavNew from '@/components/ImSideNavNew/index.vue'
import SessionPanel from './SessionPanel.vue'
import ChatPanel from './ChatPanel.vue'
import WorkbenchPanel from './WorkbenchPanel.vue'
import ContactsPanel from './ContactsPanel.vue'
import DocumentsPanel from './DocumentsPanel.vue'
import CalendarPanel from './CalendarPanel.vue'
import TodoPanel from './TodoPanel.vue'
import ApprovalPanel from './ApprovalPanel.vue'
import MailPanel from './MailPanel.vue'
import AssistantPanel from './AssistantPanel.vue'
import SearchPanel from './SearchPanel.vue'
import SettingsPanel from './SettingsPanel.vue'
import ProfilePanel from './ProfilePanel.vue'
import UserProfileDialog from '@/components/Contacts/UserProfileDialog.vue'
import AdminLayout from './admin/AdminLayout.vue'

const store = useStore()
const activeModule = ref('chat')
const showUserDetail = ref(false)
const detailUserId = ref(null)

const { connect, disconnect, onMessage, onRead, onOnline, onOffline } = useImWebSocket()

const currentSession = computed(() => store.state.im?.session?.currentSession || null)
// 从 localStorage 读取主题状态
const isDark = computed(() => localStorage.getItem('im_theme_dark') === 'true')

const handleSwitchModule = (m) => {
  activeModule.value = m
}
const handleSelectSession = (s) => { store.commit('im/session/SET_CURRENT_SESSION', s) }

// 跳转到会话
const handleGoToSession = ({ targetId, type }) => {
  activeModule.value = 'chat'
  // 查找或创建会话
  store.dispatch('im/session/findSession', { targetId, type }).then(session => {
    if (session) {
      handleSelectSession(session)
    }
  })
}

// 跳转到群组
const handleGoToGroup = (groupId) => {
  activeModule.value = 'chat'
  store.dispatch('im/session/findSession', { targetId: groupId, type: 'GROUP' }).then(session => {
    if (session) {
      handleSelectSession(session)
    }
  })
}

// 通用：显示用户详情逻辑
const handleShowUser = (userId) => {
  if (!userId) return
  detailUserId.value = userId
  showUserDetail.value = true
}

// 处理通话
const handleStartCall = (payload) => {
  ElMessage.info(`${payload.type === 'video' ? '视频' : '语音'}通话功能开发中`)
}

// 初始化 WebSocket 监听
const initWebSocket = () => {
  const token = store.state.user?.token
  if (!token) return

  connect(token)

  // 监听新消息
  onMessage((message) => {
    store.dispatch('im/message/receiveMessage', message)
  })

  // 监听已读状态（含多端同步）
  onRead((payload) => {
    // 如果是自己其他端发送的已读，同步更新本地未读数
    if (payload.userId === store.state.user?.currentUser?.id) {
      store.commit('im/session/UPDATE_SESSION', {
        id: payload.conversationId,
        unreadCount: 0
      })
    }
  })

  // 监听在线状态
  onOnline((payload) => {
    store.commit('im/contact/SET_USER_STATUS', { userId: payload.userId, status: 'online' })
  })

  onOffline((payload) => {
    store.commit('im/contact/SET_USER_STATUS', { userId: payload.userId, status: 'offline' })
  })
}

onMounted(() => {
  if (store.state.user?.token) {
    store.dispatch('im/session/loadSessions').catch(err => {
      console.warn('会话加载失败', err)
    })
    initWebSocket()
  }
})

onUnmounted(() => {
  disconnect()
})
</script>

<style lang="scss" scoped>
.dingtalk-app {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: var(--dt-bg-body);
  display: flex;
}

.layout-wrapper {
  display: flex;
  width: 100%;
  height: 100%;
}

.content-container {
  flex: 1;
  display: flex;
  height: 100%;
  min-width: 0; // 防崩红线
  background: #ffffff;
  position: relative;
  overflow: hidden;
}

.module-panel {
  flex: 1;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-card);
}

// 统一过度动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--dt-transition-base);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>