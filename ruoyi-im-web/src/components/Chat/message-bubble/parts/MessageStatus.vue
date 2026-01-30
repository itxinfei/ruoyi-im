<template>
  <div v-if="message.isOwn" class="message-status" :class="`status-${status}`">
    <!-- 发送中 -->
    <transition name="status-fade">
      <div v-if="status === 'sending'" class="status-indicator status-sending" title="发送中">
        <span class="sending-dots">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </span>
      </div>
    </transition>

    <!-- 发送成功（未读） -->
    <transition name="status-scale">
      <div v-if="status === 'sent'" class="status-indicator status-sent" title="已发送">
        <span class="material-icons-outlined">done</span>
      </div>
    </transition>

    <!-- 已读 -->
    <transition name="status-scale">
      <div
        v-if="status === 'read'"
        class="status-indicator status-read"
        :class="{ 'is-clickable': showReadInfo }"
        :title="readTooltip"
        @click="handleShowReadInfo"
      >
        <span class="material-icons-outlined">done_all</span>
      </div>
    </transition>

    <!-- 已读信息（群聊显示人数） -->
    <div v-if="status === 'read' && isGroupChat && readCount > 0" class="read-count">
      {{ readCountText }}
    </div>

    <!-- 发送失败 -->
    <transition name="status-shake">
      <div
        v-if="status === 'failed'"
        class="status-indicator status-failed"
        @click="$emit('retry')"
        title="点击重试"
      >
        <span class="material-icons-outlined">error_outline</span>
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

const status = computed(() => {
  return props.message.status || 'sent'
})

// 是否是群聊
const isGroupChat = computed(() => {
  return props.sessionType === 'GROUP'
})

// 已读人数
const readCount = computed(() => {
  return props.message.readCount || 0
})

// 已读人数文本
const readCountText = computed(() => {
  if (!isGroupChat.value) return ''
  const count = readCount.value
  const totalCount = props.message.totalMembers || 0
  return totalCount > 0 ? `${count}/${totalCount}` : `${count}`
})

// 是否显示已读信息
const showReadInfo = computed(() => {
  return status.value === 'read' && (isGroupChat.value || props.message.readTime)
})

// 已读提示文本
const readTooltip = computed(() => {
  if (!showReadInfo.value) return '已读'

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

/**
 * 显示已读信息
 */
function handleShowReadInfo() {
  if (!showReadInfo.value) return

  emit('show-read-info', {
    messageId: props.message.id,
    readCount: readCount.value,
    readBy: props.message.readBy || [],
    readTime: props.message.readTime,
    isGroupChat: isGroupChat.value
  })
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-status {
  display: flex;
  align-items: center;
  margin-left: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all var(--dt-transition-base);
  align-self: flex-end;
}

.status-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  transition: all var(--dt-transition-base);

  .material-icons-outlined {
    font-size: 16px;
  }
}

// 发送中状态
.status-sending {
  color: var(--dt-text-tertiary);

  .sending-dots {
    display: flex;
    align-items: center;
    gap: 3px;

    .dot {
      width: 4px;
      height: 4px;
      background: currentColor;
      border-radius: 50%;
      animation: sendingBounce 1.4s ease-in-out infinite;

      &:nth-child(1) { animation-delay: 0s; }
      &:nth-child(2) { animation-delay: 0.16s; }
      &:nth-child(3) { animation-delay: 0.32s; }
    }
  }
}

@keyframes sendingBounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

// 发送成功状态（绿色对勾）
.status-sent {
  color: var(--dt-color-success);

  &:hover {
    transform: scale(1.1);
  }
}

// 已读状态
.status-read {
  color: var(--dt-brand-color);

  .is-clickable {
    cursor: pointer;

    &:hover {
      transform: scale(1.1);
    }
  }
}

// 已读人数显示（群聊）
.read-count {
  font-size: 11px;
  color: var(--dt-brand-color);
  margin-left: 4px;
  font-weight: 500;
  user-select: none;
}

// 失败状态
.status-failed {
  color: var(--dt-error-color);
  cursor: pointer;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    inset: -4px;
    border-radius: 50%;
    background: rgba(239, 68, 68, 0.1);
    opacity: 0;
    transition: opacity var(--dt-transition-base);
    animation: pulse 2s ease-in-out infinite;
  }

  &:hover {
    transform: scale(1.15);

    &::after {
      opacity: 1;
    }
  }
}

// 状态过渡动画
.status-fade-enter-active,
.status-fade-leave-active {
  transition: all 0.2s var(--dt-ease-out);
}

.status-fade-enter-from,
.status-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.status-scale-enter-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.status-scale-leave-active {
  transition: all 0.2s var(--dt-ease-out);
}

.status-scale-enter-from {
  opacity: 0;
  transform: scale(0);
}

.status-scale-leave-to {
  opacity: 0;
  transform: scale(0.5);
}

.status-shake-enter-active {
  animation: shake 0.5s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

.status-shake-leave-active {
  transition: all 0.2s var(--dt-ease-out);
}

.status-shake-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}
</style>
