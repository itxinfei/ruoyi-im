/**
 * RuoYi-IM 全局类型定义
 * 用于提供项目中常用的类型
 */

// ==================== 用户相关类型 ====================

/**
 * 用户信息
 */
export interface UserInfo {
  id: number
  userId: number
  username: string
  nickName: string
  avatar?: string
  email?: string
  phone?: string
  gender?: 'MALE' | 'FEMALE' | 'UNKNOWN'
  status?: 'ONLINE' | 'OFFLINE' | 'BUSY' | 'AWAY'
  role?: 'ADMIN' | 'SUPER_ADMIN' | 'USER'
  department?: string
  position?: string
  signature?: string
  createTime?: string
}

// ==================== 消息相关类型 ====================

/**
 * 消息类型枚举
 */
export enum MessageType {
  TEXT = 'TEXT',
  IMAGE = 'IMAGE',
  FILE = 'FILE',
  VOICE = 'VOICE',
  VIDEO = 'VIDEO',
  LOCATION = 'LOCATION',
  SYSTEM = 'SYSTEM',
  NOTICE = 'NOTICE'
}

/**
 * 消息状态枚举
 */
export enum MessageStatus {
  SENDING = 'SENDING',
  SENT = 'SENT',
  DELIVERED = 'DELIVERED',
  READ = 'READ',
  FAILED = 'FAILED',
  REVOKED = 'REVOKED'
}

/**
 * 消息内容结构
 */
export interface MessageContent {
  text?: string
  imageUrl?: string
  fileName?: string
  fileSize?: number
  fileUrl?: string
  duration?: number
  location?: {
    latitude: number
    longitude: number
    address: string
  }
  mentions?: MentionInfo[]
}

/**
 * @提及信息
 */
export interface MentionInfo {
  userId: number
  nickName: string
  type?: 'AT_USER' | 'AT_ALL'
}

/**
 * 消息对象
 */
export interface Message {
  id: number
  messageId?: number
  conversationId?: number
  sessionId?: number
  senderId: number
  sender?: UserInfo
  receiverId?: number
  messageType: MessageType
  content: string | MessageContent
  status: MessageStatus
  isRevoked?: boolean
  isEdited?: boolean
  isDeleted?: boolean
  replyToMessageId?: number
  replyToMessage?: Message
  forwardFromMessageId?: number
  editCount?: number
  sendTime: string
  createTime?: string
}

// ==================== 会话相关类型 ====================

/**
 * 会话类型枚举
 */
export enum ConversationType {
  PRIVATE = 'PRIVATE',
  GROUP = 'GROUP'
}

/**
 * 会话对象
 */
export interface Conversation {
  id: number
  sessionId?: number
  type: ConversationType
  targetId: number
  name?: string
  avatar?: string
  lastMessage?: Message
  lastMessageId?: number
  lastMessageTime?: string
  unreadCount?: number
  isPinned?: boolean
  isMuted?: boolean
  isDeleted?: boolean
  createTime?: string
  updateTime?: string
}

/**
 * 会话成员
 */
export interface ConversationMember {
  id: number
  conversationId: number
  userId: number
  user?: UserInfo
  role?: 'OWNER' | 'ADMIN' | 'MEMBER'
  unreadCount?: number
  isPinned?: boolean
  isMuted?: boolean
  lastReadMessageId?: number
  joinTime?: string
}

// ==================== 群组相关类型 ====================

/**
 * 群组对象
 */
export interface Group {
  id: number
  groupId?: number
  name: string
  avatar?: string
  description?: string
  ownerId: number
  owner?: UserInfo
  type?: 'NORMAL' | 'DEPARTMENT' | 'PROJECT'
  maxMembers?: number
  memberCount?: number
  joinType?: 'OPEN' | 'NEED_APPROVAL' | 'INVITE_ONLY'
  muteAll?: boolean
  isDeleted?: boolean
  createTime?: string
  updateTime?: string
}

/**
 * 群组成员角色
 */
export enum GroupMemberRole {
  OWNER = 'OWNER',
  ADMIN = 'ADMIN',
  MEMBER = 'MEMBER'
}

/**
 * 群组成员
 */
export interface GroupMember {
  id: number
  groupId: number
  userId: number
  user?: UserInfo
  role: GroupMemberRole
  joinTime?: string
  isMuted?: boolean
  muteEndTime?: string
}

// ==================== 联系人相关类型 ====================

/**
 * 好友关系
 */
export interface Friend {
  id: number
  userId: number
  friendId: number
  friend?: UserInfo
  remark?: string
  groupName?: string
  isDeleted?: boolean
  createTime?: string
}

/**
 * 联系人分组
 */
export interface ContactGroup {
  name: string
  count: number
  contacts: Friend[]
}

// ==================== API 响应类型 ====================

/**
 * 统一 API 响应结构
 */
export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

/**
 * 分页请求参数
 */
export interface PageRequest {
  pageNum?: number
  pageSize?: number
  orderByColumn?: string
  isAsc?: string
}

/**
 * 分页响应数据
 */
export interface PageResponse<T = any> {
  total: number
  rows: T[]
  code: number
  msg: string
}

// ==================== WebSocket 相关类型 ====================

/**
 * WebSocket 消息类型
 */
export enum WSMessageType {
  AUTH = 'auth',
  MESSAGE = 'message',
  READ = 'read',
  TYPING = 'typing',
  PING = 'ping',
  PONG = 'pong',
  NOTIFICATION = 'notification'
}

/**
 * WebSocket 消息结构
 */
export interface WSMessage {
  type: WSMessageType
  data: any
  timestamp?: number
}

/**
 * WebSocket 连接状态
 */
export enum WSConnectionState {
  CONNECTING = 0,
  OPEN = 1,
  CLOSING = 2,
  CLOSED = 3
}

// ==================== 通知相关类型 ====================

/**
 * 系统通知类型
 */
export enum NotificationType {
  SYSTEM = 'SYSTEM',
  FRIEND_REQUEST = 'FRIEND_REQUEST',
  GROUP_INVITE = 'GROUP_INVITE',
  MENTION = 'MENTION',
  ANNOUNCEMENT = 'ANNOUNCEMENT'
}

/**
 * 系统通知
 */
export interface SystemNotification {
  id: number
  userId: number
  type: NotificationType
  title: string
  content: string
  data?: any
  isRead?: boolean
  createTime?: string
}

// ==================== 文件相关类型 ====================

/**
 * 上传文件信息
 */
export interface UploadFileInfo {
  name: string
  size: number
  type: string
  url?: string
  progress?: number
  status?: 'uploading' | 'success' | 'error'
}

// ==================== 路由相关类型 ====================

/**
 * 路由元信息
 */
export interface RouteMeta {
  title?: string
  requiresAuth?: boolean
  roles?: string[]
  icon?: string
  hidden?: boolean
}

// ==================== 状态管理相关类型 ====================

/**
 * IM 状态
 */
export interface ImState {
  user: UserInfo | null
  conversations: Conversation[]
  currentConversation: Conversation | null
  messages: Record<number, Message[]>
  wsConnected: boolean
  typingUsers: Record<number, number[]>
}

// ==================== 工具类型 ====================

/**
 * 可选部分属性
 */
export type PartialOptional<T, K extends keyof T> = Omit<T, K> & Partial<Pick<T, K>>

/**
 * 必需部分属性
 */
export type PartialRequired<T, K extends keyof T> = Omit<T, K> & Required<Pick<T, K>>

/**
 * 深度只读
 */
export type DeepReadonly<T> = {
  readonly [P in keyof T]: T[P] extends object ? DeepReadonly<T[P]> : T[P]
}
