<template>
  <aside class="side-nav">
    <!-- 顶部 Logo 区 (钉钉规范：40x40，圆角8px) -->
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
        :title="item.name"
      >
        <el-icon :size="20">
          <component :is="item.icon" />
        </el-icon>
        <div v-if="item.badge > 0" class="nav-badge">
          {{ item.badge > 99 ? '99+' : item.badge }}
        </div>
      </div>
    </div>

    <!-- 分割线 -->
    <div class="nav-divider" />

    <!-- 底部功能区 - 直接显示 -->
    <div class="nav-bottom">
      <div class="nav-item" title="搜索" @click="switchModule('search')">
        <el-icon :size="18"><Search /></el-icon>
      </div>
      <div class="nav-item" title="个人资料" @click="openEditProfile">
        <el-icon :size="18"><User /></el-icon>
      </div>
      <div class="nav-item" title="设置" @click="openSystemSettings">
        <el-icon :size="18"><Setting /></el-icon>
      </div>
      <div class="nav-item" title="切换主题" @click="handleToggleTheme">
        <el-icon :size="18"><Sunny v-if="isDark" /><Moon v-else /></el-icon>
      </div>
    </div>

    <!-- 用户头像 - 点击打开个人资料 -->
    <div class="user-avatar-wrapper" @click="openEditProfile" title="个人资料">
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
  Tickets, Setting, Sunny, Moon, Star, Bell, Document, MoreFilled
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
  { id: 'workreport', name: '日志', icon: Document, badge: 0 },
  { id: 'favorites', name: '收藏', icon: Star, badge: 0 }
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
.side-nav {
  width: 64px;
  height: 100%;
  background: var(--dt-brand-color);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
  flex-shrink: 0;
  z-index: 100;
}

/* Logo区域：40x40，圆角8px，渐变背景 */
.nav-logo-wrapper { margin-bottom: 24px; }
.nav-logo {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: var(--dt-brand-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.3s;
}
.nav-logo:hover { transform: scale(1.05); }
.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--dt-text-white);
  letter-spacing: 1px;
}

.nav-top { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.nav-item {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.65);
  transition: all 0.2s ease;
}

.nav-item:hover { background-color: rgba(255, 255, 255, 0.08); color: rgba(255, 255, 255, 0.95); }
.nav-item.active { background-color: var(--dt-brand-color); color: var(--dt-text-white); }

.nav-item.active::before {
  content: ''; position: absolute; left: 0; top: 10px; bottom: 10px; width: 3px; background-color: var(--dt-text-white); border-radius: 0 2px 2px 0;
}

.nav-badge {
  position: absolute; top: 2px; right: 2px; background-color: var(--dt-error-color); color: var(--dt-text-white);
  font-size: 10px; height: 14px; min-width: 14px; padding: 0 var(--dt-spacing-xs); border-radius: var(--dt-radius-md);
  display: flex; align-items: center; justify-content: center; font-weight: 600;
}

/* 分割线 */
.nav-divider {
  width: 32px;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.3) 30%,
    rgba(255, 255, 255, 0.3) 70%,
    transparent 100%
  );
  margin: 0 auto var(--dt-spacing-lg);
  flex-shrink: 0;
}

/* 底部工具栏 */
.nav-bottom {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--dt-spacing-xs);
  padding: var(--dt-spacing-sm) 0;
  width: 100%;
}

.toolbar-btn {
  position: relative;
  width: 40px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: var(--dt-radius-lg);
  color: rgba(255, 255, 255, 0.55);
  transition: all 0.2s ease;
}

.toolbar-btn:hover {
  color: rgba(255, 255, 255, 0.95);
  background: rgba(255, 255, 255, 0.1);
}

.toolbar-btn:active {
  transform: scale(0.95);
  background: rgba(255, 255, 255, 0.15);
}

/* 工具提示 - 改为按钮下方显示 */
.toolbar-tooltip {
  position: absolute;
  left: 50%;
  bottom: calc(100% + 8px);
  transform: translateX(-50%);
  background: var(--dt-bg-card);
  color: var(--dt-text-primary);
  font-size: var(--dt-font-size-sm);
  font-weight: 500;
  padding: 4px 10px;
  border-radius: var(--dt-radius-sm);
  white-space: nowrap;
  box-shadow: var(--dt-shadow-float);
  border: 1px solid var(--dt-border-light);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.15s ease, transform 0.15s ease;
  z-index: 999;
}

.toolbar-tooltip::after {
  content: '';
  position: absolute;
  left: 50%;
  top: 100%;
  transform: translateX(-50%);
  border: 5px solid transparent;
  border-top-color: var(--dt-bg-card);
}

.toolbar-btn:hover .toolbar-tooltip {
  opacity: 1;
  transform: translateX(-50%) translateY(-4px);
}

/* 用户头像 */
.user-avatar-wrapper {
  position: relative;
  width: 40px;
  height: 40px;
  margin-top: var(--dt-spacing-md);
  cursor: pointer;
  flex-shrink: 0;
}

.avatar-ring {
  position: absolute;
  inset: -3px;
  border-radius: var(--dt-radius-sm);
  background: linear-gradient(135deg, rgba(255,255,255,0.4) 0%, rgba(255,255,255,0.1) 100%);
  opacity: 0.6;
  transition: all 0.3s ease;
}

.user-avatar-wrapper:hover .avatar-ring {
  opacity: 1;
  inset: -4px;
  border-radius: calc(var(--dt-radius-sm) + 1px);
  background: linear-gradient(135deg, rgba(255,255,255,0.6) 0%, rgba(255,255,255,0.2) 100%);
  box-shadow: 0 0 16px rgba(255, 255, 255, 0.3);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-sm);
  border: none;
  position: relative;
  z-index: 1;
  display: block;
  transition: transform 0.2s ease;
}

.user-avatar-wrapper:hover .user-avatar {
  transform: scale(1.08);
}

.online-status-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 11px;
  height: 11px;
  background-color: var(--dt-success-color);
  border: 2px solid var(--dt-brand-color);
  border-radius: 50%;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: var(--dt-success-color);
  animation: statusPulse 2.5s ease-in-out infinite;
}

@keyframes statusPulse {
  0% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(2); opacity: 0; }
  100% { transform: scale(1); opacity: 0; }
}

.nav-popover-content { display: flex; flex-direction: column; gap: 2px; }
.nav-popover-item {
  display: flex; align-items: center; gap: var(--dt-spacing-sm); padding: var(--dt-spacing-sm) var(--dt-spacing-md);
  border-radius: var(--dt-radius-sm); cursor: pointer; color: var(--dt-text-primary); font-size: var(--dt-font-size-sm);
  &:hover { background: var(--dt-bg-session-hover); }
}
.nav-popover-label { flex: 1; }
.nav-popover-divider { height: 1px; background: var(--dt-border-light); margin: var(--dt-spacing-xs) 0; }
</style>
