/**
 * 语音预览 Composable
 *
 * 职责：
 * - 管理语音录制后的预览状态
 * - 播放/暂停录音
 * - 进度更新
 * - 发送或删除录音
 *
 * 使用方式：
 * ```js
 * const {
 *   voicePreview,
 *   handleVoiceRecordComplete,
 *   toggleVoicePlay,
 *   deleteVoicePreview,
 *   sendVoicePreview
 * } = useVoicePreview({ onSend })
 * ```
 */
import { ref } from 'vue'

/**
 * 格式化时间
 * @param {number} seconds - 秒数
 * @returns {string} 格式化后的时间字符串
 */
export const formatVoiceTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

export function useVoicePreview(options = {}) {
  const {
    onSend = null,
    onDelete = null
  } = options

  const voicePreview = ref(null)
  const voiceAudioElement = ref(null)
  const voiceProgressInterval = ref(null)

  /**
   * 处理录音完成
   * @param {Object} params - { blob, url, duration }
   */
  const handleVoiceRecordComplete = ({ blob, url, duration }) => {
    voicePreview.value = {
      blob,
      url,
      duration,
      isPlaying: false,
      playProgress: 0
    }
  }

  /**
   * 播放/暂停录音预览
   */
  const toggleVoicePlay = () => {
    if (!voicePreview.value) return

    if (!voiceAudioElement.value) {
      // 创建音频元素
      voiceAudioElement.value = new Audio(voicePreview.value.url)
      voiceAudioElement.value.onended = () => {
        voicePreview.value.isPlaying = false
        voicePreview.value.playProgress = 0
        if (voiceProgressInterval.value) {
          clearInterval(voiceProgressInterval.value)
          voiceProgressInterval.value = null
        }
      }
    }

    if (voicePreview.value.isPlaying) {
      // 暂停
      voiceAudioElement.value.pause()
      if (voiceProgressInterval.value) {
        clearInterval(voiceProgressInterval.value)
        voiceProgressInterval.value = null
      }
      voicePreview.value.isPlaying = false
    } else {
      // 播放
      voiceAudioElement.value.play()
      voiceProgressInterval.value = setInterval(() => {
        if (voiceAudioElement.value && voiceAudioElement.value.duration) {
          voicePreview.value.playProgress =
            (voiceAudioElement.value.currentTime / voiceAudioElement.value.duration) * 100
        }
      }, 100)
      voicePreview.value.isPlaying = true
    }
  }

  /**
   * 停止播放
   */
  const stopVoicePlay = () => {
    if (voiceAudioElement.value) {
      voiceAudioElement.value.pause()
      voiceAudioElement.value.currentTime = 0
    }
    if (voiceProgressInterval.value) {
      clearInterval(voiceProgressInterval.value)
      voiceProgressInterval.value = null
    }
    if (voicePreview.value) {
      voicePreview.value.isPlaying = false
      voicePreview.value.playProgress = 0
    }
  }

  /**
   * 删除录音预览
   */
  const deleteVoicePreview = () => {
    if (voicePreview.value?.url) {
      URL.revokeObjectURL(voicePreview.value.url)
    }

    if (voiceAudioElement.value) {
      voiceAudioElement.value.pause()
      voiceAudioElement.value = null
    }

    if (voiceProgressInterval.value) {
      clearInterval(voiceProgressInterval.value)
      voiceProgressInterval.value = null
    }

    voicePreview.value = null

    if (onDelete) {
      onDelete()
    }
  }

  /**
   * 发送录音预览
   * @returns {Object} 录音数据 { file, duration }
   */
  const sendVoicePreview = () => {
    if (!voicePreview.value) return null

    const { blob, duration } = voicePreview.value
    const file = new File([blob], `voice_${Date.now()}.webm`, { type: 'audio/webm' })

    const result = { file, duration }

    // 清理预览
    deleteVoicePreview()

    if (onSend) {
      onSend(result)
    }

    return result
  }

  /**
   * 清理资源
   */
  const cleanup = () => {
    deleteVoicePreview()
  }

  return {
    // 状态
    voicePreview,

    // 方法
    handleVoiceRecordComplete,
    toggleVoicePlay,
    stopVoicePlay,
    deleteVoicePreview,
    sendVoicePreview,
    cleanup,

    // 工具
    formatVoiceTime
  }
}
