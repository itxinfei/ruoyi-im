<template>
  <div class="calendar-panel">
    <!-- 左侧边栏 -->
    <aside class="calendar-sidebar">
      <div class="sidebar-header">
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          新建日程
        </el-button>
      </div>

      <!-- 迷你日历 -->
      <div class="mini-calendar">
        <div class="mini-calendar-header">
          <el-button text circle size="small" @click="prevMonth">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <span class="mini-calendar-title">{{ miniCalendarTitle }}</span>
          <el-button text circle size="small" @click="nextMonth">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="mini-calendar-grid">
          <div class="mini-weekdays">
            <span v-for="d in weekDays" :key="d" class="mini-weekday">{{ d }}</span>
          </div>
          <div class="mini-days">
            <span
              v-for="day in miniCalendarDays"
              :key="day.key"
              class="mini-day"
              :class="{
                'other-month': day.otherMonth,
                'today': day.isToday,
                'selected': day.date === selectedDateStr,
                'has-event': day.hasEvent
              }"
              @click="selectDate(day)"
            >
              {{ day.day }}
            </span>
          </div>
        </div>
      </div>

      <!-- 视图切换 -->
      <div class="view-switch">
        <el-radio-group v-model="currentView" size="small">
          <el-radio-button value="month">月</el-radio-button>
          <el-radio-button value="week">周</el-radio-button>
          <el-radio-button value="day">日</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 日程分类筛选 -->
      <div class="calendar-filters">
        <div class="filter-title">日程分类</div>
        <el-checkbox-group v-model="selectedCategories" class="filter-list">
          <el-checkbox value="work">
            <span class="filter-dot work" />工作
          </el-checkbox>
          <el-checkbox value="meeting">
            <span class="filter-dot meeting" />会议
          </el-checkbox>
          <el-checkbox value="personal">
            <span class="filter-dot personal" />个人
          </el-checkbox>
          <el-checkbox value="reminder">
            <span class="filter-dot reminder" />提醒
          </el-checkbox>
        </el-checkbox-group>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="calendar-main">
      <!-- 工具栏 -->
      <header class="calendar-toolbar">
        <div class="toolbar-left">
          <el-button text @click="goToday">今天</el-button>
          <el-button-group>
            <el-button @click="prevPeriod">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <el-button @click="nextPeriod">
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </el-button-group>
          <span class="current-period">{{ periodTitle }}</span>
        </div>
        <div class="toolbar-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索日程"
            prefix-icon="Search"
            clearable
            size="small"
            style="width: 200px"
          />
        </div>
      </header>

      <!-- 视图内容 -->
      <div class="calendar-content">
        <!-- 月视图 -->
        <template v-if="currentView === 'month'">
          <div class="month-view">
            <div class="month-header">
              <div v-for="d in weekDays" :key="d" class="month-header-cell">{{ d }}</div>
            </div>
            <div class="month-body">
              <div
                v-for="week in monthWeeks"
                :key="week.key"
                class="month-week"
              >
                <div
                  v-for="day in week.days"
                  :key="day.date"
                  class="month-day"
                  :class="{
                    'other-month': day.otherMonth,
                    'today': day.isToday,
                    'selected': day.date === selectedDateStr
                  }"
                  @click="selectDate(day)"
                >
                  <div class="day-header">
                    <span class="day-number">{{ day.day }}</span>
                  </div>
                  <div class="day-events">
                    <div
                      v-for="event in day.events.slice(0, 3)"
                      :key="event.id"
                      class="day-event"
                      :class="event.category"
                      @click.stop="openEventDetail(event)"
                    >
                      {{ event.title }}
                    </div>
                    <div v-if="day.events.length > 3" class="more-events">
                      +{{ day.events.length - 3 }} 更多
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 周视图 -->
        <template v-else-if="currentView === 'week'">
          <div class="week-view">
            <div class="week-header">
              <div class="time-gutter" />
              <div
                v-for="day in weekDaysData"
                :key="day.date"
                class="week-header-cell"
                :class="{ 'today': day.isToday }"
              >
                <div class="weekday">{{ day.weekDay }}</div>
                <div class="day-num">{{ day.day }}</div>
              </div>
            </div>
            <div class="week-body">
              <div class="time-axis">
                <div v-for="hour in 24" :key="hour" class="time-slot">
                  {{ String(hour - 1).padStart(2, '0') }}:00
                </div>
              </div>
              <div class="week-grid">
                <div
                  v-for="day in weekDaysData"
                  :key="day.date"
                  class="day-column"
                  :class="{ 'today': day.isToday }"
                >
                  <div v-for="hour in 24" :key="hour" class="hour-cell" />
                  <div
                    v-for="event in getEventsForDay(day.date)"
                    :key="event.id"
                    class="week-event"
                    :class="event.category"
                    :style="getEventStyle(event)"
                    @click="openEventDetail(event)"
                  >
                    <div class="event-time">{{ formatEventTime(event) }}</div>
                    <div class="event-title">{{ event.title }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 日视图 -->
        <template v-else-if="currentView === 'day'">
          <div class="day-view">
            <div class="day-header">
              <div class="time-gutter" />
              <div class="day-header-cell" :class="{ 'today': isToday(selectedDate) }">
                <div class="weekday">{{ selectedWeekDay }}</div>
                <div class="day-num">{{ selectedDate.getDate() }}</div>
              </div>
            </div>
            <div class="day-body">
              <div class="time-axis">
                <div v-for="hour in 24" :key="hour" class="time-slot">
                  {{ String(hour - 1).padStart(2, '0') }}:00
                </div>
              </div>
              <div class="day-grid">
                <div v-for="hour in 24" :key="hour" class="hour-cell" />
                <div
                  v-for="event in selectedDayEvents"
                  :key="event.id"
                  class="day-event-block"
                  :class="event.category"
                  :style="getEventStyle(event)"
                  @click="openEventDetail(event)"
                >
                  <div class="event-time">{{ formatEventTime(event) }}</div>
                  <div class="event-title">{{ event.title }}</div>
                  <div v-if="event.location" class="event-location">
                    <el-icon><Location /></el-icon>
                    {{ event.location }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </main>

    <!-- 创建/编辑日程弹窗 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingEvent ? '编辑日程' : '新建日程'"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入日程标题" />
        </el-form-item>
        <el-form-item label="时间" prop="timeRange">
          <el-date-picker
            v-model="formData.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="全天">
          <el-switch v-model="formData.allDay" />
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input v-model="formData.location" placeholder="请输入地点（选填）" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="formData.category" placeholder="选择分类" style="width: 100%">
            <el-option value="work" label="工作" />
            <el-option value="meeting" label="会议" />
            <el-option value="personal" label="个人" />
            <el-option value="reminder" label="提醒" />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒" prop="reminder">
          <el-select v-model="formData.reminder" placeholder="选择提醒时间" style="width: 100%">
            <el-option value="none" label="不提醒" />
            <el-option value="0" label="开始时" />
            <el-option value="5" label="5分钟前" />
            <el-option value="15" label="15分钟前" />
            <el-option value="30" label="30分钟前" />
            <el-option value="60" label="1小时前" />
            <el-option value="1440" label="1天前" />
          </el-select>
        </el-form-item>
        <el-form-item label="参与人" prop="participants">
          <el-select
            v-model="formData.participants"
            multiple
            filterable
            placeholder="选择参与人"
            style="width: 100%"
          >
            <el-option
              v-for="user in availableUsers"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入日程描述（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button v-if="editingEvent" type="danger" text @click="handleDelete">删除</el-button>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ editingEvent ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 日程详情弹窗 -->
    <el-dialog
      v-model="showDetailDialog"
      title="日程详情"
      width="480px"
      destroy-on-close
    >
      <div v-if="currentEvent" class="event-detail">
        <div class="detail-header">
          <div class="detail-category" :class="currentEvent.category" />
          <h3 class="detail-title">{{ currentEvent.title }}</h3>
        </div>
        <div class="detail-info">
          <div class="info-row">
            <el-icon><Clock /></el-icon>
            <span>{{ formatEventTimeFull(currentEvent) }}</span>
          </div>
          <div v-if="currentEvent.location" class="info-row">
            <el-icon><Location /></el-icon>
            <span>{{ currentEvent.location }}</span>
          </div>
          <div v-if="currentEvent.description" class="info-row">
            <el-icon><Document /></el-icon>
            <span>{{ currentEvent.description }}</span>
          </div>
          <div v-if="currentEvent.participants && currentEvent.participants.length" class="info-row">
            <el-icon><User /></el-icon>
            <span>{{ currentEvent.participants.length }} 人参与</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="openEditDialog">编辑</el-button>
        <el-button type="primary" @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, ArrowLeft, ArrowRight, Clock, Location, Document, User
} from '@element-plus/icons-vue'
import {
  createSchedule,
  updateSchedule,
  deleteSchedule,
  getSchedulesByRange
} from '@/api/im/schedule'

// 状态
const currentView = ref('month')
const currentDate = ref(new Date())
const selectedDate = ref(new Date())
const events = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const editingEvent = ref(null)
const currentEvent = ref(null)
const submitting = ref(false)
const searchKeyword = ref('')
const selectedCategories = ref(['work', 'meeting', 'personal', 'reminder'])
const formRef = ref(null)

const weekDays = ['日', '一', '二', '三', '四', '五', '六']

// 可用用户列表（模拟数据）
const availableUsers = ref([
  { id: 1, name: '张三' },
  { id: 2, name: '李四' },
  { id: 3, name: '王五' }
])

// 表单数据
const formData = ref({
  title: '',
  timeRange: [],
  allDay: false,
  location: '',
  category: 'work',
  reminder: '15',
  participants: [],
  description: ''
})

const formRules = {
  title: [{ required: true, message: '请输入日程标题', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择时间', trigger: 'change' }]
}

// 计算属性
const miniCalendarTitle = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth() + 1
  return `${year}年${month}月`
})

const periodTitle = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth() + 1
  if (currentView.value === 'month') {
    return `${year}年${month}月`
  } else if (currentView.value === 'week') {
    const weekStart = getWeekStart(currentDate.value)
    const weekEnd = new Date(weekStart)
    weekEnd.setDate(weekEnd.getDate() + 6)
    return `${weekStart.getMonth() + 1}月${weekStart.getDate()}日 - ${weekEnd.getMonth() + 1}月${weekEnd.getDate()}日`
  } else {
    return `${year}年${month}月${currentDate.value.getDate()}日`
  }
})

