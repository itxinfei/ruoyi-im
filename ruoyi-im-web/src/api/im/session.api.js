import request from '@/utils/request'

// 获取会话列表
export function getSessionList(params) {
  return request({
    url: '/im/session/list',
    method: 'get',
    params,
  })
}

// 创建会话
export function createSession(data) {
  return request({
    url: '/im/session/create',
    method: 'post',
    data,
  })
}

// 删除会话
export function deleteSession(sessionId) {
  return request({
    url: `/im/session/${sessionId}`,
    method: 'delete',
  })
}

// 清空会话消息
export function clearSessionMessages(sessionId) {
  return request({
    url: `/im/session/${sessionId}/clear`,
    method: 'put',
  })
}

// 置顶会话
export function pinSession(sessionId) {
  return request({
    url: `/im/session/${sessionId}/pin`,
    method: 'put',
  })
}

// 取消置顶会话
export function unpinSession(sessionId) {
  return request({
    url: `/im/session/${sessionId}/unpin`,
    method: 'put',
  })
}

// 设置会话免打扰
export function muteSession(sessionId) {
  return request({
    url: `/im/session/${sessionId}/mute`,
    method: 'put',
  })
}

// 取消会话免打扰
export function unmuteSession(sessionId) {
  return request({
    url: `/im/session/${sessionId}/unmute`,
    method: 'put',
  })
}

// 获取会话未读消息数
export function getSessionUnreadCount(sessionId) {
  return request({
    url: `/im/session/${sessionId}/unread`,
    method: 'get',
  })
}

// 标记会话已读
export function markSessionRead(sessionId) {
  return request({
    url: `/im/session/${sessionId}/read`,
    method: 'put',
  })
}

// 获取会话分组列表
export function getSessionGroups() {
  return request({
    url: '/im/session/groups',
    method: 'get',
  })
}

// 创建会话分组
export function createSessionGroup(data) {
  return request({
    url: '/im/session/group',
    method: 'post',
    data,
  })
}

// 更新会话分组
export function updateSessionGroup(groupId, data) {
  return request({
    url: `/im/session/group/${groupId}`,
    method: 'put',
    data,
  })
}

// 删除会话分组
export function deleteSessionGroup(groupId) {
  return request({
    url: `/im/session/group/${groupId}`,
    method: 'delete',
  })
}

// 移动会话到分组
export function moveSessionToGroup(sessionId, groupId) {
  return request({
    url: `/im/session/${sessionId}/move`,
    method: 'put',
    data: { groupId },
  })
}

// 搜索会话
export function searchSessions(query) {
  return request({
    url: '/im/session/search',
    method: 'get',
    params: query,
  })
}

// 获取会话详情
export function getSessionDetail(sessionId) {
  return request({
    url: `/im/session/${sessionId}`,
    method: 'get',
  })
}

// 更新会话设置
export function updateSessionSettings(sessionId, settings) {
  return request({
    url: `/im/session/${sessionId}/settings`,
    method: 'put',
    data: settings,
  })
}

// 获取会话成员列表
export function getSessionMembers(sessionId) {
  return request({
    url: `/im/session/${sessionId}/members`,
    method: 'get',
  })
}

// 获取置顶会话列表
export function getPinnedSessions() {
  return request({
    url: '/im/session/pinned',
    method: 'get',
  })
}

// 获取免打扰会话列表
export function getMutedSessions() {
  return request({
    url: '/im/session/muted',
    method: 'get',
  })
}

// 批量操作会话
export function batchOperateSessions(data) {
  return request({
    url: '/im/session/batch',
    method: 'post',
    data,
  })
}

// 获取会话统计信息
export function getSessionStatistics(sessionId) {
  return request({
    url: `/im/session/${sessionId}/statistics`,
    method: 'get',
  })
}

// 归档会话
export function archiveSession(sessionId) {
  return request({
    url: `/im/session/${sessionId}/archive`,
    method: 'put',
  })
}

// 取消归档会话
export function unarchiveSession(sessionId) {
  return request({
    url: `/im/session/${sessionId}/unarchive`,
    method: 'put',
  })
}

// 获取归档会话列表
export function getArchivedSessions() {
  return request({
    url: '/im/session/archived',
    method: 'get',
  })
}

// 设置会话颜色
export function setSessionColor(sessionId, color) {
  return request({
    url: `/im/session/${sessionId}/color`,
    method: 'put',
    data: { color },
  })
}

// 设置会话自定义标签
export function setSessionTags(sessionId, tags) {
  return request({
    url: `/im/session/${sessionId}/tags`,
    method: 'put',
    data: { tags },
  })
}

// 获取会话最近活跃时间
export function getSessionLastActivity(sessionId) {
  return request({
    url: `/im/session/${sessionId}/activity`,
    method: 'get',
  })
}

// 获取会话提醒设置
export function getSessionNotificationSettings(sessionId) {
  return request({
    url: `/im/session/${sessionId}/notifications`,
    method: 'get',
  })
}

// 更新会话提醒设置
export function updateSessionNotificationSettings(sessionId, settings) {
  return request({
    url: `/im/session/${sessionId}/notifications`,
    method: 'put',
    data: settings,
  })
}

// 获取会话参与者在线状态
export function getSessionMembersStatus(sessionId) {
  return request({
    url: `/im/session/${sessionId}/members/status`,
    method: 'get',
  })
}
