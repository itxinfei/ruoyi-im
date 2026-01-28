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
    // 统一转为大写，兼容后端可能返回的小写类型
    type = (type || '').toUpperCase()
    if (!content && type !== 'RECALLED') return '[暂无消息]'

    switch (type) {
        case 'TEXT':
            const text = String(content)
            return text.length > 30 ? text.substring(0, 30) + '...' : text

        case 'IMAGE':
            return '[图片]'

        case 'FILE':
            try {
                const fileInfo = typeof content === 'string' ? JSON.parse(content) : content
                const fileName = fileInfo.fileName || fileInfo.name || fileInfo.file_name || '未知文件'
                return `[文件] ${fileName}`
            } catch (e) {
                console.warn('解析文件消息失败:', e, content)
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
            // 如果type为空，尝试从content推断
            if (!type && content) {
                if (typeof content === 'string') {
                    if (content.startsWith('http')) {
                        // 可能是图片或文件URL
                        if (/\.(jpg|jpeg|png|gif|webp)$/i.test(content)) return '[图片]'
                        return '[文件]'
                    }
                    // 纯文本
                    return content.length > 30 ? content.substring(0, 30) + '...' : content
                }
                // 对象类型
                if (typeof content === 'object') {
                    if (content.fileName || content.name) return `[文件] ${content.fileName || content.name}`
                    if (content.imageUrl || content.fileUrl) return '[图片]'
                }
            }
            return type ? `[${type}]` : '[消息]'
    }
}

/**
 * 根据消息对象格式化消息预览文本
 * @param {Object|String} message - 消息对象 (ImMessageVO) 或已格式化的字符串
 * @returns {String} 格式化后的预览文本
 */
export function formatMessagePreviewFromObject(message) {
    if (!message) return '[空消息]'

    // 如果已经是字符串（已格式化），直接返回
    if (typeof message === 'string') {
        // 过滤掉方括号包裹的类型标记，保留实际内容
        const str = message.trim()
        if (str.startsWith('[') && str.endsWith(']')) {
            // 如果是纯类型标记如 [图片]、[文件] 等，直接返回
            const content = str.slice(1, -1)
            if (['图片', '文件', '视频', '语音', '位置', '系统消息', '空消息', '未知消息'].includes(content)) {
                return str
            }
            // 如果是 [文件] xxx 格式，返回完整内容
            return str
        }
        return str
    }
    
    // 处理消息类型，兼容可能的字段名差异，并统一转为大写
    const type = (message.type || message.messageType || message.message_type || '').toUpperCase()
    
    // 检查是否为撤回消息
    if (message.isRevoked === 1 || message.is_revoked === 1 || type === 'RECALLED') {
        // 钉钉风格：根据发送者显示不同的撤回提示
        if (message.isSelf) {
            return '你撤回了一条消息'
        } else {
            return `${message.senderName || '对方'}撤回了一条消息`
        }
    }
    
    const content = message.content
    
    // 如果内容为空且不是撤回消息
    if (!content) {
        switch (type) {
            case 'TEXT': return '[空消息]'
            case 'IMAGE': return '[图片]'
            case 'FILE': return '[文件]'
            case 'VIDEO': return '[视频]'
            case 'VOICE':
            case 'AUDIO': return '[语音]'
            case 'SYSTEM': return content || '[系统消息]'
            default: return type ? `[${type}]` : '[暂无消息]'
        }
    }

    switch (type) {
        case 'TEXT':
            const text = String(content)
            // 钉钉风格：文本消息直接显示，支持表情
            return text.length > 30 ? text.substring(0, 30) + '...' : text

        case 'IMAGE':
            // 钉钉风格：显示"[图片]"，不显示文件名
            return '[图片]'

        case 'FILE':
            try {
                const fileInfo = typeof content === 'string' ? JSON.parse(content) : content
                const fileName = fileInfo.fileName || fileInfo.name || fileInfo.file_name || '未知文件'
                // 钉钉风格：显示文件名
                return fileName
            } catch (e) {
                console.warn('解析文件消息失败:', e, content)
                return '[文件]'
            }

        case 'VIDEO':
            // 钉钉风格：显示"[视频]"
            return '[视频]'

        case 'VOICE':
        case 'AUDIO':
            // 钉钉风格：显示"[语音]"
            return '[语音]'

        case 'LOCATION':
            // 钉钉风格：显示位置信息
            try {
                const locationInfo = typeof content === 'string' ? JSON.parse(content) : content
                const address = locationInfo.address || locationInfo.name || '位置'
                return address === '位置' ? '[位置]' : address
            } catch (e) {
                return '[位置]'
            }

        case 'SYSTEM':
            return content || '[系统消息]'

        default:
            // 尝试识别常见格式
            if (typeof content === 'string') {
                // 如果是JSON，尝试提取文件名
                try {
                    const parsed = JSON.parse(content)
                    if (parsed.fileName || parsed.name || parsed.file_name) {
                        const fileName = parsed.fileName || parsed.name || parsed.file_name
                        return type === 'IMAGE' ? '[图片]' : fileName
                    }
                    if (parsed.imageUrl || parsed.fileUrl || parsed.url) {
                        return type === 'IMAGE' ? '[图片]' : '[文件]'
                    }
                } catch (e) {
                    // 忽略解析错误
                }
            }
            
            // 根据类型返回提示
            switch (type) {
                case 'IMAGE': return '[图片]'
                case 'FILE': return '[文件]'
                case 'VIDEO': return '[视频]'
                case 'VOICE':
                case 'AUDIO': return '[语音]'
                default: return type || '[暂无消息]'
            }
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