const selectedDateStr = computed(() => formatDate(selectedDate.value))

const selectedWeekDay = computed(() => {
  return '周' + weekDays[selectedDate.value.getDay()]
})

const miniCalendarDays = computed(() => {
  return generateMiniCalendarDays()
})

const monthWeeks = computed(() => {
  return generateMonthWeeks()
})

const weekDaysData = computed(() => {
  return generateWeekDaysData()
})

const selectedDayEvents = computed(() => {
  const dateStr = formatDate(selectedDate.value)
  return events.value.filter(e => e.date === dateStr)
})

// 工具函数
function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatDateTime(date) {
  if (!date) return ''
  const d = new Date(date)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function isToday(date) {
  const today = new Date()
  return formatDate(date) === formatDate(today)
}

function getWeekStart(date) {
  const d = new Date(date)
  const day = d.getDay()
  d.setDate(d.getDate() - day)
  return d
}

function generateMiniCalendarDays() {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const startDay = firstDay.getDay()
  const daysInMonth = lastDay.getDate()

  const days = []
  const todayStr = formatDate(new Date())

  // 上月填充
  const prevMonthLastDay = new Date(year, month, 0).getDate()
  for (let i = startDay - 1; i >= 0; i--) {
    const day = prevMonthLastDay - i
    const date = new Date(year, month - 1, day)
    days.push({
      key: `prev-${day}`,
      day,
      date: formatDate(date),
      otherMonth: true,
      isToday: false,
      hasEvent: hasEventOnDate(formatDate(date))
    })
  }

  // 当月
  for (let i = 1; i <= daysInMonth; i++) {
    const date = new Date(year, month, i)
    const dateStr = formatDate(date)
    days.push({
      key: `curr-${i}`,
      day: i,
      date: dateStr,
      otherMonth: false,
      isToday: dateStr === todayStr,
      hasEvent: hasEventOnDate(dateStr)
    })
  }

  // 下月填充
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    const date = new Date(year, month + 1, i)
    days.push({
      key: `next-${i}`,
      day: i,
      date: formatDate(date),
      otherMonth: true,
      isToday: false,
      hasEvent: hasEventOnDate(formatDate(date))
    })
  }

  return days
}

