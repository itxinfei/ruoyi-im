<template>
  <div class="calendar-pc-container">
    <!-- 1. 左侧功能面板 (240px 固定) -->
    <aside class="calendar-aside">
      <div class="aside-top">
        <el-button type="primary" class="create-btn" @click="handleAddSchedule">
          <el-icon><Plus /></el-icon>
          <span>新建日程</span>
        </el-button>
      </div>

      <div class="aside-content scrollbar-none">
        <!-- 迷你日历：PC端通常使用标准紧凑型 -->
        <div class="mini-picker-wrapper">
          <el-date-picker
            v-model="currentDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            class="hidden-input"
            :teleported="false"
            popper-class="pc-mini-popper"
          />
          <!-- 模拟一个更专业的迷你日历视图 -->
          <div class="mini-cal-header">
            <span class="year-month">{{ formatYearMonth(currentDate) }}</span>
            <div class="nav-ops">
              <el-icon @click="prevMonth"><ArrowLeft /></el-icon>
              <el-icon @click="nextMonth"><ArrowRight /></el-icon>
            </div>
          </div>
          <div class="mini-cal-grid">
            <div v-for="d in ['一','二','三','四','五','六','日']" :key="d" class="week-label">{{ d }}</div>
            <div
              v-for="(day, idx) in monthDays"
              :key="idx"
              class="day-cell"
              :class="{ 
                'not-current': !day.isCurrentMonth, 
                'is-today': isToday(day.date),
                'is-active': isSameDay(day.date, currentDate)
              }"
              @click="currentDate = day.date; selectedDate = day.date"
            >
              {{ day.date.getDate() }}
            </div>
          </div>
        </div>

        <div class="divider"></div>

        <!-- 日历分类列表 -->
        <div class="calendar-list">
          <h4 class="list-title text-slate-400">日历列表</h4>
          <div v-for="cat in calendarCategories" :key="cat.name" class="list-item">
            <el-checkbox v-model="cat.enabled" />
            <div class="color-dot" :style="{ backgroundColor: cat.hex }"></div>
            <span class="name">{{ cat.name }}</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- 2. 主视图区域 -->
    <main class="calendar-main">
      <header class="main-header">
        <div class="left-tools">
          <h2 class="current-date-text">{{ viewTitle }}</h2>
          <div class="btn-group">
            <button class="tool-btn" @click="prev"><el-icon><ArrowLeft /></el-icon></button>
            <button class="tool-btn text-sm px-3" @click="goToToday">今天</button>
            <button class="tool-btn" @click="next"><el-icon><ArrowRight /></el-icon></button>
          </div>
        </div>

        <div class="right-tools">
          <div class="view-selector">
            <div 
              v-for="v in viewOptions" 
              :key="v.key" 
              class="view-tab"
              :class="{ active: currentView === v.key }"
              @click="currentView = v.key"
            >
              {{ v.label }}
            </div>
          </div>
          <el-button circle link @click="loadEvents"><el-icon><Refresh /></el-icon></el-button>
        </div>
      </header>

      <!-- 网格内容区 -->
      <div class="view-content scrollbar-thin">
        <!-- 月视图：PC端核心 -->
        <div v-if="currentView === 'month'" class="pc-month-view">
          <div class="grid-header">
            <div v-for="d in ['周一','周二','周三','周四','周五','周六','周日']" :key="d" class="header-cell">{{ d }}</div>
          </div>
          <div class="grid-body">
            <div
              v-for="(day, idx) in monthDays"
              :key="idx"
              class="body-cell"
              :class="{ 'not-curr': !day.isCurrentMonth, 'is-today': isToday(day.date), 'is-active': isSameDay(day.date, selectedDate) }"
              @click="selectedDate = day.date"
            >
              <div class="cell-head">
                <span class="day-num">{{ day.date.getDate() }}</span>
                <span v-if="isToday(day.date)" class="today-mark">今天</span>
              </div>
              <div class="cell-events">
                <div
                  v-for="event in getEventsForDay(day.date).slice(0, 4)"
                  :key="event.id"
                  class="event-line"
                  :class="[`line-${event.color}`]"
                  @click.stop="handleEventDetail(event)"
                >
                  <span class="dot"></span>
                  <span class="time">{{ event.startTime }}</span>
                  <span class="title">{{ event.title }}</span>
                </div>
                <div v-if="getEventsForDay(day.date).length > 4" class="more-link">
                  还有 {{ getEventsForDay(day.date).length - 4 }} 项...
                </div>
              </div>
              <!-- 悬浮新增按钮 -->
              <div class="add-hover-btn" @click.stop="handleGridClick(day.date, $event)">
                <el-icon><Plus /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <!-- 周视图：精细化时间轴 -->
        <div v-else-if="currentView === 'week'" class="pc-week-view">
          <div class="week-sticky-header">
            <div class="time-col-head"></div>
            <div v-for="day in weekHeaders" :key="day.date" class="day-col-head" :class="{ 'is-today': isToday(day.date) }">
              <span class="name">{{ day.name }}</span>
              <span class="date">{{ day.date.getDate() }}</span>
            </div>
          </div>
          <div class="week-body-container">
            <div class="time-axis">
              <div v-for="hour in hours" :key="hour" class="hour-label">
                {{ hour.toString().padStart(2, '0') }}:00
              </div>
            </div>
            <div class="week-grid">
              <div v-for="day in weekHeaders" :key="day.date" class="week-col" @click="handleGridClick(day.date, $event)">
                <div v-for="h in 12" :key="h" class="hour-block"></div>
                
                <!-- 日程卡片 -->
                <div
                  v-for="event in getEventsForDay(day.date)"
                  :key="event.id"
                  class="pc-event-card"
                  :class="[`card-${event.color}`]"
                  :style="getEventStyle(event)"
                  @click.stop="handleEventDetail(event)"
                >
                  <div class="card-inner">
                    <div class="title">{{ event.title }}</div>
                    <div class="time">{{ event.startTime }} - {{ event.endTime }}</div>
                  </div>
                </div>

                <!-- 时间线 -->
                <div v-if="isToday(day.date)" class="pc-now-line" :style="{ top: currentTimeTop + 'px' }">
                  <div class="marker"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 3. 右侧详情 (可选收起) -->
    <aside class="calendar-info-panel">
      <div class="info-header">
        <h3>日程概览</h3>
        <p class="text-xs text-slate-400">{{ formatDateDisplay(selectedDate) }}</p>
      </div>
      
      <div class="info-content scrollbar-thin">
        <div v-if="selectedDateEvents.length > 0" class="agenda-list">
          <div v-for="event in selectedDateEvents" :key="event.id" class="agenda-card" @click="handleEventDetail(event)">
            <div class="color-bar" :style="{ backgroundColor: getHexColor(event.color) }"></div>
            <div class="details">
              <div class="t">{{ event.title }}</div>
              <div class="m flex items-center gap-2">
                <el-icon><Timer /></el-icon>{{ event.startTime }} - {{ event.endTime }}
              </div>
              <div v-if="event.location" class="m flex items-center gap-2">
                <el-icon><Location /></el-icon>{{ event.location }}
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-agenda">
          <el-icon class="text-4xl text-slate-100 dark:text-slate-800 mb-2"><Calendar /></el-icon>
          <p>今日暂无安排</p>
        </div>
      </div>
    </aside>

    <!-- 弹窗部分保持一致 -->
    <el-dialog v-model="showEventDetail" title="日程详情" width="420px" class="pc-dialog">
      <div v-if="activeEvent" class="space-y-4">
        <h3 class="text-lg font-bold border-l-4 pl-3" :style="{ borderColor: getHexColor(activeEvent.color) }">{{ activeEvent.title }}</h3>
        <div class="grid grid-cols-2 gap-4 text-sm">
          <div class="bg-slate-50 dark:bg-slate-800 p-3 rounded">
            <p class="text-slate-400 mb-1">开始时间</p>
            <p class="font-medium">{{ activeEvent.startTime }}</p>
          </div>
          <div class="bg-slate-50 dark:bg-slate-800 p-3 rounded">
            <p class="text-slate-400 mb-1">结束时间</p>
            <p class="font-medium">{{ activeEvent.endTime }}</p>
          </div>
        </div>
        <div v-if="activeEvent.location" class="flex items-center gap-2 text-sm text-slate-600">
          <el-icon><Location /></el-icon> {{ activeEvent.location }}
        </div>
      </div>
    </el-dialog>

    <!-- 日程创建对话框 -->
    <ScheduleDialog
      v-model="showScheduleDialog"
      :default-date="scheduleDefaultDate"
      @saved="handleScheduleSaved"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { Plus, ArrowLeft, ArrowRight, Refresh, Timer, Location, Calendar } from '@element-plus/icons-vue'
