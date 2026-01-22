/**
 * 应用中心相关 API
 */
import request from '../request'

/**
 * 获取可见应用列表
 * @returns {Promise}
 */
export function getVisibleApplications() {
    return request({
        url: '/api/im/app/visible',
        method: 'get'
    })
}

/**
 * 获取按分类分组的应用列表
 * @returns {Promise}
 */
export function getApplicationsByCategory() {
    return request({
        url: '/api/im/app/category',
        method: 'get'
    })
}

/**
 * 获取应用详情
 * @param {number} id - 应用ID
 * @returns {Promise}
 */
export function getApplicationById(id) {
    return request({
        url: `/api/im/app/${id}`,
        method: 'get'
    })
}
