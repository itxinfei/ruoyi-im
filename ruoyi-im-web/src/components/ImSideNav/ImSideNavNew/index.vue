<template>
  <nav
    :class="['dingtalk-nav', isDark ? 'nav-dark' : 'nav-light']"
    role="navigation"
    aria-label="主导航"
  >
    <!-- Logo 区域 -->
    <div class="nav-logo-area">
      <button
        class="nav-logo"
        aria-label="首页"
        @click="handleSwitch('chat')"
      >
        <img
          v-if="logoUrl"
          :src="logoUrl"
          class="nav-logo-image"
          alt="Logo"
        >
        <svg
          v-else
          class="nav-logo-svg"
          viewBox="0 0 36 36"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M5 8a4 4 0 014-4h10a4 4 0 014 4v7a4 4 0 01-4 4h-1l-4 4v-4H9a4 4 0 01-4-4V8z"
            fill="rgba(255,255,255,0.35)"
          />
          <path
            d="M13 14a4 4 0 014-4h10a4 4 0 014 4v7a4 4 0 01-4 4h-4l-4 4v-4h-2a4 4 0 01-4-4v-7z"
            fill="white"
          />
        </svg>
        <span
          v-if="unreadCount > 0"
          class="nav-logo-badge"
        >
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </button>
    </div>

    <!-- 导航项容器 -->
    <div
      class="nav-items-scroll scrollbar-none"
      role="menubar"
    >
      <el-tooltip
        v-for="item in navModules"
        :key="item.key"
        :content="item.label"
        placement="right"
        :show-after="400"
        :hide-after="0"
        effect="dark"
      >
        <button
          :class="[
            'nav-item',
            { 'is-active': activeModule === item.key }
          ]"
          :aria-label="item.label"
          :aria-current="activeModule === item.key ? 'page' : undefined"
          role="menuitem"
          @click="handleSwitch(item.key)"
        >
          <div
            class="nav-icon-bg"
            :style="{ backgroundColor: item.color }"
          >
            <span class="material-icons-outlined nav-icon">{{ item.icon }}</span>
          </div>
          <span
            v-if="item.badge"
            class="nav-badge"
          >{{ item.badge }}</span>
          <div class="active-bar" />
        </button>
      </el-tooltip>
    </div>

    <!-- 底部操作区 -->
    <div class="nav-footer">
      <!-- 搜索按钮 -->
      <el-tooltip
        content="全局搜索"
        placement="right"
        :show-after="400"
        effect="dark"
      >
        <button
          class="nav-item nav-footer-item"
          aria-label="全局搜索"
          @click="handleOpenSearch"
        >
          <span class="material-icons-outlined nav-footer-icon">search</span>
        </button>
      </el-tooltip>

      <!-- 设置按钮 -->
      <el-tooltip
        content="设置"
        placement="right"
        :show-after="400"
        effect="dark"
      >
        <button
          :class="['nav-item nav-footer-item', { 'is-active': activeModule === 'settings' }]"
          aria-label="设置"
          @click="handleSwitch('settings')"
        >
          <span class="material-icons-outlined nav-footer-icon">settings</span>
          <div class="active-bar" />
        </button>
      </el-tooltip>

      <!-- 主题切换按钮 -->
      <el-tooltip
        :content="isDark ? '切换到浅色模式' : '切换到深色模式'"
        placement="right"
        :show-after="400"
        effect="dark"
      >
        <button
          class="nav-item nav-footer-item nav-theme-toggle"
          :aria-label="isDark ? '切换到浅色模式' : '切换到深色模式'"
          @click="toggleDark"
        >
          <span class="material-icons-outlined nav-footer-icon">
            {{ isDark ? 'light_mode' : 'dark_mode' }}
          </span>
        </button>
      </el-tooltip>

      <!-- 用户头像 -->
      <el-tooltip
        :content="`个人资料: ${currentUser.nickname || currentUser.username || '我'}`"
        placement="right"
        :show-after="400"
        effect="dark"
      >
        <button
          class="nav-avatar-btn"
          :class="{ 'is-active': activeModule === 'profile' }"
          @click="handleSwitch('profile')"
        >
          <DingtalkAvatar
            :src="currentUser.avatar"
            :name="currentUser.nickname || currentUser.username || '我'"
            :user-id="currentUser.id"
            :size="34"
            shape="circle"
          />
          <span
            class="status-dot"
            :class="{ 'online': isUserOnline }"
          />
        </button>
      </el-tooltip>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useTheme } from '@/composables/useTheme'
