/**
 * 搜索高亮 composable
 * 用于在联系人、消息等列表中高亮显示搜索关键词
 */
import { computed } from 'vue'

/**
 * 转义 HTML 特殊字符，防止 XSS 攻击
 * @param {string} str - 需要转义的字符串
 * @returns {string} 转义后的字符串
 */
export function escapeHtml(str) {
  if (!str) {
    return ''
  }
  const div = document.createElement('div')
  div.textContent = str
  return div.innerHTML
}

/**
 * 搜索高亮 composable
 * @param {string} query - 搜索关键词
 */
export function useHighlightText(query = '') {
  /**
   * 高亮文本中的关键词
   * @param {string} text - 原始文本
   * @param {string} keyword - 搜索关键词
   * @param {string} className - 高亮样式类名，默认为 'highlight'
   * @returns {string} 带高亮标记的HTML字符串
   */
  const highlightText = (text, keyword, className = 'highlight') => {
    if (!text) {
      return ''
    }
    if (!keyword) {
      return escapeHtml(text)
    }

    // 转义特殊字符
    const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    const regex = new RegExp(`(${escapedKeyword})`, 'gi')

    return escapeHtml(text).replace(regex, `<span class="${className}">$1</span>`)
  }

  /**
   * 计算属性版本的高亮函数，使用响应式 query
   */
  const highlight = (text) => {
    return highlightText(text, query.value || query)
  }

  /**
   * 批量高亮对象中的多个字段
   * @param {Object} item - 数据对象
   * @param {string[]} fields - 需要高亮的字段名数组
   * @returns {Object} 包含高亮字段的新对象
   */
  const highlightFields = (item, fields) => {
    if (!item) {
      return {}
    }
    const result = { ...item }
    fields.forEach(field => {
      if (result[field]) {
        result[field + 'Highlighted'] = highlight(result[field])
      }
    })
    return result
  }

  /**
   * 过滤并高亮列表项
   * @param {Array} list - 原始列表
   * @param {string[]} searchFields - 搜索的字段名数组
   * @param {string[]} highlightFields - 需要高亮的字段名数组
   * @returns {Array} 过滤并高亮后的列表
   */
  const filterAndHighlight = (list, searchFields, fieldsToHighlight = []) => {
    if (!list || !list.length) {
      return []
    }
    const q = (query.value || query).toLowerCase().trim()

    if (!q) {
      return list
    }

    return list
      .filter(item => {
        return searchFields.some(field => {
          const value = item[field]
          return value && String(value).toLowerCase().includes(q)
        })
      })
      .map(item => {
        if (fieldsToHighlight.length > 0) {
          return highlightFields(item, fieldsToHighlight)
        }
        return item
      })
  }

  return {
    highlightText,
    highlight,
    highlightFields,
    filterAndHighlight,
    escapeHtml
  }
}

export default useHighlightText
