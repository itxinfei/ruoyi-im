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

    <!-- 顶部工具栏 (钉钉风格：轻量化图标) -->
    <div class="input-toolbar-outer">
      <InputToolbar
        :show-emoji-picker="showEmojiPicker"
        :show-at-button="session?.type === 'GROUP'"
        @toggle-emoji="toggleEmojiPicker"
        @upload-image="triggerImageUpload"
        @upload-file="triggerFileUpload"
        @screenshot="handleScreenshot"
        @at-member="handleAtMember"
        @smart-reply="handleShowSmartReply"
      />
    </div>

    <!-- 输入核心区域 -->
    <div
      ref="inputAreaRef"
      class="input-main-area"
      :class="{ 'is-drag-over': isDragOver, 'is-focused': isFocused }"
      @dragenter="handleDragEnter"
      @dragleave="handleDragLeave"
      @dragover="handleDragOver"
      @drop.prevent="handleDrop"
    >
      <!-- 引用/编辑预览逻辑保持在文本框上方 -->
      <div class="previews-container">
        <CommonPreview
          v-if="replyingMessage"
          type="reply"
          :sender-name="replyingMessage.senderName || '对方'"
          :content="replyPreviewContent"
          :show-preview="true"
          @cancel="$emit('cancel-reply')"
        />
        <CommonPreview
          v-if="editingMessage"
          type="edit"
          title="正在编辑消息"
          :content="editingMessage.content"
          :show-preview="true"
          @cancel="$emit('cancel-edit')"
        />
      </div>

      <textarea
        ref="textareaRef"
        v-model="messageContent"
        class="real-textarea"
        :placeholder="inputPlaceholder"
        @input="handleInput"
        @keydown="handleKeydown"
        @focus="isFocused = true"
        @blur="isFocused = false"
      />

      <div class="input-bottom-actions">
        <div class="action-hints">
          <span class="hint-text">Enter 发送，Ctrl+Enter 换行</span>
        </div>

        <!-- 钉钉风格的发送按钮，包含表情按钮 -->
        <div class="send-controls">
          <button
            class="send-btn-v2"
            :disabled="!canSend"
            @click="handleSend"
          >
            <span class="material-icons-outlined">send</span>
            <span class="btn-text">发送</span>
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
      :feature-enabled="isScheduledMessageEnabled"
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
import { Microphone, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { isScreenshotSupported } from '@/utils/screenshot'
import { formatMessagePreviewFromObject } from '@/utils/message'
import { extractUrls, isInternalUrl, parseLink } from '@/utils/linkParser'
import { uploadImage, uploadFile as uploadFileApi } from '@/api/im/file'
import { isFeatureEnabled, FeatureFlags } from '@/config/featureFlags'

// Composables
import { useInputDraft } from '@/composables/useInputDraft'
import { useInputResize } from '@/composables/useInputResize'
import { useInputCommand, DEFAULT_COMMANDS } from '@/composables/useInputCommand'
import { useVoicePreview } from '@/composables/useVoicePreview'
import { useTypingIndicator } from '@/composables/useTypingIndicator'

// 子组件
import EmojiPicker from '@/components/Chat/EmojiPicker.vue'
import LinkCard from '@/components/Chat/LinkCard.vue'
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
import CommonPreview from '@/components/Common/CommonPreview.vue'
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
  'send', 'send-voice', 'upload-image', 'upload-file',
  'cancel-reply', 'cancel-edit', 'edit-confirm', 'input',
  'start-call', 'start-video', 'send-screenshot', 'draft-change', 'create-announcement',
  'send-location'
])

const store = useStore()
const { sendMessage: wsSendMessage } = useImWebSocket()
const currentUser = computed(() => store.getters['user/currentUser'])

const canSend = computed(() => {
  if (props.sending) { return false }
  return Boolean(messageContent.value?.trim())
})

// 功能开关
const isScheduledMessageEnabled = computed(() =>
  isFeatureEnabled(FeatureFlags.SCHEDULED_MESSAGE)
)

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

// 链接预览相关
const linkPreview = ref(null)
const linkPreviewLoading = ref(false)
const linkPreviewError = ref(false)
const linkPreviewDebounceTimer = ref(null)
const lastParsedUrls = ref([])

// 拖拽状态
const isDragOver = ref(false)
const isFocused = ref(false)
const isUnmounted = ref(false) // 标记组件是否已卸载
let dragCounter = 0

// ========== 计算属性 ==========

// 输入框占位符（语音模式时显示不同的提示）
const inputPlaceholder = computed(() => {
  if (isVoiceMode.value) {
    return '正在录音...'
  }
  return props.session?.type === 'GROUP' ? '发消息... (输入 / 查看快捷命令)' : '发消息... (输入 / 查看快捷命令)'
})

