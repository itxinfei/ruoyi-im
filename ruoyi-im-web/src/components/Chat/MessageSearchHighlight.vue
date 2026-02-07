/**
* 消息搜索高亮组件
* 在消息列表中高亮显示搜索关键词
*/
<template>
  <div class="message-search-highlight">
    <span
      v-for="(part, index) in highlightedParts"
      :key="index"
      :class="{ 'highlight': part.isHighlight }"
    >
      {{ part.text }}
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 原始文本内容
  text: { type: String, required: true },
  // 搜索关键词
  keyword: { type: String, default: '' },
  // 是否区分大小写
  caseSensitive: { type: Boolean, default: false }
})

/**
 * 高亮分割后的文本片段
 * 每个片段包含 text 和 isHighlight 属性
 */
const highlightedParts = computed(() => {
  if (!props.keyword || !props.text) {
    return [{ text: props.text, isHighlight: false }]
  }

  const parts = []
  const flags = props.caseSensitive ? 'g' : 'gi'
  const regex = new RegExp(escapeRegExp(props.keyword), flags)

  let lastIndex = 0
  let match

  // 重置 regex 的 lastIndex
  regex.lastIndex = 0

  while ((match = regex.exec(props.text)) !== null) {
    // 添加匹配前的普通文本
    if (match.index > lastIndex) {
      parts.push({
        text: props.text.slice(lastIndex, match.index),
        isHighlight: false
      })
    }

    // 添加高亮的匹配文本
    parts.push({
      text: match[0],
      isHighlight: true
    })

    lastIndex = regex.lastIndex
  }

  // 添加剩余的普通文本
  if (lastIndex < props.text.length) {
    parts.push({
      text: props.text.slice(lastIndex),
      isHighlight: false
    })
  }

  // 如果没有匹配，返回原文
  if (parts.length === 0) {
    return [{ text: props.text, isHighlight: false }]
  }

  return parts
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

.message-search-highlight {
  display: inline;

  .highlight {
    background: var(--dt-search-highlight-bg);
    color: var(--dt-search-highlight-text);
    border-radius: 2px;
    padding: 0 2px;
    font-weight: 500;
  }
}

// 暗色模式
:global(.dark) {
  .message-search-highlight .highlight {
    background: var(--dt-search-highlight-bg-dark);
    color: var(--dt-search-highlight-text-dark);
  }
}
</style>
