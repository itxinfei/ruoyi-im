<template>
  <div 
    class="chat-input-container" 
    :class="{ 'drag-over': isDragOver }"
    :style="{ minHeight: containerHeight + 'px' }"
    @dragover.prevent="handleDragOver"
    @dragleave.prevent="handleDragLeave"
    @drop.prevent="handleDrop"
  >
    <!-- 回复/编辑预览区 -->
    <div v-if="replyingMessage" class="reply-preview-container">
      <div class="preview-content">
        <span class="label">回复 {{ replyingMessage.senderName }}:</span>
        <span class="text">{{ replyingMessage.content }}</span>
      </div>
      <el-button link @click="$emit('cancel-reply')"><el-icon><Close /></el-icon></el-button>
    </div>

    <div v-if="editingMessage" class="edit-preview-container">
      <div class="preview-content">
        <span class="label">编辑消息:</span>
        <span class="text">{{ editingMessage.content }}</span>
      </div>
      <el-button link @click="$emit('cancel-edit')"><el-icon><Close /></el-icon></el-button>
    </div>

    <!-- 工具栏 -->
    <div class="input-toolbar">
      <div class="left-tools">
        <el-popover v-model:visible="showEmojiPicker" trigger="click" placement="top-start" :width="320">
          <template #reference>
            <button class="tool-btn" title="表情"><span class="material-icons-outlined">sentiment_satisfied</span></button>
          </template>
          <div class="emoji-picker-content">
            <!-- 表情选择器组件占位 -->
            <div class="emoji-grid">
              <span v-for="emoji in ['😀','😃','😄','😁','😆','😅','😂','🤣','😊','😇','🙂','🙃','😉','😌','😍','🥰','😘','😗','😙','😚','😋','😛','😝','😜','🤪','🤨','🧐','🤓','😎','🤩','🥳','😏','😒','😞','😔','😟','😕','🙁','☹️','😮','😯','😲','😳','🥺','😦','😧','😨','😰','😥','😢','😭','😱','😖','😣','😞','😓','😩','😫','🥱','😤','😡','😠','🤬','😈','👿','💀','☠️','💩','🤡','👹','👺','👻','👽','👾','🤖','😺','😸','😹','😻','😼','😽','🙀','😿','😾','🙈','🙉','🙊','💋','💌','💘','💝','💖','💗','💓','💞','💕','💟','❣️','💔','❤️','🧡','💛','💚','💙','💜','🖤','🤍','🤎','💯','💢','💥','💫','💦','💨','🕳️','💣','💬','👁️‍🗨️','🗨️','🗯️','💭','💤']" 
                    :key="emoji" class="emoji-item" @click="insertEmoji(emoji)">{{ emoji }}</span>
            </div>
          </div>
        </el-popover>
        
        <button class="tool-btn" title="图片" @click="triggerImageUpload"><span class="material-icons-outlined">image</span></button>
        <button class="tool-btn" title="文件" @click="triggerFileUpload"><span class="material-icons-outlined">attach_file</span></button>
        <button class="tool-btn" title="语音" @click="showVoiceRecorder = true"><span class="material-icons-outlined">mic</span></button>
        <button class="tool-btn" title="视频通话" @click="$emit('start-video')"><span class="material-icons-outlined">videocam</span></button>
        <button class="tool-btn" title="截图" @click="handleScreenshot"><span class="material-icons-outlined">screenshot</span></button>
      </div>
      <div class="right-tools">
        <el-dropdown trigger="click" @command="handleShortcutChange">
          <span class="shortcut-tip">{{ shortcut === 'enter' ? 'Enter 发送' : 'Ctrl+Enter 发送' }}</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="enter" :disabled="shortcut === 'enter'">Enter 发送, Ctrl+Enter 换行</el-dropdown-item>
              <el-dropdown-item command="ctrl-enter" :disabled="shortcut === 'ctrl-enter'">Ctrl+Enter 发送, Enter 换行</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="input-area">
      <textarea
        ref="textareaRef"
        v-model="content"
        class="chat-textarea"
        :placeholder="getInputPlaceholder"
        @keydown="handleKeyDown"
        @paste="handlePaste"
        @input="handleInput"
      ></textarea>
    </div>

    <!-- URL 解析预览提示 -->
    <div v-if="detectedUrl" class="url-preview-tip">
      <span class="material-icons-outlined">link</span>
      <span>检测到链接，发送时将自动抓取预览信息</span>
      <span class="url-text">{{ detectedUrl }}</span>
    </div>

    <!-- 发送按钮 -->
    <div class="input-footer">
      <div class="footer-tip">使用钉钉 8.2 对齐版</div>
      <button 
        class="send-btn" 
        :class="{ active: content.trim() || hasMedia }" 
        :disabled="sending || (!content.trim() && !hasMedia)"
        @click="send"
      >
        {{ sending ? '发送中...' : '发送' }}
      </button>
    </div>

    <!-- 隐藏的上传入口 -->
    <input type="file" ref="imageInput" hidden accept="image/*" @change="onImageFileChange" />
    <input type="file" ref="fileInput" hidden @change="onFileChange" />

    <!-- 语音录制 -->
    <VoiceRecorder v-if="showVoiceRecorder" @close="showVoiceRecorder = false" @send="handleVoiceSend" />

    <!-- @成员选择器 -->
    <AtMemberPicker
      ref="atMemberPickerRef"
      :session-id="session?.targetId"
      @select="handleAtSelect"
    />
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { parseUrlMetadata } from '../../api/im/urlMetadata'
import VoiceRecorder from './VoiceRecorder.vue'
import AtMemberPicker from './AtMemberPicker.vue'

