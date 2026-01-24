<template>
  <nav
    :class="[
      'bg-primary flex flex-col items-center z-20 shrink-0 shadow-lg',
      collapsed ? 'w-16' : 'w-18'
    ]"
    style="height: 100vh;"
    role="navigation"
    aria-label="主导航"
  >
    <!-- Logo 区域 -->
    <div class="shrink-0 pt-2 pb-2 w-full flex justify-center">
      <div class="w-14 h-14 bg-white/20 rounded-2xl flex items-center justify-center backdrop-blur-sm">
        <span class="text-white font-bold text-2xl leading-none tracking-tight">IM</span>
      </div>
    </div>

    <!-- 导航项 -->
    <div class="flex flex-col gap-1 flex-1 w-full px-2 overflow-y-auto overflow-x-hidden" role="menubar">
      <el-tooltip
        v-for="item in navModules"
        :key="item.key"
        :content="item.label"
        placement="right"
        :show-after="500"
      >
        <button
          @click="handleSwitch(item.key)"
          :class="[
            'nav-item',
            activeModule === item.key ? 'nav-item-active' : 'nav-item-default'
          ]"
          :aria-label="item.label"
          :aria-current="activeModule === item.key ? 'page' : undefined"
          role="menuitem"
        >
          <span class="material-icons-outlined" aria-hidden="true">
            {{ item.icon }}
          </span>
        </button>
      </el-tooltip>
    </div>

    <!-- 底部操作区 -->
    <div class="flex flex-col gap-1 w-full px-2 pb-2 shrink-0">
      <!-- 主题切换按钮 -->
      <el-tooltip :content="isDark ? '切换到浅色模式' : '切换到深色模式'" placement="right" :show-after="500">
        <button
          @click="toggleDarkMode"
          class="nav-item nav-item-default"
          :aria-label="isDark ? '切换到浅色模式' : '切换到深色模式'"
        >
          <span class="material-icons-outlined" aria-hidden="true">
            {{ isDark ? 'light_mode' : 'dark_mode' }}
          </span>
        </button>
      </el-tooltip>

      <!-- 帮助与反馈 -->
      <el-tooltip content="帮助与反馈" placement="right" :show-after="500">
        <button
          @click="handleHelp"
          class="nav-item nav-item-default"
          aria-label="帮助与反馈"
        >
          <span class="material-icons-outlined" aria-hidden="true">help_outline</span>
        </button>
      </el-tooltip>

      <!-- 设置按钮 -->
      <el-tooltip content="设置" placement="right" :show-after="500">
        <button
          @click="handleSwitch('settings')"
          class="nav-item"
          :class="activeModule === 'settings' ? 'nav-item-active' : 'nav-item-default'"
          aria-label="设置"
        >
          <span class="material-icons-outlined" aria-hidden="true">settings</span>
        </button>
      </el-tooltip>

      <!-- 用户头像 -->
      <el-tooltip :content="`个人资料: ${currentUser.nickname || currentUser.username || '我'}`" placement="right" :show-after="500">
        <button
          @click="handleSwitch('profile')"
          class="avatar-btn mt-1"
          :class="{ 'avatar-active': activeModule === 'profile' }"
          :aria-label="`个人资料: ${currentUser.nickname || currentUser.username || '我'}`"
        >
          <div class="avatar-inner">
            <img
              v-if="currentUser.avatar"
              :src="currentUserAvatar"
              class="w-full h-full object-cover"
              :alt="`${currentUser.nickname || currentUser.username || '用户'}的头像`"
            />
            <span v-else class="text-white font-semibold text-lg" aria-hidden="true">
              {{ (currentUser.nickname || currentUser.username || '我').charAt(0).toUpperCase() }}
            </span>
          </div>
          <!-- 在线状态点 -->
          <span class="status-dot"></span>
        </button>
      </el-tooltip>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useTheme } from '@/composables/useTheme'
import { addTokenToUrl } from '@/utils/file'

const props = defineProps({
  activeModule: {
    type: String,
    default: 'chat'
  },
  collapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['switch-module', 'toggle-collapse'])
const store = useStore()
const { isDark, toggleDark: toggleDarkMode } = useTheme()

// 未读消息数
const unreadCount = computed(() => store.state.im?.totalUnreadCount || 0)

// 当前用户
const currentUser = computed(() => store.getters['user/currentUser'] || {})

// 当前用户头像（带 token）
const currentUserAvatar = computed(() => {
  if (!currentUser.value.avatar) return ''
  return addTokenToUrl(currentUser.value.avatar)
})

// 导航模块配置 - 使用 Material Icons Outlined 图标名称
const navModules = ref([
  { key: 'chat', label: '消息', icon: 'chat_bubble' },
  { key: 'contacts', label: '通讯录', icon: 'group' },
  { key: 'workbench', label: '工作台', icon: 'grid_view' },
  { key: 'drive', label: '云盘', icon: 'cloud' },
  { key: 'calendar', label: '日历', icon: 'calendar_today' },
  { key: 'todo', label: '待办', icon: 'check_circle' },
  { key: 'approval', label: '审批', icon: 'approval' },
  { key: 'mail', label: '邮箱', icon: 'email' },
  { key: 'assistant', label: 'AI助理', icon: 'smart_toy' }
])

/**
 * 切换模块
 * @param {string} key - 模块标识
 */
function handleSwitch(key) {
  emit('switch-module', key)
}

/**
 * 处理帮助与反馈
 */
function handleHelp() {
  window.open('https://github.com/itxinfei/ruoyi-im', '_blank')
}
</script>

<style scoped>
/* 钉钉风格导航尺寸 */
.w-18 {
  width: 72px;
}

/* 所有导航项统一样式 */
.nav-item {
  position: relative;
  width: 100%;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: rgba(255, 255, 255, 0.15);
  }

  .material-icons-outlined {
    font-size: 22px;
  }
}

/* 减少动画效果以尊重用户偏好 */
@media (prefers-reduced-motion: reduce) {
  .nav-item {
    transition: none;
  }
}

.nav-item-active {
  background-color: rgba(255, 255, 255, 0.22);
  color: #fff;
}

.nav-item-default {
  color: rgba(255, 255, 255, 0.75);
}

/* 用户头像按钮样式 */
.avatar-btn {
  position: relative;
  width: 52px;
  height: 52px;
  margin: 0 auto;
  padding: 2px;
  border-radius: 50%;
  background: transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background: rgba(255, 255, 255, 0.15);
  }
}

.avatar-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border: 2px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s;

  .avatar-btn:hover & {
    border-color: rgba(255, 255, 255, 0.6);
  }
}

.avatar-active .avatar-inner {
  border-color: #fff;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.2);
}

/* 在线状态点 */
.status-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background-color: #52c41a;
  border: 2px solid var(--el-color-primary, #0089ff);
  border-radius: 50%;
  z-index: 1;
}

/* 滚动条样式 */
div[class*="overflow-y-auto"]::-webkit-scrollbar {
  width: 4px;
}

div[class*="overflow-y-auto"]::-webkit-scrollbar-track {
  background: transparent;
}

div[class*="overflow-y-auto"]::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
}

div[class*="overflow-y-auto"]::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.35);
}
</style>
