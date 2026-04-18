<template>
  <div class="approval-v2">
    <header class="view-header">
      <div class="header-left">
        <h2 class="view-title">审批中心</h2>
        <div class="header-tab-bar">
          <div 
            v-for="tab in tabs" 
            :key="tab.key" 
            class="header-tab-item"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
            <span v-if="getCount(tab.key) > 0" class="tab-badge">{{ getCount(tab.key) }}</span>
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" class="launch-btn" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          <span>发起审批</span>
        </el-button>
      </div>
    </header>

    <main class="approval-body custom-scrollbar" v-loading="loading">
      <!-- 空状态 -->
      <div v-if="!loading && approvals.length === 0" class="empty-view">
        <div class="empty-box">
          <el-icon class="empty-icon"><Tickets /></el-icon>
          <p>暂无审批事项</p>
        </div>
      </div>

      <!-- 审批列表 -->
      <div v-else class="approval-list">
        <div v-for="item in approvals" :key="item.id" class="approval-card" @click="handleView(item)">
          <div class="card-main">
            <div class="app-icon" :style="{ background: item.bgColor }">{{ item.title.charAt(0) }}</div>
            <div class="app-info">
              <h4 class="app-title">{{ item.title }}</h4>
              <div class="app-meta">
                <span class="meta-type">{{ item.type }}</span>
                <span class="meta-dot">·</span>
                <span class="meta-time">{{ item.time }}</span>
              </div>
            </div>
            <div class="app-status" :class="item.status">
              {{ statusText(item.status) }}
            </div>
          </div>
          <!-- 底部快速摘要 (钉钉风格) -->
          <div v-if="item.summary" class="card-footer">
            摘要: {{ item.summary }}
          </div>
        </div>
      </div>
    </main>

    <CreateApprovalDialog v-model="showCreateDialog" @success="handleApprovalCreated" />
    <ApprovalDetailDialog v-model="showDetailDialog" :approval="selectedApproval" @success="handleApprovalAction" />
  </div>
</template>

<script setup lang="js">
import { ref, watch, onMounted } from 'vue'
import { Plus, Tickets, Loading } from '@element-plus/icons-vue'
import { getPendingApprovals, getMyApprovals, getProcessedApprovals } from '@/api/im/approval'
import CreateApprovalDialog from '@/components/CreateApprovalDialog/index.vue'
import ApprovalDetailDialog from '@/components/ApprovalDetailDialog/index.vue'

const activeTab = ref('pending')
const loading = ref(false)
const approvals = ref([])
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const selectedApproval = ref(null)

const tabs = [
  { key: 'pending', label: '待我审批' },
  { key: 'processed', label: '我已处理' },
  { key: 'initiated', label: '我发起的' }
]

const statusText = (s) => ({ pending: '待审批', approved: '已通过', rejected: '已拒绝' }[s] || '未知')
const getCount = (k) => k === 'pending' ? approvals.value.length : 0 // 简化演示

const loadData = async () => {
  loading.value = true
  try {
    const res = activeTab.value === 'pending' ? await getPendingApprovals() : await getProcessedApprovals()
    if (res.code === 200) approvals.value = res.data.map(i => ({ ...i, bgColor: 'var(--dt-brand-color)' }))
  } finally { loading.value = false }
}

watch(activeTab, loadData)
onMounted(loadData)

const handleView = (i) => { selectedApproval.value = i; showDetailDialog.value = true }
const handleApprovalCreated = () => loadData()
const handleApprovalAction = () => loadData()
</script>

<style scoped lang="scss">
.approval-v2 { display: flex; flex-direction: column; height: 100%; background: #fff; }

.view-header {
  height: 64px; padding: 0 24px; border-bottom: 1px solid var(--dt-border-light);
  display: flex; align-items: center; justify-content: space-between;
  .header-left { display: flex; align-items: center; gap: 40px; }
  .view-title { font-size: 18px; font-weight: 700; margin: 0; }
}

.header-tab-bar {
  display: flex; gap: 32px; height: 100%;
  .header-tab-item {
    height: 64px; line-height: 64px; font-size: 14px; color: var(--dt-text-secondary);
    cursor: pointer; position: relative; transition: 0.2s;
    &:hover { color: var(--dt-brand-color); }
    &.active {
      color: var(--dt-brand-color); font-weight: 600;
      &::after { content: ''; position: absolute; bottom: 0; left: 0; right: 0; height: 3px; background: var(--dt-brand-color); border-radius: 3px 3px 0 0; }
    }
    .tab-badge { margin-left: 4px; font-size: 11px; background: var(--dt-error-color); color: #fff; padding: 1px 6px; border-radius: 10px; vertical-align: middle; }
  }
}

.approval-body { flex: 1; overflow-y: auto; padding: 24px; background: #fdfdfe; }

.approval-list {
  max-width: 900px; margin: 0 auto;
}

.approval-card {
  background: #fff; border: 1px solid var(--dt-border-light); border-radius: 12px;
  padding: 16px 20px; margin-bottom: 16px; cursor: pointer; transition: 0.2s;
  &:hover { border-color: var(--dt-brand-light); box-shadow: var(--dt-shadow-1); .app-icon { transform: scale(1.05); } }
  
  .card-main { display: flex; align-items: center; gap: 16px; }
  .app-icon {
    width: 44px; height: 44px; border-radius: 10px; color: #fff; @include flex-center;
    font-size: 18px; font-weight: 700; transition: 0.2s;
  }
  .app-info { flex: 1; .app-title { font-size: 15px; font-weight: 600; margin: 0; } .app-meta { font-size: 12px; color: var(--dt-text-tertiary); margin-top: 4px; } }
  
  .app-status {
    padding: 4px 12px; border-radius: 6px; font-size: 12px; font-weight: 600;
    &.pending { background: #fff7e6; color: #fa8c16; }
    &.approved { background: #f6ffed; color: #52c41a; }
    &.rejected { background: #fff1f0; color: #f5222d; }
  }
  
  .card-footer { margin-top: 12px; padding-top: 12px; border-top: 1px dashed var(--dt-border-light); font-size: 12px; color: var(--dt-text-secondary); }
}

.empty-view { height: 100%; @include flex-center; color: var(--dt-text-tertiary); opacity: 0.5; text-align: center; .empty-icon { font-size: 64px; margin-bottom: 16px; } }
</style>
