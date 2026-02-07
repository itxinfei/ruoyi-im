<template>
  <teleport to="body">
    <!-- 桌面贴图层 -->
    <div
      v-for="sticker in stickers"
      :key="sticker.id"
      class="desktop-sticker"
      :class="{ minimized: sticker.minimized }"
      :style="{ left: sticker.x + 'px', top: sticker.y + 'px', zIndex: sticker.zIndex }"
      @mousedown="handleStickerMouseDown($event, sticker.id)"
    >
      <!-- 标题栏 -->
      <div
        class="sticker-header"
        @mousedown="startDrag($event, sticker.id)"
      >
        <span class="sticker-title">截图贴图</span>
        <div class="sticker-controls">
          <button
            class="control-btn"
            title="最小化"
            @click.stop="toggleMinimize(sticker.id)"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M19 13H5v-2h14v2z"
            /></svg>
          </button>
          <button
            class="control-btn"
            title="置顶"
            @click.stop="toggleTop(sticker.id)"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M2 20h20v-4H2v4zm2-3h2v2H4v-2zM2 4v4h20V4H2zm4 3H4V5h2v2zm-4 7h20v-4H2v4zm2-3h2v2H4v-2z"
            /></svg>
          </button>
          <button
            class="control-btn close-btn"
            title="关闭"
            @click.stop="removeSticker(sticker.id)"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
            /></svg>
          </button>
        </div>
      </div>

      <!-- 图片内容 -->
      <div
        v-if="!sticker.minimized"
        class="sticker-content"
      >
        <img
          :src="sticker.image"
          :alt="'贴图 ' + sticker.id"
          draggable="false"
        >
      </div>

      <!-- 操作提示 -->
      <div
        v-if="stickers.length === 1 && !stickers[0].minimized"
        class="sticker-hint"
      >
        双击贴图可复制 · 拖动标题栏移动
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// ==================== 状态管理 ====================
const stickers = ref([])
let nextZIndex = 10000
let stickerIdCounter = 0

// ==================== 拖拽状态 ====================
const dragState = ref({
  isDragging: false,
  stickerId: null,
  offsetX: 0,
  offsetY: 0
})

// ==================== 方法：添加贴图 ====================
const addSticker = (imageDataUrl, x = 100, y = 100) => {
  const screenCenterX = window.innerWidth / 2
  const screenCenterY = window.innerHeight / 2

  // 加载图片获取实际尺寸
  const img = new Image()
  img.onload = () => {
    const sticker = {
      id: ++stickerIdCounter,
      image: imageDataUrl,
      x: screenCenterX - img.width / 4,
      y: screenCenterY - img.height / 4,
      width: img.width,
      height: img.height,
      minimized: false,
      zIndex: ++nextZIndex
    }

    // 创建新数组触发响应式更新
    stickers.value = [...stickers.value, sticker]
  }
  img.src = imageDataUrl
}

// ==================== 方法：移除贴图 ====================
const removeSticker = id => {
  const index = stickers.value.findIndex(s => s.id === id)
  if (index !== -1) {
    // 创建新数组触发响应式更新
    stickers.value = [...stickers.value.slice(0, index), ...stickers.value.slice(index + 1)]
  }
}

// ==================== 方法：最小化切换 ====================
const toggleMinimize = id => {
  const sticker = stickers.value.find(s => s.id === id)
  if (sticker) {
    sticker.minimized = !sticker.minimized
  }
}

// ==================== 方法：置顶切换 ====================
const toggleTop = id => {
  const sticker = stickers.value.find(s => s.id === id)
  if (sticker) {
    sticker.zIndex = ++nextZIndex
  }
}

// ==================== 方法：开始拖拽 ====================
const startDrag = (e, id) => {
  const sticker = stickers.value.find(s => s.id === id)
  if (!sticker) {return}

  // 提升到最顶层
  sticker.zIndex = ++nextZIndex

  dragState.value = {
    isDragging: true,
    stickerId: id,
    offsetX: e.clientX - sticker.x,
    offsetY: e.clientY - sticker.y
  }

  document.addEventListener('mousemove', handleDragMove)
  document.addEventListener('mouseup', handleDragEnd)
}

