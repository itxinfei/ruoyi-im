<template>
  <teleport to="body">
    <!-- 阶段1: 区域选择层 -->
    <div
      v-if="capturing && !editing"
      class="dingtalk-screenshot"
      @mousedown="handleSelectionMouseDown"
      @mousemove="handleSelectionMouseMove"
      @mouseup="handleSelectionMouseUp"
      @contextmenu.prevent
    >
      <!-- 截图画面 -->
      <canvas
        ref="canvasRef"
        class="screenshot-canvas"
      />

      <!-- 选区遮罩 -->
      <div
        class="screenshot-mask"
        :style="maskClipPath"
      />

      <!-- 选区边框 -->
      <div
        v-if="hasSelection"
        class="selection-border"
        :style="selectionStyle"
      >
        <!-- 尺寸显示 -->
        <div class="size-tooltip">
          {{ selectionWidth }} × {{ selectionHeight }}
        </div>

        <!-- 确认工具栏 -->
        <div
          class="screenshot-toolbar confirm-toolbar"
          :style="toolbarPosition"
        >
          <button
            class="tool-item"
            title="取消 (ESC)"
            @click.stop="handleCancel"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
            /></svg>
          </button>
          <button
            class="tool-item tool-save"
            title="确认选区"
            @click.stop="enterEditMode"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"
            /></svg>
          </button>
        </div>
      </div>

      <!-- 提示 -->
      <div
        v-if="!hasSelection"
        class="screenshot-hint"
      >
        拖拽鼠标选择截图区域 · ESC 取消
      </div>
    </div>

    <!-- 阶段2: 标注编辑层 -->
    <div
      v-if="editing"
      class="screenshot-editor"
      @contextmenu.prevent
    >
      <!-- 编辑画布容器 -->
      <div
        class="editor-canvas-container"
        :style="containerStyle"
      >
        <canvas
          ref="editorCanvasRef"
          class="editor-canvas"
          @mousedown="handleDrawStart"
          @mousemove="handleDrawMove"
          @mouseup="handleDrawEnd"
          @mouseleave="handleDrawEnd"
        />
      </div>

      <!-- 顶部工具栏 -->
      <div class="editor-toolbar">
        <!-- 绘图工具组 -->
        <div class="tool-group">
          <button
            class="tool-btn"
            :class="{ active: currentTool === 'rectangle' }"
            title="方框 (R)"
            @click="setTool('rectangle')"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M3 3h18v18H3V3zm2 2v14h14V5H5z"
            /></svg>
          </button>
          <button
            class="tool-btn"
            :class="{ active: currentTool === 'arrow' }"
            title="箭头 (A)"
            @click="setTool('arrow')"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M16.01 11H4v2h12.01v3L20 12l-3.99-4z"
            /></svg>
          </button>
          <button
            class="tool-btn"
            :class="{ active: currentTool === 'brush' }"
            title="画笔 (P)"
            @click="setTool('brush')"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"
            /></svg>
          </button>
          <button
            class="tool-btn"
            :class="{ active: currentTool === 'text' }"
            title="文字 (T)"
            @click="setTool('text')"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M5 4v3h5.5v12h3V7H19V4z"
            /></svg>
          </button>
          <button
            class="tool-btn"
            :class="{ active: currentTool === 'mosaic' }"
            title="马赛克 (M)"
            @click="setTool('mosaic')"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M4 4h4v4H4V4zm6 0h4v4h-4V4zm6 0h4v4h-4V4zM4 10h4v4H4v-4zm6 0h4v4h-4v-4zm6 0h4v4h-4v-4zM4 16h4v4H4v-4zm6 0h4v4h-4v-4zm6 0h4v4h-4v-4z"
            /></svg>
          </button>
          <button
            class="tool-btn"
            :class="{ active: currentTool === 'eraser' }"
            title="橡皮擦 (E)"
            @click="setTool('eraser')"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M15.14 3c-.51 0-1.02.2-1.41.59L2.59 14.73c-.78.78-.78 2.05 0 2.83L5.17 20.17c.78.78 2.05.78 2.83 0l11.03-11.03c.79-.79.79-2.05 0-2.83l-2.58-2.58C16.07 3.2 15.61 3 15.14 3zM15.5 9l-11 11 2.5 2.5 11-11-2.5-2.5z"
            /></svg>
          </button>
        </div>

        <!-- 分隔线 -->
        <div class="toolbar-divider" />

        <!-- 颜色选择器 -->
        <div class="tool-group">
          <button
            v-for="color in colors"
            :key="color"
            class="color-btn"
            :class="{ active: strokeColor === color }"
            :style="{ backgroundColor: color }"
            :title="color"
            @click="setColor(color)"
          />
          <!-- 自定义颜色输入 -->
          <button
            class="color-btn custom-color"
            title="自定义颜色"
          >
            <input
              v-model="customColor"
              type="color"
              @input="setCustomColor"
            >
          </button>
        </div>

        <!-- 分隔线 -->
        <div class="toolbar-divider" />

        <!-- 线宽调节 -->
        <div class="tool-group line-width-group">
          <span class="line-width-label">粗细</span>
          <input
            v-model.number="lineWidth"
            type="range"
            min="1"
            max="20"
            class="line-width-slider"
          >
          <span class="line-width-value">{{ lineWidth }}</span>
        </div>

        <!-- 分隔线 -->
        <div class="toolbar-divider" />

        <!-- 操作按钮组 -->
        <div class="tool-group">
          <button
            class="tool-btn tool-ocr-btn"
            title="文字识别 (OCR)"
            :disabled="isRecognizing"
            @click="handleOCR"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M4 5h16v2H4V5zm0 4h16v2H4V9zm0 4h10v2H4v-2zm0 4h10v2H4v-2zm12 0h2v-2h-2v2zm0-4h2v-2h-2v2zm0-4h2V7h-2v2z"
            /></svg>
            <span
              v-if="isRecognizing"
              class="ocr-loading"
            >识别中...</span>
          </button>
          <button
            class="tool-btn"
            title="撤销 (Ctrl+Z)"
            :disabled="history.length === 0"
            @click="undo"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M12.5 8c-2.65 0-5.05.99-6.9 2.6L2 7v9h9l-3.62-3.62c1.39-1.16 3.16-1.88 5.12-1.88 3.54 0 6.55 2.31 7.6 5.5l2.37-.78C21.08 11.03 17.15 8 12.5 8z"
            /></svg>
          </button>
          <button
            class="tool-btn"
            title="重做 (Ctrl+Y)"
            :disabled="redoHistory.length === 0"
            @click="redo"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M18.4 10.6C16.55 9 14.15 8 11.5 8c-4.65 0-8.58 3.03-9.96 7.22L3.9 16c1.05-3.19 4.05-5.5 7.6-5.5 1.95 0 3.73.72 5.12 1.88L13 16h9V7l-3.6 3.6z"
            /></svg>
          </button>
        </div>

        <!-- 分隔线 -->
        <div class="toolbar-divider" />

        <!-- 完成按钮 -->
        <div class="tool-group">
          <button
            class="tool-btn cancel-btn"
            title="取消 (ESC)"
            @click="handleCancel"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
            /></svg>
          </button>
          <button
            class="tool-btn sticker-btn"
            title="钉在桌面"
            @click="handlePinToDesktop"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M16 12V4H17V2H7V4H8V12L6 14V16H11.2V22H12.8V16H18V14L16 12Z"
            /></svg>
            <span>钉在桌面</span>
          </button>
          <button
            class="tool-btn save-btn"
            title="发送 (Enter)"
            @click="handleConfirm"
          >
            <svg viewBox="0 0 24 24"><path
              fill="currentColor"
              d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"
            /></svg>
            <span>发送</span>
          </button>
        </div>

        <!-- 桌面贴图组件 -->
        <DesktopSticker ref="stickerRef" />
      </div>

      <!-- 文字输入框 (动态定位) -->
      <div
        v-if="showTextInput"
        class="text-input-container"
        :style="textInputStyle"
      >
        <input
          ref="textInputRef"
          v-model="textInputValue"
          class="text-input"
          placeholder="输入文字..."
          @blur="confirmTextInput"
          @keydown.enter="confirmTextInput"
          @keydown.esc="cancelTextInput"
        >
      </div>

      <!-- OCR 结果弹窗 -->
      <div
        v-if="showOcrResult"
        class="ocr-result-dialog"
        @click.self="closeOcrResult"
      >
        <div class="ocr-result-content">
          <div class="ocr-result-header">
            <span class="ocr-result-title">识别结果</span>
            <button
              class="ocr-close-btn"
              @click="closeOcrResult"
            >
              <svg viewBox="0 0 24 24"><path
                fill="currentColor"
                d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
              /></svg>
            </button>
          </div>
          <div class="ocr-result-text">
            {{ ocrResult }}
          </div>
          <div class="ocr-result-footer">
            <button
              class="ocr-btn"
              @click="copyOcrResult"
            >
              <svg viewBox="0 0 24 24"><path
                fill="currentColor"
                d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"
              /></svg>
              复制
            </button>
          </div>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import DesktopSticker from './DesktopSticker.vue'
