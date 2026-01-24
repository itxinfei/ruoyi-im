/**
 * 主题切换 Composable
 * 提供亮色/暗色模式切换功能
 */
import { ref, watch } from 'vue'

// 从 localStorage 读取主题设置
const STORAGE_KEY = 'theme-mode'
const getStoredTheme = () => {
  try {
    return localStorage.getItem(STORAGE_KEY) || 'light'
  } catch {
    return 'light'
  }
}

// 响应式主题状态
const isDark = ref(getStoredTheme() === 'dark')

// 监听主题变化，同步到 localStorage 和 HTML class
watch(isDark, (value) => {
  try {
    localStorage.setItem(STORAGE_KEY, value ? 'dark' : 'light')
    // 更新 HTML 元素的 class（用于全局样式）
    const html = document.documentElement
    if (value) {
      html.classList.add('dark')
    } else {
      html.classList.remove('dark')
    }
  } catch (e) {
    console.warn('主题设置保存失败:', e)
  }
}, { immediate: true })

/**
 * 使用主题
 * @returns {Object} 主题相关方法和状态
 */
export function useTheme() {
  /**
   * 切换主题
   */
  const toggleDark = () => {
    isDark.value = !isDark.value
  }

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
