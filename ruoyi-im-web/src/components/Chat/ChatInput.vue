<template>
  <div class="chat-input-container">
    <!-- 工具栏 -->
    <div class="input-toolbar">
      <div class="toolbar-left">
        <!-- 表情按钮 -->
        <el-tooltip content="表情" placement="top">
          <button
            class="toolbar-button"
            :class="{ active: showEmojiPicker }"
            aria-label="表情"
            @click="toggleEmojiPicker"
          >
            <i class="el-icon-collection-tag"></i>
            <span class="button-ripple"></span>
          </button>
        </el-tooltip>

        <!-- 文件上传 -->
        <el-tooltip content="发送文件" placement="top">
          <el-upload
            class="upload-item"
            action="/api/im/file/upload"
            :show-file-list="false"
            :before-upload="beforeFileUpload"
            :on-success="handleFileSuccess"
            :on-error="handleUploadError"
            :on-progress="handleFileProgress"
            :headers="uploadHeaders"
            multiple
          >
            <button class="toolbar-button" aria-label="发送文件">
              <i class="el-icon-folder"></i>
              <span class="button-ripple"></span>
            </button>
          </el-upload>
        </el-tooltip>

        <!-- 图片上传 -->
        <el-tooltip content="发送图片" placement="top">
          <el-upload
            class="upload-item"
            action="/api/im/file/upload"
            accept="image/*"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :on-success="handleImageSuccess"
            :on-error="handleUploadError"
            :on-progress="handleFileProgress"
            :headers="uploadHeaders"
            multiple
          >
            <button class="toolbar-button" aria-label="发送图片">
              <i class="el-icon-picture"></i>
              <span class="button-ripple"></span>
            </button>
          </el-upload>
        </el-tooltip>

        <!-- 语音录制 -->
        <el-tooltip content="语音消息" placement="top">
          <button
            class="toolbar-button"
            :class="{ recording: isRecording }"
            aria-label="语音消息"
            @mousedown="startRecording"
            @mouseup="stopRecording"
            @mouseleave="stopRecording"
            @touchstart.prevent="startRecording"
            @touchend.prevent="stopRecording"
            @touchcancel.prevent="stopRecording"
          >
            <i class="el-icon-microphone"></i>
            <span class="button-ripple"></span>
            <span v-if="isRecording" class="recording-indicator"></span>
          </button>
        </el-tooltip>

        <!-- 视频通话 -->
        <el-tooltip content="视频通话" placement="top">
          <button class="toolbar-button" aria-label="视频通话" @click="startVideoCall">
            <i class="el-icon-video-camera"></i>
            <span class="button-ripple"></span>
          </button>
        </el-tooltip>

        <!-- 语音通话 -->
        <el-tooltip content="语音通话" placement="top">
          <button class="toolbar-button" aria-label="语音通话" @click="startVoiceCall">
            <i class="el-icon-phone"></i>
            <span class="button-ripple"></span>
          </button>
        </el-tooltip>
      </div>

      <div class="toolbar-right">
        <!-- @提醒 -->
        <el-tooltip content="@提醒" placement="top">
          <button class="toolbar-button" aria-label="@提醒" @click="handleAtClick">
            <i class="el-icon-user"></i>
            <span class="button-ripple"></span>
          </button>
        </el-tooltip>

        <!-- 更多功能 -->
        <el-dropdown trigger="click" @command="handleMoreCommand">
          <button class="toolbar-button" aria-label="更多功能">
            <i class="el-icon-more"></i>
            <span class="button-ripple"></span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="location">
                <i class="el-icon-location"></i>
                <span>发送位置</span>
              </el-dropdown-item>
              <el-dropdown-item command="vote">
                <i class="el-icon-s-data"></i>
                <span>发起投票</span>
              </el-dropdown-item>
              <el-dropdown-item command="code">
                <i class="el-icon-document"></i>
                <span>代码片段</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <!-- 引用消息显示 -->
      <transition name="reply-slide">
        <div v-if="replyMessage" class="reply-preview">
          <div class="reply-content">
            <div class="reply-sender">{{ replyMessage.senderName }}</div>
            <div class="reply-text">{{ replyMessage.content }}</div>
          </div>
          <button class="reply-close" aria-label="取消回复" @click="cancelReply">
            <i class="el-icon-close"></i>
          </button>
        </div>
      </transition>

      <!-- 文本输入框 -->
      <div class="text-input-wrapper">
        <el-input
          ref="textInput"
          v-model="messageText"
          type="textarea"
          :rows="inputRows"
          :maxlength="maxLength"
          placeholder="请输入消息，按Enter发送，Shift+Enter换行"
          resize="none"
          class="custom-textarea"
          @keydown="handleKeyDown"
          @input="handleInput"
          @focus="handleFocus"
          @blur="handleBlur"
        />

        <!-- 字数统计 -->
        <transition name="count-fade">
          <div
            v-show="messageText.length > 0"
            class="char-count"
            :class="{ 'near-limit': isNearLimit }"
          >
            <i class="el-icon-edit"></i>
            <span>{{ messageText.length }}/{{ maxLength }}</span>
          </div>
        </transition>

        <!-- 快捷操作提示 -->
        <transition name="hint-fade">
          <div v-if="showInputHint" class="input-hint">
            <i class="el-icon-info"></i>
            <span>{{ inputHintText }}</span>
          </div>
        </transition>
      </div>

      <!-- 发送按钮 -->
      <div class="send-button-wrapper">
        <transition name="send-button">
          <el-button
            type="primary"
            :class="{
              'send-ready': canSend && !sending,
              'send-sending': sending,
            }"
            :disabled="!canSend"
            :loading="sending"
            aria-label="发送消息"
            @click="sendMessage"
          >
            <template v-if="!sending">
              <i class="el-icon-s-promotion"></i>
              <span>发送</span>
            </template>
            <template v-else>
              <span>发送中...</span>
            </template>
          </el-button>
        </transition>
      </div>
    </div>

    <!-- 表情选择器 -->
    <div v-if="showEmojiPicker" class="emoji-picker-container">
      <emoji-picker @select="handleEmojiSelect" @close="showEmojiPicker = false" />
    </div>

    <!-- @选择器 -->
    <mention-selector
      :visible="showMentionSelector"
      :members="sessionMembers"
      :position="mentionPosition"
      :show-all="isGroupChat"
      @select="handleMentionSelect"
      @close="showMentionSelector = false"
    />

    <!-- 语音录制提示 -->
    <transition name="recording-fade">
      <div v-if="isRecording" class="recording-overlay" @click="stopRecording">
        <div class="recording-modal" @click.stop>
          <div class="recording-icon-wrapper">
            <div class="recording-waves">
              <span></span>
              <span></span>
              <span></span>
            </div>
            <i class="el-icon-microphone recording-icon"></i>
          </div>
          <div class="recording-text">
            <div class="recording-status">
              <i class="el-icon-loading"></i>
              <span>正在录音...</span>
            </div>
            <div class="recording-time">{{ formatRecordingTime(recordingTime) }}</div>
          </div>
          <div class="recording-tip">
            <i class="el-icon-info"></i>
            <span>松开结束录音</span>
          </div>
          <button class="recording-cancel" aria-label="取消录音" @click="cancelRecording">
            <i class="el-icon-close"></i>
            <span>取消</span>
          </button>
        </div>
      </div>
    </transition>

    <!-- 文件上传进度 -->
    <transition name="upload-fade">
      <div v-if="uploadProgress.show" class="upload-progress-overlay">
        <div class="upload-progress-modal">
          <div class="upload-progress-icon">
            <i :class="uploadProgress.icon"></i>
          </div>
          <div class="upload-progress-text">
            <div class="upload-status">{{ uploadProgress.status }}</div>
            <div class="upload-filename">{{ uploadProgress.filename }}</div>
          </div>
          <div class="upload-progress-bar">
            <div class="progress-fill" :style="{ width: uploadProgress.percent + '%' }"></div>
          </div>
          <div class="upload-progress-percent">{{ uploadProgress.percent }}%</div>
        </div>
      </div>
    </transition>

    <!-- 功能弹窗 -->

    <!-- 位置选择器 -->
    <location-picker
      v-model="showLocationPicker"
      @confirm="handleLocationConfirm"
    />

    <!-- 投票对话框 -->
    <vote-dialog
      v-model="showVoteDialog"
      :session-id="sessionId"
      @confirm="handleVoteConfirm"
    />

    <!-- 代码片段对话框 -->
    <code-snippet-dialog
      v-model="showCodeSnippetDialog"
      :session-id="sessionId"
      @send="handleCodeSend"
    />
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import EmojiPicker from './EmojiPicker.vue'
import MentionSelector from './MentionSelector.vue'
import LocationPicker from './LocationPicker.vue'
import VoteDialog from './VoteDialog.vue'
import CodeSnippetDialog from './CodeSnippetDialog.vue'
import { getToken } from '@/utils/auth.js'

