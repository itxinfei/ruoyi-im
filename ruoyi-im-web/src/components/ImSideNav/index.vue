<template>
  <aside class="nav-sidebar" :style="{ width: navWidth + 'px' }">
    <div class="logo-area">
      <div class="logo-box">
        <span class="logo-text">钉</span>
      </div>
    </div>

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
          <span class="nav-label">{{ item.label }}</span>
          <span v-if="item.key === 'chat' && unreadCount > 0" class="nav-dot"></span>
        </div>
      </el-tooltip>
    </nav>

    <div class="nav-footer">
      <div
        class="nav-item"
        :class="{ active: activeModule === 'settings' }"
        @click="handleNavClick('settings')"
      >
        <el-icon class="nav-icon">
          <Setting />
        </el-icon>
        <span class="nav-label">设置</span>
      </div>

      <div
        class="user-avatar"
        :class="{ active: activeModule === 'profile' }"
        @click="handleNavClick('profile')"
      >
        <span class="avatar-text">我</span>
      </div>
    </div>
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
  background-color: #0089ff;
  border-right: 1px solid #e8e8e8;
}

.logo-area {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  margin-bottom: 4px;
}

.logo-box {
  width: 40px;
  height: 40px;
  background-color: #ffffff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  color: #0089ff;
  font-size: 18px;
  font-weight: bold;
}

.nav-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4px 0;
  gap: 2px;
  flex: 1;
  overflow-y: auto;
}

.nav-item {
  position: relative;
  display: flex;
  flex-direction: column;
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
    background-color: rgba(255, 255, 255, 0.2);
  }

  &.active::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 24px;
    background-color: #ffffff;
    border-radius: 0 2px 2px 0;
  }
}

.nav-icon {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.2s ease;
}

.nav-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1;
  margin-top: 2px;
}

.nav-item:hover .nav-icon,
.nav-item:hover .nav-label,
.nav-item.active .nav-icon,
.nav-item.active .nav-label {
  color: #ffffff;
}

.nav-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 10px;
  height: 10px;
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  border-radius: 50%;
  border: 2px solid #0089ff;
}

.nav-footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4px 0 12px 0;
  gap: 2px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 8px;
  background-color: rgba(255, 255, 255, 0.2);

  &:hover {
    background-color: rgba(255, 255, 255, 0.3);
  }

  &.active {
    background-color: #ffffff;
  }
}

.avatar-text {
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
  transition: all 0.2s ease;
}

.user-avatar.active .avatar-text {
  color: #0089ff;
}
</style>
