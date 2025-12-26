<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- 操作日志 -->
      <el-tab-pane label="操作日志" name="operation">
        <el-form
          v-show="showSearch"
          ref="queryForm"
          :model="queryParams"
          :inline="true"
          label-width="68px"
        >
          <el-form-item label="操作人" prop="operName">
            <el-input
              v-model="queryParams.operName"
              placeholder="请输入操作人"
              clearable
              size="small"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="操作类型" prop="operType">
            <el-select v-model="queryParams.operType" placeholder="操作类型" clearable size="small">
              <el-option label="查询" value="SELECT" />
              <el-option label="新增" value="INSERT" />
              <el-option label="修改" value="UPDATE" />
              <el-option label="删除" value="DELETE" />
              <el-option label="授权" value="GRANT" />
              <el-option label="导出" value="EXPORT" />
              <el-option label="导入" value="IMPORT" />
              <el-option label="强退" value="FORCE" />
              <el-option label="清空数据" value="CLEAN" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="操作状态" clearable size="small">
              <el-option label="成功" value="0" />
              <el-option label="失败" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作时间">
            <el-date-picker
              v-model="dateRange"
              size="small"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            ></el-date-picker>
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
              v-hasPermi="['im:log:remove']"
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
              v-hasPermi="['im:log:export']"
              type="warning"
              plain
              icon="el-icon-download"
              size="mini"
              @click="handleExport"
              >导出</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button
              v-hasPermi="['im:log:remove']"
              type="danger"
              plain
              icon="el-icon-delete"
              size="mini"
              @click="handleClean"
              >清空</el-button
            >
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="operLogList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="日志编号" align="center" prop="operId" />
          <el-table-column label="操作模块" align="center" prop="title" />
          <el-table-column label="操作类型" align="center" prop="operType">
            <template #default="scope">
              <el-tag :type="getOperTypeTag(scope.row.operType)">{{ scope.row.operType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作人员" align="center" prop="operName" />
          <el-table-column label="操作状态" align="center" prop="status">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
                {{ scope.row.status === '0' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作时间" align="center" prop="operTime" width="180">
            <template #default="scope">
              <span>{{ parseTime(scope.row.operTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)"
                >详细</el-button
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
      </el-tab-pane>

      <!-- WebSocket日志 -->
      <el-tab-pane label="WebSocket日志" name="websocket">
        <el-form
          v-show="showSearch"
          ref="queryForm"
          :model="queryParams"
          :inline="true"
          label-width="68px"
        >
          <el-form-item label="连接ID" prop="connectionId">
            <el-input
              v-model="queryParams.connectionId"
              placeholder="请输入连接ID"
              clearable
              size="small"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="用户ID" prop="userId">
            <el-input
              v-model="queryParams.userId"
              placeholder="请输入用户ID"
              clearable
              size="small"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="事件类型" prop="eventType">
            <el-select
              v-model="queryParams.eventType"
              placeholder="事件类型"
              clearable
              size="small"
            >
              <el-option label="连接" value="CONNECT" />
              <el-option label="断开" value="DISCONNECT" />
              <el-option label="心跳" value="HEARTBEAT" />
              <el-option label="消息" value="MESSAGE" />
              <el-option label="错误" value="ERROR" />
            </el-select>
          </el-form-item>
          <el-form-item label="记录时间">
            <el-date-picker
              v-model="dateRange"
              size="small"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery"
              >搜索</el-button
            >
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="wsLogList">
          <el-table-column label="连接ID" align="center" prop="connectionId" width="180" />
          <el-table-column label="用户ID" align="center" prop="userId" width="100" />
          <el-table-column label="事件类型" align="center" prop="eventType" width="100">
            <template #default="scope">
              <el-tag :type="getEventTypeTag(scope.row.eventType)">{{
                scope.row.eventType
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column
            label="事件内容"
            align="center"
            prop="content"
            :show-overflow-tooltip="true"
          />
          <el-table-column label="记录时间" align="center" prop="logTime" width="180">
            <template #default="scope">
              <span>{{ parseTime(scope.row.logTime) }}</span>
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
      </el-tab-pane>

      <!-- 消息队列日志 -->
      <el-tab-pane label="消息队列日志" name="mq">
        <el-form
          v-show="showSearch"
          ref="queryForm"
          :model="queryParams"
          :inline="true"
          label-width="68px"
        >
          <el-form-item label="队列名称" prop="queueName">
            <el-input
              v-model="queryParams.queueName"
              placeholder="请输入队列名称"
              clearable
              size="small"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="消息类型" prop="messageType">
            <el-select
              v-model="queryParams.messageType"
              placeholder="消息类型"
              clearable
              size="small"
            >
              <el-option label="生产" value="PRODUCE" />
              <el-option label="消费" value="CONSUME" />
              <el-option label="错误" value="ERROR" />
            </el-select>
          </el-form-item>
          <el-form-item label="记录时间">
            <el-date-picker
              v-model="dateRange"
              size="small"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery"
              >搜索</el-button
            >
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="mqLogList">
          <el-table-column label="队列名称" align="center" prop="queueName" />
          <el-table-column label="消息类型" align="center" prop="messageType" width="100">
            <template #default="scope">
              <el-tag :type="getMqTypeTag(scope.row.messageType)">{{
                scope.row.messageType
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="消息ID" align="center" prop="messageId" width="180" />
          <el-table-column
            label="消息内容"
            align="center"
            prop="content"
            :show-overflow-tooltip="true"
          />
          <el-table-column label="记录时间" align="center" prop="logTime" width="180">
            <template #default="scope">
              <span>{{ parseTime(scope.row.logTime) }}</span>
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
      </el-tab-pane>

      <!-- 错误日志 -->
      <el-tab-pane label="错误日志" name="error">
        <el-form
          v-show="showSearch"
          ref="queryForm"
          :model="queryParams"
          :inline="true"
          label-width="68px"
        >
          <el-form-item label="错误级别" prop="level">
            <el-select v-model="queryParams.level" placeholder="错误级别" clearable size="small">
              <el-option label="ERROR" value="ERROR" />
              <el-option label="WARN" value="WARN" />
              <el-option label="INFO" value="INFO" />
            </el-select>
          </el-form-item>
          <el-form-item label="错误来源" prop="source">
            <el-input
              v-model="queryParams.source"
              placeholder="请输入错误来源"
              clearable
              size="small"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="记录时间">
            <el-date-picker
              v-model="dateRange"
              size="small"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery"
              >搜索</el-button
            >
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="errorLogList">
          <el-table-column label="错误级别" align="center" prop="level" width="100">
            <template #default="scope">
              <el-tag :type="getErrorLevelTag(scope.row.level)">{{ scope.row.level }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="错误来源" align="center" prop="source" />
          <el-table-column
            label="错误信息"
            align="center"
            prop="message"
            :show-overflow-tooltip="true"
          />
          <el-table-column
            label="堆栈信息"
            align="center"
            prop="stackTrace"
            :show-overflow-tooltip="true"
          />
          <el-table-column label="记录时间" align="center" prop="logTime" width="180">
            <template #default="scope">
              <span>{{ parseTime(scope.row.logTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-view"
                @click="handleViewError(scope.row)"
                >详细</el-button
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
      </el-tab-pane>
    </el-tabs>

    <!-- 操作日志详细 -->
    <el-dialog v-model="openOperationDialog" title="操作日志详细" width="700px" append-to-body>
      <el-form ref="form" :model="form" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="操作模块：">{{ form.title }}</el-form-item>
            <el-form-item label="登录信息：">{{ form.operLocation }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="请求地址：">{{ form.operUrl }}</el-form-item>
            <el-form-item label="操作方法：">{{ form.method }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="请求参数：">{{ form.operParam }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="返回参数：">{{ form.jsonResult }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作状态：">
              <el-tag :type="form.status === 0 ? 'success' : 'danger'">
                {{ form.status === 0 ? '正常' : '异常' }}
              </el-tag>
            </el-form-item>
          </el-col>
          <el-col v-if="form.status === 1" :span="24">
            <el-form-item label="异常信息：">{{ form.errorMsg }}</el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="openOperationDialog = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 错误日志详细 -->
    <el-dialog v-model:visible="openErrorDialog" title="错误日志详细" width="700px" append-to-body>
      <el-form ref="errorForm" :model="errorForm" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="错误级别：">
              <el-tag :type="getErrorLevelTag(errorForm.level)">{{ errorForm.level }}</el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="错误来源：">{{ errorForm.source }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="错误信息：">{{ errorForm.message }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="堆栈信息：">
              <pre>{{ errorForm.stackTrace }}</pre>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="openErrorDialog = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  listOperLog,
  getOperLog,
  delOperLog,
  cleanOperLog,
  listWsLog,
  listMqLog,
  listErrorLog,
  getErrorLog,
} from '@/api/im/log'

export default {
  name: 'ImLog',
  data() {
    return {
      // 遮罩层
      loading: false,
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
      // 当前选中的标签页
      activeTab: 'operation',
      // 操作日志表格数据
      operLogList: [],
      // WebSocket日志表格数据
      wsLogList: [],
      // 消息队列日志表格数据
      mqLogList: [],
      // 错误日志表格数据
      errorLogList: [],
      // 是否显示操作日志详细对话框
      openOperationDialog: false,
      // 是否显示错误日志详细对话框
      openErrorDialog: false,
      // 日期范围
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        operName: undefined,
        operType: undefined,
        status: undefined,
        connectionId: undefined,
        userId: undefined,
        eventType: undefined,
        queueName: undefined,
        messageType: undefined,
        level: undefined,
        source: undefined,
      },
      // 表单参数
      form: {},
      // 错误日志表单参数
      errorForm: {},
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询日志列表 */
    getList() {
      this.loading = true
      switch (this.activeTab) {
        case 'operation':
          listOperLog(this.queryParams).then(response => {
            this.operLogList = response.rows
            this.total = response.total
            this.loading = false
          })
          break
        case 'websocket':
          listWsLog(this.queryParams).then(response => {
            this.wsLogList = response.rows
            this.total = response.total
            this.loading = false
          })
          break
        case 'mq':
          listMqLog(this.queryParams).then(response => {
            this.mqLogList = response.rows
            this.total = response.total
            this.loading = false
          })
          break
        case 'error':
          listErrorLog(this.queryParams).then(response => {
            this.errorLogList = response.rows
            this.total = response.total
            this.loading = false
          })
          break
      }
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
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.operId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const operIds = row.operId || this.ids
      this.$modal
        .confirm('是否确认删除日志编号为"' + operIds + '"的数据项？')
        .then(function () {
          return delOperLog(operIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    /** 清空按钮操作 */
    handleClean() {
      this.$modal
        .confirm('是否确认清空所有操作日志数据？')
        .then(function () {
          return cleanOperLog()
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('清空成功')
        })
        .catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download(
        'im/log/export',
        {
          ...this.queryParams,
        },
        `log_${new Date().getTime()}.xlsx`
      )
    },
    /** 查看按钮操作 */
    handleView(row) {
      getOperLog(row.operId).then(response => {
        this.form = response.data
        this.openOperationDialog = true
      })
    },
    /** 查看错误日志详细 */
    handleViewError(row) {
      getErrorLog(row.logId).then(response => {
        this.errorForm = response.data
        this.openErrorDialog = true
      })
    },
    /** 切换标签页 */
    handleTabClick() {
      this.resetQuery()
    },
    /** 获取操作类型标签样式 */
    getOperTypeTag(type) {
      const map = {
        SELECT: '',
        INSERT: 'success',
        UPDATE: 'warning',
        DELETE: 'danger',
        GRANT: 'info',
        EXPORT: '',
        IMPORT: 'warning',
        FORCE: 'danger',
        CLEAN: 'danger',
      }
      return map[type] || ''
    },
    /** 获取事件类型标签样式 */
    getEventTypeTag(type) {
      const map = {
        CONNECT: 'success',
        DISCONNECT: 'info',
        HEARTBEAT: '',
        MESSAGE: 'warning',
        ERROR: 'danger',
      }
      return map[type] || ''
    },
    /** 获取消息队列类型标签样式 */
    getMqTypeTag(type) {
      const map = {
        PRODUCE: 'success',
        CONSUME: 'warning',
        ERROR: 'danger',
      }
      return map[type] || ''
    },
    /** 获取错误级别标签样式 */
    getErrorLevelTag(level) {
      const map = {
        ERROR: 'danger',
        WARN: 'warning',
        INFO: 'info',
      }
      return map[level] || ''
    },
  },
}
</script>

<style scoped>
pre {
  margin: 0;
  padding: 8px;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
