/**
* 消息发送队列管理 Composable
* 处理消息发送、重试、队列管理等功能
*/
import { ref, computed } from 'vue'
import { useStore } from 'vuex'

/**
* 消息发送队列管理
*/
export function useMessageQueue() {
  const store = useStore()

  // 发送队列状态
  const isSending = ref(false)
  const sendingCount = computed(() => store.getters['im/message/sendingQueueSize'])
  const failedCount = computed(() => store.getters['im/message/failedRetryQueueSize'])

  /**
   * 发送消息（带重试机制）
   * @param {Object} payload - 消息内容
   * @param {number} maxRetries - 最大重试次数
   */
  const sendWithRetry = async (payload, maxRetries = 3) => {
    const { sessionId, type = 'TEXT', content, replyToMessageId } = payload

    try {
      isSending.value = true
      const result = await store.dispatch('im/message/sendMessage', {
        sessionId,
        type,
        content,
        replyToMessageId
      })
      return { success: true, data: result }
    } catch (error) {
      console.error('[消息队列] 发送失败:', error)

      // 如果是网络错误，加入重试队列
      if (isNetworkError(error)) {
        const tempId = payload.clientMsgId || `temp-${Date.now()}`
        store.commit('im/message/ADD_TO_FAILED_RETRY_QUEUE', {
          clientMsgId: tempId,
          sessionId,
          message: payload
        })
        return { success: false, error, retryable: true }
      }

      return { success: false, error, retryable: false }
    } finally {
      isSending.value = false
    }
  }

  /**
   * 重试失败的消息
   */
  const retryFailed = async () => {
    try {
      await store.dispatch('im/message/retryFailedMessages')
      return { success: true }
    } catch (error) {
      console.error('[消息队列] 重试失败:', error)
      return { success: false, error }
    }
  }

  /**
   * 清空失败队列
   */
  const clearFailedQueue = () => {
    store.commit('im/message/CLEAR_OFFLINE_QUEUE')
  }

  /**
   * 判断是否为网络错误
   */
  const isNetworkError = (error) => {
    return error?.code === 'NETWORK_ERROR' ||
      error?.code === 'ECONNABORTED' ||
      error?.message?.includes('网络') ||
      !navigator.onLine
  }

  /**
   * 队列状态信息
   */
  const queueInfo = computed(() => ({
    sending: sendingCount.value,
    failed: failedCount.value,
    total: sendingCount.value + failedCount.value
  }))

  return {
    // 状态
    isSending,
    sendingCount,
    failedCount,
    queueInfo,

    // 方法
    sendWithRetry,
    retryFailed,
    clearFailedQueue,
    isNetworkError
  }
}

/**
* 消息操作工具类
*/
export function useMessageOperations() {
  const store = useStore()

  /**
   * 撤回消息
   * @param {string} messageId - 消息ID
   */
  const recall = async (messageId) => {
    try {
      await store.dispatch('im/message/recallMessage', messageId)
      return { success: true }
    } catch (error) {
      console.error('[消息操作] 撤回失败:', error)
      return { success: false, error }
    }
  }

  /**
   * 删除消息
   * @param {string} messageId - 消息ID
   */
  const remove = async (messageId) => {
    try {
      await store.dispatch('im/message/deleteMessage', messageId)
      return { success: true }
    } catch (error) {
      console.error('[消息操作] 删除失败:', error)
      return { success: false, error }
    }
  }

  /**
   * 编辑消息
   * @param {string} messageId - 消息ID
   * @param {string} content - 新内容
   */
  const edit = async (messageId, content) => {
    try {
      await store.dispatch('im/message/editMessage', { messageId, content })
      return { success: true }
    } catch (error) {
      console.error('[消息操作] 编辑失败:', error)
      return { success: false, error }
    }
  }

  /**
   * 转发消息
   * @param {string} messageId - 消息ID
   * @param {string} targetConversationId - 目标会话ID
   */
  const forward = async (messageId, targetConversationId) => {
    try {
      const result = await store.dispatch('im/message/forwardMessage', {
        messageId,
        targetConversationId
      })
      return { success: true, data: result }
    } catch (error) {
      console.error('[消息操作] 转发失败:', error)
      return { success: false, error }
    }
  }

  /**
   * 批量操作（多选）
   * @param {Array} messageIds - 消息ID数组
   * @param {string} operation - 操作类型
   */
  const batchOperation = async (messageIds, operation) => {
    try {
      switch (operation) {
        case 'delete':
          await Promise.all(messageIds.map(id => store.dispatch('im/message/deleteMessage', id)))
          break
        case 'forward':
          // 转发需要目标会话
          return { success: true, needTarget: true }
        default:
          return { success: false, error: '未知操作' }
      }
      return { success: true }
    } catch (error) {
      console.error('[消息操作] 批量操作失败:', error)
      return { success: false, error }
    }
  }

  return {
    recall,
    remove,
    edit,
    forward,
    batchOperation
  }
}
