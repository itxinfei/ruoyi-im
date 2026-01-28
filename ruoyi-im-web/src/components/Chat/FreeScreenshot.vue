<template>
  <teleport to="body">
    <!-- 选择屏幕遮罩 -->
    <div v-if="selecting" class="simple-screenshot-overlay">
      <div class="select-hint">
        <span class="material-icons-outlined">screenshot</span>
        <span>请选择要截取的屏幕或窗口</span>
      </div>
    </div>

    <!-- 截图编辑层 -->
    <div v-else-if="capturing" class="simple-screenshot-layer"
      @mousedown="handleMouseDown"
      @mousemove="handleMouseMove"
      @mouseup="handleMouseUp">
      <!-- 画布 -->
      <canvas ref="canvasRef" class="screenshot-canvas"></canvas>

      <!-- 遮罩（暗化未选中区域） -->
      <div class="screenshot-mask" :style="maskStyle"></div>

      <!-- 选中区域边框 -->
      <div class="selection-box" :style="selectionStyle">
        <div class="selection-border"></div>
        <!-- 尺寸提示 -->
        <div class="size-label">{{ selection.width }} × {{ selection.height }}</div>
        <!-- 操作按钮 -->
        <div class="action-buttons">
          <button class="action-btn cancel-btn" @click.stop="handleCancel">
            <span class="material-icons-outlined">close</span>
            取消 (ESC)
          </button>
          <button class="action-btn confirm-btn" @click.stop="handleConfirm">
            <span class="material-icons-outlined">check</span>
            完成 (Enter)
          </button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['confirm', 'close'])

// 状态
const selecting = ref(false)
const capturing = ref(false)
const canvasRef = ref(null)
const screenshotImage = ref(null)

// 选区
const selection = ref({ x: 0, y: 0, width: 0, height: 0 })
const isDragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const selectionStart = ref({ x: 0, y: 0, width: 0, height: 0 })

// 计算样式
const selectionStyle = computed(() => {
  const { x, y, width, height } = selection.value
  return {
    left: x + 'px',
    top: y + 'px',
    width: width + 'px',
    height: height + 'px'
  }
})

const maskStyle = computed(() => {
  const { x, y, width, height } = selection.value
  return {
    clipPath: `polygon(
      0 0, 0 100%, ${x}px 100%, ${x}px ${y}px,
      ${x + width}px ${y}px, ${x + width}px ${y + height}px,
      ${x}px ${y + height}px, ${x}px 100%, 100% 100%, 100% 0
    )`
  }
})

// 开始截图
const startCapture = async () => {
  selecting.value = true

  try {
    const stream = await navigator.mediaDevices.getDisplayMedia({
      video: { cursor: 'never' },
      audio: false
    })

    const video = document.createElement('video')
    video.srcObject = stream
    video.play()

    await new Promise(resolve => video.onloadedmetadata = resolve)
    await new Promise(resolve => setTimeout(resolve, 100))

    // 创建画布
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    ctx.drawImage(video, 0, 0)

    stream.getTracks().forEach(t => t.stop())

    const dataURL = canvas.toDataURL('image/png')

    const img = new Image()
    img.onload = () => {
      screenshotImage.value = img
      selecting.value = false
      capturing.value = true

      nextTick(() => {
        initCanvas()
        initDefaultSelection()
      })
    }
    img.src = dataURL

  } catch (error) {
    selecting.value = false
    if (error.name === 'NotAllowedError') {
      ElMessage.info('已取消截图')
    } else {
      ElMessage.error('截图失败')
    }
    emit('close')
  }
}

// 初始化画布
const initCanvas = () => {
  const canvas = canvasRef.value
  if (!canvas || !screenshotImage.value) return

  canvas.width = window.innerWidth
  canvas.height = window.innerHeight

  const ctx = canvas.getContext('2d')
  const img = screenshotImage.value

  // 居中显示
  const scale = Math.min(
    canvas.width / img.width,
    canvas.height / img.height,
    1
  )

  const w = img.width * scale
  const h = img.height * scale
  const x = (canvas.width - w) / 2
  const y = (canvas.height - h) / 2

  ctx.drawImage(img, x, y, w, h)

  canvas._scale = scale
  canvas._offsetX = x
  canvas._offsetY = y
}

