<template>
  <Transition name="modal">
    <div
      v-if="modelValue"
      class="modal-overlay"
      @click="handleOverlayClick"
    >
      <div
        class="modal-container"
        :class="[sizeClass, modalClass]"
        @click.stop
      >
        <!-- 模态框头部 -->
        <div
          v-if="showHeader"
          class="modal-header"
        >
          <div class="modal-title">
            <slot name="title">
              <h3>{{ title }}</h3>
            </slot>
          </div>
          <button
            v-if="showClose"
            class="modal-close"
            :aria-label="closeAriaLabel"
            @click="closeModal"
          >
            <i class="el-icon-close" />
          </button>
        </div>

        <!-- 模态框内容 -->
        <div class="modal-body">
          <slot :close="closeModal" />
        </div>

        <!-- 模态框底部操作区 -->
        <div
          v-if="showFooter"
          class="modal-footer"
        >
          <slot
            name="footer"
            :confirm="handleConfirm"
            :cancel="closeModal"
          >
            <div class="modal-actions">
              <button
                v-if="showCancelButton"
                class="btn btn-secondary"
                @click="closeModal"
              >
                {{ cancelText }}
              </button>
              <button
                v-if="showConfirmButton"
                class="btn btn-primary"
                :class="{ loading: loading }"
                :disabled="loading || confirmDisabled"
                @click="handleConfirm"
              >
                <span
                  v-if="loading"
                  class="loading-spinner"
                />
                {{ loading ? loadingText : confirmText }}
              </button>
            </div>
          </slot>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'default', // 'small', 'default', 'large', 'full'
    validator: value => ['small', 'default', 'large', 'full'].includes(value)
  },
  showClose: {
    type: Boolean,
    default: true
  },
  closeOnClickModal: {
    type: Boolean,
    default: true
  },
  showHeader: {
    type: Boolean,
    default: true
  },
  showFooter: {
    type: Boolean,
    default: true
  },
  showCancelButton: {
    type: Boolean,
    default: true
  },
  showConfirmButton: {
    type: Boolean,
    default: true
  },
  confirmText: {
    type: String,
    default: '确认'
  },
  cancelText: {
    type: String,
    default: '取消'
  },
  loading: {
    type: Boolean,
    default: false
  },
  loadingText: {
    type: String,
    default: '提交中...'
  },
  confirmDisabled: {
    type: Boolean,
    default: false
  },
  closeAriaLabel: {
    type: String,
    default: '关闭'
  },
  modalClass: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

// 计算属性
const sizeClass = computed(() => {
  return `modal-${props.size}`
})

// 方法
const closeModal = () => {
  emit('update:modelValue', false)
  emit('cancel')
}

const handleConfirm = () => {
  if (!props.loading && !props.confirmDisabled) {
    emit('confirm')
  }
}

const handleOverlayClick = () => {
  if (props.closeOnClickModal) {
    closeModal()
  }
}
</script>

<style scoped>
/* 模态框遮罩层 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

/* 模态框容器 */
.modal-container {
  background: var(--dt-bg-card);
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  max-height: 90vh;
  max-width: 90vw;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  outline: none;
}

/* 尺寸变体 */
.modal-small {
  width: 400px;
  max-width: 90vw;
}

.modal-default {
  width: 520px;
  max-width: 90vw;
}

.modal-large {
  width: 760px;
  max-width: 95vw;
}

.modal-full {
  width: 95vw;
  height: 90vh;
}

/* 模态框头部 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--dt-border-lighter);
}

.modal-title h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.modal-close {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--dt-text-quaternary);
  padding: 8px;
  border-radius: 4px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:hover {
  background-color: var(--dt-bg-hover);
  color: var(--dt-text-primary);
}

/* 模态框主体 */
.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* 模态框底部 */
.modal-footer {
  padding: 16px 24px 20px;
  border-top: 1px solid var(--dt-border-lighter);
  display: flex;
  justify-content: flex-end;
}

.modal-actions {
  display: flex;
  gap: 12px;
}

/* 按钮样式 */
.btn {
  padding: 8px 20px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-primary {
  background-color: var(--dt-brand-color);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--dt-brand-hover);
}

.btn-secondary {
  background-color: var(--dt-bg-subtle);
  color: var(--dt-text-secondary);
}

.btn-secondary:hover {
  background-color: var(--dt-bg-hover);
}

/* 加载动画 */
.loading-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 过渡动画 */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from {
  opacity: 0;
  transform: scale(0.9);
}

.modal-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

/* 滚动条样式 */
.modal-body::-webkit-scrollbar {
  width: 6px;
}

.modal-body::-webkit-scrollbar-track {
  background: transparent;
}

.modal-body::-webkit-scrollbar-thumb {
  background: var(--dt-scrollbar-thumb);
  border-radius: 3px;
}

.modal-body::-webkit-scrollbar-thumb:hover {
  background: var(--dt-scrollbar-thumb-hover);
}

/* 暗色模式 */
:global(.dark) {
  .modal-container {
    background: var(--dt-bg-card-dark);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  }

  .modal-header,
  .modal-footer {
    border-color: var(--dt-border-dark);
  }

  .modal-title h3 {
    color: var(--dt-text-primary-dark);
  }

  .modal-close {
    color: var(--dt-text-quaternary-dark);
  }

  .modal-close:hover {
    background-color: var(--dt-bg-hover-dark);
    color: var(--dt-text-primary-dark);
  }

  .btn-secondary {
    background-color: var(--dt-bg-subtle-dark);
    color: var(--dt-text-secondary-dark);
  }

  .btn-secondary:hover {
    background-color: var(--dt-bg-hover-dark);
  }

  .modal-body::-webkit-scrollbar-thumb {
    background: var(--dt-scrollbar-thumb-dark);
  }

  .modal-body::-webkit-scrollbar-thumb:hover {
    background: var(--dt-scrollbar-thumb-dark-hover);
  }
}
</style>