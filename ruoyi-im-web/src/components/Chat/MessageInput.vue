<template>
  <div class="chat-input-container" :style="{ height: containerHeight + 'px' }">
    <!-- 拖拽手柄 -->
    <div 
      class="resize-handle" 
      @mousedown="startResize"
      @dblclick="resetHeight"
      title="拖拽调整高度，双击重置"
    >
      <div class="resize-indicator"></div>
    </div>
    <!-- 编辑预览 -->
    <div v-if="editingMessage" class="edit-preview" role="region" aria-label="编辑消息预览">
      <div class="edit-indicator"></div>
      <div class="edit-content">
        <div class="edit-header">
          <span class="edit-label">正在编辑消息</span>
        </div>
        <div class="edit-text">{{ editingMessage.content }}</div>
      </div>
      <button class="close-btn" @click="$emit('cancel-edit')" aria-label="取消编辑">
        <span class="material-icons-outlined" aria-hidden="true">close</span>
      </button>
    </div>

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
  replyingMessage: Object,
  editingMessage: Object
})

const emit = defineEmits([
  'send',
  'upload-image',
  'upload-file',
  'cancel-reply',
  'cancel-edit',
  'edit-confirm',
  'start-call',
  'start-video'
])

const message = ref('')
const showEmojiPicker = ref(false)
const textareaRef = ref(null)
const atMemberPickerRef = ref(null)
const emojiPickerRef = ref(null)
const isFocused = ref(false)

// 容器高度管理
const containerHeight = ref(180)  // 默认180px
const minHeight = 140  // 最小高度
const maxHeight = 400  // 最大高度
let isResizing = false
let startY = 0
let startHeight = 0

// 加载保存的高度
const loadSavedHeight = () => {
  try {
    const saved = localStorage.getItem('im_input_container_height')
    if (saved) {
      const height = parseInt(saved)
      if (height >= minHeight && height <= maxHeight) {
        containerHeight.value = height
      }
    }
  } catch (e) {
    console.warn('加载输入区高度失败', e)
  }
}

// 保存高度
const saveHeight = (height) => {
  try {
    localStorage.setItem('im_input_container_height', height)
  } catch (e) {
    console.warn('保存输入区高度失败', e)
  }
}

// 开始拖拽调整大小
const startResize = (e) => {
  isResizing = true
  startY = e.clientY
  startHeight = containerHeight.value
  
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
  document.body.style.cursor = 'ns-resize'
  document.body.style.userSelect = 'none'
}

// 处理拖拽
const handleResize = (e) => {
  if (!isResizing) return
  
  const deltaY = startY - e.clientY  // 向上为正
  const newHeight = startHeight + deltaY
  
  if (newHeight >= minHeight && newHeight <= maxHeight) {
    containerHeight.value = newHeight
  }
}

// 停止拖拽
const stopResize = () => {
  if (isResizing) {
    isResizing = false
    saveHeight(containerHeight.value)
    document.removeEventListener('mousemove', handleResize)
    document.removeEventListener('mouseup', stopResize)
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  }
}

// 重置高度
const resetHeight = () => {
  containerHeight.value = 180
  saveHeight(180)
}

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

// 监听编辑消息变化，填充内容
watch(() => props.editingMessage, (msg) => {
  if (msg) {
    message.value = msg.content
    nextTick(() => {
      autoResize()
      textareaRef.value?.focus()
    })
  }
})

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

// 自动调整输入框高度（已废弃，高度由 chat-input-container 拖拽控制）
const autoResize = () => {
  const textarea = textareaRef.value
  if (!textarea) return

  textarea.scrollTop = textarea.scrollHeight
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
    } else if (props.editingMessage) {
      emit('cancel-edit')
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
  
  if (props.editingMessage) {
    emit('edit-confirm', text)
  } else {
    emit('send', text)
  }
  
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
  loadSavedHeight()  // 加载保存的输入区高度
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
  padding: 16px 20px 20px;
  border-top: 1px solid #e6e6e6;
  position: relative;  /* 为拖拽手柄定位 */
  transition: none;  /* 禁用过渡以流畅调整大小 */
}

/* 拖拽手柄 */
.resize-handle {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 12px;  /* 增加拖拽区域高度 */
  cursor: ns-resize;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  user-select: none;
  transition: background 0.2s ease;  /* 添加背景过渡效果 */
}

.resize-handle:hover {
  background: rgba(22, 119, 255, 0.08);  /* 增加悬停背景 */
}

.resize-handle:active {
  background: rgba(22, 119, 255, 0.12);  /* 拖拽时背景更深 */
}

