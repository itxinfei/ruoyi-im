import request from '../request'

/**
 * 全局搜索
 * @param {Object} data - 搜索参数
 * @param {string} data.keyword - 关键词
 * @param {string} data.searchType - 搜索类型 (all/message/contact/group/file/workbench)
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
 * 搜索消息
 * @param {string} keyword - 关键词
 * @returns {Promise}
 */
export function searchMessages(keyword) {
  return request({
    url: '/api/im/search/messages',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 搜索联系人
 * @param {string} keyword - 关键词
 * @returns {Promise}
 */
export function searchContacts(keyword) {
  return request({
    url: '/api/im/search/contacts',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 搜索群组
 * @param {string} keyword - 关键词
 * @returns {Promise}
 */
export function searchGroups(keyword) {
  return request({
    url: '/api/im/search/groups',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 搜索文件
 * @param {string} keyword - 关键词
 * @returns {Promise}
 */
export function searchFiles(keyword) {
  return request({
    url: '/api/im/search/files',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 搜索工作台
 * @param {string} keyword - 关键词
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
 * 获取热门搜索
 * @returns {Promise}
 */
export function getHotKeywords() {
  return request({
    url: '/api/im/search/hot-keywords',
    method: 'get'
  })
}
