/**
 * 时间格式化工具
 * 提供统一的时间格式化方法，用于会话列表、消息列表等组件
 */

/**
 * 格式化会话时间显示（会话列表使用）
 * @param {number|string} timestamp - 时间戳
 * @returns {string} 格式化后的时间字符串
 * @example
 * formatTime(Date.now()) // => '14:30'
 * formatTime(Date.now() - 86400000) // => '昨天'
 * formatTime(Date.now() - 172800000) // => '1/15'
 */
export function formatTime(timestamp) {
  if (!timestamp) return ''

  const date = new Date(timestamp)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 86400000)

  // 今天：显示时分
  if (date >= today) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  // 昨天：显示"昨天"
  if (date >= yesterday) {
    return '昨天'
  }
  // 今年：显示月/日
  if (date.getFullYear() === now.getFullYear()) {
    return `${date.getMonth() + 1}/${date.getDate()}`
  }
  // 其他：显示年/月/日
  return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`
}

/**
 * 格式化时间分隔符（消息列表使用）
 * @param {number|string} timestamp - 时间戳
 * @returns {string} 格式化后的时间字符串
 * @example
 * formatTimeDivider(Date.now()) // => '14:30'
 * formatTimeDivider(Date.now() - 86400000) // => '昨天 14:30'
 * formatTimeDivider(Date.now() - 259200000) // => '星期一 14:30'
 */
export function formatTimeDivider(timestamp) {
  const date = new Date(timestamp)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

  const diffDays = Math.floor((today - msgDate) / (1000 * 60 * 60 * 24))

  const timeStr = date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  })

  if (diffDays === 0) return timeStr
  if (diffDays === 1) return `昨天 ${timeStr}`
  if (diffDays === 2) return `前天 ${timeStr}`
  if (diffDays < 7) {
    const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
    return `${weekDays[date.getDay()]} ${timeStr}`
  }

  return `${date.getMonth() + 1}月${date.getDate()}日 ${timeStr}`
}

/**
 * 判断两个时间是否需要显示时间分隔符
 * @param {number} currentTime - 当前消息时间戳
 * @param {number} previousTime - 前一条消息时间戳
 * @param {number} index - 消息索引
 * @param {number} [intervalMinutes=30] - 时间间隔（分钟）
 * @param {number} [messageInterval=10] - 消息间隔数
 * @returns {boolean} 是否需要显示分隔符
 */
export function shouldShowTimeDivider(currentTime, previousTime, index, intervalMinutes = 30, messageInterval = 10) {
  // 第一条消息总是显示时间
  if (index === 0) return true

  const timeDiff = currentTime - previousTime
  // 超过设定间隔或每N条消息显示一次时间
  return timeDiff > intervalMinutes * 60 * 1000 || index % messageInterval === 0
}

/**
 * Composable：时间格式化
 * @returns {Object} 时间格式化方法
 */
export function useTimeFormat() {
  return {
    formatTime,
    formatTimeDivider,
    shouldShowTimeDivider
  }
}
