/**
 * 文件URL处理工具函数
 *
 * 安全警告：
 * 1. ⚠️ 将 token 添加到 URL 参数中存在严重安全风险
 * 2. ⚠️ 容易在日志、浏览器历史记录、Referer 中泄露
 * 3. ⚠️ 容易被第三方网站获取
 *
 * 推荐的安全方案：
 * - 图片显示：使用 useSecureImage composable (src/composables/useSecureImage.js)
 * - 文件下载：使用 fetchSecureFile 函数
 * - 获取 Blob URL：使用 getSecureBlobUrl 函数
 *
 * 所有安全方案都通过 HTTP Header 传递 token，避免在 URL 中暴露
 */
import tokenManager from '@/utils/tokenManager'

/**
 * ⚠️ 已弃用：为文件URL添加认证token
 *
 * 严重安全风险：
 * - Token 在 URL 中传递容易泄露到日志、历史记录、Referer
 * - 可能被第三方网站通过 Referer 获取
 * - 不符合安全最佳实践
 *
 * 请使用以下安全方法替代：
 * - 图片显示：useSecureImage (src/composables/useSecureImage.js)
 * - 文件下载：fetchSecureFile
 * - Blob URL：getSecureBlobUrl
 *
 * @deprecated 安全风险：请使用 useSecureImage、fetchSecureFile 或 getSecureBlobUrl
 * @param {String} url - 原始URL
 * @returns {String} 带token的URL（不安全）
 */
export function addTokenToUrl(url) {
    // 触发控制台警告
    console.warn(
        '[安全警告] addTokenToUrl 已弃用！Token 在 URL 中传递存在安全风险。\n' +
        '请使用以下安全方法替代：\n' +
        '- 图片显示：useSecureImage (src/composables/useSecureImage.js)\n' +
        '- 文件下载：fetchSecureFile\n' +
        '- Blob URL：getSecureBlobUrl'
    )

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
    const token = tokenManager.getToken()
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
 * @deprecated 安全风险：Token 在 URL 中传递容易泄露。建议使用 fetchSecureFile 或 getSecureBlobUrl
 * @param {Array} urls - URL数组
 * @returns {Array} 带token的URL数组
 */
export function addTokenToUrls(urls) {
    if (!Array.isArray(urls)) return []
    return urls.map(url => addTokenToUrl(url))
}

/**
 * 获取安全的头像URL（自动添加token）
 * @deprecated 安全风险：Token 在 URL 中传递容易泄露。建议使用 getSecureBlobUrl
 * @param {String} avatarUrl - 头像URL
 * @returns {String} 安全的头像URL
 */
export function getSecureAvatarUrl(avatarUrl) {
    return addTokenToUrl(avatarUrl)
}

/**
 * 获取文件下载URL（带token）
 * @deprecated 安全风险：Token 在 URL 中传递容易泄露。建议使用 fetchSecureFile
 * @param {String} fileUrl - 文件URL
 * @returns {String} 带token的文件URL
 */
export function getSecureFileUrl(fileUrl) {
    return addTokenToUrl(fileUrl)
}

/**
 * 安全地下载文件（通过 HTTP Header 传递 token）
 * @param {String} url - 文件URL
 * @param {String} filename - 下载的文件名（可选）
 * @returns {Promise<void>} 下载完成Promise
 */
export async function fetchSecureFile(url, filename = null) {
    try {
        const token = tokenManager.getToken()
        if (!token) {
            throw new Error('未找到认证令牌')
        }

        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })

        if (!response.ok) {
            throw new Error(`下载失败: ${response.status}`)
        }

        // 从响应头或 URL 中获取文件名
        let downloadFilename = filename
        if (!downloadFilename) {
            const contentDisposition = response.headers.get('Content-Disposition')
            if (contentDisposition) {
                const match = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
                if (match && match[1]) {
                    downloadFilename = match[1].replace(/['"]/g, '')
                }
            }
        }

        if (!downloadFilename) {
            // 从 URL 中提取文件名
            const urlParts = url.split('/')
            downloadFilename = urlParts[urlParts.length - 1] || 'download'
        }

        // 获取文件内容
        const blob = await response.blob()

        // 创建下载链接
        const downloadUrl = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = downloadUrl
        link.download = downloadFilename
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)

        // 释放内存
        window.URL.revokeObjectURL(downloadUrl)
    } catch (error) {
        console.error('文件下载失败:', error)
        throw error
    }
}

/**
 * 获取安全的 Blob URL（通过 HTTP Header 传递 token）
 * 适用于需要通过 <img> 标签显示的图片，避免 token 在 URL 中泄露
 * @param {String} url - 文件URL
 * @param {Object} options - 配置选项
 * @param {Number} options.maxAge - Blob URL 最大缓存时间（毫秒），默认 60000（1分钟）
 * @returns {Promise<String>} Blob URL
 */
export async function getSecureBlobUrl(url, options = {}) {
    const { maxAge = 60000 } = options

    try {
        const token = tokenManager.getToken()
        if (!token) {
            throw new Error('未找到认证令牌')
        }

        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })

        if (!response.ok) {
            throw new Error(`获取文件失败: ${response.status}`)
        }

        const blob = await response.blob()
        const blobUrl = window.URL.createObjectURL(blob)

        // 设置自动清理，避免内存泄漏
        setTimeout(() => {
            window.URL.revokeObjectURL(blobUrl)
        }, maxAge)

        return blobUrl
    } catch (error) {
        console.error('获取 Blob URL 失败:', error)
        throw error
    }
}

/**
 * 批量获取安全的 Blob URLs
 * @param {Array<String>} urls - URL数组
 * @param {Object} options - 配置选项
 * @returns {Promise<Array<String>>} Blob URL数组
 */
export async function getSecureBlobUrls(urls, options = {}) {
    if (!Array.isArray(urls)) return []

    const promises = urls.map(url => getSecureBlobUrl(url, options))
    return Promise.all(promises)
}
