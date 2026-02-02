/**
 * API 接口类型定义
 * 为后端 API 提供类型安全的请求和响应类型
 * @author ruoyi
 */

import type { ImMessage } from './im'

// ==================== 消息 API ====================

/**
 * 发送消息请求
 */
export interface SendMessageRequest {
  conversationId: number
  content: string
  messageType: 'TEXT' | 'IMAGE' | 'FILE' | 'VOICE' | 'VIDEO'
  extras?: {
    fileName?: string
    fileSize?: number
    fileUrl?: string
    duration?: number
  }
}

/**
 * 转发消息请求
 */
export interface ForwardMessageRequest {
  messageId: number
  toConversationId?: number
  toUserId?: number
  content?: string
}

/**
 * 回复消息请求
 */
export interface ReplyMessageRequest {
  messageId: number
  content: string
}

/**
 * 编辑消息请求
 */
export interface EditMessageRequest {
  newContent: string
}

/**
 * 搜索消息请求
 */
export interface SearchMessageRequest {
  conversationId?: number
  keyword: string
  messageType?: string
  senderId?: number
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
  includeRevoked?: boolean
  exactMatch?: boolean
}

/**
 * 标记已读请求
 */
export interface MarkReadRequest {
  conversationId: number
  messageIds?: number[]
}

// ==================== 会话 API ====================

/**
 * 创建会话请求
 */
export interface CreateConversationRequest {
  type: 'PRIVATE' | 'GROUP'
  targetId: number
}

/**
 * 更新会话请求
 */
export interface UpdateConversationRequest {
  isPinned?: boolean
  isMuted?: boolean
}

/**
 * 会话操作响应
 */
export interface ConversationActionResponse {
  success: boolean
  message: string
}

// ==================== 群组 API ====================

/**
 * 创建群组请求
 */
export interface CreateGroupRequest {
  groupName: string
  avatar?: string
  description?: string
  memberIds?: number[]
  maxMembers?: number
}

/**
 * 更新群组请求
 */
export interface UpdateGroupRequest {
  groupName?: string
  avatar?: string
  description?: string
  maxMembers?: number
}

/**
 * 群组成员操作请求
 */
export interface GroupMemberOperationRequest {
  userIds: number[]
  role?: 'ADMIN' | 'MEMBER'
}

// ==================== 用户 API ====================

/**
 * 更新用户状态请求
 */
export interface UpdateUserStatusRequest {
  status: 'ENABLED' | 'DISABLED'
}

/**
 * 更新用户信息请求
 */
export interface UpdateUserProfileRequest {
  nickname?: string
  avatar?: string
  signature?: string
  gender?: number
  birthday?: string
}

// ==================== 好友 API ====================

/**
 * 添加好友请求
 */
export interface AddFriendRequest {
  userId: number
  remark?: string
  groupName?: string
}

/**
 * 好友操作响应
 */
export interface FriendOperationResponse {
  success: boolean
  message: string
  friendId?: number
}

// ==================== 通知 API ====================
// 注意: SystemNotification 类型从 im.ts 导入

// ==================== 文件上传 API ====================

/**
 * 文件上传响应
 */
export interface FileUploadResponse {
  url: string
  fileName: string
  fileSize: number
  fileType: string
}

/**
 * 头像上传响应
 */
export interface AvatarUploadResponse {
  avatarUrl: string
}

// ==================== 通用 API 响应 ====================

/**
 * 统一 API 响应
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: number
}

/**
 * 分页数据响应
 */
export interface PagedResponse<T> {
  code: number
  message: string
  data: {
    list: T[]
    total: number
    pageNum: number
    pageSize: number
    pages: number
  }
}

// ==================== WebSocket 消息类型 ====================

/**
 * WebSocket 认证消息
 */
export interface WsAuthMessage {
  type: 'auth'
  token: string
}

/**
 * WebSocket 聊天消息
 */
export interface WsChatMessage {
  type: 'message'
  conversationId: number
  messageId: number
  data: ImMessage
}

/**
 * WebSocket 已读回执
 */
export interface WsReadReceipt {
  type: 'read'
  conversationId: number
  lastReadMessageId: number
  userId: number
}

/**
 * WebSocket 输入状态
 */
export interface WsTypingMessage {
  type: 'typing'
  conversationId: number
  userId: number
}

/**
 * WebSocket 心跳
 */
export interface WsPingMessage {
  type: 'ping'
  timestamp: number
}

/**
 * WebSocket 心跳响应
 */
export interface WsPongMessage {
  type: 'pong'
  timestamp: number
}

/**
 * WebSocket 表情反应
 */
export interface WsReactionMessage {
  type: 'reaction'
  conversationId: number
  messageId: number
  userId: number
  emoji: string
  action: 'add' | 'remove'
}

// ==================== 类型守卫 ====================

/**
 * 判断是否为 API 错误响应
 */
export function isApiError(obj: any): obj is ApiResponse {
  return (
    typeof obj === 'object' &&
    obj !== null &&
    'code' in obj &&
    typeof obj.code === 'number'
  )
}

/**
 * 判断 API 响应是否成功
 */
export function isApiSuccess(obj: any): boolean {
  return isApiError(obj) && obj.code === 200
}

/**
 * 获取分页数据
 */
export function getPagedData<T>(obj: any): T[] | null {
  if (
    obj &&
    typeof obj === 'object' &&
    'data' in obj &&
    obj.data &&
    typeof obj.data === 'object' &&
    'list' in obj.data
  ) {
    return obj.data.list
  }
  return null
}
