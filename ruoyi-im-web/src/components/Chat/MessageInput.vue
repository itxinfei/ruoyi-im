<template>
  <div
    class="message-input"
    :class="{ focused: isFocused, 'drag-over': isDragOver }"
    @dragover.prevent="handleDragOver"
    @dragleave.prevent="handleDragLeave"
    @drop.prevent="handleDrop"
  >
    <!-- 拖拽上传遮罩 -->
    <div v-if="isDragOver" class="drag-overlay">
      <div class="drag-hint">
        <el-icon class="drag-icon">
          <Upload />
        </el-icon>
        <span>释放文件到此处上传</span>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <button class="toolbar-btn" @click="insertEmoji('😀')">
        <el-icon><ChatDotRound /></el-icon>
        <span class="btn-label">表情</span>
      </button>
      <button
        class="toolbar-btn"
        :disabled="!isGroupSession"
        :title="isGroupSession ? '@提及' : '只能在群聊中@成员'"
        @click="handleAtMemberClick"
      >
        <el-icon><UserFilled /></el-icon>
        <span class="btn-label">@</span>
      </button>
      <button class="toolbar-btn" @click="triggerUpload('image')">
        <el-icon><Picture /></el-icon>
        <span class="btn-label">图片</span>
      </button>
      <button class="toolbar-btn" @click="triggerUpload('file')">
        <el-icon><Files /></el-icon>
        <span class="btn-label">文件</span>
      </button>
      <button class="toolbar-btn" @click="triggerUpload('batch')">
        <el-icon><FolderOpened /></el-icon>
        <span class="btn-label">批量</span>
      </button>
    </div>

    <!-- 输入区 -->
    <div class="text-area">
      <div v-if="editingMessage" class="edit-bar">
        <div class="edit-info">
          <span class="edit-label">编辑消息</span>
          <span class="edit-text">{{ getReplyPreview(editingMessage) }}</span>
        </div>
        <button class="edit-close" @click="cancelEdit">
          ×
        </button>
      </div>
      <div v-else-if="replyingMessage" class="reply-bar">
        <div class="reply-info">
          <span class="reply-label">回复</span>
          <span class="reply-name">{{ replyingMessage.senderName || '对方' }}</span>
          <span class="reply-text">{{ getReplyPreview(replyingMessage) }}</span>
        </div>
        <button class="reply-close" @click="clearReply">
          ×
        </button>
      </div>
      <textarea
        ref="textareaRef"
        v-model="content"
        placeholder="请输入消息... (支持拖拽文件上传)"
        @keydown="handleKeydown"
        @input="handleInput"
        @paste="handlePaste"
        @focus="isFocused = true"
        @blur="isFocused = false"
      />
    </div>

    <!-- 发送按钮 -->
    <div class="footer">
      <el-button
        type="primary"
        size="small"
        :disabled="!content.trim()"
        :loading="sending"
        @click="handleSend"
      >
        发送
      </el-button>
    </div>

    <input
      ref="fileRef"
      type="file"
      hidden
      :accept="uploadType === 'image' ? 'image/*' : '*/*'"
      @change="onFileChange"
    >

    <!-- @成员选择器 -->
    <AtMemberPicker
      ref="atMemberPickerRef"
      :session-id="session?.id || session?.targetId"
      @select="handleAtMemberSelect"
    />

    <!-- 上传进度弹窗 -->
    <el-dialog
      v-model="showUploadProgress"
      title="文件上传"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="upload-progress-content">
        <div class="file-info">
          <span class="file-name">{{ currentUploadFile?.name }}</span>
          <span class="file-size">{{ formatFileSize(currentUploadFile?.size) }}</span>
        </div>
        <el-progress
          :percentage="uploadProgress"
          :status="uploadStatus"
          :stroke-width="8"
        />
        <div class="upload-actions">
          <el-button
            v-if="uploadStatus === ''"
            size="small"
            @click="handlePauseUpload"
          >
            {{ isUploadPaused ? '继续' : '暂停' }}
          </el-button>
          <el-button
            v-if="uploadStatus === ''"
            size="small"
            type="danger"
            @click="handleCancelUpload"
          >
            取消
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { ChatDotRound, Picture, Files, UserFilled, Upload, FolderOpened } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { parseUrlMetadata } from '@/api/im/urlMetadata'
import {
  initChunkUpload,
  uploadChunk,
  mergeChunks,
  pauseChunkUpload,
  cancelChunkUpload,
  resumeChunkUpload
} from '@/api/im/file'
import AtMemberPicker from './AtMemberPicker.vue'

