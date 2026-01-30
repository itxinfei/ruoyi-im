/**
 * 文件URL处理工具函数
 */

/**
 * 为文件URL添加认证token
 * @param {String} url - 原始URL
 * @returns {String} 带token的URL
 */
export function addTokenToUrl(url) {
    if (!url) return ''

    // 如果是完整的http/https URL且不是本域名，直接返回（外部资源）
    if (url.startsWith('http://') || url.startsWith('https://')) {
        try {
            const urlObj = new URL(url)
            // 如果是外部域名，直接返回
            if (!urlObj.hostname.includes(window.location.hostname)) {
                return url
            }
        } catch (e) {
            return url
        }
    }

    // 如果已经有token参数，直接返回
    if (url.includes('token=')) {
        return url
    }

    // 获取token
    const { getToken } = require('./storage')
    const token = getToken()
    if (!token) {
        console.warn('未找到token，无法添加到URL')
        return url
    }

    // 添加token参数
    const separator = url.includes('?') ? '&' : '?'
    return `${url}${separator}token=${token}`
}

/**
 * 批量为URL列表添加token
 * @param {Array} urls - URL数组
 * @returns {Array} 带token的URL数组
 */
export function addTokenToUrls(urls) {
    if (!Array.isArray(urls)) return []
    return urls.map(url => addTokenToUrl(url))
}

/**
 * 获取安全的头像URL（自动添加token）
 * @param {String} avatarUrl - 头像URL
 * @returns {String} 安全的头像URL
 */
export function getSecureAvatarUrl(avatarUrl) {
    return addTokenToUrl(avatarUrl)
}

/**
 * 获取文件下载URL（带token）
 * @param {String} fileUrl - 文件URL
 * @returns {String} 带token的文件URL
 */
export function getSecureFileUrl(fileUrl) {
    return addTokenToUrl(fileUrl)
}

// ============================================================================
// URL 链接解析工具
// ============================================================================

/**
 * URL 正则表达式
 * 匹配 http/https 协议的 URL
 */
const URL_REGEX = /(https?:\/\/[^\s<>{}|^`[\]\\]+)/g

/**
 * 从文本中提取所有 URL
 * @param {string} text - 要解析的文本
 * @returns {Array<string>} - 提取到的 URL 数组
 */
export function extractUrls(text) {
    if (!text || typeof text !== 'string') return []

    const urls = []
    let match

    // 重置正则表达式的 lastIndex
    URL_REGEX.lastIndex = 0

    while ((match = URL_REGEX.exec(text)) !== null) {
        urls.push(match[1])
    }

    return urls
}

/**
 * 判断文本是否包含 URL
 * @param {string} text - 要检查的文本
 * @returns {boolean} - 是否包含 URL
 */
export function hasUrl(text) {
    if (!text || typeof text !== 'string') return false
    URL_REGEX.lastIndex = 0
    return URL_REGEX.test(text)
}

/**
 * 从消息内容中提取链接信息
 * @param {Object|string} content - 消息内容（可能是字符串或 JSON 对象）
 * @returns {Array<Object>} - 链接信息数组
 */
export function extractLinksFromContent(content) {
    let text = ''

    if (typeof content === 'string') {
        // 尝试解析 JSON
        try {
            const parsed = JSON.parse(content)
            if (parsed.links) {
                return parsed.links
            }
            text = parsed.text || parsed.content || content
        } catch {
            text = content
        }
    } else if (typeof content === 'object') {
        if (content.links) {
            return content.links
        }
        text = content.text || content.content || ''
    }

    // 从文本中提取 URL
    const urls = extractUrls(text)
    return urls.map(url => ({
        url,
        title: '',
        description: '',
        image: ''
    }))
}

/**
 * 格式化链接显示的 URL
 * @param {string} url - 原始 URL
 * @returns {string} - 格式化后的 URL
 */
export function formatLinkUrl(url) {
    try {
        const urlObj = new URL(url)
        let display = urlObj.hostname
        if (urlObj.pathname && urlObj.pathname !== '/') {
            display += urlObj.pathname.substring(0, 30)
            if (urlObj.pathname.length > 30) {
                display += '...'
            }
        }
        return display
    } catch {
        return url.length > 40 ? url.substring(0, 40) + '...' : url
    }
}

/**
 * 获取链接的域名
 * @param {string} url - URL
 * @returns {string} - 域名
 */
export function getDomainFromUrl(url) {
    try {
        return new URL(url).hostname
    } catch {
        return ''
    }
}
