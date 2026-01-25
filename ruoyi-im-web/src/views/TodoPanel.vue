<template>
  <div class="todo-panel">
    <div class="panel-header">
      <h2 class="panel-title">待办事项</h2>
      <button class="add-btn" @click="showAddDialog = true">
        <span class="material-icons-outlined">add</span>
        新建
      </button>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <button
        v-for="tab in filterTabs"
        :key="tab.key"
        class="filter-tab"
        :class="{ active: activeFilter === tab.key }"
        @click="activeFilter = tab.key"
      >
        {{ tab.label }}
        <span v-if="getCount(tab.key) > 0" class="tab-count">{{ getCount(tab.key) }}</span>
      </button>
    </div>

    <div class="panel-content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="filteredTodos.length === 0" class="empty-state">
        <span class="material-icons-outlined empty-icon">check_circle</span>
        <p class="empty-text">{{ emptyText }}</p>
      </div>

      <div v-else class="todo-list">
        <div
          v-for="todo in filteredTodos"
          :key="todo.id"
          class="todo-item"
          :class="{ completed: todo.completed, overdue: isOverdue(todo.dueDate) }"
          @click="handleViewDetail(todo)"
        >
          <div class="todo-checkbox" @click.stop="toggleComplete(todo)">
            <span v-if="todo.completed" class="material-icons-outlined">check_box</span>
            <span v-else class="material-icons-outlined">check_box_outline_blank</span>
          </div>
          <div class="todo-content">
            <div class="todo-title">{{ todo.title }}</div>
            <div v-if="todo.content" class="todo-desc">{{ todo.content }}</div>
            <div class="todo-meta">
              <span class="todo-priority" :class="todo.priority">{{ priorityText(todo.priority) }}</span>
              <span class="todo-date" :class="{ overdue: isOverdue(todo.dueDate) }">
                {{ formatDate(todo.dueDate) }}
              </span>
            </div>
          </div>
          <div class="todo-actions" @click.stop>
            <el-button size="small" link @click="handleEdit(todo)">
              <span class="material-icons-outlined">edit</span>
            </el-button>
            <el-button size="small" link @click="deleteTodo(todo)">
              <span class="material-icons-outlined">delete</span>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建待办对话框 -->
    <CreateTodoDialog
      v-model="showAddDialog"
      :todo="editingTodo"
      @success="handleTodoCreated"
    />

    <!-- 待办详情对话框 -->
    <TodoDetailDialog
      v-model="showDetailDialog"
      :todo="selectedTodo"
      @edit="handleEditFromDetail"
      @delete="deleteTodo"
      @complete="toggleComplete"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CreateTodoDialog from '@/components/CreateTodoDialog/index.vue'
import TodoDetailDialog from '@/components/TodoDetailDialog/index.vue'
import { getTodos, completeTodo, deleteTodo as deleteTodoApi } from '@/api/im/workbench'

const loading = ref(false)
const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const todos = ref([])
const editingTodo = ref(null)
const selectedTodo = ref(null)
const activeFilter = ref('all')

// 筛选标签
const filterTabs = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '进行中' },
  { key: 'completed', label: '已完成' },
  { key: 'high', label: '紧急' }
]

// 根据筛选条件过滤待办
const filteredTodos = computed(() => {
  let list = todos.value

  switch (activeFilter.value) {
    case 'pending':
      list = list.filter(t => !t.completed)
      break
    case 'completed':
      list = list.filter(t => t.completed)
      break
    case 'high':
      list = list.filter(t => t.priority === 'high' && !t.completed)
      break
  }

  // 按截止日期排序
  return list.sort((a, b) => {
    if (a.completed !== b.completed) return a.completed ? 1 : -1
    if (a.dueDate && b.dueDate) return new Date(a.dueDate) - new Date(b.dueDate)
    return a.dueDate ? -1 : 1
  })
})

// 空状态提示文本
const emptyText = computed(() => {
  const map = {
    all: '暂无待办事项',
    pending: '暂无进行中的待办',
    completed: '暂无已完成的待办',
    high: '暂无紧急待办'
  }
  return map[activeFilter.value] || '暂无待办事项'
})

