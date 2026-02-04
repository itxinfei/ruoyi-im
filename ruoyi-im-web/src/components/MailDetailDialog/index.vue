<template>
  <el-dialog
    v-model="visible"
    :title="email?.subject || '邮件详情'"
    width="700px"
    @close="handleClose"
    class="mail-detail-dialog"
  >
    <div v-if="email" class="mail-detail">
      <!-- 邮件头部 -->
      <div class="mail-header">
        <div class="sender-info">
          <DingtalkAvatar
            :src="email.senderAvatar"
            :name="email.sender"
            :size="48"
            shape="circle"
          />
          <div class="sender-detail">
            <div class="sender-name">{{ email.sender }}</div>
            <div class="send-time">{{ email.time }}</div>
            <div v-if="email.cc && email.cc.length > 0" class="cc-info">
              抄送: {{ email.cc.join(', ') }}
            </div>
          </div>
        </div>
        <div class="mail-actions">
          <el-button :icon="ChatLineSquare" @click="handleReply">回复</el-button>
          <el-button :icon="Share" @click="handleForward">转发</el-button>
          <el-button
            :icon="email.starred ? StarFilled : Star"
            :type="email.starred ? 'primary' : ''"
            @click="toggleStar"
            :loading="starring"
          >
            {{ email.starred ? '已收藏' : '收藏' }}
          </el-button>
          <el-button :icon="Delete" type="danger" @click="handleDelete">删除</el-button>
        </div>
      </div>

      <!-- 邮件内容 -->
      <div class="mail-body">
        <div v-if="email.preview || email.fullContent" class="mail-content">
          {{ email.fullContent || email.preview }}
        </div>

        <!-- 附件列表 -->
        <div v-if="email.attachments && email.attachments.length > 0" class="attachments">
          <div class="section-title">
            <span class="material-icons-outlined">attach_file</span>
            附件 ({{ email.attachments.length }})
          </div>
          <div class="attachment-list">
            <div
              v-for="(file, index) in email.attachments"
              :key="index"
              class="attachment-item"
            >
              <span class="material-icons-outlined file-icon" :style="{ color: getFileIconColor(file.name) }">
                {{ getFileIcon(file.name) }}
              </span>
              <span class="file-name">{{ file.name }}</span>
              <span class="file-size">{{ formatFileSize(file.size) }}</span>
              <el-button
                size="small"
                link
                type="primary"
                @click="downloadAttachment(file)"
                :loading="downloadingFiles.has(file.id)"
              >
                <span class="material-icons-outlined download-icon">download</span>
                下载
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button :icon="ArrowLeft" @click="handlePrev" :disabled="!hasPrev">上一封</el-button>
        <el-button :icon="ArrowRight" @click="handleNext" :disabled="!hasNext">下一封</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ChatLineSquare, Share, StarFilled, Delete, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { starMail, downloadAttachment as downloadAttachmentApi } from '@/api/im/mail'
import { formatFileSize } from '@/utils/format'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  email: {
    type: Object,
    default: null
  },
  hasPrev: {
    type: Boolean,
    default: false
  },
  hasNext: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'reply', 'forward', 'delete', 'star', 'prev', 'next'])

const visible = ref(false)
const starring = ref(false)
const downloadingFiles = ref(new Set())

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleClose = () => {
  visible.value = false
}

const handleReply = () => {
  emit('reply', props.email)
}

const handleForward = () => {
  emit('forward', props.email)
}

