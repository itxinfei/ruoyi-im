<template>
  <nav
    :class="[
      'dingtalk-nav',
      'flex flex-col items-center z-20 shrink-0 transition-colors duration-300',
      isDark ? 'bg-nav-dark' : 'bg-nav-light'
    ]"
    style="height: 100vh;"
    role="navigation"
    aria-label="主导航"
  >
    <!-- Logo 区域 -->
    <div class="nav-logo-wrapper">
      <div class="nav-logo">
        <span class="nav-logo-text">IM</span>
        <span class="nav-logo-badge" v-if="unreadCount > 0">
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </div>
    </div>

    <!-- 导航项 -->
    <div class="nav-items" role="menubar">
      <el-tooltip
        v-for="item in navModules"
        :key="item.key"
        :content="item.label"
        placement="right"
        :show-after="500"
        :hide-after="0"
      >
        <button
          @click="handleSwitch(item.key)"
          :class="[
            'nav-item',
            activeModule === item.key ? 'nav-item-active' : ''
          ]"
          :aria-label="item.label"
          :aria-current="activeModule === item.key ? 'page' : undefined"
          role="menuitem"
        >
          <span class="nav-item-icon material-icons-outlined" aria-hidden="true">
            {{ item.icon }}
          </span>
          <span v-if="item.badge" class="nav-item-badge">{{ item.badge }}</span>
        </button>
      </el-tooltip>
    </div>

    <!-- 底部操作区 -->
    <div class="nav-footer">
      <!-- 搜索按钮 -->
      <el-tooltip content="全局搜索" placement="right" :show-after="500" :hide-after="0">
        <button
          @click="handleOpenSearch"
          class="nav-item nav-item-action"
          aria-label="全局搜索"
        >
          <span class="material-icons-outlined" aria-hidden="true">search</span>
        </button>
      </el-tooltip>

      <!-- 主题切换按钮 -->
      <el-tooltip :content="themeTooltip" placement="right" :show-after="500" :hide-after="0">
        <button
          @click="handleToggleTheme"
          class="nav-item nav-item-action"
          :aria-label="themeTooltip"
        >
          <span class="material-icons-outlined" aria-hidden="true">
            {{ themeIcon }}
          </span>
          <span v-if="themeMode === 'auto'" class="auto-badge">A</span>
        </button>
      </el-tooltip>

      <!-- 设置按钮 -->
      <el-tooltip content="设置" placement="right" :show-after="500" :hide-after="0">
        <button
          @click="handleSwitch('settings')"
          class="nav-item nav-item-action"
          :class="{ 'nav-item-active': activeModule === 'settings' }"
          aria-label="设置"
        >
          <span class="material-icons-outlined" aria-hidden="true">settings</span>
        </button>
      </el-tooltip>

      <!-- 用户头像 -->
      <el-tooltip
        :content="`个人资料: ${currentUser.nickname || currentUser.username || '我'}`"
        placement="right"
        :show-after="500"
        :hide-after="0"
      >
        <button
          @click="handleSwitch('profile')"
          class="nav-avatar"
          :class="{ 'nav-avatar-active': activeModule === 'profile' }"
          :aria-label="`个人资料: ${currentUser.nickname || currentUser.username || '我'}`"
        >
          <DingtalkAvatar
            :src="currentUser.avatar"
            :name="currentUser.nickname || currentUser.username || '我'"
            :user-id="currentUser.id"
            :size="48"
            shape="circle"
          />
          <!-- 在线状态点 -->
          <span class="nav-avatar-status" :class="{ 'online': isUserOnline }"></span>
        </button>
      </el-tooltip>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useTheme } from '@/composables/useTheme'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  activeModule: {
    type: String,
    default: 'chat'
  },
  collapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['switch-module', 'toggle-collapse', 'open-search', 'open-settings'])
const store = useStore()
const { isDark, themeMode, toggleTheme } = useTheme()

const themeIcon = computed(() => {
  if (themeMode.value === 'auto') return 'brightness_auto'
  return themeMode.value === 'dark' ? 'dark_mode' : 'light_mode'
})

