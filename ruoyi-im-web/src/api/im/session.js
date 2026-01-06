import request from '@/utils/request'

// 查询会话列表
export function listSession(query) {
  return request({
    url: '/api/im/session/list',
    method: 'get',
    params: query,
  })
}

// 查询会话详细信息
export function getSession(sessionId) {
  return request({
    url: '/api/im/session/' + sessionId,
    method: 'get',
  })
}

// 新增会话 - 后端没有提供此API，需要使用会话服务创建会话
export function addSession(data) {
  return request({
    url: '/api/im/session',
    method: 'post',
    data: data,
  })
}

// 修改会话 - 根据具体操作调用不同接口
export function updateSession(sessionId, data) {
  // 如果是置顶操作
  if (data.pinned !== undefined) {
    return request({
      url: `/api/im/session/${sessionId}/pin`,
      method: 'put',
      params: {
        pinned: data.pinned ? 1 : 0
      }
    })
  }
  // 如果是静音操作
  if (data.muted !== undefined) {
    return request({
      url: `/api/im/session/${sessionId}/mute`,
      method: 'put',
      params: {
        muted: data.muted ? 1 : 0
      }
    })
  }
  // 其他更新操作
  return request({
    url: `/api/im/session/${sessionId}`,
    method: 'put',
    data: data,
  })
}

// 删除会话
export function deleteSession(sessionId) {
  return request({
    url: '/api/im/session/' + sessionId,
    method: 'delete',
  })
}

// 删除会话（别名）
export function delSession(sessionId) {
  return deleteSession(sessionId)
}

// 批量删除会话 - 后端没有提供此API，需要逐个删除
export function delSessions(sessionIds) {
  // 使用Promise.all同时删除多个会话
  const promises = sessionIds.map(id => 
    request({
      url: `/api/im/session/${id}`,
      method: 'delete',
    })
  );
  return Promise.all(promises);
}

// 获取会话成员列表 - 群聊相关，应该在群组API中
export function listSessionMembers(sessionId) {
  // 实际上群成员信息应该通过群组API获取
  // 这里暂时返回空，实际应用中需要调用群组相关API
  return Promise.resolve({ data: [] });
}

// 添加会话成员 - 群聊相关，应该在群组API中
export function addSessionMember(data) {
  // 实际上添加群成员应该通过群组API处理
  return Promise.resolve({ data: null });
}

// 移除会话成员 - 群聊相关，应该在群组API中
export function removeSessionMember(sessionId, memberIds) {
  // 实际上移除群成员应该通过群组API处理
  return Promise.resolve({ data: null });
}

// 获取会话未读消息数 - 使用clearUnread接口的逆向逻辑，实际上应该在会话VO中有未读数
export function getUnreadCount(sessionId) {
  return request({
    url: `/api/im/session/${sessionId}`,
    method: 'get',
  })
}

// 获取用户活跃会话数量 - 后端没有此API，从会话列表中获取
export function getActiveCount() {
  return request({
    url: '/api/im/session/list',
    method: 'get',
  }).then(response => {
    return { data: response.data ? response.data.length : 0 };
  });
}

// 清理过期会话 - 后端没有此API
export function cleanExpiredSessions(days) {
  // 这个功能可能由后端定时任务处理，前端不需要直接调用
  return Promise.resolve({ data: null });
}

// 更新会话活动时间 - 后端没有此API
export function updateActiveTime(sessionId) {
  // 通常在发送消息或查看会话时自动更新，不需要单独的API
  return Promise.resolve({ data: null });
}

// 获取会话消息列表 - 实际上应该调用消息模块的API
export function listSessionMessages(sessionId, params = {}) {
  return request({
    url: `/api/im/message/list/${sessionId}`,
    method: 'get',
    params: params,
  })
}

// 导出会话消息 - 后端没有此API
export function exportSessionMessages(sessionId) {
  // 这个功能可能需要后端实现，暂时返回空
  return Promise.resolve({ data: null });
}

// 获取会话统计信息 - 后端没有此API
export function getSessionStats(sessionId) {
  // 会话统计信息应该包含在会话详情中，通过getSession获取
  return request({
    url: `/api/im/session/${sessionId}`,
    method: 'get',
  });
}
