<template>
  <Transition name="slide-up">
    <div v-if="isActive" class="multi-select-toolbar">
      <div class="selection-info">
        <div class="selection-indicator"></div>
        <span class="selection-text">
          已选择 <strong>{{ selectedCount }}</strong> / {{ totalCount }} 条
        </span>
      </div>
      <div class="actions">
        <!-- 全选/反选按钮 -->
        <button
          class="toolbar-btn toolbar-btn--select"
          @click="$emit('toggle-select-all')"
          :title="isAllSelected ? '取消全选' : '全选'"
        >
          <span class="material-icons-outlined">
            {{ isAllSelected ? 'check_box' : 'check_box_outline_blank' }}
          </span>
          <span>{{ isAllSelected ? '取消全选' : '全选' }}</span>
        </button>
        <button
          class="toolbar-btn toolbar-btn--invert"
          @click="$emit('invert-selection')"
          title="反选"
        >
          <span class="material-icons-outlined">flip</span>
          <span>反选</span>
        </button>
        <div class="toolbar-divider"></div>
        <button
          class="toolbar-btn toolbar-btn--forward"
          :disabled="selectedCount === 0"
          @click="$emit('batch-forward')"
        >
          <span class="material-icons-outlined">share</span>
          <span>逐条转发</span>
        </button>
        <button
          class="toolbar-btn toolbar-btn--combine"
          :disabled="selectedCount === 0"
          @click="$emit('combine-forward')"
        >
          <span class="material-icons-outlined">collections</span>
          <span>合并转发</span>
        </button>
        <button
          class="toolbar-btn toolbar-btn--delete"
          :disabled="selectedCount === 0"
          @click="$emit('batch-delete')"
        >
          <span class="material-icons-outlined">delete_outline</span>
          <span>删除</span>
        </button>
        <div class="toolbar-divider"></div>
        <button class="toolbar-btn toolbar-btn--cancel" @click="$emit('cancel')">
          <span>取消</span>
        </button>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  isActive: {
    type: Boolean,
    default: false
  },
  selectedCount: {
    type: Number,
    default: 0
  },
  totalCount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits([
  'batch-forward',
  'combine-forward',
  'batch-delete',
  'cancel',
  'toggle-select-all',
  'invert-selection'
])

