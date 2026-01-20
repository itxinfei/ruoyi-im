/**
 * 消息相关 API
 */
import request from '../request'

/**
 * 发送消息
 * @param {Object} data - 消息数据
 * @param {number} data.conversationId - 会话ID
 * @param {string} data.messageType - 消息类型 TEXT/IMAGE/FILE/VOICE/VIDEO
 * @param {string} data.content - 消息内容
 * @param {number} data.replyToMessageId - 回复的消息ID（可选）
 * @returns {Promise}
 */
export function sendMessage(data) {
  return request({
    url: '/im/message/send',
    method: 'post',
    data
  })
}

/**
 * 获取会话消息列表
 * @param {Object} params - 查询参数
 * @param {number} params.conversationId - 会话ID
 * @param {number} params.lastMessageId - 上一条消息ID（用于分页）
 * @param {number} params.pageSize - 每页数量（默认20）
 * @returns {Promise}
 */
export function getMessages(params) {
  return request({
    url: '/im/message/list',
    method: 'get',
    params
  })
}

/**
 * 撤回消息
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function recallMessage(messageId) {
  return request({
    url: `/im/message/${messageId}/recall`,
    method: 'put'
  })
}

/**
 * 编辑消息
 * @param {Object} data - 编辑数据
 * @param {number} data.messageId - 消息ID
 * @param {string} data.content - 新内容
 * @returns {Promise}
 */
export function editMessage(data) {
  return request({
    url: '/im/message/edit',
    method: 'put',
    data
  })
}

/**
 * 删除消息
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function deleteMessage(messageId) {
  return request({
    url: `/im/message/${messageId}`,
    method: 'delete'
  })
}

/**
 * 转发消息
 * @param {Object} data - 转发数据
 * @param {number} data.messageId - 原消息ID
 * @param {number} data.targetConversationId - 目标会话ID
 * @returns {Promise}
 */
export function forwardMessage(data) {
  return request({
    url: '/im/message/forward',
    method: 'post',
    data
  })
}

/**
 * 搜索消息
 * @param {Object} params - 查询参数
 * @param {number} params.conversationId - 会话ID
 * @param {string} params.keyword - 搜索关键词
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @returns {Promise}
 */
export function searchMessages(params) {
  return request({
    url: '/im/message/search',
    method: 'get',
    params
  })
}

/**
 * 标记消息已读
 * @param {Object} data - 已读数据
 * @param {number} data.conversationId - 会话ID
 * @param {number} data.messageId - 消息ID
 * @returns {Promise}
 */
export function markAsRead(data) {
  return request({
    url: '/im/message/read',
    method: 'post',
    data
  })
}

/**
 * 获取消息已读用户列表
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function getMessageReadUsers(messageId) {
  return request({
    url: `/im/message/${messageId}/read-users`,
    method: 'get'
  })
}
