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
        <!-- 状态筛选 -->
        <div class="nav-section">
          <div class="nav-section-title">状态</div>
          <div
            v-for="tab in statusTabs"
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

        <!-- 分类筛选 -->
        <div class="nav-section">
          <div class="nav-section-title">分类</div>
          <div
            v-for="cat in categoryTabs"
            :key="cat.key"
            class="nav-item"
            :class="{ active: activeFilter === cat.key }"
            @click="activeFilter = cat.key"
          >
            <span class="material-icons-outlined nav-icon">{{ cat.icon }}</span>
            <span class="nav-label">{{ cat.label }}</span>
            <div v-if="getCategoryCount(cat.key) > 0" class="nav-badge cat">
              {{ getCategoryCount(cat.key) }}
            </div>
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
              placeholder="搜索任务标题或内容"
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
          <p>{{ searchQuery ? '未找到匹配的任务' : '暂无待办任务' }}</p>
        </div>

        <div v-else class="todo-list">
          <div
            v-for="todo in filteredTodos"
            :key="todo.id"
            class="todo-card"
            :class="{ completed: todo.status === 'COMPLETED' }"
            @click="handleViewDetail(todo)"
          >
            <div class="todo-checkbox" @click.stop="handleToggle(todo)">
              <div class="checkbox-inner" :class="{ checked: todo.status === 'COMPLETED' }">
                <span v-if="todo.status === 'COMPLETED'" class="material-icons-outlined">check</span>
              </div>
            </div>
            <div class="todo-main-info">
              <div class="todo-title-row">
                <span class="title-text">{{ todo.title }}</span>
                <span v-if="todo.priority === 'HIGH'" class="priority-tag high">紧急</span>
                <span v-if="todo.taskType" class="category-tag" :class="todo.taskType.toLowerCase()">
                  {{ getTypeLabel(todo.taskType) }}
                </span>
              </div>
              <div class="todo-meta-row">
                <span class="meta-date" :class="{ overdue: isOverdue(todo.dueDate) }">
                  <span class="material-icons-outlined">event</span>
                  {{ formatDate(todo.dueDate) }}
                </span>
                <span v-if="todo.remindTime" class="meta-remind">
                  <span class="material-icons-outlined">notifications</span>
                  {{ formatDateTime(todo.remindTime) }}
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
import { getMyTasks, updateTaskStatus, deleteTask, getTaskStatistics } from '@/api/im/task'
import CreateTodoDialog from '@/components/CreateTodoDialog/index.vue'

const loading = ref(false)
const showAddDialog = ref(false)
const activeFilter = ref('all')
const searchQuery = ref('')
const todos = ref([])
const editingTodo = ref(null)

// 状态筛选标签
const statusTabs = [
  { key: 'all', label: '全部待办', icon: 'list' },
  { key: 'pending', label: '进行中', icon: 'schedule' },
  { key: 'high', label: '紧急任务', icon: 'priority_high' },
  { key: 'completed', label: '已完成', icon: 'check_circle' }
]

// 分类筛选标签
const categoryTabs = [
  { key: 'WORK', label: '工作', icon: 'work' },
  { key: 'PERSONAL', label: '个人', icon: 'person' },
  { key: 'STUDY', label: '学习', icon: 'school' },
  { key: 'OTHER', label: '其他', icon: 'more_horiz' }
]

const currentTabLabel = computed(() => {
  const status = statusTabs.find(t => t.key === activeFilter.value)
  if (status) return status.label
  const cat = categoryTabs.find(t => t.key === activeFilter.value)
  return cat?.label || '全部'
})

const filteredTodos = computed(() => {
  let list = todos.value

  // 状态筛选
  if (activeFilter.value === 'pending') {
    list = list.filter(t => t.status !== 'COMPLETED')
  } else if (activeFilter.value === 'completed') {
    list = list.filter(t => t.status === 'COMPLETED')
  } else if (activeFilter.value === 'high') {
    list = list.filter(t => t.priority === 'HIGH' && t.status !== 'COMPLETED')
  }

  // 分类筛选
  const isCategory = categoryTabs.some(c => c.key === activeFilter.value)
  if (isCategory) {
    list = list.filter(t => t.taskType === activeFilter.value)
  }

  // 搜索筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(t =>
      t.title?.toLowerCase().includes(query) ||
      t.description?.toLowerCase().includes(query)
    )
  }

  return list
})

const getCount = (key) => {
  if (key === 'pending') return todos.value.filter(t => t.status !== 'COMPLETED').length
  if (key === 'high') return todos.value.filter(t => t.priority === 'HIGH' && t.status !== 'COMPLETED').length
  if (key === 'completed') return todos.value.filter(t => t.status === 'COMPLETED').length
  return 0
}

