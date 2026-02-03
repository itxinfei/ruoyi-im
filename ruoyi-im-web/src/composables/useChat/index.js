/**
 * Chat Composables 统一导出
 *
 * 将 ChatPanel 的逻辑拆分为独立的 composables，提高可维护性和可测试性。
 *
 * 使用方式：
 * ```js
 * import {
 *   useChatMessages,
 *   useChatCommands,
 *   useChatDialogs,
 *   useChatMultiSelect,
 *   useChatUpload
 * } from '@/composables/useChat'
 * ```
 */

// 消息管理（加载、发送、重试）
export { useChatMessages } from './useChatMessages.js'

// 消息命令（复制、回复、转发、撤回、删除、编辑等）
export { useChatCommands } from './useChatCommands.js'

// 弹窗状态管理（通话、搜索、文件、历史等）
export { useChatDialogs } from './useChatDialogs.js'

// 消息多选（批量操作）
export { useChatMultiSelect } from './useChatMultiSelect.js'

// 文件上传（图片、文件、视频）
export { useChatUpload } from './useChatUpload.js'
