import { ref, computed } from 'vue'

/**
 * 链接预览 Composable
 * 提取文本中的 URL 并获取网页元数据
 */
export function useLinkPreview() {
  const previews = ref(new Map()) // url -> metadata
  const loading = ref(new Set())
  const errors = ref(new Set())

  // URL 正则表达式 - 匹配 http/https 链接
  const URL_REGEX = /https?:\/\/[^\s<>"{}|\\^`[\]]+/gi

  /**
   * 从文本中提取所有 URL
   */
  const extractUrls = text => {
    if (!text) {return []}
    const matches = text.match(URL_REGEX) || []
    // 去重并过滤无效 URL
    return [...new Set(matches)].filter(url => {
      try {
        new URL(url)
        return true
      } catch {
        return false
      }
    })
  }

  /**
   * 检查是否已缓存预览数据
   */
  const hasPreview = url => previews.value.has(url)

  /**
   * 获取预览数据
   */
  const getPreview = url => previews.value.get(url)

  /**
   * 获取网站的 favicon
   */
  const getFavicon = url => {
    try {
      const urlObj = new URL(url)
      return `https://www.google.com/s2/favicons?domain=${urlObj.hostname}&sz=64`
    } catch {
      return ''
    }
  }

  /**
   * 获取默认的预览数据（基于 URL 信息）
   */
  const getDefaultPreview = url => {
    try {
      const urlObj = new URL(url)
      return {
        url,
        title: urlObj.hostname,
        description: '暂无描述信息',
        image: '',
        favicon: getFavicon(url),
        siteName: urlObj.hostname,
        isDefault: true
      }
    } catch {
      return {
        url,
        title: url,
        description: '暂无描述信息',
        image: '',
        favicon: '',
        siteName: '',
        isDefault: true
      }
    }
  }

  /**
   * 获取链接预览元数据
   * 使用后端代理获取（避免 CORS 问题）
   */
  const fetchPreview = async url => {
    // 如果已加载或正在加载，跳过
    if (hasPreview(url) || loading.value.has(url)) {
      return getPreview(url)
    }

    loading.value.add(url)
    errors.value.delete(url)

    try {
      // 调用后端 API 获取元数据
      // 后端接口: POST /api/im/link/preview
      const response = await fetch('/api/im/link/preview', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        },
        body: JSON.stringify({ url })
      })

      if (response.ok) {
        const data = await response.json()
        if (data.code === 200 && data.data) {
          const preview = {
            url,
            title: data.data.title || data.data.siteName || new URL(url).hostname,
            description: data.data.description || '暂无描述信息',
            image: data.data.image || '',
            favicon: data.data.favicon || getFavicon(url),
            siteName: data.data.siteName || new URL(url).hostname,
            isDefault: false
          }
          previews.value.set(url, preview)
          return preview
        }
      }

      // 如果后端 API 不可用，使用默认预览
      throw new Error('Failed to fetch preview')
    } catch (err) {
      // 使用默认预览作为 fallback
      const defaultPreview = getDefaultPreview(url)
      previews.value.set(url, defaultPreview)
      errors.value.add(url)
      return defaultPreview
    } finally {
      loading.value.delete(url)
    }
  }

  /**
   * 批量获取链接预览
   */
  const fetchPreviews = async urls => {
    const promises = urls.map(url => fetchPreview(url))
    return Promise.allSettled(promises)
  }

  /**
   * 清除缓存
   */
  const clearCache = () => {
    previews.value.clear()
    loading.value.clear()
    errors.value.clear()
  }

  return {
    // 状态
    previews: computed(() => new Map(previews.value)),
    loading: computed(() => new Set(loading.value)),
    errors: computed(() => new Set(errors.value)),
    
    // 方法
    extractUrls,
    hasPreview,
    getPreview,
    fetchPreview,
    fetchPreviews,
    getFavicon,
    getDefaultPreview,
    clearCache
  }
}

export default useLinkPreview
