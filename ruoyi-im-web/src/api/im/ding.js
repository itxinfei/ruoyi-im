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
 * @param {string} data.priority - 优先级：URGENT紧急/NORMAL普通
 * @param {Array<number>} data.targetUsers - 目标用户ID列表
 * @param {number} data.expireHours - 过期时间（小时）
 * @returns {Promise}
 */
export function sendDing(data) {
  return request({
    url: '/api/im/ding/send',
    method: 'post',
    data
  })
}

/**
 * 获取接收的DING消息列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getReceivedDings(params = {}) {
  return request({
    url: '/api/im/ding/received',
    method: 'get',
    params
  })
}

/**
 * 获取发送的DING消息列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getSentDings(params = {}) {
  return request({
    url: '/api/im/ding/sent',
    method: 'get',
    params
  })
}

/**
 * 获取DING消息详情
 * @param {number} dingId - DING消息ID
 * @returns {Promise}
 */
export function getDingDetail(dingId) {
  return request({
    url: `/api/im/ding/${dingId}`,
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
    url: `/api/im/ding/${dingId}/read`,
    method: 'put'
  })
}

/**
 * 确认DING消息
 * @param {number} dingId - DING消息ID
 * @returns {Promise}
 */
export function confirmDing(dingId) {
  return request({
    url: `/api/im/ding/${dingId}/confirm`,
    method: 'put'
  })
}

/**
 * 获取DING消息回执列表
 * @param {number} dingId - DING消息ID
 * @returns {Promise}
 */
export function getDingReceipts(dingId) {
  return request({
    url: `/api/im/ding/${dingId}/receipts`,
    method: 'get'
  })
}

/**
 * 取消DING消息
 * @param {number} dingId - DING消息ID
 * @returns {Promise}
 */
export function cancelDing(dingId) {
  return request({
    url: `/api/im/ding/${dingId}/cancel`,
    method: 'put'
  })
}

/**
 * 获取DING模板列表
 * @returns {Promise}
 */
export function getDingTemplates() {
  return request({
    url: '/api/im/ding/templates',
    method: 'get'
  })
}

/**
 * 使用模板发送DING
 * @param {number} templateId - 模板ID
 * @param {Object} data - 发送数据
 * @returns {Promise}
 */
export function sendDingWithTemplate(templateId, data) {
  return request({
    url: `/api/im/ding/template/${templateId}`,
    method: 'post',
    data
  })
}
