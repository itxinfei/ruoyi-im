/**
 * 防抖函数
 * @param {Function} fn - 需要防抖的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function useDebounceFn(fn, delay = 300) {
  let timer = null
  let lastCallTime = 0

  return function (...args) {
    const now = Date.now()
    const timeSinceLastCall = now - lastCallTime

    // 清除之前的定时器
    if (timer) {
      clearTimeout(timer)
    }

    // 如果距离上次调用时间超过延迟时间，立即执行
    if (timeSinceLastCall >= delay) {
      lastCallTime = now
      return fn.apply(this, args)
    }

    // 否则设置新的定时器
    timer = setTimeout(() => {
      lastCallTime = Date.now()
      fn.apply(this, args)
      timer = null
    }, delay - timeSinceLastCall)
  }
}

/**
 * Vue 3 组合式API风格的防抖
 * @param {Function} fn - 需要防抖的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function useDebounce(fn, delay = 300) {
  return useDebounceFn(fn, delay)
}

/**
 * 可取消的防抖
 * @param {Function} fn - 需要防抖的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Object} 包含防抖函数和取消函数的对象
 */
export function useDebounceWithCancel(fn, delay = 300) {
  let timer = null

  const debounced = function (...args) {
    if (timer) {
      clearTimeout(timer)
    }

    timer = setTimeout(() => {
      fn.apply(this, args)
      timer = null
    }, delay)
  }

  const cancel = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  const flush = () => {
    if (timer) {
      clearTimeout(timer)
      fn.apply(this, [])
      timer = null
    }
  }

  return { debounced, cancel, flush }
}

/**
 * 立即执行的防抖
 * 第一次调用立即执行，之后的调用才会防抖
 * @param {Function} fn - 需要防抖的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function useDebounceImmediate(fn, delay = 300) {
  let timer = null
  let isFirstCall = true

  return function (...args) {
    if (isFirstCall) {
      isFirstCall = false
      return fn.apply(this, args)
    }

    if (timer) {
      clearTimeout(timer)
    }

    timer = setTimeout(() => {
      fn.apply(this, args)
      timer = null
    }, delay)
  }
}

/**
 * 带最大等待时间的防抖
 * @param {Function} fn - 需要防抖的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @param {number} maxWait - 最大等待时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function useDebounceMaxWait(fn, delay = 300, maxWait = 1000) {
  let timer = null
  let maxTimer = null
  let lastCallTime = 0

  return function (...args) {
    const now = Date.now()
    const timeSinceLastCall = now - lastCallTime

    if (timer) {
      clearTimeout(timer)
    }
    if (maxTimer) {
      clearTimeout(maxTimer)
    }

    // 如果超过最大等待时间，立即执行
    if (timeSinceLastCall >= maxWait) {
      lastCallTime = now
      return fn.apply(this, args)
    }

    // 设置正常防抖定时器
    timer = setTimeout(() => {
      lastCallTime = Date.now()
      fn.apply(this, args)
      if (maxTimer) {
        clearTimeout(maxTimer)
        maxTimer = null
      }
      timer = null
    }, delay)

    // 设置最大等待定时器
    maxTimer = setTimeout(() => {
      lastCallTime = Date.now()
      fn.apply(this, args)
      if (timer) {
        clearTimeout(timer)
        timer = null
      }
      maxTimer = null
    }, maxWait - timeSinceLastCall)
  }
}

/**
 * 多个防抖函数管理器
 */
export class DebounceManager {
  constructor() {
    this.debounces = new Map()
  }

  /**
   * 注册防抖函数
   * @param {string} key - 唯一标识
   * @param {Function} fn - 需要防抖的函数
   * @param {number} delay - 延迟时间（毫秒）
   * @returns {Function} 防抖后的函数
   */
  register(key, fn, delay = 300) {
    const debounced = useDebounceFn(fn, delay)
    this.debounces.set(key, { fn: debounced, timer: null })
    return debounced
  }

  /**
   * 执行防抖函数
   * @param {string} key - 唯一标识
   * @param {Array} args - 参数
   */
  execute(key, ...args) {
    const debounce = this.debounces.get(key)
    if (debounce) {
      return debounce.fn.apply(this, args)
    }
  }

  /**
   * 取消防抖函数
   * @param {string} key - 唯一标识
   */
  cancel(key) {
    const debounce = this.debounces.get(key)
    if (debounce && debounce.timer) {
      clearTimeout(debounce.timer)
      debounce.timer = null
    }
  }

  /**
   * 清除所有防抖函数
   */
  clearAll() {
    this.debounces.forEach(debounce => {
      if (debounce.timer) {
        clearTimeout(debounce.timer)
      }
    })
    this.debounces.clear()
  }
}

// 导出单例
export const debounceManager = new DebounceManager()
