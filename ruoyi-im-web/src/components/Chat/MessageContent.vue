<!-- 通用消息内容组件 -->
<template>
  <div class="message-content-wrapper">
    <!-- 文本消息 -->
    <div
      v-if="isTextMessage"
      class="text-content"
      v-html="formattedText"
    />

    <!-- 图片消息 -->
    <div
      v-else-if="isImageMessage"
      class="image-content"
    >
      <img
        :src="message.content"
        :alt="message.alt || '图片'"
        class="message-image"
        @click="handleImageClick"
      >
    </div>

    <!-- 文件消息 -->
    <div
      v-else-if="isFileMessage"
      class="file-content"
    >
      <div class="file-icon">
        <i class="el-icon-document" />
      </div>
      <div class="file-info">
        <div class="file-name">
          {{ message.fileName }}
        </div>
        <div class="file-size">
          {{ formatFileSize(message.fileSize) }}
        </div>
      </div>
      <button
        class="file-download-btn"
        @click="handleDownload"
      >
        下载
      </button>
    </div>

    <!-- 语音消息 -->
    <div
      v-else-if="isVoiceMessage"
      class="voice-content"
    >
      <div class="voice-player">
        <button
          class="play-btn"
          @click="togglePlayback"
        >
          <i
            v-if="!isPlaying"
            class="el-icon-video-play"
          />
          <i
            v-else
            class="el-icon-video-pause"
          />
        </button>
        <div class="voice-progress">
          <div
            class="progress-bar"
            :style="{ width: playbackProgress + '%' }"
          />
        </div>
        <span class="voice-duration">{{ formatDuration(message.duration) }}</span>
      </div>
    </div>

    <!-- 系统消息 -->
    <div
      v-else-if="isSystemMessage"
      class="system-content"
    >
      {{ message.content }}
    </div>

    <!-- 撤回消息 -->
    <div
      v-else-if="isRecalledMessage"
      class="recalled-content"
    >
      <i class="el-icon-delete" />
      <span>此消息已被撤回</span>
    </div>

    <!-- 未知消息类型 -->
    <div
      v-else
      class="unknown-content"
    >
      <i class="el-icon-question" />
      <span>不支持的消息类型</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

// 计算消息类型
const isTextMessage = computed(() => {
  return props.message.type?.toUpperCase() === 'TEXT'
})

const isImageMessage = computed(() => {
  return props.message.type?.toUpperCase() === 'IMAGE'
})

const isFileMessage = computed(() => {
  return props.message.type?.toUpperCase() === 'FILE'
})

const isVoiceMessage = computed(() => {
  return ['VOICE', 'AUDIO'].includes(props.message.type?.toUpperCase())
})

const isSystemMessage = computed(() => {
  return props.message.type?.toUpperCase() === 'SYSTEM'
})

const isRecalledMessage = computed(() => {
  return props.message.isRevoked || props.message.type?.toUpperCase() === 'RECALLED'
})

// 格式化文本内容（处理换行和链接）
const formattedText = computed(() => {
  if (!props.message.content) {return ''}
  // 简单的换行处理
  const content = props.message.content.replace(/\n/g, '<br/>')
  // 可以扩展链接识别和点击事件
  return content
})

// 本地状态
const isPlaying = ref(false)
const playbackProgress = ref(0)

// 方法
const handleImageClick = event => {
  // 触发查看大图事件
  emit('preview-image', props.message.content)
}

const handleDownload = () => {
  // 触发文件下载事件
  emit('download-file', props.message)
}

const togglePlayback = () => {
  isPlaying.value = !isPlaying.value
  if (isPlaying.value) {
    emit('start-playback', props.message)
  } else {
    emit('pause-playback', props.message)
  }
}

const formatFileSize = bytes => {
  if (bytes === undefined || bytes === null) {return ''}
  if (bytes < 1024) {return bytes + ' B'}
  else if (bytes < 1048576) {return (bytes / 1024).toFixed(1) + ' KB'}
  else {return (bytes / 1048576).toFixed(1) + ' MB'}
}

const formatDuration = milliseconds => {
  if (!milliseconds) {return '0:00'}
  const seconds = Math.floor(milliseconds / 1000)
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs < 10 ? '0' : ''}${secs}`
}

// 事件发射器
const emit = defineEmits([
  'preview-image',
  'download-file',
  'start-playback',
  'pause-playback'
])
</script>

<style scoped>
.message-content-wrapper {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.text-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
  color: var(--dt-text-primary);
}

.image-content {
  display: flex;
  justify-content: center;
  align-items: center;
}

.message-image {
  max-width: 100%;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.message-image:hover {
  transform: scale(1.03);
}

.file-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  background: var(--dt-bg-subtle);
  border-radius: 6px;
}

.file-icon {
  font-size: 24px;
  color: var(--dt-text-tertiary);
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  color: var(--dt-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.file-download-btn {
  padding: 6px 12px;
  background: var(--dt-brand-color);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.file-download-btn:hover {
  background: var(--dt-brand-hover);
}

.voice-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.voice-player {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  background: var(--dt-bg-subtle);
  border-radius: 16px;
}

.play-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--dt-brand-color);
  padding: 4px;
  border-radius: 50%;
}

.play-btn:hover {
  background: var(--dt-brand-bg);
}

.voice-progress {
  flex: 1;
  height: 4px;
  background: var(--dt-bg-hover);
  border-radius: 2px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: var(--dt-brand-color);
  transition: width 0.1s;
}

.voice-duration {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  min-width: 30px;
}

.system-content,
.recalled-content,
.unknown-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 12px;
  background: var(--dt-bg-subtle);
  border-radius: 16px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
  text-align: center;
  width: fit-content;
  margin: 0 auto;
}

.recalled-content {
  color: var(--dt-text-quaternary);
}

.unknown-content {
  color: var(--dt-warning-color);
  background: var(--dt-warning-bg);
}

/* 暗色模式 */
:global(.dark) {
  .text-content {
    color: var(--dt-text-primary-dark);
  }

  .file-content {
    background: var(--dt-bg-subtle-dark);
  }

  .file-name {
    color: var(--dt-text-primary-dark);
  }

  .file-size {
    color: var(--dt-text-tertiary-dark);
  }

  .voice-player {
    background: var(--dt-bg-subtle-dark);
  }

  .voice-progress {
    background: var(--dt-bg-hover-dark);
  }

  .system-content,
  .recalled-content,
  .unknown-content {
    background: var(--dt-bg-subtle-dark);
  }

  .system-content,
  .recalled-content {
    color: var(--dt-text-tertiary-dark);
  }

  .unknown-content {
    background: var(--dt-warning-bg-dark);
  }
}
</style>