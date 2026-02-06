<template>
  <div class="workbench-panel">
    <!-- 顶部欢迎区 -->
    <header class="workbench-header">
      <div class="greeting-section">
        <h1 class="greeting-title">
          {{ greetingText }}，{{ displayName }}
        </h1>
        <p class="greeting-date">
          <span class="material-icons-outlined date-icon">event</span>
          {{ currentDateText }}
        </p>
      </div>
      <div class="header-actions">
        <button
          class="icon-btn"
          :class="{ loading: refreshing }"
          title="刷新数据"
          @click="refreshAllData"
        >
          <span class="material-icons-outlined">refresh</span>
        </button>
      </div>
    </header>

    <!-- 主内容区 -->
    <div
      v-loading="initialLoading"
      class="workbench-content"
      element-loading-text="加载中..."
    >
      <!-- 统计概览卡片 -->
      <section class="stats-overview-section">
        <div class="stats-overview-grid">
          <div
            class="stat-card stat-card-primary"
            @click="navigateTo('todo')"
          >
            <div class="stat-icon">
              <span class="material-icons-outlined">task_alt</span>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                {{ overviewData.todoCount || 0 }}
              </div>
              <div class="stat-label">
                待办事项
              </div>
            </div>
          </div>
          <div
            class="stat-card stat-card-success"
            @click="navigateTo('approval')"
          >
            <div class="stat-icon">
              <span class="material-icons-outlined">approval</span>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                {{ overviewData.approvalCount || 0 }}
              </div>
              <div class="stat-label">
                待审批
              </div>
            </div>
          </div>
          <div
            class="stat-card stat-card-warning"
            @click="navigateTo('mail')"
          >
            <div class="stat-icon">
              <span class="material-icons-outlined">mail</span>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                {{ overviewData.mailCount || 0 }}
              </div>
              <div class="stat-label">
                未读邮件
              </div>
            </div>
          </div>
          <div
            class="stat-card stat-card-info"
            @click="navigateTo('contacts')"
          >
            <div class="stat-icon">
              <span class="material-icons-outlined">people</span>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                {{ onlineUsersCount }}
              </div>
              <div class="stat-label">
                在线同事
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 主要内容区域 -->
      <div class="main-content-grid">
        <!-- 左侧：待办 + 审批 -->
        <div class="left-column">
          <!-- 待办事项 -->
          <div
            class="content-card todo-card"
            @click="navigateTo('todo')"
          >
            <div class="card-header">
              <h3 class="card-title">
                <span class="material-icons-outlined card-icon">task_alt</span>
                我的待办
              </h3>
              <span class="view-all">查看全部 →</span>
            </div>
            <div
              v-if="todoLoading"
              class="card-body loading"
            >
              <el-skeleton
                :rows="3"
                animated
              />
            </div>
            <div
              v-else-if="todoList.length > 0"
              class="card-body"
            >
              <div
                v-for="todo in todoList.slice(0, 5)"
                :key="todo.id"
                class="todo-item"
                :class="`priority-${getPriorityClass(todo.priority)}`"
              >
                <div class="todo-checkbox">
                  <el-checkbox
                    :model-value="todo.isCompleted"
                    @change="toggleTodoComplete(todo)"
                    @click.stop
                  />
                </div>
                <div class="todo-content">
                  <p class="todo-title">
                    {{ todo.title || todo.content || '无标题' }}
                  </p>
                  <span class="todo-meta">
                    <span
                      v-if="todo.remindTime || todo.dueDate"
                      class="todo-deadline"
                    >
                      <span class="material-icons-outlined">schedule</span>
                      {{ formatDeadline(todo.remindTime || todo.dueDate) }}
                    </span>
                  </span>
                </div>
                <span
                  v-if="todo.priority"
                  class="priority-badge"
                  :class="`priority-${getPriorityClass(todo.priority)}`"
                >
                  {{ getPriorityLabel(todo.priority) }}
                </span>
              </div>
            </div>
            <div
              v-else
              class="card-body empty"
            >
              <span class="material-icons-outlined empty-icon">check_circle</span>
              <p>暂无待办事项</p>
            </div>
          </div>

          <!-- 待审批 -->
          <div
            v-if="approvalList.length > 0"
            class="content-card approval-card"
            @click="navigateTo('approval')"
          >
            <div class="card-header">
              <h3 class="card-title">
                <span class="material-icons-outlined card-icon">approval</span>
                待我审批
              </h3>
              <span class="view-all">查看全部 →</span>
            </div>
            <div class="card-body">
              <div
                v-for="approval in approvalList.slice(0, 3)"
                :key="approval.id"
                class="approval-item"
              >
                <div class="approval-icon">
                  <span class="material-icons-outlined">description</span>
                </div>
                <div class="approval-content">
                  <p class="approval-title">
                    {{ approval.title || approval.applyType || '审批申请' }}
                  </p>
                  <span class="approval-meta">
                    <span>{{ approval.applicantName || '申请人' }}</span>
                    <span>·</span>
                    <span>{{ formatTime(approval.applyTime || approval.createTime) }}</span>
                  </span>
                </div>
                <div
                  class="approval-actions"
                  @click.stop
                >
                  <el-button
                    type="success"
                    size="small"
                    circle
                    @click="quickApprove(approval)"
                  >
                    <span class="material-icons-outlined">check</span>
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    circle
                    @click="quickReject(approval)"
                  >
                    <span class="material-icons-outlined">close</span>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 中间：日程 + 快捷操作 -->
        <div class="center-column">
          <!-- 今日日程 -->
          <div
            class="content-card schedule-card"
            @click="navigateTo('calendar')"
          >
            <div class="card-header">
              <h3 class="card-title">
                <span class="material-icons-outlined card-icon">calendar_today</span>
                今日日程
              </h3>
              <span
                class="view-all"
                @click.stop="openScheduleDialog"
              >新建 +</span>
            </div>
            <div
              v-if="scheduleList.length > 0"
              class="card-body"
            >
              <div
                v-for="event in scheduleList.slice(0, 4)"
                :key="event.id"
                class="schedule-item"
                :style="{ borderLeftColor: event.color || '#0089FF' }"
              >
                <div class="schedule-time">
                  <span class="time-start">{{ formatTime(event.startTime) }}</span>
                  <span class="time-sep">-</span>
                  <span class="time-end">{{ formatTime(event.endTime) }}</span>
                </div>
                <div class="schedule-content">
                  <p class="schedule-title">
                    {{ event.title }}
                  </p>
                  <span
                    v-if="event.location"
                    class="schedule-location"
                  >
                    <span class="material-icons-outlined">place</span>
                    {{ event.location }}
                  </span>
                </div>
              </div>
            </div>
            <div
              v-else
              class="card-body empty"
            >
              <span class="material-icons-outlined empty-icon">event_available</span>
              <p>今日暂无日程</p>
            </div>
          </div>

          <!-- 快捷操作 -->
          <div class="content-card quick-actions-card">
            <div class="card-header">
              <h3 class="card-title">
                <span class="material-icons-outlined card-icon">flash_on</span>
                快捷操作
              </h3>
            </div>
            <div class="card-body">
              <div class="quick-actions-grid">
                <div
                  class="quick-action-item"
                  @click="openNewTodo"
                >
                  <span class="material-icons-outlined">add_task</span>
                  <span>新建待办</span>
                </div>
                <div
                  class="quick-action-item"
                  @click="openScheduleDialog"
                >
                  <span class="material-icons-outlined">add_alert</span>
                  <span>新建日程</span>
                </div>
                <div
                  class="quick-action-item"
                  @click="navigateTo('meeting')"
                >
                  <span class="material-icons-outlined">videocam</span>
                  <span>发起会议</span>
                </div>
                <div
                  class="quick-action-item"
                  @click="navigateTo('mail')"
                >
                  <span class="material-icons-outlined">edit</span>
                  <span>写邮件</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：公告 + 近期会议 -->
        <div class="right-column">
          <!-- 公告通知 -->
          <div
            class="content-card announcement-card"
            @click="navigateTo('announcement')"
          >
            <div class="card-header">
              <h3 class="card-title">
                <span class="material-icons-outlined card-icon">campaign</span>
                公告通知
              </h3>
              <span class="view-all">更多 →</span>
            </div>
            <div
              v-if="announcementLoading"
              class="card-body loading"
            >
              <el-skeleton
                :rows="2"
                animated
              />
            </div>
            <div
              v-else-if="announcementList.length > 0"
              class="card-body"
            >
              <div
                v-for="item in announcementList.slice(0, 4)"
                :key="item.id"
                class="announcement-item"
              >
                <span
                  class="announcement-tag"
                  :class="`tag-${getTypeClass(item.type || item.announcementType)}`"
                >
                  {{ getTypeLabel(item.type || item.announcementType) }}
                </span>
                <p class="announcement-title">
                  {{ item.title }}
                </p>
                <span class="announcement-time">{{ formatTime(item.createTime || item.publishTime) }}</span>
              </div>
            </div>
            <div
              v-else
              class="card-body empty"
            >
              <span class="material-icons-outlined empty-icon">campaign</span>
              <p>暂无公告</p>
            </div>
          </div>

          <!-- 近期会议 -->
          <div
            v-if="meetingList.length > 0"
            class="content-card meeting-card"
            @click="navigateTo('meeting')"
          >
            <div class="card-header">
              <h3 class="card-title">
                <span class="material-icons-outlined card-icon">meeting_room</span>
                近期会议
              </h3>
              <span class="view-all">全部 →</span>
            </div>
            <div class="card-body">
              <div
                v-for="meeting in meetingList.slice(0, 3)"
                :key="meeting.id"
                class="meeting-item"
              >
                <div class="meeting-time">
                  <span class="material-icons-outlined">schedule</span>
                  {{ formatDate(meeting.scheduledStartTime || meeting.startTime) }}
                </div>
                <p class="meeting-title">
                  {{ meeting.title }}
                </p>
                <span class="meeting-participants">
                  <span class="material-icons-outlined">people</span>
                  {{ meeting.participantCount || 0 }}人
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建待办弹窗 -->
    <el-dialog
      v-model="showTodoDialog"
      title="新建待办"
      width="500px"
    >
      <el-form
        :model="newTodo"
        label-width="80px"
      >
        <el-form-item label="标题">
          <el-input
            v-model="newTodo.title"
            placeholder="请输入待办标题"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="newTodo.priority">
            <el-radio :label="1">
              低
            </el-radio>
            <el-radio :label="2">
              中
            </el-radio>
            <el-radio :label="3">
              高
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="newTodo.dueDate"
            type="datetime"
            placeholder="选择截止时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTodoDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="todoSubmitting"
          @click="createNewTodo"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 新建日程弹窗 -->
    <el-dialog
      v-model="showScheduleDialog"
      title="新建日程"
      width="500px"
    >
      <el-form
        :model="newSchedule"
        label-width="80px"
      >
        <el-form-item label="标题">
          <el-input
            v-model="newSchedule.title"
            placeholder="请输入日程标题"
          />
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="scheduleTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="地点">
          <el-input
            v-model="newSchedule.location"
            placeholder="会议地点（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showScheduleDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="scheduleSubmitting"
          @click="createNewSchedule"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 导入 API
