<template>
  <el-dialog
    v-model="visible"
    title="群文件"
    width="800px"
    class="group-file-dialog"
    destroy-on-close
    append-to-body
  >
    <div class="group-file-container">
      <!-- 顶部统计与操作栏 -->
      <div class="file-header">
        <div class="stats-bar">
          <div class="stat-item">
            <span class="stat-value">{{ statistics.totalCount || 0 }}</span>
            <span class="stat-label">个文件</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ formatSize(statistics.totalSize) }}</span>
            <span class="stat-label">已使用</span>
          </div>
        </div>
        <div class="action-bar">
          <el-button type="primary" @click="handleUpload">
            <el-icon><Upload /></el-icon>
            上传文件
          </el-button>
        </div>
      </div>

      <!-- 分类筛选 -->
      <div class="filter-bar">
        <el-radio-group v-model="currentCategory" @change="handleCategoryChange">
          <el-radio-button label="">
            全部
          </el-radio-button>
          <el-radio-button
            v-for="cat in categories"
            :key="cat"
            :label="cat"
          >
            {{ cat }}
          </el-radio-button>
        </el-radio-group>

        <el-input
          v-model="searchKeyword"
          placeholder="搜索文件名"
          clearable
          style="width: 200px"
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 文件列表 -->
      <div v-loading="loading" class="file-list scrollbar-thin">
        <el-empty v-if="files.length === 0 && !loading" description="暂无文件" />

        <div
          v-for="file in files"
          :key="file.id"
          class="file-item"
          @click="handleFileClick(file)"
        >
          <div class="file-icon">
            <span class="material-icons-outlined" :class="getFileIconClass(file.fileType)">
              {{ getFileIcon(file.fileType) }}
            </span>
          </div>
          <div class="file-info">
            <div class="file-name">
              {{ file.fileName }}
            </div>
            <div class="file-meta">
              <span>{{ formatSize(file.fileSize) }}</span>
              <span>{{ file.uploaderName || '未知' }}</span>
              <span>{{ formatDate(file.createTime) }}</span>
            </div>
          </div>
          <div class="file-actions">
            <el-tooltip content="下载">
              <el-button text @click.stop="handleDownload(file)">
                <el-icon><Download /></el-icon>
              </el-button>
            </el-tooltip>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, file)">
              <el-button text>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="rename">
                    重命名
                  </el-dropdown-item>
                  <el-dropdown-item command="move">
                    移动到
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-bar">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadFiles"
          @current-change="loadFiles"
        />
      </div>
    </div>

    <!-- 上传文件对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传文件"
      width="500px"
      append-to-body
    >
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            drag
          >
            <el-icon class="el-icon--upload">
              <UploadFilled />
            </el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或<em>点击上传</em>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="uploadForm.category"
            placeholder="选择分类"
            allow-create
            filterable
          >
            <el-option label="文档" value="文档" />
            <el-option label="图片" value="图片" />
            <el-option label="视频" value="视频" />
            <el-option label="音频" value="音频" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="uploading" @click="confirmUpload">
          上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 重命名对话框 -->
    <el-dialog
      v-model="renameDialogVisible"
      title="重命名"
      width="400px"
      append-to-body
    >
      <el-input v-model="newFileName" placeholder="请输入新文件名" />
      <template #footer>
        <el-button @click="renameDialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="confirmRename">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 移动分类对话框 -->
    <el-dialog
      v-model="moveDialogVisible"
      title="移动到"
      width="400px"
      append-to-body
    >
      <el-select
        v-model="moveCategory"
        placeholder="选择分类"
        allow-create
        filterable
        style="width: 100%"
      >
        <el-option
          v-for="cat in categories"
          :key="cat"
          :label="cat"
          :value="cat"
        />
      </el-select>
      <template #footer>
        <el-button @click="moveDialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="confirmMove">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 隐藏的文件上传 input -->
    <input
      ref="fileInputRef"
      type="file"
      style="display: none"
      @change="handleFileSelect"
    >
  </el-dialog>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Download, Search, MoreFilled, UploadFilled } from '@element-plus/icons-vue'
