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
    <!-- 加载占位符 -->
    <div v-if="isLoading && !imageLoaded" class="image-placeholder">
      <el-icon class="loading-icon" :size="32">
        <Loading />
      </el-icon>
      <span class="placeholder-text">加载中...</span>
    </div>

    <!-- 错误占位符 -->
    <div v-else-if="hasError" class="image-placeholder error-placeholder">
      <el-icon class="error-icon" :size="32">
        <PictureFilled />
      </el-icon>
      <span class="placeholder-text">图片加载失败</span>
      <el-button size="small" text @click.stop="retryLoad">重试</el-button>
    </div>

    <!-- 实际图片 -->
    <img
      v-show="imageLoaded"
      ref="imageRef"
      :src="currentImageUrl"
      :alt="`${message.senderName}的图片`"
      class="image-content"
      loading="lazy"
      @load="handleImageLoad"
      @error="handleImageError"
    />

    <!-- 上传进度遮罩 -->
    <div v-if="isUploading" class="upload-overlay">
      <div class="progress-ring">
        <svg viewBox="0 0 36 36">
          <path
            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
            fill="none"
            stroke="rgba(255, 255, 255, 0.3)"
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

const props = defineProps({
  message: { type: Object, required: true }
})

const emit = defineEmits(['preview'])

const imageRef = ref(null)
const uploadProgress = ref(0)
const isLoading = ref(false)
const imageLoaded = ref(false)
const hasError = ref(false)
let progressTimer = null

// 图片 URL
const imageUrl = computed(() => {
  const parsed = parseMessageContent(props.message)
  return parsed?.imageUrl || parsed?.url || ''
})

// 当前显示的图片 URL（支持重试）
const currentImageUrl = ref(imageUrl.value)

// 是否正在上传
const isUploading = computed(() => {
  return ['uploading', 'sending'].includes(props.message?.status)
})

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
  console.warn('图片加载失败:', currentImageUrl.value)
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
  if (isUploading.value || hasError.value) return
  if (imageLoaded.value) {
    emit('preview', imageUrl.value)
  }
}

// 监听图片 URL 变化，重新加载
watch(() => imageUrl.value, (newUrl) => {
  if (newUrl && newUrl !== currentImageUrl.value) {
    currentImageUrl.value = newUrl
    isLoading.value = true
    imageLoaded.value = false
    hasError.value = false
  }
}, { immediate: true })

// 组件挂载时的处理
onMounted(() => {
  if (isUploading.value) {
    progressTimer = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += Math.random() * 15
      }
    }, 300)
  } else if (imageUrl.value) {
    // 非上传状态，开始加载图片
    isLoading.value = true
  }
})

// 组件卸载清理
onUnmounted(() => {
  if (progressTimer) {
    clearInterval(progressTimer)
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.image-bubble {
  position: relative;
  display: inline-block;
  cursor: zoom-in;
  border-radius: var(--dt-radius-md);
  overflow: hidden;
  background: var(--dt-bg-card);
  transition: all var(--dt-transition-base);
  min-width: 200px;
  min-height: 150px;

  &:hover {
    box-shadow: var(--dt-shadow-lg);
    transform: translateY(-2px);
  }

  &.is-loading,
  &.has-error {
    cursor: default;
    pointer-events: none;

    &:hover {
      transform: none;
      box-shadow: none;
    }
  }
}

.image-content {
  display: block;
  max-width: 320px;
  max-height: 400px;
  object-fit: contain;
  pointer-events: none;
}

// 图片占位符（加载中/错误）
.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 32px;
  min-width: 200px;
  min-height: 150px;
  max-width: 320px;
  max-height: 400px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);

  .loading-icon {
    color: var(--dt-brand-color);
    animation: rotate 1s linear infinite;
  }

  .placeholder-text {
    font-size: 13px;
    color: var(--dt-text-tertiary);
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

.image-bubble.is-uploading {
  position: relative;
  pointer-events: none;

  .image-content {
    opacity: 0.7;
    filter: blur(4px);
  }
}

.upload-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
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
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}

// rotate 使用全局 spin 动画 (@/styles/animations.scss)
</style>
