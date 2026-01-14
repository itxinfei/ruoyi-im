<template>
  <div class="approval-container">
    <div class="approval-header">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待我审批" name="pending">
          <template #label>
            <span>
              待我审批
              <el-badge v-if="pendingCount > 0" :value="pendingCount" />
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="我发起的" name="my" />
        <el-tab-pane label="我已审批" name="processed" />
      </el-tabs>
      <el-button type="primary" :icon="Plus" @click="showCreateDialog">发起审批</el-button>
    </div>

    <!-- 审批列表 -->
    <div class="approval-list">
      <div
        v-for="approval in approvalList"
        :key="approval.id"
        class="approval-item"
        @click="handleViewDetail(approval)"
      >
        <div class="approval-icon">
          <el-icon :color="getStatusColor(approval.status)">
            <component :is="getStatusIcon(approval.status)" />
          </el-icon>
        </div>
        <div class="approval-content">
          <div class="approval-title">{{ approval.title }}</div>
          <div class="approval-info">
            <span class="info-item">
              <el-icon><User /></el-icon>
              申请人：{{ approval.applicantName || '我' }}
            </span>
            <span class="info-item">
              <el-icon><Clock /></el-icon>
              {{ approval.createTime }}
            </span>
          </div>
        </div>
        <div class="approval-status">
          <el-tag :type="getStatusType(approval.status)">
            {{ getStatusText(approval.status) }}
          </el-tag>
        </div>
        <div v-if="activeTab === 'pending'" class="approval-actions">
          <el-button type="success" size="small" @click.stop="handleApprove(approval)">
            通过
          </el-button>
          <el-button type="danger" size="small" @click.stop="handleReject(approval)">
            驳回
          </el-button>
        </div>
      </div>
      <el-empty v-if="approvalList.length === 0" description="暂无数据" :image-size="100" />
    </div>

    <!-- 发起审批对话框 -->
    <el-dialog v-model="createDialogVisible" title="发起审批" width="600px">
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="审批模板">
          <el-select
            v-model="approvalForm.templateId"
            placeholder="请选择审批模板"
            style="width: 100%"
          >
            <el-option
              v-for="template in templates"
              :key="template.id"
              :label="template.name"
              :value="template.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="审批标题">
          <el-input v-model="approvalForm.title" placeholder="请输入审批标题" />
        </el-form-item>
        <el-form-item label="表单数据">
          <el-input
            v-model="approvalForm.formData"
            type="textarea"
            :rows="4"
            placeholder="请输入表单数据（JSON格式）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">提交</el-button>
      </template>
    </el-dialog>

    <!-- 审批意见对话框 -->
    <el-dialog v-model="commentDialogVisible" :title="commentTitle" width="500px">
      <el-input v-model="commentText" type="textarea" :rows="4" placeholder="请输入审批意见" />
      <template #footer>
        <el-button @click="commentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitComment">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Clock,
  User,
  Warning,
  CircleCheck,
  CircleClose,
  Loading,
} from '@element-plus/icons-vue'
import {
  getPendingApprovals,
  getMyApprovals,
  getProcessedApprovals,
  createApproval,
  approveApproval,
  rejectApproval,
  cancelApproval,
  getActiveTemplates,
} from '@/api/im/approval'

const router = useRouter()
const activeTab = ref('pending')
const pendingCount = ref(0)
const approvalList = ref([])
const templates = ref([])
const createDialogVisible = ref(false)
const commentDialogVisible = ref(false)
const commentTitle = ref('')
const commentText = ref('')
const currentApproval = ref(null)

const approvalForm = reactive({
  templateId: null,
  title: '',
  formData: '{}',
})

const loadApprovalList = async () => {
  try {
    let response
    if (activeTab.value === 'pending') {
      response = await getPendingApprovals()
      pendingCount.value = response.data?.length || 0
    } else if (activeTab.value === 'my') {
      response = await getMyApprovals()
    } else {
      response = await getProcessedApprovals()
    }
    if (response.code === 200) {
      approvalList.value = response.data || []
    }
  } catch (error) {
    console.error('获取审批列表失败:', error)
    approvalList.value = []
  }
}

