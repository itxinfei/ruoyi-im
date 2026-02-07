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
// 引用消息组件 - 野火IM风格

.message-reply-ref {
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.03);
  border-left: 3px solid #4168e0; // 野火IM蓝
  padding: 8px 12px;
  margin: -6px -8px 8px -8px;
  border-radius: 4px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  overflow: hidden;
  user-select: none;
  transition: all 0.2s;

  &:hover {
    background: rgba(65, 104, 224, 0.08);
    border-left-color: #3457c7;

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
  background: #4168e0;
  color: #fff;
  border-radius: 3px;
  font-size: 11px;
  transition: transform 0.2s;

  .material-icons-outlined {
    font-size: 13px;
  }
}

.ref-user {
  font-weight: 600;
  color: #333;
  font-size: 13px;
}

.ref-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.ref-type-text {
  color: #4168e0;
  font-weight: 500;
}

.ref-text {
  color: #666;
}

// 暗色模式适配
:global(.dark) {
  .message-reply-ref {
    background: rgba(255, 255, 255, 0.05);
    border-left-color: #4168e0;

    .ref-user {
      color: #e8e8e8;
    }

    .ref-content {
      color: #a0a8b8;
    }

    &:hover {
      background: rgba(65, 104, 224, 0.15);
      border-left-color: #5a7ce9;
    }
  }

  .ref-type-text {
    color: #6b8cff;
  }
}
</style>
