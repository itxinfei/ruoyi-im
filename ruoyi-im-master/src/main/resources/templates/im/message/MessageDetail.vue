<template>
  <div class="message-detail-container">
    <div v-loading="loading" class="detail-content">
      <!-- 消息基本信息 -->
      <el-descriptions :column="2" border>
        <el-descriptions-item label="消息ID">
          {{ message.id }}
        </el-descriptions-item>
        
        <el-descriptions-item label="会话ID">
          <el-link type="primary" @click="viewConversation(message.conversationId)">
            {{ message.conversationId }}
          </el-link>
        </el-descriptions-item>
        
        <el-descriptions-item label="发送者ID">
          <user-tag :user-id="message.senderId" />
        </el-descriptions-item>
        
        <el-descriptions-item label="消息类型">
          <el-tag :type="getMessageTypeColor(message.messageType)">
            {{ getMessageTypeLabel(message.messageType) }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="敏感级别">
          <el-tag
            v-if="message.sensitiveLevel > 0"
            :type="getSensitiveLevelColor(message.sensitiveLevel)"
          >
            {{ getSensitiveLevelLabel(message.sensitiveLevel) }}
          </el-tag>
          <span v-else class="text-muted">正常</span>
        </el-descriptions-item>
        
        <el-descriptions-item label="消息状态">
          <div class="message-status">
            <el-tag v-if="message.isRevoked" type="info" size="small">已撤回</el-tag>
            <el-tag v-if="message.isEdited" type="warning" size="small">已编辑</el-tag>
            <el-tag v-if="message.isDeleted" type="danger" size="small">已删除</el-tag>
            <span v-if="!hasStatus" class="text-muted">正常</span>
          </div>
        </el-descriptions-item>
        
        <el-descriptions-item label="发送时间">
          {{ formatTime(message.createTime) }}
        </el-descriptions-item>
        
        <el-descriptions-item label="更新时间">
          {{ formatTime(message.updateTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 消息内容 -->
      <div class="message-content-section">
        <h4>消息内容</h4>
        <div class="content-wrapper">
          <!-- 文本消息 -->
          <div v-if="message.messageType === 'TEXT'" class="text-content">
            <div class="content-box" :class="{ 'sensitive': message.sensitiveLevel > 0 }">
              {{ getMessageText(message.content) }}
            </div>
          </div>

          <!-- 图片消息 -->
          <div v-else-if="message.messageType === 'IMAGE'" class="image-content">
            <div class="image-preview">
              <el-image
                :src="getImageUrl(message.content)"
                :preview-src-list="[getImageUrl(message.content)]"
                fit="cover"
                class="message-image"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>图片加载失败</span>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="image-info">
              <p><strong>文件名:</strong> {{ getFileName(message.content) }}</p>
              <p><strong>文件大小:</strong> {{ getFileSize(message.content) }}</p>
              <p><strong>图片尺寸:</strong> {{ getImageDimensions(message.content) }}</p>
            </div>
          </div>

          <!-- 文件消息 -->
          <div v-else-if="message.messageType === 'FILE'" class="file-content">
            <div class="file-info">
              <el-icon class="file-icon"><Document /></el-icon>
              <div class="file-details">
                <h5>{{ getFileName(message.content) }}</h5>
                <p class="file-meta">
                  <span>{{ getFileSize(message.content) }}</span>
                  <span>{{ getFileType(message.content) }}</span>
                </p>
              </div>
              <el-button type="primary" size="small" @click="downloadFile">
                下载
              </el-button>
            </div>
          </div>

          <!-- 语音消息 -->
          <div v-else-if="message.messageType === 'VOICE'" class="voice-content">
            <div class="voice-player">
              <el-button
                :type="isPlaying ? 'danger' : 'primary'"
                :icon="isPlaying ? VideoPlay : VideoPause"
                circle
                @click="togglePlay"
              />
              <div class="voice-info">
                <span>语音时长: {{ getVoiceDuration(message.content) }}</span>
                <div class="progress-bar">
                  <div class="progress" :style="{ width: playProgress + '%' }"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 视频消息 -->
          <div v-else-if="message.messageType === 'VIDEO'" class="video-content">
            <div class="video-player">
              <video
                ref="videoPlayer"
                :src="getVideoUrl(message.content)"
                controls
                class="message-video"
              >
                您的浏览器不支持视频播放
              </video>
              <div class="video-info">
                <p><strong>文件名:</strong> {{ getFileName(message.content) }}</p>
                <p><strong>文件大小:</strong> {{ getFileSize(message.content) }}</p>
                <p><strong>视频时长:</strong> {{ getVideoDuration(message.content) }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 关联信息 -->
      <div v-if="hasRelatedInfo" class="related-info-section">
        <h4>关联信息</h4>
        
        <!-- 回复消息 -->
        <div v-if="message.replyToMessageId" class="related-item">
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="回复消息ID">
              <el-link type="primary" @click="viewRelatedMessage(message.replyToMessageId)">
                {{ message.replyToMessageId }}
              </el-link>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 转发消息 -->
        <div v-if="message.forwardFromMessageId" class="related-item">
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="转发来源消息ID">
              <el-link type="primary" @click="viewRelatedMessage(message.forwardFromMessageId)">
                {{ message.forwardFromMessageId }}
              </el-link>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <!-- 操作记录 -->
      <div class="operation-log-section">
        <h4>操作记录</h4>
        <el-timeline>
          <el-timeline-item
            v-for="log in operationLogs"
            :key="log.id"
            :timestamp="formatTime(log.createTime)"
            :type="getLogType(log.operation)"
          >
            <div class="log-content">
              <strong>{{ log.operatorName }}</strong>
              {{ getOperationDescription(log.operation) }}
              <span v-if="log.remark" class="log-remark">（{{ log.remark }}）</span>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button @click="$emit('close')">关闭</el-button>
      
      <el-button
        v-if="canEdit"
        type="primary"
        @click="handleEdit"
      >
        编辑
      </el-button>
      
      <el-button
        v-if="canRevoke"
        type="warning"
        @click="handleRevoke"
      >
        撤回
      </el-button>
      
      <el-button
        v-if="message.sensitiveLevel === 0"
        type="info"
        @click="handleMarkSensitive(true)"
      >
        标记敏感
      </el-button>
      
      <el-button
        v-else
        type="info"
        @click="handleMarkSensitive(false)"
      >
        取消敏感标记
      </el-button>
      
      <el-button
        v-if="canDelete"
        type="danger"
        @click="handleDelete"
      >
        删除
      </el-button>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑消息"
      width="500px"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="消息内容" prop="content">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入消息内容"
          />
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="editForm.remark"
            placeholder="请输入编辑备注（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit" :loading="editLoading">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Picture,
  Document,
  VideoPlay,
  VideoPause
} from '@element-plus/icons-vue'
import UserTag from '@/components/UserTag.vue'
import {
  getMessageDetail,
  updateMessage,
  revokeMessage,
  markMessageSensitive,
  deleteMessage,
  getOperationLogs
} from '@/api/im/message'
import { formatTime } from '@/utils/format'

// Props & Emits
const props = defineProps({
  messageId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['close', 'refresh'])

// 响应式数据
const loading = ref(false)
const editLoading = ref(false)
const editDialogVisible = ref(false)

const message = ref({})
const operationLogs = ref([])

// 音频播放相关
const isPlaying = ref(false)
const playProgress = ref(0)
const videoPlayer = ref(null)

// 编辑表单
const editForm = reactive({
  content: '',
  remark: ''
})

const editFormRef = ref()
const editRules = {
  content: [
    { required: true, message: '消息内容不能为空', trigger: 'blur' },
    { min: 1, max: 2000, message: '消息内容长度不能超过2000字符', trigger: 'blur' }
  ]
}

// 计算属性
const hasStatus = computed(() => {
  return message.value.isRevoked || message.value.isEdited || message.value.isDeleted
})

const hasRelatedInfo = computed(() => {
  return message.value.replyToMessageId || message.value.forwardFromMessageId
})

const canEdit = computed(() => {
  return !message.value.isDeleted && !message.value.isRevoked && message.value.messageType === 'TEXT'
})

const canRevoke = computed(() => {
  return !message.value.isDeleted && !message.value.isRevoked
})

const canDelete = computed(() => {
  return !message.value.isDeleted
})

// 方法定义
const loadMessageDetail = async () => {
  loading.value = true
  try {
    const response = await getMessageDetail(props.messageId)
    if (response.code === 200) {
      message.value = response.data || {}
      
      // 加载操作记录
      await loadOperationLogs()
    } else {
      ElMessage.error(response.msg || '获取消息详情失败')
    }
  } catch (error) {
    console.error('获取消息详情失败:', error)
    ElMessage.error('获取消息详情失败')
  } finally {
    loading.value = false
  }
}

const loadOperationLogs = async () => {
  try {
    const response = await getOperationLogs(props.messageId)
    if (response.code === 200) {
      operationLogs.value = response.data || []
    }
  } catch (error) {
    console.error('获取操作记录失败:', error)
  }
}

const viewConversation = (conversationId) => {
  // 跳转到会话详情
  emit('viewConversation', conversationId)
}

const viewRelatedMessage = (messageId) => {
  // 查看关联消息
  emit('viewMessage', messageId)
}

const handleEdit = () => {
  editForm.content = getMessageText(message.value.content)
  editForm.remark = ''
  editDialogVisible.value = true
  nextTick(() => {
    editFormRef.value?.clearValidate()
  })
}

const confirmEdit = async () => {
  try {
    await editFormRef.value.validate()
    
    editLoading.value = true
    const response = await updateMessage(props.messageId, {
      content: editForm.content,
      remark: editForm.remark
    })
    
    if (response.code === 200) {
      ElMessage.success('消息编辑成功')
      editDialogVisible.value = false
      emit('refresh')
      await loadMessageDetail()
    } else {
      ElMessage.error(response.msg || '消息编辑失败')
    }
  } catch (error) {
    if (error !== false) {
      console.error('编辑消息失败:', error)
      ElMessage.error('编辑消息失败')
    }
  } finally {
    editLoading.value = false
  }
}

const handleRevoke = async () => {
  try {
    await ElMessageBox.confirm('确定要撤回此消息吗？撤回后不可恢复！', '确认撤回', {
      type: 'warning'
    })

    const response = await revokeMessage(props.messageId)
    if (response.code === 200) {
      ElMessage.success('消息撤回成功')
      emit('refresh')
      await loadMessageDetail()
    } else {
      ElMessage.error(response.msg || '消息撤回失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回消息失败:', error)
      ElMessage.error('撤回消息失败')
    }
  }
}

const handleMarkSensitive = async (mark) => {
  try {
    const action = mark ? '标记为敏感' : '取消敏感标记'
    await ElMessageBox.confirm(`确定要${action}此消息吗？`, `确认${action}`, {
      type: 'warning'
    })

    const response = await markMessageSensitive(props.messageId, mark ? 1 : 0)
    if (response.code === 200) {
      ElMessage.success(`${action}成功`)
      emit('refresh')
      await loadMessageDetail()
    } else {
      ElMessage.error(response.msg || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${mark ? '标记' : '取消标记'}敏感失败:`, error)
      ElMessage.error(`${mark ? '标记' : '取消标记'}敏感失败`)
    }
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除此消息吗？删除后不可恢复！', '确认删除', {
      type: 'warning'
    })

    const response = await deleteMessage(props.messageId)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      emit('refresh')
      await loadMessageDetail()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除消息失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const downloadFile = () => {
  // 下载文件
  const fileInfo = JSON.parse(message.value.content)
  window.open(fileInfo.url, '_blank')
}

const togglePlay = () => {
  // 音频播放控制
  isPlaying.value = !isPlaying.value
  // 实际播放逻辑需要根据具体需求实现
}

// 工具方法
const getMessageText = (content) => {
  try {
    const parsed = JSON.parse(content)
    return parsed.text || ''
  } catch {
    return content
  }
}

const getImageUrl = (content) => {
  try {
    const parsed = JSON.parse(content)
    return parsed.url || ''
  } catch {
    return ''
  }
}

const getVideoUrl = (content) => {
  try {
    const parsed = JSON.parse(content)
    return parsed.url || ''
  } catch {
    return ''
  }
}

const getFileName = (content) => {
  try {
    const parsed = JSON.parse(content)
    return parsed.fileName || parsed.originalName || '未知文件'
  } catch {
    return '未知文件'
  }
}

const getFileSize = (content) => {
  try {
    const parsed = JSON.parse(content)
    const size = parsed.fileSize || parsed.size || 0
    return formatFileSize(size)
  } catch {
    return '0 B'
  }
}

const getFileType = (content) => {
  try {
    const parsed = JSON.parse(content)
    return parsed.fileType || parsed.mimeType || '未知类型'
  } catch {
    return '未知类型'
  }
}

const getImageDimensions = (content) => {
  try {
    const parsed = JSON.parse(content)
    return parsed.dimensions ? `${parsed.dimensions.width}x${parsed.dimensions.height}` : '未知'
  } catch {
    return '未知'
  }
}

const getVoiceDuration = (content) => {
  try {
    const parsed = JSON.parse(content)
    return parsed.duration ? `${parsed.duration}秒` : '未知'
  } catch {
    return '未知'
  }
}

const getVideoDuration = (content) => {
  try {
    const parsed = JSON.parse(content)
    return parsed.duration ? `${parsed.duration}秒` : '未知'
  } catch {
    return '未知'
  }
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getMessageTypeColor = (type) => {
  const colorMap = {
    TEXT: '',
    IMAGE: 'success',
    FILE: 'info',
    VOICE: 'warning',
    VIDEO: 'danger'
  }
  return colorMap[type] || ''
}

const getMessageTypeLabel = (type) => {
  const labelMap = {
    TEXT: '文本',
    IMAGE: '图片',
    FILE: '文件',
    VOICE: '语音',
    VIDEO: '视频'
  }
  return labelMap[type] || type
}

const getSensitiveLevelColor = (level) => {
  const colorMap = {
    1: 'warning',
    2: 'danger'
  }
  return colorMap[level] || ''
}

const getSensitiveLevelLabel = (level) => {
  const labelMap = {
    1: '一般',
    2: '严重'
  }
  return labelMap[level] || ''
}

const getLogType = (operation) => {
  const typeMap = {
    'CREATE': 'primary',
    'EDIT': 'warning',
    'REVOKE': 'info',
    'DELETE': 'danger',
    'MARK_SENSITIVE': 'warning',
    'UNMARK_SENSITIVE': 'success'
  }
  return typeMap[operation] || 'primary'
}

const getOperationDescription = (operation) => {
  const descMap = {
    'CREATE': '创建了消息',
    'EDIT': '编辑了消息',
    'REVOKE': '撤回了消息',
    'DELETE': '删除了消息',
    'MARK_SENSITIVE': '标记为敏感消息',
    'UNMARK_SENSITIVE': '取消了敏感标记'
  }
  return descMap[operation] || operation
}

// 生命周期
onMounted(() => {
  loadMessageDetail()
})
</script>

<style scoped>
.message-detail-container {
  min-height: 400px;
}

.detail-content {
  margin-bottom: 20px;
}

.message-content-section,
.related-info-section,
.operation-log-section {
  margin-top: 20px;
}

.message-content-section h4,
.related-info-section h4,
.operation-log-section h4 {
  margin-bottom: 10px;
  color: #303133;
  font-weight: 500;
}

.content-wrapper {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 16px;
  background-color: #fafafa;
}

.text-content .content-box {
  padding: 12px;
  border-radius: 4px;
  background-color: #fff;
  border-left: 4px solid #409eff;
  line-height: 1.6;
}

.text-content .content-box.sensitive {
  border-left-color: #e6a23c;
  background-color: #fdf6ec;
  color: #e6a23c;
}

.image-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.image-preview {
  flex-shrink: 0;
}

.message-image {
  width: 200px;
  height: 150px;
  border-radius: 4px;
}

.image-info {
  flex: 1;
}

.image-info p {
  margin: 5px 0;
  color: #606266;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 150px;
  color: #909399;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.image-error .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.file-content .file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #fff;
}

.file-icon {
  font-size: 32px;
  color: #909399;
}

.file-details {
  flex: 1;
}

.file-details h5 {
  margin: 0 0 5px 0;
  color: #303133;
}

.file-meta {
  margin: 0;
  color: #909399;
  font-size: 12px;
}

.file-meta span {
  margin-right: 12px;
}

.voice-content .voice-player {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #fff;
}

.voice-info {
  flex: 1;
}

.voice-info span {
  display: block;
  margin-bottom: 8px;
  color: #606266;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background-color: #e4e7ed;
  border-radius: 2px;
  overflow: hidden;
}

.progress {
  height: 100%;
  background-color: #409eff;
  transition: width 0.3s;
}

.video-content .video-player {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-video {
  width: 100%;
  max-width: 400px;
  border-radius: 4px;
}

.video-info p {
  margin: 5px 0;
  color: #606266;
}

.related-item {
  margin-bottom: 16px;
}

.log-content {
  line-height: 1.6;
}

.log-remark {
  color: #909399;
  font-style: italic;
}

.action-buttons {
  text-align: right;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.action-buttons .el-button {
  margin-left: 8px;
}

.text-muted {
  color: #909399;
  font-size: 12px;
}

.message-status {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
</style>