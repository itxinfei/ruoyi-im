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
    url: '/api/im/conversations/list',
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
    url: `/api/im/conversations/${conversationId}`,
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
    url: '/api/im/conversations/create',
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
    url: `/api/im/conversations/${conversationId}`,
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
    url: `/api/im/conversations/${conversationId}/pinned`,
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
    url: `/api/im/conversations/${conversationId}/muted`,
    method: 'put',
    params: { muted }
  })
}

export function markConversationAsRead(conversationId) {
  return request({
    url: `/api/im/conversations/${conversationId}/markAsRead`,
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
    url: '/api/im/conversations/search',
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
    url: '/api/im/conversations/unreadCount',
    method: 'get'
  })
}

/**
 * 获取归档会话列表
 * @returns {Promise}
 */
export function getArchivedSessions() {
  return request({
    url: '/api/im/conversations/archived',
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
    url: `/api/im/conversations/${conversationId}/archive`,
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
    url: `/api/im/conversations/${conversationId}/unarchive`,
    method: 'put'
  })
}

// ==================== 会话同步 ====================

/**
 * 同步会话事件
 * 获取自上次同步以来发生的会话事件（置顶、免打扰、删除、归档、已读等）
 * 用于多设备间会话设置同步
 * @param {Object} params - 同步参数
 * @param {string} params.deviceId - 设备ID（从请求头X-Device-Id获取）
 * @param {number} params.lastSyncTime - 上次同步时间戳（毫秒），可选
 * @returns {Promise} 会话事件列表和新的同步时间戳
 */
export function syncSessions(params = {}) {
  return request({
    url: '/api/im/conversations/sync',
    method: 'get',
    params,
    headers: params.deviceId ? { 'X-Device-Id': params.deviceId } : {}
  })
}

/**
 * 重置会话同步点
 * 删除指定设备的同步点，下次同步将获取全部事件
 * @param {string} deviceId - 设备ID
 * @returns {Promise}
 */
export function resetSessionSyncPoint(deviceId) {
  return request({
    url: '/api/im/conversations/sync',
    method: 'delete',
    headers: { 'X-Device-Id': deviceId }
  })
}

/**
 * 获取会话同步点
 * @returns {Promise} 同步点信息
 */
export function getSessionSyncPoints() {
  return request({
    url: '/api/im/session/sync/points',
    method: 'get'
  })
}