const props = defineProps({
  session: Object,
  sending: Boolean,
  replyingMessage: Object,
  editingMessage: Object
})

const emit = defineEmits([
  'send', 'cancel-reply', 'cancel-edit', 'edit-confirm', 
  'start-call', 'start-video', 'upload-image', 'upload-file',
  'typing', 'send-voice'
])

const content = ref('')
const shortcut = ref('enter')
const isDragOver = ref(false)
const containerHeight = ref(160)
const showEmojiPicker = ref(false)
const showVoiceRecorder = ref(false)
const textareaRef = ref(null)
const atMemberPickerRef = ref(null)
const imageInput = ref(null)
const fileInput = ref(null)

const mentionUserIds = ref([])
const mentionAll = ref(false)

const hasMedia = computed(() => false) // 暂未实现多媒体预览

// URL 链接正则
const URL_REGEX = /(https?:\/\/[^\s]+)/g

// 检测输入中的链接
const detectUrl = (text) => {
  if (!text) return null
  const match = text.match(URL_REGEX)
  return match ? match[0] : null
}

// 解析链接元数据
const parsingUrl = ref(false)
const detectedUrl = ref(null)

const getInputPlaceholder = computed(() => {
  if (props.session?.type === 'GROUP') {
    return '发送给群聊，按 Enter 发送，输入 @ 提及成员'
  }
  return '发送给 ' + (props.session?.name || '联系人') + '，按 Enter 发送'
})

const handleKeyDown = (e) => {
  if (e.key === 'Enter') {
    if ((shortcut.value === 'enter' && !e.ctrlKey && !e.shiftKey) || 
        (shortcut.value === 'ctrl-enter' && e.ctrlKey)) {
      e.preventDefault()
      send()
    }
  }
  
  if (e.key === '@' && props.session?.type === 'GROUP') {
    setTimeout(() => {
      atMemberPickerRef.value?.open()
    }, 50)
  }
}

const handleAtSelect = (member) => {
  const nickname = member.id === 'all' ? '所有人' : (member.nickname || member.username)
  const atText = `${nickname} `
  
  if (member.id === 'all') {
    mentionAll.value = true
  } else {
    const uid = member.userId || member.id
    if (!mentionUserIds.value.includes(uid)) mentionUserIds.value.push(uid)
  }

  // 补全 @ 文本
  if (content.value.endsWith('@')) {
    content.value += atText
  } else {
    content.value += `@${atText}`
  }
  textareaRef.value?.focus()
}

const send = async () => {
  const text = content.value.trim()
  if (!text && !hasMedia.value) return

  // 检测是否包含链接，如果包含则解析 URL 元数据
  const url = detectUrl(text)
  let messageContent = text
  let messageType = 'TEXT'

  if (url) {
    try {
      parsingUrl.value = true
      const result = await parseUrlMetadata(url)
      // result 已经是 response.data，结构为 { code, msg, data }
      if (result.code === 200 && result.data) {
        // 构建 LINK 类型的消息内容
        messageContent = JSON.stringify({
          url: url,
          title: result.data.title || '',
          description: result.data.description || '',
          imageUrl: result.data.imageUrl || '',
          siteName: result.data.siteName || ''
        })
        messageType = 'LINK'
      }
    } catch (e) {
      console.error('解析 URL 失败:', e)
      // 解析失败时降级为普通文本消息
    } finally {
      parsingUrl.value = false
    }
  }

  const payload = {
    content: messageContent,
    type: messageType,
    mentionInfo: {
      userIds: mentionUserIds.value,
      mentionAll: mentionAll.value
    }
  }

  if (props.editingMessage) {
    emit('edit-confirm', { ...payload, id: props.editingMessage.id })
  } else {
    emit('send', payload)
  }

  content.value = ''
  mentionUserIds.value = []
  mentionAll.value = false
}

