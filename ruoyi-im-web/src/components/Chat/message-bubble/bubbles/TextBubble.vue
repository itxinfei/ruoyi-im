/**
 * 文本消息气泡组件
 * 支持高亮显示@提及
 */
<template>
  <div class="text-bubble">
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

const props = defineProps({
  message: { type: Object, required: true },
  hasMarkers: { type: Boolean, default: false }
})

const store = useStore()
const currentUserId = store.getters['user/currentUserId'] || store.state.user.userInfo?.userId

/**
 * 格式化消息内容，高亮@提及
 * 将 @用户名 转换为带样式的 span
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

    return formatted
  }

  // 如果没有提及列表，使用正则表达式匹配 @xxx 格式
  return content.replace(
    /@([\u4e00-\u9fa5a-zA-Z0-9_-]+)/g,
    (match, name) => {
      // 简单判断是否提及当前用户
      const isCurrentUser = name === props.message.senderName
      return `<span class="mention-highlight ${isCurrentUser ? 'is-current-user' : ''}">${match}</span>`
    }
  )
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
    border-radius: 4px;
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
  border-radius: 4px;
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
