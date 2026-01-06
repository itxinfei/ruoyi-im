/**
 * @file voiceRecorder.js
 * @description 语音录制工具类 - 使用 MediaRecorder API 实现语音录制功能
 * @author IM System
 * @version 1.0.0
 */

/**
 * 语音录制器类
 * @class VoiceRecorder
 * @description 封装 MediaRecorder API，提供语音录制、暂停、恢复、停止等功能
 */
class VoiceRecorder {
  /**
   * 创建语音录制器实例
   * @param {Object} options - 配置选项
   * @param {number} options.maxDuration - 最大录制时长（秒），默认60秒
   * @param {string} options.mimeType - 音频格式，默认自动检测
   * @param {number} options.audioBitsPerSecond - 音频比特率，默认128000
   * @param {Function} options.onStart - 开始录制回调
   * @param {Function} options.onStop - 停止录制回调
   * @param {Function} options.onPause - 暂停录制回调
   * @param {Function} options.onResume - 恢复录制回调
   * @param {Function} options.onError - 错误回调
   * @param {Function} options.onTimeUpdate - 时间更新回调（每秒触发）
   * @param {Function} options.onVolumeChange - 音量变化回调
   */
  constructor(options = {}) {
    // 配置选项
    this.maxDuration = options.maxDuration || 60
    this.mimeType = options.mimeType || this._getSupportedMimeType()
    this.audioBitsPerSecond = options.audioBitsPerSecond || 128000

    // 回调函数
    this.onStart = options.onStart || (() => {})
    this.onStop = options.onStop || (() => {})
    this.onPause = options.onPause || (() => {})
    this.onResume = options.onResume || (() => {})
    this.onError = options.onError || (() => {})
    this.onTimeUpdate = options.onTimeUpdate || (() => {})
    this.onVolumeChange = options.onVolumeChange || (() => {})

    // 内部状态
    this.mediaRecorder = null
    this.audioChunks = []
    this.stream = null
    this.audioContext = null
    this.analyser = null
    this.animationFrameId = null

    // 计时器
    this.startTime = 0
    this.pausedTime = 0
    this.duration = 0
    this.timerInterval = null

    // 录制状态
    this.isRecording = false
    this.isPaused = false
  }

  /**
   * 获取浏览器支持的音频 MIME 类型
   * @private
   * @returns {string} 支持的 MIME 类型
   */
  _getSupportedMimeType() {
    const types = [
      'audio/webm;codecs=opus',
      'audio/webm',
      'audio/ogg;codecs=opus',
      'audio/ogg',
      'audio/mp4',
      'audio/mpeg',
    ]

    for (const type of types) {
      if (MediaRecorder.isTypeSupported(type)) {
        return type
      }
    }

    return 'audio/webm' // 默认格式
  }

