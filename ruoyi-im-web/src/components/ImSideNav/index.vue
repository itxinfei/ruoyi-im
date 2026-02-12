<template>
  <aside
    class="nav-sidebar"
    :style="{ width: navWidth + 'px' }"
  >
    <!-- Logo 区域 -->
    <div class="logo-area">
      <div class="logo-box">
        <span class="logo-text">钉</span>
      </div>
    </div>

    <!-- 主菜单区域 -->
    <nav class="nav-list">
      <el-tooltip
        v-for="item in navModules"
        :key="item.key"
        :content="item.label"
        placement="right"
        :show-after="500"
      >
        <div
          class="nav-item"
          :class="{ active: activeModule === item.key }"
          @click="handleNavClick(item.key)"
        >
          <el-icon class="nav-icon">
            <component :is="item.icon" />
          </el-icon>
          <span class="nav-label">{{ item.label }}</span>
          <span
            v-if="item.key === 'chat' && unreadCount > 0"
            class="nav-dot"
          />
        </div>
      </el-tooltip>
    </nav>

    <!-- 底部功能区域 -->
    <div class="nav-footer">
      <!-- 设置 -->
      <el-tooltip
        content="设置"
        placement="right"
        :show-after="500"
      >
        <div
          class="footer-item"
          :class="{ active: activeModule === 'settings' }"
          @click="handleNavClick('settings')"
        >
          <el-icon class="footer-icon">
            <Setting />
          </el-icon>
        </div>
      </el-tooltip>

      <!-- 主题切换 -->
      <el-tooltip
        content="切换主题"
        placement="right"
        :show-after="500"
      >
        <div
          class="footer-item"
          @click="toggleTheme"
        >
          <el-icon class="footer-icon">
            <component :is="isDarkMode ? Sunny : Moon" />
          </el-icon>
        </div>
      </el-tooltip>

      <!-- 退出登录 -->
      <el-tooltip
        content="退出登录"
        placement="right"
        :show-after="500"
      >
        <div
          class="footer-item logout-item"
          @click="handleLogout"
        >
          <el-icon class="footer-icon">
            <SwitchButton />
          </el-icon>
        </div>
      </el-tooltip>

      <!-- 用户头像 -->
      <div
        class="user-avatar"
        :class="{ active: activeModule === 'profile' }"
        @click="handleNavClick('profile')"
      >
        <DingtalkAvatar
          :src="currentUser.avatar"
          :name="currentUser.nickname || currentUser.username || '我'"
          :user-id="currentUser.id"
          :size="36"
          shape="circle"
          custom-class="nav-avatar"
        />
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import {
  ChatDotRound,
  User,
  Grid,
  FolderOpened,
  Calendar,
  Clock,
  Document,
  Setting,
  ChatLineSquare,
  MagicStick,
  Moon,
  Sunny,
  SwitchButton
} from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  activeModule: {
    type: String,
    default: 'chat'
  }
})

const emit = defineEmits(['switchModule', 'logout'])
const store = useStore()
const navWidth = ref(64)
const unreadCount = computed(() => store.state.im.totalUnreadCount)
const currentUser = computed(() => store.getters['user/currentUser'])

// 主题模式
const isDarkMode = computed(() => store.getters['settings/isDarkMode'])

// 切换主题
const toggleTheme = () => {
  store.dispatch('settings/toggleDarkMode')
}

// 退出登录
const handleLogout = () => {
  emit('logout')
}

const navModules = ref([
  {
    key: 'chat',
    label: '消息',
    icon: ChatDotRound
  },
  {
    key: 'contacts',
    label: '联系人',
    icon: User
  },
  {
    key: 'workbench',
    label: '工作台',
    icon: Grid
  },
  {
    key: 'drive',
    label: '云盘',
    icon: FolderOpened
  },
  {
    key: 'calendar',
    label: '日历',
    icon: Calendar
  },
  {
    key: 'todo',
    label: '待办',
    icon: Clock
  },
  {
    key: 'approval',
    label: '审批',
    icon: Document
  },
  {
    key: 'mail',
    label: '邮箱',
    icon: ChatLineSquare
  },
  {
    key: 'assistant',
    label: 'AI助理',
    icon: MagicStick
  }
])

const handleNavClick = moduleKey => {
  emit('switchModule', moduleKey)
}
</script>

<style scoped>
.nav-sidebar {
  display: flex;
  flex-direction: column;
  width: 64px;
  min-width: 64px;
  max-width: 64px;
  flex-shrink: 0;
  background: var(--dt-brand-color);
  height: 100vh;
  position: relative;
}

.logo-area {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 0 12px 0;
  flex-shrink: 0;
}

.logo-box {
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.logo-text {
  color: var(--dt-brand-color);
  font-size: 20px;
  font-weight: 700;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.nav-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 0;
  gap: 4px;
  flex: 1;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
  
  &::-webkit-scrollbar {
    display: none;
  }
}

.nav-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  margin: 0 auto;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }

  &.active {
    background-color: rgba(255, 255, 255, 0.2);
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 20px;
      background-color: #fff;
      border-radius: 0 2px 2px 0;
      box-shadow: 0 0 8px rgba(255, 255, 255, 0.5);
    }
  }
}

.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.2s ease;
  line-height: 1;
}

.nav-label {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1;
  font-weight: 500;
  letter-spacing: 0.2px;
  margin-top: 4px;
}

.nav-item:hover .nav-icon,
.nav-item:hover .nav-label {
  color: rgba(255, 255, 255, 0.9);
}

.nav-item.active .nav-icon,
.nav-item.active .nav-label {
  color: #fff;
  font-weight: 600;
}

.nav-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
  border: 2px solid var(--dt-brand-color);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.nav-footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0 16px 0;
  gap: 8px;
  flex-shrink: 0;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin-top: auto;
  background: transparent;
}

.footer-item {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  background-color: transparent;

  &:hover {
    background-color: rgba(255, 255, 255, 0.15);
  }

  &.active {
    background-color: rgba(255, 255, 255, 0.2);
  }

  &.logout-item:hover {
    background-color: rgba(255, 77, 79, 0.2);

    .footer-icon {
      color: #ff4d4f;
    }
  }
}

.footer-icon {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.2s ease;
}

.footer-item:hover .footer-icon {
  color: rgba(255, 255, 255, 0.95);
}

.footer-item.active .footer-icon {
  color: #fff;
}

.user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 4px;
  background-color: rgba(255, 255, 255, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;

  &:hover {
    background-color: rgba(255, 255, 255, 0.25);
    border-color: rgba(255, 255, 255, 0.5);
  }

  &.active {
    background-color: rgba(255, 255, 255, 0.3);
    border-color: #fff;
    box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.3);
  }
}

/* 导航头像组件样式覆盖 */
.user-avatar :deep(.nav-avatar) {
  width: 100%;
  height: 100%;
}

.user-avatar :deep(.nav-avatar .avatar-text) {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.user-avatar.active :deep(.nav-avatar .avatar-text) {
  color: #1677ff;
}
</style>
