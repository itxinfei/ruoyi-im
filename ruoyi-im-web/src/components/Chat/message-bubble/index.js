/**
 * 消息气泡组件索引
 * 导出所有气泡相关的子组件和组合式函数
 */

// ==================== 主组件 ====================
export { default as MessageBubble } from './MessageBubbleRefactored.vue'

// ==================== 气泡类型组件 ====================
export { default as TextBubble } from './bubbles/TextBubble.vue'
export { default as ImageBubble } from './bubbles/ImageBubble.vue'
export { default as FileBubble } from './bubbles/FileBubble.vue'
export { default as VoiceBubble } from './bubbles/VoiceBubble.vue'
export { default as VideoBubble } from './bubbles/VideoBubble.vue'
export { default as LocationBubble } from './bubbles/LocationBubble.vue'
export { default as SystemBubble } from './bubbles/SystemBubble.vue'
export { default as RecalledBubble } from './bubbles/RecalledBubble.vue'

// ==================== 零件组件 ====================
export { default as MessageReplyRef } from './parts/MessageReplyRef.vue'
export { default as MessageStatus } from './parts/MessageStatus.vue'
export { default as MessageReactions } from './parts/MessageReactions.vue'
export { default as CodeBlock } from './parts/CodeBlock.vue'
export { default as MessageMarkers } from './parts/MessageMarkers.vue'
export { default as LinkCardList } from './parts/LinkCardList.vue'

// ==================== Composables ====================
export { useMessageBubble } from './composables/useMessageBubble.js'
export { useMessageStatus } from './composables/useMessageStatus.js'
export { useMessageReaction } from './composables/useMessageReaction.js'
