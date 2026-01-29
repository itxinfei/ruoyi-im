<template>
  <div class="workbench-panel">
    <!-- 顶部欢迎区 -->
    <header class="workbench-header">
      <div class="greeting-section">
        <h1 class="greeting-title">{{ greetingText }}，{{ displayName }}</h1>
        <p class="greeting-date">
          <span class="material-icons-outlined date-icon">event</span>
          {{ currentDateText }}
        </p>
      </div>
      <div class="header-actions">
        <div class="header-buttons">
          <button class="icon-btn" @click="refreshAllData" :class="{ loading: refreshing }" title="刷新数据">
            <span class="material-icons-outlined">{{ refreshing ? 'refresh' : 'refresh' }}</span>
          </button>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="workbench-content" v-loading="initialLoading">
      <!-- 快捷应用区域 -->
      <section class="quick-apps-section">
        <div class="section-header">
          <h3 class="section-title">
            <span class="title-icon title-icon-primary">
              <span class="material-icons-outlined">apps</span>
            </span>
            快捷应用
          </h3>
        </div>
        <div class="quick-apps-grid">
          <div
            v-for="app in quickApps"
            :key="app.id"
            class="quick-app-item"
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

      <!-- 数据卡片网格 -->
      <div class="bento-grid">
        <!-- 待办事项卡片 -->
        <div class="bento-card card-size-1x2" @click="navigateTo('todo')">
          <div class="bento-card-content todo-card-content">
            <div class="card-header">
              <h4 class="card-title">
                <span class="material-icons-outlined card-icon">task_alt</span>
                待办事项
              </h4>
              <span class="view-all-link">查看全部 →</span>
            </div>
            <div v-if="todoLoading" class="card-loading">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else-if="todoList.length > 0" class="todo-list">
              <div
                v-for="todo in todoList.slice(0, 5)"
                :key="todo.id"
                class="todo-item"
                :class="`priority-${todo.priority || 'medium'}`"
              >
                <span class="todo-dot"></span>
                <div class="todo-content">
                  <p class="todo-title">{{ todo.title }}</p>
                  <span class="todo-deadline">
                    <span class="material-icons-outlined">schedule</span>
                    {{ formatDeadline(todo.deadline) }}
                  </span>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <span class="material-icons-outlined">check_circle</span>
              <p>暂无待办事项</p>
            </div>
          </div>
        </div>

        <!-- 审批卡片 -->
        <div class="bento-card card-size-1x1" @click="navigateTo('approval')">
          <div class="bento-card-content approval-card-content">
            <div class="card-header compact">
              <span class="material-icons-outlined card-icon">approval</span>
              <span class="card-title">待审批</span>
            </div>
            <div class="card-value">{{ approvalCount }}</div>
            <div class="card-action">
              <span class="action-text">立即处理</span>
              <span class="material-icons-outlined">arrow_forward</span>
            </div>
          </div>
        </div>

        <!-- 邮件卡片 -->
        <div class="bento-card card-size-1x1" @click="navigateTo('mail')">
          <div class="bento-card-content mail-card-content">
            <div class="card-header compact">
              <span class="material-icons-outlined card-icon">mail</span>
              <span class="card-title">未读邮件</span>
            </div>
            <div class="card-value">{{ mailCount }}</div>
            <div class="card-action">
              <span class="action-text">查看邮箱</span>
              <span class="material-icons-outlined">arrow_forward</span>
            </div>
          </div>
        </div>

        <!-- 公告卡片 -->
        <div class="bento-card card-size-1x2" @click="navigateTo('announcement')">
          <div class="bento-card-content announcement-card-content">
            <div class="card-header">
              <h4 class="card-title">
                <span class="material-icons-outlined card-icon">campaign</span>
                公司公告
              </h4>
              <span class="view-all-link">更多 →</span>
            </div>
            <div v-if="announcementLoading" class="card-loading">
              <el-skeleton :rows="2" animated />
            </div>
            <div v-else-if="announcementList.length > 0" class="announcement-list">
              <div
                v-for="item in announcementList.slice(0, 3)"
                :key="item.id"
                class="announcement-item"
              >
                <span class="announcement-tag">{{ item.type || '通知' }}</span>
                <div class="announcement-content">
                  <p class="announcement-title">{{ item.title }}</p>
                  <span class="announcement-meta">
                    {{ formatDate(item.createTime) }}
                  </span>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <span class="material-icons-outlined">campaign</span>
              <p>暂无公告</p>
            </div>
          </div>
        </div>

        <!-- 今日日程卡片 -->
        <div class="bento-card card-size-2x1" @click="navigateTo('calendar')">
          <div class="bento-card-content schedule-card-content">
            <div class="card-header">
              <h4 class="card-title">
                <span class="material-icons-outlined card-icon">calendar_today</span>
                今日日程
              </h4>
            </div>
            <div v-if="scheduleList.length > 0" class="schedule-list">
              <div
                v-for="item in scheduleList.slice(0, 3)"
                :key="item.id"
                class="schedule-item"
              >
                <div class="schedule-time">{{ item.timeRange }}</div>
                <div class="schedule-info">
                  <p class="schedule-title">{{ item.title }}</p>
                  <span class="schedule-location" v-if="item.location">
                    <span class="material-icons-outlined">place</span>
                    {{ item.location }}
                  </span>
                </div>
              </div>
            </div>
            <div v-else class="empty-state inline">
              <span class="material-icons-outlined">event_available</span>
              <p>今日暂无日程安排</p>
            </div>
          </div>
        </div>

        <!-- 工作概览统计卡片 -->
        <div class="bento-card card-size-2x2">
          <div class="bento-card-content stats-card-content">
            <div class="card-header">
              <h4 class="card-title">
                <span class="material-icons-outlined card-icon">analytics</span>
                工作概览
              </h4>
            </div>
            <div class="stats-grid">
              <div class="stat-item" @click="navigateTo('todo')" style="--stat-color: #1677ff">
                <div class="stat-value">
                  <span class="stat-number">{{ todoStats.pending || 0 }}</span>
                  <span class="stat-unit">项</span>
                </div>
                <div class="stat-label">待办事项</div>
              </div>
              <div class="stat-item" @click="navigateTo('approval')" style="--stat-color: #52c41a">
                <div class="stat-value">
                  <span class="stat-number">{{ approvalCount }}</span>
                  <span class="stat-unit">项</span>
                </div>
                <div class="stat-label">待审批</div>
              </div>
              <div class="stat-item" @click="navigateTo('mail')" style="--stat-color: #fa8c16">
                <div class="stat-value">
                  <span class="stat-number">{{ mailCount }}</span>
                  <span class="stat-unit">封</span>
                </div>
                <div class="stat-label">未读邮件</div>
              </div>
              <div class="stat-item" style="--stat-color: #722ed1">
                <div class="stat-value">
                  <span class="stat-number">{{ onlineCount }}</span>
                  <span class="stat-unit">人</span>
                </div>
                <div class="stat-label">在线同事</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