import { getOCREngine } from '@/utils/ocr'
import { createManualScrollStitcher } from '@/utils/scroll-screenshot'

const props = defineProps({
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['confirm', 'close'])

const isUnmounted = ref(false) // 标记组件是否已卸载

// ==================== 基础状态 ====================
const canvasRef = ref(null)
const editorCanvasRef = ref(null)
const textInputRef = ref(null)
const stickerRef = ref(null)

// ==================== OCR 和滚动截图状态 ====================
const ocrEngine = getOCREngine()
const isRecognizing = ref(false)
const ocrResult = ref('')
const showOcrResult = ref(false)
const scrollStitcher = ref(null)
const isScrollMode = ref(false)

const capturing = ref(false)
const editing = ref(false)
const screenshotImage = ref(null)

// ==================== 选区状态 ====================
const selection = ref({ x: 0, y: 0, width: 0, height: 0 })
const isSelecting = ref(false)
const startPos = ref({ x: 0, y: 0 })

const hasSelection = computed(() => selection.value.width !== 0 && selection.value.height !== 0)
const selectionWidth = computed(() => Math.abs(selection.value.width))
const selectionHeight = computed(() => Math.abs(selection.value.height))

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

  return {
    clipPath: `polygon(
      0 0, 0 100%, ${left}px 100%, ${left}px ${top}px,
      ${left + w}px ${top}px, ${left + w}px ${top + h}px,
      ${left}px ${top + h}px, ${left}px 100%, 100% 100%, 100% 0
    )`
  }
})

