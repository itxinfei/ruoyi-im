<template>
  <div class="chat-input-container" :style="{ minHeight: containerHeight + 'px' }">
    <div 
      class="resize-handle" 
      @mousedown="startResize"
      @dblclick="resetHeight"
    >
      <div class="resize-indicator"></div>
    </div>

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

        <el-tooltip content="语音通话" placement="top">
          <button class="toolbar-btn" @click="$emit('start-call')">
            <el-icon><Phone /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="按住说话" placement="top">
          <button class="toolbar-btn" :class="{ active: isVoiceMode }" @click="toggleVoiceMode">
            <el-icon><Microphone /></el-icon>
          </button>
        </el-tooltip>
      </div>

      <div class="toolbar-right">
        <el-button link class="history-btn">
          <el-icon><Clock /></el-icon> 聊天记录
        </el-button>
      </div>
    </div>

    <!-- 引用消息/正在回复 预览区 (还原钉钉样式) -->
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
      <!-- 语音录制模式 -->
      <VoiceRecorder
        v-if="isVoiceMode"
        @send="handleSendVoice"
        @cancel="isVoiceMode = false"
      />

      <!-- 文字输入模式 -->
      <textarea
        v-else
        ref="textareaRef"
        v-model="messageContent"
        class="message-input"
        :placeholder="session?.type === 'GROUP' ? '发消息...' : '发消息...'"
        @input="handleInput"
        @keydown="handleKeydown"
        @paste="handlePaste"
        @drop.prevent="handleDrop"
      ></textarea>

      <div class="input-footer" v-if="!isVoiceMode">
        <span class="hint-text">{{ sendShortcutHint }}</span>
        <button
          class="send-btn"
          :class="{ active: canSend }"
          :disabled="!canSend || sending"
          @click="handleSend"
        >
          {{ sending ? '正在发送' : '发送' }}
        </button>
      </div>
    </div>

    <EmojiPicker v-if="showEmojiPicker" @select="selectEmoji" ref="emojiPickerRef" />
    <AtMemberPicker ref="atMemberPickerRef" :session-id="session?.id" @select="onAtSelect" />
  </div>
</template>

<script setup>
import { ref, nextTick, computed, onMounted, onUnmounted, watch } from 'vue'
import { useStore } from 'vuex'
import { Close, ChatDotRound, Picture, FolderOpened, Phone, Clock, Microphone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useImWebSocket } from '@/composables/useImWebSocket'
import EmojiPicker from '@/components/EmojiPicker/index.vue'
import AtMemberPicker from './AtMemberPicker.vue'
import VoiceRecorder from './VoiceRecorder.vue'

const props = defineProps({
  session: Object,
  sending: Boolean,
  replyingMessage: Object,
  editingMessage: Object
})

const emit = defineEmits(['send', 'send-voice', 'upload-image', 'upload-file', 'cancel-reply', 'cancel-edit', 'edit-confirm', 'input', 'start-call', 'start-video'])

const store = useStore()
const { sendMessage: wsSendMessage } = useImWebSocket()
const currentUser = computed(() => store.getters['user/currentUser'])

const messageContent = ref('')
const sendShortcutHint = computed(() => {
  const shortcut = store.state.im.settings.shortcuts.send
  return shortcut === 'ctrl-enter' ? '按 Ctrl + Enter 发送' : '按 Enter 发送'
})

const showEmojiPicker = ref(false)
const textareaRef = ref(null)
const atMemberPickerRef = ref(null)
const isVoiceMode = ref(false)

// 钉钉风格高度逻辑
const containerHeight = ref(160)
const minHeight = 160
const maxHeight = 600
let isResizing = false
let startY = 0
let startHeight = 0

const startResize = (e) => {
  isResizing = true; startY = e.clientY; startHeight = containerHeight.value
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
  isResizing = false; document.body.style.cursor = ''
  localStorage.setItem('im_input_height', containerHeight.value)
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
}

const resetHeight = () => containerHeight.value = 160

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
  tx.style.height = tx.scrollHeight + 'px'
}

const handleInput = () => {
  autoResize()
  emit('input', messageContent.value)

  // 发送输入状态（防抖）
  sendTypingIndicator()
}

