<template>
  <div class="list-group-header" :class="{ sticky: isSticky, collapsed: isCollapsed }">
    <div class="group-title">
      <span class="group-name">{{ title }}</span>
      <span class="group-count">({{ count }})</span>
    </div>
    <button
      v-if="collapsible"
      class="group-toggle"
      :title="isCollapsed ? '展开' : '折叠'"
      @click.stop="$emit('toggle')"
    >
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="6 9 12 15 18 9"></polyline>
      </svg>
    </button>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

defineProps({
  title: {
    type: String,
    required: true
  },
  count: {
    type: Number,
    default: 0
  },
  isSticky: {
    type: Boolean,
    default: true
  },
  isCollapsed: {
    type: Boolean,
    default: false
  },
  collapsible: {
    type: Boolean,
    default: false
  }
})

defineEmits(['toggle'])
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.list-group-header {
  position: sticky;
  top: 0;
  height: 32px;
  padding: 8px 12px;
  background: var(--dt-bg-card-hover);
  border-bottom: 1px solid var(--dt-border-lighter);
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 10;
  transition: all var(--dt-transition-fast);

  &.sticky {
    box-shadow: var(--dt-shadow-2);
  }

  .group-title {
    display: flex;
    align-items: center;
    gap: 6px;
    flex: 1;
    min-width: 0;
  }

  .group-name {
    font-size: 12px;
    font-weight: 600;
    color: var(--dt-text-secondary);
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  .group-count {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    font-weight: 400;
    white-space: nowrap;
  }

  .group-toggle {
    width: 24px;
    height: 24px;
    padding: 0;
    margin-left: 8px;
    border: none;
    background: transparent;
    color: var(--dt-text-tertiary);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: var(--dt-radius-sm);
    transition: all var(--dt-transition-fast);
    flex-shrink: 0;

    svg {
      width: 16px;
      height: 16px;
      transition: transform var(--dt-transition-fast);
    }

    &:hover {
      background: var(--dt-bg-session-hover);
      color: var(--dt-text-primary);
    }

    &:active {
      transform: scale(0.95);
    }
  }

  &.collapsed .group-toggle svg {
    transform: rotate(-90deg);
  }
}

/* 暗色模式 */
.dark .list-group-header {
  background: var(--dt-bg-card-dark);
  border-bottom-color: var(--dt-border-color-dark);

  .group-name {
    color: var(--dt-text-secondary-dark);
  }

  .group-count {
    color: var(--dt-text-tertiary-dark);
  }

  .group-toggle {
    color: var(--dt-text-tertiary-dark);

    &:hover {
      background: var(--dt-bg-hover-dark);
      color: var(--dt-text-primary-dark);
    }
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .list-group-header {
    height: 28px;
    padding: 6px 12px;

    .group-name {
      font-size: 11px;
    }

    .group-count {
      font-size: 11px;
    }

    .group-toggle {
      width: 20px;
      height: 20px;

      svg {
        width: 14px;
        height: 14px;
      }
    }
  }
}
</style>
