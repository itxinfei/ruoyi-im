<template>
  <div class="app-container">
    <!-- 搜索工具栏 -->
    <el-form ref="queryForm" :model="queryParams" :inline="true" class="search-form">
      <el-form-item label="关键词" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索消息内容"
          clearable
          size="small"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="归档类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="选择类型" clearable size="small">
          <el-option
            v-for="item in typeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围" prop="dateRange">
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
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          >新增归档</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" @click="goToRules"
          >归档规则</el-button
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
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          >导出</el-button
        >
      </el-col>
    </el-row>

    <!-- 归档列表 -->
    <el-table v-loading="loading" :data="archiveList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="归档ID" align="center" prop="archiveId" width="100" />
      <el-table-column label="会话类型" align="center" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.sessionType === 'private' ? '' : 'success'">
            {{ scope.row.sessionType === 'private' ? '私聊' : '群聊' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="会话名称"
        align="center"
        prop="sessionName"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="归档类型" align="center" width="100">
        <template #default="scope">
          <el-tag :type="getArchiveTypeTag(scope.row.type)">
            {{ getArchiveTypeLabel(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="消息数量" align="center" prop="messageCount" width="100" />
      <el-table-column label="归档时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)"
            >查看</el-button
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

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <!-- 查看归档对话框 -->
    <el-dialog
      v-model:visible="viewDialogVisible"
      :title="'查看归档 - ' + archiveDetail.sessionName"
      width="60%"
    >
      <div v-loading="messageLoading" class="archive-messages">
        <div v-for="msg in archiveMessages" :key="msg.id" class="message-item">
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
import { parseTime } from '@/utils'
import { modal } from '@/utils/message'
import Pagination from '@/components/Pagination'
import {
  listArchive,
  delArchive,
  delArchiveBatch,
  exportArchive,
  listArchiveMessage,
  downloadArchiveFile,
} from '@/api/im/archive'

export default {
  name: 'Archive',
  components: {
    Pagination,
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      messageLoading: false,
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 归档列表数据
      archiveList: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      viewDialogVisible: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        keyword: undefined,
        type: undefined,
        startTime: undefined,
        endTime: undefined,
      },
      // 日期范围
      dateRange: [],
      // 类型选项
      typeOptions: [
        { label: '手动归档', value: 'manual' },
        { label: '自动归档', value: 'auto' },
        { label: '规则归档', value: 'rule' },
      ],
      // 归档详情
      archiveDetail: {},
      // 归档消息列表
      archiveMessages: [],
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询归档列表 */
    getList() {
      this.loading = true
      listArchive(this.queryParams).then(response => {
        this.archiveList = response.rows
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
    /** 新增归档按钮操作 */
    handleAdd() {
      this.$router.push('/im/archive/add')
    },
    /** 跳转到归档规则页面 */
    goToRules() {
      this.$router.push('/im/archive/rule')
    },
    /** 导出按钮操作 */
    handleExport() {
      this.$message.success('正在导出归档数据')
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.archiveId)
      this.multiple = !selection.length
    },
    /** 查看归档内容 */
    handleView(row) {
      this.archiveDetail = row
      this.viewDialogVisible = true
      this.messageLoading = true
      listArchiveMessage(row.archiveId).then(response => {
        this.archiveMessages = response.data
        this.messageLoading = false
      })
    },
    /** 下载归档 */
    handleDownload(row) {
      exportArchive(row.archiveId).then(response => {
        const blob = new Blob([response.data])
        const fileName = `归档_${row.sessionName}_${parseTime(row.createTime, '{y}{m}{d}')}.zip`
        const link = document.createElement('a')
        link.href = window.URL.createObjectURL(blob)
        link.download = fileName
        link.click()
        window.URL.revokeObjectURL(link.href)
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      modal
        .confirm('是否确认删除该归档？')
        .then(() => {
          return delArchive(row.archiveId)
        })
        .then(() => {
          modal.success('删除成功')
          this.getList()
        })
    },
    /** 批量删除操作 */
    handleBatchDelete() {
      modal
        .confirm('是否确认删除选中的归档？')
        .then(() => {
          return delArchiveBatch(this.ids)
        })
        .then(() => {
          modal.success('批量删除成功')
          this.getList()
        })
    },
    /** 下载文件 */
    downloadFile(msg) {
      downloadArchiveFile(msg.fileId).then(response => {
        const blob = new Blob([response.data])
        const fileName = msg.fileName
        const link = document.createElement('a')
        link.href = window.URL.createObjectURL(blob)
        link.download = fileName
        link.click()
        window.URL.revokeObjectURL(link.href)
      })
    },
    /** 获取归档类型标签样式 */
    getArchiveTypeTag(type) {
      const tags = {
        manual: '',
        auto: 'success',
        rule: 'warning',
      }
      return tags[type] || 'info'
    },
    /** 获取归档类型显示文本 */
    getArchiveTypeLabel(type) {
      const labels = {
        manual: '手动归档',
        auto: '自动归档',
        rule: '规则归档',
      }
      return labels[type] || type
    },
  },
}
</script>

<style lang="scss" scoped>
.app-container {
  .search-form {
    margin-bottom: 20px;
  }

  .archive-messages {
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
