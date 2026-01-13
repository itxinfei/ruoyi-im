<template>
  <div v-if="showReceipt" class="read-receipt-badge" :class="{ clickable: canClick }" @click="handleClick">
    <!-- 已读图标 -->
    <i class="el-icon-view read-icon"></i>

    <!-- 已读状态文本 -->
    <span class="read-text">
      <template v-if="isAllRead">全部已读</template>
      <template v-else-if="readCount > 0">{{ readCount }}人已读</template>
      <template v-else>未读</template>
    </span>

    <!-- 最近已读用户头像（悬停显示更多） -->
    <div v-if="recentReaders.length > 0" class="recent-readers">
      <el-avatar-group :max="3" size="small">
        <el-avatar
          v-for="reader in recentReaders"
          :key="reader.userId"
          :src="reader.avatar"
          :title="reader.userName"
        />
      </el-avatar-group>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ReadReceiptBadge',
  props: {
    // 消息ID
    messageId: {
      type: [Number, String],
      required: true,
    },
    // 会话ID
    conversationId: {
      type: [Number, String],
      required: true,
    },
    // 总人数
    totalCount: {
      type: Number,
      default: 2,
    },
    // 已读人数
    readCount: {
      type: Number,
      default: 0,
    },
    // 已读用户列表
    readUsers: {
      type: Array,
      default: () => [],
    },
    // 是否可点击查看详情
    canClick: {
      type: Boolean,
      default: true,
    },
    // 是否已读
    isRead: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    showReceipt() {
      // 只显示群聊或需要确认的消息的已读状态
      return this.totalCount > 2 || this.readCount > 0
    },
    isAllRead() {
      return this.readCount >= this.totalCount && this.totalCount > 0
    },
    recentReaders() {
      // 返回最近已读的3个用户
      return this.readUsers.slice(0, 3).map(user => ({
        userId: user.userId || user.id,
        userName: user.userName || user.nickname || user.name,
        avatar: user.avatar || user.headImage,
      }))
    },
  },
  methods: {
    handleClick() {
      if (!this.canClick) return
      this.$emit('click', {
        messageId: this.messageId,
        conversationId: this.conversationId,
      })
    },
  },
}
</script>

<style lang="scss" scoped>
.read-receipt-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  margin-top: 4px;
  font-size: 11px;
  color: #999;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 10px;
  transition: all 0.2s;

  &.clickable {
    cursor: pointer;

    &:hover {
      background: rgba(0, 0, 0, 0.06);
      color: #666;
    }
  }

  .read-icon {
    font-size: 12px;
  }

  .read-text {
    white-space: nowrap;
  }

  .recent-readers {
    display: flex;
    align-items: center;

    :deep(.el-avatar-group) {
      .el-avatar {
        border: 1px solid #fff;
        margin-left: -8px;

        &:first-child {
          margin-left: 0;
        }
      }
    }
  }
}

// 全部已读状态
.read-receipt-badge .read-icon {
  color: #67c23a;
}

// 部分已读状态
.read-receipt-badge:not(:has(.read-text[data-all-read])) .read-icon {
  color: #909399;
}
</style>
