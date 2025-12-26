<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- 基础配置 -->
      <el-tab-pane label="基础配置" name="basic">
        <el-form ref="basicForm" :model="basicForm" :rules="basicRules" label-width="150px">
          <el-card class="box-card">
            <template #header>
              <div class="clearfix">
                <span>系统参数</span>
              </div>
            </template>
            <el-form-item label="系统名称" prop="systemName">
              <el-input v-model="basicForm.systemName" placeholder="请输入系统名称" />
            </el-form-item>
            <el-form-item label="系统版本" prop="systemVersion">
              <el-input v-model="basicForm.systemVersion" placeholder="请输入系统版本" />
            </el-form-item>
            <el-form-item label="管理员邮箱" prop="adminEmail">
              <el-input v-model="basicForm.adminEmail" placeholder="请输入管理员邮箱" />
            </el-form-item>
          </el-card>

          <el-card class="box-card">
            <template #header>
              <div class="clearfix">
                <span>WebSocket配置</span>
              </div>
            </template>
            <el-form-item label="WebSocket端口" prop="wsPort">
              <el-input-number v-model="basicForm.wsPort" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="心跳间隔(秒)" prop="heartbeatInterval">
              <el-input-number v-model="basicForm.heartbeatInterval" :min="1" :max="300" />
            </el-form-item>
            <el-form-item label="最大连接数" prop="maxConnections">
              <el-input-number v-model="basicForm.maxConnections" :min="100" :max="100000" />
            </el-form-item>
          </el-card>

          <el-card class="box-card">
            <template #header>
              <div class="clearfix">
                <span>消息配置</span>
              </div>
            </template>
            <el-form-item label="最大消息长度" prop="maxMessageLength">
              <el-input-number v-model="basicForm.maxMessageLength" :min="1" :max="10000" />
            </el-form-item>
            <el-form-item label="消息保存天数" prop="messageSaveDays">
              <el-input-number v-model="basicForm.messageSaveDays" :min="1" :max="365" />
            </el-form-item>
            <el-form-item label="离线消息最大数" prop="maxOfflineMessages">
              <el-input-number v-model="basicForm.maxOfflineMessages" :min="100" :max="10000" />
            </el-form-item>
          </el-card>
        </el-form>
      </el-tab-pane>

      <!-- 文件配置 -->
      <el-tab-pane label="文件配置" name="file">
        <el-form ref="fileForm" :model="fileForm" :rules="fileRules" label-width="150px">
          <el-card class="box-card">
            <template #header>
              <div class="clearfix">
                <span>存储配置</span>
              </div>
            </template>
            <el-form-item label="存储方式" prop="storageType">
              <el-radio-group v-model="fileForm.storageType">
                <el-radio label="local">本地存储</el-radio>
                <el-radio label="oss">对象存储</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item
              v-if="fileForm.storageType === 'local'"
              label="存储路径"
              prop="storagePath"
            >
              <el-input v-model="fileForm.storagePath" placeholder="请输入存储路径" />
            </el-form-item>
            <template v-if="fileForm.storageType === 'oss'">
              <el-form-item label="OSS服务商" prop="ossProvider">
                <el-select v-model="fileForm.ossProvider">
                  <el-option label="阿里云OSS" value="aliyun" />
                  <el-option label="腾讯云COS" value="tencent" />
                  <el-option label="七牛云" value="qiniu" />
                </el-select>
              </el-form-item>
              <el-form-item label="AccessKey" prop="accessKey">
                <el-input v-model="fileForm.accessKey" placeholder="请输入AccessKey" />
              </el-form-item>
              <el-form-item label="SecretKey" prop="secretKey">
                <el-input
                  v-model="fileForm.secretKey"
                  placeholder="请输入SecretKey"
                  show-password
                />
              </el-form-item>
              <el-form-item label="Bucket" prop="bucket">
                <el-input v-model="fileForm.bucket" placeholder="请输入Bucket" />
              </el-form-item>
              <el-form-item label="访问域名" prop="domain">
                <el-input v-model="fileForm.domain" placeholder="请输入访问域名" />
              </el-form-item>
            </template>
          </el-card>

          <el-card class="box-card">
            <template #header>
              <div class="clearfix">
                <span>上传限制</span>
              </div>
            </template>
            <el-form-item label="单文件大小限制(MB)" prop="maxFileSize">
              <el-input-number v-model="fileForm.maxFileSize" :min="1" :max="1000" />
            </el-form-item>
            <el-form-item label="允许的文件类型" prop="allowedTypes">
              <el-select
                v-model="fileForm.allowedTypes"
                multiple
                placeholder="请选择允许的文件类型"
              >
                <el-option label="图片文件" value="image" />
                <el-option label="文档文件" value="document" />
                <el-option label="音频文件" value="audio" />
                <el-option label="视频文件" value="video" />
              </el-select>
            </el-form-item>
          </el-card>
        </el-form>
      </el-tab-pane>

      <!-- 安全配置 -->
      <el-tab-pane label="安全配置" name="security">
        <el-form
          ref="securityForm"
          :model="securityForm"
          :rules="securityRules"
          label-width="150px"
        >
          <el-card class="box-card">
            <template #header>
              <div class="clearfix">
                <span>认证配置</span>
              </div>
            </template>
            <el-form-item label="Token有效期(分钟)" prop="tokenExpire">
              <el-input-number v-model="securityForm.tokenExpire" :min="1" :max="1440" />
            </el-form-item>
            <el-form-item label="多端登录" prop="multiLogin">
              <el-switch v-model="securityForm.multiLogin" />
            </el-form-item>
            <el-form-item label="登录失败锁定" prop="loginLock">
              <el-switch v-model="securityForm.loginLock" />
            </el-form-item>
            <el-form-item v-if="securityForm.loginLock" label="锁定阈值" prop="lockThreshold">
              <el-input-number v-model="securityForm.lockThreshold" :min="1" :max="10" />
            </el-form-item>
          </el-card>

          <el-card class="box-card">
            <template #header>
              <div class="clearfix">
                <span>加密配置</span>
              </div>
            </template>
            <el-form-item label="消息加密" prop="messageEncrypt">
              <el-switch v-model="securityForm.messageEncrypt" />
            </el-form-item>
            <el-form-item
              v-if="securityForm.messageEncrypt"
              label="加密算法"
              prop="encryptAlgorithm"
            >
              <el-select v-model="securityForm.encryptAlgorithm">
                <el-option label="AES" value="AES" />
                <el-option label="RSA" value="RSA" />
              </el-select>
            </el-form-item>
          </el-card>

          <el-card class="box-card">
            <template #header>
              <div class="clearfix">
                <span>访问控制</span>
              </div>
            </template>
            <el-form-item label="IP白名单" prop="ipWhitelist">
              <el-input
                v-model="securityForm.ipWhitelist"
                type="textarea"
                placeholder="请输入IP白名单，每行一个"
                :rows="4"
              />
            </el-form-item>
            <el-form-item label="API访问频率限制" prop="apiRateLimit">
              <el-input-number v-model="securityForm.apiRateLimit" :min="1" :max="1000" />
              <span class="el-form-item__description">次/分钟</span>
            </el-form-item>
          </el-card>
        </el-form>
      </el-tab-pane>

      <!-- 通知配置 -->
      <el-tab-pane label="通知配置" name="notification">
        <el-form
          ref="notificationForm"
          :model="notificationForm"
          :rules="notificationRules"
          label-width="150px"
        >
          <el-card class="box-card">
            <template #header>
              <div class="clearfix">
                <span>邮件配置</span>
              </div>
            </template>
            <el-form-item label="SMTP服务器" prop="smtpServer">
              <el-input v-model="notificationForm.smtpServer" placeholder="请输入SMTP服务器地址" />
            </el-form-item>
            <el-form-item label="SMTP端口" prop="smtpPort">
              <el-input-number v-model="notificationForm.smtpPort" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="发件人邮箱" prop="senderEmail">
              <el-input v-model="notificationForm.senderEmail" placeholder="请输入发件人邮箱" />
            </el-form-item>
            <el-form-item label="邮箱密码" prop="emailPassword">
              <el-input
                v-model="notificationForm.emailPassword"
                type="password"
                placeholder="请输入邮箱密码"
              />
            </el-form-item>
          </el-card>

          <el-card class="box-card">
            <template #header>
              <div class="clearfix">
                <span>消息通知</span>
              </div>
            </template>
            <el-form-item label="新消息通知" prop="newMessageNotify">
              <el-switch v-model="notificationForm.newMessageNotify" />
            </el-form-item>
            <el-form-item label="系统通知" prop="systemNotify">
              <el-switch v-model="notificationForm.systemNotify" />
            </el-form-item>
            <el-form-item label="声音提醒" prop="soundNotify">
              <el-switch v-model="notificationForm.soundNotify" />
            </el-form-item>
          </el-card>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <div class="bottom-buttons">
      <el-button type="primary" @click="handleSubmit">保存配置</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>
  </div>
