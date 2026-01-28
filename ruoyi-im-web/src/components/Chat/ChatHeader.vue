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
      <!-- 通话按钮组 -->
      <div class="call-buttons">
        <el-tooltip content="语音通话" placement="bottom">
          <button class="action-btn call-btn voice-call" @click="handleVoiceCall">
            <div class="btn-icon-wrapper">
              <span class="material-icons-outlined">phone</span>
              <div class="icon-glow voice-glow"></div>
            </div>
          </button>
        </el-tooltip>
        <el-tooltip content="视频通话" placement="bottom">
          <button class="action-btn call-btn video-call" @click="handleVideoCall">
            <div class="btn-icon-wrapper">
              <span class="material-icons-outlined">videocam</span>
              <div class="icon-glow video-glow"></div>
            </div>
          </button>
        </el-tooltip>
      </div>

      <!-- 更多菜单 -->
      <el-dropdown trigger="click" @command="handleMenuCommand" :placement="menuPlacement">
        <button class="action-btn more-btn">
          <span class="material-icons-outlined">more_horiz</span>
        </button>
        <template #dropdown>
          <el-dropdown-menu class="header-dropdown">
            <!-- 聊天记录 -->
            <el-dropdown-item command="history" class="menu-item">
              <span class="material-icons-outlined item-icon">history</span>
              <span class="item-text">聊天记录</span>
            </el-dropdown-item>

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

            <!-- 消息速读 -->
            <el-dropdown-item command="summary" class="menu-item">
              <span class="material-icons-outlined item-icon">auto_awesome</span>
              <span class="item-text">消息速读</span>
              <span class="item-shortcut">Ctrl+S</span>
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
    </div>

    <!-- 用户详情抽屉 -->
    <UserDetailDrawer
      v-model="showUserDetail"
      :session="session"
      @send-message="handleSendMessage"
      @voice-call="handleVoiceCall"
      @video-call="handleVideoCall"
    />

    <!-- 消息速读弹窗 -->
    <MessageSummary
      v-model:visible="showSummary"
      :session-id="session?.id"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import UserDetailDrawer from './UserDetailDrawer.vue'
import MessageSummary from './MessageSummary.vue'

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

const emit = defineEmits(['show-detail', 'voice-call', 'video-call', 'history', 'search', 'files', 'announcement', 'pin', 'mute', 'clear', 'toggle-sidebar'])

// 用户详情抽屉显示状态
const showUserDetail = ref(false)

// 消息速读弹窗显示状态
const showSummary = ref(false)

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
    case 'history':
      emit('history', props.session)
      break
    case 'search':
      emit('search', props.session)
      break
    case 'files':
      emit('files', props.session)
      break
    case 'summary':
      showSummary.value = true
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

