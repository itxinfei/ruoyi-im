/**
 * 消息发送重试 Composable
 * @description 处理消息发送失败后的自动重试逻辑
 * 支持指数退避策略，避免服务端压力过大
 * @author ruoyi
 */
import { ref, computed, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { sendMessage as apiSendMessage } from '@/api/im/message'

// 重试配置
const RETRY_CONFIG = {
  // 最大重试次数
  MAX_RETRY: 3,
  // 基础重试延迟（毫秒）
  BASE_DELAY: 1000,
  // 最大重试延迟（毫秒）
  MAX_DELAY: 10000,
  // 重试状态保留时间（毫秒）
  RETRY_STATE_EXPIRE: 30 * 60 * 1000, // 30分钟
}

/**
 * 消息重试 Hook
 * @returns {Object} 重试相关方法和状态
 */
export function useMessageRetry() {
  const store = useStore()

  // 状态
  const retryingMessages = ref(new Map()) // 正在重试的消息
  const failedMessages = ref(new Map()) // 失败的消息（达到最大重试次数）

  /**
   * 计算重试延迟时间（指数退避）
   */
  const calculateDelay = (retryCount) => {
    const delay = RETRY_CONFIG.BASE_DELAY * Math.pow(2, retryCount)
    return Math.min(delay, RETRY_CONFIG.MAX_DELAY)
  }

  /**
   * 发送消息（带重试）
   */
  const sendMessageWithRetry = async (params, retryCount = 0) => {
    const { sessionId, type, content, replyTo, clientMsgId } = params
    const currentClientMsgId = clientMsgId || `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`

    try {
      // 调用 API 发送消息
      const response = await apiSendMessage({
        conversationId: sessionId,
        type,
        content: typeof content === 'string' ? content : JSON.stringify(content),
        replyToMessageId: replyTo,
        clientMsgId: currentClientMsgId,
      })

      if (response.code === 200 && response.data) {
        // 发送成功，清除重试状态
        clearRetryState(currentClientMsgId)
        return { success: true, messageId: response.data, clientMsgId: currentClientMsgId }
      } else {
        throw new Error(response.msg || '发送失败')
      }
    } catch (error) {
      console.error(`[useMessageRetry] 发送失败 (第${retryCount + 1}次):`, error)

      if (retryCount < RETRY_CONFIG.MAX_RETRY) {
        // 记录重试状态
        setRetryState(currentClientMsgId, {
          params,
          retryCount: retryCount + 1,
          lastError: error.message,
          lastAttempt: Date.now(),
        })

        // 延迟后重试
        const delay = calculateDelay(retryCount)
        console.log(`[useMessageRetry] 将在 ${delay}ms 后重试`)

        await new Promise(resolve => setTimeout(resolve, delay))
        return sendMessageWithRetry(params, retryCount + 1)
      } else {
        // 达到最大重试次数，记录为失败
        setFailedMessage(currentClientMsgId, {
          params,
          retryCount,
          lastError: error.message,
          lastAttempt: Date.now(),
        })
        clearRetryState(currentClientMsgId)

        return {
          success: false,
          error: error.message,
          clientMsgId: currentClientMsgId,
          reachedMaxRetry: true,
        }
      }
    }
  }

  /**
   * 手动重试消息
   */
  const retryMessage = async (message) => {
    const params = {
      sessionId: message.sessionId || message.conversationId,
      type: message.type || 'text',
      content: message.content,
      replyTo: message.replyToMessageId || message.replyTo,
      clientMsgId: message.clientMsgId,
    }

    return sendMessageWithRetry(params, 0)
  }

  /**
   * 批量重试失败消息
   */
  const retryAllFailed = async () => {
    const failed = Array.from(failedMessages.value.values())
    const results = []

    for (const item of failed) {
      const result = await retryMessage(item.params)
      results.push(result)
      if (result.success) {
        failedMessages.value.delete(item.params.clientMsgId)
      }
    }

    return results
  }

  /**
   * 设置重试状态
   */
  const setRetryState = (clientMsgId, state) => {
    retryingMessages.value.set(clientMsgId, {
      ...state,
      clientMsgId,
    })

    // 同时更新 Vuex store 中的消息状态
    updateMessageStatus(clientMsgId, 'retrying')
  }

  /**
   * 清除重试状态
   */
  const clearRetryState = (clientMsgId) => {
    retryingMessages.value.delete(clientMsgId)
  }

  /**
   * 设置失败消息
   */
  const setFailedMessage = (clientMsgId, state) => {
    failedMessages.value.set(clientMsgId, {
      ...state,
      clientMsgId,
    })

    // 更新 Vuex store 中的消息状态
    updateMessageStatus(clientMsgId, 'failed')
  }

  /**
   * 更新消息状态
   */
  const updateMessageStatus = (clientMsgId, status) => {
    // 查找并更新消息列表中的状态
    const messageList = store.state.im.messageList
    for (const sessionId in messageList) {
      const message = messageList[sessionId].find(
        m => m.clientMsgId === clientMsgId || m.id === clientMsgId
      )
      if (message) {
        store.commit('im/UPDATE_MESSAGE', {
          sessionId,
          messageId: message.id,
          updates: { status },
        })
        break
      }
    }
  }

  /**
   * 清除过期状态
   */
  const clearExpiredStates = () => {
    const now = Date.now()

    // 清理过期的重试状态
    for (const [clientMsgId, state] of retryingMessages.value.entries()) {
      if (now - state.lastAttempt > RETRY_CONFIG.RETRY_STATE_EXPIRE) {
        retryingMessages.value.delete(clientMsgId)
      }
    }

    // 清理过期的失败记录
    for (const [clientMsgId, state] of failedMessages.value.entries()) {
      if (now - state.lastAttempt > RETRY_CONFIG.RETRY_STATE_EXPIRE) {
        failedMessages.value.delete(clientMsgId)
      }
    }
  }

  /**
   * 获取消息重试状态
   */
  const getMessageRetryState = (clientMsgId) => {
    return retryingMessages.value.get(clientMsgId)
  }

  /**
   * 获取消息失败信息
   */
  const getMessageFailureInfo = (clientMsgId) => {
    return failedMessages.value.get(clientMsgId)
  }

  /**
   * 是否正在重试
   */
  const isRetrying = (clientMsgId) => {
    return retryingMessages.value.has(clientMsgId)
  }

  /**
   * 是否失败
   */
  const isFailed = (clientMsgId) => {
    return failedMessages.value.has(clientMsgId)
  }

  // 定期清理过期状态（使用定时器ID）
  let cleanupTimer = null

  // 启动清理定时器
  const startCleanupTimer = () => {
    if (typeof window !== 'undefined' && !cleanupTimer) {
      cleanupTimer = setInterval(clearExpiredStates, 5 * 60 * 1000) // 每5分钟清理一次
    }
  }

  // 停止清理定时器
  const stopCleanupTimer = () => {
    if (cleanupTimer) {
      clearInterval(cleanupTimer)
      cleanupTimer = null
    }
  }

  // 组件挂载时启动定时器
  startCleanupTimer()

  // 组件卸载时清理定时器
  onUnmounted(() => {
    stopCleanupTimer()
  })

  return {
    // 状态
    retryingMessages: computed(() => retryingMessages.value),
    failedMessages: computed(() => failedMessages.value),

    // 方法
    sendMessageWithRetry,
    retryMessage,
    retryAllFailed,
    clearRetryState,
    setFailedMessage,
    clearExpiredStates,
    getMessageRetryState,
    getMessageFailureInfo,
    isRetrying,
    isFailed,

    // 清理方法
    stopCleanupTimer,

    // 配置
    config: RETRY_CONFIG,
  }
}

export default useMessageRetry
