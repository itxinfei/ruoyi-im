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
        url: '/api/im/organizations/department/tree',
        method: 'get'
    })
}

/**
 * 获取部门详情
 */
export function getDepartment(deptId) {
    return request({
        url: `/api/im/organizations/department/${deptId}`,
        method: 'get'
    })
}

/**
 * 获取部门成员列表
 */
export function getDepartmentMembers(deptId) {
    return request({
        url: `/api/im/organizations/department/${deptId}/members`,
        method: 'get'
    })
}

/**
 * 获取部门在线人数统计
 * 注意：后端暂未实现该接口，前端暂时降级处理
 */
export function getDepartmentOnlineCount(deptId) {
    // 暂时返回 mock 数据或抛出错误，等待后端实现
    return Promise.resolve({ code: 200, data: 0 })
    /*
    return request({
        url: `/api/im/organizations/department/${deptId}/online-count`,
        method: 'get'
    })
    */
}

/**
 * 搜索组织架构成员
 * @param {Object} params - 搜索参数
 * @param {string} params.keyword - 关键词
 * @returns {Promise}
 */
export function searchOrgMembers(params) {
    return request({
        url: '/api/im/search/contacts',
        method: 'get',
        params
    })
}

/**
 * 搜索部门成员
 * @param {number} deptId - 部门ID
 * @param {Object} params - 搜索参数
 * @param {string} params.keyword - 关键词
 * @returns {Promise}
 */
export function searchDepartmentMembers(deptId, params) {
    // 后端目前没有按部门搜索成员的专用接口，暂使用全局搜索
    return request({
        url: '/api/im/search/contacts',
        method: 'get',
        params
    })
}

/**
 * 获取用户关系链 (同事、上下级等)
 * 注意：后端暂未实现该接口
 */
export function getUserRelations(userId) {
    return Promise.resolve({ code: 200, data: {} })
    /*
    return request({
        url: `/api/im/organizations/users/${userId}/relations`,
        method: 'get'
    })
    */
}

/**
 * 添加部门成员 (管理员功能)
 */
export function addDepartmentMember(data) {
    return request({
        url: '/api/im/organizations/department/member',
        method: 'post',
        data
    })
}

/**
 * 移除部门成员 (管理员功能)
 */
export function removeDepartmentMember(deptId, userId) {
    return request({
        url: `/api/im/organizations/department/${deptId}/member/${userId}`,
        method: 'delete'
    })
}
