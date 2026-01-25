<template>
  <div class="chat-input-container" :style="{ minHeight: containerHeight + 'px' }">
    <div 
      class="resize-handle" 
      @mousedown="startResize"
      @dblclick="resetHeight"
    >
      <div class="resize-indicator"></div>
    </div>

    <div class="input-toolbar">
      <div class="toolbar-left">
        <el-tooltip content="表情" placement="top" :show-after="500">
          <button class="toolbar-btn" :class="{ active: showEmojiPicker }" @click.stop="toggleEmojiPicker">
            <el-icon><ChatDotRound /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="图片" placement="top" :show-after="500">
          <button class="toolbar-btn" @click="$emit('upload-image')">
            <el-icon><Picture /></el-icon>
          </button>
        </el-tooltip>
        
        <el-tooltip content="文件" placement="top" :show-after="500">
          <button class="toolbar-btn" @click="$emit('upload-file')">
            <el-icon><FolderOpened /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="截图" placement="top" :show-after="500">
          <button class="toolbar-btn" @click="handleScreenshot">
            <el-icon><Scissor /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip v-if="session?.type === 'GROUP'" content="@成员" placement="top" :show-after="500">
          <button class="toolbar-btn" @click="handleAtMember">
            <span class="material-icons-outlined">alternate_email</span>
          </button>
        </el-tooltip>

        <el-tooltip content="语音消息" placement="top" :show-after="500">
          <button class="toolbar-btn" @click="handleVoiceRecord">
            <el-icon><Microphone /></el-icon>
          </button>
        </el-tooltip>
      </div>

      <div class="toolbar-right" v-if="session?.type === 'PRIVATE'">
        <el-tooltip content="语音通话" placement="top" :show-after="500">
          <button class="toolbar-btn" @click="$emit('start-call')">
            <el-icon><Phone /></el-icon>
          </button>
        </el-tooltip>
        <el-tooltip content="视频通话" placement="top" :show-after="500">
          <button class="toolbar-btn" @click="$emit('start-video')">
            <el-icon><VideoCamera /></el-icon>
          </button>
        </el-tooltip>
      </div>
    </div>

    <!-- 预览区域 (回复/编辑) -->
    <div v-if="replyingMessage" class="preview-bar reply">
      <span class="preview-label">{{ replyingMessage.senderName }}: </span>
      <span class="preview-text">{{ replyingMessage.content }}</span>
      <el-icon class="close-icon" @click="$emit('cancel-reply')"><Close /></el-icon>
    </div>
    <div v-if="editingMessage" class="preview-bar edit">
      <span class="preview-label">正在编辑: </span>
      <span class="preview-text">{{ editingMessage.content }}</span>
      <el-icon class="close-icon" @click="$emit('cancel-edit')"><Close /></el-icon>
    </div>

    <!-- 输入核心区域 -->
    <div class="input-area">
      <textarea
        ref="textareaRef"
        v-model="message"
        class="message-input"
        :placeholder="session?.type === 'GROUP' ? '发消息' : '发消息'"
        @input="handleInput"
        @keydown="handleKeydown"
        @paste="handlePaste"
        @drop.prevent="handleDrop"
        @dragover.prevent
      ></textarea>
      
      <div class="input-footer">
        <span class="hint-text">{{ sendShortcutHint }}</span>
        <button 
          class="send-btn" 
          :class="{ active: canSend }" 
          :disabled="!canSend || sending"
          @click="handleSend"
        >发送</button>
      </div>
    </div>

    <EmojiPicker v-if="showEmojiPicker" @select="selectEmoji" ref="emojiPickerRef" />
    <AtMemberPicker ref="atMemberPickerRef" :session-id="session?.id" @select="onAtSelect" />
  </div>
</template>

<script setup>
import { ref, nextTick, computed, onUnmounted, watch, onMounted } from 'vue'
import { useStore } from 'vuex'
import { Close, ChatDotRound, Picture, FolderOpened, Scissor, Microphone, Phone, VideoCamera } from '@element-plus/icons-vue'
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
const message = ref('')
const sendShortcutHint = computed(() => {
  const shortcut = store.state.im.settings.shortcuts.send
  return shortcut === 'ctrl-enter' ? 'Ctrl + Enter 发送，Enter 换行' : 'Enter 发送，Shift + Enter 换行'
})
const showEmojiPicker = ref(false)
const textareaRef = ref(null)
const atMemberPickerRef = ref(null)
const emojiPickerRef = ref(null)

// 釘釘风格高度逻辑
const containerHeight = ref(200)
const minHeight = 160
const maxHeight = 600
let isResizing = false
let startY = 0
let startHeight = 0

const startResize = (e) => {
  isResizing = true; startY = e.clientY; startHeight = containerHeight.value
  document.addEventListener('mousemove', handleResize); document.addEventListener('mouseup', stopResize)
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
  document.removeEventListener('mousemove', handleResize); document.removeEventListener('mouseup', stopResize)
}

const resetHeight = () => { containerHeight.value = 200 }

const insertAt = (nickname) => {
  const atText = `@${nickname} `; const pos = textareaRef.value?.selectionStart || message.value.length
  message.value = message.value.slice(0, pos) + atText + message.value.slice(pos)
  nextTick(() => { textareaRef.value?.focus(); autoResize() })
}
defineExpose({ insertAt })

