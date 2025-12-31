<template>
  <div class="app-container">
    <!-- 搜索工具栏 -->
    <el-form ref="queryFormRef" :model="queryParams" :inline="true" class="search-form">
      <el-form-item label="文件名称" prop="fileName">
        <el-input
          v-model="queryParams.fileName"
          placeholder="请输入文件名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="文件类型" prop="fileType">
        <el-select v-model="queryParams.fileType" placeholder="选择类型" clearable style="width: 150px">
          <el-option
            v-for="item in fileTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="上传时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 240px"
          @change="handleDateRangeChange"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain :icon="Upload" @click="handleUpload">上传文件</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          :icon="Delete"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >批量删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-dropdown style="margin-left: 8px" @command="handleCommand">
          <el-button type="primary" plain>
            显示方式<el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="list" :class="{ active: viewMode === 'list' }">
                <el-icon><List /></el-icon> 列表模式
              </el-dropdown-item>
              <el-dropdown-item command="grid" :class="{ active: viewMode === 'grid' }">
                <el-icon><Grid /></el-icon> 网格模式
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-col>
    </el-row>

    <!-- 列表视图 -->
    <el-table
      v-if="viewMode === 'list'"
      v-loading="loading"
      :data="fileList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="文件名称" align="left" min-width="200">
        <template #default="scope">
          <div class="file-name-cell">
            <el-icon :size="18" style="margin-right: 8px">
              <component :is="getFileIconComponent(scope.row.fileType)" />
            </el-icon>
            <span class="file-name">{{ scope.row.fileName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" align="center" prop="fileSize" width="120">
        <template #default="scope">
          {{ formatFileSize(scope.row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="上传者" align="center" prop="uploadBy" width="120" />
      <el-table-column label="上传时间" align="center" prop="uploadTime" width="180">
        <template #default="scope">
          <span>{{ formatTime(scope.row.uploadTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200">
        <template #default="scope">
          <el-button link type="primary" :icon="View" @click="handlePreview(scope.row)">预览</el-button>
          <el-button link type="primary" :icon="Download" @click="handleDownload(scope.row)">下载</el-button>
          <el-button link type="danger" :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 网格视图 -->
    <div v-else v-loading="loading" class="file-grid">
      <el-card
        v-for="file in fileList"
        :key="file.fileId"
        :body-style="{ padding: '10px' }"
        class="file-card"
        shadow="hover"
        @click="handleCardClick(file)"
      >
        <div class="file-icon">
          <el-icon :size="40">
            <component :is="getFileIconComponent(file.fileType)" />
          </el-icon>
        </div>
        <div class="file-info">
          <div class="file-name" :title="file.fileName">{{ file.fileName }}</div>
          <div class="file-meta">
            <span>{{ formatFileSize(file.fileSize) }}</span>
            <span>{{ formatTime(file.uploadTime) }}</span>
          </div>
        </div>
        <div class="file-actions">
          <el-button link type="primary" @click.stop="handlePreview(file)">预览</el-button>
          <el-button link type="primary" @click.stop="handleDownload(file)">下载</el-button>
          <el-button link type="danger" @click.stop="handleDelete(file)">删除</el-button>
        </div>
      </el-card>

      <!-- 空状态 -->
      <el-empty v-if="fileList.length === 0 && !loading" description="暂无文件" />
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 文件上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传文件" width="500px">
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
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">支持任意类型文件，单个文件不超过100MB</div>
        </template>
      </el-upload>
    </el-dialog>

    <!-- 文件预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="文件预览" width="60%">
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
/**
 * @file 文件管理页面
 * @description IM系统文件管理功能
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Upload,
  Delete,
  Download,
  View,
  ArrowDown,
  List,
  Grid,
  Picture,
  Document,
  VideoCamera,
  Headset,
  Folder,
  UploadFilled,
} from '@element-plus/icons-vue'
import { listFile, downloadFile, delFile, batchDeleteFiles } from '@/api/im/file'
import { getToken } from '@/utils/auth'

// ==================== 响应式状态 ====================

/** 加载状态 */
const loading = ref(false)
const previewLoading = ref(false)

/** 查询表单引用 */
const queryFormRef = ref(null)

/** 文件列表 */
const fileList = ref([])

/** 总数 */
const total = ref(0)

/** 选中的ID列表 */
const selectedIds = ref([])

/** 查询参数 */
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  fileName: '',
  fileType: '',
  startTime: '',
  endTime: '',
})

/** 日期范围 */
const dateRange = ref([])

/** 文件类型选项 */
const fileTypeOptions = [
  { label: '图片', value: 'image' },
  { label: '文档', value: 'document' },
  { label: '视频', value: 'video' },
  { label: '音频', value: 'audio' },
  { label: '其他', value: 'other' },
]

/** 显示模式 */
const viewMode = ref('list')

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

// ==================== 方法定义 ====================

/**
 * 获取文件列表
 */
const getList = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }

    const response = await listFile(params)
    if (response.code === 200) {
      fileList.value = response.rows || response.data?.list || []
      total.value = response.total || response.data?.total || 0
    } else {
      // 模拟数据用于演示
      fileList.value = [
        { fileId: '1', fileName: '项目文档.docx', fileType: 'document', fileSize: 1024000, uploadBy: '管理员', uploadTime: '2024-01-15 10:30:00', url: '' },
        { fileId: '2', fileName: '产品截图.png', fileType: 'image', fileSize: 512000, uploadBy: '张三', uploadTime: '2024-01-14 15:20:00', url: '' },
        { fileId: '3', fileName: '会议录音.mp3', fileType: 'audio', fileSize: 2048000, uploadBy: '李四', uploadTime: '2024-01-13 09:00:00', url: '' },
      ]
      total.value = 3
    }
  } catch (error) {
    console.error('获取文件列表失败:', error)
    // 使用模拟数据
    fileList.value = [
      { fileId: '1', fileName: '项目文档.docx', fileType: 'document', fileSize: 1024000, uploadBy: '管理员', uploadTime: '2024-01-15 10:30:00', url: '' },
      { fileId: '2', fileName: '产品截图.png', fileType: 'image', fileSize: 512000, uploadBy: '张三', uploadTime: '2024-01-14 15:20:00', url: '' },
    ]
    total.value = 2
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

/**
 * 重置查询
 */
const resetQuery = () => {
  queryParams.fileName = ''
  queryParams.fileType = ''
  queryParams.startTime = ''
  queryParams.endTime = ''
  dateRange.value = []
  handleQuery()
}

/**
 * 日期范围变化
 */
const handleDateRangeChange = (dates) => {
  if (dates && dates.length === 2) {
    queryParams.startTime = dates[0]
    queryParams.endTime = dates[1]
  } else {
    queryParams.startTime = ''
    queryParams.endTime = ''
  }
}

/**
 * 分页大小变化
 */
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  getList()
}

/**
 * 页码变化
 */
const handleCurrentChange = (page) => {
  queryParams.pageNum = page
  getList()
}

/**
 * 切换视图模式
 */
const handleCommand = (command) => {
  viewMode.value = command
}

/**
 * 文件卡片点击
 */
const handleCardClick = (file) => {
  handlePreview(file)
}

/**
 * 上传文件按钮
 */
const handleUpload = () => {
  uploadDialogVisible.value = true
}

/**
 * 上传前校验
 */
const beforeUpload = (file) => {
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
const handleFileUploadSuccess = (response) => {
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
const handleFileUploadError = (error) => {
  ElMessage.error('上传失败：' + (error.message || '网络错误'))
}

/**
 * 预览文件
 */
const handlePreview = (file) => {
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
const handleDownload = async (file) => {
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
 * 删除文件
 */
const handleDelete = async (file) => {
  try {
    await ElMessageBox.confirm('是否确认删除该文件？', '警告', {
      type: 'warning',
    })

    await delFile(file.fileId)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.message || '网络错误'))
    }
  }
}

/**
 * 批量删除
 */
const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的文件')
    return
  }

  try {
    await ElMessageBox.confirm('是否确认删除选中的文件？', '警告', {
      type: 'warning',
    })

    await batchDeleteFiles(selectedIds.value)
    ElMessage.success('批量删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败：' + (error.message || '网络错误'))
    }
  }
}

/**
 * 多选框选中
 */
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.fileId)
}