const props = defineProps({
  session: Object,
  sending: Boolean,
  editingMessage: Object
})
const emit = defineEmits(['send', 'upload-file', 'upload-image', 'upload-batch', 'typing', 'edit-save', 'edit-cancel'])

const store = useStore()
const content = ref('')
const textareaRef = ref(null)
const fileRef = ref(null)
const atMemberPickerRef = ref(null)
const uploadType = ref(null)
const isFocused = ref(false)

// 拖拽上传状态
const isDragOver = ref(false)

// 分片上传状态
const showUploadProgress = ref(false)
const currentUploadFile = ref(null)
const uploadProgress = ref(0)
const uploadStatus = ref('') // '', 'success', 'exception'
const isUploadPaused = ref(false)
const currentUploadId = ref(null)
const chunkUploadController = ref(null) // AbortController

const replyingMessage = computed(() => store.state.im?.message?.replyingMessage || null)

// 分片上传配置
const CHUNK_SIZE = 5 * 1024 * 1024 // 5MB 每片
const LARGE_FILE_THRESHOLD = 20 * 1024 * 1024 // 20MB 以上使用分片上传

// 判断是否为群聊会话
const isGroupSession = computed(() => {
  return props.session?.type === 'GROUP' || props.session?.sessionType === 'GROUP'
})

watch(() => props.session?.id, (id) => {
  if (!id) return
  const session = store.state.im?.session?.sessions?.find(s => s.id === id)
  content.value = session?.draftContent || ''
})

// 编辑消息模式：设置内容
watch(() => props.editingMessage, (msg) => {
  if (msg && msg.type === 'TEXT') {
    content.value = msg.content || ''
    nextTick(() => {
      textareaRef.value?.focus()
    })
  }
}, { immediate: true })

let draftTimer = null
watch(content, (val) => {
  const sessionId = props.session?.id
  if (!sessionId) return
  if (draftTimer) clearTimeout(draftTimer)
  draftTimer = setTimeout(() => {
    store.dispatch('im/session/saveDraft', { sessionId, content: val })
  }, 300)
})

