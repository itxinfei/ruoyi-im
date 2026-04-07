<template>
  <div class="workbench-wrapper">
    <!-- 左侧二级导航 -->
    <aside class="wb-sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">
          工作台
        </h2>
      </div>
      <nav class="sidebar-nav">
        <div
          v-for="menu in subMenus"
          :key="menu.key"
          class="nav-item"
          :class="{ active: activeMenu === menu.key }"
          @click="handleMenuChange(menu.key)"
        >
          <el-icon class="nav-icon">
            <component :is="menu.icon" />
          </el-icon>
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
            <h1 class="greeting-box__title">
              {{ greetingText }}，{{ displayName }}
            </h1>
            <p class="greeting-box__date">
              {{ currentDateText }}
            </p>
          </div>
          <div class="wb-search">
            <el-icon class="wb-search__icon">
              <Search />
            </el-icon>
            <input v-model="searchQuery" class="wb-search__input" placeholder="搜索应用...">
          </div>
        </header>

        <!-- 常用应用 -->
        <section class="wb-section">
          <div class="wb-section__header">
            <h3 class="wb-section__title">
              常用应用
            </h3>
            <el-dropdown trigger="click" @command="handleLayoutCommand">
              <el-button link type="primary">
                管理
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="save">保存当前布局</el-dropdown-item>
                  <el-dropdown-item command="reset">重置布局</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="app-grid">
            <div
              v-for="app in commonApps"
              :key="app.key"
              class="app-item"
              @click="handleAppClick(app)"
            >
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
            <h3 class="wb-section__title">
              全量中心
            </h3>
          </div>
          <div class="app-grid app-grid--sm">
            <div
              v-for="app in otherApps"
              :key="app.key"
              class="app-item app-item--sm"
              @click="handleAppClick(app)"
            >
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
          <h2 class="view-title">
            审批中心
          </h2>
          <div class="view-actions">
            <el-button type="primary" @click="showApprovalDialog = true">
              发起审批
            </el-button>
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
          <h2 class="view-title">
            考勤打卡
          </h2>
        </header>
        <div class="attendance-card">
          <div class="attendance-status">
            <div class="status-circle" :class="{ 'status-done': attendanceStatus.checkedIn && attendanceStatus.checkedOut }">
              <el-icon class="status-icon">
                <Timer />
              </el-icon>
            </div>
            <p class="status-text">
              {{ attendanceStatus.text }}
            </p>
            <p class="status-time">
              {{ currentTime }}
            </p>
            <div v-if="todayAttendance && todayAttendance.checkInTime" class="attendance-times">
              <span v-if="todayAttendance.checkInTime" class="time-item">
                上班: {{ todayAttendance.checkInTime.substring(11, 16) }}
              </span>
              <span v-if="todayAttendance.checkOutTime" class="time-item">
                下班: {{ todayAttendance.checkOutTime.substring(11, 16) }}
              </span>
            </div>
          </div>
          <div class="attendance-actions">
            <el-button 
              type="primary" 
              size="large" 
              :disabled="attendanceStatus.checkedIn"
              :loading="loading"
              @click="handlePunch('in')"
            >
              上班打卡
            </el-button>
            <el-button 
              type="success" 
              size="large" 
              :disabled="!attendanceStatus.checkedIn || attendanceStatus.checkedOut"
              :loading="loading"
              @click="handlePunch('out')"
            >
              下班打卡
            </el-button>
          </div>
        </div>
        <section class="wb-section">
          <h3 class="wb-section__title">
            本月考勤记录 ({{ attendanceRecords.length }}条)
          </h3>
          <div v-if="attendanceRecords.length > 0" class="attendance-list">
            <div v-for="record in attendanceRecords.slice(0, 10)" :key="record.id" class="attendance-item">
              <span class="attendance-date">{{ record.attendanceDate }}</span>
              <span class="attendance-time">{{ record.checkInTime?.substring(11, 16) || '--:--' }} - {{ record.checkOutTime?.substring(11, 16) || '--:--' }}</span>
              <el-tag :type="record.status === 'NORMAL' ? 'success' : 'warning'" size="small">
                {{ record.status === 'NORMAL' ? '正常' : '异常' }}
              </el-tag>
            </div>
          </div>
          <el-empty v-else :image-size="60" description="暂无考勤记录" />
        </section>
      </div>

      <!-- 4. 待办视图 -->
      <div v-if="activeMenu === 'todo'" class="content-view todo-view">
        <header class="view-header">
          <h2 class="view-title">
            待办事项
          </h2>
          <div class="view-actions">
            <el-button type="primary" @click="emit('switch-module', 'todo')">
              新建待办
            </el-button>
          </div>
        </header>
        <div class="todo-list">
          <div v-if="todos.length > 0" class="todo-stack">
            <div
              v-for="todo in todos.slice(0, 5)"
              :key="todo.id"
              class="todo-tile"
              @click="handleTodoClick(todo)"
            >
              <div class="todo-tile__status" :class="todo.priorityClass" />
              <div class="todo-tile__info">
                <p class="todo-tile__title">
                  {{ todo.title }}
                </p>
                <span class="todo-tile__meta">{{ formatDate(todo.deadline) }}</span>
              </div>
              <el-checkbox size="large" @click.stop="handleTodoComplete(todo)" />
            </div>
            <div v-if="todos.length > 5" class="todo-more">
              <el-button link type="primary" @click="emit('switch-module', 'todo')">
                查看全部 {{ todos.length }} 条待办
              </el-button>
            </div>
          </div>
          <el-empty v-else :image-size="80" description="没有待办事项" />
        </div>
      </div>

      <!-- 5. 日程视图 -->
      <div v-if="activeMenu === 'schedule'" class="content-view schedule-view">
        <header class="view-header">
          <h2 class="view-title">
            日程安排
          </h2>
          <div class="view-actions">
            <el-button type="primary" @click="emit('switch-module', 'calendar')">
              新建日程
            </el-button>
          </div>
        </header>
        <div class="schedule-content">
          <div v-if="scheduleList.length > 0" class="schedule-list">
            <div v-for="schedule in scheduleList.slice(0, 5)" :key="schedule.id" class="schedule-item" @click="handleScheduleClick(schedule)">
              <div class="schedule-time">
                {{ schedule.startTime?.substring(5, 16) || '' }}
              </div>
              <div class="schedule-info">
                <p class="schedule-title">{{ schedule.title }}</p>
                <span v-if="schedule.location" class="schedule-location">{{ schedule.location }}</span>
              </div>
            </div>
            <div v-if="scheduleList.length > 5" class="schedule-more">
              <el-button link type="primary" @click="emit('switch-module', 'calendar')">
                查看全部日程
              </el-button>
            </div>
          </div>
          <el-empty v-else :image-size="80" description="暂无日程安排" />
        </div>
      </div>

      <!-- 6. 工作报告视图 -->
      <div v-if="activeMenu === 'report'" class="content-view report-view">
        <header class="view-header">
          <h2 class="view-title">
            工作报告
          </h2>
          <div class="view-actions">
            <el-button type="primary" @click="showReportDialog = true">
              写周报
            </el-button>
          </div>
        </header>
        <div class="report-list">
          <el-empty :image-size="80" description="暂无工作报告" />
        </div>
      </div>
    </main>
    <!-- 发起审批对话框 -->
    <CreateApprovalDialog v-model="showApprovalDialog" @success="loadApprovals" />

    <!-- 周报对话框 -->
    <el-dialog v-model="showReportDialog" title="写周报" width="520px" append-to-body>
      <el-form label-position="top">
        <el-form-item label="周期">
          <el-date-picker
            v-model="reportForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="本周工作总结">
          <el-input
            v-model="reportForm.summary"
            type="textarea"
            :rows="4"
            placeholder="请简要描述本周完成的工作内容"
          />
        </el-form-item>
        <el-form-item label="下周工作计划">
          <el-input
            v-model="reportForm.nextWeekPlan"
            type="textarea"
            :rows="3"
            placeholder="请简要描述下周工作计划"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="reportForm.remark"
            type="textarea"
            :rows="2"
            placeholder="其他需要说明的情况（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReportDialog = false">取消</el-button>
        <el-button type="primary" :loading="reportLoading" @click="handleSubmitReport">提交周报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import {
  Search, Timer, Tickets, Management, Finished,
  Money, FolderOpened, ChatLineRound, VideoPlay, Calendar,
  DocumentCopy, Clock, Notebook, Files
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getOverview, getAppsByCategory, getRecentApps, recordAppUsage, getLayoutConfig, saveLayoutConfig, resetLayoutConfig } from '@/api/im/workbench'
import { getTodayStatus, checkIn as apiCheckIn, checkOut as apiCheckOut, getAttendanceList } from '@/api/im/attendance'
import { getMyTasks, updateTaskStatus } from '@/api/im/task'
import { getPendingApprovals } from '@/api/im/approval'
import { getSchedulesByRange } from '@/api/im/schedule'
import { createWorkReport } from '@/api/im/workReport'
import CreateApprovalDialog from '@/components/CreateApprovalDialog/index.vue'

