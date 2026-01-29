<template>
  <div class="voice-recorder">
    <!-- 点击开始录音按钮 -->
    <button
      v-if="!isRecording"
      class="record-btn"
      :class="{ 'no-permission': !hasPermission }"
      @click="handleStartRecording"
    >
      <el-icon><Microphone /></el-icon>
      <span class="record-text">{{ hasPermission ? '点击开始录音' : '点击开启麦克风权限' }}</span>
    </button>

    <!-- 录音中 - 点击停止 -->
    <div v-else class="recording-container">
      <div class="recording-btn" @click="handleStopRecording">
        <div class="recording-content">
          <div class="recording-left">
            <div class="recording-icon">
              <span class="material-icons-outlined">mic</span>
            </div>
            <div class="recording-animation">
              <span class="wave" v-for="i in 5" :key="i" :style="{ height: `${volumeLevels[i-1]}px` }"></span>
            </div>
            <span class="recording-time">{{ formatTime(recordingTime) }}</span>
          </div>
          <div class="recording-right">
            <span class="stop-text">点击停止</span>
            <span class="stop-icon">
              <span class="material-icons-outlined">stop_circle</span>
            </span>
          </div>
        </div>
      </div>
      <div class="recording-actions">
        <button class="cancel-btn" @click="handleCancelRecording">
          <span class="material-icons-outlined">close</span>
          取消
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted, onMounted } from 'vue'
import { Microphone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['record-complete', 'cancel'])

const isRecording = ref(false)
const recordingTime = ref(0)
const hasPermission = ref(true)
const volumeLevels = ref(new Array(5).fill(8)) // 音量水平数组

let mediaRecorder = null
let audioChunks = []
let recordingInterval = null
let audioContext = null
let analyser = null
let dataArray = null
let animationFrameId = null

// 检查麦克风权限
const checkMicrophonePermission = async () => {
  try {
    const permissionStatus = await navigator.permissions.query({ name: 'microphone' })
    hasPermission.value = permissionStatus.state === 'granted'
    
    permissionStatus.onchange = () => {
      hasPermission.value = permissionStatus.state === 'granted'
    }
  } catch (error) {
    console.log('无法检查麦克风权限状态:', error)
  }
}

// 组件挂载时检查权限
onMounted(() => {
  checkMicrophonePermission()
})

// 点击开始录音
const handleStartRecording = () => {
  startRecording()
}

// 点击停止录音
const handleStopRecording = () => {
  if (isRecording.value) {
    stopRecording()
  }
}

// 开始录音
const startRecording = async () => {
  try {
    // 先尝试获取麦克风权限
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    
    // 获取权限成功后，设置录音状态
    isRecording.value = true
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []

    // 初始化音频分析器用于音量可视化
    initAudioAnalyzer(stream)

    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        audioChunks.push(event.data)
      }
    }

    mediaRecorder.onstop = () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/webm' })
      const audioUrl = URL.createObjectURL(audioBlob)
      
      // 发送录音完成事件，传递音频数据
      emit('record-complete', {
        blob: audioBlob,
        url: audioUrl,
        duration: recordingTime.value
      })
      
      // 清理资源
      cleanupRecording(stream)
    }

    // 录音失败时重置状态
    mediaRecorder.onerror = (error) => {
      console.error('录音器错误:', error)
      isRecording.value = false
      clearInterval(recordingInterval)
      if (animationFrameId) {
        cancelAnimationFrame(animationFrameId)
      }
      ElMessage.error('录音失败，请重试')
    }

    mediaRecorder.start()
    hasPermission.value = true

    // 开始计时
    recordingInterval = setInterval(() => {
      recordingTime.value++
      // 最长录音60秒
      if (recordingTime.value >= 60) {
        stopRecording()
      }
    }, 1000)
  } catch (error) {
    // 录音失败时重置状态
    isRecording.value = false
    if (animationFrameId) {
      cancelAnimationFrame(animationFrameId)
    }
    
    let errorMessage = '无法访问麦克风'
    
    if (error.name === 'NotAllowedError' || error.name === 'PermissionDeniedError') {
      errorMessage = '麦克风权限被拒绝，请在浏览器设置中允许访问麦克风'
      hasPermission.value = false
    } else if (error.name === 'NotFoundError') {
      errorMessage = '未检测到麦克风设备，请检查设备连接'
    } else if (error.name === 'NotReadableError') {
      errorMessage = '麦克风被其他应用占用，请关闭其他应用后重试'
    }
    
    ElMessage.error({
      message: errorMessage,
      duration: 5000,
      showClose: true
    })
    console.error('录音失败:', error.name, error.message)
  }
}

