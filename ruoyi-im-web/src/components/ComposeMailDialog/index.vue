<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="700px"
    :close-on-click-modal="false"
    class="compose-mail-dialog"
    @close="handleClose"
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
      <el-form-item
        label="收件人"
        prop="toIds"
      >
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
              <DingtalkAvatar
                :src="user.avatar"
                :name="user.nickname || user.username"
                :size="24"
              />
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
              <DingtalkAvatar
                :src="user.avatar"
                :name="user.nickname || user.username"
                :size="24"
              />
              <span class="user-name">{{ user.nickname || user.username }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- 主题 -->
      <el-form-item
        label="主题"
        prop="subject"
      >
        <el-input
          v-model="form.subject"
          placeholder="请输入邮件主题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <!-- 正文 -->
      <el-form-item
        label="正文"
        prop="content"
      >
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
            <div
              class="upload-area"
              :class="{ 'is-dragging': isDragging }"
            >
              <div class="upload-icon-wrapper">
                <span class="material-icons-outlined upload-icon">cloud_upload</span>
                <div class="icon-glow" />
              </div>
              <div class="upload-text">
                点击或拖拽文件到此处上传
              </div>
              <div class="upload-tip">
                支持 doc、docx、xls、xlsx、pdf、jpg、png 等格式，单个文件不超过 10MB
              </div>
              <div class="upload-types">
                <div
                  v-for="(type, index) in supportedTypes"
                  :key="index"
                  class="type-tag"
                >
                  <span class="material-icons-outlined">{{ type.icon }}</span>
                  <span>{{ type.name }}</span>
                </div>
              </div>
            </div>
          </el-upload>

          <!-- 上传进度 -->
          <div
            v-if="uploadingFiles.length > 0"
            class="upload-progress-list"
          >
            <div
              v-for="file in uploadingFiles"
              :key="file.uid"
              class="upload-progress-item"
              :class="{ 'uploading': file.percentage < 100, 'success': file.percentage === 100, 'error': file.status === 'exception' }"
            >
              <div class="file-header">
                <div class="file-info">
                  <span
                    class="material-icons-outlined file-icon"
                    :class="getFileIconClass(file.name)"
                  >
                    {{ getFileIcon(file.name) }}
                  </span>
                  <span class="file-name">{{ file.name }}</span>
                  <span class="file-size">{{ formatFileSize(file.size) }}</span>
                </div>
                <div class="file-status">
                  <span
                    v-if="file.percentage < 100"
                    class="status-text uploading"
                  >{{ file.percentage }}%</span>
                  <span
                    v-else-if="file.percentage === 100"
                    class="status-text success"
                  >
                    <span class="material-icons-outlined">check_circle</span>
                    完成
                  </span>
                  <span
                    v-else
                    class="status-text error"
                  >
                    <span class="material-icons-outlined">error</span>
                    失败
                  </span>
                </div>
              </div>
              <el-progress
                :percentage="file.percentage"
                :status="file.status"
                :stroke-width="4"
                :show-text="false"
              />
            </div>
          </div>

          <!-- 已上传附件列表 -->
          <div
            v-if="form.attachments.length > 0"
            class="attachment-list"
          >
            <div
              v-for="(attachment, index) in form.attachments"
              :key="index"
              class="attachment-item"
            >
              <div class="attachment-icon-wrapper">
                <span
                  class="material-icons-outlined attachment-icon"
                  :class="getFileIconClass(attachment.name)"
                >
                  {{ getFileIcon(attachment.name) }}
                </span>
              </div>
              <div class="attachment-info">
                <span class="attachment-name">{{ attachment.name }}</span>
                <span class="attachment-size">{{ formatFileSize(attachment.size) }}</span>
              </div>
              <div class="attachment-actions">
                <el-button
                  size="small"
                  :icon="Download"
                  link
                  title="下载"
                  @click="downloadAttachment(attachment)"
                >
                  <span class="material-icons-outlined">download</span>
                </el-button>
                <el-button
                  size="small"
                  :icon="Delete"
                  link
                  type="danger"
                  title="删除"
                  @click="removeAttachment(index)"
                >
                  <span class="material-icons-outlined">delete</span>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          :loading="savingDraft"
          @click="handleSaveDraft"
        >保存草稿</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          发送
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { getToken } from '@/utils/storage'

import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Delete } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { sendMail, saveDraft, searchUsers as searchUsersApi, uploadAttachment } from '@/api/im/mail'
import { formatFileSize } from '@/utils/format'

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
const isDragging = ref(false)

