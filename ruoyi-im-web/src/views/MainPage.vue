<template>
  <div :class="['im-app', isDark ? 'dark' : '']">
    <div class="app-container">
      <!-- 新侧边导航 -->
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
        :fullscreen="isMobileScreen"
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

// 异步加载非聊天模块（延迟加载，减少首屏包体积）
const WorkbenchPanel = defineAsyncComponent(() => import('./WorkbenchPanel.vue'))
const ContactsPanel = defineAsyncComponent(() => import('./ContactsPanel.vue'))
const DocumentsPanel = defineAsyncComponent(() => import('./DocumentsPanel.vue'))
const CalendarPanel = defineAsyncComponent(() => import('./CalendarPanel.vue'))
const TodoPanel = defineAsyncComponent(() => import('./TodoPanel.vue'))
const ApprovalPanel = defineAsyncComponent(() => import('./ApprovalPanel.vue'))
const MailPanel = defineAsyncComponent(() => import('./MailPanel.vue'))
const AppCenter = defineAsyncComponent(() => import('./AppCenter.vue'))
const AssistantPanel = defineAsyncComponent(() => import('./AssistantPanel.vue'))

// 异步加载弹窗组件（按需加载）
const UserProfileDialog = defineAsyncComponent(() => import('@/components/Contacts/UserProfileDialog.vue'))
const PersonalProfileDialog = defineAsyncComponent(() => import('@/components/Common/PersonalProfileDialog.vue'))
const SystemSettingsDialog = defineAsyncComponent(() => import('@/components/Common/SystemSettingsDialog.vue'))
const HelpFeedbackDialog = defineAsyncComponent(() => import('@/components/Common/HelpFeedbackDialog.vue'))
const GlobalSearchDialog = defineAsyncComponent(() => import('@/components/Common/GlobalSearchDialog.vue'))
const VoiceCallDialog = defineAsyncComponent(() => import('@/components/Chat/VoiceCallDialog.vue'))
const VideoCallDialog = defineAsyncComponent(() => import('@/components/Chat/VideoCallDialog.vue'))

const store = useStore()
const activeModule = ref('chat')
const currentSession = computed(() => store.state.im.session?.currentSession || null)
const { isDark } = useTheme()

// 弹窗状态控制
const showProfile = ref(false)
const showSettings = ref(false)
const showHelp = ref(false)
const showUserDetail = ref(false)
const showGlobalSearch = ref(false)
const detailSession = ref(null)
const settingsDefaultMenu = ref('account') // 设置对话框默认菜单
const showExternalApp = ref(false) // 外部应用对话框
const externalApp = ref(null) // 当前打开的外部应用

// 通话相关状态
const showVoiceCall = ref(false)
const showVideoCall = ref(false)
const isIncomingCall = ref(false)
const remoteCallUser = ref(null) // 远端通话用户

const { connect, onMessage, isConnected } = useImWebSocket()

// 窗口大小响应式处理
const windowWidth = ref(window.innerWidth)
const isSmallScreen = computed(() => windowWidth.value < 1024)
const isMobileScreen = computed(() => windowWidth.value < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
}

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

// 打开设置对话框，指定默认菜单页
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
  if (!userId) {return}
  // 构造简易 session 对象供 UserProfileDialog 使用
  detailSession.value = {
    targetUserId: userId,
    type: 'PRIVATE'
  }
  showUserDetail.value = true
}

// 处理搜索结果点击
const handleSearchSelect = ({ type, data }) => {
  if (!type || !data) return
  
  switch (type) {
    case 'contacts':
      // 跳转到通讯录并打开联系人详情
      activeModule.value = 'contacts'
      handleShowUser(data.userId || data.id)
      break
      
    case 'groups':
      // 跳转到聊天并打开群组会话
      activeModule.value = 'chat'
      handleSelectSession({
        id: data.groupId || data.id,
        type: 'GROUP',
        name: data.groupName || data.name
      })
      break
      
    case 'messages':
      // 跳转到聊天并定位消息
      activeModule.value = 'chat'
      if (data.conversationId) {
        store.dispatch('im/session/selectSessionById', data.conversationId)
          .then(() => {
            // 这里可以添加滚动到指定消息的逻辑
          })
      }
      break
      
    case 'files':
      // 跳转到云盘
      activeModule.value = 'drive'
      ElMessage.info('已跳转到云盘')
      break
      
    default:
      console.warn('未知的搜索结果类型:', type)
  }
}

// 处理打开外部应用
const handleOpenExternalApp = app => {
  externalApp.value = app
  showExternalApp.value = true
}

// 处理从通讯录发起的语音通话
const handleVoiceCallFromContact = contact => {
  remoteCallUser.value = contact
  isIncomingCall.value = false
  showVoiceCall.value = true
  ElMessage.info(`正在呼叫 ${contact.userName}...`)
}

// 处理从通讯录发起的视频通话
const handleVideoCallFromContact = contact => {
  remoteCallUser.value = contact
  isIncomingCall.value = false
  showVideoCall.value = true
  ElMessage.info(`正在呼叫 ${contact.userName}...`)
}

// Watch session change to auto-switch to chat
watch(currentSession, sess => {
  if (sess) {
    activeModule.value = 'chat'
  }
})

// Global WebSocket Message Handler
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

  // 添加键盘快捷键监听
  window.addEventListener('keydown', handleKeydown)

  // 添加切换到聊天模块的事件监听
  window.addEventListener('switch-to-chat', handleSwitchToChat)

  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 移除键盘快捷键监听
  window.removeEventListener('keydown', handleKeydown)

  // 移除切换到聊天模块的事件监听
  window.removeEventListener('switch-to-chat', handleSwitchToChat)

  // 移除窗口大小变化监听
  window.removeEventListener('resize', handleResize)
})

// 处理从通讯录发起聊天的切换
const handleSwitchToChat = event => {
  const { conversationId } = event.detail
  if (conversationId) {
    activeModule.value = 'chat'
  }
}

// 键盘快捷键处理
const handleKeydown = e => {
  // Ctrl/Cmd + K 打开全局搜索
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

// ============================================================================
// 应用容器 - 统一使用flex布局
// ============================================================================
.app-container {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: var(--dt-bg-body);
  position: relative;
}

// ============================================================================
// 主内容区布局 - 确保所有面板正确显示
// ============================================================================
.main-content-area {
  flex: 1;
  min-width: 0;
  height: 100%;
  overflow: hidden;
  position: relative;
  background: var(--dt-bg-body);
}

// ============================================================================
// 聊天模块特殊布局：SessionPanel + ChatPanel 并排
// ============================================================================
.chat-layout {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.chat-layout > :first-child {
  // SessionPanel - 使用设计令牌变量
  width: var(--dt-session-panel-width);
  flex-shrink: 0;
}

.chat-layout > :last-child {
  // ChatPanel
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

// ============================================================================
// 确保其他面板占满整个主内容区
// ============================================================================
.main-content-area > :not(.chat-layout) {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  overflow: hidden;
}

// ============================================================================
// 暗色模式适配
// ============================================================================
.dark .app-container {
  background: var(--dt-bg-body-dark);
}

.dark .main-content-area {
  background: var(--dt-bg-body-dark);
}

.dark .chat-placeholder {
  background: var(--dt-bg-card-dark);
}

// ============================================================================
// 外部应用对话框
// ============================================================================
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
