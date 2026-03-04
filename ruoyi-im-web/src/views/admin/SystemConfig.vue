<template>
  <div class="admin-page system-config-page">
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
          <div class="kpi-title">撤回时限(分钟)</div>
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
          <el-form-item label="消息撤回时限(分钟)">
            <el-input-number v-model="quickForm.recallMinutes" :min="0" :max="10080" :step="5" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :md="8">
          <el-form-item label="单文件上限(MB)">
            <el-input-number v-model="quickForm.maxFileMb" :min="1" :max="2048" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :md="8">
          <el-form-item label="会话过期时长(分钟)">
            <el-input-number v-model="quickForm.sessionExpireMinutes" :min="10" :max="43200" :step="10" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-card>

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
            <el-input v-model="row.editedValue" />
          </template>
        </el-table-column>
        <el-table-column label="原始值" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.rawValue }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :loading="row.saving" @click="saveSingleConfig(row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getSystemConfigs,
  getRecallTimeLimit,
  setRecallTimeLimit,
  updateSystemConfig
} from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const lastRefresh = ref(null)
const saveState = ref('未保存')
const recallLimit = ref(0)
const configEntries = ref([])

const quickForm = ref({
  recallMinutes: 0,
  maxFileMb: 100,
  sessionExpireMinutes: 120
})

const lastRefreshText = computed(() => {
  if (!lastRefresh.value) return '-'
  return new Date(lastRefresh.value).toLocaleString()
})

const saveStateText = computed(() => saveState.value)

const normalizeConfigEntries = (data) => {
  const entries = Object.keys(data || {}).map((key) => {
    const raw = data[key]
    return {
      key,
      rawValue: raw == null ? '' : String(raw),
      editedValue: raw == null ? '' : String(raw),
      saving: false
    }
  })
  return entries.sort((a, b) => a.key.localeCompare(b.key))
}

const tryFillQuickFormByKey = () => {
  const find = (keys, fallback) => {
    const hit = configEntries.value.find(item => keys.includes(item.key))
    if (!hit) return fallback
    const n = Number(hit.editedValue)
    return Number.isFinite(n) ? n : fallback
  }

  quickForm.value.recallMinutes = recallLimit.value || find(['message.recall.minutes', 'im.message.recall.minutes'], 0)
  quickForm.value.maxFileMb = find(['upload.maxFileMb', 'im.upload.maxFileMb'], 100)
  quickForm.value.sessionExpireMinutes = find(['security.session.expireMinutes', 'im.security.session.expireMinutes'], 120)
}

const loadConfigs = async () => {
  loading.value = true
  try {
    const [configRes, recallRes] = await Promise.all([getSystemConfigs(), getRecallTimeLimit()])
    if (configRes.code === 200) {
      configEntries.value = normalizeConfigEntries(configRes.data)
    }
    if (recallRes.code === 200) {
      recallLimit.value = Number(recallRes.data || 0)
    }
    tryFillQuickFormByKey()
    lastRefresh.value = Date.now()
  } catch (error) {
    ElMessage.error('加载系统配置失败')
  } finally {
    loading.value = false
  }
}

const saveSingleConfig = async (row) => {
  row.saving = true
  try {
    const payload = row.editedValue
    const res = await updateSystemConfig(row.key, payload)
    if (res.code === 200) {
      row.rawValue = row.editedValue
      saveState.value = '最近操作保存成功'
      ElMessage.success(`配置 ${row.key} 已保存`)
    }
  } catch (error) {
    ElMessage.error(`配置 ${row.key} 保存失败`)
  } finally {
    row.saving = false
  }
}

const updateKeyIfExists = async (candidateKeys, value) => {
  const row = configEntries.value.find(item => candidateKeys.includes(item.key))
  if (!row) return
  await updateSystemConfig(row.key, String(value))
  row.editedValue = String(value)
  row.rawValue = String(value)
}

const saveQuickConfigs = async () => {
  saving.value = true
  try {
    await setRecallTimeLimit(quickForm.value.recallMinutes)
    await updateKeyIfExists(['upload.maxFileMb', 'im.upload.maxFileMb'], quickForm.value.maxFileMb)
    await updateKeyIfExists(['security.session.expireMinutes', 'im.security.session.expireMinutes'], quickForm.value.sessionExpireMinutes)
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

loadConfigs()
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

@media (max-width: 768px) {
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
