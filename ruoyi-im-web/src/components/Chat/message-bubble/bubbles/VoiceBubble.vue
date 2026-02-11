/**
 * 语音消息气泡组件
 * 支持播放/暂停、进度显示、波形可视化
 * 参考野火IM语音消息样式
 */
<template>
  <div
    class="voice-bubble"
    :class="{ 'is-playing': isPlaying }"
    @click="togglePlay"
  >
    <!-- 播放按钮 -->
    <div class="voice-play-btn">
      <el-icon :size="20">
        <component :is="isPlaying ? 'VideoPause' : 'VideoPlay'" />
      </el-icon>
    </div>

    <!-- 音频波形可视化 -->
    <div class="voice-waveform">
      <div class="waveform-bars">
        <span
          v-for="(height, index) in waveformHeights"
          :key="index"
          class="wave-bar"
          :class="{ 'active': isPlaying && index < activeBarIndex }"
          :style="{ height: height + '%' }"
        />
      </div>
    </div>

    <!-- 播放时长 -->
    <div class="voice-duration">
      {{ formattedDuration }}
    </div>

    <!-- 未读标记 -->
    <div
      v-if="!isRead && !isPlaying"
      class="voice-unread-badge"
    >
      <span
        class="material-icons-outlined"
        style="font-size: 12px;"
      >fiber_manual_record</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { VideoPlay, VideoPause } from '@element-plus/icons-vue'
import { addTokenToUrl } from '@/utils/file'

