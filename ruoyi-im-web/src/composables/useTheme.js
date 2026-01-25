/**
 * 主题切换 Composable
 * 提供亮色/暗色模式切换功能，支持系统自动跟随、跨标签页同步
 */
import { ref, watch, computed } from 'vue'

const STORAGE_KEY = 'theme-mode'

// 核心单例状态
const themeMode = ref('auto')
const isDark = ref(false)

/**
 * 判断当前是否应该应用暗色
 */
const getSystemIsDark = () => {
  if (typeof window === 'undefined') return false
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

/**
 * 更新最终导出的 isDark 状态
 */
const updateIsDark = () => {
  if (themeMode.value === 'auto') {
    isDark.value = getSystemIsDark()
  } else {
    isDark.value = themeMode.value === 'dark'
  }
}

/**
 * 应用主题样式到 DOM
 * 使用 View Transition API 实现平滑切换 (如果可用)
 */
const applyThemeToDOM = (dark) => {
  if (typeof document === 'undefined') return

  const update = () => {
    const html = document.documentElement
    if (dark) {
      html.classList.add('dark')
      html.setAttribute('data-theme', 'dark')
    } else {
      html.classList.remove('dark')
      html.setAttribute('data-theme', 'light')
    }

    // 更新 meta theme-color
    let meta = document.querySelector('meta[name="theme-color"]')
    if (!meta) {
      meta = document.createElement('meta')
      meta.name = 'theme-color'
      document.head.appendChild(meta)
    }
    meta.content = dark ? '#0f172a' : '#ffffff'
  }

  // 尝试使用极简的 View Transition (如果支持)
  if (document.startViewTransition) {
    document.startViewTransition(update)
  } else {
    update()
  }
}

// === 初始化逻辑 ===
const init = () => {
  if (typeof window === 'undefined') return

  // 1. 加载持久化设置
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored) themeMode.value = stored

  // 2. 初始计算状态
  updateIsDark()
  applyThemeToDOM(isDark.value)

  // 3. 系统主题监听
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  const listener = () => {
    if (themeMode.value === 'auto') updateIsDark()
  }
  if (mediaQuery.addEventListener) mediaQuery.addEventListener('change', listener)
  else mediaQuery.addListener(listener)

  // 4. 存储同步监听 (跨标签)
  window.addEventListener('storage', (e) => {
    if (e.key === STORAGE_KEY && e.newValue && e.newValue !== themeMode.value) {
      themeMode.value = e.newValue
    }
  })
}

// 立即执行初始化
init()

// 侦听变化
watch(themeMode, (val) => {
  localStorage.setItem(STORAGE_KEY, val)
  updateIsDark()
})

watch(isDark, (val) => {
  applyThemeToDOM(val)
})

/**
 * 主题管理 Hook
 */
export function useTheme() {
  /**
   * 切换主题: 亮色 -> 暗色 -> 自动
   */
  const toggleTheme = () => {
    const modes = ['light', 'dark', 'auto']
    const nextIdx = (modes.indexOf(themeMode.value) + 1) % modes.length
    themeMode.value = modes[nextIdx]
  }

  const setThemeMode = (mode) => {
    if (['light', 'dark', 'auto'].includes(mode)) {
      themeMode.value = mode
    }
  }

  // 计算模式文字提示
  const themeModeLabel = computed(() => {
    if (themeMode.value === 'light') return '浅色模式'
    if (themeMode.value === 'dark') return '深色模式'
    return '跟随系统'
  })

  return {
    isDark,
    themeMode,
    themeModeLabel,
    toggleTheme,
    setThemeMode
  }
}
