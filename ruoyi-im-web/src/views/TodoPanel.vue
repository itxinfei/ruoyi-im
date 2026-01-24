<template>
  <div class="todo-panel">
    <div class="panel-header">
      <h2 class="panel-title">待办事项</h2>
      <button class="add-btn" @click="showAddDialog = true">
        <span class="material-icons-outlined">add</span>
        新建
      </button>
    </div>

    <div class="panel-content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="todos.length === 0" class="empty-state">
        <span class="material-icons-outlined empty-icon">check_circle</span>
        <p class="empty-text">暂无待办事项</p>
      </div>

      <div v-else class="todo-list">
        <div
          v-for="todo in todos"
          :key="todo.id"
          class="todo-item"
          :class="{ completed: todo.completed }"
        >
          <div class="todo-checkbox" @click="toggleComplete(todo)">
            <span v-if="todo.completed" class="material-icons-outlined">check_box</span>
            <span v-else class="material-icons-outlined">check_box_outline_blank</span>
          </div>
          <div class="todo-content">
            <div class="todo-title">{{ todo.title }}</div>
            <div class="todo-meta">
              <span class="todo-priority" :class="todo.priority">{{ priorityText(todo.priority) }}</span>
              <span class="todo-date">{{ formatDate(todo.dueDate) }}</span>
            </div>
          </div>
          <div class="todo-actions">
            <el-button size="small" link @click="deleteTodo(todo)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建待办对话框 -->
    <CreateTodoDialog
      v-model="showAddDialog"
      @success="handleTodoCreated"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CreateTodoDialog from '@/components/CreateTodoDialog/index.vue'
import { getTodos, completeTodo } from '@/api/im/workbench'

const loading = ref(false)
const showAddDialog = ref(false)
const todos = ref([])

// 加载待办列表
const loadTodos = async () => {
  loading.value = true
  try {
    const res = await getTodos()
    if (res.code === 200) {
      todos.value = (res.data || []).map(item => ({
        ...item,
        priorityClass: getPriorityClass(item.priority)
      }))
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载待办失败', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const priorityText = (priority) => {
  const map = { high: '紧急', medium: '普通', low: '低' }
  return map[priority] || '普通'
}

const getPriorityClass = (priority) => {
  const map = { high: 'priority-high', medium: 'priority-medium', low: 'priority-low' }
  return map[priority] || 'priority-medium'
}

const formatDate = (date) => {
  if (!date) return '无截止日期'
  return date
}

const toggleComplete = async (todo) => {
  try {
    const res = await completeTodo(todo.id)
    if (res.code === 200) {
      todo.completed = true
      ElMessage.success('已标记完成')
      // 从列表中移除已完成的待办
      todos.value = todos.value.filter(t => t.id !== todo.id)
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('完成待办失败', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

const deleteTodo = async (todo) => {
  try {
    await ElMessageBox.confirm('确定要删除这个待办吗？', '确认删除', {
      type: 'warning'
    })
    todos.value = todos.value.filter(t => t.id !== todo.id)
    ElMessage.success('删除成功')
  } catch {
    // 用户取消
  }
}

// 新建待办成功回调
const handleTodoCreated = () => {
  loadTodos()
}

onMounted(() => {
  loadTodos()
})
</script>

<style scoped>
.todo-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: #f4f7f9;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  flex-shrink: 0;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.add-btn:hover {
  background: #4096ff;
}

.panel-content {
  flex: 1;
  padding: 16px 24px;
  overflow-y: auto;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #8c8c8c;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: #d9d9d9;
}

.empty-text {
  font-size: 14px;
  margin: 0;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
}

.todo-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.todo-item.completed {
  opacity: 0.6;
}

.todo-item.completed .todo-title {
  text-decoration: line-through;
  color: #8c8c8c;
}

.todo-checkbox {
  cursor: pointer;
  color: #1677ff;
}

.todo-checkbox .material-icons-outlined {
  font-size: 24px;
}

.todo-content {
  flex: 1;
  min-width: 0;
}

.todo-title {
  font-size: 15px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.todo-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
}

.todo-priority {
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.todo-priority.high {
  background: #fff1f0;
  color: #f5222d;
}

.todo-priority.medium {
  background: #fff7e6;
  color: #fa8c16;
}

.todo-priority.low {
  background: #f6ffed;
  color: #52c41a;
}

.todo-date {
  color: #8c8c8c;
}

/* 暗色模式 */
:deep(.dark) .todo-panel {
  background: #0f172a;
}

:deep(.dark) .panel-header {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .panel-title {
  color: #f1f5f9;
}

:deep(.dark) .todo-item {
  background: #1e293b;
}

:deep(.dark) .todo-title {
  color: #f1f5f9;
}

:deep(.dark) .todo-date {
  color: #64748b;
}
</style>
