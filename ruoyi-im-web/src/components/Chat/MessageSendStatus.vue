<template>
  <div
    v-if="visible"
    class="message-send-status"
    :class="[`status-${status}`]"
    @click="handleClick"
  >
    <!-- 发送中：灰色圆圈 + 旋转动画 -->
    <template v-if="status === 'sending'">
      <div class="status-icon sending-icon">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>
    </template>

    <!-- 发送成功：绿色对勾（0.5秒后消失） -->
    <template v-else-if="status === 'success'">
      <div class="status-icon success-icon">
        <el-icon><Select /></el-icon>
      </div>
    </template>

    <!-- 发送失败：红色感叹号 + 点击重试 -->
    <template v-else-if="status === 'failed'">
      <div class="status-icon failed-icon">
        <el-icon><CircleClose /></el-icon>
      </div>
      <div v-if="showRetryText" class="status-text">点击重试</div>
    </template>
  </div>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue'
import { Loading, Select, CircleClose } from '@element-plus/icons-vue'

/**
 * 消息发送状态组件
 * 显示消息发送的三种状态：发送中、发送成功、发送失败
 */

const props = defineProps({
  // 状态：sending（发送中）、success（成功）、failed（失败）
  status: {
    type: String,
    default: 'sending',
    validator: (value) => ['sending', 'success', 'failed'].includes(value)
  },
  // 是否显示"点击重试"文字
  showRetryText: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['retry'])

// 组件可见性（成功状态0.5秒后自动隐藏）
const visible = ref(true)
let successTimer = null

// 监听状态变化
watch(() => props.status, (newStatus) => {
  if (newStatus === 'success') {
    // 成功状态：0.5秒后自动隐藏
    visible.value = true
    clearSuccessTimer()
    successTimer = setTimeout(() => {
      visible.value = false
    }, 500)
  } else if (newStatus === 'sending' || newStatus === 'failed') {
    // 发送中或失败状态：始终显示
    visible.value = true
    clearSuccessTimer()
  }
}, { immediate: true })

// 清除定时器
function clearSuccessTimer() {
  if (successTimer) {
    clearTimeout(successTimer)
    successTimer = null
  }
}

// 点击处理（失败状态可点击重试）
function handleClick() {
  if (props.status === 'failed') {
    emit('retry')
  }
}

// 组件卸载时清理定时器
onUnmounted(() => {
  clearSuccessTimer()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-send-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 12px;
  cursor: default;
  transition: all var(--dt-transition-fast);

  // 失败状态可点击
  &.status-failed {
    cursor: pointer;

    &:hover {
      background: rgba(255, 73, 73, 0.1);

      .status-text {
        color: var(--dt-color-danger);
      }
    }
  }
}

.status-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  font-size: 16px;

  .el-icon {
    font-size: inherit;
  }
}

// 发送中：灰色 + 旋转动画（使用全局 rotate 动画）
.sending-icon {
  color: var(--dt-text-tertiary);

  .el-icon.is-loading {
    animation: rotate 2s linear infinite;
  }
}

// 发送成功：绿色（使用全局 scale-in 动画）
.success-icon {
  color: var(--dt-color-success);

  animation: scale-in 0.3s ease-out;
}

// 发送失败：红色（使用全局 shake 动画）
.failed-icon {
  color: var(--dt-color-danger);

  animation: shake 0.4s ease-in-out;
}

.status-text {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  transition: color var(--dt-transition-fast);
}

// 动画使用全局定义 (@/styles/animations.scss):
// - rotate: 旋转动画
// - scale-in: 缩放淡入
// - shake: 抖动动画

// 暗色模式
.dark {
  .sending-icon {
    color: var(--dt-text-tertiary-dark);
  }

  .status-text {
    color: var(--dt-text-tertiary-dark);
  }

  .status-failed:hover {
    background: rgba(255, 73, 73, 0.2);

    .status-text {
      color: var(--dt-color-danger);
    }
  }
}
</style>
