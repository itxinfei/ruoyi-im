<template>
  <div
    ref="containerRef"
    class="virtual-message-list im-scrollbar"
    :class="{
      scrolling: isScrolling,
      'at-bottom': atBottom,
    }"
  >
    <!-- 加载更多触发区域 -->
    <div
      v-if="hasMore"
      ref="loadMoreTrigger"
      class="load-more-trigger"
    >
      <el-button
        type="text"
        :loading="loading"
        :disabled="loading"
        @click="$emit('load-more')"
      >
        <template v-if="!loading">
          <i class="el-icon-arrow-up"></i>
          <span>加载更多</span>
        </template>
        <template v-else>
          <span>加载中...</span>
        </template>
      </el-button>
    </div>

    <!-- 虚拟列表容器 -->
    <div class="virtual-list-wrapper" :style="listStyle">
      <!-- 渲染可见的消息项 -->
      <div
        v-for="{ item, index, style } in visibleItems"
        :key="item.id || index"
        :ref="el => setItemRef(el, index)"
        class="virtual-list-item"
        :style="style"
        :data-index="index"
      >
        <!-- 日期分隔线 -->
        <div
          v-if="shouldShowDateDivider(item, index)"
          class="message-date"
        >
          <span class="date-badge">{{ formatDate(item.time) }}</span>
        </div>

        <!-- 消息气泡 -->
        <message-bubble
          :message="item"
          :is-mine="item.senderId === currentUserId"
          :show-sender="isGroup"
          @resend="$emit('resend', $event)"
          @download="$emit('download', $event)"
          @show-location="$emit('show-location', $event)"
          @image-load="handleImageLoad(index)"
          @preview-image="$emit('preview-image', item)"
          @context-menu="$emit('context-menu', $event)"
        />
      </div>
    </div>

    <!-- 滚动到底部按钮 -->
    <transition name="scroll-bottom">
      <button
        v-show="showScrollToBottom"
        class="scroll-to-bottom"
        aria-label="滚动到底部"
        @click="handleScrollToBottom"
      >
        <i class="el-icon-bottom"></i>
        <transition name="badge-pop">
          <span v-if="newMessageCount > 0" class="message-count">
            {{ formatMessageCount(newMessageCount) }}
          </span>
        </transition>
      </button>
    </transition>

    <!-- 新消息提醒 -->
    <transition name="new-message-tip">
      <div
        v-show="showNewMessageTip && !showScrollToBottom"
        class="new-message-tip"
        @click="handleScrollToBottom"
      >
        <div class="tip-content">
          <i class="el-icon-bell"></i>
          <span class="tip-text">有新消息</span>
          <el-badge
            :value="formatMessageCount(newMessageCount)"
            class="tip-badge"
            :hidden="newMessageCount <= 0"
          />
        </div>
      </div>
    </transition>

    <!-- 空状态 -->
    <transition name="empty-state">
      <div v-if="isEmpty" class="empty-state">
        <div class="empty-icon">
          <i class="el-icon-chat-dot-round"></i>
        </div>
        <div class="empty-text">暂无消息</div>
        <div class="empty-hint">开始聊天吧</div>
      </div>
    </transition>
  </div>
</template>

<script setup>
/**
 * @file VirtualMessageList.vue
 * @description 虚拟消息列表组件 - 使用虚拟滚动优化大量消息的渲染性能
 * @author IM System
 * @version 1.0.0
 */

import { ref, computed, watch, nextTick, onMounted } from 'vue'
import MessageBubble from './MessageBubble.vue'
import { useVirtualList } from '@/utils/virtualList/useVirtualList'
import { formatMessageDate } from '@/utils/message'

