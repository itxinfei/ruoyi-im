<template>
  <div 
    class="chat-input-container" 
    :class="{ 'drag-over': isDragOver }"
    :style="{ minHeight: containerHeight + 'px' }"
    @dragover.prevent="handleDragOver"
    @dragleave.prevent="handleDragLeave"
  >
    <div
      class="resize-handle"
      @mousedown="startResize"
      @dblclick="resetHeight"
    >
      <div class="resize-indicator"></div>
    </div>

    <!-- 拖拽上传提示层 -->
    <Transition name="fade">
      <div v-if="isDragOver" class="drop-overlay">
        <div class="drop-hint">
          <el-icon class="drop-icon"><Upload /></el-icon>
          <span class="drop-text">释放以上传文件</span>
          <span class="drop-subtext">支持图片、文档等多种格式</span>
        </div>
      </div>
    </Transition>

    <!-- 工具栏 -->
    <div class="input-toolbar">
      <div class="toolbar-left">
        <el-tooltip content="表情" placement="top">
          <button class="toolbar-btn" :class="{ active: showEmojiPicker }" @click.stop="toggleEmojiPicker">
            <el-icon><ChatDotRound /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="图片" placement="top">
          <button class="toolbar-btn" @click="$emit('upload-image')">
            <el-icon><Picture /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="文件" placement="top">
          <button class="toolbar-btn" @click="$emit('upload-file')">
            <el-icon><FolderOpened /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="截图" placement="top">
          <button class="toolbar-btn" @click="handleScreenshot">
             <span class="material-icons-outlined">content_cut</span>
          </button>
        </el-tooltip>

        <el-tooltip v-if="session?.type === 'GROUP'" content="@成员" placement="top">
          <button class="toolbar-btn" @click="handleAtMember">
            <span class="material-icons-outlined">alternate_email</span>
          </button>
        </el-tooltip>

        <el-divider direction="vertical" />

        <el-tooltip content="语音消息" placement="top">
          <button class="toolbar-btn" @click="handleVoiceRecord">
            <el-icon><Microphone /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="位置" placement="top">
          <button class="toolbar-btn" @click="handleLocation">
            <span class="material-icons-outlined">location_on</span>
          </button>
        </el-tooltip>

        <el-tooltip content="语音通话" placement="top">
          <button class="toolbar-btn" @click="$emit('start-call')">
            <el-icon><Phone /></el-icon>
          </button>
        </el-tooltip>
      </div>

      <div class="toolbar-right">
        <el-button link class="history-btn">
          <el-icon><Clock /></el-icon> 聊天记录
        </el-button>
      </div>
    </div>

    <!-- 引用消息/正在回复 预览区 (钉钉样式) -->
    <div v-if="replyingMessage" class="reply-preview-container">
      <div class="reply-content-box">
        <span class="reply-user">{{ replyingMessage.senderName }}:</span>
        <span class="reply-text">{{ replyingMessage.content }}</span>
        <el-icon class="cancel-reply" @click="clearReply"><Close /></el-icon>
      </div>
    </div>

    <div v-if="editingMessage" class="edit-preview-container">
      <div class="edit-content-box">
        <span class="edit-label">正在编辑:</span>
        <span class="edit-text">{{ editingMessage.content }}</span>
        <el-icon class="cancel-edit" @click="$emit('cancel-edit')"><Close /></el-icon>
      </div>
    </div>

    <!-- 输入核心区域 -->
    <div class="input-area">
      <textarea
        ref="textareaRef"
        v-model="messageContent"
        class="message-input"
        :placeholder="getInputPlaceholder"
        @input="handleInput"
        @keydown="handleKeydown"
        @paste="handlePaste"
        @drop.prevent="handleDrop"
      ></textarea>

      <div class="input-footer">
        <span class="hint-text">{{ sendShortcutHint }}</span>
        <button
          class="send-btn"
          :class="{ active: canSend }"
          :disabled="!canSend || sending"
          @click="handleSend"
        >
          <el-icon v-if="sending" class="is-loading"><Loading /></el-icon>
          {{ sending ? '发送中' : '发送' }}
        </button>
      </div>
    </div>

    <EmojiPicker v-if="showEmojiPicker" @select="selectEmoji" ref="emojiPickerRef" />
    <AtMemberPicker ref="atMemberPickerRef" :session-id="session?.id" @select="onAtSelect" />
    <VoiceRecorder ref="voiceRecorderRef" @send="handleSendVoice" />
  </div>
