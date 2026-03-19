/**
 * HTML Sanitizer - 安全的 HTML 过滤工具
 *
 * 用于过滤用户输入的 HTML 内容，防止 XSS 攻击
 * 支持高亮显示功能，同时确保安全性
 */

import DOMPurify from 'dompurify'

// 安全的 HTML 标签和属性配置
const SANITIZE_CONFIG = {
  // 允许的标签（用于高亮显示）
  ALLOWED_TAGS: ['span', 'mark', 'br', 'b', 'strong', 'i', 'em', 'u', 'code', 'pre', 'p', 'div'],

  // 允许的属性（仅用于样式和高亮）
  ALLOWED_ATTR: ['class'],

  // 允许的样式
  ALLOWED_STYLE: [],

  // 强制移除所有 JavaScript 事件处理器
  FORCE_BODY: false,

  // 移除 HTML 注释
  REMOVE_COMMENTS: true,

  // 移除所有脚本
  REMOVE_SCRIPT: true,

  // 移除所有样式标签（除非在允许范围内）
  REMOVE_STYLES: true,

  // 移除所有 iframe
  REMOVE_IFRAME: true,

  // 移除所有对象和 embed
  REMOVE_OBJECT: true,

  // 移除所有 form 元素
  REMOVE_FORM: true,

  // 移除所有 input 和 button
  REMOVE_INPUT: true,

  // 移除所有链接的 onclick 等事件
  REMOVE_ONCLICK: true,

  // 移除所有带有 JavaScript 的链接
  REMOVE_JS_LINKS: true
}

/**
 * 安全地过滤 HTML 内容
 * @param {string} html - 需要过滤的 HTML 字符串
 * @param {Object} config - 自定义配置（可选）
 * @returns {string} - 过滤后的安全 HTML
 */
export function sanitizeHTML(html, config = {}) {
  if (!html || typeof html !== 'string') {
    return ''
  }

  try {
    // 合并配置
    const finalConfig = { ...SANITIZE_CONFIG, ...config }

    // 使用 DOMPurify 进行过滤
    return DOMPurify.sanitize(html, finalConfig)
  } catch (error) {
    console.error('HTML Sanitization failed:', error)
    // 如果 DOMPurify 失败，使用备用过滤方法
    return fallbackSanitize(html)
  }
}

/**
 * 备用的 HTML 过滤方法（当 DOMPurify 不可用时）
 * @param {string} html - 需要过滤的 HTML 字符串
 * @returns {string} - 过滤后的安全 HTML
 */
function fallbackSanitize(html) {
  if (!html) return ''

  // 移除危险的标签和属性
  const sanitized = html
    // 移除 script 标签及其内容
    .replace(/<script\b[^>]*>([\s\S]*?)<\/script>/gi, '')
    // 移除 style 标签及其内容
    .replace(/<style\b[^>]*>([\s\S]*?)<\/style>/gi, '')
    // 移除 iframe 标签
    .replace(/<iframe\b[^>]*>.*?<\/iframe>/gi, '')
    // 移除 object 和 embed 标签
    .replace(/<(object|embed)\b[^>]*>.*?<\/\1>/gi, '')
    // 移除 form 标签
    .replace(/<form\b[^>]*>.*?<\/form>/gi, '')
    // 移除 input 和 button 标签
    .replace(/<(input|button|textarea|select)\b[^>]*>/gi, '')
    // 移除所有事件处理器（onclick, onerror 等）
    .replace(/\s*on\w+\s*=\s*["'][^"']*["']/gi, '')
    // 移除 javascript: 协议
    .replace(/javascript:/gi, '')
    // 移除 data: 协议（可能包含恶意代码）
    .replace(/data:[^;]+;base64/gi, '')
    // 移除 vbscript: 协议
    .replace(/vbscript:/gi, '')
    // 移除所有带有 src 属性的标签（除非是允许的）
    .replace(/<img\b[^>]*>/gi, '')
    .replace(/<video\b[^>]*>.*?<\/video>/gi, '')
    .replace(/<audio\b[^>]*>.*?<\/audio>/gi, '')
    .replace(/<source\b[^>]*>/gi, '')
    .replace(/<track\b[^>]*>/gi, '')
    // 移除 HTML 注释
    .replace(/<!--[\s\S]*?-->/g, '')
    // 移除危险的 HTML 实体编码
    .replace(/&#x[\da-fA-F]+;?/g, '')
    .replace(/&#\d+;?/g, '')

  return sanitized
}

/**
 * 高亮文本中的关键词（安全版本）
 * @param {string} text - 原始文本
 * @param {string} keyword - 要高亮的关键词
 * @param {string} tag - 高亮标签（默认为 mark）
 * @param {string} className - 高亮类名（默认为 highlight）
 * @returns {string} - 带高亮的 HTML 字符串
 */
export function highlightText(text, keyword, tag = 'mark', className = 'highlight') {
  if (!text || !keyword) {
    return escapeHTML(text)
  }

  try {
    // 先转义原始文本中的 HTML
    const escapedText = escapeHTML(text)

    // 创建安全的高亮 HTML
    const escapedKeyword = escapeHTML(keyword)
    const regex = new RegExp(`(${escapedKeyword})`, 'gi')

    // 使用 span 标签进行高亮
    const highlighted = escapedText.replace(regex, `<${tag} class="${className}">$1</${tag}>`)

    // 再次过滤确保安全
    return sanitizeHTML(highlighted)
  } catch (error) {
    console.error('Highlight text failed:', error)
    return escapeHTML(text)
  }
}

/**
 * 转义 HTML 特殊字符
 * @param {string} text - 需要转义的文本
 * @returns {string} - 转义后的文本
 */
export function escapeHTML(text) {
  if (!text) return ''

  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

/**
 * 格式化 AI 消息（安全版本）
 * 支持 Markdown 基本语法的安全渲染
 * @param {string} content - 消息内容
 * @returns {string} - 格式化后的安全 HTML
 */
export function formatAIMessage(content) {
  if (!content) return ''

  try {
    // 先转义 HTML
    let formatted = escapeHTML(content)

    // 安全地处理换行
    formatted = formatted.replace(/\n/g, '<br>')

    // 安全地处理代码块
    formatted = formatted.replace(/```(\w*)\n([\s\S]*?)```/g, (match, lang, code) => {
      const escapedCode = escapeHTML(code).trim()
      return `<pre><code>${escapedCode}</code></pre>`
    })

    // 安全地处理行内代码
    formatted = formatted.replace(/`([^`]+)`/g, (match, code) => {
      const escapedCode = escapeHTML(code)
      return `<code>${escapedCode}</code>`
    })

    // 最后过滤确保安全
    return sanitizeHTML(formatted)
  } catch (error) {
    console.error('Format AI message failed:', error)
    return escapeHTML(content)
  }
}

/**
 * 检查 DOMPurify 是否可用
 * @returns {boolean}
 */
export function isDOMPurifyAvailable() {
  return typeof DOMPurify !== 'undefined' && typeof DOMPurify.sanitize === 'function'
}

export default {
  sanitizeHTML,
  highlightText,
  escapeHTML,
  formatAIMessage,
  isDOMPurifyAvailable
}
