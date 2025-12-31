import request from '@/utils/request'

// 获取通知列表
export function listNotifications(query) {
  return request({
    url: '/api/im/notification/list',
    method: 'get',
    params: query,
  })
}

// 获取通知详情
export function getNotification(notificationId) {
  return request({
    url: '/api/im/notification/' + notificationId,
    method: 'get',
  })
}

// 标记通知已读
export function markAsRead(notificationIds) {
  return request({
    url: '/api/im/notification/read',
    method: 'put',
    data: notificationIds,
  })
}

// 标记所有通知已读
export function markAllAsRead() {
  return request({
    url: '/api/im/notification/read/all',
    method: 'put',
  })
}

// 删除通知
export function deleteNotification(notificationId) {
  return request({
    url: '/api/im/notification/' + notificationId,
    method: 'delete',
  })
}

// 批量删除通知
export function batchDeleteNotifications(notificationIds) {
  return request({
    url: '/api/im/notification/batch',
    method: 'delete',
    data: notificationIds,
  })
}

// 获取未读通知数量
export function getUnreadCount() {
  return request({
    url: '/api/im/notification/unread/count',
    method: 'get',
  })
}

// 获取通知设置
export function getNotificationSettings() {
  return request({
    url: '/api/im/notification/settings',
    method: 'get',
  })
}

// 更新通知设置
export function updateNotificationSettings(data) {
  return request({
    url: '/api/im/notification/settings',
    method: 'put',
    data: data,
  })
}

// 清空所有通知
export function clearAllNotifications() {
  return request({
    url: '/api/im/notification/clear',
    method: 'delete',
  })
}
