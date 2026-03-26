<template>
  <div
    class="message-item"
    :class="{ 'is-own': message.isOwn, 'is-failed': message.status === 'failed', 'is-highlighted': highlighted }"
    @contextmenu.prevent="handleContextMenu"
  >
    <!-- 头像 - 使用 DingtalkAvatar 组件 -->
    <DingtalkAvatar
      :src="message.senderAvatar"
      :name="message.senderName"
      :user-id="message.senderId"
      :size="avatarSize"
      shape="square"
      class="avatar"
      :class="{ 'avatar-shift': isGroupSession }"
      @click="$emit('show-user', message.senderId)"
    />

    <div class="content">
      <!-- 昵称和时间 (对方才显示) -->
      <div v-if="!message.isOwn && isGroupSession" class="message-header">
        <span class="nickname">{{ message.senderName }}</span>
      </div>

      <!-- 消息主区 (气泡) -->
      <div class="bubble-wrapper">
        <slot name="bubble" />

        <div class="message-actions">
          <button class="action-btn" @click.stop="emitCommand('reply')">
            回复
          </button>
          <button class="action-btn" @click.stop="emitCommand('forward')">
            转发
          </button>
          <button v-if="message.isOwn" class="action-btn danger" @click.stop="emitCommand('recall')">
            撤回
          </button>
        </div>

        <!-- 接收方时间戳 -->
        <span v-if="!message.isOwn" class="time-received">{{ formatTime(message.timestamp) }}</span>
      </div>
    </div>

    <!-- 己方消息时间与状态：放在头像之后更贴钉钉 -->
    <span v-if="message.isOwn" class="meta-after-avatar-root">
      <span class="time-self">{{ formatTime(message.timestamp) }}</span>
      <span class="status-inline">
        <template v-if="message.status === 'sending'">
          <el-icon class="is-loading">
            <Loading />
          </el-icon>
        </template>
        <template v-else-if="message.status === 'failed'">
          <span class="failed-indicator" title="点击重试" @click.stop="handleRetry">
            <el-icon><WarningFilled /></el-icon>
          </span>
        </template>
        <template v-else>
          <span class="status-text" :class="{ 'is-read': message.readCount > 0 }">
            {{ message.readCount > 0 ? '已读' : '送达' }}
          </span>
        </template>
      </span>
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Loading, WarningFilled } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({ message: Object, sessionType: String, highlighted: Boolean })
const emit = defineEmits(['show-user', 'retry', 'command', 'context'])

const isGroupSession = computed(() => props.sessionType === 'GROUP')
const avatarSize = computed(() => (isGroupSession.value ? 32 : 36))

const handleRetry = () => {
  emit('retry', props.message)
}

const emitCommand = (cmd) => {
  emit('command', cmd, props.message)
}

const handleContextMenu = (e) => {
  emit('context', { x: e.clientX, y: e.clientY, message: props.message })
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()

  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')

  if (isToday) {
    return `${hours}:${minutes}`
  }

  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}-${day} ${hours}:${minutes}`
}
</script>

<style scoped lang="scss">
.message-item {
  display: flex;
  padding: var(--dt-spacing-xs) var(--dt-spacing-lg);
  gap: var(--dt-spacing-sm);
  min-width: 0;
  align-items: flex-start;

  &.is-own {
    justify-content: flex-end;
    .content {
      order: 1;
      align-items: flex-end;
    }
    .avatar {
      order: 2;
      margin-left: 0;
    }
    .bubble-wrapper {
      flex-direction: row;
    }
    .time-self {
      margin-left: var(--dt-spacing-xs);
      margin-right: 0;
    }
  }
  &.is-failed {
    .message-bubble {
      border-color: var(--dt-error-color);
      background: var(--dt-error-bg);
    }
  }
}

.message-item.is-highlighted .message-bubble {
  box-shadow: 0 0 0 2px var(--dt-brand-bg-dark);
}

.avatar {
  cursor: pointer;
  flex-shrink: 0;
  width: var(--dt-avatar-size-md, 36px);
  height: var(--dt-avatar-size-md, 36px);
  border-radius: var(--dt-radius-sm);
}

.avatar.avatar-shift {
  margin-top: 2px;
}

.content {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-xs);
  max-width: calc(100% - var(--dt-avatar-size-md, 36px) - var(--dt-spacing-md));
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  margin-bottom: 2px;

  .nickname {
    font-size: var(--dt-font-size-sm);
    font-weight: var(--dt-font-weight-medium);
    color: var(--dt-text-secondary);
  }
}

.bubble-wrapper {
  display: flex;
  align-items: flex-start;
  position: relative;
  min-width: 0;
  gap: 6px;
}

.meta-after-avatar-root {
  display: inline-flex;
  align-items: center;
  margin-left: 4px;
  opacity: 0;
  transition: opacity var(--dt-transition-fast);
}

.meta-after-avatar-root .time-self {
  margin: 0;
}

.message-actions {
  position: absolute;
  top: calc(-1 * var(--dt-spacing-lg, 16px));
  right: 0;
  display: flex;
  gap: var(--dt-spacing-xs);
  opacity: 0;
  pointer-events: none;
  transition: opacity var(--dt-transition-fast);
}

.action-btn {
  border: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
  color: var(--dt-text-secondary);
  font-size: var(--dt-font-size-xs);
  padding: 2px 6px;
  border-radius: var(--dt-radius-sm);
  cursor: pointer;
  transition: all var(--dt-transition-fast);
}

.action-btn:hover {
  color: var(--dt-brand-color);
  border-color: var(--dt-brand-color);
  background: var(--dt-brand-bg);
}

.action-btn:active {
  transform: scale(0.96);
}

.action-btn.danger:hover {
  color: var(--dt-error-color);
  border-color: var(--dt-error-color);
  background: var(--dt-error-bg);
}

.message-item:hover .message-actions {
  opacity: 1;
  pointer-events: auto;
}

.message-item.is-own .meta-after-avatar-root {
  order: 3;
  align-self: flex-end;
}

.message-item:hover .meta-after-avatar-root {
  opacity: 1;
}

.message-item:not(.is-own) .meta-after-avatar-root {
  display: none;
}

.time-received {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-quaternary);
  margin-left: var(--dt-spacing-xs);
  align-self: flex-end;
  margin-bottom: var(--dt-spacing-xs);
}

.time-self {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-quaternary);
  align-self: flex-end;
  margin-bottom: var(--dt-spacing-xs);
}

.status-inline {
  display: inline-flex;
  align-items: center;
  margin-left: var(--dt-spacing-xs);

  .is-loading {
    font-size: 12px;
    color: var(--dt-text-quaternary);
  }

  .failed-indicator {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 14px;
    height: 14px;
    background: var(--dt-error-color);
    border-radius: 50%;
    cursor: pointer;
    transition: all var(--dt-transition-fast);

    .el-icon {
      font-size: 9px;
      color: var(--dt-text-white);
    }
  }

  .failed-indicator:hover {
    transform: scale(1.1);
  }

  .failed-indicator:active {
    transform: scale(0.95);
  }

  .status-text {
    font-size: var(--dt-font-size-xs);
    color: var(--dt-text-quaternary);
  }

  .status-text.is-read {
    color: var(--dt-brand-color);
  }
}

// 暗色模式适配
:global(.dark) {
  .message-header .nickname {
    color: var(--dt-text-primary-dark);
  }

  .time-received {
    color: var(--dt-text-quaternary-dark);
  }
}
</style>