import request from '@/api/request'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  activeModule: {
    type: String,
    default: 'chat'
  }
})

const emit = defineEmits(['switch-module', 'open-search', 'open-settings'])
const store = useStore()
const { isDark, toggleDark } = useTheme()

const logoUrl = ref(null)

onMounted(async () => {
  if (store.getters['user/isAdmin']) {
    try {
      const res = await request.get('/api/admin/config/logo')
      if (res.code === 200 && res.data) {
        logoUrl.value = res.data
      }
    } catch (error) {
      console.error('获取系统Logo失败:', error)
    }
  }
})

const unreadCount = computed(() => store.state.im?.totalUnreadCount || 0)

const currentUser = computed(() => {
  const user = store.getters['user/currentUser']
  return user && Object.keys(user).length > 0
    ? user
    : { nickname: '加载中...', username: '...', avatar: '' }
})

const isUserOnline = computed(() => {
  if (!currentUser.value?.id) { return false }
  // 检查联系人模块中的状态
  const status = store.state.im?.contact?.userStatus?.[currentUser.value.id]
  if (status === 'online') {return true}

  // 如果联系人模块没查到，同时本端 WebSocket 已连接，则认为是在线的
  return store.state.im?.wsConnected || false
})

// 导航模块 — 每个模块独立配色 + Material Icons Outlined
const navModules = ref([
  { key: 'chat', label: '消息', icon: 'chat', color: 'var(--dt-brand-color)' },
  { key: 'contacts', label: '通讯录', icon: 'people', color: '#00C853' },
  { key: 'workbench', label: '工作台', icon: 'grid_view', color: '#FF9800' },
  { key: 'drive', label: '云盘', icon: 'cloud', color: '#7C4DFF' },
  { key: 'calendar', label: '日历', icon: 'calendar_today', color: '#EF5350' },
  { key: 'todo', label: '待办', icon: 'task_alt', color: '#26C6DA' },
  { key: 'approval', label: '审批', icon: 'description', color: '#5C6BC0' },
  { key: 'mail', label: '邮箱', icon: 'mail', color: '#FF7043' },
  { key: 'appcenter', label: '应用中心', icon: 'apps', color: '#66BB6A' },
  { key: 'assistant', label: 'AI助理', icon: 'smart_toy', color: '#AB47BC' }
])

function handleSwitch(key) {
  emit('switch-module', key)
}