import { getEventsByTimeRange, createEvent } from '@/api/im/schedule'
import { ElMessage } from 'element-plus'
import ScheduleDialog from '@/components/Chat/ScheduleDialog.vue'
import { formatTime } from '@/utils/format'

// --- PC 状态 ---
const currentDate = ref(new Date())
const selectedDate = ref(new Date())
const currentView = ref('month')
const showEventDetail = ref(false)
const activeEvent = ref(null)
const allEvents = ref([])
const currentTimeTop = ref(0)

const viewOptions = [
  { label: '月', key: 'month' },
  { label: '周', key: 'week' },
  { label: '日', key: 'day' }
]

const calendarCategories = ref([
  { name: '工作日程', hex: '#0089FF', enabled: true },
  { name: '会议安排', hex: '#722ed1', enabled: true },
  { name: '个人生活', hex: '#52c41a', enabled: true }
])

const hours = Array.from({ length: 13 }, (_, i) => i + 8) // 08:00 - 20:00
const START_HOUR = 8
const HOUR_HEIGHT = 80

// --- 逻辑与原代码保持同步并优化 ---
const loadEvents = async () => {
  try {
    const res = await getEventsByTimeRange(
      formatDateTime(getWeekStart(currentDate.value), '00:00:00'),
      formatDateTime(getWeekEnd(currentDate.value), '23:59:59')
    )
    if (res.code === 200 && res.data) {
      allEvents.value = res.data.map(e => ({
        id: e.id, title: e.title,
        startTime: formatTime(e.startTime), endTime: formatTime(e.endTime),
        date: formatDateOnly(new Date(e.startTime)),
        color: e.color?.toLowerCase() || 'blue',
        location: e.location, description: e.description
      }))
    }
  } catch (e) { console.error('Load failed', e) }
}

