<template>
  <div
    class="resize-handle"
    :class="{ 'is-active': isActive }"
    @mousedown="$emit('start-resize', $event)"
    @dblclick="$emit('reset-height')"
  >
    <div class="resize-indicator">
      <span class="resize-dots"></span>
    </div>

    <transition name="height-indicator">
      <div v-if="isActive" class="height-indicator">
        <span class="height-value">{{ displayHeight }}px</span>
        <span class="height-hint">拖拽调整高度</span>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  isActive: {
    type: Boolean,
    default: false
  },
  containerHeight: {
    type: Number,
    default: 120
  }
})

defineEmits(['start-resize', 'reset-height'])

const displayHeight = computed(() => Math.round(props.containerHeight))
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.resize-handle {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 8px;
  cursor: ns-resize;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s var(--dt-ease-out);

  &:hover .resize-indicator .resize-dots::before,
  &:hover .resize-indicator .resize-dots::after {
    opacity: 1;
  }

  &.is-active {
    height: 32px;

    .resize-indicator {
      width: 48px;
      height: 4px;
      background: linear-gradient(90deg, var(--dt-brand-color), var(--dt-brand-color));
      opacity: 1;

      .resize-dots {
        background-size: 4px 4px;

        &::before, &::after {
          opacity: 1;
        }
      }
    }

    .height-indicator {
      opacity: 1;
      transform: translateY(-8px);
    }
  }

  .resize-indicator {
    width: 36px;
    height: 3px;
    background: var(--dt-border-color);
    border-radius: var(--dt-radius-full);
    opacity: 0.6;
    transition: all 0.25s var(--dt-ease-out);
    position: relative;

    .resize-dots {
      position: relative;
      width: 100%;
      height: 100%;

      background-image: linear-gradient(90deg, var(--dt-border-color) 50%, transparent 50%);
      background-size: 6px 100%;
      background-repeat: repeat-x;

      &::before, &::after {
        content: '';
        position: absolute;
        width: 8px;
        height: 8px;
        background: var(--dt-brand-color);
        border-radius: 50%;
        opacity: 0;
        transition: all 0.25s var(--dt-ease-out);
      }

      &::before { left: -4px; }
      &::after { right: -4px; }
    }
  }

  .height-indicator {
    position: absolute;
    top: 100%;
    left: 50%;
    transform: translateX(-50%) translateY(-4px);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    padding: 6px 12px;
    background: var(--dt-brand-color);
    color: #fff;
    border-radius: 0 0 6px 6px;
    box-shadow: 0 4px 12px rgba(0, 137, 255, 0.3);
    pointer-events: none;
    z-index: 100;

    .height-value {
      font-size: 14px;
      font-weight: 600;
      font-variant-numeric: tabular-nums;
    }

    .height-hint {
      font-size: 10px;
      opacity: 0.8;
    }
  }
}

.height-indicator-enter-active,
.height-indicator-leave-active {
  transition: all 0.2s var(--dt-ease-out);
}

.height-indicator-enter-from,
.height-indicator-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(0);
}
</style>
