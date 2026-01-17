/**
 * 消息模块API
 * @module api/im/message
 * @description 符合API接口规范文档 v1.0
 */
import request from '@/utils/request'

// ==================== 消息发送 ====================

/**
 * 发送消息
 * @param {Object} data - 消息数据
 * @param {number|string} data.conversationId - 会话ID（必填）
 * @param {string} data.type - 消息类型 TEXT/IMAGE/FILE/VOICE/VIDEO/LOCATION
 * @param {string} data.content - 消息内容（必填）
 * @param {number|string} [data.replyToMessageId] - 回复的消息ID（可选）
 * @param {string} [data.clientMsgId] - 客户端消息ID（可选，用于去重）
 * @param {number|string} [data.receiverId] - 接收者ID（私聊且会话ID为空时使用）
 * @param {Object} [data.mentionInfo] - @提及信息（可选）
 * @param {number[]} [data.mentionInfo.userIds] - 被@的用户ID列表
 * @param {boolean} [data.mentionInfo.mentionAll] - 是否@所有人
 * @returns {Promise<Result<number>>} 返回消息ID
 */
export function sendMessage(data) {
  const params = {
    conversationId: Number(data.conversationId),
    type: normalizeMessageType(data.type),
    content: data.content,
    replyToMessageId: data.replyToMessageId ? Number(data.replyToMessageId) : undefined,
    clientMsgId: data.clientMsgId,
    receiverId: data.receiverId ? Number(data.receiverId) : undefined,
    mentionInfo: data.mentionInfo,
  }
  return request({
    url: '/api/im/message/send',
    method: 'post',
    data: params,
  })
}

// ==================== 消息查询 ====================

/**
 * 获取会话消息列表
 * @param {Object} params - 查询参数
 * @param {number|string} params.conversationId - 会话ID（必填）
 * @param {number} [params.lastId] - 上一页最后一条消息ID，用于分页
 * @param {number} [params.limit] - 每页数量，默认20
 * @returns {Promise<Result<Message[]>>}
 */
export function listMessage(params) {
  return request({
    url: `/api/im/message/list/${params.conversationId}`,
    method: 'get',
    params: {
      lastId: params.lastId,
      limit: params.limit || 20,
    },
  })
}

/**
 * 获取消息详情
 * @param {number|string} messageId - 消息ID
 * @returns {Promise<Result<Message>>}
 */
export function getMessageDetail(messageId) {
  return request({
    url: `/api/im/message/${messageId}`,
    method: 'get',
  })
}

/**
 * 搜索消息
 * @param {Object} params - 搜索参数
 * @param {string} params.keyword - 搜索关键词（必填）
 * @param {number|string} [params.conversationId] - 限定会话ID（可选）
 * @param {string} [params.messageType] - 消息类型（可选）
 * @param {number|string} [params.senderId] - 发送者ID（可选）
 * @param {string} [params.startTime] - 开始时间（可选）
 * @param {string} [params.endTime] - 结束时间（可选）
 * @param {number} [params.pageNum] - 页码，默认1
 * @param {number} [params.pageSize] - 每页数量，默认20
 * @param {boolean} [params.includeRevoked] - 是否包含已撤回消息
 * @param {boolean} [params.exactMatch] - 是否精确匹配
 * @returns {Promise<Result<PageResult<Message>>>}
 */
export function searchMessages(params) {
  return request({
    url: '/api/im/message/search',
    method: 'post',
    data: {
      keyword: params.keyword,
      conversationId: params.conversationId ? Number(params.conversationId) : undefined,
      messageType: params.messageType,
      senderId: params.senderId ? Number(params.senderId) : undefined,
      startTime: params.startTime,
      endTime: params.endTime,
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 20,
      includeRevoked: params.includeRevoked || false,
      exactMatch: params.exactMatch || false,
    },
  })
}

// ==================== 消息操作 ====================

