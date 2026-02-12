<template>
  <div class="filter-tabs">
    <button
      v-for="tab in filterTabs"
      :key="tab.key"
      class="filter-tab"
      :class="{ active: activeFilter === tab.key }"
      @click="$emit('update:activeFilter', tab.key)"
    >
      <span class="material-icons-outlined filter-tab-icon">{{ tab.icon }}</span>
      <span class="filter-tab-label">{{ tab.label }}</span>
      <span
        v-if="counts[tab.key] > 0"
        class="tab-count"
      >{{ counts[tab.key] > 99 ? '99+' : counts[tab.key] }}</span>
      <div class="tab-indicator" />
    </button>
  </div>
</template>

<script setup>
defineProps({
  filterTabs: {
    type: Array,
    required: true
  },
  activeFilter: {
    type: String,
    required: true
  },
  counts: {
    type: Object,
    default: () => ({})
  }
})

defineEmits(['update:activeFilter'])
</script>

<style scoped lang="scss">
.filter-tabs {
  display: flex;
  padding: 0 28px;
  background: var(--dt-bg-body);
  border-bottom: 1px solid var(--dt-border-light);
  gap: 8px;
  flex-shrink: 0;
  position: relative;
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  background: transparent;
  border: none;
  font-size: 14px;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  margin-bottom: -1px;

  .filter-tab-icon {
    font-size: 18px;
    transition: transform 0.3s;
  }

  .filter-tab-label {
    font-size: 14px;
    font-weight: 500;
  }

  .tab-count {
    padding: 2px 6px;
    background: var(--dt-black-05);
    color: var(--dt-text-quaternary);
    border-radius: 10px;
    font-size: 11px;
    font-weight: 700;
    min-width: 18px;
    text-align: center;
  }

  .tab-indicator {
    position: absolute;
    bottom: 0;
    left: 12px;
    right: 12px;
    height: 3px;
    background: var(--dt-brand-color);
    border-radius: 3px 3px 0 0;
    transform: scaleX(0);
    transition: transform 0.3s cubic-bezier(0.65, 0, 0.35, 1);
  }

  &:hover {
    color: var(--dt-text-primary);
    background: var(--dt-bg-hover);
    .filter-tab-icon { transform: translateY(-1px); }
  }

  &.active {
    color: var(--dt-brand-color);
    font-weight: 600;

    .tab-count {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
    }

    .tab-indicator { transform: scaleX(1); }
  }
}

// 暗色模式适配
.dark {
  .filter-tabs { border-color: var(--dt-border-dark); }
  .filter-tab {
    color: var(--dt-text-quaternary);
    &:hover { background: var(--dt-bg-hover-dark); color: #f8fafc; }
    &.active { color: var(--dt-brand-color); }
    .tab-count { background: #1f2937; color: #94a3b8; }
  }
}
</style>
