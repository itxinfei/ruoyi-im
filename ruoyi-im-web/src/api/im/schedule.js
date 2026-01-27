/**
 * 日程管理相关 API
 * @author ruoyi
 */
import request from '../request'

// ==================== 日程管理 ====================

/**
 * 获取指定时间范围内的日程
 * @param {string} startTime - 开始时间 yyyy-MM-dd HH:mm:ss
 * @param {string} endTime - 结束时间 yyyy-MM-dd HH:mm:ss
 * @returns {Promise}
 */
export function getEventsByTimeRange(startTime, endTime) {
  return request({
    url: '/api/im/schedule/range',
    method: 'get',
    params: { startTime, endTime }
  })
}

/**
 * 创建日程
 * @param {Object} data - 日程数据
 * @param {string} data.title - 日程标题
 * @param {string} data.startTime - 开始时间
 * @param {string} data.endTime - 结束时间
 * @param {string} data.description - 描述
 * @param {string} data.location - 地点
 * @param {Array<number>} data.participantIds - 参与人ID列表
 * @param {string} data.color - 颜色标识
 * @param {string} data.reminderTime - 提醒时间
 * @returns {Promise}
 */
export function createEvent(data) {
  return request({
    url: '/api/im/schedule',
    method: 'post',
    data
  })
}

/**
 * 更新日程
 * @param {number} eventId - 日程ID
 * @param {Object} data - 日程数据
 * @returns {Promise}
 */
export function updateEvent(eventId, data) {
  return request({
    url: `/api/im/schedule/${eventId}`,
    method: 'put',
    data
  })
}

/**
 * 删除日程
 * @param {number} eventId - 日程ID
 * @returns {Promise}
 */
export function deleteEvent(eventId) {
  return request({
    url: `/api/im/schedule/${eventId}`,
    method: 'delete'
  })
}

/**
 * 获取日程详情
 * @param {number} eventId - 日程ID
 * @returns {Promise}
 */
export function getEventDetail(eventId) {
  return request({
    url: `/api/im/schedule/${eventId}`,
    method: 'get'
  })
}

/**
 * 分页查询日程列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getEventPage(params) {
  return request({
    url: '/api/im/schedule/page',
    method: 'post',
    data: params
  })
}

/**
 * 回复参与邀请
 * @param {number} eventId - 日程ID
 * @param {boolean} accepted - 是否接受
 * @returns {Promise}
 */
export function respondToInvite(eventId, accepted) {
  return request({
    url: `/api/im/schedule/${eventId}/respond`,
    method: 'put',
    params: { accepted }
  })
}

/**
 * 获取参与人列表
 * @param {number} eventId - 日程ID
 * @returns {Promise}
 */
export function getParticipants(eventId) {
  return request({
    url: `/api/im/schedule/${eventId}/participants`,
    method: 'get'
  })
}

/**
 * 取消日程
 * @param {number} eventId - 日程ID
 * @returns {Promise}
 */
export function cancelEvent(eventId) {
  return request({
    url: `/api/im/schedule/${eventId}/cancel`,
    method: 'put'
  })
}

export default {
  getEventsByTimeRange,
  createEvent,
  updateEvent,
  deleteEvent,
  getEventDetail,
  getEventPage,
  respondToInvite,
  getParticipants,
  cancelEvent
}
