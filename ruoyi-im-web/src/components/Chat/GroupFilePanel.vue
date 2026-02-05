<template>
  <div class="group-file-panel">
    <!-- 工具栏 -->
    <div class="file-toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索文件名"
        prefix-icon="Search"
        clearable
        class="search-input"
        @input="handleSearch"
      />
      <el-button
        type="primary"
        :loading="uploading"
        @click="handleUpload"
      >
        <el-icon><Upload /></el-icon>
        上传文件
      </el-button>
    </div>

    <!-- 分类标签页 -->
    <el-tabs
      v-model="activeCategory"
      @tab-change="handleCategoryChange"
    >
      <el-tab-pane
        label="全部"
        name="all"
      >
        <template #label>
          <span class="tab-item">
            <el-icon><Folder /></el-icon>
            全部
            <span class="count-badge">{{ statistics.totalCount || 0 }}</span>
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="image">
        <template #label>
          <span class="tab-item">
            <el-icon><Picture /></el-icon>
            图片
            <span class="count-badge">{{ statistics.imageCount || 0 }}</span>
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="document">
        <template #label>
          <span class="tab-item">
            <el-icon><Document /></el-icon>
            文档
            <span class="count-badge">{{ statistics.documentCount || 0 }}</span>
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="video">
        <template #label>
          <span class="tab-item">
            <el-icon><VideoCamera /></el-icon>
            视频
            <span class="count-badge">{{ statistics.videoCount || 0 }}</span>
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="other">
        <template #label>
          <span class="tab-item">
            <el-icon><MoreFilled /></el-icon>
            其他
            <span class="count-badge">{{ statistics.otherCount || 0 }}</span>
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 文件列表 -->
    <div class="file-list-container">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="fileList"
        class="file-table"
        empty-text="暂无文件"
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="50"
        />
        <el-table-column
          label="文件名"
          min-width="200"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <div class="file-name-cell">
              <div class="file-icon-wrapper">
                <el-icon class="file-icon">
                  <component :is="getFileIcon(row.category)" />
                </el-icon>
              </div>
              <span class="file-name">{{ row.fileName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="大小"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <span class="file-size">{{ formatSize(row.fileSize) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="上传者"
          width="120"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span class="uploader-name">{{ row.uploaderName }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="上传时间"
          width="160"
          align="center"
        >
          <template #default="{ row }">
            <span class="upload-time">{{ formatTime(row.uploadTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="180"
          align="center"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              @click="handleDownload(row)"
            >
              <el-icon><Download /></el-icon>
              下载
            </el-button>
            <el-button
              link
              type="danger"
              :disabled="!canDelete(row)"
              @click="handleDelete(row)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 批量操作栏 -->
    <transition name="slide-up">
      <div
        v-if="selectedFiles.length > 0"
        class="batch-toolbar"
      >
        <span class="selected-count">已选 {{ selectedFiles.length }} 项</span>
        <div class="batch-actions">
          <el-button
            type="danger"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
          <el-button @click="clearSelection">
            <el-icon><Close /></el-icon>
            取消选择
          </el-button>
        </div>
      </div>
    </transition>

    <!-- 上传对话框 -->
    <input
      ref="fileInputRef"
      type="file"
      style="display: none"
      @change="handleFileChange"
    >
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Upload, Download, Delete, Search, Folder, Picture,
  Document, VideoCamera, MoreFilled, Close,
  Picture as PictureIcon, Document as DocumentIcon,
  VideoCamera as VideoCameraIcon, Folder as FolderIcon
} from '@element-plus/icons-vue'
import {
  getGroupFileList,
  getGroupFileStatistics,
  deleteGroupFile,
  batchDeleteGroupFiles,
  downloadGroupFile,
  uploadGroupFile
} from '@/api/im/groupFile'
import { uploadFile } from '@/api/im/file'
import { useStore } from 'vuex'
import { formatChatTime } from '@/utils/format'

const props = defineProps({
  groupId: {
    type: [String, Number],
    required: true
  }
})

const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])

// 状态
const loading = ref(false)
const uploading = ref(false)
const searchKeyword = ref('')
const activeCategory = ref('all')
const fileList = ref([])
const selectedFiles = ref([])
const tableRef = ref(null)
const fileInputRef = ref(null)

// 统计信息
const statistics = ref({
  totalCount: 0,
  imageCount: 0,
  documentCount: 0,
  videoCount: 0,
  otherCount: 0
})

// 格式化时间（本地别名）
const formatTime = formatChatTime

// 分页
const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

// 当前用户在群组中的角色
const currentUserRole = computed(() => {
  // 从群组成员信息中获取角色
  // 这里简化处理，实际需要从群组信息中获取
  return 'MEMBER' // 可能是 OWNER, ADMIN, MEMBER
})

// 判断是否可以删除文件
const canDelete = file => {
  // 群主和管理员可以删除任意文件
  if (currentUserRole.value === 'OWNER' || currentUserRole.value === 'ADMIN') {
    return true
  }
  // 普通成员只能删除自己上传的文件
  return file.uploaderId === currentUser.value?.id
}

// 获取文件图标
const getFileIcon = category => {
  const iconMap = {
    image: PictureIcon,
    document: DocumentIcon,
    video: VideoCameraIcon,
    other: FolderIcon
  }
  return iconMap[category] || FolderIcon
}

// 格式化文件大小
const formatSize = bytes => {
  if (!bytes) {return '0 B'}
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 加载文件列表
const loadFiles = async () => {
  loading.value = true
  try {
    const res = await getGroupFileList({
      groupId: props.groupId,
      current: pagination.value.current,
      size: pagination.value.size,
      category: activeCategory.value === 'all' ? undefined : activeCategory.value,
      fileName: searchKeyword.value || undefined
    })
    if (res.code === 200 && res.data) {
      fileList.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载文件列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    const res = await getGroupFileStatistics(props.groupId)
    if (res.code === 200 && res.data) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 搜索处理（防抖）
let searchTimer = null
const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    pagination.value.current = 1
    loadFiles()
  }, 300)
}

// 分类切换
const handleCategoryChange = () => {
  pagination.value.current = 1
  loadFiles()
}

// 分页变化
const handlePageChange = () => {
  loadFiles()
}

const handleSizeChange = () => {
  pagination.value.current = 1
  loadFiles()
}

// 选择变化
const handleSelectionChange = selection => {
  selectedFiles.value = selection
}

// 清空选择
const clearSelection = () => {
  tableRef.value?.clearSelection()
}

// 上传文件
const handleUpload = () => {
  fileInputRef.value?.click()
}

const handleFileChange = async event => {
  const file = event.target.files?.[0]
  if (!file) {return}

  uploading.value = true
  try {
    // 先上传文件获取文件ID
    const formData = new FormData()
    formData.append('file', file)

    const uploadRes = await uploadFile(formData)
    if (uploadRes.code === 200 && uploadRes.data) {
      // 创建群文件记录
      const fileRes = await uploadGroupFile({
        groupId: props.groupId,
        fileId: uploadRes.data.id,
        category: getFileCategory(file.type)
      })
      if (fileRes.code === 200) {
        ElMessage.success('上传成功')
        loadFiles()
        loadStatistics()
      }
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
    // 清空input以允许重复上传同一文件
    event.target.value = ''
  }
}

// 获取文件分类
const getFileCategory = mimeType => {
  if (mimeType.startsWith('image/')) {return 'image'}
  if (mimeType.startsWith('video/')) {return 'video'}
  if (mimeType.includes('pdf') || mimeType.includes('document') || mimeType.includes('text')) {return 'document'}
  return 'other'
}

// 下载文件
const handleDownload = async file => {
  try {
    const res = await downloadGroupFile(file.id)
    if (res.code === 200 && res.data) {
      // 创建下载链接
      const url = res.data.url || res.data
      const link = document.createElement('a')
      link.href = url
      link.download = file.fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      ElMessage.success('下载成功')
    }
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败')
  }
}

// 删除文件
const handleDelete = async file => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件"${file.fileName}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteGroupFile(file.id)
    ElMessage.success('删除成功')
    loadFiles()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedFiles.value.length === 0) {return}

  // 检查权限
  const cannotDelete = selectedFiles.value.filter(file => !canDelete(file))
  if (cannotDelete.length > 0) {
    ElMessage.warning('您没有权限删除部分文件')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedFiles.value.length} 个文件吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const ids = selectedFiles.value.map(f => f.id)
    await batchDeleteGroupFiles(ids)
    ElMessage.success('删除成功')
    clearSelection()
    loadFiles()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadFiles()
  loadStatistics()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.group-file-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  background: var(--dt-bg-card);
}

.file-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--dt-border-light);

  .search-input {
    flex: 1;
    max-width: 300px;
  }
}

// 分类标签
:deep(.el-tabs) {
  .el-tabs__header {
    margin-bottom: 16px;
  }

  .el-tabs__nav-wrap::after {
    background-color: var(--dt-border-light);
  }

  .tab-item {
    display: flex;
    align-items: center;
    gap: 4px;

    .count-badge {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 18px;
      height: 18px;
      padding: 0 6px;
      background: var(--dt-fill-color-light);
      border-radius: 9px;
      font-size: 11px;
      font-weight: 500;
      color: var(--dt-text-secondary);
    }
  }
}

// 文件列表容器
.file-list-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.file-table {
  flex: 1;

  :deep(.el-table__body-wrapper) {
    max-height: calc(100vh - 400px);
    overflow-y: auto;
  }

  .file-name-cell {
    display: flex;
    align-items: center;
    gap: 10px;

    .file-icon-wrapper {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: var(--dt-radius-md);
      background: var(--dt-fill-color-light);
      flex-shrink: 0;

      .file-icon {
        font-size: 18px;
        color: var(--dt-brand-color);
      }
    }

    .file-name {
      font-size: 14px;
      color: var(--dt-text-primary);
    }
  }

  .file-size,
  .uploader-name,
  .upload-time {
    font-size: 13px;
    color: var(--dt-text-secondary);
  }

  .upload-time {
    color: var(--dt-text-tertiary);
  }
}

// 分页
.pagination-container {
  display: flex;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid var(--dt-border-light);
}

// 批量操作栏
.batch-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: linear-gradient(135deg, #0089FF 0%, #0958d9 100%);
  color: #fff;
  border-radius: var(--dt-radius-md);
  margin-top: 16px;

  .selected-count {
    font-size: 14px;
    font-weight: 500;
  }

  .batch-actions {
    display: flex;
    gap: 8px;
  }

  .el-button {
    background: rgba(255, 255, 255, 0.2);
    border-color: rgba(255, 255, 255, 0.3);
    color: #fff;

    &:hover {
      background: rgba(255, 255, 255, 0.3);
      border-color: rgba(255, 255, 255, 0.5);
    }

    &.is-disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

// 动画
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s var(--dt-ease-out);
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
