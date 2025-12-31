<template>
  <el-dialog
    v-model="visible"
    title="发送位置"
    width="600px"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    class="location-picker-dialog"
    @close="handleClose"
  >
    <!-- 搜索栏 -->
    <div class="location-search">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索地点..."
        :prefix-icon="Search"
        clearable
        @input="handleSearchDebounced"
        @keyup.enter="handleSearch"
      />
    </div>

    <!-- 地图容器 -->
    <div class="map-container">
      <!-- 地图占位区域（实际项目中集成高德/百度地图） -->
      <div ref="mapRef" class="map-view">
        <div v-if="!mapLoaded" class="map-placeholder">
          <el-icon class="map-icon"><Location /></el-icon>
          <p>地图加载中...</p>
        </div>
        <!-- 模拟地图显示 -->
        <div v-else class="map-mock">
          <div class="map-grid">
            <div
              v-for="i in 9"
              :key="i"
              class="grid-cell"
              :class="{ 'center': i === 5 }"
            ></div>
          </div>
          <!-- 中心标记 -->
          <div class="center-marker">
            <el-icon><Location /></el-icon>
          </div>
          <!-- 当前位置信息 -->
          <div class="current-location-info">
            <span class="location-name">{{ selectedLocation?.name || '选择位置' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 位置列表 -->
    <div class="location-list">
      <div class="list-header">
        <span class="header-title">附近地点</span>
        <el-button
          type="primary"
          link
          :icon="Refresh"
          :loading="loading"
          @click="refreshLocations"
        >
          刷新
        </el-button>
      </div>

      <el-scrollbar height="200px">
        <div
          v-for="(location, index) in locationList"
          :key="index"
          class="location-item"
          :class="{ 'active': selectedLocation?.id === location.id }"
          @click="selectLocation(location)"
        >
          <div class="location-icon">
            <el-icon><Location /></el-icon>
          </div>
          <div class="location-info">
            <div class="location-name">{{ location.name }}</div>
            <div class="location-address">{{ location.address }}</div>
          </div>
          <div v-if="selectedLocation?.id === location.id" class="location-check">
            <el-icon><Check /></el-icon>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty
          v-if="locationList.length === 0 && !loading"
          description="暂无附近地点"
          :image-size="60"
        />

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
      </el-scrollbar>
    </div>

    <!-- 底部操作 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :disabled="!selectedLocation"
          @click="handleConfirm"
        >
          发送位置
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * @file LocationPicker.vue
 * @description 位置选择器组件 - 用于选择和发送地理位置
 * @author IM System
 * @version 1.0.0
 */

import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Location, Search, Check, Refresh, Loading } from '@element-plus/icons-vue'
import { debounce } from '@/utils/common'

// ==================== Props 定义 ====================
const props = defineProps({
  /**
   * 控制对话框显示状态
   */
  modelValue: {
    type: Boolean,
    default: false,
  },
})

// ==================== Emits 定义 ====================
const emit = defineEmits([
  'update:modelValue',
  'select',   // 选择位置时触发
  'confirm',  // 确认发送时触发
])

// ==================== 响应式状态 ====================

/** 对话框显示状态 */
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

/** 搜索关键词 */
const searchKeyword = ref('')

/** 地图是否加载完成 */
const mapLoaded = ref(false)

/** 加载状态 */
const loading = ref(false)

/** 地图DOM引用 */
const mapRef = ref(null)

/** 当前选中的位置 */
const selectedLocation = ref(null)

/** 当前定位坐标 */
const currentCoords = ref({
  latitude: 39.9042,   // 默认北京坐标
  longitude: 116.4074,
})

/** 位置列表 */
const locationList = ref([])

// ==================== 模拟数据 ====================

/**
 * 生成模拟的附近位置数据
 * @param {number} lat - 纬度
 * @param {number} lng - 经度
 * @returns {Array} 位置列表
 */
const generateMockLocations = (lat, lng) => {
  const baseLocations = [
    { name: '当前位置', address: '我的位置', type: 'current' },
    { name: '北京市朝阳区', address: '北京市朝阳区建国路89号', type: 'poi' },
    { name: '国贸商城', address: '北京市朝阳区建国门外大街1号', type: 'poi' },
    { name: '中国大饭店', address: '北京市朝阳区建国门外大街1号', type: 'poi' },
    { name: '北京银泰中心', address: '北京市朝阳区建国门外大街2号', type: 'poi' },
    { name: '央视大楼', address: '北京市朝阳区东三环中路32号', type: 'poi' },
    { name: '三里屯太古里', address: '北京市朝阳区三里屯路19号', type: 'poi' },
    { name: '工人体育场', address: '北京市朝阳区工人体育场北路', type: 'poi' },
  ]

  return baseLocations.map((loc, index) => ({
    id: `loc_${index}`,
    name: loc.name,
    address: loc.address,
    type: loc.type,
    latitude: lat + (Math.random() - 0.5) * 0.01,
    longitude: lng + (Math.random() - 0.5) * 0.01,
    distance: index === 0 ? 0 : Math.floor(Math.random() * 2000) + 100,
  }))
}

// ==================== 方法定义 ====================

/**
 * 获取当前地理位置
 * @description 使用浏览器Geolocation API获取当前位置
 */
const getCurrentPosition = () => {
  if (!navigator.geolocation) {
    ElMessage.warning('您的浏览器不支持地理定位')
    loadDefaultLocations()
    return
  }

  loading.value = true

  navigator.geolocation.getCurrentPosition(
    (position) => {
      currentCoords.value = {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
      }
      loadNearbyLocations()
    },
    (error) => {
      console.warn('获取位置失败:', error.message)
      ElMessage.warning('无法获取当前位置，已使用默认位置')
      loadDefaultLocations()
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 60000,
    }
  )
}

/**
 * 加载默认位置列表
 */
const loadDefaultLocations = () => {
  loading.value = true

  // 模拟网络延迟
  setTimeout(() => {
    locationList.value = generateMockLocations(
      currentCoords.value.latitude,
      currentCoords.value.longitude
    )
    loading.value = false
    mapLoaded.value = true

    // 默认选中第一个位置
    if (locationList.value.length > 0) {
      selectedLocation.value = locationList.value[0]
    }
  }, 500)
}

/**
 * 加载附近位置列表
 */
const loadNearbyLocations = () => {
  loading.value = true

  // 模拟API请求延迟
  setTimeout(() => {
    locationList.value = generateMockLocations(
      currentCoords.value.latitude,
      currentCoords.value.longitude
    )
    loading.value = false
    mapLoaded.value = true

    // 默认选中"当前位置"
    if (locationList.value.length > 0) {
      selectedLocation.value = locationList.value[0]
    }
  }, 800)
}

/**
 * 刷新位置列表
 */
const refreshLocations = () => {
  getCurrentPosition()
}

/**
 * 搜索位置
 * @description 根据关键词搜索位置
 */
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    loadNearbyLocations()
    return
  }

  loading.value = true

  // 模拟搜索请求
  setTimeout(() => {
    const keyword = searchKeyword.value.toLowerCase()
    const searchResults = generateMockLocations(
      currentCoords.value.latitude,
      currentCoords.value.longitude
    ).filter(loc =>
      loc.name.toLowerCase().includes(keyword) ||
      loc.address.toLowerCase().includes(keyword)
    )

    locationList.value = searchResults
    loading.value = false

    if (searchResults.length === 0) {
      ElMessage.info('未找到相关位置')
    }
  }, 500)
}

