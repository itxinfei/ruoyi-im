export function formatTime(timestamp) {
  if (!timestamp) return ''

  const date = new Date(timestamp)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
  const messageDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

  if (messageDate.getTime() === today.getTime()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  if (messageDate.getTime() === yesterday.getTime()) {
    return '昨天'
  }

  const oneWeekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
  if (messageDate.getTime() > oneWeekAgo.getTime()) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return weekdays[date.getDay()]
  }

  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

export function formatFullTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

export function formatShortTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// formatMessageDate 已移至 @/utils/message.js
// 请使用 import { formatMessageDate } from '@/utils/message'

export function formatDuration(seconds) {
  if (!seconds && seconds !== 0) return ''
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

export function formatRelativeTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) {
    return '刚刚'
  } else if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return formatTime(timestamp)
  }
}
