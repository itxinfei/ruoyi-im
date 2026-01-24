<template>
  <div class="workbench-panel">
    <!-- 顶部欢迎区 -->
    <header class="workbench-header">
      <div class="greeting">
        <h1 class="greeting-title">{{ greetingText }}，{{ currentUser.nickname || currentUser.username || '开发者' }}</h1>
        <p class="greeting-date">{{ currentDateText }}</p>
      </div>
      <div class="header-actions">
        <div class="search-box">
          <span class="material-icons-outlined search-icon">search</span>
          <input
            v-model="searchQuery"
            class="search-input"
            placeholder="搜索应用..."
            type="text"
          />
        </div>
        <button class="custom-btn">
          <span class="material-icons-outlined">add</span>
          自定义工作台
        </button>
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="workbench-content">
      <!-- 常用应用 -->
      <section class="apps-section">
        <div class="section-header">
          <h3 class="section-title">
            <span class="title-indicator primary"></span>
            常用应用
          </h3>
          <button class="manage-btn">
            管理
            <span class="material-icons-outlined">settings</span>
          </button>
        </div>
        <div class="common-apps-grid">
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
            <span v-if="app.badge" class="app-badge">{{ app.badge }}</span>
          </div>
        </div>
      </section>

      <!-- 待办事项和公司公告 -->
      <div class="content-grid">
        <!-- 待办事项 -->
        <div class="content-card todo-card">
          <div class="card-header">
            <h4 class="card-title">待办事项</h4>
            <a class="view-all-link" href="#" @click.prevent>查看全部</a>
          </div>
          <div v-loading="loadingTodos" class="card-body">
            <div v-if="todos.length === 0" class="empty-state">
              <span class="material-icons-outlined empty-icon">task_alt</span>
              <p>暂无待办事项</p>
            </div>
            <div v-else class="todo-list">
              <div
                v-for="todo in todos"
                :key="todo.id"
                class="todo-item"
                @click="handleTodoClick(todo)"
              >
                <div class="todo-priority" :class="todo.priorityClass"></div>
                <div class="todo-content">
                  <p class="todo-title">{{ todo.title }}</p>
                  <p class="todo-deadline">截止时间: {{ todo.deadline }}</p>
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
            <h4 class="card-title">公司公告</h4>
            <a class="view-all-link" href="#" @click.prevent>更多</a>
          </div>
          <div v-loading="loadingAnnouncements" class="card-body">
            <div v-if="announcements.length === 0" class="empty-state">
              <span class="material-icons-outlined empty-icon">campaign</span>
              <p>暂无公告</p>
            </div>
            <div v-else class="announcement-list">
              <div
                v-for="announcement in announcements"
                :key="announcement.id"
                class="announcement-item"
              >
                <span class="announcement-tag" :class="announcement.tagClass">
                  {{ announcement.tag }}
                </span>
                <div class="announcement-content">
                  <p class="announcement-title">{{ announcement.title }}</p>
                  <span class="announcement-meta">{{ announcement.department }} · {{ announcement.time }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 其它应用 -->
      <section class="apps-section">
        <div class="section-header">
          <h3 class="section-title">
            <span class="title-indicator secondary"></span>
            其它应用
          </h3>
        </div>
        <div class="other-apps-grid">
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
            <span class="app-label">添加</span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { getTodos, getAnnouncements } from '@/api/im/workbench'
import { ElMessage } from 'element-plus'

const store = useStore()

const searchQuery = ref('')
const loadingTodos = ref(false)
const loadingAnnouncements = ref(false)

const currentUser = computed(() => store.getters['user/currentUser'] || {})

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
  return `今天是 ${year}年${month}月${date}日，${weekday}`
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
  { key: 'it', label: 'IT支持', icon: 'contact_support', iconClass: 'icon-cyan' }
])

