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
          :size="42"
          shape="square"
          custom-class="header-avatar"
        />
        <span v-if="session?.type !== 'GROUP' && isOnline" class="online-indicator"></span>
        <!-- 在线脉冲动画 -->
        <span v-if="session?.type !== 'GROUP' && isOnline" class="online-pulse"></span>
      </div>
      <div class="header-info">
        <h2 class="header-name">{{ session?.name }}</h2>
        <span v-if="session?.type === 'GROUP'" class="meta-info">
          <span class="material-icons-outlined meta-icon">people</span>
          {{ session?.memberCount || 0 }} 人
        </span>
        <span v-else class="meta-info" :class="{ online: isOnline }">
          <span v-if="!isTyping" class="material-icons-outlined meta-icon">
            {{ isOnline ? 'circle' : 'radio_button_unchecked' }}
          </span>
          <span v-if="!isTyping">{{ isOnline ? '在线' : '离线' }}</span>
          <!-- 输入状态显示 -->
          <span v-if="isTyping" class="typing-indicator">
            <span class="typing-dots">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </span>
            正在输入...
          </span>
        </span>
      </div>
      <div class="header-arrow">
        <span class="material-icons-outlined">chevron_right</span>
      </div>
    </div>
    <div class="header-actions">
      <!-- 多选模式按钮 -->
      <el-tooltip :content="isMultiSelectMode ? '取消多选' : '多选消息'" placement="bottom">
        <button class="action-btn" :class="{ active: isMultiSelectMode }" @click="handleToggleMultiSelect">
          <span class="material-icons-outlined">{{ isMultiSelectMode ? 'close' : 'check_circle_outline' }}</span>
        </button>
      </el-tooltip>

      <!-- 通话按钮组 -->
      <div class="call-buttons">
        <el-tooltip content="语音通话" placement="bottom">
          <button class="action-btn call-btn voice-call" @click="handleVoiceCall">
            <span class="material-icons-outlined">phone</span>
          </button>
        </el-tooltip>
        <el-tooltip content="视频通话" placement="bottom">
          <button class="action-btn call-btn video-call" @click="handleVideoCall">
            <span class="material-icons-outlined">videocam</span>
          </button>
        </el-tooltip>
      </div>

      <!-- 更多菜单 -->
      <el-dropdown trigger="click" @command="handleMenuCommand" placement="bottom-end">
        <button class="action-btn more-btn">
          <span class="material-icons-outlined">more_horiz</span>
        </button>
        <template #dropdown>
          <el-dropdown-menu class="header-dropdown">
            <!-- 搜索 -->
            <el-dropdown-item command="search" class="menu-item">
              <span class="material-icons-outlined item-icon">search</span>
              <span class="item-text">搜索聊天记录</span>
              <span class="item-shortcut">Ctrl+F</span>
            </el-dropdown-item>

            <!-- 文件 -->
            <el-dropdown-item command="files" class="menu-item">
              <span class="material-icons-outlined item-icon">folder_open</span>
              <span class="item-text">查看文件</span>
            </el-dropdown-item>

            <!-- 群公告 -->
            <el-dropdown-item v-if="session?.type === 'GROUP'" command="announcement" class="menu-item">
              <span class="material-icons-outlined item-icon">campaign</span>
              <span class="item-text">群公告</span>
              <span v-if="session?.hasAnnouncement" class="item-badge">新</span>
            </el-dropdown-item>

            <el-dropdown-item divided></el-dropdown-item>

            <!-- 置顶 -->
            <el-dropdown-item command="pin" class="menu-item" :class="{ 'is-active': session?.isPinned }">
              <span class="material-icons-outlined item-icon">
                {{ session?.isPinned ? 'push_pin' : 'push_pin' }}
              </span>
              <span class="item-text">{{ session?.isPinned ? '取消置顶' : '置顶会话' }}</span>
            </el-dropdown-item>

            <!-- 免打扰 -->
            <el-dropdown-item command="mute" class="menu-item" :class="{ 'is-active': session?.isMuted }">
              <span class="material-icons-outlined item-icon">
                {{ session?.isMuted ? 'notifications' : 'notifications_off' }}
              </span>
              <span class="item-text">{{ session?.isMuted ? '取消免打扰' : '消息免打扰' }}</span>
            </el-dropdown-item>

            <el-dropdown-item divided></el-dropdown-item>

            <!-- 清空 -->
            <el-dropdown-item command="clear" class="menu-item danger-item">
              <span class="material-icons-outlined item-icon">delete_outline</span>
              <span class="item-text">清空聊天记录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 详情按钮 -->
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
@use '@/styles/design-tokens.scss' as *;

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
  transition: all var(--dt-transition-base);
  position: relative;

  // 底部阴影
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(22, 119, 255, 0.1) 50%,
      transparent 100%
    );
    opacity: 0;
    transition: opacity var(--dt-transition-base);
  }

  &:hover::after {
    opacity: 1;
  }
}