// 默认选区
const initDefaultSelection = () => {
  const w = 400
  const h = 300
  selection.value = {
    x: (window.innerWidth - w) / 2,
    y: (window.innerHeight - h) / 2,
    width: w,
    height: h
  }
}

// 鼠标事件
const handleMouseDown = (e) => {
  if (e.target.closest('.action-buttons')) return

  isDragging.value = true
  dragStart.value = { x: e.clientX, y: e.clientY }
  selectionStart.value = { ...selection.value }
}

const handleMouseMove = (e) => {
  if (!isDragging.value) return

  const dx = e.clientX - dragStart.value.x
  const dy = e.clientY - dragStart.value.y
  const start = selectionStart.value

  let x = Math.max(0, start.x + dx)
  let y = Math.max(0, start.y + dy)
  let w = start.width
  let h = start.height

  // 边界检查
  if (x + w > window.innerWidth) x = window.innerWidth - w
  if (y + h > window.innerHeight) y = window.innerHeight - h

  selection.value = { x, y, width: w, height: h }
}

const handleMouseUp = () => {
  isDragging.value = false
}

// 确认截图
const handleConfirm = () => {
  if (!canvasRef.value) return

  const { x, y, width, height } = selection.value
  const sourceCanvas = document.createElement('canvas')
  const ctx = sourceCanvas.getContext('2d')

  sourceCanvas.width = width
  sourceCanvas.height = height

  sourceCanvas.drawImage(canvasRef.value, x, y, width, height, 0, 0, width, height)

  const dataURL = sourceCanvas.toDataURL('image/png')
  emit('confirm', dataURL)
  reset()
}

// 取消
const handleCancel = () => {
  reset()
  emit('close')
}

// 重置
const reset = () => {
  selecting.value = false
  capturing.value = false
  selection.value = { x: 0, y: 0, width: 0, height: 0 }
  screenshotImage.value = null
}

// 键盘快捷键
const handleKeydown = (e) => {
  if (!capturing.value) return
  if (e.key === 'Escape') handleCancel()
  if (e.key === 'Enter') handleConfirm()
}

// 监听 visible
watch(() => props.visible, (val) => {
  if (val) {
    startCapture()
  } else {
    reset()
  }
})

// 生命周期
onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// 选择遮罩
.simple-screenshot-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.select-hint {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px 40px;
  background: var(--dt-bg-card);
  border-radius: 12px;
  color: var(--dt-text-primary);
  font-size: 16px;

  .material-icons-outlined {
    font-size: 32px;
    color: var(--dt-brand-color);
  }
}

// 截图层
.simple-screenshot-layer {
  position: fixed;
  inset: 0;
  z-index: 10000;
  background: #1a1a1a;
  cursor: move;
  user-select: none;
}

.screenshot-canvas {
  position: absolute;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

// 遮罩
.screenshot-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  pointer-events: none;
}

// 选中区域
.selection-box {
  position: absolute;
  pointer-events: none;
}

.selection-border {
  position: absolute;
  inset: 0;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.3), 0 4px 20px rgba(0, 0, 0, 0.3);
}

// 尺寸标签
.size-label {
  position: absolute;
  top: -30px;
  left: 0;
  padding: 4px 10px;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  font-size: 12px;
  font-family: monospace;
  border-radius: 4px;
  white-space: nowrap;
}

// 操作按钮
.action-buttons {
  position: absolute;
  bottom: -50px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
  pointer-events: auto;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  .material-icons-outlined {
    font-size: 18px;
  }

  &.cancel-btn {
    background: rgba(255, 255, 255, 0.1);
    color: #fff;

    &:hover {
      background: rgba(245, 108, 108, 0.3);
    }
  }

  &.confirm-btn {
    background: var(--dt-brand-color);
    color: #fff;

    &:hover {
      opacity: 0.9;
    }
  }
}
</style>
