<template>
  <div class="voice-message" :class="{ 'is-own': message.isOwn, 'playing': isPlaying }">
    <!-- 播放按钮 -->
    <button class="play-btn" @click="togglePlay">
      <span v-if="!isPlaying" class="material-icons-outlined">play_arrow</span>
      <span v-else class="material-icons-outlined">pause</span>
    </button>

    <!-- 动态波形图 -->
    <div ref="waveformRef" class="waveform-container">
      <div
        v-for="i in 24"
        :key="i"
        class="waveform-bar"
        :style="{
          height: getBarHeight(i) + '%',
          opacity: progress >= (i/24)*100 ? 1 : 0.4
        }"
      />
    </div>

    <!-- 时长 -->
    <span class="duration">{{ formatDuration(duration) }}</span>

    <!-- 播放进度 -->
    <div class="progress-bar" @click="seekTo">
      <div class="progress-fill" :style="{ width: progress + '%' }" />
    </div>

    <!-- 发送状态 -->
    <span v-if="message.isOwn && message.status === 'sending'" class="sending-icon">
      <el-icon class="is-loading"><Loading /></el-icon>
    </span>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['command'])

// 播放状态
const isPlaying = ref(false)
const progress = ref(0)
const duration = ref(0)
const audioRef = ref(null)
const waveformRef = ref(null)

// 解析内容
const parsedContent = computed(() => {
  try {
    if (!props.message?.content) return { duration: 0, url: '' }
    const isMedia = ['VOICE', 'AUDIO'].includes(props.message.type)
    return (typeof props.message.content === 'string' && isMedia)
      ? JSON.parse(props.message.content)
      : (props.message.content || {})
  } catch (e) {
    return { duration: 0, url: '' }
  }
})

// 获取音频时长
onMounted(() => {
  if (parsedContent.value.duration) {
    duration.value = parsedContent.value.duration
  }

  // 创建音频对象
  if (parsedContent.value.url) {
    audioRef.value = new Audio(parsedContent.value.url)

    audioRef.value.addEventListener('loadedmetadata', () => {
      duration.value = audioRef.value.duration
    })

    audioRef.value.addEventListener('timeupdate', () => {
      if (audioRef.value.duration) {
        progress.value = (audioRef.value.currentTime / audioRef.value.duration) * 100
      }
    })

    audioRef.value.addEventListener('ended', () => {
      isPlaying.value = false
      progress.value = 0
    })
  }
})

onUnmounted(() => {
  if (audioRef.value) {
    audioRef.value.pause()
    audioRef.value = null
  }
})

// 切换播放/暂停
const togglePlay = () => {
  if (!audioRef.value) {
    emit('command', 'play-voice', props.message)
    return
  }

  if (isPlaying.value) {
    audioRef.value.pause()
  } else {
    audioRef.value.play()
  }
  isPlaying.value = !isPlaying.value
}

// 跳转到指定位置
const seekTo = (e) => {
  if (!audioRef.value || !duration.value) return

  const rect = e.target.getBoundingClientRect()
  const x = e.clientX - rect.left
  const percent = x / rect.width
  audioRef.value.currentTime = percent * duration.value
}

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '0:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// 生成波形条高度 (模拟波形)
const getBarHeight = (index) => {
  // 使用伪随机生成波形
  const seed = index * 7 + 13
  return 20 + Math.sin(seed) * 30 + Math.cos(seed * 0.7) * 25 + 25
}
</script>

<style scoped lang="scss">
@use "sass:math";

.voice-message {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md, 10px);
  padding: var(--dt-spacing-sm) var(--dt-spacing-md);
  min-width: var(--dt-voice-bubble-min-width, 180px);
  max-width: var(--dt-voice-bubble-max-width, 280px);
  position: relative;

  // 播放按钮
  .play-btn {
    width: var(--dt-btn-size-lg, 36px);
    height: var(--dt-btn-size-lg, 36px);
    border-radius: 50%;
    border: none;
    background: var(--dt-brand-color);
    color: var(--dt-bg-card);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    transition: all var(--dt-transition-fast);

    .material-icons-outlined {
      font-size: var(--dt-font-size-md, 22px);
    }

    &:hover {
      transform: scale(1.08);
      box-shadow: 0 2px 8px var(--dt-shadow-brand);
    }

    &:active {
      transform: scale(0.95);
    }
  }

  // 波形图容器
  .waveform-container {
    flex: 1;
    height: var(--dt-icon-size-lg, 30px);
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-xs, 2px);
    overflow: hidden;

    .waveform-bar {
      width: var(--dt-spacing-xs, 2px);
      background: currentColor;
      border-radius: var(--dt-radius-sm, 1px);
      transition: height var(--dt-transition-base) cubic-bezier(0.4, 0, 0.2, 1), opacity var(--dt-transition-base);
    }
  }

  // 播放时波形动画 (钉钉风格：非对称随机跳动)
  &.playing .waveform-bar {
    animation: waveform-jump 0.6s ease-in-out infinite alternate;

    @for $i from 1 through 24 {
      &:nth-child(#{$i}) {
        animation-delay: #{$i * 0.03}s;
        animation-duration: #{0.4 + math.div(random(4), 10)}s;
      }
    }
  }

  @keyframes waveform-jump {
    from { transform: scaleY(0.5); }
    to { transform: scaleY(1.2); }
  }

  // 时长
  .duration {
    font-size: var(--dt-font-size-xs, 12px);
    color: var(--dt-text-tertiary);
    min-width: var(--dt-voice-duration-min-width, 35px);
    text-align: right;
  }

  // 进度条
  .progress-bar {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: var(--dt-spacing-xs, 3px);
    background: var(--dt-border-light);
    cursor: pointer;
    border-radius: 0 0 var(--dt-radius-md) var(--dt-radius-md);
    overflow: hidden;

    .progress-fill {
      height: 100%;
      background: var(--dt-brand-color);
      transition: width 0.1s linear;
    }
  }

  // 发送中图标
  .sending-icon {
    position: absolute;
    right: calc(-1 * var(--dt-spacing-md, 24px));
    bottom: var(--dt-spacing-sm, 8px);
    font-size: var(--dt-font-size-xs, 12px);
    color: var(--dt-text-tertiary);
  }

  // 自己的消息样式
  &.is-own {
    flex-direction: row-reverse;

    .play-btn {
      background: var(--dt-bg-card);
      color: var(--dt-brand-color);
    }

    .waveform-bar {
      background: rgba(255, 255, 255, 0.7);
    }

    .duration {
      text-align: left;
    }

    .progress-bar {
      .progress-fill {
        background: var(--dt-bg-card);
      }
    }
  }
}

// 暗色模式
.dark .voice-message {
  .waveform-bar {
    background: rgba(255, 255, 255, 0.5);
  }

  &.is-own .waveform-bar {
    background: rgba(255, 255, 255, 0.7);
  }
}
</style>
