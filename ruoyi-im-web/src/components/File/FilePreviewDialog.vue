<template>
  <el-dialog
    v-model="visible"
    :title="fileInfo?.name || '文件预览'"
    :width="previewWidth"
    :fullscreen="isFullscreen"
    :close-on-click-modal="false"
    destroy-on-close
    class="file-preview-dialog"
    @close="handleClose"
  >
    <!-- 工具栏 -->
    <template #header="{ close }">
      <div class="preview-header">
        <div class="file-info">
          <el-icon :size="20" class="file-icon">
            <Document />
          </el-icon>
          <span class="file-name">{{ fileInfo?.name || '文件预览' }}</span>
          <el-tag v-if="fileInfo" size="small" type="info">{{ formatFileSize(fileInfo.size) }}</el-tag>
        </div>
        <div class="header-actions">
          <el-button
            v-if="canDownload"
            :icon="Download"
            circle
            size="small"
            title="下载"
            @click="handleDownload"
          />
          <el-button
            :icon="isFullscreen ? Switch :FullScreen"
            circle
            size="small"
            :title="isFullscreen ? '退出全屏' : '全屏'"
            @click="toggleFullscreen"
          />
          <el-button
            :icon="Close"
            circle
            size="small"
            title="关闭"
            @click="close"
          />
        </div>
      </div>
    </template>

    <!-- 加载状态 -->
    <div v-if="loading" class="preview-loading">
      <el-icon :size="40" class="is-loading"><Loading /></el-icon>
      <p>正在加载预览...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="preview-error">
      <el-icon :size="60"><WarningFilled /></el-icon>
      <p>{{ error }}</p>
      <el-button type="primary" @click="handleDownload">下载文件</el-button>
    </div>

    <!-- 预览内容 -->
    <div v-else class="preview-content" :class="[`preview-type-${previewType}`, { fullscreen: isFullscreen }]">
      <!-- 图片预览 -->
      <template v-if="previewType === 'image'">
        <div class="image-preview">
          <el-image
            :src="previewUrl"
            :preview-src-list="[previewUrl]"
            fit="contain"
            class="preview-image"
          >
            <template #error>
              <div class="image-error">
                <el-icon :size="60"><PictureFilled /></el-icon>
                <p>图片加载失败</p>
              </div>
            </template>
          </el-image>
        </div>
      </template>

      <!-- PDF预览 -->
      <template v-else-if="previewType === 'pdf'">
        <div class="pdf-preview">
          <iframe
            v-if="previewUrl"
            :src="previewUrl"
            class="pdf-iframe"
            frameborder="0"
          ></iframe>
          <div v-else class="preview-placeholder">
            <p>PDF预览服务暂不可用</p>
            <el-button type="primary" @click="handleDownload">下载文件</el-button>
          </div>
        </div>
      </template>

      <!-- Office文档预览 -->
      <template v-else-if="previewType === 'office'">
        <div class="office-preview">
          <iframe
            v-if="officePreviewUrl"
            :src="officePreviewUrl"
            class="office-iframe"
            frameborder="0"
          ></iframe>
          <div v-else class="preview-placeholder">
            <el-icon :size="60"><Document /></el-icon>
            <p>Office文档预览需要服务器支持</p>
            <div class="office-types">
              <el-tag v-for="type in officeTypes" :key="type" size="small">{{ type }}</el-tag>
            </div>
            <el-button type="primary" @click="handleDownload">下载文件</el-button>
          </div>
        </div>
      </template>

      <!-- 视频预览 -->
      <template v-else-if="previewType === 'video'">
        <div class="video-preview">
          <video
            v-if="previewUrl"
            :src="previewUrl"
            controls
            autoplay
            class="preview-video"
            @error="handleVideoError"
          >
            您的浏览器不支持视频播放
          </video>
          <div v-else class="preview-placeholder">
            <el-icon :size="60"><VideoCamera /></el-icon>
            <p>视频加载失败</p>
            <el-button type="primary" @click="handleDownload">下载文件</el-button>
          </div>
        </div>
      </template>

      <!-- 音频预览 -->
      <template v-else-if="previewType === 'audio'">
        <div class="audio-preview">
          <div class="audio-cover">
            <el-icon :size="80"><Microphone /></el-icon>
          </div>
          <p class="audio-name">{{ fileInfo?.name }}</p>
          <audio
            v-if="previewUrl"
            :src="previewUrl"
            controls
            class="preview-audio"
          >
            您的浏览器不支持音频播放
          </audio>
        </div>
      </template>

      <!-- 代码预览 -->
      <template v-else-if="previewType === 'code'">
        <div class="code-preview">
          <pre><code>{{ codeContent }}</code></pre>
        </div>
      </template>

      <!-- 不支持的类型 -->
      <template v-else>
        <div class="preview-unsupported">
          <el-icon :size="60"><DocumentRemove /></el-icon>
          <p>此文件类型暂不支持预览</p>
          <p class="file-type-info">类型: {{ fileInfo?.type || '未知' }}</p>
          <el-button type="primary" @click="handleDownload">下载文件</el-button>
        </div>
      </template>
    </div>

    <!-- 分页控制 (PDF) -->
    <div v-if="previewType === 'pdf' && !error && !loading" class="preview-footer">
      <div class="page-controls">
        <el-button :icon="ArrowLeft" :disabled="currentPage <= 1" @click="prevPage">上一页</el-button>
        <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
        <el-button :disabled="currentPage >= totalPages" @click="nextPage">下一页<ArrowRight /></el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import {
  Document,
  Download,
  Close,
  FullScreen,
  Switch,
  Loading,
  WarningFilled,
  PictureFilled,
  VideoCamera,
  Microphone,
  DocumentRemove,
  ArrowLeft,
  ArrowRight,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getFilePreviewInfo, downloadFile, getPreviewUrl } from '@/api/im/file'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  fileInfo: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['update:modelValue', 'download'])

