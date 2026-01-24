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
    <div class="shrink-0 pt-4 pb-3 w-full flex justify-center">
      <div class="w-14 h-14 bg-white/20 rounded-2xl flex items-center justify-center backdrop-blur-sm">
        <span class="text-white font-bold text-2xl leading-none tracking-tight">IM</span>
      </div>
    </div>

    <!-- 导航项 -->
    <div class="flex flex-col gap-2 flex-1 w-full px-2 overflow-y-auto overflow-x-hidden" role="menubar">
      <button
        v-for="item in navModules"
        :key="item.key"
        @click="handleSwitch(item.key)"
        :class="[
          'nav-item',
          activeModule === item.key ? 'nav-item-active' : 'nav-item-default'
        ]"
        :title="item.label"
        :aria-label="item.label"
        :aria-current="activeModule === item.key ? 'page' : undefined"
        role="menuitem"
      >
        <span class="material-icons-outlined" aria-hidden="true">
          {{ item.icon }}
        </span>
      </button>
    </div>

    <!-- 底部操作区 -->
    <div class="flex flex-col gap-2 w-full px-2 pb-4 shrink-0">
      <!-- 主题切换按钮 -->
      <button
        @click="toggleDarkMode"
        class="nav-item"
        :class="{ 'nav-item-active': false }"
        :aria-label="isDark ? '切换到浅色模式' : '切换到深色模式'"
        :title="isDark ? '切换到浅色模式' : '切换到深色模式'"
      >
        <span class="material-icons-outlined" aria-hidden="true">
          {{ isDark ? 'light_mode' : 'dark_mode' }}
        </span>
      </button>

      <!-- 设置按钮 -->
      <button
        @click="handleSwitch('settings')"
        class="nav-item"
        :class="{ 'nav-item-active': activeModule === 'settings' }"
        aria-label="设置"
        title="设置"
      >
        <span class="material-icons-outlined" aria-hidden="true">settings</span>
      </button>

      <!-- 用户头像 -->
      <button
        @click="handleSwitch('profile')"
        class="w-14 h-14 rounded-full bg-gradient-to-br from-blue-400 to-blue-500 border-2 border-white/30 flex items-center justify-center hover:border-white/60 overflow-hidden shrink-0 mx-auto transition-all"
        :class="{ 'ring-2 ring-white ring-offset-2 ring-offset-primary': activeModule === 'profile' }"
        :aria-label="`个人资料: ${currentUser.nickname || currentUser.username || '我'}`"
        title="个人资料"
      >
        <img
          v-if="currentUser.avatar"
          :src="currentUser.avatar"
          class="w-full h-full object-cover"
          :alt="`${currentUser.nickname || currentUser.username || '用户'}的头像`"
        />
        <span v-else class="text-white font-semibold text-lg" aria-hidden="true">
          {{ (currentUser.nickname || currentUser.username || '我').charAt(0).toUpperCase() }}
        </span>
      </button>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useTheme } from '@/composables/useTheme'

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
</script>

<style scoped>
/* 钉钉风格导航尺寸 */
.w-18 {
  width: 72px;
}

.nav-item {
  position: relative;
  width: 100%;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: rgba(255, 255, 255, 0.15);
  }

  .material-icons-outlined {
    font-size: 24px;
  }
}

/* 底部小按钮（主题、设置） */
.nav-item-small {
  position: relative;
  width: 100%;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: rgba(255, 255, 255, 0.12);
  }
}

/* 减少动画效果以尊重用户偏好 */
@media (prefers-reduced-motion: reduce) {
  .nav-item,
  .nav-item-small {
    transition: none;
  }
}

.nav-item-active {
  background-color: rgba(255, 255, 255, 0.22);
  color: #fff;
}

.nav-item-default {
  color: rgba(255, 255, 255, 0.7);
}

/* 滚动条样式 - 更细更美观 */
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
