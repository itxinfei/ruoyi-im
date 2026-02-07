/**
 * 统一文件上传 Composable
 *
 * 整合了 useFileUpload 和 useChatUpload 的所有功能：
 * - 文件类型验证
 * - 上传队列管理
 * - 进度追踪
 * - 乐观更新（可选）
 * - 重试机制
 * - 临时消息创建
 *
 * @author ruoyi-im
 * @version 2.0.0
 */

import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadImage as uploadImageApi, uploadFile as uploadFileApi } from '@/api/im/file'

/**
 * 文件类型验证配置
 */
const FILE_VALIDATION = {
  image: {
    validTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/svg+xml'],
    maxSize: 10 * 1024 * 1024,
    typeError: '支持的图片格式：JPG、PNG、GIF、WebP、SVG',
    sizeError: '图片大小不能超过10MB'
  },
  video: {
    validTypes: ['video/mp4', 'video/webm', 'video/ogg', 'video/quicktime'],
    maxSize: 200 * 1024 * 1024,
    typeError: '支持的视频格式：MP4、WebM、OGG',
    sizeError: '视频大小不能超过200MB'
  },
  file: {
    maxSize: 100 * 1024 * 1024,
    sizeError: '文件大小不能超过100MB'
  },
  voice: {
    maxSize: 5 * 1024 * 1024,
    sizeError: '语音大小不能超过5MB'
  }
}

/**
 * 统一文件上传 Composable
 * @param {Object} options - 配置选项
 * @param {Ref<Array>} options.messages - 消息列表（用于乐观更新）
 * @param {Ref<Object>} options.session - 当前会话
 * @param {Function} options.sendMessage - 发送消息函数
 * @param {Function} options.transformMsg - 消息转换函数
 * @param {Function} options.onUploadSuccess - 上传成功回调
 * @param {Function} options.onUploadError - 上传失败回调
 * @param {Object} options.currentUser - 当前用户信息
 * @param {Function} options.scrollToBottom - 滚动到底部函数
 * @param {Boolean} options.enableQueue - 是否启用队列管理（默认 false）
 * @returns {Object} 上传相关方法和状态
 */