/**
 * 防抖搜索处理
 */
const handleSearchDebounced = debounce(handleSearch, 500)

/**
 * 选择位置
 * @param {Object} location - 位置对象
 */
const selectLocation = (location) => {
  selectedLocation.value = location
  emit('select', location)
}

/**
 * 确认发送位置
 */
const handleConfirm = () => {
  if (!selectedLocation.value) {
    ElMessage.warning('请选择一个位置')
    return
  }

  // 构建位置消息数据
  const locationData = {
    type: 'location',
    name: selectedLocation.value.name,
    address: selectedLocation.value.address,
    latitude: selectedLocation.value.latitude,
    longitude: selectedLocation.value.longitude,
    // 生成静态地图图片URL（实际项目中使用地图API）
    mapUrl: generateStaticMapUrl(selectedLocation.value),
  }

  emit('confirm', locationData)
  handleClose()
}

/**
 * 生成静态地图URL
 * @param {Object} location - 位置对象
 * @returns {string} 静态地图URL
 */
const generateStaticMapUrl = (location) => {
  // 实际项目中使用高德/百度地图静态图API
  // 示例: https://restapi.amap.com/v3/staticmap?location=${lng},${lat}&zoom=15&size=400*300&markers=mid,,A:${lng},${lat}&key=YOUR_KEY
  return `https://placeholder.map/${location.longitude},${location.latitude}`
}

/**
 * 关闭对话框
 */
const handleClose = () => {
  visible.value = false
  searchKeyword.value = ''
  selectedLocation.value = null
}