// 格式化回复预览内容（处理各种消息类型）
const replyPreviewContent = computed(() => {
  if (!props.replyingMessage) { return '' }
  try {
    return formatMessagePreviewFromObject(props.replyingMessage) || '...'
  } catch (error) {
    console.error('格式化回复预览失败:', error)
    return '...'
  }
})

// ========== 工具方法 ==========

// 输入框高度限制常量
const MIN_ROWS = 3 // 最小行数
const MAX_ROWS = 8 // 最大行数
const LINE_HEIGHT = 24 // 行高(px),与CSS中的line-height: 1.6 * font-size: 15px ≈ 24px对应

const autoResize = () => {
  const tx = textareaRef.value
  if (!tx) { return }

  // 重置高度以获取真实的scrollHeight
  tx.style.height = 'auto'

  // 计算最小和最大高度
  const minHeight = MIN_ROWS * LINE_HEIGHT
  const maxHeight = MAX_ROWS * LINE_HEIGHT

  // 获取内容实际高度
  const scrollHeight = tx.scrollHeight

  // 应用高度限制
  if (scrollHeight <= minHeight) {
    tx.style.height = minHeight + 'px'
  } else if (scrollHeight >= maxHeight) {
    tx.style.height = maxHeight + 'px'
    tx.style.overflowY = 'auto' // 超过最大高度时显示滚动条
  } else {
    tx.style.height = scrollHeight + 'px'
    tx.style.overflowY = 'hidden' // 未超过时隐藏滚动条
  }
}

const insertAt = nickname => {
  const atText = `@${nickname} `
  const pos = textareaRef.value?.selectionStart || messageContent.value.length
  messageContent.value = messageContent.value.slice(0, pos) + atText + messageContent.value.slice(pos)
  nextTick(() => {
    if (isUnmounted.value) { return }
    textareaRef.value?.focus()
    autoResize()
  })
}

// ========== 事件处理 ==========

const handleInput = () => {
  autoResize()
  emit('input', messageContent.value)
  checkCommandTrigger()
  // 检测链接并显示预览
  detectAndPreviewLinks()
  // 移除输入时的 typing 状态发送，只在发送消息时才发请求
  // handleTypingInput(messageContent.value)
}

// 检测并预览链接
const detectAndPreviewLinks = () => {
  // 清除之前的定时器
  if (linkPreviewDebounceTimer.value) {
    clearTimeout(linkPreviewDebounceTimer.value)
  }

  // 延迟500ms后检测
  linkPreviewDebounceTimer.value = setTimeout(() => {
    const urls = extractUrls(messageContent.value, 3)

    // 如果没有URL，清除预览
    if (urls.length === 0) {
      clearLinkPreview()
      return
    }

    // 如果URL没有变化，不重复解析
    if (JSON.stringify(urls) === JSON.stringify(lastParsedUrls.value)) {
      return
    }

    lastParsedUrls.value = urls

    // 只解析第一个URL
    const firstUrl = urls[0]
    if (!isInternalUrl(firstUrl)) {
      parseLinkPreview(firstUrl)
    }
  }, 500)
}

// 解析链接预览
const parseLinkPreview = async url => {
  linkPreviewLoading.value = true
  linkPreviewError.value = false

  try {
    const result = await parseLink(url)
    if (result) {
      linkPreview.value = result
    } else {
      linkPreviewError.value = true
    }
  } catch (error) {
    console.warn('链接预览失败:', error)
    linkPreviewError.value = true
  } finally {
    linkPreviewLoading.value = false
  }
}

// 清除链接预览
const clearLinkPreview = () => {
  linkPreview.value = null
  linkPreviewLoading.value = false
  linkPreviewError.value = false
  lastParsedUrls.value = []
  if (linkPreviewDebounceTimer.value) {
    clearTimeout(linkPreviewDebounceTimer.value)
    linkPreviewDebounceTimer.value = null
  }
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
    setTimeout(() => {
      if (!isUnmounted.value) {
        atMemberPickerRef.value?.open(textareaRef.value?.selectionStart)
      }
    }, 50)
  }
}

const handleSend = async () => {
  const content = messageContent.value.trim()
  if (!content) { return }

  if (props.editingMessage) {
    emit('edit-confirm', content)
  } else {
    emit('send', content)
  }

  sendMyStopTypingStatus()
  clearDraft()

  messageContent.value = ''
  nextTick(() => {
    if (isUnmounted.value) { return }
    if (textareaRef.value) { textareaRef.value.style.height = 'auto' }
    textareaRef.value?.focus()
  })
}

