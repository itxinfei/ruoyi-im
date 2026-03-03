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
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e5e5e5;

  &:hover {
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  &.is-own {
    background: rgba(255, 255, 255, 0.9);
    border-color: rgba(22, 119, 255, 0.3);

    .link-title {
      color: #1677ff;
    }

    .link-url {
      color: #1677ff;
    }
  }

  .link-image {
    width: 100%;
    height: 120px;
    overflow: hidden;
    background: #e5e5e5;

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
    font-size: 14px;
    font-weight: 500;
    color: #262626;
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .link-desc {
    font-size: 12px;
    color: #666;
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
    font-size: 11px;
    color: #999;

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
</style>
