<template>
  <div class="workbench-schedule">
    <!-- 头部操作栏 -->
    <div class="schedule-header">
      <div class="header-left">
        <h2>日程</h2>
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          @change="handleDateChange"
        />
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新建日程</el-button>
      </div>
    </div>

    <!-- 日程视图切换 -->
    <div class="schedule-view-tabs">
      <div
        v-for="tab in viewTabs"
        :key="tab.key"
        class="view-tab"
        :class="{ active: currentView === tab.key }"
        @click="currentView = tab.key"
      >
        <el-icon><component :is="tab.icon" /></el-icon>
        <span>{{ tab.label }}</span>
      </div>
    </div>

    <!-- 日视图 -->
    <div v-if="currentView === 'day'" class="schedule-day-view">
      <div class="time-slots">
        <div
          v-for="hour in 24"
          :key="hour"
          class="time-slot"
          :class="{ 'has-event': hasEventAt(hour) }"
        >
          <div class="slot-time">{{ String(hour).padStart(2, '0') }}:00</div>
          <div class="slot-events">
            <div
              v-for="event in getEventsAt(hour)"
              :key="event.id"
              class="event-item"
              :style="{ backgroundColor: event.color }"
              @click="handleViewEvent(event)"
            >
              <div class="event-title">{{ event.title }}</div>
              <div class="event-time">{{ event.startTime }} - {{ event.endTime }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 周视图 -->
    <div v-if="currentView === 'week'" class="schedule-week-view">
      <div class="week-header">
        <div
          v-for="(day, index) in weekDays"
          :key="index"
          class="week-day"
          :class="{ 'is-today': day.isToday }"
        >
          <div class="day-name">{{ day.name }}</div>
          <div class="day-date">{{ day.date }}</div>
        </div>
      </div>
      <div class="week-body">
        <div class="week-time-column">
          <div v-for="hour in 24" :key="hour" class="hour-label">
            {{ String(hour).padStart(2, '0') }}:00
          </div>
        </div>
        <div class="week-events-column">
          <!-- 周视图内容 -->
        </div>
      </div>
    </div>

    <!-- 月视图 -->
    <div v-if="currentView === 'month'" class="schedule-month-view">
      <el-calendar v-model="selectedDate">
        <template #date-cell="{ data }">
          <div class="calendar-day">
            <div class="day-number">{{ data.day.split('-').slice(-1)[0] }}</div>
            <div class="day-events">
              <div
                v-for="event in getEventsForDate(data.day)"
                :key="event.id"
                class="mini-event"
                :style="{ backgroundColor: event.color }"
                @click="handleViewEvent(event)"
              >
                {{ event.title }}
              </div>
            </div>
          </div>
        </template>
      </el-calendar>
    </div>

    <!-- 列表视图 -->
    <div v-if="currentView === 'list'" class="schedule-list-view">
      <div class="list-sections">
        <div v-for="section in scheduleSections" :key="section.key" class="list-section">
          <div class="section-header">
            <h3>{{ section.title }}</h3>
            <span class="section-count">{{ section.events.length }}</span>
          </div>
          <div class="section-events">
            <div
              v-for="event in section.events"
              :key="event.id"
              class="list-event-item"
              @click="handleViewEvent(event)"
            >
              <div class="event-time">
                <div class="time-start">{{ event.startTime }}</div>
                <div class="time-end">{{ event.endTime }}</div>
              </div>
              <div class="event-bar" :style="{ backgroundColor: event.color }"></div>
              <div class="event-content">
                <div class="event-title">{{ event.title }}</div>
                <div class="event-meta">
                  <span v-if="event.location">
                    <el-icon><Location /></el-icon>
                    {{ event.location }}
                  </span>
                  <span v-if="event.participants">
                    <el-icon><User /></el-icon>
                    {{ event.participants }}人
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建/编辑日程对话框 -->
    <el-dialog
      v-model="showEventDialog"
      :title="isEditMode ? '编辑日程' : '新建日程'"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form ref="eventFormRef" :model="eventForm" :rules="eventFormRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="eventForm.title"
            placeholder="请输入日程标题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="日期" prop="date">
          <el-date-picker
            v-model="eventForm.date"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="时间" prop="timeRange">
          <el-time-picker
            v-model="eventForm.timeRange"
            is-range
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="HH:mm"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="地点" prop="location">
          <el-input v-model="eventForm.location" placeholder="请输入地点" clearable>
            <template #prefix>
              <el-icon><Location /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="参与人" prop="participants">
          <el-select
            v-model="eventForm.participants"
            multiple
            filterable
            allow-create
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

        <el-form-item label="提醒" prop="reminders">
          <el-select
            v-model="eventForm.reminders"
            multiple
            placeholder="选择提醒时间"
            style="width: 100%"
          >
            <el-option label="提前15分钟" :value="15" />
            <el-option label="提前30分钟" :value="30" />
            <el-option label="提前1小时" :value="60" />
            <el-option label="提前1天" :value="1440" />
          </el-select>
        </el-form-item>

        <el-form-item label="颜色" prop="color">
          <div class="color-picker">
            <div
              v-for="color in eventColors"
              :key="color"
              class="color-option"
              :class="{ selected: eventForm.color === color }"
              :style="{ backgroundColor: color }"
              @click="eventForm.color = color"
            ></div>
          </div>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="eventForm.remark"
            type="textarea"
            :rows="3"
            placeholder="添加备注..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEventDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveEvent">
          {{ isEditMode ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Location, User, Calendar, List, Grid } from '@element-plus/icons-vue'
import { getScheduleList, createSchedule, updateSchedule, deleteSchedule } from '@/api/im/workbench'

// State
const currentView = ref('day')
const selectedDate = ref(new Date())
const showEventDialog = ref(false)
const isEditMode = ref(false)
const saving = ref(false)
const eventFormRef = ref(null)

// 日程数据
const events = ref([])
const availableUsers = ref([
  { id: '1', name: '张三' },
  { id: '2', name: '李四' },
  { id: '3', name: '王五' },
])

// 表单数据
const eventForm = ref({
  id: null,
  title: '',
  date: new Date(),
  timeRange: null,
  location: '',
  participants: [],
  reminders: [15],
  color: '#0089FF',
  remark: '',
})

const eventFormRules = {
  title: [{ required: true, message: '请输入日程标题', trigger: 'blur' }],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择时间', trigger: 'change' }],
}

// 视图配置
const viewTabs = [
  { key: 'day', label: '日', icon: Calendar },
  { key: 'week', label: '周', icon: Calendar },
  { key: 'month', label: '月', icon: Calendar },
  { key: 'list', label: '列表', icon: List },
]

const eventColors = ['#0089FF', '#52C41A', '#FAAD14', '#F5222D', '#722ED1', '#13C2C2']

// 计算属性
const weekDays = computed(() => {
  const today = new Date()
  const monday = new Date(today)
  monday.setDate(today.getDate() - today.getDay() + 1)

  return Array.from({ length: 7 }, (_, i) => {
    const date = new Date(monday)
    date.setDate(monday.getDate() + i)
    return {
      name: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'][i],
      date: `${date.getMonth() + 1}/${date.getDate()}`,
      isToday: date.toDateString() === today.toDateString(),
    }
  })
})

const scheduleSections = computed(() => {
  const sections = {
    upcoming: { title: '即将开始', key: 'upcoming', events: [] },
    today: { title: '今天', key: 'today', events: [] },
    later: { title: '稍后', key: 'later', events: [] },
  }

  // 按时间分组
  events.value.forEach(event => {
    // 简化逻辑，实际应根据时间判断
    sections.today.events.push(event)
  })

  return Object.values(sections).filter(s => s.events.length > 0)
})

// 方法
const handleDateChange = () => {
  fetchEvents()
}

const handleAdd = () => {
  isEditMode.value = false
  eventForm.value = {
    id: null,
    title: '',
    date: selectedDate.value,
    timeRange: null,
    location: '',
    participants: [],
    reminders: [15],
    color: '#0089FF',
    remark: '',
  }
  showEventDialog.value = true
}

const handleViewEvent = event => {
  isEditMode.value = true
  eventForm.value = { ...event }
  showEventDialog.value = true
}

const handleDialogClose = () => {
  eventFormRef.value?.resetFields()
}

const handleSaveEvent = async () => {
  const valid = await eventFormRef.value?.validate()
  if (!valid) return

  saving.value = true

  try {
    // 格式化日期和时间
    const formatDateStr = date => {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }

    const startDateTime = new Date(eventForm.value.date)
    const endDateTime = new Date(eventForm.value.date)
    const [startHour, startMin] = eventForm.value.timeRange[0].split(':')
    const [endHour, endMin] = eventForm.value.timeRange[1].split(':')
    startDateTime.setHours(parseInt(startHour), parseInt(startMin), 0)
    endDateTime.setHours(parseInt(endHour), parseInt(endMin), 0)

    const eventData = {
      title: eventForm.value.title,
      startTime: startDateTime.toISOString().slice(0, 19).replace('T', ' '),
      endTime: endDateTime.toISOString().slice(0, 19).replace('T', ' '),
      location: eventForm.value.location,
      participants: eventForm.value.participants,
      reminderMinutes: eventForm.value.reminders[0] || 15,
      color: eventForm.value.color,
      description: eventForm.value.remark,
    }

    if (isEditMode.value) {
      // 调用更新API
      await updateSchedule(eventForm.value.id, eventData)
      ElMessage.success('日程已更新')

      // 更新本地列表
      const index = events.value.findIndex(e => e.id === eventForm.value.id)
      if (index !== -1) {
        events.value[index] = {
          ...events.value[index],
          ...eventForm.value,
          startTime: formatTime(eventForm.value.timeRange[0]),
          endTime: formatTime(eventForm.value.timeRange[1]),
        }
      }
    } else {
      // 调用创建API
      const { data } = await createSchedule(eventData)
      if (data.code === 200) {
        ElMessage.success('日程已创建')
        // 添加到本地列表
        events.value.push({
          id: data.data,
          ...eventForm.value,
          startTime: formatTime(eventForm.value.timeRange[0]),
          endTime: formatTime(eventForm.value.timeRange[1]),
        })
      }
    }

    showEventDialog.value = false
  } catch (error) {
    console.error('保存日程失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const hasEventAt = hour => {
  return getEventsAt(hour).length > 0
}

const getEventsAt = hour => {
  return events.value.filter(event => {
    const eventHour = parseInt(event.startTime.split(':')[0])
    return eventHour === hour
  })
}

const getEventsForDate = dateStr => {
  return events.value.filter(event => {
    return event.date && event.date.toDateString() === new Date(dateStr).toDateString()
  })
}

const formatTime = date => {
  if (!date) return ''
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

const fetchEvents = async () => {
  try {
    // 调用API获取日程列表
    // 计算当月开始和结束时间
    const year = selectedDate.value.getFullYear()
    const month = selectedDate.value.getMonth()
    const startTime = `${year}-${String(month + 1).padStart(2, '0')}-01 00:00:00`
    const endTime = `${year}-${String(month + 1).padStart(2, '0')}-31 23:59:59`

    const { data } = await getScheduleList({
      startTime,
      endTime,
      pageNum: 1,
      pageSize: 100,
    })

    if (data.code === 200 && data.data && data.data.records) {
      events.value = data.data.records.map(item => ({
        id: item.id,
        title: item.title,
        date: new Date(item.startTime),
        startTime: item.startTime ? item.startTime.split(' ')[1].slice(0, 5) : '00:00',
        endTime: item.endTime ? item.endTime.split(' ')[1].slice(0, 5) : '00:00',
        location: item.location || '',
        participants: item.participants || [],
        color: item.color || '#0089FF',
        remark: item.description || '',
      }))
    }
  } catch (error) {
    console.error('获取日程失败:', error)
    // 保持模拟数据作为备用
    events.value = [
      {
        id: '1',
        title: '项目会议',
        date: new Date(),
        startTime: '09:00',
        endTime: '10:30',
        location: '会议室A',
        participants: ['1', '2'],
        color: '#0089FF',
        remark: '讨论项目进度',
      },
      {
        id: '2',
        title: '团队建设',
        date: new Date(),
        startTime: '14:00',
        endTime: '16:00',
        location: '活动中心',
        participants: ['1', '2', '3'],
        color: '#52C41A',
        remark: '月度团建活动',
      },
    ]
  }
}

onMounted(() => {
  fetchEvents()
})
</script>

<style lang="scss" scoped>
.workbench-schedule {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.schedule-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-light);

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    h2 {
      margin: 0;
      font-size: 18px;
      font-weight: 500;
    }
  }
}

.schedule-view-tabs {
  display: flex;
  gap: 8px;
  padding: 12px 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.view-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  color: var(--el-text-color-secondary);
  transition: all 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.active {
    background: var(--el-color-primary-light-9);
    color: var(--el-color-primary);
  }
}

// 日视图
.schedule-day-view {
  flex: 1;
  overflow-y: auto;
}

.time-slots {
  display: flex;
  flex-direction: column;
}

.time-slot {
  display: flex;
  min-height: 60px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  &.has-event {
    background: var(--el-fill-color-extra-light);
  }
}

.slot-time {
  width: 70px;
  padding: 8px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  text-align: right;
  flex-shrink: 0;
}

.slot-events {
  flex: 1;
  padding: 4px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.event-item {
  padding: 8px 12px;
  border-radius: 6px;
  color: white;
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: translateX(4px);
  }

  .event-title {
    font-size: 14px;
    font-weight: 500;
  }

  .event-time {
    font-size: 12px;
    opacity: 0.9;
  }
}

// 周视图
.schedule-week-view {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.week-header {
  display: grid;
  grid-template-columns: 70px repeat(7, 1fr);
  border-bottom: 1px solid var(--el-border-color-light);
}

.week-day {
  padding: 12px 8px;
  text-align: center;

  &.is-today {
    background: var(--el-color-primary-light-9);
  }

  .day-name {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-bottom: 4px;
  }

  .day-date {
    font-size: 14px;
    font-weight: 500;
  }
}

.week-body {
  flex: 1;
  display: flex;
  overflow-y: auto;
}

.week-time-column {
  width: 70px;
  flex-shrink: 0;
}

.hour-label {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.week-events-column {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

// 月视图
.schedule-month-view {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

:deep(.el-calendar) {
  --el-calendar-cell-width: 80px;
  --el-calendar-border-color: var(--el-border-color-lighter);
}

.calendar-day {
  height: 80px;
  padding: 4px;

  .day-number {
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-primary);
  }

  .day-events {
    margin-top: 4px;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .mini-event {
    font-size: 11px;
    padding: 2px 6px;
    border-radius: 3px;
    color: white;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    cursor: pointer;
  }
}

// 列表视图
.schedule-list-view {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.list-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.list-section {
  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    h3 {
      margin: 0;
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-secondary);
    }

    .section-count {
      font-size: 12px;
      padding: 2px 8px;
      background: var(--el-fill-color-light);
      border-radius: 10px;
      color: var(--el-text-color-secondary);
    }
  }

  .section-events {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}

.list-event-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: white;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--el-color-primary);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.event-time {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 60px;

  .time-start {
    font-size: 16px;
    font-weight: 500;
    color: var(--el-text-color-primary);
  }

  .time-end {
    font-size: 12px;
    color: var(--el-text-color-placeholder);
  }
}

.event-bar {
  width: 4px;
  height: 40px;
  border-radius: 2px;
}

.event-content {
  flex: 1;

  .event-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    margin-bottom: 4px;
  }

  .event-meta {
    display: flex;
    gap: 16px;
    font-size: 12px;
    color: var(--el-text-color-secondary);

    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

// 颜色选择器
.color-picker {
  display: flex;
  gap: 12px;
}

.color-option {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;

  &:hover {
    transform: scale(1.1);
  }

  &.selected {
    border-color: var(--el-text-color-primary);
  }
}

@media (max-width: 768px) {
  .schedule-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;

    .header-left {
      justify-content: space-between;
    }

    .header-right {
      width: 100%;

      .el-button {
        width: 100%;
      }
    }
  }

  .schedule-view-tabs {
    overflow-x: auto;
  }

  .week-header,
  .week-events-column {
    font-size: 12px;
  }
}
</style>