function generateMonthWeeks() {
  const days = generateMiniCalendarDays()
  const weeks = []
  for (let i = 0; i < days.length; i += 7) {
    const weekDays = days.slice(i, i + 7).map(d => ({
      ...d,
      events: getEventsForDate(d.date)
    }))
    weeks.push({
      key: `week-${i / 7}`,
      days: weekDays
    })
  }
  return weeks
}

function generateWeekDaysData() {
  const weekStart = getWeekStart(currentDate.value)
  const days = []
  const todayStr = formatDate(new Date())

  for (let i = 0; i < 7; i++) {
    const d = new Date(weekStart)
    d.setDate(d.getDate() + i)
    days.push({
      date: formatDate(d),
      day: d.getDate(),
      weekDay: weekDays[d.getDay()],
      isToday: formatDate(d) === todayStr
    })
  }
  return days
}

function hasEventOnDate(dateStr) {
  return events.value.some(e => e.date === dateStr)
}

function getEventsForDate(dateStr) {
  return events.value.filter(e => e.date === dateStr)
}

function getEventsForDay(dateStr) {
  return getEventsForDate(dateStr)
}

function getEventStyle(event) {
  if (!event.startTime || !event.endTime) return {}
  const start = new Date(event.startTime)
  const end = new Date(event.endTime)
  const startHour = start.getHours() + start.getMinutes() / 60
  const endHour = end.getHours() + end.getMinutes() / 60
  const top = startHour * 60
  const height = (endHour - startHour) * 60
  return {
    top: `${top}px`,
    height: `${Math.max(height, 30)}px`
  }
}

