<template>
  <div
    class="chat-input-wrapper"
    :class="{ 'is-dragover': isDragover }"
    @dragover.prevent="handleDragOver"
    @dragleave.prevent="handleDragLeave"
    @drop.prevent="handleDrop"
  >
    <div class="chat-input-box" :class="{ 'is-focused': isFocused }">
      <!-- 1. 富文本编辑区 -->
      <div class="editor-zone">
          <div
            ref="editorRef"
            class="chat-rich-editor"
            contenteditable="true"
          placeholder="给同事发送消息..."
          @focus="isFocused = true"
          @blur="isFocused = false"
          @input="handleInput"
          @keydown.enter.exact.prevent="handleKeydownEnter"
        />
      </div>

      <!-- 2. 底部工具栏与发送区 -->
      <div class="toolbar-zone">
        <div class="tools-left">
          <button class="tool-btn" title="文件上传" @click="handleAttach"><el-icon><Upload /></el-icon></button>
          <button class="tool-btn" title="截图" @click="handleScreenshot"><el-icon><Crop /></el-icon></button>
          <div class="sep" />
          <button class="tool-btn" title="表情" @click="handleEmoji"><el-icon><MagicStick /></el-icon></button>
          <button class="tool-btn" title="提及" @click="handleMention"><el-icon><User /></el-icon></button>
        </div>

        <div class="tools-right">
          <span class="char-count" v-if="charCount > 0">{{ charCount }}/2000</span>
          <button
            class="chat-send-btn"
            :class="{ 'can-send': canSend, 'is-loading': isSending }"
            :disabled="!canSend"
            @click="executeSendMessage"
          >
            <el-icon v-if="!isSending"><Promotion /></el-icon>
            <el-icon v-else class="loading-icon"><Loading /></el-icon>
            <span class="btn-text">{{ isSending ? '发送中...' : '发送' }}</span>
          </button>
        </div>
      </div>
    </div>

    <div class="chat-footer-tip">
      <span v-if="!canSend"><kbd>Ctrl</kbd> + <kbd>Enter</kbd> 换行</span>
      <span v-else><kbd>Enter</kbd> 发送，<kbd>Ctrl</kbd> + <kbd>Enter</kbd> 换行</span>
    </div>

    <!-- 拖拽浮层 -->
    <transition name="fade">
      <div v-if="isDragover" class="drag-overlay">
        <div class="overlay-inner">
          <el-icon><UploadFilled /></el-icon>
          <p>释放文件以发送</p>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="js">
import { ref, computed, onMounted, watch } from 'vue'
import { Upload, Crop, MagicStick, User, Promotion, UploadFilled, Loading } from '@element-plus/icons-vue'

const emit = defineEmits([
  'send',
  'update:modelValue',
  'attach',
  'attach-click',
  'emoji-click',
  'mention-click',
  'link-click',
  'typing'
])

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const editorRef = ref(null)
const isFocused = ref(false)
const hasContent = ref(false)
const isDragover = ref(false)
const isSending = ref(false)

const canSend = computed(() => hasContent.value && !isSending.value)
const charCount = computed(() => editorRef.value?.innerText?.length || 0)

// 监听外部 modelValue 变化，同步到编辑器
watch(() => props.modelValue, (newVal) => {
  if (editorRef.value && editorRef.value.innerText !== newVal) {
    editorRef.value.innerText = newVal || ''
    hasContent.value = !!newVal?.trim()
  }
})

const handleInput = () => {
  const text = editorRef.value?.innerText.trim() || ''
  hasContent.value = !!text
  emit('update:modelValue', text)
  // 发送输入中状态
  if (text.length > 0) {
    emit('typing', true)
  }
}

const handleKeydownEnter = (e) => {
  if (e.ctrlKey || e.metaKey) {
    e.preventDefault()
    document.execCommand('insertHTML', false, '<br>')
    handleInput()
  } else {
    executeSendMessage()
  }
}

const executeSendMessage = () => {
  if (!canSend.value) return
  
  isSending.value = true
  
  const content = editorRef.value?.innerText.trim()
  if (content) {
    emit('send', content)
    editorRef.value.innerHTML = ''
    hasContent.value = false
    emit('update:modelValue', '')
  }
  
  setTimeout(() => {
    isSending.value = false
  }, 1000)
}

const handleDragOver = () => { isDragover.value = true }
const handleDragLeave = () => { isDragover.value = false }
const handleDrop = (e) => {
  isDragover.value = false
  const files = e.dataTransfer?.files
  if (files?.length) {
    emit('attach', files)
  }
}

// 工具栏功能
const handleAttach = () => { emit('attach-click') }
const handleEmoji = () => { emit('emoji-click') }
const handleMention = () => { emit('mention-click') }
// 注：当前"截图"按钮临时复用插入链接功能，待截图功能实现后切换为 screenshot-click
const handleScreenshot = () => { emit('link-click') }