// ==================== 编辑区状态 ====================
const editorBounds = ref({ x: 0, y: 0, width: 0, height: 0 })

const containerStyle = computed(() => {
  const { x, y, width, height } = editorBounds.value
  return {
    left: x + 'px',
    top: y + 'px',
    width: width + 'px',
    height: height + 'px'
  }
})

// ==================== 工具状态 ====================
const currentTool = ref('rectangle') // rectangle, arrow, brush, text, mosaic, eraser
const strokeColor = ref('#ff0000')
const customColor = ref('#ff0000')
const lineWidth = ref(3)

const colors = ['#ff0000', '#ff6b00', '#ffff00', '#00ff00', '#00ffff', '#0077ff', '#9d00ff', '#ff00ff', '#ffffff', '#000000']

// ==================== 绘图状态 ====================
const isDrawing = ref(false)
const drawStartPos = ref({ x: 0, y: 0 })
const brushPath = ref([])

// ==================== 历史记录 ====================
const history = ref([])
const redoHistoryRef = ref([])

// ==================== 文字输入状态 ====================
const showTextInput = ref(false)
const textInputValue = ref('')
const textInputPos = ref({ x: 0, y: 0 })

const textInputStyle = computed(() => ({
  left: textInputPos.value.x + 'px',
  top: textInputPos.value.y + 'px'
}))

// ==================== 画布上下文 ====================
let ctx = null
let baseImageData = null

// ==================== 方法：设置工具 ====================
const setTool = tool => {
  currentTool.value = tool
}

const setColor = color => {
  strokeColor.value = color
}

const setCustomColor = () => {
  strokeColor.value = customColor.value
}

// ==================== 方法：区域选择 ====================
const handleSelectionMouseDown = e => {
  isSelecting.value = true
  startPos.value = { x: e.clientX, y: e.clientY }
  selection.value = { x: e.clientX, y: e.clientY, width: 0, height: 0 }
}

const handleSelectionMouseMove = e => {
  if (!isSelecting.value) {return}

  selection.value = {
    x: startPos.value.x,
    y: startPos.value.y,
    width: e.clientX - startPos.value.x,
    height: e.clientY - startPos.value.y
  }
}

const handleSelectionMouseUp = () => {
  isSelecting.value = false
}

