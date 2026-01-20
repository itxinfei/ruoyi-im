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
    url: '/im/conversation/list',
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
    url: `/im/conversation/${conversationId}`,
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
    url: '/im/conversation/create',
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
    url: `/im/conversation/${conversationId}`,
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
export function pinConversation(data) {
  return request({
    url: '/im/conversation/pin',
    method: 'put',
    data
  })
}

/**
 * 设置会话免打扰
 * @param {Object} data - 免打扰数据
 * @param {number} data.conversationId - 会话ID
 * @param {boolean} data.isMuted - 是否免打扰
 * @returns {Promise}
 */
export function muteConversation(data) {
  return request({
    url: '/im/conversation/mute',
    method: 'put',
    data
  })
}

/**
 * 清除会话未读数
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 */
export function clearUnread(conversationId) {
  return request({
    url: `/im/conversation/${conversationId}/clear-unread`,
    method: 'put'
  })
}