const insertEmoji = (emoji) => {
  content.value += emoji
  showEmojiPicker.value = false
  textareaRef.value?.focus()
}

const handlePaste = (e) => {
  const items = e.clipboardData?.items
  if (!items) return
  let hasImage = false
  for (const item of items) {
    if (item.type.indexOf('image') !== -1) {
      const file = item.getAsFile()
      if (file) {
        hasImage = true; e.preventDefault()
        emit('upload-image', file)
      }
    }
  }
  if (hasImage) ElMessage.success('正在识别图片...')
}

const triggerImageUpload = () => imageInput.value?.click()
const triggerFileUpload = () => fileInput.value?.click()
const onImageFileChange = (e) => {
  if (e.target.files[0]) emit('upload-image', e.target.files[0])
}
const onFileChange = (e) => {
  if (e.target.files[0]) emit('upload-file', e.target.files[0])
}

const handleScreenshot = () => ElMessage.info('截图功能需浏览器权限支持，请使用系统快捷键 Alt+A')
const handleShortcutChange = (cmd) => { shortcut.value = cmd }
const handleDragOver = () => isDragOver.value = true
const handleDragLeave = () => isDragOver.value = false
const handleDrop = (e) => {
  isDragOver.value = false
  const files = e.dataTransfer?.files
  if (files?.length) emit('upload-file', files[0])
}

const handleVoiceSend = (blob) => emit('send-voice', blob)

const handleInput = () => {
  if (content.value.length > 0) emit('typing', true)
  // 检测链接并显示预览提示
  const url = detectUrl(content.value)
  detectedUrl.value = url
}

watch(() => props.editingMessage, (val) => {
  if (val) content.value = val.content
})

defineExpose({ focus: () => textareaRef.value?.focus() })
</script>

<style scoped lang="scss">
.chat-input-container { background: #fff; border-top: 1px solid #f0f0f0; display: flex; flex-direction: column; padding: 0 16px 12px; transition: all 0.2s;
  &.drag-over { background: #f8fafc; border-top-color: #1677ff; }
}
.reply-preview-container, .edit-preview-container { display: flex; align-items: center; justify-content: space-between; padding: 8px 12px; background: #f5f5f5; border-radius: 4px; margin-top: 8px;
  .preview-content { font-size: 12px; color: #5c5c5c; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; .label { font-weight: 600; margin-right: 4px; } }
}
.input-toolbar {
  height: 44px; // 略微加高，更从容
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
  .left-tools {
    display: flex;
    align-items: center;
    gap: 8px; // 严格对齐间距
    .tool-btn {
      width: 34px;
      height: 34px;
      color: #646a73;
      .material-icons-outlined { font-size: 22px; }
      &:hover { background: #f2f3f5; color: #1677ff; }
    }
  }
}
.input-area { flex: 1; padding: 4px 0;
  .chat-textarea { width: 100%; height: 100%; min-height: 60px; border: none; outline: none; resize: none; font-size: 14px; line-height: 1.6; color: #1f2329; background: transparent;
    &::placeholder { color: #8f959e; }
  }
}
.url-preview-tip {
  display: flex; align-items: center; gap: 6px; padding: 6px 10px; background: rgba(22, 119, 255, 0.08);
  border-radius: 4px; margin-top: 8px; font-size: 12px; color: #1677ff;
  .material-icons-outlined { font-size: 16px; }
  .url-text { color: #1677ff; text-decoration: underline; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
}
.input-footer { display: flex; align-items: center; justify-content: space-between; margin-top: 8px;
  .footer-tip { font-size: 11px; color: #c9cdd4; }
  .send-btn { padding: 6px 20px; border: none; border-radius: 4px; background: #f2f3f5; color: #8f959e; font-size: 14px; cursor: default; transition: all 0.2s;
    &.active { background: #1677ff; color: #fff; cursor: pointer; &:hover { background: #0056b3; } }
  }
}
.emoji-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 8px; padding: 10px; max-height: 240px; overflow-y: auto;
  .emoji-item { font-size: 20px; cursor: pointer; display: flex; align-items: center; justify-content: center; border-radius: 4px; transition: background 0.2s; &:hover { background: #f2f3f5; } }
}
</style>
