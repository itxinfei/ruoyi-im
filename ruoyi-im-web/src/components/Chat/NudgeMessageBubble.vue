<template>
  <div class="nudge-message-bubble">
    <!-- 头像 -->
    <div class="nudge-avatar" @click="$emit('show-user', nudge.nudgerId)">
      <DingtalkAvatar
        :src="nudge.nudgerAvatar"
        :name="nudge.nudgerName"
        :user-id="nudge.nudgerId"
        :size="36"
        shape="square"
      />
    </div>

    <!-- 消息内容 -->
    <div class="nudge-content">
      <!-- 发送者昵称 -->
      <div class="sender-name">{{ nudge.nudgerName }}</div>

      <!-- 拍一拍提示 -->
      <div class="nudge-hint">
        <span class="nudge-icon">👋</span>
        <span class="nudge-text">{{ nudgeHint }}</span>
        <span v-if="nudge.nudgeCount > 1" class="nudge-count">×{{ nudge.nudgeCount }}</span>
      </div>

      <!-- 时间戳 -->
      <div class="message-time">{{ formattedTime }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  nudge: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['show-user'])

// 拍一拍提示文本
const nudgeHint = computed(() => {
  if (props.nudge.nudgeCount === 1) {
    return `拍了拍${props.nudge.nudgedUserName || '对方'}`
  } else if (props.nudge.nudgeCount === 2) {
    return `拍了拍${props.nudge.nudgedUserName || '对方'}两下`
  } else if (props.nudge.nudgeCount === 3) {
    return `拍了拍${props.nudge.nudgedUserName || '对方'}三下`
  } else if (props.nudge.nudgeCount <= 5) {
    return `疯狂拍了拍${props.nudge.nudgedUserName || '对方'}`
  } else {
    return `把${props.nudge.nudgedUserName || '对方'}拍了冒烟了`
  }
})

// 格式化时间
const formattedTime = computed(() => {
  if (!props.nudge.createTime) return ''
  const date = new Date(props.nudge.createTime)
  const now = new Date()
  const diff = now - date

  // 小于1分钟
  if (diff < 60000) return '刚刚'
  // 小于1小时
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  // 今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 更早
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.nudge-message-bubble {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  animation: nudge-appear 0.3s ease-out;

  @keyframes nudge-appear {
    0% {
      opacity: 0;
      transform: scale(0.9) translateY(-10px);
    }
    50% {
      transform: scale(1.02) translateY(2px);
    }
    100% {
      opacity: 1;
      transform: scale(1) translateY(0);
    }
  }

  .nudge-avatar {
    flex-shrink: 0;
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
      transform: scale(1.05);
    }
  }

  .nudge-content {
    display: flex;
    flex-direction: column;
    gap: 4px;
    max-width: 70%;

    .sender-name {
      font-size: 12px;
      color: var(--dt-text-tertiary);
      margin-left: 2px;
    }

    .nudge-hint {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 8px 12px;
      background: linear-gradient(135deg, #fff9e6 0%, #fff4d6 100%);
      border: 1px solid #ffeaa7;
      border-radius: 12px;
      font-size: 14px;
      color: var(--dt-text-primary);
      box-shadow: 0 2px 8px rgba(255, 193, 7, 0.15);

      .nudge-icon {
        font-size: 18px;
        animation: wave 0.5s ease-in-out 3;

        @keyframes wave {
          0%, 100% {
            transform: rotate(0deg);
          }
          25% {
            transform: rotate(20deg);
          }
          75% {
            transform: rotate(-20deg);
          }
        }
      }

      .nudge-text {
        font-weight: 500;
      }

      .nudge-count {
        font-size: 12px;
        color: #f39c12;
        font-weight: 600;
      }
    }

    .message-time {
      font-size: 11px;
      color: var(--dt-text-placeholder);
      margin-left: 4px;
    }
  }
}

// 暗色模式适配
.dark .nudge-message-bubble {
  .nudge-content {
    .nudge-hint {
      background: linear-gradient(135deg, #4a4020 0%, #3d3618 100%);
      border-color: #5c4d20;
    }
  }
}
</style>