// 输入状态防抖发送
let typingTimer = null
let lastTypingSendTime = 0
const TYPING_DEBOUNCE = 1000 // 1秒内只发送一次
const TYPING_INTERVAL = 3000 // 每3秒重新发送一次输入状态

const sendTypingIndicator = () => {
  const now = Date.now()

  // 清除之前的定时器
  if (typingTimer) {
    clearTimeout(typingTimer)
  }

  // 检查是否需要发送（距离上次发送超过间隔时间）
  const shouldSend = now - lastTypingSendTime > TYPING_DEBOUNCE

  if (shouldSend && props.session?.id && messageContent.value.trim()) {
    lastTypingSendTime = now

    // 发送 typing 消息
    wsSendMessage({
      type: 'typing',
      data: {
        conversationId: props.session.id,
        userId: currentUser.value?.id
      }
    })

    // 设置下次重新发送的定时器
    typingTimer = setTimeout(() => {
      if (messageContent.value.trim()) {
        lastTypingSendTime = Date.now()
        wsSendMessage({
          type: 'typing',
          data: {
            conversationId: props.session.id,
            userId: currentUser.value?.id
          }
        })
      }
    }, TYPING_INTERVAL)
  }
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

// 粘贴处理
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

const handleDrop = (e) => {
  const files = e.dataTransfer?.files
  if (!files || files.length === 0) return
  for (const file of files) {
    const formData = new FormData()
    formData.append('file', file)
    if (file.type.startsWith('image/')) emit('upload-image', formData)
    else emit('upload-file', formData)
  }
}

const toggleEmojiPicker = () => { showEmojiPicker.value = !showEmojiPicker.value }
const selectEmoji = (emoji) => { 
  const pos = textareaRef.value?.selectionStart || messageContent.value.length
  messageContent.value = messageContent.value.slice(0, pos) + emoji + messageContent.value.slice(pos)
  showEmojiPicker.value = false
  nextTick(() => { textareaRef.value?.focus(); autoResize() })
}

const handleScreenshot = () => ElMessage.info('正在启用系统截图...')
const handleAtMember = () => {
  if (props.session?.type === 'GROUP') {
    atMemberPickerRef.value?.open(textareaRef.value.selectionStart || 0)
  }
}

const onAtSelect = (m) => insertAt(m.nickname || m.userName)

// 切换语音模式
const toggleVoiceMode = () => {
  isVoiceMode.value = !isVoiceMode.value
  if (isVoiceMode.value) {
    nextTick(() => textareaRef.value?.blur())
  } else {
    nextTick(() => textareaRef.value?.focus())
  }
}

// 发送语音消息
const handleSendVoice = ({ file, duration }) => {
  emit('send-voice', { file, duration })
  isVoiceMode.value = false
}

// 监听回复消息，自动获取焦点
watch(() => props.replyingMessage, (val) => {
  if (val) nextTick(() => textareaRef.value?.focus())
})

onMounted(() => {
  const saved = localStorage.getItem('im_input_height')
  if (saved) containerHeight.value = parseInt(saved)
})

onUnmounted(() => {
  // 清理输入状态定时器
  if (typingTimer) {
    clearTimeout(typingTimer)
    typingTimer = null
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

// ============================================================================
// 容器
// ============================================================================
.chat-input-container {
  background: var(--dt-bg-input);
  display: flex;
  flex-direction: column;
  position: relative;
  border-top: 1px solid var(--dt-border-light);
  padding: 8px 16px 16px;
  transition: background var(--dt-transition-base);
  z-index: 10; // 确保输入容器在正确的层级

  .dark & {
    border-top-color: var(--dt-border-dark);
  }
}

// ============================================================================
// 调整手柄
// ============================================================================
.resize-handle {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  cursor: ns-resize;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--dt-transition-base);

  &:hover {
    background: var(--dt-brand-light);
  }

  .resize-indicator {
    width: 40px;
    height: 3px;
    background: var(--dt-border-color);
    border-radius: var(--dt-radius-full);
    opacity: 0.5;
    transition: all var(--dt-transition-base);

    .resize-handle:hover & {
      width: 60px;
      background: var(--dt-brand-color);
      opacity: 1;
    }
  }
}

// ============================================================================
// 工具栏
// ============================================================================
.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .toolbar-btn {
    width: 36px;
    height: 36px;
    background: transparent;
    border: none;
    padding: 0;
    cursor: pointer;
    color: #3b4252; // 深灰色，确保在白色背景上清晰可见
    border-radius: var(--dt-radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-brand-color);
    }

    &:active {
      transform: scale(0.95);
    }

    &.active {
      color: var(--dt-brand-color);
      background: var(--dt-brand-bg);
    }

    .dark & {
      color: #bdc3c9; // 暗色模式下使用浅灰色，确保在深色背景上清晰可见

      &:hover {
        background: var(--dt-bg-hover-dark);
        color: var(--dt-brand-color);
      }
    }
  }

  .el-divider--vertical {
    height: 20px;
    margin: 0 4px;
    border-color: var(--dt-border-light);
  }

  .history-btn {
    font-size: 13px;
    color: var(--dt-text-tertiary);
    transition: color var(--dt-transition-fast);

    &:hover {
      color: var(--dt-brand-color);
    }

    .dark & {
      color: var(--dt-text-tertiary-dark);
    }
  }
}

// ============================================================================
// 引用/编辑预览
// ============================================================================
.reply-preview-container,
.edit-preview-container {
  padding: 10px 12px;
  margin-bottom: 12px;
  border-radius: var(--dt-radius-md);
  background: var(--dt-bg-body);
  border-left: 3px solid var(--dt-brand-color);
  transition: all var(--dt-transition-base);

  .dark & {
    background: var(--dt-bg-hover-dark);
  }

  .reply-content-box,
  .edit-content-box {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;

    .reply-user,
    .edit-label {
      color: var(--dt-brand-color);
      font-weight: 500;
      flex-shrink: 0;
    }

    .reply-text,
    .edit-text {
      flex: 1;
      color: var(--dt-text-secondary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;

      .dark & {
        color: var(--dt-text-secondary-dark);
      }
    }

    .cancel-reply,
    .cancel-edit {
      cursor: pointer;
      color: var(--dt-text-tertiary);
      flex-shrink: 0;
      transition: color var(--dt-transition-fast);

      &:hover {
        color: var(--dt-error-color);
      }

      .dark & {
        color: var(--dt-text-tertiary-dark);
      }
    }
  }
}

.edit-preview-container {
  border-left-color: var(--dt-success-color);

  .edit-label {
    color: var(--dt-success-color);
  }
}

// ============================================================================
// 输入区域
// ============================================================================
.input-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
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

  &::placeholder {
    color: var(--dt-text-quaternary);
  }

  .dark & {
    color: var(--dt-text-primary-dark);
  }
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

    .dark & {
      color: var(--dt-text-tertiary-dark);
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

    .dark & {
      background: var(--dt-bg-hover-dark);
    }
  }
}

// ============================================================================
// 响应式断点
// ============================================================================

// 超小屏幕 (< 480px)
@media (max-width: 479px) {
  .chat-input-container {
    padding: 8px 12px 12px;
  }

  .input-toolbar {
    padding-bottom: 8px;

    .toolbar-left {
      gap: 4px;

      .toolbar-btn {
        width: 32px;
        height: 32px;
      }
    }

    .history-btn {
      font-size: 12px;

      .el-icon {
        display: none;
      }
    }

    .el-divider--vertical {
      display: none;
    }
  }

  .reply-preview-container,
  .edit-preview-container {
    padding: 8px 10px;
    margin-bottom: 8px;

    .reply-content-box,
    .edit-content-box {
      font-size: 12px;
      gap: 6px;
    }
  }

  .message-input {
    font-size: 14px;
    min-height: 60px;
  }

  .input-footer {
    gap: 12px;
    margin-top: 6px;

    .hint-text {
      font-size: 11px;
    }

    .send-btn {
      padding: 7px 16px;
      font-size: 13px;
    }
  }
}

// 小屏幕 (480px - 767px)
@media (min-width: 480px) and (max-width: 767px) {
  .input-toolbar {
    .toolbar-left {
      gap: 5px;
    }

    .toolbar-btn {
      width: 34px;
      height: 34px;
    }
  }

  .reply-preview-container,
  .edit-preview-container {
    padding: 9px 11px;
  }

  .input-footer {
    gap: 14px;
  }
}

// 平板横屏 (768px - 1023px)
@media (min-width: 768px) and (max-width: 1023px) {
  .toolbar-btn {
    width: 35px;
    height: 35px;
  }
}
</style>
