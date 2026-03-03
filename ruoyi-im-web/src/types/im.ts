/**
 * IM系统类型定义
 * 与后端Java实体类保持同步
 */

// ==================== 基础类型 ====================

/** 消息类型 */
export type MessageType = 'TEXT' | 'IMAGE' | 'VIDEO' | 'AUDIO' | 'FILE' | 'SYSTEM' | 'LOCATION' | 'CARD' | 'LINK'

/** 消息发送状态 */
export type MessageSendStatus = 'PENDING' | 'SENDING' | 'SENT' | 'DELIVERED' | 'FAILED'

/** 会话类型 */
export type ConversationType = 'PRIVATE' | 'GROUP'

/** 用户角色 */
export type UserRole = 'USER' | 'ADMIN' | 'SUPER_ADMIN'

/** 敏感级别 */
export type SensitiveLevel = 'NORMAL' | 'CONFIDENTIAL' | 'SECRET'

// ==================== 用户相关 ====================

/** 用户信息 */
export interface User {
  id: number
  username: string
  nickname: string
  avatar: string
  gender: 0 | 1 | 2 // 0:未知 1:男 2:女
  mobile?: string
  email?: string
  status: 0 | 1 // 0:禁用 1:启用
  role: UserRole
  signature?: string
  department?: string
  position?: string
  lastOnlineTime?: string
  birthday?: string
  online?: boolean
}

/** 用户设置 */
export interface UserSetting {
  id: number
  userId: number
  theme?: string
  language?: string
  notificationEnabled: boolean
  soundEnabled: boolean
  desktopNotificationEnabled: boolean
  fontSize?: number
  chatBackground?: string
}

// ==================== 会话相关 ====================

/** 会话信息 */
export interface Conversation {
  id: number
  type: ConversationType
  targetId?: number
  name?: string
  avatar?: string
  lastMessageId?: number
  lastMessageTime?: string
  isDeleted: 0 | 1
  deletedTime?: string
  isSensitive?: 0 | 1
  sensitiveLevel?: SensitiveLevel
  noForward?: 0 | 1
  noCopy?: 0 | 1
}

/** 会话成员信息 */
export interface ConversationMember {
  id: number
  conversationId: number
  userId: number
  nickname?: string
  role: 'OWNER' | 'ADMIN' | 'MEMBER'
  unreadCount: number
  isPinned: 0 | 1
  isMuted: 0 | 1
  lastReadMessageId?: number
  lastReadTime?: string
  isDeleted: 0 | 1
  deletedTime?: string
  createTime: string
  updateTime: string
  isArchived?: 0 | 1
  pinnedTime?: string
  draftContent?: string
  draftTime?: string
}

/** 用户会话状态（用于会话列表） */
export interface UserSession {
  id: number
  userId: number
  conversationId: number
  isPinned: 0 | 1
  pinnedTime?: string
  isMuted: 0 | 1
  isArchived: 0 | 1
  unreadCount: number
  lastReadMessageId?: number
  draftContent?: string
  draftTime?: string
  isDeleted: 0 | 1
  deletedTime?: string
  createTime: string
  updateTime: string
  // 扩展字段
  conversation?: Conversation
  lastMessage?: Message
  conversationName?: string
  conversationAvatar?: string
  conversationType?: ConversationType
  targetId?: number
}

// ==================== 消息相关 ====================

/** 消息信息 */
export interface Message {
  id: number
  clientMsgId?: string
  sendStatus: MessageSendStatus
  sendRetryCount: number
  sendErrorCode?: string
  sendErrorMsg?: string
  deliveredTime?: string
  conversationId: number
  senderId: number
  messageType: MessageType
  content?: string
  fileUrl?: string
  fileName?: string
  fileSize?: number
  sensitiveLevel?: string
  isRevoked: 0 | 1
  revokedTime?: string
  revokerId?: number
  isEdited: 0 | 1
  editedContent?: string
  editCount: number
  editTime?: string
  createTime: string
  replyToMessageId?: number
  forwardFromMessageId?: number
  isDeleted: 0 | 1
  deletedTime?: string
  version?: number
  // 扩展字段
  sender?: User
  replyToMessage?: Message
}

