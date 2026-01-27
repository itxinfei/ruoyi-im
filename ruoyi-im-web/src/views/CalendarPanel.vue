<template>
  <div class="calendar-panel">
    <!-- Main Content Area -->
    <div class="calendar-main">
      <header class="calendar-header">
        <div class="header-left">
          <div class="date-range">
            <h2 class="range-title">{{ weekRangeString }}</h2>
            <button class="expand-btn">
              <span class="material-icons-outlined">expand_more</span>
            </button>
          </div>

          <div class="nav-controls">
            <button class="nav-btn" @click="previousWeek" title="上一周">
               <span class="material-icons-outlined text-lg">chevron_left</span>
            </button>
            <button class="today-btn" @click="goToToday">今天</button>
            <button class="nav-btn" @click="nextWeek" title="下一周">
               <span class="material-icons-outlined text-lg">chevron_right</span>
            </button>
          </div>
        </div>

        <div class="view-switcher">
          <button
            v-for="view in ['月', '周', '日', '日程']"
            :key="view"
            class="switch-btn"
            :class="{ active: currentView === view }"
            @click="currentView = view"
          >
            {{ view }}
          </button>
        </div>
      </header>

      <!-- Week View Grid -->
      <div v-if="currentView === '周'" class="week-view">
        <div class="week-header">
          <div class="time-column-header"></div>
          <div
            v-for="(day, index) in weekHeaders"
            :key="index"
            class="day-header"
            :class="{ 'is-today': isToday(day.date) }"
          >
            <div class="day-name">{{ day.name }}</div>
            <div class="day-date">{{ day.date.getDate() }}</div>
          </div>
        </div>

        <div class="grid-scroll-area">
          <div class="time-grid">
            <div class="grid-background">
              <div class="time-row" v-for="hour in hours" :key="hour">
                <span class="time-label">{{ formatHour(hour) }}</span>
              </div>
            </div>

            <div class="day-columns">
              <div class="time-column-spacer"></div>
              <div v-for="(day, index) in weekHeaders" :key="index" class="day-column" :class="{ 'is-today': isToday(day.date) }">
                 <!-- Events -->
                 <div
                    v-for="event in getEventsForDay(day.date)"
                    :key="event.id"
                    class="event-block"
                    :class="`event-${event.color}`"
                    :style="getEventStyle(event)"
                    @click.stop="handleEventClick(event)"
                 >
                   <div class="event-title">{{ event.title }}</div>
                   <div class="event-time">{{ event.startTime }} - {{ event.endTime }}</div>
                   <div v-if="event.location" class="event-location">{{ event.location }}</div>
                 </div>

                 <!-- Current Time Line -->
                 <div v-if="isToday(day.date)" class="current-time-line" :style="{ top: currentTimeTop + 'px' }">
                    <div class="dot"></div>
                    <div class="line"></div>
                 </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Month View Grid -->
      <div v-else-if="currentView === '月'" class="month-view">
        <div class="month-grid-header">
           <div v-for="day in ['一', '二', '三', '四', '五', '六', '日']" :key="day" class="weekday-label">
             {{ day }}
           </div>
        </div>
        <div class="month-grid">
           <div
             v-for="(day, index) in monthDays"
             :key="index"
             class="month-day-cell"
             :class="{
               'not-current-month': !day.isCurrentMonth,
               'is-today': isToday(day.date),
               'is-selected': isSameDay(day.date, selectedDate)
             }"
             @click="selectedDate = day.date"
           >
             <div class="cell-top">
                <span class="day-num">{{ day.date.getDate() }}</span>
                <span v-if="isToday(day.date)" class="today-label">今</span>
             </div>
             <div class="day-events">
               <div
                 v-for="event in getEventsForDay(day.date)"
                 :key="event.id"
                 class="mini-event"
                 :class="`event-bg-${event.color}`"
               >
                 {{ event.title }}
               </div>
             </div>
           </div>
        </div>
      </div>

      <div v-else class="view-placeholder">
        暂未实现 {{ currentView }} 视图
      </div>
    </div>

    <!-- Right Sidebar -->
    <aside class="right-panel">
      <div class="panel-header">
        <div class="date-info">
          <div>
            <h3 class="date-title">{{ formatDate(selectedDate) }}</h3>
            <p class="date-subtitle">{{ formatLunarDate(selectedDate) }}</p>
          </div>
          <div class="date-badge">
            {{ selectedDate.getDate() }}
          </div>
        </div>

        <button class="add-btn" @click="handleAddSchedule">
          <span class="material-icons-outlined">add</span>
          新建日程
        </button>
      </div>

      <div class="panel-content">
        <h4 class="section-title">今日日程</h4>

        <div v-if="selectedDateEvents.length > 0" class="schedule-list">
          <div
            v-for="event in selectedDateEvents"
            :key="event.id"
            class="schedule-item"
            @click="handleEventClick(event)"
          >
            <div class="timeline-indicator">
              <div class="dot" :class="`dot-${event.color}`"></div>
              <div class="line"></div>
            </div>

            <div class="event-card">
              <div class="card-header">
                <span class="card-title">{{ event.title }}</span>
              </div>
              <div class="card-time">
                <span class="time-tag">{{ event.startTime }} - {{ event.endTime }}</span>
              </div>

              <div v-if="event.location" class="card-location">
                <span class="material-icons-outlined text-[14px]">location_on</span>
                {{ event.location }}
              </div>

              <div v-if="event.attendees && event.attendees.length" class="attendees">
                <div
                  v-for="(person, idx) in event.attendees.slice(0, 3)"
                  :key="idx"
                  class="avatar"
                  :class="getAvatarClass(idx)"
                >
                  {{ person.name ? person.name[0] : person[0] }}
                </div>
                 <div v-if="event.attendees.length > 3" class="avatar more">
                  +{{ event.attendees.length - 3 }}
                 </div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          暂无日程
        </div>

        <div class="todos-section">
          <h4 class="section-title">待办事项</h4>
          <div v-for="todo in todos" :key="todo.id" class="todo-item">
            <input type="checkbox" class="todo-checkbox" :checked="todo.completed"/>
            <span class="todo-text">{{ todo.text }}</span>
          </div>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getEventsByTimeRange, createEvent, updateEvent, deleteEvent } from '@/api/im/schedule'