// 获取筛选条件数量
const getCount = (filter) => {
  if (filter === 'all') return todos.value.length
  if (filter === 'pending') return todos.value.filter(t => !t.completed).length
  if (filter === 'completed') return todos.value.filter(t => t.completed).length
  if (filter === 'high') return todos.value.filter(t => t.priority === 'high' && !t.completed).length
  return 0
}

// 判断是否过期
const isOverdue = (dueDate) => {
  if (!dueDate) return false
  return new Date(dueDate) < new Date()
}

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
  const d = new Date(date)
  const now = new Date()
  const diff = d - now
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '明天'
  if (days === -1) return '昨天'
  if (days < -1) return `${Math.abs(days)}天前`
  if (days <= 7) return `${days}天后`

  return `${d.getMonth() + 1}月${d.getDate()}日`
}

const toggleComplete = async (todo) => {
  try {
    const res = await completeTodo(todo.id)
    if (res.code === 200) {
      todo.completed = !todo.completed
      ElMessage.success(todo.completed ? '已标记完成' : '已重启')
      // 已完成的从当前筛选视图移除（如果是进行中视图）
      if (todo.completed && activeFilter.value === 'pending') {
        todos.value = todos.value.filter(t => t.id !== todo.id)
      }
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
    const res = await deleteTodoApi(todo.id)
    if (res.code === 200) {
      todos.value = todos.value.filter(t => t.id !== todo.id)
      ElMessage.success('删除成功')
      showDetailDialog.value = false
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

// 查看详情
const handleViewDetail = (todo) => {
  selectedTodo.value = todo
  showDetailDialog.value = true
}

// 编辑待办
const handleEdit = (todo) => {
  editingTodo.value = todo
  showAddDialog.value = true
}

// 从详情对话框编辑
const handleEditFromDetail = (todo) => {
  showDetailDialog.value = false
  handleEdit(todo)
}

// 新建待办成功回调
const handleTodoCreated = () => {
  editingTodo.value = null
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

/* 筛选标签 */
.filter-tabs {
  display: flex;
  padding: 12px 24px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  gap: 8px;
  flex-shrink: 0;
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: none;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  color: #595959;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-tab:hover {
  background: #f5f5f5;
}

.filter-tab.active {
  background: #e6f7ff;
  color: #1677ff;
  font-weight: 500;
}

.tab-count {
  padding: 2px 6px;
  background: #1677ff;
  color: #fff;
  border-radius: 10px;
  font-size: 11px;
  min-width: 18px;
  text-align: center;
}

.filter-tab:not(.active) .tab-count {
  background: #d9d9d9;
  color: #8c8c8c;
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
  cursor: pointer;
}

.todo-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.todo-item.completed {
  opacity: 0.6;
}

.todo-item.completed .todo-title {
  text-decoration: line-through;
  color: #8c8c8c;
}

.todo-item.overdue {
  border-left: 3px solid #ff4d4f;
}

.todo-item.overdue .todo-date {
  color: #ff4d4f;
}

.todo-checkbox {
  cursor: pointer;
  color: #1677ff;
  flex-shrink: 0;
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

.todo-desc {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.todo-date.overdue {
  color: #ff4d4f;
}

.todo-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.todo-item:hover .todo-actions {
  opacity: 1;
}

.todo-actions .el-button {
  padding: 4px;
}

.todo-actions .material-icons-outlined {
  font-size: 18px;
}

/* 暗色模式 */
:deep(.dark) .todo-panel {
  background: #0f172a;
}

:deep(.dark) .panel-header,
:deep(.dark) .filter-tabs {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .panel-title {
  color: #f1f5f9;
}

:deep(.dark) .filter-tab {
  color: #94a3b8;
}

:deep(.dark) .filter-tab:hover {
  background: rgba(51, 65, 85, 0.5);
}

:deep(.dark) .filter-tab.active {
  background: rgba(22, 119, 255, 0.15);
  color: #60a5fa;
}

:deep(.dark) .todo-item {
  background: #1e293b;
}

:deep(.dark) .todo-title {
  color: #f1f5f9;
}

:deep(.dark) .todo-desc {
  color: #64748b;
}

:deep(.dark) .todo-date {
  color: #64748b;
}
</style>
