<template>
  <div v-if="message.isOwn" class="message-status" :class="`status-${uiStatus}`">
    <!-- 发送中：转圈动画 + 消息半透明 -->
    <transition name="status-fade">
      <div v-if="uiStatus === 'sending'" class="status-indicator status-sending" title="发送中">
        <span class="material-icons-outlined rotating">sync</span>
      </div>
    </transition>

    <!-- 发送成功/已送达：灰色对勾 -->
    <transition name="status-scale">
      <div v-if="uiStatus === 'delivered'" class="status-indicator status-delivered" title="已送达">
        <span class="material-icons-outlined">done</span>
      </div>
    </transition>

    <!-- 已读 -->
    <transition name="status-scale">
      <div
        v-if="uiStatus === 'read'"
        class="status-indicator status-read"
        :class="{ 'is-clickable': showReadInfo }"
        :title="readTooltip"
        @click="handleShowReadInfo"
      >
        <span class="material-icons-outlined">done_all</span>
      </div>
    </transition>

    <!-- 已读信息（群聊显示人数） -->
    <div v-if="uiStatus === 'read' && isGroupChat && readCount > 0" class="read-count">
      {{ readCountText }}
    </div>

    <!-- 发送失败：红色感叹号 + 红色背景 + 点击重发按钮 -->
    <transition name="status-shake">
      <div
        v-if="uiStatus === 'failed'"
        class="status-indicator status-failed-wrapper"
        @click="handleRetry"
      >
        <!-- 红色背景 -->
        <div class="failed-background"></div>
        <!-- 红色感叹号 -->
        <span class="material-icons-outlined failed-icon">error_outline</span>
        <!-- 重发按钮（悬停显示） -->
        <div class="retry-button">
          <span class="material-icons-outlined">refresh</span>
          <span class="retry-text">点击重发</span>
        </div>
        <!-- 失败原因提示 -->
        <div class="error-tooltip">
          {{ errorText }}
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { formatRelativeTime } from '@/utils/message'
import { MessageStatus } from '@/utils/message'

const props = defineProps({
  message: { type: Object, required: true },
  sessionType: { type: String, default: 'PRIVATE' }
})

const emit = defineEmits(['retry', 'show-read-info'])

// 映射后端 sendStatus 到前端 UI 状态
const uiStatus = computed(() => {
  const sendStatus = props.message.sendStatus || props.message.status

  // 如果是字符串，直接使用
  if (typeof sendStatus === 'string') {
    if (sendStatus === 'PENDING' || sendStatus === 'SENDING') return 'sending'
    if (sendStatus === 'DELIVERED') return 'delivered'
    if (sendStatus === 'READ') return 'read'
    if (sendStatus === 'FAILED') return 'failed'
  }

  // 如果是数字，映射到对应状态
  const statusMap = {
    0: 'sending',  // PENDING
    1: 'sending',  // SENDING
    2: 'delivered', // DELIVERED
    3: 'read',     // READ
    4: 'failed'    // FAILED
  }

  return statusMap[sendStatus] || 'delivered'
})

// 是否是群聊
const isGroupChat = computed(() => {
  return props.sessionType === 'GROUP'
})

// 已读人数
const readCount = computed(() => {
  return props.message.readCount || 0
})

// 已读人数文本
const readCountText = computed(() => {
  if (!isGroupChat.value) return ''
  const count = readCount.value
  const totalCount = props.message.totalMembers || 0
  return totalCount > 0 ? `${count}/${totalCount}` : `${count}`
})

// 是否显示已读信息
const showReadInfo = computed(() => {
  return uiStatus.value === 'read' && (isGroupChat.value || props.message.readTime)
})

// 已读提示文本
const readTooltip = computed(() => {
  if (!showReadInfo.value) return '已读'

  // 单聊：显示已读时间
  if (!isGroupChat.value && props.message.readTime) {
    const readTime = formatRelativeTime(props.message.readTime)
    return `已读 ${readTime}`
  }

  // 群聊：显示人数
  if (isGroupChat.value) {
    return `${readCount.value}人已读，点击查看详情`
  }

  return '已读'
})

// 失败原因文本
const errorText = computed(() => {
  const errorCode = props.message.sendErrorCode || props.message.errorCode

  const errorMessages = {
    'NETWORK_ERROR': '网络错误，请检查网络连接',
    'TIMEOUT': '请求超时，请稍后重试',
    'SERVER_ERROR': '服务器错误，请稍后重试',
    'RATE_LIMIT': '发送过于频繁，请稍后再试',
    'FORBIDDEN': '无权限发送此消息'
  }

  return errorMessages[errorCode] || '发送失败，点击重发'
})

/**
 * 显示已读信息
 */
function handleShowReadInfo() {
  if (!showReadInfo.value) return

  emit('show-read-info', {
    messageId: props.message.id,
    readCount: readCount.value,
    readBy: props.message.readBy || [],
    readTime: props.message.readTime,
    isGroupChat: isGroupChat.value
  })
}

/**
 * 点击重发
 */
