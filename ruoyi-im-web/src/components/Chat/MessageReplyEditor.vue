<template>
  <div class="message-reply-editor" :class="{ visible: isVisible }">
    <!-- 回复预览区域 -->
    <transition name="reply-slide-down">
      <div v-if="replyMessage" class="reply-preview-area">
        <div class="reply-header">
          <div class="reply-title">
            <i class="el-icon-chat-line-round"></i>
            <span>回复 {{ replyMessage.senderName || '对方' }}</span>
          </div>
          <div class="reply-actions">
            <el-tooltip content="取消回复 (Esc)" placement="top">
              <button class="icon-button" @click="handleCancelReply">
                <i class="el-icon-close"></i>
              </button>
            </el-tooltip>
          </div>
        </div>

        <!-- 被回复消息预览 -->
        <div class="reply-message-preview" @click="handleScrollToOriginal">
          <div
            class="preview-content"
            :class="`preview-${replyMessage.messageType || replyMessage.type || 'text'}`"
          >
            <!-- 文本消息预览 -->
            <template v-if="getReplyType() === 'text'">
              <div class="preview-text">{{ getReplyContent() }}</div>
            </template>

            <!-- 图片消息预览 -->
            <template v-else-if="getReplyType() === 'image'">
              <div class="preview-image">
                <img :src="getReplyContent()" alt="图片" />
                <span class="preview-type-icon">[图片]</span>
              </div>
            </template>

            <!-- 文件消息预览 -->
            <template v-else-if="getReplyType() === 'file'">
              <div class="preview-file">
                <i class="el-icon-document"></i>
                <span>{{ getReplyContent().name || '文件' }}</span>
              </div>
            </template>

            <!-- 语音消息预览 -->
            <template v-else-if="getReplyType() === 'voice'">
              <div class="preview-voice">
                <i class="el-icon-microphone"></i>
                <span>[语音] {{ getReplyContent().duration || 0 }}秒</span>
              </div>
            </template>

            <!-- 视频消息预览 -->
            <template v-else-if="getReplyType() === 'video'">
              <div class="preview-video">
                <i class="el-icon-video-camera"></i>
                <span>[视频]</span>
              </div>
            </template>

            <!-- 位置消息预览 -->
            <template v-else-if="getReplyType() === 'location'">
              <div class="preview-location">
                <i class="el-icon-location"></i>
                <span>{{ getReplyContent().name || '位置' }}</span>
              </div>
            </template>

            <!-- 通用消息预览 -->
            <template v-else>
              <div class="preview-default">
                <i class="el-icon-chat-dot-round"></i>
                <span>[消息]</span>
              </div>
            </template>
          </div>
        </div>

        <!-- 快捷回复选项 -->
        <div class="quick-reply-options">
          <div
            v-for="(option, index) in quickReplyOptions"
            :key="index"
            class="quick-reply-item"
            @click="handleQuickReply(option)"
          >
            <span>{{ option.text }}</span>
            <span class="reply-hint">{{ option.hint }}</span>
          </div>
        </div>
      </div>
    </transition>

    <!-- 引用多条消息（转发模式） -->
    <transition name="multi-slide-down">
      <div v-if="selectedMessages.length > 0" class="multi-select-area">
        <div class="multi-select-header">
          <div class="select-title">
            <input
              type="checkbox"
              :checked="isAllSelected"
              class="select-all-checkbox"
              @change="handleToggleSelectAll"
            />
            <span>已选 {{ selectedMessages.length }} 条消息</span>
          </div>
          <div class="multi-select-actions">
            <el-button size="small" type="text" @click="handleBatchForward">
              <i class="el-icon-share"></i> 转发
            </el-button>
            <el-button size="small" type="text" @click="handleBatchDelete">
              <i class="el-icon-delete"></i> 删除
            </el-button>
            <el-button size="small" type="text" @click="handleClearSelection">
              <i class="el-icon-close"></i> 取消
            </el-button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

// Props
const props = defineProps({
  replyMessage: {
    type: Object,
    default: null,
  },
  selectedMessages: {
    type: Array,
    default: () => [],
  },
  isVisible: {
    type: Boolean,
    default: true,
  },
})

