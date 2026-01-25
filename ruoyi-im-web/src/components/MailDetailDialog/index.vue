<template>
  <el-dialog
    v-model="visible"
    :title="email?.subject || '邮件详情'"
    width="700px"
    @close="handleClose"
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
          </div>
        </div>
        <div class="mail-actions">
          <el-button :icon="ChatLineSquare" @click="handleReply">回复</el-button>
          <el-button :icon="Share" @click="handleForward">转发</el-button>
          <el-button :icon="Star" :type="email.starred ? 'primary' : ''" @click="toggleStar">
            {{ email.starred ? '已收藏' : '收藏' }}
          </el-button>
          <el-button :icon="Delete" type="danger" @click="handleDelete">删除</el-button>
        </div>
      </div>

      <!-- 邮件内容 -->
      <div class="mail-body">
        <div v-if="email.preview" class="mail-content">
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
              <span class="material-icons-outlined file-icon">description</span>
              <span class="file-name">{{ file.name }}</span>
              <span class="file-size">{{ formatFileSize(file.size) }}</span>
              <el-button size="small" link @click="downloadAttachment(file)">
                <span class="material-icons-outlined">download</span>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ChatLineSquare, Share, Star, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  email: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'reply', 'forward', 'delete'])

const visible = ref(false)

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

const toggleStar = () => {
  if (props.email) {
    props.email.starred = !props.email.starred
    ElMessage.success(props.email.starred ? '已收藏' : '已取消收藏')
  }
}

const handleDelete = () => {
  emit('delete', props.email)
}

const downloadAttachment = (file) => {
  ElMessage.info(`下载附件: ${file.name}`)
}

const formatFileSize = (bytes) => {
  if (!bytes) return ''
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(1)} ${units[unitIndex]}`
}
</script>

<style scoped>
.mail-detail {
  padding: 10px 0;
}

.mail-header {
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.sender-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.sender-detail {
  flex: 1;
}

.sender-name {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.send-time {
  font-size: 12px;
  color: #8c8c8c;
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
  color: #262626;
  white-space: pre-wrap;
}

.attachments {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 12px;
}

.section-title .material-icons-outlined {
  font-size: 18px;
  color: #8c8c8c;
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
  background: #f9f9f9;
  border-radius: 8px;
  transition: background 0.2s;
}

.attachment-item:hover {
  background: #f0f0f0;
}

.file-icon {
  font-size: 24px;
  color: #1677ff;
}

.file-name {
  flex: 1;
  font-size: 14px;
  color: #262626;
}

.file-size {
  font-size: 12px;
  color: #8c8c8c;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

/* 暗色模式 */
:deep(.dark) .sender-name,
:deep(.dark) .section-title {
  color: #f1f5f9;
}

:deep(.dark) .mail-content,
:deep(.dark) .file-name {
  color: #e2e8f0;
}

:deep(.dark) .attachment-item,
:deep(.dark) .mail-body {
  background: rgba(30, 41, 59, 0.5);
}
</style>
