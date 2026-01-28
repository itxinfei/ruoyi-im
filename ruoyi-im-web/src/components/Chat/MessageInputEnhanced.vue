<template>
  <div class="message-input-enhanced" :class="{ 'is-resizing': isResizing }" :style="{ minHeight: containerHeight + 'px' }">
    <!-- 调整高度手柄 -->
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
      <transition name="fade">
        <div v-if="isResizing" class="height-indicator">
          <span class="height-value">{{ Math.round(containerHeight) }}px</span>
          <span class="height-hint">拖拽调整高度</span>
        </div>
      </transition>
    </div>

    <!-- 快捷工具栏 - 优化版本 -->
    <div class="quick-toolbar">
      <!-- 常用功能组 -->
      <div class="toolbar-section">
        <el-tooltip content="表情" placement="top">
          <button class="tool-btn" :class="{ active: showEmojiPicker }" @click.stop="toggleEmojiPicker">
            <el-icon><ChatDotRound /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="文件传输" placement="top">
          <el-dropdown trigger="click" @command="handleFileCommand">
            <button class="tool-btn">
              <el-icon><FolderOpened /></el-icon>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu class="file-menu">
                <el-dropdown-item command="image">
                  <el-icon><Picture /></el-icon>
                  图片
                </el-dropdown-item>
                <el-dropdown-item command="file">
                  <el-icon><Document /></el-icon>
                  文件
                </el-dropdown-item>
                <el-dropdown-item command="video">
                  <el-icon><VideoCamera /></el-icon>
                  视频
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-tooltip>

        <el-tooltip content="位置" placement="top">
          <button class="tool-btn" @click="handleLocation">
            <el-icon><Location /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="截图 (Ctrl+Alt+A)" placement="top">
          <button class="tool-btn" @click="handleScreenshot">
            <el-icon><ScissorOne /></el-icon>
          </button>
        </el-tooltip>

        <!-- 更多功能 -->
        <el-popover
          placement="top"
          :width="380"
          trigger="click"
          popper-class="more-menu-popover"
        >
          <template #reference>
            <button class="tool-btn more-btn">
              <el-icon><MoreFilled /></el-icon>
            </button>
          </template>
          
          <!-- 更多功能面板 -->
          <div class="more-menu-content">
            <!-- 功能分类标签 -->
            <div class="menu-tabs">
              <div 
                v-for="tab in menuTabs"
                :key="tab.key"
                :class="['menu-tab', { active: activeMenuTab === tab.key }]"
                @click="activeMenuTab = tab.key"
              >
                <el-icon><component :is="tab.icon" /></el-icon>
                <span>{{ tab.label }}</span>
              </div>
            </div>

            <!-- 功能列表 -->
            <div class="menu-items">
              <!-- 工具类 -->
              <div v-show="activeMenuTab === 'tools'" class="menu-group">
                <div
                  v-for="item in toolItems"
                  :key="item.key"
                  class="menu-item"
                  @click="handleMenuAction(item)"
                >
                  <div class="item-icon" :style="{ backgroundColor: item.bgColor }">
                    <el-icon><component :is="item.icon" /></el-icon>
                  </div>
                  <div class="item-content">
                    <div class="item-title">{{ item.title }}</div>
                    <div class="item-desc">{{ item.desc }}</div>
                  </div>
                  <div class="item-status" v-if="item.status">
                    <el-tag :type="item.status.type" size="small">{{ item.status.text }}</el-tag>
                  </div>
                </div>
              </div>

              <!-- 协作类 -->
              <div v-show="activeMenuTab === 'collab'" class="menu-group">
                <div
                  v-for="item in collabItems"
                  :key="item.key"
                  class="menu-item"
                  @click="handleMenuAction(item)"
                >
                  <div class="item-icon" :style="{ backgroundColor: item.bgColor }">
                    <el-icon><component :is="item.icon" /></el-icon>
                  </div>
                  <div class="item-content">
                    <div class="item-title">{{ item.title }}</div>
                    <div class="item-desc">{{ item.desc }}</div>
                  </div>
                  <el-badge v-if="item.badge" :value="item.badge" :max="99" class="item-badge" />
                </div>
              </div>

              <!-- 快捷操作类 -->
              <div v-show="activeMenuTab === 'quick'" class="menu-group">
                <div
                  v-for="item in quickItems"
                  :key="item.key"
                  class="menu-item"
                  @click="handleMenuAction(item)"
                >
                  <div class="item-icon" :style="{ backgroundColor: item.bgColor }">
                    <el-icon><component :is="item.icon" /></el-icon>
                  </div>
                  <div class="item-content">
                    <div class="item-title">{{ item.title }}</div>
                    <div class="item-desc">{{ item.desc }}</div>
                  </div>
                  <div class="item-shortcut" v-if="item.shortcut">
                    <kbd>{{ item.shortcut }}</kbd>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-popover>
      </div>

      <!-- 右侧功能组 -->
      <div class="toolbar-section right-section">
        <el-tooltip v-if="session?.type === 'GROUP'" content="@成员" placement="top">
          <button class="tool-btn" @click="handleAtMember">
            <el-icon><At /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="语音消息" placement="top">
          <button 
            class="tool-btn" 
            :class="{ active: isVoiceMode }" 
            @click="toggleVoiceMode"
          >
            <el-icon><Microphone /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="AI 助手" placement="top">
          <button class="tool-btn ai-btn" @click="handleShowSmartReply">
            <el-icon><MagicStick /></el-icon>
          </button>
        </el-tooltip>
      </div>
    </div>

    <!-- 引用/编辑预览 -->
    <div v-if="replyingMessage || editingMessage" class="preview-container">
      <div v-if="replyingMessage" class="reply-preview">
        <el-icon class="preview-icon"><ChatLineSquare /></el-icon>
        <div class="preview-content">
          <span class="preview-user">{{ replyingMessage.senderName }}:</span>
          <span class="preview-text">{{ replyingMessage.content }}</span>
        </div>
        <el-icon class="preview-close" @click="clearReply"><Close /></el-icon>
      </div>

      <div v-if="editingMessage" class="edit-preview">
        <el-icon class="preview-icon"><Edit /></el-icon>
        <div class="preview-content">
          <span class="preview-label">正在编辑:</span>
          <span class="preview-text">{{ editingMessage.content }}</span>
        </div>
        <el-icon class="preview-close" @click="$emit('cancel-edit')"><Close /></el-icon>
      </div>
    </div>

    <!-- 输入区域 -->
    <div
      class="input-area"
      :class="{ 'is-drag-over': isDragOver, 'is-voice-mode': isVoiceMode }"
      @dragenter="handleDragEnter"
      @dragleave="handleDragLeave"
      @dragover="handleDragOver"
      @drop.prevent="handleDrop"
    >
      <!-- 语音录制模式 -->
      <VoiceRecorder
        v-if="isVoiceMode"
        @send="handleSendVoice"
        @cancel="isVoiceMode = false"
      />

      <!-- 文字输入模式 -->
      <div v-else class="text-input-wrapper">
        <textarea
          ref="textareaRef"
          v-model="messageContent"
          class="message-input"
          :placeholder="getPlaceholder()"
          @input="handleInput"
          @keydown="handleKeydown"
          @paste="handlePaste"
        ></textarea>

        <!-- 输入框右侧工具 -->
        <div class="input-tools">
          <span class="char-count" v-if="messageContent.length > 100">{{ messageContent.length }}/2000</span>
          <span class="hint-text">{{ getSendHint() }}</span>
        </div>
      </div>
    </div>

    <!-- 发送区域 -->
    <div class="send-area">
      <button
        class="send-btn"
        :class="{ active: canSend, 'is-sending': sending }"
        :disabled="!canSend || sending"
        @click="handleSend"
      >
        <el-icon v-if="!sending"><Promotion /></el-icon>
        <el-icon v-else class="is-loading"><Loading /></el-icon>
        <span>{{ sending ? '发送中' : '发送' }}</span>
      </button>
    </div>

    <!-- 子组件 -->
    <EmojiPicker v-if="showEmojiPicker" @select="selectEmoji" ref="emojiPickerRef" />
    <AtMemberPicker ref="atMemberPickerRef" :session-id="session?.id" @select="onAtSelect" />
    <CommandPalette
      :show="showCommandPalette"
      :position="commandPalettePosition"
      @close="showCommandPalette = false"
      @select="handleCommandSelect"
    />
    <ScreenshotPreview
      v-model="showScreenshotPreview"
      :image-data="screenshotData"
      @send="handleSendScreenshot"
      @close="handleScreenshotPreviewClose"
    />
    <AiSmartReply
      v-model:visible="showSmartReply"
      :trigger-message="lastReceivedMessage"
      :position="smartReplyPosition"
      @select="handleSelectSmartReply"
    />
    
    <!-- 隐藏的输入元素 -->
    <input
      type="file"
      ref="imageInputRef"
      accept="image/*"
      style="display: none"
      @change="handleImageFileChange"
    />
    <input
      type="file"
      ref="fileInputRef"
      style="display: none"
      @change="handleFileChange"
    />
    <input
      type="file"
      ref="videoInputRef"
      accept="video/mp4,video/webm,video/ogg,video/quicktime"
      style="display: none"
      @change="handleVideoFileChange"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useStore } from 'vuex'
