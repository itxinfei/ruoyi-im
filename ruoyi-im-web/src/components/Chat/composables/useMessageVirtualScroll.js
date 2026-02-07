/**
 * 消息虚拟滚动 Hook - 性能优化版
 * 对标钉钉/飞书，优化大群消息渲染性能
 *
 * @param {Object} props - 组件 props
 * @param {Function} getAllMessages - 获取所有消息的函数
 * @returns {Object} 虚拟滚动相关的状态和方法
 *
 * @example
 * const {
 *   isLargeGroup,
 *   isLazyLoadingEnabled,
 *   visibleMessages,
 *   topSpacerHeight,
 *   bottomSpacerHeight
 * } = useMessageVirtualScroll(props, () => messagesWithDividers.value)
 */
import { computed, ref, shallowRef } from 'vue'

// 基础配置
const ENABLE_LAZY_LOADING_THRESHOLD = 100 // 消息数超过此阈值时启用懒加载
const LARGE_GROUP_THRESHOLD = 500 // 大群阈值
const AVERAGE_MESSAGE_HEIGHT = 80 // 平均消息高度（像素）
const SCROLL_THROTTLE_MS = 16 // 滚动节流时间（约60fps）
const RECALC_DEBOUNCE_MS = 100 // 重计算防抖时间

const BASE_BUFFER_ZONE = {
  ABOVE: 300, // 视口上方缓冲区
  BELOW: 600, // 视口下方缓冲区
  MESSAGE_COUNT: 30 // 最少渲染消息数
}

/**
 * 检测是否为大群
 * @param {Object} messages - 消息列表
 * @param {String} sessionType - 会话类型
 * @returns {Boolean}
 */
export function detectLargeGroup(messages, sessionType) {
  if (sessionType !== 'GROUP') {
    return false
  }
  const groupMemberCount = messages?.find(m => m.groupMemberCount)?.groupMemberCount
  return !!(groupMemberCount && groupMemberCount >= LARGE_GROUP_THRESHOLD)
}

/**
 * 获取懒加载阈值
 * @param {Boolean} isLargeGroup - 是否大群
 * @returns {Number}
 */
export function getLazyLoadingThreshold(isLargeGroup) {
  return isLargeGroup ? 50 : ENABLE_LAZY_LOADING_THRESHOLD
}

// 导出常量供外部使用
export { AVERAGE_MESSAGE_HEIGHT }