// 组件属性定义
const props = defineProps({
  sessionId: {
    type: String,
    required: true,
    description: '当前会话ID',
  },
  sessionMembers: {
    type: Array,
    default: () => [],
    description: '会话成员列表',
  },
  replyMessage: {
    type: Object,
    default: null,
    description: '回复的消息对象',
  },
})

// 组件事件定义
const emit = defineEmits([
  'send-message',
  'send-file',
  'send-image',
  'send-voice',
  'send-location',
  'send-vote',
  'send-code',
  'start-call',
  'cancel-reply',
])

// 响应式数据
const messageText = ref('')
const inputRows = ref(3)
const maxLength = ref(1000)
const sending = ref(false)
const showEmojiPicker = ref(false)

const isRecording = ref(false)
const recordingTime = ref(0)
const recordingTimer = ref(null)
const textInput = ref(null)

const showInputHint = ref(false)
const inputHintText = ref('')
const inputHintTimer = ref(null)

const uploadProgress = ref({
  show: false,
  percent: 0,
  status: '',
  filename: '',
  icon: 'el-icon-upload',
})

const isFocused = ref(false)

// @提醒相关状态
const showMentionSelector = ref(false)
const mentionPosition = ref({ x: 0, y: 0 })
const mentionedMembers = ref([])

