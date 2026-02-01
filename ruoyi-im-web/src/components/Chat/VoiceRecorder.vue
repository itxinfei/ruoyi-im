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
    <div v-if="!isRecording" class="voice-guide">
      <span class="material-icons-outlined mic-icon">mic</span>
      <span class="guide-text">{{ touchStarted ? '松开 发送' : '按住 说话' }}</span>
    </div>

    <!-- 录音中状态 -->
    <div v-else class="recording-state">
      <!-- 录音动画波形 -->
      <div class="recording-waveform">
        <span
          v-for="index in 15"
          :key="index"
          class="wave-bar"
          :style="{ animationDelay: `${index * 0.05}s` }"
        ></span>
      </div>

      <!-- 录音时长 -->
      <div class="recording-time">{{ formattedTime }}</div>

      <!-- 取消按钮 -->
      <div class="cancel-btn" @click.stop="handleCancel">
        <span class="material-icons-outlined">cancel</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  maxLength: { type: Number, default: 60000 }, // 最长60秒
  minLength: { type: Number, default: 1000 }   // 最短1秒
})

const emit = defineEmits(['record-complete', 'cancel'])

// 状态
const isRecording = ref(false)
const recordingTime = ref(0)
const touchStarted = ref(false)
const mediaRecorder = ref(null)
const audioChunks = ref([])
const startTime = ref(0)
const timer = ref(null)

// 格式化时间
const formattedTime = computed(() => {
  const seconds = Math.floor(recordingTime.value / 1000)
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
})

// 开始录音
const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })

    mediaRecorder.value = new MediaRecorder(stream)
    audioChunks.value = []

    mediaRecorder.value.ondataavailable = (e) => {
      audioChunks.value.push(e.data)
    }

    mediaRecorder.value.onstop = () => {
      const audioBlob = new Blob(audioChunks.value, { type: 'audio/webm' })

      // 停止所有轨道
      stream.getTracks().forEach(track => track.stop())

      // 如果录音时长符合要求，发送完成事件
      const duration = Date.now() - startTime.value
      if (duration >= props.minLength && duration <= props.maxLength) {
        emit('record-complete', {
          blob: audioBlob,
          duration: duration
        })
      } else if (duration > props.maxLength) {
        ElMessage.warning(`录音时长不能超过${props.maxLength / 1000}秒`)
      }
    }

    mediaRecorder.value.start()
    startTime.value = Date.now()
    isRecording.value = true

    // 开始计时
    timer.value = setInterval(() => {
      recordingTime.value = Date.now() - startTime.value

      // 检查是否超过最大时长
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

// 停止录音
const stopRecording = () => {
  if (mediaRecorder.value && mediaRecorder.value.state !== 'inactive') {
    mediaRecorder.value.stop()
  }

  clearInterval(timer.value)
  isRecording.value = false
  recordingTime.value = 0
}

// 处理鼠标按下
const handleStartRecord = () => {
  touchStarted.value = false
  startRecording()
}

// 处理触摸开始
const handleTouchStart = () => {
  touchStarted.value = true
  startRecording()
}

// 处理鼠标释放
const handleEndRecord = () => {
  if (!touchStarted.value) {
    stopRecording()
  }
}

// 处理触摸结束
const handleTouchEnd = () => {
  stopRecording()
}

// 处理取消（鼠标离开）
const handleCancel = () => {
  if (isRecording.value && recordingTime.value < 500) {
    // 录音时间太短，自动取消
    stopRecording()
  }
}

// 清理
onUnmounted(() => {
  stopRecording()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.voice-recorder-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: var(--dt-bg-chat);
  border-radius: var(--dt-radius-lg);
  user-select: none;
  touch-action: none;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.is-recording {
    background: rgba(244, 67, 54, 0.1);
    cursor: default;
  }

  .voice-guide {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: var(--dt-text-secondary);

    .mic-icon {
      font-size: 32px;
      color: var(--dt-brand-color);
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
  gap: 16px;
  width: 100%;
  }

.recording-waveform {
  display: flex;
  gap: 3px;
  align-items: center;
  justify-content: center;
  height: 40px;

  .wave-bar {
    width: 4px;
    background: var(--dt-error-color);
    border-radius: 2px;
    animation: recordingWave 1s ease-in-out infinite;

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
  font-size: 18px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  color: var(--dt-error-color);
  margin: 8px 0;
}

.cancel-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba(244, 67, 54, 0.15);

    .material-icons-outlined {
      color: var(--dt-error-color);
    }
  }

  .material-icons-outlined {
    font-size: 20px;
    color: var(--dt-text-quaternary);
  }
}

// 暗色模式
:global(.dark) {
  .voice-recorder-wrapper {
    background: #1e1e1e1;

    &:hover {
      background: #2a2a2a2a;
    }

    &.is-recording {
      background: rgba(244, 67, 54, 0.15);
    }
  }

  .cancel-btn {
    background: rgba(255, 255, 255, 0.08);

    &:hover {
      background: rgba(244, 67, 54, 0.25);
    }
  }
}
</style>
