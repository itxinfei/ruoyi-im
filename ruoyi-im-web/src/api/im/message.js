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
  const requestParams = { ...params }
  if (requestParams.lastMessageId !== undefined && requestParams.lastId === undefined) {
    requestParams.lastId = requestParams.lastMessageId
  }
  if (requestParams.pageSize !== undefined && requestParams.limit === undefined) {
    requestParams.limit = requestParams.pageSize
  }

  return request({
    url: `/api/im/message/list/${conversationId}`,
    method: 'get',
    params: requestParams
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
  const payload = { ...data }
  payload.messageId = messageId
  if (payload.content !== undefined && payload.newContent === undefined) {
    payload.newContent = payload.content
  }

  return request({
    url: `/api/im/message/${messageId}/edit`,
    method: 'put',
    data: payload
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
  const payload = { ...data }
  if (payload.targetConversationId !== undefined && payload.toConversationId === undefined) {
    payload.toConversationId = payload.targetConversationId
  }

  return request({
    url: '/api/im/message/forward',
    method: 'post',
    data: payload
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
 * @param {number|Array} data.messageId - 消息ID或消息ID数组
 * @returns {Promise}
 */
export function markAsRead(data) {
  const messageIds = Array.isArray(data.messageId) ? data.messageId : [data.messageId]
  return request({
    url: '/api/im/message/mark-read',
    method: 'put',
    data: {
      conversationId: data.conversationId,
      messageIds
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
 * 切换消息表情反应（添加/取消）
 * @param {number} messageId - 消息ID
 * @param {string} emoji - 表情符号
 * @returns {Promise}
 */
export function toggleMessageReaction(messageId, emoji) {
  return request({
    url: '/api/im/message/reaction/toggle',
    method: 'post',
    data: { messageId, emoji }
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
  return toggleMessageReaction(messageId, data.emoji)
}

/**
 * 删除消息表情反应
 * @param {number} messageId - 消息ID
 * @param {string} emoji - 表情符号
 * @returns {Promise}
 */
export function removeReaction(messageId, emoji) {
  return toggleMessageReaction(messageId, emoji)
}

/**
 * 清空会话的所有消息（仅删除当前用户发送的消息）
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 */
export function clearConversationMessages(conversationId) {
  return request({
    url: `/api/im/message/conversation/${conversationId}/clear`,
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
    url: `/api/im/message/reaction/${messageId}`,
    method: 'get'
  })
}

/**
 * 获取未读@提及列表
 * @returns {Promise}
 */
export function getUnreadMentions() {
  return request({
    url: '/api/im/mention/unread',
    method: 'get'
  })
}

/**
 * 获取未读@提及数量
 * @returns {Promise}
 */
export function getUnreadMentionCount() {
  return request({
    url: '/api/im/mention/unread/count',
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
    url: `/api/im/mention/${messageId}/read`,
    method: 'put'
  })
}

/**
 * 获取会话未读消息数
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 * @deprecated 请使用 conversation.getUnreadCount 替代
 */
export function getUnreadCount(_conversationId) {
  console.warn('getUnreadCount: 请使用 conversation.getUnreadCount 替代')
  return request({
    url: `/api/im/conversation/unreadCount`,
    method: 'get'
  })
}

// ==================== 消息收藏相关 ====================

/**
 * 收藏消息
 * @param {Object} data - 收藏数据
 * @param {number} data.messageId - 消息ID
 * @param {number} data.conversationId - 会话ID
 * @param {string} data.remark - 备注（可选）
 * @param {string} data.tags - 标签（可选）
 * @returns {Promise}
 */
export function addMessageFavorite(data) {
  return request({
    url: `/api/im/message/favorite/${data.messageId}`,
    method: 'post',
    data
  })
}

/**
 * 取消收藏消息
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function removeMessageFavorite(messageId) {
  return request({
    url: `/api/im/message/favorite/${messageId}`,
    method: 'delete'
  })
}

/**
 * 检查消息是否已收藏
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function checkMessageFavorited(messageId) {
  return request({
    url: `/api/im/message/favorite/${messageId}/check`,
    method: 'get'
  })
}

/**
 * 获取用户收藏的消息列表
 * @returns {Promise}
 */
export function getUserFavorites() {
  return request({
    url: '/api/im/message/favorite/list',
    method: 'get'
  })
}

/**
 * 获取会话中收藏的消息列表
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 */
export function getConversationFavorites(conversationId) {
  return request({
    url: `/api/im/message/favorite/conversation/${conversationId}`,
    method: 'get'
  })
}

// ==================== 消息已读回执详情 ====================

/**
 * 获取消息已读详情（已读/未读人员列表）
 * @param {number} messageId - 消息 ID
 * @returns {Promise}
 */
export function getMessageReadDetail(messageId) {
  return request({
    url: `/api/im/message/read/detail/${messageId}`,
    method: 'get'
  })
}

/**
 * 获取消息已读状态（已读人数和百分比）
 * @param {number} messageId - 消息 ID
 * @returns {Promise}
 */
export function getMessageReadStatus(messageId) {
  return request({
    url: `/api/im/message/read/status/${messageId}`,
    method: 'get'
  })
}

// ==================== 消息编辑历史 ====================

/**
 * 获取消息编辑历史列表
 * @param {number} messageId - 消息 ID
 * @returns {Promise}
 */
export function getMessageEditHistory(messageId) {
  return request({
    url: `/api/im/message/edit-history/${messageId}`,
    method: 'get'
  })
}

