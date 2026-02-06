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
export * as favoriteApi from './favorite'
export * as transcriptApi from './transcript'
export * as globalSearchApi from './globalSearch'
export * as markerApi from './marker'
export * as announcementApi from './announcement'

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

// 消息翻译相关API
export {
  translate,
  getSupportedLanguages,
  detectLanguage
} from './translation'

// 全局搜索相关API
export {
  globalSearch,
  globalSearchMessages,
  globalSearchContacts,
  globalSearchGroups,
  globalSearchFiles,
  searchWorkbench,
  getHotKeywords
} from './globalSearch'


// 消息标记相关API
export {
  markMessage,
  unmarkMessage,
  setTodoReminder,
  completeTodo,
  reopenTodo,
  getUserMarkers,
  getMessageMarkers,
  getUserTodoCount
} from './marker'

// 公告相关API
export {
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement,
  getAnnouncementDetail,
  getAnnouncementPage,
  getPinnedAnnouncements,
  getLatestAnnouncements,
  publishAnnouncement,
  withdrawAnnouncement,
  markAnnouncementAsRead,
  markAllAnnouncementsAsRead,
  toggleLikeAnnouncement,
  addAnnouncementComment,
  getAnnouncementStatistics,
  setAnnouncementPinned
} from './announcement'