// 功能弹窗状态
const showLocationPicker = ref(false)
const showVoteDialog = ref(false)
const showCodeSnippetDialog = ref(false)

// 计算属性：是否是群聊
const isGroupChat = computed(() => props.sessionMembers.length > 0)

// 计算属性：判断是否可以发送消息
const canSend = computed(() => {
  return messageText.value.trim().length > 0 && !sending.value
})

// 计算属性：判断是否接近字数限制
const isNearLimit = computed(() => {
  return messageText.value.length > maxLength.value * 0.9
})

// 计算属性：文件上传请求头
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + getToken(),
}))

// 处理键盘按下事件
const handleKeyDown = event => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

// 处理输入事件，自动调整输入框高度
const handleInput = () => {
  nextTick(() => {
    const textarea = textInput.value.$el.querySelector('textarea')
    if (textarea) {
      const lines = messageText.value.split('\n').length
      const newRows = Math.min(Math.max(lines, 3), 6)
      if (newRows !== inputRows.value) {
        inputRows.value = newRows
      }
    }
  })
}

// 处理输入框获得焦点事件
const handleFocus = () => {
  isFocused.value = true
  showInputHint.value = true
  inputHintText.value = '按 Enter 发送，Shift + Enter 换行'

  if (inputHintTimer.value) {
    clearTimeout(inputHintTimer.value)
  }

  inputHintTimer.value = setTimeout(() => {
    showInputHint.value = false
  }, 3000)
}

// 处理输入框失去焦点事件
const handleBlur = () => {
  isFocused.value = false
  setTimeout(() => {
    if (!isFocused.value) {
      showInputHint.value = false
    }
  }, 200)
}

// 显示输入提示
const showHint = (text, duration = 3000) => {
  inputHintText.value = text
  showInputHint.value = true

  if (inputHintTimer.value) {
    clearTimeout(inputHintTimer.value)
  }

  inputHintTimer.value = setTimeout(() => {
    showInputHint.value = false
  }, duration)
}

