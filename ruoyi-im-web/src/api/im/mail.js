/**
 * 邮箱相关 API
 */
import request from '../request'

/**
 * 获取邮件列表
 * @param {string} folder - 文件夹类型 INBOX/SENT/DRAFT/STARRED/TRASH
 * @param {Object} params - 查询参数
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
 * @returns {Promise}
 */
export function sendMail(data) {
  return request({
    url: '/api/im/email/send',
    method: 'post',
    data: {
      toIds: data.toIds || [], // 后端期待 ID 列表
      subject: data.subject,
      content: data.content
    }
  })
}

/**
 * 删除邮件
 */
export function deleteMail(id) {
  return request({
    url: `/api/im/email/${id}`,
    method: 'delete'
  })
}

/**
 * 标记已读
 */
export function markAsRead(id) {
  return request({
    url: `/api/im/email/${id}/read`,
    method: 'put'
  })
}

/**
 * 移动邮件到文件夹
 */
export function moveToFolder(id, folder) {
  return request({
    url: `/api/im/email/${id}/move`,
    method: 'put',
    params: { folder: folder?.toUpperCase() }
  })
}

/**
 * 星标邮件
 */
export function starMail(id, starred) {
  return request({
    url: `/api/im/email/${id}/star`,
    method: 'put',
    params: { starred }
  })
}

/**
 * 搜索邮件
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchMail(keyword) {
  return request({
    url: '/api/im/email/search',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 回复邮件
 * @param {Object} data - { mailId, toIds, subject, content }
 * @returns {Promise}
 */
export function replyMail(data) {
  return request({
    url: '/api/im/email/reply',
    method: 'post',
    data: {
      mailId: data.mailId,
      toIds: data.toIds || [],
      subject: data.subject,
      content: data.content
    }
  })
}

/**
 * 保存草稿
 * @param {Object} data - { toIds, subject, content }
 * @returns {Promise}
 */
export function saveDraft(data) {
  return request({
    url: '/api/im/email/draft',
    method: 'post',
    data: {
      toIds: data.toIds || [],
      subject: data.subject,
      content: data.content
    }
  })
}

/**
 * 获取草稿列表
 * @returns {Promise}
 */
export function getDrafts() {
  return request({
    url: '/api/im/email/draft/list',
    method: 'get'
  })
}

/**
 * 删除草稿
 * @param {number} id - 草稿ID
 * @returns {Promise}
 */
export function deleteDraft(id) {
  return request({
    url: `/api/im/email/draft/${id}`,
    method: 'delete'
  })
}

/**
 * 获取邮件附件
 * @param {number} mailId - 邮件ID
 * @returns {Promise}
 */
export function getMailAttachments(mailId) {
  return request({
    url: `/api/im/email/${mailId}/attachments`,
    method: 'get'
  })
}

/**
 * 下载附件
 * @param {number} attachmentId - 附件ID
 * @returns {Promise}
 */
export function downloadAttachment(attachmentId) {
  return request({
    url: `/api/im/email/attachment/${attachmentId}`,
    method: 'get',
    responseType: 'blob'
  })
}