import {
  Close,
  ChatDotRound,
  Picture,
  FolderOpened,
  Location,
  Microphone,
  MoreFilled,
  ArrowDown,
  Document,
  VideoCamera,
  ScissorOne,
  At,
  Edit,
  ChatLineSquare,
  Promotion,
  Loading,
  MagicStick,
  Timer,
  Notebook,
  Calendar,
  UserFilled,
  Monitor,
  Phone,
  Share,
  DataBoard,
  SwitchButton,
  Bell,
  Position
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import EmojiPicker from '@/components/EmojiPicker/index.vue'
import AtMemberPicker from './AtMemberPicker.vue'
import VoiceRecorder from './VoiceRecorder.vue'
import ScreenshotPreview from './ScreenshotPreview.vue'
import CommandPalette from './CommandPalette.vue'
import AiSmartReply from './AiSmartReply.vue'

const props = defineProps({
  session: Object,
  sending: Boolean,
  replyingMessage: Object,
  editingMessage: Object,
  lastReceivedMessage: Object
})

const emit = defineEmits([
  'send',
  'send-voice',
  'upload-image',
  'upload-file',
  'upload-video',
  'send-location',
  'cancel-reply',
  'cancel-edit',
  'edit-confirm',
  'input',
  'start-call',
  'start-video',
  'send-screenshot'
])

const store = useStore()

// 基础状态
const messageContent = ref('')
const showEmojiPicker = ref(false)
const isVoiceMode = ref(false)
const showScreenshotPreview = ref(false)
const screenshotData = ref(null)
const isCapturing = ref(false)
const showSmartReply = ref(false)
const smartReplyPosition = ref({ x: 0, y: 0 })
const showCommandPalette = ref(false)
const commandPalettePosition = ref({ x: 0, y: 0 })
const activeMenuTab = ref('tools')

// 拖拽状态
const isDragOver = ref(false)
let dragCounter = 0

// 高度调整
const containerHeight = ref(160)
const minHeight = 160
const maxHeight = 600
let isResizing = false
let startY = 0
let startHeight = 0

// Refs
const textareaRef = ref(null)
const emojiPickerRef = ref(null)
const atMemberPickerRef = ref(null)
const imageInputRef = ref(null)
const fileInputRef = ref(null)
const videoInputRef = ref(null)

// 更多菜单配置
const menuTabs = computed(() => [
  { key: 'tools', label: '工具', icon: 'Tools' },
  { key: 'collab', label: '协作', icon: 'UserFilled' },
  { key: 'quick', label: '快捷', icon: 'Timer' }
])

const toolItems = computed(() => [
  {
    key: 'schedule',
    title: '日程安排',
    desc: '创建会议或预约事件',
    icon: 'Calendar',
    bgColor: '#1890ff',
    status: { type: 'info', text: '即将上线' }
  },
  {
    key: 'vote',
    title: '发起投票',
    desc: '在群组中创建投票',
    icon: 'DataBoard',
    bgColor: '#52c41a'
  },
  {
    key: 'collection',
    title: '收藏夹',
    desc: '查看收藏的消息',
    icon: 'Star',
    bgColor: '#faad14'
  }
])

const collabItems = computed(() => [
  {
    key: 'whiteboard',
    title: '协作白板',
    desc: '多人实时协作画板',
    icon: 'Monitor',
    bgColor: '#722ed1',
    badge: 3
  },
  {
    key: 'screen-share',
    title: '屏幕共享',
    desc: '共享您的屏幕内容',
    icon: 'Share',
    bgColor: '#13c2c2'
  },
  {
    key: 'conference',
    title: '视频会议',
    desc: '发起多人视频会议',
    icon: 'VideoCamera',
    bgColor: '#f5222d',
    badge: 5
  }
])

const quickItems = computed(() => [
  {
    key: 'quick-reply',
    title: '快捷回复',
    desc: '使用预设回复模板',
    icon: 'ChatLineSquare',
    bgColor: '#fa541c',
    shortcut: '/r'
  },
  {
    key: 'voice-translate',
    title: '语音转文字',
    desc: '自动转换语音为文字',
    icon: 'Microphone',
    bgColor: '#a0d911'
  },
  {
    key: 'remind',
    title: '定时提醒',
    desc: '设置消息提醒',
    icon: 'Bell',
    bgColor: '#eb2f96'
  },
  {
    key: 'encrypt',
    title: '加密消息',
    desc: '发送端到端加密消息',
    icon: 'Lock',
    bgColor: '#2f54eb'
  }
])

// 计算属性
const canSend = computed(() => messageContent.value.trim().length > 0)

const getPlaceholder = () => {
  if (props.session?.type === 'GROUP') {
    return '群聊消息，@成员通知他们...'
  }
  return '输入消息...'
}

const getSendHint = () => {
  const shortcut = store.state.im.settings?.shortcuts?.send || 'enter'
  if (shortcut === 'ctrl-enter') {
    return 'Ctrl + Enter 发送'
  }
  return 'Enter 发送，Shift + Enter 换行'
}

// 文件处理
const handleFileCommand = (command) => {
  switch (command) {
    case 'image':
      imageInputRef.value?.click()
      break
    case 'file':
      fileInputRef.value?.click()
      break
    case 'video':
      videoInputRef.value?.click()
      break
  }
}

const handleImageFileChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    const formData = new FormData()
    formData.append('file', file)
    emit('upload-image', formData)
  }
  e.target.value = '' // 清除以允许重复选择
}

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    const formData = new FormData()
    formData.append('file', file)
    emit('upload-file', formData)
  }
  e.target.value = ''
}

