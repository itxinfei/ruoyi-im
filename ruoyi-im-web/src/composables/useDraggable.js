// 拖拽逻辑 Composable
// 支持鼠标和触摸事件，自动吸附到屏幕边缘
// @author RuoYi-IM
import { ref, computed, onMounted, onUnmounted } from 'vue'

// 使用拖拽功能
// @param {Object} options - 配置选项
// @param {Ref<HTMLElement>} options.target - 拖拽目标元素
// @param {number} options.snapThreshold - 吸附阈值（px），默认 40
// @param {number} options.padding - 边界内边距（px），默认 0
// @param {boolean} options.constrainToParent - 是否限制在父元素内，默认 false
// @returns {Object} 拖拽相关的状态和方法
export function useDraggable(options = {}) {
  const {
    target = ref(null),
    snapThreshold = 40,
    padding = 0,
    constrainToParent = false,
  } = options

  // 状态
  const isDragging = ref(false)
  const position = ref({ x: 0, y: 0 })
  const dragOffset = ref({ x: 0, y: 0 })
  const isSnapped = ref({ left: false, right: false, top: false, bottom: false })

  // 视窗尺寸
  const viewportSize = ref({ width: window.innerWidth, height: window.innerHeight })

  // 当前拖拽手柄
  let dragHandle = null

  // 获取元素尺寸
  const getElementSize = () => {
    if (!target.value) return { width: 0, height: 0 }
    const rect = target.value.getBoundingClientRect()
    return { width: rect.width, height: rect.height }
  }

  // 计算限制后的位置
  const constrainPosition = (x, y) => {
    const { width: elemWidth, height: elemHeight } = getElementSize()
    const { width: viewWidth, height: viewHeight } = viewportSize.value

    let newX = x
    let newY = y

    // 左边界
    if (newX < padding) {
      newX = padding
    }
    // 右边界
    if (newX + elemWidth > viewWidth - padding) {
      newX = viewWidth - elemWidth - padding
    }
    // 上边界
    if (newY < padding) {
      newY = padding
    }
    // 下边界
    if (newY + elemHeight > viewHeight - padding) {
      newY = viewHeight - elemHeight - padding
    }

    return { x: newX, y: newY }
  }

  // 计算吸附位置
  const snapToEdge = (x, y) => {
    const { width: elemWidth, height: elemHeight } = getElementSize()
    const { width: viewWidth, height: viewHeight } = viewportSize.value

    let newX = x
    let newY = y
    const snapped = { left: false, right: false, top: false, bottom: false }

    // 左边缘吸附
    if (x < snapThreshold) {
      newX = padding
      snapped.left = true
    }
    // 右边缘吸附
    if (x + elemWidth > viewWidth - snapThreshold - padding) {
      newX = viewWidth - elemWidth - padding
      snapped.right = true
    }
    // 上边缘吸附
    if (y < snapThreshold) {
      newY = padding
      snapped.top = true
    }
    // 下边缘吸附
    if (y + elemHeight > viewHeight - snapThreshold - padding) {
      newY = viewHeight - elemHeight - padding
      snapped.bottom = true
    }

    return { x: newX, y: newY, snapped }
  }

  // 开始拖拽
  const startDrag = (event) => {
    // 检查是否点击了拖拽手柄
    const handle = event.target.closest('[data-drag-handle]')
    if (!handle && !event.target.hasAttribute?.('data-drag-handle')) {
      return
    }

    event.preventDefault()
    isDragging.value = true
    dragHandle = handle || event.target

    // 获取点击位置相对于元素的偏移
    const clientX = event.touches ? event.touches[0].clientX : event.clientX
    const clientY = event.touches ? event.touches[0].clientY : event.clientY

    dragOffset.value = {
      x: clientX - position.value.x,
      y: clientY - position.value.y,
    }

    // 添加拖拽中的样式
    if (target.value) {
      target.value.classList.add('is-dragging')
      target.value.style.transition = 'none'
    }

    // 监听移动和结束事件
    document.addEventListener('mousemove', onDragMove, { passive: false })
    document.addEventListener('mouseup', onDragEnd)
    document.addEventListener('touchmove', onDragMove, { passive: false })
    document.addEventListener('touchend', onDragEnd)
  }

  // 拖拽移动
  const onDragMove = (event) => {
    if (!isDragging.value) return
    event.preventDefault()

    const clientX = event.touches ? event.touches[0].clientX : event.clientX
    const clientY = event.touches ? event.touches[0].clientY : event.clientY

    // 计算新位置
    let newX = clientX - dragOffset.value.x
    let newY = clientY - dragOffset.value.y

    // 限制在视窗内
    const constrained = constrainPosition(newX, newY)
    position.value = { x: constrained.x, y: constrained.y }
  }

  // 结束拖拽
  const onDragEnd = () => {
    if (!isDragging.value) return

    // 计算吸附位置
    const snapped = snapToEdge(position.value.x, position.value.y)
    position.value = { x: snapped.x, y: snapped.y }
    isSnapped.value = snapped.snapped

    // 移除拖拽样式
    if (target.value) {
      target.value.classList.remove('is-dragging')
      target.value.style.transition = ''
    }

    isDragging.value = false

    // 移除事件监听
    document.removeEventListener('mousemove', onDragMove)
    document.removeEventListener('mouseup', onDragEnd)
    document.removeEventListener('touchmove', onDragMove)
    document.removeEventListener('touchend', onDragEnd)
  }

  // 设置位置
  const setPosition = (x, y) => {
    const constrained = constrainPosition(x, y)
    position.value = { x: constrained.x, y: constrained.y }
  }

  // 重置到默认位置（右下角）
  const resetPosition = () => {
    const { width: elemWidth, height: elemHeight } = getElementSize()
    const { width: viewWidth, height: viewHeight } = viewportSize.value

    position.value = {
      x: viewWidth - elemWidth - 20,
      y: viewHeight - elemHeight - 100,
    }
    isSnapped.value = { right: true, bottom: true, left: false, top: false }
  }

  // 处理视窗尺寸变化
  const handleResize = () => {
    viewportSize.value = {
      width: window.innerWidth,
      height: window.innerHeight,
    }
    // 重新限制位置
    const constrained = constrainPosition(position.value.x, position.value.y)
    position.value = { x: constrained.x, y: constrained.y }
  }

  // 计算样式
  const style = computed(() => ({
    position: 'fixed',
    left: `${position.value.x}px`,
    top: `${position.value.y}px`,
    transform: isDragging.value ? 'scale(1.02)' : 'scale(1)',
    cursor: isDragging.value ? 'grabbing' : 'grab',
    userSelect: isDragging.value ? 'none' : '',
    transition: isDragging.value ? 'none' : 'transform 0.2s ease, left 0.3s cubic-bezier(0.4, 0, 0.2, 1), top 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
  }))

  // 生命周期
  onMounted(() => {
    if (target.value) {
      target.value.addEventListener('mousedown', startDrag)
      target.value.addEventListener('touchstart', startDrag, { passive: false })
    }
    window.addEventListener('resize', handleResize)
    // 初始化位置
    resetPosition()
  })

  onUnmounted(() => {
    if (target.value) {
      target.value.removeEventListener('mousedown', startDrag)
      target.value.removeEventListener('touchstart', startDrag)
    }
    window.removeEventListener('resize', handleResize)
  })

  return {
    // 状态
    isDragging,
    position,
    isSnapped,
    style,

    // 方法
    startDrag,
    setPosition,
    resetPosition,

    // 内部方法（供组件使用）
    _onDragMove: onDragMove,
    _onDragEnd: onDragEnd,
  }
}

export default useDraggable
