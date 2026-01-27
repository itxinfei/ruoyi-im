<template>
  <div class="todo-panel">
    <!-- 头部区域 -->
    <div class="panel-header">
      <div class="header-left">
        <h2 class="panel-title">待办事项</h2>
        <div class="title-badge">
          <span class="material-icons-outlined badge-icon">task_alt</span>
          <span class="badge-count">{{ todos.filter(t => !t.completed).length }}</span>
        </div>
      </div>
      <button class="add-btn" @click="showAddDialog = true">
        <span class="material-icons-outlined">add</span>
        <span>新建待办</span>
      </button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-icon stat-icon--total">
          <span class="material-icons-outlined">list</span>
        </span>
        <div class="stat-info">
          <span class="stat-value">{{ todos.length }}</span>
          <span class="stat-label">全部待办</span>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon stat-icon--pending">
          <span class="material-icons-outlined">schedule</span>
        </span>
        <div class="stat-info">
          <span class="stat-value">{{ todos.filter(t => !t.completed).length }}</span>
          <span class="stat-label">进行中</span>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon stat-icon--high">
          <span class="material-icons-outlined">priority_high</span>
        </span>
        <div class="stat-info">
          <span class="stat-value">{{ todos.filter(t => t.priority === 'high' && !t.completed).length }}</span>
          <span class="stat-label">紧急</span>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon stat-icon--done">
          <span class="material-icons-outlined">check_circle</span>
        </span>
        <div class="stat-info">
          <span class="stat-value">{{ todos.filter(t => t.completed).length }}</span>
          <span class="stat-label">已完成</span>
        </div>
      </div>
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
        <span class="filter-tab-icon material-icons-outlined">{{ tab.icon }}</span>
        <span class="filter-tab-label">{{ tab.label }}</span>
        <span v-if="getCount(tab.key) > 0" class="tab-count">{{ getCount(tab.key) }}</span>
      </button>
    </div>

    <!-- 内容区域 -->
    <div class="panel-content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="filteredTodos.length === 0" class="empty-state">
        <div class="empty-illustration">
          <span class="material-icons-outlined">{{ emptyIcon }}</span>
          <div class="empty-decoration"></div>
        </div>
        <h3 class="empty-title">{{ emptyTitle }}</h3>
        <p class="empty-text">{{ emptyText }}</p>
        <button v-if="activeFilter === 'all'" class="empty-action" @click="showAddDialog = true">
          <span class="material-icons-outlined">add</span>
          创建第一个待办
        </button>
      </div>

      <div v-else class="todo-list">
        <div
          v-for="todo in filteredTodos"
          :key="todo.id"
          class="todo-item"
          :class="{
            completed: todo.completed,
            overdue: isOverdue(todo.dueDate),
            [`priority-${todo.priority}`]: true
          }"
          @click="handleViewDetail(todo)"
        >
          <!-- 优先级指示条 -->
          <div class="priority-indicator" :class="`priority-${todo.priority}`"></div>

          <!-- 复选框 -->
          <div class="todo-checkbox" @click.stop="toggleComplete(todo)">
            <div class="checkbox-inner" :class="{ checked: todo.completed }">
              <span v-if="todo.completed" class="material-icons-outlined check-icon">check</span>
            </div>
          </div>

          <!-- 内容 -->
          <div class="todo-content">
            <div class="todo-header">
              <h4 class="todo-title">{{ todo.title }}</h4>
              <span class="todo-priority-badge" :class="`priority-${todo.priority}`">
                {{ priorityText(todo.priority) }}
              </span>
            </div>
            <p v-if="todo.content" class="todo-desc">{{ todo.content }}</p>
            <div class="todo-meta">
              <span class="todo-date" :class="{ overdue: isOverdue(todo.dueDate) }">
                <span class="material-icons-outlined date-icon">{{ dateIcon(todo.dueDate) }}</span>
                {{ formatDate(todo.dueDate) }}
              </span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="todo-actions" @click.stop>
            <el-tooltip content="编辑" placement="top">
              <button class="action-btn" @click="handleEdit(todo)">
                <span class="material-icons-outlined">edit</span>
              </button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <button class="action-btn action-btn--danger" @click="deleteTodo(todo)">
                <span class="material-icons-outlined">delete</span>
              </button>
            </el-tooltip>
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

