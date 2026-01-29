<template>
  <div class="virtual-message-wrapper" :class="{ 'is-time-divider': msg.isTimeDivider }">
    <!-- 时间分隔符 -->
    <div v-if="msg.isTimeDivider" class="time-divider">
      <span class="time-text">{{ msg.timeText }}</span>
    </div>

    <!-- 消息项组件 -->
    <MessageItem
      v-else
      :message="msg"
      :multi-select-mode="multiSelectMode"
      @reply="$emit('reply', $event)"
      @reaction="handleReaction"
      @command="$emit('command', $event, msg)"
      @scroll-to="$emit('scroll-to', $event)"
      @at="$emit('at', $event)"
      @show-user="$emit('show-user', $event)"
      @retry="$emit('retry', $event)"
      @nudge="$emit('nudge', $event)"
    >
      <!-- 消息气泡内容插槽 -->
      <template #bubble>
        <MessageBubble
          :message="msg"
          :session-type="sessionType"
          @command="$emit('command', $event, msg)"
          @at="$emit('at', msg)"
          @preview="$emit('preview', $event)"
          @download="$emit('download', $event)"
          @retry="$emit('retry', $event)"
          @add-reaction="$emit('add-reaction', $event, msg)"
          @re-edit="$emit('re-edit', $event)"
        />
      </template>

      <!-- 已读状态插槽 -->
      <template #read-status>
        <!-- 群聊：显示已读人数，可悬停查看详情 -->
        <el-popover
          v-if="sessionType === 'GROUP' && (msg.readCount > 0 || msg.isRead)"
          placement="top"
          :width="220"
          trigger="hover"
          popper-class="read-receipt-popover"
          @before-enter="$emit('fetch-read-users', msg)"
        >
          <template #reference>
            <span class="read-count clickable">
              {{ msg.readCount > 0 ? `${msg.readCount}人已读` : '已读' }}
            </span>
          </template>
          <div v-loading="loadingReadUsers[msg.id]" class="read-users-list">
            <div class="read-users-header">
              <span class="read-title">已读成员</span>
              <span v-if="readUsersMap[msg.id]" class="read-count-badge">{{ readUsersMap[msg.id].length }}</span>
            </div>
            <div class="read-users-body">
              <div v-for="user in readUsersMap[msg.id]" :key="user.id" class="read-user-item">
                <DingtalkAvatar :src="user.avatar" :name="user.name" :user-id="user.id" :size="32" shape="square" />
                <span class="user-name">{{ user.name }}</span>
              </div>
              <div v-if="!loadingReadUsers[msg.id] && (!readUsersMap[msg.id] || readUsersMap[msg.id].length === 0)" class="empty-state">
                <el-icon><User /></el-icon>
                <span>暂无已读成员</span>
              </div>
            </div>
            <!-- 显示未读人数 -->
            <div v-if="unreadCount(msg)" class="unread-users-footer">
              <span>未读 {{ unreadCount(msg) }} 人</span>
            </div>
          </div>
        </el-popover>
        <!-- 单聊：只显示已读/未读 -->
        <span v-else-if="sessionType === 'PRIVATE'" class="read-status-simple" :class="{ read: msg.isRead || msg.readCount > 0, unread: !msg.isRead && msg.readCount === 0 }">
          {{ msg.isRead || msg.readCount > 0 ? '已读' : '未读' }}
        </span>
        <!-- 无已读数据时显示未读 -->
        <span v-else class="unread">未读</span>
      </template>
    </MessageItem>
  </div>
</template>

<script setup>
import { User } from '@element-plus/icons-vue'
import MessageItem from './MessageItemRefactored.vue'
import MessageBubble from './MessageBubbleRefactored.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  msg: {
    type: Object,
    required: true
  },
  sessionType: {
    type: String,
    default: 'PRIVATE'
  },
  multiSelectMode: {
    type: Boolean,
    default: false
  },
  readUsersMap: {
    type: Object,
    default: () => ({})
  },
  loadingReadUsers: {
    type: Object,
    default: () => ({})
  },
  groupMemberCount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits([
  'reply', 'reaction', 'command', 'scroll-to', 'at', 'show-user',
  'retry', 'nudge', 'preview', 'download', 'add-reaction', 're-edit',
  'fetch-read-users'
])

const handleReaction = (msg, reaction) => {
  emit('reaction', msg, reaction)
}

const unreadCount = (msg) => {
  if (!props.groupMemberCount) return null
  const readCount = msg.readCount || 0
  return Math.max(0, props.groupMemberCount - readCount)
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.virtual-message-wrapper {
  width: 100%;
  box-sizing: border-box;

  &.is-time-divider {
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &:not(.is-time-divider) {
    padding: 4px 0;
  }
}

.time-divider {
  text-align: center;
  margin: 20px 0;
  color: var(--dt-text-tertiary);
  font-size: 12px;
  line-height: 1;

  .time-text {
    background: var(--dt-bg-body);
    padding: 4px 12px;
    border-radius: var(--dt-radius-full);
    display: inline-block;
  }
}

.read-status {
  font-size: 11px;
  cursor: default;
  display: flex;
  align-items: center;
  gap: 4px;

  .read { color: var(--dt-text-quaternary); }
  .unread { color: var(--dt-brand-color); }
  .read-count {
    color: var(--dt-brand-color);
    cursor: pointer;
    transition: all var(--dt-transition-fast);
    display: inline-flex;
    align-items: center;
    gap: 2px;
    padding: 2px 6px;
    border-radius: 10px;
    background: var(--dt-brand-bg);

    &:hover {
      background: var(--dt-brand-hover);
      text-decoration: none;
    }

    &::before {
      content: 'visibility';
      font-family: 'Material Icons Outlined';
      font-size: 12px;
    }
  }
}

.read-status-simple {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 11px;
  transition: all var(--dt-transition-fast);

  &.read {
    color: var(--dt-text-quaternary);
  }

  &.unread {
    color: var(--dt-brand-color);
  }
}

.unread {
  color: var(--dt-brand-color);
  font-size: 11px;
}

.read-users-list {
  .read-users-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--dt-border-lighter);
    margin-bottom: 12px;
  }

  .read-title {
    font-size: 13px;
    font-weight: 500;
    color: var(--dt-text-primary);
  }

  .read-count-badge {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
    font-size: 11px;
    padding: 2px 8px;
    border-radius: var(--dt-radius-full);
  }

  .read-users-body {
    max-height: 200px;
    overflow-y: auto;
  }

  .read-user-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 0;

    &:hover {
      background: var(--dt-bg-hover);
      margin: 0 -8px;
      padding-left: 8px;
      padding-right: 8px;
      border-radius: var(--dt-radius-sm);
    }
  }

  .user-name {
    font-size: 13px;
    color: var(--dt-text-primary);
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24px 0;
    color: var(--dt-text-quaternary);
    font-size: 12px;
    gap: 8px;
  }

  .unread-users-footer {
    padding-top: 12px;
    border-top: 1px solid var(--dt-border-lighter);
    margin-top: 12px;
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }
}
</style>
