<template>
  <el-dialog
    v-model="visible"
    title="语音消息"
    :width="var(--dt-dialog-width-md, 400)"
    :close-on-click-modal="false"
    :show-close="true"
    class="voice-recorder-dialog"
    @close="handleClose"
  >
    <div class="recorder-content">
      <!-- 录音状态显示 -->
      <div class="recorder-status">
        <div class="status-icon" :class="recordingState">
          <span v-if="recordingState === 'idle'" class="material-icons-outlined">mic</span>
          <div v-else-if="recordingState === 'recording'" class="recording-animation">
            <div class="pulse-ring" />
            <div class="pulse-ring delay" />
            <span class="material-icons-outlined">mic</span>
          </div>
          <span v-else class="material-icons-outlined">check_circle</span>
        </div>
        <div class="status-text">
          {{ statusText }}
        </div>
        <div v-if="recordingState === 'recording'" class="duration recording">
          {{ formattedDuration }}
          <span class="duration-max">/ 1:00</span>
        </div>
        <div v-else-if="recordingState === 'recorded'" class="duration">
          {{ formattedDuration }}
        </div>
      </div>

      <!-- 波形可视化 -->
      <div v-if="recordingState === 'recording'" class="waveform-visual">
        <div class="waveform-bg" />
        <div
          v-for="(bar, index) in waveformBars"
          :key="`wave-${index}`"
          class="wave-bar"
          :style="{ height: bar + '%' }"
        />
      </div>

      <!-- 预览播放 -->
      <div v-if="recordingState === 'recorded' && audioUrl" class="preview-section">
        <div class="audio-preview">
          <button class="preview-play-btn" @click="togglePreview">
            <span v-if="!isPreviewPlaying" class="material-icons-outlined">play_arrow</span>
            <span v-else class="material-icons-outlined">pause</span>
          </button>
          <div class="preview-waveform">
            <div
              v-for="i in 30"
              :key="i"
              class="preview-bar"
              :style="{ height: getPreviewBarHeight(i) + '%' }"
            />
          </div>
          <span class="preview-duration">{{ formattedDuration }}</span>
        </div>
        <div class="preview-progress-bar" @click="seekPreview">
          <div class="progress-fill" :style="{ width: previewProgress + '%' }" />
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="recorder-actions">
        <template v-if="recordingState === 'idle'">
          <el-button type="primary" size="large" @click="startRecording">
            <span class="material-icons-outlined">mic</span>
            开始录音
          </el-button>
        </template>
        <template v-else-if="recordingState === 'recording'">
          <el-button type="danger" size="large" @click="stopRecording">
            <span class="material-icons-outlined">stop</span>
            完成录音
          </el-button>
          <el-button size="large" @click="cancelRecording">
            取消
          </el-button>
        </template>
        <template v-else-if="recordingState === 'recorded'">
          <el-button type="primary" size="large" @click="sendVoice">
            <span class="material-icons-outlined">send</span>
            发送
          </el-button>
          <el-button size="large" @click="reRecord">
            <span class="material-icons-outlined">refresh</span>
            重新录制
          </el-button>
        </template>
      </div>

      <!-- 提示信息 -->
      <div class="recorder-tips">
        <span class="material-icons-outlined tips-icon">info</span>
        <span>最长可录制60秒，点击完成或等待自动结束</span>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['send', 'close'])

const visible = ref(false)
const recordingState = ref('idle') // idle, recording, recorded
const duration = ref(0)
const audioUrl = ref('')
const audioBlob = ref(null)

// 预览播放
const isPreviewPlaying = ref(false)
const previewProgress = ref(0)
let previewAudio = null

// 录音相关
let mediaRecorder = null
let audioChunks = []
let stream = null
let timer = null
let startTime = 0

// 波形可视化
const waveformBars = ref(Array(20).fill(20))
let analyser = null
let animationId = null

const MAX_DURATION = 60 // 最大录音时长60秒

