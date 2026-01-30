<template>
  <div class="ding-message-bubble" :class="`ding-${dingType}`">
    <!-- 顶部装饰线 -->
    <div class="ding-decoration-line"></div>

    <!-- 头部区域 -->
    <div class="ding-header">
      <div class="ding-icon-wrapper">
        <div class="ding-icon" :class="`priority-${priorityClass}`">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="currentColor"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            <circle cx="12" cy="12" r="3" fill="white"/>
          </svg>
        </div>
        <div class="icon-glow" :class="`priority-${priorityClass}`"></div>
      </div>
      <div class="ding-title">
        <div class="ding-label-wrapper">
          <span class="ding-label">DING</span>
          <div class="label-glow"></div>
        </div>
        <span v-if="isUrgent" class="priority-badge priority-urgent">
          <svg class="priority-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" fill="currentColor"/>
          </svg>
          <span>紧急</span>
          <div class="priority-pulse"></div>
        </span>
        <span v-else class="priority-badge priority-normal">
          <svg class="priority-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="2"/>
            <path d="M12 8V12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span>普通</span>
        </span>
        <span class="ding-type-label">{{ dingTypeLabel }}</span>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="ding-content">
      {{ content }}
    </div>

    <!-- 底部区域 -->
    <div class="ding-footer">
      <div class="read-status">
        <div class="read-info">
          <span class="read-count">
            <svg class="read-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 4.5C7 4.5 2.73 7.61 1 12C2.73 16.39 7 19.5 12 19.5C17 19.5 21.27 16.39 23 12C21.27 7.61 17 4.5 12 4.5ZM12 17C9.24 17 7 14.76 7 12C7 9.24 9.24 7 12 7C14.76 7 17 9.24 17 12C17 14.76 14.76 17 12 17ZM12 9C10.34 9 9 10.34 9 12C9 13.66 10.34 15 12 15C13.66 15 15 13.66 15 12C15 10.34 13.66 9 12 9Z" fill="currentColor"/>
            </svg>
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
              <div class="progress-glow"></div>
            </div>
          </div>
          <div class="progress-bg"></div>
        </div>
      </div>
      <div class="ding-actions">
        <button
          v-if="!isRead && !isSender"
          class="action-btn action-btn--primary"
          @click="handleMarkAsRead"
        >
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 16.17L4.83 12L3.41 13.41L9 19L21 7L19.59 5.59L9 16.17Z" fill="currentColor"/>
          </svg>
          <span>标记已读</span>
        </button>
        <button
          v-if="isSender"
          class="action-btn action-btn--danger"
          @click="handleCancel"
        >
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 6.41L17.59 5L12 10.59L6.41 5L5 6.41L10.59 12L5 17.59L6.41 19L12 13.41L17.59 19L19 17.59L13.41 12L19 6.41Z" fill="currentColor"/>
          </svg>
          <span>取消DING</span>
        </button>
        <button class="action-btn action-btn--default" @click="handleViewDetail">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M14 2H6C4.9 2 4 2.9 4 4V20C4 21.1 4.89 22 5.99 22H18C19.1 22 20 21.1 20 20V8L14 2ZM16 18H8V16H16V18ZM16 14H8V12H16V14ZM13 9V3.5L18.5 9H13Z" fill="currentColor"/>
          </svg>
          <span>查看详情</span>
        </button>
      </div>
    </div>

    <!-- 过期时间 -->
    <div v-if="expireTime" class="ding-expire">
      <svg class="expire-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M11.99 2C6.47 2 2 6.48 2 12C2 17.52 6.47 22 11.99 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 11.99 2ZM12 20C7.58 20 4 16.42 4 12C4 7.58 7.58 4 12 4C16.42 4 20 7.58 20 12C20 16.42 16.42 20 12 20ZM12.5 7H11V13L16.25 16.15L17 14.92L12.5 12.25V7Z" fill="currentColor"/>
      </svg>
      <span>过期时间：{{ formatTime(expireTime) }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessageBox } from 'element-plus'
import { messageSuccess, messageError, confirmDelete } from '@/utils/ui'
import { formatDateTimeISO } from '@/utils/format'
import { markDingAsRead, cancelDing } from '@/api/im/ding'

const props = defineProps({
  id: {
    type: Number,
    required: true
  },
  content: {
    type: String,
    default: ''
  },
  dingType: {
    type: String,
    default: 'APP'
  },
  isUrgent: {
    type: Number,
    default: 0
  },
  readCount: {
    type: Number,
    default: 0
  },
  sendCount: {
    type: Number,
    default: 0
  },
  isRead: {
    type: Boolean,
    default: false
  },
  isSender: {
    type: Boolean,
    default: false
  },
  expireTime: {
    type: String,
    default: ''
  },
  status: {
    type: String,
    default: 'ACTIVE'
  }
})

