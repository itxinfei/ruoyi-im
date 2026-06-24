<template>
  <div class="cal-premium-v3">
    <!-- 1. 左侧二级侧栏 (240px) -->
    <aside class="cal-sidebar-v3">
      <div class="sidebar-top">
        <el-button type="primary" class="create-event-btn" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          <span>创建日程</span>
        </el-button>
      </div>

      <!-- 迷你日历 (对齐钉钉 8.2 极简风格) -->
      <div class="mini-datepicker-v3">
        <div class="datepicker-header">
          <span class="month-label">{{ miniCalMonth }}</span>
          <div class="nav-btns">
            <el-icon @click="prevMonth"><ArrowLeft /></el-icon>
            <el-icon @click="nextMonth"><ArrowRight /></el-icon>
          </div>
        </div>
        <div class="datepicker-grid">
          <span v-for="d in ['日','一','二','三','四','五','六']" :key="d" class="week-label">{{ d }}</span>
          <div 
            v-for="day in calendarDays" 
            :key="day.date" 
            class="day-cell"
            :class="{ 
              today: day.isToday, 
              selected: day.date === selectedDateStr,
              'other-month': day.isOtherMonth 
            }"
            @click="selectDate(day)"
          >
            {{ day.dayNum }}
            <div v-if="day.hasEvent" class="event-indicator"></div>
          </div>
        </div>
      </div>

      <div class="sidebar-divider"></div>

      <!-- 日历订阅 -->
      <div class="calendar-list">
        <h4 class="sub-title">我的日历</h4>
        <div v-for="c in categories" :key="c.id" class="cal-sub-item">
          <el-checkbox v-model="c.active">
            <span class="color-dot" :style="{ background: c.color }"></span>
            <span class="label">{{ c.label }}</span>
          </el-checkbox>
        </div>
      </div>

      <!-- 即将到来的日程提醒 -->
      <div class="upcoming-events" v-if="upcomingEvents.length">
        <div class="sidebar-divider" style="margin: 12px 16px;"></div>
        <h4 class="sub-title">即将到来</h4>
        <div v-for="event in upcomingEvents" :key="event.id" class="upcoming-item" @click="selectEvent(event)">
          <div class="upcoming-dot" :style="{ background: event.color }"></div>
          <div class="upcoming-info">
            <span class="upcoming-title">{{ event.title }}</span>
            <span class="upcoming-time">{{ formatEventTime(event) }}</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- 2. 主视图区 (对齐大厂：动态视图切换) -->
    <main class="cal-main-content">
      <header class="cal-view-header">
        <div class="header-left">
          <el-button-group>
            <el-button size="small" @click="goToday">今天</el-button>
            <el-button size="small" :icon="ArrowLeft" @click="prevPeriod" />
            <el-button size="small" :icon="ArrowRight" @click="nextPeriod" />
          </el-button-group>
          <h2 class="view-title-text">{{ viewTitle }}</h2>
        </div>
        <div class="header-right">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button value="day">日</el-radio-button>
            <el-radio-button value="week">周</el-radio-button>
            <el-radio-button value="month">月</el-radio-button>
          </el-radio-group>
        </div>
      </header>

      <!-- 时间网格主区 -->
      <div class="view-body custom-scrollbar">
        <!-- 以月视图为例：实现"灰底白卡"工业感 -->
        <div v-if="viewMode === 'month'" class="month-view-grid">
          <div v-for="d in ['周日','周一','周二','周三','周四','周五','周六']" :key="d" class="grid-header-cell">{{ d }}</div>
          <div 
            v-for="day in monthGridDays" 
            :key="day.date" 
            class="grid-day-cell"
            :class="{ 'is-other': day.isOtherMonth }"
          >
            <div class="cell-top">
              <span class="day-num" :class="{ isToday: day.isToday }">{{ day.dayNum }}</span>
              <span v-if="day.lunar" class="lunar-text">{{ day.lunar }}</span>
            </div>
            <div class="cell-events">
              <div 
                v-for="ev in day.events" 
                :key="ev.id" 
                class="event-pill"
                :style="{ borderLeftColor: ev.color, background: ev.bgColor }"
                @click.stop="selectEvent(ev)"
              >
                {{ ev.title }}
                <el-icon v-if="ev.recurrence" class="recurrence-icon"><RefreshRight /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 创建日程对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建日程" width="480px" :close-on-click-modal="false">
      <el-form :model="eventForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="eventForm.title" placeholder="请输入日程标题" />
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="eventForm.date" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="eventForm.category" placeholder="选择分类" style="width: 100%">
            <el-option label="工作" value="work" />
            <el-option label="会议" value="meeting" />
            <el-option label="个人" value="personal" />
          </el-select>
        </el-form-item>
        <el-form-item label="重复">
          <el-select v-model="eventForm.recurrence" placeholder="不重复" style="width: 100%" clearable>
            <el-option label="不重复" value="" />
            <el-option label="每天" value="daily" />
            <el-option label="每周" value="weekly" />
            <el-option label="每月" value="monthly" />
            <el-option label="每年" value="yearly" />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒">
          <el-select v-model="eventForm.reminder" placeholder="选择提醒时间" style="width: 100%">
            <el-option label="不提醒" value="none" />
            <el-option label="开始时" value="at-start" />
            <el-option label="5分钟前" value="5min" />
            <el-option label="15分钟前" value="15min" />
            <el-option label="30分钟前" value="30min" />
            <el-option label="1小时前" value="1hour" />
            <el-option label="1天前" value="1day" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="eventForm.description" type="textarea" :rows="3" placeholder="添加备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateEvent">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="js">
