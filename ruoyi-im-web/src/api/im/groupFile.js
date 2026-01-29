/**
 * 群组文件 API
 */
import request from '@/api/request'

/**
 * 上传群组文件
 * @param {Object} data - 上传请求
 * @param {number} data.groupId - 群组ID
 * @param {number} data.fileId - 文件ID（从文件上传接口获取）
 * @param {string} data.category - 文件分类（可选）
 * @param {string} data.description - 文件描述（可选）
 */
export function uploadGroupFile(data) {
  return request({
    url: '/im/group/file',
    method: 'post',
    data
  })
}

/**
 * 获取群组文件列表
 * @param {Object} data - 查询请求
 * @param {number} data.groupId - 群组ID
 * @param {number} data.current - 当前页
 * @param {number} data.size - 每页数量
 * @param {string} data.category - 文件分类（可选）
 * @param {string} data.fileName - 文件名（可选，模糊搜索）
 * @param {string} data.uploaderName - 上传者（可选）
 */
export function getGroupFileList(data) {
  return request({
    url: '/im/group/file/list',
    method: 'post',
    data
  })
}

/**
 * 获取群组文件统计信息
 * @param {number} groupId - 群组ID
 */
export function getGroupFileStatistics(groupId) {
  return request({
    url: `/im/group/file/statistics/${groupId}`,
    method: 'get'
  })
}

/**
 * 获取群组文件分类列表
 * @param {number} groupId - 群组ID
 */
export function getGroupFileCategories(groupId) {
  return request({
    url: `/im/group/file/categories/${groupId}`,
    method: 'get'
  })
}

/**
 * 更新群组文件信息
 * @param {number} groupFileId - 群组文件ID
 * @param {Object} data - 更新请求
 * @param {string} data.fileName - 文件名
 * @param {string} data.category - 文件分类
 * @param {string} data.description - 文件描述
 */
export function updateGroupFile(groupFileId, data) {
  return request({
    url: `/im/group/file/${groupFileId}`,
    method: 'put',
    data
  })
}

/**
 * 删除群组文件
 * @param {number} groupFileId - 群组文件ID
 */
export function deleteGroupFile(groupFileId) {
  return request({
    url: `/im/group/file/${groupFileId}`,
    method: 'delete'
  })
}

/**
 * 批量删除群组文件
 * @param {Array<number>} groupFileIds - 群组文件ID列表
 */
export function batchDeleteGroupFiles(groupFileIds) {
  return request({
    url: '/im/group/file/batch',
    method: 'delete',
    data: groupFileIds
  })
}

/**
 * 下载群组文件
 * @param {number} groupFileId - 群组文件ID
 */
export function downloadGroupFile(groupFileId) {
  return request({
    url: `/im/group/file/download/${groupFileId}`,
    method: 'get'
  })
}

/**
 * 移动文件到其他分类
 * @param {number} groupFileId - 群组文件ID
 * @param {string} category - 目标分类
 */
export function moveGroupFile(groupFileId, category) {
  return request({
    url: `/im/group/file/move/${groupFileId}`,
    method: 'put',
    params: { category }
  })
}