// URL 正则匹配
const urlPattern = /(https?:\/\/[^\s<]+[^<.,.;"')\]\s])/g

/**
 * 计算文件MD5（简化版，用于文件标识）
 * 注意：这不是真正的MD5，只是生成唯一标识
 */

/**
 * 简易MD5计算（使用文件名+大小+修改时间生成唯一标识）
 */
const generateFileId = (file) => {
  const str = `${file.name}-${file.size}-${file.lastModified}`
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash
  }
  return Math.abs(hash).toString(16)
}

/**
 * 格式化文件大小
 */
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 处理拖拽进入
 */
const handleDragOver = (e) => {
  const items = e.dataTransfer?.items
  if (items) {
    for (const item of items) {
      if (item.kind === 'file') {
        isDragOver.value = true
        return
      }
    }
  }
}

/**
 * 处理拖拽离开
 */
const handleDragLeave = (e) => {
  if (!e.relatedTarget || !e.currentTarget.contains(e.relatedTarget)) {
    isDragOver.value = false
  }
}

/**
 * 处理文件拖放
 */
const handleDrop = async (e) => {
  isDragOver.value = false
  const files = e.dataTransfer?.files
  if (!files || files.length === 0) return

  if (files.length === 1) {
    const file = files[0]
    if (file.type.startsWith('image/')) {
      if (validateImageFile(file)) {
        emit('upload-image', file)
      }
    } else {
      if (validateFile(file)) {
        await handleFileUpload(file)
      }
    }
  } else {
    // 批量上传
    const fileList = Array.from(files).filter(f => validateFile(f, false))
    if (fileList.length > 0) {
      const formData = new FormData()
      fileList.forEach(file => {
        formData.append('files', file)
      })
      formData.append('sessionId', props.session?.id)
      emit('upload-batch', formData)
    }
  }
}

/**
 * 处理大文件分片上传
 */
const handleChunkUpload = async (file) => {
  const totalChunks = Math.ceil(file.size / CHUNK_SIZE)
  const fileId = generateFileId(file)

  try {
    // 初始化分片上传
    const initRes = await initChunkUpload({
      fileName: file.name,
      fileSize: file.size,
      fileMd5: fileId,
      chunkSize: CHUNK_SIZE,
      totalChunks,
      conversationId: props.session?.id
    })

    if (initRes.code !== 200) {
      ElMessage.error('初始化上传失败')
      return null
    }

    const { uploadId, uploadedChunks, skipUpload } = initRes.data

    // 秒传
    if (skipUpload) {
      return initRes.data.file
    }

    currentUploadId.value = uploadId
    chunkUploadController.value = new AbortController()
    showUploadProgress.value = true
    uploadProgress.value = 0
    uploadStatus.value = ''
    isUploadPaused.value = false

    // 上传分片
    const uploadedSet = new Set(uploadedChunks || [])
    let completedChunks = uploadedSet.size

    for (let i = 1; i <= totalChunks; i++) {
      if (isUploadPaused.value) {
        await new Promise(resolve => {
          const checkPause = setInterval(() => {
            if (!isUploadPaused.value) {
              clearInterval(checkPause)
              resolve()
            }
          }, 100)
        })
      }

      // 已上传的分片跳过
      if (uploadedSet.has(i)) {
        continue
      }

      const start = (i - 1) * CHUNK_SIZE
      const end = Math.min(start + CHUNK_SIZE, file.size)
      const chunk = file.slice(start, end)

      await uploadChunk(uploadId, i, chunk, (progressEvent) => {
        const chunkProgress = (progressEvent.loaded / progressEvent.total) * 100
        uploadProgress.value = Math.round(((completedChunks + chunkProgress / 100) / totalChunks) * 100)
      })

      completedChunks++
      uploadProgress.value = Math.round((completedChunks / totalChunks) * 100)
    }

    // 合并分片
    const mergeRes = await mergeChunks(uploadId)
    if (mergeRes.code === 200) {
      uploadStatus.value = 'success'
      setTimeout(() => {
        showUploadProgress.value = false
      }, 1000)
      return mergeRes.data
    } else {
      throw new Error('合并失败')
    }
  } catch (error) {
    if (error.name !== 'AbortError') {
      uploadStatus.value = 'exception'
      ElMessage.error('上传失败: ' + (error.message || '未知错误'))
    }
    return null
  }
}

/**
 * 暂停/继续上传
 */
const handlePauseUpload = async () => {
  if (isUploadPaused.value) {
    // 继续
    if (currentUploadId.value) {
      await resumeChunkUpload(currentUploadId.value)
    }
    isUploadPaused.value = false
  } else {
    // 暂停
    if (currentUploadId.value) {
      await pauseChunkUpload(currentUploadId.value)
    }
    isUploadPaused.value = true
  }
}

/**
 * 取消上传
 */
const handleCancelUpload = async () => {
  if (currentUploadId.value) {
    await cancelChunkUpload(currentUploadId.value)
  }
  if (chunkUploadController.value) {
    chunkUploadController.value.abort()
  }
  showUploadProgress.value = false
  currentUploadFile.value = null
  currentUploadId.value = null
  ElMessage.info('上传已取消')
}

/**
 * 处理文件上传（智能选择普通上传或分片上传）
 */
const handleFileUpload = async (file) => {
  currentUploadFile.value = file

  // 大文件使用分片上传
  if (file.size > LARGE_FILE_THRESHOLD) {
    const result = await handleChunkUpload(file)
    if (result) {
      emit('upload-file', result)
    }
  } else {
    // 普通文件上传
    emit('upload-file', file)
  }
}

/**
 * 提取文本中的第一个 URL
 */
const extractFirstUrl = (text) => {
  const match = text.match(urlPattern)
  return match ? match[0] : null
}

/**
 * 处理 @ 成员按钮点击
 */
const handleAtMemberClick = () => {
  if (!isGroupSession.value) {
    ElMessage.info('只能在群聊中@成员')
    return
  }
  atMemberPickerRef.value?.open()
}

/**
 * 处理键盘事件
 */
const handleKeydown = (e) => {
  if (e.key === 'Enter') {
    if (e.ctrlKey || e.metaKey) {
      e.preventDefault()
      const textarea = e.target
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const { value } = content
      content.value = value.substring(0, start) + '\n' + value.substring(end)
      nextTick(() => {
        textarea.selectionStart = textarea.selectionEnd = start + 1
      })
    } else {
      e.preventDefault()
      handleSend()
    }
  }
}

/**
 * 处理文本输入
 */
const handleInput = (e) => {
  if (e.data === '@') {
    if (props.session?.type === 'GROUP' || props.session?.sessionType === 'GROUP') {
      nextTick(() => {
        atMemberPickerRef.value?.open()
      })
    }
  }
  emit('typing')
}

/**
 * 处理 @ 成员选择
 */
const handleAtMemberSelect = (member) => {
  if (member.id === 'all') {
    content.value += '@所有人 '
  } else {
    content.value += `@${member.nickname || member.username} `
  }
  nextTick(() => {
    const textarea = textareaRef.value
    if (textarea) {
      textarea.focus()
      const { length } = content.value
      textarea.setSelectionRange(length, length)
    }
  })
}

/**
 * 发送消息
 */
const handleSend = async () => {
  if (!content.value.trim()) return

  const text = content.value.trim()

  // 编辑模式
  if (props.editingMessage) {
    emit('edit-save', {
      messageId: props.editingMessage.id || props.editingMessage.messageId,
      content: text
    })
    content.value = ''
    return
  }

  const replyToMessageId = replyingMessage.value?.messageId || replyingMessage.value?.id || null
  const url = extractFirstUrl(text)

  if (url && text.trim() === url) {
    try {
      const res = await parseUrlMetadata(url)
      if (res.code === 200 && res.data) {
        emit('send', {
          content: JSON.stringify({
            url,
            title: res.data.title || '',
            description: res.data.description || '',
            imageUrl: res.data.imageUrl || res.data.thumbnail || ''
          }),
          type: 'LINK',
          replyToMessageId
        })
        content.value = ''
        clearReply()
        return
      }
    } catch (e) {
      console.warn('URL 元数据解析失败:', e)
    }
  }

  emit('send', { content: text, type: 'TEXT', replyToMessageId })
  content.value = ''
  clearReply()
}

const insertEmoji = (emoji) => {
  content.value += emoji
}

const triggerUpload = (type) => {
  uploadType.value = type
  if (type === 'batch') {
    fileRef.value.multiple = true
  } else {
    fileRef.value.multiple = false
  }
  fileRef.value.click()
}

const onFileChange = async (e) => {
  const files = e.target.files
  if (!files || files.length === 0) return

  if (uploadType.value === 'batch') {
    const fileList = Array.from(files)
    const oversizedFiles = fileList.filter(file => file.size > 100 * 1024 * 1024)
    if (oversizedFiles.length > 0) {
      ElMessage.warning(`文件大小不能超过 100MB (${oversizedFiles.length}个文件超限)`)
      return
    }
    const formData = new FormData()
    fileList.forEach(file => {
      formData.append('files', file)
    })
    formData.append('sessionId', props.session?.id)
    emit('upload-batch', formData)
  } else {
    const file = files[0]
    if (uploadType.value === 'image') {
      if (!validateImageFile(file)) return
      emit('upload-image', file)
    } else {
      if (!validateFile(file)) return
      await handleFileUpload(file)
    }
  }

  fileRef.value.value = ''
}

const handlePaste = async (e) => {
  const items = e.clipboardData?.items || []
  if (!items.length) return

  for (const item of items) {
    if (item.type.startsWith('image/')) {
      const file = item.getAsFile()
      if (file) {
        if (!validateImageFile(file)) return
        emit('upload-image', file)
        e.preventDefault()
        return
      }
    }
  }

  for (const item of items) {
    if (item.kind === 'file') {
      const file = item.getAsFile()
      if (file) {
        if (!validateFile(file)) return
        await handleFileUpload(file)
        e.preventDefault()
        return
      }
    }
  }
}

const validateImageFile = (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 10MB')
    return false
  }
  return true
}

