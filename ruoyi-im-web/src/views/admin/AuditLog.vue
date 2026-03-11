<template>
  <div class="admin-page audit-log-page">
    <!-- KPI 卡片 -->
    <el-row :gutter="16" class="kpi-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="kpi-card" shadow="never">
          <div class="kpi-title">总操作数</div>
          <div class="kpi-value">{{ stats.totalOperations || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="kpi-card" shadow="never">
          <div class="kpi-title">成功数</div>
          <div class="kpi-value success">{{ stats.successOperations || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="kpi-card" shadow="never">
          <div class="kpi-title">失败数</div>
          <div class="kpi-value danger">{{ stats.failedOperations || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="kpi-card" shadow="never">
          <div class="kpi-title">成功率</div>
          <div class="kpi-value small">{{ successRate }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选工具栏 -->
    <el-card class="panel" shadow="never">
      <template #header>
        <div class="panel-header">
          <div>
            <h3>审计日志</h3>
            <p>记录用户操作行为，支持追溯与审计</p>
          </div>
          <el-space>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" @click="loadLogs">刷新</el-button>
          </el-space>
        </div>
      </template>

      <el-row :gutter="16" class="filter-row">
        <el-col :xs="24" :sm="12" :md="6">
          <el-input v-model="filters.userId" placeholder="用户 ID" clearable @keyup.enter="loadLogs" @clear="loadLogs">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="filters.operationType" placeholder="操作类型" clearable @change="loadLogs">
            <el-option label="登录" value="LOGIN" />
            <el-option label="登出" value="LOGOUT" />
            <el-option label="发送消息" value="SEND_MESSAGE" />
            <el-option label="删除消息" value="DELETE_MESSAGE" />
            <el-option label="创建群组" value="CREATE_GROUP" />
            <el-option label="加入群组" value="JOIN_GROUP" />
            <el-option label="退出群组" value="LEAVE_GROUP" />
            <el-option label="添加好友" value="ADD_FRIEND" />
            <el-option label="删除好友" value="DELETE_FRIEND" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="filters.operationResult" placeholder="操作结果" clearable @change="loadLogs">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            @change="loadLogs"
          />
        </el-col>
      </el-row>
    </el-card>

    <!-- 日志表格 -->
    <el-card class="panel" shadow="never">
      <el-table :data="logList" v-loading="loading" border>
        <template #empty>
          <el-empty description="暂无审计日志">
            <el-button type="primary" @click="handleReset">重置筛选</el-button>
          </el-empty>
        </template>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userName" label="用户" min-width="120">
          <template #default="{ row }">
            <span>{{ row.userName || `用户${row.userId}` }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="140">
          <template #default="{ row }">
            <el-tag size="small" :type="getOperationTypeTag(row.operationType)">{{ getOperationTypeLabel(row.operationType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="目标类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ getTargetTypeLabel(row.targetType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标 ID" width="100" />
        <el-table-column prop="operationResult" label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.operationResult === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.operationResult === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" min-width="180" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP 地址" width="140" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="审计日志详情" width="600px">
      <el-descriptions v-if="currentLog" :column="1" border>
        <el-descriptions-item label="日志 ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="用户 ID">{{ currentLog.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名称">{{ currentLog.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTag(currentLog.operationType)">{{ getOperationTypeLabel(currentLog.operationType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="目标类型">{{ getTargetTypeLabel(currentLog.targetType) }}</el-descriptions-item>
        <el-descriptions-item label="目标 ID">{{ currentLog.targetId }}</el-descriptions-item>
        <el-descriptions-item label="操作结果">
          <el-tag :type="currentLog.operationResult === 'SUCCESS' ? 'success' : 'danger'">
            {{ currentLog.operationResult === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="currentLog.errorMessage">{{ currentLog.errorMessage }}</el-descriptions-item>
        <el-descriptions-item label="IP 地址">{{ currentLog.ipAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="User Agent" v-if="currentLog.userAgent">
          <el-input type="textarea" :model-value="currentLog.userAgent" :rows="2" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watchEffect } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { getAuditLogList, getAuditStatistics } from '@/api/admin'

const loading = ref(false)
const logList = ref([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const stats = ref({})
const detailDialogVisible = ref(false)
const currentLog = ref(null)

const filters = reactive({
  userId: '',
  operationType: '',
  operationResult: '',
  startTime: '',
  endTime: ''
})

const dateRange = ref([])

// 计算成功率
const successRate = computed(() => {
  const total = stats.value.totalOperations || 0
  const success = stats.value.successOperations || 0
  if (total === 0) return '0.00'
  return ((success / total) * 100).toFixed(2)
})

// 操作类型标签映射
const getOperationTypeLabel = (type) => {
  const labels = {
    LOGIN: '登录',
    LOGOUT: '登出',
    SEND_MESSAGE: '发送消息',
    DELETE_MESSAGE: '删除消息',
    CREATE_GROUP: '创建群组',
    JOIN_GROUP: '加入群组',
    LEAVE_GROUP: '退出群组',
    ADD_FRIEND: '添加好友',
    DELETE_FRIEND: '删除好友'
  }
  return labels[type] || type
}

const getOperationTypeTag = (type) => {
  const tagMap = {
    LOGIN: '',
    LOGOUT: 'info',
    SEND_MESSAGE: 'success',
    DELETE_MESSAGE: 'danger',
    CREATE_GROUP: 'warning',
    JOIN_GROUP: '',
    LEAVE_GROUP: 'info',
    ADD_FRIEND: 'success',
    DELETE_FRIEND: 'danger'
  }
  return tagMap[type] || ''
}

const getTargetTypeLabel = (type) => {
  const labels = {
    USER: '用户',
    MESSAGE: '消息',
    GROUP: '群组',
    CONVERSATION: '会话',
    FRIEND: '好友'
  }
  return labels[type] || type
}

const loadStatistics = async () => {
  try {
    const params = {}
    if (filters.startTime) params.startTime = filters.startTime
    if (filters.endTime) params.endTime = filters.endTime
    const res = await getAuditStatistics(params)
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    // 统计加载失败不影响日志列表
  }
}

const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userId: filters.userId || undefined,
      operationType: filters.operationType || undefined,
      operationResult: filters.operationResult || undefined,
      startTime: filters.startTime || undefined,
      endTime: filters.endTime || undefined
    }
    const res = await getAuditLogList(params)
    if (res.code === 200) {
      logList.value = res.data.list || []
      total.value = res.data.total || 0
    }
    // 同时加载统计
    loadStatistics()
  } catch (error) {
    ElMessage.error('加载审计日志失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  filters.userId = ''
  filters.operationType = ''
  filters.operationResult = ''
  filters.startTime = ''
  filters.endTime = ''
  dateRange.value = []
  pageNum.value = 1
  loadLogs()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadLogs()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadLogs()
}

const handleViewDetail = async (row) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

// 监听日期范围变化
watchEffect(() => {
  if (dateRange.value && dateRange.value.length === 2) {
    filters.startTime = dateRange.value[0]
    filters.endTime = dateRange.value[1]
  } else {
    filters.startTime = ''
    filters.endTime = ''
  }
})

onMounted(() => {
  loadLogs()
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
  font-size: 18px;
  font-weight: 600;
}

.kpi-value.success {
  color: #22c55e;
}

.kpi-value.danger {
  color: #ef4444;
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

.filter-row {
  margin-bottom: 0;
}

.pager-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
</style>