// ==================== Props 定义 ====================
const props = defineProps({
  /**
   * 消息列表数据
   */
  messages: {
    type: Array,
    required: true,
  },
  /**
   * 当前用户 ID
   */
  currentUserId: {
    type: [String, Number],
    required: true,
  },
  /**
   * 是否为群聊
   */
  isGroup: {
    type: Boolean,
    default: false,
  },
  /**
   * 是否有更多历史消息
   */
  hasMore: {
    type: Boolean,
    default: true,
  },
  /**
   * 是否正在加载
   */
  loading: {
    type: Boolean,
    default: false,
  },
  /**
   * 新消息数量
   */
  newMessageCount: {
    type: Number,
    default: 0,
  },
})

// ==================== Emits 定义 ====================
const emit = defineEmits([
  'load-more',
  'resend',
  'download',
  'show-location',
  'preview-image',
  'context-menu',
  'scroll-to-bottom',
])

// ==================== 响应式状态 ====================

/** 容器 DOM 引用 */
const containerRef = ref(null)

/** 加载更多触发器引用 */
const loadMoreTrigger = ref(null)

/** 消息项 DOM 引用映射 */
const itemRefs = new Map()

/** 是否显示滚动到底部按钮 */
const showScrollToBottom = ref(false)

/** 是否显示新消息提示 */
const showNewMessageTip = ref(false)

/** 是否在底部 */
const atBottom = ref(true)

/** 滚动阈值 */
const scrollThreshold = 100

// ==================== 虚拟列表 ====================

/**
 * 估算消息高度
 * @param {Object} message - 消息对象
 * @returns {number} 估算高度
 */
const estimateMessageHeight = (message) => {
  if (!message) return 60

  // 根据消息类型估算高度
  switch (message.type) {
    case 'text':
      // 根据文本长度估算
      const textLength = message.content?.length || 0
      const lines = Math.ceil(textLength / 30)
      return Math.max(60, lines * 20 + 40)

    case 'image':
      return 200

    case 'file':
      return 80

    case 'voice':
      return 60

    case 'video':
      return 240

    case 'location':
      return 160

    case 'vote':
      return 200

    case 'code':
      return 150

    default:
      return 60
  }
}

// 使用虚拟列表
const messagesRef = computed(() => props.messages)

const {
  visibleItems,
  listStyle,
  isScrolling,
  scrollToBottom,
  scrollToIndex,
  isAtBottom,
  updateItemHeight,
  updateVisibleRange,
} = useVirtualList({
  items: messagesRef,
  estimateHeight: estimateMessageHeight,
  bufferSize: 5,
  containerRef,
})

// ==================== 计算属性 ====================

/**
 * 是否为空列表
 */
const isEmpty = computed(() => props.messages.length === 0)

// ==================== 方法定义 ====================

/**
 * 设置消息项 DOM 引用
 * @param {HTMLElement} el - DOM 元素
 * @param {number} index - 消息索引
 */
const setItemRef = (el, index) => {
  if (el) {
    itemRefs.set(index, el)

    // 观察元素尺寸变化
    nextTick(() => {
      const height = el.offsetHeight
      if (height > 0) {
        updateItemHeight(index, height)
      }
    })
  } else {
    itemRefs.delete(index)
  }
}

/**
 * 判断是否显示日期分隔线
 * @param {Object} message - 当前消息
 * @param {number} index - 消息索引
 * @returns {boolean}
 */
const shouldShowDateDivider = (message, index) => {
  if (index === 0) return true

  const prevMessage = props.messages[index - 1]
  if (!prevMessage) return false

  const currentDate = new Date(message.time).toDateString()
  const prevDate = new Date(prevMessage.time).toDateString()

  return currentDate !== prevDate
}

/**
 * 格式化日期
 * @param {string|number} time - 时间
 * @returns {string}
 */
const formatDate = (time) => {
  return formatMessageDate(time)
}

/**
 * 格式化消息数量
 * @param {number} count - 数量
 * @returns {string}
 */
const formatMessageCount = (count) => {
  return count > 99 ? '99+' : count.toString()
}

/**
 * 处理图片加载完成
 * @param {number} index - 消息索引
 */
