<template>
  <div class="todo-panel">
    <!-- 头部区域 -->
    <div class="panel-header">
      <div class="header-left">
        <div class="title-wrapper">
          <h2 class="panel-title">待办事项</h2>
          <div class="title-badge">
            <div class="badge-icon-wrapper">
              <span class="material-icons-outlined badge-icon">task_alt</span>
              <div class="icon-glow"></div>
            </div>
            <span class="badge-count">{{ todos.filter(t => !t.completed).length }}</span>
          </div>
        </div>
        <!-- 搜索框 -->
        <div class="search-wrapper" v-if="!batchMode">
          <span class="material-icons-outlined search-icon">search</span>
          <input
            v-model="searchInput"
            type="text"
            class="search-input"
            placeholder="搜索待办..."
            @input="handleSearchInput"
          />
        </div>
      </div>
      <div class="header-actions">
        <!-- 批量操作按钮 -->
        <template v-if="batchMode">
          <span class="selected-count">已选 {{ selectedTodos.size }} 项</span>
          <button class="batch-btn batch-btn--complete" @click="batchComplete" :disabled="selectedTodos.size === 0">
            <span class="material-icons-outlined">check_circle</span>
            <span>完成</span>
          </button>
          <button class="batch-btn batch-btn--danger" @click="batchDelete" :disabled="selectedTodos.size === 0">
            <span class="material-icons-outlined">delete</span>
            <span>删除</span>
          </button>
          <button class="batch-btn batch-btn--cancel" @click="exitBatchMode">
            <span class="material-icons-outlined">close</span>
            <span>取消</span>
          </button>
        </template>
        <template v-else>
          <button class="icon-btn" @click="enterBatchMode" title="批量操作">
            <span class="material-icons-outlined">checklist</span>
            <span>批量</span>
          </button>
          <button class="add-btn" @click="showAddDialog = true">
            <span class="material-icons-outlined">add</span>
            <span>新建待办</span>
          </button>
        </template>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card stat-card--total">
        <div class="stat-icon-wrapper">
          <span class="material-icons-outlined stat-icon">
            <div class="icon-bg"></div>
            list
          </span>
          <div class="icon-glow icon-glow--primary"></div>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ todos.length }}</span>
          <span class="stat-label">全部待办</span>
        </div>
        <div class="stat-decoration"></div>
      </div>
      <div class="stat-card stat-card--pending">
        <div class="stat-icon-wrapper">
          <span class="material-icons-outlined stat-icon">
            <div class="icon-bg"></div>
            schedule
          </span>
          <div class="icon-glow icon-glow--warning"></div>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ todos.filter(t => !t.completed).length }}</span>
          <span class="stat-label">进行中</span>
        </div>
        <div class="stat-decoration"></div>
      </div>
      <div class="stat-card stat-card--high">
        <div class="stat-icon-wrapper">
          <span class="material-icons-outlined stat-icon">
            <div class="icon-bg"></div>
            priority_high
          </span>
          <div class="icon-glow icon-glow--danger"></div>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ todos.filter(t => t.priority === 'high' && !t.completed).length }}</span>
          <span class="stat-label">紧急</span>
        </div>
        <div class="stat-decoration"></div>
      </div>
      <div class="stat-card stat-card--done">
        <div class="stat-icon-wrapper">
          <span class="material-icons-outlined stat-icon">
            <div class="icon-bg"></div>
            check_circle
          </span>
          <div class="icon-glow icon-glow--success"></div>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ todos.filter(t => t.completed).length }}</span>
          <span class="stat-label">已完成</span>
        </div>
        <div class="stat-decoration"></div>
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
        <div class="tab-indicator"></div>
      </button>
    </div>

    <!-- 内容区域 -->
    <div class="panel-content">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner">
          <div class="spinner-ring"></div>
          <div class="spinner-dot"></div>
        </div>
        <span class="loading-text">加载中...</span>
      </div>

      <div v-else-if="filteredTodos.length === 0" class="empty-state">
        <div class="empty-illustration">
          <div class="empty-icon-wrapper">
            <span class="material-icons-outlined empty-icon">{{ emptyIcon }}</span>
            <div class="icon-rings">
              <div class="ring"></div>
              <div class="ring"></div>
              <div class="ring"></div>
            </div>
          </div>
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
        <transition-group name="todo-item">
          <div
            v-for="todo in filteredTodos"
            :key="todo.id"
            class="todo-item"
            :class="{
              completed: todo.completed,
              overdue: isOverdue(todo.dueDate),
              [`priority-${todo.priority}`]: true,
              'batch-selected': selectedTodos.has(todo.id),
              'is-dragging': draggingTodo === todo.id
            }"
            draggable="true"
            @dragstart="handleDragStart($event, todo)"
            @dragend="handleDragEnd"
            @dragover="handleDragOver($event, todo)"
            @drop="handleDrop($event, todo)"
            @click="handleItemClick(todo)"
          >
            <!-- 优先级指示条 -->
            <div class="priority-indicator" :class="`priority-${todo.priority}`">
              <div class="indicator-glow"></div>
            </div>

            <!-- 批量选择复选框 -->
            <div v-if="batchMode" class="batch-checkbox" @click.stop="toggleSelectTodo(todo)">
              <div class="checkbox-inner" :class="{ checked: selectedTodos.has(todo.id) }">
                <span v-if="selectedTodos.has(todo.id)" class="material-icons-outlined check-icon">check</span>
              </div>
            </div>

            <!-- 完成复选框 -->
            <div v-else class="todo-checkbox" @click.stop="toggleComplete(todo)">
              <div class="checkbox-inner" :class="{ checked: todo.completed }">
                <span v-if="todo.completed" class="material-icons-outlined check-icon">check</span>
                <div class="checkbox-ripple"></div>
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
              <span v-if="!batchMode" class="drag-handle" title="拖拽排序">
                <span class="material-icons-outlined">drag_indicator</span>
              </span>
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
        </transition-group>
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
import { ref, computed, onMounted, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { confirmDelete, deleteSuccess, messageError, messageSuccess } from '@/utils/ui'
import CreateTodoDialog from '@/components/CreateTodoDialog/index.vue'
import TodoDetailDialog from '@/components/TodoDetailDialog/index.vue'
import { getTodos, completeTodo, deleteTodo as deleteTodoApi, updateTodo } from '@/api/im/workbench'

// ============================================================================
// 工具函数：防抖
// ============================================================================
const debounce = (fn, delay) => {
  let timer = null
  return (...args) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}

const loading = ref(false)
const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const todos = ref([])
const editingTodo = ref(null)
const selectedTodo = ref(null)
const activeFilter = ref('all')
const searchKeyword = ref('') // 搜索关键词
const searchInput = ref('') // 搜索输入框的值（用于防抖）

// 批量操作状态
const batchMode = ref(false)
const selectedTodos = ref(new Set())

// 拖拽排序状态
const draggingTodo = ref(null)
const dragOverTodo = ref(null)

// ============================================================================
// 搜索防抖处理
// ============================================================================
const debouncedSearch = debounce((value) => {
  searchKeyword.value = value
}, 300)

const handleSearchInput = (e) => {
  debouncedSearch(e.target.value)
}

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

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(t =>
      t.title.toLowerCase().includes(keyword) ||
      (t.content && t.content.toLowerCase().includes(keyword))
    )
  }

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
  if (days === 1) return 'event_available'
  if (days === -1) return 'event'
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
    priority: priorityMap[todo.priority] || 2,
    dueDate: todo.dueDate || null
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
  if (!await confirmDelete('这个待办')) {
    return
  }
  try {
    const res = await deleteTodoApi(todo.id)
    if (res.code === 200) {
      todos.value = todos.value.filter(t => t.id !== todo.id)
      deleteSuccess()
      showDetailDialog.value = false
    } else {
      messageError(res.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除待办失败', error)
    messageError('操作失败，请稍后重试')
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

// ============================================================================
// 批量操作功能
// ============================================================================
const enterBatchMode = () => {
  batchMode.value = true
  selectedTodos.value.clear()
  searchInput.value = ''
  searchKeyword.value = ''
}

const exitBatchMode = () => {
  batchMode.value = false
  selectedTodos.value.clear()
}

const toggleSelectTodo = (todo) => {
  if (selectedTodos.value.has(todo.id)) {
    selectedTodos.value.delete(todo.id)
  } else {
    selectedTodos.value.add(todo.id)
  }
  // 触发响应式更新
  selectedTodos.value = new Set(selectedTodos.value)
}

const selectAllTodos = () => {
  if (selectedTodos.value.size === filteredTodos.value.length) {
    selectedTodos.value.clear()
  } else {
    filteredTodos.value.forEach(todo => selectedTodos.value.add(todo.id))
  }
  selectedTodos.value = new Set(selectedTodos.value)
}

const batchComplete = async () => {
  if (selectedTodos.value.size === 0) return

  try {
    await ElMessageBox.confirm(`确定要完成选中的 ${selectedTodos.value.size} 个待办吗？`, '批量完成', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    const promises = Array.from(selectedTodos.value).map(id => completeTodo(id))
    await Promise.all(promises)

    // 更新本地状态
    todos.value = todos.value.map(todo => {
      if (selectedTodos.value.has(todo.id)) {
        return { ...todo, completed: true }
      }
      return todo
    })

    ElMessage.success(`已完成 ${selectedTodos.value.size} 个待办`)
    exitBatchMode()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量完成失败', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  }
}

const batchDelete = async () => {
  if (selectedTodos.value.size === 0) return

  if (!await confirmDelete(`选中的 ${selectedTodos.value.size} 个待办`, `确定要删除选中的 ${selectedTodos.value.size} 个待办吗？此操作不可恢复。`)) {
    return
  }

  try {
    const promises = Array.from(selectedTodos.value).map(id => deleteTodoApi(id))
    await Promise.all(promises)

    // 从列表中移除已删除的待办
    todos.value = todos.value.filter(todo => !selectedTodos.value.has(todo.id))

    messageSuccess(`已删除 ${selectedTodos.value.size} 个待办`)
    exitBatchMode()
  } catch (error) {
    console.error('批量删除失败', error)
    messageError('操作失败，请稍后重试')
  }
}

// ============================================================================
// 拖拽排序功能
// ============================================================================
const handleDragStart = (e, todo) => {
  if (batchMode.value) {
    e.preventDefault()
    return
  }
  draggingTodo.value = todo.id
  e.dataTransfer.effectAllowed = 'move'
  e.target.classList.add('is-dragging')
}

const handleDragEnd = (e) => {
  draggingTodo.value = null
  dragOverTodo.value = null
  e.target.classList.remove('is-dragging')
}

const handleDragOver = (e, todo) => {
  e.preventDefault()
  if (!draggingTodo.value || draggingTodo.value === todo.id || batchMode.value) return
  dragOverTodo.value = todo.id
}

const handleDrop = async (e, targetTodo) => {
  e.preventDefault()
  if (!draggingTodo.value || draggingTodo.value === targetTodo.id || batchMode.value) return

  // 交换数组位置
  const draggingIndex = todos.value.findIndex(t => t.id === draggingTodo.value)
  const targetIndex = todos.value.findIndex(t => t.id === targetTodo.id)

  if (draggingIndex !== -1 && targetIndex !== -1) {
    const temp = todos.value[draggingIndex]
    todos.value.splice(draggingIndex, 1)
    todos.value.splice(targetIndex, 0, temp)

    // 保存顺序到后端（如果有相关API）
    ElMessage.success('排序已更新')
  }

  draggingTodo.value = null
  dragOverTodo.value = null
}

// ============================================================================
// 点击处理（批量模式和普通模式）
// ============================================================================
const handleItemClick = (todo) => {
  if (batchMode.value) {
    toggleSelectTodo(todo)
  } else {
    handleViewDetail(todo)
  }
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
  position: relative;
}

// ============================================================================
// 头部区域
// ============================================================================
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  gap: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-count {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  padding: 0 8px;
}

.batch-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid rgba(0, 0, 0, 0.08);

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  &--complete {
    background: var(--dt-success-bg);
    color: var(--dt-success-color);
    border-color: var(--dt-success-color);

    &:hover:not(:disabled) {
      background: var(--dt-success-color);
      color: #fff;
    }
  }

  &--danger {
    background: var(--dt-error-bg);
    color: var(--dt-error-color);
    border-color: var(--dt-error-color);

    &:hover:not(:disabled) {
      background: var(--dt-error-color);
      color: #fff;
    }
  }

  &--cancel {
    background: #f5f5f5;
    color: #666;

    &:hover:not(:disabled) {
      background: #e0e0e0;
    }
  }
}

.icon-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: var(--dt-brand-color);
    color: var(--dt-brand-color);
    background: var(--dt-brand-bg);
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

// ============================================================================
// 搜索框
// ============================================================================
.search-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 240px;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: #999;
  font-size: 18px;
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 36px;
  padding: 0 12px 0 38px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 18px;
  font-size: 14px;
  color: #1a1a1a;
  outline: none;
  transition: all 0.3s;

  &::placeholder {
    color: #999;
  }

  &:hover {
    border-color: rgba(0, 137, 255, 0.3);
  }

  &:focus {
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 3px rgba(0, 137, 255, 0.1);
  }
}

.title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.panel-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
  letter-spacing: -0.5px;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    bottom: 2px;
    left: 0;
    width: 0;
    height: 2px;
    background: linear-gradient(90deg, var(--dt-brand-color) 0%, var(--dt-brand-hover) 100%);
    transition: width 0.3s ease;
  }

  &:hover::before {
    width: 100%;
  }
}

.title-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: linear-gradient(135deg, var(--dt-brand-bg) 0%, var(--dt-brand-hover) 100%);
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0, 137, 255, 0.2);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.3) 50%, transparent 50%);
    animation: shimmer 3s infinite;
  }

  .badge-icon-wrapper {
    position: relative;
  }

  .badge-icon {
    font-size: 16px;
    color: var(--dt-brand-color);
    position: relative;
    z-index: 1;
  }

  .icon-glow {
    position: absolute;
    inset: -6px;
    border-radius: 50%;
    background: var(--dt-brand-color);
    opacity: 0.2;
    animation: iconPulseWeak 2s ease-in-out infinite;
  }

  .badge-count {
    font-size: 13px;
    font-weight: 700;
    color: var(--dt-brand-color);
    position: relative;
    z-index: 1;
  }
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-hover) 100%);
  color: #fff;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 16px rgba(0, 137, 255, 0.3);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.2) 50%, transparent 50%);
    animation: shimmer 2s infinite;
  }

  &:hover {
    background: linear-gradient(135deg, var(--dt-brand-hover) 0%, #4096ff 100%);
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 137, 255, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

// ============================================================================
// 统计卡片
// ============================================================================
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 20px 28px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent 0%, rgba(0, 0, 0, 0.06) 50%, transparent 50%);
  }
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0) 0%, rgba(0, 0, 0, 0.02) 100%);
    transition: opacity 0.3s;
    opacity: 0;
  }

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);

    &::before {
      opacity: 1;
    }

    .stat-icon-wrapper .stat-icon {
      transform: scale(1.1);
    }

    .stat-decoration {
      opacity: 1;
    }
  }

  .stat-icon-wrapper {
    position: relative;
    flex-shrink: 0;
  }

  .stat-icon {
    font-size: 22px;
    color: #fff;
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .icon-bg {
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: currentColor;
    opacity: 0.2;
  }

  .icon-glow {
    position: absolute;
    inset: -8px;
    border-radius: 50%;
    background: currentColor;
    opacity: 0.3;
    animation: statIconBreath 3s ease-in-out infinite;
  }

  &--total .stat-icon {
    color: #0089FF;
  }

  &--pending .stat-icon {
    color: #faad14;
  }

  &--high .stat-icon {
    color: #f54a45;
  }

  &--done .stat-icon {
    color: #52c41a;
  }

  .stat-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    flex: 1;
  }

  .stat-value {
    font-size: 20px;
    font-weight: 700;
    color: #1a1a1a;
    line-height: 1;
    letter-spacing: -0.5px;
  }

  .stat-label {
    font-size: 13px;
    color: #8c8c8c;
    font-weight: 500;
  }

  .stat-decoration {
    position: absolute;
    top: 0;
    right: 0;
    width: 60px;
    height: 60px;
    background: radial-gradient(circle at top right, rgba(0, 137, 255, 0.05) 0%, transparent 70%);
    pointer-events: none;
    opacity: 0.6;
  }
}

