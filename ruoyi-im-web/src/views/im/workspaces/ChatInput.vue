<template>
  <div class="chat-input-container" :class="{ disabled }">
    <!-- 输入工具栏 - 钉钉风格 -->
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
        <el-tooltip content="上传文件" placement="top">
          <el-button
            :icon="Folder"
            text
            :disabled="uploading"
            @click="triggerFileSelect"
          />
        </el-tooltip>
        <el-tooltip content="上传图片" placement="top">
          <el-button
            :icon="PictureFilled"
            text
            :disabled="uploading"
            @click="triggerImageSelect"
          />
        </el-tooltip>
        <!-- @成员功能（仅群聊显示） -->
        <el-tooltip v-if="isGroupChat" content="@成员" placement="top">
          <el-button
            :icon="AtSign"
            text
            :disabled="uploading"
            @click="showAtMemberPicker"
          />
        </el-tooltip>
        <el-tooltip content="语音通话" placement="top">
          <el-button
            :icon="Phone"
            text
            :disabled="uploading"
            @click="startVoiceCall"
          />
        </el-tooltip>
        <el-tooltip content="视频通话" placement="top">
          <el-button
            :icon="VideoCamera"
            text
            :disabled="uploading"
            @click="startVideoCall"
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
        <el-tooltip content="历史记录" placement="top">
          <el-button
            :icon="Clock"
            text
            @click="showHistory"
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

    <!-- 输入区域 - 发送按钮在右侧 -->
    <div class="input-area-wrapper">
      <el-input
        ref="inputRef"
        v-model="inputMessage"
        type="textarea"
        :autosize="{ minRows: 1, maxRows: 5 }"
        placeholder="输入消息... Enter发送，Shift+Enter换行"
        class="chat-input"
        :disabled="disabled"
        @keydown="handleKeydown"
        @input="handleInput"
      />

      <!-- 发送按钮（在输入框右侧） -->
      <div class="send-button-wrapper">
        <el-button
          type="primary"
          :disabled="disabled || !canSend"
          @click="sendMessage"
        >
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useStore } from 'vuex'
import {
  ChatDotRound,
  Folder,
  PictureFilled,
  AtSign,
  Phone,
  VideoCamera,
  Crop,
  Clock
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

const emit = defineEmits(['send-message', 'upload-file', 'upload-image', 'show-at-members', 'voice-call', 'video-call', 'show-history'])

const store = useStore()

// 输入状态
const inputMessage = ref('')
const showEmojiPicker = ref(false)
const isRecording = ref(false)
const inputRef = ref(null)

// 是否可以发送
const canSend = computed(() => {
  return inputMessage.value.trim().length > 0 && !props.disabled
})

// 当前会话类型（是否群聊）
const isGroupChat = computed(() => {
  const currentSession = store.state.im.currentSession
  return currentSession?.type === 'GROUP' || currentSession?.conversationType === 'GROUP'
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

// 切换语音录制（保留备用）
const toggleVoiceRecord = () => {
  isRecording.value = !isRecording.value
  if (!isRecording.value) {
    // 停止录制
  } else {
    // 开始录制
  }
}

// 显示@成员选择器
const showAtMemberPicker = () => {
  emit('show-at-members')
}

// 语音通话
const startVoiceCall = () => {
  emit('voice-call')
}

// 视频通话
const startVideoCall = () => {
  emit('video-call')
}

// 显示历史记录
const showHistory = () => {
  emit('show-history')
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
  padding: 8px 16px;
  border-bottom: 1px solid #f0f0f0;

  .toolbar-group {
    display: flex;
    align-items: center;
    gap: 4px;

    .el-button {
      --el-button-border-color: transparent;
      --el-button-bg-color: transparent;
      --el-button-text-color: #666;
      width: 32px;
      height: 32px;
      padding: 0;
      border-radius: 4px;
      font-size: 18px;
      transition: all 0.2s ease;

      &:hover {
        --el-button-text-color: #1677ff;
        background: rgba(0, 0, 0, 0.04);
      }

      &:active {
        transform: scale(0.95);
      }

      &.active {
        --el-button-text-color: #1677ff;
        background: rgba(22, 119, 255, 0.1);
      }

      &.recording {
        --el-button-text-color: #ff4d4f;
        animation: recording-pulse 1.5s ease-in-out infinite;
      }
    }
  }
}

// 输入区域 - 横向布局（输入框+发送按钮）
.input-area-wrapper {
  display: flex;
  align-items: flex-end;
  padding: 8px 16px 12px;
  gap: 12px;
}

.chat-input {
  flex: 1;

  :deep(.el-textarea__inner) {
    border: none !important;
    padding: 8px 0 !important;
    resize: none !important;
    font-size: 14px !important;
    line-height: 1.5 !important;
    background: transparent !important;
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

// 发送按钮（在输入框右侧）
.send-button-wrapper {
  flex-shrink: 0;
  margin-bottom: 2px;

  .el-button {
    min-width: 80px;
    height: 32px;
    border-radius: 4px;
    font-size: 14px;
    font-weight: 400;
    padding: 6px 16px;
    background: #1677ff;
    border: none;
    color: #ffffff;
    transition: all 0.2s ease;

    &:hover:not(:disabled) {
      background: #4096ff;
    }

    &:active:not(:disabled) {
      background: #0958d9;
    }

    &:disabled {
      background: #d9d9d9;
      color: #ffffff;
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
