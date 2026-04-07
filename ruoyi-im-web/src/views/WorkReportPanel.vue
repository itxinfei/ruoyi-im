<template>
  <div class="work-report-panel">
    <!-- 顶部标题栏 -->
    <div class="panel-header-bar">
      <div class="header-title">
        <h2>工作日志</h2>
        <span class="header-sub">记录每日工作，总结成长轨迹</span>
      </div>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        写日志
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="12" class="stats-row">
      <el-col :xs="24" :sm="8">
        <div class="stat-card">
          <div class="stat-icon today"><el-icon><Calendar /></el-icon></div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.todayCount || 0 }}</span>
            <span class="stat-label">今日日志</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-card">
          <div class="stat-icon week"><el-icon><Clock /></el-icon></div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.weekCount || 0 }}</span>
            <span class="stat-label">本周日志</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-card">
          <div class="stat-icon pending"><el-icon><Stamp /></el-icon></div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.pendingApproval || 0 }}</span>
            <span class="stat-label">待我审批</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="report-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="我的日志" name="my" />
      <el-tab-pane label="我发起的" name="submitted" />
      <el-tab-pane label="待我审批" name="pending" />
      <el-tab-pane label="已审批" name="approved" />
    </el-tabs>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索工作内容"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
        style="max-width: 320px;"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch" />
        </template>
      </el-input>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        @change="handleSearch"
        style="width: 260px;"
      />
    </div>

    <!-- 日志列表 -->
    <div v-loading="loading" class="report-list">
      <div v-if="!reportList.length && !loading" class="empty-state">
        <el-icon class="empty-icon"><Document /></el-icon>
        <p>暂无日志记录</p>
        <el-button type="primary" plain @click="openCreateDialog">写第一篇日志</el-button>
      </div>

      <div
        v-for="report in reportList"
        :key="report.id"
        class="report-card"
        @click="openDetail(report)"
      >
        <div class="card-left">
          <div class="card-date">
            <span class="date-day">{{ formatDay(report.reportDate) }}</span>
            <span class="date-month">{{ formatMonth(report.reportDate) }}</span>
          </div>
        </div>
        <div class="card-body">
          <div class="card-header-row">
            <span class="report-type-tag">{{ report.reportTypeName || report.reportType }}</span>
            <span class="report-status" :class="'status-' + report.status">
              {{ report.statusName || report.status }}
            </span>
          </div>
          <p class="card-content">{{ report.workContent || '暂无工作内容' }}</p>
          <div class="card-meta">
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ report.submitTime || report.createTime }}
            </span>
            <span v-if="report.workHours" class="meta-item">
              <el-icon><Timer /></el-icon>
              {{ report.workHours }}h
            </span>
            <span class="meta-item">
              <el-icon><ChatDotRound /></el-icon>
              {{ report.commentCount || 0 }}
            </span>
            <span class="meta-item">
              <el-icon><Star /></el-icon>
              {{ report.likeCount || 0 }}
            </span>
          </div>
          <div v-if="report.visibility" class="card-visibility">
            <el-tag size="small" effect="plain">{{ report.visibilityName || report.visibility }}</el-tag>
          </div>
        </div>
        <div class="card-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pager-wrap">
      <el-pagination
        :current-page="pageNum"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 创建/编辑日志弹窗 -->
    <el-dialog
      v-model="createDialogVisible"
      :title="editingReport ? '编辑日志' : '写日志'"
      width="640px"
      append-to-body
      destroy-on-close
    >
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="日志类型" prop="reportType">
              <el-select v-model="createForm.reportType" placeholder="请选择类型" style="width: 100%">
                <el-option label="日报" value="DAILY" />
                <el-option label="周报" value="WEEKLY" />
                <el-option label="月报" value="MONTHLY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作日期" prop="reportDate">
              <el-date-picker
                v-model="createForm.reportDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="工作内容" prop="workContent">
          <el-input
            v-model="createForm.workContent"
            type="textarea"
            :rows="5"
            placeholder="今日完成了哪些工作？遇到了什么问题？如何解决的？"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="明日计划">
          <el-input
            v-model="createForm.tomorrowPlan"
            type="textarea"
            :rows="3"
            placeholder="明天的工作计划是什么？"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工作时长 (小时)">
              <el-input-number
                v-model="createForm.workHours"
                :min="0"
                :max="24"
                :precision="1"
                :step="0.5"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可见范围">
              <el-select v-model="createForm.visibility" placeholder="选择可见范围" style="width: 100%">
                <el-option label="仅自己" value="PRIVATE" />
                <el-option label="本部门" value="DEPARTMENT" />
                <el-option label="全公司" value="PUBLIC" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注问题">
          <el-input
            v-model="createForm.issues"
            type="textarea"
            :rows="2"
            placeholder="遇到的问题或需要协调的事项（可选）"
            maxlength="300"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitReport">提交日志</el-button>
      </template>
    </el-dialog>

    <!-- 日志详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="日志详情"
      width="680px"
      append-to-body
      destroy-on-close
    >
      <div v-if="currentReport" class="report-detail">
        <!-- 基本信息 -->
        <div class="detail-header">
          <div class="detail-meta">
            <el-tag :type="getReportTypeTag(currentReport.reportType)">
              {{ currentReport.reportTypeName || currentReport.reportType }}
            </el-tag>
            <el-tag :type="getStatusTag(currentReport.status)">
              {{ currentReport.statusName || currentReport.status }}
            </el-tag>
            <span class="detail-date">{{ currentReport.reportDate }}</span>
            <span v-if="currentReport.workHours" class="detail-hours">
              <el-icon><Timer /></el-icon> {{ currentReport.workHours }}h
            </span>
          </div>
          <div class="detail-actions">
            <template v-if="activeTab === 'my' || activeTab === 'submitted'">
              <el-button v-if="currentReport.status === 'DRAFT'" type="primary" size="small" @click="handleEditReport">编辑</el-button>
              <el-button v-if="currentReport.status === 'DRAFT'" type="primary" size="small" @click="handleSubmitReport">提交</el-button>
              <el-button v-if="currentReport.status === 'DRAFT'" type="danger" size="small" @click="handleDeleteReport">删除</el-button>
            </template>
            <template v-if="activeTab === 'pending'">
              <el-button type="success" size="small" @click="handleApprove(true)">通过</el-button>
              <el-button type="danger" size="small" @click="handleApprove(false)">退回</el-button>
            </template>
          </div>
        </div>

        <!-- 撰写人 -->
        <div class="detail-author">
          <DingtalkAvatar
            :src="currentReport.userAvatar"
            :name="currentReport.userName"
            :size="40"
            shape="circle"
          />
          <div class="author-info">
            <span class="author-name">{{ currentReport.userName }}</span>
            <span class="author-dept">{{ currentReport.deptName || '—' }}</span>
          </div>
          <el-tag v-if="currentReport.visibility" size="small" effect="plain">
            {{ currentReport.visibilityName || currentReport.visibility }}
          </el-tag>
        </div>

        <!-- 工作内容 -->
        <div class="detail-section">
          <div class="section-label"><el-icon><Document /></el-icon> 工作内容</div>
          <div class="section-content">{{ currentReport.workContent || '暂无' }}</div>
        </div>

        <!-- 明日计划 -->
        <div v-if="currentReport.tomorrowPlan" class="detail-section">
          <div class="section-label"><el-icon><Timer /></el-icon> 明日计划</div>
          <div class="section-content">{{ currentReport.tomorrowPlan }}</div>
        </div>

        <!-- 备注问题 -->
        <div v-if="currentReport.issues" class="detail-section">
          <div class="section-label"><el-icon><Warning /></el-icon> 备注问题</div>
          <div class="section-content">{{ currentReport.issues }}</div>
        </div>

        <!-- 审批信息 -->
        <div v-if="currentReport.approverName" class="detail-section approval-info">
          <div class="section-label"><el-icon><Stamp /></el-icon> 审批结果</div>
          <div class="approval-content">
            <span class="approval-result" :class="currentReport.status === 'APPROVED' ? 'success' : 'danger'">
              {{ currentReport.status === 'APPROVED' ? '已通过' : '已退回' }}
            </span>
            <span class="approval-by">by {{ currentReport.approverName }}</span>
            <span v-if="currentReport.approveTime" class="approval-time">{{ currentReport.approveTime }}</span>
            <span v-if="currentReport.approveRemark" class="approval-remark">备注：{{ currentReport.approveRemark }}</span>
          </div>
        </div>

        <!-- 点赞和评论 -->
        <div class="detail-interactions">
          <div class="interaction-item" :class="{ active: currentReport.isLiked }" @click="handleToggleLike">
            <el-icon><Star /></el-icon>
            <span>{{ currentReport.isLiked ? '取消点赞' : '点赞' }} ({{ currentReport.likeCount || 0 }})</span>
          </div>
          <div class="interaction-item" @click="focusComment">
            <el-icon><ChatDotRound /></el-icon>
            <span>评论 ({{ currentReport.commentCount || 0 }})</span>
          </div>
        </div>

        <!-- 点赞用户 -->
        <div v-if="currentReport.likeUsers?.length" class="like-users">
          <span class="like-label">点赞：</span>
          <div class="like-avatars">
            <DingtalkAvatar
              v-for="u in currentReport.likeUsers"
              :key="u.userId"
              :src="u.userAvatar"
              :name="u.userName"
              :size="24"
              shape="circle"
              :title="u.userName"
            />
          </div>
        </div>

        <!-- 评论列表 -->
        <div v-if="currentReport.comments?.length" class="comment-list">
          <div v-for="comment in currentReport.comments" :key="comment.id" class="comment-item">
            <DingtalkAvatar :src="comment.userAvatar" :name="comment.userName" :size="32" shape="circle" />
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-author">{{ comment.userName }}</span>
                <span class="comment-time">{{ comment.createTime }}</span>
              </div>
              <div v-if="comment.parentContent" class="comment-quote">回复：{{ comment.parentContent }}</div>
              <div class="comment-content">{{ comment.content }}</div>
            </div>
          </div>
        </div>

        <!-- 评论输入 -->
        <div class="comment-input-area">
          <el-input
            ref="commentInputRef"
            v-model="commentText"
            placeholder="写下你的评论..."
            type="textarea"
            :rows="2"
            maxlength="200"
            show-word-limit
          />
          <el-button type="primary" size="small" :loading="commentLoading" @click="handleAddComment">
            发送
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Calendar, Clock, Stamp, Search, Document, ChatDotRound,
  Star, Timer, Warning, ArrowRight
} from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import {
  getWorkReportPage,
  getWorkReportStatistics,
  createWorkReport,
  updateWorkReport,
  deleteWorkReport,
  submitWorkReport,
  getWorkReportDetail,
  getMyReports,
  addWorkReportComment,
  toggleWorkReportLike,
  approveWorkReport
} from '@/api/im/workReport'

