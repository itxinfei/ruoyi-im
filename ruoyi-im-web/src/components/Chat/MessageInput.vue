<template>
  <div 
    class="chat-input-wrapper" 
    :class="{ 'is-focused': isFocused, 'drag-over': isDragOver }"
    @dragover.prevent="handleDragOver"
    @dragleave.prevent="handleDragLeave"
    @drop.prevent="handleDrop"
  >
    <!-- 1. 回复/编辑预览区 -->
    <div v-if="replyingMessage || editingMessage" class="input-context-bar">
      <div class="context-content">
        <el-icon class="context-icon"><ChatLineSquare v-if="replyingMessage" /><Edit v-else /></el-icon>
        <span class="context-label">{{ replyingMessage ? '回复' : '编辑' }}:</span>
        <span class="context-text">{{ (replyingMessage || editingMessage).content }}</span>
      </div>
      <button class="context-close" @click="$emit(replyingMessage ? 'cancel-reply' : 'cancel-edit')">
        <el-icon><Close /></el-icon>
      </button>
    </div>

    <!-- 2. 工具栏 -->
    <div class="input-toolbar">
      <div class="toolbar-left">
        <el-popover v-model:visible="showEmojiPicker" trigger="click" placement="top-start" :width="320" effect="light">
          <template #reference>
            <button class="tool-btn" title="表情"><el-icon><Orange /></el-icon></button>
          </template>
          <div class="emoji-picker-content custom-scrollbar">
            <div class="emoji-grid">
              <span v-for="emoji in emojiList" :key="emoji" class="emoji-item" @click="insertEmoji(emoji)">{{ emoji }}</span>
            </div>
          </div>
        </el-popover>
        
        <button class="tool-btn" title="图片" @click="triggerImageUpload"><el-icon><Picture /></el-icon></button>
        <button class="tool-btn" title="文件" @click="triggerFileUpload"><el-icon><Files /></el-icon></button>
        <button class="tool-btn" title="语音" @click="showVoiceRecorder = true"><el-icon><Mic /></el-icon></button>
        <button class="tool-btn" title="视频通话" @click="$emit('start-video')"><el-icon><VideoCamera /></el-icon></button>
      </div>
      
      <div class="toolbar-right">
        <el-dropdown trigger="click" @command="handleShortcutChange">
          <span class="shortcut-hint">{{ shortcut === 'enter' ? 'Enter 发送' : 'Ctrl+Enter 发送' }}</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="enter">Enter 发送 / Ctrl+Enter 换行</el-dropdown-item>
              <el-dropdown-item command="ctrl-enter">Ctrl+Enter 发送 / Enter 换行</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 3. 输入区 -->
    <div class="input-area">
      <textarea
        ref="textareaRef"
        v-model="content"
        class="chat-textarea custom-scrollbar"
        :placeholder="placeholder"
        @focus="isFocused = true"
        @blur="isFocused = false"
        @keydown="handleKeyDown"
        @paste="handlePaste"
        @input="handleInput"
      ></textarea>
    </div>

    <!-- 4. 底部状态与按钮 -->
    <div class="input-footer">
      <div class="footer-left">
        <span v-if="content.length > 500" class="char-count" :class="{ error: content.length > 2000 }">
          {{ content.length }}/2000
        </span>
      </div>
      <div class="footer-right">
        <button 
          class="send-btn" 
          :class="{ 'is-active': canSend }" 
          :disabled="!canSend || sending"
          @click="send"
        >
          <span v-if="!sending">发送</span>
          <el-icon v-else class="is-loading"><Loading /></el-icon>
        </button>
      </div>
    </div>

    <!-- 隐藏的上传入口 -->
    <input type="file" ref="imageInput" hidden accept="image/*" @change="onImageFileChange" />
    <input type="file" ref="fileInput" hidden @change="onFileChange" />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { 
  ChatLineSquare, Edit, Close, Orange, Picture, 
  Files, Mic, VideoCamera, Loading 
} from '@element-plus/icons-vue'

const props = defineProps({
  session: Object,
  sending: Boolean,
  replyingMessage: Object,
  editingMessage: Object
})

const emit = defineEmits([
  'send', 'cancel-reply', 'cancel-edit', 'edit-confirm', 
  'upload-image', 'upload-file', 'typing'
])

const content = ref('')
const isFocused = ref(false)
const isDragOver = ref(false)
const showEmojiPicker = ref(false)
const shortcut = ref('enter')
const textareaRef = ref(null)
const imageInput = ref(null)
const fileInput = ref(null)

const canSend = computed(() => content.value.trim().length > 0)
const placeholder = computed(() => props.session?.name ? `发送给 ${props.session.name}` : '请输入消息...')

const emojiList = ['😀','😃','😄','😁','😆','😅','😂','🤣','😊','😇','🙂','🙃','😉','😌','😍','🥰','😘','😗','😙','😚','😋','😛','😝','😜','🤪','🤨','🧐','🤓','😎','🤩','🥳','😏','😒','😞','😔','😟','😕','🙁','☹️','😮','😯','😲','😳','🥺','😦','😧','😨','😰','😥','😢','😭','😱','😖','😣','😞','😓','😩','😫','🥱','😤','😡','😠','🤬','😈','👿','💀','☠️','💩','🤡','👹','👺','👻','👽','👾','🤖']

const send = () => {
  if (!canSend.value || props.sending) return
  const payload = { content: content.value.trim(), type: 'TEXT' }
  if (props.editingMessage) emit('edit-confirm', { ...payload, id: props.editingMessage.id })
  else emit('send', payload)
  content.value = ''
}

