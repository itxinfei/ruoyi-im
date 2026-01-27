/**
 * 待办事项/消息标记相关 API
 * 对应后端 ImMessageMarkerService
 * @author ruoyi
 */
import request from '../request'

/**
 * 获取用户待办列表
 * @param {string} markerType - 标记类型过滤（可选）: TODO/FLAG/IMPORTANT
 * @returns {Promise}
 */
export function getTodoList(markerType) {
  return request({
    url: '/api/im/message/marker/list',
    method: 'get',
    params: { markerType }
  })
}

/**
 * 获取待办数量
 * @returns {Promise}
 */
export function getTodoCount() {
  return request({
    url: '/api/im/message/marker/count',
    method: 'get'
  })
}

/**
 * 设置消息待办提醒
 * @param {Object} data - 待办数据
 * @param {number} data.messageId - 消息ID
 * @param {string} data.remindTime - 提醒时间 yyyy-MM-dd HH:mm:ss
 * @param {string} data.remark - 备注（可选）
 * @returns {Promise}
 */
export function setTodoReminder(data) {
  return request({
    url: '/api/im/message/marker/todo',
    method: 'post',
    data
  })
}

/**
 * 完成待办
 * @param {number} markerId - 标记ID
 * @returns {Promise}
 */
export function completeTodo(markerId) {
  return request({
    url: `/api/im/message/marker/${markerId}/complete`,
    method: 'put'
  })
}

/**
 * 重启待办
 * @param {number} markerId - 标记ID
 * @returns {Promise}
 */
export function reopenTodo(markerId) {
  return request({
    url: `/api/im/message/marker/${markerId}/reopen`,
    method: 'put'
  })
}

/**
 * 删除待办
 * @param {number} markerId - 标记ID
 * @returns {Promise}
 */
export function deleteTodo(markerId) {
  return request({
    url: `/api/im/message/marker/${markerId}`,
    method: 'delete'
  })
}

/**
 * 标记消息
 * @param {Object} data - 标记数据
 * @param {number} data.messageId - 消息ID
 * @param {string} data.markerType - 标记类型: FLAG/IMPORTANT/TODO
 * @param {string} data.color - 颜色标识（可选）
 * @returns {Promise}
 */
export function markMessage(data) {
  return request({
    url: '/api/im/message/marker/mark',
    method: 'post',
    data
  })
}

/**
 * 取消标记
 * @param {Object} params - 参数
 * @param {number} params.messageId - 消息ID
 * @param {string} params.markerType - 标记类型（可选）
 * @returns {Promise}
 */
export function unmarkMessage(params) {
  return request({
    url: '/api/im/message/marker/unmark',
    method: 'delete',
    params
  })
}

/**
 * 获取消息的标记列表
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function getMessageMarkers(messageId) {
  return request({
    url: `/api/im/message/marker/message/${messageId}`,
    method: 'get'
  })
}

export default {
  getTodoList,
  getTodoCount,
  setTodoReminder,
  completeTodo,
  reopenTodo,
  deleteTodo,
  markMessage,
  unmarkMessage,
  getMessageMarkers
}
