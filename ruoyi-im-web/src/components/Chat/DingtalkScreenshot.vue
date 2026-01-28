<template>
  <teleport to="body">
    <!-- 截图编辑层 -->
    <div v-if="capturing" class="dingtalk-screenshot"
      @mousedown="handleMouseDown"
      @mousemove="handleMouseMove"
      @mouseup="handleMouseUp"
      @contextmenu.prevent>
      <!-- 截图画面 -->
      <canvas ref="canvasRef" class="screenshot-canvas"></canvas>

      <!-- 选区遮罩 -->
      <div class="screenshot-mask" :style="maskClipPath"></div>

      <!-- 选区边框 -->
      <div v-if="hasSelection" class="selection-border" :style="selectionStyle">
        <!-- 尺寸显示 -->
        <div class="size-tooltip">{{ selection.width }} × {{ selection.height }}</div>

        <!-- 工具栏 -->
        <div class="screenshot-toolbar" :style="toolbarPosition">
          <button class="tool-item" @click.stop="handleCancel" title="取消">
            <svg viewBox="0 0 24 24"><path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
          </button>
          <button class="tool-item tool-save" @click.stop="handleConfirm" title="完成">
            <svg viewBox="0 0 24 24"><path fill="currentColor" d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
          </button>
        </div>
      </div>

      <!-- 提示 -->
      <div class="screenshot-hint" v-if="!hasSelection">
        拖拽鼠标选择截图区域 · ESC 取消
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['confirm', 'close'])

const canvasRef = ref(null)
const capturing = ref(false)
const screenshotImage = ref(null)

// 选区状态
const selection = ref({ x: 0, y: 0, width: 0, height: 0 })
const isSelecting = ref(false)
const startPos = ref({ x: 0, y: 0 })

const hasSelection = computed(() => selection.value.width > 0 && selection.value.height > 0)

const selectionStyle = computed(() => {
  const { x, y, width, height } = selection.value
  const left = Math.min(x, x + width)
  const top = Math.min(y, y + height)
  const w = Math.abs(width)
  const h = Math.abs(height)
  return { left: left + 'px', top: top + 'px', width: w + 'px', height: h + 'px' }
})

const toolbarPosition = computed(() => {
  const { x, y, width, height } = selection.value
  const left = Math.min(x, x + width)
  const top = Math.min(y, y + height)
  const h = Math.abs(height)

  return {
    left: left + 'px',
    top: (top + h + 10) + 'px'
  }
})

const maskClipPath = computed(() => {
  const { x, y, width, height } = selection.value
  const left = Math.min(x, x + width)
  const top = Math.min(y, y + height)
  const w = Math.abs(width)
  const h = Math.abs(height)

  // 使用 clip-path 创建镂空效果
  return {
    clipPath: `polygon(
      0 0, 0 100%, ${left}px 100%, ${left}px ${top}px,
      ${left + w}px ${top}px, ${left + w}px ${top + h}px,
      ${left}px ${top + h}px, ${left}px 100%, 100% 100%, 100% 0
    )`
  }
})

// 开始截图
const startCapture = async () => {
  try {
    // 请求屏幕共享
    const stream = await navigator.mediaDevices.getDisplayMedia({
      video: {
        cursor: 'never',
        frameRate: 60
      },
      audio: false
    })

    const video = document.createElement('video')
    video.srcObject = stream
    await video.play()

    // 等待画面加载
    await new Promise(resolve => {
      video.onloadedmetadata = () => {
        requestAnimationFrame(() => {
          requestAnimationFrame(resolve)
        })
      }
    })

    // 创建全屏画布
    const canvas = document.createElement('canvas')
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight

    const ctx = canvas.getContext('2d')

    // 计算缩放，让截图适配屏幕
    const scaleX = canvas.width / video.videoWidth
    const scaleY = canvas.height / video.videoHeight
    const scale = Math.min(scaleX, scaleY)

    const w = video.videoWidth * scale
    const h = video.videoHeight * scale
    const x = (canvas.width - w) / 2
    const y = (canvas.height - h) / 2

    // 填充黑色背景
    ctx.fillStyle = '#000'
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    // 绘制截图
    ctx.drawImage(video, x, y, w, h)

    // 停止视频流
    stream.getTracks().forEach(t => t.stop())

    // 保存图片数据
    const img = new Image()
    img.onload = () => {
      screenshotImage.value = img
      capturing.value = true

      nextTick(() => {
        initCanvas(img, x, y, w, h)
      })
    }
    img.src = canvas.toDataURL('image/png')

  } catch (err) {
    if (err.name === 'NotAllowedError') {
      ElMessage.info('已取消截图')
    } else {
      ElMessage.error('截图失败: ' + err.message)
    }
    emit('close')
  }
}

