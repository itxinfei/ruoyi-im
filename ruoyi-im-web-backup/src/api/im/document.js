/**
 * 文档模块API
 * @module api/im/document
 * @description 文档管理、分享、评论、版本控制
 */
import request from '@/utils/request'

// ==================== 文档管理 ====================

/**
 * 获取文档列表
 * @param {Object} params - 查询参数
 * @param {string} [params.folderId] - 文件夹ID
 * @param {string} [params.type] - 文档类型 ALL/DOC/SHEET/MIND
 * @param {number} [params.pageNum] - 页码
 * @param {number} [params.pageSize] - 每页数量
 * @returns {Promise<Result<PageResult<Document>>>}
 */
export function getDocumentList(params) {
  return request({
    url: '/api/im/document/list',
    method: 'get',
    params,
  })
}

/**
 * 获取文档详情
 * @param {number|string} documentId - 文档ID
 * @returns {Promise<Result<Document>>}
 */
export function getDocument(documentId) {
  return request({
    url: `/api/im/document/${documentId}`,
    method: 'get',
  })
}

/**
 * 创建文档
 * @param {Object} data - 文档数据
 * @param {string} data.title - 文档标题
 * @param {string} data.type - 文档类型 DOC/SHEET/MIND
 * @param {string} [data.folderId] - 文件夹ID
 * @param {string} [data.content] - 文档内容
 * @returns {Promise<Result<number>>} 返回文档ID
 */
export function createDocument(data) {
  return request({
    url: '/api/im/document/create',
    method: 'post',
    data,
  })
}

/**
 * 更新文档
 * @param {number|string} documentId - 文档ID
 * @param {Object} data - 更新数据
 * @param {string} [data.title] - 文档标题
 * @param {string} [data.content] - 文档内容
 * @returns {Promise<Result<void>>}
 */
export function updateDocument(documentId, data) {
  return request({
    url: `/api/im/document/${documentId}`,
    method: 'put',
    data,
  })
}

/**
 * 删除文档（移至回收站）
 * @param {number|string} documentId - 文档ID
 * @returns {Promise<Result<void>>}
 */
export function deleteDocument(documentId) {
  return request({
    url: `/api/im/document/${documentId}`,
    method: 'delete',
  })
}

/**
 * 永久删除文档
 * @param {number|string} documentId - 文档ID
 * @returns {Promise<Result<void>>}
 */
export function permanentlyDeleteDocument(documentId) {
  return request({
    url: `/api/im/document/${documentId}/permanent`,
    method: 'delete',
  })
}

/**
 * 恢复文档
 * @param {number|string} documentId - 文档ID
 * @returns {Promise<Result<void>>}
 */
export function restoreDocument(documentId) {
  return request({
    url: `/api/im/document/${documentId}/restore`,
    method: 'put',
  })
}

/**
 * 搜索文档
 * @param {Object} params - 搜索参数
 * @param {string} params.keyword - 搜索关键词
 * @param {string} [params.type] - 文档类型
 * @param {number} [params.pageNum] - 页码
 * @param {number} [params.pageSize] - 每页数量
 * @returns {Promise<Result<PageResult<Document>>>}
 */
export function searchDocuments(params) {
  return request({
    url: '/api/im/document/search',
    method: 'get',
    params,
  })
}

// ==================== 文档分享 ====================

/**
 * 创建文档分享链接
 * @param {Object} data - 分享数据
 * @param {number|string} data.documentId - 文档ID
 * @param {string} [data.permission] - 权限级别 VIEW/EDIT/COMMENT
 * @param {number} [data.expireDays] - 有效期（天）
 * @returns {Promise<Result<string>>} 返回分享链接
 */
export function createShareLink(data) {
  return request({
    url: '/api/im/document/share',
    method: 'post',
    data,
  })
}

/**
 * 获取文档分享信息
 * @param {string} shareId - 分享ID
 * @returns {Promise<Result<ShareInfo>>}
 */
export function getShareInfo(shareId) {
  return request({
    url: `/api/im/document/share/${shareId}`,
    method: 'get',
  })
}

/**
 * 邀请协作
 * @param {Object} data - 邀请数据
 * @param {number|string} data.documentId - 文档ID
 * @param {number[]} data.userIds - 用户ID列表
 * @param {string} data.permission - 权限级别
 * @returns {Promise<Result<void>>}
 */
