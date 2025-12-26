<template>
  <div class="comment-list">
    <!-- 评论列表和原有内容保持不变 -->
    <!-- ... -->

    <!-- 添加评论通知组件 -->
    <comment-notification ref="notification"></comment-notification>
  </div>
</template>

<script>
import { addComment, replyComment, deleteComment, deleteReply } from '@/api/im/comment'
import CommentSocket from '@/utils/socket/commentSocket'
import CommentNotification from './CommentNotification'

export default {
  name: 'CommentList',
  components: {
    CommentNotification,
  },
  props: {
    documentId: {
      type: [String, Number],
      required: true,
    },
  },
  data() {
    return {
      loading: false,
      comments: [],
      commentContent: '',
      replyContent: '',
      activeReply: null,
      page: {
        current: 1,
        size: 10,
        total: 0,
      },
      socket: null,
    }
  },
  created() {
    this.loadComments()
    this.initWebSocket()
  },
  beforeUnmount() {
    this.closeWebSocket()
  },
  methods: {
    /** 初始化WebSocket */
    initWebSocket() {
      this.socket = new CommentSocket(this.documentId)

      // 注册事件处理器
      this.socket.on('commentAdded', this.handleCommentAdded)
      this.socket.on('commentDeleted', this.handleCommentDeleted)
      this.socket.on('replyAdded', this.handleReplyAdded)
      this.socket.on('replyDeleted', this.handleReplyDeleted)
      this.socket.on('commentUpdated', this.handleCommentUpdated)

      // 连接WebSocket
      this.socket.connect()
    },

    /** 关闭WebSocket */
    closeWebSocket() {
      if (this.socket) {
        this.socket.disconnect()
        this.socket = null
      }
    },

    /** 处理新增评论 */
    handleCommentAdded(data) {
      // 如果是当前页的评论，直接添加到列表
      if (this.page.current === 1) {
        this.comments.unshift(data)
        if (this.comments.length > this.page.size) {
          this.comments.pop()
        }
      }

      // 更新总数
      this.page.total++

      // 显示通知
      this.$refs.notification.addNotification({
        type: 'comment_added',
        userName: data.userName,
        avatar: data.avatar,
      })
    },

    /** 处理删除评论 */
    handleCommentDeleted(data) {
      const index = this.comments.findIndex(c => c.id === data.commentId)
      if (index !== -1) {
        this.comments.splice(index, 1)
        this.page.total--
      }

      // 显示通知
      this.$refs.notification.addNotification({
        type: 'comment_deleted',
        userName: data.userName,
        avatar: data.avatar,
      })
    },

    /** 处理新增回复 */
    handleReplyAdded(data) {
      const comment = this.comments.find(c => c.id === data.commentId)
      if (comment) {
        if (!comment.replies) {
          comment.replies = []
        }
        comment.replies.push(data)

        // 显示通知
        this.$refs.notification.addNotification({
          type: 'reply_added',
          userName: data.userName,
          avatar: data.avatar,
        })
      }
    },

    /** 处理删除回复 */
    handleReplyDeleted(data) {
      const comment = this.comments.find(c => c.id === data.commentId)
      if (comment && comment.replies) {
        const index = comment.replies.findIndex(r => r.id === data.replyId)
        if (index !== -1) {
          comment.replies.splice(index, 1)

          // 显示通知
          this.$refs.notification.addNotification({
            type: 'reply_deleted',
            userName: data.userName,
            avatar: data.avatar,
          })
        }
      }
    },

    /** 处理评论更新 */
    handleCommentUpdated(data) {
      const comment = this.comments.find(c => c.id === data.id)
      if (comment) {
        Object.assign(comment, data)
      }
    },

    /** 提交评论 */
    async submitComment() {
      if (!this.commentContent) {
        this.$message.warning('请输入评论内容')
        return
      }

      try {
        const response = await addComment(this.documentId, {
          content: this.commentContent,
        })

        // 发送WebSocket消息
        this.socket.send('comment_added', response.data)

        this.commentContent = ''
        this.$message.success('评论成功')
      } catch (error) {
        this.$message.error('评论失败：' + error.message)
      }
    },

    /** 提交回复 */
    async submitReply(comment) {
      if (!this.replyContent) {
        this.$message.warning('请输入回复内容')
        return
      }

      try {
        const response = await replyComment(this.documentId, comment.id, {
          content: this.replyContent,
        })

        // 发送WebSocket消息
        this.socket.send('reply_added', {
          commentId: comment.id,
          ...response.data,
        })

        this.cancelReply()
        this.$message.success('回复成功')
      } catch (error) {
        this.$message.error('回复失败：' + error.message)
      }
    },

    /** 删除评论 */
    async handleDelete(comment) {
      try {
        await this.$confirm('确认删除该评论吗？', '提示', {
          type: 'warning',
        })

        await deleteComment(this.documentId, comment.id)

        // 发送WebSocket消息
        this.socket.send('comment_deleted', {
          commentId: comment.id,
          userName: this.$store.state.user.name,
          avatar: this.$store.state.user.avatar,
        })

        this.$message.success('删除成功')
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败：' + error.message)
        }
      }
    },

    /** 删除回复 */
    async handleDeleteReply(comment, reply) {
      try {
        await this.$confirm('确认删除该回复吗？', '提示', {
          type: 'warning',
        })

        await deleteReply(this.documentId, comment.id, reply.id)

        // 发送WebSocket消息
        this.socket.send('reply_deleted', {
          commentId: comment.id,
          replyId: reply.id,
          userName: this.$store.state.user.name,
          avatar: this.$store.state.user.avatar,
        })

        this.$message.success('删除成功')
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败：' + error.message)
        }
      }
    },

    // 其他原有方法保持不变...
  },
}
</script>

<style lang="scss" scoped>
// 原有样式保持不变...
</style>
