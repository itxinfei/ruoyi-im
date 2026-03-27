/**
 * 工作台相关 API
 */
import request from '../request'

/**
 * 获取工作台概览数据
 * @returns {Promise}
 */
export function getOverview() {
  return request({
    url: '/api/im/workbench/overview',
    method: 'get'
  })
}

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
 * @param {string} data.description - 描述
 * @param {string} data.type - 类型（可选，默认TASK）
 * @param {number} data.priority - 优先级（可选，1=低, 2=中, 3=高）
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
 * @returns {Promise}
 */
export function updateTodo(todoId, data) {
  return request({
    url: `/api/im/workbench/todos/${todoId}`,
    method: 'put',
    params: {
      title: data.title,
      description: data.description
    }
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
 * @deprecated 请使用 approval.js 中的 approve/reject 方法
 */
export function handleApproval(_data) {
  console.warn('handleApproval: 请使用 approval.js 中的 approve/reject 方法')
  return Promise.reject(new Error('请使用 approval.js 中的 approve/reject 方法'))
}

/**
 * 考勤打卡
 * @param {Object} data - 打卡数据
 * @param {string} data.type - 类型 CHECK_IN/CHECK_OUT
 * @param {string} data.location - 位置
 * @param {string} data.deviceInfo - 设备信息
 * @returns {Promise}
 */
export function checkIn(data) {
  const isCheckOut = data.type === 'CHECK_OUT'
  return request({
    url: `/api/im/attendance/${isCheckOut ? 'checkOut' : 'checkIn'}`,
    method: 'post',
    params: {
      location: data.location,
      deviceInfo: data.deviceInfo
    }
  })
}

/**
 * 获取考勤记录
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期 (yyyy-MM-dd)
 * @param {string} params.endDate - 结束日期 (yyyy-MM-dd)
 * @returns {Promise}
 */
export function getAttendance(params) {
  return request({
    url: '/api/im/attendance/list',
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
    url: '/api/im/announcement/latest',
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

/**
 * 获取工作台常用应用
 * @returns {Promise}
 */
export function getCommonApps() {
  return request({
    url: '/api/im/workbench/apps',
    method: 'get'
  })
}

/**
 * 获取工作台应用分类
 * @returns {Promise}
 */
export function getAppsByCategory() {
  return request({
    url: '/api/im/workbench/apps/category',
    method: 'get'
  })
}

/**
 * 搜索工作台应用
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchApps(keyword) {
  return request({
    url: '/api/im/workbench/apps/search',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 获取最近使用的应用
 * @returns {Promise}
 */
export function getRecentApps() {
  return request({
    url: '/api/im/workbench/apps/recent',
    method: 'get'
  })
}

/**
 * 记录应用使用
 * @param {number} appId - 应用ID
 * @returns {Promise}
 */
export function recordAppUsage(appId) {
  return request({
    url: '/api/im/workbench/apps/record',
    method: 'post',
    params: { appId }
  })
}

/**
 * 获取工作台布局配置
 * @returns {Promise}
 */
export function getLayoutConfig() {
  return request({
    url: '/api/im/workbench/layout',
    method: 'get'
  })
}

/**
 * 保存工作台布局配置
 * @param {string} layoutConfig - 布局配置JSON
 * @returns {Promise}
 */
export function saveLayoutConfig(layoutConfig) {
  return request({
    url: '/api/im/workbench/layout',
    method: 'post',
    data: layoutConfig
  })
}

/**
 * 重置工作台布局配置
 * @returns {Promise}
 */
export function resetLayoutConfig() {
  return request({
    url: '/api/im/workbench/layout',
    method: 'delete'
  })
}
