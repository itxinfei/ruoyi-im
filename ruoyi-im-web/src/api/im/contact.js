import request from '@/utils/request'

// 获取联系人列表
export function listContact(query) {
  return request({
    url: '/api/im/contact/list',
    method: 'get',
    params: query,
  })
}

// 添加联系人（发送好友申请）
export function addContact(data) {
  return request({
    url: '/api/im/contact/request/send',
    method: 'post',
    data: data,
  })
}

// 删除联系人 - 需要提供好友关系ID而不是用户ID
export function deleteContact(friendId) {
  return request({
    url: '/api/im/contact/' + friendId,
    method: 'delete',
  })
}

// 获取联系人详情 - 需要提供好友关系ID而不是用户ID
export function getContactInfo(friendId) {
  return request({
    url: '/api/im/contact/' + friendId,
    method: 'get',
  })
}

// 更新联系人备注 - 需要使用好友关系ID作为路径参数
export function updateContactRemark(friendId, data) {
  return request({
    url: `/api/im/contact/${friendId}`,
    method: 'put',
    data: data,
  })
}

// 获取联系人在线状态 - 后端没有此API，需要通过WebSocket获取或从用户信息中获取
export function getContactStatus(userIds) {
  // 现在在线状态主要通过WebSocket维护和获取
  return Promise.resolve({ data: [] });
}

// 搜索联系人
export function searchContacts(keyword) {
  return request({
    url: '/api/im/contact/search',
    method: 'get',
    params: { keyword },
  })
}

// 获取收到的好友申请列表
export function getFriendRequests() {
  return request({
    url: '/api/im/contact/request/received',
    method: 'get',
  })
}

// 获取发送的好友申请列表
export function getSentFriendRequests() {
  return request({
    url: '/api/im/contact/request/sent',
    method: 'get',
  })
}

// 处理好友申请
export function handleFriendRequest(requestId, approved) {
  return request({
    url: `/api/im/contact/request/${requestId}/handle`,
    method: 'post',
    params: {
      approved: approved
    }
  })
}

// 获取好友分组列表 - 后端使用分组好友列表API
export function getFriendGroups() {
  return request({
    url: '/api/im/contact/grouped',
    method: 'get',
  })
}

// 创建好友分组
export function createFriendGroup(data) {
  return request({
    url: '/api/im/contact/group',
    method: 'post',
    data: data,
  })
}

// 更新好友分组
export function updateFriendGroup(groupId, data) {
  return request({
    url: '/api/im/contact/group/' + groupId,
    method: 'put',
    data: data,
  })
}

// 删除好友分组
export function deleteFriendGroup(groupId) {
  return request({
    url: '/api/im/contact/group/' + groupId,
    method: 'delete',
  })
}

// 移动好友到分组
export function moveFriendToGroup(data) {
  return request({
    url: '/api/im/contact/group/move',
    method: 'put',
    data: data,
  })
}

// 获取系统通知 - 后端没有此API，需要使用通知API
export function getSystemNotifications(query) {
  // 系统通知应该通过通知API获取
  return request({
    url: '/api/im/notification/list',
    method: 'get',
    params: query,
  })
}

// 标记通知已读 - 后端没有此API，需要使用通知API
export function markNotificationRead(notificationIds) {
  // 标记通知已读应该通过通知API处理
  if (Array.isArray(notificationIds) && notificationIds.length > 0) {
    // 假设使用第一个ID作为示例
    return request({
      url: `/api/im/notification/${notificationIds[0]}/read`,
      method: 'put',
    })
  } else {
    return request({
      url: `/api/im/notification/${notificationIds}/read`,
      method: 'put',
    })
  }
}

// 删除通知 - 后端没有此API，需要使用通知API
export function deleteNotification(notificationId) {
  return request({
    url: `/api/im/notification/${notificationId}`,
    method: 'delete',
  })
}

// 获取未读通知数量 - 后端没有此API，需要使用通知API
export function getUnreadNotificationCount() {
  return request({
    url: '/api/im/notification/unread-count',
    method: 'get',
  })
}