import { ref, computed } from 'vue'
import { Plus, ArrowLeft, ArrowRight, RefreshRight } from '@element-plus/icons-vue'

const viewMode = ref('month')
const miniCalMonth = ref('2026年4月')
const viewTitle = ref('2026年4月')
const selectedDateStr = ref('2026-04-18')
const showCreateDialog = ref(false)

const categories = ref([
  { id: 1, label: '工作', color: 'var(--dt-brand-color)', active: true },
  { id: 2, label: '会议', color: 'var(--dt-success-color)', active: true },
  { id: 3, label: '个人', color: 'var(--dt-warning-color)', active: true }
])

// 创建日程表单
const eventForm = ref({
  title: '',
  date: new Date(),
  category: 'work',
  recurrence: '',
  reminder: 'at-start',
  description: ''
})

// 模拟数据 - 包含重复日程
const allEvents = ref([
  { id: 1, title: 'IM界面纠偏评审', date: '2026-04-18', color: 'var(--dt-brand-color)', bgColor: 'var(--dt-brand-bg)', category: 'work', recurrence: 'weekly', reminder: '15min' },
  { id: 2, title: '每日站会', date: '2026-04-18', color: 'var(--dt-success-color)', bgColor: 'var(--dt-success-bg)', category: 'meeting', recurrence: 'daily', reminder: '5min' },
  { id: 3, title: '产品需求评审', date: '2026-04-20', color: 'var(--dt-warning-color)', bgColor: 'var(--dt-warning-bg)', category: 'meeting', recurrence: '', reminder: '30min' },
  { id: 4, title: '项目周报', date: '2026-04-25', color: 'var(--dt-brand-color)', bgColor: 'var(--dt-brand-bg)', category: 'work', recurrence: 'weekly', reminder: '1hour' }
])

const monthGridDays = ref(Array.from({ length: 35 }, (_, i) => {
  const dayNum = (i % 31) + 1
  const dateStr = `2026-04-${dayNum}`
  return {
    date: dateStr,
    dayNum,
    isOtherMonth: i >= 30,
    isToday: i === 17,
    events: allEvents.value.filter(e => e.date === dateStr)
  }
}))

// 即将到来的日程（未来7天）
const upcomingEvents = computed(() => {
  const today = new Date()
  const nextWeek = new Date(today)
  nextWeek.setDate(nextWeek.getDate() + 7)
  
  return allEvents.value
    .filter(e => {
      const eventDate = new Date(e.date)
      return eventDate >= today && eventDate <= nextWeek
    })
    .sort((a, b) => new Date(a.date) - new Date(b.date))
    .slice(0, 5)
})

const formatEventTime = (event) => {
  const date = new Date(event.date)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}

const selectDate = (day) => {
  selectedDateStr.value = day.date
}

const selectEvent = (event) => {
  console.log('Selected event:', event)
}

const prevMonth = () => {}
const nextMonth = () => {}
const goToday = () => {}
const prevPeriod = () => {}
const nextPeriod = () => {}

const handleCreateEvent = () => {
  if (!eventForm.value.title) return
  
  const newEvent = {
    id: Date.now(),
    title: eventForm.value.title,
    date: eventForm.value.date?.toISOString().split('T')[0] || new Date().toISOString().split('T')[0],
    color: eventForm.value.category === 'work' ? 'var(--dt-brand-color)' : 
           eventForm.value.category === 'meeting' ? 'var(--dt-success-color)' : 'var(--dt-warning-color)',
    bgColor: eventForm.value.category === 'work' ? 'var(--dt-brand-bg)' : 
             eventForm.value.category === 'meeting' ? 'var(--dt-success-bg)' : 'var(--dt-warning-bg)',
    category: eventForm.value.category,
    recurrence: eventForm.value.recurrence,
    reminder: eventForm.value.reminder
  }
  
  allEvents.value.push(newEvent)
  showCreateDialog.value = false
  
  // 重置表单
  eventForm.value = {
    title: '',
    date: new Date(),
    category: 'work',
    recurrence: '',
    reminder: 'at-start',
    description: ''
  }
}
</script>

