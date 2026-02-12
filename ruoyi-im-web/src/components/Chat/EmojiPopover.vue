/**
* 快捷表情选择器
* 用于消息的表情回应功能
*/
<template>
  <teleport to="body">
    <Transition name="fade">
      <div
        v-if="visible"
        v-click-outside="handleClose"
        class="emoji-popover"
        :style="{ left: position.x + 'px', top: position.y + 'px' }"
      >
        <div class="emoji-popover-header">
          <span>添加表情回应</span>
          <el-icon
            class="close-icon"
            @click="handleClose"
          >
            <Close />
          </el-icon>
        </div>
        <div class="emoji-grid">
          <span
            v-for="emoji in QUICK_EMOJIS"
            :key="emoji"
            class="emoji-item"
            @click="handleSelectEmoji(emoji)"
          >
            {{ emoji }}
          </span>
        </div>
      </div>
    </Transition>
  </teleport>
</template>

<script setup>
import { Close } from '@element-plus/icons-vue'

// 快捷表情列表（钉钉/野火IM标准）
const QUICK_EMOJIS = ['👍', '❤️', '😂', '😮', '😢', '😡', '🎉', '🔥']

const props = defineProps({
  visible: { type: Boolean, default: false },
  position: {
    type: Object,
    default: () => ({ x: 0, y: 0 })
  }
})

const emit = defineEmits(['select', 'close'])

const handleSelectEmoji = emoji => {
  emit('select', emoji)
  emit('close')
}

const handleClose = () => {
  emit('close')
}

// 点击外部关闭指令
const vClickOutside = {
  mounted(el, binding) {
    el.clickOutsideEvent = event => {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value()
      }
    }
    document.body.addEventListener('click', el.clickOutsideEvent)
  },
  unmounted(el) {
    document.body.removeEventListener('click', el.clickOutsideEvent)
  }
}
</script>

<script>
export default {
  directives: { clickOutside: vClickOutside }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.emoji-popover {
  position: fixed;
  z-index: var(--dt-z-popover);
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  box-shadow: var(--dt-shadow-lg);
  padding: 12px;
  min-width: 280px;
  animation: popoverFadeIn 0.2s var(--dt-ease-out);
}

.emoji-popover-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--dt-border-light);

  span {
    font-size: var(--dt-font-size-sm);
    font-weight: 500;
    color: var(--dt-text-primary);
  }

  .close-icon {
    cursor: pointer;
    font-size: 18px;
    color: var(--dt-text-tertiary);
    transition: color 0.2s;

    &:hover {
      color: var(--dt-text-primary);
    }
  }
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.emoji-item {
  font-size: 28px;
  text-align: center;
  padding: 8px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;

  &:hover {
    background: var(--dt-bg-hover);
    transform: scale(1.1);
  }

  &:active {
    transform: scale(0.95);
  }
}

@keyframes popoverFadeIn {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

// 暗色模式
:global(.dark) {
  .emoji-popover {
    background: var(--dt-bg-card-dark);
    box-shadow: var(--dt-shadow-lg);
  }

  .emoji-item:hover {
    background: var(--dt-bg-hover-dark);
  }
}
</style>