// ============================================================================
// 左侧区域
// ============================================================================
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px 8px 8px;
  margin: -8px -12px -8px -8px;
  border-radius: var(--dt-radius-lg);
  transition: all 0.25s var(--dt-ease-out);
  position: relative;

  &:hover {
    background: var(--dt-bg-hover);

    .header-avatar-wrapper {
      transform: scale(1.05);
    }

    .header-arrow {
      opacity: 1;
      transform: translateX(4px);
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
  transition: transform 0.25s var(--dt-ease-out);
}

.header-avatar {
  border-radius: var(--dt-radius-md);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  :deep(.dingtalk-avatar) {
    border-radius: var(--dt-radius-md) !important;
  }
}

.group-avatar {
  width: 42px;
  height: 42px;
  border-radius: var(--dt-radius-md);
  background: linear-gradient(135deg, #1677ff 0%, #0e5fd9 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.25);

  .material-icons-outlined {
    font-size: 22px;
  }
}

// 在线状态指示器
.online-indicator {
  position: absolute;
  bottom: -1px;
  right: -1px;
  width: 12px;
  height: 12px;
  background: var(--dt-success-color);
  border: 2px solid var(--dt-bg-card);
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.2);
  z-index: 2;
}

// 在线脉冲动画
.online-pulse {
  position: absolute;
  bottom: -1px;
  right: -1px;
  width: 12px;
  height: 12px;
  background: var(--dt-success-color);
  border-radius: 50%;
  z-index: 1;
  animation: onlinePulse 2s ease-out infinite;
}

@keyframes onlinePulse {
  0% {
    transform: scale(1);
    opacity: 0.6;
  }
  100% {
    transform: scale(2.5);
    opacity: 0;
  }
}

.header-arrow {
  display: flex;
  align-items: center;
  color: var(--dt-text-tertiary);
  opacity: 0.5;
  transition: all 0.25s var(--dt-ease-out);

  .material-icons-outlined {
    font-size: 18px;
  }
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
  gap: 5px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
  font-weight: 500;

  .meta-icon {
    font-size: 12px;
    opacity: 0.7;
  }

  &.online {
    color: var(--dt-success-color);

    .meta-icon {
      opacity: 1;
    }
  }
}

// 输入状态指示器
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--dt-brand-color);
  font-weight: 500;

  .typing-dots {
    display: flex;
    align-items: center;
    gap: 3px;

    .dot {
      width: 4px;
      height: 4px;
      background: currentColor;
      border-radius: 50%;
      animation: typingBounce 1.4s ease-in-out infinite;

      &:nth-child(1) { animation-delay: 0s; }
      &:nth-child(2) { animation-delay: 0.16s; }
      &:nth-child(3) { animation-delay: 0.32s; }
    }
  }
}

@keyframes typingBounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

// ============================================================================
// 右侧操作区
// ============================================================================
.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-right: 4px;

  .el-dropdown {
    display: flex;
    align-items: center;
  }
}

