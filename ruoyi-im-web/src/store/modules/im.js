import {
  sendMessage as apiSendMessage,
  listMessage,
  recallMessage as apiRecallMessage,
  forwardMessage as apiForwardMessage,
  searchMessages as apiSearchMessages,
  markMessageRead,
  deleteMessage,
  batchDeleteMessages,
} from '@/api/im/message'
import { listSession, updateSession, deleteSession as apiDeleteSession } from '@/api/im/session'
import { markConversationRead } from '@/api/im/conversation'
import { listContact } from '@/api/im/contact'
import { ElMessage } from 'element-plus'
import { getCurrentUserId, getCurrentUserInfo } from '@/utils/im-user'

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
 * 注意：如果缓存版本不匹配，会清除旧缓存
 */
function loadMessageCache() {
  try {
    const data = localStorage.getItem(MESSAGE_CACHE_KEY)
    const CACHE_VERSION_KEY = 'im_message_cache_version'
    const CURRENT_VERSION = '2.0' // 缓存版本号

    const cachedVersion = localStorage.getItem(CACHE_VERSION_KEY)
    if (cachedVersion !== CURRENT_VERSION) {
      // 版本不匹配，清除旧缓存
      console.log('[Store] 消息缓存版本不匹配，清除旧缓存')
      localStorage.removeItem(MESSAGE_CACHE_KEY)
      localStorage.setItem(CACHE_VERSION_KEY, CURRENT_VERSION)
      return {}
    }

    if (!data) return {}

    const cacheData = JSON.parse(data)
    // 重新标准化所有缓存的消息
    const normalizedCache = {}
    for (const sessionId in cacheData) {
      const messages = cacheData[sessionId]
      // 重新标准化每条消息
      normalizedCache[sessionId] = messages.map(msg => normalizeMessage(msg))
    }
    console.log(`[Store] 从缓存加载了 ${Object.keys(normalizedCache).length} 个会话的消息`)
    return normalizedCache
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

/**
 * 解析消息内容（处理后端返回的JSON格式内容）
 * @param {string|object} content - 原始内容
 * @param {string} messageType - 消息类型（用于判断如何解析）
 * @returns {object} 解析后的消息内容对象
 */
function parseMessageContent(content, messageType = 'text') {
  // 如果是对象，直接返回
  if (typeof content === 'object' && content !== null) {
    return content
  }

  // 如果是字符串
  if (typeof content === 'string') {
    // 对于文本消息，如果不是JSON格式，直接返回纯文本
    if (messageType === 'text' || messageType === 'TEXT') {
      // 检查是否是JSON格式的字符串
      const trimmed = content.trim()
      if (trimmed.startsWith('{') || trimmed.startsWith('[')) {
        try {
          const parsed = JSON.parse(content)
          // 如果是对象，返回解析结果
          if (typeof parsed === 'object' && parsed !== null) {
            return parsed
          }
        } catch (e) {
          // JSON解析失败，当作纯文本处理
        }
      }
      // 纯文本消息，直接返回
      return { text: content, isPlainText: true }
    }

    // 对于文件/图片/语音/视频消息，尝试解析JSON获取文件信息
    if (['file', 'FILE', 'image', 'IMAGE', 'voice', 'VOICE', 'video', 'VIDEO'].includes(messageType)) {
      try {
        const parsed = JSON.parse(content)
        if (typeof parsed === 'object' && parsed !== null) {
          return parsed
        }
      } catch (e) {
        // JSON解析失败，可能是纯URL，返回url对象
        return { url: content, isPlainText: true }
      }
    }

    // 其他类型，尝试解析JSON
    if (content.trim().startsWith('{') || content.trim().startsWith('[')) {
      try {
        const parsed = JSON.parse(content)
        if (typeof parsed === 'object' && parsed !== null) {
          return parsed
        }
      } catch (e) {
        // JSON解析失败，当作纯文本处理
      }
    }

    // 默认返回纯文本包装对象
    return { text: content, isPlainText: true }
  }

  // 默认返回纯文本
  return { text: String(content || ''), isPlainText: true }
}

/**
 * 标准化消息对象（确保消息格式一致）
 * @param {object} message - 原始消息对象
 * @returns {object} 标准化后的消息对象
 */
function normalizeMessage(message) {
  // 获取消息类型（支持 type 和 messageType 两种字段名）
  const messageType = message.type || message.messageType || 'text'

  // 解析content字段
  const parsedContent = parseMessageContent(message.content, messageType)

  // 调试日志
  if (import.meta.env.DEV) {
    console.log('[Store normalizeMessage]', {
      originalType: message.type || message.messageType,
      normalizedType: messageType.toLowerCase(),
      originalContent: message.content,
      parsedContent,
    })
  }

  // 根据消息类型提取显示内容
  let displayContent = ''

  if (messageType === 'text' || messageType === 'TEXT') {
    // 文本消息：提取text字段，如果没有则使用原始content
    if (parsedContent.isPlainText) {
      displayContent = parsedContent.text || message.content || ''
    } else {
      displayContent = parsedContent.text || parsedContent.content || parsedContent.body || JSON.stringify(parsedContent)
    }
  } else if (messageType === 'image' || messageType === 'IMAGE') {
    // 图片消息：保留content（可能是URL或JSON）
    displayContent = message.content || ''
  } else if (messageType === 'file' || messageType === 'FILE') {
    // 文件消息：保留content（可能是JSON）
    displayContent = message.content || ''
  } else if (messageType === 'voice' || messageType === 'VOICE') {
    // 语音消息
    displayContent = message.content || ''
  } else if (messageType === 'video' || messageType === 'VIDEO') {
    // 视频消息
    displayContent = message.content || ''
  } else {
    // 其他类型，提取文本或使用原始content
    if (parsedContent.isPlainText) {
      displayContent = parsedContent.text || message.content || ''
    } else {
      displayContent = parsedContent.text || parsedContent.content || message.content || ''
    }
  }

  // 返回标准化消息
  return {
    ...message,
    // 统一消息类型字段名（优先使用type，其次messageType），统一转为小写
    type: (message.type || message.messageType || 'text').toLowerCase(),
    // 使用处理后的显示内容
    content: displayContent,
    // 保留原始数据和解析后的完整对象
    _rawContent: message.content,
    _parsedContent: parsedContent,
  }
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
  // 当前用户ID - 使用统一的工具函数
  currentUserId: () => getCurrentUserId(),
  // 当前用户信息
  currentUser: () => getCurrentUserInfo(),
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
    // 会话列表去重：首先根据 id 字段去重
    const uniqueSessions = []
    const idSet = new Set()
    const duplicates = []

    for (const session of list) {
      if (session.id && !idSet.has(session.id)) {
        idSet.add(session.id)
        uniqueSessions.push(session)
      } else if (session.id) {
        duplicates.push(session)
      }
    }

    // 如果有重复，打印日志
    if (duplicates.length > 0) {
      console.warn('[Vuex] 发现重复会话（按id）:', duplicates.length, '条', duplicates)
    }

    // 进一步去重：对于私聊会话，根据 targetId/peerId 去重
    // 同一个私聊对象应该只显示一个会话
    const finalSessions = []
    const peerIdMap = new Map()

    for (const session of uniqueSessions) {
      // 私聊会话需要按对方用户ID去重
      if (session.type === 'private' || session.type === 'PRIVATE') {
        const peerId = session.peerId || session.targetId
        if (peerId && !peerIdMap.has(peerId)) {
          peerIdMap.set(peerId, session)
          finalSessions.push(session)
        } else if (peerId) {
          console.warn('[Vuex] 发现重复私聊会话（按peerId）', {
            existing: peerIdMap.get(peerId),
            duplicate: session,
          })
          // 保留最后消息时间较新的会话
          const existing = peerIdMap.get(peerId)
          const existingTime = existing.lastMessageTime || existing.updateTime
          const duplicateTime = session.lastMessageTime || session.updateTime
          if (duplicateTime > existingTime) {
            // 替换为较新的会话
            const index = finalSessions.indexOf(existing)
            if (index > -1) {
              finalSessions[index] = session
            }
            peerIdMap.set(peerId, session)
          }
        }
      } else {
        // 群聊会话直接添加
        finalSessions.push(session)
      }
    }

    console.log('[Vuex] SET_SESSIONS: 原始', list.length, '条, id去重后', uniqueSessions.length, '条, 最终', finalSessions.length, '条')
    state.sessions = finalSessions
  },
  ADD_SESSION: (state, session) => {
    // 首先检查是否已存在相同 id 的会话
    const existsById = state.sessions.find(s => s.id === session.id)
    if (existsById) {
      return // 已存在，不添加
    }

    // 对于私聊会话，还要检查是否已存在相同 peerId 的会话
    if (session.type === 'private' || session.type === 'PRIVATE') {
      const peerId = session.peerId || session.targetId
      if (peerId) {
        const existsByPeerId = state.sessions.find(s =>
          (s.type === 'private' || s.type === 'PRIVATE') &&
          (s.peerId === peerId || s.targetId === peerId)
        )
        if (existsByPeerId) {
          console.warn('[Vuex] ADD_SESSION: 发现相同 peerId 的会话，不添加', { existing: existsByPeerId, new: session })
          return // 已存在相同私聊对象的会话，不添加
        }
      }
    }

    state.sessions.unshift(session)
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
      // 标准化消息（解析JSON格式的内容）
      const normalizedMsg = normalizeMessage(msg)
      const key = generateMessageKey(normalizedMsg)
      if (!state.messageIdSet[sessionId].has(key)) {
        state.messageIdSet[sessionId].add(key)
        uniqueMessages.push(normalizedMsg)
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

    // 标准化消息（解析JSON格式的内容）
    const normalizedMsg = normalizeMessage(message)

    // 生成消息唯一标识用于去重
    const key = generateMessageKey(normalizedMsg)

    // 检查是否已存在（去重）
    if (state.messageIdSet[sessionId].has(key)) {
      return // 消息已存在，跳过
    }

    // 添加消息
    state.messageIdSet[sessionId].add(key)
    state.messageList[sessionId].push(normalizedMsg)

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
      // 标准化消息（解析JSON格式的内容）
      const normalizedMsg = normalizeMessage(msg)
      const key = generateMessageKey(normalizedMsg)
      if (!state.messageIdSet[sessionId].has(key)) {
        state.messageIdSet[sessionId].add(key)
        uniqueMessages.push(normalizedMsg)
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
    // 联系人去重：根据friendId（好友用户ID）去重
    // 重要：只使用friendId作为唯一标识，不使用id（关系ID）
    const uniqueContactsMap = new Map()

    for (const contact of contacts) {
      // 只使用friendId（好友用户ID）作为唯一标识
      const friendId = contact.friendId

      if (friendId != null && friendId !== '') {
        // 转为字符串确保类型一致性
        const friendKey = String(friendId)

        // 只保留第一次出现的记录
        if (!uniqueContactsMap.has(friendKey)) {
          uniqueContactsMap.set(friendKey, contact)
        }
      }
    }

    state.contacts = Array.from(uniqueContactsMap.values())
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
  async switchSession({ commit, dispatch, state }, session) {
    // 标记上一个会话为已读（如果有且不同于当前会话）
    if (state.currentSession && state.currentSession.id !== session.id) {
      try {
        await markConversationRead(state.currentSession.id)
      } catch (error) {
        console.error('标记上一个会话已读失败:', error)
      }
    }

    commit('SET_CURRENT_SESSION', session)
    commit('CLEAR_SESSION_UNREAD', session.id)

    // 标记当前会话为已读
    try {
      await markConversationRead(session.id)
    } catch (error) {
      console.error('标记当前会话已读失败:', error)
    }

    // 加载该会话的消息（首次加载，不指定lastId）
    await dispatch('loadMessages', { sessionId: session.id, pageSize: 20 })
  },

  // 加载消息
  async loadMessages({ commit, state }, { sessionId, lastId = null, pageSize = 20 }) {
    const response = await listMessage({ conversationId: sessionId, lastId, pageSize })
    const messages = response.rows || response.data || []

    // 如果没有指定lastId，说明是首次加载，清空旧消息
    if (lastId === null) {
      commit('SET_MESSAGE_LIST', { sessionId, messages })
    } else {
      // 向上翻页，将新消息添加到列表前面
      commit('PREPEND_MESSAGES', { sessionId, messages })
    }

    return {
      messages,
      hasMore: messages.length === pageSize,
    }
  },

  // 发送消息
  // 优先使用 WebSocket，WebSocket 未连接时降级到 REST API
  async sendMessage({ commit, state }, { sessionId, type, content, replyTo }) {
    // 生成唯一的clientMsgId用于去重
    const clientMsgId = `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    const tempId = `temp_${clientMsgId}`
    const userInfo = getCurrentUserInfo()

    if (!userInfo || !userInfo.userId) {
      ElMessage.error('用户未登录，请先登录')
      return null
    }

    // 创建临时消息（乐观UI更新）
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

    // 添加到消息列表（立即显示给用户）
    commit('ADD_MESSAGE', { sessionId, message: tempMessage })
    commit('ADD_PENDING_MESSAGE', { sessionId, tempId, message: tempMessage })

    const ws = state.ws

    // 优先使用 WebSocket（实时性更好）
    if (ws && ws.isConnected) {
      try {
        ws.sendMessage({
          conversationId: sessionId,
          type,
          content,
          replyToMessageId: replyTo,
          clientMsgId,
        })

        // 乐观更新：假设消息发送成功
        commit('UPDATE_MESSAGE', {
          sessionId,
          messageId: tempId,
          updates: { status: 'sent' },
        })

        commit('REMOVE_PENDING_MESSAGE', { sessionId, tempId })
        return { id: tempId, clientMsgId }
      } catch (error) {
        // WebSocket 发送失败，降级到 REST API
        commit('UPDATE_MESSAGE', {
          sessionId,
          messageId: tempId,
          updates: { status: 'sending' },
        })
      }
    }

    // WebSocket 未连接或发送失败，降级到 REST API
    try {
      const response = await apiSendMessage({
        conversationId: sessionId,
        type,
        content: typeof content === 'object' ? JSON.stringify(content) : content,
        replyToMessageId: replyTo,
        clientMsgId,
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
      ElMessage.error('消息发送失败，请检查网络连接')
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
    // 确保当前用户的消息也有senderAvatar
    const userInfo = getCurrentUserInfo()
    if (userInfo && message.senderId === userInfo.userId && !message.senderAvatar) {
      message.senderAvatar = userInfo.avatar
    }
    
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
    try {
      const response = await listContact()
      const contacts = response.rows || response.data || []

      if (!Array.isArray(contacts) || contacts.length === 0) {
        commit('SET_CONTACTS', [])
        return []
      }

      // 后端返回的是 ImFriendVO，需要映射字段
      const mappedContacts = contacts.map(c => ({
        id: c.friendId, // 使用 friendId 作为 id（好友用户ID）
        friendId: c.friendId,
        name: c.friendName || c.remark || c.name || c.username,
        nickname: c.friendName || c.remark || c.name || c.username,
        username: c.username,
        avatar: c.friendAvatar || c.avatar,
        email: c.email,
        phone: c.phone,
        signature: c.signature,
        online: c.online || false,
        groupName: c.groupName,
        // 保留原始数据
        _originalId: c.id, // 保存好友关系ID
        _remark: c.remark,
      }))

      // 去重逻辑在 SET_CONTACTS mutation 中统一处理
      console.log(`[Store] 加载联系人: 原始${mappedContacts.length}条`)
      commit('SET_CONTACTS', mappedContacts)
      return mappedContacts
    } catch (error) {
      console.error('加载联系人失败:', error)
      ElMessage.error('加载联系人失败')
      return []
    }
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

  // 处理消息表情反应（WebSocket推送）
  handleMessageReaction({ commit, state }, payload) {
    const { messageId, conversationId, userId, emoji, action } = payload
    const sessionId = conversationId // 兼容sessionId命名

    // 查找并更新消息的反应列表
    if (state.messageList[sessionId]) {
      const message = state.messageList[sessionId].find(m => m.id === messageId)
      if (message) {
        // 初始化 reactions 数组
        if (!message.reactions) {
          message.reactions = []
        }

        if (action === 'add') {
          // 添加反应
          const existingReaction = message.reactions.find(r => r.emoji === emoji)
          if (existingReaction) {
            // 如果已有相同表情，检查用户是否已反应
            if (!existingReaction.users.includes(userId)) {
              existingReaction.users.push(userId)
              existingReaction.count++
            }
          } else {
            // 新增反应
            message.reactions.push({
              emoji,
              count: 1,
              users: [userId],
            })
          }
        } else if (action === 'remove') {
          // 移除反应
          const reactionIndex = message.reactions.findIndex(r => r.emoji === emoji)
          if (reactionIndex !== -1) {
            const reaction = message.reactions[reactionIndex]
            reaction.users = reaction.users.filter(id => id !== userId)
            reaction.count--
            if (reaction.count <= 0) {
              message.reactions.splice(reactionIndex, 1)
            }
          }
        }

        // 更新缓存
        saveMessageCache(state.messageList)
      }
    }
  },

  // 处理已读回执（WebSocket推送）
  handleReadReceipt({ commit, state }, payload) {
    const { conversationId, userId, messageIds } = payload
    const sessionId = conversationId // 兼容sessionId命名

    if (!state.messageList[sessionId]) {
      return
    }

    // 更新每条消息的已读状态
    messageIds.forEach(messageId => {
      const message = state.messageList[sessionId].find(m => m.id === messageId)
      if (message) {
        // 初始化已读用户列表
        if (!message.readBy) {
          message.readBy = []
        }
        // 添加已读用户（去重）
        if (!message.readBy.includes(userId)) {
          message.readBy.push(userId)
        }
      }
    })

    // 更新缓存
    saveMessageCache(state.messageList)
  },
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions,
}
