<template>
  <div class="chat-input-container" :class="{ 'is-resizing': isResizing }" :style="{ minHeight: containerHeight + 'px' }">
    <div
      class="resize-handle"
      :class="{ 'is-active': isResizing }"
      @mousedown="startResize"
      @dblclick="resetHeight"
    >
      <div class="resize-indicator">
        <span class="resize-dots"></span>
      </div>

      <!-- 高度指示器 -->
      <transition name="height-indicator">
        <div v-if="isResizing" class="height-indicator">
          <span class="height-value">{{ Math.round(containerHeight) }}px</span>
          <span class="height-hint">拖拽调整高度</span>
        </div>
      </transition>
    </div>

    <!-- 工具栏 -->
    <div class="input-toolbar">
      <div class="toolbar-left">
        <!-- 媒体发送组 -->
        <div class="toolbar-group">
          <el-tooltip content="表情" placement="top">
            <button class="toolbar-btn" :class="{ active: showEmojiPicker }" @click.stop="toggleEmojiPicker">
              <el-icon><ChatDotRound /></el-icon>
            </button>
          </el-tooltip>

          <el-tooltip content="图片" placement="top">
            <button class="toolbar-btn" @click="triggerImageUpload">
              <el-icon><Picture /></el-icon>
            </button>
          </el-tooltip>

          <el-tooltip content="文件" placement="top">
            <button class="toolbar-btn" @click="triggerFileUpload">
              <el-icon><FolderOpened /></el-icon>
            </button>
          </el-tooltip>

          <el-tooltip content="视频" placement="top">
            <button class="toolbar-btn" @click="handleVideoUpload">
              <span class="material-icons-outlined">videocam</span>
            </button>
          </el-tooltip>

          <el-tooltip content="截图 (Ctrl+Alt+A)" placement="top">
            <button class="toolbar-btn" @click="handleScreenshot">
              <span class="material-icons-outlined">content_cut</span>
            </button>
          </el-tooltip>

          <el-tooltip v-if="session?.type === 'GROUP'" content="@成员" placement="top">
            <button class="toolbar-btn" @click="handleAtMember">
              <span class="material-icons-outlined">alternate_email</span>
            </button>
          </el-tooltip>
        </div>
      </div>

      <div class="toolbar-right">
        <!-- AI 灵动回复 -->
        <el-tooltip content="AI 灵动回复" placement="top">
          <button class="toolbar-btn ai-reply-btn" @click="handleShowSmartReply">
            <span class="material-icons-outlined">auto_awesome</span>
          </button>
        </el-tooltip>
      </div>
    </div>

    <!-- 引用消息/正在回复 预览区 (还原钉钉样式) -->
    <div v-if="replyingMessage" class="reply-preview-container">
      <div class="reply-content-box">
        <span class="reply-user">{{ replyingMessage.senderName }}:</span>
        <span class="reply-text">{{ replyingMessage.content }}</span>
        <el-icon class="cancel-reply" @click="clearReply"><Close /></el-icon>
      </div>
    </div>

    <!-- 录音预览区域 -->
    <div v-if="voicePreview" class="voice-preview-container">
      <div class="voice-preview-box">
        <div class="voice-info">
          <span class="material-icons-outlined voice-icon">mic</span>
          <span class="voice-duration">{{ formatTime(voicePreview.duration) }}</span>
        </div>
        <div class="voice-waveform">
          <span
            v-for="(item, index) in 20"
            :key="index"
            class="wave-bar"
            :class="{ active: voicePreview.isPlaying && index < Math.floor((voicePreview.playProgress / 100) * 20) }"
          ></span>
        </div>
        <div class="voice-actions">
          <button class="voice-action-btn play-btn" @click="toggleVoicePlay">
            <span class="material-icons-outlined">
              {{ voicePreview.isPlaying ? 'pause' : 'play_arrow' }}
            </span>
          </button>
          <button class="voice-action-btn delete-btn" @click="deleteVoicePreview">
            <span class="material-icons-outlined">delete</span>
            删除
          </button>
          <button class="voice-action-btn send-btn" @click="sendVoicePreview">
            <span class="material-icons-outlined">send</span>
            发送
          </button>
        </div>
      </div>
    </div>

    <div v-if="editingMessage" class="edit-preview-container">
      <div class="edit-content-box">
        <span class="edit-label">正在编辑:</span>
        <span class="edit-text">{{ editingMessage.content }}</span>
        <el-icon class="cancel-edit" @click="$emit('cancel-edit')"><Close /></el-icon>
      </div>
    </div>

    <!-- 输入核心区域 -->
    <div
      class="input-area"
      :class="{ 'is-drag-over': isDragOver }"
      @dragenter="handleDragEnter"
      @dragleave="handleDragLeave"
      @dragover="handleDragOver"
      @drop.prevent="handleDrop"
    >
      <!-- 语音录制模式 -->
      <VoiceRecorder
        v-if="isVoiceMode"
        @record-complete="handleVoiceRecordComplete"
        @cancel="handleVoiceCancel"
      />

      <!-- 文字输入模式 -->
      <textarea
        v-else
        ref="textareaRef"
        v-model="messageContent"
        class="message-input"
        :placeholder="session?.type === 'GROUP' ? '发消息...' : '发消息...'"
        @input="handleInput"
        @keydown="handleKeydown"
        @paste="handlePaste"
      ></textarea>

      <div class="input-footer" v-if="!isVoiceMode">
        <span class="hint-text">{{ sendShortcutHint }}</span>
        <div class="footer-actions">
          <!-- 语音输入切换按钮 -->
          <el-tooltip content="按住说话" placement="top">
            <button
              class="footer-action-btn voice-btn"
              :class="{ active: isVoiceMode }"
              @click="toggleVoiceMode"
            >
              <el-icon><Microphone /></el-icon>
            </button>
          </el-tooltip>

          <button
            class="send-btn"
            :class="{ active: canSend }"
            :disabled="!canSend || sending"
            @click="handleSend"
          >
            {{ sending ? '发送中' : '发送' }}
          </button>
        </div>
      </div>
    </div>

    <EmojiPicker
      v-if="showEmojiPicker"
      :position="emojiPickerPosition"
      @select="selectEmoji"
      @close="showEmojiPicker = false"
    />
    <AtMemberPicker ref="atMemberPickerRef" :session-id="session?.id" @select="onAtSelect" />

    <!-- 快捷命令面板 -->
    <CommandPalette
      :show="showCommandPalette"
      :position="commandPalettePosition"
      @close="showCommandPalette = false"
      @select="handleCommandSelect"
    />

    <!-- 隐藏的视频文件输入 -->
    <input
      type="file"
      ref="videoInputRef"
      accept="video/mp4,video/webm,video/ogg,video/quicktime"
      style="display: none"
      @change="handleVideoFileChange"
    />

    <!-- 隐藏的图片文件输入 -->
    <input
      type="file"
      ref="imageInputRef"
      accept="image/*"
      style="display: none"
      @change="handleImageFileChange"
    />

    <!-- 隐藏的文件输入 -->
    <input
      type="file"
      ref="fileInputRef"
      style="display: none"
      @change="handleFileInputChange"
    />

    <!-- 截图预览对话框 -->
    <ScreenshotPreview
      v-model="showScreenshotPreview"
      :image-data="screenshotData"
      @send="handleSendScreenshot"
      @close="handleScreenshotPreviewClose"
    />

    <!-- 钉钉风格截图组件 -->
    <DingtalkScreenshot
      :visible="showScreenshotGuide"
      @confirm="handleSendScreenshotFromGuide"
      @close="showScreenshotGuide = false"
    />

    <!-- AI 灵动回复面板 -->
    <AiSmartReply
      v-model:visible="showSmartReply"
      :trigger-message="lastReceivedMessage"
      :position="smartReplyPosition"
      @select="handleSelectSmartReply"
    />

    <!-- 日程创建对话框 -->
    <ScheduleDialog
      v-model="showScheduleDialog"
      @saved="handleScheduleSaved"
    />
  </div>
