<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="image-preview-overlay" @click.self="handleClose">
        <!-- 顶部工具栏 -->
        <div class="preview-toolbar">
          <div class="toolbar-left">
            <span class="image-counter">{{ currentIndex + 1 }} / {{ imageList.length }}</span>
          </div>
          <div class="toolbar-center">
            <el-tooltip content="缩小" placement="bottom">
              <button class="toolbar-btn" @click="zoomOut">
                <el-icon><ZoomOut /></el-icon>
              </button>
            </el-tooltip>
            <span class="zoom-level">{{ Math.round(scale * 100) }}%</span>
            <el-tooltip content="放大" placement="bottom">
              <button class="toolbar-btn" @click="zoomIn">
                <el-icon><ZoomIn /></el-icon>
              </button>
            </el-tooltip>
            <el-divider direction="vertical" />
            <el-tooltip content="重置" placement="bottom">
              <button class="toolbar-btn" @click="resetZoom">
                <el-icon><RefreshRight /></el-icon>
              </button>
            </el-tooltip>
            <el-tooltip content="适应窗口" placement="bottom">
              <button class="toolbar-btn" @click="fitToWindow">
                <el-icon><FullScreen /></el-icon>
              </button>
            </el-tooltip>
          </div>
          <div class="toolbar-right">
            <el-tooltip content="下载" placement="bottom">
              <button class="toolbar-btn" @click="downloadImage">
                <el-icon><Download /></el-icon>
              </button>
            </el-tooltip>
            <el-divider direction="vertical" />
            <el-tooltip content="关闭 (Esc)" placement="bottom">
              <button class="toolbar-btn close-btn" @click="handleClose">
                <el-icon><Close /></el-icon>
              </button>
            </el-tooltip>
          </div>
        </div>

        <!-- 图片容器 -->
        <div
          ref="containerRef"
          class="preview-container"
          @wheel.prevent="handleWheel"
          @mousedown="startDrag"
        >
          <img
            ref="imageRef"
            :src="currentImage"
            :style="imageStyle"
            class="preview-image"
            draggable="false"
            @load="onImageLoad"
            @error="onImageError"
          >
        </div>

        <!-- 左右切换按钮 -->
        <button
          v-if="imageList.length > 1"
          class="nav-btn prev-btn"
          :class="{ disabled: currentIndex === 0 }"
          @click="prevImage"
        >
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <button
          v-if="imageList.length > 1"
          class="nav-btn next-btn"
          :class="{ disabled: currentIndex === imageList.length - 1 }"
          @click="nextImage"
        >
          <el-icon><ArrowRight /></el-icon>
        </button>

        <!-- 缩略图列表 -->
        <div v-if="imageList.length > 1" class="thumbnail-bar">
          <div class="thumbnail-list">
            <div
              v-for="(img, index) in imageList"
              :key="img || `img-${index}`"
              class="thumbnail-item"
              :class="{ active: index === currentIndex }"
              @click="goToImage(index)"
            >
              <img :src="img" class="thumbnail-img">
            </div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-overlay">
          <el-icon class="is-loading loading-icon">
            <Loading />
          </el-icon>
        </div>

        <!-- 错误状态 -->
        <div v-if="error" class="error-overlay">
          <el-icon class="error-icon">
            <PictureFilled />
          </el-icon>
          <span>图片加载失败</span>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import {
  Close, Download, ZoomIn, ZoomOut, RefreshRight,
  FullScreen, ArrowLeft, ArrowRight, Loading, PictureFilled
} from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  imageList: {
    type: Array,
    default: () => []
  },
  initialIndex: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:visible', 'close'])

// 状态
const currentIndex = ref(0)
const scale = ref(1)
const rotation = ref(0)
const translateX = ref(0)
const translateY = ref(0)
const loading = ref(false)
const error = ref(false)

// 拖拽状态
const isDragging = ref(false)
const dragStartX = ref(0)
const dragStartY = ref(0)
const dragStartTranslateX = ref(0)
const dragStartTranslateY = ref(0)

// refs
const containerRef = ref(null)
const imageRef = ref(null)

// 计算属性
const currentImage = computed(() => {
  return props.imageList[currentIndex.value] || ''
})

const imageStyle = computed(() => ({
  transform: `translate(${translateX.value}px, ${translateY.value}px) scale(${scale.value}) rotate(${rotation.value}deg)`,
  transition: isDragging.value ? 'none' : 'transform 0.2s ease-out'
}))

// 方法
const zoomIn = () => {
  if (scale.value < 5) {
    scale.value = Math.min(5, scale.value + 0.25)
  }
}

const zoomOut = () => {
  if (scale.value > 0.1) {
    scale.value = Math.max(0.1, scale.value - 0.25)
  }
}

const resetZoom = () => {
  scale.value = 1
  translateX.value = 0
  translateY.value = 0
  rotation.value = 0
}

