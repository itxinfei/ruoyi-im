<template>
  <div class="calendar-panel">
    <!-- Main Content Area -->
    <div class="calendar-main">
      <header class="calendar-header">
        <div class="header-left">
          <div class="date-range">
            <h2 class="range-title">{{ weekRangeString }}</h2>
            <button class="expand-btn">
              <el-icon><ArrowDown /></el-icon>
            </button>
          </div>
          
          <div class="nav-controls">
            <button class="nav-btn" @click="previousWeek" title="上一周">
               <el-icon><ArrowLeft /></el-icon>
            </button>
            <button class="today-btn" @click="goToToday">今天</button>
            <button class="nav-btn" @click="nextWeek" title="下一周">
               <el-icon><ArrowRight /></el-icon>
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
              <div v-for="(day, index) in weekHeaders" :key="index" class="day-column">
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
          <el-icon><Plus /></el-icon>
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
                <el-icon><Location /></el-icon>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import {
  ArrowLeft,
  ArrowRight,
  ArrowDown,
  Plus,
  Location
} from '@element-plus/icons-vue'

// --- State ---
const currentDate = ref(new Date())
const selectedDate = ref(new Date())
const currentView = ref('周')
const currentTimeTop = ref(0)
let timer = null

const todos = ref([
  { id: 1, text: '审核 UI 设计稿', completed: false },
  { id: 2, text: '回复客户邮件', completed: false }
])

const allEvents = ref([
  {
    id: 1,
    title: '部门周会',
    startTime: '09:00',
    endTime: '10:30',
    date: '', // Set on init
    color: 'blue',
    location: '会议室 A302',
    attendees: [{name: '李'}, {name: '王'}, {name: '+'}]
  },
  {
    id: 2,
    title: '产品需求评审',
    startTime: '10:00',
    endTime: '11:30',
    date: '', 
    color: 'blue',
    location: '会议室 A302',
    attendees: [{name: '张'}, {name: '陈'}]
  },
  {
    id: 3,
    title: '技术分享会',
    startTime: '11:00',
    endTime: '12:00',
    date: '', 
    color: 'emerald',
    location: null,
    attendees: []
  },
  {
    id: 4,
    title: '全员大会',
    startTime: '14:00',
    endTime: '15:00',
    date: '',
    color: 'orange',
    location: 'Zoom 在线会议',
    attendees: [{name: '全'}]
  },
  {
    id: 5,
    title: 'Q4 季度规划会',
    startTime: '16:00',
    endTime: '18:00',
    date: '',
    color: 'purple',
    location: '会议室 B101',
    attendees: [{name: '高'}, {name: '李'}, {name: '赵'}, {name: '钱'}]
  },
  {
    id: 6,
    title: '提交周报',
    startTime: '17:00',
    endTime: '17:30',
    date: '', 
    color: 'slate',
    location: null,
    attendees: []
  }
])

// --- Helpers ---
const hours = Array.from({ length: 13 }, (_, i) => i + 8) // 08:00 - 20:00
const START_HOUR = 8
const HOUR_HEIGHT = 80
const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const monthNames = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

const initMockData = () => {
  const today = new Date();
  const fmt = (d) => {
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      return `${y}-${m}-${day}`;
  }
  
  const d1 = new Date(today); d1.setDate(today.getDate() - 1);
  const d2 = new Date(today); 
  const d3 = new Date(today); d3.setDate(today.getDate() + 2);
  
  allEvents.value[0].date = fmt(d1); 
  allEvents.value[1].date = fmt(d2); 
  allEvents.value[2].date = fmt(d2);
  allEvents.value[3].date = fmt(d2);
  allEvents.value[4].date = fmt(d2);
  allEvents.value[5].date = fmt(d3); 
}
initMockData();

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

const handleAddSchedule = () => {}