// --- State ---
const currentDate = ref(new Date())
const selectedDate = ref(new Date())
const currentView = ref('周')
const currentTimeTop = ref(0)
const loading = ref(false)
let timer = null

const todos = ref([
  { id: 1, text: '审核 UI 设计稿', completed: false },
  { id: 2, text: '回复客户邮件', completed: false }
])

// 从API加载的日程事件
const allEvents = ref([])

// --- Helpers ---
const hours = Array.from({ length: 13 }, (_, i) => i + 8) // 08:00 - 20:00
const START_HOUR = 8
const HOUR_HEIGHT = 80
const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const monthNames = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

// 格式化日期为 yyyy-MM-dd HH:mm:ss
const formatDateTime = (date, time = '00:00:00') => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d} ${time}`
}

// 格式化日期为 yyyy-MM-dd
const formatDateOnly = (date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

// 颜色映射：后端颜色 -> 前端颜色类
const colorMap = {
  'BLUE': 'blue',
  'GREEN': 'emerald',
  'ORANGE': 'orange',
  'PURPLE': 'purple',
  'RED': 'red',
  'GRAY': 'slate',
  'DEFAULT': 'blue'
}

// 加载日程数据
const loadEvents = async () => {
  loading.value = true
  try {
    // 获取当前周的开始和结束时间
    const curr = new Date(currentDate.value)
    const day = curr.getDay()
    const diff = curr.getDate() - day + (day === 0 ? -6 : 1)
    const monday = new Date(curr.setDate(diff))

    const startTime = new Date(monday)
    startTime.setHours(0, 0, 0, 0)

    const endTime = new Date(monday)
    endTime.setDate(monday.getDate() + 6)
    endTime.setHours(23, 59, 59, 999)

    const res = await getEventsByTimeRange(
      formatDateTime(startTime, '00:00:00'),
      formatDateTime(endTime, '23:59:59')
    )

    if (res.code === 200 && res.data) {
      // 转换后端数据格式为前端需要的格式
      allEvents.value = res.data.map(event => {
        const startDate = new Date(event.startTime)
        const endDate = new Date(event.endTime)

        return {
          id: event.id,
          title: event.title,
          startTime: `${String(startDate.getHours()).padStart(2, '0')}:${String(startDate.getMinutes()).padStart(2, '0')}`,
          endTime: `${String(endDate.getHours()).padStart(2, '0')}:${String(endDate.getMinutes()).padStart(2, '0')}`,
          date: formatDateOnly(startDate),
          color: colorMap[event.color?.toUpperCase()] || colorMap.DEFAULT,
          location: event.location,
          attendees: (event.participants || []).map(p => ({
            name: p.nickname || p.username || '?'
          })),
          description: event.description
        }
      })
    }
  } catch (error) {
    console.error('加载日程失败:', error)
    // 失败时使用空数组，不显示错误
  } finally {
    loading.value = false
  }
}

// 监听当前日期变化，重新加载日程
watch(currentDate, () => {
  loadEvents()
})

// 初始化加载
onMounted(() => {
  loadEvents()
  updateCurrentTime()
  timer = setInterval(updateCurrentTime, 60000)
})

// --- Computed ---
const weekHeaders = computed(() => {
  const curr = new Date(currentDate.value)
  const day = curr.getDay()
  const diff = curr.getDate() - day + (day === 0 ? -6 : 1) // Monday start

  const monday = new Date(curr.setDate(diff))
  const days = []

  const displayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

  for (let i = 0; i < 7; i++) {
    const d = new Date(monday)
    d.setDate(monday.getDate() + i)
    days.push({
      name: displayNames[i],
      date: d
    })
  }
  return days
})

const weekRangeString = computed(() => {
  if (!weekHeaders.value.length) return ''
  const start = weekHeaders.value[0].date
  const end = weekHeaders.value[6].date
  return `${start.getFullYear()}年 ${start.getMonth() + 1}月${start.getDate()}日 - ${end.getMonth() + 1}月${end.getDate()}日`
})

const selectedDateEvents = computed(() => {
  return allEvents.value.filter(event => {
    const eDate = new Date(event.date)
    const sDate = selectedDate.value
    return eDate.getDate() === sDate.getDate() &&
           eDate.getMonth() === sDate.getMonth() &&
           eDate.getFullYear() === sDate.getFullYear()
  })
})

const monthDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()

  // First day of the month
  const firstDay = new Date(year, month, 1)
  // Day of week (0 is Sunday)
  let startDay = firstDay.getDay()
  // Monday start: Sun->7, Mon->1, Tue->2...
  startDay = startDay === 0 ? 7 : startDay

  const days = []

  // Previous month padding
  for (let i = startDay - 1; i > 0; i--) {
    const d = new Date(year, month, 1 - i)
    days.push({ date: d, isCurrentMonth: false })
  }

  // Current month days
  const lastDay = new Date(year, month + 1, 0).getDate()
  for (let i = 1; i <= lastDay; i++) {
    const d = new Date(year, month, i)
    days.push({ date: d, isCurrentMonth: true })
  }

  // Next month padding to fill 6 weeks (42 cells)
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    const d = new Date(year, month + 1, i)
    days.push({ date: d, isCurrentMonth: false })
  }

  return days
})

// --- Methods ---
const parseTime = (timeStr) => {
  const [hours, minutes] = timeStr.split(':').map(Number);
  return hours + minutes / 60;
};

const formatHour = (hour) => `${hour.toString().padStart(2, '0')}:00`

const formatDate = (date) => `${monthNames[date.getMonth()]}${date.getDate()}日`

const formatLunarDate = (date) => `${dayNames[date.getDay()]} · 农历Mock` // Shim

const isToday = (date) => {
  const today = new Date()
  return date.getDate() === today.getDate() &&
         date.getMonth() === today.getMonth() &&
         date.getFullYear() === today.getFullYear()
}

const isSameDay = (d1, d2) => {
  return d1.getFullYear() === d2.getFullYear() &&
         d1.getMonth() === d2.getMonth() &&
         d1.getDate() === d2.getDate()
}

const getEventsForDay = (date) => {
  return allEvents.value.filter(event => {
    const eventDate = new Date(event.date)
    return eventDate.getDate() === date.getDate() &&
           eventDate.getMonth() === date.getMonth() &&
           eventDate.getFullYear() === date.getFullYear()
  })
}

const getEventStyle = (event) => {
  const start = parseTime(event.startTime)
  const end = parseTime(event.endTime)
  const top = (start - START_HOUR) * HOUR_HEIGHT
  const height = (end - start) * HOUR_HEIGHT
  return { top: `${top}px`, height: `${height}px` }
}

const getAvatarClass = (index) => {
  const colors = ['bg-blue', 'bg-orange', 'bg-slate']
  return colors[index % colors.length]
}

const updateCurrentTime = () => {
  const now = new Date()
  const hours = now.getHours()
  const minutes = now.getMinutes()
  const timeVal = hours + minutes / 60
  currentTimeTop.value = (timeVal - START_HOUR) * HOUR_HEIGHT
}

// Actions
const previousWeek = () => {
  const d = new Date(currentDate.value)
  d.setDate(d.getDate() - 7)
  currentDate.value = d
}

const nextWeek = () => {
  const d = new Date(currentDate.value)
  d.setDate(d.getDate() + 7)
  currentDate.value = d
}

const goToToday = () => {
  currentDate.value = new Date()
  selectedDate.value = new Date()
}

const handleEventClick = (event) => {
  selectedDate.value = new Date(event.date)
}

const handleAddSchedule = () => {
  // TODO: 打开日程创建对话框
  ElMessage.info('新建日程功能开发中...')
}

// Lifecycle (已移到前面)
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style lang="scss" scoped>
/* Main Layout */
.calendar-panel {
  display: flex;
  height: 100%;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  background-color: var(--dt-bg-card);
}

.calendar-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* Header */
.calendar-header {
  height: 64px;
  padding: 0 24px;
  border-bottom: 1px solid var(--dt-border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--dt-bg-card);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.range-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--dt-text-primary);
  margin: 0;
}

.expand-btn {
  border: none;
  background: transparent;
  color: var(--dt-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: color var(--dt-transition-fast);

  &:hover { color: var(--dt-brand-color); }
}

.nav-controls {
  display: flex;
  align-items: center;
  background-color: var(--dt-bg-body);
  border-radius: var(--dt-radius-md);
  padding: 2px;
}

.nav-btn {
  padding: 4px;
  border: none;
  background: transparent;
  color: var(--dt-text-secondary);
  border-radius: var(--dt-radius-sm);
  cursor: pointer;
  display: flex;
  align-items: centers;
  transition: all var(--dt-transition-fast);

  &:hover { background-color: var(--dt-bg-card); }
}

.today-btn {
  padding: 2px 12px;
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-secondary);
  border: none;
  background: transparent;
  cursor: pointer;
}

.view-switcher {
  display: flex;
  background-color: var(--dt-bg-body);
  padding: 4px;
  border-radius: var(--dt-radius-md);
}

.switch-btn {
  padding: 6px 16px;
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-secondary);
  border: none;
  background: transparent;
  border-radius: var(--dt-radius-sm);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &.active {
    background-color: var(--dt-bg-card);
    color: var(--dt-brand-color);
    box-shadow: var(--dt-shadow-1);
    font-weight: 600;
  }
}

.view-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dt-text-tertiary);
  font-size: 16px;
}

/* Week View */
.week-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: var(--dt-bg-body);
  overflow: hidden;
  height: 100%;
}

.week-header {
  display: flex;
  border-bottom: 1px solid var(--dt-border-color);
  background-color: var(--dt-bg-card);
  flex-shrink: 0;
}

.time-column-header {
  width: 60px;
  border-right: 1px solid var(--dt-border-color);
}

.day-header {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  border-right: 1px solid var(--dt-border-color);
  transition: background-color var(--dt-transition-fast);

  &:last-child { border-right: none; }

  &.is-today {
    background-color: var(--dt-brand-bg);
    .day-name, .day-date { color: var(--dt-brand-color); }
    .day-name { font-weight: 600; }
    .day-date { font-weight: bold; }
  }
}

.day-name {
  font-size: 12px;
  color: var(--dt-text-secondary);
  margin-bottom: 4px;
}

.day-date {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.grid-scroll-area {
  flex: 1;
  overflow-y: auto;
  position: relative;

  &::-webkit-scrollbar { width: 6px; height: 6px; }
  &::-webkit-scrollbar-thumb { background: var(--dt-border-color); border-radius: 10px; }
}

.time-grid {
  position: relative;
  min-height: 100%;
}

.grid-background {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  flex-direction: column;
}

.time-row {
  height: 80px;
  border-bottom: 1px solid var(--dt-border-light);
  position: relative;
  &:last-child { border-bottom: none; }
}

.time-label {
  position: absolute;
  top: -9px;
  left: 8px;
  font-size: 11px;
  color: var(--dt-text-tertiary);
  width: 50px;
  text-align: right;
  padding-right: 8px;
}

.day-columns {
  position: relative;
  display: flex;
  height: 1040px;
}

.time-column-spacer {
  width: 60px;
  border-right: 1px solid var(--dt-border-color);
  flex-shrink: 0;
  background-color: var(--dt-bg-body);
}

.day-column {
  flex: 1;
  position: relative;
  border-right: 1px solid var(--dt-border-light);

  &:last-child { border-right: none; }
}

.event-block {
  position: absolute;
  left: 4px; right: 4px;
  border-radius: var(--dt-radius-md);
  padding: 8px;
  font-size: 12px;
  cursor: pointer;
  border-left: 4px solid;
  transition: all var(--dt-transition-fast);
  overflow: hidden;
  box-shadow: var(--dt-shadow-1);
  z-index: 10;

  &:hover {
    z-index: 20;
    opacity: 0.95;
    box-shadow: var(--dt-shadow-float);
    transform: translateY(-1px);
  }
}

.event-title { font-weight: 700; margin-bottom: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.event-time { opacity: 0.9; margin-bottom: 2px; }
.event-location { opacity: 0.8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* Themes */
.event-blue { background-color: var(--dt-brand-bg); border-left-color: var(--dt-brand-color); color: var(--dt-brand-color); }
.event-orange { background-color: var(--dt-warning-bg); border-left-color: var(--dt-warning-color); color: var(--dt-warning-color); }
.event-purple { background-color: #f3e8ff; border-left-color: #a855f7; color: #6b21a8; }
.event-emerald { background-color: var(--dt-success-bg); border-left-color: var(--dt-success-color); color: var(--dt-success-color); }
.event-slate { background-color: var(--dt-bg-body); border-left-color: var(--dt-text-tertiary); color: var(--dt-text-secondary); }

.current-time-line {
  position: absolute;
  left: 0; right: 0;
  display: flex; align-items: center;
  z-index: 30; pointer-events: none;

  .dot { width: 8px; height: 8px; border-radius: 50%; background-color: var(--dt-error-color); margin-left: -4px; }
  .line { flex: 1; height: 2px; background-color: var(--dt-error-color); box-shadow: 0 0 8px var(--dt-error-color); }
}

/* Month View */
.month-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: var(--dt-bg-body);
  overflow: hidden;
}

.month-grid-header {
  display: flex;
  background-color: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-color);

  .weekday-label {
    flex: 1;
    padding: 10px 0;
    text-align: center;
    font-size: 13px;
    font-weight: 600;
    color: var(--dt-text-secondary);
  }
}

.month-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: repeat(6, 1fr);
  background-color: var(--dt-border-color);
  gap: 1px;
  overflow: auto;
}

.month-day-cell {
  background-color: var(--dt-bg-card);
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-height: 0;
  cursor: pointer;
  transition: background-color var(--dt-transition-fast);

  &:hover { background-color: var(--dt-bg-hover); }

  &.not-current-month {
    background-color: var(--dt-bg-body);
    .day-num { color: var(--dt-text-quaternary); }
  }

  &.is-selected {
    background-color: var(--dt-brand-bg);
  }

  &.is-today {
    .day-num {
      color: var(--dt-brand-color);
      font-weight: bold;
    }
  }
}

.cell-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.day-num {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
}

.today-label {
  font-size: 11px;
  background-color: var(--dt-brand-color);
  color: #fff;
  padding: 0 4px;
  border-radius: var(--dt-radius-full);
}

.day-events {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.mini-event {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: var(--dt-radius-sm);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  &.event-bg-blue { background-color: var(--dt-brand-bg); color: var(--dt-brand-color); border-left: 2px solid var(--dt-brand-color); }
  &.event-bg-orange { background-color: var(--dt-warning-bg); color: var(--dt-warning-color); border-left: 2px solid var(--dt-warning-color); }
  &.event-bg-purple { background-color: #f3e8ff; color: #6b21a8; border-left: 2px solid #a855f7; }
  &.event-bg-emerald { background-color: var(--dt-success-bg); color: var(--dt-success-color); border-left: 2px solid var(--dt-success-color); }
  &.event-bg-slate { background-color: var(--dt-bg-body); color: var(--dt-text-secondary); border-left: 2px solid var(--dt-text-tertiary); }
}

/* Right Sidebar */
.right-panel {
  width: 320px;
  background-color: var(--dt-bg-card);
  border-left: 1px solid var(--dt-border-color);
  display: flex; flex-direction: column;
  box-shadow: var(--dt-shadow-lg);
  z-index: 10;
}

.panel-header {
  padding: 24px;
  border-bottom: 1px solid var(--dt-border-color);
  background-color: var(--dt-bg-body);
}

.date-info {
  display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 24px;
}

.date-title { font-size: 20px; font-weight: 700; color: var(--dt-text-primary); margin: 0; }
.date-subtitle { font-size: 14px; color: var(--dt-text-secondary); margin-top: 4px; }

.date-badge {
  display: flex; align-items: center; justify-content: center;
  width: 48px; height: 48px;
  background-color: var(--dt-brand-bg);
  border-radius: var(--dt-radius-lg);
  color: var(--dt-brand-color); font-weight: 700; font-size: 24px;
}

.add-btn {
  width: 100%;
  background-color: var(--dt-brand-color); color: #fff;
  border: none; padding: 12px;
  border-radius: var(--dt-radius-lg);
  display: flex; align-items: center; justify-content: center; gap: 8px;
  font-weight: 500; cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background-color: var(--dt-brand-hover);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
  }
}

.panel-content { flex: 1; overflow-y: auto; padding: 20px; }

.section-title {
  font-size: 12px; font-weight: 700; color: var(--dt-text-tertiary);
  text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 16px;
}

.schedule-item {
  display: flex; gap: 16px; position: relative; padding-bottom: 32px;
  &:last-child { padding-bottom: 0; }
}

.timeline-indicator {
  display: flex; flex-direction: column; align-items: center;
  .dot { width: 12px; height: 12px; border-radius: 50%; }
  .dot-blue { background-color: var(--dt-brand-color); box-shadow: 0 0 0 4px var(--dt-brand-bg); }
  .dot-orange { background-color: var(--dt-warning-color); box-shadow: 0 0 0 4px var(--dt-warning-bg); }
  .dot-purple { background-color: #a855f7; box-shadow: 0 0 0 4px #f3e8ff; }
  .dot-emerald { background-color: var(--dt-success-color); box-shadow: 0 0 0 4px var(--dt-success-bg); }
  .dot-slate { background-color: var(--dt-text-tertiary); box-shadow: 0 0 0 4px var(--dt-bg-body); }

  .line { flex: 1; width: 2px; background-color: var(--dt-border-color); margin: 4px 0; }
  .schedule-item:last-child .line { display: none; }
}

.event-card {
  flex: 1;
  background-color: var(--dt-brand-bg);
  padding: 14px;
  border-radius: var(--dt-radius-xl);
  border: 1px solid var(--dt-brand-color);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    box-shadow: var(--dt-shadow-float);
    transform: translateY(-1px);
  }
}

.card-header { display: flex; justify-content: space-between; margin-bottom: 4px; }
.card-title { font-weight: 700; font-size: 14px; color: var(--dt-text-primary); overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
.card-time { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.time-tag { font-size: 12px; font-weight: 700; color: var(--dt-brand-color); background-color: var(--dt-bg-card); padding: 2px 6px; border-radius: var(--dt-radius-sm); }
.card-location { display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--dt-text-secondary); margin-bottom: 12px; }

.attendees {
  display: flex; padding-left: 4px;
  .avatar {
    width: 24px; height: 24px; border-radius: 50%; border: 2px solid #fff;
    display: flex; align-items: center; justify-content: center;
    font-size: 10px; color: #fff; margin-left: -8px;
    &:first-child { margin-left: 0; }
    &.bg-blue { background-color: var(--dt-brand-color); }
    &.bg-orange { background-color: var(--dt-warning-color); }
    &.bg-slate { background-color: var(--dt-border-color); color: var(--dt-text-secondary); }
  }
}

.todos-section { margin-top: 32px; padding-top: 24px; border-top: 1px solid var(--dt-border-light); }
.todo-item { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; cursor: pointer; &:hover .todo-text { color: var(--dt-brand-color); } }
.todo-checkbox { width: 16px; height: 16px; border-radius: var(--dt-radius-sm); accent-color: var(--dt-brand-color); cursor: pointer; }
.todo-text { font-size: 14px; color: var(--dt-text-secondary); transition: color var(--dt-transition-fast); }
.empty-state { font-size: 14px; color: var(--dt-text-tertiary); text-align: center; padding: 20px 0; }

/* Dark Mode */
.dark .calendar-panel { background-color: var(--dt-bg-card-dark); }
.dark .calendar-header { background-color: var(--dt-bg-card-dark); border-bottom-color: var(--dt-border-dark); }
.dark .range-title { color: var(--dt-text-primary-dark); }
.dark .week-header { background-color: var(--dt-bg-card-dark); border-bottom-color: var(--dt-border-dark); }
.dark .day-header { border-right-color: var(--dt-border-dark); }
.dark .time-column-header { border-right-color: var(--dt-border-dark); }
.dark .time-column-spacer { background-color: var(--dt-bg-body-dark); border-right-color: var(--dt-border-dark); }
.dark .day-column { border-right-color: var(--dt-border-dark); }
.dark .time-row { border-bottom-color: var(--dt-border-dark); }
.dark .month-view { background-color: var(--dt-bg-body-dark); }
.dark .month-grid-header { background-color: var(--dt-bg-card-dark); border-bottom-color: var(--dt-border-dark); }
.dark .weekday-label { color: var(--dt-text-secondary-dark); }
.dark .month-grid { background-color: var(--dt-border-dark); gap: 1px; }
.dark .month-day-cell { background-color: var(--dt-bg-card-dark); }
.dark .right-panel { background-color: var(--dt-bg-card-dark); border-left-color: var(--dt-border-dark); }
.dark .panel-header { background-color: var(--dt-bg-body-dark); }
.dark .date-title { color: var(--dt-text-primary-dark); }
.dark .event-card { background-color: var(--dt-brand-bg-dark); border-color: var(--dt-brand-color); }
.dark .nav-controls { background-color: var(--dt-bg-hover-dark); }
.dark .view-switcher { background-color: var(--dt-bg-hover-dark); }
.dark .time-label { color: var(--dt-text-tertiary-dark); }
.dark .day-name { color: var(--dt-text-secondary-dark); }
.dark .day-date { color: var(--dt-text-primary-dark); }
</style>
