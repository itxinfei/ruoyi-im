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
          <span class="material-icons-outlined meta-icon">circle</span>
          {{ isOnline ? '在线' : '离线' }}
        </span>
      </div>
    </div>
    <div class="header-actions">
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
  }
})

const emit = defineEmits(['show-detail', 'voice-call', 'video-call', 'search', 'pin', 'mute', 'clear', 'toggle-sidebar'])

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
      ElMessage.info('查看文件功能开发中')
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
  padding: 0 16px;
  background: var(--dt-bg-card);
  flex-shrink: 0;
  z-index: 10;
}

// ============================================================================
// 左侧区域
// ============================================================================
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 6px;
  margin: -6px;
  border-radius: var(--dt-radius-md);
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-body);

    .header-avatar-wrapper {
      transform: scale(1.05);
    }
  }

  &:focus-visible {
    outline: 2px solid var(--dt-brand-color);
    outline-offset: 2px;
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

  .material-icons-outlined {
    font-size: 20px;
  }
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
  gap: 2px;
}

.header-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
  line-height: 1.2;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
  font-weight: 500;

  .meta-icon {
    font-size: 10px;
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
  gap: 4px;
}

.action-btn {
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-md);
  padding: 0;
  color: var(--dt-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-body);
    color: var(--dt-brand-color);
  }

  &:focus-visible {
    outline: 2px solid var(--dt-brand-color);
    outline-offset: 2px;
  }

  &.info-btn {
    color: var(--dt-brand-color);
    background: var(--dt-brand-bg);

    &:hover {
      background: var(--dt-brand-light);
    }
  }

  .material-icons-outlined {
    font-size: 20px;
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

    .item-icon {
      font-size: 16px;
      color: var(--dt-text-secondary);
    }

    &:hover .item-icon {
      color: var(--dt-brand-color);
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
  color: var(--dt-text-secondary-dark);

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