import { getTodoList, getTodoCount, completeTodo as completeTodoApi, deleteTodo } from '@/api/im/todo'
import { getPendingApprovals, handleApproval as handleApprovalApi } from '@/api/im/approval'
import { getLatestAnnouncements } from '@/api/im/announcement'
import { getUnreadCount } from '@/api/im/mail'
import { getEventsByTimeRange, createEvent } from '@/api/im/schedule'
import { getMeetingList } from '@/api/im/meeting'
import { getTodos, getOverview, getStatistics, createTodo as createTodoApi } from '@/api/im/workbench'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const store = useStore()

// ============================================================================
// 状态管理
// ============================================================================
const initialLoading = ref(true)
const refreshing = ref(false)
const refreshTimer = ref(null)

// 弹窗状态
const showTodoDialog = ref(false)
const showScheduleDialog = ref(false)
const todoSubmitting = ref(false)
const scheduleSubmitting = ref(false)

// 新建表单
const newTodo = ref({
  title: '',
  priority: 2,
  dueDate: null
})

const newSchedule = ref({
  title: '',
  location: ''
})

const scheduleTimeRange = ref([])

// ============================================================================
// 数据状态
// ============================================================================
const todoLoading = ref(false)
const announcementLoading = ref(false)

const overviewData = ref({
  todoCount: 0,
  approvalCount: 0,
  mailCount: 0,
  onlineCount: 0
})

