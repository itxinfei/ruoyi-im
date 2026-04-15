/**
 * 日程管理相关 API
 */
import request from '../request'

/**
 * 创建日程
 * @param {Object} data - 日程数据
 * @param {string} data.title - 标题
 * @param {string} data.description - 描述
 * @param {string} data.startTime - 开始时间
 * @param {string} data.endTime - 结束时间
 * @param {string} data.location - 地点
 * @param {boolean} data.allDay - 是否全天
 * @param {string} data.reminder - 提醒方式
 * @param {Array} data.participants - 参与人ID列表
 * @returns {Promise}
 */
export function createSchedule(data) {
  return request({
    url: '/api/im/schedule',
    method: 'post',
    data
  })
}

/**
 * 更新日程
 * @param {number} eventId - 日程ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export function updateSchedule(eventId, data) {
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
export function deleteSchedule(eventId) {
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
export function getScheduleDetail(eventId) {
  return request({
    url: `/api/im/schedule/${eventId}`,
    method: 'get'
  })
}

/**
 * 分页查询日程列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 * @returns {Promise}
 */
export function getSchedulePage(params) {
  return request({
    url: '/api/im/schedule/page',
    method: 'post',
    data: params
  })
}

/**
 * 获取指定时间范围内的日程
 * @param {string} startTime - 开始时间
 * @param {string} endTime - 结束时间
 * @returns {Promise}
 */
export function getSchedulesByRange(startTime, endTime) {
  return request({
    url: '/api/im/schedule/range',
    method: 'get',
    params: { startTime, endTime }
  })
}

/**
 * 回复参与邀请
 * @param {number} eventId - 日程ID
 * @param {boolean} accepted - 是否接受
 * @returns {Promise}
 */
export function respondToSchedule(eventId, accepted) {
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
export function getScheduleParticipants(eventId) {
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
export function cancelSchedule(eventId) {
  return request({
    url: `/api/im/schedule/${eventId}/cancel`,
    method: 'put'
  })
}

// ==================== 日历共享 ====================

/**
 * 共享日历给指定用户
 * @param {Object} data - { calendarId, targetUserId, permission }
 * @returns {Promise}
 */
export function shareCalendar(data) {
  return request({
    url: '/api/im/schedule/calendar/share',
    method: 'post',
    data
  })
}

/**
 * 获取日历共享列表
 * @param {number} calendarId - 日历ID
 * @returns {Promise}
 */
export function getCalendarShares(calendarId) {
  return request({
    url: `/api/im/schedule/calendar/${calendarId}/shares`,
    method: 'get'
  })
}

/**
 * 更新日历共享权限
 * @param {number} shareId - 分享ID
 * @param {Object} data - { permission }
 * @returns {Promise}
 */
export function updateCalendarShare(shareId, data) {
  return request({
    url: `/api/im/schedule/calendar/share/${shareId}`,
    method: 'put',
    data
  })
}

/**
 * 取消日历共享
 * @param {number} shareId - 分享ID
 * @returns {Promise}
 */
export function deleteCalendarShare(shareId) {
  return request({
    url: `/api/im/schedule/calendar/share/${shareId}`,
    method: 'delete'
  })
}

/**
 * 获取已共享给我的日历列表
 * @returns {Promise}
 */
export function getSharedCalendars() {
  return request({
    url: '/api/im/schedule/calendar/shared',
    method: 'get'
  })
}

/**
 * 更新日历设置
 * @param {Object} data - { calendarId, name, color, isDefault }
 * @returns {Promise}
 */
export function updateCalendarSettings(data) {
  return request({
    url: '/api/im/schedule/calendar/settings',
    method: 'put',
    data
  })
}
