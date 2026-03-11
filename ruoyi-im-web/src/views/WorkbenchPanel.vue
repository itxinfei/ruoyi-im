<template>
  <div class="workbench-wrapper">
    <!-- 左侧二级导航 -->
    <aside class="wb-sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">工作台</h2>
      </div>
      <nav class="sidebar-nav">
        <div
          v-for="menu in subMenus"
          :key="menu.key"
          class="nav-item"
          :class="{ active: activeMenu === menu.key }"
          @click="handleMenuChange(menu.key)"
        >
          <el-icon class="nav-icon"><component :is="menu.icon" /></el-icon>
          <span class="nav-label">{{ menu.label }}</span>
          <span v-if="menu.badge > 0" class="nav-badge">{{ menu.badge }}</span>
        </div>
      </nav>
    </aside>

    <!-- 右侧内容区 -->
    <main class="wb-content custom-scrollbar">
      <!-- 1. 常用应用视图 -->
      <div v-if="activeMenu === 'apps'" class="content-view apps-view">
        <!-- 欢迎区 -->
        <header class="wb-header">
          <div class="greeting-box">
            <h1 class="greeting-box__title">{{ greetingText }}，{{ displayName }}</h1>
            <p class="greeting-box__date">{{ currentDateText }}</p>
          </div>
          <div class="wb-search">
            <el-icon class="wb-search__icon"><Search /></el-icon>
            <input v-model="searchQuery" class="wb-search__input" placeholder="搜索应用..." />
          </div>
        </header>

        <!-- 常用应用 -->
        <section class="wb-section">
          <div class="wb-section__header">
            <h3 class="wb-section__title">常用应用</h3>
            <el-button link type="primary">管理</el-button>
          </div>
          <div class="app-grid">
            <div v-for="app in commonApps" :key="app.key" class="app-item" @click="handleAppClick(app)">
              <div class="app-item__icon" :class="app.iconClass">
                <el-icon><component :is="app.elIcon" /></el-icon>
              </div>
              <span class="app-item__label">{{ app.label }}</span>
              <span v-if="app.badge" class="app-item__badge">{{ app.badge }}</span>
            </div>
          </div>
        </section>

        <!-- 全量应用 -->
        <section class="wb-section">
          <div class="wb-section__header">
            <h3 class="wb-section__title">全量中心</h3>
          </div>
          <div class="app-grid app-grid--sm">
            <div v-for="app in otherApps" :key="app.key" class="app-item app-item--sm" @click="handleAppClick(app)">
              <div class="app-item__icon app-item__icon--sm" :class="app.iconClass">
                <el-icon><component :is="app.elIcon" /></el-icon>
              </div>
              <span class="app-item__label">{{ app.label }}</span>
            </div>
          </div>
        </section>
      </div>

      <!-- 2. 审批视图 -->
      <div v-if="activeMenu === 'approval'" class="content-view approval-view">
        <header class="view-header">
          <h2 class="view-title">审批中心</h2>
          <div class="view-actions">
            <el-button type="primary">发起审批</el-button>
          </div>
        </header>
        <div class="approval-tabs">
          <div
            v-for="tab in approvalTabs"
            :key="tab.key"
            class="tab-item"
            :class="{ active: activeApprovalTab === tab.key }"
            @click="activeApprovalTab = tab.key"
          >
            {{ tab.label }}
            <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
          </div>
        </div>
        <div class="approval-list">
          <el-empty :image-size="80" description="暂无审批记录" />
        </div>
      </div>

      <!-- 3. 考勤视图 -->
      <div v-if="activeMenu === 'attendance'" class="content-view attendance-view">
        <header class="view-header">
          <h2 class="view-title">考勤打卡</h2>
        </header>
        <div class="attendance-card">
          <div class="attendance-status">
            <div class="status-circle">
              <el-icon class="status-icon"><Timer /></el-icon>
            </div>
            <p class="status-text">今日未打卡</p>
            <p class="status-time">{{ currentTime }}</p>
          </div>
          <div class="attendance-actions">
            <el-button type="primary" size="large" @click="handlePunch('in')">上班打卡</el-button>
            <el-button type="success" size="large" @click="handlePunch('out')">下班打卡</el-button>
          </div>
        </div>
        <section class="wb-section">
          <h3 class="wb-section__title">本月考勤记录</h3>
          <el-empty :image-size="60" description="暂无考勤记录" />
        </section>
      </div>

      <!-- 4. 待办视图 -->
      <div v-if="activeMenu === 'todo'" class="content-view todo-view">
        <header class="view-header">
          <h2 class="view-title">待办事项</h2>
          <div class="view-actions">
            <el-button type="primary">新建待办</el-button>
          </div>
        </header>
        <div class="todo-list">
          <div v-if="todos.length > 0" class="todo-stack">
            <div v-for="todo in todos" :key="todo.id" class="todo-tile" @click="handleTodoClick(todo)">
              <div class="todo-tile__status" :class="todo.priorityClass"></div>
              <div class="todo-tile__info">
                <p class="todo-tile__title">{{ todo.title }}</p>
                <span class="todo-tile__meta">{{ formatDate(todo.deadline) }}</span>
              </div>
              <el-checkbox size="large" @click.stop="handleTodoComplete(todo)" />
            </div>
          </div>
          <el-empty v-else :image-size="80" description="没有待办事项" />
        </div>
      </div>

      <!-- 5. 日程视图 -->
      <div v-if="activeMenu === 'schedule'" class="content-view schedule-view">
        <header class="view-header">
          <h2 class="view-title">日程安排</h2>
          <div class="view-actions">
            <el-button type="primary">新建日程</el-button>
          </div>
        </header>
        <div class="schedule-content">
          <el-empty :image-size="80" description="暂无日程安排" />
        </div>
      </div>

      <!-- 6. 工作报告视图 -->
      <div v-if="activeMenu === 'report'" class="content-view report-view">
        <header class="view-header">
          <h2 class="view-title">工作报告</h2>
          <div class="view-actions">
            <el-button type="primary">写周报</el-button>
          </div>
        </header>
        <div class="report-list">
          <el-empty :image-size="80" description="暂无工作报告" />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { 
  Search, Plus, Timer, Tickets, Management, Finished, 
  Money, FolderOpened, ChatLineRound, VideoPlay, Calendar,
  DocumentCopy, Clock, Notebook, Files
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const store = useStore()
const activeMenu = ref('apps')
const activeApprovalTab = ref('pending')
const searchQuery = ref('')
const currentTime = ref('')

const currentUser = computed(() => store.getters['user/currentUser'] || {})
const displayName = computed(() => currentUser.value.nickname || currentUser.value.username || '成员')

// 问候语
const greetingText = computed(() => {
  const h = new Date().getHours()
  if (h < 12) return '早上好'
  if (h < 18) return '下午好'
  return '晚上好'
})

// 当前日期文本
const currentDateText = computed(() => {
  return new Date().toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric', 
    weekday: 'long' 
  })
})

