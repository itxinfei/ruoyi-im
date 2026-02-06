<template>
  <div :class="['im-app', isDark ? 'dark' : '']">
    <div class="app-container">
      <!-- 桌面端水平导航 -->
      <ImSideNavNew
        :active-module="activeModule"
        @switch-module="handleSwitchModule"
        @open-search="showGlobalSearch = true"
        @open-settings="handleOpenSettings"
      />

      <!-- 主内容区 -->
      <main class="main-content-area">
        <!-- 聊天模块：左侧会话列表 + 右侧聊天面板 -->
        <template v-if="activeModule === 'chat'">
          <div class="chat-layout">
            <SessionPanel
              :current-session="currentSession"
              @select-session="handleSelectSession"
              @show-user="handleShowUser"
            />
            <ChatPanel
              v-if="currentSession"
              :session="currentSession"
              @show-user="handleShowUser"
            />
            <div
              v-else
              class="chat-placeholder"
            >
              <EmptyState
                type="chat"
                title="选择一个会话开始聊天"
                description="从左侧列表选择联系人或群组，开始你的对话"
              />
            </div>
          </div>
        </template>

        <!-- 工作台 -->
        <WorkbenchPanel v-if="activeModule === 'workbench'" />
        <!-- 通讯录 -->
        <ContactsPanel
          v-if="activeModule === 'contacts'"
          @switch-module="activeModule = $event"
          @voice-call="handleVoiceCallFromContact"
          @video-call="handleVideoCallFromContact"
        />
        <!-- 云盘 -->
        <DocumentsPanel v-if="activeModule === 'drive'" />
        <!-- 日历 -->
        <CalendarPanel v-if="activeModule === 'calendar'" />
        <!-- 待办 -->
        <TodoPanel v-if="activeModule === 'todo'" />
        <!-- 审批 -->
        <ApprovalPanel v-if="activeModule === 'approval'" />
        <!-- 邮箱 -->
        <MailPanel v-if="activeModule === 'mail'" />
        <!-- 应用中心 -->
        <AppCenter
          v-if="activeModule === 'appcenter'"
          @switch-module="activeModule = $event"
          @open-external-app="handleOpenExternalApp"
        />
        <!-- AI助理 -->
        <AssistantPanel v-if="activeModule === 'assistant'" />
      </main>

      <!-- 全局交互弹窗 (对齐钉钉模式) -->
      <PersonalProfileDialog v-model="showProfile" />
      <SystemSettingsDialog
        v-model="showSettings"
        :default-menu="settingsDefaultMenu"
      />
      <HelpFeedbackDialog v-model="showHelp" />
      <UserProfileDialog
        v-model="showUserDetail"
        :session="detailSession"
        layout-mode="compact"
        @send-message="handleSelectSession"
      />
      <GlobalSearchDialog
        v-model="showGlobalSearch"
        @select="handleSearchSelect"
      />

      <!-- 外部应用对话框 -->
      <el-dialog
        v-model="showExternalApp"
        :title="externalApp?.name || '应用'"
        width="90%"
        :fullscreen="false"
        :close-on-click-modal="false"
        class="external-app-dialog"
        @closed="externalApp = null"
      >
        <div
          v-if="externalApp"
          class="external-app-container"
        >
          <iframe
            v-if="externalApp.openMode === 'iframe' && externalApp.appUrl"
            :src="externalApp.appUrl"
            class="external-app-iframe"
            frameborder="0"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowfullscreen
          />
          <div
            v-else
            class="app-placeholder"
          >
            <span class="material-icons-outlined">extension</span>
            <p>该应用暂不支持在此打开</p>
          </div>
        </div>
      </el-dialog>

      <!-- 语音通话对话框 -->
      <VoiceCallDialog
        v-model:visible="showVoiceCall"
        :remote-user="remoteCallUser"
        :is-incoming="isIncomingCall"
      />

      <!-- 视频通话对话框 -->
      <VideoCallDialog
        v-model:visible="showVideoCall"
        :remote-user="remoteCallUser"
        :is-incoming="isIncomingCall"
      />
    </div>
  </div>