// ==================== 方法：进入编辑模式 ====================
const enterEditMode = () => {
  if (!hasSelection.value) {return}

  const { x, y, width, height } = selection.value
  const left = Math.min(x, x + width)
  const top = Math.min(y, y + height)
  const w = Math.abs(width)
  const h = Math.abs(height)

  // 保存编辑区边界
  editorBounds.value = { x: left, y: top, width: w, height: h }

  capturing.value = false
  editing.value = true

  nextTick(() => {
    if (isUnmounted.value) {return}
    initEditorCanvas()
  })
}

// ==================== 方法：初始化编辑画布 ====================
const initEditorCanvas = () => {
  const canvas = editorCanvasRef.value
  const sourceCanvas = canvasRef.value

  if (!canvas || !sourceCanvas) {return}

  const { x, y, width, height } = editorBounds.value

  canvas.width = width
  canvas.height = height

  ctx = canvas.getContext('2d')

  // 从源画布复制选区内容
  ctx.drawImage(sourceCanvas, x, y, width, height, 0, 0, width, height)

  // 保存基础图像数据（用于重绘）
  baseImageData = ctx.getImageData(0, 0, width, height)
}

// ==================== 方法：绘图事件处理 ====================
const handleDrawStart = e => {
  if (currentTool.value === 'text') {
    handleTextToolClick(e)
    return
  }

  isDrawing.value = true
  const rect = editorCanvasRef.value.getBoundingClientRect()
  drawStartPos.value = {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  }

  if (currentTool.value === 'brush' || currentTool.value === 'eraser') {
    brushPath.value = [drawStartPos.value]
    saveHistory() // 保存历史记录
  }
}

const handleDrawMove = e => {
  if (!isDrawing.value) {return}

  const rect = editorCanvasRef.value.getBoundingClientRect()
  const currentPos = {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  }

  // 恢复基础图像
  if (currentTool.value === 'rectangle' || currentTool.value === 'arrow' || currentTool.value === 'mosaic') {
    ctx.putImageData(baseImageData, 0, 0)
  }

  switch (currentTool.value) {
    case 'rectangle':
      drawRectangle(drawStartPos.value, currentPos)
      break
    case 'arrow':
      drawArrow(drawStartPos.value, currentPos)
      break
    case 'brush':
      brushPath.value.push(currentPos)
      drawBrush(brushPath.value)
      break
    case 'eraser':
      brushPath.value.push(currentPos)
      drawEraser(brushPath.value)
      break
    case 'mosaic':
      drawMosaic(drawStartPos.value, currentPos)
      break
  }
}

const handleDrawEnd = e => {
  if (!isDrawing.value) {return}

  if (currentTool.value === 'rectangle' || currentTool.value === 'arrow' || currentTool.value === 'mosaic') {
    saveHistory()
  }

  isDrawing.value = false
  brushPath.value = []

  // 更新基础图像
  baseImageData = ctx.getImageData(0, 0, editorCanvasRef.value.width, editorCanvasRef.value.height)
}

// ==================== 方法：绘图函数 ====================
const drawRectangle = (start, end) => {
  const x = Math.min(start.x, end.x)
  const y = Math.min(start.y, end.y)
  const w = Math.abs(end.x - start.x)
  const h = Math.abs(end.y - start.y)

  ctx.strokeStyle = strokeColor.value
  ctx.lineWidth = lineWidth.value
  ctx.setLineDash([])
  ctx.strokeRect(x, y, w, h)
}

const drawArrow = (start, end) => {
  const headLength = 15 + lineWidth.value
  const angle = Math.atan2(end.y - start.y, end.x - start.x)

  ctx.strokeStyle = strokeColor.value
  ctx.lineWidth = lineWidth.value
  ctx.setLineDash([])
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'

  // 绘制主线
  ctx.beginPath()
  ctx.moveTo(start.x, start.y)
  ctx.lineTo(end.x, end.y)
  ctx.stroke()

  // 绘制箭头
  ctx.beginPath()
  ctx.moveTo(end.x, end.y)
  ctx.lineTo(
    end.x - headLength * Math.cos(angle - Math.PI / 6),
    end.y - headLength * Math.sin(angle - Math.PI / 6)
  )
  ctx.moveTo(end.x, end.y)
  ctx.lineTo(
    end.x - headLength * Math.cos(angle + Math.PI / 6),
    end.y - headLength * Math.sin(angle + Math.PI / 6)
  )
  ctx.stroke()
}

