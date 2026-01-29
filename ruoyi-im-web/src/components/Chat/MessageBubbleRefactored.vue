/**
 * 消息气泡组件 - 重构版本
 *
 * 将单一的大型组件拆分为多个子组件和组合式函数
 * 提高可维护性和可测试性
 */
<template>
  <el-dropdown
    trigger="contextmenu"
    @command="handleCommand"
    popper-class="message-context-menu"
  >
    <div
      ref="bubbleRef"
      class="message-bubble"
      :class="bubbleClasses"
      @click="handleClick"
      @touchstart="enhancedHandleTouchStart"
      @touchend="handleTouchEnd"
      @touchcancel="handleTouchEnd"
      @mousedown="handleMouseHold"
      @mouseup="handleMouseRelease"
      @mouseleave="handleMouseRelease"
    >
      <!-- 消息内容区域 -->
      <div class="bubble-content">
        <!-- 文本消息 -->
        <TextBubble
          v-if="message.type === 'TEXT'"
          :message="message"
          :has-markers="hasMarkers"
          @scroll-to="$emit('scroll-to', $event)"
        />

        <!-- 图片消息 -->
        <ImageBubble
          v-else-if="message.type === 'IMAGE'"
          :message="message"
          @preview="$emit('preview', $event)"
        />

        <!-- 文件消息 -->
        <FileBubble
          v-else-if="message.type === 'FILE'"
          :message="message"
          @download="$emit('download', $event)"
        />

        <!-- 视频消息 -->
        <VideoBubble
          v-else-if="message.type === 'VIDEO'"
          :message="message"
        />

        <!-- 语音消息 -->
        <VoiceBubble
          v-else-if="message.type === 'VOICE' || message.type === 'AUDIO'"
          :message="message"
        />

        <!-- 位置消息 -->
        <LocationBubble
          v-else-if="message.type === 'LOCATION'"
          :message="message"
        />

        <!-- 系统消息 -->
        <SystemBubble
          v-else-if="message.type === 'SYSTEM'"
          :message="message"
        />

        <!-- 撤回消息 -->
        <RecalledBubble
          v-else-if="message.type === 'RECALLED'"
          :message="message"
          @re-edit="$emit('re-edit', $event)"
        />

        <!-- 拍一拍消息 -->
        <NudgeMessageBubble
          v-else-if="message.type === 'NUDGE'"
          :nudge="{
            id: message.id,
            nudgerId: message.senderId,
            nudgerName: message.senderName,
            nudgerAvatar: message.senderAvatar,
            nudgedUserId: message.nudgedUserId,
            nudgedUserName: message.nudgedUserName,
            nudgeCount: message.nudgeCount || 1,
            createTime: message.createTime || message.timestamp
          }"
          @show-user="$emit('show-user', $event)"
        />

        <!-- 合并转发消息 -->
        <CombineMessagePreview
          v-else-if="message.type === 'COMBINE' || message.type === 'COMBINE_FORWARD'"
          :messages="parsedContent.messages || []"
          @click="$emit('command', 'view-combine', { ...message, messages: parsedContent.messages })"
        />

        <!-- 未知消息类型 -->
        <span v-else class="unknown-type">[{{ message.type }}]</span>
      </div>

      <!-- 状态指示器 -->
      <MessageStatus
        v-if="showStatus"
        :message="message"
        @retry="$emit('retry', message)"
      />

      <!-- 表情回应 -->
      <MessageReactions
        v-if="hasReactions"
        :message="message"
        @toggle="toggleReaction"
      />
    </div>

    <!-- 右键菜单 -->
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="copy" v-if="message.type === 'TEXT'">
          <el-icon><CopyDocument /></el-icon> <span>复制</span>
        </el-dropdown-item>
        <el-dropdown-item command="reply">
          <el-icon><ChatLineSquare /></el-icon> <span>回复</span>
        </el-dropdown-item>
        <el-dropdown-item command="emoji" divided>
          <span class="material-icons-outlined emoji-icon">sentiment_satisfied_alt</span>
          <span>表情表态</span>
        </el-dropdown-item>
        <el-dropdown-item command="at" v-if="!message.isOwn && sessionType === 'GROUP'">
          <el-icon><InfoFilled /></el-icon> <span>@ 提及</span>
        </el-dropdown-item>
        <el-dropdown-item command="forward" divided>
          <el-icon><Share /></el-icon> <span>转发</span>
        </el-dropdown-item>
        <el-dropdown-item command="todo">
          <el-icon><Checked /></el-icon> <span>设为待办</span>
        </el-dropdown-item>
        <el-dropdown-item command="pin" :class="{ 'is-pinned': message.isPinned }">
          <el-icon><Top /></el-icon> <span>{{ message.isPinned ? '取消置顶' : '置顶' }}</span>
        </el-dropdown-item>

        <el-dropdown-item v-if="message.isOwn && canRecall" command="recall" divided class="danger">
          <el-icon><RefreshLeft /></el-icon>
          <span>撤回{{ recallTimeDisplay ? ` (${recallTimeDisplay})` : '' }}</span>
        </el-dropdown-item>
        <el-dropdown-item v-if="message.isOwn" command="delete" class="danger">
          <el-icon><Delete /></el-icon> <span>删除</span>
        </el-dropdown-item>
        <el-dropdown-item v-if="message.isOwn && message.type === 'TEXT'" command="edit" divided>
          <el-icon><Edit /></el-icon> <span>编辑</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>

  <!-- AI表情表态浮窗 -->
  <AiEmojiReaction
    :visible="showAiEmojiPanel"
    :message="message"
    :position="aiEmojiPosition"
    @select="handleAiEmojiSelect"
    @close="showAiEmojiPanel = false"
  />
