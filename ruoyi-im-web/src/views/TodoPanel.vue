<template>
  <div class="todo-v2">
    <!-- 1. 左侧分类导航 (240px) -->
    <aside class="todo-sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">待办事项</h2>
        <button class="add-todo-icon-btn" @click="showAddDialog = true"><el-icon><Plus /></el-icon></button>
      </div>
      <div class="sidebar-content">
        <div class="nav-section">
          <div v-for="tab in statusTabs" :key="tab.key" class="nav-item" :class="{ active: activeFilter === tab.key }" @click="activeFilter = tab.key">
            <el-icon class="nav-icon"><component :is="tab.iconEl" /></el-icon>
            <span class="nav-label">{{ tab.label }}</span>
            <div v-if="getCount(tab.key) > 0" class="nav-badge">{{ getCount(tab.key) }}</div>
          </div>
        </div>
        <div class="nav-divider"></div>
        <div class="nav-section">
          <div class="section-title">我的清单</div>
          <div v-for="cat in categoryTabs" :key="cat.key" class="nav-item" :class="{ active: activeFilter === cat.key }" @click="activeFilter = cat.key">
            <el-icon class="nav-icon"><component :is="cat.iconEl" /></el-icon>
            <span class="nav-label">{{ cat.label }}</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- 2. 中间任务列表区 -->
    <main class="todo-main">
      <header class="todo-header">
        <div class="header-left">
          <h2 class="view-title">{{ currentTabLabel }}</h2>
        </div>
        <div class="header-right">
          <div class="todo-search">
            <el-icon><Search /></el-icon>
            <input v-model="searchQuery" placeholder="搜索待办..." />
          </div>
        </div>
      </header>

      <div class="todo-body custom-scrollbar">
        <div v-if="filteredTodos.length === 0" class="empty-view">
          <el-icon class="empty-icon"><CircleCheck /></el-icon>
          <p>太棒了，目前没有待办任务</p>
        </div>
        <div v-else class="todo-list">
          <div v-for="todo in filteredTodos" :key="todo.id" class="todo-card-v2" :class="{ completed: todo.status === 'COMPLETED' }" @click="handleViewDetail(todo)">
            <!-- 优先级彩条 -->
            <div class="priority-indicator" :class="todo.priorityClass"></div>
            
            <div class="todo-checkbox" @click.stop="handleToggle(todo)">
              <div class="checkbox-box" :class="{ checked: todo.status === 'COMPLETED' }">
                <el-icon v-if="todo.status === 'COMPLETED'"><Check /></el-icon>
              </div>
            </div>

            <div class="todo-main-content">
              <div class="todo-title">{{ todo.title }}</div>
              <div class="todo-meta">
                <span class="meta-item" :class="{ overdue: isOverdue(todo) }">
                  <el-icon><Calendar /></el-icon>
                  {{ formatDate(todo.dueDate) }}
                </span>
                <span v-if="todo.taskType" class="meta-tag">{{ getTypeLabel(todo.taskType) }}</span>
              </div>
            </div>
            
            <div class="todo-actions">
              <el-icon class="op-icon" @click.stop="handleEdit(todo)"><Edit /></el-icon>
              <el-icon class="op-icon del" @click.stop="handleDelete(todo)"><Delete /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 3. 右侧详情面板 (Conditional) -->
    <transition name="slide">
      <aside v-if="detailVisible" class="todo-detail-aside">
        <div class="detail-header">
          <span>任务详情</span>
          <el-icon @click="detailVisible = false"><Close /></el-icon>
        </div>
        <div class="detail-body">
          <h3 class="detail-title">{{ editingTodo?.title }}</h3>
          <div class="detail-info-grid">
            <div class="grid-row"><label>截止日期</label><span>{{ formatDate(editingTodo?.dueDate) }}</span></div>
            <div class="grid-row"><label>优先级</label><span :class="editingTodo?.priorityClass">{{ editingTodo?.priority }}</span></div>
            <div class="grid-row"><label>描述</label><p>{{ editingTodo?.description || '暂无描述' }}</p></div>
          </div>
        </div>
      </aside>
    </transition>
  </div>
</template>

<script setup lang="js">
import { ref, computed, onMounted } from 'vue'
import { 
  Plus, List, Clock, Check, Calendar, Bell, Edit, 
  Delete, CircleCheck, Search, Box, User, Reading, MoreFilled, Close, ArrowUp 
} from '@element-plus/icons-vue'
import { getMyTasks, updateTaskStatus, deleteTask } from '@/api/im/task'

const activeFilter = ref('all')
const searchQuery = ref('')
const todos = ref([])
const editingTodo = ref(null)
const detailVisible = ref(false)

const statusTabs = [
  { key: 'all', label: '全部待办', iconEl: List },
  { key: 'pending', label: '进行中', iconEl: Clock },
  { key: 'high', label: '紧急任务', iconEl: ArrowUp },
  { key: 'completed', label: '已完成', iconEl: CircleCheck }
]

const categoryTabs = [
  { key: 'WORK', label: '工作清单', iconEl: Box },
  { key: 'PERSONAL', label: '个人私事', iconEl: User },
  { key: 'OTHER', label: '其它事项', iconEl: MoreFilled }
]

const filteredTodos = computed(() => {
  let list = todos.value
  if (activeFilter.value === 'pending') list = list.filter(t => t.status !== 'COMPLETED')
  else if (activeFilter.value === 'completed') list = list.filter(t => t.status === 'COMPLETED')
  else if (activeFilter.value === 'high') list = list.filter(t => t.priority === 'HIGH' && t.status !== 'COMPLETED')
  
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(t => t.title.toLowerCase().includes(q))
  }
  return list
})

