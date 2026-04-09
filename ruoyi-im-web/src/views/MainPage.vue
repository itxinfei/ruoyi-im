<template>
  <div class="dingtalk-app">
    <!-- 1. 左侧一级导航 (固定 68px) -->
    <ImSideNavNew
      :active-module="activeModule"
      @switch-module="processSwitchModule"
      @open-edit-profile="openEditProfile"
      @open-system-settings="openSystemSettings"
    />

    <!-- 2. 动态内容区 -->
    <main class="content-container">
      <!-- 消息模块专用布局 -->
      <template v-if="activeModule === 'chat'">
        <div class="chat-module-wrapper">
          <ChatSessionList />
          <div class="chat-main-view">
            <ChatWindow v-if="currentSession" />
            <div v-else class="empty-view">
              <div class="empty-icon">
                💬
              </div>
              <p>开启高效办公的一天</p>
            </div>
          </div>
        </div>
      </template>

      <!-- 搜索模块 -->
      <template v-else-if="activeModule === 'search'">
        <div class="single-panel-wrapper">
          <SearchPanel />
        </div>
      </template>

      <!-- 通讯录模块 -->
      <template v-else-if="activeModule === 'contacts'">
        <div class="single-panel-wrapper">
          <ContactsPanel />
        </div>
      </template>

      <!-- 工作台模块 -->
      <template v-else-if="activeModule === 'workbench'">
        <div class="single-panel-wrapper">
          <WorkbenchPanel @switch-module="processSwitchModule" />
        </div>
      </template>

      <!-- 云盘/文档模块 -->
      <template v-else-if="activeModule === 'documents'">
        <div class="single-panel-wrapper">
          <DocumentsPanel />
        </div>
      </template>

      <!-- 工作日志模块 -->
      <template v-else-if="activeModule === 'workreport'">
        <div class="single-panel-wrapper">
          <WorkReportPanel />
        </div>
      </template>

      <!-- 收藏模块 -->
      <template v-else-if="activeModule === 'favorites'">
        <div class="single-panel-wrapper">
          <FavoritesPanel @switch-module="processSwitchModule" />
        </div>
      </template>

      <!-- DING 模块 -->
      <template v-else-if="activeModule === 'ding'">
        <div class="single-panel-wrapper">
          <DingPanel />
        </div>
      </template>

      <!-- 日历模块 -->
      <template v-else-if="activeModule === 'calendar'">
        <div class="single-panel-wrapper">
          <CalendarPanel />
        </div>
      </template>

      <!-- 待办模块 -->
      <template v-else-if="activeModule === 'todo'">
        <div class="single-panel-wrapper">
          <TodoPanel />
        </div>
      </template>

      <!-- 审批模块 -->
      <template v-else-if="activeModule === 'approval'">
        <div class="single-panel-wrapper">
          <ApprovalPanel />
        </div>
      </template>

      <!-- 邮箱模块 -->
      <template v-else-if="activeModule === 'mail'">
        <div class="single-panel-wrapper">
          <MailPanel />
        </div>
      </template>

      <!-- AI助手模块 -->
      <template v-else-if="activeModule === 'assistant'">
        <div class="single-panel-wrapper">
          <AssistantPanel />
        </div>
      </template>

      <!-- 个人资料模块 -->
      <template v-else-if="activeModule === 'profile'">
        <div class="single-panel-wrapper">
          <ProfilePanel />
        </div>
      </template>

      <!-- 设置模块 -->
      <template v-else-if="activeModule === 'settings'">
        <div class="single-panel-wrapper">
          <SettingsPanel />
        </div>
      </template>

      <!-- 管理后台模块 -->
      <template v-else-if="activeModule === 'admin'">
        <div class="single-panel-wrapper">
          <AdminLayout />
        </div>
      </template>

      <!-- 兜底 -->
      <template v-else>
        <div class="placeholder-view">
          <div class="empty-icon">
            📦
          </div>
          <p>{{ getModuleName(activeModule) }} 模块正在接入中...</p>
        </div>
      </template>
    </main>

    <!-- 弹窗 -->
    <EditProfileDialog v-model="showEditProfileDialog" />
    <SystemSettingsDialog v-model="showSystemSettingsDialog" />
  </div>
</template>