// 支持的文件类型
const supportedTypes = [
  { name: 'Word', icon: 'description', ext: '.doc,.docx' },
  { name: 'Excel', icon: 'table_chart', ext: '.xls,.xlsx' },
  { name: 'PDF', icon: 'picture_as_pdf', ext: '.pdf' },
  { name: '图片', icon: 'image', ext: '.jpg,.jpeg,.png,.gif,.bmp' },
  { name: '压缩包', icon: 'folder_zip', ext: '.zip,.rar' }
]

// 上传地址
const uploadUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  return `${baseUrl}/im/email/attachment/upload`
})

// 上传请求头
const uploadHeaders = computed(() => {
  const token = getToken()
  return {
    'Authorization': token ? `Bearer ${token}` : ''
  }
})

// 接受的文件类型
const acceptTypes = '.doc,.docx,.xls,.xlsx,.ppt,.pptx,.pdf,.jpg,.jpeg,.png,.gif,.bmp,.zip,.rar,.txt,.csv'

// 对话框标题
const dialogTitle = computed(() => {
  if (props.replyTo?.isReply) {return '回复邮件'}
  if (props.replyTo?.isForward) {return '转发邮件'}
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

// 拖拽事件处理
const handleDragEnter = e => {
  e.preventDefault()
  isDragging.value = true
}

const handleDragLeave = e => {
  e.preventDefault()
  isDragging.value = false
}

const handleDragOver = e => {
  e.preventDefault()
}

const handleDrop = e => {
  e.preventDefault()
  isDragging.value = false
}

// 获取文件图标
const getFileIcon = fileName => {
  const ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
  const iconMap = {
    '.doc': 'description',
    '.docx': 'description',
    '.xls': 'table_chart',
    '.xlsx': 'table_chart',
    '.ppt': 'slideshow',
    '.pptx': 'slideshow',
    '.pdf': 'picture_as_pdf',
    '.jpg': 'image',
    '.jpeg': 'image',
    '.png': 'image',
    '.gif': 'image',
    '.bmp': 'image',
    '.zip': 'folder_zip',
    '.rar': 'folder_zip',
    '.txt': 'description',
    '.csv': 'table_chart'
  }
  return iconMap[ext] || 'insert_drive_file'
}

// 获取文件图标样式类
const getFileIconClass = fileName => {
  const ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
  const colorMap = {
    '.doc': 'word-color',
    '.docx': 'word-color',
    '.xls': 'excel-color',
    '.xlsx': 'excel-color',
    '.ppt': 'powerpoint-color',
    '.pptx': 'powerpoint-color',
    '.pdf': 'pdf-color',
    '.jpg': 'image-color',
    '.jpeg': 'image-color',
    '.png': 'image-color',
    '.gif': 'image-color',
    '.bmp': 'image-color',
    '.zip': 'archive-color',
    '.rar': 'archive-color',
    '.txt': 'text-color',
    '.csv': 'excel-color'
  }
  return colorMap[ext] || 'default-color'
}

// 搜索用户
const searchUsers = async query => {
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
const beforeUpload = file => {
  // 文件类型校验
  const fileName = file.name.toLowerCase()
  const allowedExtensions = ['.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.pdf', '.jpg', '.jpeg', '.png', '.gif', '.bmp', '.zip', '.rar', '.txt', '.csv']
  const isAllowedType = allowedExtensions.some(ext => fileName.endsWith(ext))
  if (!isAllowedType) {
    ElMessage.error(`不支持的文件类型！允许的文件类型：${acceptTypes}`)
    return false
  }

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
    // 延迟移除，让用户看到完成状态
    setTimeout(() => {
      uploadingFiles.value.splice(uploadingFileIndex, 1)
    }, 1500)
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
    }, 3000)
  }
  console.error('上传失败', error)
  ElMessage.error(`${file.name} 上传失败`)
}

