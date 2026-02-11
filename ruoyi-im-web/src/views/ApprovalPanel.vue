<template>
  <div class="approval-panel">
    <div class="panel-header">
      <h2 class="panel-title">
        审批流程
      </h2>
      <div class="header-actions">
        <!-- 待我审批时的批量操作 -->
        <template v-if="activeTab === 'pending' && pendingApprovals.length > 0">
          <button
            class="batch-btn batch-btn--approve"
            @click="batchApproveAll"
          >
            <span class="material-icons-outlined">check_circle</span>
            <span>全部通过</span>
          </button>
        </template>
        <button
          class="add-btn"
          @click="showCreateDialog = true"
        >
          <span class="material-icons-outlined">add</span>
          发起审批
        </button>
      </div>
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
          <span class="material-icons-outlined tab-icon">{{ tab.icon }}</span>
          {{ tab.label }}
          <span
            v-if="getCount(tab.key) > 0"
            class="tab-count"
          >{{ getCount(tab.key) }}</span>
        </button>
      </div>

      <div
        v-if="loading"
        class="loading-state"
      >
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
        <span>加载中...</span>
      </div>

      <div
        v-else-if="approvals.length === 0"
        class="empty-state"
      >
        <span class="material-icons-outlined empty-icon">{{ emptyIcon }}</span>
        <p class="empty-text">
          {{ emptyText }}
        </p>
      </div>

      <div
        v-else
        class="approval-list"
      >
        <div
          v-for="approval in approvals"
          :key="approval.id"
          class="approval-item"
          :class="{
            'is-pending': approval.status === 'pending',
            'batch-selected': selectedApprovals.has(approval.id)
          }"
          @click="handleView(approval)"
        >
          <div
            class="approval-avatar"
            :style="{ background: approval.bgColor }"
          >
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
          <div
            class="approval-status"
            :class="approval.status"
          >
            {{ statusText(approval.status) }}
          </div>
          <!-- 待审批项的快捷操作 -->
          <div
            v-if="approval.status === 'pending'"
            class="quick-actions"
            @click.stop
          >
            <button
              class="quick-btn quick-btn--approve"
              title="通过"
              @click="handleQuickApprove(approval)"
            >
              <span class="material-icons-outlined">check</span>
            </button>
            <button
              class="quick-btn quick-btn--reject"
              title="拒绝"
              @click="handleQuickReject(approval)"
            >
              <span class="material-icons-outlined">close</span>
            </button>
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
import { ref, computed, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CreateApprovalDialog from '@/components/Workplace/CreateApprovalDialog.vue'
import ApprovalDetailDialog from '@/components/Workplace/ApprovalDetailDialog.vue'
import { getPendingApprovals, getMyApprovals, getProcessedApprovals, handleApproval } from '@/api/im/approval'

const loading = ref(false)
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const selectedApproval = ref(null)
const activeTab = ref('pending')
const approvals = ref([])
const selectedApprovals = ref(new Set())

const tabs = [
  { key: 'pending', label: '待我审批', icon: 'pending_actions' },
  { key: 'processed', label: '我已处理', icon: 'task_alt' },
  { key: 'initiated', label: '我发起的', icon: 'send' }
]

// 待审批列表（用于批量操作）
const pendingApprovals = computed(() => {
  return approvals.value.filter(a => a.status === 'pending')
})

// 空状态配置
const emptyConfig = computed(() => {
  const configs = {
    pending: {
      icon: 'check_circle',
      text: '暂无待审批事项，太棒了！'
    },
    processed: {
      icon: 'history',
      text: '暂无已处理的审批记录'
    },
    initiated: {
      icon: 'send',
      text: '暂无发起的审批'
    }
  }
  return configs[activeTab.value] || configs.pending
})

const emptyIcon = computed(() => emptyConfig.value.icon)
const emptyText = computed(() => emptyConfig.value.text)

// 监听标签切换
watch(activeTab, () => {
  loadApprovals()
  selectedApprovals.value.clear()
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

    if (res && res.code === 200) {
      approvals.value = (res.data || []).map(item => ({
        ...item,
        bgColor: getRandomColor()
      }))
    } else {
      approvals.value = []
    }
  } catch (error) {
    console.warn('加载审批列表失败:', error.message)
    approvals.value = []
  } finally {
    loading.value = false
  }
}

// 随机颜色
const getRandomColor = () => {
  const colors = ['#3296FA', '#52c41a', '#fa8c16', '#722ed1', '#eb2f96']
  return colors[Math.floor(Math.random() * colors.length)]
}

const getCount = tab => {
  if (tab === 'pending') {return approvals.value.filter(a => a.status === 'pending').length}
  if (tab === 'processed') {return approvals.value.filter(a => a.status !== 'pending').length}
  return 0
}

const statusText = status => {
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
const handleView = approval => {
  selectedApproval.value = approval
  showDetailDialog.value = true
}

// 审批操作成功回调
const handleApprovalAction = () => {
  loadApprovals()
  showDetailDialog.value = false
}

// ============================================================================
// 快捷操作功能
// ============================================================================
const handleQuickApprove = async approval => {
  try {
    await ElMessageBox.prompt('请输入审批意见（可选）', '通过审批', {
      confirmButtonText: '通过',
      cancelButtonText: '取消',
      inputPattern: /^.{0,200}$/,
      inputErrorMessage: '审批意见不能超过200字'
    }).then(async ({ value }) => {
      const comment = value || '同意'
      await processApproval(approval.id, 'APPROVE', comment)
    }).catch(() => {
      // 用户取消，直接通过无意见
      processApproval(approval.id, 'APPROVE', '同意')
    })
  } catch (error) {
    // 用户取消对话框
  }
}

const handleQuickReject = async approval => {
  try {
    await ElMessageBox.prompt('请输入拒绝原因（必填）', '拒绝审批', {
      confirmButtonText: '拒绝',
      cancelButtonText: '取消',
      inputPattern: /^.{1,200}$/,
      inputErrorMessage: '拒绝原因不能为空且不能超过200字'
    }).then(async ({ value }) => {
      await processApproval(approval.id, 'REJECT', value)
    })
  } catch (error) {
    // 用户取消
  }
}

const processApproval = async (approvalId, action, comment) => {
  try {
    const res = await handleApproval({
      approvalId,
      action,
      comment
    })
    if (res.code === 200) {
      ElMessage.success(action === 'APPROVE' ? '已通过' : '已拒绝')
      loadApprovals()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('审批操作失败', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 批量全部通过
const batchApproveAll = async () => {
  const pending = pendingApprovals.value
  if (pending.length === 0) {return}

  try {
    await ElMessageBox.confirm(`确定要全部通过 ${pending.length} 条待审批吗？`, '批量通过', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    const promises = pending.map(a => handleApproval({
      approvalId: a.id,
      action: 'APPROVE',
      comment: '批量通过'
    }))

    await Promise.all(promises)
    ElMessage.success(`已通过 ${pending.length} 条审批`)
    loadApprovals()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量审批失败', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  }
}

// 组件加载时初始加载数据
loadApprovals()
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
  gap: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.batch-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  background: var(--dt-bg-card);

  &--approve {
    background: var(--dt-success-bg);
    border-color: var(--dt-success-color);
    color: var(--dt-success-color);

    &:hover {
      background: var(--dt-success-color);
      color: #fff;
    }
  }
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
  color: #fff;
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
  box-shadow: var(--dt-shadow-brand);
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
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
  padding: 14px 16px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: 14px;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  .tab-icon {
    font-size: 18px;
  }
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
  color: #fff;
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

// 快捷操作按钮
.quick-actions {
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: opacity var(--dt-transition-fast);
}

.approval-item:hover .quick-actions,
.approval-item.is-pending .quick-actions {
  opacity: 1;
}

.quick-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--dt-radius-full);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  .material-icons-outlined {
    font-size: 18px;
  }

  &--approve {
    background: var(--dt-success-bg);
    color: var(--dt-success-color);

    &:hover {
      background: var(--dt-success-color);
      color: #fff;
      transform: scale(1.1);
    }
  }

  &--reject {
    background: var(--dt-error-bg);
    color: var(--dt-error-color);

    &:hover {
      background: var(--dt-error-color);
      color: #fff;
      transform: scale(1.1);
    }
  }
}

.approval-item.is-pending {
  .quick-actions {
    opacity: 1;
  }
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

.dark .header-actions {
  .batch-btn {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);

    &--approve {
      background: var(--dt-success-bg-dark);
      border-color: var(--dt-success-color);
      color: var(--dt-success-color-dark);

      &:hover {
        background: var(--dt-success-color);
        color: #fff;
      }
    }
  }
}

.dark .quick-btn {
  &--approve {
    background: var(--dt-success-bg-dark);
    color: var(--dt-success-color-dark);

    &:hover {
      background: var(--dt-success-color);
      color: #fff;
    }
  }

  &--reject {
    background: var(--dt-error-bg-dark);
    color: var(--dt-error-color-dark);

    &:hover {
      background: var(--dt-error-color);
      color: #fff;
    }
  }
}
</style>
