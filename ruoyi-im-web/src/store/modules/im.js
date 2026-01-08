import {
  sendMessage as apiSendMessage,
  listMessage,
  recallMessage as apiRecallMessage,
  forwardMessage as apiForwardMessage,
  searchMessages as apiSearchMessages,
} from '@/api/im/message'
import { listSession, updateSession, deleteSession as apiDeleteSession } from '@/api/im/session'
import { ElMessage } from 'element-plus'

// 消息列表最大条数限制
const MAX_MESSAGES_PER_SESSION = 500
// 离线消息存储Key
const OFFLINE_MESSAGES_KEY = 'im_offline_messages'
const MESSAGE_CACHE_KEY = 'im_message_cache'

/**
 * 从本地存储加载离线消息
 */
function loadOfflineMessages() {
  try {
    const data = localStorage.getItem(OFFLINE_MESSAGES_KEY)
    return data ? JSON.parse(data) : {}
  } catch (e) {
    console.error('加载离线消息失败:', e)
    return {}
  }
}

/**
 * 保存离线消息到本地存储
 */
function saveOfflineMessages(messages) {
  try {
    localStorage.setItem(OFFLINE_MESSAGES_KEY, JSON.stringify(messages))
  } catch (e) {
    console.error('保存离线消息失败:', e)
  }
}

/**
 * 从本地存储加载消息缓存
 */
function loadMessageCache() {
  try {
    const data = localStorage.getItem(MESSAGE_CACHE_KEY)
    return data ? JSON.parse(data) : {}
  } catch (e) {
    console.error('加载消息缓存失败:', e)
    return {}
  }
}

/**
 * 保存消息缓存到本地存储
 */
function saveMessageCache(messageList) {
  try {
    // 只缓存每个会话最近的100条消息
    const cacheData = {}
    for (const sessionId in messageList) {
      const messages = messageList[sessionId]
      cacheData[sessionId] = messages.slice(-100)
    }
    localStorage.setItem(MESSAGE_CACHE_KEY, JSON.stringify(cacheData))
  } catch (e) {
    console.error('保存消息缓存失败:', e)
  }
}

/**
 * 生成消息唯一ID（用于去重）
 */
function generateMessageKey(message) {
  // 优先使用clientMsgId，其次使用id
  return (
    message.clientMsgId ||
    message.id ||
    `${message.senderId}_${message.timestamp}_${message.content?.substring?.(0, 20) || ''}`
  )
}

const state = {
  currentSession: null,
  sessions: [],
  messageList: loadMessageCache(), // 从缓存加载
  unreadCount: 0,
  onlineStatus: {},
  // 发送中的消息
  pendingMessages: loadOfflineMessages(), // 从离线存储加载
  // WebSocket连接状态
  wsConnected: false,
  // 消息ID集合（用于快速去重）
  messageIdSet: {},
  // 联系人列表
  contacts: [],
  // 群组列表
  groups: [],
  // 文件列表
  files: [],
}

const getters = {
  // 根据会话ID获取消息列表
  messagesBySession: state => sessionId => {
    return state.messageList[sessionId] || []
  },
  // 当前用户ID
  currentUserId: () => {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.userId || null
  },
  // 获取会话列表
  sessionList: state => state.sessions,
  // 当前会话
  currentSession: state => state.currentSession,
  // 未读消息总数
  totalUnreadCount: state => {
    return state.sessions.reduce((total, session) => total + (session.unreadCount || 0), 0)
  },
}

