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
export * as translationApi from './translation'
export * as aiApi from './ai'
export * as videoCallApi from './videoCall'

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
  getMessageReadUsers,
  clearConversationMessages
} from './message'

export {
  getConversations,
  getConversation,
  createConversation,
  deleteConversation,
  pinConversation,
  muteConversation,
  markConversationAsRead,
  saveDraft
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
  muteGroupMember,
  unmuteGroupMember
} from './group'

// DING强提醒相关API
export {
  sendDing,
  getReceivedDings,
  getSentDings,
  getDingDetail,
  markDingAsRead,
  confirmDing,
  getDingReceipts,
  cancelDing,
  getDingTemplates,
  sendDingWithTemplate
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

// 消息翻译相关API
export {
  translate,
  getSupportedLanguages,
  detectLanguage
} from './translation'

// AI助手相关API
export {
  chat,
  summarize,
  clearConversation,
  getSupportedModels
} from './ai'

// 视频通话相关API
export {
  initiateCall,
  acceptCall,
  rejectCall,
  endCall,
  getCallInfo,
  getActiveCall,
  sendSignal,
  getCallHistory,
  initiateGroupCall,
  joinGroupCall,
  leaveGroupCall,
  getCallParticipants,
  toggleMute,
  toggleCamera
} from './videoCall'