// ============================================================================
// 筛选标签
// ============================================================================
.filter-tabs {
  display: flex;
  padding: 16px 28px 0;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  gap: 8px;
  flex-shrink: 0;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent 0%, rgba(0, 0, 0, 0.06) 50%, transparent 50%);
  }
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: transparent;
  border: none;
  border-radius: 20px 20px 0 0;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  margin-bottom: -1px;

  .filter-tab-icon {
    font-size: 18px;
    transition: transform 0.3s;
  }

  .filter-tab-label {
    font-size: 14px;
    font-weight: 500;
  }

  .tab-count {
    padding: 2px 8px;
    background: rgba(0, 0, 0, 0.06);
    color: #666;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
    min-width: 20px;
    text-align: center;
    transition: all 0.3s;
  }

  .tab-indicator {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: var(--dt-brand-color);
    transform: scaleX(0);
    transition: transform 0.3s ease;
  }

  &:hover {
    color: #1a1a1a;
    background: rgba(0, 0, 0, 0.04);

    .filter-tab-icon {
      transform: translateY(-2px);
    }
  }

  &.active {
    color: var(--dt-brand-color);
    background: #fff;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);

    .tab-count {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
    }

    .tab-indicator {
      transform: scaleX(1);
    }
  }
}

// ============================================================================
// 内容区域
// ============================================================================
.panel-content {
  flex: 1;
  padding: 24px 28px;
  overflow-y: auto;
  position: relative;
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
  gap: 16px;
}

