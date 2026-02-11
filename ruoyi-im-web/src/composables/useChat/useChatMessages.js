/**
 * ChatPanel - 消息管理 Composable（Store 薄包装）
 *
 * 职责：
 * - 消息列表（来自 Vuex store 的 computed）
 * - 历史消息加载（cursor 分页）
 * - 标记已读
 *
 * 消息发送、重试逻辑已移至 useChatSend.js
 */

import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

export function useChatMessages(sessionId, currentUser) {
  const store = useStore()

  // 当前会话ID（响应式跟踪传入的 sessionId）
  const currentSessionId = computed(() => sessionId?.value ?? sessionId)

  // 消息列表：直接来自 store getter（唯一权威源）
  const messages = computed(() => store.getters['im/message/messagesBySessionId'](currentSessionId.value) || [])

  // UI 状态
  const loading = ref(false)
  const noMore = ref(false)

  /**
   * 加载历史消息（首次进入会话时调用）
   */
  const loadHistory = async (targetSessionId = currentSessionId.value) => {
    if (loading.value || !targetSessionId) { return }

    loading.value = true
    try {
      const result = await store.dispatch('im/message/loadMessages', {
        sessionId: targetSessionId,
        lastMessageId: null,
        pageSize: 20,
        isLoadMore: false
      })

      // 竞态守卫
      if (currentSessionId.value !== targetSessionId) { return }

      if (!result || result.length === 0) {
        noMore.value = true
      }
    } catch (error) {
      if (currentSessionId.value !== targetSessionId) { return }
      console.error('加载消息失败:', error)
      ElMessage.error('加载消息失败')
    } finally {
      if (currentSessionId.value === targetSessionId) {
        loading.value = false
      }
    }
  }

  /**
   * 加载更多消息（上拉加载，使用列表最老消息 ID 做 cursor）
   */
  const loadMore = async () => {
    if (loading.value || noMore.value) { return }

    const currentMsgs = messages.value
    const firstMsg = currentMsgs[0]
    if (!firstMsg) { return }

    loading.value = true
    try {
      const result = await store.dispatch('im/message/loadMessages', {
        sessionId: currentSessionId.value,
        lastMessageId: firstMsg.id,
        pageSize: 20,
        isLoadMore: true
      })

      if (!result || result.length === 0) {
        noMore.value = true
      }

      return result
    } catch (error) {
      console.error('加载更多消息失败:', error)
      ElMessage.error('历史消息加载失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }

  /**
   * 标记消息为已读
   */
  const markAsRead = async messageId => {
    try {
      await store.dispatch('im/message/markMessageAsRead', messageId)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  // 监听会话ID变化，重置状态并重新加载
  watch(currentSessionId, (newId, oldId) => {
    if (newId !== oldId) {
      noMore.value = false
      loading.value = false
      if (newId) {
        loadHistory(newId)
      }
    }
  })

  return {
    // 状态
    messages,
    loading,
    noMore,

    // 方法
    loadHistory,
    loadMore,
    markAsRead
  }
}
