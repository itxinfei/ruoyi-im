/**
 * IM WebSocket 客户端 - 增强版
 * 提供 WebSocket 连接管理、消息收发、心跳保活、断线重连等功能
 *
 * 优化特性：
 * - 消息批量处理（减少渲染次数）
 * - 指数退避重连策略
 * - 连接质量监控
 * - 消息队列缓冲
 * - 离线消息暂存
 * - 发送失败重试
 */
import { getUserInfo } from '../storage'
import { debug, info, warn, error } from '../logger.js'

// WebSocket 连接状态
export const WS_STATUS = {
  CONNECTING: 0,
  OPEN: 1,
  CLOSING: 2,
  CLOSED: 3
}

// 消息类型
export const MESSAGE_TYPE = {
  AUTH: 'auth',
  MESSAGE: 'message',
  MESSAGE_STATUS: 'message_status',
  MESSAGE_ACK: 'message_ack',
  PING: 'ping',
  PONG: 'pong',
  READ: 'read',
  TYPING: 'typing',
  STOP_TYPING: 'stop-typing',
  ONLINE: 'online',
  OFFLINE: 'offline',
  CALL: 'call',
  REACTION: 'reaction',
  RECALL: 'recall',
  CONVERSATION_EVENT: 'conversation_event'
}

// 性能优化配置
const BATCH_INTERVAL = 50 // 消息批量处理间隔（毫秒）
const BATCH_MAX_SIZE = 20 // 批量处理最大消息数
const BASE_RECONNECT_INTERVAL = 1000
const MAX_RECONNECT_INTERVAL = 30000
const PENDING_MESSAGES_MAX = 100

class ImWebSocket {
  constructor() {
    this.ws = null
    this.url = ''
    this.token = ''
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 10
    this.reconnectInterval = BASE_RECONNECT_INTERVAL
    this.heartbeatInterval = 30000
    this.eventHandlers = new Map()
    this.storeConnectionCallback = null

    this.messageBuffer = []
    this.batchTimer = null
    this.lastPongTime = Date.now()

    this.connectionQuality = 'good'
    this.pingLatencies = []

    this.pendingMessages = []
    this.isProcessingPending = false
  }

  /**
   * 设置连接状态回调（用于同步 Vuex store）
   * @param {Function} callback - 回调函数，接收 (connected: boolean) 参数
   */
  setStoreConnectionCallback(callback) {
    this.storeConnectionCallback = callback
  }

  updateStoreConnectionState(connected) {
    if (this.storeConnectionCallback) {
      try {
        this.storeConnectionCallback(connected)
      } catch (e) {
        error('ImWebSocket', '更新 store 连接状态失败:', e)
      }
    }
  }

