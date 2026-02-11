/**
 * 消息标记 API
 * 提供消息标记、待办提醒等功能
 */
import request from '../request'

/**
 * 标记消息
 * @param {Object} params - 标记参数
 * @param {number} params.messageId - 消息ID
 * @param {string} params.markerType - 标记类型：FLAG标记, IMPORTANT重要
 * @param {string} params.color - 标记颜色（可选）
 * @returns {Promise}
 */
export function markMessage(params) {
  return request({
    url: '/api/im/messages/markers',
    method: 'post',
    params
  })
}

/**
 * 取消标记
 * @param {Object} params - 取消标记参数
 * @param {number} params.messageId - 消息ID
 * @param {string} params.markerType - 标记类型（不传则取消所有标记）
 * @returns {Promise}
 */
export function unmarkMessage(params) {
  return request({
    url: '/api/im/messages/markers',
    method: 'delete',
    params
  })
}

/**
 * 设置待办提醒
 * @param {Object} params - 待办参数
 * @param {number} params.messageId - 消息ID
 * @param {string} params.remindTime - 提醒时间
 * @param {string} params.remark - 备注（可选）
 * @returns {Promise}
 */
export function setTodoReminder(params) {
  return request({
    url: '/api/im/messages/markers/todos',
    method: 'post',
    params
  })
}

/**
 * 完成待办
 * @param {number} markerId - 标记ID
 * @returns {Promise}
 */
export function completeTodo(markerId) {
  return request({
    url: `/api/im/messages/markers/todos/${markerId}/complete`,
    method: 'post'
  })
}

/**
 * 重启待办
 * @param {number} markerId - 标记ID
 * @returns {Promise}
 */
export function reopenTodo(markerId) {
  return request({
    url: `/api/im/messages/markers/todos/${markerId}/reopen`,
    method: 'post'
  })
}

/**
 * 获取用户的标记列表
 * @param {string} markerType - 标记类型筛选：FLAG标记, TODO待办, IMPORTANT重要
 * @returns {Promise}
 */
export function getUserMarkers(markerType) {
  return request({
    url: '/api/im/messages/markers',
    method: 'get',
    params: { markerType }
  })
}

/**
 * 获取消息的标记列表
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function getMessageMarkers(messageId) {
  return request({
    url: `/api/im/messages/markers/messages/${messageId}`,
    method: 'get'
  })
}

/**
 * 获取待办数量
 * @returns {Promise}
 */
export function getUserTodoCount() {
  return request({
    url: '/api/im/messages/markers/todos/count',
    method: 'get'
  })
}