</template>

<script setup>
import { ref, nextTick, computed, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import { Close, ChatDotRound, Picture, FolderOpened, Phone, Clock, Upload, Microphone, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import EmojiPicker from '@/components/EmojiPicker/index.vue'
import AtMemberPicker from './AtMemberPicker.vue'
import VoiceRecorder from './VoiceRecorder.vue'

const props = defineProps({
  session: Object,
  sending: Boolean,
  replyingMessage: Object,
  editingMessage: Object
})

const emit = defineEmits(['send', 'upload-image', 'upload-file', 'cancel-reply', 'cancel-edit', 'edit-confirm', 'input', 'start-call', 'start-video', 'send-location', 'typing', 'send-voice'])

const store = useStore()
const messageContent = ref('')
const sendShortcutHint = computed(() => {
  const shortcut = store.state.im.settings?.shortcuts?.send || 'enter'
  return shortcut === 'ctrl-enter' ? '按 Ctrl + Enter 发送' : '按 Enter 发送'
})

const getInputPlaceholder = computed(() => {
  if (props.session?.type === 'GROUP') {
    return '输入消息，@成员可提醒TA...'
  }
  return '输入消息...'
})

const showEmojiPicker = ref(false)
const textareaRef = ref(null)
const atMemberPickerRef = ref(null)
const voiceRecorderRef = ref(null)

// 拖拽状态
const isDragOver = ref(false)

// 钉钉风格高度逻辑
const containerHeight = ref(200)
const minHeight = 160
const maxHeight = 600
let isResizing = false
let startY = 0
let startHeight = 0

const startResize = (e) => {
  isResizing = true
  startY = e.clientY
  startHeight = containerHeight.value
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
  document.body.style.cursor = 'ns-resize'
}

const handleResize = (e) => {
  if (!isResizing) return
  const delta = startY - e.clientY
  const newH = startHeight + delta
  if (newH >= minHeight && newH <= maxHeight) containerHeight.value = newH
}

const stopResize = () => {
  isResizing = false
  document.body.style.cursor = ''
  localStorage.setItem('im_input_height', containerHeight.value)
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
}

const resetHeight = () => containerHeight.value = 200

const insertAt = (nickname) => {
  const atText = `@${nickname} `
  const pos = textareaRef.value?.selectionStart || messageContent.value.length
  messageContent.value = messageContent.value.slice(0, pos) + atText + messageContent.value.slice(pos)
  nextTick(() => { textareaRef.value?.focus(); autoResize() })
}
defineExpose({ insertAt })

const canSend = computed(() => messageContent.value.trim().length > 0)

const autoResize = () => {
  const tx = textareaRef.value
  if (!tx) return
  tx.style.height = 'auto'
  tx.style.height = Math.min(tx.scrollHeight, 200) + 'px'
}

const handleInput = () => {
  autoResize()
  emit('input', messageContent.value)
  // 发送正在输入状态（防抖）
  sendTypingStatus()
}

// 防抖发送正在输入状态
let typingTimer = null
const sendTypingStatus = () => {
  if (typingTimer) clearTimeout(typingTimer)
  typingTimer = setTimeout(() => {
    emit('typing', true)
    // 3秒后自动停止 typing 状态
    setTimeout(() => {
      emit('typing', false)
    }, 3000)
  }, 500) // 500ms 防抖
}

const handleKeydown = (e) => {
  const sendShortcut = store.state.im.settings?.shortcuts?.send || 'enter'

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

const handleSend = () => {
  const content = messageContent.value.trim()
  if (!content) return

  if (props.editingMessage) {
    emit('edit-confirm', content)
  } else {
    emit('send', content)
  }

  messageContent.value = ''
  nextTick(() => {
    if (textareaRef.value) textareaRef.value.style.height = 'auto'
    textareaRef.value?.focus()
  })
}

const clearReply = () => {
  emit('cancel-reply')
}

// 拖拽处理
const handleDragOver = (e) => {
  isDragOver.value = true
}

const handleDragLeave = (e) => {
  isDragOver.value = false
}

// 粘贴处理 (优化版)
const handlePaste = (e) => {
  const items = e.clipboardData?.items
  if (!items) return
  
  let hasFile = false
  for (const item of items) {
    if (item.kind === 'file') {
      hasFile = true
      const file = item.getAsFile()
      if (file) {
        e.preventDefault()
        processFile(file, 'paste')
      }
    }
  }
  
  // 如果粘贴了文件，显示提示
  if (hasFile) {
    ElMessage.success('正在上传...')
  }
}

// 拖拽放下处理
const handleDrop = (e) => {
  isDragOver.value = false
  const files = e.dataTransfer?.files
  if (!files || files.length === 0) return
  
  ElMessage.info(`正在上传 ${files.length} 个文件...`)
  
  for (const file of files) {
    processFile(file, 'drop')
  }
}

// 统一处理文件
const processFile = (file, source = 'upload') => {
  const formData = new FormData()
  formData.append('file', file)
  
  // 根据文件类型分发
  if (file.type.startsWith('image/')) {
    emit('upload-image', formData)
  } else {
    emit('upload-file', formData)
  }
}

// 截图功能
const handleScreenshot = () => {
  // 检查是否支持截图API
  if (navigator.mediaDevices && navigator.mediaDevices.getDisplayMedia) {
    navigator.mediaDevices.getDisplayMedia({ video: true })
      .then(stream => {
        const track = stream.getVideoTracks()[0]
        const imageCapture = new ImageCapture(track)
        return imageCapture.takePhoto()
      })
      .then(blob => {
        const file = new File([blob], `screenshot_${Date.now()}.png`, { type: 'image/png' })
        processFile(file, 'screenshot')
        ElMessage.success('截图已上传')
      })
      .catch(err => {
        if (err.name !== 'AbortError') {
          ElMessage.info('请使用系统截图工具 (如 Win+Shift+S)')
        }
      })
  } else {
    ElMessage.info('请使用系统截图工具后粘贴')
  }
}

// 语音录制
const handleVoiceRecord = () => {
  voiceRecorderRef.value?.open()
}

// 发送语音消息
const handleSendVoice = ({ blob, duration }) => {
  emit('send-voice', { blob, duration })
}

// 位置消息
const handleLocation = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords
        const locationData = {
          latitude,
          longitude,
          address: '当前定位'
        }
        emit('send-location', locationData)
        ElMessage.success('位置已发送')
      },
      (error) => {
        ElMessage.warning('无法获取位置，请检查定位权限')
      }
    )
  } else {
    ElMessage.warning('您的浏览器不支持定位功能')
  }
}

