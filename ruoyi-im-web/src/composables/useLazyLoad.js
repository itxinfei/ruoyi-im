/**
 * 图片懒加载 Composable
 * 使用 IntersectionObserver 实现图片的懒加载
 *
 * @author ruoyi
 */
import { ref, onMounted, onUnmounted, markRaw } from 'vue'

/**
 * 图片懒加载 Hook
 * @param {Object} options - 配置选项
 * @param {string} [options.placeholder] - 占位图片 URL
 * @param {string} [options.errorPlaceholder] - 错误占位图片 URL
 * @param {number} [options.threshold=0.01] - 触发加载的阈值 (0-1)
 * @param {string} [options.rootMargin='0px'] - 根边距
 * @returns {Object} 懒加载相关方法和状态
 */
export function useLazyLoad(options = {}) {
  const {
    placeholder = '',
    errorPlaceholder = '',
    threshold = 0.01,
    rootMargin = '50px'
  } = options

  const observer = ref(null)
  const loading = ref(new Map()) // 正在加载的图片
  const loaded = ref(new Map()) // 已加载的图片
  const error = ref(new Map()) // 加载失败的图片

  /**
   * 创建图片懒加载指令
   * @returns {Object} Vue 指令对象
   */
  const createLazyLoadDirective = () => {
    return {
      mounted(el, binding) {
        const imageUrl = binding.value

        // 如果已经加载过，直接设置
        if (loaded.value.has(imageUrl)) {
          el.src = imageUrl
          return
        }

        // 如果加载失败过，使用错误占位图
        if (error.value.has(imageUrl)) {
          el.src = errorPlaceholder || placeholder
          return
        }

        // 设置占位图
        if (placeholder) {
          el.src = placeholder
        }

        // 创建 IntersectionObserver
        if (!observer.value) {
          observer.value = markRaw(new IntersectionObserver(
            (entries) => {
              entries.forEach(entry => {
                if (entry.isIntersecting) {
                  const img = entry.target
                  const src = img.dataset.src

                  if (src && !loading.value.has(src) && !loaded.value.has(src)) {
                    loading.value.set(src, true)

                    // 预加载图片
                    const tempImg = new Image()
                    tempImg.onload = () => {
                      img.src = src
                      loaded.value.set(src, true)
                      loading.value.delete(src)
                      // 移除 data-src 属性
                      delete img.dataset.src
                    }
                    tempImg.onerror = () => {
                      error.value.set(src, true)
                      loading.value.delete(src)
                      if (errorPlaceholder) {
                        img.src = errorPlaceholder
                      }
                    }
                    tempImg.src = src

                    // 停止观察该元素
                    observer.value?.unobserve(img)
                  }
                }
              })
            },
            { threshold, rootMargin }
          ))
        }

        // 存储 URL 到 dataset 中
        el.dataset.src = imageUrl
        observer.value?.observe(el)
      },
      unmounted(el) {
        observer.value?.unobserve(el)
      }
    }
  }

  /**
   * 预加载图片
   * @param {string} url - 图片 URL
   * @returns {Promise<boolean>} 是否加载成功
   */
  const preloadImage = (url) => {
    return new Promise((resolve) => {
      if (loaded.value.has(url)) {
        resolve(true)
        return
      }

      if (error.value.has(url)) {
        resolve(false)
        return
      }

      const img = new Image()
      img.onload = () => {
        loaded.value.set(url, true)
        resolve(true)
      }
      img.onerror = () => {
        error.value.set(url, true)
        resolve(false)
      }
      img.src = url
    })
  }

  /**
   * 批量预加载图片
   * @param {Array<string>} urls - 图片 URL 数组
   * @param {number} [concurrency=3] - 并发数
   * @returns {Promise<Array<boolean>>} 加载结果数组
   */
  const preloadImages = async (urls, concurrency = 3) => {
    const results = []
    for (let i = 0; i < urls.length; i += concurrency) {
      const batch = urls.slice(i, i + concurrency)
      const batchResults = await Promise.all(
        batch.map(url => preloadImage(url))
      )
      results.push(...batchResults)
    }
    return results
  }

  /**
   * 清理状态
   */
  const clear = () => {
    loading.value.clear()
    // 不清除 loaded 和 error，因为它们是缓存
  }

  /**
   * 重置所有状态
   */
  const reset = () => {
    loading.value.clear()
    loaded.value.clear()
    error.value.clear()
  }

  // 清理 observer
  onUnmounted(() => {
    if (observer.value) {
      observer.value.disconnect()
      observer.value = null
    }
  })

  return {
    loading,
    loaded,
    error,
    createLazyLoadDirective,
    preloadImage,
    preloadImages,
    clear,
    reset
  }
}

/**
 * Vue 指令：图片懒加载
 * 使用方式：v-lazy-load="imageUrl"
 */
export const lazyLoadDirective = (options = {}) => {
  const { createLazyLoadDirective } = useLazyLoad(options)
  return createLazyLoadDirective()
}

/**
 * 简化版 Hook：直接在组件中使用
 * @returns {Object} 包含 vLazyLoad 指令的对象
 */
export function useImageLazyLoad(options) {
  const { createLazyLoadDirective } = useLazyLoad(options)
  return {
    vLazyLoad: createLazyLoadDirective()
  }
}