const handleVideoFileChange = (e) => {
  const file = e.target.files[0]
  if (!file) return
  
  const validTypes = ['video/mp4', 'video/webm', 'video/ogg', 'video/quicktime']
  const maxSize = 100 * 1024 * 1024 // 100MB
  
  if (!validTypes.includes(file.type)) {
    ElMessage.error('不支持的视频格式')
    return
  }
  
  if (file.size > maxSize) {
    ElMessage.error('视频大小不能超过100MB')
    return
  }
  
  const url = URL.createObjectURL(file)
  emit('upload-video', { file, url })
  e.target.value = ''
}

// 菜单操作处理
const handleMenuAction = (item) => {
  switch (item.key) {
    case 'schedule':
      ElMessage.info('日程功能即将上线')
      break
    case 'vote':
      ElMessage.info('投票功能即将上线')
      break
    case 'collection':
      ElMessage.info('收藏夹功能即将上线')
      break
    case 'whiteboard':
      ElMessage.info('协作白板即将上线')
      break
    case 'screen-share':
      ElMessage.info('屏幕共享即将上线')
      break
    case 'conference':
      emit('start-video')
      break
    case 'quick-reply':
      ElMessage.info('快捷回复功能即将上线')
      break
    case 'voice-translate':
      ElMessage.info('语音转文字功能即将上线')
      break
    case 'remind':
      ElMessage.info('定时提醒功能即将上线')
      break
    case 'encrypt':
      ElMessage.info('加密消息功能即将上线')
      break
  }
}

