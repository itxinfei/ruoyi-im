/**
 * 全局快捷键管理组合式函数
 * 提供统一的快捷键注册、注销和管理功能
 *
 * @example
 * const { registerShortcut, unregisterAll } = useKeyboardShortcuts()
 * registerShortcut('Ctrl+K', () => { openSearch() }, { description: '打开搜索' })
 */
import { onMounted, onUnmounted } from 'vue'

/**
 * 解析快捷键字符串
 * @param {string} shortcut - 快捷键字符串，如 'Ctrl+K', 'Cmd+S', 'Alt+Shift+F'
 * @returns {Object} 包含 ctrl, alt, shift, meta, key 的对象
 */
function parseShortcut(shortcut) {
  const parts = shortcut.toLowerCase().replace(/cmd/g, 'meta').replace(/command/g, 'meta').split('+')
  const result = {
    ctrl: false,
    alt: false,
    shift: false,
    meta: false,
    key: ''
  }

  for (const part of parts) {
    const trimmed = part.trim()
    if (trimmed === 'ctrl' || trimmed === 'control') {
      result.ctrl = true
    } else if (trimmed === 'alt') {
      result.alt = true
    } else if (trimmed === 'shift') {
      result.shift = true
    } else if (trimmed === 'meta' || trimmed === 'cmd' || trimmed === 'command') {
      result.meta = true
    } else {
      result.key = trimmed
    }
  }

  return result
}

/**
 * 检查键盘事件是否匹配快捷键
 * @param {KeyboardEvent} event - 键盘事件
 * @param {Object} parsedShortcut - 解析后的快捷键对象
 * @returns {boolean}
 */
function isMatch(event, parsedShortcut) {
  return (
    event.ctrlKey === parsedShortcut.ctrl &&
    event.altKey === parsedShortcut.alt &&
    event.shiftKey === parsedShortcut.shift &&
    event.metaKey === parsedShortcut.meta &&
    event.key.toLowerCase() === parsedShortcut.key
  )
}

/**
 * 格式化快捷键为显示文本
 * @param {string} shortcut - 快捷键字符串
 * @param {string} platform - 平台 'mac' 或 'windows'
 * @returns {string} 格式化后的快捷键文本
 */
export function formatShortcut(shortcut, platform = 'windows') {
  const isMac = platform === 'mac' || navigator.platform.toUpperCase().indexOf('MAC') >= 0
  return shortcut
    .replace(/cmd|command/gi, isMac ? '⌘' : 'Ctrl')
    .replace(/ctrl|control/gi, isMac ? '⌘' : 'Ctrl')
    .replace(/alt/gi, isMac ? '⌥' : 'Alt')
    .replace(/shift/gi, isMac ? '⇧' : 'Shift')
    .replace(/\+/g, isMac ? '' : '+')
    .toUpperCase()
}

/**
 * 获取快捷键的显示文本
 * @param {string} shortcut - 快捷键字符串
 * @returns {string} 格式化后的显示文本
 */
export function getShortcutLabel(shortcut) {
  const isMac = /mac|iphone|ipad|ipod/i.test(navigator.platform)

  const mappings = {
    'ctrl': isMac ? '⌘' : 'Ctrl',
    'alt': isMac ? '⌥' : 'Alt',
    'shift': isMac ? '⇧' : 'Shift',
    'cmd': '⌘',
    'command': '⌘',
    'meta': '⌘',
    'enter': 'Enter',
    'esc': 'Esc',
    'escape': 'Esc',
    'space': 'Space',
    'tab': 'Tab',
    'up': '↑',
    'down': '↓',
    'left': '←',
    'right': '→',
    'delete': 'Del',
    'backspace': '⌫'
  }

  return shortcut
    .toLowerCase()
    .split('+')
    .map(part => {
      const key = part.trim()
      return mappings[key] || key.toUpperCase()
    })
    .join(isMac ? '' : '+')
}

/**
 * 全局快捷键注册表
 */
const globalShortcuts = new Map()

/**
 * 防止默认行为的快捷键列表
 */
