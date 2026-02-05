/**
 * 消息气泡组件 - 重构版本
 *
 * 将单一的大型组件拆分为多个子组件和组合式函数
 * 提高可维护性和可测试性
 */
<template>
  <div class="message-bubble-wrapper">
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
      @contextmenu.prevent="handleContextMenu"
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

        <!-- 链接预览卡片 -->
        <LinkCard
          v-if="message.type === 'TEXT' && messageLinks.length > 0"
          :link="messageLinks[0]"
          class="message-link-card"
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
          :is-read="message.isRead || message.readStatus === 'READ'"
          @read="$emit('mark-read', message)"
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
          @click="handleCombineClick"
        />

        <!-- 未知消息类型 -->
        <span
          v-else
          class="unknown-type"
        >[{{ message.type }}]</span>
      </div>

      <!-- 状态指示器 -->
      <MessageStatus
        v-if="showStatus"
        :message="message"
        :session-type="sessionType"
        @retry="$emit('retry', message)"
        @show-read-info="handleShowReadInfo"
      />

      <!-- 表情回应 -->
      <MessageReactions
        v-if="hasReactions"
        :message="message"
        @toggle="toggleReaction"
      />
    </div>
  </div>

  <!-- 右键菜单 -->
  <ContextMenu
    :show="contextMenuVisible"
    :x="contextMenuX"
    :y="contextMenuY"
    :items="contextMenuItems"
    @select="handleContextMenuSelect"
    @update:show="contextMenuVisible = $event"
  />

  <!-- AI表情表态浮窗 -->
  <AiEmojiReaction
    :visible="showAiEmojiPanel"
    :message="message"
    :position="aiEmojiPosition"
    @select="handleAiEmojiSelect"
    @close="showAiEmojiPanel = false"
  />

  <!-- 已读详情弹窗 -->
  <ReadInfoDialog
    v-model:visible="showReadInfoDialog"
    :message-id="message.id"
    :read-count="message.readCount"
    :read-by="message.readBy"
    :read-time="message.readTime"
    :is-group-chat="sessionType === 'GROUP'"
    :total-members="message.totalMembers"
    :all-members="message.allMembers || []"
    :conversation-id="message.conversationId || message.sessionId"
    @remind-unread="(data) => $emit('remind-unread', data)"
  />
</template>

<script setup>
import { computed, ref } from 'vue'
import { CopyDocument, ChatLineSquare, Share, RefreshLeft, Delete, Edit, InfoFilled, Checked, Top } from '@element-plus/icons-vue'
import { extractUrls } from '@/utils/linkParser'

// Composables
import { useMessageBubble } from './message-bubble/composables/useMessageBubble.js'
import { useMessageStatus } from './message-bubble/composables/useMessageStatus.js'
import { useMessageReaction } from './message-bubble/composables/useMessageReaction.js'

// 子组件
import TextBubble from './message-bubble/bubbles/TextBubble.vue'
import LinkCard from './LinkCard.vue'
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
import ReadInfoDialog from './ReadInfoDialog.vue'
import ContextMenu from '@/components/Common/ContextMenu.vue'

const props = defineProps({
  message: { type: Object, required: true },
  sessionType: { type: String, default: 'PRIVATE' }
})

