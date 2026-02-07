/**
 * 通用格式化工具函数
 * 统一项目中的时间、文件大小等格式化逻辑，避免重复代码
 */

/**
 * 格式化时间为 HH:mm 格式（聊天消息常用）
 * @param {Number|String|Date} date - 日期对象、时间戳或日期字符串
 * @returns {String} 格式化后的时间字符串 "HH:mm"
 * @example
 * formatTime(new Date()) // "14:30"
 * formatTime(1706580600000) // "14:30"
 */
export function formatTime(date) {
  if (!date) {return ''}
  const d = new Date(date)
  if (isNaN(d.getTime())) {return ''}
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

/**
 * 格式化时间为 HH:mm:ss 格式
 * @param {Number|String|Date} date - 日期对象、时间戳或日期字符串
 * @returns {String} 格式化后的时间字符串 "HH:mm:ss"
 */
export function formatTimeFull(date) {
  if (!date) {return ''}
  const d = new Date(date)
  if (isNaN(d.getTime())) {return ''}
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${hours}:${minutes}:${seconds}`
}

/**
 * 格式化日期为 MM月DD日 格式
 * @param {Number|String|Date} date - 日期对象、时间戳或日期字符串
 * @returns {String} 格式化后的日期字符串 "MM月DD日"
 */
export function formatDate(date) {
  if (!date) {return ''}
  const d = new Date(date)
  if (isNaN(d.getTime())) {return ''}
  const month = d.getMonth() + 1
  const day = d.getDate()
  return `${month}月${day}日`
}

/**
 * 格式化日期为 YYYY-MM-DD 格式
 * @param {Number|String|Date} date - 日期对象、时间戳或日期字符串
 * @returns {String} 格式化后的日期字符串 "YYYY-MM-DD"
 */
export function formatDateISO(date) {
  if (!date) {return ''}
  const d = new Date(date)
  if (isNaN(d.getTime())) {return ''}
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 格式化日期时间为 YYYY-MM-DD HH:mm:ss 格式
 * @param {Number|String|Date} date - 日期对象、时间戳或日期字符串
 * @returns {String} 格式化后的日期时间字符串
 */
export function formatDateTimeISO(date) {
  if (!date) {return ''}
  const d = new Date(date)
  if (isNaN(d.getTime())) {return ''}
  return `${formatDateISO(d)} ${formatTimeFull(d)}`
}

/**
 * 格式化相对时间（如：刚刚、5分钟前）
 * @param {Number|String|Date} timestamp - 时间戳
 * @returns {String} 相对时间字符串
 */
export function formatRelativeTime(timestamp) {
  if (!timestamp) {return ''}
  const date = new Date(timestamp)
  const now = new Date()
  if (isNaN(date.getTime())) {return ''}

  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) {return '刚刚'}
  if (diffMins < 60) {return `${diffMins}分钟前`}
  if (diffHours < 24) {return `${diffHours}小时前`}
  if (diffDays < 7) {return `${diffDays}天前`}

  // 超过7天显示具体日期
  return formatDate(date)
}

/**
 * 智能时间格式化（内部统一实现）
 * @param {Number|String|Date} timestamp - 时间戳
 * @param {Object} options - 格式化选项
 * @param {String} options.style - 风格 'chat' | 'list'
 * @param {Boolean} options.showWeekday - 是否显示星期（仅 chat 风格）
 * @param {Boolean} options.showTime - 是否显示时间（仅 chat 风格）
 * @returns {String} 格式化后的时间
 * @private
 */
function _formatTimeSmart(timestamp, options = {}) {
  if (!timestamp) {return ''}
  const date = new Date(timestamp)
  const now = new Date()
  if (isNaN(date.getTime())) {return ''}

  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const targetDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const diffDays = Math.floor((today - targetDate) / 86400000)
  const style = options.style || 'chat'

  if (style === 'chat') {
    // 聊天风格：今天/昨天/周X/日期
    if (diffDays === 0) {
      return formatTime(date)
    } else if (diffDays === 1) {
      return `昨天 ${formatTime(date)}`
    } else if (diffDays < 7) {
      const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const weekday = weekdays[date.getDay()]
      return `${weekday} ${formatTime(date)}`
    } else {
      return `${formatDate(date)} ${formatTime(date)}`
    }
  } else {
    // 列表风格：时间/X天前/月日
    if (diffDays === 0) {
      return formatTime(date)
    } else if (diffDays < 7) {
      return `${diffDays}天前`
    } else {
      const month = date.toLocaleDateString('zh-CN', { month: 'short' })
      return `${month}${date.getDate()}日`
    }
  }
}

/**
 * 聊天消息时间格式化（根据时间差智能显示）
 * @param {Number|String|Date} timestamp - 时间戳
 * @returns {String} 格式化后的时间
 * @example
 * formatChatTime(Date.now())           // "14:30" (今天)
 * formatChatTime(Date.now() - 86400000) // "昨天 14:30"
 * formatChatTime(Date.now() - 259200000) // "周一 14:30" (3天前)
 */
export function formatChatTime(timestamp) {
  return _formatTimeSmart(timestamp, { style: 'chat' })
}

/**
 * 格式化文件大小
 * @param {Number} bytes - 字节数
 * @returns {String} 格式化后的大小字符串
 */
export function formatFileSize(bytes) {
  if (!bytes || bytes === 0) {return '0 B'}
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化持续时间（秒转为可读格式）
 * @param {Number} seconds - 秒数
 * @returns {String} 格式化后的持续时间
 * @example
 * formatDuration(65) // "1分5秒"
 * formatDuration(3665) // "1小时1分5秒"
 */
export function formatDuration(seconds) {
  if (!seconds || seconds < 0) {return '0秒'}

  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = Math.floor(seconds % 60)

  const parts = []
  if (hours > 0) {parts.push(`${hours}小时`)}
  if (minutes > 0) {parts.push(`${minutes}分`)}
  if (secs > 0 || parts.length === 0) {parts.push(`${secs}秒`)}

  return parts.join('')
}

/**
 * 格式化数字（添加千分位分隔符）
 * @param {Number} num - 数字
 * @returns {String} 格式化后的字符串
 * @example
 * formatNumber(1234567) // "1,234,567"
 */
export function formatNumber(num) {
  if (num === null || num === undefined) {return ''}
  return String(num).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 格式化百分比
 * @param {Number} value - 数值
 * @param {Number} total - 总数
 * @param {Number} decimals - 小数位数，默认0
 * @returns {String} 百分比字符串
 * @example
 * formatPercent(50, 200) // "25%"
 * formatPercent(1, 3, 2) // "33.33%"
 */
export function formatPercent(value, total, decimals = 0) {
  if (!total || total === 0) {return '0%'}
  const percent = (value / total) * 100
  return percent.toFixed(decimals) + '%'
}

/**
 * 截断文本并添加省略号
 * @param {String} text - 原文本
 * @param {Number} maxLength - 最大长度
 * @returns {String} 截断后的文本
 */
export function truncateText(text, maxLength) {
  if (!text) {return ''}
  if (text.length <= maxLength) {return text}
  return text.substring(0, maxLength) + '...'
}

/**
 * 格式化手机号（隐藏中间4位）
 * @param {String} phone - 手机号
 * @returns {String} 格式化后的手机号
 * @example
 * formatPhone('13812345678') // "138****5678"
 */
export function formatPhone(phone) {
  if (!phone) {return ''}
  const phoneStr = String(phone)
  if (phoneStr.length !== 11) {return phoneStr}
  return phoneStr.substring(0, 3) + '****' + phoneStr.substring(7)
}

/**
 * 格式化身份证号（隐藏中间部分）
 * @param {String} idCard - 身份证号
 * @returns {String} 格式化后的身份证号
 */
export function formatIdCard(idCard) {
  if (!idCard) {return ''}
  const idStr = String(idCard)
  if (idStr.length < 8) {return idStr}
  return idStr.substring(0, 4) + '**********' + idStr.substring(idStr.length - 4)
}

/**
 * 复制文本到剪贴板
 * @param {String} text - 要复制的文本
 * @param {Object} options - 选项
 * @param {String} options.successMsg - 成功提示消息，默认"已复制到剪贴板"
 * @param {String} options.emptyMsg - 空内容提示消息，默认"暂无内容可复制"
 * @param {Array<String>} options.emptyValues - 视为空内容的值，默认['-', '未填写', '未设置']
 * @returns {Promise<Boolean>} 是否复制成功
 */
export async function copyToClipboard(text, options = {}) {
  const {
    successMsg = '已复制到剪贴板',
    emptyMsg = '暂无内容可复制',
    emptyValues = ['-', '未填写', '未设置']
  } = options

  // 动态导入 ElMessage 避免循环依赖
  const { ElMessage } = await import('element-plus')

  // 检查是否为空内容
  if (!text || emptyValues.includes(String(text).trim())) {
    ElMessage.warning(emptyMsg)
    return false
  }

  try {
    // 优先使用现代 Clipboard API
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(text)
      ElMessage.success(successMsg)
      return true
    }
  } catch (error) {
    // 降级处理：兼容旧浏览器
    try {
      const textArea = document.createElement('textarea')
      textArea.value = text
      textArea.style.position = 'fixed'
      textArea.style.left = '-999999px'
      textArea.style.top = '-999999px'
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      const successful = document.execCommand('copy')
      document.body.removeChild(textArea)

      if (successful) {
        ElMessage.success(successMsg)
        return true
      } else {
        ElMessage.error('复制失败，请手动复制')
        return false
      }
    } catch (e) {
      ElMessage.error('复制失败，请手动复制')
      return false
    }
  }

  return false
}

/**
 * 格式化列表项时间（适用于文件列表、公告列表等）
 * 今天显示 HH:mm，7天内显示 X天前，更早显示 月日
 * @param {Number|String|Date} time - 时间
 * @returns {String} 格式化后的时间字符串
 * @example
 * formatListItemTime(Date.now())           // "14:30" (今天)
 * formatListItemTime(Date.now() - 86400000) // "1天前"
 * formatListItemTime(Date.now() - 604800000) // "2月5日" (7天前)
 */
export function formatListItemTime(time) {
  return _formatTimeSmart(time, { style: 'list' })
}

/**
 * 格式化时长为 mm:ss 格式（适用于语音、通话时长显示）
 * @param {Number} seconds - 秒数
 * @returns {String} 格式化后的时长 "mm:ss"
 */
export function formatDurationMMSS(seconds) {
  if (!seconds || seconds < 0) {return '00:00'}

  const totalSeconds = Math.floor(seconds)
  const minutes = Math.floor(totalSeconds / 60)
  const secs = totalSeconds % 60

  return `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}
