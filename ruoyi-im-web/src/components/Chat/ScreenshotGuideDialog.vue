<template>
  <teleport to="body">
    <transition name="fade">
      <div v-if="visible" class="screenshot-guide-overlay">
        <div class="guide-dialog">
          <div class="guide-header">
            <span class="material-icons-outlined">screenshot</span>
            <h3>截图操作指南</h3>
            <button class="close-btn" @click="handleCancel">
              <span class="material-icons-outlined">close</span>
            </button>
          </div>

          <!-- 步骤说明 -->
          <div class="guide-steps">
            <div class="step" :class="{ active: step >= 1 }">
              <div class="step-number">1</div>
              <div class="step-content">
                <p class="step-title">点击下方按钮开始截图</p>
              </div>
            </div>

            <div class="step" :class="{ active: step >= 2 }">
              <div class="step-number">2</div>
              <div class="step-content">
                <p class="step-title">选择要截取的窗口或屏幕</p>
              </div>
            </div>

            <div class="step" :class="{ active: step >= 3 }">
              <div class="step-number">3</div>
              <div class="step-content">
                <p class="step-title">选择「分享此窗口」即可完成</p>
              </div>
            </div>
          </div>

          <!-- 预览区域 -->
          <div v-if="previewImage" class="preview-area">
            <div class="preview-image">
              <img :src="previewImage" alt="截图预览" />
            </div>
            <div class="preview-actions">
              <el-button @click="handleRetake">重新截图</el-button>
              <el-button type="primary" @click="handleSend">发送</el-button>
              <el-button @click="handleCancel">取消</el-button>
            </div>
          </div>

          <!-- 开始按钮 -->
          <div v-else class="start-area">
            <el-button
              type="primary"
              size="large"
              :loading="capturing"
              @click="startCapture"
            >
              <span class="material-icons-outlined">screenshot</span>
              开始截图
            </el-button>
          </div>

          <!-- 提示 -->
          <div class="guide-tips">
            <span class="material-icons-outlined">info</span>
            <span>提示：支持截取整个屏幕或单个窗口</span>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { captureScreenshot, downloadScreenshot } from '@/utils/screenshot'

const props = defineProps({
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['send', 'close'])

const step = ref(1)
const capturing = ref(false)
const previewImage = ref(null)

const startCapture = async () => {
  capturing.value = true
  step.value = 2

  try {
    const dataURL = await captureScreenshot({ cursor: 'none' })
    previewImage.value = dataURL
    step.value = 3
  } catch (error) {
    if (error.message.includes('取消')) {
      ElMessage.info('已取消截图')
    } else {
      ElMessage.error(error.message || '截图失败')
    }
    step.value = 1
  } finally {
    capturing.value = false
  }
}

const handleRetake = () => {
  previewImage.value = null
  step.value = 1
}

const handleSend = () => {
  if (previewImage.value) {
    emit('send', previewImage.value)
    reset()
  }
}

const handleCancel = () => {
  reset()
  emit('close')
}

const reset = () => {
  step.value = 1
  capturing.value = false
  previewImage.value = null
}

// 监听 visible 变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    reset()
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.screenshot-guide-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(10px);
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.guide-dialog {
  width: 100%;
  max-width: 480px;
  background: var(--dt-bg-card);
  border-radius: 16px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 30px;

  .dark & {
    background: var(--dt-bg-card-dark);
  }
}

.guide-header {
  display: flex;
  align-items: center;
  gap: 12px;

  .material-icons-outlined {
    font-size: 24px;
    color: var(--dt-brand-color);
  }

  h3 {
    flex: 1;
    font-size: 18px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;
  }

  .dark & h3 {
    color: var(--dt-text-primary-dark);
  }
}

.close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  color: var(--dt-text-secondary);
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-primary);
  }

  .material-icons-outlined {
    font-size: 20px;
  }
}

.guide-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.step {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  opacity: 0.4;
  transition: opacity 0.3s;

  &.active {
    opacity: 1;
  }
}

.step-number {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dt-bg-body);
  border: 2px solid var(--dt-border-color);
  border-radius: 50%;
  font-size: 13px;
  font-weight: 600;
  color: var(--dt-text-secondary);
  flex-shrink: 0;

  .dark & {
    background: var(--dt-bg-body-dark);
    border-color: var(--dt-border-dark);
  }
}

.step-content {
  flex: 1;

  .step-title {
    margin: 0;
    font-size: 14px;
    color: var(--dt-text-primary);

    .dark & {
      color: var(--dt-text-primary-dark);
    }
  }
}

.preview-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}

.preview-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
  overflow: hidden;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    max-width: 100%;
    max-height: 300px;
    object-fit: contain;
  }
}

.preview-actions {
  display: flex;
  gap: 12px;
  width: 100%;
  justify-content: center;
}

.start-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;

  .el-button {
    min-width: 140px;
  }
}

.guide-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--dt-bg-body);
  border-radius: 8px;

  .material-icons-outlined {
    font-size: 16px;
    color: var(--dt-info-color);
  }

  span {
    font-size: 12px;
    color: var(--dt-text-secondary);
  }

  .dark & {
    background: var(--dt-bg-body-dark);
  }
}
</style>
