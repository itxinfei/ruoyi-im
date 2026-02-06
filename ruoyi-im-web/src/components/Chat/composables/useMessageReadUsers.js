/**
 * 消息已读用户管理 Hook
 *
 * @param {Ref<String|Number>} sessionId - 会话 ID
 * @returns {Object} 已读用户相关的状态和方法
 *
 * @example
 * const {
 *   readUsersMap,
 *   loadingReadUsers,
 *   fetchReadUsers,
 *   prefetchReadUsers,
 *   clearCache
 * } = useMessageReadUsers(computed(() => props.sessionId))
 */
import { ref } from 'vue'
import { getMessageReadUsers, getBatchMessageReadUsers } from '@/api/im/message'

/**
 * 默认配置
 */
const PREFETCH_BATCH_SIZE = 20 // 预加载数量
const CACHE_TTL = 5 * 60 * 1000 // 缓存有效期（5分钟）

export function useMessageReadUsers (sessionId) {
  const readUsersMap = ref({})
  const loadingReadUsers = ref({})
  const cacheTimestamps = ref({})

  /**
   * 检查缓存是否有效
   * @param {String} msgId - 消息 ID
   * @returns {Boolean}
   */
  const isCacheValid = (msgId) => {
    const timestamp = cacheTimestamps.value[msgId]
    if (!timestamp) return false
    return Date.now() - timestamp < CACHE_TTL
  }

  /**
   * 获取单条消息的已读用户列表
   * @param {Object} msg - 消息对象
   * @returns {Promise<void>}
   */
  const fetchReadUsers = async (msg) => {
    if (!msg?.id) return

    // 缓存有效或正在加载，跳过
    if (isCacheValid(msg.id) || loadingReadUsers.value[msg.id]) {
      return
    }

    loadingReadUsers.value[msg.id] = true
    try {
      const res = await getMessageReadUsers(sessionId.value, msg.id)
      if (res.code === 200) {
        readUsersMap.value[msg.id] = res.data
        cacheTimestamps.value[msg.id] = Date.now()
      }
    } catch (error) {
      console.error('获取已读用户失败:', error)
    } finally {
      loadingReadUsers.value[msg.id] = false
    }
  }

  /**
   * 批量预加载已读用户列表
   * @param {Array} messages - 需要预加载的消息列表
   * @returns {Promise<void>}
   */
  const prefetchReadUsers = async (messages) => {
    if (!messages || messages.length === 0) { return }

    // 过滤出需要查询的消息（群聊、有已读数据、缓存无效或未加载）
    const messagesToFetch = messages.filter(msg =>
      msg.groupMemberCount &&
      (msg.readCount > 0 || msg.isRead) &&
      !loadingReadUsers.value[msg.id] &&
      !isCacheValid(msg.id) // 检查缓存有效性（包括数据不存在的情况）
    )

    if (messagesToFetch.length === 0) { return }

    // 设置加载状态
    messagesToFetch.forEach(msg => {
      loadingReadUsers.value[msg.id] = true
    })

    try {
      const messageIds = messagesToFetch.map(m => m.id)
      const res = await getBatchMessageReadUsers(messageIds)

      if (res.code === 200) {
        const now = Date.now()
        Object.keys(res.data).forEach(msgId => {
          readUsersMap.value[msgId] = res.data[msgId]
          cacheTimestamps.value[msgId] = now
        })
      }
    } catch (error) {
      console.error('批量获取已读用户失败:', error)
    } finally {
      messagesToFetch.forEach(msg => {
        loadingReadUsers.value[msg.id] = false
      })
    }
  }

  /**
   * 获取消息的已读用户列表
   * @param {String} msgId - 消息 ID
   * @returns {Array|null}
   */
  const getReadUsers = (msgId) => {
    return readUsersMap.value[msgId] || null
  }

  /**
   * 检查消息是否正在加载已读用户
   * @param {String} msgId - 消息 ID
   * @returns {Boolean}
   */
  const isLoading = (msgId) => {
    return !!loadingReadUsers.value[msgId]
  }

  /**
   * 清空缓存
   */
  const clearCache = () => {
    readUsersMap.value = {}
    loadingReadUsers.value = {}
    cacheTimestamps.value = {}
  }

  /**
   * 清除单个消息的缓存
   * @param {String} msgId - 消息 ID
   */
  const clearMessageCache = (msgId) => {
    delete readUsersMap.value[msgId]
    delete cacheTimestamps.value[msgId]
  }

  return {
    // 状态
    readUsersMap,
    loadingReadUsers,
    // 方法
    fetchReadUsers,
    prefetchReadUsers,
    getReadUsers,
    isLoading,
    clearCache,
    clearMessageCache,
    isCacheValid
  }
}
