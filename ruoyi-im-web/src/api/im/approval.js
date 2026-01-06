import request from '@/utils/request'

// 发起审批
export function createApproval(data) {
  return request({
    url: '/api/im/approval/create',
    method: 'post',
    params: data,
  })
}

// 获取审批详情
export function getApprovalDetail(id) {
  return request({
    url: `/api/im/approval/${id}`,
    method: 'get',
  })
}

// 获取待我审批列表
export function getPendingApprovals() {
  return request({
    url: '/api/im/approval/pending',
    method: 'get',
  })
}

// 获取我发起的审批列表
export function getMyApprovals() {
  return request({
    url: '/api/im/approval/my',
    method: 'get',
  })
}

// 获取我已审批列表
export function getProcessedApprovals() {
  return request({
    url: '/api/im/approval/processed',
    method: 'get',
  })
}

// 通过审批
export function approveApproval(id, comment) {
  return request({
    url: `/api/im/approval/${id}/approve`,
    method: 'post',
    params: { comment },
  })
}

// 驳回审批
export function rejectApproval(id, comment) {
  return request({
    url: `/api/im/approval/${id}/reject`,
    method: 'post',
    params: { comment },
  })
}

// 撤回审批
export function cancelApproval(id) {
  return request({
    url: `/api/im/approval/${id}/cancel`,
    method: 'post',
  })
}

// 获取审批模板列表
export function getApprovalTemplates() {
  return request({
    url: '/api/im/approval/templates',
    method: 'get',
  })
}

// 获取启用的审批模板列表
export function getActiveTemplates() {
  return request({
    url: '/api/im/approval/templates/active',
    method: 'get',
  })
}
