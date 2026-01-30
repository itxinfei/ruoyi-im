<template>
  <div class="group-file-panel">
    <!-- 头部 -->
    <header class="panel-header">
      <div class="header-left">
        <h2 class="header-title">群文件</h2>
        <div v-if="statistics" class="header-stats">
          <span class="stat-item">{{ statistics.fileCount }} 个文件</span>
          <span class="stat-divider">|</span>
          <span class="stat-item">{{ formatSize(statistics.totalSize) }}</span>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleUpload">
          <span class="material-icons-outlined">upload_file</span>
          上传文件
        </el-button>
      </div>
    </header>

    <!-- 内容区 -->
    <div class="panel-content">
      <!-- 筛选栏 -->
      <div class="filter-bar">
      <div class="filter-left">
        <!-- 分类筛选 -->
        <div class="filter-group">
          <span class="filter-label">分类:</span>
          <div class="category-tabs">
            <div
              v-for="cat in categories"
              :key="cat"
              class="category-tab"
              :class="{ active: selectedCategory === cat }"
              @click="handleSelectCategory(cat)"
            >
              {{ cat }}
            </div>
          </div>
        </div>
      </div>
      <div class="filter-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文件名"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <span class="material-icons-outlined">search</span>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 文件列表 -->
    <div class="file-list">
      <div
        v-for="file in fileList"
        :key="file.id"
        class="file-item"
        @click="handleFileClick(file)"
        @contextmenu.prevent="handleContextMenu($event, file)"
      >
        <div class="file-icon">
          <span v-if="isImage(file)" class="material-icons-outlined image-icon">image</span>
          <span v-else-if="isVideo(file)" class="material-icons-outlined video-icon">videocam</span>
          <span v-else-if="isDocument(file)" class="material-icons-outlined doc-icon">description</span>
          <span v-else class="material-icons-outlined default-icon">insert_drive_file</span>
        </div>
        <div class="file-info">
          <div class="file-name">{{ file.fileName }}</div>
          <div class="file-meta">
            <span class="file-size">{{ formatSize(file.fileSize) }}</span>
            <span class="file-divider">·</span>
            <span class="file-uploader">{{ file.uploaderName }}</span>
            <span class="file-divider">·</span>
            <span class="file-time">{{ formatTime(file.uploadTime) }}</span>
          </div>
        </div>
        <div class="file-actions" @click.stop>
          <el-dropdown trigger="click" @command="(cmd) => handleFileCommand(cmd, file)">
            <span class="material-icons-outlined more-btn">more_horiz</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="download">
                  <span class="material-icons-outlined">download</span>
                  下载
                </el-dropdown-item>
                <el-dropdown-item command="rename">
                  <span class="material-icons-outlined">edit</span>
                  重命名
                </el-dropdown-item>
                <el-dropdown-item command="move">
                  <span class="material-icons-outlined">folder</span>
                  移动到
                </el-dropdown-item>
                <el-dropdown-item divided command="delete" class="danger-item">
                  <span class="material-icons-outlined">delete</span>
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="fileList.length === 0 && !loading" class="empty-state">
        <div class="empty-illustration">
          <div class="folder-icon">
            <span class="material-icons-outlined">folder_open</span>
          </div>
          <div class="floating-icons">
            <span class="icon icon-1 material-icons-outlined">image</span>
            <span class="icon icon-2 material-icons-outlined">description</span>
            <span class="icon icon-3 material-icons-outlined">video_file</span>
          </div>
        </div>
        <h3 class="empty-title">群文件为空</h3>
        <p class="empty-description">
          {{ searchKeyword ? '没有找到匹配的文件' : '暂无群文件，上传文件与群成员共享' }}
        </p>
        <div v-if="!searchKeyword" class="empty-actions">
          <el-button type="primary" @click="handleUpload">
            <span class="material-icons-outlined">upload_file</span>
            上传文件
          </el-button>
          <el-button @click="handleSelectCategory('全部')">
            <span class="material-icons-outlined">refresh</span>
            刷新列表
          </el-button>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-bar">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
    </div>

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传文件" width="500px">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :file-list="uploadFileList"
        drag
        multiple
      >
        <div class="upload-area">
          <span class="material-icons-outlined upload-icon">cloud_upload</span>
          <p class="upload-text">点击或拖拽文件到此区域上传</p>
          <p class="upload-hint">支持任意格式文件</p>
        </div>
      </el-upload>

      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleConfirmUpload">
          确定上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 重命名对话框 -->
    <el-dialog v-model="renameDialogVisible" title="重命名文件" width="400px">
      <el-input
        v-model="newFileName"
        placeholder="请输入新文件名"
        maxlength="100"
        show-word-limit
      />
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmRename">确定</el-button>
      </template>
    </el-dialog>

    <!-- 移动文件对话框 -->
    <el-dialog v-model="moveDialogVisible" title="移动到分类" width="400px">
      <div class="category-list">
        <div
          v-for="cat in categories"
          :key="cat"
          class="category-option"
          :class="{ selected: targetCategory === cat }"
          @click="targetCategory = cat"
        >
          <span class="material-icons-outlined">{{ cat === selectedCategory ? 'check' : 'folder' }}</span>
          {{ cat }}
        </div>
      </div>
      <template #footer>
        <el-button @click="moveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmMove">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import {
  getGroupFileList,
  getGroupFileStatistics,
  getGroupFileCategories,
  uploadGroupFile,
  updateGroupFile,
  deleteGroupFile,
  downloadGroupFile,
  moveGroupFile
} from '@/api/im/groupFile'
import { uploadFile } from '@/api/im/file'

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
const fileList = ref([])
const statistics = ref(null)
const categories = ref(['全部', '文档', '图片', '视频', '音频', '其他'])
const selectedCategory = ref('全部')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 上传相关
const uploadDialogVisible = ref(false)
const uploadFileList = ref([])
const uploading = ref(false)