// Lifecycle
onMounted(() => {
  updateCurrentTime()
  timer = setInterval(updateCurrentTime, 60000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style lang="scss" scoped>
/* Main Layout */
.calendar-panel {
  display: flex;
  height: 100%;
  width: 100%;
  overflow: hidden;
  background-color: #fff;
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
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
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
  color: #0f172a;
  margin: 0;
}

.expand-btn {
  border: none;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: color 0.2s;
  
  &:hover { color: #1677ff; }
}

.nav-controls {
  display: flex;
  align-items: center;
  background-color: #f1f5f9;
  border-radius: 8px;
  padding: 2px;
}

.nav-btn {
  padding: 4px;
  border: none;
  background: transparent;
  color: #475569;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  &:hover { background-color: #fff; }
}

.today-btn {
  padding: 2px 12px;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  border: none;
  background: transparent;
  cursor: pointer;
}

.view-switcher {
  display: flex;
  background-color: #f1f5f9;
  padding: 4px;
  border-radius: 8px;
}

.switch-btn {
  padding: 6px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  
  &.active {
    background-color: #fff;
    color: #1677ff;
    box-shadow: 0 1px 2px 0 rgba(0,0,0,0.05);
    font-weight: 600;
  }
}

.view-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 16px;
}

/* Week View */
.week-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
  overflow: hidden;
  height: 100%;
}

.week-header {
  display: flex;
  border-bottom: 1px solid #e2e8f0;
  background-color: #fff;
  flex-shrink: 0;
}

.time-column-header {
  width: 60px;
  border-right: 1px solid #e2e8f0;
}

.day-header {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  border-right: 1px solid #e2e8f0;
  
  &:last-child { border-right: none; }
  
  &.is-today {
    background-color: rgba(22, 119, 255, 0.05);
    .day-name, .day-date { color: #1677ff; }
    .day-name { font-weight: 600; }
    .day-date { font-weight: bold; }
  }
}

.day-name {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.day-date {
  font-size: 18px;
  font-weight: 600;
  color: #334155;
}

.grid-scroll-area {
  flex: 1;
  overflow-y: auto;
  position: relative;
  
  &::-webkit-scrollbar { width: 6px; height: 6px; }
  &::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
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
  border-bottom: 1px solid #f1f5f9;
  position: relative;
  &:last-child { border-bottom: none; }
}

.time-label {
  position: absolute;
  top: -9px;
  left: 8px;
  font-size: 11px;
  color: #94a3b8;
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
  border-right: 1px solid #e2e8f0;
  flex-shrink: 0;
  background-color: #f8fafc;
}

.day-column {
  flex: 1;
  position: relative;
  border-right: 1px solid #f1f5f9;
  &:last-child { border-right: none; }
}

.event-block {
  position: absolute;
  left: 4px; right: 4px;
  border-radius: 4px;
  padding: 8px;
  font-size: 12px;
  cursor: pointer;
  border-left: 4px solid;
  transition: all 0.2s;
  overflow: hidden;
  box-shadow: 0 1px 2px 0 rgba(0,0,0,0.05);
  z-index: 10;
  
  &:hover {
    z-index: 20;
    opacity: 0.95;
    box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
  }
}

.event-title { font-weight: 700; margin-bottom: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.event-time { opacity: 0.9; margin-bottom: 2px; }
.event-location { opacity: 0.8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* Themes */
.event-blue { background-color: #dbeafe; border-left-color: #3b82f6; color: #1e40af; }
.event-orange { background-color: #ffedd5; border-left-color: #f97316; color: #9a3412; }
.event-purple { background-color: #f3e8ff; border-left-color: #a855f7; color: #6b21a8; }
.event-emerald { background-color: #d1fae5; border-left-color: #10b981; color: #065f46; }
.event-slate { background-color: #f1f5f9; border-left-color: #94a3b8; color: #334155; }

.current-time-line {
  position: absolute;
  left: 0; right: 0;
  display: flex; align-items: center;
  z-index: 30; pointer-events: none;
  
  .dot { width: 8px; height: 8px; border-radius: 50%; background-color: #ef4444; margin-left: -4px; }
  .line { flex: 1; height: 2px; background-color: #ef4444; box-shadow: 0 0 8px rgba(239,68,68,0.5); }
}

/* Month View */
.month-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
  overflow: hidden;
}

.month-grid-header {
  display: flex;
  background-color: #fff;
  border-bottom: 1px solid #e2e8f0;
  .weekday-label {
    flex: 1;
    padding: 10px 0;
    text-align: center;
    font-size: 13px;
    font-weight: 600;
    color: #64748b;
  }
}

.month-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: repeat(6, 1fr);
  background-color: #e2e8f0;
  gap: 1px;
  overflow: auto;
}

.month-day-cell {
  background-color: #fff;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-height: 0;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:hover { background-color: #f8fafc; }
  
  &.not-current-month {
    background-color: #fdfdfd;
    .day-num { color: #cbd5e1; }
  }
  
  &.is-selected {
    background-color: #eff6ff;
  }
  
  &.is-today {
    .day-num {
      color: #1677ff;
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
  color: #334155;
}

.today-label {
  font-size: 11px;
  background-color: #1677ff;
  color: #fff;
  padding: 0 4px;
  border-radius: 10px;
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
  border-radius: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  
  &.event-bg-blue { background-color: #dbeafe; color: #1e40af; border-left: 2px solid #3b82f6; }
  &.event-bg-orange { background-color: #ffedd5; color: #9a3412; border-left: 2px solid #f97316; }
  &.event-bg-purple { background-color: #f3e8ff; color: #6b21a8; border-left: 2px solid #a855f7; }
  &.event-bg-emerald { background-color: #d1fae5; color: #065f46; border-left: 2px solid #10b981; }
  &.event-bg-slate { background-color: #f1f5f9; color: #334155; border-left: 2px solid #94a3b8; }
}

/* Right Sidebar */
.right-panel {
  width: 320px;
  background-color: #fff;
  border-left: 1px solid #e2e8f0;
  display: flex; flex-direction: column;
  box-shadow: -4px 0 15px -3px rgba(0,0,0,0.1);
  z-index: 10;
}

.panel-header {
  padding: 24px;
  border-bottom: 1px solid #e2e8f0;
  background-color: #f8fafc;
}

.date-info {
  display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 24px;
}
.date-title { font-size: 20px; font-weight: 700; color: #1e293b; margin: 0; }
.date-subtitle { font-size: 14px; color: #64748b; margin-top: 4px; }

.date-badge {
  display: flex; align-items: center; justify-content: center;
  width: 48px; height: 48px;
  background-color: rgba(22, 119, 255, 0.1);
  border-radius: 12px;
  color: #1677ff; font-weight: 700; font-size: 24px;
}

.add-btn {
  width: 100%;
  background-color: #1677ff; color: #fff;
  border: none; padding: 12px;
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  font-weight: 500; cursor: pointer;
  
  &:hover { background-color: #2563eb; transform: translateY(-2px); }
}

.panel-content { flex: 1; overflow-y: auto; padding: 20px; }

.section-title {
  font-size: 12px; font-weight: 700; color: #94a3b8;
  text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 16px;
}

.schedule-item {
  display: flex; gap: 16px; position: relative; padding-bottom: 32px;
  &:last-child { padding-bottom: 0; }
}

.timeline-indicator {
  display: flex; flex-direction: column; align-items: center;
  .dot { width: 12px; height: 12px; border-radius: 50%; }
  .dot-blue { background-color: #3b82f6; box-shadow: 0 0 0 4px #eff6ff; }
  .dot-orange { background-color: #f97316; box-shadow: 0 0 0 4px #fff7ed; }
  .dot-purple { background-color: #a855f7; box-shadow: 0 0 0 4px #f3e8ff; }
  .dot-emerald { background-color: #10b981; box-shadow: 0 0 0 4px #ecfdf5; }
  .dot-slate { background-color: #94a3b8; box-shadow: 0 0 0 4px #f1f5f9; }
  
  .line { flex: 1; width: 2px; background-color: #e2e8f0; margin: 4px 0; }
  .schedule-item:last-child .line { display: none; }
}

.event-card {
  flex: 1; background-color: #eff6ff; padding: 14px;
  border-radius: 12px; border: 1px solid #dbeafe;
  cursor: pointer; transition: all 0.2s;
  &:hover { box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
}

.card-header { display: flex; justify-content: space-between; margin-bottom: 4px; }
.card-title { font-weight: 700; font-size: 14px; color: #1e293b; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
.card-time { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.time-tag { font-size: 12px; font-weight: 700; color: #2563eb; background-color: #fff; padding: 2px 6px; border-radius: 4px; }
.card-location { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #64748b; margin-bottom: 12px; }

.attendees {
  display: flex; padding-left: 4px;
  .avatar {
    width: 24px; height: 24px; border-radius: 50%; border: 2px solid #fff;
    display: flex; align-items: center; justify-content: center;
    font-size: 10px; color: #fff; margin-left: -8px;
    &:first-child { margin-left: 0; }
    &.bg-blue { background-color: #3b82f6; }
    &.bg-orange { background-color: #f97316; }
    &.bg-slate { background-color: #e2e8f0; color: #64748b; }
  }
}

.todos-section { margin-top: 32px; padding-top: 24px; border-top: 1px solid #f1f5f9; }
.todo-item { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; cursor: pointer; &:hover .todo-text { color: #1677ff; } }
.todo-checkbox { width: 16px; height: 16px; border-radius: 4px; accent-color: #1677ff; cursor: pointer; }
.todo-text { font-size: 14px; color: #475569; }
.empty-state { font-size: 14px; color: #94a3b8; text-align: center; padding: 20px 0; }
</style>