// Emits
const emit = defineEmits([
  'cancel-reply',
  'quick-reply',
  'scroll-to-original',
  'toggle-select-all',
  'batch-forward',
  'batch-delete',
  'clear-selection',
])

// 快捷回复选项
const quickReplyOptions = ref([
  { text: '好的', hint: 'Enter', emoji: '👌' },
  { text: '收到', hint: 'Ctrl+1', emoji: '👍' },
  { text: '谢谢', hint: 'Ctrl+2', emoji: '🙏' },
  { text: '稍等', hint: 'Ctrl+3', emoji: '⏳' },
])

// 是否全选
const isAllSelected = computed(() => {
  return false // 由父组件控制
})

// 获取回复消息类型
const getReplyType = () => {
  if (!props.replyMessage) return 'text'
  return props.replyMessage.messageType || props.replyMessage.type || 'text'
}

// 获取回复消息内容
const getReplyContent = () => {
  if (!props.replyMessage) return ''
  return props.replyMessage.content || props.replyMessage.text || ''
}

// 取消回复
const handleCancelReply = () => {
  emit('cancel-reply')
}

// 快捷回复
const handleQuickReply = option => {
  emit('quick-reply', {
    text: option.text,
    replyTo: props.replyMessage,
  })
}

// 滚动到原消息
const handleScrollToOriginal = () => {
  emit('scroll-to-original', props.replyMessage)
}

// 切换全选
const handleToggleSelectAll = () => {
  emit('toggle-select-all')
}

// 批量转发
const handleBatchForward = () => {
  emit('batch-forward', props.selectedMessages)
}

// 批量删除
const handleBatchDelete = () => {
  emit('batch-delete', props.selectedMessages)
}

// 清除选择
const handleClearSelection = () => {
  emit('clear-selection')
}

// 键盘快捷键
const handleKeyDown = event => {
  if (!props.isVisible || !props.replyMessage) return

  // Esc 取消回复
  if (event.key === 'Escape') {
    event.preventDefault()
    handleCancelReply()
    return
  }

  // Ctrl+1/2/3 快捷回复
  if (event.ctrlKey || event.metaKey) {
    if (event.key === '1') {
      event.preventDefault()
      handleQuickReply(quickReplyOptions.value[1]) // 收到
    } else if (event.key === '2') {
      event.preventDefault()
      handleQuickReply(quickReplyOptions.value[2]) // 谢谢
    } else if (event.key === '3') {
      event.preventDefault()
      handleQuickReply(quickReplyOptions.value[3]) // 稍等
    }
  }
}

// 挂载键盘事件监听
if (typeof window !== 'undefined') {
  window.addEventListener('keydown', handleKeyDown)
}

// 清理事件监听
import { onUnmounted } from 'vue'
onUnmounted(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('keydown', handleKeyDown)
  }
})
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.message-reply-editor {
  width: 100%;
  background: $bg-white;
}

// 回复预览区域
.reply-preview-area {
  border-bottom: 1px solid $border-light;
  background: linear-gradient(to bottom, $bg-light, $bg-white);
  animation: slideDown 0.3s ease-out;
}

.reply-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-sm $spacing-lg;
  background: rgba($primary-color, 0.05);
  border-left: 3px solid $primary-color;

  .reply-title {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    font-size: 13px;
    color: $primary-color;
    font-weight: 500;

    i {
      font-size: 14px;
    }
  }

  .reply-actions {
    display: flex;
    gap: $spacing-xs;

    .icon-button {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 24px;
      height: 24px;
      border: none;
      background: transparent;
      border-radius: $border-radius-sm;
      cursor: pointer;
      color: $text-secondary;
      transition: all $transition-base $ease-base;

      &:hover {
        background: rgba($error-color, 0.1);
        color: $error-color;
      }

      i {
        font-size: 14px;
      }
    }
  }
}

