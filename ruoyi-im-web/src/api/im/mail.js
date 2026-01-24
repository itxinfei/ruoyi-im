/**
 * 邮箱相关 API
 */
import request from '../request'

/**
 * 获取邮件列表
 * @param {string} folder - 文件夹类型 inbox/sent/draft/starred/trash
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getMailList(folder, params) {
  return request({
    url: `/api/im/mail/${folder}`,
    method: 'get',
    params
  })
}

/**
 * 获取邮件详情
 * @param {number} id - 邮件ID
 * @returns {Promise}
 */
export function getMailDetail(id) {
  return request({
    url: `/api/im/mail/${id}`,
    method: 'get'
  })
}

/**
 * 发送邮件
 * @param {Object} data - 邮件数据
 * @param {string} data.to - 收件人（多个用逗号分隔）
 * @param {string} data.subject - 主题
 * @param {string} data.content - 正文
 * @param {Array} data.attachments - 附件列表
 * @returns {Promise}
 */
export function sendMail(data) {
  return request({
    url: '/api/im/mail/send',
    method: 'post',
    data
  })
}

/**
 * 删除邮件
 * @param {number} id - 邮件ID
 * @returns {Promise}
 */
export function deleteMail(id) {
  return request({
    url: `/api/im/mail/${id}`,
    method: 'delete'
  })
}

/**
 * 标记已读
 * @param {number} id - 邮件ID
 * @returns {Promise}
 */
export function markAsRead(id) {
  return request({
    url: `/api/im/mail/${id}/read`,
    method: 'put'
  })
}

/**
 * 移动邮件到文件夹
 * @param {number} id - 邮件ID
 * @param {string} folder - 目标文件夹
 * @returns {Promise}
 */
export function moveToFolder(id, folder) {
  return request({
    url: `/api/im/mail/${id}/move`,
    method: 'put',
    params: { folder }
  })
}

/**
 * 星标邮件
 * @param {number} id - 邮件ID
 * @param {boolean} starred - 是否星标
 * @returns {Promise}
 */
export function starMail(id, starred) {
  return request({
    url: `/api/im/mail/${id}/star`,
    method: 'put',
    params: { starred }
  })
}
