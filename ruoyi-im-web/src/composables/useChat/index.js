/**
 * Chat Composables 统一导出
 *
 * 将 ChatPanel 的逻辑拆分为独立的 composables，提高可维护性和可测试性。
 *
 * 使用方式：
 * ```js
 * // 方式1：使用主入口（推荐，获取所有功能）
 * import { useChat } from '@/composables/useChat'
 *
 * // 方式2：按需导入子 composables
 * import {
 *   useChatMessages,
 *   useChatCommands,
 *   useChatDialogs,
 *   useChatMultiSelect,
 *   useChatSend,
 *   useChatWebSocket,
 *   useChatTyping,
 *   useChatSessionOps,
 *   useChatDragDrop
 * } from '@/composables/useChat'
 * ```
 */

// 消息管理（加载、分页 — Store 薄包装）
export { useChatMessages } from './useChatMessages.js'

// 消息命令（复制、回复、转发、撤回、删除、编辑等）
export { useChatCommands } from './useChatCommands.js'

// 弹窗状态管理（通话、搜索、文件、历史等）
export { useChatDialogs } from './useChatDialogs.js'

// 消息多选（批量操作）
export { useChatMultiSelect } from './useChatMultiSelect.js'

// 统一发送 + 上传 + 重试
export { useChatSend } from './useChatSend.js'

// WebSocket 事件处理
export { useChatWebSocket } from './useChatWebSocket.js'

// 输入状态管理
export { useChatTyping } from './useChatTyping.js'

// 会话级操作（置顶、免打扰、清空）
export { useChatSessionOps } from './useChatSessionOps.js'

// 拖拽上传
export { useChatDragDrop } from './useChatDragDrop.js'