</template>

<script setup>
import { computed, ref } from 'vue'
import { CopyDocument, ChatLineSquare, Share, RefreshLeft, Delete, Edit, InfoFilled, Checked, Top } from '@element-plus/icons-vue'

// Composables
import { useMessageBubble } from './message-bubble/composables/useMessageBubble.js'
import { useMessageStatus } from './message-bubble/composables/useMessageStatus.js'
import { useMessageReaction } from './message-bubble/composables/useMessageReaction.js'

// 子组件
import TextBubble from './message-bubble/bubbles/TextBubble.vue'
import ImageBubble from './message-bubble/bubbles/ImageBubble.vue'
import FileBubble from './message-bubble/bubbles/FileBubble.vue'
import VoiceBubble from './message-bubble/bubbles/VoiceBubble.vue'
import VideoBubble from './message-bubble/bubbles/VideoBubble.vue'
import LocationBubble from './message-bubble/bubbles/LocationBubble.vue'
import SystemBubble from './message-bubble/bubbles/SystemBubble.vue'
import RecalledBubble from './message-bubble/bubbles/RecalledBubble.vue'
import MessageStatus from './message-bubble/parts/MessageStatus.vue'
import MessageReactions from './message-bubble/parts/MessageReactions.vue'

// 已有的组件（保持不变）
import NudgeMessageBubble from './NudgeMessageBubble.vue'
import CombineMessagePreview from './CombineMessagePreview.vue'
import AiEmojiReaction from './AiEmojiReaction.vue'

const props = defineProps({
  message: { type: Object, required: true },
  sessionType: { type: String, default: 'PRIVATE' }
})

const emit = defineEmits([
  'command', 'preview', 'download', 'at', 'scroll-to',
  'retry', 'toggle-reaction', 'add-reaction', 're-edit',
  'show-user', 'long-press'
])

// ==================== Composables ====================

const {
  bubbleRef,
  isLongPressing,
  isSelected,
  parsedContent,
  hasMarkers,
  canRecall: canRecallBubble,
  handleClick,
  handleCommand,
  handleRetry,
  handleTouchStart,
  handleTouchEnd,
  handleMouseHold,
  handleMouseRelease
} = useMessageBubble(props, emit)

const {
  recallTimeDisplay,
  isSending,
  isFailed,
  isRead
} = useMessageStatus(props)

const {
  hasReactions,
  toggleReaction,
  addReaction
} = useMessageReaction(props, emit)

// ==================== AI表情面板 ====================

const showAiEmojiPanel = ref(false)
const aiEmojiPosition = ref({ x: 0, y: 0 })

const handleAiEmojiSelect = (emoji) => {
  addReaction(emoji)
}

