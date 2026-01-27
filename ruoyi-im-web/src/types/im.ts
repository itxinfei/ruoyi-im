/**
 * IM 系统全局类型定义
 * 为 Vue 组件和 API 提供类型支持
 * @author ruoyi
 */

// ==================== 用户相关类型 ====================

/**
 * 用户信息
 */
export interface ImUser {
  id: number
  username: string
  nickname?: string
  avatar?: string
  mobile?: string
  email?: string
  status: number // 0=禁用 1=启用
  signature?: string
  gender?: number // 0=未知 1=男 2=女
  birthday?: string
  createTime?: string
  updateTime?: string
}

/**
 * 用户状态
 */
export interface UserStatus {
  [userId: number]: 'online' | 'offline' | 'away' | 'busy'
}

// ==================== 会话相关类型 ====================

/**
 * 会话类型
 */
export type ConversationType = 'PRIVATE' | 'GROUP'

/**
 * 会话信息
 */
export interface ImConversation {
  id: number | string
  type: ConversationType
  targetId: number
  name: string
  avatar?: string
  lastMessage?: string
  lastMessageTime?: string
  lastMessageId?: number
  unreadCount?: number
  isPinned?: boolean
  isMuted?: boolean
  peerOnline?: boolean
  memberCount?: number
  hasMention?: boolean
  lastSenderNickname?: string
}

/**
 * 会话成员
 */
export interface ImConversationMember {
  id: number
  conversationId: number
  userId: number
  role?: string // OWNER, ADMIN, MEMBER
  unreadCount?: number
  isPinned?: boolean
  isMuted?: boolean
  lastReadMessageId?: number
  lastReadTime?: string
  joinTime?: string
  [key: string]: any
}

// ==================== 消息相关类型 ====================

/**
 * 消息类型
 */
export type MessageType = 'TEXT' | 'IMAGE' | 'FILE' | 'VOICE' | 'VIDEO' | 'RECALLED' | 'NOTIFICATION'

/**
 * 消息内容
 */
export interface ImMessage {
  id: number
  conversationId: number
  senderId: number
  senderNickname?: string
  senderAvatar?: string
  type: MessageType
  content: string
  timestamp: number
  isOwn?: boolean
  isRead?: boolean
  readCount?: number
  isRevoked?: boolean
  isEdited?: boolean
  replyToMessageId?: number
  replyToContent?: string
  replyToSenderId?: number
  forwardFromMessageId?: number
  forwardFromConversationId?: number
  reactions?: MessageReaction[]
  extras?: MessageExtra
}

/**
 * 消息额外信息
 */
export interface MessageExtra {
  fileName?: string
  fileSize?: number
  fileUrl?: string
  duration?: number
  width?: number
  height?: number
  thumbnailUrl?: string
}

/**
 * 表情反应
 */
export interface MessageReaction {
  id: number
  messageId: number
  userId: number
  emoji: string
  createTime: string
  [key: string]: any
}

/**
 * 消息已读状态
 */
export interface MessageReadStatus {
  messageId: number
  readCount: number
  unreadCount: number
  readUsers?: ReadUser[]
}

/**
 * 已读用户
 */
export interface ReadUser {
  id: number
  name: string
  avatar?: string
  readTime?: string
}

// ==================== 群组相关类型 ====================

/**
 * 群组信息
 */
export interface ImGroup {
  id: number
  groupName: string
  avatar?: string
  description?: string
  ownerId: number
  memberCount?: number
  maxMembers?: number
  type?: string // NORMAL, SUPER, DISCUSSION
  isMuted?: boolean
  allowInvites?: boolean
  allowMembersModify?: boolean
  createdTime?: string
  updatedTime?: string
}

/**
 * 群组成员角色
 */
export type GroupMemberRole = 'OWNER' | 'ADMIN' | 'MEMBER'

/**
 * 群组成员
 */
export interface ImGroupMember {
  id: number
  groupId: number
  userId: number
  role: GroupMemberRole
  joinTime?: string
  nickname?: string
  [key: string]: any
}

/**
 * 群组设置
 */
export interface ImGroupSettings {
  isMuted: boolean
  allowInvites: boolean
  allowMembersModify: boolean
  [key: string]: any
}

// ==================== 好友相关类型 ====================

/**
 * 好友关系
 */
export interface ImFriend {
  id: number
  userId: number
  friendId: number
  remark?: string
  groupName?: string
  isDeleted?: boolean
  createTime?: string
  updateTime?: string
}

/**
 * 好友信息（含用户详情）
 */
export interface ImFriendVO {
  id: number
  userId: number
  friendId: number
  remark?: string
  groupName?: string
  user?: ImUser
  [key: string]: any
}

// ==================== API 响应类型 ====================

/**
 * API 响应结果
 */
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
  timestamp?: number
}

/**
 * 分页请求参数
 */
export interface PageRequest {
  pageNum?: number
  pageSize?: number
  keyword?: string
  [key: string]: any
}

