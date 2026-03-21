/**
 * 任务管理相关 API
 */
import request from '../request'

/**
 * 创建任务
 * @param {Object} data - 任务数据
 * @returns {Promise}
 */
export function createTask(data) {
  return request({
    url: '/api/im/task/create',
    method: 'post',
    data
  })
}

/**
 * 更新任务
 * @param {Object} data - 任务数据
 * @returns {Promise}
 */
export function updateTask(data) {
  return request({
    url: '/api/im/task',
    method: 'put',
    data
  })
}

/**
 * 删除任务
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function deleteTask(taskId) {
  return request({
    url: `/api/im/task/${taskId}`,
    method: 'delete'
  })
}

/**
 * 获取任务详情
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function getTaskDetail(taskId) {
  return request({
    url: `/api/im/task/${taskId}`,
    method: 'get'
  })
}

/**
 * 分页查询任务
 * @param {Object} data - 查询条件
 * @returns {Promise}
 */
export function getTaskPage(data) {
  return request({
    url: '/api/im/task/page',
    method: 'post',
    data
  })
}

/**
 * 获取我的任务列表
 * @param {string} status - 任务状态（可选）
 * @returns {Promise}
 */
export function getMyTasks(status) {
  return request({
    url: '/api/im/task/my',
    method: 'get',
    params: { status }
  })
}

/**
 * 获取我创建的任务
 * @returns {Promise}
 */
export function getMyCreatedTasks() {
  return request({
    url: '/api/im/task/created',
    method: 'get'
  })
}

/**
 * 分配任务
 * @param {number} taskId - 任务ID
 * @param {number} assigneeId - 负责人ID
 * @returns {Promise}
 */
export function assignTask(taskId, assigneeId) {
  return request({
    url: `/api/im/task/${taskId}/assign`,
    method: 'put',
    params: { assigneeId }
  })
}

/**
 * 更新任务状态
 * @param {number} taskId - 任务ID
 * @param {string} status - 状态
 * @returns {Promise}
 */
export function updateTaskStatus(taskId, status) {
  return request({
    url: `/api/im/task/${taskId}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 更新任务进度
 * @param {number} taskId - 任务ID
 * @param {number} percent - 完成百分比
 * @returns {Promise}
 */
export function updateTaskProgress(taskId, percent) {
  return request({
    url: `/api/im/task/${taskId}/progress`,
    method: 'put',
    params: { percent }
  })
}

/**
 * 关注/取消关注任务
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function toggleTaskFollow(taskId) {
  return request({
    url: `/api/im/task/${taskId}/follow`,
    method: 'put'
  })
}

/**
 * 添加任务评论
 * @param {number} taskId - 任务ID
 * @param {string} content - 评论内容
 * @param {number} replyToId - 回复的评论ID（可选）
 * @returns {Promise}
 */
export function addTaskComment(taskId, content, replyToId) {
  return request({
    url: `/api/im/task/${taskId}/comment`,
    method: 'post',
    params: { content, replyToId }
  })
}

/**
 * 获取任务评论列表
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function getTaskComments(taskId) {
  return request({
    url: `/api/im/task/${taskId}/comments`,
    method: 'get'
  })
}

/**
 * 获取任务统计信息
 * @returns {Promise}
 */
export function getTaskStatistics() {
  return request({
    url: '/api/im/task/statistics',
    method: 'get'
  })
}

/**
 * 批量删除任务
 * @param {Array} taskIds - 任务ID列表
 * @returns {Promise}
 */
export function batchDeleteTasks(taskIds) {
  return request({
    url: '/api/im/task/batch',
    method: 'delete',
    data: taskIds
  })
}

/**
 * 批量更新任务状态
 * @param {Array} taskIds - 任务ID列表
 * @param {string} status - 目标状态
 * @returns {Promise}
 */
export function batchUpdateTaskStatus(taskIds, status) {
  return request({
    url: '/api/im/task/batch/status',
    method: 'put',
    data: taskIds,
    params: { status }
  })
}

/**
 * 获取子任务列表
 * @param {number} parentId - 父任务ID
 * @returns {Promise}
 */
export function getSubtasks(parentId) {
  return request({
    url: `/api/im/task/subtasks/${parentId}`,
    method: 'get'
  })
}

/**
 * 复制任务
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function copyTask(taskId) {
  return request({
    url: `/api/im/task/${taskId}/copy`,
    method: 'post'
  })
}
