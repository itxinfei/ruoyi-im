<template>
  <div class="message-input" :class="{ focused: isFocused }">
    <!-- 工具栏 -->
    <div class="toolbar">
      <button class="toolbar-btn" @click="insertEmoji('😀')">
        <el-icon><ChatDotRound /></el-icon>
        <span class="btn-label">表情</span>
      </button>
      <button
        class="toolbar-btn"
        :disabled="!isGroupSession"
        :title="isGroupSession ? '@提及' : '只能在群聊中@成员'"
        @click="handleAtMemberClick"
      >
        <el-icon><UserFilled /></el-icon>
        <span class="btn-label">@</span>
      </button>
      <button class="toolbar-btn" @click="triggerUpload('image')">
        <el-icon><Picture /></el-icon>
        <span class="btn-label">图片</span>
      </button>
      <button class="toolbar-btn" @click="triggerUpload('file')">
        <el-icon><Files /></el-icon>
        <span class="btn-label">文件</span>
      </button>
      <button class="toolbar-btn" @click="triggerUpload('batch')">
        <el-icon><FolderOpened /></el-icon>
        <span class="btn-label">批量</span>
      </button>
    </div>

    <!-- 输入区 -->
    <div class="text-area">
      <div v-if="replyingMessage" class="reply-bar">
        <div class="reply-info">
          <span class="reply-label">回复</span>
          <span class="reply-name">{{ replyingMessage.senderName || '对方' }}</span>
          <span class="reply-text">{{ getReplyPreview(replyingMessage) }}</span>
        </div>
        <button class="reply-close" @click="clearReply">×</button>
      </div>
      <textarea
        ref="textareaRef"
        v-model="content"
        placeholder="请输入消息..."
        @keydown="handleKeydown"
        @input="handleInput"
        @paste="handlePaste"
        @focus="isFocused = true"
        @blur="isFocused = false"
      />
    </div>

    <!-- 发送按钮 -->
    <div class="footer">
      <el-button
        type="primary"
        size="small"
        :disabled="!content.trim()"
        :loading="sending"
        @click="handleSend"
      >
        发送
      </el-button>
    </div>

    <input
      ref="fileRef"
      type="file"
      hidden
      :accept="uploadType === 'image' ? 'image/*' : '*/*'"
      @change="onFileChange"
    >

    <!-- @成员选择器 -->
    <AtMemberPicker
      ref="atMemberPickerRef"
      :session-id="session?.id || session?.targetId"
      @select="handleAtMemberSelect"
    />
  </div>
</template>