/**
 * 撤回消息
 * @param {number|string} messageId - 消息ID
 * @returns {Promise<Result<void>>}
 * @description 只能撤回自己发送的消息，且发送时间不超过2分钟
 */
export function recallMessage(messageId) {
  return request({
    url: `/api/im/message/${messageId}/recall`,
    method: 'delete',
  })
}

/**
 * 编辑消息
 * @param {Object} data - 编辑数据
 * @param {number|string} data.messageId - 消息ID
 * @param {string} data.newContent - 新内容
 * @returns {Promise<Result<void>>}
 * @description 只能编辑自己发送的文本消息，且发送时间不超过15分钟
 */
export function editMessage(data) {
  return request({
    url: `/api/im/message/${data.messageId}/edit`,
    method: 'put',
    data: {
      newContent: data.newContent,
    },
  })
}

/**
 * 转发消息
 * @param {Object} data - 转发数据
 * @param {number|string} data.messageId - 原消息ID
 * @param {number|string} data.toConversationId - 目标会话ID
 * @param {number|string} [data.toUserId] - 目标用户ID（私聊使用）
 * @param {string} [data.content] - 转发附言
 * @returns {Promise<Result<number>>} 返回新消息ID
 */
export function forwardMessage(data) {
  return request({
    url: '/api/im/message/forward',
    method: 'post',
    data: {
      messageId: Number(data.messageId),
      toConversationId: data.toConversationId ? Number(data.toConversationId) : undefined,
      toUserId: data.toUserId ? Number(data.toUserId) : undefined,
      content: data.content,
    },
  })
}

/**
 * 回复消息
 * @param {Object} data - 回复数据
 * @param {number|string} data.messageId - 被回复的消息ID
 * @param {string} data.content - 回复内容
 * @returns {Promise<Result<number>>} 返回新消息ID
 */
export function replyMessage(data) {
  return request({
    url: '/api/im/message/reply',
    method: 'post',
    data: {
      messageId: Number(data.messageId),
      content: data.content,
    },
  })
}

/**
 * 删除消息
 * @param {number|string} messageId - 消息ID
 * @returns {Promise<Result<void>>}
 */
export function deleteMessage(messageId) {
  return request({
    url: `/api/im/message/${messageId}`,
    method: 'delete',
  })
}

/**
 * 批量删除消息
 * @param {number[]} messageIds - 消息ID数组
 * @returns {Promise<Result<void>>}
 */
export function batchDeleteMessages(messageIds) {
  return request({
    url: '/api/im/message/batch',
    method: 'delete',
    data: messageIds.map(id => Number(id)),
  })
}

// ==================== 消息已读回执 ====================

/**
 * 标记单条消息为已读
 * @param {Object} data - 已读数据
 * @param {number|string} data.messageId - 消息ID
 * @param {number|string} data.conversationId - 会话ID
 * @returns {Promise<Result<void>>}
 */
export function markMessageAsRead(data) {
  return request({
    url: `/api/im/message/read/${data.messageId}`,
    method: 'post',
    params: {
      conversationId: Number(data.conversationId),
    },
  })
}

/**
 * 批量标记消息为已读
 * @param {Object} data - 已读数据
 * @param {number|string} data.conversationId - 会话ID
 * @param {number[]} data.messageIds - 消息ID数组
 * @returns {Promise<Result<void>>}
 */
export function markBatchMessageAsRead(data) {
  return request({
    url: '/api/im/message/read/batch',
    method: 'post',
    data: {
      conversationId: Number(data.conversationId),
      messageIds: data.messageIds.map(id => Number(id)),
    },
  })
}

/**
 * 标记会话所有消息为已读
 * @param {Object} data - 已读数据
 * @param {number|string} data.conversationId - 会话ID
 * @param {number|string} data.upToMessageId - 标记到此消息ID为止
 * @returns {Promise<Result<void>>}
 */
export function markConversationAsRead(data) {
  return request({
    url: `/api/im/message/read/conversation/${data.conversationId}`,
    method: 'post',
    params: {
      upToMessageId: Number(data.upToMessageId),
    },
  })
}

