/**
 * 聊天记录导出工具
 * 支持导出为 txt、html、pdf 格式
 */

import { formatMessagePreviewFromObject } from './message'
import { formatFileSize } from './format'
import dayjs from 'dayjs'

/**
 * 导出格式枚举
 */
export const EXPORT_FORMAT = {
  TXT: 'txt',
  HTML: 'html',
  PDF: 'pdf'
}

/**
 * 生成文件名
 * @param {string} contactName - 联系人名称
 * @param {string} format - 文件格式
 * @returns {string} 文件名
 */
export function generateExportFileName(contactName, format) {
  const date = dayjs().format('YYYYMMDD_HHmmss')
  const sanitizedName = contactName.replace(/[<>:"/\\|?*]/g, '_')
  return `聊天记录_${sanitizedName}_${date}.${format}`
}

/**
 * 导出为纯文本格式
 * @param {Array} messages - 消息列表
 * @param {Object} options - 导出选项
 * @returns {string} 文本内容
 */
export function exportToTxt(messages, options = {}) {
  const { title = '聊天记录', startTime, endTime } = options
  let content = ''

  // 标题和日期范围
  content += `${'='.repeat(40)}\n`
  content += `${title}\n`
  if (startTime || endTime) {
    content += `时间范围: ${dayjs(startTime).format('YYYY-MM-DD')} ~ ${dayjs(endTime).format('YYYY-MM-DD')}\n`
  } else {
    content += `导出时间: ${dayjs().format('YYYY-MM-DD HH:mm:ss')}\n`
  }
  content += `共 ${messages.length} 条消息\n`
  content += `${'='.repeat(40)}\n\n`

  // 按日期分组
  let lastDate = ''
  messages.forEach(msg => {
    const msgDate = dayjs(msg.timestamp || msg.sendTime || msg.createTime).format('YYYY-MM-DD')
    const msgTime = dayjs(msg.timestamp || msg.sendTime || msg.createTime).format('HH:mm:ss')

    // 日期分隔
    if (msgDate !== lastDate) {
      content += `\n【${msgDate}】\n\n`
      lastDate = msgDate
    }

    // 消息内容
    const sender = msg.senderName || '未知用户'
    const messageContent = formatMessageContent(msg, 'txt')

    content += `[${msgTime}] ${sender}\n`
    content += `${messageContent}\n\n`
  })

  return content
}

/**
 * 导出为 HTML 格式
 * @param {Array} messages - 消息列表
 * @param {Object} options - 导出选项
 * @returns {string} HTML 内容
 */
export function exportToHtml(messages, options = {}) {
  const { title = '聊天记录', contactName, startTime, endTime, currentUser } = options

  // 生成样式
  const styles = `
    <style>
      * { margin: 0; padding: 0; box-sizing: border-box; }
      body {
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
        line-height: 1.6;
        background: #f5f5f5;
        padding: 20px;
      }
      .container {
        max-width: 800px;
        margin: 0 auto;
        background: #fff;
        border-radius: 12px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
        overflow: hidden;
      }
      .header {
        background: linear-gradient(135deg, #0089FF 0%, #0958d9 100%);
        color: #fff;
        padding: 24px;
        text-align: center;
      }
      .header h1 { font-size: 24px; margin-bottom: 8px; }
      .header .meta { font-size: 14px; opacity: 0.9; }
      .content { padding: 20px; }
      .date-divider {
        text-align: center;
        margin: 20px 0;
        position: relative;
      }
      .date-divider::before,
      .date-divider::after {
        content: '';
        position: absolute;
        top: 50%;
        width: 40%;
        height: 1px;
        background: #e8e8e8;
      }
      .date-divider::before { left: 0; }
      .date-divider::after { right: 0; }
      .date-divider span {
        background: #fff;
        padding: 0 12px;
        color: #999;
        font-size: 12px;
        position: relative;
        z-index: 1;
      }
      .message {
        display: flex;
        margin-bottom: 16px;
        animation: fadeIn 0.3s ease;
      }
      .message.own {
        flex-direction: row-reverse;
      }
      .message-avatar {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        background: #e8e8e8;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        color: #666;
        flex-shrink: 0;
        font-size: 14px;
      }
      .message.own .message-avatar {
        background: linear-gradient(135deg, #0089FF, #0958d9);
        color: #fff;
      }
      .message-body {
        max-width: 60%;
        margin: 0 12px;
      }
      .message-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 4px;
      }
      .message.own .message-header {
        flex-direction: row-reverse;
      }
      .message-sender {
        font-size: 13px;
        font-weight: 500;
        color: #333;
      }
      .message-time {
        font-size: 11px;
        color: #999;
      }
      .message-bubble {
        background: #f0f0f0;
        padding: 10px 14px;
        border-radius: 12px;
        word-break: break-word;
      }
      .message.own .message-bubble {
        background: linear-gradient(135deg, #0089FF, #0958d9);
        color: #fff;
        border-radius: 12px 2px 12px 12px;
      }
      .message:not(.own) .message-bubble {
        border-radius: 2px 12px 12px 12px;
      }
      .message-text {
        font-size: 14px;
        line-height: 1.5;
        white-space: pre-wrap;
      }
      .message-image {
        max-width: 200px;
        border-radius: 8px;
      }
      .message-file {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px;
        background: rgba(0,0,0,0.05);
        border-radius: 6px;
      }
      .message-file .icon {
        font-size: 24px;
      }
      .message-file .info {
        flex: 1;
        min-width: 0;
      }
      .message-file .name {
        font-size: 13px;
        font-weight: 500;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .message-file .size {
        font-size: 11px;
        color: #666;
      }
      .message-voice {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 12px;
        background: linear-gradient(135deg, #667eea, #764ba2);
        color: #fff;
        border-radius: 16px;
        font-size: 13px;
      }
      .message-voice .icon { font-size: 16px; }
      .message-system {
        text-align: center;
        margin: 12px 0;
      }
      .message-system span {
        background: rgba(0,0,0,0.05);
        padding: 4px 12px;
        border-radius: 4px;
        font-size: 12px;
        color: #999;
      }
      @keyframes fadeIn {
        from { opacity: 0; transform: translateY(8px); }
        to { opacity: 1; transform: translateY(0); }
      }
      .footer {
        text-align: center;
        padding: 16px;
        background: #fafafa;
        font-size: 12px;
        color: #999;
        border-top: 1px solid #e8e8e8;
      }
    </style>
  `

  // 消息内容
  let messagesHtml = ''
  let lastDate = ''

  messages.forEach(msg => {
    const msgDate = dayjs(msg.timestamp || msg.sendTime || msg.createTime).format('YYYY-MM-DD')
    const msgTime = dayjs(msg.timestamp || msg.sendTime || msg.createTime).format('HH:mm:ss')
    const isOwn = msg.isOwn || msg.senderId === currentUser?.id
    const senderName = msg.senderName || '未知用户'

    // 日期分隔
    if (msgDate !== lastDate) {
      messagesHtml += `
        <div class="date-divider">
          <span>${msgDate}</span>
        </div>
      `
      lastDate = msgDate
    }

    // 系统消息
    if (msg.type === 'SYSTEM' || msg.type === 'RECALLED') {
      const systemText = msg.type === 'RECALLED'
        ? `${isOwn ? '你' : senderName}撤回了一条消息`
        : msg.content
      messagesHtml += `
        <div class="message-system">
          <span>${systemText}</span>
        </div>
      `
      return
    }

    // 普通消息
    const avatarChar = senderName.charAt(0).toUpperCase()
    const messageContent = formatMessageContent(msg, 'html')

    messagesHtml += `
      <div class="message ${isOwn ? 'own' : ''}">
        <div class="message-avatar">${avatarChar}</div>
        <div class="message-body">
          <div class="message-header">
            <span class="message-sender">${senderName}</span>
            <span class="message-time">${msgTime}</span>
          </div>
          <div class="message-bubble">
            ${messageContent}
          </div>
        </div>
      </div>
    `
  })

  // 完整 HTML
  return `
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>${title}</title>
      ${styles}
    </head>
    <body>
      <div class="container">
        <div class="header">
          <h1>${title}</h1>
          ${contactName ? `<p>与 ${contactName} 的聊天</p>` : ''}
          <div class="meta">
            ${startTime && endTime ? `时间: ${dayjs(startTime).format('YYYY-MM-DD')} ~ ${dayjs(endTime).format('YYYY-MM-DD')}` : `导出: ${dayjs().format('YYYY-MM-DD HH:mm:ss')}`}
            | 共 ${messages.length} 条消息
          </div>
        </div>
        <div class="content">
          ${messagesHtml}
        </div>
        <div class="footer">
          由 RuoYi-IM 导出生成
        </div>
      </div>
    </body>
    </html>
  `
}

/**
 * 格式化消息内容
 * @param {Object} message - 消息对象
 * @param {string} format - 格式类型 txt/html
 * @returns {string} 格式化后的内容
 */
function formatMessageContent(message, format) {
  const type = message.type || 'TEXT'
  const content = message.content

  switch (type) {
    case 'TEXT':
      return format === 'html'
        ? `<div class="message-text">${escapeHtml(content)}</div>`
        : content

    case 'IMAGE':
      if (format === 'html') {
        try {
          const contentObj = typeof content === 'string' ? JSON.parse(content) : content
          const imageUrl = contentObj.imageUrl || contentObj.url || ''
          return `<img class="message-image" src="${imageUrl}" alt="图片" />`
        } catch {
          return '<div class="message-text">[图片]</div>'
        }
      }
      return '[图片]'

    case 'FILE':
      try {
        const contentObj = typeof content === 'string' ? JSON.parse(content) : content
        const fileName = contentObj.fileName || contentObj.name || '未知文件'
        const fileSize = formatFileSize(contentObj.size)

        if (format === 'html') {
          return `
            <div class="message-file">
              <span class="icon">📄</span>
              <div class="info">
                <div class="name">${escapeHtml(fileName)}</div>
                <div class="size">${fileSize}</div>
              </div>
            </div>
          `
        }
        return `[文件] ${fileName} (${fileSize})`
      } catch {
        return '[文件]'
      }

    case 'VOICE':
    case 'AUDIO':
      try {
        const contentObj = typeof content === 'string' ? JSON.parse(content) : content
        const duration = contentObj.duration || 0
        const mins = Math.floor(duration / 60)
        const secs = Math.floor(duration % 60)
        const durationStr = `${mins}:${secs.toString().padStart(2, '0')}`

        if (format === 'html') {
          return `
            <div class="message-voice">
              <span class="icon">🎤</span>
              <span>${durationStr}</span>
            </div>
          `
        }
        return `[语音] ${durationStr}`
      } catch {
        return '[语音]'
      }

    case 'VIDEO':
      return format === 'html' ? '<div class="message-text">[视频]</div>' : '[视频]'

    case 'LOCATION':
      try {
        const contentObj = typeof content === 'string' ? JSON.parse(content) : content
        const address = contentObj.address || '位置信息'
        return format === 'html'
          ? `<div class="message-text">📍 ${escapeHtml(address)}</div>`
          : `[位置] ${address}`
      } catch {
        return '[位置]'
      }

    default:
      return `[${type}]`
  }
}

/**
 * 转义 HTML 特殊字符
 * @param {string} text - 原文本
 * @returns {string} 转义后的文本
 */
function escapeHtml(text) {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

/**
 * 下载文件
 * @param {string} content - 文件内容
 * @param {string} fileName - 文件名
 * @param {string} mimeType - MIME 类型
 */
export function downloadFile(content, fileName, mimeType) {
  const blob = new Blob([content], { type: mimeType })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * 导出聊天记录
 * @param {Array} messages - 消息列表
 * @param {Object} options - 导出选项
 * @param {string} options.format - 导出格式
 * @param {string} options.contactName - 联系人名称
 * @param {string} options.title - 聊天标题
 * @param {string} options.startTime - 开始时间
 * @param {string} options.endTime - 结束时间
 * @param {Object} options.currentUser - 当前用户
 */
export function exportChat(messages, options = {}) {
  const { format = EXPORT_FORMAT.TXT, contactName, title, currentUser } = options
  const fileName = generateExportFileName(contactName || '聊天记录', format)

  let content, mimeType

  switch (format) {
    case EXPORT_FORMAT.TXT:
      content = exportToTxt(messages, options)
      mimeType = 'text/plain;charset=utf-8'
      break
    case EXPORT_FORMAT.HTML:
      content = exportToHtml(messages, options)
      mimeType = 'text/html;charset=utf-8'
      break
    case EXPORT_FORMAT.PDF:
      // PDF 需要第三方库，这里先导出为 HTML
      content = exportToHtml(messages, options)
      mimeType = 'text/html;charset=utf-8'
      console.warn('PDF 导出需要第三方库支持，已导出为 HTML 格式')
      break
    default:
      throw new Error(`不支持的导出格式: ${format}`)
  }

  downloadFile(content, fileName, mimeType)
}

/**
 * 按日期分组消息
 * @param {Array} messages - 消息列表
 * @returns {Object} 分组后的消息
 */
export function groupMessagesByDate(messages) {
  const groups = {}
  messages.forEach(msg => {
    const date = dayjs(msg.timestamp || msg.sendTime || msg.createTime).format('YYYY-MM-DD')
    if (!groups[date]) {
      groups[date] = []
    }
    groups[date].push(msg)
  })
  return groups
}