</template>

<script setup>
import { ref, nextTick, computed, onMounted, onUnmounted, watch } from 'vue'
import { useStore } from 'vuex'
import { Close, ChatDotRound, Picture, FolderOpened, Phone, Microphone } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { setTodoReminder } from '@/api/im/marker'
import { captureScreenshot, isScreenshotSupported } from '@/utils/screenshot'
import EmojiPicker from '@/components/Chat/EmojiPicker.vue'
import AtMemberPicker from './AtMemberPicker.vue'
import VoiceRecorder from './VoiceRecorder.vue'
import ScreenshotPreview from './ScreenshotPreview.vue'
import DingtalkScreenshot from './DingtalkScreenshot.vue'
import CommandPalette from './CommandPalette.vue'
import AiSmartReply from './AiSmartReply.vue'
import ScheduleDialog from './ScheduleDialog.vue'

const props = defineProps({
  session: Object,
  sending: Boolean,
  replyingMessage: Object,
  editingMessage: Object,
  lastReceivedMessage: Object
})

const emit = defineEmits(['send', 'send-voice', 'upload-image', 'upload-file', 'upload-video', 'send-location', 'cancel-reply', 'cancel-edit', 'edit-confirm', 'input', 'start-call', 'start-video', 'send-screenshot', 'draft-change'])

const store = useStore()
const { sendMessage: wsSendMessage } = useImWebSocket()
const currentUser = computed(() => store.getters['user/currentUser'])

const messageContent = ref('')

// ========== 草稿管理功能 ==========
const DRAFT_STORAGE_KEY = 'im_message_drafts'
let currentConversationId = ref(null)

// 使用 Vuex store 管理草稿
// 立即保存草稿
const saveDraft = (conversationId, content) => {
  if (!conversationId) return
  store.dispatch('im/session/saveDraft', { conversationId, content })
  // 通知父组件草稿状态变化
  emit('draft-change', {
    conversationId,
    hasDraft: content.trim().length > 0,
    preview: content.trim().slice(0, 50)
  })
}

// 清除草稿
const clearDraft = (conversationId) => {
  if (!conversationId) return
  store.dispatch('im/session/clearDraft', conversationId)
  emit('draft-change', {
    conversationId,
    hasDraft: false,
    preview: ''
  })
}

// 加载草稿
const loadDraft = (conversationId) => {
  if (!conversationId) return
  const draftContent = store.getters['im/session/getDraftContent'](conversationId)
  if (draftContent) {
    messageContent.value = draftContent
    nextTick(() => autoResize())
  } else {
    messageContent.value = ''
  }
  currentConversationId.value = conversationId
}
const sendShortcutHint = computed(() => {
  const shortcut = store.state.im.settings.shortcuts.send
  return shortcut === 'ctrl-enter' ? '按 Ctrl + Enter 发送' : '按 Enter 发送'
})

const showEmojiPicker = ref(false)
const showScheduleDialog = ref(false)
const emojiPickerPosition = ref({ x: 0, y: 0 })
const textareaRef = ref(null)
const atMemberPickerRef = ref(null)
const imageInputRef = ref(null)
const fileInputRef = ref(null)
const videoInputRef = ref(null)
const isVoiceMode = ref(false)
const voicePreview = ref(null)
const voiceAudioElement = ref(null)
const voiceProgressInterval = ref(null)
const showScreenshotPreview = ref(false)
const screenshotData = ref(null)
const showScreenshotGuide = ref(false)

// AI 灵动回复状态
const showSmartReply = ref(false)
const smartReplyPosition = ref({ x: 0, y: 0 })

// ========== 快捷命令面板状态 ==========
const showCommandPalette = ref(false)
const commandPalettePosition = ref({ x: 0, y: 0 })

// 在光标位置显示命令面板
const showCommandPaletteAtCursor = () => {
  const tx = textareaRef.value
  if (!tx) return

  // 获取 textarea 的位置
  const rect = tx.getBoundingClientRect()

  // 计算命令面板显示位置（在输入框上方）
  commandPalettePosition.value = {
    x: rect.left,
    y: rect.top - 400 // 显示在输入框上方
  }

  showCommandPalette.value = true
}

