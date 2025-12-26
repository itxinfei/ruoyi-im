<template>
  <div class="comment-notification">
    <transition-group name="notification-list" tag="div">
      <div
        v-for="notification in notifications"
        :key="notification.id"
        class="notification-item"
        :class="notification.type"
      >
        <div class="notification-content">
          <el-avatar :size="24" :src="notification.avatar" class="notification-avatar"></el-avatar>
          <div class="notification-message">
            <span class="user-name">{{ notification.userName }}</span>
            <span class="action">
              {{ getActionText(notification.type) }}
            </span>
          </div>
        </div>
        <div class="notification-time">
          {{ parseTime(notification.time) }}
        </div>
      </div>
    </transition-group>
  </div>
</template>

<script>
export default {
  name: 'CommentNotification',
  props: {
    documentId: {
      type: [String, Number],
      required: true,
    },
  },
  data() {
    return {
      notifications: [],
    }
  },
  methods: {
    /**
     * 添加通知
     */
    addNotification(notification) {
      this.notifications.unshift({
        id: Date.now(),
        ...notification,
        time: new Date(),
      })

      // 最多显示5条通知
      if (this.notifications.length > 5) {
        this.notifications.pop()
      }

      // 3秒后自动移除
      setTimeout(() => {
        const index = this.notifications.findIndex(n => n.id === notification.id)
        if (index !== -1) {
          this.notifications.splice(index, 1)
        }
      }, 3000)
    },

    /**
     * 获取操作文本
     */
    getActionText(type) {
      switch (type) {
        case 'comment_added':
          return '发表了评论'
        case 'comment_deleted':
          return '删除了评论'
        case 'reply_added':
          return '回复了评论'
        case 'reply_deleted':
          return '删除了回复'
        default:
          return ''
      }
    },
  },
}
</script>

<style lang="scss" scoped>
.comment-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
  width: 300px;

  .notification-item {
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    padding: 10px;
    margin-bottom: 10px;
    display: flex;
    flex-direction: column;

    .notification-content {
      display: flex;
      align-items: center;
      margin-bottom: 4px;

      .notification-avatar {
        margin-right: 8px;
      }

      .notification-message {
        .user-name {
          font-weight: bold;
          margin-right: 4px;
        }
      }
    }

    .notification-time {
      font-size: 12px;
      color: #999;
    }

    &.comment_added {
      border-left: 4px solid #67c23a;
    }

    &.comment_deleted {
      border-left: 4px solid #f56c6c;
    }

    &.reply_added {
      border-left: 4px solid #409eff;
    }

    &.reply_deleted {
      border-left: 4px solid #e6a23c;
    }
  }
}

.notification-list-enter-active,
.notification-list-leave-active {
  transition: all 0.3s;
}

.notification-list-enter {
  opacity: 0;
  transform: translateX(30px);
}

.notification-list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
