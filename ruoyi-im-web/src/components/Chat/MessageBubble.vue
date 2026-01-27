<template>
  <el-dropdown
    trigger="contextmenu"
    @command="handleCommand"
    popper-class="message-context-menu"
  >
    <div
      class="bubble"
      :class="[message.type, { 'is-own': message.isOwn, 'is-selected': isSelected, 'is-long-press': isLongPressing }]"
      @click="handleClick"
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
        <span class="main-text">{{ message.content }}</span>
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
      </div>

      <!-- 图片消息 - 点击触发预览 -->
      <div v-else-if="message.type === 'IMAGE' && parsedContent.imageUrl"
           class="image-wrapper"
           @click="handleImageClick">
        <img :src="parsedContent.imageUrl"
             class="msg-image"
             :alt="message.senderName + '的图片'"
             loading="lazy" />
      </div>

      <!-- 文件消息 -->
      <div v-else-if="message.type === 'FILE'" class="msg-file" @click="$emit('download', parsedContent)">
        <el-icon><Document /></el-icon>
        <div class="file-info">
          <span class="file-name">{{ parsedContent.fileName || '未知文件' }}</span>
          <span class="file-size">{{ formatSize(parsedContent.size) }}</span>
        </div>
      </div>

      <!-- 视频消息 -->
      <div v-else-if="message.type === 'VIDEO'" class="msg-video">
        <video v-if="parsedContent.videoUrl" :src="parsedContent.videoUrl" controls class="video-preview"></video>
      </div>

      <!-- 语音消息 -->
      <div v-else-if="message.type === 'VOICE' || message.type === 'AUDIO'" class="msg-voice" @click="togglePlayVoice">
        <div class="voice-icon">
          <el-icon><component :is="isVoicePlaying ? 'VideoPause' : 'VideoPlay'" /></el-icon>
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
      <div v-else-if="message.type === 'RECALLED'" class="msg-recalled">
        <span class="material-icons-outlined">block</span>
        <span>{{ message.isOwn ? '你撤回了一条消息' : `${message.senderName}撤回了一条消息` }}</span>
      </div>

      <!-- 合并转发消息 -->
      <CombineMessagePreview
        v-else-if="message.type === 'COMBINE' || message.type === 'COMBINE_FORWARD'"
        :messages="parsedContent.messages || []"
        @click="handleClickCombine"
      />

      <span v-else>[{{ message.type }}]</span>

      <!-- 消息状态图标 -->
      <div v-if="message.isOwn" class="message-status">
        <el-icon v-if="message.status === 'sending'" class="is-loading" color="#909399">
          <Loading />
        </el-icon>
        <el-icon v-else-if="message.status === 'sent'" color="#909399">
          <Check />
        </el-icon>
        <el-icon v-else-if="message.status === 'read'" color="#909399">
          <Check />
          <Check />
        </el-icon>
        <el-icon v-else-if="message.status === 'failed'" color="#f56c6c" @click="handleRetry">
          <WarningFilled />
        </el-icon>
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
        <el-dropdown-item command="at" v-if="!message.isOwn && sessionType === 'GROUP'">
          <el-icon><InfoFilled /></el-icon> <span>@ 提及</span>
        </el-dropdown-item>
        <el-dropdown-item command="forward" divided>
          <el-icon><Share /></el-icon> <span>转发</span>
        </el-dropdown-item>
        <el-dropdown-item command="todo">
          <el-icon><Checked /></el-icon> <span>设为待办</span>
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
</template>

<script setup>
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { Document, ChatLineSquare, CopyDocument, Share, RefreshLeft, Delete, Edit, InfoFilled, Checked, Loading, WarningFilled, VideoPlay, VideoPause } from '@element-plus/icons-vue'
import CombineMessagePreview from './CombineMessagePreview.vue'
import LinkCard from './LinkCard.vue'
import { extractLinksFromContent, formatLinkUrl } from '@/utils/file'

const props = defineProps({
  message: { type: Object, required: true },
  sessionType: { type: String, default: 'PRIVATE' }
})

const emit = defineEmits(['command', 'preview', 'download', 'at', 'scroll-to', 'retry'])

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
  else emit('command', cmd, props.message)
}

const handleRetry = () => {
  emit('retry', props.message)
}

