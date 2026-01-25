<template>
  <div class="workbench-panel">
    <!-- 顶部欢迎区 -->
    <header class="workbench-header">
      <div class="greeting-section">
        <h1 class="greeting-title">{{ greetingText }}，{{ displayName }}</h1>
        <p class="greeting-date">{{ currentDateText }}</p>
      </div>
      <div class="header-actions">
        <div class="search-box">
          <span class="material-icons-outlined search-icon">search</span>
          <input
            v-model="searchQuery"
            class="search-input"
            placeholder="搜索应用、文档、联系人..."
            type="text"
          />
        </div>
        <button class="custom-btn">
          <span class="material-icons-outlined">add</span>
          自定义
        </button>
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="workbench-content">
      <!-- 常用应用 -->
      <section class="apps-section common-section">
        <div class="section-header">
          <h3 class="section-title">
            <span class="title-icon title-icon-primary">
              <span class="material-icons-outlined">apps</span>
            </span>
            常用应用
          </h3>
          <button class="manage-btn">
            <span class="material-icons-outlined">settings</span>
            管理
          </button>
        </div>
        <div class="apps-grid">
          <div
            v-for="app in commonApps"
            :key="app.key"
            class="app-card common-app"
            @click="handleAppClick(app)"
          >
            <div class="app-icon" :class="app.iconClass">
              <span class="material-icons-outlined">{{ app.icon }}</span>
            </div>
            <span class="app-label">{{ app.label }}</span>
            <span v-if="app.badge" class="app-badge">{{ app.badge > 99 ? '99+' : app.badge }}</span>
          </div>
        </div>
      </section>

      <!-- 内容网格：待办事项 + 公司公告 -->
      <div class="content-grid">
        <!-- 待办事项 -->
        <div class="content-card todo-card">
          <div class="card-header">
            <h4 class="card-title">
              <span class="material-icons-outlined card-icon">task_alt</span>
              待办事项
            </h4>
            <a class="view-all-link" href="#" @click.prevent="handleViewAllTodos">查看全部</a>
          </div>
          <div v-loading="loadingTodos" class="card-body">
            <div v-if="todos.length === 0" class="empty-state">
              <span class="material-icons-outlined empty-icon">check_circle_outline</span>
              <p class="empty-text">暂无待办事项</p>
              <button class="empty-action" @click="handleAddTodo">
                <span class="material-icons-outlined">add</span>
                新建待办
              </button>
            </div>
            <div v-else class="todo-list">
              <div
                v-for="todo in todos"
                :key="todo.id"
                class="todo-item"
                :class="{ completed: todo.completed, overdue: isOverdue(todo.deadline) }"
                @click="handleTodoClick(todo)"
              >
                <div class="todo-priority" :class="todo.priorityClass"></div>
                <div class="todo-content">
                  <p class="todo-title">{{ todo.title }}</p>
                  <p class="todo-deadline">
                    <span class="material-icons-outlined deadline-icon">schedule</span>
                    {{ formatDate(todo.deadline) }}
                  </p>
                </div>
                <button class="todo-complete-btn" @click.stop="handleTodoComplete(todo)">
                  <span class="material-icons-outlined">check_circle_outline</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 公司公告 -->
        <div class="content-card announcement-card">
          <div class="card-header">
            <h4 class="card-title">
              <span class="material-icons-outlined card-icon">campaign</span>
              公司公告
            </h4>
            <a class="view-all-link" href="#" @click.prevent="handleViewAllAnnouncements">更多</a>
          </div>
          <div v-loading="loadingAnnouncements" class="card-body">
            <div v-if="announcements.length === 0" class="empty-state">
              <span class="material-icons-outlined empty-icon">campaign</span>
              <p class="empty-text">暂无公告</p>
            </div>
            <div v-else class="announcement-list">
              <div
                v-for="announcement in announcements"
                :key="announcement.id"
                class="announcement-item"
                @click="handleAnnouncementClick(announcement)"
              >
                <span class="announcement-tag" :class="announcement.tagClass">
                  {{ announcement.tag }}
                </span>
                <div class="announcement-content">
                  <p class="announcement-title">{{ announcement.title }}</p>
                  <span class="announcement-meta">
                    <span class="material-icons-outlined meta-icon">business</span>
                    {{ announcement.department }}
                    <span class="divider">·</span>
                    <span class="material-icons-outlined meta-icon">access_time</span>
                    {{ announcement.time }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 其它应用 -->
      <section class="apps-section other-section">
        <div class="section-header">
          <h3 class="section-title">
            <span class="title-icon title-icon-secondary">
              <span class="material-icons-outlined">extension</span>
            </span>
            其它应用
          </h3>
        </div>
        <div class="apps-grid apps-grid-sm">
          <div
            v-for="app in otherApps"
            :key="app.key"
            class="app-card other-app"
            @click="handleAppClick(app)"
          >
            <div class="app-icon" :class="app.iconClass">
              <span class="material-icons-outlined">{{ app.icon }}</span>
            </div>
            <span class="app-label">{{ app.label }}</span>
          </div>
          <div class="app-card add-app-card" @click="handleAddApp">
            <div class="app-icon add-icon">
              <span class="material-icons-outlined">add</span>
            </div>
            <span class="app-label">添加应用</span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { getTodos, getAnnouncements } from '@/api/im/workbench'
import { ElMessage } from 'element-plus'

const store = useStore()
const searchQuery = ref('')
const loadingTodos = ref(false)
const loadingAnnouncements = ref(false)

const currentUser = computed(() => store.getters['user/currentUser'] || {})
const displayName = computed(() => currentUser.value.nickname || currentUser.value.username || '开发者')

// 问候语
const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

// 当前日期
const currentDateText = computed(() => {
  const now = new Date()
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const date = now.getDate()
  const weekday = weekdays[now.getDay()]
  return `${year}年${month}月${date}日 ${weekday}`
})

// 常用应用
const commonApps = ref([
  { key: 'attendance', label: '考勤打卡', icon: 'access_time', iconClass: 'icon-orange' },
  { key: 'approval', label: '审批', icon: 'approval', iconClass: 'icon-blue' },
  { key: 'report', label: '汇报', icon: 'assignment', iconClass: 'icon-green' },
  { key: 'todo', label: '待办', icon: 'task_alt', iconClass: 'icon-purple', badge: 3 },
  { key: 'announcement', label: '公告', icon: 'campaign', iconClass: 'icon-pink' }
])

// 其它应用
const otherApps = ref([
  { key: 'expense', label: '报销', icon: 'receipt_long', iconClass: 'icon-teal' },
  { key: 'cloud', label: '云盘', icon: 'folder_shared', iconClass: 'icon-indigo' },
  { key: 'culture', label: '企业文化', icon: 'emoji_events', iconClass: 'icon-amber' },
  { key: 'health', label: '健康申报', icon: 'health_and_safety', iconClass: 'icon-rose' },
  { key: 'it', label: 'IT支持', icon: 'contact_support', iconClass: 'icon-cyan' },
  { key: 'meeting', label: '会议', icon: 'meeting_room', iconClass: 'icon-lime' }
])

// 待办事项
const todos = ref([
  {
    id: 1,
    title: '完成Q4季度产品规划文档',
    deadline: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
    priorityClass: 'priority-high'
  },
  {
    id: 2,
    title: '新员工入职培训会议',
    deadline: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
    priorityClass: 'priority-medium'
  },
  {
    id: 3,
    title: '审核前端团队代码合并请求',
    deadline: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000).toISOString(),
    priorityClass: 'priority-low'
  }
])

