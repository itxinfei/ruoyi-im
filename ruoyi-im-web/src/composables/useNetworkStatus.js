/**
* 网络状态监听 Composable
* 监听浏览器在线/离线状态，处理断网重连场景
*/
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'

/**
 * 网络状态管理
*/
export function useNetworkStatus() {
  const store = useStore()

  // 网络状态
  const isOnline = ref(navigator.onLine)
  const wasOffline = ref(false) // 是否曾经离线
  const reconnectAttempts = ref(0)
  const lastOnlineTime = ref(Date.now())

  // 网络质量（基于 navigator.connection 或手动判断）
  const connectionType = ref('unknown')
  const isSlowConnection = ref(false)

  /**
   * 检查网络连接类型
   */
  const checkConnectionType = () => {
    if (navigator.connection) {
      const conn = navigator.connection
      connectionType.value = conn.effectiveType || 'unknown'
      isSlowConnection.value = conn.saveData ||
        (conn.effectiveType === 'slow-2g' || conn.effectiveType === '2g')
    }
  }

  /**
   * 处理网络上线事件
   */
  const handleOnline = () => {
    const wasOff = !isOnline.value
    isOnline.value = true
    wasOffline.value = wasOffline.value || wasOff
    lastOnlineTime.value = Date.now()

    console.log('[网络状态] 网络已恢复')

    // 通知 store 处理离线队列
    if (wasOff) {
      // 重置重连次数
      reconnectAttempts.value = 0

      // 触发离线消息处理
      setTimeout(() => {
        store.dispatch('im/message/processOfflineQueue')
        store.dispatch('im/message/retryFailedMessages')
      }, 1000) // 延迟1秒等待 WebSocket 完全连接
    }
  }

  /**
   * 处理网络下线事件
   */
  const handleOffline = () => {
    isOnline.value = false
    console.log('[网络状态] 网络已断开')

    // 通知 store 进入离线模式
    store.dispatch('im/message/setOfflineStatus', true)
  }

  /**
   * 检查网络状态
   */
  const checkStatus = () => {
    isOnline.value = navigator.onLine
    checkConnectionType()
    return isOnline.value
  }

  // 监听网络状态变化
  onMounted(() => {
    checkConnectionType()
    checkStatus()

    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)

    // 监听连接类型变化（如果浏览器支持）
    if (navigator.connection) {
      navigator.connection.addEventListener('change', checkConnectionType)
    }
  })

  onUnmounted(() => {
    window.removeEventListener('online', handleOnline)
    window.removeEventListener('offline', handleOffline)

    if (navigator.connection) {
      navigator.connection.removeEventListener('change', checkConnectionType)
    }
  })

  /**
   * 获取网络状态描述
   */
  const networkStatusText = computed(() => {
    if (!isOnline.value) {
      return '网络已断开'
    }
    if (isSlowConnection.value) {
      return '网络较慢'
    }
    return '网络正常'
  })

  /**
   * 获取网络状态图标
   */
  const networkStatusIcon = computed(() => {
    if (!isOnline.value) {
      return 'wifi_off'
    }
    if (isSlowConnection.value) {
      return 'signal_cellular_alt_1_bar'
    }
    switch (connectionType.value) {
      case '4g':
        return 'signal_cellular_4_bar'
      case '3g':
        return 'signal_cellular_3_bar'
      case '2g':
        return 'signal_cellular_2_bar'
      default:
        return 'wifi'
    }
  })

  /**
   * 离线时长（秒）
   */
  const offlineDuration = computed(() => {
    if (isOnline.value) {
      return 0
    }
    return Math.floor((Date.now() - lastOnlineTime.value) / 1000)
  })

  return {
    // 状态
    isOnline,
    wasOffline,
    reconnectAttempts,
    connectionType,
    isSlowConnection,
    lastOnlineTime,

    // 计算属性
    networkStatusText,
    networkStatusIcon,
    offlineDuration,

    // 方法
    checkStatus,
    checkConnectionType
  }
}

/**
* WebSocket 状态监听（与网络状态结合）
*/
export function useWebSocketStatus() {
  const store = useStore()

  // WebSocket 连接状态
  const isConnected = computed(() => store.state.im?.wsConnected || false)
  const isConnecting = ref(false)
  const connectionError = ref(null)

  // 网络状态
  const { isOnline } = useNetworkStatus()

  /**
   * 综合连接状态
   * - 'connected': 已连接
   * - 'connecting': 连接中
   * - 'disconnected': 已断开
   * - 'offline': 网络离线
   * - 'error': 连接错误
   */
  const connectionStatus = computed(() => {
    if (!isOnline.value) {
      return 'offline'
    }
    if (isConnecting.value) {
      return 'connecting'
    }
    if (connectionError.value) {
      return 'error'
    }
    if (isConnected.value) {
      return 'connected'
    }
    return 'disconnected'
  })

  /**
   * 状态文本
   */
  const statusText = computed(() => {
    switch (connectionStatus.value) {
      case 'connected':
        return '已连接'
      case 'connecting':
        return '连接中...'
      case 'disconnected':
        return '连接已断开'
      case 'offline':
        return '网络离线'
      case 'error':
        return '连接错误'
      default:
        return '未知状态'
    }
  })

  return {
    isConnected,
    isConnecting,
    connectionError,
    connectionStatus,
    statusText,
    isOnline
  }
}
