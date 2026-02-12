<template>
  <WorkbenchWidget
    title="今日日程"
    icon="calendar_today"
    class="schedule-widget"
    @more="$emit('more')"
  >
    <template #actions>
      <button
        class="add-mini-btn"
        @click.stop="$emit('add')"
      >
        <span class="material-icons-outlined">add</span>
      </button>
    </template>

    <div
      v-if="loading"
      class="widget-loading"
    >
      <el-skeleton
        :rows="3"
        animated
      />
    </div>
    <div
      v-else-if="schedules.length > 0"
      class="schedule-list"
    >
      <div 
        v-for="item in schedules.slice(0, 4)" 
        :key="item.id" 
        class="schedule-item"
        :style="{ '--accent-color': item.color || '#2563eb' }"
        @click="$emit('click-item', item)"
      >
        <div class="schedule-time-box">
          <span class="time-start">{{ formatTime(item.startTime) }}</span>
          <span class="time-sep">-</span>
          <span class="time-end">{{ formatTime(item.endTime) }}</span>
        </div>
        <div class="schedule-info">
          <div class="schedule-title">
            {{ item.title }}
          </div>
          <div
            v-if="item.location"
            class="schedule-location"
          >
            <span class="material-icons-outlined">place</span>
            {{ item.location }}
          </div>
        </div>
      </div>
    </div>
    <div
      v-else
      class="widget-empty"
    >
      <span class="material-icons-outlined empty-icon">event_available</span>
      <p>今日暂无行程</p>
    </div>
  </WorkbenchWidget>
</template>

<script setup>
import WorkbenchWidget from './WorkbenchWidget.vue'
import dayjs from 'dayjs'

defineProps({
  schedules: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['more', 'click-item', 'add'])

const formatTime = time => {
  if (!time) {return ''}
  return dayjs(time).format('HH:mm')
}
</script>

<style scoped lang="scss">
.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    left: 70px;
    top: 10px;
    bottom: 10px;
    width: 2px;
    background: var(--dt-border-lighter);
    opacity: 0.5;
  }
}

.schedule-item {
  display: flex;
  gap: 20px;
  padding: 10px;
  border-radius: var(--dt-radius-lg);
  cursor: pointer;
  transition: all 0.2s;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    left: 66px;
    top: 50%;
    transform: translateY(-50%);
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: #fff;
    border: 2px solid var(--accent-color);
    z-index: 1;
  }
  
  &:hover {
    background: var(--dt-bg-subtle-hover);
    &::after { transform: translateY(-50%) scale(1.2); }
  }
}

.schedule-time-box {
  width: 60px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  font-size: 11px;
  color: var(--dt-text-quaternary);
  font-weight: 600;
  
  .time-start { color: var(--dt-text-primary); font-size: 13px; }
  .time-sep { margin: -2px 0; opacity: 0.5; }
}

.schedule-info {
  flex: 1;
  min-width: 0;
  padding-left: 10px;
}

.schedule-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin-bottom: 4px;
}

.schedule-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--dt-text-quaternary);
  
  .material-icons-outlined { font-size: 14px; }
}

.add-mini-btn {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  border: 1px solid var(--dt-border-light);
  background: #fff;
  color: var(--dt-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  
  &:hover {
    color: var(--dt-brand-color);
    border-color: var(--dt-brand-color);
    background: var(--dt-brand-bg);
  }
  
  .material-icons-outlined { font-size: 16px; }
}

.widget-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: var(--dt-text-quaternary);
  
  .empty-icon { font-size: 48px; margin-bottom: 12px; opacity: 0.5; }
  p { font-size: 14px; margin: 0; }
}
</style>
