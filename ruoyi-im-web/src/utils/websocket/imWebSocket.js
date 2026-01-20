/**
 * WebSocket 连接管理器
 * 用于管理与后端的实时通信
 */
import { ElMessage } from 'element-plus'

/**
 * WebSocket 消息类型
 */
export const WS_MESSAGE_TYPE = {
  AUTH: 'auth',           // 认证
  MESSAGE: 'message',     // 消息
  PING: 'ping',           // 心跳
  PONG: 'pong',           // 心跳响应
  TYPING: 'typing',       // 正在输入
  READ: 'read',           // 已读回执
  ONLINE: 'online',       // 上线
  OFFLINE: 'offline',     // 离线
  CALL: 'call'            // 通话相关
}

/**
 * WebSocket 连接状态
 */
export const WS_STATUS = {
  CONNECTING: 0,      // 连接中
  OPEN: 1,           // 已连接
  CLOSING: 2,        // 关闭中
  CLOSED: 3          // 已关闭
}

/**
 * WebSocket 管理器类
 */
class IMWebSocketManager {
  constructor() {
    this.ws = null
    this.url = ''
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.heartbeatInterval = 30000
    this.heartbeatTimer = null
    this.reconnectTimer = null
    this.listeners = new Map()
    this.isConnected = false
  }

  /**
   * 连接 WebSocket
   * @param {string} token - 认证令牌
   */
  connect(token) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('[WebSocket] 已连接，无需重复连接')
      return
    }

    // 构建 WebSocket URL
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const wsHost = import.meta.env.VITE_WS_HOST || window.location.hostname
    const wsPort = import.meta.env.VITE_WS_PORT || '8080'
    this.url = `${wsProtocol}//${wsHost}:${wsPort}/ws/im?token=${token}`

    console.log('[WebSocket] 正在连接:', this.url.replace(token, '***'))

    try {
      this.ws = new WebSocket(this.url)

      // 连接打开
      this.ws.onopen = () => {
        console.log('[WebSocket] 连接成功')
        this.isConnected = true
        this.reconnectAttempts = 0

        // 启动心跳
        this.startHeartbeat()

        // 触发连接成功事件
        this.emit('connected')
      }

      // 接收消息
      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          console.log('[WebSocket] 收到消息:', data)

          // 处理心跳响应
          if (data.type === WS_MESSAGE_TYPE.PONG) {
            return
          }

          // 触发消息事件
          this.emit(data.type, data)
        } catch (error) {
          console.error('[WebSocket] 消息解析失败:', error)
        }
      }

      // 连接关闭
      this.ws.onclose = (event) => {
        console.log('[WebSocket] 连接关闭:', event.code, event.reason)
        this.isConnected = false

        // 停止心跳
        this.stopHeartbeat()

        // 触发断开连接事件
        this.emit('disconnected', { code: event.code, reason: event.reason })

        // 自动重连
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
          this.scheduleReconnect(token)
        } else {
          ElMessage.error('WebSocket 连接失败，请刷新页面重试')
        }
      }

      // 连接错误
      this.ws.onerror = (error) => {
        console.error('[WebSocket] 连接错误:', error)
        this.emit('error', error)
      }
    } catch (error) {
      console.error('[WebSocket] 创建连接失败:', error)
      ElMessage.error('无法创建 WebSocket 连接')
    }
  }

  /**
   * 发送消息
   * @param {Object} message - 消息对象
   */
  send(message) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.error('[WebSocket] 未连接，无法发送消息')
      return false
    }

    try {
      const data = JSON.stringify(message)
      this.ws.send(data)
      console.log('[WebSocket] 发送消息:', message)
      return true
    } catch (error) {
      console.error('[WebSocket] 发送消息失败:', error)
      return false
    }
  }

  /**
   * 认证
   * @param {string} token - 认证令牌
   */
  authenticate(token) {
    this.send({
      type: WS_MESSAGE_TYPE.AUTH,
      payload: { token }
    })
  }

  /**
   * 发送心跳
   */
  ping() {
    this.send({
      type: WS_MESSAGE_TYPE.PING,
      payload: { timestamp: Date.now() }
    })
  }

  /**
   * 启动心跳
   */
  startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      this.ping()
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
   * 安排重连
   * @param {string} token - 认证令牌
   */
  scheduleReconnect(token) {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
    }

    this.reconnectAttempts++
    const delay = this.reconnectInterval * this.reconnectAttempts

    console.log(`[WebSocket] ${delay / 1000}秒后进行第${this.reconnectAttempts}次重连...`)

    this.reconnectTimer = setTimeout(() => {
      console.log(`[WebSocket] 开始第${this.reconnectAttempts}次重连`)
      this.connect(token)
    }, delay)
  }

  /**
   * 关闭连接
   */
  close() {
    console.log('[WebSocket] 主动关闭连接')

    // 停止心跳
    this.stopHeartbeat()

    // 取消重连
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    // 关闭连接
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }

    this.isConnected = false
    this.reconnectAttempts = 0
  }

  /**
   * 添加事件监听器
   * @param {string} event - 事件名称
   * @param {Function} callback - 回调函数
   */
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  /**
   * 移除事件监听器
   * @param {string} event - 事件名称
   * @param {Function} callback - 回调函数
   */
  off(event, callback) {
    if (!this.listeners.has(event)) {
      return
    }

    const callbacks = this.listeners.get(event)
    const index = callbacks.indexOf(callback)
    if (index > -1) {
      callbacks.splice(index, 1)
    }
  }

  /**
   * 触发事件
   * @param {string} event - 事件名称
   * @param {*} data - 事件数据
   */
  emit(event, data) {
    if (!this.listeners.has(event)) {
      return
    }

    const callbacks = this.listeners.get(event)
    callbacks.forEach(callback => {
      try {
        callback(data)
      } catch (error) {
        console.error(`[WebSocket] 事件处理器错误 [${event}]:`, error)
      }
    })
  }

  /**
   * 获取连接状态
   * @returns {boolean}
   */
  getConnectionState() {
    if (!this.ws) {
      return WS_STATUS.CLOSED
    }
    return this.ws.readyState
  }
}

// 创建单例实例
const imWebSocket = new IMWebSocketManager()

export default imWebSocket
