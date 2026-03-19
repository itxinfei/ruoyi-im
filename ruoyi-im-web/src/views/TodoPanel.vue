<template>
  <div class="todo-panel">
    <!-- 左侧分类导航 (240px 黄金中轴线) -->
    <aside class="todo-sidebar">
      <div class="sidebar-header">
        <h1 class="sidebar-title">
          待办事项
        </h1>
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
          <div v-if="getCount(tab.key) > 0" class="nav-badge">
            {{ getCount(tab.key) }}
          </div>
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
          <h2 class="header-title">
            {{ currentTabLabel }}
          </h2>
        </div>
        <div class="header-right">
          <div class="search-box">
            <span class="material-icons-outlined search-icon">search</span>
            <input
              v-model="searchQuery"
              class="search-input"
              placeholder="搜索任务"
              type="text"
            >
          </div>
        </div>
      </header>

      <div class="todo-content-scroller custom-scrollbar">
        <div v-if="loading" class="loading-state">
          <el-icon class="is-loading">
            <Loading />
          </el-icon> 正在同步...
        </div>

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
              <button class="action-icon" @click.stop="handleEdit(todo)">
                <span class="material-icons-outlined">edit</span>
              </button>
              <button class="action-icon danger" @click.stop="handleDelete(todo)">
                <span class="material-icons-outlined">delete</span>
              </button>
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

const handleEdit = (todo) => {
  editingTodo.value = todo
  showAddDialog.value = true
}

const handleViewDetail = (todo) => {
  ElMessage.info(`查看任务: ${todo.title}`)
}

const isOverdue = (date) => date && new Date(date) < new Date()
const formatDate = (date) => date ? new Date(date).toLocaleDateString() : '暂无截止日期'

onMounted(() => loadTodos())
</script>

