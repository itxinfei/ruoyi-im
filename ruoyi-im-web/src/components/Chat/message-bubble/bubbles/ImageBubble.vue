<template>
  <div
    class="image-bubble"
    :class="{
      'is-uploading': isUploading,
      'is-loading': isLoading,
      'has-error': hasError
    }"
    @click="handleClick"
  >
    <!-- 加载占位符（带模糊缩略图） -->
    <div
      v-if="isLoading && !imageLoaded"
      class="image-placeholder"
      :class="{ 'with-thumb': thumbUrl }"
    >
      <!-- 模糊缩略图背景 -->
      <div
        v-if="thumbUrl"
        class="blur-thumb"
        :style="{ backgroundImage: `url(${thumbUrl})` }"
      />

      <el-icon
        class="loading-icon"
        :size="32"
      >
        <Loading />
      </el-icon>
      <span class="placeholder-text">加载中...</span>
    </div>

    <!-- 错误占位符 -->
    <div
      v-else-if="hasError"
      class="image-placeholder error-placeholder"
    >
      <el-icon
        class="error-icon"
        :size="32"
      >
        <PictureFilled />
      </el-icon>
      <span class="placeholder-text">图片加载失败</span>
      <el-button
        size="small"
        text
        @click.stop="retryLoad"
      >
        重试
      </el-button>
    </div>

    <!-- 实际图片（带渐进加载效果） -->
    <img
      v-show="imageLoaded"
      ref="imageRef"
      :src="currentImageUrl"
      :alt="`${message.senderName}的图片`"
      class="image-content"
      :class="{ 'loaded': imageLoaded }"
      loading="lazy"
      decoding="async"
      @load="handleImageLoad"
      @error="handleImageError"
    >

    <!-- 上传进度遮罩 -->
    <div
      v-if="isUploading"
      class="upload-overlay"
    >
      <div class="progress-ring">
        <svg viewBox="0 0 36 36">
          <path
            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
            fill="none"
            stroke="var(--dt-white-30)"
            stroke-width="3"
          />
          <path
            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
            fill="none"
            stroke="#fff"
            stroke-width="3"
            stroke-dasharray="100"
            :stroke-dashoffset="100 - uploadProgress"
            stroke-linecap="round"
            class="progress-spinner"
          />
        </svg>
        <span class="progress-text">{{ uploadProgress }}%</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { Loading, PictureFilled } from '@element-plus/icons-vue'
import { parseMessageContent } from '@/utils/message'
import { warn } from '@/utils/logger.js'

const props = defineProps({
  message: { type: Object, required: true },
  // 大群标识，用于优化加载策略
  isLargeGroup: { type: Boolean, default: false }
})

const emit = defineEmits(['preview'])

const imageRef = ref(null)
const uploadProgress = ref(0)
const isLoading = ref(false)
const imageLoaded = ref(false)
const hasError = ref(false)
let progressTimer = null
let observer = null

// 图片 URL
const imageUrl = computed(() => {
  const parsed = parseMessageContent(props.message)
  return parsed?.imageUrl || parsed?.url || ''
})

// 缩略图 URL（用于模糊预览）
const thumbUrl = computed(() => {
  const parsed = parseMessageContent(props.message)
  // 优先使用 thumbnailUrl，否则生成带尺寸参数的缩略图 URL
  if (parsed?.thumbnailUrl) {return parsed.thumbnailUrl}
  if (parsed?.thumbUrl) {return parsed.thumbUrl}
  // 如果有原始 URL，尝试生成缩略图 URL（适用于支持的服务）
  if (imageUrl.value) {
    const url = new URL(imageUrl.value, window.location.origin)
    // 添加缩略图参数（适用于常见图床服务）
    if (url.hostname.includes('aliyuncs.com')) {
      url.searchParams.set('x-oss-process', 'image/resize,m_fill,h_200,w_200/quality,q_60')
      return url.toString()
    }
    // 可以根据不同的图床服务添加不同的参数
  }
  return null
})

// 当前显示的图片 URL（支持重试）
const currentImageUrl = ref(imageUrl.value)

// 是否正在上传
const isUploading = computed(() => {
  return ['uploading', 'sending'].includes(props.message?.status)
})

// Intersection Observer for lazy loading
const setupIntersectionObserver = () => {
  if (!('IntersectionObserver' in window)) {return}

  // 大群使用更小的 rootMargin 以减少同时加载的图片数量
  const rootMargin = props.isLargeGroup ? '20px' : '100px'

  observer = new IntersectionObserver(
    entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          // 图片进入视口，开始加载
          if (imageRef.value && !imageLoaded.value && !hasError.value) {
            isLoading.value = true
          }
          observer.unobserve(entry.target)
        }
      })
    },
    {
      rootMargin // 大群 20px，普通群 100px
    }
  )

  if (imageRef.value) {
    observer.observe(imageRef.value)
  }
}

