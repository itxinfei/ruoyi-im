/**
 * 会话相关 API
 */
import request from '../request'

/**
 * 获取会话列表
 * @returns {Promise}
 */
export function getConversations() {
  return request({
    url: '/api/im/conversation/list',
    method: 'get'
  })
}

/**
 * 获取会话详情
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 */
export function getConversation(conversationId) {
  return request({
    url: `/api/im/conversation/${conversationId}`,
    method: 'get'
  })
}

/**
 * 创建会话
 * @param {Object} data - 会话数据
 * @param {string} data.type - 会话类型 PRIVATE/GROUP
 * @param {number} data.targetId - 目标ID（私聊为用户ID，群聊为群组ID）
 * @returns {Promise}
 */
export function createConversation(data) {
  return request({
    url: '/api/im/conversation/create',
    method: 'post',
    data
  })
}

/**
 * 删除会话
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 */
export function deleteConversation(conversationId) {
  return request({
    url: `/api/im/conversation/${conversationId}`,
    method: 'delete'
  })
}

/**
 * 置顶会话
 * @param {Object} data - 置顶数据
 * @param {number} data.conversationId - 会话ID
 * @param {boolean} data.isPinned - 是否置顶
 * @returns {Promise}
 */
export function pinConversation(conversationId, pinned) {
  return request({
    url: `/api/im/conversation/${conversationId}/pinned`,
    method: 'put',
    params: { pinned }
  })
}

/**
 * 设置会话免打扰
 * @param {Object} data - 免打扰数据
 * @param {number} data.conversationId - 会话ID
 * @param {boolean} data.isMuted - 是否免打扰
 * @returns {Promise}
 */
export function muteConversation(conversationId, muted) {
  return request({
    url: `/api/im/conversation/${conversationId}/muted`,
    method: 'put',
    params: { muted }
  })
}

export function markConversationAsRead(conversationId) {
  return request({
    url: `/api/im/conversation/${conversationId}/markAsRead`,
    method: 'put'
  })
}

/**
 * 搜索会话
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchConversations(keyword) {
  return request({
    url: '/api/im/conversation/search',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 获取未读消息总数
 * @returns {Promise}
 */
export function getTotalUnreadCount() {
  return request({
    url: '/api/im/conversation/unreadCount',
    method: 'get'
  })
}

/**
 * 获取归档会话列表
 * @returns {Promise}
 */
export function getArchivedSessions() {
  return request({
    url: '/api/im/conversation/archived',
    method: 'get'
  })
}

/**
 * 归档会话
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 */
export function archiveSession(conversationId) {
  return request({
    url: `/api/im/conversation/${conversationId}/archive`,
    method: 'put'
  })
}

/**
 * 取消归档会话
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 */
export function unarchiveSession(conversationId) {
  return request({
    url: `/api/im/conversation/${conversationId}/unarchive`,
    method: 'put'
  })
}
