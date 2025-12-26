// 导入所有接口模块
import * as messageApi from './message.js'
import * as sessionApi from './session.api.js'
import * as contactApi from './contact.js'
import * as groupApi from './group.js'
import * as auditApi from './audit.js'
import * as backupApi from './backup.js'
import * as mfaApi from './mfa.js'
import * as userApi from './user.js'
import * as notificationApi from './notification.js'
import imSocket from '@/utils/socket/imSocket.js'

// 统一导出所有接口
export {
  messageApi, // 消息相关接口
  sessionApi, // 会话相关接口
  contactApi, // 联系人相关接口
  groupApi, // 群组相关接口
  auditApi, // 安全审计相关接口
  backupApi, // 数据备份相关接口
  mfaApi, // 多因素认证相关接口
  userApi, // 用户相关接口
  notificationApi, // 推送通知相关接口
  imSocket, // WebSocket连接管理
}

// 默认导出所有接口的集合
export default {
  message: messageApi,
  session: sessionApi,
  contact: contactApi,
  group: groupApi,
  audit: auditApi,
  backup: backupApi,
  mfa: mfaApi,
  user: userApi,
  notification: notificationApi,
  socket: imSocket,
}

// 版本信息
export const version = '1.0.0'

// API基础配置
export const apiConfig = {
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 10000,
  withCredentials: true,
}

// WebSocket配置
export const wsConfig = {
  heartbeatInterval: 20000,
  reconnectAttempts: 10,
  reconnectDelay: 1000,
}

// 导出常量
export const constants = {
  // 消息类型
  MESSAGE_TYPE: {
    TEXT: 'text',
    IMAGE: 'image',
    FILE: 'file',
    VOICE: 'voice',
    VIDEO: 'video',
    LOCATION: 'location',
    SYSTEM: 'system',
  },
  // 会话类型
  SESSION_TYPE: {
    PRIVATE: 'private',
    GROUP: 'group',
    SYSTEM: 'system',
  },
  // 在线状态
  ONLINE_STATUS: {
    ONLINE: 'online',
    OFFLINE: 'offline',
    AWAY: 'away',
    BUSY: 'busy',
  },
  // 消息状态
  MESSAGE_STATUS: {
    SENDING: 'sending',
    SENT: 'sent',
    DELIVERED: 'delivered',
    READ: 'read',
    FAILED: 'failed',
  },
  // 群组角色
  GROUP_ROLE: {
    OWNER: 'owner',
    ADMIN: 'admin',
    MEMBER: 'member',
  },
  // 审计类型
  AUDIT_TYPE: {
    USER_ACTION: 'user_action',
    SYSTEM_EVENT: 'system_event',
    SECURITY_EVENT: 'security_event',
  },
  // MFA类型
  MFA_TYPE: {
    SMS: 'sms',
    EMAIL: 'email',
    AUTHENTICATOR: 'authenticator',
    BACKUP_CODE: 'backup_code',
  },
  // 用户状态
  USER_STATUS: {
    NORMAL: 'normal',
    DISABLED: 'disabled',
    LOCKED: 'locked',
  },
  // 通知级别
  NOTIFICATION_LEVEL: {
    ALL: 'all',
    MENTION: 'mention',
    NONE: 'none',
  },
  // 通知类型
  NOTIFICATION_TYPE: {
    MESSAGE: 'message',
    FRIEND_REQUEST: 'friend_request',
    GROUP_INVITE: 'group_invite',
    SYSTEM: 'system',
    ANNOUNCEMENT: 'announcement',
  },
  // 通知优先级
  NOTIFICATION_PRIORITY: {
    HIGH: 'high',
    MEDIUM: 'medium',
    LOW: 'low',
  },
  // 通知状态
  NOTIFICATION_STATUS: {
    UNREAD: 'unread',
    READ: 'read',
    PROCESSED: 'processed',
  },
  // 隐私级别
  PRIVACY_LEVEL: {
    PUBLIC: 'public',
    FRIENDS: 'friends',
    PRIVATE: 'private',
  },
}

// 导出工具函数
export const utils = {
  // 格式化日期
  formatDate(date) {
    return new Date(date).toLocaleString()
  },
  // 格式化文件大小
  formatFileSize(bytes) {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  },
  // 生成消息ID
  generateMessageId() {
    return `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  },
  // 检查文件类型
  checkFileType(filename) {
    const ext = filename.split('.').pop().toLowerCase()
    const imageTypes = ['jpg', 'jpeg', 'png', 'gif', 'bmp']
    const videoTypes = ['mp4', 'avi', 'mov', 'wmv']
    const audioTypes = ['mp3', 'wav', 'ogg']

    if (imageTypes.includes(ext)) return 'image'
    if (videoTypes.includes(ext)) return 'video'
    if (audioTypes.includes(ext)) return 'audio'
    return 'file'
  },
  // 检查是否是移动设备
  isMobile() {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(
      navigator.userAgent
    )
  },
  // 生成随机字符串
  generateRandomString(length = 16) {
    return Array.from(crypto.getRandomValues(new Uint8Array(length)))
      .map(b => b.toString(16).padStart(2, '0'))
      .join('')
  },
  // 敏感信息脱敏
  maskSensitiveInfo(info, type) {
    switch (type) {
      case 'phone':
        return info.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
      case 'email':
        return info.replace(/(.{3}).+(@.+)/, '$1***$2')
      case 'name':
        return info.replace(/(.{1}).+(.{1})/, '$1***$2')
      default:
        return info
    }
  },
  // 格式化通知内容
  formatNotification(notification) {
    const { type, content } = notification
    switch (type) {
      case 'message':
        return `新消息: ${content.length > 20 ? content.substr(0, 20) + '...' : content}`
      case 'friend_request':
        return '新的好友请求'
      case 'group_invite':
        return '新的群组邀请'
      case 'system':
        return '系统通知'
      default:
        return content
    }
  },
}
