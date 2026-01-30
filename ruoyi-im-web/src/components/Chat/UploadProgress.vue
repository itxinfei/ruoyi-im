<template>
  <div v-if="visible" class="upload-progress-container">
    <div class="upload-progress-header">
      <div class="upload-info">
        <span class="material-icons-outlined upload-icon">cloud_upload</span>
        <span class="upload-title">正在上传...</span>
        <span class="upload-count">({{ totalCount }}/{{ totalCount }})</span>
      </div>
      <button v-if="cancellable" class="cancel-btn" @click="handleCancel">
        <span class="material-icons-outlined">close</span>
      </button>
    </div>

    <!-- 总体进度条 -->
    <div class="progress-section">
      <div class="progress-bar-wrapper">
        <div class="progress-bar" :style="{ width: overallProgress + '%' }">
          <div class="progress-bar-shine"></div>
        </div>
      </div>
      <div class="progress-text">{{ overallProgress }}%</div>
    </div>

    <!-- 详细信息 -->
    <div class="upload-details">
      <div class="detail-item">
        <span class="detail-label">当前文件:</span>
        <span class="detail-value">{{ currentFileName }}</span>
      </div>
      <div class="detail-item">
        <span class="detail-label">上传速度:</span>
        <span class="detail-value">{{ uploadSpeed }}</span>
      </div>
      <div class="detail-item">
        <span class="detail-label">剩余时间:</span>
        <span class="detail-value">{{ remainingTime }}</span>
      </div>
      <div class="detail-item">
        <span class="detail-label">已上传:</span>
        <span class="detail-value">{{ uploadedSize }} / {{ totalSize }}</span>
      </div>
    </div>

    <!-- 文件列表 -->
    <div v-if="showFileList" class="file-list">
      <div
        v-for="(file, index) in files"
        :key="index"
        class="file-item"
        :class="{ 'is-uploading': file.status === 'uploading', 'is-completed': file.status === 'completed', 'is-failed': file.status === 'failed' }"
      >
        <div class="file-status-icon">
          <span v-if="file.status === 'uploading'" class="material-icons-outlined is-loading">sync</span>
          <span v-else-if="file.status === 'completed'" class="material-icons-outlined">check_circle</span>
          <span v-else-if="file.status === 'failed'" class="material-icons-outlined">error</span>
        </div>
        <div class="file-info">
          <div class="file-name">{{ file.name }}</div>
          <div class="file-progress">
            <div class="mini-progress-bar">
              <div class="mini-progress-fill" :style="{ width: file.progress + '%' }"></div>
            </div>
            <span class="file-progress-text">{{ file.progress }}%</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'

/**
 * 上传进度显示组件
 * 显示文件上传的进度、速度和剩余时间
 */

const props = defineProps({
  // 是否显示
  visible: {
    type: Boolean,
    default: false
  },
  // 文件列表
  files: {
    type: Array,
    default: () => []
  },
  // 总字节数
  totalBytes: {
    type: Number,
    default: 0
  },
  // 已上传字节数
  uploadedBytes: {
    type: Number,
    default: 0
  },
  // 是否可以取消
  cancellable: {
    type: Boolean,
    default: true
  },
  // 是否显示文件列表
  showFileList: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['cancel'])

// 计算属性
const totalCount = computed(() => props.files.length)
const overallProgress = computed(() => {
  if (props.totalBytes === 0) return 0
  return Math.round((props.uploadedBytes / props.totalBytes) * 100)
})

const currentFileName = computed(() => {
  const uploadingFile = props.files.find(f => f.status === 'uploading')
  return uploadingFile ? uploadingFile.name : '准备中...'
})

// 速度计算
const lastUploadedBytes = ref(0)
const lastUpdateTime = ref(Date.now())
const currentSpeed = ref(0)

// 更新速度（每秒计算一次）
let speedUpdateTimer = null

watch(() => props.uploadedBytes, (newBytes) => {
  const now = Date.now()
  const timeDiff = (now - lastUpdateTime.value) / 1000 // 秒
  const bytesDiff = newBytes - lastUploadedBytes.value

  if (timeDiff > 0) {
    currentSpeed.value = bytesDiff / timeDiff
  }

  lastUploadedBytes.value = newBytes
  lastUpdateTime.value = now
})

// 启动速度更新定时器
function startSpeedUpdateTimer() {
  stopSpeedUpdateTimer()
  speedUpdateTimer = setInterval(() => {
    // 如果没有上传进度，速度归零
    if (props.uploadedBytes === lastUploadedBytes.value) {
      currentSpeed.value = 0
    }
  }, 1000)
}

function stopSpeedUpdateTimer() {
  if (speedUpdateTimer) {
    clearInterval(speedUpdateTimer)
    speedUpdateTimer = null
  }
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    startSpeedUpdateTimer()
  } else {
    stopSpeedUpdateTimer()
  }
})

