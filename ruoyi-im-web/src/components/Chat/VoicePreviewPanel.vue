<template>
  <div class="voice-preview-container">
    <div class="voice-preview-box">
      <div class="voice-info">
        <span class="material-icons-outlined voice-icon">mic</span>
        <span class="voice-duration">{{ formattedDuration }}</span>
      </div>
      <div class="voice-waveform">
        <span
          v-for="index in 20"
          :key="index"
          class="wave-bar"
          :class="{ active: isPlaying && index < activeBars }"
        ></span>
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
import { computed } from 'vue'

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

defineEmits(['toggle-play', 'delete', 'send'])

// 格式化语音时长
const formattedDuration = computed(() => {
  const seconds = Math.floor(props.duration / 1000)
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
})

// 计算激活的波形条数量
const activeBars = computed(() => {
  return Math.floor((props.playProgress / 100) * 20)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.voice-preview-container {
  padding: 10px 12px;
  margin-bottom: 12px;
  border-radius: var(--dt-radius-md);
  background: rgba(22, 119, 255, 0.05);
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
  gap: 8px;
  font-size: 13px;
  color: var(--dt-text-secondary);

  .voice-icon {
    font-size: 18px;
    color: var(--dt-brand-color);
  }

  .voice-duration {
    font-weight: 500;
    font-variant-numeric: tabular-nums;
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