import {
  getGroupFiles,
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
    default: null
  }
})

const visible = ref(false)
const loading = ref(false)
const files = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const currentCategory = ref('')
const searchKeyword = ref('')
const categories = ref([])
const statistics = ref({})

// 上传相关
const uploadDialogVisible = ref(false)
const uploading = ref(false)
const uploadForm = ref({
  category: '文档'
})
const selectedFile = ref(null)
const uploadRef = ref(null)

// 重命名相关
const renameDialogVisible = ref(false)
const newFileName = ref('')
const currentFile = ref(null)

// 移动分类相关
const moveDialogVisible = ref(false)
const moveCategory = ref('')

const fileInputRef = ref(null)

// 打开弹窗
const open = () => {
  visible.value = true
  loadStatistics()
  loadCategories()
  loadFiles()
}

// 加载统计
const loadStatistics = async () => {
  if (!props.groupId) return
  try {
    const res = await getGroupFileStatistics(props.groupId)
    if (res.code === 200) {
      statistics.value = res.data || {}
    }
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

// 加载分类
const loadCategories = async () => {
  if (!props.groupId) return
  try {
    const res = await getGroupFileCategories(props.groupId)
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

// 加载文件列表
const loadFiles = async () => {
  if (!props.groupId) return
  loading.value = true
  try {
    const res = await getGroupFiles({
      groupId: props.groupId,
      category: currentCategory.value || undefined,
      keyword: searchKeyword.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      files.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (e) {
    console.error('加载文件列表失败', e)
  } finally {
    loading.value = false
  }
}

// 分类变更
const handleCategoryChange = () => {
  pageNum.value = 1
  loadFiles()
}

// 搜索
let searchTimer = null
const handleSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    pageNum.value = 1
    loadFiles()
  }, 300)
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
})

// 上传文件
const handleUpload = () => {
  uploadDialogVisible.value = true
  uploadForm.value = { category: '文档' }
  selectedFile.value = null
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const confirmUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  uploading.value = true
  try {
    // 1. 先上传文件
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    const uploadRes = await uploadFile(formData)

    if (uploadRes.code !== 200) {
      throw new Error(uploadRes.msg || '上传失败')
    }

    // 2. 创建群文件记录
    const res = await uploadGroupFile({
      groupId: props.groupId,
      fileId: uploadRes.data.id,
      fileName: selectedFile.value.name,
      category: uploadForm.value.category || '其他',
      permission: 'ALL'
    })

    if (res.code === 200) {
      ElMessage.success('上传成功')
      uploadDialogVisible.value = false
      loadFiles()
      loadStatistics()
      loadCategories()
    } else {
      throw new Error(res.msg || '上传失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

// 文件点击
const handleFileClick = (file) => {
  // 预览或下载
  handleDownload(file)
}

// 下载文件
const handleDownload = async (file) => {
  try {
    const res = await downloadGroupFile(file.id)
    if (res.code === 200 && res.data) {
      // 创建下载链接
      const link = document.createElement('a')
      link.href = res.data
      link.download = file.fileName
      link.target = '_blank'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      ElMessage.success('开始下载')
    }
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

// 命令处理
const handleCommand = (cmd, file) => {
  currentFile.value = file
  switch (cmd) {
    case 'rename':
      newFileName.value = file.fileName
      renameDialogVisible.value = true
      break
    case 'move':
      moveCategory.value = file.category || ''
      moveDialogVisible.value = true
      break
    case 'delete':
      handleDelete(file)
      break
  }
}

// 确认重命名
const confirmRename = async () => {
  if (!newFileName.value.trim()) {
    ElMessage.warning('请输入文件名')
    return
  }

  try {
    const res = await updateGroupFile(currentFile.value.id, {
      fileName: newFileName.value.trim()
    })
    if (res.code === 200) {
      ElMessage.success('重命名成功')
      renameDialogVisible.value = false
      loadFiles()
    }
  } catch (e) {
    ElMessage.error('重命名失败')
  }
}

// 确认移动
const confirmMove = async () => {
  if (!moveCategory.value) {
    ElMessage.warning('请选择分类')
    return
  }

  try {
    const res = await moveGroupFile(currentFile.value.id, moveCategory.value)
    if (res.code === 200) {
      ElMessage.success('移动成功')
      moveDialogVisible.value = false
      loadFiles()
      loadCategories()
    }
  } catch (e) {
    ElMessage.error('移动失败')
  }
}

// 删除文件
const handleDelete = async (file) => {
  try {
    await ElMessageBox.confirm('确定要删除该文件吗？', '提示', {
      type: 'warning'
    })

    const res = await deleteGroupFile(file.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadFiles()
      loadStatistics()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleFileSelect = (e) => {
  const file = e.target.files?.[0]
  if (file) {
    selectedFile.value = file
    uploadDialogVisible.value = true
  }
}

// 获取文件图标
const getFileIcon = (fileType) => {
  const iconMap = {
    'image': 'image',
    'video': 'videocam',
    'audio': 'audiotrack',
    'document': 'description',
    'pdf': 'picture_as_pdf',
    'excel': 'table_chart',
    'word': 'article',
    'ppt': 'slideshow',
    'zip': 'folder_zip',
    'code': 'code',
    'other': 'insert_drive_file'
  }
  return iconMap[fileType?.toLowerCase()] || iconMap.other
}

const getFileIconClass = (fileType) => {
  const classMap = {
    'image': 'icon-image',
    'video': 'icon-video',
    'audio': 'icon-audio',
    'document': 'icon-doc',
    'pdf': 'icon-pdf',
    'excel': 'icon-excel',
    'word': 'icon-word',
    'ppt': 'icon-ppt'
  }
  return classMap[fileType?.toLowerCase()] || ''
}

// 格式化文件大小
const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return `${size.toFixed(1)} ${units[i]}`
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

watch(() => props.groupId, () => {
  if (visible.value) {
    loadStatistics()
    loadCategories()
    loadFiles()
  }
})

defineExpose({ open })
</script>

<style scoped lang="scss">
.group-file-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.group-file-container {
  display: flex;
  flex-direction: column;
  height: 600px;
}

.file-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);

  .stats-bar {
    display: flex;
    gap: 24px;

    .stat-item {
      display: flex;
      align-items: baseline;
      gap: 4px;

      .stat-value {
        font-size: 20px;
        font-weight: 600;
        color: var(--dt-text-primary);
      }

      .stat-label {
        font-size: 13px;
        color: var(--dt-text-tertiary);
      }
    }
  }
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: var(--dt-bg-body);
}

.file-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 20px;

  .file-item {
    display: flex;
    align-items: center;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: var(--dt-bg-body);

      .file-actions {
        opacity: 1;
      }
    }

    .file-icon {
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--dt-brand-bg);
      border-radius: 8px;
      margin-right: 12px;

      .material-icons-outlined {
        font-size: 24px;
        color: var(--dt-brand-color);

        &.icon-image { color: #22c55e; }
        &.icon-video { color: #ef4444; }
        &.icon-audio { color: #f97316; }
        &.icon-doc { color: #3b82f6; }
        &.icon-pdf { color: #ef4444; }
        &.icon-excel { color: #22c55e; }
        &.icon-word { color: #3b82f6; }
        &.icon-ppt { color: #f97316; }
      }
    }

    .file-info {
      flex: 1;
      min-width: 0;

      .file-name {
        font-size: 14px;
        font-weight: 500;
        color: var(--dt-text-primary);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .file-meta {
        display: flex;
        gap: 12px;
        margin-top: 4px;
        font-size: 12px;
        color: var(--dt-text-tertiary);
      }
    }

    .file-actions {
      opacity: 0;
      transition: opacity 0.2s;
    }
  }
}

.pagination-bar {
  padding: 12px 20px;
  border-top: 1px solid var(--dt-border-light);
  display: flex;
  justify-content: flex-end;
}
</style>