function handleOpenSearch() {
  emit('open-search')
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 侧边栏容器（公共）
// ============================================================================
.dingtalk-nav {
  width: var(--dt-nav-sidebar-width);
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  user-select: none;
  z-index: 100;
  flex-shrink: 0;
  transition: background 0.3s ease;
}

// ============================================================================
// 浅色主题
// ============================================================================
.nav-light {
  background: #F0F2F5;

  .nav-item:hover {
    background: rgba(0, 0, 0, 0.05);
  }

  .nav-item.is-active {
    background: rgba(0, 0, 0, 0.07);
  }

  .active-bar {
    background: var(--dt-brand-color);
  }

  .nav-footer {
    border-top-color: rgba(0, 0, 0, 0.06);
  }

  .nav-footer-item {
    color: #858E9E;

    &:hover {
      background: rgba(0, 0, 0, 0.05);
      color: #5F6672;
    }

    &.is-active {
      color: var(--dt-brand-color);
    }
  }

  .nav-footer-icon {
    color: inherit;
  }

  .nav-avatar-btn {
    &:hover {
      background: rgba(0, 0, 0, 0.05);
    }

    &.is-active {
      background: rgba(0, 0, 0, 0.07);
    }
  }

  .status-dot {
    border-color: #F0F2F5;
  }

  .nav-theme-toggle:hover {
    color: #FF9800;
  }
}

// ============================================================================
// 深色主题
// ============================================================================
.nav-dark {
  background: #2B3440;

  .nav-item:hover {
    background: rgba(255, 255, 255, 0.08);
  }

  .nav-item.is-active {
    background: rgba(255, 255, 255, 0.1);
  }

  .active-bar {
    background: #fff;
  }

  .nav-footer {
    border-top-color: rgba(255, 255, 255, 0.08);
  }

  .nav-footer-item {
    color: rgba(255, 255, 255, 0.5);

    &:hover {
      background: rgba(255, 255, 255, 0.08);
      color: rgba(255, 255, 255, 0.85);
    }

    &.is-active {
      color: #fff;
    }
  }

  .nav-footer-icon {
    color: inherit;
  }

  .nav-avatar-btn {
    &:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    &.is-active {
      background: rgba(255, 255, 255, 0.15);
    }
  }

  .status-dot {
    border-color: #2B3440;
  }

  .nav-theme-toggle:hover {
    color: #FFCA28;
  }
}

// ============================================================================
// Logo 区域（通用）
// ============================================================================
.nav-logo-area {
  padding: 14px 0 10px;
  flex-shrink: 0;
}

.nav-logo {
  position: relative;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: var(--dt-brand-color);
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease;

  &:hover {
    background: var(--dt-brand-color);
    filter: brightness(1.1);
    transform: scale(1.05);
  }

  &:active {
    transform: scale(0.96);
  }
}

.nav-logo-svg {
  width: 26px;
  height: 26px;
}

.nav-logo-image {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.nav-logo-badge {
  position: absolute;
  top: -4px;
  right: -6px;
  background: #FF4D4F;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 0 5px;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
  z-index: 10;
  line-height: 1;
}

// ============================================================================
// 导航项列表
// ============================================================================
.nav-items-scroll {
  flex: 1;
  width: 100%;
  padding: 4px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  overflow-y: auto;
}

// ============================================================================
// 导航项按钮
// ============================================================================
.nav-item {
  position: relative;
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover .nav-icon-bg {
    transform: scale(1.06);
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.25);
  }

  &.is-active {
    .nav-icon-bg {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    }

    .active-bar {
      opacity: 1;
      transform: scaleY(1);
    }
  }
}

// ============================================================================
// 彩色图标背景方块
// ============================================================================
.nav-icon-bg {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
}

.nav-icon {
  font-size: 16px !important;
  min-width: 16px !important;
  height: 16px !important;
  color: #fff;
  line-height: 1;
}

// 未读角标
.nav-badge {
  position: absolute;
  top: 2px;
  right: 0px;
  background: #FF4D4F;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 0 4px;
  min-width: 14px;
  height: 14px;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  line-height: 1;
}

// 活跃状态左侧指示条
.active-bar {
  position: absolute;
  left: -8px;
  width: 3px;
  height: 14px;
  border-radius: 0 3px 3px 0;
  opacity: 0;
  transform: scaleY(0.4);
  transition: all 0.2s ease;
}

// ============================================================================
// 底部区域
// ============================================================================
.nav-footer {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 0 18px;
  flex-shrink: 0;
  border-top: 1px solid transparent;
}

.nav-footer-icon {
  font-size: 20px !important;
  min-width: 20px !important;
  height: 20px !important;
  line-height: 1;
}

// 主题切换按钮动画
.nav-theme-toggle {
  .nav-footer-icon {
    transition: transform 0.3s ease, color 0.2s ease;
  }

  &:hover .nav-footer-icon {
    transform: rotate(30deg);
  }
}

// ============================================================================
// 用户头像按钮
// ============================================================================
.nav-avatar-btn {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: background 0.2s ease;
  margin-top: 4px;
}

.status-dot {
  position: absolute;
  bottom: 3px;
  right: 3px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #858E9E;
  border-width: 2px;
  border-style: solid;
  transition: background 0.3s ease;

  &.online {
    background: #00C853;
  }
}

// ============================================================================
// 响应式 — 小屏幕紧凑模式
// ============================================================================
@media (max-height: 600px) {
  .nav-logo-area {
    padding: 8px 0 4px;
  }

  .nav-footer {
    padding: 6px 0 10px;
  }
}
</style>