<script setup>
import { ref, nextTick, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { ChatDotRound, Picture, Files, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { parseUrlMetadata } from '@/api/im/urlMetadata'
import AtMemberPicker from './AtMemberPicker.vue'

const props = defineProps({
  session: Object,
  sending: Boolean
})
const emit = defineEmits(['send', 'upload-file', 'upload-image', 'upload-batch', 'typing'])

const store = useStore()
const content = ref('')
const textareaRef = ref(null)
const fileRef = ref(null)
const atMemberPickerRef = ref(null)
const uploadType = ref(null) // 记录当前上传类型
const isFocused = ref(false) // 输入框焦点状态

const _typingDebounceTimer = null // 输入防抖定时器
const replyingMessage = computed(() => store.state.im?.message?.replyingMessage || null)

// 判断是否为群聊会话
const isGroupSession = computed(() => {
  return props.session?.type === 'GROUP' || props.session?.sessionType === 'GROUP'
})

watch(() => props.session?.id, (id) => {
  if (!id) return
  const session = store.state.im?.session?.sessions?.find(s => s.id === id)
  content.value = session?.draftContent || ''
})

let draftTimer = null
watch(content, (val) => {
  const sessionId = props.session?.id
  if (!sessionId) return
  if (draftTimer) clearTimeout(draftTimer)
  draftTimer = setTimeout(() => {
    store.dispatch('im/session/saveDraft', { sessionId, content: val })
  }, 300)
})

// URL 正则匹配
const urlPattern = /(https?:\/\/[^\s<]+[^<.,:;"')\]\s])/g

/**
 * 提取文本中的第一个 URL
 */
const extractFirstUrl = (text) => {
  const match = text.match(urlPattern)
  return match ? match[0] : null
}

/**
 * 处理 @ 成员按钮点击
 */
const handleAtMemberClick = () => {
  if (!isGroupSession.value) {
    ElMessage.info('只能在群聊中@成员')
    return
  }
  atMemberPickerRef.value?.open()
}

/**
 * 处理键盘事件
 */
const handleKeydown = (e) => {
  if (e.key === 'Enter') {
    if (e.ctrlKey || e.metaKey) {
      // Ctrl+Enter 或 Cmd+Enter：换行
      e.preventDefault()
      const textarea = e.target
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const { value } = content
      content.value = value.substring(0, start) + '\n' + value.substring(end)
      // 将光标移动到换行后
      nextTick(() => {
        textarea.selectionStart = textarea.selectionEnd = start + 1
      })
    } else {
      // Enter：发送消息
      e.preventDefault()
      handleSend()
    }
  }
}

/**
 * 处理文本输入
 */
const handleInput = (e) => {
  // 检测是否输入了 @ 符号
  if (e.data === '@') {
    // 判断是否在群聊中
    if (props.session?.type === 'GROUP' || props.session?.sessionType === 'GROUP') {
      nextTick(() => {
        atMemberPickerRef.value?.open()
      })
    }
  }

  // 发送正在输入信号（防抖处理）
  emit('typing')
}

/**
 * 处理 @ 成员选择
 */
const handleAtMemberSelect = (member) => {
  if (member.id === 'all') {
    // @所有人
    content.value += '@所有人 '
  } else {
    // @具体成员
    content.value += `@${member.nickname || member.username} `
  }

  // 重新聚焦到输入框
  nextTick(() => {
    const textarea = textareaRef.value
    if (textarea) {
      textarea.focus()
      // 将光标移到末尾
      const { length } = content.value
      textarea.setSelectionRange(length, length)
    }
  })
}

/**
 * 发送消息
 */
const handleSend = async () => {
  if (!content.value.trim()) return

  const text = content.value.trim()
  const replyToMessageId = replyingMessage.value?.messageId || replyingMessage.value?.id || null
  const url = extractFirstUrl(text)

  // 如果文本只包含 URL（或主要是 URL），尝试获取元数据并发送链接卡片
  if (url && text.trim() === url) {
    try {
      const res = await parseUrlMetadata(url)
      if (res.code === 200 && res.data) {
        // 发送链接卡片消息
        emit('send', {
          content: JSON.stringify({
            url,
            title: res.data.title || '',
            description: res.data.description || '',
            imageUrl: res.data.imageUrl || res.data.thumbnail || ''
          }),
          type: 'LINK',
          replyToMessageId
        })
        content.value = ''
        clearReply()
        return
      }
    } catch (e) {
      // 解析失败，降级为普通文本
      console.warn('URL 元数据解析失败，降级为文本消息:', e)
    }
  }

  // 普通文本消息
  emit('send', { content: text, type: 'TEXT', replyToMessageId })
  content.value = ''
  clearReply()
}

const insertEmoji = (emoji) => {
  content.value += emoji
}

const triggerUpload = (type) => {
  uploadType.value = type
  if (type === 'batch') {
    fileRef.value.multiple = true
  } else {
    fileRef.value.multiple = false
  }
  fileRef.value.click()
}

const onFileChange = (e) => {
  const files = e.target.files
  if (!files || files.length === 0) return

  if (uploadType.value === 'batch') {
    // 批量上传
    const fileList = Array.from(files)
    
    // 验证所有文件大小（限制 100MB）
    const oversizedFiles = fileList.filter(file => file.size > 100 * 1024 * 1024)
    if (oversizedFiles.length > 0) {
      ElMessage.warning(`文件大小不能超过 100MB (${oversizedFiles.length}个文件超限)`)
      return
    }
    
    // 构建 FormData
    const formData = new FormData()
    fileList.forEach(file => {
      formData.append('files', file)
    })
    formData.append('sessionId', props.session?.id)
    
    emit('upload-batch', formData)
  } else {
    const file = files[0]
    
    // 验证文件类型
    if (uploadType.value === 'image') {
      // 检查是否为图片
      if (!validateImageFile(file)) return
      emit('upload-image', file)
    } else {
      // 文件上传
      if (!validateFile(file)) return
      emit('upload-file', file)
    }
  }

  // 重置 input 以便重复选择同一文件
  fileRef.value.value = ''
}

const handlePaste = (e) => {
  const items = e.clipboardData?.items || []
  if (!items.length) return

  // 优先处理图片粘贴
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      const file = item.getAsFile()
      if (file) {
        if (!validateImageFile(file)) return
        emit('upload-image', file)
        e.preventDefault()
        return
      }
    }
  }

  // 处理文件粘贴（非图片）
  for (const item of items) {
    if (item.kind === 'file') {
      const file = item.getAsFile()
      if (file) {
        if (!validateFile(file)) return
        emit('upload-file', file)
        e.preventDefault()
        return
      }
    }
  }
}

const validateImageFile = (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 10MB')
    return false
  }
  return true
}

const validateFile = (file) => {
  if (file.size > 100 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 100MB')
    return false
  }
  return true
}

const clearReply = () => {
  store.dispatch('im/message/clearReplyingMessage')
}

const getReplyPreview = (msg) => {
  if (!msg) return ''
  if (msg.type === 'TEXT') return (msg.content || '').slice(0, 60)
  if (msg.type === 'IMAGE') return '[图片]'
  if (msg.type === 'FILE') return '[文件]'
  if (msg.type === 'LINK') return '[链接]'
  return '[消息]'
}
</script>

<style scoped lang="scss">
.message-input {
  background: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-light);
  padding: var(--dt-spacing-sm) var(--dt-spacing-lg);
  display: flex;
  flex-direction: column;
  min-height: var(--dt-chat-input-height, 96px);
  border-left: 1px solid transparent;
  border-right: 1px solid transparent;
  transition: border-color var(--dt-transition-fast);

  &.focused {
    border-color: var(--dt-brand-color);
    border-top-color: var(--dt-brand-color);
  }
}

