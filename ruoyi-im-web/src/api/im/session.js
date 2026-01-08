/**
 * 会话模块API
 * @module api/im/session
 * @deprecated 建议使用 conversation.js，session 仅为兼容保留
 */
import request from '@/utils/request'

/**
 * 获取会话列表
 * @param {Object} [params] - 查询参数
 * @param {number} [params.pageSize=20] - 每页数量
 * @param {number} [params.pageNum=1] - 当前页码
 * @returns {Promise}
 */
export function listSession(params) {
  return request({
    url: '/api/im/session/list',
    method: 'get',
    params: {
      pageSize: params?.pageSize || 20,
      pageNum: params?.pageNum || 1,
    },
  })
}

/**
 * 获取会话详情
 * @param {string} sessionId - 会话ID
 * @returns {Promise}
 */
export function getSession(sessionId) {
  return request({
    url: `/api/im/session/${sessionId}`,
    method: 'get',
  })
}

/**
 * 创建会话
 * @param {Object} data - 会话数据
 * @returns {Promise}
 */
export function createSession(data) {
  return request({
    url: '/api/im/session',
    method: 'post',
    data,
  })
}

/**
 * 更新会话
 * @param {string} sessionId - 会话ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export function updateSession(sessionId, data) {
  // 置顶操作
  if (data.pinned !== undefined) {
    return request({
      url: `/api/im/session/${sessionId}/pin`,
      method: 'put',
      params: { pinned: data.pinned ? 1 : 0 },
    })
  }
  // 静音操作
  if (data.muted !== undefined) {
    return request({
      url: `/api/im/session/${sessionId}/mute`,
      method: 'put',
      params: { muted: data.muted ? 1 : 0 },
    })
  }
  // 其他更新
  return request({
    url: `/api/im/session/${sessionId}`,
    method: 'put',
    data,
  })
}

/**
 * 删除会话
 * @param {string} sessionId - 会话ID
 * @returns {Promise}
 */
export function deleteSession(sessionId) {
  return request({
    url: `/api/im/session/${sessionId}`,
    method: 'delete',
  })
}

/**
 * 设置会话置顶
 * @param {string} sessionId - 会话ID
 * @param {boolean} pinned - 是否置顶
 * @returns {Promise}
 */
export function setSessionPinned(sessionId, pinned) {
  return updateSession(sessionId, { pinned })
}

/**
 * 设置会话静音
 * @param {string} sessionId - 会话ID
 * @param {boolean} muted - 是否静音
 * @returns {Promise}
 */
export function setSessionMuted(sessionId, muted) {
  return updateSession(sessionId, { muted })
}

/**
 * 获取会话消息列表
 * @param {string} sessionId - 会话ID
 * @param {Object} [params] - 查询参数
 * @returns {Promise}
 */
export function listSessionMessages(sessionId, params = {}) {
  return request({
    url: `/api/im/message/list/${sessionId}`,
    method: 'get',
    params,
  })
}

// ========== 别名 - 向后兼容 ==========

export const addSession = createSession
export const delSession = deleteSession

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
}