.loading-spinner {
  position: relative;
  width: 40px;
  height: 40px;
}

.spinner-ring {
  position: absolute;
  inset: 0;
  border: 3px solid rgba(0, 137, 255, 0.1);
  border-top-color: var(--dt-brand-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner-dot {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 8px;
  height: 8px;
  background: var(--dt-brand-color);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: dotPulseTranslate 1.5s ease-in-out infinite;
}

.loading-text {
  font-size: 15px;
  color: #999;
  font-weight: 500;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.empty-illustration {
  position: relative;
  margin-bottom: 32px;
}

.empty-icon-wrapper {
  position: relative;
}

.empty-icon {
  font-size: 96px;
  color: #d9d9d9;
  transition: all 0.3s;
  position: relative;
  z-index: 1;
}

.icon-rings {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.ring {
  position: absolute;
  border: 2px solid var(--dt-brand-color);
  border-radius: 50%;
  opacity: 0;
  animation: ringPulse 2s ease-out infinite;

  &:nth-child(1) {
    width: 120px;
    height: 120px;
  }

  &:nth-child(2) {
    width: 160px;
    height: 160px;
    animation-delay: 0.3s;
  }

  &:nth-child(3) {
    width: 200px;
    height: 200px;
    animation-delay: 0.6s;
  }
}

.empty-decoration {
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 8px;
  background: rgba(0, 137, 255, 0.1);
  border-radius: 50%;
  opacity: 0.5;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 12px 0;
  letter-spacing: -0.5px;
}

.empty-text {
  font-size: 15px;
  color: #999;
  margin: 0 0 32px 0;
  max-width: 320px;
  line-height: 1.6;
}

.empty-action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-hover) 100%);
  color: #fff;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 16px rgba(0, 137, 255, 0.3);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.2) 50%, transparent 50%);
    animation: shimmer 2s infinite;
  }

  &:hover {
    background: linear-gradient(135deg, var(--dt-brand-hover) 0%, #4096ff 100%);
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 137, 255, 0.4);
  }
}

