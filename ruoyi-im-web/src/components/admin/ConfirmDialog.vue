<template>
  <el-dialog
    v-model="internalVisible"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    :close-on-press-escape="closeOnEscape"
    :show-close="showClose"
    :before-close="handleBeforeClose"
    :class="dialogClass"
    destroy-on-close
  >
    <!-- 图标和内容 -->
    <div class="confirm-dialog-content">
      <!-- 图标 -->
      <div v-if="showIcon" class="confirm-icon" :class="'confirm-icon--' + type">
        <el-icon :size="iconSize">
          <WarningFilled v-if="type === 'warning'" />
          <CircleCheckFilled v-else-if="type === 'success'" />
          <CircleCloseFilled v-else-if="type === 'error'" />
          <InfoFilled v-else-if="type === 'info'" />
          <WarningFilled v-else />
        </el-icon>
      </div>

      <!-- 消息内容 -->
      <div class="confirm-message">
        <h3 v-if="messageTitle" class="confirm-title">{{ messageTitle }}</h3>
        <p class="confirm-text">{{ message }}</p>

        <!-- 额外内容插槽 -->
        <div v-if="$slots.default" class="confirm-extra">
          <slot></slot>
        </div>

        <!-- 确认输入（防误操作） -->
        <el-input
          v-if="requireConfirm"
          v-model="confirmInput"
          :placeholder="confirmPlaceholder"
          clearable
          class="confirm-input"
        />
      </div>
    </div>

    <!-- 自定义内容插槽 -->
    <div v-if="$slots.content" class="confirm-custom-content">
      <slot name="content"></slot>
    </div>

    <!-- 底部操作按钮 -->
    <template #footer>
      <div class="confirm-footer">
        <el-checkbox v-if="showRemember" v-model="rememberChoice" class="remember-checkbox">
          {{ rememberText }}
        </el-checkbox>
        <div class="confirm-buttons">
          <el-button
            v-if="showCancelButton"
            :size="buttonSize"
            @click="handleCancel"
          >
            {{ cancelText }}
          </el-button>
          <el-button
            :type="confirmType"
            :size="buttonSize"
            :loading="loading"
            :disabled="requireConfirm && !isConfirmMatch"
            @click="handleConfirm"
          >
            {{ confirmText }}
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import {
  WarningFilled,
  CircleCheckFilled,
  CircleCloseFilled,
  InfoFilled
} from '@element-plus/icons-vue'

/**
 * AdminConfirmDialog - 管理后台通用确认对话框组件
 *
 * @param {Boolean} visible - 对话框显示状态（v-model）
 * @param {String} title - 对话框标题
 * @param {String} message - 对话框消息内容
 * @param {String} messageTitle - 消息标题（可选）
 * @param {String} type - 对话框类型：warning | success | error | info
 * @param {String} confirmText - 确认按钮文字
 * @param {String} cancelText - 取消按钮文字
 * @param {Boolean} showCancelButton - 是否显示取消按钮
 * @param {Boolean} showIcon - 是否显示图标
 * @param {String} width - 对话框宽度
 * @param {Boolean} requireConfirm - 是否需要输入确认文字
 * @param {String} confirmKeyword - 确认关键词（默认：确定删除）
 * @param {Boolean} showRemember - 是否显示"记住选择"复选框
 *
 * @example
 * <admin-confirm-dialog
 *   v-model:visible="dialogVisible"
 *   title="删除确认"
 *   message="确定要删除这条记录吗？删除后无法恢复。"
 *   type="warning"
 *   @confirm="handleConfirm"
 *   @cancel="handleCancel"
 * />
 */

