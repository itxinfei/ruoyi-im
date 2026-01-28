<template>
  <div
    class="message-bubble-enhanced"
    :class="[
      `message-${message.type.toLowerCase()}`,
      { 'is-self': isSelf, 'is-ghost': isGhost, 'is-recalled': isRecalled }
    ]"
    @contextmenu.prevent="handleContextMenu"
  >
    <!-- 时间分割线 -->
    <div v-if="showTimeDivider" class="time-divider">
      <span class="time-text">{{ formatTime(message.timestamp) }}</span>
    </div>

    <!-- 消息主体 -->
    <div class="message-wrapper" :id="`message-${message.id}`">
      <!-- 引用回复 -->
      <div v-if="message.replyToMessageId" class="reply-quote">
        <div class="reply-arrow"></div>
        <div class="reply-content">
          <el-icon class="reply-icon"><ChatLineSquare /></el-icon>
          <span class="reply-name">{{ replyMessage?.senderName }}</span>
          <span class="reply-text">{{ replyMessage?.content }}</span>
        </div>
      </div>

      <!-- 用户头像 -->
      <div v-if="!isSelf && !isSystem" class="avatar-wrapper">
        <DingtalkAvatar
          :src="message.senderAvatar"
          :name="message.senderName"
          :size="40"
          shape="circle"
          :user-id="message.senderId"
        />
        <div v-if="isOnline" class="online-indicator"></div>
      </div>

      <!-- 消息内容容器 -->
      <div class="message-content-wrapper">
        <!-- 消息状态图标 -->
        <div v-if="messageStatus" class="message-status">
          <el-icon v-if="messageStatus === 'sending'" class="is-loading"><Loading /></el-icon>
          <el-icon v-else-if="messageStatus === 'sent'" color="#909399"><Check /></el-icon>
          <el-icon v-else-if="messageStatus === 'delivered'" color="#909399"><DoubleCheck /></el-icon>
          <el-icon v-else-if="messageStatus === 'read'" color="#4CAF50"><DoubleCheck /></el-icon>
          <el-icon v-else-if="messageStatus === 'failed'" color="#F56C6C"><WarningFilled /></el-icon>
        </div>

        <!-- 发送者名称（群聊） -->
        <div v-if="!isSelf && sessionType === 'GROUP' && !isSystem" class="sender-name">
          {{ message.senderName }}
        </div>

        <!-- 文本消息 -->
        <div v-if="message.type === 'TEXT'" class="text-bubble" v-html="formatContent(message.content)"></div>

        <!-- 图片消息 -->
        <div v-else-if="message.type === 'IMAGE'" class="image-message">
          <div class="image-container" @click="previewImage">
            <el-image
              :src="addTokenToUrl(message.imageUrl)"
              :preview-src-list="[addTokenToUrl(message.imageUrl)]"
              fit="cover"
              lazy
              :initial-index="0"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
            
            <!-- 图片尺寸标注 -->
            <div v-if="message.imageSize" class="image-size">
              {{ formatFileSize(message.imageSize) }}
            </div>
          </div>
        </div>

        <!-- 文件消息 -->
        <div v-else-if="message.type === 'FILE'" class="file-message">
          <div class="file-card" @click="downloadFile">
            <div class="file-icon" :class="getFileIconClass(message.fileName)">
              <el-icon><component :is="getFileIcon(message.fileName)" /></el-icon>
            </div>
            <div class="file-info">
              <div class="file-name" :title="message.fileName">{{ message.fileName }}</div>
              <div class="file-meta">
                <span>{{ formatFileSize(message.fileSize) }}</span>
                <span v-if="message.fileType">{{ message.fileType }}</span>
              </div>
            </div>
            <el-icon class="download-icon"><Download /></el-icon>
          </div>
        </div>

        <!-- 语音消息 -->
        <div v-else-if="message.type === 'VOICE'" class="voice-message">
          <button class="voice-player" @click="togglePlayVoice">
            <el-icon class="play-icon" :class="{ 'is-playing': isPlaying }">
              <VideoPlay v-if="!isPlaying" />
              <VideoPause v-else />
            </el-icon>
            
            <div class="voice-waveform">
              <div
                v-for="i in 20"
                :key="i"
                class="wave-bar"
                :class="{ active: isPlaying && (waveProgress * 20) > i }"
              ></div>
            </div>
            
            <span class="voice-duration">{{ formatDuration(message.duration) }}</span>
          </button>
        </div>

        <!-- 视频消息 -->
        <div v-else-if="message.type === 'VIDEO'" class="video-message">
          <div class="video-container">
            <video
              ref="videoRef"
              :src="addTokenToUrl(message.videoUrl)"
              controls
              :poster="message.videoThumbnail"
              preload="metadata"
            >
              您的浏览器不支持视频播放
            </video>
            
            <!-- 视频信息 -->
            <div class="video-info">
              <div class="video-duration">{{ formatDuration(message.duration) }}</div>
              <div v-if="message.fileSize" class="video-size">{{ formatFileSize(message.fileSize) }}</div>
            </div>
          </div>
        </div>

        <!-- 位置消息 -->
        <div v-else-if="message.type === 'LOCATION'" class="location-message">
          <div class="location-card" @click="viewLocation">
            <div class="location-map">
              <img 
                :src="getLocationMapUrl(message.latitude, message.longitude)" 
                :alt="message.address || '位置'"
              />
              <div class="location-marker">
                <el-icon><LocationFilled /></el-icon>
              </div>
            </div>
            <div class="location-info">
              <div class="location-name">{{ message.address || '位置' }}</div>
              <div class="location-coords">
                {{ formatCoordinate(message.latitude, message.longitude) }}
              </div>
            </div>
          </div>
        </div>

        <!-- 撤回消息 -->
        <div v-else-if="isRecalled" class="recalled-message">
          <el-icon><RefreshLeft /></el-icon>
          <span>{{ isSelf ? '你撤回了一条消息' : `${message.senderName}撤回了一条消息` }}</span>
        </div>

        <!-- 系统消息 -->
        <div v-else-if="isSystem" class="system-message">
          <el-icon><InfoFilled /></el-icon>
          <span>{{ message.content }}</span>
        </div>

        <!-- 消息时间 -->
        <div v-if="!isSystem && !isGhost" class="message-time">
          {{ formatMessageTime(message.timestamp) }}
          <span v-if="message.isEdited" class="edited-mark">已编辑</span>
        </div>

        <!-- 消息操作菜单 -->
        <div v-if="!isSystem && !isGhost" class="message-actions" :class="{ 'is-visible': showActions }">
          <el-dropdown trigger="click" @command="handleCommand">
            <button class="action-btn" @click.stop>
              <el-icon><MoreFilled /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu class="message-menu">
                <!-- 引用回复 -->
                <el-dropdown-item command="reply" v-if="!isRecalled">
                  <el-icon><ChatLineSquare /></el-icon>
                  引用回复
                </el-dropdown-item>
                
                <!-- 复制 -->
                <el-dropdown-item command="copy" v-if="message.type === 'TEXT'">
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-dropdown-item>
                
                <!-- 转发 -->
                <el-dropdown-item command="forward" v-if="!isRecalled">
                  <el-icon><Promotion /></el-icon>
                  转发
                </el-dropdown-item>
                
                <!-- 多选 -->
                <el-dropdown-item command="multiselect" v-if="!isRecalled">
                  <el-icon><Select /></el-icon>
                  多选
                </el-dropdown-item>
                
                <!-- 编辑 -->
                <el-dropdown-item command="edit" v-if="isSelf && !isRecalled && message.type === 'TEXT'">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-dropdown-item>
                
                <!-- 撤回 -->
                <el-dropdown-item command="recall" v-if="isSelf && !isRecalled && canRecall">
                  <el-icon><RefreshLeft /></el-icon>
                  撤回
                </el-dropdown-item>
                
                <!-- 删除 -->
                <el-dropdown-item command="delete" v-if="isSelf && !isRecalled">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-dropdown-item>
                
                <!-- 分割线 -->
                <el-dropdown-item divided command="info" v-if="message.id">
                  <el-icon><InfoFilled /></el-icon>
                  消息详情
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 消息分割线（多选时） -->
      <div v-if="isMultiSelectMode" class="multiselect-checkbox" @click.stop="toggleSelect">
        <el-checkbox :model-value="isSelected" />
      </div>
    </div>

    <!-- 已读回执（群聊） -->
    <div v-if="showReadReceipt && readReceiptUsers.length > 0" class="read-receipt">
      <el-avatar-group :max="3" :size="20">
        <el-avatar
          v-for="user in readReceiptUsers"
          :key="user.id"
          :src="user.avatar"
          :size="20"
        >
          {{ user.nickname?.slice(0, 1) }}
        </el-avatar>
      </el-avatar-group>
      <span class="read-receipt-text">{{ getReadReceiptText() }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import {
  ChatLineSquare,
  Picture,
  Document,
  Download,
  VideoPlay,
  VideoPause,
  LocationFilled,
  InfoFilled,
  RefreshLeft,
  CopyDocument,
  Promotion,
  Select,
  Edit,
  Delete,
  MoreFilled,
  Check,
  DoubleCheck,
  Loading,
  WarningFilled,
  Folder,
  Files,
  Zip,
  Excel,
  PPT,
  PDF
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { formatFileSize } from '@/utils/format'

const props = defineProps({
  message: {
    type: Object,
    required: true
  },
  sessionType: {
    type: String,
    default: 'PRIVATE'
  },
  currentUser: {
    type: Object,
    default: () => ({})
  },
  replyMessage: {
    type: Object,
    default: null
  },
  isMultiSelectMode: {
    type: Boolean,
    default: false
  },
  isSelected: {
    type: Boolean,
    default: false
  },
  readReceiptUsers: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits([
  'reply',
  'forward',
  'multiselect',
  'edit',
  'recall',
  'delete',
  'copy',
  'select',
  'info'
])

const store = useStore()

// Refs
const videoRef = ref(null)

// 计算属性
const isSelf = computed(() => props.message.senderId === props.currentUser?.id)
const isGhost = computed(() => props.message.isGhost || false)
const isSystem = computed(() => props.message.type === 'SYSTEM' || props.message.type === 'TIP')
const isRecalled = computed(() => props.message.isRevoked || false)
const isOnline = computed(() => props.message.senderOnline)

const messageStatus = computed(() => {
  if (isSystem.value || isGhost.value) return null
  if (props.message.isSending) return 'sending'
  if (props.message.sendFailed) return 'failed'
  if (props.message.isDelivered) return 'delivered'
  if (props.message.isRead) return 'read'
  return 'sent'
})

const showTimeDivider = computed(() => {
  // 根据上一条消息的时间判断是否显示时间分割线
  // 这里简化处理，实际应该根据上一条消息的时间差
  return false
})

const canRecall = computed(() => {
  if (!isSelf.value) return false
  const now = Date.now()
  const messageTime = new Date(props.message.timestamp).getTime()
  const timeDiff = now - messageTime
  // 2分钟内可以撤回
  return timeDiff < 2 * 60 * 1000
})

const showActions = ref(false)
const showReadReceipt = computed(() => {
  return props.sessionType === 'GROUP' && isSelf.value && props.readReceiptUsers.length > 0
})

// 方法
const formatTime = (timestamp) => {
  return dayjs(timestamp).format('MM月DD日 HH:mm')
}

const formatMessageTime = (timestamp) => {
  const now = dayjs()
  const messageTime = dayjs(timestamp)
  
  if (messageTime.isSame(now, 'day')) {
    return messageTime.format('HH:mm')
  } else if (messageTime.isSame(now, 'year')) {
    return messageTime.format('MM月DD日 HH:mm')
  } else {
    return messageTime.format('YYYY年MM月DD日 HH:mm')
  }
}

const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return mins > 0 ? `${mins}:${secs.toString().padStart(2, '0')}` : `0:${secs.toString().padStart(2, '0')}`
}

const formatCoordinate = (lat, lng) => {
  return `${lat.toFixed(6)}, ${lng.toFixed(6)}`
}

const formatContent = (content) => {
  // 处理表情、@提及、链接等
  // 这里简化处理
  return content
    // 处理表情
    .replace(/\[(\w+)\]/g, '<img class="emoji" src="/emoji/$1.png" alt="$1">')
    // 处理@提及
    .replace(/@(\S+)/g, '<span class="at-mention">@$1</span>')
    // 处理链接
    .replace(/(https?:\/\/[^\s]+)/g, '<a href="$1" target="_blank" class="message-link">$1</a>')
}

const addTokenToUrl = (url) => {
  if (!url) return ''
  // 添加token用于访问私有资源
  const token = store.getters['user/token']
  return url.includes('?') ? `${url}&token=${token}` : `${url}?token=${token}`
}

const getFileIcon = (fileName) => {
  const ext = fileName?.split('.').pop()?.toLowerCase()
  
  // 图片
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)) {
    return Picture
  }
  
  // 文档类型
  const docTypes = ['doc', 'docx']
  const excelTypes = ['xls', 'xlsx']
  const pptTypes = ['ppt', 'pptx']
  const pdfTypes = ['pdf']
  
  if (docTypes.includes(ext)) return Document
  if (excelTypes.includes(ext)) return Excel
  if (pptTypes.includes(ext)) return PPT
  if (pdfTypes.includes(ext)) return PDF
  
  // 压缩包
  if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) {
    return Zip
  }
  
  // 默认文件图标
  return Files
}

