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
    />

    <!-- 录音动画区域 - 显示在工具栏上方 -->
    <div v-if="isVoiceMode" class="voice-recording-wrapper">
      <VoiceRecorder
        @record-complete="handleVoiceRecordComplete"
        @cancel="handleVoiceCancel"
      />
    </div>

    <!-- 引用消息预览 -->
    <ReplyPreview
      v-if="replyingMessage"
      :sender-name="replyingMessage.senderName"
      :content="replyingMessage.content"
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
      class="input-area"
      :class="{ 'is-drag-over': isDragOver, 'is-voice-mode': isVoiceMode }"
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
        :placeholder="isVoiceMode ? '正在录音...' : (session?.type === 'GROUP' ? '发消息...' : '发消息...')"
        @input="handleInput"
        @keydown="handleKeydown"
        @paste="handlePaste"
        :disabled="isVoiceMode"
      ></textarea>

      <div class="input-footer" v-if="!isVoiceMode">
        <span class="hint-text">{{ sendShortcutHint }}</span>
        <div class="footer-actions">
          <!-- 语音输入切换按钮 -->
          <el-tooltip content="按住说话" placement="top">
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
            {{ sending ? '发送中' : '发送' }}
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

    <AtMemberPicker ref="atMemberPickerRef" :session-id="session?.id" @select="onAtSelect" />

    <CommandPalette
      :show="showCommandPalette"
      :position="commandPalettePosition"
      @close="showCommandPalette = false"
      @select="handleCommandSelect"
    />

    <!-- 隐藏的输入元素 -->
    <input
      type="file"
      ref="videoInputRef"
      accept="video/mp4,video/webm,video/ogg,video/quicktime"
      style="display: none"
      @change="handleVideoFileChange"
    />

    <input
      type="file"
      ref="imageInputRef"
      accept="image/*"
      style="display: none"
      @change="handleImageFileChange"
    />

    <input
      type="file"
      ref="fileInputRef"
      style="display: none"
      @change="handleFileInputChange"
    />

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
  </div>
</template>

<script setup>
import { ref, nextTick, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { Microphone } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { isScreenshotSupported } from '@/utils/screenshot'

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
  onDraftChange: (data) => emit('draft-change', data)
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
    (position) => {
      const { latitude, longitude } = position.coords
      emit('send-location', {
        latitude,
        longitude,
        address: ''
      })
    },
    (error) => {
      console.error('获取位置失败:', error)
      ElMessage.error('获取位置失败，请检查定位权限')
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
  )
}

const handleCommandExecute = (command) => {
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
  onSend: (data) => emit('send-voice', data)
})

// 输入状态
const { sendMyStopTypingStatus } = useTypingIndicator({
  sessionId: computed(() => props.session?.id),
  currentUser,
  sendTyping: (sessionId) => {
    wsSendMessage({ type: 'typing', data: { conversationId: sessionId, userId: currentUser.value?.id } })
  },
  sendStopTyping: (sessionId) => {
    wsSendMessage({ type: 'stop-typing', data: { conversationId: sessionId, userId: currentUser.value?.id } })
  }
})

// ========== 其他状态 ==========

const textareaRef = ref(null)
const atMemberPickerRef = ref(null)
const imageInputRef = ref(null)
const fileInputRef = ref(null)
const videoInputRef = ref(null)
const isVoiceMode = ref(false)

const showEmojiPicker = ref(false)
const emojiPickerPosition = ref({ x: 0, y: 0 })
const showScheduleDialog = ref(false)
const showScreenshotPreview = ref(false)
const screenshotData = ref(null)
const showScreenshotGuide = ref(false)
const showSmartReply = ref(false)
const smartReplyPosition = ref({ x: 0, y: 0 })

// 拖拽状态
const isDragOver = ref(false)
let dragCounter = 0

// ========== 计算属性 ==========

const sendShortcutHint = computed(() => {
  const shortcut = store.state.im.settings.shortcuts.send
  return shortcut === 'ctrl-enter' ? '按 Ctrl + Enter 发送' : '按 Enter 发送'
})

const canSend = computed(() => messageContent.value.trim().length > 0)

// ========== 工具方法 ==========

const autoResize = () => {
  const tx = textareaRef.value
  if (!tx) return
  tx.style.height = 'auto'
  tx.style.height = tx.scrollHeight + 'px'
}

