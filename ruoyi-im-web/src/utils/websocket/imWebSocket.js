/**
 * IM WebSocket 连接管理
 * 提供稳定可靠的 WebSocket 连接，支持断线重连、心跳检测
 */

import { ElMessage } from 'element-plus'
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
  // 系统消息
  PING: 'ping',
  PONG: 'pong',
  AUTH: 'auth',

  // 聊天消息
  TEXT: 'text',
  IMAGE: 'image',
  FILE: 'file',
  VOICE: 'voice',
  VIDEO: 'video',

  // 通知消息
  NOTIFICATION: 'notification',
  SYSTEM: 'system',

  // 状态消息
  ONLINE: 'online',
  OFFLINE: 'offline',
  TYPING: 'typing',

  // 回执
  READ: 'read',
  RECEIVED: 'received',
  RECALL: 'recall',
}

class ImWebSocket {
  constructor() {
    this.ws = null
    this.url = ''
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 10
    this.reconnectDelay = 2000
    this.heartbeatInterval = null
    this.heartbeatTimer = 30000 // 30秒心跳
    this.status = WS_STATUS.DISCONNECTED
    this.listeners = new Map()
    this.pendingMessages = [] // 待发送消息
    this.isManualClose = false
    this.firstConnected = false // 标记是否首次连接
    this.hasShownReconnectMessage = false // 标记是否已显示过重连消息（整个会话只显示一次）
    this.debug = import.meta.env.DEV // 开发环境开启调试
  }

  /**
   * 连接 WebSocket
   * @param {string} token 用户认证token
   * @param {string} wsUrl WebSocket地址
   */
  connect(token, wsUrl) {
    if (
      this.ws &&
      (this.ws.readyState === WebSocket.CONNECTING || this.ws.readyState === WebSocket.OPEN)
    ) {
      this.log('WebSocket 已连接或正在连接')
      return
    }

    this.isManualClose = false
    this.status = WS_STATUS.CONNECTING
    this.emit('statusChange', this.status)

    // 获取当前用户ID
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const userId = userInfo.userId || userInfo.id

    // 构建 WebSocket URL
    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = import.meta.env.VITE_APP_WS_API || location.host
    this.url = wsUrl || `${protocol}//${host}/ws/im`

    // 添加 token 和 userId 参数
    let urlWithParams = `${this.url}?token=${encodeURIComponent(token)}`
    if (userId) {
      urlWithParams += `&userId=${userId}`
    }

    this.log(`正在连接 WebSocket: ${this.url}, userId: ${userId}`)

    try {
      this.ws = new WebSocket(urlWithParams)
      this.initEvents()
    } catch (error) {
      this.error('创建 WebSocket 失败:', error)
      this.handleReconnect()
    }
  }

  /**
   * 初始化 WebSocket 事件
   */
  initEvents() {
    this.ws.onopen = () => {
      this.log('WebSocket 连接成功')
      this.status = WS_STATUS.CONNECTED
      this.reconnectAttempts = 0
      this.emit('statusChange', this.status)

      // 开始心跳
      this.startHeartbeat()

      // 发送认证消息
      this.sendAuth()

      // 发送待发送消息
      this.sendPendingMessages()

      // 只在首次连接时显示提示
      if (!this.firstConnected) {
        ElMessage.success('连接成功')
        this.firstConnected = true
      }
    }

    this.ws.onmessage = event => {
      this.handleMessage(event.data)
    }

    this.ws.onerror = error => {
      this.error('WebSocket 错误:', error)
      this.status = WS_STATUS.ERROR
      this.emit('statusChange', this.status)
    }

    this.ws.onclose = event => {
      this.log(`WebSocket 关闭: code=${event.code}, reason=${event.reason}`)
      this.stopHeartbeat()

      if (!this.isManualClose) {
        this.status = WS_STATUS.RECONNECTING
        this.emit('statusChange', this.status)
        this.handleReconnect()
      } else {
        this.status = WS_STATUS.DISCONNECTED
        this.emit('statusChange', this.status)
      }
    }
  }

