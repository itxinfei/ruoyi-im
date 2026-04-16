<template>
  <div
    class="chat-input-wrapper"
    :class="{ 'is-dragover': isDragover }"
    @dragover.prevent="handleDragOver"
    @dragleave.prevent="handleDragLeave"
    @drop.prevent="handleDrop"
  >
    <!-- 拖拽上传遮罩 -->
    <div v-if="isDragover" class="dragover-mask">
      <el-icon class="drag-icon">
        <Upload />
      </el-icon>
      <span>拖拽文件到此处上传</span>
    </div>

    <!-- 拖拽上传进度遮罩 -->
    <div v-if="dragUploadingFiles.length > 0" class="drag-progress-mask">
      <div class="drag-progress-content">
        <el-icon class="drag-progress-icon">
          <Upload />
        </el-icon>
        <span class="drag-progress-text">正在上传 {{ dragUploadingFiles.length }} 个文件...</span>
        <div class="drag-progress-bar">
          <el-progress
            :percentage="dragProgressPercentage"
            :stroke-width="4"
            :show-text="false"
          />
        </div>
        <span class="drag-progress-detail">
          {{ dragProgressText }}
        </span>
      </div>
    </div>

    <!-- 引用消息预览区 -->
    <div v-if="replyingMessage" class="reply-preview-bar">
      <div class="reply-content">
        <span class="reply-author">{{ replyingMessage.senderName }}: </span>
        <span class="reply-text">{{ replyingMessage.content }}</span>
      </div>
      <el-icon class="icon-close" @click="clearReply">
        <Close />
      </el-icon>
    </div>

    <!-- 编辑模式提示条 -->
    <div v-if="isEditMode" class="edit-mode-bar">
      <span class="edit-mode-text">编辑消息</span>
      <span class="edit-mode-hint">Enter 保存 · ESC 取消</span>
      <el-icon class="icon-close" @click="cancelEdit">
        <Close />
      </el-icon>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <!-- 表情选择器 -->
      <el-popover
        v-model:visible="emojiPickerVisible"
        placement="top"
        :width="320"
        trigger="click"
      >
        <template #reference>
          <el-icon class="tool-icon" title="表情">
            <Star />
          </el-icon>
        </template>
        <div class="emoji-picker">
          <div class="emoji-grid">
            <span
              v-for="emoji in emojiList"
              :key="emoji"
              class="emoji-item"
              @click="insertEmoji(emoji)"
            >{{ emoji }}</span>
          </div>
        </div>
      </el-popover>

      <!-- 图片上传 -->
      <el-upload
        action="#"
        :show-file-list="false"
        :auto-upload="false"
        :on-change="processImageSelect"
        accept="image/*"
        class="upload-wrapper"
      >
        <el-icon class="tool-icon" title="图片">
          <Picture />
        </el-icon>
      </el-upload>

      <!-- 文件上传 -->
      <el-upload
        action="#"
        :show-file-list="false"
        :auto-upload="false"
        :on-change="processFileSelect"
        class="upload-wrapper"
      >
        <el-icon class="tool-icon" title="文件">
          <Folder />
        </el-icon>
      </el-upload>

      <!-- 视频上传 -->
      <el-upload
        action="#"
        :show-file-list="false"
        :auto-upload="false"
        :on-change="processVideoSelect"
        accept="video/*"
        class="upload-wrapper"
      >
        <el-icon class="tool-icon" title="视频">
          <VideoCamera />
        </el-icon>
      </el-upload>

      <!-- 名片 -->
      <el-icon class="tool-icon" title="名片" @click="openCardPicker">
        <User />
      </el-icon>

      <!-- 位置 -->
      <el-icon class="tool-icon" title="位置" @click="openLocationPicker">
        <LocationInformation />
      </el-icon>

      <!-- @成员 -->
      <el-icon class="tool-icon" title="@成员" @click="openAtMemberPicker">
        <User />
      </el-icon>

      <!-- 语音录制 -->
      <el-icon
        class="tool-icon"
        :class="{ 'is-recording': isRecording }"
        title="语音"
        @click="toggleRecording"
      >
        <Microphone />
      </el-icon>
    </div>

    <!-- 录音状态区 -->
    <div v-if="isRecording" class="recording-bar">
      <div class="recording-info">
        <span class="recording-dot" />
        <span class="recording-text">录音中 {{ formatDuration(recordingDuration) }}</span>
      </div>
      <div class="recording-actions">
        <button class="rec-btn cancel" @click="cancelRecording">
          取消
        </button>
        <button class="rec-btn send" @click="sendRecording">
          发送
        </button>
      </div>
    </div>

    <!-- 图片预览区 -->
    <div v-if="pendingImages.length > 0" class="image-preview-bar">
      <div v-for="(img, index) in pendingImages" :key="index" class="preview-item">
        <el-image :src="img.url" fit="cover" class="preview-img" />
        <el-icon class="preview-remove" @click="removeImage(index)">
          <Close />
        </el-icon>
      </div>
    </div>

    <!-- 视频预览区 -->
    <div v-if="pendingVideos.length > 0" class="video-preview-bar">
      <div v-for="(video, index) in pendingVideos" :key="index" class="preview-item video-preview-item">
        <video :src="video.url" class="preview-video" />
        <el-icon class="preview-duration">
          <Clock />
        </el-icon>
        <span class="preview-duration-text">{{ formatDuration(video.duration) }}</span>
        <el-icon class="preview-remove" @click="removeVideo(index)">
          <Close />
        </el-icon>
      </div>
    </div>

    <!-- 高级输入区 -->
    <div class="input-main" style="position: relative;">
      <div
        ref="editorRef"
        class="rich-editor"
        contenteditable="true"
        :placeholder="isEditMode ? '修改消息...' : '请输入消息...'"
        @keydown.enter.exact.prevent="executeSendMessage"
        @keydown.esc.stop="cancelEdit"
        @keydown.ctrl.enter.stop="insertNewLine"
        @keydown.shift.2.stop="openAtMemberPicker"
        @paste="processPaste"
        @input="handleInput"
      />

      <!-- @成员选择器 -->
      <AtMemberPicker
        v-model:visible="atMemberPickerVisible"
        :members="groupMembers"
        :position="atPickerPosition"
        @select="handleAtMemberSelect"
        @close="atMemberPickerVisible = false"
      />
    </div>

    <!-- 底部发送栏 -->
    <div class="input-footer">
      <span class="tip">{{ isEditMode ? 'Enter 保存，ESC 取消' : 'Enter 发送，Ctrl+Enter 换行' }}</span>
      <button
        :class="['send-btn', { 'is-active': canSend }]"
        @click="executeSendMessage"
      >
        发送
      </button>
    </div>

    <!-- 名片选择对话框 -->
    <el-dialog
      v-model="cardPickerVisible"
      title="选择联系人"
      width="400px"
      append-to-body
    >
      <div class="card-picker-search">
        <el-input v-model="cardSearchKeyword" placeholder="搜索联系人" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div class="card-picker-list">
        <div
          v-for="contact in filteredContacts"
          :key="contact.id"
          class="card-picker-item"
          @click="selectCardContact(contact)"
        >
          <img :src="contact.avatar || '/avatars/default.png'" class="card-avatar">
          <div class="card-info">
            <div class="card-name">
              {{ contact.name }}
            </div>
            <div class="card-dept">
              {{ contact.department || '' }}
            </div>
          </div>
        </div>
        <div v-if="filteredContacts.length === 0" class="card-empty">
          暂无联系人
        </div>
      </div>
    </el-dialog>

    <!-- 位置选择对话框 -->
    <el-dialog
      v-model="locationPickerVisible"
      title="发送位置"
      width="420px"
      append-to-body
    >
      <el-form :model="locationForm" label-width="80px">
        <el-form-item label="位置名称" required>
          <el-input v-model="locationForm.name" placeholder="如：公司、家里" maxlength="50" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="locationForm.address" placeholder="如：北京市朝阳区xxx路" maxlength="100" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input-number
            v-model="locationForm.latitude"
            :precision="6"
            :step="0.000001"
            placeholder="纬度"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input-number
            v-model="locationForm.longitude"
            :precision="6"
            :step="0.000001"
            placeholder="经度"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="locationPickerVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="sendLocation">
          发送
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="js">
/**
 * ChatInputArea.vue (对齐钉钉无边框沉浸式输入 & 状态驱动发送按钮 + 表情选择器 + 图片预览)
 */
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, Star, Picture, Folder, Upload, Microphone, VideoCamera, Clock, User, Search, LocationInformation } from '@element-plus/icons-vue'
import { getContacts } from '@/api/im/contact'
import { getGroupMembers } from '@/api/im/group'
import { uploadImage, uploadFile, initChunkUpload, uploadChunk, mergeChunks } from '@/api/im/file'
import AtMemberPicker from './AtMemberPicker.vue'