const todoList = ref([])
const approvalList = ref([])
const announcementList = ref([])
const scheduleList = ref([])
const meetingList = ref([])

// ============================================================================
// 用户信息
// ============================================================================
const currentUser = computed(() => store.getters['user/currentUser'] || {})
const displayName = computed(() => currentUser.value.nickname || currentUser.value.username || '用户')

// 在线用户数
const onlineUsersCount = computed(() => {
  const onlineUsers = store.getters['contact/onlineUsers'] || []
  return onlineUsers.length
})

// 问候语
const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) { return '凌晨好' }
  if (hour < 9) { return '早上好' }
  if (hour < 12) { return '上午好' }
  if (hour < 14) { return '中午好' }
  if (hour < 18) { return '下午好' }
  if (hour < 22) { return '晚上好' }
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
// 数据加载函数
// ============================================================================

// 加载概览数据
const loadOverviewData = async () => {
  try {
    const res = await getOverview()
    if (res.code === 200 && res.data) {
      overviewData.value = {
        todoCount: res.data.todoCount || 0,
        approvalCount: res.data.approvalCount || 0,
        mailCount: res.data.mailCount || 0,
        onlineCount: res.data.onlineCount || 0
      }
    }
  } catch (error) {
    console.error('加载概览数据失败:', error)
    // 使用备用数据源
    await loadIndividualStats()
  }
}