// 待办事项
const todos = ref([
  {
    id: 1,
    title: '完成Q4季度产品规划文档',
    deadline: '今天 18:00',
    priorityClass: 'priority-high'
  },
  {
    id: 2,
    title: '新员工入职培训会议',
    deadline: '明天 10:00',
    priorityClass: 'priority-medium'
  },
  {
    id: 3,
    title: '审核前端团队代码合并请求',
    deadline: '本周五',
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

const handleTodoComplete = (todo) => {
  ElMessage.success(`已完成: ${todo.title}`)
  todos.value = todos.value.filter(t => t.id !== todo.id)
}

const loadTodos = async () => {
  loadingTodos.value = true
  try {
    const res = await getTodos()
    if (res.code === 200) {
      todos.value = res.data || []
    }
  } catch (e) {
    console.error(e)
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
    console.error(e)
  } finally {
    loadingAnnouncements.value = false
  }
}

onMounted(() => {
  loadTodos()
  loadAnnouncements()
})
</script>

<style scoped>
.workbench-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
  background: #f4f7f9;
  overflow: hidden;
}

/* 顶部欢迎区 */
.workbench-header {
  background: linear-gradient(120deg, #e3f2fd 0%, #f0f7ff 100%);
  border-bottom: 1px solid rgba(0,0,0,0.04);
  padding: 32px 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;

  /* 装饰背景 */
  &::before {
    content: '';
    position: absolute;
    right: -20px;
    top: -40px;
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba(22, 119, 255, 0.1) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }
}

.greeting-title {
  font-size: 26px;
  font-weight: 700;
  color: #171a1d;
  margin: 0;
  letter-spacing: -0.5px;
}

.greeting-date {
  font-size: 14px;
  color: #646a73;
  margin: 6px 0 0 0;
  font-weight: 500;
}

/* ... existing code ... */

.search-input {
  width: 280px;
  background: #fff;
  border: 1px solid transparent;
  border-radius: 8px;
  padding: 10px 12px 10px 38px;
  font-size: 14px;
  color: #262626;
  outline: none;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.search-input:focus {
  background: #fff;
  border-color: #1677ff;
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
}

/* ... existing code ... */

.app-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid transparent;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  height: 140px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  border-color: transparent;
  z-index: 10;
}

.app-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s;
}

.app-card:hover .app-icon {
  transform: scale(1.05);
}

.app-icon .material-icons-outlined {
  font-size: 28px;
}

/* ... existing code ... */

.content-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(0,0,0,0.04);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
  overflow: hidden;
  transition: all 0.2s;
  
  &:hover { box-shadow: 0 8px 16px rgba(0,0,0,0.06); }
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0,0,0,0.04);
  /* ... */
}

/* ... existing code ... */

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
}

.todo-item:hover {
  background: #f7f8fa;
}