</template>

<script setup>
import { getToken } from '@/utils/storage'

import { ref, watch, onMounted, onUnmounted, computed, defineAsyncComponent } from 'vue'
import { useStore } from 'vuex'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { useTheme } from '@/composables/useTheme'
import { ElMessage } from 'element-plus'
import ImSideNavNew from '../components/ImSideNavNew/index.vue'
import SessionPanel from './SessionPanel.vue'
import ChatPanel from './ChatPanel.vue'
import EmptyState from '@/components/Common/EmptyState.vue'

const WorkbenchPanel = defineAsyncComponent(() => import('./WorkbenchPanel.vue'))
const ContactsPanel = defineAsyncComponent(() => import('./ContactsPanel.vue'))
const DocumentsPanel = defineAsyncComponent(() => import('./DocumentsPanel.vue'))
const CalendarPanel = defineAsyncComponent(() => import('./CalendarPanel.vue'))
const TodoPanel = defineAsyncComponent(() => import('./TodoPanel.vue'))
const ApprovalPanel = defineAsyncComponent(() => import('./ApprovalPanel.vue'))
const MailPanel = defineAsyncComponent(() => import('./MailPanel.vue'))
const AppCenter = defineAsyncComponent(() => import('./AppCenter.vue'))
const AssistantPanel = defineAsyncComponent(() => import('./AssistantPanel.vue'))

const UserProfileDialog = defineAsyncComponent(() => import('@/components/Contacts/UserProfileDialog.vue'))
const PersonalProfileDialog = defineAsyncComponent(() => import('@/components/Common/PersonalProfileDialog.vue'))
const SystemSettingsDialog = defineAsyncComponent(() => import('@/components/Common/SystemSettingsDialog.vue'))
const HelpFeedbackDialog = defineAsyncComponent(() => import('@/components/Common/HelpFeedbackDialog.vue'))
const GlobalSearchDialog = defineAsyncComponent(() => import('@/components/Common/GlobalSearchDialog.vue'))
const VoiceCallDialog = defineAsyncComponent(() => import('@/components/Chat/VoiceCallDialog.vue'))
const VideoCallDialog = defineAsyncComponent(() => import('@/components/Chat/VideoCallDialog.vue'))

const store = useStore()
const activeModule = ref('chat')
const currentSession = computed(() => {
  try {
    return store.state.im?.session?.currentSession || null
  } catch (e) {
    console.warn('Error accessing currentSession:', e)
    return null
  }
})
const { isDark } = useTheme()

const showProfile = ref(false)
const showSettings = ref(false)
const showHelp = ref(false)
const showUserDetail = ref(false)
const showGlobalSearch = ref(false)
const detailSession = ref(null)
const settingsDefaultMenu = ref('account')
const showExternalApp = ref(false)
const externalApp = ref(null)

const showVoiceCall = ref(false)
const showVideoCall = ref(false)
const isIncomingCall = ref(false)
const remoteCallUser = ref(null)

const { connect, onMessage, isConnected } = useImWebSocket()

const handleSwitchModule = module => {
  if (module === 'profile') {
    showProfile.value = true
  } else if (module === 'settings') {
    settingsDefaultMenu.value = 'account'
    showSettings.value = true
  } else if (module === 'help') {
    showHelp.value = true
  } else {
    activeModule.value = module
  }
}

const handleOpenSettings = (menu = 'account') => {
  settingsDefaultMenu.value = menu
  showSettings.value = true
}

const handleSelectSession = session => {
  if (!session || !session.id) {
    console.error('[MainPage] Invalid session object:', session)
    return
  }
  store.dispatch('im/session/selectSession', session)
}

const handleShowUser = userId => {
  if (!userId) { return }
  detailSession.value = {
    targetUserId: userId,
    type: 'PRIVATE'
  }
  showUserDetail.value = true
}

