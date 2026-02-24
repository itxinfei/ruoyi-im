<template>
  <Teleport to="body">
    <el-dialog
      v-model="showDialog"
      title="安全提示"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      class="security-alert-dialog"
      center
    >
      <div class="alert-content">
        <div class="alert-icon">
          <span class="material-icons-outlined">warning</span>
        </div>
        <p class="alert-title">
          检测到截图操作
        </p>
        <p class="alert-message">
          {{ alertMessage }}
        </p>
        <div class="alert-info">
          <div class="info-item">
            <span class="info-label">操作人：</span>
            <span class="info-value">{{ currentUser.nickname || currentUser.username }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">时间：</span>
            <span class="info-value">{{ formatTime(alertTime) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">IP地址：</span>
            <span class="info-value">{{ clientIp || '获取中...' }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button
          type="primary"
          @click="acknowledgeAlert"
        >
          我已知晓
        </el-button>
      </template>
    </el-dialog>
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

const props = defineProps({
  enabled: {
    type: Boolean,
    default: true
  },
  blockScreenshot: {
    type: Boolean,
    default: false
  },
  showAlert: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['screenshot-detected', 'clipboard-change'])

const store = useStore()
const showDialog = ref(false)
const alertTime = ref(new Date())
const clientIp = ref('')
const alertMessage = ref('截图行为已被记录。请注意不要泄露敏感信息。')

const currentUser = computed(() => store.getters['user/currentUser'] || {})

function formatTime(date) {
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

function handleScreenshotAttempt(e, method) {
  alertTime.value = new Date()

  const event = {
    eventType: 'SCREENSHOT_ATTEMPT',
    method,
    userId: currentUser.value.id,
    username: currentUser.value.nickname || currentUser.value.username,
    timestamp: alertTime.value.toISOString(),
    clientIp: clientIp.value
  }

  emit('screenshot-detected', event)

  if (props.showAlert) {
    showDialog.value = true
  }

  if (props.blockScreenshot) {
    e.preventDefault()
    ElMessage.warning('截图功能已被禁用')
  }
}

function handleKeydown(e) {
  if (!props.enabled) {return}

  const key = e.key.toLowerCase()
  const ctrl = e.ctrlKey || e.metaKey

  if (key === 'printscreen') {
    handleScreenshotAttempt(e, 'PrintScreen')
  }

  if (ctrl && key === 'p') {
    e.preventDefault()
    ElMessage.warning('打印功能已被禁用')
  }

  if (ctrl && (key === 's' || key === 'u')) {
    e.preventDefault()
  }
}

function handleClipboardChange(e) {
  if (!props.enabled) {return}

  emit('clipboard-change', {
    eventType: 'CLIPBOARD_CHANGE',
    timestamp: new Date().toISOString(),
    userId: currentUser.value.id
  })
}

function acknowledgeAlert() {
  showDialog.value = false
}

async function fetchClientIp() {
  try {
    const response = await fetch('https://api.ipify.org?format=json')
    const data = await response.json()
    clientIp.value = data.ip
  } catch {
    clientIp.value = '内网环境'
  }
}

function disableRightClick(e) {
  if (!props.enabled) {return}
  e.preventDefault()
}

function disableDevTools(e) {
  if (!props.enabled) {return}
  if (e.key === 'F12') {
    e.preventDefault()
    ElMessage.warning('开发者工具已被禁用')
  }
  if ((e.ctrlKey || e.metaKey) && e.shiftKey && (e.key === 'I' || e.key === 'J' || e.key === 'C')) {
    e.preventDefault()
  }
}

onMounted(() => {
  if (props.enabled) {
    document.addEventListener('keydown', handleKeydown)
    document.addEventListener('keydown', disableDevTools)
    document.addEventListener('copy', handleClipboardChange)
    document.addEventListener('cut', handleClipboardChange)
    document.addEventListener('contextmenu', disableRightClick)
    fetchClientIp()
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.removeEventListener('keydown', disableDevTools)
  document.removeEventListener('copy', handleClipboardChange)
  document.removeEventListener('cut', handleClipboardChange)
  document.removeEventListener('contextmenu', disableRightClick)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.security-alert-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }

  :deep(.el-dialog__body) {
    padding: 32px 24px 16px;
    text-align: center;
  }

  :deep(.el-dialog__footer) {
    padding: 0 24px 24px;
    text-align: center;
  }
}

.alert-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.alert-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--dt-warning-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;

  .material-icons-outlined {
    font-size: 32px;
    color: var(--dt-warning-color);
  }
}

.alert-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0 0 8px;
}

.alert-message {
  font-size: 14px;
  color: var(--dt-text-secondary);
  margin: 0 0 20px;
  line-height: 1.5;
}

.alert-info {
  width: 100%;
  background: var(--dt-bg-card-hover);
  border-radius: var(--dt-radius-md);
  padding: 12px 16px;
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 13px;

  &:not(:last-child) {
    border-bottom: 1px solid var(--dt-border-lighter);
  }
}

.info-label {
  color: var(--dt-text-tertiary);
}

.info-value {
  color: var(--dt-text-primary);
  font-weight: 500;
}
</style>