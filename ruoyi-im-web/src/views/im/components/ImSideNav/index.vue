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
    default: 60
  }
})

const emit = defineEmits(['switchModule'])

const store = useStore()

// 响应式状态
const isResizing = ref(false)
const startX = ref(0)
const startWidth = ref(60)

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

  // 限制最小和最大宽度
  if (newWidth >= 60 && newWidth <= 200) {
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
.nav-sidebar {
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  border-right: 1px solid #f0f0f0;
  position: relative;
  user-select: none;
  min-width: 60px;
  max-width: 200px;
}

.nav-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  gap: 8px;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background-color: #d9d9d9;
    border-radius: 2px;

    &:hover {
      background-color: #bfbfbf;
    }
  }

  &::-webkit-scrollbar-track {
    background-color: transparent;
  }
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background-color: rgba(0, 0, 0, 0.04);

    .nav-icon {
      transform: scale(1.05);
    }
  }

  &.active {
    background-color: rgba(22, 119, 255, 0.1);
  }

  &.active::before {
    content: '';
    position: absolute;
    left: -8px;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 24px;
    background-color: #1677ff;
    border-radius: 2px;
  }

  /* AI助理特殊样式 */
  &[data-module="assistant"] {
    &.active::before {
      background: linear-gradient(180deg, #722ED1 0%, #9254DE 100%);
    }

    &:hover {
      background-color: rgba(114, 46, 209, 0.1);
    }

    .nav-icon {
      color: #722ED1;
    }
  }
}

.nav-icon {
  font-size: 22px;
  color: #8c8c8c;
  transition: all 0.2s ease;
}

.nav-item.active .nav-icon {
  color: #1677ff;
}

.nav-item[data-module="assistant"] .nav-icon {
  color: #722ED1;
}

.nav-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 10px;
  height: 10px;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  border-radius: 50%;
  border: 2px solid #ffffff;
  box-shadow: 0 2px 6px rgba(255, 77, 79, 0.3);
  animation: badgePopup 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

@keyframes badgePopup {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.nav-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  z-index: 10;

  :deep(.el-badge__content) {
    background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
    border: 2px solid #ffffff;
    box-shadow: 0 2px 6px rgba(255, 77, 79, 0.3);
    font-size: 12px;
    font-weight: 600;
    min-width: 18px;
    height: 18px;
    line-height: 18px;
    padding: 0 4px;
  }
}

.nav-resize-handle {
  position: absolute;
  right: -2px;
  top: 0;
  width: 4px;
  height: 100%;
  cursor: col-resize;
  background-color: transparent;
  transition: background-color 0.2s ease;
  z-index: 100;

  &:hover {
    background-color: #1677ff;
  }

  &:active {
    background-color: #1677ff;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-sidebar {
    min-width: 60px;
  }

  .nav-item {
    width: 44px;
    height: 44px;
  }

  .nav-icon {
    font-size: 20px;
  }

  .nav-dot {
    width: 8px;
    height: 8px;
  }

  .nav-item.active::before {
    width: 2px;
    height: 20px;
  }
}
</style>