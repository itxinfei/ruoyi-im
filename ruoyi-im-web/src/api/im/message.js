import request from '@/utils/request'

// 发送消息
export function sendMessage(data) {
  // 转换字段名以匹配后端API
  const params = {
    conversationId: data.sessionId || data.conversationId,
    type: data.type,
    content: data.content,
    replyToMessageId: data.replyTo || data.replyToMessageId,
    clientMsgId: data.clientMsgId,
  }
  return request({
    url: '/api/im/message/send',
    method: 'post',
    data: params,
  })
}

// 获取会话消息列表
export function listMessage(params) {
  return request({
    url: `/api/im/message/list/${params.sessionId || params.conversationId}`,
    method: 'get',
    params: {
      limit: params.pageSize || 20,
      lastId: params.lastMessageId,
    },
  })
}

// 别名，兼容旧代码
export const getConversationMessages = listMessage

// 标记消息已读
export function markMessageRead(conversationId, data) {
  return request({
    url: `/api/im/message/read/${conversationId}`,
    method: 'put',
    data: data,
  })
}

// 撤回消息
export function recallMessage(messageId) {
  return request({
    url: `/api/im/message/${messageId}/recall`,
    method: 'delete',
  })
}

// 搜索消息
export function searchMessages(params) {
  return request({
    url: '/api/im/message/search',
    method: 'get',
    params: params,
  })
}

// 获取消息详情
export function getMessageDetail(messageId) {
  return request({
    url: `/api/im/message/${messageId}`,
    method: 'get',
  })
}

// 删除消息
export function deleteMessage(messageId) {
  return request({
    url: `/api/admin/im/message/${messageId}`,
    method: 'delete',
  })
}

// 批量删除消息
export function batchDeleteMessages(messageIds) {
  return request({
    url: '/api/im/message/batch',
    method: 'delete',
    data: messageIds,
  })
}

// 获取未读消息数量
export function getUnreadCount() {
  return request({
    url: '/api/admin/im/message/unread/count',
    method: 'get',
  })
}

// 获取会话未读消息数量
export function getConversationUnreadCount(conversationId) {
  return request({
    url: `/api/admin/im/message/unread/conversation/${conversationId}`,
    method: 'get',
  })
}

// 转发消息
export function forwardMessage(data) {
  return request({
    url: '/api/im/message/forward',
    method: 'post',
    data: data,
  })
}

// 回复消息
export function replyMessage(data) {
  return request({
    url: '/api/im/message/reply',
    method: 'post',
    data: data,
  })
}

// 获取消息统计
export function getMessageStatistics(params) {
  return request({
    url: '/api/im/message/statistics',
    method: 'get',
    params: params,
  })
}

// 获取最新消息
export function getLatestMessages(limit = 10) {
  return request({
    url: '/api/im/message/latest',
    method: 'get',
    params: { limit },
  })
}

// 获取消息类型统计
export function getMessageTypeStatistics(conversationId) {
  return request({
    url: '/api/im/message/statistics',
    method: 'get',
    params: { conversationId },
  })
}
