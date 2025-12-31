/**
 * @file nativeSocket.js
 * @description 原生 WebSocket 连接管理器 - 提供稳定可靠的 WebSocket 连接
 * @author IM System
 * @version 1.0.0
 */

import { getToken } from '@/utils/auth'

/**
 * WebSocket 连接状态枚举
 */
export const ConnectionState = {
  DISCONNECTED: 'disconnected',    // 未连接
  CONNECTING: 'connecting',        // 连接中
  CONNECTED: 'connected',          // 已连接
  RECONNECTING: 'reconnecting',    // 重连中
  CLOSED: 'closed',                // 已关闭（不再重连）
}

/**
 * 消息类型枚举
 */
export const MessageType = {
  // 系统消息
  HEARTBEAT: 'heartbeat',
  HEARTBEAT_ACK: 'heartbeat_ack',
  AUTH: 'auth',
  AUTH_SUCCESS: 'auth_success',
  AUTH_FAILED: 'auth_failed',

  // 聊天消息
  CHAT_MESSAGE: 'chat_message',
  MESSAGE_ACK: 'message_ack',
  MESSAGE_READ: 'message_read',
  MESSAGE_RECALL: 'message_recall',

  // 会话相关
  SESSION_UPDATE: 'session_update',
  TYPING: 'typing',
  STOP_TYPING: 'stop_typing',

  // 群组相关
  GROUP_UPDATE: 'group_update',
  MEMBER_UPDATE: 'member_update',

  // 用户相关
  USER_ONLINE: 'user_online',
  USER_OFFLINE: 'user_offline',
  USER_STATUS: 'user_status',

  // 通知
  NOTIFICATION: 'notification',
  FRIEND_REQUEST: 'friend_request',
}

/**
 * 原生 WebSocket 管理器类
 * @class NativeSocketManager
 * @description 管理 IM 系统的 WebSocket 连接，提供自动重连、心跳检测、消息队列等功能
 */
class NativeSocketManager {
  /**
   * 创建 WebSocket 管理器实例
   * @param {Object} options - 配置选项
   * @param {string} options.url - WebSocket 服务器地址
   * @param {number} options.heartbeatInterval - 心跳间隔（毫秒），默认 30000
   * @param {number} options.heartbeatTimeout - 心跳超时时间（毫秒），默认 10000
   * @param {number} options.reconnectInterval - 初始重连间隔（毫秒），默认 1000
   * @param {number} options.maxReconnectInterval - 最大重连间隔（毫秒），默认 30000
   * @param {number} options.maxReconnectAttempts - 最大重连次数，默认 -1（无限）
   * @param {boolean} options.autoConnect - 是否自动连接，默认 false
   * @param {boolean} options.debug - 是否开启调试日志，默认 false
   */
  constructor(options = {}) {
    // 配置选项
    this.config = {
      url: options.url || this._getDefaultUrl(),
      heartbeatInterval: options.heartbeatInterval || 30000,
      heartbeatTimeout: options.heartbeatTimeout || 10000,
      reconnectInterval: options.reconnectInterval || 1000,
      maxReconnectInterval: options.maxReconnectInterval || 30000,
      maxReconnectAttempts: options.maxReconnectAttempts ?? -1,
      autoConnect: options.autoConnect ?? false,
      debug: options.debug || false,
    }

    // WebSocket 实例
    this.ws = null

    // 连接状态
    this.state = ConnectionState.DISCONNECTED

    // 重连相关
    this.reconnectAttempts = 0
    this.reconnectTimer = null
    this.currentReconnectInterval = this.config.reconnectInterval

    // 心跳相关
    this.heartbeatTimer = null
    this.heartbeatTimeoutTimer = null
    this.lastHeartbeatTime = 0

    // 消息队列（离线时暂存）
    this.messageQueue = []
    this.maxQueueSize = 100

    // 事件处理器
    this.handlers = new Map()

    // 消息回调映射（用于请求-响应模式）
    this.pendingCallbacks = new Map()
    this.callbackTimeout = 30000

    // 消息序列号
    this.messageSeq = 0

    // 用户信息
    this.userId = null
    this.isAuthenticated = false

    // 自动连接
    if (this.config.autoConnect) {
      this.connect()
    }
  }

