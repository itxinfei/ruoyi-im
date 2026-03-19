<template>
  <div class="message-item" :class="{ 'is-own': message.isOwn, 'is-failed': message.status === 'failed' }">
    <!-- 头像 - 使用 DingtalkAvatar 组件 -->
    <DingtalkAvatar
      :src="message.senderAvatar"
      :name="message.senderName"
      :user-id="message.senderId"
      :size="40"
      shape="square"
      class="avatar"
      @click="$emit('show-user', message.senderId)"
    />

    <div class="content">
      <!-- 昵称和时间 (对方才显示) -->
      <div v-if="!message.isOwn" class="message-header">
        <span class="nickname">{{ message.senderName }}</span>
        <span class="time">{{ formatTime(message.timestamp) }}</span>
      </div>

      <!-- 消息主区 (气泡) -->
      <div class="bubble-wrapper">
        <slot name="bubble" />

        <!-- 状态标识 (己方才显示) - 移至气泡内右下角 -->
        <div v-if="message.isOwn" class="status-badge">
          <template v-if="message.status === 'sending'">
            <el-icon class="is-loading">
              <Loading />
            </el-icon>
          </template>
          <template v-else-if="message.status === 'failed'">
            <span class="failed-indicator" @click.stop="handleRetry">
              <el-icon><WarningFilled /></el-icon>
            </span>
          </template>
          <template v-else>
            <span class="read-indicator" :class="{ 'is-read': message.readCount > 0 }">
              <svg viewBox="0 0 24 24" class="check-icon">
                <path v-if="message.readCount > 0" d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
                <path v-else d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
              </svg>
            </span>
          </template>
        </div>
        
        <!-- 己方消息时间 -->
        <span v-if="message.isOwn" class="time-self">{{ formatTime(message.timestamp) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Loading, WarningFilled } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({ message: Object })
const emit = defineEmits(['show-user', 'retry'])

const handleRetry = () => {
  emit('retry', props.message)
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
  padding: var(--dt-spacing-sm) var(--dt-spacing-lg);
  gap: var(--dt-spacing-md);
  min-width: 0;

  &.is-own {
    flex-direction: row-reverse;
    .content {
      align-items: flex-end;
    }
    .bubble-wrapper {
      flex-direction: row-reverse;
    }
    .time-self {
      margin-left: var(--dt-spacing-sm);
      margin-right: var(--dt-spacing-xs);
    }
  }

  &.is-failed {
    .message-bubble {
      border-color: var(--dt-error-color);
      background: var(--dt-error-bg);
    }
  }
}

.avatar { 
  cursor: pointer; 
  flex-shrink: 0; 
  border-radius: var(--dt-radius-sm);
}

.content {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-xs);
  max-width: 70%;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  
  .nickname {
    font-size: var(--dt-font-size-sm);
    font-weight: var(--dt-font-weight-medium);
    color: var(--dt-text-primary);
  }
  
  .time {
    font-size: var(--dt-font-size-xs);
    color: var(--dt-text-quaternary);
  }
}

.bubble-wrapper {
  display: flex;
  align-items: flex-end;
  position: relative;
  min-width: 0;
}

.time-self {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-quaternary);
  align-self: flex-end;
  margin-bottom: var(--dt-spacing-xs);
}

.status-badge {
  position: absolute;
  bottom: var(--dt-spacing-xs);
  right: calc(-1 * var(--dt-spacing-lg));
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;

  .is-loading {
    font-size: 12px;
    color: var(--dt-text-quaternary);
  }

  .failed-indicator {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 16px;
    height: 16px;
    background: var(--dt-error-color);
    border-radius: 50%;
    cursor: pointer;

    .el-icon {
      font-size: 10px;
      color: var(--dt-text-white);
    }

    &:hover {
      transform: scale(1.1);
    }
  }

  .read-indicator {
    display: flex;
    align-items: center;
    justify-content: center;

    .check-icon {
      width: 14px;
      height: 14px;
      fill: var(--dt-text-quaternary);

      &.is-read {
        fill: var(--dt-brand-color);
      }
    }

    &.is-read .check-icon {
      fill: var(--dt-brand-color);
    }
  }
}

// 暗色模式适配
:global(.dark) {
  .message-header .nickname {
    color: var(--dt-text-primary-dark);
  }
}
</style>
