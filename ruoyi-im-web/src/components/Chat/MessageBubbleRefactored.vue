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
@use '@/styles/im-design-system.scss' as *;

// 包装器
.message-bubble-wrapper {
  position: relative;
  display: inline-flex;
  max-width: 100%; // 限制父容器宽度
}

.message-bubble {
  position: relative;
  display: inline-flex;
  align-items: center; // 垂直居中，与头像对齐
  max-width: min(520px, 70%); // 响应式最大宽度（野火IM标准）
  width: fit-content; // 根据内容自适应宽度
  min-width: 0; // 允许 flex 子项收缩
  // GPU 加速动画性能优化
  contain: layout style paint;
  animation: bubblePop 0.3s var(--dt-ease-bounce);
  // 动画结束后移除性能提示
  animation-fill-mode: both;
  margin-top: 0;
  padding-top: 0;
}

.bubble-content {
  padding: 10px 14px; // 钉钉/野火IM标准: 紧凑舒适的内边距
  border-radius: 12px; // 钉钉风格圆角
  font-size: 14px; // 标准字体大小
  line-height: 1.5; // 舒适的行高
  word-break: break-word;
  overflow-wrap: break-word;
  max-width: 100%;
  min-width: 0;
  transition: all 0.2s ease;
  display: flex;
  align-items: flex-start;

  // 防止长链接溢出
  :deep(a) {
    word-break: break-all;
    max-width: 100%;
    overflow-wrap: break-word;
  }

  // 防止代码块溢出
  :deep(pre) {
    max-width: 100%;
    overflow-x: auto;
    white-space: pre-wrap;
    word-break: break-all;
  }

  // 防止图片溢出
  :deep(img) {
    max-width: 100%;
    height: auto;
    display: block;
  }

  // 防止表格溢出
  :deep(table) {
    max-width: 100%;
    overflow-x: auto;
    table-layout: fixed;
  }
}

// 对方消息样式 - 钉钉/野火IM风格
.message-bubble:not(.is-own) {
  .bubble-content {
    // 钉钉/野火IM: 白色背景，带微阴影
    background: #ffffff;
    // 微妙边框
    border: 1px solid #e8e8e8;
    // 非对称圆角：靠近头像一侧圆角较小（左侧4px）
    border-radius: 12px 12px 12px 4px;
    // 文字颜色
    color: #1f2329;
    // 钉钉风格阴影：微妙的立体感
    box-shadow: var(--dt-shadow-1), var(--dt-shadow-2);
    // 微妙的悬停效果
    &:hover {
      box-shadow: var(--dt-shadow-3);
      transform: translateY(-1px);
    }
  }
}

// 己方消息样式 - 钉钉/野火IM风格
.message-bubble.is-own {
  flex-direction: row-reverse;

  .bubble-content {
    // 钉钉: 蓝色渐变背景
    background: linear-gradient(135deg, #0089ff 0%, #0066cc 100%);
    // 白色文字
    color: #ffffff;
    // 无边框
    border: none;
    // 非对称圆角：靠近头像一侧圆角较小（右侧4px）
    border-radius: 12px 12px 4px 12px;
    // 钉钉风格蓝色阴影
    box-shadow: var(--dt-shadow-brand-light);
    // 微妙的悬停效果
    &:hover {
      background: var(--dt-brand-gradient);
      filter: brightness(1.05);
      box-shadow: var(--dt-shadow-brand-strong);
    }
  }

  // 确保所有子元素文字颜色都是白色
  :deep(.message-text),
  :deep(.message-content),
  :deep(.link-content),
  :deep(.text-content) {
    color: #ffffff !important;
  }

  :deep(a) {
    color: var(--dt-bubble-right-text) !important;
    font-weight: 500;
    text-decoration: underline;
    text-decoration-thickness: 1px;
    text-underline-offset: 2px;
  }

  // 链接预览卡片样式
  :deep(.link-preview-card) {
    background: var(--dt-white-15);
    border-color: var(--dt-white-20);

    .link-title,
    .link-desc {
      color: var(--dt-bubble-right-text) !important;
    }
  }
}

// 选中状态
.message-bubble.is-selected .bubble-content {
  border: 2px solid var(--dt-brand-color) !important;
  box-shadow: 0 0 0 3px var(--dt-brand-light) !important;
}

// 长按状态
.message-bubble.is-long-press {
  animation: longPressPulse 0.3s ease-in-out;
}

// 图片消息：透明背景，无边框
.message-bubble.type-image .bubble-content {
  padding: 4px;
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

// 视频消息
.message-bubble.type-video .bubble-content {
  padding: 0;
  background: #000 !important;
  border-radius: var(--dt-radius-md);
}

// 系统消息
.message-bubble.type-system .bubble-content,
.message-bubble.type-recalled .bubble-content {
  background: transparent;
  padding: 0;
  border: none;
  box-shadow: none;
}

// 文件消息 - 野火IM/钉钉风格
.message-bubble.type-file .bubble-content {
  padding: 12px 16px;
}

// 暗色模式适配 - 钉钉/野火IM风格
:global(.dark) {
  // 对方消息: 深灰背景
  .message-bubble:not(.is-own) .bubble-content {
    background: var(--dt-bg-tertiary-dark);
    border-color: var(--dt-bg-dark-border);
    color: var(--dt-text-dark-mode);
    box-shadow: var(--dt-black-20);

    &:hover {
      box-shadow: var(--dt-black-20);
      transform: translateY(-1px);
    }
  }

  // 己方消息: 保持蓝色渐变
  .message-bubble.is-own .bubble-content {
    background: linear-gradient(135deg, #0284c7 0%, #0369a1 100%);
    color: #ffffff;
    box-shadow: var(--dt-shadow-brand);

    &:hover {
      filter: brightness(1.08);
      box-shadow: var(--dt-shadow-brand-strong);
    }
  }
}

// 气泡进入动画 - 钉钉风格
@keyframes bubblePop {
  0% {
    opacity: 0;
    transform: scale(0.9) translateY(8px);
  }

  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

// 长按脉冲动画
@keyframes longPressPulse {
  0%, 100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.02);
  }
}
</style>
