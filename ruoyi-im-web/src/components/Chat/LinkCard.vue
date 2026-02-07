<template>
  <!-- 加载状态 -->
  <div
    v-if="loading"
    class="link-card link-card--loading"
  >
    <div class="link-icon">
      <span class="material-icons-outlined loading-icon">language</span>
    </div>
    <div class="link-content">
      <el-skeleton
        :rows="2"
        animated
      />
    </div>
  </div>

  <!-- 错误状态 -->
  <div
    v-else-if="error"
    class="link-card link-card--error"
    @click="openLink"
  >
    <div class="link-icon link-icon--error">
      <span class="material-icons-outlined">link_off</span>
    </div>
    <div class="link-content link-content--full">
      <div class="link-url link-url--full">
        {{ formatUrl(link.url) }}
      </div>
      <div class="link-error">
        链接预览不可用，点击访问
      </div>
    </div>
  </div>

  <!-- 正常状态 -->
  <div
    v-else
    class="link-card"
    @click="openLink"
  >
    <!-- 左侧图片/图标区 -->
    <div class="link-image-wrapper">
      <img
        v-if="link.image"
        :src="link.image"
        :alt="link.title"
        class="link-image"
        loading="lazy"
        @error="handleImageError"
      >
      <div
        v-else
        class="link-icon"
      >
        <img
          v-if="faviconUrl"
          :src="faviconUrl"
          class="favicon"
          @error="faviconError = true"
        >
        <span
          v-else
          class="material-icons-outlined"
        >language</span>
      </div>
    </div>

    <!-- 右侧内容区 -->
    <div class="link-content">
      <div class="link-title">
        {{ link.title || link.url }}
      </div>
      <div
        v-if="link.description"
        class="link-description"
      >
        {{ link.description }}
      </div>
      <div class="link-meta">
        <span class="link-url">{{ formatUrl(link.url) }}</span>
        <span class="link-arrow material-icons-outlined">open_in_new</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  link: {
    type: Object,
    required: true,
    validator: value => {
      return value.url && typeof value.url === 'string'
    }
  },
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])
const faviconError = ref(false)

// 获取 favicon URL
const faviconUrl = computed(() => {
  if (faviconError.value) {return null}
  try {
    const urlObj = new URL(props.link.url)
    return `${urlObj.origin}/favicon.ico`
  } catch {
    return null
  }
})

const formatUrl = url => {
  try {
    const urlObj = new URL(url)
    return urlObj.hostname
  } catch {
    return url
  }
}

const openLink = () => {
  emit('click', props.link.url)
  window.open(props.link.url, '_blank')
}

const handleImageError = e => {
  // 图片加载失败时隐藏
  e.target.style.display = 'none'
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.link-card {
  display: flex;
  align-items: stretch;
  background: #ffffff;
  border: 1px solid #e0e0e0; // 野火IM边框
  border-radius: 8px; // 野火IM圆角
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  max-width: 360px;
  min-height: 72px;

  &:hover {
    border-color: #4168e0; // 野火IM蓝
    box-shadow: 0 2px 8px var(--dt-brand-extra-light);

    .link-arrow {
      opacity: 1;
      color: #4168e0;
    }
  }

  &:active {
    transform: scale(0.99);
  }
}

// 左侧图片/图标区
.link-image-wrapper {
  width: 72px;
  min-height: 72px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;

  .link-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.link-icon {
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;

  .material-icons-outlined {
    font-size: 28px;
    color: #999;
  }

  .favicon {
    width: 24px;
    height: 24px;
    border-radius: 4px;
  }

  &--error {
    background: #fff5f5;

    .material-icons-outlined {
      color: #ff6b6b;
    }
  }
}

// 右侧内容区
.link-content {
  flex: 1;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
  justify-content: center;
}

.link-title {
  font-size: 14px;
  font-weight: 500;
  color: #333333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.link-description {
  font-size: 12px;
  color: #666666;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.link-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: auto;
}

.link-url {
  font-size: 11px;
  color: #999999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.link-arrow {
  font-size: 14px;
  color: #ccc;
  opacity: 0;
  transition: all 0.2s;
}

// 加载状态
.link-card--loading {
  pointer-events: none;
  cursor: default;

  .loading-icon {
    animation: pulse 1.5s ease-in-out infinite;
  }
}

@keyframes pulse {

  0%,
  100% {
    opacity: 0.4;
  }

  50% {
    opacity: 1;
  }
}

// 错误状态
.link-card--error {
  cursor: pointer;

  .link-content--full {
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    justify-content: center;
  }

  .link-url--full {
    font-size: 13px;
    color: #4168e0; // 野火IM蓝
    font-weight: 500;
  }

  .link-error {
    font-size: 12px;
    color: #999;
  }

  &:hover {
    border-color: #4168e0;
  }
}

// 暗色模式
.dark .link-card {
  background: #1e1e1e;
  border-color: #374151;

  .link-title {
    color: #e8e8e8;
  }

  .link-description {
    color: #a0a8b8;
  }

  .link-url {
    color: #6b7280;
  }

  .link-image-wrapper,
  .link-icon {
    background: #2d2d2d;
  }

  &:hover {
    border-color: #4168e0;
    box-shadow: 0 2px 8px var(--dt-brand-bg-dark);
  }
}

.dark .link-card--error {
  .link-icon--error {
    background: var(--dt-error-02);
  }
}
</style>
