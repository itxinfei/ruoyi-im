<template>
  <WorkbenchWidget
    title="我的待办"
    icon="task_alt"
    more-text="更多"
    class="todo-widget"
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
      v-else-if="todos.length > 0"
      class="todo-list"
    >
      <TodoItem
        v-for="todo in todos.slice(0, 5)"
        :key="todo.id"
        :todo="todo"
        class="compact-item"
        @toggle-complete="$emit('toggle-complete', $event)"
        @click="$emit('click-item', $event)"
      />
    </div>
    <div
      v-else
      class="widget-empty"
    >
      <span class="material-icons-outlined empty-icon">assignment_turned_in</span>
      <p>暂无待办事项</p>
    </div>
    
    <template #footer>
      <button
        class="add-todo-btn"
        @click="$emit('add')"
      >
        <span class="material-icons-outlined">add</span>
        新建待办
      </button>
    </template>
  </WorkbenchWidget>
</template>

<script setup>
import WorkbenchWidget from './WorkbenchWidget.vue'
import TodoItem from '@/components/Todo/TodoItem.vue'

defineProps({
  todos: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['more', 'toggle-complete', 'click-item', 'add'])
</script>

<style scoped lang="scss">
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.compact-item {
  box-shadow: none !important;
  border-color: var(--dt-border-lighter) !important;
  padding: 12px 14px !important;
  
  &:hover {
    border-color: var(--dt-brand-light) !important;
    background: var(--dt-bg-subtle-hover) !important;
  }
  
  :deep(.todo-desc) {
    -webkit-line-clamp: 1 !important;
  }
  
  :deep(.todo-actions) {
    display: none !important; // 首页小挂件不显示复杂操作
  }
}

.widget-loading { padding: 10px 0; }

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

.add-todo-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: none;
  border: 1px dashed var(--dt-border-medium);
  border-radius: var(--dt-radius-lg);
  padding: 8px;
  color: var(--dt-text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    color: var(--dt-brand-color);
    border-color: var(--dt-brand-color);
    background: var(--dt-brand-bg);
  }
  
  .material-icons-outlined { font-size: 18px; }
}
</style>
