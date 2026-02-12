/**
* 消息发送状态指示器组件
* 显示消息发送状态：发送中、已送达、已读、失败
* 对标钉钉/野火IM的设计风格
*/
<template>
  <div
    class="message-status-indicator"
    :class="statusClass"
  >
    <!-- 发送中 -->
    <template v-if="status === 'sending'">
      <el-icon class="status-icon spinning">
        <Loading />
      </el-icon>
    </template>

    <!-- 已送达 -->
    <template v-else-if="status === 'delivered'">
      <el-icon class="status-icon">
        <Select />
      </el-icon>
    </template>

    <!-- 已读 -->
    <template v-else-if="status === 'read'">
      <div class="read-avatar-group">
        <span
          v-for="avatar in displayAvatars"
          :key="avatar.userId"
          class="read-avatar"
          :style="{ backgroundImage: `url(${avatar.avatar})` }"
          :title="avatar.userName"
        />
        <span
          v-if="readCount > 3"
          class="read-more"
        >+{{ readCount - 3 }}</span>
      </div>
    </template>

    <!-- 失败 - 可点击重试 -->
    <el-icon
      v-else-if="status === 'failed'"
      class="status-icon failed"
      @click="handleRetry"
    >
      <WarningFilled />
    </el-icon>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Loading, Select, WarningFilled } from '@element-plus/icons-vue'

const props = defineProps({
  // 发送状态：pending, sending, delivered, read, failed
  status: {
    type: String,
    default: 'pending',
    validator: v => ['pending', 'sending', 'delivered', 'read', 'failed'].includes(v)
  },
  // 已读用户列表（用于群聊已读详情）
  readBy: {
    type: Array,
    default: () => []
  },
  // 已读人数
  readCount: {
    type: Number,
    default: 0
  },
  // 消息ID（用于重试）
  messageId: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['retry'])

// 状态对应的CSS类
const statusClass = computed(() => {
  return `status-${props.status}`
})

// 显示的头像（最多显示3个）
const displayAvatars = computed(() => {
  if (!props.readBy || props.readBy.length === 0) {
    return []
  }
  return props.readBy.slice(0, 3).map(user => ({
    userId: user.userId || user.id,
    userName: user.userName || user.name || user.nickname,
    avatar: user.avatar || user.userAvatar || ''
  }))
})

// 处理重试
const handleRetry = () => {
  if (props.messageId) {
    emit('retry', props.messageId)
  }
}
</script>

<script>
export default {
  name: 'MessageStatusIndicator'
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-status-indicator {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 16px;
  color: var(--dt-text-quaternary);
  font-size: 14px;

  .status-icon {
    font-size: 16px;
    transition: all 0.2s;

    &.spinning {
      animation: spin 1s linear infinite;
      color: var(--dt-brand-color);
    }

    &.failed {
      color: var(--dt-error-color);
      cursor: pointer;

      &:hover {
        transform: scale(1.1);
      }
    }
  }

  // 已读头像组（钉钉风格）
  .read-avatar-group {
    display: flex;
    align-items: center;
    height: 16px;

    .read-avatar {
      width: 16px;
      height: 16px;
      border-radius: 50%;
      background-size: cover;
      background-position: center;
      border: 1px solid #fff;
      margin-left: -4px;

      &:first-child {
        margin-left: 0;
      }
    }

    .read-more {
      font-size: 10px;
      color: var(--dt-text-tertiary);
      margin-left: 2px;
    }
  }

  // 发送中 - 浅色
  &.status-sending {
    color: var(--dt-brand-color);
  }

  // 已送达 - 灰色
  &.status-delivered {
    color: var(--dt-text-tertiary);
  }

  // 已读 - 蓝色
  &.status-read {
    color: var(--dt-brand-color);
  }

  // 失败 - 红色
  &.status-failed {
    color: var(--dt-error-color);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 暗色模式
:global(.dark) {
  .read-avatar {
    border-color: var(--dt-bg-card-dark);
  }
}
</style>