// 筛选标签配置
const filterTabs = [
  { key: 'all', label: '全部', icon: 'apps' },
  { key: 'pending', label: '进行中', icon: 'schedule' },
  { key: 'completed', label: '已完成', icon: 'check_circle' },
  { key: 'high', label: '紧急', icon: 'priority_high' }
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

  // 按完成状态、优先级、截止日期排序
  return list.sort((a, b) => {
    if (a.completed !== b.completed) return a.completed ? 1 : -1
    const priorityOrder = { high: 0, medium: 1, low: 2 }
    if (priorityOrder[a.priority] !== priorityOrder[b.priority]) {
      return priorityOrder[a.priority] - priorityOrder[b.priority]
    }
    if (a.dueDate && b.dueDate) return new Date(a.dueDate) - new Date(b.dueDate)
    return a.dueDate ? -1 : 1
  })
})

// 空状态配置
const emptyConfig = computed(() => {
  const configs = {
    all: {
      icon: 'check_circle',
      title: '暂无待办事项',
      text: '还没有创建任何待办，点击下方按钮创建第一个待办吧'
    },
    pending: {
      icon: 'task_alt',
      title: '全部完成！',
      text: '你真棒，所有待办都已完成'
    },
    completed: {
      icon: 'history',
      title: '暂无已完成',
      text: '还没有完成任何待办，继续加油'
    },
    high: {
      icon: 'security',
      title: '没有紧急待办',
      text: '当前没有紧急待办，可以放松一下'
    }
  }
  return configs[activeFilter.value] || configs.all
})

const emptyIcon = computed(() => emptyConfig.value.icon)
const emptyTitle = computed(() => emptyConfig.value.title)
const emptyText = computed(() => emptyConfig.value.text)

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

// 日期图标
const dateIcon = (dueDate) => {
  if (!dueDate) return 'event_busy'
  if (isOverdue(dueDate)) return 'warning'
  const diff = new Date(dueDate) - new Date()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days === 0) return 'today'
  if (days <= 2) return 'event_available'
  return 'event'
}

// 数据转换：后端 -> 前端
const transformTodoFromApi = (apiTodo) => {
  // 优先级转换：1=低, 2=中, 3=高 -> low/medium/high
  const priorityMap = { 1: 'low', 2: 'medium', 3: 'high' }
  const priority = priorityMap[apiTodo.priority] || 'medium'
  // 状态转换：COMPLETED -> completed=true
  const completed = apiTodo.status === 'COMPLETED'
  // 格式化截止日期
  const dueDate = apiTodo.dueDate ? new Date(apiTodo.dueDate).toISOString() : null

  return {
    id: apiTodo.id,
    title: apiTodo.title,
    content: apiTodo.description || '',
    priority: priority,
    completed: completed,
    dueDate: dueDate,
    completedTime: apiTodo.completedTime,
    createdAt: apiTodo.createTime,
    priorityClass: getPriorityClass(priority)
  }
}

// 数据转换：前端 -> 后端
const transformTodoToApi = (todo) => {
  // 优先级转换：low/medium/high -> 1/2/3
  const priorityMap = { low: 1, medium: 2, high: 3 }
  return {
    title: todo.title,
    description: todo.content,
    priority: priorityMap[todo.priority] || 2
  }
}

