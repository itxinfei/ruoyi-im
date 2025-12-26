<template>
  <div class="app-container">
    <div class="document-preview">
      <!-- 工具栏 -->
      <div class="preview-toolbar">
        <el-button-group>
          <el-button size="mini" icon="el-icon-arrow-left" @click="goBack">返回</el-button>
          <el-button v-if="hasEditPermission" size="mini" icon="el-icon-edit" @click="handleEdit"
            >编辑</el-button
          >
          <el-button size="mini" icon="el-icon-download" @click="handleDownload">下载</el-button>
        </el-button-group>
        <el-button-group style="margin-left: 10px">
          <el-button size="mini" icon="el-icon-share" @click="handleShare">分享</el-button>
          <el-button size="mini" icon="el-icon-chat-dot-round" @click="toggleComments"
            >评论</el-button
          >
        </el-button-group>
      </div>

      <!-- 主体内容 -->
      <div class="preview-content">
        <!-- 目录导航 -->
        <div v-if="showOutline" class="preview-outline">
          <div class="outline-header">
            <span>文档目录</span>
            <el-button type="text" icon="el-icon-close" @click="showOutline = false"></el-button>
          </div>
          <div class="outline-content">
            <el-tree
              :data="outlineData"
              :props="defaultProps"
              highlight-current
              @node-click="handleNodeClick"
            >
            </el-tree>
          </div>
        </div>

        <!-- 预览区域 -->
        <div class="preview-main" :class="{ 'with-comments': showComments }">
          <div ref="preview" class="preview-container"></div>
        </div>

        <!-- 评论区域 -->
        <div v-if="showComments" class="preview-comments">
          <div class="comments-header">
            <span>评论</span>
            <el-button type="text" icon="el-icon-close" @click="toggleComments"></el-button>
          </div>
          <div class="comments-content">
            <div class="comments-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <div class="comment-header">
                  <el-avatar :size="32" :src="comment.avatar"></el-avatar>
                  <div class="comment-info">
                    <div class="comment-user">{{ comment.userName }}</div>
                    <div class="comment-time">{{ parseTime(comment.createTime) }}</div>
                  </div>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div class="comment-actions">
                  <el-button type="text" size="mini" @click="replyComment(comment)">回复</el-button>
                  <el-button
                    v-if="canDeleteComment(comment)"
                    type="text"
                    size="mini"
                    @click="deleteComment(comment)"
                    >删除</el-button
                  >
                </div>
                <!-- 回复列表 -->
                <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
                  <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                    <div class="reply-header">
                      <el-avatar :size="24" :src="reply.avatar"></el-avatar>
                      <div class="reply-info">
                        <div class="reply-user">{{ reply.userName }}</div>
                        <div class="reply-time">{{ parseTime(reply.createTime) }}</div>
                      </div>
                    </div>
                    <div class="reply-content">{{ reply.content }}</div>
                  </div>
                </div>
              </div>
            </div>
            <!-- 评论输入框 -->
            <div class="comments-input">
              <el-input
                v-model="commentContent"
                type="textarea"
                :rows="3"
                placeholder="添加评论..."
              >
              </el-input>
              <el-button type="primary" size="small" @click="submitComment">发表评论</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分享对话框 -->
      <el-dialog v-model:visible="share.open" title="分享文档" width="400px" append-to-body>
        <el-form :model="share.form" label-width="80px">
          <el-form-item label="过期时间">
            <el-date-picker
              v-model="share.form.expireTime"
              type="datetime"
              placeholder="选择过期时间"
            >
            </el-date-picker>
          </el-form-item>
          <el-form-item v-if="share.url" label="分享链接">
            <el-input v-model="share.url" readonly>
              <template #append>
                <el-button icon="el-icon-copy-document" @click="copyShareUrl">复制</el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button type="primary" @click="submitShare">生成链接</el-button>
            <el-button @click="share.open = false">取 消</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { getDocument, shareDocument } from '@/api/im/document'

