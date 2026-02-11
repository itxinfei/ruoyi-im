/**
 * DING强提醒相关 API
 */
import request from '../request'

/**
 * 发送DING消息
 * @param {Object} data - DING数据
 * @param {number} data.conversationId - 会话ID
 * @param {string} data.content - DING内容
 * @param {string} data.dingType - DING类型：APP应用内/SMS短信/CALL电话
 * @param {number} data.isUrgent - 是否紧急：0普通/1紧急
 * @param {Array<number>} data.targetUsers - 目标用户ID列表
 * @param {number} data.expireHours - 过期时间（小时）
 * @param {boolean} data.receiptRequired - 是否需要回执
 * @returns {Promise}
 */
export function sendDing(data) {
  return request({
    url: '/api/im/ding-messages/send',
    method: 'post',
    data
  })
}

/**
 * 查询DING消息列表
 * @param {Object} data - 查询参数
 * @param {number} data.conversationId - 会话ID
 * @param {string} data.dingType - DING类型
 * @param {number} data.isUrgent - 是否紧急
 * @param {string} data.status - 状态
 * @param {boolean} data.unreadOnly - 是否只查询未读
 * @param {number} data.pageNum - 页码
 * @param {number} data.pageSize - 每页数量
 * @returns {Promise}
 */
export function queryDings(data) {
  return request({
    url: '/api/im/ding-messages',
    method: 'post',
    data
  })
}

/**
 * 获取DING消息详情
 * @param {number} dingId - DING消息ID
 * @returns {Promise}
 */
export function getDingDetail(dingId) {
  return request({
    url: `/api/im/ding-messages/${dingId}`,
    method: 'get'
  })
}

/**
 * 标记DING消息为已读
 * @param {number} dingId - DING消息ID
 * @returns {Promise}
 */
export function markDingAsRead(dingId) {
  return request({
    url: `/api/im/ding-messages/${dingId}/read`,
    method: 'put'
  })
}

/**
 * 批量标记DING消息为已读
 * @param {Array<number>} dingIds - DING消息ID列表
 * @returns {Promise}
 */
export function batchMarkDingAsRead(dingIds) {
  return request({
    url: '/api/im/ding-messages/read/batch',
    method: 'put',
    data: dingIds
  })
}

/**
 * 取消DING消息
 * @param {number} dingId - DING消息ID
 * @returns {Promise}
 */
export function cancelDing(dingId) {
  return request({
    url: `/api/im/ding-messages/${dingId}/cancel`,
    method: 'put'
  })
}

/**
 * 获取未读DING消息数量
 * @returns {Promise}
 */
export function getUnreadDingCount() {
  return request({
    url: '/api/im/ding-messages/unread/count',
    method: 'get'
  })
}

/**
 * 获取DING消息已读用户列表
 * @param {number} dingId - DING消息ID
 * @returns {Promise}
 */
export function getDingReadUsers(dingId) {
  return request({
    url: `/api/im/ding-messages/${dingId}/read-users`,
    method: 'get'
  })
}

/**
 * 获取DING消息已读状态
 * @param {number} dingId - DING消息ID
 * @returns {Promise}
 */
export function getDingReadStatus(dingId) {
  return request({
    url: `/api/im/ding-messages/${dingId}/status`,
    method: 'get'
  })
}

/**
 * 确认DING消息（需要回执时使用）
 * @param {number} dingId - DING消息ID
 * @param {string} remark - 回执备注
 * @returns {Promise}
 */
export function confirmDing(dingId, remark = '') {
  return request({
    url: `/api/im/ding-messages/${dingId}/confirm`,
    method: 'put',
    data: { remark }
  })
}
