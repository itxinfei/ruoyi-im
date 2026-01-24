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

    // 如果不是文件下载API，直接返回
    if (!url.includes('/api/im/file/download')) {
        return url
    }

    // 如果已经有token参数，直接返回
    if (url.includes('token=')) {
        return url
    }

    // 获取token
    const token = localStorage.getItem('im_token')
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
