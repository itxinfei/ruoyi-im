<template>
  <div class="app-container">
    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          >新增规则</el-button
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

    <!-- 规则列表 -->
    <el-table v-loading="loading" :data="ruleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="规则名称" align="left" prop="ruleName" />
      <el-table-column label="规则类型" align="center" width="120">
        <template #default="scope">
          <el-tag :type="getRuleTypeTag(scope.row.type)">
            {{ getRuleTypeLabel(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="归档条件" align="left" :show-overflow-tooltip="true">
        <template #default="scope">
          {{ formatConditions(scope.row.conditions) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="1"
            inactive-value="0"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            >修改</el-button
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

    <!-- 添加或修改规则对话框 -->
    <el-dialog v-model="open" :title="title" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择规则类型">
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="归档条件" prop="conditions">
          <div class="condition-list">
            <div v-for="(condition, index) in form.conditions" :key="index" class="condition-item">
              <el-select v-model="condition.field" placeholder="选择字段" style="width: 120px">
                <el-option label="消息时间" value="messageTime" />
                <el-option label="消息数量" value="messageCount" />
                <el-option label="会话类型" value="sessionType" />
              </el-select>
              <el-select v-model="condition.operator" placeholder="选择操作符" style="width: 120px">
                <el-option label="大于" value="gt" />
                <el-option label="小于" value="lt" />
                <el-option label="等于" value="eq" />
              </el-select>
              <el-input v-model="condition.value" placeholder="输入值" style="width: 200px" />
              <el-button type="text" icon="el-icon-delete" @click="removeCondition(index)" />
            </div>
            <el-button type="text" icon="el-icon-plus" @click="addCondition">添加条件</el-button>
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="1">启用</el-radio>
            <el-radio label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ArchiveRule',
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 规则表格数据
      ruleList: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      // 表单参数
      form: {
        ruleName: undefined,
        type: undefined,
        conditions: [],
        status: '1',
        remark: undefined,
      },
      // 表单校验
      rules: {
        ruleName: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
        type: [{ required: true, message: '规则类型不能为空', trigger: 'change' }],
      },
      // 规则类型选项
      typeOptions: [
        { label: '时间归档', value: 'time' },
        { label: '数量归档', value: 'count' },
        { label: '类型归档', value: 'type' },
      ],
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询规则列表 */
    getList() {
      this.loading = true
      // 模拟异步请求
      setTimeout(() => {
        this.ruleList = [
          {
            ruleId: '1',
            ruleName: '历史消息归档',
            type: 'time',
            conditions: [{ field: 'messageTime', operator: 'gt', value: '30' }],
            status: '1',
            createTime: '2024-01-01 10:00:00',
          },
          {
            ruleId: '2',
            ruleName: '大量消息归档',
            type: 'count',
            conditions: [{ field: 'messageCount', operator: 'gt', value: '1000' }],
            status: '1',
            createTime: '2024-01-02 10:00:00',
          },
        ]
        this.total = this.ruleList.length
        this.loading = false
      }, 500)
    },
    /** 取消按钮 */
    cancel() {
      this.open = false
      this.reset()
    },
    /** 表单重置 */
    reset() {
      this.form = {
        ruleName: undefined,
        type: undefined,
        conditions: [],
        status: '1',
        remark: undefined,
      }
      this.resetForm('form')
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加归档规则'
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      this.form = { ...row }
      this.open = true
      this.title = '修改归档规则'
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.ruleId) {
            this.$message.success('修改成功')
          } else {
            this.$message.success('新增成功')
          }
          this.open = false
          this.getList()
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete() {
      this.$confirm('是否确认删除该规则？', '警告', {
        type: 'warning',
      }).then(() => {
        this.$message.success('删除成功')
        this.getList()
      })
    },
    /** 批量删除操作 */
    handleBatchDelete() {
      this.$confirm('是否确认删除选中的规则？', '警告', {
        type: 'warning',
      }).then(() => {
        this.$message.success('批量删除成功')
        this.getList()
      })
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.ruleId)
      this.multiple = !selection.length
    },
    /** 状态修改 */
    handleStatusChange(row) {
      let text = row.status === '1' ? '启用' : '停用'
      this.$confirm('确认要"' + text + '""' + row.ruleName + '"规则吗？', '警告', {
        type: 'warning',
      })
        .then(() => {
          this.$message.success(text + '成功')
        })
        .catch(() => {
          row.status = row.status === '1' ? '0' : '1'
        })
    },
    /** 添加条件 */
    addCondition() {
      this.form.conditions.push({
        field: undefined,
        operator: undefined,
        value: undefined,
      })
    },
    /** 移除条件 */
    removeCondition(index) {
      this.form.conditions.splice(index, 1)
    },
    /** 格式化条件显示 */
    formatConditions(conditions) {
      if (!conditions || !conditions.length) {
        return '无'
      }
      return conditions
        .map(condition => {
          const fieldMap = {
            messageTime: '消息时间',
            messageCount: '消息数量',
            sessionType: '会话类型',
          }
          const operatorMap = {
            gt: '大于',
            lt: '小于',
            eq: '等于',
          }
          return `${fieldMap[condition.field]} ${operatorMap[condition.operator]} ${condition.value}`
        })
        .join('且')
    },
    /** 获取规则类型标签样式 */
    getRuleTypeTag(type) {
      const tags = {
        time: '',
        count: 'success',
        type: 'warning',
      }
      return tags[type] || 'info'
    },
    /** 获取规则类型显示文本 */
    getRuleTypeLabel(type) {
      const labels = {
        time: '时间归档',
        count: '数量归档',
        type: '类型归档',
      }
      return labels[type] || type
    },
  },
}
</script>

<style lang="scss" scoped>
.app-container {
  .condition-list {
    .condition-item {
      display: flex;
      align-items: center;
      margin-bottom: 10px;

      .el-select {
        margin-right: 10px;
      }

      .el-input {
        margin-right: 10px;
      }
    }
  }
}
</style>