// 处理命令选择
const handleCommandSelect = (commandId) => {
  // 移除输入框中的 /
  const currentContent = messageContent.value
  const cursorPos = textareaRef.value?.selectionStart || 0
  let newContent = currentContent

  // 找到 / 的位置并移除它
  let slashPos = cursorPos - 1
  while (slashPos >= 0 && currentContent[slashPos] !== '/') {
    slashPos--
  }

  if (slashPos >= 0) {
    newContent = currentContent.slice(0, slashPos) + currentContent.slice(cursorPos)
    messageContent.value = newContent
    nextTick(() => {
      textareaRef.value?.focus()
      autoResize()
    })
  }

  // 根据命令类型执行相应操作
  switch (commandId) {
    case 'document':
      // 触发文件上传
      emit('upload-file')
      break
    case 'schedule':
      // 创建日程
      handleCreateSchedule()
      break
    case 'meeting':
      // 触发视频会议
      emit('start-video')
      break
    case 'todo':
      // 创建待办消息
      handleCreateTodo()
      break
    case 'approval':
      // 创建审批
      handleCreateApproval()
      break
    case 'vote':
      ElMessage.info('投票功能即将上线')
      break
    case 'announcement':
      // 创建公告
      emit('create-announcement')
      break
    case 'checkin':
      // 触发位置分享
      handleLocation()
      break
    default:
      ElMessage.info(`命令 ${commandId} 即将上线`)
  }
}

// 拖拽上传状态
const isDragOver = ref(false)
let dragCounter = 0

// 钉钉风格高度逻辑
const containerHeight = ref(160)
const minHeight = 160
const maxHeight = 600
let isResizing = false
let startY = 0
let startHeight = 0

const startResize = (e) => {
  isResizing = true; startY = e.clientY; startHeight = containerHeight.value
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
  document.body.style.cursor = 'ns-resize'
}

const handleResize = (e) => {
  if (!isResizing) return
  const delta = startY - e.clientY
  const newH = startHeight + delta
  if (newH >= minHeight && newH <= maxHeight) containerHeight.value = newH
}

const stopResize = () => {
  isResizing = false; document.body.style.cursor = ''
  localStorage.setItem('im_input_height', containerHeight.value)
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
}

const resetHeight = () => containerHeight.value = 160

const insertAt = (nickname) => {
  const atText = `@${nickname} `
  const pos = textareaRef.value?.selectionStart || messageContent.value.length
  messageContent.value = messageContent.value.slice(0, pos) + atText + messageContent.value.slice(pos)
  nextTick(() => { textareaRef.value?.focus(); autoResize() })
}

const canSend = computed(() => messageContent.value.trim().length > 0)

const autoResize = () => {
  const tx = textareaRef.value
  if (!tx) return
  tx.style.height = 'auto'
  tx.style.height = tx.scrollHeight + 'px'
}

const handleInput = () => {
  autoResize()
  emit('input', messageContent.value)

  // 检查是否输入了 / 触发快捷命令
  const value = messageContent.value
  const cursorPos = textareaRef.value?.selectionStart || 0
  const lastChar = value.charAt(cursorPos - 1)

  // 如果输入了 / 且在消息开头或前面有空格，显示命令面板
  if (lastChar === '/' && (cursorPos === 1 || value.charAt(cursorPos - 2) === ' ')) {
    showCommandPaletteAtCursor()
  } else if (showCommandPalette.value && !value.includes('/')) {
    // 如果关闭了面板且不再有 /，隐藏面板
    showCommandPalette.value = false
  }

  // 发送输入状态（防抖）
  sendTypingIndicator()

  // 立即保存草稿（不使用防抖）
  if (props.session?.id) {
    saveDraft(props.session.id, messageContent.value)
  }
}

// 输入状态防抖发送
let typingTimer = null
let lastTypingSendTime = 0
const TYPING_DEBOUNCE = 1000 // 1秒内只发送一次
const TYPING_INTERVAL = 3000 // 每3秒重新发送一次输入状态

const sendTypingIndicator = () => {
  const now = Date.now()

  // 清除之前的定时器
  if (typingTimer) {
    clearTimeout(typingTimer)
  }

  // 检查是否需要发送（距离上次发送超过间隔时间）
  const shouldSend = now - lastTypingSendTime > TYPING_DEBOUNCE

  if (shouldSend && props.session?.id && messageContent.value.trim()) {
    lastTypingSendTime = now

    // 发送 typing 消息
    wsSendMessage({
      type: 'typing',
      data: {
        conversationId: props.session.id,
        userId: currentUser.value?.id
      }
    })

    // 设置下次重新发送的定时器
    typingTimer = setTimeout(() => {
      if (messageContent.value.trim()) {
        lastTypingSendTime = Date.now()
        wsSendMessage({
          type: 'typing',
          data: {
            conversationId: props.session.id,
            userId: currentUser.value?.id
          }
        })
      }
    }, TYPING_INTERVAL)
  }
}

const handleKeydown = (e) => {
  const sendShortcut = store.state.im.settings.shortcuts.send || 'enter'
  
  if (e.key === 'Enter') {
    if (sendShortcut === 'enter') {
      if (!e.shiftKey && !e.ctrlKey) {
        e.preventDefault()
        handleSend()
      }
    } else if (sendShortcut === 'ctrl-enter') {
      if (e.ctrlKey) {
        e.preventDefault()
        handleSend()
      }
    }
  }

  if (e.key === '@' && props.session?.type === 'GROUP') {
     setTimeout(() => atMemberPickerRef.value?.open(textareaRef.value.selectionStart), 50)
  }
}

