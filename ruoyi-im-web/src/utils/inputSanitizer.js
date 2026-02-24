/**
 * 输入转义工具函数
 * 用于防止 XSS 攻击和其他安全漏洞
 */

/**
 * HTML 特殊字符转义
 * @param {string} text - 原始文本
 * @returns {string} 转义后的文本
 */
export function escapeHtml(text) {
  if (typeof text !== 'string') {
    return ''
  }
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

/**
 * HTML 属性转义
 * @param {string} text - 原始文本
 * @returns {string} 转义后的文本
 */
export function escapeHtmlAttribute(text) {
  if (typeof text !== 'string') {
    return ''
  }
  return text
    .replace(/&/g, '&amp;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}

/**
 * JavaScript 字符串转义
 * @param {string} text - 原始文本
 * @returns {string} 转义后的文本
 */
export function escapeJavaScript(text) {
  if (typeof text !== 'string') {
    return ''
  }
  return text
    .replace(/\\/g, '\\\\')
    .replace(/'/g, "\\'")
    .replace(/"/g, '\\"')
    .replace(/\n/g, '\\n')
    .replace(/\r/g, '\\r')
    .replace(/\t/g, '\\t')
}

/**
 * URL 参数转义
 * @param {string} text - 原始文本
 * @returns {string} 转义后的文本
 */
export function escapeUrl(text) {
  if (typeof text !== 'string') {
    return ''
  }
  return encodeURIComponent(text)
}

/**
 * CSS 转义
 * @param {string} text - 原始文本
 * @returns {string} 转义后的文本
 */
export function escapeCss(text) {
  if (typeof text !== 'string') {
    return ''
  }
  return text.replace(/[^a-zA-Z0-9-_]/g, '\\$&')
}

/**
 * SQL 通配符转义（用于 LIKE 查询）
 * @param {string} text - 原始文本
 * @returns {string} 转义后的文本
 */
export function escapeSqlLike(text) {
  if (typeof text !== 'string') {
    return ''
  }
  return text
    .replace(/\\/g, '\\\\')
    .replace(/%/g, '\\%')
    .replace(/_/g, '\\_')
}

/**
 * 清理用户输入（用于搜索框等）
 * 移除潜在的危险字符
 * @param {string} text - 原始文本
 * @returns {string} 清理后的文本
 */
export function sanitizeInput(text) {
  if (typeof text !== 'string') {
    return ''
  }
  return text
    .replace(/[<>]/g, '') // 移除 < 和 >
    .replace(/javascript:/gi, '') // 移除 javascript: 协议
    .replace(/on\w+=/gi, '') // 移除事件处理器
    .trim()
}

/**
 * 验证并清理搜索关键词
 * @param {string} keyword - 搜索关键词
 * @returns {{valid: boolean, keyword?: string, message?: string}} 验证结果
 */
export function validateSearchKeyword(keyword) {
  if (!keyword || typeof keyword !== 'string') {
    return { valid: false, message: '搜索关键词不能为空' }
  }

  const trimmed = keyword.trim()

  if (trimmed.length === 0) {
    return { valid: false, message: '搜索关键词不能为空' }
  }

  if (trimmed.length > 100) {
    return { valid: false, message: '搜索关键词长度不能超过 100 个字符' }
  }

  // 检查是否包含危险字符
  const dangerousPattern = /[<>]'"\\/
  if (dangerousPattern.test(trimmed)) {
    // 自动清理
    const cleaned = sanitizeInput(trimmed)
    return { valid: true, keyword: cleaned }
  }

  return { valid: true, keyword: trimmed }
}

/**
 * 深度清理对象中的所有字符串属性
 * @param {Object} obj - 需要清理的对象
 * @returns {Object} 清理后的对象
 */
export function deepSanitize(obj) {
  if (typeof obj !== 'object' || obj === null) {
    return obj
  }

  if (Array.isArray(obj)) {
    return obj.map(item => deepSanitize(item))
  }

  const result = {}
  for (const [key, value] of Object.entries(obj)) {
    if (typeof value === 'string') {
      result[key] = sanitizeInput(value)
    } else if (typeof value === 'object' && value !== null) {
      result[key] = deepSanitize(value)
    } else {
      result[key] = value
    }
  }
  return result
}

/**
 * 创建安全的 innerHTML
 * 只允许特定的安全标签
 * @param {string} html - HTML 字符串
 * @param {string[]} allowedTags - 允许的标签列表
 * @returns {string} 安全的 HTML
 */
export function createSafeHtml(html, allowedTags = ['p', 'br', 'strong', 'em', 'u', 'span']) {
  if (typeof html !== 'string') {
    return ''
  }

  // 移除所有 HTML 标签，除了允许的
  const tagPattern = new RegExp(`<\\/?(?!${allowedTags.join('|')})[^>]+>`, 'gi')
  return html.replace(tagPattern, '')
}

/**
 * Vue 自定义指令：v-safe-html
 * 用于安全地渲染 HTML 内容
 */
export const safeHtmlDirective = {
  mounted(el, binding) {
    el.innerHTML = createSafeHtml(binding.value)
  },
  updated(el, binding) {
    el.innerHTML = createSafeHtml(binding.value)
  }
}