/** 消息已读详情 */
export interface MessageReadDetail {
  id: number
  messageId: number
  conversationId: number
  userId: number
  readTime: string
  createTime: string
  // 扩展字段
  user?: User
  nickname?: string
  avatar?: string
}

/** 消息反应（点赞等） */
export interface MessageReaction {
  id: number
  messageId: number
  userId: number
  reactionType: string // 'LIKE' | 'LOVE' | 'LAUGH' | 'WOW' | 'SAD' | 'ANGRY'
  createTime: string
  // 扩展字段
  user?: User
}

/** 消息提及（@某人） */
export interface MessageMention {
  id: number
  messageId: number
  conversationId: number
  mentionedUserId: number
  mentionedByUserId: number
  mentionType: 'SINGLE' | 'ALL' // 单人@ 或 @所有人
  isNotified: 0 | 1
  createTime: string
}

/** 消息收藏 */
export interface MessageFavorite {
  id: number
  userId: number
  messageId: number
  conversationId: number
  favoriteGroupId?: number
  remark?: string
  createTime: string
  // 扩展字段
  message?: Message
}

// ==================== 输入状态 ====================

/** 输入状态 */
export interface TypingStatus {
  id: number
  conversationId: number
  userId: number
  typingType: 'TEXT' | 'VOICE'
  createTime: string
  updateTime: string
  // 扩展字段
  user?: User
  nickname?: string
  avatar?: string
}

// ==================== 群组相关 ====================

/** 群组信息 */
export interface Group {
  id: number
  name: string
  avatar?: string
  ownerId: number
  description?: string
  maxMembers: number
  allMuted: 0 | 1
  qrcodeUrl?: string
  qrcodeExpireTime?: string
  allowUpload: 0 | 1
  showMemberList: 0 | 1
  isDeleted: 0 | 1
  deletedTime?: string
  createTime: string
  updateTime: string
  // 扩展字段
  owner?: User
  memberCount?: number
}

/** 群成员信息 */
export interface GroupMember {
  id: number
  groupId: number
  userId: number
  nickname?: string
  role: 'OWNER' | 'ADMIN' | 'MEMBER'
  joinTime: string
  inviteUserId?: number
  isMuted: 0 | 1
  muteExpireTime?: string
  isBanned: 0 | 1
  banExpireTime?: string
  isDeleted: 0 | 1
  deletedTime?: string
  createTime: string
  updateTime: string
  // 扩展字段
  user?: User
}

/** 群公告 */
export interface GroupAnnouncement {
  id: number
  groupId: number
  senderId: number
  content: string
  type: 1 | 2 | 3 // 1:普通公告 2:系统公告 3:活动通知
  attachmentUrl?: string
  isPinned: 0 | 1
  status: 0 | 1 // 0:已撤回 1:正常
  createTime: string
  updateTime: string
  expireTime?: string
  // 扩展字段
  sender?: User
  readCount?: number
}

// ==================== 好友相关 ====================

/** 好友关系 */
export interface Friend {
  id: number
  userId: number
  friendId: number
  remark?: string
  groupId?: number
  isBlocked: 0 | 1
  isStar: 0 | 1
  createTime: string
  updateTime: string
  // 扩展字段
  friend?: User
}

/** 好友请求 */
export interface FriendRequest {
  id: number
  fromUserId: number
  toUserId: number
  requestMessage?: string
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED'
  handleTime?: string
  handlerId?: number
  createTime: string
  updateTime: string
  // 扩展字段
  fromUser?: User
  toUser?: User
}

// ==================== 文件相关 ====================

/** 文件资源 */
export interface FileAsset {
  id: number
  fileName: string
  fileUrl: string
  fileSize: number
  fileType: string
  mimeType: string
  md5?: string
  uploaderId: number
  uploadTime: string
  expireTime?: string
  isDeleted: 0 | 1
  deletedTime?: string
  // 扩展字段
  uploader?: User
}

// ==================== API响应类型 ====================

/** 分页请求参数 */
export interface PageParams {
  pageNum: number
  pageSize: number
}

/** 分页响应 */
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

/** API响应 */
export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

/** WebSocket消息 */
export interface WsMessage<T = any> {
  type: string
  data: T
  timestamp: number
}