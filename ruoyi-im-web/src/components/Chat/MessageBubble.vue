<template>
  <div class="message-bubble" :class="{ 'is-own': message.isOwn }">
    <!-- 文本消息 -->
    <div v-if="message.type === 'TEXT'" class="text-content">
      <template v-for="(part, index) in parsedTextParts" :key="index">
        <a v-if="part.isLink" :href="part.text" target="_blank" class="text-link">{{ part.text }}</a>
        <span v-else>{{ part.text }}</span>
      </template>
    </div>

    <!-- 链接卡片消息 -->
    <LinkCardMessage v-else-if="message.type === 'LINK'" :message="message" />

    <!-- 图片消息 -->
    <div v-else-if="message.type === 'IMAGE'" class="image-content">
      <!-- 上传中 -->
      <div v-if="message.status === 'uploading'" class="upload-progress">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>上传中... {{ message.progress || 0 }}%</span>
      </div>
      <!-- 上传失败 -->
      <div v-else-if="message.status === 'failed'" class="upload-failed">
        <el-icon><WarningFilled /></el-icon>
        <span>上传失败</span>
      </div>
      <!-- 正常显示 -->
      <el-image
        v-else
        :src="getImageUrl(message)"
        :preview-src-list="[getImageUrl(message)]"
        fit="cover"
        class="img"
      />
    </div>

    <!-- 文件消息 -->
    <div v-else-if="message.type === 'FILE'" class="file-content">
      <!-- 上传中 -->
      <div v-if="message.status === 'uploading'" class="file-uploading">
        <el-icon><Document /></el-icon>
        <div class="file-info">
          <div class="file-name">{{ message.fileName || '未知文件' }}</div>
          <div class="file-meta">
            <el-progress :percentage="message.progress || 0" :show-text="false" :stroke-width="2" />
            <span class="file-size">{{ message.fileSize || '0 B' }}</span>
          </div>
        </div>
      </div>
      <!-- 上传失败 -->
      <div v-else-if="message.status === 'failed'" class="file-failed">
        <el-icon><WarningFilled /></el-icon>
        <div class="file-info">
          <div class="file-name">{{ message.fileName || '未知文件' }}</div>
          <div class="file-meta">上传失败</div>
        </div>
      </div>
      <!-- 正常显示 -->
      <div v-else class="file-normal" @click="handleFileDownload">
        <div class="file-icon" :class="getFileIconClass(getFileName(message))">
          <el-icon :size="24"><component :is="getFileIcon(getFileName(message))" /></el-icon>
        </div>
        <div class="file-info">
          <div class="file-name">{{ getFileName(message) }}</div>
          <div class="file-meta">
            <span class="file-size">{{ getFileSize(message) }}</span>
            <span class="file-divider">·</span>
            <span class="file-action">点击下载</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 文档消息 -->
    <div v-else-if="message.type === 'DOCUMENT'" class="document-content" @click="handleDocumentClick">
      <div class="doc-card">
        <div class="doc-icon" :class="getDocumentIconClass(message.documentType)">
          <span class="material-icons-outlined">{{ getDocumentIcon(message.documentType) }}</span>
        </div>
        <div class="doc-info">
          <div class="doc-title">{{ message.documentTitle || '未知文档' }}</div>
          <div class="doc-meta">
            <span>{{ message.documentCreator || '我' }}</span>
            <span>·</span>
            <span>{{ formatTime(message.createTime) }}</span>
          </div>
        </div>
        <div class="doc-action">
          <span class="material-icons-outlined">open_in_new</span>
        </div>
      </div>
    </div>

    <!-- 撤回消息 -->
    <div v-else-if="message.type === 'RECALLED'" class="recalled">
      消息已撤回
    </div>

    <!-- 兜底 -->
    <div v-else class="other-content">
      [不支持的消息类型]
    </div>

    <!-- 文档预览抽屉 -->
    <DocumentPreviewDrawer
      ref="documentPreviewRef"
      @close="handlePreviewClose"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Document, Loading, WarningFilled, DocumentCopy, Picture, VideoCamera, Headset, Grid, Tickets } from '@element-plus/icons-vue'
import DocumentPreviewDrawer from './DocumentPreviewDrawer.vue'
import LinkCardMessage from './LinkCardMessage.vue'
import { formatTime, formatFileSize } from '@/utils/format'

const props = defineProps({
  message: Object
})

const documentPreviewRef = ref(null)