const store = useStore()
const emit = defineEmits(['switch-module'])
const activeMenu = ref('apps')
const activeApprovalTab = ref('pending')
const searchQuery = ref('')
const currentTime = ref('')
const loading = ref(false)
const overview = ref({})
const todayAttendance = ref(null)
const attendanceRecords = ref([])
const todos = ref([])
const approvalList = ref([])
const scheduleList = ref([])
const allApps = ref([])
const recentApps = ref([])
const appsLoading = ref(false)
const savedLayout = ref(null) // 保存的布局配置
const showApprovalDialog = ref(false)
const showReportDialog = ref(false)
const reportLoading = ref(false)
const reportForm = ref({ dateRange: [], summary: '', nextWeekPlan: '', remark: '' })

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
  { key: 'approval', label: '审批', icon: Finished, badge: overview.value.approvalCount || 0 },
  { key: 'attendance', label: '考勤', icon: Clock, badge: 0 },
  { key: 'todo', label: '待办', icon: Notebook, badge: overview.value.todoCount || todos.value.length },
  { key: 'schedule', label: '日程', icon: Calendar, badge: 0 },
  { key: 'report', label: '工作报告', icon: DocumentCopy, badge: 0 }
])

// 审批标签页
const approvalTabs = ref([
  { key: 'pending', label: '待处理', count: 0 },
  { key: 'processed', label: '已处理', count: 0 },
  { key: 'initiated', label: '我发起的', count: 0 }
])