const toggleEmojiPicker = () => { showEmojiPicker.value = !showEmojiPicker.value }

const selectEmoji = (emoji) => {
  const pos = textareaRef.value?.selectionStart || messageContent.value.length
  messageContent.value = messageContent.value.slice(0, pos) + emoji + messageContent.value.slice(pos)
  showEmojiPicker.value = false
  nextTick(() => { textareaRef.value?.focus(); autoResize() })
}

const handleAtMember = () => {
  if (props.session?.type === 'GROUP') {
    atMemberPickerRef.value?.open(textareaRef.value.selectionStart || 0)
  }
}

const onAtSelect = (m) => insertAt(m.nickname || m.userName)

// 监听回复消息，自动获取焦点
watch(() => props.replyingMessage, (val) => {
  if (val) nextTick(() => textareaRef.value?.focus())
})

onMounted(() => {
  const saved = localStorage.getItem('im_input_height')
  if (saved) containerHeight.value = parseInt(saved)
})
</script>

<style scoped lang="scss">
.chat-input-container {
  background: #fff;
  display: flex;
  flex-direction: column;
  position: relative;
  border-top: 1px solid #f0f1f2;
  padding: 4px 16px 16px;
  transition: all 0.2s;

  &.drag-over {
    border-color: #1677ff;
    box-shadow: inset 0 0 0 2px rgba(22, 119, 255, 0.1);
  }

  .dark & { border-top-color: #334155; }
}

// ============================================================================
// 拖拽上传提示层
// ============================================================================
.drop-overlay {
  position: absolute;
  inset: 0;
  background: rgba(22, 119, 255, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  border: 2px dashed #1677ff;
  border-radius: 8px;
  margin: 4px;

  .drop-hint {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #1677ff;

    .drop-icon {
      font-size: 48px;
    }

    .drop-text {
      font-size: 18px;
      font-weight: 600;
    }

    .drop-subtext {
      font-size: 13px;
      opacity: 0.7;
    }
  }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

// ============================================================================
// 调整大小手柄
// ============================================================================
.resize-handle {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  cursor: ns-resize;
  z-index: 10;

  &:hover {
    background: rgba(0, 137, 255, 0.1);
  }
}

.resize-indicator {
  width: 40px;
  height: 3px;
  background: #d9d9d9;
  border-radius: 2px;
  margin: 0 auto;
  transition: background 0.2s;

  .resize-handle:hover & {
    background: #1677ff;
  }
}

// ============================================================================
// 工具栏
// ============================================================================
.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 2px;
  }

  .toolbar-btn {
    background: none;
    border: none;
    width: 32px;
    height: 32px;
    padding: 0;
    cursor: pointer;
    color: #5c5c5c;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.15s;

    &:hover {
      background: #f2f3f5;
      color: #1677ff;
    }

    &.active {
      color: #1677ff;
      background: #e8f4ff;
    }

    .el-icon, .material-icons-outlined {
      font-size: 18px;
    }

    .dark & {
      color: #a1a1a1;
      &:hover {
        background: #3a3a3a;
      }
    }
  }

  .history-btn {
    font-size: 12px;
    color: #999;

    &:hover {
      color: #1677ff;
    }
  }

  .el-divider--vertical {
    height: 18px;
    margin: 0 4px;
    border-color: #e5e5e5;
  }
}

// ============================================================================
// 回复/编辑预览区
// ============================================================================
.reply-preview-container,
.edit-preview-container {
  padding: 10px 14px;
  margin-bottom: 8px;
  border-radius: 8px;
  background: #f8fafc;
  border-left: 3px solid #1677ff;

  .dark & {
    background: rgba(30, 41, 59, 0.5);
  }

  .reply-content-box,
  .edit-content-box {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;

    .reply-user,
    .edit-label {
      color: #1677ff;
      font-weight: 600;
    }

    .reply-text,
    .edit-text {
      flex: 1;
      color: #64748b;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .cancel-reply,
    .cancel-edit {
      cursor: pointer;
      color: #8f959e;
      font-size: 16px;

      &:hover {
        color: #ff4d4f;
      }
    }
  }
}

.edit-preview-container {
  border-left-color: #52c41a;

  .edit-label {
    color: #52c41a;
  }
}

// ============================================================================
// 输入区域
// ============================================================================
.input-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.message-input {
  flex: 1;
  width: 100%;
  border: none;
  outline: none;
  resize: none;
  font-size: 15px;
  line-height: 1.6;
  color: #1f2329;
  padding: 0;
  min-height: 60px;
  max-height: 200px;
  background: transparent;

  .dark & {
    color: #f1f5f9;
  }

  &::placeholder {
    color: #bbbfc4;
  }
}

.input-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-top: 8px;

  .hint-text {
    font-size: 12px;
    color: #999;
    user-select: none;
  }

  .send-btn {
    min-width: 68px;
    height: 28px;
    padding: 0 16px;
    border-radius: 4px;
    border: none;
    background: #e5e5e5;
    color: #fff;
    font-size: 13px;
    font-weight: 500;
    cursor: not-allowed;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;

    &.active {
      background: var(--dt-brand-color);
      color: #fff;
      cursor: pointer;

      &:hover {
        background: #4096ff;
      }

      &:active {
        background: #0958d9;
      }
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
.dark {
  .chat-input-container {
    background: var(--dt-bg-input-dark);
  }

  .drop-overlay {
    background: rgba(22, 119, 255, 0.1);
  }

  .toolbar-btn:hover {
    background: rgba(255, 255, 255, 0.1);
  }

  .reply-preview-container,
  .edit-preview-container {
    background: rgba(30, 41, 59, 0.5);
  }

  .send-btn {
    background: #334155;

    &.active {
      background: var(--dt-brand-color);
    }
  }
}
</style>