const props = defineProps({
  replyingMessage: Object,
  editingMessage: Object,
  session: Object,  // 当前会话信息，用于@成员功能
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['send', 'clear-reply', 'update:modelValue', 'edit-save', 'edit-cancel'])

const editorRef = ref(null)
const hasContent = ref(false)
const emojiPickerVisible = ref(false)
const pendingImages = ref([])
const pendingVideos = ref([])
const isDragover = ref(false)

// 拖拽上传状态
const dragUploadingFiles = ref([]) // { file, progress, status, fileType, result }
let dragUploadAbortController = null // 取消上传的控制器

// 拖拽进度计算属性
const dragProgressPercentage = computed(() => {
  if (dragUploadingFiles.value.length === 0) return 0
  const total = dragUploadingFiles.value.reduce((sum, f) => sum + f.progress, 0)
  return Math.round(total / dragUploadingFiles.value.length)
})

const dragProgressText = computed(() => {
  const completed = dragUploadingFiles.value.filter(f => f.status === 'completed').length
  const uploading = dragUploadingFiles.value.filter(f => f.status === 'uploading').length
  const failed = dragUploadingFiles.value.filter(f => f.status === 'failed').length
  const parts = []
  if (completed > 0) parts.push(`${completed} 完成`)
  if (uploading > 0) parts.push(`${uploading} 上传中`)
  if (failed > 0) parts.push(`${failed} 失败`)
  return parts.join(' · ')
})

let isInternalSet = false // 防止 watch 设置时触发 handleInput 冒泡
const isEditMode = ref(false) // 编辑模式标识

// 录音状态
const isRecording = ref(false)
const recordingDuration = ref(0)
let mediaRecorder = null
let recordedChunks = []
let recordingTimer = null

// 名片选择状态
const cardPickerVisible = ref(false)
const cardSearchKeyword = ref('')
const contactList = ref([])

// 位置选择状态
const locationPickerVisible = ref(false)
const locationForm = ref({
  name: '',
  address: '',
  latitude: 39.9042,
  longitude: 116.4074
})

// @成员选择状态
const atMemberPickerVisible = ref(false)
const atPickerPosition = ref({ top: 0, left: 0 })
const groupMembers = ref([])

// 监听外部草稿变化（会话切换时恢复草稿）
watch(() => props.modelValue, (newVal) => {
  if (!editorRef.value) return
  const currentText = editorRef.value.innerText
  if (currentText !== newVal) {
    isInternalSet = true
    editorRef.value.innerText = newVal || ''
    hasContent.value = !!newVal
  }
})

// 监听编辑消息变化（进入/退出编辑模式）
watch(() => props.editingMessage, (newVal) => {
  if (newVal) {
    isEditMode.value = true
    isInternalSet = true
    if (editorRef.value) {
      editorRef.value.innerText = newVal.content || ''
      hasContent.value = !!newVal.content
    }
    nextTick(() => editorRef.value?.focus())
  } else {
    isEditMode.value = false
  }
})

// 常用表情列表
const emojiList = [
  '😀', '😃', '😄', '😁', '😅', '😂', '🤣', '😊', '😇', '🙂', '😉', '😌',
  '😍', '🥰', '😘', '😋', '😛', '🤔', '🤨', '😐', '😑', '😶', '🙄', '😏',
  '😣', '😥', '😮', '🤐', '😯', '😪', '😫', '😴', '😌', '😛', '😜', '😝',
  '🤤', '😒', '😓', '😔', '😕', '🙃', '🤑', '😲', '🙁', '😖', '😞', '😟',
  '😤', '😢', '😭', '😦', '😧', '😨', '😩', '🤯', '😬', '😰', '😱', '👍', '👎'
]

// 是否可以发送（有文字或有图片或有视频）
const canSend = computed(() => {
  return hasContent.value || pendingImages.value.length > 0 || pendingVideos.value.length > 0
})

const handleInput = () => {
  if (isInternalSet) {
    isInternalSet = false
    return
  }
  const text = editorRef.value.innerText
  hasContent.value = !!text.trim()
  emit('update:modelValue', text)
}

// 插入表情
const insertEmoji = (emoji) => {
  if (!editorRef.value) return
  editorRef.value.innerText += emoji
  editorRef.value.focus()
  hasContent.value = true
  emojiPickerVisible.value = false
  emit('update:modelValue', editorRef.value.innerText)
}

// 插入换行符
const insertNewLine = () => {
  if (!editorRef.value) return
  document.execCommand('insertHTML', false, '<br>')
}

// ========== @成员功能 ==========
// 打开@成员选择器
const openAtMemberPicker = async () => {
  // 获取当前会话的群成员
  try {
    const session = await getCurrentSession()
    if (!session) return

    const isGroup = session.type === 'GROUP'

    if (isGroup && session.targetId) {
      // 加载群成员
      const res = await getGroupMembers(session.targetId)
      if (res.code === 200) {
        groupMembers.value = (res.data || []).map(m => ({
          userId: m.userId,
          nickname: m.groupNickname || m.userName,
          avatar: m.userAvatar,
          role: m.role
        }))
      }
    }

    // 计算选择器位置
    calculateAtPickerPosition()
    atMemberPickerVisible.value = true
  } catch (e) {
    console.error('获取群成员失败', e)
    ElMessage.error('获取成员列表失败')
  }
}

// 获取当前会话
const getCurrentSession = () => {
  return Promise.resolve(props.session || null)
}

// 计算@选择器位置
const calculateAtPickerPosition = () => {
  if (!editorRef.value) {
    atPickerPosition.value = { top: -300, left: 0 }
    return
  }

  atPickerPosition.value = {
    top: -290,  // 固定高度 300px，减去一些边距
    left: 16     // 与输入框左边距对齐
  }
}

// 处理@成员选择
const handleAtMemberSelect = (member) => {
  if (!editorRef.value) return

  // 获取当前光标位置之前的文本
  const selection = window.getSelection()
  if (!selection.rangeCount) return

  const range = selection.getRangeAt(0)
  const textBeforeCursor = range.startContainer.textContent?.slice(0, range.startOffset) || ''

  // 查找最后一个 @ 位置
  const atIndex = textBeforeCursor.lastIndexOf('@')
  if (atIndex === -1) return

  // 删除 @ 及其后面的内容，并插入 @昵称
  const textNode = range.startContainer

  // 创建新文本：@昵称 + 空格
  const mentionText = `@${member.nickname} `

  // 使用 DOM 操作替换
  textNode.textContent = textBeforeCursor.slice(0, atIndex) + mentionText

  // 移动光标到插入文本之后
  const newRange = document.createRange()
  newRange.setStart(textNode, textNode.textContent.length)
  newRange.collapse(true)
  selection.removeAllRanges()
  selection.addRange(newRange)

  // 更新内容
  hasContent.value = true
  emit('update:modelValue', editorRef.value.innerText)

  // 关闭选择器
  atMemberPickerVisible.value = false
}


const processPaste = async (e) => {
  const items = (e.clipboardData || e.originalEvent.clipboardData).items
  for (const item of items) {
    if (item.type.indexOf('image') !== -1) {
      const file = item.getAsFile()
      addPendingImage(file)
      e.preventDefault()
    }
  }
}

const executeSendMessage = () => {
  // 编辑模式：直接触发保存
  if (isEditMode.value) {
    const content = editorRef.value.innerText.trim()
    if (content) {
      emit('edit-save', { content })
    }
    return
  }

  // 发送图片
  if (pendingImages.value.length > 0) {
    pendingImages.value.forEach(img => {
      emit('send', { type: 'IMAGE', file: img.file })
    })
    pendingImages.value = []
  }

  // 发送视频
  if (pendingVideos.value.length > 0) {
    pendingVideos.value.forEach(video => {
      emit('send', { type: 'VIDEO', file: video.file, duration: video.duration })
    })
    pendingVideos.value = []
  }

  // 发送文字
  const content = editorRef.value.innerText.trim()
  if (content) {
    emit('send', { type: 'TEXT', content })
    editorRef.value.innerHTML = ''
  }

  hasContent.value = false
  emit('update:modelValue', '')
}

// 取消编辑
const cancelEdit = () => {
  emit('edit-cancel')
}

// 处理图片选择
const processImageSelect = (file) => {
  addPendingImage(file.raw)
}

// 处理文件选择
const processFileSelect = (file) => {
  emit('send', { type: 'FILE', file: file.raw, fileName: file.name })
}

// 处理视频选择
const processVideoSelect = (file) => {
  addPendingVideo(file.raw)
}

// 添加待发送视频
const addPendingVideo = (file) => {
  const url = URL.createObjectURL(file)
  // 获取视频时长
  const video = document.createElement('video')
  video.preload = 'metadata'
  video.onloadedmetadata = () => {
    const duration = Math.floor(video.duration)
    pendingVideos.value.push({ file, url, duration })
    URL.revokeObjectURL(url)
  }
  video.onerror = () => {
    pendingVideos.value.push({ file, url, duration: 0 })
  }
  video.src = url
}

// 移除待发送视频
const removeVideo = (index) => {
  URL.revokeObjectURL(pendingVideos.value[index].url)
  pendingVideos.value.splice(index, 1)
}

// 添加待发送图片
const addPendingImage = (file) => {
  const url = URL.createObjectURL(file)
  pendingImages.value.push({ file, url })
}

// 移除待发送图片
const removeImage = (index) => {
  URL.revokeObjectURL(pendingImages.value[index].url)
  pendingImages.value.splice(index, 1)
}

// 过滤联系人
const filteredContacts = computed(() => {
  if (!cardSearchKeyword.value) return contactList.value
  const keyword = cardSearchKeyword.value.toLowerCase()
  return contactList.value.filter(c =>
    c.name?.toLowerCase().includes(keyword) ||
    c.department?.toLowerCase().includes(keyword)
  )
})

// 打开名片选择器
const openCardPicker = async () => {
  cardSearchKeyword.value = ''
  cardPickerVisible.value = true
  try {
    const res = await getContacts()
    if (res.code === 200) {
      contactList.value = res.data || []
    } else {
      throw new Error(res.message)
    }
  } catch (e) {
    ElMessage.error('获取联系人失败')
    contactList.value = []
  }
}

// 选择联系人发送名片
const selectCardContact = (contact) => {
  cardPickerVisible.value = false
  emit('send', {
    type: 'CARD',
    card: {
      cardType: 'user',
      userId: contact.id,
      userName: contact.name,
      userAvatar: contact.avatar,
      department: contact.department || ''
    }
  })
}

// 打开位置选择器
const openLocationPicker = () => {
  locationForm.value = {
    name: '',
    address: '',
    latitude: 39.9042,
    longitude: 116.4074
  }
  locationPickerVisible.value = true
}

// 发送位置
const sendLocation = () => {
  if (!locationForm.value.name.trim()) {
    return
  }
  locationPickerVisible.value = false
  emit('send', {
    type: 'LOCATION',
    location: {
      name: locationForm.value.name,
      address: locationForm.value.address || '',
      latitude: locationForm.value.latitude,
      longitude: locationForm.value.longitude
    }
  })
}

const clearReply = () => emit('clear-reply')

// 拖拽上传
const handleDragOver = (e) => {
  const hasFiles = e.dataTransfer?.types.includes('Files')
  if (hasFiles) {
    isDragover.value = true
  }
}

const handleDragLeave = (e) => {
  // 仅当离开整个输入区时才隐藏遮罩，防止子元素触发闪烁
  if (!e.currentTarget.contains(e.relatedTarget)) {
    isDragover.value = false
  }
}

const handleDrop = (e) => {
  isDragover.value = false
  const files = e.dataTransfer?.files
  if (!files || files.length === 0) return

  for (const file of files) {
    const fileType = getFileTypeByExtension(file)

    if (fileType === 'image') {
      addPendingImage(file)
    } else if (fileType === 'video') {
      addPendingVideo(file)
    } else {
      // 其他文件：直接发送，使用分片上传（> 5MB）
      handleFileUpload(file, file.name)
    }
  }
}

// 根据文件扩展名判断文件类型
const getFileTypeByExtension = (file) => {
  const ext = file.name?.split('.').pop()?.toLowerCase() || ''

  // 图片类型
  const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg', 'ico']
  if (imageExts.includes(ext) || file.type.startsWith('image/')) {
    return 'image'
  }

  // 视频类型
  const videoExts = ['mp4', 'mov', 'avi', 'webm', 'mkv', 'flv', 'wmv', 'mpeg']
  if (videoExts.includes(ext) || file.type.startsWith('video/')) {
    return 'video'
  }

  return 'file'
}

// 处理文件上传（带进度反馈）
const handleFileUpload = async (file, fileName) => {
  const fileEntry = {
    file,
    fileName,
    progress: 0,
    status: 'uploading',
    fileType: getFileTypeByExtension(file)
  }

  dragUploadingFiles.value.push(fileEntry)
  dragUploadingFiles.value = [...dragUploadingFiles.value]

  try {
    // 大文件（> 5MB）使用分片上传
    const isLargeFile = file.size > 5 * 1024 * 1024

    if (isLargeFile) {
      await uploadLargeFileWithProgress(file, fileName, (progress) => {
        fileEntry.progress = progress
        dragUploadingFiles.value = [...dragUploadingFiles.value]
      })
    } else {
      await uploadSmallFileWithProgress(file, fileName, (progress) => {
        fileEntry.progress = progress
        dragUploadingFiles.value = [...dragUploadingFiles.value]
      })
    }

    fileEntry.status = 'completed'
    fileEntry.progress = 100
    dragUploadingFiles.value = [...dragUploadingFiles.value]

    // 上传成功后发送消息
    emit('send', { type: 'FILE', file, fileName })
  } catch (error) {
    console.error('文件上传失败:', error)
    fileEntry.status = 'failed'
    dragUploadingFiles.value = [...dragUploadingFiles.value]
    ElMessage.error(`${fileName} 上传失败`)
  } finally {
    // 3秒后移除已完成/失败的上传记录
    setTimeout(() => {
      dragUploadingFiles.value = dragUploadingFiles.value.filter(f => f.status !== 'completed' && f.status !== 'failed')
    }, 3000)
  }
}

// 小文件普通上传（带进度）
const uploadSmallFileWithProgress = (file, fileName, onProgress) => {
  return new Promise((resolve, reject) => {
    const formData = new FormData()
    formData.append('file', file)
    if (fileName) {
      formData.append('fileName', fileName)
    }

    // 模拟进度（实际进度由 axios onUploadProgress 提供）
    let progress = 0
    const progressTimer = setInterval(() => {
      progress += 10
      if (progress < 90) {
        onProgress(progress)
      }
    }, 100)

    uploadFile(formData)
      .then(res => {
        clearInterval(progressTimer)
        onProgress(100)
        resolve(res)
      })
      .catch(err => {
        clearInterval(progressTimer)
        reject(err)
      })
  })
}

// 大文件分片上传（带进度）
const uploadLargeFileWithProgress = async (file, fileName, onProgress) => {
  const chunkSize = 2 * 1024 * 1024 // 2MB per chunk
  const totalChunks = Math.ceil(file.size / chunkSize)

  // 计算文件 MD5（简化版：使用文件名+大小+时间戳作为标识）
  const fileMd5 = await calculateFileMd5(file)

  // 初始化分片上传
  const initRes = await initChunkUpload({
    fileName,
    fileSize: file.size,
    fileMd5,
    chunkSize,
    totalChunks,
    conversationId: null
  })

  if (initRes.code !== 200) {
    throw new Error(initRes.message || '初始化分片上传失败')
  }

  const uploadId = initRes.data.uploadId

  // 上传每个分片
  let completedChunks = 0

  for (let i = 1; i <= totalChunks; i++) {
    const start = (i - 1) * chunkSize
    const end = Math.min(start + chunkSize, file.size)
    const chunk = file.slice(start, end)

    await uploadChunk(uploadId, i, chunk, (progressEvent) => {
      // 计算整体进度
      const chunkProgress = progressEvent.loaded / progressEvent.total
      const totalProgress = ((completedChunks + chunkProgress) / totalChunks) * 100
      onProgress(Math.round(totalProgress))
    })

    completedChunks++
    onProgress(Math.round((completedChunks / totalChunks) * 100))
  }

  // 合并分片
  const mergeRes = await mergeChunks(uploadId)

  if (mergeRes.code !== 200) {
    throw new Error(mergeRes.message || '合并分片失败')
  }

  return mergeRes
}

// 简化版文件 MD5 计算（使用 Web Crypto API）
const calculateFileMd5 = async (file) => {
  // 使用 File 对象直接计算 MD5
  const buffer = await file.arrayBuffer()
  const hashBuffer = await crypto.subtle.digest('SHA-256', buffer)
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
  return hashHex
}

// 录音
const toggleRecording = async () => {
  if (isRecording.value) {
    stopRecording()
  } else {
    await startRecording()
  }
}

const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream, { mimeType: 'audio/webm' })
    recordedChunks = []

    mediaRecorder.ondataavailable = (e) => {
      if (e.data.size > 0) recordedChunks.push(e.data)
    }

    mediaRecorder.start()
    isRecording.value = true
    recordingDuration.value = 0

    recordingTimer = setInterval(() => {
      recordingDuration.value++
    }, 1000)
  } catch (e) {
    console.error('无法访问麦克风', e)
    ElMessage.error('无法访问麦克风，请检查权限设置')
  }
}

