<template>
  <aside
    class="nav-sidebar"
    :style="{ width: navWidth + 'px' }"
  >
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
          <span
            v-if="item.key === 'chat' && unreadCount > 0"
            class="nav-dot"
          />
        </div>
      </el-tooltip>
    </nav>

    <div class="nav-footer">
      <div
        class="user-avatar"
        :class="{ active: activeModule === 'profile' }"
        @click="handleNavClick('profile')"
      >
        <DingtalkAvatar
          :src="currentUser.avatar"
          :name="currentUser.nickname || currentUser.username || '我'"
          :user-id="currentUser.id"
          :size="40"
          shape="circle"
          custom-class="nav-avatar"
        />
      </div>
    </div>
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
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  activeModule: {
    type: String,
    default: 'chat'
  }
})

const emit = defineEmits(['switchModule'])
const store = useStore()
const navWidth = ref(64)
const unreadCount = computed(() => store.state.im.totalUnreadCount)
const currentUser = computed(() => store.getters['user/currentUser'])

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
  }
])

const handleNavClick = moduleKey => {
  emit('switchModule', moduleKey)
}
</script>

<style scoped>
.nav-sidebar {
  display: flex;
  flex-direction: column;
  width: 60px;  // 钉钉标准：60px
  min-width: 60px;
  max-width: 60px;
  flex-shrink: 0;
  background-color: #0089ff;
  border-right: 1px solid #e8e8e8;
  height: 100%;
}

.logo-area {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  margin-bottom: 4px;
}

.logo-box {
  width: 40px;  // 钉钉标准：40px
  height: 40px;
  background-color: var(--dt-bg-overlay-light);  // 半透明白色
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 8px auto;  // 居中，上下8px间距
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
  padding: 8px 0;
  gap: 4px;
  flex: 1;
  overflow-y: auto;
}

.nav-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;  // 填满父容器宽度（60px）
  height: 40px;  // 钉钉标准：40px
  margin: 0 auto;  // 居中（虽然width是100%，但保留以确保）
  border-radius: var(--dt-radius-md);  // 钉钉标准：8px 圆角
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background-color: var(--dt-bg-overlay-lighter);
    transform: scale(1.05);
  }

  &.active {
    background-color: var(--dt-bg-overlay-medium);
    box-shadow: var(--dt-shadow-md);
  }

  &.active::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 20px;
    background-color: #ffffff;
    border-radius: 0 2px 2px 0;
    box-shadow: var(--dt-glow-sm);
  }
}

.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: var(--dt-text-tertiary);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  line-height: 1;  // 确保图标垂直居中
}

.nav-label {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: var(--dt-text-tertiary);
  line-height: 1;
  font-weight: 500;
  letter-spacing: 0.3px;
  margin-top: 2px;  // 图标和文字之间的间距
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
  border-radius: var(--dt-radius-full);
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
  border-radius: var(--dt-radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 8px;
  background-color: var(--dt-bg-overlay-light);

  &:hover {
    background-color: var(--dt-bg-overlay-medium);
  }

  &.active {
    background-color: #ffffff;
  }
}

/* 导航头像组件样式覆盖 */
.user-avatar.active :deep(.nav-avatar .avatar-text) {
  color: #0089ff;
}
</style>
