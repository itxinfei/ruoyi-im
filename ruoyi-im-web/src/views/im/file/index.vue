<template>
  <div class="file-container">
    <!-- 左侧文件列表面板 -->
    <div class="file-list-panel">
      <div class="panel-header">
        <div class="search-container">
          <el-input
            v-model="searchText"
            placeholder="搜索文件"
            :prefix-icon="Search"
            clearable
            class="search-input"
            @input="handleSearch"
          />
        </div>
        <el-dropdown @command="handleUploadType">
          <el-button type="primary" :icon="Upload">上传</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="file" :icon="Upload"> 上传文件 </el-dropdown-item>
              <el-dropdown-item command="folder" :icon="Folder"> 上传文件夹 </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- 文件分类标签 -->
      <div class="file-tabs">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
          <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
        </div>
      </div>

      <!-- 文件列表 -->
      <div class="file-list">
        <div
          v-for="file in currentFiles"
          :key="file.fileId"
          class="file-item"
          :class="{ selected: selectedFile?.fileId === file.fileId }"
          @click="selectFile(file)"
        >
          <div class="file-icon">
            <el-icon :size="24">
              <component :is="getFileIconComponent(file.fileType)" />
            </el-icon>
          </div>
          <div class="file-info">
            <div class="file-name" :title="file.fileName">
              {{ file.fileName }}
            </div>
            <div class="file-meta">
              <span class="file-size">{{ formatFileSize(file.fileSize) }}</span>
              <span class="separator">•</span>
              <span class="upload-time">{{ formatTime(file.uploadTime) }}</span>
            </div>
          </div>
          <div class="file-actions">
            <el-dropdown trigger="click" @command="handleFileAction($event, file)">
              <el-button link :icon="More" size="default" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="preview" :icon="View"> 颼览 </el-dropdown-item>
                  <el-dropdown-item command="download" :icon="Download"> 下载 </el-dropdown-item>
                  <el-dropdown-item command="share" :icon="Share"> 分享 </el-dropdown-item>
                  <el-dropdown-item command="copyLink" :icon="Link"> 复制链接 </el-dropdown-item>
                  <el-dropdown-item divided command="rename" :icon="Edit">
                    重命名
                  </el-dropdown-item>
                  <el-dropdown-item command="move" :icon="FolderOpened"> 移动到 </el-dropdown-item>
                  <el-dropdown-item command="copy" :icon="CopyDocument"> 复制到 </el-dropdown-item>
                  <el-dropdown-item divided command="delete" :icon="Delete">
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <el-empty
          v-if="currentFiles.length === 0"
          :description="searchText ? '没有找到相关文件' : '暂无文件'"
        />
      </div>
    </div>

    <!-- 右侧详情区域 -->
    <div class="detail-panel">
      <template v-if="selectedFile">
        <!-- 详情头部 -->
        <div class="detail-header">
          <div class="file-preview">
            <el-icon :size="48">
              <component :is="getFileIconComponent(selectedFile.fileType)" />
            </el-icon>
          </div>
          <div class="header-info">
            <h2 class="file-name" :title="selectedFile.fileName">{{ selectedFile.fileName }}</h2>
            <p class="file-meta">
              <span>{{ formatFileSize(selectedFile.fileSize) }}</span>
              <span class="separator">•</span>
              <span>{{ selectedFile.uploadBy || '未知' }}</span>
              <span class="separator">•</span>
              <span>{{ formatTime(selectedFile.uploadTime) }}</span>
            </p>
          </div>
          <div class="header-actions">
            <el-button :icon="Download" size="default" @click="handleDownload(selectedFile)"
              >下载</el-button
            >
            <el-button type="primary" size="default" @click="handlePreview(selectedFile)"
              >预览</el-button
            >
          </div>
        </div>

        <!-- 详情内容 -->
        <div class="detail-content">
          <div class="info-section">
            <h4>文件信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">文件名</span>
                <span class="value">{{ selectedFile.fileName }}</span>
              </div>
              <div class="info-item">
                <span class="label">文件大小</span>
                <span class="value">{{ formatFileSize(selectedFile.fileSize) }}</span>
              </div>
              <div class="info-item">
                <span class="label">文件类型</span>
                <span class="value">{{ selectedFile.fileType || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="label">上传者</span>
                <span class="value">{{ selectedFile.uploadBy || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="label">上传时间</span>
                <span class="value">{{ formatTime(selectedFile.uploadTime) }}</span>
              </div>
              <div class="info-item">
                <span class="label">修改时间</span>
                <span class="value">{{
                  formatTime(selectedFile.updateTime || selectedFile.uploadTime)
                }}</span>
              </div>
            </div>
          </div>

          <!-- 快速操作 -->
          <div class="info-section">
            <h4>快速操作</h4>
            <div class="quick-actions">
              <el-button :icon="Share" size="default" @click="handleShare(selectedFile)"
                >分享</el-button
              >
              <el-button :icon="Link" size="default" @click="handleCopyLink(selectedFile)"
                >复制链接</el-button
              >
              <el-button :icon="Edit" size="default" @click="handleRename(selectedFile)"
                >重命名</el-button
              >
            </div>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <div v-else class="empty-detail">
        <div class="empty-content">
          <el-icon :size="64" class="empty-icon"><Document /></el-icon>
          <h3 class="empty-title">选择一个文件查看详细信息</h3>
          <p class="empty-description">点击左侧文件列表中的文件，查看详细信息并进行操作</p>
        </div>
      </div>
    </div>

    <!-- 文件上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传文件" width="500px" destroy-on-close>
      <el-upload
        class="upload-demo"
        drag
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleFileUploadSuccess"
        :on-error="handleFileUploadError"
        :before-upload="beforeUpload"
        multiple
      >
        <el-icon class="el-icon--upload" :size="32"><UploadFilled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">支持任意类型文件，单个文件不超过100MB</div>
        </template>
      </el-upload>
    </el-dialog>

    <!-- 文件预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="文件预览" width="60%" destroy-on-close>
      <div v-loading="previewLoading" class="preview-container">
        <div v-if="isImage(currentFile)" class="image-preview">
          <el-image :src="currentFile?.url" :preview-src-list="[currentFile?.url]" fit="contain" />
        </div>
        <div v-else-if="isText(currentFile)" class="text-preview">
          <pre>{{ previewContent }}</pre>
        </div>
        <div v-else class="no-preview">
          <el-icon :size="48">
            <component :is="getFileIconComponent(currentFile?.fileType)" />
          </el-icon>
          <p>该文件类型暂不支持预览，请下载后查看</p>
          <el-button type="primary" @click="handleDownload(currentFile)">下载文件</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Upload,
  Download,
  View,
  More,
  Plus,
  Folder,
  Document,
  Picture,
  VideoCamera,
  Headset,
  Edit,
  FolderOpened,
  CopyDocument,
  Delete,
  Share,
  Link,
  Comment,
  UploadFilled,
} from '@element-plus/icons-vue'
import { listFile, downloadFile, delFile } from '@/api/im/file'
import { getToken } from '@/utils/auth'
import { formatFileSize } from '@/utils/format/file'

// ==================== 响应式状态 ====================

/** 搜索文本 */
const searchText = ref('')

/** 活动标签 */
const activeTab = ref('all')

/** 选中的文件 */
const selectedFile = ref(null)

/** 加载状态 */
const loading = ref(false)
const previewLoading = ref(false)

/** 文件列表 */
const fileList = ref([])

/** 对话框显示控制 */
const uploadDialogVisible = ref(false)
const previewDialogVisible = ref(false)

/** 当前预览的文件 */
const currentFile = ref(null)

/** 预览内容 */
const previewContent = ref('')

// ==================== 计算属性 ====================

/** 上传地址 */
const uploadUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_BASE_API || ''
  return `${baseUrl}/api/im/file/upload`
})

/** 上传请求头 */
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + getToken(),
}))

