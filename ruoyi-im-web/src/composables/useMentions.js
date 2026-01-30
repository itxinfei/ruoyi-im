/**
 * @提及管理 Composable
 * 管理未读@提及数据，提供加载、标记已读等功能
 */
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { getUnreadMentions, getUnreadMentionCount, markMentionAsRead } from '@/api/im/message'

export function useMentions() {
  const store = useStore()

  // 未读提及列表
  const unreadMentions = ref([])

  // 未读提及数量
  const unreadCount = ref(0)

  // 加载状态
  const loading = ref(false)

  /**
   * 加载未读提及列表
   */
  const loadUnreadMentions = async () => {
    try {
      loading.value = true
      const { data } = await getUnreadMentions()
      unreadMentions.value = data || []
      return data || []
    } catch (error) {
      console.error('加载未读@提及失败:', error)
      return []
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载未读提及数量
   */
  const loadUnreadCount = async () => {
    try {
      const { data } = await getUnreadMentionCount()
      unreadCount.value = data || 0
      return data || 0
    } catch (error) {
      console.error('加载未读@提及数量失败:', error)
      return 0
    }
  }

  /**
   * 批量加载未读提及数据
   */
  const loadAll = async () => {
    await Promise.all([
      loadUnreadMentions(),
      loadUnreadCount()
    ])
  }

  /**
   * 标记提及为已读
   * @param {number} messageId - 消息ID
   */
  const markAsRead = async (messageId) => {
    try {
      await markMentionAsRead(messageId)

      // 更新本地状态
      unreadMentions.value = unreadMentions.value.filter(m => m.messageId !== messageId)
      unreadCount.value = Math.max(0, unreadCount.value - 1)

      return true
    } catch (error) {
      console.error('标记@提及已读失败:', error)
      return false
    }
  }

  /**
   * 批量标记提及为已读
   * @param {number[]} messageIds - 消息ID列表
   */
  const batchMarkAsRead = async (messageIds) => {
    try {
      // 逐个标记（如果后端支持批量API可以优化）
      await Promise.all(messageIds.map(id => markMentionAsRead(id)))

      // 更新本地状态
      const idSet = new Set(messageIds)
      unreadMentions.value = unreadMentions.value.filter(m => !idSet.has(m.messageId))
      unreadCount.value = Math.max(0, unreadCount.value - messageIds.length)

      return true
    } catch (error) {
      console.error('批量标记@提及已读失败:', error)
      return false
    }
  }

  /**
   * 获取指定会话的未读提及数量
   * @param {number} conversationId - 会话ID
   */
  const getUnreadCountByConversation = (conversationId) => {
    return unreadMentions.value.filter(m => m.conversationId === conversationId).length
  }

  /**
   * 获取指定消息是否被提及
   * @param {number} messageId - 消息ID
   */
  const isMessageMentioned = (messageId) => {
    return unreadMentions.value.some(m => m.messageId === messageId)
  }

  /**
   * 获取指定消息的提及信息
   * @param {number} messageId - 消息ID
   */
  const getMessageMention = (messageId) => {
    return unreadMentions.value.find(m => m.messageId === messageId)
  }

  /**
   * 清空所有未读提及（用于测试）
   */
  const clearAll = () => {
    unreadMentions.value = []
    unreadCount.value = 0
  }

  return {
    // 状态
    unreadMentions,
    unreadCount,
    loading,

    // 计算属性
    hasUnread: computed(() => unreadCount.value > 0),

    // 方法
    loadUnreadMentions,
    loadUnreadCount,
    loadAll,
    markAsRead,
    batchMarkAsRead,
    getUnreadCountByConversation,
    isMessageMentioned,
    getMessageMention,
    clearAll
  }
}