// 发送文本消息
const sendMessage = async () => {
  if (!canSend.value) return

  const content = messageText.value.trim()
  if (!content) return

  sending.value = true

  try {
    await emit('send-message', {
      type: 'text',
      content,
      replyTo: props.replyMessage?.id,
      mentions: mentionedMembers.value.map(m => m.id),
    })

    messageText.value = ''
    inputRows.value = 3
    mentionedMembers.value = []

    if (props.replyMessage) {
      emit('cancel-reply')
    }
  } catch (error) {
    ElMessage.error('发送失败，请重试')
  } finally {
    sending.value = false
  }
}

// 切换表情选择器显示状态
const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

// 处理表情选择事件
const handleEmojiSelect = emoji => {
  messageText.value += emoji
  showEmojiPicker.value = false
  nextTick(() => {
    textInput.value?.focus()
  })
}

// 处理@提醒按钮点击事件
const handleAtClick = event => {
  if (!isGroupChat.value) {
    ElMessage.info('@提醒功能仅在群聊中可用')
    return
  }

  // 获取按钮位置用于定位选择器
  const rect = event.target.getBoundingClientRect()
  mentionPosition.value = {
    x: rect.left,
    y: rect.top - 10,
  }
  showMentionSelector.value = true
}

// 处理@选择器选中成员
const handleMentionSelect = member => {
  // 在输入框中插入@提醒
  const mentionText = `@${member.name} `
  messageText.value += mentionText

  // 记录被@的成员
  if (!mentionedMembers.value.find(m => m.id === member.id)) {
    mentionedMembers.value.push(member)
  }

  // 关闭选择器并聚焦输入框
  showMentionSelector.value = false
  nextTick(() => {
    textInput.value?.focus()
  })
}

// 文件上传前的验证
const beforeFileUpload = file => {
  const maxSize = 100 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过100MB')
    return false
  }
  return true
}

// 图片上传前的验证
const beforeImageUpload = file => {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }
  return true
}

// 处理文件上传成功事件
const handleFileSuccess = (response, file) => {
  emit('send-file', {
    name: file.name,
    size: file.size,
    url: response.data.url,
    type: file.type,
  })
}

// 处理图片上传成功事件
const handleImageSuccess = (response, file) => {
  emit('send-image', {
    url: response.data.url,
    name: file.name,
    size: file.size,
  })
}

// 处理文件上传失败事件
const handleUploadError = () => {
  uploadProgress.value.show = false
  ElMessage.error('文件上传失败')
}

// 处理文件上传进度
const handleFileProgress = (event, file) => {
  uploadProgress.value.show = true
  uploadProgress.value.percent = Math.floor(event.percent)
  uploadProgress.value.status = '上传中...'
  uploadProgress.value.filename = file.name
  uploadProgress.value.icon = 'el-icon-loading'
}

// 开始录音
const startRecording = () => {
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    ElMessage.error('浏览器不支持录音功能')
    return
  }

  navigator.mediaDevices
    .getUserMedia({ audio: true })
    .then(() => {
      isRecording.value = true
      recordingTime.value = 0
      startRecordingTimer()
    })
    .catch(() => {
      ElMessage.error('无法访问麦克风')
    })
}

// 停止录音
const stopRecording = () => {
  if (!isRecording.value) return

  isRecording.value = false
  stopRecordingTimer()

  if (recordingTime.value < 1) {
    showHint('录音时间太短，至少需要1秒')
    return
  }

  emit('send-voice', {
    duration: recordingTime.value,
    url: 'voice_url_here',
  })

  showHint(`录音完成，时长 ${recordingTime.value} 秒`)
}

// 取消录音
const cancelRecording = () => {
  if (!isRecording.value) return

  isRecording.value = false
  stopRecordingTimer()
  showHint('录音已取消')
}

// 格式化录音时间
const formatRecordingTime = seconds => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 启动录音计时器
const startRecordingTimer = () => {
  recordingTimer.value = setInterval(() => {
    recordingTime.value++
    if (recordingTime.value >= 60) {
      stopRecording()
    }
  }, 1000)
}

// 停止录音计时器
const stopRecordingTimer = () => {
  if (recordingTimer.value) {
    clearInterval(recordingTimer.value)
    recordingTimer.value = null
  }
}

