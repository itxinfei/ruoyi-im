/**
 * 文件上传 Composable
 *
 * 职责：
 * - 统一处理各类文件的上传逻辑（图片、文件、视频、语音、截图）
 * - 乐观更新消息列表
 * - 错误处理和状态管理
 *
 * 使用方式：
 * ```js
 * const { uploadImage, uploadFile, uploadVoice } = useFileUpload({
 *   messages,
 *   session,
 *   onUploadSuccess,
 *   onUploadError
 * })
 * ```
 */
import { ref } from 'vue'
import { uploadImage, uploadFile } from '@/api/im/file'

export function useFileUpload(options = {}) {
  const {
    messages = ref([]),
    session = ref(null),
    transformMsg = (m => m),
    onUploadSuccess = null,
    onUploadError = null
  } = options

  // 文件类型验证配置
  const FILE_VALIDATION = {
    image: {
      validTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/svg+xml'],
      maxSize: 10 * 1024 * 1024, // 10MB
      typeError: '支持的图片格式：JPG、PNG、GIF、WebP、SVG',
      sizeError: '图片大小不能超过10MB'
    },
    video: {
      validTypes: ['video/mp4', 'video/webm', 'video/ogg', 'video/quicktime'],
      maxSize: 100 * 1024 * 1024, // 100MB
      typeError: '支持的视频格式：MP4、WebM、OGG',
      sizeError: '视频大小不能超过100MB'
    },
    file: {
      maxSize: 100 * 1024 * 1024, // 100MB
      sizeError: '文件大小不能超过100MB'
    },
    voice: {
      maxSize: 5 * 1024 * 1024, // 5MB
      sizeError: '语音大小不能超过5MB'
    }
  }

  /**
   * 验证文件
   * @param {File} file - 文件对象
   * @param {string} category - 文件类别 (image/video/file/voice)
   * @returns {Object} { valid: boolean, error?: string }
   */
  const validateFile = (file, category = 'file') => {
    const config = FILE_VALIDATION[category]

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

  /**
   * 创建临时消息（用于乐观更新）
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
      senderId: options.currentUser?.id,
      senderName: options.currentUser?.nickName || options.currentUser?.userName || '我',
      senderAvatar: options.currentUser?.avatar,
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

  /**
   * 通用上传处理流程
   * @param {File} file - 文件对象
   * @param {string} category - 文件类别
   * @param {Function} uploadApi - 上传API函数
   * @param {Function} sendApi - 发送消息API函数
   * @param {string} messageType - 消息类型
   * @param {Function} processContent - 内容处理函数
   */
  const handleUpload = async ({
    file,
    category,
    uploadApi,
    sendApi,
    messageType,
    processContent = data => JSON.stringify(data)
  }) => {
    // 验证文件
    const validation = validateFile(file, category)
    if (!validation.valid) {
      onUploadError?.(validation.error)
      return
    }

    // 创建本地预览URL
    const blobUrl = URL.createObjectURL(file)
    const tempMsg = createTempMessage({
      type: messageType,
      url: blobUrl,
      fileName: file.name,
      size: file.size
    })

    // 乐观更新：立即显示消息
    messages.value.push(tempMsg)
    options.scrollToBottom?.()

    try {
      // 上传文件
      const formData = new FormData()
      formData.append('file', file)

      const uploadRes = await uploadApi(formData)
      if (uploadRes.code !== 200) {
        throw new Error(uploadRes.msg || 'Upload failed')
      }

      // 发送消息
      const msg = await sendApi({
        sessionId: session.value?.id,
        type: messageType.toUpperCase(),
        content: processContent({
          fileId: uploadRes.data.id,
          [`${messageType.toLowerCase()}Url`]: uploadRes.data.url,
          fileName: file.name,
          size: file.size
        })
      })

      // 更新状态
      updateMessageStatus(tempMsg.id, msg, null)
      URL.revokeObjectURL(blobUrl)
      onUploadSuccess?.(msg)
    } catch (error) {
      markMessageFailed(tempMsg.id)
      URL.revokeObjectURL(blobUrl)
      onUploadError?.(error.message || '上传失败')
      console.error(`${category}上传失败:`, error)
    }
  }

  /**
   * 上传图片
   * @param {File|FormData} fileOrFormData - 文件对象或FormData
   */
  const uploadImageFile = async fileOrFormData => {
    const file = fileOrFormData instanceof FormData
      ? fileOrFormData.get('file')
      : fileOrFormData

    if (!file) {return}

    await handleUpload({
      file,
      category: 'image',
      uploadApi: uploadImage,
      sendApi: options.sendMessage,
      messageType: 'IMAGE',
      processContent: data => JSON.stringify({
        fileId: data.fileId,
        imageUrl: data.imageUrl
      })
    })
  }

  /**
   * 上传普通文件
   * @param {File|FormData} fileOrFormData - 文件对象或FormData
   */
  const uploadNormalFile = async fileOrFormData => {
    const file = fileOrFormData instanceof FormData
      ? fileOrFormData.get('file')
      : fileOrFormData

    if (!file) {return}

    await handleUpload({
      file,
      category: 'file',
      uploadApi: uploadFile,
      sendApi: options.sendMessage,
      messageType: 'FILE',
      processContent: data => JSON.stringify({
        fileId: data.fileId,
        fileName: data.fileName,
        size: data.size,
        fileUrl: data.fileUrl
      })
    })
  }

  /**
   * 上传视频（带预览URL）
   * @param {Object} params - { file, url }
   */
  const uploadVideoFile = async ({ file, url }) => {
    const tempMsg = createTempMessage({
      type: 'VIDEO',
      url,
      fileName: file.name,
      size: file.size
    })

    messages.value.push(tempMsg)
    options.scrollToBottom?.()

    try {
      const formData = new FormData()
      formData.append('file', file)

      const res = await uploadFile(formData)
      if (res.code !== 200) {
        throw new Error(res.msg || 'Upload failed')
      }

      const msg = await options.sendMessage({
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
    } catch (error) {
      markMessageFailed(tempMsg.id)
      URL.revokeObjectURL(url)
      onUploadError?.('视频发送失败')
      console.error('视频上传失败:', error)
    }
  }

  /**
   * 上传语音
   * @param {Object} params - { file, duration }
   */
  const uploadVoiceFile = async ({ file, duration }) => {
    const tempMsg = createTempMessage({
      type: 'VOICE',
      content: JSON.stringify({ duration })
    })

    messages.value.push(tempMsg)
    options.scrollToBottom?.()

    try {
      const formData = new FormData()
      formData.append('file', file)

      const res = await uploadFile(formData)
      const voiceUrl = res.data?.fileUrl

      const msg = await options.sendMessage({
        sessionId: session.value?.id,
        type: 'VOICE',
        content: JSON.stringify({ voiceUrl, duration })
      })

      updateMessageStatus(tempMsg.id, msg, null)
      onUploadSuccess?.(msg)
    } catch (error) {
      markMessageFailed(tempMsg.id)
      onUploadError?.('语音发送失败')
      console.error('语音发送失败:', error)
    }
  }

  /**
   * 上传截图
   * @param {Blob} blob - 截图Blob对象
   * @param {string} fileName - 文件名
   */
  const uploadScreenshot = async (blob, fileName = 'screenshot.png') => {
    const file = new File([blob], fileName, { type: 'image/png' })
    await uploadImageFile(file)
  }

  return {
    // 验证方法
    validateFile,

    // 上传方法
    uploadImage: uploadImageFile,
    uploadFile: uploadNormalFile,
    uploadVideo: uploadVideoFile,
    uploadVoice: uploadVoiceFile,
    uploadScreenshot,

    // 辅助方法
    createTempMessage,
    updateMessageStatus,
    markMessageFailed
  }
}
