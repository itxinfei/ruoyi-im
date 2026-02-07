/**
* 虚拟滚动优化 Composable
* 用于优化大量消息时的渲染性能
*/
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'

/**
 * 虚拟滚动配置
 */
const DEFAULT_CONFIG = {
  // 每条消息的预估高度（用于计算虚拟高度）
  estimatedItemHeight: 80,
  // 缓冲区大小（渲染可视区域外的额外条数）
  buffer: 5,
  // 最大消息数（超过后删除旧消息）
  maxMessages: 500,
  // 清理阈值（达到此数量后开始清理）
  cleanupThreshold: 600
}

/**
 * 虚拟滚动 Hook
 * @param {Object} options - 配置选项
 * @returns {Object} 虚拟滚动相关的状态和方法
 */
export function useVirtualScroll(options = {}) {
  const config = { ...DEFAULT_CONFIG, ...options }

  // 容器引用
  const containerRef = ref(null)
  const listRef = ref(null)

  // 消息列表
  const messages = ref([])

  // 滚动位置
  const scrollTop = ref(0)
  const isScrolling = ref(false)
  let scrollTimer = null

  // 容器尺寸
  const containerHeight = ref(0)
  const containerWidth = ref(0)

  // 可见范围
  const visibleRange = computed(() => {
    const itemHeight = config.estimatedItemHeight
    const startIndex = Math.max(0, Math.floor(scrollTop.value / itemHeight) - config.buffer)
    const visibleCount = Math.ceil(containerHeight.value / itemHeight) + config.buffer * 2
    const endIndex = Math.min(messages.value.length, startIndex + visibleCount)

    return { startIndex, endIndex }
  })

  // 可见消息
  const visibleMessages = computed(() => {
    const { startIndex, endIndex } = visibleRange.value
    return messages.value.slice(startIndex, endIndex)
  })

  // 虚拟高度（撑开滚动容器）
  const virtualHeight = computed(() => {
    return messages.value.length * config.estimatedItemHeight
  })

  // 偏移量（用于定位可见消息）
  const offset = computed(() => {
    const { startIndex } = visibleRange.value
    return startIndex * config.estimatedItemHeight
  })

  /**
   * 处理滚动事件
   */
  const handleScroll = (e) => {
    scrollTop.value = e.target.scrollTop
    isScrolling.value = true

    // 防抖滚动状态
    if (scrollTimer) {
      clearTimeout(scrollTimer)
    }
    scrollTimer = setTimeout(() => {
      isScrolling.value = false
    }, 150)
  }

  /**
   * 添加消息到列表
   * @param {Array} newMessages - 新消息数组
   */
  const addMessages = (newMessages) => {
    if (!Array.isArray(newMessages)) {
      newMessages = [newMessages]
    }

    messages.value = [...messages.value, ...newMessages]

    // 检查是否需要清理旧消息
    if (messages.value.length > config.cleanupThreshold) {
      cleanupOldMessages()
    }
  }

  /**
   * 在列表开头添加消息（加载更多）
   * @param {Array} newMessages - 新消息数组
   */
  const prependMessages = (newMessages) => {
    if (!Array.isArray(newMessages)) {
      newMessages = [newMessages]
    }

    messages.value = [...newMessages, ...messages.value]

    // 调整滚动位置，避免跳动
    nextTick(() => {
      if (containerRef.value) {
        const addedHeight = newMessages.length * config.estimatedItemHeight
        containerRef.value.scrollTop += addedHeight
      }
    })
  }

  /**
   * 更新消息
   * @param {string|Function} predicate - 消息 ID 或查找函数
   * @param {Object} updates - 更新内容
   */
  const updateMessage = (predicate, updates) => {
    const index = typeof predicate === 'function'
      ? messages.value.findIndex(predicate)
      : messages.value.findIndex(m => m.id === predicate)

    if (index !== -1) {
      messages.value[index] = { ...messages.value[index], ...updates }
    }
  }

  /**
   * 删除消息
   * @param {string|Function} predicate - 消息 ID 或查找函数
   */
  const removeMessage = (predicate) => {
    const index = typeof predicate === 'function'
      ? messages.value.findIndex(predicate)
      : messages.value.findIndex(m => m.id === predicate)

    if (index !== -1) {
      messages.value = [
        ...messages.value.slice(0, index),
        ...messages.value.slice(index + 1)
      ]
    }
  }

  /**
   * 清理旧消息
   */
  const cleanupOldMessages = () => {
    const keepCount = config.maxMessages
    if (messages.value.length > keepCount) {
      const removed = messages.value.length - keepCount
      messages.value = messages.value.slice(-keepCount)
      console.log(`[虚拟滚动] 清理了 ${removed} 条旧消息`)
    }
  }

  /**
   * 滚动到指定位置
   * @param {number} index - 消息索引
   * @param {string} behavior - 滚动行为
   */
  const scrollToIndex = (index, behavior = 'smooth') => {
    if (!containerRef.value) return

    const targetScrollTop = index * config.estimatedItemHeight
    containerRef.value.scrollTo({
      top: targetScrollTop,
      behavior
    })
  }

  /**
   * 滚动到底部
   */
  const scrollToBottom = () => {
    if (!containerRef.value) return
    containerRef.value.scrollTop = containerRef.value.scrollHeight
  }

  /**
   * 检查是否在底部
   * @param {number} threshold - 阈值（像素）
   */
  const isNearBottom = (threshold = 100) => {
    if (!containerRef.value) return true
    const { scrollTop, scrollHeight, clientHeight } = containerRef.value
    return scrollHeight - scrollTop - clientHeight < threshold
  }

  /**
   * 获取消息总数
   */
  const totalCount = computed(() => messages.value.length)

  /**
   * 获取可见消息数量
   */
  const visibleCount = computed(() => visibleMessages.value.length)

  /**
   * 初始化容器尺寸
   */
  const updateContainerSize = () => {
    if (containerRef.value) {
      containerHeight.value = containerRef.value.clientHeight
      containerWidth.value = containerRef.value.clientWidth
    }
  }

  // 监听窗口大小变化
  const handleResize = () => {
    updateContainerSize()
  }

  onMounted(() => {
    updateContainerSize()
    window.addEventListener('resize', handleResize)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    if (scrollTimer) {
      clearTimeout(scrollTimer)
    }
  })

  return {
    // 状态
    containerRef,
    listRef,
    messages,
    visibleMessages,
    scrollTop,
    isScrolling,
    containerHeight,
    containerWidth,

    // 计算属性
    totalCount,
    visibleCount,
    virtualHeight,
    offset,
    visibleRange,

    // 方法
    handleScroll,
    addMessages,
    prependMessages,
    updateMessage,
    removeMessage,
    cleanupOldMessages,
    scrollToIndex,
    scrollToBottom,
    isNearBottom,
    updateContainerSize
  }
}

