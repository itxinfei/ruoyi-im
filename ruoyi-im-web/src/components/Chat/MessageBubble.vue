<template>
  <el-dropdown
    trigger="contextmenu"
    @command="handleCommand"
    popper-class="message-context-menu"
  >
    <div
      ref="bubbleRef"
      class="bubble"
      :class="[message.type, { 'is-own': message.isOwn, 'is-selected': isSelected, 'is-long-press': isLongPressing }]"
      @click="handleClick"
      @touchstart="handleTouchStart"
      @touchend="handleTouchEnd"
      @touchcancel="handleTouchEnd"
      @mousedown="handleMouseHold"
      @mouseup="handleMouseRelease"
      @mouseleave="handleMouseRelease"
    >
      <!-- 引用消息区块 (如果该消息是回复某人的) -->
      <div v-if="message.replyTo" class="bubble-reply-ref" @click.stop="$emit('scroll-to', message.replyTo.id)">
        <div class="ref-header">
          <span class="ref-user">{{ message.replyTo.senderName }}</span>
          <span class="ref-type-icon">
            <span v-if="message.replyTo.type === 'IMAGE'" class="material-icons-outlined">image</span>
            <span v-else-if="message.replyTo.type === 'FILE'" class="material-icons-outlined">insert_drive_file</span>
            <span v-else-if="message.replyTo.type === 'VIDEO'" class="material-icons-outlined">videocam</span>
            <span v-else-if="message.replyTo.type === 'VOICE' || message.replyTo.type === 'AUDIO'" class="material-icons-outlined">mic</span>
            <span v-else class="material-icons-outlined">format_quote</span>
          </span>
        </div>
        <div class="ref-content">
          <template v-if="message.replyTo.type === 'IMAGE'">
            <span class="ref-image-text">[图片]</span>
          </template>
          <template v-else-if="message.replyTo.type === 'FILE'">
            <span class="ref-file-text">[文件] {{ getFileName(message.replyTo.content) }}</span>
          </template>
          <template v-else-if="message.replyTo.type === 'VIDEO'">
            <span class="ref-video-text">[视频]</span>
          </template>
          <template v-else-if="message.replyTo.type === 'VOICE' || message.replyTo.type === 'AUDIO'">
            <span class="ref-voice-text">[语音]</span>
          </template>
          <template v-else>
            {{ message.replyTo.content }}
          </template>
        </div>
      </div>

      <!-- 文本消息 -->
      <div v-if="message.type === 'TEXT'" class="text-content-wrapper">
        <!-- 渲染文本和代码块 -->
        <template v-if="parsedTextContent.segments.length > 1">
          <template v-for="(segment, index) in parsedTextContent.segments" :key="index">
            <!-- 普通文本 -->
            <span v-if="segment.type === 'text'" class="main-text">{{ segment.content }}</span>
            <!-- 代码块 -->
            <div v-else class="code-block" :class="'language-' + segment.language">
              <div class="code-header">
                <span class="code-language">{{ segment.language || 'text' }}</span>
                <button class="code-copy-btn" @click="copyCode(segment.content)" title="复制代码">
                  <el-icon><Document /></el-icon>
                </button>
              </div>
              <pre class="code-content"><code>{{ segment.content }}</code></pre>
            </div>
          </template>
        </template>
        <!-- 没有代码块时直接显示原始文本 -->
        <span v-else class="main-text">{{ message.content }}</span>
        <span v-if="message.isEdited" class="edited-tag">(已编辑)</span>

        <!-- 链接卡片 -->
        <div v-if="messageLinks.length > 0" class="message-links">
          <LinkCard
            v-for="(link, index) in messageLinks"
            :key="index"
            :link="link"
          />
        </div>

        <!-- 标记图标 -->
        <div v-if="hasMarkers" class="message-markers">
          <span v-for="marker in message.markers" :key="marker.id || marker.markerType"
                class="marker-icon"
                :class="{ completed: marker.isCompleted }"
                :style="{ color: marker.color || '' }">
            <span v-if="marker.markerType === 'FLAG'" class="material-icons-outlined">flag</span>
            <span v-else-if="marker.markerType === 'IMPORTANT'" class="material-icons-outlined">star</span>
            <span v-else-if="marker.markerType === 'TODO'" class="material-icons-outlined">
              {{ marker.isCompleted ? 'check_circle' : 'check_circle_outline' }}
            </span>
          </span>
        </div>

        <!-- 置顶图标 -->
        <div v-if="message.isPinned" class="message-pinned-badge" title="已置顶">
          <el-icon><Top /></el-icon>
          <span>已置顶</span>
        </div>
      </div>

      <!-- 图片消息 - 点击触发预览 -->
      <div v-else-if="message.type === 'IMAGE' && parsedContent.imageUrl"
           class="image-wrapper"
           :class="{ 'is-uploading': isUploading }"
           @click="handleImageClick">
        <img :src="parsedContent.imageUrl"
             class="msg-image"
             :alt="message.senderName + '的图片'"
             loading="lazy" />
        <!-- 上传进度遮罩 -->
        <div v-if="isUploading" class="image-upload-overlay">
          <div class="upload-progress-ring">
            <svg viewBox="0 0 36 36">
              <path
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                fill="none"
                stroke="rgba(255, 255, 255, 0.3)"
                stroke-width="3"
              />
              <path
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                fill="none"
                stroke="#fff"
                stroke-width="3"
                stroke-dasharray="100"
                stroke-dashoffset="25"
                stroke-linecap="round"
                class="image-upload-spinner"
              />
            </svg>
          </div>
          <span class="upload-text">上传中...</span>
        </div>
      </div>

      <!-- 文件消息 -->
      <div v-else-if="message.type === 'FILE'" class="msg-file" :class="{ 'is-downloading': isDownloading, 'is-uploading': isUploading }" @click="handleFileClick">
        <div class="file-icon-wrapper">
          <el-icon><Document /></el-icon>
          <!-- 上传进度圆环 -->
          <div v-if="isUploading" class="file-upload-progress">
            <svg viewBox="0 0 36 36">
              <path
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                fill="none"
                stroke="rgba(22, 119, 255, 0.15)"
                stroke-width="3"
              />
              <path
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                fill="none"
                stroke="var(--dt-brand-color)"
                stroke-width="3"
                stroke-dasharray="100"
                stroke-dashoffset="25"
                stroke-linecap="round"
                class="upload-spinner"
              />
            </svg>
          </div>
          <!-- 下载进度圆环 -->
          <div v-if="isDownloading" class="file-download-progress">
            <svg viewBox="0 0 36 36">
              <path
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                fill="none"
                stroke="rgba(22, 119, 255, 0.15)"
                stroke-width="3"
              />
              <path
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                fill="none"
                stroke="var(--dt-brand-color)"
                stroke-width="3"
                :stroke-dasharray="`${downloadProgress}, 100`"
                stroke-linecap="round"
              />
            </svg>
          </div>
        </div>
        <div class="file-info">
          <span class="file-name">{{ parsedContent.fileName || '未知文件' }}</span>
          <span class="file-meta">
            <template v-if="isUploading">
              上传中...
            </template>
            <template v-else-if="isDownloading">
              下载中 {{ downloadProgress }}%
            </template>
            <template v-else>
              {{ formatSize(parsedContent.size) }}
            </template>
          </span>
        </div>
        <div class="file-action">
          <el-icon v-if="!isUploading && !isDownloading"><Download /></el-icon>
          <el-icon v-else class="is-spinning"><Loading /></el-icon>
        </div>
      </div>

      <!-- 视频消息 -->
      <div v-else-if="message.type === 'VIDEO'" class="msg-video">
        <video v-if="parsedContent.videoUrl" :src="parsedContent.videoUrl" controls class="video-preview"></video>
      </div>

      <!-- 语音消息 -->
      <div v-else-if="message.type === 'VOICE' || message.type === 'AUDIO'" class="msg-voice" @click="togglePlayVoice">
        <div class="voice-icon">
          <el-icon>
            <VideoPause v-if="isVoicePlaying" />
            <VideoPlay v-else />
          </el-icon>
        </div>
        <div class="voice-waveform">
          <span
            v-for="(item, index) in 20"
            :key="index"
            class="wave-bar"
            :class="{ active: isVoicePlaying && voiceProgress > (index / 20) }"
          ></span>
        </div>
        <span class="voice-duration">{{ formatVoiceDuration(parsedContent.duration) }}</span>
        <audio v-if="parsedContent.voiceUrl" :src="parsedContent.voiceUrl" ref="voiceAudioRef" @ended="onVoiceEnded" @timeupdate="onVoiceTimeUpdate"></audio>
      </div>

      <!-- 位置消息 -->
      <div v-else-if="message.type === 'LOCATION'" class="msg-location" @click="openLocation">
        <div class="location-icon">
          <span class="material-icons-outlined">location_on</span>
        </div>
        <div class="location-info">
          <div class="location-address">{{ parsedContent.address || '位置信息' }}</div>
          <div class="location-coords">{{ formatLocationCoords(parsedContent.latitude, parsedContent.longitude) }}</div>
        </div>
        <div class="location-arrow">
          <span class="material-icons-outlined">open_in_new</span>
        </div>
      </div>

      <!-- 系统消息 -->
      <div v-else-if="message.type === 'SYSTEM'" class="msg-system">
        {{ message.content }}
      </div>

      <!-- 撤回消息 -->
      <div v-else-if="message.type === 'RECALLED'" class="msg-recalled" :class="{ 'is-own': message.isOwn }">
        <span class="recall-icon material-icons-outlined">history</span>
        <div class="recall-content">
          <span class="recall-text">{{ message.isOwn ? '你撤回了一条消息' : `${message.senderName}撤回了一条消息` }}</span>
          <span v-if="message.isOwn" class="recall-reedit" @click.stop="handleReEdit">
            点击重新编辑
          </span>
        </div>
      </div>

      <!-- 合并转发消息 -->
      <CombineMessagePreview
        v-else-if="message.type === 'COMBINE' || message.type === 'COMBINE_FORWARD'"
        :messages="parsedContent.messages || []"
        @click="handleClickCombine"
      />

      <span v-else>[{{ message.type }}]</span>

      <!-- 消息状态图标 -->
      <div v-if="message.isOwn" class="message-status" :class="`status-${message.status || 'sent'}`">
        <!-- 发送中状态 -->
        <transition name="status-fade">
          <div v-if="message.status === 'sending'" class="status-indicator status-sending">
            <span class="sending-dots">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </span>
          </div>
        </transition>

        <!-- 已送达状态 (DELIVERED - 双勾轮廓) -->
        <transition name="status-scale">
          <div v-if="message.status === 'sent'" class="status-indicator status-delivered" title="已送达">
            <span class="material-icons-outlined">done_all</span>
          </div>
        </transition>

        <!-- 已读状态 (READ - 双勾填充) -->
        <transition name="status-scale">
          <div v-if="message.status === 'read'" class="status-indicator status-read" title="已读">
            <span class="material-icons-outlined">done_all</span>
          </div>
        </transition>

        <!-- 发送失败状态 -->
        <transition name="status-shake">
          <div v-if="message.status === 'failed'" class="status-indicator status-failed" @click="handleRetry" title="点击重试">
            <span class="material-icons-outlined">error_outline</span>
          </div>
        </transition>
      </div>

      <!-- 表情聚合显示 -->
      <div v-if="hasReactions" class="reaction-aggregate" @click="showReactionDetail = true">
        <div
          v-for="(reaction, index) in messageReactions"
          :key="index"
          class="reaction-item"
          :class="{ 'is-active': reaction.hasOwnReaction }"
          @click.stop="toggleReaction(reaction.emoji)"
        >
          <span class="reaction-emoji">{{ reaction.emoji }}</span>
          <span class="reaction-count">{{ reaction.count }}</span>
        </div>
      </div>
    </div>

    <!-- 右键菜单 -->
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="copy" v-if="message.type === 'TEXT'">
          <el-icon><CopyDocument /></el-icon> <span>复制</span>
        </el-dropdown-item>
        <el-dropdown-item command="reply">
          <el-icon><ChatLineSquare /></el-icon> <span>回复</span>
        </el-dropdown-item>
        <el-dropdown-item command="emoji" divided>
          <span class="material-icons-outlined" style="font-size: 16px; color: #f5222d;">sentiment_satisfied_alt</span>
          <span>表情表态</span>
        </el-dropdown-item>
        <el-dropdown-item command="at" v-if="!message.isOwn && sessionType === 'GROUP'">
          <el-icon><InfoFilled /></el-icon> <span>@ 提及</span>
        </el-dropdown-item>
        <el-dropdown-item command="forward" divided>
          <el-icon><Share /></el-icon> <span>转发</span>
        </el-dropdown-item>
        <el-dropdown-item command="todo">
          <el-icon><Checked /></el-icon> <span>设为待办</span>
        </el-dropdown-item>
        <el-dropdown-item command="pin" :class="{ 'is-pinned': message.isPinned }">
          <el-icon><Top /></el-icon> <span>{{ message.isPinned ? '取消置顶' : '置顶' }}</span>
        </el-dropdown-item>

        <el-dropdown-item v-if="message.isOwn && canRecall" command="recall" divided class="danger">
          <el-icon><RefreshLeft /></el-icon>
          <span>撤回{{ recallTimeDisplay ? ` (${recallTimeDisplay})` : '' }}</span>
        </el-dropdown-item>
        <el-dropdown-item v-if="message.isOwn" command="delete" class="danger">
          <el-icon><Delete /></el-icon> <span>删除</span>
        </el-dropdown-item>
        <el-dropdown-item v-if="message.isOwn && message.type === 'TEXT'" command="edit" divided>
          <el-icon><Edit /></el-icon> <span>编辑</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>

  <!-- AI表情表态浮窗 -->
  <AiEmojiReaction
    :visible="showAiEmojiPanel"
    :message="message"
    :position="aiEmojiPosition"
    @select="handleAiEmojiSelect"
    @close="showAiEmojiPanel = false"
  />
