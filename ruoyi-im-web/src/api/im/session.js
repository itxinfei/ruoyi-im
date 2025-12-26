import request from '@/utils/request'

// 查询会话列表
export function listSession(query) {
  return request({
    url: '/im/session/list',
    method: 'get',
    params: query,
  })
}

// 查询会话详细信息
export function getSession(sessionId) {
  return request({
    url: '/im/session/' + sessionId,
    method: 'get',
  })
}

// 新增会话
export function addSession(data) {
  return request({
    url: '/im/session',
    method: 'post',
    data: data,
  })
}

// 修改会话
export function updateSession(data) {
  return request({
    url: '/im/session',
    method: 'put',
    data: data,
  })
}

// 删除会话
export function delSession(sessionId) {
  return request({
    url: '/im/session/' + sessionId,
    method: 'delete',
  })
}

// 批量删除会话
export function delSessions(sessionIds) {
  return request({
    url: '/im/session/batch/' + sessionIds,
    method: 'delete',
  })
}

// 获取会话成员列表
export function listSessionMembers(sessionId) {
  return request({
    url: '/im/session/members/' + sessionId,
    method: 'get',
  })
}

// 添加会话成员
export function addSessionMember(data) {
  return request({
    url: '/im/session/member',
    method: 'post',
    data: data,
  })
}

// 移除会话成员
export function removeSessionMember(sessionId, memberIds) {
  return request({
    url: '/im/session/member/' + sessionId + '/' + memberIds,
    method: 'delete',
  })
}

// 获取会话未读消息数
export function getUnreadCount(sessionId) {
  return request({
    url: '/im/session/unread/' + sessionId,
    method: 'get',
  })
}

// 获取用户活跃会话数量
export function getActiveCount() {
  return request({
    url: '/im/session/active/count',
    method: 'get',
  })
}

// 清理过期会话
export function cleanExpiredSessions(days) {
  return request({
    url: '/im/session/clean/' + days,
    method: 'delete',
  })
}

// 更新会话活动时间
export function updateActiveTime(sessionId) {
  return request({
    url: '/im/session/active/' + sessionId,
    method: 'put',
  })
}

// 获取会话消息列表
export function listSessionMessages(query) {
  return request({
    url: '/im/message/list',
    method: 'get',
    params: query,
  })
}

// 导出会话消息
export function exportSessionMessages(sessionId) {
  return request({
    url: '/im/message/export',
    method: 'get',
    params: {
      sessionId: sessionId,
    },
  })
}

// 获取会话统计信息
export function getSessionStats(sessionId) {
  return request({
    url: '/im/session/stats/' + sessionId,
    method: 'get',
  })
}
