<template>
  <div
    class="dingtalk-layout"
    :class="{ mobile: isMobile, 'sidebar-collapsed': isSidebarCollapsed }"
  >
    <!-- 最左侧图标导航栏 -->
    <aside class="nav-rail">
      <!-- Logo -->
      <div class="nav-rail-logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28">
            <path
              d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
            />
          </svg>
        </div>
      </div>

      <!-- 主导航菜单 -->
      <nav class="nav-rail-menu">
        <div
          v-for="item in mainMenuItems"
          :key="item.path"
          class="nav-rail-item"
          :class="{ active: isActiveMenu(item.path) }"
          @click="handleMenuClick(item)"
        >
          <el-tooltip :content="item.label" placement="right" :show-after="500">
            <div class="nav-item-inner">
              <el-badge v-if="item.badge" :value="item.badge" :max="99" class="nav-badge">
                <component :is="item.icon" class="nav-icon" />
              </el-badge>
              <component :is="item.icon" v-else class="nav-icon" />
            </div>
          </el-tooltip>
        </div>
      </nav>

      <!-- 底部工具栏 -->
      <div class="nav-rail-footer">
        <!-- 用户头像 -->
        <el-dropdown trigger="click" placement="right-end" @command="handleUserCommand">
          <div class="nav-rail-avatar">
            <el-avatar :size="36" :src="currentUser.avatar">
              {{ currentUser.name?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="status-dot" :class="{ online: wsConnected }"></span>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="user-dropdown">
              <div class="user-dropdown-header">
                <el-avatar :size="48" :src="currentUser.avatar">
                  {{ currentUser.name?.charAt(0) || 'U' }}
                </el-avatar>
                <div class="user-info">
                  <div class="user-name">{{ currentUser.name }}</div>
                  <div class="user-status" :class="{ online: wsConnected }">
                    {{ wsConnected ? '在线' : '离线' }}
                  </div>
                </div>
              </div>
              <el-dropdown-item command="profile" divided>
                <User class="dropdown-icon" />
                <span>个人资料</span>
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <Setting class="dropdown-icon" />
                <span>设置</span>
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <SwitchButton class="dropdown-icon" />
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 移动端底部导航 -->
    <nav v-if="isMobile" class="mobile-nav">
      <div
        v-for="item in mobileMenuItems"
        :key="item.path"
        class="mobile-nav-item"
        :class="{ active: isActiveMenu(item.path) }"
        @click="handleMenuClick(item)"
      >
        <el-badge v-if="item.badge" :value="item.badge" :max="99" :offset="[-2, 2]">
          <component :is="item.icon" class="mobile-nav-icon" />
        </el-badge>
        <component :is="item.icon" v-else class="mobile-nav-icon" />
        <span class="mobile-nav-label">{{ item.label }}</span>
      </div>
    </nav>

    <!-- 移动端遮罩 -->
    <transition name="fade">
      <div
        v-if="isMobile && isMobileSidebarOpen"
        class="mobile-overlay"
        @click="closeMobileSidebar"
      ></div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, provide } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  ChatDotRound,
  User,
  OfficeBuilding,
  Folder,
  Setting,
  SwitchButton,
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const store = useStore()

// 响应式状态
const isMobile = ref(false)
const isSidebarCollapsed = ref(false)
const isMobileSidebarOpen = ref(false)

// 提供给子组件
provide('isMobile', isMobile)
provide('isSidebarCollapsed', isSidebarCollapsed)
provide('toggleSidebar', () => {
  if (isMobile.value) {
    isMobileSidebarOpen.value = !isMobileSidebarOpen.value
  } else {
    isSidebarCollapsed.value = !isSidebarCollapsed.value
  }
})

// 当前用户信息
const currentUser = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return {
    name: userInfo.nickName || userInfo.userName || store.state.user?.name || '用户',
    avatar: userInfo.avatar || store.state.user?.avatar || '',
  }
})

// WebSocket连接状态
const wsConnected = computed(() => store.state.im?.wsConnected ?? false)

// 未读消息数
const unreadCount = computed(() => {
  const sessions = store.state.im?.sessions || []
  return sessions.reduce((total, session) => total + (session.unreadCount || 0), 0)
})

// 主菜单配置
const mainMenuItems = computed(() => [
  {
    path: '/im/chat',
    icon: ChatDotRound,
    label: '消息',
    badge: unreadCount.value > 0 ? unreadCount.value : null,
  },
  {
    path: '/im/contacts',
    icon: User,
    label: '通讯录',
    badge: null,
  },
  {
    path: '/im/group',
    icon: OfficeBuilding,
    label: '群组',
    badge: null,
  },
  {
    path: '/im/file/list',
    icon: Folder,
    label: '文件',
    badge: null,
  },
])