const insertAt = (nickname) => {
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
  sendMyStopTypingStatus()
}

const handleKeydown = (e) => {
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
  if (!content) return

  if (props.editingMessage) {
    emit('edit-confirm', content)
  } else {
    emit('send', content)
  }

  sendMyStopTypingStatus()
  clearDraft()

  messageContent.value = ''
  nextTick(() => {
    if (textareaRef.value) textareaRef.value.style.height = 'auto'
    textareaRef.value?.focus()
  })
}

const handleSendVoice = () => {
  const result = voicePreview.value
    if (!result) return

  emit('send-voice', { file: result.file, duration: result.duration })
  deleteVoicePreview()
  isVoiceMode.value = false
}

const handleVoiceRecordComplete = (data) => {
  handleVoiceRecordCompleteInternal(data)
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
    const inputArea = document.querySelector('.input-area')
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

const selectEmoji = (emoji) => {
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
  if (!file) return

  const config = {
    validTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/svg+xml'],
    maxSize: 10 * 1024 * 1024,
    typeError: '支持的图片格式：JPG、PNG、GIF、WebP、SVG',
    sizeError: '图片大小不能超过10MB'
  }

  if (validateFile(file, config)) {
    emit('upload-image', file)
    imageInputRef.value.value = ''
  }
}

const handleFileInputChange = () => {
  const file = fileInputRef.value?.files?.[0]
  if (!file) return

  const config = {
    maxSize: 100 * 1024 * 1024,
    sizeError: '文件大小不能超过100MB'
  }

  if (validateFile(file, config)) {
    emit('upload-file', file)
    fileInputRef.value.value = ''
  }
}

const handleVideoFileChange = () => {
  const file = videoInputRef.value?.files?.[0]
  if (!file) return

  const config = {
    validTypes: ['video/mp4', 'video/webm', 'video/ogg', 'video/quicktime'],
    maxSize: 100 * 1024 * 1024,
    typeError: '支持的视频格式：MP4、WebM、OGG',
    sizeError: '视频大小不能超过100MB'
  }

  if (validateFile(file, config)) {
    const url = URL.createObjectURL(file)
    emit('upload-video', { file, url })
  }
  videoInputRef.value.value = ''
}

// ========== 截图 ==========

const handleScreenshot = () => {
  if (!isScreenshotSupported()) {
    ElMessage.warning('您的浏览器不支持截图功能，请使用 Chrome 72+、Edge 79+ 或 Firefox 66+')
    return
  }
  showScreenshotGuide.value = true
}

const handleSendScreenshot = async (blob) => {
  const formData = new FormData()
  formData.append('file', blob, 'screenshot.png')
  emit('send-screenshot', formData)
  showScreenshotPreview.value = false
  screenshotData.value = null
}

const handleSendScreenshotFromGuide = async (dataURL) => {
  if (!dataURL) return

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

const handleDragEnter = (e) => {
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

const handleDragOver = (e) => {
  e.preventDefault()
}

const handleDrop = (e) => {
  e.preventDefault()
  dragCounter = 0
  isDragOver.value = false

  const files = e.dataTransfer?.files
  if (!files || files.length === 0) return

  for (const file of files) {
    if (file.type.startsWith('image/')) {
      emit('upload-image', file)
      break
    }
  }
}

const handlePaste = (e) => {
  const items = e.clipboardData?.items
  if (!items) return

  for (const item of items) {
    if (item.kind === 'file') {
      const file = item.getAsFile()
      if (file) {
        e.preventDefault()
        const formData = new FormData()
        formData.append('file', file)
        if (file.type.startsWith('image/')) emit('upload-image', formData)
        else emit('upload-file', formData)
      }
    }
  }
}

// ========== @提及 ==========

const handleAtMember = () => {
  if (props.session?.type === 'GROUP') {
    atMemberPickerRef.value?.open(textareaRef.value.selectionStart || 0)
  }
}

const onAtSelect = (member) => insertAt(member.nickname || member.userName)

const handleScheduleSaved = () => {
  ElMessage.success('日程已创建')
}

const handleShowSmartReply = () => {
  const inputArea = document.querySelector('.chat-input-container')
  if (!inputArea) return

  const rect = inputArea.getBoundingClientRect()
  smartReplyPosition.value = {
    x: rect.right - 360,
    y: rect.top - 480
  }
  showSmartReply.value = true
}

const handleSelectSmartReply = (replyText) => {
  messageContent.value = replyText
  nextTick(() => {
    autoResize()
    textareaRef.value?.focus()
  })
}

// ========== 暴露方法 ==========

defineExpose({
  insertAt,
  triggerScreenshot: handleScreenshot
})

// ========== 生命周期 ==========

onMounted(() => {
  // 加载所有草稿
  store.dispatch('im/session/loadDrafts')

  // 注册全局截图快捷键
  const handleGlobalKeydown = (e) => {
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
  background: var(--dt-bg-input);
  display: flex;
  flex-direction: column;
  position: relative;
  border-top: 1px solid var(--dt-border-light);
  padding: 8px 16px 16px;
  transition: background var(--dt-transition-base);
  z-index: 10;

  // 为录音区域预留空间
  &.has-recording {
    padding-bottom: 16px;
  }

  .dark & {
    border-top-color: var(--dt-border-dark);
  }

  &.is-resizing {
    box-shadow: 0 -4px 16px rgba(0, 137, 255, 0.15);
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

  &.is-drag-over {
    background: rgba(24, 144, 255, 0.04);
    border-radius: var(--dt-radius-md);
    box-shadow: inset 0 0 0 2px var(--dt-brand-color);

    &::after {
      content: '松开即可发送文件';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      padding: 12px 20px;
      background: var(--dt-brand-color);
      color: #fff;
      font-size: 14px;
      font-weight: 500;
      border-radius: 8px;
      pointer-events: none;
      z-index: 10;
    }

    .message-input { opacity: 0.3; }
  }
}

.message-input {
  flex: 1;
  width: 100%;
  border: none;
  outline: none;
  resize: none;
  font-size: var(--dt-font-size-base);
  line-height: 1.6;
  color: var(--dt-text-primary);
  padding: 8px 0;
  min-height: 80px;
  background: transparent;
  font-family: var(--dt-font-family);

  &::placeholder { color: var(--dt-text-quaternary); }

  .dark & { color: var(--dt-text-primary-dark); }
}

.input-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
  margin-top: 8px;

  .hint-text {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    user-select: none;

    .dark & { color: var(--dt-text-tertiary-dark); }
  }

  .footer-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .footer-action-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-color);
    border-radius: var(--dt-radius-md);
    color: var(--dt-text-secondary);
    cursor: pointer;
    transition: all var(--dt-transition-fast);

    .el-icon { font-size: 16px; }

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-brand-color);
      border-color: var(--dt-brand-color);
    }

    &.active {
      background: var(--dt-brand-color);
      color: #fff;
      border-color: var(--dt-brand-color);
    }

    .dark & {
      background: var(--dt-bg-body-dark);
      border-color: var(--dt-border-dark);

      &:hover { background: var(--dt-bg-hover-dark); }
    }
  }

  .send-btn {
    padding: 8px 20px;
    border-radius: var(--dt-radius-md);
    border: none;
    background: var(--dt-bg-body);
    color: var(--dt-text-quaternary);
    font-size: 14px;
    font-weight: 500;
    cursor: default;
    transition: all var(--dt-transition-fast);
    display: inline-flex;
    align-items: center;
    justify-content: center;

    &.active {
      background: var(--dt-brand-color);
      color: #fff;
      cursor: pointer;

      &:hover {
        opacity: 0.9;
        transform: translateY(-1px);
      }
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .dark & { background: var(--dt-bg-hover-dark); }
  }
}

// 响应式
@media (max-width: 479px) {
  .chat-input-container {
    padding: 8px 12px 12px;
  }

  .message-input {
    font-size: 14px;
    min-height: 60px;
  }
}

// 录音区域 - 显示在工具栏上方
.voice-recording-wrapper {
  position: absolute;
  bottom: calc(100% - 4px);
  left: 16px;
  right: 16px;
  z-index: 100;
  pointer-events: auto;

  :deep(.voice-recorder) {
    background: var(--dt-bg-card);
    border: 1px solid var(--dt-brand-color);
    border-radius: var(--dt-radius-lg);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    animation: slideInUp 0.2s ease-out;
  }

  .dark & :deep(.voice-recorder) {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-brand-color);
  }
}

// 录音动画滑入效果
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 输入区域在语音模式下的样式
.input-area.is-voice-mode {
  .message-input {
    background: rgba(0, 137, 255, 0.05);
    cursor: not-allowed;
  }
}
</style>
