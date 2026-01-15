<template>
  <div class="quote-message">
    <!-- 引用的消息 -->
    <div class="quote-content" @click="handleClickQuote">
      <div class="quote-bar"></div>
      <div class="quote-body">
        <div class="quote-header">
          <span class="quote-sender">{{ quoteData.senderName }}</span>
        </div>
        <div class="quote-text">{{ quoteData.content }}</div>
      </div>
    </div>

    <!-- 当前消息 -->
    <div class="current-content">
      {{ currentContent }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 引用数据
  content: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['click-quote'])

// 引用数据
const quoteData = computed(() => {
  if (typeof props.content === 'string') {
    return {
      senderName: '引用',
      content: props.content,
    }
  }
  return {
    senderName: props.content.quoteSenderName || '引用',
    content: props.content.quoteText || '',
    messageId: props.content.quoteMessageId,
  }
})

// 当前消息内容
const currentContent = computed(() => {
  if (typeof props.content === 'string') {
    return ''
  }
  return props.content.currentText || ''
})

// 点击引用的消息
const handleClickQuote = () => {
  emit('click-quote', { messageId: quoteData.value.messageId })
}
</script>

<style lang="scss" scoped>
.quote-message {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 400px;

  .quote-content {
    display: flex;
    padding: 8px 10px;
    background: #f5f7fa;
    border: 1px solid #e5e8eb;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #f0f2f5;
    }

    .quote-bar {
      width: 3px;
      background: #0089ff;
      border-radius: 2px;
      margin-right: 8px;
      flex-shrink: 0;
    }

    .quote-body {
      flex: 1;
      min-width: 0;

      .quote-header {
        margin-bottom: 4px;

        .quote-sender {
          font-size: 12px;
          color: #5f6468;
          font-weight: 500;
        }
      }

      .quote-text {
        font-size: 13px;
        color: #858b8f;
        line-height: 1.5;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
    }
  }

  .current-content {
    padding: 10px 14px;
    background: #fff;
    border: 1px solid #e5e8eb;
    border-radius: 12px;
    font-size: 14px;
    color: #171a1a;
    line-height: 1.6;
    word-break: break-word;
  }
}

// 接收的消息（白色背景）
.message-bubble.received .quote-message .current-content {
  background: #fff;
  border: 1px solid #e5e8eb;
  color: #171a1a;
}

// 发送的消息（蓝色背景）
.message-bubble.sent .quote-message .current-content {
  background: #0089ff;
  border: none;
  color: #fff;
}
</style>
