/**
 * 消息列表滚动处理 Hook
 *
 * @param {Ref<Object>} listRef - 列表容器 ref
 * @param {Function} emit - emit 函数
 * @param {Ref<Boolean>} isUnmounted - 组件是否已卸载
 * @returns {Object} 滚动相关的状态和方法
 *
 * @example
 * const {
 *   showScrollToBottom,
 *   scrollToBottom,
 *   scrollToMsg,
 *   handleScroll
 * } = useMessageScroll(listRef, emit, isUnmounted)
 */
import { ref, nextTick, onUnmounted } from 'vue'

// 滚动加载配置
const SCROLL_LOAD_THRESHOLD = 100 // 距离底部多少像素时触发加载
const SCROLL_DEBOUNCE_TIME = 100 // 滚动防抖时间（毫秒）
const SCROLL_TO_BOTTOM_THRESHOLD = 200 // 距离底部多少像素时显示"滚动到底部"按钮

export function useMessageScroll (listRef, emit, isUnmounted) {
  const showScrollToBottom = ref(false)
  let scrollTimer = null
  let lastScrollTop = 0
  let isScrollingUp = false

  /**
   * 滚动到底部
   * @param {Boolean} smooth - 是否平滑滚动
   */
  const scrollToBottom = (smooth = true) => {
    if (isUnmounted.value) { return }

    nextTick(() => {
      if (listRef.value) {
        const scrollHeight = listRef.value.scrollHeight
        listRef.value.scrollTo({
          top: scrollHeight,
          behavior: smooth ? 'smooth' : 'auto'
        })
      }
    })
  }

  /**
   * 滚动到指定消息
   * @param {String|Number} messageId - 消息 ID
   */
  const scrollToMsg = (messageId) => {
    if (!messageId) { return }

    nextTick(() => {
      const targetElement = listRef.value?.querySelector(`[data-id="${messageId}"]`)
      if (targetElement) {
        targetElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
        // 高亮效果
        targetElement.classList.add('highlight')
        setTimeout(() => {
          targetElement.classList.remove('highlight')
        }, 2000)
      }
    })
  }

  /**
   * 滚动到指定位置（像素）
   * @param {Number} position - 目标位置
   * @param {Boolean} smooth - 是否平滑滚动
   */
  const scrollToPosition = (position, smooth = false) => {
    if (isUnmounted.value) { return }

    nextTick(() => {
      if (listRef.value) {
        listRef.value.scrollTo({
          top: position,
          behavior: smooth ? 'smooth' : 'auto'
        })
      }
    })
  }

  /**
   * 获取当前滚动位置
   * @returns {Number}
   */
  const getScrollPosition = () => {
    return listRef.value?.scrollTop || 0
  }

  /**
   * 检查是否在底部
   * @returns {Boolean}
   */
  const isAtBottom = () => {
    if (!listRef.value) { return true }
    const { scrollTop, scrollHeight, clientHeight } = listRef.value
    return scrollHeight - scrollTop - clientHeight < SCROLL_LOAD_THRESHOLD
  }

  /**
   * 检查是否在顶部
   * @returns {Boolean}
   */
  const isAtTop = () => {
    if (!listRef.value) { return true }
    return listRef.value.scrollTop < 50
  }

  /**
   * 处理滚动事件
   * @param {Event} event - 滚动事件
   */
  const handleScroll = (event) => {
    const target = event.target
    const currentScrollTop = target.scrollTop

    // 判断滚动方向
    isScrollingUp = currentScrollTop < lastScrollTop
    lastScrollTop = currentScrollTop

    // 清除之前的定时器
    if (scrollTimer) {
      clearTimeout(scrollTimer)
    }

    // 防抖处理
    scrollTimer = setTimeout(() => {
      const { scrollTop, scrollHeight, clientHeight } = target
      const distanceToBottom = scrollHeight - scrollTop - clientHeight

      // 显示/隐藏"滚动到底部"按钮
      showScrollToBottom.value = distanceToBottom > SCROLL_TO_BOTTOM_THRESHOLD

      // 向上滚动到顶部时，触发加载更多
      if (scrollTop < 50 && isScrollingUp) {
        emit('load-more', 'top')
      }

      // 滚动到底部时，触发加载更多
      if (distanceToBottom < SCROLL_LOAD_THRESHOLD) {
        emit('load-more', 'bottom')
      }
    }, SCROLL_DEBOUNCE_TIME)
  }

  /**
   * 滚动到顶部并加载更多历史消息
   */
  const scrollToTopAndLoad = () => {
    if (isUnmounted.value) { return }

    nextTick(() => {
      if (listRef.value) {
        listRef.value.scrollTo({
          top: 0,
          behavior: 'auto'
        })
      }
      emit('load-more', 'top')
    })
  }

  /**
   * 保持当前滚动位置（用于加载历史消息后）
   * @returns {Number} 旧滚动位置
   */
  const maintainScrollPosition = () => {
    const oldScrollHeight = listRef.value?.scrollHeight || 0
    const oldScrollTop = getScrollPosition()

    nextTick(() => {
      if (listRef.value) {
        const newScrollHeight = listRef.value.scrollHeight
        const scrollDiff = newScrollHeight - oldScrollHeight
        listRef.value.scrollTop = oldScrollTop + scrollDiff
      }
    })

    return oldScrollTop
  }

  // 清理
  onUnmounted(() => {
    if (scrollTimer) {
      clearTimeout(scrollTimer)
    }
  })

  return {
    // 状态
    showScrollToBottom,
    // 滚动方法
    scrollToBottom,
    scrollToMsg,
    scrollToPosition,
    scrollToTopAndLoad,
    maintainScrollPosition,
    // 查询方法
    getScrollPosition,
    isAtBottom,
    isAtTop,
    // 事件处理
    handleScroll
  }
}