const store = useStore()

// ============================================================================
// 状态管理
// ============================================================================
const initialLoading = ref(true)
const refreshing = ref(false)
const refreshTimer = ref(null)

// ============================================================================
// 用户信息
// ============================================================================
const currentUser = computed(() => store.getters['user/currentUser'] || {})
const displayName = computed(() => currentUser.value.nickname || currentUser.value.username || '用户')

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
  const month = now.getMonth() + 1
  const date = now.getDate()
  const weekday = weekdays[now.getDay()]
  return `${month}月${date}日 ${weekday}`
})

// ============================================================================
// 快捷应用配置
// ============================================================================
const quickApps = ref([
  { id: 'qa-1', label: '待办', icon: 'task_alt', iconClass: 'icon-blue', key: 'todo', tab: 'todo' },
  { id: 'qa-2', label: '审批', icon: 'approval', iconClass: 'icon-green', key: 'approval', tab: 'approval' },
  { id: 'qa-3', label: '日程', icon: 'calendar_today', iconClass: 'icon-orange', key: 'calendar', tab: 'calendar' },
  { id: 'qa-4', label: '邮件', icon: 'mail', iconClass: 'icon-purple', key: 'mail', tab: 'mail' },
  { id: 'qa-5', label: '公告', icon: 'campaign', iconClass: 'icon-pink', key: 'announcement', tab: 'announcement' },
  { id: 'qa-6', label: '会议', icon: 'videocam', iconClass: 'icon-cyan', key: 'meeting', tab: 'meeting' },
  { id: 'qa-7', label: '云盘', icon: 'folder_shared', iconClass: 'icon-indigo', key: 'cloud', tab: 'cloud' },
  { id: 'qa-8', label: '文档', icon: 'description', iconClass: 'icon-teal', key: 'doc', tab: 'document' }
])

// ============================================================================
// 数据加载状态
// ============================================================================
const todoLoading = ref(false)
const announcementLoading = ref(false)

// ============================================================================
// 待办事项数据
// ============================================================================
const todoList = ref([])
const todoStats = ref({
  total: 0,
  pending: 0,
  completed: 0,
  overdue: 0
})