// 发起视频通话
const startVideoCall = () => {
  emit('start-call', { type: 'video' })
}

// 发起语音通话
const startVoiceCall = () => {
  emit('start-call', { type: 'voice' })
}

// 处理更多功能菜单命令
const handleMoreCommand = command => {
  switch (command) {
    case 'location':
      showLocationPicker.value = true
      break
    case 'vote':
      showVoteDialog.value = true
      break
    case 'code':
      showCodeSnippetDialog.value = true
      break
  }
}

/**
 * 处理位置选择确认
 * @param {Object} locationData - 位置数据
 */
const handleLocationConfirm = (locationData) => {
  emit('send-location', locationData)
}

/**
 * 处理投票创建确认
 * @param {Object} voteData - 投票数据
 */
const handleVoteConfirm = (voteData) => {
  emit('send-vote', voteData)
}

/**
 * 处理代码片段发送
 * @param {Object} codeData - 代码数据
 */
const handleCodeSend = (codeData) => {
  emit('send-code', codeData)
}

// 取消回复消息
const cancelReply = () => {
  emit('cancel-reply')
}

// 组件挂载时的初始化
onMounted(() => {
  // 组件挂载后的初始化
})

// 组件卸载时的清理
onUnmounted(() => {
  stopRecordingTimer()
})
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.chat-input-container {
  border-top: 1px solid $border-base;
  background: $bg-white;
  position: relative;
  transition: all $transition-base $ease-base;
}

.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm $spacing-lg;
  border-bottom: 1px solid $border-light;
  background: linear-gradient(to bottom, $bg-white, $bg-light);
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.toolbar-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: $border-radius-base;
  cursor: pointer;
  transition: all $transition-base $ease-out;
  overflow: hidden;

  i {
    font-size: 18px;
    color: $text-secondary;
    transition: color $transition-base $ease-base;
  }

  .button-ripple {
    position: absolute;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle, rgba($primary-color, 0.2) 0%, transparent 70%);
    transform: scale(0);
    opacity: 0;
    transition: all 0.4s ease;
  }

  &:hover {
    background: $primary-color-light;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba($primary-color, 0.15);

    i {
      color: $primary-color;
    }

    .button-ripple {
      transform: scale(1);
      opacity: 1;
    }
  }

  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba($primary-color, 0.1);
  }

  &.active {
    background: $primary-color-light;
    i {
      color: $primary-color;
    }
  }

  &.recording {
    background: rgba($error-color, 0.08);
    i {
      color: $error-color;
      animation: pulse 1s infinite;
    }
  }

  .recording-indicator {
    position: absolute;
    top: 4px;
    right: 4px;
    width: 8px;
    height: 8px;
    background: $error-color;
    border-radius: $border-radius-round;
    animation: blink 1s infinite;
  }
}

.upload-item {
  display: inline-block;
}

.input-area {
  padding: $spacing-md $spacing-lg;
  position: relative;
}

.reply-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, $bg-light 0%, $bg-white 100%);
  border-left: 3px solid $primary-color;
  padding: $spacing-sm $spacing-md;
  margin-bottom: $spacing-sm;
  border-radius: $border-radius-sm;
  box-shadow: $shadow-sm;
  transition: all $transition-base $ease-base;

  .reply-content {
    flex: 1;
    min-width: 0;
  }

  .reply-sender {
    font-size: 12px;
    color: $primary-color;
    font-weight: 500;
    margin-bottom: $spacing-xs;
  }

  .reply-text {
    font-size: 12px;
    color: $text-secondary;
    @include text-ellipsis;
  }

  .reply-close {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border: none;
    background: transparent;
    cursor: pointer;
    color: $text-tertiary;
    border-radius: $border-radius-sm;
    transition: all 0.2s ease;

    &:hover {
      background: $bg-active;
      color: $text-secondary;
      transform: rotate(90deg);
    }
  }
}

.text-input-wrapper {
  position: relative;
  margin-bottom: $spacing-md;
}

