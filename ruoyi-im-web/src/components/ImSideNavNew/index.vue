<template>
  <nav
    :class="[
      'dingtalk-nav',
      'flex flex-col items-center z-[100] shrink-0'
    ]"
    role="navigation"
    aria-label="主导航"
  >
    <!-- Logo 区域 -->
    <div class="nav-logo-wrapper">
      <div class="nav-logo">
        <img
          v-if="logoUrl"
          :src="logoUrl"
          class="nav-logo-image"
          alt="Logo"
        >
        <span
          v-else
          class="nav-logo-text"
        >IM</span>
        <span
          v-if="unreadCount > 0"
          class="nav-logo-badge"
        >
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </div>
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
            { 'nav-item-active': activeModule === item.key }
          ]"
          :aria-label="item.label"
          :aria-current="activeModule === item.key ? 'page' : undefined"
          role="menuitem"
          @click="handleSwitch(item.key)"
        >
          <div class="nav-item-content">
            <component
              :is="item.icon"
              class="nav-item-icon"
              aria-hidden="true"
            />
            <span
              v-if="item.badge"
              class="nav-item-badge"
            >{{ item.badge }}</span>
          </div>
          <!-- 活跃状态指示条 -->
          <div class="active-indicator" />
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
          class="nav-item nav-action-item"
          aria-label="全局搜索"
          @click="handleOpenSearch"
        >
          <Search class="nav-item-icon" />
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
          class="nav-item nav-action-item"
          :class="{ 'nav-item-active': activeModule === 'settings' }"
          aria-label="设置"
          @click="handleSwitch('settings')"
        >
          <Setting class="nav-item-icon" />
          <div class="active-indicator" />
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
          :class="{ 'active': activeModule === 'profile' }"
          @click="handleSwitch('profile')"
        >
          <DingtalkAvatar
            :src="currentUser.avatar"
            :name="currentUser.nickname || currentUser.username || '我'"
            :user-id="currentUser.id"
            :size="38"
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
import request from '@/api/request'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import {
  ChatDotRound, User, Grid, Cloudy, Calendar, CircleCheck,
  Document, Message, Search, Setting, Shop
} from '@element-plus/icons-vue'

const props = defineProps({
  activeModule: {
    type: String,
    default: 'chat'
  }
})

const emit = defineEmits(['switch-module', 'open-search', 'open-settings'])
const store = useStore()

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
  return user && Object.keys(user).length > 0 ? user : { nickname: '加载中...', username: '...', avatar: '' }
})

const isUserOnline = computed(() => {
  if (!currentUser.value?.id) { return false }
  return store.state.im?.userStatus?.[currentUser.value.id] === 'online'
})

const navModules = ref([
  { key: 'chat', label: '消息', icon: ChatDotRound },
  { key: 'contacts', label: '通讯录', icon: User },
  { key: 'workbench', label: '工作台', icon: Grid },
  { key: 'drive', label: '云盘', icon: Cloudy },
  { key: 'calendar', label: '日历', icon: Calendar },
  { key: 'todo', label: '待办', icon: CircleCheck },
  { key: 'approval', label: '审批', icon: Document },
  { key: 'mail', label: '邮箱', icon: Message },
  { key: 'appcenter', label: '应用中心', icon: Shop },
  { key: 'assistant', label: 'AI助理', icon: ChatDotRound }
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

.dingtalk-nav {
  width: var(--dt-nav-sidebar-width);
  height: 100vh;
  // 优质品牌蓝渐变 - 调整为更深邃的质感
  background: linear-gradient(180deg, #0089FF 0%, #0064C8 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  user-select: none;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 4px 0 12px rgba(0, 0, 0, 0.08); /* 增强阴影 */
  z-index: 100;
}

.nav-logo-wrapper {
  padding: 20px 0 16px; /* 增加顶部空间 */
  flex-shrink: 0;
}

.nav-logo {
  position: relative;
  width: 42px; /* 稍微加大 */
  height: 42px;
  background: rgba(255, 255, 255, 0.15); /* 增加透明度 */
  border-radius: 10px; /* 更圆润 */
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); /* 弹性动画 */
  backdrop-filter: blur(4px);

  &:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: scale(1.05) translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  .nav-logo-text {
    font-size: 19px;
    font-weight: 800; /* 加粗 */
    color: #fff;
    letter-spacing: -0.5px;
  }

  .nav-logo-image {
    width: 32px;
    height: 32px;
    object-fit: contain;
  }
}

.nav-logo-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #ff4d4f;
  color: #fff;
  font-size: 11px;
  padding: 0 5px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid transparent; /* 使用透明边框 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  z-index: 10;
}

.nav-items-scroll {
  flex: 1;
  width: 100%;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px; /* 增加间距 */
  overflow-y: auto;
}

.nav-item {
  position: relative;
  width: 46px; /* 加大点击区域 */
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px; /* 更大的圆角 */
  background: transparent;
  border: none;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.75); /* 提高默认亮度 */
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  margin: 1px 0;

  &:hover {
    background: rgba(255, 255, 255, 0.12);
    color: #fff;
    transform: translateY(-1px);
  }

  &.nav-item-active {
    background: rgba(255, 255, 255, 0.2); /* 更明显的选中背景 */
    color: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .active-indicator {
      opacity: 1;
      transform: scaleY(1);
    }
    
    .nav-item-icon {
      transform: scale(1.05); /* 选中图标微放大 */
    }
  }
}

.nav-item-content {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}

.nav-item-icon {
  font-size: 24px;
  width: 24px;
  height: 24px;
  transition: transform 0.2s;
}

.nav-item-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: #ff4d4f;
  color: #fff;
  font-size: 10px;
  padding: 0 4px;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid transparent;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}

.active-indicator {
  position: absolute;
  left: -6px; /* 移出按钮外部左侧 */
  width: 4px;
  height: 18px;
  background: #fff;
  border-radius: 0 4px 4px 0; /* 圆角朝右 */
  opacity: 0;
  transform: scaleY(0.5);
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 1px 0 4px rgba(0,0,0,0.1);
}

.nav-footer {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 0 24px; /* 增加底部空间 */
  flex-shrink: 0;
  border-top: 1px solid rgba(255, 255, 255, 0.08); /* 调整边框颜色 */
}

.nav-action-item {
  color: rgba(255, 255, 255, 0.6) !important;
  
  &:hover, &.nav-item-active {
    color: #fff !important;
    background: rgba(255, 255, 255, 0.15); /* 增加背景反馈 */
  }
}

.nav-avatar-btn {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  margin-top: 8px; /* 增加与上方图标的间距 */

  &:hover {
    background: rgba(255, 255, 255, 0.15);
    transform: scale(1.05);
  }

  &.active {
    background: rgba(255, 255, 255, 0.2);
    box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.25);
  }

  .status-dot {
    position: absolute;
    bottom: 2px;
    right: 2px;
    width: 11px; /* 稍微加大 */
    height: 11px;
    border-radius: 50%;
    background: #858E9E;
    border: 2px solid #0064C8; /* 匹配新的背景色底部 */
    transition: background 0.3s;

    &.online {
      background: #00C853;
      box-shadow: 0 0 4px rgba(0, 200, 83, 0.4);
    }
  }
}

// 适配小屏幕
@media (max-height: 600px) {
  .nav-logo-wrapper { padding: 8px 0; }
  .nav-footer { padding: 8px 0 12px; }
  .nav-item { height: 38px; width: 38px; }
}
</style>
