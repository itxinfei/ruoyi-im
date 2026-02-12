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
      <span class="ref-user">{{ reply?.senderName || '未知用户' }}</span>
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
        <span class="ref-text">{{ reply?.content || '' }}</span>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { parseMessageContent } from '@/utils/message'

const props = defineProps({
  reply: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['click'])

const replyType = computed(() => {
  return props.reply?.type || 'TEXT'
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
// 引用消息组件 - 钉钉8.0风格

.message-reply-ref {
  display: flex;
  flex-direction: column;
  background: var(--dt-black-04);
  border-left: 3px solid var(--dt-brand-color);
  padding: 12px;
  margin: -10px -14px 8px -14px;
  border-radius: 4px;
  font-size: 13px;
  color: var(--dt-text-secondary);
  cursor: pointer;
  overflow: hidden;
  user-select: none;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-brand-extra-light);
    border-left-color: var(--dt-brand-hover);

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
  border-radius: 3px;
  font-size: 11px;
  transition: transform var(--dt-transition-base);

  .material-icons-outlined {
    font-size: 13px;
  }
}

.ref-user {
  font-weight: 600;
  color: var(--dt-text-primary);
  font-size: 13px;
}

.ref-content {
  font-size: 13px;
  color: var(--dt-text-secondary);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  line-clamp: 2;
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

// 暗色模式适配
:global(.dark) {
  .message-reply-ref {
    background: var(--dt-white-06);
    border-left-color: var(--dt-brand-color);

    .ref-user {
      color: var(--dt-text-primary-dark);
    }

    .ref-content {
      color: var(--dt-text-secondary-dark);
    }

    &:hover {
      background: var(--dt-brand-bg-hover);
      border-left-color: var(--dt-brand-color-light);
    }
  }

  .ref-type-text {
    color: var(--dt-brand-color-light);
  }
}
</style>
