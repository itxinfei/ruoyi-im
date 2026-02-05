<template>
  <div class="web-screenshot-wrapper">
    <!-- 截图按钮，点击触发截图 -->
    <div ref="triggerRef" />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'

/**
 * 使用 js-web-screen-shot 实现截图功能
 * 类似 QQ/微信的截图体验
 */
const props = defineProps({
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['confirm', 'close'])

const triggerRef = ref(null)
let screenShotInstance = null

// 动态加载 js-web-screen-shot
const loadScreenShotLibrary = async () => {
  if (window.ScreenShot) {
    return window.ScreenShot
  }

  // 动态导入
  const module = await import('js-web-screen-shot')
  return module.ScreenShot
}

// 初始化截图
const initScreenshot = async () => {
  try {
    const ScreenShot = await loadScreenShotLibrary()

    // 创建截图实例
    screenShotInstance = new ScreenShot({
      // 关联的DOM元素，点击会触发截图
      el: triggerRef.value,
      // 是否启用WebRtc（截图屏幕内容）
      enableWebRtc: true,
      // 层级
      level: 9999,
      // 截图完成回调
      clickCallback: dataUrl => {
        emit('confirm', dataUrl)
        destroy()
      },
      // 初始化完成回调
      initCallback: () => {
        // 自动触发截图
        if (screenShotInstance && screenShotInstance.start) {
          screenShotInstance.start()
        }
      }
    })

  } catch (error) {
    console.error('加载截图库失败:', error)
    // 降级方案：使用原生API
    fallbackScreenshot()
  }
}

// 降级方案：使用浏览器原生截图
const fallbackScreenshot = async () => {
  try {
    const stream = await navigator.mediaDevices.getDisplayMedia({
      video: { cursor: 'never' }
    })

    const video = document.createElement('video')
    video.srcObject = stream
    await video.play()
    await new Promise(resolve => setTimeout(resolve, 100))

    const canvas = document.createElement('canvas')
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    canvas.getContext('2d').drawImage(video, 0, 0)

    stream.getTracks().forEach(t => t.stop())

    const dataUrl = canvas.toDataURL('image/png')
    emit('confirm', dataUrl)

  } catch (err) {
    emit('close')
  }
}

// 销毁实例
const destroy = () => {
  if (screenShotInstance) {
    if (screenShotInstance.destroy) {
      screenShotInstance.destroy()
    }
    screenShotInstance = null
  }
}

// 监听 visible 变化
watch(() => props.visible, async val => {
  if (val) {
    await initScreenshot()
  } else {
    destroy()
  }
})

onMounted(() => {
  // 组件挂载后，如果visible为true则初始化
  if (props.visible) {
    initScreenshot()
  }
})
</script>

<style scoped>
.web-screenshot-wrapper {
  display: none;
}

/* js-web-screen-shot 的样式会自动注入到页面中 */
</style>