// 移除上传组件中的文件
const handleRemoveAttachment = file => {
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
const removeAttachment = index => {
  form.attachments.splice(index, 1)
}

// 下载附件
const downloadAttachment = attachment => {
  if (attachment.url) {
    const link = document.createElement('a')
    link.href = attachment.url
    link.download = attachment.name
    link.click()
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    // 提取附件ID列表
    const attachmentIds = form.attachments.map(a => a.id).filter(id => id != null)

    const res = await sendMail({
      toIds: form.toIds,
      ccIds: form.ccIds,
      subject: form.subject,
      content: form.content,
      attachmentIds
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
      const errorMsg = error?.response?.data?.msg || error?.message || error?.response?.data || '发送失败，请稍后重试'
      ElMessage.error(typeof errorMsg === 'string' ? errorMsg : '发送失败，请稍后重试')
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
watch(() => props.modelValue, val => {
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

watch(visible, val => {
  emit('update:modelValue', val)
  if (!val) {
    resetForm()
  }
})
</script>

<style scoped lang="scss">
.compose-mail-dialog {
  :deep(.el-dialog__body) {
    padding: 20px 24px;
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
    padding: 0;
    border: 2px dashed var(--el-border-color);
    border-radius: var(--dt-radius-lg);
    background: transparent;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover {
    .upload-area {
      background: linear-gradient(135deg, rgba(0, 137, 255, 0.05) 0%, rgba(0, 137, 255, 0.02) 100%);
    }
  }
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 32px 24px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: var(--dt-radius-lg);

  &.is-dragging {
    background: linear-gradient(135deg, rgba(0, 137, 255, 0.1) 0%, rgba(0, 137, 255, 0.05) 100%);
    border-color: var(--el-color-primary);
    transform: scale(1.02);

    .upload-icon-wrapper {
      transform: scale(1.1);
    }

    .upload-icon {
      color: var(--el-color-primary);
    }
  }

  .upload-icon-wrapper {
    position: relative;
    margin-bottom: 4px;
  }

  .upload-icon {
    font-size: 48px;
    color: var(--el-text-color-secondary);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    z-index: 1;
  }

  .icon-glow {
    position: absolute;
    inset: -8px;
    background: radial-gradient(circle, rgba(0, 137, 255, 0.2) 0%, transparent 70%);
    border-radius: var(--dt-radius-full);
    animation: iconPulseFade 2s ease-in-out infinite;
  }

  .upload-text {
    font-size: 15px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    margin-bottom: 4px;
  }

  .upload-tip {
    font-size: 13px;
    color: var(--el-text-color-secondary);
    margin-bottom: 12px;
  }

  .upload-types {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    justify-content: center;

    .type-tag {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 4px 10px;
      background: var(--el-fill-color-light);
      border-radius: var(--dt-radius-xl);
      font-size: 12px;
      color: var(--el-text-color-secondary);
      transition: all 0.3s;

      &:hover {
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
        transform: translateY(-2px);
      }

      .material-icons-outlined {
        font-size: 14px;
      }
    }
  }
}

.upload-progress-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.upload-progress-item {
  padding: 12px 16px;
  background: var(--el-fill-color-lighter);
  border-radius: var(--dt-radius-md);
  border: 1px solid var(--el-border-color-lighter);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  &.uploading {
    border-color: var(--el-color-primary-light-5);
    background: linear-gradient(135deg, rgba(0, 137, 255, 0.03) 0%, rgba(0, 137, 255, 0.01) 100%);
  }

  &.success {
    border-color: #52c41a;
    background: linear-gradient(135deg, rgba(82, 196, 26, 0.05) 0%, rgba(82, 196, 26, 0.02) 100%);
  }

  &.error {
    border-color: #f54a45;
    background: linear-gradient(135deg, rgba(245, 74, 69, 0.05) 0%, rgba(245, 74, 69, 0.02) 100%);
  }

  .file-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  .file-info {
    display: flex;
    align-items: center;
    gap: 10px;
    flex: 1;
  }

  .file-icon {
    font-size: 24px;
    color: var(--el-color-primary);

    &.word-color {
      color: #1890ff;
    }

    &.excel-color {
      color: #52c41a;
    }

    &.powerpoint-color {
      color: #faad14;
    }

    &.pdf-color {
      color: #f5222d;
    }

    &.image-color {
      color: #722ed1;
    }

    &.archive-color {
      color: #fa8c16;
    }

    &.text-color {
      color: #8c8c8c;
    }
  }

  .file-name {
    flex: 1;
    font-size: 14px;
    color: var(--el-text-color-primary);
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .file-size {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .file-status {
    .status-text {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      font-weight: 500;

      &.uploading {
        color: var(--el-color-primary);
      }

      &.success {
        color: #52c41a;
      }

      &.error {
        color: #f54a45;
      }

      .material-icons-outlined {
        font-size: 16px;
      }
    }
  }

  :deep(.el-progress-bar__outer) {
    background-color: var(--el-fill-color);
  }

  :deep(.el-progress-bar__inner) {
    transition: all 0.3s ease;
  }
}

.attachment-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--el-fill-color-light);
  border-radius: var(--dt-radius-md);
  border: 1px solid var(--el-border-color-lighter);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    background: linear-gradient(180deg, var(--el-color-primary) 0%, var(--el-color-primary-light-7) 100%);
    opacity: 0;
    transition: opacity 0.3s;
  }

  &:hover {
    background: var(--el-fill-color);
    border-color: var(--el-border-color);
    transform: translateX(2px);
    box-shadow: var(--dt-shadow-3);

    &::before {
      opacity: 1;
    }

    .attachment-actions {
      opacity: 1;
    }
  }

  .attachment-icon-wrapper {
    width: 40px;
    height: 40px;
    border-radius: var(--dt-radius-md);
    background: var(--el-fill-color);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .attachment-icon {
    font-size: 24px;
    color: var(--el-color-primary);

    &.word-color {
      color: #1890ff;
    }

    &.excel-color {
      color: #52c41a;
    }

    &.powerpoint-color {
      color: #faad14;
    }

    &.pdf-color {
      color: #f5222d;
    }

    &.image-color {
      color: #722ed1;
    }

    &.archive-color {
      color: #fa8c16;
    }

    &.text-color {
      color: #8c8c8c;
    }

    &.default-color {
      color: var(--el-color-primary);
    }
  }

  .attachment-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .attachment-name {
    font-size: 14px;
    color: var(--el-text-color-primary);
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .attachment-size {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .attachment-actions {
    display: flex;
    gap: 4px;
    opacity: 0.7;
    transition: opacity 0.3s;

    .el-button {
      :deep(.material-icons-outlined) {
        font-size: 18px;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 暗色模式 */
.dark .attachment-upload {
  :deep(.el-upload-dragger) {
    border-color: rgba(255, 255, 255, 0.1);
    background: transparent;

    &:hover {
      .upload-area {
        background: linear-gradient(135deg, rgba(255, 255, 255, 0.03) 0%, rgba(255, 255, 255, 0.01) 100%);
      }
    }
  }
}

.dark .upload-area {
  .upload-icon {
    color: rgba(255, 255, 255, 0.6);
  }

  .upload-text {
    color: rgba(255, 255, 255, 0.9);
  }

  .upload-tip {
    color: rgba(255, 255, 255, 0.5);
  }

  .type-tag {
    background: rgba(255, 255, 255, 0.05);
    color: rgba(255, 255, 255, 0.6);

    &:hover {
      background: rgba(255, 255, 255, 0.1);
    }
  }
}

.dark .upload-progress-item,
.dark .attachment-item {
  background: rgba(255, 255, 255, 0.03);
  border-color: rgba(255, 255, 255, 0.05);

  &.uploading {
    border-color: rgba(64, 158, 255, 0.3);
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.05) 0%, rgba(64, 158, 255, 0.02) 100%);
  }

  &.success {
    border-color: rgba(82, 196, 26, 0.3);
    background: linear-gradient(135deg, rgba(82, 196, 26, 0.05) 0%, rgba(82, 196, 26, 0.02) 100%);
  }

  &.error {
    border-color: rgba(245, 74, 69, 0.3);
    background: linear-gradient(135deg, rgba(245, 74, 69, 0.05) 0%, rgba(245, 74, 69, 0.02) 100%);
  }

  &:hover {
    background: rgba(255, 255, 255, 0.05);
    border-color: rgba(255, 255, 255, 0.1);
  }
}

.dark .attachment-icon-wrapper {
  background: rgba(255, 255, 255, 0.05);
}

.dark .file-name,
.dark .attachment-name {
  color: rgba(255, 255, 255, 0.9);
}

.dark .file-size,
.dark .attachment-size {
  color: rgba(255, 255, 255, 0.5);
}
</style>