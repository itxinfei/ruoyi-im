<template>
  <div class="message-input">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button :icon="Orange" link @click="insertEmoji('😀')" />
      <el-button :icon="Picture" link @click="triggerUpload('image')" />
      <el-button :icon="Files" link @click="triggerUpload('file')" />
    </div>

    <!-- 输入区 -->
    <div class="text-area">
      <textarea
        v-model="content"
        placeholder="请输入消息..."
        @keydown.enter.prevent="handleSend"
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

    <input type="file" ref="fileRef" hidden @change="onFileChange" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Orange, Picture, Files } from '@element-plus/icons-vue'
import { parseUrlMetadata } from '@/api/im/urlMetadata'

const props = defineProps({ session: Object, sending: Boolean })
const emit = defineEmits(['send', 'upload'])

const content = ref('')
const fileRef = ref(null)

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
  fileRef.value.click()
}

const onFileChange = (e) => {
  const file = e.target.files[0]
  if (file) emit('upload', file)
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
  height: 32px;
  display: flex;
  align-items: center;
  gap: 8px;
  .el-button { font-size: 20px; color: var(--dt-text-secondary); &:hover { color: var(--dt-brand-color); } }
}

.text-area {
  flex: 1;
  padding: 8px 0;
  textarea {
    width: 100%;
    height: 80px;
    border: none;
    outline: none;
    resize: none;
    font-size: var(--dt-font-size-base);
    color: var(--dt-text-primary);
    background: transparent;
    &::placeholder { color: var(--dt-text-quaternary); }
  }
}

.footer {
  display: flex;
  justify-content: flex-end;
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