const handleSend = async () => {
  const content = messageContent.value.trim()
  if (!content) return

  let success = false
  
  if (props.editingMessage) {
    emit('edit-confirm', content)
    success = true
  } else {
    // 发送消息
    emit('send', content)
    success = true
  }

  // 先清除输入状态
  if (props.session?.id) {
    store.dispatch('im/session/clearTyping', props.session?.id)
  }
  
  messageContent.value = ''
  nextTick(() => {
    if (textareaRef.value) textareaRef.value.style.height = 'auto'
    textareaRef.value?.focus()
  })
  
  // 延迟清除草稿，确保会话已更新
  if (success && props.session?.id) {
    // 等待2个tick，确保会话更新完成
    await nextTick()
    await nextTick()
    clearDraft(props.session.id)
  }
}

const clearReply = () => {
  emit('cancel-reply')
}

// 粘贴处理
const handlePaste = (e) => {
  const items = e.clipboardData?.items
  if (!items) return
  for (const item of items) {
    if (item.kind === 'file') {
      const file = item.getAsFile()
      if (file) {
        e.preventDefault()
        const formData = new FormData()
        formData.append('file', file)
        if (file.type.startsWith('image/')) emit('upload-image', formData)
        else emit('upload-file', formData)
      }
    }
  }
}

const handleDragEnter = (e) => {
  e.preventDefault()
  dragCounter++
  const files = e.dataTransfer?.files
  if (files && files.length > 0) {
    isDragOver.value = true
  }
}

const handleDragLeave = (e) => {
  dragCounter--
  if (dragCounter <= 0) {
    dragCounter = 0
    isDragOver.value = false
  }
}

const handleDragOver = (e) => {
  e.preventDefault()
}

const handleDrop = (e) => {
  e.preventDefault()
  dragCounter = 0
  isDragOver.value = false

  const files = e.dataTransfer?.files
  if (!files || files.length === 0) return
  for (const file of files) {
    const formData = new FormData()
    formData.append('file', file)
    if (file.type.startsWith('image/')) emit('upload-image', formData)
    else emit('upload-file', formData)
  }
}

const toggleEmojiPicker = () => {
  if (!showEmojiPicker.value) {
    // 计算位置 - 在输入框上方显示
    const inputArea = document.querySelector('.input-area')
    if (inputArea) {
      const rect = inputArea.getBoundingClientRect()
      // 在输入框上方，emojiPicker 高度约 280px
      emojiPickerPosition.value = {
        x: Math.max(10, rect.left),
        y: rect.top - 285  // 输入框上方 (280px 弹窗高度 + 5px 间距)
      }
    }
    showEmojiPicker.value = true
  } else {
    showEmojiPicker.value = false
  }
}
const selectEmoji = (emoji) => { 
  const pos = textareaRef.value?.selectionStart || messageContent.value.length
  messageContent.value = messageContent.value.slice(0, pos) + emoji + messageContent.value.slice(pos)
  showEmojiPicker.value = false
  nextTick(() => { textareaRef.value?.focus(); autoResize() })
}

const handleScreenshot = async () => {
  // 检查浏览器支持
  if (!isScreenshotSupported()) {
    ElMessage.warning('您的浏览器不支持截图功能，请使用 Chrome 72+、Edge 79+ 或 Firefox 66+')
    return
  }

  // 显示截图引导对话框
  showScreenshotGuide.value = true
}

// 导出方法供父组件调用（必须在函数定义之后）
defineExpose({ insertAt, triggerScreenshot: handleScreenshot })

// 发送截图
const handleSendScreenshot = async (blob) => {
  const formData = new FormData()
  formData.append('file', blob, 'screenshot.png')
  emit('send-screenshot', formData)

  showScreenshotPreview.value = false
  screenshotData.value = null
}

// 从自由截图组件发送截图
const handleSendScreenshotFromGuide = async (dataURL) => {
  if (!dataURL) return

  try {
    // 将 dataURL 转换为 Blob
    const response = await fetch(dataURL)
    const blob = await response.blob()

    // 发送截图
    const formData = new FormData()
    formData.append('file', blob, 'screenshot.png')
    emit('send-screenshot', formData)
  } catch (error) {
    console.error('发送截图失败:', error)
    ElMessage.error('发送截图失败')
  }
}

// 关闭截图预览
const handleScreenshotPreviewClose = () => {
  showScreenshotPreview.value = false
  screenshotData.value = null
}

const handleLocation = () => {
  // 获取用户位置
  if (!navigator.geolocation) {
    ElMessage.error('您的浏览器不支持定位功能')
    return
  }

  ElMessage.info('正在获取位置...')

  navigator.geolocation.getCurrentPosition(
    (position) => {
      const { latitude, longitude } = position.coords
      emit('send-location', {
        latitude,
        longitude,
        address: '' // 可以通过逆地理编码获取地址
      })
    },
    (error) => {
      console.error('获取位置失败:', error)
      ElMessage.error('获取位置失败，请检查定位权限')
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    }
  )
}

// 创建待办消息
const handleCreateTodo = async () => {
  try {
    const { value: todoContent } = await ElMessageBox.prompt('请输入待办事项', '创建待办', {
      confirmButtonText: '创建',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：下午3点开会'
    })

    if (!todoContent || !todoContent.trim()) {
      ElMessage.warning('待办内容不能为空')
      return
    }

    // 询问提醒时间
    const { value: reminderOption } = await ElMessageBox.confirm(
      todoContent.trim() + '\n\n是否设置提醒？',
      '设置提醒',
      {
        confirmButtonText: '设置提醒',
        cancelButtonText: '不需要',
        distinguishCancelAndClose: true,
        type: 'info'
      }
    ).then(
      () => ({ setReminder: true }),
      (action) => {
        if (action === 'cancel') return { setReminder: false }
        return { setReminder: false }
      }
    )

    // 将待办内容放入输入框
    content.value = todoContent.trim()

    // 发送消息
    emit('send-message', { type: 'TEXT', content: content.value })

    // 清空输入框
    content.value = ''
  } catch (err) {
    // 用户取消
  }
}

