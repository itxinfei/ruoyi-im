/**
 * 安全图片加载 Composable
 * 用于替换 addTokenToUrl，通过 HTTP Header 传递 token，避免 token 在 URL 中泄露
 *
 * 使用场景：
 * - 头像显示
 * - 聊天消息中的图片
 * - 任何需要认证的图片资源
 *
 * 安全优势：
 * - Token 通过 HTTP Header 传递，不在 URL 中暴露
 * - 避免在浏览器历史记录、日志、Referer 中泄露 token
 * - 使用 Blob URL 临时存储，自动清理避免内存泄漏
 */

import { ref, watch, onUnmounted } from 'vue'
import { getSecureBlobUrl } from '@/utils/file'

/**
 * 安全图片加载 Hook
 * @param {String} url - 图片URL
 * @param {Object} options - 配置选项
 * @param {Number} options.maxAge - Blob URL 最大缓存时间（毫秒），默认 60000（1分钟）
 * @param {Boolean} options.immediate - 是否立即加载，默认 true
 * @returns {Object} { blobUrl: Ref<String>, loading: Ref<Boolean>, error: Ref<Error|null>, load: Function, clear: Function }
 */
export function useSecureImage(url, options = {}) {
  const { maxAge = 60000, immediate = true } = options

  const blobUrl = ref('')
  const loading = ref(false)
  const error = ref(null)

  // 清理当前的 blob URL
  const clear = () => {
    if (blobUrl.value) {
      try {
        window.URL.revokeObjectURL(blobUrl.value)
      } catch (e) {
        console.warn('清理 Blob URL 失败:', e)
      }
      blobUrl.value = ''
    }
    error.value = null
  }

  // 加载图片
  const load = async () => {
    if (!url) {
      clear()
      return
    }

    // 如果 URL 已经是 blob URL 或 data URL，直接使用
    if (url.startsWith('blob:') || url.startsWith('data:')) {
      blobUrl.value = url
      return
    }

    loading.value = true
    error.value = null

    try {
      // 清理旧的 blob URL
      if (blobUrl.value && blobUrl.value !== url) {
        clear()
      }

      // 使用安全方法获取 blob URL
      const secureUrl = await getSecureBlobUrl(url, { maxAge })
      blobUrl.value = secureUrl
    } catch (err) {
      console.error('加载安全图片失败:', err)
      error.value = err
      // 降级方案：使用原始 URL（虽然不安全，但至少能显示图片）
      // 实际生产环境中，这里应该显示占位图或错误状态
      blobUrl.value = url
    } finally {
      loading.value = false
    }
  }

  // 监听 URL 变化
  watch(() => url, (newUrl, oldUrl) => {
    if (newUrl !== oldUrl) {
      load()
    }
  })

  // 组件卸载时清理 blob URL
  onUnmounted(() => {
    clear()
  })

  // 立即加载
  if (immediate) {
    load()
  }

  return {
    blobUrl,
    loading,
    error,
    load,
    clear
  }
}

/**
 * 批量安全图片加载 Hook
 * @param {Array<String>} urls - 图片URL数组
 * @param {Object} options - 配置选项
 * @returns {Object} { blobUrls: Ref<Array<String>>, loading: Ref<Boolean>, error: Ref<Error|null>, load: Function, clear: Function }
 */
export function useSecureImages(urls, options = {}) {
  const blobUrls = ref([])
  const loading = ref(false)
  const error = ref(null)

  const clear = () => {
    blobUrls.value.forEach(url => {
      if (url && (url.startsWith('blob:') || url.startsWith('data:'))) {
        try {
          window.URL.revokeObjectURL(url)
        } catch (e) {
          console.warn('清理 Blob URL 失败:', e)
        }
      }
    })
    blobUrls.value = []
    error.value = null
  }

  const load = async () => {
    if (!Array.isArray(urls) || urls.length === 0) {
      clear()
      return
    }

    loading.value = true
    error.value = null

    try {
      const results = await Promise.all(
        urls.map(async (url, index) => {
          if (!url) return ''
          if (url.startsWith('blob:') || url.startsWith('data:')) return url

          try {
            return await getSecureBlobUrl(url, options)
          } catch (err) {
            console.error(`加载图片 [${index}] 失败:`, err)
            return url // 降级方案
          }
        })
      )

      blobUrls.value = results
    } catch (err) {
      console.error('批量加载安全图片失败:', err)
      error.value = err
      blobUrls.value = urls // 降级方案
    } finally {
      loading.value = false
    }
  }

  watch(() => urls, (newUrls, oldUrls) => {
    if (JSON.stringify(newUrls) !== JSON.stringify(oldUrls)) {
      load()
    }
  })

  onUnmounted(() => {
    clear()
  })

  load()

  return {
    blobUrls,
    loading,
    error,
    load,
    clear
  }
}