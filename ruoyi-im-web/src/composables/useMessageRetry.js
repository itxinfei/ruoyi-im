/**
 * 消息重试管理 Composable
 * 管理失败消息的缓存、重试和清理
 */
import { ref, computed } from 'vue'
import { MAX_RETRIES } from '@/constants/retry.js'

// LocalStorage 键
const FAILED_MESSAGES_KEY = 'im_failed_messages'
const CACHE_TTL = 5 * 60 * 1000 // 5分钟缓存时间

/**
 * 失败消息缓存结构
 * {
 *   [tempId]: {
 *     tempId: string,
 *     sessionId: string,
 *     type: string,
 *     content: string,
 *     retryCount: number,
 *     timestamp: number
 *   }
 * }
 */

export function useMessageRetry() {
  const failedMessages = ref({})
  const cleanupTimer = ref(null)

  /**
   * 初始化：从 localStorage 加载失败消息
   */
  function init() {
    try {
      const stored = localStorage.getItem(FAILED_MESSAGES_KEY)
      if (stored) {
        const parsed = JSON.parse(stored)
        // 清理过期的消息
        const now = Date.now()
        const cleaned = {}

        for (const [tempId, msg] of Object.entries(parsed)) {
          if (now - msg.timestamp < CACHE_TTL) {
            cleaned[tempId] = msg
          }
        }

        failedMessages.value = cleaned
        saveFailedMessages()

        // 启动定时清理
        startCleanupTimer()
      }
    } catch (error) {
      console.error('加载失败消息缓存出错:', error)
    }
  }

  /**
   * 保存失败消息到 localStorage
   */
  function saveFailedMessages() {
    try {
      localStorage.setItem(FAILED_MESSAGES_KEY, JSON.stringify(failedMessages.value))
    } catch (error) {
      console.error('保存失败消息缓存出错:', error)
    }
  }

  /**
   * 记录失败消息
   * @param {Object} message - 失败的消息对象
   */
  function recordFailedMessage(message) {
    const tempId = message.id || message.tempId
    if (!tempId) {
      console.warn('消息缺少ID，无法记录失败消息')
      return
    }

    // 检查是否已存在
    const existing = failedMessages.value[tempId]
    const retryCount = existing ? existing.retryCount + 1 : 1

    // 检查重试次数限制
    if (retryCount > MAX_RETRIES) {
      console.warn('消息重试次数已达上限:', tempId)
      removeFailedMessage(tempId)
      return false
    }

    // 记录失败消息
    failedMessages.value[tempId] = {
      tempId,
      sessionId: message.sessionId || message.conversationId,
      type: message.type || 'TEXT',
      content: message.content,
      retryCount,
      timestamp: Date.now()
    }

    saveFailedMessages()
    return true
  }

  /**
   * 获取失败消息
   * @param {string} tempId - 临时消息ID
   */
  function getFailedMessage(tempId) {
    return failedMessages.value[tempId]
  }

  /**
   * 移除失败消息
   * @param {string} tempId - 临时消息ID
   */
  function removeFailedMessage(tempId) {
    delete failedMessages.value[tempId]
    saveFailedMessages()
  }

  /**
   * 清理过期的失败消息
   */
  function cleanupExpiredMessages() {
    const now = Date.now()
    let hasChanges = false

    for (const tempId in failedMessages.value) {
      if (now - failedMessages.value[tempId].timestamp >= CACHE_TTL) {
        delete failedMessages.value[tempId]
        hasChanges = true
      }
    }

    if (hasChanges) {
      saveFailedMessages()
    }
  }

  /**
   * 启动定时清理任务
   */
  function startCleanupTimer() {
    stopCleanupTimer()
    cleanupTimer.value = setInterval(() => {
      cleanupExpiredMessages()
    }, 60 * 1000) // 每分钟清理一次
  }

  /**
   * 停止定时清理任务
   */
  function stopCleanupTimer() {
    if (cleanupTimer.value) {
      clearInterval(cleanupTimer.value)
      cleanupTimer.value = null
    }
  }

  /**
   * 清空所有失败消息
   */
  function clearAll() {
    failedMessages.value = {}
    saveFailedMessages()
  }

  /**
   * 计算属性：失败消息数量
   */
  const failedCount = computed(() => {
    return Object.keys(failedMessages.value).length
  })

  /**
   * 计算属性：是否可以重试
   * @param {string} tempId - 临时消息ID
   */
  function canRetry(tempId) {
    const msg = failedMessages.value[tempId]
    return msg && msg.retryCount < MAX_RETRIES
  }

  return {
    // 状态
    failedMessages,
    failedCount,

    // 方法
    init,
    recordFailedMessage,
    getFailedMessage,
    removeFailedMessage,
    canRetry,
    cleanupExpiredMessages,
    clearAll,
    stopCleanupTimer
  }
}