export default {
  name: 'DocumentPreview',
  data() {
    return {
      // 文档ID
      documentId: undefined,
      // 文档信息
      document: {},
      // 预览器实例
      viewer: null,
      // 是否显示目录
      showOutline: true,
      // 目录数据
      outlineData: [],
      // 目录配置
      defaultProps: {
        children: 'children',
        label: 'label',
      },
      // 是否显示评论
      showComments: false,
      // 评论列表
      comments: [],
      // 评论内容
      commentContent: '',
      // 分享配置
      share: {
        open: false,
        url: '',
        form: {
          expireTime: undefined,
        },
      },
    }
  },
  computed: {
    // 是否有编辑权限
    hasEditPermission() {
      return this.document.permissions && this.document.permissions.edit
    },
  },
  created() {
    this.documentId = this.$route.params.documentId
    this.initDocument()
  },
  methods: {
    /** 初始化文档 */
    initDocument() {
      getDocument(this.documentId).then(response => {
        this.document = response.data
        this.initViewer()
        this.loadComments()
      })
    },
    /** 初始化预览器 */
    initViewer() {
      // 根据文档类型初始化不同的预览器
      switch (this.document.type) {
        case 'pdf':
          this.initPdfViewer()
          break
        case 'docx':
        case 'xlsx':
        case 'pptx':
          this.initOfficeViewer()
          break
        default:
          this.msgError('不支持的文档类型')
      }
    },
    /** 初始化PDF预览器 */
    initPdfViewer() {
      // TODO: 实现PDF预览
    },
    /** 初始化Office预览器 */
    initOfficeViewer() {
      // TODO: 实现Office预览
    },
    /** 返回列表 */
    goBack() {
      this.$router.push('/im/document')
    },
    /** 编辑文档 */
    handleEdit() {
      this.$router.push(`/im/document/edit/${this.documentId}`)
    },
    /** 下载文档 */
    handleDownload() {
      // TODO: 实现文档下载
    },
    /** 分享文档 */
    handleShare() {
      this.share.open = true
      this.share.url = ''
    },
    /** 提交分享 */
    submitShare() {
      shareDocument(this.documentId, this.share.form.expireTime).then(response => {
        this.share.url = response.data
        this.msgSuccess('生成分享链接成功')
      })
    },
    /** 复制分享链接 */
    copyShareUrl() {
      this.$copyText(this.share.url).then(() => {
        this.msgSuccess('复制成功')
      })
    },
    /** 切换评论显示 */
    toggleComments() {
      this.showComments = !this.showComments
    },
    /** 加载评论 */
    loadComments() {
      // TODO: 实现评论加载
    },
    /** 提交评论 */
    submitComment() {
      if (!this.commentContent) {
        this.msgError('请输入评论内容')
        return
      }
      // TODO: 实现评论提交
    },
    /** 回复评论 */
    replyComment() {
      // TODO: 实现评论回复
    },
    /** 删除评论 */
    deleteComment() {
      // TODO: 实现评论删除
    },
    /** 是否可以删除评论 */
    canDeleteComment(comment) {
      return comment.userId === this.$store.state.user.userId
    },
    /** 处理目录节点点击 */
    handleNodeClick() {
      // TODO: 实现目录导航
    },
  },
}
</script>

<style lang="scss" scoped>
.document-preview {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;

  .preview-toolbar {
    padding: 10px;
    border-bottom: 1px solid #dcdfe6;
  }

  .preview-content {
    flex: 1;
    display: flex;
    overflow: hidden;

    .preview-outline {
      width: 250px;
      border-right: 1px solid #dcdfe6;
      display: flex;
      flex-direction: column;

      .outline-header {
        padding: 10px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1px solid #dcdfe6;
      }

      .outline-content {
        flex: 1;
        overflow: auto;
        padding: 10px;
      }
    }

    .preview-main {
      flex: 1;
      overflow: hidden;

      &.with-comments {
        margin-right: 300px;
      }

      .preview-container {
        height: 100%;
      }
    }

    .preview-comments {
      position: absolute;
      right: 0;
      top: 0;
      bottom: 0;
      width: 300px;
      border-left: 1px solid #dcdfe6;
      display: flex;
      flex-direction: column;

      .comments-header {
        padding: 10px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1px solid #dcdfe6;
      }

      .comments-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;

        .comments-list {
          flex: 1;
          overflow: auto;
          padding: 10px;
        }

        .comments-input {
          padding: 10px;
          border-top: 1px solid #dcdfe6;

          .el-button {
            margin-top: 10px;
            float: right;
          }
        }
      }

      .comment-item {
        margin-bottom: 20px;

        .comment-header {
          display: flex;
          align-items: center;
          margin-bottom: 8px;

          .comment-info {
            margin-left: 10px;

            .comment-user {
              font-weight: bold;
            }

            .comment-time {
              font-size: 12px;
              color: #999;
            }
          }
        }

        .comment-content {
          margin-bottom: 8px;
        }

        .comment-actions {
          display: flex;
          justify-content: flex-end;
        }

        .reply-list {
          margin-left: 42px;
          margin-top: 10px;

          .reply-item {
            margin-bottom: 10px;

            .reply-header {
              display: flex;
              align-items: center;
              margin-bottom: 4px;

              .reply-info {
                margin-left: 8px;

                .reply-user {
                  font-weight: bold;
                }

                .reply-time {
                  font-size: 12px;
                  color: #999;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>