const loadTemplates = async () => {
  try {
    const response = await getActiveTemplates()
    if (response.code === 200) {
      templates.value = response.data || []
    }
  } catch (error) {
    console.error('获取模板列表失败:', error)
  }
}

const handleTabChange = () => {
  loadApprovalList()
}

const showCreateDialog = () => {
  approvalForm.templateId = null
  approvalForm.title = ''
  approvalForm.formData = '{}'
  createDialogVisible.value = true
}

const handleCreate = async () => {
  if (!approvalForm.templateId || !approvalForm.title) {
    ElMessage.warning('请完善审批信息')
    return
  }
  try {
    let formData
    try {
      formData = JSON.parse(approvalForm.formData)
    } catch {
      formData = {}
    }
    await createApproval({
      templateId: approvalForm.templateId,
      title: approvalForm.title,
      formData,
    })
    ElMessage.success('提交成功')
    createDialogVisible.value = false
    loadApprovalList()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const handleViewDetail = approval => {
  router.push({ path: `/im/approval/detail/${approval.id}` })
}

const handleApprove = approval => {
  currentApproval.value = approval
  commentTitle.value = '通过审批'
  commentText.value = ''
  commentDialogVisible.value = true
}

const handleReject = approval => {
  currentApproval.value = approval
  commentTitle.value = '驳回审批'
  commentText.value = ''
  commentDialogVisible.value = true
}

const handleSubmitComment = async () => {
  if (!currentApproval.value) return

  const isApprove = commentTitle.value === '通过审批'
  if (!isApprove && !commentText.value.trim()) {
    ElMessage.warning('请输入驳回理由')
    return
  }

  try {
    if (isApprove) {
      await approveApproval(currentApproval.value.id, commentText.value)
      ElMessage.success('已通过')
    } else {
      await rejectApproval(currentApproval.value.id, commentText.value)
      ElMessage.success('已驳回')
    }
    commentDialogVisible.value = false
    loadApprovalList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getStatusType = status => {
  const map = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info',
  }
  return map[status] || 'info'
}

const getStatusText = status => {
  const map = {
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    CANCELLED: '已撤回',
  }
  return map[status] || status
}

const getStatusIcon = status => {
  const map = {
    PENDING: Loading,
    APPROVED: CircleCheck,
    REJECTED: CircleClose,
    CANCELLED: Warning,
  }
  return map[status] || Warning
}

const getStatusColor = status => {
  const map = {
    PENDING: '#faad14',
    APPROVED: '#52c41a',
    REJECTED: '#ff4d4f',
    CANCELLED: '#8c8c8c',
  }
  return map[status] || '#8c8c8c'
}

onMounted(() => {
  loadApprovalList()
  loadTemplates()
})
</script>

<style lang="scss" scoped>
.approval-container {
  padding: 16px; // 修改：20px -> 16px（符合4的倍数）
  background: #F5F7FA; // 钉钉规范
  min-height: calc(100vh - 60px);
}

.approval-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  background: #fff;
  padding: 16px; // 修改：16px 20px -> 16px
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04); // 修改：0.06 -> 0.04
}

.approval-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.approval-item {
  display: flex;
  align-items: center;
  padding: 16px; // 修改：16px 20px -> 16px
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08); // 修改：0.1 -> 0.08
  }
}

.approval-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: #F5F7FA;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.approval-content {
  flex: 1;
}

.approval-title {
  font-size: 16px; // 修改：15px -> 16px（小标题规范）
  font-weight: 500;
  color: #262626; // 标题色
  margin-bottom: 8px;
}

.approval-info {
  display: flex;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px; // 修改：4px -> 8px（符合4的倍数）
  font-size: 14px; // 修改：13px -> 14px（正文规范）
  color: #999999; // 辅助文字
}

.approval-status {
  margin-right: 16px;
}

.approval-actions {
  display: flex;
  gap: 8px;
}

:deep(.el-tabs__header) {
  margin: 0;
}

:deep(.el-tabs__nav-wrap::after) {
  display: none;
}
</style>
