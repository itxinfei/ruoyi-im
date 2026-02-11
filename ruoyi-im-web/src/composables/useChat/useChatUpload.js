/**
 * @deprecated 已被 useChatSend 替代，不再使用
 *
 * ChatPanel - 文件上传 Composable
 *
 * 职责：
 * - 图片上传
 * - 文件上传
 * - 视频上传
 * - 上传进度管理
 * - 上传队列管理
 */

import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '@/api/im/file'

export function useChatUpload() {
  // 上传状态
  const uploading = ref(false)
  const uploadQueue = ref([])
  const uploadProgress = ref({})

  /**
   * 上传单个文件
   */
  const uploadSingleFile = async (file, conversationId, type = 'FILE') => {
    const uploadId = `${Date.now()}_${file.name}`

    try {
      uploading.value = true

      // 添加到队列
      uploadQueue.value.push({
        uploadId,
        file,
        conversationId,
        type,
        status: 'uploading',
        progress: 0
      })

      // 构造表单数据
      const formData = new FormData()
      formData.append('file', file)
      formData.append('conversationId', conversationId)
      formData.append('type', type)

      // 上传文件
      const response = await uploadFile(formData, progress => {
        uploadProgress.value[uploadId] = progress
      })

      if (response.code === 200) {
        // 更新队列状态
        const item = uploadQueue.value.find(item => item.uploadId === uploadId)
        if (item) {
          item.status = 'success'
          item.progress = 100
          item.fileUrl = response.data.fileUrl
          item.fileId = response.data.fileId
        }

        return response.data
      } else {
        throw new Error(response.msg || '上传失败')
      }
    } catch (error) {
      console.error('文件上传失败:', error)

      // 更新队列状态
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
   * 上传图片
   */
  const uploadImage = async (file, conversationId) => {
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件')
      throw new Error('文件类型错误')
    }

    // 验证文件大小（限制 10MB）
    const maxSize = 10 * 1024 * 1024
    if (file.size > maxSize) {
      ElMessage.error('图片大小不能超过 10MB')
      throw new Error('文件过大')
    }

    return uploadSingleFile(file, conversationId, 'IMAGE')
  }

  /**
   * 上传多个图片
   */
  const uploadImages = async (files, conversationId) => {
    const results = []

    for (const file of files) {
      try {
        const result = await uploadImage(file, conversationId)
        results.push(result)
      } catch (error) {
        // 继续上传其他文件
        console.error('图片上传失败:', error)
      }
    }

    if (results.length === 0) {
      ElMessage.error('所有图片上传失败')
      throw new Error('上传失败')
    }

    if (results.length < files.length) {
      ElMessage.warning(`${results.length}/${files.length} 张图片上传成功`)
    } else {
      ElMessage.success(`成功上传 ${results.length} 张图片`)
    }

    return results
  }

  /**
   * 上传普通文件
   */
  const uploadCommonFile = async (file, conversationId) => {
    // 验证文件大小（限制 100MB）
    const maxSize = 100 * 1024 * 1024
    if (file.size > maxSize) {
      ElMessage.error('文件大小不能超过 100MB')
      throw new Error('文件过大')
    }

    return uploadSingleFile(file, conversationId, 'FILE')
  }

  /**
   * 上传视频
   */
  const uploadVideo = async (file, conversationId) => {
    // 验证文件类型
    if (!file.type.startsWith('video/')) {
      ElMessage.error('请选择视频文件')
      throw new Error('文件类型错误')
    }

    // 验证文件大小（限制 200MB）
    const maxSize = 200 * 1024 * 1024
    if (file.size > maxSize) {
      ElMessage.error('视频大小不能超过 200MB')
      throw new Error('文件过大')
    }

    return uploadSingleFile(file, conversationId, 'VIDEO')
  }

  /**
   * 取消上传
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
   */
  const retryUpload = async uploadId => {
    const item = uploadQueue.value.find(item => item.uploadId === uploadId)
    if (!item) {return}

    // 重置状态
    item.status = 'uploading'
    item.progress = 0

    try {
      await uploadSingleFile(item.file, item.conversationId, item.type)
    } catch (error) {
      // 错误已在 uploadSingleFile 中处理
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
   */
  const getUploadProgress = uploadId => {
    return uploadProgress.value[uploadId] || 0
  }

  // 计算属性
  const uploadingCount = computed(() => {
    return uploadQueue.value.filter(item => item.status === 'uploading').length
  })

  const failedCount = computed(() => {
    return uploadQueue.value.filter(item => item.status === 'failed').length
  })

  const hasUploadTask = computed(() => {
    return uploadQueue.value.length > 0
  })

  return {
    // 状态
    uploading,
    uploadQueue,
    uploadProgress,
    uploadingCount,
    failedCount,
    hasUploadTask,

    // 方法
    uploadImage,
    uploadImages,
    uploadCommonFile,
    uploadVideo,
    cancelUpload,
    retryUpload,
    clearQueue,
    getUploadProgress
  }
}
