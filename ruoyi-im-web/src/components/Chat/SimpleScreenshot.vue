<template>
  <teleport to="body">
    <transition name="fade">
      <div
        v-if="visible"
        class="simple-screenshot-overlay"
      >
        <div class="screenshot-hint">
          <p>1. 选择要截取的屏幕或窗口</p>
          <p>2. 系统会自动截取整个屏幕</p>
          <p>3. 截图完成后可直接发送</p>
          <button
            class="start-btn"
            @click="startCapture"
          >
            <span class="material-icons-outlined">screenshot</span>
            开始截图
          </button>
          <button
            class="close-btn"
            @click="$emit('close')"
          >
            <span class="material-icons-outlined">close</span>
          </button>
        </div>
      </div>
    </transition>

    <!-- 预览弹窗 -->
    <el-dialog
      v-model="showPreview"
      title="截图预览"
      width="500px"
      :show-close="true"
      @close="handleCancel"
    >
      <div
        v-if="previewImage"
        class="preview-container"
      >
        <img
          :src="previewImage"
          alt="截图预览"
        >
      </div>
      <template #footer>
        <el-button @click="handleRetake">
          重新截图
        </el-button>
        <el-button
          type="primary"
          @click="handleSend"
        >
          发送
        </el-button>
        <el-button @click="handleCancel">
          取消
        </el-button>
      </template>
    </el-dialog>
  </teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['confirm', 'close'])

const showPreview = ref(false)
const previewImage = ref(null)

const startCapture = async () => {
  try {
    // 使用浏览器原生截图 API
    const stream = await navigator.mediaDevices.getDisplayMedia({
      video: { cursor: 'never' }
    })

    const video = document.createElement('video')
    video.srcObject = stream
    await video.play()

    // 等待一帧
    await new Promise(resolve => setTimeout(resolve, 100))

    // 截取画面
    const canvas = document.createElement('canvas')
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight

    const ctx = canvas.getContext('2d')
    ctx.drawImage(video, 0, 0)

    // 停止流
    stream.getTracks().forEach(t => t.stop())

    // 获取图片
    const dataURL = canvas.toDataURL('image/png')
    previewImage.value = dataURL
    showPreview.value = true

  } catch (err) {
    if (err.name === 'NotAllowedError') {
      ElMessage.info('已取消截图')
    } else {
      ElMessage.error('截图失败：' + err.message)
    }
  }
}

const handleRetake = () => {
  previewImage.value = null
  showPreview.value = false
  startCapture()
}

const handleSend = () => {
  if (previewImage.value) {
    emit('confirm', previewImage.value)
    reset()
  }
}

const handleCancel = () => {
  reset()
  emit('close')
}

const reset = () => {
  showPreview.value = false
  previewImage.value = null
}

watch(() => props.visible, val => {
  if (!val) {reset()}
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;
@use '@/styles/z-index.scss' as *;

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.simple-screenshot-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  z-index: $z-tooltip;
  display: flex;
  align-items: center;
  justify-content: center;
}

.screenshot-hint {
  position: relative;
  padding: 40px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  text-align: center;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.3);
  max-width: 400px;

  p {
    margin: 8px 0;
    color: var(--dt-text-primary);
    font-size: 14px;
  }

  .start-btn {
    margin-top: 24px;
    padding: 12px 32px;
    background: var(--dt-brand-color);
    color: #fff;
    border: none;
    border-radius: var(--dt-radius-md);
    font-size: 16px;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    gap: 8px;
    transition: opacity 0.2s;

    &:hover {
      opacity: 0.9;
    }

    .material-icons-outlined {
      font-size: 20px;
    }
  }

  .close-btn {
    position: absolute;
    top: 16px;
    right: 16px;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: transparent;
    border: none;
    border-radius: 50%;
    cursor: pointer;
    color: var(--dt-text-secondary);

    &:hover {
      background: rgba(0, 0, 0, 0.1);
    }
  }
}

.preview-container {
  text-align: center;

  img {
    max-width: 100%;
    max-height: 400px;
    border-radius: var(--dt-radius-md);
  }
}
</style>
