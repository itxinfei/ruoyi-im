<template>
  <div class="voice-recorder">
    <!-- 按住说话按钮 -->
    <button
      v-if="!isRecording"
      class="record-btn"
      :class="{ 'no-permission': !hasPermission }"
      @mousedown.prevent="handleMouseDown"
      @mouseup.prevent="handleMouseUp"
      @mouseleave.prevent="handleMouseLeave"
      @touchstart.prevent="handleTouchStart"
      @touchend.prevent="handleTouchEnd"
    >
      <el-icon><Microphone /></el-icon>
      <span class="record-text">{{ hasPermission ? '按住说话' : '点击开启麦克风权限' }}</span>
    </button>

    <!-- 录音中 - 松手结束 -->
    <div v-else class="recording-state">
      <div class="recording-hint">
        <span class="hint-text">正在录音... 松手结束</span>
      </div>
      <div class="recording-animation">
        <span class="wave" v-for="i in 3" :key="i"></span>
      </div>
      <span class="recording-time">{{ formatTime(recordingTime) }}</span>
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

let mediaRecorder = null
let audioChunks = []
let recordingInterval = null

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

// 鼠标按下 - 开始录音
const handleMouseDown = (e) => {
  if (!hasPermission.value) {
    ElMessage.warning({
      message: '请允许浏览器访问麦克风权限。在浏览器地址栏点击🔒图标，选择"允许"麦克风权限。',
      duration: 6000,
      showClose: true
    })
    return
  }
  
  startRecording()
}

// 鼠标松开 - 停止录音
const handleMouseUp = () => {
  if (isRecording.value) {
    stopRecording()
  }
}

// 鼠标离开 - 取消录音
const handleMouseLeave = () => {
  if (isRecording.value) {
    cancelRecording()
  }
}

// 触摸开始
const handleTouchStart = (e) => {
  if (!hasPermission.value) {
    ElMessage.warning({
      message: '请允许浏览器访问麦克风权限。在浏览器地址栏点击🔒图标，选择"允许"麦克风权限。',
      duration: 6000,
      showClose: true
    })
    return
  }
  
  startRecording()
}

// 触摸结束
const handleTouchEnd = (e) => {
  const endY = e.changedTouches[0].clientY
  
  // 上滑超过 50px 视为取消
  if (startY.value - endY > 50) {
    cancelRecording()
  } else {
    if (isRecording.value) {
      stopRecording()
    }
  }
}

// 开始录音
const startRecording = async () => {
  // 立即设置录音状态，提供即时反馈
  isRecording.value = true
  
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []

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
      
      recordingTime.value = 0
      isRecording.value = false
      clearInterval(recordingInterval)
      
      // 停止所有音频轨道
      stream.getTracks().forEach(track => track.stop())
    }

    // 录音失败时重置状态
    mediaRecorder.onerror = (error) => {
      console.error('录音器错误:', error)
      isRecording.value = false
      clearInterval(recordingInterval)
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

// 停止录音
const stopRecording = () => {
  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop()
  }
}

// 取消录音
const cancelRecording = () => {
  stopRecording()
  emit('cancel')
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
.voice-recorder {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  background: #f8fafc;
  border-radius: 6px;
  min-height: 60px;
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

.recording-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  background: rgba(22, 119, 255, 0.1);
  border-radius: 12px;
  border: 2px solid var(--dt-brand-color, #1677ff);
  animation: recording-pulse 1.5s ease-in-out infinite;

  .recording-hint {
    .hint-text {
      font-size: 13px;
      color: var(--dt-brand-color, #1677ff);
      font-weight: 500;
    }
  }

  .recording-animation {
    display: flex;
    gap: 4px;
    align-items: center;

    .wave {
      width: 4px;
      height: 16px;
      background: var(--dt-brand-color, #1677ff);
      border-radius: 2px;
      animation: wave 1.2s ease-in-out infinite;

      &:nth-child(2) {
        animation-delay: 0.2s;
      }

      &:nth-child(3) {
        animation-delay: 0.4s;
      }
    }
  }

  .recording-time {
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-brand-color, #1677ff);
    font-variant-numeric: tabular-nums;
  }

  .cancel-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border: none;
    background: #f2f3f5;
    border-radius: 50%;
    cursor: pointer;
    color: #646a73;
    transition: all 0.2s;

    &:hover {
      background: #ff4d4f;
      color: #fff;
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