// 插入文本到光标位置（供父组件调用）
const insertText = (text) => {
  const editor = editorRef.value
  if (!editor) return

  editor.focus()
  const selection = window.getSelection()
  if (selection?.rangeCount) {
    const range = selection.getRangeAt(0)
    range.deleteContents()
    range.insertNode(document.createTextNode(text))
    range.collapse(false)
  } else {
    document.execCommand('insertText', false, text)
  }

  // 触发 input 事件更新状态
  handleInput()
}

onMounted(() => editorRef.value?.focus())

// 暴露方法给父组件调用
defineExpose({
  insertText
})
</script>

<style scoped lang="scss">
// ============================================================================
// 聊天输入区 - 钉钉 8.2 规范
// ============================================================================

.chat-input-wrapper {
  padding: 0 var(--dt-spacing-xl) var(--dt-spacing-lg);
  background: var(--dt-bg-card);
  position: relative;
  display: flex;
  flex-direction: column;
}

.chat-input-box {
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-lg);
  background: var(--dt-bg-card);
  display: flex;
  flex-direction: column;
  transition: border-color 0.2s, box-shadow 0.2s;
  overflow: hidden;

  &.is-focused {
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 3px var(--dt-brand-lighter);
  }
}

// ============================================================================
// 编辑区
// ============================================================================

.editor-zone {
  padding: 12px 0;
  min-height: var(--dt-input-min-height);
  max-height: var(--dt-input-max-height);
  overflow-y: auto;
}

.chat-rich-editor {
  width: 100%;
  height: 100%;
  outline: none;
  font-size: var(--dt-font-size-base);
  line-height: 1.7;
  color: var(--dt-text-primary);
  word-break: break-all;
  padding: 0 var(--dt-spacing-xl);

  &:empty::before {
    content: attr(placeholder);
    color: var(--dt-text-tertiary);
    pointer-events: none;
  }
}

// ============================================================================
// 工具栏
// ============================================================================

.toolbar-zone {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--dt-toolbar-height);
  padding: 0 var(--dt-spacing-lg);
  background: var(--dt-bg-input);
  border-top: 1px solid var(--dt-border-lighter);
}

.tools-left {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
}

.sep {
  width: 1px;
  height: 16px;
  background: var(--dt-border-color);
  margin: 0 var(--dt-spacing-xs);
}

.tool-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--dt-text-secondary);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--dt-toolbar-icon-size);
  transition: var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-primary);
  }
}

// ============================================================================
// 发送按钮 - 钉钉规范
// ============================================================================

.chat-send-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 32px;
  padding: 0 12px;
  border-radius: var(--dt-radius-sm);
  border: none;
  background: var(--dt-send-btn-empty-bg);
  color: var(--dt-text-white);
  font-size: var(--dt-font-size-sm);
  font-weight: 500;
  transition: all 0.2s;
  cursor: not-allowed;

  .btn-text {
    display: none;
  }

  &.can-send {
    background: var(--dt-brand-color);
    color: var(--dt-text-white);
    cursor: pointer;

    .btn-text {
      display: inline;
    }

    &:hover {
      background: var(--dt-brand-hover);
      box-shadow: var(--dt-shadow-brand);
    }
  }

  &.is-loading {
    pointer-events: none;
    opacity: 0.7;

    .loading-icon {
      animation: spin 1s linear infinite;
    }
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.char-count {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);
  margin-right: var(--dt-spacing-sm);
}

// ============================================================================
// 底部提示
// ============================================================================

.chat-footer-tip {
  text-align: right;
  margin-top: var(--dt-spacing-sm);
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  padding-right: var(--dt-spacing-xs);

  kbd {
    display: inline-block;
    padding: 2px 8px;
    font-size: var(--dt-font-size-xs);
    font-family: inherit;
    background: var(--dt-bg-input);
    border: 1px solid var(--dt-border-light);
    border-radius: var(--dt-radius-sm);
    margin: 0 var(--dt-spacing-xs);
  }
}

// ============================================================================
// 拖拽浮层
// ============================================================================

.drag-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.95);
  z-index: var(--dt-z-sticky);
  display: flex;
  align-items: center;
  justify-content: center;
}

.overlay-inner {
  border: 2px dashed var(--dt-brand-color);
  border-radius: var(--dt-radius-xl);
  padding: var(--dt-spacing-3xl);
  color: var(--dt-brand-color);
  text-align: center;
  font-weight: 600;
  background: var(--dt-brand-bg);

  .el-icon {
    font-size: 48px;
    margin-bottom: var(--dt-spacing-md);
  }
}
</style>
