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
      <textarea
        ref="textareaRef"
        v-model="messageContent"
        class="message-input"
        :placeholder="session?.type === 'GROUP' ? '发消息...' : '发消息...'"
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
import { Close, ChatDotRound, Picture, FolderOpened, Phone, Clock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import EmojiPicker from '@/components/EmojiPicker/index.vue'
import AtMemberPicker from './AtMemberPicker.vue'

const props = defineProps({
  session: Object,
  sending: Boolean,
  replyingMessage: Object,
  editingMessage: Object
})

const emit = defineEmits(['send', 'upload-image', 'upload-file', 'cancel-reply', 'cancel-edit', 'edit-confirm', 'input', 'start-call', 'start-video'])

const store = useStore()
const messageContent = ref('')
const sendShortcutHint = computed(() => {
  const shortcut = store.state.im.settings.shortcuts.send
  return shortcut === 'ctrl-enter' ? '按 Ctrl + Enter 发送' : '按 Enter 发送'
})

const showEmojiPicker = ref(false)
const textareaRef = ref(null)
const atMemberPickerRef = ref(null)

// 钉钉风格高度逻辑
const containerHeight = ref(200)
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
  tx.style.height = tx.scrollHeight + 'px'
}

const handleInput = () => {
  autoResize()
  emit('input', messageContent.value)
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
  background: var(--dt-bg-input);
  display: flex;
  flex-direction: column;
  position: relative;
  border-top: 1px solid #f0f1f2;
  padding: 4px 16px 16px;
  .dark & { border-top-color: #334155; }
}

.resize-handle {
  position: absolute; top: 0; left: 0; right: 0; height: 4px; cursor: ns-resize;
  z-index: 10; &:hover { background: rgba(0, 137, 255, 0.1); }
}

.input-toolbar {
  display: flex; justify-content: space-between; align-items: center; padding-bottom: 8px;
  .toolbar-left { display: flex; align-items: center; gap: 4px; }
  .toolbar-btn {
    background: none; border: none; padding: 6px; cursor: pointer; color: #646a73;
    border-radius: 6px; display: flex; align-items: center; justify-content: center;
    transition: all 0.2s;
    &:hover { background: #f2f3f5; color: var(--dt-brand-color); }
    &.active { color: var(--dt-brand-color); background: #e6f7ff; }
    .el-icon, .material-icons-outlined { font-size: 19px; }
    .dark & { &:hover { background: #334155; } }
  }
  .history-btn { font-size: 13px; color: #8f959e; &:hover { color: var(--dt-brand-color); } }
}

.reply-preview-container, .edit-preview-container {
  padding: 8px 12px; margin-bottom: 8px; border-radius: 8px;
  background: #f8fafc; border-left: 3px solid var(--dt-brand-color);
  .dark & { background: rgba(30, 41, 59, 0.5); }
  
  .reply-content-box, .edit-content-box {
    display: flex; align-items: center; gap: 8px; font-size: 13px;
    .reply-user, .edit-label { color: var(--dt-brand-color); font-weight: 500; }
    .reply-text, .edit-text { flex: 1; color: #64748b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .cancel-reply, .cancel-edit { cursor: pointer; color: #8f959e; &:hover { color: var(--dt-error-color); } }
  }
}

.edit-preview-container { border-left-color: var(--dt-success-color); .edit-label { color: var(--dt-success-color); } }

.input-area {
  flex: 1; display: flex; flex-direction: column;
}

.message-input {
  flex: 1; width: 100%; border: none; outline: none; resize: none;
  font-size: 15px; line-height: 1.6; color: #1f2329; padding: 0; min-height: 80px;
  background: transparent;
  .dark & { color: #f1f5f9; }
  &::placeholder { color: #bbbfc4; }
}

.input-footer {
  display: flex; justify-content: flex-end; align-items: center; gap: 16px; margin-top: 8px;
  .hint-text { font-size: 12px; color: #8f959e; user-select: none; }
  .send-btn {
    padding: 6px 24px; border-radius: 4px; border: none; background: #f2f3f5; color: #bbbfc4; 
    font-size: 14px; cursor: default; transition: all 0.2s;
    &.active { background: var(--dt-brand-color); color: #fff; cursor: pointer; &:hover { opacity: 0.9; } }
    &:disabled { opacity: 0.6; cursor: not-allowed; }
  }
}
</style>