const statusText = computed(() => {
  switch (recordingState.value) {
    case 'idle': return '点击开始录音'
    case 'recording': return '正在录音...'
    case 'recorded': return '录音完成'
    default: return ''
  }
})

const formattedDuration = computed(() => {
  const mins = Math.floor(duration.value / 60)
  const secs = Math.floor(duration.value % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
})

// 打开弹窗
const open = () => {
  visible.value = true
  recordingState.value = 'idle'
  duration.value = 0
  audioUrl.value = ''
  audioBlob.value = null
  isPreviewPlaying.value = false
  previewProgress.value = 0
}

// 开始录音
const startRecording = async () => {
  try {
    stream = await navigator.mediaDevices.getUserMedia({ audio: true })

    mediaRecorder = new MediaRecorder(stream, {
      mimeType: MediaRecorder.isTypeSupported('audio/webm') ? 'audio/webm' : 'audio/mp4'
    })

    audioChunks = []

    mediaRecorder.ondataavailable = (e) => {
      if (e.data.size > 0) {
        audioChunks.push(e.data)
      }
    }

    mediaRecorder.onstop = () => {
      const mimeType = mediaRecorder.mimeType || 'audio/webm'
      audioBlob.value = new Blob(audioChunks, { type: mimeType })
      audioUrl.value = URL.createObjectURL(audioBlob.value)
      recordingState.value = 'recorded'
    }

    mediaRecorder.start(100)
    recordingState.value = 'recording'
    startTime = Date.now()

    // 设置音频分析器用于波形可视化
    const audioContext = new AudioContext()
    analyser = audioContext.createAnalyser()
    const source = audioContext.createMediaStreamSource(stream)
    source.connect(analyser)
    analyser.fftSize = 64
    startVisualization()

    // 开始计时
    timer = setInterval(() => {
      duration.value = Math.floor((Date.now() - startTime) / 1000)
      if (duration.value >= MAX_DURATION) {
        stopRecording()
      }
    }, 100)

  } catch (error) {
    console.error('无法获取麦克风权限:', error)
    ElMessage.error('无法获取麦克风权限，请检查浏览器设置')
  }
}

// 停止录音
const stopRecording = () => {
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
  }

  if (stream) {
    stream.getTracks().forEach(track => track.stop())
    stream = null
  }

  if (timer) {
    clearInterval(timer)
    timer = null
  }

  stopVisualization()
}

// 取消录音
const cancelRecording = () => {
  stopRecording()
  recordingState.value = 'idle'
  duration.value = 0
  audioChunks = []
  handleClose()
}

// 重新录音
const reRecord = () => {
  if (audioUrl.value) {
    URL.revokeObjectURL(audioUrl.value)
  }
  audioUrl.value = ''
  audioBlob.value = null
  duration.value = 0
  recordingState.value = 'idle'
  isPreviewPlaying.value = false
  previewProgress.value = 0
}

// 发送语音
const sendVoice = () => {
  if (!audioBlob.value) {
    ElMessage.warning('请先录制语音')
    return
  }

  emit('send', {
    blob: audioBlob.value,
    duration: duration.value,
    url: audioUrl.value
  })

  handleClose()
}

// 预览播放切换
const togglePreview = () => {
  if (!audioUrl.value) return

  if (isPreviewPlaying.value) {
    if (previewAudio) {
      previewAudio.pause()
    }
    isPreviewPlaying.value = false
  } else {
    if (!previewAudio) {
      previewAudio = new Audio(audioUrl.value)
      previewAudio.addEventListener('timeupdate', () => {
        if (previewAudio.duration) {
          previewProgress.value = (previewAudio.currentTime / previewAudio.duration) * 100
        }
      })
      previewAudio.addEventListener('ended', () => {
        isPreviewPlaying.value = false
        previewProgress.value = 0
      })
    }
    previewAudio.play()
    isPreviewPlaying.value = true
  }
}

