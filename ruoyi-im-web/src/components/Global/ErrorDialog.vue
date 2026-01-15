<template>
  <el-dialog
    v-model="visible"
    :title="errorInfo.title"
    :width="400"
    :show-close="false"
    destroy-on-close
    :close-on-click-modal="false"
    class="error-dialog"
  >
    <div class="error-content">
      <el-icon :size="48" :color="errorInfo.type === 'error' ? '#ff4d4f' : '#faad14'" class="error-icon">
        <component :is="errorInfo.icon" />
      </el-icon>
      <p class="error-message">{{ errorInfo.message }}</p>
      <div v-if="errorInfo.details" class="error-details">
        <div class="details-label">错误详情：</div>
        <pre class="details-text">{{ errorInfo.details }}</pre>
      </div>
      <div class="error-actions">
        <el-button v-if="errorInfo.canRetry" @click="handleRetry" type="primary" plain>
          重试
        </el-button>
        <el-button @click="handleClose" type="default" plain>
          关闭
        </el-button>
        <el-button v-if="errorInfo.canReport" @click="handleReport" type="info" plain>
          报告问题
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { WarningFilled, InfoFilled, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const visible = ref(false)
const errorInfo = ref({
  title: '发生错误',
  message: '',
  type: 'error', // 'error', 'warning', 'info'
  icon: WarningFilled,
  details: '',
  canRetry: false,
  canReport: true
  retryAction: null
})

const showError = (title, message, options = {}) => {
  errorInfo.value = {
    title,
    message,
    type: options.type || 'error',
    icon: options.icon || WarningFilled,
    details: options.details || '',
    canRetry: options.canRetry || false,
    canReport: options.canReport !== false,
    retryAction: options.retryAction
  }
  visible.value = true

  if (options.onClose) {
    visible.value = false
    options.onClose()
  }

  ElMessage.error(message)
}

const showWarning = (title, message, options = {}) => {
  errorInfo.value = {
    title,
    message,
    type: 'warning',
    icon: options.icon || WarningFilled,
    details: options.details || '',
    canRetry: options.canRetry || false,
    canReport: options.canReport !== false,
    retryAction: options.retryAction
  }
  visible.value = true

  if (options.onClose) {
    visible.value = false
    options.onClose()
  }

  ElMessage.warning(message)
}

const showInfo = (title, message, options = {}) => {
  errorInfo.value = {
    title,
    message,
    type: 'info',
    icon: options.icon || InfoFilled,
    details: options.details || '',
    canRetry: false,
    canReport: false,
    retryAction: null
  }
  visible.value = true

  if (options.onClose) {
    visible.value = false
    options.onClose()
  }

  ElMessage.info(message)
}

const handleClose = () => {
  visible.value = false
  if (errorInfo.value.onClose) {
    errorInfo.value.onClose()
  }
}

const handleRetry = () => {
  visible.value = false
  if (errorInfo.value.retryAction) {
    try {
      errorInfo.value.retryAction()
    } catch (err) {
      console.error('重试失败:', err)
      showWarning('重试失败', '无法重试操作')
    }
  }
}

const handleReport = () => {
  visible.value = false
  showInfo('问题已报告，我们会尽快修复', {
    onClose: () => {
      if (errorInfo.value.onClose) {
        errorInfo.value.onClose()
      }
    }
  })
}

// 导出全局方法供其他组件使用
defineExpose({
  showError,
  showWarning,
  showInfo,
  handleClose
})
</script>

<style lang="scss" scoped>
.error-dialog {
  :deep(.el-dialog__header) {
    padding: 0;
  }
}

.error-content {
  padding: 24px 0;
  text-align: center;

  .error-icon {
    margin-bottom: 16px;
    color: #ff4d4f;
    margin-bottom: 16px;
    font-size: 48px;
  }

  .error-message {
    font-size: 16px;
    color: #262626;
    margin: 0 0 16px;
    line-height: 1.5;
  }

  .error-details {
    background: #f5f5f5;
    border-radius: 8px;
    padding: 16px;
    margin: 16px 0;
    text-align: left;

    .details-label {
      font-size: 13px;
      color: #595959;
      margin-bottom: 8px;
      font-weight: 500;
    }

    .details-text {
      font-family: 'Consolas', monospace;
      font-size: 13px;
      line-height: 1.6;
      color: #262626;
      background: #ffffff;
      padding: 12px;
      border-radius: 4px;
      max-height: 200px;
      overflow: auto;
      white-space: pre-wrap;
    }
  }

  .error-actions {
    display: flex;
    gap: 8px;
    justify-content: center;
  }
}
</style>