  /**
   * 检查浏览器是否支持录音功能
   * @static
   * @returns {boolean} 是否支持
   */
  static isSupported() {
    return !!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia && window.MediaRecorder)
  }

  /**
   * 请求麦克风权限
   * @async
   * @returns {Promise<MediaStream>} 音频流
   * @throws {Error} 权限被拒绝或不支持
   */
  async requestPermission() {
    if (!VoiceRecorder.isSupported()) {
      throw new Error('当前浏览器不支持录音功能')
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true, // 回声消除
          noiseSuppression: true, // 噪音抑制
          autoGainControl: true, // 自动增益
          sampleRate: 44100, // 采样率
          channelCount: 1, // 单声道
        },
      })
      return stream
    } catch (error) {
      if (error.name === 'NotAllowedError') {
        throw new Error('麦克风权限被拒绝，请在浏览器设置中允许访问麦克风')
      } else if (error.name === 'NotFoundError') {
        throw new Error('未找到麦克风设备')
      } else {
        throw new Error(`获取麦克风权限失败: ${error.message}`)
      }
    }
  }

  /**
   * 初始化音频分析器（用于音量可视化）
   * @private
   * @param {MediaStream} stream - 音频流
   */
  _initAudioAnalyser(stream) {
    try {
      this.audioContext = new (window.AudioContext || window.webkitAudioContext)()
      this.analyser = this.audioContext.createAnalyser()
      this.analyser.fftSize = 256
      this.analyser.smoothingTimeConstant = 0.8

      const source = this.audioContext.createMediaStreamSource(stream)
      source.connect(this.analyser)

      this._startVolumeMonitor()
    } catch (error) {
      console.warn('音频分析器初始化失败:', error)
    }
  }

  /**
   * 开始音量监控
   * @private
   */
  _startVolumeMonitor() {
    if (!this.analyser) return

    const dataArray = new Uint8Array(this.analyser.frequencyBinCount)

    const updateVolume = () => {
      if (!this.isRecording || this.isPaused) {
        this.animationFrameId = requestAnimationFrame(updateVolume)
        return
      }

      this.analyser.getByteFrequencyData(dataArray)

      // 计算平均音量
      let sum = 0
      for (let i = 0; i < dataArray.length; i++) {
        sum += dataArray[i]
      }
      const average = sum / dataArray.length
      const volume = Math.min(100, Math.round((average / 255) * 100))

      this.onVolumeChange(volume)
      this.animationFrameId = requestAnimationFrame(updateVolume)
    }

    this.animationFrameId = requestAnimationFrame(updateVolume)
  }

  /**
   * 停止音量监控
   * @private
   */
  _stopVolumeMonitor() {
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId)
      this.animationFrameId = null
    }
  }

  /**
   * 开始计时
   * @private
   */
  _startTimer() {
    this.startTime = Date.now() - (this.pausedTime || 0)

    this.timerInterval = setInterval(() => {
      this.duration = Math.floor((Date.now() - this.startTime) / 1000)
      this.onTimeUpdate(this.duration)

      // 检查是否达到最大时长
      if (this.duration >= this.maxDuration) {
        this.stop()
      }
    }, 1000)
  }

  /**
   * 停止计时
   * @private
   */
  _stopTimer() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval)
      this.timerInterval = null
    }
  }

  /**
   * 开始录制
   * @async
   * @returns {Promise<void>}
   */
  async start() {
    if (this.isRecording) {
      console.warn('录音已在进行中')
      return
    }

    try {
      // 获取音频流
      this.stream = await this.requestPermission()

      // 初始化音频分析器
      this._initAudioAnalyser(this.stream)

      // 创建 MediaRecorder
      this.mediaRecorder = new MediaRecorder(this.stream, {
        mimeType: this.mimeType,
        audioBitsPerSecond: this.audioBitsPerSecond,
      })

      // 重置数据
      this.audioChunks = []
      this.duration = 0
      this.pausedTime = 0

      // 监听数据可用事件
      this.mediaRecorder.ondataavailable = event => {
        if (event.data.size > 0) {
          this.audioChunks.push(event.data)
        }
      }

      // 监听录制停止事件
      this.mediaRecorder.onstop = () => {
        this._handleRecordingStop()
      }

      // 监听错误事件
      this.mediaRecorder.onerror = event => {
        this.onError(new Error(`录音错误: ${event.error?.message || '未知错误'}`))
        this._cleanup()
      }

      // 开始录制
      this.mediaRecorder.start(100) // 每100ms收集一次数据
      this.isRecording = true
      this.isPaused = false

      // 开始计时
      this._startTimer()

      this.onStart()
    } catch (error) {
      this.onError(error)
      this._cleanup()
    }
  }

  /**
   * 暂停录制
   */
  pause() {
    if (!this.isRecording || this.isPaused) return

    if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
      this.mediaRecorder.pause()
      this.isPaused = true
      this.pausedTime = Date.now() - this.startTime
      this._stopTimer()
      this.onPause()
    }
  }

  /**
   * 恢复录制
   */
  resume() {
    if (!this.isRecording || !this.isPaused) return

    if (this.mediaRecorder && this.mediaRecorder.state === 'paused') {
      this.mediaRecorder.resume()
      this.isPaused = false
      this._startTimer()
      this.onResume()
    }
  }

  /**
   * 停止录制
   */
  stop() {
    if (!this.isRecording) return

    if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
      this.mediaRecorder.stop()
    }

    this._stopTimer()
    this._stopVolumeMonitor()
    this.isRecording = false
    this.isPaused = false
  }

  /**
   * 取消录制
   */
  cancel() {
    this.audioChunks = [] // 清空已录制的数据
    this.stop()
    this._cleanup()
  }

  /**
   * 处理录制停止
   * @private
   */
  _handleRecordingStop() {
    if (this.audioChunks.length === 0) {
      this.onError(new Error('没有录制到音频数据'))
      this._cleanup()
      return
    }

    // 创建音频 Blob
    const audioBlob = new Blob(this.audioChunks, { type: this.mimeType })

    // 创建音频 URL
    const audioUrl = URL.createObjectURL(audioBlob)

    // 获取文件扩展名
    const extension = this._getExtensionFromMimeType(this.mimeType)

    // 生成文件名
    const fileName = `voice_${Date.now()}.${extension}`

    // 计算文件大小
    const fileSize = audioBlob.size

    // 回调结果
    this.onStop({
      blob: audioBlob,
      url: audioUrl,
      duration: this.duration,
      fileName,
      fileSize,
      mimeType: this.mimeType,
    })

    this._cleanup()
  }

  /**
   * 根据 MIME 类型获取文件扩展名
   * @private
   * @param {string} mimeType - MIME 类型
   * @returns {string} 文件扩展名
   */
  _getExtensionFromMimeType(mimeType) {
    const typeMap = {
      'audio/webm': 'webm',
      'audio/ogg': 'ogg',
      'audio/mp4': 'm4a',
      'audio/mpeg': 'mp3',
      'audio/wav': 'wav',
    }

    // 处理带 codecs 的 MIME 类型
    const baseType = mimeType.split(';')[0]
    return typeMap[baseType] || 'webm'
  }

  /**
   * 清理资源
   * @private
   */
  _cleanup() {
    // 停止所有音轨
    if (this.stream) {
      this.stream.getTracks().forEach(track => track.stop())
      this.stream = null
    }

    // 关闭音频上下文
    if (this.audioContext && this.audioContext.state !== 'closed') {
      this.audioContext.close()
      this.audioContext = null
    }

    this.analyser = null
    this.mediaRecorder = null
    this.isRecording = false
    this.isPaused = false
  }

  /**
   * 获取当前录制状态
   * @returns {Object} 录制状态
   */
  getState() {
    return {
      isRecording: this.isRecording,
      isPaused: this.isPaused,
      duration: this.duration,
      maxDuration: this.maxDuration,
    }
  }

  /**
   * 销毁实例
   */
  destroy() {
    this.cancel()
    this._stopTimer()
    this._stopVolumeMonitor()
    this._cleanup()
  }
}

/**
 * 格式化录音时长
 * @param {number} seconds - 秒数
 * @returns {string} 格式化后的时间字符串 (mm:ss)
 */
export function formatDuration(seconds) {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的大小字符串
 */
export function formatFileSize(bytes) {
  if (bytes < 1024) {
    return `${bytes} B`
  } else if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`
  } else {
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
  }
}

export default VoiceRecorder
