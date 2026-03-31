<template>
  <el-dialog
    v-model="visible"
    title="审批详情"
    width="600px"
    @close="handleClose"
  >
    <div v-if="approval" class="approval-detail">
      <!-- 状态标签 -->
      <div class="detail-header">
        <h3 class="detail-title">
          {{ approval.title }}
        </h3>
        <span class="status-badge" :class="approval.status">
          {{ statusText(approval.status) }}
        </span>
      </div>

      <!-- 审批类型 -->
      <div class="detail-section">
        <div class="section-label">
          审批类型
        </div>
        <div class="section-content">
          <span class="type-tag">{{ approval.type || '通用审批' }}</span>
        </div>
      </div>

      <!-- 申请人信息 -->
      <div class="detail-section">
        <div class="section-label">
          申请人
        </div>
        <div class="section-content">
          <DingtalkAvatar
            :src="approval.applicantAvatar"
            :name="approval.applicantName"
            :size="32"
            shape="circle"
          />
          <span>{{ approval.applicantName || '我' }}</span>
          <span class="apply-time">{{ approval.applyTime }}</span>
        </div>
      </div>

      <!-- 申请事由 -->
      <div v-if="approval.reason" class="detail-section">
        <div class="section-label">
          申请事由
        </div>
        <div class="section-content reason-content">
          {{ approval.reason }}
        </div>
      </div>

      <!-- 审批详情数据 -->
      <div v-if="approval.details" class="detail-section">
        <div class="section-label">
          详细信息
        </div>
        <div class="detail-grid">
          <div v-for="(value, key) in approval.details" :key="key" class="detail-item">
            <span class="item-label">{{ key }}:</span>
            <span class="item-value">{{ value }}</span>
          </div>
        </div>
      </div>

      <!-- 审批流程 -->
      <div class="detail-section">
        <div class="section-label">
          审批流程
        </div>
        <div class="flow-list">
          <div
            v-for="(step, index) in approvalFlow"
            :key="step.name || `step-${index}`"
            class="flow-item"
            :class="{ active: step.status === 'current', done: step.status === 'done' }"
          >
            <div class="flow-dot" />
            <div class="flow-content">
              <div class="flow-name">
                {{ step.name }}
              </div>
              <div class="flow-time">
                {{ step.time || '待处理' }}
              </div>
              <div v-if="step.comment" class="flow-comment">
                {{ step.comment }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button
          v-if="canApprove"
          type="success"
          :loading="submitting"
          @click="handleApprove"
        >
          通过
        </el-button>
        <el-button
          v-if="canApprove"
          type="danger"
          :loading="submitting"
          @click="handleReject"
        >
          拒绝
        </el-button>
        <el-button @click="handleClose">关闭</el-button>
      </span>
    </template>

    <!-- 审批意见输入框 -->
    <el-dialog
      v-model="showCommentDialog"
      :title="commentAction === 'approve' ? '通过审批' : '拒绝审批'"
      width="400px"
      append-to-body
    >
      <el-input
        v-model="comment"
        type="textarea"
        :rows="4"
        :placeholder="commentAction === 'approve' ? '请输入审批意见（可选）' : '请输入拒绝理由'"
        maxlength="200"
        show-word-limit
      />
      <template #footer>
        <el-button @click="showCommentDialog = false">
          取消
        </el-button>
        <el-button type="primary" :loading="submitting" @click="confirmAction">
          确定
        </el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { handleApproval } from '@/api/im/approval'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  approval: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const showCommentDialog = ref(false)
const comment = ref('')
const commentAction = ref('')
const submitting = ref(false)

// 是否可以审批（待审批状态）
const canApprove = computed(() => {
  return props.approval?.status === 'pending'
})

// 审批流程
const approvalFlow = computed(() => {
  if (!props.approval) return []

  const flow = [
    {
      name: '发起申请',
      status: 'done',
      time: props.approval.applyTime,
      comment: null
    }
  ]

  if (props.approval.approver) {
    flow.push({
      name: props.approval.approver,
      status: props.approval.status === 'pending' ? 'current' : 'done',
      time: props.approval.approvalTime || '',
      comment: props.approval.comment
    })
  }

  return flow
})

const statusText = (status) => {
  const map = {
    pending: '待审批',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return map[status] || '未知'
}

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleClose = () => {
  visible.value = false
  comment.value = ''
}

const handleApprove = () => {
  commentAction.value = 'approve'
  // 可以直接通过，也可以输入意见
  comment.value = ''
  showCommentDialog.value = true
}

const handleReject = () => {
  commentAction.value = 'reject'
  comment.value = ''
  showCommentDialog.value = true
}

const confirmAction = async () => {
  if (commentAction.value === 'reject' && !comment.value.trim()) {
    ElMessage.warning('请输入拒绝理由')
    return
  }

  submitting.value = true
  try {
    const res = await handleApproval({
      approvalId: props.approval.id,
      action: commentAction.value === 'approve' ? 'APPROVE' : 'REJECT',
      comment: comment.value
    })

    if (res.code === 200) {
      ElMessage.success(commentAction.value === 'approve' ? '已通过' : '已拒绝')
      showCommentDialog.value = false
      emit('success')
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('审批操作失败', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.approval-detail {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--dt-border-light);
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-main);
  margin: 0;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.status-badge.pending {
  background: var(--dt-warning-bg);
  color: var(--dt-warning-color);
}

.status-badge.approved {
  background: var(--dt-success-bg);
  color: var(--dt-success-color);
}

.status-badge.rejected {
  background: var(--dt-error-bg);
  color: var(--dt-error-color);
}

.detail-section {
  margin-bottom: 20px;
}

.section-label {
  font-size: 12px;
  color: var(--dt-text-desc);
  margin-bottom: 8px;
}

.section-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--dt-text-main);
}

.type-tag {
  padding: 4px 12px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-sm, 4px);
  font-size: 13px;
}

.apply-time {
  font-size: 12px;
  color: var(--dt-text-desc);
  margin-left: auto;
}

.reason-content {
  padding: 12px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-md, 8px);
  line-height: 1.6;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 12px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-md, 8px);
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-label {
  font-size: 12px;
  color: var(--dt-text-desc);
}

.item-value {
  font-size: 14px;
  color: var(--dt-text-main);
}

.flow-list {
  padding-left: 10px;
}

.flow-item {
  display: flex;
  gap: 16px;
  position: relative;
  padding-bottom: 20px;
}

.flow-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 24px;
  bottom: 0;
  width: 2px;
  background: var(--dt-border-light);
}