const handleImageLoad = (index) => {
  const el = itemRefs.get(index)
  if (el) {
    const height = el.offsetHeight
    updateItemHeight(index, height)

    // 如果在底部，保持滚动到底部
    if (atBottom.value) {
      scrollToBottom({ behavior: 'smooth' })
    }
  }
}

/**
 * 处理滚动到底部
 */
const handleScrollToBottom = () => {
  scrollToBottom({ behavior: 'smooth' })
  showNewMessageTip.value = false
  emit('scroll-to-bottom')
}

/**
 * 更新滚动状态
 */
const updateScrollState = () => {
  atBottom.value = isAtBottom(scrollThreshold)

  if (!containerRef.value) return

  const { scrollTop, scrollHeight, clientHeight } = containerRef.value
  const distanceFromBottom = scrollHeight - scrollTop - clientHeight

  showScrollToBottom.value = distanceFromBottom > scrollThreshold

  // 如果滚动到底部，隐藏新消息提示
  if (atBottom.value) {
    showNewMessageTip.value = false
  }
}

/**
 * 显示新消息通知
 */
const showNewMessageNotification = () => {
  if (!atBottom.value) {
    showNewMessageTip.value = true
  }
}

// ==================== 监听器 ====================

/**
 * 监听消息列表变化
 */
watch(() => props.messages.length, (newLength, oldLength) => {
  if (newLength > oldLength) {
    // 有新消息
    nextTick(() => {
      if (atBottom.value) {
        scrollToBottom({ behavior: 'smooth' })
      } else {
        showNewMessageNotification()
      }
    })
  }
})

/**
 * 监听新消息数量
 */
watch(() => props.newMessageCount, (count) => {
  if (count > 0 && !atBottom.value) {
    showNewMessageTip.value = true
  }
})

// ==================== 生命周期 ====================

onMounted(() => {
  if (containerRef.value) {
    containerRef.value.addEventListener('scroll', updateScrollState, { passive: true })

    // 初始滚动到底部
    nextTick(() => {
      scrollToBottom({ behavior: 'auto' })
    })
  }
})

// ==================== 暴露方法 ====================

defineExpose({
  scrollToBottom: handleScrollToBottom,
  scrollToIndex,
  updateVisibleRange,
})
</script>

<style lang="scss" scoped>
@import '@/styles/dingtalk-theme.scss';

.virtual-message-list {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
  background-color: $bg-base;
  -webkit-overflow-scrolling: touch;

  &.scrolling {
    cursor: grabbing;
  }

  &.at-bottom {
    scroll-behavior: smooth;
  }
}

.load-more-trigger {
  text-align: center;
  padding: $spacing-md 0;

  .el-button {
    display: inline-flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-sm $spacing-lg;
    border-radius: $border-radius-xl;
    transition: all $transition-base $ease-base;

    &:hover:not(:disabled) {
      background-color: rgba($primary-color, 0.1);
      transform: translateY(-2px);
    }
  }
}

.virtual-list-wrapper {
  position: relative;
  width: 100%;
}

.virtual-list-item {
  width: 100%;
  box-sizing: border-box;
  padding: 0 $spacing-lg;
}

.message-date {
  text-align: center;
  margin: $spacing-lg 0 $spacing-sm;

  .date-badge {
    display: inline-block;
    background-color: rgba(0, 0, 0, 0.08);
    padding: $spacing-xs $spacing-md;
    border-radius: $border-radius-base;
    font-size: 12px;
    color: $text-tertiary;
    font-weight: 500;
  }
}

