<template>
  <div class="app-container">
    <el-form
      v-show="showSearch"
      ref="queryForm"
      :model="queryParams"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="备份类型" prop="backupType">
        <el-select v-model="queryParams.backupType" placeholder="备份类型" clearable size="small">
          <el-option label="全量备份" value="full" />
          <el-option label="增量备份" value="incremental" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="备份状态" clearable size="small">
          <el-option label="成功" value="success" />
          <el-option label="失败" value="failed" />
          <el-option label="进行中" value="running" />
        </el-select>
      </el-form-item>
      <el-form-item label="备份时间">
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
          v-hasPermi="['im:backup:create']"
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleBackup"
          >创建备份</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['im:backup:restore']"
          type="success"
          plain
          icon="el-icon-upload"
          size="mini"
          @click="handleRestore"
          >恢复数据</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['im:backup:remove']"
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          >删除备份</el-button
        >
      </el-col>
      <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="backupList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="备份ID" align="center" prop="backupId" />
      <el-table-column label="备份类型" align="center" prop="backupType">
        <template #default="scope">
          <el-tag :type="scope.row.backupType === 'full' ? 'primary' : 'success'">
            {{ scope.row.backupType === 'full' ? '全量备份' : '增量备份' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" align="center" prop="fileSize">
        <template #default="scope">
          {{ formatFileSize(scope.row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column
        label="备份路径"
        align="center"
        prop="backupPath"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开始时间" align="center" prop="startTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="endTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            v-hasPermi="['im:backup:download']"
            size="mini"
            type="text"
            icon="el-icon-download"
            @click="handleDownload(scope.row)"
            >下载</el-button
          >
          <el-button
            v-hasPermi="['im:backup:query']"
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleDetail(scope.row)"
            >详情</el-button
          >
          <el-button
            v-hasPermi="['im:backup:remove']"
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

    <!-- 创建备份对话框 -->
    <el-dialog v-model:visible="open" :title="title" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="备份类型" prop="backupType">
          <el-radio-group v-model="form.backupType">
            <el-radio label="full">全量备份</el-radio>
            <el-radio label="incremental">增量备份</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备份描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入备份描述"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 恢复数据对话框 -->
    <el-dialog v-model:visible="restoreOpen" title="恢复数据" width="500px" append-to-body>
      <el-form ref="restoreForm" :model="restoreForm" :rules="restoreRules" label-width="80px">
        <el-form-item label="备份点" prop="backupId">
          <el-select v-model="restoreForm.backupId" placeholder="请选择备份点">
            <el-option
              v-for="item in backupList"
              :key="item.backupId"
              :label="
                item.backupType === 'full'
                  ? '全量备份-' + parseTime(item.startTime)
                  : '增量备份-' + parseTime(item.startTime)
              "
              :value="item.backupId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="恢复模式" prop="restoreMode">
          <el-radio-group v-model="restoreForm.restoreMode">
            <el-radio label="override">覆盖恢复</el-radio>
            <el-radio label="merge">合并恢复</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="确认" prop="confirm">
          <el-checkbox v-model="restoreForm.confirm"
            >我已了解恢复操作的风险，并已做好相应准备</el-checkbox
          >
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :disabled="!restoreForm.confirm" @click="submitRestore"
            >确 定</el-button
          >
          <el-button @click="restoreOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { listBackup, delBackup, createBackup, restoreBackup, downloadBackup } from '@/api/im/backup'

export default {
  name: 'ImBackup',
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
      // 备份表格数据
      backupList: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      // 是否显示恢复弹出层
      restoreOpen: false,
      // 日期范围
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        backupType: undefined,
        status: undefined,
      },
      // 表单参数
      form: {
        backupType: 'full',
        description: undefined,
      },
      // 恢复表单参数
      restoreForm: {
        backupId: undefined,
        restoreMode: 'override',
        confirm: false,
      },
      // 表单校验
      rules: {
        backupType: [{ required: true, message: '请选择备份类型', trigger: 'change' }],
      },
      // 恢复表单校验
      restoreRules: {
        backupId: [{ required: true, message: '请选择备份点', trigger: 'change' }],
        restoreMode: [{ required: true, message: '请选择恢复模式', trigger: 'change' }],
      },
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询备份列表 */
    getList() {
      this.loading = true
      listBackup(this.queryParams).then(response => {
        this.backupList = response.rows
        this.total = response.total
        this.loading = false
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
    /** 获取状态标签类型 */
    getStatusType(status) {
      const map = {
        success: 'success',
        failed: 'danger',
        running: 'warning',
      }
      return map[status] || 'info'
    },
    /** 获取状态显示文本 */
    getStatusLabel(status) {
      const map = {
        success: '成功',
        failed: '失败',
        running: '进行中',
      }
      return map[status] || status
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
      this.ids = selection.map(item => item.backupId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 创建备份按钮操作 */
    handleBackup() {
      this.reset()
      this.open = true
      this.title = '创建备份'
    },
    /** 恢复数据按钮操作 */
    handleRestore() {
      this.restoreForm = {
        backupId: undefined,
        restoreMode: 'override',
        confirm: false,
      }
      this.restoreOpen = true
    },
    /** 表单重置 */
    reset() {
      this.form = {
        backupType: 'full',
        description: undefined,
      }
      this.resetForm('form')
    },
    /** 取消按钮 */
    cancel() {
      this.open = false
      this.reset()
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          createBackup(this.form).then(() => {
            this.$modal.msgSuccess('创建成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    /** 提交恢复 */
    submitRestore() {
      this.$refs['restoreForm'].validate(valid => {
        if (valid) {
          this.$modal
            .confirm('确认要执行恢复操作吗？这可能会影响系统数据。')
            .then(() => {
              return restoreBackup(this.restoreForm)
            })
            .then(() => {
              this.$modal.msgSuccess('恢复操作已提交')
              this.restoreOpen = false
              this.getList()
            })
            .catch(() => {})
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const backupIds = row.backupId || this.ids
      this.$modal
        .confirm('是否确认删除备份编号为"' + backupIds + '"的数据项？')
        .then(function () {
          return delBackup(backupIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    /** 下载按钮操作 */
    handleDownload(row) {
      downloadBackup(row.backupId)
    },
    /** 详情按钮操作 */
    handleDetail(row) {
      this.$router.push({ path: '/im/backup/detail', query: { backupId: row.backupId } })
    },
  },
}
</script>

<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}
</style>
