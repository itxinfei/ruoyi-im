<template>
  <div class="calendar-panel">
    <div class="calendar-main">
      <div class="calendar-header">
        <h2 class="calendar-title">{{ currentYear }}年 {{ monthNames[currentMonth] }}</h2>
        <div class="calendar-actions">
          <el-button text class="nav-btn" @click="previousMonth">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <el-button class="today-btn" @click="goToToday">今天</el-button>
          <el-button text class="nav-btn" @click="nextMonth">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
          <el-button type="primary" class="create-btn">
            <el-icon><Plus /></el-icon>
            新建日程
          </el-button>
        </div>
      </div>

      <div class="weekdays">
        <div
          v-for="day in weekDays"
          :key="day"
          class="weekday"
        >
          {{ day }}
        </div>
      </div>

      <div class="calendar-grid">
        <button
          v-for="(date, index) in calendarDays"
          :key="index"
          class="calendar-day"
          :class="{
            'empty': !date,
            'today': isToday(date),
            'selected': isSelected(date),
            'has-events': hasEvents(date)
          }"
          :disabled="!date"
          @click="date && selectDate(date)"
        >
          <span v-if="date" class="day-number">{{ date.getDate() }}</span>
          <div v-if="date && hasEvents(date)" class="event-dots">
            <div
              v-for="(event, idx) in getEventsForDate(date).slice(0, 3)"
              :key="idx"
              class="event-dot"
              :class="`event-${event.color}`"
            ></div>
          </div>
        </button>
      </div>
    </div>

    <div class="events-sidebar">
      <div class="events-header">
        <h3 class="events-title">{{ selectedDate.getMonth() + 1 }}月{{ selectedDate.getDate() }}日</h3>
        <p class="events-subtitle">
          {{ weekDays[selectedDate.getDay()] }}
          <span v-if="isToday(selectedDate)" class="today-label"> · 今天</span>
        </p>
      </div>

      <div class="events-body">
        <div v-if="selectedDateEvents.length > 0" class="events-list">
          <div
            v-for="event in selectedDateEvents"
            :key="event.id"
            class="event-card"
            :class="`event-${event.color}`"
          >
            <div class="event-header">
              <h4 class="event-title">{{ event.title }}</h4>
              <el-button text class="event-more">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
            </div>
            <div class="event-details">
              <div class="event-detail">
                <el-icon><Clock /></el-icon>
                <span>{{ event.time }} · {{ event.duration }}</span>
              </div>
              <div v-if="event.location" class="event-detail">
                <el-icon><Location /></el-icon>
                <span>{{ event.location }}</span>
              </div>
              <div v-if="event.attendees && event.attendees.length > 0" class="event-detail">
                <el-icon><User /></el-icon>
                <div class="attendees">
                  <div
                    v-for="(attendee, idx) in event.attendees.slice(0, 3)"
                    :key="idx"
                    class="attendee-avatar"
                  >
                    {{ attendee[0] }}
                  </div>
                  <span v-if="event.attendees.length > 3" class="attendee-more">
                    +{{ event.attendees.length - 3 }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="empty-events">
          <div class="empty-icon">
            <el-icon><Clock /></el-icon>
          </div>
          <p class="empty-text">暂无日程</p>
          <el-button class="empty-btn">
            <el-icon><Plus /></el-icon>
            创建日程
          </el-button>
        </div>
      </div>

      <div class="events-footer">
        <div class="stats-row">
          <span class="stats-label">本周日程</span>
          <el-badge :value="8" class="stats-badge" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  ArrowLeft,
  ArrowRight,
  Plus,
  Clock,
  Location,
  User,
  MoreFilled
} from '@element-plus/icons-vue'

const currentDate = ref(new Date(2026, 0, 17))
const selectedDate = ref(new Date(2026, 0, 17))

const weekDays = ['日', '一', '二', '三', '四', '五', '六']
const monthNames = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

const mockEvents = {
  '2026-01-17': [
    {
      id: '1',
      title: '产品设计评审会',
      time: '10:00',
      duration: '1小时',
      location: '会议室A',
      attendees: ['张伟', '李明', '王芳'],
      color: 'blue'
    },
    {
      id: '2',
      title: '技术方案讨论',
      time: '14:30',
      duration: '1.5小时',
      location: '会议室B',
      attendees: ['刘洋', '陈晨'],
      color: 'green'
    },
    {
      id: '3',
      title: '周会',
      time: '16:00',
      duration: '1小时',
      color: 'purple'
    }
  ],
  '2026-01-20': [
    {
      id: '4',
      title: '客户需求沟通',
      time: '09:30',
      duration: '2小时',
      location: '线上会议',
      color: 'orange'
    }
  ],
  '2026-01-22': [
    {
      id: '5',
      title: '项目复盘会议',
      time: '15:00',
      duration: '1小时',
      location: '会议室C',
      color: 'red'
    }
  ]
}

const currentYear = computed(() => currentDate.value.getFullYear())
const currentMonth = computed(() => currentDate.value.getMonth())

const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const daysInMonth = lastDay.getDate()
  const startingDayOfWeek = firstDay.getDay()

  const days = []

  for (let i = 0; i < startingDayOfWeek; i++) {
    days.push(null)
  }

  for (let i = 1; i <= daysInMonth; i++) {
    days.push(new Date(year, month, i))
  }

  return days
})

