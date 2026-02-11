/**
 * 邮箱相关 API
 * @author ruoyi
 */
import request from '../request'

// ==================== 邮件基础操作 ====================

/**
 * 获取邮件列表
 * @param {string} folder - 文件夹类型 INBOX/SENT/DRAFT/STARRED/TRASH
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.keyword - 搜索关键词
 * @returns {Promise}
 */
export function getMailList(folder, params) {
  return request({
    url: '/api/im/emails',
    method: 'get',
    params: {
      ...params,
      folder: folder?.toUpperCase() || 'INBOX'
    }
  })
}

/**
 * 获取邮件详情
 * @param {number} id - 邮件ID
 * @returns {Promise}
 */
export function getMailDetail(id) {
  return request({
    url: `/api/im/emails/${id}`,
    method: 'get'
  })
}

/**
 * 发送邮件
 * @param {Object} data - 邮件数据
 * @param {Array<number>} data.toIds - 收件人ID列表
 * @param {Array<number>} data.ccIds - 抄送ID列表
 * @param {Array<number>} data.bccIds - 密送ID列表
 * @param {string} data.subject - 邮件主题
 * @param {string} data.content - 邮件内容
 * @param {Array<number>} data.attachmentIds - 附件ID列表
 * @returns {Promise}
 */
export function sendMail(data) {
  return request({
    url: '/api/im/emails',
    method: 'post',
    data: {
      toIds: data.toIds || [],
      ccIds: data.ccIds || [],
      bccIds: data.bccIds || [],
      subject: data.subject,
      content: data.content,
      attachmentIds: data.attachmentIds || []
    }
  })
}

/**
 * 保存草稿
 * @param {Object} data - 草稿数据
 * @param {string} data.subject - 邮件主题
 * @param {string} data.content - 邮件内容
 * @returns {Promise}
 */
export function saveDraft(data) {
  return request({
    url: '/api/im/emails/draft',
    method: 'post',
    data: {
      subject: data.subject,
      content: data.content
    }
  })
}

/**
 * 删除邮件（移至垃圾箱）
 * @param {number|Array<number>} id - 邮件ID或ID数组
 * @returns {Promise}
 */
export function deleteMail(id) {
  return request({
    url: '/api/im/emails',
    method: 'delete',
    data: {
      ids: Array.isArray(id) ? id : [id]
    }
  })
}

/**
 * 标记已读
 * @param {number|Array<number>} id - 邮件ID或ID数组
 * @returns {Promise}
 */
export function markAsRead(id) {
  return request({
    url: '/api/im/emails/read',
    method: 'put',
    data: {
      ids: Array.isArray(id) ? id : [id]
    }
  })
}

/**
 * 标记未读
 * @param {number|Array<number>} id - 邮件ID或ID数组
 * @returns {Promise}
 */
export function markAsUnread(id) {
  return request({
    url: '/api/im/emails/unread',
    method: 'put',
    data: {
      ids: Array.isArray(id) ? id : [id]
    }
  })
}

/**
 * 移动邮件到文件夹
 * @param {number|Array<number>} id - 邮件ID或ID数组
 * @param {string} folder - 目标文件夹
 * @returns {Promise}
 */
export function moveToFolder(id, folder) {
  return request({
    url: '/api/im/emails/move',
    method: 'put',
    params: {
      ids: Array.isArray(id) ? id : [id],
      folder: folder?.toUpperCase()
    },
    data: {
      ids: Array.isArray(id) ? id : [id]
    }
  })
}

/**
 * 星标/取消星标邮件
 * @param {number} id - 邮件ID
 * @param {boolean} starred - 是否星标
 * @returns {Promise}
 */
export function starMail(id, starred) {
  return request({
    url: `/api/im/emails/${id}/star`,
    method: 'put',
    params: { starred }
  })
}

// ==================== 附件操作 ====================

/**
 * 上传邮件附件
 * @param {FormData} formData - 文件表单数据
 * @returns {Promise}
 */