/**
 * 分页响应结果
 */
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

// ==================== WebSocket 相关类型 ====================

/**
 * WebSocket 消息类型
 */
export type WsMessageType = 'auth' | 'message' | 'ping' | 'pong' | 'read' | 'typing' | 'reaction' | 'notice' | 'presence'

/**
 * WebSocket 消息
 */
export interface WsMessage {
  type: WsMessageType
  data?: any
  timestamp?: number
  [key: string]: any
}

/**
 * 聊天输入状态
 */
export interface TypingIndicator {
  conversationId: number
  userId: number
  timestamp: number
}

// ==================== 系统设置类型 ====================

/**
 * 用户设置
 */
export interface UserSettings {
  notifications: NotificationSettings
  privacy: PrivacySettings
  general: GeneralSettings
  shortcuts: ShortcutSettings
  appearance: AppearanceSettings
}

/**
 * 通知设置
 */
export interface NotificationSettings {
  enabled: boolean
  sound: boolean
  vibration: boolean
  showPreview: boolean
  mentionOnly: boolean
}

/**
 * 隐私设置
 */
export interface PrivacySettings {
  showOnline: boolean
  showReadReceipt: boolean
  allowSearchByMobile: boolean
  allowAddByFriend: boolean
}

/**
 * 通用设置
 */
export interface GeneralSettings {
  language: string
  theme: 'auto' | 'light' | 'dark'
  fontSize: 'small' | 'medium' 'large'
}

/**
 * 快捷键设置
 */
export interface ShortcutSettings {
  send: 'enter' | 'ctrl-enter'
  screenshot: string
}

/**
 * 外观设置
 */
export interface AppearanceSettings {
  theme: 'auto' | 'light' | 'dark'
  sidebarCollapsed: boolean
  messageDensity: 'comfortable' | 'compact'
}

// ==================== 文件相关类型 ====================

/**
 * 文件信息
 */
export interface FileInfo {
  id: number
  fileName: string
  fileSize: number
  fileUrl: string
  fileType: string
  uploaderId: number
  uploadTime: string
}

/**
 * 文件上传进度
 */
export interface UploadProgress {
  file: File
  progress: number
  status: 'pending' | 'uploading' | 'success' | 'error'
  error?: string
  response?: any
}

// ==================== 搜索相关类型 ====================

/**
 * 搜索结果类型
 */
export type SearchType = 'all' | 'message' | 'contact' | 'group' | 'file'

/**
 * 全局搜索结果
 */
export interface SearchResult {
  type: SearchType
  id: number | string
  title: string
  content?: string
  avatar?: string
  time?: string
  conversationId?: number
  [key: string]: any
}

// ==================== 通知类型 ====================

/**
 * 通知类型
 */
export type NotificationType = 'message' | 'mention' | 'system' | 'friend' | 'group' | 'approval'

/**
 * 系统通知
 */
export interface SystemNotification {
  id: number
  type: NotificationType
  title: string
  content: string
  data?: any
  isRead: boolean
  createTime: string
}

// ==================== 状态管理类型 ====================

/**
 * Vuex Store 状态
 */
export interface ImState {
  user: {
    currentUser: ImUser | null
    isLoggedIn: boolean
  }
  session: {
    sessions: ImConversation[]
    currentSession: ImConversation | null
    loading: boolean
  }
  contact: {
    friends: ImFriendVO[]
    userStatus: UserStatus
  }
  settings: UserSettings
  chat: {
    messages: Record<number, ImMessage[]>
    typingUsers: Record<number, number>
  }
  ws: {
    connected: boolean
    reconnectAttempts: number
  }
}

// ==================== 组件 Props 类型 ====================

/**
 * 聊天面板 Props
 */
export interface ChatPanelProps {
  session: ImConversation | null
  height?: string | number
}

/**
 * 消息列表 Props
 */
export interface MessageListProps {
  messages: ImMessage[]
  currentUser: ImUser
  sessionType: ConversationType
  loading?: boolean
}

/**
 * 会话列表面板 Props
 */
export interface SessionPanelProps {
  currentSession: ImConversation | null
}

// ==================== 事件类型 ====================

/**
 * 组件事件
 */
export interface ChatEmits {
  send: (content: string) => void
  uploadImage: (file: FormData) => void
  uploadFile: (file: FormData) => void
  reply: (message: ImMessage) => void
  delete: (messageId: number) => void
  recall: (messageId: number) => void
  edit: (messageId: number, newContent: string) => void
  showUser: (userId: number) => void
}

// ==================== 工具类型 ====================

/**
 * 键值对
 */
export type KeyValuePair<K = string, V = any> = {
  key: K
  value: V
}

/**
 * 下拉选项
 */
export interface SelectOption {
  label: string
  value: any
  disabled?: boolean
  children?: SelectOption[]
}

/**
 * 分页配置
 */
export interface PaginationConfig {
  current: number
  pageSize: number
  total: number
  showSizeChanger?: boolean
  showQuickJumper?: boolean
}