<style scoped lang="scss">
.todo-panel { display: flex; height: 100%; flex: 1; background: var(--dt-bg-card); }
.todo-sidebar { width: var(--dt-contact-panel-width, 240px); border-right: 1px solid var(--dt-border-light); display: flex; flex-direction: column; flex-shrink: 0; }
.sidebar-header { padding: var(--dt-spacing-md, 16px) var(--dt-spacing-md, 20px); .sidebar-title { font-size: var(--dt-font-size-base); font-weight: var(--dt-font-weight-semibold); color: var(--dt-text-primary); } }
.sidebar-content { flex: 1; padding: var(--dt-spacing-xs, 8px) var(--dt-spacing-sm, 12px);
  .nav-item { display: flex; align-items: center; gap: var(--dt-spacing-sm, 10px); padding: var(--dt-spacing-sm, 10px) var(--dt-spacing-xs, 12px); margin: var(--dt-spacing-xs, 2px) 0; border-radius: var(--dt-radius-sm); cursor: pointer; color: var(--dt-text-primary); transition: all var(--dt-transition-fast);
    &:hover { background: var(--dt-bg-session-hover); }
    &.active { background: var(--dt-brand-bg); color: var(--dt-brand-color); font-weight: var(--dt-font-weight-semibold); }
    .nav-icon { font-size: var(--dt-icon-size-lg, 18px); }
    .nav-label { flex: 1; font-size: var(--dt-font-size-sm); }
    .nav-badge { background: var(--dt-error-color); color: var(--dt-text-primary); padding: 0 var(--dt-spacing-xs, 6px); border-radius: var(--dt-radius-full, 10px); font-size: var(--dt-font-size-xs); transform: scale(0.85); }
  }
}
.sidebar-footer { padding: var(--dt-spacing-md, 16px); border-top: 1px solid var(--dt-border-light);
  .add-todo-btn { width: 100%; height: var(--dt-btn-height-md, 36px); background: var(--dt-brand-color); color: var(--dt-text-primary); border: none; border-radius: var(--dt-radius-sm); display: flex; align-items: center; justify-content: center; gap: var(--dt-spacing-xs, 8px); font-size: var(--dt-font-size-sm); font-weight: var(--dt-font-weight-medium); cursor: pointer; transition: all var(--dt-transition-fast);
    &:hover { background: var(--dt-brand-hover); box-shadow: var(--dt-shadow-2); }
  }
}
.todo-main { flex: 1; display: flex; flex-direction: column; background: var(--dt-bg-card); overflow: hidden; }
.todo-header { height: var(--dt-toolbar-height, 56px); padding: 0 var(--dt-spacing-lg, 24px); border-bottom: 1px solid var(--dt-border-light); display: flex; align-items: center; justify-content: space-between;
  .header-title { font-size: var(--dt-font-size-base); font-weight: var(--dt-font-weight-semibold); color: var(--dt-text-primary); }
  .search-box { position: relative; .search-icon { position: absolute; left: var(--dt-spacing-xs, 8px); top: 50%; transform: translateY(-50%); font-size: var(--dt-icon-size-lg, 16px); color: var(--dt-text-tertiary); }
    .search-input { width: 200px; height: var(--dt-btn-height-sm, 32px); background: var(--dt-bg-body); border: none; border-radius: var(--dt-radius-sm); padding: 0 var(--dt-spacing-lg, 32px); font-size: var(--dt-font-size-sm); outline: none; &:focus { background: var(--dt-bg-card); box-shadow: 0 0 0 2px var(--dt-brand-lighter); } }
  }
}
.todo-content-scroller { flex: 1; padding: var(--dt-spacing-lg, 20px) var(--dt-spacing-lg, 24px); overflow-y: auto; background: var(--dt-bg-body); }
.todo-list { display: flex; flex-direction: column; gap: var(--dt-spacing-sm, 12px); }
.todo-card { background: var(--dt-bg-card); border-radius: var(--dt-radius-md); padding: var(--dt-spacing-md, 16px); display: flex; align-items: flex-start; gap: var(--dt-spacing-md, 14px); border: 1px solid var(--dt-border-lighter); box-shadow: var(--dt-shadow-1); cursor: pointer; transition: all var(--dt-transition-fast);
  &:hover { transform: translateY(var(--dt-transform-y, -1px)); box-shadow: var(--dt-shadow-2); .todo-actions { opacity: 1; } }
  &.completed { opacity: 0.6; .title-text { text-decoration: line-through; } }
}
.todo-checkbox { flex-shrink: 0; .checkbox-inner { width: 20px; height: 20px; border: 2px solid var(--dt-border-color); border-radius: var(--dt-radius-sm); display: flex; align-items: center; justify-content: center;
  &.checked { background: var(--dt-brand-color); border-color: var(--dt-brand-color); color: var(--dt-text-primary); .material-icons-outlined { font-size: var(--dt-font-size-xs); } } }
}
.todo-main-info { flex: 1; min-width: 0;
  .todo-title-row { display: flex; align-items: center; gap: var(--dt-spacing-xs, 8px); .title-text { font-size: var(--dt-font-size-sm); font-weight: var(--dt-font-weight-medium); color: var(--dt-text-primary); }
    .priority-tag { background: var(--dt-error-bg); color: var(--dt-error-color); padding: 0 var(--dt-spacing-xs, 6px); border-radius: var(--dt-radius-sm); font-size: var(--dt-font-size-xs); } }
  .todo-meta-row { margin-top: var(--dt-spacing-xs, 6px); .meta-date { display: flex; align-items: center; gap: var(--dt-spacing-xs, 4px); font-size: var(--dt-font-size-xs); color: var(--dt-text-tertiary); .material-icons-outlined { font-size: var(--dt-icon-size-sm, 14px); } &.overdue { color: var(--dt-error-color); } } }
}
.todo-actions { opacity: 0; display: flex; gap: var(--dt-spacing-xs, 4px); transition: all var(--dt-transition-fast); .action-icon { border: none; background: transparent; cursor: pointer; color: var(--dt-text-tertiary); &:hover { color: var(--dt-brand-color); } &.danger:hover { color: var(--dt-error-color); } } }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: var(--dt-spacing-3xl, 100px) 0; color: var(--dt-text-tertiary); .material-icons-outlined { font-size: var(--dt-icon-size-xl, 48px); margin-bottom: var(--dt-spacing-md, 12px); opacity: 0.3; } }
</style>