// 加载待办列表
const loadTodos = async () => {
  loading.value = true
  try {
    const res = await getTodos()
    if (res.code === 200) {
      todos.value = (res.data || []).map(item => transformTodoFromApi(item))
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

  if (days === 0) return '今天截止'
  if (days === 1) return '明天截止'
  if (days === -1) return '昨天截止'
  if (days < -1) return `逾期 ${Math.abs(days)} 天`
  if (days <= 7) return `${days} 天后截止`

  return `${d.getMonth() + 1}月${d.getDate()}日`
}

const toggleComplete = async (todo) => {
  try {
    const res = await completeTodo(todo.id)
    if (res.code === 200) {
      todo.completed = !todo.completed
      ElMessage.success(todo.completed ? '已标记完成' : '已重启')
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
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
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

const handleViewDetail = (todo) => {
  selectedTodo.value = todo
  showDetailDialog.value = true
}

const handleEdit = (todo) => {
  editingTodo.value = todo
  showAddDialog.value = true
}

const handleEditFromDetail = (todo) => {
  showDetailDialog.value = false
  handleEdit(todo)
}

const handleTodoCreated = () => {
  editingTodo.value = null
  loadTodos()
}

onMounted(() => {
  loadTodos()
})
</script>

<style scoped lang="scss">
// ============================================================================
// 容器
// ============================================================================
.todo-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: var(--dt-bg-body);
}

// ============================================================================
// 头部区域
// ============================================================================
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.panel-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.title-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: var(--dt-brand-bg);
  border-radius: var(--dt-radius-full);
}

.badge-icon {
  font-size: 14px;
  color: var(--dt-brand-color);
}

.badge-count {
  font-size: 12px;
  font-weight: 600;
  color: var(--dt-brand-color);
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  background: var(--dt-brand-color);
  color: #fff;
  border: none;
  border-radius: var(--dt-radius-lg);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-brand-hover);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
  }

  &:active {
    transform: translateY(0);
  }
}

// ============================================================================
// 统计卡片
// ============================================================================
.stats-row {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  padding: 12px 16px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-lg);
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-card-hover);
    transform: translateY(-2px);
  }
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .material-icons-outlined {
    font-size: 20px;
    color: #fff;
  }

  &--total {
    background: linear-gradient(135deg, #1677ff 0%, #0e5fd9 100%);
  }

  &--pending {
    background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
  }

  &--high {
    background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
  }

  &--done {
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  }
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--dt-text-primary);
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

// ============================================================================
// 筛选标签
// ============================================================================
.filter-tabs {
  display: flex;
  padding: 12px 24px 0;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  gap: 4px;
  flex-shrink: 0;
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-lg) var(--dt-radius-lg) 0 0;
  font-size: 13px;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-base);
  position: relative;
  margin-bottom: -1px;

  &:hover {
    color: var(--dt-text-primary);
    background: var(--dt-bg-body);
  }

  &.active {
    color: var(--dt-brand-color);
    background: #fff;
    font-weight: 500;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 2px;
      background: var(--dt-brand-color);
    }
  }

  .dark &.active {
    background: var(--dt-bg-body-dark);
  }
}

.filter-tab-icon {
  font-size: 16px;
}

.filter-tab-label {
  font-size: 13px;
}

.tab-count {
  padding: 2px 7px;
  background: var(--dt-brand-color);
  color: #fff;
  border-radius: var(--dt-radius-full);
  font-size: 11px;
  font-weight: 600;
  min-width: 18px;
  text-align: center;
}

.filter-tab:not(.active) .tab-count {
  background: var(--dt-border-color);
  color: var(--dt-text-tertiary);
}

// ============================================================================
// 内容区域
// ============================================================================
.panel-content {
  flex: 1;
  padding: 20px 24px;
  overflow-y: auto;
}

// ============================================================================
// 加载和空状态
// ============================================================================
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--dt-text-tertiary);
  gap: 12px;

  .el-icon {
    font-size: 32px;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-illustration {
  position: relative;
  margin-bottom: 24px;

  .material-icons-outlined {
    font-size: 80px;
    color: var(--dt-border-color);
  }
}

.empty-decoration {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 6px;
  background: var(--dt-border-color);
  border-radius: var(--dt-radius-full);
  opacity: 0.5;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0 0 8px 0;
}

.empty-text {
  font-size: 14px;
  color: var(--dt-text-tertiary);
  margin: 0 0 24px 0;
  max-width: 280px;
}

.empty-action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: var(--dt-brand-color);
  color: #fff;
  border: none;
  border-radius: var(--dt-radius-lg);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-brand-hover);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
  }
}

