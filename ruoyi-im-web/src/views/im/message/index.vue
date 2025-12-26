<template>
  <div class="app-container">
    <el-form
      v-show="showSearch"
      ref="queryForm"
      :model="queryParams"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="消息类型" prop="messageType">
        <el-select v-model="queryParams.messageType" placeholder="消息类型" clearable size="small">
          <el-option label="私聊消息" value="private" />
          <el-option label="群聊消息" value="group" />
          <el-option label="系统消息" value="system" />
        </el-select>
      </el-form-item>
      <el-form-item label="内容类型" prop="contentType">
        <el-select v-model="queryParams.contentType" placeholder="内容类型" clearable size="small">
          <el-option label="文本" value="text" />
          <el-option label="图片" value="image" />
          <el-option label="文件" value="file" />
          <el-option label="语音" value="voice" />
        </el-select>
      </el-form-item>
      <el-form-item label="发送者" prop="senderName">
        <el-input
          v-model="queryParams.senderName"
          placeholder="请输入发送者"
          clearable
          size="small"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="接收者" prop="receiverName">
        <el-input
          v-model="queryParams.receiverName"
          placeholder="请输入接收者"
          clearable
          size="small"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="时间范围" prop="timeRange">
        <el-date-picker
          v-model="queryParams.timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="small"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['im:message:remove']"
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          >删除</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['im:message:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          >导出</el-button
        >
      </el-col>
      <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="messageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="消息ID" align="center" prop="messageId" width="120" />
      <el-table-column label="消息类型" align="center" prop="messageType" width="100">
        <template #default="scope">
          <el-tag
            :type="
              scope.row.messageType === 'private'
                ? ''
                : scope.row.messageType === 'group'
                  ? 'success'
                  : 'info'
            "
          >
            {{
              scope.row.messageType === 'private'
                ? '私聊'
                : scope.row.messageType === 'group'
                  ? '群聊'
                  : '系统'
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="内容类型" align="center" prop="contentType" width="100">
        <template #default="scope">
          <el-tag :type="getContentTypeTag(scope.row.contentType)">
            {{ getContentTypeLabel(scope.row.contentType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发送者" align="center" prop="senderName" width="120" />
      <el-table-column label="接收者" align="center" prop="receiverName" width="120" />
      <el-table-column
        label="消息内容"
        align="center"
        prop="content"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="发送时间" align="center" prop="sendTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.sendTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag
            :type="
              scope.row.status === 'sent'
                ? 'success'
                : scope.row.status === 'received'
                  ? 'info'
                  : 'warning'
            "
          >
            {{
              scope.row.status === 'sent'
                ? '已发送'
                : scope.row.status === 'received'
                  ? '已送达'
                  : '已读'
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        class-name="small-padding fixed-width"
        width="150"
      >
        <template #default="scope">
          <el-button
            v-hasPermi="['im:message:query']"
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            >查看</el-button
          >
          <el-button
            v-hasPermi="['im:message:remove']"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <!-- 消息详情对话框 -->
    <el-dialog v-model="open" :title="title" width="700px" append-to-body>
      <el-form ref="form" :model="form" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="消息ID">{{ form.messageId }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="消息类型">
              {{
                form.messageType === 'private'
                  ? '私聊'
                  : form.messageType === 'group'
                    ? '群聊'
                    : '系统'
              }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发送者">{{ form.senderName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接收者">{{ form.receiverName }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="消息内容">
              <div v-if="form.contentType === 'text'" class="message-content">
                {{ form.content }}
              </div>
              <div v-else-if="form.contentType === 'image'" class="message-content">
                <img :src="form.content" style="max-width: 100%; max-height: 300px" />
              </div>
              <div v-else-if="form.contentType === 'file'" class="message-content">
                <a :href="form.content" target="_blank">下载文件</a>
              </div>
              <div v-else-if="form.contentType === 'voice'" class="message-content">
                <audio :src="form.content" controls></audio>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发送时间">{{ parseTime(form.sendTime) }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              {{
                form.status === 'sent' ? '已发送' : form.status === 'received' ? '已送达' : '已读'
              }}
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { listMessage, getMessage, delMessage } from '@/api/im/message'

export default {
  name: 'ImMessage',
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 消息表格数据
      messageList: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        messageType: undefined,
        contentType: undefined,
        senderName: undefined,
        receiverName: undefined,
        timeRange: [],
      },
      // 表单参数
      form: {},
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询消息列表 */
    getList() {
      this.loading = true
      listMessage(this.queryParams).then(response => {
        this.messageList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 获取内容类型标签样式 */
    getContentTypeTag(type) {
      const map = {
        text: '',
        image: 'success',
        file: 'warning',
        voice: 'info',
      }
      return map[type] || ''
    },
    /** 获取内容类型显示文本 */
    getContentTypeLabel(type) {
      const map = {
        text: '文本',
        image: '图片',
        file: '文件',
        voice: '语音',
      }
      return map[type] || type
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.messageId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
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
    /** 删除按钮操作 */
    handleDelete(row) {
      const messageIds = row.messageId || this.ids
      this.$modal
        .confirm('是否确认删除消息编号为"' + messageIds + '"的数据项？')
        .then(function () {
          return delMessage(messageIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download(
        'im/message/export',
        {
          ...this.queryParams,
        },
        `message_${new Date().getTime()}.xlsx`
      )
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.title = '消息详情'
      getMessage(row.messageId).then(response => {
        this.form = response.data
        this.open = true
      })
    },
  },
}
</script>

<style scoped>
.message-content {
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  min-height: 60px;
  word-break: break-all;
}
</style>
