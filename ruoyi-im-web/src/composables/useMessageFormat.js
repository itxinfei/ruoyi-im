/**
 * 消息格式化工具
 * 提供消息内容的格式化和转换方法
 */

/**
 * 消息类型常量
 */
export const MessageType = {
  TEXT: 'TEXT',
  IMAGE: 'IMAGE',
  FILE: 'FILE',
  VOICE: 'VOICE',
  VIDEO: 'VIDEO',
  RECALLED: 'RECALLED',
  NOTIFICATION: 'NOTIFICATION'
}

/**
 * 格式化消息内容预览
 * @param {Object} message - 消息对象
 * @returns {string} 格式化后的预览文本
 */
export function formatMessagePreview(message) {
  if (!message) return ''

  // 撤回消息
  if (message.type === MessageType.RECALLED || message.isRevoked) {
    return '[消息已撤回]'
  }

  // 通知消息
  if (message.type === MessageType.NOTIFICATION) {
    return message.content || '[系统通知]'
  }

  // 文本消息
  if (message.type === MessageType.TEXT) {
    return message.content || ''
  }

  // 图片消息
  if (message.type === MessageType.IMAGE) {
    return '[图片]'
  }

  // 文件消息
  if (message.type === MessageType.FILE) {
    const fileName = message.content?.fileName || message.fileUrl?.split('/').pop() || '文件'
    return `[文件] ${fileName}`
  }

  // 语音消息
  if (message.type === MessageType.VOICE) {
    const duration = message.content?.duration || 0
    return `[语音] ${Math.ceil(duration)}"`
  }

  // 视频消息
  if (message.type === MessageType.VIDEO) {
    return '[视频]'
  }

  // 未知类型
  return '[消息]'
}

/**
 * 获取消息文件图标
 * @param {string} fileName - 文件名
 * @returns {string} Material Icon 名称
 */
export function getFileIcon(fileName) {
  if (!fileName) return 'description'

  const ext = fileName.split('.').pop().toLowerCase()

  const iconMap = {
    // 图片
    jpg: 'image', jpeg: 'image', png: 'image', gif: 'image', webp: 'image', svg: 'image',
    // 文档
    pdf: 'picture_as_pdf', doc: 'description', docx: 'description',
    xls: 'table_chart', xlsx: 'table_chart',
    ppt: 'slideshow', pptx: 'slideshow',
    txt: 'text_snippet', md: 'description',
    // 压缩包
    zip: 'folder_zip', rar: 'folder_zip', '7z': 'folder_zip', tar: 'folder_zip',
    // 音频
    mp3: 'audio_file', wav: 'audio_file', flac: 'audio_file', aac: 'audio_file',
    // 视频
    mp4: 'video_file', avi: 'video_file', mkv: 'video_file', mov: 'video_file',
    // 代码
    js: 'javascript', ts: 'code', html: 'code', css: 'code',
    json: 'code', xml: 'code', java: 'code', py: 'code'
  }

  return iconMap[ext] || 'description'
}

/**
 * 判断是否为图片文件
 * @param {string} fileName - 文件名
 * @returns {boolean}
 */
export function isImageFile(fileName) {
  if (!fileName) return false
  const ext = fileName.split('.').pop().toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp'].includes(ext)
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的大小
 */
export function formatFileSize(bytes) {
  if (!bytes || bytes === 0) return '0 B'

  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return `${(bytes / Math.pow(k, i)).toFixed(i > 1 ? 2 : 0)} ${units[i]}`
}

/**
 * 解析消息内容中的 @ 提及
 * @param {string} content - 消息内容
 * @returns {Array} 提及的用户列表
 */
export function parseMentions(content) {
  if (!content) return []

  const mentionRegex =/@\(userId:(\d+),nickname:([^\)]+)\)/g
  const mentions = []
  let match

  while ((match = mentionRegex.exec(content)) !== null) {
    mentions.push({
      userId: match[1],
      nickname: match[2],
      fullMatch: match[0]
    })
  }

  return mentions
}

/**
 * 将消息内容中的 @ 提及替换为可点击的 HTML
 * @param {string} content - 消息内容
 * @param {Function} onClick - 点击回调
 * @returns {string} HTML 字符串
 */
export function renderMentions(content, onClick) {
  if (!content) return ''

  return content.replace(/@\(userId:(\d+),nickname:([^\)]+)\)/g, (match, userId, nickname) => {
    return `<span class="mention-mention" data-user-id="${userId}" title="@${nickname}">@${nickname}</span>`
  })
}

/**
 * Composable：消息格式化
 * @returns {Object} 消息格式化方法
 */
export function useMessageFormat() {
  return {
    MessageType,
    formatMessagePreview,
    getFileIcon,
    isImageFile,
    formatFileSize,
    parseMentions,
    renderMentions
  }
}
