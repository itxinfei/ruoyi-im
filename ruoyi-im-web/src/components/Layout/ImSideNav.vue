<template>
  <div class="im-side-nav" :class="{ collapsed: isCollapsed }">
    <!-- 顶部用户区域 -->
    <div class="nav-header">
      <div class="user-avatar" @click="showUserPanel">
        <el-avatar :size="40" :src="userInfo.avatar" class="avatar">
          {{ userInfo.nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="user-status" :class="onlineStatus"></div>
      </div>

      <!-- 折叠按钮 -->
      <el-button
        :icon="isCollapsed ? ArrowRight : ArrowLeft"
        circle
        size="small"
        text
        class="collapse-btn"
        @click="toggleCollapse"
      />
    </div>

    <!-- 主导航菜单 -->
    <div class="nav-menu">
      <div
        v-for="item in mainMenus"
        :key="item.key"
        class="nav-item"
        :class="{ active: activeMenu === item.key }"
        :title="item.label"
        @click="handleMenuClick(item)"
      >
        <el-badge :value="item.badge" :hidden="!item.badge" :max="99" class="nav-badge">
          <div class="nav-icon">
            <component :is="item.icon" />
          </div>
        </el-badge>
        <transition name="label-fade">
          <span v-if="!isCollapsed" class="nav-label">{{ item.label }}</span>
        </transition>
      </div>
    </div>

    <!-- 底部菜单 -->
    <div class="nav-bottom">
      <div
        v-for="item in bottomMenus"
        :key="item.key"
        class="nav-item"
        :class="{ active: activeMenu === item.key }"
        :title="item.label"
        @click="handleMenuClick(item)"
      >
        <div class="nav-icon">
          <component :is="item.icon" />
        </div>
        <transition name="label-fade">
          <span v-if="!isCollapsed" class="nav-label">{{ item.label }}</span>
        </transition>
      </div>
    </div>

    <!-- 用户面板弹窗 -->
    <el-popover
      v-model:visible="userPanelVisible"
      placement="right-start"
      :width="280"
      trigger="manual"
      popper-class="user-panel-popover"
    >
      <template #reference>
        <div></div>
      </template>
      <div class="user-panel">
        <div class="user-info">
          <el-avatar :size="56" :src="userInfo.avatar">
            {{ userInfo.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="info-text">
            <h3>{{ userInfo.nickname || '用户' }}</h3>
            <p>{{ userInfo.username }}</p>
          </div>
        </div>
        <div class="status-selector">
          <div
            v-for="status in statusOptions"
            :key="status.value"
            class="status-option"
            :class="{ active: onlineStatus === status.value }"
            @click="changeStatus(status.value)"
          >
            <span class="status-dot" :class="status.value"></span>
            <span>{{ status.label }}</span>
          </div>
        </div>
        <div class="user-actions">
          <div class="action-item" @click="goToProfile">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </div>
          <div class="action-item" @click="goToSettings">
            <el-icon><Setting /></el-icon>
            <span>设置</span>
          </div>
          <div class="action-item danger" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </div>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentUserInfo } from '@/utils/im-user'
import {
  ChatDotRound,
  User,
  Folder,
  Setting,
  Grid,
  ArrowLeft,
  ArrowRight,
} from '@element-plus/icons-vue'

const router = useRouter()
const store = useStore()

// 状态
const activeMenu = ref('message')
const userPanelVisible = ref(false)
const onlineStatus = ref('online')
const isCollapsed = ref(false)

// 用户信息 - 使用统一的用户信息获取函数
const userInfo = computed(() => {
  return getCurrentUserInfo() || {}
})

// 未读消息数
const unreadCount = computed(() => store.getters['im/totalUnreadCount'] || 0)

// 主导航菜单
const mainMenus = computed(() => [
  {
    key: 'message',
    icon: ChatDotRound,
    label: '消息',
    badge: unreadCount.value,
    route: '/im/chat',
  },
  { key: 'contacts', icon: User, label: '通讯录', badge: 0, route: '/im/contacts' },
  { key: 'group', icon: Grid, label: '群组', badge: 0, route: '/im/group' },
  { key: 'files', icon: Folder, label: '文件', badge: 0, route: '/im/file' },
])

// 底部菜单
const bottomMenus = [{ key: 'settings', icon: Setting, label: '设置', route: '/im/settings' }]

// 状态选项
const statusOptions = [
  { value: 'online', label: '在线' },
  { value: 'busy', label: '忙碌' },
  { value: 'away', label: '离开' },
  { value: 'offline', label: '隐身' },
]

// 显示用户面板
const showUserPanel = () => {
  userPanelVisible.value = !userPanelVisible.value
}

// 菜单点击
const handleMenuClick = item => {
  activeMenu.value = item.key
  if (item.route) {
    router.push(item.route)
  }
  userPanelVisible.value = false
}

// 修改状态
const changeStatus = status => {
  onlineStatus.value = status
  store.dispatch('im/updateOnlineStatus', { userId: userInfo.value.userId, status })
  userPanelVisible.value = false
}

// 跳转个人资料
const goToProfile = () => {
  router.push('/im/user')
  userPanelVisible.value = false
}

// 跳转设置
const goToSettings = () => {
  router.push('/im/settings')
  userPanelVisible.value = false
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
    ElMessage.success('退出成功')
  } catch (e) {
    // 取消
  }
  userPanelVisible.value = false
}

// 折叠/展开
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
  store.commit('app/SET_SIDEBAR_COLLAPSED', isCollapsed.value)
}

// 根据当前路由设置激活菜单
onMounted(() => {
  const path = router.currentRoute.value.path
  const menu = [...mainMenus.value, ...bottomMenus].find(m => path.startsWith(m.route))
  if (menu) {
    activeMenu.value = menu.key
  }

  // 从store恢复折叠状态
  isCollapsed.value = store.state.app?.sidebarCollapsed || false
})
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.im-side-nav {
  width: $nav-rail-width;
  height: 100%;
  background: linear-gradient(180deg, $nav-rail-bg 0%, $nav-rail-bg-dark 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-md 0;
  user-select: none;
  transition: width $transition-base $ease-base;
  position: relative;
  z-index: $z-index-fixed;

  &.collapsed {
    width: $nav-rail-width;
  }

  &:not(.collapsed) {
    width: 280px;
    align-items: stretch;
    padding: $spacing-md;
  }
}

.nav-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-xl;
  padding: 0 $spacing-sm;
  gap: $spacing-sm;

  .user-avatar {
    position: relative;
    cursor: pointer;
    transition: transform $transition-fast $ease-base;
    flex-shrink: 0;

    &:hover {
      transform: scale(1.05);
    }

    .avatar {
      border: 2px solid rgba(255, 255, 255, 0.3);
      transition: border-color $transition-fast $ease-base;

      &:hover {
        border-color: rgba(255, 255, 255, 0.6);
      }
    }

    .user-status {
      position: absolute;
      bottom: 2px;
      right: 2px;
      width: 12px;
      height: 12px;
      border-radius: $border-radius-round;
      border: 2px solid $nav-rail-bg;
      transition: all $transition-fast $ease-base;

      &.online {
        background-color: $success-color;
        box-shadow: 0 0 6px rgba($success-color, 0.5);
      }
      &.busy {
        background-color: $error-color;
        box-shadow: 0 0 6px rgba($error-color, 0.5);
      }
      &.away {
        background-color: $warning-color;
        box-shadow: 0 0 6px rgba($warning-color, 0.5);
      }
      &.offline {
        background-color: $text-disabled;
        box-shadow: 0 0 6px rgba($text-disabled, 0.5);
      }
    }
  }

  .collapse-btn {
    color: rgba(255, 255, 255, 0.7);
    transition: all $transition-fast $ease-base;

    &:hover {
      color: #fff;
      background-color: rgba(255, 255, 255, 0.1);
    }
  }
}

.nav-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  width: 100%;
  padding: $spacing-sm 0;
}

