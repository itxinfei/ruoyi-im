/**
 * 云盘/文件夹管理 API
 * 对接后端 ImCloudDriveController (/api/im/cloud/*)
 */
import request from '../request'

// ==================== 文件夹管理 ====================

/**
 * 创建文件夹
 * @param {Object} data - { folderName, parentId, ownerType, accessPermission }
 * @returns {Promise}
 */
export function createFolder(data) {
  return request({
    url: '/api/im/cloud/folder/create',
    method: 'post',
    data
  })
}

/**
 * 重命名文件夹
 * @param {Number} folderId - 文件夹ID
 * @param {String} newName - 新名称
 * @returns {Promise}
 */
export function renameFolder(folderId, newName) {
  return request({
    url: `/api/im/cloud/folder/${folderId}/rename`,
    method: 'put',
    params: { newName }
  })
}

/**
 * 删除文件夹（移入回收站）
 * @param {Number} folderId - 文件夹ID
 * @returns {Promise}
 */
export function deleteFolder(folderId) {
  return request({
    url: `/api/im/cloud/folder/${folderId}`,
    method: 'delete'
  })
}

/**
 * 永久删除文件夹
 * @param {Number} folderId - 文件夹ID
 * @returns {Promise}
 */
export function permanentlyDeleteFolder(folderId) {
  return request({
    url: `/api/im/cloud/folder/${folderId}/permanent`,
    method: 'delete'
  })
}

/**
 * 恢复文件夹
 * @param {Number} folderId - 文件夹ID
 * @returns {Promise}
 */
export function restoreFolder(folderId) {
  return request({
    url: `/api/im/cloud/folder/${folderId}/restore`,
    method: 'post'
  })
}

/**
 * 获取文件夹列表
 * @param {Object} params - { parentId, ownerType }
 * @returns {Promise}
 */
export function getFolderList(params) {
  return request({
    url: '/api/im/cloud/folder/list',
    method: 'get',
    params
  })
}

/**
 * 获取文件夹路径
 * @param {Number} folderId - 文件夹ID
 * @returns {Promise}
 */
export function getFolderPath(folderId) {
  return request({
    url: `/api/im/cloud/folder/${folderId}/path`,
    method: 'get'
  })
}

// ==================== 文件管理 ====================

/**
 * 上传文件
 * @param {Object} data - FormData with file and folderId
 * @returns {Promise}
 */
export function uploadFile(data) {
  return request({
    url: '/api/im/cloud/file/upload',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 批量上传文件
 * @param {Object} data - FormData with files and folderId
 * @returns {Promise}
 */
export function uploadFiles(data) {
  return request({
    url: '/api/im/cloud/file/batch-upload',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 重命名文件
 * @param {Number} fileId - 文件ID
 * @param {String} newName - 新名称
 * @returns {Promise}
 */
export function renameFile(fileId, newName) {
  return request({
    url: `/api/im/cloud/file/${fileId}/rename`,
    method: 'put',
    params: { newName }
  })
}

/**
 * 删除文件（移入回收站）
 * @param {Number} fileId - 文件ID
 * @returns {Promise}
 */
export function deleteFile(fileId) {
  return request({
    url: `/api/im/cloud/file/${fileId}`,
    method: 'delete'
  })
}

/**
 * 永久删除文件
 * @param {Number} fileId - 文件ID
 * @returns {Promise}
 */
export function permanentlyDeleteFile(fileId) {
  return request({
    url: `/api/im/cloud/file/${fileId}/permanent`,
    method: 'delete'
  })
}

/**
 * 恢复文件
 * @param {Number} fileId - 文件ID
 * @returns {Promise}
 */
export function restoreFile(fileId) {
  return request({
    url: `/api/im/cloud/file/${fileId}/restore`,
    method: 'post'
  })
}

/**
 * 移动文件
 * @param {Object} data - { fileIds, targetFolderId }
 * @returns {Promise}
 */
export function moveFiles(data) {
  return request({
    url: '/api/im/cloud/file/move',
    method: 'post',
    data
  })
}

/**
 * 获取文件列表
 * @param {Number} folderId - 文件夹ID
 * @returns {Promise}
 */
export function getFileList(folderId) {
  return request({
    url: '/api/im/cloud/file/list',
    method: 'get',
    params: { folderId }
  })
}

/**
 * 搜索文件
 * @param {Object} params - { keyword, fileType }
 * @returns {Promise}
 */
export function searchFiles(params) {
  return request({
    url: '/api/im/cloud/file/search',
    method: 'get',
    params
  })
}

/**
 * 获取最近上传的文件
 * @param {Number} limit - 数量限制
 * @returns {Promise}
 */
export function getRecentFiles(limit = 10) {
  return request({
    url: '/api/im/cloud/file/recent',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取回收站文件列表
 * @returns {Promise}
 */
export function getRecycleBin() {
  return request({
    url: '/api/im/cloud/recycle-bin',
    method: 'get'
  })
}

/**
 * 从消息保存文件到云盘
 * @param {Object} params - { messageId, folderId }
 * @returns {Promise}
 */
export function saveFromMessage(params) {
  return request({
    url: '/api/im/cloud/file/save-from-message',
    method: 'post',
    params
  })
}

// ==================== 文件分享 ====================

/**
 * 创建分享链接
 * @param {Object} data - { fileId/folderId, fileType, expireDays, password }
 * @returns {Promise}
 */
export function createShare(data) {
  return request({
    url: '/api/im/cloud/share/create',
    method: 'post',
    data
  })
}

/**
 * 取消分享
 * @param {Number} shareId - 分享ID
 * @returns {Promise}
 */
export function cancelShare(shareId) {
  return request({
    url: `/api/im/cloud/share/${shareId}`,
    method: 'delete'
  })
}

/**
 * 获取分享列表
 * @returns {Promise}
 */
export function getShareList() {
  return request({
    url: '/api/im/cloud/share/list',
    method: 'get'
  })
}

/**
 * 访问分享
 * @param {Object} params - { shareCode, password }
 * @returns {Promise}
 */
export function accessShare(params) {
  return request({
    url: '/api/im/cloud/share/access',
    method: 'get',
    params
  })
}

// ==================== 存储配额 ====================

/**
 * 获取存储配额
 * @returns {Promise}
 */
export function getQuota() {
  return request({
    url: '/api/im/cloud/quota',
    method: 'get'
  })
}

// ==================== 文件收藏 ====================

/**
 * 切换文件收藏状态
 * @param {number} fileId - 文件ID
 * @param {boolean} starred - 是否收藏
 * @returns {Promise}
 */
export function toggleFileStar(fileId, starred) {
  return request({
    url: `/api/im/cloud/file/${fileId}/star`,
    method: 'put',
    data: { starred }
  })
}

/**
 * 切换文件夹收藏状态
 * @param {number} folderId - 文件夹ID
 * @param {boolean} starred - 是否收藏
 * @returns {Promise}
 */
export function toggleFolderStar(folderId, starred) {
  return request({
    url: `/api/im/cloud/folder/${folderId}/star`,
    method: 'put',
    data: { starred }
  })
}
