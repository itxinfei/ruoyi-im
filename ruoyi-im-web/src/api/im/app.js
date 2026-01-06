import request from '@/utils/request'

// 获取应用列表
export function getAppList(category) {
  return request({
    url: '/api/im/app/list',
    method: 'get',
    params: { category },
  })
}

// 获取可见应用列表
export function getVisibleApps() {
  return request({
    url: '/api/im/app/visible',
    method: 'get',
  })
}

// 按分类获取应用
export function getAppsByCategory() {
  return request({
    url: '/api/im/app/category',
    method: 'get',
  })
}

// 获取应用详情
export function getAppDetail(id) {
  return request({
    url: `/api/im/app/${id}`,
    method: 'get',
  })
}

// 创建应用（管理员）
export function createApp(data) {
  return request({
    url: '/api/im/app/create',
    method: 'post',
    params: data,
  })
}

// 更新应用（管理员）
export function updateApp(id, data) {
  return request({
    url: `/api/im/app/${id}`,
    method: 'put',
    params: data,
  })
}

// 删除应用（管理员）
export function deleteApp(id) {
  return request({
    url: `/api/im/app/${id}`,
    method: 'delete',
  })
}

// 设置应用可见性（管理员）
export function setAppVisibility(id, isVisible) {
  return request({
    url: `/api/im/app/${id}/visibility`,
    method: 'put',
    params: { isVisible },
  })
}