// ==================== 生命周期钩子 ====================

/**
 * 监听对话框显示状态
 */
watch(visible, (newVal) => {
  if (newVal) {
    // 对话框打开时获取位置
    getCurrentPosition()
  }
})

/**
 * 组件挂载时初始化
 */
onMounted(() => {
  // 可以在这里预加载地图SDK
})

/**
 * 组件卸载时清理
 */
onUnmounted(() => {
  // 清理地图实例等资源
})
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.location-picker-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

// 搜索栏
.location-search {
  padding: $spacing-lg;
  border-bottom: 1px solid $border-light;

  :deep(.el-input__wrapper) {
    border-radius: $border-radius-lg;
    background-color: $bg-base;
    box-shadow: none;

    &:focus-within {
      background-color: $bg-white;
      box-shadow: 0 0 0 1px $primary-color;
    }
  }
}

// 地图容器
.map-container {
  height: 200px;
  background-color: $bg-base;
  position: relative;

  .map-view {
    width: 100%;
    height: 100%;
  }

  .map-placeholder {
    @include flex-center;
    flex-direction: column;
    height: 100%;
    color: $text-tertiary;

    .map-icon {
      font-size: 48px;
      margin-bottom: $spacing-md;
      color: $text-placeholder;
    }

    p {
      font-size: 14px;
    }
  }

  .map-mock {
    width: 100%;
    height: 100%;
    position: relative;
    background: linear-gradient(135deg, #e8f4f8 0%, #d4e8e0 100%);

    .map-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      grid-template-rows: repeat(3, 1fr);
      height: 100%;
      gap: 1px;

      .grid-cell {
        background-color: rgba($bg-white, 0.3);
        border: 1px dashed rgba($border-base, 0.5);

        &.center {
          background-color: rgba($primary-color, 0.1);
        }
      }
    }

    .center-marker {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -100%);
      color: $error-color;
      font-size: 32px;
      filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
      animation: marker-bounce 1s ease-in-out infinite;
    }

    .current-location-info {
      position: absolute;
      bottom: $spacing-md;
      left: $spacing-md;
      right: $spacing-md;
      background-color: rgba($bg-white, 0.95);
      padding: $spacing-sm $spacing-md;
      border-radius: $border-radius-base;
      box-shadow: $shadow-sm;

      .location-name {
        font-size: 14px;
        font-weight: 500;
        color: $text-primary;
      }
    }
  }
}

// 位置列表
.location-list {
  border-top: 1px solid $border-light;

  .list-header {
    @include flex-between;
    padding: $spacing-md $spacing-lg;
    border-bottom: 1px solid $border-light;

    .header-title {
      font-size: 14px;
      font-weight: 500;
      color: $text-secondary;
    }
  }

  .location-item {
    display: flex;
    align-items: center;
    padding: $spacing-md $spacing-lg;
    cursor: pointer;
    transition: background-color $transition-fast $ease-base;

    &:hover {
      background-color: $bg-hover;
    }

    &.active {
      background-color: $primary-color-light;

      .location-icon {
        color: $primary-color;
      }

      .location-name {
        color: $primary-color;
      }
    }

    .location-icon {
      width: 36px;
      height: 36px;
      @include flex-center;
      background-color: $bg-base;
      border-radius: $border-radius-round;
      color: $text-tertiary;
      font-size: 18px;
      margin-right: $spacing-md;
      flex-shrink: 0;
    }

    .location-info {
      flex: 1;
      min-width: 0;

      .location-name {
        font-size: 14px;
        font-weight: 500;
        color: $text-primary;
        @include text-ellipsis;
        margin-bottom: 2px;
      }

      .location-address {
        font-size: 12px;
        color: $text-tertiary;
        @include text-ellipsis;
      }
    }

    .location-check {
      color: $primary-color;
      font-size: 18px;
      margin-left: $spacing-sm;
    }
  }

  .loading-state {
    @include flex-center;
    padding: $spacing-xl;
    color: $text-tertiary;
    gap: $spacing-sm;

    .is-loading {
      animation: spin 1s linear infinite;
    }
  }
}

// 底部操作
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}

// 动画
@keyframes marker-bounce {
  0%, 100% {
    transform: translate(-50%, -100%);
  }
  50% {
    transform: translate(-50%, -110%);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 响应式适配
@media (max-width: $breakpoint-md) {
  .location-picker-dialog {
    :deep(.el-dialog) {
      width: 90% !important;
      margin: 5vh auto !important;
    }
  }

  .map-container {
    height: 150px;
  }
}
</style>
