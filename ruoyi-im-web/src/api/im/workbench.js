/**
 * 工作台相关 API
 * 对应后端 ImWorkbenchController
 * @author ruoyi
 */
import request from '../request'
import { checkIn as attendanceCheckIn, getAttendanceRecords as getAttendance } from './attendance'

/**
 * 获取待办列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @returns {Promise}
 */
export function getTodos(params) {
  return request({
    url: '/api/im/workbench/todos',
    method: 'get',
    params
  })
}

/**
 * 获取工作台数据概览
 * @returns {Promise}
 */
export function getOverview() {
  return request({
    url: '/api/im/workbench/overview',
    method: 'get'
  })
}

/**
 * 创建待办
 * @param {Object} data - 待办数据
 * @param {string} data.title - 标题
 * @param {string} data.description - 描述
 * @param {string} data.type - 类型（可选，默认 TASK）
 * @param {number} data.relatedId - 关联ID（可选）
 * @param {number} data.priority - 优先级（可选，1=低, 2=中, 3=高）
 * @param {string} data.dueDate - 截止日期（可选，格式：yyyy-MM-dd HH:mm:ss）
 * @returns {Promise}
 */
export function createTodo(data) {
  return request({
    url: '/api/im/workbench/todos',
    method: 'post',
    data
  })
}

/**
 * 完成待办
 * @param {number} todoId - 待办ID
 * @returns {Promise}
 */
export function completeTodo(todoId) {
  return request({
    url: `/api/im/workbench/todos/${todoId}/complete`,
    method: 'put'
  })
}

/**
 * 更新待办
 * @param {number} todoId - 待办ID
 * @param {Object} data - 待办数据
 * @param {string} data.title - 标题
 * @param {string} data.description - 描述
 * @param {number} data.priority - 优先级（可选，1=低, 2=中, 3=高）
 * @param {string} data.dueDate - 截止日期（可选，格式：yyyy-MM-dd HH:mm:ss）
 * @returns {Promise}
 */
export function updateTodo(todoId, data) {
  return request({
    url: `/api/im/workbench/todos/${todoId}`,
    method: 'put',
    data
  })
}

/**
 * 删除待办
 * @param {number} todoId - 待办ID
 * @returns {Promise}
 */
export function deleteTodo(todoId) {
  return request({
    url: `/api/im/workbench/todos/${todoId}`,
    method: 'delete'
  })
}

/**
 * 获取审批列表
 * @param {Object} params - 查询参数
 * @param {string} params.status - 状态 PENDING/APPROVED/REJECTED
 * @returns {Promise}
 */
export function getApprovals(params) {
  return request({
    url: '/api/im/approvals/pending',
    method: 'get',
    params
  })
}

/**
 * 审批处理
 * @param {Object} data - 审批数据
 * @param {number} data.approvalId - 审批ID
 * @param {string} data.action - 操作 APPROVE/REJECT
 * @param {string} data.comment - 审批意见
 * @returns {Promise}
 */
export function handleApproval(data) {
  return request({
    url: '/api/im/approvals/handle',
    method: 'post',
    data
  })
}

/**
 * 考勤打卡
 * @param {Object} data - 打卡数据
 * @param {string} data.type - 类型 CHECK_IN/CHECK_OUT
 * @param {string} data.location - 位置
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
 * @param {string} params.date - 日期
 * @returns {Promise}
 */
export function getAttendance(params) {
  return request({
    url: '/api/im/attendances/records',
    method: 'get',
    params
  })
}

/**
 * 获取公告列表
 * @returns {Promise}
 */
export function getAnnouncements() {
  return request({
    url: '/api/im/announcements/latest',
    method: 'get'
  })
}

/**
 * 获取统计数据
 * @returns {Promise}
 */
export function getStatistics() {
  return request({
    url: '/api/im/workbench/overview',
    method: 'get'
  })
}

export default {
  getTodos,
  getOverview,
  createTodo,
  completeTodo,
  updateTodo,
  deleteTodo,
  getApprovals,
  handleApproval,
  checkIn,
  getAttendance,
  getAnnouncements,
  getStatistics
}
