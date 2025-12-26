<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- 系统公告 -->
      <el-tab-pane label="系统公告" name="announcement">
        <el-form
          v-show="showSearch"
          ref="queryForm"
          :model="queryParams"
          :inline="true"
          label-width="68px"
        >
          <el-form-item label="公告标题" prop="title">
            <el-input
              v-model="queryParams.title"
              placeholder="请输入公告标题"
              clearable
              size="small"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="发布状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="发布状态" clearable size="small">
              <el-option label="草稿" value="draft" />
              <el-option label="已发布" value="published" />
              <el-option label="已过期" value="expired" />
            </el-select>
          </el-form-item>
          <el-form-item label="发布时间">
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
              v-hasPermi="['im:notice:add']"
              type="primary"
              plain
              icon="el-icon-plus"
              size="mini"
              @click="handleAdd"
              >新增公告</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button
              v-hasPermi="['im:notice:remove']"
              type="danger"
              plain
              icon="el-icon-delete"
              size="mini"
              :disabled="multiple"
              @click="handleDelete"
              >删除</el-button
            >
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>

        <el-table
          v-loading="loading"
          :data="announcementList"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column
            label="公告标题"
            align="center"
            prop="title"
            :show-overflow-tooltip="true"
          />
          <el-table-column label="发布状态" align="center" prop="status">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发布时间" align="center" prop="publishTime" width="180">
            <template #default="scope">
              <span>{{ parseTime(scope.row.publishTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="过期时间" align="center" prop="expireTime" width="180">
            <template #default="scope">
              <span>{{ parseTime(scope.row.expireTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="阅读数" align="center" prop="readCount" width="100" />
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button
                v-hasPermi="['im:notice:edit']"
                size="mini"
                type="text"
                icon="el-icon-edit"
                @click="handleUpdate(scope.row)"
                >修改</el-button
              >
              <el-button
                v-hasPermi="['im:notice:remove']"
                size="mini"
                type="text"
                icon="el-icon-delete"
                @click="handleDelete(scope.row)"
                >删除</el-button
              >
              <el-button
                v-hasPermi="['im:notice:query']"
                size="mini"
                type="text"
                icon="el-icon-view"
                @click="handlePreview(scope.row)"
                >预览</el-button
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

      <!-- 消息推送 -->
      <el-tab-pane label="消息推送" name="push">
        <el-form ref="pushForm" :model="pushForm" :rules="pushRules" label-width="100px">
          <el-card class="box-card">
            <template #header>
              <div class="clearfix">
                <span>推送内容</span>
              </div>
            </template>
            <el-form-item label="推送标题" prop="title">
              <el-input v-model="pushForm.title" placeholder="请输入推送标题" />
            </el-form-item>
            <el-form-item label="推送内容" prop="content">
              <el-input
                v-model="pushForm.content"
                type="textarea"
                placeholder="请输入推送内容"
                :rows="4"
              />
            </el-form-item>
            <el-form-item label="推送类型" prop="type">
              <el-radio-group v-model="pushForm.type">
                <el-radio label="all">全体推送</el-radio>
                <el-radio label="group">群组推送</el-radio>
                <el-radio label="user">指定用户</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="pushForm.type === 'group'" label="选择群组" prop="groupIds">
              <el-select v-model="pushForm.groupIds" multiple placeholder="请选择群组">
                <el-option
                  v-for="item in groupOptions"
                  :key="item.groupId"
                  :label="item.groupName"
                  :value="item.groupId"
                />
              </el-select>
            </el-form-item>
            <el-form-item v-if="pushForm.type === 'user'" label="选择用户" prop="userIds">
              <el-select v-model="pushForm.userIds" multiple placeholder="请选择用户">
                <el-option
                  v-for="item in userOptions"
                  :key="item.userId"
                  :label="item.userName"
                  :value="item.userId"
                />
              </el-select>
            </el-form-item>
          </el-card>

          <el-card class="box-card">
            <template #header>
              <div class="clearfix">
                <span>推送选项</span>
              </div>
            </template>
            <el-form-item label="推送方式">
              <el-checkbox-group v-model="pushForm.methods">
                <el-checkbox label="system">系统消息</el-checkbox>
                <el-checkbox label="popup">弹窗提醒</el-checkbox>
                <el-checkbox label="email">邮件通知</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="定时推送">
              <el-switch v-model="pushForm.timing" />
            </el-form-item>
            <el-form-item v-if="pushForm.timing" label="推送时间">
              <el-date-picker
                v-model="pushForm.pushTime"
                type="datetime"
                placeholder="选择推送时间"
              />
            </el-form-item>
          </el-card>

          <div class="bottom-buttons">
            <el-button type="primary" @click="handlePush">发送推送</el-button>
            <el-button @click="resetPushForm">重置</el-button>
          </div>
        </el-form>
      </el-tab-pane>

      <!-- 通知模板 -->
      <el-tab-pane label="通知模板" name="template">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              v-hasPermi="['im:notice:template:add']"
              type="primary"
              plain
              icon="el-icon-plus"
              size="mini"
              @click="handleAddTemplate"
              >新增模板</el-button
            >
          </el-col>
        </el-row>

        <el-table v-loading="loading" :data="templateList">
          <el-table-column label="模板名称" align="center" prop="templateName" />
          <el-table-column label="模板类型" align="center" prop="templateType">
            <template #default="scope">
              <el-tag>{{ scope.row.templateType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column
            label="模板内容"
            align="center"
            prop="content"
            :show-overflow-tooltip="true"
          />
          <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
            <template #default="scope">
              <span>{{ parseTime(scope.row.updateTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-edit"
                @click="handleEditTemplate(scope.row)"
                >修改</el-button
              >
              <el-button
                size="mini"
                type="text"
                icon="el-icon-delete"
                @click="handleDeleteTemplate(scope.row)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 发送记录 -->
      <el-tab-pane label="发送记录" name="record">
        <el-form
          v-show="showSearch"
          ref="queryForm"
          :model="queryParams"
          :inline="true"
          label-width="68px"
        >
          <el-form-item label="发送类型" prop="sendType">
            <el-select v-model="queryParams.sendType" placeholder="发送类型" clearable size="small">
              <el-option label="系统公告" value="announcement" />
              <el-option label="消息推送" value="push" />
              <el-option label="邮件通知" value="email" />
            </el-select>
          </el-form-item>
          <el-form-item label="发送状态" prop="sendStatus">
            <el-select
              v-model="queryParams.sendStatus"
              placeholder="发送状态"
              clearable
              size="small"
            >
              <el-option label="成功" value="success" />
              <el-option label="失败" value="failed" />
              <el-option label="待发送" value="pending" />
            </el-select>
          </el-form-item>
          <el-form-item label="发送时间">
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

        <el-table v-loading="loading" :data="recordList">
          <el-table-column label="发送类型" align="center" prop="sendType">
            <template #default="scope">
              <el-tag>{{ getSendTypeLabel(scope.row.sendType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
          <el-table-column
            label="接收对象"
            align="center"
            prop="receiver"
            :show-overflow-tooltip="true"
          />
          <el-table-column label="发送状态" align="center" prop="sendStatus">
            <template #default="scope">
              <el-tag :type="getSendStatusType(scope.row.sendStatus)">
                {{ getSendStatusLabel(scope.row.sendStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发送时间" align="center" prop="sendTime" width="180">
            <template #default="scope">
              <span>{{ parseTime(scope.row.sendTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-view"
                @click="handleViewRecord(scope.row)"
                >详情</el-button
              >
              <el-button
                v-if="scope.row.sendStatus === 'failed'"
                size="mini"
                type="text"
                icon="el-icon-refresh"
                @click="handleResend(scope.row)"
                >重发</el-button
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

    <!-- 公告编辑对话框 -->
    <el-dialog v-model:visible="open" :title="title" width="780px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <editor v-model="form.content" :min-height="192" />
        </el-form-item>
        <el-form-item label="公告类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择公告类型">
            <el-option label="通知公告" value="notice" />
            <el-option label="系统公告" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="过期时间" prop="expireTime">
          <el-date-picker v-model="form.expireTime" type="datetime" placeholder="选择过期时间" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in statusOptions" :key="dict.dictValue" :label="dict.dictValue">{{
              dict.dictLabel
            }}</el-radio>
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

    <!-- 模板编辑对话框 -->
    <el-dialog v-model:visible="templateOpen" :title="templateTitle" width="500px" append-to-body>
      <el-form ref="templateForm" :model="templateForm" :rules="templateRules" label-width="80px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="templateForm.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板类型" prop="templateType">
          <el-select v-model="templateForm.templateType" placeholder="请选择模板类型">
            <el-option label="系统消息" value="system" />
            <el-option label="邮件通知" value="email" />
            <el-option label="弹窗提醒" value="popup" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板内容" prop="content">
          <el-input
            v-model="templateForm.content"
            type="textarea"
            placeholder="请输入模板内容，支持${变量}格式的变量"
            :rows="6"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitTemplateForm">确 定</el-button>
          <el-button @click="cancelTemplate">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  listAnnouncement,
  getAnnouncement,
  delAnnouncement,
  addAnnouncement,
  updateAnnouncement,
  listTemplate,
  getTemplate,
  delTemplate,
  addTemplate,
  updateTemplate,
  listRecord,
  getRecord,
  resendNotice,
  sendPush,
} from '@/api/im/notice'
import Editor from '@/components/Editor'

export default {
  name: 'ImNotice',
  components: {
    Editor,
  },
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
      activeTab: 'announcement',
      // 公告列表数据
      announcementList: [],
      // 模板列表数据
      templateList: [],
      // 发送记录列表数据
      recordList: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      // 模板弹出层标题
      templateTitle: '',
      // 是否显示模板弹出层
      templateOpen: false,
      // 日期范围
      dateRange: [],
      // 状态数据字典
      statusOptions: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        status: undefined,
        sendType: undefined,
        sendStatus: undefined,
      },
      // 表单参数
      form: {
        title: undefined,
        content: undefined,
        type: 'notice',
        expireTime: undefined,
        status: '0',
      },
      // 表单校验
      rules: {
        title: [{ required: true, message: '公告标题不能为空', trigger: 'blur' }],
        content: [{ required: true, message: '公告内容不能为空', trigger: 'blur' }],
      },
      // 推送表单参数
      pushForm: {
        title: undefined,
        content: undefined,
        type: 'all',
        groupIds: [],
        userIds: [],
        methods: ['system'],
        timing: false,
        pushTime: undefined,
      },
      // 推送表单校验
      pushRules: {
        title: [{ required: true, message: '推送标题不能为空', trigger: 'blur' }],
        content: [{ required: true, message: '推送内容不能为空', trigger: 'blur' }],
      },
      // 模板表单参数
      templateForm: {
        templateName: undefined,
        templateType: undefined,
        content: undefined,
      },
      // 模板表单校验
      templateRules: {
        templateName: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }],
        templateType: [{ required: true, message: '模板类型不能为空', trigger: 'change' }],
        content: [{ required: true, message: '模板内容不能为空', trigger: 'blur' }],
      },
      // 群组选项
      groupOptions: [],
      // 用户选项
      userOptions: [],
    }
  },
  created() {
    this.getList()
    this.getDicts('sys_notice_status').then(response => {
      this.statusOptions = response.data
    })
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true
      switch (this.activeTab) {
        case 'announcement':
          listAnnouncement(this.queryParams).then(response => {
            this.announcementList = response.rows
            this.total = response.total
            this.loading = false
          })
          break
        case 'template':
          listTemplate(this.queryParams).then(response => {
            this.templateList = response.rows
            this.total = response.total
            this.loading = false
          })
          break
        case 'record':
          listRecord(this.queryParams).then(response => {
            this.recordList = response.rows
            this.total = response.total
            this.loading = false
          })
          break
      }
    },
    /** 取消按钮 */
    cancel() {
      this.open = false
      this.reset()
    },
    /** 表单重置 */
    reset() {
      this.form = {
        title: undefined,
        content: undefined,
        type: 'notice',
        expireTime: undefined,
        status: '0',
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
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加公告'
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getAnnouncement(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改公告'
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.id != undefined) {
            updateAnnouncement(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addAnnouncement(this.form).then(() => {
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
      const ids = row.id || this.ids
      this.$modal
        .confirm('是否确认删除公告编号为"' + ids + '"的数据项？')
        .then(function () {
          return delAnnouncement(ids)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    /** 预览按钮操作 */
    handlePreview(row) {
      this.$router.push({ path: '/im/notice/preview', query: { id: row.id } })
    },
    /** 发送推送操作 */
    handlePush() {
      this.$refs['pushForm'].validate(valid => {
        if (valid) {
          sendPush(this.pushForm).then(() => {
            this.$modal.msgSuccess('推送发送成功')
            this.resetPushForm()
          })
        }
      })
    },
    /** 重置推送表单 */
    resetPushForm() {
      this.pushForm = {
        title: undefined,
        content: undefined,
        type: 'all',
        groupIds: [],
        userIds: [],
        methods: ['system'],
        timing: false,
        pushTime: undefined,
      }
      this.resetForm('pushForm')
    },
    /** 新增模板按钮操作 */
    handleAddTemplate() {
      this.templateForm = {
        templateName: undefined,
        templateType: undefined,
        content: undefined,
      }
      this.templateOpen = true
      this.templateTitle = '新增模板'
    },
    /** 修改模板按钮操作 */
    handleEditTemplate(row) {
      getTemplate(row.id).then(response => {
        this.templateForm = response.data
        this.templateOpen = true
        this.templateTitle = '修改模板'
      })
    },
    /** 删除模板按钮操作 */
    handleDeleteTemplate(row) {
      this.$modal
        .confirm('是否确认删除该模板？')
        .then(function () {
          return delTemplate(row.id)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    /** 提交模板表单 */
    submitTemplateForm() {
      this.$refs['templateForm'].validate(valid => {
        if (valid) {
          if (this.templateForm.id != undefined) {
            updateTemplate(this.templateForm).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.templateOpen = false
              this.getList()
            })
          } else {
            addTemplate(this.templateForm).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.templateOpen = false
              this.getList()
            })
          }
        }
      })
    },
    /** 取消模板 */
    cancelTemplate() {
      this.templateOpen = false
      this.templateForm = {
        templateName: undefined,
        templateType: undefined,
        content: undefined,
      }
      this.resetForm('templateForm')
    },
    /** 查看记录详情 */
    handleViewRecord(row) {
      getRecord(row.id).then(() => {
        // 显示记录详情
      })
    },
    /** 重发消息 */
    handleResend(row) {
      this.$modal
        .confirm('是否确认重新发送该消息？')
        .then(function () {
          return resendNotice(row.id)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('发送成功')
        })
        .catch(() => {})
    },
    /** 切换标签页 */
    handleTabClick() {
      this.resetQuery()
    },
    /** 获取状态标签类型 */
    getStatusType(status) {
      if (status === 'published') return 'success'
      if (status === 'draft') return 'info'
      if (status === 'expired') return 'warning'
      return 'info'
    },
    /** 获取状态显示文本 */
    getStatusLabel(status) {
      if (status === 'published') return '已发布'
      if (status === 'draft') return '草稿'
      if (status === 'expired') return '已过期'
      return status
    },
    /** 获取发送类型显示文本 */
    getSendTypeLabel(type) {
      if (type === 'announcement') return '系统公告'
      if (type === 'push') return '消息推送'
      if (type === 'email') return '邮件通知'
      return type
    },
    /** 获取发送状态标签类型 */
    getSendStatusType(status) {
      if (status === 'success') return 'success'
      if (status === 'failed') return 'danger'
      if (status === 'pending') return 'warning'
      return 'info'
    },
    /** 获取发送状态显示文本 */
    getSendStatusLabel(status) {
      if (status === 'success') return '成功'
      if (status === 'failed') return '失败'
      if (status === 'pending') return '待发送'
      return status
    },
  },
}
</script>

<style scoped>
.box-card {
  margin-bottom: 20px;
}
</style>
