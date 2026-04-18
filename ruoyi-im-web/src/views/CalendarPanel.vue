<template>
  <div class="calendar-v2">
    <!-- 1. 左侧沉浸式侧栏 (240px) -->
    <aside class="cal-sidebar">
      <div class="sidebar-header">
        <el-button type="primary" class="create-btn" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          <span>新建日程</span>
        </el-button>
      </div>

      <!-- 迷你日历：深度复刻 -->
      <div class="mini-cal-container">
        <div class="mini-cal-header">
          <span class="title">{{ miniCalendarTitle }}</span>
          <div class="header-ops">
            <el-icon @click="prevMonth"><ArrowLeft /></el-icon>
            <el-icon @click="nextMonth"><ArrowRight /></el-icon>
          </div>
        </div>
        <div class="mini-cal-grid">
          <div v-for="d in weekDays" :key="d" class="weekday-cell">{{ d }}</div>
          <div 
            v-for="day in miniCalendarDays" 
            :key="day.date" 
            class="day-cell"
            :class="{ today: day.isToday, selected: day.date === selectedDateStr, other: day.otherMonth }"
            @click="selectDate(day)"
          >
            {{ day.day }}
            <div v-if="day.hasEvent" class="event-dot"></div>
          </div>
        </div>
      </div>

      <!-- 视图分类 -->
      <div class="cal-filters">
        <h4 class="filter-title">我的日历</h4>
        <div v-for="cat in categories" :key="cat.id" class="filter-item">
          <el-checkbox v-model="cat.checked">
            <span class="dot" :style="{ background: cat.color }"></span>
            {{ cat.label }}
          </el-checkbox>
        </div>
      </div>
    </aside>

    <!-- 2. 右侧主视图区 -->
    <main class="cal-main">
      <header class="cal-toolbar">
        <div class="toolbar-left">
          <el-button-group>
            <el-button size="small" @click="goToday">今天</el-button>
            <el-button size="small" :icon="ArrowLeft" @click="prevPeriod" />
            <el-button size="small" :icon="ArrowRight" @click="nextPeriod" />
          </el-button-group>
          <h2 class="current-period-text">{{ periodTitle }}</h2>
        </div>
        <div class="toolbar-right">
          <el-radio-group v-model="currentView" size="small">
            <el-radio-button value="day">日</el-radio-button>
            <el-radio-button value="week">周</el-radio-button>
            <el-radio-button value="month">月</el-radio-button>
          </el-radio-group>
        </div>
      </header>

      <!-- 流延式内容区 -->
      <div class="cal-content custom-scrollbar">
        <!-- 此处根据 currentView 渲染不同子组件，演示月视图布局 -->
        <div v-if="currentView === 'month'" class="month-grid">
          <div v-for="d in weekDays" :key="d" class="month-header">{{ d }}</div>
          <div v-for="day in monthDays" :key="day.date" class="month-day-cell" :class="{ other: day.otherMonth }">
            <div class="cell-top"><span class="day-num" :class="{ today: day.isToday }">{{ day.day }}</span></div>
            <div class="cell-events">
              <div v-for="ev in day.events" :key="ev.id" class="ev-item" :style="{ background: ev.color }">{{ ev.title }}</div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="js">
