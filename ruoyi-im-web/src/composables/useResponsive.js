/**
 * 响应式工具 Composable
 * 提供窗口尺寸和断点判断功能
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'

export function useResponsive() {
  const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1200)
  
  const updateWidth = () => {
    if (typeof window !== 'undefined') {
      windowWidth.value = window.innerWidth
    }
  }
  
  onMounted(() => {
    window.addEventListener('resize', updateWidth)
  })
  
  onUnmounted(() => {
    window.removeEventListener('resize', updateWidth)
  })
  
  // 断点判断
  const isBreakpointDown = computed(() => {
    return (breakpoint) => {
      const breakpoints = { xs: 480, sm: 576, md: 768, lg: 992, xl: 1200 }
      return windowWidth.value < (breakpoints[breakpoint] || 768)
    }
  })
  
  const isBreakpointUp = computed(() => {
    return (breakpoint) => {
      const breakpoints = { xs: 480, sm: 576, md: 768, lg: 992, xl: 1200 }
      return windowWidth.value >= (breakpoints[breakpoint] || 768)
    }
  })
  
  const getCurrentBreakpoint = computed(() => {
    if (windowWidth.value < 480) return 'xs'
    if (windowWidth.value < 576) return 'sm'
    if (windowWidth.value < 768) return 'md'
    if (windowWidth.value < 992) return 'lg'
    if (windowWidth.value < 1200) return 'xl'
    return 'xxl'
  })
  
  // 常用的响应式值
  const isMobile = computed(() => windowWidth.value < 768)
  const isTablet = computed(() => windowWidth.value >= 768 && windowWidth.value < 992)
  const isDesktop = computed(() => windowWidth.value >= 992)
  
  return {
    windowWidth: computed(() => windowWidth.value),
    isBreakpointDown,
    isBreakpointUp,
    getCurrentBreakpoint,
    isMobile,
    isTablet,
    isDesktop
  }
}