const drawBrush = path => {
  if (path.length < 2) {return}

  ctx.strokeStyle = strokeColor.value
  ctx.lineWidth = lineWidth.value
  ctx.setLineDash([])
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'

  ctx.beginPath()
  ctx.moveTo(path[0].x, path[0].y)

  for (let i = 1; i < path.length; i++) {
    ctx.lineTo(path[i].x, path[i].y)
  }

  ctx.stroke()
}

const drawEraser = path => {
  if (path.length < 2) {return}

  const eraserSize = lineWidth.value * 5
  ctx.strokeStyle = '#ffffff'
  ctx.lineWidth = eraserSize
  ctx.setLineDash([])
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'

  ctx.beginPath()
  ctx.moveTo(path[0].x, path[0].y)

  for (let i = 1; i < path.length; i++) {
    ctx.lineTo(path[i].x, path[i].y)
  }

  ctx.stroke()
}

const drawMosaic = (start, end) => {
  const x = Math.min(start.x, end.x)
  const y = Math.min(start.y, end.y)
  const w = Math.abs(end.x - start.x)
  const h = Math.abs(end.y - start.y)

  const blockSize = Math.max(5, Math.floor(lineWidth.value * 2))

  // 获取区域图像数据
  const imageData = ctx.getImageData(x, y, w, h)
  const data = imageData.data

  // 马赛克处理
  for (let by = 0; by < h; by += blockSize) {
    for (let bx = 0; bx < w; bx += blockSize) {
      // 计算块内平均颜色
      let r = 0, g = 0, b = 0, count = 0

      for (let py = by; py < Math.min(by + blockSize, h); py++) {
        for (let px = bx; px < Math.min(bx + blockSize, w); px++) {
          const idx = (py * w + px) * 4
          r += data[idx]
          g += data[idx + 1]
          b += data[idx + 2]
          count++
        }
      }

      r = Math.floor(r / count)
      g = Math.floor(g / count)
      b = Math.floor(b / count)

      // 填充块
      for (let py = by; py < Math.min(by + blockSize, h); py++) {
        for (let px = bx; px < Math.min(bx + blockSize, w); px++) {
          const idx = (py * w + px) * 4
          data[idx] = r
          data[idx + 1] = g
          data[idx + 2] = b
        }
      }
    }
  }

  ctx.putImageData(imageData, x, y)

  // 绘制边框
  ctx.strokeStyle = strokeColor.value
  ctx.lineWidth = 1
  ctx.setLineDash([5, 5])
  ctx.strokeRect(x, y, w, h)
  ctx.setLineDash([])
}

// ==================== 方法：文字工具 ====================
const handleTextToolClick = e => {
  const rect = editorCanvasRef.value.getBoundingClientRect()
  textInputPos.value = {
    x: e.clientX - rect.left + editorBounds.value.x,
    y: e.clientY - rect.top + editorBounds.value.y
  }
  textInputValue.value = ''
  showTextInput.value = true

  nextTick(() => {
    if (isUnmounted.value) {return}
    textInputRef.value?.focus()
  })
}

const confirmTextInput = () => {
  if (!textInputValue.value.trim()) {
    showTextInput.value = false
    return
  }

  const rect = editorCanvasRef.value.getBoundingClientRect()
  const x = textInputPos.value.x - editorBounds.value.x
  const y = textInputPos.value.y - editorBounds.value.y

  saveHistory()

  ctx.font = `${14 + lineWidth.value * 2}px Microsoft YaHei, sans-serif`
  ctx.fillStyle = strokeColor.value
  ctx.textBaseline = 'top'

  // 绘制文字背景
  const metrics = ctx.measureText(textInputValue.value)
  const textWidth = metrics.width
  const textHeight = 14 + lineWidth.value * 2

  ctx.fillStyle = 'rgba(255, 255, 255, 0.8)' // Canvas 绘图用，保持 rgba
  ctx.fillRect(x - 2, y - 2, textWidth + 4, textHeight + 4)

  // 绘制文字
  ctx.fillStyle = strokeColor.value
  ctx.fillText(textInputValue.value, x, y)

  showTextInput.value = false
  baseImageData = ctx.getImageData(0, 0, editorCanvasRef.value.width, editorCanvasRef.value.height)
}

const cancelTextInput = () => {
  showTextInput.value = false
  textInputValue.value = ''
}