const getCategoryCount = (key) => {
  return todos.value.filter(t => t.taskType === key && t.status !== 'COMPLETED').length
}

const getTypeLabel = (type) => {
  const map = { WORK: '工作', PERSONAL: '个人', STUDY: '学习', OTHER: '其他' }
  return map[type] || type || ''
}

const loadTodos = async () => {
  loading.value = true
  try {
    const res = await getMyTasks()
    if (res.code === 200) todos.value = res.data || []
  } finally { loading.value = false }
}

const handleToggle = async (todo) => {
  try {
    const newStatus = todo.status === 'COMPLETED' ? 'PENDING' : 'COMPLETED'
    const res = await updateTaskStatus(todo.id, newStatus)
    if (res.code === 200) {
      todo.status = newStatus
      ElMessage.success(newStatus === 'COMPLETED' ? '任务已完成' : '任务已重开')
    }
  } catch (e) { ElMessage.error('操作失败') }
}

const handleDelete = (todo) => {
  ElMessageBox.confirm('确定删除该任务吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteTask(todo.id)
    if (res.code === 200) {
      todos.value = todos.value.filter(t => t.id !== todo.id)
      ElMessage.success('已删除')
    }
  })
}

const handleEdit = (todo) => {
  editingTodo.value = { ...todo }
  showAddDialog.value = true
}

const handleViewDetail = (todo) => {
  editingTodo.value = { ...todo }
  showAddDialog.value = true
}

const isOverdue = (date) => {
  if (!date || todos.value.find(t => t.id === arguments[0]?.id)?.status === 'COMPLETED') return false
  return new Date(date) < new Date()
}

const formatDate = (date) => date ? new Date(date).toLocaleDateString() : '暂无截止日期'

const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => loadTodos())
</script>