// 重命名相关
const renameDialogVisible = ref(false)
const newFileName = ref('')
const currentFile = ref(null)

// 移动文件相关
const moveDialogVisible = ref(false)
const targetCategory = ref('')

// 加载文件列表
const loadFileList = async () => {
  loading.value = true
  try {
    const res = await getGroupFileList({
      groupId: props.groupId,
      current: currentPage.value,
      size: pageSize.value,
      category: selectedCategory.value === '全部' ? undefined : selectedCategory.value,
      fileName: searchKeyword.value || undefined
    })
    if (res.code === 200) {
      fileList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载文件列表失败', error)
    ElMessage.error('加载文件列表失败')
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    const res = await getGroupFileStatistics(props.groupId)
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('加载统计信息失败', error)
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getGroupFileCategories(props.groupId)
    if (res.code === 200 && res.data?.length > 0) {
      categories.value = ['全部', ...res.data]
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

// 选择分类
const handleSelectCategory = (category) => {
  selectedCategory.value = category
  currentPage.value = 1
  loadFileList()
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadFileList()
}

// 分页变化
const handlePageChange = () => {
  loadFileList()
}

const handleSizeChange = () => {
  currentPage.value = 1
  loadFileList()
}

// 上传文件
const handleUpload = () => {
  uploadFileList.value = []
  uploadDialogVisible.value = true
}

const handleFileChange = (file, fileList) => {
  uploadFileList.value = fileList
}

const handleFileRemove = (file, fileList) => {
  uploadFileList.value = fileList
}

const handleConfirmUpload = async () => {
  if (uploadFileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  uploading.value = true
  try {
    // 先上传文件到文件服务器
    for (const file of uploadFileList.value) {
      const formData = new FormData()
      formData.append('file', file.raw)

      const uploadRes = await uploadFile(formData)
      if (uploadRes.code === 200) {
        // 添加到群组文件列表
        await uploadGroupFile({
          groupId: props.groupId,
          fileId: uploadRes.data.id,
          category: '其他'
        })
      }
    }

    ElMessage.success('文件上传成功')
    uploadDialogVisible.value = false
    loadFileList()
    loadStatistics()
  } catch (error) {
    console.error('上传失败', error)
    ElMessage.error('文件上传失败')
  } finally {
    uploading.value = false
  }
}

// 文件操作
const handleFileClick = (file) => {
  // 可以打开预览
}

const handleFileCommand = async (command, file) => {
  currentFile.value = file

  switch (command) {
    case 'download':
      handleDownload(file)
      break
    case 'rename':
      newFileName.value = file.fileName
      renameDialogVisible.value = true
      break
    case 'move':
      targetCategory.value = ''
      moveDialogVisible.value = true
      break
    case 'delete':
      handleDelete(file)
      break
  }
}

const handleDownload = async (file) => {
  try {
    const res = await downloadGroupFile(file.id)
    if (res.code === 200) {
      window.open(res.data, '_blank')
    }
  } catch (error) {
    console.error('下载失败', error)
    ElMessage.error('文件下载失败')
  }
}

const handleConfirmRename = async () => {
  if (!newFileName.value.trim()) {
    ElMessage.warning('请输入文件名')
    return
  }

  try {
    await updateGroupFile(currentFile.value.id, {
      fileName: newFileName.value
    })
    ElMessage.success('重命名成功')
    renameDialogVisible.value = false
    loadFileList()
  } catch (error) {
    console.error('重命名失败', error)
    ElMessage.error('重命名失败')
  }
}

const handleConfirmMove = async () => {
  if (!targetCategory.value) {
    ElMessage.warning('请选择目标分类')
    return
  }

  try {
    await moveGroupFile(currentFile.value.id, targetCategory.value)
    ElMessage.success('移动成功')
    moveDialogVisible.value = false
    loadFileList()
  } catch (error) {
    console.error('移动失败', error)
    ElMessage.error('移动失败')
  }
}

const handleDelete = async (file) => {
  try {
    await ElMessageBox.confirm(`确定要删除文件 "${file.fileName}" 吗？`, '删除文件', {
      type: 'warning'
    })

    await deleteGroupFile(file.id)
    ElMessage.success('删除成功')
    loadFileList()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

// 工具函数
const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const isSameYear = date.getFullYear() === now.getFullYear()

  if (isSameYear) {
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  } else {
    return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
  }
}

const isImage = (file) => {
  const ext = file.fileName?.split('.').pop()?.toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp'].includes(ext)
}

const isVideo = (file) => {
  const ext = file.fileName?.split('.').pop()?.toLowerCase()
  return ['mp4', 'avi', 'mov', 'mkv', 'flv', 'wmv'].includes(ext)
}

const isDocument = (file) => {
  const ext = file.fileName?.split('.').pop()?.toLowerCase()
  return ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'pdf'].includes(ext)
}

// 右键菜单
const handleContextMenu = (event, file) => {
  // 可以显示自定义右键菜单
}

onMounted(() => {
  loadFileList()
  loadStatistics()
  loadCategories()
})
</script>

<style scoped lang="scss">
.group-file-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: var(--dt-bg-body);
  overflow: hidden;
}

.panel-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
  padding: 16px;
}

// 头部
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .header-title {
      font-size: 18px;
      font-weight: 600;
      color: var(--dt-text-primary);
      margin: 0;
    }

    .header-stats {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      color: var(--dt-text-secondary);

      .stat-divider {
        color: var(--dt-border-color);
      }
    }
  }

  .header-right {
    .el-button {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }
}

// 筛选栏
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--dt-bg-card);
  border-radius: 8px;
  margin-bottom: 16px;

  .filter-left {
    display: flex;
    align-items: center;

    .filter-group {
      display: flex;
      align-items: center;
      gap: 12px;

      .filter-label {
        font-size: 13px;
        color: var(--dt-text-secondary);
      }

      .category-tabs {
        display: flex;
        gap: 4px;

        .category-tab {
          padding: 6px 12px;
          font-size: 13px;
          color: var(--dt-text-secondary);
          border-radius: 6px;
          cursor: pointer;
          transition: all 0.2s;

          &:hover {
            background: var(--dt-bg-hover);
          }

          &.active {
            color: var(--dt-brand-color);
            background: var(--dt-brand-bg);
          }
        }
      }
    }
  }

  .filter-right {
    .el-input {
      width: 240px;
    }
  }
}

