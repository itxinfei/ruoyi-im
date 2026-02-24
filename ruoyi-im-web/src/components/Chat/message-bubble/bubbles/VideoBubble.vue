<template>
  <div
    class="video-bubble"
    :style="containerStyle"
  >
    <video
      v-if="videoUrl"
      :src="videoUrl"
      :poster="posterUrl"
      controls
      class="video-content"
      preload="metadata"
      :style="containerStyle"
    />
    <div
      v-else
      class="video-placeholder"
      :style="containerStyle"
    >
      <el-icon><VideoCamera /></el-icon>
      <span>视频加载失败</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { VideoCamera } from '@element-plus/icons-vue'
import { parseMessageContent } from '@/utils/message'

const props = defineProps({
  message: { type: Object, required: true }
})

const parsedContent = computed(() => parseMessageContent(props.message) || {})

const videoUrl = computed(() => {
  return parsedContent.value.videoUrl || parsedContent.value.url || ''
})

const posterUrl = computed(() => {
  return parsedContent.value.posterUrl || parsedContent.value.thumbnail || ''
})

const videoSize = computed(() => {
  const width = parseInt(parsedContent.value.width) || 0
  const height = parseInt(parsedContent.value.height) || 0
  
  if (width > 0 && height > 0) {
    const ratio = width / height
    const safeRatio = Math.max(0.5, Math.min(2, ratio))
    
    let displayWidth = 260
    let displayHeight = 260 / safeRatio
    
    if (displayHeight > 350) {
      displayHeight = 350
      displayWidth = 350 * safeRatio
    }
    
    return {
      width: Math.round(displayWidth),
      height: Math.round(displayHeight),
      ratio: safeRatio
    }
  }
  
  return { width: 260, height: 160, ratio: 1.625 }
})

const containerStyle = computed(() => {
  return {
    width: `${videoSize.value.width}px`,
    height: `${videoSize.value.height}px`,
    aspectRatio: `${videoSize.value.ratio}`
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.video-bubble {
  border-radius: 6px;
  overflow: hidden;
  background: var(--dt-bg-dark);
}

.video-content {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

.video-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--dt-space-2);
  background: var(--dt-bg-hover);
  border-radius: 6px;
  color: var(--dt-text-tertiary);

  .el-icon {
    font-size: 32px;
  }

  span {
    font-size: var(--dt-font-size-sm);
  }
}
</style>
