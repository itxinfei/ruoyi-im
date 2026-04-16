<template>
  <div class="approval-panel">
    <div class="panel-header">
      <h2 class="panel-title">
        审批流程
      </h2>
      <button class="add-btn" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
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
        <el-icon class="empty-icon">
          <Document />
        </el-icon>
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
import { Loading, Plus, Document } from '@element-plus/icons-vue'
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
  padding: var(--dt-spacing-lg) var(--dt-spacing-xl);
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-color);
  flex-shrink: 0;
}

.panel-title {
  font-size: var(--dt-font-size-xl);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin: 0;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-xs);
  padding: var(--dt-spacing-xs) var(--dt-spacing-lg);
  background: var(--dt-brand-color);
  color: var(--dt-text-primary);
  border: none;
  border-radius: var(--dt-radius-lg);
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  cursor: pointer;
  transition: background-color var(--dt-transition-fast);
}

.add-btn:hover {
  background: var(--dt-brand-hover);
}

.add-btn:active {
  opacity: 0.9;
}

.panel-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tabs {
  display: flex;
  padding: 0 var(--dt-spacing-xl);
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-color);
  flex-shrink: 0;
}

.tab-item {
  position: relative;
  padding: var(--dt-spacing-md) var(--dt-spacing-md);
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: color var(--dt-transition-fast);
}

.tab-item:hover {
  color: var(--dt-brand-color);
}

.tab-item.active {
  color: var(--dt-brand-color);
  border-bottom-color: var(--dt-brand-color);
  font-weight: var(--dt-font-weight-medium);
}

.tab-count {
  margin-left: var(--dt-spacing-xs);
  padding: var(--dt-spacing-xs) var(--dt-spacing-sm);
  background: var(--dt-brand-color);
  color: var(--dt-bg-card);
  border-radius: var(--dt-radius-full);
  font-size: var(--dt-font-size-xs);
  font-weight: var(--dt-font-weight-medium);
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: var(--dt-empty-state-height);
  color: var(--dt-text-tertiary);
}

.empty-icon {
  font-size: var(--dt-icon-size-2xl);
  margin-bottom: var(--dt-spacing-lg);
  color: var(--dt-border-color);
}

.empty-text {
  font-size: var(--dt-font-size-base);
  margin: 0;
  color: var(--dt-text-secondary);
}

.approval-list {
  padding: var(--dt-spacing-lg) var(--dt-spacing-xl);
  overflow-y: auto;
  flex: 1;
}

.approval-item {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  padding: var(--dt-spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
  cursor: pointer;
  transition: border-color var(--dt-transition-fast), box-shadow var(--dt-transition-fast);
  margin-bottom: var(--dt-spacing-sm);
  border: 1px solid transparent;
}

.approval-item:hover {
  border-color: var(--dt-brand-light);
}

.approval-avatar {
  width: var(--dt-avatar-size-lg);
  height: var(--dt-avatar-size-lg);
  border-radius: var(--dt-radius-lg);
  color: var(--dt-text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--dt-font-size-lg);
  font-weight: var(--dt-font-weight-semibold);
  flex-shrink: 0;
}

.approval-content {
  flex: 1;
  min-width: 0;
}

.approval-title {
  font-size: var(--dt-font-size-md);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin-bottom: var(--dt-spacing-xs);
}

.approval-meta {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);
}

.approval-type {
  padding: var(--dt-spacing-xs) var(--dt-spacing-sm);
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-secondary);
  font-weight: var(--dt-font-weight-medium);
}

.approval-status {
  font-size: var(--dt-font-size-xs);
  padding: var(--dt-spacing-xs) var(--dt-spacing-md);
  border-radius: var(--dt-radius-md);
  white-space: nowrap;
  font-weight: var(--dt-font-weight-medium);
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
