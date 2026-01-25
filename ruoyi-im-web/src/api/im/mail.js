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
