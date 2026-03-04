/**
 * URL 元数据相关 API
 */
import request from '../request'

/**
 * 解析 URL 元数据
 * @param {string} url - URL 地址
 * @returns {Promise}
 */
export function parseUrlMetadata(url) {
  return request({
    url: '/api/im/url/parse',
    method: 'get',
    params: { url }
  })
}

/**
 * 刷新 URL 元数据
 * @param {string} url - URL 地址
 * @returns {Promise}
 */
export function refreshUrlMetadata(url) {
  return request({
    url: '/api/im/url/refresh',
    method: 'post',
    params: { url }
  })
}