// ==================== 方法：历史记录 ====================
const saveHistory = () => {
  if (!ctx) {return}

  const imageData = ctx.getImageData(0, 0, editorCanvasRef.value.width, editorCanvasRef.value.height)
  history.value.push(imageData)

  // 限制历史记录数量
  if (history.value.length > 50) {
    history.value.shift()
  }

  // 清空重做历史
  redoHistoryRef.value = []
}

const undo = () => {
  if (history.value.length === 0) {return}

  // 保存当前状态到重做历史
  redoHistoryRef.value.push(ctx.getImageData(0, 0, editorCanvasRef.value.width, editorCanvasRef.value.height))

  const previous = history.value.pop()
  ctx.putImageData(previous, 0, 0)
  baseImageData = ctx.getImageData(0, 0, editorCanvasRef.value.width, editorCanvasRef.value.height)
}

const redo = () => {
  if (redoHistoryRef.value.length === 0) {return}

  const next = redoHistoryRef.value.pop()
  ctx.putImageData(next, 0, 0)
  baseImageData = ctx.getImageData(0, 0, editorCanvasRef.value.width, editorCanvasRef.value.height)
}

// ==================== 方法：截图捕获 ====================
const startCapture = async () => {
  try {
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

    await new Promise(resolve => {
      video.onloadedmetadata = () => {
        requestAnimationFrame(() => {
          requestAnimationFrame(resolve)
        })
      }
    })

    const canvas = document.createElement('canvas')
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight

    const ctx = canvas.getContext('2d')

    const scaleX = canvas.width / video.videoWidth
    const scaleY = canvas.height / video.videoHeight
    const scale = Math.min(scaleX, scaleY)

    const w = video.videoWidth * scale
    const h = video.videoHeight * scale
    const x = (canvas.width - w) / 2
    const y = (canvas.height - h) / 2

    ctx.fillStyle = '#000'
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    ctx.drawImage(video, x, y, w, h)

    stream.getTracks().forEach(t => t.stop())

    const img = new Image()
    img.onload = () => {
      screenshotImage.value = img
      capturing.value = true

      nextTick(() => {
        if (isUnmounted.value) {return}
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

const initCanvas = (img, x, y, w, h) => {
  const canvas = canvasRef.value
  if (!canvas) {return}

  canvas.width = window.innerWidth
  canvas.height = window.innerHeight

  const ctx = canvas.getContext('2d')
  ctx.fillStyle = '#000'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ctx.drawImage(img, x, y, w, h)

  canvas._offsetX = x
  canvas._offsetY = y
  canvas._scale = w / img.width
}

// ==================== 方法：确认和取消 ====================
// 钉在桌面
const handlePinToDesktop = () => {
  if (!editorCanvasRef.value) {return}

  const dataUrl = editorCanvasRef.value.toDataURL('image/png')
  stickerRef.value?.addSticker(dataUrl)

  ElMessage.success('已钉在桌面，可拖动标题栏移动')
  reset()
}

// OCR 文字识别
const handleOCR = async () => {
  if (!editorCanvasRef.value) {return}

  isRecognizing.value = true
  ElMessage.info('正在识别文字，请稍候...')

  try {
    const dataUrl = editorCanvasRef.value.toDataURL('image/png')
    const result = await ocrEngine.recognize(dataUrl, {
      language: 'chi_sim+eng',
      logger: m => {
        if (m.status === 'processing') {
          // 可以显示进度
        }
      }
    })

    if (result.success && result.text.trim()) {
      ocrResult.value = result.text.trim()
      showOcrResult.value = true

      // 复制到剪贴板
      await navigator.clipboard.writeText(result.text)
      ElMessage.success(`识别成功！已复制 ${result.text.length} 个字符到剪贴板`)
    } else {
      ElMessage.warning('未识别到文字，请尝试调整截图区域')
    }
  } catch (error) {
    console.error('OCR 识别失败:', error)
    ElMessage.error('文字识别失败: ' + error.message)
  } finally {
    isRecognizing.value = false
  }
}

// 关闭 OCR 结果弹窗
const closeOcrResult = () => {
  showOcrResult.value = false
}

// 复制 OCR 结果
const copyOcrResult = async () => {
  try {
    await navigator.clipboard.writeText(ocrResult.value)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const handleConfirm = () => {
  if (!editorCanvasRef.value) {return}

  const dataUrl = editorCanvasRef.value.toDataURL('image/png')
  emit('confirm', dataUrl)
  reset()
}

const handleCancel = () => {
  reset()
  emit('close')
}

const reset = () => {
  capturing.value = false
  editing.value = false
  selection.value = { x: 0, y: 0, width: 0, height: 0 }
  screenshotImage.value = null
  history.value = []
  redoHistoryRef.value = []
  showTextInput.value = false
  textInputValue.value = ''
  brushPath.value = []
  baseImageData = null
  ctx = null
}

// ==================== 键盘事件 ====================
const handleKeydown = e => {
  if (!editing.value) {
    if (e.key === 'Escape') {handleCancel()}
    return
  }

  // 编辑模式快捷键
  if (e.key === 'Escape') {
    handleCancel()
  } else if (e.key === 'Enter' && !showTextInput.value) {
    handleConfirm()
  } else if (e.ctrlKey && e.key === 'z') {
    e.preventDefault()
    undo()
  } else if (e.ctrlKey && e.key === 'y') {
    e.preventDefault()
    redo()
  } else if (e.key === 'r') {
    setTool('rectangle')
  } else if (e.key === 'a') {
    setTool('arrow')
  } else if (e.key === 'p') {
    setTool('brush')
  } else if (e.key === 't') {
    setTool('text')
  } else if (e.key === 'm') {
    setTool('mosaic')
  } else if (e.key === 'e') {
    setTool('eraser')
  }
}

// ==================== 生命周期 ====================
watch(() => props.visible, val => {
  if (val) {
    startCapture()
    document.addEventListener('keydown', handleKeydown)
  } else {
    reset()
    document.removeEventListener('keydown', handleKeydown)
  }
})

onUnmounted(() => {
  isUnmounted.value = true // 标记组件已卸载
  // 清理事件监听器
  document.removeEventListener('keydown', handleKeydown)
  // 清理引用
  ctx = null
  baseImageData = null
})
</script>

<style scoped lang="scss">
@use '@/styles/z-index.scss' as *;
/* ==================== 截图选择层 ==================== */
.dingtalk-screenshot {
  position: fixed;
  inset: 0;
  z-index: $z-tooltip;
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
  background: var(--dt-screenshot-mask);
  pointer-events: none;
}

.selection-border {
  position: absolute;
  border: 2px solid var(--dt-text-inverse);
  box-shadow: 0 0 0 1px var(--dt-screenshot-border-shadow), 0 4px 20px var(--dt-screenshot-shadow-card);
  pointer-events: none;
}

.size-tooltip {
  position: absolute;
  top: -30px;
  left: 0;
  padding: 4px 10px;
  background: var(--dt-screenshot-size-bg);
  color: var(--dt-text-inverse);
  font-size: 12px;
  font-family: monospace;
  border-radius: var(--dt-radius-sm);
  white-space: nowrap;
}

.confirm-toolbar {
  display: flex;
  gap: 8px;
  padding: 6px;
  background: var(--dt-screenshot-toolbar-bg);
  border-radius: var(--dt-radius-md);
  box-shadow: 0 4px 20px var(--dt-screenshot-shadow-strong);
  pointer-events: auto;
}

.screenshot-hint {
  position: fixed;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  background: var(--dt-overlay-80);
  color: var(--dt-text-inverse);
  font-size: 14px;
  border-radius: var(--dt-radius-3xl);
  pointer-events: none;
}

/* ==================== 编辑层 ==================== */
.screenshot-editor {
  position: fixed;
  inset: 0;
  z-index: $z-tooltip;
  background: var(--dt-overlay-80);
  user-select: none;
}

.editor-canvas-container {
  position: absolute;
  background: var(--dt-text-inverse);
  box-shadow: 0 0 0 1px var(--dt-screenshot-border-shadow), 0 8px 40px var(--dt-screenshot-shadow-card);
}

.editor-canvas {
  display: block;
  cursor: crosshair;
}

/* ==================== 编辑工具栏 ==================== */
.editor-toolbar {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: var(--dt-screenshot-toolbar-bg);
  border-radius: var(--dt-radius-lg);
  box-shadow: 0 4px 20px var(--dt-screenshot-shadow-strong);
}

.tool-group {
  display: flex;
  align-items: center;
  gap: 4px;
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: var(--dt-screenshot-border-white);
  margin: 0 4px;
}

.tool-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all 0.2s;
  color: var(--dt-screenshot-text-white);
}

.tool-btn:hover:not(:disabled) {
  background: var(--dt-screenshot-hover-white);
  color: var(--dt-text-inverse);
}

.tool-btn.active {
  background: var(--dt-screenshot-active-brand);
  color: var(--dt-brand-color);
}

.tool-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.tool-btn svg {
  width: 20px;
  height: 20px;
}

.cancel-btn:hover {
  background: var(--dt-screenshot-btn-cancel-bg);
  color: var(--dt-error-color);
}

.save-btn {
  background: var(--dt-brand-color);
  color: var(--dt-text-inverse);
  padding: 0 16px;
  gap: 6px;
}

.save-btn:hover {
  background: var(--dt-brand-hover);
}

.sticker-btn {
  background: var(--dt-screenshot-btn-sticker-bg);
  color: var(--dt-warning-color);
  padding: 0 12px;
  gap: 4px;
}

.sticker-btn:hover {
  background: var(--dt-screenshot-btn-sticker-hover);
}

/* ==================== 颜色选择器 ==================== */
.color-btn {
  width: 28px;
  height: 28px;
  border: 2px solid transparent;
  border-radius: var(--dt-radius-full);
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.color-btn:hover {
  transform: scale(1.1);
}

.color-btn.active {
  border-color: var(--dt-text-inverse);
  box-shadow: 0 0 0 2px var(--dt-screenshot-shadow-brand);
}

.custom-color {
  overflow: hidden;
  background: conic-gradient(red, yellow, lime, aqua, blue, magenta, red);
}

.custom-color input {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  opacity: 0;
  cursor: pointer;
}

/* ==================== 线宽调节 ==================== */
.line-width-group {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 8px;
}

.line-width-label {
  font-size: 12px;
  color: var(--dt-screenshot-text-white-60);
}

.line-width-slider {
  width: 80px;
  height: 4px;
  -webkit-appearance: none;
  appearance: none;
  background: var(--dt-screenshot-slider-track);
  border-radius: var(--dt-radius-sm);
  outline: none;
}

.line-width-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 14px;
  height: 14px;
  background: var(--dt-text-inverse);
  border-radius: var(--dt-radius-full);
  cursor: pointer;
}

.line-width-value {
  font-size: 12px;
  color: var(--dt-white-85);
  min-width: 20px;
  text-align: center;
}

/* ==================== 文字输入框 ==================== */
.text-input-container {
  position: fixed;
  z-index: 10001;
}

.text-input {
  padding: 6px 10px;
  font-size: 14px;
  border: 2px solid var(--dt-brand-color);
  border-radius: var(--dt-radius-sm);
  outline: none;
  min-width: 150px;
  background: var(--dt-overlay-white-95);
}

/* ==================== OCR 按钮 ==================== */
.tool-ocr-btn {
  background: var(--dt-screenshot-btn-ocr-bg);
  color: var(--dt-success-color);
  padding: 0 12px;
  gap: 4px;
}

.tool-ocr-btn:hover {
  background: var(--dt-screenshot-btn-ocr-hover);
}

.ocr-loading {
  font-size: 11px;
}

/* ==================== OCR 结果弹窗 ==================== */
.ocr-result-dialog {
  position: fixed;
  inset: 0;
  z-index: 10002;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dt-overlay-70);
}

.ocr-result-content {
  width: 500px;
  max-width: 80vw;
  max-height: 70vh;
  background: var(--dt-overlay-98);
  border-radius: var(--dt-radius-lg);
  box-shadow: 0 8px 40px var(--dt-black-20);
  display: flex;
  flex-direction: column;
}

.ocr-result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-screenshot-border-dialog);
}

.ocr-result-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--dt-text-inverse);
}

.ocr-close-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  color: var(--dt-screenshot-text-white-60);
  transition: all 0.2s;
}

.ocr-close-btn:hover {
  background: var(--dt-screenshot-btn-cancel-bg);
  color: var(--dt-error-color);
}

.ocr-close-btn svg {
  width: 18px;
  height: 18px;
}

.ocr-result-text {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  color: var(--dt-screenshot-text-muted-9);
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-all;
}

.ocr-result-footer {
  padding: 12px 20px;
  border-top: 1px solid var(--dt-screenshot-border-dialog);
  display: flex;
  justify-content: flex-end;
}

.ocr-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--dt-brand-color);
  color: var(--dt-text-inverse);
  border: none;
  border-radius: var(--dt-radius-md);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.ocr-btn:hover {
  background: var(--dt-brand-hover);
}

.ocr-btn svg {
  width: 18px;
  height: 18px;
}
</style>
