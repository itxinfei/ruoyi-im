/**
 * 工作台相关 API
 */
import request from '../request'

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
 * 创建待办
 * @param {Object} data - 待办数据
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 * @param {string} data.dueDate - 截止日期
 * @returns {Promise}
 */
export function createTodo(data) {
    return request({
        url: '/api/im/workbench/todo',
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
        url: `/api/im/workbench/todo/${todoId}/complete`,
        method: 'put'
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
        url: '/api/im/approval/pending',
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
        url: '/api/im/approval/handle',
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
        url: '/api/im/attendance/checkIn',
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
        url: '/api/im/attendance/records',
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
        url: '/api/im/announcement/list',
        method: 'get'
    })
}

/**
 * 获取统计数据
 * @returns {Promise}
 */
export function getStatistics() {
    return request({
        url: '/api/im/workbench/statistics',
        method: 'get'
    })
}
