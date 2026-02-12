<template>
  <el-dialog
    v-model="visible"
    :title="todo?.title || '待办详情'"
    width="480px"
    class="todo-detail-dialog"
    destroy-on-close
    @close="handleClose"
  >
    <div
      v-if="todo"
      class="todo-detail-content"
    >
      <!-- 头部状态 -->
      <div class="detail-header-tags">
        <div
          class="status-tag"
          :class="todo.completed ? 'completed' : 'pending'"
        >
          <span class="material-icons-outlined tag-icon">
            {{ todo.completed ? 'check_circle' : 'pending' }}
          </span>
          {{ todo.completed ? '已完成' : '进行中' }}
        </div>
        <div
          class="priority-tag"
          :class="todo.priority"
        >
          {{ priorityText(todo.priority) }}
        </div>
      </div>

      <!-- 标题与内容 -->
      <div class="content-section">
        <h3 class="detail-title">
          {{ todo.title }}
        </h3>
        <p
          v-if="todo.content || todo.description"
          class="detail-desc"
        >
          {{ todo.content || todo.description }}
        </p>
      </div>

      <!-- 元数据列表 -->
      <div class="meta-list">
        <div class="meta-item">
          <div class="meta-label">
            <span class="material-icons-outlined">schedule</span>
            截止时间
          </div>
          <div
            class="meta-value"
            :class="{ overdue: isOverdue(todo.dueDate) }"
          >
            {{ formatDate(todo.dueDate) }}
            <span
              v-if="isOverdue(todo.dueDate)"
              class="overdue-badge"
            >已逾期</span>
          </div>
        </div>

        <div
          v-if="todo.createdAt"
          class="meta-item"
        >
          <div class="meta-label">
            <span class="material-icons-outlined">history</span>
            创建时间
          </div>
          <div class="meta-value">
            {{ todo.createdAt }}
          </div>
        </div>

        <div
          v-if="todo.completed && todo.completedTime"
          class="meta-item"
        >
          <div class="meta-label">
            <span class="material-icons-outlined">task_alt</span>
            完成时间
          </div>
          <div class="meta-value">
            {{ todo.completedTime }}
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="detail-footer">
        <div class="footer-left">
          <el-button
            type="danger"
            plain
            @click="handleDelete"
          >
            删除
          </el-button>
        </div>
        <div class="footer-right">
          <el-button
            v-if="!todo?.completed"
            type="success"
            @click="handleComplete"
          >
            标记完成
          </el-button>
          <el-button
            v-else
            @click="handleComplete"
          >
            重启待办
          </el-button>
          <el-button
            type="primary"
            @click="handleEdit"
          >
            编辑
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  todo: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'edit', 'delete', 'complete'])

const visible = ref(false)

const priorityText = priority => {
  const map = { high: '紧急', medium: '普通', low: '低' }
  return map[priority] || '普通'
}

const isOverdue = dueDate => {
  if (!dueDate || props.todo?.completed) {return false}
  return new Date(dueDate) < new Date()
}

const formatDate = date => {
  if (!date) {return '未设置'}
  const d = new Date(date)
  return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

watch(() => props.modelValue, val => { visible.value = val })
watch(visible, val => { emit('update:modelValue', val) })

const handleClose = () => { visible.value = false }
const handleComplete = () => { emit('complete', props.todo) }
const handleEdit = () => { emit('edit', props.todo) }
const handleDelete = () => { emit('delete', props.todo) }
</script>

<style scoped lang="scss">
.todo-detail-dialog {
  :deep(.el-dialog) {
    border-radius: var(--dt-radius-xl);
    overflow: hidden;

    .el-dialog__header {
      padding: 16px 24px;
      border-bottom: 1px solid var(--dt-border-light);
      margin: 0;
      .el-dialog__title {
        font-size: 15px;
        font-weight: 600;
        color: var(--dt-text-secondary);
      }
    }

    .el-dialog__body {
      padding: 24px;
    }

    .el-dialog__footer {
      padding: 16px 24px;
      border-top: 1px solid var(--dt-border-light);
      background: var(--dt-bg-subtle);
    }
  }
}

.detail-header-tags {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.status-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: var(--dt-radius-full);
  font-size: 12px;
  font-weight: 600;
  
  &.pending { background: #fff7e6; color: #fa8c16; }
  &.completed { background: #f6ffed; color: #52c41a; }
  
  .tag-icon { font-size: 16px; }
}

.priority-tag {
  padding: 4px 12px;
  border-radius: var(--dt-radius-full);
  font-size: 12px;
  font-weight: 600;
  
  &.high { background: #fff1f0; color: #f5222d; }
  &.medium { background: #fff7e6; color: #fa8c16; }
  &.low { background: #f6ffed; color: #52c41a; }
}

.content-section {
  margin-bottom: 32px;
  
  .detail-title {
    font-size: 20px;
    font-weight: 700;
    color: var(--dt-text-primary);
    margin: 0 0 12px 0;
    line-height: 1.4;
  }
  
  .detail-desc {
    font-size: 15px;
    color: var(--dt-text-secondary);
    line-height: 1.6;
    margin: 0;
    white-space: pre-wrap;
    word-break: break-all;
  }
}

.meta-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  .meta-label {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: var(--dt-text-quaternary);
    
    .material-icons-outlined { font-size: 18px; }
  }
  
  .meta-value {
    font-size: 14px;
    color: var(--dt-text-secondary);
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 8px;
    
    &.overdue { color: var(--dt-error-60); }
  }
}

.overdue-badge {
  padding: 2px 6px;
  background: #fff1f0;
  color: #f5222d;
  border-radius: var(--dt-radius-sm);
  font-size: 11px;
}

.detail-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .footer-right {
    display: flex;
    gap: 12px;
  }
}

// 暗色模式适配
.dark {
  .todo-detail-content {
    .detail-title { color: #f8fafc; }
    .status-tag.pending { background: #432b13; }
    .status-tag.completed { background: #132b13; }
    .priority-tag.high { background: #431313; }
  }
}
</style>