// ==================== 方法：处理拖拽移动 ====================
const handleDragMove = e => {
  if (!dragState.value.isDragging) {return}

  const sticker = stickers.value.find(s => s.id === dragState.value.stickerId)
  if (!sticker) {return}

  sticker.x = e.clientX - dragState.value.offsetX
  sticker.y = e.clientY - dragState.value.offsetY

  // 边界限制
  sticker.x = Math.max(0, Math.min(window.innerWidth - 50, sticker.x))
  sticker.y = Math.max(0, Math.min(window.innerHeight - 50, sticker.y))
}

// ==================== 方法：结束拖拽 ====================
const handleDragEnd = () => {
  dragState.value.isDragging = false
  dragState.value.stickerId = null
  document.removeEventListener('mousemove', handleDragMove)
  document.removeEventListener('mouseup', handleDragEnd)
}

// ==================== 方法：激活贴图 ====================
const handleStickerMouseDown = (e, id) => {
  const sticker = stickers.value.find(s => s.id === id)
  if (sticker) {
    sticker.zIndex = ++nextZIndex
  }
}

// ==================== 方法：双击复制贴图 ====================
const handleDoubleClick = (e, id) => {
  const sticker = stickers.value.find(s => s.id === id)
  if (sticker) {
    addSticker(sticker.image, sticker.x + 20, sticker.y + 20)
  }
}

// 保存双击事件处理器引用，用于后续清理
let handleDoubleClickWrapper = null

// ==================== 暴露方法 ====================
defineExpose({
  addSticker,
  removeSticker,
  clear: () => { stickers.value = [] }
})

// ==================== 生命周期 ====================
onMounted(() => {
  // 监听双击事件复制贴图
  handleDoubleClickWrapper = e => {
    const stickerEl = e.target.closest('.desktop-sticker')
    if (stickerEl) {
      const id = parseInt(stickerEl.dataset.id)
      handleDoubleClick(e, id)
    }
  }
  document.addEventListener('dblclick', handleDoubleClickWrapper)
})

onUnmounted(() => {
  // 清理所有事件监听器
  document.removeEventListener('mousemove', handleDragMove)
  document.removeEventListener('mouseup', handleDragEnd)
  if (handleDoubleClickWrapper) {
    document.removeEventListener('dblclick', handleDoubleClickWrapper)
    handleDoubleClickWrapper = null
  }
})
</script>

<style scoped>
/* ==================== 贴图容器 ==================== */
.desktop-sticker {
  position: fixed;
  min-width: 200px;
  background: var(--dt-surface-elevated);
  border-radius: var(--dt-radius-md);
  box-shadow: var(--dt-shadow-md);
  overflow: hidden;
  user-select: none;
  transition: box-shadow 0.2s;
}

.desktop-sticker:hover {
  box-shadow: var(--dt-shadow-lg);
}

.desktop-sticker.minimized {
  min-width: 150px;
}

/* ==================== 标题栏 ==================== */
.sticker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: var(--dt-surface-overlay);
  cursor: move;
  border-bottom: 1px solid var(--dt-border-color);
}

.sticker-title {
  font-size: 12px;
  color: var(--dt-text-secondary);
  font-weight: 500;
}

.sticker-controls {
  display: flex;
  gap: 4px;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.desktop-sticker:hover .sticker-controls {
  opacity: 1;
}

.control-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-sm);
  cursor: pointer;
  transition: all 0.2s;
  color: var(--dt-icon-secondary);
}

.control-btn:hover {
  background: var(--dt-hover-bg);
  color: var(--dt-text-primary);
}

.control-btn.close-btn:hover {
  background: var(--dt-danger-bg-hover);
  color: var(--dt-danger-primary);
}

.control-btn svg {
  width: 16px;
  height: 16px;
}

/* ==================== 内容区 ==================== */
.sticker-content {
  padding: 8px;
  background: var(--dt-surface-base);
}

.sticker-content img {
  max-width: 400px;
  max-height: 300px;
  display: block;
  border-radius: var(--dt-radius-sm);
}

.desktop-sticker.minimized .sticker-content {
  display: none;
}

/* ==================== 提示 ==================== */
.sticker-hint {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  padding: 4px 12px;
  background: var(--dt-surface-overlay);
  color: var(--dt-text-secondary);
  font-size: 11px;
  border-radius: var(--dt-radius-lg);
  pointer-events: none;
  white-space: nowrap;
}
</style>