.custom-textarea {
  :deep(.el-textarea__inner) {
    border-radius: $border-radius-base;
    border: 2px solid $border-base;
    transition: all $transition-base $ease-base;
    font-size: 14px;
    line-height: 1.6;
    padding: $spacing-sm $spacing-md;

    &:focus {
      border-color: $primary-color;
      box-shadow: 0 0 0 3px rgba($primary-color, 0.1);
    }

    &:hover {
      border-color: $border-dark;
    }
  }
}

.char-count {
  position: absolute;
  bottom: $spacing-sm;
  right: $spacing-sm;
  display: inline-flex;
  align-items: center;
  gap: $spacing-xs;
  font-size: 12px;
  color: $text-tertiary;
  background: rgba($bg-white, 0.95);
  padding: $spacing-xs $spacing-sm;
  border-radius: $border-radius-base;
  box-shadow: $shadow-sm;
  transition: all $transition-base $ease-base;

  i {
    font-size: 12px;
  }

  &.near-limit {
    color: $error-color;
    background: rgba($error-color, 0.08);
    font-weight: 500;
  }
}

.input-hint {
  position: absolute;
  top: -30px;
  left: 0;
  display: inline-flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-xs $spacing-md;
  background: linear-gradient(135deg, $primary-color 0%, $primary-color-active 100%);
  color: white;
  font-size: 12px;
  border-radius: $border-radius-sm;
  box-shadow: 0 4px 12px rgba($primary-color, 0.3);

  i {
    font-size: 14px;
  }
}

.send-button-wrapper {
  display: flex;
  justify-content: flex-end;
}

:deep(.el-button) {
  padding: $spacing-sm $spacing-xl;
  border-radius: $border-radius-base;
  font-weight: 500;
  transition: all $transition-base $ease-out;

  i {
    margin-right: $spacing-xs;
  }

  &.send-ready {
    background: linear-gradient(135deg, $primary-color 0%, $primary-color-active 100%);
    border: none;
    box-shadow: 0 4px 12px rgba($primary-color, 0.3);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba($primary-color, 0.4);
    }

    &:active {
      transform: translateY(0);
    }
  }

  &.send-sending {
    background: $success-color;
    border: none;
  }

  &:disabled {
    background: $bg-hover;
    border-color: $border-dark;
    color: $text-placeholder;
    cursor: not-allowed;
    transform: none !important;
    box-shadow: none !important;
  }
}

.emoji-picker-container,
.at-picker-container {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  z-index: $z-index-dropdown;
}

.recording-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: $bg-mask;
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: $z-index-modal;
  animation: fadeIn $transition-base $ease-base;
}

.recording-modal {
  background: $bg-white;
  border-radius: $border-radius-xl;
  padding: 40px $spacing-xxl;
  text-align: center;
  box-shadow: $shadow-lg;
  animation: slideUp $transition-base $ease-base;
  max-width: 90%;
  width: 320px;
}

.recording-icon-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto $spacing-xl;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recording-waves {
  position: absolute;
  width: 100%;
  height: 100%;

  span {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
    height: 100%;
    border: 2px solid $error-color;
    border-radius: $border-radius-round;
    opacity: 0;
    animation: wave 1.5s ease-out infinite;

    &:nth-child(2) {
      animation-delay: 0.5s;
    }

    &:nth-child(3) {
      animation-delay: 1s;
    }
  }
}

.recording-icon {
  font-size: 48px;
  color: $error-color;
  animation: pulse 1s infinite;
  position: relative;
  z-index: 1;
}

.recording-text {
  margin-bottom: $spacing-xl;
}

.recording-status {
  font-size: 16px;
  color: $text-primary;
  margin-bottom: $spacing-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;

  i {
    animation: spin 1s linear infinite;
  }
}

.recording-time {
  font-size: 32px;
  font-weight: bold;
  color: $error-color;
  font-family: 'Monaco', 'Consolas', monospace;
}

.recording-tip {
  font-size: 14px;
  color: $text-secondary;
  margin-bottom: $spacing-xl;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
}

.recording-cancel {
  display: inline-flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-xl;
  background: $bg-hover;
  border: none;
  border-radius: $border-radius-base;
  color: $text-secondary;
  font-size: 14px;
  cursor: pointer;
  transition: all $transition-base $ease-base;

  &:hover {
    background: $bg-active;
    color: $text-primary;
  }
}