const formatYearMonth = (d) => `${d.getFullYear()}年${d.getMonth() + 1}月`
const formatDateTime = (d, t) => `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')} ${t}`
const formatDateOnly = (d) => `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')}`
const getWeekStart = (d) => { const curr = new Date(d); const day = curr.getDay(); const diff = curr.getDate() - day + (day === 0 ? -6 : 1); return new Date(curr.setDate(diff)) }
const getWeekEnd = (d) => { const s = getWeekStart(d); return new Date(s.getTime() + 6 * 86400000) }

// --- Computed ---
const viewTitle = computed(() => {
  if (currentView.value === 'month') return formatYearMonth(currentDate.value)
  const s = getWeekStart(currentDate.value), e = getWeekEnd(currentDate.value)
  return `${s.getMonth()+1}月${s.getDate()}日 - ${e.getMonth()+1}月${e.getDate()}日`
})

const weekHeaders = computed(() => {
  const s = getWeekStart(currentDate.value)
  const names = ['周一','周二','周三','周四','周五','周六','周日']
  return Array.from({length:7}, (_, i) => ({ name: names[i], date: new Date(s.getTime() + i*86400000) }))
})

const monthDays = computed(() => {
  const y = currentDate.value.getFullYear(), m = currentDate.value.getMonth()
  const first = new Date(y, m, 1); let start = first.getDay(); start = start === 0 ? 7 : start
  const days = []
  for (let i = start - 1; i > 0; i--) days.push({ date: new Date(y, m, 1 - i), isCurrentMonth: false })
  const last = new Date(y, m + 1, 0).getDate()
  for (let i = 1; i <= last; i++) days.push({ date: new Date(y, m, i), isCurrentMonth: true })
  while (days.length < 42) days.push({ date: new Date(y, m + 1, days.length - (last + start - 2)), isCurrentMonth: false })
  return days
})

