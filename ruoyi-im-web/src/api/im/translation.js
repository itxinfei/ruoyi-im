/**
 * 消息翻译相关API
 */
import request from '../request'

/**
 * 翻译文本
 * @param {Object} data 翻译请求
 * @param {string} data.text 待翻译的文本内容
 * @param {string} data.from 源语言（可选，auto表示自动检测）
 * @param {string} data.to 目标语言
 * @param {string} data.provider 翻译服务提供商（可选）
 */
export function translate(data) {
  return request({
    url: '/api/im/translations/translate',
    method: 'post',
    data
  })
}

/**
 * 获取支持的语言列表
 */
export function getSupportedLanguages() {
  return request({
    url: '/api/im/translations/languages',
    method: 'get'
  })
}

/**
 * 检测文本语言
 * @param {string} text 文本内容
 */
export function detectLanguage(text) {
  return request({
    url: '/api/im/translations/detect',
    method: 'get',
    params: { text }
  })
}

// 默认导出
export default {
  translate,
  getSupportedLanguages,
  detectLanguage
}
