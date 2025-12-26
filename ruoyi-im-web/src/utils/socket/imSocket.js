import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
import store from '@/store'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import { logSocketEvent, logMessageEvent, logSecurityEvent } from '@/api/im/audit'

class IMSocket {
  constructor() {
    this.stompClient = null
    this.connected = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 10
    this.reconnectDelay = 1000
    this.heartbeatInterval = null
    this.messageQueue = new Map()
    this.subscriptions = new Map()
    this.connectionPromise = null
  }

  // 初始化连接
  async connect() {
    if (this.connectionPromise) {
      return this.connectionPromise
    }

    this.connectionPromise = new Promise((resolve, reject) => {
      try {
        const socket = new SockJS('/ws')
        this.stompClient = Stomp.over(socket)

        // 配置STOMP客户端
        this.stompClient.heartbeat.outgoing = 20000 // 20秒发送一次心跳
        this.stompClient.heartbeat.incoming = 0 // 服务器心跳

        const headers = {
          Authorization: getToken(),
        }

        this.stompClient.connect(
          headers,
          frame => {
            this.onConnected(frame)
            resolve(true)
          },
          error => {
            this.onError(error)
            reject(error)
          }
        )

        // 记录连接尝试
        logSocketEvent({
          type: 'CONNECT_ATTEMPT',
          status: 'PENDING',
          details: 'Attempting to establish WebSocket connection',
        })
      } catch (error) {
        this.onError(error)
        reject(error)
      }
    })

    return this.connectionPromise
  }

  // 连接成功处理
  onConnected(frame) {
    this.connected = true
    this.reconnectAttempts = 0
    this.connectionPromise = null

    // 记录连接成功
    logSocketEvent({
      type: 'CONNECT_SUCCESS',
      status: 'SUCCESS',
      details: `Connected with session: ${frame.headers['user-name']}`,
    })

    // 订阅频道
    this.subscribe('/topic/messages', this.handleMessage.bind(this))
    this.subscribe('/user/queue/messages', this.handlePrivateMessage.bind(this))
    this.subscribe('/topic/presence', this.handlePresence.bind(this))

    // 启动心跳检测
    this.startHeartbeat()

    // 处理离线消息队列
    this.processMessageQueue()

    // 通知状态变化
    store.dispatch('im/setConnectionStatus', true)
  }

  // 错误处理
  onError(error) {
    this.connected = false
    this.connectionPromise = null

    // 记录错误
    logSocketEvent({
      type: 'CONNECT_ERROR',
      status: 'ERROR',
      details: error.message || 'Connection error occurred',
    })

    // 尝试重连
    this.reconnect()

    // 通知状态变化
    store.dispatch('im/setConnectionStatus', false)
  }

  // 重连机制
  async reconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      ElMessage.error('连接失败，请刷新页面重试')
      return
    }

    this.reconnectAttempts++
    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000)

    await new Promise(resolve => setTimeout(resolve, delay))
    this.connect()
  }

  // 心跳检测
  startHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
    }

    this.heartbeatInterval = setInterval(() => {
      if (this.connected) {
        this.send('/app/heartbeat', {
          timestamp: new Date().getTime(),
        })
      }
    }, 20000)
  }

  // 订阅主题
  subscribe(destination, callback) {
    if (!this.connected) {
      return
    }

    if (!this.subscriptions.has(destination)) {
      const subscription = this.stompClient.subscribe(destination, message => {
        try {
          const payload = JSON.parse(message.body)
          callback(payload)

          // 记录消息接收
          logMessageEvent({
            type: 'MESSAGE_RECEIVED',
            destination,
            messageId: message.headers['message-id'],
            payload,
          })
        } catch (error) {
          console.error('Message handling error:', error)
        }
      })

      this.subscriptions.set(destination, subscription)
    }
  }

  // 发送消息
  async send(destination, message) {
    if (!this.connected) {
      // 将消息加入队列
      this.queueMessage(destination, message)
      await this.connect()
      return
    }

    const messageId = this.generateMessageId()
    const timestamp = new Date().getTime()

    try {
      this.stompClient.send(
        destination,
        {
          'message-id': messageId,
          timestamp: timestamp,
        },
        JSON.stringify({
          ...message,
          messageId,
          timestamp,
        })
      )

      // 记录消息发送
      logMessageEvent({
        type: 'MESSAGE_SENT',
        destination,
        messageId,
        payload: message,
      })

      return messageId
    } catch (error) {
      // 记录发送失败
      logMessageEvent({
        type: 'MESSAGE_SEND_FAILED',
        destination,
        messageId,
        error: error.message,
      })

      throw error
    }
  }

  // 消息队列处理
  queueMessage(destination, message) {
    const queueItem = {
      destination,
      message,
      timestamp: new Date().getTime(),
      retries: 0,
    }
    this.messageQueue.set(this.generateMessageId(), queueItem)
  }

  // 处理队列中的消息
  async processMessageQueue() {
    if (!this.connected || this.messageQueue.size === 0) {
      return
    }

    for (const [messageId, item] of this.messageQueue) {
      try {
        await this.send(item.destination, item.message)
        this.messageQueue.delete(messageId)
      } catch (error) {
        if (item.retries < 3) {
          item.retries++
        } else {
          this.messageQueue.delete(messageId)
          // 记录消息发送失败
          logSecurityEvent({
            type: 'MESSAGE_DELIVERY_FAILED',
            messageId,
            details: error.message,
          })
        }
      }
    }
  }

  // 消息处理
  handleMessage(message) {
    store.dispatch('im/addMessage', message)
  }

  // 私信处理
  handlePrivateMessage(message) {
    store.dispatch('im/addPrivateMessage', message)
  }

  // 在线状态处理
  handlePresence(presence) {
    store.dispatch('im/updatePresence', presence)
  }

  // 生成消息ID
  generateMessageId() {
    return `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  // 断开连接
  disconnect() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
    }

    if (this.stompClient && this.connected) {
      this.stompClient.disconnect(() => {
        this.connected = false
        this.subscriptions.clear()

        // 记录断开连接
        logSocketEvent({
          type: 'DISCONNECT',
          status: 'SUCCESS',
          details: 'Client initiated disconnect',
        })
      })
    }
  }
}

const imSocket = new IMSocket()
export default imSocket
