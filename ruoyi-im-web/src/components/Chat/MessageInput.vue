<template>
  <div class="chat-input-container">
    <!-- 回复预览 -->
    <div v-if="replyingMessage" class="reply-preview" role="region" aria-label="回复消息预览">
      <div class="reply-indicator"></div>
      <div class="reply-content">
        <div class="reply-header">
          <span class="reply-user">{{ replyingMessage.senderName }}</span>
          <span class="reply-time">{{ formatReplyTime(replyingMessage.time) }}</span>
        </div>
        <div class="reply-text">{{ replyingMessage.content }}</div>
      </div>
      <button class="close-btn" @click="$emit('cancel-reply')" aria-label="取消回复">
        <span class="material-icons-outlined" aria-hidden="true">close</span>
      </button>
    </div>

    <!-- 工具栏 -->
    <div class="input-toolbar" role="toolbar" aria-label="消息输入工具栏">
      <button
        class="toolbar-btn"
        :class="{ active: showEmojiPicker }"
        @click.stop="toggleEmojiPicker"
        aria-label="表情"
        title="表情"
      >
        <span class="material-icons-outlined" aria-hidden="true">sentiment_satisfied</span>
      </button>
      <button
        class="toolbar-btn"
        @click="$emit('upload-image')"
        aria-label="图片"
        title="图片"
      >
        <span class="material-icons-outlined" aria-hidden="true">image</span>
      </button>
      <button
        class="toolbar-btn"
        @click="$emit('upload-file')"
        aria-label="文件"
        title="文件"
      >
        <span class="material-icons-outlined" aria-hidden="true">folder_open</span>
      </button>
      <button
        v-if="session?.type === 'GROUP'"
        class="toolbar-btn"
        @click="handleAtMember"
        aria-label="@成员"
        title="@成员"
      >
        <span class="material-icons-outlined" aria-hidden="true">alternate_email</span>
      </button>
      <div class="toolbar-spacer"></div>
      <button
        class="toolbar-btn"
        @click="$emit('start-call')"
        aria-label="语音通话"
        title="语音通话"
      >
        <span class="material-icons-outlined" aria-hidden="true">phone</span>
      </button>
      <button
        class="toolbar-btn"
        @click="$emit('start-video')"
        aria-label="视频通话"
        title="视频通话"
      >
        <span class="material-icons-outlined" aria-hidden="true">videocam</span>
      </button>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <textarea
        v-model="message"
        class="message-input"
        :placeholder="session?.type === 'GROUP' ? '发消息' : '发消息'"
        rows="1"
        @keydown="handleKeydown"
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
        ref="textareaRef"
        :aria-label="session?.type === 'GROUP' ? '输入群消息' : '输入消息'"
      ></textarea>

      <!-- 发送按钮 -->
      <button
        class="send-btn"
        :class="{
          disabled: !canSend,
          'with-text': message.trim().length > 0
        }"
        :disabled="!canSend || sending"
        @click="handleSend"
        :aria-label="sending ? '发送中...' : '发送消息'"
      >
        <span v-if="message.trim().length > 0" class="send-text">发送</span>
        <span v-else class="material-icons-outlined send-icon" aria-hidden="true">add_circle</span>
      </button>
    </div>

    <!-- 底部提示 -->
    <div v-if="isFocused" class="input-hint">
      按 Enter 发送，Shift + Enter 换行，↑↓ 浏览历史
    </div>

    <!-- Emoji选择器 -->
    <EmojiPicker
      v-if="showEmojiPicker"
      @select="selectEmoji"
      @click.stop
      ref="emojiPickerRef"
    />

    <!-- @成员选择器 -->
    <AtMemberPicker
      ref="atMemberPickerRef"
      :session-id="session?.id"
      @select="onAtSelect"
    />
  </div>
</template>