export function useFileUploadUnified(options = {}) {
  const {
    messages = ref([]),
    session = ref(null),
    sendMessage = null,
    transformMsg = m => m,
    onUploadSuccess = null,
    onUploadError = null,
    currentUser = null,
    scrollToBottom = null,
    enableQueue = false
  } = options

  // ========== 队列管理状态 ==========
  const uploading = ref(false)
  const uploadQueue = ref([])
  const uploadProgress = ref({})

  // ========== 计算属性 ==========
  const uploadingCount = computed(() => {
    return uploadQueue.value.filter(item => item.status === 'uploading').length
  })

  const failedCount = computed(() => {
    return uploadQueue.value.filter(item => item.status === 'failed').length
  })

  const hasUploadTask = computed(() => {
    return uploadQueue.value.length > 0
  })

  // ========== 验证方法 ==========

  /**
   * 验证文件
   * @param {File} file - 文件对象
   * @param {string} category - 文件类别 (image/video/file/voice)
   * @returns {Object} { valid: boolean, error?: string }
   */
  const validateFile = (file, category = 'file') => {
    const config = FILE_VALIDATION[category]

    if (!config) {
      return { valid: true }
    }

    // 检查文件类型
    if (config.validTypes) {
      if (!config.validTypes.some(type => file.type.includes(type))) {
        return { valid: false, error: config.typeError }
      }
    }

    // 检查文件大小
    if (file.size > config.maxSize) {
      return { valid: false, error: config.sizeError }
    }

    return { valid: true }
  }

  // ========== 临时消息管理（乐观更新） ==========

  /**
   * 创建临时消息
   * @param {Object} params - 消息参数
   * @returns {Object} 临时消息对象
   */
  const createTempMessage = params => {
    const {
      type,
      content,
      fileName = '',
      size = 0,
      url = ''
    } = params

    return {
      id: `temp-${type.toLowerCase()}-${Date.now()}`,
      type: type.toUpperCase(),
      content: url ? { [`${type.toLowerCase()}Url`]: url, fileName, size } : content,
      senderId: currentUser?.id,
      senderName: currentUser?.nickName || currentUser?.userName || '我',
      senderAvatar: currentUser?.avatar,
      timestamp: Date.now(),
      isOwn: true,
      status: 'uploading',
      readCount: 0
    }
  }

  /**
   * 更新消息状态
   * @param {string} tempId - 临时消息ID
   * @param {Object} realMsg - 真实消息对象
   * @param {string|null} status - 状态
   */
  const updateMessageStatus = (tempId, realMsg, status = null) => {
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value.splice(index, 1, { ...transformMsg(realMsg), status })
    }
  }

  /**
   * 标记消息失败
   * @param {string} tempId - 临时消息ID
   */
  const markMessageFailed = tempId => {
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
  }

  // ========== 核心上传逻辑 ==========

  /**
   * 通用上传处理流程
   * @param {Object} params - 上传参数
   */
  const handleUpload = async ({
    file,
    category,
    uploadApi,
    messageType,
    processContent = data => JSON.stringify(data),
    useOptimisticUpdate = true
  }) => {
    // 验证文件
    const validation = validateFile(file, category)
    if (!validation.valid) {
      onUploadError?.(validation.error)
      ElMessage.error(validation.error)
      return null
    }

    // 如果启用队列管理
    if (enableQueue) {
      return uploadWithQueue({ file, category, uploadApi, messageType, processContent })
    }

    // 乐观更新模式
    if (useOptimisticUpdate && sendMessage) {
      return uploadWithOptimisticUpdate({ file, category, uploadApi, messageType, processContent })
    }

    // 简单上传模式
    return uploadDirectly({ file, uploadApi })
  }

  /**
   * 队列管理模式上传
   */
  const uploadWithQueue = async ({ file, uploadApi }) => {
    const uploadId = `${Date.now()}_${file.name}`

    try {
      uploading.value = true

      // 添加到队列
      uploadQueue.value.push({
        uploadId,
        file,
        status: 'uploading',
        progress: 0
      })

      const formData = new FormData()
      formData.append('file', file)

      const response = await uploadApi(formData, progress => {
        uploadProgress.value[uploadId] = progress
      })

      if (response.code === 200) {
        const item = uploadQueue.value.find(item => item.uploadId === uploadId)
        if (item) {
          item.status = 'success'
          item.progress = 100
          item.fileUrl = response.data.fileUrl || response.data.url
          item.fileId = response.data.id || response.data.fileId
        }
        return response.data
      } else {
        throw new Error(response.msg || '上传失败')
      }
    } catch (error) {
      console.error('文件上传失败:', error)
      const item = uploadQueue.value.find(item => item.uploadId === uploadId)
      if (item) {
        item.status = 'failed'
      }
      ElMessage.error(error.message || '上传失败')
      throw error
    } finally {
      uploading.value = false
    }
  }

  /**
   * 乐观更新模式上传
   */
  const uploadWithOptimisticUpdate = async ({ file, category, uploadApi, messageType, processContent }) => {
    const blobUrl = URL.createObjectURL(file)
    const tempMsg = createTempMessage({
      type: messageType,
      url: blobUrl,
      fileName: file.name,
      size: file.size
    })

    // 立即显示消息
    messages.value.push(tempMsg)
    scrollToBottom?.()

    try {
      const formData = new FormData()
      formData.append('file', file)

      const uploadRes = await uploadApi(formData)
      if (uploadRes.code !== 200) {
        throw new Error(uploadRes.msg || 'Upload failed')
      }

      const msg = await sendMessage({
        sessionId: session.value?.id,
        type: messageType.toUpperCase(),
        content: processContent({
          fileId: uploadRes.data.id,
          [`${messageType.toLowerCase()}Url`]: uploadRes.data.url,
          fileName: file.name,
          size: file.size
        })
      })

      updateMessageStatus(tempMsg.id, msg, null)
      URL.revokeObjectURL(blobUrl)
      onUploadSuccess?.(msg)
      return msg
    } catch (error) {
      markMessageFailed(tempMsg.id)
      URL.revokeObjectURL(blobUrl)
      const errorMsg = error.message || '上传失败'
      onUploadError?.(errorMsg)
      ElMessage.error(errorMsg)
      console.error(`${category}上传失败:`, error)
      throw error
    }
  }

  /**
   * 直接上传（不使用队列或乐观更新）
   */
  const uploadDirectly = async ({ file, uploadApi }) => {
    try {
      uploading.value = true
      const formData = new FormData()
      formData.append('file', file)

      const response = await uploadApi(formData)
      if (response.code === 200) {
        ElMessage.success('上传成功')
        return response.data
      } else {
        throw new Error(response.msg || '上传失败')
      }
    } catch (error) {
      ElMessage.error(error.message || '上传失败')
      throw error
    } finally {
      uploading.value = false
    }
  }

  // ========== 公开的上传方法 ==========

  /**
   * 上传图片
   * @param {File|FormData} fileOrFormData - 文件对象或FormData
   * @param {string|number} conversationId - 会话ID（队列模式需要）
   */
  const uploadImage = async (fileOrFormData, conversationId) => {
    const file = fileOrFormData instanceof FormData
      ? fileOrFormData.get('file')
      : fileOrFormData

    if (!file) {return null}

    return handleUpload({
      file,
      category: 'image',
      uploadApi: uploadImageApi,
      messageType: 'IMAGE',
      processContent: data => JSON.stringify({
        fileId: data.fileId,
        imageUrl: data.imageUrl
      }),
      useOptimisticUpdate: !!sendMessage
    })
  }

  /**
   * 上传多个图片
   * @param {FileList|Array} files - 文件列表
   * @param {string|number} conversationId - 会话ID
   */
  const uploadImages = async (files, conversationId) => {
    const results = []

    for (const file of files) {
      try {
        const result = await uploadImage(file, conversationId)
        if (result) {results.push(result)}
      } catch (error) {
        console.error('图片上传失败:', error)
      }
    }

    if (results.length === 0) {
      ElMessage.error('所有图片上传失败')
    } else if (results.length < files.length) {
      ElMessage.warning(`${results.length}/${files.length} 张图片上传成功`)
    } else {
      ElMessage.success(`成功上传 ${results.length} 张图片`)
    }

    return results
  }

  /**
   * 上传普通文件
   * @param {File|FormData} fileOrFormData - 文件对象或FormData
   * @param {string|number} conversationId - 会话ID
   */
  const uploadFile = async (fileOrFormData, conversationId) => {
    const file = fileOrFormData instanceof FormData
      ? fileOrFormData.get('file')
      : fileOrFormData

    if (!file) {return null}

    return handleUpload({
      file,
      category: 'file',
      uploadApi: uploadFileApi,
      messageType: 'FILE',
      processContent: data => JSON.stringify({
        fileId: data.fileId,
        fileName: data.fileName,
        size: data.size,
        fileUrl: data.fileUrl
      }),
      useOptimisticUpdate: !!sendMessage
    })
  }

  /**
   * 上传视频
   * @param {Object} params - { file, url }
   */
  const uploadVideo = async ({ file, url }) => {
    const tempMsg = createTempMessage({
      type: 'VIDEO',
      url,
      fileName: file.name,
      size: file.size
    })

    messages.value.push(tempMsg)
    scrollToBottom?.()

    try {
      const formData = new FormData()
      formData.append('file', file)

      const res = await uploadFileApi(formData)
      if (res.code !== 200) {
        throw new Error(res.msg || 'Upload failed')
      }

      const msg = await sendMessage({
        sessionId: session.value?.id,
        type: 'VIDEO',
        content: JSON.stringify({
          fileId: res.data.id,
          videoUrl: res.data.url,
          fileName: file.name,
          size: file.size
        })
      })

      updateMessageStatus(tempMsg.id, msg, null)
      URL.revokeObjectURL(url)
      onUploadSuccess?.(msg)
      return msg
    } catch (error) {
      markMessageFailed(tempMsg.id)
      URL.revokeObjectURL(url)
      onUploadError?.('视频发送失败')
      ElMessage.error('视频发送失败')
      console.error('视频上传失败:', error)
      throw error
    }
  }

  /**
   * 上传语音
   * @param {Object} params - { file, duration }
   */
  const uploadVoice = async ({ file, duration }) => {
    const tempMsg = createTempMessage({
      type: 'VOICE',
      content: JSON.stringify({ duration })
    })

    messages.value.push(tempMsg)
    scrollToBottom?.()

    try {
      const formData = new FormData()
      formData.append('file', file)

      const res = await uploadFileApi(formData)
      const voiceUrl = res.data?.fileUrl

      const msg = await sendMessage({
        sessionId: session.value?.id,
        type: 'VOICE',
        content: JSON.stringify({ voiceUrl, duration })
      })

      updateMessageStatus(tempMsg.id, msg, null)
      onUploadSuccess?.(msg)
      return msg
    } catch (error) {
      markMessageFailed(tempMsg.id)
      onUploadError?.('语音发送失败')
      ElMessage.error('语音发送失败')
      console.error('语音发送失败:', error)
      throw error
    }
  }

  /**
   * 上传截图
   * @param {Blob} blob - 截图Blob对象
   * @param {string} fileName - 文件名
   */
  const uploadScreenshot = async (blob, fileName = 'screenshot.png') => {
    const file = new File([blob], fileName, { type: 'image/png' })
    return uploadImage(file)
  }

  // ========== 队列管理方法 ==========

  /**
   * 取消上传
   * @param {string} uploadId - 上传任务ID
   */
  const cancelUpload = uploadId => {
    const index = uploadQueue.value.findIndex(item => item.uploadId === uploadId)
    if (index !== -1) {
      uploadQueue.value.splice(index, 1)
      delete uploadProgress.value[uploadId]
    }
  }

  /**
   * 重试上传
   * @param {string} uploadId - 上传任务ID
   */
  const retryUpload = async uploadId => {
    const item = uploadQueue.value.find(item => item.uploadId === uploadId)
    if (!item) {return}

    item.status = 'uploading'
    item.progress = 0

    try {
      await uploadFile(item.file)
    } catch (error) {
      // 错误已在 handleUpload 中处理
    }
  }

  /**
   * 清空队列
   */
  const clearQueue = () => {
    uploadQueue.value = []
    uploadProgress.value = {}
  }

  /**
   * 获取上传进度
   * @param {string} uploadId - 上传任务ID
   */
  const getUploadProgress = uploadId => {
    return uploadProgress.value[uploadId] || 0
  }

  // ========== 导出 ==========

  return {
    // 验证方法
    validateFile,

    // 上传方法
    uploadImage,
    uploadImages,
    uploadFile,
    uploadVideo,
    uploadVoice,
    uploadScreenshot,

    // 临时消息管理
    createTempMessage,
    updateMessageStatus,
    markMessageFailed,

    // 队列状态
    uploading,
    uploadQueue,
    uploadProgress,
    uploadingCount,
    failedCount,
    hasUploadTask,

    // 队列管理
    cancelUpload,
    retryUpload,
    clearQueue,
    getUploadProgress
  }
}

// ========== 便捷导出（向后兼容） ==========

/**
 * 仅队列管理模式（不集成消息发送）
 * 用于文件选择器、云盘上传等场景
 */
export function useFileUploadQueue() {
  return useFileUploadUnified({ enableQueue: true })
}

/**
 * 仅乐观更新模式（不使用队列）
 * 用于聊天消息发送场景
 */
export function useFileUploadOptimistic(options) {
  return useFileUploadUnified({ ...options, enableQueue: false })
}