const themeTooltip = computed(() => {
  const currentLabel = themeMode.value === 'auto' ? '跟随系统' : (themeMode.value === 'dark' ? '深色模式' : '浅色模式')
  return `外观模式: ${currentLabel} (点击切换)`
})

// 未读消息数
const unreadCount = computed(() => store.state.im?.totalUnreadCount || 0)

// 当前用户
const currentUser = computed(() => {
  const user = store.getters['user/currentUser']
  return user && Object.keys(user).length > 0 ? user : { nickname: '加载中...', username: '...', avatar: '' }
})

// 用户在线状态
const isUserOnline = computed(() => {
  if (!currentUser.value?.id) return false
  return store.state.im?.userStatus?.[currentUser.value.id] === 'online'
})

// 导航模块配置
const navModules = ref([
  { key: 'chat', label: '消息', icon: 'chat_bubble' },
  { key: 'contacts', label: '通讯录', icon: 'people' },
  { key: 'workbench', label: '工作台', icon: 'grid_view' },
  { key: 'drive', label: '云盘', icon: 'cloud' },
  { key: 'calendar', label: '日历', icon: 'calendar_today' },
  { key: 'todo', label: '待办', icon: 'check_circle' },
  { key: 'approval', label: '审批', icon: 'approval' },
  { key: 'mail', label: '邮箱', icon: 'email' },
  { key: 'assistant', label: 'AI助理', icon: 'smart_toy' }
])

/**
 * 处理主题切换并同步 Store
 */
function handleToggleTheme() {
  toggleTheme()
  const newSettings = { ...store.state.im.settings }
  if (!newSettings.general) newSettings.general = {}
  newSettings.general.theme = themeMode.value
  store.commit('im/UPDATE_SETTINGS', newSettings)
}

/**
 * 切换模块
 */
function handleSwitch(key) {
  emit('switch-module', key)
}

/**
 * 处理帮助与反馈 - 打开设置对话框并定位到帮助标签页
 */
function handleHelp() {
  emit('open-settings', 'help')
}

/**
 * 打开全局搜索
 */
function handleOpenSearch() {
  emit('open-search')
}
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

// ============================================================================
// 导航容器
// ============================================================================
.dingtalk-nav {
  width: 72px;
  background: var(--dt-bg-sidebar-gradient);
  box-shadow: var(--dt-shadow-3);
  animation: slideInLeft 0.4s var(--dt-ease-out);
}

.bg-nav-light {
  background: linear-gradient(180deg, #1677ff 0%, #0e5fd9 100%);
}

.bg-nav-dark {
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
}

// ============================================================================
// Logo 区域
// ============================================================================
.nav-logo-wrapper {
  padding: 8px 0;
  width: 100%;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

.nav-logo {
  position: relative;
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--dt-radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  transition: all 0.3s var(--dt-ease-out);
  animation: fadeInDown 0.5s var(--dt-ease-bounce);

  @include hover-lift;
}

.nav-logo:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.05);
}

.nav-logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.5px;
  line-height: 1;
  background: linear-gradient(135deg, #ffffff 0%, #e0e7ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-logo-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: var(--dt-error-color);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  animation: scaleIn 0.3s var(--dt-ease-bounce), pulse 2s ease-in-out infinite;
}

// ============================================================================
// 导航项区域
// ============================================================================
.nav-items {
  flex: 1;
  width: 100%;
  padding: 4px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-items::-webkit-scrollbar {
  width: 4px;
}

.nav-items::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

// ============================================================================
// 导航项
// ============================================================================
.nav-item {
  position: relative;
  width: 48px;
  height: 48px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-lg);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.3s var(--dt-ease-out);
  color: rgba(255, 255, 255, 0.85) !important; // 强制使用白色，防止登录后同步延迟导致的变色
  animation: fadeInUp 0.4s var(--dt-ease-out) both;

  @for $i from 1 through 9 {
    &:nth-child(#{$i}) {
      animation-delay: #{0.05 * $i}s;
    }
  }
}

.nav-item:hover {
  background: var(--dt-bg-sidebar-item-hover);
  color: #fff;
  transform: scale(1.08);
}