<script setup>
import { ref, nextTick, computed, onUnmounted, watch, onMounted } from 'vue'
import EmojiPicker from '@/components/EmojiPicker/index.vue'
import AtMemberPicker from './AtMemberPicker.vue'

const props = defineProps({
  session: Object,
  sending: Boolean,
  replyingMessage: Object
})

const emit = defineEmits([
  'send',
  'upload-image',
  'upload-file',
  'cancel-reply',
  'start-call',
  'start-video'
])

const message = ref('')
const showEmojiPicker = ref(false)
const textareaRef = ref(null)
const atMemberPickerRef = ref(null)
const emojiPickerRef = ref(null)
const isFocused = ref(false)

// 发送历史记录
const sendHistory = ref([])
let historyIndex = -1

// 加载发送历史
const loadSendHistory = () => {
  try {
    const history = localStorage.getItem('im_send_history')
    if (history) {
      sendHistory.value = JSON.parse(history)
    }
  } catch (e) {
    console.warn('加载发送历史失败', e)
  }
}

// 保存发送历史
const saveSendHistory = (text) => {
  sendHistory.value.unshift(text)
  // 最多保存50条
  if (sendHistory.value.length > 50) {
    sendHistory.value = sendHistory.value.slice(0, 50)
  }
  try {
    localStorage.setItem('im_send_history', JSON.stringify(sendHistory.value))
  } catch (e) {
    console.warn('保存发送历史失败', e)
  }
}

// 是否可以发送
const canSend = computed(() => message.value.trim().length > 0)

// 监听会话变化，恢复草稿
watch(() => props.session?.id, (newId, oldId) => {
  // 保存旧会话的草稿
  if (oldId && message.value.trim()) {
    try {
      localStorage.setItem(`im_draft_${oldId}`, message.value)
    } catch (e) {
      console.warn('保存草稿失败', e)
    }
  }
  
  // 恢复新会话的草稿
  if (newId) {
    try {
      const draft = localStorage.getItem(`im_draft_${newId}`)
      message.value = draft || ''
      nextTick(() => autoResize())
    } catch (e) {
      console.warn('恢复草稿失败', e)
    }
  }
})

// 自动保存草稿（防抖）
let draftSaveTimer = null
watch(message, (val) => {
  if (props.session?.id) {
    clearTimeout(draftSaveTimer)
    draftSaveTimer = setTimeout(() => {
      try {
        if (val.trim()) {
          localStorage.setItem(`im_draft_${props.session.id}`, val)
        } else {
          localStorage.removeItem(`im_draft_${props.session.id}`)
        }
      } catch (e) {
        console.warn('自动保存草稿失败', e)
      }
    }, 500)
  }
})

// 切换表情选择器
const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
  if (showEmojiPicker.value) {
    nextTick(() => {
      emojiPickerRef.value?.$el?.scrollIntoView({ block: 'nearest' })
    })
  }
}

// 选择表情
const selectEmoji = (emoji) => {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const before = message.value.substring(0, start)
  const after = message.value.substring(end)

  message.value = before + emoji + after

  nextTick(() => {
    const newPos = start + emoji.length
    textarea.setSelectionRange(newPos, newPos)
    textarea.focus()
    autoResize()
  })

  showEmojiPicker.value = false
}

// 处理@成员
const handleAtMember = () => {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  atMemberPickerRef.value?.open(start)
}

const onAtSelect = (member, position) => {
  const textarea = textareaRef.value
  if (!textarea) return

  const atText = `@${member.nickname || member.username} `
  const pos = position || message.value.length

  message.value = message.value.substring(0, pos) + atText + message.value.substring(pos)

  nextTick(() => {
    textarea.focus()
    const newPos = pos + atText.length
    textarea.setSelectionRange(newPos, newPos)
    autoResize()
  })
}

// 格式化回复时间
const formatReplyTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (date.toDateString() === now.toDateString()) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 自动调整输入框高度
const autoResize = () => {
  const textarea = textareaRef.value
  if (!textarea) return

  textarea.style.height = 'auto'
  const newHeight = Math.min(Math.max(textarea.scrollHeight, 56), 160)
  textarea.style.height = newHeight + 'px'
}

