export function isToday(timestamp) {
  if (!timestamp) return false
  const date = new Date(timestamp)
  const today = new Date()
  return (
    date.getFullYear() === today.getFullYear() &&
    date.getMonth() === today.getMonth() &&
    date.getDate() === today.getDate()
  )
}

export function formatMessageDate(timestamp) {
  if (!timestamp) return ''

  const date = new Date(timestamp)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (isToday(timestamp)) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  if (isYesterday(timestamp)) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  if (isThisWeek(timestamp)) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return (
      weekdays[date.getDay()] +
      ' ' +
      date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    )
  }

  return (
    date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }) +
    ' ' +
    date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  )
}

export function isYesterday(timestamp) {
  if (!timestamp) return false
  const date = new Date(timestamp)
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  return (
    date.getFullYear() === yesterday.getFullYear() &&
    date.getMonth() === yesterday.getMonth() &&
    date.getDate() === yesterday.getDate()
  )
}

export function isThisWeek(timestamp) {
  if (!timestamp) return false
  const date = new Date(timestamp)
  const now = new Date()
  const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
  return date >= oneWeekAgo
}

export function shouldShowDateDivider(currentTimestamp, previousTimestamp) {
  if (!currentTimestamp) return false
  if (!previousTimestamp) return true

  const currentDate = new Date(currentTimestamp)
  const previousDate = new Date(previousTimestamp)

  return (
    currentDate.getFullYear() !== previousDate.getFullYear() ||
    currentDate.getMonth() !== previousDate.getMonth() ||
    currentDate.getDate() !== previousDate.getDate()
  )
}

export function groupMessagesByDate(messages) {
  if (!Array.isArray(messages)) return []

  const groups = []
  let currentGroup = null

  for (const message of messages) {
    const messageDate = new Date(message.timestamp || message.createTime)
    const dateKey = `${messageDate.getFullYear()}-${messageDate.getMonth()}-${messageDate.getDate()}`

    if (!currentGroup || currentGroup.dateKey !== dateKey) {
      currentGroup = {
        dateKey,
        date: messageDate,
        messages: [],
      }
      groups.push(currentGroup)
    }

    currentGroup.messages.push(message)
  }

  return groups
}

export function getLastMessageText(message) {
  if (!message) return '暂无消息'

  switch (message.type) {
    case 'text':
      return message.content || '暂无消息'
    case 'oa': {
      if (message.content && typeof message.content === 'string') {
        const trimmed = message.content.trim()
        if (trimmed.startsWith('{') || trimmed.startsWith('[')) {
          try {
            const parsed = JSON.parse(message.content)
            if (parsed && typeof parsed === 'object') {
              return `[OA] ${parsed.title || parsed.subject || ''}`
            }
          } catch (e) {
            return '[OA]'
          }
        }
      }
      return `[OA] ${message.title || ''}`
    }
    case 'image':
      return '[图片]'
    case 'file':
      return `[文件] ${message.fileName || message.content || ''}`
    case 'voice':
      return '[语音]'
    case 'video':
      return '[视频]'
    case 'link':
      return `[链接] ${message.title || message.content || ''}`
    case 'card':
      return `[名片] ${message.name || ''}`
    case 'system':
      return message.content || '[系统消息]'
    case 'recall':
      return '[撤回了一条消息]'
    default:
      return message.content || '[未知消息]'
  }
}

export const MESSAGE_TYPE = {
  TEXT: 'text',
  IMAGE: 'image',
  FILE: 'file',
  VOICE: 'voice',
  VIDEO: 'video',
  LINK: 'link',
  CARD: 'card',
  OA: 'oa',
  SYSTEM: 'system',
  RECALL: 'recall',
}

export function getMessageTypeLabel(type) {
  const key = (type || '').toLowerCase()
  const labels = {
    [MESSAGE_TYPE.TEXT]: '文本消息',
    [MESSAGE_TYPE.IMAGE]: '图片消息',
    [MESSAGE_TYPE.FILE]: '文件消息',
    [MESSAGE_TYPE.VOICE]: '语音消息',
    [MESSAGE_TYPE.VIDEO]: '视频消息',
    [MESSAGE_TYPE.LINK]: '链接消息',
    [MESSAGE_TYPE.CARD]: '名片消息',
    [MESSAGE_TYPE.OA]: 'OA卡片消息',
    [MESSAGE_TYPE.SYSTEM]: '系统消息',
    [MESSAGE_TYPE.RECALL]: '撤回消息',
  }
  return labels[key] || '未知消息'
}

