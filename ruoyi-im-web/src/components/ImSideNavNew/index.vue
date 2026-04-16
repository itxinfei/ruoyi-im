<template>
  <aside class="side-nav">
    <!-- 顶部 Logo 区 -->
    <div class="nav-logo-wrapper">
      <div class="nav-logo">
        <span class="logo-text">IM</span>
      </div>
    </div>

    <!-- 顶部功能区 -->
    <div class="nav-top">
      <div
        v-for="item in topNavs"
        :key="item.id"
        :class="['nav-item', { active: activeModule === item.id }]"
        @click="switchModule(item.id)"
      >
        <el-icon :size="20">
          <component :is="item.icon" />
        </el-icon>
        <span class="nav-label">{{ item.name }}</span>
        <div v-if="item.badge > 0" class="nav-badge">
          {{ item.badge > 99 ? '99+' : item.badge }}
        </div>
      </div>
    </div>

    <!-- 分割线 -->
    <div class="nav-divider" />

    <!-- 底部功能区 -->
    <div class="nav-bottom">
      <div class="nav-item" @click="switchModule('search')">
        <el-icon :size="20">
          <Search />
        </el-icon>
        <span class="nav-label">搜索</span>
      </div>
      <div class="nav-item" @click="openSystemSettings">
        <el-icon :size="20">
          <Setting />
        </el-icon>
        <span class="nav-label">设置</span>
      </div>
      <div class="nav-item" @click="handleToggleTheme">
        <el-icon :size="20">
          <Sunny v-if="isDark" /><Moon v-else />
        </el-icon>
        <span class="nav-label">{{ isDark ? '亮色' : '暗黑' }}</span>
      </div>
    </div>

    <!-- 用户头像 -->
    <div class="user-avatar-wrapper" @click="openEditProfile">
      <div class="avatar-ring" />
      <img :src="userAvatar" class="user-avatar" alt="me">
      <div class="online-status-dot">
        <span class="status-pulse" />
      </div>
    </div>
  </aside>
</template>

<script setup lang="js">
/**
 * ImSideNavNew (设置恢复版)
 */
import { ref } from 'vue'
import {
  ChatDotRound, Search, User, Menu, FolderOpened, Calendar,
  Tickets, Setting, Sunny, Moon, Star, Bell, Document, MoreFilled, Phone
} from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'

defineProps({
  activeModule: { type: String, default: 'chat' }
})

const emit = defineEmits(['switch-module', 'open-edit-profile', 'open-system-settings'])
const { isDark, toggleTheme } = useTheme()

// 状态
const userAvatar = ref('/avatars/me.svg')

// 导航项配置
const topNavs = [
  { id: 'chat', name: '消息', icon: ChatDotRound, badge: 5 },
  { id: 'contacts', name: '通讯录', icon: User, badge: 0 },
  { id: 'ding', name: 'DING', icon: Bell, badge: 0 },
  { id: 'workbench', name: '工作台', icon: Menu, badge: 0 },
  { id: 'todo', name: '待办', icon: Tickets, badge: 2 },
  { id: 'calendar', name: '日历', icon: Calendar, badge: 0 },
  { id: 'documents', name: '云盘', icon: FolderOpened, badge: 0 },
  { id: 'call', name: '通话', icon: Phone, badge: 0 }
]

const switchModule = (moduleId) => {
  emit('switch-module', moduleId)
}

const openEditProfile = () => {
  emit('open-edit-profile')
}

const openSystemSettings = () => {
  emit('open-system-settings')
}

const handleToggleTheme = () => {
  toggleTheme()
}
</script>

<style scoped>
/* 导航栏容器 */
.side-nav {
  width: 64px;
  height: 100%;
  background: var(--dt-nav-sidebar-bg, var(--dt-brand-color));
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4px 0;
  flex-shrink: 0;
  z-index: 100;
}

/* Logo区域 */
.nav-logo-wrapper {
  margin-bottom: 4px;
  padding-top: 0;
}

.nav-logo {
  width: var(--dt-logo-size);
  height: var(--dt-logo-size);
  border-radius: var(--dt-radius-lg);
  background: var(--dt-nav-logo-bg, var(--dt-brand-gradient));
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: opacity var(--dt-transition-base);
}

.nav-logo:hover {
  opacity: 0.9;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--dt-text-white);
  letter-spacing: 1px;
}

/* 顶部导航区 */
.nav-top {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0;
  padding: 0;
  width: 100%;
}

/* 导航项 - 紧凑矩形按钮 */
.nav-item {
  position: relative;
  width: 100%;
  height: 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  cursor: pointer;
  border-radius: var(--dt-radius-lg);
  color: var(--dt-nav-sidebar-text);
  transition: background-color var(--dt-transition-fast), color var(--dt-transition-fast);
  margin: 0;
}

.nav-item:hover {
  background-color: var(--dt-nav-sidebar-hover);
  color: var(--dt-nav-sidebar-text-hover);
}

.nav-item.active {
  background-color: var(--dt-nav-sidebar-active);
  color: var(--dt-nav-sidebar-text-active);
}

.nav-item .el-icon {
  font-size: 20px;
}

.nav-label {
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  line-height: 1.2;
  white-space: nowrap;
}

.nav-badge {
  position: absolute;
  top: 4px;
  right: 8px;
  background-color: var(--dt-error-color);
  color: var(--dt-text-white);
  font-size: 10px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: var(--dt-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

/* 分割线 */
.nav-divider {
  width: 48px;
  height: 1px;
  background: var(--dt-border-light);
  margin: 4px auto;
  flex-shrink: 0;
}

/* 底部工具栏 */
.nav-bottom {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0;
  padding: 0;
  width: 100%;
}

.nav-bottom .nav-item {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0;
  padding: 1px 0;
  cursor: pointer;
  border-radius: 0;
  color: var(--dt-nav-sidebar-text);
  transition: background-color var(--dt-transition-fast), color var(--dt-transition-fast);
}

.nav-bottom .nav-item:hover {
  color: var(--dt-nav-sidebar-text-hover);
  background: var(--dt-nav-sidebar-hover);
}

.nav-bottom .nav-item:active {
  background: var(--dt-nav-sidebar-active);
}

/* 用户头像 - 钉钉风格 */
.user-avatar-wrapper {
  position: relative;
  width: 44px;
  height: 44px;
  margin-top: 8px;
  cursor: pointer;
  flex-shrink: 0;
}

.avatar-ring {
  position: absolute;
  inset: 0;
  border-radius: var(--dt-radius-lg);
  background: var(--dt-avatar-ring-bg-dark);
  border: 2px solid var(--dt-avatar-ring-border-dark);
  transition: background-color var(--dt-transition-base), border-color var(--dt-transition-base), box-shadow var(--dt-transition-base);
}

.user-avatar-wrapper:hover .avatar-ring {
  background: var(--dt-avatar-ring-bg-dark-hover);
  border-color: var(--dt-avatar-ring-border-dark-hover);
  box-shadow: 0 0 12px var(--dt-avatar-ring-shadow-dark-hover);
}

.user-avatar {
  width: var(--dt-avatar-size-md);
  height: var(--dt-avatar-size-md);
  border-radius: var(--dt-radius-lg);
  border: none;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
  display: block;
  object-fit: cover;
}

.user-avatar-wrapper:hover .user-avatar {
  opacity: 0.9;
}

.online-status-dot {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 11px;
  height: 11px;
  background-color: var(--dt-success-color);
  border: 2px solid var(--dt-brand-color);
  border-radius: 50%;
  z-index: 2;
}
</style>
