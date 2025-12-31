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
