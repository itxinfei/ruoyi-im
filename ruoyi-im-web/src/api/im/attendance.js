/**
 * 考勤打卡相关 API
 */
import request from '../request'

/**
 * 上班打卡
 * @param {Object} data - 打卡数据
 * @param {string} data.location - 打卡位置
 * @param {string} data.deviceInfo - 设备信息
 * @returns {Promise}
 */
export function checkIn(data) {
  return request({
    url: '/api/im/attendance/checkIn',
    method: 'post',
    params: data
  })
}

/**
 * 下班打卡
 * @param {Object} data - 打卡数据
 * @param {string} data.location - 打卡位置
 * @param {string} data.deviceInfo - 设备信息
 * @returns {Promise}
 */
export function checkOut(data) {
  return request({
    url: '/api/im/attendance/checkOut',
    method: 'post',
    params: data
  })
}

/**
 * 获取今日打卡状态
 * @returns {Promise}
 */
export function getTodayStatus() {
  return request({
    url: '/api/im/attendance/today',
    method: 'get'
  })
}

/**
 * 获取打卡记录详情
 * @param {number} id - 打卡ID
 * @returns {Promise}
 */
export function getAttendanceDetail(id) {
  return request({
    url: `/api/im/attendance/${id}`,
    method: 'get'
  })
}

/**
 * 获取打卡记录列表
 * @param {string} startDate - 开始日期 (yyyy-MM-dd)
 * @param {string} endDate - 结束日期 (yyyy-MM-dd)
 * @returns {Promise}
 */
export function getAttendanceList(startDate, endDate) {
  return request({
    url: '/api/im/attendance/list',
    method: 'get',
    params: { startDate, endDate }
  })
}

/**
 * 获取月度统计
 * @param {number} year - 年份
 * @param {number} month - 月份
 * @returns {Promise}
 */
export function getAttendanceStatistics(year, month) {
  return request({
    url: '/api/im/attendance/statistics',
    method: 'get',
    params: { year, month }
  })
}

/**
 * 补卡申请
 * @param {number} id - 打卡ID
 * @param {string} reason - 补卡原因
 * @returns {Promise}
 */
export function applySupplement(id, reason) {
  return request({
    url: `/api/im/attendance/${id}/supplement`,
    method: 'post',
    params: { reason }
  })
}