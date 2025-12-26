<template>
  <div class="im-layout">
    <!-- 左侧图标导航栏 -->
    <div class="nav-rail">
      <!-- Logo -->
      <div class="nav-logo">
        <div class="logo-avatar">
          {{ currentUser.name ? currentUser.name.charAt(0) : 'U' }}
        </div>
      </div>

      <!-- 导航菜单 -->
      <div class="nav-menu">
        <el-tooltip
          v-for="item in menuItems"
          :key="item.path"
          :content="item.label"
          placement="right"
          :show-after="300"
        >
          <div
            class="nav-item"
            :class="{ active: isActiveMenu(item.path) }"
            @click="handleMenuClick(item.path)"
          >
            <el-badge
              v-if="item.badge"
              :value="item.badge"
              :max="99"
              class="nav-badge"
            >
              <i :class="item.icon"></i>
            </el-badge>
            <i v-else :class="item.icon"></i>
          </div>
        </el-tooltip>
      </div>

      <!-- 底部用户区域 -->
      <div class="nav-footer">
        <el-dropdown trigger="click" placement="right-end" @command="handleUserCommand">
          <div class="user-avatar">
            <el-avatar :size="36" :src="currentUser.avatar">
              {{ currentUser.name ? currentUser.name.charAt(0) : 'U' }}
            </el-avatar>
            <span class="online-dot"></span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>
                <span class="user-name">{{ currentUser.name }}</span>
              </el-dropdown-item>
              <el-dropdown-item divided command="settings">
                <i class="el-icon-setting"></i> 设置
              </el-dropdown-item>
              <el-dropdown-item command="logout">
                <i class="el-icon-switch-button"></i> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-container">
      <app-main />
    </div>

    <!-- 移动端遮罩 -->
    <div v-if="isMobileMenuOpen" class="mobile-overlay" @click="closeMobileMenu"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessageBox, ElMessage } from 'element-plus'
import AppMain from './components/AppMain.vue'

const router = useRouter()
const store = useStore()

// 当前用户信息
const currentUser = computed(() => ({
  name: store.state.user?.name || store.state.user?.info?.userName || '用户',
  avatar: store.state.user?.avatar || store.state.user?.info?.avatar || '',
}))

// 移动端状态
const isMobile = ref(false)
const isMobileMenuOpen = ref(false)

// 未读消息数
const unreadCount = computed(() => store.getters.unreadTotal || 0)

// 导航菜单配置
const menuItems = computed(() => [
  {
    path: '/chat',
    icon: 'el-icon-chat-dot-round',
    label: '消息',
    badge: unreadCount.value > 0 ? unreadCount.value : null,
  },
  {
    path: '/contacts',
    icon: 'el-icon-user',
    label: '通讯录',
    badge: null,
  },
  {
    path: '/group',
    icon: 'el-icon-office-building',
    label: '群组',
    badge: null,
  },
  {
    path: '/files',
    icon: 'el-icon-folder',
    label: '文件',
    badge: null,
  },
  {
    path: '/settings',
    icon: 'el-icon-setting',
    label: '设置',
    badge: null,
  },
])

// 判断当前激活菜单
const isActiveMenu = (path) => {
  const currentPath = router.currentRoute.value.path
  // 处理路径匹配，支持子路径
  if (currentPath === path) return true
  if (currentPath.startsWith(path + '/')) return true
  // 处理 meta.activeMenu
  const meta = router.currentRoute.value.meta
  if (meta?.activeMenu === path) return true
  return false
}

// 菜单点击
const handleMenuClick = (path) => {
  if (router.currentRoute.value.path !== path) {
    router.push(path)
  }
  if (isMobile.value) {
    isMobileMenuOpen.value = false
  }
}

// 用户菜单命令
const handleUserCommand = (command) => {
  switch (command) {
    case 'settings':
      router.push('/settings')
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

// 关闭移动端菜单
const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
}

// 响应式处理
const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.im-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

// 左侧图标导航栏
.nav-rail {
  width: $nav-rail-width;
  min-width: $nav-rail-width;
  height: 100%;
  background-color: $nav-rail-bg;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-right: 1px solid $nav-rail-border;
  z-index: 100;

  // Logo区域
  .nav-logo {
    width: 100%;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid $nav-rail-border;

    .logo-avatar {
      width: 36px;
      height: 36px;
      border-radius: 8px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 18px;
      font-weight: 600;
    }
  }

  // 导航菜单
  .nav-menu {
    flex: 1;
    width: 100%;
    padding: 12px 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    overflow-y: auto;

    &::-webkit-scrollbar {
      display: none;
    }

    .nav-item {
      width: 44px;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.2s ease;
      color: $nav-rail-text;
      position: relative;

      i {
        font-size: 22px;
      }

      &:hover {
        background-color: $nav-rail-hover;
        color: $nav-rail-text-active;
      }

      &.active {
        background-color: $nav-rail-active-bg;
        color: $nav-rail-text-active;
      }

      .nav-badge {
        :deep(.el-badge__content) {
          top: -2px;
          right: 2px;
        }
      }
    }
  }

  // 底部用户区域
  .nav-footer {
    width: 100%;
    padding: 16px 0;
    display: flex;
    justify-content: center;
    border-top: 1px solid $nav-rail-border;

    .user-avatar {
      position: relative;
      cursor: pointer;
      transition: transform 0.2s;

      &:hover {
        transform: scale(1.05);
      }

      .online-dot {
        position: absolute;
        bottom: 2px;
        right: 2px;
        width: 10px;
        height: 10px;
        background-color: #52c41a;
        border: 2px solid $nav-rail-bg;
        border-radius: 50%;
      }
    }

    .user-name {
      font-weight: 500;
      color: $text-primary;
    }
  }
}

// 主内容区
.main-container {
  flex: 1;
  height: 100%;
  overflow: hidden;
  background-color: $background-color-base;
}

// 移动端遮罩
.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 99;
}

// 移动端适配
@media screen and (max-width: 768px) {
  .nav-rail {
    position: fixed;
    left: 0;
    top: 0;
    transform: translateX(-100%);
    transition: transform 0.3s ease;

    &.open {
      transform: translateX(0);
    }
  }

  .main-container {
    width: 100%;
  }
}
</style>
