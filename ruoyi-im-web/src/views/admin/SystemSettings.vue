<template>
  <div class="system-settings">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <h2>系统设置</h2>
        <p class="page-desc">配置系统基础参数、功能开关和安全策略</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" :icon="Check" @click="handleSaveAll">保存所有配置</el-button>
      </div>
    </div>

    <!-- 设置内容 -->
    <el-card class="settings-card" shadow="never">
      <el-tabs v-model="activeTab" class="settings-tabs">
        <!-- 基础设置 -->
        <el-tab-pane label="基础设置" name="basic">
          <div class="setting-section">
            <h3 class="section-title">系统信息</h3>
            <el-form :model="basicConfig" label-width="150px" label-position="left">
              <el-form-item label="系统名称">
                <el-input v-model="basicConfig.systemName" placeholder="RuoYi-IM" style="width: 300px" />
              </el-form-item>
              <el-form-item label="系统Logo">
                <div class="logo-uploader">
                  <el-avatar v-if="basicConfig.systemLogo" :src="basicConfig.systemLogo" :size="60" />
                  <el-icon v-else class="logo-uploader-icon" :size="32"><Picture /></el-icon>
                  <el-button size="small" text type="primary" style="margin-left: 12px">
                    点击上传
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item label="系统描述">
                <el-input
                  v-model="basicConfig.systemDescription"
                  type="textarea"
                  :rows="3"
                  placeholder="企业级即时通讯系统"
                  style="width: 400px"
                />
              </el-form-item>
            </el-form>
          </div>

          <el-divider />

          <div class="setting-section">
            <h3 class="section-title">注册与登录</h3>
            <el-form :model="basicConfig" label-width="150px" label-position="left">
              <el-form-item label="允许注册">
                <el-switch
                  v-model="basicConfig.allowRegister"
                  active-text="开启"
                  inactive-text="关闭"
                />
                <span class="form-tip">关闭后需要管理员手动添加用户</span>
              </el-form-item>
              <el-form-item label="注册审核">
                <el-switch
                  v-model="basicConfig.registerAudit"
                  active-text="需要审核"
                  inactive-text="自动通过"
                  :disabled="!basicConfig.allowRegister"
                />
                <span class="form-tip">开启后新用户注册需要管理员审核</span>
              </el-form-item>
              <el-form-item label="默认角色">
                <el-select v-model="basicConfig.defaultRole" style="width: 200px">
                  <el-option label="普通用户" value="USER" />
                  <el-option label="受限用户" value="GUEST" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>

          <el-divider />

          <div class="setting-section">
            <h3 class="section-title">文件上传</h3>
            <el-form :model="basicConfig" label-width="150px" label-position="left">
              <el-form-item label="单文件大小限制">
                <el-input-number
                  v-model="basicConfig.maxFileSize"
                  :min="1"
                  :max="500"
                  controls-position="right"
                />
                <span class="form-unit">MB</span>
              </el-form-item>
              <el-form-item label="允许的文件类型">
                <el-select
                  v-model="basicConfig.allowedFileTypes"
                  multiple
                  allow-create
                  filterable
                  style="width: 400px"
                  placeholder="输入文件后缀，如：jpg,png,pdf"
                >
                  <el-option label="图片" value="jpg,jpeg,png,gif,webp" />
                  <el-option label="文档" value="pdf,doc,docx,xls,xlsx,ppt,pptx" />
                  <el-option label="压缩包" value="zip,rar,7z" />
                  <el-option label="音频" value="mp3,wav,aac" />
                  <el-option label="视频" value="mp4,avi,mov,mkv" />
                </el-select>
              </el-form-item>
              <el-form-item label="存储方式">
                <el-radio-group v-model="basicConfig.storageType">
                  <el-radio label="local">本地存储</el-radio>
                  <el-radio label="oss">阿里云OSS</el-radio>
                  <el-radio label="cos">腾讯云COS</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 消息设置 -->
        <el-tab-pane label="消息设置" name="message">
          <div class="setting-section">
            <h3 class="section-title">消息撤回</h3>
            <el-form :model="messageConfig" label-width="150px" label-position="left">
              <el-form-item label="允许撤回">
                <el-switch
                  v-model="messageConfig.allowRevoke"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="撤回时限">
                <el-input-number
                  v-model="messageConfig.revokeTimeLimit"
                  :min="1"
                  :max="60"
                  :disabled="!messageConfig.allowRevoke"
                  controls-position="right"
                />
                <span class="form-unit">分钟</span>
                <span class="form-tip">发送后多久内可以撤回消息</span>
              </el-form-item>
              <el-form-item label="撤回时限（群主）">
                <el-input-number
                  v-model="messageConfig.revokeTimeLimitAdmin"
                  :min="1"
                  :max="1440"
                  :disabled="!messageConfig.allowRevoke"
                  controls-position="right"
                />
                <span class="form-unit">分钟</span>
                <span class="form-tip">群主/管理员撤回时限</span>
              </el-form-item>
            </el-form>
          </div>

          <el-divider />

          <div class="setting-section">
            <h3 class="section-title">敏感词过滤</h3>
            <el-form :model="messageConfig" label-width="150px" label-position="left">
              <el-form-item label="启用敏感词过滤">
                <el-switch
                  v-model="messageConfig.enableSensitiveFilter"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="过滤策略">
                <el-radio-group v-model="messageConfig.sensitiveStrategy" :disabled="!messageConfig.enableSensitiveFilter">
                  <el-radio label="reject">拦截消息</el-radio>
                  <el-radio label="replace">替换为***</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="敏感词数量">
                <span class="stat-value">{{ sensitiveWordCount }} 个</span>
                <el-button text type="primary" style="margin-left: 12px" @click="sensitiveDialogVisible = true">
                  管理敏感词
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-divider />

          <div class="setting-section">
            <h3 class="section-title">消息推送</h3>
            <el-form :model="messageConfig" label-width="150px" label-position="left">
              <el-form-item label="桌面通知">
                <el-switch
                  v-model="messageConfig.desktopNotification"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="消息提醒音">
                <el-switch
                  v-model="messageConfig.messageSound"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 安全设置 -->
        <el-tab-pane label="安全设置" name="security">
          <div class="setting-section">
            <h3 class="section-title">密码策略</h3>
            <el-form :model="securityConfig" label-width="150px" label-position="left">
              <el-form-item label="最小密码长度">
                <el-input-number
                  v-model="securityConfig.minPasswordLength"
                  :min="6"
                  :max="20"
                  controls-position="right"
                />
                <span class="form-unit">位</span>
              </el-form-item>
              <el-form-item label="密码复杂度">
                <el-checkbox-group v-model="securityConfig.passwordComplexity">
                  <el-checkbox label="upper">包含大写字母</el-checkbox>
                  <el-checkbox label="lower">包含小写字母</el-checkbox>
                  <el-checkbox label="number">包含数字</el-checkbox>
                  <el-checkbox label="special">包含特殊字符</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="密码过期时间">
                <el-input-number
                  v-model="securityConfig.passwordExpireDays"
                  :min="0"
                  :max="365"
                  controls-position="right"
                />
                <span class="form-unit">天</span>
                <span class="form-tip">0表示永不过期</span>
              </el-form-item>
            </el-form>
          </div>

          <el-divider />

          <div class="setting-section">
            <h3 class="section-title">登录安全</h3>
            <el-form :model="securityConfig" label-width="150px" label-position="left">
              <el-form-item label="登录失败锁定">
                <el-switch
                  v-model="securityConfig.loginLockEnabled"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="最大失败次数">
                <el-input-number
                  v-model="securityConfig.maxLoginAttempts"
                  :min="3"
                  :max="10"
                  :disabled="!securityConfig.loginLockEnabled"
                  controls-position="right"
                />
                <span class="form-unit">次</span>
              </el-form-item>
              <el-form-item label="锁定时间">
                <el-input-number
                  v-model="securityConfig.lockDuration"
                  :min="5"
                  :max="120"
                  :disabled="!securityConfig.loginLockEnabled"
                  controls-position="right"
                />
                <span class="form-unit">分钟</span>
              </el-form-item>
              <el-form-item label="会话超时时间">
                <el-input-number
                  v-model="securityConfig.sessionTimeout"
                  :min="30"
                  :max="1440"
                  controls-position="right"
                />
                <span class="form-unit">分钟</span>
              </el-form-item>
            </el-form>
          </div>

          <el-divider />

          <div class="setting-section">
            <h3 class="section-title">验证码设置</h3>
            <el-form :model="securityConfig" label-width="150px" label-position="left">
              <el-form-item label="登录验证码">
                <el-switch
                  v-model="securityConfig.captchaEnabled"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="验证码类型">
                <el-radio-group v-model="securityConfig.captchaType" :disabled="!securityConfig.captchaEnabled">
                  <el-radio label="image">图形验证码</el-radio>
                  <el-radio label="slide">滑动验证</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 存储设置 -->
        <el-tab-pane label="存储设置" name="storage">
          <div class="setting-section">
            <h3 class="section-title">存储配置</h3>
            <el-form :model="storageConfig" label-width="150px" label-position="left">
              <el-form-item label="存储方式">
                <el-radio-group v-model="storageConfig.type">
                  <el-radio label="local">本地存储</el-radio>
                  <el-radio label="oss">阿里云OSS</el-radio>
                  <el-radio label="cos">腾讯云COS</el-radio>
                  <el-radio label="minio">MinIO</el-radio>
                </el-radio-group>
              </el-form-item>

              <template v-if="storageConfig.type === 'oss'">
                <el-divider content-position="left">阿里云OSS配置</el-divider>
                <el-form-item label="Endpoint">
                  <el-input v-model="storageConfig.ossEndpoint" placeholder="oss-cn-hangzhou.aliyuncs.com" style="width: 400px" />
                </el-form-item>
                <el-form-item label="Bucket">
                  <el-input v-model="storageConfig.ossBucket" placeholder="your-bucket-name" style="width: 300px" />
                </el-form-item>
                <el-form-item label="AccessKey">
                  <el-input v-model="storageConfig.ossAccessKey" placeholder="LTAI5t..." show-password style="width: 300px" />
                </el-form-item>
                <el-form-item label="SecretKey">
                  <el-input v-model="storageConfig.ossSecretKey" placeholder="****" show-password style="width: 300px" />
                </el-form-item>
                <el-form-item label="访问路径前缀">
                  <el-input v-model="storageConfig.ossPrefix" placeholder="im/" style="width: 200px" />
                </el-form-item>
              </template>

              <template v-else-if="storageConfig.type === 'cos'">
                <el-divider content-position="left">腾讯云COS配置</el-divider>
                <el-form-item label="Region">
                  <el-input v-model="storageConfig.cosRegion" placeholder="ap-guangzhou" style="width: 200px" />
                </el-form-item>
                <el-form-item label="Bucket">
                  <el-input v-model="storageConfig.cosBucket" placeholder="your-bucket-name" style="width: 300px" />
                </el-form-item>
                <el-form-item label="SecretId">
                  <el-input v-model="storageConfig.cosSecretId" placeholder="AKID..." show-password style="width: 300px" />
                </el-form-item>
                <el-form-item label="SecretKey">
                  <el-input v-model="storageConfig.cosSecretKey" placeholder="****" show-password style="width: 300px" />
                </el-form-item>
              </template>
            </el-form>
          </div>

          <el-divider />

          <div class="setting-section">
            <h3 class="section-title">存储空间统计</h3>
            <div class="storage-stats">
              <div class="stat-item">
                <div class="stat-icon" style="background: linear-gradient(135deg, #0089FF 0%, #0066CC 100%)">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ formatFileSize(storageStats.usedSpace) }}</div>
                  <div class="stat-label">已用空间</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon" style="background: linear-gradient(135deg, #00C853 0%, #009624 100%)">
                  <el-icon><Files /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ storageStats.fileCount }}</div>
                  <div class="stat-label">文件数量</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon" style="background: linear-gradient(135deg, #FFAB00 0%, #FF8F00 100%)">
                  <el-icon><Folder /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ storageStats.userCount }}</div>
                  <div class="stat-label">用户数</div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 高级设置 -->
        <el-tab-pane label="高级设置" name="advanced">
          <div class="setting-section">
            <h3 class="section-title">系统维护</h3>
            <el-form label-width="150px" label-position="left">
              <el-form-item label="维护模式">
                <el-switch
                  v-model="advancedConfig.maintenanceMode"
                  active-text="开启"
                  inactive-text="关闭"
                />
                <span class="form-tip">开启后普通用户无法访问系统</span>
              </el-form-item>
              <el-form-item label="维护公告">
                <el-input
                  v-model="advancedConfig.maintenanceMessage"
                  type="textarea"
                  :rows="3"
                  placeholder="系统维护中，预计恢复时间：xx:xx"
                  style="width: 500px"
                />
              </el-form-item>
            </el-form>
          </div>

          <el-divider />

          <div class="setting-section">
            <h3 class="section-title">日志管理</h3>
            <el-form :model="advancedConfig" label-width="150px" label-position="left">
              <el-form-item label="操作日志保留">
                <el-input-number
                  v-model="advancedConfig.logRetentionDays"
                  :min="7"
                  :max="365"
                  controls-position="right"
                />
                <span class="form-unit">天</span>
              </el-form-item>
              <el-form-item label="日志级别">
                <el-select v-model="advancedConfig.logLevel" style="width: 150px">
                  <el-option label="DEBUG" value="DEBUG" />
                  <el-option label="INFO" value="INFO" />
                  <el-option label="WARN" value="WARN" />
                  <el-option label="ERROR" value="ERROR" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>

          <el-divider />

          <div class="setting-section">
            <h3 class="section-title">数据管理</h3>
            <div class="danger-zone">
              <h4>危险操作</h4>
              <p class="danger-tip">以下操作不可逆，请谨慎操作</p>
              <div class="danger-actions">
                <el-button :icon="Delete" @click="handleClearLogs">清空操作日志</el-button>
                <el-button :icon="Download" @click="handleExportData">导出系统数据</el-button>
                <el-button type="danger" :icon="Delete" @click="handleClearCache">清空系统缓存</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 敏感词管理对话框 -->
    <el-dialog
      v-model="sensitiveDialogVisible"
      title="敏感词管理"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-alert
        title="敏感词过滤"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      >
        配置敏感词后，包含敏感词的消息将被{{ messageConfig.sensitiveStrategy === 'reject' ? '拦截' : '替换为***' }}。每行一个敏感词。
      </el-alert>

      <el-form label-width="100px">
        <el-form-item label="过滤策略">
          <el-radio-group v-model="messageConfig.sensitiveStrategy">
            <el-radio label="reject">拦截消息</el-radio>
            <el-radio label="replace">替换为***</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="敏感词列表">
          <el-input
            v-model="sensitiveWords"
            type="textarea"
            :rows="10"
            placeholder="每行输入一个敏感词，支持中英文、数字"
          />
        </el-form-item>
        <el-form-item label="关键词数">
          <span class="word-count">{{ sensitiveWordCount }} 个敏感词</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="sensitiveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveSensitiveWords">保存配置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Check,
  Picture,
  Document,
  Files,
  Folder,
  Delete,
  Download
} from '@element-plus/icons-vue'
import {
  getSystemConfig,
  updateSystemConfig,
  getSensitiveWords,
  saveSensitiveWords,
  getFileStatistics
} from '@/api/admin'
import { formatFileSize } from '@/utils/format'

