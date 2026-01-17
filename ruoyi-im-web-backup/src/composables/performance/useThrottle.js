/**
 * 节流函数
 * @param {Function} fn - 需要节流的函数
 * @param {number} interval - 间隔时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function useThrottleFn(fn, interval = 300) {
  let lastTime = 0
  let timer = null

  return function (...args) {
    const now = Date.now()
    const remaining = interval - (now - lastTime)

    if (remaining <= 0) {
      // 可以立即执行
      if (timer) {
        clearTimeout(timer)
        timer = null
      }
      lastTime = now
      return fn.apply(this, args)
    } else if (!timer) {
      // 设置定时器
      timer = setTimeout(() => {
        lastTime = Date.now()
        timer = null
        fn.apply(this, args)
      }, remaining)
    }
  }
}

/**
 * Vue 3 组合式API风格的节流
 * @param {Function} fn - 需要节流的函数
 * @param {number} interval - 间隔时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function useThrottle(fn, interval = 300) {
  return useThrottleFn(fn, interval)
}

/**
 * 可取消的节流
 * @param {Function} fn - 需要节流的函数
 * @param {number} interval - 间隔时间（毫秒）
 * @returns {Object} 包含节流函数和取消函数的对象
 */
export function useThrottleWithCancel(fn, interval = 300) {
  let timer = null
  let lastTime = 0

  const throttled = function (...args) {
    const now = Date.now()
    const remaining = interval - (now - lastTime)

    if (remaining <= 0) {
      if (timer) {
        clearTimeout(timer)
        timer = null
      }
      lastTime = now
      return fn.apply(this, args)
    } else if (!timer) {
      timer = setTimeout(() => {
        lastTime = Date.now()
        timer = null
        fn.apply(this, args)
      }, remaining)
    }
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
      lastTime = Date.now()
      fn.apply(this, [])
      timer = null
    }
  }

  return { throttled, cancel, flush }
}

/**
 * 首次立即执行的节流
 * @param {Function} fn - 需要节流的函数
 * @param {number} interval - 间隔时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function useThrottleLeading(fn, interval = 300) {
  let lastTime = 0

  return function (...args) {
    const now = Date.now()
    if (now - lastTime >= interval) {
      lastTime = now
      return fn.apply(this, args)
    }
  }
}

/**
 * 最后才执行的节流
 * @param {Function} fn - 需要节流的函数
 * @param {number} interval - 间隔时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function useThrottleTrailing(fn, interval = 300) {
  let timer = null

  return function (...args) {
    if (timer) {
      return
    }

    timer = setTimeout(() => {
      fn.apply(this, args)
      timer = null
    }, interval)
  }
}

/**
 * RAF节流（基于requestAnimationFrame）
 * 适合动画场景
 * @param {Function} fn - 需要节流的函数
 * @returns {Function} 节流后的函数
 */
export function useRAFThrottle(fn) {
  let rafId = null

  return function (...args) {
    if (rafId !== null) {
      return
    }

    rafId = requestAnimationFrame(() => {
      fn.apply(this, args)
      rafId = null
    })
  }
}

/**
 * 取消RAF节流
 * @param {Function} throttled - RAF节流函数
 */
export function cancelRAFThrottle(throttled) {
  if (throttled && throttled.rafId !== null) {
    cancelAnimationFrame(throttled.rafId)
    throttled.rafId = null
  }
}

/**
 * 节流函数管理器
 */
export class ThrottleManager {
  constructor() {
    this.throttles = new Map()
  }

  /**
   * 注册节流函数
   * @param {string} key - 唯一标识
   * @param {Function} fn - 需要节流的函数
   * @param {number} interval - 间隔时间（毫秒）
   * @returns {Function} 节流后的函数
   */
  register(key, fn, interval = 300) {
    const throttled = useThrottleFn(fn, interval)
    this.throttles.set(key, { fn: throttled, timer: null })
    return throttled
  }

  /**
   * 执行节流函数
   * @param {string} key - 唯一标识
   * @param {Array} args - 参数
   */
  execute(key, ...args) {
    const throttle = this.throttles.get(key)
    if (throttle) {
      return throttle.fn.apply(this, args)
    }
  }

  /**
   * 取消节流函数
   * @param {string} key - 唯一标识
   */
  cancel(key) {
    const throttle = this.throttles.get(key)
    if (throttle && throttle.timer) {
      clearTimeout(throttle.timer)
      throttle.timer = null
    }
  }

  /**
   * 清除所有节流函数
   */
  clearAll() {
    this.throttles.forEach(throttle => {
      if (throttle.timer) {
        clearTimeout(throttle.timer)
      }
    })
    this.throttles.clear()
  }
}

// 导出单例
export const throttleManager = new ThrottleManager()
