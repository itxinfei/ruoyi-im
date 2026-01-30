<template>
  <el-dialog
    v-model="visible"
    title="截图预览"
    width="600px"
    :close-on-click-modal="true"
    :show-close="true"
    @close="handleClose"
    class="screenshot-preview-dialog"
  >
    <div class="screenshot-container">
      <!-- 截图画布 -->
      <div class="canvas-wrapper" :class="{ 'is-editing': isEditing }">
        <canvas
          ref="canvasRef"
          class="screenshot-canvas"
          @mousedown="handleMouseDown"
          @mousemove="handleMouseMove"
          @mouseup="handleMouseUp"
          @mouseleave="handleMouseUp"
        ></canvas>

        <!-- 裁剪遮罩 -->
        <div
          v-if="isEditing"
          class="crop-overlay"
          :style="cropOverlayStyle"
        >
          <div class="crop-border"></div>
        </div>

        <!-- 绘制工具栏 -->
        <div v-if="isEditing" class="draw-toolbar">
          <button
            v-for="tool in drawTools"
            :key="tool.name"
            class="draw-tool-btn"
            :class="{ active: currentTool === tool.name }"
            @click="selectTool(tool.name)"
            :title="tool.label"
          >
            <span class="material-icons-outlined">{{ tool.icon }}</span>
          </button>
          <div class="color-picker-wrapper">
            <input
              type="color"
              v-model="drawColor"
              class="color-picker"
              title="选择颜色"
            />
          </div>
          <div class="stroke-width-wrapper">
            <input
              type="range"
              v-model.number="strokeWidth"
              min="1"
              max="10"
              class="stroke-width"
              title="笔触粗细"
            />
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <div class="footer-left">
          <el-button
            v-if="!isEditing"
            link
            @click="startEdit"
          >
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button
            v-else
            link
            @click="cancelEdit"
          >
            取消编辑
          </el-button>
          <el-button
            v-if="isEditing && hasDrawing"
            link
            @click="clearDrawing"
          >
            <el-icon><Delete /></el-icon>
            清除标记
          </el-button>
        </div>
        <div class="footer-right">
          <el-button @click="handleClose">取消</el-button>
          <el-button
            type="primary"
            @click="handleSend"
            :loading="sending"
          >
            发送
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: Boolean,
  imageData: String
})