// 模拟待办数据（实际应从 API 获取）
const loadTodoData = async () => {
  todoLoading.value = true
  try {
    // TODO: 调用真实 API
    // const res = await getTodoList({ status: 'pending', limit: 5 })

    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 300))

    todoList.value = [
      { id: 1, title: '完成项目需求文档', deadline: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(), priority: 'high', status: 'pending' },
      { id: 2, title: '团队周会', deadline: new Date(Date.now() + 5 * 60 * 60 * 1000).toISOString(), priority: 'medium', status: 'pending' },
      { id: 3, title: '代码审查', deadline: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(), priority: 'low', status: 'pending' }
    ]

    todoStats.value = {
      total: 12,
      pending: 5,
      completed: 6,
      overdue: 1
    }
  } catch (error) {
    console.error('加载待办失败:', error)
  } finally {
    todoLoading.value = false
  }
}

// 格式化截止日期
const formatDeadline = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const now = new Date()
  const diff = d - now
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days < 0) return '已逾期'
  if (days === 0) return '今天'
  if (days === 1) return '明天'
  if (days < 7) return `${days}天后`
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

// ============================================================================
// 公告数据
// ============================================================================
const announcementList = ref([])

const loadAnnouncementData = async () => {
  announcementLoading.value = true
  try {
    // TODO: 调用真实 API
    // const res = await getAnnouncementList({ limit: 5 })

    await new Promise(resolve => setTimeout(resolve, 200))

    announcementList.value = [
      { id: 1, title: '关于春节放假安排的通知', type: '重要', createTime: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString() },
      { id: 2, title: '系统升级维护通知', type: '通知', createTime: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString() },
      { id: 3, title: '新员工入职培训安排', type: '人事', createTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString() }
    ]
  } catch (error) {
    console.error('加载公告失败:', error)
  } finally {
    announcementLoading.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const now = new Date()
  const diff = now - d
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(hours / 24)

  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

// ============================================================================
// 审批数据
// ============================================================================
const approvalCount = ref(0)

const loadApprovalData = async () => {
  try {
    // TODO: 调用真实 API
    await new Promise(resolve => setTimeout(resolve, 100))
    approvalCount.value = 3
  } catch (error) {
    console.error('加载审批数据失败:', error)
  }
}

// ============================================================================
// 邮件数据
// ============================================================================
const mailCount = ref(0)

const loadMailData = async () => {
  try {
    // TODO: 调用真实 API
    await new Promise(resolve => setTimeout(resolve, 100))
    mailCount.value = 12
  } catch (error) {
    console.error('加载邮件数据失败:', error)
  }
}

// ============================================================================
// 日程数据
// ============================================================================
const scheduleList = ref([])

const loadScheduleData = async () => {
  try {
    // TODO: 调用真实 API
    await new Promise(resolve => setTimeout(resolve, 100))
    const now = new Date()
    scheduleList.value = [
      { id: 1, title: '产品需求评审会', timeRange: '10:00-11:30', location: '会议室A' },
      { id: 2, title: '设计沟通', timeRange: '14:00-15:00', location: '线上' },
      { id: 3, title: '代码评审', timeRange: '16:00-17:00', location: '会议室B' }
    ]
  } catch (error) {
    console.error('加载日程数据失败:', error)
  }
}

// ============================================================================
// 在线用户数据
// ============================================================================
const onlineCount = ref(0)

const loadOnlineData = async () => {
  try {
    // 从 store 获取在线用户数
    const imState = store.state.im || {}
    onlineCount.value = imState.onlineUsers?.length || Math.floor(Math.random() * 20) + 10
  } catch (error) {
    console.error('加载在线用户失败:', error)
  }
}

// ============================================================================
// 数据刷新
// ============================================================================
const refreshAllData = async () => {
  refreshing.value = true
  try {
    await Promise.all([
      loadTodoData(),
      loadAnnouncementData(),
      loadApprovalData(),
      loadMailData(),
      loadScheduleData(),
      loadOnlineData()
    ])
    ElMessage.success('数据已刷新')
  } catch (error) {
    ElMessage.error('刷新失败')
  } finally {
    refreshing.value = false
  }
}

// ============================================================================
// 导航处理
// ============================================================================
const navigateTo = (tab) => {
  // 触发 tab 切换事件
  window.dispatchEvent(new CustomEvent('switch-tab', { detail: tab }))
}

const handleAppClick = (app) => {
  if (app.tab) {
    navigateTo(app.tab)
  } else {
    ElMessage.info(`打开应用: ${app.label}`)
  }
}

// ============================================================================
// 生命周期
// ============================================================================
onMounted(async () => {
  // 初始加载所有数据
  await refreshAllData()
  initialLoading.value = false

  // 设置定时刷新（每5分钟）
  refreshTimer.value = setInterval(() => {
    loadTodoData()
    loadAnnouncementData()
    loadApprovalData()
    loadMailData()
  }, 5 * 60 * 1000)
})

onUnmounted(() => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

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
  background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 50%, #ffffff 100%);
  border-bottom: 1px solid var(--dt-border-light);
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  min-height: 88px;

  &::before {
    content: '';
    position: absolute;
    right: -50px;
    top: -100px;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(22, 119, 255, 0.06) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }
}

.greeting-section {
  position: relative;
  z-index: 1;
}

.greeting-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
  letter-spacing: -0.01em;
  line-height: 1.3;
}

.greeting-date {
  font-size: 13px;
  color: var(--dt-text-secondary);
  margin: 4px 0 0 0;
  font-weight: 400;
  display: flex;
  align-items: center;
  gap: 4px;

  .date-icon {
    font-size: 16px;
    color: var(--dt-brand-color);
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: #ffffff;
  color: var(--dt-text-secondary);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    border-color: var(--dt-brand-color);
    color: var(--dt-brand-color);
    background: var(--dt-brand-bg);
  }

  &.loading {
    pointer-events: none;
    opacity: 0.6;

    .material-icons-outlined {
      animation: spin 1s linear infinite;
    }
  }

  .material-icons-outlined {
    font-size: 18px;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// ============================================================================
// 主内容区
// ============================================================================
.workbench-content {
  flex: 1;
  padding: 20px 32px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 24px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: 3px;

    &:hover {
      background: var(--dt-text-quaternary);
    }
  }
}

// ============================================================================
// 快捷应用区域
// ============================================================================
.quick-apps-section {
  position: relative;
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
  gap: 8px;
  font-size: 15px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin: 0;
}

.title-icon {
  width: 28px;
  height: 28px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-icon-primary {
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
}

.quick-apps-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 12px;
}

.quick-app-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 8px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-lg);
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    border-color: var(--dt-brand-color);
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(22, 119, 255, 0.12);
  }

  &:active {
    transform: translateY(0);
  }
}