const selectedDateEvents = computed(() => allEvents.value.filter(e => isSameDay(new Date(e.date), selectedDate.value)))

// --- Methods ---
const isToday = (d) => isSameDay(d, new Date())
const isSameDay = (d1, d2) => d1.getFullYear() === d2.getFullYear() && d1.getMonth() === d2.getMonth() && d1.getDate() === d2.getDate()
const getEventsForDay = (d) => allEvents.value.filter(e => isSameDay(new Date(e.date), d))
const getEventStyle = (e) => {
  const p = (t) => { const [h, m] = t.split(':').map(Number); return h + m/60 }
  const s = p(e.startTime), end = p(e.endTime)
  return { top: `${(s - START_HOUR) * 80}px`, height: `${(end - s) * 80}px` }
}
const getHexColor = (c) => ({ blue: '#0089FF', purple: '#722ed1', emerald: '#52c41a', orange: '#f97316', red: '#ff4d4f' }[c] || '#0089FF')
const formatDateDisplay = (d) => `${d.getMonth()+1}月${d.getDate()}日 ${['周日','周一','周二','周三','周四','周五','周六'][d.getDay()]}`

const updateTimeLine = () => {
  const n = new Date(); currentTimeTop.value = (n.getHours() + n.getMinutes()/60 - START_HOUR) * 80
}

const prev = () => { const d = new Date(currentDate.value); if(currentView.value === 'month') d.setMonth(d.getMonth()-1); else d.setDate(d.getDate()-7); currentDate.value = d }
const next = () => { const d = new Date(currentDate.value); if(currentView.value === 'month') d.setMonth(d.getMonth()+1); else d.setDate(d.getDate()+7); currentDate.value = d }
const prevMonth = () => { currentDate.value = new Date(currentDate.value.setMonth(currentDate.value.getMonth()-1)) }
const nextMonth = () => { currentDate.value = new Date(currentDate.value.setMonth(currentDate.value.getMonth()+1)) }
const goToToday = () => { currentDate.value = new Date(); selectedDate.value = new Date() }

const handleEventDetail = (e) => { activeEvent.value = e; showEventDetail.value = true }

// 日程对话框
const showScheduleDialog = ref(false)
const scheduleDefaultDate = ref(null)

const handleGridClick = (date, event) => {
  selectedDate.value = date
  scheduleDefaultDate.value = date
  showScheduleDialog.value = true
}

const handleAddSchedule = () => {
  scheduleDefaultDate.value = new Date()
  showScheduleDialog.value = true
}

const handleScheduleSaved = () => {
  loadEvents()
}

let timer = null
onMounted(() => { loadEvents(); updateTimeLine(); timer = setInterval(updateTimeLine, 60000) })
onUnmounted(() => clearInterval(timer))
watch(currentDate, loadEvents)
</script>