export function canRecallMessage(message, recallTimeout = 2 * 60 * 1000) {
  if (!message) return false
  if (message.type === 'system' || message.type === 'recall') return false

  const now = Date.now()
  const messageTime = new Date(message.timestamp || message.createTime).getTime()
  return now - messageTime <= recallTimeout
}

export function isMessageFromSelf(message, currentUserId) {
  if (!message || !currentUserId) return false
  return message.senderId === currentUserId || message.from === currentUserId
}

export function formatMessageStatus(status) {
  const statusMap = {
    sending: '发送中',
    sent: '已发送',
    delivered: '已送达',
    read: '已读',
    failed: '发送失败',
  }
  return statusMap[status] || '未知状态'
}

export function getUnreadCount(messages, lastReadTime) {
  if (!Array.isArray(messages) || !lastReadTime) return 0

  return messages.filter(message => {
    const messageTime = new Date(message.timestamp || message.createTime).getTime()
    return messageTime > lastReadTime
  }).length
}

export function getMentionedUsers(content) {
  if (!content) return []
  const mentions = content.match(/@(\S+)/g)
  if (!mentions) return []
  return mentions.map(mention => mention.substring(1))
}

export function hasMention(content, username) {
  if (!content || !username) return false
  return content.includes(`@${username}`)
}

export function extractUrls(content) {
  if (!content) return []
  const urlRegex = /(https?:\/\/[^\s]+)/g
  const matches = content.match(urlRegex)
  return matches || []
}

export function extractEmojis(content) {
  if (!content) return []
  const emojiRegex =
    /[\u{1F600}-\u{1F64F}\u{1F300}-\u{1F5FF}\u{1F680}-\u{1F6FF}\u{1F1E0}-\u{1F1FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}]/gu
  const matches = content.match(emojiRegex)
  return matches || []
}

export function generateMessageId() {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
}

export function createTextMessage(content) {
  return {
    id: generateMessageId(),
    type: 'text',
    content,
    timestamp: Date.now(),
    status: 'sending',
  }
}

export function createImageMessage(url, width, height) {
  return {
    id: generateMessageId(),
    type: 'image',
    content: url,
    width,
    height,
    timestamp: Date.now(),
    status: 'sending',
  }
}

export function createFileMessage(url, fileName, fileSize) {
  return {
    id: generateMessageId(),
    type: 'file',
    content: url,
    fileName,
    fileSize,
    timestamp: Date.now(),
    status: 'sending',
  }
}

export function createVoiceMessage(url, duration) {
  return {
    id: generateMessageId(),
    type: 'voice',
    content: url,
    duration,
    timestamp: Date.now(),
    status: 'sending',
  }
}

export function createVideoMessage(url, duration, thumbnail) {
  return {
    id: generateMessageId(),
    type: 'video',
    content: url,
    duration,
    thumbnail,
    timestamp: Date.now(),
    status: 'sending',
  }
}

export function createLinkMessage(url, title, description, thumbnail) {
  return {
    id: generateMessageId(),
    type: 'link',
    content: url,
    title,
    description,
    thumbnail,
    timestamp: Date.now(),
    status: 'sending',
  }
}

export function createCardMessage(userId, name, avatar) {
  return {
    id: generateMessageId(),
    type: MESSAGE_TYPE.CARD,
    userId,
    name,
    avatar,
    timestamp: Date.now(),
    status: 'sending',
  }
}

export function createOaApprovalMessage(payload) {
  const contentObject = {
    cardType: 'approval',
    approvalId: payload.approvalId,
    type: payload.type,
    typeName: payload.typeName,
    approvalTypeName: payload.approvalTypeName,
    title: payload.title,
    applicant: payload.applicant,
    applicantName: payload.applicantName,
    applyTime: payload.applyTime,
    createTime: payload.createTime,
    status: payload.status,
    statusText: payload.statusText,
    timeRange: payload.timeRange,
    startTime: payload.startTime,
    endTime: payload.endTime,
    amount: payload.amount,
    formSummary: payload.formSummary,
  }
  return {
    id: generateMessageId(),
    type: MESSAGE_TYPE.OA,
    content: JSON.stringify(contentObject),
    timestamp: Date.now(),
    status: 'sending',
  }
}
