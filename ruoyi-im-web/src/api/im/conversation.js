/**
 * 会话相关 API
 */
import request from '../request'

/**
 * 获取会话列表
 * @param {string} filter - 筛选类型：all-全部，unread-未读，pinned-置顶，muted-免打扰，group-群聊，file-文件链接
 * @returns {Promise}
 */
export function getConversations(filter = 'all') {
  return request({
    url: '/api/im/conversation/list',
    method: 'get',
    params: { filter }
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
 * 保存草稿
 * @param {number} conversationId - 会话ID
 * @param {string} draft - 草稿内容
 * @returns {Promise}
 */
export function saveDraft(conversationId, draft) {
  return request({
    url: `/api/im/conversation/${conversationId}/draft`,
    method: 'put',
    data: { draft }
  })
}

/**
 * 更新会话设置
 * @param {number} conversationId - 会话ID
 * @param {Object} data - 会话数据
 * @param {boolean} data.pinned - 是否置顶
 * @param {boolean} data.muted - 是否免打扰
 * @param {string} data.draft - 草稿
 * @returns {Promise}
 */
export function updateConversation(conversationId, data) {
  return request({
    url: `/api/im/conversation/${conversationId}`,
    method: 'put',
    data
  })
}

/**
 * 置顶消息
 * @param {number} conversationId - 会话ID
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function pinMessage(conversationId, messageId) {
  return request({
    url: `/api/im/conversation/${conversationId}/pin/${messageId}`,
    method: 'post'
  })
}

/**
 * 取消置顶消息
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 */
export function unpinMessage(conversationId) {
  return request({
    url: `/api/im/conversation/${conversationId}/unpin`,
    method: 'delete'
  })
}