<style scoped lang="scss">
.cal-premium-v3 { display: flex; height: 100%; background: var(--dt-bg-card); overflow: hidden; }

.cal-sidebar-v3 {
  width: 240px; background: var(--dt-bg-body); border-right: 1px solid var(--dt-border-light);
  display: flex; flex-direction: column;
  .sidebar-top { padding: 20px 16px; .create-event-btn { width: 100%; height: 36px; font-weight: 600; border-radius: var(--dt-radius-lg); } }
}

/* 🏁 纠偏：迷你日历对齐钉钉 8.2 */
.mini-datepicker-v3 {
  padding: 0 16px 20px;
  .datepicker-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
    padding: 0 4px; .month-label { font-size: 14px; font-weight: 700; color: var(--dt-text-primary); }
    .nav-btns { display: flex; gap: 12px; color: var(--dt-text-tertiary); cursor: pointer; font-size: 14px; }
  }
  .datepicker-grid {
    display: grid; grid-template-columns: repeat(7, 1fr); gap: 2px;
    .week-label { font-size: 11px; color: var(--dt-text-tertiary); text-align: center; height: 24px; }
    .day-cell {
      aspect-ratio: 1; @include flex-center; font-size: 12px; border-radius: 50%; cursor: pointer;
      position: relative; transition: var(--dt-transition-fast); color: var(--dt-text-primary);
      &:hover { background: var(--dt-brand-bg); }
      &.today { background: var(--dt-brand-color); color: var(--dt-text-white); font-weight: 700; }
      &.selected:not(.today) { background: var(--dt-bg-card); border: 1px solid var(--dt-brand-color); color: var(--dt-brand-color); }
      &.other-month { color: var(--dt-text-quaternary); }
      .event-indicator { position: absolute; bottom: 4px; width: 4px; height: 4px; background: var(--dt-success-color); border-radius: 50%; }
    }
  }
}

.sidebar-divider { height: 1px; background: var(--dt-border-light); margin: 0 16px; }

/* 即将到来的日程 */
.upcoming-events {
  .sub-title { font-size: 12px; color: var(--dt-text-tertiary); padding: 8px 16px; margin: 0; }
  .upcoming-item {
    display: flex; align-items: center; gap: 10px; padding: 8px 16px; cursor: pointer;
    transition: background var(--dt-transition-fast);
    &:hover { background: var(--dt-bg-hover); }
  }
  .upcoming-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
  .upcoming-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
  .upcoming-title { font-size: 13px; color: var(--dt-text-primary); @include text-ellipsis; }
  .upcoming-time { font-size: 11px; color: var(--dt-text-tertiary); }
}

.cal-main-content { flex: 1; display: flex; flex-direction: column; background: var(--dt-bg-card); }

.cal-view-header {
  height: var(--dt-header-height); padding: 0 24px; border-bottom: 1px solid var(--dt-border-light);
  @include flex-between;
  .header-left { display: flex; align-items: center; gap: 20px; .view-title-text { font-size: 18px; font-weight: 700; } }
}

.view-body { flex: 1; overflow-y: auto; background: var(--dt-bg-body); }

/* 🏁 纠偏：日历主网格工业化对比 */
.month-view-grid {
  display: grid; grid-template-columns: repeat(7, 1fr); height: 100%; min-height: 100%;
  .grid-header-cell { height: 32px; background: var(--dt-bg-card); border-bottom: 1px solid var(--dt-border-light); @include flex-center; font-size: 12px; color: var(--dt-text-tertiary); }
  .grid-day-cell {
    background: var(--dt-bg-card); border-right: 1px solid var(--dt-border-light); border-bottom: 1px solid var(--dt-border-light); padding: 8px;
    display: flex; flex-direction: column; gap: 6px; transition: var(--dt-transition-fast);
    &:hover { background: var(--dt-bg-hover); }
    &.is-other { background: var(--dt-bg-body); .day-num { color: var(--dt-text-quaternary); } }
    .cell-top { display: flex; justify-content: space-between; align-items: center;
      .day-num { width: 24px; height: 24px; @include flex-center; font-size: 13px; border-radius: 50%; &.isToday { background: var(--dt-brand-color); color: var(--dt-text-white); } }
      .lunar-text { font-size: 10px; color: var(--dt-text-quaternary); }
    }
    .cell-events { display: flex; flex-direction: column; gap: 2px;
      .event-pill { font-size: 11px; padding: 2px 8px; border-left: 3px solid transparent; border-radius: var(--dt-radius-sm); cursor: pointer; @include text-ellipsis; display: flex; align-items: center; gap: 4px;
        .recurrence-icon { font-size: 10px; color: var(--dt-text-quaternary); }
      }
    }
  }
}
</style>
