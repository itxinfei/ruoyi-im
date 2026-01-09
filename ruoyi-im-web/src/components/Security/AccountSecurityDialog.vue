<template>
  <el-dialog
    v-model="dialogVisible"
    title="账户安全"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-tabs v-model="activeTab" class="security-tabs">
      <el-tab-pane label="登录设备" name="devices">
        <div v-loading="loading" class="devices-list">
          <div
            v-for="device in deviceList"
            :key="device.id"
            class="device-item"
            :class="{ current: device.isCurrent }"
          >
            <div class="device-icon">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="device-info">
              <div class="device-name">
                {{ device.deviceName }}
                <el-tag v-if="device.isCurrent" type="success" size="small">当前设备</el-tag>
              </div>
              <div class="device-detail">
                <span>{{ device.os }}</span>
                <span>{{ device.browser }}</span>
              </div>
              <div class="device-time">
                最后登录：{{ formatTime(device.lastLoginTime) }}
              </div>
            </div>
            <div v-if="!device.isCurrent" class="device-actions">
              <el-button type="danger" link @click="handleRemoveDevice(device)">
                移除
              </el-button>
            </div>
          </div>
          <el-empty v-if="deviceList.length === 0" description="暂无登录设备" :image-size="60" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="登录日志" name="logs">
        <div class="log-filters">
          <el-date-picker
            v-model="logDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="loadLoginLogs"
          />
          <el-select
            v-model="logType"
            placeholder="登录类型"
            clearable
            style="width: 150px"
            @change="loadLoginLogs"
          >
            <el-option label="成功" value="success" />
            <el-option label="失败" value="fail" />
          </el-select>
        </div>
        <div v-loading="loading" class="log-list">
          <div v-for="log in logList" :key="log.id" class="log-item">
            <div class="log-icon">
              <el-icon :class="log.status === 'success' ? 'success' : 'fail'">
                <component :is="log.status === 'success' ? CircleCheck : CircleClose" />
              </el-icon>
            </div>
            <div class="log-info">
              <div class="log-header">
                <span class="log-status">{{ log.status === 'success' ? '登录成功' : '登录失败' }}</span>
                <span class="log-time">{{ formatTime(log.createTime) }}</span>
              </div>
              <div class="log-detail">
                <span>IP：{{ log.ip }}</span>
                <span>地点：{{ log.location }}</span>
              </div>
              <div v-if="log.status === 'fail'" class="log-reason">
                失败原因：{{ log.failReason }}
              </div>
            </div>
          </div>
          <el-empty v-if="logList.length === 0" description="暂无登录日志" :image-size="60" />
        </div>
        <div v-if="logTotal > 0" class="log-pagination">
          <el-pagination
            v-model:current-page="logPageNum"
            v-model:page-size="logPageSize"
            :total="logTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadLoginLogs"
            @current-change="loadLoginLogs"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="两步验证" name="2fa">
        <div class="two-factor">
          <div v-if="!twoFactorEnabled" class="two-factor-disabled">
            <el-result icon="warning" title="两步验证未开启">
              <template #sub-title>
                开启两步验证后，登录时需要输入验证码，提高账号安全性
              </template>
              <template #extra>
                <el-button type="primary" @click="handleEnableTwoFactor">立即开启</el-button>
              </template>
            </el-result>
          </div>
          <div v-else class="two-factor-enabled">
            <el-result icon="success" title="两步验证已开启">
              <template #sub-title>
                您的账号已开启两步验证保护
              </template>
              <template #extra>
                <el-button @click="handleDisableTwoFactor">关闭验证</el-button>
              </template>
            </el-result>
            
            <el-divider />
            
            <div class="two-factor-methods">
              <h4>验证方式</h4>
              <div class="method-list">
                <div
                  v-for="method in twoFactorMethods"
                  :key="method.type"
                  class="method-item"
                  :class="{ active: method.enabled }"
                >
                  <div class="method-icon">
                    <el-icon>
                      <component :is="method.icon" />
                    </el-icon>
                  </div>
                  <div class="method-info">
                    <div class="method-name">{{ method.name }}</div>
                    <div class="method-desc">{{ method.description }}</div>
                  </div>
                  <el-switch
                    v-model="method.enabled"
                    @change="handleMethodChange(method)"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Monitor,
  CircleCheck,
  CircleClose,
  Iphone,
  Message,
  Key,
} from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'

