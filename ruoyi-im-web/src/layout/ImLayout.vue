<template>
  <div class="web-im-layout">
    <!-- 顶部导航栏 -->
    <ImHeader 
      :user-info="userInfo"
      :notification-count="notificationCount"
      @handle-user-command="handleUserCommand"
      @show-notifications="showNotifications"
      @show-start-chat-dialog="showStartChatDialog"
      @toggle-theme="toggleTheme"
    />

    <!-- 主工作区 -->
    <main class="workspace">
      <!-- 侧边导航栏 -->
      <SideNav 
        :collapsed="isNavCollapsed"
        :active-module="activeModule"
        :nav-width="navWidth"
        @update:collapsed="updateNavCollapsed"
        @update:nav-width="updateNavWidth"
        @switch-module="switchModule"
      />

      <!-- 动态工作区内容 -->
      <component 
        :is="currentWorkspaceComponent" 
        v-bind="workspaceProps"
      />
    </main>

    <!-- 全局对话框 -->
    <GlobalSearch v-if="globalSearchVisible" />
    <NotificationPanel v-if="notificationVisible" />
    <SystemSettings v-if="systemSettingsVisible" />
    
    <!-- 视频通话管理器 -->
    <CallManager ref="callManagerRef" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import ImHeader from './ImHeader.vue'
import SideNav from './SideNav.vue'
import ChatWorkspace from './workspaces/ChatWorkspace.vue'
import ContactsWorkspace from './workspaces/ContactsWorkspace.vue'
import WorkbenchWorkspace from './workspaces/WorkbenchWorkspace.vue'
import DriveWorkspace from './workspaces/DriveWorkspace.vue'
import ModuleWorkspace from './workspaces/ModuleWorkspace.vue'
import GlobalSearch from '@/components/Search/GlobalSearch.vue'
import NotificationPanel from '@/components/Notification/NotificationPanel.vue'
import SystemSettings from '@/views/settings/index.vue'
import CallManager from '@/components/Chat/CallManager.vue'

const store = useStore()

// 布局状态
const isNavCollapsed = ref(false)
const navWidth = ref(parseInt(localStorage.getItem('navWidth')) || 60)
const activeModule = ref('chat')

// 对话框状态
const globalSearchVisible = ref(false)
const notificationVisible = ref(false)
const systemSettingsVisible = ref(false)

// 用户信息
const userInfo = computed(() => store.state.user)
const notificationCount = computed(() => store.state.im.unreadCount)

// 组件引用
const callManagerRef = ref(null)

// 工作区组件映射
const workspaceComponents = {
  'chat': ChatWorkspace,
  'contacts': ContactsWorkspace,
  'workbench': WorkbenchWorkspace,
  'drive': DriveWorkspace,
  'approval': ModuleWorkspace,
  'ding': ModuleWorkspace,
  'email': ModuleWorkspace,
  'app-center': ModuleWorkspace,
}

// 当前工作区组件
const currentWorkspaceComponent = computed(() => {
  return workspaceComponents[activeModule.value] || ChatWorkspace
})

// 工作区属性
const workspaceProps = computed(() => {
  return {
    module: activeModule.value,
    collapsed: isNavCollapsed.value,
    navWidth: navWidth.value
  }
})

// 导航栏折叠状态
const updateNavCollapsed = (collapsed) => {
  isNavCollapsed.value = collapsed
  localStorage.setItem('navCollapsed', collapsed)
}

// 导航栏宽度
const updateNavWidth = (width) => {
  navWidth.value = width
  localStorage.setItem('navWidth', width)
}

// 切换模块
const switchModule = (module) => {
  activeModule.value = module
}

// 用户命令处理
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      // 打开用户资料
      break
    case 'status':
      // 切换在线状态
      break
    case 'settings':
      systemSettingsVisible.value = true
      break
    case 'theme':
      // 打开主题设置
      break
    case 'logout':
      // 退出登录
      break
    case 'feedback':
      // 打开反馈对话框
      break
    case 'update':
      // 检查更新
      break
  }
}

// 显示通知
const showNotifications = () => {
  notificationVisible.value = true
}

// 显示发起聊天对话框
const showStartChatDialog = () => {
  // 打开发起聊天对话框
}

// 切换主题
const toggleTheme = () => {
  // 切换主题逻辑
}
</script>

<style scoped lang="scss">
.web-im-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.workspace {
  display: flex;
  flex: 1;
  overflow: hidden;
  position: relative;
}
</style>