const stopRecording = () => {
  if (!mediaRecorder || mediaRecorder.state === 'inactive') return
  mediaRecorder.stop()
  mediaRecorder.stream.getTracks().forEach(t => t.stop())
  clearInterval(recordingTimer)
  isRecording.value = false
}

const cancelRecording = () => {
  stopRecording()
  recordedChunks = []
  recordingDuration.value = 0
}

const sendRecording = () => {
  if (recordedChunks.length === 0) return
  const blob = new Blob(recordedChunks, { type: 'audio/webm' })
  const file = new File([blob], `录音_${formatDuration(recordingDuration.value)}.webm`, { type: 'audio/webm' })
  emit('send', { type: 'VOICE', file })
  recordedChunks = []
  recordingDuration.value = 0
  stopRecording()
}

const formatDuration = (seconds) => {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

onMounted(() => {
  editorRef.value?.focus()
})

// 组件卸载时清理定时器
onBeforeUnmount(() => {
  if (recordingTimer) {
    clearInterval(recordingTimer)
    recordingTimer = null
  }
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
    mediaRecorder.stream.getTracks().forEach(t => t.stop())
  }
})
</script>

<style scoped>
.chat-input-wrapper {
  display: flex;
  flex-direction: column;
  background-color: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-light);
  position: relative;
  background-image: linear-gradient(180deg, rgba(39, 126, 251, 0.03), transparent 36%);
}

