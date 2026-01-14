<template>
  <div
    ref="messageList"
    class="message-list im-scrollbar"
    :class="{
      scrolling: isScrolling,
      'at-bottom': isAtBottom(),
      'has-new-messages': showNewMessageTip,
    }"
    @scroll="handleScroll"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove"
    @touchend="handleTouchEnd"
  >
    <!-- 加载更多 -->
    <transition name="load-more">
      <div v-if="hasMore" class="load-more">
        <el-button
          type="text"
          :loading="loading"
          :class="{ 'loading-active': loading }"
          :disabled="loading"
          @click="loadMoreMessages"
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
    </transition>

    <!-- 消息列表 -->
    <transition-group name="message-group" tag="div" class="message-groups">
      <div v-for="(group, date) in groupedMessages" :key="date" class="message-group">
        <div class="message-date">
          <span class="date-badge">{{ formatDate(date) }}</span>
        </div>

        <!-- 消息气泡 -->
        <message-bubble
          v-for="message in group"
          :key="message.id"
          :message="message"
          :is-mine="String(message.senderId) === String(currentUserId)"
          :show-sender="isGroup"
          @resend="handleResend"
          @download-file="handleDownload"
          @show-location="handleShowLocation"
          @image-load="handleImageLoad"
          @preview-image="handlePreviewImage"
          @context-menu="handleContextMenu"
        />
      </div>
    </transition-group>

    <!-- 滚动到底部按钮 -->
    <transition name="scroll-bottom">
      <button
        v-show="showScrollToBottom"
        class="scroll-to-bottom"
        aria-label="滚动到底部"
        @click="scrollToBottom"
      >
        <i class="el-icon-bottom"></i>
        <transition name="badge-pop">
          <span v-if="newMessageCount > 0" class="message-count">{{
            formatMessageCount(newMessageCount)
          }}</span>
        </transition>
      </button>
    </transition>

    <!-- 新消息提醒 -->
    <transition name="new-message-tip">
      <div
        v-show="showNewMessageTip && !showScrollToBottom"
        class="new-message-tip"
        role="button"
        aria-label="查看新消息"
        @click="scrollToBottom"
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

    <!-- 位置信息弹窗 -->
    <el-dialog
      v-model="locationDialogVisible"
      title="位置信息"
      width="600px"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      class="location-dialog"
    >
      <div ref="locationMap" class="location-map"></div>
    </el-dialog>

    <!-- 图片预览组件 -->
    <image-preview
      v-model:visible="imagePreviewVisible"
      :images="previewImages"
      :initial-index="previewInitialIndex"
      @close="imagePreviewVisible = false"
    />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { formatMessageDate } from '@/utils/message'
import { debounce, throttle } from '@/utils/common'
import MessageBubble from './MessageBubble.vue'
import ImagePreview from './ImagePreview.vue'