.nav-item-active {
  background: var(--dt-bg-sidebar-item-active);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  animation: pulse 0.3s var(--dt-ease-out);
}

.nav-item-active::before {
  content: '';
  position: absolute;
  left: -12px;
  width: 4px;
  height: 24px;
  background: #fff;
  border-radius: 0 4px 4px 0;
  animation: slideInLeft 0.2s var(--dt-ease-out);
}

.nav-item-icon {
  font-size: 22px;
  transition: transform 0.3s var(--dt-ease-out);
}

.nav-item:hover .nav-item-icon {
  transform: scale(1.15);
}

.nav-item-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: var(--dt-error-color);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  animation: scaleIn 0.3s var(--dt-ease-bounce);
}

// ============================================================================
// 底部操作区
// ============================================================================
.nav-footer {
  padding: 4px 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 100%;
  flex-shrink: 0;
  animation: fadeInUp 0.5s var(--dt-ease-out) both;
}

.nav-item-action {
  width: 48px;
  height: 48px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-lg);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.3s var(--dt-ease-out);
  color: rgba(255, 255, 255, 0.65);
  position: relative;
}

.nav-item-action:hover {
  background: var(--dt-bg-sidebar-item-hover);
  color: rgba(255, 255, 255, 0.9);
  transform: scale(1.08);
}

.nav-item-action.nav-item-active {
  background: var(--dt-bg-sidebar-item-active);
  color: #fff;
}

.auto-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 12px;
  height: 12px;
  background: #fff;
  color: var(--dt-brand-color);
  font-size: 9px;
  font-weight: 700;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: scaleIn 0.3s var(--dt-ease-bounce);
}

// ============================================================================
// 用户头像
// ============================================================================
.nav-avatar {
  position: relative;
  width: 52px;
  height: 52px;
  margin: 4px auto 0;
  padding: 3px;
  border-radius: 50%;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.3s var(--dt-ease-out);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeInUp 0.6s var(--dt-ease-bounce) both;
}

.nav-avatar:hover {
  background: rgba(255, 255, 255, 0.12);
  transform: scale(1.08);
}

.nav-avatar-active {
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.2);
}

.nav-avatar :deep(.dingtalk-avatar) {
  width: 100%;
  height: 100%;
  transition: transform 0.3s var(--dt-ease-out);
}

.nav-avatar:hover :deep(.dingtalk-avatar) {
  transform: scale(1.05);
}

.nav-avatar :deep(.avatar-inner) {
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: border-color 0.3s var(--dt-ease-out);
}

.nav-avatar:hover :deep(.avatar-inner) {
  border-color: rgba(255, 255, 255, 0.6);
}

.nav-avatar-active :deep(.avatar-inner) {
  border-color: rgba(255, 255, 255, 0.9);
}

.nav-avatar-status {
  position: absolute;
  bottom: 4px;
  right: 4px;
  width: 12px;
  height: 12px;
  background: var(--dt-text-tertiary);
  border: 2px solid var(--dt-bg-sidebar);
  border-radius: 50%;
  transition: all 0.3s var(--dt-ease-out);
}

.nav-avatar-status.online {
  background: var(--dt-success-color);
  box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.2);
  animation: pulse 2s ease-in-out infinite;
}

// ============================================================================
// 暗色模式适配
// ============================================================================
.dark .dingtalk-nav {
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
}

.dark .nav-logo {
  background: rgba(255, 255, 255, 0.08);
}

.dark .nav-logo:hover {
  background: rgba(255, 255, 255, 0.12);
}

.dark .nav-item {
  color: rgba(255, 255, 255, 0.6);
}

.dark .nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.dark .nav-item-active {
  background: rgba(22, 119, 255, 0.2);
}

.dark .nav-item-active::before {
  background: var(--dt-brand-color);
}

.dark .nav-item-action {
  color: rgba(255, 255, 255, 0.5);
}

.dark .nav-item-action:hover {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.8);
}

.dark .nav-avatar-status {
  border-color: #1e293b;
}

.dark .nav-avatar-active {
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.3);
}
</style>
