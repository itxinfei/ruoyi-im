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
  top: -32px;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 4px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 16px;
  box-shadow: var(--dt-shadow-sm);
  z-index: 10;

  // 对方消息：操作栏在右上方
  right: 0;

  // 自己消息：操作栏在左上方
  &.is-right {
    right: auto;
    left: 0;
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
  border-radius: 50%;
  cursor: pointer;
  color: var(--dt-text-secondary);
  transition: all 0.15s ease;

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
  transform: translateY(4px);
}

.hover-actions-fade-leave-to {
  opacity: 0;
}
</style>