.app-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--dt-transition-base);

  .material-icons-outlined {
    font-size: 22px;
  }

  &.icon-blue { background: rgba(22, 119, 255, 0.12); color: #1677ff; }
  &.icon-green { background: rgba(82, 196, 26, 0.12); color: #52c41a; }
  &.icon-orange { background: rgba(250, 140, 22, 0.12); color: #fa8c16; }
  &.icon-purple { background: rgba(114, 46, 209, 0.12); color: #722ed1; }
  &.icon-pink { background: rgba(235, 47, 150, 0.12); color: #eb2f96; }
  &.icon-cyan { background: rgba(13, 202, 240, 0.12); color: #13c2c2; }
  &.icon-indigo { background: rgba(89, 78, 236, 0.12); color: #594efc; }
  &.icon-teal { background: rgba(19, 180, 167, 0.12); color: #13c2c2; }
}

.app-label {
  font-size: 12px;
  font-weight: 400;
  color: var(--dt-text-primary);
  text-align: center;
  white-space: nowrap;
}

.app-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: var(--dt-error-color);
  color: #fff;
  font-size: 11px;
  font-weight: 500;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
}

// ============================================================================
// Bento Grid 布局
// ============================================================================
.bento-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  align-items: start;
}

// ============================================================================
// Bento 卡片
// ============================================================================
.bento-card {
  position: relative;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  overflow: hidden;
  transition: all var(--dt-transition-base);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  cursor: pointer;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transform: translateY(-1px);
    border-color: var(--dt-brand-color);
  }

  // 卡片尺寸
  &.card-size-1x1 {
    grid-column: span 1;
    grid-row: span 1;
  }

  &.card-size-1x2 {
    grid-column: span 1;
    grid-row: span 2;
  }

  &.card-size-2x1 {
    grid-column: span 2;
    grid-row: span 1;
  }

  &.card-size-2x2 {
    grid-column: span 2;
    grid-row: span 2;
  }
}

.bento-card-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);

  &.compact {
    padding: 12px 16px;

    .card-title {
      font-size: 14px;
    }

    .card-icon {
      font-size: 18px;
    }
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.card-icon {
  font-size: 18px;
  color: var(--dt-brand-color);
}

.view-all-link {
  font-size: 12px;
  color: var(--dt-text-link);
  white-space: nowrap;
}

.card-loading {
  padding: 16px;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  color: var(--dt-text-quaternary);

  &.inline {
    flex-direction: row;
    gap: 12px;
    padding: 20px;
  }

  .material-icons-outlined {
    font-size: 32px;
    margin-bottom: 8px;
  }

  p {
    font-size: 13px;
    margin: 0;
  }
}

// ============================================================================
// 待办卡片样式
// ============================================================================
.todo-card-content {
  min-height: 280px;
}

.todo-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  transition: all var(--dt-transition-base);

  &.priority-high {
    border-left: 3px solid #ff4d4f;
  }

  &.priority-medium {
    border-left: 3px solid #faad14;
  }

  &.priority-low {
    border-left: 3px solid var(--dt-brand-color);
  }
}

.todo-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;

  .todo-item.priority-high & { background: #ff4d4f; }
  .todo-item.priority-medium & { background: #faad14; }
  .todo-item.priority-low & { background: var(--dt-brand-color); }
}

.todo-content {
  flex: 1;
  min-width: 0;
}

.todo-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin: 0 0 3px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-deadline {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: var(--dt-text-quaternary);

  .material-icons-outlined {
    font-size: 12px;
  }
}

// ============================================================================
// 公告卡片样式
// ============================================================================
.announcement-card-content {
  min-height: 280px;
}

.announcement-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-item {
  display: flex;
  gap: 10px;
  padding: 12px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
}

.announcement-tag {
  padding: 2px 6px;
  font-size: 10px;
  font-weight: 600;
  border-radius: var(--dt-radius-sm);
  flex-shrink: 0;
  height: fit-content;
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
}

.announcement-content {
  flex: 1;
  min-width: 0;
}

.announcement-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin: 0 0 6px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.announcement-meta {
  font-size: 11px;
  color: var(--dt-text-quaternary);
}

// ============================================================================
// 日程卡片样式
// ============================================================================
.schedule-card-content {
  min-height: 180px;
}

.schedule-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.schedule-item {
  display: flex;
  gap: 12px;
  padding: 10px 12px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
}

.schedule-time {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--dt-brand-color);
  padding: 4px 8px;
  background: var(--dt-brand-bg);
  border-radius: var(--dt-radius-sm);
}

.schedule-info {
  flex: 1;
  min-width: 0;
}

.schedule-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin: 0 0 3px 0;
}

.schedule-location {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: var(--dt-text-quaternary);

  .material-icons-outlined {
    font-size: 12px;
  }
}

// ============================================================================
// 统计卡片样式
// ============================================================================
.stats-card-content {
  min-height: 200px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: var(--dt-bg-input);
  border-radius: var(--dt-radius-lg);
  transition: all var(--dt-transition-base);

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--dt-shadow-2);
  }
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 8px;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.stat-unit {
  font-size: 13px;
}

.stat-label {
  font-size: 13px;
  color: var(--dt-text-secondary);
}

// ============================================================================
// 快捷卡片样式
// ============================================================================
.approval-card-content,
.mail-card-content {
  min-height: 140px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-value {
  font-size: 36px;
  font-weight: 700;
  color: var(--dt-text-primary);
  text-align: center;
  padding: 20px 0;
}

.card-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px;
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  font-size: 13px;
  font-weight: 500;

  .material-icons-outlined {
    font-size: 18px;
  }
}

// ============================================================================
// 响应式
// ============================================================================
@media (max-width: 1400px) {
  .quick-apps-grid {
    grid-template-columns: repeat(6, 1fr);
  }

  .bento-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .workbench-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 16px 20px;
  }

  .greeting-title {
    font-size: 18px;
  }

  .workbench-content {
    padding: 16px 20px;
  }

  .quick-apps-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
  }

  .bento-grid {
    grid-template-columns: 1fr;
  }

  .bento-card {
    &.card-size-2x1,
    &.card-size-2x2 {
      grid-column: span 1;
    }

    &.card-size-1x2,
    &.card-size-2x2 {
      grid-row: span 1;
    }
  }
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
  color: var(--dt-text-primary);
}

.dark .greeting-date {
  color: var(--dt-text-secondary);
}

.dark .icon-btn {
  background: var(--dt-bg-card);
  border-color: var(--dt-border-dark);
}

.dark .quick-app-item {
  background: var(--dt-bg-card);
  border-color: var(--dt-border-dark);
}

.dark .bento-card {
  background: var(--dt-bg-card);
  border-color: var(--dt-border-dark);
}

.dark .todo-item,
.dark .announcement-item,
.dark .schedule-item {
  background: var(--dt-bg-input);
  border-color: var(--dt-border-dark);
}

.dark .stat-item {
  background: var(--dt-bg-input);
}

.dark .app-icon {
  background: var(--dt-bg-input);
}
</style>
