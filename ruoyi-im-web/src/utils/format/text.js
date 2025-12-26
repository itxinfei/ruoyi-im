export function escapeHtml(text) {
  if (!text) return ''
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;',
  }
  return text.replace(/[&<>"']/g, m => map[m])
}

export function unescapeHtml(text) {
  if (!text) return ''
  const map = {
    '&amp;': '&',
    '&lt;': '<',
    '&gt;': '>',
    '&quot;': '"',
    '&#039;': "'",
  }
  return text.replace(/&amp;|&lt;|&gt;|&quot;|&#039;/g, m => map[m])
}

export function formatText(text) {
  if (!text) return ''
  let result = escapeHtml(text)
  result = result.replace(/\n/g, '<br>')
  result = result.replace(/(https?:\/\/[^\s]+)/g, '<a href="$1" target="_blank" class="text-link">$1</a>')
  return result
}

export function truncateText(text, maxLength = 100, suffix = '...') {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.slice(0, maxLength) + suffix
}

export function highlightText(text, keyword, className = 'highlight') {
  if (!text || !keyword) return text
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, `<span class="${className}">$1</span>`)
}

export function stripHtml(html) {
  if (!html) return ''
  return html.replace(/<[^>]*>/g, '')
}

export function stripTags(html, allowedTags = []) {
  if (!html) return ''
  if (allowedTags.length === 0) return stripHtml(html)
  
  const tags = allowedTags.join('|')
  const regex = new RegExp(`<(?!\/?(?:${tags})\s*\/?>)[^>]+>`, 'gi')
  return html.replace(regex, '')
}

export function formatPhoneNumber(phone) {
  if (!phone) return ''
  const cleaned = phone.replace(/\D/g, '')
  if (cleaned.length === 11) {
    return `${cleaned.slice(0, 3)} ${cleaned.slice(3, 7)} ${cleaned.slice(7)}`
  }
  return phone
}

export function maskPhoneNumber(phone) {
  if (!phone) return ''
  const cleaned = phone.replace(/\D/g, '')
  if (cleaned.length === 11) {
    return `${cleaned.slice(0, 3)}****${cleaned.slice(7)}`
  }
  return phone
}

export function maskEmail(email) {
  if (!email) return ''
  const [name, domain] = email.split('@')
  if (name.length <= 2) {
    return `${name[0]}*@${domain}`
  }
  return `${name[0]}${'*'.repeat(name.length - 2)}${name[name.length - 1]}@${domain}`
}

export function generateAvatarUrl(name, size = 100) {
  const colors = ['#f56a00', '#7265e6', '#ffbf00', '#00a2ae', '#87d068']
  const firstChar = name ? name.charAt(0).toUpperCase() : '?'
  const colorIndex = name ? name.charCodeAt(0) % colors.length : 0
  const backgroundColor = colors[colorIndex]
  
  const canvas = document.createElement('canvas')
  canvas.width = size
  canvas.height = size
  const ctx = canvas.getContext('2d')
  
  ctx.fillStyle = backgroundColor
  ctx.fillRect(0, 0, size, size)
  
  ctx.fillStyle = '#fff'
  ctx.font = `bold ${size * 0.5}px Arial`
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(firstChar, size / 2, size / 2)
  
  return canvas.toDataURL('image/png')
}
