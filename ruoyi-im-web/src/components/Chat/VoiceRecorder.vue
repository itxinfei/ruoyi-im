<template>
  <!-- 按住说话模式 -->
  <div class="voice-recorder-wrapper" :class="{ 'is-recording': isRecording }" @mousedown="handleStartRecord"
    @mouseup="handleEndRecord" @mouseleave="handleCancel">
    <!-- 录音引导 -->
    <div v-if="!isRecording" class="voice-guide">
      <span class="material-icons-outlined mic-icon">mic</span>
      <span class="guide-text">按住 说话</span>
    </div>

    <!-- 录音中状态 -->
    <div v-else class="recording-state">
      <!-- 简化的录音动画波形 -->
      <div class="recording-waveform">
        <span v-for="index in 5" :key="index" class="wave-bar" :style="{ animationDelay: `${index * 0.1}s` }" />
      </div>

      <!-- 录音时长 -->
      <div class="recording-time">
        {{ formattedTime }}
      </div>

      <!-- 取消按钮 -->
      <div class="cancel-btn" @click.stop="handleCancel">
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
  startRecording()
}

const handleEndRecord = () => {
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
  border-radius: 8px; // 野火IM圆角
  user-select: none;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid #e0e0e0; // 野火IM边框

  &:hover {
    background: #f5f5f5;
    border-color: #4168e0; // 野火IM蓝
  }

  &.is-recording {
    background: #fff1f0;
    border-color: #ff6b6b;
    cursor: default;
  }

  .dark & {
    background: #1e1e1e;
    border-color: #374151;

    &:hover {
      background: #2d2d2d;
      border-color: #4168e0;
    }

    &.is-recording {
      background: rgba(255, 107, 107, 0.1);
      border-color: rgba(255, 107, 107, 0.4);
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
      color: #4168e0; // 野火IM蓝
      animation: micPulse 2s ease-in-out infinite;
    }

    .guide-text {
      font-size: 13px;
      font-weight: 500;
    }
  }
}

@keyframes micPulse {

  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }

  50% {
    opacity: 0.7;
    transform: scale(1.05);
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
    background: linear-gradient(180deg, #ff6b6b, #ff4757); // 渐变红色
    border-radius: 2px;
    animation: recordingWave 0.5s ease-in-out infinite;
    height: 8px;
    transform-origin: center;

    @keyframes recordingWave {

      0%,
      100% {
        height: 8px;
        opacity: 0.7;
      }

      50% {
        height: 28px;
        opacity: 1;
      }
    }
  }
}

.recording-time {
  font-size: 18px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  color: #ff4757;
  letter-spacing: 1px;
}

.cancel-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f5f5f5;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;

  &::before {
    content: '松开取消';
    position: absolute;
    top: -24px;
    left: 50%;
    transform: translateX(-50%);
    font-size: 11px;
    color: #999;
    white-space: nowrap;
    opacity: 0;
    transition: opacity 0.2s;
  }

  &:hover {
    background: #ffe0e0;
    transform: scale(1.1);

    &::before {
      opacity: 1;
    }

    .material-icons-outlined {
      color: #ff4757;
    }
  }

  .material-icons-outlined {
    font-size: 20px;
    color: #999;
    transition: color 0.2s;
  }

  .dark & {
    background: rgba(255, 255, 255, 0.08);

    &:hover {
      background: rgba(255, 71, 87, 0.2);
    }
  }
}
</style>
