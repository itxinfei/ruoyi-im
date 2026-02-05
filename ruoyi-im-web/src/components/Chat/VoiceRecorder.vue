<template>
  <!-- 按住说话模式 -->
  <div
    class="voice-recorder-wrapper"
    :class="{ 'is-recording': isRecording }"
    @mousedown="handleStartRecord"
    @touchstart.prevent="handleTouchStart"
    @mouseup="handleEndRecord"
    @touchend.prevent="handleTouchEnd"
    @mouseleave="handleCancel"
  >
    <!-- 录音引导 -->
    <div
      v-if="!isRecording"
      class="voice-guide"
    >
      <span class="material-icons-outlined mic-icon">mic</span>
      <span class="guide-text">{{ touchStarted ? '松开 发送' : '按住 说话' }}</span>
    </div>

    <!-- 录音中状态 -->
    <div
      v-else
      class="recording-state"
    >
      <!-- 简化的录音动画波形 -->
      <div class="recording-waveform">
        <span
          v-for="index in 5"
          :key="index"
          class="wave-bar"
          :style="{ animationDelay: `${index * 0.1}s` }"
        />
      </div>

      <!-- 录音时长 -->
      <div class="recording-time">
        {{ formattedTime }}
      </div>

      <!-- 取消按钮 -->
      <div
        class="cancel-btn"
        @click.stop="handleCancel"
      >
        <span class="material-icons-outlined">cancel</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  maxLength: { type: Number, default: 60000 },
  minLength: { type: Number, default: 1000 }
})

const emit = defineEmits(['record-complete', 'cancel'])

const isRecording = ref(false)
const recordingTime = ref(0)
const touchStarted = ref(false)
const mediaRecorder = ref(null)
const audioChunks = ref([])
const startTime = ref(0)
const timer = ref(null)

const formattedTime = computed(() => {
  const seconds = Math.floor(recordingTime.value / 1000)
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
})

const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })

    mediaRecorder.value = new MediaRecorder(stream)
    audioChunks.value = []

    mediaRecorder.value.ondataavailable = e => {
      audioChunks.value.push(e.data)
    }

    mediaRecorder.value.onstop = () => {
      const audioBlob = new Blob(audioChunks.value, { type: 'audio/webm' })
      stream.getTracks().forEach(track => track.stop())

      const duration = Date.now() - startTime.value
      if (duration >= props.minLength && duration <= props.maxLength) {
        emit('record-complete', {
          blob: audioBlob,
          duration: duration
        })
      } else {
        // 录音过短或过长，触发 cancel 事件让父组件重置状态
        if (duration < props.minLength) {
          ElMessage.warning(`录音时长太短，最少需要${props.minLength / 1000}秒`)
        } else {
          ElMessage.warning(`录音时长不能超过${props.maxLength / 1000}秒`)
        }
        emit('cancel')
      }
    }

    mediaRecorder.value.start()
    startTime.value = Date.now()
    isRecording.value = true

    timer.value = setInterval(() => {
      recordingTime.value = Date.now() - startTime.value
      if (recordingTime.value >= props.maxLength) {
        stopRecording()
      }
    }, 100)

  } catch (e) {
    console.error('录音失败', e)
    ElMessage.error('无法访问麦克风，请检查权限设置')
    isRecording.value = false
  }
}

const stopRecording = () => {
  if (mediaRecorder.value && mediaRecorder.value.state !== 'inactive') {
    mediaRecorder.value.stop()
  }
  clearInterval(timer.value)
  isRecording.value = false
  recordingTime.value = 0
}

const handleStartRecord = () => {
  touchStarted.value = false
  startRecording()
}

const handleTouchStart = () => {
  touchStarted.value = true
  startRecording()
}

const handleEndRecord = () => {
  if (!touchStarted.value) {
    stopRecording()
  }
}

const handleTouchEnd = () => {
  stopRecording()
}

const handleCancel = () => {
  if (isRecording.value) {
    stopRecording()
    // 始终触发 cancel 事件，让父组件重置 isVoiceMode
    emit('cancel')
  }
}

onUnmounted(() => {
  stopRecording()
})
</script>

<style scoped lang="scss">
.voice-recorder-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: #fff;
  border-radius: var(--dt-radius-md);
  user-select: none;
  touch-action: none;
  cursor: pointer;
  transition: all 0.15s ease;
  border: 1px solid #e8e8e8;

  &:hover {
    background: #fafafa;
  }

  &.is-recording {
    background: #fff1f0;
    border-color: #ffccc7;
    cursor: default;
  }

  .dark & {
    background: #2a2a2a;
    border-color: rgba(255, 255, 255, 0.1);

    &:hover { background: #333; }
    &.is-recording {
      background: rgba(255, 77, 79, 0.1);
      border-color: rgba(255, 77, 79, 0.3);
    }
  }

  .voice-guide {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #666;

    .mic-icon {
      font-size: 28px;
      color: #1890ff;
    }

    .guide-text {
      font-size: 13px;
    }
  }
}

.recording-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.recording-waveform {
  display: flex;
  gap: 4px;
  align-items: center;
  justify-content: center;
  height: 32px;

  .wave-bar {
    width: 4px;
    background: #ff4d4f;
    border-radius: var(--dt-radius-sm);
    animation: recordingWave 0.6s ease-in-out infinite;
    height: 8px;

    @keyframes recordingWave {
      0%, 100% {
        height: 8px;
      }
      50% {
        height: 24px;
      }
    }
  }
}

.recording-time {
  font-size: 16px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  color: #ff4d4f;
}

.cancel-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f5f5f5;
  cursor: pointer;
  transition: all 0.15s ease;

  &:hover {
    background: #ffe6e6;

    .material-icons-outlined {
      color: #ff4d4f;
    }
  }

  .material-icons-outlined {
    font-size: 18px;
    color: #999;
  }

  .dark & {
    background: rgba(255, 255, 255, 0.08);

    &:hover { background: rgba(255, 77, 79, 0.2); }
  }
}
</style>
