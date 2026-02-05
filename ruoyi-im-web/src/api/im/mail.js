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
    url: '/api/im/email/list',
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
    url: `/api/im/email/${id}`,
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
    url: '/api/im/email/send',
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
 * @returns {Promise}
 */
export function saveDraft(data) {
  return request({
    url: '/api/im/email/draft',
    method: 'post',
    data
  })
}

/**
 * 删除邮件
 * @param {number|Array<number>} id - 邮件ID或ID数组
 * @returns {Promise}
 */
export function deleteMail(id) {
  return request({
    url: '/api/im/email/delete',
    method: 'post',
    data: { ids: Array.isArray(id) ? id : [id] }
  })
}

/**
 * 标记已读
 * @param {number|Array<number>} id - 邮件ID或ID数组
 * @returns {Promise}
 */
export function markAsRead(id) {
  return request({
    url: '/api/im/email/read',
    method: 'post',
    data: { ids: Array.isArray(id) ? id : [id] }
  })
}

/**
 * 标记未读
 * @param {number|Array<number>} id - 邮件ID或ID数组
 * @returns {Promise}
 */
export function markAsUnread(id) {
  return request({
    url: '/api/im/email/unread',
    method: 'post',
    data: { ids: Array.isArray(id) ? id : [id] }
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
    url: '/api/im/email/move',
    method: 'post',
    data: {
      ids: Array.isArray(id) ? id : [id],
      folder: folder?.toUpperCase()
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
    url: `/api/im/email/${id}/star`,
    method: 'put',
    data: { starred }
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
    url: '/api/im/email/attachment/upload',
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
    url: `/api/im/email/attachment/${attachmentId}/download`,
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
    url: `/api/im/email/attachment/${attachmentId}`,
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
    url: '/api/im/email/search',
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
    url: '/api/im/email/unread-count',
    method: 'get'
  })
}

/**
 * 获取各文件夹邮件数量统计
 * @returns {Promise}
 */
export function getFolderStats() {
  return request({
    url: '/api/im/email/folder-stats',
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
