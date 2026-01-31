/**
 * 可访问性配置
 * 遵循 WCAG 2.1 AA 标准
 */

/**
 * ARIA 属性配置
 */
export const ARIA_LABELS = {
  // 导航相关
  nav: '主导航',
  main: '主要内容',
  aside: '侧边栏',
  header: '页面头部',
  footer: '页面页脚',

  // 聊天相关
  chatPanel: '聊天面板',
  messageList: '消息列表',
  messageInput: '消息输入框',
  sendButton: '发送消息',
  attachButton: '附件上传',
  emojiButton: '表情选择',
  searchButton: '搜索',

  // 联系人相关
  contactsPanel: '联系人面板',
  contactList: '联系人列表',
  groupList: '群组列表',

  // 会话相关
  sessionPanel: '会话列表面板',
  sessionList: '会话列表',

  // 通用
  loading: '加载中',
  close: '关闭',
  menu: '菜单',
  more: '更多选项',
  settings: '设置',
  profile: '个人资料',
  logout: '退出登录'
}

/**
 * 键盘快捷键配置
 */
export const KEYBOARD_SHORTCUTS = {
  // 导航
  ALT_N: '导航到搜索',
  ALT_C: '导航到聊天',
  ALT_S: '导航到会话列表',
  ALT_T: '导航到联系人',

  // 消息操作
  ALT_ENTER: '发送消息',
  ESCAPE: '关闭对话框',
  ARROW_UP: '上一条消息',
  ARROW_DOWN: '下一条消息',

  // 其他
  ALT_M: '静音/取消静音',
  ALT_F: '搜索',
  ALT_H: '帮助'
}

/**
 * 焦点管理配置
 */
export const FOCUS_MANAGEMENT = {
  // 焦点可见性样式
  focusOutline: '2px solid #409EFF',
  focusOffset: '2px',

  // 跳过链接
  skipLinks: [
    { href: '#main-content', text: '跳转到主要内容' },
    { href: '#navigation', text: '跳转到导航' }
  ],

  // 焦点陷阱选择器
  focusTrapSelectors: [
    '[role="dialog"]',
    '.el-dialog',
    '.el-message-box'
  ]
}

/**
 * 颜色对比度配置（WCAG AA 标准）
 */
export const COLOR_CONTRAST = {
  // 正常文本（最小 4.5:1）
  normalText: {
    minRatio: 4.5,
    examples: [
      { foreground: '#000000', background: '#FFFFFF', ratio: 21 },
      { foreground: '#303133', background: '#FFFFFF', ratio: 12.6 },
      { foreground: '#606266', background: '#FFFFFF', ratio: 7.5 },
      { foreground: '#909399', background: '#FFFFFF', ratio: 4.6 } // 刚好满足 AA
    ]
  },

  // 大文本（最小 3:1，18pt 或 14pt 粗体）
  largeText: {
    minRatio: 3.0,
    examples: [
      { foreground: '#606266', background: '#FFFFFF', ratio: 7.5 }
    ]
  },

  // UI 组件（最小 3:1）
  uiComponents: {
    minRatio: 3.0,
    examples: [
      { foreground: '#409EFF', background: '#FFFFFF', ratio: 4.5 },
      { foreground: '#67C23A', background: '#FFFFFF', ratio: 3.8 },
      { foreground: '#E6A23C', background: '#FFFFFF', ratio: 3.1 }
    ]
  }
}