/* 拖拽上传遮罩 */
.dragover-mask {
  position: absolute;
  inset: 0;
  background: var(--dt-bg-hover);
  border: 2px dashed var(--dt-brand-color);
  border-radius: var(--dt-radius-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  z-index: 10;
  color: var(--dt-brand-color);
  font-size: var(--dt-font-size-base);
  font-weight: 500;
}

.drag-icon {
  font-size: 32px;
}

/* 拖拽上传进度遮罩 */
.drag-progress-mask {
  position: absolute;
  inset: 0;
  background: var(--dt-bg-card);
  border: 2px solid var(--dt-brand-color);
  border-radius: var(--dt-radius-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.drag-progress-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--dt-spacing-sm);
  width: 280px;
}

.drag-progress-icon {
  font-size: 32px;
  color: var(--dt-brand-color);
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.drag-progress-text {
  font-size: var(--dt-font-size-base);
  font-weight: 500;
  color: var(--dt-text-primary);
}

.drag-progress-bar {
  width: 100%;
}

.drag-progress-detail {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
}

.chat-input-wrapper.is-dragover {
  border-color: var(--dt-brand-color);
}

/* 引用回复栏 - 钉钉规范：浅蓝背景 + 左侧 2px 蓝线 */
.reply-preview-bar {
  height: auto;
  max-height: 60px;
  background: var(--dt-brand-bg);
  border-left: 2px solid var(--dt-brand-color);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: var(--dt-spacing-sm) var(--dt-spacing-lg);
  margin: var(--dt-spacing-sm) var(--dt-spacing-xl) 0;
  border-radius: 0 var(--dt-radius-sm) var(--dt-radius-sm) 0;
}

/* 编辑模式提示条 */
.edit-mode-bar {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  padding: var(--dt-spacing-sm) var(--dt-spacing-lg);
  margin: var(--dt-spacing-sm) var(--dt-spacing-xl) 0;
  background: var(--dt-warning-bg);
  border-left: 2px solid var(--dt-warning-color);
  border-radius: 0 var(--dt-radius-sm) var(--dt-radius-sm) 0;
}

.edit-mode-text {
  font-size: var(--dt-font-size-sm);
  font-weight: 500;
  color: var(--dt-warning-color);
}

.edit-mode-hint {
  flex: 1;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
}

.reply-content {
  font-size: var(--dt-font-size-sm);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;  /* 钉钉规范：单行截断 */
  -webkit-box-orient: vertical;
  color: var(--dt-text-secondary);
}

.reply-author {
  color: var(--dt-text-tertiary);
}

.icon-close {
  cursor: pointer;
  color: var(--dt-text-tertiary);
  font-size: var(--dt-font-size-base);
  margin-left: 8px;
  transition: color var(--dt-transition-fast);
}

.icon-close:hover {
  color: var(--dt-text-primary);
}

/* 工具栏 - 钉钉风格 */
.toolbar {
  min-height: 48px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  border-bottom: 1px solid var(--dt-border-light);
}

/* 工具栏分隔符 */
.toolbar-divider {
  width: 1px;
  height: 24px;
  background: var(--dt-border-light);
  margin: 0 6px;
  flex-shrink: 0;
}

.tool-icon {
  font-size: 20px;
  color: var(--dt-text-icon);
  cursor: pointer;
  outline: none;
  padding: var(--dt-spacing-sm);
  border-radius: var(--dt-radius-lg);
  transition: color var(--dt-transition-fast);
  position: relative;
}

.tool-icon:hover {
  color: var(--dt-text-primary);
  background-color: var(--dt-bg-hover);
}

.tool-icon:active {
  background-color: var(--dt-brand-bg);
}

.tool-icon.is-active {
  color: var(--dt-brand-color);
  background-color: var(--dt-brand-bg);
}

.upload-wrapper {
  display: flex;
  align-items: center;
}

.tool-icon.is-recording {
  color: var(--dt-error-color);
}

/* 录音状态栏 */
.recording-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 var(--dt-spacing-xl);
  background: var(--dt-bg-body);
  border-top: 1px solid var(--dt-border-light);
}

.recording-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.recording-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--dt-error-color);
}

