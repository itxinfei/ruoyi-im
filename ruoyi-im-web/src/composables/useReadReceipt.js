/**
 * 消息已读回执 Composable
 * @description 处理消息自动已读标记、已读状态查询、已读回执推送
 * @author ruoyi
 */
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useStore } from 'vuex'
import { markMessageAsRead, markBatchMessageAsRead, markConversationAsRead } from '@/api/im/message'

// 已读回执配置
const READ_RECEIPT_CONFIG = {
  // 自动已读延迟（毫秒）
  AUTO_READ_DELAY: 1000,
  // 是否启用自动已读
  ENABLE_AUTO_READ: true,
  // 批量已读节流时间（毫秒）
  BATCH_READ_THROTTLE: 2000,
}

/**
 * 消息已读回执 Hook
 * @param {Ref} scrollContainer - 滚动容器 ref
 * @param {Ref<HTMLElement>} messageListRef - 消息列表 ref
 * @returns {Object} 已读回执相关方法和状态
 */
export function useReadReceipt(scrollContainer = null, messageListRef = null) {
  const store = useStore()

  // 状态
  const unreadMessageIds = ref(new Set())
  const pendingReadMessageIds = ref(new Set())
  const lastReadTime = ref(0)
  let readTimer = null
  let batchReadTimer = null
  let intersectionObserver = null

  /**
   * 获取当前用户ID
   */
  const getCurrentUserId = () => {
    return store.getters['im/currentUser']?.id || store.state.user?.userInfo?.userId
  }

  /**
   * 获取当前会话ID
   */
  const getCurrentConversationId = () => {
    return store.getters['im/currentSession']?.id || store.getters['im/currentSession']?.sessionId
  }

  /**
   * 标记单条消息为已读
   * @param {number} messageId - 消息ID
   * @param {number} conversationId - 会话ID
   */
  const markAsRead = async (messageId, conversationId) => {
    const userId = getCurrentUserId()
    if (!userId) {
      console.warn('[useReadReceipt] 用户未登录，无法标记已读')
      return
    }

    // 不能标记自己发送的消息为已读
    const message = findMessage(messageId, conversationId)
    if (message && message.senderId === userId) {
      return
    }

    try {
      await markMessageAsRead({
        messageId,
        conversationId: conversationId || getCurrentConversationId(),
      })

      // 更新本地状态
      unreadMessageIds.value.delete(messageId)
      pendingReadMessageIds.value.delete(messageId)

      console.debug('[useReadReceipt] 标记消息已读:', messageId)
    } catch (error) {
      console.error('[useReadReceipt] 标记消息已读失败:', error)
    }
  }

  /**
   * 批量标记消息为已读
   * @param {number[]} messageIds - 消息ID数组
   * @param {number} conversationId - 会话ID
   */
  const markBatchAsRead = async (messageIds, conversationId) => {
    if (!messageIds || messageIds.length === 0) return

    const userId = getCurrentUserId()
    if (!userId) {
      console.warn('[useReadReceipt] 用户未登录，无法标记已读')
      return
    }

    try {
      await markBatchMessageAsRead({
        conversationId: conversationId || getCurrentConversationId(),
        messageIds,
      })

      // 更新本地状态
      messageIds.forEach(id => {
        unreadMessageIds.value.delete(id)
        pendingReadMessageIds.value.delete(id)
      })

      console.debug('[useReadReceipt] 批量标记消息已读:', messageIds.length, '条')
    } catch (error) {
      console.error('[useReadReceipt] 批量标记消息已读失败:', error)
    }
  }

  /**
   * 标记会话所有消息为已读
   * @param {number} conversationId - 会话ID
   * @param {number} upToMessageId - 标记到此消息ID为止
   */
  const markConversationAsReadFn = async (conversationId, upToMessageId) => {
    const userId = getCurrentUserId()
    if (!userId) {
      console.warn('[useReadReceipt] 用户未登录，无法标记已读')
      return
    }

    try {
      await markConversationAsRead({
        conversationId,
        upToMessageId,
      })

      console.debug('[useReadReceipt] 标记会话已读:', conversationId)
    } catch (error) {
      console.error('[useReadReceipt] 标记会话已读失败:', error)
    }
  }

  /**
   * 查找消息
   */
  const findMessage = (messageId, conversationId) => {
    const sessionId = conversationId || getCurrentConversationId()
    const messageList = store.state.im.messageList[sessionId]
    if (!messageList) return null
    return messageList.find(m => m.id === messageId || m.messageId === messageId)
  }

  /**
   * 添加未读消息ID
   */
  const addUnreadMessage = (messageId) => {
    unreadMessageIds.value.add(messageId)
  }

  /**
   * 延迟标记消息为已读
   */
  const scheduleMarkAsRead = (messageId, conversationId, delay = READ_RECEIPT_CONFIG.AUTO_READ_DELAY) => {
    if (!READ_RECEIPT_CONFIG.ENABLE_AUTO_READ) return

    // 清除之前的定时器
    if (readTimer) {
      clearTimeout(readTimer)
    }

    // 添加到待读列表
    pendingReadMessageIds.value.add(messageId)

    // 延迟标记已读
    readTimer = setTimeout(() => {
      if (pendingReadMessageIds.value.size > 0) {
        const ids = Array.from(pendingReadMessageIds.value)
        if (ids.length === 1) {
          markAsRead(ids[0], conversationId)
        } else {
          markBatchAsRead(ids, conversationId)
        }
        pendingReadMessageIds.value.clear()
      }
    }, delay)
  }

  /**
   * 初始化 IntersectionObserver 监听消息可见性
   */
  const initIntersectionObserver = () => {
    if (!messageListRef?.value || typeof IntersectionObserver === 'undefined') {
      return
    }

    // 清理旧的 observer
    if (intersectionObserver) {
      intersectionObserver.disconnect()
    }

    intersectionObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const messageId = entry.target.dataset.messageId
            const conversationId = entry.target.dataset.conversationId || getCurrentConversationId()

            if (messageId && conversationId) {
              scheduleMarkAsRead(Number(messageId), Number(conversationId))
            }

            // 已读后停止监听该消息
            intersectionObserver.unobserve(entry.target)
          }
        })
      },
      {
        root: scrollContainer?.value || null,
        rootMargin: '0px',
        threshold: 0.5, // 消息显示50%时触发
      }
    )

    // 监听所有未读消息
    observeUnreadMessages()

    return intersectionObserver
  }

  /**
   * 监听未读消息
   */
  const observeUnreadMessages = () => {
    if (!intersectionObserver || !messageListRef?.value) return

    const conversationId = getCurrentConversationId()
    const messageList = store.state.im.messageList[conversationId]
    if (!messageList) return

    const userId = getCurrentUserId()

    messageList.forEach(message => {
      // 只监听他人发送的未读消息
      if (message.senderId !== userId && !message.readBy?.includes(userId)) {
        const messageElement = messageListRef.value.querySelector(`[data-message-id="${message.id}"]`)
        if (messageElement) {
          intersectionObserver.observe(messageElement)
        }
      }
    })
  }

  /**
   * 标记当前会话所有未读消息为已读
   */
  const markAllAsRead = async () => {
    const conversationId = getCurrentConversationId()
    if (!conversationId) return

    const messageList = store.state.im.messageList[conversationId]
    if (!messageList) return

    const userId = getCurrentUserId()
    const unreadMessages = messageList.filter(
      m => m.senderId !== userId && !m.readBy?.includes(userId)
    )

    if (unreadMessages.length > 0) {
      const messageIds = unreadMessages.map(m => m.id)
      const lastMessageId = messageIds[messageIds.length - 1]
      await markConversationAsReadFn(conversationId, lastMessageId)
    }
  }

  /**
   * 清理定时器和观察者
   */
  const cleanup = () => {
    if (readTimer) {
      clearTimeout(readTimer)
      readTimer = null
    }
    if (batchReadTimer) {
      clearTimeout(batchReadTimer)
      batchReadTimer = null
    }
    if (intersectionObserver) {
      intersectionObserver.disconnect()
      intersectionObserver = null
    }
  }

  // 生命周期
  onMounted(() => {
    if (READ_RECEIPT_CONFIG.ENABLE_AUTO_READ) {
      // 延迟初始化 observer，确保 DOM 已渲染
      setTimeout(() => {
        initIntersectionObserver()
      }, 500)
    }
  })

  onBeforeUnmount(() => {
    cleanup()
  })

  // 监听会话切换
  watch(
    () => getCurrentConversationId(),
    (newConversationId, oldConversationId) => {
      if (newConversationId && newConversationId !== oldConversationId) {
        // 切换会话时，先标记旧会话所有消息为已读
        if (oldConversationId) {
          markAllAsRead()
        }
        // 重新初始化 observer
        setTimeout(() => {
          initIntersectionObserver()
        }, 300)
      }
    }
  )

  return {
    // 状态
    unreadMessageIds,
    pendingReadMessageIds,
    lastReadTime,

    // 方法
    markAsRead,
    markBatchAsRead,
    markConversationAsRead: markConversationAsReadFn,
    markAllAsRead,
    addUnreadMessage,
    scheduleMarkAsRead,
    initIntersectionObserver,
    observeUnreadMessages,
    cleanup,

    // 配置
    config: READ_RECEIPT_CONFIG,
  }
}

export default useReadReceipt
