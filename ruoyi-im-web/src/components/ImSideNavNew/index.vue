  <nav 
    :class="[
      'bg-primary flex flex-col items-center py-6 gap-6 z-20 shrink-0 shadow-lg transition-all duration-300',
      collapsed ? 'w-16' : 'w-20'
    ]"
  >
    <!-- Logo 区域与折叠按钮 -->
    <div class="flex flex-col items-center gap-4 mb-2 shrink-0">
      <div 
        class="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center cursor-pointer backdrop-blur-sm"
        @click="$emit('toggle-collapse')"
      >
        <span class="text-white font-bold text-xl italic leading-none">{{ collapsed ? '若' : '若依' }}</span>
      </div>
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
        <el-icon class="text-2xl transition-transform duration-300" :class="{ 'scale-110': collapsed }">
          <component :is="item.icon" />
        </el-icon>
        <span v-if="!collapsed" class="text-[11px] font-medium tracking-wide animate-fade-in">{{ item.label }}</span>
        
        <!-- 未读红点 -->
        <span
          v-if="item.key === 'chat' && unreadCount > 0"
          :class="[
            'absolute bg-red-500 text-white text-[10px] rounded-full px-1 border border-primary flex items-center justify-center',
            collapsed ? 'top-1 right-2 min-w-[12px] h-[12px]' : 'top-1 right-1 min-w-[14px] h-[14px]'
          ]"
        >
          <template v-if="!collapsed">{{ unreadCount > 99 ? '99+' : unreadCount }}</template>
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

.animate-fade-in {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-2px); }
  to { opacity: 1; transform: translateY(0); }
}

.nav-item {
  position: relative;
  width: 100%;
  padding: 8px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border-radius: 12px;
  transition: all 0.3s;
  
  &:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }
}

.nav-item-active {
  background-color: rgba(255, 255, 255, 0.2);
  color: #fff;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.nav-item-default {
  color: rgba(255, 255, 255, 0.7);
}
</style>
