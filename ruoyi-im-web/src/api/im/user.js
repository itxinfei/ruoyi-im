import request from '@/utils/request'

// 查询IM用户列表
export function listUser(query) {
  return request({
    url: '/api/im/user/list',
    method: 'get',
    params: query,
  })
}

// 查询IM用户详细
export function getUser(userId) {
  return request({
    url: '/api/im/user/' + userId,
    method: 'get',
  })
}

// 新增IM用户
export function addUser(data) {
  return request({
    url: '/api/im/user',
    method: 'post',
    data: data,
  })
}

// 修改IM用户
export function updateUser(data) {
  return request({
    url: '/api/im/user',
    method: 'put',
    data: data,
  })
}

// 删除IM用户
export function delUser(userId) {
  return request({
    url: '/api/im/user/' + userId,
    method: 'delete',
  })
}

// 修改用户状态
export function changeUserStatus(userId, status) {
  const data = {
    userId,
    status,
  }
  return request({
    url: '/api/im/user/changeStatus',
    method: 'put',
    data: data,
  })
}

// 查询用户好友列表
export function listUserFriends(userId) {
  return request({
    url: '/api/im/user/friends/' + userId,
    method: 'get',
  })
}

// 获取当前登录用户信息
export function getCurrentUserInfo() {
  return request({
    url: '/api/im/user/info',
    method: 'get',
  })
}

// 更新个人资料
export function updateProfile(userId, data) {
  return request({
    url: '/api/im/user/' + userId,
    method: 'put',
    data: data,
  })
}

// 修改密码
export function changePassword(userId, oldPassword, newPassword) {
  return request({
    url: '/api/im/user/' + userId + '/password',
    method: 'put',
    params: {
      oldPassword,
      newPassword,
    },
  })
}

// 上传用户头像
export function uploadAvatar(data) {
  return request({
    url: '/api/im/user/avatar',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}
