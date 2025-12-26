const THEME_KEY = 'app-theme'

const themes = {
  light: {
    name: 'light',
    displayName: '浅色主题',
    variables: {
      '--primary-color': '#3494e6',
      '--primary-light': '#ecf5ff',
      '--primary-dark': '#2c7be5',
      '--success-color': '#67c23a',
      '--warning-color': '#e6a23c',
      '--danger-color': '#f56c6c',
      '--info-color': '#909399',
      '--background-color': '#f5f7fa',
      '--text-primary': '#303133',
      '--text-regular': '#606266',
      '--text-secondary': '#909399',
      '--border-color': '#dcdfe6',
      '--border-light': '#e4e7ed',
      '--header-background': '#ffffff',
      '--sidebar-background': '#ffffff',
      '--chat-background': '#f5f7fa',
      '--message-background': '#ffffff',
      '--hover-background': '#f5f7fa',
      '--box-shadow': '0 2px 12px 0 rgba(0, 0, 0, 0.1)',
    },
  },
  dark: {
    name: 'dark',
    displayName: '深色主题',
    variables: {
      '--primary-color': '#3494e6',
      '--primary-light': '#1d1e23',
      '--primary-dark': '#2c7be5',
      '--success-color': '#67c23a',
      '--warning-color': '#e6a23c',
      '--danger-color': '#f56c6c',
      '--info-color': '#909399',
      '--background-color': '#1a1b1f',
      '--text-primary': '#ffffff',
      '--text-regular': '#d0d0d0',
      '--text-secondary': '#909399',
      '--border-color': '#363636',
      '--border-light': '#363636',
      '--header-background': '#1d1e23',
      '--sidebar-background': '#1d1e23',
      '--chat-background': '#1a1b1f',
      '--message-background': '#1d1e23',
      '--hover-background': '#2a2a2a',
      '--box-shadow': '0 2px 12px 0 rgba(0, 0, 0, 0.3)',
    },
  },
}

let currentTheme = null

export function getTheme() {
  if (!currentTheme) {
    const savedTheme = localStorage.getItem(THEME_KEY)
    currentTheme = savedTheme && themes[savedTheme] ? savedTheme : 'light'
  }
  return currentTheme
}

export function setTheme(themeName) {
  if (!themes[themeName]) {
    console.warn(`主题 "${themeName}" 不存在`)
    return false
  }

  const theme = themes[themeName]
  const root = document.documentElement

  Object.entries(theme.variables).forEach(([key, value]) => {
    root.style.setProperty(key, value)
  })

  root.setAttribute('data-theme', themeName)
  localStorage.setItem(THEME_KEY, themeName)
  currentTheme = themeName

  return true
}

export function initTheme() {
  const themeName = getTheme()
  setTheme(themeName)
  return themeName
}

export function toggleTheme() {
  const current = getTheme()
  const newTheme = current === 'light' ? 'dark' : 'light'
  return setTheme(newTheme)
}

export function getThemes() {
  return Object.values(themes).map(theme => ({
    name: theme.name,
    displayName: theme.displayName,
  }))
}

export function getCurrentThemeInfo() {
  const themeName = getTheme()
  return themes[themeName]
}
