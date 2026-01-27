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
      @touchstart="handleTouchStart"
      @touchend="handleTouchEnd"
      @touchcancel="handleTouchEnd"
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

      <!-- 图片消息 - 增强全屏预览 -->
      <viewer v-else-if="message.type === 'IMAGE' && parsedContent.imageUrl"
              :images="[parsedContent.imageUrl]"
              :options="viewerOptions"
              class="image-viewer">
        <img :src="parsedContent.imageUrl"
             class="msg-image"
             :alt="message.senderName + '的图片'"
             loading="lazy" />
      </viewer>

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

    <!-- 右键菜单：精品化菜单项 -->
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
  } else {
    // 普通点击：不做处理，让父组件处理
    emit('command', 'click', props.message)
  }
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

const parsedContent = computed(() => {
  try {
    if (!props.message || !props.message.content) return {}
    const isMedia = ['IMAGE', 'FILE', 'VIDEO', 'VOICE', 'AUDIO', 'COMBINE', 'COMBINE_FORWARD'].includes(props.message.type)
    return (typeof props.message.content === 'string' && isMedia)
      ? JSON.parse(props.message.content)
      : (props.message.content || {})
  } catch (e) { return {} }
})

// v-viewer 图片预览配置 - 支持缩放、旋转、下载、键盘快捷键
const viewerOptions = {
  // 内联查看模式
  inline: false,
  // 按钮显示
  button: true,
  // 导航栏
  navbar: true,
  // 工具栏
  toolbar: {
    zoomIn: 1,          // 放大
    zoomOut: 1,         // 缩小
    oneToOne: 1,        // 1:1
    reset: 1,           // 重置
    prev: 1,            // 上一张
    play: false,        // 幻灯片播放 (单图不需要)
    next: 1,            // 下一张
    rotateLeft: 1,      // 左旋转
    rotateRight: 1,     // 右旋转
    flipHorizontal: 0,  // 水平翻转
    flipVertical: 0     // 垂直翻转
  },
  // 标题显示
  title: false,
  // 键盘快捷键
  keyboard: true,
  // 快捷键配置
  keyActions: {
    esc: 'close',           // ESC 关闭
    space: 'toggleZoom',    // 空格切换缩放
    left: 'prev',           // 左箭头上一张
    right: 'next',          // 右箭头下一张
    up: 'zoomIn',           // 上箭头放大
    down: 'zoomOut'         // 下箭头缩小
  },
  // 加载指示器
  loading: true,
  // 循环浏览
  loop: false,
  // 最小缩放比例
  minZoomRatio: 0.1,
  // 最大缩放比例
  maxZoomRatio: 10,
  // 鼠标滚轮缩放
  movable: true,
  // 缩放时可移动
  zoomable: true,
  // 可旋转
  rotatable: true,
  // 可翻转
  scalable: false,
  // 过渡动画
  transition: true,
  // 全屏模式
  fullscreen: true,
  // 双击切换缩放
  dblclick: 'toggleZoom'
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

// 组件卸载时停止播放
onUnmounted(() => {
  if (voiceAudioRef.value) {
    voiceAudioRef.value.pause()
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

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
    display: flex;
    flex-direction: column;
    background: rgba(0, 0, 0, 0.03);
    border-left: 3px solid #0089ff;
    padding: 8px 12px;
    margin-bottom: 8px;
    border-radius: 0 4px 4px 0;
    font-size: 12px;
    color: #64748b;
    cursor: pointer;
    overflow: hidden;
    user-select: none;
    transition: all 0.2s var(--dt-ease-out);

    @include hover-lift;

    .ref-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 4px;

      .ref-user {
        font-weight: 600;
        color: #1f2329;
        font-size: 12px;
      }

      .ref-type-icon {
        color: #0089ff;
        opacity: 0.7;
        transition: opacity 0.2s;
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

      .ref-image-text,
      .ref-file-text,
      .ref-video-text,
      .ref-voice-text {
        color: #0089ff;
        font-weight: 500;
      }
    }

    &:hover {
      background: rgba(0, 137, 255, 0.08);
      border-left-color: #0066cc;

      .ref-type-icon {
        opacity: 1;
      }
    }
  }

  .text-content-wrapper {
    display: flex; flex-direction: column;
    .main-text { white-space: pre-wrap; }
    .edited-tag { font-size: 11px; opacity: 0.5; margin-top: 2px; align-self: flex-end; }

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

:global(.dark) {
  .bubble {
    background: #1e293b; color: #f1f5f9; border-color: #334155;
    &.is-own { background: #1d4ed8; color: #fff; }
    .bubble-reply-ref { background: rgba(255, 255, 255, 0.05); color: #94a3b8; .ref-user { color: #f1f5f9; } }
  }
  .msg-file { background: #0f172a; border-color: #334155; .file-name { color: #f1f5f9; } }
}
</style>