// ============================================================================
// 待办列表
// ============================================================================
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 20px;
}

.todo-item {
  position: relative;
  background: #fff;
  border-radius: 16px;
  padding: 18px 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.04);

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0) 0%, rgba(0, 0, 0, 0.01) 100%);
    transition: opacity 0.3s;
    opacity: 0;
  }

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
    border-color: rgba(0, 137, 255, 0.1);

    &::before {
      opacity: 1;
    }

    .todo-actions {
      opacity: 1;
    }
  }

  &.completed {
    opacity: 0.5;

    .todo-title {
      text-decoration: line-through;
      color: #999;
    }

    .priority-indicator {
      opacity: 0.3;
    }
  }

  &.overdue {
    border-color: rgba(245, 74, 69, 0.2);

    .priority-indicator {
      background: #f54a45;
    }
  }
}

// 动画
.todo-item-enter-active,
.todo-item-leave-active {
  transition: all 0.3s ease;
}

.todo-item-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.todo-item-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.priority-indicator {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  border-radius: 0 4px 0 0;

  &.priority-high {
    background: #f54a45;
  }

  &.priority-medium {
    background: #faad14;
  }

  &.priority-low {
    background: #52c41a;
  }

  .indicator-glow {
    position: absolute;
    inset: -4px;
    background: currentColor;
    border-radius: 4px;
    opacity: 0.4;
    animation: priorityPulseStrong 2s ease-in-out infinite;
  }
}

