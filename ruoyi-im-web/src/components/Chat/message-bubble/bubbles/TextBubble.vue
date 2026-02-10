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
    <div
      class="text-content"
      v-html="formattedContent"
    />

    <!-- 链接预览卡片 -->
    <div
      v-if="extractedUrls.length > 0"
      class="link-previews"
    >
      <LinkPreviewCard
        v-for="url in extractedUrls"
        :key="url"
        :preview="getPreviewData(url)"
        :is-loading="isLoadingUrl(url)"
        @click="handleLinkClick"
        @image-error="handleLinkImageError"
      />
    </div>

    <!-- 已编辑标记 -->
    <span
      v-if="message.isEdited"
      class="edited-tag"
    >(已编辑)</span>

    <!-- 标记图标 -->
    <div
      v-if="hasMarkers"
      class="message-markers"
    >
      <span
        v-for="marker in message.markers"
        :key="marker.id || marker.markerType"
        class="marker-icon"
        :class="{ completed: marker.isCompleted }"
        :style="{ color: marker.color || '' }"
      >
        <span
          v-if="marker.markerType === 'FLAG'"
          class="material-icons-outlined"
        >flag</span>
        <span
          v-else-if="marker.markerType === 'IMPORTANT'"
          class="material-icons-outlined"
        >star</span>
        <span
          v-else-if="marker.markerType === 'TODO'"
          class="material-icons-outlined"
        >
          {{ marker.isCompleted ? 'check_circle' : 'check_circle_outline' }}
        </span>
      </span>
    </div>

    <!-- 置顶图标 -->
    <div
      v-if="message.isPinned"
      class="message-pinned-badge"
      title="已置顶"
    >
      <el-icon>
        <Top />
      </el-icon>
      <span>已置顶</span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import { Top } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import DOMPurify from 'dompurify'
import MessageReplyRef from '../parts/MessageReplyRef.vue'
import LinkPreviewCard from '../parts/LinkPreviewCard.vue'
import { useLinkPreview } from '@/composables/useLinkPreview.js'

const props = defineProps({
  message: { type: Object, required: true },
  hasMarkers: { type: Boolean, default: false }
})

const emit = defineEmits(['scroll-to'])

const store = useStore()
const currentUserId = store.getters['user/currentUserId'] || store.state.user.userInfo?.userId

// 链接预览功能
const { extractUrls, fetchPreview, getDefaultPreview } = useLinkPreview()
const linkPreviews = ref(new Map())
const loadingUrls = ref(new Set())

/**
 * 从消息内容中提取的 URL 列表
 */
const extractedUrls = computed(() => {
  return extractUrls(props.message?.content)
})

/**
 * 获取 URL 的预览数据
 */
const getPreviewData = url => {
  return linkPreviews.value.get(url) || getDefaultPreview(url)
}

/**
 * 检查 URL 是否正在加载
 */
const isLoadingUrl = url => {
  return loadingUrls.value.has(url)
}

/**
 * 加载链接预览数据
 */
const loadLinkPreviews = async () => {
  const urls = extractedUrls.value
  if (urls.length === 0) { return }

  for (const url of urls) {
    if (linkPreviews.value.has(url) || loadingUrls.value.has(url)) { continue }

    loadingUrls.value.add(url)
    try {
      const preview = await fetchPreview(url)
      if (preview) {
        linkPreviews.value.set(url, preview)
      }
    } finally {
      loadingUrls.value.delete(url)
    }
  }
}

/**
 * 处理链接点击
 * @param {Object} preview - 链接预览数据
 */
const handleLinkClick = preview => {
  // 打开链接
  if (preview?.url) {
    window.open(preview.url, '_blank')
  }
}

/**
 * 处理链接图片加载失败
 */
const handleLinkImageError = url => {
  const preview = linkPreviews.value.get(url)
  if (preview) {
    preview.image = ''
    linkPreviews.value.set(url, { ...preview })
  }
}

// 组件挂载时加载链接预览
onMounted(() => {
  loadLinkPreviews()
})

// 监听消息内容变化，重新加载链接预览
watch(() => props.message?.content, () => {
  loadLinkPreviews()
}, { immediate: true })

/**
 * 查找被引用的消息
 * 从消息列表中查找 replyToMessageId 对应的原始消息
 */
const replyMessage = computed(() => {
  if (!props.message.replyToMessageId) { return null }

  // 从当前会话的消息列表中查找
  const messages = store.getters['im/message/currentMessages'] || []
  return messages.find(m => m.id === props.message.replyToMessageId) || null
})

