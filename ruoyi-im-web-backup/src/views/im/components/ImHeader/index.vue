<template>
  <header class="ding-header">
    <!-- 左侧：Logo和搜索（居中） -->
    <div class="header-left">
      <div class="header-logo">
        <el-icon :size="24" color="#0089FF">
          <ChatDotRound />
        </el-icon>
      </div>
      <div class="header-divider"></div>
      <div class="header-search">
        <el-input
          v-model="globalSearchKeyword"
          placeholder="搜索联系人、群聊、消息..."
          :prefix-icon="Search"
          class="global-search-input"
          clearable
          readonly
          @click="showGlobalSearch"
          @keyup.enter="showGlobalSearch"
        >
          <template #suffix>
            <span class="search-shortcut">⌘K</span>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 右侧：操作区 -->
    <div class="header-right">
      <!-- 通知按钮 -->
      <el-badge
        :value="notificationCount"
        :hidden="notificationCount === 0"
        :max="99"
        class="header-badge"
      >
        <el-tooltip content="通知" placement="bottom">
          <el-button :icon="Bell" text class="header-action-btn" @click="showNotifications" />
        </el-tooltip>
      </el-badge>

      <!-- 快捷操作 -->
      <el-tooltip content="发起聊天" placement="bottom">
        <el-button :icon="Plus" text class="header-action-btn" @click="showStartChatDialog" />
      </el-tooltip>

      <!-- 设置 -->
      <el-dropdown trigger="click" placement="bottom-end" @command="handleSettingsCommand">
        <div class="header-action-wrapper">
          <el-tooltip content="设置" placement="bottom">
            <el-button :icon="Setting" text class="header-action-btn" />
          </el-tooltip>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="settings-dropdown">
            <el-dropdown-item command="theme">
              <el-icon class="menu-icon theme">
                <Sunny />
              </el-icon>
              <span class="menu-text">主题切换</span>
              <el-switch
                v-model="isDark"
                inactive-color="#8c8c8c"
                active-color="#1677ff"
                @change="toggleTheme"
                style="margin-left: auto"
              />
            </el-dropdown-item>
            <el-dropdown-item command="notifications">
              <el-icon class="menu-icon">
                <Bell />
              </el-icon>
              <span class="menu-text">消息通知</span>
            </el-dropdown-item>
            <el-dropdown-item command="privacy">
              <el-icon class="menu-icon">
                <Lock />
              </el-icon>
              <span class="menu-text">隐私与安全</span>
            </el-dropdown-item>
            <el-dropdown-item divided command="shortcuts">
              <el-icon class="menu-icon">
                <Key />
              </el-icon>
              <span class="menu-text">快捷键</span>
            </el-dropdown-item>
            <el-dropdown-item command="about">
              <el-icon class="menu-icon">
                <InfoFilled />
              </el-icon>
              <span class="menu-text">关于系统</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 帮助 -->
      <el-dropdown trigger="click" placement="bottom-end" @command="handleHelpCommand">
        <div class="header-action-wrapper">
          <el-tooltip content="帮助" placement="bottom">
            <el-button :icon="QuestionFilled" text class="header-action-btn" />
          </el-tooltip>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="help-dropdown">
            <el-dropdown-item command="help">
              <el-icon class="menu-icon">
                <QuestionFilled />
              </el-icon>
              <span class="menu-text">使用帮助</span>
            </el-dropdown-item>
            <el-dropdown-item command="shortcuts">
              <el-icon class="menu-icon">
                <Key />
              </el-icon>
              <span class="menu-text">快捷键指南</span>
            </el-dropdown-item>
            <el-dropdown-item command="feedback">
              <el-icon class="menu-icon">
                <Message />
              </el-icon>
              <span class="menu-text">反馈建议</span>
            </el-dropdown-item>
            <el-dropdown-item divided command="update">
              <el-icon class="menu-icon">
                <Refresh />
              </el-icon>
              <span class="menu-text">检查更新</span>
              <el-tag size="small" type="success" class="version-tag">v1.0</el-tag>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 用户下拉 -->
      <el-dropdown trigger="click" placement="bottom-end" @command="handleUserCommand">
        <div class="header-user">
          <SmartAvatar
            :name="currentUser?.name"
            :avatar="currentUser?.avatar"
            :size="32"
            :show-status="true"
            :status="currentOnlineStatus === 'online' ? 'online' : 'offline'"
            class="user-avatar"
          />
          <span class="header-username">{{ currentUser?.name || '用户' }}</span>
          <el-icon class="dropdown-arrow">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <div class="profile-item">
                <el-avatar :size="36" :src="currentUser?.avatar" class="dropdown-avatar">
                  {{ currentUser?.name?.charAt(0) || 'U' }}
                </el-avatar>
                <div class="user-info-item">
                  <span class="user-name">{{ currentUser?.name || '用户' }}</span>
                  <span class="user-id">ID: {{ currentUser?.userId }}</span>
                </div>
              </div>
            </el-dropdown-item>
            <el-dropdown-item divided command="status">
              <div class="status-item-wrapper">
                <div class="status-item-content">
                  <el-icon>
                    <CircleCheck />
                  </el-icon>
                  <span>在线状态</span>
                </div>
                <div class="status-selector">
                  <el-tooltip content="在线" placement="top">
                    <span
                      class="status-btn"
                      :class="{ active: currentOnlineStatus === 'online' }"
                      @click.stop="setOnlineStatus('online')"
                    >
                      <span class="status-dot online"></span>
                    </span>
                  </el-tooltip>
                  <el-tooltip content="离开" placement="top">
                    <span
                      class="status-btn"
                      :class="{ active: currentOnlineStatus === 'away' }"
                      @click.stop="setOnlineStatus('away')"
                    >
                      <span class="status-dot away"></span>
                    </span>
                  </el-tooltip>
                  <el-tooltip content="忙碌" placement="top">
                    <span
                      class="status-btn"
                      :class="{ active: currentOnlineStatus === 'busy' }"
                      @click.stop="setOnlineStatus('busy')"
                    >
                      <span class="status-dot busy"></span>
                    </span>
                  </el-tooltip>
                  <el-tooltip content="离线" placement="top">
                    <span
                      class="status-btn"
                      :class="{ active: currentOnlineStatus === 'offline' }"
                      @click.stop="setOnlineStatus('offline')"
                    >
                      <span class="status-dot offline"></span>
                    </span>
                  </el-tooltip>
                </div>
                <div class="current-status-text">
                  {{ getOnlineStatusDisplay(currentOnlineStatus) }}
                </div>
              </div>
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <el-icon>
                <Setting />
              </el-icon>
              个人设置
            </el-dropdown-item>
            <el-dropdown-item command="theme">
              <el-icon>
                <Moon />
              </el-icon>
              主题设置
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon>
                <SwitchButton />
              </el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import {
  Search,
  Bell,
  Plus,
  Setting,
  QuestionFilled,
  ArrowDown,
  Sunny,
  Moon,
  Lock,
  Key,
  InfoFilled,
  Message,
  Refresh,
  CircleCheck,
  SwitchButton,
  Star,
  ChatDotRound
} from '@element-plus/icons-vue'
import SmartAvatar from '@/components/SmartAvatar/index.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { toggleTheme as toggleAppTheme, initTheme as initAppTheme } from '@/utils/theme.js'