const handleSearchSelect = ({ type, data }) => {
  if (!type || !data) {return}

  switch (type) {
    case 'contacts':
      activeModule.value = 'contacts'
      handleShowUser(data.userId || data.id)
      break

    case 'groups':
      activeModule.value = 'chat'
      handleSelectSession({
        id: data.groupId || data.id,
        type: 'GROUP',
        name: data.groupName || data.name
      })
      break

    case 'messages':
      activeModule.value = 'chat'
      if (data.conversationId) {
        store.dispatch('im/session/selectSessionById', data.conversationId)
          .then(() => {
          })
      }
      break

    case 'files':
      activeModule.value = 'drive'
      ElMessage.info('已跳转到云盘')
      break

    default:
      console.warn('未知的搜索结果类型:', type)
  }
}

const handleOpenExternalApp = app => {
  externalApp.value = app
  showExternalApp.value = true
}

const handleVoiceCallFromContact = contact => {
  remoteCallUser.value = contact
  isIncomingCall.value = false
  showVoiceCall.value = true
  ElMessage.info(`正在呼叫 ${contact.userName}...`)
}

const handleVideoCallFromContact = contact => {
  remoteCallUser.value = contact
  isIncomingCall.value = false
  showVideoCall.value = true
  ElMessage.info(`正在呼叫 ${contact.userName}...`)
}

watch(currentSession, sess => {
  if (sess) {
    activeModule.value = 'chat'
  }
})

onMessage(msg => {
  store.dispatch('im/message/receiveMessage', msg)
})

const { onOnline, onOffline } = useImWebSocket()

onOnline(data => {
  if (data.userId) {
    store.commit('im/contact/SET_USER_STATUS', { userId: data.userId, status: 'online' })
  }
})

onOffline(data => {
  if (data.userId) {
    store.commit('im/contact/SET_USER_STATUS', { userId: data.userId, status: 'offline' })
  }
})

onMounted(async () => {
  try {
    await store.dispatch('user/getUserInfo')
  } catch (error) {
    console.warn('获取用户信息失败', error)
  }

  try {
    await store.dispatch('im/session/loadSessions')
  } catch (error) {
    console.warn('加载会话列表失败', error)
  }

  if (!isConnected.value) {
    const token = getToken()
    if (token) {
      connect(token)
    }
  }

  window.addEventListener('keydown', handleKeydown)
  window.addEventListener('switch-to-chat', handleSwitchToChat)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('switch-to-chat', handleSwitchToChat)
})

const handleSwitchToChat = event => {
  const { conversationId } = event.detail
  if (conversationId) {
    activeModule.value = 'chat'
  }
}

const handleKeydown = e => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    showGlobalSearch.value = true
  }
}
</script>

<style lang="scss" scoped>
.im-app {
  width: 100%;
  height: 100%;
}

.app-container {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: var(--dt-bg-body);
  position: relative;
}

.main-content-area {
  flex: 1;
  min-width: 0;
  height: 100%;
  overflow: hidden;
  position: relative;
  background: var(--dt-bg-body);
}

.chat-layout {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.chat-layout > :first-child {
  width: var(--dt-session-panel-width);
  flex-shrink: 0;
}

.chat-layout > :last-child {
  flex: 1;
  min-width: 0;
  height: 100%;
}

.chat-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dt-bg-card);
}

.main-content-area > :not(.chat-layout) {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  overflow: hidden;
}

.dark .app-container {
  background: var(--dt-bg-body-dark);
}

.dark .main-content-area {
  background: var(--dt-bg-body-dark);
}

.dark .chat-placeholder {
  background: var(--dt-bg-card-dark);
}

.external-app-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.external-app-container {
  width: 100%;
  height: 70vh;
  display: flex;
  flex-direction: column;
}

.external-app-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.app-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 16px;
  color: var(--dt-text-tertiary);

  .material-icons-outlined {
    font-size: 64px;
  }

  p {
    font-size: 16px;
    margin: 0;
  }
}
</style>