const emit = defineEmits([
  'command', 'preview', 'download', 'at', 'scroll-to',
  'retry', 'toggle-reaction', 'add-reaction', 're-edit',
  'show-user', 'long-press', 'show-read-info', 'remind-unread'
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
const showReadInfoDialog = ref(false)

const handleAiEmojiSelect = emoji => {
  addReaction(emoji)
}

// ==================== 右键菜单 ====================

const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)

// 右键菜单项配置
const contextMenuItems = computed(() => {
  const msg = props.message
  const items = []

  // 复制（仅文本消息）
  if (msg.type === 'TEXT') {
    items.push({
      label: '复制',
      icon: 'content_copy',
      value: 'copy'
    })
  }

  // 回复
  items.push({
    label: '回复',
    icon: 'chat_bubble',
    value: 'reply'
  })

  // 表情表态
  items.push({
    label: '表情表态',
    icon: 'sentiment_satisfied_alt',
    value: 'emoji'
  })

  // @提及（仅群聊且非自己消息）
  if (!msg.isOwn && props.sessionType === 'GROUP') {
    items.push({
      label: '@ 提及',
      icon: 'alternate_email',
      value: 'at'
    })
  }

  // 分隔线
  items.push({ divider: true })

  // 转发
  items.push({
    label: '转发',
    icon: 'forward',
    value: 'forward'
  })

  // 设为待办
  items.push({
    label: '设为待办',
    icon: 'check_circle',
    value: 'todo'
  })

  // 置顶/取消置顶
  items.push({
    label: msg.isPinned ? '取消置顶' : '置顶',
    icon: 'push_pin',
    value: 'pin'
  })

  // 分隔线
  items.push({ divider: true })

  // 撤回（仅自己消息且可撤回）
  if (msg.isOwn && canRecallBubble.value) {
    items.push({
      label: `撤回${recallTimeDisplay.value ? ` (${recallTimeDisplay.value})` : ''}`,
      icon: 'refresh',
      value: 'recall',
      danger: true
    })
  }

  // 删除（仅自己消息）
  if (msg.isOwn) {
    items.push({
      label: '删除',
      icon: 'delete',
      value: 'delete',
      danger: true
    })
  }

  // 编辑（仅自己消息且为文本消息）
  if (msg.isOwn && msg.type === 'TEXT') {
    items.push({
      label: '编辑',
      icon: 'edit',
      value: 'edit'
    })
  }

  return items
})

// 处理右键菜单触发
const handleContextMenu = e => {
  contextMenuX.value = e.clientX
  contextMenuY.value = e.clientY
  contextMenuVisible.value = true
}

// 处理右键菜单选择
const handleContextMenuSelect = item => {
  handleCommand(item.value, props.message)
  contextMenuVisible.value = false
}

/**
 * 显示已读详情
 */
const handleShowReadInfo = readInfo => {
  // 如果没有 readBy 数据，尝试获取（这里简化处理，实际应该从 store 获取）
  if (!readInfo.readBy || readInfo.readBy.length === 0) {
    // 显示空状态或跳过
    return
  }

  showReadInfoDialog.value = true
}

/**
 * 处理合并转发消息点击
 */
const handleCombineClick = () => {
  const combineData = {
    id: props.message.id,
    type: props.message.type,
    senderId: props.message.senderId,
    senderName: props.message.senderName,
    content: props.message.content,
    createTime: props.message.createTime,
    timestamp: props.message.timestamp,
    messages: parsedContent.value.messages
  }
  emit('command', 'view-combine', combineData)
}

// 处理长按事件，显示AI表情面板
const originalHandleTouchStart = handleTouchStart
const enhancedHandleTouchStart = e => {
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

// 提取消息中的链接
const messageLinks = computed(() => {
  if (props.message.type !== 'TEXT' || !props.message.content) {
    return []
  }

  // 先尝试从扩展数据中获取链接预览
  if (props.message.linkPreview) {
    return [props.message.linkPreview]
  }

  // 否则从内容中提取URL
  return extractUrls(props.message.content, 3)
})

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

// 包装器
.message-bubble-wrapper {
  position: relative;
  display: inline-flex;
}

.message-bubble {
  position: relative;
  display: inline-flex;
  align-items: flex-start;
  max-width: min(520px, calc(100vw - 400px));
  animation: messagePop 0.3s var(--dt-ease-bounce);
  margin-top: 0;
  padding-top: 0;
}

.bubble-content {
  padding: 10px 12px;  // 钉钉标准：保持紧凑，不修改
  border-radius: var(--dt-radius-md);
  font-size: var(--dt-font-size-base);
  line-height: 1.4;  // 钉钉标准：保持 1.4 行高
  word-break: break-word;
  overflow-wrap: break-word;
  transition: background-color var(--dt-transition-base);
  display: flex;
  align-items: flex-start;
}

// 对方消息样式
.message-bubble:not(.is-own) {
  .bubble-content {
    background: var(--dt-bubble-left-bg);
    border: 1px solid var(--dt-bubble-left-border);
    // 钉钉标准：左尖右圆（左上4px 左下4px 右上12px 右下12px）
    border-radius: var(--dt-radius-sm) var(--dt-radius-lg) var(--dt-radius-lg) var(--dt-radius-sm);
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
    color: var(--dt-bubble-right-text);  // 白色文字
    border: none;
    // 钉钉标准：左圆右尖（左上12px 左下12px 右上4px 右下4px）
    border-radius: var(--dt-radius-lg) var(--dt-radius-sm) var(--dt-radius-sm) var(--dt-radius-lg);
  }

  &:hover .bubble-content {
    background: var(--dt-bubble-right-bg-hover);
  }

  // 确保所有子元素文字颜色都是白色
  :deep(.message-text),
  :deep(.message-content),
  :deep(.link-content) {
    color: #FFFFFF !important;
  }

  :deep(a) {
    color: #FFFFFF !important;
    text-decoration: underline;
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

// 暗色模式适配（钉钉风格）
:global(.dark) {
  // 对方消息：深灰背景
  .message-bubble:not(.is-own) .bubble-content {
    background: #2a2a2a;
    border-color: #3a3a3a;
    color: #e8e8e8;
  }

  // 自己消息：钉钉蓝（暗模式下稍亮）
  .message-bubble.is-own .bubble-content {
    background: #0089FF;
    color: #FFFFFF;
  }

  // 悬停状态
  .message-bubble:not(.is-own):hover .bubble-content {
    background: #333333;
    border-color: #444444;
  }

  .message-bubble.is-own:hover .bubble-content {
    background: #1A9FFF;
  }
}

// 钉钉风格：消息气泡之间的间距优化
.message-bubble + .message-bubble {
  margin-top: 8px;
}

// 第一条消息不需要上边距
.message-bubble:first-child {
  margin-top: 0;
}

// 钉钉风格：图片/视频消息的特殊圆角
.message-bubble.type-image .bubble-content,
.message-bubble.type-video .bubble-content {
  // 钉钉图片消息：圆角8px
  border-radius: var(--dt-radius-md);
  overflow: hidden;
}

// 钉钉风格：文件消息样式优化
.message-bubble.type-file .bubble-content {
  padding: 12px 16px;
}
</style>
