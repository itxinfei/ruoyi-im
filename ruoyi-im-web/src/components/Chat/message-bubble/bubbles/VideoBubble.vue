<template>
  <div class="video-bubble">
    <video
      v-if="videoUrl"
      :src="videoUrl"
      :poster="posterUrl"
      controls
      class="video-content"
      preload="metadata"
    />
    <div
      v-else
      class="video-placeholder"
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
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.video-bubble {
  border-radius: var(--dt-radius-md);
  overflow: hidden;
  background: var(--dt-bg-dark);
}

.video-content {
  display: block;
  max-width: 320px;
  max-height: 400px;
  border-radius: var(--dt-radius-md);
}

.video-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--dt-space-2);
  width: 200px;
  height: 120px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);
  color: var(--dt-text-tertiary);

  .el-icon {
    font-size: 32px;
  }

  span {
    font-size: var(--dt-font-size-sm);
  }
}
</style>