  /**
   * 处理接收到的消息
   */
  handleMessage(data) {
    try {
      // 尝试解析 JSON
      const message = typeof data === 'string' ? JSON.parse(data) : data

      this.log('收到消息:', message)

      // 处理心跳响应
      if (message.type === MSG_TYPE.PONG) {
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

      // 处理普通消息
      switch (message.type) {
        case MSG_TYPE.TEXT:
        case MSG_TYPE.IMAGE:
        case MSG_TYPE.FILE:
        case MSG_TYPE.VOICE:
        case MSG_TYPE.VIDEO:
          this.emit('message', message)
          // 更新 Vuex store
          if (store && store.dispatch) {
            store.dispatch('im/receiveMessage', message)
          }
          break

        case MSG_TYPE.NOTIFICATION:
          this.emit('notification', message)
          ElMessage.info(message.content)
          break

        case MSG_TYPE.SYSTEM:
          this.emit('system', message)
          break

        case MSG_TYPE.READ:
          this.emit('messageRead', message)
          break

        case MSG_TYPE.RECALL:
          this.emit('messageRecall', message)
          break

        case MSG_TYPE.ONLINE:
        case MSG_TYPE.OFFLINE:
          this.emit('onlineStatusChange', message)
          if (store && store.dispatch) {
            store.dispatch('im/updateOnlineStatus', {
              userId: message.userId,
              status: message.type === MSG_TYPE.ONLINE ? 'online' : 'offline',
            })
          }
          break

        default:
          this.emit('unknown', message)
      }
    } catch (error) {
      this.error('解析消息失败:', error, data)
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
      userId: userInfo.userId,
      timestamp: Date.now(),
    })
  }

  /**
   * 发送消息
   * @param {Object} message 消息对象
   */
  send(message) {
    const data =
      typeof message === 'string'
        ? message
        : JSON.stringify({
            ...message,
            timestamp: Date.now(),
          })

    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(data)
      this.log('发送消息:', message)
      return true
    } else {
      this.log('WebSocket 未连接，消息加入待发送队列')
      this.pendingMessages.push(message)
      return false
    }
  }

  /**
   * 发送聊天消息
   */
  sendMessage(message) {
    return this.send({
      type: message.type || MSG_TYPE.TEXT,
      sessionId: message.sessionId,
      conversationId: message.conversationId,
      content: message.content,
      replyToMessageId: message.replyToMessageId,
      clientMsgId: message.clientMsgId,
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
   * 开始心跳
   */
  startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatInterval = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.send({ type: MSG_TYPE.PING })
      }
    }, this.heartbeatTimer)
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  /**
   * 处理重连
   */
  handleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      this.error('达到最大重连次数')
      this.status = WS_STATUS.DISCONNECTED
      this.emit('statusChange', this.status)
      ElMessage.error('连接已断开，请刷新页面重试')
      return
    }

    this.reconnectAttempts++
    const delay = this.reconnectDelay * Math.min(2, this.reconnectAttempts)

    this.log(`${delay}ms 后进行第 ${this.reconnectAttempts} 次重连...`)

    // 不再显示重连提示弹窗（用户请求取消）
    // if (!this.hasShownReconnectMessage) {
    //   ElMessage.warning('连接断开，正在重新连接...')
    //   this.hasShownReconnectMessage = true
    // }

    setTimeout(() => {
      const token = localStorage.getItem('Admin-Token') || localStorage.getItem('token')
      if (token) {
        this.connect(token)
      }
    }, delay)
  }

  /**
   * 手动关闭连接
   */
  close() {
    this.isManualClose = true
    this.stopHeartbeat()
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.status = WS_STATUS.DISCONNECTED
    this.emit('statusChange', this.status)
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
    return this.status === WS_STATUS.CONNECTED && this.ws && this.ws.readyState === WebSocket.OPEN
  }

  /**
   * 添加事件监听器
   */
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  /**
   * 移除事件监听器
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
        this.error(`事件回调错误 [${event}]:`, error)
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

  /**
   * 错误输出
   */
  error(...args) {
    console.error('[WebSocket]', ...args)
  }
}

// 创建单例实例
const imWebSocket = new ImWebSocket()

export default imWebSocket