const getFileIconClass = (fileName) => {
  const ext = fileName?.split('.').pop()?.toLowerCase()
  
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)) {
    return 'is-image'
  }
  if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'pdf'].includes(ext)) {
    return 'is-document'
  }
  if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) {
    return 'is-archive'
  }
  
  return 'is-default'
}

const getLocationMapUrl = (lat, lng) => {
  // 使用地图API生成静态地图
  return `https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/pin-l-marker+L${lng},${lat},14/300x200@2x?access_token=YOUR_MAPBOX_TOKEN`
}

const getReadReceiptText = () => {
  const users = props.readReceiptUsers
  if (users.length === 0) return ''
  if (users.length === 1) return `${users[0].nickname}已读`
  if (users.length === 2) return `${users[0].nickname}、${users[1].nickname}已读`
  return `${users[0].nickname}等${users.length}人已读`
}

// 事件处理
const handleCommand = (command) => {
  switch (command) {
    case 'reply':
      emit('reply', props.message)
      break
    case 'copy':
      copyToClipboard(props.message.content)
      break
    case 'forward':
      emit('forward', props.message)
      break
    case 'multiselect':
      emit('multiselect', props.message)
      break
    case 'edit':
      emit('edit', props.message)
      break
    case 'recall':
      emit('recall', props.message)
      break
    case 'delete':
      emit('delete', props.message)
      break
    case 'info':
      emit('info', props.message)
      break
  }
}