.toolbar {
  height: var(--dt-toolbar-height, 40px);
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-xs);
  padding-bottom: var(--dt-spacing-sm);
  border-bottom: 1px solid var(--dt-border-lighter);

  .toolbar-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: var(--dt-spacing-xs);
    padding: var(--dt-spacing-xs) var(--dt-spacing-md);
    border: none;
    background: transparent;
    border-radius: var(--dt-radius-sm);
    cursor: pointer;
    color: var(--dt-text-secondary);
    transition: all var(--dt-transition-fast);
    height: var(--dt-btn-height-sm, 32px);

    .el-icon {
      font-size: var(--dt-icon-size-md, 18px);
      line-height: 1;
    }

    .btn-label {
      font-size: var(--dt-font-size-xs);
      line-height: 1;
    }

    &:hover {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
    }

    &:active {
      background: var(--dt-brand-lighter);
    }

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;
      &:hover {
        background: transparent;
        color: var(--dt-text-secondary);
      }
    }
  }
}

.text-area {
  flex: 1;
  padding: var(--dt-spacing-sm) 0;
  min-height: var(--dt-input-min-height, 60px);

  .reply-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: var(--dt-brand-lighter);
    color: var(--dt-text-primary);
    border-radius: var(--dt-radius-sm);
    padding: var(--dt-spacing-xs) var(--dt-spacing-sm);
    margin-bottom: var(--dt-spacing-sm);
    font-size: var(--dt-font-size-sm);
  }

  .reply-info {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-xs);
    min-width: 0;
  }

  .reply-label {
    color: var(--dt-brand-color);
    font-weight: var(--dt-font-weight-medium);
  }

  .reply-name {
    color: var(--dt-text-secondary);
  }

  .reply-text {
    color: var(--dt-text-tertiary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 260px;
  }

  .reply-close {
    border: none;
    background: transparent;
    color: var(--dt-text-secondary);
    cursor: pointer;
    font-size: var(--dt-font-size-base);
    line-height: 1;
    padding: 0 4px;
  }

  textarea {
    width: 100%;
    min-height: var(--dt-input-min-height, 60px);
    max-height: var(--dt-input-max-height, 300px);
    border: none;
    outline: none;
    resize: none;
    font-size: var(--dt-font-size-base);
    line-height: 1.6;
    color: var(--dt-text-primary);
    background: transparent;
    font-family: var(--dt-font-family);
    
    &::placeholder { 
      color: var(--dt-text-quaternary); 
    }
  }
}

.footer {
  display: flex;
  justify-content: flex-end;
  padding-top: var(--dt-spacing-sm);

  .el-button {
    min-width: var(--dt-btn-min-width, 80px);
    height: var(--dt-btn-height-md, 36px);
    font-size: var(--dt-font-size-base);
    font-weight: var(--dt-font-weight-medium);
    border-radius: var(--dt-radius-sm);
  }
}

// 暗色模式适配
:global(.dark) {
  .message-input {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .toolbar {
    border-color: var(--dt-border-dark);
  }

  .text-area .reply-bar {
    background: var(--dt-bg-hover-dark);
    color: var(--dt-text-primary-dark);
  }

  .text-area textarea {
    color: var(--dt-text-primary-dark);
    &::placeholder { color: var(--dt-text-quaternary-dark); }
  }
}
</style>
