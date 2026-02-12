<template>
  <WorkbenchWidget
    title="公告通知"
    icon="campaign"
    class="announcement-widget"
    @more="$emit('more')"
  >
    <div
      v-if="loading"
      class="widget-loading"
    >
      <el-skeleton
        :rows="2"
        animated
      />
    </div>
    <div
      v-else-if="announcements.length > 0"
      class="announcement-list"
    >
      <div 
        v-for="item in announcements.slice(0, 4)" 
        :key="item.id" 
        class="announcement-item"
        @click="$emit('click-item', item)"
      >
        <div class="announcement-main">
          <div class="announcement-title">
            <span 
              class="priority-dot" 
              :class="item.type?.toLowerCase() || 'info'"
            />
            {{ item.title }}
          </div>
          <div class="announcement-meta">
            {{ formatTime(item.createTime || item.publishTime) }}
          </div>
        </div>
        <span class="material-icons-outlined arrow">chevron_right</span>
      </div>
    </div>
    <div
      v-else
      class="widget-empty"
    >
      <span class="material-icons-outlined empty-icon">campaign</span>
      <p>暂无最新公告</p>
    </div>
  </WorkbenchWidget>
</template>

<script setup>
import WorkbenchWidget from './WorkbenchWidget.vue'
import dayjs from 'dayjs'

defineProps({
  announcements: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['more', 'click-item'])

const formatTime = time => {
  if (!time) {return ''}
  return dayjs(time).format('MM-DD HH:mm')
}
</script>

<style scoped lang="scss">
.announcement-list {
  display: flex;
  flex-direction: column;
}

.announcement-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--dt-border-lighter);
  cursor: pointer;
  transition: all 0.2s;
  
  &:last-child { border-bottom: none; }
  
  &:hover {
    .announcement-title { color: var(--dt-brand-color); }
    .arrow { transform: translateX(4px); opacity: 1; }
  }
}

.announcement-main {
  flex: 1;
  min-width: 0;
}

.announcement-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  gap: 8px;
}

.priority-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
  
  &.important, &.high { background: #f5222d; box-shadow: 0 0 6px rgba(245, 34, 45, 0.4); }
  &.notice, &.medium { background: #fa8c16; }
  &.info, &.low { background: #1890ff; }
}

.announcement-meta {
  font-size: 12px;
  color: var(--dt-text-quaternary);
}

.arrow {
  font-size: 18px;
  color: var(--dt-text-quaternary);
  opacity: 0.5;
  transition: all 0.2s;
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
