<template>
  <div 
    class="stat-card" 
    :class="[`stat-card--${type}`]"
    @click="$emit('click')"
  >
    <div class="stat-icon-outer">
      <div class="stat-icon-inner">
        <span class="material-icons-outlined">{{ icon }}</span>
      </div>
    </div>
    <div class="stat-main">
      <div class="stat-value">
        {{ value }}
      </div>
      <div class="stat-label">
        {{ label }}
      </div>
    </div>
    <div
      v-if="trend"
      class="stat-trend"
    >
      <span class="material-icons-outlined">{{ trend > 0 ? 'trending_up' : 'trending_down' }}</span>
      {{ Math.abs(trend) }}%
    </div>
  </div>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'primary' // primary, success, warning, info
  },
  icon: String,
  value: [Number, String],
  label: String,
  trend: Number
})

defineEmits(['click'])
</script>

<style scoped lang="scss">
.stat-card {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--dt-border-light);
  cursor: pointer;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, transparent 0%, var(--dt-black-02) 100%);
    opacity: 0;
    transition: opacity 0.3s;
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.05);
    border-color: var(--dt-brand-light);
    
    &::after { opacity: 1; }
    .stat-icon-inner { transform: scale(1.1); }
  }
}

.stat-icon-outer {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.stat-icon-inner {
  font-size: 28px;
  transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.stat-main {
  flex: 1;
  position: relative;
  z-index: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--dt-text-primary);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 13px;
  color: var(--dt-text-quaternary);
  font-weight: 500;
}

.stat-trend {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 2px;
  font-weight: 600;
  
  .material-icons-outlined { font-size: 14px; }
  &.up { color: #52c41a; }
  &.down { color: #f5222d; }
}

// 变体样式
.stat-card--primary {
  .stat-icon-outer { background: rgba(37, 99, 235, 0.1); color: #2563eb; }
}

.stat-card--success {
  .stat-icon-outer { background: rgba(74, 222, 128, 0.1); color: #16a34a; }
}

.stat-card--warning {
  .stat-icon-outer { background: rgba(245, 158, 11, 0.1); color: #d97706; }
}

.stat-card--info {
  .stat-icon-outer { background: rgba(124, 58, 237, 0.1); color: #7c3aed; }
}

// 暗色模式
.dark {
  .stat-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
    .stat-value { color: #f8fafc; }
  }
}
</style>
