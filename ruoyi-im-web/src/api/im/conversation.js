/**
 * 会话相关API
 * @module api/im/conversation
 */
import request from '@/utils/request'

/**
 * 获取会话列表
 * @param {Object} params - 查询参数
 * @param {number} [params.pageSize=20] - 每页数量
 * @param {number} [params.pageNum=1] - 当前页码
 * @returns {Promise}
 */
export function listConversation(params) {
  return request({
    url: '/api/im/conversation/list',
    method: 'get',
    params: {
      pageSize: params?.pageSize || 20,
      pageNum: params?.pageNum || 1,
    },
  })
}

/**
 * 获取会话详情
 * @param {string} conversationId - 会话ID
 * @returns {Promise}
 */
export function getConversation(conversationId) {
  return request({
    url: `/api/im/conversation/${conversationId}`,
    method: 'get',
  })
}

/**
 * 创建会话
 * @param {Object} data - 会话数据
 * @param {string} data.type - 会话类型 PRIVATE/GROUP
 * @param {string} [data.targetId] - 目标用户ID（私聊）
 * @param {Object} [data.groupInfo] - 群组信息（群聊）
 * @returns {Promise}
 */
export function createConversation(data) {
  return request({
    url: '/api/im/conversation/create',
    method: 'post',
    data,
  })
}

/**
 * 更新会话
 * @param {string} conversationId - 会话ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export function updateConversation(conversationId, data) {
  return request({
    url: `/api/im/conversation/${conversationId}`,
    method: 'put',
    data,
  })
}

/**
 * 删除会话
 * @param {string} conversationId - 会话ID
 * @returns {Promise}
 */
export function deleteConversation(conversationId) {
  return request({
    url: `/api/im/conversation/${conversationId}`,
    method: 'delete',
  })
}

/**
 * 置顶/取消置顶会话
 * @param {string} conversationId - 会话ID
 * @param {boolean} pinned - 是否置顶
 * @returns {Promise}
 */
export function setConversationPinned(conversationId, pinned) {
  return request({
    url: `/api/im/conversation/${conversationId}/pinned`,
    method: 'put',
    params: { pinned },
  })
}

/**
 * 设置免打扰
 * @param {string} conversationId - 会话ID
 * @param {boolean} muted - 是否免打扰
 * @returns {Promise}
 */
export function setConversationMuted(conversationId, muted) {
  return request({
    url: `/api/im/conversation/${conversationId}/muted`,
    method: 'put',
    params: { muted },
  })
}

/**
 * 搜索会话
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchConversation(keyword) {
  return request({
    url: '/api/im/conversation/search',
    method: 'get',
    params: { keyword },
  })
}

/**
 * 标记会话为已读
 * @param {string} conversationId - 会话ID
 * @returns {Promise}
 */
export function markConversationRead(conversationId) {
  return request({
    url: `/api/im/conversation/${conversationId}/markAsRead`,
    method: 'put',
  })
}

/**
 * 获取未读消息总数
 * @returns {Promise}
 */
export function getTotalUnreadCount() {
  return request({
    url: '/api/im/conversation/unreadCount',
    method: 'get',
  })
}

/**
 * 创建私聊会话
 * @param {string} peerUserId - 对方用户ID
 * @returns {Promise}
 */
export function createPrivateConversation(peerUserId) {
  return createConversation({
    type: 'PRIVATE',
    targetId: peerUserId,
  })
}

/**
 * 创建群聊会话
 * @param {Object} data - 群组信息
 * @returns {Promise}
 */
export function createGroupConversation(data) {
  return createConversation({
    type: 'GROUP',
    ...data,
  })
}

// ========== 别名 - 向后兼容 ==========

// 旧函数名别名
export const list = listConversation
export const get = getConversation
export const create = createConversation
export const update = updateConversation
export const del = deleteConversation // delete 是保留关键字，使用 del
export const setPinned = setConversationPinned
export const setMuted = setConversationMuted
export const search = searchConversation
export const markAsRead = markConversationRead
export const getTotalUnread = getTotalUnreadCount
export const createPrivate = createPrivateConversation
export const createGroup = createGroupConversation

// 默认导出 - 方便批量引入
export default {
  listConversation,
  getConversation,
  createConversation,
  updateConversation,
  deleteConversation,
  del: deleteConversation, // delete 别名
  setConversationPinned,
  setConversationMuted,
  searchConversation,
  markConversationRead,
  getTotalUnreadCount,
  createPrivateConversation,
  createGroupConversation,
  // 别名
  list,
  get,
  create,
  update,
  delete: deleteConversation,
  setPinned,
  setMuted,
  search,
  markAsRead,
  getTotalUnread,
  createPrivate,
  createGroup,
}
