<template>
  <div class="calendar-panel">
    <aside class="calendar-sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">
          日历
        </h2>
      </div>
      <nav class="sidebar-nav">
        <div
          v-for="menu in viewMenus"
          :key="menu.key"
          class="nav-item"
          :class="{ active: currentView === menu.key }"
          @click="currentView = menu.key"
        >
          <el-icon class="nav-icon">
            <component :is="menu.icon" />
          </el-icon>
          <span class="nav-label">{{ menu.label }}</span>
        </div>
      </nav>
    </aside>
    <main class="calendar-main">
      <div class="view-content">
        <header class="calendar-header">
          <div class="header-left">
            <div class="date-range">
              <h2 class="range-title">
                {{ dateRangeString }}
              </h2>
            </div>
          </div>
        </header>
        <div v-if="currentView === 'day'" class="day-view">
          <el-empty :image-size="80" description="日视图即将推出" />
        </div>
        <div v-if="currentView === 'week'" class="week-view">
          <div class="week-header">
            <div class="time-column-header" />
          </div>
        </div>
        <div v-if="currentView === 'month'" class="month-view">
          <div class="month-grid" />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Calendar, List } from '@element-plus/icons-vue'

const currentDate = ref(new Date())
const currentView = ref('week')

const viewMenus = [
  { key: 'day', label: '日视图', icon: Calendar },
  { key: 'week', label: '周视图', icon: Calendar },
  { key: 'month', label: '月视图', icon: Calendar },
  { key: 'list', label: '日程列表', icon: List }
]

const dateRangeString = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth() + 1
  return `${year}年${month}月`
})
</script>

<style scoped lang="scss">
.calendar-panel {
  display: flex;
  height: 100%;
  background: var(--dt-bg-body);
}
.calendar-sidebar {
  width: 200px;
  background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-light);
}
.sidebar-title {
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-semibold);
  margin: 0;
  padding: var(--dt-spacing-lg) var(--dt-spacing-md);
}
.nav-item {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm, 12px);
  padding: var(--dt-spacing-sm, 12px) var(--dt-spacing-md, 20px);
  cursor: pointer;
  &:hover { background: var(--dt-bg-session-hover); }
  &.active { background: var(--dt-brand-lighter); }
}
.calendar-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.view-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.calendar-header {
  height: var(--dt-toolbar-height, 64px);
  padding: 0 var(--dt-spacing-lg, 24px);
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  background-color: var(--dt-bg-card);
}
.range-title {
  font-size: var(--dt-font-size-lg, 18px);
  font-weight: var(--dt-font-weight-semibold);
  margin: 0;
}
.day-view, .week-view, .month-view {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
