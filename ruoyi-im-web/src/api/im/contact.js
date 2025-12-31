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
    url: '/api/im/contact/add',
    method: 'post',
    data: data,
  })
}

// 删除联系人
export function deleteContact(userId) {
  return request({
    url: '/api/im/contact/' + userId,
    method: 'delete',
  })
}

// 获取联系人详情
export function getContactInfo(userId) {
  return request({
    url: '/api/im/contact/' + userId,
    method: 'get',
  })
}

// 更新联系人备注
export function updateContactRemark(data) {
  return request({
    url: '/api/im/contact/remark',
    method: 'put',
    data: data,
  })
}

// 获取联系人在线状态
export function getContactStatus(userIds) {
  return request({
    url: '/api/im/contact/status',
    method: 'post',
    data: userIds,
  })
}

// 搜索联系人
export function searchContacts(keyword) {
  return request({
    url: '/api/im/contact/search',
    method: 'get',
    params: { keyword },
  })
}

// 获取好友申请列表
export function getFriendRequests() {
  return request({
    url: '/api/im/contact/requests',
    method: 'get',
  })
}

// 处理好友申请
export function handleFriendRequest(data) {
  return request({
    url: '/api/im/contact/request/handle',
    method: 'post',
    data: data,
  })
}

// 获取好友分组列表
export function getFriendGroups() {
  return request({
    url: '/api/im/contact/groups',
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

// 获取系统通知
export function getSystemNotifications(query) {
  return request({
    url: '/api/im/contact/notifications',
    method: 'get',
    params: query,
  })
}

// 标记通知已读
export function markNotificationRead(notificationIds) {
  return request({
    url: '/api/im/contact/notifications/read',
    method: 'post',
    data: notificationIds,
  })
}

// 删除通知
export function deleteNotification(notificationId) {
  return request({
    url: '/api/im/contact/notifications/' + notificationId,
    method: 'delete',
  })
}

// 获取未读通知数量
export function getUnreadNotificationCount() {
  return request({
    url: '/api/im/contact/notifications/unread/count',
    method: 'get',
  })
}
