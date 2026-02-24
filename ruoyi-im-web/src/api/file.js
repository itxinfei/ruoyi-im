/**
 * 文件上传相关 API
 */
import request from './request'

/**
 * 上传文件
 * @param {File} file - 文件对象
 * @param {Object} options - 上传选项
 * @param {Function} options.onProgress - 进度回调
 * @returns {Promise<{url: string, name: string, size: number}>}
 */
export function uploadFile(file, options = {}) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/api/im/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: options.onProgress ? progressEvent => {
      const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
      options.onProgress(percent)
    } : undefined
  })
}

/**
 * 分片上传初始化
 * @param {Object} data - 初始化数据
 * @param {string} data.fileName - 文件名
 * @param {number} data.fileSize - 文件大小
 * @param {number} data.chunkSize - 分片大小
 * @returns {Promise<{uploadId: string, chunkSize: number, totalChunks: number}>}
 */
export function initChunkUpload(data) {
  return request({
    url: '/api/im/files/chunk/init',
    method: 'post',
    data
  })
}

/**
 * 上传分片
 * @param {Object} params - 上传参数
 * @param {string} params.uploadId - 上传ID
 * @param {number} params.chunkIndex - 分片索引
 * @param {Blob} params.chunk - 分片数据
 * @param {Function} onProgress - 进度回调
 * @returns {Promise}
 */
export function uploadChunk(params, onProgress) {
  const formData = new FormData()
  formData.append('uploadId', params.uploadId)
  formData.append('chunkIndex', params.chunkIndex)
  formData.append('chunk', params.chunk)

  return request({
    url: '/api/im/files/chunk/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress ? progressEvent => {
      const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
      onProgress(percent)
    } : undefined
  })
}

/**
 * 合并分片
 * @param {Object} data - 合并数据
 * @param {string} data.uploadId - 上传ID
 * @param {string} data.fileName - 文件名
 * @returns {Promise<{url: string}>}
 */
export function mergeChunks(data) {
  return request({
    url: '/api/im/files/chunk/merge',
    method: 'post',
    data
  })
}

/**
 * 获取文件下载链接
 * @param {string} fileId - 文件ID
 * @returns {Promise<{url: string, expires: number}>}
 */
export function getFileDownloadUrl(fileId) {
  return request({
    url: `/api/im/files/${fileId}/download`,
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
    url: `/api/im/files/${fileId}`,
    method: 'delete'
  })
}

/**
 * 获取文件信息
 * @param {string} fileId - 文件ID
 * @returns {Promise}
 */
export function getFileInfo(fileId) {
  return request({
    url: `/api/im/files/${fileId}`,
    method: 'get'
  })
}