// 计算菜单弹出位置，确保菜单不会超出视口
const menuPlacement = computed(() => {
  const viewportWidth = window.innerWidth
  const viewportHeight = window.innerHeight
  const isSmallScreen = viewportWidth < 768
  const isShortScreen = viewportHeight < 600
  
  if (isSmallScreen) {
    return 'bottom-start'
  }
  
  if (isShortScreen) {
    return 'bottom-end'
  }
  
  return 'bottom-end'
})
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
    width: 40px;
    height: 40px;
    border-radius: 12px;
    position: relative;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    .btn-icon-wrapper {
      position: relative;
      z-index: 1;
    }

    .icon-glow {
      position: absolute;
      inset: -8px;
      border-radius: 50%;
      opacity: 0;
      transition: opacity 0.3s;
      pointer-events: none;
    }

    &.voice-call {
      background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
      color: #22c55e;
      border: 1px solid rgba(34, 197, 94, 0.2);

      .voice-glow {
        background: radial-gradient(circle, rgba(34, 197, 94, 0.3) 0%, transparent 70%);
      }

      &:hover {
        background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
        color: #fff;
        transform: translateY(-2px) scale(1.05);
        box-shadow: 0 6px 20px rgba(34, 197, 94, 0.4);

        .icon-glow {
          opacity: 1;
        }

        .material-icons-outlined {
          transform: scale(1.1);
        }
      }

      &:active {
        transform: translateY(0) scale(0.98);
      }
    }

    &.video-call {
      background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
      color: #3b82f6;
      border: 1px solid rgba(59, 130, 246, 0.2);

      .video-glow {
        background: radial-gradient(circle, rgba(59, 130, 246, 0.3) 0%, transparent 70%);
      }

      &:hover {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        color: #fff;
        transform: translateY(-2px) scale(1.05);
        box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);

        .icon-glow {
          opacity: 1;
        }

        .material-icons-outlined {
          transform: scale(1.1);
        }
      }

      &:active {
        transform: translateY(0) scale(0.98);
      }
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
// 下拉菜单 - 现代化设计
// ============================================================================
:deep(.header-dropdown) {
  padding: 0;
  min-width: 240px;
  max-width: 280px;
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.05),
    0 10px 30px -5px rgba(0, 0, 0, 0.08),
    0 20px 50px -10px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(20px) saturate(180%);
  background: rgba(255, 255, 255, 0.95);
  animation: menuSlideIn 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  overflow: hidden;

  // 顶部装饰条
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #1677ff 0%, #52c41a 50%, #ff4d4f 100%);
    opacity: 0;
    transition: opacity 0.3s;
  }

  &:hover::before {
    opacity: 1;
  }

  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    font-size: 14px;
    color: var(--dt-text-primary);
    border-radius: 0;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    margin: 0;
    position: relative;
    overflow: hidden;

    // 菜单项背景动画
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 3px;
      background: var(--dt-brand-color);
      transform: scaleY(0);
      transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    }

    .item-icon {
      font-size: 20px;
      color: var(--dt-text-tertiary);
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 24px;
      height: 24px;
    }

    .item-text {
      flex: 1;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      font-weight: 500;
    }

    .item-shortcut {
      font-size: 11px;
      color: var(--dt-text-quaternary);
      background: linear-gradient(135deg, rgba(0, 0, 0, 0.04) 0%, rgba(0, 0, 0, 0.02) 100%);
      padding: 3px 8px;
      border-radius: 6px;
      font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
      font-weight: 500;
      flex-shrink: 0;
      border: 1px solid rgba(0, 0, 0, 0.04);
      letter-spacing: 0.5px;
    }

    .item-badge {
      background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
      color: #fff;
      font-size: 10px;
      padding: 3px 8px;
      border-radius: 12px;
      font-weight: 600;
      flex-shrink: 0;
      animation: badgePulse 2s ease-in-out infinite;
      box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3);
    }

    &:hover {
      background: linear-gradient(90deg, rgba(22, 119, 255, 0.06) 0%, rgba(22, 119, 255, 0.02) 100%);
      color: var(--dt-brand-color);
      transform: translateX(4px);

      &::before {
        transform: scaleY(1);
      }

      .item-icon {
        color: var(--dt-brand-color);
        transform: scale(1.1);
      }

      .item-shortcut {
        background: linear-gradient(135deg, rgba(22, 119, 255, 0.12) 0%, rgba(22, 119, 255, 0.06) 100%);
        color: var(--dt-brand-color);
        border-color: rgba(22, 119, 255, 0.15);
        transform: scale(1.05);
      }
    }

    &:active {
      transform: translateX(4px) scale(0.98);
    }

    &.is-active {
      background: linear-gradient(90deg, rgba(22, 119, 255, 0.1) 0%, rgba(22, 119, 255, 0.03) 100%);
      color: var(--dt-brand-color);

      &::before {
        transform: scaleY(1);
      }

      .item-icon {
        color: var(--dt-brand-color);
      }

      .item-text {
        font-weight: 600;
      }
    }

    &.danger-item {
      color: var(--dt-error-color);

      .item-icon {
        color: var(--dt-error-color);
      }

      &::before {
        background: var(--dt-error-color);
      }

      &:hover {
        background: linear-gradient(90deg, rgba(255, 77, 79, 0.08) 0%, rgba(255, 77, 79, 0.02) 100%);
        color: #d9363e;
        transform: translateX(4px);

        &::before {
          transform: scaleY(1);
        }

        .item-icon {
          color: #d9363e;
          transform: scale(1.1);
        }
      }
    }

    // 分割线样式
    &.el-dropdown-menu__item--divided {
      position: relative;
      margin-top: 4px;
      margin-bottom: 4px;

      &::after {
        content: '';
        position: absolute;
        left: 52px;
        right: 16px;
        top: 0;
        height: 1px;
        background: linear-gradient(90deg, transparent 0%, rgba(0, 0, 0, 0.08) 50%, transparent 100%);
      }

      &::before {
        display: none;
      }
    }
  }
}

