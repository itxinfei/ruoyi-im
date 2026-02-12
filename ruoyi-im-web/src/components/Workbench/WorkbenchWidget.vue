<template>
  <div 
    class="workbench-widget" 
    :class="{ 'is-clickable': clickable }"
    @click="clickable && $emit('click')"
  >
    <div
      v-if="title || $slots.header"
      class="widget-header"
    >
      <div class="header-left">
        <span
          v-if="icon"
          class="material-icons-outlined widget-icon"
        >{{ icon }}</span>
        <h3
          v-if="title"
          class="widget-title"
        >
          {{ title }}
        </h3>
        <slot name="header" />
      </div>
      <div class="header-right">
        <slot name="actions">
          <button
            v-if="moreText"
            class="more-btn"
            @click.stop="$emit('more')"
          >
            {{ moreText }}
            <span class="material-icons-outlined">chevron_right</span>
          </button>
        </slot>
      </div>
    </div>
    <div
      class="widget-body"
      :class="{ 'no-padding': noPadding }"
    >
      <slot />
    </div>
    <div
      v-if="$slots.footer"
      class="widget-footer"
    >
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: String,
  icon: String,
  moreText: {
    type: String,
    default: '更多'
  },
  noPadding: {
    type: Boolean,
    default: false
  },
  clickable: {
    type: Boolean,
    default: false
  }
})

defineEmits(['more', 'click'])
</script>

<style scoped lang="scss">
.workbench-widget {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-xl);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;

  &.is-clickable {
    cursor: pointer;
    &:hover {
      border-color: var(--dt-brand-light);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.04);
    }
  }
}

.widget-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-lighter);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.widget-icon {
  font-size: 20px;
  color: var(--dt-brand-color);
}

.widget-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.more-btn {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 13px;
  color: var(--dt-text-quaternary);
  background: none;
  border: none;
  padding: 4px 8px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    color: var(--dt-brand-color);
    background: var(--dt-brand-bg);
  }

  .material-icons-outlined { font-size: 16px; }
}

.widget-body {
  flex: 1;
  padding: 20px;
  
  &.no-padding { padding: 0; }
}

.widget-footer {
  padding: 12px 20px;
  border-top: 1px solid var(--dt-border-lighter);
  background: var(--dt-bg-subtle);
}

// 暗色模式
.dark {
  .workbench-widget {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
    .widget-header { border-bottom-color: var(--dt-border-dark); }
  }
}
</style>
