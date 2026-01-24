<template>
  <div class="chat-input-container">
    <!-- 回复预览 -->
    <div v-if="replyingMessage" class="reply-preview" role="region" aria-label="回复消息预览">
      <div class="reply-content">
        <span class="reply-user">{{ replyingMessage.senderName }}:</span>
        <span class="reply-text">{{ replyingMessage.content }}</span>
      </div>
      <button class="close-icon" @click="$emit('cancel-reply')" aria-label="取消回复">
        <span class="material-icons-outlined" aria-hidden="true">close</span>
      </button>
    </div>

    <!-- 工具栏 -->
    <div class="input-toolbar" role="toolbar" aria-label="消息输入工具栏">
      <button class="toolbar-btn" @click.stop="toggleEmojiPicker" aria-label="插入表情" title="表情">
        <span class="material-icons-outlined" aria-hidden="true">sentiment_satisfied</span>
      </button>
      <button class="toolbar-btn" @click="$emit('upload-file')" aria-label="上传附件" title="附件">
        <span class="material-icons-outlined" aria-hidden="true">attachment</span>
      </button>
      <button class="toolbar-btn" @click="$emit('upload-image')" aria-label="上传图片" title="图片">
        <span class="material-icons-outlined" aria-hidden="true">image</span>
      </button>
      <button class="toolbar-btn" aria-label="语音输入" title="语音">
        <span class="material-icons-outlined" aria-hidden="true">mic</span>
      </button>

      <!-- Emoji选择器 -->
      <EmojiPicker
        v-if="showEmojiPicker"
        @select="selectEmoji"
        @click.stop
      />
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <textarea
        v-model="message"
        class="message-input"
        placeholder="输入消息..."
        rows="3"
        @keydown="handleKeydown"
        :aria-label="session?.type === 'GROUP' ? '输入群消息' : '输入消息'"
      ></textarea>
    </div>

    <!-- 发送按钮 -->
    <div class="send-btn-wrapper">
      <button
        class="send-btn"
        :disabled="!message.trim() || sending"
        :class="{ sending }"
        @click="handleSend"
        :aria-label="sending ? '发送中...' : '发送消息'"
      >
        <span class="material-icons-outlined send-icon" aria-hidden="true">send</span>
        <span class="send-text">发送</span>
      </button>
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
import EmojiPicker from '@/components/EmojiPicker/index.vue'
import AtMemberPicker from './AtMemberPicker.vue'

const props = defineProps({
  session: Object,
  sending: Boolean,
  replyingMessage: Object
})

const emit = defineEmits(['send', 'upload-file', 'upload-image', 'cancel-reply'])

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
  background: #fff;
  padding: 16px 24px;
  flex-shrink: 0;
  border-top: 1px solid #e6e6e6;
}

/* 回复预览 */
.reply-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f5f5f5;
  padding: 8px 12px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.reply-content {
  font-size: 12px;
  color: #595959;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.reply-user {
  font-weight: 500;
  margin-right: 4px;
}

.close-icon {
  background: none;
  border: none;
  color: #bfbfbf;
  cursor: pointer;
  padding: 2px;
  display: flex;
  align-items: center;
}

.close-icon:hover {
  color: #595959;
}

/* 工具栏 */
.input-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.toolbar-btn {
  font-size: 20px;
  padding: 4px;
  color: #8c8c8c;
  background: transparent;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: color 0.2s;
}

.toolbar-btn:hover {
  color: #1677ff;
}

/* 输入区域 */
.input-area {
  position: relative;
}

.message-input {
  width: 100%;
  border: none;
  background: transparent;
  resize: none;
  font-size: 14px;
  color: #262626;
  outline: none;
  font-family: inherit;
  line-height: 1.5;
}

.message-input::placeholder {
  color: #bfbfbf;
}

/* 发送按钮区域 */
.send-btn-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.send-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
}

.send-btn:hover:not(:disabled) {
  background: #4096ff;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn.sending {
  opacity: 0.7;
}

.send-icon {
  font-size: 14px;
}

/* 暗色模式 */
:deep(.dark) .chat-input-container {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .reply-preview {
  background: rgba(51, 65, 85, 0.5);
}

:deep(.dark) .reply-content,
:deep(.dark) .close-icon {
  color: #94a3b8;
}

:deep(.dark) .toolbar-btn {
  color: #64748b;
}

:deep(.dark) .toolbar-btn:hover {
  color: #60a5fa;
}

:deep(.dark) .message-input {
  color: #f1f5f9;
}

:deep(.dark) .message-input::placeholder {
  color: #475569;
}

:deep(.dark) .send-btn {
  background: #1677ff;
}
</style>
