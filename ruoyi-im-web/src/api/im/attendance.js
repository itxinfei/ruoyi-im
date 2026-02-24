/**
 * 考勤相关 API
 * 对应后端 ImAttendanceController
 * @author ruoyi
 */
import request from '../request'

/**
 * 考勤打卡
 * @param {Object} data - 打卡数据
 * @param {string} data.type - 类型 CHECK_IN/CHECK_OUT
 * @param {string} data.location - 位置
 * @param {number} data.latitude - 纬度（可选）
 * @param {number} data.longitude - 经度（可选）
 * @returns {Promise}
 */
export function checkIn(data) {
  return request({
    url: '/api/im/attendances/checkIn',
    method: 'post',
    data
  })
}

/**
 * 获取考勤记录
 * @param {Object} params - 查询参数
 * @param {string} params.date - 日期，格式：yyyy-MM-dd
 * @param {number} params.userId - 用户ID（可选）
 * @returns {Promise}
 */
export function getAttendanceRecords(params) {
  return request({
    url: '/api/im/attendances/records',
    method: 'get',
    params
  })
}

/**
 * 获取月度考勤汇总
 * @param {Object} params - 查询参数
 * @param {string} params.month - 月份，格式：yyyy-MM
 * @param {number} params.userId - 用户ID（可选）
 * @returns {Promise}
 */
export function getMonthlyAttendanceSummary(params) {
  return request({
    url: '/api/im/attendances/summary',
    method: 'get',
    params
  })
}

/**
 * 获取考勤统计
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @returns {Promise}
 */
export function getAttendanceStats(params) {
  return request({
    url: '/api/im/attendances/stats',
    method: 'get',
    params
  })
}

export default {
  checkIn,
  getAttendanceRecords,
  getMonthlyAttendanceSummary,
  getAttendanceStats
}