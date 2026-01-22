/**
 * 组织架构相关 API
 */
import request from '../request'

/**
 * 获取组织架构树
 * @returns {Promise}
 */
export function getOrgTree() {
    return request({
        url: '/api/im/organization/tree',
        method: 'get'
    })
}

/**
 * 获取部门详情
 * @param {number} deptId - 部门ID
 * @returns {Promise}
 */
export function getDepartment(deptId) {
    return request({
        url: `/api/im/organization/dept/${deptId}`,
        method: 'get'
    })
}

/**
 * 获取部门成员
 * @param {number} deptId - 部门ID
 * @returns {Promise}
 */
export function getDepartmentMembers(deptId) {
    return request({
        url: `/api/im/organization/dept/${deptId}/members`,
        method: 'get'
    })
}

/**
 * 搜索组织成员
 * @param {Object} params - 查询参数
 * @param {string} params.keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchOrgMembers(params) {
    return request({
        url: '/api/im/organization/search',
        method: 'get',
        params
    })
}