export function uploadAttachment(formData) {
  return request({
    url: '/api/im/emails/attachment/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 下载附件
 * @param {number} attachmentId - 附件ID
 * @returns {Promise}
 */
export function downloadAttachment(attachmentId) {
  return request({
    url: `/api/im/emails/attachment/${attachmentId}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 删除附件
 * @param {number} attachmentId - 附件ID
 * @returns {Promise}
 */
export function deleteAttachment(attachmentId) {
  return request({
    url: `/api/im/emails/attachment/${attachmentId}`,
    method: 'delete'
  })
}

/**
 * 永久删除邮件
 * @param {number} id - 邮件ID
 * @returns {Promise}
 */
export function permanentlyDeleteMail(id) {
  return request({
    url: `/api/im/emails/${id}/permanent`,
    method: 'delete'
  })
}

// ==================== 邮件搜索 ====================

/**
 * 搜索邮件
 * @param {Object} params - 搜索参数
 * @param {string} params.keyword - 关键词
 * @param {string} params.folder - 文件夹限制
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @returns {Promise}
 */
export function searchMail(params) {
  return request({
    url: '/api/im/emails/search',
    method: 'get',
    params
  })
}

// ==================== 统计信息 ====================

/**
 * 获取未读邮件数量
 * @returns {Promise}
 */
export function getUnreadCount() {
  return request({
    url: '/api/im/emails/unread-count',
    method: 'get'
  })
}

/**
 * 获取各文件夹邮件数量统计
 * @returns {Promise}
 */
export function getFolderStats() {
  return request({
    url: '/api/im/emails/folder-stats',
    method: 'get'
  })
}

// ==================== 邮件模板管理 ====================

/**
 * 获取邮件模板列表
 * @param {string} category - 分类（可选）
 * @returns {Promise}
 */
export function getEmailTemplates(category) {
  return request({
    url: '/api/im/emails/template/list',
    method: 'get',
    params: category ? { category } : {}
  })
}

/**
 * 获取邮件模板详情
 * @param {string} templateCode - 模板编码
 * @returns {Promise}
 */
export function getEmailTemplate(templateCode) {
  return request({
    url: `/api/im/emails/template/${templateCode}`,
    method: 'get'
  })
}

/**
 * 预览邮件模板
 * @param {string} templateCode - 模板编码
 * @returns {Promise}
 */
export function previewEmailTemplate(templateCode) {
  return request({
    url: `/api/im/emails/template/${templateCode}/preview`,
    method: 'get'
  })
}

/**
 * 创建邮件模板
 * @param {Object} data - 模板数据
 * @returns {Promise}
 */
export function createEmailTemplate(data) {
  return request({
    url: '/api/im/emails/template',
    method: 'post',
    data
  })
}

/**
 * 更新邮件模板
 * @param {number} templateId - 模板ID
 * @param {Object} data - 模板数据
 * @returns {Promise}
 */
export function updateEmailTemplate(templateId, data) {
  return request({
    url: `/api/im/emails/template/${templateId}`,
    method: 'put',
    data
  })
}

/**
 * 删除邮件模板
 * @param {number} templateId - 模板ID
 * @returns {Promise}
 */
export function deleteEmailTemplate(templateId) {
  return request({
    url: `/api/im/emails/template/${templateId}`,
    method: 'delete'
  })
}

/**
 * 启用/禁用邮件模板
 * @param {number} templateId - 模板ID
 * @param {boolean} enabled - 是否启用
 * @returns {Promise}
 */
export function setEmailTemplateEnabled(templateId, enabled) {
  return request({
    url: `/api/im/emails/template/${templateId}/enabled`,
    method: 'put',
    params: { enabled }
  })
}

/**
 * 复制邮件模板
 * @param {number} templateId - 模板ID
 * @returns {Promise}
 */
export function copyEmailTemplate(templateId) {
  return request({
    url: `/api/im/emails/template/${templateId}/copy`,
    method: 'post'
  })
}

/**
 * 获取模板变量说明
 * @param {string} templateCode - 模板编码
 * @returns {Promise}
 */
export function getEmailTemplateVariables(templateCode) {
  return request({
    url: `/api/im/emails/template/${templateCode}/variables`,
    method: 'get'
  })
}

// ==================== 用户相关 ====================

/**
 * 搜索用户（用于收件人选择）
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchUsers(keyword) {
  return request({
    url: '/api/im/user/search',
    method: 'get',
    params: { keyword }
  })
}