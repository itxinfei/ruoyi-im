/**
 * 文本消息气泡组件
 */
<template>
  <div class="text-bubble">
    <!-- 文本内容 -->
    <div class="text-content">{{ message.content }}</div>

    <!-- 已编辑标记 -->
    <span v-if="message.isEdited" class="edited-tag">(已编辑)</span>

    <!-- 标记图标 -->
    <div v-if="hasMarkers" class="message-markers">
      <span v-for="marker in message.markers" :key="marker.id || marker.markerType"
            class="marker-icon"
            :class="{ completed: marker.isCompleted }"
            :style="{ color: marker.color || '' }">
        <span v-if="marker.markerType === 'FLAG'" class="material-icons-outlined">flag</span>
        <span v-else-if="marker.markerType === 'IMPORTANT'" class="material-icons-outlined">star</span>
        <span v-else-if="marker.markerType === 'TODO'" class="material-icons-outlined">
          {{ marker.isCompleted ? 'check_circle' : 'check_circle_outline' }}
        </span>
      </span>
    </div>

    <!-- 置顶图标 -->
    <div v-if="message.isPinned" class="message-pinned-badge" title="已置顶">
      <el-icon><Top /></el-icon>
      <span>已置顶</span>
    </div>
  </div>
</template>

<script setup>
import { Top } from '@element-plus/icons-vue'

const props = defineProps({
  message: { type: Object, required: true },
  hasMarkers: { type: Boolean, default: false }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.text-bubble {
  position: relative;
}

.text-content {
  word-break: break-word;
  white-space: pre-wrap;
  line-height: 1.4;
}

.edited-tag {
  margin-left: 4px;
  font-size: 11px;
  color: var(--dt-text-quaternary);
}

.message-markers {
  display: flex;
  gap: 4px;
  margin-top: 4px;

  .marker-icon {
    font-size: 16px;
    opacity: 0.8;

    &.completed {
      opacity: 0.5;
    }
  }
}

.message-pinned-badge {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  margin-top: 4px;
  padding: 2px 6px;
  background: var(--dt-brand-bg);
  border-radius: 4px;
  font-size: 11px;
  color: var(--dt-brand-color);
}
</style>