// 是否全部选中
const isAllSelected = computed(() => {
  return props.totalCount > 0 && props.selectedCount === props.totalCount
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.multi-select-toolbar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 72px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 -8px 24px rgba(0, 0, 0, 0.08);
  z-index: var(--dt-z-sticky);

  .selection-info {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 16px;
    background: linear-gradient(135deg, var(--dt-brand-bg) 0%, var(--dt-brand-hover) 100%);
    border-radius: 24px;
    border: 1px solid rgba(22, 119, 255, 0.2);

    .selection-indicator {
      width: 12px;
      height: 12px;
      background: var(--dt-brand-color);
      border-radius: 50%;
      animation: selectionPulse 2s ease-in-out infinite;
      box-shadow: 0 0 8px rgba(22, 119, 255, 0.5);
    }

    @keyframes selectionPulse {
      0%, 100% { transform: scale(1); opacity: 1; }
      50% { transform: scale(1.2); opacity: 0.8; }
    }

    .selection-text {
      font-size: 14px;
      font-weight: 500;
      color: var(--dt-brand-color);

      strong { font-weight: 700; font-size: 16px; }
    }
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  // 全选按钮样式
  .toolbar-btn--select {
    background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
    color: #16a34a;
    border: 1px solid rgba(22, 163, 74, 0.2);

    &:hover {
      background: linear-gradient(135deg, #16a34a 0%, #15803d 100%);
      color: #fff;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(22, 163, 74, 0.3);
    }

    &.is-all-selected {
      background: linear-gradient(135deg, #16a34a 0%, #15803d 100%);
      color: #fff;
    }
  }

  // 反选按钮样式
  .toolbar-btn--invert {
    background: linear-gradient(135deg, #f3e8ff 0%, #e9d5ff 100%);
    color: #9333ea;
    border: 1px solid rgba(147, 51, 234, 0.2);

    &:hover {
      background: linear-gradient(135deg, #9333ea 0%, #7e22ce 100%);
      color: #fff;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(147, 51, 234, 0.3);
    }
  }

  .toolbar-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: 36px;
    padding: 0 16px;
    border-radius: 20px;
    border: none;
    background: transparent;
    color: #666;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    .material-icons-outlined { font-size: 16px; }

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;

      &:hover { transform: none; background: transparent; }
    }

    &--forward {
      background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
      color: #0284c7;
      border: 1px solid rgba(2, 132, 199, 0.2);

      &:hover:not(:disabled) {
        background: linear-gradient(135deg, #0284c7 0%, #0ea5e9 100%);
        color: #fff;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(2, 132, 199, 0.3);
      }
    }

    &--combine {
      background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
      color: #d97706;
      border: 1px solid rgba(217, 119, 6, 0.2);

      &:hover:not(:disabled) {
        background: linear-gradient(135deg, #d97706 0%, #f59e0b 100%);
        color: #fff;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(217, 119, 6, 0.3);
      }
    }

    &--delete {
      background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
      color: #dc2626;
      border: 1px solid rgba(220, 38, 38, 0.2);

      &:hover:not(:disabled) {
        background: linear-gradient(135deg, #dc2626 0%, #ef4444 100%);
        color: #fff;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
      }
    }

    &--cancel {
      background: rgba(0, 0, 0, 0.04);
      color: #666;

      &:hover {
        background: rgba(0, 0, 0, 0.08);
        color: #1a1a1a;
      }
    }
  }

  .toolbar-divider {
    width: 1px;
    height: 24px;
    background: rgba(0, 0, 0, 0.1);
  }
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all var(--dt-transition-slow);
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

// 暗色模式
:global(.dark) {
  .multi-select-toolbar {
    background: linear-gradient(135deg, #2d2d2d 0%, #1e1e1e 100%);
    border-color: rgba(255, 255, 255, 0.1);
    box-shadow: 0 -8px 24px rgba(0, 0, 0, 0.3);

    .selection-info {
      background: linear-gradient(135deg, rgba(22, 119, 255, 0.2) 0%, rgba(22, 119, 255, 0.15) 100%);
      border-color: rgba(22, 119, 255, 0.3);

      .selection-text { color: #ffffff; }
    }

    .actions .toolbar-btn {
      &--forward {
        background: linear-gradient(135deg, rgba(2, 132, 199, 0.15) 0%, rgba(2, 132, 199, 0.1) 100%);
        color: #38bdf8;

        &:hover:not(:disabled) {
          background: linear-gradient(135deg, #0284c7 0%, #0ea5e9 100%);
          color: #fff;
        }
      }

      &--combine {
        background: linear-gradient(135deg, rgba(217, 119, 6, 0.15) 0%, rgba(217, 119, 6, 0.1) 100%);
        color: #fbbf24;

        &:hover:not(:disabled) {
          background: linear-gradient(135deg, #d97706 0%, #f59e0b 100%);
          color: #fff;
        }
      }

      &--delete {
        background: linear-gradient(135deg, rgba(220, 38, 38, 0.15) 0%, rgba(220, 38, 38, 0.1) 100%);
        color: #f87171;

        &:hover:not(:disabled) {
          background: linear-gradient(135deg, #dc2626 0%, #ef4444 100%);
          color: #fff;
        }
      }

      &--cancel {
        background: rgba(255, 255, 255, 0.06);
        color: #999;

        &:hover {
          background: rgba(255, 255, 255, 0.1);
          color: #e8e8e8;
        }
      }
    }

    .toolbar-divider { background: rgba(255, 255, 255, 0.1); }

    .actions .toolbar-btn {
      &--select {
        background: linear-gradient(135deg, rgba(22, 163, 74, 0.15) 0%, rgba(22, 163, 74, 0.1) 100%);
        color: #4ade80;

        &:hover {
          background: linear-gradient(135deg, #16a34a 0%, #15803d 100%);
          color: #fff;
        }

        &.is-all-selected {
          background: linear-gradient(135deg, #16a34a 0%, #15803d 100%);
          color: #fff;
        }
      }

      &--invert {
        background: linear-gradient(135deg, rgba(147, 51, 234, 0.15) 0%, rgba(147, 51, 234, 0.1) 100%);
        color: #c084fc;

        &:hover {
          background: linear-gradient(135deg, #9333ea 0%, #7e22ce 100%);
          color: #fff;
        }
      }
    }
  }
}
</style>