onUnmounted(() => {
  stopSpeedUpdateTimer()
})

// 格式化速度
const uploadSpeed = computed(() => {
  if (currentSpeed.value === 0) return '0 B/s'
  return formatBytes(currentSpeed.value) + '/s'
})

// 计算剩余时间
const remainingTime = computed(() => {
  if (currentSpeed.value === 0 || props.totalBytes === 0) {
    return '计算中...'
  }

  const remainingBytes = props.totalBytes - props.uploadedBytes
  const remainingSeconds = remainingBytes / currentSpeed.value

  if (remainingSeconds < 60) {
    return Math.round(remainingSeconds) + '秒'
  } else if (remainingSeconds < 3600) {
    return Math.round(remainingSeconds / 60) + '分钟'
  } else {
    const hours = Math.floor(remainingSeconds / 3600)
    const minutes = Math.round((remainingSeconds % 3600) / 60)
    return `${hours}小时${minutes}分钟`
  }
})

// 格式化字节数
const uploadedSize = computed(() => formatBytes(props.uploadedBytes))
const totalSize = computed(() => formatBytes(props.totalBytes))

function formatBytes(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

// 取消上传
function handleCancel() {
  emit('cancel')
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.upload-progress-container {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-lg);
  padding: 16px;
  box-shadow: var(--dt-shadow-3);
}

.upload-progress-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.upload-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-icon {
  font-size: 20px;
  color: var(--dt-brand-color);
}

.upload-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
}

.upload-count {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.cancel-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--dt-text-tertiary);
  cursor: pointer;
  border-radius: 50%;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-primary);
  }

  .material-icons-outlined {
    font-size: 18px;
  }
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.progress-bar-wrapper {
  flex: 1;
  height: 8px;
  background: var(--dt-bg-body);
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--dt-brand-color), #4facfe);
  border-radius: 4px;
  position: relative;
  transition: width 0.3s ease;
}

.progress-bar-shine {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shine 1.5s infinite;
}

@keyframes shine {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.progress-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-brand-color);
  min-width: 45px;
  text-align: right;
}

.upload-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 12px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-md);
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.detail-value {
  font-size: 13px;
  font-weight: 500;
  color: var(--dt-text-primary);
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: 2px;
  }
}

.file-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-md);
  transition: all var(--dt-transition-fast);

  &.is-uploading {
    border-color: var(--dt-brand-color);
    background: rgba(0, 137, 255, 0.02);
  }

  &.is-completed {
    border-color: var(--dt-color-success);
    background: rgba(82, 196, 26, 0.02);
  }

  &.is-failed {
    border-color: var(--dt-color-danger);
    background: rgba(239, 68, 68, 0.02);
  }
}

.file-status-icon {
  flex-shrink: 0;
  width: 20px;
  height: 20px;

  .material-icons-outlined {
    font-size: 20px;

    &.is-loading {
      color: var(--dt-brand-color);
      animation: rotate 1s linear infinite;
    }
  }

  .is-completed & .material-icons-outlined {
    color: var(--dt-color-success);
  }

  .is-failed & .material-icons-outlined {
    color: var(--dt-color-danger);
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 13px;
  color: var(--dt-text-primary);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-progress-bar {
  flex: 1;
  height: 4px;
  background: var(--dt-bg-body);
  border-radius: 2px;
  overflow: hidden;
}

.mini-progress-fill {
  height: 100%;
  background: var(--dt-brand-color);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.file-progress-text {
  font-size: 11px;
  color: var(--dt-text-tertiary);
  min-width: 35px;
  text-align: right;
}

// 暗色模式
.dark {
  .upload-progress-container {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .progress-bar-wrapper {
    background: var(--dt-bg-body-dark);
  }

  .upload-details {
    background: var(--dt-bg-body-dark);
  }

  .file-item {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);

    &.is-uploading {
      border-color: var(--dt-brand-color);
      background: rgba(0, 137, 255, 0.1);
    }

    &.is-completed {
      border-color: var(--dt-color-success);
      background: rgba(82, 196, 26, 0.1);
    }

    &.is-failed {
      border-color: var(--dt-color-danger);
      background: rgba(239, 68, 68, 0.1);
    }
  }
}
</style>
