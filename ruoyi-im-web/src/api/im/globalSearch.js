/**
 * 全局搜索 API
 * 提供跨模块的统一搜索功能
 */
import request from '../request'

/**
 * 全局搜索
 * 根据关键词搜索消息、联系人、群组、文件、工作台内容
 * @param {Object} data - 搜索请求参数
 * @param {string} data.keyword - 搜索关键词
 * @param {string} data.searchType - 搜索类型：ALL/MESSAGE/CONTACT/GROUP/FILE/WORKBENCH
 * @param {number} data.pageNum - 页码
 * @param {number} data.pageSize - 每页数量
 * @returns {Promise}
 */
export function globalSearch(data) {
  return request({
    url: '/api/im/search/global',
    method: 'post',
    data
  })
}

/**
 * 搜索消息（全局搜索）
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function globalSearchMessages(keyword) {
  return request({
    url: '/api/im/search/messages',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 搜索联系人（全局搜索）
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function globalSearchContacts(keyword) {
  return request({
    url: '/api/im/search/contacts',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 搜索群组（全局搜索）
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function globalSearchGroups(keyword) {
  return request({
    url: '/api/im/search/groups',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 搜索文件（全局搜索）
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function globalSearchFiles(keyword) {
  return request({
    url: '/api/im/search/files',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 搜索工作台内容
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchWorkbench(keyword) {
  return request({
    url: '/api/im/search/workbench',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 获取热门搜索关键词
 * 返回当前用户最近搜索的关键词
 * @returns {Promise}
 */
export function getHotKeywords() {
  return request({
    url: '/api/im/search/hot-keywords',
    method: 'get'
  })
}