const props = defineProps({
  visible: Boolean,
})

const emit = defineEmits(['update:visible'])

const dialogVisible = ref(false)
const loading = ref(false)
const activeTab = ref('devices')

const deviceList = ref([])
const logList = ref([])
const logDateRange = ref([])
const logType = ref('')
const logPageNum = ref(1)
const logPageSize = ref(10)
const logTotal = ref(0)

const twoFactorEnabled = ref(false)
const twoFactorMethods = reactive([
  {
    type: 'sms',
    name: '短信验证',
    description: '通过手机短信接收验证码',
    icon: Iphone,
    enabled: true,
  },
  {
    type: 'email',
    name: '邮箱验证',
    description: '通过邮箱接收验证码',
    icon: Message,
    enabled: false,
  },
  {
    type: 'totp',
    name: '身份验证器',
    description: '使用Google Authenticator等应用',
    icon: Key,
    enabled: false,
  },
])

const loadDevices = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/im/user/devices', {
      headers: {
        Authorization: 'Bearer ' + getToken(),
      },
    })
    const data = await response.json()
    if (data.code === 200) {
      deviceList.value = data.data || []
    }
  } catch (error) {
    console.error('加载登录设备失败:', error)
  } finally {
    loading.value = false
  }
}

const loadLoginLogs = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams({
      pageNum: logPageNum.value,
      pageSize: logPageSize.value,
    })
    
    if (logDateRange.value && logDateRange.value.length === 2) {
      params.append('startDate', logDateRange.value[0])
      params.append('endDate', logDateRange.value[1])
    }
    
    if (logType.value) {
      params.append('status', logType.value)
    }
    
    const response = await fetch(`/api/im/user/login-logs?${params}`, {
      headers: {
        Authorization: 'Bearer ' + getToken(),
      },
    })
    const data = await response.json()
    if (data.code === 200) {
      logList.value = data.data?.list || []
      logTotal.value = data.data?.total || 0
    }
  } catch (error) {
    console.error('加载登录日志失败:', error)
  } finally {
    loading.value = false
  }
}

const loadTwoFactorStatus = async () => {
  try {
    const response = await fetch('/api/im/user/two-factor/status', {
      headers: {
        Authorization: 'Bearer ' + getToken(),
      },
    })
    const data = await response.json()
    if (data.code === 200) {
      twoFactorEnabled.value = data.data?.enabled || false
      if (data.data?.methods) {
        twoFactorMethods.forEach(method => {
          const found = data.data.methods.find(m => m.type === method.type)
          if (found) {
            method.enabled = found.enabled
          }
        })
      }
    }
  } catch (error) {
    console.error('加载两步验证状态失败:', error)
  }
}

const handleRemoveDevice = async device => {
  try {
    await ElMessageBox.confirm(
      `确定要移除设备"${device.deviceName}"吗？移除后该设备将无法登录您的账号。`,
      '确认移除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    const response = await fetch(`/api/im/user/devices/${device.id}`, {
      method: 'DELETE',
      headers: {
        Authorization: 'Bearer ' + getToken(),
      },
    })
    const data = await response.json()
    
    if (data.code === 200) {
      ElMessage.success('设备已移除')
      loadDevices()
    } else {
      ElMessage.error(data.msg || '移除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除设备失败:', error)
      ElMessage.error('移除失败')
    }
  }
}

