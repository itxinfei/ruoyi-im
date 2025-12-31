import request from '@/utils/request'

// 查询文件列表
export function listFile(query) {
  return request({
    url: '/api/im/file/list',
    method: 'get',
    params: query,
  })
}

// 查询文件详细
export function getFile(fileId) {
  return request({
    url: '/api/im/file/' + fileId,
    method: 'get',
  })
}

// 删除文件
export function delFile(fileId) {
  return request({
    url: '/api/im/file/' + fileId,
    method: 'delete',
  })
}

// 批量删除文件
export function batchDeleteFiles(fileIds) {
  return request({
    url: '/api/im/file/batch',
    method: 'delete',
    data: fileIds
  })
}

// 下载文件
export function downloadFile(fileId) {
  return request({
    url: '/api/im/file/download',
    method: 'get',
    params: { fileId },
    responseType: 'blob',
  })
}

// 获取文件预览URL
export function getFilePreviewUrl(fileId) {
  return request({
    url: '/api/im/file/preview/' + fileId,
    method: 'get',
  })
}

// 获取文件统计信息
export function getFileStats() {
  return request({
    url: '/api/im/file/statistics',
    method: 'get',
  })
}

// 获取存储空间使用情况
export function getStorageUsage() {
  return request({
    url: '/api/im/file/storage',
    method: 'get',
  })
}

// 获取文件上传配置
export function getUploadConfig() {
  return request({
    url: '/api/im/file/config',
    method: 'get',
  })
}

// 更新文件上传配置
export function updateUploadConfig(data) {
  return request({
    url: '/api/im/file/config',
    method: 'put',
    data: data,
  })
}

// 清理无效文件
export function cleanInvalidFiles() {
  return request({
    url: '/api/im/file/clean',
    method: 'delete',
  })
}

// 批量移动文件
export function moveFiles(data) {
  return request({
    url: '/api/im/file/move',
    method: 'put',
    data: data,
  })
}

// 批量复制文件
export function copyFiles(data) {
  return request({
    url: '/api/im/file/copy',
    method: 'post',
    data: data,
  })
}
