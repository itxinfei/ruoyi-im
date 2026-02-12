<template>
  <div
    class="todo-item"
    :class="{
      completed: todo.completed,
      overdue: isOverdue,
      'batch-selected': isSelected,
      'is-dragging': isDragging
    }"
    @click="$emit('click', todo)"
  >
    <!-- 优先级指示条 -->
    <div
      class="priority-indicator"
      :class="`priority-${todo.priority}`"
    >
      <div
        v-if="todo.priority === 'high'"
        class="indicator-glow"
      />
    </div>

    <!-- 批量选择复选框 -->
    <div
      v-if="batchMode"
      class="batch-checkbox"
    >
      <div
        class="checkbox-inner"
        :class="{ checked: isSelected }"
      >
        <span
          v-if="isSelected"
          class="material-icons-outlined check-icon"
        >check</span>
      </div>
    </div>

    <!-- 状态勾选框 -->
    <div
      v-else
      class="todo-checkbox"
      @click.stop="$emit('toggle-complete', todo)"
    >
      <div
        class="checkbox-inner"
        :class="{ checked: todo.completed }"
      >
        <span
          v-if="todo.completed"
          class="material-icons-outlined check-icon"
        >check</span>
        <div class="checkbox-ripple" />
      </div>
    </div>

    <!-- 拖拽手柄 -->
    <div
      v-if="!batchMode"
      class="drag-handle"
      draggable="true"
      @dragstart="$emit('dragstart', $event, todo)"
      @dragend="$emit('dragend', $event, todo)"
      @click.stop
    >
      <span class="material-icons-outlined">drag_indicator</span>
    </div>

    <!-- 内容 -->
    <div class="todo-content">
      <div class="todo-header">
        <h4 class="todo-title">
          {{ todo.title }}
        </h4>
        <span
          class="todo-priority-badge"
          :class="`priority-${todo.priority}`"
        >
          {{ priorityText }}
        </span>
      </div>

      <p
        v-if="todo.content || todo.description"
        class="todo-desc"
      >
        {{ todo.content || todo.description }}
      </p>

      <div class="todo-meta">
        <span
          v-if="todo.dueDate"
          class="todo-date"
          :class="{ overdue: isOverdue }"
        >
          <span class="material-icons-outlined date-icon">schedule</span>
          {{ formattedDate }}
        </span>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div
      v-if="!batchMode"
      class="todo-actions"
      @click.stop
    >
      <el-tooltip
        content="编辑"
        placement="top"
      >
        <button
          class="action-btn"
          @click="$emit('edit', todo)"
        >
          <span class="material-icons-outlined">edit</span>
        </button>
      </el-tooltip>
      <el-tooltip
        content="删除"
        placement="top"
      >
        <button
          class="action-btn action-btn--danger"
          @click="$emit('delete', todo)"
        >
          <span class="material-icons-outlined">delete</span>
        </button>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  todo: {
    type: Object,
    required: true
  },
  batchMode: {
    type: Boolean,
    default: false
  },
  isSelected: {
    type: Boolean,
    default: false
  },
  isDragging: {
    type: Boolean,
    default: false
  }
})

defineEmits(['click', 'toggle-complete', 'edit', 'delete', 'dragstart', 'dragend'])

const priorityText = computed(() => {
  const map = { high: '紧急', medium: '普通', low: '低' }
  return map[props.todo.priority] || '普通'
})

const isOverdue = computed(() => {
  if (!props.todo.dueDate || props.todo.completed) {return false}
  return new Date(props.todo.dueDate) < new Date()
})

const formattedDate = computed(() => {
  if (!props.todo.dueDate) {return ''}
  const date = new Date(props.todo.dueDate)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
})
</script>

<style scoped lang="scss">
.todo-item {
  position: relative;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  padding: 16px 18px;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  box-shadow: var(--dt-shadow-sm);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  overflow: hidden;
  border: 1px solid var(--dt-border-light);

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, transparent 0%, var(--dt-black-01) 100%);
    transition: opacity 0.3s;
    opacity: 0;
  }

  &:hover {
    box-shadow: var(--dt-shadow-md);
    transform: translateY(-1px);
    border-color: var(--dt-brand-light);

    &::before {
      opacity: 1;
    }

    .todo-actions {
      opacity: 1;
      transform: translateX(0);
    }
  }

  &.completed {
    opacity: 0.6;
    .todo-title {
      text-decoration: line-through;
      color: var(--dt-text-quaternary);
    }
    .priority-indicator {
      opacity: 0.4;
    }
  }

  &.overdue {
    border-color: var(--dt-error-20);
  }

  &.batch-selected {
    background: var(--dt-brand-bg);
    border-color: var(--dt-brand-color);
  }
}

.priority-indicator {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;

  &.priority-high { background: var(--dt-priority-high); }
  &.priority-medium { background: var(--dt-priority-medium); }
  &.priority-low { background: var(--dt-priority-low); }

  .indicator-glow {
    position: absolute;
    inset: -2px;
    background: currentColor;
    opacity: 0.3;
    animation: priorityPulse 2s ease-in-out infinite;
  }
}

.checkbox-inner {
  width: 22px;
  height: 22px;
  border: 2px solid var(--dt-border-medium);
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  background: var(--dt-bg-body);

  &.checked {
    background: var(--dt-brand-color);
    border-color: var(--dt-brand-color);
    .check-icon {
      color: #ffffff;
      font-size: 16px;
      font-weight: bold;
    }
  }
}

.todo-checkbox {
  flex-shrink: 0;
  margin-top: 2px;
}

.drag-handle {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  color: var(--dt-text-quaternary);
  transition: color 0.2s;
  margin-top: -2px;

  &:hover { color: var(--dt-brand-color); }
  &:active { cursor: grabbing; }

  .material-icons-outlined { font-size: 20px; }
}

.todo-content {
  flex: 1;
  min-width: 0;
}

.todo-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.todo-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-priority-badge {
  padding: 2px 8px;
  border-radius: var(--dt-radius-sm);
  font-size: 11px;
  font-weight: 600;

  &.priority-high { background: #fff1f0; color: var(--dt-priority-high); }
  &.priority-medium { background: #fff7e6; color: var(--dt-priority-medium); }
  &.priority-low { background: #f6ffed; color: var(--dt-priority-low); }
}

.todo-desc {
  font-size: 13px;
  color: var(--dt-text-secondary);
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.todo-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.todo-date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--dt-text-quaternary);
  font-weight: 500;

  .date-icon { font-size: 15px; }
  &.overdue { color: var(--dt-error-60); }
}

.todo-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transform: translateX(10px);
  transition: all 0.3s;
  flex-shrink: 0;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--dt-radius-lg);
  background: var(--dt-bg-subtle-hover);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--dt-text-secondary);

  &:hover {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }

  &--danger:hover {
    background: #fff1f0;
    color: var(--dt-error-60);
  }

  .material-icons-outlined { font-size: 18px; }
}

@keyframes priorityPulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.1); opacity: 0.5; }
}

@keyframes checkBounce {
  0% { transform: scale(0); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

// 暗色模式适配
.dark {
  .todo-item {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
    
    &:hover { border-color: var(--dt-brand-bg-dark); }
    &.batch-selected { background: var(--dt-brand-bg-dark-subtle); }
  }
}
</style>
