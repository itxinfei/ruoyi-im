/**
 * 考勤打卡相关 API
 */
import request from '../request'

/**
 * 获取今日打卡状态
 */
export function getTodayStatus() {
  return request({
    url: '/api/im/attendances/today',
    method: 'get'
  })
}

/**
 * 上班打卡
 * @param {string} location - 位置JSON
 */
export function checkIn(params) {
  return request({
    url: '/api/im/attendances/checkIn',
    method: 'post',
    params // location, deviceInfo
  })
}

/**
 * 下班打卡
 */
export function checkOut(params) {
  return request({
    url: '/api/im/attendances/checkOut',
    method: 'post',
    params
  })
}

/**
 * 获取考勤列表
 */
export function getAttendanceList(params) {
  return request({
    url: '/api/im/attendances',
    method: 'get',
    params
  })
}

/**
 * 获取月度统计
 */
export function getMonthlyStatistics(params) {
  return request({
    url: '/api/im/attendances/statistics',
    method: 'get',
    params
  })
}
