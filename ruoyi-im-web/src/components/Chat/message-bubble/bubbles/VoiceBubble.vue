<template>
  <div class="voice-bubble" :class="{ 'is-playing': isPlaying }" @click="togglePlay">
    <!-- 播放按钮 -->
    <div class="voice-icon">
      <el-icon>
        <VideoPause v-if="isPlaying" />
        <VideoPlay v-else />
      </el-icon>
    </div>

    <!-- 音频波形 -->
    <div class="voice-waveform">
      <span
        v-for="i in 20"
        :key="i"
        class="wave-bar"
        :class="{ active: isPlaying && progress > (i / 20) }"
      ></span>
    </div>

    <!-- 时长显示 -->
    <span class="voice-duration">{{ formattedDuration }}</span>

    <!-- 隐藏的音频元素 -->
    <audio
      v-if="voiceUrl"
      :src="voiceUrl"
      ref="audioRef"
      @ended="onEnded"
      @timeupdate="onTimeUpdate"
    ></audio>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { VideoPlay, VideoPause } from '@element-plus/icons-vue'
import { parseMessageContent } from '@/utils/message'

const props = defineProps({
  message: { type: Object, required: true }
})

const audioRef = ref(null)
const isPlaying = ref(false)
const progress = ref(0)

const parsedContent = computed(() => parseMessageContent(props.message) || {})

const voiceUrl = computed(() => {
  return parsedContent.value.voiceUrl || parsedContent.value.url || ''
})

const duration = computed(() => {
  return parsedContent.value.duration || 0
})

const formattedDuration = computed(() => {
  if (!duration.value) return '0:00'
  const mins = Math.floor(duration.value / 60)
  const secs = Math.floor(duration.value % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
})

const togglePlay = () => {
  if (!audioRef.value) return

  if (isPlaying.value) {
    audioRef.value.pause()
  } else {
    // 停止其他正在播放的语音
    document.querySelectorAll('audio.voice-audio').forEach(audio => {
      if (audio !== audioRef.value) {
        audio.pause()
        audio.currentTime = 0
      }
    })
    audioRef.value.play()
  }
  isPlaying.value = !isPlaying.value
}

const onEnded = () => {
  isPlaying.value = false
  progress.value = 0
}

const onTimeUpdate = () => {
  if (!audioRef.value) return
  progress.value = audioRef.value.currentTime / audioRef.value.duration
}

onUnmounted(() => {
  if (audioRef.value) {
    audioRef.value.pause()
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.voice-bubble {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 160px;
  max-width: 280px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    transform: scale(1.02);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  }

  &.is-playing .voice-icon {
    animation: pulse 1s ease-in-out infinite;
  }
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
  transition: transform var(--dt-transition-fast);

  .voice-bubble:hover & {
    transform: scale(1.1);
  }

  .el-icon {
    font-size: 18px;
  }
}

.voice-waveform {
  display: flex;
  gap: 2px;
  align-items: center;
  flex: 1;
  height: 20px;
}

.wave-bar {
  width: 3px;
  height: 6px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 2px;
  transition: all 0.15s var(--dt-ease-out);

  &.active {
    background: rgba(255, 255, 255, 0.9);
    height: 14px;
    animation: waveAnim 0.3s ease-in-out infinite alternate;
  }
}

.voice-duration {
  font-size: var(--dt-font-size-sm);
  color: rgba(255, 255, 255, 0.9);
  font-variant-numeric: tabular-nums;
}

@keyframes waveAnim {
  from { transform: scaleY(0.8); }
  to { transform: scaleY(1.2); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}
</style>
