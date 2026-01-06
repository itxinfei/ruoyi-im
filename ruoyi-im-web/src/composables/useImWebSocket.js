/**
 * IM WebSocket 组合式函数
 * 提供在 Vue 组件中使用 WebSocket 的便捷方法
 */

import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import imWebSocket, { WS_STATUS, MSG_TYPE } from '@/utils/websocket/imWebSocket'

/**
 * 使用 IM WebSocket
 * @returns {Object} WebSocket 相关的状态和方法
 */
export function useImWebSocket() {
  const store = useStore()

  // 连接状态
  const wsStatus = ref(WS_STATUS.DISCONNECTED)
  const isConnected = computed(() => wsStatus.value === WS_STATUS.CONNECTED)

  /**
   * 连接 WebSocket
   */
  const connect = () => {
    const token = localStorage.getItem('Admin-Token') || localStorage.getItem('token')
    if (!token) {
      console.error('未找到认证 token')
      return false
    }

    // 添加状态监听
    imWebSocket.on('statusChange', handleStatusChange)

    // 添加消息监听
    imWebSocket.on('message', handleMessage)
    imWebSocket.on('notification', handleNotification)
    imWebSocket.on('system', handleSystem)
    imWebSocket.on('messageRead', handleMessageRead)
    imWebSocket.on('messageRecall', handleMessageRecall)

    // 连接
    imWebSocket.connect(token)

    return true
  }

  /**
   * 断开连接
   */
  const disconnect = () => {
    // 移除所有监听
    imWebSocket.off('statusChange', handleStatusChange)
    imWebSocket.off('message', handleMessage)
    imWebSocket.off('notification', handleNotification)
    imWebSocket.off('system', handleSystem)
    imWebSocket.off('messageRead', handleMessageRead)
    imWebSocket.off('messageRecall', handleMessageRecall)

    imWebSocket.close()
  }

  /**
   * 发送消息
   */
  const send = message => {
    return imWebSocket.send(message)
  }

  /**
   * 发送聊天消息
   */
  const sendMessage = message => {
    return imWebSocket.sendMessage(message)
  }

  /**
   * 处理状态变化
   */
  const handleStatusChange = status => {
    wsStatus.value = status
    store.dispatch('im/setWsConnected', status === WS_STATUS.CONNECTED)
  }

  /**
   * 处理接收到的消息
   */
  const handleMessage = message => {
    // 消息已由 WebSocket 处理器更新到 store
    // 这里可以添加额外的处理逻辑
  }

  /**
   * 处理通知消息
   */
  const handleNotification = message => {
    // 通知消息处理
  }

  /**
   * 处理系统消息
   */
  const handleSystem = message => {
    // 系统消息处理
  }

  /**
   * 处理消息已读回执
   */
  const handleMessageRead = message => {
    // 更新消息已读状态
    if (message.messageId && message.sessionId) {
      store.commit('im/UPDATE_MESSAGE', {
        sessionId: message.sessionId,
        messageId: message.messageId,
        updates: { read: true },
      })
    }
  }

  /**
   * 处理消息撤回
   */
  const handleMessageRecall = message => {
    // 处理消息撤回
  }

  // 组件卸载时自动断开（可选）
  onUnmounted(() => {
    // 如果需要在组件卸载时断开，可以取消注释
    // disconnect()
  })

  return {
    // 状态
    wsStatus,
    isConnected,

    // 方法
    connect,
    disconnect,
    send,
    sendMessage,

    // 常量
    WS_STATUS,
    MSG_TYPE,

    // 原始实例
    ws: imWebSocket,
  }
}

export default useImWebSocket