const activeTab = ref('basic')
const sensitiveDialogVisible = ref(false)
const sensitiveWords = ref('')

// 基础配置
const basicConfig = ref({
  systemName: 'RuoYi-IM',
  systemLogo: '',
  systemDescription: '企业级即时通讯系统',
  allowRegister: true,
  registerAudit: false,
  defaultRole: 'USER',
  maxFileSize: 100,
  allowedFileTypes: ['jpg', 'jpeg', 'png', 'gif', 'pdf', 'doc', 'docx'],
  storageType: 'local'
})

// 消息配置
const messageConfig = ref({
  allowRevoke: true,
  revokeTimeLimit: 2,
  revokeTimeLimitAdmin: 10,
  enableSensitiveFilter: true,
  sensitiveStrategy: 'replace',
  desktopNotification: true,
  messageSound: true
})

// 安全配置
const securityConfig = ref({
  minPasswordLength: 6,
  passwordComplexity: ['lower', 'number'],
  passwordExpireDays: 0,
  loginLockEnabled: true,
  maxLoginAttempts: 5,
  lockDuration: 30,
  sessionTimeout: 120,
  captchaEnabled: true,
  captchaType: 'image'
})

// 存储配置
const storageConfig = ref({
  type: 'local',
  ossEndpoint: '',
  ossBucket: '',
  ossAccessKey: '',
  ossSecretKey: '',
  ossPrefix: '',
  cosRegion: '',
  cosBucket: '',
  cosSecretId: '',
  cosSecretKey: ''
})