// 公告
const announcements = ref([
  {
    id: 1,
    title: '关于国庆节放假安排的通知',
    department: '人事行政部',
    time: '2小时前',
    tag: '重要',
    tagClass: 'tag-important'
  },
  {
    id: 2,
    title: '2023年度员工体检预约开启',
    department: '人事行政部',
    time: '昨天',
    tag: '通知',
    tagClass: 'tag-notice'
  }
])

const handleAppClick = (app) => {
  ElMessage.info(`打开应用: ${app.label}`)
}

const handleAddApp = () => {
  ElMessage.info('添加应用')
}

const handleTodoClick = (todo) => {
  ElMessage.info(`查看待办: ${todo.title}`)
}

const handleAddTodo = () => {
  // 切换到待办面板
  window.dispatchEvent(new CustomEvent('switch-tab', { detail: 'todo' }))
}

const handleTodoComplete = (todo) => {
  ElMessage.success(`已完成: ${todo.title}`)
  todos.value = todos.value.filter(t => t.id !== todo.id)
}

const handleAnnouncementClick = (announcement) => {
  ElMessage.info(`查看公告: ${announcement.title}`)
}

const handleViewAllTodos = () => {
  window.dispatchEvent(new CustomEvent('switch-tab', { detail: 'todo' }))
}