.nav-bottom {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  width: 100%;
  margin-top: auto;
  padding: $spacing-sm 0;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md $spacing-sm;
  cursor: pointer;
  color: $nav-rail-text;
  transition: all $transition-fast $ease-base;
  position: relative;
  border-radius: $border-radius-sm;
  white-space: nowrap;
  margin: 0 $spacing-xs;

  .nav-icon {
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    flex-shrink: 0;
    transition: all $transition-fast $ease-base;
  }

  .nav-label {
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &:hover {
    color: $nav-rail-text-hover;
    background-color: $nav-rail-hover-bg;
  }

  &.active {
    color: $nav-rail-text-active;
    background-color: $nav-rail-icon-active-bg;

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

  :deep(.el-badge__content) {
    border: none;
    top: -2px;
    right: 0;
    background: linear-gradient(135deg, $error-color 0%, #ff7875 100%);
    font-weight: $font-weight-semibold;
    min-width: 18px;
    height: 18px;
    line-height: 18px;
    padding: 0 4px;
    box-shadow: 0 2px 6px rgba($error-color, 0.3);
  }
}

// 用户面板
.user-panel {
  .user-info {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    padding-bottom: $spacing-lg;
    border-bottom: 1px solid $border-light;
    margin-bottom: $spacing-lg;

    .info-text {
      h3 {
        margin: 0 0 $spacing-xs 0;
        font-size: $font-size-lg;
        font-weight: $font-weight-semibold;
        color: $text-primary;
      }
      p {
        margin: 0;
        font-size: $font-size-sm;
        color: $text-secondary;
      }
    }
  }

  .status-selector {
    display: flex;
    gap: $spacing-sm;
    margin-bottom: $spacing-lg;
    padding-bottom: $spacing-md;
    border-bottom: 1px solid $border-light;

    .status-option {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: $spacing-xs;
      padding: $spacing-sm $spacing-md;
      border-radius: $border-radius-sm;
      cursor: pointer;
      font-size: $font-size-sm;
      transition: all $transition-fast $ease-base;
      border: 1px solid transparent;

      &:hover {
        background-color: $bg-hover;
      }

      &.active {
        background-color: $primary-color-lighter;
        color: $primary-color;
        border-color: $primary-color;
      }

      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: $border-radius-round;
        flex-shrink: 0;

        &.online {
          background-color: $success-color;
        }
        &.busy {
          background-color: $error-color;
        }
        &.away {
          background-color: $warning-color;
        }
        &.offline {
          background-color: $text-disabled;
        }
      }
    }
  }

  .user-actions {
    .action-item {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      padding: $spacing-md $spacing-sm;
      cursor: pointer;
      border-radius: $border-radius-sm;
      transition: all $transition-fast $ease-base;
      color: $text-regular;

      &:hover {
        background-color: $bg-hover;
        color: $primary-color;
      }

      &.danger {
        color: $error-color;

        &:hover {
          background-color: #fef0f0;
        }
      }
    }
  }
}

// 过渡动画
.label-fade-enter-active,
.label-fade-leave-active {
  transition: all 0.3s ease;
}

.label-fade-enter-from,
.label-fade-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

// 响应式
@media (max-width: 1200px) {
  .im-side-nav:not(.collapsed) {
    width: 240px;
  }
}

@media (max-width: 768px) {
  .im-side-nav {
    width: $nav-rail-width;

    &:not(.collapsed) {
      width: $nav-rail-width;
    }

    .nav-header {
      .collapse-btn {
        display: none;
      }
    }

    .nav-item {
      .nav-label {
        display: none;
      }
    }
  }
}
</style>

<style>
.user-panel-popover {
  margin-left: 8px !important;
}
</style>