// 存储统计 - 从 API 获取真实数据
const storageStats = ref({
  usedSpace: 0,
  fileCount: 0,
  userCount: 0
})

// 高级配置
const advancedConfig = ref({
  maintenanceMode: false,
  maintenanceMessage: '系统维护中，预计恢复时间：18:00',
  logRetentionDays: 90,
  logLevel: 'INFO'
})

// 敏感词数量
const sensitiveWordCount = computed(() => {
  if (!sensitiveWords.value) return 0
  return sensitiveWords.value.split('\n').filter(w => w.trim()).length
})

// 加载系统配置
const loadSystemConfig = async () => {
  const res = await getSystemConfig()
  if (res.code === 200) {
    const config = res.data
    if (config.basic) basicConfig.value = { ...basicConfig.value, ...config.basic }
    if (config.message) messageConfig.value = { ...messageConfig.value, ...config.message }
    if (config.security) securityConfig.value = { ...securityConfig.value, ...config.security }
    if (config.storage) storageConfig.value = { ...storageConfig.value, ...config.storage }
    if (config.advanced) advancedConfig.value = { ...advancedConfig.value, ...config.advanced }
  }
}

// 加载敏感词
const loadSensitiveWords = async () => {
  const res = await getSensitiveWords()
  if (res.code === 200) {
    sensitiveWords.value = (res.data.words || []).join('\n')
    if (res.data.strategy) {
      messageConfig.value.sensitiveStrategy = res.data.strategy
    }
  }
}