// 创建审批
const handleCreateApproval = async () => {
  try {
    const { value: approvalContent } = await ElMessageBox.prompt(
      '请输入审批事项',
      '发起审批',
      {
        confirmButtonText: '发起',
        cancelButtonText: '取消',
        inputPlaceholder: '例如：申请报销xxx元'
      }
    )

    if (!approvalContent || !approvalContent.trim()) {
      ElMessage.warning('审批事项不能为空')
      return
    }

    // 发送审批请求消息
    content.value = `[审批申请] ${approvalContent.trim()}`
    emit('send-message', { type: 'TEXT', content: content.value })
    content.value = ''
    ElMessage.success('审批申请已发起')
  } catch (err) {
    // 用户取消
  }
}

// 创建日程
const handleCreateSchedule = () => {
  showScheduleDialog.value = true
}

const handleScheduleSaved = () => {
  ElMessage.success('日程已创建')
}

const handleVideoUpload = () => {
  videoInputRef.value?.click()
}

const triggerImageUpload = () => {
  imageInputRef.value?.click()
}

const triggerFileUpload = () => {
  fileInputRef.value?.click()
}

const handleVideoFileChange = async () => {
  const file = videoInputRef.value?.files?.[0]
  if (!file) return

  // 验证视频类型和大小
  const validTypes = ['video/mp4', 'video/webm', 'video/ogg', 'video/quicktime']
  const maxSize = 100 * 1024 * 1024 // 100MB

  if (!validTypes.some(type => file.type.includes(type))) {
    ElMessage.error('支持的视频格式：MP4、WebM、OGG')
    return
  }

  if (file.size > maxSize) {
    ElMessage.error('视频大小不能超过100MB')
    return
  }

  // 创建视频预览
  const url = URL.createObjectURL(file)
  emit('upload-video', { file, url })
}

const handleImageFileChange = async () => {
  const file = imageInputRef.value?.files?.[0]
  if (!file) return

  // 验证图片类型和大小
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/svg+xml']
  const maxSize = 10 * 1024 * 1024 // 10MB

  if (!validTypes.includes(file.type)) {
    ElMessage.error('支持的图片格式：JPG、PNG、GIF、WebP、SVG')
    return
  }

  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过10MB')
    return
  }

  emit('upload-image', file)
  // 清空输入
  imageInputRef.value.value = ''
}

const handleFileInputChange = async () => {
  const file = fileInputRef.value?.files?.[0]
  if (!file) return

  // 验证文件大小
  const maxSize = 100 * 1024 * 1024 // 100MB

  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过100MB')
    return
  }

  emit('upload-file', file)
  // 清空输入
  fileInputRef.value.value = ''
}

const handleAtMember = () => {
  if (props.session?.type === 'GROUP') {
    atMemberPickerRef.value?.open(textareaRef.value.selectionStart || 0)
  }
}

const onAtSelect = (m) => insertAt(m.nickname || m.userName)

// 切换语音模式
const toggleVoiceMode = () => {
  isVoiceMode.value = !isVoiceMode.value
  if (isVoiceMode.value) {
    nextTick(() => textareaRef.value?.blur())
  } else {
    nextTick(() => textareaRef.value?.focus())
  }
}

// 录音完成处理
const handleVoiceRecordComplete = ({ blob, url, duration }) => {
  voicePreview.value = {
    blob,
    url,
    duration,
    isPlaying: false,
    playProgress: 0
  }
  isVoiceMode.value = false
}

// 录音取消处理
const handleVoiceCancel = () => {
  voicePreview.value = null
  isVoiceMode.value = false
}

// 播放/暂停录音预览
const toggleVoicePlay = () => {
  if (!voicePreview.value) return

  if (!voiceAudioElement.value) {
    voiceAudioElement.value = new Audio(voicePreview.value.url)
    voiceAudioElement.value.onended = () => {
      voicePreview.value.isPlaying = false
      voicePreview.value.playProgress = 0
      if (voiceProgressInterval.value) {
        clearInterval(voiceProgressInterval.value)
        voiceProgressInterval.value = null
      }
    }
  }

  if (voicePreview.value.isPlaying) {
    voiceAudioElement.value.pause()
    if (voiceProgressInterval.value) {
      clearInterval(voiceProgressInterval.value)
      voiceProgressInterval.value = null
    }
    voicePreview.value.isPlaying = false
  } else {
    voiceAudioElement.value.play()
    voiceProgressInterval.value = setInterval(() => {
      if (voiceAudioElement.value && voiceAudioElement.value.duration) {
        voicePreview.value.playProgress = (voiceAudioElement.value.currentTime / voiceAudioElement.value.duration) * 100
      }
    }, 100)
    voicePreview.value.isPlaying = true
  }
}

// 删除录音预览
const deleteVoicePreview = () => {
  if (voicePreview.value && voicePreview.value.url) {
    URL.revokeObjectURL(voicePreview.value.url)
  }
  if (voiceAudioElement.value) {
    voiceAudioElement.value.pause()
    voiceAudioElement.value = null
  }
  if (voiceProgressInterval.value) {
    clearInterval(voiceProgressInterval.value)
    voiceProgressInterval.value = null
  }
  voicePreview.value = null
}

// 发送录音预览
const sendVoicePreview = () => {
  if (!voicePreview.value) return

  const file = new File([voicePreview.value.blob], `voice_${Date.now()}.webm`, { type: 'audio/webm' })
  emit('send-voice', {
    file,
    duration: voicePreview.value.duration
  })
  deleteVoicePreview()
}

// 格式化时间
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 显示 AI 灵动回复面板
const handleShowSmartReply = () => {
  const inputArea = document.querySelector('.chat-input-container')
  if (!inputArea) return

  const rect = inputArea.getBoundingClientRect()
  smartReplyPosition.value = {
    x: rect.right - 360,
    y: rect.top - 480
  }
  showSmartReply.value = true
}

// 选择 AI 灵动回复
const handleSelectSmartReply = (replyText) => {
  messageContent.value = replyText
  nextTick(() => {
    autoResize()
    textareaRef.value?.focus()
  })
}

// 监听回复消息，自动获取焦点
watch(() => props.replyingMessage, (val) => {
  if (val) nextTick(() => textareaRef.value?.focus())
})