<script setup lang="js">
/**
 * MainPage.vue (完整版 - 对齐需求文档)
 * 支持所有导航模块的面板渲染
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import ImSideNavNew from '@/components/ImSideNavNew/index.vue'
import ChatSessionList from '@/components/im/ChatSessionList.vue'
import ChatWindow from '@/components/im/ChatWindow.vue'
import SearchPanel from '@/views/SearchPanel.vue'
import ContactsPanel from '@/views/ContactsPanel.vue'
import WorkbenchPanel from '@/views/WorkbenchPanel.vue'
import DocumentsPanel from '@/views/DocumentsPanel.vue'
import CalendarPanel from '@/views/CalendarPanel.vue'
import TodoPanel from '@/views/TodoPanel.vue'
import ApprovalPanel from '@/views/ApprovalPanel.vue'
import MailPanel from '@/views/MailPanel.vue'
import AssistantPanel from '@/views/AssistantPanel.vue'
import ProfilePanel from '@/views/ProfilePanel.vue'
import SettingsPanel from '@/views/SettingsPanel.vue'
import EditProfileDialog from '@/components/Common/EditProfileDialog.vue'
import SystemSettingsDialog from '@/components/Common/SystemSettingsDialog.vue'
import AdminLayout from '@/views/admin/AdminLayout.vue'
import FavoritesPanel from '@/views/FavoritesPanel.vue'
import DingPanel from '@/views/DingPanel.vue'
import WorkReportPanel from '@/views/WorkReportPanel.vue'

const store = useStore()
const activeModule = ref('chat')
const showEditProfileDialog = ref(false)
const showSystemSettingsDialog = ref(false)

// 修正：嵌套访问路径 im -> session (增加可选链防护)
const currentSession = computed(() => store.state.im?.session?.currentSession)

const processSwitchModule = (moduleId) => {
  activeModule.value = moduleId
}

const openEditProfile = () => {
  showEditProfileDialog.value = true
}

const openSystemSettings = () => {
  showSystemSettingsDialog.value = true
}

const getModuleName = (id) => {
  const map = {
    'chat': '消息',
    'search': '搜索',
    'contacts': '通讯录',
    'workbench': '工作台',
    'documents': '云盘/文档',
    'calendar': '日历',
    'todo': '待办',
    'approval': '审批',
    'mail': '邮箱',
    'assistant': 'AI助手',
    'profile': '个人资料',
    'settings': '设置',
    'admin': '管理后台',
    'favorites': '收藏',
    'ding': 'DING'
  }
  return map[id] || id
}

// 全局快捷键事件处理
const handleGlobalSearch = () => {
  activeModule.value = 'search'
}

const handleNewChat = () => {
  activeModule.value = 'chat'
  // 触发创建新会话事件
  window.dispatchEvent(new CustomEvent('main:new-chat'))
}

const handleScreenshot = () => {
  // 触发截图事件（通知截图组件）
  window.dispatchEvent(new CustomEvent('main:screenshot'))
  ElMessage.info('截图功能已触发')
}

const handleScrollToTop = () => {
  // 触发滚动到顶部事件（通知聊天窗口）
  window.dispatchEvent(new CustomEvent('main:scroll-to-top'))
}

// 导航到用户详情
const handleNavigateContact = (e) => {
  const { id } = e.detail || {}
  if (id) {
    activeModule.value = 'contacts'
    // 派发事件让通讯录显示用户详情
    window.dispatchEvent(new CustomEvent('main:show-contact', { detail: { userId: id } }))
  }
}

// 导航到聊天会话
const handleNavigateChat = (e) => {
  const { type, id } = e.detail || {}
  if (id) {
    activeModule.value = 'chat'
    // 派发事件让会话列表选择对应会话
    window.dispatchEvent(new CustomEvent('main:select-session', { detail: { type, id } }))
  }
}

// 快捷键事件监听
onMounted(() => {
  window.addEventListener('shortcut:global-search', handleGlobalSearch)
  window.addEventListener('shortcut:new-chat', handleNewChat)
  window.addEventListener('shortcut:screenshot', handleScreenshot)
  window.addEventListener('shortcut:scroll-to-top', handleScrollToTop)
  window.addEventListener('main:navigate-contact', handleNavigateContact)
  window.addEventListener('main:navigate-chat', handleNavigateChat)
})

onUnmounted(() => {
  window.removeEventListener('shortcut:global-search', handleGlobalSearch)
  window.removeEventListener('shortcut:new-chat', handleNewChat)
  window.removeEventListener('shortcut:screenshot', handleScreenshot)
  window.removeEventListener('shortcut:scroll-to-top', handleScrollToTop)
  window.removeEventListener('main:navigate-contact', handleNavigateContact)
  window.removeEventListener('main:navigate-chat', handleNavigateChat)
})
</script>

<style scoped>
/* 钉钉 Windows 7.0+ 官方视觉契约 - 强对比/高清晰度版 */
.dingtalk-app {
  width: 100%; height: 100vh; overflow: hidden; display: flex;
}


.content-container { flex: 1; height: 100%; min-width: 0; overflow: hidden; }
.chat-module-wrapper { display: flex; width: 100%; height: 100%; }
.chat-main-view { flex: 1; height: 100%; min-width: var(--dt-chat-min-width); background-color: var(--dt-bg-chat); }
.empty-view, .placeholder-view { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: var(--dt-text-tertiary); }
.empty-icon { font-size: 64px; margin-bottom: 24px; opacity: 0.5; }  /* 空状态图标，保持非标准值 */
.single-panel-wrapper { width: 100%; height: 100%; }
</style>