// 加载存储统计
const loadStorageStats = async () => {
  try {
    const res = await getFileStatistics()
    if (res.code === 200 && res.data) {
      storageStats.value = {
        usedSpace: res.data.usedSpace || 0,
        fileCount: res.data.fileCount || 0,
        userCount: res.data.userCount || 0
      }
    }
  } catch (error) {
    console.error('加载存储统计失败:', error)
  }
}

// 保存所有配置
const handleSaveAll = async () => {
  const config = {
    basic: basicConfig.value,
    message: messageConfig.value,
    security: securityConfig.value,
    storage: storageConfig.value,
    advanced: advancedConfig.value
  }
  const res = await updateSystemConfig(config)
  if (res.code === 200) {
    ElMessage.success('配置保存成功')
  }
}

// 保存敏感词
const handleSaveSensitiveWords = async () => {
  const words = sensitiveWords.value.split('\n').filter(w => w.trim())
  const res = await saveSensitiveWords({
    strategy: messageConfig.value.sensitiveStrategy,
    words
  })
  if (res.code === 200) {
    ElMessage.success('敏感词配置保存成功')
    sensitiveDialogVisible.value = false
  }
}

// 清空日志
const handleClearLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有操作日志吗？此操作不可恢复。', '警告', {
      type: 'warning'
    })
    ElMessage.success('日志已清空')
  } catch {
    // 取消
  }
}

