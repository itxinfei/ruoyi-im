import request from '@/utils/request'

// 登录方法
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data: data,
  })
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: '/system/user/getInfo',
    method: 'get',
  })
}

// 退出方法
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post',
  })
}

// 更新用户个人信息
export function updateUserProfile(data) {
  return request({
    url: '/system/user/profile',
    method: 'put',
    data: data,
  })
}

// 更新用户密码
export function updatePassword(oldPassword, newPassword) {
  const data = {
    oldPassword,
    newPassword,
  }
  return request({
    url: '/system/user/profile/updatePwd',
    method: 'put',
    params: data,
  })
}