function formatEventTime(event) {
  if (event.allDay) return '全天'
  return `${formatDateTime(event.startTime)} - ${formatDateTime(event.endTime)}`
}

function formatEventTimeFull(event) {
  const startDate = new Date(event.startTime)
  const endDate = new Date(event.endTime)
  if (event.allDay) {
    return `${formatDate(startDate)} 全天`
  }
  return `${formatDate(startDate)} ${formatDateTime(event.startTime)} - ${formatDateTime(event.endTime)}`
}

// 操作方法
function prevMonth() {
  const d = new Date(currentDate.value)
  d.setMonth(d.getMonth() - 1)
  currentDate.value = d
  loadEvents()
}

function nextMonth() {
  const d = new Date(currentDate.value)
  d.setMonth(d.getMonth() + 1)
  currentDate.value = d
  loadEvents()
}

function prevPeriod() {
  const d = new Date(currentDate.value)
  if (currentView.value === 'month') {
    d.setMonth(d.getMonth() - 1)
  } else if (currentView.value === 'week') {
    d.setDate(d.getDate() - 7)
  } else {
    d.setDate(d.getDate() - 1)
  }
  currentDate.value = d
  loadEvents()
}

function nextPeriod() {
  const d = new Date(currentDate.value)
  if (currentView.value === 'month') {
    d.setMonth(d.getMonth() + 1)
  } else if (currentView.value === 'week') {
    d.setDate(d.getDate() + 7)
  } else {
    d.setDate(d.getDate() + 1)
  }
  currentDate.value = d
  loadEvents()
}

function goToday() {
  currentDate.value = new Date()
  selectedDate.value = new Date()
  loadEvents()
}

function selectDate(day) {
  selectedDate.value = new Date(day.date)
  if (day.otherMonth) {
    currentDate.value = new Date(day.date)
    loadEvents()
  }
}

