import request from '@/utils/request'

// 更新通知设置
export function updateNotificationSettings(data) {
  return request({
    url: '/im/config/notification',
    method: 'put',
    data: data,
  })
}

// 更新隐私设置
export function updatePrivacySettings(data) {
  return request({
    url: '/im/config/privacy',
    method: 'put',
    data: data,
  })
}

// 获取黑名单用户列表
export function getBlockedUsers() {
  return request({
    url: '/im/config/blocked',
    method: 'get',
  })
}

// 拉黑用户
export function blockUser(userId) {
  return request({
    url: '/im/config/block/' + userId,
    method: 'post',
  })
}

// 取消拉黑用户
export function unblockUser(userId) {
  return request({
    url: '/im/config/unblock/' + userId,
    method: 'delete',
  })
}

// 获取用户隐私设置
export function getPrivacySettings() {
  return request({
    url: '/im/config/privacy',
    method: 'get',
  })
}