const preventDefaultShortcuts = new Set([
  'ctrl+s', 'cmd+s', // 保存
  'ctrl+k', 'cmd+k', // 搜索
  'ctrl+f', 'cmd+f', // 查找
  'ctrl+/', 'cmd+/', // 快捷键帮助
  'escape', 'esc' // 关闭
])

/**
 * 组合式函数：使用快捷键
 * @param {Object} options - 配置选项
 * @param {boolean} options.global - 是否全局注册（默认 true）
 * @param {HTMLElement} options.target - 绑定目标元素（非全局时使用）
 * @returns {Object} 快捷键操作方法
 */
export function useKeyboardShortcuts(options = {}) {
  const { global = true, target = null } = options

  // 当前组件注册的快捷键
  const localShortcuts = new Map()

  /**
   * 注册快捷键
   * @param {string} shortcut - 快捷键字符串
   * @param {Function} handler - 处理函数
   * @param {Object} options - 选项
   * @param {string} options.description - 快捷键描述
   * @param {boolean} options.preventDefault - 是否阻止默认行为
   * @param {Function} options.condition - 执行条件函数
   */
  const registerShortcut = (shortcut, handler, options = {}) => {
    const {
      description = '',
      preventDefault = preventDefaultShortcuts.has(shortcut.toLowerCase()),
      condition = () => true
    } = options

    const parsed = parseShortcut(shortcut)
    const shortcutData = {
      key: shortcut.toLowerCase(),
      handler,
      description,
      preventDefault,
      condition,
      parsed
    }

    if (global) {
      // 全局快捷键，添加到全局注册表
      if (!globalShortcuts.has(shortcutData.key)) {
        globalShortcuts.set(shortcutData.key, [])
      }
      globalShortcuts.get(shortcutData.key).push(shortcutData)
    }

    // 本地快捷键
    localShortcuts.set(shortcutData.key, shortcutData)

    return () => unregisterShortcut(shortcut)
  }

  /**
   * 注销快捷键
   * @param {string} shortcut - 快捷键字符串
   */
  const unregisterShortcut = (shortcut) => {
    const key = shortcut.toLowerCase()

    if (global) {
      const shortcuts = globalShortcuts.get(key)
      if (shortcuts) {
        const index = shortcuts.findIndex(s => s.key === key)
        if (index !== -1) {
          shortcuts.splice(index, 1)
        }
        if (shortcuts.length === 0) {
          globalShortcuts.delete(key)
        }
      }
    }

    localShortcuts.delete(key)
  }

  /**
   * 注销所有本地快捷键
   */
  const unregisterAll = () => {
    for (const key of localShortcuts.keys()) {
      unregisterShortcut(key)
    }
  }

  /**
   * 检查快捷键是否已注册
   * @param {string} shortcut - 快捷键字符串
   * @returns {boolean}
   */
  const isRegistered = (shortcut) => {
    return localShortcuts.has(shortcut.toLowerCase())
  }

  /**
   * 获取所有已注册的快捷键
   * @returns {Array} 快捷键列表
   */
  const getAllShortcuts = () => {
    return Array.from(localShortcuts.values()).map(s => ({
      key: s.key,
      description: s.description,
      label: getShortcutLabel(s.key)
    }))
  }

  /**
   * 键盘事件处理函数
   */
  const handleKeyDown = (event) => {
    // 忽略在输入框中的按键（除非是特定快捷键）
    const target = event.target
    const isInputElement = target.tagName === 'INPUT' ||
                           target.tagName === 'TEXTAREA' ||
                           target.contentEditable === 'true'

    // 检查所有已注册的快捷键
    const allShortcuts = global ? Array.from(globalShortcuts.values()).flat() : Array.from(localShortcuts.values())

    for (const shortcutData of allShortcuts) {
      if (isMatch(event, shortcutData.parsed)) {
        // 检查执行条件
        if (!shortcutData.condition()) {
          continue
        }

        // 如果在输入框中，跳过一些快捷键
        if (isInputElement && !shortcutData.allowInInput) {
          const allowedInInput = ['escape', 'ctrl+enter', 'cmd+enter', 'ctrl+shift+enter', 'ctrl+/', 'cmd+/']
          if (!allowedInInput.includes(shortcutData.key)) {
            continue
          }
        }

        if (shortcutData.preventDefault) {
          event.preventDefault()
          event.stopPropagation()
        }

        shortcutData.handler(event)
        return
      }
    }
  }

  /**
   * 组件挂载时注册事件监听
   */
  onMounted(() => {
    if (global) {
      // 全局快捷键只在第一次挂载时注册
      if (globalShortcuts.size === localShortcuts.size) {
        window.addEventListener('keydown', handleKeyDown, { capture: true })
      }
    } else if (target) {
      target.addEventListener('keydown', handleKeyDown)
    } else {
      window.addEventListener('keydown', handleKeyDown)
    }
  })

  /**
   * 组件卸载时清理
   */
  onUnmounted(() => {
    if (global) {
      // 只清理本地快捷键
      for (const key of localShortcuts.keys()) {
        const shortcuts = globalShortcuts.get(key)
        if (shortcuts) {
          const index = shortcuts.findIndex(s => localShortcuts.has(s.key))
          if (index !== -1) {
            shortcuts.splice(index, 1)
          }
          if (shortcuts.length === 0) {
            globalShortcuts.delete(key)
          }
        }
      }

      // 如果没有全局快捷键了，移除事件监听
      if (globalShortcuts.size === 0) {
        window.removeEventListener('keydown', handleKeyDown)
      }
    } else {
      unregisterAll()
      if (target) {
        target.removeEventListener('keydown', handleKeyDown)
      } else {
        window.removeEventListener('keydown', handleKeyDown)
      }
    }
  })

  return {
    registerShortcut,
    unregisterShortcut,
    unregisterAll,
    isRegistered,
    getAllShortcuts,
    formatShortcut,
    getShortcutLabel
  }
}

