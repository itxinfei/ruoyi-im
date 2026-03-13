<template>
  <el-drawer
    v-model="visible"
    :title="document?.title || '文档预览'"
    direction="rtl"
    size="50%"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-wrapper">
      <el-icon class="is-loading"><loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 文档内容 -->
    <div v-else-if="document" class="document-preview">
      <!-- 文档头部 -->
      <div class="doc-header">
        <div class="doc-info">
          <span class="doc-icon" :class="getIconClass(document.type)">
            <span class="material-icons-outlined">{{ getFileIcon(document.type) }}</span>
          </span>
          <div class="doc-meta">
            <h3 class="doc-title">{{ document.title }}</h3>
            <div class="doc-meta-text">
              <span>{{ document.creatorName || '我' }}</span>
              <span>·</span>
              <span>{{ formatTime(document.updateTime) }}</span>
            </div>
          </div>
        </div>
        <div class="doc-actions">
          <el-button @click="handleEdit" :icon="Edit" circle />
          <el-button @click="handleShare" :icon="Share" circle />
          <el-button @click="handleDownload" :icon="Download" circle />
        </div>
      </div>

      <el-divider />

      <!-- 文档内容区 -->
      <div class="doc-content">
        <div class="doc-text">{{ document.content || '暂无内容' }}</div>
      </div>

      <!-- 评论区域 -->
      <div class="comments-section">
        <div class="comments-header">
          <h4>评论 ({{ comments.length }})</h4>
          <el-button @click="showAddComment = true" :icon="ChatDotRound" circle size="small" />
        </div>

        <!-- 评论列表 -->
        <div class="comments-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-avatar">
              <el-avatar :size="32" :src="comment.avatarUrl">
                {{ comment.creatorName?.charAt(0) }}
              </el-avatar>
            </div>
            <div class="comment-content">
              <div class="comment-header">
                <span class="comment-author">{{ comment.creatorName }}</span>
                <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              </div>
              <div class="comment-text">{{ comment.content }}</div>
            </div>
          </div>
        </div>

        <!-- 添加评论对话框 -->
        <el-dialog
          v-model="showAddComment"
          title="添加评论"
          width="400px"
          :close-on-click-modal="false"
        >
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="4"
            placeholder="输入评论内容..."
          />
          <template #footer>
            <el-button @click="showAddComment = false">取消</el-button>
            <el-button type="primary" @click="handleAddComment" :loading="submitting">
              发送
            </el-button>
          </template>
        </el-dialog>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getDocument, getDocumentComments, addComment } from '@/api/im/document'
import { formatTime } from '@/utils/format'

const props = defineProps({
  documentId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['close'])

const visible = ref(false)
const loading = ref(false)
const document = ref(null)
const comments = ref([])
const showAddComment = ref(false)
const newComment = ref('')
const submitting = ref(false)

// 打开预览
const open = (docId) => {
  visible.value = true
  if (docId) {
    loadDocument(docId)
  }
}

// 加载文档
const loadDocument = async (docId) => {
  loading.value = true
  try {
    const res = await getDocument(docId)
    if (res.code === 200) {
      document.value = res.data
      // 加载评论
      loadComments(docId)
    }
  } catch (error) {
    console.error('加载文档失败:', error)
    ElMessage.error('加载文档失败')
  } finally {
    loading.value = false
  }
}

// 加载评论
const loadComments = async (docId) => {
  try {
    const res = await getDocumentComments(docId)
    if (res.code === 200) {
      comments.value = res.data || []
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

// 添加评论
const handleAddComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  submitting.value = true
  try {
    const res = await addComment({
      documentId: document.value.id,
      content: newComment.value
    })
    if (res.code === 200) {
      ElMessage.success('评论成功')
      newComment.value = ''
      showAddComment.value = false
      // 重新加载评论
      loadComments(document.value.id)
    }
  } catch (error) {
    console.error('添加评论失败:', error)
    ElMessage.error('添加评论失败')
  } finally {
    submitting.value = false
  }
}

// 获取文件图标
const getFileIcon = (type) => {
  const icons = {
    'TEXT': 'description',
    'FILE': 'description',
    'IMAGE': 'image',
    'VIDEO': 'video_library',
    'VOICE': 'mic'
  }
  return icons[type] || 'description'
}

// 获取图标样式
const getIconClass = (type) => {
  const classes = {
    'TEXT': 'icon-doc',
    'FILE': 'icon-doc',
    'IMAGE': 'icon-image',
    'VIDEO': 'icon-video',
    'VOICE': 'icon-voice'
  }
  return classes[type] || 'icon-doc'
}

// 编辑文档
const handleEdit = () => {
  ElMessage.info('文档编辑功能即将推出')
}

// 分享文档
const handleShare = () => {
  ElMessage.info('文档分享功能即将推出，请使用复制链接方式分享')
}

// 下载文档
const handleDownload = () => {
  ElMessage.info('文档下载功能即将推出')
}

// 关闭
const handleClose = () => {
  visible.value = false
  document.value = null
  comments.value = []
  emit('close')
}

// 监听documentId变化
watch(() => props.documentId, (newVal) => {
  if (newVal && visible.value) {
    loadDocument(newVal)
  }
})

defineExpose({
  open
})
</script>

<style scoped lang="scss">
.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #8f959e;
  
  .is-loading {
    font-size: 32px;
    margin-bottom: 16px;
  }
}

.document-preview {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.doc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  
  .doc-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .doc-icon {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      
      &.icon-doc { background: #e6f4ff; color: #1677ff; }
      &.icon-image { background: #f0f5ff; color: #1890ff; }
      &.icon-video { background: #fff0f6; color: #eb2f96; }
      &.icon-voice { background: #fffbe6; color: #fa8c16; }
    }
    
    .doc-meta {
      .doc-title {
        margin: 0 0 4px 0;
        font-size: 16px;
        font-weight: 600;
        color: #1f2329;
      }
      
      .doc-meta-text {
        font-size: 12px;
        color: #8f959e;
      }
    }
  }
  
  .doc-actions {
    display: flex;
    gap: 8px;
  }
}

.doc-content {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
  
  .doc-text {
    line-height: 1.8;
    color: #303133;
    white-space: pre-wrap;
  }
}

.comments-section {
  border-top: 1px solid #f2f3f5;
  padding-top: 16px;
  
  .comments-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
    
    h4 {
      margin: 0;
      font-size: 14px;
      font-weight: 600;
      color: #1f2329;
    }
  }
  
  .comments-list {
    max-height: 300px;
    overflow-y: auto;
    
    .comment-item {
      display: flex;
      gap: 12px;
      margin-bottom: 16px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .comment-content {
        flex: 1;
        
        .comment-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 4px;
          
          .comment-author {
            font-size: 13px;
            font-weight: 500;
            color: #1f2329;
          }
          
          .comment-time {
            font-size: 12px;
            color: #8f959e;
          }
        }
        
        .comment-text {
          font-size: 13px;
          color: #303133;
          line-height: 1.6;
        }
      }
    }
  }
}
</style>