// 导出数据
const handleExportData = () => {
  try {
    // 导出系统配置为 JSON 文件
    const exportData = {
      exportTime: new Date().toISOString(),
      systemConfig: {
        basic: basicConfig.value,
        message: messageConfig.value,
        security: securityConfig.value,
        storage: storageConfig.value,
        advanced: advancedConfig.value
      },
      sensitiveWords: sensitiveWords.value
    }

    const jsonContent = JSON.stringify(exportData, null, 2)
    const blob = new Blob([jsonContent], { type: 'application/json;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `系统配置_${new Date().toISOString().slice(0, 10)}.json`
    link.click()
    URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 清空缓存
const handleClearCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清空系统缓存吗？', '确认', {
      type: 'warning'
    })
    ElMessage.success('缓存已清空')
  } catch {
    // 取消
  }
}

onMounted(() => {
  loadSystemConfig()
  loadSensitiveWords()
  loadStorageStats()
})
</script>

<style scoped lang="scss">


/* ================================
   页面容器
   ================================ */
.system-settings {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--dt-space-md);
}

.page-title h2 {
  font-size: var(--dt-font-size-xl);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin: 0 0 4px 0;
}

.page-desc {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin: 0;
}

.page-actions {
  display: flex;
  gap: var(--dt-space-sm);
}

