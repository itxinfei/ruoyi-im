/**
 * 会议室管理 API
 */
import request from '@/utils/request'

const BASE_URL = '/api/im/meeting-room'

/**
 * 创建会议室
 * @param {Object} data - 会议室数据
 */
export function createMeetingRoom(data) {
  return request({
    url: `${BASE_URL}/create`,
    method: 'post',
    data,
  })
}

/**
 * 更新会议室
 * @param {Object} data - 会议室数据
 */
export function updateMeetingRoom(data) {
  return request({
    url: `${BASE_URL}`,
    method: 'put',
    data,
  })
}

/**
 * 删除会议室
 * @param {number} roomId - 会议室ID
 */
export function deleteMeetingRoom(roomId) {
  return request({
    url: `${BASE_URL}/${roomId}`,
    method: 'delete',
  })
}

/**
 * 获取会议室详情
 * @param {number} roomId - 会议室ID
 */
export function getMeetingRoom(roomId) {
  return request({
    url: `${BASE_URL}/${roomId}`,
    method: 'get',
  })
}

/**
 * 分页查询会议室列表
 * @param {Object} data - 查询条件
 */
export function getMeetingRoomPage(data) {
  return request({
    url: `${BASE_URL}/page`,
    method: 'post',
    data,
  })
}

/**
 * 获取可用会议室列表
 */
export function getAvailableRooms() {
  return request({
    url: `${BASE_URL}/available`,
    method: 'get',
  })
}

/**
 * 预订会议室
 * @param {Object} data - 预订数据
 */
export function bookMeetingRoom(data) {
  return request({
    url: `${BASE_URL}/book`,
    method: 'post',
    data,
  })
}

/**
 * 取消预订
 * @param {number} bookingId - 预订ID
 */
export function cancelBooking(bookingId) {
  return request({
    url: `${BASE_URL}/booking/${bookingId}/cancel`,
    method: 'post',
  })
}

/**
 * 确认预订
 * @param {number} bookingId - 预订ID
 */
export function confirmBooking(bookingId) {
  return request({
    url: `${BASE_URL}/booking/${bookingId}/confirm`,
    method: 'post',
  })
}

/**
 * 签到
 * @param {number} bookingId - 预订ID
 */
export function checkIn(bookingId) {
  return request({
    url: `${BASE_URL}/booking/${bookingId}/check-in`,
    method: 'post',
  })
}

/**
 * 签退
 * @param {number} bookingId - 预订ID
 */
export function checkOut(bookingId) {
  return request({
    url: `${BASE_URL}/booking/${bookingId}/check-out`,
    method: 'post',
  })
}

/**
 * 获取预订详情
 * @param {number} bookingId - 预订ID
 */
export function getBookingDetail(bookingId) {
  return request({
    url: `${BASE_URL}/booking/${bookingId}`,
    method: 'get',
  })
}

/**
 * 获取用户的预订列表
 */
export function getMyBookings() {
  return request({
    url: `${BASE_URL}/booking/my`,
    method: 'get',
  })
}

/**
 * 获取会议室日程
 * @param {number} roomId - 会议室ID
 * @param {string} date - 日期 (yyyy-MM-dd)
 */
export function getRoomSchedule(roomId, date) {
  return request({
    url: `${BASE_URL}/${roomId}/schedule`,
    method: 'get',
    params: { date },
  })
}

/**
 * 检查会议室可用性
 * @param {number} roomId - 会议室ID
 * @param {string} startTime - 开始时间
 * @param {string} endTime - 结束时间
 */
export function checkAvailability(roomId, startTime, endTime) {
  return request({
    url: `${BASE_URL}/${roomId}/availability`,
    method: 'get',
    params: { startTime, endTime },
  })
}

/**
 * 提交会议反馈
 * @param {number} bookingId - 预订ID
 * @param {string} feedback - 反馈内容
 * @param {number} rating - 评分 (1-5)
 */
export function submitFeedback(bookingId, feedback, rating) {
  return request({
    url: `${BASE_URL}/booking/${bookingId}/feedback`,
    method: 'post',
    params: { feedback, rating },
  })
}

/**
 * 获取会议室统计数据
 */
export function getMeetingRoomStatistics() {
  return request({
    url: `${BASE_URL}/statistics`,
    method: 'get',
  })
}
