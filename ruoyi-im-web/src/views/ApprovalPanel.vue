<template>
  <div class="approval-panel">
    <div class="panel-header">
      <h2 class="panel-title">
        审批流程
      </h2>
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
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="approvals.length === 0" class="empty-state">
        <span class="material-icons-outlined empty-icon">assignment</span>
        <p class="empty-text">
          暂无审批事项
        </p>
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
            <div class="approval-title">
              {{ approval.title }}
            </div>
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

    <!-- 审批详情对话框 -->
    <ApprovalDetailDialog
      v-model="showDetailDialog"
      :approval="selectedApproval"
      @success="handleApprovalAction"
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import CreateApprovalDialog from '@/components/CreateApprovalDialog/index.vue'
import ApprovalDetailDialog from '@/components/ApprovalDetailDialog/index.vue'
import { getPendingApprovals, getMyApprovals, getProcessedApprovals } from '@/api/im/approval'

const loading = ref(false)
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const selectedApproval = ref(null)
const activeTab = ref('pending')
const approvals = ref([])
const tabCounts = ref({
  pending: 0,
  processed: 0,
  initiated: 0
})


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
    const currentTab = activeTab.value
    if (currentTab === 'pending') {
      res = await getPendingApprovals()
    } else if (currentTab === 'processed') {
      res = await getProcessedApprovals()
    } else if (currentTab === 'initiated') {
      res = await getMyApprovals()
    }

    if (res.code === 200) {
      approvals.value = (res.data || []).map(item => ({
        ...item,
        bgColor: getRandomColor()
      }))
      // 更新当前 tab 的计数
      tabCounts.value[currentTab] = approvals.value.length
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
  const colors = ['var(--dt-brand-color)', 'var(--dt-success-color)', 'var(--dt-warning-color)', 'var(--dt-info-color)', 'var(--dt-error-color)']
  return colors[Math.floor(Math.random() * colors.length)]
}

const getCount = (tab) => {
  return tabCounts.value[tab] || 0
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

// 查看审批详情
const handleView = (approval) => {
  selectedApproval.value = approval
  showDetailDialog.value = true
}

// 审批操作成功回调
const handleApprovalAction = () => {
  loadApprovals()
  showDetailDialog.value = false
}

// 组件加载时初始加载数据
onMounted(() => {
  loadApprovals()
})
</script>

<style scoped lang="scss">
.approval-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: var(--dt-bg-body);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-color);
  flex-shrink: 0;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--dt-brand-color);
  color: var(--dt-text-primary);
  border: none;
  border-radius: var(--dt-radius-lg);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--dt-transition-fast);
}

.add-btn:hover {
  background: var(--dt-brand-hover);
  transform: translateY(-1px);
  box-shadow: var(--dt-shadow-float);
}

.add-btn:active {
  transform: translateY(0);
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
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-color);
  flex-shrink: 0;
}

.tab-item {
  position: relative;
  padding: 14px 16px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: 14px;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);
}

.tab-item:hover {
  color: var(--dt-brand-color);
}

.tab-item.active {
  color: var(--dt-brand-color);
  border-bottom-color: var(--dt-brand-color);
  font-weight: 500;
}

.tab-count {
  margin-left: 4px;
  padding: 2px 8px;
  background: var(--dt-brand-color);
  color: var(--dt-bg-card);
  border-radius: var(--dt-radius-full);
  font-size: 11px;
  font-weight: 500;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--dt-text-tertiary);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: var(--dt-border-color);
}

.empty-text {
  font-size: 14px;
  margin: 0;
  color: var(--dt-text-secondary);
}

.approval-list {
  padding: 16px 24px;
  overflow-y: auto;
  flex: 1;
}

.approval-item {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  margin-bottom: 8px;
  border: 1px solid var(--dt-border-light);
}

.approval-item:hover {
  box-shadow: var(--dt-shadow-float);
  transform: translateY(-1px);
  border-color: var(--dt-brand-color);
}

.approval-avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--dt-radius-lg);
  color: var(--dt-text-primary);
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
  color: var(--dt-text-primary);
  margin-bottom: 4px;
}

.approval-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.approval-type {
  padding: 3px 8px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-secondary);
  font-weight: 500;
}

.approval-status {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: var(--dt-radius-md);
  white-space: nowrap;
  font-weight: 500;
}

.approval-status.pending {
  background: var(--dt-warning-bg);
  color: var(--dt-warning-color);
}

.approval-status.approved {
  background: var(--dt-success-bg);
  color: var(--dt-success-color);
}

.approval-status.rejected {
  background: var(--dt-error-bg);
  color: var(--dt-error-color);
}

/* 暗色模式 */
.dark .approval-panel {
  background: var(--dt-bg-body-dark);
}

.dark .panel-header {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .panel-title {
  color: var(--dt-text-primary-dark);
}

.dark .tabs,
.dark .approval-item {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .approval-title {
  color: var(--dt-text-primary-dark);
}

.dark .tab-item {
  color: var(--dt-text-secondary-dark);
}

.dark .tab-item.active {
  color: var(--dt-brand-color);
}

.dark .approval-type {
  background: var(--dt-bg-hover-dark);
  color: var(--dt-text-secondary-dark);
}

.dark .empty-icon {
  color: var(--dt-border-dark);
}
</style>
