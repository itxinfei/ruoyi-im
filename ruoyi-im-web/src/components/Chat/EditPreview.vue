/**
 * 编辑消息预览组件
 * 显示正在编辑的消息内容，提供取消编辑功能
 */
<template>
  <div class="edit-preview-container">
    <div class="edit-header">
      <div class="edit-title">
        <span class="material-icons-outlined edit-icon">edit</span>
        <span class="edit-label">正在编辑消息</span>
      </div>
      <el-button
        size="small"
        text
        class="cancel-btn"
        @click="$emit('cancel')"
      >
        <el-icon><Close /></el-icon>
        <span>取消 (ESC)</span>
      </el-button>
    </div>

    <!-- 原消息预览 -->
    <div class="original-message">
      <div class="message-label">原消息内容:</div>
      <div class="message-content">{{ displayContent }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { Close } from '@element-plus/icons-vue'

const props = defineProps({
  content: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['cancel'])

// 显示内容（截断长文本）
const displayContent = computed(() => {
  if (!props.content) return ''
  const maxLength = 100
  if (props.content.length <= maxLength) return props.content
  return props.content.substring(0, maxLength) + '...'
})

// 处理ESC键取消编辑
const handleKeydown = (e) => {
  if (e.key === 'Escape') {
    emit('cancel')
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.edit-preview-container {
  padding: 12px 16px;
  margin-bottom: 12px;
  border-radius: var(--dt-radius-md);
  background: linear-gradient(135deg, rgba(82, 196, 26, 0.08) 0%, rgba(82, 196, 26, 0.04) 100%);
  border: 1px solid rgba(82, 196, 26, 0.2);
}

.edit-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.edit-title {
  display: flex;
  align-items: center;
  gap: 6px;
}

.edit-icon {
  font-size: 16px;
  color: var(--dt-success-color);
}

.edit-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--dt-success-color);
}

.cancel-btn {
  font-size: 12px;
  color: var(--dt-text-secondary);
  padding: 4px 8px;

  &:hover {
    color: var(--dt-error-color);
    background: rgba(255, 77, 79, 0.1);
  }

  .el-icon {
    font-size: 14px;
    margin-right: 2px;
  }
}

.original-message {
  padding: 8px 12px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-sm);
  border-left: 2px solid var(--dt-border-color);
}

.message-label {
  font-size: 11px;
  color: var(--dt-text-quaternary);
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.message-content {
  font-size: 13px;
  color: var(--dt-text-secondary);
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
  max-height: 80px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: var(--dt-radius-sm);
  }
}

// 暗色模式
:global(.dark) {
  .edit-preview-container {
    background: linear-gradient(135deg, rgba(82, 196, 26, 0.12) 0%, rgba(82, 196, 26, 0.06) 100%);
    border-color: rgba(82, 196, 26, 0.3);
  }

  .original-message {
    background: var(--dt-bg-card-dark);
    border-left-color: var(--dt-border-dark);
  }

  .message-content {
    color: var(--dt-text-secondary-dark);
  }
}
</style>
