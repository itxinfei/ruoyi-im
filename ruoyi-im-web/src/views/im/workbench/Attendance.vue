<template>
  <div class="workbench-attendance">
    <!-- 打卡头部 -->
    <div class="attendance-header">
      <div class="date-info">
        <div class="date-text">{{ currentDate }}</div>
        <div class="time-text">{{ currentTime }}</div>
        <div class="weekday-text">{{ currentWeekday }}</div>
      </div>
      <div class="attendance-status">
        <div v-if="todayRecord" class="status-recorded">
          <el-icon class="status-icon"><Select /></el-icon>
          <span>今日已打卡</span>
        </div>
        <div v-else class="status-pending">
          <el-icon class="status-icon"><Clock /></el-icon>
          <span>等待打卡</span>
        </div>
      </div>
    </div>

    <!-- 打卡操作区 -->
    <div class="attendance-actions">
      <!-- 上班打卡 -->
      <div
        class="attendance-card"
        :class="{ 'clocked-in': todayRecord?.clockInTime, disabled: todayRecord?.clockInTime }"
      >
        <div class="card-icon">
          <el-icon :size="32"><OfficeBuilding /></el-icon>
        </div>
        <div class="card-info">
          <div class="card-title">上班打卡</div>
          <div class="card-time">
            <span v-if="todayRecord?.clockInTime">{{ todayRecord.clockInTime }}</span>
            <span v-else class="time-placeholder">09:00</span>
          </div>
          <div class="card-location">
            <el-icon><Location /></el-icon>
            <span v-if="clockInLocation">{{ clockInLocation }}</span>
            <span v-else>位置未设置</span>
          </div>
        </div>
        <el-button
          v-if="!todayRecord?.clockInTime"
          type="primary"
          size="large"
          :loading="clockingIn"
          @click="handleClockIn"
        >
          打卡
        </el-button>
        <el-button v-else disabled size="large"> 已打卡 </el-button>
      </div>

      <!-- 下班打卡 -->
      <div
        class="attendance-card"
        :class="{ 'clocked-out': todayRecord?.clockOutTime, disabled: !todayRecord?.clockInTime }"
      >
        <div class="card-icon">
          <el-icon :size="32"><HomeFilled /></el-icon>
        </div>
        <div class="card-info">
          <div class="card-title">下班打卡</div>
          <div class="card-time">
            <span v-if="todayRecord?.clockOutTime">{{ todayRecord.clockOutTime }}</span>
            <span v-else class="time-placeholder">18:00</span>
          </div>
          <div class="card-location">
            <el-icon><Location /></el-icon>
            <span v-if="clockOutLocation">{{ clockOutLocation }}</span>
            <span v-else>位置未设置</span>
          </div>
        </div>
        <el-button
          v-if="!todayRecord?.clockOutTime && todayRecord?.clockInTime"
          type="primary"
          size="large"
          :loading="clockingOut"
          @click="handleClockOut"
        >
          打卡
        </el-button>
        <el-button v-else-if="todayRecord?.clockOutTime" disabled size="large"> 已打卡 </el-button>
        <el-button v-else disabled size="large"> 请先上班打卡 </el-button>
      </div>
    </div>

    <!-- 本周打卡记录 -->
    <div class="attendance-week">
      <div class="week-header">
        <h3>本周打卡</h3>
        <el-tag :type="weekSummary.status">{{ weekSummary.text }}</el-tag>
      </div>
      <div class="week-days">
        <div
          v-for="(day, index) in weekDays"
          :key="index"
          class="day-item"
          :class="{
            'is-today': day.isToday,
            'is-full': day.isFull,
            'is-half': day.isHalf,
            'is-absent': day.isAbsent,
            'is-leave': day.isLeave,
            'is-future': day.isFuture,
          }"
        >
          <div class="day-name">{{ day.name }}</div>
          <div class="day-date">{{ day.date }}</div>
          <div class="day-status">
            <span v-if="day.isFull">正常</span>
            <span v-else-if="day.isHalf">迟到</span>
            <span v-else-if="day.isAbsent">缺卡</span>
            <span v-else-if="day.isLeave">请假</span>
            <span v-else-if="day.isFuture">-</span>
            <span v-else>待打卡</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 考勤统计 -->
    <div class="attendance-stats">
      <h3>本月考勤统计</h3>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-label">出勤天数</div>
          <div class="stat-value primary">{{ monthStats.attendance }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">迟到次数</div>
          <div class="stat-value warning">{{ monthStats.late }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">早退次数</div>
          <div class="stat-value info">{{ monthStats.early }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">缺卡次数</div>
          <div class="stat-value danger">{{ monthStats.absent }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { OfficeBuilding, HomeFilled, Location, Clock, Select } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  userId: {
    type: [String, Number],
    default: null,
  },
})

// State
const currentDate = ref('')
const currentTime = ref('')
const currentWeekday = ref('')
const todayRecord = ref(null)
const weekDays = ref([])
const monthStats = ref({
  attendance: 0,
  late: 0,
  early: 0,
  absent: 0,
})
const clockInLocation = ref('公司')
const clockOutLocation = ref('公司')
const clockingIn = ref(false)
const clockingOut = ref(false)

let timeTimer = null

// 计算属性
const weekSummary = computed(() => {
  const days = weekDays.value
  const futureDays = days.filter(d => d.isFuture).length
  const completedDays = days.length - futureDays
  const fullDays = days.filter(d => d.isFull).length
  const halfDays = days.filter(d => d.isHalf).length

  if (completedDays === 0) {
    return { text: '待统计', status: 'info' }
  }

  const rate = (fullDays * 2 + halfDays) / (completedDays * 2)
  if (rate >= 0.9) {
    return { text: '优秀', status: 'success' }
  } else if (rate >= 0.7) {
    return { text: '良好', type: 'warning' }
  } else {
    return { text: '需努力', status: 'danger' }
  }
})

// 方法
const updateDateTime = () => {
  const now = new Date()

  // 更新日期
  currentDate.value = `${now.getMonth() + 1}月${now.getDate()}日`

  // 更新时间
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${hours}:${minutes}:${seconds}`

  // 更新星期
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  currentWeekday.value = weekdays[now.getDay()]
}

const fetchTodayRecord = async () => {
  try {
    // TODO: 调用API获取今日打卡记录
    // const response = await getTodayAttendance()
    // todayRecord.value = response.data
    // 模拟数据
    // todayRecord.value = null
  } catch (error) {
    console.error('获取打卡记录失败:', error)
  }
}

const fetchWeekRecords = async () => {
  try {
    // TODO: 调用API获取本周打卡记录
    // const response = await getWeekAttendance()
    // weekDays.value = response.data

    // 模拟数据
    const today = new Date()
    const monday = new Date(today)
    monday.setDate(today.getDate() - today.getDay() + 1)

    weekDays.value = Array.from({ length: 7 }, (_, i) => {
      const date = new Date(monday)
      date.setDate(monday.getDate() + i)
      const isToday = date.toDateString() === today.toDateString()
      const isFuture = date > today

      return {
        name: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'][i],
        date: `${date.getMonth() + 1}/${date.getDate()}`,
        isToday,
        isFuture,
        isFull: !isToday && !isFuture && Math.random() > 0.2,
        isHalf: !isToday && !isFuture && Math.random() <= 0.2,
        isAbsent: false,
        isLeave: false,
      }
    })
  } catch (error) {
    console.error('获取本周记录失败:', error)
  }
}

const fetchMonthStats = async () => {
  try {
    // TODO: 调用API获取本月考勤统计
    // const response = await getMonthAttendance()
    // monthStats.value = response.data

    // 模拟数据
    monthStats.value = {
      attendance: 18,
      late: 1,
      early: 0,
      absent: 0,
    }
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

const handleClockIn = async () => {
  clockingIn.value = true

  try {
    // TODO: 调用API上班打卡
    // await clockIn({
    //   location: clockInLocation.value,
    //   latitude: '',
    //   longitude: ''
    // })

    ElMessage.success('上班打卡成功')

    // 更新状态
    todayRecord.value = {
      clockInTime: currentTime.value,
      clockInLocation: clockInLocation.value,
    }

    // 刷新本周记录
    await fetchWeekRecords()
  } catch (error) {
    console.error('上班打卡失败:', error)
    ElMessage.error('上班打卡失败，请重试')
  } finally {
    clockingIn.value = false
  }
}

const handleClockOut = async () => {
  clockingOut.value = true

  try {
    // TODO: 调用API下班打卡
    // await clockOut({
    //   location: clockOutLocation.value,
    //   latitude: '',
    //   longitude: ''
    // })

    ElMessage.success('下班打卡成功')

    // 更新状态
    todayRecord.value = {
      ...todayRecord.value,
      clockOutTime: currentTime.value,
      clockOutLocation: clockOutLocation.value,
    }

    // 刷新本周记录
    await fetchWeekRecords()
  } catch (error) {
    console.error('下班打卡失败:', error)
    ElMessage.error('下班打卡失败，请重试')
  } finally {
    clockingOut.value = false
  }
}

// 生命周期
onMounted(() => {
  updateDateTime()
  timeTimer = setInterval(updateDateTime, 1000)

  fetchTodayRecord()
  fetchWeekRecords()
  fetchMonthStats()
})

onUnmounted(() => {
  if (timeTimer) {
    clearInterval(timeTimer)
  }
})
</script>

<style lang="scss" scoped>
.workbench-attendance {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

// 打卡头部
.attendance-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
  margin-bottom: 20px;
}

.date-info {
  .date-text {
    font-size: 16px;
    opacity: 0.9;
    margin-bottom: 4px;
  }

  .time-text {
    font-size: 36px;
    font-weight: 600;
    margin-bottom: 4px;
    font-variant-numeric: tabular-nums;
  }

  .weekday-text {
    font-size: 14px;
    opacity: 0.8;
  }
}

.attendance-status {
  text-align: right;

  .status-recorded,
  .status-pending {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
  }

  .status-icon {
    font-size: 20px;
  }
}

// 打卡操作区
.attendance-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.attendance-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  border: 1px solid var(--el-border-color-light);
  transition: all 0.3s;

  &:hover {
    border-color: var(--el-color-primary);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  }

  &.clocked-in,
  &.clocked-out {
    background: var(--el-fill-color-light);
  }

  &.disabled {
    opacity: 0.6;
  }
}

.card-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-light);
  border-radius: 12px;
  color: var(--el-color-primary);
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}

.card-time {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;

  .time-placeholder {
    color: var(--el-text-color-placeholder);
  }
}

.card-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

// 本周打卡
.attendance-week {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid var(--el-border-color-light);
}

.week-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  h3 {
    margin: 0;
    font-size: 16px;
    color: var(--el-text-color-primary);
  }
}

.week-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.day-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  border-radius: 8px;
  transition: all 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.is-today {
    background: var(--el-color-primary-light-9);
    border: 1px solid var(--el-color-primary-light-7);
  }

  &.is-full .day-status {
    color: var(--el-color-success);
  }

  &.is-half .day-status {
    color: var(--el-color-warning);
  }

  &.is-absent .day-status {
    color: var(--el-color-danger);
  }

  &.is-leave .day-status {
    color: var(--el-text-color-secondary);
  }

  &.is-future {
    opacity: 0.5;
  }
}

.day-name {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}

.day-date {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.day-status {
  font-size: 12px;
  color: var(--el-color-info);
}

// 考勤统计
.attendance-stats {
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--el-border-color-light);

  h3 {
    margin: 0 0 16px 0;
    font-size: 16px;
    color: var(--el-text-color-primary);
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;

  &.primary {
    color: var(--el-color-primary);
  }

  &.warning {
    color: var(--el-color-warning);
  }

  &.info {
    color: var(--el-color-info);
  }

  &.danger {
    color: var(--el-color-danger);
  }
}

@media (max-width: 768px) {
  .attendance-actions {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .week-days {
    gap: 4px;
  }

  .day-item {
    padding: 8px 4px;
  }

  .day-name {
    font-size: 10px;
  }

  .day-date {
    font-size: 12px;
  }

  .day-status {
    font-size: 10px;
  }
}
</style>
