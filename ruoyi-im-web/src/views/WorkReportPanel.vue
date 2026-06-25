<template>
  <div class="report-v2">
    <!-- 顶部状态统计 -->
    <header class="report-header">
      <div class="header-left">
        <h2 class="view-title">工作日志</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" class="write-btn" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          <span>写日志</span>
        </el-button>
      </div>
    </header>

    <div class="report-stats">
      <div v-for="s in statItems" :key="s.label" class="stat-box">
        <div class="stat-icon" :class="s.type"><el-icon><component :is="s.icon" /></el-icon></div>
        <div class="stat-content">
          <span class="stat-val">{{ s.value }}</span>
          <span class="stat-lab">{{ s.label }}</span>
        </div>
      </div>
    </div>

    <main class="report-main custom-scrollbar">
      <el-tabs v-model="activeTab" class="report-tabs">
        <el-tab-pane label="我的日志" name="my" />
        <el-tab-pane label="待我审批" name="pending" />
      </el-tabs>

      <div class="report-list" v-loading="loading">
        <div v-if="reportList.length === 0" class="empty-state">
          <el-icon :size="64" color="var(--dt-text-quaternary)"><Document /></el-icon>
          <p>暂无日志记录</p>
        </div>
        
        <!-- 日志流布局 (对齐钉钉 8.2) -->
        <div v-for="report in reportList" :key="report.id" class="report-flow-item" @click="openDetail(report)">
          <div class="flow-date">
            <span class="day">{{ formatDay(report.reportDate) }}</span>
            <span class="month">{{ formatMonth(report.reportDate) }}</span>
          </div>
          <div class="flow-card">
            <div class="card-header">
              <span class="type-tag">{{ report.reportType }}</span>
              <span class="status-tag" :class="report.status">{{ report.statusName }}</span>
            </div>
            <p class="card-body-text">{{ report.workContent }}</p>
            <div class="card-footer">
              <div class="footer-left">
                <el-icon><Clock /></el-icon> <span>{{ report.submitTime }}</span>
              </div>
              <div class="footer-right">
                <el-icon><ChatDotRound /></el-icon> <span>{{ report.commentCount || 0 }}</span>
                <el-icon><Star /></el-icon> <span>{{ report.likeCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="js">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Calendar, Clock, Stamp, ChatDotRound, Star, Document } from '@element-plus/icons-vue'
import { getWorkReportPage, getWorkReportStatistics } from '@/api/im/workReport'

const activeTab = ref('my')
const loading = ref(false)
const reportList = ref([])
const stats = ref({})

const statItems = computed(() => [
  { label: '今日已交', value: stats.value.todayCount || 0, icon: Calendar, type: 'blue' },
  { label: '本周累计', value: stats.value.weekCount || 0, icon: Clock, type: 'green' },
  { label: '待我审批', value: stats.value.pendingApproval || 0, icon: Stamp, type: 'orange' }
])

const openCreateDialog = () => { ElMessage.info('功能开发中') }
const openDetail = (report) => { ElMessage.info('功能开发中') }

const formatDay = (d) => d?.split('-')[2] || '--'
const formatMonth = (d) => {
  const m = d?.split('-')[1]
  return m ? `${parseInt(m)}月` : ''
}

const loadData = async () => {
  loading.value = true
  try {
    const [sRes, pRes] = await Promise.all([getWorkReportStatistics(), getWorkReportPage({ pageNum: 1, pageSize: 10 })])
    if (sRes.code === 200) stats.value = sRes.data
    if (pRes.code === 200) reportList.value = pRes.data.records || []
  } catch (e) {
    ElMessage.error('加载日志数据失败')
  } finally { loading.value = false }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.report-v2 { display: flex; flex-direction: column; height: 100%; background: var(--dt-bg-body); }

// 钉钉规范：头部高度 56px
.report-header {
  height: var(--dt-header-height); padding: 0 24px; border-bottom: 1px solid var(--dt-border-light);
  display: flex; align-items: center; justify-content: space-between;
  .view-title { font-size: var(--dt-font-size-xl); font-weight: var(--dt-font-weight-bold); margin: 0; }
}

.report-stats {
  display: flex; gap: 16px; padding: 24px 24px;
  .stat-box {
    flex: 1; height: 80px; background: var(--dt-bg-card); border: 1px solid var(--dt-border-light);
    border-radius: var(--dt-radius-lg); display: flex; align-items: center; padding: 0 20px; gap: 16px;
    transition: var(--dt-transition-fast); &:hover { box-shadow: var(--dt-shadow-1); border-color: var(--dt-brand-light); }

    .stat-icon {
      width: 44px; height: 44px; border-radius: var(--dt-radius-lg); @include flex-center; font-size: var(--dt-font-size-2xl);
      &.blue { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
      &.green { background: var(--dt-success-bg); color: var(--dt-success-color); }
      &.orange { background: var(--dt-warning-bg); color: var(--dt-warning-color); }
    }
    .stat-content {
      .stat-val { font-size: var(--dt-font-size-2xl); font-weight: var(--dt-font-weight-bold); display: block; line-height: 1.2; }
      .stat-lab { font-size: 12px; color: var(--dt-text-tertiary); }
    }
  }
}

.report-main { flex: 1; overflow-y: auto; padding: 0 24px 24px; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: var(--dt-spacing-lg);
  p { color: var(--dt-text-tertiary); font-size: var(--dt-font-size-base); margin: 0; }
}

.report-flow-item {
  display: flex; gap: 24px; margin-bottom: 24px; cursor: pointer;

  .flow-date {
    width: 60px; text-align: right; flex-shrink: 0;
    .day { font-size: 32px; font-weight: var(--dt-font-weight-bold); color: var(--dt-text-primary); display: block; line-height: 1; }
    .month { font-size: 12px; color: var(--dt-text-tertiary); margin-top: 4px; display: block; }
  }

  .flow-card {
    flex: 1; background: var(--dt-bg-card); border: 1px solid var(--dt-border-light); border-radius: var(--dt-radius-lg);
    padding: 16px 20px; transition: var(--dt-transition-fast);
    &:hover { border-color: var(--dt-brand-color); box-shadow: var(--dt-shadow-2); }

    .card-header {
      display: flex; justify-content: space-between; margin-bottom: 12px;
      .type-tag { font-size: 12px; font-weight: var(--dt-font-weight-semibold); color: var(--dt-brand-color); background: var(--dt-brand-bg); padding: 2px 8px; border-radius: var(--dt-radius-sm); }
      .status-tag { font-size: 12px; &.APPROVED { color: var(--dt-success-color); } }
    }
    .card-body-text { font-size: 14px; color: var(--dt-text-primary); line-height: 1.6; @include text-ellipsis-multiline(2); margin-bottom: 16px; }
    .card-footer {
      display: flex; justify-content: space-between; color: var(--dt-text-tertiary); font-size: 12px;
      .footer-left, .footer-right { display: flex; align-items: center; gap: 6px; }
      .footer-right { gap: 16px; }
    }
  }
}
</style>
