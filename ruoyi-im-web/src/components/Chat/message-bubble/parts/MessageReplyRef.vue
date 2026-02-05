<template>
  <div
    class="message-reply-ref"
    @click.stop="handleClick"
  >
    <div class="ref-header">
      <span class="ref-icon">
        <span
          v-if="replyType === 'IMAGE'"
          class="material-icons-outlined"
        >image</span>
        <span
          v-else-if="replyType === 'FILE'"
          class="material-icons-outlined"
        >insert_drive_file</span>
        <span
          v-else-if="replyType === 'VIDEO'"
          class="material-icons-outlined"
        >videocam</span>
        <span
          v-else-if="replyType === 'VOICE' || replyType === 'AUDIO'"
          class="material-icons-outlined"
        >mic</span>
        <span
          v-else
          class="material-icons-outlined"
        >format_quote</span>
      </span>
      <span class="ref-user">{{ reply.senderName }}</span>
    </div>
    <div class="ref-content">
      <template v-if="replyType === 'IMAGE'">
        <span class="ref-type-text">[图片]</span>
      </template>
      <template v-else-if="replyType === 'FILE'">
        <span class="ref-type-text">[文件] {{ fileName }}</span>
      </template>
      <template v-else-if="replyType === 'VIDEO'">
        <span class="ref-type-text">[视频]</span>
      </template>
      <template v-else-if="replyType === 'VOICE' || replyType === 'AUDIO'">
        <span class="ref-type-text">[语音]</span>
      </template>
      <template v-else>
        <span class="ref-text">{{ reply.content }}</span>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { parseMessageContent } from '@/utils/message'

const props = defineProps({
  reply: { type: Object, required: true }
})

const emit = defineEmits(['click'])

const replyType = computed(() => {
  return props.reply.type || 'TEXT'
})

const fileName = computed(() => {
  const parsed = parseMessageContent(props.reply)
  return parsed?.fileName || parsed?.name || '文件'
})

const handleClick = () => {
  emit('click')
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-reply-ref {
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.4);
  border-left: 3px solid var(--dt-brand-color);
  padding: 8px 12px;
  margin: -6px -8px 8px -8px;
  border-radius: var(--dt-radius-sm);
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  cursor: pointer;
  overflow: hidden;
  user-select: none;
  transition: all var(--dt-transition-base);
  animation: slideInDown 0.25s var(--dt-ease-out);

  &:hover {
    background: rgba(255, 255, 255, 0.6);
    border-left-color: var(--dt-brand-hover);
    box-shadow: 0 2px 6px rgba(0, 137, 255, 0.1);

    .ref-icon {
      transform: scale(1.05);
    }
  }

  &:active {
    transform: translateX(1px) scale(0.99);
  }
}

.ref-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.ref-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  background: var(--dt-brand-color);
  color: #fff;
  border-radius: var(--dt-radius-sm);
  font-size: 11px;

  .material-icons-outlined {
    font-size: 13px;
  }
}

.ref-user {
  font-weight: 600;
  color: var(--dt-text-primary);
  font-size: var(--dt-font-size-sm);
}

.ref-content {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.ref-type-text {
  color: var(--dt-brand-color);
  font-weight: 500;
}

.ref-text {
  color: var(--dt-text-secondary);
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-3px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 暗色模式适配
:global(.dark) {
  .message-reply-ref {
    background: rgba(255, 255, 255, 0.05);
    border-left-color: var(--dt-brand-color);

    .ref-user {
      color: var(--dt-text-primary);
    }

    .ref-content {
      color: var(--dt-text-secondary);
    }

    &:hover {
      background: rgba(0, 137, 255, 0.15);
      border-left-color: var(--dt-brand-hover);
    }
  }
}
</style>