// 加载各项统计（备用）
const loadIndividualStats = async () => {
  try {
    // 待办数量
    const todoRes = await getTodoCount()
    if (todoRes.code === 200) {
      overviewData.value.todoCount = todoRes.data || 0
    }

    // 邮件数量
    const mailRes = await getUnreadCount()
    if (mailRes.code === 200) {
      overviewData.value.mailCount = mailRes.data || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载待办列表
const loadTodoData = async () => {
  todoLoading.value = true
  try {
    const res = await getTodoList('TODO')
    if (res.code === 200 && res.data) {
      todoList.value = (Array.isArray(res.data) ? res.data : (res.data.list || res.data.records || []))
        .map(item => ({
          ...item,
          priority: item.priority || 2,
          isCompleted: item.isCompleted !== false
        }))
        .sort((a, b) => {
          if (a.isCompleted !== b.isCompleted) { return a.isCompleted ? 1 : -1 }
          return (b.priority || 2) - (a.priority || 2)
        })
    }
  } catch (error) {
    console.error('加载待办失败:', error)
    todoList.value = []
  } finally {
    todoLoading.value = false
  }
}

// 加载审批列表
const loadApprovalData = async () => {
  try {
    const res = await getPendingApprovals()
    if (res.code === 200 && res.data) {
      approvalList.value = Array.isArray(res.data) ? res.data : (res.data.list || res.data.records || [])
      overviewData.value.approvalCount = approvalList.value.length
    }
  } catch (error) {
    console.error('加载审批失败:', error)
    approvalList.value = []
  }
}

// 加载公告列表
const loadAnnouncementData = async () => {
  announcementLoading.value = true
  try {
    const res = await getLatestAnnouncements(5)
    if (res.code === 200 && res.data) {
      announcementList.value = Array.isArray(res.data) ? res.data : (res.data.list || res.data.records || [])
    }
  } catch (error) {
    console.error('加载公告失败:', error)
    announcementList.value = []
  } finally {
    announcementLoading.value = false
  }
}

// 加载今日日程
const loadScheduleData = async () => {
  try {
    const today = dayjs().startOf('day')
    const startTime = today.format('YYYY-MM-DD HH:mm:ss')
    const endTime = today.add(1, 'day').format('YYYY-MM-DD HH:mm:ss')

    const res = await getEventsByTimeRange(startTime, endTime)
    if (res.code === 200 && res.data) {
      scheduleList.value = Array.isArray(res.data) ? res.data : (res.data.list || res.data.records || [])
    }
  } catch (error) {
    console.error('加载日程失败:', error)
    scheduleList.value = []
  }
}

// 加载会议列表
const loadMeetingData = async () => {
  try {
    const res = await getMeetingList('SCHEDULED')
    if (res.code === 200 && res.data) {
      const list = Array.isArray(res.data) ? res.data : (res.data.list || res.data.records || [])
      // 只显示未来7天的会议
      const now = dayjs()
      meetingList.value = list
        .filter(m => {
          const meetingTime = dayjs(m.scheduledStartTime || m.startTime)
          return meetingTime.isBefore(now.add(7, 'day')) && meetingTime.isAfter(now.subtract(1, 'hour'))
        })
        .sort((a, b) => dayjs(a.scheduledStartTime || a.startTime).diff(dayjs(b.scheduledStartTime || b.startTime)))
    }
  } catch (error) {
    console.error('加载会议失败:', error)
    meetingList.value = []
  }
}

// 加载所有数据
const loadAllData = async () => {
  await Promise.all([
    loadOverviewData(),
    loadTodoData(),
    loadApprovalData(),
    loadAnnouncementData(),
    loadScheduleData(),
    loadMeetingData()
  ])
}

// 刷新所有数据
const refreshAllData = async () => {
  refreshing.value = true
  try {
    await loadAllData()
    ElMessage.success('数据已刷新')
  } catch (error) {
    ElMessage.error('刷新失败')
  } finally {
    refreshing.value = false
  }
}

// ============================================================================
// 格式化函数
// ============================================================================

const formatTime = time => {
  if (!time) { return '' }
  return dayjs(time).fromNow()
}

const formatDate = time => {
  if (!time) { return '' }
  const date = dayjs(time)
  const now = dayjs()
  const diffDays = date.diff(now, 'day')

  if (diffDays === 0) {
    // 今天显示时间
    return date.format('HH:mm')
  } else if (diffDays === 1) {
    return '明天 ' + date.format('HH:mm')
  } else if (diffDays < 7) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return weekdays[date.day()] + ' ' + date.format('HH:mm')
  } else {
    return date.format('MM-DD HH:mm')
  }
}

const formatDeadline = time => {
  if (!time) { return '' }
  const date = dayjs(time)
  const now = dayjs()
  const diffDays = date.diff(now, 'day')

  if (diffDays < 0) { return '已逾期' }
  if (diffDays === 0) { return '今天' }
  if (diffDays === 1) { return '明天' }
  if (diffDays < 7) { return `${diffDays}天后` }
  return date.format('MM-DD')
}

const getPriorityClass = priority => {
  const p = parseInt(priority) || 2
  if (p >= 3) { return 'high' }
  if (p <= 1) { return 'low' }
  return 'medium'
}

const getPriorityLabel = priority => {
  const p = parseInt(priority) || 2
  if (p >= 3) { return '高' }
  if (p <= 1) { return '低' }
  return '中'
}

const getTypeClass = type => {
  const t = (type || '').toUpperCase()
  if (t.includes('重要') || t === 'HIGH' || t === 'URGENT') { return 'important' }
  if (t.includes('通知') || t === 'NOTICE') { return 'notice' }
  return 'normal'
}

const getTypeLabel = type => {
  const t = (type || '').toUpperCase()
  if (t.includes('重要') || t === 'HIGH' || t === 'URGENT') { return '重要' }
  if (t.includes('系统') || t === 'SYSTEM') { return '系统' }
  if (t.includes('活动') || t === 'ACTIVITY') { return '活动' }
  return '通知'
}

// ============================================================================
// 操作处理
// ============================================================================

// 切换待办完成状态
const toggleTodoComplete = async todo => {
  try {
    if (todo.isCompleted) {
      await completeTodoApi(todo.id || todo.markerId)
      ElMessage.success('待办已完成')
    } else {
      await completeTodoApi(todo.id || todo.markerId)
      ElMessage.success('待办已重启')
    }
    await loadTodoData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 快速审批
const quickApprove = async approval => {
  try {
    await ElMessageBox.confirm('确认通过该审批吗？', '提示', { type: 'info' })
    await handleApprovalApi({
      approvalId: approval.id,
      action: 'APPROVE',
      comment: '同意'
    })
    ElMessage.success('已通过')
    await loadApprovalData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const quickReject = async approval => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回理由', '驳回审批', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入驳回理由'
    })
    await handleApprovalApi({
      approvalId: approval.id,
      action: 'REJECT',
      comment: value
    })
    ElMessage.success('已驳回')
    await loadApprovalData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 打开新建待办
const openNewTodo = () => {
  newTodo.value = {
    title: '',
    priority: 2,
    dueDate: new Date()
  }
  showTodoDialog.value = true
}

// 创建待办
const createNewTodo = async () => {
  if (!newTodo.value.title) {
    ElMessage.warning('请输入待办标题')
    return
  }
  todoSubmitting.value = true
  try {
    await createTodoApi({
      title: newTodo.value.title,
      priority: newTodo.value.priority,
      dueDate: newTodo.value.dueDate ? dayjs(newTodo.value.dueDate).format('YYYY-MM-DD HH:mm:ss') : null
    })
    ElMessage.success('待办创建成功')
    showTodoDialog.value = false
    await loadTodoData()
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    todoSubmitting.value = false
  }
}

// 打开新建日程
const openScheduleDialog = () => {
  newSchedule.value = {
    title: '',
    location: ''
  }
  const now = dayjs()
  scheduleTimeRange.value = [
    now.hour(now.hour() + 1).minute(0).toDate(),
    now.hour(now.hour() + 2).minute(0).toDate()
  ]
  showScheduleDialog.value = true
}

// 创建日程
const createNewSchedule = async () => {
  if (!newSchedule.value.title) {
    ElMessage.warning('请输入日程标题')
    return
  }
  if (!scheduleTimeRange.value || scheduleTimeRange.value.length !== 2) {
    ElMessage.warning('请选择时间')
    return
  }
  scheduleSubmitting.value = true
  try {
    await createEvent({
      title: newSchedule.value.title,
      startTime: dayjs(scheduleTimeRange.value[0]).format('YYYY-MM-DD HH:mm:ss'),
      endTime: dayjs(scheduleTimeRange.value[1]).format('YYYY-MM-DD HH:mm:ss'),
      location: newSchedule.value.location
    })
    ElMessage.success('日程创建成功')
    showScheduleDialog.value = false
    await loadScheduleData()
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    scheduleSubmitting.value = false
  }
}

// 导航处理
const navigateTo = tab => {
  window.dispatchEvent(new CustomEvent('switch-tab', { detail: tab }))
}

// ============================================================================
// 生命周期
// ============================================================================
onMounted(async () => {
  await loadAllData()
  initialLoading.value = false

  // 定时刷新（每2分钟）
  refreshTimer.value = setInterval(() => {
    loadTodoData()
    loadApprovalData()
    loadAnnouncementData()
  }, 2 * 60 * 1000)
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

  &::before {
    content: '';
    position: absolute;
    right: -50px;
    top: -100px;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(0, 137, 255, 0.06) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }
}

.greeting-section {
  position: relative;
}

.greeting-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.greeting-date {
  font-size: 13px;
  color: var(--dt-text-secondary);
  margin: 4px 0 0 0;
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
  gap: 12px;
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
  transition: all 0.2s;

  &:hover {
    border-color: var(--dt-brand-color);
    color: var(--dt-brand-color);
  }

  &.loading {
    pointer-events: none;
    opacity: 0.6;

    .material-icons-outlined {
      animation: spin 1s linear infinite;
    }
  }
}

// ============================================================================
// 主内容区
// ============================================================================
.workbench-content {
  flex: 1;
  padding: 20px 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: var(--dt-radius-sm);
  }
}

// ============================================================================
// 统计概览卡片
// ============================================================================
.stats-overview-section {
  margin-bottom: 8px;
}

.stats-overview-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-lg);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--dt-radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;

    .material-icons-outlined {
      font-size: 24px;
      color: #fff;
    }
  }

  .stat-content {
    flex: 1;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 700;
    line-height: 1;
    margin-bottom: 4px;
  }

  .stat-label {
    font-size: 13px;
    color: var(--dt-text-secondary);
  }

  &.stat-card-primary .stat-icon {
    background: linear-gradient(135deg, #0089FF 0%, #0958d9 100%);
  }

  &.stat-card-primary .stat-value {
    color: #0089FF;
  }

  &.stat-card-success .stat-icon {
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  }

  &.stat-card-success .stat-value {
    color: #52c41a;
  }

  &.stat-card-warning .stat-icon {
    background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
  }

  &.stat-card-warning .stat-value {
    color: #fa8c16;
  }

  &.stat-card-info .stat-icon {
    background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
  }

  &.stat-card-info .stat-value {
    color: #722ed1;
  }
}

// ============================================================================
// 主内容网格布局
// ============================================================================
.main-content-grid {
  display: grid;
  grid-template-columns: 1fr 320px 280px;
  gap: 20px;
  align-items: start;
}

.left-column,
.center-column,
.right-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// ============================================================================
// 内容卡片
// ============================================================================
.content-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid var(--dt-border-light);
  }

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;

    .card-icon {
      font-size: 18px;
      color: var(--dt-brand-color);
    }
  }

  .view-all {
    font-size: 12px;
    color: var(--dt-text-link);
    cursor: pointer;
    white-space: nowrap;

    &:hover {
      text-decoration: underline;
    }
  }

  .card-body {
    padding: 16px 20px;
    min-height: 120px;

    &.loading {
      display: flex;
      align-items: center;
    }

    &.empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: var(--dt-text-quaternary);
      min-height: 120px;

      .empty-icon {
        font-size: 36px;
        margin-bottom: 8px;
      }

      p {
        font-size: 13px;
        margin: 0;
      }
    }
  }
}

