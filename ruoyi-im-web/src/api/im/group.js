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
    url: '/api/im/group/create',
    method: 'post',
    data: data,
  })
}

// 修改群组
export function updateGroup(groupId, data) {
  return request({
    url: `/api/im/group/${groupId}`,
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

// 修改群组设置 - 群组设置通过更新群组信息实现
export function updateGroupSettings(groupId, data) {
  return request({
    url: `/api/im/group/${groupId}`,
    method: 'put',
    data: data,
  })
}

// 获取群组设置 - 群组设置包含在群组详情中
export function getGroupSettings(groupId) {
  return request({
    url: `/api/im/group/${groupId}`,
    method: 'get',
  })
}

// 更新群组公告 - 通过更新群组信息实现
export function updateGroupAnnouncement(groupId, announcement) {
  return request({
    url: `/api/im/group/${groupId}`,
    method: 'put',
    data: {
      announcement: announcement,
    },
  })
}

// 获取群组公告 - 包含在群组详情中
export function getGroupAnnouncement(groupId) {
  return request({
    url: `/api/im/group/${groupId}`,
    method: 'get',
  }).then(response => {
    return { data: response.data?.announcement || '' }
  })
}