/** 过滤后的文件列表 */
const filteredFiles = computed(() => {
  let result = fileList.value
  if (!searchText.value) {
    result = fileList.value
  } else {
    const keyword = searchText.value.toLowerCase()
    result = fileList.value.filter(f => (f.fileName || '').toLowerCase().includes(keyword))
  }

  // 按标签过滤
  switch (activeTab.value) {
    case 'image':
      return result.filter(f => f.fileType === 'image')
    case 'document':
      return result.filter(f => f.fileType === 'document')
    case 'video':
      return result.filter(f => f.fileType === 'video')
    case 'audio':
      return result.filter(f => f.fileType === 'audio')
    default:
      return result
  }
})

/** 当前显示的文件列表 */
const currentFiles = computed(() => filteredFiles.value)

/** 标签信息 */
const tabs = computed(() => [
  { key: 'all', label: '全部', count: fileList.value.length },
  { key: 'image', label: '图片', count: fileList.value.filter(f => f.fileType === 'image').length },
  {
    key: 'document',
    label: '文档',
    count: fileList.value.filter(f => f.fileType === 'document').length,
  },
  { key: 'video', label: '视频', count: fileList.value.filter(f => f.fileType === 'video').length },
  { key: 'audio', label: '音频', count: fileList.value.filter(f => f.fileType === 'audio').length },
])

