/**
 * Chat 组件统一导出
 *
 * 导出重构后的消息组件、输入组件和子组件
 */

// ========== 主重构组件 ==========
export { default as MessageBubble } from './MessageBubble.vue'
export { default as MessageItem } from './MessageItem.vue'
export { default as MessageInput } from './MessageInput.vue'

// ========== 工具栏组件 ==========
export { default as InputToolbar } from './InputToolbar.vue'
export { default as MultiSelectToolbar } from './MultiSelectToolbar.vue'
export { default as ResizeHandle } from './ResizeHandle.vue'

// ========== 预览组件 ==========
export { default as CommonPreview } from '../Common/CommonPreview.vue'
// 向后兼容旧组件名称，实际上使用新的通用组件
export { default as ReplyPreview } from '../Common/CommonPreview.vue'
export { default as EditPreview } from '../Common/CommonPreview.vue'
export { default as VoicePreviewPanel } from './VoicePreviewPanel.vue'

// ========== 状态指示组件 ==========
export { default as MessageStatusIndicator } from './MessageStatusIndicator.vue'
// 旧组件名称向后兼容
export { default as MessageStatus } from './MessageStatusIndicator.vue'
export { default as VoicePreviewPanel } from './VoicePreviewPanel.vue'

// ========== 功能组件 ==========
export { default as EmojiPicker } from './EmojiPicker.vue'
export { default as AtMemberPicker } from './AtMemberPicker.vue'
export { default as VoiceRecorder } from './VoiceRecorder.vue'
export { default as ScreenshotPreview } from './ScreenshotPreview.vue'
export { default as DingtalkScreenshot } from './DingtalkScreenshot.vue'
export { default as CommandPalette } from './CommandPalette.vue'
export { default as AiSmartReply } from './AiSmartReply.vue'
export { default as ScheduleDialog } from './ScheduleDialog.vue'
export { default as QuickReplyDialog } from '../QuickReplyDialog.vue'