// 计算优先级样式类
const priorityClass = computed(() => {
  return props.isUrgent === 1 ? 'urgent' : 'normal'
})

// 是否紧急
const isUrgent = computed(() => {
  return props.isUrgent === 1
})

const emit = defineEmits(['read', 'cancel', 'detail'])

const dingTypeLabel = computed(() => {
  const typeMap = {
    'APP': '应用内提醒',
    'SMS': '短信提醒',
    'CALL': '电话提醒'
  }
  return typeMap[props.dingType] || '应用内提醒'
})

const readPercentage = computed(() => {
  if (props.sendCount === 0) return 0
  return Math.round((props.readCount / props.sendCount) * 100)
})

const formatTime = (time) => {
  return formatDateTimeISO(time)
}

const handleMarkAsRead = async () => {
  try {
    await markDingAsRead(props.id)
    messageSuccess('已标记为已读')
    emit('read', props.id)
  } catch (error) {
    messageError('标记失败')
  }
}

const handleCancel = async () => {
  if (!await confirm('确定要取消此DING消息吗？', '确认')) {
    return
  }
  try {
    await cancelDing(props.id)
    messageSuccess('DING已取消')
    emit('cancel', props.id)
  } catch (error) {
    messageError('取消失败')
  }
}

const handleViewDetail = () => {
  emit('detail', props.id)
}
</script>

<style lang="scss" scoped>
// ============================================================================
// 容器
// ============================================================================
.ding-message-bubble {
  position: relative;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 16px;
  padding: 20px;
  min-width: 300px;
  max-width: 450px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0) 0%, rgba(0, 0, 0, 0.02) 100%);
    transition: opacity 0.3s;
    opacity: 0;
  }

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);

    &::before {
      opacity: 1;
    }

    .ding-icon {
      transform: scale(1.1) rotate(-5deg);
    }
  }

  // 不同类型的渐变背景
  &.ding-APP {
    background: linear-gradient(135deg, #e8f4ff 0%, #f0f7ff 100%);
    border-color: rgba(0, 137, 255, 0.2);
  }

  &.ding-SMS {
    background: linear-gradient(135deg, #fff7e6 0%, #fff9ed 100%);
    border-color: rgba(250, 173, 20, 0.2);
  }

  &.ding-CALL {
    background: linear-gradient(135deg, #fff1f0 0%, #fff5f3 100%);
    border-color: rgba(245, 74, 69, 0.2);
  }
}

// ============================================================================
// 装饰线
// ============================================================================
.ding-decoration-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--dt-brand-color) 0%, var(--dt-brand-hover) 100%);
  opacity: 0.8;
}

// ============================================================================
// 头部区域
// ============================================================================
.ding-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
  position: relative;
  z-index: 1;
}

.ding-icon-wrapper {
  position: relative;
  flex-shrink: 0;
}

.ding-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
  z-index: 1;
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

  svg {
    width: 22px;
    height: 22px;
  }

  &.priority-urgent {
    background: linear-gradient(135deg, #f54a45 0%, #d32f2f 100%);
    animation: urgentPulse 2s ease-in-out infinite;
  }

  // 使用全局动画定义
  &.priority-normal {
    background: linear-gradient(135deg, #0089FF 0%, #006ECC 100%);
  }
}

.icon-glow {
  position: absolute;
  inset: -8px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.3;
  animation: iconGlowPulse 3s ease-in-out infinite;
  z-index: 0;

  &.priority-urgent {
    background: #f54a45;
    animation: urgentGlow 1.5s ease-in-out infinite;
  }

  &.priority-normal {
    background: #0089FF;
  }
}

.ding-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  flex-wrap: wrap;
}

.ding-label-wrapper {
  position: relative;
}

.ding-label {
  font-weight: 800;
  font-size: 18px;
  background: linear-gradient(135deg, #0089FF 0%, #006ECC 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  z-index: 1;
}

.label-glow {
  position: absolute;
  bottom: 2px;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, rgba(0, 137, 255, 0.3) 0%, transparent 100%);
  border-radius: 2px;
}