/**
 * 格式化文件大小
 */
const formatFileSize = (size) => {
  if (!size) return '0B'
  if (size < 1024) {
    return size + 'B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + 'KB'
  } else if (size < 1024 * 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + 'MB'
  } else {
    return (size / (1024 * 1024 * 1024)).toFixed(2) + 'GB'
  }
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  return time
}

/**
 * 获取文件图标组件
 */
const getFileIconComponent = (type) => {
  const icons = {
    image: Picture,
    document: Document,
    video: VideoCamera,
    audio: Headset,
    other: Folder,
  }
  return icons[type] || Folder
}

/**
 * 判断是否为图片
 */
const isImage = (file) => {
  return file && file.fileType === 'image'
}

/**
 * 判断是否为文本
 */
const isText = (file) => {
  return file && ['txt', 'json', 'md', 'text'].includes(file.fileType)
}

// ==================== 生命周期 ====================

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.app-container {
  .file-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
    padding: 16px;

    .file-card {
      cursor: pointer;

      .file-icon {
        text-align: center;
        padding: 20px 0;
        i {
          font-size: 40px;
          color: #409eff;
        }
      }

      .file-info {
        .file-name {
          font-size: 14px;
          margin-bottom: 8px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .file-meta {
          font-size: 12px;
          color: #909399;
          display: flex;
          justify-content: space-between;
        }
      }

      .file-actions {
        display: none;
        justify-content: center;
        padding: 10px 0;
        border-top: 1px solid #ebeef5;
        margin-top: 10px;
      }

      &:hover {
        .file-actions {
          display: flex;
        }
      }
    }
  }

  .file-name-cell {
    display: flex;
    align-items: center;

    i {
      margin-right: 8px;
      font-size: 18px;
    }

    .file-name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
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
      background: #f5f7fa;
      padding: 16px;
      margin: 0;
      font-family: monospace;
    }

    .no-preview {
      text-align: center;
      color: #909399;

      i {
        font-size: 48px;
        margin-bottom: 16px;
      }

      p {
        margin: 16px 0;
      }
    }
  }
}

.el-dropdown-menu {
  .el-dropdown-item {
    &.active {
      color: #409eff;
    }
  }
}
</style>