.scroll-to-bottom {
  position: fixed;
  right: 40px;
  bottom: 100px;
  width: 48px;
  height: 48px;
  border-radius: $border-radius-round;
  background: linear-gradient(135deg, $primary-color 0%, $primary-color-hover 100%);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba($primary-color, 0.4);
  transition: all $transition-base $ease-out;
  z-index: 100;

  &:hover {
    transform: translateY(-4px) scale(1.05);
    box-shadow: 0 6px 20px rgba($primary-color, 0.5);
  }

  &:active {
    transform: translateY(-2px) scale(1);
  }

  i {
    font-size: 20px;
  }

  .message-count {
    position: absolute;
    top: -4px;
    right: -4px;
    min-width: 20px;
    height: 20px;
    padding: 0 6px;
    background-color: $error-color;
    color: white;
    font-size: 11px;
    font-weight: 600;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 8px rgba($error-color, 0.4);
  }
}

.new-message-tip {
  position: fixed;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  cursor: pointer;
  z-index: 99;

  .tip-content {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    background: linear-gradient(135deg, $bg-white 0%, $bg-light 100%);
    padding: $spacing-md $spacing-xl;
    border-radius: $border-radius-xl;
    box-shadow: $shadow-base;
    transition: all $transition-base $ease-out;

    &:hover {
      transform: translateY(-2px);
      box-shadow: $shadow-lg;
    }

    i {
      font-size: 18px;
      color: $primary-color;
      animation: bellShake 0.5s ease-in-out infinite;
    }

    .tip-text {
      font-size: 14px;
      color: $text-primary;
      font-weight: 500;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 300px;
  padding: 40px $spacing-xl;

  .empty-icon {
    width: 120px;
    height: 120px;
    border-radius: $border-radius-round;
    background: linear-gradient(
      135deg,
      rgba($primary-color, 0.1) 0%,
      rgba($primary-color-hover, 0.1) 100%
    );
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: $spacing-xl;
    animation: float 3s ease-in-out infinite;

    i {
      font-size: 64px;
      color: $primary-color;
      opacity: 0.6;
    }
  }

  .empty-text {
    font-size: 18px;
    color: $text-primary;
    font-weight: 500;
    margin-bottom: $spacing-sm;
  }

  .empty-hint {
    font-size: 14px;
    color: $text-tertiary;
  }
}

// 动画
@keyframes bellShake {
  0%, 100% { transform: rotate(0deg); }
  10%, 30%, 50%, 70%, 90% { transform: rotate(-10deg); }
  20%, 40%, 60%, 80% { transform: rotate(10deg); }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

// 过渡动画
.scroll-bottom-enter-active,
.scroll-bottom-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.scroll-bottom-enter-from,
.scroll-bottom-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.8);
}

.new-message-tip-enter-active,
.new-message-tip-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.new-message-tip-enter-from,
.new-message-tip-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(20px);
}

.badge-pop-enter-active {
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.badge-pop-enter-from {
  opacity: 0;
  transform: scale(0);
}

.empty-state-enter-active {
  transition: all 0.5s ease;
}

.empty-state-enter-from {
  opacity: 0;
  transform: scale(0.9);
}

// 响应式
@media (max-width: $breakpoint-md) {
  .virtual-list-item {
    padding: 0 $spacing-md;
  }

  .scroll-to-bottom {
    right: $spacing-xl;
    bottom: 80px;
    width: 44px;
    height: 44px;

    i {
      font-size: 18px;
    }
  }

  .new-message-tip {
    bottom: 80px;

    .tip-content {
      padding: $spacing-sm $spacing-lg;

      i {
        font-size: 16px;
      }

      .tip-text {
        font-size: 13px;
      }
    }
  }

  .empty-state {
    padding: $spacing-xl;

    .empty-icon {
      width: 80px;
      height: 80px;

      i {
        font-size: 40px;
      }
    }

    .empty-text {
      font-size: 16px;
    }

    .empty-hint {
      font-size: 13px;
    }
  }
}

// 减少动画偏好
@media (prefers-reduced-motion: reduce) {
  .scroll-to-bottom,
  .new-message-tip,
  .empty-state {
    animation: none;
    transition: none;
  }

  .new-message-tip .tip-content i {
    animation: none;
  }

  .empty-icon {
    animation: none;
  }
}
</style>
