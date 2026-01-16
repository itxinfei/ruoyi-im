<template>
  <aside class="nav-sidebar" :style="{ width: navWidth + 'px' }" @mousedown="startNavResize">
    <nav class="nav-list">
      <el-tooltip
        v-for="item in navModules"
        :key="item.key"
        :content="item.label"
        placement="right"
        :show-after="500"
      >
        <div
          class="nav-item"
          :class="{ active: activeModule === item.key }"
          @click="handleNavClick(item.key)"
        >
          <el-icon class="nav-icon">
            <component :is="item.icon" />
          </el-icon>
          <!-- 未读红点 -->
          <span v-if="item.key === 'chat' && unreadCount > 0" class="nav-dot"></span>
          <!-- 徽章 -->
          <el-badge
            v-if="item.badge"
            :value="item.badge"
            :max="99"
            class="nav-badge"
          />
        </div>
      </el-tooltip>
    </nav>
    <!-- 拖拽手柄 -->
    <div class="nav-resize-handle" @mousedown.stop="startNavResize"></div>
  </aside>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
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
  MagicStick
} from '@element-plus/icons-vue'

const props = defineProps({
  activeModule: {
    type: String,
    default: 'chat'
  },
  navWidth: {
    type: Number,
    default: 64  // DingTalk standard 64px
  }
})

const emit = defineEmits(['switchModule'])

const store = useStore()

// 响应式状态
const isResizing = ref(false)
const startX = ref(0)
const startWidth = ref(64)  // DingTalk standard 64px

// 计算属性
const unreadCount = computed(() => store.state.im.unreadCount)

const navModules = computed(() => [
  {
    key: 'chat',
    label: '消息',
    icon: ChatDotRound,
    badge: unreadCount.value || null
  },
  {
    key: 'contacts',
    label: '联系人',
    icon: User,
    badge: null
  },
  {
    key: 'workbench',
    label: '工作台',
    icon: Grid,
    badge: null
  },
  {
    key: 'drive',
    label: '云盘',
    icon: FolderOpened,
    badge: null
  },
  {
    key: 'calendar',
    label: '日历',
    icon: Calendar,
    badge: null
  },
  {
    key: 'todo',
    label: '待办',
    icon: Clock,
    badge: null
  },
  {
    key: 'approval',
    label: '审批',
    icon: Document,
    badge: null
  },
  {
    key: 'mail',
    label: '邮箱',
    icon: ChatLineSquare,
    badge: null
  },
  {
    key: 'assistant',
    label: 'AI助理',
    icon: MagicStick,
    badge: null
  },
  {
    key: 'settings',
    label: '设置',
    icon: Setting,
    badge: null
  }
])

// 方法
const handleNavClick = (moduleKey) => {
  emit('switchModule', moduleKey)
}

const startNavResize = (e) => {
  // 如果点击的是导航项，不进行拖拽
  if (e.target.closest('.nav-item')) {
    return
  }

  isResizing.value = true
  startX.value = e.clientX
  startWidth.value = props.navWidth

  document.addEventListener('mousemove', handleNavResize)
  document.addEventListener('mouseup', stopNavResize)
}

const handleNavResize = (e) => {
  if (!isResizing.value) return

  const diff = e.clientX - startX.value
  const newWidth = startWidth.value + diff

  // 限制最小和最大宽度（DingTalk standard 64px）
  if (newWidth >= 64 && newWidth <= 200) {
    emit('update:navWidth', newWidth)
  }
}

const stopNavResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', handleNavResize)
  document.removeEventListener('mouseup', stopNavResize)
}
</script>

<style scoped>
  /* nav icon styling */
  .nav-icon {
    font-size: 24px;  /* DingTalk standard 24px */
    color: #8c8c8c;
    transition: all 0.2s ease;
  }
</style>