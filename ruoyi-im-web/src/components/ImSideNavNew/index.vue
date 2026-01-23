<template>
  <nav class="w-20 bg-primary flex flex-col items-center py-6 gap-6 z-20 shrink-0 shadow-lg">
    <!-- Logo 区域 -->
    <div class="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center cursor-pointer mb-2 shrink-0">
      <span class="text-white font-bold text-xl italic">若</span>
    </div>

    <!-- 导航项 -->
    <div class="flex flex-col gap-3 flex-1 items-center w-full px-2">
      <button
        v-for="item in navModules"
        :key="item.key"
        @click="handleSwitch(item.key)"
        :class="[
          'nav-item',
          activeModule === item.key ? 'nav-item-active' : 'nav-item-default'
        ]"
        :title="item.label"
      >
        <el-icon class="text-2xl">
          <component :is="item.icon" />
        </el-icon>
        <span class="text-[11px] font-medium tracking-wide">{{ item.label }}</span>
        <!-- 未读红点 -->
        <span
          v-if="item.key === 'chat' && unreadCount > 0"
          class="absolute top-1 right-1 min-w-[14px] h-[14px] flex items-center justify-center bg-red-500 text-white text-[10px] rounded-full px-1"
        >
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </button>
    </div>

    <!-- 底部操作区 -->
    <div class="flex flex-col gap-4 items-center w-full px-2 pb-2">
      <!-- 主题切换按钮 -->
      <button
        @click="toggleDarkMode"
        class="p-2 text-white/70 hover:text-white hover:bg-white/10 rounded-xl transition-all"
        title="切换主题"
      >
        <el-icon class="text-xl">
          <component :is="isDark ? 'Sunny' : 'Moon'" />
        </el-icon>
      </button>

      <!-- 设置按钮 -->
      <button
        @click="handleSwitch('settings')"
        class="p-2 text-white/70 hover:text-white hover:bg-white/10 rounded-xl transition-all"
        :class="{ 'bg-white/10': activeModule === 'settings' }"
        title="设置"
      >
        <el-icon class="text-xl">
          <Setting />
        </el-icon>
      </button>

      <!-- 用户头像 -->
      <div
        @click="handleSwitch('profile')"
        class="w-10 h-10 rounded-full bg-blue-400 border-2 border-white/20 flex items-center justify-center cursor-pointer hover:border-white/50 transition-colors overflow-hidden"
        :class="{ 'border-white': activeModule === 'profile' }"
        title="个人资料"
      >
        <img
          v-if="currentUser.avatar"
          :src="currentUser.avatar"
          class="w-full h-full object-cover"
          alt="用户"
        />
        <span v-else class="text-white font-medium text-sm">
          {{ (currentUser.nickname || currentUser.username || '我').charAt(0) }}
        </span>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useTheme } from '@/composables/useTheme'
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
  Sunny
} from '@element-plus/icons-vue'

const props = defineProps({
  activeModule: {
    type: String,
    default: 'chat'
  }
})

const emit = defineEmits(['switch-module'])
const store = useStore()
const { isDark, toggleDark: toggleDarkMode } = useTheme()

// 未读消息数
const unreadCount = computed(() => store.state.im?.totalUnreadCount || 0)

// 当前用户
const currentUser = computed(() => store.getters['user/currentUser'] || {})

// 导航模块配置
const navModules = ref([
  { key: 'chat', label: '消息', icon: ChatDotRound },
  { key: 'contacts', label: '通讯录', icon: User },
  { key: 'workbench', label: '工作台', icon: Grid },
  { key: 'drive', label: '云盘', icon: FolderOpened },
  { key: 'calendar', label: '日历', icon: Calendar },
  { key: 'todo', label: '待办', icon: Clock },
  { key: 'approval', label: '审批', icon: Document },
  { key: 'mail', label: '邮箱', icon: ChatLineSquare },
  { key: 'assistant', label: 'AI助理', icon: MagicStick }
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
/* 添加 Material Symbols Outlined 字体支持（如果使用的话） */
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0');
</style>
