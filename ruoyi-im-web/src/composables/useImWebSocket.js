/**
 * WebSocket 组合式函数
 * 用于在 Vue 组件中使用 WebSocket
 */
import { onUnmounted, ref } from 'vue'
import imWebSocket, { WS_STATUS } from '@/utils/websocket/imWebSocket'

/**
 * 使用 WebSocket
 * @returns {Object}
 */
export function useImWebSocket() {
  const isConnected = ref(false)
  const connectionState = ref(WS_STATUS.CLOSED)

  // 连接 WebSocket
  const connect = (token) => {
    if (!token) {
      token = localStorage.getItem('im_token')
    }

    if (!token) {
      console.error('[useImWebSocket] 未找到认证令牌')
      return
    }

    // 监听连接状态
    imWebSocket.on('connected', () => {
      isConnected.value = true
      connectionState.value = WS_STATUS.OPEN
    })

    imWebSocket.on('disconnected', () => {
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

  // 发送消息
  const sendMessage = (message) => {
    return imWebSocket.send(message)
  }

  // 监听消息
  const onMessage = (callback) => {
    imWebSocket.on('message', callback)
  }

  // 监听用户上线
  const onOnline = (callback) => {
    imWebSocket.on('online', callback)
  }

  // 监听用户下线
  const onOffline = (callback) => {
    imWebSocket.on('offline', callback)
  }

  // 监听正在输入
  const onTyping = (callback) => {
    imWebSocket.on('typing', callback)
  }

  // 监听已读回执
  const onRead = (callback) => {
    imWebSocket.on('read', callback)
  }

  // 监听通话事件
  const onCall = (callback) => {
    imWebSocket.on('call', callback)
  }

  // 组件卸载时清理监听器
  onUnmounted(() => {
    // 这里可以选择是否自动断开连接
    // disconnect()
  })

  return {
    // 状态
    isConnected,
    connectionState,

    // 方法
    connect,
    disconnect,
    sendMessage,

    // 事件监听
    onMessage,
    onOnline,
    onOffline,
    onTyping,
    onRead,
    onCall,

    // WebSocket 实例
    ws: imWebSocket
  }
}
