/**
 * IM WebSocket 客户端
 * 提供 WebSocket 连接管理、消息收发、心跳保活、断线重连等功能
 */

// WebSocket 连接状态
export const WS_STATUS = {
  CONNECTING: 0,
  OPEN: 1,
  CLOSING: 2,
  CLOSED: 3
}

// 消息类型
export const MESSAGE_TYPE = {
  AUTH: 'auth',           // 认证
  MESSAGE: 'message',     // 聊天消息
  PING: 'ping',           // 心跳请求
  PONG: 'pong',           // 心跳响应
  READ: 'read',           // 已读回执
  TYPING: 'typing',       // 正在输入
  ONLINE: 'online',       // 用户上线
  OFFLINE: 'offline',     // 用户下线
  CALL: 'call'            // 音视频通话
}

class ImWebSocket {
  constructor() {
    this.ws = null
    this.url = ''
    this.token = ''
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.heartbeatInterval = 30000 // 30 秒心跳
    this.eventHandlers = new Map()
  }

  /**
   * 连接 WebSocket
   * @param {string} token - 认证令牌
   */
  connect(token) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.warn('[ImWebSocket] WebSocket 已连接')
      return
    }

    this.token = token
    const wsBaseUrl = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080'
    this.url = `${wsBaseUrl}/ws/im?token=${token}`

    try {
      console.log('[ImWebSocket] 正在连接...', this.url)
      this.ws = new WebSocket(this.url)

      // 绑定事件
      this.ws.onopen = this.onOpen.bind(this)
      this.ws.onmessage = this.onMessage.bind(this)
      this.ws.onerror = this.onError.bind(this)
      this.ws.onclose = this.onClose.bind(this)
    } catch (error) {
      console.error('[ImWebSocket] 连接失败:', error)
      this.handleReconnect()
    }
  }

  /**
   * 连接成功
   */
  onOpen() {
    console.log('[ImWebSocket] 连接成功')
    this.reconnectAttempts = 0

    // 发送认证消息
    this.sendAuth()

    // 启动心跳
    this.startHeartbeat()

    // 触发连接成功事件
    this.emit('connected')
  }

  /**
   * 接收消息
   */
  onMessage(event) {
    try {
      const data = JSON.parse(event.data)
      console.log('[ImWebSocket] 收到消息:', data)

      const { type, data: payload } = data

      switch (type) {
        case MESSAGE_TYPE.PONG:
          // 心跳响应
          console.log('[ImWebSocket] 收到心跳响应')
          break
        case MESSAGE_TYPE.MESSAGE:
          // 聊天消息
          this.emit('message', payload)
          break
        case MESSAGE_TYPE.READ:
          // 已读回执
          this.emit('read', payload)
          break
        case MESSAGE_TYPE.TYPING:
          // 正在输入
          this.emit('typing', payload)
          break
        case MESSAGE_TYPE.ONLINE:
          // 用户上线
          this.emit('online', payload)
          break
        case MESSAGE_TYPE.OFFLINE:
          // 用户下线
          this.emit('offline', payload)
          break
        case MESSAGE_TYPE.CALL:
          // 音视频通话
          this.emit('call', payload)
          break
        default:
          console.warn('[ImWebSocket] 未知消息类型:', type)
      }
    } catch (error) {
      console.error('[ImWebSocket] 解析消息失败:', error)
    }
  }

  /**
   * 连接错误
   */
  onError(error) {
    console.error('[ImWebSocket] 连接错误:', error)
    this.emit('error', error)
  }

  /**
   * 连接关闭
   */
  onClose(event) {
    console.log('[ImWebSocket] 连接关闭:', event.code, event.reason)

    // 停止心跳
    this.stopHeartbeat()

    // 触发断开连接事件
    this.emit('disconnected')

    // 尝试重连
    if (event.code !== 1000) { // 1000 表示正常关闭
      this.handleReconnect()
    }
  }

  /**
   * 发送认证消息
   */
  sendAuth() {
    this.send({
      type: MESSAGE_TYPE.AUTH,
      data: {
        token: this.token
      }
    })
  }

  /**
   * 发送消息
   * @param {Object} message - 消息对象
   * @returns {boolean} 是否发送成功
   */
  send(message) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.error('[ImWebSocket] WebSocket 未连接')
      return false
    }

    try {
      const data = typeof message === 'string' ? message : JSON.stringify(message)
      this.ws.send(data)
      console.log('[ImWebSocket] 发送消息:', message)
      return true
    } catch (error) {
      console.error('[ImWebSocket] 发送消息失败:', error)
      return false
    }
  }

  /**
   * 启动心跳
   */
  startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      this.send({
        type: MESSAGE_TYPE.PING,
        data: {
          timestamp: Date.now()
        }
      })
    }, this.heartbeatInterval)
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 处理重连
   */
  handleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('[ImWebSocket] 达到最大重连次数，停止重连')
      this.emit('reconnect-failed')
      return
    }

    this.reconnectAttempts++
    console.log(`[ImWebSocket] 尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)

    this.reconnectTimer = setTimeout(() => {
      this.connect(this.token)
    }, this.reconnectInterval)
  }

  /**
   * 关闭连接
   */
  close() {
    console.log('[ImWebSocket] 主动关闭连接')

    // 清除定时器
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    this.stopHeartbeat()

    // 关闭 WebSocket
    if (this.ws) {
      this.ws.close(1000, '正常关闭')
      this.ws = null
    }

    // 重置重连次数
    this.reconnectAttempts = 0
  }

  /**
   * 注册事件监听器
   * @param {string} event - 事件名称
   * @param {Function} handler - 事件处理函数
   */
  on(event, handler) {
    if (!this.eventHandlers.has(event)) {
      this.eventHandlers.set(event, [])
    }
    this.eventHandlers.get(event).push(handler)
  }

  /**
   * 移除事件监听器
   * @param {string} event - 事件名称
   * @param {Function} handler - 事件处理函数
   */
  off(event, handler) {
    if (!this.eventHandlers.has(event)) {
      return
    }
    const handlers = this.eventHandlers.get(event)
    const index = handlers.indexOf(handler)
    if (index !== -1) {
      handlers.splice(index, 1)
    }
  }

  /**
   * 触发事件
   * @param {string} event - 事件名称
   * @param {*} data - 事件数据
   */
  emit(event, data) {
    if (!this.eventHandlers.has(event)) {
      return
    }
    const handlers = this.eventHandlers.get(event)
    handlers.forEach(handler => {
      try {
        handler(data)
      } catch (error) {
        console.error(`[ImWebSocket] 事件处理器执行失败 (${event}):`, error)
      }
    })
  }

  /**
   * 获取连接状态
   * @returns {number} WebSocket 状态码
   */
  getReadyState() {
    return this.ws ? this.ws.readyState : WS_STATUS.CLOSED
  }

  /**
   * 是否已连接
   * @returns {boolean}
   */
  isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN
  }
}

// 导出单例
const imWebSocket = new ImWebSocket()
export default imWebSocket