/**
 * 获取消息已读状态
 * @param {number|string} messageId - 消息ID
 * @returns {Promise<Result<ImMessageReadStatusVO>>}
 */
export function getMessageReadStatus(messageId) {
  return request({
    url: `/api/im/message/read/status/${messageId}`,
    method: 'get',
  })
}

/**
 * 获取消息已读详情
 * @param {number|string} messageId - 消息ID
 * @returns {Promise<Result<ImMessageReadDetailVO>>}
 */
export function getMessageReadDetail(messageId) {
  return request({
    url: `/api/im/message/read/detail/${messageId}`,
    method: 'get',
  })
}

/**
 * 获取会话消息已读状态列表
 * @param {number|string} conversationId - 会话ID
 * @returns {Promise<Result<ImMessageReadStatusVO[]>>}
 */
export function getConversationReadStatus(conversationId) {
  return request({
    url: `/api/im/message/read/conversation/${conversationId}`,
    method: 'get',
  })
}

/**
 * 撤回已读回执
 * @param {number|string} messageId - 消息ID
 * @returns {Promise<Result<void>>}
 */
export function revokeMessageRead(messageId) {
  return request({
    url: `/api/im/message/read/${messageId}`,
    method: 'delete',
  })
}

/**
 * 获取未读消息数量
 * @returns {Promise<Result<number>>}
 */
export function getUnreadCount() {
  return request({
    url: '/api/im/message/unread/count',
    method: 'get',
  })
}

/**
 * 获取会话未读消息数量
 * @param {number|string} conversationId - 会话ID
 * @returns {Promise<Result<number>>}
 */
export function getConversationUnreadCount(conversationId) {
  return request({
    url: `/api/im/message/unread/conversation/${conversationId}`,
    method: 'get',
  })
}

// ==================== 消息反应 ====================

/**
 * 添加消息表情反应
 * @param {Object} data - 反应数据
 * @param {number|string} data.messageId - 消息ID
 * @param {string} data.emoji - 表情符号
 * @returns {Promise<Result<MessageReaction>>}
 * @description 再次使用相同表情会取消反应
 */
export function addMessageReaction(data) {
  return request({
    url: `/api/im/message/${data.messageId}/reaction`,
    method: 'post',
    data: {
      messageId: Number(data.messageId),
      emoji: data.emoji,
    },
  })
}

/**
 * 删除消息表情反应
 * @param {number|string} messageId - 消息ID
 * @returns {Promise<Result<void>>}
 */
export function removeMessageReaction(messageId) {
  return request({
    url: `/api/im/message/${messageId}/reaction`,
    method: 'delete',
  })
}

/**
 * 获取消息的表情反应列表
 * @param {number|string} messageId - 消息ID
 * @returns {Promise<Result<MessageReaction[]>>}
 */
export function getMessageReactions(messageId) {
  return request({
    url: `/api/im/message/${messageId}/reactions`,
    method: 'get',
  })
}

/**
 * 获取消息的表情反应统计
 * @param {number|string} messageId - 消息ID
 * @returns {Promise<Result<MessageReaction[]>>}
 */
export function getReactionStats(messageId) {
  return request({
    url: `/api/im/message/${messageId}/reactions/stats`,
    method: 'get',
  })
}

// ==================== 消息收藏 ====================

/**
 * 收藏消息
 * @param {Object} data - 收藏数据
 * @param {number|string} data.messageId - 消息ID
 * @param {number|string} [data.conversationId] - 会话ID
 * @param {string} [data.remark] - 备注信息
 * @param {string} [data.tags] - 标签（逗号分隔）
 * @returns {Promise<Result<number>>} 返回收藏ID
 */
export function addMessageFavorite(data) {
  return request({
    url: `/api/im/message/favorite/${data.messageId}`,
    method: 'post',
    params: {
      conversationId: data.conversationId ? Number(data.conversationId) : undefined,
      remark: data.remark,
      tags: data.tags,
    },
  })
}