export function useMessageVirtualScroll(props, getAllMessages) {
  // 滚动位置跟踪
  const scrollTop = ref(0)
  const clientHeight = ref(600) // 初始估算值

  // 消息高度缓存 - 使用 shallowRef 提升性能
  const messageHeightCache = shallowRef(new Map())

  // 缓存的可见范围 - 减少重复计算
  const cachedRange = ref({ startIndex: 0, endIndex: 0, lastUpdate: 0 })

  // 节流状态
  let scrollThrottleTimer = null
  let recalcDebounceTimer = null

  // 检测当前会话是否为大群
  const isLargeGroup = computed(() => {
    return detectLargeGroup(props.messages, props.sessionType)
  })

  // 动态懒加载阈值
  const lazyLoadingThreshold = computed(() => {
    return getLazyLoadingThreshold(isLargeGroup.value)
  })

  // 是否启用懒加载
  const isLazyLoadingEnabled = computed(() => {
    return props.messages?.length > lazyLoadingThreshold.value
  })

  // 动态缓冲区配置
  const BUFFER_ZONE = computed(() => {
    if (isLargeGroup.value) {
      return {
        ABOVE: 200,
        BELOW: 400,
        MESSAGE_COUNT: 20
      }
    }
    return BASE_BUFFER_ZONE
  })

  /**
   * 获取消息的估算高度（支持缓存）
   */
  const getMessageHeight = (msgId) => {
    return messageHeightCache.value.get(msgId) || AVERAGE_MESSAGE_HEIGHT
  }

  /**
   * 更新消息高度缓存
   */
  const updateMessageHeight = (msgId, height) => {
    if (msgId && height > 0) {
      const cache = new Map(messageHeightCache.value)
      cache.set(msgId, height)
      messageHeightCache.value = cache
    }
  }

  /**
   * 计算可见区域的消息范围（带缓存）
   * @returns {{startIndex: Number, endIndex: Number}}
   */
  const calculateVisibleRange = () => {
    const allMessages = getAllMessages()
    if (!isLazyLoadingEnabled.value) {
      return { startIndex: 0, endIndex: allMessages.length }
    }

    if (allMessages.length === 0) {
      return { startIndex: 0, endIndex: 0 }
    }

    const buffer = BUFFER_ZONE.value
    const viewportTop = scrollTop.value
    const viewportBottom = scrollTop.value + clientHeight.value

    const renderTop = Math.max(0, viewportTop - buffer.ABOVE)
    const renderBottom = viewportBottom + buffer.BELOW

    // 使用累计高度精确计算起始位置
    let accumulatedHeight = 0
    let startIndex = 0
    let endIndex = allMessages.length

    for (let i = 0; i < allMessages.length; i++) {
      const msgHeight = getMessageHeight(allMessages[i]?.id)
      if (accumulatedHeight + msgHeight >= renderTop && startIndex === 0) {
        startIndex = Math.max(0, i - buffer.MESSAGE_COUNT)
      }
      if (accumulatedHeight >= renderBottom) {
        endIndex = Math.min(allMessages.length, i + buffer.MESSAGE_COUNT)
        break
      }
      accumulatedHeight += msgHeight
    }

    // 确保最少渲染数量
    if (endIndex - startIndex < buffer.MESSAGE_COUNT * 2) {
      const centerIndex = Math.floor((startIndex + endIndex) / 2)
      startIndex = Math.max(0, centerIndex - buffer.MESSAGE_COUNT)
      endIndex = Math.min(allMessages.length, centerIndex + buffer.MESSAGE_COUNT)
    }

    // 更新缓存
    cachedRange.value = { startIndex, endIndex, lastUpdate: Date.now() }

    return { startIndex, endIndex }
  }

  // 顶部占位符高度（使用缓存高度计算）
  const topSpacerHeight = computed(() => {
    if (!isLazyLoadingEnabled.value) { return 0 }
    const { startIndex } = calculateVisibleRange()
    const allMessages = getAllMessages()

    let height = 0
    for (let i = 0; i < startIndex; i++) {
      height += getMessageHeight(allMessages[i]?.id)
    }
    return height
  })

  // 底部占位符高度（使用缓存高度计算）
  const bottomSpacerHeight = computed(() => {
    if (!isLazyLoadingEnabled.value) { return 0 }
    const { endIndex } = calculateVisibleRange()
    const allMessages = getAllMessages()

    let height = 0
    for (let i = endIndex; i < allMessages.length; i++) {
      height += getMessageHeight(allMessages[i]?.id)
    }
    return height
  })

  // 可见消息列表
  const visibleMessages = computed(() => {
    const allMessages = getAllMessages()
    if (allMessages.length === 0) { return [] }

    const validMessages = allMessages.filter(msg => msg && (msg.id || msg.timeText))

    if (!isLazyLoadingEnabled.value) {
      return validMessages
    }

    const { startIndex, endIndex } = calculateVisibleRange()
    return validMessages.slice(startIndex, endIndex)
  })

  /**
   * 更新滚动位置（带节流）
   * @param {HTMLElement} element - 滚动容器元素
   */
  const updateScrollPosition = element => {
    if (!element) { return }

    // 节流处理
    if (scrollThrottleTimer) { return }

    scrollThrottleTimer = setTimeout(() => {
      scrollTop.value = element.scrollTop
      clientHeight.value = element.clientHeight
      scrollThrottleTimer = null
    }, SCROLL_THROTTLE_MS)
  }

  /**
   * 强制刷新可见范围（防抖）
   */
  const forceRecalculate = () => {
    if (recalcDebounceTimer) {
      clearTimeout(recalcDebounceTimer)
    }
    recalcDebounceTimer = setTimeout(() => {
      cachedRange.value = { ...cachedRange.value, lastUpdate: 0 }
      recalcDebounceTimer = null
    }, RECALC_DEBOUNCE_MS)
  }

  return {
    // 状态
    scrollTop,
    clientHeight,
    messageHeightCache,
    // 计算属性
    isLargeGroup,
    isLazyLoadingEnabled,
    topSpacerHeight,
    bottomSpacerHeight,
    visibleMessages,
    // 方法
    calculateVisibleRange,
    updateScrollPosition,
    updateMessageHeight,
    forceRecalculate
  }
}
