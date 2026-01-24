<template>
  <div class="approval-panel">
    <div class="panel-header">
      <h2 class="panel-title">审批流程</h2>
      <button class="add-btn" @click="showCreateDialog = true">
        <span class="material-icons-outlined">add</span>
        发起审批
      </button>
    </div>

    <div class="panel-content">
      <div class="tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
          <span v-if="getCount(tab.key) > 0" class="tab-count">{{ getCount(tab.key) }}</span>
        </button>
      </div>

      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="approvals.length === 0" class="empty-state">
        <span class="material-icons-outlined empty-icon">assignment</span>
        <p class="empty-text">暂无审批事项</p>
      </div>

      <div v-else class="approval-list">
        <div
          v-for="approval in approvals"
          :key="approval.id"
          class="approval-item"
          @click="handleView(approval)"
        >
          <div class="approval-avatar" :style="{ background: approval.bgColor }">
            {{ approval.title.charAt(0) }}
          </div>
          <div class="approval-content">
            <div class="approval-title">{{ approval.title }}</div>
            <div class="approval-meta">
              <span class="approval-type">{{ approval.type }}</span>
              <span class="approval-time">{{ approval.time }}</span>
            </div>
          </div>
          <div class="approval-status" :class="approval.status">
            {{ statusText(approval.status) }}
          </div>
        </div>
      </div>
    </div>

    <!-- 发起审批对话框 -->
    <CreateApprovalDialog
      v-model="showCreateDialog"
      @success="handleApprovalCreated"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import CreateApprovalDialog from '@/components/CreateApprovalDialog/index.vue'
import { getPendingApprovals, getMyApprovals, getProcessedApprovals } from '@/api/im/approval'

const loading = ref(false)
const showCreateDialog = ref(false)
const activeTab = ref('pending')
const approvals = ref([])


const tabs = [
  { key: 'pending', label: '待我审批' },
  { key: 'processed', label: '我已处理' },
  { key: 'initiated', label: '我发起的' }
]

// 监听标签切换
watch(activeTab, () => {
  loadApprovals()
})

// 加载审批列表
const loadApprovals = async () => {
  loading.value = true
  try {
    let res
    if (activeTab.value === 'pending') {
      res = await getPendingApprovals()
    } else if (activeTab.value === 'processed') {
      res = await getProcessedApprovals()
    } else if (activeTab.value === 'initiated') {
      res = await getMyApprovals()
    }

    if (res.code === 200) {
      approvals.value = (res.data || []).map(item => ({
        ...item,
        bgColor: getRandomColor()
      }))
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载审批列表失败', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 随机颜色
const getRandomColor = () => {
  const colors = ['#1677ff', '#52c41a', '#fa8c16', '#722ed1', '#eb2f96']
  return colors[Math.floor(Math.random() * colors.length)]
}

const getCount = (tab) => {
  if (tab === 'pending') return approvals.value.filter(a => a.status === 'pending').length
  if (tab === 'processed') return approvals.value.filter(a => a.status !== 'pending').length
  return 0
}

const statusText = (status) => {
  const map = {
    pending: '待审批',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return map[status] || '未知'
}

// 新建审批成功回调
const handleApprovalCreated = () => {
  if (activeTab.value === 'initiated') {
    loadApprovals()
  }
}

// 组件加载时初始加载数据
loadApprovals()
</script>

<style scoped>
.approval-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: #f4f7f9;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  flex-shrink: 0;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.add-btn:hover {
  background: #4096ff;
}

.panel-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tabs {
  display: flex;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  flex-shrink: 0;
}

.tab-item {
  position: relative;
  padding: 14px 16px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: 14px;
  color: #595959;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-item:hover {
  color: #1677ff;
}

.tab-item.active {
  color: #1677ff;
  border-bottom-color: #1677ff;
}

.tab-count {
  margin-left: 4px;
  padding: 2px 6px;
  background: #1677ff;
  color: #fff;
  border-radius: 10px;
  font-size: 11px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #8c8c8c;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: #d9d9d9;
}

.empty-text {
  font-size: 14px;
  margin: 0;
}

.approval-list {
  padding: 16px 24px;
  overflow-y: auto;
  flex: 1;
}

.approval-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.approval-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.approval-avatar {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}

.approval-content {
  flex: 1;
  min-width: 0;
}

.approval-title {
  font-size: 15px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.approval-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #8c8c8c;
}

.approval-type {
  padding: 2px 8px;
  background: #f0f0f0;
  border-radius: 4px;
}

.approval-status {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  white-space: nowrap;
}

.approval-status.pending {
  background: #fff7e6;
  color: #fa8c16;
}

.approval-status.approved {
  background: #f6ffed;
  color: #52c41a;
}

.approval-status.rejected {
  background: #fff1f0;
  color: #ff4d4f;
}

/* 暗色模式 */
:deep(.dark) .approval-panel {
  background: #0f172a;
}

:deep(.dark) .panel-header {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .panel-title {
  color: #f1f5f9;
}

:deep(.dark) .tabs,
:deep(.dark) .approval-item {
  background: #1e293b;
}

:deep(.dark) .approval-title {
  color: #f1f5f9;
}

:deep(.dark) .tab-item {
  color: #94a3b8;
}

:deep(.dark) .tab-item.active {
  color: #60a5fa;
}
</style>
