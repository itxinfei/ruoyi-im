<template>
  <aside class="nav-sidebar" :style="{ width: navWidth + 'px' }">
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
          <span v-if="item.key === 'chat' && unreadCount > 0" class="nav-dot"></span>
        </div>
      </el-tooltip>
    </nav>
  </aside>
</template>

<script setup>
import { ref } from 'vue'
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
  }
})

const emit = defineEmits(['switchModule'])

const navWidth = ref(64)
const unreadCount = ref(5)

const navModules = ref([
  {
    key: 'chat',
    label: '消息',
    icon: ChatDotRound
  },
  {
    key: 'contacts',
    label: '联系人',
    icon: User
  },
  {
    key: 'workbench',
    label: '工作台',
    icon: Grid
  },
  {
    key: 'drive',
    label: '云盘',
    icon: FolderOpened
  },
  {
    key: 'calendar',
    label: '日历',
    icon: Calendar
  },
  {
    key: 'todo',
    label: '待办',
    icon: Clock
  },
  {
    key: 'approval',
    label: '审批',
    icon: Document
  },
  {
    key: 'mail',
    label: '邮箱',
    icon: ChatLineSquare
  },
  {
    key: 'assistant',
    label: 'AI助理',
    icon: MagicStick
  },
  {
    key: 'settings',
    label: '设置',
    icon: Setting
  }
])

const handleNavClick = (moduleKey) => {
  emit('switchModule', moduleKey)
}
</script>

<style scoped>
.nav-sidebar {
  display: flex;
  flex-direction: column;
  background-color: #2c2c2c;
  border-right: 1px solid #e8e8e8;
}

.nav-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0;
  gap: 4px;
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
    background-color: rgba(255, 255, 255, 0.1);
  }

  &.active {
    background-color: rgba(0, 137, 255, 0.1);
  }

  &.active::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 24px;
    background-color: #0089ff;
    border-radius: 0 2px 2px 0;
  }
}

.nav-icon {
  font-size: 24px;
  color: #8c8c8c;
  transition: all 0.2s ease;
}

.nav-item:hover .nav-icon,
.nav-item.active .nav-icon {
  color: #0089ff;
}

.nav-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 10px;
  height: 10px;
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  border-radius: 50%;
  border: 2px solid #2c2c2c;
}
</style>
