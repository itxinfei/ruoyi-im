<template>
  <div class="chat-input-container" :class="{ disabled }">
    <!-- 精简版输入工具栏 -->
    <div class="input-toolbar">
      <div class="toolbar-group">
        <el-tooltip content="表情" placement="top">
          <el-button
            :icon="ChatDotRound"
            text
            :class="{ active: showEmojiPicker }"
            @click="toggleEmojiPicker"
          />
        </el-tooltip>
        <el-tooltip content="文件" placement="top">
          <el-button
            :icon="Folder"
            text
            :disabled="uploading"
            @click="triggerFileSelect"
          />
        </el-tooltip>
        <el-tooltip content="图片" placement="top">
          <el-button
            :icon="PictureFilled"
            text
            :disabled="uploading"
            @click="triggerImageSelect"
          />
        </el-tooltip>
        <el-tooltip content="语音" placement="top">
          <el-button
            :icon="Microphone"
            text
            :disabled="uploading"
            :class="{ recording: isRecording }"
            @click="toggleVoiceRecord"
          />
        </el-tooltip>
        <el-tooltip content="截图" placement="top">
          <el-button
            :icon="Crop"
            text
            :disabled="uploading"
            @click="triggerScreenshot"
          />
        </el-tooltip>
      </div>
    </div>

    <!-- 表情选择器 -->
    <EmojiPicker
      v-if="showEmojiPicker"
      :visible="showEmojiPicker"
      @select="insertEmoji"
      @close="showEmojiPicker = false"
    />

    <!-- 输入框 -->
    <el-input
      ref="inputRef"
      v-model="inputMessage"
      type="textarea"
      :rows="3"
      placeholder="输入消息... Enter发送，Shift+Enter换行"
      class="chat-input"
      :disabled="disabled"
      @keydown="handleKeydown"
      @input="handleInput"
    />

    <!-- 发送按钮 -->
    <div class="send-footer">
      <el-button 
        type="primary" 
        class="send-button" 
        :disabled="disabled || !canSend"
        @click="sendMessage"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { 
  ChatDotRound, Folder, PictureFilled, Microphone, Crop 
} from '@element-plus/icons-vue'
import EmojiPicker from '@/components/Chat/EmojiPicker.vue'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  uploading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send-message', 'upload-file', 'upload-image'])

// 输入状态
const inputMessage = ref('')
const showEmojiPicker = ref(false)
const isRecording = ref(false)
const inputRef = ref(null)

// 是否可以发送
const canSend = computed(() => {
  return inputMessage.value.trim().length > 0 && !props.disabled
})

// 切换表情选择器
const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

// 插入表情
const insertEmoji = (emoji) => {
  inputMessage.value += emoji
  nextTick(() => {
    focusInput()
  })
}

// 触发文件选择
const triggerFileSelect = () => {
  emit('upload-file')
}

// 触发图片选择
const triggerImageSelect = () => {
  emit('upload-image')
}

// 切换语音录制
const toggleVoiceRecord = () => {
  isRecording.value = !isRecording.value
  if (!isRecording.value) {
    // 停止录制
  } else {
    // 开始录制
  }
}

// 触发截图
const triggerScreenshot = () => {
  console.log('触发截图')
  // 实现截图功能
}

// 处理键盘事件
const handleKeydown = (event) => {
  if (props.disabled) return

  // Ctrl+Enter 换行，Enter 发送
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  } else if (event.key === 'Enter' && event.shiftKey) {
    // 换行
    // 让默认行为继续
  } else if (event.key === 'Escape') {
    showEmojiPicker.value = false
  }
}

// 处理输入
const handleInput = () => {
  // 处理@提及等逻辑
}

// 发送消息
const sendMessage = () => {
  if (!canSend.value) return

  const message = inputMessage.value.trim()
  if (message) {
    emit('send-message', message)
    inputMessage.value = ''
  }

  nextTick(() => {
    focusInput()
  })
}

// 聚焦输入框
const focusInput = () => {
  if (inputRef.value) {
    const textarea = inputRef.value.$el?.querySelector('textarea')
    if (textarea) {
      textarea.focus()
      // 设置光标到末尾
      textarea.setSelectionRange(textarea.value.length, textarea.value.length)
    }
  }
}