function handleRetry() {
  emit('retry', props.message)
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-status {
  display: flex;
  align-items: center;
  margin-left: 8px;  // 方案A: 6px → 8px，增加间距让状态更独立
  margin-bottom: 2px;  // 方案A: 新增，稍微向下偏移垂直居中
  font-size: 14px;
  cursor: pointer;
  transition: all var(--dt-transition-base);
  align-self: flex-end;
}

.status-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;  // 方案A: 18px → 20px，增大尺寸更易看清
  height: 20px;  // 方案A: 18px → 20px
  border-radius: 50%;
  transition: all var(--dt-transition-base);

  .material-icons-outlined {
    font-size: 18px;  // 方案A: 16px → 18px
  }
}

// ========== 发送中状态：转圈动画 + 脉冲环效果 ==========
.status-sending {
  color: var(--dt-text-tertiary);
  position: relative;

  // 方案A: 加快动画速度，更有"处理中"的感觉
  .rotating {
    animation: rotate 0.6s linear infinite;  // 1s → 0.6s
  }

  // 方案B: 添加脉冲环效果，让发送中状态更醒目
  .status-indicator {
    &::after {
      content: '';
      position: absolute;
      inset: -2px;
      border-radius: 50%;
      border: 1px solid var(--dt-text-tertiary);
      opacity: 0;
      animation: pulse-ring 1.5s ease-out infinite;
    }
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 方案B: 脉冲环动画
@keyframes pulse-ring {
  0% {
    transform: scale(1);
    opacity: 0.5;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

// ========== 已送达状态：灰色对勾（钉钉标准）==========
.status-delivered {
  color: #999999; // 钉钉标准：灰色对勾

  &:hover {
    transform: scale(1.1);
  }
}

// ========== 已读状态：蓝色双对勾 ==========
.status-read {
  color: var(--dt-brand-color);

  .is-clickable {
    cursor: pointer;
    padding: 2px;  // 方案A: 新增内边距，增大点击区域

    &:hover {
      transform: scale(1.1);
      background: var(--dt-brand-bg);  // 方案A: 新增背景色，hover 更明显
      border-radius: 4px;
    }
  }
}

// ========== 发送失败状态：红色感叹号 + 红色背景 + 重发按钮 + 闪烁提示 ==========
.status-failed-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 2px;

  // 方案B: 失败状态闪烁3次，吸引用户注意
  animation: failed-blink 2s ease-in-out 3;  // 闪烁 3 次

  // 方案A: 增强红色背景可见性
  .failed-background {
    position: absolute;
    inset: 0;
    background: rgba(244, 67, 54, 0.2);  // 方案A: 0.1 → 0.2，增强可见性
    border: 1px solid rgba(244, 67, 54, 0.3);  // 方案A: 新增边框
    border-radius: 50%;
    opacity: 1;
    transition: opacity var(--dt-transition-base);
  }

  // 方案B: 失败闪烁动画
  @keyframes failed-blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.5; }
  }

  // 红色感叹号
  .failed-icon {
    position: relative;
    z-index: 1;
    color: #F44336; // 钉钉标准：红色
    font-size: 18px;
  }

  // 重发按钮（默认隐藏）
  .retry-button {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%) scale(0.8);
    z-index: 10;
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 12px;
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    opacity: 0;
    pointer-events: none;
    transition: all var(--dt-transition-base);
    white-space: nowrap;

    .material-icons-outlined {
      font-size: 14px;
      color: #F44336;
    }

    .retry-text {
      font-size: 12px;
      color: #333;
      font-weight: 500;
    }
  }

  // 失败原因提示（悬停显示）
  .error-tooltip {
    position: absolute;
    bottom: calc(100% + 8px);
    left: 50%;
    transform: translateX(-50%);
    padding: 6px 12px;
    background: rgba(0, 0, 0, 0.8);
    color: #fff;
    font-size: 12px;
    border-radius: 4px;
    white-space: nowrap;
    opacity: 0;
    pointer-events: none;
    transition: opacity var(--dt-transition-base);
    z-index: 100;
  }

  // 悬停时显示重发按钮和错误提示
  &:hover {
    transform: scale(1.15);

    .failed-background {
      opacity: 1;
    }

    .retry-button {
      opacity: 1;
      pointer-events: auto;
    }

    .error-tooltip {
      opacity: 1;
    }
  }
}

// ========== 已读人数显示（群聊）==========
.read-count {
  font-size: 11px;
  color: var(--dt-brand-color);
  margin-left: 4px;
  font-weight: 500;
  user-select: none;
}

// ========== 状态过渡动画 ==========
.status-fade-enter-active,
.status-fade-leave-active {
  transition: all 0.2s var(--dt-ease-out);
}

.status-fade-enter-from,
.status-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

// 方案A: 优化已送达/已读的进入动画，更有弹性
.status-scale-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);  // 0.3s → 0.4s
}

.status-scale-leave-active {
  transition: all 0.2s var(--dt-ease-out);
}

.status-scale-enter-from {
  opacity: 0;
  transform: scale(0) rotate(-15deg);  // 方案A: 添加旋转，进入更生动
}

.status-scale-leave-to {
  opacity: 0;
  transform: scale(0.5);
}

.status-shake-enter-active {
  animation: shake 0.5s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

.status-shake-leave-active {
  transition: all 0.2s var(--dt-ease-out);
}

.status-shake-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}
</style>
