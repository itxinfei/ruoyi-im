<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="visible = $event"
    title="已读详情"
    width="400px"
    :close-on-click-modal="true"
  >
    <div class="read-info-container">
      <!-- 单聊已读信息 -->
      <div v-if="!isGroupChat" class="single-chat-info">
        <div class="info-row">
          <span class="info-label">已读时间:</span>
          <span class="info-value">{{ formatReadTime }}</span>
        </div>
      </div>

      <!-- 群聊已读信息 -->
      <div v-else class="group-chat-info">
        <div class="info-row">
          <span class="info-label">已读人数:</span>
          <span class="info-value">{{ readCount }}&nbsp;/&nbsp;{{ totalCount }}</span>
        </div>

        <el-divider />

        <div class="members-section">
          <div class="section-title">已读成员</div>
          <div class="members-grid">
            <div
              v-for="member in readBy"
              :key="member.userId"
              class="member-item"
              :title="member.nickname || member.userName"
            >
              <DingtalkAvatar
                :src="member.avatar"
                :size="40"
                :username="member.nickname || member.userName"
              />
              <div class="member-name">{{ member.nickname || member.userName }}</div>
              <div class="read-time">{{ formatRelativeTime(member.readTime) }}</div>
            </div>
          </div>

          <div v-if="unreadMembers.length > 0" class="unread-section">
            <div class="section-title">未读成员</div>
            <div class="members-grid">
              <div
                v-for="member in unreadMembers"
                :key="member.userId"
                class="member-item member-unread"
                :title="member.nickname || member.userName"
              >
                <DingtalkAvatar
                  :src="member.avatar"
                  :size="40"
                  :username="member.nickname || member.userName"
                />
                <div class="member-name">{{ member.nickname || member.userName }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <!-- 未读成员提醒按钮（仅群聊且有未读成员时显示） -->
        <el-button
          v-if="isGroupChat && unreadMembers.length > 0"
          type="primary"
          @click="handleRemindUnread"
        >
          <span class="material-icons-outlined">notifications_active</span>
          <span>提醒未读 ({{ unreadMembers.length }})</span>
        </el-button>

        <el-button @click="visible = false">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { formatRelativeTime } from '@/utils/message'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

/**
 * 已读详情弹窗
 * 显示单聊/群聊的消息已读详情
 */

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  messageId: [String, Number],
  readCount: {
    type: Number,
    default: 0
  },
  readBy: {
    type: Array,
    default: () => []
  },
  readTime: [String, Date],
  isGroupChat: {
    type: Boolean,
    default: false
  },
  totalMembers: {
    type: Number,
    default: 0
  },
  allMembers: {
    type: Array,
    default: () => []
  },
  conversationId: [String, Number]
})

const emit = defineEmits(['remind-unread'])

// 格式化已读时间
const formatReadTime = computed(() => {
  if (!props.readTime) return '-'
  return new Date(props.readTime).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
})

/**
 * 一键提醒未读成员
 * 发送 @ 提醒给所有未读成员
 */
const handleRemindUnread = () => {
  if (!props.conversationId) {
    ElMessage.warning('缺少会话信息')
    return
  }

  if (unreadMembers.value.length === 0) {
    ElMessage.info('暂无未读成员')
    return
  }

  // 触发提醒事件，传递未读成员列表
  emit('remind-unread', {
    conversationId: props.conversationId,
    messageId: props.messageId,
    unreadMembers: unreadMembers.value
  })

  ElMessage.success(`已提醒 ${unreadMembers.value.length} 位未读成员`)
}

// 未读成员
const unreadMembers = computed(() => {
  if (!props.isGroupChat || !props.allMembers.length) return []

  const readUserIds = new Set(props.readBy.map(m => m.userId))
  return props.allMembers.filter(m => !readUserIds.has(m.userId))
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.read-info-container {
  padding: 8px 0;
}

.single-chat-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}

.info-label {
  font-size: 14px;
  color: var(--dt-text-secondary);
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
}

.group-chat-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.members-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin-bottom: 8px;
}

.members-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
  padding: 4px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: var(--dt-radius-sm);
  }
}

.member-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 8px;
  border-radius: var(--dt-radius-md);
  transition: background var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.member-unread {
    opacity: 0.6;
  }
}

.member-name {
  font-size: 12px;
  color: var(--dt-text-primary);
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
}

.read-time {
  font-size: 10px;
  color: var(--dt-text-tertiary);
  text-align: center;
}

.unread-section {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--dt-border-light);
}

// Dialog Footer
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  align-items: center;

  .el-button {
    display: flex;
    align-items: center;
    gap: 6px;

    .material-icons-outlined {
      font-size: 16px;
    }
  }
}

// 暗色模式
.dark {
  .single-chat-info,
  .group-chat-info {
    .info-label {
      color: var(--dt-text-secondary-dark);
    }

    .info-value {
      color: var(--dt-text-primary-dark);
    }
  }

  .section-title {
    color: var(--dt-text-primary-dark);
  }

  .member-item {
    &:hover {
      background: var(--dt-bg-hover-dark);
    }
  }

  .member-name {
    color: var(--dt-text-primary-dark);
  }

  .read-time {
    color: var(--dt-text-tertiary-dark);
  }

  .unread-section {
    border-top-color: var(--dt-border-dark);
  }
}
</style>
