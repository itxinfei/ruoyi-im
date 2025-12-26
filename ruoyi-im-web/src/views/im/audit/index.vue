<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- 登录审计 -->
      <el-tab-pane label="登录审计" name="login">
        <el-form
          v-show="showSearch"
          ref="queryForm"
          :model="queryParams"
          :inline="true"
          label-width="68px"
        >
          <el-form-item label="用户名称" prop="userName">
            <el-input
              v-model="queryParams.userName"
              placeholder="请输入用户名称"
              clearable
              size="small"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="登录IP" prop="ipaddr">
            <el-input
              v-model="queryParams.ipaddr"
              placeholder="请输入登录IP"
              clearable
              size="small"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="登录状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="登录状态" clearable size="small">
              <el-option label="成功" value="0" />
              <el-option label="失败" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="登录时间">
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

        <el-table v-loading="loading" :data="loginList">
          <el-table-column label="访问编号" align="center" prop="infoId" />
          <el-table-column label="用户名称" align="center" prop="userName" />
          <el-table-column
            label="登录地址"
            align="center"
            prop="ipaddr"
            width="130"
            :show-overflow-tooltip="true"
          />
          <el-table-column
            label="登录地点"
            align="center"
            prop="loginLocation"
            :show-overflow-tooltip="true"
          />
          <el-table-column label="浏览器" align="center" prop="browser" />
          <el-table-column label="操作系统" align="center" prop="os" />
          <el-table-column label="登录状态" align="center" prop="status">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
                {{ scope.row.status === '0' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作信息" align="center" prop="msg" />
          <el-table-column label="登录时间" align="center" prop="loginTime" width="180">
            <template #default="scope">
              <span>{{ parseTime(scope.row.loginTime) }}</span>
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

      <!-- 操作审计 -->
      <el-tab-pane label="操作审计" name="operation">
        <el-form
          v-show="showSearch"
          ref="queryForm"
          :model="queryParams"
          :inline="true"
          label-width="68px"
        >
          <el-form-item label="操作人员" prop="operName">
            <el-input
              v-model="queryParams.operName"
              placeholder="请输入操作人员"
              clearable
              size="small"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="操作类型" prop="operationType">
            <el-select
              v-model="queryParams.operationType"
              placeholder="操作类型"
              clearable
              size="small"
            >
              <el-option label="查询" value="SELECT" />
              <el-option label="新增" value="INSERT" />
              <el-option label="修改" value="UPDATE" />
              <el-option label="删除" value="DELETE" />
              <el-option label="授权" value="GRANT" />
              <el-option label="导出" value="EXPORT" />
              <el-option label="导入" value="IMPORT" />
              <el-option label="强退" value="FORCE" />
              <el-option label="生成代码" value="GENCODE" />
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
              v-hasPermi="['monitor:operlog:remove']"
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
              v-hasPermi="['monitor:operlog:remove']"
              type="danger"
              plain
              icon="el-icon-delete"
              size="mini"
              @click="handleClean"
              >清空</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button
              v-hasPermi="['monitor:operlog:export']"
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

        <el-table
          v-loading="loading"
          :data="operationList"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="日志编号" align="center" prop="operId" />
          <el-table-column label="操作模块" align="center" prop="title" />
          <el-table-column label="操作类型" align="center" prop="operationType">
            <template #default="scope">
              <el-tag :type="getOperationTypeTag(scope.row.operationType)">{{
                scope.row.operationType
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作人员" align="center" prop="operName" />
          <el-table-column
            label="操作地址"
            align="center"
            prop="operIp"
            width="130"
            :show-overflow-tooltip="true"
          />
          <el-table-column
            label="操作地点"
            align="center"
            prop="operLocation"
            :show-overflow-tooltip="true"
          />
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

      <!-- 安全审计 -->
      <el-tab-pane label="安全审计" name="security">
        <el-form
          v-show="showSearch"
          ref="queryForm"
          :model="queryParams"
          :inline="true"
          label-width="68px"
        >
          <el-form-item label="事件类型" prop="eventType">
            <el-select
              v-model="queryParams.eventType"
              placeholder="事件类型"
              clearable
              size="small"
            >
              <el-option label="认证失败" value="AUTH_FAIL" />
              <el-option label="越权访问" value="UNAUTHORIZED" />
              <el-option label="敏感操作" value="SENSITIVE" />
              <el-option label="异常登录" value="ABNORMAL_LOGIN" />
              <el-option label="数据泄露" value="DATA_LEAK" />
            </el-select>
          </el-form-item>
          <el-form-item label="风险等级" prop="riskLevel">
            <el-select
              v-model="queryParams.riskLevel"
              placeholder="风险等级"
              clearable
              size="small"
            >
              <el-option label="高" value="high" />
              <el-option label="中" value="medium" />
              <el-option label="低" value="low" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理状态" prop="handleStatus">
            <el-select
              v-model="queryParams.handleStatus"
              placeholder="处理状态"
              clearable
              size="small"
            >
              <el-option label="未处理" value="0" />
              <el-option label="已处理" value="1" />
              <el-option label="忽略" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="事件时间">
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

        <el-table v-loading="loading" :data="securityList">
          <el-table-column label="事件编号" align="center" prop="eventId" />
          <el-table-column label="事件类型" align="center" prop="eventType">
            <template #default="scope">
              <el-tag :type="getEventTypeTag(scope.row.eventType)">{{
                getEventTypeLabel(scope.row.eventType)
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="风险等级" align="center" prop="riskLevel">
            <template #default="scope">
              <el-tag :type="getRiskLevelTag(scope.row.riskLevel)">{{
                scope.row.riskLevel
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column
            label="事件描述"
            align="center"
            prop="description"
            :show-overflow-tooltip="true"
          />
          <el-table-column label="事件源" align="center" prop="source" />
          <el-table-column label="IP地址" align="center" prop="ipaddr" />
          <el-table-column label="处理状态" align="center" prop="handleStatus">
            <template #default="scope">
              <el-tag :type="getHandleStatusTag(scope.row.handleStatus)">
                {{
                  scope.row.handleStatus === '0'
                    ? '未处理'
                    : scope.row.handleStatus === '1'
                      ? '已处理'
                      : '忽略'
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发生时间" align="center" prop="eventTime" width="180">
            <template #default="scope">
              <span>{{ parseTime(scope.row.eventTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-view"
                @click="handleSecurityDetail(scope.row)"
                >详细</el-button
              >
              <el-button
                v-if="scope.row.handleStatus === '0'"
                size="mini"
                type="text"
                icon="el-icon-edit"
                @click="handleSecurityEvent(scope.row)"
                >处理</el-button
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

    <!-- 安全事件处理 -->
    <el-dialog
      v-model:visible="openSecurityDialog"
      :title="securityTitle"
      width="500px"
      append-to-body
    >
      <el-form ref="securityForm" :model="securityForm" :rules="securityRules" label-width="80px">
        <el-form-item label="处理方式" prop="handleType">
          <el-radio-group v-model="securityForm.handleType">
            <el-radio label="1">处理</el-radio>
            <el-radio label="2">忽略</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明" prop="handleNote">
          <el-input
            v-model="securityForm.handleNote"
            type="textarea"
            placeholder="请输入处理说明"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitSecurityHandle">确 定</el-button>
          <el-button @click="openSecurityDialog = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  listLoginInfo,
  listOperLog,
  delOperLog,
  cleanOperLog,
  listSecurityEvent,
  handleSecurityEvent,
} from '@/api/im/audit'

export default {
  name: 'ImAudit',
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
      // 表格数据
      loginList: [],
      operationList: [],
      securityList: [],
      // 是否显示弹出层
      openOperationDialog: false,
      openSecurityDialog: false,
      // 日期范围
      dateRange: [],
      // 当前选中的tab
      activeTab: 'login',
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: undefined,
        ipaddr: undefined,
        status: undefined,
        operName: undefined,
        operationType: undefined,
        eventType: undefined,
        riskLevel: undefined,
        handleStatus: undefined,
      },
      // 表单参数
      form: {},
      // 安全事件表单参数
      securityForm: {
        eventId: undefined,
        handleType: '1',
        handleNote: undefined,
      },
      // 安全事件表单校验
      securityRules: {
        handleType: [{ required: true, message: '请选择处理方式', trigger: 'change' }],
        handleNote: [{ required: true, message: '请输入处理说明', trigger: 'blur' }],
      },
      // 弹出层标题
      securityTitle: '',
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true
      if (this.activeTab === 'login') {
        listLoginInfo(this.queryParams).then(response => {
          this.loginList = response.rows
          this.total = response.total
          this.loading = false
        })
      } else if (this.activeTab === 'operation') {
        listOperLog(this.queryParams).then(response => {
          this.operationList = response.rows
          this.total = response.total
          this.loading = false
        })
      } else if (this.activeTab === 'security') {
        listSecurityEvent(this.queryParams).then(response => {
          this.securityList = response.rows
          this.total = response.total
          this.loading = false
        })
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
        .confirm('是否确认清空所有操作日志数据项？')
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
        'monitor/operlog/export',
        {
          ...this.queryParams,
        },
        `operlog_${new Date().getTime()}.xlsx`
      )
    },
    /** 查看详细按钮操作 */
    handleView(row) {
      this.form = row
      this.openOperationDialog = true
    },
    /** 处理安全事件按钮操作 */
    handleSecurityEvent(row) {
      this.securityForm = {
        eventId: row.eventId,
        handleType: '1',
        handleNote: undefined,
      }
      this.securityTitle = '安全事件处理'
      this.openSecurityDialog = true
    },
    /** 提交安全事件处理 */
    submitSecurityHandle() {
      this.$refs['securityForm'].validate(valid => {
        if (valid) {
          handleSecurityEvent(this.securityForm).then(() => {
            this.$modal.msgSuccess('处理成功')
            this.openSecurityDialog = false
            this.getList()
          })
        }
      })
    },
    /** 切换标签页 */
    handleTabClick() {
      this.resetQuery()
    },
    /** 获取操作类型标签样式 */
    getOperationTypeTag(type) {
      const map = {
        SELECT: '',
        INSERT: 'success',
        UPDATE: 'warning',
        DELETE: 'danger',
        GRANT: 'info',
        EXPORT: '',
        IMPORT: 'warning',
        FORCE: 'danger',
        GENCODE: 'success',
        CLEAN: 'danger',
      }
      return map[type] || ''
    },
    /** 获取事件类型标签样式 */
    getEventTypeTag(type) {
      const map = {
        AUTH_FAIL: 'warning',
        UNAUTHORIZED: 'danger',
        SENSITIVE: 'warning',
        ABNORMAL_LOGIN: 'danger',
        DATA_LEAK: 'danger',
      }
      return map[type] || ''
    },
    /** 获取事件类型显示文本 */
    getEventTypeLabel(type) {
      const map = {
        AUTH_FAIL: '认证失败',
        UNAUTHORIZED: '越权访问',
        SENSITIVE: '敏感操作',
        ABNORMAL_LOGIN: '异常登录',
        DATA_LEAK: '数据泄露',
      }
      return map[type] || type
    },
    /** 获取风险等级标签样式 */
    getRiskLevelTag(level) {
      const map = {
        high: 'danger',
        medium: 'warning',
        low: 'info',
      }
      return map[level] || ''
    },
    /** 获取处理状态标签样式 */
    getHandleStatusTag(status) {
      const map = {
        0: 'danger',
        1: 'success',
        2: 'info',
      }
      return map[status] || ''
    },
  },
}
</script>

<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}
</style>
