/**
 * 消息格式化工具函数
 */

// 从 format.js 重新导出，避免重复定义
export { formatFileSize, formatRelativeTime } from './format.js'

/**
 * 消息状态枚举
 */
export const MessageStatus = {
  PENDING: 'PENDING',       // 发送中
  SENDING: 'SENDING',       // 发送中（带进度）
  DELIVERED: 'DELIVERED',   // 已送达
  FAILED: 'FAILED'          // 发送失败
}

/**
 * 消息类型格式化（内部核心逻辑）
 * @param {String} type - 消息类型（大写）
 * @param {String|Object} content - 消息内容
 * @param {Object} options - 选项
 * @param {Boolean} options.showFileName - 是否显示文件名（列表项显示文件名，会话显示[文件]）
 * @returns {String} 格式化后的预览文本
 * @private
 */
function _formatMessageByType(type, content, options = {}) {
    const { showFileName = false } = options

    switch (type) {
        case 'TEXT': {
            const text = String(content)
            return text.length > 30 ? text.substring(0, 30) + '...' : text
        }

        case 'IMAGE':
            return '[图片]'

        case 'FILE':
            try {
                const fileInfo = typeof content === 'string' ? JSON.parse(content) : content
                const fileName = fileInfo.fileName || fileInfo.name || fileInfo.file_name || '未知文件'
                // 会话列表显示文件名，会话预览显示 [文件] 文件名
                return showFileName ? fileName : `[文件] ${fileName}`
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
            try {
                const locationInfo = typeof content === 'string' ? JSON.parse(content) : content
                const address = locationInfo.address || locationInfo.name || '位置'
                return address === '位置' ? '[位置]' : address
            } catch (e) {
                return '[位置]'
            }

        case 'SYSTEM':
            return content || '[系统消息]'

        case 'RECALLED':
            return '[消息已撤回]'

        default:
            // 尝试从内容推断类型
            if (typeof content === 'string') {
                if (content.startsWith('http')) {
                    if (/\.(jpg|jpeg|png|gif|webp)$/i.test(content)) {return '[图片]'}
                    return '[文件]'
                }
                if (content.length <= 100) {return content.length > 30 ? content.substring(0, 30) + '...' : content}
            }
            if (typeof content === 'object') {
                if (content.fileName || content.name) {
                    return showFileName ? (content.fileName || content.name) : `[文件] ${content.fileName || content.name}`
                }
                if (content.imageUrl || content.fileUrl) {return '[图片]'}
            }
            return type ? `[${type}]` : '[消息]'
    }
}

/**
 * 根据消息类型格式化消息预览文本
 * @param {String} type - 消息类型 (TEXT/IMAGE/FILE/VIDEO/VOICE/RECALLED等)
 * @param {String|Object} content - 消息内容
 * @returns {String} 格式化后的预览文本
 */
export function formatMessagePreview(type, content) {
    type = (type || '').toUpperCase()
    if (!content && type !== 'RECALLED') {return '[暂无消息]'}

    return _formatMessageByType(type, content, { showFileName: false })
}

/**
 * 根据消息对象格式化消息预览文本（主要API，支持完整功能）
 * @param {Object|String} message - 消息对象 (ImMessageVO) 或已格式化的字符串
 * @returns {String} 格式化后的预览文本
 * @example
 * formatMessagePreviewFromObject(messageObject)
 * formatMessagePreviewFromObject('[图片]') // 直接返回
 */
export function formatMessagePreviewFromObject(message) {
    if (!message) {return '[空消息]'}

    // 如果已经是字符串（已格式化），直接返回
    if (typeof message === 'string') {
        const str = message.trim()
        if (str.startsWith('[') && str.endsWith(']')) {
            const content = str.slice(1, -1)
            if (['图片', '文件', '视频', '语音', '位置', '系统消息', '空消息', '未知消息'].includes(content)) {
                return str
            }
            return str
        }
        return str
    }

    // 提取消息类型，兼容多种字段名
    const type = (message.type || message.messageType || message.message_type || '').toUpperCase()

    // 检查撤回状态
    if (message.isRevoked === 1 || message.is_revoked === 1 || type === 'RECALLED') {
        if (message.isSelf) {
            return '你撤回了一条消息'
        }
        return `${message.senderName || '对方'}撤回了一条消息`
    }

    const content = message.content

    // 空内容处理
    if (!content) {
        const emptyMessages = { TEXT: '[空消息]', IMAGE: '[图片]', FILE: '[文件]', VIDEO: '[视频]', VOICE: '[语音]', AUDIO: '[语音]' }
        return emptyMessages[type] || (type ? `[${type}]` : '[暂无消息]')
    }

    // 调用核心格式化逻辑（会话列表风格：显示文件名）
    return _formatMessageByType(type, content, { showFileName: true })
}

/**
 * 解析消息内容（处理JSON格式）
 * @param {Object} message - 消息对象
 * @returns {Object|String} 解析后的内容
 */
export function parseMessageContent(message) {
    if (!message || !message.content) {return {}}

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

/**
 * 解析内容字符串（处理JSON格式）
 * @param {string|Object} content - 内容字符串或对象
 * @returns {Object} 解析后的对象
 */
export function parseContentString(content) {
    if (!content) {return {}}

    // 如果已经是对象，直接返回
    if (typeof content === 'object') {return content}

    // 尝试解析JSON字符串
    try {
        return JSON.parse(content)
    } catch (e) {
        return { text: String(content) }
    }
}
