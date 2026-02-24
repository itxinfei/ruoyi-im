<template>
  <div
    v-if="showStatus"
    class="message-status-indicator"
    :class="`status-${computedStatus}`"
  >
    <!-- 发送中状态 -->
    <div
      v-if="computedStatus === 'sending'"
      class="status-indicator status-sending"
      title="发送中"
    >
      <div class="css-loader" />
    </div>

    <!-- 发送失败状态 -->
    <div
      v-else-if="computedStatus === 'failed'"
      class="status-indicator status-failed"
      @click="handleRetry"
    >
      <span class="material-icons">error</span>
      <div class="error-tooltip">
        {{ errorText }}
      </div>
    </div>

    <!-- 已撤回状态 -->
    <div
      v-else-if="computedStatus === 'recalled'"
      class="status-text-recalled"
    >
      已撤回
    </div>

    <!-- 已送达状态（私聊） -->
    <div
      v-else-if="computedStatus === 'delivered' && !isGroupChat"
      class="status-indicator status-delivered"
      title="已送达"
    >
      ✓
    </div>

    <!-- 已读状态（私聊） -->
    <div
      v-else-if="computedStatus === 'read' && !isGroupChat"
      class="status-indicator status-read"
      title="已读"
    >
      ✓✓
    </div>

    <!-- 群聊状态（显示已读人数或未读提示） -->
    <div
      v-else-if="isGroupChat"
      class="status-group-wrapper"
    >
      <div
        v-if="isAllRead"
        class="status-text-read"
        title="全部已读"
      >
        全部已读
      </div>
      <div
        v-else-if="unreadCount > 0"
        class="status-group-unread"
        :title="`${unreadCount}人未读`"
        @click="handleShowReadInfo"
      >
        {{ unreadCount }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { formatRelativeTime } from '@/utils/message'

const props = defineProps({
  message: { type: Object, required: true },
  sessionType: { type: String, default: 'PRIVATE' }
})

const emit = defineEmits(['retry', 'show-read-info'])

// 确定是否为群聊
const isGroupChat = computed(() => props.sessionType === 'GROUP')

// 计算消息状态
const computedStatus = computed(() => {
  const sendStatus = props.message.sendStatus || props.message.status

  if (props.message?.isRevoked || props.message?.type?.toUpperCase() === 'RECALLED') {
    return 'recalled'
  }

  if (typeof sendStatus === 'string') {
    const normalized = sendStatus.toUpperCase()
    if (normalized === 'PENDING' || normalized === 'SENDING') { return 'sending' }
    if (normalized === 'DELIVERED') { return 'delivered' }
    if (normalized === 'READ') { return 'read' }
    if (normalized === 'FAILED') { return 'failed' }
    if (normalized === 'RECALLED' || normalized === 'RECALL') { return 'recalled' }
  }

  const statusMap = {
    0: 'sending',  // PENDING
    1: 'sending',  // SENDING
    2: 'delivered',// DELIVERED
    3: 'read',     // READ
    4: 'failed',   // FAILED
    5: 'recalled'  // RECALLED
  }

  return statusMap[sendStatus] || 'delivered'
})

// 是否显示状态指示器
const showStatus = computed(() => {
  return props.message.isOwn && computedStatus.value !== 'recalled'
})

// 群聊未读数
const unreadCount = computed(() => {
  if (!isGroupChat.value) { return 0 }
  const total = props.message.totalMembers || 0
  const read = props.message.readCount || 0
  return Math.max(0, total - read)
})

// 是否全部已读（群聊）
const isAllRead = computed(() => {
  if (isGroupChat.value) {
    const total = props.message.totalMembers || 0
    return total > 0 && props.message.readCount >= total
  }
  return computedStatus.value === 'read'
})

// 错误信息
const errorText = computed(() => {
  const errorCode = props.message.sendErrorCode || props.message.errorCode
  const errorMessages = {
    'NETWORK_ERROR': '网络错误',
    'TIMEOUT': '请求超时',
    'SERVER_ERROR': '服务器错误',
    'RATE_LIMIT': '发送频繁',
    'FORBIDDEN': '权限不足'
  }
  return errorMessages[errorCode] || '发送失败'
})

// 事件处理
const handleRetry = () => {
  emit('retry', props.message)
}

const handleShowReadInfo = () => {
  if (isGroupChat.value && unreadCount.value < (props.message.totalMembers || 0)) {
    emit('show-read-info', {
      messageId: props.message.id,
      readCount: props.message.readCount || 0,
      readBy: props.message.readBy || [],
      readTime: props.message.readTime,
      isGroupChat: isGroupChat.value,
      totalMembers: props.message.totalMembers || 0,
      allMembers: props.message.allMembers || []
    })
  }
}
</script>

<style scoped>
.message-status-indicator {
  display: flex;
  align-items: center;
  margin-left: 4px;
  font-size: 12px;
  align-self: flex-end;
  min-width: 14px;
  min-height: 14px;
}

/* 发送中状态 */
.status-sending {
  width: 12px;
  height: 12px;
}

.css-loader {
  width: 100%;
  height: 100%;
  border: 1.5px solid var(--dt-divider-tertiary);
  border-top-color: var(--dt-brand-color);
  border-radius: 50%;
  animation: rotate 0.8s linear infinite;
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 发送失败状态 */
.status-failed {
  cursor: pointer;
  position: relative;
  color: #f44336;
  font-size: 14px;
}

.status-failed .error-tooltip {
  position: absolute;
  bottom: calc(100% + 6px);
  left: 50%;
  transform: translateX(-50%);
  background: var(--dt-fill-surface-elevated);
  color: var(--dt-text-primary-on-color);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 10px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.status-failed:hover .error-tooltip {
  opacity: 1;
}

/* 已读/已送达状态 */
.status-delivered,
.status-read {
  font-size: 11px;
  color: var(--dt-text-quaternary);
}

.status-read {
  color: var(--dt-brand-color);
}

/* 已撤回状态 */
.status-text-recalled {
  font-size: 11px;
  color: var(--dt-text-quaternary);
  font-style: italic;
}

/* 群聊状态 */
.status-group-wrapper {
  display: flex;
  align-items: center;
}

.status-group-unread {
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background-color: var(--dt-brand-color);
  color: #fff;
  font-size: 10px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.status-group-unread:hover {
  background-color: var(--dt-brand-hover);
  transform: scale(1.1);
}

.status-text-read {
  font-size: 11px;
  color: var(--dt-text-quaternary);
  cursor: pointer;
}

/* 暗色模式支持 */
:global(.dark) {
  .css-loader {
    border-color: var(--dt-divider-tertiary-dark);
  }

  .status-delivered,
  .status-read,
  .status-text-recalled {
    color: var(--dt-text-quaternary-dark);
  }

  .status-read {
    color: var(--dt-brand-color);
  }

  .status-group-unread {
    background-color: var(--dt-brand-color);
  }

  .status-text-read {
    color: var(--dt-text-quaternary-dark);
  }
}
</style>