// 通话按钮组
.call-buttons {
  display: flex;
  align-items: center;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-md);
  padding: 2px;
  margin: 0 4px;
}

.action-btn {
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-sm);
  padding: 0;
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s var(--dt-ease-out);
  position: relative;

  .material-icons-outlined {
    font-size: 20px;
    transition: transform 0.2s var(--dt-ease-out);
  }

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-brand-color);

    .material-icons-outlined {
      transform: scale(1.1);
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
  }

  // 通话按钮特殊样式
  &.call-btn {
    border-radius: var(--dt-radius-sm);

    &.voice-call:hover {
      color: #22c55e;
      background: rgba(34, 197, 94, 0.1);
    }

    &.video-call:hover {
      color: #3b82f6;
      background: rgba(59, 130, 246, 0.1);
    }
  }

  // 更多按钮
  &.more-btn:hover {
    background: var(--dt-bg-hover);
  }

  // 详情按钮
  &.info-btn {
    color: var(--dt-brand-color);
    background: var(--dt-brand-bg);
    width: 32px;
    height: 32px;

    &:hover {
      background: var(--dt-brand-light);
    }
  }
}

// ============================================================================
// 下拉菜单
// ============================================================================
:deep(.header-dropdown) {
  padding: 6px;
  min-width: 200px;
  border-radius: var(--dt-radius-lg);
  border: 1px solid var(--dt-border-light);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);

  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    font-size: 13px;
    color: var(--dt-text-primary);
    border-radius: var(--dt-radius-md);
    transition: all 0.2s var(--dt-ease-out);
    margin: 2px 0;

    .item-icon {
      font-size: 18px;
      color: var(--dt-text-secondary);
      transition: color 0.2s var(--dt-ease-out);
    }

    .item-text {
      flex: 1;
    }

    .item-shortcut {
      font-size: 11px;
      color: var(--dt-text-tertiary);
      background: var(--dt-bg-body);
      padding: 2px 6px;
      border-radius: 4px;
      font-family: monospace;
    }

    .item-badge {
      background: var(--dt-error-color);
      color: #fff;
      font-size: 10px;
      padding: 2px 6px;
      border-radius: 10px;
      font-weight: 500;
    }

    &:hover {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);

      .item-icon {
        color: var(--dt-brand-color);
      }

      .item-shortcut {
        background: rgba(22, 119, 255, 0.1);
        color: var(--dt-brand-color);
      }
    }

    &.is-active {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);

      .item-icon {
        color: var(--dt-brand-color);
      }
    }

    &.danger-item {
      color: var(--dt-error-color);

      .item-icon {
        color: var(--dt-error-color);
      }

      &:hover {
        background: var(--dt-error-bg);
        color: var(--dt-error-color);

        .item-icon {
          color: var(--dt-error-color);
        }
      }
    }

    // 分割线样式
    &.el-dropdown-menu__item--divided {
      margin-top: 6px;
      margin-bottom: 6px;

      &::before {
        margin: 0;
        height: 1px;
        background: var(--dt-border-light);
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
  color: #94a3b8;

  &:hover {
    background: var(--dt-bg-hover-dark);
  }

  &.info-btn {
    color: var(--dt-brand-color);
    background: rgba(22, 119, 255, 0.15);

    &:hover {
      background: rgba(22, 119, 255, 0.25);
    }
  }
}

.dark .online-indicator {
  border-color: var(--dt-bg-card-dark);
}

.dark .call-buttons {
  background: rgba(255, 255, 255, 0.05);
}

.dark :deep(.header-dropdown) {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);

  .el-dropdown-menu__item {
    color: var(--dt-text-primary-dark);

    .item-icon {
      color: var(--dt-text-secondary-dark);
    }

    .item-shortcut {
      background: rgba(255, 255, 255, 0.1);
      color: var(--dt-text-tertiary-dark);
    }

    &:hover {
      background: rgba(22, 119, 255, 0.15);
    }
  }
}
</style>
