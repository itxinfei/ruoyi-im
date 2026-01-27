<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
    class="compose-mail-dialog"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="70px"
      label-position="left"
      class="compose-form"
    >
      <!-- 收件人 -->
      <el-form-item label="收件人" prop="toIds">
        <el-select
          v-model="form.toIds"
          filterable
          remote
          reserve-keyword
          placeholder="搜索并选择收件人"
          :remote-method="searchUsers"
          :loading="userSearchLoading"
          multiple
          collapse-tags
          collapse-tags-tooltip
          class="full-width"
        >
          <el-option
            v-for="user in userOptions"
            :key="user.id"
            :label="user.nickname || user.username"
            :value="user.id"
          >
            <div class="user-option">
              <DingtalkAvatar :src="user.avatar" :name="user.nickname || user.username" :size="24" />
              <span class="user-name">{{ user.nickname || user.username }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- 抄送 -->
      <el-form-item label="抄送">
        <el-select
          v-model="form.ccIds"
          filterable
          remote
          reserve-keyword
          placeholder="搜索抄送人"
          :remote-method="searchUsers"
          :loading="userSearchLoading"
          multiple
          collapse-tags
          collapse-tags-tooltip
          class="full-width"
        >
          <el-option
            v-for="user in userOptions"
            :key="user.id"
            :label="user.nickname || user.username"
            :value="user.id"
          >
            <div class="user-option">
              <DingtalkAvatar :src="user.avatar" :name="user.nickname || user.username" :size="24" />
              <span class="user-name">{{ user.nickname || user.username }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- 主题 -->
      <el-form-item label="主题" prop="subject">
        <el-input
          v-model="form.subject"
          placeholder="请输入邮件主题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <!-- 正文 -->
      <el-form-item label="正文" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          placeholder="请输入邮件内容"
          :rows="8"
          maxlength="5000"
          show-word-limit
        />
      </el-form-item>

      <!-- 附件 -->
      <el-form-item label="附件">
        <div class="attachment-section">
          <el-upload
            ref="uploadRef"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-progress="handleUploadProgress"
            :before-upload="beforeUpload"
            :on-remove="handleRemoveAttachment"
            :file-list="uploadFileList"
            :auto-upload="true"
            :multiple="true"
            :limit="5"
            :on-exceed="handleExceed"
            :accept="acceptTypes"
            class="attachment-upload"
            drag
          >
            <div class="upload-area">
              <span class="material-icons-outlined upload-icon">cloud_upload</span>
              <div class="upload-text">点击或拖拽文件到此处上传</div>
              <div class="upload-tip">支持 doc、docx、xls、xlsx、pdf、jpg、png 等格式，单个文件不超过 10MB</div>
            </div>
          </el-upload>

          <!-- 上传进度 -->
          <div v-if="uploadingFiles.length > 0" class="upload-progress-list">
            <div
              v-for="file in uploadingFiles"
              :key="file.uid"
              class="upload-progress-item"
            >
              <div class="file-info">
                <span class="material-icons-outlined file-icon">description</span>
                <span class="file-name">{{ file.name }}</span>
                <span class="file-size">{{ formatFileSize(file.size) }}</span>
              </div>
              <el-progress
                :percentage="file.percentage"
                :status="file.status"
                :stroke-width="4"
              />
            </div>
          </div>

          <!-- 已上传附件列表 -->
          <div v-if="form.attachments.length > 0" class="attachment-list">
            <div
              v-for="(attachment, index) in form.attachments"
              :key="index"
              class="attachment-item"
            >
              <span class="material-icons-outlined attachment-icon">insert_drive_file</span>
              <span class="attachment-name">{{ attachment.name }}</span>
              <span class="attachment-size">{{ formatFileSize(attachment.size) }}</span>
              <el-button
                size="small"
                :icon="Delete"
                link
                type="danger"
                @click="removeAttachment(index)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button @click="handleSaveDraft" :loading="savingDraft">保存草稿</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          发送
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { sendMail, saveDraft, searchUsers as searchUsersApi, uploadAttachment } from '@/api/im/mail'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  replyTo: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const formRef = ref(null)
const uploadRef = ref(null)
const submitting = ref(false)
const savingDraft = ref(false)
const uploadFileList = ref([])
const uploadingFiles = ref([])
const userOptions = ref([])
const userSearchLoading = ref(false)

// 上传地址
const uploadUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  return `${baseUrl}/im/email/attachment/upload`
})