.resize-indicator {
  width: 48px;  /* 增加指示器宽度 */
  height: 4px;  /* 增加指示器高度 */
  background: #d9d9d9;
  border-radius: 3px;  /* 增加圆角 */
  position: relative;
  transition: all 0.2s ease;
}

.resize-handle:hover .resize-indicator {
  background: #1677ff;  /* 悬停时指示器变色 */
  width: 56px;  /* 悬停时指示器变宽 */
  height: 5px;  /* 悬停时指示器变高 */
}

.resize-handle:active .resize-indicator {
  background: #0958d9;  /* 拖拽时指示器颜色更深 */
  width: 60px;  /* 拖拽时指示器更宽 */
}

:global(.dark) .resize-indicator {
  background: #475569;  /* 深色模式指示器颜色 */
}

:global(.dark) .resize-handle:hover .resize-indicator {
  background: #60a5fa;  /* 深色模式悬停颜色 */
}

:global(.dark) .resize-handle:active .resize-indicator {
  background: #3b82f6;  /* 深色模式拖拽颜色 */
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

/* 编辑预览区域 */
.edit-preview {
  display: flex;
  align-items: stretch;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 8px;
  overflow: hidden;
}

:global(.dark) .edit-preview {
  background: rgba(51, 65, 85, 0.4);
}

.edit-preview .edit-indicator {
  width: 3px;
  background: linear-gradient(180deg, #52c41a 0%, #95de64 100%);
  flex-shrink: 0;
}

.edit-preview .edit-content {
  flex: 1;
  padding: 10px 12px;
  min-width: 0;
}

.edit-preview .edit-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.edit-preview .edit-label {
  font-size: 12px;
  font-weight: 500;
  color: #52c41a;
}

:global(.dark) .edit-preview .edit-label {
  color: #95de64;
}

.edit-preview .edit-text {
  font-size: 13px;
  color: #595959;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:global(.dark) .edit-preview .edit-text {
  color: #cbd5e1;
}

.edit-preview .close-btn {
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

.edit-preview .close-btn:hover {
  color: #595959;
  background: rgba(0, 0, 0, 0.05);
}

:global(.dark) .edit-preview .close-btn:hover {
  color: #e2e8f0;
  background: rgba(255, 255, 255, 0.1);
}

.edit-preview .close-btn .material-icons-outlined {
  font-size: 18px;
}

/* 工具栏 */
.input-toolbar {
  display: flex;
  align-items: center;
  margin: 0;  /* 边距设置为0 */
  gap: 4px;  /* 使用gap替代margin-right */
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
  position: relative;  /* 为tooltip定位 */
}

.input-toolbar .toolbar-btn:hover {
  background: #f1f5f9;
  color: #1677ff;
  transform: scale(1.05);  /* 轻微放大效果 */
}

.input-toolbar .toolbar-btn:active {
  transform: scale(0.95);  /* 点击缩小效果 */
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
  margin: 0;  /* 边距设置为0 */
  border: 1px solid #e6e6e6;  /* 添加边框 */
  border-radius: 8px;  /* 增加圆角 */
  overflow: hidden;  /* 防止内容溢出 */
  transition: border-color 0.2s ease, box-shadow 0.2s ease;  /* 添加阴影过渡 */
  align-items: flex-end;
  gap: 8px;
  position: relative;
  background: #fff;  /* 添加背景色 */
}

.input-area:focus-within {
  border-color: #1677ff;  /* 聚焦时边框颜色 */
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);  /* 聚焦时添加阴影 */
}

:global(.dark) .input-area {
  border-color: #475569;  /* 深色模式边框颜色 */
  background: rgba(255, 255, 255, 0.03);  /* 深色模式背景 */
}

:global(.dark) .input-area:focus-within {
  border-color: #60a5fa;  /* 深色模式聚焦边框颜色 */
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.15);  /* 深色模式聚焦阴影 */
}

.message-input {
  flex: 1;
  height: 100%;  /* 填满父容器高度 */
  padding: 16px 18px;  /* 增加padding */
  background: transparent;  /* 背景透明，使用父容器背景 */
  border: none;  /* 移除边框，使用父容器边框 */
  border-radius: 0;  /* 移除圆角，使用父容器圆角 */
  resize: none;
  font-size: 15px;
  color: #262626;
  outline: none;
  font-family: inherit;
  line-height: 1.6;  /* 增加行高 */
  overflow-y: auto;  /* 允许垂直滚动 */
  overflow-x: hidden;  /* 隐藏水平滚动 */
  transition: none;  /* 移除过渡，使用父容器过渡 */
}

.message-input::placeholder {
  color: #94a3b8;
}

.message-input:focus {
  background: transparent;  /* 聚焦时保持透明背景 */
  border: none;  /* 聚焦时无边框 */
  box-shadow: none;  /* 聚焦时无阴影 */
}

:global(.dark) .message-input:focus {
  background: transparent;  /* 深色模式聚焦时保持透明 */
  border: none;
  box-shadow: none;
}

:global(.dark) .message-input {
  background: transparent;  /* 深色模式背景透明 */
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
  min-width: 48px;  /* 增加最小宽度 */
  height: 40px;
  padding: 0 16px;  /* 调整内边距 */
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 6px;  /* 调整圆角 */
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);  /* 使用更平滑的过渡 */
  flex-shrink: 0;
  font-weight: 500;  /* 增加字体粗细 */
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);  /* 添加轻微阴影 */
}

