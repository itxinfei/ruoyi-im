<template>
  <div class="app-container">
    <!-- 搜索工具栏 -->
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" :inline="true">
      <el-form-item label="文档名称" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入文档名称" clearable size="small" />
      </el-form-item>
      <el-form-item label="文档类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择文档类型" clearable size="small">
          <el-option
            v-for="dict in typeOptions"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
          >上传文档</el-button
        >
      </el-col>
      <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
    </el-row>

    <!-- 文档列表 -->
    <el-table v-loading="loading" :data="documentList">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="文档名称" align="center" prop="title" />
      <el-table-column label="文档类型" align="center" prop="type">
        <template #default="scope">
          <dict-tag :options="typeOptions" :value="scope.row.type" />
        </template>
      </el-table-column>
      <el-table-column label="文件大小" align="center" prop="size">
        <template #default="scope">
          {{ formatFileSize(scope.row.size) }}
        </template>
      </el-table-column>
      <el-table-column label="创建者" align="center" prop="creatorName" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handlePreview(scope.row)"
            >预览</el-button
          >
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)"
            >编辑</el-button
          >
          <el-button size="mini" type="text" icon="el-icon-share" @click="handleShare(scope.row)"
            >分享</el-button
          >
          <el-dropdown>
            <span class="el-dropdown-link">
              <i class="el-icon-more"></i>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleVersion(scope.row)">版本历史</el-dropdown-item>
                <el-dropdown-item @click="handleDelete(scope.row)">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <pagination
      v-show="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <!-- 上传文档对话框 -->
    <el-dialog v-model="upload.open" :title="upload.title" width="400px" append-to-body>
      <el-upload
        class="upload-demo"
        :action="upload.url"
        :headers="upload.headers"
        :before-upload="beforeUpload"
        :on-success="onUploadSuccess"
        :on-error="onUploadError"
        :file-list="upload.fileList"
      >
        <el-button size="small" type="primary">点击上传</el-button>
        <template #tip>
          <div class="el-upload__tip">只能上传office文档，且不超过50MB</div>
        </template>
      </el-upload>
    </el-dialog>

    <!-- 分享文档对话框 -->
    <el-dialog v-model:visible="share.open" :title="share.title" width="400px" append-to-body>
      <el-form :model="share.form" label-width="80px">
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="share.form.expireTime"
            type="datetime"
            placeholder="选择过期时间"
          >
          </el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitShare">确 定</el-button>
          <el-button @click="share.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getDocumentList, deleteDocument, shareDocument } from '@/api/im/document'

export default {
  name: 'Document',
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 文档表格数据
      documentList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        type: undefined,
      },
      // 文档类型选项
      typeOptions: [
        { value: 'docx', label: 'Word文档' },
        { value: 'xlsx', label: 'Excel表格' },
        { value: 'pptx', label: 'PPT演示文稿' },
        { value: 'pdf', label: 'PDF文档' },
      ],
      // 上传参数
      upload: {
        open: false,
        title: '上传文档',
        url: process.env.VUE_APP_BASE_API + '/im/document/upload',
        headers: { Authorization: 'Bearer ' + this.getToken() },
        fileList: [],
      },
      // 分享参数
      share: {
        open: false,
        title: '分享文档',
        form: {
          documentId: undefined,
          expireTime: undefined,
        },
      },
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询文档列表 */
    getList() {
      this.loading = true
      getDocumentList(this.queryParams).then(response => {
        this.documentList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    /** 上传按钮操作 */
    handleUpload() {
      this.upload.open = true
      this.upload.fileList = []
    },
    /** 文件上传前的处理 */
    beforeUpload() {
      // 验证文件类型和大小
      return true
    },
    /** 文件上传成功的处理 */
    onUploadSuccess() {
      this.msgSuccess('上传成功')
      this.upload.open = false
      this.getList()
    },
    /** 文件上传失败的处理 */
    onUploadError() {
      this.msgError('上传失败')
    },
    /** 预览按钮操作 */
    handlePreview() {
      // TODO: 实现文档预览
    },
    /** 编辑按钮操作 */
    handleEdit() {
      // TODO: 实现文档编辑
    },
    /** 分享按钮操作 */
    handleShare(row) {
      this.share.open = true
      this.share.form.documentId = row.documentId
      this.share.form.expireTime = undefined
    },
    /** 提交分享 */
    submitShare() {
      shareDocument(this.share.form.documentId, this.share.form.expireTime).then(() => {
        this.msgSuccess('分享成功')
        this.share.open = false
      })
    },
    /** 版本历史按钮操作 */
    handleVersion() {
      // TODO: 实现版本历史查看
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$confirm('是否确认删除该文档?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          return deleteDocument(row.documentId)
        })
        .then(() => {
          this.getList()
          this.msgSuccess('删除成功')
        })
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
  },
}
</script>
