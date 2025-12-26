const getters = {
  // App
  sidebar: state => state.app.sidebar,
  size: state => state.app.size,
  device: state => state.app.device,
  language: state => state.app.language,

  // User
  token: state => state.user.token,
  avatar: state => state.user.avatar,
  name: state => state.user.name,
  roles: state => state.user.roles,
  permissions: state => state.user.permissions,

  // IM - 会话和消息
  currentSession: state => state.im?.currentSession,
  sessions: state => state.im?.sessions || [],
  sessionList: state => state.im?.sessions || [],
  messageList: state => state.im?.messageList || {},
  messagesBySession: state => sessionId => state.im?.messageList?.[sessionId] || [],
  unreadCount: state => state.im?.unreadCount || 0,
  totalUnreadCount: state => {
    const sessions = state.im?.sessions || []
    return sessions.reduce((total, s) => total + (s.unreadCount || 0), 0)
  },
  onlineStatus: state => state.im?.onlineStatus || {},
  wsConnected: state => state.im?.wsConnected ?? true,
  currentUserId: () => {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.userId || null
  },

  // Contact
  contactList: state => state.contact?.contactList || [],
  groupList: state => state.contact?.groupList || [],
}

export default getters