.recording-text {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

.recording-actions {
  display: flex;
  gap: var(--dt-spacing-lg);
}

.rec-btn {
  border: none;
  border-radius: var(--dt-radius-sm);
  padding: 4px 16px;
  font-size: var(--dt-font-size-sm);
  cursor: pointer;
  transition: background-color var(--dt-transition-fast), color var(--dt-transition-fast);
}

.rec-btn.cancel {
  background: var(--dt-bg-hover);
  color: var(--dt-text-secondary);
}

.rec-btn.cancel:hover {
  background: var(--dt-border-light);
}

.rec-btn.send {
  background: var(--dt-brand-color);
  color: var(--dt-text-white);
}

.rec-btn.send:hover {
  background: var(--dt-brand-hover);
}

/* 表情选择器 - 钉钉风格 */
.emoji-picker {
  padding: 12px;
  background: var(--dt-bg-card);
  border-radius: 12px 12px 0 0;
  border-top: 1px solid var(--dt-border-light);
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 6px;
}

.emoji-item {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;  /* 表情图标22px，保持非标准值 */
  cursor: pointer;
  border-radius: var(--dt-radius-lg);
  transition: background-color var(--dt-transition-fast);
}

.emoji-item:hover {
  background-color: var(--dt-bg-hover);
}

.emoji-item:active {
  background-color: var(--dt-brand-bg);
}

/* 图片预览区 - 钉钉风格 */
.image-preview-bar {
  display: flex;
  gap: var(--dt-spacing-sm);
  padding: 12px 20px;
  background-color: var(--dt-bg-body);
  overflow-x: auto;
}

.preview-item {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: var(--dt-radius-xl);
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: var(--dt-shadow-1);
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  background-color: var(--dt-overlay-bg);
  color: var(--dt-text-white);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: var(--dt-font-size-base);
  transition: background-color var(--dt-transition-fast);
}

.preview-remove:hover {
  background-color: var(--dt-error-color);
}

/* 视频预览区 */
.video-preview-bar {
  display: flex;
  gap: 8px;
  padding: 8px 20px;
  background-color: var(--dt-bg-body);
  overflow-x: auto;
}

.video-preview-item {
  position: relative;
  width: 96px;
  height: 64px;
  border-radius: var(--dt-radius-sm);
  overflow: hidden;
  flex-shrink: 0;
  background-color: var(--dt-bg-card);
}

.preview-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-duration {
  position: absolute;
  bottom: 2px;
  left: 4px;
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-white);
  background-color: var(--dt-overlay-bg);
  border-radius: var(--dt-radius-xs);
  padding: 1px 3px;
}

