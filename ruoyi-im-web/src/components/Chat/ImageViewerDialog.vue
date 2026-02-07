<template>
  <el-dialog v-model="visible" :show-close="false" :close-on-click-modal="true" :close-on-press-escape="true" fullscreen
    class="image-viewer-dialog" @close="handleClose">
    <div class="image-viewer-container" @click.self="handleClose">
      <!-- 工具栏 -->
      <div v-if="images.length > 0" class="viewer-toolbar">
        <div class="toolbar-left">
          <span class="image-counter">{{ currentIndex + 1 }}&nbsp;/&nbsp;{{ images.length }}</span>
        </div>
        <div class="toolbar-center">
          <span class="image-name">{{ currentImageName }}</span>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" :icon="Download" circle size="small" title="下载" @click.stop="downloadCurrent" />
          <el-button type="default" :icon="CloseBold" circle size="small" title="关闭 (ESC)" @click.stop="handleClose" />
        </div>
      </div>

      <!-- 主图片区域 -->
      <div class="viewer-main" @wheel.prevent="handleWheel">
        <transition name="image-fade" mode="out-in">
          <div :key="currentIndex" class="image-wrapper"
            :style="{ transform: `scale(${scale}) rotate(${rotation}deg)` }">
            <img :src="currentImage" :alt="`图片 ${currentIndex + 1}`" @mousedown="handleDragStart"
              @load="handleImageLoad" @error="handleImageError">
          </div>
        </transition>

        <!-- 加载状态 -->
        <div v-if="loading" class="image-loading">
          <el-icon class="is-loading" :size="40">
            <Loading />
          </el-icon>
        </div>

        <!-- 加载失败 -->
        <div v-if="error" class="image-error">
          <el-icon :size="48">
            <PictureFilled />
          </el-icon>
          <span>图片加载失败</span>
        </div>
      </div>

      <!-- 左右切换按钮 -->
      <template v-if="images.length > 1">
        <el-button class="nav-btn nav-prev" :icon="ArrowLeft" circle size="large" :disabled="currentIndex === 0"
          @click.stop="prevImage" />
        <el-button class="nav-btn nav-next" :icon="ArrowRight" circle size="large"
          :disabled="currentIndex === images.length - 1" @click.stop="nextImage" />
      </template>

      <!-- 缩放控制栏 -->
      <div class="zoom-controls">
        <el-button-group>
          <el-button :icon="ZoomOut" :disabled="scale <= 0.3" @click.stop="zoomOut" />
          <el-button disabled class="zoom-display">
            {{ Math.round(scale * 100) }}%
          </el-button>
          <el-button :icon="ZoomIn" :disabled="scale >= 3" @click.stop="zoomIn" />
        </el-button-group>
        <el-button :icon="RefreshLeft" title="向左旋转" @click.stop="rotateLeft" />
        <el-button :icon="RefreshRight" title="向右旋转" @click.stop="rotateRight" />
        <el-button :icon="FullScreen" title="重置视图" @click.stop="resetView" />
      </div>

      <!-- 缩略图列表 -->
      <div v-if="images.length > 1" class="thumbnail-list">
        <div v-for="(img, index) in images" :key="index" class="thumbnail-item"
          :class="{ active: index === currentIndex }" @click.stop="goToImage(index)">
          <img :src="img" :alt="`缩略图 ${index + 1}`">
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import {
  ArrowLeft,
  ArrowRight,
  ZoomIn,
  ZoomOut,
  RefreshLeft,
  RefreshRight,
  CloseBold,
  Download,
  Loading,
  PictureFilled,
  FullScreen
} from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  images: {
    type: Array,
    default: () => []
  },
  initialIndex: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

// 状态
const visible = ref(false)
const currentIndex = ref(0)
const scale = ref(1)
const rotation = ref(0)
const loading = ref(false)
const error = ref(false)

// 拖拽状态
const isDragging = ref(false)
const dragStartX = ref(0)
const dragStartY = ref(0)
const dragOffsetX = ref(0)
const dragOffsetY = ref(0)

// 当前图片
const currentImage = computed(() => {
  return props.images[currentIndex.value] || ''
})

// 当前图片名称
const currentImageName = computed(() => {
  const url = currentImage.value
  if (!url) { return '' }
  try {
    const pathname = new URL(url).pathname
    const filename = pathname.split('/').pop()
    return decodeURIComponent(filename)
  } catch {
    return `图片_${currentIndex.value + 1}`
  }
})

// 监听显示状态
watch(() => props.modelValue, val => {
  visible.value = val
  if (val) {
    resetView()
    error.value = false
  }
})

watch(visible, val => {
  emit('update:modelValue', val)
})

// 监听初始索引
watch(() => props.initialIndex, val => {
  if (val >= 0 && val < props.images.length) {
    currentIndex.value = val
  }
})

// 键盘事件
const handleKeydown = e => {
  if (!visible.value) { return }

  switch (e.key) {
    case 'ArrowLeft':
      prevImage()
      break
    case 'ArrowRight':
      nextImage()
      break
    case 'Escape':
      handleClose()
      break
    case '+':
    case '=':
      zoomIn()
      break
    case '-':
    case '_':
      zoomOut()
      break
    case '0':
      resetView()
      break
  }
}

// 图片操作
const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    resetView()
    emit('change', currentIndex.value)
  }
}

const nextImage = () => {
  if (currentIndex.value < props.images.length - 1) {
    currentIndex.value++
    resetView()
    emit('change', currentIndex.value)
  }
}

const goToImage = index => {
  currentIndex.value = index
  resetView()
  emit('change', index)
}

