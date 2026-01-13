/**
 * 日程管理 API
 */
import request from '@/utils/request'

const BASE_URL = '/api/im/schedule'

/**
 * 获取日程列表
 * @param {Object} params - 查询参数
 */
export function getScheduleList(params) {
  return request({
    url: `${BASE_URL}/list`,
    method: 'get',
    params,
  })
}

/**
 * 创建日程
 * @param {Object} data - 日程数据
 */
export function createSchedule(data) {
  return request({
    url: `${BASE_URL}/create`,
    method: 'post',
    data,
  })
}

/**
 * 更新日程
 * @param {number} id - 日程ID
 * @param {Object} data - 日程数据
 */
export function updateSchedule(id, data) {
  return request({
    url: `${BASE_URL}/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除日程
 * @param {number} id - 日程ID
 */
export function deleteSchedule(id) {
  return request({
    url: `${BASE_URL}/${id}`,
    method: 'delete',
  })
}

/**
 * 获取日程详情
 * @param {number} id - 日程ID
 */
export function getScheduleDetail(id) {
  return request({
    url: `${BASE_URL}/${id}`,
    method: 'get',
  })
}

/**
 * 获取时间范围内的日程
 * @param {string} startTime - 开始时间
 * @param {string} endTime - 结束时间
 */
export function getSchedulesInRange(startTime, endTime) {
  return request({
    url: `${BASE_URL}/range`,
    method: 'get',
    params: { startTime, endTime },
  })
}

/**
 * 添加参与者
 * @param {number} scheduleId - 日程ID
 * @param {Array<number>} userIds - 用户ID列表
 */
export function addParticipants(scheduleId, userIds) {
  return request({
    url: `${BASE_URL}/${scheduleId}/participants`,
    method: 'post',
    data: userIds,
  })
}

/**
 * 移除参与者
 * @param {number} scheduleId - 日程ID
 * @param {number} userId - 用户ID
 */
export function removeParticipant(scheduleId, userId) {
  return request({
    url: `${BASE_URL}/${scheduleId}/participants/${userId}`,
    method: 'delete',
  })
}

/**
 * 回复参与邀请
 * @param {number} scheduleId - 日程ID
 * @param {string} response - 回复: accept/decline/tentative
 */
export function replyInvitation(scheduleId, response) {
  return request({
    url: `${BASE_URL}/${scheduleId}/response`,
    method: 'post',
    params: { response },
  })
}

/**
 * 设置提醒
 * @param {number} scheduleId - 日程ID
 * @param {Array<Object>} reminders - 提醒列表
 */
export function setReminders(scheduleId, reminders) {
  return request({
    url: `${BASE_URL}/${scheduleId}/reminders`,
    method: 'put',
    data: reminders,
  })
}

/**
 * 获取参与者的日程列表
 * @param {Array<number>} userIds - 用户ID列表
 * @param {string} startTime - 开始时间
 * @param {string} endTime - 结束时间
 */
export function getParticipantsSchedules(userIds, startTime, endTime) {
  return request({
    url: `${BASE_URL}/participants`,
    method: 'get',
    params: { userIds, startTime, endTime },
  })
}
