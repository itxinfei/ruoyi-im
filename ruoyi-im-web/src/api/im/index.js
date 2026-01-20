/**
 * IM 模块 API 统一导出
 */
export * as messageApi from './message'
export * as conversationApi from './conversation'
export * as contactApi from './contact'
export * as groupApi from './group'

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
  clearUnread
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