</template>

<script setup>
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Document, ChatLineSquare, CopyDocument, Share, RefreshLeft, Delete, Edit, InfoFilled, Checked, Loading, WarningFilled, VideoPlay, VideoPause, Download, Top } from '@element-plus/icons-vue'
import CombineMessagePreview from './CombineMessagePreview.vue'
import LinkCard from './LinkCard.vue'
import AiEmojiReaction from './AiEmojiReaction.vue'
import { extractLinksFromContent, formatLinkUrl } from '@/utils/file'

const props = defineProps({
  message: { type: Object, required: true },
  sessionType: { type: String, default: 'PRIVATE' }
})

const emit = defineEmits(['command', 'preview', 'download', 'at', 'scroll-to', 'retry', 'toggle-reaction', 'add-reaction', 're-edit'])

// ============================================================================
// AI表情表态状态
// ============================================================================
const showAiEmojiPanel = ref(false)
const aiEmojiPosition = ref({ x: 0, y: 0 })

// 处理AI表情选择
const handleAiEmojiSelect = (emoji) => {
  emit('add-reaction', props.message.id, emoji, true)
  ElMessage.success(`已添加表情: ${emoji}`)
}

const store = useStore()
const selectedMessages = computed(() => store.state.im.message.selectedMessages)

const isSelected = computed(() => selectedMessages.value.has(props.message.id))

