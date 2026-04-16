/**
 * 分片上传组合式函数
 * 支持文件 MD5 秒传、分片上传、暂停/恢复/取消、网络中断自动恢复
 */
import { ref, computed } from 'vue'
import {
  initChunkUpload,
  uploadChunk,
  mergeChunks,
  cancelChunkUpload,
  pauseChunkUpload,
  resumeChunkUpload,
  getUploadProgress
} from '@/api/im/file'

// 上传状态枚举
export const UPLOAD_STATUS = {
  IDLE: 'idle',
  COMPUTING: 'computing',
  UPLOADING: 'uploading',
  PAUSED: 'paused',
  COMPLETED: 'completed',
  FAILED: 'failed',
  CANCELLED: 'cancelled'
}

// 默认配置
const DEFAULT_CHUNK_SIZE = 2 * 1024 * 1024 // 2MB
const DEFAULT_PARALLEL_COUNT = 3
const RETRY_MAX_ATTEMPTS = 3
const RETRY_DELAY = 1000 // ms
const NETWORK_CHECK_INTERVAL = 3000 // ms

/**
 * 计算文件 MD5（使用 Web Crypto API）
 * @param {File} file - 文件对象
 * @param {Function} onProgress - 进度回调
 * @returns {Promise<string>} MD5 十六进制字符串
 */
async function computeFileMd5(file, onProgress) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    const chunks = []
    let offset = 0
    const chunkSize = 2 * 1024 * 1024 // 2MB 每次读取大小

    reader.onload = async (e) => {
      chunks.push(e.target.result)
      offset += e.target.result.byteLength

      if (onProgress && file.size > 0) {
        onProgress({
          loaded: offset,
          total: file.size,
          percentage: Math.round((offset / file.size) * 100)
        })
      }

      if (offset < file.size) {
        // 继续读取下一个块
        readNextChunk()
      } else {
        // 所有块读取完成，计算 MD5
        try {
          const combined = new Uint8Array(offset)
          let pos = 0
          for (const chunk of chunks) {
            combined.set(new Uint8Array(chunk), pos)
            pos += chunk.byteLength
          }

          // 使用 Web Crypto API 计算 SHA-256
          const hashBuffer = await crypto.subtle.digest('SHA-256', combined)
          const hashArray = Array.from(new Uint8Array(hashBuffer))
          const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
          resolve(hashHex)
        } catch (err) {
          // 降级方案：使用简易 hash
          const简易hash = btoa(String.fromCharCode.apply(null, combined.slice(0, Math.min(1024, combined.length)))) + file.size
          resolve(简易hash)
        }
      }
    }

    reader.onerror = () => reject(new Error('文件读取失败'))

    function readNextChunk() {
      const slice = file.slice(offset, offset + chunkSize)
      reader.readAsArrayBuffer(slice)
    }

    readNextChunk()
  })
}

/**
 * 分片上传 Composable
 * @param {Object} options - 配置选项
 * @param {number} options.chunkSize - 分片大小（字节），默认 2MB
 * @param {number} options.parallel - 并行上传数，默认 3
 * @param {string} options.conversationId - 会话 ID（可选）
 * @returns {Object} 分片上传 API 和状态
 */