// ============================================================================
// 待办卡片
// ============================================================================
.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  margin-bottom: 8px;
  transition: all 0.2s;

  &:last-child {
    margin-bottom: 0;
  }

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.priority-high {
    border-left: 3px solid #ff4d4f;
  }

  &.priority-medium {
    border-left: 3px solid #faad14;
  }

  &.priority-low {
    border-left: 3px solid var(--dt-brand-color);
  }

  .todo-checkbox {
    flex-shrink: 0;
  }

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

  .todo-meta {
    display: flex;
    gap: 12px;
  }

  .todo-deadline {
    display: flex;
    align-items: center;
    gap: 3px;
    font-size: 12px;
    color: var(--dt-text-quaternary);

    .material-icons-outlined {
      font-size: 14px;
    }
  }

  .priority-badge {
    padding: 2px 6px;
    font-size: 11px;
    border-radius: var(--dt-radius-sm);
    flex-shrink: 0;

    &.priority-high {
      background: #fff1f0;
      color: #ff4d4f;
    }

    &.priority-medium {
      background: #fffbe6;
      color: #faad14;
    }

    &.priority-low {
      background: #e6f7ff;
      color: #0089FF;
    }
  }
}

// ============================================================================
// 审批卡片
// ============================================================================
.approval-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  margin-bottom: 8px;

  &:last-child {
    margin-bottom: 0;
  }

  .approval-icon {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: var(--dt-brand-bg);
    display: flex;
    align-items: center;
    justify-content: center;

    .material-icons-outlined {
      font-size: 16px;
      color: var(--dt-brand-color);
    }
  }

  .approval-content {
    flex: 1;
    min-width: 0;
  }

  .approval-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);
    margin: 0 0 4px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .approval-meta {
    display: flex;
    gap: 8px;
    font-size: 12px;
    color: var(--dt-text-quaternary);
  }

  .approval-actions {
    display: flex;
    gap: 4px;
    flex-shrink: 0;

    .el-button {
      width: 28px;
      height: 28px;
      padding: 0;

      .material-icons-outlined {
        font-size: 14px;
      }
    }
  }
}