.upload-progress-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: $bg-mask;
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: $z-index-modal;
  animation: fadeIn $transition-base $ease-base;
}

.upload-progress-modal {
  background: $bg-white;
  border-radius: $border-radius-lg;
  padding: $spacing-xxl;
  text-align: center;
  box-shadow: $shadow-lg;
  animation: slideUp $transition-base $ease-base;
  width: 320px;
  max-width: 90%;
}

.upload-progress-icon {
  font-size: 48px;
  color: $primary-color;
  margin-bottom: $spacing-lg;

  i {
    animation: spin 1s linear infinite;
  }
}

.upload-progress-text {
  margin-bottom: $spacing-xl;
}

.upload-status {
  font-size: 16px;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.upload-filename {
  font-size: 14px;
  color: $text-secondary;
  @include text-ellipsis;
}

.upload-progress-bar {
  width: 100%;
  height: 8px;
  background: $bg-hover;
  border-radius: $border-radius-sm;
  overflow: hidden;
  margin-bottom: $spacing-md;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, $primary-color 0%, $primary-color-active 100%);
  border-radius: $border-radius-sm;
  transition: width $transition-base $ease-base;
}

.upload-progress-percent {
  font-size: 18px;
  font-weight: bold;
  color: $primary-color;
}

// 动画定义
@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes wave {
  0% {
    transform: translate(-50%, -50%) scale(0.8);
    opacity: 0.8;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 过渡动画
.reply-slide-enter-active,
.reply-slide-leave-active {
  transition: all 0.3s ease;
}

.reply-slide-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.reply-slide-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.count-fade-enter-active,
.count-fade-leave-active {
  transition: all 0.3s ease;
}

.count-fade-enter-from,
.count-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.hint-fade-enter-active,
.hint-fade-leave-active {
  transition: all 0.3s ease;
}

.hint-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.hint-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.send-button-enter-active,
.send-button-leave-active {
  transition: all 0.3s ease;
}

.send-button-enter-from,
.send-button-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

.recording-fade-enter-active,
.recording-fade-leave-active {
  transition: all 0.3s ease;
}

.recording-fade-enter-from,
.recording-fade-leave-to {
  opacity: 0;
}

.upload-fade-enter-active,
.upload-fade-leave-active {
  transition: all 0.3s ease;
}

.upload-fade-enter-from,
.upload-fade-leave-to {
  opacity: 0;
}

// 响应式设计
@media (max-width: $breakpoint-md) {
  .input-toolbar {
    padding: $spacing-xs $spacing-md;
  }

  .toolbar-button {
    width: 32px;
    height: 32px;

    i {
      font-size: 16px;
    }
  }

  .input-area {
    padding: $spacing-sm $spacing-md;
  }

  .recording-modal {
    padding: $spacing-xl;
    margin: $spacing-lg;
    width: auto;
  }

  .recording-icon {
    font-size: 36px;
  }

  .recording-time {
    font-size: 28px;
  }

  .upload-progress-modal {
    padding: $spacing-xl;
    margin: $spacing-lg;
    width: auto;
  }

  :deep(.el-button) {
    padding: $spacing-sm $spacing-xl;
    font-size: 14px;
  }
}

@media (max-width: $breakpoint-xs) {
  .toolbar-left,
  .toolbar-right {
    gap: $spacing-xs;
  }

  .toolbar-button {
    width: 30px;
    height: 30px;

    i {
      font-size: 14px;
    }
  }

  .input-area {
    padding: $spacing-xs $spacing-sm;
  }

  .custom-textarea {
    :deep(.el-textarea__inner) {
      font-size: 13px;
      padding: $spacing-sm $spacing-sm;
    }
  }

  .char-count {
    font-size: 11px;
    padding: 3px $spacing-xs;
  }

  .input-hint {
    font-size: 11px;
    padding: $spacing-xs $spacing-sm;
  }
}

@media (hover: none) and (pointer: coarse) {
  .toolbar-button {
    &:active {
      background: $primary-color-light;
      transform: scale(0.95);
    }
  }

  .recording-cancel {
    &:active {
      background: $border-dark;
    }
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