.flow-item.done:not(:last-child)::before {
  background: var(--dt-success-color);
}

.flow-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--dt-border-light);
  flex-shrink: 0;
  margin-top: 4px;
  position: relative;
  z-index: 1;
}

.flow-item.done .flow-dot {
  background: var(--dt-success-color);
}

.flow-item.active .flow-dot {
  background: var(--dt-brand-color);
  box-shadow: 0 0 0 4px rgba(0, 102, 204, 0.2);
}

.flow-content {
  flex: 1;
  padding-top: 2px;
}

.flow-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-main);
  margin-bottom: 4px;
}

.flow-time {
  font-size: 12px;
  color: var(--dt-text-desc);
}

.flow-comment {
  margin-top: 8px;
  padding: 8px 12px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-sm, 6px);
  font-size: 13px;
  color: var(--dt-text-desc);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  width: 100%;
}

/* 暗色模式 */
:deep(.dark) .detail-title {
  color: var(--dt-text-primary-dark);
}

:deep(.dark) .section-content,
:deep(.dark) .item-value,
:deep(.dark) .flow-name {
  color: var(--dt-text-secondary-dark);
}

:deep(.dark) .reason-content,
:deep(.dark) .detail-grid,
:deep(.dark) .flow-comment {
  background: rgba(30, 41, 59, 0.5);
}

:deep(.dark) .type-tag {
  background: rgba(51, 65, 85, 0.5);
}
</style>