const selectedDateEvents = computed(() => {
  const dateStr = formatDate(selectedDate.value)
  return mockEvents[dateStr] || []
})

const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const isToday = (date) => {
  if (!date) return false
  const today = new Date(2026, 0, 17)
  return (
    date.getDate() === today.getDate() &&
    date.getMonth() === today.getMonth() &&
    date.getFullYear() === today.getFullYear()
  )
}

const isSelected = (date) => {
  if (!date) return false
  return (
    date.getDate() === selectedDate.value.getDate() &&
    date.getMonth() === selectedDate.value.getMonth() &&
    date.getFullYear() === selectedDate.value.getFullYear()
  )
}

const hasEvents = (date) => {
  if (!date) return false
  const dateStr = formatDate(date)
  return mockEvents[dateStr] && mockEvents[dateStr].length > 0
}

const getEventsForDate = (date) => {
  const dateStr = formatDate(date)
  return mockEvents[dateStr] || []
}

const previousMonth = () => {
  currentDate.value = new Date(currentYear.value, currentMonth.value - 1)
}

const nextMonth = () => {
  currentDate.value = new Date(currentYear.value, currentMonth.value + 1)
}

const goToToday = () => {
  currentDate.value = new Date(2026, 0, 17)
  selectedDate.value = new Date(2026, 0, 17)
}

const selectDate = (date) => {
  selectedDate.value = date
}
</script>

<style scoped>
.calendar-panel {
  flex: 1;
  display: flex;
  background: #fff;
  overflow: hidden;
}

.calendar-main {
  flex: 1;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.calendar-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.calendar-title {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.calendar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-btn {
  .el-icon {
    font-size: 16px;
  }
}

.today-btn {
  padding: 6px 12px;
  font-size: 14px;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 8px;
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  padding: 16px 24px 0;
}

.weekday {
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  color: #595959;
  padding: 8px 0;
}

.calendar-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: repeat(6, 1fr);
  gap: 8px;
  padding: 16px 24px 24px;
}

.calendar-day {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 8px;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-start;

  &:hover:not(.empty) {
    background-color: #f5f5f5;
  }

  &.empty {
    background-color: #fafafa;
    cursor: default;
  }

  &.today {
    border-color: #1890ff;
    background-color: #e6f7ff;
  }

  &.selected {
    box-shadow: 0 0 0 2px #1890ff;
  }

  &.has-events {
    padding-bottom: 12px;
  }
}

.day-number {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;

  .today & {
    color: #1890ff;
  }
}

.event-dots {
  display: flex;
  gap: 2px;
  flex-wrap: wrap;
}

.event-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;

  &.event-blue {
    background-color: #1890ff;
  }

  &.event-green {
    background-color: #52c41a;
  }

  &.event-purple {
    background-color: #722ed1;
  }

  &.event-orange {
    background-color: #fa8c16;
  }

  &.event-red {
    background-color: #f5222d;
  }
}

.events-sidebar {
  width: 384px;
  background-color: #f7f8fa;
  display: flex;
  flex-direction: column;
}

.events-header {
  background: #fff;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.events-title {
  font-size: 18px;
  font-weight: 500;
  color: #262626;
  margin: 0 0 4px 0;
}

.events-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;

  .today-label {
    color: #1890ff;
  }
}

.events-body {
  flex: 1;
  padding: 16px 24px;
  overflow-y: auto;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.event-card {
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid;
  cursor: pointer;
  transition: box-shadow 0.2s ease;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  &.event-blue {
    background-color: #e6f7ff;
    border-left-color: #1890ff;
    color: #0958d9;
  }

  &.event-green {
    background-color: #f6ffed;
    border-left-color: #52c41a;
    color: #389e0d;
  }

  &.event-purple {
    background-color: #f9f0ff;
    border-left-color: #722ed1;
    color: #531dab;
  }

  &.event-orange {
    background-color: #fff7e6;
    border-left-color: #fa8c16;
    color: #d46b08;
  }

  &.event-red {
    background-color: #fff1f0;
    border-left-color: #f5222d;
    color: #cf1322;
  }
}

.event-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 8px;
}

.event-title {
  font-size: 14px;
  font-weight: 500;
  margin: 0;
}

.event-more {
  padding: 4px;
  margin-top: -4px;
  margin-right: -4px;

  .el-icon {
    font-size: 16px;
  }
}

.event-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
}

.event-detail {
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    font-size: 16px;
    flex-shrink: 0;
  }

  span {
    flex: 1;
  }
}

.attendees {
  display: flex;
  align-items: center;
  gap: 4px;
}

.attendee-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
}

.attendee-more {
  font-size: 12px;
  margin-left: 4px;
}

.empty-events {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
  text-align: center;
}

.empty-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;

  .el-icon {
    font-size: 32px;
    color: #d9d9d9;
  }
}

.empty-text {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0 0 16px 0;
}

.empty-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.events-footer {
  background: #fff;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.stats-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
}

.stats-label {
  color: #595959;
}

.stats-badge {
  :deep(.el-badge__content) {
    background-color: #f0f0f0;
    color: #595959;
  }
}
</style>
