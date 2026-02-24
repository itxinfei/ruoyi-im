<template>
  <div class="audit-center">
    <div class="audit-header">
      <div class="header-title">
        <span class="material-icons-outlined header-icon">security</span>
        <h2>安全审计中心</h2>
      </div>
      <div class="header-actions">
        <el-button
          type="primary"
          plain
          @click="exportReport"
        >
          <span class="material-icons-outlined">download</span>
          导出报告
        </el-button>
        <el-button @click="refreshData">
          <span class="material-icons-outlined">refresh</span>
          刷新
        </el-button>
      </div>
    </div>

    <div class="audit-stats">
      <div class="stat-card danger">
        <div class="stat-icon">
          <span class="material-icons-outlined">warning</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">
            {{ stats.highRisk }}
          </div>
          <div class="stat-label">
            高风险事件
          </div>
        </div>
      </div>
      <div class="stat-card warning">
        <div class="stat-icon">
          <span class="material-icons-outlined">info</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">
            {{ stats.mediumRisk }}
          </div>
          <div class="stat-label">
            中风险事件
          </div>
        </div>
      </div>
      <div class="stat-card info">
        <div class="stat-icon">
          <span class="material-icons-outlined">description</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">
            {{ stats.totalLogs }}
          </div>
          <div class="stat-label">
            审计记录总数
          </div>
        </div>
      </div>
      <div class="stat-card success">
        <div class="stat-icon">
          <span class="material-icons-outlined">person</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">
            {{ stats.activeUsers }}
          </div>
          <div class="stat-label">
            活跃用户数
          </div>
        </div>
      </div>
    </div>

    <div class="audit-filters">
      <div class="filter-row">
        <div class="filter-item">
          <label>时间范围</label>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
          />
        </div>
        <div class="filter-item">
          <label>事件类型</label>
          <el-select
            v-model="filters.eventType"
            placeholder="全部类型"
            clearable
          >
            <el-option
              v-for="type in eventTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </div>
        <div class="filter-item">
          <label>风险等级</label>
          <el-select
            v-model="filters.riskLevel"
            placeholder="全部等级"
            clearable
          >
            <el-option
              label="高风险"
              value="high"
            >
              <span class="risk-tag high">高风险</span>
            </el-option>
            <el-option
              label="中风险"
              value="medium"
            >
              <span class="risk-tag medium">中风险</span>
            </el-option>
            <el-option
              label="低风险"
              value="low"
            >
              <span class="risk-tag low">低风险</span>
            </el-option>
          </el-select>
        </div>
        <div class="filter-item">
          <label>操作人</label>
          <el-input
            v-model="filters.operator"
            placeholder="输入用户名搜索"
            clearable
          />
        </div>
      </div>
      <div class="filter-actions">
        <el-button @click="resetFilters">
          重置
        </el-button>
        <el-button
          type="primary"
          @click="searchLogs"
        >
          查询
        </el-button>
      </div>
    </div>

    <div class="audit-content">
      <el-table
        :data="auditLogs"
        :loading="loading"
        stripe
        style="width: 100%"
        @row-click="showLogDetail"
      >
        <el-table-column
          label="风险等级"
          width="100"
        >
          <template #default="{ row }">
            <span
              class="risk-tag"
              :class="row.riskLevel"
            >
              {{ riskLabel(row.riskLevel) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          label="事件类型"
          width="140"
        >
          <template #default="{ row }">
            <div class="event-type">
              <span class="material-icons-outlined event-icon">{{ eventIcon(row.eventType) }}</span>
              <span>{{ eventLabel(row.eventType) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="事件描述"
          min-width="200"
        />
        <el-table-column
          prop="operatorName"
          label="操作人"
          width="100"
        />
        <el-table-column
          prop="clientIp"
          label="IP地址"
          width="130"
        />
        <el-table-column
          label="时间"
          width="170"
        >
          <template #default="{ row }">
            {{ formatTime(row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="80"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              text
              size="small"
              @click.stop="showLogDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="showDetailDialog"
      title="审计详情"
      width="600px"
      class="audit-detail-dialog"
    >
      <div
        v-if="selectedLog"
        class="detail-content"
      >
        <div class="detail-header">
          <span
            class="risk-tag large"
            :class="selectedLog.riskLevel"
          >
            {{ riskLabel(selectedLog.riskLevel) }}
          </span>
          <h3>{{ eventLabel(selectedLog.eventType) }}</h3>
        </div>
        <el-descriptions
          :column="2"
          border
        >
          <el-descriptions-item label="事件描述">
            {{ selectedLog.description }}
          </el-descriptions-item>
          <el-descriptions-item label="操作人">
            {{ selectedLog.operatorName }} (ID: {{ selectedLog.operatorId }})
          </el-descriptions-item>
          <el-descriptions-item label="客户端IP">
            {{ selectedLog.clientIp }}
          </el-descriptions-item>
          <el-descriptions-item label="发生时间">
            {{ formatTime(selectedLog.timestamp) }}
          </el-descriptions-item>
          <el-descriptions-item label="设备信息">
            {{ selectedLog.deviceInfo || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="selectedLog.status === 'resolved' ? 'success' : 'warning'">
              {{ selectedLog.status === 'resolved' ? '已处理' : '待处理' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item
            label="详细信息"
            :span="2"
          >
            <pre class="detail-json">{{ JSON.stringify(selectedLog.details || {}, null, 2) }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">
          关闭
        </el-button>
        <el-button
          v-if="selectedLog?.status !== 'resolved'"
          type="primary"
          @click="markResolved"
        >
          标记已处理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const loading = ref(false)
const showDetailDialog = ref(false)
const selectedLog = ref(null)

const dateRange = ref([])
const filters = reactive({
  eventType: '',
  riskLevel: '',
  operator: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const stats = reactive({
  highRisk: 0,
  mediumRisk: 0,
  totalLogs: 0,
  activeUsers: 0
})

const auditLogs = ref([])

const eventTypes = [
  { value: 'SCREENSHOT_ATTEMPT', label: '截图尝试' },
  { value: 'CLIPBOARD_CHANGE', label: '剪贴板操作' },
  { value: 'LOGIN', label: '登录事件' },
  { value: 'LOGOUT', label: '登出事件' },
  { value: 'MESSAGE_DELETE', label: '消息删除' },
  { value: 'FILE_DOWNLOAD', label: '文件下载' },
  { value: 'FORWARD_ATTEMPT', label: '转发尝试' },
  { value: 'SENSITIVE_WORD', label: '敏感词触发' }
]

const dateShortcuts = [
  {
    text: '今天',
    value: () => {
      const today = new Date()
      return [today, today]
    }
  },
  {
    text: '最近7天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近30天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  }
]

function riskLabel(level) {
  const map = { high: '高风险', medium: '中风险', low: '低风险' }
  return map[level] || level
}

function eventLabel(type) {
  const found = eventTypes.find(t => t.value === type)
  return found?.label || type
}

function eventIcon(type) {
  const map = {
    SCREENSHOT_ATTEMPT: 'screenshot',
    CLIPBOARD_CHANGE: 'content_copy',
    LOGIN: 'login',
    LOGOUT: 'logout',
    MESSAGE_DELETE: 'delete',
    FILE_DOWNLOAD: 'download',
    FORWARD_ATTEMPT: 'forward',
    SENSITIVE_WORD: 'warning'
  }
  return map[type] || 'info'
}

function formatTime(timestamp) {
  if (!timestamp) {return '-'}
  return new Date(timestamp).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

async function loadStats() {
  try {
    const res = await request.get('/api/im/security/stats')
    if (res.code === 200) {
      Object.assign(stats, res.data)
    }
  } catch (error) {
    console.warn('Load stats failed:', error)
  }
}

async function loadLogs() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...filters
    }
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await request.get('/api/im/security/logs', { params })
    if (res.code === 200) {
      auditLogs.value = res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.warn('Load logs failed:', error)
    auditLogs.value = []
  } finally {
    loading.value = false
  }
}

function searchLogs() {
  pagination.page = 1
  loadLogs()
}

function resetFilters() {
  dateRange.value = []
  filters.eventType = ''
  filters.riskLevel = ''
  filters.operator = ''
  searchLogs()
}

function refreshData() {
  loadStats()
  loadLogs()
  ElMessage.success('数据已刷新')
}

function handleSizeChange(size) {
  pagination.pageSize = size
  loadLogs()
}

function handlePageChange(page) {
  pagination.page = page
  loadLogs()
}

function showLogDetail(row) {
  selectedLog.value = row
  showDetailDialog.value = true
}

async function markResolved() {
  if (!selectedLog.value) {return}
  try {
    await request.put(`/api/im/security/logs/${selectedLog.value.id}/resolve`)
    selectedLog.value.status = 'resolved'
    ElMessage.success('已标记为已处理')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

async function exportReport() {
  try {
    ElMessage.info('正在生成报告...')
    const params = { ...filters }
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await request.get('/api/im/security/export', { params, responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `安全审计报告_${new Date().toISOString().split('T')[0]}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    ElMessage.success('报告已下载')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  loadStats()
  loadLogs()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.audit-center {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-body);
}

.audit-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--dt-space-4) var(--dt-space-6);
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
}

.header-title {
  display: flex;
  align-items: center;
  gap: var(--dt-space-3);

  .header-icon {
    font-size: 28px;
    color: var(--dt-brand-color);
  }

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: var(--dt-text-primary);
  }
}

.header-actions {
  display: flex;
  gap: var(--dt-space-2);

  .el-button {
    .material-icons-outlined {
      font-size: 18px;
      margin-right: 4px;
    }
  }
}

.audit-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--dt-space-4);
  padding: var(--dt-space-4) var(--dt-space-6);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--dt-space-3);
  padding: var(--dt-space-4);
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  border: 1px solid var(--dt-border-light);
  transition: all var(--dt-transition-base);

  &:hover {
    box-shadow: var(--dt-shadow-card-hover);
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--dt-radius-md);
    display: flex;
    align-items: center;
    justify-content: center;

    .material-icons-outlined {
      font-size: 24px;
      color: #fff;
    }
  }

  .stat-content {
    flex: 1;
  }

  .stat-value {
    font-size: 24px;
    font-weight: 700;
    color: var(--dt-text-primary);
    line-height: 1.2;
  }

  .stat-label {
    font-size: 13px;
    color: var(--dt-text-tertiary);
    margin-top: 2px;
  }

  &.danger .stat-icon {
    background: linear-gradient(135deg, #ff4d4f, #ff7875);
  }

  &.warning .stat-icon {
    background: linear-gradient(135deg, #faad14, #ffc53d);
  }

  &.info .stat-icon {
    background: linear-gradient(135deg, #1677ff, #4096ff);
  }

  &.success .stat-icon {
    background: linear-gradient(135deg, #52c41a, #73d13d);
  }
}

.audit-filters {
  padding: var(--dt-space-4) var(--dt-space-6);
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);

  .filter-row {
    display: flex;
    gap: var(--dt-space-4);
    flex-wrap: wrap;
  }

  .filter-item {
    display: flex;
    flex-direction: column;
    gap: var(--dt-space-1);

    label {
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }
  }

  .filter-actions {
    display: flex;
    justify-content: flex-end;
    gap: var(--dt-space-2);
    margin-top: var(--dt-space-3);
  }
}

.audit-content {
  flex: 1;
  overflow: hidden;
  padding: var(--dt-space-4) var(--dt-space-6);
  display: flex;
  flex-direction: column;
}

.risk-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: var(--dt-radius-sm);
  font-size: 12px;
  font-weight: 500;

  &.high {
    background: rgba(255, 77, 79, 0.15);
    color: #ff4d4f;
  }

  &.medium {
    background: rgba(250, 173, 20, 0.15);
    color: #faad14;
  }

  &.low {
    background: rgba(82, 196, 26, 0.15);
    color: #52c41a;
  }

  &.large {
    padding: 4px 12px;
    font-size: 14px;
  }
}

.event-type {
  display: flex;
  align-items: center;
  gap: var(--dt-space-1);

  .event-icon {
    font-size: 16px;
    color: var(--dt-text-secondary);
  }
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding-top: var(--dt-space-4);
}

.audit-detail-dialog {
  .detail-content {
    .detail-header {
      display: flex;
      align-items: center;
      gap: var(--dt-space-3);
      margin-bottom: var(--dt-space-4);

      h3 {
        margin: 0;
        font-size: 18px;
      }
    }
  }

  .detail-json {
    background: var(--dt-bg-card-hover);
    padding: var(--dt-space-3);
    border-radius: var(--dt-radius-sm);
    font-size: 12px;
    max-height: 200px;
    overflow: auto;
    margin: 0;
  }
}

@media (max-width: 1200px) {
  .audit-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .audit-stats {
    grid-template-columns: 1fr;
  }

  .filter-row {
    flex-direction: column;
  }
}
</style>