import request from '@/utils/request'

// 查询群组列表
export function listGroup(query) {
  return request({
    url: '/api/im/group/list',
    method: 'get',
    params: query,
  })
}

// 查询群组详细
export function getGroup(groupId) {
  return request({
    url: '/api/im/group/' + groupId,
    method: 'get',
  })
}

// 新增群组
export function addGroup(data) {
  return request({
    url: '/api/im/group',
    method: 'post',
    data: data,
  })
}

// 修改群组
export function updateGroup(data) {
  return request({
    url: '/api/im/group',
    method: 'put',
    data: data,
  })
}

// 删除群组
export function delGroup(groupId) {
  return request({
    url: '/api/im/group/' + groupId,
    method: 'delete',
  })
}

// 修改群组设置
export function updateGroupSettings(data) {
  return request({
    url: '/api/im/group/settings',
    method: 'put',
    data: data,
  })
}

// 获取群组设置
export function getGroupSettings(groupId) {
  return request({
    url: '/api/im/group/settings/' + groupId,
    method: 'get',
  })
}

// 更新群组公告
export function updateGroupAnnouncement(data) {
  return request({
    url: '/api/im/group/announcement',
    method: 'put',
    data: data,
  })
}

// 获取群组公告
export function getGroupAnnouncement(groupId) {
  return request({
    url: '/api/im/group/announcement/' + groupId,
    method: 'get',
  })
}
