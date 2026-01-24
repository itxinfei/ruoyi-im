/**
 * 消息格式化工具函数
 */

/**
 * 根据消息类型格式化消息预览文本
 * @param {String} type - 消息类型 (TEXT/IMAGE/FILE/VIDEO/VOICE/RECALLED等)
 * @param {String|Object} content - 消息内容
 * @returns {String} 格式化后的预览文本
 */
export function formatMessagePreview(type, content) {
    if (!content && type !== 'RECALLED') return '[空消息]'

    switch (type) {
        case 'TEXT':
            const text = String(content)
            return text.length > 30 ? text.substring(0, 30) + '...' : text

        case 'IMAGE':
            return '[图片]'

        case 'FILE':
            try {
                const fileInfo = typeof content === 'string' ? JSON.parse(content) : content
                const fileName = fileInfo.fileName || fileInfo.name || '未知文件'
                return `[文件] ${fileName}`
            } catch (e) {
                return '[文件]'
            }

        case 'VIDEO':
            return '[视频]'

        case 'VOICE':
        case 'AUDIO':
            return '[语音]'

        case 'LOCATION':
            return '[位置]'

        case 'RECALLED':
            return '[消息已撤回]'

        case 'SYSTEM':
            return content || '[系统消息]'

        default:
            return '[未知消息]'
    }
}

/**
 * 根据消息对象格式化消息预览文本
 * @param {Object} message - 消息对象 (ImMessageVO)
 * @returns {String} 格式化后的预览文本
 */
export function formatMessagePreviewFromObject(message) {
    if (!message) return '[空消息]'
    
    const type = message.type
    const content = message.content
    
    if (!content && type !== 'RECALLED') return '[空消息]'

    switch (type) {
        case 'TEXT':
            const text = String(content)
            return text.length > 30 ? text.substring(0, 30) + '...' : text

        case 'IMAGE':
            return '[图片]'

        case 'FILE':
            try {
                const fileInfo = typeof content === 'string' ? JSON.parse(content) : content
                const fileName = fileInfo.fileName || fileInfo.name || '未知文件'
                return `[文件] ${fileName}`
            } catch (e) {
                return '[文件]'
            }

        case 'VIDEO':
            return '[视频]'

        case 'VOICE':
        case 'AUDIO':
            return '[语音]'

        case 'LOCATION':
            return '[位置]'

        case 'RECALLED':
            return '[消息已撤回]'

        case 'SYSTEM':
            return content || '[系统消息]'

        default:
            return '[未知消息]'
    }
}

/**
 * 格式化文件大小
 * @param {Number} bytes - 字节数
 * @returns {String} 格式化后的大小字符串
 */
export function formatFileSize(bytes) {
    if (!bytes || bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化时间戳为相对时间
 * @param {Number|String|Date} timestamp - 时间戳
 * @returns {String} 格式化后的时间字符串
 */
export function formatRelativeTime(timestamp) {
    const date = new Date(timestamp)
    const now = new Date()
    const diffMs = now - date
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMs / 3600000)
    const diffDays = Math.floor(diffMs / 86400000)

    if (diffMins < 1) return '刚刚'
    if (diffMins < 60) return `${diffMins}分钟前`
    if (diffHours < 24) return `${diffHours}小时前`
    if (diffDays < 7) return `${diffDays}天前`

    // 超过7天显示具体日期
    const month = date.getMonth() + 1
    const day = date.getDate()
    return `${month}月${day}日`
}

/**
 * 解析消息内容（处理JSON格式）
 * @param {Object} message - 消息对象
 * @returns {Object|String} 解析后的内容
 */
export function parseMessageContent(message) {
    if (!message || !message.content) return {}

    // 如果是文本消息，直接返回
    if (message.type === 'TEXT') {
        return message.content
    }

    // 其他类型尝试解析JSON
    try {
        return typeof message.content === 'string'
            ? JSON.parse(message.content)
            : message.content
    } catch (e) {
        return message.content
    }
}