// 监听会话变化，加载草稿
watch(() => props.session?.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    // 保存旧会话的草稿
    if (oldId && messageContent.value.trim()) {
      saveDraft(oldId, messageContent.value)
    }
    // 加载新会话的草稿
    loadDraft(newId)
  }
}, { immediate: true })

// 监听输入内容变化，更新输入状态
watch(messageContent, (newContent, oldContent) => {
  if (newContent !== oldContent) {
    // 更新输入状态
    if (newContent.trim().length > 0) {
      store.dispatch('im/session/setTyping', {
        conversationId: props.session?.id,
        isTyping: true
      })
    } else {
      // 延迟清除输入状态，避免在发送时立即清除
      setTimeout(() => {
        store.dispatch('im/session/clearTyping', props.session?.id)
      }, 100)
    }
  }
})
onMounted(() => {
  const saved = localStorage.getItem('im_input_height')
  if (saved) containerHeight.value = parseInt(saved)

  // 加载所有草稿
  store.dispatch('im/session/loadDrafts')

  // 注册全局截图快捷键 (Ctrl+Alt+A / Cmd+Shift+A)
  const handleGlobalKeydown = (e) => {
    // Windows: Ctrl+Alt+A, Mac: Cmd+Shift+A
    const isWinShortcut = e.ctrlKey && e.altKey && e.key === 'a'
    const isMacShortcut = e.metaKey && e.shiftKey && e.key === 'a'

    if ((isWinShortcut || isMacShortcut) && !showEmojiPicker.value && !isVoiceMode.value) {
      e.preventDefault()
      handleScreenshot()
    }
  }

  document.addEventListener('keydown', handleGlobalKeydown)

  // 保存引用以便在 onUnmounted 中移除
  window._messageInputKeydownHandler = handleGlobalKeydown
})

