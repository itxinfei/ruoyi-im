/**
 * 文档管理 API
 */
import request from '../request'

/**
 * 获取文档列表
 */
export function getDocumentList(params) {
  return request({
    url: '/api/im/document/list',
    method: 'get',
    params
  })
}

/**
 * 获取文档详情
 */
export function getDocumentDetail(id) {
  return request({
    url: `/api/im/document/${id}`,
    method: 'get'
  })
}

/**
 * 创建文档
 */
export function createDocument(data) {
  return request({
    url: '/api/im/document/create',
    method: 'post',
    data
  })
}

/**
 * 更新文档内容
 */
export function updateDocument(id, data) {
  return request({
    url: `/api/im/document/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除文档
 */
export function deleteDocument(id) {
  return request({
    url: `/api/im/document/${id}`,
    method: 'delete'
  })
}

/**
 * 获取文档版本历史
 */
export function getDocumentVersions(id) {
  return request({
    url: `/api/im/document/${id}/versions`,
    method: 'get'
  })
}

/**
 * 恢复到指定版本
 */
export function restoreDocumentVersion(id, versionId) {
  return request({
    url: `/api/im/document/${id}/versions/${versionId}/restore`,
    method: 'post'
  })
}

/**
 * 分享文档
 */
export function shareDocument(data) {
  return request({
    url: '/api/im/document/share',
    method: 'post',
    data
  })
}

/**
 * 获取文档评论
 */
export function getDocumentComments(id) {
  return request({
    url: `/api/im/document/${id}/comments`,
    method: 'get'
  })
}

/**
 * 添加文档评论
 */
export function addDocumentComment(id, data) {
  return request({
    url: `/api/im/document/${id}/comments`,
    method: 'post',
    data
  })
}

// ========== 文档协作相关 API ==========

/**
 * 添加协作者
 */
export function addCollaborators(data) {
  return request({
    url: '/api/im/document/collaboration/collaborators/add',
    method: 'post',
    data
  })
}

/**
 * 移除协作者
 */
export function removeCollaborator(documentId, targetUserId) {
  return request({
    url: `/api/im/document/collaboration/${documentId}/collaborators/${targetUserId}`,
    method: 'delete'
  })
}

/**
 * 更新协作者权限
 */
export function updateCollaboratorPermission(documentId, targetUserId, permission) {
  return request({
    url: `/api/im/document/collaboration/${documentId}/collaborators/${targetUserId}/permission`,
    method: 'put',
    params: { permission }
  })
}

/**
 * 获取协作者列表
 */
export function getCollaborators(documentId) {
  return request({
    url: `/api/im/document/collaboration/${documentId}/collaborators`,
    method: 'get'
  })
}

/**
 * 加入文档编辑
 */
export function joinDocument(documentId) {
  return request({
    url: `/api/im/document/collaboration/${documentId}/join`,
    method: 'post'
  })
}

/**
 * 离开文档编辑
 */
export function leaveDocument(documentId) {
  return request({
    url: `/api/im/document/collaboration/${documentId}/leave`,
    method: 'post'
  })
}

/**
 * 更新光标位置
 */
export function updateCursor(documentId, position, selection) {
  return request({
    url: `/api/im/document/collaboration/${documentId}/cursor`,
    method: 'put',
    params: { position, selection }
  })
}

/**
 * 获取在线编辑者
 */
export function getOnlineEditors(documentId) {
  return request({
    url: `/api/im/document/collaboration/${documentId}/online`,
    method: 'get'
  })
}

/**
 * 心跳保活
 */
export function sendHeartbeat(documentId) {
  return request({
    url: `/api/im/document/collaboration/${documentId}/heartbeat`,
    method: 'post'
  })
}

/**
 * 获取操作日志
 */
export function getOperationLogs(documentId, limit = 100) {
  return request({
    url: `/api/im/document/collaboration/${documentId}/logs`,
    method: 'get',
    params: { limit }
  })
}

export default {
  getDocumentList,
  getDocumentDetail,
  createDocument,
  updateDocument,
  deleteDocument,
  getDocumentVersions,
  restoreDocumentVersion,
  shareDocument,
  getDocumentComments,
  addDocumentComment,
  addCollaborators,
  removeCollaborator,
  updateCollaboratorPermission,
  getCollaborators,
  joinDocument,
  leaveDocument,
  updateCursor,
  getOnlineEditors,
  sendHeartbeat,
  getOperationLogs
}