// 其他功能（保持原有实现）
const handleLocation = () => {
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
        address: ''
      })
    },
    (error) => {
      ElMessage.error('获取位置失败，请检查定位权限')
    },
    { enableHighAccuracy: true }
  )
}

const handleScreenshot = async () => {
  ElMessage.info('截图功能即将上线')
}

const handleAtMember = () => {
  if (props.session?.type === 'GROUP') {
    atMemberPickerRef.value?.open(textareaRef.value?.selectionStart || 0)
  }
}

const toggleVoiceMode = () => {
  isVoiceMode.value = !isVoiceMode.value
  nextTick(() => {
    if (isVoiceMode.value) {
      textareaRef.value?.blur()
    } else {
      textareaRef.value?.focus()
    }
  })
}

const handleSendVoice = ({ file, duration }) => {
  emit('send-voice', { file, duration })
  isVoiceMode.value = false
}

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

const selectEmoji = (emoji) => {
  const pos = textareaRef.value?.selectionStart || messageContent.value.length
  messageContent.value = messageContent.value.slice(0, pos) + emoji + messageContent.value.slice(pos)
  showEmojiPicker.value = false
  nextTick(() => {
    textareaRef.value?.focus()
  })
}

const handleShowSmartReply = () => {
  const inputArea = document.querySelector('.message-input-enhanced')
  if (!inputArea) return
  
  const rect = inputArea.getBoundingClientRect()
  smartReplyPosition.value = {
    x: rect.right - 360,
    y: rect.top - 480
  }
  showSmartReply.value = true
}

