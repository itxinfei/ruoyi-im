<template>
  <div class="side-nav">
    <!-- 用户头像 -->
    <div class="nav-user" @click="showUserPanel">
      <el-avatar :size="40" :src="userInfo.avatar">
        {{ userInfo.nickname?.charAt(0) || 'U' }}
      </el-avatar>
      <div class="user-status" :class="onlineStatus"></div>
    </div>

    <!-- 主导航菜单 -->
    <div class="nav-menu">
      <div
        v-for="item in mainMenus"
        :key="item.key"
        class="nav-item"
        :class="{ active: activeMenu === item.key }"
        @click="handleMenuClick(item)"
      >
        <el-badge :value="item.badge" :hidden="!item.badge" :max="99">
          <div class="nav-icon">
            <component :is="item.icon" />
          </div>
        </el-badge>
        <span class="nav-label">{{ item.label }}</span>
      </div>
    </div>

    <!-- 底部菜单 -->
    <div class="nav-bottom">
      <div
        v-for="item in bottomMenus"
        :key="item.key"
        class="nav-item"
        :class="{ active: activeMenu === item.key }"
        @click="handleMenuClick(item)"
      >
        <div class="nav-icon">
          <component :is="item.icon" />
        </div>
        <span class="nav-label">{{ item.label }}</span>
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
import {
  ChatDotRound,
  User,
  Folder,
  Calendar,
  Setting,
  SwitchButton,
  Bell,
  Grid,
} from '@element-plus/icons-vue'

const router = useRouter()
const store = useStore()

// 状态
const activeMenu = ref('message')
const userPanelVisible = ref(false)
const onlineStatus = ref('online')

// 用户信息
const userInfo = computed(() => {
  const info = localStorage.getItem('userInfo')
  return info ? JSON.parse(info) : {}
})

// 未读消息数
const unreadCount = computed(() => store.getters['im/totalUnreadCount'] || 0)

// 主导航菜单
const mainMenus = computed(() => [
  { key: 'message', icon: ChatDotRound, label: '消息', badge: unreadCount.value, route: '/im/chat' },
  { key: 'contacts', icon: User, label: '通讯录', badge: 0, route: '/im/contacts' },
  { key: 'workspace', icon: Grid, label: '工作台', badge: 0, route: '/im/workspace' },
  { key: 'calendar', icon: Calendar, label: '日历', badge: 0, route: '/im/calendar' },
  { key: 'files', icon: Folder, label: '云文档', badge: 0, route: '/im/file' },
])

// 底部菜单
const bottomMenus = [
  { key: 'notification', icon: Bell, label: '通知', route: '/im/notification' },
  { key: 'settings', icon: Setting, label: '设置', route: '/im/settings' },
]

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
const handleMenuClick = (item) => {
  activeMenu.value = item.key
  if (item.route) {
    router.push(item.route)
  }
  userPanelVisible.value = false
}

// 修改状态
const changeStatus = (status) => {
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

// 根据当前路由设置激活菜单
onMounted(() => {
  const path = router.currentRoute.value.path
  const menu = [...mainMenus.value, ...bottomMenus].find(m => path.startsWith(m.route))
  if (menu) {
    activeMenu.value = menu.key
  }
})
</script>

<style lang="scss" scoped>
.side-nav {
  width: 64px;
  height: 100%;
  background: linear-gradient(180deg, #1e3a5f 0%, #0d2137 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
  user-select: none;
}

.nav-user {
  position: relative;
  cursor: pointer;
  margin-bottom: 24px;
  transition: transform 0.2s;

  &:hover {
    transform: scale(1.05);
  }

  .user-status {
    position: absolute;
    bottom: 2px;
    right: 2px;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    border: 2px solid #1e3a5f;

    &.online {
      background-color: #67c23a;
    }
    &.busy {
      background-color: #f56c6c;
    }
    &.away {
      background-color: #e6a23c;
    }
    &.offline {
      background-color: #909399;
    }
  }
}

.nav-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
}

.nav-bottom {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.2s;
  position: relative;

  .nav-icon {
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    margin-bottom: 2px;
  }

  .nav-label {
    font-size: 10px;
    transform: scale(0.9);
  }

  &:hover {
    color: #fff;
    background-color: rgba(255, 255, 255, 0.1);
  }

  &.active {
    color: #fff;
    background-color: rgba(255, 255, 255, 0.15);

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 24px;
      background-color: #409eff;
      border-radius: 0 2px 2px 0;
    }
  }

  :deep(.el-badge__content) {
    border: none;
    top: -2px;
    right: 8px;
  }
}

// 用户面板
.user-panel {
  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-bottom: 16px;
    border-bottom: 1px solid #ebeef5;
    margin-bottom: 12px;

    .info-text {
      h3 {
        margin: 0 0 4px;
        font-size: 16px;
        font-weight: 500;
      }
      p {
        margin: 0;
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .status-selector {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #ebeef5;

    .status-option {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      padding: 6px 8px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 12px;
      transition: all 0.2s;

      &:hover {
        background-color: #f5f7fa;
      }

      &.active {
        background-color: #ecf5ff;
        color: #409eff;
      }

      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;

        &.online { background-color: #67c23a; }
        &.busy { background-color: #f56c6c; }
        &.away { background-color: #e6a23c; }
        &.offline { background-color: #909399; }
      }
    }
  }

  .user-actions {
    .action-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 8px;
      cursor: pointer;
      border-radius: 4px;
      transition: all 0.2s;

      &:hover {
        background-color: #f5f7fa;
      }

      &.danger {
        color: #f56c6c;
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
