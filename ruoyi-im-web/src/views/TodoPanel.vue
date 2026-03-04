<template>
  <div class="todo-panel">
    <!-- 左侧分类导航 (240px 黄金中轴线) -->
    <aside class="todo-sidebar">
      <div class="sidebar-header">
        <h1 class="sidebar-title">待办事项</h1>
      </div>
      <div class="sidebar-content">
        <div 
          v-for="tab in filterTabs" 
          :key="tab.key" 
          class="nav-item" 
          :class="{ active: activeFilter === tab.key }"
          @click="activeFilter = tab.key"
        >
          <span class="material-icons-outlined nav-icon">{{ tab.icon }}</span>
          <span class="nav-label">{{ tab.label }}</span>
          <div v-if="getCount(tab.key) > 0" class="nav-badge">{{ getCount(tab.key) }}</div>
        </div>
      </div>
      <div class="sidebar-footer">
        <button class="add-todo-btn" @click="showAddDialog = true">
          <span class="material-icons-outlined">add</span>
          <span>新建待办</span>
        </button>
      </div>
    </aside>

    <!-- 右侧内容区 -->
    <main class="todo-main">
      <header class="todo-header">
        <div class="header-left">
          <h2 class="header-title">{{ currentTabLabel }}</h2>
        </div>
        <div class="header-right">
          <div class="search-box">
            <span class="material-icons-outlined search-icon">search</span>
            <input v-model="searchQuery" class="search-input" placeholder="搜索任务" type="text" />
          </div>
        </div>
      </header>

      <div class="todo-content-scroller custom-scrollbar">
        <div v-if="loading" class="loading-state"><el-icon class="is-loading"><Loading /></el-icon> 正在同步...</div>
        
        <div v-else-if="filteredTodos.length === 0" class="empty-state">
          <span class="material-icons-outlined">task_alt</span>
          <p>暂无待办任务</p>
        </div>

        <div v-else class="todo-list">
          <div 
            v-for="todo in filteredTodos" 
            :key="todo.id" 
            class="todo-card"
            :class="{ completed: todo.completed }"
            @click="handleViewDetail(todo)"
          >
            <div class="todo-checkbox" @click.stop="handleToggle(todo)">
              <div class="checkbox-inner" :class="{ checked: todo.completed }">
                <span v-if="todo.completed" class="material-icons-outlined">check</span>
              </div>
            </div>
            <div class="todo-main-info">
              <div class="todo-title-row">
                <span class="title-text">{{ todo.title }}</span>
                <span v-if="todo.priority === 'high'" class="priority-tag">紧急</span>
              </div>
              <div class="todo-meta-row">
                <span class="meta-date" :class="{ overdue: isOverdue(todo.dueDate) }">
                  <span class="material-icons-outlined">event</span>
                  {{ formatDate(todo.dueDate) }}
                </span>
              </div>
            </div>
            <div class="todo-actions">
              <button class="action-icon" @click.stop="handleEdit(todo)"><span class="material-icons-outlined">edit</span></button>
              <button class="action-icon danger" @click.stop="handleDelete(todo)"><span class="material-icons-outlined">delete</span></button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <CreateTodoDialog v-model="showAddDialog" :todo="editingTodo" @success="loadTodos" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTodos, completeTodo, deleteTodo } from '@/api/im/workbench'
import CreateTodoDialog from '@/components/CreateTodoDialog/index.vue'

const loading = ref(false)
const showAddDialog = ref(false)
const activeFilter = ref('all')
const searchQuery = ref('')
const todos = ref([])
const editingTodo = ref(null)

const filterTabs = [
  { key: 'all', label: '全部待办', icon: 'list' },
  { key: 'pending', label: '进行中', icon: 'schedule' },
  { key: 'high', label: '紧急任务', icon: 'priority_high' },
  { key: 'completed', label: '已完成', icon: 'check_circle' }
]

const currentTabLabel = computed(() => filterTabs.find(t => t.key === activeFilter.value)?.label || '全部')

const filteredTodos = computed(() => {
  let list = todos.value
  if (activeFilter.value === 'pending') list = list.filter(t => !t.completed)
  if (activeFilter.value === 'completed') list = list.filter(t => t.completed)
  if (activeFilter.value === 'high') list = list.filter(t => t.priority === 'high' && !t.completed)
  if (searchQuery.value) list = list.filter(t => t.title.includes(searchQuery.value))
  return list
})

const getCount = (key) => {
  if (key === 'pending') return todos.value.filter(t => !t.completed).length
  if (key === 'high') return todos.value.filter(t => t.priority === 'high' && !t.completed).length
  return 0
}

const loadTodos = async () => {
  loading.value = true
  try {
    const res = await getTodos()
    if (res.code === 200) todos.value = res.data || []
  } finally { loading.value = false }
}

