<template>
  <div
    class="enhanced-input"
    :class="{ 'focused': isFocused, 'has-content': hasContent }"
  >
    <!-- 工具栏 -->
    <div
      v-if="showToolbar"
      class="input-toolbar"
    >
      <slot name="toolbar-start" />

      <button
        v-if="showEmojiButton"
        class="toolbar-btn emoji-btn"
        @click="toggleEmojiPicker"
        title="表情"
      >
        <i class="material-icons">mood</i>
      </button>

      <button
        v-if="showAtButton && isGroupChat"
        class="toolbar-btn at-btn"
        @click="handleAtClick"
        title="@提及"
      >
        <i class="material-icons">alternate_email</i>
      </button>

      <button
        v-if="showFileButton"
        class="toolbar-btn file-btn"
        @click="triggerFileUpload"
        title="发送文件"
      >
        <i class="material-icons">attach_file</i>
      </button>

      <button
        v-if="showImageButton"
        class="toolbar-btn image-btn"
        @click="triggerImageUpload"
        title="发送图片"
      >
        <i class="material-icons">image</i>
      </button>

      <slot name="toolbar-end" />
    </div>

    <!-- 表情选择器 -->
    <div
      v-show="showEmojiPickerInternal"
      class="emoji-picker-overlay"
      @click="hideEmojiPicker"
    >
      <div
        class="emoji-picker"
        @click.stop
      >
        <div class="emoji-categories">
          <button
            v-for="category in emojiCategories"
            :key="category.id"
            class="emoji-category-btn"
            :class="{ active: activeCategory === category.id }"
            @click="activeCategory = category.id"
          >
            {{ category.icon }}
          </button>
        </div>

        <div class="emoji-grid">
          <button
            v-for="emoji in getEmojisByCategory(activeCategory)"
            :key="emoji"
            class="emoji-item"
            @click="insertEmoji(emoji)"
          >
            {{ emoji }}
          </button>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-wrapper">
      <textarea
        ref="textareaRef"
        v-model="localValue"
        class="input-textarea"
        :placeholder="placeholder"
        :rows="minRows"
        :maxlength="maxLength"
        @input="handleInput"
        @keydown="handleKeydown"
        @focus="handleFocus"
        @blur="handleBlur"
        @paste="handlePaste"
      />

      <div class="input-actions">
        <button
          v-if="showVoiceButton"
          class="voice-btn"
          :class="{ 'recording': isRecording }"
          @mousedown="startRecording"
          @mouseup="stopRecording"
          @mouseleave="cancelRecording"
          title="按住说话"
        >
          <i class="material-icons">{{ isRecording ? 'mic' : 'mic_none' }}</i>
        </button>

        <button
          v-if="showSendButton"
          class="send-btn"
          :class="{ disabled: !canSend }"
          :disabled="!canSend"
          @click="handleSend"
          title="发送"
        >
          <i class="material-icons">send</i>
        </button>
      </div>
    </div>

    <!-- 提示信息 -->
    <div
      v-if="showHint && hintMessage"
      class="input-hint"
      :class="hintType"
    >
      {{ hintMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'

// Props
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '输入内容...'
  },
  minRows: {
    type: Number,
    default: 1
  },
  maxRows: {
    type: Number,
    default: 5
  },
  maxLength: {
    type: Number,
    default: 1000
  },
  showToolbar: {
    type: Boolean,
    default: true
  },
  showEmojiButton: {
    type: Boolean,
    default: true
  },
  showAtButton: {
    type: Boolean,
    default: false
  },
  isGroupChat: {
    type: Boolean,
    default: false
  },
  showFileButton: {
    type: Boolean,
    default: true
  },
  showImageButton: {
    type: Boolean,
    default: true
  },
  showVoiceButton: {
    type: Boolean,
    default: true
  },
  showSendButton: {
    type: Boolean,
    default: true
  },
  showHint: {
    type: Boolean,
    default: true
  },
  autoResize: {
    type: Boolean,
    default: true
  }
})

// Emits
const emit = defineEmits([
  'update:modelValue',
  'send',
  'paste',
  'file-upload',
  'image-upload',
  'voice-record',
  'at-click',
  'keydown'
])