const validateFile = (file, showMessage = true) => {
  if (file.size > 500 * 1024 * 1024) {
    if (showMessage) ElMessage.warning('文件大小不能超过 500MB')
    return false
  }
  return true
}

const clearReply = () => {
  store.dispatch('im/message/clearReplyingMessage')
}

const cancelEdit = () => {
  content.value = ''
  emit('edit-cancel')
}

const getReplyPreview = (msg) => {
  if (!msg) return ''
  if (msg.type === 'TEXT') return (msg.content || '').slice(0, 60)
  if (msg.type === 'IMAGE') return '[图片]'
  if (msg.type === 'FILE') return '[文件]'
  if (msg.type === 'LINK') return '[链接]'
  return '[消息]'
}
</script>

<style scoped lang="scss">
.message-input {
  background: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-light);
  padding: var(--dt-spacing-sm) var(--dt-spacing-lg);
  display: flex;
  flex-direction: column;
  min-height: var(--dt-chat-input-height, 96px);
  border-left: 1px solid transparent;
  border-right: 1px solid transparent;
  transition: border-color var(--dt-transition-fast);
  position: relative;

  &.focused {
    border-color: var(--dt-brand-color);
    border-top-color: var(--dt-brand-color);
  }

  &.drag-over {
    border-color: var(--dt-brand-color);
    border-width: 2px;
    background: var(--dt-brand-lighter);
  }
}

