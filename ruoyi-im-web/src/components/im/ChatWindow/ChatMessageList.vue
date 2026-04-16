<template>
  <div ref="listRef" class="message-list-viewport" @scroll="handleScroll">
    <div class="message-list-content">
      <div
        v-for="(msg, index) in messages"
        :key="msg.clientMsgId || msg.messageId"
        :data-message-id="msg.messageId"
        :class="['message-item-wrapper', { 'is-selected': isMessageSelected(msg) }]"
        @click="handleMessageClick(msg)"
      >
        <div v-if="isSelectionMode" class="message-checkbox">
          <el-checkbox :model-value="isMessageSelected(msg)" />
        </div>
        <ChatMessageBubble
          :message="msg"
          :is-me="msg.isSelf"
          :is-grouped="checkIsGrouped(msg, index)"
          :show-time="checkShowTime(msg, index)"
          :quoted-message="msg.quotedMessage || getQuotedMessage(msg)"
          @reply="$emit('reply', msg)"
          @forward="$emit('forward', msg)"
          @recall="$emit('recall', msg)"
          @delete="$emit('delete', msg)"
          @favorite="$emit('favorite', msg)"
          @reaction="(emoji) => $emit('reaction', { message: msg, emoji })"
          @read-detail="$emit('read-detail', msg.messageId)"
          @edit="$emit('edit', msg)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ChatMessageBubble from '../ChatMessageBubble.vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  isSelectionMode: {
    type: Boolean,
    default: false
  },
  selectedMessages: {
    type: Set,
    default: () => new Set()
  }
})

const emit = defineEmits([
  'reply',
  'forward',
  'recall',
  'delete',
  'favorite',
  'reaction',
  'read-detail',
  'edit',
  'scroll-top'
])

const listRef = ref(null)

// 暴露 listRef 给父组件
defineExpose({ listRef })

// 检查是否应该与上一条消息分组（同一用户且时间<60秒）
const checkIsGrouped = (msg, index) => {
  if (index === 0 || !msg) return false
  const prevMsg = props.messages[index - 1]
  if (!prevMsg || !msg.sendTime || !prevMsg.sendTime) return false
  const timeDiff = (new Date(msg.sendTime).getTime() - new Date(prevMsg.sendTime).getTime()) / 1000
  return msg.senderId === prevMsg.senderId && timeDiff < 60
}

// 检查是否显示时间线（间隔>5分钟）
const checkShowTime = (msg, index) => {
  if (index === 0 || !msg) return true
  const prevMsg = props.messages[index - 1]
  if (!prevMsg || !msg.sendTime || !prevMsg.sendTime) return true
  const timeDiff = (new Date(msg.sendTime).getTime() - new Date(prevMsg.sendTime).getTime()) / 1000
  return timeDiff > 300
}

// 获取被引用的消息
const getQuotedMessage = (msg) => {
  if (!msg.replyToMessageId) return null
  return props.messages.find(m => m.messageId === msg.replyToMessageId)
}

const isMessageSelected = (msg) => {
  return props.selectedMessages.has(msg.messageId)
}

const handleMessageClick = (msg) => {
  if (!props.isSelectionMode) return
  emit('select-message', msg)
}

const handleScroll = () => {
  emit('scroll-top', listRef.value)
}
</script>

<style scoped>
.message-list-viewport {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  background-color: transparent;
  padding: 14px var(--dt-chat-gutter) 20px;
}

.message-list-content {
  width: min(100%, var(--dt-chat-content-max-width));
  margin: 0 auto;
}

/* 消息多选样式 */
.message-item-wrapper {
  position: relative;
  cursor: pointer;
  padding: 4px var(--dt-spacing-md);
  margin: 2px 0;
  border-radius: var(--dt-radius-xl);
  transition: background-color var(--dt-transition-fast), transform var(--dt-transition-fast);
}

.message-item-wrapper:hover {
  background-color: rgba(255, 255, 255, 0.42);
}

.message-item-wrapper.is-selected {
  background-color: rgba(39, 126, 251, 0.08);
}

.message-checkbox {
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 5;
}

@media (max-width: 960px) {
  .message-list-viewport {
    padding-left: var(--dt-chat-gutter-compact);
    padding-right: var(--dt-chat-gutter-compact);
  }
}

@media (max-width: 640px) {
  .message-list-viewport {
    padding: 10px 10px 12px;
  }

  .message-item-wrapper {
    padding-left: 4px;
    padding-right: 4px;
  }
}
</style>
