<template>
  <div class="link-card" :class="{ 'is-own': message.isOwn }" @click="openLink">
    <!-- 缩略图 -->
    <div v-if="linkInfo.imageUrl" class="link-image">
      <img :src="linkInfo.imageUrl" alt="" @error="handleImageError" />
    </div>
    
    <!-- 链接信息 -->
    <div class="link-content">
      <div class="link-title">{{ linkInfo.title || '链接预览' }}</div>
      <div v-if="linkInfo.description" class="link-desc">{{ linkInfo.description }}</div>
      <div class="link-url">
        <span class="material-icons-outlined">link</span>
        <span>{{ displayUrl }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['command'])

// 图片加载错误标志
const imageError = ref(false)

// 解析链接内容
const parsedContent = computed(() => {
  try {
    if (!props.message?.content) return {}
    return (typeof props.message.content === 'string')
      ? JSON.parse(props.message.content)
      : (props.message.content || {})
  } catch (e) {
    return {}
  }
})

// 链接信息
const linkInfo = computed(() => ({
  url: parsedContent.value.url || props.message.content || '',
  title: parsedContent.value.title || '',
  description: parsedContent.value.description || '',
  imageUrl: parsedContent.value.imageUrl || parsedContent.value.thumbnail || ''
}))

// 显示的URL（缩短）
const displayUrl = computed(() => {
  try {
    const url = linkInfo.value.url
    const urlObj = new URL(url)
    return urlObj.host + (urlObj.pathname.length > 20 ? urlObj.pathname.substring(0, 20) + '...' : urlObj.pathname)
  } catch {
    return linkInfo.value.url.substring(0, 30)
  }
})

// 图片加载错误处理
const handleImageError = () => {
  imageError.value = true
}

// 打开链接
const openLink = () => {
  if (linkInfo.value.url) {
    window.open(linkInfo.value.url, '_blank')
  }
}
</script>

<style scoped lang="scss">
.link-card {
  display: flex;
  flex-direction: column;
  max-width: 280px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--dt-transition-base);
  border: 1px solid var(--dt-border-light);

  &:hover {
    background: var(--dt-bg-card);
    box-shadow: var(--dt-shadow-2);
  }

  &.is-own {
    background: rgba(255, 255, 255, 0.9);
    border-color: var(--dt-brand-light);

    .link-title {
      color: var(--dt-brand-color);
    }

    .link-url {
      color: var(--dt-brand-color);
    }
  }

  .link-image {
    width: 100%;
    height: 120px;
    overflow: hidden;
    background: var(--dt-border-lighter);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .link-content {
    padding: 10px 12px;
  }

  .link-title {
    font-size: var(--dt-font-size-base);
    font-weight: var(--dt-font-weight-medium);
    color: var(--dt-text-primary);
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .link-desc {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-secondary);
    line-height: 1.4;
    margin-bottom: 6px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .link-url {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: var(--dt-font-size-xs);
    color: var(--dt-text-tertiary);

    .material-icons-outlined {
      font-size: 14px;
    }

    span:last-child {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

// 暗色模式适配
:global(.dark) {
  .link-card {
    background: var(--dt-bg-hover-dark);
    border-color: var(--dt-border-dark);

    &:hover {
      background: var(--dt-bg-active-dark);
    }

    .link-title {
      color: var(--dt-text-primary-dark);
    }

    .link-desc {
      color: var(--dt-text-secondary-dark);
    }

    .link-url {
      color: var(--dt-text-tertiary-dark);
    }
  }
}
</style>