.send-btn:not(.disabled):hover {
  background: #4096ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.25);  /* 增强悬停阴影 */
}

.send-btn:not(.disabled):active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(22, 119, 255, 0.2);  /* 按下时阴影减弱 */
}

.send-btn.disabled {
  background: transparent;
  color: #94a3b8;
  cursor: default;
  box-shadow: none;
}

.send-btn.disabled:hover {
  background: rgba(0, 0, 0, 0.04);  /* 禁用状态悬停背景 */
  transform: none;
}

:global(.dark) .send-btn.disabled {
  color: #475569;
}

:global(.dark) .send-btn.disabled:hover {
  background: rgba(255, 255, 255, 0.05);
}

.send-btn.with-text {
  padding: 0 20px;
  min-width: 80px;  /* 有文字时增加最小宽度 */
}

.send-btn .send-text {
  font-size: 15px;
  font-weight: 500;
  white-space: nowrap;
  letter-spacing: 0.3px;  /* 增加字间距 */
}

.send-btn .send-icon {
  font-size: 24px;  /* 调整图标大小 */
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

/* 响应式设计 */
@media (max-width: 768px) {
  .input-toolbar .toolbar-btn {
    width: 32px;  /* 移动端减小按钮尺寸 */
    height: 32px;
    margin-right: 4px;  /* 减小按钮间距 */
  }

  .input-toolbar .toolbar-btn .material-icons-outlined {
    font-size: 20px;  /* 减小图标尺寸 */
  }

  .message-input {
    min-height: 56px;  /* 移动端减小最小高度 */
    max-height: 160px;  /* 移动端减小最大高度 */
    padding: 12px 14px;  /* 减小内边距 */
    font-size: 14px;  /* 减小字体大小 */
  }

  .send-btn {
    min-width: 40px;  /* 移动端减小发送按钮最小宽度 */
    height: 36px;  /* 减小高度 */
    padding: 0 12px;  /* 减小内边距 */
  }

  .send-btn.with-text {
    min-width: 64px;  /* 有文字时减小最小宽度 */
    padding: 0 16px;
  }

  .send-btn .send-text {
    font-size: 14px;  /* 减小文字大小 */
  }

  .send-btn .send-icon {
    font-size: 22px;  /* 减小图标大小 */
  }

  .input-area {
    gap: 6px;  /* 减小元素间距 */
  }

  .resize-handle {
    height: 10px;  /* 移动端减小拖拽手柄高度 */
  }

  .resize-indicator {
    width: 40px;  /* 移动端减小指示器宽度 */
    height: 3px;  /* 移动端减小指示器高度 */
  }

  .resize-handle:hover .resize-indicator {
    width: 44px;  /* 移动端悬停时指示器宽度 */
    height: 4px;
  }

  .resize-handle:active .resize-indicator {
    width: 48px;  /* 移动端拖拽时指示器宽度 */
  }
}

@media (max-width: 480px) {
  .input-toolbar .toolbar-btn {
    width: 30px;  /* 小屏幕进一步减小按钮尺寸 */
    height: 30px;
    margin-right: 3px;
  }

  .input-toolbar .toolbar-btn .material-icons-outlined {
    font-size: 18px;
  }

  .message-input {
    min-height: 48px;
    max-height: 140px;
    padding: 10px 12px;
    font-size: 13px;
  }

  .send-btn {
    min-width: 36px;
    height: 32px;
    padding: 0 10px;
  }

  .send-btn.with-text {
    min-width: 56px;
    padding: 0 12px;
  }

  .send-btn .send-text {
    font-size: 13px;
  }

  .send-btn .send-icon {
    font-size: 20px;
  }

  .input-area {
    gap: 4px;
  }
}
</style>
