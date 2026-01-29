/**
 * Composables 统一导出
 * 提供项目所有可复用组合式函数的统一入口
 */

// 时间格式化
export { useTimeFormat, formatTime, formatTimeDivider, shouldShowTimeDivider } from './useTimeFormat'

// 用户状态
export { useUserStatus, UserStatus, mapUserOnlineStatus } from './useUserStatus'

// 消息格式化
export {
  useMessageFormat,
  MessageType,
  formatMessagePreview,
  getFileIcon,
  isImageFile,
  formatFileSize,
  parseMentions,
  renderMentions
} from './useMessageFormat'

// 暗色模式
export { useDarkMode, ThemeMode, getDarkModeStatus } from './useDarkMode'

// 图片懒加载
export {
  useLazyLoad,
  useImageLazyLoad,
  lazyLoadDirective
} from './useLazyLoad'

// 响应式断点
export { useResponsive } from './useResponsive'

// 主题
export { useTheme } from './useTheme'

// WebSocket IM
export { useImWebSocket } from './useImWebSocket'

// ========== 聊天相关 Composables ==========

// 文件上传
export { useFileUpload } from './useFileUpload'

// 输入状态指示器
export { useTypingIndicator } from './useTypingIndicator'

// 消息转换
export { useMessageTransformation } from './useMessageTransformation'

// 聊天面板状态管理
export { useChatPanel } from './useChatPanel'

// ========== 输入相关 Composables ==========

// 输入框草稿管理
export { useInputDraft } from './useInputDraft'

// 输入框高度调整
export { useInputResize } from './useInputResize'

// 输入框快捷命令
export { useInputCommand, DEFAULT_COMMANDS } from './useInputCommand'

// 语音预览
export { useVoicePreview, formatVoiceTime } from './useVoicePreview'
