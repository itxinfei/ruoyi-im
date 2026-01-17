<template>
  <nav class="side-nav" :class="{ collapsed }">
    <div class="nav-section">
      <div
        v-for="item in navModules"
        :key="item.key"
        class="nav-item"
        :class="{ active: activeModule === item.key }"
        @click="handleModuleClick(item.key)"
      >
        <el-tooltip :content="item.label" :placement="collapsed ? 'right' : 'top'">
          <el-icon :size="22" class="nav-icon">
            <component :is="item.icon" />
          </el-icon>
        </el-tooltip>
        <!-- 未读红点 -->
        <div
          v-if="item.unreadCount > 0"
          class="nav-badge"
          :class="{ 'has-count': item.unreadCount > 9 }"
        >
          {{ item.unreadCount > 99 ? '99+' : item.unreadCount }}
        </div>
      </div>
    </div>

    <!-- AI助理特殊项 -->
    <div class="nav-section">
      <div
        class="nav-item ai-item"
        :class="{ active: activeModule === 'ai-assistant' }"
        @click="handleModuleClick('ai-assistant')"
      >
        <el-tooltip content="AI助理" :placement="collapsed ? 'right' : 'top'">
          <el-icon :size="22" class="nav-icon ai-icon">
            <MagicStick />
          </el-icon>
        </el-tooltip>
      </div>
    </div>

    <!-- 底部设置 -->
    <div class="nav-footer">
      <el-tooltip :content="设置" placement="right">
        <div class="nav-item" @click="handleSettingsClick">
          <el-icon :size="18" class="nav-icon">
            <Setting />
          </el-icon>
        </div>
      </el-tooltip>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import {
  ChatDotRound, Message, UserFilled, Folder, Calendar,
  Files, Tickets, OfficeBuilding, AppFilled, MagicStick, Setting
} from '@element-plus/icons-vue'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  },
  activeModule: {
    type: String,
    default: 'chat'
  }
})

const emit = defineEmits(['update:collapsed', 'switch-module'])

const store = useStore()

// 导航模块
const navModules = computed(() => {
  return [
    {
      key: 'chat',
      label: '消息',
      icon: ChatDotRound,
      unreadCount: store.state.im.unreadCount || 0
    },
    {
      key: 'contacts',
      label: '联系人',
      icon: UserFilled,
      unreadCount: 0
    },
    {
      key: 'workbench',
      label: '工作台',
      icon: Calendar,
      unreadCount: 0
    },
    {
      key: 'drive',
      label: '文件',
      icon: Folder,
      unreadCount: 0
    },
    {
      key: 'approval',
      label: '审批',
      icon: Tickets,
      unreadCount: 0
    },
    {
      key: 'ding',
      label: '钉钉',
      icon: OfficeBuilding,
      unreadCount: 0
    },
    {
      key: 'email',
      label: '邮箱',
      icon: Message,
      unreadCount: 0
    },
    {
      key: 'app-center',
      label: '应用',
      icon: AppFilled,
      unreadCount: 0
    }
  ]
})

// 处理模块点击
const handleModuleClick = (moduleKey) => {
  emit('switch-module', moduleKey)
}

// 处理设置点击
const handleSettingsClick = () => {
  emit('switch-module', 'settings')
}
</script>

<style scoped lang="scss">
.side-nav {
  display: flex;
  flex-direction: column;
  width: 60px;
  height: 100%;
  background: #ffffff;
  border-right: 1px solid #f0f0f0;
  flex-shrink: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  .nav-section {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 8px 0;

    .nav-item {
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 48px;
      height: 48px;
      margin: 0 auto;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      font-size: 16px;
      color: #8c8c8c;

      &:hover {
        background: rgba(0, 0, 0, 0.04);
        transform: scale(1.05);
      }

      &.active {
        background: rgba(22, 119, 255, 0.1);
        color: #1677ff;
        position: relative;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 20px;
          background: #1677ff;
          border-radius: 2px;
          transition: all 0.2s ease;
        }
      }

      &.ai-item {
        .nav-icon {
          background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
          color: #722ed1;
        }

        &.active::before {
          background: linear-gradient(180deg, #722ed1 0%, #9254de 100%);
        }

        &:hover .nav-icon {
          transform: scale(1.1);
        }
      }

      .nav-badge {
        position: absolute;
        top: 4px;
        right: 4px;
        min-width: 16px;
        height: 16px;
        padding: 0 4px;
        background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
        color: #ffffff;
        font-size: 10px;
        font-weight: 600;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 2px solid #ffffff;
        box-shadow: 0 2px 6px rgba(255, 77, 79, 0.3);
        animation: badgePop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);

        &.has-count {
          padding: 0 2px;
        }
      }

      .nav-icon {
        font-size: 22px;
        transition: transform 0.2s ease;
      }
    }
  }

  .nav-footer {
    margin-top: auto;
    padding: 8px 0;

    .nav-item {
      width: 44px;
      height: 44px;
      margin: 0 auto;
      color: #8c8c8c;
    }
  }

  // 折叠状态
  &.collapsed {
    .nav-item {
      width: 44px;
      height: 44px;
    }
  }
}

// 徽章动画
@keyframes badgePop {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