const canSend = computed(() => message.value.trim().length > 0)

const autoResize = () => {
  const tx = textareaRef.value
  if (!tx) return
  tx.style.height = 'auto'
  // textarea 填充 input-area，input-area 填充 container
  // 我们不需要在这里强行改 tx 高度到 px，除非它超出了容器。
  // 但为了实现输入时自动撑开容器（钉钉效果），我们需要同步 containerHeight
  const contentH = tx.scrollHeight
  if (contentH > 100 && contentH < 400) {
     // 允许容器随文字略微长高，但尊重用户的 resize 设定
  }
}

const handleInput = (e) => { autoResize(); emit('input', message.value) }

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
  if (!canSend.value) return
  if (props.editingMessage) emit('edit-confirm', message.value.trim())
  else emit('send', message.value.trim())
  message.value = ''
  nextTick(() => { textareaRef.value.style.height = 'auto'; textareaRef.value.focus() })
}

const handlePaste = (e) => {
  const items = e.clipboardData?.items
  if (!items) return

  // 优先处理文件/图片
  let hasFile = false
  for (const item of items) {
    if (item.kind === 'file') {
      const file = item.getAsFile()
      if (file) {
        hasFile = true
        e.preventDefault() // 阻止默认粘贴行为（如粘贴文件名）
        const type = file.type.startsWith('image/') ? 'IMAGE' : 'FILE'
        uploadAndSend(file, type)
      }
    }
  }
  
  // 如果没有文件，允许默认粘贴（文本）
}

const handleDrop = (e) => {
  const files = e.dataTransfer?.files
  if (!files || files.length === 0) return
  
  for (const file of files) {
    const type = file.type.startsWith('image/') ? 'IMAGE' : 'FILE'
    uploadAndSend(file, type)
  }
}

const uploadAndSend = async (file, type) => {
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    if (type === 'IMAGE') {
      emit('upload-image', formData)
    } else {
      emit('upload-file', formData)
    }
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const toggleEmojiPicker = () => { showEmojiPicker.value = !showEmojiPicker.value }
const selectEmoji = (emoji) => { 
  const tx = textareaRef.value
  const pos = tx?.selectionStart || message.value.length
  message.value = message.value.slice(0, pos) + emoji + message.value.slice(pos)
  showEmojiPicker.value = false
  nextTick(() => { tx?.focus(); autoResize() })
}
const onAtSelect = (m, p) => { insertAt(m.nickname || m.username) }

const handleScreenshot = () => {
  ElMessage.info('快捷截图功能暂未启用 (建议使用 Alt+A 钉钉快捷键)')
}

const handleVoiceRecord = () => {
  ElMessage.info('语音录制功能开发中...')
}

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
  border-top: 1px solid #f0f0f0;
  padding: 0 16px 12px;
  min-height: 160px;
}

.resize-handle {
  position: absolute; top: 0; left: 0; right: 0; height: 3px; cursor: ns-resize;
  z-index: 10; transition: background 0.2s;
  &:hover { background: rgba(0, 137, 255, 0.4); }
}

.input-toolbar {
  .toolbar-left, .toolbar-right { display: flex; align-items: center; gap: 4px; }
  .toolbar-btn {
    background: none; border: none; padding: 8px; cursor: pointer; color: #646a73;
    border-radius: 6px; display: flex; align-items: center; justify-content: center;
    transition: all 0.2s;
    &:hover { background: #f2f3f5; color: #1677ff; }
    &.active { color: #1677ff; background: #e0f2fe; }
    .el-icon { font-size: 20px; }
    .material-icons-outlined { font-size: 20px; }
  }
}

.preview-bar {
  background: #f3f3f3; padding: 6px 12px; border-radius: 4px; margin-bottom: 8px;
  display: flex; align-items: center; gap: 8px; border-left: 3px solid #1677ff;
  font-size: 13px; .preview-text { flex: 1; color: #666; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .close-icon { cursor: pointer; color: #999; &:hover { color: #f54a45; } }
  &.edit { border-left-color: #52c41a; }
}

.input-area {
  flex: 1; display: flex; flex-direction: column;
}

.message-input {
  flex: 1; width: 100%; border: none; outline: none; resize: none;
  font-size: 15px; line-height: 1.6; color: #1f2329; padding: 4px 0;
  min-height: 80px; 
}

.input-footer {
  display: flex; justify-content: flex-end; align-items: center; gap: 16px; margin-top: 8px;
  .hint-text { font-size: 12px; color: #8f959e; }
  .send-btn {
    padding: 6px 20px; border-radius: 4px; border: none; background: #f2f3f5; color: #bbbfc4; 
    cursor: default; transition: all 0.2s;
    &.active { background: #1677ff; color: #fff; cursor: pointer; &:hover { background: #4096ff; } }
  }
}

:global(.dark) {
  .chat-input-container { background: #1e293b; border-color: #334155; }
  .preview-bar { background: #334155; .preview-text { color: #cbd5e1; } }
  .message-input { background: transparent; color: #f1f5f9; }
  .input-footer .send-btn { background: #334155; color: #64748b; &.active { background: #3b82f6; color: #fff; } }
}
</style>