// Refs
const textareaRef = ref(null)
const showEmojiPickerInternal = ref(false)
const isFocused = ref(false)
const isRecording = ref(false)
const activeCategory = ref('people')

// Local state
const localValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const hasContent = computed(() => !!props.modelValue.trim())

const canSend = computed(() => {
  return props.modelValue.trim().length > 0
})

// Emoji data
const emojiCategories = ref([
  { id: 'people', icon: '😊', name: '表情' },
  { id: 'nature', icon: '🐶', name: '动物' },
  { id: 'food', icon: '🍎', name: '食物' },
  { id: 'activity', icon: '⚽', name: '活动' },
  { id: 'travel', icon: '🚗', name: '旅行' },
  { id: 'objects', icon: '💡', name: '物品' }
])

const emojiMap = ref({
  people: ['😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇', '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚', '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩', '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣', '😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬', '🤯', '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓', '🤗', '🤔', '🤭', '🤫', '🤥', '😶', '😐', '😑', '😬', '🙄', '😯', '😦', '😧', '😮', '😲', '🥱', '😴', '🤤', '😪', '😵', '🤐', '🥴', '🤢', '🤮', '🤧', '😷', '🤒', '🤕', '🤑', '🤠', '😈', '👿', '👹', '👺', '🤡', '💩', '👻', '💀', '☠️', '👽', '👾', '🤖', '🎃', '😺', '😸', '😹', '😻', '😼', '😽', '🙀', '😿', '😾'],
  nature: ['🐶', '🐱', '🐭', '🐹', '🐰', '🦊', '🐻', '🐼', '🐨', '🐯', '🦁', '🐮', '🐷', '🐽', '🐸', '🐵', '🙈', '🙉', '🙊', '🐒', '🐔', '🐧', '🐦', '🐤', '🐣', '🐥', '🦆', '🦅', '🦉', '🦇', '🐺', '🐗', '🐴', '🦄', '🐝', '🐛', '🦋', '🐌', '🐞', '🐜', '🦟', '🦗', '🕷', '🕸', '🦂', '🐢', '🐍', '🦎', '🦖', '🦕', '🐙', '🦑', '🦐', '🦞', '🦀', '🐡', '🐠', '🐟', '🐬', '🐳', '🐋', '🦈', '🐊', '🐅', '🐆', '🦓', '🦍', '🦧', '🐘', '🦛', '🦏', '🐪', '🐫', '🦒', '🦘', '🐃', '🐂', '🐄', '🐎', '🐖', '🐏', '🐑', '🦙', '🐐', '🦌', '🐕', '🐩', '🦮', '🐕‍🦺', '🐈', '🐓', '🦃', '🦚', '🦜', '🦢', '🦩', '🕊', '🐇', '🦝', '🦨', '🦡', '🦦', '🦥', '🐁', '🐀', '🐿', '🦔'],
  food: ['🍏', '🍎', '🍐', '🍊', '🍋', '🍌', '🍉', '🍇', '🍓', '🫐', '🍈', '🍒', '🍑', '🥭', '🍍', '🥥', '🥝', '🍅', '🍆', '🥑', '🥦', '🥬', '🥒', '🌶', '🫑', '🌽', '🥕', '🫒', '🧄', '🧅', '🥔', '🍠', '🥐', '🥯', '🍞', '🥖', '🥨', '🧀', '🥚', '🍳', '🧈', '🥞', '🧇', '🥓', '🥩', '🍗', '🍖', '🦴', '🌭', '🍔', '🍟', '🍕', '🫓', '🥪', '🥙', '🧆', '🌮', '🌯', '🫔', '🥗', '🥘', '🫕', '🥫', '🍝', '🍜', '🍲', '🍛', '🍣', '🍱', '🥟', '🦪', '🍤', '🍙', '🍚', '🍘', '🍥', '🥠', '🥮', '🍢', '🍡', '🍧', '🍨', '🍦', '🥧', '🧁', '🍰', '🎂', '🍮', '🍭', '🍬', '🍫', '🍿', '🍩', '🍪', '🌰', '🥜', '🫘', '🍯', '🥛', '🍼', '🫖', '☕', '🍵', '🧃', '🥤', '🧋', '🍶', '🍺', '🍻', '🥂', '🍷', '🥃', '🍸', '🍹', '🧉', '🍾', '🧊', '🥄', '🍴', '🍽', '🥣', '🥡', '🥢']
})

