<template>
  <div class="app-container">
    <el-form
      v-show="showSearch"
      ref="queryForm"
      :model="queryParams"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="任务名称" prop="jobName">
        <el-input
          v-model="queryParams.jobName"
          placeholder="请输入任务名称"
          clearable
          size="small"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="任务组" prop="jobGroup">
        <el-select v-model="queryParams.jobGroup" placeholder="任务组" clearable size="small">
          <el-option
            v-for="dict in jobGroupOptions"
            :key="dict.dictValue"
            :label="dict.dictLabel"
            :value="dict.dictValue"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="任务状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="任务状态" clearable size="small">
          <el-option label="正常" value="0" />
          <el-option label="暂停" value="1" />
        </el-select>
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
          v-hasPermi="['im:job:add']"
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          >新增</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['im:job:edit']"
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          >修改</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['im:job:remove']"
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
          v-hasPermi="['im:job:export']"
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
          v-hasPermi="['im:job:query']"
          type="info"
          plain
          icon="el-icon-s-operation"
          size="mini"
          @click="handleJobLog"
          >日志</el-button
        >
      </el-col>
      <right-toolbar
        :show-search="showSearch"
        @update:show-search="showSearch = $event"
        @query-table="getList"
      ></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="jobList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务编号" align="center" prop="jobId" />
      <el-table-column
        label="任务名称"
        align="center"
        prop="jobName"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="任务组" align="center" prop="jobGroup">
        <template #default="scope">
          {{ getDictLabel(jobGroupOptions, scope.row.jobGroup) }}
        </template>
      </el-table-column>
      <el-table-column
        label="调用目标"
        align="center"
        prop="invokeTarget"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="cron表达式"
        align="center"
        prop="cronExpression"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="状态" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            v-hasPermi="['im:job:edit']"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            >修改</el-button
          >
          <el-button
            v-hasPermi="['im:job:remove']"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            >删除</el-button
          >
          <el-dropdown size="mini" @command="command => handleCommand(command, scope.row)">
            <span class="el-dropdown-link"> <i class="el-icon-d-arrow-right"></i>更多 </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-hasPermi="['im:job:changeStatus']"
                  command="handleRun"
                  icon="el-icon-caret-right"
                  >执行一次</el-dropdown-item
                >
                <el-dropdown-item
                  v-hasPermi="['im:job:query']"
                  command="handleView"
                  icon="el-icon-view"
                  >任务详情</el-dropdown-item
                >
                <el-dropdown-item
                  v-hasPermi="['im:job:query']"
                  command="handleJobLog"
                  icon="el-icon-s-order"
                  >调度日志</el-dropdown-item
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :page="queryParams.pageNum"
      :limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <!-- 添加或修改定时任务对话框 -->
    <el-dialog v-model="open" :title="title" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="任务名称" prop="jobName">
              <el-input v-model="form.jobName" placeholder="请输入任务名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务组" prop="jobGroup">
              <el-select v-model="form.jobGroup" placeholder="请选择任务组">
                <el-option
                  v-for="dict in jobGroupOptions"
                  :key="dict.dictValue"
                  :label="dict.dictLabel"
                  :value="dict.dictValue"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="调用目标" prop="invokeTarget">
              <el-input v-model="form.invokeTarget" placeholder="请输入调用目标字符串" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="cron表达式" prop="cronExpression">
              <el-input v-model="form.cronExpression" placeholder="请输入cron表达式">
                <template #append>
                  <el-button icon="el-icon-timer" @click="handleShowCron"></el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="执行策略" prop="misfirePolicy">
              <el-radio-group v-model="form.misfirePolicy">
                <el-radio label="1">立即执行</el-radio>
                <el-radio label="2">执行一次</el-radio>
                <el-radio label="3">放弃执行</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="是否并发" prop="concurrent">
              <el-radio-group v-model="form.concurrent">
                <el-radio label="0">允许</el-radio>
                <el-radio label="1">禁止</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Cron表达式生成器对话框 -->
    <el-dialog v-model="openCron" title="Cron表达式生成器" width="700px" append-to-body>
      <crontab :expression="expression" @hide="openCron = false" @fill="crontabFill"></crontab>
    </el-dialog>
  </div>
</template>

<script>
import { listJob, getJob, runJob, changeJobStatus } from '@/api/im/job'
import Crontab from '@/components/Crontab'

export default {
  name: 'ImJob',
  components: { Crontab },
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
      // 定时任务表格数据
      jobList: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      // 是否显示Cron表达式弹出层
      openCron: false,
      // Cron表达式
      expression: '',
      // 任务组选项
      jobGroupOptions: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        jobName: undefined,
        jobGroup: undefined,
        status: undefined,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        jobName: [{ required: true, message: '任务名称不能为空', trigger: 'blur' }],
        jobGroup: [{ required: true, message: '任务组不能为空', trigger: 'change' }],
        invokeTarget: [{ required: true, message: '调用目标字符串不能为空', trigger: 'blur' }],
        cronExpression: [{ required: true, message: 'cron表达式不能为空', trigger: 'blur' }],
      },
    }
  },
  created() {
    this.getList()
    this.getDicts('sys_job_group').then(response => {
      this.jobGroupOptions = response.data
    })
  },
  methods: {
    /** 查询定时任务列表 */
    getList() {
      this.loading = true
      listJob(this.queryParams).then(response => {
        this.jobList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 任务状态修改 */
    handleStatusChange(row) {
      let text = row.status === '0' ? '启用' : '停用'
      this.$modal
        .confirm('确认要"' + text + '""' + row.jobName + '"任务吗？')
        .then(function () {
          return changeJobStatus(row.jobId, row.status)
        })
        .then(() => {
          this.$modal.msgSuccess(text + '成功')
        })
        .catch(function () {
          row.status = row.status === '0' ? '1' : '0'
        })
    },
    /** 更多操作 */
    handleCommand(command, row) {
      switch (command) {
        case 'handleRun':
          this.handleRun(row)
          break
        case 'handleView':
          this.handleView(row)
          break
        case 'handleJobLog':
          this.handleJobLog(row)
          break
        default:
          break
      }
    },
    /** 立即执行一次 */
    handleRun(row) {
      this.$modal
        .confirm('确认要立即执行一次"' + row.jobName + '"任务吗？')
        .then(function () {
          return runJob(row.jobId, row.jobGroup)
        })
        .then(() => {
          this.$modal.msgSuccess('执行成功')
        })
        .catch(() => {})
    },
    /** 任务详细信息 */
    handleView(row) {
      getJob(row.jobId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '任务详细信息'
      })
    },
    /** 调度日志按钮操作 */
    handleJobLog(row) {
      const jobId = row.jobId || this.ids
      this.$router.push({ path: '/im/job/log', query: { jobId: jobId } })
    },
    /** 显示Cron表达式生成器 */
    handleShowCron() {
      this.expression = this.form.cronExpression
      this.openCron = true
    },
    /** Cron表达式生成器回调 */
    crontabFill(val) {
      this.form.cronExpression = val
      this.openCron = false
    },
  },
}
</script>