// URL 正则匹配
const urlPattern = /(https?:\/\/[^\s<]+[^<.,:;"')\]\s])/g

// 解析文本中的链接
const parsedTextParts = computed(() => {
  const content = props.message?.content || ''
  if (!content) return []

  const parts = []
  let lastIndex = 0
  let match

  while ((match = urlPattern.exec(content)) !== null) {
    // 添加链接前的文本
    if (match.index > lastIndex) {
      parts.push({ text: content.slice(lastIndex, match.index), isLink: false })
    }
    // 添加链接
    parts.push({ text: match[0], isLink: true })
    lastIndex = match.index + match[0].length
  }

  // 添加剩余文本
  if (lastIndex < content.length) {
    parts.push({ text: content.slice(lastIndex), isLink: false })
  }

  return parts
})

// 获取文档图标
const getDocumentIcon = (type) => {
  const icons = {
    'TEXT': 'description',
    'FILE': 'description',
    'IMAGE': 'image',
    'VIDEO': 'video_library',
    'VOICE': 'mic'
  }
  return icons[type] || 'description'
}

// 获取文档图标样式
const getDocumentIconClass = (type) => {
  const classes = {
    'TEXT': 'icon-doc',
    'FILE': 'icon-doc',
    'IMAGE': 'icon-image',
    'VIDEO': 'icon-video',
    'VOICE': 'icon-voice'
  }
  return classes[type] || 'icon-doc'
}

// 点击文档消息
const handleDocumentClick = () => {
  if (props.message.documentId) {
    documentPreviewRef.value?.open(props.message.documentId)
  }
}

// 预览关闭回调
const handlePreviewClose = () => {
  // 可以在这里处理预览关闭后的逻辑
}

// 获取图片URL
const getImageUrl = (msg) => {
  try {
    const content = JSON.parse(msg.content)
    return content.fileUrl || content.thumbnailUrl || content.url || msg.content
  } catch (e) {
    return msg.content
  }
}

// 获取文件名
const getFileName = (msg) => {
  try {
    const content = JSON.parse(msg.content)
    return content.fileName || '未知文件'
  } catch (e) {
    return msg.fileName || '未知文件'
  }
}

// 获取文件大小
const getFileSize = (msg) => {
  try {
    const content = JSON.parse(msg.content)
    return formatFileSize(content.fileSize || 0)
  } catch (e) {
    return formatFileSize(msg.fileSize || 0)
  }
}

// 获取文件图标组件
const getFileIcon = (fileName) => {
  const ext = fileName?.split('.').pop()?.toLowerCase() || ''
  const iconMap = {
    // 图片
    'jpg': Picture, 'jpeg': Picture, 'png': Picture, 'gif': Picture, 'webp': Picture, 'svg': Picture,
    // 视频
    'mp4': VideoCamera, 'avi': VideoCamera, 'mov': VideoCamera, 'wmv': VideoCamera, 'flv': VideoCamera,
    // 音频
    'mp3': Headset, 'wav': Headset, 'ogg': Headset, 'flac': Headset, 'aac': Headset,
    // 文档
    'pdf': DocumentCopy, 'doc': DocumentCopy, 'docx': DocumentCopy, 'txt': Tickets,
    // 表格
    'xls': Grid, 'xlsx': Grid, 'csv': Grid,
    // 演示
    'ppt': DocumentCopy, 'pptx': DocumentCopy
  }
  return iconMap[ext] || Document
}

// 获取文件图标样式类
const getFileIconClass = (fileName) => {
  const ext = fileName?.split('.').pop()?.toLowerCase() || ''
  const classMap = {
    // 图片
    'jpg': 'icon-image', 'jpeg': 'icon-image', 'png': 'icon-image', 'gif': 'icon-image',
    // 视频
    'mp4': 'icon-video', 'avi': 'icon-video', 'mov': 'icon-video',
    // 音频
    'mp3': 'icon-audio', 'wav': 'icon-audio', 'ogg': 'icon-audio',
    // 文档
    'pdf': 'icon-pdf', 'doc': 'icon-doc', 'docx': 'icon-doc', 'txt': 'icon-text',
    // 表格
    'xls': 'icon-sheet', 'xlsx': 'icon-sheet', 'csv': 'icon-sheet',
    // 演示
    'ppt': 'icon-ppt', 'pptx': 'icon-ppt'
  }
  return classMap[ext] || 'icon-default'
}
const handleFileDownload = () => {
  try {
    const content = JSON.parse(props.message.content)
    const fileUrl = content.fileUrl || content.url
    if (fileUrl) {
      window.open(fileUrl, '_blank')
    }
  } catch (e) {
    console.error('文件下载失败:', e)
  }
}
</script>

<style scoped lang="scss">
.message-bubble {
  padding: var(--dt-bubble-padding-v) var(--dt-bubble-padding-h);
  border-radius: var(--dt-bubble-radius);
  font-size: var(--dt-font-size-base);
  line-height: 1.5;
  word-break: break-all;
  background: var(--dt-bubble-left-bg);
  border: 1px solid var(--dt-border-light);
  color: var(--dt-text-primary);
  transition: all var(--dt-transition-fast);
  max-width: 520px;

  &.is-own {
    background: var(--dt-bubble-right-bg);
    border-color: var(--dt-border-light);
  }

  &:hover {
    box-shadow: var(--dt-shadow-1);
  }

  .text-link {
    color: var(--dt-brand-color);
    text-decoration: none;
    word-break: break-all;

    &:hover {
      text-decoration: underline;
    }
  }
}

.image-content {
  border-radius: var(--dt-radius-md);
  overflow: hidden;
  .img { max-width: 240px; display: block; }
}

.upload-progress {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  padding: var(--dt-spacing-md);
  color: var(--dt-text-secondary);
  .el-icon { font-size: 16px; }
}

.upload-failed {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  padding: var(--dt-spacing-md);
  color: var(--dt-error-color);
  .el-icon { font-size: 16px; }
}

.file-content {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  color: var(--dt-brand-color);
}

.file-uploading,
.file-failed,
.file-normal {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
  padding: var(--dt-spacing-md);
  border-radius: var(--dt-radius-md);
  transition: all var(--dt-transition-fast);
}

.file-uploading {
  background: var(--dt-bg-body);
  color: var(--dt-text-secondary);
}

.file-failed {
  background: var(--dt-error-bg);
  color: var(--dt-error-color);
}

.file-normal {
  cursor: pointer;
  min-width: 200px;

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  .file-icon {
    width: 44px;
    height: 44px;
    border-radius: var(--dt-radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &.icon-image { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
    &.icon-video { background: var(--dt-warning-bg); color: var(--dt-warning-color); }
    &.icon-audio { background: var(--dt-success-bg); color: var(--dt-success-color); }
    &.icon-pdf { background: var(--dt-error-bg); color: var(--dt-error-color); }
    &.icon-doc { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
    &.icon-sheet { background: var(--dt-success-bg); color: var(--dt-success-color); }
    &.icon-ppt { background: var(--dt-warning-bg); color: var(--dt-warning-color); }
    &.icon-text { background: var(--dt-bg-body); color: var(--dt-text-secondary); }
    &.icon-default { background: var(--dt-bg-body); color: var(--dt-text-secondary); }
  }
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-name {
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  text-decoration: none;
}

.file-uploading .file-name,
.file-failed .file-name {
  color: inherit;
}

.file-meta {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-xs);

  .file-divider {
    color: var(--dt-text-quaternary);
  }

  .file-action {
    color: var(--dt-brand-color);
    font-weight: var(--dt-font-weight-medium);
  }
}

.file-size {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
}

.file-normal .file-meta {
  color: var(--dt-brand-color);
}

.document-content {
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    transform: translateY(-1px);
    box-shadow: var(--dt-shadow-2);
  }

  .doc-card {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-md);
    padding: var(--dt-spacing-md);
    background: var(--dt-bg-body);
    border-radius: var(--dt-radius-md);
    min-width: 240px;

    .doc-icon {
      width: 40px;
      height: 40px;
      border-radius: var(--dt-radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;

      &.icon-doc { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
      &.icon-image { background: var(--dt-brand-lighter); color: var(--dt-brand-color); }
      &.icon-video { background: var(--dt-error-bg); color: var(--dt-error-color); }
      &.icon-voice { background: var(--dt-warning-bg); color: var(--dt-warning-color); }
    }

    .doc-info {
      flex: 1;

      .doc-title {
        font-size: var(--dt-font-size-base);
        font-weight: var(--dt-font-weight-medium);
        color: var(--dt-text-primary);
        margin-bottom: 4px;
      }

      .doc-meta {
        font-size: var(--dt-font-size-sm);
        color: var(--dt-text-tertiary);
      }
    }

    .doc-action {
      color: var(--dt-text-tertiary);
    }
  }
}

.recalled {
  color: var(--dt-text-tertiary);
  font-size: var(--dt-font-size-sm);
}

// 暗色模式适配
:global(.dark) {
  .message-bubble {
    background: var(--dt-bg-hover-dark);
    border-color: var(--dt-border-dark);
    color: var(--dt-text-primary-dark);

    &.is-own {
      background: var(--dt-brand-active);
      color: var(--dt-text-primary);
    }
  }

  .document-content .doc-card {
    background: var(--dt-bg-hover-dark);

    .doc-info .doc-title {
      color: var(--dt-text-primary-dark);
    }

    .doc-info .doc-meta {
      color: var(--dt-text-tertiary-dark);
    }

    .doc-action {
      color: var(--dt-text-tertiary-dark);
    }
  }
}
</style>