// ============================================================================
// 批量选择复选框
// ============================================================================
.batch-checkbox {
  flex-shrink: 0;
  padding-top: 3px;
  cursor: pointer;
}

.batch-checkbox .checkbox-inner {
  width: 20px;
  height: 20px;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  background: #fafafa;

  &.checked {
    background: var(--dt-brand-color);
    border-color: var(--dt-brand-color);

    .check-icon {
      color: #fff;
      font-size: 16px;
      animation: checkBounce 0.3s ease;
    }
  }
}

.todo-item.batch-selected {
  background: var(--dt-brand-bg);
  border-color: var(--dt-brand-color);
}

// ============================================================================
// 复选框
// ============================================================================
.todo-checkbox {
  flex-shrink: 0;
  padding-top: 3px;
}

.checkbox-inner {
  width: 24px;
  height: 24px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fafafa;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(0, 137, 255, 0.1) 0%, transparent 50%);
    opacity: 0;
    transition: opacity 0.3s;
  }

  .todo-item:hover .checkbox-inner & {
    border-color: var(--dt-brand-color);
  }

  &.checked {
    background: var(--dt-brand-color);
    border-color: var(--dt-brand-color);

    .check-icon {
      color: #fff;
      font-size: 18px;
      animation: checkBounce 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
    }

    &::after {
      opacity: 1;
    }
  }

  .checkbox-ripple {
    position: absolute;
    inset: 0;
    border-radius: 6px;
    background: rgba(0, 137, 255, 0.3);
    transform: scale(0);
    opacity: 0;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &.checked .checkbox-ripple {
    animation: rippleEffect 0.4s ease-out;
  }
}