const toggleStar = async () => {
  if (!props.email) return

  starring.value = true
  try {
    const newStarred = !props.email.starred
    const res = await starMail(props.email.id, newStarred)
    if (res.code === 200) {
      props.email.starred = newStarred
      ElMessage.success(newStarred ? '已收藏' : '已取消收藏')
      emit('star', { id: props.email.id, starred: newStarred })
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('星标操作失败', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    starring.value = false
  }
}

const handleDelete = () => {
  emit('delete', props.email)
}

const handlePrev = () => {
  emit('prev')
}

const handleNext = () => {
  emit('next')
}

const downloadAttachment = async (file) => {
  if (downloadingFiles.value.has(file.id)) return

  downloadingFiles.value.add(file.id)
  try {
    const res = await downloadAttachmentApi(file.id)
    if (res) {
      // 创建 Blob 并下载
      const blob = new Blob([res])
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = file.name
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      ElMessage.success('下载成功')
    }
  } catch (error) {
    console.error('下载附件失败', error)
    ElMessage.error('下载失败，请稍后重试')
  } finally {
    downloadingFiles.value.delete(file.id)
  }
}

const getFileIcon = (fileName) => {
  const ext = fileName?.split('.').pop()?.toLowerCase() || ''
  const iconMap = {
    // 图片
    jpg: 'image', jpeg: 'image', png: 'image', gif: 'image', bmp: 'image', svg: 'image', webp: 'image',
    // 文档
    doc: 'description', docx: 'description',
    xls: 'table_chart', xlsx: 'table_chart',
    ppt: 'slideshow', pptx: 'slideshow',
    pdf: 'picture_as_pdf',
    txt: 'text_snippet',
    // 压缩
    zip: 'archive', rar: 'archive', '7z': 'archive', tar: 'archive', gz: 'archive',
    // 音视频
    mp3: 'audio_file', wav: 'audio_file', flac: 'audio_file',
    mp4: 'video_file', avi: 'video_file', mkv: 'video_file', mov: 'video_file',
    // 代码
    js: 'javascript', ts: 'typescript', html: 'code', css: 'code', json: 'code',
    java: 'code', py: 'code', cpp: 'code', c: 'code'
  }
  return iconMap[ext] || 'insert_drive_file'
}

const getFileIconColor = (fileName) => {
  const ext = fileName?.split('.').pop()?.toLowerCase() || ''
  const colorMap = {
    // 图片 - 蓝紫色
    jpg: '#722ed1', jpeg: '#722ed1', png: '#722ed1', gif: '#722ed1', bmp: '#722ed1', svg: '#722ed1',
    // 文档 - 蓝色
    doc: '#0089FF', docx: '#0089FF',
    xls: '#52c41a', xlsx: '#52c41a',
    ppt: '#fa8c16', pptx: '#fa8c16',
    pdf: '#f5222d',
    txt: '#8c8c8c',
    // 压缩 - 橙色
    zip: '#fa8c16', rar: '#fa8c16', '7z': '#fa8c16', tar: '#fa8c16',
    // 音视频 - 红色
    mp3: '#f5222d', wav: '#f5222d',
    mp4: '#f5222d', avi: '#f5222d', mkv: '#f5222d',
    // 代码 - 青色
    js: '#13c2c2', ts: '#13c2c2', html: '#f5222d', css: '#1890ff', json: '#faad14'
  }
  return colorMap[ext] || '#8c8c8c'
}
</script>

<style scoped lang="scss">
.mail-detail-dialog {
  :deep(.el-dialog__body) {
    padding: 16px 24px;
    max-height: 60vh;
    overflow-y: auto;
  }
}

.mail-detail {
  padding: 10px 0;
}

.mail-header {
  padding-bottom: 16px;
  border-bottom: 1px solid var(--dt-border-color);
  margin-bottom: 20px;
}

.sender-info {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.sender-detail {
  flex: 1;
}

.sender-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin-bottom: 4px;
}

.send-time {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin-bottom: 4px;
}

.cc-info {
  font-size: 12px;
  color: var(--dt-text-secondary);
}

.mail-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.mail-body {
  min-height: 200px;
}

.mail-content {
  font-size: 14px;
  line-height: 1.8;
  color: var(--dt-text-primary);
  white-space: pre-wrap;
  word-break: break-word;
}

.attachments {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--dt-border-color);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: 12px;
}

.section-title .material-icons-outlined {
  font-size: 18px;
  color: var(--dt-text-tertiary);
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-active);
  }
}

.file-icon {
  font-size: 24px;
}

.file-name {
  flex: 1;
  font-size: 14px;
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin-right: 8px;
}

.download-icon {
  font-size: 16px;
  margin-right: 2px;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

/* 暗色模式 */
.dark .mail-header,
.dark .attachments {
  border-color: var(--dt-border-dark);
}

.dark .sender-name,
.dark .section-title,
.dark .file-name {
  color: var(--dt-text-primary-dark);
}

.dark .attachment-item {
  background: rgba(30, 41, 59, 0.5);

  &:hover {
    background: rgba(51, 65, 85, 0.5);
  }
}

.dark .mail-content {
  color: var(--dt-text-secondary-dark);
}

.dark .send-time,
.dark .cc-info,
.dark .file-size {
  color: var(--dt-text-tertiary-dark);
}

/* 响应式设计 */
@media (max-width: 767px) {
  .mail-detail-dialog {
    width: 95% !important;

    :deep(.el-dialog__body) {
      padding: 12px 16px;
    }
  }

  .mail-actions {
    :deep(.el-button) {
      span {
        display: none;
      }

      .el-icon {
        margin: 0;
      }
    }
  }

  .sender-info {
    flex-direction: column;
    gap: 8px;
  }

  .attachment-item {
    padding: 10px;
  }
}
</style>
