// 响应式断点配置
export const breakpoints = {
  xs: 480,
  sm: 576,
  md: 768,
  lg: 992,
  xl: 1200,
  xxl: 1600
}

// 设计系统常量
export const designSystem = {
  // 基础间距单位（8px网格系统）
  spacing: {
    xs: 4,
    sm: 8,
    md: 16,
    lg: 24,
    xl: 32,
    xxl: 48
  },
  
  // 圆角
  borderRadius: {
    sm: 4,
    md: 6,
    lg: 8,
    xl: 12
  },
  
  // 阴影
  shadows: {
    sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    md: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
    lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1)',
    xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1)'
  },
  
  // 过渡动画
  transitions: {
    fast: '0.15s ease',
    normal: '0.3s ease',
    slow: '0.5s ease'
  },
  
  // 字体大小
  fontSizes: {
    xs: 12,
    sm: 13,
    base: 14,
    md: 16,
    lg: 18,
    xl: 20,
    xxl: 24
  },
  
  // 行高
  lineHeights: {
    tight: 1.25,
    normal: 1.5,
    relaxed: 1.75
  },
  
  // 组件高度
  componentHeight: {
    small: 32,
    medium: 36,
    large: 40
  }
}

// 响应式工具函数
export const useResponsive = () => {
  // 检查屏幕宽度
  const getScreenWidth = () => {
    if (typeof window !== 'undefined') {
      return window.innerWidth
    }
    return 1200 // 默认值
  }
  
  // 检查是否小于断点
  const isBreakpointDown = (bp) => {
    return getScreenWidth() < breakpoints[bp]
  }
  
  // 检查是否大于断点
  const isBreakpointUp = (bp) => {
    return getScreenWidth() >= breakpoints[bp]
  }
  
  // 获取当前断点
  const getCurrentBreakpoint = () => {
    const width = getScreenWidth()
    if (width < breakpoints.xs) return 'xs'
    if (width < breakpoints.sm) return 'sm'
    if (width < breakpoints.md) return 'md'
    if (width < breakpoints.lg) return 'lg'
    if (width < breakpoints.xl) return 'xl'
    return 'xxl'
  }
  
  // 响应式值
  const responsiveValue = (values) => {
    const bp = getCurrentBreakpoint()
    return values[bp] || values.md || values.lg
  }
  
  return {
    breakpoints,
    getScreenWidth,
    isBreakpointDown,
    isBreakpointUp,
    getCurrentBreakpoint,
    responsiveValue
  }
}

// 通用响应式样式 Mixin（用于 SCSS）
export const responsiveMixin = `
  @mixin respond-to($breakpoint) {
    @media (max-width: #{map-get($breakpoints, $breakpoint)}px) {
      @content;
    }
  }
  
  @mixin respond-to-up($breakpoint) {
    @media (min-width: #{map-get($breakpoints, $breakpoint + 1)}px) {
      @content;
    }
  }
  
  // 使用示例
  // .element {
  //   width: 300px;
  //   @include respond-to(md) {
  //     width: 100%;
  //   }
  // }
`