const handleKeyDown = (e) => {
  if (e.key === 'Enter') {
    const isSendShortcut = (shortcut.value === 'enter' && !e.ctrlKey) || (shortcut.value === 'ctrl-enter' && e.ctrlKey)
    if (isSendShortcut) {
      e.preventDefault(); send()
    }
  }
}

const insertEmoji = (emoji) => {
  content.value += emoji
  showEmojiPicker.value = false
  textareaRef.value?.focus()
}

const triggerImageUpload = () => imageInput.value?.click()
const triggerFileUpload = () => fileInput.value?.click()
const onImageFileChange = (e) => { if (e.target.files[0]) emit('upload-image', e.target.files[0]) }
const onFileChange = (e) => { if (e.target.files[0]) emit('upload-file', e.target.files[0]) }

const handlePaste = (e) => {
  const items = e.clipboardData?.items
  for (const item of items) {
    if (item.type.indexOf('image') !== -1) {
      const file = item.getAsFile()
      if (file) { emit('upload-image', file); e.preventDefault() }
    }
  }
}

const handleInput = () => { if (content.value.length > 0) emit('typing', true) }
const handleShortcutChange = (cmd) => { shortcut.value = cmd }
const handleDragOver = () => isDragOver.value = true
const handleDragLeave = () => isDragOver.value = false
const handleDrop = (e) => { isDragOver.value = false; const files = e.dataTransfer?.files; if (files?.length) emit('upload-file', files[0]) }

watch(() => props.editingMessage, (val) => { if (val) { content.value = val.content; textareaRef.value?.focus() } })
defineExpose({ focus: () => textareaRef.value?.focus() })
</script>

<style scoped lang="scss">
// ============================================================================
// 输入框容器 - 呼吸感控制
// ============================================================================
.chat-input-wrapper {
  background: var(--dt-bg-input);
  border-top: 1px solid var(--dt-border-light);
  display: flex;
  flex-direction: column;
  padding: 0 var(--dt-spacing-lg) var(--dt-spacing-sm);
  transition: all var(--dt-transition-base);
  position: relative;

  &.is-focused {
    background: #fff;
    box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.02);
  }

  &.drag-over {
    background: var(--dt-brand-lighter);
    border-top-color: var(--dt-brand-color);
  }
}

// ============================================================================
// 上下文提示条 (回复/编辑)
// ============================================================================
.input-context-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--dt-spacing-sm) var(--dt-spacing-md);
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-sm);
  margin-top: var(--dt-spacing-sm);
  
  .context-content {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-secondary);
    min-width: 0;

    .context-icon { color: var(--dt-brand-color); }
    .context-label { font-weight: 600; flex-shrink: 0; }
    .context-text { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; opacity: 0.8; }
  }

  .context-close {
    background: none;
    border: none;
    padding: 0;
    cursor: pointer;
    color: var(--dt-text-tertiary);
    &:hover { color: var(--dt-error-color); }
  }
}

// ============================================================================
// 工具栏
// ============================================================================
.input-toolbar {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .toolbar-left, .toolbar-right {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-sm);
  }

  .tool-btn {
    width: 32px;
    height: 32px;
    background: none;
    border: none;
    padding: 0;
    cursor: pointer;
    border-radius: var(--dt-radius-sm);
    color: var(--dt-text-secondary);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-session-hover);
      color: var(--dt-brand-color);
    }

    .el-icon { font-size: 20px; }
  }

  .shortcut-hint {
    font-size: var(--dt-font-size-xs);
    color: var(--dt-text-quaternary);
    cursor: pointer;
    &:hover { color: var(--dt-text-tertiary); }
  }
}

// ============================================================================
// 输入区
// ============================================================================
.input-area {
  flex: 1;
  padding: var(--dt-spacing-xs) 0;

  .chat-textarea {
    width: 100%;
    height: 100px;
    border: none;
    outline: none;
    resize: none;
    font-size: var(--dt-font-size-base);
    line-height: var(--dt-line-height-base);
    color: var(--dt-text-primary);
    background: transparent;
    font-family: inherit;
    &::placeholder { color: var(--dt-text-quaternary); }
  }
}

// ============================================================================
// 页脚与发送按钮
// ============================================================================
.input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 36px;

  .char-count {
    font-size: var(--dt-font-size-xs);
    color: var(--dt-text-quaternary);
    &.error { color: var(--dt-error-color); }
  }

  .send-btn {
    height: 32px;
    padding: 0 20px;
    border: none;
    border-radius: var(--dt-radius-sm);
    background: var(--dt-border-light);
    color: var(--dt-text-quaternary);
    font-size: var(--dt-font-size-sm);
    font-weight: var(--dt-font-weight-medium);
    cursor: not-allowed;
    transition: all var(--dt-transition-base);
    display: flex;
    align-items: center;
    justify-content: center;

    &.is-active {
      background: var(--dt-brand-color);
      color: #ffffff;
      cursor: pointer;
      &:hover { background: var(--dt-brand-hover); box-shadow: var(--dt-shadow-2); }
      &:active { transform: scale(0.96); }
    }
  }
}

// 表情选择器微调
.emoji-picker-content {
  max-height: 200px;
  overflow-y: auto;
  .emoji-grid {
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 8px;
    padding: 8px;
    .emoji-item {
      font-size: 22px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: var(--dt-radius-sm);
      &:hover { background: var(--dt-bg-session-hover); }
    }
  }
}
</style>
