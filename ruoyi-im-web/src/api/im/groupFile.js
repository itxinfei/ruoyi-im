/**
 * 群文件相关 API
 */
import request from '../request'

/**
 * 获取群文件列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getGroupFiles(params) {
  return request({
    url: '/api/im/group/file/list',
    method: 'post',
    data: params
  })
}

/**
 * 获取群文件统计
 * @param {number} groupId - 群组ID
 * @returns {Promise}
 */
export function getGroupFileStatistics(groupId) {
  return request({
    url: `/api/im/group/file/statistics/${groupId}`,
    method: 'get'
  })
}

/**
 * 获取群文件分类列表
 * @param {number} groupId - 群组ID
 * @returns {Promise}
 */
export function getGroupFileCategories(groupId) {
  return request({
    url: `/api/im/group/file/categories/${groupId}`,
    method: 'get'
  })
}

/**
 * 上传群文件
 * @param {Object} data - 上传数据
 * @returns {Promise}
 */
export function uploadGroupFile(data) {
  return request({
    url: '/api/im/group/file/upload',
    method: 'post',
    data
  })
}

/**
 * 更新群文件
 * @param {number} groupFileId - 群文件ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export function updateGroupFile(groupFileId, data) {
  return request({
    url: `/api/im/group/file/${groupFileId}`,
    method: 'put',
    data
  })
}

/**
 * 删除群文件
 * @param {number} groupFileId - 群文件ID
 * @returns {Promise}
 */
export function deleteGroupFile(groupFileId) {
  return request({
    url: `/api/im/group/file/${groupFileId}`,
    method: 'delete'
  })
}

/**
 * 批量删除群文件
 * @param {Array} groupFileIds - 群文件ID列表
 * @returns {Promise}
 */
export function batchDeleteGroupFiles(groupFileIds) {
  return request({
    url: '/api/im/group/file/batch-delete',
    method: 'post',
    data: groupFileIds
  })
}

/**
 * 下载群文件
 * @param {number} groupFileId - 群文件ID
 * @returns {Promise}
 */
export function downloadGroupFile(groupFileId) {
  return request({
    url: `/api/im/group/file/download/${groupFileId}`,
    method: 'get'
  })
}

/**
 * 移动群文件到分类
 * @param {number} groupFileId - 群文件ID
 * @param {string} category - 分类名称
 * @returns {Promise}
 */
export function moveGroupFile(groupFileId, category) {
  return request({
    url: `/api/im/group/file/move/${groupFileId}`,
    method: 'put',
    params: { category }
  })
}