/**
 * @description 全局常量定义
 * @author RuoYi
 */

/**
 * 时间相关常量（毫秒）
 */
export const TIME = {
  /** 一秒 */
  SECOND: 1000,
  /** 一分钟 */
  MINUTE: 60 * 1000,
  /** 一小时 */
  HOUR: 60 * 60 * 1000,
  /** 一天 */
  DAY: 24 * 60 * 60 * 1000,
  /** 一周 */
  WEEK: 7 * 24 * 60 * 60 * 1000,
}

/**
 * 文件大小限制（字节）
 */
export const FILE_SIZE = {
  /** 1KB */
  KB: 1024,
  /** 1MB */
  MB: 1024 * 1024,
  /** 10MB */
  LIMIT_10MB: 10 * 1024 * 1024,
  /** 100MB */
  LIMIT_100MB: 100 * 1024 * 1024,
  /** 500MB */
  LIMIT_500MB: 500 * 1024 * 1024,
}

/**
 * 消息相关常量
 */
export const MESSAGE = {
  /** 消息撤回时间限制（2分钟） */
  RECALL_TIME_LIMIT: 2 * 60 * 1000,
  /** 每个会话最大消息数 */
  MAX_PER_SESSION: 500,
  /** 文本消息最大长度 */
  MAX_TEXT_LENGTH: 1000,
}

/**
 * 通话相关常量
 */
export const CALL = {
  /** 最大通话时长（秒） */
  MAX_DURATION: 60 * 60,
  /** 最小录音时长（秒） */
  MIN_RECORD_DURATION: 1,
  /** 最大录音时长（秒） */
  MAX_RECORD_DURATION: 60,
}

/**
 * 分页相关常量
 */
export const PAGE = {
  /** 默认页码 */
  DEFAULT_PAGE: 1,
  /** 默认每页条数 */
  DEFAULT_SIZE: 20,
  /** 每页最大条数 */
  MAX_SIZE: 100,
}

/**
 * 状态码
 */
export const STATUS_CODE = {
  /** 成功 */
  SUCCESS: 200,
  /** 未授权 */
  UNAUTHORIZED: 401,
  /** 禁止访问 */
  FORBIDDEN: 403,
  /** 未找到 */
  NOT_FOUND: 404,
  /** 服务器错误 */
  SERVER_ERROR: 500,
}

/**
 * 本地存储键名
 */
export const STORAGE_KEY = {
  /** Token */
  TOKEN: 'Admin-Token',
  /** 用户信息 */
  USER_INFO: 'userInfo',
  /** 语言设置 */
  LANGUAGE: 'language',
  /** 主题设置 */
  THEME: 'theme',
  /** 侧边栏状态 */
  SIDEBAR_STATUS: 'sidebarStatus',
}

/**
 * WebSocket 事件类型
 */
export const WS_EVENT = {
  /** 连接打开 */
  OPEN: 'open',
  /** 连接关闭 */
  CLOSE: 'close',
  /** 错误 */
  ERROR: 'error',
  /** 消息 */
  MESSAGE: 'message',
  /** 心跳 */
  PING: 'ping',
  /** 心跳响应 */
  PONG: 'pong',
}

/**
 * 消息类型
 */
export const MESSAGE_TYPE = {
  /** 文本消息 */
  TEXT: 'text',
  /** 图片消息 */
  IMAGE: 'image',
  /** 文件消息 */
  FILE: 'file',
  /** 语音消息 */
  VOICE: 'voice',
  /** 视频消息 */
  VIDEO: 'video',
  /** 位置消息 */
  LOCATION: 'location',
  /** 系统消息 */
  SYSTEM: 'system',
  /** 引用消息 */
  QUOTE: 'quote',
  /** 投票消息 */
  VOTE: 'vote',
}

/**
 * 会话类型
 */
export const SESSION_TYPE = {
  /** 私聊 */
  PRIVATE: 'private',
  /** 群聊 */
  GROUP: 'group',
  /** 系统通知 */
  SYSTEM: 'system',
}

/**
 * 用户状态
 */
export const USER_STATUS = {
  /** 在线 */
  ONLINE: 'online',
  /** 离线 */
  OFFLINE: 'offline',
  /** 忙碌 */
  BUSY: 'busy',
  /** 隐身 */
  INVISIBLE: 'invisible',
}

/**
 * 群组成员角色
 */
export const GROUP_ROLE = {
  /** 群主 */
  OWNER: 'owner',
  /** 管理员 */
  ADMIN: 'admin',
  /** 普通成员 */
  MEMBER: 'member',
}

/**
 * 好友请求状态
 */
export const FRIEND_REQUEST_STATUS = {
  /** 待处理 */
  PENDING: 'pending',
  /** 已同意 */
  ACCEPTED: 'accepted',
  /** 已拒绝 */
  REJECTED: 'rejected',
}
