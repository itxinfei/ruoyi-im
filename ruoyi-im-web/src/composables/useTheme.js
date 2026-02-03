// ============================================================================
// useTheme.js
// 主题管理组合式函数
// 支持浅色/深色/自动模式，使用 data-theme 属性切换
// ============================================================================

import { ref, onMounted, watch } from 'vue'
import { getItem, setItem } from '@/utils/storage'

const THEME_KEY = 'im_admin_theme'
const THEME_LIGHT = 'light'
const THEME_DARK = 'dark'
const THEME_AUTO = 'auto'

// 全局响应式主题状态（单例模式）
const currentTheme = ref(THEME_LIGHT)
const isDark = ref(false)

/**
 * 获取系统主题偏好
 */
const getSystemTheme = () => {
  if (typeof window === 'undefined') return THEME_LIGHT
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? THEME_DARK : THEME_LIGHT
}

/**
 * 应用主题到 DOM
 * @param {string} theme 主题名称 ('light', 'dark', 'auto')
 */
const applyTheme = (theme) => {
  if (typeof document === 'undefined') return

  const root = document.documentElement
  const actualTheme = theme === THEME_AUTO ? getSystemTheme() : theme

  // 使用 data-theme 属性（与 admin-theme.scss 配合）
  root.setAttribute('data-theme', actualTheme)
  isDark.value = actualTheme === THEME_DARK
}

/**
 * 初始化主题
 */
const initTheme = () => {
  // 从 localStorage 读取保存的主题设置
  const savedTheme = getItem(THEME_KEY) || THEME_AUTO
  currentTheme.value = savedTheme
  applyTheme(savedTheme)
}

/**
 * 设置主题
 * @param {string} theme 主题名称
 */
const setTheme = (theme) => {
  currentTheme.value = theme
  setItem(THEME_KEY, theme)
  applyTheme(theme)
}

/**
 * 切换深色/浅色模式
 */
const toggleDark = () => {
  const newTheme = isDark.value ? THEME_LIGHT : THEME_DARK
  setTheme(newTheme)
}

/**
 * 主题管理 Hook
 * @returns {Object} 主题相关的状态和方法
 */
export function useTheme() {
  // 监听系统主题变化（仅在 auto 模式下）
  const setupSystemThemeListener = () => {
    if (typeof window === 'undefined') return () => { }

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    const handleChange = (e) => {
      if (currentTheme.value === THEME_AUTO) {
        applyTheme(THEME_AUTO)
      }
    }

    // 现代浏览器使用 addEventListener
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handleChange)
      return () => mediaQuery.removeEventListener('change', handleChange)
    }
    // 旧版浏览器兼容
    else if (mediaQuery.addListener) {
      mediaQuery.addListener(handleChange)
      return () => mediaQuery.removeListener(handleChange)
    }

    return () => { }
  }

  // 组件挂载时初始化（仅第一次调用时执行）
  let initialized = false
  onMounted(() => {
    if (!initialized) {
      initTheme()
      const cleanup = setupSystemThemeListener()
      initialized = true
      return cleanup
    }
  })

  // 监听主题变化
  watch(currentTheme, (newTheme) => {
    applyTheme(newTheme)
  })

  return {
    currentTheme,
    isDark,
    setTheme,
    toggleDark,
    THEME_LIGHT,
    THEME_DARK,
    THEME_AUTO
  }
}

// 导出初始化函数供应用入口调用
export { initTheme }