// 初始化音频分析器
const initAudioAnalyzer = (stream) => {
  try {
    audioContext = new (window.AudioContext || window.webkitAudioContext)()
    const source = audioContext.createMediaStreamSource(stream)
    analyser = audioContext.createAnalyser()
    analyser.fftSize = 32 // 较小的fftSize，适合简单的可视化
    
    source.connect(analyser)
    
    const bufferLength = analyser.frequencyBinCount
    dataArray = new Uint8Array(bufferLength)
    
    // 开始可视化
    updateVolumeVisualization()
  } catch (error) {
    console.error('初始化音频分析器失败:', error)
  }
}

// 更新音量可视化
const updateVolumeVisualization = () => {
  if (!analyser) return
  
  analyser.getByteFrequencyData(dataArray)
  
  // 计算5个频段的音量水平
  const bandSize = Math.floor(dataArray.length / 5)
  for (let i = 0; i < 5; i++) {
    let sum = 0
    for (let j = 0; j < bandSize; j++) {
      sum += dataArray[i * bandSize + j]
    }
    const average = sum / bandSize
    // 将音量映射到8-24px的高度范围
    volumeLevels.value[i] = Math.max(8, Math.min(24, Math.floor(average * 0.1)))
  }
  
  animationFrameId = requestAnimationFrame(updateVolumeVisualization)
}

// 清理录音资源
const cleanupRecording = (stream) => {
  recordingTime.value = 0
  isRecording.value = false
  clearInterval(recordingInterval)
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
  if (audioContext) {
    audioContext.close()
  }
  // 停止所有音频轨道
  if (stream) {
    stream.getTracks().forEach(track => track.stop())
  }
}

// 停止录音
const stopRecording = () => {
  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop()
  }
}

// 处理取消录音
const handleCancelRecording = () => {
  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop()
    // 清理资源
    if (mediaRecorder.stream) {
      cleanupRecording(mediaRecorder.stream)
    } else {
      // 如果stream不存在，仍然清理其他资源
      isRecording.value = false
      clearInterval(recordingInterval)
      if (animationFrameId) {
        cancelAnimationFrame(animationFrameId)
      }
      if (audioContext) {
        audioContext.close()
      }
    }
    emit('cancel')
  }
}

// 取消录音
const cancelRecording = () => {
  handleCancelRecording()
}

