/**
 * 文档管理相关 API
 */
import request from '../request'

/**
 * 创建文档
 * @param {Object} data - 文档数据
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 * @param {string} data.type - 类型
 * @returns {Promise}
 */
export function createDocument(data) {
  return request({
    url: '/api/im/document/create',
    method: 'post',
    data
  })
}

/**
 * 更新文档
 * @param {Object} data - 文档数据
 * @param {number} data.id - 文档ID
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 * @returns {Promise}
 */
export function updateDocument(data) {
  return request({
    url: '/api/im/document/update',
    method: 'put',
    data
  })
}

/**
 * 删除文档
 * @param {number} documentId - 文档ID
 * @returns {Promise}
 */
export function deleteDocument(documentId) {
  return request({
    url: `/api/im/document/${documentId}`,
    method: 'delete'
  })
}

/**
 * 永久删除文档
 * @param {number} documentId - 文档ID
 * @returns {Promise}
 */
export function permanentlyDeleteDocument(documentId) {
  return request({
    url: `/api/im/document/${documentId}/permanent`,
    method: 'delete'
  })
}

/**
 * 恢复文档
 * @param {number} documentId - 文档ID
 * @returns {Promise}
 */
export function restoreDocument(documentId) {
  return request({
    url: `/api/im/document/${documentId}/restore`,
    method: 'post'
  })
}

/**
 * 获取文档详情
 * @param {number} documentId - 文档ID
 * @returns {Promise}
 */
export function getDocument(documentId) {
  return request({
    url: `/api/im/document/${documentId}`,
    method: 'get'
  })
}

/**
 * 获取文档列表
 * @param {string} type - 类型（all=全部, my=我的, shared=共享, starred=收藏, trash=回收站）
 * @returns {Promise}
 */
export function getDocuments(type = 'all') {
  return request({
    url: '/api/im/document/list',
    method: 'get',
    params: { type }
  })
}

/**
 * 搜索文档
 * @param {string} keyword - 关键词
 * @returns {Promise}
 */
export function searchDocuments(keyword) {
  return request({
    url: '/api/im/document/search',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 收藏/取消收藏文档
 * @param {number} documentId - 文档ID
 * @param {boolean} starred - 是否收藏
 * @returns {Promise}
 */
export function toggleStar(documentId, starred) {
  return request({
    url: `/api/im/document/${documentId}/star`,
    method: 'post',
    params: { starred }
  })
}

/**
 * 分享文档
 * @param {Object} data - 分享数据
 * @param {number} data.documentId - 文档ID
 * @param {number} data.targetUserId - 目标用户ID
 * @param {string} data.permission - 权限
 * @returns {Promise}
 */
export function shareDocument(data) {
  return request({
    url: '/api/im/document/share',
    method: 'post',
    data
  })
}

/**
 * 取消分享
 * @param {number} documentId - 文档ID
 * @param {number} targetUserId - 目标用户ID
 * @returns {Promise}
 */
export function unshareDocument(documentId, targetUserId) {
  return request({
    url: `/api/im/document/share/${documentId}/${targetUserId}`,
    method: 'delete'
  })
}

/**
 * 添加评论
 * @param {Object} data - 评论数据
 * @param {number} data.documentId - 文档ID
 * @param {string} data.content - 评论内容
 * @returns {Promise}
 */
export function addComment(data) {
  return request({
    url: '/api/im/document/comment',
    method: 'post',
    data
  })
}

/**
 * 删除评论
 * @param {number} commentId - 评论ID
 * @returns {Promise}
 */
export function deleteComment(commentId) {
  return request({
    url: `/api/im/document/comment/${commentId}`,
    method: 'delete'
  })
}

/**
 * 获取文档评论列表
 * @param {number} documentId - 文档ID
 * @returns {Promise}
 */
export function getDocumentComments(documentId) {
  return request({
    url: `/api/im/document/${documentId}/comments`,
    method: 'get'
  })
}

/**
 * 获取文档版本历史
 * @param {number} documentId - 文档ID
 * @returns {Promise}
 */
export function getDocumentVersions(documentId) {
  return request({
    url: `/api/im/document/${documentId}/versions`,
    method: 'get'
  })
}

/**
 * 恢复到指定版本
 * @param {number} documentId - 文档ID
 * @param {number} versionId - 版本ID
 * @returns {Promise}
 */
export function restoreVersion(documentId, versionId) {
  return request({
    url: `/api/im/document/${documentId}/versions/${versionId}/restore`,
    method: 'post'
  })
}
