<template>
  <div class="operation-log">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <h2>操作日志</h2>
        <p class="page-desc">记录系统内所有用户的操作行为</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Download" @click="handleExport">导出日志</el-button>
        <el-button :icon="Delete" @click="handleClear" :disabled="selectedLogs.length === 0">
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 搜索卡片 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="操作人">
          <el-input
            v-model="searchForm.operator"
            placeholder="输入用户名/昵称"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select v-model="searchForm.module" placeholder="全部模块" clearable style="width: 120px">
            <el-option label="用户管理" value="user" />
            <el-option label="群组管理" value="group" />
            <el-option label="消息管理" value="message" />
            <el-option label="部门管理" value="department" />
            <el-option label="角色管理" value="role" />
            <el-option label="系统设置" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.action" placeholder="全部类型" clearable style="width: 120px">
            <el-option label="新增" value="create" />
            <el-option label="修改" value="update" />
            <el-option label="删除" value="delete" />
            <el-option label="查询" value="query" />
            <el-option label="导出" value="export" />
            <el-option label="登录" value="login" />
            <el-option label="登出" value="logout" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 320px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 批量操作栏 -->
    <div v-if="selectedLogs.length > 0" class="batch-actions-bar">
      <div class="batch-info">
        <el-icon class="batch-icon"><Select /></el-icon>
        <span>已选择 <strong>{{ selectedLogs.length }}</strong> 条日志</span>
      </div>
      <div class="batch-buttons">
        <el-button type="danger" size="small" :icon="Delete" @click="handleBatchDelete">
          批量删除
        </el-button>
        <el-button size="small" @click="handleClearSelection">取消选择</el-button>
      </div>
    </div>

    <!-- 日志统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card stat-card--total">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Document /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ logStats.total || 0 }}</div>
              <div class="stat-label">总记录数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card--success">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ logStats.success || 0 }}</div>
              <div class="stat-label">今日成功</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card--error">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><CircleClose /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ logStats.failed || 0 }}</div>
              <div class="stat-label">今日失败</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card--active">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ logStats.activeUsers || 0 }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="logList"
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="50" fixed="left" />
        <el-table-column prop="id" label="日志ID" width="90" />
        <el-table-column prop="operator" label="操作人" width="120">
          <template #default="{ row }">
            <div class="operator-cell">
              <el-avatar :size="24" :src="row.operatorAvatar">
                {{ row.operatorName?.[0] || 'U' }}
              </el-avatar>
              <span class="operator-name">{{ row.operatorName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="操作模块" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getModuleColor(row.module)" size="small">
              {{ getModuleLabel(row.module) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作类型" width="80" align="center">
          <template #default="{ row }">
            <span class="action-tag" :class="'action-tag--' + row.action">
              {{ getActionLabel(row.action) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column prop="createTime" label="操作时间" width="160" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'success'" size="small" type="success">成功</el-tag>
            <el-tag v-else size="small" type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[20, 50, 100, 200]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 日志详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      title="日志详情"
      size="450px"
    >
      <div v-if="currentLog" class="detail-content">
        <el-descriptions :column="1" border class="detail-descriptions">
          <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
          <el-descriptions-item label="操作人">
            <div class="operator-detail">
              <el-avatar :size="32" :src="currentLog.operatorAvatar">
                {{ currentLog.operatorName?.[0] || 'U' }}
              </el-avatar>
              <span>{{ currentLog.operatorName }}</span>
              <el-tag size="small" type="info">{{ currentLog.operatorId }}</el-tag>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="操作模块">
            <el-tag :type="getModuleColor(currentLog.module)">
              {{ getModuleLabel(currentLog.module) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <span class="action-tag" :class="'action-tag--' + currentLog.action">
              {{ getActionLabel(currentLog.action) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="操作描述">
            {{ currentLog.description }}
          </el-descriptions-item>
          <el-descriptions-item label="请求方法">
            <el-tag size="small" type="info">{{ currentLog.method || 'GET' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请求路径">
            <span class="path-text">{{ currentLog.path || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">
            {{ currentLog.ip }}
          </el-descriptions-item>
          <el-descriptions-item label="浏览器">
            {{ currentLog.userAgent || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="操作时间">
            {{ currentLog.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="执行时长">
            {{ currentLog.duration || '-' }} ms
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="currentLog.status === 'success'" type="success">成功</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentLog.errorMessage" label="错误信息">
            <span class="error-message">{{ currentLog.errorMessage }}</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentLog.requestParams" label="请求参数">
            <pre class="params-json">{{ JSON.stringify(currentLog.requestParams, null, 2) }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Download,
  Delete,
  Select,
  User,
  Document,
  CircleCheck,
  CircleClose
} from '@element-plus/icons-vue'
import {
  getAuditLogList,
  getAuditStatistics,
  deleteExpiredLogs
} from '@/api/admin'

const loading = ref(false)
const logList = ref([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedLogs = ref([])
const tableRef = ref(null)

// 搜索表单
const searchForm = ref({
  operator: '',
  module: '',
  action: '',
  status: '',
  dateRange: []
})

// 详情抽屉
const detailDrawerVisible = ref(false)
const currentLog = ref(null)

// 日志统计
const logStats = ref({
  total: 0,
  success: 0,
  failed: 0,
  activeUsers: 0
})

// 加载日志列表
const loadLogs = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      startTime: searchForm.value.dateRange?.[0],
      endTime: searchForm.value.dateRange?.[1]
    }

    // 调用后端 API
    const res = await getAuditLogList(params)
    if (res.code === 200 && res.data) {
      logList.value = (res.data.list || []).map(log => ({
        id: log.id,
        operatorId: log.userId || '-',
        operatorName: log.userName || '-',
        operatorAvatar: log.userAvatar || '',
        module: log.module || 'system',
        action: log.operationType || 'query',
        description: log.description || '-',
        ip: log.ip || '-',
        userAgent: log.userAgent || '-',
        method: log.method || 'GET',
        path: log.requestUrl || '-',
        status: log.operationResult === 'success' ? 'success' : 'failed',
        errorMessage: log.errorMessage || '',
        duration: log.duration || 0,
        createTime: log.createTime || '-'
      }))
      total.value = res.data.total || 0
    }

    // 加载统计数据
    const statsRes = await getAuditStatistics(params)
    if (statsRes.code === 200 && statsRes.data) {
      logStats.value = {
        total: statsRes.data.total || 0,
        success: statsRes.data.successCount || 0,
        failed: statsRes.data.failedCount || 0,
        activeUsers: statsRes.data.activeUsers || 0
      }
    }
  } catch (error) {
    console.error('加载日志失败:', error)
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  loadLogs()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    operator: '',
    module: '',
    action: '',
    status: '',
    dateRange: []
  }
  pageNum.value = 1
  loadLogs()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  loadLogs()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadLogs()
}

// 表格选择
const handleSelectionChange = (selection) => {
  selectedLogs.value = selection
}

const handleClearSelection = () => {
  tableRef.value?.clearSelection()
}

// 查看详情
const handleViewDetail = (row) => {
  currentLog.value = row
  detailDrawerVisible.value = true
}

// 导出
const handleExport = () => {
  try {
    // 导出当前日志列表为 CSV
    const headers = ['日志ID', '操作模块', '操作类型', '操作人', 'IP地址', '操作时间', '状态']
    const rows = logList.value.map(log => [
      log.id,
      `"${log.module || ''}"`,
      `"${log.action || ''}"`,
      `"${log.operatorName || ''}"`,
      log.ip || '',
      log.createTime || '',
      log.status === 'success' ? '成功' : '失败'
    ])

    // 添加 BOM 以支持中文
    const BOM = '\uFEFF'
    const csvContent = BOM + headers.join(',') + '\n' + rows.map(r => r.join(',')).join('\n')

    // 创建 Blob 并下载
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `操作日志_${new Date().toISOString().slice(0, 10)}.csv`
    link.click()
    URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 清空日志
const handleClear = async () => {
  try {
    await ElMessageBox.confirm('确定要清空30天前的操作日志吗？此操作不可恢复。', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    // 删除30天前的日志
    const beforeDate = new Date()
    beforeDate.setDate(beforeDate.getDate() - 30)
    const res = await deleteExpiredLogs(beforeDate.toISOString())

    if (res.code === 200) {
      ElMessage.success(`已清理 ${res.data || 0} 条日志`)
      loadLogs()
    } else {
      ElMessage.error(res.msg || '清理失败')
    }
  } catch {
    // 取消
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedLogs.value.length} 条日志吗？`, '确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    // 实际后端可能需要提供批量删除接口
    // 这里暂时用清空30天前的日志代替
    ElMessage.warning('批量删除功能开发中，请使用清空过期日志功能')
  } catch {
    // 取消
  }
}

// 获取模块标签
const getModuleLabel = (module) => {
  const labels = {
    user: '用户',
    group: '群组',
    message: '消息',
    department: '部门',
    role: '角色',
    system: '系统'
  }
  return labels[module] || module
}

const getModuleColor = (module) => {
  const colors = {
    user: 'primary',
    group: 'success',
    message: 'warning',
    department: 'info',
    role: 'danger',
    system: ''
  }
  return colors[module] || ''
}

// 获取操作类型标签
const getActionLabel = (action) => {
  const labels = {
    create: '新增',
    update: '修改',
    delete: '删除',
    query: '查询',
    export: '导出',
    login: '登录',
    logout: '登出'
  }
  return labels[action] || action
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped lang="scss">
/* 引入主题变量 */
@import '@/styles/admin-theme.scss';

/* ================================
   页面容器
   ================================ */
.operation-log {
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
   搜索卡片
   ================================ */
.search-card {
  margin-bottom: var(--dt-space-md);
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
}

.search-card :deep(.el-card__body) {
  padding: var(--dt-space-md);
}

.search-card :deep(.el-form-item) {
  margin-bottom: var(--dt-space-sm);
  margin-right: var(--dt-space-md);
}

.search-card :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.search-card :deep(.el-form-item__label) {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-regular);
}

/* ================================
   批量操作栏
   ================================ */
.batch-actions-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--dt-space-sm) var(--dt-space-md);
  margin-bottom: var(--dt-space-md);
  background: var(--dt-primary);
  border-radius: var(--dt-radius-sm);
  color: white;
}

.batch-info {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
  font-size: var(--dt-font-size-base);
}

.batch-icon {
  font-size: 18px;
}

.batch-buttons {
  display: flex;
  gap: var(--dt-space-sm);
}

.batch-buttons .el-button {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
}

.batch-buttons .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 深色模式下的按钮样式 */
[data-theme='dark'] .batch-buttons .el-button {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
}

[data-theme='dark'] .batch-buttons .el-button:hover {
  background: rgba(255, 255, 255, 0.2);
}

.batch-buttons .el-button--danger {
  background: var(--dt-error);
  border-color: var(--dt-error);
}

/* ================================
   统计卡片
   ================================ */
.stats-row {
  margin-bottom: var(--dt-space-md);
}

.stat-card {
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: var(--dt-space-md);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stat-card--total .stat-icon {
  background: linear-gradient(135deg, #0089FF 0%, #0066CC 100%);
}

.stat-card--success .stat-icon {
  background: linear-gradient(135deg, #00C853 0%, #009624 100%);
}

.stat-card--error .stat-icon {
  background: linear-gradient(135deg, #FF3D00 0%, #D50000 100%);
}

.stat-card--active .stat-icon {
  background: linear-gradient(135deg, #7C4DFF 0%, #5E32CC 100%);
}

.stat-value {
  font-size: 24px;
  font-weight: var(--dt-font-weight-bold);
  color: var(--dt-text-primary);
}

.stat-label {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-secondary);
}

/* ================================
   表格
   ================================ */
.table-card {
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
}

.operator-cell {
  display: flex;
  align-items: center;
  gap: var(--dt-space-xs);
}

.operator-name {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-regular);
}

.action-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--dt-radius-sm);
  font-size: var(--dt-font-size-xs);
  font-weight: var(--dt-font-weight-medium);
}

.action-tag--create { background: var(--dt-success-bg); color: var(--dt-success); }
.action-tag--update { background: var(--dt-warning-bg); color: var(--dt-warning); }
.action-tag--delete { background: var(--dt-error-bg); color: var(--dt-error); }
.action-tag--query { background: var(--dt-info-bg); color: var(--dt-info); }
.action-tag--export { background: var(--dt-primary-bg); color: var(--dt-primary); }
.action-tag--login { background: #E3F2FD; color: #1976D2; }
.action-tag--logout { background: #FFEBEE; color: #C62828; }

/* ================================
   详情抽屉
   ================================ */
.detail-content {
  padding: var(--dt-space-md);
}

.operator-detail {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
}

.path-text {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-info);
}

.error-message {
  color: var(--dt-error);
  word-break: break-all;
}

.params-json {
  background: var(--dt-bg-page);
  padding: var(--dt-space-sm);
  border-radius: var(--dt-radius-sm);
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-regular);
  overflow-x: auto;
}

/* ================================
   分页
   ================================ */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: var(--dt-space-md);
  border-top: 1px solid var(--dt-divider);
}
</style>