// ==================== 方法定义 ====================

/**
 * 获取文件列表
 */
const getList = async () => {
  loading.value = true
  try {
    const response = await listFile({})
    if (response.code === 200 || response.success) {
      fileList.value = response.data || response.rows || []
    } else {
      fileList.value = []
    }
  } catch (error) {
    console.error('获取文件列表失败:', error)
    fileList.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 搜索文件
 */
const handleSearch = () => {
  // 搜索已在computed中处理
}

/**
 * 选择文件
 */
const selectFile = file => {
  selectedFile.value = file
}

/**
 * 上传类型选择
 */
const handleUploadType = command => {
  if (command === 'file') {
    uploadDialogVisible.value = true
  } else if (command === 'folder') {
    ElMessage.info('上传文件夹功能开发中...')
  }
}

/**
 * 上传前校验
 */
const beforeUpload = file => {
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    ElMessage.error('文件大小不能超过 100MB!')
    return false
  }
  return true
}

/**
 * 文件上传成功
 */
const handleFileUploadSuccess = response => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    getList()
  } else {
    ElMessage.error('上传失败：' + (response.msg || '未知错误'))
  }
}

/**
 * 文件上传失败
 */
const handleFileUploadError = error => {
  ElMessage.error('上传失败：' + (error.message || '网络错误'))
}

/**
 * 颼览文件
 */
const handlePreview = file => {
  currentFile.value = file
  previewDialogVisible.value = true
  previewLoading.value = true

  if (isText(file)) {
    setTimeout(() => {
      previewContent.value = '文件内容预览...'
      previewLoading.value = false
    }, 500)
  } else {
    previewLoading.value = false
  }
}

/**
 * 下载文件
 */
