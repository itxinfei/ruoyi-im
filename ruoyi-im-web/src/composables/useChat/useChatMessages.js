/**
 * ChatPanel - 消息管理 Composable
 *
 * 职责：
 * - 消息加载（历史消息、分页）
 * - 消息发送
 * - 消息重试
 * - 消息状态管理
 */

import { ref, computed, watch, nextTick } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { getMessages, sendMessage, retryMessage, deleteMessage } from '@/api/im/message'
import { useIndexedDB } from '../useIndexedDB.js'
import { MAX_RETRIES } from '@/constants/retry.js'

export function useChatMessages(sessionId, currentUser) {
  const store = useStore()

  // 状态
  const messages = ref([])
  const loading = ref(false)
  const sending = ref(false)
  const noMore = ref(false)
  const currentPage = ref(1)
  const pageSize = 50

  // 当前会话ID
  const currentSessionId = ref(sessionId)

  // 获取本地缓存
  const { getConversationMessages, cacheMessages, cacheSingleMessage } = useIndexedDB()

  /**
   * 加载历史消息
   */
  const loadHistory = async (sessionId = currentSessionId.value) => {
    if (loading.value || noMore.value) return

    loading.value = true

    try {
      // 1. 先尝试从本地缓存加载
      const localResult = await getConversationMessages(sessionId, pageSize)

      if (localResult.fromLocal && localResult.messages.length > 0) {
        messages.value = localResult.messages
        loading.value = false
        console.log(`[useChatMessages] 从本地加载 ${localResult.messages.length} 条消息`)

        // 后台从服务器获取最新消息
        syncMessagesFromServer(sessionId)
        return
      }

      // 2. 从服务器加载
      const response = await getMessages(sessionId, {
        page: currentPage.value,
        size: pageSize
      })

      if (response.code === 200) {
        const newMessages = response.data.records || []

        if (newMessages.length === 0) {
          noMore.value = true
        } else {
          // 按时间倒序排列（最新在前）
          messages.value = [...newMessages.reverse(), ...messages.value]
          currentPage.value++

          // 缓存到本地
          await cacheMessages(newMessages)
        }
      }
    } catch (error) {
      console.error('加载消息失败:', error)
      ElMessage.error('加载消息失败')
    } finally {
      loading.value = false
    }
  }

  /**
   * 从服务器同步最新消息
   */
  const syncMessagesFromServer = async (sessionId) => {
    try {
      const response = await getMessages(sessionId, {
        page: 1,
        size: 50
      })

      if (response.code === 200) {
        const serverMessages = response.data.records || []
        const currentIds = new Set(messages.value.map(m => m.id))

        // 只添加新消息
        const newMessages = serverMessages
          .filter(m => !currentIds.has(m.id))
          .reverse()

        if (newMessages.length > 0) {
          messages.value = [...newMessages, ...messages.value]
          // 缓存新消息
          await cacheMessages(newMessages)
          console.log(`[useChatMessages] 同步了 ${newMessages.length} 条新消息`)
        }
      }
    } catch (error) {
      console.error('同步消息失败:', error)
    }
  }

  /**
   * 发送消息
   */
  const send = async (content, type = 'TEXT', extra = {}) => {
    if (!content || sending.value) return

    sending.value = true

    try {
      // 构造消息对象
      const messageData = {
        conversationId: currentSessionId.value,
        type,
        content,
        ...extra
      }

      const response = await sendMessage(messageData)

      if (response.code === 200) {
        const newMessage = response.data

        // 添加到消息列表
        messages.value.push(newMessage)

        // 缓存新消息
        await cacheSingleMessage(newMessage)

        // 更新会话的最后消息
        updateConversationLastMessage(currentSessionId.value, newMessage)

        return newMessage
      }
    } catch (error) {
      console.error('发送消息失败:', error)
      ElMessage.error('发送失败，请重试')
      throw error
    } finally {
      sending.value = false
    }
  }

  // 消息重试最大次数限制（从 @/constants/retry.js 统一管理）

  /**
   * 重试失败的消息
   *
   * 重试策略：
   * - MAX_RETRIES = 3 表示总共最多尝试 3 次（首次发送 + 最多 2 次重试）
   * - 重试前立即更新计数，防止竞态条件
   * - 失败时回滚计数，允许后续重试
   * - retryCount 持久化到 IndexedDB，刷新页面后保留
   */
  const retry = async (message) => {
    // 预递增：在 API 调用前增加计数，防止失败时计数不增加
    const currentRetryCount = message.retryCount || 0
    const newRetryCount = currentRetryCount + 1

    // 检查是否超过最大重试次数
    if (currentRetryCount >= MAX_RETRIES) {
      ElMessage.warning('已达到最大重试次数')
      return
    }

    // 立即更新 UI，防止并发点击
    const index = messages.value.findIndex(m => m.clientMsgId === message.clientMsgId)
    if (index !== -1) {
      messages.value[index] = {
        ...messages.value[index],
        retryCount: newRetryCount
      }
    }

    try {
      const response = await retryMessage(message.clientMsgId)

      if (response.code === 200) {
        // 成功：更新消息状态（保持已递增的计数）
        if (index !== -1) {
          messages.value[index] = {
            ...messages.value[index],
            ...response.data,
            retryCount: newRetryCount
          }
          await cacheSingleMessage(messages.value[index])
        }

        ElMessage.success('发送成功')
      }
    } catch (error) {
      // 失败：回滚计数，允许后续重试
      if (index !== -1) {
        messages.value[index].retryCount = currentRetryCount
        await cacheSingleMessage(messages.value[index])
      }
      console.error('重试消息失败:', error)
      ElMessage.error('重试失败')
      throw error
    }
  }

  /**
   * 删除消息
   */
  const removeMessage = async (messageId) => {
    try {
      await deleteMessage(messageId)

      const index = messages.value.findIndex(m => m.id === messageId)
      if (index !== -1) {
        messages.value.splice(index, 1)
      }

      ElMessage.success('删除成功')
    } catch (error) {
      console.error('删除消息失败:', error)
      ElMessage.error('删除失败')
    }
  }

  /**
   * 更新会话的最后消息
   */
  const updateConversationLastMessage = (conversationId, message) => {
    store.dispatch('im/session/updateLastMessage', {
      conversationId,
      message
    })
  }

  /**
   * 标记消息为已读
   */
  const markAsRead = async (messageId) => {
    try {
      await store.dispatch('im/message/markMessageAsRead', messageId)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  /**
   * 加载更多消息
   */
  const loadMore = async () => {
    if (loading.value || noMore.value) return
    currentPage.value++
    await loadHistory()
  }

  // 监听会话ID变化
  watch(() => currentSessionId.value, (newId, oldId) => {
    if (newId !== oldId) {
      messages.value = []
      currentPage.value = 1
      noMore.value = false
      if (newId) {
        loadHistory(newId)
      }
    }
  })

  return {
    // 状态
    messages,
    loading,
    sending,
    noMore,
    currentPage,

    // 方法
    loadHistory,
    send,
    retry,
    removeMessage,
    markAsRead,
    loadMore,
    syncMessagesFromServer
  }
}
