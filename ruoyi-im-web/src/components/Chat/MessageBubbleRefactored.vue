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
      @mousedown="handleMouseHold"
      @mouseup="handleMouseRelease"
      @mouseleave="handleMouseRelease"
      @contextmenu.prevent="handleContextMenu"
      @touchstart="handleTouchStart"
      @touchend="handleTouchEnd"
      @touchmove="handleTouchMove"
    >
      <!-- 消息内容区域 -->
      <div class="bubble-content">
        <!-- 文本消息 -->
        <TextBubble
          v-if="['text', 'raw'].includes(message.type?.toLowerCase())"
          :message="message"
          :has-markers="hasMarkers"
          @scroll-to="$emit('scroll-to', $event)"
        />

        <!-- 链接预览卡片 -->
        <LinkCard
          v-if="['text', 'raw'].includes(message.type?.toLowerCase()) && messageLinks.length > 0"
          :link="messageLinks[0]"
          class="message-link-card"
        />

        <!-- 图片消息 -->
        <ImageBubble
          v-else-if="message.type?.toUpperCase() === 'IMAGE'"
          :message="message"
          :is-large-group="isLargeGroup"
          @preview="$emit('preview', $event)"
        />

        <!-- 文件消息 -->
        <FileBubble
          v-else-if="message.type?.toUpperCase() === 'FILE'"
          :message="message"
          @download="$emit('download', $event)"
        />

        <!-- 视频消息 -->
        <VideoBubble
          v-else-if="message.type?.toUpperCase() === 'VIDEO'"
          :message="message"
        />

        <!-- 语音消息 -->
        <VoiceBubble
          v-else-if="['voice', 'audio'].includes(message.type?.toLowerCase())"
          :message="message"
          :is-read="message.isRead || message.readStatus === 'READ'"
          @read="$emit('mark-read', message)"
        />

        <!-- 位置消息 -->
        <LocationBubble
          v-else-if="message.type?.toUpperCase() === 'LOCATION'"
          :message="message"
        />

        <!-- 系统消息 -->
        <SystemBubble
          v-else-if="message.type?.toUpperCase() === 'SYSTEM'"
          :message="message"
        />

        <!-- 撤回消息 -->
        <RecalledBubble
          v-else-if="message.type?.toUpperCase() === 'RECALLED'"
          :message="message"
          @re-edit="$emit('re-edit', $event)"
        />

        <!-- 拍一拍消息 -->
        <NudgeMessageBubble
          v-else-if="message.type?.toUpperCase() === 'NUDGE'"
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
          v-else-if="['combine', 'combine_forward'].includes(message.type?.toLowerCase())"
          :messages="parsedContent.messages || []"
          @click="handleCombineClick"
        />

        <!-- 未知消息类型 - 根据用户要求，不显示气泡 -->
        <template v-else />
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
import { Top } from '@element-plus/icons-vue'
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
  sessionType: { type: String, default: 'PRIVATE' },
  isLargeGroup: { type: Boolean, default: false }
})

