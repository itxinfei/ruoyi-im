/**
 * @file useVirtualList.js
 * @description Vue Composable - 虚拟列表的组合式函数
 * @author IM System
 * @version 1.0.0
 */

import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'

/**
 * 虚拟列表组合式函数
 * @param {Object} options - 配置选项
 * @param {Ref<Array>} options.items - 列表数据源
 * @param {number} options.itemHeight - 列表项高度（固定高度模式）
 * @param {Function} options.estimateHeight - 列表项高度估算函数（动态高度模式）
 * @param {number} options.bufferSize - 缓冲区大小，默认 5
 * @param {Ref<HTMLElement>} options.containerRef - 容器 DOM 引用
 * @returns {Object} 虚拟列表相关的状态和方法
 */
export function useVirtualList(options = {}) {
  const { items, itemHeight = 0, estimateHeight = () => 60, bufferSize = 5, containerRef } = options

  // ==================== 响应式状态 ====================

  /** 容器高度 */
  const containerHeight = ref(0)

  /** 滚动位置 */
  const scrollTop = ref(0)

  /** 可见区域起始索引 */
  const startIndex = ref(0)

  /** 可见区域结束索引 */
  const endIndex = ref(0)

  /** 列表项高度缓存（动态高度模式） */
  const heightCache = ref(new Map())

  /** 列表项位置缓存 */
  const positionCache = ref([])

  /** 是否正在滚动 */
  const isScrolling = ref(false)

  /** 滚动定时器 */
  let scrollTimer = null

  /** ResizeObserver 实例 */
  let resizeObserver = null

  // ==================== 计算属性 ====================

  /**
   * 是否为固定高度模式
   */
  const isFixedHeight = computed(() => itemHeight > 0)

  /**
   * 列表总高度
   */
  const totalHeight = computed(() => {
    if (isFixedHeight.value) {
      return items.value.length * itemHeight
    }

    // 动态高度模式：计算所有项的高度之和
    let height = 0
    for (let i = 0; i < items.value.length; i++) {
      height += getItemHeight(i)
    }
    return height
  })

  /**
   * 可见区域内的列表项
   */
  const visibleItems = computed(() => {
    const start = Math.max(0, startIndex.value - bufferSize)
    const end = Math.min(items.value.length, endIndex.value + bufferSize)

    return items.value.slice(start, end).map((item, index) => ({
      item,
      index: start + index,
      style: getItemStyle(start + index),
    }))
  })

  /**
   * 列表容器样式
   */
  const listStyle = computed(() => ({
    height: `${totalHeight.value}px`,
    position: 'relative',
  }))

  // ==================== 方法定义 ====================

  /**
   * 获取列表项高度
   * @param {number} index - 列表项索引
   * @returns {number} 高度
   */
  const getItemHeight = index => {
    if (isFixedHeight.value) {
      return itemHeight
    }

    // 优先使用缓存的实际高度
    if (heightCache.value.has(index)) {
      return heightCache.value.get(index)
    }

    // 使用估算高度
    return estimateHeight(items.value[index], index)
  }

  /**
   * 获取列表项位置
   * @param {number} index - 列表项索引
   * @returns {number} 顶部位置
   */
  const getItemTop = index => {
    if (isFixedHeight.value) {
      return index * itemHeight
    }

    // 使用位置缓存
    if (positionCache.value[index] !== undefined) {
      return positionCache.value[index]
    }

    // 计算位置
    let top = 0
    for (let i = 0; i < index; i++) {
      top += getItemHeight(i)
    }

    // 缓存位置
    positionCache.value[index] = top
    return top
  }

  /**
   * 获取列表项样式
   * @param {number} index - 列表项索引
   * @returns {Object} 样式对象
   */
  const getItemStyle = index => ({
    position: 'absolute',
    top: `${getItemTop(index)}px`,
    left: 0,
    right: 0,
    height: isFixedHeight.value ? `${itemHeight}px` : 'auto',
  })

  /**
   * 根据滚动位置查找起始索引（二分查找）
   * @param {number} scrollTop - 滚动位置
   * @returns {number} 起始索引
   */
  const findStartIndex = scrollTop => {
    if (isFixedHeight.value) {
      return Math.floor(scrollTop / itemHeight)
    }

    // 二分查找
    let low = 0
    let high = items.value.length - 1

    while (low <= high) {
      const mid = Math.floor((low + high) / 2)
      const top = getItemTop(mid)
      const height = getItemHeight(mid)

      if (top + height < scrollTop) {
        low = mid + 1
      } else if (top > scrollTop) {
        high = mid - 1
      } else {
        return mid
      }
    }

    return Math.max(0, low)
  }

  /**
   * 根据起始索引计算结束索引
   * @param {number} startIndex - 起始索引
   * @returns {number} 结束索引
   */
  const findEndIndex = startIndex => {
    if (isFixedHeight.value) {
      return Math.min(
        items.value.length,
        startIndex + Math.ceil(containerHeight.value / itemHeight)
      )
    }

    let height = 0
    let index = startIndex

    while (height < containerHeight.value && index < items.value.length) {
      height += getItemHeight(index)
      index++
    }

    return Math.min(items.value.length, index)
  }

  /**
   * 更新可见范围
   */
  const updateVisibleRange = () => {
    const start = findStartIndex(scrollTop.value)
    const end = findEndIndex(start)

    startIndex.value = start
    endIndex.value = end
  }

  /**
   * 处理滚动事件
   * @param {Event} event - 滚动事件
   */
  const handleScroll = event => {
    scrollTop.value = event.target.scrollTop
    isScrolling.value = true

    updateVisibleRange()

    // 滚动结束检测
    if (scrollTimer) {
      clearTimeout(scrollTimer)
    }

    scrollTimer = setTimeout(() => {
      isScrolling.value = false
    }, 150)
  }

  /**
   * 更新列表项高度缓存
   * @param {number} index - 列表项索引
   * @param {number} height - 实际高度
   */
  const updateItemHeight = (index, height) => {
    if (isFixedHeight.value) return

    const oldHeight = heightCache.value.get(index)
    if (oldHeight !== height) {
      heightCache.value.set(index, height)

      // 清除后续项的位置缓存
      positionCache.value = positionCache.value.slice(0, index)

      // 重新计算可见范围
      nextTick(updateVisibleRange)
    }
  }

  /**
   * 滚动到指定索引
   * @param {number} index - 目标索引
   * @param {Object} options - 滚动选项
   * @param {string} options.behavior - 滚动行为 ('auto' | 'smooth')
   * @param {string} options.align - 对齐方式 ('start' | 'center' | 'end')
   */
  const scrollToIndex = (index, options = {}) => {
    const { behavior = 'auto', align = 'start' } = options

    if (!containerRef.value) return

    const targetIndex = Math.max(0, Math.min(index, items.value.length - 1))
    let targetTop = getItemTop(targetIndex)

    // 根据对齐方式调整滚动位置
    if (align === 'center') {
      targetTop -= (containerHeight.value - getItemHeight(targetIndex)) / 2
    } else if (align === 'end') {
      targetTop -= containerHeight.value - getItemHeight(targetIndex)
    }

    containerRef.value.scrollTo({
      top: Math.max(0, targetTop),
      behavior,
    })
  }

  /**
   * 滚动到底部
   * @param {Object} options - 滚动选项
   */
  const scrollToBottom = (options = {}) => {
    const { behavior = 'smooth' } = options

    if (!containerRef.value) return

    containerRef.value.scrollTo({
      top: totalHeight.value,
      behavior,
    })
  }

  /**
   * 滚动到顶部
   * @param {Object} options - 滚动选项
   */
  const scrollToTop = (options = {}) => {
    const { behavior = 'smooth' } = options

    if (!containerRef.value) return

    containerRef.value.scrollTo({
      top: 0,
      behavior,
    })
  }

  /**
   * 检查是否在底部
   * @param {number} threshold - 阈值
   * @returns {boolean}
   */
  const isAtBottom = (threshold = 50) => {
    if (!containerRef.value) return true

    const { scrollTop, scrollHeight, clientHeight } = containerRef.value
    return scrollHeight - scrollTop - clientHeight <= threshold
  }

  /**
   * 检查是否在顶部
   * @param {number} threshold - 阈值
   * @returns {boolean}
   */
  const isAtTop = (threshold = 50) => {
    return scrollTop.value <= threshold
  }

  /**
   * 重置列表
   */
  const reset = () => {
    scrollTop.value = 0
    startIndex.value = 0
    endIndex.value = 0
    heightCache.value.clear()
    positionCache.value = []

    if (containerRef.value) {
      containerRef.value.scrollTop = 0
    }

    nextTick(updateVisibleRange)
  }

  /**
   * 更新容器尺寸
   */
  const updateContainerSize = () => {
    if (!containerRef.value) return

    containerHeight.value = containerRef.value.clientHeight
    updateVisibleRange()
  }

  // ==================== 监听器 ====================

  /**
   * 监听数据源变化
   */
  watch(
    items,
    () => {
      // 清除高度缓存
      heightCache.value.clear()
      positionCache.value = []

      nextTick(updateVisibleRange)
    },
    { deep: true }
  )

  /**
   * 监听容器引用变化
   */
  watch(containerRef, (newRef, oldRef) => {
    // 移除旧的事件监听
    if (oldRef) {
      oldRef.removeEventListener('scroll', handleScroll)
    }

    // 添加新的事件监听
    if (newRef) {
      newRef.addEventListener('scroll', handleScroll, { passive: true })
      updateContainerSize()
    }
  })

  // ==================== 生命周期 ====================

  onMounted(() => {
    if (containerRef.value) {
      containerRef.value.addEventListener('scroll', handleScroll, { passive: true })
      updateContainerSize()

      // 监听容器尺寸变化
      resizeObserver = new ResizeObserver(() => {
        updateContainerSize()
      })
      resizeObserver.observe(containerRef.value)
    }
  })

  onUnmounted(() => {
    if (containerRef.value) {
      containerRef.value.removeEventListener('scroll', handleScroll)
    }

    if (resizeObserver) {
      resizeObserver.disconnect()
    }

    if (scrollTimer) {
      clearTimeout(scrollTimer)
    }
  })

  // ==================== 返回值 ====================

  return {
    // 状态
    containerHeight,
    scrollTop,
    startIndex,
    endIndex,
    totalHeight,
    isScrolling,

    // 计算属性
    visibleItems,
    listStyle,

    // 方法
    getItemHeight,
    getItemTop,
    getItemStyle,
    updateItemHeight,
    scrollToIndex,
    scrollToBottom,
    scrollToTop,
    isAtBottom,
    isAtTop,
    reset,
    updateVisibleRange,
    updateContainerSize,
  }
}

export default useVirtualList