export default {
  name: 'MessageList',
  components: {
    MessageBubble,
    ImagePreview,
  },
  props: {
    sessionId: {
      type: String,
      required: true,
    },
    isGroup: {
      type: Boolean,
      default: false,
    },
  },
  emits: [
    'context-menu',
    'preview-image',
    'download-file',
    'image-load',
    'load-more',
    'resend',
    'reply',
    'forward',
    'recall',
    'delete',
    'copy',
    'scroll-to-bottom',
  ],
  data() {
    return {
      loading: false,
      hasMore: true,
      page: 1,
      pageSize: 20,
      showNewMessageTip: false,
      newMessageCount: 0,
      locationDialogVisible: false,
      currentLocation: null,
      isScrollingToBottom: false,
      lastScrollTop: 0,
      isScrolling: false,
      scrollTimer: null,
      touchStartY: 0,
      touchStartTime: 0,
      isTouchScrolling: false,
      scrollDirection: 'down',
      showScrollToBottom: false,
      scrollThreshold: 100,
      reducedMotion: false,
      // 图片预览状态
      imagePreviewVisible: false,
      previewImages: [],
      previewInitialIndex: 0,
    }
  },
  computed: {
    ...mapGetters(['messagesBySession', 'currentUserId']),
    groupedMessages() {
      const messages = this.messagesBySession(this.sessionId)
      if (!messages || messages.length === 0) return {}

      return messages.reduce((groups, message) => {
        const date = new Date(message.time).toLocaleDateString()
        if (!groups[date]) {
          groups[date] = []
        }
        groups[date].push(message)
        return groups
      }, {})
    },
    isEmpty() {
      const messages = this.messagesBySession(this.sessionId)
      return !messages || messages.length === 0
    },
  },
  watch: {
    sessionId: {
      immediate: true,
      handler(newVal) {
        if (newVal) {
          this.resetList()
          this.loadMessages()
        }
      },
    },
    messagesBySession: {
      deep: true,
      handler() {
        this.$nextTick(() => {
          if (this.isAtBottom()) {
            this.scrollToBottom()
          } else {
            this.showNewMessageNotification()
          }
        })
      },
    },
  },
  beforeUnmount() {
    if (this.scrollTimer) {
      clearTimeout(this.scrollTimer)
    }
    this.hideNewMessageTip()
  },
  mounted() {
    this.checkReducedMotion()
    this.handleScroll = debounce(this.handleScrollInternal, 100)
    this.handleTouchMove = throttle(this.handleTouchMoveInternal, 16)
  },
  methods: {
    checkReducedMotion() {
      this.reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    },

    resetList() {
      this.page = 1
      this.hasMore = true
      this.showNewMessageTip = false
      this.newMessageCount = 0
      this.showScrollToBottom = false
    },

    async loadMessages() {
      if (this.loading || !this.hasMore) return

      this.loading = true
      try {
        const params = {
          sessionId: this.sessionId,
          page: this.page,
          pageSize: this.pageSize,
        }

        await this.$store.dispatch('im/loadMessages', params)

        this.page++
        this.hasMore = true

        if (this.page === 2) {
          this.$nextTick(() => {
            this.scrollToBottom({ smooth: true })
          })
        }
      } catch (error) {
        this.$message.error('加载消息失败')
      } finally {
        this.loading = false
      }
    },

    loadMoreMessages() {
      this.loadMessages()
    },

    handleScrollInternal(e) {
      const target = e.target
      const scrollTop = target.scrollTop
      const scrollHeight = target.scrollHeight
      const clientHeight = target.clientHeight

      this.scrollDirection = scrollTop > this.lastScrollTop ? 'down' : 'up'
      this.lastScrollTop = scrollTop

      if (scrollTop === 0 && !this.loading && this.hasMore) {
        this.loadMoreMessages()
      }

      const distanceFromBottom = scrollHeight - scrollTop - clientHeight
      this.showScrollToBottom = distanceFromBottom > this.scrollThreshold

      if (this.isAtBottom()) {
        this.hideNewMessageTip()
      }

      this.setScrollingState()
    },

    handleScroll(e) {
      this.handleScrollInternal(e)
    },

    setScrollingState() {
      this.isScrolling = true
      if (this.scrollTimer) {
        clearTimeout(this.scrollTimer)
      }
      this.scrollTimer = setTimeout(() => {
        this.isScrolling = false
      }, 150)
    },

    handleTouchStart(e) {
      this.touchStartY = e.touches[0].clientY
      this.touchStartTime = Date.now()
      this.isTouchScrolling = true
    },

    handleTouchMoveInternal(e) {
      if (!this.isTouchScrolling) return

      const touchY = e.touches[0].clientY
      const deltaY = touchY - this.touchStartY
      const deltaTime = Date.now() - this.touchStartTime

      if (deltaTime > 0) {
        const velocity = Math.abs(deltaY) / deltaTime
        if (velocity > 0.5) {
          this.isScrolling = true
          if (this.scrollTimer) {
            clearTimeout(this.scrollTimer)
          }
          this.scrollTimer = setTimeout(() => {
            this.isScrolling = false
          }, 150)
        }
      }
    },

    handleTouchMove(e) {
      this.handleTouchMoveInternal(e)
    },

    handleTouchEnd() {
      this.isTouchScrolling = false
    },

    isAtBottom() {
      const el = this.$refs.messageList
      if (!el) return true

      const tolerance = 50
      return el.scrollHeight - el.scrollTop - el.clientHeight <= tolerance
    },

    scrollToBottom(options = {}) {
      const { smooth = false } = options
      this.isScrollingToBottom = true
      this.$nextTick(() => {
        const el = this.$refs.messageList
        if (el) {
          if (smooth && !this.reducedMotion) {
            el.scrollTo({
              top: el.scrollHeight,
              behavior: 'smooth',
            })
          } else {
            el.scrollTop = el.scrollHeight
          }
        }
        this.isScrollingToBottom = false
      })
    },

    showNewMessageNotification() {
      if (!this.isScrollingToBottom) {
        this.showNewMessageTip = true
        this.newMessageCount++
        if (this.newMessageCount > 99) {
          this.newMessageCount = 99
        }
      }
    },

    hideNewMessageTip() {
      this.showNewMessageTip = false
      this.newMessageCount = 0
    },

    formatMessageCount(count) {
      if (count > 99) return '99+'
      return count.toString()
    },

    formatDate(dateStr) {
      return formatMessageDate(dateStr)
    },

    handleResend(message) {
      this.$store.dispatch('im/resendMessage', message)
    },

    handleDownload(message) {
      this.$store.dispatch('im/downloadFile', message)
    },

    handleShowLocation(location) {
      this.currentLocation = location
      this.locationDialogVisible = true
      this.$nextTick(() => {
        this.initLocationMap()
      })
    },

    handleImageLoad() {
      this.$nextTick(() => {
        if (this.isAtBottom()) {
          this.scrollToBottom({ smooth: true })
        }
      })
    },

    initLocationMap() {
      if (!this.currentLocation) return
    },

    // 处理图片预览
    handlePreviewImage(message) {
      // 收集当前会话中的所有图片消息
      const messages = this.messagesBySession(this.sessionId) || []
      const imageMessages = messages.filter(m => m.type === 'image')

      if (imageMessages.length === 0) return

      // 获取图片URL列表
      this.previewImages = imageMessages.map(m => m.content)

      // 找到当前点击图片的索引
      const currentIndex = imageMessages.findIndex(m => m.id === message.id)
      this.previewInitialIndex = currentIndex >= 0 ? currentIndex : 0

      // 显示预览
      this.imagePreviewVisible = true
    },

    // 处理右键菜单
    handleContextMenu(payload) {
      this.$emit('context-menu', payload)
    },
  },
}
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.message-list {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  padding: $spacing-xl;
  position: relative;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  background-color: $bg-base;
  transition: background-color $transition-base $ease-base;

  &.scrolling {
    cursor: grabbing;
  }

  &.at-bottom {
    scroll-behavior: smooth;
  }

  .load-more {
    text-align: center;
    margin-bottom: $spacing-xl;
    padding: $spacing-sm 0;

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

      &:active:not(:disabled) {
        transform: translateY(0);
      }

      &.loading-active {
        opacity: 0.7;
        cursor: not-allowed;
      }

      i {
        font-size: 16px;
        transition: transform $transition-base $ease-base;
      }

      &:hover i {
        transform: translateY(-2px);
      }
    }
  }

  .message-groups {
    display: flex;
    flex-direction: column;
    gap: $spacing-xl;
  }

  .message-group {
    animation: fadeInUp $transition-base $ease-out;
  }

  .message-date {
    text-align: center;
    margin: $spacing-xl 0 $spacing-sm;

    .date-badge {
      display: inline-block;
      background-color: rgba(0, 0, 0, 0.08);
      padding: $spacing-xs $spacing-md;
      border-radius: $border-radius-base;
      font-size: 12px;
      color: $text-tertiary;
      font-weight: 500;
      transition: all $transition-base $ease-base;

      &:hover {
        background-color: rgba(0, 0, 0, 0.12);
      }
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
      transition: transform $transition-base $ease-base;
    }

    &:hover i {
      transform: translateY(2px);
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
      font-weight: 500;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 8px rgba($error-color, 0.4);
      animation: badgePop $transition-base $ease-bounce;
    }
  }

  .new-message-tip {
    position: fixed;
    bottom: 100px;
    left: 50%;
    transform: translateX(-50%);
    cursor: pointer;
    z-index: 99;
    animation: slideUp $transition-base $ease-out;

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

      &:active {
        transform: translateY(0);
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

      .tip-badge {
        :deep(.el-badge__content) {
          background-color: $error-color;
          border: none;
          animation: badgePop $transition-base $ease-bounce;
        }
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
    animation: fadeIn $transition-slow $ease-out;

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
}

.location-dialog {
  :deep(.el-dialog) {
    border-radius: $border-radius-lg;
    overflow: hidden;
  }

  :deep(.el-dialog__header) {
    padding: $spacing-xl $spacing-xl;
    border-bottom: 1px solid $border-light;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.location-map {
  height: 400px;
  width: 100%;
  background-color: $bg-base;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-tertiary;
  font-size: 14px;
}

.transition-enter-active,
.transition-leave-active {
  transition: all 0.3s ease;
}

.transition-enter-from,
.transition-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.load-more-enter-active,
.load-more-leave-active {
  transition: all 0.3s ease;
}

.load-more-enter-from,
.load-more-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.message-group-enter-active {
  transition: all 0.3s ease;
}

.message-group-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.date-divider-enter-active,
.date-divider-leave-active {
  transition: all 0.3s ease;
}

.date-divider-enter-from,
.date-divider-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

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

.badge-pop-leave-active {
  transition: all 0.2s ease;
}

.badge-pop-leave-to {
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

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

@keyframes badgePop {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes bellShake {
  0%,
  100% {
    transform: rotate(0deg);
  }
  10%,
  30%,
  50%,
  70%,
  90% {
    transform: rotate(-10deg);
  }
  20%,
  40%,
  60%,
  80% {
    transform: rotate(10deg);
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@media (max-width: $breakpoint-md) {
  .message-list {
    padding: $spacing-md;

    .load-more {
      margin-bottom: $spacing-md;
    }

    .message-group {
      margin-bottom: $spacing-md;
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
        gap: $spacing-xs;

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
}

@media (prefers-reduced-motion: reduce) {
  .message-list {
    scroll-behavior: auto;
    transition: none;
  }

  .message-group,
  .scroll-to-bottom,
  .new-message-tip,
  .empty-state {
    animation: none;
    transition: none;
  }

  .empty-icon {
    animation: none;
  }

  .new-message-tip .tip-content i {
    animation: none;
  }
}
</style>