// ============================================================================
// 日程卡片
// ============================================================================
.schedule-item {
  display: flex;
  gap: 12px;
  padding: 10px 12px;
  border-left: 3px solid transparent;
  border-radius: var(--dt-radius-md);
  margin-bottom: 8px;
  background: var(--dt-bg-input);

  &:last-child {
    margin-bottom: 0;
  }

  .schedule-time {
    flex-shrink: 0;
    font-size: 12px;
    font-weight: 600;
    color: var(--dt-brand-color);
    padding: 4px 8px;
    background: var(--dt-brand-bg);
    border-radius: var(--dt-radius-sm);
    white-space: nowrap;
  }

  .schedule-content {
    flex: 1;
    min-width: 0;
  }

  .schedule-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);
    margin: 0 0 4px 0;
  }

  .schedule-location {
    display: flex;
    align-items: center;
    gap: 3px;
    font-size: 12px;
    color: var(--dt-text-quaternary);

    .material-icons-outlined {
      font-size: 14px;
    }
  }
}

// ============================================================================
// 快捷操作卡片
// ============================================================================
.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-brand-bg);
    border-color: var(--dt-brand-color);

    .material-icons-outlined {
      color: var(--dt-brand-color);
    }
  }

  .material-icons-outlined {
    font-size: 20px;
    color: var(--dt-text-secondary);
  }

  span:last-child {
    font-size: 12px;
    color: var(--dt-text-primary);
  }
}

