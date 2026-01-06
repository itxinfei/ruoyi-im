<template>
  <div class="voice-recorder">
    <!-- 录音按钮 -->
    <div
      class="record-button"
      :class="{ recording: isRecording, locked: isLocked }"
      @mousedown="handleStart"
      @touchstart.prevent="handleStart"
      @mouseup="handleStop"
      @touchend.prevent="handleStop"
      @mouseleave="handleMouseLeave"
      @touchcancel="handleMouseLeave"
    >
      <el-icon><Microphone /></el-icon>
      <span v-if="!isRecording">{{ isMobile ? '按住说话' : '按住录音' }}</span>
      <span v-else>{{ formatTime(recordingTime) }}</span>
      <div v-if="isRecording" class="recording-wave">
        <span v-for="i in 5" :key="i" class="wave-bar"></span>
      </div>
    </div>

    <!-- 锁定模式下的操作栏 -->
    <div v-if="isLocked" class="locked-controls">
      <el-button size="small" @click="cancelRecording">取消</el-button>
      <el-button type="primary" size="small" @click="finishRecording">发送</el-button>
    </div>

    <!-- 录音中提示弹窗 -->
    <teleport to="body">
      <transition name="recording-fade">
        <div v-if="isRecording && !isLocked" class="recording-popover" :style="popoverStyle">
          <div class="recording-content">
            <div class="recording-icon">
              <el-icon class="is-recording"><Microphone /></el-icon>
            </div>
            <div class="recording-info">
              <div class="recording-time">{{ formatTime(recordingTime) }}</div>
              <div class="recording-hint">{{ recordingHint }}</div>
            </div>
          </div>
          <div class="recording-cancel-area" @click="cancelRecording">
            <el-icon><Delete /></el-icon>
            <span>上滑取消</span>
          </div>
        </div>
      </transition>
    </teleport>

    <!-- 试听弹窗 -->
    <el-dialog v-model="showPreview" title="语音预览" width="400px" :close-on-click-modal="false">
      <div class="voice-preview">
        <div class="preview-waveform">
          <canvas ref="waveformCanvas" width="300" height="60"></canvas>
        </div>
        <div class="preview-time">{{ formatTime(audioDuration) }}</div>
        <div class="preview-controls">
          <el-button @click="playAudio" :icon="isPlaying ? VideoPause : VideoClock" circle />
          <el-button type="danger" @click="deleteAudio" :icon="Delete" circle />
          <el-button type="primary" @click="sendAudio" :icon="Promotion" circle />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { Microphone, Delete, Promotion, VideoClock, VideoPause } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  maxLength: {
    type: Number,
    default: 60,
  },
})

const emit = defineEmits(['send', 'cancel'])

// 状态
const isRecording = ref(false)
const isLocked = ref(false)
const isPlaying = ref(false)
const recordingTime = ref(0)
const audioBlob = ref(null)
const audioUrl = ref('')
const audioDuration = ref(0)
const showPreview = ref(false)

// DOM 引用
const waveformCanvas = ref(null)

// 计时器
let recordingTimer = null
let audioContext = null
let analyser = null
let mediaRecorder = null
let audioChunks = []
let audioElement = null

// 检测是否是移动设备
const isMobile = computed(() => {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
})

// 提示文字
const recordingHint = computed(() => {
  if (recordingTime.value < 1) return '松开发送'
  if (recordingTime.value < 3) return '上滑取消，松开发送'
  return '上滑取消，右滑锁定'
})

// 弹窗位置
const popoverStyle = ref({
  left: '50%',
  top: '50%',
  transform: 'translate(-50%, -50%)',
})

// 开始录音
const handleStart = async (event) => {
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    ElMessage.error('浏览器不支持录音功能')
    return
  }

  // 获取按钮位置
  const rect = event.target.getBoundingClientRect()
  popoverStyle.value = {
    left: rect.left + rect.width / 2 + 'px',
    top: rect.top - 100 + 'px',
    transform: 'translate(-50%, 0)',
  }

  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })

    // 创建音频上下文用于可视化
    audioContext = new (window.AudioContext || window.webkitAudioContext)()
    analyser = audioContext.createAnalyser()
    const source = audioContext.createMediaStreamSource(stream)
    source.connect(analyser)
    analyser.fftSize = 256

    // 创建录音器
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []

    mediaRecorder.ondataavailable = (e) => {
      if (e.data.size > 0) {
        audioChunks.push(e.data)
      }
    }

    mediaRecorder.onstop = () => {
      audioBlob.value = new Blob(audioChunks, { type: 'audio/webm' })
      audioUrl.value = URL.createObjectURL(audioBlob.value)
      audioDuration.value = recordingTime.value

      // 停止所有轨道
      stream.getTracks().forEach(track => track.stop())

      // 显示预览
      showPreview.value = true
      nextTick(() => {
        drawWaveform()
      })
    }

    mediaRecorder.start()
    isRecording.value = true
    recordingTime.value = 0
    startTimer()
    emit('cancel')
  } catch (error) {
    console.error('录音启动失败:', error)
    ElMessage.error('无法访问麦克风，请检查权限设置')
  }
}

// 停止录音
const handleStop = () => {
  if (!isRecording.value || isLocked.value) return

  if (recordingTime.value < 1) {
    ElMessage.warning('录音时间太短')
    cancelRecording()
    return
  }

  finishRecording()
}

// 鼠标离开按钮
const handleMouseLeave = () => {
  if (!isRecording.value || isLocked.value) return
}

