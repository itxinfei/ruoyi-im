/**
 * WebSocket 连接管理器 - 增强版
 * 提供更稳定的 WebSocket 连接，支持智能重连、网络状态检测、离线队列
 * @author ruoyi
 */

import { ElMessage, ElNotification } from 'element-plus'
import store from '@/store'

// WebSocket 连接状态
export const WS_STATUS = {
  CONNECTING: 'connecting',
  CONNECTED: 'connected',
  DISCONNECTED: 'disconnected',
  RECONNECTING: 'reconnecting',
  ERROR: 'error',
}

// 消息类型
export const MSG_TYPE = {
  PING: 'ping',
  PONG: 'pong',
  AUTH: 'auth',
  TEXT: 'text',
  IMAGE: 'image',
  FILE: 'file',
  VOICE: 'voice',
  VIDEO: 'video',
  NOTIFICATION: 'notification',
  SYSTEM: 'system',
  ONLINE: 'online',
  OFFLINE: 'offline',
  READ: 'read',
  RECEIVED: 'received',
  RECALL: 'recall',
  REACTION: 'reaction',
  FAVORITE: 'favorite',
  TYPING: 'typing',
}

// 重连配置
const RECONNECT_CONFIG = {
  // 最大重连次数（无限重连）
  MAX_RECONNECT: Infinity,
  // 初始重连延迟（毫秒）
  INITIAL_DELAY: 1000,
  // 最大重连延迟（毫秒）
  MAX_DELAY: 30000,
  // 指数退避因子
  BACKOFF_FACTOR: 1.5,
  // 心跳间隔（毫秒）
  HEARTBEAT_INTERVAL: 30000,
  // 心跳超时（毫秒）
  HEARTBEAT_TIMEOUT: 10000,
}

class WebSocketManager {
  constructor() {
    this.ws = null
    this.url = ''
    this.status = WS_STATUS.DISCONNECTED

    // 重连状态
    this.reconnectAttempts = 0
    this.reconnectTimer = null
    this.reconnectDelay = RECONNECT_CONFIG.INITIAL_DELAY

    // 心跳状态
    this.heartbeatTimer = null
    this.heartbeatTimeoutTimer = null
    this.lastPongTime = 0

    // 事件监听器
    this.listeners = new Map()

    // 消息队列
    this.pendingMessages = []
    this.messageQueue = []

    // 状态标志
    this.isManualClose = false
    this.firstConnected = false
    this.destroyed = false

    // 调试模式
    this.debug = import.meta.env.DEV

    // 网络状态监听
    this.initNetworkListener()
  }

  /**
   * 初始化网络状态监听
   */
  initNetworkListener() {
    if (typeof window !== 'undefined' && 'addEventListener' in window) {
      // 监听在线/离线事件
      window.addEventListener('online', () => {
        this.log('网络已恢复，尝试重新连接...')
        if (this.status !== WS_STATUS.CONNECTED) {
          this.reconnect()
        }
      })

      window.addEventListener('offline', () => {
        this.log('网络已断开')
        this.stopHeartbeat()
        this.status = WS_STATUS.DISCONNECTED
        this.emit('statusChange', this.status)
        ElNotification.warning({
          title: '网络断开',
          message: '网络连接已断开，请检查网络设置',
          duration: 5000,
        })
      })
    }
  }

  /**
   * 连接 WebSocket
   */
  connect(token, wsUrl) {
    if (this.destroyed) {
      console.warn('[WebSocket] 实例已销毁，无法连接')
      return
    }

    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.log('WebSocket 已连接')
      return true
    }

    if (this.ws && this.ws.readyState === WebSocket.CONNECTING) {
      this.log('WebSocket 正在连接中...')
      return true
    }

    this.isManualClose = false
    this.status = WS_STATUS.CONNECTING
    this.emit('statusChange', this.status)

    // 构建连接 URL
    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = wsUrl || import.meta.env.VITE_APP_WS_API || location.host
    this.url = `${protocol}//${host}/ws/im`

    // 获取用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const userId = userInfo.userId || userInfo.id

    // 构建查询参数
    const params = new URLSearchParams({
      token: token || localStorage.getItem('Admin-Token') || localStorage.getItem('token') || '',
    })
    if (userId) {
      params.append('userId', userId)
    }

    const urlWithParams = `${this.url}?${params.toString()}`

    this.log(`正在连接 WebSocket: ${this.url}`)