// 文件列表
.file-list {
  flex: 1;
  overflow-y: auto;
  background: var(--dt-bg-card);
  border-radius: 8px;
  padding: 8px;

  .file-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: var(--dt-bg-hover);

      .file-actions {
        opacity: 1;
      }
    }

    .file-icon {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      flex-shrink: 0;

      .image-icon { color: #0089FF; }
      .video-icon { color: #722ed1; }
      .doc-icon { color: #52c41a; }
      .default-icon { color: #8c8c8c; }
    }

    .file-info {
      flex: 1;
      min-width: 0;

      .file-name {
        font-size: 14px;
        color: var(--dt-text-primary);
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .file-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 12px;
        color: var(--dt-text-tertiary);

        .file-divider {
          color: var(--dt-border-color);
        }
      }
    }

    .file-actions {
      opacity: 0;
      transition: opacity 0.2s;

      .more-btn {
        padding: 4px;
        color: var(--dt-text-secondary);
        cursor: pointer;

        &:hover {
          color: var(--dt-brand-color);
        }
      }
    }
  }

  .empty-state,
  .loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 400px;
    color: var(--dt-text-tertiary);
  }

  // 空状态插画
  .empty-illustration {
    position: relative;
    width: 160px;
    height: 120px;
    margin-bottom: 24px;
  }

  .folder-icon {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 80px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(255, 107, 107, 0.25);

    .material-icons-outlined {
      font-size: 40px;
      color: #fff;
    }
  }

  .floating-icons {
    position: absolute;
    inset: 0;

    .icon {
      position: absolute;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      animation: float 3s ease-in-out infinite;

      .material-icons-outlined {
        font-size: 20px;
      }

      &.icon-1 {
        top: 8px;
        left: 16px;
        color: #0089FF;
        animation-delay: 0s;
      }

      &.icon-2 {
        top: 16px;
        right: 12px;
        color: #52c41a;
        animation-delay: 0.5s;
      }

      &.icon-3 {
        bottom: 8px;
        right: 20px;
        color: #722ed1;
        animation-delay: 1s;
      }
    }
  }

  @keyframes float {
    0%, 100% {
      transform: translateY(0);
    }
    50% {
      transform: translateY(-8px);
    }
  }

  .empty-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin-bottom: 8px;
  }

  .empty-description {
    font-size: 13px;
    color: var(--dt-text-secondary);
    margin-bottom: 24px;
    max-width: 280px;
    text-align: center;
  }

  .empty-actions {
    display: flex;
    gap: 12px;

    .el-button {
      display: flex;
      align-items: center;
      gap: 6px;

      .material-icons-outlined {
        font-size: 18px;
      }
    }
  }

  .loading-state {
    gap: 12px;
  }
}

// 分页
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

// 对话框样式
.upload-area {
  padding: 40px;
  text-align: center;
  border: 2px dashed var(--dt-border-color);
  border-radius: 8px;

  .upload-icon {
    font-size: 48px;
    color: var(--dt-brand-color);
  }

  .upload-text {
    margin-top: 16px;
    font-size: 14px;
    color: var(--dt-text-primary);
  }

  .upload-hint {
    margin-top: 8px;
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .category-option {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: var(--dt-bg-hover);
    }

    &.selected {
      color: var(--dt-brand-color);
      background: var(--dt-brand-bg);
    }
  }
}

.danger-item {
  color: var(--dt-error-color);

  &:hover {
    background: var(--dt-error-bg) !important;
  }
}

// 暗色模式
:deep(.dark) {
  .filter-bar,
  .file-list {
    background: var(--dt-bg-card-dark);
  }
}
</style>