</template>

<script>
import {
  getBasicConfig,
  getFileConfig,
  getSecurityConfig,
  getNotificationConfig,
  updateConfig,
} from '@/api/im/config'

export default {
  name: 'ImConfig',
  data() {
    return {
      // 当前激活的标签页
      activeTab: 'basic',
      // 基础配置表单
      basicForm: {
        systemName: '',
        systemVersion: '',
        adminEmail: '',
        wsPort: 9090,
        heartbeatInterval: 30,
        maxConnections: 1000,
        maxMessageLength: 1000,
        messageSaveDays: 30,
        maxOfflineMessages: 1000,
      },
      // 文件配置表单
      fileForm: {
        storageType: 'local',
        storagePath: '',
        ossProvider: '',
        accessKey: '',
        secretKey: '',
        bucket: '',
        domain: '',
        maxFileSize: 100,
        allowedTypes: [],
      },
      // 安全配置表单
      securityForm: {
        tokenExpire: 120,
        multiLogin: false,
        loginLock: true,
        lockThreshold: 5,
        messageEncrypt: true,
        encryptAlgorithm: 'AES',
        ipWhitelist: '',
        apiRateLimit: 100,
      },
      // 通知配置表单
      notificationForm: {
        smtpServer: '',
        smtpPort: 465,
        senderEmail: '',
        emailPassword: '',
        newMessageNotify: true,
        systemNotify: true,
        soundNotify: true,
      },
      // 表单校验规则
      basicRules: {
        systemName: [{ required: true, message: '请输入系统名称', trigger: 'blur' }],
        adminEmail: [
          { required: true, message: '请输入管理员邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] },
        ],
      },
      fileRules: {
        storagePath: [{ required: true, message: '请输入存储路径', trigger: 'blur' }],
        accessKey: [{ required: true, message: '请输入AccessKey', trigger: 'blur' }],
        secretKey: [{ required: true, message: '请输入SecretKey', trigger: 'blur' }],
      },
      securityRules: {
        ipWhitelist: [
          {
            pattern: /^(\d{1,3}\.){3}\d{1,3}(\n(\d{1,3}\.){3}\d{1,3})*$/,
            message: '请输入正确的IP地址格式',
            trigger: 'blur',
          },
        ],
      },
      notificationRules: {
        smtpServer: [{ required: true, message: '请输入SMTP服务器地址', trigger: 'blur' }],
        senderEmail: [
          { required: true, message: '请输入发件人邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] },
        ],
      },
    }
  },
  created() {
    this.getConfig()
  },
  methods: {
    /** 获取配置信息 */
    getConfig() {
      switch (this.activeTab) {
        case 'basic':
          getBasicConfig().then(response => {
            this.basicForm = response.data
          })
          break
        case 'file':
          getFileConfig().then(response => {
            this.fileForm = response.data
          })
          break
        case 'security':
          getSecurityConfig().then(response => {
            this.securityForm = response.data
          })
          break
        case 'notification':
          getNotificationConfig().then(response => {
            this.notificationForm = response.data
          })
          break
      }
    },
    /** 切换标签页 */
    handleTabClick() {
      this.getConfig()
    },
    /** 提交表单 */
    handleSubmit() {
      let form = null
      let formName = ''
      switch (this.activeTab) {
        case 'basic':
          form = this.basicForm
          formName = 'basicForm'
          break
        case 'file':
          form = this.fileForm
          formName = 'fileForm'
          break
        case 'security':
          form = this.securityForm
          formName = 'securityForm'
          break
        case 'notification':
          form = this.notificationForm
          formName = 'notificationForm'
          break
      }

      this.$refs[formName].validate(valid => {
        if (valid) {
          updateConfig({
            type: this.activeTab,
            data: form,
          }).then(() => {
            this.$modal.msgSuccess('保存成功')
          })
        }
      })
    },
    /** 重置表单 */
    handleReset() {
      switch (this.activeTab) {
        case 'basic':
          this.$refs.basicForm.resetFields()
          break
        case 'file':
          this.$refs.fileForm.resetFields()
          break
        case 'security':
          this.$refs.securityForm.resetFields()
          break
        case 'notification':
          this.$refs.notificationForm.resetFields()
          break
      }
      this.getConfig()
    },
  },
}
</script>

<style scoped>
.box-card {
  margin-bottom: 20px;
}
.bottom-buttons {
  margin-top: 20px;
  text-align: center;
}
.el-form-item__description {
  margin-left: 10px;
  color: #909399;
}
</style>
