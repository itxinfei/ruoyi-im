<template>
  <div class="voice-preview-container">
    <div class="voice-preview-box">
      <div class="voice-info">
        <span class="material-icons-outlined voice-icon">mic</span>
        <div class="voice-time-info">
          <span class="voice-duration">{{ formattedDuration }}</span>
          <span class="voice-current-time">{{ formattedCurrentTime }}</span>
        </div>
      </div>
      <div class="voice-waveform">
        <span
          v-for="index in 20"
          :key="index"
          class="wave-bar"
          :class="{ active: isPlaying && index < activeBars }"
        ></span>
      </div>
      <!-- 播放进度条 -->
      <div class="voice-progress-container">
        <div class="voice-progress-bar" @click="handleProgressClick">
          <div class="voice-progress-fill" :style="{ width: `${playProgress}%` }"></div>
          <div class="voice-progress-handle" :style="{ left: `${playProgress}%` }" @mousedown="handleProgressDragStart"></div>
        </div>
      </div>
      <div class="voice-actions">
        <button class="voice-action-btn play-btn" @click="$emit('toggle-play')">
          <span class="material-icons-outlined">
            {{ isPlaying ? 'pause' : 'play_arrow' }}
          </span>
        </button>
        <button class="voice-action-btn delete-btn" @click="$emit('delete')">
          <span class="material-icons-outlined">delete</span>
          删除
        </button>
        <button class="voice-action-btn send-btn" @click="$emit('send')">
          <span class="material-icons-outlined">send</span>
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { formatDurationMMSS } from '@/utils/format'

const props = defineProps({
  duration: {
    type: Number,
    default: 0
  },
  isPlaying: {
    type: Boolean,
    default: false
  },
  playProgress: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['toggle-play', 'delete', 'send', 'seek'])

// 使用共享工具函数格式化语音时长（输入是毫秒，需要转换为秒）
const formattedDuration = computed(() => formatDurationMMSS(props.duration / 1000))

// 格式化当前播放时间
const formattedCurrentTime = computed(() => {
  const currentSeconds = (props.playProgress / 100) * props.duration / 1000
  return formatDurationMMSS(currentSeconds)
})

// 计算激活的波形条数量
const activeBars = computed(() => {
  return Math.floor((props.playProgress / 100) * 20)
})

// 处理进度条点击
const handleProgressClick = (event) => {
  const progressBar = event.currentTarget
  const rect = progressBar.getBoundingClientRect()
  const clickX = event.clientX - rect.left
  const progress = (clickX / rect.width) * 100
  emit('seek', Math.max(0, Math.min(100, progress)))
}

// 处理进度条拖拽开始
const handleProgressDragStart = (event) => {
  event.preventDefault()
  const progressBar = event.currentTarget.parentElement
  const rect = progressBar.getBoundingClientRect()
  
  const handleMouseMove = (moveEvent) => {
    const moveX = moveEvent.clientX - rect.left
    const progress = (moveX / rect.width) * 100
    emit('seek', Math.max(0, Math.min(100, progress)))
  }
  
  const handleMouseUp = () => {
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }
  
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.voice-preview-container {
  padding: 10px 12px;
  margin-bottom: 12px;
  border-radius: var(--dt-radius-md);
  background: rgba(0, 137, 255, 0.05);
  border-left: 3px solid var(--dt-brand-color);
}

.voice-preview-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.voice-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--dt-text-secondary);

  .voice-icon {
    font-size: 18px;
    color: var(--dt-brand-color);
  }

  .voice-time-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .voice-duration {
      font-weight: 500;
      font-variant-numeric: tabular-nums;
      color: var(--dt-text-tertiary);
    }

    .voice-current-time {
      font-weight: 500;
      font-variant-numeric: tabular-nums;
      color: var(--dt-brand-color);
    }
  }
}

.voice-waveform {
  display: flex;
  gap: 2px;
  align-items: center;
  height: 24px;
  padding: 8px 0;

  .wave-bar {
    width: 3px;
    height: 8px;
    background: #d1d5db;
    border-radius: 2px;
    transition: all 0.2s;

    &.active {
      background: var(--dt-brand-color);
      height: 16px;
    }
  }
}

.voice-progress-container {
  margin: 4px 0;

  .voice-progress-bar {
    position: relative;
    width: 100%;
    height: 6px;
    background: #e2e8f0;
    border-radius: 3px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #cbd5e1;
    }

    .voice-progress-fill {
      position: absolute;
      top: 0;
      left: 0;
      height: 100%;
      background: var(--dt-brand-color);
      border-radius: 3px;
      transition: width 0.1s ease;
    }

    .voice-progress-handle {
      position: absolute;
      top: 50%;
      transform: translate(-50%, -50%);
      width: 12px;
      height: 12px;
      background: #fff;
      border: 2px solid var(--dt-brand-color);
      border-radius: 50%;
      cursor: pointer;
      transition: all 0.2s;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

      &:hover {
        transform: translate(-50%, -50%) scale(1.1);
        box-shadow: 0 3px 6px rgba(0, 0, 0, 0.15);
      }
    }
  }
}

.voice-actions {
  display: flex;
  gap: 8px;

  .voice-action-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 12px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 13px;
    transition: all 0.2s;

    .material-icons-outlined { font-size: 16px; }

    &.play-btn {
      background: var(--dt-brand-color);
      color: #fff;

      &:hover { opacity: 0.9; }
    }

    &.delete-btn {
      background: #f2f3f5;
      color: #646a73;

      &:hover {
        background: #ff4d4f;
        color: #fff;
      }
    }

    &.send-btn {
      background: var(--dt-brand-color);
      color: #fff;

      &:hover { opacity: 0.9; }
    }
  }
}

// 暗色模式
:global(.dark) {
  .voice-waveform .wave-bar {
    background: #4b5563;

    &.active {
      background: var(--dt-brand-color);
    }
  }

  .voice-progress-container .voice-progress-bar {
    background: #334155;

    &:hover {
      background: #475569;
    }

    .voice-progress-handle {
      background: #1e293b;
      border-color: var(--dt-brand-color);
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);

      &:hover {
        box-shadow: 0 3px 6px rgba(0, 0, 0, 0.4);
      }
    }
  }

  .voice-actions .voice-action-btn.delete-btn {
    background: rgba(255, 255, 255, 0.1);
    color: #9ca3af;

    &:hover {
      background: #ff4d4f;
      color: #fff;
    }
  }
}
</style>
