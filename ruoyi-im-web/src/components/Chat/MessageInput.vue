<template>
  <div class="message-input">
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
      <textarea
        v-model="content"
        placeholder="请输入消息..."
        @keydown="handleKeydown"
        @input="handleInput"
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
import { ref, nextTick, computed } from 'vue'
import { ChatDotRound, Picture, Files, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { parseUrlMetadata } from '@/api/im/urlMetadata'
import AtMemberPicker from './AtMemberPicker.vue'

const props = defineProps({
  session: Object,
  sending: Boolean
})
const emit = defineEmits(['send', 'upload-file', 'upload-image', 'upload-batch', 'typing'])

const content = ref('')
const fileRef = ref(null)
const atMemberPickerRef = ref(null)
const uploadType = ref(null) // 记录当前上传类型

const _typingDebounceTimer = null // 输入防抖定时器

// 判断是否为群聊会话
const isGroupSession = computed(() => {
  return props.session?.type === 'GROUP' || props.session?.sessionType === 'GROUP'
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
    const textarea = document.querySelector('.text-area textarea')
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
          type: 'LINK'
        })
        content.value = ''
        return
      }
    } catch (e) {
      // 解析失败，降级为普通文本
      console.warn('URL 元数据解析失败，降级为文本消息:', e)
    }
  }

  // 普通文本消息
  emit('send', { content: text, type: 'TEXT' })
  content.value = ''
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
      if (!file.type.startsWith('image/')) {
        ElMessage.warning('请选择图片文件')
        return
      }
      // 检查文件大小（限制 10MB）
      if (file.size > 10 * 1024 * 1024) {
        ElMessage.warning('图片大小不能超过 10MB')
        return
      }
      emit('upload-image', file)
    } else {
      // 文件上传
      // 检查文件大小（限制 100MB）
      if (file.size > 100 * 1024 * 1024) {
        ElMessage.warning('文件大小不能超过 100MB')
        return
      }
      emit('upload-file', file)
    }
  }

  // 重置 input 以便重复选择同一文件
  fileRef.value.value = ''
}
</script>

<style scoped lang="scss">
.message-input {
  background: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-light);
  padding: var(--dt-spacing-sm) var(--dt-spacing-lg);
  display: flex;
  flex-direction: column;
  min-height: var(--dt-chat-input-height, 160px);
}

.toolbar {
  height: 40px;
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
    height: 32px;

    .el-icon {
      font-size: 18px;
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
  min-height: 60px;
  
  textarea {
    width: 100%;
    min-height: 60px;
    max-height: 200px;
    border: none;
    outline: none;
    resize: vertical;
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
    min-width: 80px;
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

  .text-area textarea {
    color: var(--dt-text-primary-dark);
    &::placeholder { color: var(--dt-text-quaternary-dark); }
  }
}
</style>