const mutations = {
  SET_CURRENT_SESSION: (state, session) => {
    state.currentSession = session
  },
  SET_SESSIONS: (state, list) => {
    state.sessions = list
  },
  ADD_SESSION: (state, session) => {
    const exists = state.sessions.find(s => s.id === session.id)
    if (!exists) {
      state.sessions.unshift(session)
    }
  },
  UPDATE_SESSION: (state, { sessionId, updates }) => {
    const index = state.sessions.findIndex(s => s.id === sessionId)
    if (index !== -1) {
      state.sessions[index] = { ...state.sessions[index], ...updates }
    }
  },
  REMOVE_SESSION: (state, sessionId) => {
    state.sessions = state.sessions.filter(s => s.id !== sessionId)
    // 清理相关消息
    delete state.messageList[sessionId]
    delete state.messageIdSet[sessionId]
    delete state.pendingMessages[sessionId]
    // 更新缓存
    saveMessageCache(state.messageList)
  },
  SET_MESSAGE_LIST: (state, { sessionId, messages }) => {
    // 初始化消息ID集合
    if (!state.messageIdSet[sessionId]) {
      state.messageIdSet[sessionId] = new Set()
    }
    // 清空旧数据
    state.messageIdSet[sessionId].clear()
    // 添加新消息并记录ID
    const uniqueMessages = []
    for (const msg of messages) {
      const key = generateMessageKey(msg)
      if (!state.messageIdSet[sessionId].has(key)) {
        state.messageIdSet[sessionId].add(key)
        uniqueMessages.push(msg)
      }
    }
    state.messageList[sessionId] = uniqueMessages
    // 更新缓存
    saveMessageCache(state.messageList)
  },
  ADD_MESSAGE: (state, { sessionId, message }) => {
    if (!state.messageList[sessionId]) {
      state.messageList[sessionId] = []
    }
    if (!state.messageIdSet[sessionId]) {
      state.messageIdSet[sessionId] = new Set()
    }

    // 生成消息唯一标识用于去重
    const key = generateMessageKey(message)

    // 检查是否已存在（去重）
    if (state.messageIdSet[sessionId].has(key)) {
      return // 消息已存在，跳过
    }

    // 添加消息
    state.messageIdSet[sessionId].add(key)
    state.messageList[sessionId].push(message)

    // 超过上限时移除最旧的消息
    if (state.messageList[sessionId].length > MAX_MESSAGES_PER_SESSION) {
      const removed = state.messageList[sessionId].shift()
      const removedKey = generateMessageKey(removed)
      state.messageIdSet[sessionId].delete(removedKey)
    }

    // 更新缓存
    saveMessageCache(state.messageList)
  },
  UPDATE_MESSAGE: (state, { sessionId, messageId, updates }) => {
    if (state.messageList[sessionId]) {
      const index = state.messageList[sessionId].findIndex(
        m => m.id === messageId || m.clientMsgId === messageId
      )
      if (index !== -1) {
        const oldMessage = state.messageList[sessionId][index]
        const newMessage = { ...oldMessage, ...updates }

        // 如果ID变化，更新ID集合
        if (updates.id && updates.id !== oldMessage.id) {
          const oldKey = generateMessageKey(oldMessage)
          const newKey = generateMessageKey(newMessage)
          state.messageIdSet[sessionId].delete(oldKey)
          state.messageIdSet[sessionId].add(newKey)
        }

        state.messageList[sessionId][index] = newMessage
        // 更新缓存
        saveMessageCache(state.messageList)
      }
    }
  },
  PREPEND_MESSAGES: (state, { sessionId, messages }) => {
    if (!state.messageList[sessionId]) {
      state.messageList[sessionId] = []
    }
    if (!state.messageIdSet[sessionId]) {
      state.messageIdSet[sessionId] = new Set()
    }

    // 去重后的消息
    const uniqueMessages = []
    for (const msg of messages) {
      const key = generateMessageKey(msg)
      if (!state.messageIdSet[sessionId].has(key)) {
        state.messageIdSet[sessionId].add(key)
        uniqueMessages.push(msg)
      }
    }

    state.messageList[sessionId] = [...uniqueMessages, ...state.messageList[sessionId]]

    // 超过上限时移除最旧的消息（从末尾移除，保留最新的）
    while (state.messageList[sessionId].length > MAX_MESSAGES_PER_SESSION) {
      const removed = state.messageList[sessionId].pop()
      const removedKey = generateMessageKey(removed)
      state.messageIdSet[sessionId].delete(removedKey)
    }

    // 更新缓存
    saveMessageCache(state.messageList)
  },
  SET_UNREAD_COUNT: (state, count) => {
    state.unreadCount = count
  },
  SET_ONLINE_STATUS: (state, { userId, status }) => {
    state.onlineStatus[userId] = status
  },
  SET_WS_CONNECTED: (state, status) => {
    state.wsConnected = status
  },
  ADD_PENDING_MESSAGE: (state, { sessionId, tempId, message }) => {
    if (!state.pendingMessages[sessionId]) {
      state.pendingMessages[sessionId] = {}
    }
    state.pendingMessages[sessionId][tempId] = message
    // 保存到离线存储
    saveOfflineMessages(state.pendingMessages)
  },
  REMOVE_PENDING_MESSAGE: (state, { sessionId, tempId }) => {
    if (state.pendingMessages[sessionId]) {
      delete state.pendingMessages[sessionId][tempId]
      // 更新离线存储
      saveOfflineMessages(state.pendingMessages)
    }
  },
  CLEAR_SESSION_UNREAD: (state, sessionId) => {
    const session = state.sessions.find(s => s.id === sessionId)
    if (session) {
      session.unreadCount = 0
    }
  },
  // 清理过期的消息缓存
  CLEAR_OLD_MESSAGES: (state, { sessionId, keepCount = 200 }) => {
    if (state.messageList[sessionId] && state.messageList[sessionId].length > keepCount) {
      const removed = state.messageList[sessionId].splice(
        0,
        state.messageList[sessionId].length - keepCount
      )
      // 清理ID集合
      for (const msg of removed) {
        const key = generateMessageKey(msg)
        state.messageIdSet[sessionId]?.delete(key)
      }
      saveMessageCache(state.messageList)
    }
  },
  SET_CONTACTS: (state, contacts) => {
    state.contacts = contacts
  },
  SET_GROUPS: (state, groups) => {
    state.groups = groups
  },
  SET_FILES: (state, files) => {
    state.files = files
  },
}

