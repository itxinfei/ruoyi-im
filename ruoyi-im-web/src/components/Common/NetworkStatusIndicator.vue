/**
* 网络状态指示器组件
* 显示网络连接状态、WebSocket连接状态
*/
<template>
  <div
    v-if="showIndicator"
    class="network-status-indicator"
    :class="statusClass"
    @click="handleClick"
  >
    <el-icon class="status-icon">
      <component :is="statusIcon" />
    </el-icon>
    <span class="status-text">{{ statusText }}</span>
    <el-badge
      v-if="queueInfo.total > 0"
      :value="queueInfo.total"
      :max="99"
      class="queue-badge"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import {
  Wifi, WifiOff, SignalCellular4Bar, SignalCellular3Bar,
  SignalCellular2Bar, SignalCellular1Bar, Loading, WarningFilled
} from '@element-plus/icons-vue'

const props = defineProps({
  // 是否始终显示（默认仅在异常状态时显示）
  alwaysShow: { type: Boolean, default: false },
  // 是否显示队列数量
  showQueue: { type: Boolean, default: true }
})

const emit = defineEmits(['click'])

const store = useStore()

// 网络在线状态
const isOnline = computed(() => navigator.onLine)

// WebSocket 连接状态
const wsConnected = computed(() => store.state.im?.wsConnected || false)

// 队列信息
const queueInfo = computed(() => ({
  sending: store.getters['im/message/sendingQueueSize'] || 0,
  failed: store.getters['im/message/failedRetryQueueSize'] || 0,
  total: (store.getters['im/message/sendingQueueSize'] || 0) +
         (store.getters['im/message/failedRetryQueueSize'] || 0)
}))

// 是否显示指示器
const showIndicator = computed(() => {
  return props.alwaysShow || !isOnline.value || !wsConnected.value || queueInfo.value.total > 0
})

// 连接状态
const connectionStatus = computed(() => {
  if (!isOnline.value) return 'offline'
  if (!wsConnected.value) return 'disconnected'
  if (queueInfo.value.failed > 0) return 'has-failed'
  if (queueInfo.value.sending > 0) return 'sending'
  return 'connected'
})

// 状态对应的CSS类
const statusClass = computed(() => {
  return `status-${connectionStatus.value}`
})

// 状态图标
const statusIcon = computed(() => {
  switch (connectionStatus.value) {
    case 'offline':
      return WifiOff
    case 'disconnected':
      return WarningFilled
    case 'has-failed':
      return WarningFilled
    case 'sending':
      return Loading
    default:
      return Wifi
  }
})

// 状态文本
const statusText = computed(() => {
  switch (connectionStatus.value) {
    case 'offline':
      return '网络离线'
    case 'disconnected':
      return '连接断开'
    case 'has-failed':
      return `${queueInfo.value.failed}条失败`
    case 'sending':
      return '发送中...'
    default:
      return ''
  }
})

const handleClick = () => {
  emit('click', {
    isOnline: isOnline.value,
    wsConnected: wsConnected.value,
    queueInfo: queueInfo.value
  })
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.network-status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: var(--dt-radius-full);
  font-size: var(--dt-font-size-xs);
  cursor: pointer;
  transition: all 0.2s;

  .status-icon {
    font-size: 14px;
  }

  .status-text {
    white-space: nowrap;
  }

  .queue-badge {
    :deep(.el-badge__content) {
      background: var(--dt-error-color);
    }
  }

  // 离线状态
  &.status-offline {
    background: var(--dt-error-bg);
    color: var(--dt-error-color);

    .status-icon {
      animation: pulse 2s ease-in-out infinite;
    }
  }

  // 断连状态
  &.status-disconnected {
    background: var(--dt-warning-bg);
    color: var(--dt-warning-color);
  }

  // 有失败消息
  &.status-has-failed {
    background: var(--dt-warning-bg);
    color: var(--dt-warning-color);

    .status-icon {
      animation: shake 0.5s ease-in-out;
    }
  }

  // 发送中
  &.status-sending {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);

    .status-icon {
      animation: spin 1s linear infinite;
    }
  }

  &:hover {
    opacity: 0.85;
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-2px);
  }
  75% {
    transform: translateX(2px);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 暗色模式
:global(.dark) {
  .status-offline {
    background: var(--dt-error-bg);
    color: var(--dt-error-color);
  }

  .status-disconnected,
  .status-has-failed {
    background: var(--dt-warning-bg);
    color: var(--dt-warning-color);
  }

  .status-sending {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }
}
</style>