<style scoped lang="scss">
.todo-panel { display: flex; height: 100%; flex: 1; background: var(--dt-bg-card); }
.todo-sidebar { width: var(--dt-contact-panel-width, 240px); border-right: 1px solid var(--dt-border-light); display: flex; flex-direction: column; flex-shrink: 0; }
.sidebar-header { padding: var(--dt-spacing-md, 16px) var(--dt-spacing-md, 20px); .sidebar-title { font-size: var(--dt-font-size-base); font-weight: var(--dt-font-weight-semibold); color: var(--dt-text-primary); } }
.sidebar-content { flex: 1; padding: var(--dt-spacing-xs, 8px) var(--dt-spacing-sm, 12px); overflow-y: auto;
  .nav-section { margin-bottom: var(--dt-spacing-md, 16px);
    .nav-section-title { font-size: var(--dt-font-size-xs, 12px); color: var(--dt-text-tertiary); padding: var(--dt-spacing-xs, 8px); font-weight: var(--dt-font-weight-medium); }
  }
  .nav-item { display: flex; align-items: center; gap: var(--dt-spacing-sm, 10px); padding: var(--dt-spacing-sm, 10px) var(--dt-spacing-xs, 12px); margin: var(--dt-spacing-xs, 2px) 0; border-radius: var(--dt-radius-sm); cursor: pointer; color: var(--dt-text-primary); transition: all var(--dt-transition-fast);
    &:hover { background: var(--dt-bg-session-hover); }
    &.active { background: var(--dt-brand-bg); color: var(--dt-brand-color); font-weight: var(--dt-font-weight-semibold); }
    .nav-icon { font-size: var(--dt-icon-size-lg, 18px); }
    .nav-label { flex: 1; font-size: var(--dt-font-size-sm); }
    .nav-badge { background: var(--dt-error-color); color: #fff; padding: 0 var(--dt-spacing-xs, 6px); border-radius: var(--dt-radius-full, 10px); font-size: var(--dt-font-size-xs); transform: scale(0.85); min-width: 18px; text-align: center;
      &.cat { background: var(--dt-brand-color); }
    }
  }
}
.sidebar-footer { padding: var(--dt-spacing-md, 16px); border-top: 1px solid var(--dt-border-light);
  .add-todo-btn { width: 100%; height: var(--dt-btn-height-md, 36px); background: var(--dt-brand-color); color: #fff; border: none; border-radius: var(--dt-radius-sm); display: flex; align-items: center; justify-content: center; gap: var(--dt-spacing-xs, 8px); font-size: var(--dt-font-size-sm); font-weight: var(--dt-font-weight-medium); cursor: pointer; transition: all var(--dt-transition-fast);
    &:hover { background: var(--dt-brand-hover); box-shadow: var(--dt-shadow-2); }
  }
}
.todo-main { flex: 1; display: flex; flex-direction: column; background: var(--dt-bg-card); overflow: hidden; }
.todo-header { height: var(--dt-toolbar-height, 56px); padding: 0 var(--dt-spacing-lg, 24px); border-bottom: 1px solid var(--dt-border-light); display: flex; align-items: center; justify-content: space-between;
  .header-title { font-size: var(--dt-font-size-base); font-weight: var(--dt-font-weight-semibold); color: var(--dt-text-primary); }
  .search-box { position: relative; .search-icon { position: absolute; left: var(--dt-spacing-xs, 8px); top: 50%; transform: translateY(-50%); font-size: var(--dt-icon-size-lg, 16px); color: var(--dt-text-tertiary); }
    .search-input { width: 240px; height: var(--dt-btn-height-sm, 32px); background: var(--dt-bg-body); border: none; border-radius: var(--dt-radius-sm); padding: 0 var(--dt-spacing-lg, 32px); font-size: var(--dt-font-size-sm); outline: none; &:focus { background: var(--dt-bg-card); box-shadow: 0 0 0 2px var(--dt-brand-lighter); } }
  }
}
.todo-content-scroller { flex: 1; padding: var(--dt-spacing-lg, 20px) var(--dt-spacing-lg, 24px); overflow-y: auto; background: var(--dt-bg-body); }
.todo-list { display: flex; flex-direction: column; gap: var(--dt-spacing-sm, 12px); }
.todo-card { background: var(--dt-bg-card); border-radius: var(--dt-radius-md); padding: var(--dt-spacing-md, 16px); display: flex; align-items: flex-start; gap: var(--dt-spacing-md, 14px); border: 1px solid var(--dt-border-lighter); box-shadow: var(--dt-shadow-1); cursor: pointer; transition: all var(--dt-transition-fast);
  &:hover { transform: translateY(var(--dt-transform-y, -1px)); box-shadow: var(--dt-shadow-2); .todo-actions { opacity: 1; } }
  &.completed { opacity: 0.6; .title-text { text-decoration: line-through; } }
}
.todo-checkbox { flex-shrink: 0; .checkbox-inner { width: var(--dt-checkbox-size, 20px); height: var(--dt-checkbox-size, 20px); border: 2px solid var(--dt-border-color); border-radius: var(--dt-radius-sm); display: flex; align-items: center; justify-content: center;
  &.checked { background: var(--dt-brand-color); border-color: var(--dt-brand-color); color: #fff; .material-icons-outlined { font-size: var(--dt-font-size-xs); } } }
}
.todo-main-info { flex: 1; min-width: 0;
  .todo-title-row { display: flex; align-items: center; gap: var(--dt-spacing-xs, 8px); flex-wrap: wrap; .title-text { font-size: var(--dt-font-size-sm); font-weight: var(--dt-font-weight-medium); color: var(--dt-text-primary); }
    .priority-tag { padding: 0 var(--dt-spacing-xs, 6px); border-radius: var(--dt-radius-sm); font-size: var(--dt-font-size-xs);
      &.high { background: var(--dt-error-bg); color: var(--dt-error-color); }
    }
    .category-tag { padding: 0 var(--dt-spacing-xs, 6px); border-radius: var(--dt-radius-sm); font-size: var(--dt-font-size-xs);
      &.work { background: #e3f2fd; color: #1976d2; }
      &.personal { background: #f3e5f5; color: #7b1fa2; }
      &.study { background: #e8f5e9; color: #388e3c; }
      &.other { background: #fafafa; color: #616161; }
    }
  }
  .todo-meta-row { margin-top: var(--dt-spacing-xs, 6px); display: flex; align-items: center; gap: var(--dt-spacing-md, 12px);
    .meta-date, .meta-remind { display: flex; align-items: center; gap: var(--dt-spacing-xs, 4px); font-size: var(--dt-font-size-xs); color: var(--dt-text-tertiary); .material-icons-outlined { font-size: var(--dt-icon-size-sm, 14px); } &.overdue { color: var(--dt-error-color); } }
  }
}
.todo-actions { opacity: 0; display: flex; gap: var(--dt-spacing-xs, 4px); transition: all var(--dt-transition-fast); .action-icon { border: none; background: transparent; cursor: pointer; color: var(--dt-text-tertiary); padding: 4px; &:hover { color: var(--dt-brand-color); } &.danger:hover { color: var(--dt-error-color); } } }
.empty-state, .loading-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: var(--dt-spacing-3xl, 100px) 0; color: var(--dt-text-tertiary); .material-icons-outlined { font-size: var(--dt-icon-size-xl, 48px); margin-bottom: var(--dt-spacing-md, 12px); opacity: 0.3; } }
</style>