// 是否有标记
const hasMarkers = computed(() => {
  return props.message?.markers && props.message.markers.length > 0
})

const handleClick = (event) => {
  if (event.ctrlKey || event.metaKey) {
    // Ctrl + 点击：不连续多选
    toggleSelection()
    event.stopPropagation()
  } else if (event.shiftKey) {
    // Shift + 点击：连续多选
    rangeSelection()
    event.stopPropagation()
  }
  // 普通点击：不做任何处理
}

const toggleSelection = () => {
  store.commit('im/message/TOGGLE_MESSAGE_SELECTION', props.message.id)
}

const rangeSelection = () => {
  // TODO: 实现连续选择逻辑
  // 需要获取当前会话的所有消息，然后找到当前消息和最后选中的消息之间的所有消息
  toggleSelection()
}

const handleCommand = (cmd) => {
  if (!cmd) return
  if (cmd === 'at') emit('at', props.message)
  else if (cmd === 'emoji') {
    // 显示AI表情面板
    const bubble = bubbleRef.value
    if (bubble) {
      const rect = bubble.getBoundingClientRect()
      aiEmojiPosition.value = {
        x: rect.right + 10,
        y: rect.top
      }
    }
    showAiEmojiPanel.value = true
  }
  else emit('command', cmd, props.message)
}

const handleRetry = () => {
  emit('retry', props.message)
}

// 长按处理（AI表情表态入口）
const LONG_PRESS_DURATION = 500 // 长按时长（毫秒）
let longPressTimer = null
const isLongPressing = ref(false)
const bubbleRef = ref(null)

const handleTouchStart = (e) => {
  // 所有消息类型都支持长按AI表情表态
  longPressTimer = setTimeout(() => {
    isLongPressing.value = true
    // 计算面板显示位置
    const rect = e.currentTarget?.getBoundingClientRect()
    if (rect) {
      aiEmojiPosition.value = {
        x: rect.right + 10,
        y: rect.top
      }
    }
    showAiEmojiPanel.value = true
    // 触发触觉反馈（如果支持）
    if (navigator.vibrate) {
      navigator.vibrate(50)
    }
  }, LONG_PRESS_DURATION)
}

const handleTouchEnd = (e) => {
  if (longPressTimer) {
    clearTimeout(longPressTimer)
    longPressTimer = null
  }

  // 如果是长按结束且不是自己的消息，不触发编辑
  if (isLongPressing.value) {
    isLongPressing.value = false
    // 如果是自己的文本消息且没有打开AI表情面板，触发编辑
    if (props.message.type === 'TEXT' && props.message.isOwn && !showAiEmojiPanel.value) {
      e.preventDefault()
      emit('command', 'edit', props.message)
    }
  }
}

// PC端右键长按模拟（可选）
const handleMouseHold = (e) => {
  // 只对非右键事件启用
  if (e.button !== 0) return

  longPressTimer = setTimeout(() => {
    isLongPressing.value = true
    const rect = e.currentTarget?.getBoundingClientRect()
    if (rect) {
      aiEmojiPosition.value = {
        x: rect.right + 10,
        y: rect.top
      }
    }
    showAiEmojiPanel.value = true
  }, LONG_PRESS_DURATION)
}

const handleMouseRelease = () => {
  if (longPressTimer) {
    clearTimeout(longPressTimer)
    longPressTimer = null
  }
  isLongPressing.value = false
}

// 处理合并转发消息点击
const handleClickCombine = (messages) => {
  emit('command', 'view-combine', { ...props.message, messages })
}

// 处理重新编辑撤回的消息
const handleReEdit = () => {
  if (props.message.isOwn && props.message.originalContent) {
    emit('re-edit', { content: props.message.originalContent })
  }
}

// 处理图片点击 - 触发预览
const handleImageClick = () => {
  const imageUrl = parsedContent.value.imageUrl
  if (imageUrl) {
    emit('preview', imageUrl)
  }
}

const parsedContent = computed(() => {
  try {
    if (!props.message || !props.message.content) return {}
    const isMedia = ['IMAGE', 'FILE', 'VIDEO', 'VOICE', 'AUDIO', 'COMBINE', 'COMBINE_FORWARD', 'LOCATION'].includes(props.message.type)
    return (typeof props.message.content === 'string' && isMedia)
      ? JSON.parse(props.message.content)
      : (props.message.content || {})
  } catch (e) { return {} }
})