/**
 * 简化版虚拟滚动 Hook（适用于现有 MessageList）
 * 只提供性能优化相关的功能，不改变现有结构
 */
export function useVirtualScrollOptimization(options = {}) {
  const config = {
    maxMessages: 500,
    cleanupThreshold: 600,
    ...options
  }

  const messageListRef = ref(null)
  const lastCleanupTime = ref(0)
  const CLEANUP_INTERVAL = 30 * 1000 // 30秒

  /**
   * 清理旧消息（保留最近的消息）
   * @param {Array} messages - 消息列表
   * @returns {Array} 清理后的消息列表
   */
  const cleanupMessages = (messages) => {
    if (!messages || messages.length <= config.maxMessages) {
      return messages
    }

    const now = Date.now()
    // 限制清理频率，避免频繁操作
    if (now - lastCleanupTime.value < CLEANUP_INTERVAL) {
      return messages
    }

    lastCleanupTime.value = now
    const removed = messages.length - config.maxMessages

    console.log(`[消息优化] 清理了 ${removed} 条旧消息，保留 ${config.maxMessages} 条`)

    // 保留最近的消息
    return messages.slice(-config.maxMessages)
  }

  /**
   * 滚动到底部
   */
  const scrollToBottom = (smooth = true) => {
    if (messageListRef.value) {
      const element = messageListRef.value.$el || messageListRef.value
      if (element) {
        element.scrollTo({
          top: element.scrollHeight,
          behavior: smooth ? 'smooth' : 'auto'
        })
      }
    }
  }

  /**
   * 检查是否在底部
   */
  const isNearBottom = (threshold = 100) => {
    if (!messageListRef.value) return true
    const element = messageListRef.value.$el || messageListRef.value
    if (!element) return true

    const { scrollTop, scrollHeight, clientHeight } = element
    return scrollHeight - scrollTop - clientHeight < threshold
  }

  /**
   * 获取滚动容器
   */
  const getScrollContainer = () => {
    if (messageListRef.value) {
      return messageListRef.value.$el || messageListRef.value
    }
    return null
  }

  return {
    messageListRef,
    cleanupMessages,
    scrollToBottom,
    isNearBottom,
    getScrollContainer
  }
}
