/**
 * 云盘相关 API
 */
import request from '../request'

/**
 * 上传文件到云盘
 * @param {number} folderId - 文件夹ID
 * @param {FormData} file - 文件数据
 * @returns {Promise}
 */
export function uploadToCloud(folderId, file) {
  const formData = new FormData()
  formData.append('file', file)
  if (folderId !== null) {
    formData.append('folderId', folderId)
  }
  return request({
    url: '/api/im/cloud-drive/file/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 保存文件到云盘（通过 URL）
 * @param {Object} data - 文件数据
 * @param {string} data.fileName - 文件名
 * @param {string} data.fileUrl - 文件URL
 * @param {number} data.folderId - 目标文件夹ID（可选，默认根目录）
 * @returns {Promise}
 */
export function saveFileToCloud(data) {
  return request({
    url: '/api/im/cloud-drive/file/save',
    method: 'post',
    data
  })
}

/**
 * 获取文件夹列表
 * @param {number} parentId - 父文件夹ID
 * @param {string} ownerType - 所有者类型 PERSONAL/DEPARTMENT/COMPANY
 * @returns {Promise}
 */
export function getFolderList(parentId = null, ownerType = 'PERSONAL') {
  return request({
    url: '/api/im/cloud-drive/folder/list',
    method: 'get',
    params: { parentId, ownerType }
  })
}

/**
 * 创建文件夹
 * @param {Object} data - 文件夹数据
 * @param {string} data.folderName - 文件夹名称
 * @param {number} data.parentId - 父文件夹ID
 * @param {string} data.ownerType - 所有者类型
 * @returns {Promise}
 */
export function createFolder(data) {
  return request({
    url: '/api/im/cloud-drive/folder/create',
    method: 'post',
    data
  })
}

/**
 * 获取文件列表
 * @param {number} folderId - 文件夹ID，null表示根目录
 * @returns {Promise}
 */
export function getFileList(folderId = null) {
  return request({
    url: '/api/im/cloud-drive/file/list',
    method: 'get',
    params: { folderId: folderId ?? '' }
  })
}

/**
 * 获取存储配额信息
 * @returns {Promise}
 */
export function getStorageQuota() {
  return request({
    url: '/api/im/cloud-drive/quota',
    method: 'get'
  })
}

/**
 * 获取最近文件
 * @returns {Promise}
 */
export function getRecentFiles() {
  return request({
    url: '/api/im/cloud-drive/file/recent',
    method: 'get'
  })
}

/**
 * 搜索文件
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchFiles(keyword) {
  return request({
    url: '/api/im/cloud-drive/file/search',
    method: 'get',
    params: { keyword }
  })
}

export default {
  uploadToCloud,
  saveFileToCloud,
  getFolderList,
  createFolder,
  getFileList,
  getStorageQuota,
  getRecentFiles,
  searchFiles
}
