/**
 * 工作周报 API
 * 对接后端 ImWorkReportController (/api/im/work-report)
 */
import request from '../request'

/**
 * 创建工作日志
 * @param {Object} data - WorkReportCreateRequest
 * @returns {Promise}
 */
export function createWorkReport(data) {
  return request({
    url: '/api/im/work-report',
    method: 'post',
    data
  })
}

/**
 * 提交工作日志
 * @param {Number} reportId - 日志ID
 * @returns {Promise}
 */
export function submitWorkReport(reportId) {
  return request({
    url: `/api/im/work-report/${reportId}/submit`,
    method: 'put'
  })
}

/**
 * 获取工作日志详情
 * @param {Number} reportId - 日志ID
 * @returns {Promise}
 */
export function getWorkReportDetail(reportId) {
  return request({
    url: `/api/im/work-report/${reportId}`,
    method: 'get'
  })
}

/**
 * 获取我的日志列表
 * @returns {Promise}
 */
export function getMyReports() {
  return request({
    url: '/api/im/work-report/my',
    method: 'get'
  })
}

/**
 * 分页查询工作日志
 * @param {Object} data - WorkReportQueryRequest
 * @returns {Promise}
 */
export function getWorkReportPage(data) {
  return request({
    url: '/api/im/work-report/page',
    method: 'post',
    data
  })
}

/**
 * 获取统计信息
 * @returns {Promise}
 */
export function getWorkReportStatistics() {
  return request({
    url: '/api/im/work-report/statistics',
    method: 'get'
  })
}

/**
 * 删除工作日志
 * @param {Number} reportId - 日志ID
 * @returns {Promise}
 */
export function deleteWorkReport(reportId) {
  return request({
    url: `/api/im/work-report/${reportId}`,
    method: 'delete'
  })
}

/**
 * 更新工作日志
 * @param {Number} reportId - 日志ID
 * @param {Object} data - WorkReportCreateRequest
 * @returns {Promise}
 */
export function updateWorkReport(reportId, data) {
  return request({
    url: `/api/im/work-report/${reportId}`,
    method: 'put',
    data
  })
}

// ==================== 评论管理 ====================

/**
 * 添加评论
 * @param {Number} reportId - 日志ID
 * @param {String} content - 评论内容
 * @param {Number} parentId - 父评论ID（可选）
 * @returns {Promise}
 */
export function addWorkReportComment(reportId, content, parentId) {
  return request({
    url: `/api/im/work-report/${reportId}/comment`,
    method: 'post',
    params: { content, parentId }
  })
}

/**
 * 删除评论
 * @param {Number} commentId - 评论ID
 * @returns {Promise}
 */
export function deleteWorkReportComment(commentId) {
  return request({
    url: `/api/im/work-report/comment/${commentId}`,
    method: 'delete'
  })
}

/**
 * 获取评论列表
 * @param {Number} reportId - 日志ID
 * @returns {Promise}
 */
export function getWorkReportComments(reportId) {
  return request({
    url: `/api/im/work-report/${reportId}/comments`,
    method: 'get'
  })
}

// ==================== 点赞管理 ====================

/**
 * 点赞/取消点赞
 * @param {Number} reportId - 日志ID
 * @returns {Promise}
 */
export function toggleWorkReportLike(reportId) {
  return request({
    url: `/api/im/work-report/${reportId}/like`,
    method: 'post'
  })
}

/**
 * 获取点赞用户列表
 * @param {Number} reportId - 日志ID
 * @returns {Promise}
 */
export function getWorkReportLikeUsers(reportId) {
  return request({
    url: `/api/im/work-report/${reportId}/likes`,
    method: 'get'
  })
}

// ==================== 审批管理 ====================

/**
 * 审批工作日志
 * @param {Number} reportId - 日志ID
 * @param {Boolean} approved - 是否通过
 * @param {String} remark - 审批备注
 * @returns {Promise}
 */
export function approveWorkReport(reportId, approved, remark) {
  return request({
    url: `/api/im/work-report/${reportId}/approve`,
    method: 'put',
    params: { approved, remark }
  })
}
