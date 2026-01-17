<template>
  <div class="oa-card" @click="handleClick">
    <div class="oa-card-header">
      <div class="oa-card-title-row">
        <span class="oa-card-title">{{ cardTitle }}</span>
        <el-tag :type="statusTagType" size="small" effect="plain">
          {{ statusText }}
        </el-tag>
      </div>
      <div class="oa-card-subtitle">
        <span class="oa-card-applicant">{{ applicantText }}</span>
        <span v-if="applyTimeText" class="oa-card-dot">·</span>
        <span v-if="applyTimeText" class="oa-card-apply-time">{{ applyTimeText }}</span>
      </div>
    </div>
    <div v-if="summaryItems.length" class="oa-card-body">
      <div v-for="(item, index) in summaryItems" :key="index" class="oa-card-item">
        <span class="oa-card-item-label">{{ item.label }}</span>
        <span class="oa-card-item-value">{{ item.value }}</span>
      </div>
    </div>
    <div class="oa-card-footer">
      <span class="oa-card-link">查看详情</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  card: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['click'])

const cardTitle = computed(() => {
  if (!props.card) {
    return '审批'
  }
  return props.card.title || props.card.subject || '审批'
})

const statusValue = computed(() => {
  if (!props.card || !props.card.status) {
    return ''
  }
  return String(props.card.status).toLowerCase()
})

const statusText = computed(() => {
  const map = {
    pending: '待审批',
    processing: '审批中',
    approved: '已通过',
    rejected: '已驳回',
    completed: '已完成',
  }
  if (props.card && props.card.statusText) {
    return props.card.statusText
  }
  const key = statusValue.value
  return map[key] || '待处理'
})

const statusTagType = computed(() => {
  const map = {
    pending: 'info',
    processing: 'warning',
    approved: 'success',
    rejected: 'danger',
    completed: 'success',
  }
  const key = statusValue.value
  return map[key] || 'info'
})

const applicantText = computed(() => {
  if (!props.card) {
    return ''
  }
  return (
    props.card.applicant ||
    props.card.applicantName ||
    props.card.creator ||
    props.card.creatorName ||
    ''
  )
})

const applyTimeText = computed(() => {
  if (!props.card) {
    return ''
  }
  return props.card.applyTime || props.card.createTime || ''
})

const summaryItems = computed(() => {
  if (!props.card) {
    return []
  }
  if (Array.isArray(props.card.formSummary) && props.card.formSummary.length > 0) {
    return props.card.formSummary
  }
  const items = []
  if (props.card.approvalTypeName) {
    items.push({ label: '类型', value: props.card.approvalTypeName })
  } else if (props.card.typeName) {
    items.push({ label: '类型', value: props.card.typeName })
  } else if (props.card.type) {
    items.push({ label: '类型', value: props.card.type })
  }
  if (props.card.timeRange) {
    items.push({ label: '时间', value: props.card.timeRange })
  } else if (props.card.startTime || props.card.endTime) {
    const parts = []
    if (props.card.startTime) {
      parts.push(props.card.startTime)
    }
    if (props.card.endTime) {
      parts.push(props.card.endTime)
    }
    if (parts.length > 0) {
      items.push({ label: '时间', value: parts.join(' ~ ') })
    }
  }
  if (props.card.amount) {
    items.push({ label: '金额', value: props.card.amount })
  }
  return items
})

const handleClick = () => {
  if (!props.card) {
    return
  }
  emit('click', props.card)
}
</script>

<style scoped>
.oa-card {
  display: flex;
  flex-direction: column;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
  background-color: var(--el-bg-color);
  cursor: pointer;
  transition: all 0.2s ease;
  max-width: 360px;
}

.oa-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: var(--el-border-color);
  transform: translateY(-1px);
}

.oa-card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.oa-card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.oa-card-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.oa-card-subtitle {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.oa-card-applicant {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.oa-card-dot {
  color: var(--el-text-color-placeholder);
}

.oa-card-apply-time {
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.oa-card-body {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed var(--el-border-color-lighter);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.oa-card-item {
  display: flex;
  align-items: center;
  font-size: 12px;
}

.oa-card-item-label {
  width: 48px;
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
}

.oa-card-item-value {
  flex: 1;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.oa-card-footer {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.oa-card-link {
  font-size: 12px;
  color: var(--el-color-primary);
}
</style>