const props = defineProps({
  // 显示状态
  visible: {
    type: Boolean,
    default: false
  },
  // 标题
  title: {
    type: String,
    default: '提示'
  },
  // 消息内容
  message: {
    type: String,
    required: true
  },
  // 消息标题
  messageTitle: {
    type: String,
    default: ''
  },
  // 类型
  type: {
    type: String,
    default: 'warning',
    validator: (val) => ['warning', 'success', 'error', 'info'].includes(val)
  },
  // 确认按钮文字
  confirmText: {
    type: String,
    default: '确定'
  },
  // 取消按钮文字
  cancelText: {
    type: String,
    default: '取消'
  },
  // 是否显示取消按钮
  showCancelButton: {
    type: Boolean,
    default: true
  },
  // 是否显示图标
  showIcon: {
    type: Boolean,
    default: true
  },
  // 对话框宽度
  width: {
    type: String,
    default: '420px'
  },
  // 按钮大小
  buttonSize: {
    type: String,
    default: 'default'
  },
  // 图标大小
  iconSize: {
    type: Number,
    default: 48
  },
  // 是否需要输入确认
  requireConfirm: {
    type: Boolean,
    default: false
  },
  // 确认关键词
  confirmKeyword: {
    type: String,
    default: '确定删除'
  },
  // 是否显示记住选择
  showRemember: {
    type: Boolean,
    default: false
  },
  // 记住选择文字
  rememberText: {
    type: String,
    default: '记住此选择，不再提示'
  },
  // 是否显示关闭按钮
  showClose: {
    type: Boolean,
    default: true
  },
  // ESC 关闭
  closeOnEscape: {
    type: Boolean,
    default: true
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 对话框类名
  dialogClass: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible', 'confirm', 'cancel', 'close'])

// 内部显示状态
const internalVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// 确认输入值
const confirmInput = ref('')

// 记忆选择
const rememberChoice = ref(false)

// 确认关键词匹配检查
const isConfirmMatch = computed(() => {
  if (!props.requireConfirm) return true
  return confirmInput.value === props.confirmKeyword
})

// 确认输入提示文字
const confirmPlaceholder = computed(() => {
  return `请输入 "${props.confirmKeyword}" 确认操作`
})

// 确认按钮类型
const confirmType = computed(() => {
  const typeMap = {
    warning: 'warning',
    success: 'success',
    error: 'danger',
    info: 'primary'
  }
  return typeMap[props.type] || 'primary'
})

// 重置状态
const resetState = () => {
  confirmInput.value = ''
  rememberChoice.value = false
}

// 监听对话框关闭，重置状态
watch(() => props.visible, (newVal) => {
  if (!newVal) {
    resetState()
  }
})

// 处理确认
const handleConfirm = () => {
  emit('confirm', {
    remember: rememberChoice.value
  })
}

// 处理取消
const handleCancel = () => {
  emit('cancel')
  emit('update:visible', false)
}

// 处理关闭前
const handleBeforeClose = (done) => {
  if (props.loading) return
  emit('close')
  done()
}

// 暴露方法
defineExpose({
  resetState
})
</script>

<style scoped>
/* 引入主题变量 */
@import '@/styles/admin-theme.css';

/* ================================
   对话框内容容器
   ================================ */
.confirm-dialog-content {
  display: flex;
  gap: var(--dt-space-md);
  padding: var(--dt-space-md) 0;
}

/* 图标 */
.confirm-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.confirm-icon--warning {
  background-color: var(--dt-warning-bg);
  color: var(--dt-warning);
}

.confirm-icon--success {
  background-color: var(--dt-success-bg);
  color: var(--dt-success);
}

.confirm-icon--error {
  background-color: var(--dt-error-bg);
  color: var(--dt-error);
}

.confirm-icon--info {
  background-color: var(--dt-info-bg);
  color: var(--dt-info);
}

/* 消息内容 */
.confirm-message {
  flex: 1;
}

.confirm-title {
  font-size: var(--dt-font-size-md);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin: 0 0 var(--dt-space-xs) 0;
}

.confirm-text {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-regular);
  line-height: var(--dt-line-height-base);
  margin: 0 0 var(--dt-space-sm) 0;
}

.confirm-extra {
  margin-top: var(--dt-space-sm);
}

.confirm-input {
  margin-top: var(--dt-space-md);
}

/* 自定义内容区域 */
.confirm-custom-content {
  margin-top: var(--dt-space-md);
  padding-top: var(--dt-space-md);
  border-top: 1px solid var(--dt-divider);
}

/* 底部操作区 */
.confirm-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remember-checkbox {
  font-size: var(--dt-font-size-sm);
}

.remember-checkbox :deep(.el-checkbox__label) {
  color: var(--dt-text-secondary);
}

.confirm-buttons {
  display: flex;
  gap: var(--dt-space-sm);
}

/* ================================
   对话框样式覆盖
   ================================ */
:deep(.el-dialog__header) {
  padding: var(--dt-space-md) var(--dt-space-md) var(--dt-space-sm);
  border-bottom: 1px solid var(--dt-divider);
}

:deep(.el-dialog__title) {
  font-size: var(--dt-font-size-md);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
}

:deep(.el-dialog__body) {
  padding: var(--dt-space-md);
}

:deep(.el-dialog__footer) {
  padding: var(--dt-space-sm) var(--dt-space-md);
  border-top: 1px solid var(--dt-divider);
}

/* ================================
   特殊样式：对话框居中内容
   ================================ */
:deep(.el-dialog .confirm-dialog-content) {
  display: flex;
  align-items: flex-start;
}
</style>
