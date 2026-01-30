<template>
  <div
    class="image-bubble"
    :class="{ 'is-uploading': isUploading }"
    @click="handleClick"
  >
    <img
      :src="imageUrl"
      :alt="`${message.senderName}的图片`"
      class="image-content"
      loading="lazy"
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
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { parseMessageContent } from '@/utils/message'

const props = defineProps({
  message: { type: Object, required: true }
})

const emit = defineEmits(['preview'])

const uploadProgress = ref(0)
let progressTimer = null

const imageUrl = computed(() => {
  const parsed = parseMessageContent(props.message)
  return parsed?.imageUrl || parsed?.url || ''
})

const isUploading = computed(() => {
  return ['uploading', 'sending'].includes(props.message?.status)
})

const handleClick = () => {
  if (isUploading.value) return
  emit('preview', imageUrl.value)
}

onMounted(() => {
  if (isUploading.value) {
    progressTimer = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += Math.random() * 15
      }
    }, 300)
  }
})

onUnmounted(() => {
  if (progressTimer) {
    clearInterval(progressTimer)
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.image-bubble {
  display: inline-block;
  cursor: zoom-in;
  border-radius: var(--dt-radius-md);
  overflow: hidden;
  background: var(--dt-bg-card);
  transition: all var(--dt-transition-base);

  &:hover {
    box-shadow: var(--dt-shadow-lg);
    transform: translateY(-2px);
  }
}

.image-content {
  display: block;
  max-width: 320px;
  max-height: 400px;
  object-fit: contain;
  pointer-events: none;
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
</style>