const props = defineProps({
  message: {
    type: Object,
    required: true
  },
  isRead: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['play', 'pause', 'read'])

// 播放状态
const isPlaying = ref(false)
const audio = ref(null)
const currentTime = ref(0)
const duration = ref(0)
const activeBarIndex = ref(0)

// 波形高度数组（模拟语音波形）
const waveformHeights = ref([])

// 初始化波形（基于消息内容生成伪随机波形）
const initWaveform = () => {
  const seed = props.message.content?.length || props.message.id?.length || 10
  const heights = []
  for (let i = 0; i < 20; i++) {
    // 生成30-100之间的随机高度
    const height = 30 + ((seed + i * 17) % 70)
    heights.push(height)
  }
  waveformHeights.value = heights
}

// 格式化时长显示
const formattedDuration = computed(() => {
  const seconds = Math.floor(duration.value || 0)
  if (seconds === 0 && props.message.content) {
    // 从消息内容解析时长（格式：{"duration": 30000, "url": "..."}）
    try {
      const content = typeof props.message.content === 'string'
        ? JSON.parse(props.message.content)
        : props.message.content
      if (content.duration) {
        const d = Math.floor(content.duration / 1000)
        const m = Math.floor(d / 60)
        const s = d % 60
        return `${m}:${s.toString().padStart(2, '0')}`
      }
    } catch (e) {
      console.warn('解析语音时长失败:', e)
    }
  }
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
})

// 切换播放/暂停
const togglePlay = async () => {
  if (isPlaying.value) {
    pause()
  } else {
    play()
  }
}

// 播放语音
const play = async () => {
  if (!audio.value) {
    await initAudio()
  }

  if (audio.value) {
    try {
      await audio.value.play()
      isPlaying.value = true
      emit('play')
      // 标记为已读
      if (!props.isRead) {
        emit('read')
      }
    } catch (error) {
      console.error('播放语音失败:', error)
    }
  }
}

// 暂停播放
const pause = () => {
  if (audio.value) {
    audio.value.pause()
    isPlaying.value = false
    emit('pause')
  }
}

// 初始化音频对象
const initAudio = async () => {
  try {
    let audioUrl = ''

    // 从消息内容获取音频URL
    if (props.message.content) {
      const content = typeof props.message.content === 'string'
        ? JSON.parse(props.message.content)
        : props.message.content

      audioUrl = content.url || content.audioUrl || ''
    }

    if (!audioUrl) {
      console.warn('语音消息缺少音频URL')
      return
    }

    // 添加token
    audioUrl = addTokenToUrl(audioUrl)

    // 创建音频对象
    audio.value = new Audio(audioUrl)

    // 监听播放事件
    audio.value.addEventListener('loadedmetadata', handleLoadedMetadata)
    audio.value.addEventListener('timeupdate', handleTimeUpdate)
    audio.value.addEventListener('ended', handleEnded)
    audio.value.addEventListener('play', () => { isPlaying.value = true })
    audio.value.addEventListener('pause', () => { isPlaying.value = false })
  } catch (error) {
    console.error('初始化音频失败:', error)
  }
}

// 音频元数据加载完成
const handleLoadedMetadata = () => {
  if (audio.value) {
    duration.value = audio.value.duration
  }
}

// 播放进度更新
const handleTimeUpdate = () => {
  if (audio.value) {
    currentTime.value = audio.value.currentTime
    // 更新活动波形索引
    const progress = currentTime.value / duration.value
    activeBarIndex.value = Math.floor(progress * waveformHeights.value.length)
  }
}

// 播放结束
const handleEnded = () => {
  isPlaying.value = false
  currentTime.value = 0
  activeBarIndex.value = 0
}

// 组件挂载时初始化
onMounted(() => {
  initWaveform()
})

// 组件卸载时清理
onUnmounted(() => {
  if (audio.value) {
    audio.value.pause()
    audio.value.removeEventListener('loadedmetadata', handleLoadedMetadata)
    audio.value.removeEventListener('timeupdate', handleTimeUpdate)
    audio.value.removeEventListener('ended', handleEnded)
    audio.value = null
  }
})

// 监听消息变化，重新初始化
watch(() => props.message?.id, () => {
  if (audio.value) {
    audio.value.pause()
    audio.value = null
  }
  isPlaying.value = false
  currentTime.value = 0
  activeBarIndex.value = 0
  initWaveform()
})
</script>

<style lang="scss" scoped>
.voice-bubble {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--dt-bg-card);
  border: none;
  border-radius: var(--dt-radius-lg);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  min-width: 160px;
  max-width: 320px;
  transition: all var(--dt-transition-base);

  &:hover {
    background-color: var(--dt-black-04);
  }

  &.is-playing {
    background-color: var(--dt-brand-bg);
    box-shadow: 0 0 0 1px var(--dt-brand-color), 0 1px 2px rgba(0, 0, 0, 0.06);
    transition: background-color var(--dt-transition-base);

    .voice-play-btn {
      color: var(--dt-brand-color);
    }

    .wave-bar.active {
      background-color: var(--dt-brand-color);
    }
  }
}

.voice-play-btn {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: var(--dt-black-08);
  transition: all var(--dt-transition-base);
}

.voice-waveform {
  flex: 1;
  height: 32px;
  display: flex;
  align-items: center;
}

.waveform-bars {
  display: flex;
  align-items: center;
  gap: 2px;
  width: 100%;
}

.wave-bar {
  flex: 1;
  min-width: 3px;
  max-width: 4px;
  height: 30%;
  background-color: var(--dt-black-20);
  border-radius: 2px;
  transition: height var(--dt-transition-fast), background-color var(--dt-transition-base);

  &.active {
    background-color: var(--dt-brand-color);
  }
}

.voice-duration {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  min-width: 40px;
  text-align: right;
}

.voice-unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 16px;
  height: 16px;
  background-color: var(--el-color-danger);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

// 气泡在不同位置时的样式调整
.text-bubble,
.message-bubble {
  .voice-bubble {
    // 在左侧气泡（接收消息）中
  }
}

// 深色主题适配
.dark {
  .voice-bubble {
    box-shadow: none;
    border: 1px solid var(--dt-border-dark, #3F424A);

    &:hover {
      background-color: var(--dt-white-10);
    }

    &.is-playing {
      border-color: var(--dt-brand-color);
      box-shadow: none;
    }

    .voice-play-btn {
      background-color: var(--dt-white-10);
    }

    .wave-bar {
      background-color: var(--dt-white-20);
    }
  }
}
</style>
