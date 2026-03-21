/**
 * 文件上传相关 API
 */
import request from '../request'

/**
 * 上传图片
 * @param {FormData} formData - 文件数据
 * @returns {Promise}
 */
export function uploadImage(formData) {
  return request({
    url: '/api/im/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传文件
 * @param {FormData} formData - 文件数据
 * @returns {Promise}
 */
export function uploadFile(formData) {
  return request({
    url: '/api/im/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传文件
 * @param {FormData} formData - 包含文件列表的表单数据
 * @returns {Promise}
 */
export function batchUploadFiles(formData) {
  return request({
    url: '/api/im/file/upload/batch',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 下载文件
 * @param {string} fileId - 文件ID
 * @returns {Promise}
 */
export function downloadFile(fileId) {
  return request({
    url: `/api/im/file/download/${fileId}`,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 获取文件信息
 * @param {string} fileId - 文件ID
 * @returns {Promise}
 */
export function getFileInfo(fileId) {
  return request({
    url: `/api/im/file/${fileId}`,
    method: 'get'
  })
}

/**
 * 删除文件
 * @param {string} fileId - 文件ID
 * @returns {Promise}
 */
export function deleteFile(fileId) {
  return request({
    url: `/api/im/file/${fileId}`,
    method: 'delete'
  })
}

// ==================== 分片上传 ====================

/**
 * 初始化分片上传
 * @param {Object} data - 初始化参数
 * @param {string} data.fileName - 文件名
 * @param {number} data.fileSize - 文件大小（字节）
 * @param {string} data.fileMd5 - 文件MD5
 * @param {number} data.chunkSize - 分片大小（字节）
 * @param {number} data.totalChunks - 分片总数
 * @param {string} data.conversationId - 会话ID
 * @returns {Promise}
 */
export function initChunkUpload(data) {
  return request({
    url: '/api/im/file/chunk/init',
    method: 'post',
    data
  })
}

/**
 * 上传分片
 * @param {string} uploadId - 上传ID
 * @param {number} chunkNumber - 分片序号（从1开始）
 * @param {Blob} chunk - 分片数据
 * @param {function} onProgress - 进度回调
 * @returns {Promise}
 */
export function uploadChunk(uploadId, chunkNumber, chunk, onProgress) {
  const formData = new FormData()
  formData.append('uploadId', uploadId)
  formData.append('chunkNumber', chunkNumber)
  formData.append('file', chunk)

  return request({
    url: '/api/im/file/chunk/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress
  })
}

/**
 * 合并分片
 * @param {string} uploadId - 上传ID
 * @returns {Promise}
 */
export function mergeChunks(uploadId) {
  return request({
    url: '/api/im/file/chunk/merge',
    method: 'post',
    data: { uploadId }
  })
}

/**
 * 取消分片上传
 * @param {string} uploadId - 上传ID
 * @returns {Promise}
 */
export function cancelChunkUpload(uploadId) {
  return request({
    url: `/api/im/file/chunk/cancel/${uploadId}`,
    method: 'delete'
  })
}

/**
 * 暂停分片上传
 * @param {string} uploadId - 上传ID
 * @returns {Promise}
 */
export function pauseChunkUpload(uploadId) {
  return request({
    url: `/api/im/file/chunk/pause/${uploadId}`,
    method: 'put'
  })
}

/**
 * 恢复分片上传
 * @param {string} uploadId - 上传ID
 * @returns {Promise}
 */
export function resumeChunkUpload(uploadId) {
  return request({
    url: `/api/im/file/chunk/resume/${uploadId}`,
    method: 'post'
  })
}

/**
 * 获取上传进度
 * @param {string} uploadId - 上传ID
 * @returns {Promise}
 */
export function getUploadProgress(uploadId) {
  return request({
    url: `/api/im/file/chunk/progress/${uploadId}`,
    method: 'get'
  })
}
