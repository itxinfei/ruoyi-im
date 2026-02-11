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
        url: '/api/im/apps/visible',
        method: 'get'
    })
}

/**
 * 获取我的已安装应用
 * @returns {Promise}
 */
export function getMyApplications() {
    return request({
        url: '/api/im/apps/installed',
        method: 'get'
    })
}

/**
 * 获取按分类分组的应用列表
 * @returns {Promise}
 */
export function getApplicationsByCategory() {
    return request({
        url: '/api/im/apps/categories',
        method: 'get'
    })
}

/**
 * 获取应用详情
 * @param {number} appId - 应用ID
 * @returns {Promise}
 */
export function getApplicationById(appId) {
    return request({
        url: `/api/im/apps/${appId}`,
        method: 'get'
    })
}

/**
 * 获取已安装应用详情
 * @param {number} appId - 应用ID
 * @returns {Promise}
 */
export function getInstalledApp(appId) {
    return request({
        url: `/api/im/apps/installed/${appId}`,
        method: 'get'
    })
}

/**
 * 安装应用
 * @param {Object} data - 安装请求数据
 * @param {number} data.appId - 应用ID
 * @param {Object} data.config - 应用配置
 * @param {boolean} data.pinned - 是否置顶
 * @param {number} data.sortOrder - 排序
 * @returns {Promise}
 */
export function installApplication(data) {
    return request({
        url: '/api/im/apps',
        method: 'post',
        data
    })
}

/**
 * 卸载应用
 * @param {number} appId - 应用ID
 * @returns {Promise}
 */
export function uninstallApplication(appId) {
    return request({
        url: `/api/im/apps/${appId}`,
        method: 'delete'
    })
}

/**
 * 更新应用配置
 * @param {number} appId - 应用ID
 * @param {Object} config - 配置数据
 * @returns {Promise}
 */
export function updateAppConfig(appId, config) {
    return request({
        url: `/api/im/apps/${appId}/config`,
        method: 'post',
        data: { config }
    })
}

/**
 * 获取应用配置
 * @param {number} appId - 应用ID
 * @returns {Promise}
 */
export function getAppConfig(appId) {
    return request({
        url: `/api/im/apps/${appId}/config`,
        method: 'get'
    })
}

/**
 * 置顶/取消置顶应用
 * @param {number} appId - 应用ID
 * @param {boolean} pinned - 是否置顶
 * @returns {Promise}
 */
export function pinApp(appId, pinned) {
    return request({
        url: `/api/im/apps/${appId}/pin`,
        method: 'post',
        data: { pinned }
    })
}

/**
 * 更新应用排序
 * @param {Array} sortList - 排序列表 [{appId, sortOrder}]
 * @returns {Promise}
 */
export function updateAppSort(sortList) {
    return request({
        url: '/api/im/apps/sort',
        method: 'post',
        data: { sortList }
    })
}

/**
 * 启用/禁用应用
 * @param {number} appId - 应用ID
 * @param {boolean} enabled - 是否启用
 * @returns {Promise}
 */
export function setAppEnabled(appId, enabled) {
    return request({
        url: `/api/im/apps/${appId}/enabled`,
        method: 'post',
        data: { enabled }
    })
}

/**
 * 记录应用使用
 * @param {number} appId - 应用ID
 * @returns {Promise}
 */
export function recordAppUsage(appId) {
    return request({
        url: `/api/im/apps/${appId}/usage`,
        method: 'post'
    })
}

/**
 * 获取应用统计信息
 * @param {number} appId - 应用ID
 * @returns {Promise}
 */
export function getAppStatistics(appId) {
    return request({
        url: `/api/im/apps/${appId}/statistics`,
        method: 'get'
    })
}