// 处理输入
const handleInput = () => {
  autoResize()
}

// 处理聚焦
const handleFocus = () => {
  isFocused.value = true
}

// 处理失焦
const handleBlur = () => {
  setTimeout(() => {
    if (!showEmojiPicker.value) {
      isFocused.value = false
    }
  }, 150)
}

// 处理按键
const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  } else if (e.key === 'Enter' && e.shiftKey) {
    nextTick(() => autoResize())
  } else if (e.key === 'Escape') {
    if (showEmojiPicker.value) {
      showEmojiPicker.value = false
    } else if (props.replyingMessage) {
      emit('cancel-reply')
    }
  } else if (e.key === '@' && props.session?.type === 'GROUP' && !e.ctrlKey && !e.metaKey) {
    setTimeout(() => {
      const textarea = textareaRef.value
      if (textarea) {
        const pos = textarea.selectionStart
        atMemberPickerRef.value?.open(pos)
      }
    }, 50)
  } else if (e.key === 'ArrowUp' && !message.value && !e.shiftKey) {
    // 上箭头：浏览发送历史
    e.preventDefault()
    if (sendHistory.value.length > 0) {
      historyIndex = Math.min(historyIndex + 1, sendHistory.value.length - 1)
      message.value = sendHistory.value[historyIndex] || ''
      nextTick(() => autoResize())
    }
  } else if (e.key === 'ArrowDown' && historyIndex >= 0 && !e.shiftKey) {
    // 下箭头：返回
    e.preventDefault()
    historyIndex = Math.max(historyIndex - 1, -1)
    if (historyIndex >= 0) {
      message.value = sendHistory.value[historyIndex] || ''
    } else {
      message.value = ''
    }
    nextTick(() => autoResize())
  }
}

// 处理发送
const handleSend = () => {
  if (!canSend.value) return

  const text = message.value.trim()
  
  // 保存到发送历史
  saveSendHistory(text)
  
  // 重置历史索引
  historyIndex = -1
  
  emit('send', text)
  message.value = ''
  
  // 清除草稿
  if (props.session?.id) {
    try {
      localStorage.removeItem(`im_draft_${props.session.id}`)
    } catch (e) {
      console.warn('清除草稿失败', e)
    }
  }

  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
      textareaRef.value.focus()
    }
  })
}

// 组件挂载时加载历史
onMounted(() => {
  loadSendHistory()
})

// 点击外部关闭表情选择器
const handleClickOutside = (e) => {
  if (showEmojiPicker.value) {
    const emojiEl = emojiPickerRef.value?.$el
    if (emojiEl && !emojiEl.contains(e.target)) {
      showEmojiPicker.value = false
    }
  }
}

if (typeof window !== 'undefined') {
  window.addEventListener('click', handleClickOutside)
}

onUnmounted(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('click', handleClickOutside)
  }
})
</script>

<style scoped lang="scss">
.chat-input-container {
  background: #fff;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  padding: 16px 20px 20px;  /* 增加padding使界面更宽敞 */
  border-top: 1px solid #e6e6e6;  /* 添加顶部边框分隔 */
}

:global(.dark) .chat-input-container {
  background: #1e293b;
}

/* 回复预览区域 */
.reply-preview {
  display: flex;
  align-items: stretch;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 8px;
  overflow: hidden;
}

:global(.dark) .reply-preview {
  background: rgba(51, 65, 85, 0.4);
}

