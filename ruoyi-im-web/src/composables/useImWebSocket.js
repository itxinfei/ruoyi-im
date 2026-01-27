/**
 * WebSocket 组合式函数
 * 用于在 Vue 组件中使用 WebSocket
 */
import { onUnmounted, ref } from 'vue'
import { error } from '@/utils/logger'
import imWebSocket, { WS_STATUS } from '@/utils/websocket/imWebSocket'

/**
 * 使用 WebSocket
 * @returns {Object}
 */
export function useImWebSocket() {
  const isConnected = ref(false)
  const connectionState = ref(WS_STATUS.CLOSED)

  // 存储当前组件注册的事件监听器，用于清理
  const registeredHandlers = []

  // 注册监听器并记录
  const registerHandler = (event, callback) => {
    imWebSocket.on(event, callback)
    registeredHandlers.push({ event, callback })
  }

  // 连接 WebSocket
  const connect = (token) => {
    if (!token) {
      token = localStorage.getItem('im_token')
    }

    if (!token) {
      error('useImWebSocket', '未找到认证令牌')
      return
    }

    // 监听连接状态
    registerHandler('connected', () => {
      isConnected.value = true
      connectionState.value = WS_STATUS.OPEN
    })

    registerHandler('disconnected', () => {
      isConnected.value = false
      connectionState.value = WS_STATUS.CLOSED
    })

    // 开始连接
    imWebSocket.connect(token)
  }

  // 断开连接
  const disconnect = () => {
    imWebSocket.close()
    isConnected.value = false
    connectionState.value = WS_STATUS.CLOSED
  }

  // 完全销毁（登出时使用）
  const destroy = () => {
    imWebSocket.destroy()
    isConnected.value = false
    connectionState.value = WS_STATUS.CLOSED
    registeredHandlers.length = 0
  }

  // 发送消息
  const sendMessage = (message) => {
    return imWebSocket.send(message)
  }

  // 监听消息
  const onMessage = (callback) => {
    registerHandler('message', callback)
  }

  // 监听用户上线
  const onOnline = (callback) => {
    registerHandler('online', callback)
  }

  // 监听用户下线
  const onOffline = (callback) => {
    registerHandler('offline', callback)
  }

  // 监听正在输入
  const onTyping = (callback) => {
    registerHandler('typing', callback)
  }

  // 监听已读回执
  const onRead = (callback) => {
    registerHandler('read', callback)
  }

  // 监听消息状态更新
  const onMessageStatus = (callback) => {
    registerHandler('message_status', callback)
  }

  // 监听通话事件
  const onCall = (callback) => {
    registerHandler('call', callback)
  }

  // 清理当前组件的所有监听器
  const cleanup = () => {
    registeredHandlers.forEach(({ event, callback }) => {
      imWebSocket.off(event, callback)
    })
    registeredHandlers.length = 0
  }

  // 组件卸载时自动清理监听器
  onUnmounted(() => {
    cleanup()
  })

  return {
    // 状态
    isConnected,
    connectionState,

    // 方法
    connect,
    disconnect,
    destroy,
    sendMessage,
    cleanup,

    // 事件监听
    onMessage,
    onOnline,
    onOffline,
    onTyping,
    onRead,
    onMessageStatus,
    onCall,

    // WebSocket 实例
    ws: imWebSocket
  }
}