onUnmounted(() => {
  // 清理输入状态定时器
  if (typingTimer) {
    clearTimeout(typingTimer)
    typingTimer = null
  }
  
  // 清理语音资源
  if (voiceAudioElement.value) {
    voiceAudioElement.value.pause()
    voiceAudioElement.value = null
  }
  if (voiceProgressInterval.value) {
    clearInterval(voiceProgressInterval.value)
    voiceProgressInterval.value = null
  }
  if (voicePreview.value && voicePreview.value.url) {
    URL.revokeObjectURL(voicePreview.value.url)
  }
  
  // 保存当前草稿
  if (currentConversationId.value && messageContent.value.trim()) {
    saveDraft(currentConversationId.value, messageContent.value)
  }
  // 清除输入状态
  if (currentConversationId.value) {
    store.dispatch('im/session/clearTyping', currentConversationId.value)
  }
  // 移除全局键盘事件监听
  if (window._messageInputKeydownHandler) {
    document.removeEventListener('keydown', window._messageInputKeydownHandler)
    delete window._messageInputKeydownHandler
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 容器
// ============================================================================
.chat-input-container {
  background: var(--dt-bg-input);
  display: flex;
  flex-direction: column;
  position: relative;
  border-top: 1px solid var(--dt-border-light);
  padding: 8px 16px 16px;
  transition: background var(--dt-transition-base);
  z-index: 10; // 确保输入容器在正确的层级

  .dark & {
    border-top-color: var(--dt-border-dark);
  }
}

// ============================================================================
// 调整手柄
// ============================================================================
.chat-input-container {
  &.is-resizing {
    box-shadow: 0 -4px 16px rgba(22, 119, 255, 0.15);
    border-top-color: var(--dt-brand-color);
  }
}

.resize-handle {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 8px;
  cursor: ns-resize;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s var(--dt-ease-out);

  &:hover {
    .resize-indicator {
      .resize-dots {
        &::before,
        &::after {
          opacity: 1;
        }
      }
    }
  }

  &.is-active {
    height: 32px;

    .resize-indicator {
      width: 48px;
      height: 4px;
      background: linear-gradient(90deg, var(--dt-brand-color), var(--dt-brand-color));
      opacity: 1;

      .resize-dots {
        background-size: 4px 4px;

        &::before,
        &::after {
          opacity: 1;
        }
      }
    }

    .height-indicator {
      opacity: 1;
      transform: translateY(-8px);
    }
  }

  .resize-indicator {
    width: 36px;
    height: 3px;
    background: var(--dt-border-color);
    border-radius: var(--dt-radius-full);
    opacity: 0.6;
    transition: all 0.25s var(--dt-ease-out);
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;

    .resize-dots {
      position: relative;
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;

      // 创建虚线效果
      background-image: linear-gradient(
        90deg,
        var(--dt-border-color) 50%,
        transparent 50%
      );
      background-size: 6px 100%;
      background-repeat: repeat-x;
      transition: all 0.25s var(--dt-ease-out);

      &::before,
      &::after {
        content: '';
        position: absolute;
        width: 8px;
        height: 8px;
        background: var(--dt-brand-color);
        border-radius: 50%;
        opacity: 0;
        transition: all 0.25s var(--dt-ease-out);
      }

      &::before {
        left: -4px;
      }

      &::after {
        right: -4px;
      }
    }
  }

  // 高度指示器
  .height-indicator {
    position: absolute;
    top: 100%;
    left: 50%;
    transform: translateX(-50%) translateY(-4px);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    padding: 6px 12px;
    background: var(--dt-brand-color);
    color: #fff;
    border-radius: 0 0 6px 6px;
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
    pointer-events: none;
    z-index: 100;

    .height-value {
      font-size: 14px;
      font-weight: 600;
      font-variant-numeric: tabular-nums;
    }

    .height-hint {
      font-size: 10px;
      opacity: 0.8;
    }
  }
}

// 高度指示器过渡动画
.height-indicator-enter-active,
.height-indicator-leave-active {
  transition: all 0.2s var(--dt-ease-out);
}

.height-indicator-enter-from,
.height-indicator-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(0);
}

// 双击重置的反馈动画
@keyframes resetPulse {
  0% { background: transparent; }
  50% { background: rgba(22, 119, 255, 0.15); }
  100% { background: transparent; }
}

.chat-input-container {
  &:has(.resize-handle:active) {
    animation: resetPulse 0.3s var(--dt-ease-out);
  }
}

// ============================================================================
// 工具栏
// ============================================================================
.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  gap: 12px;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 4px;

    // 工具按钮分组
    .toolbar-group {
      display: flex;
      align-items: center;
      gap: 2px;
      padding: 4px;
      background: var(--dt-bg-input);
      border-radius: var(--dt-radius-lg);
      transition: all var(--dt-transition-fast);
    }

    .toolbar-btn {
      position: relative;
      width: 28px;
      height: 28px;
      background: transparent;
      border: none;
      padding: 0;
      cursor: pointer;
      color: var(--dt-text-secondary);
      border-radius: var(--dt-radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

      // 图标样式
      .el-icon,
      .material-icons-outlined {
        font-size: 16px;
        transition: transform 0.2s var(--dt-ease-out);
      }

      // 悬停效果
      &:hover {
        background: var(--dt-brand-bg);
        color: var(--dt-brand-color);
        transform: translateY(-1px);

        .el-icon,
        .material-icons-outlined {
          transform: scale(1.1);
        }

        // 悬停时的光晕效果
        &::after {
          content: '';
          position: absolute;
          inset: -4px;
          border-radius: var(--dt-radius-lg);
          background: var(--dt-brand-color);
          opacity: 0.1;
          pointer-events: none;
        }
      }

      // 点击效果
      &:active {
        transform: translateY(0) scale(0.95);
      }

      // 激活状态
      &.active {
        color: var(--dt-brand-color);
        background: var(--dt-brand-bg);
        box-shadow: inset 0 1px 2px rgba(22, 119, 255, 0.2);

        &::before {
          content: '';
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 16px;
          height: 2px;
          background: var(--dt-brand-color);
          border-radius: 1px;
        }
      }

      // AI 灵动回复按钮特殊样式
      &.ai-reply-btn {
        background: linear-gradient(135deg, rgba(147, 51, 234, 0.1) 0%, rgba(147, 51, 234, 0.15) 100%);
        color: #9333ea;

        .material-icons-outlined {
          background: linear-gradient(135deg, #9333ea, #7c3aed);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }

        &:hover {
          background: linear-gradient(135deg, rgba(147, 51, 234, 0.2) 0%, rgba(147, 51, 234, 0.25) 100%);
          color: #7c3aed;
          transform: translateY(-2px) scale(1.05);
          box-shadow: 0 4px 12px rgba(147, 51, 234, 0.25);

          &::after {
            opacity: 0.15;
          }
        }

        // 添加呼吸动画效果
        animation: aiPulse 3s ease-in-out infinite;
      }
    }

    // 分隔线
    .toolbar-divider {
      width: 1px;
      height: 24px;
      background: var(--dt-border-light);
      margin: 0 6px;
    }
  }

  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

// AI 按钮呼吸动画
@keyframes aiPulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(147, 51, 234, 0);
  }
  50% {
    box-shadow: 0 0 0 4px rgba(147, 51, 234, 0.2);
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
:global(.dark) {
  .input-toolbar {
    .toolbar-left {
      .toolbar-group {
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid var(--dt-border-dark);
      }
    }

    .toolbar-btn {
      color: var(--dt-text-secondary-dark);

      &:hover {
        background: rgba(22, 119, 255, 0.15);
        color: var(--dt-brand-color);

        &::after {
          opacity: 0.15;
        }
      }

      &.active {
        background: rgba(22, 119, 255, 0.15);
        color: var(--dt-brand-color);
      }

      &.ai-reply-btn {
        background: linear-gradient(135deg, rgba(147, 51, 234, 0.15) 0%, rgba(147, 51, 234, 0.1) 100%);
        color: #c084fc;

        .material-icons-outlined {
          background: linear-gradient(135deg, #c084fc, #a78bfa);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }

        &:hover {
          background: linear-gradient(135deg, rgba(147, 51, 234, 0.25) 0%, rgba(147, 51, 234, 0.2) 100%);
          color: #d8b4fe;
          box-shadow: 0 4px 12px rgba(147, 51, 234, 0.3);
        }
      }
    }

    .toolbar-divider {
      background: var(--dt-border-dark);
    }
  }
}

// ============================================================================
// 引用/编辑预览
// ============================================================================
.reply-preview-container,
.edit-preview-container,
.voice-preview-container {
  padding: 10px 12px;
  margin-bottom: 12px;
  border-radius: var(--dt-radius-md);
  background: var(--dt-bg-body);
  border-left: 3px solid var(--dt-brand-color);
  transition: all var(--dt-transition-base);

  .dark & {
    background: var(--dt-bg-hover-dark);
  }
}

.voice-preview-container {
  background: rgba(22, 119, 255, 0.05);
  border-color: var(--dt-brand-color);
}

.voice-preview-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.voice-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--dt-text-secondary);

  .voice-icon {
    font-size: 18px;
    color: var(--dt-brand-color);
  }

  .voice-duration {
    font-weight: 500;
    font-variant-numeric: tabular-nums;
  }
}

.voice-waveform {
  display: flex;
  gap: 2px;
  align-items: center;
  height: 24px;
  padding: 8px 0;

  .wave-bar {
    width: 3px;
    height: 8px;
    background: #d1d5db;
    border-radius: 2px;
    transition: all 0.2s;

    &.active {
      background: var(--dt-brand-color);
      height: 16px;
    }
  }
}

.voice-actions {
  display: flex;
  gap: 8px;

  .voice-action-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 12px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 13px;
    transition: all 0.2s;

    .material-icons-outlined {
      font-size: 16px;
    }

    &.play-btn {
      background: var(--dt-brand-color);
      color: #fff;

      &:hover {
        opacity: 0.9;
      }
    }

    &.delete-btn {
      background: #f2f3f5;
      color: #646a73;

      &:hover {
        background: #ff4d4f;
        color: #fff;
      }
    }

    &.send-btn {
      background: var(--dt-brand-color);
      color: #fff;

      &:hover {
        opacity: 0.9;
      }
    }
  }
}

.reply-preview-container,
.edit-preview-container {
  padding: 10px 12px;
  margin-bottom: 12px;
  border-radius: var(--dt-radius-md);
  background: var(--dt-bg-body);
  border-left: 3px solid var(--dt-brand-color);
  transition: all var(--dt-transition-base);

  .dark & {
    background: var(--dt-bg-hover-dark);
  }

  .reply-content-box,
  .edit-content-box {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;

    .reply-user,
    .edit-label {
      color: var(--dt-brand-color);
      font-weight: 500;
      flex-shrink: 0;
    }

    .reply-text,
    .edit-text {
      flex: 1;
      color: var(--dt-text-secondary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;

      .dark & {
        color: var(--dt-text-secondary-dark);
      }
    }

    .cancel-reply,
    .cancel-edit {
      cursor: pointer;
      color: var(--dt-text-tertiary);
      flex-shrink: 0;
      transition: color var(--dt-transition-fast);

      &:hover {
        color: var(--dt-error-color);
      }

      .dark & {
        color: var(--dt-text-tertiary-dark);
      }
    }
  }
}

.edit-preview-container {
  border-left-color: var(--dt-success-color);

  .edit-label {
    color: var(--dt-success-color);
  }
}

// ============================================================================
// 输入区域
// ============================================================================
.input-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  position: relative;
  transition: all 0.2s ease;

  // 拖拽上传状态
  &.is-drag-over {
    background: rgba(24, 144, 255, 0.04);
    border-radius: var(--dt-radius-md);
    box-shadow: inset 0 0 0 2px var(--dt-brand-color, #1890ff);

    &::after {
      content: '松开即可发送文件';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      padding: 12px 20px;
      background: var(--dt-brand-color, #1890ff);
      color: #fff;
      font-size: 14px;
      font-weight: 500;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.25);
      pointer-events: none;
      z-index: 10;
      display: flex;
      align-items: center;
      gap: 8px;
      white-space: nowrap;
      animation: drop-pulse 1s ease-in-out infinite;
    }

    .message-input {
      opacity: 0.3;
    }
  }
}

@keyframes drop-pulse {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    transform: translate(-50%, -50%) scale(1.05);
  }
}

.message-input {
  flex: 1;
  width: 100%;
  border: none;
  outline: none;
  resize: none;
  font-size: var(--dt-font-size-base);
  line-height: 1.6;
  color: var(--dt-text-primary);
  padding: 8px 0;
  min-height: 80px;
  background: transparent;
  font-family: var(--dt-font-family);

  &::placeholder {
    color: var(--dt-text-quaternary);
  }

  .dark & {
    color: var(--dt-text-primary-dark);
  }
}

.input-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
  margin-top: 8px;

  .hint-text {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    user-select: none;

    .dark & {
      color: var(--dt-text-tertiary-dark);
    }
  }

  .footer-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .footer-action-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-color);
    border-radius: var(--dt-radius-md);
    color: var(--dt-text-secondary);
    cursor: pointer;
    transition: all var(--dt-transition-fast);

    .el-icon {
      font-size: 16px;
    }

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-brand-color);
      border-color: var(--dt-brand-color);
    }

    &.active {
      background: var(--dt-brand-color);
      color: #fff;
      border-color: var(--dt-brand-color);
    }

    .dark & {
      background: var(--dt-bg-body-dark);
      border-color: var(--dt-border-dark);

      &:hover {
        background: var(--dt-bg-hover-dark);
      }
    }
  }

  .send-btn {
    padding: 8px 20px;
    border-radius: var(--dt-radius-md);
    border: none;
    background: var(--dt-bg-body);
    color: var(--dt-text-quaternary);
    font-size: 14px;
    font-weight: 500;
    cursor: default;
    transition: all var(--dt-transition-fast);
    display: inline-flex;
    align-items: center;
    justify-content: center;

    &.active {
      background: var(--dt-brand-color);
      color: #fff;
      cursor: pointer;

      &:hover {
        opacity: 0.9;
        transform: translateY(-1px);
      }
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .dark & {
      background: var(--dt-bg-hover-dark);
    }
  }
}