.reply-preview .reply-indicator {
  width: 3px;
  background: linear-gradient(180deg, #1677ff 0%, #69b1ff 100%);
  flex-shrink: 0;
}

.reply-preview .reply-content {
  flex: 1;
  padding: 10px 12px;
  min-width: 0;
}

.reply-preview .reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.reply-preview .reply-user {
  font-size: 12px;
  font-weight: 500;
  color: #1677ff;
}

:global(.dark) .reply-preview .reply-user {
  color: #60a5fa;
}

.reply-preview .reply-time {
  font-size: 11px;
  color: #94a3b8;
}

.reply-preview .reply-text {
  font-size: 13px;
  color: #595959;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:global(.dark) .reply-preview .reply-text {
  color: #cbd5e1;
}

.reply-preview .close-btn {
  width: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.reply-preview .close-btn:hover {
  color: #595959;
  background: rgba(0, 0, 0, 0.05);
}

:global(.dark) .reply-preview .close-btn:hover {
  color: #e2e8f0;
  background: rgba(255, 255, 255, 0.1);
}

.reply-preview .close-btn .material-icons-outlined {
  font-size: 18px;
}

/* 工具栏 */
.input-toolbar {
  display: flex;
  align-items: center;
  padding: 8px 0 12px;  /* 增加上下padding */
}

.input-toolbar .toolbar-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  color: #64748b;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s ease;
  margin-right: 6px;  /* 增加按钮间距 */
}

.input-toolbar .toolbar-btn:hover {
  background: #f1f5f9;
  color: #1677ff;
}

:global(.dark) .input-toolbar .toolbar-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #60a5fa;
}

.input-toolbar .toolbar-btn.active {
  background: #e0f2fe;
  color: #1677ff;
}

:global(.dark) .input-toolbar .toolbar-btn.active {
  background: rgba(22, 119, 255, 0.2);
  color: #60a5fa;
}

.input-toolbar .toolbar-btn .material-icons-outlined {
  font-size: 22px;
}

.input-toolbar .toolbar-spacer {
  flex: 1;
}

/* 输入区域 */
.input-area {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  position: relative;
}

.message-input {
  flex: 1;
  min-height: 64px;  /* 增加最小高度从56px到64px */
  max-height: 200px;  /* 增加最大高度从160px到200px */
  padding: 16px 18px;  /* 增加padding */
  background: #f8f9fa;
  border: 1px solid transparent;
  border-radius: 8px;
  resize: none;
  font-size: 15px;
  color: #262626;
  outline: none;
  font-family: inherit;
  line-height: 1.6;  /* 增加行高 */
  transition: all 0.2s ease;
}

.message-input::placeholder {
  color: #94a3b8;
}

.message-input:focus {
  background: #fff;
  border-color: #1677ff;
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.08);
}

:global(.dark) .message-input:focus {
  background: #0f172a;
  border-color: #1677ff;
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.15);
}

:global(.dark) .message-input {
  background: rgba(255, 255, 255, 0.05);
  color: #f1f5f9;
}

:global(.dark) .message-input::placeholder {
  color: #475569;
}

/* 发送按钮 */
.send-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: 40px;
  padding: 0 12px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.send-btn:not(.disabled):hover {
  background: #4096ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.35);
}

.send-btn:not(.disabled):active {
  transform: translateY(0);
}

.send-btn.disabled {
  background: transparent;
  color: #94a3b8;
  cursor: default;
}

:global(.dark) .send-btn.disabled {
  color: #475569;
}

.send-btn.with-text {
  padding: 0 20px;
}

.send-btn .send-text {
  font-size: 15px;
  font-weight: 500;
  white-space: nowrap;
}

.send-btn .send-icon {
  font-size: 26px;
}

:global(.dark) .send-btn.disabled .send-icon {
  color: #64748b;
}

/* 底部提示 */
.input-hint {
  position: absolute;
  bottom: 2px;
  right: 0;
  font-size: 11px;
  color: #94a3b8;
  pointer-events: none;
}

:global(.dark) .input-hint {
  color: #475569;
}

/* Emoji选择器定位 */
:deep(.emoji-picker) {
  position: absolute;
  bottom: 100%;
  left: 0;
  margin-bottom: 8px;
  z-index: 100;
}
</style>
