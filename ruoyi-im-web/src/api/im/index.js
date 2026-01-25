/**
 * IM 模块 API 统一导出
 */
export * as messageApi from './message'
export * as conversationApi from './conversation'
export * as contactApi from './contact'
export * as groupApi from './group'
export * as userApi from './user'
export * as dingApi from './ding'
export * as botApi from './bot'

// 直接导出常用方法
export {
  sendMessage,
  getMessages,
  recallMessage,
  editMessage,
  deleteMessage,
  forwardMessage,
  searchMessages,
  markAsRead,
  getMessageReadUsers
} from './message'

export {
  getConversations,
  getConversation,
  createConversation,
  deleteConversation,
  pinConversation,
  muteConversation,
  markConversationAsRead
} from './conversation'

export {
  getContacts,
  searchContacts,
  getContact,
  sendFriendRequest,
  handleFriendRequest,
  getFriendRequests,
  updateContactRemark,
  deleteContact,
  moveContactToGroup
} from './contact'

export {
  getGroups,
  getGroup,
  createGroup,
  dismissGroup,
  leaveGroup,
  addGroupMembers,
  removeGroupMember,
  setGroupAdmin,
  transferGroupOwner,
  updateGroup,
  getGroupMembers,
  setGroupMute,
  muteGroupMember
} from './group'

// DING强提醒相关API
export {
  sendDing,
  queryDings,
  getDingDetail,
  markDingAsRead,
  batchMarkDingAsRead,
  cancelDing,
  getUnreadDingCount,
  getDingReadStatus
} from './ding'

// 群机器人相关API
export {
  createBot,
  updateBot,
  deleteBot,
  getGroupBots,
  getBotDetail,
  addBotRule,
  updateBotRule,
  deleteBotRule,
  setBotEnabled
} from './bot'
