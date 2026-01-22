<template>
  <div class="chat-input-container">
    <div v-if="replyingMessage" class="reply-preview">
      <div class="reply-content">
        <span class="reply-user">{{ replyingMessage.senderName }}:</span>
        <span class="reply-text">{{ replyingMessage.content }}</span>
      </div>
      <el-icon class="close-icon" @click="$emit('cancel-reply')"><Close /></el-icon>
    </div>
    <div class="input-toolbar">
      <div class="emoji-picker-wrapper">
        <el-tooltip content="表情">
          <el-button :icon="ChatDotRound" text class="toolbar-btn" @click.stop="toggleEmojiPicker" />
        </el-tooltip>
        <EmojiPicker
          v-if="showEmojiPicker"
          @select="selectEmoji"
          @click.stop
        />
      </div>
      <el-tooltip content="上传文件">
        <el-button :icon="Folder" text class="toolbar-btn" @click="$emit('upload-file')" />
      </el-tooltip>
      <el-tooltip content="上传图片">
        <el-button :icon="Picture" text class="toolbar-btn" @click="$emit('upload-image')" />
      </el-tooltip>
    </div>

    <div class="input-area">
      <el-input
        v-model="message"
        type="textarea"
        :autosize="{ minRows: 1, maxRows: 5 }"
        placeholder="输入消息... Enter发送"
        class="custom-textarea"
        @keydown="handleKeydown"
      />
      <div class="send-btn-wrapper">
        <el-button type="primary" :disabled="!message.trim()" :loading="sending" @click="handleSend">
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ChatDotRound, Folder, Picture, Close } from '@element-plus/icons-vue'
import EmojiPicker from '@/components/EmojiPicker/index.vue'

const props = defineProps({
  sending: Boolean,
  replyingMessage: Object
})

const emit = defineEmits(['send', 'upload-file', 'upload-image', 'cancel-reply'])

const message = ref('')
const showEmojiPicker = ref(false)

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

const selectEmoji = (emoji) => {
  message.value += emoji
  showEmojiPicker.value = false
}

const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

const handleSend = () => {
  if (!message.value.trim()) return
  emit('send', message.value)
  message.value = ''
}
</script>

<style scoped>
.chat-input-container {
  border-top: 1px solid #f0f0f0;
  background: #fff;
  padding: 10px 20px 20px;
}

.input-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.toolbar-btn {
  font-size: 20px;
  padding: 6px;
  color: #666;
}

.emoji-picker-wrapper {
  position: relative;
}

.custom-textarea :deep(.el-textarea__inner) {
  box-shadow: none;
  background: #f5f5f5;
  border-radius: 8px;
  padding: 10px;
}

.send-btn-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.reply-preview {
  display: flex;
  align-items: center;
  background: #f0f0f0;
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 8px;
  justify-content: space-between;
  
  .reply-content {
    font-size: 12px;
    color: #8c8c8c;
    flex: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    
    .reply-user {
      font-weight: bold;
      margin-right: 4px;
    }
  }
  
  .close-icon {
    cursor: pointer;
    color: #bfbfbf;
    &:hover { color: #8c8c8c; }
  }
}
</style>
