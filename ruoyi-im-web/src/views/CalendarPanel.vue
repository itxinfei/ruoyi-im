<template>
  <div class="cal-premium-v3">
    <!-- 1. 左侧二级侧栏 (240px) -->
    <aside class="cal-sidebar-v3">
      <div class="sidebar-top">
        <el-button type="primary" class="create-event-btn" @click="handleCreateEvent">
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
        <!-- 以月视图为例：实现“灰底白卡”工业感 -->
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
              >
                {{ ev.title }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="js">
import { ref, computed } from 'vue'
import { Plus, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const viewMode = ref('month')
const miniCalMonth = ref('2026年4月')
const viewTitle = ref('2026年4月')
const selectedDateStr = ref('2026-04-18')

const categories = ref([
  { id: 1, label: '工作', color: 'var(--dt-brand-color)', active: true },
  { id: 2, label: '会议', color: 'var(--dt-success-color)', active: true },
  { id: 3, label: '个人', color: 'var(--dt-warning-color)', active: true }
])

// 模拟数据
const monthGridDays = ref(Array.from({ length: 35 }, (_, i) => ({
  date: `2026-04-${i+1}`,
  dayNum: (i % 31) + 1,
  isOtherMonth: i >= 30,
  isToday: i === 17,
  events: i === 17 ? [{ id: 1, title: 'IM界面纠偏评审', color: 'var(--dt-brand-color)', bgColor: 'var(--dt-brand-bg)' }] : []
})))

const goToday = () => {}
const handleCreateEvent = () => {}
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

.cal-main-content { flex: 1; display: flex; flex-direction: column; background: var(--dt-bg-card); }

.cal-view-header {
  height: 56px; padding: 0 24px; border-bottom: 1px solid var(--dt-border-light);
  @include flex-between;
  .header-left { display: flex; align-items: center; gap: 20px; .view-title-text { font-size: 18px; font-weight: 700; } }
}

.view-body { flex: 1; overflow-y: auto; background: var(--dt-bg-body); }

/* 🏁 纠偏：日历主网格工业化对比 */
.month-view-grid {
  display: grid; grid-template-columns: repeat(7, 1fr); height: 100%; min-height: 800px;
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
      .event-pill { font-size: 11px; padding: 2px 8px; border-left: 3px solid transparent; border-radius: var(--dt-radius-sm); cursor: pointer; @include text-ellipsis; }
    }
  }
}
</style>