  /**
   * 获取默认 WebSocket URL
   * @private
   * @returns {string} WebSocket URL
   */
  _getDefaultUrl() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = import.meta.env.VITE_WS_HOST || window.location.host
    const path = import.meta.env.VITE_WS_PATH || '/ws/im'
    return `${protocol}//${host}${path}`
  }

  /**
   * 输出调试日志
   * @private
   * @param {string} level - 日志级别
   * @param {string} message - 日志消息
   * @param {*} data - 附加数据
   */
  _log(level, message, data = null) {
    if (!this.config.debug) return

    const timestamp = new Date().toISOString()
    const prefix = `[NativeSocket ${timestamp}]`

    switch (level) {
      case 'info':
        console.log(`${prefix} ${message}`, data || '')
        break
      case 'warn':
        console.warn(`${prefix} ${message}`, data || '')
        break
      case 'error':
        console.error(`${prefix} ${message}`, data || '')
        break
      default:
        console.log(`${prefix} ${message}`, data || '')
    }
  }

  /**
   * 连接 WebSocket
   * @returns {Promise<void>}
   */
  connect() {
    return new Promise((resolve, reject) => {
      if (this.state === ConnectionState.CONNECTED) {
        this._log('warn', '已经处于连接状态')
        resolve()
        return
      }

      if (this.state === ConnectionState.CONNECTING) {
        this._log('warn', '正在连接中')
        resolve()
        return
      }

      this._setState(ConnectionState.CONNECTING)
      this._log('info', '开始连接', this.config.url)

      try {
        // 获取认证 token
        const token = getToken()
        const url = token
          ? `${this.config.url}?token=${encodeURIComponent(token)}`
          : this.config.url

        this.ws = new WebSocket(url)

        // 连接成功
        this.ws.onopen = () => {
          this._log('info', '连接成功')
          this._setState(ConnectionState.CONNECTED)
          this.reconnectAttempts = 0
          this.currentReconnectInterval = this.config.reconnectInterval

          // 启动心跳
          this._startHeartbeat()

          // 发送排队的消息
          this._flushMessageQueue()

          // 触发连接事件
          this._emit('connect', { state: this.state })

          resolve()
        }

        // 连接关闭
        this.ws.onclose = (event) => {
          this._log('info', '连接关闭', { code: event.code, reason: event.reason })
          this._handleDisconnect(event)
        }

        // 连接错误
        this.ws.onerror = (error) => {
          this._log('error', '连接错误', error)
          this._emit('error', { error, type: 'connection' })

          if (this.state === ConnectionState.CONNECTING) {
            reject(error)
          }
        }

        // 接收消息
        this.ws.onmessage = (event) => {
          this._handleMessage(event)
        }
      } catch (error) {
        this._log('error', '创建连接失败', error)
        this._setState(ConnectionState.DISCONNECTED)
        reject(error)
      }
    })
  }

  /**
   * 断开连接
   * @param {number} code - 关闭代码
   * @param {string} reason - 关闭原因
   */
  disconnect(code = 1000, reason = '主动断开') {
    this._log('info', '主动断开连接', { code, reason })

    // 停止重连
    this._clearReconnectTimer()

    // 停止心跳
    this._stopHeartbeat()

    // 标记为已关闭
    this._setState(ConnectionState.CLOSED)

    // 关闭连接
    if (this.ws) {
      this.ws.close(code, reason)
      this.ws = null
    }

    // 清理回调
    this._clearPendingCallbacks()

    // 触发断开事件
    this._emit('disconnect', { code, reason, manual: true })
  }

  /**
   * 处理断开连接
   * @private
   * @param {CloseEvent} event - 关闭事件
   */
  _handleDisconnect(event) {
    // 停止心跳
    this._stopHeartbeat()

    // 清理回调
    this._clearPendingCallbacks()

    // 如果是主动关闭，不重连
    if (this.state === ConnectionState.CLOSED) {
      return
    }

    this._setState(ConnectionState.DISCONNECTED)
    this._emit('disconnect', { code: event.code, reason: event.reason, manual: false })

    // 尝试重连
    this._scheduleReconnect()
  }

  /**
   * 安排重连
   * @private
   */
  _scheduleReconnect() {
    // 检查是否超过最大重连次数
    if (this.config.maxReconnectAttempts >= 0 &&
        this.reconnectAttempts >= this.config.maxReconnectAttempts) {
      this._log('warn', '已达到最大重连次数')
      this._setState(ConnectionState.CLOSED)
      this._emit('reconnectFailed', { attempts: this.reconnectAttempts })
      return
    }

    this._setState(ConnectionState.RECONNECTING)
    this.reconnectAttempts++

    // 指数退避
    const interval = Math.min(
      this.currentReconnectInterval,
      this.config.maxReconnectInterval
    )

    this._log('info', `将在 ${interval}ms 后重连（第 ${this.reconnectAttempts} 次）`)

    this._emit('reconnecting', {
      attempt: this.reconnectAttempts,
      interval,
      maxAttempts: this.config.maxReconnectAttempts,
    })

    this._clearReconnectTimer()
    this.reconnectTimer = setTimeout(() => {
      this.connect().catch((error) => {
        this._log('error', '重连失败', error)
        // 增加重连间隔
        this.currentReconnectInterval = Math.min(
          this.currentReconnectInterval * 2,
          this.config.maxReconnectInterval
        )
      })
    }, interval)
  }

  /**
   * 清除重连定时器
   * @private
   */
  _clearReconnectTimer() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }

  /**
   * 启动心跳
   * @private
   */
  _startHeartbeat() {
    this._stopHeartbeat()

    this.heartbeatTimer = setInterval(() => {
      this._sendHeartbeat()
    }, this.config.heartbeatInterval)

    // 发送第一次心跳
    this._sendHeartbeat()
  }

  /**
   * 停止心跳
   * @private
   */
  _stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }

    if (this.heartbeatTimeoutTimer) {
      clearTimeout(this.heartbeatTimeoutTimer)
      this.heartbeatTimeoutTimer = null
    }
  }

  /**
   * 发送心跳
   * @private
   */
  _sendHeartbeat() {
    if (this.state !== ConnectionState.CONNECTED) return

    this.lastHeartbeatTime = Date.now()

    this._sendRaw({
      type: MessageType.HEARTBEAT,
      timestamp: this.lastHeartbeatTime,
    })

    // 设置心跳超时检测
    this.heartbeatTimeoutTimer = setTimeout(() => {
      this._log('warn', '心跳超时')
      this._emit('heartbeatTimeout')

      // 强制重连
      if (this.ws) {
        this.ws.close(4000, '心跳超时')
      }
    }, this.config.heartbeatTimeout)
  }

  /**
   * 处理接收到的消息
   * @private
   * @param {MessageEvent} event - 消息事件
   */
  _handleMessage(event) {
    try {
      const message = JSON.parse(event.data)
      this._log('info', '收到消息', message)

      // 处理心跳响应
      if (message.type === MessageType.HEARTBEAT_ACK) {
        this._handleHeartbeatAck(message)
        return
      }

      // 处理认证响应
      if (message.type === MessageType.AUTH_SUCCESS) {
        this.isAuthenticated = true
        this.userId = message.data?.userId
        this._emit('authenticated', message.data)
        return
      }

      if (message.type === MessageType.AUTH_FAILED) {
        this.isAuthenticated = false
        this._emit('authFailed', message.data)
        return
      }

      // 处理回调响应
      if (message.seq && this.pendingCallbacks.has(message.seq)) {
        const { resolve, timer } = this.pendingCallbacks.get(message.seq)
        clearTimeout(timer)
        this.pendingCallbacks.delete(message.seq)
        resolve(message)
        return
      }

      // 触发消息事件
      this._emit('message', message)
      this._emit(message.type, message.data)
    } catch (error) {
      this._log('error', '解析消息失败', error)
      this._emit('error', { error, type: 'parse' })
    }
  }

  /**
   * 处理心跳响应
   * @private
   * @param {Object} message - 心跳响应消息
   */
  _handleHeartbeatAck(message) {
    // 清除超时定时器
    if (this.heartbeatTimeoutTimer) {
      clearTimeout(this.heartbeatTimeoutTimer)
      this.heartbeatTimeoutTimer = null
    }

    const latency = Date.now() - this.lastHeartbeatTime
    this._log('info', `心跳响应延迟: ${latency}ms`)
    this._emit('heartbeat', { latency, serverTime: message.timestamp })
  }

  /**
   * 发送原始消息
   * @private
   * @param {Object} data - 消息数据
   */
  _sendRaw(data) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      this._log('warn', '连接未就绪，无法发送消息')
      return false
    }

    try {
      this.ws.send(JSON.stringify(data))
      return true
    } catch (error) {
      this._log('error', '发送消息失败', error)
      return false
    }
  }

  /**
   * 发送消息
   * @param {string} type - 消息类型
   * @param {*} data - 消息数据
   * @param {Object} options - 发送选项
   * @param {boolean} options.queue - 离线时是否加入队列，默认 true
   * @param {boolean} options.waitResponse - 是否等待响应，默认 false
   * @param {number} options.timeout - 响应超时时间，默认 30000ms
   * @returns {Promise<Object>|boolean} 发送结果
   */
  send(type, data, options = {}) {
    const {
      queue = true,
      waitResponse = false,
      timeout = this.callbackTimeout,
    } = options

    const message = {
      type,
      data,
      seq: ++this.messageSeq,
      timestamp: Date.now(),
    }

    // 如果未连接，加入队列
    if (this.state !== ConnectionState.CONNECTED) {
      if (queue && this.messageQueue.length < this.maxQueueSize) {
        this.messageQueue.push(message)
        this._log('info', '消息已加入队列', message)
        return waitResponse ? Promise.resolve(null) : false
      }
      return waitResponse ? Promise.reject(new Error('未连接')) : false
    }

    // 等待响应模式
    if (waitResponse) {
      return new Promise((resolve, reject) => {
        const timer = setTimeout(() => {
          this.pendingCallbacks.delete(message.seq)
          reject(new Error('响应超时'))
        }, timeout)

        this.pendingCallbacks.set(message.seq, { resolve, reject, timer })

        if (!this._sendRaw(message)) {
          clearTimeout(timer)
          this.pendingCallbacks.delete(message.seq)
          reject(new Error('发送失败'))
        }
      })
    }

    return this._sendRaw(message)
  }

  /**
   * 发送聊天消息
   * @param {string} sessionId - 会话 ID
   * @param {Object} messageData - 消息数据
   * @returns {Promise<Object>} 发送结果
   */
  sendChatMessage(sessionId, messageData) {
    return this.send(MessageType.CHAT_MESSAGE, {
      sessionId,
      ...messageData,
    }, { waitResponse: true })
  }

  /**
   * 发送已读回执
   * @param {string} sessionId - 会话 ID
   * @param {string} messageId - 消息 ID
   */
  sendReadReceipt(sessionId, messageId) {
    this.send(MessageType.MESSAGE_READ, { sessionId, messageId })
  }

  /**
   * 发送正在输入状态
   * @param {string} sessionId - 会话 ID
   */
  sendTyping(sessionId) {
    this.send(MessageType.TYPING, { sessionId })
  }

  /**
   * 发送停止输入状态
   * @param {string} sessionId - 会话 ID
   */
  sendStopTyping(sessionId) {
    this.send(MessageType.STOP_TYPING, { sessionId })
  }

  /**
   * 刷新消息队列
   * @private
   */
  _flushMessageQueue() {
    if (this.messageQueue.length === 0) return

    this._log('info', `发送队列中的 ${this.messageQueue.length} 条消息`)

    while (this.messageQueue.length > 0) {
      const message = this.messageQueue.shift()
      this._sendRaw(message)
    }
  }

  /**
   * 清理待处理的回调
   * @private
   */
  _clearPendingCallbacks() {
    for (const [seq, { reject, timer }] of this.pendingCallbacks) {
      clearTimeout(timer)
      reject(new Error('连接已断开'))
    }
    this.pendingCallbacks.clear()
  }

  /**
   * 设置连接状态
   * @private
   * @param {string} state - 新状态
   */
  _setState(state) {
    const oldState = this.state
    this.state = state

    if (oldState !== state) {
      this._emit('stateChange', { oldState, newState: state })
    }
  }

  /**
   * 注册事件处理器
   * @param {string} event - 事件名称
   * @param {Function} handler - 处理函数
   * @returns {Function} 取消注册的函数
   */
  on(event, handler) {
    if (!this.handlers.has(event)) {
      this.handlers.set(event, new Set())
    }
    this.handlers.get(event).add(handler)

    // 返回取消注册的函数
    return () => this.off(event, handler)
  }

  /**
   * 注册一次性事件处理器
   * @param {string} event - 事件名称
   * @param {Function} handler - 处理函数
   */
  once(event, handler) {
    const wrapper = (data) => {
      this.off(event, wrapper)
      handler(data)
    }
    this.on(event, wrapper)
  }

  /**
   * 移除事件处理器
   * @param {string} event - 事件名称
   * @param {Function} handler - 处理函数
   */
  off(event, handler) {
    if (!this.handlers.has(event)) return

    if (handler) {
      this.handlers.get(event).delete(handler)
    } else {
      this.handlers.delete(event)
    }
  }

  /**
   * 触发事件
   * @private
   * @param {string} event - 事件名称
   * @param {*} data - 事件数据
   */
  _emit(event, data) {
    if (!this.handlers.has(event)) return

    for (const handler of this.handlers.get(event)) {
      try {
        handler(data)
      } catch (error) {
        this._log('error', `事件处理器错误 [${event}]`, error)
      }
    }
  }

  /**
   * 获取当前连接状态
   * @returns {Object} 连接状态信息
   */
  getStatus() {
    return {
      state: this.state,
      isConnected: this.state === ConnectionState.CONNECTED,
      isAuthenticated: this.isAuthenticated,
      userId: this.userId,
      reconnectAttempts: this.reconnectAttempts,
      queuedMessages: this.messageQueue.length,
      pendingCallbacks: this.pendingCallbacks.size,
    }
  }

  /**
   * 销毁实例
   */
  destroy() {
    this.disconnect()
    this.handlers.clear()
    this.messageQueue = []
    this._log('info', '实例已销毁')
  }
}

// 创建单例实例
let instance = null

/**
 * 获取 NativeSocket 单例实例
 * @param {Object} options - 配置选项
 * @returns {NativeSocketManager} NativeSocket 实例
 */
export function getNativeSocket(options = {}) {
  if (!instance) {
    instance = new NativeSocketManager(options)
  }
  return instance
}

/**
 * 重置 NativeSocket 单例实例
 */
export function resetNativeSocket() {
  if (instance) {
    instance.destroy()
    instance = null
  }
}

export default NativeSocketManager