/**
 * 取消收藏消息
 * @param {number|string} messageId - 消息ID
 * @returns {Promise<Result<void>>}
 */
export function removeMessageFavorite(messageId) {
  return request({
    url: `/api/im/message/favorite/${messageId}`,
    method: 'delete',
  })
}

/**
 * 检查消息是否已收藏
 * @param {number|string} messageId - 消息ID
 * @returns {Promise<Result<boolean>>}
 */
export function isMessageFavorited(messageId) {
  return request({
    url: `/api/im/message/favorite/${messageId}/check`,
    method: 'get',
  })
}

/**
 * 获取用户收藏的消息列表
 * @returns {Promise<Result<FavoriteMessage[]>>}
 */
export function getFavoriteMessages() {
  return request({
    url: '/api/im/message/favorite/list',
    method: 'get',
  })
}

/**
 * 获取会话中收藏的消息列表
 * @param {number|string} conversationId - 会话ID
 * @returns {Promise<Result<FavoriteMessage[]>>}
 */
export function getConversationFavorites(conversationId) {
  return request({
    url: `/api/im/message/favorite/conversation/${conversationId}`,
    method: 'get',
  })
}

/**
 * 根据标签获取收藏消息
 * @param {string} tag - 标签
 * @returns {Promise<Result<FavoriteMessage[]>>}
 */
export function getFavoritesByTag(tag) {
  return request({
    url: `/api/im/message/favorite/tag/${tag}`,
    method: 'get',
  })
}

/**
 * 更新收藏备注和标签
 * @param {Object} data - 更新数据
 * @param {number|string} data.messageId - 消息ID
 * @param {string} [data.remark] - 备注信息
 * @param {string} [data.tags] - 标签（逗号分隔）
 * @returns {Promise<Result<void>>}
 */
export function updateMessageFavorite(data) {
  return request({
    url: `/api/im/message/favorite/${data.messageId}`,
    method: 'put',
    params: {
      remark: data.remark,
      tags: data.tags,
    },
  })
}

// ==================== @提及功能 ====================

/**
 * 获取未读@提及列表
 * @returns {Promise<Result<Mention[]>>}
 */
export function getUnreadMentions() {
  return request({
    url: '/api/im/message/mention/unread',
    method: 'get',
  })
}

/**
 * 获取未读@提及数量
 * @returns {Promise<Result<number>>}
 */
export function getUnreadMentionCount() {
  return request({
    url: '/api/im/message/mention/unread/count',
    method: 'get',
  })
}

/**
 * 标记@提及为已读
 * @param {number|string} messageId - 消息ID
 * @returns {Promise<Result<void>>}
 */
export function markMentionAsRead(messageId) {
  return request({
    url: `/api/im/message/${messageId}/mention/read`,
    method: 'put',
  })
}

/**
 * 批量标记@提及为已读
 * @param {number[]} mentionIds - 提及ID列表
 * @returns {Promise<Result<void>>}
 */
export function batchMarkMentionsAsRead(mentionIds) {
  return request({
    url: '/api/im/message/mention/read/batch',
    method: 'put',
    data: mentionIds.map(id => Number(id)),
  })
}

// ==================== 统计和其他 ====================

/**
 * 获取消息统计
 * @param {Object} params - 统计参数
 * @param {number|string} [params.conversationId] - 会话ID
 * @param {string} [params.startTime] - 开始时间
 * @param {string} [params.endTime] - 结束时间
 * @returns {Promise<Result<MessageStatistics>>}
 */
export function getMessageStatistics(params) {
  return request({
    url: '/api/im/message/statistics',
    method: 'get',
    params: {
      conversationId: params.conversationId ? Number(params.conversationId) : undefined,
      startTime: params.startTime,
      endTime: params.endTime,
    },
  })
}

