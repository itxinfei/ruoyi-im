<template>
  <el-dialog
    v-model="visible"
    title="消息已读详情"
    width="450px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-loading="loading" class="read-receipt-detail">
      <!-- 消息预览 -->
      <div class="message-preview">
        <div class="preview-header">
          <span class="preview-label">消息内容</span>
        </div>
        <div class="preview-content">{{ messagePreview }}</div>
      </div>

      <!-- 统计信息 -->
      <div class="read-stats">
        <div class="stat-item read">
          <span class="stat-value">{{ detail.readCount || 0 }}</span>
          <span class="stat-label">已读</span>
        </div>
        <div class="stat-item unread">
          <span class="stat-value">{{ detail.unreadCount || 0 }}</span>
          <span class="stat-label">未读</span>
        </div>
        <div class="stat-item total">
          <span class="stat-value">{{ detail.totalCount || 0 }}</span>
          <span class="stat-label">总计</span>
        </div>
      </div>

      <!-- 已读用户列表 -->
      <div v-if="detail.readUsers && detail.readUsers.length > 0" class="user-list-section">
        <div class="section-header">
          <i class="el-icon-view"></i>
          <span>已读用户 ({{ detail.readUsers.length }})</span>
        </div>
        <div class="user-list">
          <div
            v-for="user in detail.readUsers"
            :key="user.userId"
            class="user-item read"
          >
            <el-avatar :src="user.avatar" :size="36">{{ user.userName?.charAt(0) }}</el-avatar>
            <div class="user-info">
              <span class="user-name">{{ user.userName }}</span>
              <span class="read-time">{{ formatReadTime(user.readTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 未读用户列表 -->
      <div v-if="detail.unreadUsers && detail.unreadUsers.length > 0" class="user-list-section">
        <div class="section-header">
          <i class="el-icon-time"></i>
          <span>未读用户 ({{ detail.unreadUsers.length }})</span>
        </div>
        <div class="user-list">
          <div
            v-for="user in detail.unreadUsers"
            :key="user.userId"
            class="user-item unread"
          >
            <el-avatar :src="user.avatar" :size="36">{{ user.userName?.charAt(0) }}</el-avatar>
            <div class="user-info">
              <span class="user-name">{{ user.userName }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button v-if="detail.unreadCount > 0" type="primary" @click="handleRemind">
        提醒未读用户
      </el-button>
    </template>
  </el-dialog>
</template>

<script>
import { getMessageReadDetail } from '@/api/im/message'
import { formatRelativeTime } from '@/utils/format/time'

export default {
  name: 'ReadReceiptDialog',
  props: {
    modelValue: {
      type: Boolean,
      default: false,
    },
    messageId: {
      type: [Number, String],
      default: null,
    },
  },
  emits: ['update:modelValue', 'remind'],
  data() {
    return {
      loading: false,
      detail: {
        messageId: null,
        conversationId: null,
        messagePreview: '',
        sendTime: null,
        totalCount: 0,
        readCount: 0,
        unreadCount: 0,
        readUsers: [],
        unreadUsers: [],
      },
    }
  },
  computed: {
    visible: {
      get() {
        return this.modelValue
      },
      set(val) {
        this.$emit('update:modelValue', val)
      }
    },
    messagePreview() {
      return this.detail.messagePreview || '[消息内容]'
    },
  },
  watch: {
    visible(val) {
      if (val && this.messageId) {
        this.loadReadDetail()
      }
    },
    messageId: {
      immediate: true,
      handler(val) {
        if (val && this.visible) {
          this.loadReadDetail()
        }
      },
    },
  },
  methods: {
    async loadReadDetail() {
      if (!this.messageId) return

      this.loading = true
      try {
        const response = await getMessageReadDetail(this.messageId)
        if (response.code === 200 && response.data) {
          this.detail = {
            ...this.detail,
            ...response.data,
          }
        }
      } catch (error) {
        console.error('[ReadReceiptDialog] 加载已读详情失败:', error)
        this.$message.error('加载已读详情失败')
      } finally {
        this.loading = false
      }
    },
    formatReadTime(time) {
      if (!time) return ''
      return formatRelativeTime(new Date(time))
    },
    handleClose() {
      this.visible = false
      // 重置数据
      this.detail = {
        messageId: null,
        conversationId: null,
        messagePreview: '',
        sendTime: null,
        totalCount: 0,
        readCount: 0,
        unreadCount: 0,
        readUsers: [],
        unreadUsers: [],
      }
    },
    handleRemind() {
      this.$emit('remind', {
        messageId: this.messageId,
        unreadUsers: this.detail.unreadUsers,
      })
      this.$message.success('已提醒未读用户')
      this.handleClose()
    },
  },
}
</script>

<style lang="scss" scoped>
.read-receipt-detail {
  .message-preview {
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 16px;

    .preview-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .preview-label {
        font-size: 12px;
        color: #909399;
      }
    }

    .preview-content {
      font-size: 14px;
      color: #303133;
      line-height: 1.5;
      word-break: break-all;
    }
  }

  .read-stats {
    display: flex;
    justify-content: space-around;
    padding: 16px 0;
    margin-bottom: 16px;
    background: #fafafa;
    border-radius: 8px;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-value {
        font-size: 24px;
        font-weight: 500;
        line-height: 1;
      }

      .stat-label {
        margin-top: 4px;
        font-size: 12px;
        color: #909399;
      }

      &.read .stat-value {
        color: #67c23a;
      }

      &.unread .stat-value {
        color: #909399;
      }

      &.total .stat-value {
        color: #409eff;
      }
    }
  }

  .user-list-section {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }

    .section-header {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 0;
      font-size: 14px;
      font-weight: 500;
      color: #303133;

      i {
        color: #909399;
      }
    }

    .user-list {
      max-height: 200px;
      overflow-y: auto;

      .user-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 8px;
        border-radius: 6px;
        transition: background 0.2s;

        &:hover {
          background: #f5f7fa;
        }

        .user-info {
          display: flex;
          flex-direction: column;
          flex: 1;

          .user-name {
            font-size: 14px;
            color: #303133;
          }

          .read-time {
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }
  }
}
</style>