const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('已复制到剪贴板')
  }
}

const previewImage = () => {
  // 图片预览功能由 el-image 组件处理
}

const downloadFile = () => {
  const link = document.createElement('a')
  link.href = addTokenToUrl(props.message.fileUrl)
  link.download = props.message.fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const viewLocation = () => {
  // 打开地图应用查看位置
  const url = `https://www.google.com/maps/search/?api=1&query=${props.message.latitude},${props.message.longitude}`
  window.open(url, '_blank')
}

const toggleSelect = () => {
  emit('select', props.message)
}

// 语音播放相关
const isPlaying = ref(false)
const waveProgress = ref(0)

const togglePlayVoice = () => {
  isPlaying.value = !isPlaying.value
  
  if (isPlaying.value) {
    // 开始播放逻辑
    playVoice()
  } else {
    // 停止播放逻辑
    stopVoice()
  }
}

const playVoice = () => {
  const audio = new Audio(addTokenToUrl(props.message.voiceUrl))
  
  audio.addEventListener('timeupdate', () => {
    waveProgress.value = audio.currentTime / audio.duration
  })
  
  audio.addEventListener('ended', () => {
    isPlaying.value = false
    waveProgress.value = 0
  })
  
  audio.play()
}

const stopVoice = () => {
  // 停止音频播放
  isPlaying.value = false
  waveProgress.value = 0
}

// 鼠标悬停显示操作按钮
let hoverTimer = null

const handleMouseEnter = () => {
  clearTimeout(hoverTimer)
  showActions.value = true
}

const handleMouseLeave = () => {
  hoverTimer = setTimeout(() => {
    showActions.value = false
  }, 300)
}

const handleContextMenu = (e) => {
  e.preventDefault()
  showActions.value = true
}

// 生命周期
onMounted(() => {
  const messageEl = document.querySelector(`#message-${props.message.id}`)
  if (messageEl) {
    messageEl.addEventListener('mouseenter', handleMouseEnter)
    messageEl.addEventListener('mouseleave', handleMouseLeave)
  }
})

onUnmounted(() => {
  const messageEl = document.querySelector(`#message-${props.message.id}`)
  if (messageEl) {
    messageEl.removeEventListener('mouseenter', handleMouseEnter)
    messageEl.removeEventListener('mouseleave', handleMouseLeave)
  }
  clearTimeout(hoverTimer)
})
</script>

<style lang="scss" scoped>
.message-bubble-enhanced {
  margin-bottom: 16px;
  animation: fadeInUp 0.3s ease;
  
  &:hover {
    .message-actions {
      opacity: 1;
    }
  }
  
  .time-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 24px 0 12px;
    
    &::before,
    &::after {
      content: '';
      flex: 1;
      height: 1px;
      background: var(--el-border-color-lighter);
    }
    
    .time-text {
      padding: 4px 12px;
      background: var(--el-fill-color-light);
      border-radius: 12px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
  
  .message-wrapper {
    display: flex;
    position: relative;
    
    &.is-self {
      flex-direction: row-reverse;
      
      .message-content-wrapper {
        align-items: flex-end;
        
        .message-status {
          order: -1;
          margin-right: 8px;
        }
        
        .message-time {
          margin-right: 8px;
        }
        
        .message-actions {
          left: auto;
          right: 100%;
          margin-right: 8px;
        }
      }
      
      &.is-multi-select {
        .multiselect-checkbox {
          right: 100%;
          margin-right: 8px;
        }
      }
    }
    
    .avatar-wrapper {
      margin-right: 12px;
      flex-shrink: 0;
      position: relative;
      
      .online-indicator {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 12px;
        height: 12px;
        background: #52c41a;
        border: 2px solid var(--el-bg-color);
        border-radius: 50%;
      }
    }
  }
  
  .message-content-wrapper {
    max-width: 60%;
    min-width: 0;
    position: relative;
    
    .message-status {
      font-size: 16px;
      color: var(--el-text-color-placeholder);
      
      &.is-loading {
        color: var(--el-color-primary);
        animation: spin 1s linear infinite;
      }
    }
    
    .sender-name {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-bottom: 4px;
      max-width: 200px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .message-time {
      font-size: 12px;
      color: var(--el-text-color-placeholder);
      margin-top: 4px;
      display: flex;
      align-items: center;
      gap: 4px;
      
      .edited-mark {
        color: var(--el-color-warning);
        font-size: 11px;
      }
    }
    
    .message-actions {
      position: absolute;
      top: 0;
      opacity: 0;
      transition: opacity 0.2s ease;
      
      .action-btn {
        width: 32px;
        height: 32px;
        border: none;
        background: var(--el-bg-color);
        border: 1px solid var(--el-border-color-light);
        border-radius: 6px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--el-text-color-regular);
        
        &:hover {
          background: var(--el-fill-color-light);
          color: var(--el-color-primary);
        }
      }
    }
    
    .multiselect-checkbox {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 8px;
      
      &:hover {
        background: var(--el-fill-color-light);
        border-radius: 6px;
      }
    }
  }
  
  // 回复引用
  .reply-quote {
    display: flex;
    margin-bottom: 8px;
    position: relative;
    
    .reply-arrow {
      position: absolute;
      left: -6px;
      top: 12px;
      width: 0;
      height: 0;
      border-top: 6px solid transparent;
      border-bottom: 6px solid transparent;
      border-left: 6px solid var(--el-color-primary-light-8);
    }
    
    .reply-content {
      flex: 1;
      padding: 8px 12px;
      background: var(--el-color-primary-light-9);
      border-left: 3px solid var(--el-color-primary);
      border-radius: 6px;
      display: flex;
      align-items: center;
      gap: 8px;
      
      .reply-icon {
        font-size: 16px;
        color: var(--el-color-primary);
      }
      
      .reply-name {
        font-weight: 500;
        color: var(--el-color-primary);
        font-size: 13px;
      }
      
      .reply-text {
        flex: 1;
        color: var(--el-text-color-regular);
        font-size: 13px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
  
  // 文本消息气泡
  .text-bubble {
    padding: 10px 14px;
    background: var(--el-fill-color-light);
    border-radius: 12px;
    color: var(--el-text-color-primary);
    word-wrap: break-word;
    line-height: 1.5;
    position: relative;
    
    :deep(.emoji) {
      width: 20px;
      height: 20px;
      vertical-align: middle;
    }
    
    :deep(.at-mention) {
      color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
      padding: 2px 4px;
      border-radius: 4px;
    }
    
    :deep(.message-link) {
      color: var(--el-color-primary);
      text-decoration: none;
      
      &:hover {
        text-decoration: underline;
      }
    }
    
    &::before {
      content: '';
      position: absolute;
      bottom: 0;
      left: -6px;
      width: 8px;
      height: 12px;
      background: var(--el-fill-color-light);
      clip-path: polygon(100% 50%, 0 0, 0 100%);
    }
  }
  
  .is-self {
    .text-bubble {
      background: var(--el-color-primary);
      color: white;
      
      &::before {
        background: var(--el-color-primary);
        left: auto;
        right: -6px;
        transform: rotateY(180deg);
      }
    }
  }
  
  // 图片消息
  .image-message {
    .image-container {
      position: relative;
      cursor: pointer;
      border-radius: 12px;
      overflow: hidden;
      max-width: 300px;
      
      .el-image {
        width: 100%;
        display: block;
        
        :deep(.el-image__inner) {
          border-radius: 12px;
        }
      }
      
      .image-size {
        position: absolute;
        bottom: 8px;
        right: 8px;
        background: rgba(0, 0, 0, 0.6);
        color: white;
        padding: 4px 8px;
        border-radius: 4px;
        font-size: 12px;
      }
      
      .image-error {
        width: 100%;
        height: 150px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background: var(--el-fill-color-light);
        color: var(--el-text-color-placeholder);
        
        .el-icon {
          font-size: 32px;
          margin-bottom: 8px;
        }
      }
    }
  }
  
  // 文件消息
  .file-message {
    .file-card {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      background: var(--el-fill-color-light);
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s ease;
      max-width: 280px;
      
      &:hover {
        background: var(--el-fill-color);
        
        .download-icon {
          opacity: 1;
        }
      }
      
      .file-icon {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        flex-shrink: 0;
        
        &.is-image {
          background: linear-gradient(135deg, #667eea, #764ba2);
        }
        
        &.is-document {
          background: linear-gradient(135deg, #3b82f6, #1e40af);
        }
        
        &.is-archive {
          background: linear-gradient(135deg, #f97316, #ea580c);
        }
        
        &.is-default {
          background: linear-gradient(135deg, #6b7280, #374151);
        }
        
        .el-icon {
          font-size: 20px;
        }
      }
      
      .file-info {
        flex: 1;
        min-width: 0;
        
        .file-name {
          font-size: 14px;
          color: var(--el-text-color-primary);
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .file-meta {
          font-size: 12px;
          color: var(--el-text-color-secondary);
          display: flex;
          gap: 8px;
        }
      }
      
      .download-icon {
        color: var(--el-color-primary);
        opacity: 0;
        transition: opacity 0.2s ease;
      }
    }
  }
  
  // 语音消息
  .voice-message {
    .voice-player {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      background: var(--el-fill-color-light);
      border-radius: 12px;
      cursor: pointer;
      min-width: 160px;
      transition: all 0.2s ease;
      
      &:hover {
        background: var(--el-fill-color);
      }
      
      .play-icon {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        background: var(--el-color-primary);
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
        
        &.is-playing {
          background: var(--el-color-success);
        }
      }
      
      .voice-waveform {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 2px;
        
        .wave-bar {
          width: 2px;
          height: 16px;
          background: var(--el-border-color-light);
          border-radius: 1px;
          transition: all 0.3s ease;
          
          &.active {
            background: var(--el-color-primary);
            height: 20px;
          }
        }
      }
      
      .voice-duration {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }
  
  // 视频消息
  .video-message {
    .video-container {
      position: relative;
      border-radius: 12px;
      overflow: hidden;
      max-width: 300px;
      
      video {
        width: 100%;
        max-height: 200px;
      }
      
      .video-info {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
        padding: 8px 12px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .video-duration,
        .video-size {
          color: white;
          font-size: 12px;
          background: rgba(0, 0, 0, 0.5);
          padding: 2px 6px;
          border-radius: 4px;
        }
      }
    }
  }
  
  // 位置消息
  .location-message {
    .location-card {
      max-width: 300px;
      border-radius: 12px;
      overflow: hidden;
      cursor: pointer;
      background: var(--el-bg-color);
      border: 1px solid var(--el-border-color-light);
      
      .location-map {
        position: relative;
        height: 150px;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        
        .location-marker {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 32px;
          height: 32px;
          background: var(--el-color-danger);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
        }
      }
      
      .location-info {
        padding: 12px;
        
        .location-name {
          font-size: 14px;
          color: var(--el-text-color-primary);
          margin-bottom: 4px;
          font-weight: 500;
        }
        
        .location-coords {
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
    }
  }
  
  // 撤回消息
  .recalled-message {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: var(--el-fill-color-light);
    border-radius: 8px;
    color: var(--el-text-color-secondary);
    font-size: 13px;
    
    .el-icon {
      color: var(--el-text-color-placeholder);
    }
  }
  
  // 系统消息
  .system-message {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 8px 16px;
    background: var(--el-fill-color-lighter);
    border-radius: 12px;
    color: var(--el-text-color-secondary);
    font-size: 13px;
    
    .el-icon {
      color: var(--el-color-primary);
    }
  }
  
  // 已读回执
  .read-receipt {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-text-color-placeholder);
    
    .read-receipt-text {
      margin-left: 4px;
    }
  }
  
  // 消息菜单
  :deep(.message-menu) {
    .el-dropdown-menu__item {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .el-icon {
        font-size: 16px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}

// 动画
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
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

// 响应式
@media (max-width: 768px) {
  .message-bubble-enhanced {
    .message-wrapper {
      max-width: 80%;
    }
  }
}

// 深色模式
.dark {
  .message-bubble-enhanced {
    .text-bubble {
      background: var(--el-fill-color-dark);
      
      &::before {
        background: var(--el-fill-color-dark);
      }
    }
    
    .is-self .text-bubble {
      background: var(--el-color-primary-dark);
    }
  }
}
</style>