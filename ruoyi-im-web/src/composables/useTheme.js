/**
 * 主题切换 Composable
 * 提供亮色/暗色模式切换功能
 */
import { useDark, useToggle, useStorage } from '@vueuse/core'

/**
 * 使用主题
 * @returns {Object} 主题相关方法和状态
 */
export function useTheme() {
  // 使用 useDark 管理暗色模式状态
  const isDark = useDark({
    storageKey: 'theme-mode',
    valueDark: 'dark',
    valueLight: 'light',
  })

  // 切换主题
  const toggleDark = useToggle(isDark)

  /**
   * 设置主题
   * @param {'light' | 'dark'} theme - 主题模式
   */
  const setTheme = (theme) => {
    isDark.value = theme === 'dark'
  }

  /**
   * 获取当前主题
   * @returns {'light' | 'dark'} 当前主题模式
   */
  const getTheme = () => {
    return isDark.value ? 'dark' : 'light'
  }

  return {
    isDark,
    toggleDark,
    setTheme,
    getTheme,
  }
}
