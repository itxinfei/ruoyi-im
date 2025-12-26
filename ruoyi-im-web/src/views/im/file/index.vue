<template>
  <div class="app-container">
    <!-- 搜索工具栏 -->
    <el-form ref="queryForm" :model="queryParams" :inline="true" class="search-form">
      <el-form-item label="文件名称" prop="fileName">
        <el-input
          v-model="queryParams.fileName"
          placeholder="请输入文件名称"
          clearable
          size="small"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="文件类型" prop="fileType">
        <el-select v-model="queryParams.fileType" placeholder="选择类型" clearable size="small">
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
          size="small"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          @change="handleDateRangeChange"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-upload" size="mini" @click="handleUpload"
          >上传文件</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleBatchDelete"
          >批量删除</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-dropdown style="margin-left: 8px" @command="handleCommand">
          <el-button type="primary" plain size="mini">
            显示方式<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="list" :class="{ active: viewMode === 'list' }">
                <i class="el-icon-menu"></i> 列表模式
              </el-dropdown-item>
              <el-dropdown-item command="grid" :class="{ active: viewMode === 'grid' }">
                <i class="el-icon-s-grid"></i> 网格模式
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
            <i :class="getFileIcon(scope.row.fileType)"></i>
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
          <span>{{ parseTime(scope.row.uploadTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handlePreview(scope.row)"
            >预览</el-button
          >
          <el-button
            size="mini"
            type="text"
            icon="el-icon-download"
            @click="handleDownload(scope.row)"
            >下载</el-button
          >
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            >删除</el-button
          >
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
          <i :class="getFileIcon(file.fileType)"></i>
        </div>
        <div class="file-info">
          <div class="file-name" :title="file.fileName">{{ file.fileName }}</div>
          <div class="file-meta">
            <span>{{ formatFileSize(file.fileSize) }}</span>
            <span>{{ parseTime(file.uploadTime) }}</span>
          </div>
        </div>
        <div class="file-actions">
          <el-button type="text" icon="el-icon-view" @click.stop="handlePreview(file)"
            >预览</el-button
          >
          <el-button type="text" icon="el-icon-download" @click.stop="handleDownload(file)"
            >下载</el-button
          >
          <el-button type="text" icon="el-icon-delete" @click.stop="handleDelete(file)"
            >删除</el-button
          >
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <!-- 文件上传对话框 -->
    <el-dialog v-model:visible="uploadDialogVisible" :title="'上传文件'" width="500px">
      <el-upload
        class="upload-demo"
        drag
        action="#"
        :http-request="uploadFile"
        :before-upload="beforeUpload"
        multiple
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">支持任意类型文件，单个文件不超过100MB</div>
        </template>
      </el-upload>
    </el-dialog>

    <!-- 文件预览对话框 -->
    <el-dialog v-model:visible="previewDialogVisible" :title="'文件预览'" width="60%">
      <div v-loading="previewLoading" class="preview-container">
        <div v-if="isImage(currentFile)" class="image-preview">
          <el-image :src="currentFile.url" :preview-src-list="[currentFile.url]" />
        </div>
        <div v-else-if="isText(currentFile)" class="text-preview">
          <pre>{{ previewContent }}</pre>
        </div>
        <div v-else class="no-preview">
          <i :class="getFileIcon(currentFile ? currentFile.fileType : 'other')"></i>
          <p>该文件类型暂不支持预览，请下载后查看</p>
          <el-button type="primary" @click="handleDownload(currentFile)">下载文件</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'File',
  data() {
    return {
      // 遮罩层
      loading: true,
      previewLoading: false,
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 文件列表
      fileList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        fileName: undefined,
        fileType: undefined,
        startTime: undefined,
        endTime: undefined,
      },
      // 日期范围
      dateRange: [],
      // 文件类型选项
      fileTypeOptions: [
        { label: '图片', value: 'image' },
        { label: '文档', value: 'document' },
        { label: '视频', value: 'video' },
        { label: '音频', value: 'audio' },
        { label: '其他', value: 'other' },
      ],
      // 显示模式：list/grid
      viewMode: 'list',
      // 对话框显示控制
      uploadDialogVisible: false,
      previewDialogVisible: false,
      // 当前预览的文件
      currentFile: null,
      // 预览内容
      previewContent: '',
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询文件列表 */
    getList() {
      this.loading = true
      // 模拟异步请求
      setTimeout(() => {
        this.fileList = [
          {
            fileId: '1',
            fileName: '示例图片.jpg',
            fileType: 'image',
            fileSize: 1024 * 1024 * 2, // 2MB
            uploadBy: 'admin',
            uploadTime: '2024-01-01 10:00:00',
            url: 'https://example.com/image.jpg',
          },
          {
            fileId: '2',
            fileName: '文档.pdf',
            fileType: 'document',
            fileSize: 1024 * 1024 * 1.5, // 1.5MB
            uploadBy: 'admin',
            uploadTime: '2024-01-02 10:00:00',
            url: 'https://example.com/doc.pdf',
          },
        ]
        this.total = this.fileList.length
        this.loading = false
      }, 500)
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    /** 日期范围变化 */
    handleDateRangeChange(dates) {
      if (dates) {
        this.queryParams.startTime = dates[0]
        this.queryParams.endTime = dates[1]
      } else {
        this.queryParams.startTime = undefined
        this.queryParams.endTime = undefined
      }
    },
    /** 切换视图模式 */
    handleCommand(command) {
      this.viewMode = command
    },
    /** 文件卡片点击 */
    handleCardClick(file) {
      this.handlePreview(file)
    },
    /** 上传文件按钮操作 */
    handleUpload() {
      this.uploadDialogVisible = true
    },
    /** 上传前校验 */
    beforeUpload(file) {
      const isLt100M = file.size / 1024 / 1024 < 100
      if (!isLt100M) {
        this.$message.error('文件大小不能超过 100MB!')
        return false
      }
      return true
    },
    /** 自定义上传 */
    uploadFile(params) {
      const formData = new FormData()
      formData.append('file', params.file)
      // 这里应该调用实际的上传API
      this.$message.success('上传成功')
      this.uploadDialogVisible = false
      this.getList()
    },
    /** 预览文件 */
    handlePreview(file) {
      this.currentFile = file
      this.previewDialogVisible = true
      this.previewLoading = true

      if (this.isText(file)) {
        // 模拟加载文本内容
        setTimeout(() => {
          this.previewContent = '这是文件内容...'
          this.previewLoading = false
        }, 500)
      } else {
        this.previewLoading = false
      }
    },
    /** 下载文件 */
    handleDownload(file) {
      window.open(file.url)
    },
    /** 删除按钮操作 */
    handleDelete() {
      this.$confirm('是否确认删除该文件？', '警告', {
        type: 'warning',
      }).then(() => {
        this.$message.success('删除成功')
        this.getList()
      })
    },
    /** 批量删除操作 */
    handleBatchDelete() {
      this.$confirm('是否确认删除选中的文件？', '警告', {
        type: 'warning',
      }).then(() => {
        this.$message.success('批量删除成功')
        this.getList()
      })
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.fileId)
      this.multiple = !selection.length
    },
    /** 格式化文件大小 */
    formatFileSize(size) {
      if (size < 1024) {
        return size + 'B'
      } else if (size < 1024 * 1024) {
        return (size / 1024).toFixed(2) + 'KB'
      } else if (size < 1024 * 1024 * 1024) {
        return (size / (1024 * 1024)).toFixed(2) + 'MB'
      } else {
        return (size / (1024 * 1024 * 1024)).toFixed(2) + 'GB'
      }
    },
    /** 获取文件图标 */
    getFileIcon(type) {
      const icons = {
        image: 'el-icon-picture',
        document: 'el-icon-document',
        video: 'el-icon-video-camera',
        audio: 'el-icon-headset',
        other: 'el-icon-document',
      }
      return icons[type] || icons.other
    },
    /** 判断是否为图片 */
    isImage(file) {
      return file && file.fileType === 'image'
    },
    /** 判断是否为文本 */
    isText(file) {
      return file && ['txt', 'json', 'md'].includes(file.fileType)
    },
  },
}
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