// ============================================================================
// 响应式断点
// ============================================================================

// 超小屏幕 (< 480px)
@media (max-width: 479px) {
  .chat-input-container {
    padding: 8px 12px 12px;
  }

  .input-toolbar {
    padding-bottom: 8px;
    gap: 8px;

    .toolbar-left {
      gap: 2px;

      .toolbar-group {
        padding: 3px;
        gap: 1px;
      }
    }

    .toolbar-btn {
      width: 28px;
      height: 28px;

      .el-icon,
      .material-icons-outlined {
        font-size: 16px;
      }
    }
  }

  .reply-preview-container,
  .edit-preview-container {
    padding: 8px 10px;
    margin-bottom: 8px;

    .reply-content-box,
    .edit-content-box {
      font-size: 12px;
      gap: 6px;
    }
  }

  .message-input {
    font-size: 14px;
    min-height: 60px;
  }

  .input-footer {
    gap: 12px;
    margin-top: 6px;

    .hint-text {
      font-size: 11px;
    }

    .send-btn {
      padding: 7px 16px;
      font-size: 13px;
    }
  }
}

// 小屏幕 (480px - 767px)
@media (min-width: 480px) and (max-width: 767px) {
  .input-toolbar {
    .toolbar-left {
      gap: 3px;

      .toolbar-group {
        padding: 3px 4px;
      }
    }

    .toolbar-btn {
      width: 30px;
      height: 30px;
    }
  }

  .reply-preview-container,
  .edit-preview-container {
    padding: 9px 11px;
  }

  .input-footer {
    gap: 14px;
  }
}

// 平板横屏 (768px - 1023px)
@media (min-width: 768px) and (max-width: 1023px) {
  .toolbar-btn {
    width: 34px;
    height: 34px;
  }
}
</style>