const handleEnableTwoFactor = async () => {
  try {
    const response = await fetch('/api/im/user/two-factor/enable', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + getToken(),
      },
      body: JSON.stringify({
        methods: ['sms'],
      }),
    })
    const data = await response.json()
    
    if (data.code === 200) {
      ElMessage.success('两步验证已开启')
      twoFactorEnabled.value = true
    } else {
      ElMessage.error(data.msg || '开启失败')
    }
  } catch (error) {
    console.error('开启两步验证失败:', error)
    ElMessage.error('开启失败')
  }
}

const handleDisableTwoFactor = async () => {
  try {
    await ElMessageBox.confirm(
      '关闭两步验证后，您的账号安全性将降低，确定要关闭吗？',
      '确认关闭',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    const response = await fetch('/api/im/user/two-factor/disable', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + getToken(),
      },
    })
    const data = await response.json()
    
    if (data.code === 200) {
      ElMessage.success('两步验证已关闭')
      twoFactorEnabled.value = false
    } else {
      ElMessage.error(data.msg || '关闭失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('关闭两步验证失败:', error)
      ElMessage.error('关闭失败')
    }
  }
}

const handleMethodChange = async method => {
  try {
    const response = await fetch('/api/im/user/two-factor/method', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + getToken(),
      },
      body: JSON.stringify({
        type: method.type,
        enabled: method.enabled,
      }),
    })
    const data = await response.json()
    
    if (data.code !== 200) {
      ElMessage.error(data.msg || '设置失败')
      method.enabled = !method.enabled
    }
  } catch (error) {
    console.error('设置验证方式失败:', error)
    ElMessage.error('设置失败')
    method.enabled = !method.enabled
  }
}

const formatTime = time => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const handleClose = () => {
  dialogVisible.value = false
  emit('update:visible', false)
}

watch(
  () => props.visible,
  visible => {
    dialogVisible.value = visible
    if (visible) {
      loadDevices()
      loadLoginLogs()
      loadTwoFactorStatus()
    }
  }
)

watch(dialogVisible, val => {
  if (!val) {
    emit('update:visible', false)
  }
})

watch(activeTab, tab => {
  if (tab === 'devices') {
    loadDevices()
  } else if (tab === 'logs') {
    loadLoginLogs()
  } else if (tab === '2fa') {
    loadTwoFactorStatus()
  }
})
</script>

<style lang="scss" scoped>
.security-tabs {
  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }
}

.devices-list {
  max-height: 500px;
  overflow-y: auto;
}

.device-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.2s;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  }

  &.current {
    background: #f0f9ff;
    border-color: #409eff;
  }
}

.device-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: #606266;
}

.device-info {
  flex: 1;
}

.device-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.device-detail {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;

  span {
    margin-right: 12px;
  }
}

.device-time {
  font-size: 12px;
  color: #c0c4cc;
}

.log-filters {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.log-list {
  max-height: 400px;
  overflow-y: auto;
}

.log-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border-bottom: 1px solid #ebeef5;

  &:last-child {
    border-bottom: none;
  }
}

.log-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;

  .el-icon {
    font-size: 18px;
  }

  &.success {
    background: #f0f9ff;
    color: #67c23a;
  }

  &.fail {
    background: #fef0f0;
    color: #f56c6c;
  }
}

.log-info {
  flex: 1;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.log-status {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.log-time {
  font-size: 12px;
  color: #909399;
}

.log-detail {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;

  span {
    margin-right: 16px;
  }
}

.log-reason {
  font-size: 12px;
  color: #f56c6c;
}

.log-pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.two-factor {
  .two-factor-disabled,
  .two-factor-enabled {
    padding: 20px 0;
  }
}

.two-factor-methods {
  h4 {
    margin: 0 0 16px;
    font-size: 14px;
    color: #303133;
  }
}

.method-list {
  max-height: 400px;
  overflow-y: auto;
}

.method-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.2s;

  &:hover {
    border-color: #409eff;
  }

  &.active {
    background: #f0f9ff;
    border-color: #409eff;
  }
}

.method-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 20px;
  color: #606266;
}

.method-info {
  flex: 1;
}

.method-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.method-desc {
  font-size: 12px;
  color: #909399;
}
</style>
