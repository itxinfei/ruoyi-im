<template>
  <div class="system-monitor">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <h2>系统监控</h2>
        <p class="page-desc">实时监控系统运行状态和性能指标</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Refresh" @click="refreshAll">刷新数据</el-button>
        <el-button :icon="Setting" @click="settingsDialogVisible = true">监控设置</el-button>
      </div>
    </div>

    <!-- 系统状态概览 -->
    <el-row :gutter="16" class="status-overview">
      <el-col :span="6">
        <el-card class="status-card" :class="{ 'status--error': systemStatus.overall !== 'healthy' }">
          <div class="status-icon">
            <el-icon :size="32">
              <Monitor v-if="systemStatus.overall === 'healthy'" />
              <WarningFilled v-else />
            </el-icon>
          </div>
          <div class="status-info">
            <div class="status-label">系统状态</div>
            <div class="status-value">{{ systemStatus.overall === 'healthy' ? '运行正常' : '异常' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card">
          <div class="status-mini-icon">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="status-mini-info">
            <div class="status-mini-label">运行时间</div>
            <div class="status-mini-value">{{ systemStatus.uptime }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card">
          <div class="status-mini-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="status-mini-info">
            <div class="status-mini-label">在线用户</div>
            <div class="status-mini-value">{{ systemStatus.onlineUsers }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card">
          <div class="status-mini-icon">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="status-mini-info">
            <div class="status-mini-label">QPS</div>
            <div class="status-mini-value">{{ systemStatus.qps }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 监控图表 -->
    <el-row :gutter="16" class="charts-row">
      <!-- CPU使用率 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-title">
              <span>CPU使用率</span>
              <span class="chart-value">{{ cpuUsage }}%</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="gauge-wrapper">
              <svg class="gauge" viewBox="0 0 200 100">
                <!-- 背景弧 -->
                <path
                  d="M 20 90 A 80 80 0 0 1 180 90"
                  fill="none"
                  stroke="var(--dt-bg-page)"
                  stroke-width="12"
                />
                <!-- 进度弧 -->
                <path
                  d="M 20 90 A 80 80 0 0 1 180 90"
                  fill="none"
                  :stroke="getCpuColor()"
                  stroke-width="12"
                  :stroke-dasharray="cpuCircumference + ' ' + (251 - cpuCircumference)"
                  stroke-dashoffset="0"
                  class="gauge-progress"
                />
                <!-- 刻度 -->
                <text x="100" y="95" text-anchor="middle" class="gauge-label">{{ cpuUsage }}%</text>
              </svg>
            </div>
            <div class="gauge-legend">
              <div class="legend-item">
                <span class="legend-dot" style="background: var(--dt-success)"></span>
                <span>0-50%</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot" style="background: var(--dt-warning)"></span>
                <span>50-80%</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot" style="background: var(--dt-error)"></span>
                <span>80-100%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 内存使用率 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-title">
              <span>内存使用率</span>
              <span class="chart-value">{{ memoryUsage }}%</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="memory-bars">
              <div class="memory-bar">
                <span class="memory-label">已使用</span>
                <div class="bar-track">
                  <div class="bar-fill bar-fill--used" :style="{ width: memoryUsage + '%' }"></div>
                </div>
                <span class="memory-value">{{ memoryUsage }}%</span>
              </div>
              <div class="memory-bar">
                <span class="memory-label">缓存</span>
                <div class="bar-track">
                  <div class="bar-fill bar-fill--cache" :style="{ width: memoryCache + '%' }"></div>
                </div>
                <span class="memory-value">{{ memoryCache }}%</span>
              </div>
              <div class="memory-bar">
                <span class="memory-label">可用</span>
                <div class="bar-track">
                  <div class="bar-fill bar-fill--free" :style="{ width: memoryFree + '%' }"></div>
                </div>
                <span class="memory-value">{{ memoryFree }}%</span>
              </div>
            </div>
            <div class="memory-detail">
              <div class="detail-item">
                <span class="detail-label">总内存</span>
                <span class="detail-value">{{ systemStatus.totalMemory }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">已使用</span>
                <span class="detail-value">{{ systemStatus.usedMemory }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">JVM堆</span>
                <span class="detail-value">{{ systemStatus.jvmMemory }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 磁盘使用率 -->
    <el-row :gutter="16" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-title">磁盘使用率</div>
          </template>
          <div class="chart-container">
            <div class="disk-list">
              <div v-for="disk in diskUsage" :key="disk.path" class="disk-item">
                <div class="disk-info">
                  <div class="disk-icon">
                    <el-icon><FolderOpened /></el-icon>
                  </div>
                  <div class="disk-details">
                    <div class="disk-name">{{ disk.mount }}</div>
                    <div class="disk-path">{{ disk.path }}</div>
                  </div>
                </div>
                <div class="disk-usage">
                  <div class="disk-bar">
                    <div class="bar-fill" :style="{ width: disk.usage + '%', background: getDiskColor(disk.usage) }"></div>
                  </div>
                  <span class="disk-value">{{ disk.usage }}%</span>
                </div>
                <div class="disk-size">
                  <span>{{ formatFileSize(disk.used) }}&nbsp;/&nbsp;{{ formatFileSize(disk.total) }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 网络流量 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-title">网络流量</div>
          </template>
          <div class="chart-container">
            <div class="network-stats">
              <div class="network-item network-item--in">
                <div class="network-icon">
                  <el-icon><Download /></el-icon>
                </div>
                <div class="network-info">
                  <div class="network-label">下载速率</div>
                  <div class="network-value">{{ networkStats.inSpeed }}</div>
                </div>
              </div>
              <div class="network-item network-item--out">
                <div class="network-icon">
                  <el-icon><Upload /></el-icon>
                </div>
                <div class="network-info">
                  <div class="network-label">上传速率</div>
                  <div class="network-value">{{ networkStats.outSpeed }}</div>
                </div>
              </div>
            </div>
            <div class="network-detail">
              <div class="detail-item">
                <span class="detail-label">总接收</span>
                <span class="detail-value">{{ formatBytes(networkStats.totalIn) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">总发送</span>
                <span class="detail-value">{{ formatBytes(networkStats.totalOut) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 请求统计 -->
    <el-row :gutter="16" class="charts-row">
      <!-- 今日请求趋势 -->
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-title">
              <span>今日请求趋势</span>
              <el-radio-group v-model="trendPeriod" size="small">
                <el-radio-button label="1h">1小时</el-radio-button>
                <el-radio-button label="6h">6小时</el-radio-button>
                <el-radio-button label="24h">24小时</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <div class="trend-chart">
              <svg class="trend-svg" viewBox="0 0 600 150">
                <!-- 网格线 -->
                <line
                  v-for="i in 6"
                  :key="i"
                  x1="40"
                  :y1="20 + i * 20"
                  x2="600"
                  :y2="20 + i * 20"
                  stroke="var(--dt-border-lighter)"
                  stroke-dasharray="4 4"
                />
                <!-- 趋势线 -->
                <polyline
                  :points="trendPoints"
                  fill="none"
                  stroke="var(--dt-primary)"
                  stroke-width="2"
                  vector-effect="non-scaling-stroke"
                />
                <!-- 数据点 -->
                <circle
                  v-for="(point, index) in trendPoints"
                  :key="index"
                  :cx="point.x"
                  :cy="point.y"
                  r="3"
                  fill="var(--dt-primary)"
                />
              </svg>
            </div>
            <div class="trend-legend">
              <div class="legend-item">
                <span class="legend-dot"></span>
                <span>请求数</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot" style="background: var(--dt-success)"></span>
                <span>成功</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot" style="background: var(--dt-error)"></span>
                <span>失败</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 请求统计 -->
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-title">请求统计</div>
          </template>
          <div class="chart-container">
            <div class="request-stats">
              <div class="stat-row">
                <span class="stat-row-label">今日请求</span>
                <span class="stat-row-value">{{ requestStats.todayCount }}</span>
              </div>
              <div class="stat-row">
                <span class="stat-row-label">平均响应</span>
                <span class="stat-row-value">{{ requestStats.avgResponse }}ms</span>
              </div>
              <div class="stat-row">
                <span class="stat-row-label">成功率</span>
                <span class="stat-row-value" :class="getSuccessClass(requestStats.successRate)">
                  {{ requestStats.successRate }}%
                </span>
              </div>
              <div class="stat-row">
                <span class="stat-row-label">错误数</span>
                <span class="stat-row-value stat-row-value--error">{{ requestStats.errorCount }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 活跃用户列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <span>在线用户列表 ({{ onlineUsers.length }})</span>
          <el-input
            v-model="userSearch"
            placeholder="搜索用户"
            :prefix-icon="Search"
            clearable
            style="width: 200px"
          />
        </div>
      </template>
      <el-table :data="filteredUsers" stripe v-loading="loading">
        <el-table-column label="用户" width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :src="row.avatar">{{ row.nickname?.charAt(0) || row.username?.charAt(0) }}</el-avatar>
              <div class="user-info">
                <div class="user-name">{{ row.nickname || row.username }}</div>
                <div class="user-account">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="loginIp" label="登录IP" width="140" />
        <el-table-column prop="sessionId" label="会话ID" width="200" show-overflow-tooltip />
        <el-table-column prop="lastActive" label="最后活跃时间" width="120" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="danger" size="small" :icon="Delete" @click="handleKickUser(row)">
              踢出
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && onlineUsers.length === 0" description="暂无在线用户" />
    </el-card>

    <!-- 监控设置对话框 -->
    <el-dialog
      v-model="settingsDialogVisible"
      title="监控设置"
      width="500px"
    >
      <el-form label-width="120px">
        <el-form-item label="刷新间隔">
          <el-select v-model="monitorSettings.refreshInterval" style="width: 150px">
            <el-option label="5秒" :value="5000" />
            <el-option label="10秒" :value="10000" />
            <el-option label="30秒" :value="30000" />
            <el-option label="1分钟" :value="60000" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警阈值">
          <div class="threshold-item">
            <span class="threshold-label">CPU使用率</span>
            <el-input-number v-model="monitorSettings.cpuThreshold" :max="100" controls-position="right" />
            <span class="threshold-unit">%</span>
          </div>
          <div class="threshold-item">
            <span class="threshold-label">内存使用率</span>
            <el-input-number v-model="monitorSettings.memoryThreshold" :max="100" controls-position="right" />
            <span class="threshold-unit">%</span>
          </div>
          <div class="threshold-item">
            <span class="threshold-label">磁盘使用率</span>
            <el-input-number v-model="monitorSettings.diskThreshold" :max="100" controls-position="right" />
            <span class="threshold-unit">%</span>
          </div>
        </el-form-item>
        <el-form-item label="告警通知">
          <el-checkbox-group v-model="monitorSettings.alertTypes">
            <el-checkbox label="system">系统通知</el-checkbox>
            <el-checkbox label="email">邮件通知</el-checkbox>
            <el-checkbox label="webhook">Webhook</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settingsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveSettings">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh,
  Setting,
  Monitor,
  WarningFilled,
  Clock,
  User,
  Connection,
  Download,
  Upload,
  FolderOpened,
  Search,
  Delete
} from '@element-plus/icons-vue'
import { formatFileSize } from '@/utils/format'
import {
  getSystemMonitor,
  getOnlineUsers,
  getSystemPerformance,
  kickUser
} from '@/api/admin'

// 系统状态
const systemStatus = ref({
  overall: 'healthy',
  uptime: '-',
  onlineUsers: 0,
  qps: 0,
  totalMemory: '-',
  usedMemory: '-',
  jvmMemory: '-'
})

// 在线用户列表
const onlineUsers = ref([])
const userSearch = ref('')

const filteredUsers = computed(() => {
  if (!userSearch.value) return onlineUsers.value
  return onlineUsers.value.filter(u =>
    (u.username && u.username.includes(userSearch.value)) ||
    (u.nickname && u.nickname.includes(userSearch.value))
  )
})

// CPU使用率
const cpuUsage = ref(35)
const cpuCircumference = computed(() => {
  return (cpuUsage.value / 100) * 251
})

// 内存使用率
const memoryUsage = ref(53)
const memoryCache = ref(12)
const memoryFree = computed(() => 100 - memoryUsage.value - memoryCache.value)

// 磁盘使用率（从API获取）
const diskUsage = ref([])

// 网络流量（从API获取）
const networkStats = ref({
  inSpeed: '-',
  outSpeed: '-',
  totalIn: 0,
  totalOut: 0
})

// 请求统计（从API获取）
const requestStats = ref({
  todayCount: 0,
  avgResponse: 0,
  successRate: 100,
  errorCount: 0
})

// 趋势图数据（从API获取）
const trendPeriod = ref('24h')
const trendPoints = ref([])


// 对话框状态
const settingsDialogVisible = ref(false)

// 监控设置
const monitorSettings = ref({
  refreshInterval: 10000,
  cpuThreshold: 80,
  memoryThreshold: 85,
  diskThreshold: 90,
  alertTypes: ['system']
})

let refreshTimer = null
const loading = ref(false)

// 刷新所有数据
const refreshAll = async () => {
  loading.value = true
  try {
    // 获取系统监控数据
    const monitorRes = await getSystemMonitor()
    if (monitorRes.code === 200 && monitorRes.data) {
      const data = monitorRes.data

      // 更新在线用户数
      if (data.onlineUserCount !== undefined) {
        systemStatus.value.onlineUsers = data.onlineUserCount
      }

      // 更新系统状态
      if (data.uptime !== undefined) {
        systemStatus.value.uptime = formatUptime(data.uptime)
      }
      if (data.qps !== undefined) {
        systemStatus.value.qps = data.qps
      }

      // 更新 JVM 内存信息
      if (data.jvm) {
        systemStatus.value.totalMemory = formatBytes(data.jvm.maxMemory || 0)
        systemStatus.value.usedMemory = formatBytes(data.jvm.usedMemory || 0)
        systemStatus.value.jvmMemory = formatBytes(data.jvm.usedMemory || 0)
        // 更新内存使用率
        if (data.jvm.memoryUsagePercent !== undefined) {
          memoryUsage.value = data.jvm.memoryUsagePercent
          // 计算缓存和可用内存
          memoryCache.value = data.jvm.memoryCachePercent || 0
        }
      }

      // 更新CPU使用率
      if (data.cpu !== undefined && data.cpu.usagePercent !== undefined) {
        cpuUsage.value = data.cpu.usagePercent
      }

      // 更新磁盘使用率
      if (data.disks && data.disks.length > 0) {
        diskUsage.value = data.disks.map(disk => ({
          path: disk.path || disk.mount,
          mount: disk.mount || disk.path,
          total: disk.total || 0,
          used: disk.used || 0,
          usage: Math.round((disk.used / disk.total) * 100) || 0
        }))
      }

      // 更新网络流量
      if (data.network) {
        networkStats.value = {
          inSpeed: formatSpeed(data.network.inSpeed || 0),
          outSpeed: formatSpeed(data.network.outSpeed || 0),
          totalIn: data.network.totalIn || 0,
          totalOut: data.network.totalOut || 0
        }
      }

      // 更新请求统计
      if (data.requests) {
        requestStats.value = {
          todayCount: data.requests.todayCount || 0,
          avgResponse: data.requests.avgResponse || 0,
          successRate: data.requests.successRate || 100,
          errorCount: data.requests.errorCount || 0
        }
      }

      // 更新趋势图数据
      if (data.trends && data.trends.length > 0) {
        trendPoints.value = data.trends.map((point, index) => ({
          x: 40 + (index * 70),
          y: Math.max(20, 130 - point.value)
        }))
      }
    }

    // 获取在线用户列表
    const usersRes = await getOnlineUsers()
    if (usersRes.code === 200 && usersRes.data) {
      onlineUsers.value = usersRes.data.map(user => ({
        userId: user.userId,
        username: user.username || '-',
        nickname: user.nickname || '-',
        department: '-',
        lastActive: formatTime(user.lastActiveTime),
        requestCount: 0,
        sessionId: user.sessionId,
        loginIp: user.loginIp || '-',
        avatar: user.avatar
      }))
    }

    ElMessage.success('数据已刷新')
  } catch (error) {
    console.error('刷新数据失败:', error)
    ElMessage.error('刷新数据失败')
  } finally {
    loading.value = false
  }
}

// 格式化运行时间
const formatUptime = (seconds) => {
  if (!seconds) return '-'
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (days > 0) return `${days}天${hours}小时`
  if (hours > 0) return `${hours}小时${minutes}分钟`
  return `${minutes}分钟`
}

// 格式化字节
const formatBytes = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const unitIndex = Math.floor(Math.log(bytes) / Math.log(1024))
  const value = bytes / Math.pow(1024, unitIndex)
  return Math.round(value * 100) / 100 + ' ' + units[unitIndex]
}

// 格式化速度
const formatSpeed = (bytesPerSecond) => {
  if (!bytesPerSecond || bytesPerSecond === 0) return '0 B/s'
  const units = ['B/s', 'KB/s', 'MB/s', 'GB/s']
  const unitIndex = Math.floor(Math.log(bytesPerSecond) / Math.log(1024))
  const value = bytesPerSecond / Math.pow(1024, unitIndex)
  return Math.round(value * 100) / 100 + ' ' + units[unitIndex]
}

// 踢出用户
const handleKickUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要踢出用户 "${user.nickname || user.username}" 吗？`,
      '确认踢出',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await kickUser(user.userId)
    if (res.code === 200) {
      ElMessage.success('踢出成功')
      // 刷新在线用户列表
      await loadOnlineUsers()
    } else {
      ElMessage.error(res.msg || '踢出失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('踢出用户失败:', error)
      ElMessage.error('踢出用户失败')
    }
  }
}

// 加载在线用户列表
const loadOnlineUsers = async () => {
  try {
    const res = await getOnlineUsers()
    if (res.code === 200 && res.data) {
      onlineUsers.value = res.data.map(user => ({
        userId: user.userId,
        username: user.username || '-',
        nickname: user.nickname || '-',
        department: '-',
        lastActive: formatTime(user.lastActiveTime),
        requestCount: 0,
        sessionId: user.sessionId,
        loginIp: user.loginIp || '-',
        avatar: user.avatar
      }))
      // 更新在线用户数
      systemStatus.value.onlineUsers = onlineUsers.value.length
    }
  } catch (error) {
    console.error('加载在线用户失败:', error)
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const now = new Date()
  const date = new Date(time)
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return Math.floor(diff / 86400000) + '天前'
}

// 保存设置
const handleSaveSettings = () => {
  ElMessage.success('监控设置已保存')
  startAutoRefresh()
}

// 开始自动刷新
const startAutoRefresh = () => {
  stopAutoRefresh()
  refreshTimer = setInterval(async () => {
    // 调用完整的刷新逻辑，从API获取真实数据
    await refreshAll()
  }, monitorSettings.value.refreshInterval)
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 获取CPU颜色
const getCpuColor = () => {
  if (cpuUsage.value < 50) return 'var(--dt-success)'
  if (cpuUsage.value < 80) return 'var(--dt-warning)'
  return 'var(--dt-error)'
}

// 获取磁盘颜色
const getDiskColor = (usage) => {
  if (usage < 70) return 'var(--dt-success)'
  if (usage < 90) return 'var(--dt-warning)'
  return 'var(--dt-error)'
}

// 获取成功率样式类
const getSuccessClass = (rate) => {
  if (rate >= 99) return 'stat-row-value--success'
  if (rate >= 95) return 'stat-row-value--warning'
  return 'stat-row-value--error'
}

onMounted(() => {
  loadOnlineUsers()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
/* 引入主题变量 */
@import '@/styles/admin-theme.css';

/* ================================
   页面容器
   ================================ */
.system-monitor {
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
   状态卡片
   ================================ */
.status-overview {
  margin-bottom: var(--dt-space-md);
}

.status-card {
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
  display: flex;
  align-items: center;
  gap: var(--dt-space-md);
  padding: var(--dt-space-md);
}

.status-card.status--error {
  border-color: var(--dt-error);
  background: var(--dt-error-bg);
}

.status-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  background: var(--dt-primary-gradient);
}

.status-info {
  flex: 1;
}

.status-label {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin-bottom: 4px;
}

.status-value {
  font-size: var(--dt-font-size-md);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
}

.status-mini-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  background: var(--dt-primary-gradient);
}

.status-mini-info {
  flex: 1;
}

.status-mini-label {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-secondary);
}

.status-mini-value {
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
}

/* ================================
   图表卡片
   ================================ */
.charts-row {
  margin-bottom: var(--dt-space-md);
}

.chart-card {
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
  height: 280px;
}

.chart-card :deep(.el-card__header) {
  padding: var(--dt-space-sm) var(--dt-space-md);
  border-bottom: 1px solid var(--dt-divider);
}

.chart-card :deep(.el-card__body) {
  padding: var(--dt-space-md);
  height: calc(100% - 50px);
}

.chart-title {
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
}

.chart-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 仪表盘 */
.gauge-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.gauge {
  width: 200px;
  height: 100px;
}

.gauge-progress {
  transition: stroke-dashoffset 0.5s ease;
}

.gauge-label {
  font-size: 18px;
  font-weight: var(--dt-font-weight-medium);
  fill: var(--dt-text-primary);
}

.gauge-legend {
  display: flex;
  gap: var(--dt-space-lg);
  margin-left: var(--dt-space-lg);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: var(--dt-space-xs);
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-secondary);
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--dt-radius-round);
}

.chart-value {
  font-size: var(--dt-font-size-lg);
  font-weight: var(--dt-font-weight-bold);
  color: var(--dt-primary);
}

/* 内存条 */
.memory-bars {
  width: 100%;
  max-width: 300px;
  margin: 0 auto;
}

.memory-bar {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
  margin-bottom: var(--dt-space-sm);
}

.memory-label {
  width: 40px;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

.bar-track {
  flex: 1;
  height: 8px;
  background-color: var(--dt-bg-page);
  border-radius: var(--dt-radius-sm);
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: var(--dt-radius-sm);
  transition: width var(--dt-transition-base);
}

.bar-fill--used { background: var(--dt-primary); }
.bar-fill--cache { background: var(--dt-warning); }
.bar-fill--free { background: var(--dt-success); }

.memory-value {
  width: 40px;
  text-align: right;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-regular);
  font-weight: var(--dt-font-weight-medium);
}

.memory-detail {
  margin-top: var(--dt-space-md);
  display: flex;
  justify-content: center;
  gap: var(--dt-space-lg);
}

.detail-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: var(--dt-font-size-sm);
}

.detail-label {
  color: var(--dt-text-secondary);
  margin-bottom: 4px;
}

.detail-value {
  color: var(--dt-text-primary);
  font-weight: var(--dt-font-weight-medium);
}

/* 磁盘列表 */
.disk-list {
  width: 100%;
}

.disk-item {
  display: flex;
  align-items: center;
  gap: var(--dt-space-md);
  margin-bottom: var(--dt-space-md);
}

.disk-info {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
  flex: 1;
}

.disk-icon {
  width: 32px;
  height: 32px;
  background: var(--dt-warning-bg);
  border-radius: var(--dt-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dt-warning);
}

.disk-name {
  font-size: var(--dt-font-size-sm);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
}

.disk-path {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-secondary);
}

.disk-usage {
  display: flex;
  align-items: center;
  gap: var(--dt-space-xs);
  margin-right: var(--dt-space-md);
}

.disk-bar {
  width: 100px;
  height: 6px;
  background: var(--dt-bg-page);
  border-radius: var(--dt-radius-sm);
  overflow: hidden;
}

.disk-size {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-secondary);
}

/* 网络统计 */
.network-stats {
  display: flex;
  flex-direction: column;
  gap: var(--dt-space-md);
  align-items: center;
}

.network-item {
  display: flex;
  align-items: center;
  gap: var(--dt-space-md);
  width: 200px;
  padding: var(--dt-space-sm);
  border-radius: var(--dt-radius-sm);
  background: var(--dt-bg-page);
}

.network-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.network-item--in .network-icon { background: var(--dt-info-bg); color: var(--dt-info); }
.network-item--out .network-icon { background: var(--dt-success-bg); color: var(--dt-success); }

.network-info {
  flex: 1;
}

.network-label {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-secondary);
  margin-bottom: 4px;
}

.network-value {
  font-size: var(--dt-font-size-lg);
  font-weight: var(--dt-font-weight-bold);
  color: var(--dt-text-primary);
}

.network-detail {
  display: flex;
  justify-content: space-around;
  margin-top: var(--dt-space-md);
}

/* 请求统计 */
.request-stats {
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 100%;
}

.stat-row {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-row-label {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin-bottom: 4px;
}

.stat-row-value {
  font-size: var(--dt-font-size-xl);
  font-weight: var(--dt-font-weight-bold);
  color: var(--dt-text-primary);
}

.stat-row-value--success { color: var(--dt-success); }
.stat-row-value--warning { color: var(--dt-warning); }
.stat-row-value--error { color: var(--dt-error); }

/* 趋势图 */
.trend-chart {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.trend-svg {
  width: 100%;
  height: 120px;
}

.trend-legend {
  display: flex;
  gap: var(--dt-space-md);
  margin-top: var(--dt-space-sm);
}

/* 表格卡片 */
.table-card {
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
}

.table-card :deep(.el-card__header) {
  padding: var(--dt-space-sm) var(--dt-space-md);
  border-bottom: 1px solid var(--dt-divider);
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

/* 设置表单 */
.threshold-item {
  display: flex;
  align-items: center;
  margin-bottom: var(--dt-space-sm);
}

.threshold-label {
  width: 100px;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-regular);
}

.threshold-unit {
  margin-left: var(--dt-space-xs);
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

/* 用户单元格 */
.user-cell {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: var(--dt-font-size-sm);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
}

.user-account {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-secondary);
}
</style>