const handleViewAllAnnouncements = () => {
  ElMessage.info('查看全部公告')
}

const isOverdue = (dueDate) => {
  if (!dueDate) return false
  return new Date(dueDate) < new Date()
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

const loadTodos = async () => {
  loadingTodos.value = true
  try {
    const res = await getTodos()
    if (res.code === 200) {
      todos.value = res.data || []
    }
  } catch (e) {
    console.error('加载待办失败', e)
  } finally {
    loadingTodos.value = false
  }
}

const loadAnnouncements = async () => {
  loadingAnnouncements.value = true
  try {
    const res = await getAnnouncements()
    if (res.code === 200) {
      announcements.value = res.data || []
    }
  } catch (e) {
    console.error('加载公告失败', e)
  } finally {
    loadingAnnouncements.value = false
  }
}

onMounted(() => {
  loadTodos()
  loadAnnouncements()
})
</script>

<style scoped lang="scss">
// ============================================================================
// 工作台面板容器
// ============================================================================
.workbench-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
  background: var(--dt-bg-body);
  overflow: hidden;
}

// ============================================================================
// 顶部欢迎区
// ============================================================================
.workbench-header {
  background: linear-gradient(135deg, #e6f4ff 0%, #f0f9ff 50%, #fff 100%);
  border-bottom: 1px solid var(--dt-border-light);
  padding: 32px 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    right: -50px;
    top: -100px;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(22, 119, 255, 0.08) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }
}

.greeting-section {
  position: relative;
  z-index: 1;
}

.greeting-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--dt-text-primary);
  margin: 0;
  letter-spacing: -0.02em;
}

.greeting-date {
  font-size: 14px;
  color: var(--dt-text-secondary);
  margin: 6px 0 0 0;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: var(--dt-text-tertiary);
  font-size: 20px;
  pointer-events: none;
}

.search-input {
  width: 280px;
  height: 40px;
  padding: 0 12px 0 40px;
  background: #fff;
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-lg);
  font-size: 14px;
  color: var(--dt-text-primary);
  outline: none;
  transition: all var(--dt-transition-base);
  box-shadow: var(--dt-shadow-1);
}

.search-input:hover {
  border-color: var(--dt-border-input-hover);
}

.search-input:focus {
  border-color: var(--dt-brand-color);
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);
}

.custom-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 20px;
  height: 40px;
  background: var(--dt-brand-color);
  color: #fff;
  border: none;
  border-radius: var(--dt-radius-lg);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--dt-transition-base);
  box-shadow: var(--dt-shadow-2);
}

.custom-btn:hover {
  background: var(--dt-brand-hover);
  box-shadow: var(--dt-shadow-3);
  transform: translateY(-1px);
}

.custom-btn:active {
  transform: translateY(0);
}

// ============================================================================
// 主内容区
// ============================================================================
.workbench-content {
  flex: 1;
  padding: 24px 40px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

// ============================================================================
// 应用区块
// ============================================================================
.apps-section {
  position: relative;
  z-index: 1;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.title-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-icon-primary {
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
}

.title-icon-secondary {
  background: rgba(22, 119, 255, 0.06);
  color: var(--dt-brand-color);
}

.title-icon .material-icons-outlined {
  font-size: 18px;
}

.manage-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-md);
  font-size: 13px;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-base);
}

.manage-btn:hover {
  border-color: var(--dt-brand-color);
  color: var(--dt-brand-color);
  background: var(--dt-brand-bg);
}

.apps-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.apps-grid-sm {
  grid-template-columns: repeat(6, 1fr);
}

@media (max-width: 1400px) {
  .apps-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .apps-grid-sm {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 1024px) {
  .workbench-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .apps-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .apps-grid-sm {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .workbench-header {
    padding: 20px;
  }

  .greeting-title {
    font-size: 22px;
  }

  .search-input {
    width: 200px;
  }

  .workbench-content {
    padding: 16px 20px;
  }

  .apps-grid,
  .apps-grid-sm {
    grid-template-columns: repeat(2, 1fr);
  }

  .custom-btn span:not(.material-icons-outlined) {
    display: none;
  }
}

// ============================================================================
// 应用卡片
// ============================================================================
.app-card {
  position: relative;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-xl);
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  height: 120px;
  cursor: pointer;
  transition: all var(--dt-transition-slow);
  box-shadow: var(--dt-shadow-card);
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--dt-shadow-card-hover);
  border-color: var(--dt-brand-color);
}

