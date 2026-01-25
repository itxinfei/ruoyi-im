/**
 * 审批中心相关 API
 */
import request from '../request'

/**
 * 发起审批
 * @param {Object} data - 审批数据
 */
export function createApproval(params, data) {
    return request({
        url: '/api/im/approval/create',
        method: 'post',
        params, // templateId, title
        data
    })
}

/**
 * 获取审批详情
 * @param {number} id - 审批ID
 */
export function getApproval(id) {
    return request({
        url: `/api/im/approval/${id}`,
        method: 'get'
    })
}

/**
 * 获取待我审批列表
 */
export function getPendingApprovals() {
    return request({
        url: '/api/im/approval/pending',
        method: 'get'
    })
}

/**
 * 获取我发起的审批列表
 */
export function getMyApprovals() {
    return request({
        url: '/api/im/approval/my',
        method: 'get'
    })
}

/**
 * 获取我已审批列表
 */
export function getProcessedApprovals() {
    return request({
        url: '/api/im/approval/processed',
        method: 'get'
    })
}

/**
 * 通过审批
 * @param {number} id - 审批ID
 * @param {string} comment - 意见
 */
export function approve(id, comment) {
    return request({
        url: `/api/im/approval/${id}/approve`,
        method: 'post',
        params: { comment }
    })
}

/**
 * 驳回审批
 * @param {number} id - 审批ID
 * @param {string} comment - 意见
 */
export function reject(id, comment) {
    return request({
        url: `/api/im/approval/${id}/reject`,
        method: 'post',
        params: { comment }
    })
}

/**
 * 获取审批模板
 */
export function getTemplates() {
    return request({
        url: '/api/im/approval/templates/active',
        method: 'get'
    })
}

/**
 * 处理审批（通过或拒绝）
 * @param {Object} data - { approvalId, action, comment }
 * @param {string} data.approvalId - 审批ID
 * @param {string} data.action - 操作类型: APPROVE 或 REJECT
 * @param {string} data.comment - 审批意见
 */
export function handleApproval(data) {
    return request({
        url: `/api/im/approval/${data.approvalId}/${data.action.toLowerCase()}`,
        method: 'post',
        data: {
            comment: data.comment
        }
    })
}
