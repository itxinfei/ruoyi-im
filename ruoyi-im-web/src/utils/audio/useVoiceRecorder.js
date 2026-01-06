/**
 * @file useVoiceRecorder.js
 * @description Vue Composable - 语音录制功能的组合式函数
 * @author IM System
 * @version 1.0.0
 */

import { ref, reactive, onUnmounted, computed } from 'vue'
import VoiceRecorder, { formatDuration, formatFileSize } from './voiceRecorder'

/**
 * 语音录制组合式函数
 * @param {Object} options - 配置选项
 * @param {number} options.maxDuration - 最大录制时长（秒），默认60秒
 * @param {number} options.minDuration - 最小录制时长（秒），默认1秒
 * @param {Function} options.onRecordComplete - 录制完成回调
 * @param {Function} options.onError - 错误回调
 * @returns {Object} 录制相关的状态和方法
 */
export function useVoiceRecorder(options = {}) {
  const {
    maxDuration = 60,
    minDuration = 1,
    onRecordComplete = () => {},
    onError = () => {},
  } = options

  // ==================== 响应式状态 ====================

  /** 是否正在录制 */
  const isRecording = ref(false)

  /** 是否暂停 */
  const isPaused = ref(false)

  /** 当前录制时长（秒） */
  const duration = ref(0)

  /** 当前音量（0-100） */
  const volume = ref(0)

  /** 录制结果 */
  const recordResult = ref(null)

  /** 错误信息 */
  const error = ref(null)

  /** 是否支持录音 */
  const isSupported = ref(VoiceRecorder.isSupported())

  /** 录制器实例 */
  let recorder = null

  // ==================== 计算属性 ====================

  /**
   * 格式化的录制时长
   */
  const formattedDuration = computed(() => formatDuration(duration.value))

  /**
   * 录制进度百分比
   */
  const progress = computed(() => {
    return Math.min(100, (duration.value / maxDuration) * 100)
  })

  /**
   * 是否可以发送（达到最小时长）
   */
  const canSend = computed(() => {
    return duration.value >= minDuration
  })

  /**
   * 剩余可录制时间
   */
  const remainingTime = computed(() => {
    return Math.max(0, maxDuration - duration.value)
  })

  /**
   * 格式化的剩余时间
   */
  const formattedRemainingTime = computed(() => formatDuration(remainingTime.value))

  // ==================== 方法定义 ====================

  /**
   * 初始化录制器
   * @private
   */
  const initRecorder = () => {
    if (recorder) {
      recorder.destroy()
    }

    recorder = new VoiceRecorder({
      maxDuration,
      onStart: () => {
        isRecording.value = true
        isPaused.value = false
        error.value = null
        recordResult.value = null
      },
      onStop: result => {
        isRecording.value = false
        isPaused.value = false

        if (result.duration < minDuration) {
          error.value = `录音时间太短，至少需要 ${minDuration} 秒`
          return
        }

        recordResult.value = {
          ...result,
          formattedDuration: formatDuration(result.duration),
          formattedSize: formatFileSize(result.fileSize),
        }

        onRecordComplete(recordResult.value)
      },
      onPause: () => {
        isPaused.value = true
      },
      onResume: () => {
        isPaused.value = false
      },
      onError: err => {
        error.value = err.message
        isRecording.value = false
        isPaused.value = false
        onError(err)
      },
      onTimeUpdate: time => {
        duration.value = time
      },
      onVolumeChange: vol => {
        volume.value = vol
      },
    })
  }

  /**
   * 开始录制
   * @async
   * @returns {Promise<void>}
   */
  const startRecording = async () => {
    if (!isSupported.value) {
      error.value = '当前浏览器不支持录音功能'
      onError(new Error(error.value))
      return
    }

    if (isRecording.value) {
      console.warn('录音已在进行中')
      return
    }

    // 重置状态
    duration.value = 0
    volume.value = 0
    error.value = null
    recordResult.value = null

    // 初始化并开始录制
    initRecorder()
    await recorder.start()
  }

  /**
   * 停止录制
   */
  const stopRecording = () => {
    if (!isRecording.value) return
    recorder?.stop()
  }

  /**
   * 暂停录制
   */
  const pauseRecording = () => {
    if (!isRecording.value || isPaused.value) return
    recorder?.pause()
  }

  /**
   * 恢复录制
   */
  const resumeRecording = () => {
    if (!isRecording.value || !isPaused.value) return
    recorder?.resume()
  }

  /**
   * 取消录制
   */
  const cancelRecording = () => {
    if (!isRecording.value) return

    recorder?.cancel()
    isRecording.value = false
    isPaused.value = false
    duration.value = 0
    volume.value = 0
    recordResult.value = null
  }

  /**
   * 切换录制状态
   * @async
   */
  const toggleRecording = async () => {
    if (isRecording.value) {
      stopRecording()
    } else {
      await startRecording()
    }
  }

  /**
   * 切换暂停状态
   */
  const togglePause = () => {
    if (isPaused.value) {
      resumeRecording()
    } else {
      pauseRecording()
    }
  }

  /**
   * 清除录制结果
   */
  const clearResult = () => {
    if (recordResult.value?.url) {
      URL.revokeObjectURL(recordResult.value.url)
    }
    recordResult.value = null
  }

  /**
   * 清除错误
   */
  const clearError = () => {
    error.value = null
  }

  /**
   * 获取音量条数据（用于可视化）
   * @param {number} barCount - 音量条数量
   * @returns {Array<number>} 各个音量条的高度百分比
   */
  const getVolumeBars = (barCount = 5) => {
    const bars = []
    const baseVolume = volume.value

    for (let i = 0; i < barCount; i++) {
      // 生成随机波动，使音量条更生动
      const variation = Math.random() * 20 - 10
      const barHeight = Math.max(10, Math.min(100, baseVolume + variation))
      bars.push(barHeight)
    }

    return bars
  }

  // ==================== 生命周期 ====================

  /**
   * 组件卸载时清理资源
   */
  onUnmounted(() => {
    if (recorder) {
      recorder.destroy()
      recorder = null
    }
    clearResult()
  })

  // ==================== 返回值 ====================

  return {
    // 状态
    isRecording,
    isPaused,
    duration,
    volume,
    recordResult,
    error,
    isSupported,

    // 计算属性
    formattedDuration,
    progress,
    canSend,
    remainingTime,
    formattedRemainingTime,

    // 方法
    startRecording,
    stopRecording,
    pauseRecording,
    resumeRecording,
    cancelRecording,
    toggleRecording,
    togglePause,
    clearResult,
    clearError,
    getVolumeBars,

    // 工具函数
    formatDuration,
    formatFileSize,
  }
}

export default useVoiceRecorder
