/**
 * 暗色模式管理
 * 提供统一的暗色模式切换和状态管理
 */

import { ref, computed, watch, onMounted } from 'vue'

/**
 * 主题常量
 */
export const ThemeMode = {
  LIGHT: 'light',
  DARK: 'dark',
  AUTO: 'auto'
}

/**
 * 本地存储键名
 */
const STORAGE_KEY = 'im-theme-mode'
const CLASSIC_DARK_KEY = 'darkMode'

/**
 * 全局主题状态（跨组件共享）
 */
const globalThemeMode = ref(ThemeMode.LIGHT)

/**
 * Composable：暗色模式
 * @param {Object} [options] - 配置选项
 * @param {boolean} [options.listenToSystem=true] - 是否监听系统主题变化
 * @param {HTMLElement} [options.target=document.documentElement] - 应用主题的目标元素
 * @returns {Object} 主题相关方法和状态
 */
export function useDarkMode(options = {}) {
  const {
    listenToSystem = true,
    target = document.documentElement
  } = options

  /**
   * 获取保存的主题模式
   * @returns {string} 主题模式
   */
  const getSavedTheme = () => {
    try {
      // 优先读取新格式
      const saved = localStorage.getItem(STORAGE_KEY)
      if (saved) return saved

      // 兼容旧格式
      const classicDark = localStorage.getItem(CLASSIC_DARK_KEY)
      if (classicDark !== null) {
        return classicDark === 'true' ? ThemeMode.DARK : ThemeMode.LIGHT
      }
    } catch (e) {
      console.warn('读取主题设置失败:', e)
    }
    return ThemeMode.AUTO
  }

  /**
   * 保存主题模式
   * @param {string} mode - 主题模式
   */
  const saveTheme = (mode) => {
    try {
      localStorage.setItem(STORAGE_KEY, mode)
      // 同步更新旧格式（兼容）
      localStorage.setItem(CLASSIC_DARK_KEY, String(mode === ThemeMode.DARK))
    } catch (e) {
      console.warn('保存主题设置失败:', e)
    }
  }

  /**
   * 检测系统主题
   * @returns {boolean} 是否为暗色模式
   */
  const getSystemTheme = () => {
    if (typeof window === 'undefined' || !window.matchMedia) return false
    return window.matchMedia('(prefers-color-scheme: dark)').matches
  }

  /**
   * 应用主题到 DOM
   * @param {string} mode - 主题模式
   */
  const applyTheme = (mode) => {
    const isDark = mode === ThemeMode.DARK || (mode === ThemeMode.AUTO && getSystemTheme())

    if (isDark) {
      target.classList.add('dark')
    } else {
      target.classList.remove('dark')
    }
  }

  /**
   * 当前是否为暗色模式
   */
  const isDark = computed(() => {
    const mode = globalThemeMode.value
    return mode === ThemeMode.DARK || (mode === ThemeMode.AUTO && getSystemTheme())
  })

  /**
   * 当前主题模式
   */
  const themeMode = computed(() => globalThemeMode.value)

  /**
   * 设置主题模式
   * @param {string} mode - 主题模式
   */
  const setThemeMode = (mode) => {
    if (!Object.values(ThemeMode).includes(mode)) {
      console.warn(`无效的主题模式: ${mode}`)
      return
    }

    globalThemeMode.value = mode
    saveTheme(mode)
    applyTheme(mode)
  }

  /**
   * 切换暗色模式
   */
  const toggleDarkMode = () => {
    const newMode = isDark.value ? ThemeMode.LIGHT : ThemeMode.DARK
    setThemeMode(newMode)
  }

  /**
   * 监听系统主题变化
   */
  const setupSystemListener = () => {
    if (!listenToSystem || typeof window === 'undefined' || !window.matchMedia) return

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    const handler = (e) => {
      if (globalThemeMode.value === ThemeMode.AUTO) {
        applyTheme(ThemeMode.AUTO)
      }
    }

    // 现代浏览器使用 addEventListener
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handler)
      return () => mediaQuery.removeEventListener('change', handler)
    }
    // 旧浏览器兼容
    else if (mediaQuery.addListener) {
      mediaQuery.addListener(handler)
      return () => mediaQuery.removeListener(handler)
    }
  }

  // 初始化
  onMounted(() => {
    const saved = getSavedTheme()
    globalThemeMode.value = saved
    applyTheme(saved)
  })

  // 监听主题变化
  watch(globalThemeMode, (newMode) => {
    applyTheme(newMode)
  })

  // 监听系统主题变化
  let cleanupSystemListener
  watch(() => options.listenToSystem, () => {
    if (cleanupSystemListener) {
      cleanupSystemListener()
    }
    cleanupSystemListener = setupSystemListener()
  }, { immediate: true })

  return {
    ThemeMode,
    isDark,
    themeMode,
    setThemeMode,
    toggleDarkMode,
    getSystemTheme
  }
}

/**
 * 快速获取暗色模式状态（不响应式）
 * @returns {boolean}
 */
export function getDarkModeStatus() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved === ThemeMode.DARK) return true
    if (saved === ThemeMode.LIGHT) return false

    // AUTO 模式下检测系统主题
    if (typeof window !== 'undefined' && window.matchMedia) {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }
  } catch (e) {
    // Ignore errors
  }
  return false
}