// ============================================================================
// 公告卡片
// ============================================================================
.announcement-item {
  padding: 12px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  margin-bottom: 8px;

  &:last-child {
    margin-bottom: 0;
  }

  .announcement-tag {
    display: inline-block;
    padding: 2px 6px;
    font-size: 10px;
    font-weight: 600;
    border-radius: var(--dt-radius-sm);
    margin-bottom: 6px;

    &.tag-important {
      background: #fff1f0;
      color: #ff4d4f;
    }

    &.tag-notice {
      background: #e6f7ff;
      color: #0089FF;
    }

    &.tag-normal {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
    }
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

  .announcement-time {
    font-size: 11px;
    color: var(--dt-text-quaternary);
  }
}

// ============================================================================
// 会议卡片
// ============================================================================
.meeting-item {
  padding: 12px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  margin-bottom: 8px;

  &:last-child {
    margin-bottom: 0;
  }

  .meeting-time {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: var(--dt-brand-color);
    margin-bottom: 6px;

    .material-icons-outlined {
      font-size: 14px;
    }
  }

  .meeting-title {
    font-size: 13px;
    font-weight: 500;
    color: var(--dt-text-primary);
    margin: 0 0 6px 0;
  }

  .meeting-participants {
    display: flex;
    align-items: center;
    gap: 3px;
    font-size: 11px;
    color: var(--dt-text-quaternary);

    .material-icons-outlined {
      font-size: 14px;
    }
  }
}

// ============================================================================
// 响应式
// ============================================================================
@media (max-width: 1400px) {
  .main-content-grid {
    grid-template-columns: 1fr 280px 260px;
  }

  .stats-overview-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }

  .stat-card {
    padding: 16px;
    gap: 12px;

    .stat-icon {
      width: 40px;
      height: 40px;

      .material-icons-outlined {
        font-size: 20px;
      }
    }

    .stat-value {
      font-size: 24px;
    }
  }
}


// ============================================================================
// 暗色模式
// ============================================================================
.dark {
  .workbench-header {
    background: linear-gradient(135deg, #0c4a6e 0%, #075985 50%, #0f172a 100%);

    &::before {
      background: radial-gradient(circle, rgba(56, 189, 248, 0.1) 0%, transparent 70%);
    }
  }

  .icon-btn {
    background: var(--dt-bg-card);
    border-color: var(--dt-border-dark);
  }

  .stat-card,
  .content-card {
    background: var(--dt-bg-card);
    border-color: var(--dt-border-dark);
  }

  .todo-item,
  .approval-item,
  .schedule-item,
  .quick-action-item,
  .announcement-item,
  .meeting-item {
    background: var(--dt-bg-input);
    border-color: var(--dt-border-dark);
  }
}
</style>
