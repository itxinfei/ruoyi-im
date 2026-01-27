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
