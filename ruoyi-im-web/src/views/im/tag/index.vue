<template>
  <div class="app-container">
    <!-- 搜索工具栏 -->
    <el-form ref="queryForm" :model="queryParams" :inline="true" class="search-form">
      <el-form-item label="标签名称" prop="tagName">
        <el-input
          v-model="queryParams.tagName"
          placeholder="请输入标签名称"
          clearable
          size="small"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="标签类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="选择类型" clearable size="small">
          <el-option
            v-for="item in typeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
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

    <!-- 操作工具栏
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          >新增标签</el-button
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
    </el-row>

    <!-- 标签列表：展示所有标签信息，支持多选、查看、编辑和删除操作 -->
    <el-table v-loading="loading" :data="tagList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="标签名称" align="left" prop="tagName">
        <template #default="scope">
          <el-tag
            :type="scope.row.type === 'system' ? 'primary' : ''"
            :effect="scope.row.type === 'system' ? 'dark' : 'plain'"
          >
            {{ scope.row.tagName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标签类型" align="center" width="100">
        <template #default="scope">
          <el-tag :type="getTagTypeStyle(scope.row.type)">
            {{ getTagTypeLabel(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="使用次数" align="center" prop="useCount" width="100" />
      <el-table-column label="创建者" align="center" prop="createBy" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            v-if="scope.row.type !== 'system'"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            >修改</el-button
          >
          <el-button
            v-if="scope.row.type !== 'system'"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            >删除</el-button
          >
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleViewMessages(scope.row)"
            >查看消息</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <!-- 添加或修改标签对话框：用于创建新标签或编辑现有标签信息 -->
    <el-dialog v-model:visible="open" :title="title" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标签名称" prop="tagName">
          <el-input v-model="form.tagName" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="标签颜色" prop="color">
          <el-color-picker v-model="form.color" />
        </el-form-item>
        <el-form-item label="标签描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入标签描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看标签消息对话框 -->
    <el-dialog v-model="messageDialogVisible" title="标签消息" width="60%">
      <div v-loading="messageLoading" class="tag-messages">
        <div v-for="msg in tagMessages" :key="msg.id" class="message-item">
          <div class="message-header">
            <span class="sender">{{ msg.senderName }}</span>
            <span class="time">{{ parseTime(msg.createTime) }}</span>
          </div>
          <div class="message-content">
            <template v-if="msg.type === 'text'">
              {{ msg.content }}
            </template>
            <template v-else-if="msg.type === 'image'">
              <el-image
                style="max-width: 200px; max-height: 200px"
                :src="msg.content"
                :preview-src-list="[msg.content]"
              />
            </template>
            <template v-else-if="msg.type === 'file'">
              <div class="file-message">
                <i class="el-icon-document"></i>
                <span class="file-name">{{ msg.fileName }}</span>
                <el-button type="text" size="mini" @click="downloadFile(msg)">下载</el-button>
              </div>
            </template>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { modal } from '@/utils/message'
import Pagination from '@/components/Pagination'
import { listTag, addTag, updateTag, delTag, delTagBatch, listTagMessage } from '@/api/im/tag'

export default {
  name: 'Tag',
  components: {
    Pagination,
  },
  data() {
    return {
      loading: true,
      messageLoading: false,
      ids: [],
      multiple: true,
      total: 0,
      tagList: [],
      title: '',
      open: false,
      messageDialogVisible: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        tagName: undefined,
        type: undefined,
      },
      form: {
        tagName: undefined,
        color: '#409EFF',
        description: undefined,
      },
      rules: {
        tagName: [{ required: true, message: '标签名称不能为空', trigger: 'blur' }],
      },
      typeOptions: [
        { label: '系统标签', value: 'system' },
        { label: '用户标签', value: 'user' },
      ],
      tagMessages: [],
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询标签列表 */
    getList() {
      this.loading = true
      listTag(this.queryParams).then(response => {
        this.tagList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 取消按钮 */
    cancel() {
      this.open = false
      this.reset()
    },
    /** 表单重置 */
    reset() {
      this.form = {
        tagName: undefined,
        color: '#409EFF',
        description: undefined,
      }
      this.resetForm('form')
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
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加标签'
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      this.form = { ...row }
      this.open = true
      this.title = '修改标签'
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          const request = this.form.tagId ? updateTag(this.form) : addTag(this.form)
          request.then(() => {
            modal.success(this.form.tagId ? '修改成功' : '新增成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      modal
        .confirm('是否确认删除该标签？')
        .then(() => {
          return delTag(row.tagId)
        })
        .then(() => {
          modal.success('删除成功')
          this.getList()
        })
    },
    /** 批量删除按钮操作 */
    handleBatchDelete() {
      modal
        .confirm('是否确认删除选中的标签？')
        .then(() => {
          return delTagBatch(this.ids)
        })
        .then(() => {
          modal.success('批量删除成功')
          this.getList()
        })
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.tagId)
      this.multiple = !selection.length
    },
    /** 查看标签消息 */
    handleViewMessages(row) {
      this.messageDialogVisible = true
      this.messageLoading = true
      listTagMessage(row.tagId).then(response => {
        this.tagMessages = response.rows
        this.messageLoading = false
      })
    },
    /** 下载文件 */
    downloadFile(msg) {
      const link = document.createElement('a')
      link.href = msg.content
      link.download = msg.fileName || '未命名文件'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    },
    /** 获取标签类型样式 */
    getTagTypeStyle(type) {
      return type === 'system' ? 'primary' : 'success'
    },
    /** 获取标签类型标签文本 */
    getTagTypeLabel(type) {
      return type === 'system' ? '系统标签' : '用户标签'
    },
  },
}
</script>

<style lang="scss" scoped>
.app-container {
  .tag-messages {
    max-height: 500px;
    overflow-y: auto;
    padding: 10px;

    .message-item {
      margin-bottom: 20px;

      .message-header {
        margin-bottom: 5px;
        font-size: 12px;
        color: #909399;

        .sender {
          margin-right: 10px;
          font-weight: bold;
        }
      }

      .message-content {
        background-color: #f5f7fa;
        padding: 10px;
        border-radius: 4px;

        .file-message {
          display: flex;
          align-items: center;

          .el-icon-document {
            font-size: 20px;
            margin-right: 10px;
          }

          .file-name {
            flex: 1;
            margin-right: 10px;
          }
        }
      }
    }
  }
}
</style>
