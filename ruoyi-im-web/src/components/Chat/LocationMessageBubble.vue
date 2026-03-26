<template>
  <div class="location-message" :class="{ 'is-own': message.isOwn }" @click="openMap">
    <!-- 地图缩略图 -->
    <div class="map-thumbnail" :style="mapStyle">
      <div class="map-overlay">
        <span class="material-icons-outlined">location_on</span>
      </div>
    </div>

    <!-- 位置信息 -->
    <div class="location-info">
      <div class="location-name">
        {{ locationName || '位置共享' }}
      </div>
      <div class="location-address">
        {{ address || '点击查看地图' }}
      </div>
    </div>

    <!-- 导航图标 -->
    <div class="nav-icon">
      <span class="material-icons-outlined">navigation</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

defineEmits(['command'])

// 解析位置内容
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

// 位置名称
const locationName = computed(() => parsedContent.value.name || parsedContent.value.title || '')

// 地址
const address = computed(() => parsedContent.value.address || parsedContent.value.desc || '')

// 经纬度
const latitude = computed(() => parsedContent.value.latitude || parsedContent.value.lat)
const longitude = computed(() => parsedContent.value.longitude || parsedContent.value.lng)

// 地图缩略图样式 (使用静态地图API)
const mapStyle = computed(() => {
  const lat = latitude.value
  const lng = longitude.value
  const mapKey = import.meta.env.VITE_MAP_API_KEY || ''
  if (lat && lng && mapKey) {
    // 使用高德/腾讯静态地图
    return {
      backgroundImage: `url(https://apis.map.qq.com/ws/staticmap/v2/?center=${lat},${lng}&zoom=15&size=200x120&markers=${lat},${lng}&key=${mapKey})`
    }
  }
  return {}
})

// 点击打开地图
const openMap = () => {
  const lat = latitude.value
  const lng = longitude.value
  if (lat && lng) {
    // 打开腾讯地图或高德地图
    window.open(`https://apis.map.qq.com/uri/v1/marker?marker=coord:${lat},${lng};title:${encodeURIComponent(locationName.value)};addr:${encodeURIComponent(address.value)}`, '_blank')
  }
}
</script>

<style scoped lang="scss">
.location-message {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  border: 1px solid var(--dt-border-light);
  cursor: pointer;
  min-width: 220px;
  max-width: 280px;
  transition: all 0.2s;

  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: 0 2px 8px rgba(22, 119, 255, 0.15);
  }

  // 地图缩略图
  .map-thumbnail {
    width: 80px;
    height: 60px;
    border-radius: var(--dt-radius-md);
    background: var(--dt-brand-lighter);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    overflow: hidden;
    background-size: cover;
    background-position: center;

    .map-overlay {
      width: 28px;
      height: 28px;
      background: var(--dt-brand-color);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      box-shadow: 0 2px 6px rgba(22, 119, 255, 0.3);

      .material-icons-outlined {
        font-size: 18px;
      }
    }
  }

  // 位置信息
  .location-info {
    flex: 1;
    overflow: hidden;

    .location-name {
      font-size: 14px;
      font-weight: 600;
      color: var(--dt-text-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .location-address {
      font-size: 12px;
      color: var(--dt-text-tertiary);
      margin-top: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  // 导航图标
  .nav-icon {
    width: 32px;
    height: 32px;
    background: var(--dt-brand-bg);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--dt-brand-color);
    flex-shrink: 0;

    .material-icons-outlined {
      font-size: 18px;
    }
  }

  // 自己的消息样式
  &.is-own {
    background: var(--dt-brand-lighter);
    border-color: var(--dt-brand-lighter);

    .map-thumbnail {
      background: var(--dt-brand-bg);
    }

    .location-info {
      .location-name {
        color: var(--dt-brand-color);
      }
    }

    .nav-icon {
      background: var(--dt-brand-color);
      color: #fff;
    }
  }
}

// 暗色模式
.dark .location-message {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);

  .location-info {
    .location-name {
      color: var(--dt-text-primary-dark);
    }
  }

  &.is-own {
    background: var(--dt-brand-bg);
    border-color: var(--dt-brand-bg);
  }
}
</style>
