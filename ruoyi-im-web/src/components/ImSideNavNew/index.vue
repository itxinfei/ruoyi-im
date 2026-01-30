<template>
  <nav
    :class="[
      'dingtalk-nav',
      'flex flex-col items-center z-20 shrink-0 bg-nav-light'
    ]"
    style="height: 100vh;"
    role="navigation"
    aria-label="主导航"
  >
    <!-- Logo 区域 -->
    <div class="nav-logo-wrapper">
      <div class="nav-logo">
        <img v-if="logoUrl" :src="logoUrl" class="nav-logo-image" alt="Logo" />
        <span v-else class="nav-logo-text">IM</span>
        <span class="nav-logo-badge" v-if="unreadCount > 0">
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </div>
    </div>

    <!-- 导航项 -->
    <div class="nav-items" role="menubar">
      <el-tooltip
        v-for="item in navModules"
        :key="item.key"
        :content="item.label"
        placement="right"
        :show-after="500"
        :hide-after="0"
      >
        <button
          @click="handleSwitch(item.key)"
          :class="[
            'nav-item',
            activeModule === item.key ? 'nav-item-active' : ''
          ]"
          :aria-label="item.label"
          :aria-current="activeModule === item.key ? 'page' : undefined"
          role="menuitem"
        >
          <component :is="item.icon" class="nav-item-icon" aria-hidden="true" />
          <span v-if="item.badge" class="nav-item-badge">{{ item.badge }}</span>
        </button>
      </el-tooltip>
    </div>

    <!-- 底部操作区 -->
    <div class="nav-footer">
      <!-- 搜索按钮 -->
      <el-tooltip content="全局搜索" placement="right" :show-after="500" :hide-after="0">
        <button
          @click="handleOpenSearch"
          class="nav-item nav-item-action"
          aria-label="全局搜索"
        >
          <Search class="nav-icon" aria-hidden="true" />
        </button>
      </el-tooltip>



      <!-- 设置按钮 -->
      <el-tooltip content="设置" placement="right" :show-after="500" :hide-after="0">
        <button
          @click="handleSwitch('settings')"
          class="nav-item nav-item-action"
          :class="{ 'nav-item-active': activeModule === 'settings' }"
          aria-label="设置"
        >
          <Setting class="nav-icon" aria-hidden="true" />
        </button>
      </el-tooltip>

      <!-- 用户头像 -->
      <el-tooltip
        :content="`个人资料: ${currentUser.nickname || currentUser.username || '我'}`"
        placement="right"
        :show-after="500"
        :hide-after="0"
      >
        <button
          @click="handleSwitch('profile')"
          class="nav-avatar"
          :class="{ 'nav-avatar-active': activeModule === 'profile' }"
          :aria-label="`个人资料: ${currentUser.nickname || currentUser.username || '我'}`"
        >
          <DingtalkAvatar
            :src="currentUser.avatar"
            :name="currentUser.nickname || currentUser.username || '我'"
            :user-id="currentUser.id"
            :size="42"
            shape="circle"
          />
          <!-- 在线状态点 -->
          <span class="nav-avatar-status" :class="{ 'online': isUserOnline }"></span>
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
  Document, Message, Search, Setting
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
  // 只对管理员请求自定义logo
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



// 未读消息数
const unreadCount = computed(() => store.state.im?.totalUnreadCount || 0)

// 当前用户
const currentUser = computed(() => {
  const user = store.getters['user/currentUser']
  return user && Object.keys(user).length > 0 ? user : { nickname: '加载中...', username: '...', avatar: '' }
})

// 用户在线状态
const isUserOnline = computed(() => {
  if (!currentUser.value?.id) return false
  return store.state.im?.userStatus?.[currentUser.value.id] === 'online'
})

// 导航模块配置
const navModules = ref([
  { key: 'chat', label: '消息', icon: ChatDotRound },
  { key: 'contacts', label: '通讯录', icon: User },
  { key: 'workbench', label: '工作台', icon: Grid },
  { key: 'drive', label: '云盘', icon: Cloudy },
  { key: 'calendar', label: '日历', icon: Calendar },
  { key: 'todo', label: '待办', icon: CircleCheck },
  { key: 'approval', label: '审批', icon: Document },
  { key: 'mail', label: '邮箱', icon: Message },
  { key: 'assistant', label: 'AI助理', icon: ChatDotRound }
])



/**
 * 切换模块
 */
function handleSwitch(key) {
  emit('switch-module', key)
}

/**
 * 处理帮助与反馈 - 打开设置对话框并定位到帮助标签页
 */
function handleHelp() {
  emit('open-settings', 'help')
}

/**
 * 打开全局搜索
 */
function handleOpenSearch() {
  emit('open-search')
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 导航容器
// ============================================================================
.dingtalk-nav {
  width: 60px;  // 钉钉标准：左侧导航栏宽度 60px
  height: 100vh;
  max-height: 100vh;
  background: var(--dt-bg-sidebar-gradient);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1), 0 4px 24px rgba(0, 0, 0, 0.12);
  animation: slideInLeft 0.4s var(--dt-ease-out);
  overflow: hidden;
}

