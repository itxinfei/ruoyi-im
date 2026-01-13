<template>
  <div class="approval-detail-container">
    <div class="detail-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div v-loading="loading" class="detail-content">
      <!-- 审批状态 -->
      <div class="status-card" :class="'status-' + detail.approval?.status?.toLowerCase()">
        <el-icon :size="48" :color="getStatusColor(detail.approval?.status)">
          <component :is="getStatusIcon(detail.approval?.status)" />
        </el-icon>
        <div class="status-text">{{ getStatusText(detail.approval?.status) }}</div>
      </div>

      <!-- 审批信息 -->
      <div class="info-card">
        <div class="card-title">审批信息</div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="审批标题">
            {{ detail.approval?.title }}
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(detail.approval?.status)">
              {{ getStatusText(detail.approval?.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请人">
            {{ detail.approval?.applicantName || '我' }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ detail.approval?.createTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 表单数据 -->
      <div v-if="detail.formData?.length > 0" class="info-card">
        <div class="card-title">表单内容</div>
        <el-form label-width="120px" class="form-display">
          <el-form-item
            v-for="item in detail.formData"
            :key="item.id"
            :label="item.fieldLabel || item.fieldName"
          >
            <span>{{ item.fieldValue }}</span>
          </el-form-item>
        </el-form>
      </div>

      <!-- 审批流程 -->
      <div class="info-card">
        <div class="card-title">审批流程</div>
        <el-steps direction="vertical" :active="getCurrentStep()">
          <el-step
            v-for="node in detail.nodes"
            :key="node.id"
            :title="node.nodeName"
            :status="getNodeStatus(node.status)"
            :description="getNodeDescription(node)"
          />
        </el-steps>
      </div>

      <!-- 审批记录 -->
      <div v-if="detail.records?.length > 0" class="info-card">
        <div class="card-title">审批记录</div>
        <el-timeline>
          <el-timeline-item
            v-for="record in detail.records"
            :key="record.id"
            :timestamp="record.actionTime"
            placement="top"
          >
            <div class="record-item">
              <div class="record-action">
                <el-tag :type="record.action === 'APPROVE' ? 'success' : 'danger'" size="small">
                  {{ record.action === 'APPROVE' ? '通过' : '驳回' }}
                </el-tag>
              </div>
              <div v-if="record.comment" class="record-comment">
                {{ record.comment }}
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 操作按钮 -->
      <div v-if="detail.approval?.status === 'PENDING' && isApprover" class="action-buttons">
        <el-button type="success" :icon="Select" @click="handleApprove">通过</el-button>
        <el-button type="danger" :icon="Close" @click="handleReject">驳回</el-button>
        <el-button :icon="Promotion" @click="handleTransfer">转交</el-button>
        <el-button :icon="Share" @click="handleDelegate">委托</el-button>
      </div>
      <div v-if="detail.approval?.status === 'PENDING' && isApplicant" class="action-buttons">
        <el-button :icon="Close" @click="handleCancel">撤回</el-button>
      </div>
    </div>

    <!-- 转交审批对话框 -->
    <el-dialog v-model="transferDialogVisible" title="转交审批" width="450px">
      <el-form label-width="80px">
        <el-form-item label="转交给">
          <el-select
            v-model="transferTargetUserId"
            filterable
            placeholder="选择转交对象"
            style="width: 100%"
          >
            <el-option
              v-for="user in availableUsers"
              :key="user.userId"
              :label="user.nickName || user.userName"
              :value="user.userId"
            >
              <span>{{ user.nickName || user.userName }}</span>
              <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
                {{ user.dept?.deptName || '' }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="transferRemark" type="textarea" :rows="3" placeholder="请输入转交备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitTransfer">确定</el-button>
      </template>
    </el-dialog>

    <!-- 委托审批对话框 -->
    <el-dialog v-model="delegateDialogVisible" title="委托审批" width="450px">
      <el-form label-width="80px">
        <el-form-item label="委托给">
          <el-select
            v-model="delegateTargetUserId"
            filterable
            placeholder="选择委托对象"
            style="width: 100%"
          >
            <el-option
              v-for="user in availableUsers"
              :key="user.userId"
              :label="user.nickName || user.userName"
              :value="user.userId"
            >
              <span>{{ user.nickName || user.userName }}</span>
              <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
                {{ user.dept?.deptName || '' }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="delegateRemark" type="textarea" :rows="3" placeholder="请输入委托备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="delegateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitDelegate">确定</el-button>
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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Select,
  Close,
  CircleCheck,
  CircleClose,
  Warning,
  Loading,
  Promotion,
  Share,
} from '@element-plus/icons-vue'
import {
  getApprovalDetail,
  approveApproval,
  rejectApproval,
  cancelApproval,
  transferApproval,
  delegateApproval,
} from '@/api/im/approval'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref({
  approval: null,
  template: null,
  formData: [],
  nodes: [],
  records: [],
})
const commentDialogVisible = ref(false)
const commentTitle = ref('')
const commentText = ref('')

// 转交和委托相关
const transferDialogVisible = ref(false)
const transferTargetUserId = ref(null)
const transferRemark = ref('')
const delegateDialogVisible = ref(false)
const delegateTargetUserId = ref(null)
const delegateRemark = ref('')
// 可转交/委托的用户列表（实际应从API获取）
const availableUsers = ref([])

const isApprover = computed(() => {
  // TODO: 判断当前用户是否为审批人
  return true
})

const isApplicant = computed(() => {
  // TODO: 判断当前用户是否为申请人
  return false
})

const loadDetail = async () => {
  loading.value = true
  try {
    const response = await getApprovalDetail(route.params.id)
    if (response.code === 200) {
      detail.value = response.data || {}
    }
  } catch (error) {
    ElMessage.error('获取审批详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const getCurrentStep = () => {
  if (!detail.value.nodes) return 0
  const completedCount = detail.value.nodes.filter(
    n => n.status === 'APPROVED' || n.status === 'SKIPPED'
  ).length
  return completedCount
}

const getNodeStatus = status => {
  const map = {
    PENDING: 'wait',
    APPROVED: 'success',
    REJECTED: 'error',
    SKIPPED: 'finish',
  }
  return map[status] || 'wait'
}

const getNodeDescription = node => {
  if (node.status === 'PENDING') return '待审批'
  if (node.status === 'APPROVED') return '已通过'
  if (node.status === 'REJECTED') return '已驳回'
  if (node.status === 'SKIPPED') return '已跳过'
  return ''
}

const handleApprove = () => {
  commentTitle.value = '通过审批'
  commentText.value = ''
  commentDialogVisible.value = true
}

const handleReject = () => {
  commentTitle.value = '驳回审批'
  commentText.value = ''
  commentDialogVisible.value = true
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确认撤回该审批？', '提示', { type: 'warning' })
    await cancelApproval(route.params.id)
    ElMessage.success('已撤回')
    loadDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleSubmitComment = async () => {
  const isApprove = commentTitle.value === '通过审批'
  if (!isApprove && !commentText.value.trim()) {
    ElMessage.warning('请输入驳回理由')
    return
  }
  try {
    if (isApprove) {
      await approveApproval(route.params.id, commentText.value)
      ElMessage.success('已通过')
    } else {
      await rejectApproval(route.params.id, commentText.value)
      ElMessage.success('已驳回')
    }
    commentDialogVisible.value = false
    loadDetail()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 打开转交对话框
const handleTransfer = () => {
  transferTargetUserId.value = null
  transferRemark.value = ''
  transferDialogVisible.value = true
}

// 提交转交
const handleSubmitTransfer = async () => {
  if (!transferTargetUserId.value) {
    ElMessage.warning('请选择转交对象')
    return
  }
  try {
    await transferApproval(route.params.id, transferTargetUserId.value)
    ElMessage.success('已转交')
    transferDialogVisible.value = false
    loadDetail()
  } catch (error) {
    ElMessage.error('转交失败: ' + (error.message || '未知错误'))
  }
}

// 打开委托对话框
const handleDelegate = () => {
  delegateTargetUserId.value = null
  delegateRemark.value = ''
  delegateDialogVisible.value = true
}

// 提交委托
const handleSubmitDelegate = async () => {
  if (!delegateTargetUserId.value) {
    ElMessage.warning('请选择委托对象')
    return
  }
  try {
    await delegateApproval(route.params.id, delegateTargetUserId.value)
    ElMessage.success('已委托')
    delegateDialogVisible.value = false
    loadDetail()
  } catch (error) {
    ElMessage.error('委托失败: ' + (error.message || '未知错误'))
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
  loadDetail()
})
</script>

<style lang="scss" scoped>
.approval-detail-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.detail-header {
  margin-bottom: 16px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-card {
  background: #fff;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .status-text {
    font-size: 20px;
    font-weight: 500;
    margin-top: 16px;
  }

  &.status-pending .status-text {
    color: #faad14;
  }

  &.status-approved .status-text {
    color: #52c41a;
  }

  &.status-rejected .status-text {
    color: #ff4d4f;
  }

  &.status-cancelled .status-text {
    color: #8c8c8c;
  }
}

.info-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.form-display {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }

  :deep(.el-form-item__label) {
    color: #666;
  }
}

.record-item {
  .record-action {
    margin-bottom: 8px;
  }

  .record-comment {
    color: #666;
    font-size: 14px;
  }
}

.action-buttons {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
