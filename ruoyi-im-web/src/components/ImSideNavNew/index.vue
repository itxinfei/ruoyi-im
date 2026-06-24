<template>
  <aside class="dt-premium-l1">
    <!-- 1. 顶部 Logo -->
    <div class="l1-header">
      <div class="logo-box">
        <el-icon><Promotion /></el-icon>
      </div>
    </div>

    <!-- 2. 中间主导航 (已对齐) -->
    <div class="l1-body custom-scrollbar">
      <div
        v-for="item in topNavs"
        :key="item.id"
        class="nav-v4-item-wrap"
        :class="{ active: activeModule === item.id }"
        @click="$emit('switch-module', item.id)"
      >
        <div class="indicator"></div>
        <div class="nav-v4-content">
          <el-icon class="icon">
            <component :is="activeModule === item.id ? item.activeIcon : item.icon" />
          </el-icon>
          <span class="label">{{ item.name }}</span>
        </div>
        <div v-if="item.badge" class="v4-badge">{{ item.badge }}</div>
      </div>
    </div>

    <!-- 3. 底部功能菜单 -->
    <div class="l1-footer">
      <!-- 搜索 -->
      <div class="footer-action" :class="{ active: activeModule === 'search' }" @click="$emit('switch-module', 'search')">
        <el-icon><Search /></el-icon>
        <span class="action-label">搜索</span>
      </div>

      <!-- 主题切换 -->
      <div class="footer-action" :class="{ active: isDark }" @click="handleToggleTheme">
        <el-icon><component :is="isDark ? Moon : Sunny" /></el-icon>
        <span class="action-label">主题</span>
      </div>

      <!-- 设置 -->
      <div class="footer-action" :class="{ active: activeModule === 'settings' }" @click="$emit('open-system-settings')">
        <el-icon><Setting /></el-icon>
        <span class="action-label">设置</span>
      </div>

      <!-- 个人头像 -->
      <div class="v4-profile-trigger" @click="$emit('open-edit-profile')">
        <div class="avatar-container">
          <img src="/avatars/me.svg" class="main-avatar">
          <div class="status-ring online"></div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="js">
import { markRaw } from 'vue'
import {
  ChatDotRound, User, Bell, Menu, Tickets, Calendar,
  FolderOpened, FolderChecked, Phone, Search, Setting,
  InfoFilled, Promotion, ChatLineRound, Sunny, Moon,
  UserFilled, BellFilled, Grid, List,
  PhoneFilled
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useTheme } from '@/composables/useTheme'

const { isDark, toggleTheme, themeModeLabel } = useTheme()

const handleToggleTheme = () => {
  toggleTheme()
  ElMessage.success(`主题已切换为：${themeModeLabel.value}`)
}

defineProps({ activeModule: String })
const emit = defineEmits(['switch-module', 'open-edit-profile', 'open-system-settings'])

const topNavs = [
  { id: 'chat', name: '消息', icon: markRaw(ChatLineRound), activeIcon: markRaw(ChatDotRound), badge: 5 },
  { id: 'contacts', name: '通讯录', icon: markRaw(User), activeIcon: markRaw(UserFilled) },
  { id: 'workbench', name: '工作台', icon: markRaw(Menu), activeIcon: markRaw(Grid) },
  { id: 'todo', name: '待办', icon: markRaw(Tickets), activeIcon: markRaw(List), badge: 2 },
  { id: 'documents', name: '云盘', icon: markRaw(FolderOpened), activeIcon: markRaw(FolderChecked) },
  { id: 'calendar', name: '日历', icon: markRaw(Calendar), activeIcon: markRaw(Calendar) },
  { id: 'approval', name: '审批', icon: markRaw(Tickets), activeIcon: markRaw(Tickets) },
  { id: 'mail', name: '邮件', icon: markRaw(FolderOpened), activeIcon: markRaw(FolderOpened) }
]
</script>

<style scoped lang="scss">
// ============================================================================
// 钉钉规范：侧边栏导航 64px
// ============================================================================

.dt-premium-l1 {
  width: var(--dt-nav-sidebar-width, 64px);
  height: 100%;
  background: var(--dt-nav-sidebar-bg);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--dt-spacing-md) 0;
  flex-shrink: 0;
  z-index: 2000;
  border-right: 1px solid var(--dt-border-subtle, rgba(255, 255, 255, 0.05));
}

// ============================================================================
// Logo
// ============================================================================

.l1-header {
  margin-bottom: var(--dt-spacing-lg);
}

.logo-box {
  width: 36px;
  height: 36px;
  background: var(--dt-brand-gradient);
  border-radius: var(--dt-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dt-text-white);
  font-size: var(--dt-font-size-xl);
  box-shadow: var(--dt-shadow-brand);
}

// ============================================================================
// 导航主体
// ============================================================================

.l1-body {
  flex: 1;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

// ============================================================================
// 导航项
// ============================================================================

.nav-v4-item-wrap {
  position: relative;
  width: 100%;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.15s;

  .indicator {
    position: absolute;
    left: 0;
    width: 3px;
    height: 18px;
    background: var(--dt-brand-color);
    border-radius: 0 2px 2px 0;
    opacity: 0;
    transition: opacity 0.2s, height 0.2s;
  }

    .nav-v4-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    color: var(--dt-nav-sidebar-text);
    transition: color 0.2s;

    .icon {
      font-size: 20px;
    }

    .label {
      font-size: var(--dt-font-size-xs);
      font-weight: 500;
    }
  }

  &:hover {
    background: var(--dt-nav-hover);

    .nav-v4-content {
      color: var(--dt-nav-sidebar-text-hover, rgba(255, 255, 255, 0.9));
    }
  }

  &.active {
    .indicator {
      opacity: 1;
      height: 22px;
    }

    .nav-v4-content {
      color: var(--dt-nav-sidebar-text-active);
      font-weight: 600;
    }

    background: var(--dt-nav-active);
  }
}

// ============================================================================
// 未读角标
// ============================================================================

.v4-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: var(--dt-error-color);
  color: var(--dt-text-white);
  font-size: var(--dt-font-size-xs);
  height: 18px;
  min-width: 18px;
  border-radius: var(--dt-radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  border: 2px solid var(--dt-nav-sidebar-bg);
  font-weight: 700;
}

// ============================================================================
// 底部功能区
// ============================================================================

.l1-footer {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding-bottom: var(--dt-spacing-md);
}

.footer-action {
  width: 52px;
  height: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: var(--dt-nav-sidebar-text);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: background-color 0.15s, color 0.15s;

  .action-label {
    font-size: var(--dt-font-size-xs);
    font-weight: 500;
  }

  &:hover {
    background: var(--dt-nav-hover);
    color: var(--dt-nav-sidebar-text-hover, rgba(255, 255, 255, 0.9));
  }

  &.active {
    color: var(--dt-nav-sidebar-text-active);
    background: var(--dt-nav-active);
  }
}

// ============================================================================
// 个人头像
// ============================================================================

.v4-profile-trigger {
  position: relative;
  width: 36px;
  height: 36px;
  cursor: pointer;
  margin-top: 4px;
}

.avatar-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.main-avatar {
  width: 100%;
  height: 100%;
  border-radius: var(--dt-radius-lg);
  background: var(--dt-nav-sidebar-bg);
  object-fit: cover;
}

.status-ring {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid var(--dt-nav-sidebar-bg);

  &.online {
    background: var(--dt-success-color);
  }
}
</style>