const handleToggle = async (todo) => {
  try {
    const res = await completeTodo(todo.id)
    if (res.code === 200) {
      todo.completed = !todo.completed
      ElMessage.success(todo.completed ? '任务已完成' : '任务已重开')
    }
  } catch (e) { ElMessage.error('操作失败') }
}

const handleDelete = (todo) => {
  ElMessageBox.confirm('确定删除该任务吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteTodo(todo.id)
    if (res.code === 200) {
      todos.value = todos.value.filter(t => t.id !== todo.id)
      ElMessage.success('已删除')
    }
  })
}

const isOverdue = (date) => date && new Date(date) < new Date()
const formatDate = (date) => date ? new Date(date).toLocaleDateString() : '暂无截止日期'

onMounted(() => loadTodos())
</script>

<style scoped lang="scss">
.todo-panel { display: flex; height: 100%; flex: 1; background: #fff; }
.todo-sidebar { width: 240px; border-right: 1px solid #f2f3f5; display: flex; flex-direction: column; flex-shrink: 0; }
.sidebar-header { padding: 16px 20px; .sidebar-title { font-size: 16px; font-weight: 600; color: #1f2329; } }
.sidebar-content { flex: 1; padding: 8px 12px;
  .nav-item { display: flex; align-items: center; gap: 10px; padding: 10px 12px; margin: 2px 0; border-radius: 6px; cursor: pointer; color: #1f2329; transition: all 0.2s;
    &:hover { background: #f5f6f7; }
    &.active { background: #e8f4ff; color: #1677ff; font-weight: 600; }
    .nav-icon { font-size: 18px; }
    .nav-label { flex: 1; font-size: 13px; }
    .nav-badge { background: #ff4d4f; color: #fff; padding: 0 6px; border-radius: 10px; font-size: 10px; transform: scale(0.85); }
  }
}
.sidebar-footer { padding: 16px; border-top: 1px solid #f2f3f5;
  .add-todo-btn { width: 100%; height: 36px; background: #1677ff; color: #fff; border: none; border-radius: 6px; display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 13px; font-weight: 500; cursor: pointer; transition: all 0.2s;
    &:hover { background: #0056b3; box-shadow: 0 4px 12px rgba(22,119,255,0.2); }
  }
}
.todo-main { flex: 1; display: flex; flex-direction: column; background: #fff; overflow: hidden; }
.todo-header { height: 56px; padding: 0 24px; border-bottom: 1px solid #f2f3f5; display: flex; align-items: center; justify-content: space-between;
  .header-title { font-size: 16px; font-weight: 600; color: #1f2329; }
  .search-box { position: relative; .search-icon { position: absolute; left: 8px; top: 50%; transform: translateY(-50%); font-size: 16px; color: #8f959e; }
    .search-input { width: 200px; height: 32px; background: #f3f3f3; border: none; border-radius: 6px; padding: 0 32px; font-size: 13px; outline: none; &:focus { background: #fff; box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1); } }
  }
}
.todo-content-scroller { flex: 1; padding: 20px 24px; overflow-y: auto; background: #f5f5f5; }
.todo-list { display: flex; flex-direction: column; gap: 12px; }
.todo-card { background: #fff; border-radius: 8px; padding: 16px; display: flex; align-items: flex-start; gap: 14px; border: 1px solid rgba(0,0,0,0.05); box-shadow: 0 1px 2px rgba(0,0,0,0.04); cursor: pointer; transition: all 0.2s;
  &:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,0,0,0.08); .todo-actions { opacity: 1; } }
  &.completed { opacity: 0.6; .title-text { text-decoration: line-through; } }
}
.todo-checkbox { flex-shrink: 0; .checkbox-inner { width: 20px; height: 20px; border: 2px solid #d9d9d9; border-radius: 4px; display: flex; align-items: center; justify-content: center;
  &.checked { background: #1677ff; border-color: #1677ff; color: #fff; .material-icons-outlined { font-size: 14px; } } }
}
.todo-main-info { flex: 1; min-width: 0;
  .todo-title-row { display: flex; align-items: center; gap: 8px; .title-text { font-size: 14px; font-weight: 500; color: #1f2329; }
    .priority-tag { background: #fff1f0; color: #f5222d; padding: 0 6px; border-radius: 4px; font-size: 11px; } }
  .todo-meta-row { margin-top: 6px; .meta-date { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #8f959e; .material-icons-outlined { font-size: 14px; } &.overdue { color: #f5222d; } } }
}
.todo-actions { opacity: 0; display: flex; gap: 4px; transition: all 0.2s; .action-icon { border: none; background: transparent; cursor: pointer; color: #8f959e; &:hover { color: #1677ff; } &.danger:hover { color: #ff4d4f; } } }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 100px 0; color: #8f959e; .material-icons-outlined { font-size: 48px; margin-bottom: 12px; opacity: 0.3; } }
</style>
