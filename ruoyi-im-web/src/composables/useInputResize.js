/**
 * 输入框高度调整 Composable
 *
 * 职责：
 * - 管理输入框容器的拖拽调整高度
 * - 高度状态持久化到 localStorage
 * - 双击重置高度
 *
 * 使用方式：
 * ```js
 * const { containerHeight, startResize, resetHeight } = useInputResize({
 *   minHeight: 160,
 *   maxHeight: 600,
 *   defaultHeight: 160,
 *   storageKey: 'im_input_height'
 * })
 * ```
 */
import { ref, onMounted } from 'vue'

export function useInputResize(options = {}) {
  const {
    minHeight = 160,
    maxHeight = 600,
    defaultHeight = 160,
    storageKey = 'im_input_height'
  } = options

  const containerHeight = ref(defaultHeight)
  const isResizing = ref(false)

  let startY = 0
  let startHeight = 0

  /**
   * 开始调整高度
   */
  const startResize = (e) => {
    isResizing.value = true
    startY = e.clientY
    startHeight = containerHeight.value

    document.addEventListener('mousemove', handleResize)
    document.addEventListener('mouseup', stopResize)
    document.body.style.cursor = 'ns-resize'
  }

  /**
   * 处理调整中
   */
  const handleResize = (e) => {
    if (!isResizing.value) return

    const delta = startY - e.clientY
    const newHeight = startHeight + delta

    // 限制在最小和最大高度之间
    if (newHeight >= minHeight && newHeight <= maxHeight) {
      containerHeight.value = newHeight
    }
  }

  /**
   * 停止调整高度
   */
  const stopResize = () => {
    isResizing.value = false
    document.body.style.cursor = ''

    // 持久化高度设置
    try {
      localStorage.setItem(storageKey, String(containerHeight.value))
    } catch (e) {
      console.warn('Failed to save input height:', e)
    }

    document.removeEventListener('mousemove', handleResize)
    document.removeEventListener('mouseup', stopResize)
  }

  /**
   * 重置高度
   */
  const resetHeight = () => {
    containerHeight.value = defaultHeight
    try {
      localStorage.setItem(storageKey, String(defaultHeight))
    } catch (e) {
      console.warn('Failed to save input height:', e)
    }
  }

  /**
   * 从 localStorage 恢复高度
   */
  const restoreHeight = () => {
    const { getItem } = require('@/utils/storage')
    try {
      const saved = getItem(storageKey)
      if (saved) {
        const height = parseInt(saved, 10)
        if (!isNaN(height) && height >= minHeight && height <= maxHeight) {
          containerHeight.value = height
        }
      }
    } catch (e) {
      console.warn('Failed to restore input height:', e)
    }
  }

  // 组件挂载时恢复高度
  onMounted(() => {
    restoreHeight()
  })

  return {
    // 状态
    containerHeight,
    isResizing,

    // 方法
    startResize,
    resetHeight,
    restoreHeight
  }
}