// 初始化画布
const initCanvas = (img, x, y, w, h) => {
  const canvas = canvasRef.value
  if (!canvas) return

  canvas.width = window.innerWidth
  canvas.height = window.innerHeight

  const ctx = canvas.getContext('2d')
  ctx.fillStyle = '#000'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ctx.drawImage(img, x, y, w, h)

  // 保存偏移信息
  canvas._offsetX = x
  canvas._offsetY = y
  canvas._scale = w / img.width
}

// 鼠标事件
const handleMouseDown = (e) => {
  isSelecting.value = true
  startPos.value = { x: e.clientX, y: e.clientY }
  selection.value = { x: e.clientX, y: e.clientY, width: 0, height: 0 }
}

const handleMouseMove = (e) => {
  if (!isSelecting.value) return

  selection.value = {
    x: startPos.value.x,
    y: startPos.value.y,
    width: e.clientX - startPos.value.x,
    height: e.clientY - startPos.value.y
  }
}

const handleMouseUp = () => {
  isSelecting.value = false
}

// 确认截图
const handleConfirm = () => {
  const canvas = canvasRef.value
  if (!canvas || !hasSelection.value) return

  const { x, y, width, height } = selection.value
  const left = Math.min(x, x + width)
  const top = Math.min(y, y + height)
  const w = Math.abs(width)
  const h = Math.abs(height)

  // 创建输出画布
  const outputCanvas = document.createElement('canvas')
  outputCanvas.width = w
  outputCanvas.height = h
  const ctx = outputCanvas.getContext('2d')

  ctx.drawImage(canvas, left, top, w, h, 0, 0, w, h)

  const dataUrl = outputCanvas.toDataURL('image/png')
  emit('confirm', dataUrl)
  reset()
}

// 取消
const handleCancel = () => {
  reset()
  emit('close')
}

// 重置
const reset = () => {
  capturing.value = false
  selection.value = { x: 0, y: 0, width: 0, height: 0 }
  screenshotImage.value = null
}

// 键盘事件
const handleKeydown = (e) => {
  if (!capturing.value) return
  if (e.key === 'Escape') handleCancel()
  if (e.key === 'Enter' && hasSelection.value) handleConfirm()
}

// 监听 visible
watch(() => props.visible, (val) => {
  if (val) {
    startCapture()
    document.addEventListener('keydown', handleKeydown)
  } else {
    reset()
    document.removeEventListener('keydown', handleKeydown)
  }
})
</script>

<style scoped>
.dingtalk-screenshot {
  position: fixed;
  inset: 0;
  z-index: 10000;
  cursor: crosshair;
  user-select: none;
}

.screenshot-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.screenshot-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  pointer-events: none;
}

.selection-border {
  position: absolute;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.3), 0 4px 20px rgba(0, 0, 0, 0.5);
  pointer-events: none;
}

.size-tooltip {
  position: absolute;
  top: -30px;
  left: 0;
  padding: 4px 10px;
  background: rgba(0, 0, 0, 0.85);
  color: #fff;
  font-size: 12px;
  font-family: monospace;
  border-radius: 4px;
  white-space: nowrap;
}

.screenshot-toolbar {
  position: absolute;
  display: flex;
  gap: 8px;
  padding: 6px;
  background: rgba(30, 30, 30, 0.95);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
  pointer-events: auto;
}

.tool-item {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.tool-item svg {
  width: 20px;
  height: 20px;
  color: #fff;
}

.tool-item:hover {
  background: rgba(255, 255, 255, 0.2);
}

.tool-save {
  background: var(--dt-brand-color, #1677ff);
}

.tool-save:hover {
  opacity: 0.9;
}

.screenshot-hint {
  position: fixed;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  font-size: 14px;
  border-radius: 24px;
  pointer-events: none;
}
</style>