function openCreateDialog() {
  editingEvent.value = null
  formData.value = {
    title: '',
    timeRange: [new Date(), new Date(Date.now() + 3600000)],
    allDay: false,
    location: '',
    category: 'work',
    reminder: '15',
    participants: [],
    description: ''
  }
  showCreateDialog.value = true
}

function openEditDialog() {
  if (!currentEvent.value) return
  editingEvent.value = currentEvent.value
  formData.value = {
    title: currentEvent.value.title,
    timeRange: [new Date(currentEvent.value.startTime), new Date(currentEvent.value.endTime)],
    allDay: currentEvent.value.allDay,
    location: currentEvent.value.location || '',
    category: currentEvent.value.category || 'work',
    reminder: currentEvent.value.reminder || '15',
    participants: currentEvent.value.participants || [],
    description: currentEvent.value.description || ''
  }
  showDetailDialog.value = false
  showCreateDialog.value = true
}

function openEventDetail(event) {
  currentEvent.value = event
  showDetailDialog.value = true
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const data = {
      title: formData.value.title,
      startTime: formData.value.timeRange[0].toISOString(),
      endTime: formData.value.timeRange[1].toISOString(),
      allDay: formData.value.allDay,
      location: formData.value.location,
      category: formData.value.category,
      reminder: formData.value.reminder,
      participants: formData.value.participants,
      description: formData.value.description
    }

    let res
    if (editingEvent.value) {
      res = await updateSchedule(editingEvent.value.id, data)
    } else {
      res = await createSchedule(data)
    }

    if (res.code === 200) {
      ElMessage.success(editingEvent.value ? '更新成功' : '创建成功')
      showCreateDialog.value = false
      loadEvents()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete() {
  if (!editingEvent.value) return
  try {
    await ElMessageBox.confirm('确定删除此日程吗？', '删除确认', { type: 'warning' })
    const res = await deleteSchedule(editingEvent.value.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      showCreateDialog.value = false
      loadEvents()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

async function loadEvents() {
  loading.value = true
  try {
    const year = currentDate.value.getFullYear()
    const month = currentDate.value.getMonth()
    const startTime = new Date(year, month, 1)
    const endTime = new Date(year, month + 2, 0)

    const res = await getSchedulesByRange(
      startTime.toISOString(),
      endTime.toISOString()
    )

    if (res.code === 200) {
      events.value = (res.data || []).map(e => ({
        ...e,
        date: formatDate(new Date(e.startTime))
      }))
    }
  } catch (e) {
    console.error('加载日程失败', e)
    // 模拟数据用于演示
    generateMockEvents()
  } finally {
    loading.value = false
  }
}

function generateMockEvents() {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const today = new Date()

  events.value = [
    {
      id: 1,
      title: '团队周会',
      startTime: new Date(year, month, today.getDate(), 10, 0).toISOString(),
      endTime: new Date(year, month, today.getDate(), 11, 30).toISOString(),
      allDay: false,
      category: 'meeting',
      location: '会议室A',
      date: formatDate(new Date(year, month, today.getDate()))
    },
    {
      id: 2,
      title: '项目评审',
      startTime: new Date(year, month, today.getDate() + 1, 14, 0).toISOString(),
      endTime: new Date(year, month, today.getDate() + 1, 16, 0).toISOString(),
      allDay: false,
      category: 'work',
      location: '会议室B',
      date: formatDate(new Date(year, month, today.getDate() + 1))
    },
    {
      id: 3,
      title: '午餐约会',
      startTime: new Date(year, month, today.getDate() + 2, 12, 0).toISOString(),
      endTime: new Date(year, month, today.getDate() + 2, 13, 0).toISOString(),
      allDay: false,
      category: 'personal',
      location: '楼下餐厅',
      date: formatDate(new Date(year, month, today.getDate() + 2))
    },
    {
      id: 4,
      title: '提交报告',
      startTime: new Date(year, month, today.getDate() + 3).toISOString(),
      endTime: new Date(year, month, today.getDate() + 3).toISOString(),
      allDay: true,
      category: 'reminder',
      date: formatDate(new Date(year, month, today.getDate() + 3))
    }
  ]
}

// 监听视图切换
watch(currentView, () => {
  loadEvents()
})

onMounted(() => {
  loadEvents()
})
</script>

<style scoped lang="scss">
.calendar-panel {
  display: flex;
  height: 100%;
  background: var(--dt-bg-body);
}

// 侧边栏
.calendar-sidebar {
  width: 240px;
  background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: var(--dt-spacing-lg);
}

// 迷你日历
.mini-calendar {
  padding: 0 var(--dt-spacing-md);
  margin-bottom: var(--dt-spacing-lg);
}

.mini-calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--dt-spacing-sm) 0;
}

.mini-calendar-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.mini-calendar-grid {
  .mini-weekdays {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    text-align: center;
    margin-bottom: var(--dt-spacing-xs);
  }

  .mini-weekday {
    font-size: 11px;
    color: var(--dt-text-tertiary);
    padding: var(--dt-spacing-xs) 0;
  }

  .mini-days {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 2px;
  }

  .mini-day {
    aspect-ratio: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    color: var(--dt-text-primary);
    border-radius: 50%;
    cursor: pointer;
    position: relative;

    &:hover {
      background: var(--dt-bg-session-hover);
    }

    &.other-month {
      color: var(--dt-text-quaternary);
    }

    &.today {
      background: var(--dt-brand-color);
      color: #fff;
    }

    &.selected:not(.today) {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
    }

    &.has-event::after {
      content: '';
      position: absolute;
      bottom: 2px;
      width: 4px;
      height: 4px;
      border-radius: 50%;
      background: var(--dt-brand-color);
    }

    &.today.has-event::after {
      background: #fff;
    }
  }
}

// 视图切换
.view-switch {
  padding: 0 var(--dt-spacing-md);
  margin-bottom: var(--dt-spacing-lg);

  :deep(.el-radio-group) {
    width: 100%;
    display: flex;
  }

  :deep(.el-radio-button) {
    flex: 1;
  }

  :deep(.el-radio-button__inner) {
    width: 100%;
  }
}

// 筛选器
.calendar-filters {
  padding: 0 var(--dt-spacing-md);
  flex: 1;
}

.filter-title {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin-bottom: var(--dt-spacing-sm);
}

.filter-list {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-sm);

  :deep(.el-checkbox) {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-sm);
  }
}

.filter-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;

  &.work { background: #3b82f6; }
  &.meeting { background: #f59e0b; }
  &.personal { background: #10b981; }
  &.reminder { background: #ef4444; }
}

// 主内容区
.calendar-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

// 工具栏
.calendar-toolbar {
  height: 56px;
  padding: 0 var(--dt-spacing-lg);
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-sm);
  }

  .current-period {
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin-left: var(--dt-spacing-md);
  }
}

// 日历内容
.calendar-content {
  flex: 1;
  overflow: hidden;
}

// 月视图
.month-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.month-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;
}

.month-header-cell {
  padding: var(--dt-spacing-sm);
  text-align: center;
  font-size: 12px;
  color: var(--dt-text-tertiary);
  font-weight: 500;
}

.month-body {
  flex: 1;
  overflow-y: auto;
}

.month-week {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  min-height: 100px;
  border-bottom: 1px solid var(--dt-border-lighter);
}

.month-day {
  border-right: 1px solid var(--dt-border-lighter);
  padding: var(--dt-spacing-xs);
  cursor: pointer;
  transition: background var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  &:last-child {
    border-right: none;
  }

  &.other-month {
    background: var(--dt-bg-body);

    .day-number {
      color: var(--dt-text-quaternary);
    }
  }

  &.today {
    background: var(--dt-brand-lighter);
  }

  &.selected {
    background: var(--dt-brand-bg);
  }
}

.day-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: var(--dt-spacing-xs);
}

