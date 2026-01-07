// 导出所有接口模块
import * as messageApi from './message.js'
import * as sessionApi from './session.js'
import * as contactApi from './contact.js'
import * as groupApi from './group.js'
import * as auditApi from './audit.js'
import * as backupApi from './backup.js'
import * as mfaApi from './mfa.js'
import * as userApi from './user.js'
import * as notificationApi from './notification.js'
import * as conversationApi from './conversation.js'

// 统一导出
export {
  messageApi,
  sessionApi,
  contactApi,
  groupApi,
  auditApi,
  backupApi,
  mfaApi,
  userApi,
  notificationApi,
  conversationApi,
}

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
  conversation: conversationApi,
}

// 常量定义
export const constants = {
  MESSAGE_TYPE: {
    TEXT: 'text',
    IMAGE: 'image',
    FILE: 'file',
    VOICE: 'voice',
    VIDEO: 'video',
    LOCATION: 'location',
    SYSTEM: 'system',
  },
  SESSION_TYPE: {
    PRIVATE: 'private',
    GROUP: 'group',
    SYSTEM: 'system',
  },
  ONLINE_STATUS: {
    ONLINE: 'online',
    OFFLINE: 'offline',
    AWAY: 'away',
    BUSY: 'busy',
  },
  MESSAGE_STATUS: {
    SENDING: 'sending',
    SENT: 'sent',
    DELIVERED: 'delivered',
    READ: 'read',
    FAILED: 'failed',
  },
}
