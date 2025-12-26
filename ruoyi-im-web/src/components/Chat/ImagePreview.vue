<template>
  <teleport to="body">
    <transition name="preview-fade">
      <div v-if="visible" class="image-preview-overlay" @click.self="close">
        <div class="preview-container">
          <!-- 关闭按钮 -->
          <button class="preview-close" @click="close" aria-label="关闭预览">
            <i class="el-icon-close"></i>
          </button>

          <!-- 图片计数器 -->
          <div v-if="images.length > 1" class="preview-counter">
            {{ currentIndex + 1 }} / {{ images.length }}
          </div>

          <!-- 工具栏 -->
          <div class="preview-toolbar">
            <button class="toolbar-btn" @click="zoomIn" title="放大">
              <i class="el-icon-zoom-in"></i>
            </button>
            <button class="toolbar-btn" @click="zoomOut" title="缩小">
              <i class="el-icon-zoom-out"></i>
            </button>
            <button class="toolbar-btn" @click="resetZoom" title="重置">
              <i class="el-icon-refresh"></i>
            </button>
            <button class="toolbar-btn" @click="rotateLeft" title="左旋转">
              <i class="el-icon-refresh-left"></i>
            </button>
            <button class="toolbar-btn" @click="rotateRight" title="右旋转">
              <i class="el-icon-refresh-right"></i>
            </button>
            <button class="toolbar-btn" @click="downloadImage" title="下载">
              <i class="el-icon-download"></i>
            </button>
          </div>

          <!-- 主图片区域 -->
          <div
            class="preview-main"
            ref="previewMain"
            @wheel.prevent="handleWheel"
            @mousedown="startDrag"
            @touchstart="handleTouchStart"
            @touchmove="handleTouchMove"
            @touchend="handleTouchEnd"
          >
            <transition :name="slideDirection" mode="out-in">
              <img
                :key="currentImage"
                :src="currentImage"
                :style="imageStyle"
                class="preview-image"
                @load="handleImageLoad"
                alt="预览图片"
                draggable="false"
              />
            </transition>
          </div>

          <!-- 左右切换按钮 -->
          <template v-if="images.length > 1">
            <button
              class="preview-nav prev"
              :class="{ disabled: currentIndex === 0 }"
              @click="prevImage"
              :disabled="currentIndex === 0"
              aria-label="上一张"
            >
              <i class="el-icon-arrow-left"></i>
            </button>
            <button
              class="preview-nav next"
              :class="{ disabled: currentIndex === images.length - 1 }"
              @click="nextImage"
              :disabled="currentIndex === images.length - 1"
              aria-label="下一张"
            >
              <i class="el-icon-arrow-right"></i>
            </button>
          </template>

          <!-- 缩略图列表 -->
          <div v-if="images.length > 1" class="preview-thumbnails">
            <div class="thumbnails-wrapper">
              <div
                v-for="(img, index) in images"
                :key="index"
                class="thumbnail-item"
                :class="{ active: currentIndex === index }"
                @click="goToImage(index)"
              >
                <img :src="img" alt="缩略图" />
              </div>
            </div>
          </div>

          <!-- 加载指示器 -->
          <transition name="loading-fade">
            <div v-if="loading" class="preview-loading">
              <i class="el-icon-loading"></i>
              <span>加载中...</span>
            </div>
          </transition>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  images: {
    type: Array,
    default: () => [],
  },
  initialIndex: {
    type: Number,
    default: 0,
  },
})

const emit = defineEmits(['close', 'update:visible'])

// 响应式数据
const currentIndex = ref(0)
const scale = ref(1)
const rotation = ref(0)
const translateX = ref(0)
const translateY = ref(0)
const loading = ref(false)
const previewMain = ref(null)
const slideDirection = ref('slide-right')

// 拖拽状态
const isDragging = ref(false)
const dragStartX = ref(0)
const dragStartY = ref(0)
const lastTranslateX = ref(0)
const lastTranslateY = ref(0)

// 触摸状态
const touchStartDistance = ref(0)
const touchStartScale = ref(1)

// 计算属性
const currentImage = computed(() => props.images[currentIndex.value] || '')

const imageStyle = computed(() => ({
  transform: `translate(${translateX.value}px, ${translateY.value}px) scale(${scale.value}) rotate(${rotation.value}deg)`,
  cursor: isDragging.value ? 'grabbing' : scale.value > 1 ? 'grab' : 'default',
}))

