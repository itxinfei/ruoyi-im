const breakpoints = {
  xs: 0,
  sm: 576,
  md: 768,
  lg: 992,
  xl: 1200,
  xxl: 1600
}

let resizeObserver = null
const listeners = new Set()

function getBreakpoint() {
  const width = window.innerWidth
  if (width < breakpoints.sm) return 'xs'
  if (width < breakpoints.md) return 'sm'
  if (width < breakpoints.lg) return 'md'
  if (width < breakpoints.xl) return 'lg'
  if (width < breakpoints.xxl) return 'xl'
  return 'xxl'
}

function isMobile() {
  return window.innerWidth < breakpoints.md
}

function isTablet() {
  const width = window.innerWidth
  return width >= breakpoints.md && width < breakpoints.lg
}

function isDesktop() {
  return window.innerWidth >= breakpoints.lg
}

function addResizeListener(callback) {
  listeners.add(callback)
  
  if (!resizeObserver) {
    resizeObserver = new ResizeObserver(() => {
      const breakpoint = getBreakpoint()
      listeners.forEach(listener => listener(breakpoint))
    })
    resizeObserver.observe(document.body)
  }
  
  return () => removeResizeListener(callback)
}

function removeResizeListener(callback) {
  listeners.delete(callback)
  
  if (listeners.size === 0 && resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
}

function getViewport() {
  return {
    width: window.innerWidth,
    height: window.innerHeight,
    breakpoint: getBreakpoint(),
    isMobile: isMobile(),
    isTablet: isTablet(),
    isDesktop: isDesktop()
  }
}

function debounce(func, wait = 100) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

function throttle(func, limit = 100) {
  let inThrottle
  return function executedFunction(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

export {
  breakpoints,
  getBreakpoint,
  isMobile,
  isTablet,
  isDesktop,
  addResizeListener,
  removeResizeListener,
  getViewport,
  debounce,
  throttle
}

export default {
  breakpoints,
  getBreakpoint,
  isMobile,
  isTablet,
  isDesktop,
  addResizeListener,
  removeResizeListener,
  getViewport,
  debounce,
  throttle
}