// 长按处理（移动端编辑入口）
const LONG_PRESS_DURATION = 500 // 长按时长（毫秒）
let longPressTimer = null
const isLongPressing = ref(false)

const handleTouchStart = (e) => {
  // 只对文本消息启用长按编辑
  if (props.message.type !== 'TEXT' || !props.message.isOwn) return

  longPressTimer = setTimeout(() => {
    isLongPressing.value = true
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

  // 如果是长按结束，触发编辑
  if (isLongPressing.value) {
    isLongPressing.value = false
    e.preventDefault()
    emit('command', 'edit', props.message)
  }
}

// 处理合并转发消息点击
const handleClickCombine = (messages) => {
  emit('command', 'view-combine', { ...props.message, messages })
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
  background: var(--dt-bubble-left-bg);
  padding: 10px 14px;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.06);
  font-size: 14px;
  word-break: break-word;
  line-height: 1.6;
  color: #1f2329;
  position: relative;
  max-width: 520px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e2e8f0;
  animation: messagePop 0.3s var(--dt-ease-bounce);

  @include hover-lift(-1px);

  &.is-selected {
    border: 2px solid #1890ff;
    background-color: #e6f7ff;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
    animation: pulse 0.3s var(--dt-ease-out);
  }

  // 长按视觉反馈
  &.is-long-press {
    animation: longPressPulse 0.3s ease-in-out;
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.2);
  }

  @keyframes longPressPulse {
    0% { transform: scale(1); }
    50% { transform: scale(1.02); }
    100% { transform: scale(1); }
  }

  &.is-own {
    background: var(--dt-bubble-right-bg);
    color: #1f2329;
    border-radius: 4px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
    border: none;

    &.is-selected {
      border: 2px solid #1890ff;
      background-color: #e6f7ff;
    }
  }

  /* 引用回复展示 - 钉钉风格增强 */
  .bubble-reply-ref {
    position: relative;
    display: flex;
    flex-direction: column;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
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

    // 引用指示器竖线装饰
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

    // 右侧箭头指示
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
        color: #334155;
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
      border-left: 1px dashed #cbd5e1;
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
      background: #eff6ff;
      border-color: #bfdbfe;
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

    // 激活状态（点击跳转时）
    &:active {
      transform: translateX(1px) scale(0.99);
      box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
    }
  }

  // 自己发送消息中的引用回复样式适配
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
      border-color: rgba(22, 119, 255, 0.3);
    }
  }

  // 暗色模式适配
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
  }

  &:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.08); }

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

.message-status {
  display: flex;
  align-items: center;
  margin-left: 5px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s var(--dt-ease-out);

  @include hover-lift;

  .el-icon {
    margin: 0 1px;
    transition: transform 0.2s var(--dt-ease-out);
  }

  &:hover .el-icon {
    transform: scale(1.1);
  }
}

.msg-file {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  background: #f8fafc;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #eef2f6;
  transition: all 0.2s var(--dt-ease-out);
  animation: fadeIn 0.3s var(--dt-ease-out);

  @include hover-lift;

  &:hover {
    border-color: #1677ff;
    box-shadow: 0 2px 8px rgba(22, 119, 255, 0.15);
  }

  .el-icon {
    font-size: 32px;
    color: #1677ff;
    transition: transform 0.2s var(--dt-ease-out);
  }

  &:hover .el-icon {
    transform: scale(1.05);
  }

  .file-info {
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .file-name {
      font-weight: 600;
      font-size: 14px;
      color: #1f2329;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .file-size {
      font-size: 11px;
      color: #8f959e;
      margin-top: 2px;
    }
  }
}

.msg-recalled {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8f959e;
  font-size: 13px;
  font-style: italic;
  animation: fadeIn 0.3s var(--dt-ease-out);
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

:global(.dark) {
  .bubble {
    background: #1e293b; color: #f1f5f9; border-color: #334155;
    &.is-own { background: #1d4ed8; color: #fff; }
    .bubble-reply-ref { background: rgba(255, 255, 255, 0.05); color: #94a3b8; .ref-user { color: #f1f5f9; } }
  }
  .msg-file { background: #0f172a; border-color: #334155; .file-name { color: #f1f5f9; } }
}
</style>
