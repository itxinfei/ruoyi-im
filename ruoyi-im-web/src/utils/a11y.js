/**
 * 可访问性工具函数
 * 提供键盘导航、焦点管理、屏幕阅读器支持等功能
 */

/**
 * 管理焦点陷阱
 * 用于模态框、对话框等场景，限制 Tab 键在特定区域内移动
 */
export class FocusTrap {
  constructor(container) {
    this.container = container
    this.focusableElements = []
    this.firstElement = null
    this.lastElement = null
    this.previousActiveElement = null
  }

  /**
   * 获取所有可获得焦点的元素
   */
  getFocusableElements() {
    const focusableSelectors = [
      'button:not([disabled])',
      '[href]',
      'input:not([disabled])',
      'select:not([disabled])',
      'textarea:not([disabled])',
      '[tabindex]:not([tabindex="-1"])',
      '[contenteditable="true"]'
    ].join(', ')

    return Array.from(
      this.container.querySelectorAll(focusableSelectors)
    ).filter(el => {
      // 过滤隐藏元素
      return el.offsetWidth > 0 || el.offsetHeight > 0 || el.getClientRects().length > 0
    })
  }

  /**
   * 激活焦点陷阱
   */
  activate() {
    // 保存当前获得焦点的元素
    this.previousActiveElement = document.activeElement

    // 获取可聚焦元素
    this.focusableElements = this.getFocusableElements()
    this.firstElement = this.focusableElements[0]
    this.lastElement = this.focusableElements[this.focusableElements.length - 1]

    // 聚焦到第一个元素
    if (this.firstElement) {
      this.firstElement.focus()
    }

    // 添加键盘事件监听
    this.handleKeyDown = (e) => {
      if (e.key === 'Tab') {
        if (e.shiftKey) {
          // Shift + Tab
          if (document.activeElement === this.firstElement) {
            e.preventDefault()
            this.lastElement.focus()
          }
        } else {
          // Tab
          if (document.activeElement === this.lastElement) {
            e.preventDefault()
            this.firstElement.focus()
          }
        }
      }
    }

    this.container.addEventListener('keydown', this.handleKeyDown)
  }

  /**
   * 停用焦点陷阱
   */
  deactivate() {
    this.container.removeEventListener('keydown', this.handleKeyDown)

    // 恢复之前的焦点
    if (this.previousActiveElement) {
      this.previousActiveElement.focus()
    }
  }
}

/**
 * 设置跳过链接
 * 允许键盘用户跳过重复内容，直接访问主要区域
 */
export function setupSkipLinks() {
  const skipLinks = [
    { href: '#main-content', text: '跳转到主要内容' },
    { href: '#navigation', text: '跳转到导航' }
  ]

  skipLinks.forEach((link, index) => {
    const existingLink = document.querySelector(`a[href="${link.href}"]`)
    if (existingLink) return

    const skipLink = document.createElement('a')
    skipLink.href = link.href
    skipLink.textContent = link.text
    skipLink.className = 'skip-link'
    skipLink.style.cssText = `
      position: absolute;
      top: -40px;
      left: 0;
      background: #409EFF;
      color: white;
      padding: 8px 16px;
      z-index: 9999;
      text-decoration: none;
      transition: top 0.3s;
    `

    // 获得焦点时显示
    skipLink.addEventListener('focus', () => {
      skipLink.style.top = '0'
    })

    // 失去焦点时隐藏
    skipLink.addEventListener('blur', () => {
      skipLink.style.top = '-40px'
    })

    // 作为第一个子元素插入 body
    document.body.insertBefore(skipLink, document.body.firstChild)
  })
}

/**
 * 为元素添加 ARIA 标签
 */
export function addAriaLabels(element, options = {}) {
  if (!element) return

  const {
    label,
    labelledby,
    describedby,
    live,
    atomic,
    relevant,
    role
  } = options

  if (label) element.setAttribute('aria-label', label)
  if (labelledby) element.setAttribute('aria-labelledby', labelledby)
  if (describedby) element.setAttribute('aria-describedby', describedby)
  if (live) element.setAttribute('aria-live', live)
  if (atomic !== undefined) element.setAttribute('aria-atomic', String(atomic))
  if (relevant) element.setAttribute('aria-relevant', relevant)
  if (role) element.setAttribute('role', role)
}

/**
 * 设置键盘导航
 */
export function setupKeyboardNavigation(options = {}) {
  const {
    container = document,
    shortcuts = {}
  } = options

  container.addEventListener('keydown', (e) => {
    const key = `${e.altKey ? 'ALT_' : ''}${e.ctrlKey ? 'CTRL_' : ''}${e.shiftKey ? 'SHIFT_' : ''}${e.key.toUpperCase()}`

    if (shortcuts[key]) {
      e.preventDefault()
      shortcuts[key](e)
    }
  })
}

/**
 * 为动态内容添加实时区域通知
 * 用于屏幕阅读器播报重要信息
 */
