<template>
  <WorkbenchWidget
    title="待我审批"
    icon="approval"
    class="approval-widget"
    @more="$emit('more')"
  >
    <div
      v-if="loading"
      class="widget-loading"
    >
      <el-skeleton
        :rows="3"
        animated
      />
    </div>
    <div
      v-else-if="approvals.length > 0"
      class="approval-list"
    >
      <div 
        v-for="item in approvals.slice(0, 3)" 
        :key="item.id" 
        class="approval-item"
        @click="$emit('click-item', item)"
      >
        <div class="approval-type-icon">
          <span class="material-icons-outlined">{{ getIcon(item.applyType) }}</span>
        </div>
        <div class="approval-info">
          <div class="approval-title">
            {{ item.title || item.applyType || '审批申请' }}
          </div>
          <div class="approval-meta">
            <span class="applicant">{{ item.applicantName }}</span>
            <span class="dot">·</span>
            <span class="time">{{ item.applyTime }}</span>
          </div>
        </div>
        <div
          class="approval-actions"
          @click.stop
        >
          <el-tooltip
            content="通过"
            placement="top"
          >
            <button
              class="action-btn pass"
              @click="$emit('approve', item)"
            >
              <span class="material-icons-outlined">check</span>
            </button>
          </el-tooltip>
          <el-tooltip
            content="驳回"
            placement="top"
          >
            <button
              class="action-btn reject"
              @click="$emit('reject', item)"
            >
              <span class="material-icons-outlined">close</span>
            </button>
          </el-tooltip>
        </div>
      </div>
    </div>
    <div
      v-else
      class="widget-empty"
    >
      <span class="material-icons-outlined empty-icon">rule</span>
      <p>暂无待处理审批</p>
    </div>
  </WorkbenchWidget>
</template>

<script setup>
import WorkbenchWidget from './WorkbenchWidget.vue'

defineProps({
  approvals: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['more', 'click-item', 'approve', 'reject'])

const getIcon = type => {
  const map = {
    'leave': 'luggage',
    'expense': 'payments',
    'overtime': 'more_time',
    'entry': 'person_add',
    'exit': 'person_remove'
  }
  return map[type?.toLowerCase()] || 'description'
}
</script>

<style scoped lang="scss">
.approval-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.approval-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  background: var(--dt-bg-subtle-hover);
  border-radius: var(--dt-radius-lg);
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  
  &:hover {
    background: var(--dt-bg-card);
    border-color: var(--dt-brand-lighter);
    box-shadow: var(--dt-shadow-sm);
    
    .approval-actions { opacity: 1; transform: translateX(0); }
  }
}

.approval-type-icon {
  width: 40px;
  height: 40px;
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .material-icons-outlined { font-size: 20px; }
}

.approval-info {
  flex: 1;
  min-width: 0;
}

.approval-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.approval-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--dt-text-quaternary);
  
  .dot { opacity: 0.5; }
}

.approval-actions {
  display: flex;
  gap: 6px;
  opacity: 0;
  transform: translateX(10px);
  transition: all 0.3s;
}

.action-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #fff;
  transition: transform 0.2s;
  
  &:hover { transform: scale(1.1); }
  
  &.pass { background: #52c41a; }
  &.reject { background: #f5222d; }
  
  .material-icons-outlined { font-size: 16px; }
}

.widget-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: var(--dt-text-quaternary);
  
  .empty-icon { font-size: 48px; margin-bottom: 12px; opacity: 0.5; }
  p { font-size: 14px; margin: 0; }
}
</style>