const emit = defineEmits([
  'command', 'preview', 'download', 'at', 'scroll-to',
  'retry', 'toggle-reaction', 'add-reaction', 're-edit',
  'show-user', 'long-press', 'show-read-info', 'remind-unread',
  'mark-read'
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

// ==================== 触摸支持 ====================
let touchTimer = null
const LONG_PRESS_DURATION = 500 // 长按触发时间

/**
 * 触摸开始 - 检测长按
 */
const handleTouchStart = e => {
  touchTimer = setTimeout(() => {
    // 触发右键菜单
    const touch = e.touches[0]
    contextMenuX.value = touch.clientX
    contextMenuY.value = touch.clientY
    contextMenuVisible.value = true
    // 触觉反馈（如果设备支持）
    if (navigator.vibrate) {
      navigator.vibrate(50)
    }
  }, LONG_PRESS_DURATION)
}

/**
 * 触摸移动 - 取消长按
 */
const handleTouchMove = () => {
  if (touchTimer) {
    clearTimeout(touchTimer)
    touchTimer = null
  }
}

/**
 * 触摸结束 - 清理定时器
 */
const handleTouchEnd = () => {
  if (touchTimer) {
    clearTimeout(touchTimer)
    touchTimer = null
  }
}

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
  if (msg.type?.toUpperCase() === 'TEXT') {
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
  if (msg.isOwn && msg.type?.toUpperCase() === 'TEXT') {
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

// 移除移动端触摸特有的 AI 表情面板处理，PC 端主要通过右键菜单或长按鼠标触发（如有需要）
// 目前保持 isLongPressing 逻辑由 handleMouseHold 处理

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

const bubbleClasses = computed(() => {
  const type = (props.message.type || 'text').toLowerCase()
  return {
    'is-own': props.message.isOwn,
    'is-selected': isSelected.value,
    'is-long-press': isLongPressing.value,
    [`type-${type}`]: true
  }
})

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
  max-width: min(520px, 100%);
  // GPU 加速动画性能优化
  contain: layout style paint;
  will-change: transform, opacity;
  animation: messagePop 0.3s var(--dt-ease-bounce);
  // 动画结束后移除性能提示
  animation-fill-mode: both;
  margin-top: 0;
  padding-top: 0;
}

.bubble-content {
  padding: 10px 12px; // 野火IM标准:上下10px,左右12px
  border-radius: 8px; // 野火IM标准:统一8px圆角
  font-size: var(--dt-font-size-base);
  line-height: 1.5;
  word-break: break-all;
  overflow-wrap: anywhere;
  transition: all var(--dt-transition-base);
  display: flex;
  align-items: flex-start;
}

// 对方消息样式 - 野火IM风格
.message-bubble:not(.is-own) {
  .bubble-content {
    background: #ffffff; // 野火IM:白色背景
    border: 1px solid #e0e0e0; // 野火IM:灰色边框
    border-radius: 8px; // 野火IM:统一圆角,不要非对称
    color: #333333; // 野火IM:深灰文字
    box-shadow: none; // 野火IM:无阴影或极浅阴影
    transition: background 0.2s;

    &:hover {
      background: #f5f5f5; // hover时稍微变灰
    }
  }

  &:hover .bubble-content {
    background: #f5f5f5;
  }
}

// 自己消息样式 - 野火IM风格
.message-bubble.is-own {
  flex-direction: row-reverse;

  .bubble-content {
    background: #4168e0; // 野火IM:蓝色背景
    color: #ffffff; // 白色文字
    border: none; // 无边框
    border-radius: 8px; // 野火IM:统一圆角,不要非对称
    box-shadow: none; // 野火IM:无阴影
    transition: background 0.2s;

    &:hover {
      background: #3558d0; // hover时稍微变深
    }
  }

  &:hover .bubble-content {
    background: #3558d0;
  }

  // 确保所有子元素文字颜色都是白色
  :deep(.message-text),
  :deep(.message-content),
  :deep(.link-content),
  :deep(.text-content) {
    color: #FFFFFF !important;
  }

  :deep(a) {
    color: #FFFFFF !important;
    font-weight: 500;
    text-decoration: underline;
    text-decoration-thickness: 1px;
    text-underline-offset: 2px;
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

// 暗色模式适配(野火IM风格)
:global(.dark) {

  // 对方消息:深灰背景
  .message-bubble:not(.is-own) .bubble-content {
    background: #2c2c2c;
    border-color: #444444;
    color: #e8e8e8;
    box-shadow: none;
  }

  // 自己消息:蓝色(暗模式下稍亮)
  .message-bubble.is-own .bubble-content {
    background: #4168e0;
    color: #FFFFFF;
  }

  // 悬停状态
  .message-bubble:not(.is-own):hover .bubble-content {
    background: #333333;
    border-color: #555555;
  }

  .message-bubble.is-own:hover .bubble-content {
    background: #3558d0;
  }
}

// 钉钉风格：消息气泡之间的间距优化
.message-bubble+.message-bubble {
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
