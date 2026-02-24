<template>
  <div
    v-if="showPreview"
    class="common-preview"
    :class="`preview-${type}`"
  >
    <div class="preview-header">
      <span class="preview-title">{{ title }}</span>
      <button
        class="preview-close"
        @click="handleCancel"
      >
        <i class="el-icon-close"></i>
      </button>
    </div>

    <div class="preview-content">
      <slot name="content">
        <!-- 根据类型显示默认内容 -->
        <div
          v-if="type === 'reply'"
          class="reply-content"
        >
          <div class="reply-sender">@{{ senderName }}</div>
          <div class="reply-text">{{ formatPreviewContent(content) }}</div>
        </div>

        <div
          v-else-if="type === 'edit'"
          class="edit-content"
        >
          <div class="edit-label">正在编辑:</div>
          <div class="edit-text">{{ formatPreviewContent(content) }}</div>
        </div>

        <div
          v-else-if="type === 'voice'"
          class="voice-content"
        >
          <div class="voice-duration">{{ formatDuration(duration) }}</div>
          <div class="voice-controls">
            <button
              class="play-btn"
              :class="{ playing: isPlaying }"
              @click="togglePlay"
            >
              {{ isPlaying ? '停止' : '播放' }}
            </button>
            <button
              class="delete-btn"
              @click="handleDelete"
            >
              删除
            </button>
          </div>
        </div>
      </slot>
    </div>

    <div class="preview-actions">
      <slot name="actions">
        <button
          class="action-btn confirm"
          @click="handleConfirm"
        >
          {{ confirmText }}
        </button>
        <button
          class="action-btn cancel"
          @click="handleCancel"
        >
          {{ cancelText }}
        </button>
      </slot>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  type: {
    type: String,
    required: true,
    validator: (value) => ['reply', 'edit', 'voice', 'file', 'image'].includes(value)
  },
  title: {
    type: String,
    default: ''
  },
  content: {
    type: [String, Object],
    default: ''
  },
  senderName: {
    type: String,
    default: ''
  },
  duration: {
    type: Number, // 语音消息持续时间（毫秒）
    default: 0
  },
  showPreview: {
    type: Boolean,
    default: false
  },
  confirmText: {
    type: String,
    default: '确认'
  },
  cancelText: {
    type: String,
    default: '取消'
  }
})

const emit = defineEmits(['confirm', 'cancel', 'delete', 'play', 'stop'])

// 本地状态
const isPlaying = ref(false)

// 格式化预览内容
const formatPreviewContent = (content) => {
  if (typeof content === 'string') {
    return content.length > 50 ? content.substring(0, 50) + '...' : content
  }
  if (typeof content === 'object' && content !== null) {
    return JSON.stringify(content).substring(0, 50) + '...'
  }
  return String(content || '')
}

// 格式化持续时间
const formatDuration = (milliseconds) => {
  const seconds = Math.floor(milliseconds / 1000)
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs < 10 ? '0' : ''}${secs}`
}

// 事件处理
const handleConfirm = () => {
  emit('confirm')
}

const handleCancel = () => {
  emit('cancel')
}

const handleDelete = () => {
  emit('delete')
}

const togglePlay = () => {
  isPlaying.value = !isPlaying.value
  if (isPlaying.value) {
    emit('play')
  } else {
    emit('stop')
  }
}
</script>

<style scoped>
.common-preview {
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  padding: 12px;
  margin: 8px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  max-width: 100%;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--dt-border-lighter);
}

.preview-title {
  font-weight: 600;
  color: var(--dt-text-primary);
  font-size: 14px;
}

.preview-close {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--dt-text-quaternary);
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.preview-close:hover {
  background-color: var(--dt-bg-hover);
  color: var(--dt-text-primary);
}

.preview-content {
  flex: 1;
  margin-bottom: 12px;
}

.reply-content,
.edit-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reply-sender {
  font-weight: 600;
  color: var(--dt-brand-color);
  font-size: 13px;
}

.reply-text,
.edit-text {
  color: var(--dt-text-secondary);
  font-size: 13px;
  word-break: break-word;
}

.voice-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.voice-duration {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.voice-controls {
  display: flex;
  gap: 8px;
}

.play-btn,
.delete-btn {
  padding: 6px 12px;
  border-radius: 4px;
  border: 1px solid var(--dt-border-light);
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.play-btn {
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
}

.play-btn:hover {
  background: var(--dt-brand-hover);
  color: white;
}

.play-btn.playing {
  background: var(--dt-success-bg);
  color: var(--dt-success-color);
}

.delete-btn {
  background: var(--dt-danger-bg);
  color: var(--dt-danger-color);
}

.delete-btn:hover {
  background: var(--dt-danger-hover);
  color: white;
}

.preview-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.action-btn {
  padding: 6px 16px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.action-btn.confirm {
  background: var(--dt-brand-color);
  color: white;
}

.action-btn.confirm:hover {
  background: var(--dt-brand-hover);
}

.action-btn.cancel {
  background: var(--dt-bg-subtle);
  color: var(--dt-text-secondary);
}

.action-btn.cancel:hover {
  background: var(--dt-bg-hover);
}

/* 暗色模式 */
:global(.dark) {
  .common-preview {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  }

  .preview-header {
    border-bottom-color: var(--dt-border-dark);
  }

  .preview-title {
    color: var(--dt-text-primary-dark);
  }

  .preview-close {
    color: var(--dt-text-quaternary-dark);
  }

  .preview-close:hover {
    background-color: var(--dt-bg-hover-dark);
    color: var(--dt-text-primary-dark);
  }

  .reply-sender {
    color: var(--dt-brand-color);
  }

  .reply-text,
  .edit-text {
    color: var(--dt-text-secondary-dark);
  }

  .action-btn.cancel {
    background: var(--dt-bg-subtle-dark);
    color: var(--dt-text-secondary-dark);
  }

  .action-btn.cancel:hover {
    background: var(--dt-bg-hover-dark);
  }
}
</style>