export function announceToScreenReader(message, priority = 'polite') {
  // 移除旧的 live region
  const oldRegion = document.getElementById('a11y-live-region')
  if (oldRegion) {
    oldRegion.remove()
  }

  // 创建新的 live region
  const liveRegion = document.createElement('div')
  liveRegion.id = 'a11y-live-region'
  liveRegion.setAttribute('role', 'status')
  liveRegion.setAttribute('aria-live', priority)
  liveRegion.setAttribute('aria-atomic', 'true')
  liveRegion.className = 'sr-only'
  liveRegion.style.cssText = `
    position: absolute;
    width: 1px;
    height: 1px;
    padding: 0;
    margin: -1px;
    overflow: hidden;
    clip: rect(0, 0, 0, 0);
    white-space: nowrap;
    border-width: 0;
  `
  liveRegion.textContent = message

  document.body.appendChild(liveRegion)

  // 延迟移除，确保屏幕阅读器能够读取
  setTimeout(() => {
    liveRegion.remove()
  }, 1000)
}

/**
 * 检查颜色对比度是否符合 WCAG 标准
 */
export function checkColorContrast(foreground, background, isLargeText = false) {
  // 转换颜色为 RGB
  const fg = hexToRgb(foreground)
  const bg = hexToRgb(background)

  if (!fg || !bg) return null

  // 计算相对亮度
  const luminance = (color) => {
    const [r, g, b] = color.map(v => {
      v = v / 255
      return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4)
    })
    return 0.2126 * r[0] + 0.7152 * g[0] + 0.0722 * b[0]
  }

  const L1 = luminance(fg)
  const L2 = luminance(bg)

  // 计算对比度
  const lighter = Math.max(L1, L2)
  const darker = Math.min(L1, L2)
  const ratio = (lighter + 0.05) / (darker + 0.05)

  // 判断是否符合标准
  const minRatio = isLargeText ? 3.0 : 4.5
  const passes = ratio >= minRatio

  return {
    ratio: Math.round(ratio * 100) / 100,
    passes,
    level: passes ? 'AA' : 'Fail',
    isLargeText
  }
}

/**
 * 辅助函数：十六进制颜色转 RGB
 */
function hexToRgb(hex) {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result
    ? [
        parseInt(result[1], 16),
        parseInt(result[2], 16),
        parseInt(result[3], 16)
      ]
    : null
}

/**
 * 为表单元素添加错误提示的可访问性支持
 */
export function setupFormErrorA11y(input, errorElement, errorMessage) {
  if (!input || !errorElement) return

  // 设置 aria-invalid
  input.setAttribute('aria-invalid', 'true')

  // 设置 aria-describedby
  const errorId = 'error-' + Math.random().toString(36).substr(2, 9)
  errorElement.id = errorId
  input.setAttribute('aria-describedby', errorId)

  // 设置 role="alert"
  errorElement.setAttribute('role', 'alert')

  // 通知屏幕阅读器
  announceToScreenReader(errorMessage, 'assertive')
}

/**
 * 为模态框添加可访问性支持
 */
export function setupModalA11y(modalElement, options = {}) {
  const {
    title = '对话框',
    closeButtonText = '关闭'
  } = options

  // 设置 role
  modalElement.setAttribute('role', 'dialog')
  modalElement.setAttribute('aria-modal', 'true')
  modalElement.setAttribute('aria-labelledby', 'modal-title')
  modalElement.setAttribute('aria-describedby', 'modal-content')

  // 查找或创建标题
  let titleElement = modalElement.querySelector('[id="modal-title"]')
  if (!titleElement) {
    titleElement = modalElement.querySelector('.el-dialog__title, .modal-title')
    if (titleElement) {
      titleElement.id = 'modal-title'
    }
  }

  // 查找或创建内容区域
  let contentElement = modalElement.querySelector('[id="modal-content"]')
  if (!contentElement) {
    contentElement = modalElement.querySelector('.el-dialog__body, .modal-body')
    if (contentElement) {
      contentElement.id = 'modal-content'
    }
  }

  // 设置焦点陷阱
  const focusTrap = new FocusTrap(modalElement)
  focusTrap.activate()

  // 返回清理函数
  return () => {
    focusTrap.deactivate()
  }
}

/**
 * 为列表设置 ARIA 标签
 */
export function setupListA11y(listElement, options = {}) {
  const {
    label,
    size = null,
    itemCount = null
  } = options

  listElement.setAttribute('role', 'list')

  if (label) {
    listElement.setAttribute('aria-label', label)
  }

  if (size !== null) {
    listElement.setAttribute('aria-setsize', size)
  }

  if (itemCount !== null) {
    listElement.setAttribute('aria-count', itemCount)
  }

  // 为列表项添加 role="listitem"
  const items = listElement.querySelectorAll('[role="listitem"]')
  if (items.length === 0) {
    listElement.querySelectorAll('li').forEach((item, index) => {
      item.setAttribute('role', 'listitem')
      item.setAttribute('aria-posinset', index + 1)
      item.setAttribute('aria-setsize', itemCount || listElement.children.length)
    })
  }
}