const loading = ref(false)
const activeTab = ref('my')
const searchKeyword = ref('')
const dateRange = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const reportList = ref([])
const stats = ref({})
const currentReportId = ref(null)
const currentReport = ref(null)
const detailDialogVisible = ref(false)
const createDialogVisible = ref(false)
const editingReport = ref(null)
const submitting = ref(false)
const commentText = ref('')
const commentLoading = ref(false)
const commentInputRef = ref(null)

const createFormRef = ref(null)
const createForm = ref({
  reportType: 'DAILY',
  reportDate: '',
  workContent: '',
  tomorrowPlan: '',
  issues: '',
  workHours: null,
  visibility: 'DEPARTMENT'
})

const createRules = {
  reportType: [{ required: true, message: '请选择日志类型', trigger: 'change' }],
  reportDate: [{ required: true, message: '请选择工作日期', trigger: 'change' }],
  workContent: [{ required: true, message: '请填写工作内容', trigger: 'blur' }]
}

const getReportTypeTag = (type) => {
  const map = { DAILY: '', WEEKLY: 'success', MONTHLY: 'warning' }
  return map[type] || ''
}

const getStatusTag = (status) => {
  const map = { DRAFT: 'info', SUBMITTED: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || ''
}

const formatDay = (dateStr) => {
  if (!dateStr) return '--'
  const parts = dateStr.split('-')
  return parts[parts.length - 1]
}

const formatMonth = (dateStr) => {
  if (!dateStr) return ''
  const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
  const parts = dateStr.split('-')
  if (parts.length >= 2) {
    const m = parseInt(parts[1], 10)
    return months[m - 1] || ''
  }
  return ''
}

const loadStats = async () => {
  try {
    const res = await getWorkReportStatistics()
    if (res.code === 200) {
      stats.value = res.data || {}
    }
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const loadReports = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value,
      reportType: ''
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    let res
    if (activeTab.value === 'my') {
      params.status = ''
      res = await getWorkReportPage(params)
    } else if (activeTab.value === 'submitted') {
      params.status = 'SUBMITTED'
      res = await getWorkReportPage(params)
    } else if (activeTab.value === 'pending') {
      params.status = 'PENDING_APPROVAL'
      res = await getWorkReportPage(params)
    } else {
      params.status = 'APPROVED,REJECTED'
      res = await getWorkReportPage(params)
    }

    if (res.code === 200) {
      reportList.value = res.data?.records || res.data?.list || []
      total.value = res.data?.total || 0
    }
  } catch (e) {
    ElMessage.error('加载日志列表失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  pageNum.value = 1
  loadReports()
}

const handleSearch = () => {
  pageNum.value = 1
  loadReports()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadReports()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadReports()
}

const openCreateDialog = () => {
  editingReport.value = null
  createForm.value = {
    reportType: 'DAILY',
    reportDate: new Date().toISOString().split('T')[0],
    workContent: '',
    tomorrowPlan: '',
    issues: '',
    workHours: null,
    visibility: 'DEPARTMENT'
  }
  createDialogVisible.value = true
}

const openDetail = async (report) => {
  currentReportId.value = report.id
  try {
    const res = await getWorkReportDetail(report.id)
    if (res.code === 200) {
      currentReport.value = res.data
      detailDialogVisible.value = true
    } else {
      throw new Error(res.message)
    }
  } catch (e) {
    ElMessage.error('加载日志详情失败')
  }
}

const handleEditReport = () => {
  if (!currentReport.value) return
  editingReport.value = currentReport.value
  createForm.value = {
    reportType: currentReport.value.reportType || 'DAILY',
    reportDate: currentReport.value.reportDate || '',
    workContent: currentReport.value.workContent || '',
    tomorrowPlan: currentReport.value.tomorrowPlan || '',
    issues: currentReport.value.issues || '',
    workHours: currentReport.value.workHours || null,
    visibility: currentReport.value.visibility || 'DEPARTMENT'
  }
  detailDialogVisible.value = false
  createDialogVisible.value = true
}

const validateForm = async () => {
  if (!createFormRef.value) return false
  try {
    await createFormRef.value.validate()
    return true
  } catch {
    return false
  }
}

const handleSaveDraft = async () => {
  const valid = await validateForm()
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      ...createForm.value,
      isDraft: true
    }
    let res
    if (editingReport.value) {
      res = await updateWorkReport(editingReport.value.id, data)
    } else {
      res = await createWorkReport(data)
    }
    if (res.code === 200) {
      ElMessage.success('草稿已保存')
      createDialogVisible.value = false
      loadReports()
      loadStats()
    } else {
      throw new Error(res.message)
    }
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const handleSubmitReport = async () => {
  const valid = await validateForm()
  if (!valid) return

  // 如果是草稿提交
  if (editingReport.value && editingReport.value.status === 'DRAFT') {
    submitting.value = true
    try {
      const res = await submitWorkReport(editingReport.value.id)
      if (res.code === 200) {
        ElMessage.success('日志已提交')
        createDialogVisible.value = false
        loadReports()
        loadStats()
      } else {
        throw new Error(res.message)
      }
    } catch (e) {
      ElMessage.error(e.message || '提交失败')
    } finally {
      submitting.value = false
    }
    return
  }

  submitting.value = true
  try {
    const data = {
      ...createForm.value,
      isDraft: false
    }
    let res
    if (editingReport.value) {
      res = await updateWorkReport(editingReport.value.id, data)
    } else {
      res = await createWorkReport(data)
    }
    if (res.code === 200) {
      ElMessage.success('日志已提交')
      createDialogVisible.value = false
      loadReports()
      loadStats()
    } else {
      throw new Error(res.message)
    }
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const handleDeleteReport = async () => {
  if (!currentReportId.value) return
  try {
    await ElMessageBox.confirm('确定要删除这条日志吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteWorkReport(currentReportId.value)
    if (res.code === 200) {
      ElMessage.success('已删除')
      detailDialogVisible.value = false
      loadReports()
      loadStats()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleApprove = async (approved) => {
  if (!currentReportId.value) return
  try {
    const remark = ''
    const res = await approveWorkReport(currentReportId.value, approved, remark)
    if (res.code === 200) {
      ElMessage.success(approved ? '已通过' : '已退回')
      detailDialogVisible.value = false
      loadReports()
      loadStats()
    } else {
      throw new Error(res.message)
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleToggleLike = async () => {
  if (!currentReportId.value) return
  try {
    const res = await toggleWorkReportLike(currentReportId.value)
    if (res.code === 200) {
      currentReport.value.isLiked = res.data
      currentReport.value.likeCount = (currentReport.value.likeCount || 0) + (res.data ? 1 : -1)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const focusComment = () => {
  commentInputRef.value?.focus()
}

const handleAddComment = async () => {
  if (!commentText.value.trim() || !currentReportId.value) return
  commentLoading.value = true
  try {
    const res = await addWorkReportComment(currentReportId.value, commentText.value)
    if (res.code === 200) {
      ElMessage.success('评论成功')
      commentText.value = ''
      // 刷新详情
      const detailRes = await getWorkReportDetail(currentReportId.value)
      if (detailRes.code === 200) {
        currentReport.value = detailRes.data
      }
    } else {
      throw new Error(res.message)
    }
  } catch (e) {
    ElMessage.error(e.message || '评论失败')
  } finally {
    commentLoading.value = false
  }
}

onMounted(() => {
  loadReports()
  loadStats()
})
</script>

<style scoped lang="scss">
.work-report-panel {
  height: 100%;
  background: var(--dt-bg-body);
  padding: var(--dt-spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-md);
}

.panel-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .header-title {
    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 700;
      color: var(--dt-text-primary);
    }

    .header-sub {
      font-size: 13px;
      color: var(--dt-text-secondary);
      margin-left: 12px;
    }
  }
}

.stats-row {
  .stat-card {
    background: var(--dt-bg-card);
    border: 1px solid var(--dt-border-light);
    border-radius: var(--dt-radius-xl);
    padding: var(--dt-spacing-md) var(--dt-spacing-lg);
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-md);

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: var(--dt-radius-lg);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22px;

      &.today { background: rgba(64, 158, 255, 0.1); color: var(--dt-brand-color); }
      &.week { background: rgba(103, 194, 58, 0.1); color: var(--dt-success-color); }
      &.pending { background: rgba(230, 162, 60, 0.1); color: var(--dt-warning-color); }
    }

    .stat-info {
      display: flex;
      flex-direction: column;

      .stat-value {
        font-size: 24px;
        font-weight: 700;
        color: var(--dt-text-primary);
        line-height: 1.2;
      }

      .stat-label {
        font-size: 13px;
        color: var(--dt-text-secondary);
      }
    }
  }
}

.report-tabs {
  :deep(.el-tabs__header) {
    margin: 0;
  }
}

.search-bar {
  display: flex;
  gap: var(--dt-spacing-sm);
  align-items: center;
  flex-wrap: wrap;
}

.report-list {
  flex: 1;
  overflow-y: auto;
  min-height: 200px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-quaternary);

  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }

  p {
    margin-bottom: 16px;
    font-size: 14px;
  }
}

.report-card {
  display: flex;
  align-items: center;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-xl);
  padding: var(--dt-spacing-md) var(--dt-spacing-lg);
  margin-bottom: var(--dt-spacing-sm);
  cursor: pointer;
  transition: border-color var(--dt-transition-fast), box-shadow var(--dt-transition-fast);
  gap: var(--dt-spacing-md);

  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: var(--dt-shadow-2);
  }

  .card-left {
    .card-date {
      display: flex;
      flex-direction: column;
      align-items: center;
      min-width: 48px;

      .date-day {
        font-size: 28px;
        font-weight: 700;
        color: var(--dt-text-primary);
        line-height: 1;
      }

      .date-month {
        font-size: 12px;
        color: var(--dt-text-secondary);
      }
    }
  }

  .card-body {
    flex: 1;
    min-width: 0;

    .card-header-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 6px;

      .report-type-tag {
        font-size: 12px;
        color: var(--dt-brand-color);
        font-weight: 600;
      }

      .report-status {
        font-size: var(--dt-font-size-xs);
        padding: var(--dt-spacing-xs) var(--dt-spacing-sm);
        border-radius: var(--dt-radius-lg);
        font-weight: 500;

        &.status-DRAFT { background: var(--dt-bg-body); color: var(--dt-text-secondary); }
        &.status-SUBMITTED { background: rgba(230, 162, 60, 0.15); color: var(--dt-warning-color); }
        &.status-PENDING_APPROVAL { background: rgba(230, 162, 60, 0.15); color: var(--dt-warning-color); }
        &.status-APPROVED { background: rgba(103, 194, 58, 0.15); color: var(--dt-success-color); }
        &.status-REJECTED { background: rgba(245, 108, 108, 0.15); color: var(--dt-error-color); }
      }
    }

    .card-content {
      margin: 0 0 8px;
      font-size: 14px;
      color: var(--dt-text-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .card-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 3px;
        font-size: 12px;
        color: var(--dt-text-quaternary);

        .el-icon {
          font-size: 12px;
        }
      }
    }

    .card-visibility {
      margin-top: 6px;
    }
  }

  .card-arrow {
    color: var(--dt-text-quaternary);
    font-size: 16px;
  }
}

.pager-wrap {
  display: flex;
  justify-content: flex-end;
  padding-top: var(--dt-spacing-sm);
}

/* 详情弹窗 */
.report-detail {
  .detail-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: var(--dt-spacing-md);

    .detail-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;

      .detail-date {
        font-size: 14px;
        color: var(--dt-text-secondary);
      }

      .detail-hours {
        display: flex;
        align-items: center;
        gap: 3px;
        font-size: 13px;
        color: var(--dt-text-secondary);
      }
    }

    .detail-actions {
      display: flex;
      gap: 8px;
    }
  }

  .detail-author {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: var(--dt-spacing-md);
    background: var(--dt-bg-body);
    border-radius: var(--dt-radius-lg);
    margin-bottom: var(--dt-spacing-lg);

    .author-info {
      flex: 1;
      display: flex;
      flex-direction: column;

      .author-name {
        font-weight: 600;
        font-size: 15px;
        color: var(--dt-text-primary);
      }

      .author-dept {
        font-size: 12px;
        color: var(--dt-text-secondary);
      }
    }
  }

  .detail-section {
    margin-bottom: var(--dt-spacing-md);

    .section-label {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      font-weight: 600;
      color: var(--dt-text-secondary);
      margin-bottom: 8px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .section-content {
      font-size: 14px;
      color: var(--dt-text-primary);
      line-height: 1.6;
      white-space: pre-wrap;
      background: var(--dt-bg-body);
      border-radius: var(--dt-radius-md);
      padding: var(--dt-spacing-md);
    }

    &.approval-info .approval-content {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;
      font-size: 14px;

      .approval-result {
        font-weight: 600;
        &.success { color: var(--dt-success-color); }
        &.danger { color: var(--dt-error-color); }
      }

      .approval-by, .approval-time {
        color: var(--dt-text-secondary);
        font-size: 13px;
      }

      .approval-remark {
        color: var(--dt-text-secondary);
        font-size: 13px;
        font-style: italic;
      }
    }
  }

  .detail-interactions {
    display: flex;
    gap: var(--dt-spacing-lg);
    padding: var(--dt-spacing-md) 0;
    border-top: 1px solid var(--dt-border-light);
    border-bottom: 1px solid var(--dt-border-light);
    margin: var(--dt-spacing-md) 0;

    .interaction-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: var(--dt-text-secondary);
      cursor: pointer;
      transition: color 0.2s;

      &:hover, &.active {
        color: var(--dt-brand-color);
      }
    }
  }

  .like-users {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: var(--dt-spacing-md);

    .like-label {
      font-size: 13px;
      color: var(--dt-text-secondary);
    }

    .like-avatars {
      display: flex;
      gap: 4px;
    }
  }

  .comment-list {
    margin-bottom: var(--dt-spacing-md);

    .comment-item {
      display: flex;
      gap: var(--dt-spacing-sm);
      margin-bottom: var(--dt-spacing-md);

      .comment-body {
        flex: 1;
        background: var(--dt-bg-body);
        border-radius: var(--dt-radius-md);
        padding: var(--dt-spacing-sm) var(--dt-spacing-md);

        .comment-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 4px;

          .comment-author {
            font-weight: 600;
            font-size: 13px;
            color: var(--dt-text-primary);
          }

          .comment-time {
            font-size: 12px;
            color: var(--dt-text-quaternary);
          }
        }

        .comment-quote {
          font-size: 12px;
          color: var(--dt-text-secondary);
          background: rgba(0, 0, 0, 0.04);
          border-left: 2px solid var(--dt-border-color);
          padding: 2px 8px;
          margin-bottom: 4px;
          border-radius: 0 4px 4px 0;
        }

        .comment-content {
          font-size: 14px;
          color: var(--dt-text-primary);
          line-height: 1.5;
        }
      }
    }
  }

  .comment-input-area {
    display: flex;
    gap: 8px;
    align-items: flex-end;

    .el-textarea {
      flex: 1;
    }
  }
}
</style>
