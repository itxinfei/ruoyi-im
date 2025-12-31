/**
 * @file useImSocket.js
 * @description Vue Composable - IM WebSocket 连接的组合式函数
 * @author IM System
 * @version 1.0.0
 */

import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { getNativeSocket, ConnectionState, MessageType } from './nativeSocket'

/**
 * IM WebSocket 组合式函数
 * @param {Object} options - 配置选项
 * @param {boolean} options.autoConnect - 是否自动连接，默认 true
 * @param {Function} options.onMessage - 消息回调
 * @param {Function} options.onConnect - 连接成功回调
 * @param {Function} options.onDisconnect - 断开连接回调
 * @param {Function} options.onError - 错误回调
 * @returns {Object} WebSocket 相关的状态和方法
 */
export function useImSocket(options = {}) {
  const {
    autoConnect = true,
    onMessage = () => {},
    onConnect = () => {},
    onDisconnect = () => {},
    onError = () => {},
  } = options

  // ==================== 响应式状态 ====================

  /** 连接状态 */
  const connectionState = ref(ConnectionState.DISCONNECTED)

  /** 是否已连接 */
  const isConnected = computed(() => connectionState.value === ConnectionState.CONNECTED)

  /** 是否正在连接 */
  const isConnecting = computed(() => connectionState.value === ConnectionState.CONNECTING)

  /** 是否正在重连 */
  const isReconnecting = computed(() => connectionState.value === ConnectionState.RECONNECTING)

  /** 是否已认证 */
  const isAuthenticated = ref(false)

  /** 重连次数 */
  const reconnectAttempts = ref(0)

  /** 网络延迟（毫秒） */
  const latency = ref(0)

  /** 最后一次心跳时间 */
  const lastHeartbeat = ref(null)

  /** 排队消息数量 */
  const queuedMessages = ref(0)

  /** 错误信息 */
  const error = ref(null)

  /** 正在输入的用户列表 */
  const typingUsers = reactive(new Map())

  /** Socket 实例 */
  let socket = null

  /** 事件取消订阅函数列表 */
  const unsubscribers = []

  // ==================== 方法定义 ====================

  /**
   * 初始化 Socket 连接
   */
  const initSocket = () => {
    socket = getNativeSocket({
      debug: import.meta.env.DEV,
    })

    // 订阅连接状态变化
    unsubscribers.push(
      socket.on('stateChange', ({ newState }) => {
        connectionState.value = newState
      })
    )

    // 订阅连接成功
    unsubscribers.push(
      socket.on('connect', (data) => {
        error.value = null
        onConnect(data)
      })
    )

    // 订阅断开连接
    unsubscribers.push(
      socket.on('disconnect', (data) => {
        onDisconnect(data)
      })
    )

    // 订阅重连中
    unsubscribers.push(
      socket.on('reconnecting', ({ attempt }) => {
        reconnectAttempts.value = attempt
      })
    )

    // 订阅重连失败
    unsubscribers.push(
      socket.on('reconnectFailed', () => {
        error.value = '连接失败，请刷新页面重试'
      })
    )

    // 订阅认证成功
    unsubscribers.push(
      socket.on('authenticated', () => {
        isAuthenticated.value = true
      })
    )

    // 订阅认证失败
    unsubscribers.push(
      socket.on('authFailed', () => {
        isAuthenticated.value = false
        error.value = '认证失败，请重新登录'
      })
    )

    // 订阅心跳
    unsubscribers.push(
      socket.on('heartbeat', (data) => {
        latency.value = data.latency
        lastHeartbeat.value = new Date()
      })
    )

    // 订阅心跳超时
    unsubscribers.push(
      socket.on('heartbeatTimeout', () => {
        error.value = '网络连接不稳定'
      })
    )

    // 订阅错误
    unsubscribers.push(
      socket.on('error', (data) => {
        error.value = data.error?.message || '连接错误'
        onError(data)
      })
    )

    // 订阅消息
    unsubscribers.push(
      socket.on('message', (message) => {
        onMessage(message)
      })
    )

    // 订阅正在输入
    unsubscribers.push(
      socket.on(MessageType.TYPING, ({ sessionId, userId, userName }) => {
        const key = `${sessionId}_${userId}`
        typingUsers.set(key, {
          sessionId,
          userId,
          userName,
          timestamp: Date.now(),
        })

        // 5秒后自动清除
        setTimeout(() => {
          typingUsers.delete(key)
        }, 5000)
      })
    )

    // 订阅停止输入
    unsubscribers.push(
      socket.on(MessageType.STOP_TYPING, ({ sessionId, userId }) => {
        const key = `${sessionId}_${userId}`
        typingUsers.delete(key)
      })
    )
  }

  /**
   * 连接 WebSocket
   * @returns {Promise<void>}
   */
  const connect = async () => {
    if (!socket) {
      initSocket()
    }
    return socket.connect()
  }

  /**
   * 断开连接
   */
  const disconnect = () => {
    socket?.disconnect()
  }

  /**
   * 发送消息
   * @param {string} type - 消息类型
   * @param {*} data - 消息数据
   * @param {Object} options - 发送选项
   * @returns {Promise<Object>|boolean}
   */
  const send = (type, data, options = {}) => {
    if (!socket) {
      console.warn('Socket 未初始化')
      return options.waitResponse ? Promise.reject(new Error('未连接')) : false
    }
    return socket.send(type, data, options)
  }

  /**
   * 发送聊天消息
   * @param {string} sessionId - 会话 ID
   * @param {Object} messageData - 消息数据
   * @returns {Promise<Object>}
   */
  const sendChatMessage = (sessionId, messageData) => {
    return socket?.sendChatMessage(sessionId, messageData)
  }

  /**
   * 发送已读回执
   * @param {string} sessionId - 会话 ID
   * @param {string} messageId - 消息 ID
   */
  const sendReadReceipt = (sessionId, messageId) => {
    socket?.sendReadReceipt(sessionId, messageId)
  }

  /**
   * 发送正在输入状态
   * @param {string} sessionId - 会话 ID
   */
  const sendTyping = (sessionId) => {
    socket?.sendTyping(sessionId)
  }

  /**
   * 发送停止输入状态
   * @param {string} sessionId - 会话 ID
   */
  const sendStopTyping = (sessionId) => {
    socket?.sendStopTyping(sessionId)
  }

  /**
   * 获取指定会话正在输入的用户列表
   * @param {string} sessionId - 会话 ID
   * @returns {Array} 正在输入的用户列表
   */
  const getTypingUsers = (sessionId) => {
    const users = []
    for (const [key, user] of typingUsers) {
      if (user.sessionId === sessionId) {
        users.push(user)
      }
    }
    return users
  }

  /**
   * 注册消息处理器
   * @param {string} type - 消息类型
   * @param {Function} handler - 处理函数
   * @returns {Function} 取消注册函数
   */
  const onMessageType = (type, handler) => {
    if (!socket) {
      initSocket()
    }
    return socket.on(type, handler)
  }

  /**
   * 获取连接状态
   * @returns {Object}
   */
  const getStatus = () => {
    return socket?.getStatus() || {
      state: ConnectionState.DISCONNECTED,
      isConnected: false,
      isAuthenticated: false,
      userId: null,
      reconnectAttempts: 0,
      queuedMessages: 0,
      pendingCallbacks: 0,
    }
  }

  /**
   * 清除错误
   */
  const clearError = () => {
    error.value = null
  }

  // ==================== 生命周期 ====================

  onMounted(() => {
    if (autoConnect) {
      connect()
    }
  })

  onUnmounted(() => {
    // 取消所有事件订阅
    unsubscribers.forEach(unsub => unsub())
    unsubscribers.length = 0
  })

  // ==================== 返回值 ====================

  return {
    // 状态
    connectionState,
    isConnected,
    isConnecting,
    isReconnecting,
    isAuthenticated,
    reconnectAttempts,
    latency,
    lastHeartbeat,
    queuedMessages,
    error,
    typingUsers,

    // 方法
    connect,
    disconnect,
    send,
    sendChatMessage,
    sendReadReceipt,
    sendTyping,
    sendStopTyping,
    getTypingUsers,
    onMessageType,
    getStatus,
    clearError,

    // 常量
    ConnectionState,
    MessageType,
  }
}

export default useImSocket