// 波形可视化
const startVisualization = () => {
  if (!analyser) return

  const dataArray = new Uint8Array(analyser.frequencyBinCount)

  const updateWaveform = () => {
    if (recordingState.value !== 'recording') return

    analyser.getByteFrequencyData(dataArray)

    // 生成20个波形条
    const bars = []
    const step = Math.floor(dataArray.length / 20)
    for (let i = 0; i < 20; i++) {
      const value = dataArray[i * step] || 0
      bars.push(20 + (value / 255) * 80)
    }
    waveformBars.value = bars

    animationId = requestAnimationFrame(updateWaveform)
  }

  updateWaveform()
}

const stopVisualization = () => {
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
}

// 关闭弹窗
const handleClose = () => {
  if (recordingState.value === 'recording') {
    cancelRecording()
  }

  if (previewAudio) {
    previewAudio.pause()
    previewAudio = null
  }

  stopVisualization()
  visible.value = false
  emit('close')
}

onUnmounted(() => {
  if (audioUrl.value) {
    URL.revokeObjectURL(audioUrl.value)
  }
  if (previewAudio) {
    previewAudio.pause()
  }
  stopVisualization()
})

defineExpose({ open })
</script>

<style scoped lang="scss">
.voice-recorder-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
  }
}

.recorder-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--dt-spacing-lg, 20px);
}

.recorder-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  .status-icon {
    width: var(--dt-avatar-size-xl, 80px);
    height: var(--dt-avatar-size-xl, 80px);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);

    &.recording {
      background: var(--dt-error-bg);
      color: var(--dt-error-color);
      animation: pulse 1.5s infinite;
    }

    &.recorded {
      background: var(--dt-success-bg);
      color: var(--dt-success-color);
    }

    .material-icons-outlined {
      font-size: var(--dt-font-size-xl, 40px);
    }

    .recording-icon {
      font-size: 32px;
      color: var(--dt-error-color);
    }
  }

  .status-text {
    font-size: 14px;
    color: var(--dt-text-secondary);
  }

  .duration {
    font-size: var(--dt-font-size-lg, 24px);
    font-weight: 600;
    color: var(--dt-text-primary);
    font-variant-numeric: tabular-nums;
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.waveform-visual {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--dt-spacing-xs, 3px);
  height: var(--dt-icon-size-xl, 40px);
  width: 100%;

  .wave-bar {
    width: var(--dt-spacing-xs, 4px);
    background: var(--dt-brand-color);
    border-radius: var(--dt-radius-sm, 2px);
    transition: height 0.05s ease;
  }
}

.preview-section {
  width: 100%;

  .audio-preview {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-md, 12px);
    padding: var(--dt-spacing-md, 12px);
    background: var(--dt-bg-body);
    border-radius: var(--dt-radius-md, 8px);

    .preview-play-btn {
      width: var(--dt-btn-size-lg, 36px);
      height: var(--dt-btn-size-lg, 36px);
      border-radius: 50%;
      border: none;
      background: var(--dt-brand-color);
      color: var(--dt-text-white);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;

      .material-icons-outlined {
        font-size: var(--dt-icon-size-lg, 20px);
      }

      &:hover {
        opacity: 0.9;
      }
    }

    .preview-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: var(--dt-spacing-xs, 6px);

      .preview-duration {
        font-size: var(--dt-font-size-xs, 12px);
        color: var(--dt-text-secondary);
      }

      .preview-progress {
        height: var(--dt-spacing-xs, 4px);
        background: var(--dt-border-color);
        border-radius: var(--dt-radius-sm, 2px);
        overflow: hidden;

        .progress-fill {
          height: 100%;
          background: var(--dt-brand-color);
          transition: width 0.1s linear;
        }
      }
    }
  }
}

.recorder-actions {
  display: flex;
  gap: var(--dt-spacing-md, 12px);

  .el-button {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-xs, 6px);

    .material-icons-outlined {
      font-size: var(--dt-font-size-sm, 18px);
    }
  }
}

.recorder-tips {
  font-size: var(--dt-font-size-xs, 12px);
  color: var(--dt-text-quaternary);
}
</style>