export function inviteCollaborators(data) {
  return request({
    url: '/api/im/document/collaborators',
    method: 'post',
    data,
  })
}

/**
 * 移除协作者
 * @param {number|string} documentId - 文档ID
 * @param {number|string} userId - 用户ID
 * @returns {Promise<Result<void>>}
 */
export function removeCollaborator(documentId, userId) {
  return request({
    url: `/api/im/document/${documentId}/collaborators/${userId}`,
    method: 'delete',
  })
}

// ==================== 文档评论 ====================

/**
 * 获取文档评论列表
 * @param {number|string} documentId - 文档ID
 * @returns {Promise<Result<Comment[]>>}
 */
export function getDocumentComments(documentId) {
  return request({
    url: `/api/im/document/${documentId}/comments`,
    method: 'get',
  })
}

/**
 * 添加文档评论
 * @param {number|string} documentId - 文档ID
 * @param {Object} data - 评论数据
 * @param {string} data.content - 评论内容
 * @returns {Promise<Result<number>>} 返回评论ID
 */
export function addDocumentComment(documentId, data) {
  return request({
    url: `/api/im/document/${documentId}/comments`,
    method: 'post',
    data,
  })
}

/**
 * 删除评论
 * @param {number|string} documentId - 文档ID
 * @param {number|string} commentId - 评论ID
 * @returns {Promise<Result<void>>}
 */
export function deleteDocumentComment(documentId, commentId) {
  return request({
    url: `/api/im/document/${documentId}/comments/${commentId}`,
    method: 'delete',
  })
}

// ==================== 版本管理 ====================

/**
 * 获取文档版本历史
 * @param {number|string} documentId - 文档ID
 * @returns {Promise<Result<Version[]>>}
 */
export function getDocumentVersions(documentId) {
  return request({
    url: `/api/im/document/${documentId}/versions`,
    method: 'get',
  })
}

/**
 * 创建新版本
 * @param {number|string} documentId - 文档ID
 * @param {string} content - 文档内容
 * @param {string} [comment] - 版本说明
 * @returns {Promise<Result<number>>} 返回版本ID
 */
export function createDocumentVersion(documentId, content, comment) {
  return request({
    url: `/api/im/document/${documentId}/versions`,
    method: 'post',
    data: { content, comment },
  })
}

/**
 * 恢复到指定版本
 * @param {number|string} documentId - 文档ID
 * @param {number|string} versionId - 版本ID
 * @returns {Promise<Result<void>>}
 */
export function restoreDocumentVersion(documentId, versionId) {
  return request({
    url: `/api/im/document/${documentId}/versions/${versionId}/restore`,
    method: 'put',
  })
}

// ==================== 文档收藏 ====================

/**
 * 收藏文档
 * @param {number|string} documentId - 文档ID
 * @returns {Promise<Result<void>>}
 */
export function starDocument(documentId) {
  return request({
    url: `/api/im/document/${documentId}/star`,
    method: 'put',
  })
}

/**
 * 取消收藏
 * @param {number|string} documentId - 文档ID
 * @returns {Promise<Result<void>>}
 */
export function unstarDocument(documentId) {
  return request({
    url: `/api/im/document/${documentId}/unstar`,
    method: 'put',
  })
}

/**
 * 获取收藏的文档列表
 * @returns {Promise<Result<Document[]>>}
 */
export function getStarredDocuments() {
  return request({
    url: '/api/im/document/starred',
    method: 'get',
  })
}

// 默认导出
export default {
  // 文档管理
  getDocumentList,
  getDocument,
  createDocument,
  updateDocument,
  deleteDocument,
  permanentlyDeleteDocument,
  restoreDocument,
  searchDocuments,

  // 分享
  createShareLink,
  getShareInfo,
  inviteCollaborators,
  removeCollaborator,

  // 评论
  getDocumentComments,
  addDocumentComment,
  deleteDocumentComment,

  // 版本
  getDocumentVersions,
  createDocumentVersion,
  restoreDocumentVersion,

  // 收藏
  starDocument,
  unstarDocument,
  getStarredDocuments,
}
