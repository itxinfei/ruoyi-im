<template>
  <div class="stats-row">
    <div
      v-for="stat in statsData"
      :key="stat.key"
      class="stat-card"
      :class="[`stat-card--${stat.key}`]"
    >
      <div class="stat-icon-wrapper">
        <span class="material-icons-outlined stat-icon">
          <div class="icon-bg" />
          {{ stat.icon }}
        </span>
      </div>
      <div class="stat-info">
        <span class="stat-value">{{ stat.value }}</span>
        <span class="stat-label">{{ stat.label }}</span>
      </div>
      <div class="stat-decoration" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  todos: {
    type: Array,
    required: true
  }
})

const statsData = computed(() => {
  const ongoing = props.todos.filter(t => !t.completed).length
  const completed = props.todos.filter(t => t.completed).length
  const highPriority = props.todos.filter(t => t.priority === 'high' && !t.completed).length

  return [
    { key: 'ongoing', label: '进行中', value: ongoing, icon: 'pending_actions' },
    { key: 'high', label: '紧急任务', value: highPriority, icon: 'priority_high' },
    { key: 'completed', label: '已完成', value: completed, icon: 'task_alt' }
  ]
})
</script>

<style scoped lang="scss">
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 24px 28px;
  background: var(--dt-bg-body);
}

.stat-card {
  position: relative;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: var(--dt-shadow-sm);
  border: 1px solid var(--dt-border-light);
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, transparent 0%, var(--dt-black-02) 100%);
    opacity: 0.5;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--dt-shadow-md);
    border-color: var(--dt-brand-light);

    .stat-icon { transform: scale(1.1) rotate(-5deg); }
  }

  &--ongoing {
    .stat-icon { color: var(--dt-brand-color); }
    .icon-bg { background: var(--dt-brand-bg); }
  }

  &--high {
    .stat-icon { color: var(--dt-priority-high); }
    .icon-bg { background: #fff1f0; }
  }

  &--completed {
    .stat-icon { color: var(--dt-priority-low); }
    .icon-bg { background: #f6ffed; }
  }
}

.stat-icon-wrapper {
  position: relative;
  z-index: 1;
}

.stat-icon {
  position: relative;
  font-size: 30px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: block;

  .icon-bg {
    position: absolute;
    inset: -6px;
    border-radius: var(--dt-radius-lg);
    z-index: -1;
    transform: rotate(5deg);
  }
}

.stat-info {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--dt-text-primary);
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.5px;
}

.stat-label {
  font-size: 13px;
  color: var(--dt-text-quaternary);
  font-weight: 500;
  margin-top: 4px;
}

.stat-decoration {
  position: absolute;
  top: 0;
  right: 0;
  width: 70px;
  height: 70px;
  background: radial-gradient(circle at top right, var(--dt-brand-light) 0%, transparent 75%);
  pointer-events: none;
  opacity: 0.2;
}

// 暗色模式适配
.dark {
  .stat-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
    
    &--ongoing .icon-bg { background: var(--dt-brand-bg-dark); }
    &--high .icon-bg { background: #451b1b; }
    &--completed .icon-bg { background: #1b351b; }
  }
  .stat-value { color: #f8fafc; }
}

@media (max-width: 768px) {
  .stats-row { grid-template-columns: 1fr; }
}
</style>
