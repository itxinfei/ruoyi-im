<template>
  <div
    v-if="enabled"
    id="im-security-watermark"
    class="watermark-overlay"
    aria-hidden="true"
  />
</template>

<script setup>
import { onMounted, onUnmounted, watch, computed } from 'vue'
import { useStore } from 'vuex'
import { useTheme } from '@/composables/useTheme'

const props = defineProps({
  enabled: { type: Boolean, default: true },
  opacity: { type: Number, default: 0.12 }
})

const store = useStore()
const { isDark } = useTheme()
let watermarkDiv = null
let observer = null

const currentUser = computed(() => store.getters['user/currentUser'] || {})
const watermarkText = computed(() => {
  const name = currentUser.value.nickname || currentUser.value.username || 'RuoYi-IM'
  const id = currentUser.value.id ? `(${currentUser.value.id})` : ''
  const date = new Date().toISOString().slice(0, 10)
  return `${name}${id} ${date} 内网专用`
})

const drawWatermark = () => {
  if (!props.enabled) {return}

  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  const width = 300
  const height = 200
  
  canvas.width = width
  canvas.height = height
  
  ctx.font = '14px sans-serif'
  ctx.fillStyle = isDark.value ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  
  ctx.translate(width / 2, height / 2)
  ctx.rotate((-25 * Math.PI) / 180)
  ctx.fillText(watermarkText.value, 0, 0)
  
  const base64Url = canvas.toDataURL('image/png')
  
  if (!watermarkDiv) {
    watermarkDiv = document.createElement('div')
    watermarkDiv.style.pointerEvents = 'none'
    watermarkDiv.style.position = 'fixed'
    watermarkDiv.style.zIndex = '99999'
    watermarkDiv.style.top = '0'
    watermarkDiv.style.left = '0'
    watermarkDiv.style.width = '100vw'
    watermarkDiv.style.height = '100vh'
    watermarkDiv.style.background = `url(${base64Url}) left top repeat`
    document.body.appendChild(watermarkDiv)
  } else {
    watermarkDiv.style.background = `url(${base64Url}) left top repeat`
  }
}

const removeWatermark = () => {
  if (watermarkDiv && document.body.contains(watermarkDiv)) {
    document.body.removeChild(watermarkDiv)
    watermarkDiv = null
  }
}

// 防篡改监控
const initObserver = () => {
  if (observer) {return}
  observer = new MutationObserver(mutations => {
    mutations.forEach(mutation => {
      if (mutation.type === 'childList') {
        const removedNodes = Array.from(mutation.removedNodes)
        if (removedNodes.includes(watermarkDiv)) {
          drawWatermark() // 被删除了，立即重建
        }
      } else if (mutation.type === 'attributes' && mutation.target === watermarkDiv) {
        drawWatermark() // 样式被改了，立即重置
      }
    })
  })
  observer.observe(document.body, { childList: true, attributes: true, subtree: true })
}

watch(() => [props.enabled, isDark.value, watermarkText.value], () => {
  if (props.enabled) {
    drawWatermark()
    initObserver()
  } else {
    removeWatermark()
    if (observer) { observer.disconnect(); observer = null }
  }
}, { immediate: true })

onMounted(() => {
  if (props.enabled) {
    drawWatermark()
    initObserver()
  }
})

onUnmounted(() => {
  removeWatermark()
  if (observer) {observer.disconnect()}
})
</script>

<style scoped>
.watermark-overlay { display: none; } /* 逻辑在JS中直接操作DOM */
</style>