// Computed
const hintMessage = computed(() => {
  if (props.modelValue.length > props.maxLength * 0.9) {
    return `还可以输入 ${props.maxLength - props.modelValue.length} 个字符`
  }
  return ''
})

const hintType = computed(() => {
  return props.modelValue.length > props.maxLength * 0.9 ? 'warning' : 'info'
})

// Methods
const handleInput = () => {
  if (props.autoResize) {
    autoResize()
  }
  // 通知父组件输入变化
}

const handleKeydown = (e) => {
  emit('keydown', e)
  // 处理发送快捷键
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    if (canSend.value) {
      handleSend()
    }
  }
}

const handleFocus = () => {
  isFocused.value = true
}

const handleBlur = () => {
  isFocused.value = false
}

const handleSend = () => {
  if (canSend.value) {
    emit('send', props.modelValue)
    // 发送后清空输入框
    emit('update:modelValue', '')
    nextTick(() => {
      autoResize()
    })
  }
}

const autoResize = () => {
  if (!textareaRef.value || !props.autoResize) return

  const ta = textareaRef.value
  ta.style.height = 'auto'

  const maxHeight = props.maxRows * 24 // 假设每行24px
  const scrollHeight = ta.scrollHeight

  if (scrollHeight > maxHeight) {
    ta.style.height = maxHeight + 'px'
    ta.style.overflowY = 'auto'
  } else {
    ta.style.height = scrollHeight + 'px'
    ta.style.overflowY = 'hidden'
  }
}

const toggleEmojiPicker = () => {
  showEmojiPickerInternal.value = !showEmojiPickerInternal.value
}

const hideEmojiPicker = () => {
  showEmojiPickerInternal.value = false
}

const insertEmoji = (emoji) => {
  const startPos = textareaRef.value?.selectionStart || localValue.value.length
  const newValue = localValue.value.substring(0, startPos) + emoji + localValue.value.substring(startPos)
  emit('update:modelValue', newValue)

  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.focus()
      textareaRef.value.setSelectionRange(startPos + 1, startPos + 1)
      autoResize()
    }
  })

  hideEmojiPicker()
}

const getEmojisByCategory = (categoryId) => {
  return emojiMap.value[categoryId] || emojiMap.value.people
}

const triggerFileUpload = () => {
  emit('file-upload')
}

const triggerImageUpload = () => {
  emit('image-upload')
}

const handleAtClick = () => {
  emit('at-click')
}

const handlePaste = (e) => {
  emit('paste', e)
}

const startRecording = () => {
  isRecording.value = true
  // 开始录音逻辑
}

const stopRecording = () => {
  if (isRecording.value) {
    // 结束录音逻辑
    emit('voice-record')
  }
  isRecording.value = false
}

const cancelRecording = () => {
  isRecording.value = false
  // 取消录音
}

// Lifecycle
onMounted(() => {
  nextTick(() => {
    autoResize()
  })
})

// Expose methods
defineExpose({
  focus: () => {
    textareaRef.value?.focus()
  },
  blur: () => {
    textareaRef.value?.blur()
  },
  insertText: (text) => {
    const startPos = textareaRef.value?.selectionStart || localValue.value.length
    const newValue = localValue.value.substring(0, startPos) + text + localValue.value.substring(startPos)
    emit('update:modelValue', newValue)
    nextTick(() => {
      autoResize()
    })
  },
  resize: autoResize
})
</script>

<style scoped>
.enhanced-input {
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  padding: 8px;
  transition: all 0.2s ease;
}

.enhanced-input.focused {
  border-color: var(--dt-brand-color);
  box-shadow: 0 0 0 2px var(--dt-brand-lighter);
}