// 暴露聚焦方法
defineExpose({
  focus: focusInput
})
</script>

<style scoped lang="scss">
.chat-input-container {
  display: flex;
  flex-direction: column;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
  transition: all 0.2s ease;

  &.disabled {
    opacity: 0.6;
    pointer-events: none;
  }
}

.input-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 6px 12px;
  background: #fafafa;
  border-radius: 10px;
  border: 1px solid #f0f0f0;

  .toolbar-group {
    display: flex;
    align-items: center;
    gap: 2px;

    .el-button {
      --el-button-border-color: transparent;
      --el-button-bg-color: transparent;
      --el-button-text-color: #8c8c8c;
      width: 36px;
      height: 36px;
      padding: 0;
      border-radius: 8px;
      font-size: 16px;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        --el-button-text-color: #1677ff;
        --el-button-hover-bg-color: rgba(22, 119, 255, 0.06);
        transform: scale(1.05);
      }

      &:active {
        transform: scale(0.95);
      }

      &.active {
        --el-button-text-color: #1677ff;
        --el-button-bg-color: rgba(22, 119, 255, 0.1);
      }

      &.recording {
        --el-button-text-color: #ff4d4f;
        animation: recording-pulse 1.5s ease-in-out infinite;
      }
    }
  }
}

.chat-input {
  flex: 1;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  background: #ffffff;
  box-shadow: 
    0 1px 3px rgba(0, 0, 0, 0.04),
    0 4px 12px rgba(0, 0, 0, 0.02);
  display: flex;
  flex-direction: column;
  min-height: 80px;
  max-height: 200px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    border-color: #d9d9d9;
    box-shadow: 
      0 2px 6px rgba(0, 0, 0, 0.06),
      0 8px 20px rgba(0, 0, 0, 0.04);
  }

  &:focus-within {
    border-color: #1677ff;
    box-shadow: 
      0 0 0 3px rgba(22, 119, 255, 0.1),
      0 4px 12px rgba(22, 119, 255, 0.15);
  }

  :deep(.el-textarea) {
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  :deep(.el-textarea__inner) {
    border: none !important;
    padding: 16px !important;
    resize: none !important;
    font-size: 14px !important;
    line-height: 1.7 !important;
    background: transparent !important;
    height: 100% !important;
    min-height: 48px !important;
    box-shadow: none !important;
    color: #262626 !important;
    font-weight: 400 !important;

    // 文字渲染优化
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-rendering: optimizeLegibility;

    // 占位符样式
    &::placeholder {
      color: #bfbfbf !important;
      font-weight: 400 !important;
    }

    // 滚动条样式
    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: #d9d9d9;
      border-radius: 2px;

      &:hover {
        background: #bfbfbf;
      }
    }
  }
}

.send-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 12px;
  padding: 0 4px;

  .send-button {
    min-width: 88px;
    height: 36px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    padding: 8px 20px;
    background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
    border: none;
    color: #ffffff;
    box-shadow: 
      0 2px 6px rgba(22, 119, 255, 0.2),
      0 4px 16px rgba(22, 119, 255, 0.1);
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover:not(:disabled) {
      background: linear-gradient(135deg, #0958d9 0%, #1677ff 100%);
      transform: translateY(-1px);
      box-shadow: 
        0 4px 12px rgba(22, 119, 255, 0.3),
        0 8px 20px rgba(22, 119, 255, 0.2);
    }

    &:active:not(:disabled) {
      transform: translateY(0) scale(0.98);
      box-shadow: 
        0 2px 6px rgba(22, 119, 255, 0.2),
        0 4px 12px rgba(22, 119, 255, 0.1);
    }

    &:disabled {
      background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
      color: #bfbfbf;
      box-shadow: none;
      transform: none;
      cursor: not-allowed;
    }
  }
}

// 动画定义
@keyframes recording-pulse {
  0%, 100% { 
    opacity: 1;
    transform: scale(1);
  }
  50% { 
    opacity: 0.7;
    transform: scale(1.1);
  }
}
</style>
