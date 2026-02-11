/**
 * 拖拽上传 Composable
 *
 * 职责：
 * - 拖拽文件进入/离开/释放处理
 * - 粘贴图片处理
 * - isDragging / isDragOver 状态
 */

import { ref } from 'vue'

export function useChatDragDrop(sendImage, sendFile) {
  const isDragging = ref(false)
  const isDragOver = ref(false)
  let dragEnterCounter = 0

  const handleDragEnter = event => {
    dragEnterCounter++
    const files = event.dataTransfer?.files
    if (files && files.length > 0) {
      isDragOver.value = true
    }
  }

  const handleDragOver = event => {
    const files = event.dataTransfer?.files
    if (files && files.length > 0) {
      isDragging.value = true
    }
  }

  const handleDragLeave = event => {
    dragEnterCounter--
    if (dragEnterCounter <= 0) {
      dragEnterCounter = 0
      isDragging.value = false
      isDragOver.value = false
    }
  }

  const handleDrop = async event => {
    isDragging.value = false
    isDragOver.value = false
    dragEnterCounter = 0

    const files = event.dataTransfer?.files
    if (!files || files.length === 0) { return }

    for (const file of files) {
      if (file.type.startsWith('image/')) {
        await sendImage(file)
      } else {
        await sendFile(file)
      }
    }
  }

  const handlePaste = async event => {
    const items = event.clipboardData?.items
    if (!items) { return }

    for (const item of items) {
      if (item.type.startsWith('image/')) {
        const file = item.getAsFile()
        if (file) {
          await sendImage(file)
          break // 只处理第一张图片
        }
      }
    }
  }

  return {
    isDragging,
    isDragOver,
    handleDragEnter,
    handleDragOver,
    handleDragLeave,
    handleDrop,
    handlePaste
  }
}