const emit = defineEmits(['update:modelValue', 'send', 'close'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const canvasRef = ref(null)
const sending = ref(false)
const isEditing = ref(false)
const currentTool = ref('pen')
const drawColor = ref('#ff0000')
const strokeWidth = ref(3)

// 绘图状态
const isDrawing = ref(false)
const lastX = ref(0)
const lastY = ref(0)
const hasDrawing = ref(false)
const originalImageData = ref(null)

// 裁剪状态
const cropStart = ref({ x: 0, y: 0 })
const cropEnd = ref({ x: 0, y: 0 })
const isCropping = ref(false)

const drawTools = [
  { name: 'pen', icon: 'edit', label: '画笔' },
  { name: 'arrow', icon: 'arrow_forward', label: '箭头' },
  { name: 'rect', icon: 'crop_square', label: '矩形' },
  { name: 'text', icon: 'text_fields', label: '文字' }
]

const cropOverlayStyle = computed(() => {
  const x = Math.min(cropStart.value.x, cropEnd.value.x)
  const y = Math.min(cropStart.value.y, cropEnd.value.y)
  const width = Math.abs(cropEnd.value.x - cropStart.value.x)
  const height = Math.abs(cropEnd.value.y - cropStart.value.y)

  return {
    left: x + 'px',
    top: y + 'px',
    width: width + 'px',
    height: height + 'px'
  }
})

// 初始化画布
const initCanvas = async () => {
  await nextTick()
  const canvas = canvasRef.value
  if (!canvas || !props.imageData) return

  const img = new Image()
  img.onload = () => {
    // 限制最大尺寸
    const maxSize = 1920
    let width = img.width
    let height = img.height

    if (width > maxSize || height > maxSize) {
      const ratio = Math.min(maxSize / width, maxSize / height)
      width *= ratio
      height *= ratio
    }

    canvas.width = width
    canvas.height = height

    const ctx = canvas.getContext('2d')
    ctx.drawImage(img, 0, 0, width, height)

    // 保存原始图像数据
    originalImageData.value = ctx.getImageData(0, 0, width, height)
  }
  img.src = props.imageData
}

// 鼠标事件处理
const handleMouseDown = (e) => {
  if (!isEditing.value) return

  const rect = canvasRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top

  if (currentTool.value === 'crop') {
    isCropping.value = true
    cropStart.value = { x, y }
    cropEnd.value = { x, y }
  } else {
    isDrawing.value = true
    lastX.value = x
    lastY.value = y
  }
}

const handleMouseMove = (e) => {
  if (!isEditing.value) return

  const rect = canvasRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top

  if (isCropping.value) {
    cropEnd.value = { x, y }
  } else if (isDrawing.value) {
    draw(x, y)
    lastX.value = x
    lastY.value = y
  }
}

const handleMouseUp = () => {
  if (isCropping.value) {
    isCropping.value = false
    applyCrop()
  }
  isDrawing.value = false
}

// 绘图函数
const draw = (x, y) => {
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  const tool = currentTool.value

  ctx.strokeStyle = drawColor.value
  ctx.fillStyle = drawColor.value
  ctx.lineWidth = strokeWidth.value
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'

  switch (tool) {
    case 'pen':
      ctx.beginPath()
      ctx.moveTo(lastX.value, lastY.value)
      ctx.lineTo(x, y)
      ctx.stroke()
      break
    case 'arrow':
      drawArrow(ctx, lastX.value, lastY.value, x, y)
      break
    case 'rect':
      // 矩形在 mouseup 时绘制预览
      break
  }

  hasDrawing.value = true
}

// 绘制箭头
const drawArrow = (ctx, fromX, fromY, toX, toY) => {
  const headlen = 15
  const angle = Math.atan2(toY - fromY, toX - fromX)

  ctx.beginPath()
  ctx.moveTo(fromX, fromY)
  ctx.lineTo(toX, toY)
  ctx.stroke()

  ctx.beginPath()
  ctx.moveTo(toX, toY)
  ctx.lineTo(toX - headlen * Math.cos(angle - Math.PI / 6), toY - headlen * Math.sin(angle - Math.PI / 6))
  ctx.moveTo(toX, toY)
  ctx.lineTo(toX - headlen * Math.cos(angle + Math.PI / 6), toY - headlen * Math.sin(angle + Math.PI / 6))
  ctx.stroke()
}

// 应用裁剪
const applyCrop = () => {
  const x = Math.min(cropStart.value.x, cropEnd.value.x)
  const y = Math.min(cropStart.value.y, cropEnd.value.y)
  const width = Math.abs(cropEnd.value.x - cropStart.value.x)
  const height = Math.abs(cropEnd.value.y - cropStart.value.y)

  if (width < 10 || height < 10) return

  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')

  const imageData = ctx.getImageData(x, y, width, height)
  canvas.width = width
  canvas.height = height
  ctx.putImageData(imageData, 0, 0)

  originalImageData.value = ctx.getImageData(0, 0, width, height)
}

// 选择工具
const selectTool = (tool) => {
  currentTool.value = tool
}

// 开始编辑
const startEdit = () => {
  isEditing.value = true
  hasDrawing.value = false
}

// 取消编辑
const cancelEdit = () => {
  if (originalImageData.value) {
    const canvas = canvasRef.value
    const ctx = canvas.getContext('2d')
    ctx.putImageData(originalImageData.value, 0, 0)
  }
  isEditing.value = false
  hasDrawing.value = false
}

// 清除标记
const clearDrawing = () => {
  cancelEdit()
}

// 发送截图
const handleSend = async () => {
  const canvas = canvasRef.value
  if (!canvas) return

  sending.value = true

  try {
    // 转换为 blob
    const blob = await new Promise((resolve) => {
      canvas.toBlob((blob) => resolve(blob), 'image/png')
    })

    // 发送图片消息
    emit('send', blob)
    handleClose()
    ElMessage.success('截图已发送')
  } catch (error) {
    console.error('发送截图失败:', error)
    ElMessage.error('发送截图失败')
  } finally {
    sending.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  isEditing.value = false
  hasDrawing.value = false
  currentTool.value = 'pen'
  emit('close')
  visible.value = false
}

// 监听 dialog 打开
watch(() => props.modelValue, (val) => {
  if (val) {
    initCanvas()
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.screenshot-preview-dialog {
  :deep(.el-dialog__body) {
    padding: 16px;
  }
}

.screenshot-container {
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-md);
  overflow: hidden;
}

.canvas-wrapper {
  position: relative;
  max-width: 100%;
  max-height: 60vh;
  overflow: hidden;

  &.is-editing {
    cursor: crosshair;
  }
}

.screenshot-canvas {
  display: block;
  max-width: 100%;
  max-height: 60vh;
  border-radius: var(--dt-radius-sm);
  box-shadow: var(--dt-shadow-2);
}

.crop-overlay {
  position: absolute;
  border: 2px dashed var(--dt-brand-color);
  background: rgba(0, 137, 255, 0.1);
  pointer-events: none;
}

.crop-border {
  position: absolute;
  inset: 0;
  border: 1px solid var(--dt-brand-color);
}

.draw-toolbar {
  position: absolute;
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  box-shadow: var(--dt-shadow-3);
  z-index: 10;
}

.draw-tool-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: var(--dt-radius-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dt-text-secondary);
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-brand-color);
  }

  &.active {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }

  .material-icons-outlined {
    font-size: 20px;
  }
}

.color-picker-wrapper {
  width: 32px;
  height: 32px;
  border-radius: var(--dt-radius-sm);
  overflow: hidden;
  border: 2px solid var(--dt-border-light);
  cursor: pointer;

  &:hover {
    border-color: var(--dt-brand-color);
  }
}

.color-picker {
  width: 100%;
  height: 100%;
  border: none;
  padding: 0;
  cursor: pointer;
}

.stroke-width-wrapper {
  width: 80px;
  display: flex;
  align-items: center;
}

.stroke-width {
  width: 100%;
  cursor: pointer;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.footer-left {
  display: flex;
  gap: 8px;
}

.footer-right {
  display: flex;
  gap: 12px;
}

.dark {
  .screenshot-container {
    background: var(--dt-bg-hover-dark);
  }

  .draw-toolbar {
    background: var(--dt-bg-card-dark);
    border: 1px solid var(--dt-border-dark);
  }

  .color-picker-wrapper {
    border-color: var(--dt-border-dark);
  }
}
</style>
