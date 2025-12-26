import store from '@/store'
import { getToken } from '@/utils/auth'
import envConfig from '../../env.config.js'

class WebSocketClient {
  constructor() {
    this.ws = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.heartbeatInterval = null
    this.isConnected = false
    this.eventListeners = {}
  }

  init() {
    if (import.meta.env.DEV) {
      // 开发环境下模拟WebSocket
      this.mockWebSocket()
      return
    }

    const token = getToken()
    if (!token) {
      console.warn('WebSocket: No token found')
      return
    }

    const wsUrl = `${envConfig.wsAPI}?token=${token}`
    this.connect(wsUrl)
  }

  connect(url) {
    try {
      this.ws = new WebSocket(url)
      this.bindEvents()
    } catch (error) {
      console.error('WebSocket connection error:', error)
      this.handleReconnect()
    }
  }

  bindEvents() {
    this.ws.onopen = () => {
      console.log('WebSocket connected')
      this.isConnected = true
      this.reconnectAttempts = 0
      this.startHeartbeat()
      store.dispatch('im/updateOnlineStatus', { userId: store.state.user.userId, status: true })
    }

    this.ws.onclose = () => {
      console.log('WebSocket closed')
      this.isConnected = false
      this.handleReconnect()
      store.dispatch('im/updateOnlineStatus', { userId: store.state.user.userId, status: false })
    }

    this.ws.onerror = error => {
      console.error('WebSocket error:', error)
      this.isConnected = false
    }

    this.ws.onmessage = event => {
      try {
        const data = JSON.parse(event.data)
        this.handleMessage(data)
      } catch (error) {
        console.error('Failed to parse WebSocket message:', error)
      }
    }
  }

  handleMessage(data) {
    switch (data.type) {
      case 'chat':
        // 处理聊天消息
        store.dispatch('im/addMessage', {
          sessionId: data.sessionId,
          message: data.message,
        })
        break
      case 'status':
        // 处理状态更新
        store.dispatch('im/updateOnlineStatus', {
          userId: data.userId,
          status: data.status,
        })
        break
      case 'typing':
        // 处理正在输入状态
        break
      case 'recall':
        // 处理消息撤回
        break
      case 'call-offer':
      case 'call-answer':
      case 'call-candidate':
      case 'call-reject':
      case 'call-cancel':
      case 'call-busy':
      case 'call-end':
        // 触发通话相关事件
        this.emit(data.type, data)
        break
      default:
        console.warn('Unknown message type:', data.type)
    }
  }

  handleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('Max reconnection attempts reached')
      return
    }

    setTimeout(() => {
      this.reconnectAttempts++
      console.log(
        `Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`
      )
      this.init()
    }, this.reconnectInterval)
  }

  startHeartbeat() {
    this.heartbeatInterval = setInterval(() => {
      if (this.isConnected) {
        this.send({ type: 'heartbeat' })
      }
    }, 30000)
  }

  send(data) {
    if (!this.isConnected) {
      console.warn('WebSocket is not connected')
      return
    }

    try {
      this.ws.send(JSON.stringify(data))
    } catch (error) {
      console.error('Failed to send message:', error)
    }
  }

  close() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
    this.isConnected = false
  }

  // 添加事件监听器
  on(event, callback) {
    if (!this.eventListeners[event]) {
      this.eventListeners[event] = []
    }
    this.eventListeners[event].push(callback)
  }

  // 移除事件监听器
  off(event, callback) {
    if (!this.eventListeners[event]) return
    const index = this.eventListeners[event].indexOf(callback)
    if (index > -1) {
      this.eventListeners[event].splice(index, 1)
    }
  }

  // 触发事件
  emit(event, data) {
    if (!this.eventListeners[event]) return
    this.eventListeners[event].forEach(callback => {
      try {
        callback(data)
      } catch (error) {
        console.error(`Error in event listener for ${event}:`, error)
      }
    })
  }

  // 开发环境下模拟WebSocket功能
  mockWebSocket() {
    this.isConnected = true
    console.log('Mock WebSocket connected')

    // 模拟接收消息
    setInterval(() => {
      if (Math.random() > 0.7) {
        this.handleMessage({
          type: 'chat',
          sessionId: '1',
          message: {
            id: Date.now().toString(),
            sessionId: '1',
            fromId: '2',
            fromName: '张三',
            content: '这是一条模拟消息 ' + new Date().toLocaleTimeString(),
            type: 'text',
            createTime: new Date().toISOString(),
          },
        })
      }
    }, 10000)
  }
}

export default new WebSocketClient()
