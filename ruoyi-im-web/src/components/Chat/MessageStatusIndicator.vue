/**
* 消息发送状态指示器组件
* 显示消息发送状态：发送中、已送达、已读、失败、群聊未读等
* 对标钉钉/野火IM的设计风格
*/
<template>
  <div
    v-if="showStatus"
    class="message-status-indicator"
    :class="statusClass"
  >
    <!-- 发送中 -->
    <template v-if="status === 'sending'">
      <el-icon class="status-icon spinning">
        <Loading />
      </el-icon>
    </template>

    <!-- 已送达 -->
    <template v-else-if="status === 'delivered' && !isGroupChat">
      <el-icon class="status-icon">
        <Select />
      </el-icon>
    </template>

    <!-- 私聊-已读：灰色文字 -->
    <template v-else-if="status === 'read' && !isGroupChat">
      <div
        class="status-text-read"
        title="已读"
        @click="handleShowReadInfo"
      >
        已读
      </div>
    </template>

    <!-- 群聊-未读：蓝色数字圆圈 -->
    <template v-else-if="isGroupChat && !isAllRead">
      <div
        class="status-group-unread"
        :title="`${unreadCount}人未读`"
        @click="handleShowReadInfo"
      >
        {{ unreadCount }}
      </div>
    </template>

    <!-- 群聊-全部已读：灰色文字 -->
    <template v-else-if="isGroupChat && isAllRead">
      <div
        class="status-text-read"
        title="全部已读"
        @click="handleShowReadInfo"
      >
        全部已读
      </div>
    </template>

    <!-- 私聊-未读：蓝色小圆点 -->
    <template v-else-if="status === 'delivered' && !isGroupChat">
      <div class="status-indicator status-unread-dot" title="未读" />
    </template>

    <!-- 已读（带头像） -->
    <template v-else-if="status === 'read' && showAvatars">
      <div class="read-avatar-group">
        <span
          v-for="avatar in displayAvatars"
          :key="avatar.userId"
          class="read-avatar"
          :style="{ backgroundImage: `url(${avatar.avatar})` }"
          :title="avatar.userName"
        />
        <span
          v-if="readCount > 3"
          class="read-more"
        >+{{ readCount - 3 }}</span>
      </div>
    </template>

    <!-- 失败 - 可点击重试 -->
    <el-icon
      v-else-if="status === 'failed'"
      class="status-icon failed"
      @click="handleRetry"
    >
      <WarningFilled />
    </el-icon>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Loading, Select, WarningFilled } from '@element-plus/icons-vue'
import { formatRelativeTime } from '@/utils/message'