// 启动计时器
const startTimer = () => {
  recordingTimer = setInterval(() => {
    recordingTime.value++
    if (recordingTime.value >= props.maxLength) {
      finishRecording()
    }
  }, 1000)
}

// 停止计时器
const stopTimer = () => {
  if (recordingTimer) {
    clearInterval(recordingTimer)
    recordingTimer = null
  }
}

// 完成录音
const finishRecording = () => {
  if (!isRecording.value) return

  stopTimer()
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
  }

  if (audioContext) {
    audioContext.close()
    audioContext = null
  }

  isRecording.value = false
  isLocked.value = false
}

// 取消录音
const cancelRecording = () => {
  stopTimer()

  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
  }

  if (audioContext) {
    audioContext.close()
    audioContext = null
  }

  audioBlob.value = null
  audioUrl.value = ''
  audioDuration.value = 0
  recordingTime.value = 0
  isRecording.value = false
  isLocked.value = false
  audioChunks = []
}

// 删除录音
const deleteAudio = () => {
  if (audioUrl.value) {
    URL.revokeObjectURL(audioUrl.value)
  }

  audioBlob.value = null
  audioUrl.value = ''
  audioDuration.value = 0
  showPreview.value = false
}

// 播放音频
const playAudio = () => {
  if (!audioUrl.value) return

  if (isPlaying.value) {
    if (audioElement) {
      audioElement.pause()
      audioElement = null
    }
    isPlaying.value = false
    return
  }

  audioElement = new Audio(audioUrl.value)
  audioElement.onended = () => {
    isPlaying.value = false
    audioElement = null
  }
  audioElement.play()
  isPlaying.value = true
}

// 发送音频
const sendAudio = () => {
  if (!audioBlob.value) return

  emit('send', {
    blob: audioBlob.value,
    url: audioUrl.value,
    duration: audioDuration.value,
    type: 'audio/webm',
  })

  // 清理
  deleteAudio()
  showPreview.value = false
}

// 绘制波形
const drawWaveform = () => {
  const canvas = waveformCanvas.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  const width = canvas.width
  const height = canvas.height

  ctx.clearRect(0, 0, width, height)
  ctx.fillStyle = '#409eff'

  // 绘制模拟波形
  const bars = 50
  const barWidth = width / bars

  for (let i = 0; i < bars; i++) {
    const barHeight = Math.random() * height * 0.8 + height * 0.1
    const x = i * barWidth
    const y = (height - barHeight) / 2

    ctx.fillRect(x, y, barWidth - 1, barHeight)
  }
}

// 格式化时间
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

onUnmounted(() => {
  stopTimer()
  if (audioUrl.value) {
    URL.revokeObjectURL(audioUrl.value)
  }
  if (audioElement) {
    audioElement.pause()
    audioElement = null
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.voice-recorder {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.record-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
  position: relative;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  }

  &:active {
    transform: translateY(0);
  }

  &.recording {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    animation: recording-pulse 1s infinite;
  }

  .el-icon {
    font-size: 18px;
  }

  span {
    font-size: 14px;
    font-weight: 500;
  }

  .recording-wave {
    display: flex;
    align-items: center;
    gap: 2px;
    margin-left: 4px;

    .wave-bar {
      width: 3px;
      height: 12px;
      background: rgba(255, 255, 255, 0.8);
      border-radius: 2px;
      animation: wave 0.5s ease-in-out infinite alternate;

      &:nth-child(1) { animation-delay: 0s; }
      &:nth-child(2) { animation-delay: 0.1s; }
      &:nth-child(3) { animation-delay: 0.2s; }
      &:nth-child(4) { animation-delay: 0.3s; }
      &:nth-child(5) { animation-delay: 0.4s; }
    }
  }
}

.locked-controls {
  display: flex;
  gap: 8px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 20px;
}

.recording-popover {
  position: fixed;
  z-index: 9999;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 16px;
  min-width: 200px;

  .recording-content {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;

    .recording-icon {
      width: 48px;
      height: 48px;
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;

      .el-icon {
        font-size: 24px;
        color: white;

        &.is-recording {
          animation: pulse 1s infinite;
        }
      }
    }

    .recording-info {
      flex: 1;

      .recording-time {
        font-size: 24px;
        font-weight: bold;
        color: $text-primary;
        font-family: monospace;
      }

      .recording-hint {
        font-size: 12px;
        color: $text-secondary;
        margin-top: 4px;
      }
    }
  }

  .recording-cancel-area {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 8px;
    background: #fef0f0;
    border-radius: 8px;
    color: #f56c6c;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #fde2e2;
    }

    .el-icon {
      font-size: 16px;
    }
  }
}

.voice-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;

  .preview-waveform {
    margin-bottom: 16px;
  }

  .preview-time {
    font-size: 24px;
    font-weight: bold;
    color: $text-primary;
    margin-bottom: 20px;
    font-family: monospace;
  }

  .preview-controls {
    display: flex;
    gap: 16px;
  }
}

@keyframes recording-pulse {
  0%, 100% {
    box-shadow: 0 4px 12px rgba(240, 147, 251, 0.4);
  }
  50% {
    box-shadow: 0 4px 20px rgba(240, 147, 251, 0.6);
  }
}

@keyframes wave {
  from {
    height: 6px;
  }
  to {
    height: 20px;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.recording-fade-enter-active,
.recording-fade-leave-active {
  transition: all 0.2s;
}

.recording-fade-enter-from,
.recording-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -10px);
}
</style>
