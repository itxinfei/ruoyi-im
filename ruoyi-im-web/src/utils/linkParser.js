/**
 * 链接解析工具
 * 用于检测和解析消息中的链接
 */

import { parseLinkPreview } from '@/api/im/message'

/**
 * URL检测正则
 * 匹配 http:// 或 https:// 开头的链接
 * 排除尾部标点符号 .,!?;:
 */
const URL_PATTERN = /https?:\/\/[^\s<>"{}|\\^`[\]]+[^\s<>"{}|\\^`[\].,!?;:]/g

/**
 * 内网地址检测正则
 */
const INTERNAL_URL_PATTERNS = [
  /^https?:\/\/localhost/i,
  /^https?:\/\/127\.\d+\.\d+\.\d+/i,
  /^https?:\/\/192\.168\./i,
  /^https?:\/\/10\./i,
  /^https?:\/\/172\.(1[6-9]|2\d|3[01])\./i,
  /^https?:\/\/169\.254\./i,
  /^https?:\/\/100\.100\.100\./i
]

/**
 * 最大并发解析数量
 */
const MAX_CONCURRENT = 3

/**
 * 从文本中提取所有URL
 * @param {string} text - 文本内容
 * @param {number} maxCount - 最大返回数量，默认3
 * @returns {string[]} URL数组
 */
export function extractUrls(text, maxCount = 3) {
  if (!text || typeof text !== 'string') {
    return []
  }

  const matches = text.match(URL_PATTERN)
  if (!matches) {
    return []
  }

  // 去重并限制数量
  const uniqueUrls = [...new Set(matches)]
  return uniqueUrls.slice(0, maxCount)
}

/**
 * 判断是否为内网地址
 * @param {string} url - 链接地址
 * @returns {boolean} true-内网地址，false-外网地址
 */
export function isInternalUrl(url) {
  if (!url || typeof url !== 'string') {
    return false
  }

  return INTERNAL_URL_PATTERNS.some(pattern => pattern.test(url))
}

/**
 * 解析单个链接
 * @param {string} url - 链接地址
 * @returns {Promise<Object|null>} 解析结果或null
 */
export async function parseLink(url) {
  if (!url || isInternalUrl(url)) {
    return null
  }

  try {
    const response = await parseLinkPreview(url)
    if (response.code === 200 && response.data) {
      return response.data
    }
    return null
  } catch (error) {
    console.warn('链接解析失败:', url, error.message)
    return null
  }
}

/**
 * 批量解析链接（限制并发）
 * @param {string[]} urls - URL数组
 * @returns {Promise<Object[]>} 解析结果数组
 */
export async function parseLinksInBatch(urls) {
  if (!urls || urls.length === 0) {
    return []
  }

  // 限制并发数量
  const results = []
  for (let i = 0; i < urls.length; i += MAX_CONCURRENT) {
    const batch = urls.slice(i, i + MAX_CONCURRENT)
    const batchResults = await Promise.all(
      batch.map(url => parseLink(url).catch(() => null))
    )
    results.push(...batchResults)
  }

  // 过滤掉null结果
  return results.filter(result => result !== null)
}

/**
 * 获取链接中的域名
 * @param {string} url - 链接地址
 * @returns {string} 域名
 */
export function extractDomain(url) {
  if (!url) {
    return ''
  }

  try {
    const urlObj = new URL(url)
    return urlObj.hostname
  } catch {
    return ''
  }
}

/**
 * 格式化显示URL
 * @param {string} url - 链接地址
 * @param {number} maxLength - 最大长度，默认50
 * @returns {string} 格式化后的URL
 */
export function formatUrl(url, maxLength = 50) {
  if (!url || url.length <= maxLength) {
    return url
  }

  return url.substring(0, maxLength - 3) + '...'
}

/**
 * 检测文本中是否包含链接
 * @param {string} text - 文本内容
 * @returns {boolean} true-包含链接，false-不包含
 */
export function hasLink(text) {
  if (!text || typeof text !== 'string') {
    return false
  }

  return URL_PATTERN.test(text)
}

export default {
  extractUrls,
  isInternalUrl,
  parseLink,
  parseLinksInBatch,
  extractDomain,
  formatUrl,
  hasLink
}
