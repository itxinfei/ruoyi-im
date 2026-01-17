<template>
  <div class="message-status" :class="statusClass">
    <!-- 发送中状态 -->
    <template v-if="status === 'sending'">
      <el-icon class="status-icon is-spinning">
        <Loading />
      </el-icon>
    </template>

    <!-- 发送成功但未读状态 -->
    <template v-else-if="status === 'sent'">
      <el-icon class="status-icon">
        <CircleCheck />
      </el-icon>
      <span class="status-text">已发送</span>
    </template>

    <!-- 已读状态 -->
    <template v-else-if="status === 'read'">
      <el-icon class="status-icon is-read">
        <CircleCheck />
      </el-icon>
      <span v-if="showReadCount" class="status-text"> {{ readCount }}人已读 </span>
    </template>

    <!-- 发送失败状态 -->
    <template v-else-if="status === 'failed'">
      <el-icon class="status-icon is-failed">
        <CircleClose />
      </el-icon>
      <span class="status-text">发送失败</span>
      <el-button type="text" size="small" class="retry-btn" @click="$emit('retry')">
        重试
      </el-button>
    </template>

    <!-- 撤回状态 -->
    <template v-else-if="status === 'recalled'">
      <span class="status-text recalled">已撤回</span>
    </template>

    <!-- 编辑状态 -->
    <template v-else-if="status === 'edited'">
      <span class="status-text edited">已编辑</span>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'

const props = defineProps({
  // 消息状态: sending, sent, read, failed, recalled, edited
  status: {
    type: String,
    default: 'sent',
  },
  // 已读人数
  readCount: {
    type: Number,
    default: 0,
  },
  // 是否显示已读人数
  showReadCount: {
    type: Boolean,
    default: true,
  },
  // 总人数（用于计算已读比例）
  totalCount: {
    type: Number,
    default: 0,
  },
})

defineEmits(['retry'])

const statusClass = computed(() => {
  return `status-${props.status}`
})
</script>

<style lang="scss" scoped>
.message-status {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  transition: all 0.3s ease;

  .status-icon {
    font-size: 14px;
    transition: all 0.3s ease;

    &.is-spinning {
      animation: spin 1s linear infinite;
    }

    &.is-read {
      color: #67c23a;
    }

    &.is-failed {
      color: #f56c6c;
    }
  }

  .status-text {
    font-size: 12px;
    color: var(--el-text-color-placeholder);

    &.recalled {
      color: var(--el-text-color-secondary);
    }

    &.edited {
      color: var(--el-color-primary);
    }
  }

  .retry-btn {
    margin-left: 4px;
    padding: 0;
    font-size: 12px;
    color: var(--el-color-primary);

    &:hover {
      text-decoration: underline;
    }
  }

  // 不同状态的整体样式
  &.status-sending {
    color: var(--el-text-color-secondary);
  }

  &.status-sent {
    color: var(--el-text-color-placeholder);
  }

  &.status-read {
    .status-text {
      color: #67c23a;
    }
  }

  &.status-failed {
    cursor: pointer;
    background-color: #fef0f0;

    &:hover {
      background-color: #fde2e2;
    }
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
</style>