.drag-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--dt-brand-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border: 2px dashed var(--dt-brand-color);
  border-radius: 4px;
  margin: 2px;
}

.drag-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--dt-spacing-sm);
  color: var(--dt-brand-color);

  .drag-icon {
    font-size: 48px;
  }

  span {
    font-size: 16px;
    font-weight: 500;
  }
}

.toolbar {
  height: var(--dt-toolbar-height, 40px);
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-xs);
  padding-bottom: var(--dt-spacing-sm);
  border-bottom: 1px solid var(--dt-border-lighter);

  .toolbar-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: var(--dt-spacing-xs);
    padding: var(--dt-spacing-xs) var(--dt-spacing-md);
    border: none;
    background: transparent;
    border-radius: var(--dt-radius-sm);
    cursor: pointer;
    color: var(--dt-text-secondary);
    transition: all var(--dt-transition-fast);
    height: var(--dt-btn-height-sm, 32px);

    .el-icon {
      font-size: var(--dt-icon-size-md, 18px);
      line-height: 1;
    }

    .btn-label {
      font-size: var(--dt-font-size-xs);
      line-height: 1;
    }

    &:hover {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
    }

    &:active {
      background: var(--dt-brand-lighter);
    }

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;
      &:hover {
        background: transparent;
        color: var(--dt-text-secondary);
      }
    }
  }
}

.text-area {
  flex: 1;
  padding: var(--dt-spacing-sm) 0;
  min-height: var(--dt-input-min-height, 60px);

  .reply-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: var(--dt-brand-lighter);
    color: var(--dt-text-primary);
    border-radius: var(--dt-radius-sm);
    padding: var(--dt-spacing-xs) var(--dt-spacing-sm);
    margin-bottom: var(--dt-spacing-sm);
    font-size: var(--dt-font-size-sm);
  }

  .edit-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: var(--dt-warning-bg);
    color: var(--dt-text-primary);
    border-radius: var(--dt-radius-sm);
    padding: var(--dt-spacing-xs) var(--dt-spacing-sm);
    margin-bottom: var(--dt-spacing-sm);
    font-size: var(--dt-font-size-sm);
    border-left: 3px solid var(--dt-warning-color);
  }

  .edit-info,
  .reply-info {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-xs);
    min-width: 0;
  }

  .edit-label {
    color: var(--dt-warning-color);
    font-weight: var(--dt-font-weight-medium);
  }

  .reply-label {
    color: var(--dt-brand-color);
    font-weight: var(--dt-font-weight-medium);
  }

  .reply-name {
    color: var(--dt-text-secondary);
  }

  .reply-text,
  .edit-text {
    color: var(--dt-text-tertiary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 260px;
  }

  .reply-close,
  .edit-close {
    border: none;
    background: transparent;
    color: var(--dt-text-secondary);
    cursor: pointer;
    font-size: var(--dt-font-size-base);
    line-height: 1;
    padding: 0 4px;
  }

  textarea {
    width: 100%;
    min-height: var(--dt-input-min-height, 60px);
    max-height: var(--dt-input-max-height, 300px);
    border: none;
    outline: none;
    resize: none;
    font-size: var(--dt-font-size-base);
    line-height: 1.6;
    color: var(--dt-text-primary);
    background: transparent;
    font-family: var(--dt-font-family);

    &::placeholder {
      color: var(--dt-text-quaternary);
    }
  }
}

.footer {
  display: flex;
  justify-content: flex-end;
  padding-top: var(--dt-spacing-sm);

  .el-button {
    min-width: var(--dt-btn-min-width, 80px);
    height: var(--dt-btn-height-md, 36px);
    font-size: var(--dt-font-size-base);
    font-weight: var(--dt-font-weight-medium);
    border-radius: var(--dt-radius-sm);
  }
}

.upload-progress-content {
  .file-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: var(--dt-spacing-md);

    .file-name {
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 250px;
    }

    .file-size {
      color: var(--dt-text-secondary);
      font-size: var(--dt-font-size-sm);
    }
  }

  .upload-actions {
    display: flex;
    justify-content: center;
    gap: var(--dt-spacing-sm);
    margin-top: var(--dt-spacing-md);
  }
}

// 暗色模式适配
:global(.dark) {
  .message-input {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .toolbar {
    border-color: var(--dt-border-dark);
  }

  .text-area .reply-bar {
    background: var(--dt-bg-hover-dark);
    color: var(--dt-text-primary-dark);
  }

  .text-area textarea {
    color: var(--dt-text-primary-dark);
    &::placeholder { color: var(--dt-text-quaternary-dark); }
  }
}
</style>