/**
 * 预定义的快捷键常量
 */
export const ShortcutKeys = {
  // 搜索
  SEARCH: 'Ctrl+K',
  FIND_IN_CHAT: 'Ctrl+F',

  // 发送消息
  SEND_MESSAGE: 'Ctrl+Enter',
  SEND_MESSAGE_ALT: 'Ctrl+Shift+Enter',

  // 导航
  NEXT_SESSION: 'Ctrl+Tab',
  PREV_SESSION: 'Ctrl+Shift+Tab',

  // 操作
  NEW_CHAT: 'Ctrl+N',
  NEW_GROUP: 'Ctrl+G',
  SCREENSHOT: 'Ctrl+Alt+A',

  // 关闭/取消
  CLOSE: 'Escape',
  CLOSE_SIDEBAR: 'Escape',

  // 帮助
  SHOW_SHORTCUTS: 'Ctrl+/',

  // 其他
  SELECT_ALL: 'Ctrl+A',
  DELETE: 'Delete',
  FORWARD: 'Ctrl+Shift+F'
}

/**
 * 默认快捷键描述
 */
export const ShortcutDescriptions = {
  [ShortcutKeys.SEARCH]: '打开全局搜索',
  [ShortcutKeys.FIND_IN_CHAT]: '搜索聊天记录',
  [ShortcutKeys.SEND_MESSAGE]: '发送消息',
  [ShortcutKeys.SEND_MESSAGE_ALT]: '换行发送',
  [ShortcutKeys.NEXT_SESSION]: '下一个会话',
  [ShortcutKeys.PREV_SESSION]: '上一个会话',
  [ShortcutKeys.NEW_CHAT]: '新建聊天',
  [ShortcutKeys.NEW_GROUP]: '新建群组',
  [ShortcutKeys.SCREENSHOT]: '截屏',
  [ShortcutKeys.CLOSE]: '关闭弹窗/取消操作',
  [ShortcutKeys.CLOSE_SIDEBAR]: '关闭侧边栏',
  [ShortcutKeys.SHOW_SHORTCUTS]: '显示快捷键帮助',
  [ShortcutKeys.SELECT_ALL]: '全选消息',
  [ShortcutKeys.DELETE]: '删除选中消息',
  [ShortcutKeys.FORWARD]: '转发消息'
}
