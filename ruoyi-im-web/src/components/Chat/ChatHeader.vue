<template>
  <div class="chat-header">
    <div class="header-left" @click="handleShowDetail" role="button" tabindex="0">
      <div class="header-avatar-wrapper">
        <!-- 群组使用图标，单聊使用钉钉风格头像 -->
        <div v-if="session?.type === 'GROUP'" class="header-avatar group-avatar">
          <span class="material-icons-outlined">groups</span>
        </div>
        <DingtalkAvatar
          v-else
          :src="session?.avatar"
          :name="session?.name"
          :user-id="session?.targetId"
          :size="40"
          shape="square"
          custom-class="header-avatar"
        />
        <span v-if="session?.type !== 'GROUP' && isOnline" class="online-indicator"></span>
      </div>
      <div class="header-info">
        <h2 class="header-name">{{ session?.name }}</h2>
        <span v-if="session?.type === 'GROUP'" class="meta-info">
          <span class="material-icons-outlined meta-icon">people</span>
          {{ session?.memberCount || 0 }} 人
        </span>
        <span v-else class="meta-info" :class="{ online: isOnline }">
          <span v-if="!isTyping" class="material-icons-outlined meta-icon">circle</span>
          <span v-if="!isTyping">{{ isOnline ? '在线' : '离线' }}</span>
          <!-- 输入状态显示 -->
          <span v-if="isTyping" class="typing-indicator">
            <span class="material-icons-outlined typing-icon">edit</span>
            正在输入...
          </span>
        </span>
      </div>
    </div>
    <div class="header-actions">
      <!-- 多选模式按钮 -->
      <el-tooltip :content="isMultiSelectMode ? '取消多选' : '多选消息'" placement="bottom">
        <button class="action-btn" :class="{ active: isMultiSelectMode }" @click="handleToggleMultiSelect">
          <span class="material-icons-outlined">{{ isMultiSelectMode ? 'close' : 'check_circle_outline' }}</span>
        </button>
      </el-tooltip>
      <el-tooltip content="语音通话" placement="bottom">
        <button class="action-btn" @click="handleVoiceCall">
          <span class="material-icons-outlined">phone</span>
        </button>
      </el-tooltip>
      <el-tooltip content="视频通话" placement="bottom">
        <button class="action-btn" @click="handleVideoCall">
          <span class="material-icons-outlined">videocam</span>
        </button>
      </el-tooltip>
      <el-dropdown trigger="click" @command="handleMenuCommand" placement="bottom-end">
        <button class="action-btn">
          <span class="material-icons-outlined">more_horiz</span>
        </button>
        <template #dropdown>
          <el-dropdown-menu class="header-dropdown">
            <el-dropdown-item command="search">
              <span class="material-icons-outlined item-icon">search</span>
              搜索聊天记录
            </el-dropdown-item>
            <el-dropdown-item command="files">
              <span class="material-icons-outlined item-icon">folder</span>
              查看文件
            </el-dropdown-item>
            <el-dropdown-item v-if="session?.type === 'GROUP'" command="announcement">
              <span class="material-icons-outlined item-icon">campaign</span>
              群公告
            </el-dropdown-item>
            <el-dropdown-item divided command="pin">
              <span class="material-icons-outlined item-icon">
                {{ session?.isPinned ? 'push_pin' : 'push_pin' }}
              </span>
              {{ session?.isPinned ? '取消置顶' : '置顶会话' }}
            </el-dropdown-item>
            <el-dropdown-item command="mute">
              <span class="material-icons-outlined item-icon">
                {{ session?.isMuted ? 'notifications' : 'notifications_off' }}
              </span>
              {{ session?.isMuted ? '取消免打扰' : '消息免打扰' }}
            </el-dropdown-item>
            <el-dropdown-item divided command="clear" class="danger-item">
              <span class="material-icons-outlined item-icon">delete_outline</span>
              清空聊天记录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-tooltip content="详情" placement="bottom">
        <button class="action-btn info-btn" @click="$emit('toggle-sidebar')">
          <span class="material-icons-outlined">info</span>
        </button>
      </el-tooltip>
    </div>

    <!-- 用户详情抽屉 -->
    <UserDetailDrawer
      v-model="showUserDetail"
      :session="session"
      @send-message="handleSendMessage"
      @voice-call="handleVoiceCall"
      @video-call="handleVideoCall"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import UserDetailDrawer from './UserDetailDrawer.vue'