/**
 * 获取最新消息
 * @param {number} [limit=10] - 获取数量
 * @returns {Promise<Result<Message[]>>}
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
 * @param {number|string} conversationId - 会话ID
 * @returns {Promise<Result<Object>>}
 */
export function getMessageTypeStatistics(conversationId) {
  return request({
    url: '/api/im/message/statistics',
    method: 'get',
    params: { conversationId },
  })
}

// ==================== 消息已读回执 ====================

/**
 * 标记会话消息已读
 * 将会话中指定消息之前的所有消息标记为已读
 * @param {Object} params - 参数
 * @param {number|string} params.conversationId - 会话ID
 * @param {number|string} [params.lastReadMessageId] - 最后已读消息ID
 * @returns {Promise<Result<void>>}
 */
export function markMessageRead(params) {
  return request({
    url: '/api/im/message/read',
    method: 'put',
    params: {
      conversationId: Number(params.conversationId),
      lastReadMessageId: params.lastReadMessageId ? Number(params.lastReadMessageId) : undefined,
    },
  })
}

/**
 * 获取会话未读消息数
 * @param {number|string} conversationId - 会话ID
 * @returns {Promise<Result<number>>}
 */
export function getConversationUnread(conversationId) {
  return request({
    url: `/api/im/message/unread/count/${conversationId}`,
    method: 'get',
  })
}

/**
 * 获取消息已读状态
 * @param {number|string} conversationId - 会话ID
 * @param {number|string} messageId - 消息ID
 * @returns {Promise<Result<{userId: number, readTime: string}[]>>}
 */
export function getReadStatus(conversationId, messageId) {
  return request({
    url: `/api/im/message/read/status/${conversationId}/${messageId}`,
    method: 'get',
  })
}

// ==================== 工具函数 ====================

/**
 * 规范化消息类型
 * @param {string} type - 原始消息类型
 * @returns {string} 规范化后的消息类型
 */
function normalizeMessageType(type) {
  const typeMap = {
    text: 'TEXT',
    image: 'IMAGE',
    img: 'IMAGE',
    file: 'FILE',
    voice: 'VOICE',
    video: 'VIDEO',
    location: 'LOCATION',
    oa: 'CARD',
    card: 'CARD',
  }
  const lowerType = (type || '').toLowerCase()
  return typeMap[lowerType] || (type || '').toUpperCase() || 'TEXT'
}

// ==================== 别名 - 向后兼容 ====================

export const getConversationMessages = listMessage
export const markRead = (conversationId, messageIds) =>
  markMessageRead({ conversationId, messageIds })
export const getUnread = getUnreadCount // 保持不变，获取所有未读总数

// ==================== 默认导出 ====================

export default {
  // 发送相关
  sendMessage,
  replyMessage,
  forwardMessage,

  // 查询相关
  listMessage,
  getMessageDetail,
  searchMessages,
  getLatestMessages,

  // 操作相关
  recallMessage,
  editMessage,
  deleteMessage,
  batchDeleteMessages,

  // 已读相关
  markMessageAsRead,
  markBatchMessageAsRead,
  markConversationAsRead,
  getMessageReadStatus,
  getMessageReadDetail,
  getConversationReadStatus,
  revokeMessageRead,
  markMessageRead, // 兼容
  getUnreadCount, // 获取所有未读总数
  getConversationUnread, // 获取指定会话未读数

  // 反应相关
  addMessageReaction,
  removeMessageReaction,
  getMessageReactions,
  getReactionStats,

  // 收藏相关
  addMessageFavorite,
  removeMessageFavorite,
  isMessageFavorited,
  getFavoriteMessages,
  getConversationFavorites,
  getFavoritesByTag,
  updateMessageFavorite,

  // @提及相关
  getUnreadMentions,
  getUnreadMentionCount,
  markMentionAsRead,
  batchMarkMentionsAsRead,

  // 统计相关
  getMessageStatistics,
  getMessageTypeStatistics,

  // 别名
  getConversationMessages,
  markRead,
  getUnread,
}
