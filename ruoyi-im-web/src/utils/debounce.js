/**
 * 防抖和节流工具函数
 * 用于优化频繁触发的事件处理
 */

/**
 * 防抖函数
 * 在事件被触发 n 秒后再执行回调，如果在这 n 秒内又被触发，则重新计时
 *
 * @param {Function} fn - 需要防抖的函数
 * @param {Number} delay - 延迟时间（毫秒）
 * @param {Boolean} immediate - 是否立即执行
 * @returns {Function} 防抖后的函数
 *
 * @example
 * const debouncedSearch = useDebounce((value) => {
 *   console.log('搜索:', value)
 * }, 300)
 *
 * watch(searchKeyword, (val) => debouncedSearch(val))
 */
export function debounce(fn, delay = 300, immediate = false) {
  let timer = null

  function debounced(...args) {
    const context = this

    // 清除之前的定时器
    if (timer) {
      clearTimeout(timer)
    }

    // 立即执行模式
    if (immediate) {
      // 如果已经执行过，不再执行
      const callNow = !timer
      timer = setTimeout(() => {
        timer = null
      }, delay)
      if (callNow) {
        fn.apply(context, args)
      }
    } else {
      // 延迟执行
      timer = setTimeout(() => {
        fn.apply(context, args)
      }, delay)
    }
  }

  // 取消防抖
  debounced.cancel = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  return debounced
}

/**
 * 节流函数
 * 规定在一个单位时间内，只能触发一次函数。如果这个单位时间内触发多次函数，只有一次生效
 *
 * @param {Function} fn - 需要节流的函数
 * @param {Number} delay - 延迟时间（毫秒）
 * @param {Object} options - 配置选项
 * @param {Boolean} options.leading - 是否在开始时执行
 * @param {Boolean} options.trailing - 是否在结束时执行
 * @returns {Function} 节流后的函数
 *
 * @example
 * const throttledScroll = useThrottle(() => {
 *   console.log('滚动位置:', window.scrollY)
 * }, 100)
 *
 * window.addEventListener('scroll', throttledScroll)
 */
export function throttle(fn, delay = 300, options = {}) {
  const { leading = true, trailing = true } = options
  let timer = null
  let lastTime = 0

  function throttled(...args) {
    const context = this
    const now = Date.now()

    // 如果是第一次执行且 leading 为 true，立即执行
    if (leading && !lastTime) {
      fn.apply(context, args)
      lastTime = now
      return
    }

    // 计算剩余时间
    const remaining = delay - (now - lastTime)

    if (remaining <= 0) {
      // 时间到了，执行函数
      if (timer) {
        clearTimeout(timer)
        timer = null
      }
      fn.apply(context, args)
      lastTime = now
    } else if (trailing && !timer) {
      // 设置定时器在结束时执行
      timer = setTimeout(() => {
        fn.apply(context, args)
        lastTime = Date.now()
        timer = null
      }, remaining)
    }
  }

  // 取消节流
  throttled.cancel = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
    lastTime = 0
  }

  return throttled
}

/**
 * Vue 3 Composition API 防抖 Hook
 *
 * @param {Function} fn - 需要防抖的函数
 * @param {Number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 *
 * @example
 * const { ref, watch } = Vue
 * const keyword = ref('')
 *
 * const debouncedSearch = useDebounceFn((val) => {
 *   console.log('搜索:', val)
 * }, 300)
 *
 * watch(keyword, debouncedSearch)
 */
export function useDebounceFn(fn, delay = 300) {
  return debounce(fn, delay)
}

/**
 * Vue 3 Composition API 节流 Hook
 *
 * @param {Function} fn - 需要节流的函数
 * @param {Number} delay - 延迟时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function useThrottleFn(fn, delay = 300) {
  return throttle(fn, delay)
}

/**
 * 创建防抖 ref
 * 当 ref 的值改变时，延迟执行回调
 *
 * @param {Function} callback - 回调函数
 * @param {Number} delay - 延迟时间（毫秒）
 * @returns {Object} { debouncedRef, flush, cancel }
 *
 * @example
 * const { debouncedRef, flush } = useDebounceRef('', 300)
 * debouncedRef.value = 'test' // 300ms 后才会更新
 * flush() // 立即更新
 */
export function useDebounceRef(initialValue, delay = 300) {
  let timer = null
  const innerValue = ref(initialValue)
  const debouncedValue = ref(initialValue)

  const flush = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
    debouncedValue.value = innerValue.value
  }

  const cancel = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  watch(innerValue, (newValue) => {
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(() => {
      debouncedValue.value = newValue
    }, delay)
  })

  return {
    get value() {
      return innerValue.value
    },
    set value(newValue) {
      innerValue.value = newValue
    },
    debouncedRef: debouncedValue,
    flush,
    cancel
  }
}