// 状态
const visible = ref(false)
const loading = ref(false)
const error = ref('')
const previewUrl = ref('')
const codeContent = ref('')
const isFullscreen = ref(false)
const currentPage = ref(1)
const totalPages = ref(1)

// Office支持的文件类型
const officeTypes = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx']

// 预览类型
const previewType = computed(() => {
  if (!props.fileInfo) return 'unsupported'

  const ext = getFileExtension(props.fileInfo.name)
  const mimeType = props.fileInfo.type || ''

  // 图片类型
  if (['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp', 'svg'].includes(ext) ||
      mimeType.startsWith('image/')) {
    return 'image'
  }

  // PDF
  if (ext === 'pdf' || mimeType === 'application/pdf') {
    return 'pdf'
  }

  // Office文档
  if (officeTypes.includes(ext) || mimeType.includes('office') ||
      mimeType.includes('word') || mimeType.includes('excel') ||
      mimeType.includes('powerpoint') || mimeType.includes('ms-')) {
    return 'office'
  }

  // 视频
  if (['mp4', 'webm', 'ogg', 'avi', 'mov', 'mkv'].includes(ext) ||
      mimeType.startsWith('video/')) {
    return 'video'
  }

  // 音频
  if (['mp3', 'wav', 'flac', 'aac', 'm4a'].includes(ext) ||
      mimeType.startsWith('audio/')) {
    return 'audio'
  }

  // 代码文件
  if (['js', 'ts', 'vue', 'jsx', 'tsx', 'html', 'css', 'scss', 'json',
       'xml', 'md', 'txt', 'py', 'java', 'c', 'cpp', 'go', 'rs', 'php'].includes(ext)) {
    return 'code'
  }

  return 'unsupported'
})

// Office预览URL (使用微软在线预览服务)
const officePreviewUrl = computed(() => {
  if (previewType.value !== 'office' || !previewUrl.value) return ''

  // 使用微软Office在线预览服务
  const encodedUrl = encodeURIComponent(previewUrl.value)
  return `https://view.officeapps.live.com/op/embed.aspx?src=${encodedUrl}`
})

// 预览宽度
const previewWidth = computed(() => {
  if (isFullscreen.value) return '100%'
  return previewType.value === 'image' ? '70%' : '80%'
})

// 是否可以下载
const canDownload = computed(() => {
  return previewType.value !== 'unsupported'
})

// 获取文件扩展名
function getFileExtension(filename) {
  if (!filename) return ''
  const parts = filename.split('.')
  return parts.length > 1 ? parts[parts.length - 1].toLowerCase() : ''
}

// 格式化文件大小
function formatFileSize(bytes) {
  if (!bytes) return '未知'
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0

  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }

  return `${size.toFixed(unitIndex > 0 ? 1 : 0)} ${units[unitIndex]}`
}

