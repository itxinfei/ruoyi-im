<template>
  <div class="voice-message" :class="{ playing: isPlaying }" @click="togglePlay">
    <!-- 播放按钮 -->
    <div class="voice-play-btn">
      <el-icon>
        <component :is="isPlaying ? VideoPause : VideoPlay" />
      </el-icon>
    </div>

    <!-- 波形动画 -->
    <div class="voice-wave">
      <div
        v-for="i in 20"
        :key="i"
        class="wave-bar"
        :class="{ active: isPlaying }"
        :style="{ animationDelay: `${i * 0.05}s` }"
      ></div>
    </div>

    <!-- 时长 -->
    <div class="voice-duration">{{ formattedDuration }}</div>

    <!-- 音频元素 -->
    <audio
      ref="audioRef"
      :src="content.url"
      @ended="handleEnded"
      @timeupdate="handleTimeUpdate"
    ></audio>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { VideoPlay, VideoPause } from '@element-plus/icons-vue'

const props = defineProps({
  content: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['play', 'pause', 'end'])

const audioRef = ref(null)
const isPlaying = ref(false)
const currentTime = ref(0)

// 格式化时长
const formattedDuration = computed(() => {
  const duration = props.content.duration || 0
  const seconds = Math.floor(currentTime.value)
  const total = Math.floor(duration)
  return `${formatTime(seconds)} / ${formatTime(total)}`
})

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 切换播放状态
const togglePlay = () => {
  if (!audioRef.value) return

  if (isPlaying.value) {
    audioRef.value.pause()
  } else {
    audioRef.value.play()
  }
}

// 播放结束
const handleEnded = () => {
  isPlaying.value = false
  currentTime.value = 0
  emit('end')
}

// 时间更新
const handleTimeUpdate = () => {
  if (audioRef.value) {
    currentTime.value = audioRef.value.currentTime
  }
}

// 组件卸载时停止播放
onUnmounted(() => {
  if (audioRef.value && isPlaying.value) {
    audioRef.value.pause()
  }
})

// 暴露方法给父组件
const play = () => {
  if (audioRef.value) {
    audioRef.value.play()
  }
}

const pause = () => {
  if (audioRef.value) {
    audioRef.value.pause()
  }
}

defineExpose({ play, pause })
</script>

<style lang="scss" scoped>
.voice-message {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 160px;
  max-width: 240px;
  padding: 10px 14px;
  background: #fff;
  border: 1px solid #E5E8EB;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #FAFAFA;
  }

  &.playing {
    .voice-wave .wave-bar.active {
      animation: wave 1s ease-in-out infinite;
    }
  }

  .voice-play-btn {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #0089FF;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .el-icon {
      font-size: 14px;
    }
  }

  .voice-wave {
    flex: 1;
    height: 24px;
    display: flex;
    align-items: center;
    gap: 2px;

    .wave-bar {
      width: 2px;
      height: 100%;
      background: #D9D9D9;
      border-radius: 1px;
      transition: background 0.2s;

      &.active {
        background: #0089FF;
      }
    }
  }

  .voice-duration {
    font-size: 12px;
    color: #858B8F;
    flex-shrink: 0;
  }
}

@keyframes wave {
  0%, 100% {
    height: 6px;
  }
  50% {
    height: 18px;
  }
}
</style>