// 提取消息中的链接
const messageLinks = computed(() => {
  if (!props.message) return []
  return extractLinksFromContent(props.message.content)
})

// 解析文本内容中的代码块
const parsedTextContent = computed(() => {
  if (props.message.type !== 'TEXT' || !props.message.content) {
    return { segments: [] }
  }

  const content = props.message.content
  const segments = []
  const codeBlockRegex = /```(\w+)?\n([\s\S]*?)```/g
  let lastIndex = 0
  let match

  while ((match = codeBlockRegex.exec(content)) !== null) {
    // 添加代码块之前的普通文本
    if (match.index > lastIndex) {
      const textBefore = content.substring(lastIndex, match.index).trim()
      if (textBefore) {
        segments.push({
          type: 'text',
          content: textBefore
        })
      }
    }

    // 添加代码块
    segments.push({
      type: 'code',
      language: match[1] || 'plaintext',
      content: match[2]
    })

    lastIndex = codeBlockRegex.lastIndex
  }

  // 添加剩余的普通文本
  if (lastIndex < content.length) {
    const textAfter = content.substring(lastIndex).trim()
    if (textAfter) {
      segments.push({
        type: 'text',
        content: textAfter
      })
    }
  }

  // 如果没有代码块，将整个内容作为普通文本
  if (segments.length === 0) {
    segments.push({
      type: 'text',
      content: content
    })
  }

  return { segments }
})

// 复制代码到剪贴板
const copyCode = async (code) => {
  try {
    await navigator.clipboard.writeText(code)
    ElMessage.success('代码已复制')
  } catch (error) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = code
    document.body.appendChild(textarea)
    textarea.select()
    try {
      document.execCommand('copy')
      ElMessage.success('代码已复制')
    } catch (e) {
      ElMessage.error('复制失败')
    }
    document.body.removeChild(textarea)
  }
}

// 获取撤回时限配置（分钟）
const recallTimeLimit = computed(() => {
  return store.state.im.settings.chat?.recallTimeLimit || 2
})

// 倒计时剩余时间（秒）
const recallRemainingSeconds = ref(0)
let recallTimer = null

// 更新倒计时
const updateRecallCountdown = () => {
  if (!props.message?.timestamp) {
    recallRemainingSeconds.value = 0
    return
  }

  const messageTime = new Date(props.message.timestamp).getTime()
  const elapsed = Date.now() - messageTime
  const timeLimit = recallTimeLimit.value * 60 * 1000
  const remaining = Math.max(0, timeLimit - elapsed)
  recallRemainingSeconds.value = Math.ceil(remaining / 1000)
}

// 是否可以撤回
const canRecall = computed(() => {
  if (!props.message?.timestamp || !props.message.isOwn) return false
  return recallRemainingSeconds.value > 0
})

// 格式化剩余时间显示
const recallTimeDisplay = computed(() => {
  const seconds = recallRemainingSeconds.value
  if (seconds <= 0) return ''
  if (seconds < 60) return `${seconds}秒`
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return secs > 0 ? `${minutes}分${secs}秒` : `${minutes}分钟`
})

// 组件挂载时启动倒计时
onMounted(() => {
  updateRecallCountdown()
  recallTimer = setInterval(updateRecallCountdown, 1000)
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (recallTimer) {
    clearInterval(recallTimer)
    recallTimer = null
  }
})

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 获取文件名
const getFileName = (content) => {
  try {
    if (typeof content === 'string') {
      const parsed = JSON.parse(content)
      return parsed.fileName || parsed.name || '文件'
    }
    return content?.fileName || content?.name || '文件'
  } catch {
    return '文件'
  }
}

// 文件下载相关
const isDownloading = ref(false)
const downloadProgress = ref(0)

// 文件上传相关
const isUploading = computed(() => {
  return props.message?.status === 'uploading' || props.message?.status === 'sending'
})

const handleFileClick = async () => {
  if (isDownloading.value) return

  const fileUrl = parsedContent.value?.fileUrl || parsedContent.value?.url
  if (!fileUrl) {
    emit('download', parsedContent.value)
    return
  }

  // 如果是同源或支持跨域的直接下载
  try {
    isDownloading.value = true
    downloadProgress.value = 0

    // 模拟下载进度（实际项目中可以使用 axios 的 onDownloadProgress）
    const progressInterval = setInterval(() => {
      if (downloadProgress.value < 90) {
        downloadProgress.value += Math.random() * 15
      }
    }, 200)

    // 创建下载链接
    const response = await fetch(fileUrl)
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = url
    link.download = parsedContent.value?.fileName || '下载文件'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    clearInterval(progressInterval)
    downloadProgress.value = 100

    // 延迟重置状态
    setTimeout(() => {
      isDownloading.value = false
      downloadProgress.value = 0
    }, 500)
  } catch (error) {
    console.error('文件下载失败:', error)
    isDownloading.value = false
    downloadProgress.value = 0
    // 降级到 emit 方式
    emit('download', parsedContent.value)
  }
}

// ============================================================================
// 表情回复相关
// ============================================================================
// 快捷表情列表（钉钉风格）
const showReactionDetail = ref(false)
const isReacting = ref(false)

// 消息的表情回复数据
const messageReactions = computed(() => {
  if (!props.message?.reactions) return []

  const currentUser = store.getters['user/currentUser']
  const reactions = {}

  // 按表情分组
  props.message.reactions.forEach(r => {
    if (!reactions[r.emoji]) {
      reactions[r.emoji] = {
        emoji: r.emoji,
        users: [],
        count: 0,
        hasOwnReaction: false
      }
    }
    reactions[r.emoji].users.push(r)
    reactions[r.emoji].count++
    reactions[r.emoji].hasOwnReaction = r.userId === currentUser?.id
  })

  return Object.values(reactions)
})

const hasReactions = computed(() => messageReactions.value.length > 0)

// 检查当前用户是否对某个表情回复过
const hasReacted = (emoji) => {
  const currentUser = store.getters['user/currentUser']
  return props.message?.reactions?.some(
    r => r.emoji === emoji && r.userId === currentUser?.id
  )
}