.day-number {
  font-size: 13px;
  color: var(--dt-text-primary);
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.today .day-number {
  background: var(--dt-brand-color);
  color: #fff;
}

.day-events {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.day-event {
  font-size: 11px;
  padding: 2px 4px;
  border-radius: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #fff;

  &.work { background: #3b82f6; }
  &.meeting { background: #f59e0b; }
  &.personal { background: #10b981; }
  &.reminder { background: #ef4444; }
}

.more-events {
  font-size: 11px;
  color: var(--dt-brand-color);
  padding: 2px 4px;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

// 周视图
.week-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.week-header {
  display: flex;
  border-bottom: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
  flex-shrink: 0;
}

.time-gutter {
  width: 60px;
  flex-shrink: 0;
}

.week-header-cell {
  flex: 1;
  padding: var(--dt-spacing-sm);
  text-align: center;
  border-left: 1px solid var(--dt-border-lighter);

  &.today {
    background: var(--dt-brand-lighter);
  }

  .weekday {
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }

  .day-num {
    font-size: 20px;
    font-weight: 600;
    color: var(--dt-text-primary);
  }
}

.week-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.time-axis {
  width: 60px;
  flex-shrink: 0;
  background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-lighter);
}

.time-slot {
  height: 60px;
  font-size: 11px;
  color: var(--dt-text-tertiary);
  text-align: right;
  padding-right: var(--dt-spacing-sm);
  padding-top: 0;
}

.week-grid {
  flex: 1;
  display: flex;
  overflow-x: auto;
}

.day-column {
  flex: 1;
  min-width: 120px;
  position: relative;
  border-right: 1px solid var(--dt-border-lighter);

  &.today {
    background: rgba(59, 130, 246, 0.02);
  }
}

.hour-cell {
  height: 60px;
  border-bottom: 1px solid var(--dt-border-lighter);
}

.week-event {
  position: absolute;
  left: 2px;
  right: 2px;
  border-radius: 4px;
  padding: 4px;
  font-size: 11px;
  color: #fff;
  cursor: pointer;
  overflow: hidden;

  &.work { background: #3b82f6; }
  &.meeting { background: #f59e0b; }
  &.personal { background: #10b981; }
  &.reminder { background: #ef4444; }

  .event-time {
    font-size: 10px;
    opacity: 0.8;
  }

  .event-title {
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

// 日视图
.day-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.day-header {
  display: flex;
  border-bottom: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
  flex-shrink: 0;
}

.day-header-cell {
  flex: 1;
  padding: var(--dt-spacing-md);
  text-align: center;

  &.today {
    background: var(--dt-brand-lighter);
  }

  .weekday {
    font-size: 14px;
    color: var(--dt-text-tertiary);
  }

  .day-num {
    font-size: 32px;
    font-weight: 600;
    color: var(--dt-text-primary);
  }
}

.day-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.day-grid {
  flex: 1;
  position: relative;
  background: var(--dt-bg-card);
}

.day-event-block {
  position: absolute;
  left: 70px;
  right: var(--dt-spacing-lg);
  border-radius: 8px;
  padding: var(--dt-spacing-sm);
  color: #fff;
  cursor: pointer;

  &.work { background: linear-gradient(135deg, #3b82f6, #2563eb); }
  &.meeting { background: linear-gradient(135deg, #f59e0b, #d97706); }
  &.personal { background: linear-gradient(135deg, #10b981, #059669); }
  &.reminder { background: linear-gradient(135deg, #ef4444, #dc2626); }

  .event-time {
    font-size: 12px;
    opacity: 0.9;
  }

  .event-title {
    font-size: 14px;
    font-weight: 500;
    margin: 4px 0;
  }

  .event-location {
    font-size: 12px;
    opacity: 0.8;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

// 日程详情弹窗
.event-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-sm);
    margin-bottom: var(--dt-spacing-lg);
  }

  .detail-category {
    width: 4px;
    height: 24px;
    border-radius: 2px;

    &.work { background: #3b82f6; }
    &.meeting { background: #f59e0b; }
    &.personal { background: #10b981; }
    &.reminder { background: #ef4444; }
  }

  .detail-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;
  }

  .detail-info {
    .info-row {
      display: flex;
      align-items: flex-start;
      gap: var(--dt-spacing-sm);
      padding: var(--dt-spacing-sm) 0;
      color: var(--dt-text-secondary);

      .el-icon {
        color: var(--dt-text-tertiary);
        margin-top: 2px;
      }
    }
  }
}
</style>