// 加载预览
async function loadPreview() {
  if (!props.fileInfo) return

  loading.value = true
  error.value = ''

  try {
    if (previewType.value === 'code') {
      // 对于代码文件，直接获取文件内容
      const response = await fetch(props.fileInfo.url)
      if (response.ok) {
        codeContent.value = await response.text()
      } else {
        throw new Error('无法加载文件内容')
      }
    } else {
      // 获取预览URL
      const { data } = await getFilePreviewInfo(props.fileInfo.id)

      if (data.code === 200 && data.data) {
        previewUrl.value = data.data.url || data.data.previewUrl || props.fileInfo.url
      } else {
        // 如果API不可用，直接使用文件URL
        previewUrl.value = props.fileInfo.url
      }
    }
  } catch (err) {
    console.error('加载预览失败:', err)
    // 降级：直接使用文件URL
    previewUrl.value = props.fileInfo.url
  } finally {
    loading.value = false
  }
}

// 下载文件
function handleDownload() {
  emit('download', props.fileInfo)
  if (props.fileInfo?.id) {
    downloadFile(props.fileInfo.id).then(blob => {
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = props.fileInfo.name
      link.click()
      window.URL.revokeObjectURL(url)
      ElMessage.success('下载成功')
    }).catch(() => {
      ElMessage.error('下载失败')
    })
  }
}

// 切换全屏
function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value
}

// PDF翻页
function prevPage() {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

// 视频加载错误
function handleVideoError() {
  error.value = '视频加载失败'
}

// 关闭对话框
function handleClose() {
  visible.value = false
  // 清理状态
  loading.value = false
  error.value = ''
  previewUrl.value = ''
  codeContent.value = ''
  currentPage.value = 1
}

// 监听对话框显示状态
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadPreview()
  }
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
    handleClose()
  }
})

// 监听文件变化
watch(() => props.fileInfo, () => {
  if (visible.value) {
    loadPreview()
  }
}, { deep: true })
</script>

<style lang="scss" scoped>
@import '@/styles/dingtalk-theme.scss';

.file-preview-dialog {
  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__footer) {
    padding: 0;
  }
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid $border-light;
  background: $bg-base;

  .file-info {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
    min-width: 0;

    .file-icon {
      color: $text-secondary;
      flex-shrink: 0;
    }

    .file-name {
      font-size: 15px;
      font-weight: 500;
      color: $text-primary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .header-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
  }
}

.preview-content {
  min-height: 400px;
  max-height: 70vh;
  overflow: auto;
  background: $bg-light;

  &.fullscreen {
    max-height: calc(100vh - 80px);
  }

  &.preview-type-image {
    display: flex;
    align-items: center;
    justify-content: center;
    background: #000;
  }

  &.preview-type-audio {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px;
  }
}

.preview-loading,
.preview-error,
.preview-unsupported,
.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: $text-secondary;

  .el-icon {
    margin-bottom: 16px;
    color: $text-tertiary;
  }

  p {
    margin: 8px 0 16px;
    font-size: 14px;
  }

  .office-types {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: center;
    margin-bottom: 16px;
  }
}

// 图片预览
.image-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;

  .preview-image {
    max-width: 100%;
    max-height: 70vh;
  }

  .image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: $text-tertiary;
  }
}

// PDF预览
.pdf-preview {
  width: 100%;
  height: 70vh;

  .pdf-iframe {
    width: 100%;
    height: 100%;
    border: none;
  }
}

// Office预览
.office-preview {
  width: 100%;
  height: 70vh;

  .office-iframe {
    width: 100%;
    height: 100%;
    border: none;
  }
}

// 视频预览
.video-preview {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;

  .preview-video {
    max-width: 100%;
    max-height: 70vh;
  }
}

// 音频预览
.audio-preview {
  text-align: center;

  .audio-cover {
    width: 200px;
    height: 200px;
    border-radius: 12px;
    background: linear-gradient(135deg, $primary-color 0%, $primary-color-light 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20px;

    .el-icon {
      color: white;
    }
  }

  .audio-name {
    font-size: 16px;
    font-weight: 500;
    color: $text-primary;
    margin-bottom: 16px;
  }

  .preview-audio {
    width: 100%;
    max-width: 400px;
  }
}

// 代码预览
.code-preview {
  padding: 20px;

  pre {
    margin: 0;
    padding: 16px;
    background: #282c34;
    border-radius: 8px;
    overflow: auto;
    max-height: 70vh;

    code {
      font-family: 'Fira Code', 'Consolas', monospace;
      font-size: 13px;
      line-height: 1.6;
      color: #abb2bf;
    }
  }
}

.file-type-info {
  font-size: 12px;
  color: $text-tertiary;
}

// 页脚控制
.preview-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px 20px;
  border-top: 1px solid $border-light;
  background: $bg-base;

  .page-controls {
    display: flex;
    align-items: center;
    gap: 16px;

    .page-info {
      font-size: 14px;
      color: $text-secondary;
      min-width: 80px;
      text-align: center;
    }
  }
}
</style>