// 上传请求头
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('access_token')
  return {
    'Authorization': token ? `Bearer ${token}` : ''
  }
})

// 接受的文件类型
const acceptTypes = '.doc,.docx,.xls,.xlsx,.ppt,.pptx,.pdf,.jpg,.jpeg,.png,.gif,.bmp,.zip,.rar,.txt,.csv'

// 对话框标题
const dialogTitle = computed(() => {
  if (props.replyTo?.isReply) return '回复邮件'
  if (props.replyTo?.isForward) return '转发邮件'
  return '写邮件'
})

// 表单数据
const form = reactive({
  toIds: [],
  ccIds: [],
  bccIds: [],
  subject: '',
  content: '',
  attachments: []
})

// 表单校验规则
const rules = {
  toIds: [
    { required: true, message: '请选择收件人', trigger: 'change', type: 'array', min: 1 }
  ],
  subject: [
    { required: true, message: '请输入邮件主题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入邮件内容', trigger: 'blur' }
  ]
}

// 搜索用户
const searchUsers = async (query) => {
  if (!query || query.trim().length === 0) {
    userOptions.value = []
    return
  }
  userSearchLoading.value = true
  try {
    const res = await searchUsersApi(query.trim())
    if (res.code === 200) {
      userOptions.value = res.data || []
    }
  } catch (error) {
    console.error('搜索用户失败', error)
  } finally {
    userSearchLoading.value = false
  }
}

// 上传前校验
const beforeUpload = (file) => {
  // 文件大小校验
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }

  // 添加到上传中列表
  uploadingFiles.value.push({
    uid: file.uid,
    name: file.name,
    size: file.size,
    percentage: 0,
    status: ''
  })

  return true
}

// 上传进度
const handleUploadProgress = (event, file) => {
  const uploadingFile = uploadingFiles.value.find(f => f.uid === file.uid)
  if (uploadingFile) {
    uploadingFile.percentage = Math.floor(event.percent)
  }
}