const handleSendVoice = () => {
  const result = voicePreview.value
  if (!result) { return }

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
    if (isUnmounted.value) { return }
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
    if (isUnmounted.value) { return }
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
  if (!file) { return }

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
  if (!file) { return }

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
  if (!dataURL) { return }

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
  if (files.length === 0) { return }

  // 添加到待上传列表
  pendingFiles.value = [...pendingFiles.value, ...files]
  showFilePreview.value = true
}

const handlePaste = e => {
  const items = e.clipboardData?.items
  if (!items) { return }

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
  if (!inputArea) { return }

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
 * @param {File} file - 要上传的文件
 * @param {string} description - 文件描述
 * @param {string} type - 文件类型（image/audio/file）
 */
const uploadFileWithDescription = async (file, description, type) => {
  const formData = new FormData()
  formData.append('file', file)

  // 调用后端 API 上传文件
  let uploadUrl
  try {
    let res
    if (type === 'image') {
      res = await uploadImage(formData)
    } else {
      res = await uploadFileApi(formData)
    }

    // 根据后端响应结构获取 URL
    uploadUrl = res?.data?.url || res?.url || res?.data?.fileUrl
  } catch (error) {
    console.error('[文件上传] 失败:', error)
    throw new Error('上传失败: ' + (error.message || '未知错误'))
  }

  if (!uploadUrl) {
    throw new Error('上传失败: 未获取到文件地址')
  }

  // 通知父组件上传成功（保留兼容性）
  if (type === 'image') {
    emit('upload-image', { file, url: uploadUrl })
  } else {
    emit('upload-file', { file, url: uploadUrl })
  }

  // 如果有描述，发送一条文本消息
  if (description && description.trim()) {
    emit('send', description.trim())
  }

  // 发送文件消息
  if (type === 'image') {
    emit('send', `[图片: ${file.name}]`)
  } else if (type === 'audio') {
    emit('send', `[音频: ${file.name}]`)
  } else {
    emit('send', `[文件: ${file.name}]`)
  }
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
    if (isUnmounted.value) { return }
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
    if (isUnmounted.value) { return }
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

// 使用闭包变量存储事件处理器引用，避免污染全局对象
let globalKeydownHandler = null

onMounted(() => {
  // 加载所有草稿
  store.dispatch('im/session/loadDrafts')

  // 注册全局截图快捷键
  globalKeydownHandler = e => {
    const isWinShortcut = e.ctrlKey && e.altKey && e.key === 'a'
    const isMacShortcut = e.metaKey && e.shiftKey && e.key === 'a'

    if ((isWinShortcut || isMacShortcut) && !showEmojiPicker.value && !isVoiceMode.value) {
      e.preventDefault()
      handleScreenshot()
    }
  }

  document.addEventListener('keydown', globalKeydownHandler)
})

onUnmounted(() => {
  isUnmounted.value = true // 标记组件已卸载，防止后续 DOM 操作

  cleanupVoice()

  // 清理链接预览定时器
  if (linkPreviewDebounceTimer.value) {
    clearTimeout(linkPreviewDebounceTimer.value)
    linkPreviewDebounceTimer.value = null
  }

  // 移除全局键盘事件
  if (globalKeydownHandler) {
    document.removeEventListener('keydown', globalKeydownHandler)
    globalKeydownHandler = null
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.chat-input-container {
  display: flex;
  flex-direction: column;
  background: #FFFFFF;
  border-top: 1px solid #F2F3F5;
  padding: 0; // 移除外层 padding，改为内部精确控制
  position: relative;
  min-height: 180px;
}

// 顶部工具栏外壳
.input-toolbar-outer {
  height: 40px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid transparent; // 默认透明，聚焦时可变色
}

// 输入主区域
.input-main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 4px 16px 12px;
  position: relative;
  transition: background 0.2s;

  &.is-drag-over {
    background: rgba(22, 93, 255, 0.05);
    outline: 2px dashed #165DFF;
    outline-offset: -10px;
    border-radius: 8px;
    
    &::after {
      content: '松开鼠标即刻发送文件';
      position: absolute;
      top: 50%; left: 50%; transform: translate(-50%, -50%);
      background: #165DFF; color: #fff;
      padding: 8px 20px; border-radius: 20px;
      font-weight: 600; pointer-events: none;
    }
  }
}

.previews-container {
  margin-bottom: 8px;
}

.real-textarea {
  flex: 1;
  width: 100%;
  border: none !important;
  outline: none !important;
  resize: none;
  font-size: 15px;
  line-height: 1.6;
  color: #1D2129;
  background: transparent;
  padding: 0;
  margin: 0;
  font-family: inherit;
  
  &::placeholder {
    color: #C9CDD4;
  }
}

// 底部操作行
.input-bottom-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;

  .action-hints {
    .hint-text {
      font-size: 12px;
      color: #86909C;
      opacity: 0.8;
    }
  }

  .send-controls {
    display: flex;
    align-items: center;
    gap: 8px;

    .send-btn-v2 {
      height: 32px;
      padding: 0 16px;
      background: #165DFF;
      color: #FFFFFF;
      border: none;
      border-radius: 4px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
      display: flex;
      align-items: center;
      gap: 6px;

      .btn-text {
        margin-left: 2px; // 调整间距
      }

      &:hover:not(:disabled) {
        background: #3471FF;
        box-shadow: 0 4px 12px rgba(22, 93, 255, 0.2);
      }

      &:active:not(:disabled) {
        transform: scale(0.96);
      }

      &:disabled {
        background: #F2F3F5;
        color: #C9CDD4;
        cursor: not-allowed;
      }
    }
  }
}

.dark {
  .chat-input-container { background: #1D2129; border-top-color: #2E3238; }
  .real-textarea { color: #F2F3F5; }
  .send-btn-v2:disabled { background: #2E3238; color: #4E5969; }
}

.reply-preview-wrapper {
  margin: 0 0 8px;
  position: relative;
  max-width: 100%;
}

.reply-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  height: 40px;
  background: var(--dt-bg-tertiary);
  border-radius: 4px;
  border-left: 3px solid var(--dt-brand-color);
  transition: all 0.2s ease;

  &:hover {
    background: var(--dt-bg-subtle-hover);
  }

  .reply-icon {
    color: var(--dt-brand-color);
    font-size: 16px;
    flex-shrink: 0;
  }

  .reply-content {
    flex: 1;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
    color: var(--dt-text-secondary);

    .reply-sender {
      font-weight: 600;
      color: var(--dt-text-primary);
      margin-right: 6px;
    }
  }

  .reply-close {
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    cursor: pointer;
    color: var(--dt-text-quaternary);
    transition: all 0.2s;
    flex-shrink: 0;

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-text-primary);
    }

    .el-icon {
      font-size: 14px;
    }
  }
}

.toolbar-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.footer-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.footer-action-btn {
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s;

  &.voice-btn {
    width: 32px;
    color: var(--dt-text-secondary);

    &:hover {
      background: var(--dt-bg-subtle-hover);
      color: var(--dt-brand-color);
    }
  }

  &.send-btn {
    padding: 0 16px;
    background: var(--dt-brand-color);
    color: #fff;
    font-size: 13px;
    font-weight: 500;
    gap: 6px;

    &:hover:not(:disabled) {
      background: var(--dt-brand-hover);
    }

    &:disabled {
      background: #E4E7ED;
      color: #C0C4CC;
      cursor: not-allowed;
    }

    .material-icons-outlined {
      font-size: 16px;
    }
  }
}

.voice-recording-wrapper {
  padding: 12px;
  background: var(--dt-bg-subtle);
  border-radius: 8px;
  margin: 0 16px 12px;
}

.link-preview-wrapper {
  margin: 0 16px 8px;
  position: relative;
  max-width: 400px;

  .link-preview-close {
    position: absolute;
    top: -8px;
    right: -8px;
    width: 20px;
    height: 20px;
    background: #909399;
    color: #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid #fff;
    cursor: pointer;
    font-size: 12px;
    z-index: 5;

    &:hover {
      background: #f56c6c;
    }
  }
}

// 暗色模式（统一使用 :global(.dark) 选择器）
:global(.dark) {
  .chat-input-container {
    background: var(--dt-bg-card-dark);
    border-top-color: var(--dt-border-dark);
  }

  .input-area {
    background: var(--dt-bg-hover-dark);

    &.is-focused {
      background: var(--dt-bg-card-dark);
      box-shadow: 0 0 0 1px var(--dt-brand-lighter);
    }
  }

  .message-input {
    color: #e8e8e8;

    &::placeholder {
      color: #4b5563;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--dt-scrollbar-thumb-dark);

      &:hover {
        background: var(--dt-scrollbar-thumb-dark-hover);
      }
    }
  }

  .footer-action-btn {
    color: #6b7280;

    &:hover {
      background: var(--dt-white-06);
    }

    &.send-btn {
      background: var(--dt-brand-color);
      color: #ffffff;

      &:hover {
        background: var(--dt-brand-hover);
      }

      &:disabled {
        background: var(--dt-border-dark, #4b5563);
      }
    }
  }

  .send-btn:disabled {
    background: #3F424A;
    color: #5F6672;
  }
}
</style>