const props = defineProps({
  session: {
    type: Object,
    default: null,
    validator: (value) => {
      if (value === null) return true
      return typeof value.id === 'string' || typeof value.id === 'number'
    }
  },
  typingUsers: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['show-detail', 'voice-call', 'video-call', 'search', 'files', 'announcement', 'pin', 'mute', 'clear', 'toggle-sidebar', 'toggle-multi-select', 'clear-selection'])

// 多选模式状态
const isMultiSelectMode = ref(false)

// 切换多选模式
const handleToggleMultiSelect = () => {
  isMultiSelectMode.value = !isMultiSelectMode.value
  emit('toggle-multi-select', isMultiSelectMode.value)
  if (!isMultiSelectMode.value) {
    // 退出多选时，通知父组件清空选择
    emit('clear-selection')
  }
}

// 用户详情抽屉显示状态
const showUserDetail = ref(false)

// 获取在线状态
const store = useStore()
const isOnline = computed(() => {
  if (props.session?.type === 'GROUP') return false

  const userId = props.session?.targetId
  if (userId && store.state.im.contact.userStatus[userId]) {
    return store.state.im.contact.userStatus[userId] === 'online'
  }

  return props.session?.peerOnline ?? false
})

// 是否有用户正在输入
const isTyping = computed(() => {
  return props.typingUsers && props.typingUsers.length > 0
})

// 显示详情
const handleShowDetail = () => {
  showUserDetail.value = true
  emit('show-detail', props.session)
}

// 发送消息
const handleSendMessage = () => {
  ElMessage.success('已切换到聊天')
}

// 语音通话
const handleVoiceCall = () => {
  ElMessage.info('语音通话功能开发中')
  emit('voice-call', props.session)
}

// 视频通话
const handleVideoCall = () => {
  ElMessage.info('视频通话功能开发中')
  emit('video-call', props.session)
}

// 菜单命令处理
const handleMenuCommand = (command) => {
  switch (command) {
    case 'search':
      emit('search', props.session)
      break
    case 'files':
      emit('files', props.session)
      break
    case 'announcement':
      emit('announcement', props.session)
      break
    case 'pin':
      emit('pin', props.session)
      break
    case 'mute':
      emit('mute', props.session)
      break
    case 'clear':
      emit('clear', props.session)
      break
  }
}
</script>

<style scoped lang="scss">
// ============================================================================
// 容器
// ============================================================================
.chat-header {
  height: var(--dt-chat-header-height);
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: var(--dt-bg-card);
  flex-shrink: 0;
  z-index: 10;
  transition: background var(--dt-transition-base);
}

// ============================================================================
// 左侧区域
// ============================================================================
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  margin: -8px -12px;
  border-radius: var(--dt-radius-lg);
  transition: all var(--dt-transition-fast);

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    background: var(--dt-brand-color);
    opacity: 0;
    transition: opacity var(--dt-transition-fast);
  }

  &:hover {
    background: var(--dt-bg-hover);

    &::after {
      opacity: 0.06;
    }

    .header-avatar-wrapper {
      transform: scale(1.05);
    }
  }

  &:focus-visible {
    outline: 2px solid var(--dt-brand-color);
    outline-offset: 2px;
  }

  &:active {
    transform: scale(0.98);
  }
}

.header-avatar-wrapper {
  position: relative;
  transition: transform var(--dt-transition-base);
}

.header-avatar {
  border-radius: var(--dt-radius-md);

  :deep(.dingtalk-avatar) {
    border-radius: var(--dt-radius-md) !important;
  }
}

.group-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--dt-radius-md);
  background: linear-gradient(135deg, #1677ff 0%, #0e5fd9 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.2);
}

// 在线状态指示器
.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background: var(--dt-success-color);
  border: 2px solid var(--dt-bg-card);
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.2);
  z-index: 1;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.header-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
  line-height: 1.3;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
  font-weight: 500;

  .meta-icon {
    font-size: 11px;
    opacity: 0.8;
  }

  &.online {
    color: var(--dt-success-color);
  }
}

// ============================================================================
// 右侧操作区
// ============================================================================
.header-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-right: 4px;

  .el-dropdown {
    display: flex;
    align-items: center;
  }
}

.action-btn {
  width: 38px;
  height: 38px;
  background: transparent;
  border: none;
  border-radius: 6px;
  padding: 0;
  color: #3b4252;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--dt-transition-fast);
  position: relative;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    background: var(--dt-brand-color);
    opacity: 0;
    transition: opacity var(--dt-transition-fast);
  }

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-brand-color);

    &::after {
      opacity: 0.08;
    }
  }

  &:active {
    transform: scale(0.95);
  }

  &:focus-visible {
    outline: 2px solid var(--dt-brand-color);
    outline-offset: 2px;
  }

  &.active {
    color: var(--dt-brand-color);
    background: var(--dt-brand-bg);

    &::after {
      opacity: 1;
      background: var(--dt-brand-color);
    }
  }

  &.info-btn {
    color: var(--dt-brand-color);
    background: var(--dt-brand-bg);

    &::after {
      opacity: 1;
    }

    &:hover {
      background: var(--dt-brand-light);

      &::after {
        opacity: 0;
      }
    }
  }

  .material-icons-outlined {
    position: relative;
    z-index: 1;
  }
}

// ============================================================================
// 下拉菜单
// ============================================================================
:deep(.header-dropdown) {
  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 12px;
    font-size: 13px;
    color: #1f2329;

    .item-icon {
      font-size: 16px;
      color: #646a73;
    }

    &:hover {
      background: #f5f6f7;
      color: #1677ff;

      .item-icon {
        color: #1677ff;
      }
    }

    &.danger-item {
      color: var(--dt-error-color);

      .item-icon {
        color: var(--dt-error-color);
      }

      &:hover {
        background: var(--dt-error-bg);
      }
    }
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
.dark .chat-header {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .header-name {
  color: var(--dt-text-primary-dark);
}

.dark .meta-info {
  color: var(--dt-text-tertiary-dark);

  &.online {
    color: var(--dt-success-color);
  }
}

.dark .header-left:hover {
  background: var(--dt-bg-hover-dark);
}

.dark .action-btn {
  color: #bdc3c9;

  &:hover {
    background: var(--dt-bg-hover-dark);
  }

  &.info-btn {
    color: var(--dt-brand-color);
    background: rgba(22, 119, 255, 0.1);

    &:hover {
      background: rgba(22, 119, 255, 0.2);
    }
  }
}

.dark .online-indicator {
  border-color: var(--dt-bg-card-dark);
}
</style>