// 上传成功
const handleUploadSuccess = (response, file) => {
  const uploadingFileIndex = uploadingFiles.value.findIndex(f => f.uid === file.uid)
  if (uploadingFileIndex > -1) {
    uploadingFiles.value.splice(uploadingFileIndex, 1)
  }

  if (response.code === 200) {
    form.attachments.push({
      id: response.data.id,
      name: response.data.fileName || file.name,
      size: response.data.fileSize || file.size,
      url: response.data.url
    })
    ElMessage.success(`${file.name} 上传成功`)
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 上传失败
const handleUploadError = (error, file) => {
  const uploadingFileIndex = uploadingFiles.value.findIndex(f => f.uid === file.uid)
  if (uploadingFileIndex > -1) {
    uploadingFiles.value[uploadingFileIndex].status = 'exception'
    setTimeout(() => {
      uploadingFiles.value.splice(uploadingFileIndex, 1)
    }, 2000)
  }
  console.error('上传失败', error)
  ElMessage.error(`${file.name} 上传失败`)
}

// 移除上传组件中的文件
const handleRemoveAttachment = (file) => {
  const index = form.attachments.findIndex(a => a.id === file.response?.data?.id)
  if (index > -1) {
    form.attachments.splice(index, 1)
  }
}

// 超出限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传 5 个附件')
}

// 移除附件
const removeAttachment = (index) => {
  form.attachments.splice(index, 1)
}

// 格式化文件大小
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

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    const res = await sendMail({
      toIds: form.toIds,
      ccIds: form.ccIds,
      subject: form.subject,
      content: form.content,
      attachments: form.attachments
    })

    if (res.code === 200) {
      ElMessage.success('邮件发送成功')
      emit('success', res.data)
      handleClose()
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch (error) {
    if (error !== false) {
      console.error('发送邮件失败', error)
      ElMessage.error('发送失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

// 保存草稿
const handleSaveDraft = async () => {
  try {
    savingDraft.value = true
    const res = await saveDraft({
      toIds: form.toIds,
      ccIds: form.ccIds,
      subject: form.subject || '(无主题)',
      content: form.content,
      attachments: form.attachments
    })
    if (res.code === 200) {
      ElMessage.success('草稿已保存')
      handleClose()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存草稿失败', error)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    savingDraft.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  if (form.attachments.length > 0 || form.subject || form.content) {
    ElMessageBox.confirm('关闭将丢失未保存的内容，确定要关闭吗？', '提示', {
      type: 'warning'
    }).then(() => {
      visible.value = false
    }).catch(() => {})
  } else {
    visible.value = false
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  form.toIds = []
  form.ccIds = []
  form.bccIds = []
  form.subject = ''
  form.content = ''
  form.attachments = []
  uploadFileList.value = []
  uploadingFiles.value = []
  userOptions.value = []
}

// 监听 modelValue 变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.replyTo) {
    // 回复/转发模式：填充表单
    if (props.replyTo.isReply) {
      form.toIds = props.replyTo.toIds || [props.replyTo.senderId]
      form.subject = props.replyTo.subject || ''
      form.content = '\n\n---------- 原始邮件 ----------\n'
    } else if (props.replyTo.isForward) {
      form.subject = props.replyTo.subject || ''
      form.content = props.replyTo.content || ''
      // 转发时需要包含原附件
      if (props.replyTo.attachments) {
        form.attachments = [...props.replyTo.attachments]
      }
    }
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    resetForm()
  }
})
</script>

<style scoped lang="scss">
.compose-mail-dialog {
  :deep(.el-dialog__body) {
    padding: 16px 24px;
  }
}

.compose-form {
  .full-width {
    width: 100%;
  }

  .user-option {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.attachment-section {
  width: 100%;
}

.attachment-upload {
  width: 100%;

  :deep(.el-upload) {
    width: 100%;
  }

  :deep(.el-upload-dragger) {
    width: 100%;
    padding: 20px;
    border: 1px dashed var(--el-border-color);
    border-radius: var(--el-border-radius-base);
    background: var(--el-fill-color-light);
  }
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-icon {
  font-size: 32px;
  color: var(--el-color-primary);
}

.upload-text {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.upload-progress-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-progress-item {
  padding: 8px;
  background: var(--el-fill-color-light);
  border-radius: 4px;

  .file-info {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;
  }

  .file-icon {
    font-size: 20px;
    color: var(--el-color-primary);
  }

  .file-name {
    flex: 1;
    font-size: 13px;
    color: var(--el-text-color-primary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .file-size {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.attachment-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  transition: background var(--el-transition-duration);

  &:hover {
    background: var(--el-fill-color);
  }
}

.attachment-icon {
  font-size: 20px;
  color: var(--el-color-primary);
}

.attachment-name {
  flex: 1;
  font-size: 13px;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-size {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-right: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 暗色模式 */
.dark .attachment-upload {
  :deep(.el-upload-dragger) {
    border-color: var(--dt-border-dark);
    background: rgba(30, 41, 59, 0.5);
  }
}

.dark .upload-progress-item,
.dark .attachment-item {
  background: rgba(30, 41, 59, 0.5);

  &:hover {
    background: rgba(51, 65, 85, 0.5);
  }
}
</style>