@keyframes checkBounce {
  0% { transform: scale(0); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

@keyframes rippleEffect {
  0% {
    transform: scale(0);
    opacity: 0.5;
  }
  100% {
    transform: scale(2);
    opacity: 0;
  }
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
  gap: 10px;
  margin-bottom: 8px;
}

.todo-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: -0.3px;
}

.todo-priority-badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;

  &.priority-high {
    background: #fff1f0;
    color: #f54a45;
  }

  &.priority-medium {
    background: #fff7e6;
    color: #faad14;
  }

  &.priority-low {
    background: #f6ffed;
    color: #52c41a;
  }
}

.todo-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.6;
  letter-spacing: 0.2px;
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
  font-size: 13px;
  color: #999;
  font-weight: 500;

  .date-icon {
    font-size: 16px;
  }

  &.overdue {
    color: #f54a45;
    font-weight: 600;
  }
}

// ============================================================================
// 拖拽手柄
// ============================================================================
.drag-handle {
  width: 24px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  color: #ccc;
  transition: all 0.2s;

  &:hover {
    color: var(--dt-brand-color);
  }

  &:active {
    cursor: grabbing;
  }

  .material-icons-outlined {
    font-size: 20px;
  }
}

.todo-item.is-dragging {
  opacity: 0.5;
  transform: scale(0.98);
}

