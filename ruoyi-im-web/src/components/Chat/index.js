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
export { default as ReplyPreview } from './ReplyPreview.vue'
export { default as EditPreview } from './EditPreview.vue'
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
