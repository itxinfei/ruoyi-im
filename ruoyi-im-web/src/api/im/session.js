/**
 * 会话模块API - 向后兼容层
 * @module api/im/session
 * @deprecated 请使用 conversation.js，此文件仅为兼容旧代码保留
 * @description 内部已重定向到 /api/im/conversation 端点，符合API规范
 */

// 导入 conversation API 的所有函数
export * from './conversation'

// 为了保持完全兼容，导出别名映射
import {
  listConversation,
  getConversation,
  createConversation,
  updateConversation,
  deleteConversation,
  setConversationPinned,
  setConversationMuted,
  markConversationRead,
  getTotalUnreadCount,
  createPrivateConversation,
  createGroupConversation,
  searchConversation,
} from './conversation'

// ==================== 别名映射 - session → conversation ====================
/**
 * @deprecated 请使用 listConversation
 */
export const listSession = listConversation

/**
 * @deprecated 请使用 getConversation
 */
export const getSession = getConversation

/**
 * @deprecated 请使用 createConversation
 */
export const createSession = createConversation

/**
 * @deprecated 请使用 updateConversation
 */
export const updateSession = updateConversation

/**
 * @deprecated 请使用 deleteConversation
 */
export const deleteSession = deleteConversation

/**
 * @deprecated 请使用 setConversationPinned
 */
export const setSessionPinned = setConversationPinned

/**
 * @deprecated 请使用 setConversationMuted
 */
export const setSessionMuted = setConversationMuted

/**
 * @deprecated 请使用 createPrivateConversation
 */
export const createPrivateSession = createPrivateConversation

/**
 * @deprecated 请使用 createGroupConversation
 */
export const createGroupSession = createGroupConversation

/**
 * @deprecated 请使用 searchConversation
 */
export const searchSession = searchConversation

/**
 * @deprecated 请使用 markConversationRead
 */
export const markSessionRead = markConversationRead

/**
 * @deprecated 请使用 getTotalUnreadCount
 */
export const getSessionUnreadCount = getTotalUnreadCount

/**
 * @deprecated 请使用 conversation.js 中的 listMessage
 * 获取会话消息列表
 * @param {string} sessionId - 会话ID
 * @param {Object} [params] - 查询参数
 * @returns {Promise}
 */
export function listSessionMessages(sessionId, params = {}) {
  // 动态导入 message API 以避免循环依赖
  return import('./message').then(({ listMessage }) => {
    return listMessage({ conversationId: sessionId, ...params })
  })
}

// ==================== 更多别名 ====================

export const addSession = createConversation
export const delSession = deleteConversation
export const add = createConversation
export const get = getConversation
export const list = listConversation
export const update = updateConversation
export const del = deleteConversation
export const setPinned = setConversationPinned
export const setMuted = setConversationMuted
export const search = searchConversation
export const markAsRead = markConversationRead
export const getTotalUnread = getTotalUnreadCount

// 默认导出
export default {
  listSession,
  getSession,
  createSession,
  updateSession,
  deleteSession,
  setSessionPinned,
  setSessionMuted,
  listSessionMessages,
  // 别名
  addSession,
  delSession,
  add,
  get,
  list,
  update,
  del,
  setPinned,
  setMuted,
  search,
  markAsRead,
  getTotalUnread,
  // 直接映射到 conversation API
  listConversation,
  getConversation,
  createConversation,
  updateConversation,
  deleteConversation,
  setConversationPinned,
  setConversationMuted,
  searchConversation,
  markConversationRead,
  getTotalUnreadCount,
}
