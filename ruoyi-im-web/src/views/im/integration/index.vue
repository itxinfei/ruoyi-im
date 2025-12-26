<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- 应用管理 -->
      <el-tab-pane label="应用管理" name="app">
        <el-form
          v-show="showSearch"
          ref="queryForm"
          :model="queryParams"
          :inline="true"
          label-width="68px"
        >
          <el-form-item label="应用名称" prop="appName">
            <el-input
              v-model="queryParams.appName"
              placeholder="请输入应用名称"
              clearable
              size="small"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="应用状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="应用状态" clearable size="small">
              <el-option label="启用" value="0" />
              <el-option label="禁用" value="1" />
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
              v-hasPermi="['im:integration:add']"
              type="primary"
              plain
              icon="el-icon-plus"
              size="mini"
              @click="handleAdd"
              >新增应用</el-button
            >
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="appList">
          <el-table-column label="应用ID" align="center" prop="appId" width="100" />
          <el-table-column label="应用名称" align="center" prop="appName" />
          <el-table-column label="应用密钥" align="center" prop="appSecret" width="300">
            <template #default="scope">
              <el-input :value="scope.row.appSecret" size="mini" readonly>
                <template #append>
                  <el-button
                    icon="el-icon-document-copy"
                    @click="copyText(scope.row.appSecret)"
                  ></el-button>
                </template>
              </el-input>
            </template>
          </el-table-column>
          <el-table-column
            label="回调地址"
            align="center"
            prop="callbackUrl"
            :show-overflow-tooltip="true"
          />
          <el-table-column label="状态" align="center" prop="status" width="80">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                active-value="0"
                inactive-value="1"
                @change="handleStatusChange(scope.row)"
              ></el-switch>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button
                v-hasPermi="['im:integration:edit']"
                size="mini"
                type="text"
                icon="el-icon-edit"
                @click="handleUpdate(scope.row)"
                >修改</el-button
              >
              <el-button
                v-hasPermi="['im:integration:remove']"
                size="mini"
                type="text"
                icon="el-icon-delete"
                @click="handleDelete(scope.row)"
                >删除</el-button
              >
              <el-button
                v-hasPermi="['im:integration:edit']"
                size="mini"
                type="text"
                icon="el-icon-refresh"
                @click="handleResetSecret(scope.row)"
                >重置密钥</el-button
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

      <!-- Webhook配置 -->
      <el-tab-pane label="Webhook配置" name="webhook">
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
              <el-option label="消息事件" value="message" />
              <el-option label="用户事件" value="user" />
              <el-option label="群组事件" value="group" />
              <el-option label="系统事件" value="system" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="状态" clearable size="small">
              <el-option label="启用" value="0" />
              <el-option label="禁用" value="1" />
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
              v-hasPermi="['im:integration:add']"
              type="primary"
              plain
              icon="el-icon-plus"
              size="mini"
              @click="handleAddWebhook"
              >新增Webhook</el-button
            >
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="webhookList">
          <el-table-column label="ID" align="center" prop="webhookId" width="100" />
          <el-table-column label="事件类型" align="center" prop="eventType">
            <template #default="scope">
              <el-tag>{{ scope.row.eventType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="URL" align="center" prop="url" :show-overflow-tooltip="true" />
          <el-table-column label="密钥" align="center" prop="secret" width="300">
            <template #default="scope">
              <el-input :value="scope.row.secret" size="mini" readonly>
                <template #append>
                  <el-button
                    icon="el-icon-document-copy"
                    @click="copyText(scope.row.secret)"
                  ></el-button>
                </template>
              </el-input>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" prop="status" width="80">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                active-value="0"
                inactive-value="1"
                @change="handleWebhookStatusChange(scope.row)"
              ></el-switch>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button
                v-hasPermi="['im:integration:edit']"
                size="mini"
                type="text"
                icon="el-icon-edit"
                @click="handleUpdateWebhook(scope.row)"
                >修改</el-button
              >
              <el-button
                v-hasPermi="['im:integration:remove']"
                size="mini"
                type="text"
                icon="el-icon-delete"
                @click="handleDeleteWebhook(scope.row)"
                >删除</el-button
              >
              <el-button
                v-hasPermi="['im:integration:edit']"
                size="mini"
                type="text"
                icon="el-icon-video-play"
                @click="handleTestWebhook(scope.row)"
                >测试</el-button
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

      <!-- 服务集成 -->
      <el-tab-pane label="服务集成" name="service">
        <el-card v-for="service in serviceList" :key="service.type" class="box-card">
          <template #header>
            <div class="clearfix">
              <span>{{ service.name }}</span>
              <el-switch
                v-model="service.enabled"
                style="float: right"
                @change="handleServiceStatusChange(service)"
              ></el-switch>
            </div>
          </template>
          <el-form :model="service.config" label-width="120px" :disabled="!service.enabled">
            <template v-if="service.type === 'email'">
              <el-form-item label="SMTP服务器">
                <el-input v-model="service.config.smtpServer"></el-input>
              </el-form-item>
              <el-form-item label="SMTP端口">
                <el-input-number v-model="service.config.smtpPort"></el-input-number>
              </el-form-item>
              <el-form-item label="邮箱账号">
                <el-input v-model="service.config.username"></el-input>
              </el-form-item>
              <el-form-item label="邮箱密码">
                <el-input v-model="service.config.password" type="password"></el-input>
              </el-form-item>
            </template>
            <template v-else-if="service.type === 'sms'">
              <el-form-item label="服务提供商">
                <el-select v-model="service.config.provider">
                  <el-option label="阿里云" value="aliyun"></el-option>
                  <el-option label="腾讯云" value="tencent"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="AccessKey">
                <el-input v-model="service.config.accessKey"></el-input>
              </el-form-item>
              <el-form-item label="SecretKey">
                <el-input v-model="service.config.secretKey" type="password"></el-input>
              </el-form-item>
              <el-form-item label="短信签名">
                <el-input v-model="service.config.signName"></el-input>
              </el-form-item>
            </template>
            <template v-else-if="service.type === 'oss'">
              <el-form-item label="存储类型">
                <el-select v-model="service.config.type">
                  <el-option label="阿里云OSS" value="aliyun"></el-option>
                  <el-option label="腾讯云COS" value="tencent"></el-option>
                  <el-option label="七牛云" value="qiniu"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="AccessKey">
                <el-input v-model="service.config.accessKey"></el-input>
              </el-form-item>
              <el-form-item label="SecretKey">
                <el-input v-model="service.config.secretKey" type="password"></el-input>
              </el-form-item>
              <el-form-item label="Bucket">
                <el-input v-model="service.config.bucket"></el-input>
              </el-form-item>
              <el-form-item label="访问域名">
                <el-input v-model="service.config.domain"></el-input>
              </el-form-item>
            </template>
            <el-form-item>
              <el-button type="primary" @click="handleSaveService(service)">保存配置</el-button>
              <el-button type="info" @click="handleTestService(service)">测试连接</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 应用表单对话框 -->
    <el-dialog v-model:visible="open" :title="title" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="form.appName" placeholder="请输入应用名称" />
        </el-form-item>
        <el-form-item label="回调地址" prop="callbackUrl">
          <el-input v-model="form.callbackUrl" placeholder="请输入回调地址" />
        </el-form-item>
        <el-form-item label="应用说明" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入应用说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Webhook表单对话框 -->
    <el-dialog v-model:visible="webhookOpen" :title="webhookTitle" width="500px" append-to-body>
      <el-form ref="webhookForm" :model="webhookForm" :rules="webhookRules" label-width="80px">
        <el-form-item label="事件类型" prop="eventType">
          <el-select v-model="webhookForm.eventType" placeholder="请选择事件类型">
            <el-option label="消息事件" value="message" />
            <el-option label="用户事件" value="user" />
            <el-option label="群组事件" value="group" />
            <el-option label="系统事件" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="回调URL" prop="url">
          <el-input v-model="webhookForm.url" placeholder="请输入回调URL" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="webhookForm.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitWebhookForm">确 定</el-button>
          <el-button @click="cancelWebhook">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  listApp,
  getApp,
  delApp,
  addApp,
  updateApp,
  resetAppSecret,
  listWebhook,
  getWebhook,
  delWebhook,
  addWebhook,
  updateWebhook,
  testWebhook,
  listService,
  updateService,
  testService,
} from '@/api/im/integration'
import clipboard from '@/utils/clipboard'

export default {
  name: 'ImIntegration',
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
      activeTab: 'app',
      // 应用表格数据
      appList: [],
      // Webhook表格数据
      webhookList: [],
      // 服务列表数据
      serviceList: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      // Webhook弹出层标题
      webhookTitle: '',
      // 是否显示Webhook弹出层
      webhookOpen: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        appName: undefined,
        status: undefined,
        eventType: undefined,
      },
      // 表单参数
      form: {},
      // Webhook表单参数
      webhookForm: {},
      // 表单校验
      rules: {
        appName: [{ required: true, message: '应用名称不能为空', trigger: 'blur' }],
        callbackUrl: [{ required: true, message: '回调地址不能为空', trigger: 'blur' }],
      },
      // Webhook表单校验
      webhookRules: {
        eventType: [{ required: true, message: '事件类型不能为空', trigger: 'change' }],
        url: [{ required: true, message: '回调URL不能为空', trigger: 'blur' }],
      },
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true
      switch (this.activeTab) {
        case 'app':
          listApp(this.queryParams).then(response => {
            this.appList = response.rows
            this.total = response.total
            this.loading = false
          })
          break
        case 'webhook':
          listWebhook(this.queryParams).then(response => {
            this.webhookList = response.rows
            this.total = response.total
            this.loading = false
          })
          break
        case 'service':
          listService().then(response => {
            this.serviceList = response.data
            this.loading = false
          })
          break
      }
    },
    /** 复制文本 */
    copyText(text) {
      clipboard(text)
    },
    /** 切换标签页 */
    handleTabClick() {
      this.resetQuery()
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
    /** 新增应用按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加应用'
    },
    /** 修改应用按钮操作 */
    handleUpdate(row) {
      this.reset()
      const appId = row.appId
      getApp(appId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改应用'
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.appId != undefined) {
            updateApp(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addApp(this.form).then(() => {
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
      this.$modal
        .confirm('是否确认删除该应用？')
        .then(function () {
          return delApp(row.appId)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    /** 重置密钥按钮操作 */
    handleResetSecret(row) {
      this.$modal
        .confirm('是否确认重置该应用的密钥？重置后需要更新使用此应用的系统配置。')
        .then(function () {
          return resetAppSecret(row.appId)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('重置成功')
        })
        .catch(() => {})
    },
    /** 新增Webhook按钮操作 */
    handleAddWebhook() {
      this.resetWebhook()
      this.webhookOpen = true
      this.webhookTitle = '添加Webhook'
    },
    /** 修改Webhook按钮操作 */
    handleUpdateWebhook(row) {
      this.resetWebhook()
      const webhookId = row.webhookId
      getWebhook(webhookId).then(response => {
        this.webhookForm = response.data
        this.webhookOpen = true
        this.webhookTitle = '修改Webhook'
      })
    },
    /** 提交Webhook表单 */
    submitWebhookForm() {
      this.$refs['webhookForm'].validate(valid => {
        if (valid) {
          if (this.webhookForm.webhookId != undefined) {
            updateWebhook(this.webhookForm).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.webhookOpen = false
              this.getList()
            })
          } else {
            addWebhook(this.webhookForm).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.webhookOpen = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除Webhook按钮操作 */
    handleDeleteWebhook(row) {
      this.$modal
        .confirm('是否确认删除该Webhook？')
        .then(function () {
          return delWebhook(row.webhookId)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    /** 测试Webhook按钮操作 */
    handleTestWebhook(row) {
      testWebhook(row.webhookId).then(() => {
        this.$modal.msgSuccess('测试成功')
      })
    },
    /** 保存服务配置 */
    handleSaveService(service) {
      updateService(service).then(() => {
        this.$modal.msgSuccess('保存成功')
      })
    },
    /** 测试服务连接 */
    handleTestService(service) {
      testService(service.type).then(() => {
        this.$modal.msgSuccess('测试成功')
      })
    },
    /** 表单重置 */
    reset() {
      this.form = {
        appId: undefined,
        appName: undefined,
        callbackUrl: undefined,
        description: undefined,
      }
      this.resetForm('form')
    },
    /** Webhook表单重置 */
    resetWebhook() {
      this.webhookForm = {
        webhookId: undefined,
        eventType: undefined,
        url: undefined,
        description: undefined,
      }
      this.resetForm('webhookForm')
    },
  },
}
</script>

<style scoped>
.box-card {
  margin-bottom: 20px;
}
</style>
