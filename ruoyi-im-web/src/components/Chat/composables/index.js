/**
 * Chat 模块组合式函数统一导出
 *
 * @module composables/chat
 */

export { useMessageVirtualScroll, detectLargeGroup, getLazyLoadingThreshold } from './useMessageVirtualScroll.js'
export { useMessageReadUsers } from './useMessageReadUsers.js'
export { useMessageScroll } from './useMessageScroll.js'

// 类型定义（JSDoc）
export { MessageType, SessionType, SendStatus } from './types.js'
