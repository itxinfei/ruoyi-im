import request from '@/utils/request'

// 获取群组成员列表
export function listGroupMember(groupId, params) {
  return request({
    url: `/api/im/groups/${groupId}/members`,
    method: 'get',
    params,
  })
}

// 添加群组成员
export function addGroupMember(groupId, data) {
  return request({
    url: `/api/im/groups/${groupId}/members`,
    method: 'post',
    data,
  })
}

// 删除群组成员
export function delGroupMember(groupId, memberId) {
  return request({
    url: `/api/im/groups/${groupId}/members/${memberId}`,
    method: 'delete',
  })
}

// 更新成员角色
export function updateMemberRole(groupId, memberId, data) {
  return request({
    url: `/api/im/groups/${groupId}/members/${memberId}/role`,
    method: 'put',
    data,
  })
}

export default {
  listGroupMember,
  addGroupMember,
  delGroupMember,
  updateMemberRole,
}
