<template>
  <div class="admin-page system-config-page">
    <!-- KPI 卡片 -->
    <el-row :gutter="16" class="kpi-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="kpi-card" shadow="never">
          <div class="kpi-title">配置总数</div>
          <div class="kpi-value">{{ configEntries.length }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="kpi-card" shadow="never">
          <div class="kpi-title">上次刷新</div>
          <div class="kpi-value small">{{ lastRefreshText }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="kpi-card" shadow="never">
          <div class="kpi-title">撤回时限 (分钟)</div>
          <div class="kpi-value">{{ recallLimit }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="kpi-card" shadow="never">
          <div class="kpi-title">变更状态</div>
          <div class="kpi-value small">{{ saveStateText }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷参数 -->
    <el-card class="panel" shadow="never">
      <template #header>
        <div class="panel-header">
          <div>
            <h3>快捷参数</h3>
            <p>高频参数可直接编辑并保存</p>
          </div>
          <el-space>
            <el-button @click="loadConfigs">刷新</el-button>
            <el-button type="primary" :loading="saving" @click="saveQuickConfigs">保存快捷参数</el-button>
          </el-space>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :xs="24" :md="8">
          <el-form-item label="消息撤回时限 (分钟)">
            <el-input-number v-model="quickForm.recallMinutes" :min="0" :max="10080" :step="5" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :md="8">
          <el-form-item label="单文件上限 (MB)">
            <el-input-number v-model="quickForm.maxFileMb" :min="1" :max="2048" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :md="8">
          <el-form-item label="会话过期时长 (分钟)">
            <el-input-number v-model="quickForm.sessionExpireMinutes" :min="10" :max="43200" :step="10" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-card>

    <!-- 配置分组：消息策略 -->
    <el-card class="panel" shadow="never">
      <template #header>
        <div class="panel-header">
          <h3>消息策略</h3>
          <p>消息相关配置</p>
        </div>
      </template>

      <el-form :model="configForm" label-width="200px">
        <el-form-item label="最大消息长度">
          <el-input-number v-model="configForm.maxMessageLength" :min="100" :max="10000" :step="100" style="width: 200px" />
          <span class="form-tip">单条消息允许的最大字符数</span>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 配置分组：上传策略 -->
    <el-card class="panel" shadow="never">
      <template #header>
        <div class="panel-header">
          <h3>上传策略</h3>
          <p>文件上传相关配置</p>
        </div>
      </template>

      <el-form :model="configForm" label-width="200px">
        <el-form-item label="单文件上限 (MB)">
          <el-input-number v-model="configForm.uploadMaxFileMb" :min="1" :max="2048" :step="10" style="width: 200px" />
          <span class="form-tip">单个文件允许的最大大小</span>
        </el-form-item>
        <el-form-item label="会话过期时长 (分钟)">
          <el-input-number v-model="configForm.sessionExpireMinutes" :min="10" :max="43200" :step="10" style="width: 200px" />
          <span class="form-tip">用户会话的过期时间</span>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 配置分组：安全策略（仅 SUPER_ADMIN 可修改） -->
    <el-card class="panel" shadow="never">
      <template #header>
        <div class="panel-header">
          <h3>安全策略</h3>
          <p>
            <el-tag type="warning" size="small">仅 SUPER_ADMIN 可修改</el-tag>
          </p>
        </div>
      </template>

      <el-form :model="configForm" label-width="200px">
        <el-form-item label="登录失败阈值">
          <el-input-number v-model="configForm.loginFailureThreshold" :min="1" :max="20" style="width: 200px" />
          <span class="form-tip">连续登录失败触发锁定的次数</span>
        </el-form-item>
        <el-form-item label="密码复杂度级别">
          <el-select v-model="configForm.passwordComplexityLevel" style="width: 200px">
            <el-option label="1 - 简单（6 位以上）" :value="1" />
            <el-option label="2 - 中等（8 位以上，含字母数字）" :value="2" />
            <el-option label="3 - 复杂（10 位以上，含大小写/数字/特殊字符）" :value="3" />
          </el-select>
          <span class="form-tip">密码强度要求级别</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingSecurity" @click="saveSecurityConfigs">保存安全策略</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 配置总览表格 -->
    <el-card class="panel" shadow="never">
      <template #header>
        <div class="panel-header">
          <div>
            <h3>配置总览</h3>
            <p>支持单项更新，适合低频参数维护</p>
          </div>
        </div>
      </template>

      <el-table :data="configEntries" v-loading="loading" border>
        <el-table-column prop="key" label="配置键" min-width="260" show-overflow-tooltip />
        <el-table-column label="当前值" min-width="300">
          <template #default="{ row }">
            <el-input v-model="row.editedValue" :disabled="row.superAdminOnly && !isSuperAdmin" />
          </template>
        </el-table-column>
        <el-table-column label="原始值" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.rawValue }}</template>
        </el-table-column>
        <el-table-column label="权限" min-width="100">
          <template #default="{ row }">
            <el-tag v-if="row.superAdminOnly" type="warning" size="small">SUPER_ADMIN</el-tag>
            <el-tag v-else type="info" size="small">ADMIN</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :loading="row.saving"
              :disabled="row.superAdminOnly && !isSuperAdmin"
              @click="saveSingleConfig(row)"
            >
              保存
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getSystemConfigs,
  getRecallTimeLimit,
  setRecallTimeLimit,
  updateSystemConfig
} from '@/api/admin'
import tokenManager from '@/utils/tokenManager'

const store = useStore()

// 配置键常量
const CONFIG_KEYS = {
  RECALL_TIME_LIMIT: 'message.recall.time.limit',
  MAX_MESSAGE_LENGTH: 'message.max.length',
  UPLOAD_MAX_FILE_MB: 'upload.maxFileMb',
  SESSION_EXPIRE_MINUTES: 'security.session.expireMinutes',
  LOGIN_FAILURE_THRESHOLD: 'security.login.failureThreshold',
  PASSWORD_COMPLEXITY_LEVEL: 'security.password.complexityLevel'
}

const loading = ref(false)
const saving = ref(false)
const savingSecurity = ref(false)
const lastRefresh = ref(null)
const saveState = ref('未保存')
const recallLimit = ref(0)
const configEntries = ref([])
const isSuperAdmin = ref(false)

// 快捷参数表单
const quickForm = ref({
  recallMinutes: 0,
  maxFileMb: 100,
  sessionExpireMinutes: 120
})

// 配置表单
const configForm = ref({
  maxMessageLength: 2000,
  uploadMaxFileMb: 100,
  sessionExpireMinutes: 120,
  loginFailureThreshold: 5,
  passwordComplexityLevel: 2
})

const lastRefreshText = computed(() => {
  if (!lastRefresh.value) return '-'
  return new Date(lastRefresh.value).toLocaleString()
})

const saveStateText = computed(() => saveState.value)

/**
 * 检查当前用户是否为 SUPER_ADMIN
 */
const checkUserRole = () => {
  try {
    const userRole = tokenManager.getRole()
    isSuperAdmin.value = userRole === 'SUPER_ADMIN'
  } catch (e) {
    isSuperAdmin.value = false
  }
}

/**
 * 规范化配置条目
 */
const normalizeConfigEntries = (data) => {
  const superAdminOnlyKeys = [
    CONFIG_KEYS.LOGIN_FAILURE_THRESHOLD,
    CONFIG_KEYS.PASSWORD_COMPLEXITY_LEVEL
  ]

  const entries = Object.keys(data || {}).map((key) => {
    const raw = data[key]
    return {
      key,
      rawValue: raw == null ? '' : String(raw),
      editedValue: raw == null ? '' : String(raw),
      superAdminOnly: superAdminOnlyKeys.includes(key),
      saving: false
    }
  })
  return entries.sort((a, b) => a.key.localeCompare(b.key))
}

/**
 * 从配置条目填充快捷表单
 */
const fillQuickFormFromEntries = () => {
  const findValue = (keys, fallback) => {
    const hit = configEntries.value.find(item => keys.includes(item.key))
    if (!hit) return fallback
    const n = Number(hit.editedValue)
    return Number.isFinite(n) ? n : fallback
  }

  quickForm.value.recallMinutes = recallLimit.value || findValue([CONFIG_KEYS.RECALL_TIME_LIMIT], 0)
  quickForm.value.maxFileMb = findValue([CONFIG_KEYS.UPLOAD_MAX_FILE_MB], 100)
  quickForm.value.sessionExpireMinutes = findValue([CONFIG_KEYS.SESSION_EXPIRE_MINUTES], 120)
}

/**
 * 从配置条目填充配置表单
 */
const fillConfigFormFromEntries = () => {
  const findValue = (keys, fallback) => {
    const hit = configEntries.value.find(item => keys.includes(item.key))
    if (!hit) return fallback
    const n = Number(hit.editedValue)
    return Number.isFinite(n) ? n : fallback
  }

  configForm.value.maxMessageLength = findValue([CONFIG_KEYS.MAX_MESSAGE_LENGTH], 2000)
  configForm.value.uploadMaxFileMb = findValue([CONFIG_KEYS.UPLOAD_MAX_FILE_MB], 100)
  configForm.value.sessionExpireMinutes = findValue([CONFIG_KEYS.SESSION_EXPIRE_MINUTES], 120)
  configForm.value.loginFailureThreshold = findValue([CONFIG_KEYS.LOGIN_FAILURE_THRESHOLD], 5)
  configForm.value.passwordComplexityLevel = findValue([CONFIG_KEYS.PASSWORD_COMPLEXITY_LEVEL], 2)
}

/**
 * 加载所有配置
 */
const loadConfigs = async () => {
  loading.value = true
  try {
    const [configRes, recallRes] = await Promise.all([getSystemConfigs(), getRecallTimeLimit()])
    if (configRes.code === 200) {
      configEntries.value = normalizeConfigEntries(configRes.data)
      fillQuickFormFromEntries()
      fillConfigFormFromEntries()
    }
    if (recallRes.code === 200) {
      recallLimit.value = Number(recallRes.data || 0)
    }
    lastRefresh.value = Date.now()
  } catch (error) {
    ElMessage.error('加载系统配置失败')
  } finally {
    loading.value = false
  }
}

/**
 * 保存单个配置
 */
const saveSingleConfig = async (row) => {
  row.saving = true
  try {
    const payload = row.editedValue
    const res = await updateSystemConfig(row.key, payload)
    if (res.code === 200) {
      row.rawValue = row.editedValue
      saveState.value = '最近操作保存成功'
      ElMessage.success(`配置 ${row.key} 已保存`)
    } else {
      ElMessage.error(res.msg || `配置 ${row.key} 保存失败`)
    }
  } catch (error) {
    ElMessage.error(`配置 ${row.key} 保存失败`)
  } finally {
    row.saving = false
  }
}

/**
 * 更新配置（如果键存在）
 */
const updateKeyIfExists = async (candidateKeys, value) => {
  const row = configEntries.value.find(item => candidateKeys.includes(item.key))
  if (!row) return
  await updateSystemConfig(row.key, String(value))
  row.editedValue = String(value)
  row.rawValue = String(value)
}

/**
 * 保存快捷参数
 */
const saveQuickConfigs = async () => {
  saving.value = true
  try {
    // 保存撤回时间限制
    await setRecallTimeLimit(quickForm.value.recallMinutes)
    // 保存其他配置
    await updateKeyIfExists([CONFIG_KEYS.UPLOAD_MAX_FILE_MB], quickForm.value.maxFileMb)
    await updateKeyIfExists([CONFIG_KEYS.SESSION_EXPIRE_MINUTES], quickForm.value.sessionExpireMinutes)

    recallLimit.value = quickForm.value.recallMinutes
    saveState.value = '最近操作保存成功'
    ElMessage.success('快捷参数保存成功')
  } catch (error) {
    saveState.value = '保存失败'
    ElMessage.error('快捷参数保存失败')
  } finally {
    saving.value = false
  }
}

/**
 * 保存安全策略配置
 */
const saveSecurityConfigs = async () => {
  if (!isSuperAdmin.value) {
    ElMessage.warning('仅 SUPER_ADMIN 可修改安全策略')
    return
  }

  savingSecurity.value = true
  try {
    await updateSystemConfig(CONFIG_KEYS.LOGIN_FAILURE_THRESHOLD, configForm.value.loginFailureThreshold)
    await updateSystemConfig(CONFIG_KEYS.PASSWORD_COMPLEXITY_LEVEL, configForm.value.passwordComplexityLevel)

    // 更新表格中的值
    const loginRow = configEntries.value.find(item => item.key === CONFIG_KEYS.LOGIN_FAILURE_THRESHOLD)
    const pwdRow = configEntries.value.find(item => item.key === CONFIG_KEYS.PASSWORD_COMPLEXITY_LEVEL)
    if (loginRow) {
      loginRow.rawValue = String(configForm.value.loginFailureThreshold)
      loginRow.editedValue = String(configForm.value.loginFailureThreshold)
    }
    if (pwdRow) {
      pwdRow.rawValue = String(configForm.value.passwordComplexityLevel)
      pwdRow.editedValue = String(configForm.value.passwordComplexityLevel)
    }

    saveState.value = '最近操作保存成功'
    ElMessage.success('安全策略保存成功')
  } catch (error) {
    ElMessage.error('安全策略保存失败')
  } finally {
    savingSecurity.value = false
  }
}

// 初始化
onMounted(() => {
  checkUserRole()
  loadConfigs()
})
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.kpi-row {
  margin-bottom: 0;
}

.kpi-card {
  border-radius: 12px;
  border: 1px solid #e6ebf3;
}

.kpi-title {
  color: #64748b;
  font-size: 12px;
}

.kpi-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.kpi-value.small {
  font-size: 15px;
  font-weight: 600;
}

.panel {
  border-radius: 12px;
  border: 1px solid #e6ebf3;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #0f172a;
}

.panel-header p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}

.form-tip {
  margin-left: 12px;
  color: #94a3b8;
  font-size: 12px;
}
</style>