// 格式化时间
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 清理资源
onUnmounted(() => {
  clearInterval(recordingInterval)
  if (mediaRecorder && mediaRecorder.stream) {
    mediaRecorder.stream.getTracks().forEach(track => track.stop())
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.voice-recorder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;

  .dark & {
    background: #1e293b;
  }
}

.record-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  background: var(--dt-brand-color, #1677ff);
  color: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;

  &:hover {
    opacity: 0.9;
    transform: scale(1.02);
  }

  &:active {
    transform: scale(0.98);
  }

  &.no-permission {
    background: #ff4d4f;
    cursor: help;

    &:hover {
      opacity: 1;
      transform: none;
    }
  }

  .el-icon {
    font-size: 20px;
  }
}

.recording-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.recording-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 10px 20px;
  background: var(--dt-brand-bg);
  border: 1px solid var(--dt-brand-color);
  border-radius: var(--dt-radius-lg);
  cursor: pointer;
  transition: all 0.2s;
  animation: recording-pulse 1.5s ease-in-out infinite;

  &:hover {
    background: var(--dt-brand-bg);
    border-color: var(--dt-brand-hover);
  }

  .recording-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    gap: 16px;
  }

  .recording-left {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
  }

  .recording-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    background: var(--dt-brand-color);
    border-radius: 50%;

    .material-icons-outlined {
      font-size: 18px;
      color: #fff;
    }
  }

  .recording-animation {
    display: flex;
    gap: 3px;
    align-items: center;

    .wave {
      width: 4px;
      background: var(--dt-brand-color, #1677ff);
      border-radius: 2px;
      transition: height 0.1s ease;
    }
  }

  .recording-time {
    font-size: 15px;
    font-weight: 600;
    color: var(--dt-brand-color, #1677ff);
    font-variant-numeric: tabular-nums;
  }

  .recording-right {
    display: flex;
    align-items: center;
    gap: 6px;

    .stop-text {
      font-size: 13px;
      color: var(--dt-text-secondary);
      font-weight: 500;
    }

    .stop-icon {
      display: flex;
      align-items: center;
      justify-content: center;

      .material-icons-outlined {
        font-size: 24px;
        color: #ff4d4f;
      }
    }
  }
}

.recording-actions {
  display: flex;
  justify-content: flex-end;

  .cancel-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 12px;
    border: 1px solid var(--dt-border-color);
    background: var(--dt-bg-card);
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    font-size: 12px;
    color: var(--dt-text-secondary);
    transition: all 0.2s;

    &:hover {
      background: var(--dt-bg-hover);
      border-color: var(--dt-border-input-hover);
      color: var(--dt-text-primary);
    }

    .material-icons-outlined {
      font-size: 14px;
    }

    .dark & {
      background: var(--dt-bg-card-dark);
      border-color: var(--dt-border-dark);

      &:hover {
        background: var(--dt-bg-hover-dark);
        border-color: var(--dt-border-dark);
      }
    }
  }
}

.preview-state {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 320px;

  .audio-preview {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: #fff;
    border-radius: 6px;
    border: 1px solid #e2e8f0;
    .dark & {
      background: #0f172a;
      border-color: #334155;
    }

    .play-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      border: none;
      background: var(--dt-brand-color, #1677ff);
      border-radius: 50%;
      cursor: pointer;
      color: #fff;
      flex-shrink: 0;

      &:hover {
        opacity: 0.9;
      }

      .el-icon {
        font-size: 18px;
      }
    }

    .audio-waveform {
      display: flex;
      gap: 2px;
      align-items: center;
      flex: 1;
      height: 24px;

      .wave-bar {
        width: 3px;
        height: 8px;
        background: #d1d5db;
        border-radius: 2px;
        transition: all 0.2s;

        &.active {
          background: var(--dt-brand-color, #1677ff);
          height: 16px;
        }
      }
    }

    .duration {
      font-size: 12px;
      color: #8f959e;
      font-variant-numeric: tabular-nums;
    }
  }

  .preview-actions {
    display: flex;
    gap: 8px;

    .action-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      padding: 8px 16px;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-size: 13px;
      transition: all 0.2s;

      &.delete-btn {
        background: #f2f3f5;
        color: #646a73;

        &:hover {
          background: #ff4d4f;
          color: #fff;
        }
      }

      &.send-btn {
        background: var(--dt-brand-color, #1677ff);
        color: #fff;

        &:hover {
          opacity: 0.9;
        }
      }
    }
  }
}

@keyframes wave {
  0%, 100% {
    height: 8px;
  }
  50% {
    height: 24px;
  }
}

@keyframes recording-pulse {
  0%, 100% {
    border-color: var(--dt-brand-color, #1677ff);
    background: rgba(22, 119, 255, 0.1);
  }
  50% {
    border-color: rgba(22, 119, 255, 0.7);
    background: rgba(22, 119, 255, 0.15);
  }
}
</style>