const fitToWindow = () => {
  if (!imageRef.value || !containerRef.value) return

  const containerWidth = containerRef.value.clientWidth
  const containerHeight = containerRef.value.clientHeight
  const imgWidth = imageRef.value.naturalWidth
  const imgHeight = imageRef.value.naturalHeight

  const scaleX = (containerWidth * 0.9) / imgWidth
  const scaleY = (containerHeight * 0.9) / imgHeight

  scale.value = Math.min(scaleX, scaleY, 1)
  translateX.value = 0
  translateY.value = 0
}

const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    resetZoom()
    loadImage()
  }
}

const nextImage = () => {
  if (currentIndex.value < props.imageList.length - 1) {
    currentIndex.value++
    resetZoom()
    loadImage()
  }
}

const goToImage = (index) => {
  currentIndex.value = index
  resetZoom()
  loadImage()
}

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

const handleWheel = (e) => {
  e.preventDefault()
  if (e.deltaY < 0) {
    zoomIn()
  } else {
    zoomOut()
  }
}

const startDrag = (e) => {
  if (e.button !== 0) return
  isDragging.value = true
  dragStartX.value = e.clientX
  dragStartY.value = e.clientY
  dragStartTranslateX.value = translateX.value
  dragStartTranslateY.value = translateY.value

  document.addEventListener('mousemove', handleDrag)
  document.addEventListener('mouseup', stopDrag)
}

const handleDrag = (e) => {
  if (!isDragging.value) return

  const deltaX = e.clientX - dragStartX.value
  const deltaY = e.clientY - dragStartY.value

  translateX.value = dragStartTranslateX.value + deltaX
  translateY.value = dragStartTranslateY.value + deltaY
}

const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
}

const downloadImage = async () => {
  if (!currentImage.value) return

  try {
    const response = await fetch(currentImage.value)
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = url
    link.download = `image_${Date.now()}.jpg`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } catch (err) {
    console.error('下载失败:', err)
  }
}

const loadImage = () => {
  loading.value = true
  error.value = false
}

const onImageLoad = () => {
  loading.value = false
  error.value = false
}

const onImageError = () => {
  loading.value = false
  error.value = true
}

// 键盘事件
const handleKeydown = (e) => {
  if (!props.visible) return

  switch (e.key) {
    case 'Escape':
      handleClose()
      break
    case 'ArrowLeft':
      prevImage()
      break
    case 'ArrowRight':
      nextImage()
      break
    case 'ArrowUp':
      zoomIn()
      break
    case 'ArrowDown':
      zoomOut()
      break
    case ' ':
      e.preventDefault()
      resetZoom()
      break
  }
}

// 监听
watch(() => props.visible, (val) => {
  if (val) {
    currentIndex.value = props.initialIndex
    resetZoom()
    loadImage()
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

watch(() => props.initialIndex, (val) => {
  currentIndex.value = val
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.body.style.overflow = ''
})
</script>

<style scoped lang="scss">
// ============================================================================
// 遮罩层
// ============================================================================
.image-preview-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  flex-direction: column;
  user-select: none;
}

// ============================================================================
// 工具栏
// ============================================================================
.preview-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: rgba(0, 0, 0, 0.5);
  color: var(--dt-text-white);
  z-index: 10;

  .toolbar-left,
  .toolbar-center,
  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .image-counter {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
  }

  .zoom-level {
    min-width: 50px;
    text-align: center;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.8);
  }

  .toolbar-btn {
    width: 36px;
    height: 36px;
    border: none;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    color: #fff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
    }

    &.close-btn:hover {
      background: var(--dt-error-color);
    }

    .el-icon {
      font-size: 18px;
    }
  }

  .el-divider--vertical {
    height: 20px;
    border-color: rgba(255, 255, 255, 0.2);
  }
}

// ============================================================================
// 图片容器
// ============================================================================
.preview-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: grab;

  &:active {
    cursor: grabbing;
  }
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: transform 0.2s ease-out;
}

// ============================================================================
// 导航按钮
// ============================================================================
.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  z-index: 10;

  &:hover:not(.disabled) {
    background: rgba(255, 255, 255, 0.2);
  }

  &.disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }

  .el-icon {
    font-size: 24px;
  }
}

.prev-btn {
  left: 24px;
}

.next-btn {
  right: 24px;
}

// ============================================================================
// 缩略图栏
// ============================================================================
.thumbnail-bar {
  padding: 12px 24px;
  background: rgba(0, 0, 0, 0.5);
  z-index: 10;
}

.thumbnail-list {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.thumbnail-item {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
  opacity: 0.6;

  &:hover {
    opacity: 0.9;
  }

  &.active {
    border-color: var(--dt-brand-color);
    opacity: 1;
  }
}

.thumbnail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

// ============================================================================
// 加载和错误状态
// ============================================================================
.loading-overlay,
.error-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #fff;
  z-index: 5;
}

.loading-icon {
  font-size: 48px;
  color: var(--dt-brand-color);
}

.error-icon {
  font-size: 48px;
  color: rgba(255, 255, 255, 0.5);
}

// ============================================================================
// 动画
// ============================================================================
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