.priority-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  position: relative;
  overflow: hidden;

  .priority-icon {
    width: 14px;
    height: 14px;
  }

  &.priority-urgent {
    background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
    color: #f54a45;
    border: 1px solid rgba(245, 74, 69, 0.2);
  }

  &.priority-normal {
    background: linear-gradient(135deg, #f0f9ff 0%, #bae7ff 100%);
    color: #0089FF;
    border: 1px solid rgba(0, 137, 255, 0.2);
  }

  .priority-pulse {
    position: absolute;
    inset: 0;
    background: currentColor;
    opacity: 0.1;
    animation: priorityBadgePulse 2s ease-in-out infinite;
  }

  &.priority-urgent .priority-pulse {
    animation: urgentBadgePulse 1.5s ease-in-out infinite;
  }

  @keyframes priorityBadgePulse {
    0%, 100% {
      opacity: 0.1;
    }
    50% {
      opacity: 0.2;
    }
  }

  @keyframes urgentBadgePulse {
    0%, 100% {
      opacity: 0.15;
      transform: scale(1);
    }
    50% {
      opacity: 0.3;
      transform: scale(1.05);
    }
  }
}

.ding-type-label {
  color: #999;
  font-size: 12px;
  font-weight: 500;
}

// ============================================================================
// 内容区域
// ============================================================================
.ding-content {
  color: #1a1a1a;
  font-size: 15px;
  line-height: 1.7;
  margin-bottom: 16px;
  white-space: pre-wrap;
  word-break: break-word;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  position: relative;
  z-index: 1;
}

// ============================================================================
// 底部区域
// ============================================================================
.ding-footer {
  position: relative;
  z-index: 1;
}

.read-status {
  margin-bottom: 14px;
}

.read-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.read-count {
  font-size: 13px;
  color: #666;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;

  .read-icon {
    width: 14px;
    height: 14px;
    color: #999;
  }
}

.read-percentage {
  font-size: 14px;
  font-weight: 700;
  color: #0089FF;
}

.progress-wrapper {
  position: relative;
  height: 8px;
  background: rgba(0, 0, 0, 0.06);
  border-radius: 10px;
  overflow: hidden;
}

.progress-bar {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
}

.progress-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;

  &.priority-urgent {
    background: linear-gradient(90deg, #f54a45 0%, #ff7875 100%);
  }

  &.priority-normal {
    background: linear-gradient(90deg, #0089FF 0%, #4096ff 100%);
  }

  .progress-glow {
    position: absolute;
    top: 0;
    right: 0;
    width: 20px;
    height: 100%;
    background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.4) 100%);
    border-radius: 10px;
    animation: progressShine 2s ease-in-out infinite;
  }

  @keyframes progressShine {
    0% {
      opacity: 0;
      transform: translateX(-20px);
    }
    50% {
      opacity: 1;
    }
    100% {
      opacity: 0;
      transform: translateX(40px);
    }
  }
}

.ding-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  border: none;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  .btn-icon {
    width: 14px;
    height: 14px;
  }

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.2) 50%, transparent 50%);
    opacity: 0;
    transition: opacity 0.3s;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

    &::before {
      opacity: 1;
      animation: btnShimmer 2s infinite;
    }
  }

  &:active {
    transform: translateY(0);
  }

  @keyframes btnShimmer {
    0% {
      background-position: -100% 0;
    }
    100% {
      background-position: 100% 0;
    }
  }

  &--primary {
    background: linear-gradient(135deg, #0089FF 0%, #006ECC 100%);
    color: #fff;
    box-shadow: 0 2px 8px rgba(0, 137, 255, 0.3);
  }

  &--danger {
    background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
    color: #f54a45;
    border: 1px solid rgba(245, 74, 69, 0.2);
  }

  &--default {
    background: rgba(0, 0, 0, 0.04);
    color: #666;
  }
}

// ============================================================================
// 过期时间
// ============================================================================
.ding-expire {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 6px;

  .expire-icon {
    width: 14px;
    height: 14px;
    color: #999;
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
.dark .ding-message-bubble {
  background: linear-gradient(135deg, #1a1f2e 0%, #141925 100%);
  border-color: rgba(255, 255, 255, 0.1);

  &::before {
    background: linear-gradient(135deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.03) 100%);
  }

  &.ding-APP {
    background: linear-gradient(135deg, #0d1d2d 0%, #0a1929 100%);
    border-color: rgba(0, 137, 255, 0.2);
  }

  &.ding-SMS {
    background: linear-gradient(135deg, #2d1f0f 0%, #25190a 100%);
    border-color: rgba(250, 173, 20, 0.2);
  }

  &.ding-CALL {
    background: linear-gradient(135deg, #2d1312 0%, #25100f 100%);
    border-color: rgba(245, 74, 69, 0.2);
  }
}

.dark .ding-content {
  color: #e8e8e8;
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.08);
}

.dark .read-count {
  color: #999;
}

.dark .progress-wrapper {
  background: rgba(255, 255, 255, 0.06);
}

.dark .ding-expire {
  border-color: rgba(255, 255, 255, 0.08);
  color: #888;
}

.dark .action-btn--default {
  background: rgba(255, 255, 255, 0.06);
  color: #999;
}
</style>