    try {
      this.ws = new WebSocket(urlWithParams)
      this.initEvents()
      return true
    } catch (error) {
      this.error('创建 WebSocket 失败:', error)
      this.handleReconnect()
      return false
    }
  }

  /**
   * 初始化 WebSocket 事件
   */
  initEvents() {
    this.ws.onopen = () => {
      this.onOpen()
    }

    this.ws.onmessage = (event) => {
      this.onMessage(event.data)
    }

    this.ws.onerror = (error) => {
      this.onError(error)
    }

    this.ws.onclose = (event) => {
      this.onClose(event)
    }
  }

  /**
   * 连接成功处理
   */
  onOpen() {
    this.log('WebSocket 连接成功')
    this.status = WS_STATUS.CONNECTED
    this.reconnectAttempts = 0
    this.reconnectDelay = RECONNECT_CONFIG.INITIAL_DELAY

    // 清除重连定时器
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    this.emit('statusChange', this.status)

    // 开始心跳
    this.startHeartbeat()

    // 发送认证消息
    this.sendAuth()

    // 发送待发送消息
    this.sendPendingMessages()

    // 发送队列中的消息
    this.flushMessageQueue()

    // 首次连接提示
    if (!this.firstConnected) {
      ElNotification.success({
        title: '连接成功',
        message: 'IM 服务已连接',
        duration: 2000,
      })
      this.firstConnected = true
    } else {
      // 重连成功提示
      ElNotification.success({
        title: '重连成功',
        message: 'IM 服务已重新连接',
        duration: 2000,
      })
    }
  }

  /**
   * 消息接收处理
   */
  onMessage(data) {
    try {
      const message = typeof data === 'string' ? JSON.parse(data) : data

      // 处理心跳响应
      if (message.type === MSG_TYPE.PONG) {
        this.lastPongTime = Date.now()
        this.debugLog('收到心跳响应')
        return
      }

      // 处理认证响应
      if (message.type === MSG_TYPE.AUTH) {
        if (message.success) {
          this.log('认证成功')
        } else {
          this.error('认证失败:', message.message)
        }
        return
      }

      // 转发消息到对应处理器
      this.handleMessage(message)

    } catch (error) {
      this.error('解析消息失败:', error, data)
    }
  }

  /**
   * 处理业务消息
   */
  handleMessage(message) {
    this.log('收到消息:', message.type)

    // 更新 Vuex store
    if (store && store.dispatch) {
      switch (message.type) {
        case MSG_TYPE.TEXT:
        case MSG_TYPE.IMAGE:
        case MSG_TYPE.FILE:
        case MSG_TYPE.VOICE:
        case MSG_TYPE.VIDEO:
        case 'message':
          store.dispatch('im/receiveMessage', message)
          break
        case MSG_TYPE.ONLINE:
        case MSG_TYPE.OFFLINE:
          store.dispatch('im/updateOnlineStatus', {
            userId: message.userId,
            status: message.type === MSG_TYPE.ONLINE ? 'online' : 'offline',
          })
          break
        case MSG_TYPE.REACTION:
          store.dispatch('im/handleMessageReaction', {
            messageId: message.messageId,
            conversationId: message.conversationId,
            userId: message.userId,
            emoji: message.emoji,
            action: message.action,
          })
          break
        case 'read_receipt':
          store.dispatch('im/handleReadReceipt', message)
          break
      }
    }

    // 触发事件
    this.emit('message', message)
  }

  /**
   * 错误处理
   */
  onError(error) {
    this.error('WebSocket 错误:', error)
    this.status = WS_STATUS.ERROR
    this.emit('statusChange', this.status)
  }

  /**
   * 连接关闭处理
   */
  onClose(event) {
    this.log(`WebSocket 关闭: code=${event.code}, reason=${event.reason}`)

    this.stopHeartbeat()

    if (!this.isManualClose && !this.destroyed) {
      this.status = WS_STATUS.RECONNECTING
      this.emit('statusChange', this.status)
      this.handleReconnect()
    } else {
      this.status = WS_STATUS.DISCONNECTED
      this.emit('statusChange', this.status)
    }
  }

  /**
   * 处理重连
   */
  handleReconnect() {
    if (this.isManualClose || this.destroyed) {
      return
    }

    // 清除之前的重连定时器
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
    }

    if (this.reconnectAttempts >= RECONNECT_CONFIG.MAX_RECONNECT) {
      this.error('达到最大重连次数')
      this.status = WS_STATUS.DISCONNECTED
      this.emit('statusChange', this.status)
      ElNotification.error({
        title: '连接失败',
        message: '无法连接到服务器，请刷新页面重试',
        duration: 0,
      })
      return
    }

    this.reconnectAttempts++

    // 计算重连延迟（指数退避）
    this.reconnectDelay = Math.min(
      this.reconnectDelay * RECONNECT_CONFIG.BACKOFF_FACTOR,
      RECONNECT_CONFIG.MAX_DELAY
    )

    this.log(`${this.reconnectDelay}ms 后进行第 ${this.reconnectAttempts} 次重连...`)

    this.reconnectTimer = setTimeout(() => {
      const token = localStorage.getItem('Admin-Token') || localStorage.getItem('token')
      if (token) {
        this.connect(token)
      }
    }, this.reconnectDelay)
  }

  /**
   * 开始心跳
   */
  startHeartbeat() {
    this.stopHeartbeat()

    this.lastPongTime = Date.now()

    // 定期发送心跳
    this.heartbeatTimer = setInterval(() => {
      if (this.isConnected()) {
        this.send({ type: MSG_TYPE.PING })
      }
    }, RECONNECT_CONFIG.HEARTBEAT_INTERVAL)

    // 检查心跳超时
    this.heartbeatTimeoutTimer = setInterval(() => {
      const now = Date.now()
      if (now - this.lastPongTime > RECONNECT_CONFIG.HEARTBEAT_TIMEOUT * 2) {
        this.log('心跳超时，主动断开重连')
        if (this.ws) {
          this.ws.close()
        }
      }
    }, RECONNECT_CONFIG.HEARTBEAT_TIMEOUT)
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
    if (this.heartbeatTimeoutTimer) {
      clearInterval(this.heartbeatTimeoutTimer)
      this.heartbeatTimeoutTimer = null
    }
  }

  /**
   * 发送认证消息
   */
  sendAuth() {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const token = localStorage.getItem('Admin-Token') || localStorage.getItem('token')

    this.send({
      type: MSG_TYPE.AUTH,
      token: token,
      userId: userInfo.userId || userInfo.id,
      timestamp: Date.now(),
    })
  }

  /**
   * 发送消息
   */
  send(message) {
    if (this.isConnected()) {
      try {
        const data = typeof message === 'string' ? message : JSON.stringify(message)
        this.ws.send(data)
        return true
      } catch (error) {
        this.error('发送消息失败:', error)
        return false
      }
    } else {
      // 连接断开时，将消息加入队列
      this.log('连接断开，消息加入队列')
      this.messageQueue.push(message)
      return false
    }
  }

  /**
   * 发送聊天消息
   */
  sendMessage(message) {
    const payload = {
      conversationId: message.conversationId || message.sessionId,
      messageType: (message.type || MSG_TYPE.TEXT).toUpperCase(),
      content: message.content,
      replyToMessageId: message.replyToMessageId,
      clientMsgId: message.clientMsgId,
    }

    return this.send({
      type: 'message',
      payload: payload,
    })
  }

  /**
   * 发送待发送消息
   */
  sendPendingMessages() {
    while (this.pendingMessages.length > 0) {
      const message = this.pendingMessages.shift()
      this.send(message)
    }
  }

  /**
   * 刷新消息队列
   */
  flushMessageQueue() {
    while (this.messageQueue.length > 0) {
      const message = this.messageQueue.shift()
      this.send(message)
    }
  }

  /**
   * 重新连接
   */
  reconnect() {
    this.reconnectAttempts = 0
    this.reconnectDelay = RECONNECT_CONFIG.INITIAL_DELAY
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    const token = localStorage.getItem('Admin-Token') || localStorage.getItem('token')
    if (token) {
      this.connect(token)
    }
  }

  /**
   * 手动关闭连接
   */
  close() {
    this.isManualClose = true
    this.stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.status = WS_STATUS.DISCONNECTED
    this.emit('statusChange', this.status)
  }

  /**
   * 销毁实例
   */
  destroy() {
    this.destroyed = true
    this.close()
    this.listeners.clear()
    this.pendingMessages = []
    this.messageQueue = []
  }

  /**
   * 获取连接状态
   */
  getStatus() {
    return this.status
  }

  /**
   * 是否已连接
   */
  isConnected() {
    return this.status === WS_STATUS.CONNECTED &&
      this.ws &&
      this.ws.readyState === WebSocket.OPEN
  }

  /**
   * 事件监听
   */
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
    return () => this.off(event, callback)
  }

  /**
   * 取消事件监听
   */
  off(event, callback) {
    if (!this.listeners.has(event)) return
    const callbacks = this.listeners.get(event)
    const index = callbacks.indexOf(callback)
    if (index > -1) {
      callbacks.splice(index, 1)
    }
  }

  /**
   * 触发事件
   */
  emit(event, data) {
    if (!this.listeners.has(event)) return
    this.listeners.get(event).forEach(callback => {
      try {
        callback(data)
      } catch (error) {
        this.error(`事件处理器错误 [${event}]:`, error)
      }
    })
  }

  /**
   * 日志输出
   */
  log(...args) {
    if (this.debug) {
      console.log('[WebSocket]', ...args)
    }
  }

  debugLog(...args) {
    if (this.debug) {
      console.debug('[WebSocket]', ...args)
    }
  }

  error(...args) {
    console.error('[WebSocket]', ...args)
  }
}

// 创建单例
const wsManager = new WebSocketManager()

export default wsManager
export { WS_STATUS, MSG_TYPE, RECONNECT_CONFIG }