// 二级菜单配置
const subMenus = computed(() => [
  { key: 'apps', label: '常用应用', icon: Files, badge: 0 },
  { key: 'approval', label: '审批', icon: Finished, badge: 0 },
  { key: 'attendance', label: '考勤', icon: Clock, badge: 0 },
  { key: 'todo', label: '待办', icon: Notebook, badge: todos.value.length },
  { key: 'schedule', label: '日程', icon: Calendar, badge: 0 },
  { key: 'report', label: '工作报告', icon: DocumentCopy, badge: 0 }
])

// 审批标签页
const approvalTabs = ref([
  { key: 'pending', label: '待处理', count: 0 },
  { key: 'processed', label: '已处理', count: 0 },
  { key: 'initiated', label: '我发起的', count: 0 }
])

// 常用应用
const commonApps = ref([
  { key: 'punch', label: '签到打卡', elIcon: Timer, iconClass: 'icon-orange' },
  { key: 'flow', label: '审批流', elIcon: Finished, iconClass: 'icon-blue' },
  { key: 'report', label: '周报/日报', elIcon: Tickets, iconClass: 'icon-green' },
  { key: 'task', label: '协作待办', elIcon: Management, iconClass: 'icon-purple' },
  { key: 'meeting', label: '视频会议', elIcon: VideoPlay, iconClass: 'icon-pink' }
])

// 全量应用
const otherApps = ref([
  { key: 'finance', label: '财务报销', elIcon: Money, iconClass: 'icon-teal' },
  { key: 'disk', label: '企业网盘', elIcon: FolderOpened, iconClass: 'icon-indigo' },
  { key: 'assistant', label: 'AI助手', elIcon: ChatLineRound, iconClass: 'icon-cyan' }
])

// 待办事项
const todos = ref([
  { id: 1, title: '审核 Q4 前端架构方案', deadline: '今天 18:00', priorityClass: 'high' },
  { id: 2, title: '部门周例会 - 302 会议室', deadline: '明天 10:00', priorityClass: 'medium' }
])

// 方法
const handleMenuChange = (key) => {
  activeMenu.value = key
}

