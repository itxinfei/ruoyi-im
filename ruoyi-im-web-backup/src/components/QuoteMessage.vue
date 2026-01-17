<template>
  <div class="quote-message">
    <div class="quote-header">
      <el-icon class="quote-icon"><ChatDotRound /></el-icon>
      <span class="replying-to">回复 {{ message.senderName }}</span>
      <el-button 
        type="text" 
        size="small" 
        class="cancel-btn"
        @click="$emit('cancel')"
      >
        取消
      </el-button>
    </div>
    <div class="quote-content" @click="$emit('clickQuote')">
      <div class="quoted-message">
        <span class="quote-text">{{ getQuoteText() }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['cancel', 'clickQuote'])

function getQuoteText() {
  if (!props.message) return ''
  
  const content = props.message.content
  
  if (typeof content === 'string') {
    return content.length > 50 ? content.substring(0, 50) + '...' : content
  } else if (typeof content === 'object' && content.text) {
    return content.text.length > 50 ? content.text.substring(0, 50) + '...' : content.text
  } else if (typeof content === 'object') {
    // 处理其他类型的消息
    if (content.type === 'image') return '[图片]'
    if (content.type === 'file') return '[文件]'
    if (content.type === 'voice') return '[语音]'
    if (content.type === 'video') return '[视频]'
    return '[消息]'
  }
  
  return '[消息]'
}
</script>

<style scoped>
.quote-message {
  background: #f8f9fa;
  border-left: 3px solid #1677ff;
  border-radius: 6px;
  margin: 8px 0;
  overflow: hidden;
}

.quote-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px 6px;
  font-size: 12px;
  color: #666;
  background: rgba(22, 119, 255, 0.1);
}

.quote-icon {
  color: #1677ff;
  font-size: 14px;
}

.replying-to {
  flex: 1;
  font-weight: 500;
}

.cancel-btn {
  color: #1677ff;
  padding: 0;
  font-size: 12px;
  height: auto;
}

.cancel-btn:hover {
  background: rgba(22, 119, 255, 0.1);
}

.quote-content {
  padding: 6px 12px 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.quote-content:hover {
  background: rgba(0, 0, 0, 0.02);
}

.quoted-message {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quote-text {
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

/* 深色主题适配 */
@media (prefers-color-scheme: dark) {
  .quote-message {
    background: #2a2a2a;
    border-left-color: #4096ff;
  }
  
  .quote-header {
    background: rgba(64, 150, 255, 0.1);
    color: #ccc;
  }
  
  .quote-icon {
    color: #4096ff;
  }
  
  .quote-content:hover {
    background: rgba(255, 255, 255, 0.05);
  }
  
  .quote-text {
    color: #ccc;
  }
  
  .cancel-btn {
    color: #4096ff;
  }
  
  .cancel-btn:hover {
    background: rgba(64, 150, 255, 0.1);
  }
}
</style>