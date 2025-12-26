import request from '@/utils/request'

// WebSocket 连接
let socket

// 建立 WebSocket 连接
export function connectWebSocket(userId) {
  socket = new WebSocket(`ws://your-websocket-server-url/${userId}`)

  socket.onopen = () => {
    // WebSocket 连接已建立
  }

  socket.onmessage = event => {
    const data = JSON.parse(event.data)
    // 处理接收到的消息
    // 这里可以触发 Vuex action 或者 emit 事件来更新 UI
  }

  socket.onclose = () => {
    // WebSocket 连接已关闭
  }
}

// 通过 WebSocket 发送消息
export function sendWebSocketMessage(message) {
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify(message))
  }
}

// 更新输入状态
export function updateTypingStatus(sessionId, isTyping) {
  return request({
    url: '/im/message/typing',
    method: 'post',
    data: {
      sessionId,
      isTyping,
    },
  })
}

// 发送消息
export function sendMessage(data) {
  return request({
    url: '/im/message/send',
    method: 'post',
    data: data,
  })
}

// 撤回消息
export function recallMessage(messageId) {
  return request({
    url: `/im/message/${messageId}/recall`,
    method: 'post',
  })
}

// 获取消息列表
export function getMessageList(query) {
  return request({
    url: '/im/message/list',
    method: 'get',
    params: query,
  })
}

// 获取消息详情
export function getMessageDetail(messageId) {
  return request({
    url: `/im/message/${messageId}`,
    method: 'get',
  })
}

// 标记消息已读
export function markMessageRead(messageIds) {
  return request({
    url: '/im/message/read',
    method: 'post',
    data: { messageIds },
  })
}

// 获取未读消息数
export function getUnreadCount() {
  return request({
    url: '/im/message/unread/count',
    method: 'get',
  })
}

// 获取消息接收状态
export function getMessageStatus(messageId) {
  return request({
    url: `/im/message/${messageId}/status`,
    method: 'get',
  })
}

// 转发消息
export function forwardMessage(data) {
  return request({
    url: '/im/message/forward',
    method: 'post',
    data: data,
  })
}

// 删除消息
export function deleteMessage(messageId) {
  return request({
    url: `/im/message/${messageId}`,
    method: 'delete',
  })
}

// 批量删除消息
export function batchDeleteMessages(messageIds) {
  return request({
    url: '/im/message/batch',
    method: 'delete',
    data: { messageIds },
  })
}

// 添加消息反应
export function addReaction(messageId, emoji) {
  return request({
    url: `/im/message/${messageId}/reaction`,
    method: 'post',
    data: { emoji },
  })
}

// 移除消息反应
export function removeReaction(messageId, emoji) {
  return request({
    url: `/im/message/${messageId}/reaction`,
    method: 'delete',
    data: { emoji },
  })
}

// 获取消息草稿
export function getMessageDraft(sessionId) {
  return request({
    url: `/im/message/draft/${sessionId}`,
    method: 'get',
  })
}

// 保存消息草稿
export function saveMessageDraft(sessionId, content) {
  return request({
    url: `/im/message/draft/${sessionId}`,
    method: 'post',
    data: { content },
  })
}

// 获取会话的未读消息数
export function getUnreadCountBySession(sessionId) {
  return request({
    url: `/im/message/unread/${sessionId}`,
    method: 'get',
  })
}

// 标记会话所有消息为已读
export function markSessionAsRead(sessionId) {
  return request({
    url: `/im/message/read/${sessionId}`,
    method: 'post',
  })
}
