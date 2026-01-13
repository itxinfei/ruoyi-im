/**
 * @提及功能 API
 */
import request from '@/utils/request'

const BASE_URL = '/api/im/mention'

/**
 * 获取可@用户列表
 * @param {number} conversationId - 会话ID
 */
export function getMentionableUsers(conversationId) {
  return request({
    url: `${BASE_URL}/users/${conversationId}`,
    method: 'get',
  })
}

/**
 * 检查是否可@所有人
 * @param {number} conversationId - 会话ID
 */
export function canMentionAll(conversationId) {
  return request({
    url: `${BASE_URL}/can-mention-all/${conversationId}`,
    method: 'get',
  })
}

/**
 * 获取用户未读的@提及列表
 */
export function getUnreadMentions() {
  return request({
    url: `${BASE_URL}/unread`,
    method: 'get',
  })
}

/**
 * 获取用户未读的@提及数量
 */
export function getUnreadMentionCount() {
  return request({
    url: `${BASE_URL}/unread/count`,
    method: 'get',
  })
}

/**
 * 标记@提及已读
 * @param {number} messageId - 消息ID
 */
export function markMentionAsRead(messageId) {
  return request({
    url: `${BASE_URL}/${messageId}/read`,
    method: 'put',
  })
}

/**
 * 批量标记@提及已读
 * @param {Array<number>} mentionIds - 提及ID列表
 */
export function batchMarkMentionsAsRead(mentionIds) {
  return request({
    url: `${BASE_URL}/read/batch`,
    method: 'put',
    data: mentionIds,
  })
}