const props = defineProps({
  // 发送状态：pending, sending, delivered, read, failed
  status: {
    type: String,
    default: 'pending',
    validator: v => ['pending', 'sending', 'delivered', 'read', 'failed'].includes(v)
  },
  // 消息对象（用于复杂计算）
  message: {
    type: Object,
    default: () => ({})
  },
  // 会话类型：PRIVATE 或 GROUP
  sessionType: {
    type: String,
    default: 'PRIVATE'
  },
  // 已读用户列表（用于群聊已读详情）
  readBy: {
    type: Array,
    default: () => []
  },
  // 已读人数
  readCount: {
    type: Number,
    default: 0
  },
  // 总成员数（群聊用）
  totalMembers: {
    type: Number,
    default: 0
  },
  // 是否显示头像（群聊已读详情）
  showAvatars: {
    type: Boolean,
    default: true
  },
  // 消息ID（用于重试）
  messageId: {
    type: String,
    default: ''
  },
  // 是否显示状态（控制组件显示）
  show: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['retry', 'show-read-info'])

// 是否为群聊
const isGroupChat = computed(() => props.sessionType === 'GROUP')

// 是否全部已读
const isAllRead = computed(() => {
  if (isGroupChat.value) {
    const total = props.totalMembers || props.message.totalMembers || 0
    return total > 0 && props.readCount >= total
  }
  return props.status === 'read'
})

// 未读人数（用于群聊）
const unreadCount = computed(() => {
  if (!isGroupChat.value) { return 0 }
  const total = props.totalMembers || props.message.totalMembers || 0
  const read = props.readCount || props.message.readCount || 0
  return Math.max(0, total - read)
})

// 显示状态的判断
const showStatus = computed(() => {
  // 如果明确设置了show为false，则不显示
  if (!props.show) return false

  // 只有发送方消息才显示状态
  if (!props.message || !props.message.isOwn) return false

  // 撤回的消息不显示状态
  if (props.message.isRevoked || props.message.type?.toUpperCase() === 'RECALLED') return false

  return true
})

// 状态对应的CSS类
const statusClass = computed(() => {
  return `status-${props.status}`
})

// 显示的头像（最多显示3个）
const displayAvatars = computed(() => {
  if (!props.readBy || props.readBy.length === 0) {
    return []
  }
  return props.readBy.slice(0, 3).map(user => ({
    userId: user.userId || user.id,
    userName: user.userName || user.name || user.nickname,
    avatar: user.avatar || user.userAvatar || ''
  }))
})

// 处理重试
const handleRetry = () => {
  if (props.messageId) {
    emit('retry', props.messageId)
  }
}

// 显示已读信息
const handleShowReadInfo = () => {
  emit('show-read-info', {
    messageId: props.message.id || props.messageId,
    readCount: props.readCount || props.message.readCount || 0,
    readBy: props.readBy || props.message.readBy || [],
    isGroupChat: isGroupChat.value
  })
}
</script>

<script>
export default {
  name: 'MessageStatusIndicator'
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-status-indicator {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 16px;
  color: var(--dt-text-quaternary);
  font-size: 14px;

  .status-icon {
    font-size: 16px;
    transition: all 0.2s;

    &.spinning {
      animation: spin 1s linear infinite;
      color: var(--dt-brand-color);
    }

    &.failed {
      color: var(--dt-error-color);
      cursor: pointer;

      &:hover {
        transform: scale(1.1);
      }
    }
  }

  // 已读头像组（钉钉风格）
  .read-avatar-group {
    display: flex;
    align-items: center;
    height: 16px;

    .read-avatar {
      width: 16px;
      height: 16px;
      border-radius: 50%;
      background-size: cover;
      background-position: center;
      border: 1px solid #fff;
      margin-left: -4px;

      &:first-child {
        margin-left: 0;
      }
    }

    .read-more {
      font-size: 10px;
      color: var(--dt-text-tertiary);
      margin-left: 2px;
    }
  }

  // 发送中 - 浅色
  &.status-sending {
    color: var(--dt-brand-color);
  }

  // 已送达 - 灰色
  &.status-delivered {
    color: var(--dt-text-tertiary);
  }

  // 已读 - 蓝色
  &.status-read {
    color: var(--dt-brand-color);
  }

  // 失败 - 红色
  &.status-failed {
    color: var(--dt-error-color);
  }

  // 私聊未读：精致的蓝色小圆点
  .status-unread-dot {
    width: 6px;
    height: 6px;
    background-color: #165DFF;
    border-radius: 50%;
    box-shadow: 0 0 4px rgba(22, 93, 255, 0.3);
  }

  // 已读文本：极致灰调
  .status-text-read {
    font-size: 11px;
    color: #86909C; // 钉钉标准四级文本色
    letter-spacing: -0.2px;
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }

  // 群聊未读：数字圆点
  .status-group-unread {
    min-width: 14px;
    height: 14px;
    padding: 0 3px;
    background-color: #165DFF;
    color: #fff;
    font-size: 9px;
    font-weight: 600;
    border-radius: 7px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;

    &:hover {
      background-color: #1553e6;
    }
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
  .read-avatar {
    border-color: var(--dt-bg-card-dark);
  }

  .status-text-read {
    color: var(--dt-text-quaternary-dark);
  }

  .status-unread-dot {
    box-shadow: 0 0 4px rgba(22, 93, 255, 0.5);
  }

  .status-group-unread {
    &:hover {
      background-color: #1861ff;
    }
  }
}
</style>
