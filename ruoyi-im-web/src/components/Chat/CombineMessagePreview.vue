<template>
  <div
    class="combine-preview"
    @click="handleClick"
  >
    <!-- 标题栏 -->
    <div class="preview-header">
      <span class="material-icons-outlined">forum</span>
      <span class="header-title">聊天记录</span>
    </div>

    <!-- 消息预览列表 -->
    <div class="preview-body">
      <div
        v-for="(msg, index) in displayMessages"
        :key="msg.id || index"
        class="preview-message-item"
      >
        <span class="msg-sender">{{ msg.senderName }}:</span>
        <span class="msg-content">{{ getMessagePreview(msg) }}</span>
      </div>
      <div
        v-if="hasMore"
        class="preview-more"
      >
        还有 {{ moreCount }} 条消息...
      </div>
    </div>

    <!-- 页脚：消息数量和时间范围 -->
    <div class="preview-footer">
      <span class="message-count">{{ messageCount }} 条消息</span>
      <span
        v-if="timeRange"
        class="time-range"
      >{{ timeRange }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { parseMessageContent } from '@/utils/message'
import { formatChatTime } from '@/utils/format'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  // 最多显示几条消息预览
  maxPreview: {
    type: Number,
    default: 3
  }
})

const emit = defineEmits(['click'])

// 消息总数
const messageCount = computed(() => props.messages.length)

// 显示的消息（最多 maxPreview 条）
const displayMessages = computed(() => {
  return props.messages.slice(0, props.maxPreview)
})

// 是否还有更多消息
const hasMore = computed(() => props.messages.length > props.maxPreview)

// 剩余消息数量
const moreCount = computed(() => props.messages.length - props.maxPreview)

// 时间范围
const timeRange = computed(() => {
  if (props.messages.length === 0) {return ''}

  const timestamps = props.messages
    .map(m => m.timestamp || m.sendTime || m.createTime)
    .filter(Boolean)
    .map(t => new Date(t).getTime())
    .sort((a, b) => a - b)

  if (timestamps.length === 0) {return ''}

  const start = new Date(timestamps[0])
  const end = new Date(timestamps[timestamps.length - 1])

  const startStr = formatChatTime(start)
  const endStr = formatChatTime(end)

  return startStr === endStr ? startStr : `${startStr} - ${endStr}`
})

// 获取消息预览文本
const getMessagePreview = msg => {
  const type = msg.type || msg.messageType || 'TEXT'

  switch (type) {
    case 'IMAGE':
      return '[图片]'
    case 'VIDEO':
      return '[视频]'
    case 'FILE': {
      const content = parseMessageContent(msg) || {}
      return `[文件] ${content.fileName || '未知文件'}`
    }
    case 'AUDIO':
    case 'VOICE':
      return '[语音]'
    case 'RECALLED':
      return '[撤回]'
    default: {
      // 文本消息截取
      const text = msg.content || ''
      return text.length > 20 ? text.substring(0, 20) + '...' : text
    }
  }
}

// 点击处理
const handleClick = () => {
  emit('click', props.messages)
}
</script>

<style scoped lang="scss">
.combine-preview {
  width: 280px;
  background: #f5f5f5;
  border-radius: var(--dt-radius-lg, 12px);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #e8e8e8;
    box-shadow: var(--dt-shadow-2);
  }

  // 标题栏
  .preview-header {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 12px;
    border-bottom: 1px solid #e8e8e8;

    .material-icons-outlined {
      font-size: 16px;
      color: var(--dt-brand-color, #3296FA);
    }

    .header-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--dt-text-primary, #262626);
    }
  }

  // 消息预览
  .preview-body {
    padding: 8px 12px;

    .preview-message-item {
      display: flex;
      gap: 6px;
      padding: 6px 0;
      font-size: 13px;
      line-height: 1.5;
      border-bottom: 1px dashed #e8e8e8;

      &:last-child {
        border-bottom: none;
      }

      .msg-sender {
        font-weight: 500;
        color: var(--dt-brand-color, #3296FA);
        flex-shrink: 0;
      }

      .msg-content {
        color: var(--dt-text-secondary, #595959);
        word-break: break-all;
      }
    }

    .preview-more {
      padding: 6px 0;
      font-size: 12px;
      color: var(--dt-text-tertiary, #8c8c8c);
      text-align: center;
      font-style: italic;
    }
  }

  // 页脚
  .preview-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    border-top: 1px solid #e8e8e8;
    font-size: 12px;
    color: var(--dt-text-tertiary, #8c8c8c);
  }
}

// 暗色模式
:deep(.dark) {
  .combine-preview {
    background: #1e293b;
    border: 1px solid #334155;

    &:hover {
      background: #334155;
    }

    .preview-header {
      border-color: #334155;
    }

    .preview-body .preview-message-item {
      border-color: #334155;
    }

    .preview-footer {
      border-color: #334155;
    }
  }
}
</style>
