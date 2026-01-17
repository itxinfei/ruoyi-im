// 主题管理器：提供应用主题切换的核心接口
// 通过 data-theme 属性实现简单而稳健的主题切换，后续可扩展为多主题包

export const THEME_KEY = 'ruoyi-im-theme'
export const THEME_MAP = {
  light: 'Light',
  dark: 'Dark',
}

/**
 * 应用主题
 * @param {string} themeName - 主题名称，需在 THEME_MAP 中定义
 */
export function applyTheme(themeName) {
  if (!THEME_MAP[themeName]) return
  document.documentElement.setAttribute('data-theme', themeName)
  localStorage.setItem(THEME_KEY, themeName)
}

/**
 * 读取上次使用的主题
 * @returns {string} 主题名称
 */
export function loadTheme() {
  const t = localStorage.getItem(THEME_KEY)
  if (t && THEME_MAP[t]) return t
  return 'light'
}
