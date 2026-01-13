/**
 * IM API 统一导出入口
 * @module api/im
 */
import * as messageApi from './message.js'
import * as conversationApi from './conversation.js'
import * as sessionApi from './session.js'
import * as contactApi from './contact.js'
import * as groupApi from './group.js'
import * as userApi from './user.js'
import * as notificationApi from './notification.js'
import * as groupMemberApi from './groupMember.js'
import * as appApi from './app.js'
import * as approvalApi from './approval.js'
import * as workbenchApi from './workbench.js'
import * as organizationApi from './organization.js'
import * as fileApi from './file.js'
import * as dingApi from './ding.js'
import * as videoCallApi from './video-call.js'
import * as documentApi from './document.js'
import * as emailApi from './email.js'
import * as mentionApi from './mention.js'
import * as externalContactApi from './external-contact.js'
import * as meetingRoomApi from './meeting-room.js'
import * as scheduleApi from './schedule.js'

// ========== 统一导出 ==========
export {
  messageApi,
  conversationApi,
  sessionApi,
  contactApi,
  groupApi,
  userApi,
  notificationApi,
  groupMemberApi,
  appApi,
  approvalApi,
  workbenchApi,
  organizationApi,
  fileApi,
  dingApi,
  videoCallApi,
  documentApi,
  emailApi,
  mentionApi,
  externalContactApi,
  meetingRoomApi,
  scheduleApi,
}

// ========== 默认导出 ==========
export default {
  message: messageApi,
  conversation: conversationApi,
  session: sessionApi,
  contact: contactApi,
  group: groupApi,
  user: userApi,
  notification: notificationApi,
  groupMember: groupMemberApi,
  app: appApi,
  approval: approvalApi,
  workbench: workbenchApi,
  organization: organizationApi,
  file: fileApi,
  ding: dingApi,
  videoCall: videoCallApi,
  document: documentApi,
  email: emailApi,
  mention: mentionApi,
  externalContact: externalContactApi,
  meetingRoom: meetingRoomApi,
  schedule: scheduleApi,
}

// ========== 常量定义 ==========
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
  CONVERSATION_TYPE: {
    PRIVATE: 'PRIVATE',
    GROUP: 'GROUP',
    SYSTEM: 'SYSTEM',
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

  // 用户状态
  USER_STATUS: {
    DISABLED: 0,
    ENABLED: 1,
  },

  // 性别
  GENDER: {
    UNKNOWN: 0,
    MALE: 1,
    FEMALE: 2,
  },

  // 审批状态
  APPROVAL_STATUS: {
    PENDING: 'PENDING',
    APPROVED: 'APPROVED',
    REJECTED: 'REJECTED',
    CANCELLED: 'CANCELLED',
  },

  // 审批操作
  APPROVAL_ACTION: {
    APPROVE: 'APPROVE',
    REJECT: 'REJECT',
    TRANSFER: 'TRANSFER',
    ADD_SIGN: 'ADD_SIGN',
  },

  // 好友请求状态
  FRIEND_REQUEST_STATUS: {
    PENDING: 'PENDING',
    APPROVED: 'APPROVED',
    REJECTED: 'REJECTED',
  },
}