const zoomIn = () => {
  scale.value = Math.min(scale.value + 0.2, 3)
}

const zoomOut = () => {
  scale.value = Math.max(scale.value - 0.2, 0.3)
}

const rotateLeft = () => {
  rotation.value -= 90
}

const rotateRight = () => {
  rotation.value += 90
}

const resetView = () => {
  scale.value = 1
  rotation.value = 0
  dragOffsetX.value = 0
  dragOffsetY.value = 0
}

// 滚轮缩放
const handleWheel = e => {
  if (e.deltaY < 0) {
    zoomIn()
  } else {
    zoomOut()
  }
}

// 拖拽
const handleDragStart = e => {
  if (scale.value > 1) {
    isDragging.value = true
    dragStartX.value = e.clientX - dragOffsetX.value
    dragStartY.value = e.clientY - dragOffsetY.value

    document.addEventListener('mousemove', handleDragMove)
    document.addEventListener('mouseup', handleDragEnd)
  }
}

const handleDragMove = e => {
  if (isDragging.value) {
    dragOffsetX.value = e.clientX - dragStartX.value
    dragOffsetY.value = e.clientY - dragStartY.value
  }
}

const handleDragEnd = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', handleDragMove)
  document.removeEventListener('mouseup', handleDragEnd)
}

// 图片加载
const handleImageLoad = () => {
  loading.value = false
  error.value = false
}

const handleImageError = () => {
  loading.value = false
  error.value = true
}

// 下载当前图片
const downloadCurrent = () => {
  const link = document.createElement('a')
  link.href = currentImage.value
  link.download = currentImageName.value
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 关闭
const handleClose = () => {
  visible.value = false
}

// 生命周期
onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.removeEventListener('mousemove', handleDragMove)
  document.removeEventListener('mouseup', handleDragEnd)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.image-viewer-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }

  :deep(.el-dialog__body) {
    padding: 0;
    height: 100vh;
    background: rgba(0, 0, 0, 0.95); // 稍微透明
  }
}

.image-viewer-container {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 工具栏
.viewer-toolbar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.7) 0%, transparent 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 100;
  color: #fff;

  .toolbar-left {
    .image-counter {
      font-size: 14px;
      font-weight: 500;
      opacity: 0.9;
    }
  }

  .toolbar-center {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);

    .image-name {
      font-size: 14px;
      opacity: 0.8;
      max-width: 300px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .toolbar-right {
    display: flex;
    gap: 8px;

    .el-button {
      background: rgba(255, 255, 255, 0.15);
      border-color: transparent;
      color: #fff;

      &:hover {
        background: #4168e0; // 野火IM蓝
        border-color: #4168e0;
      }

      &.is-disabled {
        opacity: 0.3;
      }
    }
  }
}

// 主图片区域
.viewer-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  cursor: grab;

  &:active {
    cursor: grabbing;
  }

  .image-wrapper {
    transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1); // 优化动画曲线
    max-width: 90%;
    max-height: 80%;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      max-width: 100%;
      max-height: 80vh;
      object-fit: contain;
      user-select: none;
      -webkit-user-drag: none;
      border-radius: 4px; // 野火IM风格圆角
    }
  }
}

// 加载状态
.image-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: rgba(255, 255, 255, 0.7);
}

.image-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.5);
}

// 导航按钮
.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 90;
  background: rgba(255, 255, 255, 0.15);
  border-color: transparent;
  color: #fff;
  opacity: 0;
  transition: all 0.2s;

  .image-viewer-container:hover & {
    opacity: 1;
  }

  &:hover:not(.is-disabled) {
    background: #4168e0; // 野火IM蓝
    border-color: #4168e0;
    transform: translateY(-50%) scale(1.1);
  }

  &.is-disabled {
    opacity: 0.2;
  }
}

.nav-prev {
  left: 24px;
}

.nav-next {
  right: 24px;
}

// 缩放控制
.zoom-controls {
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 90;
  background: rgba(0, 0, 0, 0.7);
  padding: 8px 12px;
  border-radius: 8px; // 野火IM圆角
  opacity: 0;
  transition: opacity 0.3s;
  backdrop-filter: blur(8px);

  .image-viewer-container:hover & {
    opacity: 1;
  }

  .el-button {
    background: transparent;
    border-color: transparent;
    color: #fff;

    &:hover {
      background: rgba(65, 104, 224, 0.5); // 野火IM蓝
    }

    &.is-disabled {
      opacity: 0.3;
    }

    &.zoom-display {
      min-width: 60px;
      color: #fff;
      font-weight: 500;
    }
  }
}

// 缩略图列表
.thumbnail-list {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 72px;
  background: linear-gradient(0deg, rgba(0, 0, 0, 0.8) 0%, transparent 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 24px;
  overflow-x: auto;
  z-index: 90;

  &::-webkit-scrollbar {
    height: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.3);
    border-radius: 2px;
  }

  .thumbnail-item {
    width: 56px;
    height: 56px;
    border-radius: 4px; // 野火IM圆角
    overflow: hidden;
    cursor: pointer;
    opacity: 0.6;
    transition: all 0.2s;
    border: 2px solid transparent;
    flex-shrink: 0;

    &:hover {
      opacity: 0.9;
      transform: scale(1.05);
    }

    &.active {
      opacity: 1;
      border-color: #4168e0; // 野火IM蓝
      box-shadow: 0 0 8px rgba(65, 104, 224, 0.5);
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

// 图片切换动画
.image-fade-enter-active,
.image-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.image-fade-enter-from {
  opacity: 0;
  transform: scale(0.95);
}

.image-fade-leave-to {
  opacity: 0;
  transform: scale(1.05);
}
</style>
