<template>
  <div class="location-bubble" @click="openLocation">
    <!-- 位置图标 -->
    <div class="location-icon">
      <span class="material-icons-outlined">location_on</span>
    </div>

    <!-- 位置信息 -->
    <div class="location-info">
      <div class="location-address">{{ address || '位置信息' }}</div>
      <div class="location-coords">{{ formattedCoords }}</div>
    </div>

    <!-- 打开箭头 -->
    <div class="location-arrow">
      <span class="material-icons-outlined">open_in_new</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { parseMessageContent } from '@/utils/message'

const props = defineProps({
  message: { type: Object, required: true }
})

const parsedContent = computed(() => parseMessageContent(props.message) || {})

const address = computed(() => {
  return parsedContent.value.address || parsedContent.value.name || ''
})

const latitude = computed(() => {
  return parsedContent.value.latitude || parsedContent.value.lat || 0
})

const longitude = computed(() => {
  return parsedContent.value.longitude || parsedContent.value.lng || parsedContent.value.lon || 0
})

const formattedCoords = computed(() => {
  if (!latitude.value || !longitude.value) return ''
  return `${latitude.value.toFixed(6)}, ${longitude.value.toFixed(6)}`
})

const openLocation = () => {
  if (!latitude.value || !longitude.value) {
    ElMessage.warning('位置信息不完整')
    return
  }

  // 使用高德地图打开位置
  const url = `https://uri.amap.com/marker?position=${longitude.value},${latitude.value}&name=${encodeURIComponent(address.value || '位置')}`
  window.open(url, '_blank')
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.location-bubble {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 200px;
  max-width: 320px;
  padding: 12px 16px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    border-color: var(--dt-brand-color);
    background: #e0f2fe;
    transform: translateY(-1px);
    box-shadow: var(--dt-shadow-card);
  }
}

.location-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #0ea5e9, #0284c7);
  border-radius: 50%;
  color: #fff;
  flex-shrink: 0;
  transition: transform var(--dt-transition-fast);

  .location-bubble:hover & {
    transform: scale(1.05);
  }

  .material-icons-outlined {
    font-size: 24px;
  }
}

.location-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.location-address {
  font-size: var(--dt-font-size-base);
  font-weight: 500;
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.location-coords {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-secondary);
  font-variant-numeric: tabular-nums;
}

.location-arrow {
  color: #0ea5e9;
  flex-shrink: 0;

  .material-icons-outlined {
    font-size: 18px;
  }
}
</style>