const handleSelectSmartReply = (replyText) => {
  messageContent.value = replyText
  nextTick(() => {
    textareaRef.value?.focus()
  })
}

// 输入处理
const handleInput = () => {
  emit('input', messageContent.value)
  
  // 检查快捷命令
  const value = messageContent.value
  const cursorPos = textareaRef.value?.selectionStart || 0
  const lastChar = value.charAt(cursorPos - 1)
  
  if (lastChar === '/' && (cursorPos === 1 || value.charAt(cursorPos - 2) === ' ')) {
    showCommandPaletteAtCursor()
  }
}

const showCommandPaletteAtCursor = () => {
  const tx = textareaRef.value
  if (!tx) return
  
  const rect = tx.getBoundingClientRect()
  commandPalettePosition.value = {
    x: rect.left,
    y: rect.top - 400
  }
  showCommandPalette.value = true
}

const handleCommandSelect = (commandId) => {
  // 处理命令选择逻辑
  showCommandPalette.value = false
}

const handleKeydown = (e) => {
  if (e.key === 'Enter') {
    const sendShortcut = store.state.im.settings?.shortcuts?.send || 'enter'
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

const handleSend = () => {
  const content = messageContent.value.trim()
  if (!content) return
  
  if (props.editingMessage) {
    emit('edit-confirm', content)
  } else {
    emit('send', content)
  }
  
  messageContent.value = ''
}

// 拖拽处理
const handleDragEnter = (e) => {
  e.preventDefault()
  dragCounter++
  isDragOver.value = true
}

const handleDragLeave = () => {
  dragCounter--
  if (dragCounter <= 0) {
    isDragOver.value = false
    dragCounter = 0
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
    
    if (file.type.startsWith('image/')) {
      emit('upload-image', formData)
    } else {
      emit('upload-file', formData)
    }
  }
}

// 高度调整
const startResize = (e) => {
  isResizing = true
  startY = e.clientY
  startHeight = containerHeight.value
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
  document.body.style.cursor = 'ns-resize'
}

const handleResize = (e) => {
  if (!isResizing) return
  const delta = startY - e.clientY
  const newHeight = startHeight + delta
  if (newHeight >= minHeight && newHeight <= maxHeight) {
    containerHeight.value = newHeight
  }
}

const stopResize = () => {
  isResizing = false
  document.body.style.cursor = ''
  localStorage.setItem('im_input_height', containerHeight.value)
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
}

const resetHeight = () => {
  containerHeight.value = 160
}

const clearReply = () => {
  emit('cancel-reply')
}

// 其他方法
const handleSendScreenshot = async (blob) => {
  const formData = new FormData()
  formData.append('file', blob, 'screenshot.png')
  emit('send-screenshot', formData)
  showScreenshotPreview.value = false
  screenshotData.value = null
}

const handleScreenshotPreviewClose = () => {
  showScreenshotPreview.value = false
  screenshotData.value = null
}

const onAtSelect = (m) => {
  const nickname = m.nickname || m.userName
  const pos = textareaRef.value?.selectionStart || messageContent.value.length
  messageContent.value = messageContent.value.slice(0, pos) + `@${nickname} ` + messageContent.value.slice(pos)
  nextTick(() => {
    textareaRef.value?.focus()
  })
}

// 监听
watch(() => props.replyingMessage, () => {
  nextTick(() => textareaRef.value?.focus())
})

watch(() => props.editingMessage, () => {
  nextTick(() => textareaRef.value?.focus())
})

onMounted(() => {
  const saved = localStorage.getItem('im_input_height')
  if (saved) {
    containerHeight.value = parseInt(saved)
  }
})

onUnmounted(() => {
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
})
</script>

<style lang="scss" scoped>
.message-input-enhanced {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 12px;
  transition: all 0.3s ease;
  
  &:hover {
    border-color: var(--el-border-color);
  }
  
  .resize-handle {
    position: absolute;
    top: -1px;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 6px;
    cursor: ns-resize;
    z-index: 10;
    
    .resize-indicator {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .resize-dots {
        display: flex;
        gap: 4px;
        
        &::before,
        &::after {
          content: '';
          width: 4px;
          height: 4px;
          background: var(--el-border-color);
          border-radius: 50%;
          transition: all 0.3s ease;
        }
      }
    }
    
    &:hover .resize-dots::before,
    &:hover .resize-dots::after {
      background: var(--el-color-primary);
    }
    
    &.is-active .resize-dots::before,
    &.is-active .resize-dots::after {
      width: 6px;
      height: 6px;
      background: var(--el-color-primary);
    }
  }
  
  .height-indicator {
    position: absolute;
    top: -40px;
    left: 50%;
    transform: translateX(-50%);
    background: var(--el-bg-color);
    border: 1px solid var(--el-border-color);
    border-radius: 6px;
    padding: 8px 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    z-index: 100;
    
    .height-value {
      font-weight: 600;
      color: var(--el-color-primary);
    }
    
    .height-hint {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}

.quick-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 0 4px;
  
  .toolbar-section {
    display: flex;
    align-items: center;
    gap: 4px;
    
    &.right-section {
      margin-left: auto;
    }
  }
  
  .tool-btn {
    width: 36px;
    height: 36px;
    border: none;
    background: transparent;
    border-radius: 6px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--el-text-color-regular);
    transition: all 0.2s ease;
    position: relative;
    
    &:hover {
      background: var(--el-fill-color-light);
      color: var(--el-color-primary);
    }
    
    &.active {
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
    }
    
    .dropdown-arrow {
      font-size: 12px;
      margin-left: 2px;
    }
    
    &.more-btn {
      &:hover {
        transform: rotate(180deg);
      }
    }
    
    &.ai-btn {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      
      &:hover {
        background: linear-gradient(135deg, #5a72d4 0%, #6a4190 100%);
        transform: scale(1.05);
      }
    }
  }
}

.preview-container {
  margin-bottom: 12px;
  
  .reply-preview,
  .edit-preview {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: var(--el-fill-color-light);
    border-left: 3px solid var(--el-color-primary);
    border-radius: 6px;
    position: relative;
    
    .preview-icon {
      color: var(--el-color-primary);
      font-size: 16px;
    }
    
    .preview-content {
      flex: 1;
      min-width: 0;
      
      .preview-user {
        font-weight: 600;
        color: var(--el-color-primary);
        margin-right: 8px;
      }
      
      .preview-label {
        font-weight: 600;
        color: var(--el-color-warning);
        margin-right: 8px;
      }
      
      .preview-text {
        color: var(--el-text-color-regular);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    
    .preview-close {
      cursor: pointer;
      color: var(--el-text-color-placeholder);
      font-size: 16px;
      
      &:hover {
        color: var(--el-text-color-secondary);
      }
    }
  }
}

.input-area {
  position: relative;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  overflow: hidden;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: var(--el-border-color-light);
  }
  
  &.is-drag-over {
    border-color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }
  
  &.is-voice-mode {
    padding: 20px;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 120px;
  }
}

.text-input-wrapper {
  position: relative;
  
  .message-input {
    width: 100%;
    min-height: 80px;
    max-height: 300px;
    padding: 12px;
    border: none;
    outline: none;
    resize: none;
    font-size: 14px;
    line-height: 1.5;
    color: var(--el-text-color-primary);
    background: transparent;
    
    &::placeholder {
      color: var(--el-text-color-placeholder);
    }
  }
  
  .input-tools {
    position: absolute;
    bottom: 8px;
    right: 12px;
    display: flex;
    align-items: center;
    gap: 12px;
    
    .char-count {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
    
    .hint-text {
      font-size: 12px;
      color: var(--el-text-color-placeholder);
    }
  }
}

.send-area {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  
  .send-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border: none;
    border-radius: 6px;
    background: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
    cursor: pointer;
    transition: all 0.2s ease;
    
    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    
    &.active {
      background: var(--el-color-primary);
      color: white;
      
      &:hover {
        background: var(--el-color-primary-light-3);
      }
    }
    
    &.is-sending {
      .el-icon {
        animation: rotate 1s linear infinite;
      }
    }
  }
}

// 更多菜单样式
:deep(.more-menu-popover) {
  padding: 0;
  
  .more-menu-content {
    width: 380px;
    
    .menu-tabs {
      display: flex;
      border-bottom: 1px solid var(--el-border-color-lighter);
      
      .menu-tab {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
        padding: 12px;
        cursor: pointer;
        transition: all 0.2s ease;
        
        &:hover {
          background: var(--el-fill-color-light);
        }
        
        &.active {
          color: var(--el-color-primary);
          border-bottom: 2px solid var(--el-color-primary);
        }
        
        .el-icon {
          font-size: 18px;
        }
        
        span {
          font-size: 12px;
        }
      }
    }
    
    .menu-group {
      padding: 8px 0;
      
      .menu-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 16px;
        cursor: pointer;
        transition: all 0.2s ease;
        position: relative;
        
        &:hover {
          background: var(--el-fill-color-light);
          
          .item-icon {
            transform: scale(1.1);
          }
        }
        
        .item-icon {
          width: 40px;
          height: 40px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          transition: transform 0.2s ease;
          
          .el-icon {
            font-size: 20px;
          }
        }
        
        .item-content {
          flex: 1;
          min-width: 0;
          
          .item-title {
            font-weight: 500;
            margin-bottom: 4px;
            color: var(--el-text-color-primary);
          }
          
          .item-desc {
            font-size: 12px;
            color: var(--el-text-color-secondary);
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
        
        .item-status {
          flex-shrink: 0;
        }
        
        .item-badge {
          flex-shrink: 0;
        }
        
        .item-shortcut {
          kbd {
            padding: 2px 6px;
            background: var(--el-fill-color-light);
            border: 1px solid var(--el-border-color-light);
            border-radius: 4px;
            font-size: 11px;
            color: var(--el-text-color-secondary);
          }
        }
      }
    }
  }
}

// 动画
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 响应式
@media (max-width: 768px) {
  .message-input-enhanced {
    padding: 8px;
    
    .quick-toolbar {
      .tool-btn {
        width: 32px;
        height: 32px;
      }
    }
    
    .text-input-wrapper {
      .message-input {
        min-height: 60px;
        padding: 8px;
      }
    }
  }
}

// 深色模式适配
.dark {
  .message-input-enhanced {
    background: var(--el-bg-color);
    border-color: var(--el-border-color);
    
    .preview-container .reply-preview,
    .preview-container .edit-preview {
      background: var(--el-fill-color-dark);
    }
  }
}
</style>