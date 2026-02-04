<!--
多选操作栏组件
用于聊天消息的多选操作：转发、删除等
-->
<template>
  <Transition name="slide-up">
    <div v-if="active" class="multi-select-toolbar">
      <div class="selection-info">
        <div class="selection-indicator"></div>
        <span class="selection-text">
          已选择 <strong>{{ count }}</strong> 条消息
        </span>
      </div>

      <div class="actions">
        <button
          class="toolbar-btn toolbar-btn--forward"
          @click="$emit('forward')"
          :disabled="count === 0"
          title="逐条转发"
        >
          <span class="material-icons-outlined">share</span>
          <span>逐条转发</span>
        </button>

        <button
          class="toolbar-btn toolbar-btn--combine"
          @click="$emit('combine')"
          :disabled="count === 0"
          title="合并转发"
        >
          <span class="material-icons-outlined">collections</span>
          <span>合并转发</span>
        </button>

        <button
          class="toolbar-btn toolbar-btn--delete"
          @click="$emit('delete')"
          :disabled="count === 0"
          title="删除"
        >
          <span class="material-icons-outlined">delete_outline</span>
          <span>删除</span>
        </button>

        <div class="toolbar-divider"></div>

        <button
          class="toolbar-btn toolbar-btn--cancel"
          @click="$emit('cancel')"
          title="取消选择"
        >
          <span>取消</span>
        </button>
      </div>
    </div>
  </Transition>
</template>

<script setup>
defineProps({
  active: {
    type: Boolean,
    default: false
  },
  count: {
    type: Number,
    default: 0
  }
})

defineEmits(['forward', 'combine', 'delete', 'cancel'])
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.multi-select-toolbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-light);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
  z-index: 100;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-top-color: var(--dt-border-dark);
    box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.3);
  }
}

.selection-info {
  display: flex;
  align-items: center;
  gap: 12px;

  .selection-indicator {
    width: 4px;
    height: 16px;
    background: var(--dt-brand-color);
    border-radius: var(--dt-radius-sm);
  }

  .selection-text {
    font-size: 14px;
    color: var(--dt-text-primary);

    strong {
      color: var(--dt-brand-color);
      font-weight: 600;
      margin: 0 2px;
    }
  }
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: var(--dt-radius-sm);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  background: var(--dt-bg-hover);
  color: var(--dt-text-primary);

  &:hover:not(:disabled) {
    background: var(--dt-bg-active);
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  .material-icons-outlined {
    font-size: 18px;
  }

  &--forward {
    &:hover:not(:disabled) {
      background: rgba(0, 137, 255, 0.1);
      color: var(--dt-brand-color);
    }
  }

  &--combine {
    &:hover:not(:disabled) {
      background: rgba(103, 58, 183, 0.1);
      color: #673AB7;
    }
  }

  &--delete {
    &:hover:not(:disabled) {
      background: rgba(255, 77, 79, 0.1);
      color: var(--dt-error-color);
    }
  }

  &--cancel {
    font-weight: 500;
  }
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: var(--dt-border-lighter);
  margin: 0 8px;
}

// 滑入动画
.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform var(--dt-transition-base), opacity var(--dt-transition-base);
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}
</style>