const actions = {
  // 加载会话列表
  async loadSessions({ commit }) {
    const response = await listSession()
    const sessions = response.rows || response.data || []
    commit('SET_SESSIONS', sessions)
    return sessions
  },

  // 切换会话
  async switchSession({ commit, dispatch }, session) {
    commit('SET_CURRENT_SESSION', session)
    commit('CLEAR_SESSION_UNREAD', session.id)
    // 加载该会话的消息
    await dispatch('loadMessages', { sessionId: session.id, page: 1, pageSize: 20 })
  },

  // 加载消息
  async loadMessages({ commit }, { sessionId, page = 1, pageSize = 20 }) {
    const response = await listMessage({ conversationId: sessionId, page, pageSize })
    const messages = response.rows || response.data || []

    if (page === 1) {
      commit('SET_MESSAGE_LIST', { sessionId, messages })
    } else {
      commit('PREPEND_MESSAGES', { sessionId, messages })
    }

    return {
      messages,
      hasMore: messages.length === pageSize,
    }
  },

  // 发送消息
  async sendMessage({ commit }, { sessionId, type, content, replyTo }) {
    // 生成唯一的clientMsgId用于去重
    const clientMsgId = `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    const tempId = `temp_${clientMsgId}`
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

    // 创建临时消息
    const tempMessage = {
      id: tempId,
      clientMsgId,
      sessionId,
      type,
      content,
      replyTo,
      senderId: userInfo.userId,
      senderName: userInfo.nickName || userInfo.userName,
      senderAvatar: userInfo.avatar,
      timestamp: Date.now(),
      time: new Date().toISOString(),
      status: 'sending',
    }

    // 添加到消息列表
    commit('ADD_MESSAGE', { sessionId, message: tempMessage })
    commit('ADD_PENDING_MESSAGE', { sessionId, tempId, message: tempMessage })

    try {
      // 调用发送消息API
      const response = await apiSendMessage({
        sessionId,
        type,
        content: typeof content === 'object' ? JSON.stringify(content) : content,
        replyTo,
        clientMsgId, // 传递clientMsgId用于服务端去重
      })

      // 更新消息状态
      commit('UPDATE_MESSAGE', {
        sessionId,
        messageId: tempId,
        updates: {
          id: response.data?.id || tempId,
          status: 'sent',
        },
      })

      // 更新会话的最后消息
      commit('UPDATE_SESSION', {
        sessionId,
        updates: {
          lastMessage: {
            content: typeof content === 'string' ? content : '[消息]',
            timestamp: Date.now(),
          },
        },
      })

      commit('REMOVE_PENDING_MESSAGE', { sessionId, tempId })
      return response.data
    } catch (error) {
      // 更新消息状态为失败
      commit('UPDATE_MESSAGE', {
        sessionId,
        messageId: tempId,
        updates: { status: 'failed' },
      })
      commit('REMOVE_PENDING_MESSAGE', { sessionId, tempId })
      throw error
    }
  },

  // 重发消息
  async resendMessage({ commit, dispatch }, message) {
    // 更新消息状态为发送中
    commit('UPDATE_MESSAGE', {
      sessionId: message.sessionId,
      messageId: message.id,
      updates: { status: 'sending' },
    })

    try {
      await dispatch('sendMessage', {
        sessionId: message.sessionId,
        type: message.type,
        content: message.content,
        replyTo: message.replyTo,
      })
    } catch (error) {
      ElMessage.error('重发失败，请重试')
    }
  },

  // 重发所有离线消息
  async resendOfflineMessages({ state, dispatch }) {
    const pendingMessages = state.pendingMessages
    for (const sessionId in pendingMessages) {
      for (const tempId in pendingMessages[sessionId]) {
        const message = pendingMessages[sessionId][tempId]
        if (message.status === 'failed' || message.status === 'sending') {
          try {
            await dispatch('resendMessage', message)
          } catch (e) {
            console.error('重发离线消息失败:', e)
          }
        }
      }
    }
  },

  // 接收新消息
  receiveMessage({ commit, state }, message) {
    commit('ADD_MESSAGE', { sessionId: message.sessionId, message })

    // 如果不是当前会话，增加未读数
    if (!state.currentSession || state.currentSession.id !== message.sessionId) {
      const session = state.sessions.find(s => s.id === message.sessionId)
      if (session) {
        commit('UPDATE_SESSION', {
          sessionId: message.sessionId,
          updates: {
            unreadCount: (session.unreadCount || 0) + 1,
            lastMessage: {
              content: message.content,
              timestamp: message.timestamp,
            },
          },
        })
      }
    }
  },

  // 置顶/取消置顶会话
  async toggleSessionPin({ commit, state }, sessionId) {
    const session = state.sessions.find(s => s.id === sessionId)
    if (!session) return

    const pinned = !session.pinned
    try {
      await updateSession(sessionId, { pinned })
      commit('UPDATE_SESSION', { sessionId, updates: { pinned } })
    } catch (error) {
      ElMessage.error('操作失败')
    }
  },

  // 静音/取消静音会话
  async toggleSessionMute({ commit, state }, sessionId) {
    const session = state.sessions.find(s => s.id === sessionId)
    if (!session) return

    const muted = !session.muted
    try {
      await updateSession(sessionId, { muted })
      commit('UPDATE_SESSION', { sessionId, updates: { muted } })
    } catch (error) {
      ElMessage.error('操作失败')
    }
  },

  // 删除会话
  async deleteSession({ commit, state }, sessionId) {
    try {
      await apiDeleteSession(sessionId)
      commit('REMOVE_SESSION', sessionId)
      if (state.currentSession?.id === sessionId) {
        commit('SET_CURRENT_SESSION', null)
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  },

  // 下载文件
  downloadFile(_, message) {
    if (message.type === 'file' && message.content?.url) {
      const link = document.createElement('a')
      link.href = message.content.url
      link.download = message.content.name || 'file'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }
  },

  // 更新WebSocket连接状态
  setWsConnected({ commit }, status) {
    commit('SET_WS_CONNECTED', status)
  },

  // 更新在线状态
  updateOnlineStatus({ commit }, { userId, status }) {
    commit('SET_ONLINE_STATUS', { userId, status })
  },

  // 切换到联系人私聊
  async switchToContact({ commit, state }, contact) {
    // 创建或获取与该联系人的私聊会话
    let session = state.sessions.find(s => s.type === 'private' && s.peerId === contact.id)

    if (!session) {
      session = {
        id: contact.id,
        name: contact.nickname || contact.username,
        avatar: contact.avatar,
        type: 'private',
        peerId: contact.id,
        unreadCount: 0,
        lastMessage: null,
        pinned: false,
        muted: false,
        online: contact.online,
      }
      commit('ADD_SESSION', session)
    }

    commit('SET_CURRENT_SESSION', session)
    return session
  },

  // 切换到群组
  async switchToGroup({ commit, state }, group) {
    let session = state.sessions.find(s => s.type === 'group' && s.id === group.id)

    if (!session) {
      session = {
        id: group.id,
        name: group.name,
        avatar: group.avatar,
        type: 'group',
        groupId: group.id,
        unreadCount: 0,
        lastMessage: null,
        pinned: false,
        muted: false,
        memberCount: group.memberCount,
      }
      commit('ADD_SESSION', session)
    }

    commit('SET_CURRENT_SESSION', session)
    return session
  },

  // 打开文件
  openFile(_, file) {
    // TODO: 实现文件打开逻辑，如预览、下载等
    window.open(file.url, '_blank')
  },

  // 加载联系人列表
  async loadContacts({ commit }) {
    // TODO: 调用API获取联系人列表
    // const response = await listContacts()
    // commit('SET_CONTACTS', response.data)
    commit('SET_CONTACTS', [])
  },

  // 加载群组列表
  async loadGroups({ commit }) {
    // TODO: 调用API获取群组列表
    // const response = await listGroups()
    // commit('SET_GROUPS', response.data)
    commit('SET_GROUPS', [])
  },

  // 加载文件列表
  async loadFiles({ commit }) {
    // TODO: 调用API获取文件列表
    // const response = await listFiles()
    // commit('SET_FILES', response.data)
    commit('SET_FILES', [])
  },

  // 删除联系人
  async deleteContact({ commit }, contactId) {
    // TODO: 调用API删除联系人
    ElMessage.success('联系人已删除')
  },

  // 退出群组
  async leaveGroup({ commit }, groupId) {
    // TODO: 调用API退出群组
    commit('REMOVE_SESSION', groupId)
    ElMessage.success('已退出群组')
  },

  // 删除文件
  async deleteFile({ commit }, fileId) {
    // TODO: 调用API删除文件
    ElMessage.success('文件已删除')
  },

  // 撤回消息
  async recallMessage({ commit, state }, messageId) {
    try {
      await apiRecallMessage(messageId)
      // 查找并更新消息状态为已撤回
      for (const sessionId in state.messageList) {
        const msg = state.messageList[sessionId].find(m => m.id === messageId)
        if (msg) {
          commit('UPDATE_MESSAGE', {
            sessionId,
            messageId,
            updates: { status: 'recalled', revoked: true, content: '[消息已撤回]' },
          })
          break
        }
      }
      ElMessage.success('消息已撤回')
    } catch (error) {
      console.error('撤回消息失败:', error)
      ElMessage.error(
        error?.response?.data?.message || error?.message || '撤回失败，可能已超过撤回时间限制'
      )
      throw error
    }
  },

  // 转发消息
  async forwardMessage({ commit, state }, { messageId, toSessionId, toUserId, content }) {
    try {
      const response = await apiForwardMessage({
        messageId,
        toSessionId,
        toUserId,
        content,
      })
      ElMessage.success('转发成功')
      return response.data
    } catch (error) {
      console.error('转发消息失败:', error)
      ElMessage.error('转发失败')
      throw error
    }
  },

  // 搜索消息
  async searchMessages({ commit }, { sessionId, keyword, messageType, page = 1, pageSize = 20 }) {
    try {
      const response = await apiSearchMessages({
        sessionId,
        keyword,
        messageType,
        page,
        pageSize,
      })
      const messages = response.rows || response.data || []
      return {
        messages,
        total: response.total || 0,
        hasMore: messages.length === pageSize,
      }
    } catch (error) {
      console.error('搜索消息失败:', error)
      ElMessage.error('搜索失败')
      return { messages: [], total: 0, hasMore: false }
    }
  },

  // 批量删除消息
  async batchDeleteMessages({ commit, state }, { sessionId, messageIds }) {
    try {
      await deleteMessage(messageIds)
      // 从本地消息列表中移除
      const messageList = state.messageList[sessionId]
      if (messageList) {
        state.messageList[sessionId] = messageList.filter(m => !messageIds.includes(m.id))
        // 更新缓存
        saveMessageCache(state.messageList)
      }
      ElMessage.success(`已删除 ${messageIds.length} 条消息`)
    } catch (error) {
      console.error('删除消息失败:', error)
      ElMessage.error('删除失败')
    }
  },
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions,
}
