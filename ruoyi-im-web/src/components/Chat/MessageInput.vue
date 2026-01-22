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
      <el-tooltip v-if="session?.type === 'GROUP'" content="@成员">
        <el-button text class="toolbar-btn" @click="handleAtMember">@</el-button>
      </el-tooltip>
      <el-tooltip content="语音通话">
        <el-button :icon="Phone" text class="toolbar-btn" @click="$emit('voice-call')" />
      </el-tooltip>
      <el-tooltip content="视频通话">
        <el-button :icon="VideoCamera" text class="toolbar-btn" @click="$emit('video-call')" />
      </el-tooltip>
      <el-tooltip content="截图">
        <el-button :icon="Scissor" text class="toolbar-btn" />
      </el-tooltip>
      <el-tooltip content="历史记录">
        <el-button :icon="Clock" text class="toolbar-btn" />
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

    <!-- @成员选择器 -->
    <AtMemberPicker
      ref="atMemberPickerRef"
      :session-id="session?.id"
      @select="onAtSelect"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ChatDotRound, Folder, Picture, Close, Phone, VideoCamera, Scissor, Clock } from '@element-plus/icons-vue'
import EmojiPicker from '@/components/EmojiPicker/index.vue'
import AtMemberPicker from './AtMemberPicker.vue'

const props = defineProps({
  session: Object,
  sending: Boolean,
  replyingMessage: Object
})

const emit = defineEmits(['send', 'upload-file', 'upload-image', 'cancel-reply', 'voice-call', 'video-call'])

const message = ref('')
const showEmojiPicker = ref(false)
const atMemberPickerRef = ref(null)

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

const selectEmoji = (emoji) => {
  message.value += emoji
  showEmojiPicker.value = false
}

const handleAtMember = () => {
  atMemberPickerRef.value?.open()
}

const onAtSelect = (member) => {
  const atText = `@${member.nickname || member.username} `
  message.value += atText
}

const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  } else if (e.key === '@' && props.session?.type === 'GROUP') {
    // 延迟打开，避免在输入框还没输入@就打开
    setTimeout(() => {
      handleAtMember()
    }, 100)
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
  padding: 12px 16px 16px;
}

.input-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.toolbar-btn {
  font-size: 20px;
  padding: 6px;
  color: #666;
}

.emoji-picker-wrapper {
  position: relative;
}

.input-area {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.custom-textarea {
  flex: 1;
}

.custom-textarea :deep(.el-textarea__inner) {
  box-shadow: none;
  background: #f5f5f5;
  border-radius: 8px;
  padding: 10px 12px;
  resize: none;
}

.send-btn-wrapper {
  flex-shrink: 0;
}

.send-btn-wrapper .el-button {
  height: 36px;
  padding: 0 20px;
}

.reply-preview {
  display: flex;
  align-items: center;
  background: #f0f0f0;
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 10px;
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