.bg-nav-light {
  background: linear-gradient(180deg, #0089FF 0%, #006ECC 100%);
}

// ============================================================================
// Logo 区域
// ============================================================================
.nav-logo-wrapper {
  padding: 8px 0;  // 上下8px，左右0（居中）
  width: 100%;
  display: flex;
  justify-content: center;  // 水平居中
  flex-shrink: 0;
}

.nav-logo {
  position: relative;
  width: 40px;  // 钉钉标准：40px × 40px
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;  // 小圆角
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.nav-logo:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.05);
}

.nav-logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #FFFFFF;  // 纯白色，不要渐变
  letter-spacing: -0.5px;
  line-height: 1;
}

.nav-logo-image {
  width: 40px;
  height: 40px;
  object-fit: contain;
  border-radius: 8px;
}

.nav-logo-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: var(--dt-error-color);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  animation: scaleIn 0.3s var(--dt-ease-bounce), pulse 2s ease-in-out infinite;
}

// ============================================================================
// 导航项区域
// ============================================================================
.nav-items {
  flex: 1;
  width: 100%;
  padding: 4px 0;  // 移除左右padding，只保留上下padding
  display: flex;
  flex-direction: column;
  align-items: center;  // 水平居中
  gap: 2px;
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-items::-webkit-scrollbar {
  width: 4px;
}

.nav-items::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

// ============================================================================
// 导航项
// ============================================================================
.nav-item {
  position: relative;
  width: 40px;  // 钉钉标准：导航项 40px × 40px
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;  // 钉钉标准：圆角 8px
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  color: rgba(255, 255, 255, 0.85);
  animation: fadeInUp 0.4s var(--dt-ease-out) both;

  @for $i from 1 through 9 {
    &:nth-child(#{$i}) {
      animation-delay: #{0.05 * $i}s;
    }
  }
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  transform: scale(1.05);
  backdrop-filter: blur(8px);
}

.nav-item-active {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: pulse 0.3s var(--dt-ease-out);
  backdrop-filter: blur(12px);
}

.nav-item-active::before {
  content: '';
  position: absolute;
  left: -8px;
  width: 3px;
  height: 20px;
  background: #fff;
  border-radius: 0 2px 2px 0;
  animation: slideInLeft 0.2s var(--dt-ease-out);
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.5);
}

.nav-item-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  transition: transform 0.3s var(--dt-ease-out);
  width: 22px;
  height: 22px;
  line-height: 1;  // 确保垂直居中
}

.nav-item:hover .nav-item-icon {
  transform: scale(1.15);
}

.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  width: 22px;
  height: 22px;
  line-height: 1;  // 确保垂直居中
}

.nav-item-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: var(--dt-error-color);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  animation: scaleIn 0.3s var(--dt-ease-bounce);
}

// ============================================================================
// 底部操作区
// ============================================================================
.nav-footer {
  padding: 4px 0 12px;  // 移除左右padding
  display: flex;
  flex-direction: column;
  align-items: center;  // 水平居中
  gap: 2px;
  width: 100%;
  flex-shrink: 0;
  animation: fadeInUp 0.5s var(--dt-ease-out) both;
}

.nav-item-action {
  width: 40px;  // 统一为 40px
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  justify-content: center;
  border-radius: 12px;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  color: rgba(255, 255, 255, 0.65);
  position: relative;
}

.nav-item-action:hover {
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.9);
  transform: scale(1.05);
  backdrop-filter: blur(8px);
}

.nav-item-action.nav-item-active {
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.auto-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 12px;
  height: 12px;
  background: #fff;
  color: var(--dt-brand-color);
  font-size: 9px;
  font-weight: 700;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: scaleIn 0.3s var(--dt-ease-bounce);
}

// ============================================================================
// 用户头像
// ============================================================================
.nav-avatar {
  position: relative;
  width: 48px;  // 与其他按钮统一
  height: 48px;
  margin: 4px auto 0;
  padding: 3px;
  border-radius: 50%;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeInUp 0.6s var(--dt-ease-bounce) both;
}

.nav-avatar:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: scale(1.05);
  backdrop-filter: blur(8px);
}

.nav-avatar-active {
  background: rgba(255, 255, 255, 0.2);
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.25), 0 4px 12px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(12px);
}

.nav-avatar :deep(.dingtalk-avatar) {
  width: 100%;
  height: 100%;
  transition: transform 0.3s var(--dt-ease-out);
}

.nav-avatar:hover :deep(.dingtalk-avatar) {
  transform: scale(1.05);
}

.nav-avatar :deep(.avatar-inner) {
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: border-color 0.3s var(--dt-ease-out);
}

.nav-avatar:hover :deep(.avatar-inner) {
  border-color: rgba(255, 255, 255, 0.6);
}

.nav-avatar-active :deep(.avatar-inner) {
  border-color: rgba(255, 255, 255, 0.9);
}

.nav-avatar-status {
  position: absolute;
  bottom: 4px;
  right: 4px;
  width: 12px;
  height: 12px;
  background: var(--dt-text-tertiary);
  border: 2px solid var(--dt-bg-sidebar);
  border-radius: 50%;
  transition: all 0.3s var(--dt-ease-out);
}

.nav-avatar-status.online {
  background: var(--dt-success-color);
  box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.2);
  animation: pulse 2s ease-in-out infinite;
}


</style>