.app-card:active {
  transform: translateY(-2px);
}

.app-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--dt-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--dt-transition-base);
}

.app-card:hover .app-icon {
  transform: scale(1.1) rotate(5deg);
}

.app-icon .material-icons-outlined {
  font-size: 26px;
}

.app-icon.icon-orange { background: #fff7e6; color: #fa8c16; }
.app-icon.icon-blue { background: #e6f4ff; color: #1677ff; }
.app-icon.icon-green { background: #f6ffed; color: #52c41a; }
.app-icon.icon-purple { background: #f9f0ff; color: #722ed1; }
.app-icon.icon-pink { background: #fff0f6; color: #eb2f96; }
.app-icon.icon-teal { background: #e6fffa; color: #13c2c2; }
.app-icon.icon-indigo { background: #eef0ff; color: #594efc; }
.app-icon.icon-amber { background: #fffbe6; color: #faad14; }
.app-icon.icon-rose { background: #fff1f0; color: #f74a5c; }
.app-icon.icon-cyan { background: #e6fffe; color: #08bdb2; }
.app-icon.icon-lime { background: #fcffe6; color: #a0d911; }

.app-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--dt-text-primary);
  text-align: center;
}

.app-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: var(--dt-error-color);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(255, 77, 79, 0.3);
}

.add-app-card {
  background: transparent;
  border: 1px dashed var(--dt-border-color);
  box-shadow: none;
}

.add-app-card:hover {
  background: var(--dt-bg-card-hover);
  border-color: var(--dt-brand-color);
  box-shadow: var(--dt-shadow-2);
}

.add-app-card .app-icon {
  background: var(--dt-bg-input);
  color: var(--dt-text-tertiary);
}

.add-app-card .app-label {
  color: var(--dt-text-tertiary);
}

// ============================================================================
// 内容网格
// ============================================================================
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

// ============================================================================
// 内容卡片
// ============================================================================
.content-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-xl);
  box-shadow: var(--dt-shadow-card);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: all var(--dt-transition-base);
}

.content-card:hover {
  box-shadow: var(--dt-shadow-card-hover);
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.card-icon {
  font-size: 20px;
  color: var(--dt-brand-color);
}

.view-all-link {
  font-size: 13px;
  color: var(--dt-text-link);
  text-decoration: none;
  transition: color var(--dt-transition-fast);
}

.view-all-link:hover {
  color: var(--dt-text-link-hover);
}

.card-body {
  flex: 1;
  padding: 16px 24px;
  min-height: 200px;
}

// ============================================================================
// 待办列表
// ============================================================================
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);
}

.todo-item:hover {
  background: var(--dt-bg-card-hover);
  border-color: var(--dt-brand-color);
  transform: translateX(2px);
}

.todo-item.completed {
  opacity: 0.6;
}

.todo-item.completed .todo-title {
  text-decoration: line-through;
  color: var(--dt-text-tertiary);
}

.todo-item.overdue {
  border-left: 3px solid var(--dt-error-color);
}

.todo-priority {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.priority-high { background: var(--dt-error-color); }
.priority-medium { background: var(--dt-warning-color); }
.priority-low { background: var(--dt-brand-color); }

.todo-content {
  flex: 1;
  min-width: 0;
}

.todo-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-deadline {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin: 0;
}

.deadline-icon {
  font-size: 14px;
}

.todo-item.overdue .todo-deadline {
  color: var(--dt-error-color);
}

.todo-complete-btn {
  opacity: 0;
  width: 28px;
  height: 28px;
  padding: 0;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-tertiary);
  cursor: pointer;
  transition: all var(--dt-transition-base);
  display: flex;
  align-items: center;
  justify-content: center;
}

.todo-item:hover .todo-complete-btn {
  opacity: 1;
}

.todo-complete-btn:hover {
  background: var(--dt-success-bg);
  color: var(--dt-success-color);
}

.todo-complete-btn .material-icons-outlined {
  font-size: 20px;
}

// ============================================================================
// 公告列表
// ============================================================================
.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);
}

.announcement-item:hover {
  background: var(--dt-bg-card-hover);
  border-color: var(--dt-info-border);
  transform: translateX(2px);
}

.announcement-tag {
  padding: 3px 8px;
  font-size: 11px;
  font-weight: 600;
  border-radius: var(--dt-radius-sm);
  flex-shrink: 0;
  height: fit-content;
}

.tag-important {
  background: var(--dt-error-bg);
  color: var(--dt-error-color);
  border: 1px solid var(--dt-error-border);
}

.tag-notice {
  background: var(--dt-info-bg);
  color: var(--dt-info-color);
  border: 1px solid var(--dt-info-border);
}

.announcement-content {
  flex: 1;
  min-width: 0;
}

.announcement-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin: 0 0 8px 0;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.announcement-title:hover {
  color: var(--dt-brand-color);
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
  flex-wrap: wrap;
}

.meta-icon {
  font-size: 14px;
}

.divider {
  color: var(--dt-border-color);
}

// ============================================================================
// 空状态
// ============================================================================
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.empty-icon {
  font-size: 56px;
  color: var(--dt-border-color);
  margin-bottom: 12px;
}

.empty-text {
  font-size: 14px;
  color: var(--dt-text-tertiary);
  margin: 0 0 16px 0;
}

.empty-action {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--dt-brand-color);
  color: #fff;
  border: none;
  border-radius: var(--dt-radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--dt-transition-base);
}

.empty-action:hover {
  background: var(--dt-brand-hover);
}

// ============================================================================
// 暗色模式
// ============================================================================
.dark .workbench-header {
  background: linear-gradient(135deg, #0c4a6e 0%, #075985 50%, #0f172a 100%);
  border-color: var(--dt-border-dark);

  &::before {
    background: radial-gradient(circle, rgba(56, 189, 248, 0.1) 0%, transparent 70%);
  }
}

.dark .greeting-title {
  color: var(--dt-text-primary-dark);
}

.dark .greeting-date {
  color: var(--dt-text-secondary-dark);
}

.dark .search-input {
  background: var(--dt-bg-input-dark);
  color: var(--dt-text-primary-dark);
  border-color: var(--dt-border-dark);
}

.dark .app-card,
.dark .content-card {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .app-card:hover,
.dark .content-card:hover {
  background: var(--dt-bg-hover-dark);
  border-color: var(--dt-brand-color);
}

.dark .app-label,
.dark .card-title,
.dark .todo-title,
.dark .announcement-title {
  color: var(--dt-text-primary-dark);
}

.dark .todo-deadline,
.dark .announcement-meta {
  color: var(--dt-text-tertiary-dark);
}

.dark .todo-item,
.dark .announcement-item {
  background: var(--dt-bg-input-dark);
  border-color: var(--dt-border-dark);
}

.dark .todo-item:hover,
.dark .announcement-item:hover {
  background: var(--dt-bg-hover-dark);
}

.dark .add-app-card {
  border-color: var(--dt-border-dark);
}

.dark .add-app-card .app-icon {
  background: var(--dt-bg-card-dark);
}

.dark .manage-btn {
  border-color: var(--dt-border-dark);
  color: var(--dt-text-secondary-dark);
}

.dark .manage-btn:hover {
  background: rgba(22, 119, 255, 0.1);
}

.dark .app-icon {
  background: var(--dt-bg-input-dark);
}

.dark .app-icon.icon-orange { background: rgba(250, 140, 22, 0.15); color: #fdba74; }
.dark .app-icon.icon-blue { background: rgba(22, 119, 255, 0.15); color: #7dd3fc; }
.dark .app-icon.icon-green { background: rgba(82, 196, 26, 0.15); color: #86efac; }
.dark .app-icon.icon-purple { background: rgba(114, 46, 209, 0.15); color: #c084fc; }
.dark .app-icon.icon-pink { background: rgba(235, 47, 150, 0.15); color: #f472b6; }
.dark .app-icon.icon-teal { background: rgba(19, 180, 167, 0.15); color: #2dd4bf; }
.dark .app-icon.icon-indigo { background: rgba(89, 78, 236, 0.15); color: #818cf8; }
.dark .app-icon.icon-amber { background: rgba(250, 162, 29, 0.15); color: #fbbf24; }
.dark .app-icon.icon-rose { background: rgba(244, 114, 182, 0.15); color: #fb7185; }
.dark .app-icon.icon-cyan { background: rgba(8, 189, 180, 0.15); color: #22d3ee; }
.dark .app-icon.icon-lime { background: rgba(160, 217, 17, 0.15); color: #a3e635; }
</style>
