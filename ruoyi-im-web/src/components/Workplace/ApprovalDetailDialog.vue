<template>
  <el-dialog
    title="审批详情"
    v-model="visible"
    width="600px"
    destroy-on-close
    :close-on-click-modal="true"
    @close="handleClose"
  >
    <div v-loading="loading" class="approval-detail">
      <div v-if="detail" class="detail-container">
        <!-- 头部状态 -->
        <div class="header-section">
          <div class="applicant-info">
            <el-avatar :size="50" :src="detail.applicantAvatar">
              {{ detail.applicantName?.charAt(0) }}
            </el-avatar>
            <div class="name-box">
              <div class="applicant-name">{{ detail.applicantName }}</div>
              <div class="apply-time">{{ detail.applyTime }}</div>
            </div>
          </div>
          <div class="status-tag">
            <el-tag :type="getStatusType(detail.status)" effect="dark" size="large">
              {{ getStatusLabel(detail.status) }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <!-- 详情内容 -->
        <div class="content-section">
          <h3 class="title">{{ detail.title }}</h3>
          
          <div class="form-data">
            <div 
              v-for="(value, key) in parsedFormData" 
              :key="key" 
              class="form-item"
            >
              <div class="label">{{ translateKey(key) }}</div>
              <div class="value">{{ formatValue(value) }}</div>
            </div>
          </div>
        </div>

        <el-divider v-if="detail.remark" />
        <div v-if="detail.remark" class="remark-section">
          <div class="label">备注/审批意见</div>
          <div class="value">{{ detail.remark }}</div>
        </div>

        <!-- 操作区域 -->
        <div v-if="detail.status === 'PENDING'" class="action-footer">
          <el-input
            v-model="comment"
            type="textarea"
            :rows="2"
            placeholder="填写审批意见..."
            class="comment-input"
          />
          <div class="buttons">
            <el-button @click="visible = false">取消</el-button>
            <el-button type="danger" :loading="processing" @click="handleReject">驳回</el-button>
            <el-button type="primary" :loading="processing" @click="handleApprove">通过</el-button>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { getApproval, approve, reject } from '@/api/im/approval'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  approvalId: [Number, String]
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = ref(false)
const loading = ref(false)
const processing = ref(false)
const detail = ref(null)
const comment = ref('')

const parsedFormData = computed(() => {
  if (!detail.value || !detail.value.formData) return {}
  try {
    return typeof detail.value.formData === 'string' 
      ? JSON.parse(detail.value.formData) 
      : detail.value.formData
  } catch (e) {
    return {}
  }
})

const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已驳回',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const translateKey = (key) => {
  const dictionary = {
    'startTime': '开始时间',
    'endTime': '结束时间',
    'duration': '时长',
    'reason': '理由',
    'type': '类型',
    'amount': '金额',
    'description': '描述'
  }
  return dictionary[key] || key
}

const formatValue = (val) => {
  if (val === true) return '是'
  if (val === false) return '否'
  return val
}

const fetchDetail = async () => {
  if (!props.approvalId) return
  loading.value = true
  try {
    const res = await getApproval(props.approvalId)
    if (res.code === 200) {
      detail.value = res.data
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = async () => {
  processing.value = true
  try {
    await approve(detail.value.id, comment.value)
    ElMessage.success('已审批通过')
    visible.value = false
    emit('refresh')
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    processing.value = false
  }
}

const handleReject = async () => {
  if (!comment.value) {
    ElMessage.warning('请输入驳回理由')
    return
  }
  processing.value = true
  try {
    await reject(detail.value.id, comment.value)
    ElMessage.success('已驳回申请')
    visible.value = false
    emit('refresh')
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    processing.value = false
  }
}

const handleClose = () => {
  emit('update:modelValue', false)
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    comment.value = ''
    fetchDetail()
  }
})

watch(visible, (val) => {
  if (!val) emit('update:modelValue', false)
})
</script>

<style scoped lang="scss">
.approval-detail {
  min-height: 300px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .applicant-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .name-box {
      .applicant-name {
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }
      .apply-time {
        font-size: 12px;
        color: #8c8c8c;
        margin-top: 4px;
      }
    }
  }
}

.content-section {
  .title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 20px;
    color: var(--dt-text-primary);
  }

  .form-data {
    display: grid;
    grid-template-columns: repeat(1, 1fr);
    gap: 16px;

    .form-item {
      .label {
        font-size: 13px;
        color: var(--dt-text-tertiary);
        margin-bottom: 4px;
      }
      .value {
        font-size: 14px;
        color: var(--dt-text-primary);
        word-break: break-all;
      }
    }
  }
}

.remark-section {
  .label {
    font-size: 13px;
    color: #8c8c8c;
    margin-bottom: 4px;
  }
  .value {
    font-size: 14px;
    color: #262626;
    font-style: italic;
  }
}

.action-footer {
  margin-top: 30px;
  background: #f9f9f9;
  padding: 16px;
  border-radius: var(--dt-radius-md);

  .comment-input {
    margin-bottom: 16px;
  }

  .buttons {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>