const handleDownload = async file => {
  if (!file) return

  try {
    const response = await downloadFile(file.fileId)
    const blob = new Blob([response.data], { type: 'application/octet-stream' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = file.fileName
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载失败：' + (error.message || '网络错误'))
  }
}

/**
 * 文件操作
 */
const handleFileAction = async (command, file) => {
  switch (command) {
    case 'preview':
      handlePreview(file)
      break
    case 'download':
      handleDownload(file)
      break
    case 'share':
      handleShare(file)
      break
    case 'copyLink':
      handleCopyLink(file)
      break
    case 'rename':
      handleRename(file)
      break
    case 'move':
      ElMessage.info('移动文件功能开发中...')
      break
    case 'copy':
      ElMessage.info('复制文件功能开发中...')
      break
    case 'delete':
      try {
        await ElMessageBox.confirm('确定要删除该文件吗？', '确认删除', {
          type: 'warning',
        })
        await delFile(file.fileId)
        ElMessage.success('删除成功')
        // 从列表中移除
        const index = fileList.value.findIndex(f => f.fileId === file.fileId)
        if (index > -1) {
          fileList.value.splice(index, 1)
        }
        if (selectedFile.value?.fileId === file.fileId) {
          selectedFile.value = null
        }
      } catch {
        // 取消操作
      }
      break
  }
}

/**
 * 分享文件
 */
const handleShare = file => {
  ElMessage.info('分享功能开发中...')
}

/**
 * 复制链接
 */
const handleCopyLink = file => {
  const link = `${window.location.origin}/api/im/file/download/${file.fileId}`
  navigator.clipboard
    .writeText(link)
    .then(() => {
      ElMessage.success('链接已复制到剪贴板')
    })
    .catch(() => {
      ElMessage.error('复制链接失败')
    })
}

/**
 * 重命名文件
 */
const handleRename = file => {
  ElMessageBox.prompt('请输入新的文件名', '重命名', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: file.fileName,
    inputValidator: value => {
      if (!value) return '文件名不能为空'
      if (value.length > 50) return '文件名不能超过50个字符'
      return true
    },
  })
    .then(({ value }) => {
      // 模拟重命名
      file.fileName = value
      ElMessage.success('重命名成功')
    })
    .catch(() => {
      // 取消操作
    })
}

// formatFileSize 函数已从 @/utils/format/file 导入

/**
 * 格式化时间
 */
const formatTime = time => {
  if (!time) return ''
  return time
}

/**
 * 获取文件图标组件
 */
const getFileIconComponent = type => {
  const icons = {
    image: Picture,
    document: Document,
    video: VideoCamera,
    audio: Headset,
    other: Document,
  }
  return icons[type] || Document
}

/**
 * 判断是否为图片
 */
const isImage = file => {
  return file && file.fileType === 'image'
}

/**
 * 判断是否为文本
 */
const isText = file => {
  return file && ['txt', 'json', 'md', 'text'].includes(file.fileType)
}

// ==================== 生命周期 ====================

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.file-container {
  height: 100%;
  display: flex;
  background-color: #F5F7FA;
}

// 左侧文件列表面板
.file-list-panel {
  width: $list-panel-width;
  min-width: $list-panel-width;
  height: 100%;
  background-color: #fff;
  border-right: 1px solid #E8E8E8;
  display: flex;
  flex-direction: column;

  .panel-header {
    padding: 16px; // 修改：统一边距
    border-bottom: 1px solid #E8E8E8; // 钉钉规范
    display: flex;
    gap: 8px;

    .search-container {
      flex: 1;
      position: relative;

      .search-input {
        :deep(.el-input__wrapper) {
          border-radius: 4px; // 修改：18px -> 4px（符合规范）
          background-color: #F5F7FA;
          padding-left: 36px;
        }

        :deep(.el-input__prefix) {
          left: 12px;
        }
      }
    }
  }

  .file-tabs {
    display: flex;
    padding: 0 12px;
    border-bottom: 1px solid #E8E8E8;

    .tab-item {
      padding: 12px 16px;
      font-size: 14px; // 正文规范
      color: #666666; // 次要文字
      cursor: pointer;
      position: relative;
      transition: color 0.2s;
      display: flex;
      align-items: center;
      gap: 8px; // 修改：4px -> 8px（符合4的倍数）

      &:hover {
        color: #333333;
      }

      &.active {
        color: #0089FF; // 钉钉规范
        font-weight: 500;

        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 24px;
          height: 2px;
          background-color: #0089FF;
          border-radius: 1px;
        }
      }

      .tab-count {
        font-size: 12px; // 辅助文字
        background-color: #F0F2F5;
        color: #666666;
        border-radius: 8px;
        padding: 2px 6px; // 修改：0 6px -> 2px 6px
        min-width: 18px;
        height: 18px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .file-list {
    flex: 1;
    overflow-y: auto;

    .file-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      cursor: pointer;
      transition: background-color 0.2s;
      border-left: 3px solid transparent;

      &:hover {
        background-color: #F5F7FA;

        .file-actions {
          opacity: 1;
          visibility: visible;
        }
      }

      &.selected {
        background-color: #E6F7FF;
        border-left-color: #0089FF;
      }

      .file-icon {
        margin-right: 12px;
        color: #0089FF;
      }

      .file-info {
        flex: 1;
        min-width: 0;

        .file-name {
          font-size: 14px;
          font-weight: 500;
          color: #333333; // 正文色
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .file-meta {
          display: flex;
          align-items: center;
          gap: 8px; // 修改：4px -> 8px
          font-size: 12px;
          color: #999999; // 辅助文字

          .separator {
            color: #CCCCCC;
          }
        }
      }

      .file-actions {
        display: flex;
        align-items: center;
        gap: 4px;
        opacity: 0;
        visibility: hidden;
        transition:
          opacity 0.2s,
          visibility 0.2s;

        .el-button {
          padding: 4px;
        }

        .el-dropdown {
          .el-button {
            padding: 4px;
          }
        }
      }
    }
  }
}

// 右侧详情区域
.detail-panel {
  flex: 1;
  height: 100%;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .detail-header {
    padding: 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    border-bottom: 1px solid #E8E8E8;
    background-color: #FAFAFA;

    .file-preview {
      font-size: 24px;
      color: #0089FF;
    }

    .header-info {
      flex: 1;

      .file-name {
        margin: 0 0 8px; // 修改：4px -> 8px
        font-size: 20px; // 修改：18px -> 20px（模块标题规范）
        font-weight: 500; // 修改：600 -> 500
        color: #262626; // 标题色
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .file-meta {
        margin: 0;
        font-size: 14px;
        color: #666666; // 次要文字
        display: flex;
        align-items: center;
        gap: 8px; // 修改：4px -> 8px

        .separator {
          color: #CCCCCC;
        }
      }
    }

    .header-actions {
      display: flex;
      gap: 8px;
      flex-shrink: 0;
    }
  }

  .detail-content {
    flex: 1;
    padding: 24px;
    overflow-y: auto;

    .info-section {
      margin-bottom: 24px;

      h4 {
        margin: 0 0 16px;
        font-size: 16px; // 修改：14px -> 16px（小标题规范）
        font-weight: 500;
        color: #262626;
      }

      .info-grid {
        display: grid;
        grid-template-columns: 1fr;
        gap: 12px;

        .info-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 8px 0;
          border-bottom: 1px solid #F5F7FA;

          &:last-child {
            border-bottom: none;
          }

          .label {
            font-size: 14px;
            color: #666666;
          }

          .value {
            font-size: 14px;
            color: #262626;
            text-align: right;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            max-width: 60%;
          }
        }
      }
    }

    .quick-actions {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
    }
  }

  .empty-detail {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;

    .empty-content {
      text-align: center;
      color: #999999;

      .empty-icon {
        color: #CCCCCC;
        margin-bottom: 16px;
      }

      .empty-title {
        margin: 0 0 8px;
        font-size: 16px;
        font-weight: 500;
        color: #666666;
      }

      .empty-description {
        margin: 0;
        font-size: 14px;
        color: #999999;
      }
    }
  }
}

.preview-container {
  min-height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;

  .el-image {
    max-width: 100%;
    max-height: 500px;
  }

  .text-preview {
    width: 100%;
    max-height: 500px;
    overflow: auto;
    background: #F5F7FA;
    padding: 16px;
    margin: 0;
    font-family: monospace;
  }

  .no-preview {
    text-align: center;
    color: #999999;

    i {
      font-size: 48px;
      margin-bottom: 16px;
    }

    p {
      margin: 16px 0;
    }
  }
}

// 响应式
@media screen and (max-width: 768px) {
  .file-list-panel {
    width: 100%;
  }

  .detail-panel {
    display: none;
  }
}
</style>