// 图标映射表
const iconMap = {
  OFFICE: { component: Tickets, class: 'icon-blue' },
  DATA: { component: Management, class: 'icon-purple' },
  TOOLS: { component: FolderOpened, class: 'icon-teal' },
  CUSTOM: { component: Files, class: 'icon-cyan' },
  default: { component: Files, class: 'icon-indigo' }
}

// 分类到模块的映射
const categoryRouteMap = {
  OFFICE: '/approval',
  DATA: '/documents',
  TOOLS: '/documents',
  CUSTOM: null
}

// 将后端应用数据转换为前端格式
const transformApp = (app) => {
  const iconInfo = iconMap[app.category] || iconMap.default
  const route = categoryRouteMap[app.category] || app.appUrl
  return {
    id: app.id,
    key: app.code || app.id,
    label: app.name,
    elIcon: iconInfo.component,
    iconClass: iconInfo.class,
    route: route,
    action: route ? null : 'custom',
    appUrl: app.appUrl,
    appType: app.appType,
    badge: 0
  }
}

// 常用应用 - 优先显示最近使用的应用，最多5个
const commonApps = computed(() => {
  if (searchQuery.value) {
    return filteredApps.value.slice(0, 5)
  }
  // 如果有最近使用的应用，优先显示
  if (recentApps.value.length > 0) {
    return recentApps.value.slice(0, 5).map(transformApp)
  }
  return allApps.value.slice(0, 5).map(transformApp)
})

// 全量应用 - 搜索模式下显示所有匹配结果
const otherApps = computed(() => {
  if (searchQuery.value) {
    return filteredApps.value.slice(5)
  }
  return allApps.value.slice(5).map(transformApp)
})

// 过滤后的应用（用于搜索）
const filteredApps = computed(() => {
  if (!searchQuery.value) {
    return []
  }
  const query = searchQuery.value.toLowerCase()
  return allApps.value
    .filter(app => app.name.toLowerCase().includes(query) ||
                   (app.description && app.description.toLowerCase().includes(query)) ||
                   app.code.toLowerCase().includes(query))
    .map(transformApp)
})

