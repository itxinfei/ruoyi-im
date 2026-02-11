/**
 * 消息悬停操作栏 - 钉钉/飞书风格
 *
 * 鼠标悬停在消息气泡上时，显示浮动操作栏（回复、表情、转发、更多）
 */
<template>
  <transition name="hover-actions-fade">
    <div
      v-if="visible"
      class="message-hover-actions"
      :class="{ 'is-right': isOwn }"
      @mouseenter="$emit('keep-hover')"
      @mouseleave="$emit('leave-hover')"
    >
      <button
        class="hover-action-btn"
        title="回复"
        @click.stop="$emit('action', 'reply')"
      >
        <span class="material-icons-outlined">reply</span>
      </button>
      <button
        class="hover-action-btn"
        title="表情"
        @click.stop="$emit('action', 'emoji')"
      >
        <span class="material-icons-outlined">sentiment_satisfied_alt</span>
      </button>
      <button
        class="hover-action-btn"
        title="转发"
        @click.stop="$emit('action', 'forward')"
      >
        <span class="material-icons-outlined">forward</span>
      </button>
      <button
        class="hover-action-btn"
        title="更多"
        @click.stop="$emit('action', 'more')"
      >
        <span class="material-icons-outlined">more_horiz</span>
      </button>
    </div>
  </transition>
</template>

<script setup>
defineProps({
  visible: { type: Boolean, default: false },
  isOwn: { type: Boolean, default: false }
})

defineEmits(['action', 'keep-hover', 'leave-hover'])
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-hover-actions {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  left: calc(100% + 4px);
  right: auto;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 2px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;

  // 自己消息：操作栏在气泡左侧
  &.is-right {
    left: auto;
    right: calc(100% + 4px);
  }

  .dark & {
    background: var(--dt-bg-card-dark, #1A1D24);
    border-color: var(--dt-border-dark, #3F424A);
  }
}

.hover-action-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  color: var(--dt-text-secondary);
  transition: background-color 0.15s ease;

  .material-icons-outlined {
    font-size: 16px;
  }

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-brand-color);
  }

  .dark & {
    color: var(--dt-text-secondary-dark, #9CA3AF);

    &:hover {
      background: var(--dt-bg-hover-dark, #2A2D35);
      color: var(--dt-brand-color);
    }
  }
}

// 淡入动画
.hover-actions-fade-enter-active {
  transition: opacity 0.1s ease, transform 0.1s ease;
}

.hover-actions-fade-leave-active {
  transition: opacity 0.08s ease;
}

.hover-actions-fade-enter-from {
  opacity: 0;
  transform: translateY(-50%) scale(0.95);
}

.hover-actions-fade-leave-to {
  opacity: 0;
  transform: translateY(-50%) scale(0.95);
}
</style>