// 监听器
watch(
  () => props.visible,
  newVal => {
    if (newVal) {
      currentIndex.value = props.initialIndex
      resetTransform()
      document.body.style.overflow = 'hidden'
      document.addEventListener('keydown', handleKeydown)
    } else {
      document.body.style.overflow = ''
      document.removeEventListener('keydown', handleKeydown)
    }
  }
)

watch(
  () => props.initialIndex,
  newVal => {
    if (props.visible) {
      currentIndex.value = newVal
    }
  }
)

// 方法
const close = () => {
  emit('update:visible', false)
  emit('close')
}

const resetTransform = () => {
  scale.value = 1
  rotation.value = 0
  translateX.value = 0
  translateY.value = 0
}

const zoomIn = () => {
  if (scale.value < 5) {
    scale.value = Math.min(scale.value + 0.5, 5)
  }
}

const zoomOut = () => {
  if (scale.value > 0.5) {
    scale.value = Math.max(scale.value - 0.5, 0.5)
    if (scale.value <= 1) {
      translateX.value = 0
      translateY.value = 0
    }
  }
}

const resetZoom = () => {
  resetTransform()
}

const rotateLeft = () => {
  rotation.value -= 90
}

const rotateRight = () => {
  rotation.value += 90
}

const downloadImage = async () => {
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
    ElMessage.success('图片下载成功')
  } catch (error) {
    ElMessage.error('图片下载失败')
  }
}

const prevImage = () => {
  if (currentIndex.value > 0) {
    slideDirection.value = 'slide-left'
    currentIndex.value--
    resetTransform()
  }
}

const nextImage = () => {
  if (currentIndex.value < props.images.length - 1) {
    slideDirection.value = 'slide-right'
    currentIndex.value++
    resetTransform()
  }
}

const goToImage = index => {
  if (index !== currentIndex.value) {
    slideDirection.value = index > currentIndex.value ? 'slide-right' : 'slide-left'
    currentIndex.value = index
    resetTransform()
  }
}

const handleWheel = event => {
  const delta = event.deltaY > 0 ? -0.1 : 0.1
  const newScale = Math.max(0.5, Math.min(5, scale.value + delta))
  scale.value = newScale

  if (newScale <= 1) {
    translateX.value = 0
    translateY.value = 0
  }
}

const startDrag = event => {
  if (scale.value <= 1) return

  isDragging.value = true
  dragStartX.value = event.clientX
  dragStartY.value = event.clientY
  lastTranslateX.value = translateX.value
  lastTranslateY.value = translateY.value

  document.addEventListener('mousemove', handleDrag)
  document.addEventListener('mouseup', stopDrag)
}

const handleDrag = event => {
  if (!isDragging.value) return

  const deltaX = event.clientX - dragStartX.value
  const deltaY = event.clientY - dragStartY.value

  translateX.value = lastTranslateX.value + deltaX
  translateY.value = lastTranslateY.value + deltaY
}

const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
}

const handleTouchStart = event => {
  if (event.touches.length === 2) {
    // 双指缩放
    const touch1 = event.touches[0]
    const touch2 = event.touches[1]
    touchStartDistance.value = Math.hypot(
      touch2.clientX - touch1.clientX,
      touch2.clientY - touch1.clientY
    )
    touchStartScale.value = scale.value
  } else if (event.touches.length === 1 && scale.value > 1) {
    // 单指拖拽
    isDragging.value = true
    dragStartX.value = event.touches[0].clientX
    dragStartY.value = event.touches[0].clientY
    lastTranslateX.value = translateX.value
    lastTranslateY.value = translateY.value
  }
}

const handleTouchMove = event => {
  if (event.touches.length === 2) {
    const touch1 = event.touches[0]
    const touch2 = event.touches[1]
    const currentDistance = Math.hypot(
      touch2.clientX - touch1.clientX,
      touch2.clientY - touch1.clientY
    )
    const scaleChange = currentDistance / touchStartDistance.value
    scale.value = Math.max(0.5, Math.min(5, touchStartScale.value * scaleChange))
  } else if (event.touches.length === 1 && isDragging.value) {
    const deltaX = event.touches[0].clientX - dragStartX.value
    const deltaY = event.touches[0].clientY - dragStartY.value
    translateX.value = lastTranslateX.value + deltaX
    translateY.value = lastTranslateY.value + deltaY
  }
}

const handleTouchEnd = () => {
  isDragging.value = false
  if (scale.value <= 1) {
    translateX.value = 0
    translateY.value = 0
  }
}