// 方法
const handleMenuChange = (key) => {
  activeMenu.value = key
}

// 处理布局操作
const handleLayoutCommand = (command) => {
  if (command === 'save') {
    handleSaveLayout()
  } else if (command === 'reset') {
    handleResetLayout()
  }
}

const handleAppClick = async (app) => {
  // 记录应用使用
  if (app.id) {
    try {
      await recordAppUsage(app.id)
    } catch (e) {
      console.error('记录应用使用失败:', e)
      ElMessage.error('记录应用使用失败')
    }
  }

  // 如果有 route，跳转到对应模块（通过 emit 切换 activeModule）
  if (app.route) {
    const moduleMap = {
      '/approval': 'approval',
      '/todo': 'todo',
      '/documents': 'documents',
      '/calendar': 'calendar',
      '/assistant': 'assistant'
    }
    const module = moduleMap[app.route]
    if (module) {
      emit('switch-module', module)
    }
    return
  }
  // 如果有 action，切换到对应视图
  if (app.action) {
    if (app.action === 'attendance') {
      activeMenu.value = 'attendance'
    } else if (app.action === 'report') {
      activeMenu.value = 'report'
    } else if (app.action === 'meeting') {
      emit('switch-module', 'chat')
    } else {
      ElMessage.info(`${app.label}功能开发中`)
    }
    return
  }
  ElMessage.success(`正在进入: ${app.label}`)
}

const handleTodoClick = (todo) => {
  emit('switch-module', 'todo')
}

const handleScheduleClick = (schedule) => {
  emit('switch-module', 'calendar')
}