// 移动端菜单（底部导航）
const mobileMenuItems = computed(() => [
  {
    path: '/im/chat',
    icon: ChatDotRound,
    label: '消息',
    badge: unreadCount.value > 0 ? unreadCount.value : null,
  },
  {
    path: '/im/contacts',
    icon: User,
    label: '联系人',
    badge: null,
  },
  {
    path: '/im/group',
    icon: OfficeBuilding,
    label: '群组',
    badge: null,
  },
  {
    path: '/im/settings',
    icon: Setting,
    label: '我的',
    badge: null,
  },
])

// 判断当前激活菜单
const isActiveMenu = path => {
  const currentPath = route.path
  if (currentPath === path) return true
  if (currentPath.startsWith(path + '/')) return true
  if (route.meta?.activeMenu === path) return true
  return false
}

// 菜单点击
const handleMenuClick = item => {
  if (route.path !== item.path) {
    router.push(item.path)
  }
  if (isMobile.value) {
    isMobileSidebarOpen.value = false
  }
}

// 用户菜单命令
const handleUserCommand = command => {
  switch (command) {
    case 'profile':
      router.push('/im/settings')
      break
    case 'settings':
      router.push('/im/settings')
      break
    case 'logout':
      confirmLogout()
      break
  }
}

// 确认退出登录
const confirmLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      store.dispatch('user/logout').then(() => {
        router.push('/login')
        ElMessage.success('退出成功')
      })
    })
    .catch(() => {})
}

// 关闭移动端侧边栏
const closeMobileSidebar = () => {
  isMobileSidebarOpen.value = false
}

// 响应式处理
const handleResize = () => {
  const width = window.innerWidth
  isMobile.value = width < 768

  if (width < 768) {
    isSidebarCollapsed.value = true
  } else if (width < 1200) {
    isSidebarCollapsed.value = false
  } else {
    isSidebarCollapsed.value = false
  }
}

// 键盘快捷键
const handleKeydown = e => {
  // Ctrl/Cmd + B 切换侧边栏
  if ((e.ctrlKey || e.metaKey) && e.key === 'b') {
    e.preventDefault()
    isSidebarCollapsed.value = !isSidebarCollapsed.value
  }
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.dingtalk-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: $bg-base;
}

