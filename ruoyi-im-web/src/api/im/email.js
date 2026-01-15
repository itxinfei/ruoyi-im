/**
 * 邮箱API
 * @module api/im/email
 */
import request from '@/utils/request'

/**
 * 文件夹类型
 */
export const FOLDER_TYPE = {
  INBOX: 'INBOX', // 收件箱
  SENT: 'SENT', // 已发送
  DRAFTS: 'DRAFTS', // 草稿箱
  SPAM: 'SPAM', // 垃圾邮件
  TRASH: 'TRASH', // 回收站
}

/**
 * 获取邮件列表
 * @param {Object} params - 查询参数
 * @param {string} [params.folder='INBOX'] - 文件夹类型
 * @returns {Promise<Result<Email[]>>}
 */
export function getEmailList(params = {}) {
  return request({
    url: '/api/im/email/list',
    method: 'get',
    params: {
      folder: FOLDER_TYPE.INBOX,
      ...params,
    },
  })
}

/**
 * 获取收件箱邮件
 * @returns {Promise<Result<Email[]>>}
 */
export function getInboxEmails() {
  return getEmailList({ folder: FOLDER_TYPE.INBOX })
}

/**
 * 获取已发送邮件
 * @returns {Promise<Result<Email[]>>}
 */
export function getSentEmails() {
  return getEmailList({ folder: FOLDER_TYPE.SENT })
}

/**
 * 获取草稿箱邮件
 * @returns {Promise<Result<Email[]>>}
 */
export function getDraftEmails() {
  return getEmailList({ folder: FOLDER_TYPE.DRAFTS })
}

/**
 * 获取垃圾邮件
 * @returns {Promise<Result<Email[]>>}
 */
export function getSpamEmails() {
  return getEmailList({ folder: FOLDER_TYPE.SPAM })
}

/**
 * 获取回收站邮件
 * @returns {Promise<Result<Email[]>>}
 */
export function getTrashEmails() {
  return getEmailList({ folder: FOLDER_TYPE.TRASH })
}

/**
 * 获取邮件详情
 * @param {number} emailId - 邮件ID
 * @returns {Promise<Result<Email>>}
 */
export function getEmailDetail(emailId) {
  return request({
    url: `/api/im/email/${emailId}`,
    method: 'get',
  })
}

/**
 * 发送邮件
 * @param {Object} data - 邮件数据
 * @param {number[]} data.toIds - 接收者ID列表
 * @param {string} data.subject - 邮件主题
 * @param {string} data.content - 邮件内容（HTML格式）
 * @returns {Promise<Result<number>>} 返回邮件ID
 */
export function sendEmail(data) {
  return request({
    url: '/api/im/email/send',
    method: 'post',
    data: {
      toIds: data.toIds,
      subject: data.subject,
      content: data.content,
    },
  })
}

/**
 * 保存草稿
 * @param {Object} data - 草稿数据
 * @param {string} data.subject - 邮件主题
 * @param {string} data.content - 邮件内容
 * @returns {Promise<Result<number>>} 返回邮件ID
 */
export function saveDraft(data) {
  return request({
    url: '/api/im/email/draft',
    method: 'post',
    data: {
      subject: data.subject || '',
      content: data.content || '',
    },
  })
}

/**
 * 标记邮件为已读
 * @param {number} emailId - 邮件ID
 * @returns {Promise<Result<void>>}
 */
export function markAsRead(emailId) {
  return request({
    url: `/api/im/email/${emailId}/read`,
    method: 'put',
  })
}

/**
 * 标记/取消星标
 * @param {number} emailId - 邮件ID
 * @param {boolean} starred - 是否星标
 * @returns {Promise<Result<void>>}
 */
export function markAsStarred(emailId, starred = true) {
  return request({
    url: `/api/im/email/${emailId}/star`,
    method: 'put',
    params: { starred },
  })
}

/**
 * 移至垃圾箱
 * @param {number} emailId - 邮件ID
 * @returns {Promise<Result<void>>}
 */
export function moveToTrash(emailId) {
  return request({
    url: `/api/im/email/${emailId}/trash`,
    method: 'put',
  })
}

/**
 * 永久删除邮件
 * @param {number} emailId - 邮件ID
 * @returns {Promise<Result<void>>}
 */
export function permanentlyDelete(emailId) {
  return request({
    url: `/api/im/email/${emailId}`,
    method: 'delete',
  })
}

/**
 * 获取未读邮件数量
 * @returns {Promise<Result<number>>}
 */
export function getUnreadCount() {
  return request({
    url: '/api/im/email/unread/count',
    method: 'get',
  })
}

/**
 * 批量标记已读
 * @param {number[]} emailIds - 邮件ID列表
 * @returns {Promise<Result<void>>}
 */
export function batchMarkAsRead(emailIds) {
  return Promise.all(emailIds.map(id => markAsRead(id)))
}

/**
 * 批量移至垃圾箱
 * @param {number[]} emailIds - 邮件ID列表
 * @returns {Promise<Result<void>>}
 */
export function batchMoveToTrash(emailIds) {
  return Promise.all(emailIds.map(id => moveToTrash(id)))
}

/**
 * 批量永久删除
 * @param {number[]} emailIds - 邮件ID列表
 * @returns {Promise<Result<void>>}
 */
export function batchPermanentlyDelete(emailIds) {
  return Promise.all(emailIds.map(id => permanentlyDelete(id)))
}

// 默认导出
export default {
  FOLDER_TYPE,
  getEmailList,
  getInboxEmails,
  getSentEmails,
  getDraftEmails,
  getSpamEmails,
  getTrashEmails,
  getEmailDetail,
  sendEmail,
  saveDraft,
  markAsRead,
  markAsStarred,
  moveToTrash,
  permanentlyDelete,
  getUnreadCount,
  batchMarkAsRead,
  batchMoveToTrash,
  batchPermanentlyDelete,
}