// 提交周报
const handleSubmitReport = async () => {
  if (!reportForm.value.dateRange || reportForm.value.dateRange.length !== 2) {
    ElMessage.warning('请选择周报周期')
    return
  }
  if (!reportForm.value.summary.trim()) {
    ElMessage.warning('请填写本周工作总结')
    return
  }
  reportLoading.value = true
  try {
    const [startDate, endDate] = reportForm.value.dateRange
    const formatDate = (d) => d instanceof Date ? d.toISOString().split('T')[0] : d
    const res = await createWorkReport({
      reportType: 'WEEKLY',
      reportDate: formatDate(endDate), // 周报日期为周期结束日
      workContent: reportForm.value.summary,
      tomorrowPlan: reportForm.value.nextWeekPlan,
      issues: reportForm.value.remark,
      completionStatus: 'COMPLETED',
      isDraft: false
    })
    if (res.code === 200) {
      ElMessage.success('周报已提交')
      showReportDialog.value = false
      reportForm.value = { dateRange: [], summary: '', nextWeekPlan: '', remark: '' }
    } else {
      throw new Error(res.message || '提交失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '提交失败，请重试')
  } finally {
    reportLoading.value = false
  }
}

const handleTodoComplete = async (todo) => {
  try {
    const res = await updateTaskStatus(todo.id, 'COMPLETED')
    if (res.code === 200) {
      ElMessage.success('待办已完成')
      todos.value = todos.value.filter(t => t.id !== todo.id)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handlePunch = async (type) => {
  try {
    loading.value = true
    const action = type === 'in' ? '上班' : '下班'
    const apiCall = type === 'in' ? apiCheckIn : apiCheckOut
    const res = await apiCall({})
    if (res.code === 200) {
      ElMessage.success(`${action}打卡成功`)
      await loadTodayAttendance()
    } else {
      ElMessage.error(res.msg || `${action}打卡失败`)
    }
  } catch (error) {
    console.error('打卡失败:', error)
    ElMessage.error('打卡失败，请重试')
  } finally {
    loading.value = false
  }
}

const formatDate = (d) => d

// 加载应用数据
const loadApps = async () => {
  try {
    appsLoading.value = true
    const res = await getAppsByCategory()
    if (res.code === 200) {
      // 将分类后的数据展平为数组
      const apps = []
      const categoryData = res.data || {}
      Object.keys(categoryData).forEach(category => {
        if (categoryData[category] && Array.isArray(categoryData[category])) {
          apps.push(...categoryData[category])
        }
      })
      allApps.value = apps
    }
  } catch (error) {
    console.error('加载应用失败:', error)
  } finally {
    appsLoading.value = false
  }
}

// 加载最近使用的应用
const loadRecentApps = async () => {
  try {
    const res = await getRecentApps()
    if (res.code === 200) {
      recentApps.value = res.data || []
    }
  } catch (error) {
    console.error('加载最近使用失败:', error)
  }
}

// 加载布局配置
const loadLayout = async () => {
  try {
    const res = await getLayoutConfig()
    if (res.code === 200 && res.data) {
      savedLayout.value = JSON.parse(res.data)
    }
  } catch (error) {
    console.error('加载布局配置失败:', error)
  }
}

// 保存布局配置
const handleSaveLayout = async () => {
  try {
    // 保存当前常用应用配置
    const layoutData = {
      commonAppIds: commonApps.value.slice(0, 5).map(app => app.id),
      recentApps: recentApps.value.map(app => app.id)
    }
    await saveLayoutConfig(JSON.stringify(layoutData))
    ElMessage.success('布局已保存')
  } catch (error) {
    console.error('保存布局失败:', error)
    ElMessage.error('保存布局失败')
  }
}

// 重置布局配置
const handleResetLayout = async () => {
  try {
    await resetLayoutConfig()
    savedLayout.value = null
    ElMessage.success('布局已重置为默认')
  } catch (error) {
    console.error('重置布局失败:', error)
    ElMessage.error('重置布局失败')
  }
}

// 加载数据方法
const loadOverview = async () => {
  try {
    const res = await getOverview()
    if (res.code === 200) {
      overview.value = res.data || {}
    }
  } catch (error) {
    console.error('加载概览数据失败:', error)
  }
}

const loadTodayAttendance = async () => {
  try {
    const res = await getTodayStatus()
    if (res.code === 200) {
      todayAttendance.value = res.data || null
    }
  } catch (error) {
    console.error('加载今日考勤失败:', error)
  }
}

const loadAttendanceRecords = async () => {
  try {
    const now = new Date()
    const startDate = new Date(now.getFullYear(), now.getMonth(), 1)
    const endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0)
    const formatDateStr = (d) => d.toISOString().split('T')[0]
    const res = await getAttendanceList(formatDateStr(startDate), formatDateStr(endDate))
    if (res.code === 200) {
      attendanceRecords.value = res.data || []
    }
  } catch (error) {
    console.error('加载考勤记录失败:', error)
  }
}

const loadTodos = async () => {
  try {
    const res = await getMyTasks('PENDING')
    if (res.code === 200) {
      todos.value = (res.data || res.rows || []).map(t => ({
        id: t.id,
        title: t.title,
        deadline: t.dueDate || t.deadline || '无截止日期',
        priorityClass: t.priority === 'HIGH' ? 'high' : t.priority === 'MEDIUM' ? 'medium' : 'low'
      }))
    }
  } catch (error) {
    console.error('加载待办失败:', error)
  }
}

const loadApprovals = async () => {
  try {
    const res = await getPendingApprovals()
    if (res.code === 200) {
      approvalList.value = res.data || res.rows || []
      approvalTabs.value[0].count = approvalList.value.length
    }
  } catch (error) {
    console.error('加载审批失败:', error)
  }
}

const loadSchedules = async () => {
  try {
    const today = new Date()
    const fmt = (d) => {
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day} 00:00:00`
    }
    const start = fmt(today)
    const endDate = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000)
    const end = fmt(endDate)
    const res = await getSchedulesByRange(start, end)
    if (res.code === 200) {
      scheduleList.value = res.data || []
    }
  } catch (error) {
    console.error('加载日程失败:', error)
  }
}

// 考勤状态计算
const attendanceStatus = computed(() => {
  if (!todayAttendance.value || !todayAttendance.value.id) {
    return { text: '今日未打卡', checkedIn: false, checkedOut: false }
  }
  const att = todayAttendance.value
  if (att.checkInTime && att.checkOutTime) {
    return { text: '今日已完成打卡', checkedIn: true, checkedOut: true }
  }
  if (att.checkInTime) {
    return { text: '已上班打卡', checkedIn: true, checkedOut: false }
  }
  return { text: '今日未打卡', checkedIn: false, checkedOut: false }
})

// 更新当前时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

let timeInterval = null

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  // 加载数据
  loadOverview()
  loadApps()
  loadRecentApps()
  loadLayout()
  loadTodayAttendance()
  loadAttendanceRecords()
  loadTodos()
  loadApprovals()
  loadSchedules()
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
// 左侧二级导航栏 (优化为220px，更加宽敞)
// ============================================================================
.wb-sidebar {
  width: 220px;
  background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  box-shadow: inset -1px 0 0 0 var(--dt-border-lighter);
}

.sidebar-header {
  padding: var(--dt-spacing-xl) var(--dt-spacing-lg);
  border-bottom: 1px solid var(--dt-border-lighter);
  background: var(--dt-bg-card);
}

.sidebar-title {
  font-size: var(--dt-font-size-lg);
  font-weight: var(--dt-font-weight-bold);
  color: var(--dt-text-primary);
  margin: 0;
  letter-spacing: -0.3px;
}

// 左侧二级导航 - 钉钉风格：图标在上文字在下
.sidebar-nav {
  flex: 1;
  padding: 12px 8px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

// 导航项 - 紧凑矩形按钮
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 10px 8px;
  height: auto;
  box-sizing: border-box;
  cursor: pointer;
  position: relative;
  transition: background-color 0.2s ease;
  border-radius: 8px;
  margin: 0;

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.active {
    background: var(--dt-brand-bg);

    .nav-icon {
      color: var(--dt-brand-color);
    }

    .nav-label {
      color: var(--dt-brand-color);
      font-weight: 600;
    }
  }
}

.nav-icon {
  font-size: 20px;
  flex-shrink: 0;
  color: var(--dt-text-secondary);
  transition: color 0.2s ease;
}

.nav-label {
  flex: none;
  font-size: 11px;
  color: var(--dt-text-secondary);
  font-weight: 500;
  text-align: center;
  line-height: 1.2;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-badge {
  background: var(--dt-error-color);
  color: var(--dt-bg-card);
  font-size: var(--dt-font-size-xs);
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: var(--dt-radius-full);
  @include flex-center;
  font-weight: var(--dt-font-weight-semibold);
  box-shadow: 0 2px 4px rgba(245, 63, 63, 0.3);
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

// 视图头部 - 钉钉风格
.view-header {
  padding: 16px 24px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.view-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.view-actions {
  display: flex;
  gap: 12px;
}

// ============================================================================
// 常用应用视图
// ============================================================================
.apps-view {
  padding: var(--dt-spacing-xl);
  overflow-y: auto;
}

.wb-header {
  margin-bottom: var(--dt-spacing-xl);
  padding-bottom: var(--dt-spacing-lg);
  border-bottom: 1px solid var(--dt-border-lighter);
  @include flex-between;
  gap: var(--dt-spacing-xl);
}

.greeting-box {
  &__title {
    font-size: var(--dt-font-size-xl);
    font-weight: var(--dt-font-weight-bold);
    color: var(--dt-text-primary);
    margin: 0;
    letter-spacing: -0.5px;
  }

  &__date {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-secondary);
    margin-top: var(--dt-spacing-sm);
  }
}

.wb-search {
  position: relative;
  width: var(--dt-search-width-lg);

  &__icon {
    position: absolute;
    left: var(--dt-spacing-md);
    top: 50%;
    transform: translateY(-50%);
    color: var(--dt-text-tertiary);
    font-size: var(--dt-icon-size-lg);
  }

  &__input {
    width: 100%;
    height: var(--dt-btn-height-md);
    padding: 0 var(--dt-spacing-md) 0 var(--dt-spacing-2xl);
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-light);
    border-radius: var(--dt-radius-md);
    outline: none;
    font-size: var(--dt-font-size-sm);
    transition: background-color var(--dt-transition-base), border-color var(--dt-transition-base), box-shadow var(--dt-transition-base);

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
  margin-bottom: var(--dt-spacing-xl);

  &__header {
    @include flex-between;
    margin-bottom: var(--dt-spacing-lg);
  }

  &__title {
    font-size: var(--dt-font-size-base);
    font-weight: var(--dt-font-weight-semibold);
    color: var(--dt-text-primary);
    margin: 0;
  }
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(88px, 1fr));
  gap: var(--dt-spacing-lg);

  &--sm {
    grid-template-columns: repeat(auto-fill, minmax(72px, 1fr));
    gap: var(--dt-spacing-md);
  }
}

.app-item {
  @include flex-center;
  flex-direction: column;
  gap: var(--dt-spacing-md);
  cursor: pointer;
  padding: var(--dt-spacing-lg) var(--dt-spacing-sm);
  border-radius: var(--dt-radius-lg);
  transition: background-color var(--dt-transition-base), border-color var(--dt-transition-base);
  position: relative;
  background: var(--dt-bg-card);
  border: 1px solid transparent;

  &:hover {
    background: var(--dt-bg-card);
    border-color: var(--dt-brand-light);

    .app-item__icon {
      opacity: 0.9;
    }
  }

  &__icon {
    width: 48px;
    height: 48px;
    border-radius: var(--dt-radius-lg);
    @include flex-center;
    font-size: var(--dt-icon-size-xl);
    background: var(--dt-bg-body);
    transition: opacity var(--dt-transition-base);

    &--sm {
      width: 40px;
      height: 40px;
      font-size: var(--dt-icon-size-lg);
    }

    &.icon-orange { background: var(--dt-warning-bg); color: var(--dt-warning-color); }
    &.icon-blue { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
    &.icon-green { background: var(--dt-success-bg); color: var(--dt-success-color); }
    &.icon-purple { background: var(--dt-info-bg); color: var(--dt-info-color); }
    &.icon-pink { background: var(--dt-error-bg); color: var(--dt-error-color); }
    &.icon-teal { background: var(--dt-contacts-cyan-bg); color: var(--dt-contacts-cyan-color); }
    &.icon-indigo { background: var(--dt-brand-lighter); color: var(--dt-brand-color); }
    &.icon-cyan { background: var(--dt-contacts-cyan-bg); color: var(--dt-contacts-cyan-color); }
  }

  &__label {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-primary);
    font-weight: var(--dt-font-weight-medium);
    text-align: center;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__badge {
    position: absolute;
    top: var(--dt-spacing-sm);
    right: var(--dt-spacing-sm);
    background: var(--dt-error-color);
    color: var(--dt-bg-card);
    font-size: 10px;
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    border-radius: var(--dt-radius-full);
    @include flex-center;
    font-weight: var(--dt-font-weight-bold);
    box-shadow: 0 2px 4px rgba(245, 63, 63, 0.4);
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
  font-size: var(--dt-font-size-sm);
  position: relative;
  transition: color var(--dt-transition-fast);

  &:hover {
    color: var(--dt-brand-color);
  }

  &.active {
    color: var(--dt-brand-color);
    font-weight: var(--dt-font-weight-medium);

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
    font-size: var(--dt-font-size-xs);
    min-width: 16px;
    height: 16px;
    padding: 0 var(--dt-spacing-xs);
    border-radius: var(--dt-radius-full);
    @include flex-center;
    margin-left: var(--dt-spacing-sm);
    font-weight: var(--dt-font-weight-medium);
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
  padding: var(--dt-spacing-xl);
  overflow-y: auto;
}

.attendance-card {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  padding: var(--dt-spacing-2xl);
  text-align: center;
  box-shadow: var(--dt-shadow-2);
  margin-bottom: var(--dt-spacing-xl);
  border: 1px solid var(--dt-border-light);
}

.attendance-status {
  margin-bottom: var(--dt-spacing-xl);
}

.status-circle {
  width: 72px;
  height: 72px;
  border-radius: var(--dt-radius-full);
  background: var(--dt-bg-card);
  @include flex-center;
  margin: 0 auto var(--dt-spacing-md);
  box-shadow: var(--dt-shadow-2);

  &.status-done {
    background: var(--dt-success-bg);

    .status-icon {
      color: var(--dt-success-color);
    }
  }
}

.status-icon {
  font-size: var(--dt-icon-size-2xl);
  color: var(--dt-brand-color);
}

.status-text {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin: 0 0 var(--dt-spacing-xs);
}

.status-time {
  font-size: var(--dt-font-size-2xl);
  font-weight: var(--dt-font-weight-bold);
  color: var(--dt-brand-color);
  margin: 0;
}

.attendance-times {
  margin-top: var(--dt-spacing-md);
  display: flex;
  justify-content: center;
  gap: var(--dt-spacing-xl);

  .time-item {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-secondary);
  }
}

.attendance-actions {
  display: flex;
  gap: var(--dt-spacing-lg);
  justify-content: center;
}

.attendance-list {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-sm);
}

.attendance-item {
  display: flex;
  align-items: center;
  padding: var(--dt-spacing-md) var(--dt-spacing-lg);
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-md);
  gap: var(--dt-spacing-lg);
  border: 1px solid var(--dt-border-lighter);
  transition: border-color var(--dt-transition-fast), box-shadow var(--dt-transition-fast);

  &:hover {
    border-color: var(--dt-brand-light);
    box-shadow: var(--dt-shadow-2);
  }

  .attendance-date {
    min-width: 100px;
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-primary);
    font-weight: var(--dt-font-weight-medium);
  }

  .attendance-time {
    flex: 1;
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-secondary);
  }
}

// ============================================================================
// 待办视图
// ============================================================================
.todo-view {
  padding: 0;
}

.todo-list {
  flex: 1;
  padding: var(--dt-spacing-xl);
  overflow-y: auto;
}

.todo-stack {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-sm);
}

.todo-more {
  text-align: center;
  padding: var(--dt-spacing-md);
}

.todo-tile {
  background: var(--dt-bg-card);
  padding: var(--dt-spacing-md) var(--dt-spacing-lg);
  border-radius: var(--dt-radius-lg);
  cursor: pointer;
  transition: border-color var(--dt-transition-base), box-shadow var(--dt-transition-base);
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
  border: 1px solid var(--dt-border-lighter);

  &:hover {
    border-color: var(--dt-brand-light);
    box-shadow: var(--dt-shadow-2);
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
    font-size: var(--dt-font-size-sm);
    font-weight: var(--dt-font-weight-medium);
    color: var(--dt-text-primary);
    margin: 0;
    @include text-ellipsis;
  }

  &__meta {
    font-size: var(--dt-font-size-xs);
    color: var(--dt-text-tertiary);
    margin-top: var(--dt-spacing-xs);
    display: block;
  }
}

// ============================================================================
// 日程视图
// ============================================================================
.schedule-view {
  padding: var(--dt-spacing-xl);
  overflow-y: auto;
}

.schedule-content {
  flex: 1;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-sm);
}

.schedule-item {
  display: flex;
  align-items: center;
  padding: var(--dt-spacing-md) var(--dt-spacing-lg);
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  gap: var(--dt-spacing-lg);
  cursor: pointer;
  transition: border-color var(--dt-transition-base), box-shadow var(--dt-transition-base);
  border: 1px solid var(--dt-border-lighter);

  &:hover {
    border-color: var(--dt-brand-light);
    box-shadow: var(--dt-shadow-2);
  }

  .schedule-time {
    min-width: 100px;
    font-size: var(--dt-font-size-sm);
    color: var(--dt-brand-color);
    font-weight: var(--dt-font-weight-semibold);
    padding: var(--dt-spacing-xs) var(--dt-spacing-sm);
    background: var(--dt-brand-lighter);
    border-radius: var(--dt-radius-sm);
  }

  .schedule-info {
    flex: 1;
    min-width: 0;
  }

  .schedule-title {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-primary);
    margin: 0;
    @include text-ellipsis;
    font-weight: var(--dt-font-weight-medium);
  }

  .schedule-location {
    font-size: var(--dt-font-size-xs);
    color: var(--dt-text-tertiary);
    margin-top: var(--dt-spacing-xs);
    display: block;
  }
}

.schedule-more {
  text-align: center;
  padding: var(--dt-spacing-md);
}

// ============================================================================
// 工作报告视图
// ============================================================================
.report-view {
  padding: 0;
}

.report-list {
  flex: 1;
  padding: var(--dt-spacing-xl);
  overflow-y: auto;
}
</style>
