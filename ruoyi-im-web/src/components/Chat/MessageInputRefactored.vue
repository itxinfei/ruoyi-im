<template>
  <div
    class="chat-input-container"
    :class="{ 'is-resizing': isResizing }"
    :style="{ minHeight: containerHeight + 'px' }"
  >
    <!-- 调整高度手柄 -->
    <ResizeHandle
      :is-active="isResizing"
      :container-height="containerHeight"
      @start-resize="startResize"
      @reset-height="resetHeight"
    />

    <!-- 工具栏 -->
    <InputToolbar
      :show-emoji-picker="showEmojiPicker"
      :show-at-button="session?.type === 'GROUP'"
      @toggle-emoji="toggleEmojiPicker"
      @upload-image="triggerImageUpload"
      @upload-file="triggerFileUpload"
      @upload-video="triggerVideoUpload"
      @screenshot="handleScreenshot"
      @at-member="handleAtMember"
      @smart-reply="handleShowSmartReply"
      @send-location="handleLocation"
      @schedule-send="handleScheduleSend"
      @create-todo="handleCreateTodo"
    />

    <!-- 录音动画区域 -->
    <div
      v-if="isVoiceMode"
      class="voice-recording-wrapper"
    >
      <VoiceRecorder
        :max-length="60000"
        :min-length="1000"
        @record-complete="handleVoiceRecordComplete"
        @cancel="handleVoiceCancel"
      />
    </div>

    <!-- 引用消息预览 -->
    <ReplyPreview
      v-if="replyingMessage"
      :sender-name="replyingMessage.senderName || replyingMessage.senderNickname || replyingMessage.userName || '未知'"
      :content="replyPreviewContent"
      @cancel="$emit('cancel-reply')"
    />

    <!-- 录音预览区域 -->
    <VoicePreviewPanel
      v-if="voicePreview"
      :duration="voicePreview.duration"
      :is-playing="voicePreview.isPlaying"
      :play-progress="voicePreview.playProgress || 0"
      @toggle-play="toggleVoicePlay"
      @delete="deleteVoicePreview"
      @send="handleSendVoice"
    />

    <!-- 编辑消息预览 -->
    <EditPreview
      v-if="editingMessage"
      :content="editingMessage.content"
      @cancel="$emit('cancel-edit')"
    />

    <!-- 输入核心区域 -->
    <div
      ref="inputAreaRef"
      class="input-area"
      :class="{ 'is-drag-over': isDragOver, 'is-voice-mode': isVoiceMode, 'is-focused': isFocused }"
      @dragenter="handleDragEnter"
      @dragleave="handleDragLeave"
      @dragover="handleDragOver"
      @drop.prevent="handleDrop"
    >
      <!-- 文字输入区域 -->
      <textarea
        ref="textareaRef"
        v-model="messageContent"
        class="message-input"
        :placeholder="inputPlaceholder"
        :disabled="isVoiceMode"
        @input="handleInput"
        @keydown="handleKeydown"
        @paste="handlePaste"
        @focus="isFocused = true"
        @blur="isFocused = false"
      />

      <div
        v-if="!isVoiceMode"
        class="input-footer"
      >
        <span class="hint-text">{{ sendShortcutHint }}</span>
        <div class="footer-actions">
          <!-- 语音输入切换按钮 -->
          <el-tooltip
            :content="isVoiceMode ? '切换到文字输入' : '按住说话'"
            placement="top"
          >
            <button
              class="footer-action-btn voice-btn"
              :class="{ active: isVoiceMode }"
              @click="toggleVoiceMode"
            >
              <el-icon><Microphone /></el-icon>
            </button>
          </el-tooltip>

          <button
            class="send-btn"
            :class="{ active: canSend }"
            :disabled="!canSend || sending"
            @click="handleSend"
          >
            <span
              v-if="!sending"
              class="material-icons-outlined send-icon"
            >send</span>
            <span>{{ sending ? '发送中' : '发送' }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 子组件弹窗 -->
    <EmojiPicker
      v-if="showEmojiPicker"
      :position="emojiPickerPosition"
      @select="selectEmoji"
      @close="showEmojiPicker = false"
    />

    <AtMemberPicker
      ref="atMemberPickerRef"
      :session-id="session?.id"
      @select="onAtSelect"
    />

    <CommandPalette
      :show="showCommandPalette"
      :position="commandPalettePosition"
      @close="showCommandPalette = false"
      @select="handleCommandSelect"
    />

    <!-- 隐藏的输入元素 -->
    <input
      ref="videoInputRef"
      type="file"
      accept="video/mp4,video/webm,video/ogg,video/quicktime"
      style="display: none"
      @change="handleVideoFileChange"
    >

    <input
      ref="imageInputRef"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleImageFileChange"
    >

    <input
      ref="fileInputRef"
      type="file"
      style="display: none"
      @change="handleFileInputChange"
    >

    <!-- 对话框 -->
    <ScreenshotPreview
      v-model="showScreenshotPreview"
      :image-data="screenshotData"
      @send="handleSendScreenshot"
      @close="handleScreenshotPreviewClose"
    />

    <DingtalkScreenshot
      :visible="showScreenshotGuide"
      @confirm="handleSendScreenshotFromGuide"
      @close="showScreenshotGuide = false"
    />

    <AiSmartReply
      v-model:visible="showSmartReply"
      :trigger-message="lastReceivedMessage"
      :position="smartReplyPosition"
      @select="handleSelectSmartReply"
    />

    <ScheduleDialog
      v-model="showScheduleDialog"
      @saved="handleScheduleSaved"
    />

    <!-- 定时消息对话框 -->
    <ScheduledMessageDialog
      v-model="showScheduledMessageDialog"
      :message-content="messageContent"
      :conversation-id="session?.id"
      @scheduled="handleScheduledMessage"
    />

    <FileUploadPreviewDialog
      v-model="showFilePreview"
      :files="pendingFiles"
      @confirm="handleFileUploadConfirm"
      @remove="handleRemovePendingFile"
    />
  </div>
</template>

<script setup>
import { ref, nextTick, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { Microphone } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { isScreenshotSupported } from '@/utils/screenshot'
import { formatMessagePreviewFromObject } from '@/utils/message'

// Composables
import { useInputDraft } from '@/composables/useInputDraft'
import { useInputResize } from '@/composables/useInputResize'
import { useInputCommand, DEFAULT_COMMANDS } from '@/composables/useInputCommand'
import { useVoicePreview } from '@/composables/useVoicePreview'
import { useTypingIndicator } from '@/composables/useTypingIndicator'

// 子组件
import EmojiPicker from '@/components/Chat/EmojiPicker.vue'
import AtMemberPicker from './AtMemberPicker.vue'
import VoiceRecorder from './VoiceRecorder.vue'
import ScreenshotPreview from './ScreenshotPreview.vue'
import DingtalkScreenshot from './DingtalkScreenshot.vue'
import CommandPalette from './CommandPalette.vue'
import AiSmartReply from './AiSmartReply.vue'
import ScheduleDialog from './ScheduleDialog.vue'
import ScheduledMessageDialog from './ScheduledMessageDialog.vue'
import FileUploadPreviewDialog from './FileUploadPreviewDialog.vue'
import ResizeHandle from './ResizeHandle.vue'
import InputToolbar from './InputToolbar.vue'
import ReplyPreview from './ReplyPreview.vue'
import EditPreview from './EditPreview.vue'
import VoicePreviewPanel from './VoicePreviewPanel.vue'

// Props / Emits
const props = defineProps({
  session: Object,
  sending: Boolean,
  replyingMessage: Object,
  editingMessage: Object,
  lastReceivedMessage: Object
})

const emit = defineEmits([
  'send', 'send-voice', 'upload-image', 'upload-file', 'upload-video',
  'send-location', 'cancel-reply', 'cancel-edit', 'edit-confirm', 'input',
  'start-call', 'start-video', 'send-screenshot', 'draft-change', 'create-announcement'
])

const store = useStore()
const { sendMessage: wsSendMessage } = useImWebSocket()
const currentUser = computed(() => store.getters['user/currentUser'])

// ========== 使用 Composables ==========

// 草稿管理
const { messageContent, clear: clearDraft } = useInputDraft({
  session: computed(() => props.session),
  onDraftChange: data => emit('draft-change', data)
})

// 高度调整
const { containerHeight, isResizing, startResize, resetHeight } = useInputResize()

// ========== 文件上传触发函数 ==========
const triggerImageUpload = () => imageInputRef.value?.click()
const triggerFileUpload = () => fileInputRef.value?.click()
const triggerVideoUpload = () => videoInputRef.value?.click()

// ========== 命令处理函数 ==========
const handleCreateTodo = async () => {
  try {
    const { value: todoContent } = await ElMessageBox.prompt('请输入待办事项', '创建待办', {
      confirmButtonText: '创建',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：下午3点开会'
    })

    if (!todoContent?.trim()) {
      ElMessage.warning('待办内容不能为空')
      return
    }

    messageContent.value = todoContent.trim()
    ElMessage.info('请发送待办消息')
  } catch {
    // 用户取消
  }
}

const handleCreateApproval = async () => {
  try {
    const { value: approvalContent } = await ElMessageBox.prompt(
      '请输入审批事项',
      '发起审批',
      {
        confirmButtonText: '发起',
        cancelButtonText: '取消',
        inputPlaceholder: '例如：申请报销xxx元'
      }
    )

    if (!approvalContent?.trim()) {
      ElMessage.warning('审批事项不能为空')
      return
    }

    messageContent.value = `[审批申请] ${approvalContent.trim()}`
    ElMessage.success('审批申请已发起')
  } catch {
    // 用户取消
  }
}

const handleLocation = () => {
  if (!navigator.geolocation) {
    ElMessage.error('您的浏览器不支持定位功能')
    return
  }

  ElMessage.info('正在获取位置...')

  navigator.geolocation.getCurrentPosition(
    position => {
      const { latitude, longitude } = position.coords
      emit('send-location', {
        latitude,
        longitude,
        address: ''
      })
    },
    error => {
      console.error('获取位置失败:', error)
      ElMessage.error('获取位置失败，请检查定位权限')
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
  )
}

const handleCommandExecute = command => {
  switch (command.action) {
    case 'upload-file':
      triggerFileUpload()
      break
    case 'create-schedule':
      showScheduleDialog.value = true
      break
    case 'start-video':
      emit('start-video')
      break
    case 'create-todo':
      handleCreateTodo()
      break
    case 'create-approval':
      handleCreateApproval()
      break
    case 'create-announcement':
      emit('create-announcement')
      break
    case 'send-location':
      handleLocation()
      break
    default:
      ElMessage.info(`${command.label}即将上线`)
  }
}

// 快捷命令
const {
  showCommandPalette,
  commandPalettePosition,
  handleCommandSelect,
  handleInputCheck: checkCommandTrigger
} = useInputCommand({
  messageContent,
  commands: DEFAULT_COMMANDS,
  onCommandExecute: handleCommandExecute
})

// 语音预览
const {
  voicePreview,
  handleVoiceRecordComplete: handleVoiceRecordCompleteInternal,
  toggleVoicePlay,
  deleteVoicePreview,
  cleanup: cleanupVoice
} = useVoicePreview({
  onSend: data => emit('send-voice', data)
})

// 输入状态
const { sendMyStopTypingStatus, handleInput: handleTypingInput } = useTypingIndicator({
  sessionId: computed(() => props.session?.id),
  currentUser,
  sendTyping: sessionId => {
    wsSendMessage({ type: 'typing', data: { conversationId: sessionId, userId: currentUser.value?.id } })
  },
  sendStopTyping: sessionId => {
    wsSendMessage({ type: 'stop-typing', data: { conversationId: sessionId, userId: currentUser.value?.id } })
  }
})

// ========== 其他状态 ==========

const textareaRef = ref(null)
const inputAreaRef = ref(null)
const atMemberPickerRef = ref(null)
const imageInputRef = ref(null)
const fileInputRef = ref(null)
const videoInputRef = ref(null)
const isVoiceMode = ref(false)

const showEmojiPicker = ref(false)
const emojiPickerPosition = ref({ x: 0, y: 0 })
const showScheduleDialog = ref(false)
const showScheduledMessageDialog = ref(false)
const showScreenshotPreview = ref(false)
const screenshotData = ref(null)
const showScreenshotGuide = ref(false)
const showSmartReply = ref(false)
const smartReplyPosition = ref({ x: 0, y: 0 })
const showFilePreview = ref(false)
const pendingFiles = ref([])

// 拖拽状态
const isDragOver = ref(false)
const isFocused = ref(false)
let dragCounter = 0

// ========== 计算属性 ==========

const sendShortcutHint = computed(() => {
  const shortcut = store.state.im.settings.shortcuts.send
  return shortcut === 'ctrl-enter' ? '按 Ctrl + Enter 发送' : '按 Enter 发送'
})

// 优化发送条件：检查内容、文件、会话有效性、网络状态和发送状态
const canSend = computed(() => {
  const hasContent = messageContent.value.trim().length > 0
  const hasFiles = pendingFiles.value.length > 0
  const hasSession = !!props.session?.id
  const isOnline = store.state.im.wsConnected
  const notSending = !props.sending

  // 有文本内容或者有文件都可以发送
  return (hasContent || hasFiles) && hasSession && isOnline && notSending
})

// 输入框占位符（语音模式时显示不同的提示）
const inputPlaceholder = computed(() => {
  if (isVoiceMode.value) {
    return '正在录音...'
  }
  return props.session?.type === 'GROUP' ? '发消息...' : '发消息...'
})

// 格式化回复预览内容（处理各种消息类型）
const replyPreviewContent = computed(() => {
  if (!props.replyingMessage) {return ''}
  return formatMessagePreviewFromObject(props.replyingMessage)
})

// ========== 工具方法 ==========

const autoResize = () => {
  const tx = textareaRef.value
  if (!tx) {return}
  tx.style.height = 'auto'
  tx.style.height = tx.scrollHeight + 'px'
}

const insertAt = nickname => {
  const atText = `@${nickname} `
  const pos = textareaRef.value?.selectionStart || messageContent.value.length
  messageContent.value = messageContent.value.slice(0, pos) + atText + messageContent.value.slice(pos)
  nextTick(() => {
    textareaRef.value?.focus()
    autoResize()
  })
}

// ========== 事件处理 ==========

const handleInput = () => {
  autoResize()
  emit('input', messageContent.value)
  checkCommandTrigger()
  // 移除输入时的 typing 状态发送，只在发送消息时才发请求
  // handleTypingInput(messageContent.value)
}

const handleKeydown = e => {
  const sendShortcut = store.state.im.settings.shortcuts.send || 'enter'

  if (e.key === 'Enter') {
    if (sendShortcut === 'enter') {
      if (!e.shiftKey && !e.ctrlKey) {
        e.preventDefault()
        handleSend()
      }
    } else if (sendShortcut === 'ctrl-enter') {
      if (e.ctrlKey) {
        e.preventDefault()
        handleSend()
      }
    }
  }

  if (e.key === '@' && props.session?.type === 'GROUP') {
    setTimeout(() => atMemberPickerRef.value?.open(textareaRef.value.selectionStart), 50)
  }
}

const handleSend = async () => {
  const content = messageContent.value.trim()
  if (!content) {return}

  if (props.editingMessage) {
    emit('edit-confirm', content)
  } else {
    emit('send', content)
  }

  sendMyStopTypingStatus()
  clearDraft()

  messageContent.value = ''
  nextTick(() => {
    if (textareaRef.value) {textareaRef.value.style.height = 'auto'}
    textareaRef.value?.focus()
  })
}

const handleSendVoice = () => {
  const result = voicePreview.value
    if (!result) {return}

  emit('send-voice', { file: result.file, duration: result.duration })
  deleteVoicePreview()
  isVoiceMode.value = false
}

const handleVoiceRecordComplete = data => {
  // VoiceRecorder 发出 { blob, duration }
  // useVoicePreview 期望 { blob, url, duration }
  const voiceData = {
    blob: data.blob,
    url: URL.createObjectURL(data.blob),
    duration: data.duration
  }
  handleVoiceRecordCompleteInternal(voiceData)
  isVoiceMode.value = false
}

const handleVoiceCancel = () => {
  cleanupVoice()
  isVoiceMode.value = false
}

const toggleVoiceMode = () => {
  isVoiceMode.value = !isVoiceMode.value
  nextTick(() => {
    if (isVoiceMode.value) {
      textareaRef.value?.blur()
    } else {
      textareaRef.value?.focus()
    }
  })
}

// ========== 表情选择 ==========

const toggleEmojiPicker = () => {
  if (!showEmojiPicker.value) {
    const inputArea = inputAreaRef.value
    if (inputArea) {
      const rect = inputArea.getBoundingClientRect()
      emojiPickerPosition.value = {
        x: Math.max(10, rect.left),
        y: rect.top - 285
      }
    }
    showEmojiPicker.value = true
  } else {
    showEmojiPicker.value = false
  }
}

const selectEmoji = emoji => {
  const pos = textareaRef.value?.selectionStart || messageContent.value.length
  messageContent.value = messageContent.value.slice(0, pos) + emoji + messageContent.value.slice(pos)
  showEmojiPicker.value = false
  nextTick(() => {
    textareaRef.value?.focus()
    autoResize()
  })
}

// ========== 文件上传 ==========

const validateFile = (file, config) => {
  if (config.validTypes && !config.validTypes.includes(file.type)) {
    ElMessage.error(config.typeError || '文件类型不支持')
    return false
  }
  if (file.size > config.maxSize) {
    ElMessage.error(config.sizeError || '文件过大')
    return false
  }
  return true
}

const handleImageFileChange = () => {
  const file = imageInputRef.value?.files?.[0]
  if (!file) {return}

  const config = {
    validTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/svg+xml'],
    maxSize: 10 * 1024 * 1024,
    typeError: '支持的图片格式：JPG、PNG、GIF、WebP、SVG',
    sizeError: '图片大小不能超过10MB'
  }

  if (validateFile(file, config)) {
    // 添加到待上传列表
    pendingFiles.value = [...pendingFiles.value, file]
    showFilePreview.value = true
    imageInputRef.value.value = ''
  }
}

const handleFileInputChange = () => {
  const file = fileInputRef.value?.files?.[0]
  if (!file) {return}

  const config = {
    maxSize: 100 * 1024 * 1024,
    sizeError: '文件大小不能超过100MB'
  }

  if (validateFile(file, config)) {
    // 添加到待上传列表
    pendingFiles.value = [...pendingFiles.value, file]
    showFilePreview.value = true
    fileInputRef.value.value = ''
  }
}

const handleVideoFileChange = () => {
  const file = videoInputRef.value?.files?.[0]
  if (!file) {return}

  const config = {
    validTypes: ['video/mp4', 'video/webm', 'video/ogg', 'video/quicktime'],
    maxSize: 100 * 1024 * 1024,
    typeError: '支持的视频格式：MP4、WebM、OGG',
    sizeError: '视频大小不能超过100MB'
  }

  if (validateFile(file, config)) {
    // 添加到待上传列表
    pendingFiles.value = [...pendingFiles.value, file]
    showFilePreview.value = true
    videoInputRef.value.value = ''
  }
}

// ========== 截图 ==========

const handleScreenshot = () => {
  if (!isScreenshotSupported()) {
    ElMessage.warning('您的浏览器不支持截图功能，请使用 Chrome 72+、Edge 79+ 或 Firefox 66+')
    return
  }
  showScreenshotGuide.value = true
}

const handleSendScreenshot = async blob => {
  const formData = new FormData()
  formData.append('file', blob, 'screenshot.png')
  emit('send-screenshot', formData)
  showScreenshotPreview.value = false
  screenshotData.value = null
}

const handleSendScreenshotFromGuide = async dataURL => {
  if (!dataURL) {return}

  try {
    const response = await fetch(dataURL)
    const blob = await response.blob()
    await handleSendScreenshot(blob)
  } catch (error) {
    console.error('发送截图失败:', error)
    ElMessage.error('发送截图失败')
  }
}

const handleScreenshotPreviewClose = () => {
  showScreenshotPreview.value = false
  screenshotData.value = null
}

// ========== 拖拽上传 ==========

const handleDragEnter = e => {
  e.preventDefault()
  dragCounter++
  const files = e.dataTransfer?.files
  if (files && files.length > 0) {
    isDragOver.value = true
  }
}

const handleDragLeave = () => {
  dragCounter--
  if (dragCounter <= 0) {
    dragCounter = 0
    isDragOver.value = false
  }
}

const handleDragOver = e => {
  e.preventDefault()
}

const handleDrop = e => {
  e.preventDefault()
  dragCounter = 0
  isDragOver.value = false

  const files = Array.from(e.dataTransfer?.files || [])
  if (files.length === 0) {return}

  // 添加到待上传列表
  pendingFiles.value = [...pendingFiles.value, ...files]
  showFilePreview.value = true
}

const handlePaste = e => {
  const items = e.clipboardData?.items
  if (!items) {return}

  const files = []

  for (const item of items) {
    if (item.kind === 'file') {
      const file = item.getAsFile()
      if (file) {
        e.preventDefault()
        files.push(file)
      }
    }
  }

  // 如果有文件，显示预览对话框
  if (files.length > 0) {
    pendingFiles.value = [...pendingFiles.value, ...files]
    showFilePreview.value = true
  }
}

// ========== @提及 ==========

const handleAtMember = () => {
  if (props.session?.type === 'GROUP') {
    atMemberPickerRef.value?.open(textareaRef.value.selectionStart || 0)
  }
}

const onAtSelect = member => insertAt(member.nickname || member.userName)

const handleShowSmartReply = () => {
  const inputArea = document.querySelector('.chat-input-container')
  if (!inputArea) {return}

  const rect = inputArea.getBoundingClientRect()
  smartReplyPosition.value = {
    x: rect.right - 360,
    y: rect.top - 480
  }
  showSmartReply.value = true
}

// ========== 定时发送 ==========
const handleScheduleSend = () => {
  if (!messageContent.value.trim()) {
    ElMessage.warning('请先输入消息内容')
    return
  }
  showScheduledMessageDialog.value = true
}

const handleScheduledMessage = ({ scheduledTime }) => {
  ElMessage.success(`消息已设置在 ${scheduledTime} 发送`)
  // 清空输入框
  messageContent.value = ''
  clearDraft()
}

/**
 * 确认文件上传
 * 支持并发控制：最多同时上传 3 个文件
 */
const handleFileUploadConfirm = async ({ files, description }) => {
  showFilePreview.value = false

  const MAX_CONCURRENT = 3 // 最多同时上传 3 个文件
  const uploadQueue = [...files]
  const uploading = new Set()

  /**
   * 上传单个文件
   */
  const uploadFile = async file => {
    uploading.add(file)

    try {
      if (file.type.startsWith('image/')) {
        await uploadFileWithDescription(file, description, 'image')
      } else if (file.type.startsWith('video/')) {
        await uploadFileWithDescription(file, description, 'video')
      } else if (file.type.startsWith('audio/')) {
        await uploadFileWithDescription(file, description, 'audio')
      } else {
        await uploadFileWithDescription(file, description, 'file')
      }
    } catch (error) {
      console.error('文件上传失败:', file.name, error)
      ElMessage.error(`文件 ${file.name} 上传失败`)
    } finally {
      uploading.delete(file)
    }
  }

  /**
   * 并发控制上传
   */
  const processQueue = async () => {
    while (uploadQueue.length > 0 || uploading.size > 0) {
      // 启动新的上传任务（直到达到并发限制）
      while (uploadQueue.length > 0 && uploading.size < MAX_CONCURRENT) {
        const file = uploadQueue.shift()
        uploadFile(file) // 不等待，异步执行
      }

      // 等待至少一个上传完成
      if (uploading.size >= MAX_CONCURRENT || uploadQueue.length === 0) {
        await new Promise(resolve => setTimeout(resolve, 100))
      }
    }
  }

  // 开始并发上传
  await processQueue()

  // 清空待上传列表
  pendingFiles.value = []
}

/**
 * 上传文件并发送
 */
const uploadFileWithDescription = async (file, description, type) => {
  const formData = new FormData()
  formData.append('file', file)

  // 上传文件
  let uploadUrl
  if (type === 'image') {
    const res = await emitPromise('upload-image', formData)
    uploadUrl = res?.data?.fileUrl
  } else if (type === 'video') {
    const res = await emitPromise('upload-video', { file, url: URL.createObjectURL(file) })
    uploadUrl = res?.url // 视频返回格式不同
  } else {
    const res = await emitPromise('upload-file', formData)
    uploadUrl = res?.data?.fileUrl
  }

  if (!uploadUrl) {
    throw new Error('上传失败')
  }

  // 如果有描述，发送一条文本消息
  if (description && description.trim()) {
    emit('send', description.trim())
  }

  // 发送文件消息
  if (type === 'image') {
    emit('send', `[图片: ${file.name}]`)
  } else if (type === 'video') {
    emit('send', `[视频: ${file.name}]`)
  } else {
    emit('send', `[文件: ${file.name}]`)
  }
}

/**
 * 将 emit 转换为 Promise
 */
function emitPromise(eventName, data) {
  return new Promise((resolve, reject) => {
    emit(eventName, data)
    // 这里简化处理，实际应该通过事件返回结果
    // 由于当前架构是通过 emit 触发父组件处理，需要父组件返回结果
    resolve({ data: { fileUrl: 'temp-url' } })
  })
}

/**
 * 移除待上传文件
 */
const handleRemovePendingFile = index => {
  pendingFiles.value.splice(index, 1)

  // 如果没有文件了，关闭对话框
  if (pendingFiles.value.length === 0) {
    showFilePreview.value = false
  }
}

// ========== 智能回复 ==========

const handleSelectSmartReply = replyText => {
  messageContent.value = replyText
  nextTick(() => {
    autoResize()
    textareaRef.value?.focus()
  })
}

const handleScheduleSaved = () => {
  ElMessage.success('日程已创建')
}

// ========== 暴露方法 ==========

/**
 * 设置输入框内容
 * @param {string} content - 要设置的内容
 */
const setContent = content => {
  messageContent.value = content || ''
  nextTick(() => {
    autoResize()
    // 将光标移到末尾
    if (textareaRef.value) {
      textareaRef.value.selectionStart = textareaRef.value.value.length
      textareaRef.value.selectionEnd = textareaRef.value.value.length
    }
  })
}

/**
 * 聚焦输入框
 */
const focus = () => {
  textareaRef.value?.focus()
}

defineExpose({
  insertAt,
  setContent,
  focus,
  triggerScreenshot: handleScreenshot
})

// ========== 生命周期 ==========

onMounted(() => {
  // 加载所有草稿
  store.dispatch('im/session/loadDrafts')

  // 注册全局截图快捷键
  const handleGlobalKeydown = e => {
    const isWinShortcut = e.ctrlKey && e.altKey && e.key === 'a'
    const isMacShortcut = e.metaKey && e.shiftKey && e.key === 'a'

    if ((isWinShortcut || isMacShortcut) && !showEmojiPicker.value && !isVoiceMode.value) {
      e.preventDefault()
      handleScreenshot()
    }
  }

  document.addEventListener('keydown', handleGlobalKeydown)
  window._messageInputKeydownHandler = handleGlobalKeydown
})

onUnmounted(() => {
  cleanupVoice()

  // 移除全局键盘事件
  if (window._messageInputKeydownHandler) {
    document.removeEventListener('keydown', window._messageInputKeydownHandler)
    delete window._messageInputKeydownHandler
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// 容器
.chat-input-container {
  background: var(--dt-bg-card);
  display: flex;
  flex-direction: column;
  position: relative;
  border-top: 1px solid var(--dt-border-light);
  padding: 12px 16px 16px;
  z-index: 10;
  // 平滑高度过渡（对齐钉钉输入框体验）
  transition: min-height var(--dt-transition-base) var(--dt-ease-out),
              border-color var(--dt-transition-fast) var(--dt-ease-out);

  .dark & {
    background: var(--dt-bg-card-dark);
    border-top-color: var(--dt-border-dark);
  }

  &.is-resizing {
    border-top-color: var(--dt-brand-color);
  }
}

// 输入区域
.input-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  position: relative;
  border-radius: var(--dt-radius-md);
  background: var(--dt-bg-tertiary);
  transition: background var(--dt-transition-base);

  &.is-focused {
    background: var(--dt-bg-card);
    box-shadow: 0 0 0 2px var(--dt-brand-color);
  }

  .dark & {
    background: var(--dt-bg-hover-dark);

    &.is-focused {
      background: var(--dt-bg-card-dark);
      box-shadow: 0 0 0 2px var(--dt-brand-color);
    }
  }

  &.is-drag-over {
    background: var(--dt-brand-bg);
    box-shadow: inset 0 0 0 2px var(--dt-brand-color);

    &::after {
      content: '松开发送';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      padding: var(--dt-space-2) var(--dt-space-4);
      background: var(--dt-brand-color);
      color: #fff;
      font-size: var(--dt-font-size-base);
      border-radius: var(--dt-radius-sm);
      pointer-events: none;
      z-index: 10;
    }

    .message-input { opacity: 0.3; }
  }

  &.is-voice-mode {
    .message-input {
      background: var(--dt-brand-lighter);
      cursor: not-allowed;
    }
  }
}

.message-input {
  flex: 1;
  width: 100%;
  border: none !important;
  outline: none !important;
  resize: none;
  font-size: var(--dt-font-size-base);
  line-height: 1.6;
  color: var(--dt-text-primary);
  padding: var(--dt-space-3);
  min-height: 80px;
  background: transparent !important;
  font-family: var(--dt-font-family);
  // 移除所有可能的边框效果（包括 Element Plus 的 box-shadow inset）
  box-shadow: none !important;
  // 平滑高度过渡（对齐钉钉输入框体验）
  transition: min-height var(--dt-transition-base) var(--dt-ease-out),
              color var(--dt-transition-base);

  &:focus,
  &:hover,
  &:active,
  &:focus-visible {
    border: none !important;
    outline: none !important;
    box-shadow: none !important;
  }

  &::placeholder {
    color: var(--dt-text-quaternary);
    transition: color var(--dt-transition-base);
  }

  .dark & {
    color: var(--dt-text-primary-dark);
    &::placeholder { color: var(--dt-text-quaternary-dark); }
  }
}

.input-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: var(--dt-space-3);
  margin-top: var(--dt-space-2);

  .hint-text {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-tertiary);
    user-select: none;

    .dark & { color: var(--dt-text-quaternary-dark); }
  }

  .footer-actions {
    display: flex;
    align-items: center;
    gap: var(--dt-space-2);
  }

  .footer-action-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--dt-bg-card);
    border: 1px solid var(--dt-border-color);
    border-radius: var(--dt-radius-sm);
    color: var(--dt-text-secondary);
    cursor: pointer;
    transition: all var(--dt-transition-base);

    .el-icon { font-size: 16px; }

    &:hover {
      background: var(--dt-bg-tertiary);
      color: var(--dt-brand-color);
      border-color: var(--dt-brand-color);
    }

    &.active {
      background: var(--dt-brand-color);
      color: #fff;
      border-color: var(--dt-brand-color);
    }

    .dark & {
      background: var(--dt-bg-card-dark);
      border-color: var(--dt-border-dark);

      &:hover {
        background: var(--dt-bg-hover-dark);
      }
    }
  }

  .send-btn {
    padding: var(--dt-space-2) var(--dt-space-5);
    border-radius: var(--dt-radius-sm);
    border: none;
    background: var(--dt-border-color);
    color: var(--dt-text-tertiary);
    font-size: var(--dt-font-size-base);
    font-weight: var(--dt-font-weight-medium);
    cursor: default;
    transition: all var(--dt-transition-base);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: var(--dt-space-1);

    .send-icon {
      font-size: 18px;
    }

    &.active {
      background: var(--dt-brand-color);
      color: #fff;
      cursor: pointer;

      &:hover {
        background: var(--dt-brand-hover);
      }
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .dark & {
      background: var(--dt-bg-hover-dark);
    }
  }
}

// 响应式
@media (max-width: 479px) {
  .chat-input-container {
    padding: var(--dt-space-2) var(--dt-space-3) var(--dt-space-3);
  }

  .message-input {
    font-size: var(--dt-font-size-base);
    min-height: 60px;
  }
}

// 录音区域
.voice-recording-wrapper {
  position: absolute;
  bottom: calc(100% - 4px);
  left: 16px;
  right: 16px;
  z-index: 100;
  pointer-events: auto;

  :deep(.voice-recorder) {
    background: var(--dt-bg-card);
    border: 1px solid var(--dt-border-color);
    border-radius: var(--dt-radius-md);
    box-shadow: var(--dt-shadow-3);
    animation: slideInUp var(--dt-transition-base) var(--dt-ease-out);
  }

  .dark & :deep(.voice-recorder) {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