const handleKeydown = event => {
  switch (event.key) {
    case 'Escape':
      close()
      break
    case 'ArrowLeft':
      prevImage()
      break
    case 'ArrowRight':
      nextImage()
      break
    case '+':
    case '=':
      zoomIn()
      break
    case '-':
      zoomOut()
      break
    case '0':
      resetZoom()
      break
  }
}

const handleImageLoad = () => {
  loading.value = false
}

watch(currentImage, () => {
  loading.value = true
})

// 生命周期
onMounted(() => {
  if (props.visible) {
    document.body.style.overflow = 'hidden'
    document.addEventListener('keydown', handleKeydown)
  }
})

onUnmounted(() => {
  document.body.style.overflow = ''
  document.removeEventListener('keydown', handleKeydown)
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
})
</script>

<style lang="scss" scoped>
.image-preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-container {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-close {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 44px;
  height: 44px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  color: white;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: rotate(90deg);
  }
}

.preview-counter {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  font-size: 14px;
  background: rgba(0, 0, 0, 0.5);
  padding: 6px 16px;
  border-radius: 20px;
  z-index: 10;
}

.preview-toolbar {
  position: absolute;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  background: rgba(0, 0, 0, 0.6);
  padding: 8px 16px;
  border-radius: 24px;
  z-index: 10;

  .toolbar-btn {
    width: 40px;
    height: 40px;
    border: none;
    background: transparent;
    color: white;
    font-size: 18px;
    cursor: pointer;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.15);
      transform: scale(1.1);
    }

    &:active {
      transform: scale(0.95);
    }
  }
}

.preview-main {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  user-select: none;
}

.preview-image {
  max-width: 90%;
  max-height: 80%;
  object-fit: contain;
  transition: transform 0.1s ease;
}

.preview-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 50px;
  height: 50px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  color: white;
  font-size: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10;

  &.prev {
    left: 20px;
  }

  &.next {
    right: 20px;
  }

  &:hover:not(.disabled) {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-50%) scale(1.1);
  }

  &.disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
}

.preview-thumbnails {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  max-width: 80%;
  z-index: 10;

  .thumbnails-wrapper {
    display: flex;
    gap: 8px;
    padding: 10px;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 12px;
    overflow-x: auto;

    &::-webkit-scrollbar {
      height: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.3);
      border-radius: 2px;
    }
  }

  .thumbnail-item {
    width: 60px;
    height: 60px;
    flex-shrink: 0;
    border-radius: 6px;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.3s ease;
    opacity: 0.6;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    &:hover {
      opacity: 0.9;
    }

    &.active {
      border-color: #1890ff;
      opacity: 1;
    }
  }
}

.preview-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: white;

  i {
    font-size: 32px;
    animation: spin 1s linear infinite;
  }

  span {
    font-size: 14px;
  }
}

// 动画
.preview-fade-enter-active,
.preview-fade-leave-active {
  transition: all 0.3s ease;
}

.preview-fade-enter-from,
.preview-fade-leave-to {
  opacity: 0;
}

.preview-fade-enter-from .preview-image {
  transform: scale(0.9);
}

.slide-right-enter-active,
.slide-right-leave-active,
.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.3s ease;
}

.slide-right-enter-from {
  opacity: 0;
  transform: translateX(50px);
}

.slide-right-leave-to {
  opacity: 0;
  transform: translateX(-50px);
}

.slide-left-enter-from {
  opacity: 0;
  transform: translateX(-50px);
}

.slide-left-leave-to {
  opacity: 0;
  transform: translateX(50px);
}

.loading-fade-enter-active,
.loading-fade-leave-active {
  transition: opacity 0.3s ease;
}

.loading-fade-enter-from,
.loading-fade-leave-to {
  opacity: 0;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .preview-close {
    top: 10px;
    right: 10px;
    width: 40px;
    height: 40px;
  }

  .preview-toolbar {
    bottom: 80px;
    padding: 6px 12px;

    .toolbar-btn {
      width: 36px;
      height: 36px;
      font-size: 16px;
    }
  }

  .preview-nav {
    width: 40px;
    height: 40px;
    font-size: 20px;

    &.prev {
      left: 10px;
    }

    &.next {
      right: 10px;
    }
  }

  .preview-thumbnails {
    bottom: 10px;

    .thumbnail-item {
      width: 50px;
      height: 50px;
    }
  }
}
</style>