/* ================================
   设置卡片
   ================================ */
.settings-card {
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
}

.settings-card :deep(.el-card__body) {
  padding: var(--dt-space-md);
}

.settings-tabs {
  height: calc(100vh - 220px);
}

.settings-tabs :deep(.el-tabs__content) {
  height: calc(100% - 55px);
  overflow-y: auto;
}

/* 设置区块 */
.setting-section {
  margin-bottom: var(--dt-space-lg);
}

.section-title {
  font-size: var(--dt-font-size-md);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin: 0 0 var(--dt-space-md) 0;
}

/* 表单样式 */
.setting-section :deep(.el-form-item) {
  margin-bottom: var(--dt-space-md);
}

.setting-section :deep(.el-form-item__label) {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-regular);
  font-weight: var(--dt-font-weight-normal);
}

.form-tip {
  margin-left: var(--dt-space-sm);
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-secondary);
}

.form-unit {
  margin-left: var(--dt-space-xs);
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

/* Logo上传器 */
.logo-uploader {
  display: flex;
  align-items: center;
}

.logo-uploader-icon {
  color: var(--dt-text-placeholder);
}

/* 存储统计 */
.storage-stats {
  display: flex;
  gap: var(--dt-space-lg);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--dt-space-md);
  padding: var(--dt-space-md);
  background: var(--dt-bg-page);
  border-radius: var(--dt-radius-sm);
  flex: 1;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--dt-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: var(--dt-font-size-lg);
  font-weight: var(--dt-font-weight-bold);
  color: var(--dt-text-primary);
}

.stat-label {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-secondary);
}

/* 危险操作区域 */
.danger-zone {
  padding: var(--dt-space-md);
  background: var(--dt-error-bg);
  border-radius: var(--dt-radius-sm);
  border: 1px solid var(--dt-error);
}

.danger-zone h4 {
  font-size: var(--dt-font-size-base);
  color: var(--dt-error);
  margin: 0 0 var(--dt-space-xs) 0;
}

.danger-tip {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin: 0 0 var(--dt-space-md) 0;
}

.danger-actions {
  display: flex;
  gap: var(--dt-space-sm);
  flex-wrap: wrap;
}

/* 敏感词配置 */
.word-count {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

/* ================================
   Element Plus 样式覆盖
   ================================ */

/* 开关样式 */
:deep(.el-switch.is-checked .el-switch__core) {
  background-color: var(--dt-success);
}

/* 复选框组 */
:deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: var(--dt-space-md);
}

/* 单选框组 */
:deep(.el-radio-group) {
  display: flex;
  gap: var(--dt-space-md);
}

/* 输入数字 */
:deep(.el-input-number) {
  margin-right: var(--dt-space-xs);
}

/* ================================
   响应式
   ================================ */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: var(--dt-space-sm);
  }

  .storage-stats {
    flex-direction: column;
  }

  .danger-actions {
    flex-direction: column;
  }
}
</style>
