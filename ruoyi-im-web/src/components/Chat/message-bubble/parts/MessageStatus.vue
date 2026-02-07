<template>
  <div
    v-if="message.isOwn"
    class="message-status"
    :class="`status-${uiStatus}`"
  >
    <!-- 发送中：轻量级 CSS 转圈 -->
    <transition name="status-fade">
      <div
        v-if="uiStatus === 'sending'"
        class="status-indicator status-sending"
        title="发送中"
      >
        <div class="css-loader" />
      </div>
    </transition>

    <!-- 私聊-未读：蓝色小圆点 -->
    <transition name="status-scale">
      <div
        v-if="uiStatus === 'delivered' && !isGroupChat"
        class="status-indicator status-unread-dot"
        title="未读"
      />
    </transition>

    <!-- 私聊-已读：灰色文字 -->
    <transition name="status-fade">
      <div
        v-if="uiStatus === 'read' && !isGroupChat"
        class="status-text-read"
        title="已读"
      >
        已读
      </div>
    </transition>

    <!-- 群聊-未读：蓝色数字圆圈 -->
    <transition name="status-scale">
      <div
        v-if="isGroupChat && !isAllRead"
        class="status-group-unread"
        :title="`${unreadCount}人未读`"
        @click="handleShowReadInfo"
      >
        {{ unreadCount }}
      </div>
    </transition>

    <!-- 群聊-全部已读：灰色文字 -->
    <transition name="status-fade">
      <div
        v-if="isGroupChat && isAllRead"
        class="status-text-read"
        title="全部已读"
        @click="handleShowReadInfo"
      >
        全部已读
      </div>
    </transition>

    <!-- 发送失败：红色感叹号 -->
    <transition name="status-shake">
      <div
        v-if="uiStatus === 'failed'"
        class="status-failed-container"
        @click="handleRetry"
      >
        <span class="material-icons">error</span>
        <div class="error-tooltip">
          {{ errorText }}
        </div>
      </div>
    </transition>
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

// 映射后端 sendStatus 到前端 UI 状态
const uiStatus = computed(() => {
  const sendStatus = props.message.sendStatus || props.message.status

  // 如果是字符串，直接使用
  if (typeof sendStatus === 'string') {
    if (sendStatus === 'PENDING' || sendStatus === 'SENDING') { return 'sending' }
    if (sendStatus === 'DELIVERED') { return 'delivered' }
    if (sendStatus === 'READ') { return 'read' }
    if (sendStatus === 'FAILED') { return 'failed' }
  }

  // 如果是数字，映射到对应状态
  const statusMap = {
    0: 'sending',  // PENDING
    1: 'sending',  // SENDING
    2: 'delivered', // DELIVERED
    3: 'read',     // READ
    4: 'failed'    // FAILED
  }

  return statusMap[sendStatus] || 'delivered'
})

// 未读人数（用于群聊）
const isGroupChat = computed(() => props.sessionType === 'GROUP')
const unreadCount = computed(() => {
  if (!isGroupChat.value) { return 0 }
  const total = props.message.totalMembers || 0
  const read = props.message.readCount || 0
  return Math.max(0, total - read)
})

// 是否全部已读
const isAllRead = computed(() => {
  if (isGroupChat.value) {
    const total = props.message.totalMembers || 0
    return total > 0 && props.message.readCount >= total
  }
  return uiStatus.value === 'read'
})

// 是否显示已读信息
const showReadInfo = computed(() => {
  return uiStatus.value === 'read' && (isGroupChat.value || props.message.readTime)
})

// readCount 计算属性
const readCount = computed(() => props.message.readCount || 0)

// 已读提示文本
const readTooltip = computed(() => {
  if (!showReadInfo.value) { return '已读' }

  // 单聊：显示已读时间
  if (!isGroupChat.value && props.message.readTime) {
    const readTime = formatRelativeTime(props.message.readTime)
    return `已读 ${readTime}`
  }

  // 群聊：显示人数
  if (isGroupChat.value) {
    return `${readCount.value}人已读，点击查看详情`
  }

  return '已读'
})

// 失败原因文本
const errorText = computed(() => {
  const errorCode = props.message.sendErrorCode || props.message.errorCode

  const errorMessages = {
    'NETWORK_ERROR': '网络错误，请检查网络连接',
    'TIMEOUT': '请求超时，请稍后重试',
    'SERVER_ERROR': '服务器错误，请稍后重试',
    'RATE_LIMIT': '发送过于频繁，请稍后再试',
    'FORBIDDEN': '无权限发送此消息'
  }

  return errorMessages[errorCode] || '发送失败，点击重发'
})

/**
 * 显示已读信息
 */
function handleShowReadInfo() {
  if (!showReadInfo.value) { return }

  emit('show-read-info', {
    messageId: props.message.id,
    readCount: readCount.value,
    readBy: props.message.readBy || [],
    readTime: props.message.readTime,
    isGroupChat: isGroupChat.value
  })
}

/**
 * 点击重发
 */
function handleRetry() {
  emit('retry', props.message)
}
</script>

<style scoped lang="scss">
.message-status {
  display: flex;
  align-items: center;
  margin-left: 4px;
  margin-bottom: 2px;
  font-size: 12px;
  align-self: flex-end;
  pointer-events: auto;
  min-width: 12px;
  min-height: 12px;
}

// 发送中：CSS Loader
.css-loader {
  width: 12px;
  height: 12px;
  border: 1.5px solid var(--dt-divider-tertiary);
  border-top-color: var(--dt-brand-color);
  border-radius: 50%;
  animation: rotate 0.8s linear infinite;
}

// 私聊未读：蓝色小点
.status-unread-dot {
  width: 8px;
  height: 8px;
  background-color: var(--dt-brand-color);
  border-radius: 50%;
  margin: 2px;
}

// 已读文本样式
.status-text-read {
  font-size: 11px;
  color: var(--dt-text-quaternary);
  user-select: none;
  white-space: nowrap;
}

// 群聊未读：蓝色遮罩圆圈
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
  transition: transform 0.2s;

  &:hover {
    transform: scale(1.1);
  }
}

// 发送失败样式
.status-failed-container {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;

  .material-icons {
    color: #f44336;
    font-size: 18px;
  }

  .error-tooltip {
    position: absolute;
    bottom: calc(100% + 8px);
    left: 50%;
    transform: translateX(-50%);
    background: var(--dt-fill-surface-elevated);
    color: var(--dt-text-primary-on-color);
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 11px;
    opacity: 0;
    pointer-events: none;
    transition: opacity 0.2s;
    white-space: nowrap;
  }

  &:hover .error-tooltip {
    opacity: 1;
  }
}

// 动画
@keyframes rotate {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.status-fade-enter-active,
.status-fade-leave-active {
  transition: opacity 0.2s;
}

.status-fade-enter-from,
.status-fade-leave-to {
  opacity: 0;
}

.status-scale-enter-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.status-scale-enter-from {
  opacity: 0;
  transform: scale(0.5);
}

.status-shake-enter-active {
  animation: shake 0.5s;
}

@keyframes shake {

  0%,
  100% {
    transform: translateX(0);
  }

  25% {
    transform: translateX(-2px);
  }

  75% {
    transform: translateX(2px);
  }
}

:global(.dark) {
  .css-loader {
    border-color: var(--dt-divider-tertiary-dark);
  }

  .status-text-read {
    color: var(--dt-text-quaternary-dark);
  }
}
</style>