<style scoped lang="scss">
// ============================================================================
// PC 桌面端全局容器
// ============================================================================
.calendar-pc-container {
  display: flex;
  height: 100%;
  background: #fff;
  color: #334155;
  .dark & { background: #0f172a; color: #cbd5e1; }
}

// ============================================================================
// 左侧面板 - 规整、功能导向
// ============================================================================
.calendar-aside {
  width: 240px;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
  .dark & { border-color: #1e293b; background: #0f172a; }

  .aside-top { padding: 20px; }
  .create-btn {
    @apply w-full !h-10 !rounded-md !font-bold flex items-center justify-center gap-2;
    box-shadow: 0 4px 12px rgba(0, 137, 255, 0.15);
  }

  .mini-picker-wrapper {
    padding: 0 16px;
    .mini-cal-header {
      @apply flex items-center justify-between mb-3 px-2;
      .year-month { font-size: 13px; font-weight: 800; color: #1e293b; .dark & { color: #f1f5f9; } }
      .nav-ops { @apply flex gap-2 text-slate-400 cursor-pointer; i:hover { color: #0089FF; } }
    }
    .mini-cal-grid {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      gap: 2px;
      .week-label { @apply text-[10px] font-black text-slate-400 text-center py-1 uppercase; }
      .day-cell {
        @apply aspect-square flex items-center justify-center text-[11px] font-medium rounded cursor-pointer transition-all;
        &:hover { background: #e2e8f0; .dark & { background: #1e293b; } }
        &.not-current { @apply text-slate-300 dark:text-slate-700; }
        &.is-today { @apply text-primary font-bold; }
        &.is-active { @apply bg-primary text-white font-black shadow-md; }
      }
    }
  }

  .divider { @apply h-px bg-slate-200 dark:bg-slate-800 my-6 mx-4; }

  .calendar-list {
    padding: 0 20px;
    .list-title { @apply text-[10px] font-black uppercase tracking-widest mb-4; }
    .list-item {
      @apply flex items-center gap-3 mb-2 px-2 py-1.5 rounded cursor-pointer hover:bg-slate-200/50;
      .color-dot { @apply w-2 h-2 rounded-full; }
      .name { @apply text-sm font-medium text-slate-600 dark:text-slate-400; }
    }
  }
}

// ============================================================================
// 主内容区 - 高密度网格
// ============================================================================
.calendar-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.main-header {
  height: 56px;
  padding: 0 24px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  .dark & { border-color: #1e293b; }

  .left-tools {
    display: flex;
    align-items: center;
    gap: 20px;
    .current-date-text { font-size: 18px; font-weight: 800; color: #1e293b; .dark & { color: #f1f5f9; } }
    .tool-btn {
      @apply h-8 border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 flex items-center justify-center hover:bg-slate-50 transition-colors;
      &:first-child { border-radius: 4px 0 0 4px; }
      &:last-child { border-radius: 0 4px 4px 0; border-left: none; }
      &:nth-child(2) { border-left: none; }
    }
  }

  .view-selector {
    @apply flex bg-slate-100 dark:bg-slate-800 rounded-md p-1;
    .view-tab {
      @apply px-4 py-1 text-xs font-bold text-slate-500 cursor-pointer rounded transition-all;
      &.active { @apply bg-white dark:bg-slate-700 text-primary shadow-sm; }
    }
  }
}

.view-content { flex: 1; overflow: hidden; position: relative; }

// PC 月视图网格
.pc-month-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  .grid-header {
    @apply grid grid-cols-7 border-b border-slate-100 dark:border-slate-800;
    .header-cell { @apply py-2 text-center text-[11px] font-black text-slate-400 uppercase; }
  }
  .grid-body {
    @apply grid grid-cols-7 flex-1;
    .body-cell {
      @apply border-r border-b border-slate-100 dark:border-slate-800 p-2 flex flex-col relative transition-colors;
      &:hover {
        background: #f8fafc;
        .dark & {
          background: rgba(30, 41, 59, 0.3);
        }
        .add-hover-btn { opacity: 1; }
      }
      &.not-curr { background: #fafbfc; .dark & { background: rgba(15, 23, 42, 0.5); } .day-num { opacity: 0.3; } }
      &.is-active { background: rgba(0, 137, 255, 0.02); }
      
      .cell-head {
        @apply flex justify-between items-start mb-1;
        .day-num { @apply text-xs font-bold text-slate-500; }
        .today-mark { @apply text-[10px] bg-primary text-white px-1.5 rounded-sm font-black; }
      }
      
      .cell-events {
        @apply space-y-1 overflow-hidden;
        .event-line {
          @apply text-[11px] py-0.5 px-1.5 rounded cursor-pointer flex items-center gap-1.5 transition-all;
          .dot { @apply w-1.5 h-1.5 rounded-full flex-shrink-0; }
          .time { @apply font-bold opacity-60 flex-shrink-0; }
          .title { @apply truncate; }
          
          &.line-blue { @apply bg-blue-50 text-blue-600 dark:bg-blue-900/20; .dot { background: #3b82f6; } }
          &.line-purple { @apply bg-purple-50 text-purple-600 dark:bg-purple-900/20; .dot { background: #a855f7; } }
          &.line-emerald { @apply bg-emerald-50 text-emerald-600 dark:bg-emerald-900/20; .dot { background: #10b981; } }
          
          &:hover { @apply brightness-95 scale-[1.02]; }
        }
        .more-link { @apply text-[10px] font-bold text-slate-400 pl-1 mt-1; }
      }

      .add-hover-btn {
        @apply absolute bottom-2 right-2 w-6 h-6 rounded-full bg-primary text-white flex items-center justify-center opacity-0 transition-opacity cursor-pointer shadow-lg;
      }
    }
  }
}

// PC 周视图
.pc-week-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  .week-sticky-header {
    @apply flex border-b border-slate-100 dark:border-slate-800 bg-white/80 dark:bg-slate-900/80 backdrop-blur-md z-20;
    .time-col-head { width: 64px; }
    .day-col-head {
      @apply flex-1 py-3 flex flex-col items-center border-l border-slate-50 dark:border-slate-800;
      .name { @apply text-[10px] font-black text-slate-400 uppercase; }
      .date { @apply text-lg font-black text-slate-700 dark:text-slate-200; }
      &.is-today { .name, .date { color: #0089FF; } background: rgba(0, 137, 255, 0.02); }
    }
  }
  .week-body-container {
    @apply flex relative h-[1040px];
    .time-axis {
      width: 64px;
      .hour-label { @apply h-20 -mt-2 pr-3 text-right text-[10px] font-bold text-slate-300 dark:text-slate-600 uppercase; }
    }
    .week-grid {
      @apply flex-1 flex border-t border-slate-100 dark:border-slate-800;
      .week-col {
        @apply flex-1 relative border-l border-slate-50 dark:border-slate-800;
        .hour-block { @apply h-20 border-b border-slate-50 dark:border-slate-800/50; }
        &:hover { background: rgba(0, 137, 255, 0.01); }
      }
    }
  }
}

.pc-event-card {
  @apply absolute left-1 right-1 rounded p-2 text-[11px] font-medium cursor-pointer border-l-4 shadow-sm transition-all;
  &:hover { @apply shadow-md z-30 scale-[1.01] brightness-105; }
  &.card-blue { @apply bg-blue-50 border-blue-500 text-blue-700 dark:bg-blue-900/30; }
  &.card-purple { @apply bg-purple-50 border-purple-500 text-purple-700 dark:bg-purple-900/30; }
  &.card-emerald { @apply bg-emerald-50 border-emerald-500 text-emerald-700 dark:bg-emerald-900/30; }
}

.pc-now-line {
  @apply absolute left-0 right-0 flex items-center z-10 pointer-events-none;
  height: 2px; background: #ef4444;
  .marker { @apply w-2 h-2 rounded-full bg-red-500 -ml-1 shadow-sm; }
}

// ============================================================================
// 右侧概览面板
// ============================================================================
.calendar-info-panel {
  width: var(--dt-session-panel-width);
  border-left: 1px solid var(--dt-border-color);
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-card);
  .dark & { border-color: var(--dt-border-dark); background: var(--dt-bg-card-dark); }

  .info-header { padding: 24px; border-bottom: 1px solid #f1f5f9; .dark & { border-color: #1e293b; } h3 { @apply text-lg font-black; } }
  .info-content { flex: 1; padding: 20px; }
  
  .agenda-card {
    @apply mb-4 p-3 rounded-lg border border-slate-100 dark:border-slate-800 flex gap-3 cursor-pointer hover:shadow-md transition-all;
    .color-bar { @apply w-1 rounded-full flex-shrink-0; }
    .details {
      .t { @apply text-sm font-bold mb-1; }
      .m { @apply text-[11px] text-slate-400; }
    }
  }
}

.empty-agenda { @apply h-full flex flex-col items-center justify-center text-slate-300 dark:text-slate-700 font-bold text-sm; }

// ============================================================================
// 通用工具
// ============================================================================
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-thin::-webkit-scrollbar { width: 4px; }
.scrollbar-thin::-webkit-scrollbar-thumb { @apply bg-slate-200 dark:bg-slate-800 rounded-full; }

.pc-dialog {
  :deep(.el-dialog__header) { @apply font-bold border-b border-slate-100 dark:border-slate-800 p-4; }
  :deep(.el-dialog__body) { @apply p-6; }
}
</style>