// 处理长按事件，显示AI表情面板
const originalHandleTouchStart = handleTouchStart
const enhancedHandleTouchStart = (e) => {
  originalHandleTouchStart(e)
  // 长按后显示AI表情面板
  setTimeout(() => {
    if (isLongPressing.value) {
      const rect = e.currentTarget?.getBoundingClientRect()
      if (rect) {
        // AI 面板尺寸
        const PANEL_WIDTH = 320
        const PANEL_HEIGHT = 400
        const PADDING = 20

        // 计算安全位置，防止超出屏幕边界
        const maxX = window.innerWidth - PANEL_WIDTH - PADDING
        const maxY = window.innerHeight - PANEL_HEIGHT - PADDING

        aiEmojiPosition.value = {
          x: Math.min(Math.max(PADDING, rect.right + 10), maxX),
          y: Math.max(PADDING, Math.min(rect.top, maxY))
        }
      }
      showAiEmojiPanel.value = true
    }
  }, 500)
}

// ==================== 计算属性 ====================

const bubbleClasses = computed(() => ({
  'is-own': props.message.isOwn,
  'is-selected': isSelected.value,
  'is-long-press': isLongPressing.value,
  [`type-${props.message.type.toLowerCase()}`]: true
}))

const showStatus = computed(() => {
  return props.message.isOwn && (isSending.value || isFailed.value || isRead.value)
})

// 合并可撤回状态
const canRecall = computed(() => {
  return canRecallBubble.value
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-bubble {
  position: relative;
  display: inline-flex;
  align-items: flex-end;
  max-width: min(520px, calc(100vw - 400px));
  animation: messagePop 0.3s var(--dt-ease-bounce);
}

@keyframes messagePop {
  0% {
    opacity: 0;
    transform: scale(0.9) translateY(10px);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.bubble-content {
  padding: 10px 12px;
  border-radius: var(--dt-radius-md);
  font-size: var(--dt-font-size-base);
  line-height: 1.5;
  word-break: break-word;
  overflow-wrap: break-word;
  transition: background-color var(--dt-transition-base);
}

// 对方消息样式
.message-bubble:not(.is-own) {
  .bubble-content {
    background: var(--dt-bubble-left-bg);
    border: 1px solid var(--dt-bubble-left-border);
    border-radius: var(--dt-radius-md) var(--dt-radius-md) var(--dt-radius-md) 2px;
  }

  &:hover .bubble-content {
    background: var(--dt-bg-card-hover);
  }
}

// 自己消息样式
.message-bubble.is-own {
  flex-direction: row-reverse;

  .bubble-content {
    background: var(--dt-bubble-right-bg);
    border: none;
    border-radius: var(--dt-radius-md) var(--dt-radius-md) 2px var(--dt-radius-md);
  }

  &:hover .bubble-content {
    background: var(--dt-bubble-right-bg-hover);
  }
}

// 选中状态
.message-bubble.is-selected .bubble-content {
  border: 2px solid var(--dt-brand-color);
  box-shadow: 0 0 0 2px var(--dt-brand-lighter);
}

// 长按状态
.message-bubble.is-long-press {
  animation: longPressPulse 0.3s ease-in-out;
}

@keyframes longPressPulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.01); }
  100% { transform: scale(1); }
}

// 消息类型特殊样式
.message-bubble.type-image .bubble-content {
  padding: 4px;
  background: var(--dt-bg-card) !important;
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-sm);
}

.message-bubble.type-video .bubble-content {
  padding: 0;
  background: #000 !important;
  border-radius: var(--dt-radius-sm);
}

.message-bubble.type-system .bubble-content {
  background: transparent;
  padding: 0;
}

.message-bubble.type-recalled .bubble-content {
  background: transparent;
  padding: 0;
}

// 未知类型
.unknown-type {
  color: var(--dt-text-tertiary);
  font-style: italic;
}

// 右键菜单图标样式
:deep(.message-context-menu) {
  .emoji-icon {
    font-size: 16px;
    color: #f5222d;
    margin-right: 8px;
  }

  .is-pinned {
    .el-icon__text {
      color: var(--dt-brand-color);
    }
  }

  .danger {
    color: var(--dt-error-color);
  }
}

// 暗色模式适配
:global(.dark) {
  .message-bubble:not(.is-own) .bubble-content {
    background: #2d2d2d;
    border-color: #3e3e3e;
  }

  .message-bubble.is-own .bubble-content {
    background: #0958d9;
  }

  .message-bubble:not(.is-own):hover .bubble-content {
    background: #1e293b;
  }

  .message-bubble.is-own:hover .bubble-content {
    background: #0e5fd9;
  }
}
</style>
