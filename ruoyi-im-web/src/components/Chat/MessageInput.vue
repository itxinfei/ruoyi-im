<template>
  <div class="message-input">
    <!-- 工具栏 -->
    <div class="toolbar">
      <button class="toolbar-btn" @click="insertEmoji('😀')">
        <el-icon><ChatDotRound /></el-icon>
        <span class="btn-label">表情</span>
      </button>
      <button class="toolbar-btn" @click="handleAtMemberClick" :disabled="!isGroupSession" :title="isGroupSession ? '@提及' : '只能在群聊中@成员'">
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
    </div>

    <!-- 输入区 -->
    <div class="text-area">
      <textarea
        v-model="content"
        placeholder="请输入消息..."
        @keydown="handleKeydown"
        @input="handleInput"
      ></textarea>
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
      type="file" 
      ref="fileRef" 
      hidden 
      :accept="uploadType === 'image' ? 'image/*' : '*/*'"
      @change="onFileChange" 
    />
    
    <!-- @成员选择器 -->
    <AtMemberPicker
      ref="atMemberPickerRef"
      :session-id="session?.id || session?.targetId"
      @select="handleAtMemberSelect"
    />
  </div>
</template>

<script setup>
import { ref, nextTick, watch, computed } from 'vue'
import { ChatDotRound, Picture, Files, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { parseUrlMetadata } from '@/api/im/urlMetadata'
import AtMemberPicker from './AtMemberPicker.vue'

const props = defineProps({ 
  session: Object, 
  sending: Boolean 
})
const emit = defineEmits(['send', 'upload-file', 'upload-image', 'typing'])

const content = ref('')
const fileRef = ref(null)
const atMemberPickerRef = ref(null)
const uploadType = ref(null) // 记录当前上传类型
let typingDebounceTimer = null // 输入防抖定时器

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
      const value = content.value
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
  const text = e.target.value
  const cursorPosition = e.target.selectionStart
  
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
      const length = content.value.length
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
            url: url,
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
  fileRef.value.click()
}

const onFileChange = (e) => {
  const file = e.target.files[0]
  if (!file) return
  
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
  
  // 重置 input 以便重复选择同一文件
  fileRef.value.value = ''
}
</script>

<style scoped lang="scss">
.message-input {
  background: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-light);
  padding: 8px 16px;
  display: flex;
  flex-direction: column;
}

.toolbar {
  height: 36px;
  display: flex;
  align-items: center;
  gap: 4px;

  .toolbar-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 2px;
    padding: 4px 12px;
    border: none;
    background: transparent;
    border-radius: var(--dt-radius-sm);
    cursor: pointer;
    color: var(--dt-text-secondary);
    transition: all var(--dt-transition-fast);

    .el-icon {
      font-size: 18px;
    }

    .btn-label {
      font-size: 11px;
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
  padding: 8px 0;
  min-height: 40px;
  textarea {
    width: 100%;
    min-height: 40px;
    max-height: 200px;
    border: none;
    outline: none;
    resize: vertical;
    font-size: var(--dt-font-size-base);
    color: var(--dt-text-primary);
    background: transparent;
    &::placeholder { color: var(--dt-text-quaternary); }
  }
}

.footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;

  .el-button {
    min-width: 100px;
    height: 36px;
    font-size: 14px;
    font-weight: 500;
  }
}

// 暗色模式适配
:global(.dark) {
  .message-input {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .text-area textarea {
    color: var(--dt-text-primary-dark);
    &::placeholder { color: var(--dt-text-quaternary-dark); }
  }
}
</style>