import { ref, computed, onMounted } from 'vue'
import { Plus, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const currentView = ref('month')
const currentDate = ref(new Date())
const selectedDate = ref(new Date())
const weekDays = ['日', '一', '二', '三', '四', '五', '六']

const categories = ref([
  { id: 'work', label: '工作', color: 'var(--dt-event-work)', checked: true },
  { id: 'meeting', label: '会议', color: 'var(--dt-event-meeting)', checked: true },
  { id: 'personal', label: '个人', color: 'var(--dt-event-personal)', checked: true }
])

const miniCalendarTitle = computed(() => `${currentDate.value.getFullYear()}年${currentDate.value.getMonth() + 1}月`)
const periodTitle = computed(() => `${currentDate.value.getFullYear()}年${currentDate.value.getMonth() + 1}月`)
const selectedDateStr = computed(() => selectedDate.value.toISOString().split('T')[0])

// 迷你日历与主网格数据生成逻辑...
const miniCalendarDays = ref([])
const monthDays = ref([])

const goToday = () => { currentDate.value = new Date(); selectedDate.value = new Date() }
const prevMonth = () => { /* 翻页逻辑 */ }
const nextMonth = () => { /* 翻页逻辑 */ }

onMounted(() => {
  // 初始化数据加载
})
</script>

<style scoped lang="scss">
.calendar-v2 { display: flex; height: 100%; background: #fff; overflow: hidden; }

.cal-sidebar {
  width: 240px; background: #f8fbff; border-right: 1px solid var(--dt-border-light);
  display: flex; flex-direction: column;
  .sidebar-header { padding: 24px 20px; .create-btn { width: 100%; height: 36px; border-radius: 8px; font-weight: 600; } }
}

.mini-cal-container {
  padding: 0 16px 20px;
  .mini-cal-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
    padding: 0 4px; .title { font-size: 14px; font-weight: 600; }
    .header-ops { display: flex; gap: 12px; color: var(--dt-text-tertiary); cursor: pointer; }
  }
  .mini-cal-grid {
    display: grid; grid-template-columns: repeat(7, 1fr); gap: 2px;
    .weekday-cell { font-size: 10px; color: var(--dt-text-quaternary); text-align: center; height: 24px; }
    .day-cell {
      aspect-ratio: 1; @include flex-center; font-size: 12px; border-radius: 50%; cursor: pointer;
      position: relative; transition: 0.2s;
      &:hover { background: var(--dt-bg-hover); }
      &.today { background: var(--dt-brand-color); color: #fff; font-weight: 700; }
      &.selected:not(.today) { background: var(--dt-brand-bg); color: var(--dt-brand-color); font-weight: 600; }
      &.other { color: var(--dt-text-quaternary); }
      .event-dot { position: absolute; bottom: 4px; width: 3px; height: 3px; border-radius: 50%; background: var(--dt-brand-color); }
    }
  }
}

.cal-filters {
  padding: 20px; .filter-title { font-size: 12px; color: var(--dt-text-tertiary); margin-bottom: 12px; }
  .filter-item { margin-bottom: 8px; .dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 8px; } }
}

.cal-main { flex: 1; display: flex; flex-direction: column; background: #fff; }

.cal-toolbar {
  height: 56px; padding: 0 24px; border-bottom: 1px solid var(--dt-border-light);
  display: flex; align-items: center; justify-content: space-between;
  .toolbar-left { display: flex; align-items: center; gap: 20px; }
  .current-period-text { font-size: 18px; font-weight: 700; margin: 0; }
}

.cal-content { flex: 1; overflow: auto; background: #fdfdfe; }

.month-grid {
  display: grid; grid-template-columns: repeat(7, 1fr); height: 100%;
  .month-header { height: 40px; @include flex-center; font-size: 12px; color: var(--dt-text-tertiary); border-bottom: 1px solid var(--dt-border-lighter); }
  .month-day-cell {
    border-right: 1px solid var(--dt-border-lighter); border-bottom: 1px solid var(--dt-border-lighter);
    min-height: 120px; padding: 8px; transition: 0.2s; &:hover { background: #f9fbff; }
    &.other { background: #fcfcfd; .day-num { color: var(--dt-text-quaternary); } }
    .day-num { width: 24px; height: 24px; @include flex-center; font-size: 13px; border-radius: 50%; &.today { background: var(--dt-brand-color); color: #fff; } }
    .cell-events { margin-top: 8px; display: flex; flex-direction: column; gap: 2px;
      .ev-item { font-size: 11px; color: #fff; padding: 2px 6px; border-radius: 4px; @include text-ellipsis; }
    }
  }
}
</style>