.enhanced-input.has-content {
  border-color: var(--dt-border-light);
}

.input-toolbar {
  display: flex;
  gap: 8px;
  padding: 4px 0;
  border-bottom: 1px solid var(--dt-border-lighter);
  margin-bottom: 8px;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  color: var(--dt-text-secondary);
  transition: all 0.2s ease;
}

.toolbar-btn:hover {
  background: var(--dt-bg-hover);
  color: var(--dt-brand-color);
}

.emoji-picker-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  z-index: 1000;
  padding-top: 60px;
}

.emoji-picker {
  background: var(--dt-bg-card);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  width: 300px;
  max-height: 400px;
  display: flex;
  flex-direction: column;
}

.emoji-categories {
  display: flex;
  border-bottom: 1px solid var(--dt-border-lighter);
  padding: 8px;
}

.emoji-category-btn {
  flex: 1;
  border: none;
  background: transparent;
  padding: 8px;
  cursor: pointer;
  border-radius: 4px;
  font-size: 18px;
}

.emoji-category-btn.active {
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  padding: 8px;
  overflow-y: auto;
  flex: 1;
}

.emoji-item {
  border: none;
  background: transparent;
  font-size: 20px;
  cursor: pointer;
  border-radius: 4px;
  padding: 4px;
  transition: background 0.2s;
}

.emoji-item:hover {
  background: var(--dt-bg-hover);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.input-textarea {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  padding: 8px;
  background: transparent;
  color: var(--dt-text-primary);
  min-height: 32px;
  max-height: 120px;
  overflow-y: auto;
}

.input-textarea:focus {
  outline: none;
}

.input-actions {
  display: flex;
  gap: 4px;
}

.voice-btn, .send-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.voice-btn {
  background: var(--dt-bg-subtle);
  color: var(--dt-text-secondary);
}

.voice-btn:hover {
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
}

.voice-btn.recording {
  background: #f44336;
  color: white;
  animation: pulse 1s infinite;
}

.send-btn {
  background: var(--dt-brand-color);
  color: white;
}

.send-btn:hover:not(.disabled) {
  background: var(--dt-brand-hover);
}

.send-btn.disabled {
  background: var(--dt-border-light);
  color: var(--dt-text-quaternary);
  cursor: not-allowed;
}

.input-hint {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  margin-top: 4px;
}

.input-hint.info {
  color: var(--dt-text-tertiary);
}

.input-hint.warning {
  color: var(--dt-warning-color);
  background: var(--dt-warning-bg);
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

/* 滚动条样式 */
.input-textarea::-webkit-scrollbar {
  width: 6px;
}

.input-textarea::-webkit-scrollbar-track {
  background: transparent;
}

.input-textarea::-webkit-scrollbar-thumb {
  background: var(--dt-scrollbar-thumb);
  border-radius: 3px;
}

.input-textarea::-webkit-scrollbar-thumb:hover {
  background: var(--dt-scrollbar-thumb-hover);
}

/* 暗色模式 */
:global(.dark) {
  .enhanced-input {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .enhanced-input.focused {
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 2px var(--dt-brand-lighter);
  }

  .toolbar-btn:hover {
    background: var(--dt-bg-hover-dark);
    color: var(--dt-brand-color);
  }

  .emoji-picker {
    background: var(--dt-bg-card-dark);
    border: 1px solid var(--dt-border-dark);
  }

  .emoji-category-btn.active {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }

  .emoji-item:hover {
    background: var(--dt-bg-hover-dark);
  }

  .input-textarea {
    color: var(--dt-text-primary-dark);
    background: transparent;
  }

  .voice-btn {
    background: var(--dt-bg-subtle-dark);
    color: var(--dt-text-secondary-dark);
  }

  .voice-btn:hover {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }

  .input-hint.warning {
    background: var(--dt-warning-bg-dark);
  }

  .input-textarea::-webkit-scrollbar-thumb {
    background: var(--dt-scrollbar-thumb-dark);
  }

  .input-textarea::-webkit-scrollbar-thumb:hover {
    background: var(--dt-scrollbar-thumb-dark-hover);
  }
}
</style>