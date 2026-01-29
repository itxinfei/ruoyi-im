// ============================================================================
// useTheme.js
// 主题管理组合式函数
// ============================================================================

import { ref, computed, onMounted, watch } from 'vue'
import { useStore } from 'vuex'

/**
 * 主题管理组合式函数
 * @returns {Object} 主题相关的状态和方法
 */
export function useTheme() {
  const store = useStore()
  const currentTheme = ref('light')
  const isDarkMode = ref(false)

  /**
   * 初始化主题
   */
  const initTheme = () => {
    // 从本地存储获取主题设置
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme) {
      currentTheme.value = savedTheme
      isDarkMode.value = savedTheme === 'dark'
    } else {
      // 检查系统偏好
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      currentTheme.value = prefersDark ? 'dark' : 'light'
      isDarkMode.value = prefersDark
    }

    // 应用主题
    applyTheme(currentTheme.value)
  }

  /**
   * 应用主题
   * @param {string} theme 主题名称 ('light', 'dark', 'system')
   */
  const applyTheme = (theme) => {
    if (theme === 'system') {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      isDarkMode.value = prefersDark
    } else {
      isDarkMode.value = theme === 'dark'
    }

    // 更新文档根元素的 class
    const root = document.documentElement
    if (isDarkMode.value) {
      root.classList.add('dark')
    } else {
      root.classList.remove('dark')
    }

    // 保存主题设置到本地存储
    localStorage.setItem('theme', theme)
    if (currentTheme.value !== theme) {
      currentTheme.value = theme
    }

    // 同步到服务器（仅在已登录时执行，且静默处理）
    const token = localStorage.getItem('im_token')
    if (token) {
      store.dispatch('im/batchUpdateServerSettings', {
        'general.theme': theme
      }).catch(() => {
        // 忽略服务器更新失败
      })
    }
  }

  /**
   * 切换主题
   * @param {string} theme 主题名称 ('light', 'dark', 'system')
   */
  const setTheme = (theme) => {
    applyTheme(theme)
  }

  /**
   * 切换深色模式
   * @param {boolean} dark 是否启用深色模式
   */
  const toggleDarkMode = (dark) => {
    applyTheme(dark ? 'dark' : 'light')
  }

  /**
   * 监听系统主题变化
   */
  const setupThemeListener = () => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')

    const handleChange = (e) => {
      if (currentTheme.value === 'system') {
        isDarkMode.value = e.matches
        applyTheme('system')
      }
    }

    mediaQuery.addEventListener('change', handleChange)

    // 清理监听器
    return () => {
      mediaQuery.removeEventListener('change', handleChange)
    }
  }

  // 生命周期钩子
  onMounted(() => {
    initTheme()
    setupThemeListener()
  })

  // 监听主题变化
  watch(currentTheme, (newTheme, oldTheme) => {
    if (newTheme !== oldTheme) {
      applyTheme(newTheme)
    }
  })

  /**
   * 切换主题模式
   */
  const toggleTheme = () => {
    const newTheme = isDarkMode.value ? 'light' : 'dark'
    applyTheme(newTheme)
  }

  return {
    currentTheme,
    isDark: isDarkMode,
    isDarkMode,
    setTheme,
    toggleDarkMode,
    toggleTheme,
    applyTheme
  }
}