// 处理图片加载成功
const handleImageLoad = () => {
  isLoading.value = false
  imageLoaded.value = true
  hasError.value = false
}

// 处理图片加载失败
const handleImageError = () => {
  isLoading.value = false
  imageLoaded.value = false
  hasError.value = true
  warn('ImageBubble', '图片加载失败:', currentImageUrl.value)
}

// 重试加载图片
const retryLoad = () => {
  hasError.value = false
  isLoading.value = true
  imageLoaded.value = false
  // 强制刷新 URL 以重新请求
  const url = new URL(currentImageUrl.value, window.location.origin)
  url.searchParams.set('t', Date.now())
  currentImageUrl.value = url.toString()
}

// 处理点击预览
const handleClick = () => {
  if (isUploading.value || hasError.value) {return}
  if (imageLoaded.value) {
    emit('preview', imageUrl.value)
  }
}

// 监听图片 URL 变化，重新加载
watch(() => imageUrl.value, newUrl => {
  if (newUrl && newUrl !== currentImageUrl.value) {
    currentImageUrl.value = newUrl
    isLoading.value = true
    imageLoaded.value = false
    hasError.value = false
  }
}, { immediate: true })

// 监听 imageLoaded 变化，设置 Intersection Observer
watch(imageLoaded, loaded => {
  if (loaded && observer) {
    observer.disconnect()
    observer = null
  }
})

// 组件挂载时的处理
onMounted(() => {
  if (isUploading.value) {
    progressTimer = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += Math.random() * 15
      }
    }, 300)
  } else if (imageUrl.value) {
    // 设置 Intersection Observer 实现懒加载
    setupIntersectionObserver()
  }
})

// 组件卸载清理
onUnmounted(() => {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
  if (observer) {
    observer.disconnect()
    observer = null
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.image-bubble {
  position: relative;
  display: inline-block;
  cursor: zoom-in;
  border-radius: 6px; // 钉钉紧凑标准：6px 圆角
  overflow: hidden;
  background: var(--dt-bg-card);
  transition: all var(--dt-transition-base);
  min-width: 100px; // 钉钉紧凑标准：100px 最小宽度
  min-height: 80px; // 钉钉紧凑标准：80px 最小高度

  &:hover {
    opacity: 0.95;
  }

  &.is-loading,
  &.has-error {
    cursor: default;
    pointer-events: none;

    &:hover {
      opacity: 1;
    }
  }
}

.image-content {
  display: block;
  max-width: 260px; // 钉钉紧凑标准：260px 最大宽度
  max-height: 350px; // 钉钉紧凑标准：350px 最大高度
  object-fit: contain;
  pointer-events: none;
  opacity: 0;
  transition: opacity var(--dt-transition-base) ease;

  &.loaded {
    opacity: 1;
  }
}

// 图片占位符（加载中/错误）
.image-placeholder {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px; // 钉钉紧凑标准：6px
  padding: 16px; // 钉钉紧凑标准：16px
  min-width: 100px; // 钉钉紧凑标准：100px
  min-height: 80px; // 钉钉紧凑标准：80px
  max-width: 260px; // 钉钉紧凑标准：260px
  max-height: 350px; // 钉钉紧凑标准：350px
  background: var(--dt-bg-hover);
  border-radius: 6px; // 钉钉紧凑标准：6px
  overflow: hidden;

  &.with-thumb {
    padding: 0;
  }

  .loading-icon {
    position: relative;
    z-index: 1;
    color: var(--dt-brand-color);
    animation: rotate 1s linear infinite;
  }

  .placeholder-text {
    position: relative;
    z-index: 1;
    font-size: 13px;
    color: var(--dt-text-tertiary);
    background: var(--dt-black-20);
    padding: 4px 12px;
    border-radius: var(--dt-radius-lg);
  }

  &.error-placeholder {
    .error-icon {
      color: var(--dt-text-quaternary);
    }

    .el-button {
      margin-top: 4px;
      pointer-events: auto;
    }
  }
}

// 模糊缩略图背景
.blur-thumb {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  filter: blur(10px); // 优化：降低模糊程度以提高性能
  transform: scale(1.1);
  will-change: transform; // 优化：提示浏览器优化 transform
}

.image-bubble.is-uploading {
  position: relative;
  pointer-events: none;

  .image-content {
    opacity: 0.7;
  }
}

.upload-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dt-overlay-50);
}

.progress-ring {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 56px;
    height: 56px;
    transform: rotate(-90deg);
  }

  .progress-spinner {
    animation: spinSvgStroke 1.5s linear infinite;
  }
}

.progress-text {
  position: absolute;
  color: var(--dt-text-inverse);
  font-size: 13px;
  font-weight: 600;
}

// rotate 使用全局 spin 动画 (@/styles/animations.scss)
</style>