/**
 * 处理引用消息点击事件
 * 触发滚动到原消息，并带高亮标记
 */
const handleReplyClick = () => {
  if (!replyMessage.value) { return }
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
      if (!nickname) { continue }

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
@use '@/styles/im-design-system.scss' as *;

.text-bubble {
  position: relative;
}

.text-content {
  padding: 12px;
  word-break: break-word;
  overflow-wrap: break-word;
  white-space: pre-wrap;
  line-height: 1.6; // 钉钉标准:1.6
  font-size: var(--dt-font-size-content, 15px); // 钉钉标准:15px
  max-width: 100%;
  min-width: 0;
  color: var(--dt-text-primary);

  // 链接样式 - 钉钉/野火IM风格
  :deep(a) {
    color: var(--dt-brand-color);
    text-decoration: none;
    word-break: break-all;
    max-width: 100%;
    overflow-wrap: break-word;
    transition: color var(--dt-transition-fast);

    &:hover {
      color: var(--dt-brand-hover);
      text-decoration: underline;
    }
  }

  // 防止代码块溢出
  :deep(code) {
    max-width: 100%;
    overflow-x: auto;
    white-space: pre-wrap;
    word-break: break-all;
    background: var(--dt-bg-code);
    padding: 8px 12px;
    border-radius: var(--dt-radius-sm);
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    font-size: 13px;
  }

  // @提及高亮样式 - 钉钉/野火IM风格
  :deep(.mention-highlight) {
    word-break: break-word;
    color: var(--dt-brand-color);
    font-weight: 500;
    background: var(--dt-brand-bg);
    padding: 2px 6px;
    border-radius: var(--dt-radius-xs);
    cursor: pointer;
    margin: 0 1px;
    transition: all var(--dt-transition-fast);

    &:hover {
      background: var(--dt-brand-bg-hover);
    }

    // 提及当前用户时 - 更明显的背景
    &.is-current-user {
      background: var(--dt-error-bg);
      color: var(--dt-error-color);
      font-weight: 600;

      &:hover {
        background: var(--dt-error-bg-hover);
      }
    }
  }

  // 己方消息内的链接和提及样式调整为白色
  :deep(.is-right) & {
    a {
      color: var(--dt-text-primary-inverted);

      &:hover {
        color: var(--dt-text-secondary-inverted);
      }
    }

    .mention-highlight {
      color: var(--dt-text-primary-inverted);
      background: var(--dt-mention-bg-own);

      &:hover {
        background: var(--dt-mention-bg-own-hover);
      }

      &.is-current-user {
        background: var(--dt-mention-current-user-bg-own);
        color: var(--dt-text-primary-inverted);
      }
    }
  }
}

.edited-tag {
  margin-left: 4px;
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-quaternary);
}

.link-previews {
  margin-top: var(--dt-spacing-xs);
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-xs);
}

.message-markers {
  display: flex;
  gap: var(--dt-spacing-xs);
  margin-top: var(--dt-spacing-xs);

  .marker-icon {
    font-size: 16px;
    opacity: 0.8;
    transition: opacity var(--dt-transition-fast);

    &.completed {
      opacity: 0.5;
    }
  }
}

.message-pinned-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--dt-spacing-xs);
  margin-top: var(--dt-spacing-xs);
  padding: 3px 8px;
  background: var(--dt-brand-bg);
  border-radius: var(--dt-radius-xs);
  font-size: var(--dt-font-size-sm);
  color: var(--dt-brand-color);
  font-weight: 500;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-brand-bg-hover);
  }

  .el-icon {
    font-size: 14px;
  }
}

// 暗色模式适配 - 钉钉/野火IM风格
:global(.dark) {
  .text-content {
    color: var(--dt-text-primary-dark);

    a {
      color: var(--dt-brand-color-light);

      &:hover {
        color: var(--dt-brand-hover-light);
      }
    }

    :deep(code) {
      background: var(--dt-bg-code-dark);
    }

    .mention-highlight {
      color: var(--dt-brand-color-light);
      background: var(--dt-mention-bg-dark);

      &:hover {
        background: var(--dt-mention-bg-dark-hover);
      }

      &.is-current-user {
        background: var(--dt-mention-current-user-bg-dark);
        color: var(--dt-error-color-light);
      }
    }
  }

  .message-pinned-badge {
    background: var(--dt-brand-bg-dark);
    color: var(--dt-brand-color-light);
  }
}
</style>
