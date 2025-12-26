import { getToken } from '@/utils/auth'

/**
 * 文档评论WebSocket客户端
 */
export default class CommentSocket {
  constructor(documentId) {
    this.documentId = documentId
    this.ws = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.handlers = new Map()
    this.connected = false
  }

  /**
   * 连接WebSocket
   */
  connect() {
    const token = getToken()
    const url = `${process.env.VUE_APP_WS_API}/document/${this.documentId}/comments?token=${token}`

    this.ws = new WebSocket(url)

    this.ws.onopen = () => {
      console.log('Comment WebSocket connected')
      this.connected = true
      this.reconnectAttempts = 0
      this.emit('connect')
    }

    this.ws.onclose = () => {
      console.log('Comment WebSocket closed')
      this.connected = false
      this.emit('disconnect')
      this.reconnect()
    }

    this.ws.onerror = error => {
      console.error('Comment WebSocket error:', error)
      this.emit('error', error)
    }

    this.ws.onmessage = event => {
      try {
        const message = JSON.parse(event.data)
        this.handleMessage(message)
      } catch (error) {
        console.error('Failed to parse message:', error)
      }
    }
  }

  /**
   * 重新连接
   */
  reconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log('Max reconnection attempts reached')
      return
    }

    this.reconnectAttempts++
    console.log(`Reconnecting... Attempt ${this.reconnectAttempts}`)

    setTimeout(() => {
      this.connect()
    }, this.reconnectInterval)
  }

  /**
   * 关闭连接
   */
  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  /**
   * 发送消息
   */
  send(type, data) {
    if (!this.connected) {
      console.warn('WebSocket is not connected')
      return
    }

    const message = {
      type,
      data,
    }

    this.ws.send(JSON.stringify(message))
  }

  /**
   * 处理接收到的消息
   */
  handleMessage(message) {
    const { type, data } = message

    switch (type) {
      case 'comment_added':
        this.emit('commentAdded', data)
        break
      case 'comment_deleted':
        this.emit('commentDeleted', data)
        break
      case 'reply_added':
        this.emit('replyAdded', data)
        break
      case 'reply_deleted':
        this.emit('replyDeleted', data)
        break
      case 'comment_updated':
        this.emit('commentUpdated', data)
        break
      default:
        console.warn('Unknown message type:', type)
    }
  }

  /**
   * 注册事件处理器
   */
  on(event, handler) {
    if (!this.handlers.has(event)) {
      this.handlers.set(event, [])
    }
    this.handlers.get(event).push(handler)
  }

  /**
   * 移除事件处理器
   */
  off(event, handler) {
    if (!this.handlers.has(event)) {
      return
    }
    if (!handler) {
      this.handlers.delete(event)
      return
    }
    const handlers = this.handlers.get(event)
    const index = handlers.indexOf(handler)
    if (index !== -1) {
      handlers.splice(index, 1)
    }
  }

  /**
   * 触发事件
   */
  emit(event, data) {
    if (!this.handlers.has(event)) {
      return
    }
    this.handlers.get(event).forEach(handler => {
      try {
        handler(data)
      } catch (error) {
        console.error('Error in event handler:', error)
      }
    })
  }
}
