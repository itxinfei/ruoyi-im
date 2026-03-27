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

    <!-- 底部功能区 -->
    <div class="nav-bottom">
      <!-- 搜索 -->
      <div
        :class="['nav-item', { active: activeModule === 'search' }]"
        @click="switchModule('search')"
        title="搜索"
      >
        <el-icon :size="20">
          <Search />
        </el-icon>
      </div>

      <!-- 个人设置 -->
      <div
        :class="['nav-item', { active: activeModule === 'profile' }]"
        @click="switchModule('profile')"
        title="个人设置"
      >
        <el-icon :size="20">
          <Setting />
        </el-icon>
      </div>

      <!-- 主题切换 -->
      <div class="nav-item" @click="handleToggleTheme" title="切换模式">
        <el-icon :size="20">
          <Sunny v-if="isDark" />
          <Moon v-else />
        </el-icon>
      </div>

      <!-- 用户头像 -->
      <div class="user-avatar-wrapper" @click="openProfile" title="个人资料">
        <img :src="userAvatar" class="user-avatar" alt="me">
        <div class="online-status-dot" />
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
  Tickets, Setting, Sunny, Moon, Star
} from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'

defineProps({
  activeModule: { type: String, default: 'chat' }
})

const emit = defineEmits(['switch-module'])
const { isDark, toggleTheme } = useTheme()

// 状态
const userAvatar = ref('/avatars/me.svg')

// 导航项配置
const topNavs = [
  { id: 'chat', name: '消息', icon: ChatDotRound, badge: 5 },
  { id: 'contacts', name: '通讯录', icon: User, badge: 0 },
  { id: 'workbench', name: '工作台', icon: Menu, badge: 0 },
  { id: 'todo', name: '待办', icon: Tickets, badge: 2 },
  { id: 'calendar', name: '日历', icon: Calendar, badge: 0 },
  { id: 'documents', name: '云盘', icon: FolderOpened, badge: 0 },
  { id: 'favorites', name: '收藏', icon: Star, badge: 0 }
]

const switchModule = (moduleId) => {
  emit('switch-module', moduleId)
}

const openProfile = () => {
  emit('switch-module', 'profile')
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
.nav-item.active { background-color: rgba(255, 255, 255, 0.12); color: var(--dt-text-white); }

.nav-item.active::before {
  content: ''; position: absolute; left: 0; top: 10px; bottom: 10px; width: 3px; background-color: var(--dt-brand-color); border-radius: 0 2px 2px 0;
}

.nav-badge {
  position: absolute; top: 2px; right: 2px; background-color: var(--dt-error-color); color: var(--dt-text-white);
  font-size: 10px; height: 14px; min-width: 14px; padding: 0 3px; border-radius: 7px;
  display: flex; align-items: center; justify-content: center; font-weight: 600;
}

.nav-bottom { margin-top: auto; display: flex; flex-direction: column; align-items: center; gap: 8px; }

.user-avatar-wrapper { position: relative; padding: 2px; cursor: pointer; transition: transform 0.2s; margin-top: 8px; }
.user-avatar-wrapper:hover { transform: scale(1.05); }
.user-avatar { width: 36px; height: 36px; border-radius: 8px; border: 2px solid rgba(255, 255, 255, 0.15); }
.online-status-dot { position: absolute; bottom: 0; right: 0; width: 10px; height: 10px; background-color: var(--dt-success-color); border: 2px solid var(--dt-bg-sidebar); border-radius: 50%; }
</style>
