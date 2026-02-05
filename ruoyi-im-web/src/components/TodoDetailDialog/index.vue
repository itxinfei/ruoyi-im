<template>
  <el-dialog
    v-model="visible"
    :title="todo?.title || '待办详情'"
    width="500px"
    @close="handleClose"
  >
    <div
      v-if="todo"
      class="todo-detail"
    >
      <!-- 状态标签 -->
      <div class="detail-header">
        <span
          class="status-badge"
          :class="todo.completed ? 'completed' : 'pending'"
        >
          {{ todo.completed ? '已完成' : '进行中' }}
        </span>
        <span
          class="priority-badge"
          :class="todo.priority"
        >{{ priorityText(todo.priority) }}</span>
      </div>

      <!-- 内容描述 -->
      <div
        v-if="todo.content"
        class="detail-section"
      >
        <div class="section-label">
          描述
        </div>
        <div class="section-content">
          {{ todo.content }}
        </div>
      </div>

      <!-- 截止时间 -->
      <div class="detail-section">
        <div class="section-label">
          截止时间
        </div>
        <div
          class="section-content"
          :class="{ overdue: isOverdue(todo.dueDate) }"
        >
          <span class="material-icons-outlined">schedule</span>
          {{ formatDate(todo.dueDate) }}
          <span
            v-if="isOverdue(todo.dueDate)"
            class="overdue-tag"
          >已过期</span>
        </div>
      </div>

      <!-- 创建时间 -->
      <div
        v-if="todo.createdAt"
        class="detail-section"
      >
        <div class="section-label">
          创建时间
        </div>
        <div class="section-content">
          <span class="material-icons-outlined">create</span>
          {{ todo.createdAt }}
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button
          v-if="todo && !todo.completed"
          type="success"
          :icon="Check"
          @click="handleComplete"
        >
          标记完成
        </el-button>
        <el-button
          v-if="todo && todo.completed"
          :icon="Refresh"
          @click="handleComplete"
        >
          重启待办
        </el-button>
        <el-button
          :icon="Edit"
          @click="handleEdit"
        >编辑</el-button>
        <el-button
          type="danger"
          :icon="Delete"
          @click="handleDelete"
        >删除</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { Check, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

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
  if (!dueDate) {return false}
  return new Date(dueDate) < new Date()
}

const formatDate = date => {
  if (!date) {return '无截止日期'}
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

watch(() => props.modelValue, val => {
  visible.value = val
})

watch(visible, val => {
  emit('update:modelValue', val)
})

const handleClose = () => {
  visible.value = false
}

const handleComplete = () => {
  emit('complete', props.todo)
}

const handleEdit = () => {
  emit('edit', props.todo)
}

const handleDelete = () => {
  emit('delete', props.todo)
}
</script>

<style scoped>
.todo-detail {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: var(--dt-radius-lg);
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.completed {
  background: #f6ffed;
  color: #52c41a;
}

.priority-badge {
  padding: 4px 12px;
  border-radius: var(--dt-radius-lg);
  font-size: 12px;
  font-weight: 500;
}

.priority-badge.high {
  background: #fff1f0;
  color: #f5222d;
}

.priority-badge.medium {
  background: #fff7e6;
  color: #fa8c16;
}

.priority-badge.low {
  background: #f6ffed;
  color: #52c41a;
}

.detail-section {
  margin-bottom: 20px;
}

.section-label {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.section-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #262626;
}

.section-content .material-icons-outlined {
  font-size: 18px;
  color: #8c8c8c;
}

.section-content.overdue {
  color: #ff4d4f;
}

.overdue-tag {
  margin-left: 8px;
  padding: 2px 8px;
  background: #fff1f0;
  color: #ff4d4f;
  border-radius: var(--dt-radius-sm);
  font-size: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  width: 100%;
}

/* 暗色模式 */
:deep(.dark) .section-label {
  color: #64748b;
}

:deep(.dark) .section-content {
  color: #e2e8f0;
}

:deep(.dark) .section-content .material-icons-outlined {
  color: #64748b;
}
</style>