const handleAppClick = (app) => {
  ElMessage.success(`正在进入: ${app.label}`)
}

const handleTodoClick = (todo) => {
  ElMessage.info(`待办详情: ${todo.title}`)
}

const handleTodoComplete = (todo) => {
  ElMessage.success('待办已完成')
  todos.value = todos.value.filter(t => t.id !== todo.id)
}

const handlePunch = (type) => {
  const action = type === 'in' ? '上班' : '下班'
  ElMessage.success(`${action}打卡成功`)
}

const formatDate = (d) => d

// 更新当前时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

let timeInterval = null

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped lang="scss">
@mixin flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

@mixin flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

@mixin text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// ============================================================================
// 工作台容器 - 双栏布局
// ============================================================================
.workbench-wrapper {
  display: flex;
  width: 100%;
  height: 100%;
  background: var(--dt-bg-body);
  overflow: hidden;
}

// ============================================================================
// 左侧二级导航栏
// ============================================================================
.wb-sidebar {
  width: 200px;
  background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: var(--dt-spacing-lg) var(--dt-spacing-md);
  border-bottom: 1px solid var(--dt-border-lighter);
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.sidebar-nav {
  flex: 1;
  padding: var(--dt-spacing-md) 0;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
  padding: var(--dt-spacing-md) var(--dt-spacing-lg);
  cursor: pointer;
  position: relative;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  &.active {
    background: var(--dt-brand-lighter);
    color: var(--dt-brand-color);

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 3px;
      background: var(--dt-brand-color);
    }

    .nav-label {
      font-weight: 500;
    }
  }
}

.nav-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.nav-label {
  flex: 1;
  font-size: 14px;
  color: var(--dt-text-primary);
}

.nav-badge {
  background: var(--dt-error-color);
  color: var(--dt-bg-card);
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  @include flex-center;
  font-weight: 500;
}

// ============================================================================
// 右侧内容区
// ============================================================================
.wb-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.content-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 视图头部
.view-header {
  padding: var(--dt-spacing-lg) var(--dt-spacing-2xl);
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  @include flex-between;
}

.view-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.view-actions {
  display: flex;
  gap: var(--dt-spacing-md);
}

// ============================================================================
// 常用应用视图
// ============================================================================
.apps-view {
  padding: var(--dt-spacing-2xl) 40px;
}

.wb-header {
  margin-bottom: var(--dt-spacing-2xl);
  padding-bottom: var(--dt-spacing-xl);
  border-bottom: 1px solid var(--dt-border-lighter);
  @include flex-between;
}

.greeting-box {
  &__title {
    font-size: 24px;
    font-weight: 700;
    color: var(--dt-text-primary);
    margin: 0;
    letter-spacing: -0.5px;
  }

  &__date {
    font-size: 14px;
    color: var(--dt-text-tertiary);
    margin-top: 8px;
  }
}

.wb-search {
  position: relative;
  width: 320px;

  &__icon {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: var(--dt-text-tertiary);
    font-size: 16px;
  }

  &__input {
    width: 100%;
    height: 36px;
    padding: 0 16px 0 36px;
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-light);
    border-radius: var(--dt-radius-md);
    outline: none;
    font-size: 14px;
    transition: all var(--dt-transition-base);

    &:focus {
      background: var(--dt-bg-card);
      border-color: var(--dt-brand-color);
      box-shadow: 0 0 0 2px var(--dt-brand-lighter);
    }

    &::placeholder {
      color: var(--dt-text-quaternary);
    }
  }
}

.wb-section {
  margin-bottom: var(--dt-spacing-2xl);

  &__header {
    @include flex-between;
    margin-bottom: var(--dt-spacing-lg);
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;
  }
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: var(--dt-spacing-lg);

  &--sm {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    gap: var(--dt-spacing-md);
  }
}

