/**
 * 消息收藏相关 API
 * 对应后端 ImMessageFavoriteController
 */
import request from '../request'

/**
 * 收藏消息
 * @param {Object} data - 收藏数据
 * @param {number} data.messageId - 消息ID
 * @param {number} data.conversationId - 会话ID（可选）
 * @param {string} data.remark - 备注（可选）
 * @param {string} data.tags - 标签，逗号分隔（可选）
 * @returns {Promise}
 */
export function addFavorite(data) {
  return request({
    url: '/api/im/messages/favorites',
    method: 'post',
    params: {
      messageId: data.messageId,
      conversationId: data.conversationId,
      remark: data.remark,
      tags: data.tags
    }
  })
}

/**
 * 取消收藏消息
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function removeFavorite(messageId) {
  return request({
    url: '/api/im/messages/favorites',
    method: 'delete',
    params: { messageId }
  })
}

/**
 * 检查消息是否已收藏
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function isFavorited(messageId) {
  return request({
    url: '/api/im/messages/favorites/check',
    method: 'get',
    params: { messageId }
  })
}

/**
 * 获取用户收藏的消息列表
 * @returns {Promise}
 */
export function getUserFavorites() {
  return request({
    url: '/api/im/messages/favorites',
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
    url: '/api/im/messages/favorites/conversations',
    method: 'get',
    params: { conversationId }
  })
}

/**
 * 根据标签获取收藏消息
 * @param {string} tag - 标签名称
 * @returns {Promise}
 */
export function getFavoritesByTag(tag) {
  return request({
    url: '/api/im/messages/favorites/tags',
    method: 'get',
    params: { tag }
  })
}

/**
 * 更新收藏备注和标签
 * @param {Object} data - 更新数据
 * @param {number} data.messageId - 消息ID
 * @param {string} data.remark - 备注（可选）
 * @param {string} data.tags - 标签（可选）
 * @returns {Promise}
 */
export function updateFavorite(data) {
  return request({
    url: '/api/im/messages/favorites',
    method: 'put',
    params: {
      messageId: data.messageId,
      remark: data.remark,
      tags: data.tags
    }
  })
}

export default {
  addFavorite,
  removeFavorite,
  isFavorited,
  getUserFavorites,
  getConversationFavorites,
  getFavoritesByTag,
  updateFavorite
}