  /**
   * 连接 WebSocket
   * @param {string} token - 认证令牌
   */
  connect(token) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      warn('ImWebSocket', 'WebSocket 已连接')
      return
    }

    this.token = token
    const wsBaseUrl = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080'

    // 从 localStorage 获取 userId
    let userId = ''
    try {
      const user = getUserInfo()
      if (user?.id) {
        userId = user.id
        debug('ImWebSocket', '用户ID:', userId)
      }
    } catch (e) {
      warn('ImWebSocket', '获取用户ID失败:', e)
    }

    // 同时发送 token 和 userId
    this.url = `${wsBaseUrl}/ws/im?token=${token}&userId=${userId}`

    try {
      debug('ImWebSocket', '正在连接...', this.url)
      this.ws = new WebSocket(this.url)

      // 绑定事件
      this.ws.onopen = this.onOpen.bind(this)
      this.ws.onmessage = this.onMessage.bind(this)
      this.ws.onerror = this.onError.bind(this)
      this.ws.onclose = this.onClose.bind(this)
    } catch (err) {
      error('ImWebSocket', '连接失败:', err)
      this.handleReconnect()
    }
  }

  /**
   * 连接成功
   */
  onOpen() {
    info('ImWebSocket', '连接成功')
    this.reconnectAttempts = 0
    this.reconnectInterval = BASE_RECONNECT_INTERVAL // 重置重连间隔

    // 发送认证消息
    this.sendAuth()

    // 启动心跳
    this.startHeartbeat()

    // 触发连接成功事件
    this.emit('connected')

    // 更新 Vuex store 的连接状态
    this.updateStoreConnectionState(true)

    // ========== 处理待发送消息队列 ==========
    if (this.pendingMessages.length > 0) {
      info('ImWebSocket', `有 ${this.pendingMessages.length} 条待发送消息`)
      // 延迟处理，等待认证完成
      setTimeout(() => {
        this.processPendingMessages()
      }, 500)
    }
  }

  /**
   * 接收消息 - 支持批量处理
   */
  onMessage(event) {
    try {
      const data = JSON.parse(event.data)
      debug('ImWebSocket', '收到消息:', data)

      const { type, data: payload } = data

      switch (type) {
        case MESSAGE_TYPE.PONG:
          // 心跳响应 - 计算延迟
          this.handlePong()
          break
        case MESSAGE_TYPE.MESSAGE:
          this.bufferMessage('message', payload)
          break
        case MESSAGE_TYPE.MESSAGE_STATUS:
          this.emit('message_status', payload)
          break
        case MESSAGE_TYPE.MESSAGE_ACK:
          this.emit('message_ack', payload)
          break
        case MESSAGE_TYPE.READ:
          this.bufferMessage('read', payload)
          break
        case MESSAGE_TYPE.TYPING:
          this.emit('typing', payload)
          break
        case MESSAGE_TYPE.ONLINE:
          this.emit('online', payload)
          break
        case MESSAGE_TYPE.OFFLINE:
          this.emit('offline', payload)
          break
        case MESSAGE_TYPE.CALL:
          this.emit('call', payload)
          break
        case MESSAGE_TYPE.REACTION:
          this.emit('reaction', payload)
          break
        case MESSAGE_TYPE.RECALL:
          this.emit('recall', payload)
          break
        default:
          warn('ImWebSocket', '未知消息类型:', type)
      }
    } catch (err) {
      error('ImWebSocket', '解析消息失败:', err)
    }
  }

  /**
   * 处理心跳响应
   */
  handlePong() {
    const latency = Date.now() - this.lastPongTime
    this.pingLatencies.push(latency)

    // 保留最近10次延迟记录
    if (this.pingLatencies.length > 10) {
      this.pingLatencies.shift()
    }

    // 计算平均延迟并更新连接质量
    const avgLatency = this.pingLatencies.reduce((a, b) => a + b, 0) / this.pingLatencies.length
    if (avgLatency < 100) {
      this.connectionQuality = 'good'
    } else if (avgLatency < 300) {
      this.connectionQuality = 'fair'
    } else {
      this.connectionQuality = 'poor'
    }

    debug('ImWebSocket', `心跳延迟: ${latency}ms, 连接质量: ${this.connectionQuality}`)
  }

  /**
   * 消息缓冲（批量处理）
   */
  bufferMessage(type, payload) {
    this.messageBuffer.push({ type, payload })

    // 如果缓冲区已满，立即处理
    if (this.messageBuffer.length >= BATCH_MAX_SIZE) {
      this.flushMessageBuffer()
      return
    }

    // 启动批量处理定时器
    if (!this.batchTimer) {
      this.batchTimer = setTimeout(() => {
        this.flushMessageBuffer()
      }, BATCH_INTERVAL)
    }
  }

  /**
   * 刷新消息缓冲区
   */
  flushMessageBuffer() {
    if (this.batchTimer) {
      clearTimeout(this.batchTimer)
      this.batchTimer = null
    }

    if (this.messageBuffer.length === 0) { return }

    // 按类型分组处理
    const messagesByType = {}
    this.messageBuffer.forEach(({ type, payload }) => {
      if (!messagesByType[type]) {
        messagesByType[type] = []
      }
      messagesByType[type].push(payload)
    })

    // 批量触发事件
    Object.keys(messagesByType).forEach(type => {
      const messages = messagesByType[type]
      if (messages.length === 1) {
        this.emit(type, messages[0])
      } else {
        // 批量消息事件
        this.emit(`${type}:batch`, messages)
        // 同时触发单条消息事件（兼容性）
        messages.forEach(msg => this.emit(type, msg))
      }
    })

    // 清空缓冲区
    this.messageBuffer = []
  }

  /**
   * 连接错误
   */
  onError(err) {
    error('ImWebSocket', '连接错误:', err)
    this.emit('error', err)
  }

  /**
   * 连接关闭
   */
  onClose(event) {
    info('ImWebSocket', '连接关闭:', event.code, event.reason)

    // 停止心跳
    this.stopHeartbeat()

    // 刷新剩余消息
    this.flushMessageBuffer()

    // 触发断开连接事件
    this.emit('disconnected')

    // 更新 Vuex store 的连接状态
    this.updateStoreConnectionState(false)

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
    // 如果未连接，将消息加入待发送队列
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      warn('ImWebSocket', 'WebSocket 未连接，消息加入待发送队列')
      this.addPendingMessage(message)
      return false
    }

    try {
      const data = typeof message === 'string' ? message : JSON.stringify(message)
      this.ws.send(data)
      debug('ImWebSocket', '发送消息:', message)
      return true
    } catch (err) {
      error('ImWebSocket', '发送消息失败:', err)
      // 发送失败，加入待发送队列
      this.addPendingMessage(message)
      return false
    }
  }

  /**
   * 启动心跳
   */
  startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      this.lastPongTime = Date.now()
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
   * 发送正在输入状态
   * @param {string} conversationId - 会话ID
   */
  sendTyping(conversationId) {
    this.send({
      type: MESSAGE_TYPE.TYPING,
      data: {
        conversationId,
        isTyping: true,
        timestamp: Date.now()
      }
    })
  }

  /**
   * 发送停止输入状态
   * @param {string} conversationId - 会话ID
   */
  sendStopTyping(conversationId) {
    this.send({
      type: MESSAGE_TYPE.STOP_TYPING,
      data: {
        conversationId,
        timestamp: Date.now()
      }
    })
  }

  /**
   * 发送消息ACK确认
   * @param {string} messageId - 消息ID
   * @param {string} ackType - ACK类型：receive（已接收）/ read（已读）
   * @param {string} [deviceId] - 设备ID
   */
  sendAck(messageId, ackType, deviceId) {
    const data = {
      messageId: messageId,
      ackType: ackType,
      timestamp: Date.now()
    }
    if (deviceId) {
      data.deviceId = deviceId
    }
    this.send({
      type: MESSAGE_TYPE.MESSAGE_ACK,
      data
    })
    debug('ImWebSocket', '发送ACK:', data)
  }

  /**
   * 处理重连 - 指数退避策略
   */
  handleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      error('ImWebSocket', '达到最大重连次数，停止重连')
      this.emit('reconnect-failed')
      return
    }

    // 清除已有的重连定时器，防止堆叠
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    this.reconnectAttempts++

    // 指数退避：每次重连增加延迟，最大不超过 MAX_RECONNECT_INTERVAL
    this.reconnectInterval = Math.min(
      BASE_RECONNECT_INTERVAL * Math.pow(2, this.reconnectAttempts - 1),
      MAX_RECONNECT_INTERVAL
    )

    info('ImWebSocket', `尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})，${this.reconnectInterval}ms 后重试`)

    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      this.connect(this.token)
    }, this.reconnectInterval)
  }

  /**
   * 关闭连接
   */
  close() {
    info('ImWebSocket', '主动关闭连接')

    // 清除定时器
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    this.stopHeartbeat()

    // 刷新剩余消息
    this.flushMessageBuffer()

    // 关闭 WebSocket
    if (this.ws) {
      this.ws.close(1000, '正常关闭')
      this.ws = null
    }

    // 重置重连次数
    this.reconnectAttempts = 0
    this.reconnectInterval = BASE_RECONNECT_INTERVAL
  }

  /**
   * 完全销毁 WebSocket 实例（登出时调用）
   * 关闭连接并清空所有事件监听器
   */
  destroy() {
    this.close()
    this.removeAllListeners()
    this.token = ''
    this.url = ''
    this.messageBuffer = []
    this.pingLatencies = []
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
   * 移除指定事件的所有监听器或全部监听器
   * @param {string} [event] - 事件名称，不传则清空所有监听器
   */
  removeAllListeners(event) {
    if (event) {
      this.eventHandlers.delete(event)
    } else {
      // 如果没有指定事件，清空所有监听器
      this.eventHandlers.clear()
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
      } catch (err) {
        error('ImWebSocket', `事件处理器执行失败 (${event}):`, err)
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

  /**
   * 获取连接质量
   * @returns {string} good | fair | poor
   */
  getConnectionQuality() {
    return this.connectionQuality
  }

  // ========== 发送队列管理方法 ==========

  /**
   * 添加消息到待发送队列
   * @param {Object} message - 消息对象
   */
  addPendingMessage(message) {
    if (this.pendingMessages.length >= PENDING_MESSAGES_MAX) {
      // 队列已满，移除最旧的消息
      this.pendingMessages.shift()
      warn('ImWebSocket', '待发送队列已满，移除最旧消息')
    }
    this.pendingMessages.push({
      ...message,
      timestamp: Date.now()
    })
    debug('ImWebSocket', '添加待发送消息，队列长度:', this.pendingMessages.length)
  }

  /**
   * 处理待发送消息队列
   * 在连接恢复时调用
   */
  async processPendingMessages() {
    if (this.isProcessingPending || this.pendingMessages.length === 0) {
      return
    }

    if (!this.isConnected()) {
      warn('ImWebSocket', '未连接，无法处理待发送消息')
      return
    }

    this.isProcessingPending = true
    info('ImWebSocket', `开始处理 ${this.pendingMessages.length} 条待发送消息`)

    const messagesToSend = [...this.pendingMessages]
    this.pendingMessages = []

    try {
      for (let i = 0; i < messagesToSend.length; i++) {
        if (!this.isConnected()) {
          // 连接断开，将剩余消息放回队列
          this.pendingMessages.unshift(...messagesToSend.slice(i))
          break
        }

        // 重新发送消息
        this.send(messagesToSend[i])
        // 稍微延迟，避免消息堆积
        await new Promise(resolve => setTimeout(resolve, 50))
      }
    } catch (e) {
      error('ImWebSocket', '处理待发送消息失败:', e)
    } finally {
      this.isProcessingPending = false
    }

    if (this.pendingMessages.length > 0) {
      info('ImWebSocket', `还有 ${this.pendingMessages.length} 条消息待发送`)
    }
  }

  /**
   * 清空待发送队列
   */
  clearPendingMessages() {
    const count = this.pendingMessages.length
    this.pendingMessages = []
    info('ImWebSocket', `清空待发送队列，移除 ${count} 条消息`)
  }

  /**
   * 获取待发送消息数量
   */
  getPendingMessageCount() {
    return this.pendingMessages.length
  }
}

// 导出单例
const imWebSocket = new ImWebSocket()
export default imWebSocket

