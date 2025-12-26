<template>
  <div class="app-container">
    <!-- 搜索工具栏 -->
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" :inline="true">
      <el-form-item label="会话类型" prop="sessionType">
        <el-select v-model="queryParams.sessionType" placeholder="请选择会话类型" clearable>
          <el-option label="单聊" :value="1" />
          <el-option label="群聊" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="会话名称" prop="sessionName">
        <el-input
          v-model="queryParams.sessionName"
          placeholder="请输入会话名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="会话状态" clearable>
          <el-option label="正常" :value="0" />
          <el-option label="禁用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['im:session:create']"
          type="primary"
          plain
          icon="el-icon-plus"
          @click="handleAdd"
          >新建会话</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['im:session:delete']"
          type="danger"
          plain
          icon="el-icon-delete"
          :disabled="multiple"
          @click="handleDelete"
          >批量删除</el-button
        >
      </el-col>
      <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
    </el-row>

    <!-- 会话列表 -->
    <el-table v-loading="loading" :data="sessionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="会话ID" align="center" prop="sessionId" />
      <el-table-column label="会话类型" align="center" prop="sessionType">
        <template #default="scope">
          <dict-tag :options="dict.type.im_session_type" :value="scope.row.sessionType" />
        </template>
      </el-table-column>
      <el-table-column
        label="会话名称"
        align="center"
        prop="sessionName"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="会话头像" align="center" width="100">
        <template #default="scope">
          <el-avatar :size="40" :src="scope.row.sessionAvatar">
            {{ scope.row.sessionName ? scope.row.sessionName.charAt(0) : '?' }}
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column label="最后活动时间" align="center" prop="lastActiveTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.lastActiveTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center">
        <template #default="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            v-hasPermi="['im:session:query']"
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            >查看</el-button
          >
          <el-button
            v-hasPermi="['im:session:update']"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            >修改</el-button
          >
          <el-button
            v-hasPermi="['im:session:delete']"
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

    <!-- 添加或修改会话对话框 -->
    <el-dialog v-model="open" :title="title" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="会话类型" prop="sessionType">
          <el-radio-group v-model="form.sessionType">
            <el-radio :label="1">单聊</el-radio>
            <el-radio :label="2">群聊</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="会话名称" prop="sessionName">
          <el-input v-model="form.sessionName" placeholder="请输入会话名称" />
        </el-form-item>
        <el-form-item label="会话头像" prop="sessionAvatar">
          <el-input v-model="form.sessionAvatar" placeholder="请输入会话头像URL" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">禁用</el-radio>
          </el-radio-group>
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
import { listSession, getSession, addSession, updateSession, delSession } from '@/api/im/session'

export default {
  name: 'Session',
  dicts: ['sys_normal_disable', 'im_session_type'],
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
      // 会话表格数据
      sessionList: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        sessionType: null,
        sessionName: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        sessionType: [{ required: true, message: '会话类型不能为空', trigger: 'change' }],
        sessionName: [{ required: true, message: '会话名称不能为空', trigger: 'blur' }],
      },
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询会话列表 */
    getList() {
      this.loading = true
      listSession(this.queryParams).then(response => {
        this.sessionList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        sessionId: null,
        sessionType: 1,
        sessionName: null,
        sessionAvatar: null,
        status: 0,
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
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.sessionId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加会话'
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const sessionId = row.sessionId || this.ids
      getSession(sessionId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改会话'
      })
    },
    /** 查看按钮操作 */
    handleView(row) {
      const sessionId = row.sessionId
      this.$router.push({ path: '/im/session/detail', query: { sessionId: sessionId } })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.sessionId != null) {
            updateSession(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addSession(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const sessionIds = row.sessionId || this.ids
      this.$modal
        .confirm('是否确认删除会话编号为"' + sessionIds + '"的数据项？')
        .then(function () {
          return delSession(sessionIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
  },
}
</script>
