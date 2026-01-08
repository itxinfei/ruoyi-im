/**
 * 消息模块API
 * @module api/im/message
 */
import request from '@/utils/request'

/**
 * 发送消息
 * @param {Object} data - 消息数据
 * @param {string} data.conversationId - 会话ID
 * @param {string} data.type - 消息类型 text/image/file/voice/video
 * @param {string} data.content - 消息内容
 * @param {string} [data.replyToMessageId] - 回复消息ID
 * @param {string} [data.clientMsgId] - 客户端消息ID
 * @returns {Promise}
 */
export function sendMessage(data) {
  // 统一参数命名
  const params = {
    conversationId: data.conversationId,
    type: data.type,
    content: data.content,
    replyToMessageId: data.replyToMessageId,
    clientMsgId: data.clientMsgId,
  }
  return request({
    url: '/api/im/message/send',
    method: 'post',
    data: params,
  })
}

/**
 * 获取会话消息列表
 * @param {Object} params - 查询参数
 * @param {string} params.conversationId - 会话ID
 * @param {number} [params.pageSize=20] - 每页数量
 * @param {string} [params.lastId] - 上一页最后一条消息ID
 * @returns {Promise}
 */
export function listMessage(params) {
  return request({
    url: `/api/im/message/list/${params.conversationId}`,
    method: 'get',
    params: {
      pageSize: params.pageSize || 20,
      lastId: params.lastId,
    },
  })
}

/**
 * 标记消息已读
 * @param {string} conversationId - 会话ID
 * @param {string|string[]} messageIds - 消息ID或消息ID数组
 * @returns {Promise}
 */
export function markMessageRead(conversationId, messageIds) {
  return request({
    url: '/api/im/message/read',
    method: 'put',
    data: {
      conversationId,
      messageIds: Array.isArray(messageIds) ? messageIds : [messageIds],
    },
  })
}

/**
 * 撤回消息
 * @param {string} messageId - 消息ID
 * @returns {Promise}
 */
export function recallMessage(messageId) {
  return request({
    url: `/api/im/message/${messageId}/recall`,
    method: 'delete',
  })
}

/**
 * 搜索消息
 * @param {Object} params - 搜索参数
 * @param {string} params.keyword - 搜索关键词
 * @param {string} [params.conversationId] - 限定会话ID
 * @param {string} [params.startTime] - 开始时间
 * @param {string} [params.endTime] - 结束时间
 * @param {number} [params.pageSize=20] - 每页数量
 * @returns {Promise}
 */
export function searchMessages(params) {
  return request({
    url: '/api/im/message/search',
    method: 'get',
    params: {
      keyword: params.keyword,
      conversationId: params.conversationId,
      startTime: params.startTime,
      endTime: params.endTime,
      pageSize: params.pageSize || 20,
    },
  })
}

/**
 * 获取消息详情
 * @param {string} messageId - 消息ID
 * @returns {Promise}
 */
export function getMessageDetail(messageId) {
  return request({
    url: `/api/im/message/${messageId}`,
    method: 'get',
  })
}

/**
 * 删除消息
 * @param {string} messageId - 消息ID
 * @returns {Promise}
 */
export function deleteMessage(messageId) {
  return request({
    url: `/api/im/message/${messageId}`,
    method: 'delete',
  })
}

/**
 * 批量删除消息
 * @param {string[]} messageIds - 消息ID数组
 * @returns {Promise}
 */
export function batchDeleteMessages(messageIds) {
  return request({
    url: '/api/im/message/batch',
    method: 'delete',
    data: messageIds,
  })
}

/**
 * 获取未读消息数量
 * @returns {Promise}
 */
export function getUnreadCount() {
  return request({
    url: '/api/im/message/unread/count',
    method: 'get',
  })
}

/**
 * 获取会话未读消息数量
 * @param {string} conversationId - 会话ID
 * @returns {Promise}
 */
export function getConversationUnreadCount(conversationId) {
  return request({
    url: `/api/im/message/unread/conversation/${conversationId}`,
    method: 'get',
  })
}

/**
 * 转发消息
 * @param {Object} data - 转发数据
 * @param {string} data.messageId - 原消息ID
 * @param {string|string[]} data.targetConversationIds - 目标会话ID数组
 * @returns {Promise}
 */
export function forwardMessage(data) {
  return request({
    url: '/api/im/message/forward',
    method: 'post',
    data: {
      messageId: data.messageId,
      targetConversationIds: Array.isArray(data.targetConversationIds)
        ? data.targetConversationIds
        : [data.targetConversationIds],
    },
  })
}

/**
 * 回复消息
 * @param {Object} data - 回复数据
 * @param {string} data.conversationId - 会话ID
 * @param {string} data.replyToMessageId - 被回复消息ID
 * @param {string} data.content - 回复内容
 * @param {string} data.type - 消息类型
 * @returns {Promise}
 */
export function replyMessage(data) {
  return request({
    url: '/api/im/message/reply',
    method: 'post',
    data,
  })
}

/**
 * 获取消息统计
 * @param {Object} params - 统计参数
 * @param {string} [params.conversationId] - 会话ID
 * @param {string} [params.startTime] - 开始时间
 * @param {string} [params.endTime] - 结束时间
 * @returns {Promise}
 */
export function getMessageStatistics(params) {
  return request({
    url: '/api/im/message/statistics',
    method: 'get',
    params,
  })
}

/**
 * 获取最新消息
 * @param {number} [limit=10] - 获取数量
 * @returns {Promise}
 */
export function getLatestMessages(limit = 10) {
  return request({
    url: '/api/im/message/latest',
    method: 'get',
    params: { limit },
  })
}

/**
 * 获取消息类型统计
 * @param {string} conversationId - 会话ID
 * @returns {Promise}
 */
export function getMessageTypeStatistics(conversationId) {
  return request({
    url: '/api/im/message/statistics',
    method: 'get',
    params: { conversationId },
  })
}

// ========== 别名 - 向后兼容 ==========

// 旧参数名兼容
export const getConversationMessages = listMessage

// 默认导出 - 方便批量引入
export default {
  sendMessage,
  listMessage,
  markMessageRead,
  recallMessage,
  searchMessages,
  getMessageDetail,
  deleteMessage,
  batchDeleteMessages,
  getUnreadCount,
  getConversationUnreadCount,
  forwardMessage,
  replyMessage,
  getMessageStatistics,
  getLatestMessages,
  getMessageTypeStatistics,
  // 别名
  getConversationMessages,
}