// 处理表情回复
const handleReaction = async (emoji) => {
  if (isReacting.value) return

  // 检查是否已经回复过
  const alreadyReacted = hasReacted(emoji)

  try {
    isReacting.value = true
    const { addReaction, removeReaction } = await import('@/api/im/message')

    if (alreadyReacted) {
      // 取消回复
      await removeReaction(props.message.id)
      // 更新本地状态
      if (props.message.reactions) {
        props.message.reactions = props.message.reactions.filter(
          r => !(r.emoji === emoji && r.userId === store.getters['user/currentUser']?.id)
        )
      }
    } else {
      // 添加回复
      await addReaction(props.message.id, { emoji })
      // 添加到本地状态
      if (!props.message.reactions) {
        props.message.reactions = []
      }
      props.message.reactions.push({
        emoji,
        userId: store.getters['user/currentUser']?.id,
        userName: store.getters['user/currentUser']?.nickName || '我',
        userAvatar: store.getters['user/currentUser']?.avatar
      })
    }

    // 通知父组件更新
    emit('add-reaction', props.message.id, emoji, !alreadyReacted)
  } catch (error) {
    console.error('表情回复失败:', error)
  } finally {
    isReacting.value = false
  }
}

// 切换表情回复（点击表情聚合）
const toggleReaction = async (emoji) => {
  await handleReaction(emoji)
}

// 语音消息相关
const voiceAudioRef = ref(null)
const isVoicePlaying = ref(false)
const voiceProgress = ref(0)

// 切换语音播放
const togglePlayVoice = () => {
  if (!voiceAudioRef.value) return

  if (isVoicePlaying.value) {
    voiceAudioRef.value.pause()
  } else {
    // 停止其他正在播放的语音
    document.querySelectorAll('audio.voice-audio').forEach(audio => {
      if (audio !== voiceAudioRef.value) {
        audio.pause()
        audio.currentTime = 0
      }
    })
    voiceAudioRef.value.play()
  }
  isVoicePlaying.value = !isVoicePlaying.value
}

// 语音播放结束
const onVoiceEnded = () => {
  isVoicePlaying.value = false
  voiceProgress.value = 0
}

// 语音播放进度更新
const onVoiceTimeUpdate = () => {
  if (!voiceAudioRef.value) return
  voiceProgress.value = voiceAudioRef.value.currentTime / voiceAudioRef.value.duration
}