const currentTabLabel = computed(() => [...statusTabs, ...categoryTabs].find(t => t.key === activeFilter.value)?.label || '任务')

const loadTodos = async () => {
  const res = await getMyTasks()
  if (res.code === 200) {
    todos.value = (res.data || []).map(t => ({
      ...t,
      priorityClass: t.priority?.toLowerCase() || 'low'
    }))
  }
}

const handleViewDetail = (todo) => {
  editingTodo.value = todo
  detailVisible.value = true
}

const formatDate = (d) => d ? new Date(d).toLocaleDateString() : '无日期'
const isOverdue = (t) => t.dueDate && new Date(t.dueDate) < new Date() && t.status !== 'COMPLETED'
const getTypeLabel = (t) => ({ WORK: '工作', PERSONAL: '个人' }[t] || '其它')

onMounted(loadTodos)
</script>

<style scoped lang="scss">
.todo-v2 {
  display: flex; height: 100%; background: #fff; overflow: hidden;
}

.todo-sidebar {
  width: 240px; background: #f8fbff; border-right: 1px solid var(--dt-border-light);
  display: flex; flex-direction: column;
  .sidebar-header { padding: 24px 20px; display: flex; justify-content: space-between; align-items: center; .sidebar-title { font-size: 18px; font-weight: 700; margin: 0; } }
  .add-todo-icon-btn { width: 28px; height: 28px; border-radius: 6px; border: none; background: var(--dt-brand-color); color: #fff; cursor: pointer; @include flex-center; }
  .sidebar-content {
    flex: 1; padding: 12px 8px;
    .nav-section { margin-bottom: 20px; .section-title { font-size: 11px; color: var(--dt-text-quaternary); padding: 8px 12px; } }
    .nav-item {
      height: 40px; display: flex; align-items: center; gap: 12px; padding: 0 12px;
      border-radius: 8px; cursor: pointer; color: var(--dt-text-secondary); margin-bottom: 2px;
      &:hover { background: #eff4fc; }
      &.active { background: var(--dt-brand-bg); color: var(--dt-brand-color); font-weight: 600; }
      .nav-badge { margin-left: auto; font-size: 11px; background: var(--dt-error-color); color: #fff; padding: 0 6px; border-radius: 10px; }
    }
    .nav-divider { height: 1px; background: rgba(0,0,0,0.04); margin: 12px; }
  }
}

.todo-main { flex: 1; display: flex; flex-direction: column; background: #fff; }

.todo-header {
  height: 56px; padding: 0 24px; border-bottom: 1px solid var(--dt-border-light);
  display: flex; align-items: center; justify-content: space-between;
  .view-title { font-size: 16px; font-weight: 600; }
}

.todo-search {
  width: 200px; height: 32px; background: var(--dt-bg-hover); border-radius: 16px;
  display: flex; align-items: center; padding: 0 12px; color: var(--dt-text-tertiary);
  input { flex: 1; border: none; background: transparent; outline: none; font-size: 13px; margin-left: 8px; }
}

.todo-body { flex: 1; overflow-y: auto; padding: 16px 24px; }

.todo-card-v2 {
  position: relative; display: flex; align-items: center; padding: 12px 16px;
  background: #fff; border: 1px solid var(--dt-border-light); border-radius: 12px;
  margin-bottom: 12px; cursor: pointer; transition: 0.2s;
  &:hover { border-color: var(--dt-brand-light); box-shadow: var(--dt-shadow-1); .todo-actions { opacity: 1; } }
  &.completed { opacity: 0.6; .todo-title { text-decoration: line-through; } }
}

.priority-indicator {
  position: absolute; left: 0; top: 12px; bottom: 12px; width: 4px; border-radius: 0 2px 2px 0;
  &.high { background: var(--dt-error-color); }
  &.medium { background: var(--dt-warning-color); }
  &.low { background: var(--dt-brand-color); }
}

.checkbox-box {
  width: 18px; height: 18px; border: 2px solid var(--dt-border-color); border-radius: 4px;
  @include flex-center; margin-right: 16px; color: transparent;
  &.checked { background: var(--dt-brand-color); border-color: var(--dt-brand-color); color: #fff; }
}

.todo-main-content {
  flex: 1; min-width: 0;
  .todo-title { font-size: 14px; font-weight: 500; color: var(--dt-text-primary); }
  .todo-meta {
    display: flex; align-items: center; gap: 16px; margin-top: 6px;
    .meta-item { display: flex; align-items: center; gap: 4px; font-size: 12px; color: var(--dt-text-tertiary); }
    .meta-item.overdue { color: var(--dt-error-color); font-weight: 600; }
    .meta-tag { font-size: 11px; background: var(--dt-bg-hover); padding: 1px 6px; border-radius: 4px; color: var(--dt-text-secondary); }
  }
}

.todo-actions {
  display: flex; gap: 12px; opacity: 0; transition: 0.2s;
  .op-icon { font-size: 16px; color: var(--dt-text-tertiary); &:hover { color: var(--dt-brand-color); } &.del:hover { color: var(--dt-error-color); } }
}

.todo-detail-aside {
  width: 320px; background: #fdfdfe; border-left: 1px solid var(--dt-border-light);
  display: flex; flex-direction: column;
  .detail-header { height: 56px; padding: 0 16px; display: flex; align-items: center; justify-content: space-between; font-weight: 600; border-bottom: 1px solid var(--dt-border-light); }
  .detail-body { padding: 24px; .detail-title { font-size: 18px; margin-bottom: 24px; } }
}

.slide-enter-active, .slide-leave-active { transition: transform 0.3s ease; }
.slide-enter-from, .slide-leave-to { transform: translateX(100%); }
</style>
