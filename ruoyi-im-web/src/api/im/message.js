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
    url: '/api/im/message/send',
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
export function getMessages(conversationId, params = {}) {
  return request({
    url: `/api/im/message/list/${conversationId}`,
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
    url: `/api/im/message/${messageId}/recall`,
    method: 'delete'
  })
}

/**
 * 编辑消息
 * @param {Object} data - 编辑数据
 * @param {number} data.messageId - 消息ID
 * @param {string} data.content - 新内容
 * @returns {Promise}
 */
export function editMessage(messageId, data) {
  return request({
    url: `/api/im/message/${messageId}/edit`,
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
    url: `/api/im/message/${messageId}`,
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
    url: '/api/im/message/forward',
    method: 'post',
    data
  })
}

/**
 * 批量转发消息
 * @param {Object} data - 批量转发数据
 * @param {Array<number>} data.messageIds - 原消息ID列表
 * @param {number} data.toConversationId - 目标会话ID
 * @param {string} data.forwardType - 转发类型：batch=逐条转发, combine=合并转发
 * @param {string} data.content - 附加说明
 * @returns {Promise}
 */
export function batchForwardMessages(data) {
  return request({
    url: '/api/im/message/forward/batch',
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
export function searchMessages(data) {
  return request({
    url: '/api/im/message/search',
    method: 'post',
    data
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
    url: '/api/im/message/read',
    method: 'put',
    params: {
      conversationId: data.conversationId,
      lastReadMessageId: data.messageId
    }
  })
}

/**
 * 获取消息已读用户列表
 * @param {number} conversationId - 会话ID
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function getMessageReadUsers(conversationId, messageId) {
  return request({
    url: `/api/im/message/read/status/${conversationId}/${messageId}`,
    method: 'get'
  })
}

/**
 * 回复消息
 * @param {Object} data - 回复数据
 * @param {number} data.messageId - 原消息ID
 * @param {string} data.content - 回复内容
 * @returns {Promise}
 */
export function replyMessage(data) {
  return request({
    url: '/api/im/message/reply',
    method: 'post',
    data
  })
}

/**
 * 添加消息表情反应
 * @param {number} messageId - 消息ID
 * @param {Object} data - 反应数据
 * @param {string} data.emoji - 表情符号
 * @returns {Promise}
 */
export function addReaction(messageId, data) {
  return request({
    url: `/api/im/message/${messageId}/reaction`,
    method: 'post',
    data
  })
}

/**
 * 删除消息表情反应
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function removeReaction(messageId) {
  return request({
    url: `/api/im/message/${messageId}/reaction`,
    method: 'delete'
  })
}

/**
 * 获取消息的表情反应列表
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function getMessageReactions(messageId) {
  return request({
    url: `/api/im/message/${messageId}/reactions`,
    method: 'get'
  })
}

/**
 * 获取未读@提及列表
 * @returns {Promise}
 */
export function getUnreadMentions() {
  return request({
    url: '/api/im/message/mention/unread',
    method: 'get'
  })
}

/**
 * 获取未读@提及数量
 * @returns {Promise}
 */
export function getUnreadMentionCount() {
  return request({
    url: '/api/im/message/mention/unread/count',
    method: 'get'
  })
}

/**
 * 标记@提及为已读
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function markMentionAsRead(messageId) {
  return request({
    url: `/api/im/message/${messageId}/mention/read`,
    method: 'put'
  })
}

/**
 * 获取会话未读消息数
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 */
export function getUnreadCount(conversationId) {
  return request({
    url: `/api/im/message/unread/count/${conversationId}`,
    method: 'get'
  })
}

/**
 * 置顶消息
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function pinMessage(messageId) {
  return request({
    url: `/api/im/message/${messageId}/pin`,
    method: 'post'
  })
}

/**
 * 取消置顶消息
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function unpinMessage(messageId) {
  return request({
    url: `/api/im/message/${messageId}/pin`,
    method: 'delete'
  })
}

/**
 * 获取会话的置顶消息列表
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 */
export function getPinnedMessages(conversationId) {
  return request({
    url: `/api/im/message/pinned/${conversationId}`,
    method: 'get'
  })
}