// 菜单滑入动画
@keyframes menuSlideIn {
  from {
    opacity: 0;
    transform: translateY(-12px) scale(0.92);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

// 徽章脉冲动画
@keyframes badgePulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
    box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3);
  }
  50% {
    opacity: 0.85;
    transform: scale(1.08);
    box-shadow: 0 4px 12px rgba(255, 77, 79, 0.5);
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
.dark .chat-header {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);

  &::after {
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(22, 119, 255, 0.15) 50%,
      transparent 100%
    );
  }
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

  &.call-btn {
    &.voice-call {
      background: linear-gradient(135deg, rgba(34, 197, 94, 0.15) 0%, rgba(34, 197, 94, 0.1) 100%);
      color: #4ade80;
      border-color: rgba(34, 197, 94, 0.3);

      &:hover {
        background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
        color: #fff;
      }
    }

    &.video-call {
      background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(59, 130, 246, 0.1) 100%);
      color: #60a5fa;
      border-color: rgba(59, 130, 246, 0.3);

      &:hover {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        color: #fff;
      }
    }
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
  background: rgba(30, 41, 59, 0.95);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.2),
    0 10px 30px -5px rgba(0, 0, 0, 0.3),
    0 20px 50px -10px rgba(0, 0, 0, 0.2);

  &::before {
    background: linear-gradient(90deg, #1677ff 0%, #52c41a 50%, #ff4d4f 100%);
  }

  .el-dropdown-menu__item {
    color: var(--dt-text-primary-dark);

    &::before {
      background: var(--dt-brand-color);
    }

    .item-icon {
      color: var(--dt-text-tertiary-dark);
    }

    .item-text {
      font-weight: 500;
    }

    .item-shortcut {
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.08) 0%, rgba(255, 255, 255, 0.04) 100%);
      color: var(--dt-text-quaternary-dark);
      border-color: rgba(255, 255, 255, 0.08);
    }

    .item-badge {
      background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
      box-shadow: 0 2px 8px rgba(255, 77, 79, 0.4);
    }

    &:hover {
      background: linear-gradient(90deg, rgba(22, 119, 255, 0.15) 0%, rgba(22, 119, 255, 0.05) 100%);
      color: var(--dt-brand-color);

      .item-icon {
        color: var(--dt-brand-color);
      }

      .item-shortcut {
        background: linear-gradient(135deg, rgba(22, 119, 255, 0.2) 0%, rgba(22, 119, 255, 0.1) 100%);
        color: var(--dt-brand-color);
        border-color: rgba(22, 119, 255, 0.25);
      }
    }

    &.is-active {
      background: linear-gradient(90deg, rgba(22, 119, 255, 0.2) 0%, rgba(22, 119, 255, 0.08) 100%);
      color: var(--dt-brand-color);

      .item-icon {
        color: var(--dt-brand-color);
      }
    }

    &.danger-item {
      color: #f87171;

      .item-icon {
        color: #f87171;
      }

      &::before {
        background: #ff4d4f;
      }

      &:hover {
        background: linear-gradient(90deg, rgba(255, 77, 79, 0.15) 0%, rgba(255, 77, 79, 0.05) 100%);
        color: #f87171;

        .item-icon {
          color: #f87171;
        }
      }
    }

    &.el-dropdown-menu__item--divided {
      &::after {
        background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.1) 50%, transparent 100%);
      }
    }
  }
}

// ============================================================================
// 响应式设计
// ============================================================================
@media (max-width: 768px) {
  .chat-header {
    padding: 0 12px;

    .header-name {
      font-size: 14px;
    }

    .meta-info {
      font-size: 11px;
    }
  }

  .header-left {
    gap: 8px;
    padding: 6px 8px 6px 6px;
  }

  .header-avatar {
    width: 38px !important;
    height: 38px !important;
  }

  .group-avatar {
    width: 38px;
    height: 38px;

    .material-icons-outlined {
      font-size: 20px;
    }
  }

  .action-btn {
    width: 34px;
    height: 34px;

    &.call-btn {
      width: 36px;
      height: 36px;
    }

    .material-icons-outlined {
      font-size: 18px;
    }
  }

  :deep(.header-dropdown) {
    min-width: 200px;
    max-width: 240px;

    .el-dropdown-menu__item {
      padding: 10px 14px;
      font-size: 13px;
      gap: 10px;

      .item-icon {
        font-size: 18px;
        width: 22px;
        height: 22px;
      }

      .item-shortcut {
        display: none; // 移动端隐藏快捷键提示
      }

      &.el-dropdown-menu__item--divided {
        &::after {
          left: 46px;
          right: 14px;
        }
      }
    }
  }
}

@media (max-width: 480px) {
  .chat-header {
    padding: 0 8px;
  }

  .header-arrow {
    display: none;
  }

  .call-buttons {
    margin: 0 2px;
    padding: 2px;
  }

  :deep(.header-dropdown) {
    .el-dropdown-menu__item {
      padding: 10px 12px;

      &.el-dropdown-menu__item--divided {
        &::after {
          left: 42px;
          right: 12px;
        }
      }
    }
  }
}
</style>
