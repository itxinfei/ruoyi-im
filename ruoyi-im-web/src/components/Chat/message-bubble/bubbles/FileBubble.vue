<template>
  <div
    class="file-bubble"
    :class="{ 'is-downloading': isDownloading, 'is-uploading': isUploading }"
    @click="handleClick"
  >
    <!-- 文件图标 -->
    <div class="file-icon">
      <el-icon><Document /></el-icon>

      <!-- 下载进度环 -->
      <div
        v-if="isDownloading"
        class="progress-ring"
      >
        <svg viewBox="0 0 36 36">
          <path
            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
            fill="none"
            stroke="rgba(0, 137, 255, 0.15)"
            stroke-width="3"
          />
          <path
            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
            fill="none"
            stroke="var(--dt-brand-color)"
            stroke-width="3"
            :stroke-dasharray="`${downloadProgress}, 100`"
            stroke-linecap="round"
          />
        </svg>
      </div>

      <!-- 上传进度环 -->
      <div
        v-if="isUploading"
        class="progress-ring"
      >
        <svg viewBox="0 0 36 36">
          <path
            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
            fill="none"
            stroke="rgba(0, 137, 255, 0.15)"
            stroke-width="3"
          />
          <path
            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
            fill="none"
            stroke="var(--dt-brand-color)"
            stroke-width="3"
            stroke-dasharray="100"
            stroke-dashoffset="25"
            stroke-linecap="round"
            class="upload-spinner"
          />
        </svg>
      </div>
    </div>

    <!-- 文件信息 -->
    <div class="file-info">
      <div class="file-name">
        {{ fileName }}
      </div>
      <div class="file-meta">
        <template v-if="isUploading">
          上传中...
        </template>
        <template v-else-if="isDownloading">
          下载中 {{ downloadProgress }}%
        </template>
        <template v-else>
          {{ fileSize }}
        </template>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="file-action">
      <el-icon v-if="!isUploading && !isDownloading">
        <Download />
      </el-icon>
      <el-icon
        v-else
        class="is-spinning"
      >
        <Loading />
      </el-icon>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Document, Download, Loading } from '@element-plus/icons-vue'
import { parseMessageContent } from '@/utils/message'
import { formatFileSize } from '@/utils/format'

const props = defineProps({
  message: { type: Object, required: true }
})

const emit = defineEmits(['download'])

const isDownloading = ref(false)
const downloadProgress = ref(0)

const parsedContent = computed(() => parseMessageContent(props.message) || {})

const fileName = computed(() => {
  return parsedContent.value.fileName || parsedContent.value.name || '未知文件'
})

const fileSize = computed(() => {
  return formatFileSize(parsedContent.value.size || 0)
})

const isUploading = computed(() => {
  return ['uploading', 'sending'].includes(props.message?.status)
})

const fileUrl = computed(() => {
  return parsedContent.value.fileUrl || parsedContent.value.url || ''
})

const handleClick = async () => {
  if (isDownloading.value || isUploading.value) {return}
  if (!fileUrl.value) {
    emit('download', parsedContent.value)
    return
  }

  try {
    isDownloading.value = true
    downloadProgress.value = 0

    const progressInterval = setInterval(() => {
      if (downloadProgress.value < 90) {
        downloadProgress.value += Math.random() * 15
      }
    }, 200)

    const response = await fetch(fileUrl.value)
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = url
    link.download = fileName.value
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    clearInterval(progressInterval)
    downloadProgress.value = 100

    setTimeout(() => {
      isDownloading.value = false
      downloadProgress.value = 0
    }, 500)
  } catch (error) {
    console.error('文件下载失败:', error)
    isDownloading.value = false
    downloadProgress.value = 0
    emit('download', parsedContent.value)
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.file-bubble {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);
  min-width: 240px;
  max-width: 400px;

  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: var(--dt-shadow-card);

    .file-action {
      opacity: 1;
      transform: translateX(0);
    }

    .file-icon .el-icon {
      transform: scale(1.05);
    }
  }

  &.is-downloading {
    border-color: var(--dt-brand-color);
    background: var(--dt-brand-bg);
    cursor: wait;

    .file-icon {
      animation: pulse 1.5s ease-in-out infinite;
    }
  }

  &.is-uploading {
    cursor: wait;
  }
}

.file-icon {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .el-icon {
    font-size: 32px;
    color: var(--dt-brand-color);
    transition: transform var(--dt-transition-base);
  }

  .progress-ring {
    position: absolute;
    inset: -4px;
    display: flex;
    align-items: center;
    justify-content: center;

    svg {
      width: 48px;
      height: 48px;
      transform: rotate(-90deg);
    }

    .upload-spinner {
      animation: spin 1.5s linear infinite;
    }
  }
}

.file-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-weight: 600;
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);
}

.file-action {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  color: var(--dt-text-tertiary);
  opacity: 0.6;
  transition: all var(--dt-transition-base);
  transform: translateX(4px);
  flex-shrink: 0;

  .el-icon {
    font-size: 18px;
  }

  &.is-spinning .el-icon {
    animation: spin 1s linear infinite;
    color: var(--dt-brand-color);
  }
}

</style>