// ==================== 左侧图标导航栏 ====================
.nav-rail {
  width: $nav-rail-width;
  min-width: $nav-rail-width;
  height: 100%;
  background: linear-gradient(180deg, $nav-rail-bg 0%, $nav-rail-bg-dark 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: $z-index-fixed;
  position: relative;

  // Logo区域
  .nav-rail-logo {
    width: 100%;
    height: 64px;
    @include flex-center;
    border-bottom: 1px solid $nav-rail-border;

    .logo-icon {
      width: 40px;
      height: 40px;
      @include flex-center;
      background: linear-gradient(135deg, $primary-color 0%, #40a9ff 100%);
      border-radius: $border-radius-lg;
      color: #fff;
      transition: transform $transition-base $ease-out;

      &:hover {
        transform: scale(1.05);
      }
    }
  }

  // 导航菜单
  .nav-rail-menu {
    flex: 1;
    width: 100%;
    padding: $spacing-md 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-xs;
    overflow-y: auto;
    @include custom-scrollbar(4px, rgba(255, 255, 255, 0.2));
  }

  .nav-rail-item {
    width: 48px;
    height: 48px;
    @include flex-center;
    border-radius: $border-radius-lg;
    cursor: pointer;
    transition: all $transition-base $ease-out;
    position: relative;

    .nav-item-inner {
      @include flex-center;
      width: 100%;
      height: 100%;
    }

    .nav-icon {
      width: 24px;
      height: 24px;
      color: $nav-rail-text;
      transition: color $transition-base $ease-base;
    }

    .nav-badge {
      :deep(.el-badge__content) {
        top: -4px;
        right: -4px;
        border: none;
        background: linear-gradient(135deg, $error-color 0%, #ff7875 100%);
        box-shadow: 0 2px 8px rgba($error-color, 0.4);
      }
    }

    &:hover {
      background-color: $nav-rail-hover-bg;

      .nav-icon {
        color: $nav-rail-text-hover;
      }
    }

    &.active {
      background-color: $nav-rail-icon-active-bg;
      box-shadow: 0 4px 12px rgba($primary-color, 0.4);

      .nav-icon {
        color: $nav-rail-text-active;
      }

      // 左侧指示条
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 24px;
        background: #fff;
        border-radius: 0 2px 2px 0;
      }
    }

    @include click-scale(0.95);
  }

  // 底部区域
  .nav-rail-footer {
    width: 100%;
    padding: $spacing-lg 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-sm;
    border-top: 1px solid $nav-rail-border;

    .nav-rail-avatar {
      position: relative;
      cursor: pointer;
      padding: $spacing-sm;
      border-radius: $border-radius-lg;
      transition: background-color $transition-base $ease-base;

      &:hover {
        background-color: $nav-rail-hover-bg;
      }

      .status-dot {
        position: absolute;
        bottom: 8px;
        right: 8px;
        width: 10px;
        height: 10px;
        background-color: $text-disabled;
        border: 2px solid $nav-rail-bg;
        border-radius: $border-radius-round;
        transition: background-color $transition-base $ease-base;

        &.online {
          background-color: $success-color;
          animation: online-pulse 2s infinite;
        }
      }

      :deep(.el-avatar) {
        border: 2px solid rgba(255, 255, 255, 0.2);
        transition: border-color $transition-base $ease-base;
      }

      &:hover :deep(.el-avatar) {
        border-color: rgba(255, 255, 255, 0.4);
      }
    }
  }
}

// ==================== 主内容区 ====================
.main-content {
  flex: 1;
  height: 100%;
  overflow: hidden;
  background-color: $bg-white;
  position: relative;
}

// ==================== 用户下拉菜单 ====================
.user-dropdown {
  .user-dropdown-header {
    display: flex;
    align-items: center;
    padding: $spacing-lg;
    border-bottom: 1px solid $border-light;
    gap: $spacing-md;

    .user-info {
      .user-name {
        font-size: 15px;
        font-weight: 600;
        color: $text-primary;
        margin-bottom: $spacing-xs;
      }

      .user-status {
        font-size: 12px;
        color: $text-tertiary;
        display: flex;
        align-items: center;
        gap: $spacing-xs;

        &::before {
          content: '';
          width: 6px;
          height: 6px;
          background-color: $text-disabled;
          border-radius: $border-radius-round;
        }

        &.online {
          color: $success-color;

          &::before {
            background-color: $success-color;
          }
        }
      }
    }
  }

  .dropdown-icon {
    width: 16px;
    height: 16px;
    margin-right: $spacing-sm;
  }
}

// ==================== 移动端底部导航 ====================
.mobile-nav {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: $header-height-mobile;
  background-color: $bg-white;
  border-top: 1px solid $border-light;
  z-index: $z-index-fixed;
  padding-bottom: env(safe-area-inset-bottom);

  .mobile-nav-item {
    flex: 1;
    @include flex-center;
    flex-direction: column;
    gap: 2px;
    cursor: pointer;
    transition: all $transition-fast $ease-base;

    .mobile-nav-icon {
      width: 22px;
      height: 22px;
      color: $text-tertiary;
      transition: color $transition-fast $ease-base;
    }

    .mobile-nav-label {
      font-size: 10px;
      color: $text-tertiary;
      transition: color $transition-fast $ease-base;
    }

    &.active {
      .mobile-nav-icon {
        color: $primary-color;
      }

      .mobile-nav-label {
        color: $primary-color;
        font-weight: 500;
      }
    }
  }
}

// ==================== 移动端遮罩 ====================
.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: $bg-mask;
  z-index: $z-index-modal-backdrop;
}

// ==================== 页面切换动画 ====================
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity $transition-base $ease-base;
}

.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity $transition-base $ease-base;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// ==================== 响应式适配 ====================
@include respond-to(md) {
  .dingtalk-layout {
    &.mobile {
      .nav-rail {
        display: none;
      }

      .main-content {
        padding-bottom: $header-height-mobile;
      }

      .mobile-nav {
        display: flex;
      }
    }
  }
}

// 平板适配
@media screen and (min-width: 768px) and (max-width: 1024px) {
  .dingtalk-layout {
    .nav-rail {
      width: 56px;
      min-width: 56px;

      .nav-rail-item {
        width: 42px;
        height: 42px;

        .nav-icon {
          width: 20px;
          height: 20px;
        }
      }

      .nav-rail-avatar {
        :deep(.el-avatar) {
          width: 32px !important;
          height: 32px !important;
        }
      }
    }
  }
}

// 减少动画偏好
@media (prefers-reduced-motion: reduce) {
  .dingtalk-layout {
    .nav-rail-item {
      transition: none;
    }

    .status-dot.online {
      animation: none;
    }
  }

  .page-fade-enter-active,
  .page-fade-leave-active,
  .fade-enter-active,
  .fade-leave-active {
    transition: none;
  }
}
</style>
