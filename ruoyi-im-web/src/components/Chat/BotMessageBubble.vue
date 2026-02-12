/**
 * 机器人/通知消息气泡组件
 * 参考野火IM的机器人消息样式
 * 支持已读回执、确认操作等
 */
<template>
  <div
    class="bot-message-bubble"
    :class="`bot-${msgType}`"
  >
    <!-- 顶部装饰线 -->
    <div class="bot-decoration-line" />

    <!-- 头部区域 -->
    <div class="bot-header">
      <div class="bot-icon-wrapper">
        <div
          class="bot-icon"
          :class="`priority-${priorityClass}`"
        >
          <span class="material-icons-outlined">{{ botIcon }}</span>
        </div>
        <div
          class="icon-glow"
          :class="`priority-${priorityClass}`"
        />
      </div>
      <div class="bot-title">
        <div class="bot-label-wrapper">
          <span class="bot-label">{{ botLabel }}</span>
          <div class="label-glow" />
        </div>
        <span
          v-if="isUrgent"
          class="priority-badge priority-urgent"
        >
          <span class="material-icons-outlined priority-icon">flash_on</span>
          <span>紧急</span>
          <div class="priority-pulse" />
        </span>
        <span
          v-else
          class="priority-badge priority-normal"
        >
          <span class="material-icons-outlined priority-icon">notifications</span>
          <span>普通</span>
        </span>
        <span class="bot-type-label">{{ typeLabel }}</span>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="bot-content">
      {{ content }}
    </div>

    <!-- 底部区域 -->
    <div class="bot-footer">
      <!-- 已读状态 -->
      <div
        v-if="showReadStatus"
        class="read-status"
      >
        <div class="read-info">
          <span class="read-count">
            <span class="material-icons-outlined read-icon">visibility</span>
            已读 {{ readCount }}/{{ sendCount }} 人
          </span>
          <span class="read-percentage">{{ readPercentage }}%</span>
        </div>
        <div class="progress-wrapper">
          <div class="progress-bar">
            <div
              class="progress-fill"
              :class="`priority-${priorityClass}`"
              :style="{ width: `${readPercentage}%` }"
            >
              <div class="progress-glow" />
            </div>
          </div>
          <div class="progress-bg" />
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="bot-actions">
        <button
          v-if="needsConfirm && !isConfirmed && !isSender"
          class="action-btn action-btn--primary"
          @click="handleConfirm"
        >
          <span class="material-icons-outlined btn-icon">check</span>
          <span>确认</span>
        </button>
        <button
          v-if="needsConfirm && !isConfirmed"
          class="action-btn action-btn--secondary"
          @click="handleReject"
        >
          <span class="material-icons-outlined btn-icon">close</span>
          <span>拒收</span>
        </button>
        <button
          v-if="isConfirmed"
          class="action-btn action-btn--success"
          disabled
        >
          <span class="material-icons-outlined btn-icon">done_all</span>
          <span>已确认</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 消息类型
  msgType: {
    type: String,
    default: 'notification' // notification, announcement, reminder, approval
  },
  // 消息内容
  content: {
    type: String,
    default: ''
  },
  // 是否紧急
  isUrgent: {
    type: Boolean,
    default: false
  },
  // 优先级
  priority: {
    type: String,
    default: 'normal' // normal, urgent, critical
  },
  // 已读数量
  readCount: {
    type: Number,
    default: 0
  },
  // 总发送数量
  sendCount: {
    type: Number,
    default: 1
  },
  // 是否需要确认
  needsConfirm: {
    type: Boolean,
    default: false
  },
  // 是否已确认
  isConfirmed: {
    type: Boolean,
    default: false
  },
  // 是否显示已读状态
  showReadStatus: {
    type: Boolean,
    default: true
  },
  // 是否为发送者
  isSender: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['confirm', 'reject', 'mark-read'])

// 计算属性
const priorityClass = computed(() => {
  if (props.isUrgent || props.priority === 'critical') {return 'critical'}
  if (props.priority === 'urgent') {return 'urgent'}
  return 'normal'
})

const botLabel = computed(() => {
  if (props.msgType === 'announcement') {return '公告'}
  if (props.msgType === 'reminder') {return '提醒'}
  if (props.msgType === 'approval') {return '审批'}
  if (props.msgType === 'bot') {return '机器人'}
  return '通知'
})

const botIcon = computed(() => {
  if (props.msgType === 'announcement') {return 'campaign'}
  if (props.msgType === 'reminder') {return 'alarm'}
  if (props.msgType === 'approval') {return 'assignment_turned_in'}
  if (props.msgType === 'bot') {return 'smart_toy'}
  return 'notifications'
})

const typeLabel = computed(() => {
  const labels = {
    notification: '系统通知',
    announcement: '群公告',
    reminder: '定时提醒',
    approval: '审批通知',
    bot: '机器人消息'
  }
  return labels[props.msgType] || '通知'
})

const readPercentage = computed(() => {
  if (props.sendCount === 0) {return 0}
  return Math.round((props.readCount / props.sendCount) * 100)
})