const store = useStore()
const router = useRouter()

// 响应式状态
const globalSearchKeyword = ref('')
const currentOnlineStatus = ref('online')
const isDark = ref(false)

// 初始化主题
onMounted(() => {
  initAppTheme()
  isDark.value = document.documentElement.getAttribute('data-theme') === 'dark'

  // 监听主题变化事件
  window.addEventListener('themeChange', handleThemeChange)
})

onUnmounted(() => {
  window.removeEventListener('themeChange', handleThemeChange)
})

// 处理主题变化事件（来自 ThemeSwitch 组件）
const handleThemeChange = (event) => {
  isDark.value = event.detail.theme === 'dark'
}

// 切换主题
const toggleTheme = () => {
  toggleAppTheme()
  isDark.value = !isDark.value
}

// 计算属性
const currentUser = computed(() => store.state.user.userInfo)
const notificationCount = computed(() => store.state.im.unreadCount || 0)

// 方法
const showGlobalSearch = () => {
  // TODO: 实现全局搜索对话框
  ElMessage.info('全局搜索功能开发中...')
}

const showNotifications = () => {
  ElMessage.info('通知列表功能开发中...')
}

const showStartChatDialog = () => {
  ElMessage.info('发起聊天功能开发中...')
}

const handleSettingsCommand = (command) => {
  switch (command) {
    case 'theme':
      // 主题切换已通过 el-switch 处理
      break
    case 'notifications':
      ElMessage.info('通知设置功能开发中...')
      break
    case 'privacy':
      ElMessage.info('隐私设置功能开发中...')
      break
    case 'shortcuts':
      ElMessage.info('快捷键功能开发中...')
      break
    case 'about':
      ElMessage.info('关于系统功能开发中...')
      break
  }
}