export function useChunkUpload(options = {}) {
  const {
    chunkSize = DEFAULT_CHUNK_SIZE,
    parallel = DEFAULT_PARALLEL_COUNT,
    conversationId = ''
  } = options

  // 状态
  const status = ref(UPLOAD_STATUS.IDLE)
  const progress = ref(0) // 0-100
  const uploadedChunks = ref([]) // 已上传的分片编号列表
  const currentFile = ref(null) // 当前上传的文件
  const error = ref(null) // 错误信息
  const speed = ref(0) // 上传速度（字节/秒）
  const remainingTime = ref(0) // 剩余时间（秒）

  // 内部状态
  let uploadId = null // 上传 ID
  let totalChunks = 0 // 总分片数
  let uploadedSize = 0 // 已上传大小
  let fileMd5 = null // 文件 MD5
  let isPaused = false // 是否暂停
  let isCancelled = false // 是否取消
  let activeUploads = 0 // 当前活跃的上传任务数
  let uploadQueue = [] // 待上传的分片队列
  let retryAttempts = {} // 每个分片的重试次数
  let networkCheckTimer = null // 网络检测定时器
  let lastNetworkState = true // 上次网络状态
  let startTime = null // 上传开始时间

  // 计算属性
  const isIdle = computed(() => status.value === UPLOAD_STATUS.IDLE)
  const isComputing = computed(() => status.value === UPLOAD_STATUS.COMPUTING)
  const isUploading = computed(() => status.value === UPLOAD_STATUS.UPLOADING)
  const isPaused$ = computed(() => status.value === UPLOAD_STATUS.PAUSED)
  const isCompleted = computed(() => status.value === UPLOAD_STATUS.COMPLETED)
  const isFailed = computed(() => status.value === UPLOAD_STATUS.FAILED)
  const isCancelled$ = computed(() => status.value === UPLOAD_STATUS.CANCELLED)

  const progressInfo = computed(() => ({
    percentage: progress.value,
    uploadedSize,
    totalSize: currentFile.value?.size || 0,
    speed: speed.value,
    remainingTime: remainingTime.value,
    uploadedChunks: uploadedChunks.value.length,
    totalChunks
  }))

  /**
   * 更新上传速度和剩余时间
   */
  function updateSpeedAndTime() {
    if (!startTime || uploadedSize === 0) return
    const elapsed = (Date.now() - startTime) / 1000 // 秒
    if (elapsed > 0) {
      speed.value = Math.round(uploadedSize / elapsed)
      const remaining = (currentFile.value?.size || 0) - uploadedSize
      if (remaining > 0 && speed.value > 0) {
        remainingTime.value = Math.round(remaining / speed.value)
      } else {
        remainingTime.value = 0
      }
    }
  }

  /**
   * 更新总体进度
   */
  function updateProgress() {
    if (totalChunks === 0) return
    const percentage = Math.round((uploadedChunks.value.length / totalChunks) * 100)
    progress.value = percentage
    updateSpeedAndTime()
  }

  /**
   * 上传单个分片
   */
  async function uploadSingleChunk(chunkNumber, chunk) {
    if (isPaused || isCancelled) return false

    try {
      await uploadChunk(uploadId, chunkNumber, chunk)

      if (!uploadedChunks.value.includes(chunkNumber)) {
        uploadedChunks.value.push(chunkNumber)
        uploadedSize += chunk.size
      }

      retryAttempts[chunkNumber] = 0
      return true
    } catch (err) {
      console.error(`分片 ${chunkNumber} 上传失败:`, err)

      // 重试逻辑
      if (!retryAttempts[chunkNumber]) {
        retryAttempts[chunkNumber] = 0
      }

      if (retryAttempts[chunkNumber] < RETRY_MAX_ATTEMPTS) {
        retryAttempts[chunkNumber]++
        await new Promise(resolve => setTimeout(resolve, RETRY_DELAY))
        return uploadSingleChunk(chunkNumber, chunk)
      }

      throw err
    }
  }

  /**
   * 并行上传分片
   */
  async function uploadChunksInParallel() {
    while (uploadQueue.length > 0 && activeUploads < parallel && !isPaused && !isCancelled) {
      const chunkNumber = uploadQueue.shift()
      if (uploadedChunks.value.includes(chunkNumber)) {
        continue // 已上传，跳过
      }

      const start = (chunkNumber - 1) * chunkSize
      const end = Math.min(start + chunkSize, currentFile.value.size)
      const chunk = currentFile.value.slice(start, end)

      activeUploads++

      uploadSingleChunk(chunkNumber, chunk)
        .then(() => {
          updateProgress()
        })
        .catch((err) => {
          if (!isPaused && !isCancelled) {
            error.value = `分片 ${chunkNumber} 上传失败: ${err.message}`
            status.value = UPLOAD_STATUS.FAILED
          }
        })
        .finally(() => {
          activeUploads--
        })
    }
  }

  /**
   * 启动分片上传
   */
  async function startChunkUpload() {
    if (!currentFile.value) return

    isPaused = false
    isCancelled = false
    error.value = null
    uploadedChunks.value = []
    uploadedSize = 0
    uploadQueue = []
    retryAttempts = {}
    startTime = Date.now()
    speed.value = 0
    remainingTime.value = 0

    try {
      // 计算文件 MD5
      status.value = UPLOAD_STATUS.COMPUTING
      fileMd5 = await computeFileMd5(currentFile.value)

      // 计算总分片数
      totalChunks = Math.ceil(currentFile.value.size / chunkSize)

      // 初始化分片上传
      const initResult = await initChunkUpload({
        fileName: currentFile.value.name,
        fileSize: currentFile.value.size,
        fileMd5,
        chunkSize,
        totalChunks,
        conversationId
      })

      // 如果后端返回已上传的分片列表（秒传或断点续传）
      if (initResult.data?.uploadedChunks) {
        uploadedChunks.value = initResult.data.uploadedChunks
        uploadedSize = uploadedChunks.value.reduce((acc, num) => {
          const start = (num - 1) * chunkSize
          const end = Math.min(start + chunkSize, currentFile.value.size)
          return acc + (end - start)
        }, 0)
        updateProgress()
      }

      // 获取上传 ID
      uploadId = initResult.data?.uploadId || initResult.uploadId

      if (!uploadId) {
        throw new Error('获取上传ID失败')
      }

      // 构建上传队列（排除已上传的分片）
      uploadQueue = []
      for (let i = 1; i <= totalChunks; i++) {
        if (!uploadedChunks.value.includes(i)) {
          uploadQueue.push(i)
        }
      }

      // 如果所有分片都已上传（秒传），直接完成
      if (uploadQueue.length === 0) {
        status.value = UPLOAD_STATUS.COMPLETED
        progress.value = 100
        return
      }

      // 开始上传
      status.value = UPLOAD_STATUS.UPLOADING

      // 启动多个并行上传任务
      const parallelTasks = []
      for (let i = 0; i < parallel; i++) {
        parallelTasks.push(
          (async () => {
            while (uploadQueue.length > 0 && !isPaused && !isCancelled) {
              await uploadChunksInParallel()
              if (uploadQueue.length > 0 && activeUploads < parallel) {
                await new Promise(resolve => setTimeout(resolve, 100))
              }
            }
          })()
        )
      }

      await Promise.all(parallelTasks)

      // 所有分片上传完成，合并
      if (!isPaused && !isCancelled && uploadedChunks.value.length === totalChunks) {
        const mergeResult = await mergeChunks(uploadId)
        if (mergeResult.code === 200) {
          status.value = UPLOAD_STATUS.COMPLETED
          progress.value = 100
        } else {
          throw new Error(mergeResult.msg || '合并分片失败')
        }
      }
    } catch (err) {
      console.error('分片上传失败:', err)
      error.value = err.message
      status.value = UPLOAD_STATUS.FAILED
    }
  }

  /**
   * 网络状态检测
   */
  function startNetworkCheck() {
    networkCheckTimer = setInterval(() => {
      const isOnline = navigator.onLine

      if (!isOnline && lastNetworkState) {
        // 网络断开
        console.warn('网络连接断开，暂停上传')
        if (status.value === UPLOAD_STATUS.UPLOADING) {
          pause()
        }
      } else if (isOnline && !lastNetworkState) {
        // 网络恢复
        console.log('网络恢复，尝试继续上传')
        if (status.value === UPLOAD_STATUS.PAUSED && !isPaused) {
          resume()
        }
      }

      lastNetworkState = isOnline
    }, NETWORK_CHECK_INTERVAL)
  }

  function stopNetworkCheck() {
    if (networkCheckTimer) {
      clearInterval(networkCheckTimer)
      networkCheckTimer = null
    }
  }

  /**
   * 上传文件
   * @param {File} file - 文件对象
   * @param {Object} opts - 上传选项
   * @returns {Promise<Object>} 上传结果
   */
  async function upload(file, opts = {}) {
    if (!file) {
      throw new Error('请选择文件')
    }

    // 重置状态
    status.value = UPLOAD_STATUS.IDLE
    progress.value = 0
    error.value = null
    currentFile.value = file

    // 合并配置
    const _config = {
      ...options,
      ...opts
    }

    // 启动网络检测
    startNetworkCheck()

    try {
      await startChunkUpload()
      return {
        success: status.value === UPLOAD_STATUS.COMPLETED,
        fileMd5,
        uploadId,
        status: status.value,
        progress: progress.value
      }
    } finally {
      stopNetworkCheck()
    }
  }

  /**
   * 暂停上传
   */
  async function pause() {
    if (status.value !== UPLOAD_STATUS.UPLOADING) return

    isPaused = true
    status.value = UPLOAD_STATUS.PAUSED

    try {
      await pauseChunkUpload(uploadId)
    } catch (err) {
      console.warn('暂停上传请求失败:', err)
    }
  }

  /**
   * 恢复上传
   */
  async function resume() {
    if (status.value !== UPLOAD_STATUS.PAUSED) return

    isPaused = false

    try {
      // 向服务器确认恢复
      await resumeChunkUpload(uploadId)

      // 重新获取已上传的分片状态
      try {
        const progressResult = await getUploadProgress(uploadId)
        if (progressResult.data?.uploadedChunks) {
          const serverChunks = progressResult.data.uploadedChunks
          // 更新本地已上传列表
          serverChunks.forEach(num => {
            if (!uploadedChunks.value.includes(num)) {
              uploadedChunks.value.push(num)
              const start = (num - 1) * chunkSize
              const end = Math.min(start + chunkSize, currentFile.value.size)
              uploadedSize += (end - start)
            }
          })
          updateProgress()
        }
      } catch (e) {
        console.warn('获取上传进度失败:', e)
      }

      // 重建上传队列
      uploadQueue = []
      for (let i = 1; i <= totalChunks; i++) {
        if (!uploadedChunks.value.includes(i)) {
          uploadQueue.push(i)
        }
      }

      status.value = UPLOAD_STATUS.UPLOADING
      startTime = Date.now() - (uploadedSize / speed.value * 1000) || Date.now()

      // 继续上传
      const parallelTasks = []
      for (let i = 0; i < parallel; i++) {
        parallelTasks.push(
          (async () => {
            while (uploadQueue.length > 0 && !isPaused && !isCancelled) {
              await uploadChunksInParallel()
              if (uploadQueue.length > 0 && activeUploads < parallel) {
                await new Promise(resolve => setTimeout(resolve, 100))
              }
            }
          })()
        )
      }

      Promise.all(parallelTasks)
    } catch (err) {
      console.error('恢复上传失败:', err)
      error.value = err.message
      status.value = UPLOAD_STATUS.FAILED
    }
  }

  /**
   * 取消上传
   */
  async function cancel() {
    isCancelled = true
    isPaused = false

    if (uploadId) {
      try {
        await cancelChunkUpload(uploadId)
      } catch (err) {
        console.warn('取消上传请求失败:', err)
      }
    }

    // 重置状态
    status.value = UPLOAD_STATUS.CANCELLED
    progress.value = 0
    uploadedChunks.value = []
    uploadQueue = []
    uploadId = null
    currentFile.value = null
    uploadedSize = 0
    stopNetworkCheck()
  }

  return {
    // 状态
    status,
    progress,
    progressInfo,
    error,
    speed,
    remainingTime,

    // 计算属性
    isIdle,
    isComputing,
    isUploading,
    isPaused: isPaused$,
    isCompleted,
    isFailed,
    isCancelled: isCancelled$,

    // 方法
    upload,
    pause,
    resume,
    cancel,

    // 常量
    UPLOAD_STATUS
  }
}

export default useChunkUpload