.preview-duration-text {
  position: absolute;
  bottom: 2px;
  right: 4px;
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-white);
  background-color: var(--dt-overlay-bg);
  border-radius: var(--dt-radius-xs);
  padding: 1px 3px;
}

/* 名片选择器 */
.card-picker-search {
  margin-bottom: var(--dt-spacing-lg);
}

.card-picker-list {
  max-height: 300px;
  overflow-y: auto;
}

.card-picker-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: var(--dt-radius-sm);
  cursor: pointer;
  transition: background-color var(--dt-transition-fast);
}

.card-picker-item:hover {
  background-color: var(--dt-bg-hover);
}

.card-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--dt-radius-sm);
  object-fit: cover;
}

.card-info {
  flex: 1;
  overflow: hidden;
}

.card-name {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-dept {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-empty {
  text-align: center;
  padding: 24px;
  color: var(--dt-text-tertiary);
  font-size: var(--dt-font-size-sm);
}

/* 主输入区 - 钉钉规范优化 */
.input-main {
  flex: 1;
  padding: 8px var(--dt-spacing-xl);
  min-height: var(--dt-input-min-height);  /* 钉钉标准 96px */
  max-height: var(--dt-input-max-height);  /* 钉钉标准 300px */
  overflow-y: auto;
}

/* 钉钉风格富文本编辑器: 无边框，无焦点发光 */
.rich-editor {
  width: 100%;
  height: 100%;
  outline: none;
  border: none;
  font-size: var(--dt-font-size-md);
  line-height: 1.7;
  color: var(--dt-text-primary);
  white-space: pre-wrap;
  word-break: break-all;
  caret-color: var(--dt-brand-color);
}

.rich-editor:focus {
  outline: none;
}

.rich-editor:empty:before {
  content: attr(placeholder);
  color: var(--dt-text-tertiary);
  pointer-events: none;
}

/* 底部发送栏 */
.input-footer {
  height: 48px;
  padding: 0 var(--dt-spacing-xl) var(--dt-spacing-md);
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.tip {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
}

.send-btn {
  background-color: var(--dt-send-btn-empty-bg);  /* 钉钉规范：空状态背景 */
  color: var(--dt-text-white);  /* 钉钉规范：空状态文字 */
  border: none;
  width: 64px;  /* 钉钉规范：固定64px宽度 */
  height: 32px;
  border-radius: var(--dt-radius-sm);  /* 钉钉规范：4px 圆角 */
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  cursor: not-allowed;
  transition: background-color 200ms ease-out, color 200ms ease-out;  /* 钉钉规范：200ms ease-out */
}

.send-btn.is-active {
  background-color: var(--dt-brand-color);
  color: var(--dt-text-white);
  cursor: pointer;
  box-shadow: var(--dt-shadow-brand);
}

.send-btn.is-active:hover {
  background-color: var(--dt-brand-hover);
  box-shadow: var(--dt-shadow-brand-strong);
}

.send-btn.is-active:active {
  background-color: var(--dt-brand-active);
}

/* 滚动条美化 */
.input-main::-webkit-scrollbar { width: 4px; }
.input-main::-webkit-scrollbar-thumb { background: var(--dt-scrollbar-thumb-bg); border-radius: var(--dt-radius-sm); }

@media (max-width: 768px) {
  .reply-preview-bar,
  .edit-mode-bar {
    margin-left: 16px;
    margin-right: 16px;
  }

  .toolbar {
    padding: 6px 12px;
    gap: 4px;
  }

  .tool-icon {
    padding: 6px;
    font-size: 18px;
  }

  .image-preview-bar,
  .video-preview-bar {
    padding-left: 16px;
    padding-right: 16px;
  }

  .input-main {
    padding-left: 16px;
    padding-right: 16px;
    min-height: 88px;
  }

  .input-footer {
    padding-left: 16px;
    padding-right: 16px;
  }
}

@media (max-width: 640px) {
  .reply-preview-bar,
  .edit-mode-bar {
    margin-left: 12px;
    margin-right: 12px;
  }

  .reply-content,
  .edit-mode-hint {
    min-width: 0;
  }

  .toolbar {
    padding: 6px 10px;
  }

  .emoji-grid {
    grid-template-columns: repeat(6, 1fr);
  }

  .preview-item {
    width: 64px;
    height: 64px;
  }

  .video-preview-item {
    width: 88px;
    height: 60px;
  }

  .input-main {
    padding-left: 12px;
    padding-right: 12px;
    min-height: 80px;
  }

  .rich-editor {
    font-size: 14px;
    line-height: 1.65;
  }

  .input-footer {
    height: auto;
    gap: 10px;
    padding: 0 12px 12px;
    align-items: center;
  }

  .tip {
    flex: 1;
    line-height: 1.4;
  }

  .send-btn {
    width: 60px;
    height: 30px;
  }

  .recording-bar {
    height: auto;
    gap: 10px;
    padding: 10px 12px;
    flex-wrap: wrap;
  }

  .recording-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