const handleHelpCommand = (command) => {
  switch (command) {
    case 'help':
      ElMessage.info('使用帮助功能开发中...')
      break
    case 'shortcuts':
      ElMessage.info('快捷键指南功能开发中...')
      break
    case 'feedback':
      ElMessage.info('反馈建议功能开发中...')
      break
    case 'update':
      ElMessage.info('检查更新功能开发中...')
      break
  }
}

const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人资料功能开发中...')
      break
    case 'status':
      // 状态切换逻辑在点击处理中
      break
    case 'settings':
      ElMessage.info('个人设置功能开发中...')
      break
    case 'theme':
      toggleTheme()
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        router.push('/login')
      }).catch(() => {})
      break
  }
}

const setOnlineStatus = (status) => {
  currentOnlineStatus.value = status
  ElMessage.success(`状态已切换为：${getOnlineStatusDisplay(status)}`)
  // TODO: 保存到后端
}

const getOnlineStatusDisplay = (status) => {
  const statusMap = {
    online: '在线',
    away: '离开',
    busy: '忙碌',
    offline: '离线'
  }
  return statusMap[status] || '在线'
}
</script>

<style scoped>
.ding-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 24px;
  background-color: #ffffff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.header-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
}

.header-divider {
  width: 1px;
  height: 24px;
  margin: 0 20px;
  background-color: #f0f0f0;
}

.header-search {
  width: 320px;
  max-width: 600px;
}

.global-search-input {
  :deep(.el-input__wrapper) {
    border-radius: 8px;
    background-color: #f5f5f5;
    box-shadow: none;
    border: 1px solid transparent;

    &:hover {
      background-color: #f0f0f0;
      border-color: #d9d9d9;
    }

    &.is-focus {
      background-color: #ffffff;
      border-color: #1677ff;
      box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
    }
  }

  :deep(.el-input__inner) {
    font-size: 14px;
    color: #262626;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-badge {
  margin-right: 4px;
}

.header-action-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  transition: all 0.2s ease;

  &:hover {
    background-color: rgba(0, 0, 0, 0.04);
  }

  :deep(.el-icon) {
    font-size: 18px;
    color: #8c8c8c;
  }
}

.header-action-wrapper {
  display: flex;
  align-items: center;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: rgba(0, 0, 0, 0.04);
  }
}

.header-username {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.dropdown-arrow {
  font-size: 12px;
  color: #8c8c8c;
}

.settings-dropdown,
.help-dropdown {
  min-width: 200px;

  .menu-icon {
    margin-right: 8px;
    font-size: 16px;
    color: #8c8c8c;
  }

  .menu-icon.theme {
    color: #1677ff;
  }

  .menu-text {
    flex: 1;
    font-size: 14px;
    color: #262626;
    font-weight: 400;
  }

  .item-shortcut {
    font-size: 12px;
    color: #8c8c8c;
    margin-left: 8px;
  }

  .version-tag {
    margin-left: auto;
  }
}

.profile-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.dropdown-avatar {
  flex-shrink: 0;
}

.user-info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.user-id {
  font-size: 12px;
  color: #8c8c8c;
}

.status-item-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px 0;
}

.status-item-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #262626;
}

.status-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.status-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background-color: rgba(0, 0, 0, 0.04);
  }

  &.active {
    background-color: rgba(22, 119, 255, 0.1);
  }
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);

  &.online {
    background-color: #52c41a;
  }

  &.away {
    background-color: #faad14;
  }

  &.busy {
    background-color: #ff4d4f;
  }

  &.offline {
    background-color: #8c8c8c;
  }
}

.current-status-text {
  font-size: 12px;
  color: #8c8c8c;
  text-align: center;
}
</style>