// 方法
const handleConfirm = () => {
  emit('confirm')
}

const handleReject = () => {
  emit('reject')
}
</script>

<style lang="scss" scoped>
.bot-message-bubble {
  position: relative;
  padding: 12px 16px;
  border-radius: var(--dt-radius-lg, 12px);
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: 1px solid var(--dt-border-color, #E5E9EF);
  min-width: 280px;
  max-width: 400px;

  &.priority-critical {
    background: linear-gradient(135deg, #fff5f5 0%, #ffebee 100%);
    border-color: var(--dt-error-border, #EF9A9A);
  }
}

.bot-decoration-line {
  position: absolute;
  top: 0;
  left: 16px;
  right: 16px;
  height: 2px;
  background: linear-gradient(90deg,
    transparent 0%,
    var(--dt-brand-color) 50%,
    transparent 100%
  );
  border-radius: 1px;

  .priority-critical & {
    background: linear-gradient(90deg,
      transparent 0%,
      var(--dt-error-color, #F44336) 50%,
      transparent 100%
    );
  }
}

.bot-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.bot-icon-wrapper {
  position: relative;
  flex-shrink: 0;
}

.bot-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: var(--dt-brand-bg, #E5F2FF);
  color: var(--dt-brand-color);

  &.priority-urgent {
    background: var(--dt-warning-bg, #FFF3E0);
    color: var(--dt-warning-color, #FF9800);
  }

  &.priority-critical {
    background: var(--dt-error-bg, #FFEBEE);
    color: var(--dt-error-color, #F44336);
  }

  .material-icons-outlined {
    font-size: 20px;
  }
}

.icon-glow {
  position: absolute;
  inset: -4px;
  border-radius: 14px;
  background: inherit;
  opacity: 0.3;
  filter: blur(8px);
  z-index: -1;
}

.bot-title {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.bot-label-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.bot-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-brand-color);
  letter-spacing: 0.5px;
}

.label-glow {
  position: absolute;
  inset: -2px;
  background: var(--dt-brand-color);
  opacity: 0.2;
  filter: blur(4px);
  z-index: -1;
}

.priority-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 16px;
  font-size: 11px;
  font-weight: 500;

  &.priority-urgent {
    background: var(--dt-warning-bg, #FFF3E0);
    color: var(--dt-warning-color, #FF9800);
  }

  &.priority-normal {
    background: var(--dt-bg-card, #FFFFFF);
    color: var(--dt-text-secondary, #5F6672);
  }

  .priority-icon {
    font-size: 12px;
  }
}

.priority-pulse {
  position: absolute;
  inset: 0;
  border-radius: 16px;
  background: var(--dt-warning-color, #FF9800);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0; transform: scale(1.1); }
}

.bot-type-label {
  font-size: 12px;
  color: var(--dt-text-tertiary, #858E9E);
}

.bot-content {
  margin-bottom: 12px;
  padding: 12px;
  background: var(--dt-bg-card, #FFFFFF);
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--dt-text-primary, #171A1D);
  word-break: break-word;
}

.bot-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.read-status {
  flex: 1;
}

.read-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.read-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--dt-text-secondary, #5F6672);

  .read-icon {
    font-size: 14px;
  }
}

.read-percentage {
  font-size: 12px;
  font-weight: 600;
  color: var(--dt-brand-color);
}

.progress-wrapper {
  position: relative;
  height: 4px;
}

.progress-bar {
  position: relative;
  height: 100%;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 2px;
  background: var(--dt-brand-color);
  transition: width 0.3s ease;

  &.priority-urgent {
    background: var(--dt-warning-color, #FF9800);
  }

  &.priority-critical {
    background: var(--dt-error-color, #F44336);
  }
}

.progress-glow {
  position: absolute;
  inset: 0;
  background: inherit;
  filter: blur(4px);
}

.progress-bg {
  position: absolute;
  inset: 0;
  background: var(--dt-border-lighter, #F5F7FA);
  border-radius: 2px;
}

.bot-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &--primary {
    background: var(--dt-brand-color);
    color: white;

    &:hover {
      background: var(--dt-brand-hover, #006ECC);
    }
  }

  &--secondary {
    background: var(--dt-bg-card, #FFFFFF);
    color: var(--dt-text-secondary, #5F6672);
    border: 1px solid var(--dt-border-color, #E5E9EF);

    &:hover {
      background: var(--dt-bg-card-hover, #FAFBFC);
    }
  }

  &--success {
    background: var(--dt-success-color, #00C853);
    color: white;
    cursor: default;
  }

  .btn-icon {
    font-size: 16px;
  }
}

// 深色主题
.dark {
  .bot-message-bubble {
    background: linear-gradient(135deg, #2a2a2a 0%, #1f1f1f 100%);
    border-color: var(--dt-border-dark, var(--dt-border-alpha-dark));
  }

  .bot-content {
    background: var(--dt-bg-card-dark, var(--dt-bg-hover-dark));
    color: var(--dt-text-primary-dark, var(--dt-text-secondary-inverted));
  }
}
</style>