// 格式化语音时长
const formatVoiceDuration = (seconds) => {
  if (!seconds) return '0:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// 格式化坐标
const formatLocationCoords = (lat, lng) => {
  if (!lat || !lng) return ''
  return `${lat.toFixed(6)}, ${lng.toFixed(6)}`
}

// 打开位置（在地图中查看）
const openLocation = () => {
  const { latitude, longitude, address } = parsedContent.value
  if (!latitude || !longitude) {
    ElMessage.warning('位置信息不完整')
    return
  }

  // 使用高德地图或百度地图打开位置
  // 高德地图: https://uri.amap.com/marker?position=lng,lat&name=address
  // 百度地图: https://api.map.baidu.com/marker?location=lat,lng&title=address&content=address&output=html
  const url = `https://uri.amap.com/marker?position=${longitude},${latitude}&name=${encodeURIComponent(address || '位置')}`
  window.open(url, '_blank')
}

// 组件卸载时停止播放
onUnmounted(() => {
  if (voiceAudioRef.value) {
    voiceAudioRef.value.pause()
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.bubble {
  background: #D4EAFF;
  border: none;
  padding: 10px 12px;
  border-radius: 8px 8px 8px 2px;
  box-shadow: none;
  font-size: 14px;
  word-break: break-word;
  line-height: 1.5;
  color: #303133;
  position: relative;
  max-width: 520px;
  transition: background-color 0.15s ease, border-color 0.15s ease;
  animation: messagePop 0.3s var(--dt-ease-bounce);

  &.is-selected {
    border: 2px solid var(--dt-brand-color);
    background-color: #C4E0FF;
    animation: pulse 0.3s var(--dt-ease-out);
  }

  &.is-long-press {
    animation: longPressPulse 0.3s ease-in-out;
    border-color: var(--dt-brand-color);
  }

  @keyframes longPressPulse {
    0% { transform: scale(1); }
    50% { transform: scale(1.01); }
    100% { transform: scale(1); }
  }

  &:hover {
    background: #C4E0FF;
  }

  &.is-own {
    background: #D4EAFF;
    color: #303133;
    border: none;
    border-radius: 8px 8px 2px 8px;
    box-shadow: none;
    text-align: left;

    &.is-selected {
      border: 2px solid var(--dt-brand-color);
      background-color: #C4E0FF;
    }

    &:hover {
      background: #C4E0FF;
    }
  }

  .bubble-reply-ref {
    position: relative;
    display: flex;
    flex-direction: column;
    background: rgba(255, 255, 255, 0.5);
    border: 1px solid rgba(255, 255, 255, 0.6);
    border-left: 3px solid var(--dt-brand-color);
    padding: 10px 12px;
    margin: -8px -10px 10px -10px;
    border-radius: 6px 0 0 6px;
    font-size: 12px;
    color: #475569;
    cursor: pointer;
    overflow: hidden;
    user-select: none;
    transition: all 0.25s var(--dt-ease-out);
    animation: slideInDown 0.3s var(--dt-ease-out);

    @keyframes slideInDown {
      from {
        opacity: 0;
        transform: translateY(-4px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 60%;
      background: linear-gradient(
        180deg,
        transparent 0%,
        var(--dt-brand-color) 20%,
        var(--dt-brand-color) 80%,
        transparent 100%
      );
      border-radius: 0 2px 2px 0;
      transition: height 0.25s var(--dt-ease-out);
    }

    &::after {
      content: 'keyboard_return';
      font-family: 'Material Icons Outlined';
      position: absolute;
      right: 8px;
      bottom: 6px;
      font-size: 14px;
      color: var(--dt-brand-color);
      opacity: 0;
      transform: translateX(-4px);
      transition: all 0.25s var(--dt-ease-out);
    }

    .ref-header {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-bottom: 5px;

      .ref-user {
        font-weight: 600;
        color: #1e293b;
        font-size: 12px;
        display: flex;
        align-items: center;
        gap: 4px;

        &::before {
          content: '';
          display: inline-block;
          width: 4px;
          height: 4px;
          background: var(--dt-brand-color);
          border-radius: 50%;
        }
      }

      .ref-type-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 18px;
        height: 18px;
        background: rgba(22, 119, 255, 0.1);
        color: var(--dt-brand-color);
        border-radius: 4px;
        font-size: 12px;
        margin-left: auto;
        transition: all 0.25s var(--dt-ease-out);

        .material-icons-outlined {
          font-size: 14px;
        }
      }
    }

    .ref-content {
      font-size: 12px;
      color: #64748b;
      line-height: 1.5;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      padding-left: 8px;
      border-left: 1px dashed rgba(148, 163, 184, 0.5);
      transition: all 0.25s var(--dt-ease-out);

      .ref-image-text,
      .ref-file-text,
      .ref-video-text,
      .ref-voice-text {
        color: var(--dt-brand-color);
        font-weight: 500;
        display: inline-flex;
        align-items: center;
        gap: 4px;

        &::before {
          content: 'attach_file';
          font-family: 'Material Icons Outlined';
          font-size: 14px;
        }
      }

      .ref-image-text::before { content: 'image'; }
      .ref-video-text::before { content: 'videocam'; }
      .ref-voice-text::before { content: 'mic'; }
    }

    &:hover {
      background: rgba(255, 255, 255, 0.7);
      border-color: rgba(255, 255, 255, 0.8);
      border-left-color: var(--dt-brand-color);
      box-shadow: 0 2px 4px rgba(22, 119, 255, 0.1);
      transform: translateX(2px);

      &::before {
        height: 80%;
      }

      &::after {
        opacity: 0.6;
        transform: translateX(0);
      }

      .ref-type-icon {
        background: var(--dt-brand-color);
        color: #fff;
        transform: scale(1.05);
      }

      .ref-content {
        border-left-color: var(--dt-brand-color);
        color: #475569;
      }
    }

    &:active {
      transform: translateX(1px) scale(0.99);
      box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
    }
  }

  &.is-own .bubble-reply-ref {
    background: rgba(255, 255, 255, 0.5);
    border-color: rgba(255, 255, 255, 0.6);
    border-left-color: var(--dt-brand-color);

    .ref-header .ref-user {
      color: #1e293b;
    }

    .ref-content {
      color: #64748b;
      border-left-color: rgba(148, 163, 184, 0.5);
    }

    &:hover {
      background: rgba(255, 255, 255, 0.7);
      border-color: rgba(255, 255, 255, 0.8);
      border-left-color: var(--dt-brand-color);
      box-shadow: 0 2px 4px rgba(22, 119, 255, 0.1);

      .ref-type-icon {
        background: var(--dt-brand-color);
        color: #fff;
      }

      .ref-content {
        border-left-color: var(--dt-brand-color);
        color: #475569;
      }
    }
  }

  @media (prefers-color-scheme: dark) {
    .bubble-reply-ref {
      background: rgba(255, 255, 255, 0.08);
      border-color: rgba(255, 255, 255, 0.1);
      border-left-color: var(--dt-brand-color);

      .ref-header .ref-user {
        color: #e2e8f0;
      }

      .ref-content {
        color: #94a3b8;
        border-left-color: rgba(148, 163, 184, 0.3);
      }

      &:hover {
        background: rgba(22, 119, 255, 0.15);
        border-color: rgba(59, 130, 246, 0.3);

        .ref-type-icon {
          background: rgba(22, 119, 255, 0.2);
          color: #60a5fa;
        }
      }

      &::after {
        color: #60a5fa;
      }
    }
  }

  .text-content-wrapper {
    display: flex; flex-direction: column;
    .main-text { white-space: pre-wrap; }
    .edited-tag { font-size: 11px; opacity: 0.5; margin-top: 2px; align-self: flex-end; }

    // 代码块样式
    .code-block {
      margin: 8px 0;
      background: #1e1e1e;
      border-radius: 6px;
      overflow: hidden;
      font-size: 13px;
      max-width: 500px;

      .code-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 6px 12px;
        background: #2d2d2d;
        border-bottom: 1px solid #3e3e3e;

        .code-language {
          font-size: 11px;
          color: #8b949e;
          text-transform: uppercase;
          font-weight: 500;
        }

        .code-copy-btn {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 24px;
          height: 24px;
          border: none;
          background: transparent;
          color: #8b949e;
          cursor: pointer;
          border-radius: 4px;
          transition: all 0.2s;

          &:hover {
            background: rgba(255, 255, 255, 0.1);
            color: #fff;
          }

          .el-icon {
            font-size: 14px;
          }
        }
      }

      .code-content {
        margin: 0;
        padding: 12px;
        overflow-x: auto;
        background: transparent;

        code {
          font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
          line-height: 1.6;
          color: #e6edf3;
          white-space: pre;
        }
      }

      // 语言标识颜色
      &.language-javascript .code-language { color: #f1e05a; }
      &.language-typescript .code-language { color: #3178c6; }
      &.language-python .code-language { color: #3572A5; }
      &.language-java .code-language { color: #b07219; }
      &.language-cpp .code-language { color: #f34b7d; }
      &.language-c .code-language { color: #555555; }
      &.language-go .code-language { color: #00ADD8; }
      &.language-rust .code-language { color: #dea584; }
      &.language-ruby .code-language { color: #701516; }
      &.language-php .code-language { color: #4F5D95; }
      &.language-swift .code-language { color: #F05138; }
      &.language-kotlin .code-language { color: #A97BFF; }
      &.language-html .code-language { color: #e34c26; }
      &.language-css .code-language { color: #563d7c; }
      &.language-scss .code-language { color: #c6538c; }
      &.language-json .code-language { color: #cbcb41; }
      &.language-sql .code-language { color: #cc3e44; }
      &.language-bash .code-language { color: #89e051; }
      &.language-shell .code-language { color: #89e051; }
    }

    // 链接卡片容器
    .message-links {
      margin-top: 8px;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    // 消息标记图标
    .message-markers {
      display: flex;
      gap: 4px;
      margin-top: 4px;
      align-self: flex-start;

      .marker-icon {
        display: inline-flex;
        align-items: center;
        font-size: 14px;
        padding: 2px 4px;
        border-radius: 4px;
        background: rgba(0, 0, 0, 0.05);
        transition: all 0.2s var(--dt-ease-out);
        animation: scaleIn 0.2s var(--dt-ease-bounce);

        &.completed {
          opacity: 0.6;
          text-decoration: line-through;
        }

        &:hover {
          transform: scale(1.15);
        }
      }
    }

    // 置顶徽章
    .message-pinned-badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      margin-top: 6px;
      padding: 4px 8px;
      background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
      color: #fff;
      font-size: 12px;
      border-radius: 12px;
      align-self: flex-start;
      box-shadow: 0 2px 4px rgba(22, 119, 255, 0.3);
      animation: slideInDown 0.3s var(--dt-ease-out);

      .el-icon {
        font-size: 14px;
      }
    }
  }


  &.IMAGE {
    padding: 4px;
    border-radius: 4px;
    background: var(--dt-bg-card) !important;
    border: 1px solid var(--dt-border-light);
  }
  &.VIDEO {
    padding: 0;
    border-radius: 4px;
    background: #000 !important;
  }
}

// 图片容器（用于点击预览）
.image-wrapper {
  display: inline-block;
  cursor: zoom-in;

  .msg-image {
    pointer-events: none; // 让点击事件穿透到 wrapper
  }
}

.msg-image {
  max-width: 320px;
  max-height: 400px;
  border-radius: 4px;
  display: block;
  cursor: zoom-in;
  transition: all 0.3s var(--dt-ease-out);
  animation: fadeIn 0.4s var(--dt-ease-out);

  @include hover-lift;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  }
}

// 图片上传遮罩
.image-wrapper.is-uploading {
  position: relative;
  pointer-events: none;

  .msg-image {
    opacity: 0.7;
    filter: blur(2px);
  }

  .image-upload-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.4);
    border-radius: 4px;
    animation: fadeIn 0.3s var(--dt-ease-out);

    .upload-progress-ring {
      margin-bottom: 12px;

      svg {
        width: 48px;
        height: 48px;
        transform: rotate(-90deg);
      }

      .image-upload-spinner {
        animation: uploadSpin 1.5s linear infinite;
      }
    }

    .upload-text {
      color: #fff;
      font-size: 14px;
      font-weight: 600;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
    }
  }
}

@keyframes uploadSpin {
  0% {
    stroke-dashoffset: 100;
  }
  100% {
    stroke-dashoffset: 0;
  }
}

.message-status {
  display: flex;
  align-items: center;
  margin-left: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.25s var(--dt-ease-out);
  align-self: flex-end;

  .status-indicator {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 18px;
    height: 18px;
    border-radius: 50%;
    transition: all 0.25s var(--dt-ease-out);

    .material-icons-outlined {
      font-size: 16px;
    }
  }

  // 发送中状态
  .status-sending {
    color: #94a3b8;

    .sending-dots {
      display: flex;
      align-items: center;
      gap: 3px;

      .dot {
        width: 4px;
        height: 4px;
        background: currentColor;
        border-radius: 50%;
        animation: sendingBounce 1.4s ease-in-out infinite;

        &:nth-child(1) { animation-delay: 0s; }
        &:nth-child(2) { animation-delay: 0.16s; }
        &:nth-child(3) { animation-delay: 0.32s; }
      }
    }
  }

  // 已送达状态 (DELIVERED - 双勾灰色)
  .status-delivered {
    color: #94a3b8;
    opacity: 0.9;
    transition: all 0.25s var(--dt-ease-out);

    .material-icons-outlined {
      font-weight: 400;
    }

    &:hover {
      color: var(--dt-brand-color);
      transform: scale(1.1);
    }
  }

  // 已读状态 (READ - 双勾蓝色)
  .status-read {
    color: var(--dt-brand-color);
    opacity: 1;

    .material-icons-outlined {
      font-weight: 700;
    }

    &:hover {
      transform: scale(1.1);
    }
  }

  // 失败状态
  .status-failed {
    color: #ef4444;
    cursor: pointer;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      inset: -4px;
      border-radius: 50%;
      background: rgba(239, 68, 68, 0.1);
      opacity: 0;
      transition: opacity 0.25s var(--dt-ease-out);
      animation: pulse 2s ease-in-out infinite;
    }

    &:hover {
      transform: scale(1.15);

      &::after {
        opacity: 1;
      }
    }
  }
}

// 发送中的弹跳动画
@keyframes sendingBounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

// 置顶徽章滑入动画
@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 状态过渡动画
.status-fade-enter-active,
.status-fade-leave-active {
  transition: all 0.2s var(--dt-ease-out);
}

.status-fade-enter-from,
.status-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.status-scale-enter-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.status-scale-leave-active {
  transition: all 0.2s var(--dt-ease-out);
}

.status-scale-enter-from {
  opacity: 0;
  transform: scale(0);
}

.status-scale-leave-to {
  opacity: 0;
  transform: scale(0.5);
}

.status-shake-enter-active {
  animation: shake 0.5s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

.status-shake-leave-active {
  transition: all 0.2s var(--dt-ease-out);
}

.status-shake-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}

.msg-file {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  background: #f8fafc;
  padding: 12px 14px;
  border-radius: 8px;
  border: 1px solid #eef2f6;
  transition: all 0.25s var(--dt-ease-out);
  animation: fadeIn 0.3s var(--dt-ease-out);
  position: relative;
  overflow: hidden;

  @include hover-lift;

  &:hover {
    border-color: #1677ff;
    box-shadow: 0 2px 8px rgba(22, 119, 255, 0.15);

    .file-action {
      opacity: 1;
      transform: translateX(0);
    }
  }

  &.is-downloading {
    border-color: var(--dt-brand-color);
    background: #eff6ff;
    cursor: wait;

    .file-icon-wrapper {
      animation: pulse 1.5s ease-in-out infinite;
    }
  }

  .file-icon-wrapper {
    position: relative;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .el-icon {
      font-size: 32px;
      color: var(--dt-brand-color);
      transition: transform 0.25s var(--dt-ease-out);
    }

    .file-download-progress {
      position: absolute;
      top: -4px;
      left: -4px;
      right: -4px;
      bottom: -4px;
      display: flex;
      align-items: center;
      justify-content: center;

      svg {
        width: 48px;
        height: 48px;
        transform: rotate(-90deg);
      }
    }

    .file-upload-progress {
      position: absolute;
      top: -4px;
      left: -4px;
      right: -4px;
      bottom: -4px;
      display: flex;
      align-items: center;
      justify-content: center;

      svg {
        width: 48px;
        height: 48px;
        transform: rotate(-90deg);
      }

      .upload-spinner {
        animation: uploadSpin 1.5s linear infinite;
      }

      @keyframes uploadSpin {
        0% {
          stroke-dashoffset: 100;
        }
        100% {
          stroke-dashoffset: 0;
        }
      }
    }
  }

  &:hover .file-icon-wrapper .el-icon {
    transform: scale(1.05);
  }

  .file-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    min-width: 0;

    .file-name {
      font-weight: 600;
      font-size: 14px;
      color: #1f2329;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .file-meta {
      font-size: 11px;
      color: #8f959e;
      margin-top: 2px;
      display: flex;
      align-items: center;
      gap: 4px;

      // 进度条背景
      &::before {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        height: 2px;
        background: rgba(22, 119, 255, 0.2);
        width: calc(100% - 28px);
        border-radius: 0 0 8px 8px;
        opacity: 0;
        transition: opacity 0.25s var(--dt-ease-out);
      }
    }
  }

  .is-downloading .file-meta::before {
    opacity: 1;
  }

  .file-action {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    color: #8f959e;
    opacity: 0.6;
    transition: all 0.25s var(--dt-ease-out);
    transform: translateX(4px);
    flex-shrink: 0;

    .el-icon {
      font-size: 18px;
    }

    &.is-spinning .el-icon {
      animation: spin 1s linear infinite;
      color: var(--dt-brand-color);
    }
  }
}

// 旋转动画
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 脉冲动画
@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

// 撤回消息样式 - 钉钉风格
.msg-recalled {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--dt-bg-body);
  border-radius: 8px;
  font-size: 13px;
  animation: fadeIn 0.3s var(--dt-ease-out);
  max-width: 280px;

  .recall-icon {
    color: var(--dt-text-tertiary);
    font-size: 16px;
  }

  .recall-content {
    display: flex;
    flex-direction: column;
    gap: 2px;

    .recall-text {
      color: var(--dt-text-secondary);
    }

    .recall-reedit {
      color: var(--dt-brand-color);
      cursor: pointer;
      font-size: 12px;
      transition: color var(--dt-transition-fast);

      &:hover {
        color: var(--dt-brand-hover);
        text-decoration: underline;
      }
    }
  }

  &.is-own {
    background: rgba(255, 59, 48, 0.05);

    .recall-icon {
      color: #ff3b30;
    }
  }
}

.msg-system {
  font-size: 12px;
  color: #8f959e;
  text-align: center;
  width: 100%;
  margin: 8px 0;
  animation: fadeIn 0.3s var(--dt-ease-out);
}

// 视频消息样式
.msg-video {
  .video-preview {
    max-width: 320px;
    max-height: 400px;
    border-radius: 4px;
    display: block;
    cursor: pointer;
  }
}

// 语音消息样式
.msg-voice {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 160px;
  max-width: 280px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s var(--dt-ease-out);
  animation: scaleIn 0.3s var(--dt-ease-bounce);

  @include hover-lift;

  &:hover {
    transform: scale(1.02);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  }

  &.is-own {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  }

  .voice-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    color: #fff;
    flex-shrink: 0;
    transition: transform 0.2s var(--dt-ease-out);
  }

  &:hover .voice-icon {
    transform: scale(1.1);
  }

  .voice-waveform {
    display: flex;
    gap: 2px;
    align-items: center;
    flex: 1;
    height: 20px;

    .wave-bar {
      width: 3px;
      height: 6px;
      background: rgba(255, 255, 255, 0.4);
      border-radius: 2px;
      transition: all 0.15s var(--dt-ease-out);

      &.active {
        background: rgba(255, 255, 255, 0.9);
        height: 14px;
        animation: voiceWave 0.3s ease-in-out infinite alternate;
      }
    }
  }

  .voice-duration {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.9);
    font-variant-numeric: tabular-nums;
  }
}

@keyframes voiceWave {
  from { transform: scaleY(0.8); }
  to { transform: scaleY(1.2); }
}

// 位置消息样式
.msg-location {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 200px;
  max-width: 320px;
  padding: 12px 16px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s var(--dt-ease-out);
  animation: fadeIn 0.3s var(--dt-ease-out);

  @include hover-lift;

  &:hover {
    border-color: var(--dt-brand-color);
    background: #e0f2fe;
  }

  .location-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #0ea5e9, #0284c7);
    border-radius: 50%;
    color: #fff;
    flex-shrink: 0;
    transition: transform 0.2s var(--dt-ease-out);

    .material-icons-outlined {
      font-size: 24px;
    }
  }

  &:hover .location-icon {
    transform: scale(1.05);
  }

  .location-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;

    .location-address {
      font-size: 14px;
      font-weight: 500;
      color: #0f172a;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .location-coords {
      font-size: 11px;
      color: #64748b;
      font-variant-numeric: tabular-nums;
    }
  }

  .location-arrow {
    color: #0ea5e9;
    flex-shrink: 0;

    .material-icons-outlined {
      font-size: 18px;
    }
  }
}

// ============================================================================
// 表情聚合显示
// ============================================================================
.reaction-aggregate {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
  margin-left: -2px;

  .reaction-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    background: rgba(22, 119, 255, 0.08);
    border-radius: 12px;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s var(--dt-ease-out);
    border: 1px solid transparent;

    .reaction-emoji {
      font-size: 14px;
    }

    .reaction-count {
      font-size: 11px;
      font-weight: 500;
      color: #64748b;
      min-width: 12px;
    }

    &:hover {
      background: rgba(22, 119, 255, 0.15);

      .reaction-count {
        color: var(--dt-brand-color);
      }
    }

    &.is-active {
      background: var(--dt-brand-bg);
      border-color: var(--dt-brand-color);

      .reaction-emoji {
        animation: bounce 0.3s var(--dt-ease-out);
      }

      .reaction-count {
        color: var(--dt-brand-color);
      }
    }
  }
}

@keyframes bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.3); }
}

// ============================================================================
// 暗色模式 - 钉钉风格适配
// ============================================================================
:global(.dark) {
  .bubble {
    background: #1E40AF;
    color: #FFFFFF;
    border: none;
    border-radius: 8px 8px 8px 2px;

    &.is-own {
      background: #1E40AF;
      color: #FFFFFF;
      border: none;
      border-radius: 8px 8px 2px 8px;
    }

    &:hover {
      background: #1E3A8A;
    }

    &.is-own:hover {
      background: #1E3A8A;
    }

    .bubble-reply-ref {
      background: rgba(255, 255, 255, 0.05);
      color: #94A3B8;
      .ref-user { color: #F1F5F9; }
    }
  }
  .msg-file {
    background: #0F172A;
    border-color: #334155;
    .file-name { color: #F1F5F9; }
  }

  .reaction-aggregate {
    .reaction-item {
      background: rgba(22, 119, 255, 0.1);
      border-color: #334155;

      &:hover {
        background: rgba(22, 119, 255, 0.2);
        border-color: #1677ff;
      }

      &.is-active {
        background: rgba(22, 119, 255, 0.25);
        border-color: #1677ff;
      }

      .reaction-emoji {
        filter: brightness(1.1);
      }

      .reaction-count {
        color: #cbd5e1;
      }

      &.is-active .reaction-count {
        color: #f1f5f9;
      }
    }
  }

  .reaction-detail-dialog {
    .dialog-header {
      border-color: #334155;
    }

    .dialog-content {
      background: #0f172a;

      .reaction-user-item {
        border-color: #1e293b;

        &:hover {
          background: #1e293b;
        }
      }
    }
  }
}
</style>
