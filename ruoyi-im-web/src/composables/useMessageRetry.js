/**
 * 消息重试管理 Composable（简化版）
 *
 * 仅保留 localStorage 持久化层：
 * - 重试计数持久化
 * - 检查是否可重试
 * - 记录/清除重试次数
 *
 * 失败消息的管理已移至 Vuex store (im/message)
 * 重试逻辑已移至 useChatSend.js
 */
import { MAX_RETRIES } from '@/constants/retry.js'

const RETRY_COUNTS_KEY = 'im_retry_counts'

// 单例缓存（避免每个组件实例各持一份）
let retryCounts = null

/**
 * 从 localStorage 加载重试计数
 */
function loadRetryCounts() {
  if (retryCounts !== null) { return }
  try {
    const stored = localStorage.getItem(RETRY_COUNTS_KEY)
    retryCounts = stored ? JSON.parse(stored) : {}
  } catch (error) {
    console.error('加载重试计数失败:', error)
    retryCounts = {}
  }
}

/**
 * 保存重试计数到 localStorage
 */
function saveRetryCounts() {
  try {
    localStorage.setItem(RETRY_COUNTS_KEY, JSON.stringify(retryCounts))
  } catch (error) {
    console.error('保存重试计数失败:', error)
  }
}

export function useMessageRetry() {
  /**
   * 初始化：加载重试计数
   */
  function init() {
    loadRetryCounts()
  }

  /**
   * 检查是否可以重试
   * @param {string} clientMsgId - 客户端消息ID
   * @returns {boolean}
   */
  function canRetry(clientMsgId) {
    loadRetryCounts()
    const count = retryCounts[clientMsgId] || 0
    return count < MAX_RETRIES
  }

  /**
   * 记录一次重试尝试
   * @param {string} clientMsgId - 客户端消息ID
   */
  function recordRetryAttempt(clientMsgId) {
    loadRetryCounts()
    retryCounts[clientMsgId] = (retryCounts[clientMsgId] || 0) + 1
    saveRetryCounts()
  }

  /**
   * 发送成功时清除重试记录
   * @param {string} clientMsgId - 客户端消息ID
   */
  function removeRetryRecord(clientMsgId) {
    loadRetryCounts()
    delete retryCounts[clientMsgId]
    saveRetryCounts()
  }

  return {
    init,
    canRetry,
    recordRetryAttempt,
    removeRetryRecord
  }
}
