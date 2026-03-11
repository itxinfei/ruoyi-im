<template>
  <div class="message-item" :class="{ 'is-own': message.isOwn, 'is-failed': message.status === 'failed' }">
    <!-- 头像 -->
    <el-avatar
      shape="square"
      :size="36"
      :src="message.senderAvatar"
      @click="$emit('show-user', message.senderId)"
      class="avatar"
    >
      {{ message.senderName?.charAt(0) }}
    </el-avatar>

    <div class="content">
      <!-- 昵称 (对方才显示) -->
      <div v-if="!message.isOwn" class="nickname">{{ message.senderName }}</div>

      <!-- 消息主区 (气泡) -->
      <div class="bubble-wrapper">
        <slot name="bubble"></slot>

        <!-- 状态标识 (己方才显示) -->
        <div v-if="message.isOwn" class="status">
          <span v-if="message.status === 'sending'" class="loading">
            <el-icon class="is-loading"><Loading /></el-icon>
          </span>
          <span v-else-if="message.status === 'failed'" class="failed" @click="handleRetry">
            <el-icon><WarningFilled /></el-icon>
            重试
          </span>
          <span v-else :class="{ 'read': message.readCount > 0 }">
            {{ message.readCount > 0 ? '已读' : '未读' }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Loading, WarningFilled } from '@element-plus/icons-vue'

const props = defineProps({ message: Object })
const emit = defineEmits(['show-user', 'retry'])

const handleRetry = () => {
  emit('retry', props.message)
}
</script>

<style scoped lang="scss">
.message-item {
  display: flex;
  padding: 0 16px;
  gap: 12px;

  &.is-own {
    flex-direction: row-reverse;
    .content {
      align-items: flex-end;
    }
    .bubble-wrapper {
      flex-direction: row-reverse;
    }
  }

  &.is-failed {
    .message-bubble {
      border-color: var(--dt-error-color);
      background: var(--dt-error-bg);
    }
  }
}

.avatar { cursor: pointer; flex-shrink: 0; border-radius: var(--dt-radius-md); }

.content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 70%;
}

.nickname {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
}

.bubble-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.status {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-quaternary);
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 2px;

  .loading {
    color: var(--dt-text-tertiary);
    .el-icon {
      font-size: 14px;
    }
  }

  .read { color: var(--dt-brand-color); }

  .failed {
    color: var(--dt-error-color);
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 2px;
    padding: 2px 6px;
    border-radius: var(--dt-radius-sm);
    transition: all var(--dt-transition-fast);

    .el-icon {
      font-size: 14px;
    }

    &:hover {
      background: var(--dt-error-bg);
    }
  }
}
</style>