// ============================================================================
// 待办列表
// ============================================================================
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  position: relative;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  padding: 16px;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  box-shadow: var(--dt-shadow-card);
  transition: all var(--dt-transition-base);
  cursor: pointer;
  overflow: hidden;

  &:hover {
    box-shadow: var(--dt-shadow-card-hover);
    transform: translateY(-2px);
  }

  &.completed {
    opacity: 0.6;

    .todo-title {
      text-decoration: line-through;
      color: var(--dt-text-tertiary);
    }
  }

  &.overdue {
    .priority-indicator {
      background: var(--dt-error-color);
    }
  }
}

.priority-indicator {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;

  &.priority-high {
    background: var(--dt-error-color);
  }

  &.priority-medium {
    background: var(--dt-warning-color);
  }

  &.priority-low {
    background: var(--dt-success-color);
  }
}

// ============================================================================
// 复选框
// ============================================================================
.todo-checkbox {
  flex-shrink: 0;
  padding-top: 2px;
}

.checkbox-inner {
  width: 22px;
  height: 22px;
  border: 2px solid var(--dt-border-color);
  border-radius: var(--dt-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--dt-transition-base);
  background: var(--dt-bg-card);

  .todo-item:hover .todo-checkbox & {
    border-color: var(--dt-brand-color);
  }

  &.checked {
    background: var(--dt-brand-color);
    border-color: var(--dt-brand-color);

    .check-icon {
      color: #fff;
      font-size: 16px;
      animation: checkBounce 0.3s ease-out;
    }
  }
}

@keyframes checkBounce {
  0% { transform: scale(0); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

// ============================================================================
// 待办内容
// ============================================================================
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
  font-weight: 500;
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
  flex-shrink: 0;

  &.priority-high {
    background: var(--dt-error-bg);
    color: var(--dt-error-color);
  }

  &.priority-medium {
    background: var(--dt-warning-bg);
    color: var(--dt-warning-color);
  }

  &.priority-low {
    background: var(--dt-success-bg);
    color: var(--dt-success-color);
  }
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
  line-height: 1.4;
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
  color: var(--dt-text-tertiary);

  .date-icon {
    font-size: 14px;
  }

  &.overdue {
    color: var(--dt-error-color);
    font-weight: 500;
  }
}

// ============================================================================
// 操作按钮
// ============================================================================
.todo-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity var(--dt-transition-base);
  flex-shrink: 0;
}

.todo-item:hover .todo-actions {
  opacity: 1;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--dt-radius-md);
  background: var(--dt-bg-body);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--dt-transition-base);
  color: var(--dt-text-secondary);

  &:hover {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }

  &--danger:hover {
    background: var(--dt-error-bg);
    color: var(--dt-error-color);
  }

  .material-icons-outlined {
    font-size: 16px;
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
.dark .todo-panel {
  background: var(--dt-bg-body-dark);
}

.dark .panel-header,
.dark .filter-tabs {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .panel-title,
.dark .stat-value {
  color: var(--dt-text-primary-dark);
}

.dark .stat-label,
.dark .filter-tab:not(.active),
.dark .todo-desc,
.dark .todo-date {
  color: var(--dt-text-tertiary-dark);
}

.dark .filter-tab {
  color: var(--dt-text-secondary-dark);
}

.dark .filter-tab.active {
  color: var(--dt-brand-color);
}

.dark .todo-item {
  background: var(--dt-bg-card-dark);
}

.dark .todo-title {
  color: var(--dt-text-primary-dark);
}

.dark .checkbox-inner {
  background: var(--dt-bg-input-dark);
  border-color: var(--dt-border-dark);
}

.dark .action-btn {
  background: var(--dt-bg-hover-dark);
  color: var(--dt-text-secondary-dark);
}

.dark .empty-illustration .material-icons-outlined {
  color: var(--dt-border-dark);
}

.dark .empty-decoration {
  background: var(--dt-border-dark);
}

.dark .stat-card {
  background: var(--dt-bg-hover-dark);
}

.dark .filter-tab:hover {
  background: var(--dt-bg-hover-dark);
}
</style>
