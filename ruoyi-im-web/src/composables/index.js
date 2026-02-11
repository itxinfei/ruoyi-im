/**
 * Composables 统一导出
 * 提供项目所有可复用组合式函数的统一入口
 */

// 用户状态
export { useUserStatus, UserStatus, mapUserOnlineStatus } from './useUserStatus'

// 响应式断点
export { useResponsive } from './useResponsive'

// 主题
export { useTheme } from './useTheme'

// WebSocket IM
export { useImWebSocket } from './useImWebSocket'

// 图片懒加载
export {
  useLazyLoad,
  useImageLazyLoad,
  lazyLoadDirective
} from './useLazyLoad'

// ========== 聊天相关 Composables ==========

// 文件上传（推荐使用统一版本）
export {
  useFileUploadUnified,
  useFileUploadQueue,
  useFileUploadOptimistic
} from './useFileUploadUnified'

// @deprecated 请使用 useFileUploadUnified
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

// 消息重试
export { useMessageRetry } from './useMessageRetry'

// @提及功能
export { useMentions } from './useMentions'

// 键盘快捷键
export { useKeyboardShortcuts } from './useKeyboardShortcuts'

// IndexedDB
export { useIndexedDB } from './useIndexedDB'

// 聊天功能模块
export {
  useChatMultiSelect,
  useChatDialogs,
  useChatMessages,
  useChatCommands,
  useChatSend,
  useChatWebSocket,
  useChatTyping,
  useChatSessionOps,
  useChatDragDrop
} from './useChat'

// 网络状态
export { useNetworkStatus } from './useNetworkStatus'

// 高亮文本
export { useHighlightText } from './useHighlightText'

// 错误处理
export { useErrorHandler, useMessageErrorHandler, useApiErrorHandler } from './useErrorHandler'

// 联系人批量操作
export { useContactBatch } from './useContactBatch'

// 链接预览
export { useLinkPreview } from './useLinkPreview'

// @deprecated 已被 useChatSend (store + useChatSend.retryMessage) 替代
// export { useMessageQueue } from './useMessageQueue'