/* 暗色模式适配 */
:deep(.dark) {
  .workbench-header {
    background: linear-gradient(120deg, #1e293b 0%, #0f172a 100%);
    border-bottom-color: rgba(255,255,255,0.05);
    
    &::before {
      background: radial-gradient(circle, rgba(56, 189, 248, 0.1) 0%, transparent 70%);
    }

    .greeting-title { color: #f1f5f9; }
    .greeting-date { color: #94a3b8; }
    
    .search-input { 
      background: #1e293b; color: #f1f5f9; box-shadow: none;
      &:focus { border-color: #38bdf8; background: #1e293b; }
    }
  }

  .app-card, .content-card {
    background: #1e293b;
    border-color: rgba(255,255,255,0.05);
    box-shadow: none;
    
    &:hover {
      background: #2b3648;
      box-shadow: 0 12px 24px rgba(0,0,0,0.2);
    }
  }

  .app-label { color: #cbd5e1; }
  .card-title { color: #f1f5f9; }
  .todo-item:hover { background: #334155; }
  .todo-title { color: #e2e8f0; }
  .announcement-title { color: #e2e8f0; }
  .section-title { color: #e2e8f0; }
  
  .app-icon.icon-orange { background: rgba(250, 140, 22, 0.15); color: #fdba74; }
  .app-icon.icon-blue { background: rgba(22, 119, 255, 0.15); color: #7dd3fc; }
  .app-icon.icon-green { background: rgba(82, 196, 26, 0.15); color: #86efac; }
  /* ... add other colors if needed, or rely onopacity */
}

.todo-priority {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.priority-high { background: #ff4d4f; }
.priority-medium { background: #faad14; }
.priority-low { background: #1677ff; }

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
  margin: 0 0 2px 0;
}

.todo-item:hover .todo-title {
  color: #1677ff;
}

.todo-deadline {
  font-size: 12px;
  color: #8c8c8c;
  margin: 0;
}

.todo-complete-btn {
  opacity: 0;
  background: none;
  border: none;
  color: #bfbfbf;
  cursor: pointer;
  padding: 4px;
  transition: all 0.2s;
}

.todo-item:hover .todo-complete-btn {
  opacity: 1;
}

.todo-complete-btn:hover {
  color: #52c41a;
}

/* 公告列表 */
.announcement-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-item {
  display: flex;
  gap: 12px;
}

.announcement-tag {
  padding: 4px 8px;
  font-size: 12px;
  border-radius: 4px;
  border: 1px solid;
  flex-shrink: 0;
  height: fit-content;
}

.tag-important {
  background: #fff1f0;
  color: #ff4d4f;
  border-color: #ffccc7;
}

.tag-notice {
  background: #e6f7ff;
  color: #1677ff;
  border-color: #91caff;
}

.announcement-content {
  flex: 1;
}

.announcement-title {
  font-size: 14px;
  color: #262626;
  margin: 0 0 4px 0;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.announcement-title:hover {
  color: #1677ff;
}

.announcement-meta {
  font-size: 12px;
  color: #bfbfbf;
  display: block;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #bfbfbf;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

/* 其它应用网格 */
.other-apps-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

@media (max-width: 1200px) {
  .other-apps-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .other-apps-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.other-app {
  height: auto;
  padding: 16px;
  flex-direction: row;
  gap: 8px;
}

.other-app .app-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
}

.other-app .app-icon .material-icons-outlined {
  font-size: 20px;
}

.other-app .app-label {
  font-size: 13px;
  color: #595959;
}

.add-app-card {
  background: #fafafa;
  border: 1px dashed #d9d9d9;
}

.add-app-card:hover {
  background: #f0f0f0;
  border-color: #bfbfbf;
  box-shadow: none;
}

.add-app-card .app-icon {
  background: #e6e6e6;
  color: #bfbfbf;
}

.add-app-card:hover .app-icon {
  background: #d9d9d9;
}

.add-app-card .app-label {
  color: #bfbfbf;
}

.add-app-card:hover .app-label {
  color: #8c8c8c;
}

/* 暗色模式 */
:deep(.dark) .workbench-header,
:deep(.dark) .app-card,
:deep(.dark) .content-card {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .greeting-title,
:deep(.dark) .section-title,
:deep(.dark) .card-title,
:deep(.dark) .todo-title,
:deep(.dark) .announcement-title {
  color: #f1f5f9;
}

:deep(.dark) .greeting-date,
:deep(.dark) .todo-deadline,
:deep(.dark) .announcement-meta {
  color: #64748b;
}

:deep(.dark) .search-input {
  background: #0f172a;
  color: #cbd5e1;
}

:deep(.dark) .todo-item:hover {
  background: rgba(51, 65, 85, 0.5);
}

:deep(.dark) .add-app-card {
  background: rgba(30, 41, 59, 0.5);
  border-color: #475569;
}

:deep(.dark) .add-app-card:hover {
  background: rgba(51, 65, 85, 0.5);
}

:deep(.dark) .add-app-card .app-icon {
  background: #334155;
}

:deep(.dark) .add-app-card .app-label {
  color: #475569;
}

:deep(.dark) .add-app-card:hover .app-label {
  color: #64748b;
}
</style>