// ============================================================================
// 操作按钮
// ============================================================================
.todo-actions {
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.3s;
  flex-shrink: 0;
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #f5f5f5;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #666;

  &:hover {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
    transform: scale(1.1);
  }

  &--danger:hover {
    background: #fff1f0;
    color: #f54a45;
  }

  .material-icons-outlined {
    font-size: 18px;
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
.dark .todo-panel {
  background: #0a0e1a;
}

.dark .header-actions {
  .selected-count {
    color: #999;
  }

  .batch-btn {
    background: #1a1f2e;
    border-color: rgba(255, 255, 255, 0.1);
    color: #999;

    &--complete {
      background: rgba(82, 196, 26, 0.15);
      border-color: #52c41a;
      color: #86efac;
    }

    &--danger {
      background: rgba(245, 74, 69, 0.15);
      border-color: #f54a45;
      color: #fca5a5;
    }

    &--cancel {
      background: #2d2d2d;
      color: #999;
    }
  }

  .icon-btn {
    background: #1a1f2e;
    border-color: rgba(255, 255, 255, 0.1);
    color: #999;
  }
}

.dark .panel-header,
.dark .filter-tabs,
.dark .stats-row {
  background: #1a1f2e;
  border-color: rgba(255, 255, 255, 0.1);
}

.dark .panel-title {
  color: #e8e8e8;
}

.dark .stat-card {
  background: #1a1f2e;
  border-color: rgba(255, 255, 255, 0.1);

  &::before {
    background: linear-gradient(135deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.03) 100%);
  }
}

.dark .stat-value {
  color: #e8e8e8;
}

.dark .stat-label {
  color: #888;
}

.dark .filter-tab {
  color: #999;
}

.dark .filter-tab:hover {
  color: #e8e8e8;
  background: rgba(255, 255, 255, 0.06);
}

.dark .filter-tab.active {
  color: var(--dt-brand-color);
  background: #0d1d2d;
}

.dark .filter-tab.active .tab-count {
  background: rgba(0, 137, 255, 0.15);
}

.dark .todo-item {
  background: #1a1f2e;
  border-color: rgba(255, 255, 255, 0.08);
}

.dark .todo-title {
  color: #e8e8e8;
}

.dark .todo-desc {
  color: #999;
}

.dark .todo-date {
  color: #888;
}

.dark .todo-checkbox {
  background: #0d1d2d;
  border-color: #3d3d3d;
}

.dark .action-btn {
  background: #2d2d2d;
  color: #999;
}

.dark .empty-state {
  background: #0a0e1a;
}

.dark .empty-icon {
  color: #3d3d3d;
}

.dark .empty-decoration {
  background: rgba(0, 137, 255, 0.05);
}

.dark .empty-title {
  color: #e8e8e8;
}

.dark .empty-text {
  color: #888;
}

.dark .todo-item.batch-selected {
  background: rgba(0, 137, 255, 0.15);
  border-color: rgba(0, 137, 255, 0.3);
}

.dark .batch-checkbox .checkbox-inner {
  background: #1a1f2e;
  border-color: #3d3d3d;

  &.checked {
    background: var(--dt-brand-color);
    border-color: var(--dt-brand-color);
  }
}

.dark .drag-handle {
  color: #3d3d3d;

  &:hover {
    color: var(--dt-brand-color);
  }
}

.dark .search-wrapper {
  .search-icon {
    color: #888;
  }

  .search-input {
    background: #1a1f2e;
    border-color: rgba(255, 255, 255, 0.1);
    color: #e8e8e8;

    &::placeholder {
      color: #888;
    }

    &:hover {
      border-color: rgba(0, 137, 255, 0.3);
    }

    &:focus {
      border-color: var(--dt-brand-color);
      box-shadow: 0 0 0 3px rgba(0, 137, 255, 0.15);
    }
  }
}

// ============================================================================
// 局部动画 - 特定效果
// ============================================================================
// statIconBreath: 图标呼吸效果（缩放+透明度）- 与全局 statIconPulse 不同
@keyframes statIconBreath {
  0%, 100% {
    transform: scale(1);
    opacity: 0.3;
  }
  50% {
    transform: scale(1.15);
    opacity: 0.5;
  }
}
</style>