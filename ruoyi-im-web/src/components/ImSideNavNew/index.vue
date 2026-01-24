<template>
  <nav
    :class="[
      'bg-primary flex flex-col items-center z-20 shrink-0 shadow-lg overflow-hidden',
      collapsed ? 'w-14' : 'w-16'
    ]"
    style="height: 100vh;"
    role="navigation"
    aria-label="主导航"
  >
    <!-- Logo 区域 -->
    <div class="shrink-0 pt-3 pb-2 w-full flex justify-center">
      <div class="w-12 h-12 bg-white/20 rounded-xl flex items-center justify-center backdrop-blur-sm">
        <span class="text-white font-bold text-2xl italic leading-none">若依</span>
      </div>
    </div>

    <!-- 导航项 -->
    <div class="flex flex-col gap-1 flex-1 w-full px-1.5 overflow-y-auto overflow-x-hidden" role="menubar">
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
        <span class="material-icons-outlined text-xl" aria-hidden="true">
          {{ item.icon }}
        </span>
        <span v-if="!collapsed" class="text-[9px] font-medium animate-fade-in">{{ item.label }}</span>

        <!-- 未读红点 -->
        <span
          v-if="item.key === 'chat' && unreadCount > 0"
          class="absolute top-0.5 right-0.5 bg-red-500 text-white text-[8px] rounded-full min-w-[10px] h-[10px] flex items-center justify-center border border-primary"
          :aria-label="`${unreadCount} 条未读消息`"
        >
          <template v-if="!collapsed">{{ unreadCount > 99 ? '99+' : unreadCount }}</template>
        </span>
      </button>
    </div>

    <!-- 底部操作区 -->
    <div class="flex flex-col gap-0.5 w-full px-1.5 pb-3 shrink-0">
      <!-- 主题切换按钮 -->
      <button
        @click="toggleDarkMode"
        class="nav-item"
        :class="{ 'nav-item-active': false }"
        :aria-label="isDark ? '切换到浅色模式' : '切换到深色模式'"
        :title="isDark ? '切换到浅色模式' : '切换到深色模式'"
      >
        <span class="material-icons-outlined text-xl" aria-hidden="true">
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
        <span class="material-icons-outlined text-xl" aria-hidden="true">settings</span>
      </button>

      <!-- 用户头像 -->
      <button
        @click="handleSwitch('profile')"
        class="w-12 h-12 rounded-full bg-gradient-to-br from-blue-400 to-blue-500 border-2 border-white/30 flex items-center justify-center hover:border-white/60 overflow-hidden shrink-0 mx-auto"
        :class="{ 'ring-2 ring-white': activeModule === 'profile' }"
        :aria-label="`个人资料: ${currentUser.nickname || currentUser.username || '我'}`"
        title="个人资料"
      >
        <img
          v-if="currentUser.avatar"
          :src="currentUser.avatar"
          class="w-full h-full object-cover"
          :alt="`${currentUser.nickname || currentUser.username || '用户'}的头像`"
        />
        <span v-else class="text-white font-semibold text-base" aria-hidden="true">
          {{ (currentUser.nickname || currentUser.username || '我').charAt(0) }}
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
.animate-fade-in {
  animation: fadeIn 0.2s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-1px); }
  to { opacity: 1; transform: translateY(0); }
}

.nav-item {
  position: relative;
  width: 100%;
  height: 44px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border-radius: 10px;
  transition: background-color 0.15s ease;

  &:hover {
    background-color: rgba(255, 255, 255, 0.12);
  }
}

/* 减少动画效果以尊重用户偏好 */
@media (prefers-reduced-motion: reduce) {
  .nav-item {
    transition: none;
  }

  .animate-fade-in {
    animation: none;
  }
}

.nav-item-active {
  background-color: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.nav-item-default {
  color: rgba(255, 255, 255, 0.75);
}

/* 滚动条样式 */
div[class*="overflow-y-auto"]::-webkit-scrollbar {
  width: 3px;
}

div[class*="overflow-y-auto"]::-webkit-scrollbar-track {
  background: transparent;
}

div[class*="overflow-y-auto"]::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

div[class*="overflow-y-auto"]::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>