.app-item {
  @include flex-center;
  flex-direction: column;
  gap: 10px;
  cursor: pointer;
  padding: var(--dt-spacing-md) 0;
  border-radius: var(--dt-radius-md);
  transition: all var(--dt-transition-base);
  position: relative;

  &:hover {
    background: var(--dt-bg-session-hover);
    transform: translateY(-2px);

    .app-item__icon {
      box-shadow: var(--dt-shadow-3);
      transform: scale(1.05);
    }
  }

  &__icon {
    width: 52px;
    height: 52px;
    border-radius: 12px;
    @include flex-center;
    font-size: 24px;
    background: var(--dt-bg-card);
    box-shadow: var(--dt-shadow-1);
    transition: all var(--dt-transition-base);

    &--sm {
      width: 44px;
      height: 44px;
      font-size: 20px;
    }

    &.icon-orange { background: var(--dt-warning-bg); color: var(--dt-warning-color); }
    &.icon-blue { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
    &.icon-green { background: var(--dt-success-bg); color: var(--dt-success-color); }
    &.icon-purple { background: var(--dt-info-bg); color: var(--dt-info-color); }
    &.icon-pink { background: var(--dt-error-bg); color: var(--dt-error-color); }
    &.icon-teal { background: var(--dt-brand-bg); color: #08979c; }
    &.icon-indigo { background: var(--dt-brand-lighter); color: var(--dt-brand-color); }
    &.icon-cyan { background: #e6fffe; color: #08bdb2; }
  }

  &__label {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-primary);
    font-weight: 500;
  }

  &__badge {
    position: absolute;
    top: 10px;
    right: 20px;
    background: var(--dt-error-color);
    color: var(--dt-bg-card);
    font-size: 10px;
    min-width: 16px;
    height: 16px;
    border-radius: 8px;
    @include flex-center;
    border: 1.5px solid var(--dt-bg-card);
    font-weight: 600;
  }
}

// ============================================================================
// 审批视图
// ============================================================================
.approval-view {
  padding: 0;
}

.approval-tabs {
  display: flex;
  padding: 0 var(--dt-spacing-2xl);
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
}

.tab-item {
  padding: var(--dt-spacing-md) var(--dt-spacing-xl);
  cursor: pointer;
  color: var(--dt-text-secondary);
  font-size: 14px;
  position: relative;
  transition: color var(--dt-transition-fast);

  &:hover {
    color: var(--dt-brand-color);
  }

  &.active {
    color: var(--dt-brand-color);
    font-weight: 500;

    &::after {
      content: '';
      position: absolute;
      left: 0;
      right: 0;
      bottom: -1px;
      height: 2px;
      background: var(--dt-brand-color);
    }
  }

  .tab-count {
    background: var(--dt-error-color);
    color: var(--dt-bg-card);
    font-size: 10px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    border-radius: 8px;
    @include flex-center;
    margin-left: 6px;
    font-weight: 500;
  }
}

.approval-list {
  flex: 1;
  padding: var(--dt-spacing-2xl);
  overflow-y: auto;
}

// ============================================================================
// 考勤视图
// ============================================================================
.attendance-view {
  padding: var(--dt-spacing-2xl) 40px;
}

.attendance-card {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  padding: var(--dt-spacing-2xl);
  text-align: center;
  box-shadow: var(--dt-shadow-card);
  margin-bottom: var(--dt-spacing-2xl);
}

.attendance-status {
  margin-bottom: var(--dt-spacing-xl);
}

.status-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--dt-brand-lighter);
  @include flex-center;
  margin: 0 auto var(--dt-spacing-md);
}

.status-icon {
  font-size: 36px;
  color: var(--dt-brand-color);
}

.status-text {
  font-size: 16px;
  color: var(--dt-text-primary);
  margin: 0 0 var(--dt-spacing-xs);
}

.status-time {
  font-size: 24px;
  font-weight: 600;
  color: var(--dt-brand-color);
  margin: 0;
}

.attendance-actions {
  display: flex;
  gap: var(--dt-spacing-lg);
  justify-content: center;
}

// ============================================================================
// 待办视图
// ============================================================================
.todo-view {
  padding: 0;
}

.todo-list {
  flex: 1;
  padding: var(--dt-spacing-2xl);
  overflow-y: auto;
}

.todo-stack {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-md);
}

.todo-tile {
  background: var(--dt-bg-card);
  padding: var(--dt-spacing-md) var(--dt-spacing-lg);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
  box-shadow: var(--dt-shadow-sm);

  &:hover {
    box-shadow: var(--dt-shadow-card);
    transform: translateX(2px);
  }

  &__status {
    width: 4px;
    height: 32px;
    border-radius: 2px;
    flex-shrink: 0;

    &.high { background: var(--dt-error-color); }
    &.medium { background: var(--dt-warning-color); }
    &.low { background: var(--dt-brand-color); }
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__title {
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);
    margin: 0;
    @include text-ellipsis;
  }

  &__meta {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    margin-top: 4px;
    display: block;
  }
}

// ============================================================================
// 日程视图
// ============================================================================
.schedule-view {
  padding: var(--dt-spacing-2xl) 40px;
}

.schedule-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

// ============================================================================
// 工作报告视图
// ============================================================================
.report-view {
  padding: 0;
}

.report-list {
  flex: 1;
  padding: var(--dt-spacing-2xl);
  overflow-y: auto;
}
</style>