// 被回复消息预览
.reply-message-preview {
  padding: $spacing-sm $spacing-lg;
  cursor: pointer;
  transition: background-color $transition-base $ease-base;

  &:hover {
    background: rgba($primary-color, 0.03);
  }

  .preview-content {
    display: flex;
    align-items: center;
    padding: $spacing-sm $spacing-md;
    background: $bg-hover;
    border-radius: $border-radius-base;
    max-width: 100%;

    // 文本预览
    &.preview-text .preview-text {
      font-size: 13px;
      color: $text-secondary;
      line-height: 1.5;
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    // 图片预览
    &.preview-image {
      display: flex;
      align-items: center;
      gap: $spacing-sm;

      img {
        width: 40px;
        height: 40px;
        object-fit: cover;
        border-radius: $border-radius-sm;
      }

      .preview-type-icon {
        font-size: 12px;
        color: $text-tertiary;
      }
    }

    // 文件预览
    &.preview-file {
      display: flex;
      align-items: center;
      gap: $spacing-sm;

      i {
        font-size: 18px;
        color: $primary-color;
      }

      span {
        font-size: 13px;
        color: $text-secondary;
        max-width: 200px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    // 语音预览
    &.preview-voice {
      display: flex;
      align-items: center;
      gap: $spacing-sm;

      i {
        font-size: 16px;
        color: $success-color;
      }

      span {
        font-size: 12px;
        color: $text-tertiary;
      }
    }

    // 视频预览
    &.preview-video {
      display: flex;
      align-items: center;
      gap: $spacing-sm;

      i {
        font-size: 16px;
        color: $warning-color;
      }

      span {
        font-size: 12px;
        color: $text-tertiary;
      }
    }

    // 位置预览
    &.preview-location {
      display: flex;
      align-items: center;
      gap: $spacing-sm;

      i {
        font-size: 16px;
        color: $error-color;
      }

      span {
        font-size: 13px;
        color: $text-secondary;
        max-width: 200px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    // 默认预览
    &.preview-default {
      display: flex;
      align-items: center;
      gap: $spacing-sm;

      i {
        font-size: 16px;
        color: $text-tertiary;
      }

      span {
        font-size: 12px;
        color: $text-tertiary;
      }
    }
  }
}

// 快捷回复选项
.quick-reply-options {
  display: flex;
  padding: $spacing-sm $spacing-lg;
  gap: $spacing-sm;
  border-top: 1px solid $border-light;

  .quick-reply-item {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    padding: $spacing-xs $spacing-md;
    background: $bg-white;
    border: 1px solid $border-base;
    border-radius: $border-radius-base;
    cursor: pointer;
    transition: all $transition-base $ease-base;
    user-select: none;

    span:first-child {
      font-size: 13px;
      color: $text-primary;
    }

    .reply-hint {
      font-size: 11px;
      color: $text-tertiary;
      background: $bg-hover;
      padding: 2px 6px;
      border-radius: 4px;
    }

    &:hover {
      background: $primary-color-light;
      border-color: $primary-color;
      transform: translateY(-2px);
      box-shadow: 0 2px 8px rgba($primary-color, 0.15);

      span:first-child {
        color: $primary-color;
      }
    }

    &:active {
      transform: translateY(0);
    }
  }
}

// 多选区域
.multi-select-area {
  background: linear-gradient(to bottom, #fffbe6, $bg-white);
  border-bottom: 1px solid #ffe58f;
  animation: slideDown 0.3s ease-out;
}

.multi-select-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-sm $spacing-lg;

  .select-title {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-size: 13px;
    color: $text-primary;

    .select-all-checkbox {
      width: 16px;
      height: 16px;
      cursor: pointer;
    }
  }

  .multi-select-actions {
    display: flex;
    gap: $spacing-xs;

    :deep(.el-button) {
      font-size: 12px;
      padding: 4px 8px;
    }
  }
}

// 动画
.reply-slide-down-enter-active,
.reply-slide-down-leave-active {
  transition: all 0.3s ease;
}

.reply-slide-down-enter-from,
.reply-slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.multi-slide-down-enter-active,
.multi-slide-down-leave-active {
  transition: all 0.3s ease;
}

.multi-slide-down-enter-from,
.multi-slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式
@media (max-width: $breakpoint-md) {
  .reply-header,
  .reply-message-preview,
  .quick-reply-options {
    padding-left: $spacing-md;
    padding-right: $spacing-md;
  }

  .quick-reply-options {
    flex-wrap: wrap;

    .quick-reply-item {
      flex: 1;
      min-width: calc(50% - #{$spacing-sm});
      justify-content: center;
    }
  }
}
</style>
