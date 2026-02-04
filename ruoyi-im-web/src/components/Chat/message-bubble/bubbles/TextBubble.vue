/**
 * 文本消息气泡组件
 * 支持高亮显示@提及
 * 支持显示和点击引用消息
 */
<template>
  <div class="text-bubble">
    <!-- 引用消息预览 -->
    <MessageReplyRef
      v-if="replyMessage"
      :message="replyMessage"
      @click="handleReplyClick"
    />

    <!-- 文本内容 -->
    <div class="text-content" v-html="formattedContent"></div>

    <!-- 已编辑标记 -->
    <span v-if="message.isEdited" class="edited-tag">(已编辑)</span>

    <!-- 标记图标 -->
    <div v-if="hasMarkers" class="message-markers">
      <span v-for="marker in message.markers" :key="marker.id || marker.markerType"
            class="marker-icon"
            :class="{ completed: marker.isCompleted }"
            :style="{ color: marker.color || '' }">
        <span v-if="marker.markerType === 'FLAG'" class="material-icons-outlined">flag</span>
        <span v-else-if="marker.markerType === 'IMPORTANT'" class="material-icons-outlined">star</span>
        <span v-else-if="marker.markerType === 'TODO'" class="material-icons-outlined">
          {{ marker.isCompleted ? 'check_circle' : 'check_circle_outline' }}
        </span>
      </span>
    </div>

    <!-- 置顶图标 -->
    <div v-if="message.isPinned" class="message-pinned-badge" title="已置顶">
      <el-icon><Top /></el-icon>
      <span>已置顶</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Top } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import DOMPurify from 'dompurify'
import MessageReplyRef from '../parts/MessageReplyRef.vue'

const props = defineProps({
  message: { type: Object, required: true },
  hasMarkers: { type: Boolean, default: false }
})

const emit = defineEmits(['scroll-to'])

const store = useStore()
const currentUserId = store.getters['user/currentUserId'] || store.state.user.userInfo?.userId

/**
 * 查找被引用的消息
 * 从消息列表中查找 replyToMessageId 对应的原始消息
 */
const replyMessage = computed(() => {
  if (!props.message.replyToMessageId) return null

  // 从当前会话的消息列表中查找
  const messages = store.getters['im/message/currentMessages'] || []
  return messages.find(m => m.id === props.message.replyToMessageId) || null
})

/**
 * 处理引用消息点击事件
 * 触发滚动到原消息，并带高亮标记
 */
const handleReplyClick = () => {
  if (!replyMessage.value) return
  emit('scroll-to', {
    messageId: props.message.replyToMessageId,
    highlight: true
  })
}

/**
 * 格式化消息内容，高亮@提及
 * 将 @用户名 转换为带样式的 span
 * 使用 DOMPurify 过滤防止 XSS 攻击
 */
const formattedContent = computed(() => {
  const content = props.message.content || ''

  // 如果消息中有提及列表，使用它来高亮
  if (props.message.mentions && props.message.mentions.length > 0) {
    let formatted = content

    // 按照长度降序排序，避免替换时的问题
    const sortedMentions = [...props.message.mentions].sort((a, b) =>
      (b.nickname?.length || 0) - (a.nickname?.length || 0)
    )

    // 替换每个@提及
    for (const mention of sortedMentions) {
      const nickname = mention.nickname || mention.userName || ''
      if (!nickname) continue

      const mentionText = `@${nickname}`
      const isCurrentUserMentioned = mention.mentionedUserId === currentUserId

      // 替换为带样式的 span
      formatted = formatted.replace(
        new RegExp(escapeRegExp(mentionText), 'g'),
        `<span class="mention-highlight ${isCurrentUserMentioned ? 'is-current-user' : ''}">${mentionText}</span>`
      )
    }

    // XSS 防护：过滤 HTML
    return DOMPurify.sanitize(formatted, {
      ALLOWED_TAGS: ['p', 'br', 'span', 'strong', 'em', 'u', 'a'],
      ALLOWED_ATTR: ['class', 'href', 'target', 'rel'],
      ALLOW_DATA_ATTR: false,
      KEEP_CONTENT: true
    })
  }

  // 如果没有提及列表，使用正则表达式匹配 @xxx 格式
  const formatted = content.replace(
    /@([\u4e00-\u9fa5a-zA-Z0-9_-]+)/g,
    (match, name) => {
      // 简单判断是否提及当前用户
      const isCurrentUser = name === props.message.senderName
      return `<span class="mention-highlight ${isCurrentUser ? 'is-current-user' : ''}">${match}</span>`
    }
  )

  // XSS 防护：过滤 HTML
  return DOMPurify.sanitize(formatted, {
    ALLOWED_TAGS: ['p', 'br', 'span', 'strong', 'em', 'u', 'a'],
    ALLOWED_ATTR: ['class', 'href', 'target', 'rel'],
    ALLOW_DATA_ATTR: false,
    KEEP_CONTENT: true
  })
})

/**
 * 转义正则表达式特殊字符
 */
function escapeRegExp(string) {
  return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.text-bubble {
  position: relative;
}

.text-content {
  word-break: break-word;
  white-space: pre-wrap;
  line-height: 1.4;

  // @提及高亮样式
  :deep(.mention-highlight) {
    color: var(--dt-brand-color);
    font-weight: 500;
    background: rgba(0, 137, 255, 0.1);
    padding: 2px 4px;
    border-radius: var(--dt-radius-sm);
    cursor: pointer;
    transition: all var(--dt-transition-fast);

    &:hover {
      background: rgba(0, 137, 255, 0.2);
    }

    // 提及当前用户时，更加醒目
    &.is-current-user {
      background: rgba(255, 77, 79, 0.15);
      color: var(--dt-error-color);
      font-weight: 600;

      &:hover {
        background: rgba(255, 77, 79, 0.25);
      }
    }
  }
}

.edited-tag {
  margin-left: 4px;
  font-size: 11px;
  color: var(--dt-text-quaternary);
}

.message-markers {
  display: flex;
  gap: 4px;
  margin-top: 4px;

  .marker-icon {
    font-size: 16px;
    opacity: 0.8;

    &.completed {
      opacity: 0.5;
    }
  }
}

.message-pinned-badge {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  margin-top: 4px;
  padding: 2px 6px;
  background: var(--dt-brand-bg);
  border-radius: var(--dt-radius-sm);
  font-size: 11px;
  color: var(--dt-brand-color);
}

// 暗色模式适配
:global(.dark) {
  .text-content {
    :deep(.mention-highlight) {
      background: rgba(0, 137, 255, 0.2);
      color: #4da6ff;

      &:hover {
        background: rgba(0, 137, 255, 0.3);
      }

      &.is-current-user {
        background: rgba(255, 77, 79, 0.25);
        color: #ff7875;

        &:hover {
          background: rgba(255, 77, 79, 0.35);
        }
      }
    }
  }
}
</style>
