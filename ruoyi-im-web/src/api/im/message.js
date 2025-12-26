import request from '@/utils/request'

// 发送消息
export function sendMessage(data) {
  return request({
    url: '/im/message/send',
    method: 'post',
    data: data,
  })
}

// 获取会话消息列表
export function getConversationMessages(params) {
  return request({
    url: '/im/message/list',
    method: 'get',
    params: params,
  })
}

// 标记消息已读
export function markMessageRead(conversationId, data) {
  return request({
    url: `/im/message/read/${conversationId}`,
    method: 'put',
    data: data,
  })
}

// 撤回消息
export function recallMessage(messageId) {
  return request({
    url: `/im/message/recall/${messageId}`,
    method: 'put',
  })
}

// 搜索消息
export function searchMessages(params) {
  return request({
    url: '/im/message/search',
    method: 'get',
    params: params,
  })
}

// 获取消息详情
export function getMessageDetail(messageId) {
  return request({
    url: `/im/message/${messageId}`,
    method: 'get',
  })
}

// 删除消息
export function deleteMessage(messageId) {
  return request({
    url: `/im/message/delete/${messageId}`,
    method: 'delete',
  })
}

// 批量删除消息
export function batchDeleteMessages(messageIds) {
  return request({
    url: '/im/message/batch',
    method: 'delete',
    data: messageIds,
  })
}

// 获取未读消息数量
export function getUnreadCount() {
  return request({
    url: '/im/message/unread/count',
    method: 'get',
  })
}

// 获取会话未读消息数量
export function getConversationUnreadCount(conversationId) {
  return request({
    url: `/im/message/unread/conversation/${conversationId}`,
    method: 'get',
  })
}

// 转发消息
export function forwardMessage(data) {
  return request({
    url: '/im/message/forward',
    method: 'post',
    data: data,
  })
}

// 回复消息
export function replyMessage(data) {
  return request({
    url: '/im/message/reply',
    method: 'post',
    data: data,
  })
}

// 获取消息统计
export function getMessageStatistics(params) {
  return request({
    url: '/im/message/statistics',
    method: 'get',
    params: params,
  })
}

// 获取最新消息
export function getLatestMessages(limit = 10) {
  return request({
    url: '/im/message/latest',
    method: 'get',
    params: { limit },
  })
}

// 获取消息类型统计
export function getMessageTypeStatistics(conversationId) {
  return request({
    url: '/im/message/statistics',
    method: 'get',
    params: { conversationId },
  })
}
